package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.util.AjaxObject;

import java.io.IOException;

/**
 * Created by liuzhen.
 * Date: 2017/10/23
 * Time: 14:44
 */
public interface DownLoadService {

    public AjaxObject findTotalByActiveIdAndProjectId(String projectId, String activeId, Integer type);

    String buildIndex(JSONObject esJSon, String projectId, String crfId, String createId);

    AjaxObject sysBuildIndex(DownLoadService downLoadService, String patientSetId, JSONObject esJSon, String crfId, String createId, String createName, String patientName, String projectId, String uqlQuery, String projectName, String crfName) throws IOException;
}
