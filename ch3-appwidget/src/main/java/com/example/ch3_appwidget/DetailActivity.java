package com.example.ch3_appwidget;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        int id = getIntent().getIntExtra("item_id",0);
        String content=getIntent().getStringExtra("item_data");

        Log.d("kkang","DetailActivity:"+id+","+content);
        TextView tv=(TextView)findViewById(R.id.detail_text);

        if (id != 0 && content != null) {
            tv.setText(id+":"+content);
        }
    }
}
