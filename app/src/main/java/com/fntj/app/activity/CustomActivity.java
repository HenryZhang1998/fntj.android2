package com.fntj.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.fntj.app.R;
import com.fntj.app.util.StatusBarUtils;
import com.fntj.lib.zxing.CameraScan;
import com.fntj.lib.zxing.ViewfinderView;
import com.google.zxing.Result;

//import androidx.camera.view.PreviewView;

/**
 * 自定义扫码：当直接使用CaptureActivity
 * 自定义扫码，切记自定义扫码需在{@link Activity}或者{@link Fragment}相对应的生命周期里面调用{@link #mCameraScan}对应的生命周期
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class CustomActivity extends AppCompatActivity implements CameraScan.OnScanResultCallback {

    private boolean isContinuousScan;

    private CameraScan mCameraScan;

//    private PreviewView previewView;

    private ViewfinderView viewfinderView;

    private View ivFlash;

    private Toast toast;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.custom_activity);

        initUI();
    }

    private void initUI(){

        Toolbar toolbar = findViewById(R.id.toolbar);
        StatusBarUtils.immersiveStatusBar(this,toolbar,0.2f);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(getIntent().getStringExtra(CaptureFragmentActivity.KEY_TITLE));


//        previewView = findViewById(R.id.previewView);
        viewfinderView = findViewById(R.id.viewfinderView);
        ivFlash = findViewById(R.id.ivFlash);
        ivFlash.setVisibility(View.INVISIBLE);

        isContinuousScan = getIntent().getBooleanExtra(CaptureFragmentActivity.KEY_IS_CONTINUOUS,false);

//        mCameraScan = new DefaultCameraScan(this,previewView);
        mCameraScan.setOnScanResultCallback(this)
                .setVibrate(true)
                .startCamera();

    }

    @Override
    protected void onDestroy() {
        mCameraScan.release();
        super.onDestroy();
    }

    /**
     * 扫码结果回调
     * @param result 扫码结果
     * @return
     */
    @Override
    public boolean onScanResultCallback(Result result) {
        if(isContinuousScan){
            showToast(result.getText());
        }
        //如果需支持连扫，返回true即可
        return isContinuousScan;
    }

    private void showToast(String text){
        if(toast == null){
            toast = Toast.makeText(this,text,Toast.LENGTH_SHORT);
        }else{
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setText(text);
        }
        toast.show();
    }



    public void onClick(View v){
        switch (v.getId()){
            case R.id.ivLeft:
                onBackPressed();
                break;
        }
    }
}