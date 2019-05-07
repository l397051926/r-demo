package com.gennlife.rws.entity;

public class ProjectScientificMap {
	
    private Integer id;

    private String scientificId;

    private String projectId;

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

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId == null ? null : projectId.trim();
    }
}