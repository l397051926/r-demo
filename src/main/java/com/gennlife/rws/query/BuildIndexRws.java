package com.gennlife.rws.query;

import com.alibaba.fastjson.JSONObject;

import static com.gennlife.rws.query.BuildIndexCrf.PROJECT_INDEX_NAME_PREFIX;


public class BuildIndexRws {
    private String indexName;
    private String fromIndexName;
    private String query;
    private JSONObject power;
    private Integer action;
    private String uid;
    private String buildIndexID;
    public BuildIndexRws() {
    }

    public BuildIndexRws(String indexName, String fromIndexName, String query,JSONObject power) {
        this.indexName = "rws_emr_"+indexName;
        this.fromIndexName = fromIndexName;
        this.query = query;
        this.power = power;
    }
    public BuildIndexRws(String indexName, String fromIndexName, String query,String crfId,JSONObject power) {
        this.indexName =  PROJECT_INDEX_NAME_PREFIX.get(crfId) + indexName;
        this.fromIndexName = fromIndexName;
        this.query = query;
        this.power = power;
    }

    public String getBuildIndexID() {
        return buildIndexID;
    }

    public void setBuildIndexID(String buildIndexID) {
        this.buildIndexID = buildIndexID;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public JSONObject getPower() {
        return power;
    }

    public void setPower(JSONObject power) {
        this.power = power;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getFromIndexName() {
        return fromIndexName;
    }

    public void setFromIndexName(String fromIndexName) {
        this.fromIndexName = fromIndexName;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
