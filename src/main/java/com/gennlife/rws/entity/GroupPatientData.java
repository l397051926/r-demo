package com.gennlife.rws.entity;

public class GroupPatientData {
    private Integer id;

    private String groupId;

    private String patientSetId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId == null ? null : groupId.trim();
    }

    public String getPatientSetId() {
        return patientSetId;
    }

    public void setPatientSetId(String patientSetId) {
        this.patientSetId = patientSetId == null ? null : patientSetId.trim();
    }
}