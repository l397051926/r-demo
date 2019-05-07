package com.gennlife.rws.dao;

import com.gennlife.rws.entity.ProjectScientificMap;

public interface ProjectScientificMapMapper {

	void insert(ProjectScientificMap record);

	ProjectScientificMap selectById(Integer id);

	void updateById(ProjectScientificMap record);
	
	void deleteByProjectId(String projectId);

	ProjectScientificMap selectByProjectId(String projectid);



}