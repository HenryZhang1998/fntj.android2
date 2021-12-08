package com.fntj.app.listener;

import android.view.View;

import com.fntj.app.handler.SpeekHandler;
import com.fntj.app.model.QueueInfo;
import com.fntj.lib.zb.base.BaseActivity;
import com.fntj.lib.zb.util.StringUtil;

public class TvNextClickListener implements View.OnClickListener {

    private QueueInfo data = null;
    private BaseActivity context = null;
    private SpeekHandler speekHandler;

    public TvNextClickListener(BaseActivity context, SpeekHandler speekHandler){
        this.context = context;
        this.speekHandler = speekHandler;
    }

    public QueueInfo getData() {
        return data;
    }

    public void setData(QueueInfo data) {
        this.data = data;
    }

    @Override
    public void onClick(View v) {
        if (data == null || data.getWaitingItem() == null) {
            context.showShortToast("暂无项目内容");
            return;
        }

        String speekText = String.format("%s 您好，您的下一个体检项目是：%s"
                , data.getUserName()
                , data.getWaitingItem().getTitle());

        if (!StringUtil.isEmpty(data.getWaitingItem().getRoomCode())) {
            speekText += String.format("，请到%s诊室就诊", data.getWaitingItem().getRoomCode());
        }

        context.showShortToast(speekText);

        if(speekHandler != null) {
            speekHandler.doSpeek(speekText);
        }
    }
}
