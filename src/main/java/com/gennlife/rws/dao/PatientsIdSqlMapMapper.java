package com.gennlife.rws.dao;


import com.gennlife.rws.entity.PatientsIdSqlMap;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PatientsIdSqlMapMapper {
    void insert(PatientsIdSqlMap record);

    void updateById(PatientsIdSqlMap record);

    void updateExportByPatientSetId(@Param("patientsSetId") String patientsSetId,@Param("export") Integer export);

    List<PatientsIdSqlMap> getPatientSnIdsBypatientSetId(String patientSetId);

    void insertForGroupid(PatientsIdSqlMap record);

}