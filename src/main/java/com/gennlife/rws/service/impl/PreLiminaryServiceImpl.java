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

    @Override
    public void saveLogMoreData(long curenntCount, String searchCondition, String createId, String createName,
                                String projectId, String patientName, String patientSetId, String uqlQuery, String buildIndex,
                                String crfId, JSONObject esJSon, Integer nowCount, String projectName,String crfName)  {
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
    public void saveInpuTask(Long count, String createId, String projectId, String patientName, String patientSetId, String inputTaskId, String crfId,
                             String crfName, JSONObject esJSon, String uqlQuery) {
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

}
