package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.Group;
import com.gennlife.rws.entity.GroupCondition;
import com.gennlife.rws.util.AjaxObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author liumingxin
 * @create 2018 29 9:57
 * @desc 对比分析service
 **/
public interface CortrastiveAnalysisService {
    AjaxObject getPatientGroupCondition(List<Group> groupList, List<GroupCondition> groupConditionList);
    AjaxObject getContResult(String uid, String projectId, Integer cortType, boolean showSubGroup, String crfId, String s) throws ExecutionException, InterruptedException, IOException;

    AjaxObject getContResultForPatient(String createId, String projectId, Integer pageNum, Integer pageSize, JSONArray showColumns, Integer cortType, String crfId, String uid) throws IOException, ExecutionException, InterruptedException;

    AjaxObject saveGroupCondition(String uname, String uid, String projectId, JSONArray groupsIds, Integer cortType, String groupTypeId, String createId);

    Object calculationResult(JSONObject paramObj) throws IOException, ExecutionException, InterruptedException;

    Object calculationResultOne(JSONObject paramObj) throws InterruptedException, ExecutionException, IOException;

    Object snapshootActiveResult(JSONObject paramObj);
}
