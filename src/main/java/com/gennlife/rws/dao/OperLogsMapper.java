package com.gennlife.rws.dao;

import java.util.List;
import java.util.Map;

import com.gennlife.rws.entity.OperLogs;
import org.springframework.data.repository.query.Param;

public interface OperLogsMapper {

	void insert(OperLogs record);

	void updateById(OperLogs record);


	List<OperLogs> getOperLogsListBy2(String projectId);
	List<OperLogs> getOperLogsList(Map<String, Object> param );

	int getOperLogsCount(String projectId);
}