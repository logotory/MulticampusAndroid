package com.example.ch6_socket;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	ArrayList<ChatMessage> list;
	MyAdapter ap;

	ListView lv;
	Button sendBtn;
	EditText msgEdit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		lv = (ListView) findViewById(R.id.list);
		sendBtn = (Button) findViewById(R.id.send_btn);
		msgEdit = (EditText) findViewById(R.id.send_text);

		sendBtn.setOnClickListener(this);

		list = new ArrayList<ChatMessage>();
		ap = new MyAdapter(this, R.layout.list_item, list);
		lv.setAdapter(ap);

		lv.setDividerHeight(0);
		

		//lab1-----------------------------
		//우리의 경우는 boot complete시에 service 구동하게 했다.
		//시스템에 의해 유저에 의해 Service는 죽는다.
		//우리의 Service가 살아 있는지 판단

		boolean isMyService = false;
		ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(100);

		ActivityManager.RunningServiceInfo rsi = null;
		for(int i=0; i < rs.size(); i++){
			rsi = rs.get(i);
			if(rsi.service.getClassName().equals("com.example.ch6_socket.ChatService")) {
				isMyService = true;
				break;
			}
		}

		if(!isMyService) {
			Intent intent = new Intent(this, ChatService.class);
			startService(intent);
		}


		//lab1 end-------------------------
	}

	private void addMessage(String who, String msg) {
		ChatMessage vo = new ChatMessage();
		vo.who = who;
		vo.msg = msg;
		list.add(vo);
		ap.notifyDataSetChanged();
		lv.setSelection(list.size() - 1);
	}

	@Override
	protected void onResume() {
		super.onResume();

		registerReceiver(chatReceiver, new IntentFilter("com.multi.ACTION_TO_ACTIVITY"));

		//우리의 경우는 Notification에 의해서 실행되었을 수도 있다..
		//자신을 실행시킨 intent 획득
		Intent intent = getIntent();
		String msg = intent.getStringExtra("msg");

		if(msg != null && !msg.equals("")){
			addMessage("you", msg);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(chatReceiver);
	}

	//lab2------------------------------------------

	//service로부터 넘어오는 데이터 받을 준비..
	BroadcastReceiver chatReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("kkang", "activity receiver...");
			if(intent.getStringExtra("msg") != null){
				addMessage("you", intent.getStringExtra("msg"));
			}

		}
	};


//lab2 end------------------------------------
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (!msgEdit.getText().toString().trim().equals("")) {

			Intent bcIntent = new Intent("com.multi.ACTION_TO_SERVICE");
			bcIntent.putExtra("msg", msgEdit.getText().toString());
			sendBroadcast(bcIntent);

			addMessage("me", msgEdit.getText().toString());
			msgEdit.setText("");
		}
	}
}

class ChatMessage {
	String who;
	String msg;
}

class MyAdapter extends ArrayAdapter<ChatMessage> {
	ArrayList<ChatMessage> list;
	int resId;
	Context context;

	public MyAdapter(Context context, int resId, ArrayList<ChatMessage> list) {
		// TODO Auto-generated constructor stub
		super(context, resId, list);
		this.context = context;
		this.resId = resId;
		this.list = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
//		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(resId, null);
//		}

		TextView msgView = (TextView) convertView.findViewById(R.id.msg);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) msgView
				.getLayoutParams();

		ChatMessage msg = list.get(position);
		if (msg.who.equals("me")) {
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
					RelativeLayout.TRUE);
			msgView.setBackgroundColor(Color.YELLOW);
		} else if (msg.who.equals("you")) {
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
					RelativeLayout.TRUE);
			msgView.setBackgroundColor(Color.WHITE);
		}
		msgView.setText(msg.msg);

		return convertView;

	}
}
