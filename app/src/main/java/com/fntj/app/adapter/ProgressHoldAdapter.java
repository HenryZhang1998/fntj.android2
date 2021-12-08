package com.fntj.app.adapter;

import android.app.Activity;
import android.view.ViewGroup;

import com.fntj.app.model.HoldItem;
import com.fntj.app.view.ProgressHoldView;
import com.fntj.lib.zb.base.BaseAdapter;

public class ProgressHoldAdapter extends BaseAdapter<HoldItem, ProgressHoldView> {

    public ProgressHoldAdapter(Activity context) {
        super(context);
    }

    @Override
    public ProgressHoldView createView(int position, ViewGroup parent) {
        return new ProgressHoldView(context);
    }

    @Override
    public long getItemId(int position) {
        return 0;
//        try {
//            return getItem(position).getI("id");
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return 0;
//        }
    }
}
