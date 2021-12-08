package com.fntj.app.listener;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.airbnb.lottie.LottieAnimationView;
import com.fntj.app.MyApplication;
import com.fntj.app.R;
import com.fntj.app.dialog.NavingDialog;
import com.fntj.app.handler.RobotHandler;
import com.fntj.app.manager.FNBatteryManager;
import com.fntj.app.util.SU;
import com.fntj.lib.zb.base.BaseActivity;
import com.fntj.lib.zb.util.CommonUtil;

public class NavDistClickListener implements View.OnClickListener {

    public static NavDistClickListener instance;

    public static NavDistClickListener create(BaseActivity context,
                                              RobotHandler robotHandler,
                                              String dist,
                                              boolean goHome,
                                              RobotHandler.NavCancelHandler cancelHandler,
                                              RobotHandler.NavCompleteHandler completeHandler,
                                              int cancelOrDismissOld) {


        if (instance != null) {

            if (cancelOrDismissOld == 1) {
                instance.cancel();
            }
            if (cancelOrDismissOld == 2) {
                instance.dissmiss();
            }

            instance = null;
        }

        instance = new NavDistClickListener(context, robotHandler, dist, goHome, (dlg, d, isTask) -> {
            //取消
            instance.isCancel = true;

            if (cancelHandler != null) {
                cancelHandler.onCancel(dlg, d, instance.isTask);
            }

            instance = null;
        }, (dlg, d) -> {
            //完成
            if (completeHandler != null) {
                completeHandler.onComplete(dlg, d);
            }

            instance = null;
        });

        return instance;
    }

    private BaseActivity context = null;
    private Handler handler;
    private RobotHandler robotHandler;
    private NavingDialog navingDialog;
    private AlertDialog alertDialog;

    private String dist;
    private boolean goHome;
    private boolean isTask = false;

    private RobotHandler.NavCancelHandler cancelHandler;
    private RobotHandler.NavCompleteHandler completeHandler;

    private LottieAnimationView btnStartNav;


    public NavDistClickListener(BaseActivity context,
                                RobotHandler robotHandler,
                                String dist,
                                boolean goHome,
                                RobotHandler.NavCancelHandler cancelHandler,
                                RobotHandler.NavCompleteHandler completeHandler) {

        this.context = context;
        this.robotHandler = robotHandler;
        this.handler = context.handler;

        this.dist = dist;
        this.goHome = goHome;

        this.cancelHandler = cancelHandler;
        this.completeHandler = completeHandler;

        navingDialog = new NavingDialog(context, robotHandler);
    }

    public static void Dissmiss() {
        if (instance == null) return;

        instance.dissmiss();
    }

    public void dissmiss() {
        handler.post(() -> {
            if (hasShowRecord) {
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                if (navingDialog != null) {
                    navingDialog.dismiss();
                }
            }
        });
    }

    public static void Cancel() {
        if (instance == null) return;

        instance.cancel();
    }

    public void cancel() {

        dissmiss();

        if (hasShowRecord) {
            context.showShortToast("任务取消!");
            if (cancelHandler != null) {
                cancelHandler.onCancel(navingDialog, dist, isTask);
            }
        }
    }

    public void complete() {
        context.runUiThread(() -> {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
            if (navingDialog != null) {
                navingDialog.dismiss();
            }

            if (this.completeHandler != null) {
                this.completeHandler.onComplete(navingDialog, dist);
            }
        });
    }

    public void start() {
        onClick(null);
    }

    @Override
    public void onClick(View v) {

        int power = FNBatteryManager.getInstance().getBattery();
        context.showShortToast("power:" + power + "%");

        if (FNBatteryManager.getInstance().isCharging() && power < 90) {
            robotHandler.doSpeek("很抱歉，小曼电量还没充够百分之90，暂不能为您服务");
            return;
        }

        if (power < 20) {
            robotHandler.doSpeek("很抱歉，小曼电量低于百分之20，暂不能为您服务");
            return;
        }

        boolean num = SU.isNumber(dist);

        if (dist.contains("充电")) {
            robotHandler.doSpeek("点击确认按钮，小曼将自动去充电");
        } else {
            robotHandler.doSpeek("点击确认按钮，小曼将带您去：" + dist + (num ? "诊室" : ""));
        }

        View dialogView = View.inflate(context, R.layout.dialog_nav, null);

        btnStartNav = dialogView.findViewById(R.id.btnStartNav);

        //开始
        btnStartNav.setOnClickListener((view) -> {
            btnStartNav.pauseAnimation();
            context.showShortToast("开始导航：" + dist);

            confirmStart();
        });

        //取消
        dialogView.findViewById(R.id.btnCancelNav).setOnClickListener((view) -> {
            isCancel = true;
            isTask = false;

            alertDialog.dismiss();
            btnStartNav.resumeAnimation();
        });

        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        tvTitle.setText("智能引导");

        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        tvMessage.setText(dist + (num ? "诊室" : ""));

        alertDialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(dialogView)
                .create();

        alertDialog.show();

        hasShowRecord = true;
    }

    public void dismissThis() {
        handler.post(() -> {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
        });
    }

    private boolean isCancel = false;

    public void confirmStart() {

        int power = FNBatteryManager.getInstance().getBattery();
        context.showShortToast("power:" + power + "%");

        if (FNBatteryManager.getInstance().isCharging() && power < 90) {
            robotHandler.doSpeek("很抱歉，小曼电量还没充够百分之90，暂不能为您服务");
            return;
        }

        if (power < 20) {
            robotHandler.doSpeek("很抱歉，小曼电量低于百分之20，暂不能为您服务");
            return;
        }

        context.showShortToast("导航目的地：" + dist);

        boolean num = SU.isNumber(dist);

        handler.post(() -> {

            String format = "";
            if (dist.contains("充电")) {
                format = "小曼去充电";
                robotHandler.doSpeek("好的，小曼这就去充电，请注意避让");
            } else {
                format = "小曼带您去：%s" + (num ? "诊室" : "");
                robotHandler.doSpeek("好的，小曼这就带您去" + dist + (num ? "诊室" : "") + "，请注意避让");
            }

            navingDialog.show(dist, format, goHome, (d, ac, isTask) -> {
                //取消导航
                d.dismiss();

                if (cancelHandler != null) {
                    cancelHandler.onCancel(d, ac, this.isTask);
                }

                instance = null;
            }, (d, ac) -> {
                //导航完成
                d.dismiss();

                if (completeHandler != null) {
                    completeHandler.onComplete(d, ac);
                }

                instance = null;
            });
        });

        handler.postDelayed(() -> {

            if (isCancel)
                return;

            String loc = null;

            String ac = dist.contains("充电") ? "充电站" : dist;
            loc = robotHandler.doNavAction(ac);

            if (loc == null) {
                robotHandler.doSpeek("很抱歉，小曼没有找到" + ac + "在哪");
                CommonUtil.showShortToast(MyApplication.getInstance(), "很抱歉，小曼没有找到" + ac + "在哪");

                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                if (navingDialog != null) {
                    navingDialog.dismiss();
                }

                instance = null;

            } else {
                isTask = true;
            }

        }, 5000);

        hasShowRecord = true;

        dismissThis();
    }

    private boolean hasShowRecord = false;

}
