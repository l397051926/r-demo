/**
 * copyRight
 */
package com.gennlife.rws.content;

/**
 * Created by liuzhen.
 * Date: 2017/10/19
 * Time: 19:45
 */
public class CommonContent {
    public static final String UPDATE_MODE_INSERT = "INSERT";
    public static final String UPDATE_MODE_UPDATE = "UPDATE";
    public static final String EMR_PATIENT_INFO = "patient_info";
    public static final String EMR_VISIT = "visits";
    public static final String EMR_CRF_NAME = "EMR";
    /**
     * 临时数据
     */
    public static final int ACTIVE_TYPE_TEMP = 1;
    /**
     * 临时另存
     */
    public static final int ACTIVE_TYPE_TEMP_SAVEAS = 2;
    /**
     * 正式数据
     */
    public static final int ACTIVE_TYPE_NOTEMP = 0;
    /**
     * 事件
     */
    public static final Integer ACTIVE_TYPE_EVENT = 1;

    /**
     * 事件中文描述
     */
    public static final String ACTIVE_TYPE_EVENT_DESC = "事件";
    /**
     * 指标
     */
    public static final Integer ACTIVE_TYPE_INDEX = 2;
    /***
     * 指标中文描述
     */
    public static final String ACTIVE_TYPE_INDEX_DESC = "指标";
    /**
     * 入排
     */
    public static final Integer ACTIVE_TYPE_INOUTN = 3;
    /**
     * 入排中文描述
     */
    public static final String ACTIVE_TYPE_INOUTN_DESC = "入排条件";
    public static final String ACTIVE_TYPE_IN_DESE = "纳入标准";
    public static final String ACTIVE_TYPE_IN = "3";
    public static final String ACTIVE_TYPE_OUT = "4";
    public static final String ACTIVE_TYPE_OUT_DESC = "排除标准";
    public static final String ACTIVE_INDEX_TYPE_DESC = "自定义枚举类型";
    public static final String ACTIVE_INDEX_STATIC_RESULT = "visits";
    /**
     * 组
     */
    public static final Integer ACTIVE_CONDITION_TYPE_1 = 1;
    /**
     * 条件
     */
    public static final Integer ACTIVE_CONDITION_TYPE_2 = 2;

    public static final String ACTIVE_INDEX_VALUE_TYPE_1 = "1";
    public static final String ACTIVE_INDEX_VALUE_TYPE_2 = "2";
    public static final String ACTIVE_INDEX_VALUE_TYPE_3 = "3";
    /**
     * 枚举
     */
    public static final String ACTIVE_INDEX_VALUE_TYPE_4 = "枚举:boolean";
    /**
     * 提交任务成功
     */
    public static final Integer ACTIVE_TASK_STATUS_0 = 0;
    /**
     * 处理任务成功
     */
    public static final Integer ACTIVE_TASK_STATUS_1 = 1;
    /**
     * 提交任务失败
     */
    public static final Integer ACTIVE_TASK_STATUS_2 = 2;
    /**
     * 任务开始计算
     */
    public static final Integer ACTIVE_TASK_STATUS_CALCING = 3;
    /**
     * ES 查询串
     */
    public static final String ESSEARCHPARAM = "{\n" +
        "            \"size\":PAGESIZE,\n" +
        "            \"hospitalID\":\"public\",\n" +
        "            \"indexName\":\"INDEXNAME\",\n" +
        "            \"page\":PAGENUM,\n" +
        "            \"source\":[\"\"],\n" +
        "            \"query\":\"QUERYPARAM\"\n" +
        "\n" +
        "}";
    public static final String ESSEARCHPARAMEXPORT = "{\n" +
        "  \"indexName\": \"QUERYINDEXNAME\",\n" +
        "  \"query\": \"QUERYCONDITION\",    \n" +
        "  \"size\": PAGESIZE,\n" +
        "\"source\": [\"patient_info\"]" +
        "}";

    /**
     * 修改任务状态的sql
     */
    public static final String PACKAGINGSERVICE_UPDATE_SQL = "update active_index_task set status=PRODUCESTATUA,message=MESSAGE,complate_time=COMPLATETIME,case_total=CASETOTAL,market_apply=MARKETAPPLY,search_result=SEARCHRESULT,contain_apply=CONTAINAPPLY where active_index_id='INDEXPRECESSID' and  id='PRECESSID'";

    public static final String REMOTE_STATUS = "status";

    public static final String SHARDCOLLECTION = "{ shardcollection : \"RWS.TABLES\",key : {patient_sn: \"hashed\"} }";
}
