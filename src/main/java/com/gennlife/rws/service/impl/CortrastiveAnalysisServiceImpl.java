package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.darren.collection.keypath.KeyPath;
import com.gennlife.rws.content.CommonContent;
import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.content.UqlConfig;
import com.gennlife.rws.dao.*;
import com.gennlife.rws.entity.*;
import com.gennlife.rws.service.*;
import com.gennlife.rws.util.*;
import com.gennlife.rws.web.WebAPIResult;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.apache.commons.math3.stat.inference.TTest;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.gennlife.darren.controlflow.exception.Force.force;
import static com.gennlife.darren.controlflow.for_.Foreach.foreach;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;

@Service
public class CortrastiveAnalysisServiceImpl implements CortrastiveAnalysisService {
    private static final Logger LOG = LoggerFactory.getLogger(CortrastiveAnalysisServiceImpl.class);
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private GroupConditionMapper groupConditionMapper;
    @Autowired
    private GroupDataMapper groupDataMapper;
    @Autowired
    private HttpUtils httpUtils;
    @Autowired
    private ContrastiveAnalysisActiveService contrastiveAnalysisActiveService;
    @Autowired
    private ActiveIndexMapper activeIndexMapper;
    @Autowired
    private ActiveSqlMapMapper activeSqlMapMapper;
    @Autowired
    private GroupService groupService;
    @Autowired
    private GroupTypeMapper groupTypeMapper;
    @Autowired
    private RedisMapDataService redisMapDataService;
    @Autowired
    private SearchByuqlService searchByuqlService;
    @Autowired
    private ActiveIndexConfigMapper activeIndexConfigMapper;


    @Override
    public AjaxObject getPatientGroupCondition(List<Group> groupList, List<GroupCondition> groupConditionList) {
        //从 条件组筛选 真正组
        Map<String, List<String>> plainOptions = new HashMap<>();
        for (GroupCondition groupCondition : groupConditionList) {
            String groupId = groupCondition.getGroupId();
            for (Group group : groupList) {
                if (group.getGroupId().equals(groupId)) {
                    group.setCheckable(true);
                    if (plainOptions.containsKey(group.getGroupTypeId())) {
                        List<String> tmpList = plainOptions.get(group.getGroupTypeId());
                        tmpList.add(groupId);
                    } else {
                        List<String> tmpList = new ArrayList<>();
                        tmpList.add(groupId);
                        plainOptions.put(group.getGroupTypeId(), tmpList);
                    }
                }
            }
        }
//        List<String> groupConditions =groupConditionList.stream().map(GroupCondition::getGroupId).collect(toList());
        JSONArray grouArray = getGroupTreeTo(groupList, 4);
        JSONArray newGroupArray = new JSONArray();
        int size = grouArray.size();
        for (int i = 0; i < size; i++) {
            JSONObject obj = grouArray.getJSONObject(i);
            String groupType = obj.getString("groupTypeId");
            obj.put("plainOptions", JSON.toJSONString(plainOptions.get(groupType)));
            newGroupArray.add(obj);
        }

        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
//        ajaxObject.setPlainOptions(groupConditions);
        ajaxObject.setData(newGroupArray);
        return ajaxObject;
    }

