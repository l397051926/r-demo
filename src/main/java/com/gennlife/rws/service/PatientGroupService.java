package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.Group;
import com.gennlife.rws.entity.GroupType;
import com.gennlife.rws.util.AjaxObject;

import java.io.IOException;
import java.util.List;

public interface PatientGroupService {
	/**
	 * @author zhengguohui
	 * @description 保存分组和患者的对应关系
	 * @date 2018年7月2日
	 * @param obj
	 */
	public Integer saveGroupAndPatient(JSONObject obj) throws IOException;

	/**
	 * @author zhengguohui
	 * @description 根据项目ID查询患者分组
	 * @date 2018年6月29日
	 * @param param
	 * @return List<PatientsGroup>
	 */
	public List<Group> getPatientGroupList(JSONObject obj);

	/**
	 *
	 * @author zhengguohui
	 * @description 根据患者分组ID查询患者分组信息
	 * @date 2018年6月29日
	 * @param param
	 * @return PatientsGroup
	 */
	public Group getPatientGroup(JSONObject obj);

	/**
	 *
	 * @author zhengguohui
	 * @description 保存患者分组信息
	 * @date 2018年6月29日
	 * @param param
	 */
	public Group savePatientGroup(JSONObject obj);

	/**
	 * @author zhengguohui
	 * @description 进一步筛选 保存按钮 保存分组和患者的对应关系
	 * @date 2018年7月5日
	 * @param obj
	 */
	public String insertGroupDataPatient(JSONObject obj);

	/**
	 * 
	 * @author zhengguohui
	 * @description 导入本组按钮 保存分组和患者的对应关系
	 * @date 2018年7月13日
	 * @param obj
	 *            void
	 */
//	public void exportGroupDataPatient(JSONObject obj);

	/**
	 *
	 * @author zhengguohui
	 * @description 编辑患者分组信息
	 * @date 2018年6月29日
	 * @param param
	 */
	public Group updatePatientGroup(JSONObject obj);

	/**
	 *
	 * @author zhengguohui
	 * @description 删除患者分组信息
	 * @date 2018年6月29日
	 * @param param
	 */
	public void deletePatientGroup(JSONObject obj);

	AjaxObject getPatientList(JSONObject object) throws IOException;

	/**
	 * @author zhengguohui
	 * @description 查询科研类型所对应的分组
	 * @date 2018年8月16日
	 * @param projectId
	 * @return List<GroupType>
	 */
	List<GroupType> getGroupTypeList(JSONObject obj);

	/**
	 * 查找组里 显示数据结果统计表盘
	 * @param object
	 */
	AjaxObject groupAggregation(JSONObject object);

    AjaxObject getGroupParentData(JSONObject object);

	AjaxObject getGroupCountTypeList(JSONObject object,List<GroupType> list);

	AjaxObject getActiveIndexByGroup(String activeId);

	AjaxObject getPatientSearchActive(String groupId);
}
