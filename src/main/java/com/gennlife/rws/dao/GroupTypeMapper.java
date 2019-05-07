package com.gennlife.rws.dao;

import com.gennlife.rws.entity.GroupType;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface GroupTypeMapper {
	
	int deleteByPrimaryKey(Integer id);

	int insert(GroupType record);

	int insertSelective(GroupType record);

	GroupType selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(GroupType record);

	int updateByPrimaryKey(GroupType record);

	/**
	 * 根据 类型名字获取 id；
	 * 
	 * @param groupType
	 * @return
	 */
	String getGroupType(@Param("groupTypeName") String groupType);

	String getGroupTypeName(@Param("groupTypeId") String groupTypeId);

	List<GroupType> getGroupTypeList(String projectId);

    String getGroupTypeNameByGroupId(String groupId);
}