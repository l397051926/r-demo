package com.gennlife.rws.content;

import com.gennlife.rws.entity.ActiveSqlMap;
import com.gennlife.rws.util.StringUtils;


import java.io.IOException;
import java.util.*;

import static com.gennlife.rws.query.BuildIndexCrf.PROJECT_INDEX_NAME_PREFIX;
import static java.util.stream.Collectors.joining;



public class UqlConfig {
    /**
     * 对比分析页面 对应模拟组名字
     */
    public static final String CORT_INDEX_ID = "CORT_INDEX_ID";

    public static final String CORT_INDEX_REDIS_KEY = "rws_cort_index_";

    public static final String CORT_CONT_ENUM_REDIS_KEY ="rws_cont_enum_index_";

    public static final String CORT_CONT_ACTIVE_REDIS_KEY = "rws_cont_active_index_";

    public static final Map<String,String> RESULT_ORDER_KEY ;
    /**
     * 需要将true  改变是的字段
     */
    public static final List<String> ACTIVE_CONVER_TRUE_OR_FALSE;
    static {
        ACTIVE_CONVER_TRUE_OR_FALSE = new ArrayList<>();
        ACTIVE_CONVER_TRUE_OR_FALSE.add("MAIN_DIAGNOSIS_FLAG");
        ACTIVE_CONVER_TRUE_OR_FALSE.add("CONTAGIOUS_FLAG");
        ACTIVE_CONVER_TRUE_OR_FALSE.add("UNCERTAIN_FLAG");
        ACTIVE_CONVER_TRUE_OR_FALSE.add("IS_REDUCTION_PAIN");
        ACTIVE_CONVER_TRUE_OR_FALSE.add("IS_PHYSICAL_COOLING");
        ACTIVE_CONVER_TRUE_OR_FALSE.add("IS_REPEATED_MEASURE");
        RESULT_ORDER_KEY = new HashMap<>();
    }

    /**
     *
     * @param crfId
     * @return  false  is EMR  ; true  is not EMR
     */
    public static final Boolean isCrf(String crfId){
       return StringUtils.isNotEmpty(crfId) && !crfId.equals("EMR");
    }


    //整个项目数据sql
    public static final String getAllProjectSql(String projectId){
        return "select  visit_info.DOC_ID as pSn  from rws_emr_" + projectId + " where   join_field ='patient_info' ";
    }
    //获取患者集的sql
    public static final String getPatientSetSql(String projectId,String patientSetQuery){
        return "select patient_info.DOC_ID as pSn from rws_emr_"+projectId+" where "+patientSetQuery+" group by patient_info.DOC_ID";
    }

    public static String getEnumAllSql(String projectId, List<ActiveSqlMap> sqlList, String patientSql) throws IOException {
        List<String> whereList = new LinkedList<>();
        for (ActiveSqlMap activeSqlMap : sqlList){
            whereList.add(activeSqlMap.getUncomSqlWhere());
        }
        String where = whereList.stream().map( s -> "("+s+")").collect(joining(" OR "));
        return "select patient_info.DOC_ID as pSn from rws_emr_"+projectId+" where " + where + " group by patient_info.DOC_ID";

    }

    public static String getEnumAllSql(String projectId, List<ActiveSqlMap> sqlList, String patientSql, String crfId) throws IOException {
        List<String> whereList = new LinkedList<>();
        for (ActiveSqlMap activeSqlMap : sqlList){
            whereList.add(activeSqlMap.getUncomSqlWhere());
        }
        String where = whereList.stream().map( s -> "("+s+")").collect(joining(" OR "));
        return "select "+IndexContent.getPatientDocId(crfId)+" as pSn from "+PROJECT_INDEX_NAME_PREFIX.get(crfId)+projectId+" where " + where +" "+ IndexContent.getGroupBy(crfId);
    }
}
