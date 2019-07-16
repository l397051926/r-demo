package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.PatientsIdSqlMap;
import com.gennlife.rws.util.AjaxObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @author liumingxin
 * @create 2018 13 9:18
 * @desc
 **/
public interface SearchCrfByuqlService {
     String SearchByIndex(JSONObject object, String resultOrderKey, Integer isSearch, PatientsIdSqlMap patientSql, String crfId) throws ExecutionException, InterruptedException, IOException;

    String searchByActive(JSONObject obj, String resultOrderKey, Integer isSearch, PatientsIdSqlMap patientSql, String crfId) throws ExecutionException, InterruptedException, IOException;

     String SearchByExclude(JSONObject object, String resultOrderKey, Integer isSearch, PatientsIdSqlMap patientSql, String crfId) throws ExecutionException, InterruptedException, IOException;

     String SearchByEnume(JSONObject obj, String resultOrderKey, Integer isSearch, PatientsIdSqlMap patientSql, String crfId) throws ExecutionException, InterruptedException, IOException;

    AjaxObject searchClacIndexResultByUql(String activeId, String projectId, Integer pageSize, Integer pageNum, JSONArray basicColumns, String crfId, String groupFromId, JSONArray patientSetId, String groupId, String isVariant) throws IOException, ExecutionException, InterruptedException;
    /**
     * 查询时间结果列表展示
     * @return
     */
    AjaxObject searchCalcResultByUql(String activeId, String projectId, JSONArray basicColumns, JSONArray visitColumns, Integer activeType, Integer pageNum, Integer pageSize, String activeResult, String crfId, String groupFromId, JSONArray patientSetId, String groupId) throws InterruptedException, IOException, ExecutionException;

    AjaxObject searchCalcExculeByUql(String activeId, String projectId, Integer pageSize, Integer pageNum, JSONArray basicColumns, String crfId, String isExport, String groupId, String groupName, JSONArray patientSetId, String createId, String createName, String groupFromId, boolean b) throws IOException, ExecutionException, InterruptedException;

}
