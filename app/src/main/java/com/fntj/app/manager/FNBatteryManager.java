package com.fntj.app.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import com.fntj.app.MyApplication;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.BATTERY_SERVICE;

public class FNBatteryManager extends BroadcastReceiver {

    private static FNBatteryManager fnBatteryManager;

    public static FNBatteryManager getInstance() {
        if (fnBatteryManager == null) {
            fnBatteryManager = new FNBatteryManager(MyApplication.getInstance());
        }
        return fnBatteryManager;
    }

    private Context context;
    private Intent batteryStatus;
    private int percent = 100;

    private Map<Context, BatteryChangeListener> listeners = new HashMap<>();

    public FNBatteryManager(Context context) {
        this.context = context;
    }

    public void init() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //Intent batteryStatus = context.registerReceiver(null, ifilter);

        batteryStatus = MyApplication.getInstance().registerReceiver(fnBatteryManager, filter);

    }

    public void distory() {
        MyApplication.getInstance().unregisterReceiver(this);
    }

    public void register(Context context, BatteryChangeListener listener) {
        if (listeners.containsKey(context)) {
            listeners.remove(context);
        }
    }

    public void unregist(Context context) {
        if (listeners.containsKey(context)) {
            listeners.remove(context);
        }
    }

    public int getBattery() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

//            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//            capacity = Math.round( level / (float)scale );

            return percent;

        } else {
            BatteryManager batteryManager = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            int capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY); //百分比电量
            return capacity;
        }

//        int counter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER); //总容量，单位:mah
//        int ave = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);// 剩余电量，mah
//        int cur = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);//剩余电量,mah
//        int energyCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER);//容量，单位:nw(纳瓦)

    }

    public boolean isCharging() {
        return false;
    }

    public interface BatteryChangeListener {
        void onReceive(int total, int current, int percent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        int current = intent.getExtras().getInt("level");//获得当前电量
        int total = intent.getExtras().getInt("scale");//获得总电量

        percent = current * 100 / total;

        if (listeners != null) {
            for (BatteryChangeListener listener : listeners.values()) {
                listener.onReceive(total, current, percent);
            }
        }
    }
}
