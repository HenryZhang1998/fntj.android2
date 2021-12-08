package com.fntj.app.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.airbnb.lottie.LottieAnimationView;
import com.fntj.app.MyApplication;
import com.fntj.app.R;
import com.fntj.app.handler.RobotHandler;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.speech.setting.IatSettings;
import com.iflytek.speech.util.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class SearchDialogFragment extends BaseDialogFragment implements View.OnClickListener {

    private Context context;

    private static String TAG = SearchDialogFragment.class.getSimpleName();

    // 语音听写对象
    private SpeechRecognizer mIat;

    // 语音听写UI
    private RecognizerDialog mIatDialog;

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    //private EditText mResultText;

    private Toast mToast;

    private SharedPreferences mSharedPreferences;

    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private String[] languageEntries;
    private String[] languageValues;
    private String language = "zh_cn";

    private int selectedNum = 0;

    private String resultType = "json";

    private boolean cyclic = false;//音频流识别是否循环调用

    private StringBuffer buffer = new StringBuffer();

    private RobotHandler robotHandler;

    Handler han = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x001) {
                //executeStream();
            }
        }
    };

    private static int flg = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();

        languageEntries = getResources().getStringArray(R.array.iat_language_entries);
        languageValues = getResources().getStringArray(R.array.iat_language_value);

        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(context, mInitListener);

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(context, mInitListener);

        mSharedPreferences = getActivity().getSharedPreferences(IatSettings.PREFER_NAME, Activity.MODE_PRIVATE);

        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);

        robotHandler = RobotHandler.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 初始化Layout。
     */
    private void initLayout(View container) {

        container.findViewById(R.id.iat_recognize).setOnClickListener(SearchDialogFragment.this);

        try {
            Window win = getDialog().getWindow();

            // 一定要设置Background，如果不设置，window属性设置无效
            win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

            int ww = dm.widthPixels;
            int dw = (int) (dm.widthPixels * 0.5);
            int x = (ww - dw) / 2;

            ViewGroup.LayoutParams params2 = container.getLayoutParams();
            params2.width = dw;
            params2.height = LinearLayout.LayoutParams.WRAP_CONTENT;

            container.setLayoutParams(params2);
            container.setX(x);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    int ret = 0; // 函数调用返回值

    @Override
    public void onClick(View view) {

        if (null == mIat) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            this.showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            return;
        }

        switch (view.getId()) {

            // 开始听写
            // 如何判断一次听写结束：OnResult isLast=true 或者 onError
            case R.id.iat_recognize:
                // 移动数据分析，收集开始听写事件
                //	FlowerCollector.onEvent(IatActivity.this, "iat_recognize");
                hideInput(view);

                robotHandler.stopSpeek();

                buffer.setLength(0);
                tbKeyword.setText("");// 清空显示内容
                mIatResults.clear();
                // 设置参数
                setParam();

                boolean isShowDialog = mSharedPreferences.getBoolean(getString(R.string.pref_key_iat_show), true);
                if (isShowDialog) {
                    // 显示听写对话框
                    mIatDialog.setListener(mRecognizerDialogListener);
                    mIatDialog.show();
                    showTip(getString(R.string.ify_text_begin));
                } else {
                    // 不显示听写对话框
                    ret = mIat.startListening(mRecognizerListener);
                    if (ret != ErrorCode.SUCCESS) {
                        showTip("听写失败,错误码：" + ret + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
                    } else {
                        showTip(getString(R.string.ify_text_begin));
                    }
                }
                break;

            // 停止听写
            case R.id.iat_stop:
                mIat.stopListening();
                showTip("停止听写");

                robotHandler.stopSpeek();
                break;

            // 取消听写
            case R.id.iat_cancel:
                mIat.cancel();
                showTip("取消听写");
                robotHandler.stopSpeek();
                break;

            default:
                break;
        }
    }

    private void hideInput(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };


    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。

            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());

            System.out.println(flg++);

            if (resultType.equals("json")) {

                printResult(results);

            } else if (resultType.equals("plain")) {

                buffer.append(results.getResultString());

                String text = buffer.toString();

                System.out.println("IatActivity--->text--->" + text);

                Log.i(TAG, text);

                tbKeyword.setText(text.replace("。", ""));

                tbKeyword.setSelection(tbKeyword.length());
            }

            if (isLast & cyclic) {
                // TODO 最后的结果
                Message message = Message.obtain();
                message.what = 0x001;
                han.sendMessageDelayed(message, 100);
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private void printResult(RecognizerResult results) {

        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        String _text = resultBuffer.toString();

        System.out.println("IatActivity--->text--->" + text);

        Log.i(TAG, _text);

        tbKeyword.setText(_text.replace("。", ""));

        tbKeyword.setSelection(tbKeyword.length());
    }


    //听写UI监听器
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
        }

        //识别回调错误
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }
    };

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);


        if (language.equals("zh_cn")) {
            String lag = mSharedPreferences.getString("iat_language_preference",
                    "mandarin");
            Log.e(TAG, "language:" + language);// 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        } else {

            mIat.setParameter(SpeechConstant.LANGUAGE, language);
        }
        Log.e(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));

        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != mIat) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * title view
     */
    private TextView mTitle;

    /**
     * 内容文本
     */
    private EditText tbKeyword;

    /**
     * 底部button layout
     */
    private LinearLayout mButtons;

    /**
     * 左边的text button
     */
    private Button mLeft;

    /**
     * 右边的text button
     */
    private LottieAnimationView mRight;

    /**
     * 设置layout params
     */
    private WindowManager.LayoutParams mLayoutParams;

    private String mTitleText;

    private String mContentText;

    private String mLeftButtonText;

    private String mRightButtonText;

    /**
     * 底部button的显示状态，0，代表不显示，1代表显示一个，2代表显示两个。
     */
    private int mButtonState = 2;

    /**
     * 设置点击dialog外部是否会显示
     */
    private boolean mIsCanceledOnTouchOutside;

    /**
     * 设置点击dialog外部是否会显示
     */
    private boolean mCancelable;

    /**
     * 点击事件的监听接口引用
     */
    private DialogButtonClickListener mListener;

    public SearchDialogFragment() {

    }

    //@SuppressLint("ValidFragment")
    public SearchDialogFragment(Builder builder) {

        if (builder == null) {
            return;
        }

        mLayoutParams = builder.mLayoutParams;
        mTitleText = builder.mTitleText;
        mContentText = builder.mKeywordText;
        mLeftButtonText = builder.mLeftButtonText;
        mRightButtonText = builder.mRightButtonText;
        mButtonState = builder.mButtonState;
        mIsCanceledOnTouchOutside = builder.mIsCanceledOnTouchOutside;
        mCancelable = builder.mCancelable;
    }

    @Override
    protected int getDialogLayoutResId() {
        return R.layout.dialog_search2;
    }

    @Override
    protected void onInflated(View container, Bundle savedInstanceState) {
        findView(container);

        setView();

        setListener();

        initLayout(container);
    }

    /**
     * 通过父view找到子view
     *
     * @param container 父view
     */
    private void findView(View container) {
        mTitle = container.findViewById(R.id.tvTitle);
        tbKeyword = container.findViewById(R.id.tbKeyword);
        mButtons = container.findViewById(R.id.ll_buttons);
        mLeft = container.findViewById(R.id.tv_left);
        mRight = container.findViewById(R.id.tv_right);
        tv_right_text = container.findViewById(R.id.tv_right_text);
    }

    private TextView tv_right_text;

    /**
     * 设置view内容及是否显示
     */
    private void setView() {

        if (TextUtils.isEmpty(mTitleText)) {
            mTitle.setVisibility(View.GONE);
        } else {
            mTitle.setVisibility(View.VISIBLE);
            mTitle.setText(mTitleText);
        }

        tbKeyword.setText(mContentText);

        setButtons();

    }

    public String getKeyword() {
        return tbKeyword.getText().toString();
    }

    /**
     * 设置点击事件
     */
    private void setListener() {

        mLeft.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.leftOnClick(this);
            }
        });

        mRight.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.rightOnClick(this);
            }
        });

    }

    private void setButtons() {

        switch (mButtonState) {
            case 0:
                mButtons.setVisibility(View.GONE);
                break;

            case 1: {
                mLeft.setVisibility(View.GONE);
                mRight.setVisibility(View.VISIBLE);
                tv_right_text.setText(mRightButtonText);
                break;
            }

            case 2:
            default: {
                mLeft.setVisibility(View.VISIBLE);
                mRight.setVisibility(View.VISIBLE);

                mLeft.setText(mLeftButtonText);
                tv_right_text.setText(mRightButtonText);
                break;
            }
        }

    }

    @Override
    protected WindowManager.LayoutParams getLayoutParams(WindowManager.LayoutParams params) {
        if (mLayoutParams != null) {
            return mLayoutParams;
        }
        return super.getLayoutParams(params);
    }

    @Override
    protected boolean getCanceledOnTouchOutside() {
        if (mIsCanceledOnTouchOutside) {
            return true;
        }
        return super.getCanceledOnTouchOutside();
    }

    @Override
    protected boolean getCancelable() {
        if (mCancelable) {
            return true;
        }
        return super.getCancelable();
    }

    public void show(FragmentManager manager) {
        if (manager == null) {
            return;
        }

        show(manager, "FragmentManager");

    }

    /**
     * 点击事件的监听接口，默认情况不用复写方法，用户可根据实际需求复写。
     */
    public interface DialogButtonClickListener {

        /**
         * 左边点击事件回调方法
         *
         * @param dialog 当前dialog实例
         */
        default void leftOnClick(SearchDialogFragment dialog) {
        }

        /**
         * 右边点击事件回调方法
         *
         * @param dialog 当前dialog实例
         */
        default void rightOnClick(SearchDialogFragment dialog) {
        }

    }

    /**
     * 设置按钮的点击事件监听
     */
    public void setDialogButtonListener(DialogButtonClickListener listener) {
        mListener = listener;
    }

    /***
     * 静态内部类，用builder模式来设置该dialog的相关属性
     *
     */
    public static class Builder {

        /**
         * 设置dialog的layout params
         */
        private WindowManager.LayoutParams mLayoutParams;

        /**
         * 设置title的文本
         */
        private String mTitleText;

        /**
         * 设置dialog文本内容
         */
        private String mKeywordText;

        /**
         * 设置dialog底部左边button的文本，默认显示cancle
         */
        private String mLeftButtonText = "cancel";

        /**
         * 设置dialog底部左边button的文本，默认显示ok
         */
        private String mRightButtonText = "ok";

        /**
         * 设置dialog底部三个button显示状态，1，2 ？
         */
        private int mButtonState = 2;

        /**
         * 设置点击dialog外部是否会显示
         */
        private boolean mIsCanceledOnTouchOutside;

        /**
         * 设置点击dialog外部是否会显示
         */
        private boolean mCancelable;

        public Builder setLayoutParams(WindowManager.LayoutParams layoutParams) {
            mLayoutParams = layoutParams;
            return this;
        }

        public Builder setTitleText(String titleText) {
            mTitleText = titleText;
            return this;
        }

        public Builder setKeyword(String keyword) {
            mKeywordText = keyword;
            return this;
        }

        public Builder setLeftButtonText(String leftButtonText) {
            mLeftButtonText = leftButtonText;
            return this;
        }

        public Builder setRightButtonText(String rightButtonText) {
            mRightButtonText = rightButtonText;
            return this;
        }

        public Builder setButtonState(int buttonState) {
            mButtonState = buttonState;
            return this;
        }

        public Builder setCanceledOnTouchOutside(boolean isCanceledOnTouchOutside) {
            mIsCanceledOnTouchOutside = isCanceledOnTouchOutside;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            mCancelable = cancelable;
            return this;
        }

        public SearchDialogFragment build() {

            this.setTitleText(MyApplication.getInstance().getString(R.string.zsk) + "查询");
            this.setCancelable(true);
            this.setCanceledOnTouchOutside(false);
            this.setKeyword("");
            this.setLeftButtonText("取消");
            this.setRightButtonText("查询");

            SearchDialogFragment dialog = new SearchDialogFragment(this);

            return dialog;
        }
    }


}
