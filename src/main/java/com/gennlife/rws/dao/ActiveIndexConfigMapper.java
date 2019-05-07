package com.gennlife.rws.dao;

import com.gennlife.rws.entity.ActiveIndexConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ActiveIndexConfigMapper{
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table active_index_config
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    int deleteByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table active_index_config
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    int insert(ActiveIndexConfig record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table active_index_config
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    int insertSelective(ActiveIndexConfig record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table active_index_config
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    ActiveIndexConfig selectByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table active_index_config
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    int updateByPrimaryKeySelective(ActiveIndexConfig record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table active_index_config
     *
     * @mbggenerated Wed Oct 18 21:17:09 CST 2017
     */
    int updateByPrimaryKey(ActiveIndexConfig record);

    void insertBatch(List<ActiveIndexConfig> configs);

    List<ActiveIndexConfig> findAllByActiveIndexId(String activeIndexId);
    void updateBatch(List<ActiveIndexConfig> configs);
    int countByParam(Map<String, Object> param);
    void deleteByActiveId(String activeIndexId);

    List<String> findActiveIdByConfigId(List<String> configids);

    void updateActiveIdToTemp(String id,String toId);

    String getActiveResult(@Param("activeId") String activeId);

    void deleteByPrimaryKeyOnActiveIndex(@Param("activeIndexConfigId") String activeIndexConfigId);

    List<String> getActiveIndexType(@Param("activeId") String activeId);
}