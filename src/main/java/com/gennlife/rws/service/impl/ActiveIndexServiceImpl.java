/**
 * copyRight
 */
package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.gennlife.rws.content.CommonContent;
import com.gennlife.rws.content.SeparatorContent;
import com.gennlife.rws.content.UqlConfig;
import com.gennlife.rws.dao.*;
import com.gennlife.rws.entity.*;
import com.gennlife.rws.service.ActiveIndexService;
import com.gennlife.rws.service.ActiveIndexTaskService;
import com.gennlife.rws.service.ContrastiveAnalysisActiveService;
import com.gennlife.rws.service.RedisMapDataService;
import com.gennlife.rws.util.AjaxObject;
import com.gennlife.rws.util.CodeToDesc;
import com.gennlife.rws.util.StringUtils;
import com.gennlife.rws.vo.ActiveIndexVo;
import com.gennlife.rws.web.WebAPIResult;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.stream.Collectors.joining;

//import com.gennlife.rws.service.CallPackagingServerService;

/**
 * Created by liuzhen.
 * Date: 2017/10/19
 * Time: 12:09
 *
 * @author liuzhen
 */
@Service
@Transactional(rollbackFor = RuntimeException.class)
public class ActiveIndexServiceImpl implements ActiveIndexService {
    private static Logger LOG = LoggerFactory.getLogger(ActiveIndexServiceImpl.class);
    @Autowired
    private ActiveIndexMapper activeIndexMapper;
    @Autowired
    private ActiveIndexConfigMapper activeIndexConfigMapper;
    @Autowired
    private ActiveIndexConfigConditionMapper conditionMapper;
    @Autowired
    private ActiveIndexTaskService taskService;
    @Autowired
    private ActiveIndexTaskMapper taskMapper;
//    @Autowired
//    private CallPackagingServerService serverService;
    @Autowired
    private ContrastiveAnalysisActiveMapper contrastiveAnalysisActiveMapper;
    @Autowired
    private RedisMapDataService redisMapDataService;


    private JSONObject jsonOrderKeys;

