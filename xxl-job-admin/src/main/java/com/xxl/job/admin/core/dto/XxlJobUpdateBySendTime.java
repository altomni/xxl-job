package com.xxl.job.admin.core.dto;

public class XxlJobUpdateBySendTime {

    //修改的标识
    private Integer xxlJobId;

    private String cron;

    private Integer triggerStatus = 1;

    private long triggerNextTime;

    public long getTriggerNextTime() {
        return triggerNextTime;
    }

    public void setTriggerNextTime(long triggerNextTime) {
        this.triggerNextTime = triggerNextTime;
    }

    public XxlJobUpdateBySendTime(Integer xxlJobId, String cron) {
        this.xxlJobId = xxlJobId;
        this.cron = cron;
    }

    public XxlJobUpdateBySendTime() {
    }

    public Integer getXxlJobId() {
        return xxlJobId;
    }

    public void setXxlJobId(Integer xxlJobId) {
        this.xxlJobId = xxlJobId;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Integer getTriggerStatus() {
        return triggerStatus;
    }

    public void setTriggerStatus(Integer triggerStatus) {
        this.triggerStatus = triggerStatus;
    }

    @Override
    public String toString() {
        return "XxlJobUpdateBySendTime{" +
                "xxlJobId=" + xxlJobId +
                ", cron='" + cron + '\'' +
                ", triggerStatus=" + triggerStatus +
                '}';
    }
}
