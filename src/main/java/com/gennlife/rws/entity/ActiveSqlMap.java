package com.gennlife.rws.entity;


import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.util.GzipUtil;

import java.io.IOException;

public class ActiveSqlMap {
    private Integer id;
    private String projectId;
    private String activeIndexId;
    private String activeSql;
    private String indexResultValue;

    private String sqlSelect;
    private String sqlWhere;
    private String sqlFrom;
    private String sqlHaving;
    private String sourceFiltere;
    private String activeType;

    private String refActiveIds;
    private String sourceValue;

    private String activeResultDocId;
    private String activeResultValue;
    private String eventWhere;
    private String selectValue;

    private String indexTypeValue;
    private String activeName ;  //指标名称
    private Integer isOther;
    private String groupId;
    private Integer patSqlGroup;


    public ActiveSqlMap(String projectId, String activeIndexId, String activeSql,
                        String sqlSelect, String sqlFrom, String sourceFilter,
                        String activeType, String refActiveIds, String sourceValue, String activeResultDocId,
                        String activeResultValue, String sqlMd5, String eventWhere,
                        String selectValue, String activeOtherResult, String countValue) {
        this.projectId = projectId;
        this.activeIndexId = activeIndexId;
        this.activeSql = activeSql;
        this.sqlSelect = sqlSelect;
        this.sqlFrom = sqlFrom;
        this.sourceFiltere = sourceFilter;
        this.activeType = activeType;
        this.refActiveIds = refActiveIds;
        this.sourceValue = sourceValue;
        this.activeResultDocId = activeResultDocId;
        this.activeResultValue = activeResultValue;
        this.sqlMd5 = sqlMd5;
        this.eventWhere = eventWhere;
        this.selectValue = selectValue;
        this.activeOtherResult = activeOtherResult;
        this.countValue = countValue;

    }

    public ActiveSqlMap(String projectId, String activeIndexId, String activeSql,
                        String sqlSelect, String sqlFrom, String refActiveIds, String sourceValue) {
        this.projectId = projectId;
        this.activeIndexId = activeIndexId;
        this.activeSql = activeSql;
        this.sqlSelect = sqlSelect;
        this.sqlFrom = sqlFrom;
        this.refActiveIds = refActiveIds;
        this.sourceValue = sourceValue;
    }

    public ActiveSqlMap(String projectId, String activeIndexId, String activeSql, String sqlSelect,
                        String sqlFrom, String sourceFiltere, String refActiveIds, String sourceValue,
                        String indexResultValue, String indexTypeValue, String selectValue, String activeName) {
        this.projectId = projectId;
        this.activeIndexId = activeIndexId;
        this.activeSql = activeSql;
        this.sqlSelect = sqlSelect;
        this.sqlFrom = sqlFrom;
        this.sourceFiltere = sourceFiltere;
        this.refActiveIds = refActiveIds;
        this.sourceValue = sourceValue;
        this.indexResultValue = indexResultValue;
        this.indexTypeValue = indexTypeValue;
        this.selectValue = selectValue;
        this.activeName = activeName;
    }

    public Integer getIsOther() {
        return isOther;
    }

    public void setIsOther(Integer isOther) {
        this.isOther = isOther;
    }

    public ActiveSqlMap() {
    }