    /**
     * 活动保存功能
     *
     * @param activeIndex
     * @return
     */
    @Override
    public AjaxObject saveActive(JSONObject activeIndex) {
        ActiveIndex actives = convertJsonToActive(activeIndex, 0);
        if (StringUtils.isEmpty(actives.getName(), actives.getProjectId(), actives.getProjectName())) {
            String message = "活动名称/项目id/项目名称不能为空";
            LOG.debug(message);
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, message);
        }
        actives.setMark(activeIndex.toJSONString());
        String id = actives.getId();
        List<ActiveIndexConfig> activeIndexConfigs = convertJsonToConfig(activeIndex, id, 0);
        activeIndexMapper.insertSelective(actives);
        if (activeIndexConfigs != null && !activeIndexConfigs.isEmpty()) {
            activeIndexConfigMapper.insertBatch(activeIndexConfigs);
        }
        for (ActiveIndexConfig config : activeIndexConfigs) {
            List<ActiveIndexConfigCondition> configCondition = config.getConditions();
            if (configCondition != null && !configCondition.isEmpty()) {
                conditionMapper.insertBatch(configCondition);
            }
        }
        LOG.info("数据保存成功，正常返回");
        return new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
    }

    @Override
    public AjaxObject updateActive(JSONObject activeIndex) {
        LOG.debug(activeIndex.toJSONString());
        boolean configCallFlag = false;
        boolean conDiCallFlag = false;
        ActiveIndex actives = convertJsonToActive(activeIndex, 0);
        if (StringUtils.isEmpty(actives.getName(), actives.getProjectId(), actives.getProjectName())) {
            String message = "活动名称/项目id/项目名称不能为空";
            LOG.debug(message);
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, message);
        }
        actives.setMark(activeIndex.toJSONString());
        actives.setCreateTime(new Date());
        actives.setUpdateTime(new Date());
        String id = actives.getId();
        if (StringUtils.isEmpty(id)) {
            String message = "数据解析错误无法找到活动";
            LOG.debug(message);
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, message);
        }
        ActiveIndex activeIndex1 = activeIndexMapper.selectByPrimaryKey(id);
        //未修改过则不进行更新
        if (!actives.equals(activeIndex1)) {
            activeIndexMapper.insertSelective(actives);
        }
        List<ActiveIndexConfig> activeIndexConfigs = convertJsonToConfig(activeIndex, id, 0);
        if (activeIndexConfigs != null && !activeIndexConfigs.isEmpty()) {
            List<ActiveIndexConfig> indexConfigs = activeIndexConfigMapper.findAllByActiveIndexId(id);
            for (ActiveIndexConfig con1 : activeIndexConfigs) {
                if (indexConfigs != null && !indexConfigs.isEmpty()) {
                    for (ActiveIndexConfig con2 : indexConfigs) {
                        if (StringUtils.equals(con1.getId(), con2.getId()) && !con1.equals(con2)) {
                            configCallFlag = true;
                            break;
                        }
                    }
                } else {
                    configCallFlag = true;
                }
                if (configCallFlag) {
                    break;
                }
            }
        }
        if (configCallFlag) {
            activeIndexConfigMapper.updateBatch(activeIndexConfigs);
        }
        for (ActiveIndexConfig config : activeIndexConfigs) {
            List<ActiveIndexConfigCondition> configCondition = config.getConditions();
            if (configCondition != null && !configCondition.isEmpty()) {
                List<ActiveIndexConfigCondition> configConditions = conditionMapper.findAllByActiveIndexConfigId(config.getId());
                for (ActiveIndexConfigCondition con1 : configConditions) {
                    if (configConditions != null && !configConditions.isEmpty()) {
                        for (ActiveIndexConfigCondition con2 : configCondition) {
                            if (StringUtils.equals(con1.getId(), con2.getId()) && !con1.equals(con2)) {
                                conDiCallFlag = true;
                                break;
                            }
                        }
                    } else {
                        conDiCallFlag = true;
                    }
                }
                if (conDiCallFlag) {
                    conditionMapper.insertBatch(configCondition);
                }
            }
        }
        LOG.info("数据保存成功，正常返回");
        return new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS, actives, (configCallFlag || conDiCallFlag));
    }

    @Override
    public AjaxObject saveOrUpdate(JSONObject active, String groupToId) {

        long start = System.currentTimeMillis();
        AjaxObject object = new AjaxObject();
        boolean insertFlag = false;
        boolean changeFlag = false;
        ActiveIndex actives = null;
        JSONObject activeJSONObject = active.getJSONObject("active");
        //是否为检索
        String activeType = activeJSONObject.getString("activeType");
        Integer isTmp = active.getInteger("isSearch");
        String activeId = activeJSONObject.getString("id");
        String projectId = activeJSONObject.getString("projectId").replaceAll("-", "");
        String conId = activeJSONObject.getString("confirmActiveId");
        activeJSONObject.put("projectId", projectId);
        String conActiveId = StringUtils.isEmpty(conId) ? "" : conId;
        if(Integer.valueOf(activeType)== CommonContent.ACTIVE_TYPE_INOUTN && isTmp ==CommonContent.ACTIVE_TYPE_NOTEMP ){//是否為那排
            activeId = groupToId;
        }
        ActiveIndex index = this.findByActiveId(activeId);
        int type = getType(index, isTmp);
        conActiveId = StringUtils.isEmpty(conActiveId) ? StringUtils.getUUID() : conActiveId;
        actives = getActiveIndex(activeJSONObject, type);
        if(Integer.valueOf(activeType)== CommonContent.ACTIVE_TYPE_INOUTN && isTmp ==CommonContent.ACTIVE_TYPE_NOTEMP ){//是否為那排
            actives.setId(groupToId);
            List<ActiveIndexConfig> activeIndexConfigs = actives.getConfig();
            for (ActiveIndexConfig activeIndexConfig : activeIndexConfigs){
                activeIndexConfig.setActiveIndexId(groupToId);
                activeIndexConfig.setActiveIndexId(groupToId);
            }
        }
        if (isTmp == CommonContent.ACTIVE_TYPE_TEMP_SAVEAS) {
            /*另存操作*/
            actives.setIsTmp(CommonContent.ACTIVE_TYPE_NOTEMP);
        } else {
            actives.setIsTmp(isTmp);
        }
        StringBuffer depRelations = new StringBuffer();
        depRelations.append("产生循环依赖：");
        depRelations.append(actives.getName());
        judgeLoopDep(actives.getConfig(), actives.getId(), depRelations);
        boolean contains = StringUtils.contains(depRelations, ";TRUEFLAG");
        if (contains) {
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "条件中存在环状依赖关系！");
        }
        ActiveIndexConfig config1 = (actives.getConfig() != null && !actives.getConfig().isEmpty()) ? actives.getConfig().get(0) : new ActiveIndexConfig();
        String activeResult = config1.getActiveResult();
        if (actives.getActiveType().intValue() == CommonContent.ACTIVE_TYPE_INDEX.intValue()) {
            activeResult = config1.getIndexColumn();
        }
        if (StringUtils.isEmpty(activeResult) && actives.getActiveType().intValue() == CommonContent.ACTIVE_TYPE_EVENT.intValue()) {
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "activeResult不能为空");
        }
        if (actives.getActiveType().intValue() == CommonContent.ACTIVE_TYPE_INDEX.intValue()) {
            activeResult = StringUtils.substringBeforeLast(activeResult, ".");
        }
        String resultOrderKey = active.getString("resultOrderKey");
        JSONObject keys = JSONObject.parseObject(resultOrderKey);
        String sortKey = StringUtils.isEmpty(keys.getString(activeResult)) ? "" : keys.getString(activeResult);
        actives.setSortKey(sortKey);
        List<ActiveIndexConfig> configs = actives.getConfig();
        JSONArray dataGroup = getDataGroup(configs);
        if (actives.getActiveType().intValue() != CommonContent.ACTIVE_TYPE_INOUTN.intValue() && StringUtils.isNotEmpty(activeResult) && StringUtils.split(activeResult, ".").length >= 2) {
            String resultGroup = StringUtils.split(activeResult, ".")[1];
            if (StringUtils.isNotEmpty(resultGroup) && !dataGroup.contains(resultGroup)) {
                dataGroup.add(resultGroup);
            }
        }

        actives.setDataGroup(dataGroup.toJSONString());
        Date date = new Date();
        AjaxObject message = basicValidate(object, actives, actives.getConfig(), activeJSONObject, isTmp);
        if (message != null && message.getStatus() == AjaxObject.AJAX_STATUS_FAILURE) {
            return message;
        }
        if (isTmp == CommonContent.ACTIVE_TYPE_TEMP) {
            if (type != 1 && index != null) {
                actives.setConfirmActiveId(conActiveId);
                JSONObject actJson = (JSONObject) JSON.toJSON(index);
                List<ActiveIndexConfig> actConfigs = convertJsonToConfig(actJson, index.getId(), 0);
                List<ActiveIndexConfig> config = actives.getConfig();
                boolean change = true;//isChange1(config, actConfigs);
                if (change) {
                    changeFlag = true;
                    if (index.getIsTmp() == 0) {
                        insertFlag = true;
                        actives = getActiveIndex(activeJSONObject, CommonContent.ACTIVE_TYPE_TEMP);
                        actives.setDataGroup(dataGroup.toJSONString());
                        actives.setConfirmActiveId(conActiveId);
                        actives.setCreateTime(date);
                        actives.setUpdateTime(date);
                        actives.setIsTmp(CommonContent.ACTIVE_TYPE_TEMP);
                        activeIndexMapper.insert(actives);
                        executeSave(actives.getConfig());
                    } else {
                        actives.setUpdateTime(date);
                        activeIndexMapper.updateByPrimaryKeySelective(actives);
                        activeId = actives.getId();
                        this.deleteActiveForSave(activeId);
                        //因为前端传的参数诡异 后端彻底删除数据行为
                        this.deleteByConfig(config);
                        executeSave(config);
                    }
                } /*else {
                    actives.setIsTmp(index.getIsTmp());
                    activeIndexMapper.updateByPrimaryKeySelective(actives);
                }*/
            } else {
                insertFlag = true;
                actives.setConfirmActiveId(conActiveId);
                actives.setCreateTime(date);
                actives.setUpdateTime(date);
                activeIndexMapper.insert(actives);
                executeSave(actives.getConfig());
            }
            activeId = actives.getId();

        } else if (isTmp == CommonContent.ACTIVE_TYPE_TEMP_SAVEAS) {
            insertFlag = true;
            actives.setConfirmActiveId(StringUtils.getUUID());
            actives.setCreateTime(date);
            actives.setUpdateTime(date);
            activeIndexMapper.insert(actives);
            executeSave(actives.getConfig());
            activeId = actives.getId();
        } else {
            if (index != null) {
                actives.setConfirmActiveId(conActiveId);
                JSONObject actJson = (JSONObject) JSON.toJSON(index);
                List<ActiveIndexConfig> actConfigs = convertJsonToConfig(actJson, index.getId(), 0);
                List<ActiveIndexConfig> config = actives.getConfig();
                boolean change = true;//isChange1(config, actConfigs);
                actives.setConfirmActiveId(conActiveId);
                ActiveIndex confirmActive = getActiveIndexByConfirm(projectId, conActiveId, isTmp);
                if (confirmActive == null) {
                    confirmActive = getActiveIndexByConfirm(projectId, conActiveId, Math.abs(type - 1));
                }
                activeId = confirmActive == null ? "" : confirmActive.getId();
                if (change) {
                    changeFlag = true;
                    actives.setUpdateTime(date);
                    if (actives.getIsTmp().intValue() != index.getIsTmp().intValue()) {
                        changeFlag = true;
                        if (confirmActive.getIsTmp().intValue() == 0) {
                            this.deleteByActiveIdTemp(activeId);
                        } else {
                            this.deleteActiveForSave(activeId);
                        }
                        this.deleteByConfig(config);
                        executeSave(config);
                        activeIndexMapper.updateTempToActive(activeId, isTmp, actives.getId(), actives.getName());
                        String id = actives.getId();
                        activeIndexConfigMapper.updateActiveIdToTemp(id, activeId);
                    } else {
                        activeIndexMapper.updateByPrimaryKeySelective(actives);
                        activeId = actives.getId();
                        this.deleteActiveForSave(activeId);
                        //因为前端传的参数诡异 后端彻底删除数据行为
                        this.deleteByConfig(config);
                        executeSave(config);
                    }
                } else {
                    if (StringUtils.equals(activeId, actives.getId())/*&&index.getIsTmp().intValue()!=actives.getIsTmp()*/) {
                        if (index.getIsTmp() == 1) {
                            //查询之后直接保存此时，需要调用计算服务
                            changeFlag = true;
                        }
                        actives.setUpdateTime(date);
                        activeIndexMapper.updateByPrimaryKeySelective(actives);
                    } else if (StringUtils.isNotEmpty(activeId) && index.getIsTmp().intValue() != isTmp) {
                        changeFlag = true;
                        this.deleteByActiveIdTemp(activeId);
                        activeIndexMapper.updateTempToActive(activeId, isTmp, actives.getId(), actives.getName());
                        activeIndexConfigMapper.updateActiveIdToTemp(actives.getId(), activeId);
                    }
                }
            } else {
                insertFlag = true;
                actives.setConfirmActiveId(conActiveId);
                activeId = actives.getId();
                actives.setCreateTime(date);
                actives.setUpdateTime(date);
                this.deleteActiveForSave(activeId);
                //因为前端传的参数诡异 后端彻底删除数据行为
                this.deleteByConfig(actives.getConfig());
                activeIndexMapper.insert(actives);
                executeSave(actives.getConfig());
            }
        }
        object.setFlag(insertFlag || changeFlag);
        LOG.info("处理数据及保存用时={}", (System.currentTimeMillis() - start));
        long sStart = System.currentTimeMillis();
        object.setData(this.findByActiveId(activeId));
        LOG.info("查询数据用时={}", (System.currentTimeMillis() - sStart));
        LOG.info("处理成功");
        return object;
    }

    private void deleteByConfig(List<ActiveIndexConfig> activeIndexConfigs) {
        if (activeIndexConfigs != null && !activeIndexConfigs.isEmpty()) {
            for (ActiveIndexConfig config : activeIndexConfigs) {
                List<ActiveIndexConfigCondition> configCondition = config.getConditions();
                if (configCondition != null && !configCondition.isEmpty()) {
                    String configId = configCondition.get(0).getActiveIndexConfigId();
                    conditionMapper.deleteByActiveIndexConfigId(configId);
                }
            }
        }
    }

    private JSONArray getDataGroup(List<ActiveIndexConfig> configs) {
        JSONArray dataGroup = new JSONArray();
        dataGroup.add("patient_info");
        if (configs != null && !configs.isEmpty()) {
            for (ActiveIndexConfig config : configs) {
                List<ActiveIndexConfigCondition> conditions = config.getConditions();
                for (ActiveIndexConfigCondition condition : conditions) {
                    String sourceTagName = condition.getSourceTagName();
                    if (StringUtils.contains(sourceTagName, ".")) {
                        String cs1 = StringUtils.substringBeforeLast(sourceTagName, ".");
                        if (StringUtils.containsIgnoreCase(cs1, CommonContent.EMR_PATIENT_INFO)) {
                            if (!dataGroup.contains(cs1)) {
                                dataGroup.add(cs1);
                            }
                        } else if (StringUtils.containsIgnoreCase(cs1, CommonContent.EMR_VISIT)) {
                            String visits = StringUtils.substringBetween(sourceTagName, ".", ".");
                            if (!dataGroup.contains(visits)) {
                                dataGroup.add(visits);
                            }
                        }
                    }
                }
            }
        }
        return dataGroup;
    }

    @Override
    public boolean conditioIsnChange(JSONObject active) {
        String name = active.getString("name");
        String confirmActiveId = active.getString("confirmActiveId");
        if (StringUtils.isEmpty(confirmActiveId)) {
            return false;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("activeConfirmId", confirmActiveId);
        param.put("isTmp", 0);
        ActiveIndex activeIndex = activeIndexMapper.findByConfirmIdAndType(param);
        if (activeIndex == null) {
            return false;
        }
        String oldName = activeIndex.getName();
        if(StringUtils.isNotEmpty(name) && !name.equals(oldName)){
            return true;
        }
        ActiveIndex index = this.findByActiveId(activeIndex.getId());
        ActiveIndex actives = getActiveIndex(active, 0);
        boolean change = isChange(actives, index);
        return change;
    }

    @Override
    public boolean judgeLoopDep(List<ActiveIndexConfig> configs, String currentActiveId, StringBuffer depRelations) {
        ActiveIndex index = activeIndexMapper.selectByPrimaryKey(currentActiveId);
        List<String> deps = new ArrayList<String>();
        if (index == null) {
            return true;
        } else {
            if (configs != null && !configs.isEmpty()) {
                for (ActiveIndexConfig config : configs) {
                    List<ActiveIndexConfigCondition> conditions = config.getConditions();
                    if (conditions != null && !conditions.isEmpty()) {
                        for (ActiveIndexConfigCondition condition : conditions) {
                            String refActiveId = condition.getRefActiveId();
                            if (StringUtils.isNotEmpty(refActiveId)) {
                                deps.add(refActiveId);
                            }
                        }
                    }
                }
                if (dep(deps, depRelations, currentActiveId)) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean editActiveName(Map<String, Object> params) {
        int i = activeIndexMapper.editActiveName(params);
        conditionMapper.updateRefNameByRefId(params);
        if (i > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<ActiveIndexConfigCondition> findByRefActiveId(String refActiveId) {
        List<ActiveIndexConfigCondition> condition = conditionMapper.findByRefActiveId(refActiveId);
        return condition;
    }
    @Override
    public List<ActiveIndex> dependenceCurActiveByIsTmp(String activeId) {
        List<ActiveIndex> activeIndexList = activeIndexMapper.findReferenceActiveIndex(activeId,0);
        return activeIndexList;
    }

    @Override
    public String getindexType(String id) {
        List<ActiveIndexConfig>  activeIndexConfigs =  activeIndexConfigMapper.findAllByActiveIndexId(id);
        if(activeIndexConfigs.size() ==0) return null;
        return activeIndexConfigs.get(0).getIndexType();
    }

    @Override
    public List<String> getActiveName(String activeId) {
        List<String> result = new ArrayList<>();
        List<ActiveIndex> activeIndices = activeIndexMapper.findReferenceActiveIndex(activeId,0);
        for (ActiveIndex activeIndex : activeIndices){
            result.add(activeIndex.getName());
        }
        return result;
    }

    @Override
    public AjaxObject getAllResearchVariable(String createId, String projectId, Integer cortType, String uid) {
        Integer isVariant = 1;
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("projectId", projectId);
        mapParam.put("isVariant", isVariant);
        Integer count = activeIndexMapper.getAllResearchVariableCount(mapParam);
        List<ActiveIndex> activeIndex = activeIndexMapper.getAllResearchVariable(mapParam);
        List<String> contrastiveAnalysisActives = contrastiveAnalysisActiveMapper.getActiveIndexes(uid,projectId,cortType);
        AjaxObject ajaxObject = disPonseResearchVariable(activeIndex);
        ajaxObject.setCount(count);
        ajaxObject.setPlainOptions(contrastiveAnalysisActives);
        return ajaxObject;
    }

    public AjaxObject disPonseResearchVariable(List<ActiveIndex> activeIndex) {
        JSONArray array = new JSONArray();
        if (activeIndex != null && !activeIndex.isEmpty()) {
            for (ActiveIndex index : activeIndex) {
                JSONObject object = new JSONObject();
                String id = index.getId();
                object.put("activeId", id);
                object.put("calculate",redisMapDataService.exists(UqlConfig.CORT_INDEX_REDIS_KEY.concat(id)));
                object.put("activeName", index.getName());
                object.put("type", CodeToDesc.activeTypeToDesc(index.getActiveType()));
                JSONArray dataMap = new JSONArray();
                List<ActiveIndexConfig> configs = activeIndexConfigMapper.findAllByActiveIndexId(id);
                if (configs != null && !configs.isEmpty()) {
                    for (ActiveIndexConfig config : configs) {
                        object.put("function", config.getFunction());
                        object.put("functionParam", config.getFunctionParam());
                        object.put("dataType", config.getIndexType());
                        object.put("dateFormat", config.getDateFormat());
                        object.put("indexTypeDesc", config.getIndexTypeDesc());
                        if (StringUtils.equals(config.getIndexType(), CommonContent.ACTIVE_INDEX_VALUE_TYPE_4)) {
                            JSONObject data = new JSONObject();
                            data.put("id", config.getId());
                            data.put("value", config.getIndexResultValue());
                            dataMap.add(data);
                        }
                    }
                }
                object.put("dataMap", dataMap);
                array.add(object);
            }
        }
        AjaxObject ajaxObject = new AjaxObject();
        ajaxObject.setData(array);
        return ajaxObject;
    }

    @Override
    public List<ActiveIndex> getActiveIndexByProjectId(String uid, String projectId) {
        return activeIndexMapper.getActiveIndexByProjectId(uid, projectId);
    }

    @Autowired
    ContrastiveAnalysisActiveService contrastiveAnalysisActiveService;

    @Override
    public AjaxObject getContrastiveActive(String uid, String projectId,Integer cortType) {

        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("projectId", projectId);
        mapParam.put("isVariant", 1);
        List<ActiveIndex> activeIndex = activeIndexMapper.getAllResearchVariable(mapParam);

        return disPonseResearchVariable(activeIndex);
    }

    private boolean dep(List<String> deps, StringBuffer depRelations, String currentActiveId) {
        for (String activeId : deps) {
            ActiveIndex active = activeIndexMapper.selectByPrimaryKey(activeId);
            if (findLoopDep(depRelations, currentActiveId, activeId, active)) {
                return true;
            }
        }
        return false;
    }

    private boolean findLoopDep(StringBuffer depRelations, String currentActiveId, String activeId, ActiveIndex active) {
        if (active != null) {
            depRelations.append(active.getName()).append("->");
            List<ActiveIndexConfig> configes = activeIndexConfigMapper.findAllByActiveIndexId(activeId);
            if (configes != null && !configes.isEmpty()) {
                for (ActiveIndexConfig config : configes) {
                    List<ActiveIndexConfigCondition> conditions = conditionMapper.findByConfigId(config.getId());
                    if (conditions != null && !conditions.isEmpty()) {
                        for (ActiveIndexConfigCondition con : conditions) {
                            String refActiveId = con.getRefActiveId();
                            if (StringUtils.equals(refActiveId, currentActiveId)) {
                                depRelations.append(active.getName()).append(";TRUEFLAG");
                            } else {
                                ActiveIndex index = activeIndexMapper.selectByPrimaryKey(refActiveId);
                                if (index == null) {
                                    continue;
                                }
                                findLoopDep(depRelations, currentActiveId, index.getId(), index);
                            }
                        }
                    }
                }
            }
        } else {
            return false;
        }
        return false;
    }

    private ActiveIndex getActiveIndexByConfirm(String projectId, String confirmActiveId, Integer type) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("projectId", projectId);
        param.put("activeConfirmId", confirmActiveId);
        param.put("isTmp", type);
        ActiveIndex activeIndex = activeIndexMapper.findByConfirmIdAndType(param);

        if (activeIndex != null) {
            List<ActiveIndexConfig> configs = activeIndexConfigMapper.findAllByActiveIndexId(activeIndex.getId());
            if (configs != null && !configs.isEmpty()) {
                for (ActiveIndexConfig config : configs) {
                    String id = config.getId();
                    List<ActiveIndexConfigCondition> conditions = conditionMapper.findByConfigIdAndTypeAndLevel(id, CommonContent.ACTIVE_CONDITION_TYPE_1, 1);

                    config.setConditions(conditions);
                }
            }
            activeIndex.setConfig(configs);
        }
        return activeIndex;
    }

    private ActiveIndex getActiveIndex(JSONObject activeJSONObject, int type) {
        ActiveIndex actives = convertJsonToActive(activeJSONObject, type);
        List<ActiveIndexConfig> activeIndexConfigs = convertJsonToConfig(activeJSONObject, actives.getId(), type);
        actives.setConfig(activeIndexConfigs);
        return actives;
    }

    private int getType(ActiveIndex index, int isTemp) {
        int type;
        if (isTemp == CommonContent.ACTIVE_TYPE_TEMP_SAVEAS) {
            return CommonContent.ACTIVE_TYPE_TEMP;
        }
        boolean indexIsTemp = index != null && index.getIsTmp() == isTemp || (index != null && index.getIsTmp() == 0 && isTemp == 1);
        if (indexIsTemp || isTemp == 0) {
            type = CommonContent.ACTIVE_TYPE_NOTEMP;
        } else {
            type = CommonContent.ACTIVE_TYPE_TEMP;
        }
        return type;
    }

    @Nullable
    private AjaxObject basicValidate(AjaxObject object, ActiveIndex actives, List<ActiveIndexConfig> activeIndexConfigs, JSONObject activeJSONObject, Integer isTmp) {
        if (activeJSONObject == null || activeJSONObject.isEmpty() || isTmp == null) {
            object.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            object.setMessage("参数错误，请调整");
            return object;
        }
        if (actives == null || StringUtils.isEmpty(actives.getName(), actives.getProjectId(), actives.getProjectName()) || actives.getActiveType() == null) {
            String message = "活动名称/项目id/项目名称不能为空或提交的数据结构错误";
            LOG.debug(message);
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, message);
        }
        if (!checkConfigs(activeIndexConfigs, actives.getActiveType())) {
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "活动/指标配置或类型错误！");
        }
        return null;
    }

    private String saveTempActive(JSONObject activeJSONObject, Integer isTmp, ActiveIndex act, String tempId, String sortKey) {
        String activeId;
        ActiveIndex activesTemp = convertJsonToActive(activeJSONObject, isTmp);
        activesTemp.setSortKey(sortKey);
        Date date = new Date();
        activesTemp.setCreateTime(date);
        activesTemp.setUpdateTime(date);
        if (StringUtils.isEmpty(tempId) && act != null && act.getIsTmp() == 1) {
            activesTemp.setId(act.getId());
        } else if (StringUtils.isNotEmpty(tempId)) {
            activesTemp.setId(tempId);
        }
        activeId = activesTemp.getId();
        activesTemp.setIsTmp(isTmp);
        List<ActiveIndexConfig> activeIndexConfigsTemp = convertJsonToConfig(activeJSONObject, activesTemp.getId(), isTmp);
        activeIndexMapper.replaceIntoActive(activesTemp);
        executeSave(activeIndexConfigsTemp);
        return activeId;
    }

    /**
     * 执行数据保存
     *
     * @param activeIndexConfigs
     */
    private void executeSave(List<ActiveIndexConfig> activeIndexConfigs) {
        if (activeIndexConfigs != null && !activeIndexConfigs.isEmpty()) {
            Map<String,ActiveIndexConfig> map = new HashMap<>();
            activeIndexConfigMapper.insertBatch(activeIndexConfigs);
            for (ActiveIndexConfig config : activeIndexConfigs) {
                map.put(config.getId(),config);
                List<ActiveIndexConfigCondition> configCondition = config.getConditions();
                if (configCondition != null && !configCondition.isEmpty()) {
                    conditionMapper.insertBatch(configCondition);
                }
            }
            changeExculedEnum(map,activeIndexConfigs.get(0).getActiveIndexId(),activeIndexConfigs.get(0).getIndexType());
        }
    }

    private void changeExculedEnum(Map<String, ActiveIndexConfig> map,String activeIndexId,String indexType) {
        if("枚举:boolean".equals(indexType)){
            List<ActiveIndexConfigCondition> activeIndexConfigConditions = conditionMapper.findByRefActiveId(activeIndexId);
            List<ActiveIndexConfigCondition> configConditions = new ArrayList<>();
            for (ActiveIndexConfigCondition configCondition : activeIndexConfigConditions){
                String enumActvieIndexId = configCondition.getEnumActiveConfigId();
                if(StringUtils.isEmpty(enumActvieIndexId)) continue;
                String[] enums =  enumActvieIndexId.split(SeparatorContent.getRegexVartivalBar());
                JSONArray array = new JSONArray();
                for (int i = 0; i < enums.length; i++) {
                    ActiveIndexConfig tmp = map.get(enums[i]);
                    if(tmp == null) continue;
                    String resultValue = tmp.getIndexResultValue();
                    if(StringUtils.isNotEmpty(resultValue)) array.add(resultValue);
                }
                if(array.size()>0){
                    configCondition.setValue(array.toJSONString());
                    configConditions.add(configCondition);
                }
            }
            if (configConditions != null && !configConditions.isEmpty()) {
                conditionMapper.insertBatch(configConditions);
            }
        }
    }

    private boolean isChange(ActiveIndex active, ActiveIndex act) {
        if (act == null) {
            return true;
        }
        List<ActiveIndexConfig> configs = active.getConfig();
        List<ActiveIndexConfig> configs1 = act.getConfig();
        if (configs == null || configs.isEmpty() || configs1 == null || configs1.isEmpty()) {
            return false;
        }
        if (configs.size() != configs1.size()) {
            return true;
        }
        for (ActiveIndexConfig con : configs) {
            String id = con.getId();
            for (ActiveIndexConfig config : configs1) {
                String id1 = config.getId();
                if (StringUtils.equals(id, id1)) {
                    if (!con.equals(config)) {
                        return true;
                    } else {
                        List<ActiveIndexConfigCondition> conditions = con.getConditions();
                        conditions = conditions.subList(1, conditions.size());
                        List<ActiveIndexConfigCondition> cond = config.getConditions();
                        ArrayList<ActiveIndexConfigCondition> conditions1 = new ArrayList<>();
                        if (cond != null && !cond.isEmpty()) {
                            for (ActiveIndexConfigCondition c : cond) {
                                List<ActiveIndexConfigCondition> details = c.getDetails();
                                if (details != null && !details.isEmpty()) {
                                    for (ActiveIndexConfigCondition str : details) {
                                        List<ActiveIndexConfigCondition> strongRef = str.getStrongRef();
                                        if (strongRef != null && !strongRef.isEmpty()) {
                                            conditions1.addAll(strongRef);
                                        }
                                    }
                                }
                                conditions1.addAll(details);
                            }
                        }
                        if (conditions == null || conditions.isEmpty() || conditions1 == null || conditions1.isEmpty()) {
                            return false;
                        }
                        if (conditions.size() != conditions1.size()) {
                            return true;
                        }
                        for (ActiveIndexConfigCondition con1 : conditions) {
                            String id2 = con1.getId();
                            for (ActiveIndexConfigCondition con2 : conditions1) {
                                String id3 = con2.getId();
                                if (StringUtils.equals(id2, id3)) {
                                    if (!con1.equals(con2)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * 判断条件和配置信息是否发生变化
     *
     * @param active
     * @param act
     * @return
     */
    private boolean isChange1(List<ActiveIndexConfig> active, List<ActiveIndexConfig> act) {
        if (act == null || active == null) {
            return true;
        }

        Gson gson = new Gson();
        String s = gson.toJson(active);
        System.out.println(s);
        JsonParser parser = new JsonParser();
        String resultAndCondition1 = gson.toJson(act);
        System.out.println(resultAndCondition1);
        JsonArray ac1 = (JsonArray) parser.parse(resultAndCondition1);

        JsonArray parse = (JsonArray) parser.parse(s);
        if (ac1 != null && parse != null && parse.equals(ac1)) {
            return false;
        }
        //未发生变化
        return true;
    }

    /**
     * 获取某个活动/指标的全集信息
     *
     * @param indexId
     * @return
     */
    @Override
    public ActiveIndex findByActiveId(String indexId) {
        ActiveIndex activeIndex = activeIndexMapper.selectByPrimaryKey(indexId);
        if (activeIndex != null) {
            List<ActiveIndexConfig> configs = activeIndexConfigMapper.findAllByActiveIndexId(indexId);
            if (configs != null && !configs.isEmpty()) {
                for (ActiveIndexConfig config : configs) {
                    String id = config.getId();
                    List<ActiveIndexConfigCondition> conditions = conditionMapper.findByConfigIdAndTypeAndLevel(id, CommonContent.ACTIVE_CONDITION_TYPE_1, 1);

                    config.setConditions(conditions);
                }
            }
            activeIndex.setConfig(configs);
        }
        return activeIndex;
    }


    /**
     * 将前端提交的数据转化为活动/指标/入排条件定义，活动基本信息
     *
     * @param active
     * @return
     */
    private ActiveIndex convertJsonToActive(JSONObject active, Integer isTemp) {
        ActiveIndex activeIndex = JSONObject.toJavaObject(active, ActiveIndex.class);
        if (isTemp == 1) {
            activeIndex.setId(StringUtils.getUUID());
        } else {
            String uuid = StringUtils.isEmpty(activeIndex.getId()) ? StringUtils.getUUID() : activeIndex.getId();
            activeIndex.setId(uuid);
        }
        return activeIndex;
    }

    /**
     * 将前端提交的数据转化为活动/指标/入排条件定义，条件结果组信息
     *
     * @param active
     * @return
     */
    private List<ActiveIndexConfig> convertJsonToConfig(JSONObject active, String activeId, Integer isTemp) {
        List<ActiveIndexConfig> configs = Lists.newArrayList();
        JSONArray resultAndCondition = active.getJSONArray("config");
        int size = resultAndCondition == null ? 0 : resultAndCondition.size();
        if (size == 0) {
            return configs;
        }
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = resultAndCondition.getJSONObject(i);
            ActiveIndexConfig config1 = JSONObject.toJavaObject(jsonObject, ActiveIndexConfig.class);
            if (config1 == null) {
                continue;
            }
            config1.setActiveIndexId(activeId);
            String uuid = "";
            if (isTemp == 1) {
                uuid = StringUtils.getUUID();
            } else {
                uuid = StringUtils.isEmpty(config1.getId()) ? StringUtils.getUUID() : config1.getId();
            }
            config1.setId(uuid);
            JSONArray conditions = jsonObject.getJSONArray("conditions");
            List<ActiveIndexConfigCondition> configConditions = new ArrayList<>();
            JSONObject ob = new JSONObject();
            int level = 1;
            convertJsonToCondditionNew1(configConditions, conditions, uuid, null, ob, level, isTemp);
            String id = ob.getString("id");
            while (id != null) {
                level++;
                JSONArray condition = ob.getJSONArray("condition");
                LOG.debug("当前级别{},当前condigtionId={},当前组个数={}", level, id, condition == null ? 0 : condition.size());
                convertJsonToCondditionNew1(configConditions, condition, uuid, id, ob, level, isTemp);
                id = ob.getString("id");
            }
            config1.setConditions(configConditions);
            configs.add(config1);
        }
        return configs;
    }

    /***
     * 根据新的格式，进行数据解析
     * @param condition
     * @param configId
     * @return
     */
    @Override
    public List<ActiveIndexConfigCondition> convertJsonToCondditionNew(JSONArray condition, String configId, Integer level, Integer isTemp) {
        List<ActiveIndexConfigCondition> conditions = Lists.newArrayList();
        int size = condition == null ? 0 : condition.size();
        if (size == 0) {
            return conditions;
        }
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = condition.getJSONObject(i);
            //将jsonObject中的inner保存到 jsonArray，之后在做处理
            JSONObject ob = new JSONObject();
            compantCondition(configId, conditions, jsonObject, null, ob, level, isTemp);
            JSONArray condition1 = ob.getJSONArray("condition");
            if (condition1 != null && !condition1.isEmpty()) {

            }
        }
        return conditions;
    }

    public void convertJsonToCondditionNew1(List<ActiveIndexConfigCondition> conditions, JSONArray condition, String configId, String parentId, JSONObject ob, int level, Integer isTemp) {
        int size = condition == null ? 0 : condition.size();
        if (size == 0) {
            return;
        }
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = condition.getJSONObject(i);
            //将jsonObject中的inner保存到 jsonArray，之后在做处理
            compantCondition(configId, conditions, jsonObject, parentId, ob, level, isTemp);
        }
    }

    @Override
    public AjaxObject comConditonToPackaging(String projectId, String activeid, String taskId, Integer isSearch) {
        AjaxObject ajaxObject = this.findDepRelation(activeid, taskId, isSearch);
        LOG.debug(JSONObject.toJSONString(ajaxObject));
        return ajaxObject;
    }

    /**
     * 组装数据返回web
     *
     * @param projectId
     * @param activeid
     * @return
     */
    @Override
    public AjaxObject comConditonToWebUI(String projectId, String activeid) {
        ActiveIndex index = activeIndexMapper.selectByPrimaryKey(activeid);
        AjaxObject ajaxObject = new AjaxObject();
        if (index == null) {
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, activeid + "对应的活动不存在！");
        }
        List<ActiveIndexConfig> configs = activeIndexConfigMapper.findAllByActiveIndexId(activeid);
        if (configs != null && !configs.isEmpty()) {
            for (ActiveIndexConfig config : configs) {
                String configId = config.getId();
                List<ActiveIndexConfigCondition> conditions = conditionMapper.findByConfigIdAndTypeAndLevel(configId, CommonContent.ACTIVE_CONDITION_TYPE_1, 1);
                config.setConditions(null);
                config.setConditions(conditions);

            }
            index.setConfig(configs);
        }
        ajaxObject.setData(index);
        return ajaxObject;
    }

    @Override
    public List<ActiveIndex> findActiveIdByProject(String projectId, Integer type) {
        Map<String, Object> param = new HashMap<String, Object>(10);
        param.put("projectId", projectId);
        param.put("activeType", type);
        param.put("isTmp", 0);
        List<ActiveIndex> allActiveIs = activeIndexMapper.findByProjectIdAndTypeNoPage(param);
        return allActiveIs;
    }

    @Autowired
    private ActiveIndexConfigConditionMapper activeIndexConfigConditionMapper;

    @Override
    public AjaxObject findByProjectIdAndTypeNoPage(String activeId, String projectId, Integer type, String name, String isTwiceIndex, String dePactiveId) {
        Map<String, Object> param = new HashMap<String, Object>(10);
        param.put("projectId", projectId);
        param.put("activeType", type);
        param.put("isTmp", 0);
        param.put("activeName", name);
        param.put("dePactiveId",dePactiveId);
        List<String> depActiveIds = new ArrayList<String>();
        if (StringUtils.isNotEmpty(activeId)) {
            judegDep(activeId, depActiveIds);
            depActiveIds.add(activeId);
            param.put("depActiveIds", depActiveIds);
        }
        List<ActiveIndex> activeIndex = activeIndexMapper.findByProjectIdAndTypeNoPage(param);
        JSONArray array = new JSONArray();

        if (activeIndex != null && !activeIndex.isEmpty()) {
//            indexconfig:
            for (ActiveIndex index : activeIndex) {
                JSONObject object = new JSONObject();
                if (StringUtils.isNotEmpty(name)  ) {
                    index.setName(index.getName().replaceAll(name, "<span style='color:red'>" + name + "</span>"));
                }
                String id = index.getId();
                object.put("activeId", id);
                object.put("activeName", index.getName());
                object.put("type", CodeToDesc.activeTypeToDesc(index.getActiveType()));
                JSONArray dataMap = new JSONArray();
                List<ActiveIndexConfig> configs = activeIndexConfigMapper.findAllByActiveIndexId(id);
                if (configs != null && !configs.isEmpty()) {
                    for (ActiveIndexConfig config : configs) {
//                        List<ActiveIndexConfigCondition> activeIndexConfigConditions = activeIndexConfigConditionMapper.findByConfigId(config.getId());
//                        for (ActiveIndexConfigCondition activeIndexConfigCondition : activeIndexConfigConditions) {
//                            if (StringUtils.isNotEmpty(activeIndexConfigCondition.getRefActiveId())&& "1".equals(isTwiceIndex) )
//                                continue indexconfig;
//                        }
//                        object.put("function", config.getFunction());
//                        object.put("functionParam", config.getFunctionParam());
                        object.put("dataType", config.getIndexType());
                        object.put("dateFormat", config.getDateFormat());
//                        object.put("indexTypeDesc", config.getIndexTypeDesc());
                        if (StringUtils.equals(config.getIndexType(), CommonContent.ACTIVE_INDEX_VALUE_TYPE_4)) {
                            JSONObject data = new JSONObject();
                            data.put("id", config.getId());
                            data.put("value", config.getIndexResultValue());
                            dataMap.add(data);
                        }
                    }
                }
                object.put("dataMap", dataMap);
                array.add(object);
            }
        }
        AjaxObject ajaxObject = new AjaxObject();
        ajaxObject.setData(array);
        return ajaxObject;
    }

    /**
     * 获取所有依赖于当前id的所有事件和指标
     *
     * @param activeId
     * @return
     */
    private void judegDep(String activeId, List<String> depActiveId) {
        List<ActiveIndexConfigCondition> conditions = conditionMapper.findByRefActiveId(activeId);
        int status = 100;
        if (conditions == null || conditions.isEmpty()) {
            status = 200;
        } else {
            for (ActiveIndexConfigCondition condition : conditions) {
                String activeIndexConfigId = condition.getActiveIndexConfigId();
                List<String> configIds = new ArrayList<String>();
                configIds.add(activeIndexConfigId);
                List<String> acitveIdds = activeIndexConfigMapper.findActiveIdByConfigId(configIds);
                if (acitveIdds == null || acitveIdds.isEmpty()) {
                    continue;
                } else {
                    depActiveId.addAll(acitveIdds);
                    for (String id : acitveIdds) {
                        judegDep(id, depActiveId);
                    }
                }
            }
        }
    }

    /**
     * 根据依赖关系递归查询所有依赖活动
     *
     * @param activeId
     * @return
     */
    @Override
    public AjaxObject findDepRelation(String activeId, String taskId, Integer isSearch) {
        List<List<ActiveIndexConfig>> relations = Lists.newArrayList();
        JSONArray array = new JSONArray();
        getAllDepRelation(activeId, array, taskId, isSearch);
        AjaxObject object = new AjaxObject();
        object.setData(array);
        return object;
    }

    /**
     * 递归完成依赖关系的生成
     *
     * @param activeid
     * @param array
     * @param taskId
     * @param isSearch
     */
    private void getAllDepRelation(String activeid, JSONArray array, String taskId, Integer isSearch) {
        ActiveIndex index = this.findByActiveId(activeid);
        if (index == null) {
            return;
        }
        List<ActiveIndexConfig> config = index.getConfig();
        JSONObject object = (JSONObject) JSON.toJSON(index);
        index.setMark(null);
        String indexType = "";
        if (config != null && !config.isEmpty()) {
            for (ActiveIndexConfig con : config) {
                if (StringUtils.isNotEmpty(con.getIndexType())) {
                    indexType = con.getIndexType();
                }
            }
        }
        ActiveIndexConfig config2 = config != null && !config.isEmpty() ? config.get(0) : new ActiveIndexConfig();
        if (index.getActiveType().intValue() == CommonContent.ACTIVE_TYPE_EVENT.intValue()) {
            object.put("function", config2.getOperator());
            object.put("functionParam", config2.getOperatorNum());
        } else if (index.getActiveType().intValue() == CommonContent.ACTIVE_TYPE_INDEX.intValue()) {
            if (StringUtils.equals(indexType, CommonContent.ACTIVE_INDEX_VALUE_TYPE_4)) {
                object.put("function", "static");
            } else {
                object.put("function", config2.getFunction());
                object.put("functionParam", config2.getFunctionParam());
            }
        }
        Integer activeType = index.getActiveType();
        if (activeType != null && activeType.intValue() == CommonContent.ACTIVE_TYPE_EVENT.intValue()) {
            object.put("activeResult", config2.getActiveResult());
        } else if (activeType != null && activeType.intValue() == CommonContent.ACTIVE_TYPE_INDEX.intValue()) {
            if (StringUtils.equals(config2.getIndexTypeDesc(), CommonContent.ACTIVE_INDEX_TYPE_DESC)) {
                object.put("activeResult", CommonContent.ACTIVE_INDEX_STATIC_RESULT);
            } else {
                object.put("activeResult", config2.getIndexColumn());
            }
        }
        object.put("isTmp", isSearch == 1 ? true : false);
        object.put("unique_id", index.getId());
        String projectId = index.getProjectId();
        object.put("activeType", activeType);
        object.put("project_id", projectId);
        object.put("resultsql", CommonContent.PACKAGINGSERVICE_UPDATE_SQL.replace("INDEXPRECESSID", index.getId()).replace("PRECESSID", taskId));
        //需要更新所有任务，将其状态改为0
        ActiveIndexTask tasks = taskMapper.selectByPrimaryKey(taskId);
        if (isSearch == 0) {
            if (tasks != null) {
                tasks.setSubmitTime(new Date());
                tasks.setStatus(0);
                tasks.setMessage("关联数据发生变化，重新计算");
                taskMapper.updateByPrimaryKeySelective(tasks);
            }
        }
        if (config != null && !config.isEmpty()) {
            for (ActiveIndexConfig con : config) {
                List<ActiveIndexConfigCondition> conditions = con.getConditions();
                if (conditions != null && !conditions.isEmpty()) {
                    for (ActiveIndexConfigCondition condition : conditions) {
                        Integer conditionType = condition.getConditionType();
                        condition.setInner(null);
                        condition.setDetails(null);
                        if (conditionType != null) {
                            /**避免出现循环依赖问题*/
                            String s = JSON.toJSONStringWithDateFormat(condition, "", SerializerFeature.DisableCircularReferenceDetect);
                            JSONObject parse = JSONArray.parseObject(s);

                            if (conditionType == 3) {
                                JSONArray match = object.getJSONArray("match");
                                if (match == null || match.size() == 0) {
                                    JSONArray array1 = new JSONArray();
                                    array1.add(parse);
                                    object.put("match", array1);
                                } else {
                                    match.add(parse);
                                    object.put("match", match);
                                }
                            } else if (conditionType == 4) {
                                JSONArray filter = object.getJSONArray("filter");
                                if (filter == null || filter.size() == 0) {
                                    JSONArray array1 = new JSONArray();
                                    array1.add(parse);
                                    object.put("filter", array1);
                                } else {
                                    filter.add(parse);
                                    object.put("filter", filter);
                                }
                            }
                            object.remove("config");
                        }
                    }
                }
            }
        }
        //入排条件
        if (index.getActiveType().intValue() == CommonContent.ACTIVE_TYPE_INOUTN.intValue()) {
            Map<String, Object> param = new HashMap<String, Object>(3);
            //活动或指标
            param.put("activeType", 5);
            param.put("projectId", projectId);
            List<ActiveIndex> indexs = activeIndexMapper.findByProjectIdAndTypeNoPage(param);
            JSONArray idList = new JSONArray();
            if (indexs != null && !indexs.isEmpty()) {
                for (ActiveIndex inde : indexs) {
                    idList.add(inde.getId());
                }
            }
            object.put("id_list", idList);
        } else {//其他
            JSONArray jsonArray = (JSONArray) JSONArray.toJSON(config);
            object.put("attr", jsonArray);
            object.remove("config");
        }
        array.add(object);
        //判断configId是否被其他活动或指标依赖
        List<String> configIds = conditionMapper.findConfigIdByRefActiveId(index.getId());
        //查询依赖当前活动的的活动
        if (configIds == null || configIds.isEmpty()) {
            return;
        }
        List<String> activeIds = activeIndexConfigMapper.findActiveIdByConfigId(configIds);
        if (activeIds == null || activeIds.isEmpty()) {
            return;
        }
//        serverService.clearData(index, isSearch, projectId, activeid);
        /*只有是保存时，才需要递归获取依赖关系的全部查询条件*/
        if (isSearch == 0) {
            for (String ids : activeIds) {
                String uuid = StringUtils.getUUID();
                taskService.initTask(uuid, index.getId(), index.getProjectId(), "提交任务！");
                getAllDepRelation(ids, array, uuid, isSearch);
            }
        }
    }

    private void getDetail(ActiveIndexConfigCondition con, JSONObject condition) {
        String id = con.getId();
        condition.put("id", id);
        condition.put("operatorSign", con.getOperatorSign());
        condition.put("parentId", con.getParentId());
        List<ActiveIndexConfigCondition> details = conditionMapper.findByParentIdAndType(id, CommonContent.ACTIVE_CONDITION_TYPE_2);
        condition.put("detail", details);
    }

    public void getInnerConditionFromDb(String id, Integer type, Integer level, JSONArray inners) {
        List<ActiveIndexConfigCondition> inner = conditionMapper.findByConfigIdAndTypeAndLevel(id, level, type);
        if (inner == null || inner.isEmpty()) {
            return;
        }
        for (ActiveIndexConfigCondition con : inner) {
            JSONObject condition = new JSONObject();
            getDetail(con, condition);
            level++;
            List<ActiveIndexConfigCondition> inn = conditionMapper.findByConfigIdAndTypeAndLevel(id, level, type);
            while (inn != null && !inn.isEmpty()) {

            }
        }
    }

    private void comConditionOutSideFromDb(List<ActiveIndexConfigCondition> comCon, JSONArray array, JSONObject config) {
        if (comCon != null && !comCon.isEmpty()) {
            for (ActiveIndexConfigCondition condition : comCon) {
                JSONObject object = new JSONObject();
                String conditionId = condition.getId();
                String operatorSign = condition.getOperatorSign();
                String parentId = condition.getParentId();
                object.put("operator", operatorSign);
                object.put("id", conditionId);
                object.put("parentId", parentId);
                //拼装detail完成
                comConditonDetailFromdb(conditionId, object);
                //递归处理inner
                List<ActiveIndexConfigCondition> conditons = conditionMapper.findByParentIdAndType(conditionId, CommonContent.ACTIVE_CONDITION_TYPE_1);
                JSONArray inner = new JSONArray();
                array.add(object);
                comCoditonInnerFromDb(conditons, inner);
                object.put("inner", inner);
                config.put("conditions", array);
            }
        }
    }

    private void comCoditonInnerFromDb(List<ActiveIndexConfigCondition> conditons, JSONArray inner) {

        if (conditons != null && !conditons.isEmpty()) {
            for (ActiveIndexConfigCondition condition : conditons) {

                JSONObject object = new JSONObject();
                String conId = condition.getId();
                String operatorSign = condition.getOperatorSign();
                String parentId = condition.getParentId();
                object.put("operator", operatorSign);
                object.put("id", conId);
                object.put("parentId", parentId);
                comConditonDetailFromdb(conId, object);
                inner.add(object);
                object.put("inner", inner);
                JSONArray array = new JSONArray();
                comCoditonInnerFromDb(conditionMapper.findByParentIdAndType(conId, CommonContent.ACTIVE_CONDITION_TYPE_1), array);
            }
        } else {
            return;
        }
    }

    public void comConditonDetailFromdb(String conditionId, JSONObject jsonObject) {
        JSONArray detail = new JSONArray();
        //查找条件信息
        List<ActiveIndexConfigCondition> conditons = conditionMapper.findByParentIdAndType(conditionId, CommonContent.ACTIVE_CONDITION_TYPE_2);

        if (conditons != null && !conditons.isEmpty()) {
            for (ActiveIndexConfigCondition condition : conditons) {
                JSONObject object = (JSONObject) JSONObject.toJSON(condition);
                detail.add(object);
            }
        }

        jsonObject.put("detail", detail);
    }

    private void compantCondition(String configId, List<ActiveIndexConfigCondition> conditions, JSONObject jsonObject, String parentId, JSONObject ob, int level, Integer isTemp) {
        ActiveIndexConfigCondition condition = new ActiveIndexConfigCondition();
        String operator = jsonObject.getString("operator");
        Integer conditionType = jsonObject.getInteger("conditionType");
        condition.setConditionType(conditionType);
        condition.setOperatorSign(operator);
        condition.setActiveIndexConfigId(configId);
        String id1 = "";
        if (isTemp == 1) {
            id1 = StringUtils.getUUID();
        } else {
            id1 = StringUtils.isEmpty(jsonObject.getString("id")) ? StringUtils.getUUID() : jsonObject.getString("id");
        }
        condition.setId(id1);
        condition.setType(1);
        condition.setNeedPath(".");
        condition.setOperatorSign(jsonObject.getString("operatorSign"));
        condition.setLevel(level);
        condition.setUuid(jsonObject.getString("uuid"));
        condition.setParentId(parentId);
        conditions.add(condition);
        condition.setAfter(jsonObject.getString("after"));
        condition.setBefore(jsonObject.getString("before"));
        condition.setAcceptanceState(jsonObject.getInteger("acceptanceState"));
        condition.setNodeType(jsonObject.getString("nodeType"));
        condition.setTitleInfo(jsonObject.getString("titleInfo"));
        condition.setChildrenKey(jsonObject.getString("childrenKey"));
        condition.setInnerLever(jsonObject.getInteger("innerLever"));
        condition.setOrde(jsonObject.getInteger("orde"));
        JSONArray detail = jsonObject.getJSONArray("details");
        //递归details
        details(conditions, detail, configId, isTemp, id1, 2);
        JSONArray inner = jsonObject.getJSONArray("inner");
        if (inner != null && !inner.isEmpty()) {
            ob.put("id", id1);
            ob.put("condition", inner);
        } else {
            ob.put("id", null);
        }
        int size = inner.size();
        level++;
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject1 = inner.getJSONObject(i);
            if (jsonObject1 == null) {
                return;
            }
            compantCondition(configId, conditions, jsonObject1, id1, ob, level, isTemp);
        }
        if (inner == null || inner.isEmpty()) {
            return;
        }
    }

    public void details(List<ActiveIndexConfigCondition> conditions, JSONArray detail, String configId, Integer isTemp, String parentId, Integer type) {
        int i1 = (detail == null) ? 0 : detail.size();
        for (int j = 0; j < i1; j++) {
            JSONObject jsonObject1 = detail.getJSONObject(j);
            ActiveIndexConfigCondition condition1 = JSONObject.toJavaObject(jsonObject1, ActiveIndexConfigCondition.class);
            String id = "";
            if (isTemp == 1) {
                id = StringUtils.getUUID();
            } else {
                id = StringUtils.isEmpty(condition1.getId()) ? StringUtils.getUUID() : condition1.getId();
            }
            condition1.setId(id);
            condition1.setNeedPath(".");
            condition1.setType(type);
            condition1.setActiveIndexConfigId(configId);
            condition1.setAfter(jsonObject1.getString("after"));
            condition1.setBefore(jsonObject1.getString("before"));
            condition1.setAcceptanceState(jsonObject1.getInteger("acceptanceState"));
            condition1.setNodeType(jsonObject1.getString("nodeType"));
            condition1.setTitleInfo(jsonObject1.getString("titleInfo"));
            condition1.setInnerLever(jsonObject1.getInteger("innerLever"));
            condition1.setOrde(jsonObject1.getInteger("orde"));
            condition1.setChildrenKey(jsonObject1.getString("childrenKey"));
            condition1.setParentId(parentId);
            conditions.add(condition1);
            JSONArray detail1 = jsonObject1.getJSONArray("strongRef");
            details(conditions, detail1, configId, isTemp, id, 3);
        }
    }

    private void webUIInnerToObject(JSONArray array, List<ActiveIndexConfigCondition> conditions, String parentId) {
        int size = array.size();
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = array.getJSONObject(i);

        }
    }

    /**
     * 第一版条件格式解析，目前不在使用
     *
     * @param condition
     * @param configId
     * @return
     */
    @Deprecated
    public List<ActiveIndexConfigCondition> convertJsonToCondition(JSONArray condition, String configId) {
        List<ActiveIndexConfigCondition> conditions = Lists.newArrayList();
        int size = condition == null ? 0 : condition.size();
        if (size == 0) {
            return conditions;
        }
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = condition.getJSONObject(i);
            ActiveIndexConfigCondition activeIndexConfigCondition = JSONObject.toJavaObject(jsonObject, ActiveIndexConfigCondition.class);
            activeIndexConfigCondition.setActiveIndexConfigId(configId);
            String uuid = StringUtils.isEmpty(activeIndexConfigCondition.getId()) ? StringUtils.getUUID() : activeIndexConfigCondition.getId();
            activeIndexConfigCondition.setId(uuid);
            JSONArray teamOrRelation = jsonObject.getJSONArray("teamOrRelation");
            int teamSize = teamOrRelation == null ? 0 : teamOrRelation.size();
            List<ActiveIndexConfigCondition> configConditions = new ArrayList<ActiveIndexConfigCondition>();
            if (teamSize > 0) {
                for (int j = 0; j < teamSize; j++) {
                    JSONObject jsonObject1 = teamOrRelation.getJSONObject(i);
                    ActiveIndexConfigCondition configCondition = JSONObject.toJavaObject(jsonObject, ActiveIndexConfigCondition.class);
                    String uuids = StringUtils.isEmpty(configCondition.getId()) ? StringUtils.getUUID() : configCondition.getId();
                    configCondition.setId(uuids);
                    configCondition.setActiveIndexConfigId(configId);
                    configCondition.setParentId(uuid);
                    conditions.add(configCondition);
                    configConditions.add(configCondition);
                }
            }
            conditions.add(activeIndexConfigCondition);
        }
        return conditions;
    }

    private boolean checkConfigs(List<ActiveIndexConfig> configs, Integer activeType) {
        boolean flag = true;
        if (configs == null || configs.isEmpty()) {
            return false;
        }

        for (ActiveIndexConfig config : configs) {
            if (activeType.intValue() == CommonContent.ACTIVE_TYPE_EVENT.intValue()) {
                if (StringUtils.isEmpty(config.getActiveResult()) || StringUtils.isEmpty(config.getActiveResultDesc())
                        || StringUtils.isEmpty(config.getOperator())) {
                    flag = false;
                    break;
                }
            }/*else if(activeType.intValue() == CommonContent.ACTIVE_TYPE_INDEX.intValue()){
                if(StringUtils.isEmpty(config.getFunction()) ||
                        StringUtils.isEmpty(config.getIndexColumn()) || StringUtils.isEmpty(config.getIndexColumnDesc())){
                    flag = false;
                    break;
                }
            }*/
        }
        return flag;
    }

    @Override
    public String findById(Long id) {
        return null;
    }

    @Override
    public String findAllWithPage(ActiveIndex activeIndex, int pageNum, int pageSize) {
        return null;
    }

    /**
     * 查找依赖
     *
     * @param activeId
     * @param byRefActiveId
     * @return
     */
    private List<Map<String, Object>> dependencies(String activeId, List<ActiveIndexConfigCondition> byRefActiveId) {
        List<Map<String, Object>> dependencieded = new ArrayList<Map<String, Object>>();
        List<String> all = new ArrayList<String>();
        if (byRefActiveId != null && !byRefActiveId.isEmpty()) {
            for (ActiveIndexConfigCondition condition : byRefActiveId) {
                String refActiveId = condition.getRefActiveId();
                if (StringUtils.isNotEmpty(refActiveId) && !StringUtils.equals(activeId, refActiveId)) {
                    if (!all.contains(refActiveId)) {
                        Map<String, Object> map = new HashMap<String, Object>(50);
                        ActiveIndex index = activeIndexMapper.selectByPrimaryKey(refActiveId);
                        relationDep(dependencieded, all, activeId, map, index);
                    }
                }

            }
        }
        return dependencieded;
    }

    private void relationDep(List<Map<String, Object>> dependencieded, List<String> all, String refActiveId, Map<String, Object> map, ActiveIndex index) {
        if (index != null && index.getIsTmp() == CommonContent.ACTIVE_TYPE_NOTEMP) {
            if (index.getActiveType().intValue() == CommonContent.ACTIVE_TYPE_INOUTN.intValue()) {
                List<ActiveIndexConfig> configs = activeIndexConfigMapper.findAllByActiveIndexId(index.getId());
                if (configs != null && !configs.isEmpty()) {
                    for (ActiveIndexConfig config : configs) {
                        if (!all.contains(config.getId())) {
                            List<ActiveIndexConfigCondition> conditions = conditionMapper.findByConfigIdAndType(config.getId(), 2);
                            for (ActiveIndexConfigCondition condition : conditions) {
                                boolean equals = StringUtils.equals(condition.getRefActiveId(), refActiveId);
                                if (!all.contains(condition.getId()) && equals) {
                                    String activeResultDesc = StringUtils.trim(config.getActiveResultDesc());
                                    Map<String, Object> dep = new HashMap<String, Object>();
                                    dep.put("refActiveId", index.getId());
                                    dep.put("refActiveName", activeResultDesc);
                                    dep.put("type", index.getActiveType());
                                    if (StringUtils.equals(activeResultDesc, CommonContent.ACTIVE_TYPE_IN_DESE) && !all.contains(activeResultDesc)) {
                                        dep.put("conditionType", CommonContent.ACTIVE_TYPE_IN);
                                        all.add(activeResultDesc);
                                        dependencieded.add(dep);
                                    }
                                    if (StringUtils.equals(activeResultDesc, CommonContent.ACTIVE_TYPE_OUT_DESC) && !all.contains(activeResultDesc)) {
                                        dep.put("conditionType", CommonContent.ACTIVE_TYPE_OUT);
                                        all.add(activeResultDesc);
                                        dependencieded.add(dep);
                                    }
                                    all.add(condition.getId());
                                }
                            }
                            all.add(config.getId());
                        }
                    }
                }
            } else {
                if (!all.contains(index.getId())) {
                    map.put("refActiveId", index.getId());
                    map.put("refActiveName", index.getName());
                    map.put("type", index.getActiveType());
                    dependencieded.add(map);
                    all.add(index.getId());
                }
            }
            map.put("type", index.getActiveType());
            all.add(refActiveId);
        }
    }

    /**
     * 获取当前项目下已定义的所有活动/指标/入排
     *
     * @param projectId
     * @param type
     * @param name
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public AjaxObject findByProjectId(String projectId, int type, String name, int pageNum, int pageSize) {
        AjaxObject object = new AjaxObject();
        Map<String, Object> param = new HashMap<String, Object>(10);
        param.put("projectId", projectId);
        param.put("activeType", type);
        if (StringUtils.isNotEmpty(name)) {
            param.put("activeName", StringUtils.trim(name));
        }
        param.put("isTmp", 0);
        int total = activeIndexMapper.countByParam(param);
        WebAPIResult<Object> webAPIResult = new WebAPIResult<>(pageNum, pageSize, total);
        List<ActiveIndexVo> data = new ArrayList<ActiveIndexVo>();
        param.put("pageNum", webAPIResult.getStartRow());
        param.put("pageSize", pageSize);
        List<ActiveIndex> indes = activeIndexMapper.findByProjectId(param);
        if (indes != null && !indes.isEmpty()) {
            param.put("ids", indes);
            List<ActiveIndexRelation> relations = conditionMapper.selectRelation(param);
            Map<String, ActiveIndexVo> depandes = new ConcurrentHashMap<>();
            ConcurrentLinkedQueue<String> listSort = new ConcurrentLinkedQueue<>();
            if (relations != null && !relations.isEmpty()) {
                List<String> res = new ArrayList<>();
                Map<String, List<String>> isDeals = new HashMap<>();
                for (ActiveIndexRelation relation : relations) {
                    //依赖关系
                    String activeIndexId = relation.getActiveIndexId();
                    ActiveIndexVo maps;
                    if (depandes.keySet().contains(activeIndexId)) {
                        maps = depandes.get(activeIndexId);
                    } else {
                        maps = new ActiveIndexVo();
                        maps.setId(activeIndexId);
                        maps.setIndexType(relation.getIndexTypeDesc());
                        maps.setActiveIndexName(relation.getSourceName());
                        maps.setActiveType(relation.getActiveTypeName());
                        maps.setIndexType(relation.getIndexTypeDesc());
                        maps.setCreateTime(relation.getCreateTime());
                    }
                    //依赖当前事件的指标或者事件
                    if (StringUtils.isNotEmpty(relation.getRefActiveId()) && (isDeals.get(activeIndexId) == null || !isDeals.get(activeIndexId).contains(relation.getRefActiveId()))) {
                        Map<String, Object> datas = new HashMap<>();
                        datas.put("type", relation.getActiveType());
                        datas.put("refActiveId", relation.getRefActiveId());
                        datas.put("refActiveName", relation.getActiveName());
                        datas.put("createTime", relation.getCreateTime());
                        maps.addDependencies(datas);
                        List<String> strings = isDeals.get(activeIndexId);
                        if (strings == null || strings.isEmpty()) {
                            strings = new ArrayList<>();
                            strings.add(relation.getRefActiveId());
                        } else {
                            strings.add(relation.getRefActiveId());
                        }
                        isDeals.put(activeIndexId, strings);
                        res.add(relation.getRefActiveId());
                    }
                    depandes.put(activeIndexId, maps);
                    if(!listSort.contains(activeIndexId)) {
                        listSort.add(activeIndexId);
                    }
                }
                Set<String> keySet = depandes.keySet();
                List<ActiveIndexRelation> refRelations = conditionMapper.selectRelationByRefId(param);
                Set<String> isDeal = new HashSet<>();
                for (String key : listSort) {
                    ActiveIndexVo activeIndexVo = depandes.get(key);
                    String id = activeIndexVo.getId();
                    isDeal.clear();
                    for (ActiveIndexRelation relation : refRelations) {
                        String concat = relation.getActiveIndexId().concat(relation.getSourceName());
                        if (StringUtils.equals(id, relation.getRefActiveId()) && !isDeal.contains(concat)) {
                            Map<String, Object> deped = new HashMap<>();
                            deped.put("type", relation.getActiveType());
                            deped.put("refActiveId", relation.getActiveIndexId());
                            deped.put("refActiveName", relation.getSourceName());
                            deped.put("createTime", relation.getCreateTime());
                            activeIndexVo.addDependenced(deped);
                            isDeal.add(concat);
                        }
                    }
                    data.add(activeIndexVo);
                }
            }
        }
        /*List<String> all = new ArrayList<String>();
        if (indes != null && !indes.isEmpty()) {
            for (ActiveIndex index : indes) {
                all.clear();
                ActiveIndexVo activeIndexVo = new ActiveIndexVo();
                String id = index.getId();
                activeIndexVo.setId(id);
                Integer activeType = index.getActiveType();
                activeIndexVo.setActiveType(CodeToDesc.activeTypeToDesc(activeType));
                activeIndexVo.setActiveIndexName(index.getName());
                List<ActiveIndexConfig> indexConfigs = activeIndexConfigMapper.findAllByActiveIndexId(id);
                List<ActiveIndexConfigCondition> byRefActiveId = conditionMapper.findByRefActiveId(id);
                List<Map<String, Object>> dependencieded = new ArrayList<Map<String, Object>>();

                //查找被依赖
                for (ActiveIndexConfigCondition condition : byRefActiveId) {
                    Map<String, Object> map = new HashMap<String, Object>(50);
                    String activeIndexConfigId = condition.getActiveIndexConfigId();
                    ActiveIndexConfig config = activeIndexConfigMapper.selectByPrimaryKey(activeIndexConfigId);
                    String activeIndexId = config.getActiveIndexId();
                    if (!all.contains(activeIndexId)) {
                        ActiveIndex activeIndex = activeIndexMapper.selectByPrimaryKey(activeIndexId);
                        relationDep(dependencieded, all, id, map, activeIndex);
                    }
                }
                activeIndexVo.setDependenced(dependencieded);
                all.clear();
                //查找依赖
                if (indexConfigs != null && !indexConfigs.isEmpty()) {
                    for (ActiveIndexConfig config : indexConfigs) {

                        if (activeType == 1) {
                            activeIndexVo.setIndexType(config.getActiveResultDesc());
                        } else if (activeType == 2) {
                            activeIndexVo.setIndexType(config.getIndexTypeDesc());
                        }
                        String configId = config.getId();
                        List<ActiveIndexConfigCondition> conditions = conditionMapper.findByActiveIndexConfigId(configId);
                        List<Map<String, Object>> dependencies1 = dependencies(configId, conditions);
                        if(dependencies1!=null&&!dependencies1.isEmpty()){
                            activeIndexVo.setDependencies(dependencies1);
                        }
                    }

                }
                if(!all.contains(id)){
                    data.add(activeIndexVo);
                    all.add(id);
                }
            }
        }*/
        object.setData(data);
        object.setWebAPIResult(webAPIResult);
        return object;
    }

    /**
     * 用于前台调用删除操作时使用
     *
     * @param activeId
     * @return
     */
    @Override
    public AjaxObject deleteByActiveId(String activeId) {
        AjaxObject ajaxObject = judgeActiveDependence(activeId);
        if (ajaxObject.getStatus() == AjaxObject.AJAX_STATUS_FAILURE) {
            return ajaxObject;
        }
        List<ActiveIndexConfig> configs = activeIndexConfigMapper.findAllByActiveIndexId(activeId);
        int i = activeIndexMapper.deleteByPrimaryKey(activeId);
        deleteConditionAndConfig(configs);
        return new AjaxObject();
    }

    public AjaxObject deleteByActiveIdTemp(String activeId) {
        List<ActiveIndexConfig> configs = activeIndexConfigMapper.findAllByActiveIndexId(activeId);
        int i = activeIndexMapper.deleteByPrimaryKey(activeId);
        deleteConditionAndConfig(configs);
        return new AjaxObject();
    }

    /**
     * 更新数据时，把当前活动下的配置信息和条件信息删除
     *
     * @param activeId
     * @return
     */
    public AjaxObject deleteActiveForSave(String activeId) {
        List<ActiveIndexConfig> configs = activeIndexConfigMapper.findAllByActiveIndexId(activeId);
        deleteConditionAndConfig(configs);
        return new AjaxObject();
    }

    /**
     * 删除配置信息和条件信息
     *
     * @param configs
     */
    private void deleteConditionAndConfig(List<ActiveIndexConfig> configs) {
        if (configs != null && !configs.isEmpty()) {
            for (ActiveIndexConfig config : configs) {
                String id = config.getId();
//                conditionMapper.deleteByActiveIndexConfigId(id);
                activeIndexConfigMapper.deleteByPrimaryKeyOnActiveIndex(id);
                activeIndexConfigMapper.deleteByPrimaryKey(id);
            }
        }
    }

    /**
     * 查找哪些活动依赖于当前活动
     *
     * @param activeId
     * @return
     */
    @Override
    public AjaxObject judgeActiveDependence(String activeId) {
        List<String> configIds = conditionMapper.findConfigIdByRefActiveId(activeId);
        AjaxObject object = new AjaxObject();
        if (configIds == null || configIds.isEmpty()) {
            return object;
        }
        List<String> refActiveId = activeIndexConfigMapper.findActiveIdByConfigId(configIds);
        if (refActiveId == null || refActiveId.isEmpty()) {
            return object;
        }
        List<ActiveIndex> byIds = activeIndexMapper.findByIds(refActiveId);
        if (byIds == null || byIds.isEmpty()) {
            return object;
        } else {
            StringBuffer message = new StringBuffer(100);
            message.append("以下业务事件的定义依赖本业务，请删除这些依赖关系后，再执行删除操作。｛");
            String name = byIds.stream().map( a -> a.getName()).distinct().collect(joining(","));
            message.append(name);
//            for (ActiveIndex id : byIds) {
//                message.append(id.getName()).append(",");
//            }
            object.setStatus(AjaxObject.AJAX_STATUS_FAILURE);
            object.setMessage(message.append("}").toString());
        }
        object.setData(byIds);
        return object;
    }

    /**
     * 不采用分页，根据projectId和type获取数据
     *
     * @param projectId
     * @param type
     * @return
     */
    @Override
    public List<ActiveIndex> findeByProjectAndType(String projectId, int type) {
        Map<String, Object> param = new HashMap<>(3);
        param.put("projectId", projectId);
        param.put("activeType", type);
        param.put("isTmp", 0);
        List<ActiveIndex> index = activeIndexMapper.findByProjectIdAndTypeNoPage(param);
        return index;
    }

    @Override
    public List<ActiveIndex> findeByActiveName(Map<String, Object> param) {
        List<ActiveIndex> actives = activeIndexMapper.findByParam(param);
        return actives;
    }

    /**
     * 根据configId 获取congfig下所有依赖的活动id
     *
     * @param configIds
     * @return
     */
    @Override
    public List<String> findRefActiveIdByConfigId(List<String> configIds) {
        if (configIds == null || configIds.isEmpty()) {
            return Lists.newArrayList();
        }
        return conditionMapper.findRefActiveIdByConfigId(configIds);
    }

    /**
     * 获得当前活动依赖的所有活动或指标的id
     *
     * @param configs
     * @return
     */
    @Override
    public List<String> getRefActiveIds(List<ActiveIndexConfig> configs) {
        List<String> activeIds = null;
        if (configs != null && !configs.isEmpty()) {
            activeIds = Lists.newArrayList();
            for (ActiveIndexConfig config : configs) {
                activeIds.add(config.getId());
            }
        }

        return this.findRefActiveIdByConfigId(activeIds);
    }

    @Override
    public List<ActiveIndexConfigCondition> dependenceCurActive(String activeId) {
        List<ActiveIndexConfigCondition> conditions = conditionMapper.findByRefActiveId(activeId);
        return conditions;
    }


}
