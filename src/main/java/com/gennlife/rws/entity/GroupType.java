package com.gennlife.rws.entity;

public class GroupType {
    private Integer id;

    private String groupTypeId;

    private String groupTypeName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGroupTypeId() {
        return groupTypeId;
    }

    public void setGroupTypeId(String groupTypeId) {
        this.groupTypeId = groupTypeId == null ? null : groupTypeId.trim();
    }

    public String getGroupTypeName() {
        return groupTypeName;
    }

    public void setGroupTypeName(String groupTypeName) {
        this.groupTypeName = groupTypeName == null ? null : groupTypeName.trim();
    }
}