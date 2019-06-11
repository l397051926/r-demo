package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.ActiveSqlMap;
import com.gennlife.rws.entity.GroupData;
import com.gennlife.rws.entity.Patient;
import com.gennlife.rws.util.AjaxObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface SearchByuqlService {
     String SearchByIndex(JSONObject object,String resultOrderKey,Integer isSearch) throws ExecutionException, InterruptedException, IOException;

    String searchByActive(JSONObject obj, String resultOrderKey, Integer isSearch) throws ExecutionException, InterruptedException, IOException;

    Map<String, String> saveEnumCortrastiveResultRedisMap(List<ActiveSqlMap> activeSqlMap1, String projectId, String crfId, String activeIndexId) throws IOException;

    Map<String,String> saveCortrastiveResultRedisMap(ActiveSqlMap activeSqlMap, String projectId, String crfId, String activeIndexId) throws IOException;

    String SearchByExclude(JSONObject object, String resultOrderKey, Integer isSearch) throws ExecutionException, InterruptedException, IOException;

     String SearchByEnume(JSONObject obj, String resultOrderKey,Integer isSearch) throws ExecutionException, InterruptedException, IOException;

    AjaxObject searchClacIndexResultByUql(String activeId, String projectId, Integer pageSize, Integer pageNum, JSONArray basicColumns, String groupFromId, JSONArray patientSetId, String groupId, String isVariant) throws IOException, ExecutionException, InterruptedException;

    AjaxObject searchCalcResultByUql(String activeId, String projectId, JSONArray basicColumns, JSONArray visitColumns, Integer activeType, Integer pageNum, Integer pageSize, String activeResult, String groupFromId, JSONArray patientSetId, String groupId) throws InterruptedException, IOException, ExecutionException;

    AjaxObject searchCalcExculeByUql(String activeId, String projectId, Integer pageSize, Integer pageNum, JSONArray basicColumns, String isExport, String groupId, String groupName, JSONArray patientSetId, String createId, String createName,String groupFromId,boolean autoExoprt) throws IOException, ExecutionException, InterruptedException;

    AjaxObject getPatientListByAll(String patientSetId, String projectId, JSONArray showColumns, JSONArray actives, Integer pageNum, Integer pageSize, Integer type, String crfId) throws IOException;

    AjaxObject getAggregationAll(String patientSetId, JSONArray aggregationTeam, String projectId, String crfId) throws IOException;

    List<Patient> getpatentByUql(String patientSetId, boolean isExport, String projectId, String crfId) throws IOException;

    JSONArray getPatientListByPatientSn(List<GroupData> groupDataList, JSONArray columns, Integer activeType, String projectId, String crfId);

    AjaxObject getPatientListByAllByPatientSetIds(JSONArray patientSetIdTmp, String projectId, JSONArray showColumns, JSONArray actives, Integer pageNum, Integer pageSize, int i, String crfId);

    AjaxObject getPatientSnsByAll(String patientsSetId, String projectId, JSONArray showColumns, JSONArray actives, Integer pageNum, Integer pageSize, Integer type, String crfId) throws IOException;
}
