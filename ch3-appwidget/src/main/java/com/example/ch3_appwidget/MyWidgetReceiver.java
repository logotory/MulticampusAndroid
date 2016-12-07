package com.example.ch3_appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidgetReceiver extends AppWidgetProvider {

    public MyWidgetReceiver() {
    }

    //우리의 appwidget이 정상적으로 실행되려면... DataService가 구동되어야 한다.
    //언제?
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        Intent intent = new Intent(context, DataService.class);
        context.startService(intent);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Intent intent = new Intent(context, DataService.class);
        context.stopService(intent);
    }

    //하나의 appwiget이 실행 될때 마다 콜되는
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        //의뢰서 준비
        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(), //내가 누구다.. 소속을 밝히는..
                R.layout.widget_layout //어느 ui를 건드리는...
        );

        //1. Factory를 전달받기 위해서 실행시키는 Service 구동시킬 intentn 정보..
        Intent sIntent = new Intent(context, MyRemoteViewsService.class);
        remoteViews.setRemoteAdapter(R.id.list, sIntent);

        //2. 항목선택 이벤특 발생 했을때 발생시키는 intent의뢰
        Intent aIntent = new Intent(context, DetailActivity.class);
        PendingIntent pendingIntent  = PendingIntent.getActivity(context, 10, aIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //항목 선택시에 Detail Activity는 실행이 되지만
        //항목별 데이터는 어떻게 추가하는가???
        //여기서 모한다.. 항목 구성은 adapter(factory묶음으로)에서
        //이 intent에 내용을 추가할 거다..
        remoteViews.setPendingIntentTemplate(R.id.list, pendingIntent);

        //의뢰..
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);


    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Log.d("kkang", intent.getAction());

        if(intent.getAction().equals("com.multi.ACTION_DATA")) {
            //DataService에서 데이터가 발생이 되었다 알려준거다..
            //launcher app에게 데이터 구성 다시 해야한다고 의로 한다.
            //Factory(Adapter)이용 다시 화면을 잡는다.

            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int ids []  = manager.getAppWidgetIds(new ComponentName(context, MyWidgetReceiver.class));

            //Factory 다시 작업한다
            manager.notifyAppWidgetViewDataChanged(ids, R.id.list);
        }
    }
}
