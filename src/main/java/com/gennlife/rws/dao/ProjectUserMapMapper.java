package com.gennlife.rws.dao;

import java.util.List;

import com.gennlife.rws.entity.ProjectUserMap;
import org.apache.ibatis.annotations.Param;

public interface ProjectUserMapMapper {

	void deleteById(Integer id);

	void deleteByProjectId(String projectId);

	void insert(ProjectUserMap record);

	ProjectUserMap selectById(Integer id);

	void updateById(ProjectUserMap record);

	// 根据用户ID查询数据=对应项目成员表
	ProjectUserMap selectByUid(String uid);

	List<ProjectUserMap> selectByProjectId(String projectId);

	/**
	 * 根据项目id 用户id 获取 要删除的 用户id
	 * @param projectId
	 * @param uid
	 * @return
	 */
	ProjectUserMap selectByUidAndProjectId(String projectId, String uid);

	/**
	 * 根据 项目id 和 用户id 删除用户
	 * @param projectId
	 * @param uid
	 */
    void deleteByUidProjectId(@Param("projectId") String projectId, @Param("uid") String uid);

    void updateByProjectIdAndUid(ProjectUserMap projectUserMap);

    List<String> getUserIds(String projectId);

	Integer selectCountByProjectIdAndUid(@Param("uid") String uid, @Param("projectId") String projectId);

}