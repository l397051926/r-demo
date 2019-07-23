package com.gennlife.rws.dao;


import com.gennlife.rws.entity.BatchingSqlMap;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BatchingSqlMapMapper {
    void insert(BatchingSqlMap record);

    void updateById(BatchingSqlMap record);

    void updateExportByPatientSetId(@Param("patientsSetId") String patientsSetId,@Param("export") Integer export);

    List<BatchingSqlMap> getPatientsSqlMapByDataSourceId(String patientSetId);

    void insertForGroupid(BatchingSqlMap record);

    List<BatchingSqlMap> getPatientsSqlMapByDataSourceIdAndExclude(@Param("dataSourceId") String dataSourceId, @Param("export") Integer export);

    List<BatchingSqlMap> getPatientsSqlMapBypatientSetIdsAndExclude(@Param("dataSourceIds") List<String> patientSetIds, @Param("export") Integer export);

    BatchingSqlMap getPatientsSqlMapByIdAndExclude(@Param("id") Integer id, @Param("export") Integer export);

    void deleteByDataSourceId(String groupId);
}