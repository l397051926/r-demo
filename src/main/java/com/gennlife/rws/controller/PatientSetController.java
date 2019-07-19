package com.gennlife.rws.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.ContrastiveAnalysisCount;
import com.gennlife.rws.entity.PatientsSet;
import com.gennlife.rws.entity.SearchLog;
import com.gennlife.rws.local.ProjectLocal;
import com.gennlife.rws.service.PatientSetService;
import com.gennlife.rws.service.SearchByuqlService;
import com.gennlife.rws.util.AjaxObject;
import com.gennlife.rws.vo.CustomerStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(value = "/rws/patientSet", produces = "application/json;charset=UTF-8")
public class PatientSetController {

    private static final Logger LOG = LoggerFactory.getLogger(PatientSetController.class);
    @Autowired
    private PatientSetService patientSetService;
    @Autowired
    private SearchByuqlService searchByuqlService;

    @RequestMapping(value = "/getPatientSetList", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject getPatientSetList(@RequestBody String param) {
        AjaxObject ajaxObject;
        try {
            JSONObject object;
            try {
                object = JSONObject.parseObject(param);
            } catch (Exception e) {
                ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
                    CustomerStatusEnum.FORMATJSONERROR.getMessage());
                return ajaxObject;
            }
            List<PatientsSet> list = patientSetService.getPatientSetList(object);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
            ajaxObject.setData(list);
            return ajaxObject;
        } catch (Exception e) {
            ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
            return ajaxObject;
        }
    }

    @RequestMapping(value = "/getPatientSet", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject getPatientSet(@RequestBody String param) {
        // 根据患者集ID获取患者集基础数据
        // 根据患者集ID查询患者集搜索条件=搜索条件记录表=SearchLog
        // 根据患者集ID查询患者数据列表
        AjaxObject ajaxObject;
        try {
            JSONObject object;
            try {
                object = JSONObject.parseObject(param);
            } catch (Exception e) {
                ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
                    CustomerStatusEnum.FORMATJSONERROR.getMessage());
                return ajaxObject;
            }
            PatientsSet patSet = patientSetService.getPatientSet(object);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
            ajaxObject.setData(patSet);
            return ajaxObject;
        } catch (Exception e) {
            ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
            return ajaxObject;
        }

    }

    @RequestMapping(value = "/getPatientList", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject getPatientList(@RequestBody String param) {
        AjaxObject object = new AjaxObject();
        LOG.info(param);
        try {
            JSONObject params = JSONObject.parseObject(param);
            JSONArray showColumns = params.getJSONArray("showColumns");
            String patientsSetId = params.getString("patientsSetId");
            String projectId = params.getString("projectId");
            Integer pageNum = params.getInteger("pageNum");
            String crfId = params.getString("crfId");
            Integer type = 1;
            Integer pageSize = params.getInteger("pageSize");
            if (showColumns == null || showColumns.isEmpty()) {
                return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "参数错误");
            }
            //查找活动和指标
            JSONArray actives = new JSONArray();
            object = searchByuqlService.getPatientSnsByAll(patientsSetId, projectId, showColumns, actives, pageNum, pageSize, type, crfId);
            object.setColumns(showColumns);
        } catch (Exception e) {
            object.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            object.setMessage(AjaxObject.AJAX_MESSAGE_FAILURE + e.getMessage());
            LOG.error("获取已经导出的数据列表时出错，{}", e);
        }
        return object;
    }

    @RequestMapping(value = "/getSearchCondition", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject getSearchCondition(@RequestBody String param) {
        // 根据患者集ID查询患者集搜索条件=搜索条件记录表=SearchLog
        AjaxObject ajaxObject;
        try {
            JSONObject object;
            try {
                object = JSONObject.parseObject(param);
            } catch (Exception e) {
                ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
                    CustomerStatusEnum.FORMATJSONERROR.getMessage());
                return ajaxObject;
            }
            List<SearchLog> searchLog = patientSetService.getSearchLog(object);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
            ajaxObject.setData(searchLog);
            return ajaxObject;
        } catch (Exception e) {
            ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
            return ajaxObject;
        }
    }

    @RequestMapping(value = "/getContrasAnalyList", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject getContrasAnalyList(@RequestBody String param) {
        // 根据患者用户ID和项目ID研究变量创建情况
        // 是否需要返回 分组查询结果
        AjaxObject ajaxObject;
        try {
            JSONObject object;
            try {
                object = JSONObject.parseObject(param);
            } catch (Exception e) {
                ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
                    CustomerStatusEnum.FORMATJSONERROR.getMessage());
                return ajaxObject;
            }
            List<ContrastiveAnalysisCount> list = patientSetService.getContrasAnalyList(object);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
            ajaxObject.setData(list);
            return ajaxObject;
        } catch (Exception e) {
            ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
            return ajaxObject;
        }
    }

    @RequestMapping(value = "/savePatientSet", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject savePatientSet(@RequestBody String param) {
        AjaxObject ajaxObject;
        try {
            JSONObject obj = JSONObject.parseObject(param);
            PatientsSet patientsSet = patientSetService.savePatientSet(obj);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
            ajaxObject.setData(patientsSet);
            return ajaxObject;
        } catch (Exception e) {
            LOG.error("保存 患者集发生问题");
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, AjaxObject.AJAX_MESSAGE_FAILURE);
        }

    }

    @RequestMapping(value = "/updatePatientSet", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject updatePatientSet(@RequestBody String param) {
        AjaxObject ajaxObject;
        try {
            JSONObject object;
            try {
                object = JSONObject.parseObject(param);
            } catch (Exception e) {
                ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
                    CustomerStatusEnum.FORMATJSONERROR.getMessage());
                return ajaxObject;
            }
            PatientsSet patientsSet = patientSetService.updatePatientSet(object);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
            ajaxObject.setData(patientsSet);
            return ajaxObject;
        } catch (Exception e) {
            ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
            return ajaxObject;
        }
    }

    @RequestMapping(value = "/deletePatientSet", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject deletePatientSet(@RequestBody String param) {
        AjaxObject ajaxObject;
        String projectId = null;
        try {
            JSONObject object;
            try {
                object = JSONObject.parseObject(param);
            } catch (Exception e) {
                ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
                    CustomerStatusEnum.FORMATJSONERROR.getMessage());
                return ajaxObject;
            }
            projectId = object.getString("projectId");
            patientSetService.deletePatientSet(object);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
            ajaxObject.setData(object.getString("patientsSetId"));
            return ajaxObject;
        } catch (Exception e) {
            ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
            return ajaxObject;
        } finally {
            ProjectLocal.PROJECT_LOCAL.remove(projectId);
        }
    }

    // 根据项目id 获取 患者集列表
    @RequestMapping(value = "/getPatientSetByProjectId", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject getPatientSetByProjectId(@RequestBody String param) {
        AjaxObject ajaxObject;
        try {
            JSONObject paramObj = JSONObject.parseObject(param);
            List<PatientsSet> patientsSetList = patientSetService.getPatientSetByProjectId(paramObj);
            if (patientsSetList != null && patientsSetList.size() != 0) {
                ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
                ajaxObject.setData(patientsSetList);
            } else {
                return new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, "没有数据");
            }
        } catch (Exception e) {
            LOG.error("获取患者集列表，异常信息{}", e);
            ajaxObject = new AjaxObject();
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage("获取患者集列表,错误原因" + e.getMessage());
        }
        return ajaxObject;
    }

}
