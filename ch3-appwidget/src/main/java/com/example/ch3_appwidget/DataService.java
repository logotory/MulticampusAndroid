package com.example.ch3_appwidget;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

//30분 미만 주기로 appwidgeht을 위한 데이터 발생 혁활.,.
//데이터 적용은 appwidget receiver에 넘겨서
public class DataService extends Service {

    boolean isRun = false;

    public DataService() {
    }

    //screen on 시 시스템에 띄우는 intent에 반응할 receiver..
    //receiver는 아주 자주 특정 commponent(activity, service)내의 inner로
    //만들어지고 .. manifest에 등록하지 않고 코드에서 등록/해제가 가능

    //screen on 시에 정상적인 데이터 발생.. off시에 데이터 발생 멈춘다..
    //receiver off를 코드에서 등록/해제 하는 경우는?
    //==> 이 receiver가 특정 상황에만 되면 무조건 수행되어야 한다면.. manifeset 에 등록..
    //==> recever가 특정 component(activity, service)가 동작중에만 의미가 있다면..
    //그 component inner로 만들고,.. 코드에서 등록/해제
    BroadcastReceiver brOn = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isRun = true;
            DataThread t = new DataThread();
            t.start();
        }
    };

    BroadcastReceiver brOff = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isRun = false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("kkang", "DataService onCreate....");

        registerReceiver(brOn, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(brOff, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        isRun = true;
        DataThread t = new DataThread();
        t.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(brOn);
        unregisterReceiver(brOff);
        isRun = false;
    }

    class DataThread extends Thread {
        @Override
        public void run() {
            //여기에 network프로그램을 짜면 된다.
            int count = 1;
            while (isRun){
                Log.d("kkang", "DataSerivce Thread.." + count);
                SystemClock.sleep(10000);

                //데이터 발생..db에 저장.. 저장된 데이터를 select는
                //Adapter(RemoteViewsFactorys)에서..
                DBHelper dbHelper = new DBHelper(DataService.this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("insert into tb_data (content) values (?)", new String []{"count: :" + count});
                db.close();

                //Receiver실행 시켜서 .. 데이터 변경이 있다는 의뢰고 들어가게
                Intent intent = new Intent("com.multi.ACTION_DATA");
                sendBroadcast(intent);
                count++;
                if(count == 7)
                    break;
            }

            stopSelf(); //service 죽는다.. 테스트성이어서
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
