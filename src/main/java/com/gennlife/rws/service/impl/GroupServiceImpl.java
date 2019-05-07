package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.dao.*;
import com.gennlife.rws.entity.Group;
import com.gennlife.rws.entity.GroupCondition;
import com.gennlife.rws.entity.GroupData;
import com.gennlife.rws.entity.GroupPatientData;
import com.gennlife.rws.service.GroupService;
import com.gennlife.rws.util.AjaxObject;
import com.gennlife.rws.util.LogUtil;
import com.gennlife.rws.util.StringUtils;
import com.gennlife.rws.vo.DelFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author liumingxin
 * @create 2018 29 10:08
 * @desc
 **/
@Service
@Transactional(rollbackFor = RuntimeException.class)
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private GroupTypeMapper groupTypeMapper;
    @Autowired
    private GroupConditionMapper groupConditionMapper;
    @Autowired
    private GroupDataMapper groupDataMapper;
    @Autowired
    private LogUtil logUtil;
    @Autowired
    private GroupPatientDataMapper groupPatDataMapper;
    private static final int exportMax = 2000;

    @Override
    public List<Group> getGroupByProjectId(String groupType,String projectId) {

        //获取 分组类型id
        String groupTypeId = groupTypeMapper.getGroupType(groupType);
        //根据分组类型id 和project Id 项目数量>0 获取 分组数据
        List<Group> groupList = groupMapper.getGroupListByProjectId(projectId);
        List<Group> resultGroup = new ArrayList<>();
        for (Group group : groupList) {
            String groupId = group.getGroupId();
            int count = groupDataMapper.getPatSetAggregationCount(groupId);
            if(count>0){
                resultGroup.add(group);
            }
        }
        return resultGroup;
    }

    @Override
    public List<GroupCondition> getGroupConditionProjectId(String uid, String projectId, Integer cortType) {

        return groupConditionMapper.getGroupByProjectId(uid,projectId,cortType);
    }

    @Override
    public String getGroupNamePath(String groupId, String groupName) {
        List<String> groupNamePathList = new ArrayList<>();
        groupNamePathList.add(groupName);
        getGroupNamePathList(groupId,groupNamePathList);
        return String.join("-",groupNamePathList);
    }

    @Override
    public AjaxObject getGroupIdPath(JSONObject object) {
        String projectId = object.getString("projectId");
        String uid = object.getString("userId");
        String gId = object.getString("groupId");
        JSONObject data = new JSONObject();
        if (StringUtils.isNotEmpty(gId)){
            String groupName = groupMapper.getGroupNameByGroupId(gId);
            String val = getGroupNamePath(gId,groupName);
            data.put(gId,val);
        }else {
            List<Group> groupList = groupMapper.getGroupListByGroupCondition(uid,projectId,2);
            for (Group group : groupList){
                String groupId = group.getGroupId();
                String val = getGroupNamePath(groupId,group.getGroupName());
                data.put(groupId,val);
            }
        }
        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,AjaxObject.AJAX_MESSAGE_SUCCESS);
        ajaxObject.setData(data);
        return ajaxObject;
    }

    private void getGroupNamePathList(String groupId, List<String> groupNamePathList) {
        Group group = groupMapper.getpGroupParentName(groupId);
        if(group == null){
            String name = groupTypeMapper.getGroupTypeNameByGroupId(groupId);
            groupNamePathList.add(0,name);
        }else {
            groupNamePathList.add(0,group.getGroupName());
            getGroupNamePathList(group.getGroupId(),groupNamePathList);
        }
    }

    @Override
    public boolean exportToGroup(JSONArray data, String groupId, String groupName, String projectId, String createId, String createName, boolean b, boolean autoExport) {
        List<List<GroupData>> list = new ArrayList<>();
        List<GroupData> listdata = new ArrayList<>();

        List<String> patSns = data.stream().map(JSONObject.class ::cast).map(o -> o.getString("PATIENT_SN")).collect(toList());
        List<String> broPatSns = groupDataMapper.getBrotherPatSns(groupId);
        boolean isExport = isConflictedPatients(patSns,broPatSns);
        if(isExport) return false;
        if(!autoExport){
            groupDataMapper.deleteByGroupId(groupId);
        }

        int size = data.size();
        for (int i = 0; i < size; i++) {
            if(i != 0 &&  i % exportMax == 0  ){
                list.add(listdata);
                listdata = new ArrayList<>();
            }
            JSONObject patient = data.getJSONObject(i);
            GroupData groupData = new GroupData();
            groupData.setGroupId(groupId);
            groupData.setPatientSn(patient.getString("PATIENT_SN"));
            groupData.setEfhnic(patient.getString("ETHNIC"));
            groupData.setNationality(patient.getString("NATIONALITY"));
            groupData.setMaritalStatus(patient.getString("MARITAL_STATUS"));
            groupData.setGender(patient.getString("GENDER"));
            groupData.setPatientDocId(patient.getString("DOC_ID"));
            groupData.setRemove(DelFlag.AVIABLE.toString());
            groupData.setCreateTime(new Date());
            groupData.setUpdateTime(new Date());
            listdata.add(groupData);
        }
        list.add(listdata);
        for (List<GroupData> li : list){
            if (li != null && !li.isEmpty()) {
                groupDataMapper.batchInsert(li);
            }
        }
        // 将患者集ID 映射成患者集名称
        if(!autoExport){
            String content = createName + "通过精细筛选更新组" + groupName+" 中患者";
            logUtil.saveLog(projectId, content, createId, createName);
        }
        return  true;

    }

    @Override
    public void exportToGroupById(JSONArray data, String groupId, String groupName,String patientsSetId,String projectId,String createId,String createName,boolean isExport,boolean autoExport) {
        List<List<GroupData>> list = new ArrayList<>();
        List<GroupData> listdata = new ArrayList<>();

        int count = groupPatDataMapper.getPatSetAndGroutId(groupId, patientsSetId);
        String patients = "";

        GroupPatientData gpData = new GroupPatientData();
        gpData.setGroupId(groupId);
        gpData.setPatientSetId(patientsSetId);
        if(count>0){
            groupPatDataMapper.deleteByGroupId();
        }else {
            groupPatDataMapper.insert(gpData);
        }
        if(!autoExport){
            groupDataMapper.deleteByGroupId(groupId);
        }
        int size = data.size();
        for (int i = 0; i < size; i++) {
            if(i != 0 &&  i % exportMax == 0  ){
                list.add(listdata);
                listdata = new ArrayList<>();
            }

            JSONObject patient = data.getJSONObject(i);
            GroupData groupData = new GroupData();
            groupData.setPatientSetId(patientsSetId);
            groupData.setGroupId(groupId);
            groupData.setPatientSn(patient.getString("PATIENT_SN"));
            groupData.setEfhnic(patient.getString("ETHNIC"));
            groupData.setNationality(patient.getString("NATIONALITY"));
            groupData.setMaritalStatus(patient.getString("MARITAL_STATUS"));
            groupData.setGender(patient.getString("GENDER"));
            groupData.setPatientDocId(patient.getString("DOC_ID"));
            groupData.setRemove(DelFlag.AVIABLE.toString());
            groupData.setCreateTime(new Date());
            groupData.setUpdateTime(new Date());
            listdata.add(groupData);
        }
        list.add(listdata);
        for (List<GroupData> li : list){
            if (li != null && !li.isEmpty()) {
                groupDataMapper.batchInsert(li);
            }
        }

        // 将患者集ID 映射成患者集名称
        if(!autoExport){
            String content = createName + "将患者集： " + patients + " 添加到组:" + groupName;
            logUtil.saveLog(projectId, content, createId, createName);
        }

    }

    @Override
    public void exportToRemoveGroup(JSONArray data, String groupId, String groupName,String patientsSetId,String projectId,String createId,String createName,boolean isExport,boolean autoExport) {
        List<List<GroupData>> list = new ArrayList<>();
        List<GroupData> listdata = new ArrayList<>();

        int size = data.size();
        for (int i = 0; i < size; i++) {
            if(i != 0 &&  i % exportMax == 0  ){
                list.add(listdata);
                listdata = new ArrayList<>();
            }
            JSONObject patient = data.getJSONObject(i);
            GroupData groupData = new GroupData();
            groupData.setPatientSetId(patientsSetId);
            groupData.setGroupId(groupId);
            groupData.setPatientSn(patient.getString("PATIENT_SN"));
            groupData.setEfhnic(patient.getString("ETHNIC"));
            groupData.setNationality(patient.getString("NATIONALITY"));
            groupData.setMaritalStatus(patient.getString("MARITAL_STATUS"));
            groupData.setGender(patient.getString("GENDER"));
            groupData.setPatientDocId(patient.getString("DOC_ID"));
            groupData.setRemove(DelFlag.LOSE.toString());
            groupData.setCreateTime(new Date());
            groupData.setUpdateTime(new Date());
            listdata.add(groupData);
        }
        list.add(listdata);
        for (List<GroupData> li : list){
            if (li != null && !li.isEmpty()) {
                groupDataMapper.batchInsert(li);
            }
        }
    }

    public static boolean isConflictedPatients(List<String> patients1 ,List<String> patients2){
        for (String patSn1 :patients1){
            if(patients2.contains(patSn1)){
                return  true;
            }
        }
        return false;
    }
}
