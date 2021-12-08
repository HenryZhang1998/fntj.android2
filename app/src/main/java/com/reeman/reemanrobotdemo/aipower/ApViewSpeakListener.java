package com.reeman.reemanrobotdemo.aipower;

import android.content.Intent;
import android.os.Handler;

import com.fntj.app.MyApplication;
import com.fntj.lib.zb.util.CommonUtil;
import com.fntj.lib.zb.util.JSON;
import com.fntj.lib.zb.util.Log;
import com.iflytek.cloud.SpeechError;
import com.speech.abstracts.ISpeakListener;

/**
 * 语音播放
 * <p>
 * Created by ZJcan on 2017-04-24.
 */

public class ApViewSpeakListener implements ISpeakListener {

    public static final String BROADCAST_speek_onBegin = "robot.speek.onBegin";
    private static final String TAG = "ApViewSpeakListener";
    private static final Handler handler = new Handler();

    @Override
    public void onSpeakOver(SpeechError speechError) {

        if (speechError != null) {
            Log.e(TAG, JSON.toJSONString(speechError));

            int code = speechError.getErrorCode();
            String msg = speechError.getErrorDescription();

            Log.e(TAG, "speak error: code:" + code + ", msg:" + msg);

            Intent intent = new Intent();
            intent.setAction("robot.speek.onError");
            intent.putExtra("code", code);
            intent.putExtra("msg", msg);

            MyApplication.getInstance().sendBroadcast(intent);

            handler.post(() -> {
                CommonUtil.showShortToast(MyApplication.getInstance(), "speak error: code:" + code + ", msg:" + msg);
            });
        }
    }

    @Override
    public void onInterrupted() {

    }

    @Override
    public void onSpeakBegin(String text) {

        Intent intent = new Intent();
        intent.setAction(BROADCAST_speek_onBegin);
        intent.putExtra("text", text);

        MyApplication.getInstance().sendBroadcast(intent);
    }
}
