package com.fntj.app.model;

import java.io.Serializable;

public class WaitingItemItem  implements Serializable {
    private String name;//": "甲胎蛋白定量",
    private String tip;//": "",
    private String sense;//": "初步筛查肝脏肿瘤"

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

    public String getSense() {
        return sense;
    }

    public void setSense(String sense) {
        this.sense = sense;
    }
}
