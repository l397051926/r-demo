package com.gennlife.rws.dao;

import com.gennlife.rws.entity.GroupCondition;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GroupConditionMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(GroupCondition record);

    int insertSelective(GroupCondition record);

    GroupCondition selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(GroupCondition record);

    int updateByPrimaryKey(GroupCondition record);

    List<GroupCondition> getGroupByProjectId(@Param("uid") String uid, @Param("projectId") String projectId, @Param("cortType")Integer cortType);

    void deleteByprojectIdAndUid(@Param("uid") String uid,@Param("projectId") String projectId, @Param("groupTypeId") String groupTypeId,@Param("cortType") Integer cortType);

    /**
     * 获取 列种类
     * @param uid
     * @param projectId
     * @return
     */
    List<String> getColNameByUidAndPro(String uid, String projectId);

    void deleteByproIdAndUid(@Param("projectId") String projectId, @Param("uid") String uid);

    List<String> getGroupIdByProjectIdAndUid(@Param("uid") String uid, @Param("projectId") String projectId, @Param("cortType")Integer cortType);
}