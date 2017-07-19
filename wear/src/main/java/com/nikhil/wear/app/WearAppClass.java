package com.nikhil.wear.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by Nikhil on 19/7/17.
 */

public class WearAppClass extends Application {

    private static WearAppClass instance;

    public static Context getAppContext(){
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
