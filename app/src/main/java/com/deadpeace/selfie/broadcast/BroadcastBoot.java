package com.deadpeace.selfie.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.deadpeace.selfie.util.Contract;
import com.deadpeace.selfie.util.SelfieUtil;

/**
 * Created by Виталий on 17.11.2015.
 */
//TODO Broadcast receiver for start Alarm manager after boot device
public class BroadcastBoot extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context,Intent intent)
    {
        SharedPreferences preferences=context.getSharedPreferences(Contract.TIME_REMINDER,Context.MODE_PRIVATE);
        if(preferences.getBoolean(Contract.DO_REMIND,false))
            SelfieUtil.startAlarm(context,preferences.getInt(Contract.HOUR,12),preferences.getInt(Contract.MINUTE,0));
        Log.i("BROADCAST-BOOT","Alarm manager was started");
    }
}
