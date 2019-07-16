package com.gennlife.rws.dao;

import com.gennlife.rws.entity.Group;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface GroupMapper {

    void deleteByProjectId(String projectId);

    void insert(Group record);

    Group selectById(Integer id);

    void updateById(Group record);

    Group selectByGroupId(String groupId);

    List<Group> getGroupList(Map<String, Object> map);

    List<Group> getGroupByProjectId(@Param("groupTypeId") String groupTypeId, @Param("projectId") String projectId);

    List<Group> getGroupListByProjectId(@Param("projectId") String projectId);

    List<Group> getGroupByTypeId(@Param("projectId") String projectId, @Param("groupTypeId") String groupTypeId);

    int getMaxLevelebyProjectId(@Param("projectId") String projectId);

    String getQuerySearch(String groupId);

    String getProjectId(String groupId);

    List<Group> getgroupChildIds(String groupId);

    Group getGroupByGroupId(String groupId);

    Group getGroupByGroupParentId(String groupId);

    String getGroupParentId(String groupToId);

    Integer selectCountByGroupId(String groupId);

    List<Group> getGroupListByParentId(String groupId);

    String getGroupNameByGroupId(String groupId);

    List<String> getGroupByProjectIdAndUid(@Param("projectId") String projectId, @Param("createId") String createId);

    Group getpGroupParentName(String groupId);

    Integer selectCountByGroupIdAndUid(@Param("groupId") String groupId, @Param("uid") String uid);

    List<Group> getGroupListByGroupCondition(@Param("uid") String uid, @Param("projectId") String projectId, @Param("cortType") Integer cortType);

    List<Group> getGroupListByGroupIds(List<String> groupList);

    List<String> getGroupIdsByProjectId(@Param("projectId") String projectId);
}