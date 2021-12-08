package com.fntj.app.handler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.fntj.app.BuildConfig;
import com.fntj.app.MyApplication;
import com.fntj.app.dialog.NavingDialog;
import com.fntj.app.net.RobotApi;
import com.fntj.app.util.RecognizeWaitTimer;
import com.fntj.lib.zb.util.CommonUtil;
import com.fntj.lib.zb.util.JSON;
import com.fntj.lib.zb.util.StringUtil;
import com.iflytek.cloud.SpeechError;
import com.reeman.nerves.RobotActionProvider;
import com.reeman.reemanrobotdemo.aipower.ApRecognizeListener;
import com.reeman.reemanrobotdemo.aipower.ApResultProcessor;
import com.reeman.reemanrobotdemo.aipower.ApViewSpeakListener;
import com.reeman.reemanrobotdemo.baiduloca.BaiLocHelper;
import com.reeman.reemanrobotdemo.hardcontrol.ReemanSdkImpl;
import com.reeman.reemanrobotdemo.robotreceiver.RobotReceiver;
import com.rsc.impl.OnROSListener;
import com.rsc.impl.RscServiceConnectionImpl;
import com.rsc.reemanclient.ConnectServer;
import com.speech.abstracts.IRecognizeListener;
import com.speech.abstracts.IResultProcessor;
import com.speech.abstracts.ISpeakListener;
import com.speech.processor.SpeechPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RobotHandler implements SpeekHandler {

    public interface NavCompleteHandler {
        void onComplete(NavingDialog navingDialog, String dist);
    }

    public interface NavCancelHandler {
        void onCancel(NavingDialog navingDialog, String dist, boolean isTask);
    }

    public static final Map<String, String> Voices = new HashMap<String, String>() {{
        put("回前台", "前台");
        put("去前台", "前台");
        put("去充电", "充电站");
        put("去卫生间", "卫生间");
        put("上厕所", "卫生间");
    }};

    private static String wifiIP = null;
    private static final String locationsPath = "reeman/data/locations.cfg";
    public static Map<String, String> Locations = new HashMap<String, String>() {{
        put("前台", "-1.25,-4.44,-45.83");
        put("充电站", "0.46,-0.27,-20.05");
    }};

    private ConnectServer mConnectServer;
    private BDLocationListener myListener;
    private RobotReceiver robotReceiver;
    private ReemanSdkImpl hardSDk;

    private int argmode;

    private static RobotHandler instance;

    public static RobotHandler getInstance() {
        if (instance == null) {
            instance = new RobotHandler();
        }

        return instance;
    }

    public RobotHandler() {
    }

    public void init() {
        try {
            argmode = RobotActionProvider.getInstance().getArgMode();

            IntentFilter filter = new IntentFilter();
            filter.addAction("REEMAN_LAST_MOVTION");
            filter.addAction("REEMAN_BROADCAST_SCRAMSTATE");
            filter.addAction("REEMAN_BODY_POSITION");
            filter.addAction("REEMAN_BROADCAST_MICROWAVE_SENSOR_STATE");
            filter.addAction(BROADCAST_REEMAN_BROADCAST_WAKEUP);
            filter.addAction(ApViewSpeakListener.BROADCAST_speek_onBegin);
            filter.addAction(ApRecognizeListener.BROADCAST_recognize_onBegin);
            filter.addAction(ApRecognizeListener.BROADCAST_recognize_onEnd);
            filter.addAction(ApRecognizeListener.BROADCAST_recognize_onError);

            MyApplication.getInstance().registerReceiver(receiver, filter);

            System.out.println("Product Model: " + Build.DISPLAY + "," + Build.VERSION.SDK_INT + "," + Build.VERSION.RELEASE);
            System.out.println("sdk version: " + RobotActionProvider.getInstance().getSDKVersion());
            System.out.println("设备ID: " + RobotActionProvider.getInstance().getRobotID());

            try {
                SpeechPlugin.CreateInstance(MyApplication.getInstance()); // init ai
                SpeechPlugin.getInstance().setDevID(RobotActionProvider.getInstance().getRobotID()); // 设置机器ID
                //SpeechPlugin.getInstance().setDevID("20210305001"); // 设置机器ID
                SpeechPlugin.getInstance().setSpeakParams("xiaoyan", null, null, null);

                IRecognizeListener recListener = new ApRecognizeListener(MyApplication.getInstance()); // 1.识别状态回调
                IResultProcessor resListener = new ApResultProcessor(MyApplication.getInstance()); // 2.语义处理回调
                ISpeakListener viewSpeakListener = new ApViewSpeakListener(); // 3.合成回调

                SpeechPlugin.getInstance().setRecognizeListener(recListener); // 5.设置sdk内部识别回调
                SpeechPlugin.getInstance().setResultProcessor(resListener); // 6.设置sdk内部语义解析回调
                SpeechPlugin.getInstance().setViewSpeakListener(viewSpeakListener); // 7.设置sdk内部语音合成回调

                myListener = new MyLocationListener();
                //注册监听函数
                BaiLocHelper.getInstance().registerListener(myListener);

                // robot 广播
                robotReceiver = new RobotReceiver(MyApplication.getInstance());
                robotReceiver.register();

                // 硬件控制
                hardSDk = ReemanSdkImpl.CreateInstance(MyApplication.getInstance()); // 建议在这里初始化

                //读取本地坐标数据,获取配置文件中导航地点名称及坐标集合

                //SpeechPlugin 内部提供，主动获取机器 sdcard 的 location 中配置的导航地点名称及坐标集合。
                // 配置文件位置 sdcard/reeman/data/locations.cfg
                File navFile = new File(Environment.getExternalStorageDirectory(), locationsPath);
                File navFileDir = navFile.getParentFile();
                if (!navFileDir.exists()) {
                    navFileDir.mkdirs();
                }

                //for test
//                FileUtil.writeSDFile(
//                        "前台:-1.25,-4.44,-45.83;充电站:0.46,-0.27,-20.05;铂:3.64,2.62,-24.06;312:-0.93,-10.99,-108.86;311:-2.3,-14.67,-91.1;310:-2.97,-18.54,-84.79;309:-3.78,-24.28,71.61;308:-4.12,-26.17,-104.27;307:-5.37,-30.87,-173.03;306:-11.95,-31,-49.84;305:-11.97,-30.99,-169.02;304:-11.96,-30.98,163.86;303:-11.71,-28.75,96.25;302:-10.64,-24.83,85.94;301:-10.25,-19.34,-108.28;315:0.19,5.64,13.17;316:-3.28,6.9,-5.72;317:-3.29,6.91,148.39;318:-2.44,4.23,156.99;319:-2.46,4.23,138.65;321:-5.71,-11.08,170.74;322:-3.03,-16.72,-101.41;323:-3.87,-20.61,-99.69",
//                        locationsPath);

                if (navFile.exists()) {
                    Map<String, String> navList = SpeechPlugin.getInstance().getContactLocations();

                    for (Map.Entry<String, String> kv : navList.entrySet()) {
                        Locations.put(kv.getKey(), kv.getValue());
                    }
                }

            } catch (Exception ex2) {
                ex2.printStackTrace();
                Log.e("init", ex2.getMessage(), ex2);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("init", ex.getMessage(), ex);
        }

        try {
            // 连接外设
            mConnectServer = ConnectServer.getInstance(MyApplication.getInstance(), connection);
            mConnectServer.registerROSListener(new RosProcess());   //设置外设的监听回调(物体识别，人脸识别，导航等回调)

            //查询wifi ip
            RobotActionProvider.getInstance().sendRosCom("ip:request");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onResume() {
        Log.v("RobotHandler", "onResume");
        try {
            BaiLocHelper.getInstance().start();
            SpeechPlugin.getInstance().startRecognize();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onPause() {
        Log.v("RobotHandler", "onPause");
        try {
            BaiLocHelper.getInstance().stop();
            SpeechPlugin.getInstance().stopSpeak();
            SpeechPlugin.getInstance().stopRecognize();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onDestroy() {
        Log.v("RobotHandler", "onDestroy");
        if (receiver != null) {
            try {
                MyApplication.getInstance().unregisterReceiver(receiver);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            robotReceiver.unregister();
            //反注册监听函数
            BaiLocHelper.getInstance().unregisterListener(myListener);
            SpeechPlugin.getInstance().onDestroy();
            if (hardSDk != null) {
                hardSDk.reLease(); // 建议在这里释放
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            if (mConnectServer != null) {
                mConnectServer.release();
                mConnectServer = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void stopSpeek() {
        try {
            if (SpeechPlugin.getInstance().isSpeaking()) {
                SpeechPlugin.getInstance().stopSpeak();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("stopSpeek", ex.getMessage(), ex);
        }

    }

    public void doSpeek(String speek) {

        try {
            Log.i("speek", speek);
            SpeechPlugin.getInstance().startSpeak(speek);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("speek", ex.getMessage(), ex);
        }
    }

    public void doSpeek(String speek, ISpeakListener listener) {
        try {
            Log.i("speek", speek);
            SpeechPlugin.getInstance().startSpeak(speek, listener);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("speek", ex.getMessage(), ex);
        }
    }


    /**
     * 开始导航
     *
     * @param loc 坐标
     */
    public void doNav(String loc) {
        try {
            RobotActionProvider.getInstance().sendRosCom(String.format("goal:nav[%s]", loc));
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("nav", ex.getMessage(), ex);
        }
    }

    /**
     * 取消民航
     */
    public void cancelNav() {
        try {
            RobotActionProvider.getInstance().sendRosCom("cancel_goal");
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("nav", ex.getMessage(), ex);
        }
    }

    private boolean charging = false;

    public boolean isCharging() {
        return charging;
    }

    public void setCharging(boolean charging) {
        this.charging = charging;
    }

    private boolean goingCharge = false;

    public boolean isGoingCharge() {
        return goingCharge;
    }

    /**
     * 去充电
     */
    public void goCharge() {
        goingCharge = true;

        //goal:charge[x,y,yaw]
        try {
            String dist = getNavLocation("充电站");
            if (dist == null) {
                CommonUtil.showShortToast(MyApplication.getInstance(), "没找到充电站在哪");
                return;
            }
            RobotActionProvider.getInstance().sendRosCom(String.format("goal:charge[%s]", dist));
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("nav", ex.getMessage(), ex);
        }
    }

    /**
     * 回前台
     */
    public void goHome() {
        try {
            String dist = getNavLocation("前台");
            if (dist == null) {
                CommonUtil.showShortToast(MyApplication.getInstance(), "没找到前台在哪");
                doSpeek("很抱歉，小曼没找到前台在哪");
                return;
            }
            doNav(dist);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 去卫生间
     */
//    public void goWC() {
//        try {
//            String dist = getNavLocation("卫生间");
//            if (dist == null) {
//                CommonUtil.showShortToast(context, "没找到卫生间在哪");
//                doSpeek("很抱歉，小曼没找到卫生间在哪");
//                return;
//            }
//            doNav(dist);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
    public String getNavLocation(String action) {

        for (Map.Entry<String, String> kv : Locations.entrySet()) {
            if (action.contains(kv.getKey())) {
                return kv.getValue();
            }
        }

        return null;
    }

    public String doNavAction(String dist) {
        try {
            String loc = getNavLocation(dist);
            if (loc == null) {
                return null;
            }

            if (dist.contains("充电")) {
                goCharge();
            } else {
                doNav(loc);
            }

            return loc;
        } catch (Exception ex) {
            ex.printStackTrace();

            doSpeek("很抱歉，小曼出现未知异常");

            CommonUtil.showShortToast(MyApplication.getInstance(), "很抱歉，小曼出现未知异常：" + ex.getMessage());

            return null;
        }
    }

    //TODO 替换为自己申请的讯飞id
    public static final String IFLY_ID = "586b954d";
    //TODO 替换为锐曼提供的id
    public static final String REEMAN_ID = "qhaCKzWd6AOM0LBP"; // 锐曼后台id，详见readme.txt

    public static String curLocation = "福州"; // 默认

    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (!TextUtils.isEmpty(location.getCity())) {
                curLocation = location.getCity();
            }
            Log.i("MainActivity", "city: " + location.getCity());
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
            Log.i("MainActivity", "connect hot spot message: " + s);
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.v("receiver", "action = " + action);

            if ("REEMAN_LAST_MOVTION".equals(action)) {
                //底座运动、头部动作、手臂等动作完成广播

                int type = intent.getIntExtra("REEMAN_MOVTION_TYPE", 0);
                Log.v("demo", "type：" + type);
            } else if ("REEMAN_BROADCAST_SCRAMSTATE".equals(action)) {
                //急停按钮状态上报广播

                int value = intent.getIntExtra("SCRAM_STATE", -1);
                Log.v("receiver", "value = " + value);

            } else if ("REEMAN_BODY_POSITION".equals(action)) {
                //头部位置以及手臂位置广播

                int cmd = intent.getIntExtra("CMD", -1);
                int mode = intent.getIntExtra("MODE", -1);
                String data = intent.getStringExtra("DATA");

                Log.v("receiver", "cmd: " + cmd + " ,mode: " + mode + " ,data: " + data);

            } else if ("REEMAN_BROADCAST_MICROWAVE_SENSOR_STATE".equals(action)) {
                //人体红外探测

                int state = intent.getIntExtra("SENSOR_STATE", -1);
                //0 没有人；1 有人

                Log.v("receiver", "SENSOR_STATE: " + state);
            } else if (ApViewSpeakListener.BROADCAST_speek_onBegin.equals(action)) {
                //机器人开始说话时，取消监听
                SpeechPlugin.getInstance().stopRecognize();
            } else if (ApRecognizeListener.BROADCAST_recognize_onBegin.equals(action)) {
                //开始识别语音
            } else if (ApRecognizeListener.BROADCAST_recognize_onEnd.equals(action)) {
                //识别语音结束，停止监听识音
            } else if (ApRecognizeListener.BROADCAST_recognize_onError.equals(action)) {
                //识别语音错误，停止监听识音
            } else if (BROADCAST_REEMAN_BROADCAST_WAKEUP.equals(action)) {
                //唤醒
                RobotHandler.getInstance().doSpeek("你好，请说");
            }
        }
    };

    private RscServiceConnectionImpl connection = new RscServiceConnectionImpl() {
        public void onServiceConnected(int name) {
            if (mConnectServer == null)
                return;
//            if (name == ConnectServer.Connect_D3) {
//                // 3D摄像头回调
//                //                mConnectServer.register3DSensorListener(sensorListener);
//            } else if (name == ConnectServer.Connect_Pr_Id) {
//                // 身份证识别回调
//                //mConnectServer.registerIDListener(idListener);
//            }
        }

        public void onServiceDisconnected(int name) {
            System.out.println("onServiceDisconnected......");
        }
    };

    // -------------------------------------------------------------------------

    /**
     * 外设监听回调(物体识别，人脸识别，导航状态回调，充电信息回调...)
     */

    public class RosProcess extends OnROSListener {
        private final String TAG = RosProcess.class.getSimpleName();

        @Override
        public void onResult(String result) {
            Log.e(TAG, "----OnROSListener.onResult()---result:" + result);
            if (result != null) {

                Intent intent = new Intent();

                if (result.startsWith("od:")) {
                    //Log.e(TAG, "收到物体识别回调：  " + result);
                    intent.setAction(BROADCAST_od);

                } else if (result.startsWith("pt:[")) {
                    //人脸识别结果回调
                    intent.setAction(BROADCAST_pt);
                    intent.putExtra("result", result);

                    MyApplication.getInstance().sendBroadcast(intent);
                } else if (result.startsWith("move_status:")) {

                    System.out.println("move_status-->handler-->" + result);

                    Log.e(TAG, "收到导航信息回调：  " + result);
                    String code = result.split("\\:")[1].trim();

                    Log.e(TAG, "收到导航信息回调：  code:" + code);

                    // code
                    // 0 : 静止待命  
                    // 1 : 上次目标失败，等待新的导航命令  
                    // 2 : 上次目标完成，等待新的导航命令  
                    // 3 : 移动中，正在前往目的地  
                    // 4 : 前方障碍物  
                    // 5 : 目的地被遮挡
                    // 6 ：用户取消导航
                    // 7 ：收到新的导航

                    intent.putExtra("code", Integer.valueOf(code));
                    intent.setAction(BROADCAST_move_status);

                    MyApplication.getInstance().sendBroadcast(intent);

                } else if (result.equals("bat:reached")) {
                    //Log.e(TAG, "收到充电信息回调：  " + result);

                    goingCharge = false;

                    intent.setAction(BROADCAST_bat_reached);
                    MyApplication.getInstance().sendBroadcast(intent);

                } else if (result.equals("sys:uwb:0")) {
                    goingCharge = false;
                    //Log.e(TAG, "收到导航信息回调：  uwb错误：:" + result);
                    intent.setAction(BROADCAST_uwb);
                    MyApplication.getInstance().sendBroadcast(intent);
                } else if (result.startsWith("ip:")) {
                    Log.i("ip", result);
                    //ip:ssid:x.x.x.x

                    String[] arr = result.split(":");
                    if (arr.length < 3) {
                        return;
                    }

                    wifiIP = arr[2];
                    Log.i("wifiIP", wifiIP);

                    //获取局域网机器目标点列表
                    String url = "http://" + wifiIP + "/reeman/android_target";

                    RobotApi.simpleGet(MyApplication.getInstance(), url, new RobotApi.MyResponseListener<String>() {
                        @Override
                        public void onError(String message, String code) {
                            Log.e(code, message);
                        }

                        @Override
                        public void onSuccess(String data) {
                            Log.i("android_target", data);
                            //{"A点":["-3.66","-0.06","-110.96"],"B点":["-4.27","-1.36","0.00"],"C点":["-3.47","-2.27","73.41"],"D点":["-2.54","-1.29","180.00"]}

                            JSONObject j = JSON.parseObject(data);
                            for (String key : j.keySet()) {
                                List<String> points = j.getJSONArray(key).toJavaList(String.class);

                                Locations.put(key, StringUtil.join(points, ","));
                            }
                        }
                    });
                }
            }
        }
    }

    public static final Map<Integer, MoveStatusBroadcastReceiver> mslist = new HashMap<>();

    public MoveStatusBroadcastReceiver registMoveStatus(Object instance, MoveStatusListener listener) {

        synchronized (mslist) {
            int hc = instance.hashCode();
            if (mslist.containsKey(hc)) {

                try {
                    MoveStatusBroadcastReceiver myBroadcastReceiver = mslist.get(hc);
                    MyApplication.getInstance().unregisterReceiver(myBroadcastReceiver);
                } catch (Exception ex) {

                }

                mslist.remove(hc);
            }

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BROADCAST_move_status);

            MoveStatusBroadcastReceiver myBroadcastReceiver = new MoveStatusBroadcastReceiver(listener);
            MyApplication.getInstance().registerReceiver(myBroadcastReceiver, intentFilter);

            mslist.put(hc, myBroadcastReceiver);

            return myBroadcastReceiver;
        }
    }

    public void unregistMoveStatus(Object instance) {
        synchronized (mslist) {
            int hc = instance.hashCode();
            if (mslist.containsKey(hc)) {
                try {
                    MoveStatusBroadcastReceiver myBroadcastReceiver = mslist.get(hc);
                    MyApplication.getInstance().unregisterReceiver(myBroadcastReceiver);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                mslist.remove(hc);
            }
        }
    }

    public static final Map<Integer, FaceBroadcastReceiver> facelist = new HashMap<>();

    public FaceBroadcastReceiver registFace(Object object, FaceListener listener) {

        synchronized (facelist) {
            int hc = object.hashCode();
            if (facelist.containsKey(hc)) {

                FaceBroadcastReceiver myBroadcastReceiver = facelist.get(hc);

                MyApplication.getInstance().unregisterReceiver(myBroadcastReceiver);

                facelist.remove(hc);
            }

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BROADCAST_pt);

            FaceBroadcastReceiver myBroadcastReceiver = new FaceBroadcastReceiver(listener);
            MyApplication.getInstance().registerReceiver(myBroadcastReceiver, intentFilter);

            facelist.put(hc, myBroadcastReceiver);

            return myBroadcastReceiver;
        }
    }

    public void unregistFace(Object object) {
        synchronized (facelist) {
            int hc = object.hashCode();
            if (facelist.containsKey(hc)) {
                FaceBroadcastReceiver myBroadcastReceiver = facelist.get(hc);
                MyApplication.getInstance().unregisterReceiver(myBroadcastReceiver);
                facelist.remove(hc);
            }
        }
    }

    public static final Map<Integer, VoiceBroadcastReceiver> voicelist = new HashMap<>();

    public VoiceBroadcastReceiver registVoice(Object object, VoiceListener listener) {

        synchronized (voicelist) {
            int hc = object.hashCode();
            if (voicelist.containsKey(hc)) {

                VoiceBroadcastReceiver myBroadcastReceiver = voicelist.get(hc);

                MyApplication.getInstance().unregisterReceiver(myBroadcastReceiver);

                voicelist.remove(hc);
            }

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BROADCAST_voice);

            VoiceBroadcastReceiver myBroadcastReceiver = new VoiceBroadcastReceiver(listener);
            MyApplication.getInstance().registerReceiver(myBroadcastReceiver, intentFilter);

            voicelist.put(hc, myBroadcastReceiver);

            return myBroadcastReceiver;
        }
    }

    public void unregistVoice(Object object) {
        synchronized (voicelist) {
            int hc = object.hashCode();
            if (voicelist.containsKey(hc)) {
                VoiceBroadcastReceiver myBroadcastReceiver = voicelist.get(hc);
                MyApplication.getInstance().unregisterReceiver(myBroadcastReceiver);
                voicelist.remove(hc);
            }
        }
    }

    public interface MoveStatusListener {
        void onReceive(int code);
    }

    public interface FaceListener {
        void onReceive(String result);
    }

    public interface VoiceListener {
        void onReceive(String voice);
    }

    public class VoiceBroadcastReceiver extends BroadcastReceiver {

        private VoiceListener listener;

        public VoiceBroadcastReceiver(VoiceListener listener) {
            this.listener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            String voice = intent.getStringExtra("voice");

            listener.onReceive(voice);
        }
    }

    public class MoveStatusBroadcastReceiver extends BroadcastReceiver {

        private MoveStatusListener listener;

        public MoveStatusBroadcastReceiver(MoveStatusListener listener) {
            this.listener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            int code = intent.getIntExtra("code", 0);

            listener.onReceive(code);
        }
    }

    public class FaceBroadcastReceiver extends BroadcastReceiver {

        private FaceListener listener;

        public FaceBroadcastReceiver(FaceListener listener) {
            this.listener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            String result = intent.getStringExtra("result");

            listener.onReceive(result);
        }
    }

    public static final Map<Integer, PowerBroadcastReceiver> powerlist = new HashMap<>();

    public PowerBroadcastReceiver registPower(Object _class, PowerListener listener) {

        int hc = _class.hashCode();
        if (powerlist.containsKey(hc)) {
            MyApplication.getInstance().unregisterReceiver(powerlist.get(hc));
            powerlist.remove(hc);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);//
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);    //电量变化

        filter.addAction(BROADCAST_bat_reached);//到达充电区域
        filter.addAction(BROADCAST_POWER_CONNECTE);    //电源连接广播(适配器充电，充电桩充电)"ACTION_POWER_CONNECTE_REEMAN"

        filter.addAction(BROADCAST_DOCKNOTFOUND);    //没有找到充电桩 "AUTOCHARGE_ERROR_DOCKNOTFOUND"
        filter.addAction(BROADCAST_DOCKINGFAILURE); //连接充电桩失败 "AUTOCHARGE_ERROR_DOCKINGFAILURE"

        PowerBroadcastReceiver myBroadcastReceiver = new PowerBroadcastReceiver(listener);
        MyApplication.getInstance().registerReceiver(myBroadcastReceiver, filter);

        powerlist.put(hc, myBroadcastReceiver);
        return myBroadcastReceiver;
    }

    public void unregistPower(Object _class) {

        int hc = _class.hashCode();
        if (powerlist.containsKey(hc)) {
            MyApplication.getInstance().unregisterReceiver(powerlist.get(hc));
            powerlist.remove(hc);
        }
    }

    public interface PowerListener {
        void onReceive(String action, Intent intent);
    }

    public class PowerBroadcastReceiver extends BroadcastReceiver {
        private PowerListener listener;

        public PowerBroadcastReceiver(PowerListener listener) {
            this.listener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (Intent.ACTION_POWER_CONNECTED.equalsIgnoreCase(action)) {
                setCharging(true);
                goingCharge = false;
            }

            if (Intent.ACTION_POWER_DISCONNECTED.equalsIgnoreCase(action)) {
                setCharging(false);
                goingCharge = false;
            }

            listener.onReceive(action, intent);
        }
    }


    public static final String BROADCAST_od = "BROADCAST_od";
    public static final String BROADCAST_pt = "BROADCAST_pt";
    public static final String BROADCAST_move_status = "BROADCAST_move_status";
    public static final String BROADCAST_bat_reached = "BROADCAST_bat_reached";
    public static final String BROADCAST_uwb = "BROADCAST_sys:uwb:0";
    public static final String BROADCAST_voice = "BROADCAST_voice";

    public static final String BROADCAST_POWER_CONNECTE = "ACTION_POWER_CONNECTE_REEMAN";
    public static final String BROADCAST_DOCKNOTFOUND = "AUTOCHARGE_ERROR_DOCKNOTFOUND";
    public static final String BROADCAST_DOCKINGFAILURE = "AUTOCHARGE_ERROR_DOCKINGFAILURE";
    public static final String BROADCAST_REEMAN_BROADCAST_WAKEUP = "REEMAN_BROADCAST_WAKEUP";


}
