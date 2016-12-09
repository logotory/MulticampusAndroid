package com.example.ch6_http;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GetImageService extends IntentService {

    public GetImageService() {
        super("GetImageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SystemClock.sleep(5000); //테스트..
        //network정보는 activity가 전달해 줄거다..
        String url = intent.getStringExtra("url");
        HashMap<String, String> params = (HashMap<String, String>) intent.getParcelableExtra("param");
        ResultReceiver callback =  intent.getParcelableExtra("callback");

        Bitmap response = null;
        String postData = "";
        PrintWriter pw = null;

        try {
            URL text = new URL(url);
            HttpURLConnection http = (HttpURLConnection)text.openConnection();
            http.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=UTF-8");
            http.setConnectTimeout(10000);
            http.setReadTimeout(10000);
            http.setRequestMethod("POST");
            http.setDoInput(true);
            http.setDoOutput(true);

            if(params != null && params .size() > 0){
                //서버에  넘길 데이터가 이싿면 웹의 query 문잔열론 =롬나
                //Write
                Iterator<Map.Entry<String, String>> entries = params.entrySet().iterator();
                while (entries.hasNext()){
                    Map.Entry<String, String> entry = (Map.Entry<String, String>) entries.next();
                    postData = postData+"&"+entry.getKey()+"="+entry.getValue();
                }

                pw = new PrintWriter(new OutputStreamWriter(http.getOutputStream(), "UTF-8"));
                pw.write(postData);
                pw.flush();
            }

            response = BitmapFactory.decodeStream(http.getInputStream());
            Log.d("kkang", "image download ok~");

            //activity에게 데이터를 넘겨야 한다..
            if(callback != null){
                Bundle bundle = new Bundle();
                bundle.putParcelable("result", response);
                callback.send(0, bundle);
            }

        }catch (Exception e ) {
            e.printStackTrace();
        } finally {
            try {
                if(pw != null) pw.close();
            }catch (Exception e) {}
        }
    }
}
