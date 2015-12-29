package com.example.wifiapp.application;

import android.app.Application;
import android.content.Context;

/**
 * Created by fg607 on 15-12-24.
 */
public class MyApplication extends Application {

    private static Context mContext;
    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static Context getContext(){

        return mInstance.getApplicationContext();
    }

    public MyApplication getInstance(){

        return mInstance;
    }
}
