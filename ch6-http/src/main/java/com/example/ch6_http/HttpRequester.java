package com.example.ch6_http;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by student on 2016-12-09.
 * 네트웍 대행하는 라이브러기 코드..
 */

public class HttpRequester {

    HttpTask http;

    //activity쪽에서 네트워크 필요시 호출
    //ulr
    //param: 서버 요청시 넘겨야할 web의 query 문
    //callback: 결과를 받을 interface구현 핵체
    public  void request(String url, HashMap<String, String> param, HttpCallback callback) {

        http = new HttpTask(url, param, callback);
        http.execute();

    }

    //activiey에서의 네트웡은 ANR문제를 꼭 고려해서 작성해야.

    //그 코드까지도 추상화 시키겠다.
    //ARR문의 해결 기본은 Threa-Handler로 작성하는 거지만
    //Thread-Hander 추상화 AsyncTask 작성
    private class HttpTask extends AsyncTask<Void, Void, String>  {

        String url;
        HashMap<String, String> params;
        HttpCallback callback;

        public HttpTask(String url, HashMap<String, String> params, HttpCallback callback) {
            this.url = url;
            this.params = params;
            this.callback = callback;
        }


        //AsyncTask 객체의 execute()을 호출하면
        //내부적으로 Thread 구동되고 Thread의 run 함수에서 아래의 함수 호출
        //backgroud에서 동작해야할 업무로직..네트워킹..
        @Override
        protected String doInBackground(Void... voids) {

            String response  = "";
            String postData = "";
            PrintWriter pw = null;
            BufferedReader in = null;

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

                in = new BufferedReader(new InputStreamReader(http.getInputStream(), "euc-kr"));

                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = in.readLine()) != null){
                    sb.append(line);
                }

                response = sb.toString();
                Log.d("kkang", "text data read..." + response.length());

            }catch (Exception e ) {
                e.printStackTrace();
            } finally {
                 try {
                     if(pw != null) pw.close();
                     if(in != null) in.close();
                 }catch (Exception e) {}
            }
            return response;
        }

        //doInBackgroud 함수에서 결과를 리턴하면 내북적으로 Handler동작시켜
        //아래 함수를 호출해 준다.. 리턴한 값이 매게 변수로..
        @Override
        protected void onPostExecute(String s) {
            callback.onResult(s);
        }
    }
}
