package com.fntj.app.model;

import java.io.Serializable;
import java.util.List;

public class WaitingItem  implements Serializable {

    private String recordId;
    private String departmentArchivedId;//": "8adc8c86657f2e8001658460c9e601d0",
    private String header;//": "4楼采血",
    private String title;//": "采血",
    private String subTitle;//": "请稍等",
    private String state;//": "0",
    private String illustration;//": "您的排队号2号，当前检查到1号",
    private String roomCode;//": "421",
    private String roomPosition;//": "201,223",
    private String overWarning;//": "",
    private String sense;//": "初步筛查肝脏肿瘤 女性卵巢癌的早期诊断 血液病、急性和慢性感染、寄生虫病、组织损伤等疾病的协助诊断及鉴别诊断 主要反应胃的分泌功能，通过全自动酶免仪检测，胃癌、萎缩性胃炎、HP阳性者等均有较大的变化 包括肝功、肾功、血脂、血糖、心肌酶等共28项检查 主要初步筛查消化道良性或恶性肿瘤 通过酶免的检测方法，检测人血清中食物过敏原特异性IGg抗体，不耐受人员会表现为胃肠道等多系统不适症状 乳腺癌的早期诊断 胰腺癌和胆管癌、直肠癌等早期诊断 ",
    private String tips;//": " 请空腹",
    private List<WaitingItemItem> items;//

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomPosition() {
        return roomPosition;
    }

    public void setRoomPosition(String roomPosition) {
        this.roomPosition = roomPosition;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getDepartmentArchivedId() {
        return departmentArchivedId;
    }

    public void setDepartmentArchivedId(String departmentArchivedId) {
        this.departmentArchivedId = departmentArchivedId;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getIllustration() {
        return illustration;
    }

    public void setIllustration(String illustration) {
        this.illustration = illustration;
    }

    public String getOverWarning() {
        return overWarning;
    }

    public void setOverWarning(String overWarning) {
        this.overWarning = overWarning;
    }

    public String getSense() {
        return sense;
    }

    public void setSense(String sense) {
        this.sense = sense;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public List<WaitingItemItem> getItems() {
        return items;
    }

    public void setItems(List<WaitingItemItem> items) {
        this.items = items;
    }
}
