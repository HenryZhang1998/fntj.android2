package com.fntj.app;

import android.app.Application;
import android.util.Log;

import com.fntj.app.util.AppUtils;
import com.tencent.bugly.crashreport.CrashReport;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    public MyApplication() {
    }

    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "项目启动 >>>>>>>>>>>>>>>>>>>> \n\n");

        instance = this;

        //  bugly
        // appid f48a000a09
        // appkey fcee8c0a-cf8c-4457-aff1-b300b47b13af

        String versionName = AppUtils.getVersionName(this);

        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(this);
        strategy.setAppVersion(versionName);

        //第3个参数：调试模式
        CrashReport.initCrashReport(getApplicationContext(), "f48a000a09", true, strategy);
    }

    /**
     * 退出程序
     */
    public void exit(){
        System.exit(0);
    }


}
