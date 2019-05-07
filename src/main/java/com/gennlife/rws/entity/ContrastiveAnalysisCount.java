package com.gennlife.rws.entity;

import java.util.Date;

public class ContrastiveAnalysisCount {
    private Integer id;

    private String uid;

    private String projectid;

    private String activerIndexId;

    private String groupType;

    private String contrastiveAnalysisCountResultId;

    private String createId;

    private String createName;

    private Date createTime;

    private String groupId;

    private String groupName;

    private String activeName;

    public String getActiveName() {
        return activeName;
    }

    public void setActiveName(String activeName) {
        this.activeName = activeName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? null : uid.trim();
    }

    public String getProjectid() {
        return projectid;
    }

    public void setProjectid(String projectid) {
        this.projectid = projectid == null ? null : projectid.trim();
    }

    public String getActiverIndexId() {
        return activerIndexId;
    }

    public void setActiverIndexId(String activerIndexId) {
        this.activerIndexId = activerIndexId == null ? null : activerIndexId.trim();
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType == null ? null : groupType.trim();
    }

    public String getContrastiveAnalysisCountResultId() {
        return contrastiveAnalysisCountResultId;
    }

    public void setContrastiveAnalysisCountResultId(String contrastiveAnalysisCountResultId) {
        this.contrastiveAnalysisCountResultId = contrastiveAnalysisCountResultId == null ? null : contrastiveAnalysisCountResultId.trim();
    }

    public String getCreateId() {
        return createId;
    }

    public void setCreateId(String createId) {
        this.createId = createId == null ? null : createId.trim();
    }

    public String getCreateName() {
        return createName;
    }

    public void setCreateName(String createName) {
        this.createName = createName == null ? null : createName.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}