    public ActiveSqlMap(String projectId, String activeIndexId, String activeSql,
                        String sqlSelect, String sqlFrom, String sourceFiltere,
                        String refActiveIds, String sourceValue, String selectValue,
                        String indexTypeValue, String activeName, String countValue,
                        String sqlMd5) {
        this.projectId = projectId;
        this.activeIndexId = activeIndexId;
        this.activeSql = activeSql;
        this.sqlSelect = sqlSelect;
        this.sqlFrom = sqlFrom;
        this.sourceFiltere = sourceFiltere;
        this.refActiveIds = refActiveIds;
        this.sourceValue = sourceValue;
        this.selectValue = selectValue;
        this.indexTypeValue = indexTypeValue;
        this.activeName = activeName;
        this.countValue = countValue;
        this.sqlMd5 = sqlMd5;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getSqlHaving() {
        return sqlHaving;
    }

    public void setSqlHaving(String sqlHaving) {
        this.sqlHaving = sqlHaving;
    }

    public String getActiveName() {
        return activeName;
    }

    public void setActiveName(String activeName) {
        this.activeName = activeName;
    }

    public String getIndexTypeValue() {
        return indexTypeValue;
    }

    public void setIndexTypeValue(String indexTypeValue) {
        this.indexTypeValue = indexTypeValue;
    }

    private String activeOtherResult;
    private String countValue; //统计 count 的value
    private String sqlMd5;

    public String getSqlMd5() {
        return sqlMd5;
    }

    public void setSqlMd5(String sqlMd5) {
        this.sqlMd5 = sqlMd5;
    }

    public String getCountValue() {
        return countValue;
    }

    public void setCountValue(String countValue) {
        this.countValue = countValue;
    }

    public String getActiveOtherResult() throws IOException {
        return activeOtherResult;
    }
    public String getUncoomActiveOtherResult() throws IOException {
        return GzipUtil.uncompress(getActiveOtherResult());
    }

    public void setActiveOtherResult(String activeOtherResult) throws IOException {
        this.activeOtherResult = activeOtherResult;
    }

    public String getSelectValue() {
        return selectValue;
    }

    public void setSelectValue(String selectValue) {
        this.selectValue = selectValue;
    }

    public String getEventWhere() throws IOException {
        return eventWhere;
    }
    public String getUnncomEventWhere() throws IOException {
        return GzipUtil.uncompress(getEventWhere());
    }

    public void setEventWhere(String eventWhere) throws IOException {
        this.eventWhere = eventWhere;
    }

    public String getActiveResultValue() {
        return activeResultValue;
    }

    public void setActiveResultValue(String activeResultValue) {
        this.activeResultValue = activeResultValue;
    }

    public String getActiveResultDocId() {
        return activeResultDocId;
    }

    public void setActiveResultDocId(String activeResultDocId) {
        this.activeResultDocId = activeResultDocId;
    }

    public String getSourceValue() {
        return sourceValue;
    }

    public void setSourceValue(String sourceValue) {
        this.sourceValue = sourceValue;
    }

    public String getRefActiveIds() {
        return refActiveIds;
    }

    public void setRefActiveIds(String refActiveIds) {
        this.refActiveIds = refActiveIds;
    }

    public String getActiveType() {
        return activeType;
    }

    public void setActiveType(String activeType) {
        this.activeType = activeType;
    }

    public String getSourceFiltere() {
        return sourceFiltere;
    }

    public void setSourceFiltere(String sourceFiltere) {
        this.sourceFiltere = sourceFiltere;
    }

    public String getSqlSelect() {
        return sqlSelect;
    }

    public void setSqlSelect(String sqlSelect) {
        this.sqlSelect = sqlSelect;
    }

    public String getSqlWhere() throws IOException {
        return sqlWhere;
    }
    public String getUncomSqlWhere() throws IOException {
        return GzipUtil.uncompress(getSqlWhere());
    }

    public void setSqlWhere(String sqlWhere) throws IOException {
        this.sqlWhere = sqlWhere;
    }
    public void setUncomSqlWhere(String sqlWhere) throws IOException {
        setSqlWhere(GzipUtil.compress(sqlWhere));
    }


    public String getSqlFrom() throws IOException {
        return sqlFrom;
    }

    public void setSqlFrom(String sqlFrom) throws IOException {
        this.sqlFrom = sqlFrom;
    }

    public String getIndexResultValue() {
        return indexResultValue;
    }

    public void setIndexResultValue(String indexResultValue) {
        this.indexResultValue = indexResultValue;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getActiveIndexId() {
        return activeIndexId;
    }

    public void setActiveIndexId(String activeIndexId) {
        this.activeIndexId = activeIndexId;
    }

    public String getActiveSql() throws IOException {
        return activeSql;
    }
    public String getUncomActiveSql()throws IOException{
        return GzipUtil.uncompress(getActiveSql());
    }

    public void setActiveSql(String activeSql) throws IOException {
        this.activeSql = activeSql;
    }

    public Integer getPatSqlGroup() {
        return patSqlGroup;
    }

    public void setPatSqlGroup(Integer patSqlGroup) {
        this.patSqlGroup = patSqlGroup;
    }

    public String getUql() throws IOException {
        return "select " + getSqlSelect() +" from " + getSqlFrom() +" where "+getUncomSqlWhere() +" group by patient_info.DOC_ID ";
    }
    public String getUql(String crfId) throws IOException {
        return "select " + getSqlSelect() +" from " + getSqlFrom() +" where "+getUncomSqlWhere() + IndexContent.getGroupBy(crfId);
    }
    public String getEnumUql(String patSn) throws IOException {
        return "select " + getSqlSelect() +" from " + getSqlFrom() +" where "+getUncomSqlWhere() + " and patient_info.PATIENT_SN in ('"+patSn+"') " +" group by patient_info.DOC_ID ";
    }
    public String getActiveUql() throws IOException {
        return "select " + getSqlSelect() +" from " + getSqlFrom() +" where "+getUnncomEventWhere() +" group by patient_info.DOC_ID ";

    }
}
