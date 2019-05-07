package com.gennlife.rws.entity;

public class ScientificGroupMap {
    private Integer id;

    private String scientificId;

    private String groupTypeId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getScientificId() {
        return scientificId;
    }

    public void setScientificId(String scientificId) {
        this.scientificId = scientificId == null ? null : scientificId.trim();
    }

    public String getGroupTypeId() {
        return groupTypeId;
    }

    public void setGroupTypeId(String groupTypeId) {
        this.groupTypeId = groupTypeId == null ? null : groupTypeId.trim();
    }
}