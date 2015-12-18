package com.deadpeace.selfie.util;

import com.deadpeace.selfie.unsafe.EasyHttpClient;

import retrofit.RestAdapter;
import retrofit.client.ApacheClient;

/**
 * Created by Виталий on 11.10.2015.
 */
public class SelfieConnection
{
    //TODO Change IP
    private static final String SERVER="https://192.168.1.103:8443";

    private static SelfieSvcApi ourInstance=new RestAdapter.Builder()
            .setClient(new ApacheClient(new EasyHttpClient()))
            .setEndpoint(SERVER)
            .setLogLevel(RestAdapter.LogLevel.FULL).build()
            .create(SelfieSvcApi.class);

    public synchronized static SelfieSvcApi getInstance()
    {
        return ourInstance;
    }

    private SelfieConnection()
    {
    }
}
