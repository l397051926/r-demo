/**
 * copyRight
 */
package com.gennlife.rws.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.content.CommonContent;
import com.gennlife.rws.dao.InputTaskMapper;
import com.gennlife.rws.dao.PatientsSetMapper;
import com.gennlife.rws.entity.ActiveIndex;
import com.gennlife.rws.entity.Project;
import com.gennlife.rws.exception.DownLoadSystemException;
import com.gennlife.rws.service.ActiveIndexService;
import com.gennlife.rws.service.DownLoadService;
import com.gennlife.rws.service.ProjectService;
import com.gennlife.rws.service.SearchByuqlService;
import com.gennlife.rws.util.AjaxObject;
import com.gennlife.rws.util.HttpUtils;
import com.gennlife.rws.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * Created by liuzhen.
 * Date: 2017/10/23
 * Time: 9:58
 */
@RestController
@RequestMapping(value="/rws/pre/",produces = "application/json;charset=UTF-8")
@Api(description = "RWS 入排数据初筛及统计")
public class PreLiminaryController {
    private static Logger LOG = LoggerFactory.getLogger(PreLiminaryController.class);
    @Autowired
    private ProjectService projectService;
    @Autowired
    private DownLoadService downLoadService;
    @Autowired
    private HttpUtils httpUtils;
    @Autowired
    private ActiveIndexService activeIndexService;
    @Autowired
    private InputTaskMapper inputTaskMapper;
    @Autowired
    private PatientsSetMapper patientsSetMapper;
    @Autowired
    private SearchByuqlService searchByuqlService;

