package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.darren.collection.Pair;
import com.gennlife.darren.collection.keypath.KeyPath;
import com.gennlife.rws.content.CommonContent;
import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.content.UqlConfig;
import com.gennlife.rws.dao.*;
import com.gennlife.rws.entity.*;
import com.gennlife.rws.query.QuerySearch;
import com.gennlife.rws.query.UqlQureyResult;
import com.gennlife.rws.schema.AbstractFieldAnalyzer;
import com.gennlife.rws.schema.EmrFieldAnalyzer;
import com.gennlife.rws.service.ActiveIndexService;
import com.gennlife.rws.service.GroupService;
import com.gennlife.rws.service.RedisMapDataService;
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
    private GroupMapper groupMapper;
    @Autowired
    private ActiveIndexTaskMapper activeIndexTaskMapper;
    @Autowired
    private ActiveIndexMapper activeIndexMapper;
    @Autowired
    private ActiveIndexConfigMapper activeIndexConfigMapper;
    @Autowired
    private ActiveIndexService activeIndexService;
    @Autowired
    private PatientsSetMapper patientsSetMapper;
    @Autowired
    private GroupPatientDataMapper groupPatDataMapper;
    @Autowired
    private GroupDataMapper groupDataMapper;
    @Autowired
    private LogUtil logUtil;
    @Autowired
    private SearchByuqlServiceImpl searchByuqlService;
    @Autowired
    private RedisMapDataService redisMapDataService;


    public static Map<String, AbstractFieldAnalyzer> SCHEMAS = force(() -> {
        Map<String, AbstractFieldAnalyzer> ret = new HashMap<>();
        ret.put("lymphoma_release_1.0", new EmrFieldAnalyzer("/schema/rws_crf_lymphadenoma_data_schema_V2.0.7.json"));
        ret.put("lymphoma", new EmrFieldAnalyzer("/schema/rws_crf_lymphadenoma_data_schema_V2.0.7.json"));
        return ret;
    });

    @Override
    public AjaxObject searchClacIndexResultByUql(String activeId, String projectId, Integer pageSize, Integer pageNum, JSONArray basicColumns, String crfId, String groupFromId, JSONArray patientSetId, String groupId, String isVariant) throws IOException, ExecutionException, InterruptedException {
        Long tmie = System.currentTimeMillis();
        AjaxObject ajaxObject = new AjaxObject();
        UqlClass joinUqlClass = new StandardUqlClass(projectId);
        List<ActiveSqlMap> sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        if(sqlList == null || sqlList.size() == 0 ){
            referenceCalculate(activeId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get(crfId),patientSetId,groupId,null,crfId);
            sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        }
        String indexResultValue = "";
        if(sqlList.size()>0){
            indexResultValue = sqlList.get(0).getIndexResultValue();
        }
        LOG.info("开始请求结果"+(System.currentTimeMillis()-tmie));
        Long tmie1 = System.currentTimeMillis();
        if (StringUtils.isNotEmpty(indexResultValue)) {//枚举类型处理
            return searchClasEnumResultByUql(activeId, sqlList, projectId, pageSize, pageNum, basicColumns, crfId ,groupFromId,patientSetId,groupId,isVariant,patientSetId);
        }

        ActiveSqlMap activeSqlMap = sqlList.get(0);        //获取sql语句
        String newSql = activeSqlMap.getUncomActiveSql();
        String sourceFilter = activeSqlMap.getSourceFiltere();
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
            String sql = activeSqlMapMapper.getActiveSqlByActiveId(refActiveId.substring(1, refActiveId.length()),groupId);
            if(StringUtils.isEmpty(sql) ){
                referenceCalculate(refActiveId.substring(1, refActiveId.length()),projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get(crfId),patientSetId,groupId,null,crfId);
                sql = activeSqlMapMapper.getActiveSqlByActiveId(refActiveId.substring(1, refActiveId.length()),groupId);
            }
            joinUqlClass.setJoinValue(refActiveId, sql);
        }

        LOG.info("拼接columns"+(System.currentTimeMillis()-tmie1));
        Long tmie2 = System.currentTimeMillis();
        JSONArray source = new JSONArray();
        source.add("patient_info");
        JSONArray sourceValue = JSONArray.parseArray(activeSqlMap.getSourceValue());
        int sourceSize = sourceValue == null ? 0 : sourceValue.size();
        for (int i = 0; i < sourceSize; i++) {
            String sourceVal = sourceValue.getString(i);
            source.add(sourceVal);
        }
        String result = httpUtils.querySearch(projectId, newSql, pageNum, pageSize, sourceFilter, source,crfId);
        JSONObject object = JSONObject.parseObject(result);
        if (object.containsKey("error")) {
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "错误信息：" + object.getString("error"));
        }
        //处理结果
        Integer total = UqlQureyResult.getTotal(result);
//        JSONArray data = UqlQureyResult.getResultData(result, activeId,refActiveIds);

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
            if(val.contains(".")){
               try {
                   val = String.format("%.2f",  Double.parseDouble(val));
               }catch (Exception ep){}
            }
            data.add(e.fluentPut(activeId, val));
        });

        List<String> pasSn = new ArrayList<>();
        int size = data == null ? 0 : data.size();
        Map<String, JSONObject> dataMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            JSONObject tmpObj = data.getJSONObject(i);
            pasSn.add(tmpObj.getString("PATIENT_SN"));
            dataMap.put(tmpObj.getString("PATIENT_SN"), tmpObj);
        }
        String patSnWhere = "";
        if(pasSn.size()==0){
            patSnWhere = "visitinfo.PATIENT_SN IN ('')";
        }else {
            patSnWhere = "visitinfo.PATIENT_SN " + TransPatientSql.transForExtContain(pasSn);
        }
        LOG.info("处理结果"+(System.currentTimeMillis()-tmie2));
        Long tmie3 = System.currentTimeMillis();
        for (int i = 0; i < refSize; i++) {
            //拼接column
            String refActiveId = refActiveIds.getString(i);
            List<ActiveSqlMap> patSqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, refActiveId.substring(1),groupId);
            if(patSqlList == null || patSqlList.size() == 0 ){
                referenceCalculate(refActiveId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get(crfId),patientSetId,groupId,null,crfId);
                patSqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, refActiveId,groupId);
            }
            ActiveSqlMap patActiveSqlMap = patSqlList.get(0);
            String patSql = patActiveSqlMap.getUncomActiveSql();
            String[] patSqls = patSql.split("where");
            String where = patSqls[1];
            String newWhere = patSnWhere + " and " + where;
            String patSnResult = querySearch(projectId, patSqls[0] + " where "+ newWhere, 1, pageSize, sourceFilter, source, crfId);
            JSONArray tmpHits = UqlQureyResult.getHitsArray(patSnResult);
            int tmpHitsSize = tmpHits.size();
            for (int j = 0; j < tmpHitsSize; j++) {
                String colle = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONObject("select_field").getString("condition");
                if(colle.contains(".")){
                    try {
                        colle = String.format("%.2f",  Double.parseDouble(colle));
                    }catch (Exception e){
                    }
                }
                String patSn = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0).getJSONArray("patient_basicinfo").getJSONObject(0).getString("PATIENT_SN");
                dataMap.get(patSn).put(refActiveId, colle);
            }
            for(String key : dataMap.keySet()){
                JSONObject obj = dataMap.get(key);
                if(!obj.containsKey(refActiveId)){
                    obj.put(refActiveId,"-");
                }
            }
        }
        LOG.info("处理引用结果"+(System.currentTimeMillis()-tmie3));

        Integer count = 0 ;

        if(StringUtils.isEmpty(groupFromId) && (patientSetId ==null || patientSetId.size()==0) && StringUtils.isNotEmpty(groupId)){
            groupFromId = groupMapper.getGroupParentId(groupId);
            if(StringUtils.isEmpty(groupFromId)){
                List<String> patSetIds = groupPatDataMapper.getPatSetByGroupId(groupId);
                patientSetId = JSONArray.parseArray(JSON.toJSONString(patSetIds));
            }
        }
        //获取总共人数
        if(patientSetId !=null && patientSetId.size()>0){
            count = getPatientSqlCount(patientSetId,projectId,crfId);
        }else {
            count = getGroupSqlCount(groupFromId);
        }

        saveActiveIndexTask(activeId, projectId, total);
