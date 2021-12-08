package com.fntj.app.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.alibaba.fastjson.JSONObject;
import com.fntj.app.MyApplication;
import com.fntj.app.R;
import com.fntj.app.handler.RobotHandler;
import com.fntj.app.manager.DataManager;
import com.fntj.app.net.NetworkUtil;
import com.fntj.app.net.RobotApi;
import com.fntj.app.util.AppUtils;
import com.fntj.app.util.DeviceUtils;
import com.fntj.lib.zb.util.CommonUtil;
import com.fntj.lib.zb.util.Log;
import com.fntj.lib.zxing.util.LogUtils;
import com.fntj.lib.zxing.util.PermissionUtils;
import com.liulishuo.filedownloader.exception.FileDownloadHttpException;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import constacne.UiType;
import listener.UpdateDownloadListener;
import model.UiConfig;
import model.UpdateConfig;
import update.UpdateAppUtils;

public class SplashActivity extends Activity implements View.OnClickListener {

    private Context context;
    private Handler handler;

    TextView tv_version;

    private int PERMISSION_REQUEST_CODE = 100023;

    private static final String[] permissions = new String[]{

            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            //Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.RECORD_AUDIO,
            //Manifest.permission.BATTERY_STATS,
            Manifest.permission.CAMERA,
            //Manifest.permission.VIBRATE,

//            Manifest.permission_group.MICROPHONE,
//            Manifest.permission_group.CAMERA,
//            Manifest.permission_group.PHONE,
//            Manifest.permission_group.STORAGE,
//            Manifest.permission_group.LOCATION
    };

    private String[] requestPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //CrashReport.testJavaCrash();

        context = this;
        handler = new Handler();

        DeviceUtils.init(this);

        int dpi = DeviceUtils.getScreenInfo(this).getDensityDpi();
        String dpiName = DeviceUtils.getScreenInfo(this).getDensityDpiName();

        tv_version = findViewById(R.id.tv_version);
        tv_version.setText("version:" + AppUtils.getVersionName(this) + " / dpi:" + dpiName + "("+dpi+")");

        //CommonUtil.showShortToast(SplashActivity.this, "debug 00");

