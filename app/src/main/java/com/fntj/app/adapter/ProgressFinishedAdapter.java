package com.fntj.app.adapter;

import android.app.Activity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fntj.app.model.FinishedItem;
import com.fntj.app.view.ProgressFinishedView;

import java.util.ArrayList;
import java.util.List;


public class ProgressFinishedAdapter extends RecyclerView.Adapter<ProgressFinishedView> { //implements ListAdapter, AdapterViewPresenter<ProgressFinishedView> //extends BaseAdapter<FinishedItem, ProgressFinishedView> {

    private Activity context;
    private List<FinishedItem> list = new ArrayList<>();

    public ProgressFinishedAdapter(Activity context) {
        super();
        this.context = context;
    }

    @NonNull
    @Override
    public ProgressFinishedView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ProgressFinishedView view = new ProgressFinishedView(context, parent, viewType);
        view.initView();

        return view;
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressFinishedView holder, int position) {
        FinishedItem item = getItem(position);

        holder.bindView(item, position, getItemViewType(position));
    }

    public FinishedItem getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public synchronized void refresh(List<FinishedItem> list) {
        this.list = list == null ? null : new ArrayList<>(list);

        notifyDataSetChanged(); //仅对 AbsListView 有效
    }
}
