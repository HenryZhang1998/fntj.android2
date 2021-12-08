package com.fntj.app.listener;

import android.content.Intent;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.airbnb.lottie.LottieAnimationView;
import com.bjw.bean.ComBean;
import com.fntj.app.R;
import com.fntj.app.activity.ProgressActivity;
import com.fntj.app.net.NetworkUtil;
import com.fntj.app.util.DialogUtil;
import com.fntj.lib.zb.base.BaseActivity;
import com.fntj.lib.zb.ui.WebViewActivity;
import com.fntj.lib.zb.util.CommonUtil;
import com.fntj.lib.zb.util.StringUtil;
import com.iflytek.cloud.SpeechUtility;
import com.stig.scanner.ScannerInfo;
import com.stig.scanner.impl.IOnDataListener;

public class ScannerOnDataListener implements IOnDataListener {

    private static final String TAG = "ScannerOnDataListener";

    private BaseActivity context;
    private Handler handler;

    private AlertDialog codeDialog;
    private boolean dialogDiaplayed = false;
    private EditText tbCode = null;

    public ScannerOnDataListener(BaseActivity activity, Handler handler) {
        this.context = activity;
        this.handler = handler;
    }

    @Override
    public void onDataResult(ComBean comBean, String data) {
        Log.d("data == ", data);

        handler.post(() -> {
            context.showShortToast("扫描结果：" + data);
        });

        if (dialogDiaplayed) {

            handler.post(() -> {
                tbCode.setText(data);
            });

            handler.postDelayed(() -> {
                codeDialog.dismiss();

                parseCode(data);
            }, 1000);

        } else {
            parseCode(data);
        }
    }

    void parseCode(String code) {

        if (StringUtil.isEmpty(code, false)) {
            return;
        }

        if (code.toLowerCase().startsWith("http")) {
            context.toActivity(WebViewActivity.createIntent(context, "UnTitled", code));
            return;
        }

        String pebillArchiveId = code.trim();
        boolean b = pebillArchiveId.matches("^\\d+$");
        if (!b) {
            return;
        }

        if (!NetworkUtil.isNetworkAvailable(context)) {
            Toast.makeText(context, "网络不可用，请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }

        DialogUtil.showLoading(context, "正在处理，请稍候...");

        Intent intent = ProgressActivity.createIntent(context, "体检信息", pebillArchiveId);

        context.toActivity(intent);

        handler.postDelayed(()->{
            DialogUtil.dismessLoading(context);
        }, 1000);
    }

    public void showDialog() {

        handler.post(() -> {

            View dialogCode = View.inflate(context, R.layout.dialog_scancode, null);

            LottieAnimationView btnSearch = dialogCode.findViewById(R.id.btnSearch);
            btnSearch.setOnClickListener(v -> {
                String code = tbCode.getText().toString().trim();
                if (code.length() == 0) {
                    CommonUtil.showShortToast(context, "请扫描条形码或输入体检编号");
                    return;
                }

                dialogDiaplayed = false;
                codeDialog.dismiss();

                parseCode(code);
            });

            Button btnCancel = dialogCode.findViewById(R.id.btnCancel);
            btnCancel.setOnClickListener((v)->{
                codeDialog.dismiss();
            });

            tbCode = dialogCode.findViewById(R.id.tbAppKey);
            tbCode.setInputType(EditorInfo.TYPE_CLASS_NUMBER);

            codeDialog = new AlertDialog.Builder(context)
                    .setTitle("请扫描条形码或输入体检编号")
                    .setIcon(R.drawable.i_g3)
                    .setView(dialogCode)
                    .setOnDismissListener((dialog) -> {
                        dialogDiaplayed = false;
                        tbCode = null;
                    })
//                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            codeDialog.dismiss();
//                        }
//                    })
                    .create();

            codeDialog.setCancelable(false);
            codeDialog.setCanceledOnTouchOutside(false);
            codeDialog.show();
            try {
                DisplayMetrics dm = new DisplayMetrics();
                context.getWindowManager().getDefaultDisplay().getMetrics(dm);

                int ww = dm.widthPixels;
                int dw = (int) (dm.widthPixels * 0.5);
                int x = (ww - dw) / 2;

                codeDialog.getWindow().setLayout(dw, LinearLayout.LayoutParams.WRAP_CONTENT);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            dialogDiaplayed = true;
        });
    }

    public void init(){
        String mode = getSystemModel();

        try {

            ScannerInfo.getInstance().setDebug(true);//默认false
            ScannerInfo.getInstance().acceptRosData(mode);
            ScannerInfo.getInstance().setDataListener(this);
        } catch (Exception ex) {
            context.showShortToast("无法初始化扫描器（" + ex.getMessage() + "），请检查设备是否就绪");
        }
    }

    public void unInit() {
        if (SpeechUtility.getUtility() != null) {
            SpeechUtility.getUtility().destroy();

            try {
                new Thread().sleep(40);
            } catch (InterruptedException e) {
                Log.w(TAG, "msc uninit failed" + e.toString());
            }
        }
    }


    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

}
