package com.example.ch4_camera;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.media.Image;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.util.Size;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by kkang on 2015-02-05.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Util {
    //사이즈 비교 로직
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }
    //preview 화면 size 결정을 위해 가능한 size중 가장 작은 size를 선택하는 로직
    //choices : 디바이스에서 제공하는 size 목록
    //width, height : surface의 사이즈
    //surface의 사이즈 보다 큰것중 가장 작은것을 획득 목적
    static Size chooseBigEnoughSize(Size[] choices, int width, int height) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<Size>();
        for (Size option : choices) {
            if (option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e("kkang", "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    static class CapturedImageSaver implements Runnable {
        private Image mCapture;
        public CapturedImageSaver(Image capture) {
            mCapture = capture;
        }
        @Override
        public void run() {
            try {
                File file = File.createTempFile("kkang", ".jpg",
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES));
                try (FileOutputStream ostream = new FileOutputStream(file)) {
                    Log.i("kkang", "Retrieved image is" +
                            (mCapture.getFormat() == ImageFormat.JPEG ? "" : "n't") + " a JPEG");
                    ByteBuffer buffer = mCapture.getPlanes()[0].getBuffer();
                    Log.i("kkang", "Captured image size: " +
                            mCapture.getWidth() + 'x' + mCapture.getHeight());
                    byte[] jpeg = new byte[buffer.remaining()];
                    buffer.get(jpeg);
                    ostream.write(jpeg);
                    Log.d("kkang", "file write........................" + file.getAbsolutePath());
                } catch (FileNotFoundException ex) {
                    Log.e("kkang", "Unable to open output file for writing", ex);
                } catch (IOException ex) {
                    Log.e("kkang", "Failed to write the image to the output file", ex);
                }
            } catch (IOException ex) {
                Log.e("kkang", "Unable to create a new output file", ex);
            } finally {
                mCapture.close();
            }
        }
    }
}
