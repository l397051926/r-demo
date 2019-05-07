package com.gennlife.rws.service;


import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.util.AjaxObject;

public interface InputTaskService {

    AjaxObject getAllInputTasks(JSONObject object);

    AjaxObject deleteInputTask(JSONObject object);

    AjaxObject restartInputTask(JSONObject object);

    AjaxObject cencelInputTasks(JSONObject object);

    void cencelInputTasksOnDelPatSet(String patientsSetId,String userId,String projectId,String projectName);

    void updateCencelDate(String taskId);

    Object judgeInputTaskStatus(JSONObject object);

    void cencelInputTasksOnDelProject(String userId, String projectId, String projectName);

    Object decideInputs(JSONObject object);
}
