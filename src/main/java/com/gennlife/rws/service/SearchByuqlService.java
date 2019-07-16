package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.ActiveSqlMap;
import com.gennlife.rws.entity.GroupData;
import com.gennlife.rws.entity.Patient;
import com.gennlife.rws.entity.PatientsIdSqlMap;
import com.gennlife.rws.util.AjaxObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface SearchByuqlService {
    String SearchByIndex(JSONObject object, String resultOrderKey, Integer isSearch, PatientsIdSqlMap patientSql, String crfId) throws ExecutionException, InterruptedException, IOException;

    String searchDocIdBySql(String newSql, String projectId, String crfId);

    String searchByActive(JSONObject obj, String resultOrderKey, Integer isSearch, PatientsIdSqlMap patientSql, String crfId) throws ExecutionException, InterruptedException, IOException;

    String SearchByEnume(JSONObject obj, String resultOrderKey, Integer isSearch, PatientsIdSqlMap patientSql, String crfId) throws ExecutionException, InterruptedException, IOException;

    String SearchByExclude(JSONObject object, String resultOrderKey, Integer isSearch, PatientsIdSqlMap patientSql, String crfId) throws ExecutionException, InterruptedException, IOException;

    AjaxObject searchClacIndexResultByUql(String activeId, String projectId, Integer pageSize, Integer pageNum, JSONArray basicColumns,
                                          String groupFromId, JSONArray patientSetId, String groupId, String isVariant, String crfId) throws IOException, ExecutionException, InterruptedException;

    Integer getSearchUqlAllCount(String groupFromId, JSONArray patientSetId, String groupId, String projectId);

    AjaxObject searchCalcResultByUql(String activeId, String projectId, JSONArray basicColumns, JSONArray visitColumns, Integer activeType,
                                     Integer pageNum, Integer pageSize, String activeResult, String groupFromId, JSONArray patientSetId, String groupId, String crfId) throws InterruptedException, IOException, ExecutionException;

    AjaxObject searchCalcExculeByUql(String activeId, String projectId, Integer pageSize, Integer pageNum, JSONArray basicColumns,
                                     String isExport, String groupId, String groupName, JSONArray patientSetId, String createId,
                                     String createName, String groupFromId, boolean autoExoprt, String crfId) throws IOException, ExecutionException, InterruptedException;

    Map<String, String> saveEnumCortrastiveResultRedisMap(List<ActiveSqlMap> activeSqlMap1, String projectId, String crfId, String activeIndexId) throws IOException;

    Map<String, String> saveCortrastiveResultRedisMap(ActiveSqlMap activeSqlMap, String projectId, String crfId, String activeIndexId) throws IOException;

    AjaxObject exportToGroup(String groupId, List<String> allResutList, List<ActiveSqlMap> sqlList, String projectId, Integer pageNum, String crfId, String activeId, JSONArray refActiveIds, JSONArray patientSetId, String groupName, String createId, String createName, boolean autoExport) throws IOException;

    AjaxObject getPatientListByAll(String patientSetId, String projectId, JSONArray showColumns, JSONArray actives, Integer pageNum, Integer pageSize, Integer type, String crfId) throws IOException;

    AjaxObject getAggregationAll(String patientSetId, JSONArray aggregationTeam, String projectId, String crfId);

    AjaxObject getAggregationData(String patSns, String crfId, JSONArray aggregationTeam, String projectId);

    List<Patient> getpatentByUql(String patientSetId, boolean isExport, String projectId, String crfId) ;

    JSONArray getPatientListByPatientSn(List<GroupData> groupDataList, JSONArray columns, Integer activeType, String projectId, String crfId);

    AjaxObject getPatientListByAllByPatientSetIds(JSONArray patientSetIdTmp, String projectId, JSONArray showColumns, JSONArray actives, Integer pageNum, Integer pageSize, int i, String crfId);

    void RunReferenceCalculate(String T_activeIndexId, String projectId, String crfId);

    void referenceCalculate(String activeId, String projectId, Integer activeType, String resultOrderKey, JSONArray patientsSetId, String groupToId, String groupFromId, String crfId) throws ExecutionException, InterruptedException, IOException;

    AjaxObject getPatientSnsByAll(String patientsSetId, String projectId, JSONArray showColumns, JSONArray actives, Integer pageNum, Integer pageSize, Integer type, String crfId);

    String getInitialSQL(String groupFromId, String isVariant, String groupToId, JSONArray patientSetId, String projectId, String crfId);

    List<String> getInitialSQLList(String groupFromId, String isVariant, String groupToId, JSONArray patientSetId, String projectId, String crfId);

    List<PatientsIdSqlMap> getInitialSQLTmp(String groupFromId, String isVariant, String groupToId, JSONArray patientSetId, String projectId, String crfId);
}
