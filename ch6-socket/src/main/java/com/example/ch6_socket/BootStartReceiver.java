package com.example.ch6_socket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootStartReceiver extends BroadcastReceiver {

    public BootStartReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent sIntent = new Intent(context, ChatService.class);
        context.startService(sIntent);
    }
}
