/**
 * copyRight
 */
package com.gennlife.rws.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.content.CommonContent;
import com.gennlife.rws.content.UqlConfig;
import com.gennlife.rws.dao.ActiveIndexMapper;
import com.gennlife.rws.dao.ActiveSqlMapMapper;
import com.gennlife.rws.dao.ContrastiveAnalysisActiveMapper;
import com.gennlife.rws.entity.ActiveIndex;
import com.gennlife.rws.entity.ActiveIndexConfigCondition;
import com.gennlife.rws.entity.PatientsIdSqlMap;
import com.gennlife.rws.service.*;
import com.gennlife.rws.util.AjaxObject;
import com.gennlife.rws.util.LogUtil;
import com.gennlife.rws.util.SingleExecutorService;
import com.gennlife.rws.util.StringUtils;
import com.gennlife.rws.vo.CustomerStatusEnum;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author liuzhen
 * Date: 2017/10/19
 * Time: 12:07
 */
@RestController
@RequestMapping("/rws")
@Api(description = "rws服务接口")
@ApiModel
public class RwsController {
    private static Logger LOG = LoggerFactory.getLogger(RwsController.class);
    @Autowired
    private ActiveIndexService activeIndexService;
    @Autowired
    private ModuleConvertService moduleConvertService;
    @Autowired
    private SearchByuqlService searchByuqlService;
    @Autowired
    private SearchCrfByuqlService searchCrfByuqlService;
    @Autowired
    private LogUtil logUtil;
    @Autowired
    private ActiveIndexMapper activeIndexMapper;
    @Autowired
    private ContrastiveAnalysisActiveService contrastiveAnalysisActiveService;
    @Autowired
    private ContrastiveAnalysisActiveMapper contrastiveAnalysisActiveMapper;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ActiveSqlMapMapper activeSqlMapMapper;

