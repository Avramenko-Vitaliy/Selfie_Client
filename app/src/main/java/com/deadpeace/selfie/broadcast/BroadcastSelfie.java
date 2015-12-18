package com.deadpeace.selfie.broadcast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;

import com.deadpeace.selfie.R;
import com.deadpeace.selfie.activity.SelfieActivity;
import com.deadpeace.selfie.util.Contract;

/**
 * Created by Виталий on 14.11.2015.
 */
//TODO Notification remind take selfie
public class BroadcastSelfie extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context,Intent intent)
    {
        Notification.Builder builder=new Notification.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setTicker(context.getResources().getText(R.string.take_selfie))
                .setSmallIcon(android.R.drawable.ic_menu_crop)
                .setContentTitle(context.getResources().getText(R.string.app_name))
                .setContentText(context.getResources().getText(R.string.take_selfie))
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context,0,new Intent(context,SelfieActivity.class),PendingIntent.FLAG_UPDATE_CURRENT));
        NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(Contract.ID_NOTIFY_TAKE_SELFIE,builder.build());
    }
}
