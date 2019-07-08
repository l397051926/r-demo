package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.Group;
import com.gennlife.rws.entity.GroupType;
import com.gennlife.rws.util.AjaxObject;

import java.io.IOException;
import java.util.List;

public interface PatientGroupService {
    /**
     * @param obj
     * @author zhengguohui
     * @description 保存分组和患者的对应关系
     * @date 2018年7月2日
     */
    Integer saveGroupAndPatient(JSONObject obj) throws IOException;

    /**
     * @param param
     * @return List<PatientsGroup>
     * @author zhengguohui
     * @description 根据项目ID查询患者分组
     * @date 2018年6月29日
     */
    List<Group> getPatientGroupList(JSONObject param);

    /**
     * @param param
     * @return PatientsGroup
     * @author zhengguohui
     * @description 根据患者分组ID查询患者分组信息
     * @date 2018年6月29日
     */
    Group getPatientGroup(JSONObject param);

    /**
     * @param param
     * @author zhengguohui
     * @description 保存患者分组信息
     * @date 2018年6月29日
     */
    Group savePatientGroup(JSONObject param);

    /**
     * @param obj
     * @author zhengguohui
     * @description 进一步筛选 保存按钮 保存分组和患者的对应关系
     * @date 2018年7月5日
     */
    String insertGroupDataPatient(JSONObject obj);

    /**
     * @param param
     * @author zhengguohui
     * @description 编辑患者分组信息
     * @date 2018年6月29日
     */
    Group updatePatientGroup(JSONObject param);

    /**
     * @param param
     * @author zhengguohui
     * @description 删除患者分组信息
     * @date 2018年6月29日
     */
    void deletePatientGroup(JSONObject param);

    AjaxObject getPatientList(JSONObject object) throws IOException;

    /**
     * @param param
     * @return List<GroupType>
     * @author zhengguohui
     * @description 查询科研类型所对应的分组
     * @date 2018年8月16日
     */
    List<GroupType> getGroupTypeList(JSONObject param);

    /**
     * 查找组里 显示数据结果统计表盘
     *
     * @param object
     */
    AjaxObject groupAggregation(JSONObject object);

    AjaxObject getGroupParentData(JSONObject object);

    AjaxObject getGroupCountTypeList(JSONObject object, List<GroupType> list);

    AjaxObject getPatientSearchActive(String groupId);
}
