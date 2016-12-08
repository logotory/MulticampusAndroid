package com.example.ch4_camera;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by student on 2016-12-08.
 */

public class MyView extends SurfaceView implements SurfaceHolder.Callback{

    Activity context;
    SurfaceHolder holder;  //surface 작업자.. 우리의 경우는
    //직접 작업 없이 넘어오는 surface를 받기만 하면 된다.

    Camera camera;

    //camera에서 지원하는 preview 사이즈 목록..
    List<Camera.Size> supportedPreviewSizes;
    //privew 사이즈 목록중 결정된 preview 사이즈...
    Camera.Size previewSize;

    int width;
    int height;

    public MyView(Activity context) {
        super(context);
        this.context = context;
        holder = getHolder();
        holder.addCallback(this);
    }

    //폰마다 SCREEN 사이즈가 다르다.. 종횡비도 다르고..
    //폰의 카메라마다 지원하는 preview 사이즈가 다르다..
    //폰 screen size, 지원 preview 사이즈로 계산해서 최적의 사이즈 값으로
    //대입하지 않으면 preview 화면 이상하게 나온다..
    //화면 회전에 대응하기 위한 준비도..

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private void setCameraDisplayOrientation(Activity activity, int cameraId)
    {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation)
        {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        }
        else
        { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    //surface가 준비될때 최초에 한번 호출...
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        camera = Camera.open();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        try {
            //아래의 코드에 의해 camera가 그린 surface가 holder에 전달..
            camera.setPreviewDisplay(holder);
            Camera.Parameters parameters = camera.getParameters();

            //지원되는 preview사이즈 목록 획득..
            supportedPreviewSizes = parameters.getSupportedPreviewSizes();

            //preview 사이즈를 개발자 임의 사이즈로 지정하면 에러..
            //꼭 camera에서 지원하는 사이즈중 하나를 지정..
            if(supportedPreviewSizes != null) {
                previewSize = getOptimalPreviewSize(supportedPreviewSizes, width, height);
            }

        } catch (Exception e) {
            camera.release();
            camera = null;
        }
    }

    //surface 사이즈가 변경될때 마다 호출.. surface 사이즈 전달 목적으로
    //일반적으로는 create 호출수 바로 호출되고 호출 안된다..
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        setCameraDisplayOrientation(context, 0);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(parameters);

        //화면에 preivew 출력 시작..
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if(camera != null) {
            camera.stopPreview();;
            camera.release();
            camera = null;
        }
    }

    public void capture() {
        if(camera != null) {
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    FileOutputStream fout;
                    try {
                        //개발자 임의 디렉토리 지정..
                        File dir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/multi");

                        if(!dir.exists()){
                            dir.mkdir();
                        }

                        //파일 이름 ..여러번 사진 찍는다.. 파일명 중복 문제는?
                        //temp 파일로
                        File file = File.createTempFile("kkang", ".jpg", dir);
                        if(!file.exists()) {
                            file.createNewFile();

                        }

                        fout = new FileOutputStream(file);
                        fout.write(bytes);
                        fout.flush();
                        fout.close();
                        Log.d("kkang", file.getAbsolutePath());

                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    camera.startPreview();
                }
            });
        }
    }
}
