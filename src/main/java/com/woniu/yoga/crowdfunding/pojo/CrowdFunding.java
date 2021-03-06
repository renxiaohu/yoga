package com.woniu.yoga.crowdfunding.pojo;

import com.woniu.yoga.user.pojo.User;

import java.math.BigDecimal;
import java.util.List;

public class CrowdFunding {
    private Integer cfId;

    private String cfTitle;

    private String cfImg;

    private Long cfSupCount;

    private String cfDescribe;

    private Integer cfType;

    private Long cfFocCount;

    private User cfSponsor;

    public List<Supporter> getSupporters() {
        return supporters;
    }

    public void setSupporters(List<Supporter> supporters) {
        this.supporters = supporters;
    }

    private List<Supporter> supporters;

    private BigDecimal cfTargetMoney;

    private BigDecimal cfCurMoney;

    private Integer cfStatus;

    private Integer cfLight;

    private Integer cfFlag;

    public Integer getCfId() {
        return cfId;
    }

    public void setCfId(Integer cfId) {
        this.cfId = cfId;
    }

    public String getCfTitle() {
        return cfTitle;
    }

    public void setCfTitle(String cfTitle) {
        this.cfTitle = cfTitle;
    }

    public String getCfImg() {
        return cfImg;
    }

    public void setCfImg(String cfImg) {
        this.cfImg = cfImg;
    }

    public Long getCfSupCount() {
        return cfSupCount;
    }

    public void setCfSupCount(Long cfSupCount) {
        this.cfSupCount = cfSupCount;
    }

    public String getCfDescribe() {
        return cfDescribe;
    }

    public void setCfDescribe(String cfDescribe) {
        this.cfDescribe = cfDescribe;
    }

    public Integer getCfType() {
        return cfType;
    }

    public void setCfType(Integer cfType) {
        this.cfType = cfType;
    }

    public Long getCfFocCount() {
        return cfFocCount;
    }

    public void setCfFocCount(Long cfFocCount) {
        this.cfFocCount = cfFocCount;
    }

    public BigDecimal getCfTargetMoney() {
        return cfTargetMoney;
    }

    public void setCfTargetMoney(BigDecimal cfTargetMoney) {
        this.cfTargetMoney = cfTargetMoney;
    }

    public BigDecimal getCfCurMoney() {
        return cfCurMoney;
    }

    public void setCfCurMoney(BigDecimal cfCurMoney) {
        this.cfCurMoney = cfCurMoney;
    }

    public Integer getCfStatus() {
        return cfStatus;
    }

    public void setCfStatus(Integer cfStatus) {
        this.cfStatus = cfStatus;
    }

    public Integer getCfLight() {
        return cfLight;
    }

    public void setCfLight(Integer cfLight) {
        this.cfLight = cfLight;
    }

    public Integer getCfFlag() {
        return cfFlag;
    }

    public void setCfFlag(Integer cfFlag) {
        this.cfFlag = cfFlag;
    }

    public User getCfSponsor() {
        return cfSponsor;
    }

    public void setCfSponsor(User cfSponsor) {
        this.cfSponsor = cfSponsor;
    }

    @Override
    public String toString() {
        return "CrowdFunding{" +
                "cfId=" + cfId +
                ", cfTitle='" + cfTitle + '\'' +
                ", cfImg='" + cfImg + '\'' +
                ", cfSupCount=" + cfSupCount +
                ", cfDescribe='" + cfDescribe + '\'' +
                ", cfType=" + cfType +
                ", cfFocCount=" + cfFocCount +
                ", cfSponsor=" + cfSponsor +
                ", supporters=" + supporters +
                ", cfTargetMoney=" + cfTargetMoney +
                ", cfCurMoney=" + cfCurMoney +
                ", cfStatus=" + cfStatus +
                ", cfLight=" + cfLight +
                ", cfFlag=" + cfFlag +
                '}';
    }
}