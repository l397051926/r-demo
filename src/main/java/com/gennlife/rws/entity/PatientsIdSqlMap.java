package com.gennlife.rws.entity;

/**
 * @author lmx
 * @create 2019 04 18:37
 * @desc
 **/
public class PatientsIdSqlMap {
    private Integer id;
    private String patientsSetId;
    private Integer patGroupId;
    private String patientSnIds;
    private Integer export = 0;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPatientsSetId() {
        return patientsSetId;
    }

    public void setPatientsSetId(String patientsSetId) {
        this.patientsSetId = patientsSetId;
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
