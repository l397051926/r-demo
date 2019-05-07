package com.gennlife.rws.dao;

import com.gennlife.rws.entity.ContrastiveAnalysisActive;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author liumingxin
 * @create 2018 16 15:09
 * @desc
 **/
public interface ContrastiveAnalysisActiveMapper {
    void insert(ContrastiveAnalysisActive contrastiveAnalysisActive);

    /**
     * 查询所有的 activeIndex id 集合
     * @param uid
     * @param projectId
     * @param cortType
     * @return
     */
    List<String> getActiveIndexes(@Param("createId") String uid, @Param("projectId") String projectId,@Param("cortType") Integer cortType);

    /**
     * 删除 根据 用户id 根据projectId
     * @param createId
     * @param projectId
     */
    void deleteByUidAndProjectId(@Param("createId") String createId, @Param("projectId") String projectId,@Param("cortType") Integer cortType);

    /**
     * 删除研究变量
     * @param paramMap
     */
    void deleteByActiveId(Map<String, Object> paramMap);

    void deleteByActiveIds(@Param("activeId") String activeId);

    void deleteByUidAndProId(@Param("projectId") String projectId, @Param("createId") String uid);
}
