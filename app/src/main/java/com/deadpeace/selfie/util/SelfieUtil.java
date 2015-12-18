package com.deadpeace.selfie.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.deadpeace.selfie.broadcast.BroadcastSelfie;
import com.deadpeace.selfie.model.User;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

/**
 * Created by Виталий on 17.11.2015.
 */
public class SelfieUtil
{
    static User currentUser;

    private SelfieUtil()
    {

    }

    public static synchronized void startAlarm(Context context,int hour,int minute)
    {
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.HOUR,hour);
        calendar.add(Calendar.MINUTE,minute);
        calendar.add(Calendar.SECOND,10);
        calendar.add(Calendar.MILLISECOND,0);
        Intent intentBoot=new Intent(context,BroadcastSelfie.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context,0,intentBoot,0);
        AlarmManager alarmManager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);
    }

    public static synchronized void cancelAlarm(Context context)
    {
        Intent intentBoot=new Intent(context,BroadcastSelfie.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context,0,intentBoot,0);
        AlarmManager alarmManager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public synchronized static User getCurrentUser()
    {
        return currentUser;
    }

    public synchronized static void setCurrentUser(User currentUser)
    {
        SelfieUtil.currentUser=currentUser;
    }

    public synchronized static Bitmap convertToBitmap(Response response)
    {
        return BitmapFactory.decodeStream(new ByteArrayInputStream(((TypedByteArray)response.getBody()).getBytes()));
    }

    public synchronized static File createImageFile() throws IOException
    {
        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName="selfie_"+timeStamp;
        File storageDir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image=File.createTempFile(imageFileName,".jpg",storageDir);
        return image;
    }
}
