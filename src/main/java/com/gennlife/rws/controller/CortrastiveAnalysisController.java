package com.gennlife.rws.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.Group;
import com.gennlife.rws.entity.GroupCondition;
import com.gennlife.rws.service.ActiveIndexService;
import com.gennlife.rws.service.ContrastiveAnalysisActiveService;
import com.gennlife.rws.service.CortrastiveAnalysisService;
import com.gennlife.rws.service.GroupService;
import com.gennlife.rws.util.AjaxObject;
import com.gennlife.rws.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(value="/rws/",produces = "application/json;charset=UTF-8")
public class CortrastiveAnalysisController {

    private static final Logger LOG = LoggerFactory.getLogger(CortrastiveAnalysisController.class);

    @Autowired
    private GroupService groupService;

    @Autowired
    private CortrastiveAnalysisService cortrastiveAnalysisService;

    @Autowired
    private ActiveIndexService activeIndexService;

    @Autowired
    private ContrastiveAnalysisActiveService contrastiveAnalysisActiveService;

    //根据项目id 用户id 样本名称 搜索条件
    @RequestMapping(value = "/cort/getPatientGroupCondition",method = {RequestMethod.POST,RequestMethod.GET})
    public AjaxObject getPatientGroupCondition(@RequestBody String param){
        AjaxObject ajaxObject = null;
        try {
            JSONObject paramObj = JSONObject.parseObject(param);
            LOG.info("获取患者分组信息  参数： "+paramObj);
            String groupType = paramObj.getString("groupType");
            String projectId = paramObj.getString("projectId");
            String uid = paramObj.getString("uid");
            Integer cortType = paramObj.getInteger("cortType");
            String createId = paramObj.getString("createId");
            //获取项目分组 信息
            List<Group> groupList =groupService.getGroupByProjectId(groupType,projectId);
            List<GroupCondition> groupConditionList = groupService.getGroupConditionProjectId(uid,projectId,cortType);
            ajaxObject = cortrastiveAnalysisService.getPatientGroupCondition(groupList,groupConditionList);
        } catch (Exception e) {
            LOG.error("获取患者分组信息，异常信息{}",e);
            ajaxObject = new AjaxObject();
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage("获取患者分组信息,错误原因"+e.getMessage());
        }
        return ajaxObject;
    }
    //获取研究变量参数
    @RequestMapping(value = "/cort/getResearchVariable",method = {RequestMethod.POST,RequestMethod.GET})
    public AjaxObject getResearchVariable(@RequestBody String param){
        AjaxObject ajaxObject = null;
        try {
            JSONObject paramObj = JSONObject.parseObject(param);
            LOG.info("获取研究变量参数 "+paramObj);
            String uid = paramObj.getString("uid");
            String projectId = paramObj.getString("projectId");
            Integer cortType = paramObj.getInteger("cortType");
            String createId = paramObj.getString("createId");
            ajaxObject= activeIndexService.getContrastiveActive(createId,projectId,cortType);
        } catch (Exception e) {
            LOG.error("获取用户研究变量，异常信息{}",e);
            ajaxObject = new AjaxObject();
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage("获取用户研究变量,错误原因"+e.getMessage());
        }
        return ajaxObject;
    }
    //获取全部研究变量参数
    @RequestMapping(value = "/cort/getAllResearchVariable",method = {RequestMethod.POST,RequestMethod.GET})
    public AjaxObject getAllResearchVariable(@RequestBody String param){
        AjaxObject ajaxObject = null;
        try {
            JSONObject paramObj = JSONObject.parseObject(param);
            LOG.info("获取全部研究变量参数 "+paramObj);
            String uid = paramObj.getString("uid");
            String projectId = paramObj.getString("projectId");
            Integer cortType = paramObj.getInteger("cortType");
            String createId = paramObj.getString("createId");
            ajaxObject= activeIndexService.getAllResearchVariable(createId,projectId,cortType,uid);
        } catch (Exception e) {
            LOG.error("获取用户研究变量，异常信息{}",e);
            ajaxObject = new AjaxObject();
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage("获取用户研究变量,错误原因"+e.getMessage());
        }
        return ajaxObject;
    }
    //存储研究变量参数
    @RequestMapping(value = "/cort/saveResearchVariable",method = {RequestMethod.POST,RequestMethod.GET})
    public AjaxObject saveResearchVariable(@RequestBody String param){
        AjaxObject ajaxObject = null;
        try {
            JSONObject paramObj = JSONObject.parseObject(param);
            LOG.info("存储研究变量参数 "+paramObj);
            ajaxObject= contrastiveAnalysisActiveService.saveContrastiveActive(paramObj);
        } catch (Exception e) {
            LOG.error("存储研究变量参数，异常信息{}",e);
            ajaxObject = new AjaxObject();
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage("存储研究变量参数,错误原因"+e.getMessage());
        }
        return ajaxObject;
    }
      //删除研究变量参数 {"activeIndexId","projectId","createId"}
    @RequestMapping(value = "/cort/deleteResearchVariable",method = {RequestMethod.POST,RequestMethod.GET})
    public AjaxObject deleteResearchVariable(@RequestBody String param){
        AjaxObject ajaxObject = null;
        try {
            JSONObject paramObj = JSONObject.parseObject(param);
            LOG.info("删除研究变量参数 "+paramObj);
            ajaxObject= contrastiveAnalysisActiveService.deleteContrastiveActive(paramObj);
        } catch (Exception e) {
            LOG.error("删除研究变量参数，异常信息{}",e);
            ajaxObject = new AjaxObject();
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage("删除研究变量参数,错误原因"+e.getMessage());
        }
        return ajaxObject;
    }
    //获取结果 图形列表
    @RequestMapping(value = "/cort/getContResult",method = {RequestMethod.POST,RequestMethod.GET})
    public AjaxObject getContResult(@RequestBody String param){
        AjaxObject ajaxObject = null;
        try {
            JSONObject paramObj = JSONObject.parseObject(param);
            String uid = paramObj.getString("uid");
            String projectId = paramObj.getString("projectId");
            Integer cortType = paramObj.getInteger("cortType");
            Boolean showSubGroup = paramObj.getBoolean("showSubGroup");
            String createId = paramObj.getString("createId");
            String crfId = paramObj.getString("crfId");
            if(StringUtils.isEmpty(createId) || "undefined".equals(createId)){
                createId = uid;
            }
            ajaxObject =  cortrastiveAnalysisService.getContResult(createId,projectId,cortType,showSubGroup,crfId,uid);
        } catch (Exception e) {
            LOG.error("获取计算结果 的统计图形列表，异常信息{}",e);
            ajaxObject = new AjaxObject();
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage("获取计算结果 的统计图形列表,错误原因"+e.getMessage());
        }
        return ajaxObject;
    }
    //获取计算结果的患者列表
    @RequestMapping(value = "/cort/getContResultForPatient",method = {RequestMethod.POST,RequestMethod.GET})
    public AjaxObject getContResultForPatient(@RequestBody String param){
        AjaxObject ajaxObject = null;
        try {
            JSONObject paramObj = JSONObject.parseObject(param);
            String uid = paramObj.getString("uid");
            String projectId = paramObj.getString("projectId");
            Integer pageNum = paramObj.getInteger("pageNum");
            Integer pageSize = paramObj.getInteger("pageSize");
            JSONArray showColumns = paramObj.getJSONArray("showColumns");
            Integer cortType = paramObj.getInteger("cortType");
            String createId = paramObj.getString("createId");
            String crfId = paramObj.getString("crfId");
            if(StringUtils.isEmpty(createId) || "undefined".equals(createId)){
                createId = uid;
            }
            ajaxObject = cortrastiveAnalysisService.getContResultForPatient(createId,projectId,pageNum,pageSize,showColumns,cortType,crfId,uid);
        } catch (Exception e) {
            LOG.error("获取计算结果的患者列表，异常信息{}",e);
            ajaxObject = new AjaxObject();
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage("获取计算结果的患者列表,错误原因"+e.getMessage());
         }
        return ajaxObject;
    }
    //存储分组条件param:{uname:xx,uid:xx , projectId :xx,groupIds[1,2,3,4,5]}
    @RequestMapping(value = "/cort/saveGroupCondition",method = {RequestMethod.POST,RequestMethod.GET})
    public AjaxObject saveGroupCondition(@RequestBody String param){
        AjaxObject ajaxObject = null;
        try {
            JSONObject paramObj = JSONObject.parseObject(param);
            String uid = paramObj.getString("uid");
            String createId = paramObj.getString("createId");
            String projectId = paramObj.getString("projectId");
            JSONArray groupsIds = paramObj.getJSONArray("groupIds");
            String uname = paramObj.getString("uname");
            Integer cortType = paramObj.getInteger("cortType");
            String groupTypeId = paramObj.getString("groupTypeId");
            ajaxObject = cortrastiveAnalysisService.saveGroupCondition(uname,uid,projectId,groupsIds,cortType,groupTypeId,createId);
        } catch (Exception e) {
            LOG.error("存储分组条件，异常信息{}",e);
            ajaxObject = new AjaxObject();
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage("存储分组条件,错误原因"+e.getMessage());
        }
        return ajaxObject;
    }

