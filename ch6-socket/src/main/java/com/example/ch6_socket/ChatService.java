package com.example.ch6_socket;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;

public class ChatService extends Service {

    Socket socket;
    BufferedInputStream bin;
    BufferedOutputStream bout;

    SocketThread st;
    ReadThread rt;

    boolean isCheck = true;
    boolean isConnected = false;

    public ChatService() {
    }

    //activity에서 발생한 caht data를 service가 받아야 한다.
    BroadcastReceiver chatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Log.d("kkang", "service receivers..");
                bout.write(intent.getStringExtra("msg").getBytes("utf-8"));
                bout.flush();
            }catch (Exception e) {
                //현 연결 문제있다..
                isConnected = false; //연결관리 thread가 새로 연결하게
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(chatReceiver, new IntentFilter("com.multi.ACTION_TO_SERVICE"));
        Log.d("kkang", "Service start...");

        st = new SocketThread();
        st.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(chatReceiver);
        isCheck = false;
        isConnected = false;
        if(socket != null) {
            rt.isRead = false;
            try {
                bout.close();
                bin.close();
                socket.close();
            }catch (Exception e) {

            }
        }
    }

    //연결 상태 파악.. 연결이 안된경우 다시 연결시도 역할..
    class SocketThread extends Thread {
        @Override
        public void run() {
            Log.d("kkang", "SockenThread start...");
            while (isCheck) {
                try {
                    if(!isConnected) {
                        socket = new Socket();
                        SocketAddress remoteAttr = new InetSocketAddress("70.12.108.90", 7070);
                        socket.connect(remoteAttr, 10000);
                        bout = new BufferedOutputStream(socket.getOutputStream());
                        bin = new BufferedInputStream(socket.getInputStream());
                        if(rt != null) {
                            rt.isRead = false;
                        }

                        //새로운 연결정보로 read start
                        rt = new ReadThread();
                        rt.start();

                        isConnected = true;
                        Log.d("kkang", "connection ok~~~");
                    }else  {
                        //잘 연결된 상태...
                        SystemClock.sleep(10000);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ReadThread extends Thread {
        boolean isRead = true;

        @Override
        public void run() {
            Log.d("kkang", "read thread start...");
            byte [] buffer = null;
            while (isRead) {
                buffer = new byte[1024];
                try {

                    String message = null;
                    //아래의 코드만 만나면 대기상태..서버로부터 데이터 넘어올때 까지
                    //넘어온 데이터를 buffer에 저장해주고 몇 byte를 읽었는지 리턴

                    int size = bin.read(buffer);
                    Log.d("kkang", "data read..."+size);

                    if(size > 0) {
                        message = new String(buffer, 0, size, "utf-8");
                        if (message != null && !message.equals("")) {
                            //유저가 우리 화면을 안보고 있다면?
                            //유저 topActivity 판단..
                            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                            List<ActivityManager.RunningTaskInfo> rti
                                    = am.getRunningTasks(1);
                            //하위 호환성 때문에 getRunningTasks
                            //가장 우선선위가 높은게 1개가 나온다. 우선순위 높은거는 화면 점유중인거

                            boolean isNotify = true;
                            if (rti != null && rti.size() > 0) {

                                Log.d("kkang", "1111");

                                ComponentName topActivity = rti.get(0).topActivity;
                                if (topActivity.getClassName().equals("com.example.ch6_socket.MainActivity")) {

                                    Log.d("kkang", "2222");
                                    isNotify = false;
                                }
                            }

                            if (isNotify) {
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(ChatService.this);


                                //noti 확장 터치시 발생할 intnent 준비
                                Intent intent = new Intent(ChatService.this, MainActivity.class);
                                intent.putExtra("msg", message);

                                PendingIntent pendingIntent
                                        = PendingIntent.getActivity(ChatService.this, 10, intent,
                                        PendingIntent.FLAG_UPDATE_CURRENT);

                                builder.setSmallIcon(R.drawable.notification_bar_icon);
                                builder.setWhen(System.currentTimeMillis());
                                builder.setContentTitle("New Message");
                                builder.setContentText(message);
                                builder.setContentIntent(pendingIntent);
                                builder.setAutoCancel(true);

                                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                manager.notify(111, builder.build());

                            } else {
                                //activity 에게 전달
                                Intent intent = new Intent("com.multi.ACTION_TO_ACTIVITY");
                                intent.putExtra("msg", message);
                                sendBroadcast(intent);
                            }
                        }
                    }else {
                        isRead = false;
                        isConnected = false;
                    }
                }catch (Exception e) {
                    isRead = false;
                    isConnected = false;
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
