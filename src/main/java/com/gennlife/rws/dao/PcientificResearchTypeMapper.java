package com.gennlife.rws.dao;

import com.gennlife.rws.entity.PcientificResearchType;

import java.util.List;

public interface PcientificResearchTypeMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PcientificResearchType record);

    int insertSelective(PcientificResearchType record);

    PcientificResearchType selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PcientificResearchType record);

    int updateByPrimaryKey(PcientificResearchType record);

    List<PcientificResearchType> selectAll();

}