    @RequestMapping(value = "/export/calculationResult",method = {RequestMethod.POST,RequestMethod.GET})
    public Object calculationResult(@RequestBody String param){
        Object ajaxObject = null;
        try {
            JSONObject paramObj = JSONObject.parseObject(param);
            ajaxObject = cortrastiveAnalysisService.calculationResult(paramObj);
        } catch (Exception e) {
            LOG.error("存储分组条件，异常信息{}",e);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"计算研究变量结果 发送问题"+e.getMessage());
        }
        return ajaxObject;
    }

    @RequestMapping(value = "/export/calculationResultOne",method = {RequestMethod.POST,RequestMethod.GET})
    public Object calculationResultOne(@RequestBody String param){
        Object ajaxObject = null;
        try {
            JSONObject paramObj = JSONObject.parseObject(param);
            ajaxObject = cortrastiveAnalysisService.calculationResultOne(paramObj);
        } catch (Exception e) {
            LOG.error("存储分组条件，异常信息{}",e);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"计算研究变量结果 发送问题"+e.getMessage());
        }
        return ajaxObject;
    }

    @RequestMapping(value = "/export/snapshootActiveResult",method = {RequestMethod.POST,RequestMethod.GET})
    public Object snapshootActiveResult(@RequestBody String param){
        Object ajaxObject = null;
        try {
            JSONObject paramObj = JSONObject.parseObject(param);
            ajaxObject = cortrastiveAnalysisService.snapshootActiveResult(paramObj);
        } catch (Exception e) {
            LOG.error("快照导出任务结果，异常信息{}",e);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"快照导出任务结果 发送问题"+e.getMessage());
        }
        return ajaxObject;
    }

}
