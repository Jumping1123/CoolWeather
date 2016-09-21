package com.example.jumping.coolweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.jumping.coolweather.service.AutoUpdateService;

/**
 * Created by Jumping on 2016/9/20.
 */
public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent intent_for_service = new Intent(context, AutoUpdateService.class);
        context.startService(intent_for_service);

    }
}
