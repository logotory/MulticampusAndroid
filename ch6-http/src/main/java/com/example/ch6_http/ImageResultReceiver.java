package com.example.ch6_http;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by student on 2016-12-09.
 *
 * 이 클래스는 activeity와 Server간 데이터를 call by reference 방식으로
 * 주고 받기 위해 상호 공유하는 객체
 *
 * activity가 생성해서 service 구동시키면서 extra 데이터로 넘긴다..
 */

public class ImageResultReceiver extends ResultReceiver{



    //activity 에서 결과를 받기 위한 callback interface
    public interface ImageResultCallback {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    private ImageResultCallback callback;

    public ImageResultReceiver(Handler handler) {
        super(handler);
    }

    //activity 쪽에서 결과를 받기 위한 callback 등록목적으로 호출..
    public void setReceiver(ImageResultCallback callback) {
        this.callback = callback;
    }


    //service 쪽에서 데이터 발생 순간 이 객체의 send 함수 호출하면
    //아래의 함수가 자동 호출..
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if(callback != null) {
            //activity의 callback을 호출에서 데이터 이용하게..
            callback.onReceiveResult(resultCode, resultData);
        }
    }


}
