package com.reeman.reemanrobotdemo.hardcontrol;

import android.os.Handler;
import android.os.Message;

/**
 * Created by ZJcan on 2017-09-30.
 */

public class ReemanSdkHandler extends Handler {
//    private ApViewController mViewControl;
//
//    public void setViewControl(ApViewController viewControl) {
//        this.mViewControl = viewControl;
//    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                break;
            case 2:
                break;
            case 3:
//                if (mViewControl != null) {
//                    mViewControl.setRosip(msg.obj.toString());
//                }
                break;
            default:
                break;
        }
    }
}
