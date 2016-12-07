package com.example.ch3_aldi_client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.example.ch3_aidl.IPlayService;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    IPlayService pService;
    ImageButton start;
    ImageButton stop;
    ProgressBar mProgress;

    boolean isRunning = true;
    ProgressThread pt;

    Handler handler;
    Intent intent;


    //--add 1---------------
    //bindService의 리턴값을 받을수 있는 ServiceConnection 구현클래스
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //실제 넘어온건 Service쪽과 통신을 대ㅎㅇ할 Stub객체
            //물론 api 가 동일해서.. 실 service처럼 이용하면 된다..
            pService = IPlayService.Stub.asInterface(iBinder);
            start.setEnabled(true); //바인딩이 성공한 순간 버튼 누를수 있게 해줄거다
            checkService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            pService = null;
        }
    };

	//------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        start = (ImageButton) findViewById(R.id.start);
        stop = (ImageButton) findViewById(R.id.stop);

        mProgress = (ProgressBar) findViewById(R.id.pb);
        mProgress.setProgress(0);

        start.setOnClickListener(this);
        stop.setOnClickListener(this);

        start.setEnabled(false);
        stop.setEnabled(false);


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case 1:	// stopped media
                        start.setEnabled(true);
                        stop.setEnabled(false);
                        mProgress.setProgress(0);
                        break;
                }
                super.handleMessage(msg);
            }
        };

		//add2-------------------------------------
        intent = new Intent("com.multi.ACTION_PLAY");
        //Lollipop부터 bindSerivice의 경우 대상 Serivce를 가지는 app의
        //package명을 꼭 주어야..
        intent.setPackage("com.example.ch3_aidl");

		//--------------------------------------
    }

    private void checkService() {
        if(pService != null) {
            try {
                if(pService.getMediaStatus() == CommonProperties.MEDIA_STATUS_STOP) {
                    Log.d("kkang", "MEDIA_STATUS_STOP");
                    stop.setEnabled(false);
                } else if(pService.getMediaStatus() == CommonProperties.MEDIA_STATUS_RUNNING) {
                    Log.d("kkang", "MEDIA_STATUS_RUNNING");
                    start.setEnabled(false);
                    isRunning = true;
                    pt = new ProgressThread();
                    pt.start();
                }
            } catch (RemoteException e) {

            }
        }

    }

    //add3-------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        checkService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(connection);
        isRunning = false;
    }

    //---------------------------------

    @Override
    public void onClick(View v) {
        if(v == start) {
            try {
                pService.start();
                mProgress.setMax(pService.getMaxDuration());
                isRunning = true;
                pt = new ProgressThread();
                pt.start();

                start.setEnabled(false);
                stop.setEnabled(true);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if(v == stop) {
            try {
                pService.stop();
                isRunning = false;
                start.setEnabled(true);
                stop.setEnabled(false);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }


    class ProgressThread extends Thread {
        @Override
        public void run() {
            while(isRunning) {
                try {

                    if(pService.getMediaStatus() == CommonProperties.MEDIA_STATUS_COMPLETED) {
                        handler.sendEmptyMessage(1);
                        break;
                    } else {
                        mProgress.setProgress(pService.currentPosition());
                        SystemClock.sleep(1000);
                    }
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}