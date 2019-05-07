package com.gennlife.rws.entity;

/**
 * @author liumingxin
 * @create 2018 18 21:23
 * @desc
 **/
public class Patient {
    private String patientSn;
    private String efhnic;
    private String nationality;
    private String maritalStatus;
    private String gender;
    private String DOC_ID;
    public Patient() {
    }

    public Patient(String patientSn, String efhnic, String nationality, String maritalStatus, String gender,String DOC_ID) {
        this.patientSn = patientSn;
        this.efhnic = efhnic;
        this.nationality = nationality;
        this.maritalStatus = maritalStatus;
        this.gender = gender;
        this.DOC_ID = DOC_ID;
    }

    public String getDOC_ID() {
        return DOC_ID;
    }

    public void setDOC_ID(String DOC_ID) {
        this.DOC_ID = DOC_ID;
    }

    public String getPatientSn() {
        return patientSn;
    }

    public void setPatientSn(String patientSn) {
        this.patientSn = patientSn;
    }

    public String getEfhnic() {
        return efhnic;
    }

    public void setEfhnic(String efhnic) {
        this.efhnic = efhnic;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
