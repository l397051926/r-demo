package com.gennlife.rws.dao;


import com.gennlife.rws.entity.OperLogs;
import com.gennlife.rws.entity.PatientsIdSqlMap;

public interface PatientsIdSqlMapMapper {
    void insert(PatientsIdSqlMap record);

    void updateById(PatientsIdSqlMap record);

}