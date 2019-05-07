package com.gennlife.rws.query;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author
 * @create 2018 10 21:45
 * @desc
 **/
public class QuerySearch {
    private String query;
    private String indexName;
    private String hospitalID;
    private Integer size;
    private Integer page;
    private Integer  search_dimension;
    private JSONArray source ;
    private JSONObject aggs;
    private String source_filter;
    private JSONArray exclude_psns;
    private Boolean fetchAllGroupByResult = false;
//    private long time_out ;
//    private  boolean isIgnore ;
//    private JsonArray roles ;
//    private JsonArray groups ;
//    private JsonObject power ;
//    private boolean searchOnePat ;
//    private String patientSn ;
//    private boolean hasError ;


    public Boolean getFetchAllGroupByResult() {
        return fetchAllGroupByResult;
    }

    public void setFetchAllGroupByResult(Boolean fetchAllGroupByResult) {
        this.fetchAllGroupByResult = fetchAllGroupByResult;
    }

    public String getSource_filter() {
        return source_filter;
    }

    public void setSource_filter(String source_filter) {
        this.source_filter = source_filter;
    }

    public JSONArray getExclude_psns() {
        return exclude_psns;
    }

    public void setExclude_psns(JSONArray exclude_psns) {
        this.exclude_psns = exclude_psns;
    }

    public QuerySearch() {
        this.hospitalID="public";
        this.search_dimension = 0;
    }

    public QuerySearch(Integer size,Integer page,String indexName,String query,JSONArray source){
        this();
        this.page = page;
        this.size = size;
        this.query = query;
        this.indexName = indexName;
        this.source =source;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getHospitalID() {
        return hospitalID;
    }

    public void setHospitalID(String hospitalID) {
        this.hospitalID = hospitalID;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSearch_dimension() {
        return search_dimension;
    }

    public void setSearch_dimension(Integer search_dimension) {
        this.search_dimension = search_dimension;
    }

    public JSONArray getSource() {
        return source;
    }

    public void setSource(JSONArray source) {
        this.source = source;
    }

    public JSONObject getAggs() {
        return aggs;
    }

    public void setAggs(JSONObject aggs) {
        this.aggs = aggs;
    }
}
