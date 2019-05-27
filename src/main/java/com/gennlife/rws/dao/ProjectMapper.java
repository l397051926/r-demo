package com.gennlife.rws.dao;

import com.gennlife.rws.entity.Project;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ProjectMapper {

	void insert(Project record);

	Project selectById(Integer id);

	void updateById(Project record);

	/** 根据项目ID 获取项目信息 */
	Project selectByProjectId(String projectId);

	/** 根据用户ID 获取项目列表 */
	List<Project> getProjectList(Map<String, Object> map);

	/**
	 * 根据 数据源 用户 获取数据列表
	 * 
	 * @param uid
	 * @return
	 */
	List<Project> getProjectByUid(@Param("uid") String uid);

	void saveDatasource(@Param("projectId") String projectId, @Param("dataSource") String dataSource,
			@Param("crfId") String crfId);

	/**
	 * 根据项目id 获取 是否是合作项目
	 * 
	 * @param projectId
	 * @return
	 */
	String getCooperIsByProjectId(String projectId);

	/**
	 * 
	 * @author zhengguohui
	 * @description 校验名称是否存在
	 * @date 2018年9月7日
	 */
	int chengName(Map<String, Object> map);

	Integer selectCountByProjectId(String projectId);

    String getProjectNameByProjectId(String projectId);

    List<Project> getProjectAttribute(@Param("userId") String userId, @Param("proIds") List<String> proIds, @Param("projectName") String projectName);

    Integer getCountByProjectIdAndProjectName(@Param("projectId") String projectId, @Param("projectName") String projectName);

    String getCreateIdByTaskId(String taskId);

	void updateCrfId(@Param("projectId") String projectId,@Param("crfId")  String crfId);
}