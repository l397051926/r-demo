package com.gennlife.rws.entity;

import java.util.Date;

/**
 * @author liumingxin
 * @create 2019 03 15:27
 * @desc
 **/
public class InputTask {
    /**
     * `input_id`  varchar(52) NOT NULL COMMENT '导入id' ,
     `project_id`  varchar(52) NULL COMMENT '项目id' ,
     `project_name`  varchar(255) NULL COMMENT '项目名称' ,
     `patient_set_id`  varchar(255) NULL COMMENT '患者集id' ,
     `patient_set_name`  varchar(255) NULL COMMENT '患者集名称' ,
     `uid`  varchar(52) NULL COMMENT '用户id' ,
     `patient_count`  tinyint NULL COMMENT '患者数量' ,
     `create_time`  datetime NULL COMMENT '创建时间' ,
     `start_time`  datetime NULL COMMENT '开始时间' ,
     `finish_time`  datetime NULL COMMENT '完成时间' ,
     `status`  varchar(255) NULL COMMENT '状态' ,
     `remain_time`  datetime NULL COMMENT '预计完成事件' ,
     `update_time`  datetime NULL COMMENT '更新时间 排序字段' ,
     progress   完成进度
     */
    private String inputId;
    private String projectId;
    private String projectName;
    private String patientSetId;
    private String patientSetName;
    private String uid;
    private Long patientCount;
    private Date createTime;
    private Date startTime;
    private Date finishTime;
    private Integer status;
    private Long remainTime;
    private Date updateTime;
    private Integer progress;
    private String crfId;

    public InputTask() {
    }

    public InputTask(String taskId, Long createTime, Long startTime, Long finishTime, Integer status, Integer progress, Long remainTime) {
        this.inputId = taskId;
        if(createTime != null){
            this.createTime = new Date(createTime);
        }
        if(startTime != null){
            this.startTime = new Date(startTime);
        }
        if(finishTime != null){
            this.finishTime = new Date(finishTime);
        }
        this.status = status;
        this.progress = progress;
        this.remainTime =remainTime;

    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getInputId() {
        return inputId;
    }

    public void setInputId(String inputId) {
        this.inputId = inputId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getPatientSetId() {
        return patientSetId;
    }

    public void setPatientSetId(String patientSetId) {
        this.patientSetId = patientSetId;
    }

    public String getPatientSetName() {
        return patientSetName;
    }

    public void setPatientSetName(String patientSetName) {
        this.patientSetName = patientSetName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getPatientCount() {
        return patientCount;
    }

    public void setPatientCount(Long patientCount) {
        this.patientCount = patientCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getRemainTime() {
        return remainTime;
    }

    public void setRemainTime(Long remainTime) {
        this.remainTime = remainTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getCrfId() {
        return crfId;
    }

    public void setCrfId(String crfId) {
        this.crfId = crfId;
    }
}
