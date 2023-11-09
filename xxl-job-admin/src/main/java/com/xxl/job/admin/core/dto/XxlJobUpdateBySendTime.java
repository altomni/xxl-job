package com.xxl.job.admin.core.dto;

import java.time.Instant;

public class XxlJobUpdateBySendTime {

    //修改的标识
    private Integer xxlJobId;

    //入参的json中
    private Object reminderConfig;

    //入参的json中
    private String sendTime;

    private String cron;

    private Integer triggerStatus = 1;

    public XxlJobUpdateBySendTime(Integer xxlJobId, Object reminderConfig, String sendTime, String cron) {
        this.xxlJobId = xxlJobId;
        this.reminderConfig = reminderConfig;
        this.sendTime = sendTime;
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

    public Object getReminderConfig() {
        return reminderConfig;
    }

    public void setReminderConfig(Object reminderConfig) {
        this.reminderConfig = reminderConfig;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
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
                ", reminderConfig=" + reminderConfig +
                ", sendTime=" + sendTime +
                ", cron='" + cron + '\'' +
                '}';
    }
}
