package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.Group;
import com.gennlife.rws.entity.GroupCondition;
import com.gennlife.rws.util.AjaxObject;

import java.util.List;

public interface GroupService {
    List<Group> getGroupByProjectId(String groupType,String projectId);

    List<GroupCondition> getGroupConditionProjectId(String uid, String projectId, Integer cortType);

    String getGroupNamePath(String groupId, String groupName);

    AjaxObject getGroupIdPath(JSONObject object);

    boolean exportToGroup(JSONArray data, String groupId, String groupName, String projectId, String createId, String createName, boolean b, boolean autoExport);

    void exportToGroupById(JSONArray data, String groupId, String groupName, String id, String projectId, String createId, String createName, boolean b, boolean autoExport);

    void exportToRemoveGroup(JSONArray dataRe, String groupId, String groupName, String id, String projectId, String createId, String createName, boolean b, boolean autoExport);
}
