package com.gennlife.rws.dao;

import com.gennlife.rws.entity.ProjectMember;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ProjectMemberMapper {

	void insert(ProjectMember record);

	ProjectMember selectById(Integer id);

	void updateById(ProjectMember record);

	// 根据项目ID查询项目成员列表 和项目成员对应表联合查询
	public List<ProjectMember> getUserProjectList(String projectId);

	int getUserProjectCount(String projectId);

	List<ProjectMember> getUserProjectListByLimit(Map<String, Object> mapParam);

	/**
	 * 获取负责人数量
	 * @param projectId
	 * @return
	 */
	Integer getCountPrincipal(@Param("projectId") String projectId,@Param("obligId")String obligId);
}