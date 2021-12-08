package com.reeman.reemanrobotdemo.aipower;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fntj.app.MyApplication;
import com.fntj.lib.zb.util.JSON;
import com.iflytek.cloud.SpeechError;
import com.speech.abstracts.IRecognizeListener;

import static com.fntj.app.handler.RobotHandler.BROADCAST_voice;

/**
 * Created by ZJcan on 2017-04-22.
 */

public class ApRecognizeListener implements IRecognizeListener {

    private static final String TAG = "ApRecognizeListener";
    private Context context;
    public ApRecognizeListener(Context context){
        this.context = context;
    }
    @Override
    public void onBeginOfSpeech() {
        Intent intent = new Intent();
        intent.setAction("robot.recognize.onBegin");
        MyApplication.getInstance().sendBroadcast(intent);
    }

    @Override
    public void onError(SpeechError speechError) {

        if (speechError != null) {
            Log.e(TAG, JSON.toJSONString(speechError));

            int code = speechError.getErrorCode();
            String msg = speechError.getErrorDescription();

            Log.e(TAG, "code:" + code + ", msg:" + msg);

            Intent intent = new Intent();
            intent.setAction("robot.recognize.onError");
            intent.putExtra("code", code);
            intent.putExtra("msg", msg);

            MyApplication.getInstance().sendBroadcast(intent);
        }
    }

    @Override
    public void onEndOfSpeech() {

        Intent intent = new Intent();
        intent.setAction("robot.recognize.onEnd");

        MyApplication.getInstance().sendBroadcast(intent);
    }

    @Override
    public void onResult(String voice) {

        Log.i(TAG, voice);

        Intent intent = new Intent();
        intent.setAction(BROADCAST_voice);
        intent.putExtra("voice", voice);

        context.sendBroadcast(intent);
    }

    @Override
    public void onVolumeChanged(int i, byte[] bytes) {
//        if (mViewControl != null) {
//            mViewControl.setVol("vol: " + i);
//        }
    }
}