    @Override
    public AjaxObject getContResult(String createId, String projectId, Integer cortType, boolean showSubGroup, String crfId, String uid, boolean autoCort) throws ExecutionException, InterruptedException, IOException {
        List<Group> groupList = groupService.getGroupByProjectId("001", projectId);
        List<GroupCondition> groupConditionList = groupConditionMapper.getGroupByProjectId(uid, projectId, 1);
        if (groupConditionList == null || groupConditionList.size() == 0) {
            AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "没有选择条件组数据，无结果");
            ajaxObject.setData(new JSONArray());
            ajaxObject.setCount(0);
            return ajaxObject;
        }
        if (groupList == null || groupList.size() == 0) {
            AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "分组没有数据，无法计算");
            ajaxObject.setData(new JSONArray());
            ajaxObject.setCount(0);
            return ajaxObject;
        }
        //增加父及id
        Set<String> set = saveParentConditionList(groupConditionList, groupList);
        for (GroupCondition groupCondition : groupConditionList) {
            String groupId = groupCondition.getGroupId();
            for (Group group : groupList) {
                if (group.getGroupId().equals(groupId) || set.contains(group.getGroupId())) {
                    group.setCheckable(true);
                }
            }
        }
        List<Group> trueGroupList = copeGroupList(groupList);
        //拿到组数据 的sql patSn
        int maxLevel = groupList.stream()
            .mapToInt(Group::getGroupLevel)
            .max().getAsInt();
        int maxLevelChild = groupList.stream().
            filter(a -> a.getCheckable() != null && a.getCheckable()).
            collect(toList()).stream().
            mapToInt(Group::getGroupLevel).max().getAsInt();
        boolean childrenType = false; // true  为不可以点击  false 点击
        if (0 == maxLevelChild) {
            childrenType = true;
        }
        int trueLevel = maxLevel;

        if (!showSubGroup) {
            maxLevel = 0;
        }
        JSONArray grouArray = getGroupTree(groupList, maxLevel);
        JSONArray trueGroupArray = getGroupTree(trueGroupList, trueLevel);
        List<Group> trueLevelGroup = new ArrayList<>();
        getGrouMxLevel(trueGroupArray, trueLevelGroup);
        List<Group> maxLevelGroup = new ArrayList<>(); //获取最底层的组数据
        getGrouMxLevel(grouArray, maxLevelGroup);
        Map<String, Set<Group>> groupMaps = getBigGroups(groupList, trueLevel);
        int relayMaxLevel = maxLevelGroup.stream().mapToInt(Group::getGroupLevel).max().getAsInt();
        List<String> groupIds = trueLevelGroup.stream().map(Group::getGroupId).collect(toList());

        Integer countsAll = groupDataMapper.getPatSetCountBygroupIds(groupIds);
        //拿到条件
        List<String> activeIndexIds = contrastiveAnalysisActiveService.getActiveIndexes(uid, projectId, 1);
        if (activeIndexIds == null || activeIndexIds.size() == 0) {
            AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, "研究变量条件数据为空");
            ajaxObject.setData(new JSONArray());
            ajaxObject.setCount(countsAll);
            return ajaxObject;
        }
        List<Column> result = new ArrayList<>();
        List<Integer> groupCounts = new ArrayList<>();
        {
            List<List<ActiveSqlMap>> conditions = activeIndexIds.stream()
                .map(x -> {
                        List<ActiveSqlMap> activeSqlMaps = activeSqlMapMapper.getActiveSql(x, UqlConfig.CORT_INDEX_ID);
                        activeSqlMaps = referenceCalculateSearch(projectId, crfId, x, activeSqlMaps);
                        return activeSqlMaps;
                    }
                )
                .collect(toList());
            List<Group> groups = maxLevelGroup;
            // group, item, num->""/enum->value, patients
            List<Future> futures = new ArrayList<>();
            for (Group group : groups) {
                String patsCondition = null;
                if (group.getGroupLevel() == 0 && groupMaps.get(group.getGroupId()).size() > 0) {
                    patsCondition = getGroupsSql(groupMaps.get(group.getGroupId()), crfId);
                } else {
                    patsCondition = getGroupSql(group.getGroupId(), crfId);
                }
                int total = patsCondition.split("\\$").length - 1;  // 需要存病人数
                Column column = new Column();
                column.title = group.getGroupName();
                for (List<ActiveSqlMap> condition : conditions) {
                    if (condition.isEmpty()) {
                        continue;
                    }
                    ActiveSqlMap source = condition.get(0);
                    boolean isEnumeration = source.getIndexResultValue() != null;
                    DataType type = DataType.valueOf(source.getIndexTypeValue());
                    Item item = null;
                    if (isEnumeration) {
                        item = new DiscreteItem();
                        item.total = total;
                        item.suffix = " — 人数（百分比）";
                        Map<String,List<ActiveSqlMap>> groupActiveMap = condition.stream().collect(groupingBy(ActiveSqlMap :: getIndexResultValue));
                        Iterator<String> iterator = groupActiveMap.keySet().iterator();
                        while (iterator.hasNext()){
                            List<ActiveSqlMap> val = groupActiveMap.get(iterator.next());
                            ActiveSqlMap src = val.get(0);
                            DiscreteCell cell = new DiscreteCell();
                            cell.projectId = projectId;
                            cell.patsCondition = patsCondition;
                            cell.crfId = crfId;
                            cell.sqlMaps = condition;
                            String redisKey = UqlConfig.CORT_CONT_ENUM_REDIS_KEY + src.getActiveIndexId() + "_" + src.getId() + "_" + group.getGroupId();
                            if (redisMapDataService.exists(redisKey)) {
                                if (autoCort) {
                                    return null;
                                }
                                LOG.info("从缓存获取数据");
                                cell.patients = redisMapDataService.getAllSet(redisKey);
                            } else {
                                LOG.info("没有走缓存 进行redis 缓存");
                                cell.redisKey = redisKey;
                                futures.add(cell.execute(SingleExecutorService.getInstance().getCortrastiveCountResultExecutor()));
                            }
                            item.cells.add(cell);
                            item.enumTitles.add(src.getIndexResultValue());
                        }
                    } else if (type.isNumeric()) {
                        item = new ContinuousItem();
                        item.suffix = " — 均值 ± 标准差 / 范围 / 中位数";
                        ContinuousCell cell = new ContinuousCell();
                        cell.redisMapDataService = redisMapDataService;
                        cell.sqlMaps = condition;
                        cell.projectId = projectId;
                        cell.patsCondition = patsCondition;
                        cell.varName = "condition";
                        cell.crfId = crfId;
                        cell.groupId = group.getGroupId();
                        if (redisMapDataService.exists(UqlConfig.CORT_CONT_ACTIVE_REDIS_KEY.concat(source.getActiveIndexId() + "_" + group.getGroupId())) && autoCort) {
                            return null;
                        }
                        futures.add(cell.execute(SingleExecutorService.getInstance().getCortrastiveCountResultExecutor()));
                        item.cells.add(cell);
                        item.enumTitles.add("");
                    } else {
                        continue;
                    }
//                    item.title = source.getActiveName();
                    item.title = activeIndexMapper.findActiveName(source.getActiveIndexId());
                    column.items.add(item);
                }
                result.add(column);
                groupCounts.add(total);
            }
            for (Future future : futures) {
                future.get();
            }
            // ...
        }
        JSONObject table = new JSONObject();
        {
            int columnSize = result.size();
            int rowSize = result.get(0).items.size();
            JSONArray body = new JSONArray();
            table.put("data", body);
            table.put("grp_cnt", groupCounts.stream().collect(toCollection(JSONArray::new)));
            for (int i = 0; i < rowSize; ++i) {
                Item infoItem = result.get(0).items.get(i);
                JSONArray cellValues = new JSONArray();
                JSONObject rowObject = new JSONObject()
                    .fluentPut("col_name", infoItem.title)
                    .fluentPut("col_append", infoItem.suffix)
                    .fluentPut("values", cellValues)
                    .fluentPut("grp", result.stream()
                        .map(item -> item.title)
                        .collect(toCollection(JSONArray::new)));
                body.add(rowObject);
                try {
                    if (infoItem instanceof DiscreteItem) {
                        final int x = i;
                        if (result.stream()
                            .map(column -> (DiscreteItem) column.items.get(x))
                            .anyMatch(DiscreteItem::isConflicted)) {
                            rowObject.put("conflicted", true);
                        } else {
                            int size = result.size();
                            int flag = 0;
                            int tmpSize = result.get(0).items.get(x).cells.size();
                            long counts[][] = new long[size][tmpSize + 1];
                            for (int j = 0; j < size; j++) {
                                Column column = result.get(j);
                                Item item = column.items.get(x);
                                Long total = item.total;
                                int itemSize = item.cells.size();
                                for (int k = 0; k < itemSize; k++) {
                                    Cell cell = item.cells.get(k);
                                    Long count = cell.count();
                                    total = total - count;
                                    counts[j][k] = count;
                                }
                                if (total == 0) {
                                    flag++;
                                }
                                counts[j][itemSize] = total;
                            }
                            if (size == flag) {
                                for (int j = 0; j < counts.length; j++) {
                                    counts[j] = Arrays.copyOf(counts[j], counts[j].length - 1);
                                }
                            }
                            rowObject.put("p_value", new ChiSquareTest().chiSquareTest(transpose(counts)));
                            rowObject.put("conflicted", false);
                        }
                    } else if (infoItem instanceof ContinuousItem) {
                        final int x = i;
                        SummaryStatistics summaries[] = result.stream()
                            .map(column -> ((ContinuousCell) column.items.get(x).cells.get(0)).summary)
                            .toArray(SummaryStatistics[]::new);
                        if (summaries.length == 2) {
                            rowObject.put("p_value", new TTest().tTest(summaries[0], summaries[1]));
                        } else {
                            rowObject.put("p_value", new OneWayAnova().anovaPValue(asList(summaries), true));
                        }
                    } else {
                        rowObject.put("p_value", Double.NaN);
                    }
                } catch (Exception e) {
                    rowObject.put("p_value", Double.NaN);
                } finally {
                    Object pValue = rowObject.get("p_value");
                    if (pValue instanceof Number) {
                        double value = ((Number) pValue).doubleValue();
                        if (Double.isNaN(value)) {
                            rowObject.put("p_value", "N/A");
                        } else if (value < 0.001) {
                            rowObject.put("p_value", "<0.001");
                        } else {
                            rowObject.put("p_value", pValueFormat.format(value));
                        }
                    }
                }
                cellValues.add(infoItem.enumTitles.stream().collect(toCollection(JSONArray::new)));
                for (int j = 0; j < columnSize; ++j) {
                    Item item = result.get(j).items.get(i);
                    JSONArray subs = new JSONArray();
                    cellValues.add(subs);
                    for (Cell cell : item.cells) {
                        subs.add(cell.serialize(groupCounts.get(j)));
                    }
                }
            }
        }
