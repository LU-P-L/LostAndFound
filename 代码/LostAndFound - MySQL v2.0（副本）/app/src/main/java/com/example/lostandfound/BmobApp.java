package com.example.lostandfound;

import android.app.Application;
import cn.bmob.v3.Bmob;

public class BmobApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Bmob.initialize(this, "1eb189c2c0fd83bd44c9992769cdb1b2");
    }
}
