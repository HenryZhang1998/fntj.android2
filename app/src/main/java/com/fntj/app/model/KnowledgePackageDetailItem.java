package com.fntj.app.model;

import java.io.Serializable;

public class KnowledgePackageDetailItem implements Serializable {

    private long i;

    private String id;//": "44",
    private String name;//": "心脏彩超",
    private Integer sex;//": 0,
    private Integer mealFlag;//": 0,
    private Integer checkpointNum;//": 0,
    private String tips;//": "",
    private String femalTips;//": "",
    private String sense;//": "直观了解心脏的结构，是筛查心脏形态异常的首选方法，通过心脏结构及血流探查、心功能测定等检查心脏病变，它是实时性与血流显示超过了CT、MRI",
    private String backColor;//": "",
    private Integer price;//": 275,
    private KnowledgePackageDetailItemDept phDept;

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

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getMealFlag() {
        return mealFlag;
    }

    public void setMealFlag(Integer mealFlag) {
        this.mealFlag = mealFlag;
    }

    public Integer getCheckpointNum() {
        return checkpointNum;
    }

    public void setCheckpointNum(Integer checkpointNum) {
        this.checkpointNum = checkpointNum;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public String getFemalTips() {
        return femalTips;
    }

    public void setFemalTips(String femalTips) {
        this.femalTips = femalTips;
    }

    public String getSense() {
        return sense;
    }

    public void setSense(String sense) {
        this.sense = sense;
    }

    public String getBackColor() {
        return backColor;
    }

    public void setBackColor(String backColor) {
        this.backColor = backColor;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public KnowledgePackageDetailItemDept getPhDept() {
        return phDept;
    }

    public void setPhDept(KnowledgePackageDetailItemDept phDept) {
        this.phDept = phDept;
    }
}
