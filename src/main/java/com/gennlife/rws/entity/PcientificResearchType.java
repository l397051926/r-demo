package com.gennlife.rws.entity;

public class PcientificResearchType {
    private Integer id;

    private String scientificId;

    private String scientificName;

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

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName == null ? null : scientificName.trim();
    }
}