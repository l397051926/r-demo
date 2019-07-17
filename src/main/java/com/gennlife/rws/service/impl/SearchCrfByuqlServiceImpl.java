package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.darren.collection.Pair;
import com.gennlife.darren.collection.keypath.KeyPath;
import com.gennlife.rws.content.CommonContent;
import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.content.SeparatorContent;
import com.gennlife.rws.content.UqlConfig;
import com.gennlife.rws.dao.ActiveIndexConfigMapper;
import com.gennlife.rws.dao.ActiveIndexMapper;
import com.gennlife.rws.dao.ActiveSqlMapMapper;
import com.gennlife.rws.entity.ActiveIndex;
import com.gennlife.rws.entity.ActiveSqlMap;
import com.gennlife.rws.entity.EnumResult;
import com.gennlife.rws.entity.PatientsIdSqlMap;
import com.gennlife.rws.query.UqlQureyResult;
import com.gennlife.rws.schema.AbstractFieldAnalyzer;
import com.gennlife.rws.schema.EmrFieldAnalyzer;
import com.gennlife.rws.service.PatientSetService;
import com.gennlife.rws.service.SearchCrfByuqlService;
import com.gennlife.rws.uql.*;
import com.gennlife.rws.uqlcondition.*;
import com.gennlife.rws.util.*;
import com.gennlife.rws.vo.SearchUqlVisitSn;
import com.gennlife.rws.web.WebAPIResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collector;

import static com.gennlife.darren.collection.Pair.makePair;
import static com.gennlife.darren.controlflow.exception.Force.force;
import static com.gennlife.darren.controlflow.for_.ForeachJSON.foreachKeyPath;
import static com.gennlife.rws.query.BuildIndexCrf.PROJECT_INDEX_NAME_PREFIX;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.*;

@Service
public class SearchCrfByuqlServiceImpl implements SearchCrfByuqlService {
    private static final Logger LOG = LoggerFactory.getLogger(SearchCrfByuqlServiceImpl.class);
    @Autowired
    private HttpUtils httpUtils;
    @Autowired
    private ActiveSqlMapMapper activeSqlMapMapper;
    @Autowired
    private ActiveIndexMapper activeIndexMapper;
    @Autowired
    private ActiveIndexConfigMapper activeIndexConfigMapper;
    @Autowired
    private SearchByuqlServiceImpl searchByuqlService;
    @Autowired
    private PatientSetService patientSetService;

    public static Map<String, AbstractFieldAnalyzer> SCHEMAS = force(() -> {
        Map<String, AbstractFieldAnalyzer> ret = new HashMap<>();
        ret.put("lymphoma_release_1.0", new EmrFieldAnalyzer("/schema/rws_crf_lymphadenoma_data_schema_V2.0.7.json"));
        ret.put("lymphoma", new EmrFieldAnalyzer("/schema/rws_crf_lymphadenoma_data_schema_V2.0.7.json"));
        return ret;
    });

