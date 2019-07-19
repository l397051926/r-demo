package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.content.InputStratus;
import com.gennlife.rws.content.UqlConfig;
import com.gennlife.rws.dao.InputTaskMapper;
import com.gennlife.rws.dao.ProjectMapper;
import com.gennlife.rws.entity.InputTask;
import com.gennlife.rws.service.PreLiminaryService;
import com.gennlife.rws.service.RedisMapDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private InputTaskMapper inputTaskMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private RedisMapDataService redisMapDataService;

    @Override
    public void saveLogMoreData(long curenntCount, String searchCondition, String createId, String createName,
                                String projectId, String patientName, String patientSetId, String buildIndex,
                                String crfId, JSONObject esJSon, Integer nowCount, String projectName, String crfName) {
        saveInpuTask(curenntCount, createId, projectId, patientName, patientSetId, buildIndex, crfId, crfName, esJSon);

        JSONObject object = new JSONObject()
            .fluentPut("curenntCount", curenntCount)
            .fluentPut("searchCondition", searchCondition)
            .fluentPut("createId", createId)
            .fluentPut("createName", createName)
            .fluentPut("projectId", projectId)
            .fluentPut("patientName", patientName)
            .fluentPut("patientSetId", patientSetId)
            .fluentPut("buildIndex", buildIndex)
            .fluentPut("crfId", crfId)
            .fluentPut("esJSon", esJSon)
            .fluentPut("nowCount", nowCount)
            .fluentPut("projectName", projectName)
            .fluentPut("crfName", crfName);
        redisMapDataService.set(UqlConfig.getRwsService(buildIndex), object.toJSONString());
        logger.debug("导入数据成功");


    }

    @Override
    public void saveInpuTask(Long count, String createId, String projectId, String patientName, String patientSetId, String inputTaskId, String crfId,
                             String crfName, JSONObject esJSon) {
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
        inputTaskMapper.insert(inputTask);
    }

}
