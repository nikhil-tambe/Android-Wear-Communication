package com.nikhil.phone.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by Nikhil on 19/7/17.
 */

public class ApplicationClass extends Application {

    private static ApplicationClass instance;

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }

    public static synchronized ApplicationClass getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // MultiDex.install(this);
    }

}
