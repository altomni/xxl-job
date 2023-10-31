package com.xxl.job.admin.core.model;

/**
 * @author EDY
 */
public class XxlExtendJobInfo extends XxlJobInfo{

    //一般是参考id+userId
    private String onlyId;

    public String getOnlyId() {
        return onlyId;
    }

    public void setOnlyId(String onlyId) {
        this.onlyId = onlyId;
    }

}
