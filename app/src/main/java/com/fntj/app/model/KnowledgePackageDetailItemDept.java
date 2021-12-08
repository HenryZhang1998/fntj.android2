package com.fntj.app.model;

import java.io.Serializable;

public class KnowledgePackageDetailItemDept implements Serializable {
    private String id;//": "60",
    private String name;//": "彩超室1",
    private String introduction;//": ""

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
}
