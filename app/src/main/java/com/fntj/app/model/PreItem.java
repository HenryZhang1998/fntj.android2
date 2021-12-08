package com.fntj.app.model;

import java.io.Serializable;
import java.util.List;

public class PreItem  implements Serializable {

    private String departmentArchivedId;//": "8adc8c86657f2e8001658469c2170209",
    private String header;//": "4楼7B 彩超",
    private String title;//": "7B 彩超",
    private String subTitle;//": "预排号",
    private String state;//": "6",
    private String illustration;//": "1A",
    private String overWarning;//": "",
    private String sense;//": "较直观了解大小形态等结构，对甲状腺肿、甲亢、滤泡增生、腺瘤等临床常见疾病的的检查，结合多普勒对其疾病良恶性进行初筛 对于乳腺囊肿、纤维瘤、增生结节等常见疾病的诊断比较精准，而且简单，检查面广，对腋窝淋巴结及血管可观察，尤其对乳腺微小占位性病变检出率高 通过彩色多普勒血流频谱技术检测血液流量、方向，从而辨别脏器（肝、胆、胰、脾、双肾、双输尿管、膀胱、子宫、卵巢)的受损性质和程度，腹腔内实质性脏器炎症及占位性病变的筛查。",
    private String tips;//": " ★注:此为空腹项目,未完成该检查前禁食禁饮(除白开水外）；并请憋尿",
    private List<PreItemItem> items;//": [{

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

    public List<PreItemItem> getItems() {
        return items;
    }

    public void setItems(List<PreItemItem> items) {
        this.items = items;
    }
}
