package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.packagingservice.testUtils.HttpRequestUtils;
import com.gennlife.rws.content.InputStratus;
import com.gennlife.rws.content.RedisContent;
import com.gennlife.rws.dao.InputTaskMapper;
import com.gennlife.rws.dao.ProjectMapper;
import com.gennlife.rws.entity.InputTask;
import com.gennlife.rws.entity.ResultBean;
import com.gennlife.rws.service.PreLiminaryService;
import com.gennlife.rws.service.RedisMapDataService;
import com.gennlife.rws.util.HttpUtils;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;

/**
 * @author liumingxin
 * @create 2018 28 16:39
 * @desc
 **/
@Service
@Transactional(rollbackFor = RuntimeException.class)
public class PreLiminaryServiceImpl implements PreLiminaryService {
    private static final Logger logger = LoggerFactory.getLogger(PreLiminaryServiceImpl.class);

    @Autowired
    private HttpUtils httpUtils;
    @Autowired
    private InputTaskMapper inputTaskMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private RedisMapDataService redisMapDataService;



    @Override //导出权限校验 日志存储 数据记录
    public String importSampleCheck(JSONObject dataObj, JSONObject userObj) {
        JSONObject query = dataObj.getJSONObject("query");
        if (dataObj.containsKey("crfId")) {
            ResultBean resultBean = new ResultBean();
            JsonObject data = new JsonObject();
            data.addProperty("next", true);
            data.addProperty("export", false);
            data.addProperty("sub", 0);
            resultBean.setCode(1);
            resultBean.setData(data);
            return JSONObject.toJSONString(resultBean);
        }
        JSONObject power = dataObj.getJSONObject("power");
        if (dataObj.containsKey("groups")) {
            JSONArray groups = dataObj.getJSONArray("groups");
            query.put("groups", groups);
        }

        query.put("power", power);
        logger.info("原始搜索天骄 = " + query);

        boolean next = true;
        boolean export = false;
        int sub = 0;
        if (query.containsKey("sid")) {
            next = false;
            String querySid = query.getString("sid");
            JSONArray searchArray = power.getJSONArray("has_searchExport");
            int size = searchArray == null ? 0 : searchArray.size();
            for (int i = 0; i < size; i++) {
                JSONObject obj = searchArray.getJSONObject(i);
                if (obj.getString("sid").equals(querySid)) {
                    next = true;
                    export = true;
                    break;
                }
            }
        } else {
            JSONObject queryNew = transformSidForImport(query, userObj);
            if (queryNew.containsKey("code") && queryNew.getInteger("code") == 0) {
                return queryNew.toJSONString();
            }
            queryNew.put("indexName", httpUtils.getSearchIndexName());//////////////////////
            queryNew.put("size", 1);
            queryNew.getJSONArray("source").add("patient_info.PATIENT_SN");
            String url = httpUtils.getEsServiceUrl();

            logger.info("data_01 处理后导出条件=" + queryNew.toJSONString());
            //查找数据
            String data_01 = HttpRequestUtils.httpPost(url, queryNew.toJSONString());
            logger.info("data_01=" + data_01);

            JSONArray searchExport = power.getJSONArray("has_searchExport");

            int size = searchExport == null ? 0 : searchExport.size();
            for (int i = 0; i < size; i++) {
                JSONObject obj = searchExport.getJSONObject(i);
                obj.put("has_search", obj.getString("has_searchExport"));
                obj.remove("has_searchExport");
            }
            JSONArray search = power.getJSONArray("has_search");
            int size1 = search == null ? 0 : search.size();

            for (int i = 0; i < size1; i++) {
                JSONObject obj = search.getJSONObject(i);
                obj.put("has_searchExport", obj.getString("has_search"));
                obj.remove("has_search");
            }
            queryNew.getJSONObject("power").put("has_search", searchExport);
            queryNew.getJSONObject("power").put("has_searchExport", search);
            logger.info("data_02 处理后导出条件=" + queryNew.toJSONString());

            //查找数据
            String data_02 = HttpRequestUtils.httpPost(url, queryNew.toJSONString());
            logger.info("data_02=" + data_02);

            JSONObject hits_01 = JSONObject.parseObject(data_01).getJSONObject("hits");
            JSONObject hits_02 = JSONObject.parseObject(data_02).getJSONObject("hits");

            logger.info("total========= " + hits_01.getInteger("count") + " ======== " + hits_02.getInteger("count"));
            int count = hits_01.getInteger("total") - hits_02.getInteger("count");
            if (count > 0) {
                next = false;
                export = true;
                sub = count;
            }
        }

        ResultBean resultBean = new ResultBean();
        JsonObject data = new JsonObject();
        data.addProperty("next", next);
        data.addProperty("export", export);
        data.addProperty("sub", sub);
        resultBean.setCode(1);
        resultBean.setData(data);
        return JSONObject.toJSONString(resultBean);
    }

