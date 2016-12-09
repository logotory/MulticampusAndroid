package com.example.ch6_http;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by student on 2016-12-09.
 * 문자열 데이터를 다양한 UI로 화면에 찍는다 util 개발자 클래스..
 */

public class SpannableUtil {
    Context context;
    TextView textView;
    SpannableStringBuilder stringBuilder; //문자열 데이터에 spannable(문자열데이터의 ui정보)정보까지 포함된 결과 데이터..

    public SpannableUtil(Context context) {
        this.context = context;
    }

    //ForegroudColorSpan 같은 경우는 이 span이 두여되서 문자열 일부분이
    //그 색으로 나오면 끝이지만.. URLSpan의 경우는 링크 모양으로 나온다가
    //끝이 아니라.. 클릭시 이벤트는???
    //링크 모양이 적용된 span을 우리의 이벤트 포함 span으로 대체..
    private void makeLink(final URLSpan span) {
        //이전 span 적용된 위치 획득..
        int start = stringBuilder.getSpanStart(span);
        int end  = stringBuilder.getSpanEnd(span);
        int flag = stringBuilder.getSpanFlags(span);

        //우리의 span 준비..

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                //<a>태그의 href문자열 획득..
                Toast t = Toast.makeText(context, span.getURL(), Toast.LENGTH_LONG);
                t.show();
            }
        };

        stringBuilder.setSpan(clickableSpan, start, end, flag);
        stringBuilder.removeSpan(span);
    }

    //초기에는 리소스 이미지 적용 span.. 이미지 다운로드 시도후 ..
    //실 이미지로 대체..
    private void downloadImage(final ImageSpan span) {
        final int start = stringBuilder.getSpanStart(span);
        final int end = stringBuilder.getSpanEnd(span);
        final int flag = stringBuilder.getSpanFlags(span);

        Log.d("kkang", "image downloaded.."+span.getSource());

        //서비스쪽 결과를 받기 위한 call by reference 형식의 객체 준비
        ImageResultReceiver callback = new ImageResultReceiver(new Handler());
        callback.setReceiver(new ImageResultReceiver.ImageResultCallback(){
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                //이미지 다운로드 완료
                Bitmap bitmap = resultData.getParcelable("result");

                //실 이미지 span 만들어서..
                Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
                drawable.setBounds(0,0,drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                ImageSpan imageSpan = new ImageSpan(drawable);
                stringBuilder.setSpan(imageSpan, start, end, flag);
                stringBuilder.removeSpan(span);
                textView.setText(stringBuilder);
            }
        });

        //service 구동
        Intent intent = new Intent(context, GetImageService.class);
        intent.putExtra("url", "http://70.12.108.90:8080/images/"+span.getSource()+".jpg");
        intent.putExtra("callback", callback);
        context.startService(intent);
    }

    //activity가 서버로부터 문자열 획득후 호출하여 다양한 ui 효과 적용..
    public void setTextView(TextView  textView, String data) {
        this.textView = textView;
        //문자열 UI효과 적용
        CharSequence sequence = Html.fromHtml(data, new MyImageGetter(), null);

        //전체 spannable 적용된 결과 획득..
        stringBuilder = new SpannableStringBuilder(sequence);
        //URLSpan과 ImageSpan 만 획득
        URLSpan [] urls = stringBuilder.getSpans(0, sequence.length(), URLSpan.class);
        ImageSpan [] imgs = stringBuilder.getSpans(0, sequence.length(), ImageSpan.class);

        for(URLSpan span : urls) {
            makeLink(span);
        }

        for(ImageSpan span : imgs) {
            downloadImage(span);
        }

        textView.setText(stringBuilder);
        textView.setTextSize(16);
        textView.setMovementMethod(new ScrollingMovementMethod()); //문자열이 길어지면 스크롤바 달아주세요
        textView.setLinksClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    class MyImageGetter implements  Html.ImageGetter {

        @Override
        public Drawable getDrawable(String s) {

            Drawable drawable =  context.getResources().getDrawable(R.drawable.loading);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            return drawable;
        }
    }
}