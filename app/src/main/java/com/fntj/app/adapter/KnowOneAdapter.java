package com.fntj.app.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fntj.app.R;
import com.fntj.app.model.KnowledgePackage;
import com.fntj.lib.zb.util.StringUtil;

import java.util.List;

public class KnowOneAdapter extends BaseAdapter {

    private static final String TAG = "KnowOneAdapter";

    private final Activity context;
    private final LayoutInflater inflater;
    private int layoutRes;//item视图资源

    public KnowOneAdapter(Activity context) {
        this(context, com.fntj.lib.R.layout.grid_item);
    }

    public KnowOneAdapter(Activity context, int layoutRes) {
        this.context = context;
        this.inflater = context.getLayoutInflater();

        setLayoutRes(layoutRes);
    }


    public void setLayoutRes(int layoutRes) {
        this.layoutRes = layoutRes;
    }

    private List<KnowledgePackage> list;

    /**
     * 刷新列表
     *
     * @param list
     */
    public synchronized void refresh(List<KnowledgePackage> list) {
        if (list != null && list.size() > 0) {
            initList(list);
        }

        notifyDataSetChanged();
    }

    /**
     * 标记List<String>中的值是否已被选中。
     * 不需要可以删除，但“this.list = list;”这句
     * 要放到constructor【这个adapter只有ModleAdapter(Context context, List<Object> list)这一个constructor】里去
     *
     * @param list
     * @return
     */
    @SuppressLint("UseSparseArrays")
    private void initList(List<KnowledgePackage> list) {
        this.list = list;
    }


    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public KnowledgePackage getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getI();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        KnowOneAdapter.ViewHolder holder = convertView == null ? null : (KnowOneAdapter.ViewHolder) convertView.getTag();

        if (holder == null) {
            convertView = inflater.inflate(layoutRes, parent, false);

            holder = new KnowOneAdapter.ViewHolder();

            holder.layout = convertView.findViewById(R.id.layoutItem);
            holder.ivImage = convertView.findViewById(R.id.ivImage);
            holder.tvName = convertView.findViewById(R.id.tvGridItemName);
            holder.item_name_layout = convertView.findViewById(R.id.item_name_layout);

            holder.layout.getBackground().setAlpha(50);
            holder.item_name_layout.getBackground().setAlpha(70);

            //View v = findViewById(R.id.content);//找到你要设透明背景的layout 的id
            //v.getBackground().setAlpha(100);//0~255透明度值 ，0为完全透明，255为不透明

            convertView.setTag(holder);
        }

        final KnowledgePackage item = getItem(position);
        final String name = item.getName();

        long i = item.getI();
        if(i > 17){
            i = i - 17;
        }

        String imgname = (i < 10L) ? "ic_0" + i : "ic_" + i;

        int imgid = context.getResources().getIdentifier(imgname, "drawable", context.getPackageName());

        Glide.with(context).load(imgid).into(holder.ivImage);

        holder.tvName.setText(StringUtil.getTrimedString(name));

        return convertView;
    }

    static class ViewHolder {
        public LinearLayout layout;
        public ImageView ivImage;
        public TextView tvName;
        public LinearLayout item_name_layout;
    }


}

