package com.fntj.app.view;

import android.app.Activity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.fntj.app.R;
import com.fntj.app.model.FinishedItem;
import com.fntj.lib.zb.util.StringUtil;

public class ProgressFinishedView extends RecyclerView.ViewHolder {

    private static final String TAG = "ProgressFinishedView";

    public Activity context;

    public ImageView ivHead;
    public TextView tvName;
    public TextView tvsense;
    public TextView tvitems;
    public TextView tvtip;
    public TextView tvdoneTime;

    public ProgressFinishedView(Activity context, ViewGroup parent, int viewType) {
        super(context.getLayoutInflater().inflate(R.layout.progress_detail_view, parent, false));

        context = context;

        //initView();
    }


    public View initView() {

        ivHead = findView(R.id.ivHead);
        tvName = findView(R.id.tvName);

        tvsense = findView(R.id.tvsense);
        tvitems = findView(R.id.tvitems);
        tvtip = findView(R.id.tvtip);
        tvdoneTime = findView(R.id.tvdoneTime);

        return itemView;
    }

    private <V extends View> V findView(int id) {
        return itemView.findViewById(id);
    }

    private String getString(String s) {
        return StringUtil.isEmpty(s) ? "--" : s;
    }

    public void bindView(FinishedItem j, int position, int viewType) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvName.setText(Html.fromHtml("<span style='color:black;font-weight:bold;'>项目名称：</span>" + getString(j.getName()), Html.FROM_HTML_MODE_LEGACY));
            tvsense.setText(Html.fromHtml("<span style='color:black;font-weight:bold;'>检查意义：</span>" + getString(j.getSense()), Html.FROM_HTML_MODE_LEGACY));
            tvitems.setText(Html.fromHtml("<span style='color:black;font-weight:bold;'>检查内容：</span>" + getString(j.getItems()), Html.FROM_HTML_MODE_LEGACY));
            tvtip.setText(Html.fromHtml("<span style='color:black;font-weight:bold;'>注意事项：</span>" + getString(j.getTip()), Html.FROM_HTML_MODE_LEGACY));
            tvdoneTime.setText(Html.fromHtml("<span style='color:black;font-weight:bold;'>完成时间：</span>" + getString(j.getDoneTime()), Html.FROM_HTML_MODE_LEGACY));
        }
        else{

            tvName.setText(Html.fromHtml("<span style='color:black;font-weight:bold;'>项目名称：</span>" + getString(j.getName())));
            tvsense.setText(Html.fromHtml("<span style='color:black;font-weight:bold;'>检查意义：</span>" + getString(j.getSense())));
            tvitems.setText(Html.fromHtml("<span style='color:black;font-weight:bold;'>检查内容：</span>" + getString(j.getItems())));
            tvtip.setText(Html.fromHtml("<span style='color:black;font-weight:bold;'>注意事项：</span>" + getString(j.getTip())));
            tvdoneTime.setText(Html.fromHtml("<span style='color:black;font-weight:bold;'>完成时间：</span>" + getString(j.getDoneTime())));
        }
    }
}
