package com.fntj.app.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.fntj.app.R;
import com.fntj.app.adapter.KnowTwoAdapter;
import com.fntj.app.fragment.SearchDialogFragment;
import com.fntj.app.model.KnowledgePackage;
import com.fntj.app.model.KnowledgePackageDetail;
import com.fntj.app.model.KnowledgePackageDetailItem;
import com.fntj.app.net.NetworkUtil;
import com.fntj.app.net.RobotApi;
import com.fntj.app.util.DialogUtil;
import com.fntj.lib.zb.base.BaseListActivity;
import com.fntj.lib.zb.interfaces.AdapterCallBack;
import com.fntj.lib.zb.interfaces.OnBottomDragListener;
import com.fntj.lib.zb.util.CommonUtil;
import com.fntj.lib.zb.util.StringUtil;

import java.util.List;

public class KnowTwoActivity extends BaseListActivity<KnowledgePackageDetailItem, GridView, KnowTwoAdapter> implements OnBottomDragListener {

    //public static final String INTENT_RANGE = "INTENT_RANGE";
    public static final String RESULT_CLICKED_ITEM = "RESULT_CLICKED_ITEM";

    private Handler handler = new Handler();

    private String packageId = "";
    private String name = "";

    private int type;
    private List<KnowledgePackageDetailItem> _list;

    private SearchDialogFragment searchDialog2;

    public static Intent createIntent(Context context, KnowledgePackage pkg) {
        Intent intent = new Intent(context, KnowTwoActivity.class);

        intent.putExtra("type", 1);
        intent.putExtra("pkg", pkg);

        return intent;
    }

    public static Intent createIntent(Context context, String keyword, List<KnowledgePackageDetailItem> list) {

        Intent intent = new Intent(context, KnowTwoActivity.class);

        KnowledgePackageDetail pkg = new KnowledgePackageDetail();
        pkg.setName(keyword);
        pkg.setItemList(list);

        intent.putExtra("type", 2);
        intent.putExtra("pkg", pkg);

        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_knowtwo, this);

        intent = getIntent();

        type = intent.getIntExtra("type", 1);
        if (type == 1) {
            KnowledgePackage pkg = (KnowledgePackage) intent.getSerializableExtra("pkg");
            packageId = pkg.getId();
            name = pkg.getName();
        }

        if (type == 2) {
            KnowledgePackageDetail pkg = (KnowledgePackageDetail) intent.getSerializableExtra("pkg");
            name = pkg.getName();
            _list = pkg.getItemList();
        }

        initView();

        initData();

        initEvent();

        if (type == 1) {
            onRefresh();
        }

        if (type == 2) {
            for (int i = 0; i < _list.size(); i++) {
                _list.get(i).setI(i + 1);
            }

            setList(_list);
        }
    }

    @Override
    public void initView() {//必须在onCreate方法内调用
        super.initView();

        setStatusBarColor(this, R.color.gray_3);

        tvBaseTitle.setText(getString(R.string.zsk) + "：" + name);
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

        if (type == 1) {

            DialogUtil.showLoading(KnowTwoActivity.this);

            RobotApi.getKnowledgePackageDetail(this, packageId, new RobotApi.MyResponseListener<KnowledgePackageDetail>() {
                @Override
                public void onSuccess(KnowledgePackageDetail data) {

                    DialogUtil.dismessLoading(KnowTwoActivity.this);

                    List<KnowledgePackageDetailItem> list = data.getItemList();

                    for (int i = 0; i < list.size(); i++) {
                        list.get(i).setI(i + 1);
                    }

                    handler.post(() -> {
                        onLoadSucceed(page, list);
                    });
                }

                @Override
                public void onError(String message, String code) {

                    DialogUtil.dismessLoading(KnowTwoActivity.this);

                    runUiThread(() -> {
                        CommonUtil.showShortToast(context, message);
                    });
                }
            });
        }
    }

    @Override
    public void setList(final List<KnowledgePackageDetailItem> list) {

        AdapterCallBack adapterHandler = new AdapterCallBack<KnowTwoAdapter>() {
            @Override
            public void refreshAdapter() {
                adapter.refresh(list);
            }

            @Override
            public KnowTwoAdapter createAdapter() {
                KnowTwoAdapter adapter = new KnowTwoAdapter(context, R.layout.one_grid_item);
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

    private String getString(String input, String def) {

        if (StringUtil.isEmpty(input))
            return def;

        return input;
    }

    private AlertDialog detailDialog;
    private AlertDialog searchDialog;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        KnowledgePackageDetailItem item = adapter.getItem(position);

        String name = item.getName();

        //showShortToast("点击了 " + name);

        View dialogView = View.inflate(context, R.layout.dialog_itemdetail, null);

        TextView tvdept = dialogView.findViewById(R.id.tvdept);
        TextView tvsense = dialogView.findViewById(R.id.tvsense);
        TextView tvGender = dialogView.findViewById(R.id.tvGender);
        TextView tvmeal = dialogView.findViewById(R.id.tvmeal);
        TextView tvtips = dialogView.findViewById(R.id.tvtips);
        TextView tvfemalTips = dialogView.findViewById(R.id.tvfemalTips);
        TextView tvprice = dialogView.findViewById(R.id.tvprice);

        tvdept.setText(getString(item.getPhDept() == null ? "--" : item.getPhDept().getName(), "--"));
        tvsense.setText(getString(item.getSense(), "--"));
        tvGender.setText(item.getSex() == 0 ? "不限" : (item.getSex() == 1 ? "限男" : "限女"));
        tvmeal.setText(item.getMealFlag() == 0 ? "不限" : (item.getSex() == 1 ? "餐前" : "餐后"));
        tvtips.setText(getString(item.getTips(), "--"));
        tvfemalTips.setText(getString(item.getFemalTips(), "--"));
        tvprice.setText(String.valueOf(item.getPrice() == null ? 0 : item.getPrice()));


        detailDialog = new AlertDialog.Builder(context)
                .setTitle(name)
                .setIcon(R.drawable.star_light)
                .setView(dialogView)
                .setPositiveButton("关闭", (dialogInterface, i) -> {
                    detailDialog.dismiss();
                    detailDialog = null;
                }).show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
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
                        Toast.makeText(context, "网络不可用，请检查网络", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //隐藏输入法
                    hideInput(searchDialog2.getView());

                    DialogUtil.showLoading(KnowTwoActivity.this, "正在查询：" + keyword + "，请稍候...");

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

        runUiThread(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });


        RobotApi.getKnowledgePackageItemList(this, keyword, new RobotApi.MyListResponseListener<KnowledgePackageDetailItem>() {
            @Override
            public void onSuccess(List<KnowledgePackageDetailItem> list) {

                DialogUtil.dismessLoading(KnowTwoActivity.this);

                if (list == null || list.size() == 0) {

                    runUiThread(() -> {
                        CommonUtil.showShortToast(context, "查询没有结果！");
                    });

                    return;
                }

                _list = list;

                for (int i = 0; i < _list.size(); i++) {
                    _list.get(i).setI(i + 1);
                }

                runUiThread(() -> {
                    tvBaseTitle.setText("查询项目：" + keyword);
                    setList(_list);
                });
            }

            @Override
            public void onError(String message, String code) {

                DialogUtil.dismessLoading(KnowTwoActivity.this);

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
