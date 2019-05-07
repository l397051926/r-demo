package com.gennlife.rws.entity;

import java.util.Date;

public class ProjectUserMap {
	
    private Integer id;

    private String projectId;

    private String uid;
    // 职责人编码
    private String obligId;
    // 职责人名称 对照表中存储
    private String obligName;
    //创建时间
    private Date createTime;

    public String getObligId() {
        return obligId;
    }

    public void setObligId(String obligId) {
        this.obligId = obligId;
    }

    public String getObligName() {
        return obligName;
    }

    public void setObligName(String obligName) {
        this.obligName = obligName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId == null ? null : projectId.trim();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? null : uid.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}