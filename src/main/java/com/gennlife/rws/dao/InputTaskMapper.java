package com.gennlife.rws.dao;

import com.gennlife.rws.entity.InputTask;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface InputTaskMapper {
    int insert(InputTask record);

    int insertSelective(InputTask record);

    List<InputTask> getInputTasks(@Param("uid") String uid,
                                  @Param("projectName") String projectName,
                                  @Param("patientSetName") String patientSetName,
                                  @Param("status") Integer status,
                                  @Param("startNum") Integer startNum,
                                  @Param("endNum") Integer endNum);

    Integer getInputTasksTotal(@Param("uid") String uid,
                               @Param("projectName") String projectName,
                               @Param("patientSetName") String patientSetName,
                               @Param("status") Integer status);


    void deleteInputTaskByInputId(String inputId);

    InputTask getInputtaskByInputId(String inputId);

    void updateInputTask(InputTask inputTask);

     List<String> getInputIdsByPatientSetId(String patientsSetId);

    void updateInputTaskByMap(Map<String, String> inputMaps);

    void updateinputCancelDate(InputTask inputTask);

    Integer judgeInputTaskStatus(InputTask inputTask);

    Integer getRunTimeTaskByProjectId(String projectId);

    Integer getInputQueueTask(String createId);

    Integer getRunTaskSumCountByProjcetId(String projectId);

    List<String> getInputIdsByProjectId(String projectId);

    Integer getWorkTaskByProjectId(String projectId);

    void updateInputTaskRemainTime(InputTask inputTask);

    Integer getCountByProjectIdAndStatus(String projectId, Integer cancel);

    InputTask getInputtaskAllByInputId(String taskId);
}