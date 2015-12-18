package com.deadpeace.selfie.loader;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by Виталий on 13.10.2015.
 */
//TODO This class extends Thread for Selfie App
public abstract class LoaderSelfie<T> extends Thread implements Runnable,Task<T>
{
    private Handler mHandler=new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case TASK_COMPLETED:
                    success((T)msg.obj);
                    break;
                case TASK_FAIL:
                    fail((Exception)msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    public LoaderSelfie()
    {
        setDaemon(true);
    }

    @Override
    public void run()
    {
        Message message=mHandler.obtainMessage();
        try
        {
            T t=doInBackground();
            message=mHandler.obtainMessage(TASK_COMPLETED,t);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            message=mHandler.obtainMessage(TASK_FAIL,e);
        }
        finally
        {
            mHandler.sendMessage(message);
        }
    }
}
