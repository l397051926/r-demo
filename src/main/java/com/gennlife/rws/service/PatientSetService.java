package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.ContrastiveAnalysisCount;
import com.gennlife.rws.entity.PatientsSet;
import com.gennlife.rws.entity.SearchLog;
import com.gennlife.rws.util.AjaxObject;

import java.io.IOException;
import java.util.List;

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
	public List<PatientsSet> getPatientSetList(JSONObject obj) throws IOException;

	/**
	 *
	 * @author zhengguohui
	 * @description 根据患者集ID查询患者集信息
	 * @date 2018年6月29日
	 * @param param
	 * @return PatientsSet
	 */
	public PatientsSet getPatientSet(JSONObject obj);

    /**
     *
     * @author zhengguohui
     * @description 保存患者集信息
     * @date 2018年6月29日
     * @param param
     */
    public PatientsSet savePatientSet(JSONObject param);

	/**
	 *
	 * @author zhengguohui
	 * @description 编辑患者集信息
	 * @date 2018年6月29日
	 * @param param
	 */
	public PatientsSet updatePatientSet(JSONObject obj);

	/**
	 *
	 * @author zhengguohui
	 * @description 删除患者集信息
	 * @date 2018年6月29日
	 * @param param
	 */
	public void deletePatientSet(JSONObject obj) throws IOException;

	/**
	 * @author zhengguohui
	 * @description 根据患者集ID查询搜索条件
	 * @date 2018年7月5日
	 * @param param
	 * @return SearchLog
	 */
	public List<SearchLog> getSearchLog(JSONObject obj);

	/**
	 * @author zhengguohui
	 * @description 查询研究变量的数据
	 * @date 2018年7月5日
	 * @param obj
	 * @return List<ContrastiveAnalysisCount>
	 */
	public List<ContrastiveAnalysisCount> getContrasAnalyList(JSONObject obj);

	public List<PatientsSet> getPatientSetByProjectId(JSONObject paramObj);

	AjaxObject getPatientSetForList(JSONObject object);

    void savePatientImport(JSONObject obj) throws IOException;
}
