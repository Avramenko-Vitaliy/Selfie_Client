package com.deadpeace.selfie.loader;

/**
 * Created by Виталий on 25.10.2015.
 */
public interface Task<T>
{
    int TASK_FAIL=-1;
    int TASK_COMPLETED=1;

    void success(T t);

    void fail(Exception e);

    T doInBackground()throws InterruptedException;
}
