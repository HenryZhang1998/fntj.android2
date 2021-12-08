package com.fntj.app.model;

import java.io.Serializable;

public class HoldItem  implements Serializable {
    private String name;//": "放射科(数字化X线)",
    private String tip;//": "",
    private String items;//": "数字化X线胸部正侧位片",
    private String sense;//": "",
    private String doneTime;//": ""

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public String getSense() {
        return sense;
    }

    public void setSense(String sense) {
        this.sense = sense;
    }

    public String getDoneTime() {
        return doneTime;
    }

    public void setDoneTime(String doneTime) {
        this.doneTime = doneTime;
    }
}
