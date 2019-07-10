/**
 * copyRight
 */
package com.gennlife.rws.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.content.CommonContent;
import com.gennlife.rws.content.UqlConfig;
import com.gennlife.rws.entity.ActiveIndexTask;
import com.gennlife.rws.service.*;
import com.gennlife.rws.util.AjaxObject;
import com.gennlife.rws.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuzhen.
 * Date: 2017/10/28
 * Time: 9:48
 */
@RestController
@RequestMapping("/search")
@Api(description = "rws 检索服务")
public class RwsSearchController {
    private static Logger LOG = LoggerFactory.getLogger(RwsSearchController.class);
    @Autowired
    private ActiveIndexTaskService taskService;
    @Autowired
    private SearchByuqlService searchByuqlService;
    @Autowired
    private SearchCrfByuqlService searchCrfByuqlService;

    private AjaxObject getAjaxObject(Integer status, String message) {
        AjaxObject ajaxObject = new AjaxObject();
        ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
        ajaxObject.setMessage(message);
        return ajaxObject;
    }

    @ApiOperation(value = "根据条件查询活动计算统计结果信息", notes = "活动定义页下的详情 Created by liuzhen.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "param", value = "查询参数{\"activeId\":\"12112\"}", dataType = "JSONObject", required = true),
    })
    @RequestMapping(value = "/getCalcTotalByActiveId", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject getCalcTotalByActiveId(@RequestBody String param) {
        AjaxObject ajaxObject = new AjaxObject();
        try {
            LOG.info(param);
            JSONObject object = JSONObject.parseObject(param);
            String activeId = object.getString("activeId");
            if (StringUtils.isEmpty(activeId)) {
                return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "参数错误");
            }
            List<String> activeIds = new ArrayList<String>();
            activeIds.add(activeId);
            List<ActiveIndexTask> tasks = taskService.getTaskByActiveIdsAndStatus(activeIds);
            if (tasks == null || tasks.isEmpty()) {
                ajaxObject.setFlag(false);
                ajaxObject.setMessage("计算中，请重试！");
            } else {
                ajaxObject.setFlag(true);
                ajaxObject.setMessage("计算完成");
                ajaxObject.setData(tasks);
            }
        } catch (Exception e) {
            LOG.error("查询计算汇总结果时发成错误，错误信息为{}", e);
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage("查询失败" + e.getMessage());
            ajaxObject.setFlag(false);
        }
        return ajaxObject;
    }

    @RequestMapping(value = "clacIndexResultSearch", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject clacIndexResultSearchByUql(@RequestBody String param) {
        AjaxObject ajaxObject = null;

        try {
            LOG.info(param);
            JSONObject jsonObject = JSONObject.parseObject(param);
            JSONArray patientSetId = jsonObject.getJSONArray("patientSetId");
            String createId = jsonObject.getString("createId");
            String createName = jsonObject.getString("createName");
            String groupId = jsonObject.getString("groupToId");
            String groupName = jsonObject.getString("groupName");
            String isExport = jsonObject.getString("isExport");
            String activeId = jsonObject.getString("activeId");
            String projectId = jsonObject.getString("projectId").replaceAll("-","");
            Integer pageSize = jsonObject.getInteger("pageSize");
            Integer pageNum = jsonObject.getInteger("pageNum");
            String crfId = jsonObject.getString("crfId");
            JSONArray basicColumns = jsonObject.getJSONArray("basicColumns");
            JSONObject indexColumns = jsonObject.getJSONArray("indexColumns").getJSONObject(0);
            String groupFromId = jsonObject.getString("groupFromId");
            String isVariant = jsonObject.getString("isVariant");

            //JSONArray indexColumns = jsonObject.getJSONArray("indexColumns");
            if (StringUtils.isEmpty(activeId) || StringUtils.isEmpty(projectId) ) {
                return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "参数错误");
            }
            int activeType = jsonObject.getInteger("activeType");
            if(StringUtils.isEmpty(indexColumns.getString("name")) ){
                indexColumns.put("name","指标");
            }
            if(activeType !=3){
                basicColumns.add(indexColumns);
            }
            if(UqlConfig.isCrf(crfId)){
                if(CommonContent.ACTIVE_TYPE_INDEX==activeType){//指标  枚举
                    ajaxObject = searchCrfByuqlService.searchClacIndexResultByUql(activeId,projectId,pageSize,pageNum,basicColumns,crfId,groupFromId,patientSetId,groupId,isVariant);
                }else if(CommonContent.ACTIVE_TYPE_INOUTN==activeType){//入排
                    ajaxObject = searchCrfByuqlService.searchCalcExculeByUql(activeId,projectId,pageSize,pageNum,basicColumns,crfId,isExport,groupId,groupName,
                        patientSetId,createId,createName,groupFromId,false);
                }
            }else {
                if(CommonContent.ACTIVE_TYPE_INDEX==activeType){//指标  枚举
                    ajaxObject = searchByuqlService.searchClacIndexResultByUql(activeId,projectId,pageSize,pageNum,basicColumns,groupFromId,patientSetId,groupId,isVariant,crfId);
                }else if(CommonContent.ACTIVE_TYPE_INOUTN==activeType){//入排
                    ajaxObject = searchByuqlService.searchCalcExculeByUql(activeId,projectId,pageSize,pageNum,basicColumns,isExport,groupId,groupName,patientSetId,createId,
                        createName,groupFromId,false,crfId);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
             ajaxObject = getAjaxObject(AjaxObject.AJAX_STATUS_FAILURE, e.getMessage());
        }
        return ajaxObject;
    }

    @RequestMapping(value = "/clacResultSearch", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject clacResultSearchByUql(@RequestBody String param) {
        AjaxObject ajaxObject = null;
        try {
            JSONObject object = JSONObject.parseObject(param);
            String activeId = object.getString("activeId");
            String projectId = object.getString("projectId").replaceAll("-","");
            JSONArray visitColumns = object.getJSONArray("visitColumns");
            JSONArray basicColumns = object.getJSONArray("basicColumns");
            Integer activeType = object.getInteger("activeType");
            Integer pageNum = object.getInteger("pageNum");
            Integer pageSize = object.getInteger("pageSize");
            String activeResult = object.getString("activeResult");
            String groupFromId = object.getString("groupFromId");
            JSONArray patientSetId = object.getJSONArray("patientSetId");
            String groupId = object.getString("groupToId");
            String crfId = object.getString("crfId");
            LOG.info(param);
            if (StringUtils.isEmpty(activeId) || StringUtils.isEmpty(projectId) || pageSize == null
                    || pageNum == null || basicColumns == null || basicColumns.isEmpty()
                    || visitColumns == null || visitColumns.isEmpty()) {
                return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "参数错误");
            }
            if(StringUtils.isNotEmpty(crfId) && !crfId.equals("EMR")){
                ajaxObject = searchCrfByuqlService.searchCalcResultByUql(activeId, projectId, basicColumns, visitColumns, activeType, pageNum, pageSize,activeResult,crfId,groupFromId,patientSetId,groupId);
            }else {
                ajaxObject = searchByuqlService.searchCalcResultByUql(activeId, projectId, basicColumns, visitColumns, activeType, pageNum, pageSize,activeResult,groupFromId,patientSetId,groupId,crfId);
            }
        } catch (Exception e) {
            ajaxObject = getAjaxObject(AjaxObject.AJAX_STATUS_FAILURE, e.getMessage());
        }
        return ajaxObject;
    }


}