//        Integer count = getProjectCount(projectId, crfId);
        AjaxObject.getReallyDataValue(data,basicColumns);
        ajaxObject.setCount(count);
        ajaxObject.setWebAPIResult(new WebAPIResult<Object>(pageNum, pageSize, total));
        ajaxObject.setColumns(basicColumns);
        ajaxObject.setData(data);

        return ajaxObject;
    }

    public String querySearch(String projectId, String newSql, Integer pageNum, Integer pageSize, String sourceFilter, JSONArray source,String crfId) {
        Long startTime = System.currentTimeMillis();
        QuerySearch querySearch = new QuerySearch();
        querySearch.setIndexName(PROJECT_INDEX_NAME_PREFIX.get(crfId) + projectId);//换成 projectId
        querySearch.setQuery(newSql);
        querySearch.setPage(pageNum);
        querySearch.setSize(pageSize);
        querySearch.setSource_filter(sourceFilter);
        querySearch.setSource(source);
        String url = httpUtils.getEsSearchUql();
        String param = JSON.toJSONString(querySearch);
        String result = httpUtils.httpPost(param, url);
        LOG.info("搜索 --消耗时间为："+(System.currentTimeMillis() - startTime));
        LOG.info("访问 uql 查询param: " + param);
        JSONObject data = JSON.parseObject(result);
        Object error = data.get("error");
        if (error != null) {
            LOG.error("发生异常了： " + error);
            LOG.error("参数为： " + param);
        }
        return result;
    }

    private void saveActiveIndexTask(String activeId, String projectId, Integer total) {
        ActiveIndexTask activeIndexTask = new ActiveIndexTask();
        activeIndexTask.setId(StringUtils.getUUID());
        activeIndexTask.setActiveIndexId(activeId.replaceAll("_tmp", ""));
        activeIndexTask.setProjectId(projectId+"_tmp");
        activeIndexTask.setSubmitTime(new Date());
        activeIndexTask.setStatus(1);
        activeIndexTask.setMessage("计算完成");
        activeIndexTask.setComplateTime(new Date());
        activeIndexTask.setSearchResult(total);
        activeIndexTaskMapper.insert(activeIndexTask);
    }

    private void saveExcludeTask(String activeId, String projectId, Integer total) {
        ActiveIndexTask activeIndexTask = new ActiveIndexTask();
        activeIndexTask.setId(StringUtils.getUUID());
        activeIndexTask.setActiveIndexId(activeId.replaceAll("_tmp", ""));
        activeIndexTask.setProjectId(projectId);
        activeIndexTask.setSubmitTime(new Date());
        activeIndexTask.setStatus(1);
        activeIndexTask.setMessage("计算完成");
        activeIndexTask.setComplateTime(new Date());
        activeIndexTask.setSearchResult(total);
        activeIndexTask.setMarketApply(total);
        activeIndexTaskMapper.insert(activeIndexTask);
    }

    @Override/*获取事件结果集*/
    public AjaxObject searchCalcResultByUql(String activeId, String projectId, JSONArray basicColumns, JSONArray visitColumns, Integer activeType, Integer pageNum, Integer pageSize, String activeResult, String crfId, String groupFromId, JSONArray patientSetId, String groupId) throws InterruptedException, IOException, ExecutionException {
        final AbstractFieldAnalyzer schema = SCHEMAS.get(crfId);
        List<ActiveSqlMap> sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        if(sqlList == null || sqlList.size() == 0 ){
            referenceCalculate(activeId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get(crfId),patientSetId,groupId,null,crfId);
            sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        }
        activeResult = activeIndexConfigMapper.getActiveResult(activeId.replaceAll("_tmp", ""));
        UqlClass joinUqlClass = new StandardUqlClass(projectId);
        ActiveSqlMap activeSqlMap = sqlList.get(0);
        String newSql = sqlList.get(0).getUncomActiveSql();
        JSONArray refActiveIds = JSONArray.parseArray(activeSqlMap.getRefActiveIds());//ids
        int refSize = refActiveIds == null ? 0 : refActiveIds.size();
        String sourceFilter = activeSqlMap.getSourceFiltere();
        for (int i = 0; i < refSize; i++) {
            String refActiveId = refActiveIds.getString(i);
            String sql = activeSqlMapMapper.getActiveSqlByActiveId(refActiveId.substring(1, refActiveId.length()),groupId);
            if(StringUtils.isEmpty(sql) ){
                referenceCalculate(refActiveId.substring(1, refActiveId.length()),projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get(crfId),patientSetId,groupId,null,crfId);
                sql = activeSqlMapMapper.getActiveSqlByActiveId(refActiveId.substring(1, refActiveId.length()),groupId);
            }
            joinUqlClass.setJoinValue(refActiveId, sql);
        }
        /*处理 检验子项 和检验报告命名方式*/
        String activeResultValue = activeSqlMap.getActiveResultValue();

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
        visitsPath = KeyPath.compile(visits);

        JSONArray source = new JSONArray();
        source.add("patient_info");
        /*增加 source value*/
        JSONArray sourceValue = JSONArray.parseArray(sqlList.get(0).getSourceValue());
        int sourceSize = sourceValue == null ? 0 : sourceValue.size();
        for (int i = 0; i < sourceSize; i++) {
            String sourceVal = sourceValue.getString(i);
            source.add(sourceVal);
        }
        String having ="";
        if( activeSqlMap.getUncomActiveSql().contains("having")){
            having = "having"+ activeSqlMap.getUncomActiveSql().split("having")[1];
        }
        String activeReuslt = activeSqlMap.getActiveResultDocId();
        String sql = "";
        String tmpSql = "";

        sql = "select " +activeReuslt +"as condition ,count("+visits +".DOC_ID) as jocount from " + activeSqlMap.getSqlFrom() +" where "+ activeSqlMap.getUncomSqlWhere() + " and "+visits+ ".DOC_ID is not null "+IndexContent.getGroupBy(crfId)+" "+having;

        String result = httpUtils.querySearch(projectId,sql,pageNum,pageSize,sourceFilter,source,crfId);
        /*处理结果*/
        Integer total = UqlQureyResult.getTotal(result);
        saveActiveIndexTask(activeId, projectId, total);
        JSONArray data = UqlQureyResult.getActiveVisitSn(result, activeId);

        String query =  getVisitSns(data);
        /*组装新的 uql搜索 搜索新的数据*/
        UqlClass uqlClass = new StandardUqlClass();
        uqlClass.setFrom(projectId,crfId);
        JSONArray array = new JSONArray();
        array = getSource(basicColumns, "patient_info.patient_basicinfo", array, schema);
        /*处理病案首页 手术问题*/

        String repeaceActive = resultVisit;
        array = getSource(visitColumns, repeaceActive, array, schema);

        int size = array == null ? 0 : array.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                uqlClass.setActiveSelect(uqlClass.getSelect() + ",");
            }
            uqlClass.setActiveSelect(uqlClass.getSelect() + array.getString(i));
        }

        uqlClass.setActiveSelect(uqlClass.getSelect() + ",visitinfo.DOC_ID");
        uqlClass.setActiveSelect(uqlClass.getSelect() + ",visitinfo.PATIENT_SN");
        uqlClass.setActiveSelect(uqlClass.getSelect() + ", patient_info.patient_basicinfo.DOC_ID");
        if (StringUtils.isEmpty(uqlClass.getWhere())) {
            uqlClass.setWhere(visits+".DOC_ID in (" + query + ")");
        } else {
            uqlClass.setWhere(uqlClass.getWhere() + "and "+visits+".DOC_ID in (" + query + ")");
        }
        /*查询docId*/
        JSONArray resultSource = new JSONArray();
        String resultJson = httpUtils.querySearch(projectId,uqlClass.getVisitsSql(),1,Integer.MAX_VALUE,null,resultSource, crfId);
        JSONArray dataObj = getActiveResultData(resultJson, basicColumns, visitColumns, repeaceActive,result,repeaceActive);

        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
        Integer count = 0;

        if(StringUtils.isEmpty(groupFromId) && (patientSetId ==null || patientSetId.size()==0) && StringUtils.isNotEmpty(groupId)){
            groupFromId = groupMapper.getGroupParentId(groupId);
            if(StringUtils.isEmpty(groupFromId)){
                List<String> patSetIds = groupPatDataMapper.getPatSetByGroupId(groupId);
                patientSetId = JSONArray.parseArray(JSON.toJSONString(patSetIds));
            }
        }
        if("1".equals(groupFromId) || "''".equals(groupFromId) || StringUtils.isEmpty(groupFromId)){
            count = getPatientSqlCount(patientSetId,projectId,crfId);
        }else {
            count = getGroupSqlCount(groupFromId);
        }
        ajaxObject.setCount(count);
        ajaxObject.setData(dataObj);
        WebAPIResult webAPIResult = new WebAPIResult(pageNum, pageSize, total);
        ajaxObject.setWebAPIResult(webAPIResult);
        return ajaxObject;
    }

    private JSONArray getActiveResultData(String resultJson, JSONArray basicColumns, JSONArray visitColumns, String key, String result,String visitsKey) {

        JSONArray resultHits = UqlQureyResult.getHitsArray(result);
        JSONArray hits = UqlQureyResult.getHitsArray(resultJson);
        JSONArray data = new JSONArray();
        int size = hits == null ? 0 : hits.size();
        int resultSize = resultHits == null ? 0 : resultHits.size();
        Map<String, JSONObject> patMap = new ConcurrentHashMap<>();
        Map<String,JSONObject> resultMap = new ConcurrentHashMap<>();
        Set<String> visSet = new HashSet<>();

        for (int i = 0; i < resultSize; i++) {
            String dockId = new KeyPath(i, "_source", "select_field", "condition").tryResolveAsString(resultHits);
            JSONObject patientInfo = new KeyPath(i, "_source", "patient_info", 0, "patient_basicinfo", 0).tryResolveAsJSONObject(resultHits);
            String patSn = patientInfo.getString("PATIENT_SN");
            /*处理结果*/
            JSONArray patientIn = transforActiveData(patientInfo,basicColumns);
            if(patMap.containsKey(patSn)){
                JSONObject object =resultMap.get(dockId);
                object.put("patient_inf",patientIn);
            }else {
                JSONObject object = new JSONObject();
                object.put("patient_inf",patientIn);
                patMap.put(patSn,object);
            }
        }
        for (int i = 0; i < size; i++) {
            String docId = hits.getJSONObject(i).getString("_id");
            Object visitObj = hits.getJSONObject(i).getJSONObject("_source").get("visitinfo");
            String visitSn = "";
            String patSn ="";
            String inspectionSn = "";
            if(visitObj instanceof JSONArray){
                 visitSn = ((JSONArray) visitObj).getJSONObject(0).getString("DOC_ID");
                 patSn = ((JSONArray) visitObj).getJSONObject(0).getString("PATIENT_SN");
            }else if(visitObj instanceof JSONObject){
                visitSn = ((JSONObject) visitObj).getString("DOC_ID");
                patSn = ((JSONObject) visitObj).getString("PATIENT_SN");
            }
            Object visitKeyObj = KeyPath.compile(visitsKey).fuzzyResolve(hits.getJSONObject(i).getJSONObject("_source"));

            if(visitKeyObj instanceof JSONArray){
                patSn = StringUtils.isNotEmpty (patSn) ? patSn: ((JSONArray) visitKeyObj).getJSONObject(0).getString("PATIENT_SN");
//                inspectionSn = ((JSONArray) visitKeyObj).getJSONObject(0).getString("INSPECTION_SN");
            }else if(visitKeyObj instanceof JSONObject){
                patSn = StringUtils.isNotEmpty (patSn) ? patSn:  ((JSONObject) visitKeyObj).getString("PATIENT_SN");
//                inspectionSn = ((JSONObject) visitKeyObj).getString("INSPECTION_SN");
            }
            Object visitsObj =  KeyPath.compile(key).fuzzyResolve(hits.getJSONObject(i).getJSONObject("_source"));

            JSONObject visits = getActieResultVisitObj(visitsObj);
            if(visits == null) continue;
            if("visitinfo".equals(key)  ){
                docId = visitSn;
            }

            if(visSet.contains(docId)){
                continue;
            }
            visSet.add(docId);
            visits.put("VISIT_SN",visitSn);
            /*处理结果*/
            JSONArray visit = transforActiveData(visits,visitColumns);
            visits.put("VISIT_SN",visitSn);

            if(patMap.containsKey(patSn)){
                JSONObject object =patMap.get(patSn);
                if(!object.containsKey("visit_info")){
                    JSONObject visitInfo = new JSONObject();
                    visitInfo.put("summarize",visit);
                    visitInfo.put("detail",new JSONArray());
                    object.put("visit_info",visitInfo);
                    patMap.put(patSn,object);
                }else {
                    JSONArray array = object.getJSONObject("visit_info").getJSONArray("summarize");
                    visit.forEach(x -> array.add(x));
                }
            }else {
                JSONObject object = new JSONObject();
                JSONObject visitInfo = new JSONObject();
                visitInfo.put("summarize",visit);
                visitInfo.put("detail",new JSONArray());
                object.put("visit_info",visitInfo);
                patMap.put(patSn,object);
            }
        }
        for (String keyTmp : patMap.keySet()){
            JSONObject object = patMap.get(keyTmp);
            if(!object.containsKey("visit_info")){
                JSONObject visitInfo = new JSONObject();
                JSONArray sumArray = new JSONArray();
                for (int i = 0; i < visitColumns.size(); i++) {
                    JSONObject sumObj = new JSONObject();
                    JSONObject tmpObj = visitColumns.getJSONObject(i);
                    sumObj.fluentPut("name",tmpObj.getString("id"))
                            .fluentPut("name_des",tmpObj.getString("name"))
                            .fluentPut("value","-");
                    sumArray.add(sumObj);
                }
                visitInfo.put("summarize",sumArray);
                visitInfo.put("detail",new JSONArray());
                object.put("visit_info",visitInfo);
            }
            data.add(object);
        }
        return data;
    }

    private JSONObject getActieResultVisitObj(Object visitsObj) {
        if(visitsObj instanceof JSONArray && ((JSONArray) visitsObj).size()>0){
           return getActieResultVisitObj(((JSONArray) visitsObj).get(0));
        }else if(visitsObj instanceof JSONObject){
           return (JSONObject) visitsObj;
        }else {
            return null;
        }
    }

    private JSONArray transforActiveData(JSONObject result, JSONArray showColumns){
        JSONArray data = new JSONArray();
        int size = showColumns ==null ? 0 :showColumns.size();
        for (int i = 0; i < size; i++) {
            JSONObject resObj = new JSONObject();
            JSONObject obj = showColumns.getJSONObject(i);
            String id = obj.getString("id");
            String name = obj.getString("name");
            String dmp = null;
            if(result.get(id) instanceof JSONArray){
                JSONArray array = result.getJSONArray(id);
                dmp = array.toJavaList(String.class).stream().collect(joining(" ; "));
            }else {
                dmp =  result.getString(id);
            }
            if(StringUtils.isEmpty(dmp)){
                dmp = "-";
            }
            resObj.put("name",id);
            resObj.put("name_des",name);
            resObj.put("value",dmp);
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

    @Autowired
    private GroupService groupService;

    @Override
    public AjaxObject searchCalcExculeByUql(String activeId, String projectId, Integer pageSize, Integer pageNum, JSONArray basicColumns, String crfId, String isExport, String groupId, String groupName, JSONArray patientSetId, String createId, String createName, String groupFromId, boolean autoExport) throws IOException, ExecutionException, InterruptedException {
        AjaxObject ajaxObject = new AjaxObject();
        List<ActiveSqlMap> sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        if(sqlList == null || sqlList.size() == 0 ){
            referenceCalculate(activeId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get(crfId),patientSetId,groupId,null,crfId);
            sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        }
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


        JSONArray source = new JSONArray();
        source.add("patient_info.patient_basicinfo");

        if("1".equals(isExport)){//处理导出数据
            String result =  httpUtils.querySearch(projectId,activeSqlMap.getUncomActiveSql(),pageNum,Integer.MAX_VALUE-1,activeSqlMap.getSourceFiltere(),source,crfId,false);
            Integer total = UqlQureyResult.getTotal(result);
            JSONArray data = UqlQureyResult.getResultData(result, activeId,refActiveIds,false,crfId);
            Map<String, JSONObject> dataMap = new HashMap<>();
            boolean flag = true;
            if(patientSetId == null){
                flag = groupService.exportToGroup(data,groupId,groupName,projectId,createId,createName,true,autoExport);
            }else {
                int size = patientSetId.size();
                String patients = "";
                for (int i = 0; i < size; i++) {
                    String id = patientSetId.getString(i);
                    groupService.exportToGroupById(data,groupId,groupName,id,projectId,createId,createName,true,autoExport);
                    if(StringUtils.isNotEmpty(patients)){
                        patients = patients + "," + patientsSetMapper.getpatientSetNameByPatSetId(id);
                    }else {
                        patients  = patientsSetMapper.getpatientSetNameByPatSetId(id);
                    }
                }
                //增加 移除患者列表
                Set<String> docIds = new KeyPath("hits", "hits", "_id")
                    .fuzzyResolve(JSON.parseObject(result))
                    .stream()
                    .map(String.class::cast)
                    .collect(toSet());
                String sql = getPatientSqlForIds(patientSetId,projectId,docIds,crfId);
                String removeQuery = "select "+ IndexContent.getPatientDocId(crfId) +" as pSn from "+ IndexContent.getIndexName(crfId, projectId)+" where "+sql+IndexContent.getGroupBy(crfId);;
                String re =  httpUtils.querySearch(projectId,removeQuery,pageNum,Integer.MAX_VALUE-1,activeSqlMap.getSourceFiltere(),source,crfId,false);
                JSONArray dataRe = UqlQureyResult.getResultData(re, activeId,refActiveIds,false,crfId);
                for (int i = 0; i < size; i++) {
                    String id = patientSetId.getString(i);
                    groupService.exportToRemoveGroup(dataRe,groupId,groupName,id,projectId,createId,createName,true,autoExport);
                }
                if(!autoExport){
                    String content = createName + "将患者集： " + patients + " 添加到组:" + groupName;
                    logUtil.saveLog(projectId, content, createId, createName);
                }
            }
            if(flag){
                AjaxObject ajaxObject1 = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,AjaxObject.AJAX_MESSAGE_SUCCESS);
                ajaxObject1.setFlag(true);
                return ajaxObject1;
            }else {
                AjaxObject ajaxObject1 = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,"与同层子组有重复患者，导入失败");
                ajaxObject1.setFlag(false);
                return ajaxObject1;
            }
        }
        //请求 获取数据
        String result =  httpUtils.querySearch(projectId,activeSqlMap.getUncomActiveSql(),pageNum,pageSize,activeSqlMap.getSourceFiltere(),source,crfId);
        JSONObject applyOutObj = JSONObject.parseObject( httpUtils.querySearch(projectId,activeSqlMap.getUncomActiveSql(),pageNum,Integer.MAX_VALUE-1,activeSqlMap.getSourceFiltere(),source,crfId,false));
        String applyCondition = getApplyCondition(applyOutObj,crfId);
        //处理结果
        Integer total = UqlQureyResult.getTotal(result);
        JSONArray data = UqlQureyResult.getResultData(result, activeId,refActiveIds, true,crfId);

        List<String> pasSn = new ArrayList<>();
        int size = data == null ? 0 : data.size();
        Map<String, JSONObject> dataMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            JSONObject tmpObj = data.getJSONObject(i);
            pasSn.add(tmpObj.getString("DOC_ID"));
            dataMap.put(tmpObj.getString("DOC_ID"), tmpObj);
        }
        String patSnWhere = "";
        if(pasSn.size()==0){
            patSnWhere = "patient_info.patient_basicinfo.DOC_ID IN ('')";
        }else {
            patSnWhere = "patient_info.patient_basicinfo.DOC_ID " +TransPatientSql.transForExtContain(pasSn);
        }

        for (int i = 0; i < refSize; i++) {
            //拼接column
            String refActiveId = refActiveIds.getString(i);
            List<ActiveSqlMap> patSqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, refActiveId,groupId);
            if(patSqlList == null || patSqlList.size() == 0 ){
                referenceCalculate(refActiveId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get(crfId),patientSetId,groupId,null,crfId);
                patSqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, refActiveId,groupId);
            }
            ActiveSqlMap patActiveSqlMap = patSqlList.get(0);
            String indexResultValue = patActiveSqlMap.getIndexResultValue();
            if(StringUtils.isNotEmpty(indexResultValue)){//枚举
                makeEnumResultData(patSqlList,patSnWhere,dataMap,projectId,pageSize,source,refActiveId,crfId);
            }else if("1".equals(patActiveSqlMap.getActiveType())){//事件
                String patSql = patActiveSqlMap.getUncomActiveSql();
                String[] patSqls = patSql.split("where");
                String where = patActiveSqlMap.getUncomSqlWhere()+IndexContent.getGroupBy(crfId);//patActiveSqlMap.getEventWhere() +"group by visitinfo.PATIENT_SN";// +" and join_field='visitinfo' ";
                String newWhere = patSnWhere+ " and " + where;
                String patSnResult = httpUtils.querySearch(projectId, patSqls[0] + " where "+ newWhere , 1, pageSize, "", source, crfId);
                JSONArray tmpHits = UqlQureyResult.getHitsArray(patSnResult);
                String activeResultDoc = patActiveSqlMap.getActiveResultDocId();
                boolean isFirst = activeResultDoc.startsWith("first") || activeResultDoc.startsWith("any") || activeResultDoc.startsWith("last");
                int tmpHitsSize = tmpHits.size();
                for (int j = 0; j < tmpHitsSize; j++) {
                    Long colle = new KeyPath(j, "_source", "select_field", "jocount").resolveAsLong(tmpHits);
                    String patSn = new KeyPath(j, "_source", "patient_info", 0, "patient_basicinfo", 0, "DOC_ID").resolveAsString(tmpHits);
                    if(isFirst){
                        dataMap.get(patSn).put(refActiveId, 1);
                    }else {
                        dataMap.get(patSn).put(refActiveId, colle);
                    }
                }
                for (String key : dataMap.keySet()){
                    JSONObject obj = dataMap.get(key);
                    if(!obj.containsKey(refActiveId)){
                        obj.put(refActiveId,0);
                    }
                }
            }else {//指标
                String patSql = patActiveSqlMap.getUncomActiveSql();
                String[] patSqls = patSql.split("where");
                String where = patSqls[1];
                String newWhere = patSnWhere + " and " + where;
                String patSnResult = querySearch(projectId, patSqls[0] + " where "+ newWhere, 1, pageSize, "", source, crfId);
                JSONArray tmpHits = UqlQureyResult.getHitsArray(patSnResult);
                int tmpHitsSize = tmpHits.size();
                for (int j = 0; j < tmpHitsSize; j++) {
                    Object colle = new KeyPath(j, "_source", "select_field", "condition").resolve(tmpHits);
                    String patSn = new KeyPath(j, "_source", "patient_info", 0, "patient_basicinfo", 0, "DOC_ID").resolveAsString(tmpHits);
                    dataMap.get(patSn).put(refActiveId, colle);
                }
                for (String key : dataMap.keySet()){
                    JSONObject obj = dataMap.get(key);
                    if(!obj.containsKey(refActiveId)){
                        obj.put(refActiveId,"-");
                    }
                }
            }

        }
        saveExcludeTask(activeId, projectId, total);
        Integer count = 0 ;
        //获取总共人数

        if(StringUtils.isEmpty(groupFromId) && (patientSetId ==null || patientSetId.size()==0) && StringUtils.isNotEmpty(groupId)){
            groupFromId = groupMapper.getGroupParentId(groupId);
            if(StringUtils.isEmpty(groupFromId)){
                List<String> patSetIds = groupPatDataMapper.getPatSetByGroupId(groupId);
                patientSetId = JSONArray.parseArray(JSON.toJSONString(patSetIds));
            }
        }

        if("1".equals(groupFromId) || "''".equals(groupFromId) || StringUtils.isEmpty(groupFromId)){
            count = getPatientSqlCount(patientSetId,projectId,crfId);
        }else {
            count = getGroupSqlCount(groupFromId);
        }
