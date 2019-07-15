package com.gennlife.rws.entity;

/**
 * @author lmx
 * @create 2019 04 18:37
 * @desc
 **/
public class PatientsIdSqlMap {
    private Integer id;
    private String dataSourceId;
    private Integer patGroupId;
    private String patientSnIds;
    private Integer export = 0;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public Integer getPatGroupId() {
        return patGroupId;
    }

    public void setPatGroupId(Integer patGroupId) {
        this.patGroupId = patGroupId;
    }

    public String getPatientSnIds() {
        return patientSnIds;
    }

    public void setPatientSnIds(String patientSnIds) {
        this.patientSnIds = patientSnIds;
    }

    public Integer getExport() {
        return export;
    }

    public void setExport(Integer export) {
        this.export = export;
    }
}
