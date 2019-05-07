package com.gennlife.rws.dao;

import com.gennlife.rws.entity.ContrastiveAnalysisCount;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ContrastiveAnalysisCountMapper {
	
    int deleteByPrimaryKey(Integer id);

    int insert(ContrastiveAnalysisCount record);

    int insertSelective(ContrastiveAnalysisCount record);

    ContrastiveAnalysisCount selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ContrastiveAnalysisCount record);

    int updateByPrimaryKey(ContrastiveAnalysisCount record);

    List<ContrastiveAnalysisCount> getContrastiveByUidAndPro(@Param("uid") String uid,@Param("projectId") String projectId);
}