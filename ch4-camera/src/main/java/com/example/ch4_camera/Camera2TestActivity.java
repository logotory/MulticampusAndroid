package com.example.ch4_camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@TargetApi(Build.VERSION_CODES.M)
public class Camera2TestActivity extends AppCompatActivity {

    /** 사진 찍기 등을 background 에서 처리하기 위해서. thread-handler를 간단하게 사용하는 방법 */
    HandlerThread backgroundThread;
    Handler backgroundHandler;

    SurfaceView surfaceView;

    CameraManager manager;
    CameraDevice camera;

    /** surface에서 사진데이터 추출 */
    ImageReader imgReader;
    /** surface 화면을 preview 로 찍거나 사진추출 기능 제공.. */
    CameraCaptureSession captureSession;

    @Override
    protected void onResume() {
        super.onResume();
        backgroundThread = new HandlerThread("background");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        manager = (CameraManager) getSystemService(CAMERA_SERVICE);

        setContentView(R.layout.activity_camera2_test);

        surfaceView = (SurfaceView)findViewById(R.id.mainSurfaceView);
        Button btn=(Button)findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capture();
            }
        });
        surfaceView.getHolder().addCallback(holderCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            // 초기화
            surfaceView.getHolder().setFixedSize(/*width*/0, /*height*/0);
            if (captureSession != null) {
                captureSession.close();
                captureSession = null;
            }
        } finally {
            if (camera != null) {
                camera.close();
                camera = null;
            }
        }
        //file capture-write thread 종료
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();//thread 종료시까지 대기
        } catch (InterruptedException ex) {
            Log.e("kkang", "Background worker thread was interrupted while joined", ex);
        }
        if (imgReader != null) imgReader.close();
    }
    //개발자 함수.. button click 시 호출.. 사진 촬영
    private void capture() {
        if (captureSession != null) {
            try {
                CaptureRequest.Builder requester =
                        camera.createCaptureRequest(camera.TEMPLATE_STILL_CAPTURE);
                requester.addTarget(imgReader.getSurface());
                try {
                    //surfaceview의 화면 capture 명령
                    //1. ImageReader가 surface 의 화면 read
                    //2. read 완료되면 ImageReader에 등록된 OnImageAvailableListener 호출
                    //3. listener에서 acquireNextImage함수로 이미지 추출
                    //4. file write background thread 를 이용하여 file write..
                    captureSession.capture(requester.build(), /*listener*/null, /*handler*/null);
                } catch (CameraAccessException ex) {
                    Log.e("kkang", "Failed to file actual capture request", ex);
                }
            } catch (CameraAccessException ex) {
                Log.e("kkang", "Failed to build actual capture request", ex);
            }
        } else {
            Log.e("kkang", "User attempted to perform a capture outside our session");
        }
    }

    final SurfaceHolder.Callback holderCallback = new SurfaceHolder.Callback() {
        private String mCameraId;
        private boolean mGotSecondCallback;
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i("kkang", "Surface created");
            mCameraId = null;
            mGotSecondCallback = false;
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i("kkang", "Surface destroyed");
            holder.removeCallback(this);
        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (mCameraId == null && camera==null) {
                try {
                    for (String cameraId : manager.getCameraIdList()) {
                        CameraCharacteristics cameraCharacteristics =
                                manager.getCameraCharacteristics(cameraId);
                        if (cameraCharacteristics.get(cameraCharacteristics.LENS_FACING) ==
                                CameraCharacteristics.LENS_FACING_BACK) {
                            Log.i("kkang", "Found a back-facing camera");
                            StreamConfigurationMap info = cameraCharacteristics
                                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                            Size largestSize = Collections.max(
                                    Arrays.asList(info.getOutputSizes(ImageFormat.JPEG)),
                                    new Camera2Util.CompareSizesByArea());

                            imgReader = ImageReader.newInstance(largestSize.getWidth(),
                                    largestSize.getHeight(), ImageFormat.JPEG, /*maxImages*/2);
                            imgReader.setOnImageAvailableListener(
                                    mImageCaptureListener, backgroundHandler);

                            Size optimalSize = Camera2Util.chooseBigEnoughSize(
                                    info.getOutputSizes(SurfaceHolder.class), width, height);

                            SurfaceHolder surfaceHolder = surfaceView.getHolder();
                            surfaceHolder.setFixedSize(optimalSize.getWidth(),
                                    optimalSize.getHeight());

                            mCameraId = cameraId;
                            return;
                        }
                    }
                } catch (CameraAccessException ex) {
                    Log.e("kkang", "Unable to list cameras", ex);
                }
                Log.e("kkang", "Didn't find any back-facing cameras");
            } else if (!mGotSecondCallback) {
                if (camera != null) {
                    Log.e("kkang", "Aborting camera open because it hadn't been closed");
                    return;
                }
                try {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            manager.openCamera(mCameraId, mCameraStateCallback,
                                    backgroundHandler);
                        }
                    }else {
                        manager.openCamera(mCameraId, mCameraStateCallback,
                                backgroundHandler);
                    }

                } catch (Exception ex) {
                    Log.e("kkang", "Failed to configure output surface", ex);
                }
                mGotSecondCallback = true;
            }
        }

    };

    final CameraDevice.StateCallback mCameraStateCallback =
            new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {

                    Camera2TestActivity.this.camera = camera;
                    try {
                        Log.i("kkang", "Successfully opened camera");
                        List<Surface> outputs = Arrays.asList(
                                surfaceView.getHolder().getSurface(), imgReader.getSurface());

                        //camera open 시점에 화면을 capture 할수 있는 session 을 만든다.
                        camera.createCaptureSession(outputs, mCaptureSessionListener,
                                backgroundHandler);

                    } catch (Exception ex) {
                        Log.e("kkang", "Failed to create a capture session", ex);
                    }
                }
                @Override
                public void onDisconnected(CameraDevice camera) {
                    Log.e("kkang", "Camera was disconnected");
                }
                @Override
                public void onError(CameraDevice camera, int error) {
                    Log.e("kkang", "State error on device '" + camera.getId() + "': code " + error);
                }};

    final CameraCaptureSession.StateCallback mCaptureSessionListener =
            new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    Log.i("kkang", "Finished configuring camera outputs");
                    captureSession = session;
                    SurfaceHolder holder = surfaceView.getHolder();
                    if (holder != null) {
                        try {
                            CaptureRequest.Builder requestBuilder =
                                    camera.createCaptureRequest(camera.TEMPLATE_PREVIEW);
                            requestBuilder.addTarget(holder.getSurface());
                            CaptureRequest previewRequest = requestBuilder.build();
                            //preview 화면 출력..
                            try {
                                session.setRepeatingRequest(previewRequest, /*listener*/null,
                                /*handler*/null);
                            } catch (CameraAccessException ex) {
                                Log.e("kkang", "Failed to make repeating preview request", ex);
                            }
                        } catch (CameraAccessException ex) {
                            Log.e("kkang", "Failed to build preview request", ex);
                        }
                    }
                    else {
                        Log.e("kkang", "Holder didn't exist when trying to formulate preview request");
                    }
                }
                @Override
                public void onClosed(CameraCaptureSession session) {
                    captureSession = null;
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e("kkang", "Configuration error on device '" + camera.getId());
                }};

    final ImageReader.OnImageAvailableListener mImageCaptureListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.d("kkang","onImageAvailable..................");
                    backgroundHandler.post(new Camera2Util.CapturedImageSaver(reader.acquireNextImage()));
                }};

}
