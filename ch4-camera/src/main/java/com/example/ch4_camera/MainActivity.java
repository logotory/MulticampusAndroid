package com.example.ch4_camera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
              super.onCreate(savedInstanceState);

        final MyView myView = new MyView(this);
        myView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                myView.capture();
            }
        });
        setContentView(myView);
    }
}