    @Override
    public void saveLogMoreData(long curenntCount, String searchCondition, String createId, String createName,
                                String projectId, String patientName, String patientSetId, String uqlQuery, String buildIndex, String crfId, JSONObject esJSon, Integer nowCount, String projectName,String crfName) throws IOException {
        saveInpuTask(curenntCount,createId,projectId,patientName,patientSetId,buildIndex,crfId,crfName,esJSon,uqlQuery);

        JSONObject object = new JSONObject()
                                    .fluentPut("curenntCount",curenntCount)
                                    .fluentPut("searchCondition",searchCondition)
                                    .fluentPut("createId",createId)
                                    .fluentPut("createName",createName)
                                    .fluentPut("projectId",projectId)
                                    .fluentPut("patientName",patientName)
                                    .fluentPut("patientSetId",patientSetId)
                                    .fluentPut("uqlQuery",uqlQuery)
                                    .fluentPut("buildIndex",buildIndex)
                                    .fluentPut("crfId",crfId)
                                    .fluentPut("esJSon",esJSon)
                                    .fluentPut("nowCount",nowCount)
                                    .fluentPut("projectName",projectName)
                                    .fluentPut("crfName",crfName);

        redisMapDataService.set(RedisContent.getRwsService(buildIndex), object.toJSONString());


    }

    @Override
    public void saveInpuTask(Long count, String createId, String projectId, String patientName, String patientSetId, String inputTaskId, String crfId, String crfName, JSONObject esJSon, String uqlQuery) {
        String projectName = projectMapper.getProjectNameByProjectId(projectId);
        InputTask inputTask = new InputTask();
        inputTask.setInputId(inputTaskId);
        inputTask.setProjectId(projectId);
        inputTask.setProjectName(projectName);
        inputTask.setPatientSetId(patientSetId);
        inputTask.setPatientSetName(patientName);
        inputTask.setUid(createId);
        inputTask.setPatientCount(count);
        inputTask.setCreateTime(new Date());
        inputTask.setStartTime(new Date());
        inputTask.setStatus(InputStratus.IN_QUEUE);
        inputTask.setUpdateTime(new Date());
        inputTask.setCrfId(crfId);
        inputTask.setCrfName(crfName);
        inputTask.setEsJson(esJSon.toJSONString());
        inputTask.setUqlQuery(uqlQuery);
        inputTaskMapper.insert(inputTask);
    }

    private JSONObject transformSidForImport(JSONObject query, JSONObject userObj) {
        if (query.containsKey("sid") && query.containsKey("power")) {
            String sid = query.getString("sid");
            query.remove("groups");
            query.remove("sid");
            JSONObject power = query.getJSONObject("power");
            JSONArray has_searchExportArray = power.getJSONArray("has_searchExport");
            JSONArray newhas_searchExportArray = new JSONArray();
            JSONArray has_searchArray = power.getJSONArray("has_search");
            JSONArray newHas_searchArray = new JSONArray();

            int size = has_searchExportArray == null ? 0 : has_searchExportArray.size();
            for (int i = 0; i < size; i++) {
                JSONObject has_searchExportObj = has_searchExportArray.getJSONObject(i);
                String tmpSid = has_searchExportObj.getString("sid");
                if (tmpSid.equals(sid)) {
                    newhas_searchExportArray.add(has_searchExportObj);
                }
            }

            int size1 = has_searchArray == null ? 0 : has_searchArray.size();
            for (int i = 0; i < size1; i++) {
                JSONObject has_searchObj = has_searchArray.getJSONObject(i);
                String tmpSid = has_searchObj.getString("sid");
                if (tmpSid.equals(sid)) {
                    newHas_searchArray.add(has_searchObj);
                }
            }
            power.put("has_search", newHas_searchArray);
            power.put("has_searchExport", newhas_searchExportArray);
            query.put("power", power);
            logger.info("通过sid 转化后,搜索请求参数 " + query);
            return query;
        } else if (query.containsKey("power")) {
            return query;
        } else {
            return query;
        }
    }

}