    @ApiOperation(value = "RWS 检索数据导出",notes = "根据前端提交的信息保存RWS 保存活动/指标/入排条件的定义 Created by liuzhen.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "param", value = "项目Id", dataType = "JSONObject",required = true)
    })
    @RequestMapping(value = "/liminary",method = {RequestMethod.POST,RequestMethod.GET})
    public AjaxObject preLiminary(@RequestBody String param){
        AjaxObject object = new AjaxObject();
        String condition = null;
        try {
            LOG.info("接收到的参数为{}",param);
            JSONObject params = JSONObject.parseObject(param);
            condition = params.getString("condition");
            //用户id 项目名称 用户名称
            String createId = params.getString("createId");
            String createName = params.getString("createName");
            String patientName = params.getString("patientName");
            String crfName = params.getString("crfName");
            String projectId = params.getString("projectId").replaceAll("-","");
            String patientSetId = params.getString("patientSetId");
            String crfId = params.getString("crfId");
            String indexName = params.getString("indexName");
            String projectName = params.getString("projectName");
            String power = params.getString("power")==null? "":params.getString("power");
            String groups = params.getString("groups")==null? "":params.getString("groups");
            if(StringUtils.isEmpty(condition) || StringUtils.isEmpty(projectId) || StringUtils.isEmpty(indexName)){
                return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"参数错误，导出条件、项目id及索引名称不能为空");
            }
            String esParam = CommonContent.ESSEARCHPARAMEXPORT.replace("QUERYINDEXNAME",indexName).replace("QUERYCONDITION",condition).replace("PAGESIZE",httpUtils.getPageSize()+"");
            JSONObject esJSon = JSONObject.parseObject(esParam);
            if(StringUtils.isNotEmpty(power)){
                JSONArray pow = JSONArray.parseArray(power);
                JSONObject powerJson = new JSONObject().fluentPut("has_search",pow);
                esJSon.put("power",powerJson);
            }
            if(StringUtils.isNotEmpty(groups)){
                JSONArray arrayJson = JSONArray.parseArray(groups);
                esJSon.put("groups",arrayJson);
            }
            Integer patientSetCount = patientsSetMapper.getCountByProjectIdAndPatientsetName(projectId,patientName);
            if(patientSetCount == null || patientSetCount ==0){
                return new AjaxObject(AjaxObject.AJAX_STATUS_TIPS,"项目id与数据集id不匹配");
            }
            Integer patCount = patientsSetMapper.getcountByPatIdAndPatName(patientSetId,patientName);
            if(patCount == null || patCount ==0){
                return new AjaxObject(AjaxObject.AJAX_STATUS_TIPS,"数据集名称与id不匹配");
            }
            if(StringUtils.isNotEmpty(crfId) && StringUtils.isNotEmpty(crfName) ){
                projectService.saveDatasource(projectId,crfId,crfName);
            }else {
                LOG.warn("crfId 或者 crfName 为空了" );
            }
            //导出任务项目数据
            Integer quereCount = inputTaskMapper.getInputQueueTask(createId);
            if(quereCount >2){
                return new AjaxObject(AjaxObject.AJAX_STATUS_TIPS,"排队已满3个， 无法导出数据") ;
            }
            String uqlQuery = "";
            object = downLoadService.sysBuildIndex(downLoadService,patientSetId,esJSon,crfId,createId,createName,patientName,projectId,uqlQuery,projectName,crfName);
        } catch (DownLoadSystemException e){
            object = new AjaxObject(300, e.getMessage());
            return object;
        } catch (Exception e) {
            object.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            object.setMessage("导出数据时发生异常，异常信息为："+e.getMessage());
            LOG.error("根据请求参数{}\n下载数据发生错误，错误信息为：{}",condition,e);
        }
        return object;
    }

    @ApiOperation(value = "RWS 初筛结果统计",notes = "根据需要统计的信息，完成图形统计 Created by liuzhen.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "param", value = "参数" , dataType = "JSONObject",required = true)
    })
    @RequestMapping(value = "/aggregation",method = {RequestMethod.POST,RequestMethod.GET})
    public AjaxObject aggregation(@RequestBody String param){
        AjaxObject object = new AjaxObject();
        try {
            JSONObject params = JSONObject.parseObject(param);
            String patientSetId = params.getString("patientSetId").replaceAll("-","");
            JSONArray aggregationTeam = params.getJSONArray("aggregationTeam");
            String projectId = params.getString("projectId");
            String crfId = params.getString("crfId");
            if(StringUtils.isEmpty(patientSetId) || aggregationTeam == null || aggregationTeam.size()<=0){
                object.setMessage("请求参数错误");
                object.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
                return object;
            }
            LOG.info("rws 初筛结果图形统计，获取到的参数{}",param);
            object = searchByuqlService.getAggregationAll(patientSetId,aggregationTeam,projectId,crfId);

        } catch (Exception e) {
            object.setMessage(AjaxObject.AJAX_MESSAGE_FAILURE+e.getMessage());
            object.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            LOG.error("统计信息时发生异常,异常信息为{}",e);
        }
        return object;
    }

    @ApiOperation(value = "RWS 获取已经导出的数据列表",notes = " Created by liuzhen.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "param", value = "参数" , dataType = "JSONObject",required = true)
    })
    @RequestMapping(value = "/findForProjectData",method = {RequestMethod.POST,RequestMethod.GET})
    public AjaxObject findForProjectData(@RequestBody String param/*String projectId,String showColumns,Integer pageNum,Integer pageSize*/){
        AjaxObject object = new AjaxObject();
        LOG.info(param);
        try {
            JSONObject params = JSONObject.parseObject(param);
            JSONArray showColumns = params.getJSONArray("showColumns");
            String projectId = params.getString("projectId").replaceAll("-","");
            Integer pageNum = params.getInteger("pageNum");
            Integer type = params.getInteger("type");
            Integer pageSize = params.getInteger("pageSize");
            String crfId = params.getString("crfId");
            if(showColumns == null || showColumns.isEmpty() || StringUtils.isEmpty(projectId)){
                return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"参数错误");
            }
            if(type == null){
                type = 1;
            }
            //查找活动和指标
            JSONArray actives = new JSONArray();
            List<ActiveIndex> activeIndex = activeIndexService.findActiveIdByProject(projectId, 5);
            if(activeIndex!=null && !activeIndex.isEmpty()){
                for (ActiveIndex ids:activeIndex) {
                    JSONObject object1 = new JSONObject();
                    object1.put("name",ids.getName());
                    object1.put("id",ids.getId());
                    object1.put("activeType",ids.getActiveType());
                    actives.add(object1);
                }
            }
            if(StringUtils.isEmpty(projectId) || pageNum == null || pageSize==null || showColumns ==null || showColumns.size()<=0){
                object.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
                object.setMessage("提交参数错误");
                LOG.warn(object.getMessage());
                return object;
            }
            object = searchByuqlService.getPatientListByAll("",projectId,showColumns,actives,pageNum,pageSize,type, crfId);
            object.setColumns(showColumns);
        } catch (Exception e) {
            object.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            object.setMessage(AjaxObject.AJAX_MESSAGE_FAILURE+e.getMessage());
            LOG.error("获取已经导出的数据列表时出错，{}",e);
        }
        return object;
    }

    @ApiOperation(value = "RWS 统计如数据结果",notes = " Created by liuzhen.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "param", value = "参数{\"projectId\":\"1213212\",\"type\":1,\"activeId\":\"\"}" , dataType = "JSONObject",required = true)
    })
    @RequestMapping(value = "/findTotalForImport",method = {RequestMethod.POST,RequestMethod.GET})
    public AjaxObject findTotalForImport(@RequestBody String param){
        AjaxObject ajaxObject = null;
        try {
            JSONObject object = JSONObject.parseObject(param);
            String projectId = object.getString("projectId").replaceAll("-","");
            String activeId = object.getString("activeId");
            Integer type = object.getIntValue("type");
            if(StringUtils.isEmpty(projectId)){
                return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"activeId,projectId和type不能为空");
            }
            if(StringUtils.isNotEmpty(activeId)){
                activeId = StringUtils.substringBeforeLast(activeId,"_");
            }
            ajaxObject = downLoadService.findTotalByActiveIdAndProjectId(projectId,activeId,type);
        } catch (Exception e) {
            LOG.error("查询统计数据失败，异常信息{}",e);
            ajaxObject = new AjaxObject();
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage("查询统计数据失败,错误原因"+e.getMessage());
        }
        return ajaxObject;
    }

    //获取项目列表
    @RequestMapping(value = "/getProjectByCrfId",method = {RequestMethod.POST,RequestMethod.GET})
    public AjaxObject getProjectByCrfId(@RequestBody String param){
        AjaxObject ajaxObject = null;
        try {
            JSONObject paramObj = JSONObject.parseObject(param);
            List<Project> projectList = projectService.getProjectListByCrfId(paramObj);
            if(projectList != null && projectList.size()==0 ){
                return new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,"沒有数据");
            }else {
                ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,AjaxObject.AJAX_MESSAGE_SUCCESS);
                ajaxObject.setData(projectList);
            }

        } catch (Exception e) {
            LOG.error("获取数据列表，异常信息{}",e);
            ajaxObject = new AjaxObject();
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage("获取数据列表,错误原因"+e.getMessage());
        }
        return ajaxObject;
    }


}
