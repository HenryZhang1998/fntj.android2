package com.fntj.app.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.fntj.app.R;
import com.fntj.app.util.StatusBarUtils;
import com.fntj.lib.zxing.CaptureFragment;

/**
 * Fragment扫码
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class CaptureFragmentActivity extends AppCompatActivity {

    public static final String KEY_TITLE = "key_title";
    public static final String KEY_IS_QR_CODE = "key_code";
    public static final String KEY_IS_CONTINUOUS = "key_continuous_scan";

    public static final int REQUEST_CODE_SCAN = 0X01;
    public static final int REQUEST_CODE_PHOTO = 0X02;

    public static final int RC_CAMERA = 0X01;

    public static final int RC_READ_PHOTO = 0X02;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        StatusBarUtils.immersiveStatusBar(this,toolbar,0.2f);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(getIntent().getStringExtra(KEY_TITLE));

        replaceFragment(CaptureFragment.newInstance());
    }

    public void replaceFragment(Fragment fragment){
        replaceFragment( R.id.fragmentContent,fragment);
    }

    public void replaceFragment(@IdRes int id, Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(id, fragment).commit();
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.ivLeft:
                onBackPressed();
                break;
        }
    }
}
