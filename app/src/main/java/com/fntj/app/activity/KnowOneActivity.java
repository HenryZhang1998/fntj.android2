package com.fntj.app.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.fntj.app.R;
import com.fntj.app.adapter.KnowOneAdapter;
import com.fntj.app.fragment.SearchDialogFragment;
import com.fntj.app.model.KnowledgePackage;
import com.fntj.app.model.KnowledgePackageDetailItem;
import com.fntj.app.net.NetworkUtil;
import com.fntj.app.net.RobotApi;
import com.fntj.app.util.DialogUtil;
import com.fntj.lib.zb.base.BaseListActivity;
import com.fntj.lib.zb.interfaces.AdapterCallBack;
import com.fntj.lib.zb.interfaces.OnBottomDragListener;
import com.fntj.lib.zb.util.CommonUtil;

import java.util.List;


public class KnowOneActivity extends BaseListActivity<KnowledgePackage, GridView, KnowOneAdapter> implements OnBottomDragListener {

    //public static final String INTENT_RANGE = "INTENT_RANGE";
    public static final String RESULT_CLICKED_ITEM = "RESULT_CLICKED_ITEM";

    private Handler handler = new Handler();

    public static Intent createIntent(Context context) {
        return new Intent(context, KnowOneActivity.class);
    }

    private FragmentManager fragmentManager;
    private SearchDialogFragment searchDialog2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_knowone, this);

        intent = getIntent();

        fragmentManager = this.getSupportFragmentManager();

        initView();

        initData();

        initEvent();

        onRefresh();
    }

    @Override
    public void initView() {//必须在onCreate方法内调用
        super.initView();

        setStatusBarColor(this, R.color.gray_3);

        tvBaseTitle.setText(getString(R.string.zsk));
    }

    @Override
    public void initData() {
        //必须在onCreate方法内调用
        super.initData();
    }

    @Override
    public void getListAsync(int page) {
        //super.getListAsync(page);
    }


    @Override
    public void loadData(final int page) {

        DialogUtil.showLoading(this, getResources().getString(R.string.loading));

        RobotApi.getKnowledgePackageList(this, new RobotApi.MyListResponseListener<KnowledgePackage>() {
            @Override
            public void onSuccess(final List<KnowledgePackage> list) {

                DialogUtil.dismessLoading(KnowOneActivity.this);

                for (int i = 0; i < list.size(); i++) {
                    list.get(i).setI(i + 1);
                }

                handler.post(() -> {
                    onLoadSucceed(page, list);
                });
            }

            @Override
            public void onError(String message, String code) {

                DialogUtil.dismessLoading(KnowOneActivity.this);

                runUiThread(() -> {
                    CommonUtil.showShortToast(context, message);
                });

                finish();
            }
        });
    }

    @Override
    public void setList(final List<KnowledgePackage> list) {

        AdapterCallBack adapterHandler = new AdapterCallBack<KnowOneAdapter>() {
            @Override
            public void refreshAdapter() {
                adapter.refresh(list);
            }

            @Override
            public KnowOneAdapter createAdapter() {
                KnowOneAdapter adapter = new KnowOneAdapter(context, R.layout.one_grid_item);
                return adapter;
            }
        };

        setList(adapterHandler);
    }

    @Override
    public void initEvent() {
        super.initEvent();

        lvBaseList.setOnItemClickListener(this);
        lvBaseList.setOnItemLongClickListener(this);
    }

    @Override
    public void onDragBottom(boolean rightToLeft) {
        if (rightToLeft) {
            return;
        }

        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (!NetworkUtil.isNetworkAvailable(context)) {
            Toast.makeText(context, "网络不可用，请检查网络", Toast.LENGTH_SHORT).show();
        }

        KnowledgePackage pkg = adapter.getItem(position);

        //String name = pkg.getName();

        Intent _intent = KnowTwoActivity.createIntent(context, pkg);

        toActivity(_intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        //showShortToast("长按了 " + adapter.getItem(position).getName());

        return true;
    }

    public void onSearchClick(View v) {

        if (searchDialog2 == null) {

            searchDialog2 = new SearchDialogFragment.Builder().build();

            searchDialog2.setDialogButtonListener(new SearchDialogFragment.DialogButtonClickListener() {
                @Override
                public void leftOnClick(SearchDialogFragment dialog) {
                    searchDialog2.dismiss();
                }

                @Override
                public void rightOnClick(SearchDialogFragment dialog) {

                    String keyword = dialog.getKeyword().trim();
                    if (keyword.length() == 0) {
                        CommonUtil.showShortToast(context, "请输入关键词");
                        return;
                    }

                    if (!NetworkUtil.isNetworkAvailable(context)) {
                        CommonUtil.showShortToast(context, "网络不可用，请检查网络");
                        return;
                    }

                    //隐藏输入法
                    hideInput(searchDialog2.getView());

                    DialogUtil.showLoading(KnowOneActivity.this, "正在查询，请稍候...");

                    searchList(keyword);

                    searchDialog2.dismiss();
                }
            });
        }

        searchDialog2.show(fragmentManager);
    }

    private void hideInput(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void searchList(final String keyword) {

        RobotApi.getKnowledgePackageItemList(this, keyword, new RobotApi.MyListResponseListener<KnowledgePackageDetailItem>() {
            @Override
            public void onSuccess(List<KnowledgePackageDetailItem> list) {

                DialogUtil.dismessLoading(KnowOneActivity.this);

                if (list == null || list.size() == 0) {

                    runUiThread(() -> {
                        CommonUtil.showShortToast(context, "查询没有结果！");
                    });

                    return;
                }

                final List<KnowledgePackageDetailItem> _list = list;

                for (int i = 0; i < _list.size(); i++) {
                    _list.get(i).setI(i);
                }

                try {
                    Intent _intent = KnowTwoActivity.createIntent(context, keyword, _list);
                    toActivity(_intent);
                    //startActivity(_intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onError(String message, String code) {

                DialogUtil.dismessLoading(KnowOneActivity.this);

                runUiThread(() -> {
                    CommonUtil.showShortToast(context, "查询失败：" + message);
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DialogUtil.onDestory(this);
    }
}
