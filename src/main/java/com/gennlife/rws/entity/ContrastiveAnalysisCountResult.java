package com.gennlife.rws.entity;

public class ContrastiveAnalysisCountResult {
    private Integer id;

    private String activeIndexId;

    private String colName;

    private String colValue;

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
        this.activeIndexId = activeIndexId == null ? null : activeIndexId.trim();
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName == null ? null : colName.trim();
    }

    public String getColValue() {
        return colValue;
    }

    public void setColValue(String colValue) {
        this.colValue = colValue == null ? null : colValue.trim();
    }
}