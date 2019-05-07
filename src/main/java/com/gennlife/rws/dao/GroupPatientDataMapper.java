package com.gennlife.rws.dao;

import java.util.List;

import com.gennlife.rws.entity.GroupPatientData;
import com.gennlife.rws.entity.PatientsSet;
import org.apache.ibatis.annotations.Param;

public interface GroupPatientDataMapper {

	void deleteById(Integer id);

	void insert(GroupPatientData record);

	GroupPatientData selectById(Integer id);

	List<GroupPatientData> selectByPatientSetId(String patientsSetId);

	List<GroupPatientData> selectByGroupId(String groupId);
	/**
	 * 根据患者集 和分组id 去查数量 判断是否重复导入
	 * @param groupId
	 * @param patientsSetId
	 * @return
	 */
	int getPatSetAndGroutId(@Param("groupId") String groupId, @Param("patientsSetId") String patientsSetId);

    int getPatGroupConuntByGroupId(String groupId);

	List<String> getGroupIds(String patientSetId);

	List<String> getPatSetByGroupId(String groupId);

    void deleteByGroupId();
}