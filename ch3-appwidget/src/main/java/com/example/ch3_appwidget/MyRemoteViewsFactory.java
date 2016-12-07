package com.example.ch3_appwidget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

/**
 * Created by student on 2016-12-07.
 */
//ListView를 완성하기 우한 adapter이다.
    //우리쪽에서 이요하는것이 아니라 launcher app에서 이용..
public class MyRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{

    Context context;
    ArrayList<ItemVO> arrayList;

    public MyRemoteViewsFactory(Context context, Intent intent) {
        this.context = context;
    }

    //항목 하나의 데이터를 추상화 시키기 위한 개발자 I/O 클래스
    class ItemVO {
        int _id;
        String content;
    }

    //항목 구성 데이터 획득 dbms
    private void selectDB() {
        arrayList = new ArrayList<>();
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select _id, content from tb_data", null);
        while (cursor.moveToNext()){
            ItemVO vo = new ItemVO();
            vo._id = cursor.getInt(0);
            vo.content = cursor.getString(1);
            arrayList.add(vo);
        }
        db.close();
    }


    @Override
    public void onCreate() {
        selectDB();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    //일반 Adapter의 getView()에 해당하는 함수.. 항목 하나하나의 구성
    @Override
    public RemoteViews getViewAt(int i) {
        //의뢰서.. 항목선택 이벤트시 각 항목의 데이터가 포함 되어야 한다.
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.row);
        remoteViews.setTextViewText(R.id.text1, arrayList.get(i).content);

        Intent intent = new Intent();
        intent.putExtra("item_id", arrayList.get(i)._id);
        intent.putExtra("itemn_data", arrayList.get(i).content);
        remoteViews.setOnClickFillInIntent(R.id.text1, intent);

        return remoteViews;
    }



    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    //receiver에서 데이터 변경시 notifiAppWidgetViewDataChanged호출
    //아래아ㅣ 함수 자동 호출
    @Override
    public void onDataSetChanged() {
        selectDB();;
    }
}
