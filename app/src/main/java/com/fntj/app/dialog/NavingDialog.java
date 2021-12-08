package com.fntj.app.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.airbnb.lottie.LottieAnimationView;
import com.fntj.app.R;
import com.fntj.app.handler.RobotHandler;
import com.fntj.app.listener.NavDistClickListener;
import com.fntj.lib.zb.base.BaseActivity;
import com.fntj.lib.zb.util.StringUtil;

public class NavingDialog {

    private BaseActivity context;
    private AlertDialog dialog;
    private TextView tvMessage;
    private View view;
    private LottieAnimationView btnCancelNav;

    private RobotHandler robotHandler;
    private String dist;
    private boolean goHome;

    private RobotHandler.NavCancelHandler cancelHandler;
    private RobotHandler.NavCompleteHandler completeHandler;

    public NavingDialog(BaseActivity context, RobotHandler robotHandler) {
        this.context = context;
        this.robotHandler = robotHandler;
    }

    public void show(String dist, String format, boolean goHome, RobotHandler.NavCancelHandler cancelHandler, RobotHandler.NavCompleteHandler completeHandler) {
        this.dist = dist;
        this.goHome = goHome;

        this.cancelHandler = cancelHandler;
        this.completeHandler = completeHandler;

        context.runUiThread(() -> {
            if (dialog == null) {
                dialog = create(context);
            }

            // 显示文本
            tvMessage.setText(String.format(format, dist));
            dialog.show();
        });

        robotHandler.registVoice(this, voice -> {
            if (StringUtil.isEmpty(voice)) {
                return;
            }

            if (voice.contains("取消")) {
                context.runUiThread(() -> {
                    context.showShortToast("导航取消");
                    if (cancelHandler != null) {
                        cancelHandler.onCancel(this, dist, true);
                    }
                });
            }
        });

        robotHandler.registMoveStatus(this, (code) -> {

            // code
            // 0 : 静止待命  
            // 1 : 上次目标失败，等待新的导航命令  
            // 2 : 上次目标完成，等待新的导航命令  
            // 3 : 移动中，正在前往目的地  
            // 4 : 前方障碍物  
            // 5 : 目的地被遮挡
            // 6 ：用户取消导航
            // 7 ：收到新的导航

            System.out.println("move_status-->dialog-->" + code);

            if (1 == code) {
                context.handler.post(() -> {
                    if (goHome) {
                        context.showShortToast("很抱歉，本次导航任务失败，小曼现在要回前台，请注意避让");
                    } else {
                        context.showShortToast("很抱歉，本次导航任务失败");
                    }
                    
//                    if(this.cancelHandler != null){
//                        this.cancelHandler.onCancel(this, dist, true);
//                    }
                    
                    NavDistClickListener.Dissmiss();
                    NavDistClickListener.instance = null;
                });

                if (goHome) {
                    robotHandler.doSpeek("很抱歉，本次导航任务失败，小曼现在要回前台，请注意避让");

                    context.handler.postDelayed(() -> {
                        robotHandler.goHome();
                    }, 5000);
                } else {
                    robotHandler.doSpeek("很抱歉，本次导航任务失败");
                }
            }

            if (4 == code) {
                //障碍物
                robotHandler.doSpeek("小曼正在导航，请注意避让");
            }

            if (5 == code) {
                //取消导航

                if (goHome) {
                    context.handler.post(() -> {
                        context.showShortToast("很抱歉，因为目的地被遮挡，本次导航任务失败，小曼现在要回前台，请注意避让");
                    });

                    robotHandler.doSpeek("很抱歉，因为目的地被遮挡，本次导航任务失败，小曼现在要回前台，请注意避让");
                    context.handler.postDelayed(() -> {
                        robotHandler.goHome();
                    }, 5000);
                } else {
                    context.handler.post(() -> {
                        context.showShortToast("很抱歉，因为目的地被遮挡，本次导航任务失败");
                    });
                    robotHandler.doSpeek("很抱歉，因为目的地被遮挡，本次导航任务失败");
                }
            }

            if (7 == code) {

            }

            if (2 == code) {
                //到了
                if (this.completeHandler != null) {
                    this.completeHandler.onComplete(this, dist);
                }
            }
        });
    }

    public void dismiss() {

        robotHandler.unregistVoice(this);
        robotHandler.unregistMoveStatus(this);
        context.runUiThread(() -> {
            if (dialog == null || !dialog.isShowing()) {
                return;
            }
            dialog.dismiss();
        });
    }

    private AlertDialog create(Context context) {

        // 首先得到整个View
        view = LayoutInflater.from(context).inflate(R.layout.dialog_naving, null);

        // 页面中显示文本
        tvMessage = view.findViewById(R.id.tvMessage);

        //取消导航
        btnCancelNav = view.findViewById(R.id.btnCancelNav);
        btnCancelNav.setOnClickListener(v -> {
            if (cancelHandler != null) {
                cancelHandler.onCancel(NavingDialog.this, dist, true);
            }
        });

        // 创建自定义样式的Dialog
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle("").setView(view).setCancelable(false).create();

        return dialog;
    }
}
