package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.ContrastiveAnalysisCount;
import com.gennlife.rws.entity.PatientsSet;
import com.gennlife.rws.entity.SearchLog;
import com.gennlife.rws.util.AjaxObject;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author liumingxin
 * @create 2018 28 15:45
 * @desc
 **/
public interface PatientSetService {

	/**
	 * @author zhengguohui
	 * @description 根据项目ID查询患者集
	 * @date 2018年6月29日
	 * @param param
	 * @return List<PatientsSet>
	 */
	List<PatientsSet> getPatientSetList(JSONObject obj) throws IOException;

	/**
	 *
	 * @author zhengguohui
	 * @description 根据患者集ID查询患者集信息
	 * @date 2018年6月29日
	 * @param param
	 * @return PatientsSet
	 */
	PatientsSet getPatientSet(JSONObject obj);

    /**
     *
     * @author zhengguohui
     * @description 保存患者集信息
     * @date 2018年6月29日
     * @param param
     */
    PatientsSet savePatientSet(JSONObject param);

	/**
	 *
	 * @author zhengguohui
	 * @description 编辑患者集信息
	 * @date 2018年6月29日
	 * @param param
	 */
	PatientsSet updatePatientSet(JSONObject obj);

	/**
	 *
	 * @author zhengguohui
	 * @description 删除患者集信息
	 * @date 2018年6月29日
	 * @param param
	 */
	void deletePatientSet(JSONObject obj) throws IOException;

	/**
	 * @author zhengguohui
	 * @description 根据患者集ID查询搜索条件
	 * @date 2018年7月5日
	 * @param param
	 * @return SearchLog
	 */
	List<SearchLog> getSearchLog(JSONObject obj);

	/**
	 * @author zhengguohui
	 * @description 查询研究变量的数据
	 * @date 2018年7月5日
	 * @param obj
	 * @return List<ContrastiveAnalysisCount>
	 */
	List<ContrastiveAnalysisCount> getContrasAnalyList(JSONObject obj);

	List<PatientsSet> getPatientSetByProjectId(JSONObject paramObj);

    void savePatientImport(JSONObject obj) throws IOException;

	Long getPatientSetLocalCount(String patientSetId);

	void savePatientSetGroupBlock(String patientSetId, Set<String> allPats, Integer num);
}
