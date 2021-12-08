/*
 * Copyright (C) 2018 Jenly Yu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fntj.app.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.fntj.app.R;
import com.fntj.app.util.StatusBarUtils;
import com.fntj.lib.zxing.CaptureActivity;

/**
 * @author Jenly <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class EasyCaptureActivity extends CaptureActivity {


    @Override
    public int getLayoutId() {
        return R.layout.easy_capture_activity;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        StatusBarUtils.immersiveStatusBar(this,toolbar,0.2f);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(getIntent().getStringExtra(CaptureFragmentActivity.KEY_TITLE));

    }

    @Override
    public void initCameraScan() {
        super.initCameraScan();
        getCameraScan()
                .setPlayBeep(true)
                .setVibrate(true);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.ivLeft:
                onBackPressed();
                break;
        }
    }
}
