/**
 * copyRight
 */
package com.gennlife.rws.entity;

import java.util.Date;

/**
 * @author liuzhen
 * Created by liuzhen.
 * Date: 2018/8/30
 * Time: 20:57
 */
public class ActiveIndexRelation {
    private String refActiveId;
    private String activeName;
    private String activeIndexId;
    private String sourceName;
    private String projectId;
    private String activeType;
    private String activeTypeName;
    private String isTmp;
    private String refActiveType;
    private String indexTypeDesc;
    private Date updateTime;
    private Date createTime;

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getIndexTypeDesc() {

        return indexTypeDesc;
    }

    public void setIndexTypeDesc(String indexTypeDesc) {

        this.indexTypeDesc = indexTypeDesc;
    }

    public String getRefActiveType() {

        return refActiveType;
    }

    public void setRefActiveType(String refActiveType) {

        this.refActiveType = refActiveType;
    }

    public String getProjectId() {

        return projectId;
    }

    public void setProjectId(String projectId) {

        this.projectId = projectId;
    }

    public String getActiveType() {

        return activeType;
    }

    public void setActiveType(String activeType) {

        this.activeType = activeType;
    }

    public String getActiveTypeName() {

        return activeTypeName;
    }

    public void setActiveTypeName(String activeTypeName) {

        this.activeTypeName = activeTypeName;
    }

    public String getIsTmp() {

        return isTmp;
    }

    public void setIsTmp(String isTmp) {

        this.isTmp = isTmp;
    }

    public String getRefActiveId() {

        return refActiveId;
    }

    public void setRefActiveId(String refActiveId) {

        this.refActiveId = refActiveId;
    }

    public String getActiveName() {

        return activeName;
    }

    public void setActiveName(String activeName) {

        this.activeName = activeName;
    }

    public String getActiveIndexId() {

        return activeIndexId;
    }

    public void setActiveIndexId(String activeIndexId) {

        this.activeIndexId = activeIndexId;
    }

    public String getSourceName() {

        return sourceName;
    }

    public void setSourceName(String sourceName) {

        this.sourceName = sourceName;
    }
}
