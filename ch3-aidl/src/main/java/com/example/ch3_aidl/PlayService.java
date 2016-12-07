package com.example.ch3_aidl;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;

public class PlayService extends Service {

    MediaPlayer player; //원원, 영상 play
    //밴대로 소리, 영상 녹화는 Media\Redlcdr

    int status =  0;


    public PlayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        player = new MediaPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //외부 요청에 의해 객체 리턴..프로세스간 통신을 대행해주는
        //Stub  객체자 자동으로 만들어져서 리턴된다.
        //실 업무의 객체가 넘어가지 않는다.
        return new IPlayService.Stub(){
            @Override
            public int currentPosition() throws RemoteException {
                if(player.isPlaying()){
                    return player.getCurrentPosition();
                }else
                    return 0;
            }

            @Override
            public int getMaxDuration() throws RemoteException {
                if(player.isPlaying()){
                    return player.getDuration();
                }else
                    return 0;
            }

            @Override
            public int start() throws RemoteException {
                if(!player.isPlaying()){
                    player = MediaPlayer.create(PlayService.this, R.raw.music);
                    player.start();
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            status = CommonProperties.MEDIA_STATUS_COMPLETED;
                        }
                    });

                    status = CommonProperties.MEDIA_STATUS_RUNNING;
                }
                return 0;
            }

            @Override
            public int stop() throws RemoteException {

                if(player.isPlaying()){
                    player.stop();
                    status = CommonProperties.MEDIA_STATUS_STOP;
                }

                return 0;
            }

            @Override
            public int getMediaStatus() throws RemoteException {
                return status;
            }
        };
    }
}
