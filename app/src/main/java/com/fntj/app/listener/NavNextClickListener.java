package com.fntj.app.listener;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.fntj.app.R;
import com.fntj.app.dialog.NavingDialog;
import com.fntj.app.handler.RobotHandler;
import com.fntj.app.manager.FNBatteryManager;
import com.fntj.lib.zb.base.BaseActivity;
import com.fntj.lib.zb.util.StringUtil;

public class NavNextClickListener implements View.OnClickListener {

    private String dist;
    private boolean goHome;
    private String completeSpeek;
    private BaseActivity context = null;
    private Handler handler;
    private RobotHandler robotHandler;
    private NavingDialog navingDialog;
    private AlertDialog alertDialog;

    public NavNextClickListener(BaseActivity context, RobotHandler robotHandler, String dist, boolean goHome, String completeSpeek) {

        this.context = context;
        this.robotHandler = robotHandler;
        this.handler = new Handler();
        this.dist = dist;
        this.goHome = goHome;
        this.completeSpeek = completeSpeek;

        navingDialog = new NavingDialog(context, robotHandler);
    }

    public void dissmiss() {
        alertDialog.dismiss();
        navingDialog.dismiss();
    }

    @Override
    public void onClick(View v) {

        if (StringUtil.isEmpty(dist)) {
            context.showShortToast("没有目的地");
            robotHandler.doSpeek("很抱歉，小曼不知道目的地是哪里");
            return;
        }

        String loc = robotHandler.getNavLocation(dist);
        if (StringUtil.isEmpty(dist)) {
            context.showShortToast(String.format("没有找到%s的坐标", dist));
            robotHandler.doSpeek(String.format("很抱歉，小曼没有找到%s的坐标在哪里"));
            return;
        }

        int power = FNBatteryManager.getInstance().getBattery();
        context.showShortToast("power:" + power + "%");

        if (FNBatteryManager.getInstance().isCharging() && power < 90) {
            robotHandler.doSpeek("很抱歉，小曼电量还没充够百分之90，暂不能为您导诊服务");
            return;
        }

        if (power < 20) {
            robotHandler.doSpeek("很抱歉，小曼电量低于百分之20，暂不能为您导诊服务");
            return;
        }

        context.showShortToast("引导下一项目地点：" + dist + " / " + loc);
        robotHandler.doSpeek("点击开始按钮，小曼将带您到" + dist);

        View dialogView = View.inflate(context, R.layout.dialog_nav, null);

        dialogView.findViewById(R.id.btnStartNav).setOnClickListener((view) -> {

            context.showShortToast("开始导航：" + dist);

            handler.post(() -> {
                robotHandler.doSpeek("好的，小曼这就带您去" + dist + "，请注意避让");

                //String completeSpeek = String.format("%s您好，%s诊室到了，感谢您使用福能体检智能导诊系统，再见", data.getUserName(), data.getWaitingItem().getRoomCode());

                navingDialog.show(dist, "小曼带您去：%s", goHome, (d, ac, isTask) -> {

                    navingDialog.dismiss();
                    robotHandler.cancelNav();
                    context.showShortToast("取消导航!");

                    if (goHome) {
                        robotHandler.doSpeek("本次导航任务已取消，小曼现在要返回前台，请注意避让");
                    } else {
                        robotHandler.doSpeek("本次导航任务已取消");
                    }

                    handler.postDelayed(() -> {
                        robotHandler.goHome();
                        context.showShortToast("返回前台!");
                        context.finish();
                    }, 5000);

                }, (d, ac) -> {
                    //complete
                    String completeSpeek = String.format("您好，%s到了，感谢您使用福能体检智能导诊系统，再见", ac);
                    robotHandler.doSpeek(completeSpeek);
                });
            });

            handler.postDelayed(() -> {
                robotHandler.doNav(dist);
            }, 5000);

            alertDialog.dismiss();
        });

        dialogView.findViewById(R.id.btnCancelNav).setOnClickListener((view) -> {
            alertDialog.dismiss();
        });

        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        tvTitle.setText("智能引导");

        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        tvMessage.setText("点击【开始】按钮，小曼将带您到：" + dist);

        alertDialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(dialogView)
                .create();

        alertDialog.show();
    }

}
