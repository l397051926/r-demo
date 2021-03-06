package com.gennlife.rws.entity;

import java.util.Date;

public class ActiveIndexTask {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column active_index_task.id
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    private String id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column active_index_task.active_index_id
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    private String activeIndexId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column active_index_task.project_id
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    private String projectId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column active_index_task.submit_time
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    private Date submitTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column active_index_task.status
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    private Integer status;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column active_index_task.message
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    private String message;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column active_index_task.complate_time
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    private Date complateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column active_index_task.case_total
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    private Integer caseTotal;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column active_index_task.market_apply
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    private Integer marketApply;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column active_index_task.search_result
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    private Integer searchResult;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column active_index_task.contain_apply
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    private Integer containApply;

    private Integer submitNum;

    public Integer getSubmitNum() {
        return submitNum;
    }

    public void setSubmitNum(Integer submitNum) {
        this.submitNum = submitNum;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column active_index_task.id
     *
     * @return the value of active_index_task.id
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column active_index_task.id
     *
     * @param id the value for active_index_task.id
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column active_index_task.active_index_id
     *
     * @return the value of active_index_task.active_index_id
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public String getActiveIndexId() {
        return activeIndexId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column active_index_task.active_index_id
     *
     * @param activeIndexId the value for active_index_task.active_index_id
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public void setActiveIndexId(String activeIndexId) {
        this.activeIndexId = activeIndexId == null ? null : activeIndexId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column active_index_task.project_id
     *
     * @return the value of active_index_task.project_id
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column active_index_task.project_id
     *
     * @param projectId the value for active_index_task.project_id
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId == null ? null : projectId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column active_index_task.submit_time
     *
     * @return the value of active_index_task.submit_time
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public Date getSubmitTime() {
        return submitTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column active_index_task.submit_time
     *
     * @param submitTime the value for active_index_task.submit_time
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column active_index_task.status
     *
     * @return the value of active_index_task.status
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column active_index_task.status
     *
     * @param status the value for active_index_task.status
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column active_index_task.message
     *
     * @return the value of active_index_task.message
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public String getMessage() {
        return message;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column active_index_task.message
     *
     * @param message the value for active_index_task.message
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public void setMessage(String message) {
        this.message = message == null ? null : message.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column active_index_task.complate_time
     *
     * @return the value of active_index_task.complate_time
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public Date getComplateTime() {
        return complateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column active_index_task.complate_time
     *
     * @param complateTime the value for active_index_task.complate_time
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public void setComplateTime(Date complateTime) {
        this.complateTime = complateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column active_index_task.case_total
     *
     * @return the value of active_index_task.case_total
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public Integer getCaseTotal() {
        return caseTotal;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column active_index_task.case_total
     *
     * @param caseTotal the value for active_index_task.case_total
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public void setCaseTotal(Integer caseTotal) {
        this.caseTotal = caseTotal;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column active_index_task.market_apply
     *
     * @return the value of active_index_task.market_apply
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public Integer getMarketApply() {
        return marketApply;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column active_index_task.market_apply
     *
     * @param marketApply the value for active_index_task.market_apply
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public void setMarketApply(Integer marketApply) {
        this.marketApply = marketApply;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column active_index_task.search_result
     *
     * @return the value of active_index_task.search_result
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public Integer getSearchResult() {
        return searchResult;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column active_index_task.search_result
     *
     * @param searchResult the value for active_index_task.search_result
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public void setSearchResult(Integer searchResult) {
        this.searchResult = searchResult;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column active_index_task.contain_apply
     *
     * @return the value of active_index_task.contain_apply
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public Integer getContainApply() {
        return containApply;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column active_index_task.contain_apply
     *
     * @param containApply the value for active_index_task.contain_apply
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    public void setContainApply(Integer containApply) {
        this.containApply = containApply;
    }
}