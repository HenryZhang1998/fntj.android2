package com.reeman.reemanrobotdemo.aipower;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.reeman.nerves.RobotActionProvider;
import com.reeman.reemanrobotdemo.nlp.NLPResult;
import com.speech.abstracts.IResultProcessor;
import com.speech.bean.ReemanResult;
import com.speech.processor.SpeechPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

/**
 * Created by ZJcan on 2017-04-22.
 */

public class ApResultProcessor implements IResultProcessor {

    private Context mContext;

    public ApResultProcessor(Context context) {
        mContext = context;
    }

    @Override
    public void onPartialResult(ReemanResult result) {
        if (result == null)
            return;

        Log.i("RobotDemo", "onPartialResult: "+result.getType()+"   "+result.getJson());
        String json = result.getJson();
        if (json == null) {
            Random random = new Random();
            String[] otherArr = {"主人，臣妾听不懂", "主人，臣妾办不到", "臣妾做不到", "臣妾没听懂，请换种说法吧！"};
            String res = otherArr[random.nextInt(otherArr.length)];
            SpeechPlugin.getInstance().startSpeak(res);
            return;
        }

        int type = result.getType();
        switch (type) {
            case 1:
                // reeman
                handleReemanJson(result);
                break;
            case 2:
                // ifly
                handleIflyJson(result);
                break;
            default:
                break;
        }
    }

    private final Random random = new Random();
    private final String[] otherArr = {"主人，小曼听不懂", "主人，小曼办不到", "小曼做不到", "小曼没听懂，请换种说法吧！"};

    private void handleIflyJson(ReemanResult result) {
        ReemanResult ifly = result;
        String json = ifly.getJson();

        if (TextUtils.isEmpty(json)) {
            String res = otherArr[random.nextInt(otherArr.length)];
            SpeechPlugin.getInstance().startSpeak(res);
        } else {
            NLPResult mResult = new NLPResult(mContext, json);
            String iflyAnswer = mResult.getmAnswer();
            String rawText = mResult.getmRawtext();
            String focus = mResult.getmFocus();
            if (TextUtils.isEmpty(iflyAnswer)) {
                String res = otherArr[random.nextInt(otherArr.length)];
                SpeechPlugin.getInstance().startSpeak(res);
            } else {
//                if (NLPResult.RAWTEXT_WEATHER_LOCAL.equals(iflyAnswer)) {
//                    SpeechPlugin.getInstance().onIflyTextUnderstand(DemoApplication.curLocation + rawText);
//                    return;
//                }
                SpeechPlugin.getInstance().startSpeak(iflyAnswer);
            }
        }
    }

    private void handleReemanJson(ReemanResult result) {
        try {
            ReemanResult reeman = result;
            if (reeman == null) return;

            String json = reeman.getJson();
            if (json == null || json.length() < 1) return;

            JSONObject root = new JSONObject(json);
            String answer = root.optString("Data");
            if (answer == null || answer.length() < 1) return;

            Log.v("ReemanJson", "answer: " + answer);

            if (answer.contains("charge_")) {

                int stopState = RobotActionProvider.getInstance().getScramState();
                if (stopState == 0) {
                    SpeechPlugin.getInstance().startSpeak("急停开关被按下，无法去充电");
                    return;
                }

                SpeechPlugin.getInstance().startSpeak("好的");
                String list = SpeechPlugin.getInstance().getContactLocations().get("充电站");
                if (list == null) {
                    SpeechPlugin.getInstance().startSpeak("不好意思，我还不知道充电站在哪呢");
                    return;
                }

                final String goal = coordinate(list, 0);
                if (goal == null || "".equals(goal)) {
                    SpeechPlugin.getInstance().startSpeak("未能匹配到正确坐标");
                    return;
                }

                RobotActionProvider.getInstance().sendRosCom(goal);

            } else if ("cancel_charge".equals(answer)) {
                cancelCharge();
            } else if ("cancel_navigation".equals(answer)) {
                cancelNavigation();
            } else if (answer.contains("navigation_")) {
                int stopState = RobotActionProvider.getInstance().getScramState();
                if (stopState == 0) {
                    SpeechPlugin.getInstance().startSpeak("急停开关被按下，无法进行导航");
                    return;
                }

                if (SpeechPlugin.getInstance().getContactLocations() == null) {
                    SpeechPlugin.getInstance().startSpeak("我还没设置导航地址呢");
                    return;
                }

                String guidePlace = parse(answer);
                List<String> navtList = SpeechPlugin.getInstance().getNavList();

                for (String nav : navtList) {
                    if (guidePlace.contains(nav)) {

                        String list = SpeechPlugin.getInstance().getContactLocations().get(nav);
                        Log.d("NavigationManager", "---navigateByPoint---list:" + list);

                        if (list == null) {
                            SpeechPlugin.getInstance().startSpeak("不好意思，我还不知道" + nav + "在哪呢");
                            return;
                        }

                        final String goal = coordinate(list, 1);
                        if (goal == null || "".equals(goal)) {
                            SpeechPlugin.getInstance().startSpeak("未能匹配到正确坐标");
                            return;
                        }

                        SpeechPlugin.getInstance().startSpeak("好的，这就带你去找" + nav);
                        Log.v("11111","goal: "+goal);
                        RobotActionProvider.getInstance().sendRosCom(goal);
                        return;
                    }
                }

            } else {
                SpeechPlugin.getInstance().startSpeak(answer);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String parse(String result) {
        if (result.contains("charge_") || result.contains("navigation_")) {
            return result.substring(result.indexOf("_") + 1, result.length());
        }
        return "";
    }

    public static void cancelNavigation() {
        SpeechPlugin.getInstance().startSpeak("好的");
        RobotActionProvider.getInstance().sendRosCom("cancel_goal");
    }

    public static void cancelCharge() {
        SpeechPlugin.getInstance().startSpeak("好的");
        RobotActionProvider.getInstance().sendRosCom("cancel_goal");
        RobotActionProvider.getInstance().sendRosCom("bat:uncharge");
    }

    public static String coordinate(String site, int type) {
        StringBuffer buffer = new StringBuffer();
        if (site == null)
            return "";
        String[] point = site.split(",");
        float x = Float.valueOf(point[0]);
        float y = Float.valueOf(point[1]);
        float yaw = Float.valueOf(point[2]);
        if (type == 0) {
            buffer = new StringBuffer("goal:charge");
        } else if (type == 1) {
            buffer = new StringBuffer("goal:nav");
        }
        buffer.append("[").append(x).append(",").append(y).append(",").append(yaw).append("]");
        return buffer.toString();
    }
}
