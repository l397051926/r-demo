/**
 *
 */
package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.exception.CustomerException;
import com.gennlife.exception.CustomerStatusEnum;
import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.dao.*;
import com.gennlife.rws.entity.*;
import com.gennlife.rws.query.UqlQureyResult;
import com.gennlife.rws.service.ActiveIndexService;
import com.gennlife.rws.service.ModuleConvertService;
import com.gennlife.rws.service.PatientGroupService;
import com.gennlife.rws.service.SearchByuqlService;
import com.gennlife.rws.util.*;
import com.gennlife.rws.vo.DataCheckEmpty;
import com.gennlife.rws.vo.DelFlag;
import com.gennlife.rws.vo.GroupColums;
import com.gennlife.rws.web.WebAPIResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.*;


@Service
@Transactional(rollbackFor = RuntimeException.class)
public class PatientGroupServiceImpl implements PatientGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientGroupServiceImpl.class);

    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private GroupDataMapper groupDataMapper;
    @Autowired
    private LogUtil logUtil;
    @Autowired
    private GroupPatientDataMapper groupPatDataMapper;
    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    @Autowired
    private GroupConditionMapper groupConditionMapper;
    @Autowired
    private GroupTypeMapper groupTypeMapper;
    @Autowired
    private PatientsSetMapper patientsSetMapper;
    @Autowired
    private ActiveIndexService activeIndexService;
    @Autowired
    private ModuleConvertService moduleConvertService;
    @Autowired
    private SearchByuqlService searchByuqlService;
    @Autowired
    private HttpUtils httpUtils;
    @Autowired
    private SearchLogMapper searchLogMapper;
    private static final int exportMax = 2000;

    @Override
    public List<Group> getPatientGroupList(JSONObject obj) {
        Map<String, Object> map = new HashMap<>();
        String projectId = obj.getString("projectId");
        map.put("projectId", projectId);
        map.put("groupTypeId", obj.getString("groupTypeId"));
        map.put("groupLevel", "0");
        DataCheckEmpty.dataCheckEmpty(obj.getString("projectId"), obj.getString("groupTypeId"));
        List<Group> listGroup = groupMapper.getGroupList(map);
        for (Group gp : listGroup) {
            gp.setGroupNum(getPatientSnCount(gp.getGroupId(),projectId));
            gp.setExistType(getExistType(gp));
            map.put("groupParentId", gp.getGroupId());
            map.put("groupLevel", "1");
            List<Group> listLev1 = groupMapper.getGroupList(map);
            if (listLev1 != null && !listLev1.isEmpty()) {
                gp.setChildGroup(listLev1);
                for (Group gp1 : listLev1) {
                    gp1.setGroupNum(getPatientSnCount(gp1.getGroupId(),projectId));
                    gp1.setExistType(getExistType(gp1));
                    map.put("groupParentId", gp1.getGroupId());
                    map.put("groupLevel", "2");
                    List<Group> listLev2 = groupMapper.getGroupList(map);
                    if (listLev2 != null && !listLev2.isEmpty()) {
                        gp1.setChildGroup(listLev2);
                        for (Group gp2 : listLev2) {
                            gp2.setGroupNum(getPatientSnCount(gp2.getGroupId(),projectId));
                            gp2.setExistType(getExistType(gp2));
                        }
                    }
                }
            }

        }
        return listGroup;
    }

    /**
     * @param gp
     * @return
     *  0 代表没有 数据
     *  1 代表有数据
     *  2 代表实际有数据 但是存在数据列表为0
     */
    private Integer getExistType(Group gp) {
        Integer num = gp.getGroupNum();
        List<String> list = groupDataMapper.getAllPatientSnList(gp.getGroupId());
        Integer allNum = list.size();
        if(allNum == 0 ){
            return  0;
        }else if(num > 0){
            return  1;
        }else {
            return 2;
        }
    }

    @Override
    public Group getPatientGroup(JSONObject obj) {
        String groupId = obj.getString("groupId");
        DataCheckEmpty.dataCheckEmpty(groupId);
        Group group = groupMapper.selectByGroupId(groupId);
        // 分组和患者对应表
        List<GroupData> list = groupDataMapper.getByGroupId(groupId);
        if (list != null && list.size() > 0) {
            group.setGruopDatas(list);
        }
        return group;

    }

    @Override
    public Group savePatientGroup(JSONObject obj) {
        Group group = JSONObject.toJavaObject(obj, Group.class);
        // 项目ID 分组名称 分组类型(病例=0 对照组=1)
        DataCheckEmpty.dataCheckEmpty(group.getGroupName(), group.getGroupTypeId(), group.getProjectId());
        group.setGroupId(UUIDUtil.getUUID());
        group.setCreateTime(new Date());
        group.setUpdateTime(new Date());
        group.setIsDelete(DelFlag.AVIABLE.toString());// 枚举值
        group.setGroupLevel(0);
        // 层级关系
        if (!StringUtils.isEmpty(group.getGroupParentId())) {
            Group groupLever = groupMapper.selectByGroupId(group.getGroupParentId());
            if (groupLever != null) {
                int k = groupLever.getGroupLevel();
                group.setGroupLevel(++k);
            } else {
                // 理论上不会出现
            }
        }
        groupMapper.insert(group);
        // 保存 用户分组条件
        saveUserGroupCondition(group.getProjectId(), group.getGroupId());
        String content = group.getCreateName() + "新增了组： " + group.getGroupName();
        logUtil.saveLog(group.getProjectId(), content, group.getCreateId(), group.getCreateName());
        return group;
    }

    @Override
    public Group updatePatientGroup(JSONObject obj) {
        Group group = JSONObject.toJavaObject(obj, Group.class);
        Group group1 = groupMapper.selectByGroupId(group.getGroupId());
        String oldGroupName = group1.getGroupName();
        if (group1 != null) {
            group.setId(group1.getId());
            group.setUpdateTime(new Date());
            groupMapper.updateById(group);
        } else {
            // 返回异常信息
            throw new CustomerException(CustomerStatusEnum.UNKONW_ERROR.toString(), "患者分组不存在,无法更新!");
        }
        String content = group.getUpdateName() + "编辑了组： " + oldGroupName;
        logUtil.saveLog(group.getProjectId(), content, group.getCreateId(), group.getCreateName());
        return group;

    }

    @Override
    public String insertGroupDataPatient(JSONObject obj) {
        // 如果某患者集下的患者完全移除,需考虑删除分组和患者集的关系
        String projectId = obj.getString("projectId");
        String groupName = obj.getString("groupName");
        String groupId = obj.getString("groupId");
        String createId = obj.getString("createId");
        String createName = obj.getString("createName");
        String all = obj.getString("all");
        String message = removeGroupDataPatient(projectId,groupName,groupId,createId,createName,all,obj);
        return message;

    }

	@Override
	public AjaxObject getPatientList(JSONObject object) throws IOException {
		// 获取患者列表
		// 1.根据mysq 查出 患者sn列表 从monggo获取
		AjaxObject ajaxObject = null;
		String projectId = object.getString("projectId");
		String operType = object.getString("operType");
		String groupId = object.getString("groupId");
		JSONArray columns = object.getJSONArray("basicColumns");
		Integer activeType = object.getInteger("activeType");
		Integer pageSize = object.getInteger("pageSize");
		Integer pageNum = (object.getInteger("pageNum") - 1)*pageSize;
		String isAll = object.getString("isAll");
		String crfId = object.getString("crfId");
		List<String> willRecoverPatientList =null;
		List<String> willRemovePatientList =null;
		if(object.containsKey("willRemovePatientList")){
			willRemovePatientList = object.getJSONArray("willRemovePatientList").toJavaList(String.class);
		}
		if(object.containsKey("willRecoverPatientList")){
			willRecoverPatientList = object.getJSONArray("willRecoverPatientList").toJavaList(String.class);
		}

		List<GroupData> groupDataList = null;
		if ("true".equals(isAll)){
			groupDataList = groupDataMapper.getByGroupIdLimitByAll(groupId,operType,willRecoverPatientList,willRemovePatientList);
		}else {
			groupDataList = groupDataMapper.getByGroupIdLimitByRemove(groupId, pageNum, pageSize, operType,willRecoverPatientList,willRemovePatientList);
		}

		JSONArray hits = searchByuqlService.getPatientListByPatientSn(groupDataList, columns, activeType,projectId,crfId);
		int size = hits ==null ?0 : hits.size();
		JSONArray result = new JSONArray();
        for (int i = 0; i < size; i++) {
            JSONObject patientInfo = IndexContent.getPatientInfoObj(hits.getJSONObject(i).getJSONObject("_source"),crfId);
            result.add(patientInfo);
        }
        //增加 患者来自哪个分组
        addPatientSetGroupFrom(result,groupId);
        addGroupFromColumns(columns);
		// 增加 患者来的哪个 患者集
		addPatientSetFrom(result, groupId);
		addPatientSetCount(columns);
		List<String> patSns = groupDataMapper.getPatientSnList(groupId);
		String applyOutCondition = String.join(",",patSns);
		ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
		AjaxObject.getReallyDataValue(result,columns);
		ajaxObject.setData(result);
		ajaxObject.setColumns(columns);
		ajaxObject.setApplyOutCondition(applyOutCondition);
		Map<String, Object> mapParam = new HashMap<>();
		mapParam.put("groupId", groupId);
		int total = groupDataMapper.getPatSetCountByGroupIdAndOperType(groupId, operType,willRecoverPatientList,willRemovePatientList);
		WebAPIResult webAPIResult = new WebAPIResult(pageNum, pageSize, total);
		ajaxObject.setWebAPIResult(webAPIResult);
		return ajaxObject;

    }

    private void addPatientSetGroupFrom(JSONArray result, String groupId) {
        int size = result == null ? 0 : result.size();
        //递归找父及id
        Map<String,List<String>> allMap= new HashMap<>();
        List<Group> groupList = groupMapper.getGroupListByParentId(groupId);
        for (Group group : groupList){
            String tmpId = group.getGroupId();
            String groupName = group.getGroupName();
            List<String> patisns = groupDataMapper.getPatientSnList(tmpId);
            allMap.put(groupName,patisns);
        }
        for (int i = 0; i < size; i++) {
            JSONObject patientObj = result.getJSONObject(i);
            String patientSn = patientObj.getString("PATIENT_SN");
            for (Map.Entry<String,List<String>> entry : allMap.entrySet()){
                String name = entry.getKey();
                List<String> patSns = entry.getValue();
                if(patSns.contains(patientSn)){
                    patientObj.put("groupFrom", name);
                }
            }
        }
    }

    private void addGroupFromColumns(JSONArray columns) {
        JSONObject tmpObj = new JSONObject();
        tmpObj.put("id", "groupFrom");
        tmpObj.put("name", "所属子组名称");
        columns.add(1,tmpObj);
    }

    @Override
    public List<GroupType> getGroupTypeList(JSONObject obj) {
        String projectId = obj.getString("projectId");
        DataCheckEmpty.dataCheckEmpty(projectId);
        List<GroupType> list = groupTypeMapper.getGroupTypeList(projectId);
        return list;
    }

    @Override
    public AjaxObject groupAggregation(JSONObject object) {
        String groupId = object.getString("groupId");
        JSONArray data = new JSONArray();

        Map<String, Object> mapParam = new HashMap<>();
        JSONArray aggregationArray = object.getJSONArray("aggregationTeam");
        int size = aggregationArray == null ? 0 : aggregationArray.size();
        for (int i = 0; i < size; i++) {
            mapParam.clear();
            JSONObject dataObj = new JSONObject();
            JSONObject arggreObj = aggregationArray.getJSONObject(i);
            String domainDesc = arggreObj.getString("domain_desc");
            String domain_id = arggreObj.getString("domain_id");
            mapParam.put("groupId", groupId);
            mapParam.put(domain_id, " ");
            List<GroupAggregation> groupAggregations = groupDataMapper.getPatSetAggregation(mapParam);
            for(GroupAggregation groupAggregation : groupAggregations){
                if(StringUtils.isEmpty(groupAggregation.getName())){
                    groupAggregation.setName("无数据");
                }
            }
            JSONArray domainData = JSONArray.parseArray(JSON.toJSONString(groupAggregations));
            int count = groupDataMapper.getPatSetAggregationCount(groupId);
            // 构造数据
            dataObj.put("domain_data", domainData);
            dataObj.put("count", count);
            dataObj.put("domain_desc", domainDesc);
            data.add(dataObj);
        }
        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
        ajaxObject.setData(data);
        return ajaxObject;
    }

    @Override
    public AjaxObject getGroupParentData(JSONObject object) {
        AjaxObject ajaxObject = null;
        JSONArray showColumns = object.getJSONArray("showColumns");
        String projectId = object.getString("projectId");
        String groupId = object.getString("groupId");
        JSONArray patientSetIdTmp = object.getJSONArray("patientSetId");
        Integer activeType = object.getInteger("activeType");
        Integer pageNum = object.getInteger("pageNum");
        Integer pageSize = object.getInteger("pageSize");
        String crfId = object.getString("crfId");
        Group group = groupMapper.selectByGroupId(groupId);
        String groupParentId = group.getGroupParentId();
        JSONObject data = new JSONObject();
        List<String> patientSetIds =null;
        if(patientSetIdTmp == null){
            List<String> patSetIds = groupPatDataMapper.getPatSetByGroupId(groupId);
            patientSetIdTmp  = JSONArray.parseArray(JSON.toJSONString(patSetIds));
        }
        if(patientSetIdTmp !=null){
            patientSetIds = patientSetIdTmp.toJavaList(String.class);
        }
        if (StringUtils.isEmpty(groupParentId) && patientSetIds != null ) { //父及数据获取
            JSONArray columns = GroupColums.getPatientSetColumnJSON(crfId);
            StringBuffer groupNames = new StringBuffer();
            int patientSetSize = patientSetIdTmp.size();
            for (int i = 0; i < patientSetSize; i++) {
                String patientSetId = patientSetIdTmp.getString(i);
                String patientSetName = patientsSetMapper.getpatientSetNameByPatSetId(patientSetId);
                groupNames.append(patientSetName);
                if(patientSetSize>0 && i<patientSetSize-1){
                    groupNames.append(" + ");
                }
            }

            // 构造数据 从 患者集获取数据 **s
            JSONArray actives = new JSONArray();
            ajaxObject = searchByuqlService.getPatientListByAllByPatientSetIds(patientSetIdTmp,projectId,showColumns,actives,pageNum,pageSize,1,crfId);
            JSONArray dataTmp = JSONArray.parseArray(JSON.toJSONString(ajaxObject.getData()));
            AjaxObject.getReallyDataValue(dataTmp,columns);
            data.put("data", dataTmp);
            data.put("grandParents", groupNames);
            ajaxObject.setData(data);
            ajaxObject.setColumns(columns);

        } else if(StringUtils.isNotEmpty(groupParentId)){// 不为空 为子组
            Integer startNum = (pageNum-1)*pageSize;
            Integer endNum = pageSize;
            // 递归找他所有父组
            List<String> groupNames = new LinkedList<>();
            String groupParentName = "";
            getParentGroup(groupParentId,groupNames);

            String groupName = String.join(" - ",groupNames);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
            data.put("grandParents", groupName);
            data.put("groupParentName", groupParentName);
            data.put("groupParentId", groupParentId);
            Integer counts = groupDataMapper.getPatSetAggregationCount(groupParentId);
            // 查找数据
            List<GroupData> groupDataList = groupDataMapper.getByGroupIdLimit(groupParentId, startNum, endNum, null);
            String patientSns = IndexContent.getPatientSn(crfId)+" IN ( " + groupDataList.stream().map(x -> "'"+x.getPatientSn()+"'").collect(joining(","))+")";
            String query = "select "+IndexContent.getPatientDocId(crfId)+" as patSn  from "+ IndexContent.getIndexName(crfId,projectId) + " where "+patientSns+IndexContent.getGroupBy(crfId);
            JSONArray source = new JSONArray();
            source.add("patient_info");
            JSONObject jsonData = JSONObject.parseObject(httpUtils.querySearch(projectId,query,0,pageSize,null,source,crfId));
            JSONArray dataArr = UqlQureyResult.getQueryData(jsonData,crfId);
            WebAPIResult webAPIResult = new WebAPIResult(pageNum, pageSize, counts);
            AjaxObject.getReallyDataValue(dataArr,showColumns);
            data.put("data",dataArr);
            ajaxObject.setColumns(showColumns);
            ajaxObject.setData(data);
            ajaxObject.setWebAPIResult(webAPIResult);
        }else {
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
            Integer startNum = (pageNum-1)*pageSize;
            Integer endNum = pageSize;
            Integer counts = groupDataMapper.getPatSetAggregationCount(group.getGroupId());
            List<GroupData> groupDataList = groupDataMapper.getByGroupIdLimit(group.getGroupId(), startNum, endNum, null);
            String patientSns = IndexContent.getPatientSn(crfId)+" IN ( " + groupDataList.stream().map( x -> "'"+x.getPatientSn()+"'").collect(joining(","))+")";
            String query = "select "+IndexContent.getPatientDocId(crfId)+" as patSn  from "+ IndexContent.getIndexName(crfId,projectId) + " where "+patientSns+IndexContent.getGroupBy(crfId);
            JSONArray source = new JSONArray();
            source.add("patient_info");
            JSONObject jsonData = JSONObject.parseObject(httpUtils.querySearch(projectId,query,0,pageSize,null,source,crfId));
            JSONArray dataArr = UqlQureyResult.getQueryData(jsonData,crfId);

            WebAPIResult webAPIResult = new WebAPIResult(pageNum, pageSize, counts);

            List<GroupPatientData> groupPatientDatas = groupPatDataMapper.selectByGroupId(groupId);
            StringBuffer groupNames = new StringBuffer();
            for (int i = 0; i < groupPatientDatas.size(); i++) {
                String patientSetId = groupPatientDatas.get(i).getPatientSetId();
                String patientName = patientsSetMapper.getpatientSetNameByPatSetId(patientSetId);
                groupNames.append(patientName);
                if(groupPatientDatas.size()>0 && i<groupPatientDatas.size()-1){
                    groupNames.append(" + ");
                }
            }
            AjaxObject.getReallyDataValue(dataArr,showColumns);
            data.put("grandParents", groupNames);
            data.put("groupParentName", group.getGroupName());
            data.put("groupParentId", groupParentId);
            data.put("data",dataArr);
            ajaxObject.setColumns(showColumns);
            ajaxObject.setData(data);
            ajaxObject.setWebAPIResult(webAPIResult);
        }
        return ajaxObject;
    }

    @Override
    public AjaxObject getGroupCountTypeList(JSONObject object, List<GroupType> list) {
        AjaxObject ajaxObject = null;
        JSONArray dataArray = JSONArray.parseArray(JSON.toJSONString(list));
        String projectId = object.getString("projectId");
        Integer cortType = object.getInteger("cortType");
        String uid = object.getString("uid");
        Set<String> set = null;
        if(cortType != null && StringUtils.isNotEmpty(uid) ){
            List<GroupCondition> groupConditionList = groupConditionMapper.getGroupByProjectId(uid, projectId,cortType);
            set = groupConditionList.stream().map(x-> x.getGroupId()).collect(toSet());
        }
        if (StringUtils.isNotEmpty(projectId)) {
            int size = dataArray == null ? 0 : dataArray.size();
            for (int i = 0; i < size; i++) {
                JSONObject tmpGroupType = dataArray.getJSONObject(i);
                String groupTypeId = tmpGroupType.getString("groupTypeId");
                List<Group> groupList = groupMapper.getGroupByTypeId(projectId, groupTypeId);
                List<Group> groupListNew = new LinkedList<>();
                Map<String ,Group> groupConditionMap = new HashMap<>();
                for (Group group : groupList) {
                    int count = groupDataMapper.getPatSetAggregationCount(group.getGroupId());
                    if (count > 0)
                        groupListNew.add(group);
                }
                if(set != null){
                    for (Group group : groupList) {
                        String groupId = group.getGroupId();
                        int count = groupDataMapper.getPatSetAggregationCount(group.getGroupId());
                        String groupParentId = group.getGroupParentId();
                        if(StringUtils.isNotEmpty(groupParentId) && groupConditionMap.containsKey(groupParentId) && count>0 ){
                            groupConditionMap.remove(groupParentId);
                        }
                        if(set.contains(groupId) && count>0 ){
                            groupConditionMap.put(groupId,group);
                        }
                    }
                    tmpGroupType.put("plainOptions",groupConditionMap.size());
                }
                tmpGroupType.put("count", groupListNew.size());
            }
        }
        ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
        ajaxObject.setData(dataArray);
        return ajaxObject;
    }

    @Override
    public AjaxObject getActiveIndexByGroup(String activeId) {
        AjaxObject ajaxObject = new AjaxObject();
        ActiveIndex active = null;
        active = activeIndexService.findByActiveId(activeId);
        active = active == null ? new ActiveIndex() : active;
        JSONObject o = (JSONObject) JSONObject.toJSON(active);
        // 兼容前端json解析
        if (o != null) {
            // 字符串格式的["",""]转成数组格式
            String text = o.toString();
            String toWebUI = StringUtils.replace(text, "\"[", "[").replace("]\"", "]").replace("\\\"", "\"");

            JSONObject webUi = JSONObject.parseObject(toWebUI);
            JSONArray webConfig = o.getJSONArray("config");
            JSONArray newWebConfig = new JSONArray();
            int webSize = webConfig == null ? 0 : webConfig.size();
            // 如果数据为空 则结束
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
            // 枚举修改
            JSONArray conditionNew = moduleConvertService.enumFormatToUi(newWebConfig);
            webUi.put("config", conditionNew);
            JSONObject activeResult = new JSONObject();
            activeResult.put("active", webUi);
            JSONObject data = new JSONObject();
            data.put("data", activeResult);
            data.put("isGroupActive", 1);
            ajaxObject.setData(data);
        }
        return ajaxObject;
    }

    @Override
    public AjaxObject getPatientSearchActive(String groupId) {
        AjaxObject ajaxObject = null;
        JSONObject data = new JSONObject();
        List<GroupPatientData> groupPatientDataList = groupPatDataMapper.selectByGroupId(groupId);
        if (groupPatientDataList == null || groupPatientDataList.size() == 0) {
            data.put("data", new JSONArray());
            data.put("isGroupActive", 0);
            ajaxObject.setData(data);
            return ajaxObject;
        }
        List<String> patientSetData = new ArrayList<>();
        JSONObject searchLogs = new JSONObject();
        List<SortName> sortArray = new ArrayList<>();
        for (GroupPatientData groupPatientData : groupPatientDataList) {
            String patientSetName = patientsSetMapper.getpatientSetNameByPatSetId(groupPatientData.getPatientSetId());
            List<SearchLog> searchLog = searchLogMapper.selectByPrtisntId(groupPatientData.getPatientSetId());
            searchLogs.put(patientSetName, searchLog);
            SortName sortName = new SortName(patientSetName,searchLog.get(0).getCreateTime());
            sortArray.add(sortName);
        }
        List<SortName> sortNames = sortArray.stream()
            .sorted(Comparator.comparing(SortName::getCreateTime).reversed())
            .collect(toList());
        Map<String,JSONArray> map = new LinkedHashMap<>();
        for (SortName sortName : sortNames){
            map.put(sortName.getName(),searchLogs.getJSONArray(sortName.getName()));
        }

        ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);

        data.put("data", map);
        data.put("isGroupActive", 0);
        ajaxObject.setData(data);
        return ajaxObject;
    }

    private String removeGroupDataPatient(String projectId, String groupName, String groupId, String createId, String createName, String all,JSONObject obj) {
        String content = "";
        String type = "";
        List<GroupData> groupDataList = groupDataMapper.getByGroupParentId(groupId);
        Group groupParent = groupMapper.getGroupByGroupParentId(groupId);
        List<String> removePatSn = groupDataList.stream().map( x ->x.getPatientSn()).distinct().collect(toList());
        int maxTol = 0;
        int inFinalTol = 0;
        String groupParentName = "";
        if(groupParent !=null){
            groupParentName = groupParent.getGroupName();
        }
        JSONArray patientsArray = new JSONArray();
        int resNum = 0;
        int remNum = 0;
        if ("all".equals(all) && !StringUtils.isEmpty(groupId)) {
//            int total = groupDataMapper.allUpdate(groupId);
            List<String> groupPatSns =groupDataMapper.getPatientSnListByRemove(groupId).stream().distinct().collect(toList());
            inFinalTol = 0;
            Set<String> distPatients = new HashSet<>();
            maxTol = groupPatSns ==null ? 0 :groupPatSns.size();
            int remove = 0;
            for (int i = 0; i < groupPatSns.size(); i++) {
                // 操作类型 1=移除 0 = 复原
                String operType = "0";
                String patientSn = groupPatSns.get(i);
                if(distPatients.contains(patientSn)){
                    continue;
                }else {
                    distPatients.add(patientSn);
                }
                if("0".equals(operType) && removePatSn.contains(patientSn)){
                    inFinalTol++;
                    continue;
                }
                GroupData groupData = groupDataMapper.findDataByKey(groupId, patientSn);
                if(groupData !=null && operType.equals(groupData.getRemove())) continue;
                groupData.setRemove(operType);
                groupDataMapper.updateById(groupData);
                remove++;
            }
//            inFinalTol =inFinalTol -remove;
            content = createName + "在 " + groupName + "组执行全部复原,复原了" + remove + "名患者";
        } else {
            JSONArray patients = JSONObject.parseArray(obj.getString("patients"));
            Set<String> distPatients = new HashSet<>();
            inFinalTol = 0;
            maxTol = patients ==null ? 0 :patients.size();
            if (patients != null) {
                // 移除数量
                int removeNum = 0;
                for (int i = 0; i < patients.size(); i++) {
                    // 操作类型 1=移除 0 = 复原
                    String operType = patients.getJSONObject(i).getString("operType");
                    type = operType;
                    String patientSn = patients.getJSONObject(i).getString("patientSn");
                    if("1".equals(operType)){
                        patientsArray.add(patients.getJSONObject(i));
                    }
                    if(distPatients.contains(patientSn)){
                        continue;
                    }else {
                        distPatients.add(patientSn);
                    }
                    if("0".equals(operType) && removePatSn.contains(patientSn)){
                        inFinalTol++;
                        continue;
                    }
                    GroupData groupData = groupDataMapper.findDataByKey(groupId, patientSn);
                    if(groupData !=null && operType.equals(groupData.getRemove())) continue;
                    if(groupData==null){continue;}
                    groupData.setRemove(operType);
                    groupDataMapper.updateById(groupData);
                    // 计算移除数量,记录日志
                    if("1".equals(operType)){
                        remNum++;
                    }
                    if("0".equals(operType)){
                        resNum++;
                    }
                    removeNum++;
//                    if (DelFlag.LOSE.toString().equals(operType)) {
//                        removeNum++;
//                    }
                }

            }
        }
        if (remNum > 0  ) {
            content = createName + "在 " + groupName + "组移除了" + remNum + "名患者";
            logUtil.saveLog(projectId, content, createId, createName);
        }
        // 复原数量
//                int restore = patients.size() - removeNum;
        if (resNum > 0  ) {
            content = createName + "在 " + groupName + "组复原了" + resNum + "名患者";
            logUtil.saveLog(projectId, content, createId, createName);
//                    inFinalTol = distPatients.size() - removeNum;
        }
        List<Group> groupChildList = groupMapper.getgroupChildIds(groupId);
//        List<String> groupChildList = groupMapper.getgroupChildIds(groupId);
        obj.put("patients",patientsArray);
        if(patientsArray.size()>0){ //如果为1 的话才 移除 复原不复原所有
            for (Group group : groupChildList){
                String id = group.getGroupId();
                String name = group.getGroupName();
                removeGroupDataPatient(projectId,name,id,createId,createName,all,obj);
            }
        }
        String result = "";
        if("0".equals(type) && inFinalTol >0 && maxTol>0 && inFinalTol==maxTol && StringUtils.isNotEmpty(groupParentName) ){
//            result = "所复原的患者已在"+groupParentName+"组移除，无法复原";
            result = "有"+inFinalTol+"名患者已在"+groupParentName+"移除，无法复原";
        }else if("0".equals(type) && inFinalTol >0 && maxTol>0 && inFinalTol != maxTol && StringUtils.isNotEmpty(groupParentName) ){
            result = "有"+inFinalTol+"名患者已在"+groupParentName+"移除，无法复原";
        }
        return result;
    }


    private void saveUserGroupCondition(String projectId, String groupId) {
        List<ProjectMember> projectMembers = projectMemberMapper.getUserProjectList(projectId);
        for (ProjectMember projectMember : projectMembers) {
            GroupCondition groupCondition = new GroupCondition();
            groupCondition.setUid(projectMember.getUid());
            groupCondition.setGroupId(groupId);
            groupCondition.setProjectId(projectId);
            groupCondition.setCreateId(projectMember.getUid());
            // groupCondition.setCreateName();
            groupCondition.setCreateTime(new Date());
            groupCondition.setCortType(1);
            groupConditionMapper.insert(groupCondition);
            groupCondition.setCortType(2);
            groupConditionMapper.insert(groupCondition);
        }
    }

    // 增加 患者集从哪个患者集来的 columns
    private void addPatientSetCount(JSONArray columns) {
        JSONObject tmpObj = new JSONObject();
        tmpObj.put("id", "PatientSetName");
        tmpObj.put("name", "所属患者集");
        columns.add(1,tmpObj);
    }

    // 增加 患者集从哪个患者集来的 数据
    private void addPatientSetFrom(JSONArray result, String groupId) throws IOException {
        int size = result == null ? 0 : result.size();
        //递归找父及id
        String groupParId = getGroupParentId(groupId);
        List<PatientsSet> patientsSets = patientsSetMapper.getPatientSet(groupParId);
        for (int i = 0; i < size; i++) {
            JSONObject patientObj = result.getJSONObject(i);
            String patientSn = patientObj.getString("PATIENT_SN");
//            String patSetName = groupDataMapper.getPatSetNameByPatientSn(patientSn, groupId);
            String patSetName = getPatentSetName(patientsSets,patientSn);
            patientObj.put("PatientSetName", patSetName);
        }

    }

    private String getGroupParentId(String groupId) {
        String groupParId = groupMapper.getGroupParentId(groupId);
        while (StringUtils.isNotEmpty(groupParId)){
            groupId = groupParId;
            groupParId = groupMapper.getGroupParentId(groupId);
        }
        return groupId;
    }

    private String getPatentSetName(List<PatientsSet> patientsSets, String patientSn) throws IOException {
        List<String> patSetNames = new LinkedList<>();
        for (PatientsSet patientsSet : patientsSets){
            String query = patientsSet.putUncomUqlQuery();
            Set<String> patSet = TransPatientSql.getSetPatientSql(query);
            if(patSet.contains(patientSn)){
                patSetNames.add(patientsSet.getPatientsSetName());
            }
        }
        return String.join("+",patSetNames);
    }

    /**
     * 递归找 所有父组
     *
     * @param groupParentId
     */
    private void getParentGroup(String groupParentId,List<String> groupNames) {
        Group group = groupMapper.selectByGroupId(groupParentId);
        groupNames.add(0,group.getGroupName());
        String groupParentIdTmp = group.getGroupParentId();
        if (StringUtils.isNotEmpty(groupParentIdTmp)) {
            getParentGroup(groupParentIdTmp,groupNames);
        }
    }

    private Integer getPatientSnCount(String groupId) {
        List<String> list = groupDataMapper.getPatientSnList(groupId);
        return list.size();
    }

    private Integer getPatientSnCount(String groupId,String projectId) {
        List<String> list = groupDataMapper.getPatientSnList(groupId);
        return  list.size();
    }

    @Transactional
    @Override
    public void deletePatientGroup(JSONObject obj) {
        String createId = obj.getString("createId");
        String createName = obj.getString("createName");
        // 传递患者分组ID 包括大组和子组
        JSONArray arr = JSONObject.parseArray(obj.get("data").toString());
        for (int i = 0; i < arr.size(); i++) {
            String groupId = arr.getString(i);
            Group group = groupMapper.selectByGroupId(groupId);
            if (group != null) {
                group.setIsDelete(DelFlag.LOSE.toString());
                groupMapper.updateById(group);
                activeIndexService.deleteByActiveId(groupId);
                // 删除患者分组和患者集对照 可 批量操作
                List<GroupPatientData> list = groupPatDataMapper.selectByGroupId(groupId);
                for (GroupPatientData data : list) {
                    groupPatDataMapper.deleteById(data.getId());
                }
                List<GroupData> listGroupData = groupDataMapper.getByGroupId(groupId);
                // 删除患者分组和患者对照表 可 批量操作
                if (listGroupData != null && listGroupData.size() > 0) {
                    groupDataMapper.deleteByGroupId(groupId);
                }
            }
            String content = createName + "删除了组： " + group.getGroupName();
            logUtil.saveLog(group.getProjectId(), content, createId, createName);
        }
    }

    @Transactional
    @Override
    public Integer saveGroupAndPatient(JSONObject obj) throws IOException {
        boolean isExport = false;
        String groupId = obj.getString("groupId");
        String groupName = obj.getString("groupName");
        String createId = obj.getString("createId");
        String createName = obj.getString("createName");
        String projectId = obj.getString("projectId");
        String crfId = obj.getString("crfId");
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("groupId", groupId);
        DataCheckEmpty.dataCheckEmpty(groupId, projectId, createName, groupName);
        // 传递患者集ID 数组
        JSONArray arr = JSONObject.parseArray(obj.get("data").toString());
        String patientsSetId = arr.getJSONObject(0).getString("patientSetId");
        int count = groupPatDataMapper.getPatSetAndGroutId(groupId, patientsSetId);
        if (count > 0) {
            throw new CustomerException(CustomerStatusEnum.SUCCESS.toString(), "不能重复导入患者集");
        }
        String patients = "";
        List<List<GroupData>> list = new ArrayList<>();
        List<GroupData> listdata = new ArrayList<>();

        int startCount = groupDataMapper.getPatSetAggregationCount(groupId);
        int contCount = startCount;
        for (int i = 0; i < arr.size(); i++) {
            String patientSetId = arr.getJSONObject(i).getString("patientSetId");
            String patientSetName = arr.getJSONObject(i).getString("patientSetName");
            patientsSetMapper.updateIsFlush(3,patientSetId);
            if (StringUtils.isNotEmpty(patients)){
                patients = patients + "," + patientSetName;
            }else {
                patients = patientSetName;
            }
            // 获取患者集合 根据患者集ID查询患者集合
            // List<String> listPatients = getPatientFrouMongo(patientSetId,
            // groupId, createId, createName);
//            List<Patient> listPatients = getPatentByMongo(patientSetId, isExport);
            List<Patient> listPatients = searchByuqlService.getpatentByUql(patientSetId,isExport,projectId,crfId);
            contCount = contCount + listPatients.size();
            for (int j = 0; j < listPatients.size(); j++) {
                Patient patient = listPatients.get(j);
                if(j != 0 &&  j % exportMax == 0  ){
                    list.add(listdata);
                    listdata = new ArrayList<>();
                }
                GroupData groupData = new GroupData();
                groupData.setPatientSetId(patientSetId);
                groupData.setGroupId(groupId);
                groupData.setPatientSn(patient.getPatientSn());
                groupData.setEfhnic(patient.getEfhnic());
                groupData.setNationality(patient.getNationality());
                groupData.setPatientDocId(patient.getDOC_ID());
                groupData.setMaritalStatus(patient.getMaritalStatus());
                groupData.setGender(patient.getGender());
                groupData.setRemove(DelFlag.AVIABLE.toString());
                groupData.setCreateTime(new Date());
                groupData.setUpdateTime(new Date());
                listdata.add(groupData);
            }
            // 保存分组和患者集关系
            GroupPatientData gpData = new GroupPatientData();
            gpData.setGroupId(groupId);
            gpData.setPatientSetId(patientSetId);
            groupPatDataMapper.insert(gpData);
        }
        list.add(listdata);
        for (List<GroupData> li : list){
            if (li != null && !li.isEmpty()) {
                groupDataMapper.batchInsert(li);
            }
        }

        int endCount = groupDataMapper.getPatSetAggregationCount(groupId);
        // 将患者集ID 映射成患者集名称
        String content = createName + "将患者集：" + patients + " 添加到组：" + groupName;
        logUtil.saveLog(projectId, content, createId, createName);
        // 逻辑方式 导入的总数据 + 最开始的数据 - 导入后的数据 等于0 全部导入 大于0 有重复数据 小于0 全部为重复数据
        return contCount - endCount >= 0 ? contCount - endCount : contCount;

    }

    static class SortName{
        String name;
        Date createTime;

        public SortName(String name, Date createTime) {
            this.name = name;
            this.createTime = createTime;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }
    }

}
