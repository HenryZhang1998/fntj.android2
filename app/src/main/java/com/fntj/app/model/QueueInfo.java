package com.fntj.app.model;

import java.io.Serializable;
import java.util.List;

public class QueueInfo  implements Serializable {

    private String pebillArchiveId;//": "8873292",
    private String userArchivedId;//": "",
    private String userName;//": "郝思颖",
    private String gender;//": "2",
    private String lastDepartmentArchiveId;//": "",
    private String lastDepartmentName;//": "",

    private List<HoldItem> holdItems;

    private List<FinishedItem> finishedItems;

    private WaitingItem waitingItem;

    private List<PreItem> preItem;

    public String getPebillArchiveId() {
        return pebillArchiveId;
    }

    public void setPebillArchiveId(String pebillArchiveId) {
        this.pebillArchiveId = pebillArchiveId;
    }

    public String getUserArchivedId() {
        return userArchivedId;
    }

    public void setUserArchivedId(String userArchivedId) {
        this.userArchivedId = userArchivedId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLastDepartmentArchiveId() {
        return lastDepartmentArchiveId;
    }

    public void setLastDepartmentArchiveId(String lastDepartmentArchiveId) {
        this.lastDepartmentArchiveId = lastDepartmentArchiveId;
    }

    public String getLastDepartmentName() {
        return lastDepartmentName;
    }

    public void setLastDepartmentName(String lastDepartmentName) {
        this.lastDepartmentName = lastDepartmentName;
    }

    public List<HoldItem> getHoldItems() {
        return holdItems;
    }

    public void setHoldItems(List<HoldItem> holdItems) {
        this.holdItems = holdItems;
    }

    public List<FinishedItem> getFinishedItems() {
        return finishedItems;
    }

    public void setFinishedItems(List<FinishedItem> finishedItems) {
        this.finishedItems = finishedItems;
    }

    public WaitingItem getWaitingItem() {
        return waitingItem;
    }

    public void setWaitingItem(WaitingItem waitingItem) {
        this.waitingItem = waitingItem;
    }

    public List<PreItem> getPreItem() {
        return preItem;
    }

    public void setPreItem(List<PreItem> preItem) {
        this.preItem = preItem;
    }
}
