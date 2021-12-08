package com.fntj.app.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.fntj.app.R;
import com.fntj.lib.zb.base.BaseActivity;

public class AppKeyActivity extends BaseActivity {

    private static final String TAG = "AppKeyActivity";

    public static Intent createIntent(Context context) {
        return new Intent(context, AppKeyActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intent = getIntent();

        initView();

        initData();

        initEvent();
    }

    @Override
    public void initView() {

        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题

        setContentView(R.layout.activity_appkey);
    }

    @Override
    public void initData() {

    }

    @Override
    public void initEvent() {

    }

    public void saveClick(View view) {

    }
}
