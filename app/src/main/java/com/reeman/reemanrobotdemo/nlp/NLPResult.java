package com.reeman.reemanrobotdemo.nlp;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONObject;

/**
 * Created by anonymous on 2016/6/22.
 */
public class NLPResult {

    private Context mContext;

    /**
     * 解析结果
     */
    private String mRawtext = ""; // 转写结果
    private String mAnswer = ""; // 闲聊回答
    private String mFocus = ""; // 语义焦点

    /**
     * 天气查询，无指定城市
     */
    public static final String RAWTEXT_WEATHER_LOCAL = "localweather";

    public NLPResult(Context context, String json) {
        this.mContext = context;
        parseResult(json);
    }

    private void parseResult(String json) {
        try {
            if (TextUtils.isEmpty(json)) {
                throw new Exception();
            }
            JSONObject root = new JSONObject(json);
            parseRawtext(root);

            int rc = root.optInt("rc");
            if (rc != 0) {
                return;
            }

            parseFocus(root);

            if (mFocus == null) {
                throw new Exception();
            }

            parseAnswer(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseRawtext(JSONObject root) {
        try {
            mRawtext = root.optString("text");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseFocus(JSONObject root) {
        try {
            mFocus = root.optString("service");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void parseAnswer(JSONObject root) {
        try {
            JSONObject root2 = root.optJSONObject("answer");
            mAnswer = root2.optString("text");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getmRawtext() {
        return mRawtext;
    }

    public String getmAnswer() {
        return mAnswer;
    }

    public void setmRawtext(String mRawtext) {
        this.mRawtext = mRawtext;
    }

    public void setmAnswer(String mAnswer) {
        this.mAnswer = mAnswer;
    }

    public String getmFocus() {
        return mFocus;
    }

    public void setmFocus(String mFocus) {
        this.mFocus = mFocus;
    }
}
