package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.ProjectMember;

import java.util.List;

public interface ProjectMemberService {

	/**
	 * 
	 * @author zhengguohui
	 * @description 根据项目ID查询项目成员列表
	 * @date 2018年6月28日
	 * @param JSONObject
	 * @return List<ProjectMember>
	 */
	public List<ProjectMember> getProjectMemberList(JSONObject obj);

	/**
	 * 
	 * @author zhengguohui
	 * @description 根据项目ID查询项目成员
	 * @date 2018年6月28日
	 * @param JSONObject
	 * @return ProjectMember
	 */
	public ProjectMember getProjectMember(JSONObject obj);

	/**
	 * 
	 * @author zhengguohui
	 * @description 保存项目成员信息
	 * @date 2018年6月28日
	 * @param JSONObject
	 */
	public List<ProjectMember> saveProjectMember(JSONObject obj);

	/**
	 *
	 * @author zhengguohui
	 * @description 编辑项目成员信息
	 * @date 2018年6月28日
	 * @param JSONObject
	 */
	public ProjectMember updateProjectMember(JSONObject obj);

	/**
	 *
	 * @author zhengguohui
	 * @description 删除项目成员信息
	 * @date 2018年6月28日
	 * @param JSONObject
	 */
	public void deleteProjectMember(JSONObject obj);

	/**
	 * 根据项目id 获取项目的用户总数
	 * @param object
	 * @return
	 */
	int getProjectMemberCount(JSONObject object);
}
