package com.gennlife.rws.entity;

import java.util.Date;

/**
 * @author liumingxin
 * @create 2018 16 15:06
 * @desc
 **/
public class ContrastiveAnalysisActive {
    private Integer id;
    private String activeIndexId;
    private String projectId;
    private String createId;
    private Date createTime;
    private String updateId;
    private Date updateTime;
    private Integer cortType;

    public Integer getCortType() {
        return cortType;
    }

    public void setCortType(Integer cortType) {
        this.cortType = cortType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getActiveIndexId() {
        return activeIndexId;
    }

    public void setActiveIndexId(String activeIndexId) {
        this.activeIndexId = activeIndexId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getCreateId() {
        return createId;
    }

    public void setCreateId(String createId) {
        this.createId = createId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateId() {
        return updateId;
    }

    public void setUpdateId(String updateId) {
        this.updateId = updateId;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
