package com.gennlife.rws.dao;

import java.util.List;

import com.gennlife.rws.entity.ScientificGroupMap;

public interface ScientificGroupMapMapper {

	void deleteByPrimaryKey(Integer id);

	void insert(ScientificGroupMap record);

	ScientificGroupMap selectByPrimaryKey(Integer id);

	void update(ScientificGroupMap record);

	List<ScientificGroupMap> getSciGroupByProject(String projectId);
}