        //CommonUtil.showShortToast(SplashActivity.this, "debug 01");
        if (Build.VERSION.SDK_INT < 23) {
            //Android 6.0（API23）之前应用的权限在安装程序时就全部授予，运行时应用不再需要询问用户。

            //CommonUtil.showShortToast(SplashActivity.this, "debug 02");
            doLaunch();
        } else {
            // 在Android 6.0和更高版本对权限进行了分类
            //CommonUtil.showShortToast(SplashActivity.this, "debug 03");
            if (!reqPermission()) {
                doLaunch();
            }
        }
    }

    private boolean reqPermission() {

//        runOnUiThread(() -> {
//            CommonUtil.showShortToast(SplashActivity.this, "debug reqPermission");
//        });

        List<String> ps = new ArrayList<>();

        for (String permission : permissions) {
            if (!PermissionUtils.checkPermission(this, permission)) {
                LogUtils.d("checkPermissionResult != PERMISSION_GRANTED");
                ps.add(permission);
            }
        }

        if (ps.size() > 0) {
            requestPermissions = new String[ps.size()];
            ps.toArray(requestPermissions);

            PermissionUtils.requestPermissions(this, requestPermissions, PERMISSION_REQUEST_CODE);
            return true;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!PermissionUtils.requestPermissionsResult(requestPermissions, permissions, grantResults)) {
                reqPermission();
            } else {
//                runOnUiThread(() -> {
//                    CommonUtil.showShortToast(SplashActivity.this, "debug 100");
//                });

                doLaunch();
            }
        }
    }

    private void doLaunch() {
        if (!NetworkUtil.isNetworkAvailable(context)) {
            runOnUiThread(() -> {
                CommonUtil.showShortToast(context, "网络不可用，请检查网络");
            });

            handler.postDelayed(() -> {
                finish();
            }, 1500);
            return;
        }

//        CommonUtil.showShortToast(this, "debug 1");

        RobotApi.onLaunch(this, new RobotApi.MyResponseListener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject j) {
//                runOnUiThread(() -> {
//                    CommonUtil.showShortToast(SplashActivity.this, "debug 2");
//                });
                onLunch(j);

//                runOnUiThread(() -> {
//                    CommonUtil.showShortToast(SplashActivity.this, "debug 3");
//                });
                if (!isUpdate(j)) {
//                    runOnUiThread(() -> {
//                        CommonUtil.showShortToast(SplashActivity.this, "debug 4");
//                    });
                    startMain(j);
                }
            }

            @Override
            public void onError(String message, String code) {
                runOnUiThread(() -> {
                    CommonUtil.showShortToast(context, "程序启动失败：" + message);

                    if ("clientId-not-exist".equalsIgnoreCase(code)
                            || "appKey-empty".equalsIgnoreCase(code)
                            || "appKey-error".equalsIgnoreCase(code)) {

                        showDialog();
                    } else {
                        handler.postDelayed(() -> {
                            finish();
                        }, 1500);
                    }
                });
            }
        });
    }

    Toast toast = null;

    private void showProgress(final int p) {

        handler.post(() -> {
            if (toast == null) {
                toast = Toast.makeText(SplashActivity.this, "", Toast.LENGTH_SHORT);
            }

            if (p == 100) {
                toast.setText("下载完成，准备安装");
            } else {
                toast.setText("正在下载 ： " + p + " %");
            }

            toast.show();
        });
    }

    private boolean isUpdate(JSONObject j) {
        if (j.containsKey("update")) {

            JSONObject update = j.getJSONObject("update");

            final boolean force = update.getBoolean("force");
            int versionCode = AppUtils.getVersionCode(SplashActivity.this);
            int vc = update.getIntValue("versionCode");

            if (vc > versionCode) {
                //show update

                String apkUrl = update.getString("apkUrl");

                String apkName = String.format("fntj_%s_%s.apk", update.getString("versionName"), System.currentTimeMillis());

                UpdateConfig updateConfig = new UpdateConfig();
                updateConfig.setCheckWifi(false);
                updateConfig.setNeedCheckMd5(false);
                updateConfig.setNotifyImgRes(R.drawable.fntj_logo00);
                updateConfig.setApkSaveName(apkName);
                updateConfig.setShowNotification(true);

                updateConfig.setServerVersionCode(vc);
                updateConfig.setServerVersionName(update.getString("versionName"));
                //updateConfig.setForce(force);
                updateConfig.setShowDownloadingToast(true);

                UiConfig uiConfig = new UiConfig();
                uiConfig.setUiType(UiType.PLENTIFUL);

                StringBuilder updateContent = new StringBuilder();
                updateContent.append("新版本：" + update.getString("versionName") + "\n");
                updateContent.append("更新内容：" + update.getString("updateContent") + "\n");
                UpdateAppUtils
                        .getInstance()
                        .apkUrl(apkUrl)
                        .updateTitle("版本更新")
                        .updateContent(updateContent.toString())
                        .uiConfig(uiConfig)
                        .updateConfig(updateConfig)
                        .setMd5CheckResultListener(result -> {

                        })
                        .setOnInitUiListener((view, updateConfig1, uiConfig1) -> {
                            view.findViewById(R.id.btn_update_cancel).setOnClickListener((v) -> {
                                MyApplication.getInstance().exit();
                            });
                        })
                        .setUpdateDownloadListener(new UpdateDownloadListener() {
                            @Override
                            public void onStart() {
                                Log.d("APK", "开始下载");
                            }

                            @Override
                            public void onDownload(int progress) {
                                Log.d("APK", "下载：" + progress + "%");
                                showProgress(progress);
                            }

                            @Override
                            public void onFinish() {
                                Log.d("APK", "下载完成");
                                showProgress(100);
                            }

                            @Override
                            public void onError(@NotNull Throwable e) {
                                Log.d("APK", "下载失败");
                                Class ec = e.getClass();
                                String code, msg;

                                if (e instanceof FileDownloadHttpException) {
                                    FileDownloadHttpException ex = (FileDownloadHttpException) e;
                                    code = String.valueOf(ex.getCode());
                                    msg = ex.getMessage();
                                } else if (e instanceof IOException) {
                                    IOException ex = (IOException) e;
                                    code = "IOException";
                                    msg = ex.getMessage();
                                } else {
                                    code = ec.getSimpleName();
                                    msg = e.getMessage();
                                }
                                System.out.println(ec.getName());

                                CommonUtil.showShortToast(SplashActivity.this, String.format("文件下载失败:[%s]:%s", code, msg));
                                handler.postDelayed(() -> {
                                    MyApplication.getInstance().exit();
                                }, 1500);
                            }
                        })
                        .update();

                return true;
            }
        }

        return false;
    }


    private void onLunch(JSONObject j) {

        String token = j.getString("token");
        System.out.println("token:" + token);

        DataManager.getInstance().setToken(token == null ? "" : token);

        JSONObject locations = j.getJSONObject("locations");
        System.out.println("locations:" + locations);

        JSONObject voices = j.getJSONObject("voices");
        System.out.println("voices:" + voices);

        RobotHandler.Locations.clear();
        for (String key : locations.keySet()) {
            RobotHandler.Locations.put(key, locations.getString(key));
        }

        RobotHandler.Voices.clear();
        for (String key : voices.keySet()) {
            RobotHandler.Voices.put(key, voices.getString(key));
        }
    }

    private AlertDialog appkeyDialog;

    void showDialog() {

//        runOnUiThread(() -> {
//            CommonUtil.showShortToast(this, "显示AppKey输入框");
//        });

        handler.post(() -> {
            String clientId = RobotApi.getClientId();

            View dialogAppKey = View.inflate(context, R.layout.dialog_appkey, null);

            TextView tbClientId = dialogAppKey.findViewById(R.id.tbClientId);
            tbClientId.setText(clientId);

            EditText tbAppKey = dialogAppKey.findViewById(R.id.tbAppKey);

            appkeyDialog = new AlertDialog.Builder(context).setTitle("请输入AppKey")
                    .setIcon(R.drawable.edit_light)
                    .setView(dialogAppKey)
                    .setPositiveButton("确定", (dialogInterface, i) -> {

                        String appKey = tbAppKey.getText().toString().trim();
                        if (appKey.length() == 0) {
                            CommonUtil.showShortToast(context, "请输入AppKey");
                            return;
                        }

                        DataManager.getInstance().setAppKey(appKey);

                        checkAppKey();

                        appkeyDialog.dismiss();

                    }).setNegativeButton("取消", (dialogInterface, i) -> finish()).show();
        });
    }

    void checkAppKey() {

        RobotApi.onLaunch(this, new RobotApi.MyResponseListener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject j) {
                onLunch(j);

                handler.post(() -> {
                    CommonUtil.showShortToast(context, "验证成功！");
                });

                if (!isUpdate(j)) {
//                    handler.post(() -> {
//                        CommonUtil.showShortToast(context, "debug 100！");
//                    });
                    startMain(j);
                } else {
//                    handler.post(() -> {
//                        CommonUtil.showShortToast(context, "debug 101！");
//                    });
                }
            }

            @Override
            public void onError(String message, String code) {
                runOnUiThread(() -> {
                    CommonUtil.showShortToast(context, "验证失败：" + message);
                    if ("clientId-not-exist".equalsIgnoreCase(code)
                            || "appKey-empty".equalsIgnoreCase(code)
                            || "appKey-error".equalsIgnoreCase(code)) {

                        showDialog();
                    } else {
                        handler.postDelayed(() -> {
                            finish();
                        }, 1500);
                    }
                });
            }
        });
    }

    void startMain(JSONObject j) {
        //初始化系统

        //讯飞ID
        //5b95eb16
        final String ifly_id = j.containsKey("ifly_id") ? j.getString("ifly_id") : "37ed734f";//"5b95eb16";

        //586b954d
        final String ifly_id2 = j.containsKey("ifly_id2") ? j.getString("ifly_id2") : "37ed734f"; //RobotHandler.IFLY_ID; //"37ed734f";

        //锐曼ID
        final String reeman_id = j.containsKey("reeman_id") ? j.getString("reeman_id") : RobotHandler.REEMAN_ID;//"qhaCKzWd6AOM0LBP";

        //DialogUtil.showLoading(this);

        handler.postDelayed(() -> {
            startActivity(MainActivity2.createIntent(context, ifly_id, ifly_id2, reeman_id));
            finish();
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        //DialogUtil.onDestory(this);

        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(R.anim.fade, R.anim.hold);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btn_update_cancel:
                finish();
                break;

            case R.id.btn_update_sure:
                break;
        }

        System.out.println(id);
    }
}