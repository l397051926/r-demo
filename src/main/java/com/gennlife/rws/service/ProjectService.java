package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.OperLogs;
import com.gennlife.rws.entity.Project;
import com.gennlife.rws.util.AjaxObject;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author liumingxin
 * @create 2018 28 15:06
 * @desc
 **/
public interface ProjectService {

	/**
	 * @author zhengguohui
	 * @description 根据用户ID查询项目集合
	 * @date 2018年6月27日
	 * @param param
	 * @return List<Project>
	 */
	public List<Project> getProjectList(JSONObject param);

	/**
	 *
	 * @author zhengguohui
	 * @description 根据项目ID查询项目信息
	 * @date 2018年6月27日
	 * @param param
	 * @return Project
	 */
	public Project getProject(JSONObject param);

	/**
	 *
	 * @author zhengguohui
	 * @description 保存项目信息
	 * @date 2018年6月27日
	 * @param param
	 */
	public Project saveProject(JSONObject param);

	/**
	 *
	 * @author zhengguohui
	 * @description 编辑项目信息
	 * @date 2018年6月27日
	 * @param param
	 */
	public Project updateProject(JSONObject param);

	/**
	 *
	 * @author zhengguohui
	 * @description 删除项目信息
	 * @date 2018年6月27日
	 * @param param
	 */
	public void deleteProject(JSONObject param);

	/**
	 * 根据crfid 以及 uid 获取数据
	 * 
	 * @param paramObj
	 * @return
	 */
	List<Project> getProjectListByCrfId(JSONObject paramObj);

	/**
	 * 保存 dasource
	 * 
	 * @param crfId
	 * @param crfName
	 */
	void saveDatasource(String projectId, String crfId, String crfName);

	/**
	 * @author zhengguohui
	 * @description 查询项目(日志)动态
	 * @date 2018年7月10日
	 * @return List<OperLogs>
	 */
	List<OperLogs> getOperLogsList(JSONObject param);

	int getOperLogsCount(JSONObject object);

	/**
	 * @author zhengguohui
	 * @description 检验名称是否存在
	 * @date 2018年9月7日
	 * @param object
	 * @return int
	 */
	String checkNameType(JSONObject object);

    AjaxObject getprojectAggregation(JSONObject object);

    AjaxObject projectPowerExamine(JSONObject object);

    Object getProjectAttribute(JSONObject object);

    Object eligible(JSONObject object);

    void deleteProjectDelIndex();

    void deleteProjectIndex(String projectId, String crfId);

    Integer getCountByProjectIdAndProjectName(String projectId, String projectName);

    AjaxObject getCortastivePatientSn(JSONObject object);
}
