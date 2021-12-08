package com.fntj.app.model;

import java.util.List;

public class KnowledgePackageDetail extends KnowledgePackage{

    private List<KnowledgePackageDetailItem> itemList;

    public List<KnowledgePackageDetailItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<KnowledgePackageDetailItem> itemList) {
        this.itemList = itemList;
    }
}
