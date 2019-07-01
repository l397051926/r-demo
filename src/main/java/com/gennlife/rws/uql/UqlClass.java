package com.gennlife.rws.uql;

import com.alibaba.fastjson.JSONArray;
import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.schema.AbstractFieldAnalyzer;
import com.gennlife.rws.uqlcondition.UqlWhere;
import com.gennlife.rws.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.gennlife.rws.query.BuildIndexCrf.PROJECT_INDEX_NAME_PREFIX;

/**
 * @author liumingxin
 * @create 2018 17 9:07
 * @desc
 **/
public abstract class UqlClass {

    public static final String IS_VARIANT_TRUE = "1";

    private String select= "";
    private String where;
    private String from ;
    private String having;
    private Map<String,String> join;
    private List<String> source;
    private String sourceFilter;
    private List<AdjointWhere> adjoint;

    private String function;

    private String resultValue ;
    private String resultFunction;
    private String resultFunctionNum;

    private String adjointValue;
    private String adjointCond;
    private String adjointActiveId;

    private JSONArray activeId = new JSONArray();

    private List<String> enumOther = new ArrayList<>();
    private String visitsGroup;

    public void setAdjointVal(AdjointWhere adjointValue){
        this.adjoint.add(adjointValue);
    }

    public String getAdjointTopVal(){
        return adjoint.get(adjoint.size()-1).getAdjointName();
    }

    public boolean isAdjointTopValBracket(){
        return adjoint.get(adjoint.size()-1).getAdjointName().contains("(");
    }

    public List<String> getEnumOther() {
        return enumOther;
    }

    public void setEnumOther(List<String> enumOther) {
        this.enumOther = enumOther;
    }

    public JSONArray getActiveId() {
        return activeId;
    }

    public void addActiveId(String activeId){
        if(!this.activeId.contains(activeId)){
            this.activeId.add(activeId);
        }
    }

    public void setActiveId(JSONArray activeId) {
        this.activeId = activeId;
    }

    public String getAdjointActiveId() {
        return adjointActiveId;
    }

    public void setAdjointActiveId(String adjointActiveId) {
        this.adjointActiveId = adjointActiveId;
    }

    public String getAdjointCond() {
        return adjointCond;
    }

    public void setAdjointCond(String adjointCond) {
        this.adjointCond = adjointCond;
    }

    public String getAdjointValue() {
        return adjointValue;
    }

    public void setAdjointValue(String adjointValue) {
        this.adjointValue = adjointValue;
    }

    public List<AdjointWhere> getAdjoint() {
        return adjoint;
    }

    public void setAdjoint(List<AdjointWhere> adjoint) {
        this.adjoint = adjoint;
    }

    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    public String getResultFunction() {
        return resultFunction;
    }

    public void setResultFunction(String resultFunction) {
        this.resultFunction = resultFunction;
    }

    public String getResultFunctionNum() {
        return resultFunctionNum;
    }

