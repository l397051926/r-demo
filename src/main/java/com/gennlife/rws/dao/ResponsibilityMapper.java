package com.gennlife.rws.dao;

import com.gennlife.rws.entity.Responsibility;

public interface ResponsibilityMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Responsibility record);

    int insertSelective(Responsibility record);

    Responsibility selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Responsibility record);

    int updateByPrimaryKey(Responsibility record);
}