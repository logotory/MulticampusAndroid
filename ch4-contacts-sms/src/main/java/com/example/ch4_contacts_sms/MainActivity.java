package com.example.ch4_contacts_sms;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button sendButton;
    Button voiceButton;
    Button contactButton;
    EditText phoneEdit;
    EditText contentEdit;

    boolean contactPermission;
    boolean smsPermission;
    boolean phonePermission;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = (Button) findViewById(R.id.button_send);
        voiceButton = (Button) findViewById(R.id.button_voice);
        contactButton = (Button) findViewById(R.id.button_contacts);

        phoneEdit = (EditText) findViewById(R.id.edit_phone);
        contentEdit = (EditText) findViewById(R.id.edit_content);

        sendButton.setOnClickListener(this);
        voiceButton.setOnClickListener(this);
        contactButton.setOnClickListener(this);

        //add1~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //permission - uses-permisson개념은 api level1부터 있었던 개념이지만..
        //5.x 버전까지는 개발자가 manifst에 users-permission사용한다고 선언하면 끝..
        //6.x 부터는 아무리 개발자가 manifest에 달았다고 하더라도 유저가 취소 시킬 수 있다.
        //기본 app install시는 모두다 disable이다.

        //permission이 enable된건지 확인하는 코드..
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {

            contactPermission = true;
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {

            smsPermission = true;
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            phonePermission = true;
        }

        if(!contactPermission && !smsPermission && !phonePermission) {
            //permission enable을 유저에게 요청해야하다.
            //코드에서 직접 enable, disable 조정은 물가하다..
            //=>dialog 띄워서 app 내에서 직접 부여할 수 있게 ..
            //시스템 dialog 이다.
            requestPermissions();
        }

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    }

    private void requestPermissions() {
        //dialog 화면 3번 변경된다..
        //dialog를 띄웠다고 하더라고 유저가 dialog 화면에서 Cancel했을 수 있도록. 사후추ㅈ거..
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_PHONE_STATE}, 200);
    }

    //requestPermissions에 의한 dialog가 닫기는 순간 자동 호출..
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 200 && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                contactPermission = true;
            }

            if(grantResults[1] == PackageManager.PERMISSION_GRANTED){
                smsPermission = true;
            }

            if(grantResults[2] == PackageManager.PERMISSION_GRANTED){
                phonePermission = true;
            }
        }
    }

    public void onClick(View v) {
		//add2~~~~~~~~~~~~~~~~~~~~~~~~~

        if(v==contactButton){
            if(contactPermission){
                //주소록 목록화면 intent로
                Intent intent = new Intent(Intent.ACTION_PICK,
                        Uri.parse("content://com.android.contacts/data/phones"));

                startActivityForResult(intent, 10);
            }else{
                requestPermissions();
            }
        }else if(v== voiceButton) {
            //음성인식 app을 intent로 실행..
            //외부 app의 activity를 intent로 실행시킬 때 intent에
            //반응할 ativity가 없을수도 있다면?? ==> 에러..
            //먼저 intent 반응 activity있는지 확인하고..

            //PackageManager : app이 설치된 static 정보 제공..
            // ==> 폰에 설치된 app 목록..app package 획득..
            // ==> intent 반응 Component 정보

            //ActivityManager: app이 구동 상태에 있는 정보..(동작중인 다이나믹 정보)
            // ==> 시스템의 Process 목록..
            // ==> 구동중인 Service 목록..(실행상태인 서비스 목록)
            // ==> 지금 유저 top 화면을 점유한 activity 정보..
            PackageManager pm = getPackageManager();

            List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
            if(activities.size() > 0) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                //free-form, web-search
                //원하면 local을 지정할 수  있음
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "ReconizedTest..."); //dialog title

                startActivityForResult(intent, 20);

            }else {
                Toast t = Toast.makeText(this, "no speech app", Toast.LENGTH_LONG);
                t.show();
            }
        }else if(v==sendButton){
            if(smsPermission && phonePermission){
                //유저 폰 번호 축출
                TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                String myNumber = manager.getLine1Number();

                //sent ack 반응할 intent..
                Intent intent = new Intent("SENT_SMS_ACTION");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        getApplicationContext(), 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                //sms 발송
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(
                        phoneEdit.getText().toString(),
                        myNumber,
                        contentEdit.getText().toString(),
                        pendingIntent,
                        null);

            }else{
                requestPermissions();
            }
        }

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    }

    //startActivityFoerResult에 의한 요청이 되돌아올때 자동 호출..
    //requestCode: intent를 발생시킨 곳에서 intent 를 구분하기 위해서 준 개발자 임의의 숫자값
    //resultCode: intent에 의해 실행된 곳에서 결과를 되돌리기 전에 어떻게 처리해서 되돌린건지(잘됬는자 안됬는지)를 표현..
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 10 && resultCode==RESULT_OK) {
            //주소록 모곩에서 되돌아 왔을때 전화번호가 넘어오지 않는다..
            //홍길동의 식별자 값만.. url문자열로..url 마지막 단어가 식별자
            String id = Uri.parse(data.getDataString()).getLastPathSegment();
            //id값을 조건으로 구체적으로 원하는 데이터 요청..
            Cursor cursor = getContentResolver().query(

                    //content prvider 식별자 uri
                    ContactsContract.Data.CONTENT_URI,
                    //select column 조건 .. null 이면 모든 데이터
                    new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER},
                    //select where 조건
                    ContactsContract.Data._ID + "=" +id,
                    //data args ..?에 들어갈 데이터
                    null,
                    //order by 뒤에 들어갈 단어..
                    null);

            //row 선택..
            cursor.moveToFirst();
            phoneEdit.setText(cursor.getString(0));
        } else if(requestCode == 20 && resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            contentEdit.setText(results.get(0));
        }
    }

    BroadcastReceiver sentReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context _context, Intent _intent) {
                    String msg="";
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            // 전송 성공 처리; break;
                            msg="sms 전송 성공";
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            // 일반적인 실패 처리; break;
                            msg="sms 전송 실패";
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            // 무선 꺼짐 처리; break;
                            msg="무선 꺼짐";
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            // PDU 실패 처리; break;
                            msg="pdu 오류";
                            break;
                    }
                    Toast t = Toast.makeText(MainActivity.this, msg,
                            Toast.LENGTH_SHORT);
                    t.show();
                }
            };

    protected void onResume() {
        super.onResume();
        registerReceiver(sentReceiver, new IntentFilter("SENT_SMS_ACTION"));
    };
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        unregisterReceiver(sentReceiver);
    }

}