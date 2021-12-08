package com.fntj.app.model;

import java.io.Serializable;

public class KnowledgePackage implements Serializable {

    private long i;
    private String id; //": "402880ea777a8fd801777a93668c0004",
    private String name; //": "甲状腺专项检查",
    private String description; //": "在健康筛查的基础上，侧重肝肾功能、血脂血糖、肿瘤等检查，提供准确的早期预警，让您更好的了解目前身体状况，及早发现潜在的危害健康危险因素。",
    private String tag; //": "甲状腺",
    private Integer state; //": 1,
    private Integer sort; //": 1

    public long getI() {
        return i;
    }

    public void setI(long i) {
        this.i = i;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }
}
