package com.gennlife.rws.dao;

import com.gennlife.rws.entity.AggregationModel;
import com.gennlife.rws.entity.GroupAggregation;
import com.gennlife.rws.entity.PatientsSet;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PatientsSetMapper {

	// 根据项目id删除
	void deleteByProjectID(String projectId);

	void insert(PatientsSet record);

	PatientsSet selectById(Integer id);

	void updateById(PatientsSet record);

	PatientsSet selectByPatSetId(String patientsSetId);

	List<PatientsSet> getPatientsSetList(Map<String, Object> map);

	/**
	 * 根据 项目id 获取患者集
	 * 
	 * @param projectId
	 * @return
	 */
	List<PatientsSet> getPatientSetByProjectId(String projectId);

	/**
	 * 根据患者集id 获取患者集名字
	 * @param patientsSetId
	 * @return
	 */
    String getpatientSetNameByPatSetId(String patientsSetId);

	void updatePatientsCountAndQuery(@Param("patientSetId") String patientSetId, @Param("curenntCount") long curenntCount, @Param("uqlQuery") String uqlQuery,@Param("isFlush") Integer isFlush);

    Integer getPatientSetCount(String patientsSetId);

	String getPatientsetSql(String patientSetId);

    List<String> getPatientSetSqlByGroupId(String groupId);

	List<String> getPatientsetSqlAll(@Param("patientSets") List<String> patientSets);

	List<String> getpatientSetNameByPatSetIds(@Param("patientSetIds") List<String> patientSetIds);

    List<PatientsSet> getPatientSet(String groupId);

    List<AggregationModel> getPatientSetAggreagation(String projectId);

	Integer getPatientSetCountByProjectId(String projectId);

    Integer getSumCount(String projectId);

    Integer getCountByProjectIdAndPatientsetName(@Param("projectId") String projectId,@Param("patientsSetName") String patientsSetName);

	Integer getcountByPatIdAndPatName(@Param("patientSetId") String patientSetId,@Param("patientsSetName") String patientsSetName);

	void updateIsFlush(@Param("isFlush") Integer isFlush, @Param("patientSetId") String patientSetId);

	List<PatientsSet> getPatientSetByProjectIdRemoveQuery(String projectId);

}