package com.gennlife.rws.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gennlife.rws.entity.GroupAggregation;
import com.gennlife.rws.entity.GroupData;
import org.apache.ibatis.annotations.Param;

public interface GroupDataMapper {

	void insert(GroupData record);

	GroupData selectByPrimaryKey(GroupData key);

	GroupData selectById(Integer id);

	GroupData findDataByKey(@Param("groupId") String groupId, @Param("patientSn") String patientSn);

	void updateById(GroupData record);

	void deleteByGroupId(String groupId);

	void deleteByPatientSetId(String patientSetId);

	void batchUpdate(List<GroupData> list);

	void batchInsert(List<GroupData> list);

	int allUpdate(@Param("groupId") String groupId);

	List<GroupData> getByGroupId(String groupId);

	List<GroupData> getByPatientSetId(String patientSetId);

	List<String> getPatientSnList(@Param("groupId") String groupId);

	List<GroupAggregation> getPatSetAggregation(Map<String, Object> paramMap);

	int getPatSetAggregationCount(String groupId);

	List<GroupData> getByGroupIdLimit(@Param("groupId") String groupId, @Param("pageNum") Integer pageNum,
			@Param("pageSize") Integer pageSize, @Param("operType") String operType);

	/**
	 * 分组数据根据 患者 编号查找数据 以及分组id
	 * 
	 * @param patientSn
	 * @return
	 */
	String getPatSetNameByPatientSn(@Param("patientSn") String patientSn, @Param("groupId") String groupId);

	int getPatSetCountByGroupIdAndOperType(@Param("groupId") String groupId, @Param("operType") String operType,@Param("willRecoverPatientList") List<String> willRecoverPatientList,@Param("willRemovePatientList") List<String> willRemovePatientList);

	List<GroupData> getByGroupIdLimitByRemove(@Param("groupId") String groupId, @Param("pageNum") Integer pageNum,
											  @Param("pageSize") Integer pageSize, @Param("operType") String operType,@Param("willRecoverPatientList") List<String> willRecoverPatientList,@Param("willRemovePatientList") List<String> willRemovePatientList);

	List<GroupData> getByGroupIdLimitByAll(@Param("groupId") String groupId, @Param("operType") String operType,@Param("willRecoverPatientList") List<String> willRecoverPatientList,@Param("willRemovePatientList") List<String> willRemovePatientList);

    List<GroupData> getPatientSnByGroupIds(@Param("willRecoverGroupIds") List<String> groupIds, @Param("startNum") Integer startNum, @Param("endNum") Integer endNum);

	Integer getPatSetCountBygroupIds(@Param("willRecoverGroupIds") List<String> groupIds);

    List<String> getPatientSnByPatientSetId(@Param("patientsSetId") String patientsSetId);

	void deleteByPatSn(@Param("groupId") String groupId, @Param("patSns") List<String> patSns);

    List<GroupData> getByGroupParentId(String groupId);

	List<String> getPatientSnListByRemove(String groupId);

    List<String> getBrotherPatSns(String groupId);

	List<String> getPatientDocId(String groupId);

	List<String> getAllPatientSnList(String groupId);

    List<GroupData> getPatientSnListAndDocId(String groupId);

    List<String> getPatientSnListByGroupIds(@Param("groupList") List<String> groupList);

	List<GroupData> getGroupDataByPatientSns(@Param("patSns") List<String> patSns);

	String getGroupName(@Param("groupId") String groupId);

    List<String> getPatientDocIdsByPatientSns(@Param("patSns") List<String> patSns);

	Set<String> getPatientSnListsByGroupIds(@Param("willRecoverGroupIds") List<String> groupIds);
}