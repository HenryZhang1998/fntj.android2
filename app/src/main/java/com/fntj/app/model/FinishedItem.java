package com.fntj.app.model;

import java.io.Serializable;

public class FinishedItem  implements Serializable {
    private String name;//": "心电图",
    private String tip;//": "",
    private String items;//": "心电图",
    private String sense;//": "",
    private String doneTime;//": "09:28:00"

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
