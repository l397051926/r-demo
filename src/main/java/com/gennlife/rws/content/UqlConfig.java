package com.gennlife.rws.content;

import com.gennlife.rws.util.StringUtils;
import com.gennlife.rws.util.TransPatientSql;

import java.util.*;


public class UqlConfig {
    /**
     * 对比分析页面 对应模拟组名字
     */
    public static final String CORT_INDEX_ID = "CORT_INDEX_ID";

    public static final String CORT_INDEX_REDIS_KEY = "rws_cort_index_";

    public static final String CORT_CONT_ENUM_REDIS_KEY = "rws_cont_enum_index_";

    public static final String CORT_CONT_ACTIVE_REDIS_KEY = "rws_cont_active_index_";

    private static final String RWS_SERVICE = "rws_service_";

    public static final Map<String, String> RESULT_ORDER_KEY;
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
     * @return false  is EMR  ; true  is not EMR
     */
    public static Boolean isCrf(String crfId) {
        return StringUtils.isNotEmpty(crfId) && !crfId.equals("EMR");
    }


    //整个项目数据sql
    public static String getAllProjectSql(String projectId) {
        return "select  visit_info.DOC_ID as pSn  from rws_emr_" + projectId + " where   join_field ='patient_info' ";
    }

    public static String getEnumSql(Set<String> allsql, String projectId, String crfId, List<String> joinList) {
        String where = IndexContent.getPatientInfoPatientSn(crfId) + " " + TransPatientSql.transForExtContain(allsql);
        String joinWhere = IndexContent.getPatientInfoPatientSn(crfId) + " " + TransPatientSql.transForExtContain(joinList);
        return "select " + IndexContent.getPatientDocId(crfId) + " as pSn from " + IndexContent.getIndexName(crfId, projectId) + " where " + where + " AND " + joinWhere + " " + IndexContent.getGroupBy(crfId);
    }

    public static String getRwsService(String val) {
        return RWS_SERVICE.concat(val);
    }

}
