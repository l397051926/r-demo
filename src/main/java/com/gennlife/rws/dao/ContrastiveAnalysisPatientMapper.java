package com.gennlife.rws.dao;

import com.gennlife.rws.entity.ContrastiveAnalysisPatient;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ContrastiveAnalysisPatientMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ContrastiveAnalysisPatient record);

    int insertSelective(ContrastiveAnalysisPatient record);

    ContrastiveAnalysisPatient selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ContrastiveAnalysisPatient record);

    int updateByPrimaryKey(ContrastiveAnalysisPatient record);

    /**
     * 根据 用户id 和项目id 获取 列名列表 去重 排序
     * @param uid
     * @param projectId
     * @return
     */
    List<String> getColNameByProjectId(@Param("uid") String uid,@Param("projectId") String projectId);

    List<ContrastiveAnalysisPatient> getContrastiveByUidAndPro(@Param("uid") String uid,@Param("projectId") String projectId ,@Param("startNum")Integer startNum,@Param("endNum") Integer endNum);

    /**
     * 求最大 row Num
     * @param uid
     * @param projectId
     * @return
     */
    Integer getMaxRowNum(@Param("uid") String uid,@Param("projectId") String projectId);
}