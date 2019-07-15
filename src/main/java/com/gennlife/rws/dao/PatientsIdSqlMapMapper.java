package com.gennlife.rws.dao;


import com.gennlife.rws.entity.PatientsIdSqlMap;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PatientsIdSqlMapMapper {
    void insert(PatientsIdSqlMap record);

    void updateById(PatientsIdSqlMap record);

    void updateExportByPatientSetId(@Param("patientsSetId") String patientsSetId,@Param("export") Integer export);

    List<PatientsIdSqlMap> getPatientsSqlMapByDataSourceId(String patientSetId);

    void insertForGroupid(PatientsIdSqlMap record);

    List<PatientsIdSqlMap> getPatientsSqlMapByDataSourceIdAndExclude(@Param("dataSourceId") String dataSourceId, @Param("export") Integer export);

    List<PatientsIdSqlMap> getPatientsSqlMapBypatientSetIdsAndExclude(@Param("dataSourceIds") List<String> patientSetIds, @Param("export") Integer export);

    PatientsIdSqlMap getPatientsSqlMapByIdAndExclude(@Param("id") Integer id, @Param("export") Integer export);

    void deleteByDataSourceId(String groupId);
}