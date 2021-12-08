
package com.fntj.app.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fntj.app.R;
import com.fntj.app.adapter.ProgressFinishedAdapter;
import com.fntj.app.adapter.ProgressHoldAdapter;
import com.fntj.app.handler.RobotHandler;
import com.fntj.app.listener.NavDistClickListener;
import com.fntj.app.listener.TvNextClickListener;
import com.fntj.app.manager.FileManager;
import com.fntj.app.model.QueueInfo;
import com.fntj.app.net.RobotApi;
import com.fntj.app.util.DialogUtil;
import com.fntj.lib.zb.base.BaseActivity;
import com.fntj.lib.zb.interfaces.OnBottomDragListener;
import com.fntj.lib.zb.model.APIResult;
import com.fntj.lib.zb.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 体检单详情
 * <p>
 * <p>
 * RobotActionProvider 是提供机器人硬件控制接口以及机器人系统接口。
 * 通过 RobotActionProvider 提供的接口，可以实现行走；
 * 导航以及串口操作等功能，一定要初始化 ConnectServer，并注册回调函数。
 * <p>
 * ConnectServer 是提供外设控制器，包括导航、打印机、身份证读卡器。
 * 通过 ConnectServer 注册对应的监听回调方法，可以获取外设实时数据；
 */

public class ProgressActivity extends BaseActivity implements OnBottomDragListener {

    private static final String TAG = "ProgressActivity";

    public static ProgressActivity instance = null;

    /**
     * 启动这个Activity的Intent
     *
     * @param context
     * @param title
     * @return
     */
    public static Intent createIntent(Context context, String title, String code) {

        if (instance != null) {
            instance.finish();
        }

        return new Intent(context, ProgressActivity.class)
                .putExtra(INTENT_TITLE, title)
                .putExtra("code", code);
    }

    private Handler handler = new Handler();

    private String code = null;
    private QueueInfo data = null;

    private TextView tv_no2, tv_no1;
    private TextView tbBarTitle, tvNextProj;

    private RecyclerView recyclerView1;
    private RecyclerView recyclerView2;

    private ProgressFinishedAdapter adapter1;
    private ProgressHoldAdapter adapter2;

    private RobotHandler robotHandler;

    private LottieAnimationView btnNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        robotHandler = RobotHandler.getInstance();

        intent = getIntent();

        code = intent.getStringExtra("code");

        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题

        setContentView(R.layout.activity_progress);

        initView();

        initEvent();