    @Override
    public AjaxObject searchClacIndexResultByUql(String activeId, String projectId, Integer pageSize, Integer pageNum, JSONArray basicColumns, String crfId, String groupFromId, JSONArray patientSetId, String groupId, String isVariant) throws IOException, ExecutionException, InterruptedException {
        AjaxObject ajaxObject = new AjaxObject();
        Long startMysqlTime = System.currentTimeMillis();
        List<ActiveSqlMap> sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId, groupId);
        if (sqlList == null || sqlList.size() == 0) {
            searchByuqlService.referenceCalculate(activeId, projectId, CommonContent.ACTIVE_TYPE_INDEX, UqlConfig.RESULT_ORDER_KEY.get(crfId), patientSetId, groupId, null, crfId);
            sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId, groupId);
        }
        LOG.info("指标 从mysql数据库读取时间为： " + (System.currentTimeMillis() - startMysqlTime));
        String indexResultValue = "";
        if (sqlList.size() > 0) {
            indexResultValue = sqlList.get(0).getIndexResultValue();
        }

        if (StringUtils.isNotEmpty(indexResultValue)) {//枚举类型处理
            return searchClasEnumResultByUql(activeId, sqlList, projectId, pageSize, pageNum, basicColumns, crfId, groupFromId, patientSetId, groupId, isVariant, patientSetId);
        }

        ActiveSqlMap activeSqlMap = sqlList.get(0);        //获取sql语句
        JSONArray refActiveIds = JSONArray.parseArray(activeSqlMap.getRefActiveIds());//获取 join 的ids
        int refSize = refActiveIds == null ? 0 : refActiveIds.size();
        for (int i = 0; i < refSize; i++) {
            //拼接columns
            JSONObject tmpObj = new JSONObject();
            String refActiveId = refActiveIds.getString(i);
            ActiveIndex activeIndex = activeIndexMapper.selectByPrimaryKey(refActiveId.substring(1));
            String name = activeIndex.getName();
            tmpObj.put("name", name);
            tmpObj.put("id", refActiveId);
            basicColumns.add(tmpObj);
        }
        List<String> allResutList = sqlList.stream()
            .map(x -> x.getResultDocId() == null ? new String[]{} : x.getResultDocId().split(SeparatorContent.getRegexVartivalBar()))
            .flatMap(Arrays::stream)
            .distinct()
            .collect(toList());
        List<String> resultList = PagingUtils.getPageContentForString(allResutList, pageNum, pageSize);
        String sql = TransPatientSql.getPatientDocIdSql(resultList, crfId);
        Integer total = allResutList.size(); // 计算后的总数
        Set<String> allTmpSet = new HashSet<>();
        //蛋疼的计算  pageNum pageSize
        Integer before = (pageNum - 1) * pageSize + 1;
        Integer last = pageNum * pageSize;
        JSONArray dataAll = new JSONArray();

        for (ActiveSqlMap sqlMap : sqlList) {
            if(total == 0){
                break;
            }
            Set<String> tmpSet = Arrays.stream(sqlMap.getResultDocId().split(SeparatorContent.getRegexVartivalBar())).collect(toSet());
            tmpSet.removeAll(allTmpSet);
            allTmpSet.addAll(tmpSet);
            if (allTmpSet.size() + 1 < before) {
                continue;
            }
            if (dataAll.size() >= pageSize) {
                break;
            }
            int page = 1;
            int size = last - dataAll.size() + pageNum;

            String newSql = sqlMap.getHavingSqlJoinSql(sql, crfId);
            String sourceFilter = sqlMap.getSourceFiltere();
            JSONArray source = new JSONArray().fluentAdd("patient_info");
            JSONArray sourceValue = JSONArray.parseArray(sqlMap.getSourceValue());
            int sourceSize = sourceValue == null ? 0 : sourceValue.size();
            for (int i = 0; i < sourceSize; i++) {
                String sourceVal = sourceValue.getString(i);
                source.add(sourceVal);
            }
            String result = httpUtils.querySearch(projectId, newSql, page, size, sourceFilter, source, crfId);
            JSONObject object = JSONObject.parseObject(result);

            List<String> basicColumnNames = new KeyPath("id").fuzzyResolve(basicColumns).stream().map(String.class::cast).collect(toList());

            JSONArray data = new JSONArray();

            foreachKeyPath(object, new KeyPath("hits", "hits", "_source"), pat -> {
                JSONObject basicInfo = new KeyPath("patient_info", "patient_basicinfo")
                    .flatFuzzyResolve(pat)
                    .getJSONObject(0);
                JSONObject e = new JSONObject();
                e.putAll(basicColumnNames.stream()
                    .filter(key -> basicInfo.get(key) != null)
                    .collect(toMap(Function.identity(), basicInfo::get)));
                String val = new KeyPath("select_field", "condition").tryResolve(pat).toString();
                if (val.contains(".")) {
                    try {
                        val = String.format("%.2f", Double.parseDouble(val));
                    } catch (Exception ep) {
                    }
                }
                data.add(e.fluentPut(activeId, val));
            });

            List<String> pasSn = new ArrayList<>();
            Map<String, JSONObject> dataMap = new HashMap<>();
            for (int i = 0; i < data.size(); i++) {
                JSONObject tmpObj = data.getJSONObject(i);
                pasSn.add(tmpObj.getString("PATIENT_SN"));
                dataMap.put(tmpObj.getString("PATIENT_SN"), tmpObj);
            }
            String patSnWhere = "visitinfo.PATIENT_SN " + TransPatientSql.transForExtContain(pasSn);

            for (int i = 0; i < refSize; i++) {
                //拼接column
                String refActiveId = refActiveIds.getString(i);
                List<ActiveSqlMap> patSqlList = activeSqlMapMapper.getActiveSqlMapByProjectIdAndSqlGroup(projectId, refActiveId.substring(1), groupId, sqlMap.getPatSqlGroup());
                if (patSqlList == null || patSqlList.size() == 0) {
                    searchByuqlService.referenceCalculate(refActiveId, projectId, CommonContent.ACTIVE_TYPE_INDEX, UqlConfig.RESULT_ORDER_KEY.get(crfId), patientSetId, groupId, null, crfId);
                    patSqlList = activeSqlMapMapper.getActiveSqlMapByProjectIdAndSqlGroup(projectId, refActiveId.substring(1), groupId, sqlMap.getPatSqlGroup());
                }
                ActiveSqlMap patActiveSqlMap = patSqlList.get(0);
                String patSql = patActiveSqlMap.getUncomActiveSql();
                String[] patSqls = patSql.split("where");
                String where = patSqls[1];
                String newWhere = patSnWhere + " and " + where;
                String patSnResult = httpUtils.querySearch(projectId, patSqls[0] + " where " + newWhere, 1, pageSize, sourceFilter, source, crfId);
                JSONArray tmpHits = UqlQureyResult.getHitsArray(patSnResult);
                int tmpHitsSize = tmpHits.size();
                for (int j = 0; j < tmpHitsSize; j++) {
                    String colle = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONObject("select_field").getString("condition");
                    if (colle.contains(".")) {
                        try {
                            colle = String.format("%.2f", Double.parseDouble(colle));
                        } catch (Exception e) {
                        }
                    }
                    String patSn = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0).getJSONArray("patient_basicinfo").getJSONObject(0).getString("PATIENT_SN");
                    dataMap.get(patSn).put(refActiveId, colle);
                }
                for (String key : dataMap.keySet()) {
                    JSONObject obj = dataMap.get(key);
                    if (!obj.containsKey(refActiveId)) {
                        obj.put(refActiveId, "-");
                    }
                }
            }
            dataAll.addAll(data);
        }

        Integer count = searchByuqlService.getSearchUqlAllCount(groupFromId, patientSetId, groupId, projectId);

        AjaxObject.getReallyDataValue(dataAll, basicColumns);
        ajaxObject.setCount(count);
        ajaxObject.setWebAPIResult(new WebAPIResult<Object>(pageNum, pageSize, total));
        ajaxObject.setColumns(basicColumns);
        ajaxObject.setData(dataAll);

        return ajaxObject;
    }

    @Override/*获取事件结果集*/
    public AjaxObject searchCalcResultByUql(String activeId, String projectId, JSONArray basicColumns, JSONArray visitColumns, Integer activeType, Integer pageNum, Integer pageSize, String activeResult, String crfId, String groupFromId, JSONArray patientSetId, String groupId) throws InterruptedException, IOException, ExecutionException {
        final AbstractFieldAnalyzer schema = SCHEMAS.get(crfId);
        Long startMysqlTime = System.currentTimeMillis();
        List<ActiveSqlMap> sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId, groupId);
        if (sqlList == null || sqlList.size() == 0) {
            searchByuqlService.referenceCalculate(activeId, projectId, CommonContent.ACTIVE_TYPE_INDEX, UqlConfig.RESULT_ORDER_KEY.get(crfId), patientSetId, groupId, null, crfId);
            sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId, groupId);
        }
        LOG.info("事件 从mysql数据库读取时间为： " + (System.currentTimeMillis() - startMysqlTime));
        activeResult = activeIndexConfigMapper.getActiveResult(activeId.replaceAll("_tmp", ""));

        List<String> allResutList = sqlList.stream()
            .map(x -> x.getResultDocId() == null ? new String[]{} : x.getResultDocId().split(SeparatorContent.getRegexVartivalBar()))
            .flatMap(Arrays::stream)
            .distinct()
            .collect(toList());
        List<String> resultList = PagingUtils.getPageContentForString(allResutList, pageNum, pageSize);
        String sql = TransPatientSql.getPatientDocIdSql(resultList, crfId);
        Integer total = allResutList.size(); // 计算后的总数
        Set<String> allTmpSet = new HashSet<>();
        //蛋疼的计算  pageNum pageSize
        Integer before = (pageNum - 1) * pageSize + 1;
        Integer last = pageNum * pageSize;
        JSONArray dataAll = new JSONArray();

        // --------------开始分批查找
        for (ActiveSqlMap sqlMap : sqlList) {
            if(total == 0){
                break;
            }
            Set<String> tmpSet = Arrays.stream(sqlMap.getResultDocId().split(SeparatorContent.getRegexVartivalBar())).collect(toSet());
            tmpSet.removeAll(allTmpSet);
            allTmpSet.addAll(tmpSet);
            if (allTmpSet.size() + 1 < before) {
                continue;
            }
            if (dataAll.size() >= pageSize) {
                break;
            }
            int page = 1;
            int size = last - dataAll.size() + pageNum;

            String sourceFilter = sqlMap.getSourceFiltere();
            KeyPath visitsPath = KeyPath.compile(activeResult);
            String visits = activeResult;
            String resultVisit = activeResult;
            if (schema.isPackagedGroup(visits)) {
                visits = visitsPath.removeLastAsString();
                resultVisit = visits;
            } else {
                visits = "visitinfo";
                resultVisit = ("visits".equals(visitsPath.first()) ? visitsPath.keyPathByRemovingFirst() : visitsPath).toString();
            }

            JSONArray source = new JSONArray();
            source.add("patient_info");

            /*增加 source value*/
            JSONArray sourceValue = JSONArray.parseArray(sqlList.get(0).getSourceValue());
            int sourceSize = sourceValue == null ? 0 : sourceValue.size();
            for (int i = 0; i < sourceSize; i++) {
                String sourceVal = sourceValue.getString(i);
                source.add(sourceVal);
            }
            String having = sqlMap.getSqlHaving();
            String activeReuslt = sqlMap.getActiveResultDocId();
            String allSql = "select " + activeReuslt + "as condition ,count(" + visits + ".DOC_ID) as jocount from " + sqlMap.getSqlFrom() + " where " + sqlMap.getUncomSqlWhere() + " and " + visits + ".DOC_ID is not null " + IndexContent.getGroupBy(crfId) + " " + having;
            String result = httpUtils.querySearch(projectId, allSql, pageNum, pageSize, sourceFilter, source, crfId);
            /*处理结果*/
            JSONArray data = UqlQureyResult.getActiveVisitSn(result, activeId);

            String query = getVisitSns(data);
            /*组装新的 uql搜索 搜索新的数据*/
            UqlClass uqlClass = new StandardUqlClass();
            uqlClass.setFrom(projectId, crfId);
            JSONArray array = new JSONArray();
            array = getSource(basicColumns, "patient_info.patient_basicinfo", array, schema);
            /*处理病案首页 手术问题*/

            String repeaceActive = resultVisit;
            array = getSource(visitColumns, repeaceActive, array, schema);
            List<String> selectList = new LinkedList<>();
            for (int i = 0; i < array.size(); i++) {
                selectList.add(array.getString(i));
            }
            selectList.add("visitinfo.DOC_ID");
            selectList.add("visitinfo.PATIENT_SN");
            selectList.add("patient_info.patient_basicinfo.DOC_ID");
            uqlClass.setActiveSelect(String.join(",", selectList));
            if (StringUtils.isEmpty(uqlClass.getWhere())) {
                uqlClass.setWhere(visits + ".DOC_ID in (" + query + ")");
            } else {
                uqlClass.setWhere(uqlClass.getWhere() + " and " + visits + ".DOC_ID in (" + query + ")");
            }
            /*查询docId*/
            JSONArray resultSource = new JSONArray();
            String resultJson = httpUtils.querySearch(projectId, uqlClass.getVisitsSql(), 1, Integer.MAX_VALUE, null, resultSource, crfId);
            JSONArray dataObj = getActiveResultData(resultJson, basicColumns, visitColumns, repeaceActive, result, repeaceActive);
            dataAll.addAll(dataObj);
        }

        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
        Integer count = searchByuqlService.getSearchUqlAllCount(groupFromId, patientSetId, groupId, projectId);
        ajaxObject.setCount(count);
        ajaxObject.setData(dataAll);
        WebAPIResult webAPIResult = new WebAPIResult(pageNum, pageSize, total);
        ajaxObject.setWebAPIResult(webAPIResult);
        return ajaxObject;
    }

    private JSONArray getActiveResultData(String resultJson, JSONArray basicColumns, JSONArray visitColumns, String key, String result, String visitsKey) {

        JSONArray resultHits = UqlQureyResult.getHitsArray(result);
        JSONArray hits = UqlQureyResult.getHitsArray(resultJson);
        JSONArray data = new JSONArray();
        int size = hits == null ? 0 : hits.size();
        int resultSize = resultHits == null ? 0 : resultHits.size();
        Map<String, JSONObject> patMap = new ConcurrentHashMap<>();
        Map<String, JSONObject> resultMap = new ConcurrentHashMap<>();
        Set<String> visSet = new HashSet<>();

        for (int i = 0; i < resultSize; i++) {
            String dockId = new KeyPath(i, "_source", "select_field", "condition").tryResolveAsString(resultHits);
            JSONObject patientInfo = new KeyPath(i, "_source", "patient_info", 0, "patient_basicinfo", 0).tryResolveAsJSONObject(resultHits);
            String patSn = patientInfo.getString("PATIENT_SN");
            /*处理结果*/
            JSONArray patientIn = transforActiveData(patientInfo, basicColumns);
            if (patMap.containsKey(patSn)) {
                JSONObject object = resultMap.get(dockId);
                object.put("patient_inf", patientIn);
            } else {
                JSONObject object = new JSONObject();
                object.put("patient_inf", patientIn);
                patMap.put(patSn, object);
            }
        }
        for (int i = 0; i < size; i++) {
            String docId = hits.getJSONObject(i).getString("_id");
            Object visitObj = hits.getJSONObject(i).getJSONObject("_source").get("visitinfo");
            String visitSn = "";
            String patSn = "";
            String inspectionSn = "";
            if (visitObj instanceof JSONArray) {
                visitSn = ((JSONArray) visitObj).getJSONObject(0).getString("DOC_ID");
                patSn = ((JSONArray) visitObj).getJSONObject(0).getString("PATIENT_SN");
            } else if (visitObj instanceof JSONObject) {
                visitSn = ((JSONObject) visitObj).getString("DOC_ID");
                patSn = ((JSONObject) visitObj).getString("PATIENT_SN");
            }
            Object visitKeyObj = KeyPath.compile(visitsKey).fuzzyResolve(hits.getJSONObject(i).getJSONObject("_source"));

            if (visitKeyObj instanceof JSONArray) {
                patSn = StringUtils.isNotEmpty(patSn) ? patSn : ((JSONArray) visitKeyObj).getJSONObject(0).getString("PATIENT_SN");
//                inspectionSn = ((JSONArray) visitKeyObj).getJSONObject(0).getString("INSPECTION_SN");
            } else if (visitKeyObj instanceof JSONObject) {
                patSn = StringUtils.isNotEmpty(patSn) ? patSn : ((JSONObject) visitKeyObj).getString("PATIENT_SN");
//                inspectionSn = ((JSONObject) visitKeyObj).getString("INSPECTION_SN");
            }
            Object visitsObj = KeyPath.compile(key).fuzzyResolve(hits.getJSONObject(i).getJSONObject("_source"));

            JSONObject visits = getActieResultVisitObj(visitsObj);
            if (visits == null) continue;
            if ("visitinfo".equals(key)) {
                docId = visitSn;
            }

            if (visSet.contains(docId)) {
                continue;
            }
            visSet.add(docId);
            visits.put("VISIT_SN", visitSn);
            /*处理结果*/
            JSONArray visit = transforActiveData(visits, visitColumns);
            visits.put("VISIT_SN", visitSn);

            if (patMap.containsKey(patSn)) {
                JSONObject object = patMap.get(patSn);
                if (!object.containsKey("visit_info")) {
                    JSONObject visitInfo = new JSONObject();
                    visitInfo.put("summarize", visit);
                    visitInfo.put("detail", new JSONArray());
                    object.put("visit_info", visitInfo);
                    patMap.put(patSn, object);
                } else {
                    JSONArray array = object.getJSONObject("visit_info").getJSONArray("summarize");
                    visit.forEach(x -> array.add(x));
                }
            } else {
                JSONObject object = new JSONObject();
                JSONObject visitInfo = new JSONObject();
                visitInfo.put("summarize", visit);
                visitInfo.put("detail", new JSONArray());
                object.put("visit_info", visitInfo);
                patMap.put(patSn, object);
            }
        }
        for (String keyTmp : patMap.keySet()) {
            JSONObject object = patMap.get(keyTmp);
            if (!object.containsKey("visit_info")) {
                JSONObject visitInfo = new JSONObject();
                JSONArray sumArray = new JSONArray();
                for (int i = 0; i < visitColumns.size(); i++) {
                    JSONObject sumObj = new JSONObject();
                    JSONObject tmpObj = visitColumns.getJSONObject(i);
                    sumObj.fluentPut("name", tmpObj.getString("id"))
                        .fluentPut("name_des", tmpObj.getString("name"))
                        .fluentPut("value", "-");
                    sumArray.add(sumObj);
                }
                visitInfo.put("summarize", sumArray);
                visitInfo.put("detail", new JSONArray());
                object.put("visit_info", visitInfo);
            }
            data.add(object);
        }
        return data;
    }

    private JSONObject getActieResultVisitObj(Object visitsObj) {
        if (visitsObj instanceof JSONArray && ((JSONArray) visitsObj).size() > 0) {
            return getActieResultVisitObj(((JSONArray) visitsObj).get(0));
        } else if (visitsObj instanceof JSONObject) {
            return (JSONObject) visitsObj;
        } else {
            return null;
        }
    }

    private JSONArray transforActiveData(JSONObject result, JSONArray showColumns) {
        JSONArray data = new JSONArray();
        int size = showColumns == null ? 0 : showColumns.size();
        for (int i = 0; i < size; i++) {
            JSONObject resObj = new JSONObject();
            JSONObject obj = showColumns.getJSONObject(i);
            String id = obj.getString("id");
            String name = obj.getString("name");
            String dmp = null;
            if (result.get(id) instanceof JSONArray) {
                JSONArray array = result.getJSONArray(id);
                dmp = array.toJavaList(String.class).stream().collect(joining(" ; "));
            } else {
                dmp = result.getString(id);
            }
            if (StringUtils.isEmpty(dmp)) {
                dmp = "-";
            }
            resObj.put("name", id);
            resObj.put("name_des", name);
            resObj.put("value", dmp);
            data.add(resObj);
        }

        return data;
    }

    private String getVisitSns(JSONArray data) {
        int size = data == null ? 0 : data.size();
        Set<String> visSet = new HashSet<>();
        for (int i = 0; i < size; i++) {
            String vis = data.getString(i);
            String[] visArra = vis.split(",");
            for (int j = 0; j < visArra.length; j++) {
                String visTmp = visArra[j];
                if (visSet.contains(visTmp)) continue;
                visSet.add(visTmp);
            }
        }
        return visSet.stream().map(x -> "'" + x + "'").collect(joining(","));
    }

    @Override
    public AjaxObject searchCalcExculeByUql(String activeId, String projectId, Integer pageSize, Integer pageNum, JSONArray basicColumns, String crfId, String isExport, String groupId, String groupName, JSONArray patientSetId, String createId, String createName, String groupFromId, boolean autoExport) throws IOException, ExecutionException, InterruptedException {
        AjaxObject ajaxObject = new AjaxObject();
        Long startMysqlTime = System.currentTimeMillis();
        List<ActiveSqlMap> sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId, groupId);
        if (sqlList == null || sqlList.size() == 0) {
            searchByuqlService.referenceCalculate(activeId, projectId, CommonContent.ACTIVE_TYPE_INDEX, UqlConfig.RESULT_ORDER_KEY.get(crfId), patientSetId, groupId, null, crfId);
            sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId, groupId);
        }
        LOG.info("那排 从mysql数据库读取时间为： " + (System.currentTimeMillis() - startMysqlTime));
        ActiveSqlMap activeSqlMap = sqlList.get(0);
        JSONArray refActiveIds = JSONArray.parseArray(activeSqlMap.getRefActiveIds());
        int refSize = refActiveIds == null ? 0 : refActiveIds.size();

        for (int i = 0; i < refSize; i++) {
            //拼接columns
            JSONObject tmpObj = new JSONObject();
            String refActiveId = refActiveIds.getString(i);
            ActiveIndex activeIndex = activeIndexMapper.selectByPrimaryKey(refActiveId);
            String name = activeIndex.getName();
            tmpObj.put("name", name);
            tmpObj.put("id", refActiveId);
            basicColumns.add(tmpObj);
        }


        JSONArray source = new JSONArray().fluentAdd("patient_info.patient_basicinfo");
        List<String> allResutList = sqlList.stream()
            .map(x -> x.getResultDocId() == null ? new String[]{} : x.getResultDocId().split(SeparatorContent.getRegexVartivalBar()))
            .flatMap(Arrays::stream)
            .distinct()
            .collect(toList());

        if ("1".equals(isExport)) {//处理导出数据
            return searchByuqlService.exportToGroup(groupId, allResutList, sqlList, projectId, pageNum, crfId, activeId, refActiveIds, patientSetId, groupName, createId, createName, autoExport);
        }

        List<String> resultList = PagingUtils.getPageContentForString(allResutList, pageNum, pageSize);
        String joinSql = TransPatientSql.getPatientDocIdSql(resultList, crfId);
        Integer total = allResutList.size(); // 计算后的总数
        Set<String> allTmpSet = new HashSet<>();
        //蛋疼的计算  pageNum pageSize
        Integer before = (pageNum - 1) * pageSize + 1;
        Integer last = pageNum * pageSize;
        JSONArray dataAll = new JSONArray();

        for (ActiveSqlMap sqlMap : sqlList) {
            if(total == 0){
                break;
            }
            Set<String> tmpSet = Arrays.stream(sqlMap.getResultDocId().split(SeparatorContent.getRegexVartivalBar())).collect(toSet());
            tmpSet.removeAll(allTmpSet);
            allTmpSet.addAll(tmpSet);
            if (allTmpSet.size() + 1 < before) {
                continue;
            }
            if (dataAll.size() >= pageSize) {
                break;
            }
            int page = 1;
            int size = last - dataAll.size() + pageNum;

            //请求 获取数据
            String newSql = sqlMap.getSqlJoinSql(joinSql, crfId);
            String result = httpUtils.querySearch(projectId, newSql, page, size, activeSqlMap.getSourceFiltere(), source, crfId);

            //处理结果
            JSONArray data = UqlQureyResult.getResultData(result, activeId, refActiveIds, true, crfId);
            List<String> pasSn = new ArrayList<>();
            Map<String, JSONObject> dataMap = new HashMap<>();
            for (int i = 0; i < data.size(); i++) {
                JSONObject tmpObj = data.getJSONObject(i);
                pasSn.add(tmpObj.getString("DOC_ID"));
                dataMap.put(tmpObj.getString("DOC_ID"), tmpObj);
            }
            String patSnWhere = "patient_info.patient_basicinfo.DOC_ID " + TransPatientSql.transForExtContain(pasSn);


            for (int i = 0; i < refSize; i++) {
                //拼接column
                String refActiveId = refActiveIds.getString(i);
                List<ActiveSqlMap> patSqlList = activeSqlMapMapper.getActiveSqlBySqlGroup(refActiveId, groupId, sqlMap.getPatSqlGroup());
                if (patSqlList == null || patSqlList.size() == 0) {
                    searchByuqlService.referenceCalculate(refActiveId, projectId, CommonContent.ACTIVE_TYPE_INDEX, UqlConfig.RESULT_ORDER_KEY.get(crfId), patientSetId, groupId, null, crfId);
                    patSqlList = activeSqlMapMapper.getActiveSqlBySqlGroup(refActiveId, groupId, sqlMap.getPatSqlGroup());
                }
                ActiveSqlMap patActiveSqlMap = patSqlList.get(0);
                String indexResultValue = patActiveSqlMap.getIndexResultValue();
                if (StringUtils.isNotEmpty(indexResultValue)) {//枚举
                    makeEnumResultData(patSqlList, patSnWhere, dataMap, projectId, pageSize, source, refActiveId, crfId);
                } else if ("1".equals(patActiveSqlMap.getActiveType())) {//事件
                    String patSql = patActiveSqlMap.getUncomActiveSql();
                    String[] patSqls = patSql.split("where");
                    String where = patActiveSqlMap.getUncomSqlWhere() + IndexContent.getGroupBy(crfId);//patActiveSqlMap.getEventWhere() +"group by visitinfo.PATIENT_SN";// +" and join_field='visitinfo' ";
                    String newWhere = patSnWhere + " and " + where;
                    String patSnResult = httpUtils.querySearch(projectId, patSqls[0] + " where " + newWhere, 1, pageSize, "", source, crfId);
                    JSONArray tmpHits = UqlQureyResult.getHitsArray(patSnResult);
                    String activeResultDoc = patActiveSqlMap.getActiveResultDocId();
                    boolean isFirst = activeResultDoc.startsWith("first") || activeResultDoc.startsWith("any") || activeResultDoc.startsWith("last");
                    int tmpHitsSize = tmpHits.size();
                    for (int j = 0; j < tmpHitsSize; j++) {
                        Long colle = new KeyPath(j, "_source", "select_field", "jocount").resolveAsLong(tmpHits);
                        String patSn = new KeyPath(j, "_source", "patient_info", 0, "patient_basicinfo", 0, "DOC_ID").resolveAsString(tmpHits);
                        if (isFirst) {
                            dataMap.get(patSn).put(refActiveId, 1);
                        } else {
                            dataMap.get(patSn).put(refActiveId, colle);
                        }
                    }
                    for (String key : dataMap.keySet()) {
                        JSONObject obj = dataMap.get(key);
                        if (!obj.containsKey(refActiveId)) {
                            obj.put(refActiveId, 0);
                        }
                    }
                } else {//指标
                    String patSql = patActiveSqlMap.getUncomActiveSql();
                    String[] patSqls = patSql.split("where");
                    String where = patSqls[1];
                    String newWhere = patSnWhere + " and " + where;
                    String patSnResult = httpUtils.querySearch(projectId, patSqls[0] + " where " + newWhere, 1, pageSize, "", source, crfId);
                    JSONArray tmpHits = UqlQureyResult.getHitsArray(patSnResult);
                    int tmpHitsSize = tmpHits.size();
                    for (int j = 0; j < tmpHitsSize; j++) {
                        Object colle = new KeyPath(j, "_source", "select_field", "condition").resolve(tmpHits);
                        String patSn = new KeyPath(j, "_source", "patient_info", 0, "patient_basicinfo", 0, "DOC_ID").resolveAsString(tmpHits);
                        dataMap.get(patSn).put(refActiveId, colle);
                    }
                    for (String key : dataMap.keySet()) {
                        JSONObject obj = dataMap.get(key);
                        if (!obj.containsKey(refActiveId)) {
                            obj.put(refActiveId, "-");
                        }
                    }
                }
            }
            dataAll.addAll(data);
        }

        //获取总共人数
        Integer count = searchByuqlService.getSearchUqlAllCount(groupFromId, patientSetId, groupId, projectId);
        AjaxObject.getReallyDataValue(dataAll, basicColumns);