//        Integer count = getProjectCount(projectId,crfId);
        AjaxObject.getReallyDataValue(data,basicColumns);
        ajaxObject.setApplyOutCondition(applyCondition);
        ajaxObject.setCount(count);
        ajaxObject.setWebAPIResult(new WebAPIResult<Object>(pageNum, pageSize, total));
        ajaxObject.setColumns(basicColumns);
        ajaxObject.setData(data);

        return ajaxObject;

    }

    private String getApplyCondition(JSONObject applyOutObj) {
        Set<String> patients = new KeyPath("hits", "hits","_source","patient_info", "PATIENT_SN")
            .fuzzyResolve(applyOutObj)
            .stream()
            .map(String.class::cast)
            .collect(toSet());
        return  String.join(",",patients);
    }
    private String getApplyCondition(JSONObject applyOutObj,String crfId) {
        Set<String> patients ;
        if(StringUtils.isEmpty(crfId) || IndexContent.EMR_CRF_ID.equals(crfId)){
            patients = new KeyPath("hits", "hits","_source","patient_info", "PATIENT_SN")
                .fuzzyResolve(applyOutObj)
                .stream()
                .map(String.class::cast)
                .collect(toSet());
        }else {
            patients = new KeyPath("hits", "hits","_source","patient_info","patient_basicinfo", "PATIENT_SN")
                .fuzzyResolve(applyOutObj)
                .stream()
                .map(String.class::cast)
                .collect(toSet());
        }
        return  String.join(",",patients);
    }

    private String getPatientSqlForIds(JSONArray patientSetId,String projectId,Set<String> docIds,String crfId) {

        List<String> patientSets = patientSetId.toJavaList(String.class);
        List<String> patientSetSql = patientsSetMapper.getPatientsetSqlAll(patientSets);
        String query = String.join(" or ",patientSetSql.stream().map( x -> "("+TransPatientSql.getAllPatientSql(TransPatientSql.getUncomPatientSnSql(x),crfId)+")").collect(toList()));
        JSONArray sourceFilter = new JSONArray();
        sourceFilter.add(IndexContent.getPatientInfoPatientSn(crfId));
        String result = null;
        String newSql = "select  "+ IndexContent.getPatientDocId(crfId)+" as pSn from "+ IndexContent.getIndexName(crfId, projectId)+" where "+query+IndexContent.getGroupBy(crfId);
        String response = httpUtils.querySearch(projectId,newSql,1,Integer.MAX_VALUE-1,null,sourceFilter,crfId,true);
        Set<String> patients = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(JSON.parseObject(response))
            .stream()
            .map(String.class::cast)
            .collect(toSet());
        patients.removeAll(docIds);
        if (patients.isEmpty()) {
            result = "patient_info.patient_basicinfo.DOC_ID IN ('')";
        } else {
            result = "patient_info.patient_basicinfo.DOC_ID " +TransPatientSql.transForExtContain(patients);
        }
        return result;
    }

    private void makeEnumResultData(List<ActiveSqlMap> patSqlList, String patSnWhere, Map<String, JSONObject> dataMap, String projectId, Integer pageSize, JSONArray source, String refActiveId, String crfId) throws IOException {//处理枚举
        String isOtherName = "";
        for (ActiveSqlMap activeSqlMap : patSqlList){
            Integer isOther = activeSqlMap.getIsOther();
            if(isOther != null && isOther ==1){
                isOtherName = activeSqlMap.getIndexResultValue();
            }
        }
        Map<String,EnumResult> map = new HashMap<>();
        for (ActiveSqlMap activeSqlMap : patSqlList){
            String patSql = activeSqlMap.getUncomActiveSql();
            String where =activeSqlMap.getUncomSqlWhere();
            String newWhere = patSnWhere + " and (" + where+")";
            activeSqlMap.setUncomSqlWhere(newWhere);
            String sql = activeSqlMap.getUql(crfId);
            String patSnResult = httpUtils.querySearch(projectId, sql, 1, pageSize, "", source, crfId);
            JSONArray tmpHits = UqlQureyResult.getHitsArray(patSnResult);
            int tmpHitsSize = tmpHits.size();
            String indexResultValue = activeSqlMap.getIndexResultValue();
            for (int j = 0; j < tmpHitsSize; j++) {
                String patSn = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0).getJSONArray("patient_basicinfo").getJSONObject(0).getString("DOC_ID");;
                JSONObject obj = dataMap.get(patSn);
                if(!map.containsKey(patSn)){
                    EnumResult enumResult = new EnumResult();
                    map.put(patSn,enumResult);
                }
                if(StringUtils.isNotEmpty(indexResultValue) && indexResultValue.equals(isOtherName)){
                    map.get(patSn).repleaceAdd(indexResultValue);
                }
                if(!map.get(patSn).contain(isOtherName)){
                    map.get(patSn).add(indexResultValue);
                }

            }
        }
        for (Map.Entry<String,EnumResult> entry : map.entrySet()){
            JSONObject obj = dataMap.get(entry.getKey());
            obj.put(refActiveId,entry.getValue().toString());
        }
    }

    @Override
    public String searchByActive(JSONObject object, String resultOrderKey, Integer isSearch, String crfId) throws ExecutionException, InterruptedException, IOException {
        UqlClass uqlClass = null;
        final AbstractFieldAnalyzer schema = SCHEMAS.get(crfId);
        String id = object.getString("id");
        String activeType = object.getString("activeType");
        JSONArray patientSetId = object.getJSONArray("patientSetId");
        String groupFromId = object.getString("groupFromId");
        String isVariant = object.getString("isVariant");
        String groupToId = object.getString("groupToId");
        groupToId =StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId;
        //获取初筛 sql
        String projectId = object.getString("projectId").replaceAll("-", "");
        String patientSql =  getInitialSQL(groupFromId,isVariant,groupToId,patientSetId,projectId,crfId);

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
            activeResultDocId = FunctionUtilMap.getUqlFunction(operator, operaotrNum, visits+".DOC_ID", indexType, orderKey);
            uqlClass.setSelect(uqlClass.getSelect() + order);
            order1 = orderKey;
            if (schema.isPackagedField(order1)) {
                order1 = KeyPath.compile(order1).removeLast(2).stream().map(Object::toString).collect(joining("."));
            }
            uqlClass.setSqlHaving(operaotrNum);
            uqlClass.setResultValue("visitinfo.DOC_ID");
            uqlClass.setActiveSelect(uqlClass.getSelect() );
            uqlClass.setResultFunction(operator);
            uqlClass.setResultFunctionNum(operaotrNum);
        }
        UqlWhere where = new UqlWhere();

        String hasCount = getActiveHasCount(visits);
        uqlClass.setActiveSelect(uqlClass.getSelect() + hasCount);

        JSONObject contitionObj = contitions.getJSONObject(0);
        transforEnumCondition(contitionObj, uqlClass, where, T_activeIndexId, SCHEMAS.get(crfId),crfId,groupToId,projectId,patientSetId,patientSql);

        UqlClass sqlresult = null;
        String sqlMd5 = "";
        String allWhere = "";
        String eventWhere = "";
        String tmpVisits = order1.substring(0,order1.lastIndexOf("."));
        uqlClass.setVistSnWhere(where,tmpVisits,order1,schema);
        uqlClass.setWhereIsEmpty(where,"visitinfo.DOC_ID",null,null,schema);
        uqlClass.setNotAllWhere(operator,order1,null, null);
        uqlClass.setInitialPatients(isVariant,patientSql);
        where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
        uqlClass.setWhere(uqlClass.getWhereNotNull() + "(" + where.toString() + " ) ");
        boolean isGetVisisn = !where.isEmpty();
        String sqlNew = uqlClass.getHavingSql();
        /*开始搞 MD5 替换sql*/
        LOG.info("输出sql： " + sqlNew);
        sqlMd5 = StringToMd5.stringToMd5(sqlNew);
        Integer sqlMd5count = activeSqlMapMapper.getCountByActiveAndsqlMd5(R_activeIndexId,sqlMd5,groupToId);
        if(sqlMd5count>0) return null;

        allWhere = uqlClass.getWhere();
        //构造新sql 只有 visitSn 搜错功能
        if (StringUtils.isNotEmpty(activeResult) && isGetVisisn) {
            sqlresult = getIndexSql(uqlClass, operator, operaotrNum, activeResult, indexType, indexDate, projectId,hasCount,crfId);
        } else {
            sqlresult = uqlClass;
        }

        eventWhere = sqlresult.getWhere().contains("join_field") ?sqlresult.getWhere():sqlresult.getWhere()+" and join_field='visitinfo'";
        if(!"all".equals(operator) ){
            if("visitinfo".equals(visits)  ){
                if (order1.startsWith("visitinfo")) {
                    sqlresult.setWhere(sqlresult.getNotEmptyWhere()+ " AND " + order1 + " IS NOT NULL ");
                } else {
                    sqlresult.setWhere(sqlresult.getNotEmptyWhere()+ " AND " + " haschild( " + order1 + " IS NOT NULL ) ");
                }
            }else {
                sqlresult.setWhere(sqlresult.getNotEmptyWhere()+ " AND " + order1 + " IS NOT NULL");
            }
        }

        String andGroupCondition = getActiveGroupCondition(where,visits );
        if(StringUtils.isNotEmpty(andGroupCondition)){
            sqlresult.setWhere(sqlresult.getWhere() + " and ("+andGroupCondition+")");
        }
        sqlresult.setWhere(sqlresult.getWhere()+" AND join_field = '"+visits+"'");

        sqlresult.setSqlHaving(operaotrNum);
        String newSql = sqlresult.getHavingSql(crfId);

        Set<String> patients= getProjectPatients(projectId,patientSql,crfId);

        String allSql = "select patient_info.patient_basicinfo.DOC_ID from "+uqlClass.getFrom()+" where " +allWhere+" group by patient_info.patient_basicinfo.DOC_ID";
        String activeOtherPat = querySearch(projectId,allSql,1,Integer.MAX_VALUE-1,null,new JSONArray(),crfId);
