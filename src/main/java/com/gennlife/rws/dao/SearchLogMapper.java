package com.gennlife.rws.dao;

import com.gennlife.rws.entity.SearchLog;

import java.util.List;

public interface SearchLogMapper {

	int deleteByPrimaryKey(Integer id);

	int insert(SearchLog record);

	int insertSelective(SearchLog record);

	SearchLog selectByPrimaryKey(Integer id);

	int update(SearchLog record);

	List<SearchLog> selectByPrtisntId(String patientSetId);

    List<SearchLog> selectBypatientSetIds(List<String> patientSetData);

	String getSearchLog(String patientSetId);
}