//        ajaxObject.setApplyOutCondition(applyCondition);
        ajaxObject.setCount(count);
        ajaxObject.setWebAPIResult(new WebAPIResult<Object>(pageNum, pageSize, total));
        ajaxObject.setColumns(basicColumns);
        ajaxObject.setData(dataAll);

        return ajaxObject;

    }

    private void makeEnumResultData(List<ActiveSqlMap> patSqlList, String patSnWhere, Map<String, JSONObject> dataMap, String projectId, Integer pageSize, JSONArray source, String refActiveId, String crfId) throws IOException {//处理枚举
        String isOtherName = "";
        for (ActiveSqlMap activeSqlMap : patSqlList) {
            Integer isOther = activeSqlMap.getIsOther();
            if (isOther != null && isOther == 1) {
                isOtherName = activeSqlMap.getIndexResultValue();
            }
        }
        Map<String, EnumResult> map = new HashMap<>();
        for (ActiveSqlMap activeSqlMap : patSqlList) {
            String patSql = activeSqlMap.getUncomActiveSql();
            String where = activeSqlMap.getUncomSqlWhere();
            String newWhere = patSnWhere + " and (" + where + ")";
            activeSqlMap.setUncomSqlWhere(newWhere);
            String sql = activeSqlMap.getUql(crfId);
            String patSnResult = httpUtils.querySearch(projectId, sql, 1, pageSize, "", source, crfId);
            JSONArray tmpHits = UqlQureyResult.getHitsArray(patSnResult);
            int tmpHitsSize = tmpHits.size();
            String indexResultValue = activeSqlMap.getIndexResultValue();
            for (int j = 0; j < tmpHitsSize; j++) {
                String patSn = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0).getJSONArray("patient_basicinfo").getJSONObject(0).getString("DOC_ID");
                ;
                JSONObject obj = dataMap.get(patSn);
                if (!map.containsKey(patSn)) {
                    EnumResult enumResult = new EnumResult();
                    map.put(patSn, enumResult);
                }
                if (StringUtils.isNotEmpty(indexResultValue) && indexResultValue.equals(isOtherName)) {
                    map.get(patSn).repleaceAdd(indexResultValue);
                }
                if (!map.get(patSn).contain(isOtherName)) {
                    map.get(patSn).add(indexResultValue);
                }

            }
        }
        for (Map.Entry<String, EnumResult> entry : map.entrySet()) {
            JSONObject obj = dataMap.get(entry.getKey());
            obj.put(refActiveId, entry.getValue().toString());
        }
    }

    @Override
    public String searchByActive(JSONObject object, String resultOrderKey, Integer isSearch, PatientsIdSqlMap patientsIdSqlMap, String crfId) throws ExecutionException, InterruptedException, IOException {
        final AbstractFieldAnalyzer schema = SCHEMAS.get(crfId);
        UqlClass uqlClass = null;
        String patientSql = TransPatientSql.getAllPatientSql(patientsIdSqlMap.getPatientSnIds(), crfId);
        String activeType = object.getString("activeType");
        JSONArray patientSetId = object.getJSONArray("patientSetId");
        String isVariant = object.getString("isVariant");
        String groupToId = object.getString("groupToId");
        groupToId = StringUtils.isEmpty(groupToId) ? UqlConfig.CORT_INDEX_ID : groupToId;
        //获取初筛 sql
        String projectId = object.getString("projectId");

        JSONObject config = object.getJSONArray("config").getJSONObject(0);
        String activeIndexId = config.getString("activeIndexId");//指标id
        String R_activeIndexId = isSearch == CommonContent.ACTIVE_TYPE_TEMP ? activeIndexId.concat("_tmp") : activeIndexId;
        String T_activeIndexId = "t".concat(activeIndexId);

        String indexType = config.getString("indexType");
        JSONArray contitions = config.getJSONArray("conditions");
        String indexDate = "visitinfo.ADMISSION_DATE";
        String order1 = null;
        //事件
        String activeResult = config.getString("activeResult");
        String operator = config.getString("operator");
        String operaotrNum = config.getString("operatorNum");
        KeyPath visitsPath = KeyPath.compile(activeResult);
        String visits = activeResult;
        if (schema.isPackagedGroup(visits)) {
            visits = visitsPath.removeLastAsString();
        } else {
            visits = "visitinfo";
        }

        String activeResultDocId = "";
        //事件处理
        if (StringUtils.isNotEmpty(activeResult)) {
            uqlClass = new CrfActiveUqlClass(projectId, crfId);
            JSONObject orderKeyObjet = JSONObject.parseObject(resultOrderKey);
            String orderKey = disposeVisits(orderKeyObjet.getString(activeResult));
            if (schema.isPackagedField(orderKey)) {
                orderKey = KeyPath.compile(orderKey).removeLast(2).stream().map(Object::toString).collect(joining("."));
            }
            String order = FunctionUtilMap.getUqlFunction(operator, operaotrNum, "visitinfo.DOC_ID", indexType, orderKey);
            activeResultDocId = FunctionUtilMap.getUqlFunction(operator, operaotrNum, visits + ".DOC_ID", indexType, orderKey);
            uqlClass.setSelect(uqlClass.getSelect() + order);
            order1 = orderKey;
            if (schema.isPackagedField(order1)) {
                order1 = KeyPath.compile(order1).removeLast(2).stream().map(Object::toString).collect(joining("."));
            }
            uqlClass.setSqlHaving(operaotrNum);
            uqlClass.setResultValue("visitinfo.DOC_ID");
            uqlClass.setActiveSelect(uqlClass.getSelect());
            uqlClass.setResultFunction(operator);
            uqlClass.setResultFunctionNum(operaotrNum);
        }
        UqlWhere where = new UqlWhere();

        String hasCount = getActiveHasCount(visits);
        uqlClass.setActiveSelect(uqlClass.getSelect() + hasCount);

        JSONObject contitionObj = contitions.getJSONObject(0);
        transforEnumCondition(contitionObj, uqlClass, where, T_activeIndexId, SCHEMAS.get(crfId), crfId, groupToId, projectId, patientSetId, patientSql, patientsIdSqlMap.getId());

        UqlClass sqlresult = null;
        String sqlMd5 = "";
        String allWhere = "";
        String eventWhere = "";
        String tmpVisits = order1.substring(0, order1.lastIndexOf("."));
        uqlClass.setVistSnWhere(where, tmpVisits, order1, schema);
        uqlClass.setWhereIsEmpty(where, "visitinfo.DOC_ID", null, null, schema);
        uqlClass.setNotAllWhere(operator, order1, null, null);
        uqlClass.setInitialPatients(isVariant, patientSql);
        where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
        uqlClass.setWhere(uqlClass.getWhereNotNull() + "(" + where.toString() + " ) ");
        boolean isGetVisisn = !where.isEmpty();
        String sqlNew = uqlClass.getHavingSql();
        /*开始搞 MD5 替换sql*/
        sqlMd5 = StringToMd5.stringToMd5(sqlNew);
        Integer sqlMd5count = activeSqlMapMapper.getCountByActiveAndsqlMd5(R_activeIndexId, sqlMd5, groupToId);
        if (sqlMd5count > 0) return null;

        allWhere = uqlClass.getWhere();
        //构造新sql 只有 visitSn 搜错功能
        if (StringUtils.isNotEmpty(activeResult) && isGetVisisn) {
            sqlresult = getIndexSql(uqlClass, operator, operaotrNum, activeResult, indexType, indexDate, projectId, hasCount, crfId);
        } else {
            sqlresult = uqlClass;
        }

        eventWhere = sqlresult.getWhere().contains("join_field") ? sqlresult.getWhere() : sqlresult.getWhere() + " and join_field='visitinfo'";
        if (!"all".equals(operator)) {
            if ("visitinfo".equals(visits)) {
                if (order1.startsWith("visitinfo")) {
                    sqlresult.setWhere(sqlresult.getNotEmptyWhere() + " AND " + order1 + " IS NOT NULL ");
                } else {
                    sqlresult.setWhere(sqlresult.getNotEmptyWhere() + " AND " + " haschild( " + order1 + " IS NOT NULL ) ");
                }
            } else {
                sqlresult.setWhere(sqlresult.getNotEmptyWhere() + " AND " + order1 + " IS NOT NULL");
            }
        }

        String andGroupCondition = getActiveGroupCondition(where, visits);
        if (StringUtils.isNotEmpty(andGroupCondition)) {
            sqlresult.setWhere(sqlresult.getWhere() + " and (" + andGroupCondition + ")");
        }
        sqlresult.setWhere(sqlresult.getWhere() + " AND join_field = '" + visits + "'");

        sqlresult.setSqlHaving(operaotrNum);
        String newSql = sqlresult.getHavingSql(crfId);

        Set<String> patients = getProjectPatients(projectId, patientSql, crfId);

        String allSql = "select patient_info.patient_basicinfo.DOC_ID from " + uqlClass.getFrom() + " where " + allWhere + " group by patient_info.patient_basicinfo.DOC_ID";
        String activeOtherPat = httpUtils.querySearch(projectId, allSql, 1, Integer.MAX_VALUE - 1, null, new JSONArray(), crfId);
//        Set<String> patients= getProjectPatients(projectId, crfId);
        Set<String> allPats = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(JSON.parseObject(activeOtherPat))
            .stream()
            .map(String.class::cast)
            .collect(toSet());
        String resultDocId = String.join(SeparatorContent.VERTIVAL_BAR, allPats);
        patients.removeAll(allPats);
        String otherResult = "";
        if (patients.isEmpty()) {
            otherResult = "";
        } else {
            otherResult = String.join("$", patients);
        }
        ActiveSqlMap activeSqlMap = new ActiveSqlMap(projectId, R_activeIndexId, GzipUtil.compress(newSql),
            sqlresult.getSelect(), sqlresult.getFrom(), uqlClass.getSourceFilter(), activeType,
            uqlClass.getActiveId().toJSONString(), JSON.toJSONString(uqlClass.getSource()), activeResultDocId, activeResult,
            sqlMd5, GzipUtil.compress(eventWhere), hasCount.split(",")[1], GzipUtil.compress(otherResult), hasCount.split(",")[1]);

        activeSqlMap.setGroupId(StringUtils.isEmpty(groupToId) ? UqlConfig.CORT_INDEX_ID : groupToId);
        activeSqlMap.setUncomSqlWhere(sqlresult.getWhere());
        activeSqlMap.setSqlHaving(uqlClass.getHaving());
        activeSqlMap.setPatSqlGroup(patientsIdSqlMap.getId());
        activeSqlMap.setResultDocId(resultDocId);
        activeSqlMapMapper.insert(activeSqlMap);
        return sqlresult.getCrfSql();
    }

    private String getActiveGroupCondition(UqlWhere where, String activeResult) {
        StringBuffer andGroupCondition = new StringBuffer();
        for (UqlWhereElem elem : where) {
            String s = elem.toString().trim();
            if (elem instanceof LiteralUqlWhereElem) {
                andGroupCondition.append(" " + s.replaceAll("haschild", "") + " ");
            } else if (elem instanceof SimpleConditionUqlWhereElem || elem instanceof ReferenceConditionUqlWhereElem) {
                if (s.startsWith(activeResult)) {
                    andGroupCondition.append(" " + s + " ");
                } else if (s.startsWith("haschild(" + activeResult)) {
                    andGroupCondition.append(" " + s.substring(0, s.length() - 1).replaceAll("haschild\\(", " "));
                } else {
                    andGroupCondition.append("visitinfo.DOC_ID IS NOT NULL");
                }
            }
        }
        return andGroupCondition.toString();
    }

    private String getActiveHasCount(String visits) {
        return " ,count(" + visits + ".DOC_ID) as jocount ";
    }

    @Override
    public String SearchByEnume(JSONObject obj, String resultOrderKey, Integer isSearch, PatientsIdSqlMap patientsIdSqlMap, String crfId) throws ExecutionException, InterruptedException, IOException {
        final AbstractFieldAnalyzer schema = SCHEMAS.get(crfId);
        UqlClass uqlClass = null;
        String patientSql = TransPatientSql.getAllPatientSql(patientsIdSqlMap.getPatientSnIds(), crfId);
        String projectId = obj.getString("projectId");
        String isVariant = obj.getString("isVariant");
        JSONArray configs = obj.getJSONArray("config");
        String groupToId = obj.getString("groupToId");
        if (UqlConfig.CORT_INDEX_ID.equals(groupToId)) {
            isVariant = "1";
        }
        groupToId = StringUtils.isEmpty(groupToId) ? UqlConfig.CORT_INDEX_ID : groupToId;
        String name = obj.getString("name");
        JSONArray patientSetId = obj.getJSONArray("patientSetId");
        String activeIndexId = configs.getJSONObject(0).getString("activeIndexId");//指标id

        String R_activeIndexId = isSearch == CommonContent.ACTIVE_TYPE_TEMP ? activeIndexId.concat("_tmp") : activeIndexId;
        String T_activeIndexId = "t" + activeIndexId;

        JSONObject resultObj = JSONObject.parseObject(resultOrderKey);

        int size = configs == null ? 0 : configs.size();

        List<ActiveSqlMap> activeSqlMaps = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            uqlClass = new CrfEnumUqlClass(projectId, crfId);
            uqlClass.setActiveSelect(" patient_info.patient_basicinfo.DOC_ID as pSn  ");
            JSONObject config = configs.getJSONObject(i);

            String indexType = config.getString("indexType");
            JSONArray contitions = config.getJSONArray("conditions");
            if (contitions == null || contitions.size() == 0) {
                continue;
            }
            String indexDate = resultObj.getString("visits.visitinfo");
            //普通指标
            String indexColumn = disposeVisits(config.getString("indexColumn"));
            String function = config.getString("function");
            String indexTypeValue = DataType.fromString(indexType).name();
            String indexResultValue = config.getString("indexResultValue");
            String functionParam = config.getString("functionParam");

            KeyPath indexDatePath = KeyPath.compile(indexDate);
            if (schema.isPackagedField(indexDate)) {
                indexDate = indexDatePath.removeLast(2).stream().map(Object::toString).collect(joining("."));
            } else {
                indexDatePath = "visits".equals(indexDatePath.getFirst()) ? indexDatePath.keyPathByRemovingFirst() : indexDatePath;
                indexDate = indexDatePath.stream().map(Object::toString).collect(joining("."));
            }

            //指标处理
            UqlWhere where = new UqlWhere();
            JSONObject contitionObj = contitions.getJSONObject(0);

            transforEnumCondition(contitionObj, uqlClass, where, T_activeIndexId, SCHEMAS.get(crfId), crfId, groupToId, projectId, patientSetId, patientSql, patientsIdSqlMap.getId());
            uqlClass.setWhereIsEmpty(where, null, isVariant, patientSql, schema);

            String hasCount = " ,count(visitinfo.DOC_ID) as jocount ";
            uqlClass.setActiveSelect(uqlClass.getSelect() + hasCount);

            where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
            uqlClass.setWhere(uqlClass.getWhereNotNull() + " ( " + where.toString() + " ) ");

            UqlClass sqlresult = null;
            if (StringUtils.isNotEmpty(indexColumn)) {
                sqlresult = getIndexSql(uqlClass, function, functionParam, indexColumn, indexType, indexDate, projectId, hasCount, crfId);
            } else {
                sqlresult = uqlClass;
            }
            String newSql = sqlresult.getCrfSql();
            ActiveSqlMap activeSqlMap = new ActiveSqlMap(projectId, R_activeIndexId, GzipUtil.compress(newSql), sqlresult.getSelect(),
                sqlresult.getFrom(), uqlClass.getSourceFilter(), uqlClass.getActiveId().toJSONString(),
                JSON.toJSONString(uqlClass.getSource()), indexResultValue, indexTypeValue, "patient_info.patient_basicinfo.DOC_ID", name);

            activeSqlMap.setUncomSqlWhere(sqlresult.getWhere());
            activeSqlMap.setGroupId(StringUtils.isEmpty(groupToId) ? UqlConfig.CORT_INDEX_ID : groupToId);
            activeSqlMaps.add(activeSqlMap);
            activeSqlMap.setPatSqlGroup(patientsIdSqlMap.getId());
            Long mysqlStartTime = System.currentTimeMillis();
            activeSqlMapMapper.insert(activeSqlMap);
            LOG.info("数据库用时 :  " + (System.currentTimeMillis() - mysqlStartTime));
        }
        if (StringUtils.isEmpty(groupToId) || UqlConfig.CORT_INDEX_ID.equals(groupToId)) {
            SingleExecutorService.getInstance().getFlushCountGroupExecutor().submit(() -> {
                try {
                    searchByuqlService.saveEnumCortrastiveResultRedisMap(activeSqlMaps, projectId, "EMR", R_activeIndexId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        return uqlClass.getCrfSql();
    }

    public Set<String> getProjectPatients(String projectId, String patientSql, String crfId) {
        String sql = "select  patient_info.patient_basicinfo.DOC_ID as pSn from " + PROJECT_INDEX_NAME_PREFIX.get(crfId) + projectId + " where " + patientSql + " group by patient_info.patient_basicinfo.DOC_ID";
        JSONArray source = new JSONArray();
        String result = httpUtils.querySearch(projectId, sql, 1, Integer.MAX_VALUE - 1, null, source, crfId);
        Set<String> patients = new KeyPath("hits", "hits")
            .flatFuzzyResolve(JSON.parseObject(result))
            .stream()
            .map(JSONObject.class::cast)
            .flatMap(o -> new KeyPath("_id").fuzzyResolve(o).stream())
            .map(String.class::cast)
            .collect(toSet());
        return patients;
    }

    private void transforEnumCondition(JSONObject contitionObj, UqlClass uqlClass, UqlWhere where, String activeIndexId,
                                       AbstractFieldAnalyzer schema, String crfId, String groupId, String projectId, JSONArray patientSetId, String patientSql, Integer sqlGroupId) throws IOException, ExecutionException, InterruptedException {
        String operatorSign = contitionObj.getString("operatorSign");
        JSONArray details = contitionObj.getJSONArray("details");
        JSONArray inner = contitionObj.getJSONArray("inner");
        JSONObject sortObj = sortForDetailAndInner(details, inner);
        details = sortObj.getJSONArray("detail");
        inner = sortObj.getJSONArray("inner");
        transforEnumDetails(details, uqlClass, operatorSign, where, activeIndexId, schema, crfId, groupId, projectId, patientSetId, patientSql, sqlGroupId);
        int innerSize = inner == null ? 0 : inner.size();
        for (int i = 0; i < innerSize; i++) {
            if (where.needsOperator()) {
                where.addElem(new LiteralUqlWhereElem(operatorSign));
            }
            where.addElem(new LiteralUqlWhereElem("("));
            JSONObject tmpObj = inner.getJSONObject(i);
            transforEnumCondition(tmpObj, uqlClass, where, activeIndexId, schema, crfId, groupId, projectId, patientSetId, patientSql, sqlGroupId);
            where.addElem(new LiteralUqlWhereElem(")"));
        }
    }

    private JSONObject sortForDetailAndInner(JSONArray details, JSONArray inner) {
        JSONArray newDetails = new JSONArray();
        JSONArray newInner = new JSONArray();
        JSONObject result = new JSONObject();
        //先找到开头元素
        String afterTmp = "";
        for (int i = 0; i < details.size(); i++) {
            JSONObject obj = details.getJSONObject(i);
            String before = obj.getString("before");
            if ("undefined".equals(before)) {
                newDetails.add(obj);
                afterTmp = obj.getString("after");
            }
        }
        for (int i = 0; i < inner.size(); i++) {
            JSONObject obj = inner.getJSONObject(i);
            String before = obj.getString("before");
            if ("undefined".equals(before)) {
                newInner.add(obj);
                afterTmp = obj.getString("after");
            }
        }
        //依次往下找
        sort:
        while (true) {
            if (StringUtils.isEmpty(afterTmp) || afterTmp.equals("undefined")) {
                break sort;
            }
            for (int i = 0; i < details.size(); i++) {
                JSONObject obj = details.getJSONObject(i);
                String uuid = obj.getString("uuid");
                if (afterTmp.equals(uuid)) {
                    newDetails.add(obj);
                    afterTmp = obj.getString("after");
                    continue sort;
                }
            }
            for (int i = 0; i < inner.size(); i++) {
                JSONObject obj = inner.getJSONObject(i);
                String uuid = obj.getString("uuid");
                if (afterTmp.equals(uuid)) {
                    newInner.add(obj);
                    afterTmp = obj.getString("after");
                    continue sort;
                }
            }
            break;
        }
        result.put("detail", newDetails);
        result.put("inner", newInner);
        return result;
    }

    private void transforEnumDetails(JSONArray details, UqlClass uqlClass, String operatorSign, UqlWhere where, String activeIndexId,
                                     AbstractFieldAnalyzer schema, String crfId, String groupId, String projectId, JSONArray patientSetId, String patientSql, Integer sqlGroupId) throws IOException, ExecutionException, InterruptedException {
        List<List<UqlWhereElem>> elemLists = new ArrayList<>();
        int detailSize = details == null ? 0 : details.size();

        for (int i = 0; i < detailSize; i++) {
            List<UqlWhereElem> elems = new ArrayList<>();
            JSONObject detailObj = details.getJSONObject(i);
            JSONArray strongRef = detailObj.getJSONArray("strongRef");
            String detailOperatorSign = detailObj.getString("operatorSign");
            String nodeType = detailObj.getString("nodeType");
            if ("placeholder".equals(nodeType)) {
                continue;
            }
            int strongRefSize = strongRef == null ? 0 : strongRef.size();
            if (strongRefSize > 0) {
                elems.add(new LiteralUqlWhereElem("("));
            }
            transforEnumDetailModel(detailObj, detailOperatorSign, elems, uqlClass, activeIndexId, schema, crfId, groupId, projectId, patientSetId, patientSql, sqlGroupId);
            if (strongRefSize > 0) {
                elems.add(new LiteralUqlWhereElem("AND"));
//                where.addElem(new LiteralUqlWhereElem("("));
                transforEnumStrongRef(strongRef, strongRefSize, uqlClass, elems, activeIndexId, schema, crfId, groupId, projectId, patientSetId, patientSql, sqlGroupId);
                elems.add(new LiteralUqlWhereElem(")"));
            }
            elemLists.add(elems);
        }
        elemLists.stream()
            .map(list -> makePair(
                topGroup((
                    list.get(0) instanceof ReferenceConditionUqlWhereElem ?
                        ((ReferenceConditionUqlWhereElem) list.get(0)).group() :
                        list.get(0) instanceof SimpleConditionUqlWhereElem ?
                            list.get(0) :
                            list.get(1))
                    .toString().split("\\.")[0]),
                list))
            .collect(groupingBy(Pair::_1, mapping(Pair::_2, Collector.of(
                ArrayList<UqlWhereElem>::new,
                (a, b) -> {
                    if (!a.isEmpty()) {
                        a.add(new LiteralUqlWhereElem(operatorSign));
                    }
                    a.addAll(b);
                },
                (a, b) -> {
                    if (b.isEmpty()) {
                        return a;
                    }
                    if (!a.isEmpty()) {
                        a.add(new LiteralUqlWhereElem(operatorSign));
                    }
                    a.addAll(b);
                    return a;
                }))))
            .forEach((group, elems) -> {
                if (where.needsOperator()) {
                    where.addElem(new LiteralUqlWhereElem(operatorSign));
                }
                if ("visitinfo".equals(group)) {
                    where.addElems(elems);
                } else {
                    Map<String, SimpleConditionUqlWhereElem> simpleMap = new HashMap<>();
                    List<UqlWhereElem> newElems = new ArrayList<>();
                    if ("or".equals(operatorSign)) {
                        for (UqlWhereElem elem : elems) {
                            if (elem instanceof SimpleConditionUqlWhereElem && ((SimpleConditionUqlWhereElem) elem).getJsonType().equals("string")) {
                                SimpleConditionUqlWhereElem elem1 = (SimpleConditionUqlWhereElem) elem;
                                if (elem1.getValue().contains("(") || elem1.getValue().contains(")")) {
                                    newElems.clear();
                                    newElems = elems;
                                    break;
                                }
                                if (!simpleMap.containsKey(elem1.getSourceTagName())) {
                                    simpleMap.put(elem1.getSourceTagName(), elem1);
                                    newElems.add(elem);
                                } else {
                                    newElems.remove(newElems.size() - 1);
                                    simpleMap.get(elem1.getSourceTagName()).updateStr(elem1.getValue());
                                    int i = 1;
                                }
                            } else {
                                if (elem.toString().equals("(") || elem.toString().equals(")")) {
                                    newElems.clear();
                                    newElems = elems;
                                    break;
                                }
                                newElems.add(elem);
                            }
                        }
                    } else {
                        newElems = elems;
                    }
                    if (newElems.size() > 0 && newElems.get(0) instanceof SimpleConditionUqlWhereElem) {
                        SimpleConditionUqlWhereElem sim = (SimpleConditionUqlWhereElem) newElems.get(0);
                        if (schema.isPackagedField(sim.getSourceTagName())) {
                            where.addElem(new LiteralUqlWhereElem("haschild("));
                            where.addElems(newElems);
                            where.addElem(new LiteralUqlWhereElem(")"));
                        } else {
                            addActiveElemsHasChild(where, newElems, schema, operatorSign);
                        }
                    } else {
                        addActiveElemsHasChild(where, newElems, schema, operatorSign);
//                        where.addElem(new LiteralUqlWhereElem("haschild("));
//                        where.addElems(newElems);
//                        where.addElem(new LiteralUqlWhereElem(")"));
                    }
                }
            });
    }

    private void addActiveElemsHasChild(UqlWhere where, List<UqlWhereElem> newElems, AbstractFieldAnalyzer schema, String operatorSign) {
        Map<String, List<UqlWhereElem>> elems = new HashMap<>();
        String tmpkey = "";
        String judgehao = "";
        String haoKey = "";
        boolean judgeoera = false;
        for (UqlWhereElem elem : newElems) {
            if (elem instanceof SimpleConditionUqlWhereElem) {
                SimpleConditionUqlWhereElem elem1 = (SimpleConditionUqlWhereElem) elem;
                String sourceTagName = elem1.getSourceTagName();
                String key = sourceTagName.substring(0, sourceTagName.lastIndexOf('.'));
                tmpkey = sourceTagName;
                if (!elems.containsKey(key)) {
                    elems.put(key, new ArrayList<UqlWhereElem>());
                }
                if ("(".equals(judgehao) && schema.isPackagedField(sourceTagName)) {
                    elems.get(key).add(new LiteralUqlWhereElem(judgehao));
                    judgehao = "";
                    haoKey = key;
                }
                elems.get(key).add(elem);
                if (judgeoera) {
                    elems.get(key).add(new LiteralUqlWhereElem("AND"));
                } else {
                    elems.get(key).add(new LiteralUqlWhereElem(operatorSign));
                }
            } else if (elem instanceof ReferenceConditionUqlWhereElem) {
                ReferenceConditionUqlWhereElem elem1 = (ReferenceConditionUqlWhereElem) elem;
                String key = elem1.group();
                tmpkey = elem1.getField();
                if (!elems.containsKey(key)) {
                    elems.put(key, new ArrayList<UqlWhereElem>());
                }
                if ("(".equals(judgehao)) {
                    elems.get(key).add(new LiteralUqlWhereElem(judgehao));
                    haoKey = key;
                    judgehao = "";
                }
                elems.get(key).add(elem);
//                if(")".equals(judgehao)){
////                    elems.get(key).add(new LiteralUqlWhereElem(judgehao));
////                    judgehao = "";
////                }
                if (judgeoera) {
                    elems.get(key).add(new LiteralUqlWhereElem("AND"));
                } else {
                    elems.get(key).add(new LiteralUqlWhereElem(operatorSign));
                }
            } else {
                if (elem.toString().equals("(")) {
                    judgeoera = true;
                    judgehao = "(";
                }
                if (elem.toString().equals(")") && StringUtils.isNotEmpty(haoKey)) {
                    judgeoera = false;
                    elems.get(haoKey).add(new LiteralUqlWhereElem(")"));
                }
            }
        }
        if (elems.keySet().size() == 1) {
            if (schema.isPackagedField(tmpkey)) {
                where.addElem(new LiteralUqlWhereElem("haschild("));
                where.addElems(newElems);
                where.addElem(new LiteralUqlWhereElem(")"));
                return;
            } else {
                where.addElems(newElems);
                return;
            }
        }
        Set<String> judgeKey = new HashSet<>();
        for (UqlWhereElem elem : newElems) {
            if (elem instanceof SimpleConditionUqlWhereElem) {
                SimpleConditionUqlWhereElem elem1 = (SimpleConditionUqlWhereElem) elem;
                String sourceTagName = elem1.getSourceTagName();
                String key = sourceTagName.substring(0, sourceTagName.lastIndexOf('.'));
                if (judgeKey.contains(key)) continue;
                judgeKey.add(key);
                if (schema.isPackagedField(sourceTagName)) {
                    where.addElem(new LiteralUqlWhereElem("haschild("));
                    if (elems.get(key).get(elems.get(key).size() - 1).toString().equals(")")) {
                        elems.get(key).remove(elems.get(key).size() - 2);
                        if (elems.get(key).get(elems.get(key).size() - 2).toString().equals("AND")) {
                            elems.get(key).remove(elems.get(key).size() - 2);
                        }
                    } else {
                        elems.get(key).remove(elems.get(key).size() - 1);
                    }
                    where.addElems(elems.get(key));
                    where.addElem(new LiteralUqlWhereElem(")"));
                } else {
                    elems.get(key).remove(elems.get(key).size() - 1);
                    where.addElems(elems.get(key));
                }
                if (judgeKey.size() < elems.size()) {
                    where.addElem(new LiteralUqlWhereElem("AND"));
                }
            } else if (elem instanceof ReferenceConditionUqlWhereElem) {
                ReferenceConditionUqlWhereElem elem1 = (ReferenceConditionUqlWhereElem) elem;
                String key = elem1.group();
                if (judgeKey.contains(key)) continue;
                judgeKey.add(key);
                if (schema.isPackagedField(elem1.getField())) {
                    where.addElem(new LiteralUqlWhereElem("haschild("));
                    if (elems.get(key).get(elems.get(key).size() - 1).toString().equals(")")) {
                        elems.get(key).remove(elems.get(key).size() - 2);
                    } else {
                        elems.get(key).remove(elems.get(key).size() - 1);
                    }
                    where.addElems(elems.get(key));
                    where.addElem(new LiteralUqlWhereElem(")"));
                } else {
                    if (elems.get(key).get(elems.get(key).size() - 1).toString().equals(")")) {
                        elems.get(key).remove(elems.get(key).size() - 2);
                        if (elems.get(key).get(elems.get(key).size() - 2).toString().equals("AND")) {
                            elems.get(key).remove(elems.get(key).size() - 2);
                        }
                    } else {
                        elems.get(key).remove(elems.get(key).size() - 1);
                    }
                    where.addElems(elems.get(key));
                }
                if (judgeKey.size() < elems.size()) {
                    where.addElem(new LiteralUqlWhereElem("AND"));
                }
            } else {
                if ("AND".equals(elem.toString()) || "and".equals(elem.toString()) || elem.toString().equals(operatorSign)) {
                    if (elem.toString().equals(operatorSign) && ")".equals(where.getLastElem())) {
                        where.addElem(elem);
                        continue;
                    } else {
                        continue;
                    }
                }
                if (")".equals(elem.toString()) && ("AND".equals(where.getLastElem()) || "and".equals(where.getLastElem()) || operatorSign.equals(where.getLastElem()))) {
                    where.removeElem();
                }
                if ("(".equals(elem.toString()) && !where.isEmpty() && ")".equals(where.getLastElem())) {
                    where.addElem(new LiteralUqlWhereElem(operatorSign));
                }
                if (")".equals(elem.toString()) && "(".equals(where.getLastElem())) {
                    while (where.getLastElems() instanceof LiteralUqlWhereElem) {
                        where.removeElem();
                    }
                }
                where.addElem(elem);
            }
        }
    }

    private String topGroup(String s) {
        return "sub_inspection".equals(s) ? "inspection_reports" : s;
    }

    private void transforEnumStrongRef(JSONArray strongRef, int strongRefSize, UqlClass uqlClass, List<UqlWhereElem> elems,
                                       String activeIndexId, AbstractFieldAnalyzer schema, String crfId, String groupId, String projectId, JSONArray patientSetId, String patientSql, Integer sqlGroupId) throws IOException, ExecutionException, InterruptedException {
        for (int i = 0; i < strongRefSize; i++) {
            if (i > 0) elems.add(new LiteralUqlWhereElem("AND"));
            JSONObject tmpObj = strongRef.getJSONObject(i);
            transforEnumDetailModel(tmpObj, "and", elems, uqlClass, activeIndexId, schema, crfId, groupId, projectId, patientSetId, patientSql, sqlGroupId);
        }
    }

    private void transforEnumDetailModel(JSONObject detailObj, String detailOperatorSign, List<UqlWhereElem> elems, UqlClass uqlClass,
                                         String activeIndexId, AbstractFieldAnalyzer schema, String crfId, String groupId, String projectId, JSONArray patientSetId, String patientSql, Integer sqlGroupId) throws IOException, ExecutionException, InterruptedException {
        StringBuffer stringBuffer = new StringBuffer();
        String stitching = detailObj.getString("Stitching");
        if (StringUtils.isEmpty(stitching)) {
            stitching = detailObj.getString("operatorSign");
        }
        String sourceName = detailObj.getString("sourceTagName");
        if (StringUtils.isEmpty(sourceName)) return;
        String sourceTagName = disposeVisits(sourceName);
        if (schema.isPackagedField(sourceTagName)) {
            sourceTagName = KeyPath.compile(sourceTagName).removeLast(2).stream().map(Object::toString).collect(joining("."));
        }
        String value = detailObj.getString("value");
        String refActiveId = detailObj.getString("refActiveId");
        String condition = ConditionUtilMap.getCondition(stitching);
        String jsonType = detailObj.getString("jsonType");
        if (StringUtils.isNotEmpty(refActiveId)) { // 引用数据
            List<ActiveSqlMap> activeSqlMaps = activeSqlMapMapper.getActiveSqlBySqlGroup(refActiveId, groupId, sqlGroupId);
            if (activeSqlMaps == null || activeSqlMaps.size() == 0) {
                searchByuqlService.referenceCalculate(refActiveId, projectId, CommonContent.ACTIVE_TYPE_INDEX, UqlConfig.RESULT_ORDER_KEY.get(crfId), patientSetId, groupId, null, crfId);
                activeSqlMaps = activeSqlMapMapper.getActiveSqlBySqlGroup(refActiveId, groupId, sqlGroupId);
            }
            ActiveSqlMap activeSqlMap = activeSqlMaps.get(0);
            String activeId = activeSqlMap.getRefActiveIds();
//
            String sql = activeSqlMap.getUncomActiveSql();
            refActiveId = "t" + refActiveId;
            uqlClass.getEnumOther().add(activeSqlMap.getUncomSqlWhere());
            uqlClass.setJoinValue(refActiveId, sql);
            uqlClass.addActiveId(refActiveId);
            String sourceValue;
            switch (jsonType) {
                case "date":
                    sourceValue = ConditionUtilMap.getIndexSourceValue(stitching, sourceTagName, value, refActiveId, refActiveId, jsonType, uqlClass.getResultValue(), "all", "0", activeIndexId);
                    break;
                case "long":
                    sourceValue = ConditionUtilMap.getIndexSourceValueForNum(stitching, sourceTagName, value, refActiveId, jsonType, uqlClass.getResultValue(), "all", "0", activeIndexId);
                    break;
                case "double":
                    sourceValue = ConditionUtilMap.getIndexSourceValueForDou(stitching, sourceTagName, value, refActiveId, jsonType, uqlClass.getResultValue(), "all", "0", activeIndexId);
                    if (StringUtils.isEmpty(sourceValue)) {
                        sourceValue = ConditionUtilMap.getIndexSourceValueForNum(stitching, sourceTagName, value, refActiveId, jsonType, uqlClass.getResultValue(), "all", "0", activeIndexId);
                    }
                    break;
                default:
                    throw new RuntimeException("未知的jsonType");
            }
            elems.add(new ReferenceConditionUqlWhereElem(sql, singletonList(sourceValue), sourceTagName, uqlClass.getFrom(), detailOperatorSign, true, crfId, patientSql));
            return;
        }

        if ("boolean".equals(jsonType)) {
            disposeBooleanCondition(stringBuffer, value, condition, sourceTagName);
            String result = stringBuffer.toString();
            elems.add(new SimpleConditionUqlWhereElem(result, detailOperatorSign, jsonType, sourceTagName, condition, value));
            return;
        }
        if ("between".equals(condition)) {
            disposeDataCondition(value, condition, sourceTagName, stringBuffer);
            String result = stringBuffer.toString();
            elems.add(new SimpleConditionUqlWhereElem(result, detailOperatorSign, jsonType, sourceTagName, condition, value));
            return;
        }
        value = disposeArrayByContain(value, condition);
        condition = disposeConditionByContain(value, condition);
        value = disposeValue(sourceTagName, value, jsonType, schema, condition);
        stringBuffer.append(TransData.transDataNumber(sourceTagName));
        stringBuffer.append(" " + condition);
        stringBuffer.append(" " + value);
        String result = stringBuffer.toString();
        elems.add(new SimpleConditionUqlWhereElem(result, detailOperatorSign, jsonType, sourceTagName, condition, value));

    }

    @Override
    public String SearchByExclude(JSONObject object, String resultOrderKey, Integer isSearch, PatientsIdSqlMap patientsIdSqlMap, String crfId) throws ExecutionException, InterruptedException, IOException {
        String patientSql = TransPatientSql.getAllPatientSql(patientsIdSqlMap.getPatientSnIds(), crfId);
        final AbstractFieldAnalyzer schema = SCHEMAS.get(crfId);
        String projectId = object.getString("projectId");
        JSONArray patientSetId = object.getJSONArray("patientSetId");
        String isVariant = object.getString("isVariant");
        String groupToId = object.getString("groupToId");
        groupToId = StringUtils.isEmpty(groupToId) ? UqlConfig.CORT_INDEX_ID : groupToId;
        String activeIndexId = object.getString("id");
        JSONArray config = object.getJSONArray("config");

        UqlWhere where = new UqlWhere();
        UqlClass uqlClass = new CrfExcludeUqlClass(projectId, crfId);
        transforConditionForConfig(config, uqlClass, where, schema, crfId, groupToId, projectId, patientSetId, patientsIdSqlMap.getId());
        uqlClass.setInitialPatients(isVariant, patientSql);
        activeIndexId = isSearch == CommonContent.ACTIVE_TYPE_TEMP ? activeIndexId.concat("_tmp") : activeIndexId;
        String resultDocId = searchByuqlService.searchDocIdBySql(uqlClass.getSql(), projectId, crfId);

        ActiveSqlMap activeSqlMap = new ActiveSqlMap(projectId, activeIndexId, GzipUtil.compress(uqlClass.getCrfSql()),
            uqlClass.getSelect(), uqlClass.getFrom(), uqlClass.getActiveId().toJSONString(),
            JSON.toJSONString(uqlClass.getSource()));
        activeSqlMap.setUncomSqlWhere(uqlClass.getWhere());
        activeSqlMap.setGroupId(StringUtils.isEmpty(groupToId) ? UqlConfig.CORT_INDEX_ID : groupToId);
        activeSqlMap.setResultDocId(resultDocId);
        activeSqlMap.setPatSqlGroup(patientsIdSqlMap.getId());
        activeSqlMapMapper.insert(activeSqlMap);
        return uqlClass.getCrfSql();
    }

    private void transforConditionForConfig(JSONArray config, UqlClass uqlClass, UqlWhere where, AbstractFieldAnalyzer schema, String crfId,
                                            String groupId, String projectId, JSONArray patientsSetId, Integer sqlGroupId) throws InterruptedException, ExecutionException {
        StringBuffer resultBuffer = new StringBuffer();
        JSONObject incs = config.stream().map(JSONObject.class::cast).filter(o -> "纳入标准".equals(o.getString("activeResult"))).findAny().get();
        JSONObject excs = config.stream().map(JSONObject.class::cast).filter(o -> "排除标准".equals(o.getString("activeResult"))).findAny().get();
        transforConditionForConditions(incs.getJSONArray("conditions"), uqlClass, where, false, true, true, false, schema, crfId, groupId, projectId, patientsSetId, sqlGroupId);
        transforConditionForConditions(excs.getJSONArray("conditions"), uqlClass, where, true, where.isEmpty(), true, true, schema, crfId, groupId, projectId, patientsSetId, sqlGroupId);
        where.addElem(new LiteralUqlWhereElem("AND join_field='patient_info'"));
        where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
        uqlClass.setWhere(where.toString());
        uqlClass.setSelect("patient_info.patient_basicinfo.DOC_ID");
    }

    //有个 not ( ()) 因为 在inner 里 所以传 not 的地方需要在增加一个变量
    private void transforConditionForConditions(JSONArray conditions, UqlClass uqlClass, UqlWhere where, boolean not, boolean first,
                                                boolean op, boolean isNot, AbstractFieldAnalyzer schema, String crfId, String groupId,
                                                String projectId, JSONArray patientsSetId, Integer sqlGroupId) {
        String opStr = op ? "AND" : "OR";
        // noinspection SuspiciousToArrayCall
        for (JSONObject condition : conditions.toArray(new JSONObject[0])) {
            boolean subOp = "and".equals(condition.getString("operatorSign"));
            String subOpStr = subOp ? "AND" : "OR";
            if (!first) {
                where.addElem(new LiteralUqlWhereElem(opStr + " "));
            }
            if (not) {
                where.addElem(new LiteralUqlWhereElem("NOT"));
            }
            JSONArray inner = condition.getJSONArray("inner");
            List<JSONObject> details = condition.getJSONArray("details").stream()
                .map(JSONObject.class::cast)
                .filter(o -> !"placeholder".equals(o.getString("nodeType")))
                .collect(toList());
            if (inner.size() + details.size() > 1) {
                where.addElem(new LiteralUqlWhereElem("("));
            }
            first = false;
            if (!inner.isEmpty()) {
                transforConditionForConditions(condition.getJSONArray("inner"), uqlClass, where, false, true, subOp, isNot, schema, crfId, groupId, projectId, patientsSetId, sqlGroupId);
                where.addElem(new LiteralUqlWhereElem(subOpStr));
            }
            details.forEach(o -> {
                UqlWhereElem elem;
                boolean isEnum = "枚举".equals(o.getString("jsonType").split(":")[0]);
                DataType type = DataType.fromString(o.getString("jsonType"));
                if (StringUtils.isNotEmpty(o.getString("refActiveId"))) {
                    // 指标或者事件
                    String refId = o.getString("refActiveId");
                    uqlClass.addActiveId(refId);
                    List<ActiveSqlMap> activeSql = activeSqlMapMapper.getActiveSqlBySqlGroup(refId, groupId, sqlGroupId);
                    if (activeSql == null || activeSql.size() == 0) {
                        try {
                            searchByuqlService.referenceCalculate(refId, projectId, CommonContent.ACTIVE_TYPE_INDEX, UqlConfig.RESULT_ORDER_KEY.get(crfId), patientsSetId, groupId, null, crfId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        activeSql = activeSqlMapMapper.getActiveSqlBySqlGroup(refId, groupId, sqlGroupId);
                    }
                    if (isEnum) {
                        // 枚举只可能是指标
                        JSONArray values = o.getJSONArray("value");
                        activeSql = activeSql.stream().filter(map -> values.contains(map.getIndexResultValue())).collect(toList());
                        if (activeSql.size() > 1) {
                            where.addElem(new LiteralUqlWhereElem("("));
                        }
                        if ("!equal".equals(o.getString("operatorSign"))) {
                            where.addElem(new LiteralUqlWhereElem("NOT("));
                        }
                        activeSql.forEach(map -> {
                            try {
                                where.addElem(new IndexReferenceConditionUqlWhereElem(
                                    map.getSelectValue(),
                                    uqlClass.getFrom(),
                                    map.getUncomActiveSql().split("where ")[1],
                                    null,
                                    null,
                                    null,
                                    null,
                                    true,
                                    crfId
                                ));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            where.addElem(new LiteralUqlWhereElem("OR"));
                        });
                        if (!where.isEmpty()) {
                            where.removeElem();  // 去除的是最后一个"OR"
                        }
                        if ("!equal".equals(o.getString("operatorSign"))) {
                            where.addElem(new LiteralUqlWhereElem(")"));
                        }
                        if (activeSql.size() > 1) {
                            where.addElem(new LiteralUqlWhereElem(")"));
                        }
                    } else if ("1".equals(activeSql.get(0).getActiveType())) {
                        // 事件
                        ActiveSqlMap map = activeSql.get(0);
                        String activeResultDoc = map.getActiveResultDocId();
                        boolean isActiveFirst = activeResultDoc.startsWith("first") || activeResultDoc.startsWith("any") || activeResultDoc.startsWith("last");
                        try {
                            where.addElem(new IndexReferenceConditionUqlWhereElem(
                                map.getSelectValue().split(" as")[0],
                                uqlClass.getFrom(),
                                map.getUncomActiveSql().split("where ")[1],
                                o.getString("operatorSign"),
                                o.get("value"),
                                type,
                                not,
                                map.getUncoomActiveOtherResult(),
                                isActiveFirst,
                                map.getCountValue(),
                                true,
                                crfId
                            ));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // 非枚举的指标
                        ActiveSqlMap map = activeSql.get(0);
                        try {
                            where.addElem(new IndexReferenceConditionUqlWhereElem(
                                map.getSelectValue(),
                                uqlClass.getFrom(),
                                map.getUncomActiveSql().split("where ")[1],
                                o.getString("operatorSign"),
                                o.get("value"),
                                type,
                                map.getCountValue(),
                                true,
                                crfId
                            ));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    JSONArray strongRef = o.getJSONArray("strongRef");
                    if (strongRef.size() > 0) {
                        where.addElem(new LiteralUqlWhereElem("("));
                        strongRef.forEach(obj -> {
                                JSONObject str = (JSONObject) obj;
                                DataType strType = DataType.fromString(str.getString("jsonType"));
                                where.addElem(new LiteralUqlWhereElem(strType.serialize(
                                    str.getString("sourceTagName"),
                                    str.getString("operatorSign"),
                                    str.get("value"),
                                    true)));
                                where.addElem(new LiteralUqlWhereElem("AND"));
                            }
                        );
                    }
                    where.addElem(new LiteralUqlWhereElem(type.serialize(
                        o.getString("sourceTagName"),
                        o.getString("operatorSign"),
                        o.get("value"),
                        true)));
                    if (strongRef.size() > 0) {
                        where.addElem(new LiteralUqlWhereElem(")"));
                    }
                }
                where.addElem(new LiteralUqlWhereElem(subOpStr));
            });
            where.removeElem();
            if (inner.size() + details.size() > 1) {
                where.addElem(new LiteralUqlWhereElem(")"));
            }
        }
    }

    @Override
    public String SearchByIndex(JSONObject object, String resultOrderKey, Integer isSearch, PatientsIdSqlMap patientsIdSqlMap, String crfId) throws ExecutionException, InterruptedException, IOException {
        UqlClass uqlClass = null;
        String patientSql = TransPatientSql.getAllPatientSql(patientsIdSqlMap.getPatientSnIds(), crfId);
        final AbstractFieldAnalyzer schema = SCHEMAS.get(crfId);
        JSONArray patientSetId = object.getJSONArray("patientSetId");
        String name = object.getString("name");
        String isVariant = object.getString("isVariant");
        String groupToId = object.getString("groupToId");
        String projectId = object.getString("projectId");
        if (UqlConfig.CORT_INDEX_ID.equals(groupToId)) {
            isVariant = "1";
        }
        groupToId = StringUtils.isEmpty(groupToId) ? UqlConfig.CORT_INDEX_ID : groupToId;
        JSONObject config = object.getJSONArray("config").getJSONObject(0);
        String activeIndexId = config.getString("activeIndexId");//指标id
        String T_activeIndexId = isSearch == CommonContent.ACTIVE_TYPE_TEMP ? activeIndexId.concat("_tmp") : activeIndexId;
        String R_activeIndexId = "t" + activeIndexId;
        String indexType = config.getString("indexType");
        String indexTypeValue = DataType.fromString(indexType).name();
        JSONArray contitions = config.getJSONArray("conditions");
        JSONObject resultObj = JSONObject.parseObject(resultOrderKey);
        String indexCol = config.getString("indexColumn").substring(0, config.getString("indexColumn").lastIndexOf("."));
        String indexColumn = disposeVisits(config.getString("indexColumn"));
        String function = config.getString("function");
        String functionParam = config.getString("functionParam");

        String order1 = null;
        String selectValue = null;

        String indexDate = disposeVisits(resultObj.getString(indexCol));
        KeyPath indexDatePath = KeyPath.compile(indexDate);
        if (schema.isPackagedField(indexDate)) {
            indexDate = indexDatePath.removeLast(2).stream().map(Object::toString).collect(joining("."));
        } else {
            indexDatePath = "visits".equals(indexDatePath.getFirst()) ? indexDatePath.keyPathByRemovingFirst() : indexDatePath;
            indexDate = indexDatePath.stream().map(Object::toString).collect(joining("."));
        }

        KeyPath visitsPath = KeyPath.compile(indexColumn);
        String visits = indexColumn;
        if (schema.isPackagedField(visits)) {
            visits = visitsPath.removeLast(2).firstAsString();
        } else {
            visits = "visitinfo";
        }

        //指标处理
        if (StringUtils.isNotEmpty(indexColumn)) {
            if (schema.isPackagedField(indexColumn)) {
                indexColumn = KeyPath.compile(indexColumn).removeLast(2).stream().map(Object::toString).collect(joining("."));
            }
            uqlClass = new CrfIndexUqlClass(projectId, crfId);
            String order = FunctionUtilMap.getUqlFunction(function, functionParam, TransData.transDataNumber(indexColumn), indexType, TransData.transDataNumber(indexDate));
            selectValue = order;
            uqlClass.setSelect(uqlClass.getSelect() + order);
            order1 = indexColumn;

            uqlClass.setSqlHaving(functionParam);
            uqlClass.setResultValue(indexColumn);
            uqlClass.setResultFunction(function);
            uqlClass.setResultFunctionNum(functionParam);
        }
        UqlWhere where = new UqlWhere();

        JSONObject contitionObj = contitions.getJSONObject(0);
        String hasCount = getIndexHasCount(visits);
        uqlClass.setActiveSelect(uqlClass.getSelect() + hasCount);

        //处理 条件
        transforEnumCondition(contitionObj, uqlClass, where, R_activeIndexId, schema, crfId, groupToId, projectId, patientSetId, patientSql, patientsIdSqlMap.getId());

        UqlClass sqlresult = null;
        String sqlMd5 = "";
        if (where.isSameGroup(visits)) {
            uqlClass.setWhere(TransData.transDataNumber(order1) + " IS NOT NULL AND ");
            uqlClass.setInitialPatients(isVariant, patientSql);
            String orderKey = order1.substring(0, order1.lastIndexOf("."));
            String indexDateKey = indexDate.substring(0, indexDate.lastIndexOf("."));
            if (orderKey.equals(indexDateKey)) {
                uqlClass.setWhere(uqlClass.getWhere() + TransData.transDataNumber(indexDate) + " IS NOT NULL AND ");
            } else {
                uqlClass.setNotAllWhere(function, order1, indexDate, schema);
            }
            where.deleteHasChild();
            where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
            uqlClass.setWhere(uqlClass.getWhereNotNull() + " ( " + where.toString() + " ) ");
            String sqlNew = uqlClass.getHavingSql();
            sqlMd5 = StringToMd5.stringToMd5(sqlNew);
            Integer sqlMd5count = activeSqlMapMapper.getCountByActiveAndsqlMd5(T_activeIndexId, sqlMd5, groupToId);
            if (sqlMd5count > 0) return null;
            sqlresult = uqlClass;
            sqlresult.setWhere(sqlresult.getWhere() + " and join_field = '" + visits + "'");
        } else {
            uqlClass.setWhereIsEmpty(where, order1, null, null, schema);
            uqlClass.setInitialPatients(isVariant, patientSql);
            uqlClass.setNotAllWhere(function, order1, indexDate, schema);
            where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
            uqlClass.setWhere(uqlClass.getWhereNotNull() + " ( " + where.toString() + " ) ");

            boolean isGetVisisn = !where.isEmpty();
            String sqlNew = uqlClass.getHavingSql();
            sqlMd5 = StringToMd5.stringToMd5(sqlNew);
            Integer sqlMd5count = activeSqlMapMapper.getCountByActiveAndsqlMd5(T_activeIndexId, sqlMd5, groupToId);
            if (sqlMd5count > 0) return null;
            if (StringUtils.isNotEmpty(indexColumn) && isGetVisisn) {
                sqlresult = getIndexSql(uqlClass, function, functionParam, indexColumn, indexType, indexDate, projectId, hasCount, crfId);
            } else {
                sqlresult = uqlClass;
            }

            String andGroupCondition = getIndexGroupCondition(where, indexColumn);
            if (StringUtils.isNotEmpty(andGroupCondition)) {
                sqlresult.setWhere(sqlresult.getWhere() + " and (" + andGroupCondition.toString() + ")");
            }
            sqlresult.setWhere(sqlresult.getWhere() + " and join_field = '" + visits + "'");

        }
        sqlresult.setWhere("(" + indexColumn + " is not null) and " + sqlresult.getWhereNotNull());
        sqlresult.setSqlHaving(functionParam);
        String newSql = sqlresult.getHavingSql(crfId);
        String resultDocId = searchByuqlService.searchDocIdBySql(newSql, projectId, crfId);
        ActiveSqlMap activeSqlMap = new ActiveSqlMap(projectId, T_activeIndexId, GzipUtil.compress(newSql),
            sqlresult.getSelect(), sqlresult.getFrom(), uqlClass.getSourceFilter(),
            uqlClass.getActiveId().toJSONString(), JSON.toJSONString(uqlClass.getSource()), selectValue,
            indexTypeValue, name, hasCount.split(",")[1], sqlMd5);
        activeSqlMap.setUncomSqlWhere(sqlresult.getWhere());
        activeSqlMap.setGroupId(StringUtils.isEmpty(groupToId) ? UqlConfig.CORT_INDEX_ID : groupToId);
        activeSqlMap.setSqlHaving(uqlClass.getHaving());
        activeSqlMap.setPatSqlGroup(patientsIdSqlMap.getId());
        activeSqlMap.setResultDocId(resultDocId);
        if (StringUtils.isEmpty(groupToId) || UqlConfig.CORT_INDEX_ID.equals(groupToId)) {
            SingleExecutorService.getInstance().getFlushCountGroupExecutor().submit(() -> {
                try {
                    searchByuqlService.saveCortrastiveResultRedisMap(activeSqlMap, projectId, crfId, T_activeIndexId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        activeSqlMapMapper.insert(activeSqlMap);
        /*引用依赖计算*/
        return sqlresult.getCrfSql();
    }

    private String getIndexGroupCondition(UqlWhere where, String indexColumn) {
        StringBuffer andGroupCondition = new StringBuffer();
        for (UqlWhereElem elem : where) {
            String s = elem.toString().trim();
            if (elem instanceof LiteralUqlWhereElem) {
                andGroupCondition.append(" " + s.replaceAll("haschild", "") + " ");
            } else if (elem instanceof SimpleConditionUqlWhereElem || elem instanceof ReferenceConditionUqlWhereElem) {
                String indexTarget = indexColumn.substring(0, indexColumn.indexOf("."));
                if (s.startsWith(indexTarget) || s.startsWith("\"" + indexTarget) || s.startsWith("sub_inspection") && indexTarget.startsWith("inspection_reports")) {
                    andGroupCondition.append(" " + s + " ");
                } else if (s.startsWith(indexTarget) || s.startsWith("inspection_reports") && indexTarget.startsWith("sub_inspection")) {
                    andGroupCondition.append(" " + s + " ");
                } else if (s.startsWith("haschild(" + indexTarget) || s.startsWith("haschild(sub_inspection") && indexTarget.startsWith("inspection_reports")) {
                    andGroupCondition.append(" " + s.substring(0, s.length() - 1).replaceAll("haschild\\(", " "));
                } else if (s.startsWith("haschild(" + indexTarget) || s.startsWith("haschild(inspection_reports") && indexTarget.startsWith("sub_inspection")) {
                    andGroupCondition.append(" " + s.substring(0, s.length() - 1).replaceAll("haschild\\(", " "));
                } else {
                    andGroupCondition.append("visitinfo.DOC_ID  IS NOT NULL");
                }
            }
        }
        return andGroupCondition.toString();
    }

    private String getIndexHasCount(String indexColumn) {
        return " ,count(" + indexColumn + ".DOC_ID) as jocount ";
    }

    private void disposeDataCondition(String value, String condition, String sourceTagName, StringBuffer stringBuffer) {
        JSONArray valueArray = JSONArray.parseArray(value);
        String date1 = valueArray.getString(0);
        String date2 = valueArray.getString(1);
        stringBuffer.append(TransData.transDataNumber(sourceTagName) + " between '" + date1 + "' and '" + date2 + "'");
    }

    private UqlClass getIndexSql(UqlClass uqlClass, String function, String functionParam, String indexColumn, String indexType, String indexDate, String projectId, String hascount, String crfId) {
        UqlClass resultUql = new StandardUqlClass();
        resultUql.setActiveSelect(uqlClass.getSelect());
        resultUql.setFrom(uqlClass.getFrom());

        String where = getIndexResultWhere(uqlClass, function, functionParam, indexColumn, indexType, indexDate, projectId, hascount, crfId);
        resultUql.setWhere(where);
        return resultUql;
    }

    private String getIndexResultWhere(UqlClass uqlClass, String function, String functionParam, String indexColumn, String indexType, String indexDate, String projectId, String hasCount, String crfId) {
        String order = FunctionUtilMap.getUqlFunction(function, functionParam, "visitinfo.DOC_ID", indexType, TransData.transDataNumber(indexDate));
        uqlClass.setSelect(order);
        uqlClass.setSelect(" group_concat(visitinfo.DOC_ID,',') ");
        uqlClass.setWhere("join_field='visitinfo' AND " + uqlClass.getWhereNotNull());
        String sql = uqlClass.getCrfSql();
        JSONArray source = new JSONArray();
        String result = httpUtils.querySearch(projectId, sql, 1, Integer.MAX_VALUE - 1, null, source, crfId);
        JSONObject jsonData = JSONObject.parseObject(result);
        String visitSnAll = UqlQureyResult.getVisitSnAll(jsonData);
        String key = SearchUqlVisitSn.getSearchUqlVisitSn(indexColumn.substring(0, indexColumn.lastIndexOf(".")));
        if (StringUtils.isEmpty(key)) {
            key = "visitinfo.DOC_ID";
        }
        return key + " " + visitSnAll;
    }

    private void disposeBooleanCondition(StringBuffer resultBuffer, String value, String condition, String sourceTagName) {
        JSONArray values = JSONArray.parseArray(value);
        int size = values == null ? 0 : values.size();
        for (int i = 0; i < size; i++) {
            if (size > 1 && i == 0) resultBuffer.append("(");
            if (i > 0) resultBuffer.append(" OR ");
            resultBuffer.append(sourceTagName);
            resultBuffer.append(condition);
            String val = values.getString(i);
            val = "否".equals(val) ? "false" : val;
            val = "是".equals(val) ? "true" : val;
            resultBuffer.append(val);
        }
        if (size > 1) {
            resultBuffer.append(")");
        }

    }

    private String disposeValue(String key, String value, String jsonType, AbstractFieldAnalyzer schema, String condition) {
        value = value.trim();
        if ("IN".equals(condition) || "NOT IN".equals(condition)) {
            if (value.startsWith("(")) {
                return value;
            }
            if (value.startsWith("[")) {
                return "(" + value.substring(1, value.length() - 1) + ")";
            }
        }
        if (value.startsWith("(")) {
            value = "[" + value.substring(1, value.length() - 1) + "]";
        }
        if (value.startsWith("[")) {
            value = JSON.parseArray(value).getString(0);
        }
        value = value.replace('\'', '"');
        try {
            if ("string".equals(jsonType)) {
                value = "'" + value + "'";
            } else if ("date".equals(jsonType)) {
                Date date = null;
                for (String pattern : asList("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "yyyy")) {
                    try {
                        date = new SimpleDateFormat(pattern).parse(value);
                        break;
                    } catch (ParseException ignored) {
                    }
                }
                value = "'" + new SimpleDateFormat(schema.getDateFormat(key, false)).format(date) + "'";
            } else {
                Integer.valueOf(value);
            }
        } catch (NumberFormatException e) {
            value = "'" + value + "'";
        }
        return value;
    }

    private String disposeConditionByContain(String value, String condition) {
        if (condition.contains("CONTAIN") && value.startsWith("(")) {
            condition = condition.replace("CONTAIN", "IN");
        }
        return condition;
    }

    //处理 包含 并且数组类型
    private String disposeArrayByContain(String value, String condition) {
        StringBuffer tmBuffer = new StringBuffer();
        if (condition.contains("CONTAIN") && value.startsWith("[")) {
            condition = condition.replace("CONTAIN", "IN");
            value = value.substring(1, value.length() - 1);
            String[] valueArray = value.split(",");
            tmBuffer.append("(");
            for (int i = 0; i < valueArray.length; i++) {
                if (i > 0) {
                    tmBuffer.append(",");
                }
                tmBuffer.append(valueArray[i].replace("\"", "'"));
            }
            tmBuffer.append(")");
            value = tmBuffer.toString();
        }
        return value;
    }

    private String disposeVisits(String visits) {
        String[] vistsArray = visits.split("\\.");
        if (visits.startsWith("visits.medical_record_home_page")) {
            return vistsArray[vistsArray.length - 2] + "." + vistsArray[vistsArray.length - 1];
        }
        if (visits.startsWith("visits.inspection_reports")) {
            return vistsArray[vistsArray.length - 2] + "." + vistsArray[vistsArray.length - 1];
        }
        if (visits.startsWith("visits.test.blood_routine")) {
            return vistsArray[vistsArray.length - 2] + "." + vistsArray[vistsArray.length - 1];
        }
        if (visits.startsWith("visits")) {
            return visits.substring(visits.indexOf(".") + 1, visits.length());
        }
        return visits;
    }

    private static JSONArray getSource(JSONArray showColumns, String keys, JSONArray array, AbstractFieldAnalyzer schema) {
        int size = showColumns == null ? 0 : showColumns.size();
        for (int i = 0; i < size; i++) {
            JSONObject tmpColumn = showColumns.getJSONObject(i);
            String column = tmpColumn.getString("id");
            String pathStr = keys + "." + column;
            KeyPath path = KeyPath.compile(pathStr);
            if (path.tryResolve(schema.getFieldMapobj()) != null) {
                array.add(pathStr);
            }
        }
        return array;
    }

    private AjaxObject searchClasEnumResultByUql(String activeId, List<ActiveSqlMap> sqlList, String projectId, Integer pageSize, Integer pageNum, JSONArray basicColumns,
                                                 String crfId, String groupFromId, JSONArray patientSetIds, String groupId, String isVariant, JSONArray patientSetId) throws IOException, ExecutionException, InterruptedException {

        Set<String> refList = sqlList.stream()
            .map(sqlMap -> sqlMap.getRefActiveIds())
            .flatMap(array -> JSONArray.parseArray(array)
                .stream()
                .map(String.class::cast))
            .collect(toSet());

        for (String refId : refList) {
            ActiveIndex activeIndex = activeIndexMapper.selectByPrimaryKey(refId.substring(1));
            if (activeIndex == null) continue;
            String name = activeIndex.getName();
            basicColumns.add(new JSONObject().fluentPut("name", name).fluentPut("id", refId));
        }

        List<String> patientSqlList = searchByuqlService.getInitialSQLList(groupFromId, isVariant, groupId, patientSetId, projectId, crfId);
        Integer total = patientSqlList.size();
        List<String> pageList = PagingUtils.getPageContentForString(patientSqlList, pageNum, pageSize);
        Map<Integer, List<ActiveSqlMap>> groupMap = sqlList.stream().collect(groupingBy(ActiveSqlMap::getPatSqlGroup, TreeMap::new, toList()));
        Iterator<Integer> iterator = groupMap.keySet().iterator();
        JSONArray dataAll = new JSONArray();
        while (iterator.hasNext()) {
            Integer mapKey = iterator.next();
            List<ActiveSqlMap> value = groupMap.get(mapKey);
            if (dataAll.size() >= pageSize) {
                break;
            }
            Integer page = 1;
            Set<String> patientSetLocalSqlLists = patientSetService.getPatientSetLocalSqlListById(mapKey);
            String allSql = UqlConfig.getEnumSql(patientSetLocalSqlLists, projectId, crfId, pageList);
            String result = httpUtils.querySearch(projectId, allSql, page, pageSize, null, new JSONArray().fluentAdd("patient_info"), crfId);
            JSONArray data = transforEnumResult(JSON.parseObject(result), sqlList, projectId, activeId, pageSize, crfId);
            List<String> pasSn = new ArrayList<>();
            int daTasize = data == null ? 0 : data.size();
            Map<String, JSONObject> dataMap = new HashMap<>();
            for (int i = 0; i < daTasize; i++) {
                JSONObject tmpObj = data.getJSONObject(i);
                pasSn.add(tmpObj.getString("PATIENT_SN"));
                dataMap.put(tmpObj.getString("PATIENT_SN"), tmpObj);
            }
            String patSnWhere = "visitinfo.PATIENT_SN  " + TransPatientSql.transForExtContain(pasSn);
            JSONArray source = new JSONArray().fluentAdd("patient_info.patient_basicinfo.PATIENT_SN");
            for (String refActiveId : refList) {
                //拼接column
                List<ActiveSqlMap> patSqlList = activeSqlMapMapper.getActiveSqlMapByProjectIdAndSqlGroup(projectId, refActiveId.substring(1), groupId, mapKey);
                if (patSqlList == null || patSqlList.size() == 0) {
                    searchByuqlService.referenceCalculate(refActiveId, projectId, CommonContent.ACTIVE_TYPE_INDEX, UqlConfig.RESULT_ORDER_KEY.get(crfId), patientSetId, groupId, null, crfId);
                    patSqlList = activeSqlMapMapper.getActiveSqlMapByProjectIdAndSqlGroup(projectId, refActiveId.substring(1), groupId, mapKey);
                }
                if (patSqlList.size() == 0) continue;
                ActiveSqlMap patActiveSqlMap = patSqlList.get(0);
                String patSql = patActiveSqlMap.getSqlJoinSql(patSnWhere);
                String patSnResult = httpUtils.querySearch(projectId, patSql, 1, pageSize, "", source, crfId);
                JSONArray tmpHits = UqlQureyResult.getHitsArray(patSnResult);
                int tmpHitsSize = tmpHits.size();
                for (int j = 0; j < tmpHitsSize; j++) {
                    String colle = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONObject("select_field").getString("condition");
                    if (colle.contains(".")) {
                        try {
                            colle = String.format("%.2f", Double.parseDouble(colle));
                        } catch (Exception e) {
                        }
                    }
                    String patSn = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0).getJSONArray("patient_basicinfo").getJSONObject(0).getString("PATIENT_SN");
                    dataMap.get(patSn).put(refActiveId, colle);
                }
                for (String key : dataMap.keySet()) {
                    JSONObject obj = dataMap.get(key);
                    if (!obj.containsKey(refActiveId)) {
                        obj.put(refActiveId, "-");
                    }
                }
            }
            dataAll.addAll(data);
        }

        Integer count = searchByuqlService.getSearchUqlAllCount(groupFromId, patientSetId, groupId, projectId);

        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
        AjaxObject.getReallyDataValue(dataAll, basicColumns);
        ajaxObject.setData(dataAll);
        ajaxObject.setCount(count);
        ajaxObject.setColumns(basicColumns);
        WebAPIResult webAPIResult = new WebAPIResult(pageNum, pageSize, total);
        ajaxObject.setWebAPIResult(webAPIResult);

        return ajaxObject;
    }

    private JSONArray transforEnumResult(JSONObject data, List<ActiveSqlMap> sqlList, String projectId, String activeId, Integer size, String crfId) throws IOException {

        Set<String> pats = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(data)
            .stream()
            .map(String.class::cast)
            .collect(toSet());
        String patsStr = "";
        patsStr = TransPatientSql.transForExtContain(pats);
        Map<String, Set<String>> resultMap = new ConcurrentHashMap<>();
        List<Future> futures = new ArrayList<>();
        for (ActiveSqlMap activeSqlMap : sqlList) {
            String where = activeSqlMap.getUncomSqlWhere();
            String newWhere = " patient_info.patient_basicinfo.DOC_ID IN (" + patsStr + ")  and (" + where + " )";
            activeSqlMap.setUncomSqlWhere(newWhere);
            String sql = activeSqlMap.getUql(crfId);
            JSONArray source = new JSONArray();
            source.add("patient_info.patient_basicinfo.PATIENT_SN");
            String patData = httpUtils.querySearch(projectId, sql, 1, size, null, source, crfId);
            activeSqlMap.setUncomSqlWhere(where);
            Set<String> values = JSONObject.parseObject(patData)
                .getJSONObject("hits")
                .getJSONArray("hits")
                .stream()
                .map(JSONObject.class::cast)
                .map(o -> o.getJSONObject("_source"))
                .flatMap(o -> o.getJSONArray("patient_info").getJSONObject(0).getJSONArray("patient_basicinfo")
                    .stream()
                    .map(JSONObject.class::cast)
                    .map(obj -> obj.getString("PATIENT_SN")))
                .collect(toSet());
            String indexResultValue = activeSqlMap.getIndexResultValue();
            resultMap.put(indexResultValue, values);
        }

        return transforEnumDataValue(data, resultMap, activeId);
    }

    private JSONArray transforEnumDataValue(JSONObject data, Map<String, Set<String>> resultMap, String activeId) {
        JSONArray hitsArray = UqlQureyResult.getHitsArray(data);
        int size = hitsArray == null ? 0 : hitsArray.size();
        JSONArray array = new JSONArray();
        for (int i = 0; i < size; i++) {
            JSONObject obj = hitsArray.getJSONObject(i).getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0).getJSONArray("patient_basicinfo").getJSONObject(0);
            String patSn = obj.getString("PATIENT_SN");
            for (String key : resultMap.keySet()) {
                Set<String> patSet = resultMap.get(key);
                if (patSet.contains(patSn)) {
                    if (obj.containsKey(activeId)) {
                        obj.put(activeId, obj.getString(activeId) + ";" + key);
                    } else {
                        obj.put(activeId, key);
                    }
                }
            }
            array.add(obj);
        }
        return array;
    }


}
