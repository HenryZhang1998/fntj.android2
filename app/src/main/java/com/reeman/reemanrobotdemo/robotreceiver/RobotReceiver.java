package com.reeman.reemanrobotdemo.robotreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.reeman.nerves.RobotActionProvider;
import com.speech.processor.SpeechPlugin;

public class RobotReceiver extends BroadcastReceiver {

    private Context rContext;

    //private ApViewController mViewControl;

    public RobotReceiver(Context c) {
        this.rContext = c;
    }

    public void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("REEMAN_BROADCAST_WAKEUP");        // 唤醒
        filter.addAction("REEMAN_BROADCAST_SCRAMSTATE");    // 急停开关
        rContext.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("REEMAN_BROADCAST_WAKEUP".equals(action)) {
            // 唤醒
            int angle = intent.getIntExtra("REEMAN_6MIC_WAY", 0);
//            if (mViewControl != null) {
//                mViewControl.setWake("唤醒角度: " + angle);
//            }
        } else if ("REEMAN_BROADCAST_SCRAMSTATE".equals(action)) {
            // 急停开关
            int stopState = intent.getIntExtra("SCRAM_STATE", -1);
            if (stopState == 0) {
                // 取消导航
                SpeechPlugin.getInstance().startSpeak("急停开关被按下");
                RobotActionProvider.getInstance().sendRosCom("bat:uncharge");
                RobotActionProvider.getInstance().sendRosCom("cancel_goal");
            }
        }

    }

    public void unregister() {
        rContext.unregisterReceiver(this);
    }

//    public void setViewControl(ApViewController avc) {
//        this.mViewControl = avc;
//    }
}
