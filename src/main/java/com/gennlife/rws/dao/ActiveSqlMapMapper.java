package com.gennlife.rws.dao;

import com.gennlife.rws.entity.ActiveSqlMap;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author liumingxin
 * @create 2018 21 11:17
 * @desc
 **/
public interface ActiveSqlMapMapper {


    void insert(ActiveSqlMap record);

    ActiveSqlMap selectById(Integer id);

    void updateById(ActiveSqlMap record);


    List<String> getSqlByProjectIdAndActiveId(@Param("projectId") String projectId, @Param("activeId") String activeId);

    int getCountByActiveIndexId(@Param("activeId") String activeId,@Param("groupId")String groupId);

    void updateByActiveId(ActiveSqlMap activeSqlMap);

    List<ActiveSqlMap> getActiveSqlMapByProjectId(@Param("projectId") String projectId, @Param("activeId") String activeId,@Param("groupId") String groupId);

    String getActiveSqlByActiveId(@Param("activeId") String activeId,@Param("groupId")String groupId);

    List<ActiveSqlMap> getActiveSql(@Param("activeId") String activeId , @Param("groupId")String groupId);

    Integer getCountByActiveIdAndIndexValue(@Param("activeIndexId") String activeIndexId,@Param("indexResultValue") String indexResultValue , @Param("groupId")String groupId);

    void updateByActiveIdAndIndexValue(ActiveSqlMap activeSqlMap);

    int deleteByActiveIndexId(@Param("activeId") String activeId , @Param("groupId")String groupId);

    Integer getCountByActiveAndsqlMd5(@Param("activeIndexId") String activeIndexId,@Param("sqlMd5") String sqlMd5 , @Param("groupId")String groupId);

    void deleteByTmpActiveId();

    List<String> getGroupIdsByActiveId(String activeIdTmp);

    void deleteByIndexId(@Param("activeId") String activeId);

    List<ActiveSqlMap> getDelRedisActiveSql(@Param("activeId")String activeId);

    List<ActiveSqlMap> getActiveSqlBySqlGroup(@Param("activeId") String activeId , @Param("groupId")String groupId, @Param("patSqlGroup") Integer patSqlGroup);

    List<ActiveSqlMap> getActiveSqlMapByProjectIdAndSqlGroup(@Param("projectId") String projectId, @Param("activeId") String activeId,@Param("groupId") String groupId, @Param("patSqlGroup") Integer patSqlGroup);

    void deleteByActiveIndexIdAndSqlMap(@Param("activeId") String activeId , @Param("groupId")String groupId, @Param("patSqlGroup") Integer patSqlGroup);
}
