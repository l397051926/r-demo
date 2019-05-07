package com.gennlife.rws.dao;

import com.gennlife.rws.entity.ContrastiveAnalysisCountResult;

import java.util.List;

public interface ContrastiveAnalysisCountResultMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ContrastiveAnalysisCountResult record);

    int insertSelective(ContrastiveAnalysisCountResult record);

    ContrastiveAnalysisCountResult selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ContrastiveAnalysisCountResult record);

    int updateByPrimaryKey(ContrastiveAnalysisCountResult record);

    /**
     * 根据 索引名称 获取数据
     * @param activerIndexId
     * @return
     */
    List<ContrastiveAnalysisCountResult> getResultByUidAndPro(String activerIndexId);
}