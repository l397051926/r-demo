package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

/**
 * @author liumingxin
 * @create 2018 28 16:39
 * @desc
 **/
public interface PreLiminaryService {

    String importSampleCheck(JSONObject dataObj, JSONObject userObj);

    void saveLogMoreData(long curenntCount, String searchConditio, String createId, String createName, String projectId, String patientName, String patientSetId,
                         String uqlQuery, String buildIndex, String crfId, JSONObject esJSon, Integer nowCount, String projectName,String crfName) throws IOException;

    void saveInpuTask(Long count, String createId, String projectId, String patientName, String patientSetId, String inputTaskId, String crfId, String crfName, JSONObject esJSon, String uqlQuery);

}