//        Set<String> patients= getProjectPatients(projectId, crfId);
        Set<String> allPats = new KeyPath("hits", "hits")
            .flatFuzzyResolve(JSON.parseObject(activeOtherPat))
            .stream()
            .map(JSONObject.class::cast)
            .flatMap(o -> new KeyPath("_id").fuzzyResolve(o).stream())
            .map(String.class::cast)
            .collect(toSet());
        patients.removeAll(allPats);
        String otherResult = "";
        if (patients.isEmpty()) {
            otherResult = "";
        } else {
            otherResult =String.join("$",patients);
        }
        ActiveSqlMap activeSqlMap = new ActiveSqlMap();
        LOG.info("新的sql------------------： " + newSql);
        activeSqlMap.setProjectId(projectId);
        activeSqlMap.setActiveIndexId(R_activeIndexId);
        activeSqlMap.setActiveSql(GzipUtil.compress(newSql));
        activeSqlMap.setSqlSelect(sqlresult.getSelect());
        activeSqlMap.setSqlFrom(sqlresult.getFrom());
        activeSqlMap.setUncomSqlWhere(sqlresult.getWhere());
        activeSqlMap.setSourceFiltere(uqlClass.getSourceFilter());
        activeSqlMap.setActiveType(activeType);
        activeSqlMap.setRefActiveIds(uqlClass.getActiveId().toJSONString());
        activeSqlMap.setSourceValue(JSON.toJSONString(uqlClass.getSource()));
        activeSqlMap.setActiveResultDocId(activeResultDocId);
        activeSqlMap.setActiveResultValue(activeResult);
        activeSqlMap.setEventWhere(GzipUtil.compress(eventWhere));
        activeSqlMap.setSelectValue(hasCount.split(",")[1]);
        activeSqlMap.setActiveOtherResult(GzipUtil.compress(otherResult));
        activeSqlMap.setCountValue(hasCount.split(",")[1]);
        activeSqlMap.setSqlHaving(uqlClass.getHaving());
        activeSqlMap.setGroupId(StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId);
        int count = activeSqlMapMapper.getCountByActiveIndexId(R_activeIndexId,groupToId);
        if (count > 0) {
            activeSqlMapMapper.deleteByIndexId(R_activeIndexId);
        }
        activeSqlMapMapper.insert(activeSqlMap);
        /*引用依赖计算*/