    public void setResultFunctionNum(String resultFunctionNum) {
        this.resultFunctionNum = resultFunctionNum;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public UqlClass(){

    }

    public UqlClass(String from){
        this.setFrom("rws_emr_"+from);
        this.setActiveSelect(" patient_info.DOC_ID as pSn, ");
        join = new ConcurrentHashMap<>();
        source = new LinkedList<>();
        adjoint = new LinkedList<>();
    }

    public UqlClass(String from,String crfId){
        this.setFrom(PROJECT_INDEX_NAME_PREFIX.get(crfId) + from);
        if(StringUtils.isNotEmpty(crfId)){
            this.setActiveSelect(" "+ IndexContent.getPatientDocId(crfId)+" as pSn, ");
        }
        join = new ConcurrentHashMap<>();
        source = new LinkedList<>();
        adjoint = new LinkedList<>();
    }

    public void setSourceFilterValue(String cond,String value){
        if(StringUtils.isEmpty(sourceFilter)){
            this.sourceFilter = value;
        }else {
            this.sourceFilter = this.sourceFilter.concat(" "+cond+" "+value);
        }
    }

    public String getSourceFilter() {
        return sourceFilter;
    }

    public void setSourceFilter(String sourceFilter) {
        this.sourceFilter = sourceFilter;
    }

    public void setSourceValue(String value){
        this.source.add(value);
    }

    public void setJoinValue(String activeId,String value) {
        this.join.put(activeId,value);
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select +" as condition";
    }

    public void setActiveSelect(String select) {
        this.select = select;
    }

    public void setJoinSelect(String select){
        this.select = select;
    }

    public String getWhereNotNull(){
        return where==null?"":where;
    }

    public String getWhere() {
        return where;
    }
    public String getNotEmptyWhere(){
        return StringUtils.isEmpty(where) ? " " : where;
    }

    public void setWhere(String where) {
        this.where = where;
    }
    public void setIsEmptyWhere(String where){
        if(StringUtils.isEmpty(where)){
            this.where = where;
        }else {
            this.where = this.where + where;
        }
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setFrom(String from,String crfId) {
        this.setFrom(PROJECT_INDEX_NAME_PREFIX.get(crfId) + from);
    }

    public String getSql(){
        return "select " + select +" from " + from +" where "+where +" group by patient_info.DOC_ID ";
    }

    public String getCrfSql(){
        return "select " + select +" from " + from +" where "+where +" group by patient_info.patient_basicinfo.DOC_ID ";
    }

    public String getVisitsSql(){
        return "select " + select +" from " + from +" where "+where ;
    }

    public Map<String, String> getJoin() {
        return join;
    }

    public void setJoin(Map<String, String> join) {
        this.join = join;
    }

    public List<String> getSource() {
        return source;
    }

    public void setSource(List<String> source) {
        this.source = source;
    }

    public void setIndexWhereIsEmpty(String order1, UqlWhere where){
        if (where.isEmpty()) {
            if (order1.startsWith("visit_info")) {
                setWhere(order1 + " IS NOT NULL");
            } else {
                setWhere("haschild( " + order1 + " IS NOT NULL )");
            }
        } else {
            if (order1.startsWith("visit_info")) {
                setWhere(order1 + " IS NOT NULL AND ");
            } else {
                setWhere("haschild( " + order1 + " IS NOT NULL ) AND ");
            }
        }
    }

    public void setActiveWhereIsEmpty(String visits, UqlWhere where) {
        if (where.isEmpty()) {
            if (visits.startsWith("visit_info")) {
                setWhere(visits + ".VISIT_SN IS NOT NULL AND ");
            } else {
                setWhere("haschild( " + visits + ".VISIT_SN IS NOT NULL ) AND ");
            }
        } else {
            if (visits.startsWith("visit_info")) {
                setWhere(visits + ".VISIT_SN IS NOT NULL AND ");
            } else {
                setWhere("haschild( " + visits + ".VISIT_SN IS NOT NULL ) AND ");
            }
        }
    }

    public String getHaving() {
        return having;
    }

    public void setHaving(String having) {
        this.having = having;
    }

    public abstract void setInitialPatients(String isVariant, String patientSql);

    public abstract void setWhereIsEmpty(UqlWhere where, String order1, String isVariant, String patientSql, AbstractFieldAnalyzer schema);

    public void setSqlHaving(String functionParam) {
        if(StringUtils.isNotEmpty(functionParam)){
            having =  " having jocount >=" + functionParam;
        }

    }

    public abstract void  setNotAllWhere(String function, String order1, String indexDate, AbstractFieldAnalyzer schema);

    public abstract  String getHavingSql() ;
    public abstract  String getHavingSql(String crfId) ;

    public void setVistSnWhere(UqlWhere where,String visits,String order1, AbstractFieldAnalyzer schema) {

    }

    public String getVisitsGroup() {
        return visitsGroup;
    }

    public void setVisitsGroup(String visitsGroup) {
        this.visitsGroup = visitsGroup;
    }
}
