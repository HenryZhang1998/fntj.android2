package com.reeman.reemanrobotdemo.hardcontrol;


import android.app.Application;
import android.content.Context;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.reeman.nerves.RobotActionProvider;
import com.rsc.impl.OnROSListener;
import com.rsc.impl.RscServiceConnectionImpl;
import com.rsc.reemanclient.ConnectServer;
import com.synjones.idcard.IDCardInfo;
import com.synjones.idcard.OnIDListener;


/**
 * Created by jincan on 2016/6/27.
 */
public class ReemanSdkImpl {

    private final static String TAG = "ReemanSdkImpl";
    private static ReemanSdkImpl sdk;
    private ConnectServer cs;
    private ReemanSdkHandler mHandler;
    private Context mContext;

    private ReemanSdkImpl(Application application) {
        mContext = application;
        mHandler = new ReemanSdkHandler();
        cs = ConnectServer.getInstance(application, impl);
        registerRos();
    }

    /**
     * 创建SDK实例
     *
     * @param application
     */
    public static ReemanSdkImpl CreateInstance(Application application) {
        if (sdk == null) {
            sdk = new ReemanSdkImpl(application);
        }
        return sdk;
    }

    /**
     * 获取SDK实例
     *
     * @return
     */
    public static ReemanSdkImpl getInstance() {
        if (sdk != null) {
            return sdk;
        } else {
            Log.e(TAG, "sdk init null");
            return null;
        }
    }

//    public void setViewControl(ApViewController viewControl) {
//        if (mHandler != null) {
//            mHandler.setViewControl(viewControl);
//        }
//    }

    /**
     * 询问ros的ip
     *
     * @param
     */
    public void getROSIp() {
        RobotActionProvider.getInstance().sendRosCom("ip:request");
    }

    private RscServiceConnectionImpl impl = new RscServiceConnectionImpl() {

        @Override
        public void onServiceConnected(int name) {
            super.onServiceConnected(name);
            Log.e(TAG, "onServiceConnected: " + name);
            if (name == ConnectServer.Connect_Pr_Id) {
                cs.registerIDListener(Ilistener);
            }
        }

        @Override
        public void onServiceDisconnected(int name) {
            super.onServiceDisconnected(name);
        }
    };

    public void registerRos() {
        Log.d("ReemanSDK", "----registerRos()---" + (cs == null));
        if (cs == null)
            return;
        cs.registerROSListener(mRosListener);
    }


    /**
     * 身份证接口
     */
    private OnIDListener Ilistener = new OnIDListener.Stub() {

        @Override
        public void onResult(IDCardInfo info, byte[] photo)
                throws RemoteException {
            Log.e("ID",
                    "name: " + info.getName() + ",nation: " + info.getNation()
                            + ",birthday: " + info.getBirthday());
        }
    };

    /**
     * ROS接口
     */
    private OnROSListener mRosListener = new OnROSListener() {
        @Override
        public void onResult(String result) {
            Log.d("ReemanSDK", "----OnROSListener.onResult()---result:" + result);
            if (result != null) {
                if (result.startsWith("pt:[")) {
                    sendMsg(1, result);
                } else if (result.equals("wifi:rec")) {
                    sendMsg(2, result);
                } else if (result.startsWith("ip:")) {
                    sendMsg(3, result);
                } else if (result.startsWith("move_status:")) {
                    Log.w("roslistener", "收到导航信息回调：  " + result);
                } else if (result.equals("bat:reached")) {
                    Log.w("roslistener", "收到充电信息回调：  " + result);
                    sendMsg(4, result);
                } else if (result.equals("sys:uwb:0")) {
                    Log.w("roslistener", "收到导航信息回调：  uwb错误：:" + result);
                    sendMsg(5, result);
                } else if (result.startsWith("loc[")) {
                    Log.w("roslistener", "收到位置信息回调：  " + result);
                    sendMsg(6, result);
                }
            }

        }
    };

    /**
     * 释放
     */
    public void reLease() {
        Log.v("ReemanSdkImpl", "release......");
        if (cs != null)
            cs.release();
        if (sdk != null)
            sdk = null;
    }

    private void sendMsg(int what, String msg) {
        if (mHandler != null) {
            Message message = mHandler.obtainMessage();
            message.what = what;
            message.obj = msg;
            mHandler.sendMessage(message);
        }
    }

}