//        List<ActiveIndex> activeIndex = activeIndexMapper.findByActiveIds(activeIndexIds);
        JSONObject response = new JSONObject();
        {
            System.out.println("demo");
            //处理成前端格式
            JSONArray columns = getViewGroupTree(grouArray, relayMaxLevel, trueGroupArray);

            JSONObject titleObj = new JSONObject();
            titleObj.put("title", "");
            titleObj.put("dataIndex", "groupType");
            titleObj.put("key", "groupType");
            columns.add(0, titleObj);
            JSONObject pObj = new JSONObject();
            pObj.put("title", "P值");
            pObj.put("dataIndex", "PID");
            pObj.put("key", "PID");
            columns.add(pObj);

            //生生的拼接data
            JSONArray data = parseData(new JSONObject()
                .fluentPut("code", 1)
                .fluentPut("data", table));
            response.put("columns", columns);
            response.put("data", data);
            response.put("childrenType", childrenType);
        }
        System.out.println(JSON.toJSONString(response));
        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
        ajaxObject.setData(response);
        ajaxObject.setCount(countsAll);
        return ajaxObject;
    }

    @Nullable
    private List<ActiveSqlMap> referenceCalculateSearch(String projectId, String crfId, String activeIndexId, List<ActiveSqlMap> activeSqlMaps) {
        if (activeSqlMaps == null || activeSqlMaps.size() == 0) {
            try {
                if (StringUtils.isNotEmpty(crfId) && !crfId.equals("EMR")) {
                    searchByuqlService.referenceCalculate(activeIndexId, projectId, CommonContent.ACTIVE_TYPE_INDEX, UqlConfig.RESULT_ORDER_KEY.get(crfId), null, UqlConfig.CORT_INDEX_ID, null, crfId);
                } else {
                    searchByuqlService.referenceCalculate(activeIndexId, projectId, CommonContent.ACTIVE_TYPE_INDEX, UqlConfig.RESULT_ORDER_KEY.get("EMR"), null, UqlConfig.CORT_INDEX_ID, null, crfId);
                }
                activeSqlMaps = activeSqlMapMapper.getActiveSql(activeIndexId, UqlConfig.CORT_INDEX_ID);
            } catch (Exception e) {
                contrastiveAnalysisActiveService.deleteContrastiveActiveById(activeIndexId, projectId);
                e.printStackTrace();
            }
        }
        return activeSqlMaps;
    }

    private List<Group> copeGroupList(List<Group> groupList) {
        List<Group> list = new ArrayList<>();
        for (Group aGroupList : groupList) {
            JSONObject object = JSON.parseObject(JSON.toJSONString(aGroupList));
            list.add(JSONObject.toJavaObject(object, Group.class));
        }
        return list;
    }

    private Set<String> saveParentConditionList(List<GroupCondition> groupConditionList, List<Group> groupList) {
        Map<String, Group> map = new HashMap<>();
        for (Group group : groupList) {
            map.put(group.getGroupId(), group);
        }
        Set<String> set = groupConditionList.stream().map(GroupCondition::getGroupId).collect(toSet());
        for (GroupCondition groupCondition : groupConditionList) {
            String id = groupCondition.getGroupId();
            Group group = map.get(id);
            if (group == null) {
                continue;
            }
            String patId = map.get(id).getGroupParentId();
            if (StringUtils.isNotEmpty(patId) && !set.contains(patId)) {
                set.add(patId);
            }
        }
        return set;
    }

    private Map<String, Set<Group>> getBigGroups(List<Group> groupList, Integer maxLevel) {
        Map<String, Set<Group>> resultMap = new HashMap<>();
        Map<String, String> mapGroup = new HashMap<>();
        for (int i = 0; i < maxLevel + 1; i++) {
            for (Group group : groupList) {
                Boolean checkAble = group.getCheckable();
                if (checkAble == null || !checkAble) {
                    continue;
                }
                Integer level = group.getGroupLevel();
                if (0 == level && !resultMap.containsKey(group.getGroupId())) {
                    resultMap.put(group.getGroupId(), new HashSet<>());
                } else {
                    if (resultMap.containsKey(group.getGroupParentId()) && (group.getChildGroup() == null || group.getChildGroup().size() == 0)) {
                        resultMap.get(group.getGroupParentId()).add(group);
                    } else if (resultMap.containsKey(mapGroup.get(group.getGroupParentId())) && (group.getChildGroup() == null || group.getChildGroup().size() == 0)) {
                        resultMap.get(mapGroup.get(group.getGroupParentId())).add(group);
                    }
                    mapGroup.put(group.getGroupId(), group.getGroupParentId());
                }
            }
        }
        return resultMap;
    }

    @Override
    public AjaxObject getContResultForPatient(String createId, String projectId, Integer pageNum, Integer pageSize, JSONArray showColumns, Integer cortType, String crfId, String uid) throws ExecutionException, InterruptedException {
        Integer startNum = (pageNum - 1) * pageSize;
        List<GroupCondition> groupConditionList = groupConditionMapper.getGroupByProjectId(uid, projectId, 2);
        List<Group> groupList = groupService.getGroupByProjectId("001", projectId);
        for (GroupCondition groupCondition : groupConditionList) {
            String groupId = groupCondition.getGroupId();
            for (Group group : groupList) {
                if (group.getGroupId().equals(groupId)) {
                    group.setCheckable(true);
                }
            }
        }
        int maxLevel = groupList.stream()
            .mapToInt(Group::getGroupLevel)
            .max().getAsInt();
        JSONArray grouArray = getGroupTree(groupList, maxLevel);
        List<Group> maxLevelGroup = new ArrayList<>(); //获取最底层的组数据
        getGrouMxLevel(grouArray, maxLevelGroup);

        List<String> groupIds = maxLevelGroup.stream().map(Group::getGroupId).collect(toList());
        Set<String> patSns = groupDataMapper.getPatientSnListsByGroupIds(groupIds);
        String applyOutCondition = String.join(",", patSns);

        List<GroupData> patientSns = groupDataMapper.getPatientSnByGroupIds(groupIds, startNum, pageSize);
        String patientSnQuery = IndexContent.getPatientInfoPatientSn(crfId) + TransPatientSql.transForExtContainForGroupData(patientSns);
        List<String> activeIndexIds = contrastiveAnalysisActiveService.getActiveIndexes(uid, projectId, 2);
        Integer counts = groupDataMapper.getPatSetCountBygroupIds(groupIds);
        if (activeIndexIds == null || activeIndexIds.isEmpty()) {
            JSONArray data = getContResultForPatientDataByNoActiveIndex(crfId, projectId, patientSnQuery, pageSize, patientSns);
            AjaxObject.getReallyDataValue(data, showColumns);
            AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
            ajaxObject.setData(data);
            ajaxObject.setColumns(showColumns);
            WebAPIResult webAPIResult = new WebAPIResult(pageNum, pageSize, counts);
            ajaxObject.setWebAPIResult(webAPIResult);
            ajaxObject.setApplyOutCondition(applyOutCondition);
            return ajaxObject;
        }
        String query = "select " + IndexContent.getPatientDocId(crfId) + " as patSn  from " + IndexContent.getIndexName(crfId, projectId) + " where " + patientSnQuery + " and  join_field='patient_info'";
        JSONArray source = new JSONArray().fluentAdd("patient_info");
        String resultSearch = httpUtils.querySearch(projectId, query, 1, pageSize, null, source, crfId);
        Map<String, JSONObject> resultMap;
        if (StringUtils.isEmpty(crfId) || IndexContent.EMR_CRF_ID.equals(crfId)) {
            resultMap = new KeyPath("hits", "hits")
                .resolveAsJSONArray(JSON.parseObject(resultSearch))
                .stream()
                .collect(toMap(
                    new KeyPath("_id")::resolveAsString,
                    new KeyPath("_source", IndexContent.getPatientInfo(crfId), 0)::resolveAsJSONObject));
        } else {
            resultMap = new KeyPath("hits", "hits")
                .resolveAsJSONArray(JSON.parseObject(resultSearch))
                .stream()
                .collect(toMap(
                    new KeyPath("_id")::resolveAsString,
                    new KeyPath("_source", "patient_info", 0, "patient_basicinfo", 0)::resolveAsJSONObject));
        }

        List<Future> futures = new ArrayList<>();
        LOG.info("time1");
        long time1 = System.currentTimeMillis();
        for (String activeIndexId : activeIndexIds) {
            futures.add(SingleExecutorService.getInstance().getCortrastiveAnalysisExecutor().submit(() -> {
                List<ActiveSqlMap> activeSqlMaps = activeSqlMapMapper.getActiveSql(activeIndexId, UqlConfig.CORT_INDEX_ID);
                activeSqlMaps = referenceCalculateSearch(projectId, crfId, activeIndexId, activeSqlMaps);
                if (activeSqlMaps.size() > 0) {
                    //这里有问题 不应该查不到数据的 说明需要重新计算 后期增加
                    ActiveSqlMap activeSqlMap = activeSqlMaps.get(0);
                    String activeName = activeIndexMapper.findActiveName(activeSqlMap.getActiveIndexId());
                    String activeId = activeSqlMap.getActiveIndexId();
                    JSONObject obj = new JSONObject().fluentPut("id", activeId).fluentPut("name", activeName);
                    showColumns.add(obj);
                    String indexResultValue = activeSqlMap.getIndexResultValue();
                    if (redisMapDataService.exists(UqlConfig.CORT_INDEX_REDIS_KEY.concat(activeIndexId))) {
                        for (String key : resultMap.keySet()) {
                            String val = redisMapDataService.hmGetKey(UqlConfig.CORT_INDEX_REDIS_KEY.concat(activeIndexId), key);
                            resultMap.get(key).put(activeSqlMap.getActiveIndexId(), StringUtils.isEmpty(val) ? "-" : val);
                        }
                    } else {
                        try {
                            if (StringUtils.isEmpty(indexResultValue)) {//指标
                                Map<String, String> mapAll = new HashMap<>();
                                for (ActiveSqlMap sqlMap : activeSqlMaps) {
                                    Map<String, String> map = searchByuqlService.saveCortrastiveResultRedisMap(sqlMap, projectId, crfId, activeIndexId);
                                    mapAll.putAll(map);
                                }
                                foreach(resultMap.keySet(), key -> resultMap.get(key).put(activeSqlMap.getActiveIndexId(), StringUtils.isEmpty(mapAll.get(key)) ? "-" : mapAll.get(key)));
                            } else {//枚举
                                Map<String, String> mapAll = new HashMap<>();
                                Map<Integer, List<ActiveSqlMap>> groupMap = activeSqlMaps.stream().collect(groupingBy(ActiveSqlMap::getPatSqlGroup, TreeMap::new, toList()));
                                Iterator<Integer> iterator = groupMap.keySet().iterator();
                                while (iterator.hasNext()) {
                                    Integer mapKey = iterator.next();
                                    List<ActiveSqlMap> value = groupMap.get(mapKey);
                                    Map<String, String> map = searchByuqlService.saveEnumCortrastiveResultRedisMap(value, projectId, crfId, activeIndexId);
                                    mapAll.putAll(map);
                                }
                                foreach(resultMap.keySet(), key -> resultMap.get(key).put(activeSqlMap.getActiveIndexId(), StringUtils.isEmpty(mapAll.get(key)) ? "-" : mapAll.get(key)));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }));
        }
        for (Future future : futures) {
            LOG.info("0---0");
            future.get();
        }
        LOG.info("时间啊-----" + (System.currentTimeMillis() - time1));
        JSONArray data = new JSONArray();
        for (GroupData groupData : patientSns) {
            String docId = groupData.getPatientDocId();
            JSONObject object = resultMap.get(docId);
            object.put("GROUP_NAME", groupData.getGroupName());
            data.add(object);
        }
        AjaxObject.getReallyDataValue(data, showColumns);
        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
        ajaxObject.setData(data);
        ajaxObject.setColumns(showColumns);
        WebAPIResult webAPIResult = new WebAPIResult(pageNum, pageSize, counts);
        ajaxObject.setWebAPIResult(webAPIResult);
        ajaxObject.setApplyOutCondition(applyOutCondition);
        return ajaxObject;
    }

    @Override
    public AjaxObject saveGroupCondition(String uname, String uid, String projectId, JSONArray groupsIds, Integer cortType, String groupTypeId, String createId) {
        List<String> groupList = groupsIds.toJavaList(String.class);
        groupConditionMapper.deleteByprojectIdAndUid(uid, projectId, groupTypeId, cortType);

        for (String groupId : groupList) {
            if (StringUtils.isEmpty(groupId) || "null".equals(groupId)) {
                continue;
            }
            GroupCondition groupCondition = new GroupCondition();
            groupCondition.setUid(uid);
            groupCondition.setProjectId(projectId);
            groupCondition.setGroupId(groupId);
            groupCondition.setUpdateId(uid);
            groupCondition.setUpdateName(uname);
            groupCondition.setUpdateTime(new Date());
            groupCondition.setCortType(cortType);
            groupConditionMapper.insert(groupCondition);
        }
        return new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
    }

    @Override
    public Object calculationResult(JSONObject paramObj) throws IOException, ExecutionException, InterruptedException {
        String projectId = paramObj.getString("projectId");
        String crfId = paramObj.getString("crfId");
        String uid = paramObj.getString("uid");
        List<String> activeIndexIds = paramObj.getJSONArray("calculations").toJavaList(String.class);
        List<String> groupIds = paramObj.getJSONArray("groupId").toJavaList(String.class);
        List<String> patientSns = paramObj.getJSONArray("patientSns").toJavaList(String.class);
        if (groupIds == null || groupIds.size() == 0) {
            List<GroupCondition> groupConditionList = groupConditionMapper.getGroupByProjectId(uid, projectId, 2);
            List<Group> groupList = groupService.getGroupByProjectId("001", projectId);
            for (GroupCondition groupCondition : groupConditionList) {
                String groupId = groupCondition.getGroupId();
                for (Group group : groupList) {
                    if (group.getGroupId().equals(groupId)) {
                        group.setCheckable(true);
                    }
                }
            }
            int maxLevel = groupList.stream()
                .mapToInt(Group::getGroupLevel)
                .max().getAsInt();
            JSONArray grouArray = getGroupTree(groupList, maxLevel);
            List<Group> maxLevelGroup = new ArrayList<>(); //获取最底层的组数据
            getGrouMxLevel(grouArray, maxLevelGroup);

            groupIds = maxLevelGroup.stream().map(Group::getGroupId).collect(toList());
        }

        JSONArray data = new JSONArray();
        Map<String, String> activeNames = new HashMap<>();
        Map<String, String> actieveIndexType = new HashMap<>();
        Map<String, JSONObject> groupIdQuery = new HashMap<>();
        Map<String, String> patDocIdPatSnMap = new HashMap<>();
        if (patientSns != null && patientSns.size() > 0) {
            Map<String, List<GroupData>> groupMaps = new HashMap<>();
            List<GroupData> groupDatas = groupDataMapper.getGroupDataByPatientSns(patientSns);
            for (GroupData groupData : groupDatas) {
                if (!groupMaps.containsKey(groupData.getGroupId())) {
                    groupMaps.put(groupData.getGroupId(), new ArrayList<>());
                }
                groupMaps.get(groupData.getGroupId()).add(groupData);
            }
            for (Map.Entry<String, List<GroupData>> entry : groupMaps.entrySet()) {
                String groupId = entry.getKey();
                Integer count = groupMapper.selectCountByGroupIdAndUid(groupId, uid);
                if (count == null || count == 0) {
                    continue;
                }
                List<GroupData> groupDataList = entry.getValue();
                String query = TransPatientSql.getSqlByPatSns(groupDataList, crfId);
                String groupName = groupMapper.getGroupNameByGroupId(groupId);
                String groupNamePath = groupService.getGroupNamePath(groupId, groupName);
                JSONObject object = new JSONObject()
                    .fluentPut("groupId", groupId)
                    .fluentPut("groupName", groupName)
                    .fluentPut("query", query)
                    .fluentPut("groupDatas", groupDatas)
                    .fluentPut("groupNamePath", groupNamePath);
                groupIdQuery.put(groupId, object);
            }
        } else {
            for (String groupId : groupIds) {
                List<GroupData> groupDatas = groupDataMapper.getPatientSnListAndDocId(groupId);
                String query = TransPatientSql.getSqlByPatSns(groupDatas, crfId);
                String groupName = groupMapper.getGroupNameByGroupId(groupId);
                String groupNamePath = groupService.getGroupNamePath(groupId, groupName);
                JSONObject object = new JSONObject()
                    .fluentPut("groupId", groupId)
                    .fluentPut("groupName", groupName)
                    .fluentPut("query", query)
                    .fluentPut("groupDatas", groupDatas)
                    .fluentPut("groupNamePath", groupNamePath);
                groupIdQuery.put(groupId, object);
            }
        }
        if (patientSns.size() > 0) {
            Map<String, List<JSONObject>> resultMap = new HashMap<>();
            boolean isCalculate = true;
            for (String groupId : groupIdQuery.keySet()) {
                List<GroupData> groupDataList = groupIdQuery.get(groupId).getJSONArray("groupDatas").toJavaList(GroupData.class);
                String query = groupIdQuery.get(groupId).getString("query");
                if (isCalculate) {
                    groupDataList.forEach(x -> {
                        resultMap.put(x.getPatientDocId(), new ArrayList<>());
                        patDocIdPatSnMap.put(x.getPatientDocId(), x.getPatientSn());
                    });
                    calculate(activeIndexIds, query, projectId, crfId, resultMap, activeNames, actieveIndexType);
                    isCalculate = false;
                }
                foreach(resultMap, (key, val) -> data.add(new JSONObject()
                    .fluentPut("patSn", patDocIdPatSnMap.get(key))
                    .fluentPut("groupId", groupIdQuery.get(groupId).getString("groupId"))
                    .fluentPut("groupName", groupIdQuery.get(groupId).getString("groupName"))
                    .fluentPut("groupNamePath", groupIdQuery.get(groupId).getString("groupNamePath"))
                    .fluentPut("result", val)));
            }
        } else {
            for (String groupId : groupIdQuery.keySet()) {
                List<GroupData> groupDataList = groupIdQuery.get(groupId).getJSONArray("groupDatas").toJavaList(GroupData.class);
                Map<String, List<JSONObject>> resultMap = new HashMap<>();
                groupDataList.forEach(x -> {
                    resultMap.put(x.getPatientDocId(), new ArrayList<>());
                    patDocIdPatSnMap.put(x.getPatientDocId(), x.getPatientSn());
                });
                String query = groupIdQuery.get(groupId).getString("query");
                calculate(activeIndexIds, query, projectId, crfId, resultMap, activeNames, actieveIndexType);
                foreach(resultMap, (key, val) -> data.add(new JSONObject().fluentPut("patSn", patDocIdPatSnMap.get(key))
                    .fluentPut("groupId", groupIdQuery.get(groupId).getString("groupId"))
                    .fluentPut("groupName", groupIdQuery.get(groupId).getString("groupName"))
                    .fluentPut("groupNamePath", groupIdQuery.get(groupId).getString("groupNamePath"))
                    .fluentPut("result", val)));
            }
        }


        return new JSONObject()
            .fluentPut("status", 200)
            .fluentPut("data", data);
    }

    @Override
    public Object calculationResultOne(JSONObject paramObj) throws InterruptedException, ExecutionException, IOException {
        String projectId = paramObj.getString("projectId");
        String crfId = paramObj.getString("crfId");
        String taskId = paramObj.getString("taskId"); //导出项目id
        List<String> activeIndexIds = paramObj.getJSONArray("calculations").toJavaList(String.class);
        List<String> patientSns = paramObj.getJSONArray("patientSns").toJavaList(String.class);
        List<String> patDocIds = groupDataMapper.getPatientDocIdsByPatientSns(patientSns);
        Map<String, String> activeNames = new HashMap<>();
        Map<String, String> actieveIndexType = new HashMap<>();
        Map<String, List<JSONObject>> resultMap = new HashMap<>();

        JSONObject data = new JSONObject();
        for (String activeId : activeIndexIds) {
            if (!redisMapDataService.exists(UqlConfig.CORT_INDEX_REDIS_KEY.concat(activeId))) {
                calculateAll(activeIndexIds, projectId, crfId, activeNames, activeNames);
            }
            for (String docId : patDocIds) {
                String val = redisMapDataService.hmGetKey(UqlConfig.CORT_INDEX_REDIS_KEY.concat(activeId), docId);
                data.put(activeId, StringUtils.isEmpty(val) ? "-" : val);
            }
        }
        return new JSONObject()
            .fluentPut("status", 200)
            .fluentPut("data", data);

    }

    @Override
    public Object snapshootActiveResult(JSONObject paramObj) {
        String projectId = paramObj.getString("projectId");
        String taskId = paramObj.getString("taskId"); //导出项目id
        List<ActiveIndex> activeIndices = activeIndexMapper.getClasActiveIdsNameAndIdsByProjectId(projectId);
        for (ActiveIndex activeIndex : activeIndices) {
            String activeIndexId = activeIndex.getId();
            List<ActiveSqlMap> activeSqlMaps = activeSqlMapMapper.getActiveSql(activeIndexId, UqlConfig.CORT_INDEX_ID);
            int count = activeSqlMapMapper.getCountByActiveIndexId(taskId + "_" + activeIndexId, UqlConfig.CORT_INDEX_ID);
            if (count > 0) {
                continue;
            }
            for (ActiveSqlMap activeSqlMap : activeSqlMaps) {
                activeSqlMap.setId(null);
                activeSqlMap.setActiveIndexId(taskId + "_" + activeIndexId);
                activeSqlMapMapper.insert(activeSqlMap);
            }
        }
        return null;
    }

    @Override
    public void deleteActiveIndexVariable(String projectId) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("projectId", projectId);
        mapParam.put("isVariant", 1);
        List<ActiveIndex> activeIndex = activeIndexMapper.getAllResearchVariable(mapParam);
        List<Group> groupList = groupMapper.getGroupListByProjectId(projectId);
        for (ActiveIndex activeIndex1 : activeIndex) {
            String id = activeIndex1.getId();
            redisMapDataService.delete(UqlConfig.CORT_INDEX_REDIS_KEY.concat(id));
            for (Group group : groupList) {
                String groupId = group.getGroupId();
                redisMapDataService.delete(UqlConfig.CORT_CONT_ACTIVE_REDIS_KEY.concat(id + "_" + groupId));
                List<ActiveSqlMap> delList = activeSqlMapMapper.getDelRedisActiveSql(id);
                if (delList.size() > 0) {
                    for (ActiveSqlMap src : delList) {
                        redisMapDataService.delete(UqlConfig.CORT_CONT_ENUM_REDIS_KEY + src.getActiveIndexId() + "_" + src.getId() + "_" + groupId);
                    }
                }
            }
        }
    }

    @Override
    public List<Group> getCortastiveGroupList(String uid, String projectId) {
        List<GroupCondition> groupConditionList = groupConditionMapper.getGroupByProjectId(uid, projectId, 2);
        List<Group> groupList = groupService.getGroupByProjectId("001", projectId);
        for (GroupCondition groupCondition : groupConditionList) {
            String groupId = groupCondition.getGroupId();
            for (Group group : groupList) {
                if (group.getGroupId().equals(groupId)) {
                    group.setCheckable(true);
                }
            }
        }
        int maxLevel = groupList.stream()
            .mapToInt(Group::getGroupLevel)
            .max().getAsInt();
        JSONArray grouArray = getGroupTree(groupList, maxLevel);
        List<Group> maxLevelGroup = new ArrayList<>(); //获取最底层的组数据
        getGrouMxLevel(grouArray, maxLevelGroup);
        return maxLevelGroup;
    }

    @Override
    public void autoBackgroundCecort(List<Project> projectList) {
        for (Project project : projectList) {
            String projectId = project.getProjectId();
            String crfId = project.getCrfId();
            String uid = project.getCreatorId();
            if (StringUtils.isEmpty(uid) || StringUtils.isEmpty(crfId)) {
                continue;
            }
            List<String> activeIndexIds = contrastiveAnalysisActiveService.getActiveIndexes(uid, projectId, 2);
            for (String activeIndexId : activeIndexIds) {
                SingleExecutorService.getInstance().getAutoCortrastiveExecutor().submit(() -> {
                    try {
                        if (!redisMapDataService.exists(UqlConfig.CORT_INDEX_REDIS_KEY.concat(activeIndexId))) {
                            LOG.info("患者列表开始缓存计算----activeIndexId: " + activeIndexId + "projectId: " + projectId);
                            List<ActiveSqlMap> activeSqlMaps = activeSqlMapMapper.getActiveSql(activeIndexId, UqlConfig.CORT_INDEX_ID);
                            ActiveSqlMap activeSqlMap = activeSqlMaps.get(0);
                            String indexResultValue = activeSqlMap.getIndexResultValue();
                            if (StringUtils.isEmpty(indexResultValue)) {//指标
                                searchByuqlService.saveCortrastiveResultRedisMap(activeSqlMap, projectId, crfId, activeIndexId);
                            } else {//枚举
                                searchByuqlService.saveEnumCortrastiveResultRedisMap(activeSqlMaps, projectId, crfId, activeIndexId);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            SingleExecutorService.getInstance().getAutoCortrastiveExecutor().submit(() -> {
                try {
                    LOG.info("图形列表 开始缓存计算----" + projectId);
                    getContResult(uid, projectId, 1, true, crfId, uid, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void calculate(List<String> activeIndexIds, String patientSnQuery, String projectId, String crfId, Map<String, List<JSONObject>> resultMap, Map<String, String> activeNames, Map<String, String> actieveIndexType) throws IOException, ExecutionException, InterruptedException {
        List<Future> futures = new ArrayList<>();
        for (String activeIndexId : activeIndexIds) {
            futures.add(SingleExecutorService.getInstance().getCortrastiveAnalysisExecutor().submit(() -> {
                try {
                    List<ActiveSqlMap> activeSqlMaps = activeSqlMapMapper.getActiveSql(activeIndexId, UqlConfig.CORT_INDEX_ID);
                    if (activeSqlMaps.size() < 1) {
                        return;
                    }
                    ActiveSqlMap activeSqlMap = activeSqlMaps.get(0);
                    String activeName = "";
                    if (!activeNames.containsKey(activeIndexId)) {
                        activeName = activeIndexMapper.findActiveName(activeSqlMap.getActiveIndexId());
                        activeNames.put(activeIndexId, activeName);
                    } else {
                        activeName = activeNames.get(activeIndexId);
                    }
                    String type = "";
                    if (!actieveIndexType.containsKey(activeIndexId)) {
                        List<String> types = activeIndexConfigMapper.getActiveIndexType(activeSqlMap.getActiveIndexId());
                        type = types.get(0);
                        actieveIndexType.put(activeIndexId, type);
                    } else {
                        type = actieveIndexType.get(activeIndexId);
                    }
                    JSONObject obj = new JSONObject().fluentPut("activeId", activeIndexId).fluentPut("activeName", activeName).fluentPut("type", transForIndexType(type));
                    String indexResultValue = activeSqlMap.getIndexResultValue();

                    if (StringUtils.isEmpty(indexResultValue)) {//指标
                        activeSqlMap.setUncomSqlWhere("(" + activeSqlMap.getUncomSqlWhere() + ") AND " + patientSnQuery);
                        String result = httpUtils.querySearch(projectId, activeSqlMap.getUql(crfId), 1, Integer.MAX_VALUE - 1, null, new JSONArray(), crfId);
                        Map<String, Object> map = new KeyPath("hits", "hits")
                            .resolveAsJSONArray(JSON.parseObject(result))
                            .stream()
                            .map(new KeyPath("_source", "select_field")::resolveAsJSONObject)
                            .collect(toMap(o -> o.getString(IndexContent.getPatientDocId(crfId)), o -> o.get("condition") == null ? "-" : o.get("condition")));
                        foreach(map, (key, value) -> {
                            if (!resultMap.containsKey(key)) {
                                resultMap.put(key, new ArrayList<>());
                            }
                            if (String.valueOf(value).contains(".")) {
                                try {
                                    value = String.format("%.2f", Double.parseDouble(String.valueOf(value)));
                                } catch (Exception e) {
                                }
                            }
                            obj.put("val", value);
                            resultMap.get(key).add(obj);
                        });
                    } else {//枚举
                        Map<String, EnumResult> map = new HashMap<>();
                        for (ActiveSqlMap activeSqlMap1 : activeSqlMaps) {
                            String indexValue = activeSqlMap1.getIndexResultValue();
                            activeSqlMap1.setUncomSqlWhere("(" + activeSqlMap1.getUncomSqlWhere() + ") AND " + patientSnQuery);
                            String result = httpUtils.querySearch(projectId, activeSqlMap1.getUql(crfId), 1, Integer.MAX_VALUE - 1, null, new JSONArray(), crfId);
                            List<String> list = new KeyPath("hits", "hits", "_source", "select_field", IndexContent.getPatientDocId(crfId))
                                .fuzzyResolve(JSON.parseObject(result))
                                .stream()
                                .map(String.class::cast)
                                .collect(toList());
                            for (String key : list) {
                                if (!map.containsKey(key)) {
                                    map.put(key, new EnumResult());
                                }
                                map.get(key).add(indexValue);
                            }
                        }
                        foreach(map, (key, value) -> {
                            if (!resultMap.containsKey(key)) {
                                resultMap.put(key, new ArrayList<>());
                            }
                            obj.put("val", value.toString());
                            resultMap.get(key).add(obj);
                        });
                    }
                } catch (IOException e) {
                    LOG.error("计算发生了错误！！！");
                }
            }));
        }
        for (Future future : futures) {
            future.get();
        }
    }

    private void calculateAll(List<String> activeIndexIds, String projectId, String crfId, Map<String, String> activeNames, Map<String, String> actieveIndexType) throws IOException, ExecutionException, InterruptedException {
        List<Future> futures = new ArrayList<>();
        for (String activeIndexId : activeIndexIds) {
            if (redisMapDataService.exists(UqlConfig.CORT_INDEX_REDIS_KEY.concat(activeIndexId))) {
                continue;
            }
            futures.add(SingleExecutorService.getInstance().getCortrastiveAnalysisExecutor().submit(() -> {
                try {
                    List<ActiveSqlMap> activeSqlMaps = activeSqlMapMapper.getActiveSql(activeIndexId, UqlConfig.CORT_INDEX_ID);
                    if (activeSqlMaps.size() < 1) {
                        return;
                    }
                    ActiveSqlMap activeSqlMap = activeSqlMaps.get(0);
                    String activeName = "";
                    if (!activeNames.containsKey(activeIndexId)) {
                        activeName = activeIndexMapper.findActiveName(activeSqlMap.getActiveIndexId());
                        activeNames.put(activeIndexId, activeName);
                    } else {
                        activeName = activeNames.get(activeIndexId);
                    }
                    String type = "";
                    if (!actieveIndexType.containsKey(activeIndexId)) {
                        List<String> types = activeIndexConfigMapper.getActiveIndexType(activeSqlMap.getActiveIndexId());
                        type = types.get(0);
                        actieveIndexType.put(activeIndexId, type);
                    } else {
                        type = actieveIndexType.get(activeIndexId);
                    }
                    JSONObject obj = new JSONObject().fluentPut("activeId", activeIndexId).fluentPut("activeName", activeName).fluentPut("type", transForIndexType(type));
                    String indexResultValue = activeSqlMap.getIndexResultValue();

                    if (StringUtils.isEmpty(indexResultValue)) {//指标
                        Map<String, String> map = searchByuqlService.saveCortrastiveResultRedisMap(activeSqlMap, projectId, crfId, activeIndexId);
                    } else {//枚举
                        Map<String, String> map = searchByuqlService.saveEnumCortrastiveResultRedisMap(activeSqlMaps, projectId, crfId, activeIndexId);
                    }

                } catch (IOException e) {
                    LOG.error("计算发生了错误！！！");
                }
            }));
        }
        for (Future future : futures) {
            future.get();
        }
    }

    private String transForIndexType(String type) {
        if (type.contains(":")) {
            return type.split(":")[1];
        } else {
            return type;
        }
    }

    private JSONArray getGroupTreeTo(List<Group> groupList, int maxLevel) {
        Map<String, Group> groupMap = new HashMap<>();
        Map<String, Group> tmpMap = new HashMap<>();
        for (int i = 0; i < maxLevel + 1; i++) {
            for (Group group : groupList) {
                int groupLevel = group.getGroupLevel();
                if (i == groupLevel) {
                    String groupId = group.getGroupId();
                    String groupParentId = group.getGroupParentId();
                    int groupNum = groupDataMapper.getPatSetAggregationCount(group.getGroupId());
                    group.setGroupNum(groupNum);
                    if (StringUtils.isNotEmpty(groupParentId) && tmpMap.containsKey(groupParentId)) {
                        tmpMap.get(group.getGroupParentId()).getChildGroup().add(group);
                        tmpMap.put(groupId, group);
                    } else {
                        groupMap.put(groupId, group);
                        tmpMap.put(groupId, group);
                    }
                }
            }
        }
        Map<String, Group> groupMap1 = new HashMap<>();
        JSONArray jsonArray = new JSONArray();
        for (Map.Entry<String, Group> map : groupMap.entrySet()) {
            Group group = map.getValue();
            if (groupMap1.containsKey(group.getGroupTypeId())) {
                groupMap1.get(group.getGroupTypeId()).getChildGroup().add(group);
            } else {
                String groupTypeName = groupTypeMapper.getGroupTypeName(group.getGroupTypeId());
                Group resultGroup = new Group(group.getGroupTypeId(), groupTypeName, groupTypeName);
                resultGroup.getChildGroup().add(group);
                groupMap1.put(group.getGroupTypeId(), resultGroup);
                jsonArray.add(resultGroup);
            }
        }
        JSONArray resultArray = new JSONArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            Group group = jsonArray.getJSONObject(i).toJavaObject(Group.class);
            sortGroupList(group);
            resultArray.add(group);
        }
        return resultArray;
    }

    private void sortGroupList(Group group) {
        List<Group> groupChilds = group.getChildGroup();
        if (groupChilds != null && groupChilds.size() > 1) {
            groupChilds = groupChilds.stream().sorted(Comparator.comparing(Group::getCreateTime)).collect(toList());
            group.setChildGroup(groupChilds);
            for (Group g1 : groupChilds) {
                sortGroupList(g1);
            }
        } else {
            return;
        }
    }

    private static boolean doublePresentable(String s) {
        try {
            // noinspection ResultOfMethodCallIgnored
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void getGrouMxLevel(JSONArray grouArray, List<Group> maxLevelGroup) {
        int size = grouArray.size();
        for (int i = 0; i < size; i++) {
            JSONObject obj = grouArray.getJSONObject(i);
            if (obj.containsKey("childGroup") && obj.getJSONArray("childGroup").size() > 0) {
                JSONArray childGroup = obj.getJSONArray("childGroup");
                getGrouMxLevel(childGroup, maxLevelGroup);
            } else {
                maxLevelGroup.add(JSONObject.toJavaObject(obj, Group.class));
            }
        }
    }

    private JSONArray parseData(JSONObject obj) {
        JSONObject data = obj.getJSONObject("data");
        JSONArray dataArray = data.getJSONArray("data");
        JSONArray resultArray = new JSONArray();
        int size = dataArray == null ? 0 : dataArray.size();
        for (int i = 0; i < size; i++) {
            JSONObject tmpObj = dataArray.getJSONObject(i);
            JSONObject object = new JSONObject();
            String colName = tmpObj.getString("col_name");
            String colAppend = tmpObj.getString("col_append");
            if (colAppend != null) {
                colName += colAppend;
            }
            object.put("groupType", colName);
            resultArray.add(object);
            JSONArray grp = tmpObj.getJSONArray("grp");
            JSONArray values = tmpObj.getJSONArray("values");
            Object pValue = tmpObj.get("p_value");
            Boolean conflicted = tmpObj.getBoolean("conflicted");
            parseValues(resultArray, grp, values, pValue == null || pValue.equals(Double.NaN) ? "N/A" : pValue, conflicted);
        }
        return resultArray;
    }

    private void parseValues(JSONArray resultArray, JSONArray grp, JSONArray values, Object pValue, Boolean conflicted) {
        int grpSize = grp.size();
        int valuesSize = values.getJSONArray(0).size();
        for (int i = 0; i < valuesSize; i++) {
            JSONObject obj = new JSONObject();
            obj.put("PID", pValue);
            if (conflicted != null) {
                obj.put("conflicted", conflicted);
            }
            obj.put("groupType", values.getJSONArray(0).getString(i));
            for (int j = 0; j < grpSize; j++) {
                String grpName = grp.getString(j);
                obj.put(grpName, values.getJSONArray(j + 1).get(i));
            }
            resultArray.add(obj);
        }
    }

    public String getGroupsSql(Set<Group> groups, String crfId) {
        List<String> groupDatPatSns = new ArrayList<>();
        for (Group group : groups) {
            List<String> groupDataPatSn = groupDataMapper.getPatientDocId(group.getGroupId());
            groupDatPatSns.addAll(groupDataPatSn);
        }
        return " " + IndexContent.getPatientDocId(crfId) + TransPatientSql.transForExtContain(groupDatPatSns);
    }

    public String getGroupSql(String groupId, String crfId) {
        List<String> groupDataPatSn = groupDataMapper.getPatientDocId(groupId);
        return " " + IndexContent.getPatientDocId(crfId) + TransPatientSql.transForExtContain(groupDataPatSn);
    }

    private JSONArray getViewGroupTree(JSONArray grouArray, int maxLevel, JSONArray trueGroupArray) {
        int size = grouArray == null ? 0 : grouArray.size();
        JSONArray resultArr = new JSONArray();

        for (int i = 0; i < size; i++) {
            JSONObject groupObj = grouArray.getJSONObject(i);
            JSONObject trueGroupObj = trueGroupArray.getJSONObject(i);
            JSONObject viewObj = new JSONObject();
            String groupId = groupObj.getString("groupId");
            Integer groupLevel = groupObj.getInteger("groupLevel") == null ? -1 : groupObj.getInteger("groupLevel");
            Integer patientsCount = 0;
            if (0 == groupLevel) {
                patientsCount = getSumsbGroupCHilds(trueGroupObj, patientsCount);
                if (patientsCount == 0) {
                    patientsCount = groupObj.getInteger("groupNum") == null ? 0 : groupObj.getInteger("groupNum");
                }
            } else if (1 == groupLevel) {
                patientsCount = getSumGroupChilds(trueGroupObj, patientsCount);
                if (patientsCount == 0) {
                    patientsCount = groupObj.getInteger("groupNum") == null ? 0 : groupObj.getInteger("groupNum");
                }
            } else {
                patientsCount = groupObj.getInteger("groupNum") == null ? 0 : groupObj.getInteger("groupNum");
            }
            String attach = patientsCount == 0 ? "" : "(" + patientsCount + ")";
            viewObj.put("title", groupObj.getString("groupName") + attach);
            if (groupLevel == maxLevel) {
                viewObj.put("dataIndex", groupObj.getString("groupName"));
                viewObj.put("key", groupObj.getString("groupName"));
            }
            if (groupLevel < maxLevel) {
                if (groupObj.getJSONArray("childGroup").size() != 0) {
                    viewObj.put("children", getViewGroupTree(groupObj.getJSONArray("childGroup"), maxLevel, trueGroupObj.getJSONArray("childGroup")));
                } else {
                    viewObj.put("children", putViewChildren(groupLevel + 1, maxLevel, groupObj.getString("groupName")));
                }
            }
            resultArr.add(viewObj);
        }
        return resultArr;
    }

    private Integer getSumsbGroupCHilds(JSONObject groupObj, Integer sum) {
        JSONArray childGroup = groupObj.getJSONArray("childGroup");
        int x = 0;
        int size = childGroup == null ? 0 : childGroup.size();
        for (int i = 0; i < size; i++) {
            JSONObject obj = childGroup.getJSONObject(i);
            if (obj.getJSONArray("childGroup").size() > 0) {
                x += getSumsbGroupCHilds(obj, sum);
            } else {
                x += obj.getInteger("groupNum");
            }
        }
        return x;
    }

    private Integer getSumGroupChilds(JSONObject groupObj, Integer sum) {
        JSONArray childGroup = groupObj.getJSONArray("childGroup");
        int x = 0;
        int size = childGroup == null ? 0 : childGroup.size();
        for (int i = 0; i < size; i++) {
            JSONObject obj = childGroup.getJSONObject(i);
            x += obj.getInteger("groupNum");
//            x +=getSumGroupChilds(obj,sum);
        }
        return x;
    }

    private JSONArray putViewChildren(Integer groupLevel, int maxLevel, String groupName) {
        JSONArray childArr = new JSONArray();
        JSONObject childObj = new JSONObject();
        if (groupLevel < maxLevel) {
            childObj.put("children", putViewChildren(groupLevel + 1, maxLevel, groupName));
        } else {
            childObj.put("dataIndex", groupName);
            childObj.put("key", groupName);
        }
        childArr.add(childObj);
        return childArr;
    }

    public JSONArray getGroupTree(List<Group> groupList, Integer maxLevel) {
        Map<String, Group> groupMap = new HashMap<>();
        Map<String, Group> tmpMap = new HashMap<>();

        for (int i = 0; i < maxLevel + 1; i++) {
            for (Group group : groupList) {
                if (group.getCheckable() == null || !group.getCheckable()) {
                    continue;
                }
                int groupLevel = group.getGroupLevel();
                if (i == groupLevel) {
                    String groupId = group.getGroupId();
                    String groupParentId = group.getGroupParentId();
                    int groupNum = groupDataMapper.getPatSetAggregationCount(group.getGroupId());
                    group.setGroupNum(groupNum);
                    if (StringUtils.isNotEmpty(groupParentId) && tmpMap.containsKey(groupParentId)) {
                        tmpMap.get(group.getGroupParentId()).getChildGroup().add(group);
                        tmpMap.put(groupId, group);
                    } else {
                        groupMap.put(groupId, group);
                        tmpMap.put(groupId, group);
                    }
                }
            }
        }
        Map<String, Group> groupMap1 = new HashMap<>();
        JSONArray jsonArray = new JSONArray();
        for (Map.Entry<String, Group> map : groupMap.entrySet()) {
            Group group = map.getValue();
            if (groupMap1.containsKey(group.getGroupTypeId())) {
                groupMap1.get(group.getGroupTypeId()).getChildGroup().add(group);
            } else {
                String groupTypeName = groupTypeMapper.getGroupTypeName(group.getGroupTypeId());
                Group resultGroup = new Group(group.getGroupTypeId(), groupTypeName, groupTypeName);
                resultGroup.getChildGroup().add(group);
                groupMap1.put(group.getGroupTypeId(), resultGroup);
                if ("001".equals(resultGroup.getGroupTypeId()) || "003".equals(resultGroup.getGroupTypeId())) {
                    jsonArray.add(0, resultGroup);
                } else {

                    jsonArray.add(resultGroup);
                }
            }
        }

        return jsonArray;
    }

    public JSONArray getContResultForPatientDataByNoActiveIndex(String crfId, String projectId, String patientSnQuery, Integer pageSize, List<GroupData> patientSns) {
        JSONArray data = new JSONArray();
        String query = "select " + IndexContent.getPatientDocId(crfId) + " as patSn  from " + IndexContent.getIndexName(crfId, projectId) + " where " + patientSnQuery + " and  join_field='patient_info'";
        JSONArray source = new JSONArray().fluentAdd("patient_info");
        String resultSearch = httpUtils.querySearch(projectId, query, 1, pageSize, null, source, crfId);
        Map<String, JSONObject> resultMap;
        if (StringUtils.isEmpty(crfId) || IndexContent.EMR_CRF_ID.equals(crfId)) {
            resultMap = new KeyPath("hits", "hits")
                .resolveAsJSONArray(JSON.parseObject(resultSearch))
                .stream()
                .collect(toMap(
                    new KeyPath("_id")::resolveAsString,
                    new KeyPath("_source", IndexContent.getPatientInfo(crfId), 0)::resolveAsJSONObject));
        } else {
            resultMap = new KeyPath("hits", "hits")
                .resolveAsJSONArray(JSON.parseObject(resultSearch))
                .stream()
                .collect(toMap(
                    new KeyPath("_id")::resolveAsString,
                    new KeyPath("_source", "patient_info", 0, "patient_basicinfo", 0)::resolveAsJSONObject));
        }
        for (GroupData groupData : patientSns) {
            String docId = groupData.getPatientDocId();
            JSONObject object = resultMap.get(docId);
            object.put("GROUP_NAME", groupData.getGroupName());
            data.add(object);
        }
        return data;
    }


    abstract static class Cell {
        abstract Future execute(ExecutorService es);

        abstract Long count();

        abstract String serialize(int total);
    }

    static class DiscreteCell extends Cell {
        String redisKey;
        String projectId;
        String patsCondition;
        Set<String> patients;
        String crfId;
        RedisMapDataService redisMapDataService;
        List<ActiveSqlMap> sqlMaps;

        @Override
        Future execute(ExecutorService es) {
            return es.submit(() -> {
                for (ActiveSqlMap sqlMap : sqlMaps){
                    String newsql = "SELECT " + IndexContent.getPatientDocId(crfId) + " FROM " + sqlMap.getSqlFrom() + " WHERE (" + sqlMap.getSqlWhere() + ") AND " + patsCondition + IndexContent.getGroupBy(crfId);
                    if (StringUtils.isNotEmpty(sqlMap.getSqlHaving())) {
                        newsql = newsql + " " + sqlMap.getSqlHaving();
                    }
                    String finalNewsql = newsql;
                    String response = ApplicationContextHelper
                        .getBean(HttpUtils.class)
                        .querySearch(
                            projectId,
                            finalNewsql,
                            1,
                            Integer.MAX_VALUE - 1,
                            null,
                            null,
                            crfId,
                            true);
                    patients.addAll(
                        new KeyPath("hits", "hits", "_id")
                        .fuzzyResolve(JSON.parseObject(response))
                        .stream()
                        .map(String.class::cast)
                        .collect(toSet()));
                }
                redisMapDataService.setSets(redisKey, patients);
                redisMapDataService.setOutTime(redisKey, 7 * 24 * 60 * 60);
                LOG.info("插入 redis 缓存 成功 " + redisKey);
            });
        }

        @Override
        Long count() {
            return (long) patients.size();
        }

        @Override
        public String serialize(int total) {
            double d = (double) count() / (double) total * 100;
            BigDecimal bg = new BigDecimal(d).setScale(2, RoundingMode.UP);
//            return nf.format(d);
            return count() + " (" + String.format("%.2f", bg.doubleValue()) + "%)";
        }
    }

    static class ContinuousCell extends Cell {
        String projectId;
        String patsCondition;
        String varName;
        String crfId;
        List<ActiveSqlMap> sqlMaps;
        Long count = null;
        SummaryStatistics summary = null;
        double median = Double.NaN;
        RedisMapDataService redisMapDataService;

        String groupId;

        @Override
        Future execute(ExecutorService es) {
            String activeId = sqlMaps.get(0).getActiveIndexId();
            return es.submit(() -> {
                JSONArray arrAll = new JSONArray();
                if (redisMapDataService.exists(UqlConfig.CORT_CONT_ACTIVE_REDIS_KEY.concat(activeId + "_" + groupId))) {
                    String val = redisMapDataService.getDataBykey(UqlConfig.CORT_CONT_ACTIVE_REDIS_KEY.concat(activeId + "_" + groupId));
                    arrAll = JSONArray.parseArray(val);
                }else{
                    for (ActiveSqlMap sqlMap : sqlMaps) {
                        String newsql = null;
                        newsql = "SELECT " + sqlMap.getSqlSelect() + " FROM " + sqlMap.getSqlFrom() + " WHERE (" + sqlMap.getSqlWhere() + ") AND " + patsCondition + IndexContent.getGroupBy(crfId);
                        if (StringUtils.isNotEmpty(sqlMap.getSqlHaving())) {
                            newsql = newsql + " " + sqlMap.getSqlHaving();
                        }
                        String finalNewsql = newsql;
                        JSONObject response = JSON.parseObject(ApplicationContextHelper
                            .getBean(HttpUtils.class)
                            .querySearch(
                                projectId,
                                finalNewsql,
                                1,
                                Integer.MAX_VALUE - 1,
                                null,
                                null,
                                crfId,
                                true));
                        JSONArray arr = new KeyPath("hits", "hits", "_source", "select_field", varName).fuzzyResolve(response);
                        arrAll.add(arr);
                    }
                }
                redisMapDataService.set(UqlConfig.CORT_CONT_ACTIVE_REDIS_KEY.concat(activeId + "_" + groupId), arrAll.toJSONString());
                redisMapDataService.setOutTime(UqlConfig.CORT_CONT_ACTIVE_REDIS_KEY.concat(activeId + "_" + groupId), 7 * 24 * 60 * 60);
                LOG.info("插入 redis 缓存 成功 " + UqlConfig.CORT_CONT_ACTIVE_REDIS_KEY.concat(activeId + "_" + groupId));
                if (arrAll.isEmpty() || !arrAll.stream().allMatch(o -> o instanceof Number || o instanceof String && doublePresentable((String) o))) {
                    summary = null;
                    median = Double.NaN;
                } else {
                    summary = new SummaryStatistics();
                    double values[] = arrAll.stream()
                        .mapToDouble(o -> o instanceof Number ? ((Number) o).doubleValue() : Double.parseDouble((String) o))
                        .sorted()
                        .peek(summary::addValue)
                        .toArray();
                    median = values.length % 2 == 0
                        ? (values[values.length / 2 - 1] + values[values.length / 2]) / 2
                        : values[values.length / 2];
                }
            });
        }

        @Override
        Long count() {
            return count;
        }

        @Override
        public String serialize(int total) {
            if (summary == null) {
                return "N/A";
            }
            return fmt.format(summary.getMean()) + " ± " + fmt.format(summary.getStandardDeviation())
                + " / " + fmt.format(summary.getMin()) + " ～ " + fmt.format(summary.getMax())
                + " / " + fmt.format(median);
        }

        private static final NumberFormat fmt = force(() -> {
            NumberFormat ret = NumberFormat.getNumberInstance();
            ret.setMinimumFractionDigits(2);
            ret.setMaximumFractionDigits(2);
            ret.setRoundingMode(RoundingMode.HALF_UP);
            return ret;
        });
    }

    abstract static class Item {
        String title;
        DataType type;
        String suffix;
        long total;
        List<Cell> cells = new ArrayList<>();
        List<String> enumTitles = new ArrayList<>();
    }

    static class DiscreteItem extends Item {
        boolean isConflicted() {
            Set<String> pats = new HashSet<>();
            // noinspection SuspiciousToArrayCall
            for (DiscreteCell cell : cells.toArray(new DiscreteCell[0])) {
                for (String pat : cell.patients) {
                    if (pats.contains(pat)) {
                        return true;
                    }
                    pats.add(pat);
                }
            }
            return false;
        }
    }

    static class ContinuousItem extends Item {
    }

    static class Column {
        String title;
        List<Item> items = new ArrayList<>();
    }

    private static long[][] transpose(long m[][]) {
        long ret[][] = new long[m[0].length][m.length];
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[0].length; ++j) {
                ret[j][i] = m[i][j];
            }
        }
        return ret;
    }

    private static final NumberFormat pValueFormat = force(() -> {
        NumberFormat ret = NumberFormat.getNumberInstance();
        ret.setMinimumFractionDigits(3);
        ret.setMaximumFractionDigits(3);
        ret.setRoundingMode(RoundingMode.HALF_UP);
        return ret;
    });

}