        initData();
    }

    @Override
    public void initView() {

        tbBarTitle = findViewById(R.id.tbBarTitle);
        tbBarTitle.setText("查询：" + code);

        tvNextProj = findViewById(R.id.tvNextProj);

        tv_no1 = findViewById(R.id.tv_no1);
        tv_no2 = findViewById(R.id.tv_no2);

        recyclerView1 = findViewById(R.id.rvBaseRecycler1);
        recyclerView2 = findViewById(R.id.rvBaseRecycler2);

        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));

        adapter1 = new ProgressFinishedAdapter(context);
        adapter2 = new ProgressHoldAdapter(context);

        recyclerView1.setAdapter(adapter1);
        recyclerView2.setAdapter(adapter2);

        btnNav = findViewById(R.id.btnNav);
    }

    private NavDistClickListener createNavDistClickListener(String dist) {
        NavDistClickListener navDistClickListener = NavDistClickListener.create(this, robotHandler, dist, true, (d, ac, isTask) -> {
            //取消导航
            //btnNav.resumeAnimation();

            if (isTask) {
                MainActivity2.instance.handler.postDelayed(() -> {
                    MainActivity2.instance.showShortToast("任务取消!");

                    robotHandler.doSpeek("导航任务已取消，小曼现在要返回前台，请注意避让");
                    robotHandler.cancelNav();

                    MainActivity2.instance.handler.postDelayed(() -> {
                        robotHandler.goHome();
                    }, 5000);

                }, 500);

                finish();
            }
        }, (d, ac) -> {
            //导航到了

            handler.post(() -> {
                String completeSpeek = String.format("%s您好，%s诊室到了，感谢使用，再见", data.getUserName(), dist);
                robotHandler.doSpeek(completeSpeek);
            });

            context.handler.postDelayed(() -> {
                robotHandler.goHome();
                finish();
            }, 6000);

        }, 1);

        return navDistClickListener;
    }

    private void setContent(final QueueInfo info) {

        data = info;
        tvNextClickListener.setData(info);

        String dist = data.getWaitingItem() == null ? null : data.getWaitingItem().getRoomCode();

        String speek = info.getUserName() + "您好";
        if (info.getWaitingItem() != null) {
            speek += "，您的下一个项目是：" + info.getWaitingItem().getTitle();
            if (!StringUtil.isEmpty(info.getWaitingItem().getRoomCode())) {
                speek += "，在" + dist + "诊室";
            }
        } else {
            speek += "，没有找到您下一个体检项目";
        }

        robotHandler.doSpeek(speek);

        runUiThread(() -> {

            btnNav.setOnClickListener((v) -> {
                btnNav.pauseAnimation();

                createNavDistClickListener(dist).start();
            });

            String gen = "未知";

            if ("0".equals(info.getGender())) {
                gen = "未知";
            }

            if ("1".equals(info.getGender())) {
                gen = "男";
            }

            if ("2".equals(info.getGender())) {
                gen = "女";
            }

            ((TextView) findView(R.id.tvCode)).setText(code);
            ((TextView) findView(R.id.tvName)).setText(info.getUserName());
            ((TextView) findView(R.id.tvGender)).setText(gen);

            if (StringUtil.isEmpty(info.getLastDepartmentName())) {
                ((TextView) findView(R.id.tvlastDepartmentName)).setText("--");
            } else {
                ((TextView) findView(R.id.tvlastDepartmentName)).setText(info.getLastDepartmentName());
            }

            if (info.getWaitingItem() == null) {
                tvNextProj.setText("--");
                findView(R.id.btnNavFrameLayout).setVisibility(View.GONE);
            } else {
                String nextText = data.getWaitingItem().getTitle();
                if (!StringUtil.isEmpty(data.getWaitingItem().getRoomCode())) {
                    nextText += String.format("(%s诊室)", data.getWaitingItem().getRoomCode());
                }

                tvNextProj.setText(nextText);
                findView(R.id.btnNavFrameLayout).setVisibility(View.VISIBLE);
            }

            if (data.getFinishedItems() == null || data.getFinishedItems().size() == 0) {
                tv_no1.setVisibility(View.VISIBLE);
                recyclerView1.setVisibility(View.GONE);
            } else {
                tv_no1.setVisibility(View.GONE);
                adapter1.refresh(data.getFinishedItems());
                recyclerView1.setVisibility(View.VISIBLE);
            }

            if (data.getHoldItems() == null || data.getHoldItems().size() == 0) {
                tv_no2.setVisibility(View.VISIBLE);
                recyclerView2.setVisibility(View.GONE);
            } else {
                tv_no2.setVisibility(View.GONE);
                adapter2.refresh(data.getHoldItems());
                recyclerView2.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void initData() {

        DialogUtil.showLoading(this, "正在查询数据，请稍候...");

        robotHandler.doSpeek("正在查询您的体检数据，请稍候");

        loadData();
    }

    private TvNextClickListener tvNextClickListener;


    @Override
    public void initEvent() {

        tvNextClickListener = new TvNextClickListener(this, robotHandler);

        tvNextProj.setOnClickListener(tvNextClickListener);

        robotHandler.registVoice(this, (voice) -> {
            showShortToast(voice);
        });
    }

    public void refreshData(View view) {

        //刷新
        DialogUtil.showLoading(this, "正在刷新据，请稍候...");

        robotHandler.doSpeek("正在刷新您的体检数据，请稍候");

        loadData();
    }

    Map<String, String> codes = new HashMap<String, String>() {
        {
            put("000000", "前台");
            put("000001", "充电");
            put("000002", "卫生间");

            put("000003", "铂");
            put("000312", "312");
            put("000311", "311");
            put("000310", "310");
            put("000309", "309");
            put("000308", "308");
            put("000307", "307");
            put("000306", "306");
            put("000305", "305");
            put("000304", "304");
            put("000303", "303");
            put("000302", "302");
            put("000301", "301");

            put("000315", "315");
            put("000316", "316");
            put("000317", "317");
            put("000318", "318");
            put("000319", "319");
            put("000321", "321");
            put("000322", "322");
            put("000323", "323");

            put("8873292", "312");
        }
        //"0092"
    };

    private void loadData() {

        QueueInfo queueInfo = null;

        try {
            if (codes.containsKey(code)) {
                String dist = codes.get(code);

                APIResult<String> jresult = FileManager.readAsset(context, "8873292.json");

                if (jresult.isSuccess()) {

                    String json = jresult.getData();

                    JSONObject j = JSONObject.parseObject(json);

                    JSONArray jarr = j.getJSONArray("data");

                    JSONObject jdata = jarr.getJSONObject(0);

                    queueInfo = jdata.toJavaObject(QueueInfo.class);

                    DialogUtil.dismessLoading(this);

                    if (queueInfo.getWaitingItem() != null) {
                        String loc = robotHandler.getNavLocation(dist);

                        queueInfo.getWaitingItem().setRoomCode(dist);
                        queueInfo.getWaitingItem().setRoomPosition(loc);
                    }

                    setContent(queueInfo);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (queueInfo == null) {

            RobotApi.queueInfo(this, 1, code, new RobotApi.MyResponseListener<QueueInfo>() {
                @Override
                public void onSuccess(QueueInfo d) {
                    DialogUtil.dismessLoading(ProgressActivity.this);
                    setContent(d);
                }

                @Override
                public void onError(String message, String code) {

                    DialogUtil.dismessLoading(ProgressActivity.this);
                    runUiThread(() -> {
                        showShortToast("数据查询失败：" + message);

                        robotHandler.doSpeek("很抱歉，没有查询到该报告");
                    });

                    finish();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        robotHandler.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        DialogUtil.dismessLoading(this);

        robotHandler.onPause();
    }

    @Override
    public void onDragBottom(boolean rightToLeft) {
        return;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DialogUtil.onDestory(this);
        instance = null;
    }
}