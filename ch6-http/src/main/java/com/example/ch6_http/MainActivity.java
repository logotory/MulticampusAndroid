package com.example.ch6_http;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    String datas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.newsTextView);

        HttpRequester requester = new HttpRequester();
        HttpCallback callback = new HttpCallback() {
            @Override
            public void onResult(String result) {
                SpannableUtil util = new SpannableUtil(MainActivity.this);
                util.setTextView(textView, result);
            }
        };
        requester.request("http://70.12.108.90:8080/news.jsp", null , callback);
    }
}