    @RequestMapping(value = "/saveActiveIndex", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "RWS 配置信息保存", notes = "根据前端提交的信息保存RWS 保存活动/指标/入排条件的定义 Created by liuzhen.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "param", value = "json数据{\"active\":json,\"isSearch\":0}", dataType = "JSONObject", required = true)
    })
    public AjaxObject saveActiveIndex(@RequestBody String param) {
        AjaxObject ajaxObject = null;
        try {
            LOG.info("接收到的计算参数：{}", param);
            JSONObject object = null;
            try {
                object = JSONObject.parseObject(param);
            } catch (Exception e) {
                return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "条件定义不是JSON格式");
            }
            Integer isSearch = object.getInteger("isSearch");
            JSONObject active = object.getJSONObject("active");
            String resultOrderKey = object.getString("resultOrderKey");
            String crfId = object.getString("crfId");
            JSONArray patientsSetId = active.getJSONArray("patientSetId");
            String groupToId = active.getString("groupToId");
            String groupFromId = active.getString("groupFromId");
            Integer isVariant = active.getInteger("isVariant");
            String projectId = active.getString("projectId");
            String create_user = active.getString("create_user");
            String createName = active.getString("createName");
            String name = active.getString("name");
            String activeId = active.getString("id");
            //若是另存为 的数据 判定 名字是不是重复了
            if (isSearch == CommonContent.ACTIVE_TYPE_TEMP_SAVEAS) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("projectId", projectId);
                params.put("name", name);
                params.put("isTmp", 0);
                params.put("isVariant", isVariant);
                List<ActiveIndex> activeIndexList = activeIndexService.findeByActiveName(params);
                if (activeIndexList != null && !activeIndexList.isEmpty()) {
                    if (Objects.nonNull(isVariant) && Objects.equals(1, isVariant)) {
                        return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "同一个项目内的研究变量名称不能重复");
                    }
                }
            }
            //判定是不是二次保存的数据
            Boolean isClsUpdate = active.getBoolean("isClsUpdate") == null ? false : active.getBoolean("isClsUpdate");
            if (isClsUpdate) {
                activeIndexService.deleteByActiveId(activeId);
                contrastiveAnalysisActiveMapper.deleteByActiveIds(activeId);
            }
            String oldName = activeIndexMapper.findActiveName(active.getString("id"));
            boolean judge = isSearch != null && isSearch != 0 && isSearch != 1 && isSearch != 2;

            if (active == null || judge || StringUtils.isEmpty(crfId)) {
                return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "参数错误");
            }
            if (StringUtils.isNotEmpty(crfId) && !UqlConfig.RESULT_ORDER_KEY.containsKey(crfId)) {
                UqlConfig.RESULT_ORDER_KEY.put(crfId, resultOrderKey);
            }
            JSONArray newConfigs = new JSONArray();
            JSONObject actives = object.getJSONObject("active");
            JSONArray configss = actives.getJSONArray("config");
            //枚举修改
            Boolean enumEmity = false;
            JSONArray configsst = moduleConvertService.enumFormat(configss, enumEmity);
            if (configsst != null && configsst.size() > 0 && configsst.getJSONObject(0).containsKey("enumEmpty") && configsst.getJSONObject(0).getBoolean("enumEmpty")) {
                return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "不能输入重复的枚举值");
            }
            int size = configsst == null ? 0 : configsst.size();
            for (int i = 0; i < size; i++) {
                JSONObject config = configsst.getJSONObject(i);
                JSONArray conditions = config.getJSONArray("conditions");
                int length = conditions == null ? 0 : conditions.size();
                JSONArray newCondition = new JSONArray();
                for (int j = 0; j < length; j++) {
                    JSONObject condition = conditions.getJSONObject(j);
                    JSONObject converted = moduleConvertService.uiToRws(condition);
                    newCondition.add(converted);
                }
                config.put("conditions", newCondition);
                newConfigs.add(config);
            }
            actives.put("config", newConfigs);
            System.out.println(object);
            //校验/保存数据并返回保存后的结果，用于判断是否调用PackagingService和前端回显数据
            ajaxObject = activeIndexService.saveOrUpdate(object, groupToId);

            JSONObject obj = JSONObject.parseObject(JSON.toJSONString(ajaxObject.getData()));
            obj.put("patientSetId", patientsSetId);
            obj.put("groupToId", groupToId);
            obj.put("groupFromId", groupFromId);
            Integer activeType = active.getInteger("activeType");
            String indexTypeDesc = configss.getJSONObject(0).getString("indexTypeDesc");

            //处理研究变量的问题
            if (StringUtils.isEmpty(oldName)) {
                oldName = name;
            }
            if (isVariant != null && 1 == isVariant) {
                activeSqlMapMapper.deleteByActiveIndexId(active.getString("id"), UqlConfig.CORT_INDEX_ID);
                if (StringUtils.isEmpty(active.getString("id")) && !isClsUpdate) {
                    String content = createName + "新增 研究变量 ： " + oldName;
                    logUtil.saveLog(projectId, content, create_user, createName);
                } else if (isSearch == 2 && !isClsUpdate) {
                    String content = createName + "新增 研究变量 ： " + name;
                    logUtil.saveLog(projectId, content, create_user, createName);
                } else {
                    if (isClsUpdate) {
                    }
                    contrastiveAnalysisActiveService.deleteContrastiveActiveById(obj.getString("id"), projectId);
                    String content = createName + "编辑 研究变量 ： " + oldName;
                    logUtil.saveLog(projectId, content, create_user, createName);
                }
            }

            List<PatientsIdSqlMap> patientSql = searchByuqlService.getInitialSQLTmp(groupFromId,isVariant == null ? "" : String.valueOf(isVariant),groupToId, obj.getJSONArray("patientSetId"),projectId,crfId);
            String activeIndexId = obj.getJSONArray("config").getJSONObject(0).getString("activeIndexId");//指标id
            String T_activeIndexId = isSearch == CommonContent.ACTIVE_TYPE_TEMP ? activeIndexId.concat("_tmp") : activeIndexId;
            int count = activeSqlMapMapper.getCountByActiveIndexId(T_activeIndexId,groupToId);
            if (count > 0) {
                activeSqlMapMapper.deleteByIndexId(T_activeIndexId);
                if( 2 == activeType){
                    if (UqlConfig.isCrf(crfId)) {
                        searchCrfByuqlService.RunReferenceCalculate(T_activeIndexId,projectId, crfId);
                    }else {
                        searchByuqlService.RunReferenceCalculate(T_activeIndexId,projectId, crfId);
                    }
                }
            }
            //*************  开始计算  *************
            List<Future> futures = new LinkedList<>();
            if (isVariant != null && 1 == isVariant) {
                SingleExecutorService.getInstance().getBackgroundVariantExecutor().submit(() -> patientSql.forEach(o -> {
                    try {
                        searchByUqlService(crfId, activeType, obj, resultOrderKey, isSearch, indexTypeDesc, o);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
            } else {
                patientSql.forEach( o -> futures.add(SingleExecutorService.getInstance().getSearchUqlExecutor().submit(() -> {
                    try {
                        searchByUqlService( crfId, activeType, obj, resultOrderKey, isSearch, indexTypeDesc,o);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                })));
            }
            for (Future future : futures){
                future.get();
            }
            //****************** 计算结束 *****************
            ActiveIndex data = (ActiveIndex) ajaxObject.getData();
            data = data == null ? new ActiveIndex() : data;
            JSONObject o = (JSONObject) JSONObject.toJSON(data);
            //兼容前端json解析
            if (o != null) {
                //字符串格式的["",""]转成数组格式
                String text = o.toString();
                String toWebUI = StringUtils.replace(text, "\"[", "[").replace("]\"", "]").replace("\\\"", "\"");

                JSONObject webUi = JSONObject.parseObject(toWebUI);
                JSONArray webConfig = o.getJSONArray("config");
                JSONArray newWebConfig = new JSONArray();
                int webSize = webConfig == null ? 0 : webConfig.size();
                for (int i = 0; i < webSize; i++) {
                    JSONObject config = webConfig.getJSONObject(i);
                    JSONArray conditions = config.getJSONArray("conditions");
                    int length = conditions == null ? 0 : conditions.size();
                    JSONArray newCondition = new JSONArray();
                    for (int j = 0; j < length; j++) {
                        JSONObject condition = conditions.getJSONObject(j);
                        JSONObject converted = moduleConvertService.rwsToUi(condition);
                        newCondition.add(converted);
                    }
                    config.put("conditions", newCondition);
                    newWebConfig.add(config);
                }
                //枚举修改
                JSONArray conditionNew = moduleConvertService.enumFormatToUi(newWebConfig);
                webUi.put("config", conditionNew);
                webUi.remove("crfId");
                JSONObject activeResult = new JSONObject();
                activeResult.put("active", webUi);
                activeResult.put("crfId", crfId);
                ajaxObject.setData(activeResult);
            }
        } catch (Exception e) {
            LOG.error("保存失败，异常信息为{}", e);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, AjaxObject.AJAX_MESSAGE_FAILURE);
        }

        return ajaxObject;
    }

    private void searchByUqlService(String crfId, Integer activeType, JSONObject obj, String resultOrderKey, Integer isSearch, String indexTypeDesc, PatientsIdSqlMap patientSql) throws ExecutionException, InterruptedException, IOException {

        if (UqlConfig.isCrf(crfId)) {
            if (3 == activeType) {//那排
                searchCrfByuqlService.SearchByExclude(obj, resultOrderKey, isSearch, patientSql, crfId);
            } else if ("自定义枚举类型".equals(indexTypeDesc)) {//处理枚举
                searchCrfByuqlService.SearchByEnume(obj, resultOrderKey, isSearch, crfId);
            } else if (2 == activeType) {//指标
                searchCrfByuqlService.SearchByIndex(obj, resultOrderKey, isSearch, crfId);
            } else if (1 == activeType) { //事件
                searchCrfByuqlService.searchByActive(obj, resultOrderKey, isSearch, crfId);
            }
        } else {
            if (3 == activeType) {//那排
                searchByuqlService.SearchByExclude(obj, resultOrderKey, isSearch,patientSql, crfId);
            } else if ("自定义枚举类型".equals(indexTypeDesc)) {//处理枚举
                searchByuqlService.SearchByEnume(obj, resultOrderKey, isSearch, patientSql, crfId);
            } else if (2 == activeType) {//指标
                searchByuqlService.SearchByIndex(obj, resultOrderKey, isSearch, patientSql, crfId);
            } else if (1 == activeType) { //事件
                searchByuqlService.searchByActive(obj, resultOrderKey, isSearch, patientSql, crfId);
            }
        }
    }

    /**
     * 更新活动定义信息
     *
     * @param activeIndex
     * @return
     */
    @RequestMapping(value = "/updateActiveIndex", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "RWS 配置信息更新接口，目前此接口不用，更新和新增全部用保存的接口", notes = "根据前端提交的信息保存RWS 更新活动/指标/入排条件的定义，此接口暂时不用Created by liuzhen.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "activeIndex", value = "activeIndex", paramType = "query", dataType = "String", required = true)
    })
    public AjaxObject updateActiveIndex(String activeIndex) {
        LOG.info("接收到需要保存的自定义信息为{}", activeIndex);
        AjaxObject ajaxObject = null;
        try {
            JSONObject active = JSONObject.parseObject(activeIndex);
            ajaxObject = activeIndexService.updateActive(active);
            if (ajaxObject != null && AjaxObject.AJAX_STATUS_SUCCESS == ajaxObject.getStatus()) {
                boolean call = (boolean) ajaxObject.getData();
                //data为true说明数据发生变化
                if (call) {
                    //调用packaging serivce的接口
                }
            }
            LOG.debug("保存成功，返回的信息为{}", ajaxObject.toString());
        } catch (Exception e) {
            LOG.error("跟新失败，异常信息为{}", e);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, AjaxObject.AJAX_MESSAGE_FAILURE + e);
        }
        return ajaxObject;
    }

    @RequestMapping(value = "/findByProjectId", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "根据项目id，获取本项目下所有的已定义的活动", notes = "获取项目下所有已定义的事件/指标列表 Created by liuzhen.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "param", value = "获取所有已定义的指标和事件{\"projectId\":\"项目id\",\"type\":\"事件类型\",\"name\":\"事件名称\",\"pageNum\":1,\"pageSize\":10}", dataType = "JSONObject", required = true)
    })
    public AjaxObject findByProjectId(@RequestBody String param/*String projectId,int type, Integer pageNum,Integer pageSize*/) {
        AjaxObject object = new AjaxObject();
        try {
            JSONObject params = JSONObject.parseObject(param);
            String projectId = params.getString("projectId").replaceAll("-", "");
            String name = params.getString("name");
            Integer type = params.getInteger("type");
            Integer pageNum = params.getInteger("pageNum");
            Integer pageSize = params.getInteger("pageSize");
            if (StringUtils.isEmpty(projectId) || type == null || pageNum == null || pageSize == null) {
                return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "参数错误");
            }
            object = activeIndexService.findByProjectId(projectId, type, name, pageNum, pageSize);
        } catch (JSONException e) {
            object.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            object.setMessage(AjaxObject.AJAX_MESSAGE_FAILURE + e.getMessage());
        } catch (Exception e) {
            object.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            object.setMessage(AjaxObject.AJAX_MESSAGE_FAILURE + e.getMessage());
        }
        return object;
    }

    @RequestMapping(value = "/deleteByActiveId", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "根据活动id删除活动的全部信息", notes = "获取项目下所有已定义的事件/指标列表 Created by liuzhen.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "param", value = "参数{\"activeId\":\"adadfasdad\"}", dataType = "JSONObject", required = true)
    })
    public AjaxObject deleteByActiveId(@RequestBody String param) {
        AjaxObject ajaxObject = new AjaxObject();
        String activeId = null;
        try {
            JSONObject params = JSONObject.parseObject(param);
            activeId = params.getString("activeId");
            String createId = params.getString("createId");
            String createName = params.getString("createName");
            String activeName = params.getString("activeName");
            String projectId = params.getString("projectId");
            Integer isVariant = params.getInteger("isVariant");
            if (StringUtils.isEmpty(activeId)) {
                return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "参数错误");
            }
            if (isVariant != null && 1 == isVariant) {
                String content = createName + "新增 研究变量 ： " + activeName;
                logUtil.saveLog(projectId, content, createId, createName);
            }
            ajaxObject = activeIndexService.deleteByActiveId(activeId);
            LOG.info("删除活动id 为{}的指标或活动成功", activeId);
        } catch (JSONException e) {
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage(AjaxObject.AJAX_MESSAGE_FAILURE + e.getMessage());
        } catch (Exception e) {
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage(AjaxObject.AJAX_MESSAGE_FAILURE + e.getMessage());
        }
        return ajaxObject;
    }

    @RequestMapping(value = "/dependenceChange", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "判断是否有活动/指标依赖于此活动或指标", notes = "获取项目下所有已定义的事件/指标列表 Created by liuzhen.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "param", value = "参数{\"active\":\"adadfasdad\"}", dataType = "JSONObject", required = true)
    })
    public AjaxObject dependenceChange(@RequestBody String param) {
        AjaxObject ajaxObject = new AjaxObject();
        try {
            JSONObject object = JSONObject.parseObject(param);
            JSONObject active = object.getJSONObject("active");
            String activeId = active.getString("activeId");
            activeId = StringUtils.isEmpty(activeId) ? active.getString("id") : activeId;
            if (StringUtils.isEmpty(activeId)) {
                return ajaxObject;
            }
//            List<ActiveIndexConfigCondition> configConditions = activeIndexService.dependenceCurActive(activeId);
            List<ActiveIndex> activeIndices = activeIndexService.dependenceCurActiveByIsTmp(activeId);
            JSONObject actives = object.getJSONObject("active");
            JSONArray configss = actives.getJSONArray("config");
            //枚举修改
            Boolean isEmity = false;
            JSONArray configsst = moduleConvertService.enumFormat(configss, isEmity);
            int size = configsst == null ? 0 : configsst.size();
            JSONArray newConfigs = new JSONArray();
            for (int i = 0; i < size; i++) {
                JSONObject config = configsst.getJSONObject(i);
                JSONArray conditions = config.getJSONArray("conditions");
                int length = conditions == null ? 0 : conditions.size();
                JSONArray newCondition = new JSONArray();
                for (int j = 0; j < length; j++) {
                    JSONObject condition = conditions.getJSONObject(j);
                    JSONObject converted = moduleConvertService.uiToRws(condition);
                    newCondition.add(converted);
                }
                config.put("conditions", newCondition);
                newConfigs.add(config);
            }
            actives.put("config", newConfigs);
            /*搜索结果类型改变问题*/
            if (CommonContent.ACTIVE_TYPE_INDEX == (active.getInteger("activeType"))) {
                String indexType = actives.getJSONArray("config").getJSONObject(0).getString("indexType");
                String oindexType = activeIndexService.getindexType(active.getString("id"));
                if (StringUtils.isEmpty(oindexType)) {
                    return ajaxObject;
                }
                List<String> activeName = activeIndexService.getActiveName(active.getString("id"));
                String name = String.join(";", activeName);
                if (!oindexType.equals(indexType) && activeName != null && activeName.size() != 0) {
                    return new AjaxObject(AjaxObject.AJAX_STATUS_TIPS, "您修改的指标已被其他指标引用 ( " + name + " ) ，取消引用后方可进行修改", 0);
                }
            }
            boolean change = activeIndexService.conditioIsnChange(active);
            if (activeIndices != null && !activeIndices.isEmpty() && change) {
                return new AjaxObject(AjaxObject.AJAX_STATUS_TIPS, "当前事件或指标的定义发生变化，若保存，需要同步更新关联事件、指标和纳排的计算结果。点击“确定”执行保存当前修改，点击“取消”放弃保存当前修改。");
            }
        } catch (Exception e) {
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage("请求出错" + e.getMessage());
            LOG.error(e.toString());
        }
        return ajaxObject;
    }

    @RequestMapping(value = "/activeIsChange", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "判断是否有活动/指标依赖于此活动或指标", notes = "获取项目下所有已定义的事件/指标列表 Created by liuzhen.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "param", value = "参数{\"active\":\"adadfasdad\"}", dataType = "JSONObject", required = true)
    })
    public AjaxObject activeIsChange(@RequestBody String param) {
        AjaxObject ajaxObject = new AjaxObject();
        try {
            if (StringUtils.isEmpty(param)) {
                ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
                ajaxObject.setMessage("参数不能为空");
            }
            JSONObject object = JSONObject.parseObject(param);
            JSONObject active = object.getJSONObject("active");
            boolean change = activeIndexService.conditioIsnChange(active);
            ajaxObject.setData(change);
        } catch (Exception e) {
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage("系统异常" + e);
        }
        return ajaxObject;
    }

    @RequestMapping(value = "/dependenceActives", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "根据活动id删除活动的全部信息", notes = "获取项目下所有已定义的事件/指标列表 Created by liuzhen.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "param", value = "参数{\"activeId\":\"adadfasdad\"}", dataType = "JSONObject", required = true)
    })
    public AjaxObject dependenceActives(@RequestBody String param) {
        AjaxObject ajaxObject = new AjaxObject();
        try {
            JSONObject params = JSONObject.parseObject(param);
            String activeId = params.getString("activeId");
            ajaxObject = activeIndexService.judgeActiveDependence(activeId);
        } catch (Exception e) {
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage("获取数据失败" + e.getMessage());
        }
        return ajaxObject;
    }

    @RequestMapping(value = "/getActive", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "根据活动id或projectId和activeType获取活动的全部信息", notes = "获取某个活动的全部信息 Created by liuzhen.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "param", value = "参数{\"activeId\":\"23412a234123\",\"projectId\":\"\",\"activeType\":3} 当ActiveType=1或2时，可以只传activeId，当activeType=3时，可以只填projectId和activeType", dataType = "JSONObject", required = true)
    })
    public AjaxObject getActive(@RequestBody String param) {
        ActiveIndex active = null;
        AjaxObject ajaxObject = new AjaxObject();
        try {
            JSONObject params = JSONObject.parseObject(param);
            String activeId = params.getString("activeId");
            String projectId = params.getString("projectId") == null ? null : params.getString("projectId").replaceAll("-", "");
            Integer activeType = params.getInteger("activeType");
            String groupFromId = params.getString("groupToId");
            if (StringUtils.isEmpty(activeId) && StringUtils.isEmpty(projectId)) {
                return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "参数错误");
            }

//            if (activeType != null && activeType == CommonContent.ACTIVE_TYPE_INOUTN.intValue()) {
//                List<ActiveIndex> activeIndexList = activeIndexService.findeByProjectAndType(projectId, activeType);
//                if (activeIndexList != null && !activeIndexList.isEmpty()) {
//                    ActiveIndex index = activeIndexList.get(0);
//                    activeId = index.getId();
//                }
//            }
            if (activeType != null && activeType == CommonContent.ACTIVE_TYPE_INOUTN.intValue()) {
                activeId = groupFromId;
            }
            active = activeIndexService.findByActiveId(activeId);
            active = active == null ? new ActiveIndex() : active;
            JSONObject o = (JSONObject) JSONObject.toJSON(active);
            //兼容前端json解析
            if (o != null) {
                //字符串格式的["",""]转成数组格式
                String text = o.toString();
                String toWebUI = StringUtils.replace(text, "\"[", "[").replace("]\"", "]").replace("\\\"", "\"");

                JSONObject webUi = JSONObject.parseObject(toWebUI);
                JSONArray webConfig = o.getJSONArray("config");
                JSONArray newWebConfig = new JSONArray();
                int webSize = webConfig == null ? 0 : webConfig.size();
                //如果数据为空 则结束
                if (webSize == 0) {
                    ajaxObject.setData(null);
                    return ajaxObject;
                }
                for (int i = 0; i < webSize; i++) {
                    JSONObject config = webConfig.getJSONObject(i);
                    JSONArray conditions = config.getJSONArray("conditions");
                    int length = conditions == null ? 0 : conditions.size();
                    JSONArray newCondition = new JSONArray();
                    for (int j = 0; j < length; j++) {
                        JSONObject condition = conditions.getJSONObject(j);
                        JSONObject converted = moduleConvertService.rwsToUi(condition);
                        newCondition.add(converted);
                    }
                    config.put("conditions", newCondition);
                    newWebConfig.add(config);
                }
                //枚举修改
                JSONArray conditionNew = moduleConvertService.enumFormatToUi(newWebConfig);
                webUi.put("config", conditionNew);
                JSONObject activeResult = new JSONObject();
                activeResult.put("active", webUi);
                ajaxObject.setData(activeResult);
            }
        } catch (JSONException e) {
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage(AjaxObject.AJAX_MESSAGE_FAILURE + e.getMessage());
        } catch (Exception e) {
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage(AjaxObject.AJAX_MESSAGE_FAILURE + e.getMessage());
        }
        return ajaxObject;
    }

    @ApiOperation(value = "根据项目id和指标类型，获取下拉选择框数据", notes = "根据项目id和指标类型，获取下拉选择框数据 Created by liuzhen.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "object", value = "项目id,{\"projectId\":\"18912313132123\",\"type\":1}", dataType = "JSONObject", required = true),
    })
    @RequestMapping(value = "/getAllActiveOrIndex", method = {RequestMethod.GET, RequestMethod.POST})
    public AjaxObject getAllActiveOrIndex(@RequestBody JSONObject object /*String projectId,Integer type*/) {
        AjaxObject ajaxObject = new AjaxObject();
        try {
            String projectId = object.getString("projectId").replaceAll("-", "");
            Integer type = object.getInteger("type");
            String activeId = object.getString("activeId");
            String name = object.getString("name");
            String isTwiceIndex = object.getString("isTwiceIndex");
            String depActiveId = object.getString("dePactiveId");
            if (StringUtils.isEmpty(projectId) || type == null) {
                return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "参数错误");
            }
            ajaxObject = activeIndexService.findByProjectIdAndTypeNoPage(activeId, projectId, type, name, isTwiceIndex, depActiveId);
        } catch (Exception e) {
            LOG.error("查询自定义活动/指标时出错，错误信息{}", e);
            ajaxObject.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            ajaxObject.setMessage(AjaxObject.AJAX_MESSAGE_FAILURE);
        }
        return ajaxObject;
    }

    @ApiOperation(value = "校验名称是否存在", notes = "校验名称是否存在 Created by liuzhen.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "param", value = "项目id,{\"projectId\":\"18912313132123\",\"name\":\"\"}", dataType = "JSONObject", required = true),
    })
    @RequestMapping(value = "/activeIsExists", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject activeIsExists(@RequestBody String param) {
        JSONObject jsonObject = JSONObject.parseObject(param);
        String projectId = jsonObject.getString("projectId").replaceAll("-", "");
        String name = jsonObject.getString("name");
        Integer type = jsonObject.getInteger("type");
        String isVariant = jsonObject.getString("isVariant");
        if (StringUtils.isEmpty(projectId) || StringUtils.isEmpty(name)) {
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "项目名称和指标/活动名称不能为空");
        }
        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("projectId", projectId);
        params.put("name", name);
        params.put("isTmp", 0);
        params.put("isVariant", isVariant);
        params.put("activeType", type);
        List<ActiveIndex> activeIndexList = activeIndexService.findeByActiveName(params);
        if (activeIndexList != null && !activeIndexList.isEmpty()) {
            if (StringUtils.isNotEmpty(isVariant) && "1".equals(isVariant)) {
                return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "同一个项目内的研究变量名称不能重复");
            } else {
                return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "该项目下已存在名称为'" + name + "'的指标或事件");
            }
        }
        return new AjaxObject();
    }

    @ApiOperation(value = "修改指标或者事件名称", notes = "修改指标或者事件名称 Created by lmx.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "param", value = "项目id,{\"activeid\":\"18912313132123\",\"name\":\"\"}", dataType = "JSONObject", required = true),
    })
    @RequestMapping(value = "/editActiveName", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject editActiveName(@RequestBody String param) {
        JSONObject jsonObject = JSONObject.parseObject(param);
        String activeId = jsonObject.getString("activeId");
        String name = jsonObject.getString("name");
        String oldName = activeIndexMapper.findActiveName(activeId);
        Integer edited = jsonObject.getInteger("edited");
        Integer isVariant = jsonObject.getInteger("isVariant");
        String createName = jsonObject.getString("createName");
        String projectId = jsonObject.getString("projectId");
        String create_user = jsonObject.getString("createId");
        List<ActiveIndexConfigCondition> condition = activeIndexService.findByRefActiveId(activeId);
        if (condition != null && !condition.isEmpty() && edited != null && edited == 1) {
            return new AjaxObject(AjaxObject.AJAX_STATUS_TIPS, "当前要修改名称的指标/事件被其他事件依赖，修改后会重新刷新当前编辑的页面内容，可能导致您得数据丢失，是否确认修改?");
        }
        if (StringUtils.isEmpty(activeId) || StringUtils.isEmpty(name)) {
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "指标/活动 id 或名称不能为空");
        }
        if (isVariant != null && 1 == isVariant) {
            String content = createName + "编辑 研究变量 ： " + oldName;
            logUtil.saveLog(projectId, content, create_user, createName);
        }

        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("id", activeId);
        params.put("name", name);
        boolean result = activeIndexService.editActiveName(params);
        if (result) {
            return new AjaxObject();
        } else {
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "修改失败");
        }

    }

    @RequestMapping(value = "/export/getCortastivePatientSn", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject getCortastivePatientSn(@RequestBody String param) {
        AjaxObject ajaxObject = null;
        try {
            JSONObject object = null;
            try {
                object = JSONObject.parseObject(param);
            } catch (Exception e) {
                ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
                    CustomerStatusEnum.FORMATJSONERROR.getMessage());
                return ajaxObject;
            }
            ajaxObject = projectService.getCortastivePatientSn(object);
        } catch (Exception e) {
            LOG.error("获取数据列表，异常信息{}", e);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, AjaxObject.AJAX_MESSAGE_FAILURE);
        }
        return ajaxObject;
    }
}
