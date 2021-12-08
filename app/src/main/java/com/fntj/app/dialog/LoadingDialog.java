package com.fntj.app.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.fntj.app.R;
import com.fntj.lib.zb.util.StringUtil;

/**
 * 公用的Loding
 *
 * @author devid
 */
public class LoadingDialog {

    private Activity context;
    private View view;
    private TextView tvMessage;
    private Dialog dialog;

    public LoadingDialog(Activity context) {
        this.context = context;
    }

    private static final String message  = "正在加载，请稍后...";

    public void show() {
        show(message);
    }

    public void show(String msg) {

        context.runOnUiThread(() -> {

            if (dialog == null) {
                dialog = create(context);
            }

            String showMessage = msg;
            if(StringUtil.isEmpty(msg)){
                showMessage = message;
            }

            // 显示文本
            tvMessage.setText(showMessage);
            if(!dialog.isShowing()) {
                dialog.show();
            }
        });
    }

    public void dismiss() {
        context.runOnUiThread(() -> {
            if (dialog != null && !dialog.isShowing()) {
                return;
            }

            dialog.dismiss();
        });
    }

    /**
     * 得到自定义的progressDialog
     *
     * @param context
     * @return
     */
    private Dialog create(Context context) {

        // 首先得到整个View
        view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);

        // 页面中显示文本
        tvMessage = (TextView) view.findViewById(R.id.tvMessage);

        // 创建自定义样式的Dialog
        //Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);

        // 设置返回键无效
        //loadingDialog.setCancelable(false);

        // 创建自定义样式的Dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(context)
                .setTitle(null)
                .setView(view)
                .setCancelable(false)
                .create();

//        ViewGroup.LayoutParams params = view.getLayoutParams();
//        params.width = 400;
//        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//
//        loadingDialog.setContentView(view, params);

        return loadingDialog;
    }
}