//        getReferenceActiveIndex(id,resultOrderKey);
        return sqlresult.getCrfSql();
    }

    private String getActiveGroupCondition(UqlWhere where, String activeResult) {
        StringBuffer andGroupCondition = new StringBuffer();
        for (UqlWhereElem elem : where) {
            String s = elem.toString().trim();
            if (elem instanceof LiteralUqlWhereElem){
                andGroupCondition.append(" "+s.replaceAll("haschild","")+" ");
            }else if (elem instanceof SimpleConditionUqlWhereElem || elem instanceof ReferenceConditionUqlWhereElem) {
                if (s.startsWith(activeResult)) {
                    andGroupCondition.append(" "+s+" ");
                }else if (s.startsWith("haschild("+activeResult)) {
                    andGroupCondition.append(" " + s.substring(0,s.length()-1).replaceAll("haschild\\("," "));
                } else {
                    andGroupCondition.append("visitinfo.DOC_ID IS NOT NULL");
                }
            }
        }
        return andGroupCondition.toString();
    }

    private String getActiveHasCount(String visits) {
        return  " ,count(" + visits + ".DOC_ID) as jocount ";
    }

    @Override
    public String SearchByEnume(JSONObject obj, String resultOrderKey, Integer isSearch, String crfId) throws ExecutionException, InterruptedException, IOException {
        final AbstractFieldAnalyzer schema = SCHEMAS.get(crfId);
        UqlClass uqlClass = null;
        String projectId = obj.getString("projectId").replaceAll("-", "");
        String isVariant = obj.getString("isVariant");
        JSONArray configs = obj.getJSONArray("config");
        String groupFromId = obj.getString("groupFromId");
        String groupToId = obj.getString("groupToId");
        if( UqlConfig.CORT_INDEX_ID.equals(groupToId)){
            isVariant = "1";
        }
        groupToId =StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId;
        String name = obj.getString("name");
        JSONArray patientSetId = obj.getJSONArray("patientSetId");
        String activeIndexId = configs.getJSONObject(0).getString("activeIndexId");//指标id

        String R_activeIndexId =  isSearch == CommonContent.ACTIVE_TYPE_TEMP ? activeIndexId.concat("_tmp") : activeIndexId;
        String T_activeIndexId = "t" + activeIndexId;

        String patientSql = getInitialSQL(groupFromId,isVariant,groupToId,patientSetId,projectId,crfId);


//        List<ActiveSqlMap> activeSqlMaps = new ArrayList<>();
//        ActiveSqlMap otherActiveSqlMap = null;
//        UqlClass otherUql = null;
        JSONObject resultObj = JSONObject.parseObject(resultOrderKey);
//        Set<String> enumPatients = new HashSet<>();

        int size = configs == null ? 0 : configs.size();
        List<Group> groupList = groupMapper.getGroupListByProjectId(projectId);
        redisMapDataService.delete(UqlConfig.CORT_INDEX_REDIS_KEY.concat(R_activeIndexId));
        for (Group group : groupList){
            String groupId = group.getGroupId();
            List<ActiveSqlMap> delList = activeSqlMapMapper.getDelRedisActiveSql(R_activeIndexId);
            if(delList.size()>0){
                for (ActiveSqlMap src : delList){
                    redisMapDataService.delete(UqlConfig.CORT_CONT_ENUM_REDIS_KEY + src.getActiveIndexId() + "_" + src.getId() + "_" +groupId);
                }
            }
        }
        activeSqlMapMapper.deleteByActiveIndexId(R_activeIndexId,groupToId);
        List<ActiveSqlMap> activeSqlMaps = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            uqlClass = new CrfEnumUqlClass(projectId, crfId);
            uqlClass.setActiveSelect(" patient_info.patient_basicinfo.DOC_ID as pSn  ");
            JSONObject config = configs.getJSONObject(i);

            String indexType = config.getString("indexType");
            JSONArray contitions = config.getJSONArray("conditions");
            if(contitions == null || contitions.size()==0){
                continue;
            }
            String indexDate = resultObj.getString("visits.visitinfo");
            //普通指标
            String indexColumn = disposeVisits(config.getString("indexColumn"));
            String function = config.getString("function");
            String indexTypeValue = DataType.fromString(indexType).name();
            String indexResultValue = config.getString("indexResultValue");
            String functionParam = config.getString("functionParam");
            Integer isOther = config.getInteger("isOther");

            KeyPath indexDatePath = KeyPath.compile(indexDate);
            if (schema.isPackagedField(indexDate)) {
                indexDate = indexDatePath.removeLast(2).stream().map(Object::toString).collect(joining("."));
                indexDatePath = KeyPath.compile(indexDate);
            } else {
                indexDatePath = "visits".equals(indexDatePath.getFirst()) ? indexDatePath.keyPathByRemovingFirst() : indexDatePath;
                indexDate = indexDatePath.stream().map(Object::toString).collect(joining("."));
            }

            //指标处理
            UqlWhere where = new UqlWhere();
            JSONObject contitionObj = contitions.getJSONObject(0);

            transforEnumCondition(contitionObj, uqlClass, where, T_activeIndexId, SCHEMAS.get(crfId),crfId,groupToId,projectId,patientSetId, patientSql);
            uqlClass.setWhereIsEmpty(where,null,isVariant,patientSql, schema);

            String hasCount = " ,count(visitinfo.DOC_ID) as jocount ";
            uqlClass.setActiveSelect(uqlClass.getSelect() + hasCount);

            where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
            uqlClass.setWhere(uqlClass.getWhereNotNull() + " ( " + where.toString()+" ) ");

            String sqlNew = uqlClass.getCrfSql();
            LOG.info("输出sql： " + sqlNew);
//            String allWhere = uqlClass.getWhere();
            UqlClass sqlresult = null;
            if (StringUtils.isNotEmpty(indexColumn)) {
                sqlresult = getIndexSql(uqlClass, function, functionParam, indexColumn, indexType, indexDate, projectId,hasCount,crfId);
            } else {
                sqlresult = uqlClass;
            }
            String newSql = sqlresult.getCrfSql();
            LOG.info("新的sql： " + newSql);
            ActiveSqlMap activeSqlMap = new ActiveSqlMap();

            activeSqlMap.setProjectId(projectId);
            activeSqlMap.setActiveIndexId(R_activeIndexId);
            activeSqlMap.setActiveSql(GzipUtil.compress(newSql));
            activeSqlMap.setSqlSelect(sqlresult.getSelect());
            activeSqlMap.setSqlFrom(sqlresult.getFrom());
            activeSqlMap.setUncomSqlWhere(sqlresult.getWhere());
            activeSqlMap.setSourceFiltere(uqlClass.getSourceFilter());
            activeSqlMap.setRefActiveIds(uqlClass.getActiveId().toJSONString());
            activeSqlMap.setSourceValue(JSON.toJSONString(uqlClass.getSource()));
            activeSqlMap.setIndexResultValue(indexResultValue);
            activeSqlMap.setIndexTypeValue(indexTypeValue);
            activeSqlMap.setSelectValue("patient_info.patient_basicinfo.DOC_ID ");
            activeSqlMap.setGroupId(StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId);
            activeSqlMaps.add(activeSqlMap);
            Long mysqlStartTime = System.currentTimeMillis();
            Integer count = activeSqlMapMapper.getCountByActiveIdAndIndexValue(R_activeIndexId, indexResultValue,groupToId);
            if (count > 0) {
                activeSqlMapMapper.deleteByIndexId(R_activeIndexId);
            }
            activeSqlMapMapper.insert(activeSqlMap);
            LOG.info("数据库用时 :  "+(System.currentTimeMillis()-mysqlStartTime));
        }
        if(StringUtils.isEmpty(groupToId) || UqlConfig.CORT_INDEX_ID.equals(groupToId)){
            SingleExecutorService.getInstance().getFlushCountGroupExecutor().submit(() -> {
                try {
                    searchByuqlService.saveEnumCortrastiveResultRedisMap(activeSqlMaps,projectId,"EMR",R_activeIndexId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        return uqlClass.getCrfSql();
    }

    public Set<String> getProjectPatients(String projectId,String patientSql, String crfId) {
        String sql = "select  patient_info.patient_basicinfo.DOC_ID as pSn from " + PROJECT_INDEX_NAME_PREFIX.get(crfId) + projectId + " where "+patientSql+ " group by patient_info.patient_basicinfo.DOC_ID";
        JSONArray source = new JSONArray();
        String result = querySearch(projectId,sql,1,Integer.MAX_VALUE-1,null,source,crfId);
        Set<String> patients = new KeyPath("hits", "hits")
            .flatFuzzyResolve(JSON.parseObject(result))
            .stream()
            .map(JSONObject.class :: cast)
            .flatMap(o -> new KeyPath("_id").fuzzyResolve(o).stream())
            .map(String.class::cast)
            .collect(toSet());
        return patients;
    }

    private void transforEnumCondition(JSONObject contitionObj, UqlClass uqlClass, UqlWhere where, String activeIndexId,
                                       AbstractFieldAnalyzer schema, String crfId, String groupId, String projectId, JSONArray patientSetId, String patientSql) throws IOException, ExecutionException, InterruptedException {
        String operatorSign = contitionObj.getString("operatorSign");
        JSONArray details = contitionObj.getJSONArray("details");
        JSONArray inner = contitionObj.getJSONArray("inner");
        JSONObject sortObj = sortForDetailAndInner(details,inner);
        details = sortObj.getJSONArray("detail");
        inner = sortObj.getJSONArray("inner");
        transforEnumDetails(details, uqlClass, operatorSign, where, activeIndexId, schema,crfId,groupId,projectId,patientSetId,patientSql);
        int innerSize = inner == null ? 0 : inner.size();
        for (int i = 0; i < innerSize; i++) {
            if(where.needsOperator()){
                where.addElem(new LiteralUqlWhereElem(operatorSign));
            }
            where.addElem(new LiteralUqlWhereElem("("));
            JSONObject tmpObj = inner.getJSONObject(i);
            transforEnumCondition(tmpObj, uqlClass, where, activeIndexId, schema,crfId,groupId,projectId,patientSetId, patientSql);
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
            if("undefined".equals(before)){
                newDetails.add(obj);
                afterTmp = obj.getString("after");
            }
        }
        for (int i = 0; i < inner.size(); i++) {
            JSONObject obj = inner.getJSONObject(i);
            String before = obj.getString("before");
            if("undefined".equals(before)){
                newInner.add(obj);
                afterTmp = obj.getString("after");
            }
        }
        //依次往下找
        sort: while (true){
            if(StringUtils.isEmpty(afterTmp) || afterTmp.equals("undefined")){
                break sort;
            }
            for (int i = 0; i < details.size(); i++) {
                JSONObject obj = details.getJSONObject(i);
                String uuid = obj.getString("uuid");
                if(afterTmp.equals(uuid)){
                    newDetails.add(obj);
                    afterTmp = obj.getString("after");
                    continue sort;
                }
            }
            for (int i = 0; i < inner.size(); i++) {
                JSONObject obj = inner.getJSONObject(i);
                String uuid = obj.getString("uuid");
                if(afterTmp.equals(uuid)){
                    newInner.add(obj);
                    afterTmp = obj.getString("after");
                    continue sort;
                }
            }
            break ;
        }
        result.put("detail",newDetails);
        result.put("inner",newInner);
        return result;
    }
    private void transforEnumDetails(JSONArray details, UqlClass uqlClass, String operatorSign, UqlWhere where, String activeIndexId,
                                     AbstractFieldAnalyzer schema, String crfId, String groupId, String projectId, JSONArray patientSetId, String patientSql) throws IOException, ExecutionException, InterruptedException {
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
            transforEnumDetailModel(detailObj, detailOperatorSign, elems, uqlClass, activeIndexId, schema, crfId,groupId,projectId,patientSetId,patientSql);
            if (strongRefSize > 0) {
                elems.add(new LiteralUqlWhereElem("AND"));
//                where.addElem(new LiteralUqlWhereElem("("));
                transforEnumStrongRef(strongRef, strongRefSize, uqlClass, elems, activeIndexId, schema, crfId, groupId,projectId,patientSetId,patientSql);
                elems.add(new LiteralUqlWhereElem(")"));
            }
            elemLists.add(elems);
        }
        elemLists.stream()
            .map(list -> makePair(
                topGroup((
                    list.get(0) instanceof ReferenceConditionUqlWhereElem ?
                        ((ReferenceConditionUqlWhereElem)list.get(0)).group() :
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
                    Map<String,SimpleConditionUqlWhereElem> simpleMap = new HashMap<>();
                    List<UqlWhereElem> newElems = new ArrayList<>();
                    if("or".equals(operatorSign) ){
                        for (UqlWhereElem elem : elems){
                            if(elem instanceof SimpleConditionUqlWhereElem &&  ((SimpleConditionUqlWhereElem) elem).getJsonType().equals("string")){
                                SimpleConditionUqlWhereElem elem1 = (SimpleConditionUqlWhereElem) elem;
                                if(elem1.getValue().contains("(") || elem1.getValue().contains(")")){
                                    newElems.clear();
                                    newElems = elems;
                                    break;
                                }
                                if(!simpleMap.containsKey(elem1.getSourceTagName())){
                                    simpleMap.put(elem1.getSourceTagName(),elem1);
                                    newElems.add(elem);
                                }else{
                                    newElems.remove(newElems.size()-1);
                                    simpleMap.get(elem1.getSourceTagName()).updateStr(elem1.getValue());
                                    int i=1;
                                }
                            }else {
                                if(elem.toString().equals("(") || elem.toString().equals(")")){
                                    newElems.clear();
                                    newElems = elems;
                                    break;
                                }
                                newElems.add(elem);
                            }
                        }
                    }else {
                        newElems = elems;
                    }
                    if(newElems.size() > 0 && newElems.get(0) instanceof SimpleConditionUqlWhereElem){
                       SimpleConditionUqlWhereElem sim = (SimpleConditionUqlWhereElem)newElems.get(0);
                       if (schema.isPackagedField(sim.getSourceTagName())){
                           where.addElem(new LiteralUqlWhereElem("haschild("));
                           where.addElems(newElems);
                           where.addElem(new LiteralUqlWhereElem(")"));
                       }else {
                           addActiveElemsHasChild(where,newElems,schema,operatorSign);
                       }
                    }else {
                        addActiveElemsHasChild(where,newElems,schema,operatorSign);
//                        where.addElem(new LiteralUqlWhereElem("haschild("));
//                        where.addElems(newElems);
//                        where.addElem(new LiteralUqlWhereElem(")"));
                    }
                }
            });
    }

    private void sbcs(UqlWhere where, List<UqlWhereElem> newElems, AbstractFieldAnalyzer schema,String operatorSign){

        for (UqlWhereElem elem : newElems){
            if(elem instanceof SimpleConditionUqlWhereElem ){
                SimpleConditionUqlWhereElem elem1 = (SimpleConditionUqlWhereElem) elem;

            }else if(elem instanceof ReferenceConditionUqlWhereElem ) {
                ReferenceConditionUqlWhereElem elem1 = (ReferenceConditionUqlWhereElem) elem;

            }else {

            }
        }
    }

    private void addActiveElemsHasChild(UqlWhere where, List<UqlWhereElem> newElems, AbstractFieldAnalyzer schema, String operatorSign) {
        Map<String,List<UqlWhereElem>> elems = new HashMap<>();
        String tmpkey = "";
        String judgehao = "";
        String haoKey = "";
        boolean judgeoera = false;
        for (UqlWhereElem elem : newElems){
            if(elem instanceof SimpleConditionUqlWhereElem ){
                SimpleConditionUqlWhereElem elem1 = (SimpleConditionUqlWhereElem) elem;
                String sourceTagName =elem1.getSourceTagName();
                String key =sourceTagName.substring(0, sourceTagName.lastIndexOf('.'));
                tmpkey = sourceTagName;
                if (!elems.containsKey(key)){
                    elems.put(key,new ArrayList<UqlWhereElem>());
                }
                if("(".equals(judgehao) && schema.isPackagedField(sourceTagName)){
                    elems.get(key).add(new LiteralUqlWhereElem(judgehao));
                    judgehao = "";
                    haoKey = key;
                }
                elems.get(key).add(elem);
                if(judgeoera){
                    elems.get(key).add(new LiteralUqlWhereElem("AND"));
                }else {
                    elems.get(key).add(new LiteralUqlWhereElem(operatorSign));
                }
            }else if(elem instanceof ReferenceConditionUqlWhereElem ) {
                ReferenceConditionUqlWhereElem elem1 = (ReferenceConditionUqlWhereElem) elem;
                String key = elem1.group();
                tmpkey = elem1.getField();
                if (!elems.containsKey(key)){
                    elems.put(key,new ArrayList<UqlWhereElem>());
                }
                if("(".equals(judgehao)){
                    elems.get(key).add(new LiteralUqlWhereElem(judgehao));
                    haoKey = key;
                    judgehao = "";
                }
                elems.get(key).add(elem);
//                if(")".equals(judgehao)){
////                    elems.get(key).add(new LiteralUqlWhereElem(judgehao));
////                    judgehao = "";
////                }
                if(judgeoera){
                    elems.get(key).add(new LiteralUqlWhereElem("AND"));
                }else {
                    elems.get(key).add(new LiteralUqlWhereElem(operatorSign));
                }
            }else {
                if(elem.toString().equals("(")){
                    judgeoera = true;
                    judgehao = "(";
                }
                if(elem.toString().equals(")") && StringUtils.isNotEmpty(haoKey) ){
                    judgeoera = false;
                    elems.get(haoKey).add(new LiteralUqlWhereElem(")"));
                }
            }
        }
        if(elems.keySet().size() ==1 ){
            if(schema.isPackagedField(tmpkey)){
                where.addElem(new LiteralUqlWhereElem("haschild("));
                where.addElems(newElems);
                where.addElem(new LiteralUqlWhereElem(")"));
                return;
            }else {
                where.addElems(newElems);
                return;
            }
        }
        Set<String> judgeKey = new HashSet<>();
        for (UqlWhereElem elem : newElems){
            if(elem instanceof SimpleConditionUqlWhereElem ){
                SimpleConditionUqlWhereElem elem1 = (SimpleConditionUqlWhereElem) elem;
                String sourceTagName =elem1.getSourceTagName();
                String key =sourceTagName.substring(0, sourceTagName.lastIndexOf('.'));
                if(judgeKey.contains(key)) continue;
                judgeKey.add(key);
                if (schema.isPackagedField(sourceTagName)){
                    where.addElem(new LiteralUqlWhereElem("haschild("));
                    if(elems.get(key).get(elems.get(key).size()-1).toString().equals(")")){
                        elems.get(key).remove(elems.get(key).size()-2);
                        if(elems.get(key).get(elems.get(key).size()-2).toString().equals("AND")){
                            elems.get(key).remove(elems.get(key).size()-2);
                        }
                    }else {
                        elems.get(key).remove(elems.get(key).size()-1);
                    }
                    where.addElems(elems.get(key));
                    where.addElem(new LiteralUqlWhereElem(")"));
                }else {
                    elems.get(key).remove(elems.get(key).size()-1);
                    where.addElems(elems.get(key));
                }
                if(judgeKey.size() < elems.size()){
                    where.addElem(new LiteralUqlWhereElem("AND"));
                }
            }else if( elem instanceof ReferenceConditionUqlWhereElem ) {
                ReferenceConditionUqlWhereElem elem1 = (ReferenceConditionUqlWhereElem) elem;
                String key = elem1.group();
                if(judgeKey.contains(key)) continue;
                judgeKey.add(key);
                if (schema.isPackagedField(elem1.getField())){
                    where.addElem(new LiteralUqlWhereElem("haschild("));
                    if(elems.get(key).get(elems.get(key).size()-1).toString().equals(")")){
                        elems.get(key).remove(elems.get(key).size()-2);
                    }else {
                        elems.get(key).remove(elems.get(key).size()-1);
                    }
                    where.addElems(elems.get(key));
                    where.addElem(new LiteralUqlWhereElem(")"));
                }else {
                    if(elems.get(key).get(elems.get(key).size()-1).toString().equals(")")){
                        elems.get(key).remove(elems.get(key).size()-2);
                        if(elems.get(key).get(elems.get(key).size()-2).toString().equals("AND")){
                            elems.get(key).remove(elems.get(key).size()-2);
                        }
                    }else {
                        elems.get(key).remove(elems.get(key).size()-1);
                    }
                    where.addElems(elems.get(key));
                }
                if(judgeKey.size() < elems.size()){
                    where.addElem(new LiteralUqlWhereElem("AND"));
                }
            } else  {
                if("AND".equals(elem.toString()) || "and".equals(elem.toString()) || elem.toString().equals(operatorSign)){
                    if(elem.toString().equals(operatorSign) && ")".equals(where.getLastElem()) ){
                        where.addElem(elem);
                        continue;
                    }else {
                        continue;
                    }
                }
                if(")".equals(elem.toString()) && ("AND".equals(where.getLastElem()) || "and".equals(where.getLastElem()) || operatorSign.equals(where.getLastElem()) ) ){
                    where.removeElem();
                }
                if("(".equals(elem.toString()) && !where.isEmpty() &&")".equals(where.getLastElem())){
                    where.addElem(new LiteralUqlWhereElem(operatorSign));
                }
                if(")".equals(elem.toString()) && "(".equals(where.getLastElem())){
                    while (where.getLastElems() instanceof LiteralUqlWhereElem){
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
                                       String activeIndexId, AbstractFieldAnalyzer schema, String crfId, String groupId, String projectId, JSONArray patientSetId, String patientSql) throws IOException, ExecutionException, InterruptedException {
        for (int i = 0; i < strongRefSize; i++) {
            if(i>0) elems.add(new LiteralUqlWhereElem("AND"));
            JSONObject tmpObj = strongRef.getJSONObject(i);
            transforEnumDetailModel(tmpObj, "and", elems, uqlClass, activeIndexId, schema ,crfId,groupId,projectId,patientSetId, patientSql);
        }
    }

    private void transforEnumDetailModel(JSONObject detailObj, String detailOperatorSign, List<UqlWhereElem> elems, UqlClass uqlClass,
                                         String activeIndexId, AbstractFieldAnalyzer schema, String crfId, String groupId, String projectId, JSONArray patientSetId, String patientSql) throws IOException, ExecutionException, InterruptedException {
        StringBuffer stringBuffer = new StringBuffer();
        String stitching = detailObj.getString("Stitching");
        if (StringUtils.isEmpty(stitching)) {
            stitching = detailObj.getString("operatorSign");
        }
        String sourceName = detailObj.getString("sourceTagName");
        if(StringUtils.isEmpty(sourceName)) return;
        String sourceTagName = disposeVisits(sourceName);
        if (schema.isPackagedField(sourceTagName)) {
            sourceTagName = KeyPath.compile(sourceTagName).removeLast(2).stream().map(Object::toString).collect(joining("."));
        }
        String value = detailObj.getString("value");
        String refActiveId = detailObj.getString("refActiveId");
        String condition = ConditionUtilMap.getCondition(stitching);
        String jsonType = detailObj.getString("jsonType");
        if (StringUtils.isNotEmpty(refActiveId)) { // 引用数据
            List<ActiveSqlMap> activeSqlMaps = activeSqlMapMapper.getActiveSql(refActiveId,groupId);
            if(activeSqlMaps ==null || activeSqlMaps.size()==0 ){
                referenceCalculate(refActiveId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get(crfId),patientSetId,groupId,null,crfId);
                activeSqlMaps = activeSqlMapMapper.getActiveSql(refActiveId,groupId);
            }
            ActiveSqlMap activeSqlMap = activeSqlMaps.get(0);
            String activeId = activeSqlMap.getRefActiveIds();
//            uqlClass.addActiveId(activeId);
            String sql = activeSqlMap.getUncomActiveSql();
            refActiveId = "t" + refActiveId;
            uqlClass.getEnumOther().add(activeSqlMap.getUncomSqlWhere());
            uqlClass.setJoinValue(refActiveId, sql);
            uqlClass.addActiveId(refActiveId);
            String sourceValue;
            //同期增加 all_value   all_value(visitinfo.DOC_ID,diagnose.DIAGNOSTIC_DATE)
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
            elems.add(new ReferenceConditionUqlWhereElem(sql, singletonList(sourceValue), sourceTagName, uqlClass.getFrom(), detailOperatorSign, true,crfId,patientSql));
            return;
        }

        if ("boolean".equals(jsonType)) {
            disposeBooleanCondition(stringBuffer, value, condition, sourceTagName);
            String result = stringBuffer.toString();
            elems.add(new SimpleConditionUqlWhereElem(result, detailOperatorSign,jsonType,sourceTagName,condition,value));
            return;
        }
        if ("between".equals(condition)) {
            disposeDataCondition(value, condition, sourceTagName, stringBuffer);
            String result = stringBuffer.toString();
            elems.add(new SimpleConditionUqlWhereElem(result, detailOperatorSign,jsonType,sourceTagName,condition,value));
            return;
        }
        value = disposeArrayByContain(value, condition);
        condition = disposeConditionByContain(value, condition);
        value = disposeValue(sourceTagName,value,jsonType,schema,condition);
        stringBuffer.append(TransData.transDataNumber(sourceTagName));
        stringBuffer.append(" " + condition);
        stringBuffer.append(" " + value);
        String result = stringBuffer.toString();
        elems.add(new SimpleConditionUqlWhereElem(result, detailOperatorSign,jsonType,sourceTagName,condition,value));

    }

    @Override
    public String SearchByExclude(JSONObject object, String resultOrderKey, Integer isSearch, String crfId) throws ExecutionException, InterruptedException, IOException {
        final AbstractFieldAnalyzer schema = SCHEMAS.get(crfId);
        String projectId = object.getString("projectId").replaceAll("-", "");
        JSONArray patientSetId = object.getJSONArray("patientSetId");
        String groupFromId = object.getString("groupFromId");
        String isVariant = object.getString("isVariant");
        String groupToId = object.getString("groupToId");
        groupToId =StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId;
        String activeIndexId = object.getString("id");
        JSONArray config = object.getJSONArray("config");

        String patientSql = getInitialSQL(groupFromId,isVariant,groupToId, patientSetId,projectId,crfId);
        UqlWhere where = new UqlWhere();
        UqlClass uqlClass = new CrfExcludeUqlClass(projectId, crfId);
        transforConditionForConfig(config, uqlClass, where, schema,crfId,groupToId,projectId,patientSetId);
        uqlClass.setInitialPatients(isVariant,patientSql);
        ActiveSqlMap activeSqlMap = new ActiveSqlMap();
        activeIndexId = isSearch == CommonContent.ACTIVE_TYPE_TEMP ? activeIndexId.concat("_tmp") : activeIndexId;
        activeSqlMap.setProjectId(projectId);
        activeSqlMap.setActiveIndexId(activeIndexId);
        activeSqlMap.setActiveSql(GzipUtil.compress(uqlClass.getCrfSql()));
        activeSqlMap.setSqlSelect(uqlClass.getSelect());
        activeSqlMap.setSqlFrom(uqlClass.getFrom());
        activeSqlMap.setUncomSqlWhere(uqlClass.getWhere());
        activeSqlMap.setRefActiveIds(uqlClass.getActiveId().toJSONString());
        activeSqlMap.setSourceValue(JSON.toJSONString(uqlClass.getSource()));
        activeSqlMap.setGroupId(StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId);
        int count = activeSqlMapMapper.getCountByActiveIndexId(activeIndexId,groupToId);
        if (count > 0) {
            activeSqlMapMapper.updateByActiveId(activeSqlMap);
        } else {
            activeSqlMapMapper.insert(activeSqlMap);
        }
        return uqlClass.getCrfSql();
    }

    private void transforConditionForConfig(JSONArray config, UqlClass uqlClass, UqlWhere where, AbstractFieldAnalyzer schema,String crfId,String groupId,String projectId,JSONArray patientsSetId) throws InterruptedException, ExecutionException {
        StringBuffer resultBuffer = new StringBuffer();
        JSONObject incs = config.stream().map(JSONObject.class::cast).filter(o -> "纳入标准".equals(o.getString("activeResult"))).findAny().get();
        JSONObject excs = config.stream().map(JSONObject.class::cast).filter(o -> "排除标准".equals(o.getString("activeResult"))).findAny().get();
        transforConditionForConditions(incs.getJSONArray("conditions"), uqlClass, where, false, true, true,false, schema,crfId,groupId,projectId,patientsSetId);
        transforConditionForConditions(excs.getJSONArray("conditions"), uqlClass, where, true, where.isEmpty(), true,true, schema,crfId,groupId,projectId,patientsSetId);
        where.addElem(new LiteralUqlWhereElem("AND join_field='patient_info'"));
        where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
        uqlClass.setWhere(where.toString());
        uqlClass.setSelect("patient_info.patient_basicinfo.DOC_ID");
    }
    //有个 not ( ()) 因为 在inner 里 所以传 not 的地方需要在增加一个变量
    private void transforConditionForConditions(JSONArray conditions, UqlClass uqlClass, UqlWhere where, boolean not, boolean first,
                                                boolean op , boolean isNot, AbstractFieldAnalyzer schema,String crfId,String groupId,String projectId,JSONArray patientsSetId) {
        String opStr = op ? "AND" : "OR";
        // noinspection SuspiciousToArrayCall
        for (JSONObject condition: conditions.toArray(new JSONObject[0])) {
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
                transforConditionForConditions(condition.getJSONArray("inner"), uqlClass, where, false, true, subOp,isNot,schema,crfId,groupId,projectId,patientsSetId);
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
                    List<ActiveSqlMap> activeSql = activeSqlMapMapper.getActiveSql(refId,groupId);
                    if(activeSql ==null || activeSql.size()==0 ){
                        try {
                            referenceCalculate(refId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get(crfId),patientsSetId,groupId,null,crfId);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        activeSql = activeSqlMapMapper.getActiveSql(refId,groupId);
                    }
                    if (isEnum) {
                        // 枚举只可能是指标
                        JSONArray values = o.getJSONArray("value");
                        activeSql = activeSql.stream().filter(map -> values.contains(map.getIndexResultValue())).collect(toList());
                        if (activeSql.size() > 1) {
                            where.addElem(new LiteralUqlWhereElem("("));
                        }
                        if("!equal".equals(o.getString("operatorSign"))){
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
                        if(!where.isEmpty()){
                            where.removeElem();  // 去除的是最后一个"OR"
                        }
                        if("!equal".equals(o.getString("operatorSign"))){
                            where.addElem(new LiteralUqlWhereElem(")"));
                        }
                        if (activeSql.size() > 1) {
                            where.addElem(new LiteralUqlWhereElem(")"));
                        }
                    } else if("1".equals(activeSql.get(0).getActiveType())){
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
                    }else {
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
                    if(strongRef.size()>0){
                        where.addElem(new LiteralUqlWhereElem("("));
                        strongRef.forEach(obj ->{
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
                    if(strongRef.size()>0){
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
    public String SearchByIndex(JSONObject object, String resultOrderKey, Integer isSearch, String crfId) throws ExecutionException, InterruptedException, IOException {
        UqlClass uqlClass = null;
        final AbstractFieldAnalyzer schema = SCHEMAS.get(crfId);
        JSONArray patientSetId = object.getJSONArray("patientSetId");
        /*获取参数列表*/
        String name = object.getString("name");
        String id = object.getString("id");
        String groupFromId = object.getString("groupFromId");
        String isVariant = object.getString("isVariant");
        String groupToId = object.getString("groupToId");
        if( UqlConfig.CORT_INDEX_ID.equals(groupToId)){
            isVariant = "1";
        }
        groupToId =StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId;
        String projectId = object.getString("projectId").replaceAll("-", "");
        JSONObject config = object.getJSONArray("config").getJSONObject(0);

        String activeIndexId = config.getString("activeIndexId");//指标id
        String T_activeIndexId = isSearch == CommonContent.ACTIVE_TYPE_TEMP ? activeIndexId.concat("_tmp") : activeIndexId;
        String R_activeIndexId = "t" + activeIndexId;

        String indexType = config.getString("indexType");
        String indexTypeValue = DataType.fromString(indexType).name();
        JSONArray contitions = config.getJSONArray("conditions");
        String indexCol = config.getString("indexColumn").substring(0, config.getString("indexColumn").lastIndexOf("."));

        JSONObject resultObj = JSONObject.parseObject(resultOrderKey);

        String patientSql = getInitialSQL(groupFromId,isVariant,groupToId,patientSetId,projectId,crfId);


        String indexColumn = disposeVisits(config.getString("indexColumn"));
        String indexDate = disposeVisits(resultObj.getString(indexCol));
        KeyPath indexDatePath = KeyPath.compile(indexDate);
        if (schema.isPackagedField(indexDate)) {
            indexDate = indexDatePath.removeLast(2).stream().map(Object::toString).collect(joining("."));
            indexDatePath = KeyPath.compile(indexDate);
        } else {
            indexDatePath = "visits".equals(indexDatePath.getFirst()) ? indexDatePath.keyPathByRemovingFirst() : indexDatePath;
            indexDate = indexDatePath.stream().map(Object::toString).collect(joining("."));
        }

        String function = config.getString("function");
        String indexResultValue = config.getString("indexResultValue");
        String functionParam = config.getString("functionParam");
        String order1 = null;
        String selectValue = null;
        KeyPath visitsPath = KeyPath.compile(indexColumn);
        String visits = indexColumn;
        if (schema.isPackagedField(visits)) {
            visits = visitsPath.removeLast(2).firstAsString();
        } else {
            visits = "visitinfo";
        }
        visitsPath = KeyPath.compile(visits);
//        if( parts.length>1 && parts[0].equals("sub_inspection") ){
//            visits = "inspection_reports";
//        }
        //指标处理
        if (StringUtils.isNotEmpty(indexColumn)) {
            if (schema.isPackagedField(indexColumn)) {
                indexColumn = KeyPath.compile(indexColumn).removeLast(2).stream().map(Object::toString).collect(joining("."));
            }
            uqlClass = new CrfIndexUqlClass(projectId,crfId);
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
        transforEnumCondition(contitionObj, uqlClass, where, R_activeIndexId, schema,crfId,groupToId,projectId,patientSetId, patientSql);

        UqlClass sqlresult = null;
        String sqlMd5 = "";
        redisMapDataService.delete(UqlConfig.CORT_INDEX_REDIS_KEY.concat(T_activeIndexId));
        List<Group> groupList = groupMapper.getGroupListByProjectId(projectId);
        for (Group group : groupList){
            String groupId = group.getGroupId();
            redisMapDataService.delete(UqlConfig.CORT_CONT_ACTIVE_REDIS_KEY.concat(T_activeIndexId+"_"+groupId));
        }
        if(where.isSameGroup(visits)){
            uqlClass.setWhere(TransData.transDataNumber(order1) + " IS NOT NULL AND ");
            uqlClass.setInitialPatients(isVariant,patientSql);
            String orderKey = order1.substring(0,order1.lastIndexOf("."));
            String indexDateKey = indexDate.substring(0,indexDate.lastIndexOf("."));
            if(orderKey.equals(indexDateKey)){
                uqlClass.setWhere(uqlClass.getWhere()+TransData.transDataNumber(indexDate) + " IS NOT NULL AND " );
            }else {
                uqlClass.setNotAllWhere(function,order1,indexDate,schema);
            }
            where.deleteHasChild();
            where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
            uqlClass.setWhere(uqlClass.getWhereNotNull() + " ( "+ where.toString()+" ) ");
            String sqlNew = uqlClass.getHavingSql();
            LOG.info("输出sql： " + sqlNew);
            sqlMd5 = StringToMd5.stringToMd5(sqlNew);
            Integer sqlMd5count = activeSqlMapMapper.getCountByActiveAndsqlMd5(T_activeIndexId,sqlMd5,groupToId);
            if(sqlMd5count>0) return null;
            sqlresult = uqlClass;
            sqlresult.setWhere(sqlresult.getWhere()+" and join_field = '"+visits+"'");
        }else {
            uqlClass.setWhereIsEmpty(where,order1,null,null,schema);
            uqlClass.setInitialPatients(isVariant,patientSql);
            uqlClass.setNotAllWhere(function,order1,indexDate,schema);
            where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
            uqlClass.setWhere(uqlClass.getWhereNotNull() + " ( "+ where.toString()+" ) ");

            boolean isGetVisisn = !where.isEmpty();
            String sqlNew = uqlClass.getHavingSql();
            LOG.info("输出sql： " + sqlNew);
            sqlMd5 = StringToMd5.stringToMd5(sqlNew);
            Integer sqlMd5count = activeSqlMapMapper.getCountByActiveAndsqlMd5(T_activeIndexId,sqlMd5,groupToId);
            if(sqlMd5count>0) return null;

            LOG.info("index -------------where : "+ where);
            if (StringUtils.isNotEmpty(indexColumn) && isGetVisisn) {
                sqlresult = getIndexSql(uqlClass, function, functionParam, indexColumn, indexType, indexDate, projectId,hasCount,crfId);
            } else {
                sqlresult = uqlClass;
            }

            String andGroupCondition =getIndexGroupCondition(where,indexColumn);
            if(StringUtils.isNotEmpty(andGroupCondition)){
                sqlresult.setWhere(sqlresult.getWhere() + " and ("+andGroupCondition.toString()+")");
            }
            sqlresult.setWhere(sqlresult.getWhere()+" and join_field = '"+visits+"'");

        }
        sqlresult.setWhere("("+indexColumn + " is not null) and "+sqlresult.getWhereNotNull());
        sqlresult.setSqlHaving(functionParam);
        String newSql = sqlresult.getHavingSql(crfId);

        ActiveSqlMap activeSqlMap = new ActiveSqlMap();

        LOG.info("新的sql： " + newSql);
        activeSqlMap.setProjectId(projectId);
        activeSqlMap.setActiveIndexId(T_activeIndexId);
        activeSqlMap.setActiveSql(GzipUtil.compress(newSql));
        activeSqlMap.setSqlSelect(sqlresult.getSelect());
        activeSqlMap.setSqlFrom(sqlresult.getFrom());
        activeSqlMap.setUncomSqlWhere(sqlresult.getWhere());
        activeSqlMap.setSourceFiltere(uqlClass.getSourceFilter());
        activeSqlMap.setRefActiveIds(uqlClass.getActiveId().toJSONString());
        activeSqlMap.setSourceValue(JSON.toJSONString(uqlClass.getSource()));
        activeSqlMap.setSelectValue(selectValue);
        activeSqlMap.setCountValue(hasCount.split(",")[1]);
        activeSqlMap.setSqlHaving(uqlClass.getHaving());
        activeSqlMap.setIndexTypeValue(indexTypeValue);
        activeSqlMap.setGroupId(StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId);
        if(StringUtils.isEmpty(groupToId) || UqlConfig.CORT_INDEX_ID.equals(groupToId)){
            SingleExecutorService.getInstance().getFlushCountGroupExecutor().submit(() -> {
                try {
                    searchByuqlService.saveCortrastiveResultRedisMap(activeSqlMap,projectId,"EMR",T_activeIndexId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        int count = activeSqlMapMapper.getCountByActiveIndexId(T_activeIndexId,groupToId);
        if (count > 0) {
            activeSqlMapMapper.deleteByIndexId(T_activeIndexId);
            RunReferenceCalculate(T_activeIndexId,projectId,crfId);
        }
        activeSqlMapMapper.insert(activeSqlMap);
        /*引用依赖计算*/
        getReferenceActiveIndex(id,resultOrderKey,patientSetId,groupToId,groupFromId,crfId);
        return sqlresult.getCrfSql();
    }

    private String getIndexGroupCondition(UqlWhere where, String indexColumn) {
        StringBuffer andGroupCondition = new StringBuffer();
        for (UqlWhereElem elem : where) {
            String s = elem.toString().trim();
            if (elem instanceof LiteralUqlWhereElem){
                andGroupCondition.append(" "+s.replaceAll("haschild","")+" ");
            }else if (elem instanceof SimpleConditionUqlWhereElem || elem instanceof ReferenceConditionUqlWhereElem) {
                String indexTarget = indexColumn.substring(0, indexColumn.indexOf("."));
                if (s.startsWith(indexTarget) || s.startsWith("\""+indexTarget) || s.startsWith("sub_inspection") && indexTarget.startsWith("inspection_reports")) {
                    andGroupCondition.append(" "+s+" ");
                }else if(s.startsWith(indexTarget) || s.startsWith("inspection_reports") && indexTarget.startsWith("sub_inspection")){
                    andGroupCondition.append(" "+s+" ");
                }else if (s.startsWith("haschild("+indexTarget) || s.startsWith("haschild(sub_inspection") && indexTarget.startsWith("inspection_reports")) {
                    andGroupCondition.append(" " + s.substring(0,s.length()-1).replaceAll("haschild\\("," "));
                }else if(s.startsWith("haschild("+indexTarget) || s.startsWith("haschild(inspection_reports") && indexTarget.startsWith("sub_inspection")){
                    andGroupCondition.append(" " + s.substring(0,s.length()-1).replaceAll("haschild\\("," "));
                }else {
                    andGroupCondition.append("visitinfo.DOC_ID  IS NOT NULL");
                }
            }
        }
        return andGroupCondition.toString();
    }

    private String getIndexHasCount(String indexColumn) {
        return " ,count(" + indexColumn + ".DOC_ID) as jocount ";
    }

    private String getInitialSQL(String groupFromId, String isVariant, String groupToId, JSONArray patientSetId, String projectId,String crfId) {
        String patientSql = "";
        if(StringUtils.isEmpty(groupFromId) && (patientSetId ==null || patientSetId.size()==0) && StringUtils.isNotEmpty(groupToId)){
            groupFromId = groupMapper.getGroupParentId(groupToId);
            if(StringUtils.isEmpty(groupFromId)){
                List<String> patSetIds = groupPatDataMapper.getPatSetByGroupId(groupToId);
                patientSetId = JSONArray.parseArray(JSON.toJSONString(patSetIds));
            }
        }
        if(!"1".equals(isVariant)){
            if(patientSetId !=null && patientSetId.size()>0){
                patientSql = getPatientSql(patientSetId,projectId,crfId);
            }else{
                patientSql = getGroupSql(groupFromId);
            }
        }
        return patientSql;
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

        String where = getIndexResultWhere(uqlClass, function, functionParam, indexColumn, indexType, indexDate, projectId,hascount,crfId);
        resultUql.setWhere(where);
        return resultUql;
    }

    private String getIndexResultWhere(UqlClass uqlClass, String function, String functionParam, String indexColumn, String indexType, String indexDate, String projectId, String hasCount, String crfId) {
        String order = FunctionUtilMap.getUqlFunction(function, functionParam, "visitinfo.DOC_ID", indexType, TransData.transDataNumber(indexDate));
        uqlClass.setSelect(order);
        uqlClass.setSelect(" group_concat(visitinfo.DOC_ID,',') ");
        uqlClass.setWhere("join_field='visitinfo' AND "+uqlClass.getWhereNotNull());
        String sql = uqlClass.getCrfSql();
        JSONArray source = new JSONArray();
        String result = httpUtils.querySearch(projectId,sql,1,Integer.MAX_VALUE-1,null,source,crfId);
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
            if (size > 1) resultBuffer.append(" and ");
            resultBuffer.append(TransData.transDataNumber(sourceTagName));
            resultBuffer.append(condition);
            String val = values.getString(i);
            val = "否".equals(val) ? "false" : val;
            val = "是".equals(val) ? "true" : val;
            resultBuffer.append(val);
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
            if("string".equals(jsonType)){
                value = "'" + value + "'";
            } else if ("date".equals(jsonType)) {
                Date date = null;
                for (String pattern: asList("yyyy-MM-dd HH:mm:ss","yyyy-MM-dd","yyyy")) {
                    try {
                        date = new SimpleDateFormat(pattern).parse(value);
                        break;
                    } catch (ParseException ignored) {}
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
        if(visits.startsWith("visits.test.blood_routine")){
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
                                                 String crfId, String groupFromId, JSONArray patientSetIds, String groupId, String isVariant,JSONArray patientSetId) throws IOException, ExecutionException, InterruptedException {
        int size = sqlList.size();
        UqlClass joinUqlClass = new StandardUqlClass(projectId);
        Map<String, Map<String, JSONObject>> resultMap = new HashMap<>();
        List<String> sortList = new LinkedList<>();
        JSONArray refActiveIds = new JSONArray();
        for (int i = 0; i < sqlList.size(); i++) {
            JSONArray refId = JSONArray.parseArray(sqlList.get(i).getRefActiveIds());
            refActiveIds.addAll(refId);
        }
        int refSize = refActiveIds == null ? 0 : refActiveIds.size();
        Set<String> set = new HashSet<>();
        for (int i = 0; i < refSize; i++) {
            //拼接columns
            JSONObject tmpObj = new JSONObject();
            String refActiveId = refActiveIds.getString(i);
            if(set.contains(refActiveId)) continue;
            set.add(refActiveId);
            ActiveIndex activeIndex = activeIndexMapper.selectByPrimaryKey(refActiveId.substring(1));
           if(activeIndex == null ) continue;
            String name = activeIndex.getName();
            tmpObj.put("name", name);
            tmpObj.put("id", refActiveId);
            basicColumns.add(tmpObj);
            String sql = activeSqlMapMapper.getActiveSqlByActiveId(refActiveId.substring(1, refActiveId.length()),groupId);
            if(StringUtils.isEmpty(sql) ){
                referenceCalculate(refActiveId.substring(1, refActiveId.length()),projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get(crfId),patientSetId,groupId,null,crfId);
                sql = activeSqlMapMapper.getActiveSqlByActiveId(refActiveId.substring(1, refActiveId.length()),groupId);
            }
            joinUqlClass.setJoinValue(refActiveId, sql);
        }
        String patientSql = "";
        if(StringUtils.isEmpty(groupFromId) && (patientSetIds ==null || patientSetIds.size()==0) && StringUtils.isNotEmpty(groupId)){
            groupFromId = groupMapper.getGroupParentId(groupId);
            if(StringUtils.isEmpty(groupFromId)){
                List<String> patSetIds = groupPatDataMapper.getPatSetByGroupId(groupId);
                patientSetIds = JSONArray.parseArray(JSON.toJSONString(patSetIds));
            }
        }
        if(!"1".equals(isVariant)){
            if(patientSetIds !=null && patientSetIds.size()>0){
                patientSql = getPatientSql(patientSetIds,projectId,crfId);
            }else{
                patientSql = getGroupSql(groupFromId);
            }
        }

        //查找全部数据
//        String allSql = "select  patient_info.patient_basicinfo.DOC_ID as pSn  from " +PROJECT_INDEX_NAME_PREFIX.get(crfId) + projectId + " where "+patientSql+" group by patient_info.patient_basicinfo.DOC_ID ";
        String allSql = UqlConfig.getEnumAllSql(projectId,sqlList,patientSql,crfId);
        JSONArray allSource = new JSONArray();
        allSource.add("patient_info");
        String result = httpUtils.querySearch(projectId,allSql,pageNum,pageSize,null, allSource,crfId);
        Integer total = UqlQureyResult.getTotal(result);

        JSONArray data  = transforEnumResult(JSON.parseObject(result), sqlList, projectId, activeId,pageSize,crfId);
        List<String> pasSn = new ArrayList<>();
        int daTasize = data == null ? 0 : data.size();
        Map<String, JSONObject> dataMap = new HashMap<>();
        for (int i = 0; i < daTasize; i++) {
            JSONObject tmpObj = data.getJSONObject(i);
            pasSn.add(tmpObj.getString("PATIENT_SN"));
            dataMap.put(tmpObj.getString("PATIENT_SN"), tmpObj);
        }
        String patSnWhere = "";
        if(pasSn.size()==0){
            patSnWhere = "visitinfo.PATIENT_SN IN ('')";
        }else {
            patSnWhere = "visitinfo.PATIENT_SN  " + TransPatientSql.transForExtContain(pasSn);
        }
        JSONArray source = new JSONArray();
        source.add("patient_info.patient_basicinfo.PATIENT_SN");
        Long tmie3 = System.currentTimeMillis();
        for (int i = 0; i < refSize; i++) {
            //拼接column
            String refActiveId = refActiveIds.getString(i);
            List<ActiveSqlMap> patSqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, refActiveId.substring(1),groupId);
            if(patSqlList == null || patSqlList.size() == 0 ){
                referenceCalculate(refActiveId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get(crfId),patientSetId,groupId,null,crfId);
                patSqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, refActiveId,groupId);
            }
            if(patSqlList.size() == 0 ) continue;
            ActiveSqlMap patActiveSqlMap = patSqlList.get(0);
            String patSql = patActiveSqlMap.getUncomActiveSql();
            String[] patSqls = patSql.split("where");
            String where = patSqls[1];
            String newWhere = patSnWhere + " and " + where;
            String patSnResult = querySearch(projectId, patSqls[0] + " where "+ newWhere, 1, pageSize, "", source, crfId);
            JSONArray tmpHits = UqlQureyResult.getHitsArray(patSnResult);
            int tmpHitsSize = tmpHits.size();
            for (int j = 0; j < tmpHitsSize; j++) {
                String colle = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONObject("select_field").getString("condition");
                if(colle.contains(".")){
                    try {
                        colle = String.format("%.2f",  Double.parseDouble(colle));
                    }catch (Exception e){
                    }
                }
                String patSn = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0).getJSONArray("patient_basicinfo").getJSONObject(0).getString("PATIENT_SN");;
                dataMap.get(patSn).put(refActiveId, colle);
            }
            for(String key : dataMap.keySet()){
                JSONObject obj = dataMap.get(key);
                if(!obj.containsKey(refActiveId)){
                    obj.put(refActiveId,"-");
                }
            }
        }
        Integer count = 0;
        if(StringUtils.isEmpty(groupFromId) && (patientSetIds ==null || patientSetIds.size()==0) && StringUtils.isNotEmpty(groupId)){
            groupFromId = groupMapper.getGroupParentId(groupId);
            if(StringUtils.isEmpty(groupFromId)){
                List<String> patSetIds = groupPatDataMapper.getPatSetByGroupId(groupId);
                patientSetIds = JSONArray.parseArray(JSON.toJSONString(patSetIds));
            }
        }
        //获取总共人数
        if(patientSetIds !=null && patientSetIds.size()>0){
            count = getPatientSqlCount(patientSetIds,projectId,crfId);
        }else {
            count = getGroupSqlCount(groupFromId);
        }
        saveActiveIndexTask(activeId, projectId, total);
//        Integer count = getProjectCount(projectId, crfId);
        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
        AjaxObject.getReallyDataValue(data,basicColumns);
        ajaxObject.setData(data);
        ajaxObject.setCount(count);
        ajaxObject.setColumns(basicColumns);
        WebAPIResult webAPIResult = new WebAPIResult(pageNum, pageSize, total);
        ajaxObject.setWebAPIResult(webAPIResult);

        return ajaxObject;
    }

    private JSONArray  transforEnumResult(JSONObject data, List<ActiveSqlMap> sqlList, String projectId, String activeId, Integer size, String crfId) throws IOException {

        Set<String> pats = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(data)
            .stream()
            .map(String.class::cast)
            .collect(toSet());
        String patsStr ="";
        if(pats.size()==0){
            patsStr = "''";
        }else {
            patsStr = TransPatientSql.transForExtContain(pats);
        }
        Map<String, Set<String>> resultMap = new ConcurrentHashMap<>();
        List<Future> futures = new ArrayList<>();
        for (ActiveSqlMap activeSqlMap : sqlList){
            String where = activeSqlMap.getUncomSqlWhere();
            String newWhere = " patient_info.patient_basicinfo.DOC_ID IN (" + patsStr +")  and ("+where+" )";
            activeSqlMap.setUncomSqlWhere(newWhere);
            String sql = activeSqlMap.getUql(crfId);
            JSONArray source= new JSONArray();
            source.add("patient_info.patient_basicinfo.PATIENT_SN");
            String patData = httpUtils.querySearch(projectId,sql, 1,size,null,source,crfId);
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
            resultMap.put(indexResultValue,values);
        }

        return transforEnumDataValue(data,resultMap,activeId);
    }

    private JSONArray transforEnumDataValue(JSONObject data, Map<String, Set<String>> resultMap,String activeId) {
        JSONArray hitsArray = UqlQureyResult.getHitsArray(data);
        int size = hitsArray == null ? 0 :hitsArray.size();
        JSONArray array = new JSONArray();
        for (int i = 0; i < size; i++) {
            JSONObject obj = hitsArray.getJSONObject(i).getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0).getJSONArray("patient_basicinfo").getJSONObject(0);
            String patSn = obj.getString("PATIENT_SN");
            for (String key :resultMap.keySet()){
                Set<String> patSet = resultMap.get(key);
                if(patSet.contains(patSn)){
                    if(obj.containsKey(activeId)){
                        obj.put(activeId,obj.getString(activeId)+";"+key);
                    }else {
                        obj.put(activeId,key);
                    }
                }
            }
            array.add(obj);
        }
        return array;
    }

    public void getReferenceActiveIndex(String activeId,String resultOrderKey,JSONArray patientsSetId,String groupToId,String groupFromId,String crfId){
        int isTmp = 0;
        List<ActiveIndex> activeIndices = activeIndexMapper.findReferenceActiveIndex(activeId,isTmp);

        for (ActiveIndex activeIndex : activeIndices){
            String projectId = activeIndex.getProjectId();
            String activeIdTmp = activeIndex.getId();
            Integer activeType = activeIndex.getActiveType();
            SingleExecutorService.getInstance().getReferenceActiveExecutor().submit(() -> {
                try {
                    if("1".equals(activeIndex.getIsVariant())){
                        referenceCalculate(activeIdTmp,projectId,activeType,resultOrderKey, null, UqlConfig.CORT_INDEX_ID, null,crfId);
                    }else {
                        referenceCalculate(activeIdTmp,projectId,activeType,resultOrderKey, patientsSetId, groupToId, groupFromId,crfId);
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    private void RunReferenceCalculate(String T_activeIndexId, String projectId,String crfId) {
        SingleExecutorService.getInstance().getReferenceActiveExecutor().submit(() -> {
            try {
                referenceCalculate(T_activeIndexId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get(crfId),null,UqlConfig.CORT_INDEX_ID,null,crfId);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public void referenceCalculate(String activeId, String projectId, Integer activeType, String resultOrderKey,JSONArray patientsSetId,String groupToId,String groupFromId,String crfId) throws ExecutionException, InterruptedException, IOException {

        ActiveIndex active = null;
        if (activeType != null && activeType == CommonContent.ACTIVE_TYPE_INOUTN.intValue()) {
            List<ActiveIndex> activeIndexList = activeIndexService.findeByProjectAndType(projectId, activeType);
            if (activeIndexList != null && !activeIndexList.isEmpty()) {
                ActiveIndex index = activeIndexList.get(0);
                activeId = index.getId();
            }
        }
        active = activeIndexService.findByActiveId(activeId);
        active = active == null ? new ActiveIndex() : active;
        JSONObject obj = (JSONObject) JSONObject.toJSON(active);
        obj.put("patientSetId",patientsSetId);
        obj.put("groupToId",groupToId);
        obj.put("groupFromId",groupFromId);
        JSONArray configss = obj.getJSONArray("config");
        if(configss.size() <1){
            LOG.error("错误的数据 ： activeId" + activeId);
            return;
        }
        String indexTypeDesc = configss.getJSONObject(0).getString("indexTypeDesc");
        int isSearch = CommonContent.ACTIVE_TYPE_NOTEMP ;
        String sql = "";
        activeType = active.getActiveType();
        if (3 == activeType) {//那排
            sql =  this.SearchByExclude(obj, resultOrderKey,isSearch, crfId);
        } else if ("自定义枚举类型".equals(indexTypeDesc)) {//处理枚举
            sql =   this.SearchByEnume(obj, resultOrderKey,isSearch, crfId);
        } else if(2 == activeType) {//指标
            sql =   this.SearchByIndex(obj, resultOrderKey,isSearch, crfId);
        }else  if(1 == activeType){ //事件
            sql =   this.searchByActive(obj, resultOrderKey,isSearch, crfId);
        }

    }

    private String getPatientSql(JSONArray patientSetId,String projectId,String crfId) {
        List<String> patientSets = patientSetId.toJavaList(String.class);
        List<String> patientSetSql = patientsSetMapper.getPatientsetSqlAll(patientSets);
        String query = String.join(" or ",patientSetSql.stream().map( x -> "("+TransPatientSql.getAllPatientSql(TransPatientSql.getUncomPatientSnSql(x),crfId)+")").collect(toList()));
        JSONArray sourceFilter = new JSONArray();
        sourceFilter.add(IndexContent.getPatientInfoPatientSn(crfId));
        String result = null;
        String newSql = "select  "+ IndexContent.getPatientDocId(crfId)+" as pSn from "+ IndexContent.getIndexName(crfId, projectId)+" where "+query+IndexContent.getGroupBy(crfId);
        String response = httpUtils.querySearch(projectId,newSql,1,Integer.MAX_VALUE-1,null,sourceFilter,crfId,true);
        Set<String> patients = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(JSON.parseObject(response))
            .stream()
            .map(String.class::cast)
            .collect(toSet());
        if (patients.isEmpty()) {
            result = IndexContent.getPatientDocId(crfId)+" IN ('')";
        } else {
            result = IndexContent.getPatientDocId(crfId)+TransPatientSql.transForExtContain(patients);
        }
        return result;
    }

    private String getGroupSql(String groupId) {
        List<String> groupDataPatSn = groupDataMapper.getPatientDocId(groupId);
        String query = null;
        if(groupDataMapper == null || groupDataPatSn.size()==0){
            query = "''";
        }else {
            query = TransPatientSql.transForExtContain(groupDataPatSn);
        }
        return  " patient_info.patient_basicinfo.DOC_ID "+query;
    }

    private Integer getPatientSqlCount(JSONArray patientSetId, String projectId,String crfId) {
        List<String> patientSets = patientSetId.toJavaList(String.class);
        List<String> patientSetSql = patientsSetMapper.getPatientsetSqlAll(patientSets);
        String query = String.join(" or ",patientSetSql.stream().map( x -> "("+TransPatientSql.getPatientSnSql(TransPatientSql.getUncomPatientSnSql(x),crfId)+")").collect(toList()));
        JSONArray sourceFilter = new JSONArray();
//        sourceFilter.add("patient_info");
        String newSql = "select "+IndexContent.getPatientDocId(crfId)+" as pSn from "+IndexContent.getIndexName(crfId, projectId)+" where "+query+IndexContent.getGroupBy(crfId);
        String response = httpUtils.querySearch(projectId,newSql,1,Integer.MAX_VALUE-1,null,sourceFilter,crfId,true);
        return UqlQureyResult.getTotal(response);
    }

    private Integer getGroupSqlCount(String groupFromId) {
        return groupDataMapper.getPatSetAggregationCount(groupFromId);
    }

}
