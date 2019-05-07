package com.gennlife.rws.dao;

import com.gennlife.rws.entity.RoleUserMap;

public interface RoleUserMapMapper {
    int insert(RoleUserMap record);

    int insertSelective(RoleUserMap record);
}