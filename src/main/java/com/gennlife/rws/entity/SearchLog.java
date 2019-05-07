package com.gennlife.rws.entity;

import java.util.Date;

public class SearchLog {
    private Integer id;

    private String patientSetId;

    private String createId;

    private Date createTime;

    private String searchConditio;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPatientSetId() {
        return patientSetId;
    }

    public void setPatientSetId(String patientSetId) {
        this.patientSetId = patientSetId == null ? null : patientSetId.trim();
    }

    public String getCreateId() {
        return createId;
    }

    public void setCreateId(String createId) {
        this.createId = createId == null ? null : createId.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getSearchConditio() {
        return searchConditio;
    }

    public void setSearchConditio(String searchConditio) {
        this.searchConditio = searchConditio == null ? null : searchConditio.trim();
    }
}