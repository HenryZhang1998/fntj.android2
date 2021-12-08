package com.fntj.app.util;

import android.app.Activity;

import com.fntj.app.dialog.LoadingDialog;

import java.util.HashMap;
import java.util.Map;

public class DialogUtil {

    private static Map<Activity, LoadingDialog> loadingDialogMap = new HashMap<>();
    public static void showLoading(Activity context) {
        showLoading(context, null);
    }
    public static void showLoading(Activity activity, String message) {
        LoadingDialog dialog = null;

        if(loadingDialogMap.containsKey(activity)){
            dialog = loadingDialogMap.get(activity);
        }

        if (dialog == null) {
            dialog = new LoadingDialog(activity);
            loadingDialogMap.put(activity, dialog);
        }

        final LoadingDialog loadingDialog = dialog;
        activity.runOnUiThread(() -> {
            loadingDialog.show(message);
        });
    }

    public static void dismessLoading(Activity activity){
        if(loadingDialogMap.containsKey(activity)){

            final LoadingDialog loadingDialog = loadingDialogMap.get(activity);

            loadingDialog.dismiss();
        }
    }

    public static void onDestory(Activity activity){

        if(loadingDialogMap.containsKey(activity)){

            LoadingDialog loadingDialog = loadingDialogMap.get(activity);
            if(loadingDialog != null) {
                loadingDialog.dismiss();
            }

            loadingDialogMap.remove(activity);
        }
    }
}
