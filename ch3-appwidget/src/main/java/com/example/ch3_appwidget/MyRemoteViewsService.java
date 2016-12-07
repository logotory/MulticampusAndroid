package com.example.ch3_appwidget;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

//AppWidget의 ListView를 완성하기 위한 Factory 클래스를 획득할 목적으로
//Launcher App에 의해 실행
public class MyRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d("kkang", "RemoteViewSerivce...");
        return new MyRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
