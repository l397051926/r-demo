package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.darren.collection.Pair;
import com.gennlife.darren.collection.keypath.KeyPath;
import com.gennlife.exception.CustomerException;
import com.gennlife.exception.CustomerStatusEnum;
import com.gennlife.rws.content.CommonContent;
import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.content.UqlConfig;
import com.gennlife.rws.dao.*;
import com.gennlife.rws.entity.*;
import com.gennlife.rws.query.UqlQureyResult;
import com.gennlife.rws.service.ActiveIndexService;
import com.gennlife.rws.service.GroupService;
import com.gennlife.rws.service.RedisMapDataService;
import com.gennlife.rws.service.SearchByuqlService;
import com.gennlife.rws.uql.*;
import com.gennlife.rws.uqlcondition.*;
import com.gennlife.rws.util.*;
import com.gennlife.rws.web.WebAPIResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collector;

import static com.gennlife.darren.collection.Pair.makePair;
import static com.gennlife.darren.controlflow.for_.Foreach.foreach;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.*;


@Service
public class SearchByuqlServiceImpl implements SearchByuqlService {
    private static final Logger LOG = LoggerFactory.getLogger(SearchByuqlServiceImpl.class);
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
    private GroupService groupService;
    @Autowired
    private RedisMapDataService redisMapDataService;

    @Override
    public AjaxObject searchClacIndexResultByUql(String activeId, String projectId, Integer pageSize, Integer pageNum, JSONArray basicColumns, String groupFromId, JSONArray patientSetId, String groupId, String isVariant) throws IOException, ExecutionException, InterruptedException {
        Long tmie = System.currentTimeMillis();
        AjaxObject ajaxObject = new AjaxObject();
        UqlClass joinUqlClass = new StandardUqlClass(projectId);
        Long startMysqlTime = System.currentTimeMillis();
        String tmpAcId = activeId.split("_tmp")[0];
        List<ActiveIndexConfig> activeIndexConfigs = activeIndexConfigMapper.findAllByActiveIndexId(tmpAcId);
        String indexType = "";
        if(activeIndexConfigs.size()>0){
            indexType = activeIndexConfigs.get(0).getIndexTypeDesc();
        }
        List<ActiveSqlMap> sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        if(sqlList == null || sqlList.size() == 0 ){
            referenceCalculate(activeId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null);
            sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        }
        LOG.info("指标 从mysql数据库读取时间为： "+(System.currentTimeMillis()-startMysqlTime));
//        LOG.info("开始请求结果"+(System.currentTimeMillis()-tmie));
        Long tmie1 = System.currentTimeMillis();
        if ("自定义枚举类型".equals(indexType)) {//枚举类型处理
            return searchClasEnumResultByUql(activeId, sqlList, projectId, pageSize, pageNum, basicColumns,groupFromId,patientSetId,groupId,isVariant,patientSetId);
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
                referenceCalculate(refActiveId.substring(1, refActiveId.length()),projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null);
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
        String result = httpUtils.querySearch(projectId, newSql, pageNum, pageSize, sourceFilter, source,false);
        JSONObject object = JSONObject.parseObject(result);
        if (object.containsKey("error")) {
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, "错误信息：" + object.getString("error"));
        }
        //处理结果
        Integer total = UqlQureyResult.getTotal(result);
        JSONArray data = UqlQureyResult.getResultData(result, activeId,refActiveIds);
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
            patSnWhere = "patient_info.DOC_ID IN ('')";
        }else {
            patSnWhere = "patient_info.DOC_ID " + TransPatientSql.transForExtContain(pasSn);
        }
//        LOG.info("处理结果"+(System.currentTimeMillis()-tmie2));
        Long tmie3 = System.currentTimeMillis();
        for (int i = 0; i < refSize; i++) {
            //拼接column
            String refActiveId = refActiveIds.getString(i);
            List<ActiveSqlMap> patSqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, refActiveId.substring(1),groupId);
            if(patSqlList == null || patSqlList.size() == 0 ){
                referenceCalculate(refActiveId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null);
                patSqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, refActiveId.substring(1),groupId);
            }
            ActiveSqlMap patActiveSqlMap = patSqlList.get(0);
            String patSql = patActiveSqlMap.getUncomActiveSql();
            String[] patSqls = patSql.split("where");
            String where = patSqls[1];
            String newWhere = patSnWhere + " and " + where;
            String patSnResult = httpUtils.querySearch(projectId, patSqls[0] + " where "+ newWhere, 1, pageSize, sourceFilter, source,false);
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
                String patSn = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0).getString("DOC_ID");
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
            count = getPatientSqlCount(patientSetId,projectId);
        }else {
            count = getGroupSqlCount(groupFromId);
        }

        saveActiveIndexTask(activeId, projectId, total);
        AjaxObject.getReallyDataValue(data,basicColumns);
//        Integer count = getProjectCount(projectId);
        ajaxObject.setCount(count);
        ajaxObject.setWebAPIResult(new WebAPIResult<Object>(pageNum, pageSize, total));
        ajaxObject.setColumns(basicColumns);
        ajaxObject.setData(data);
        deleteTmpActiveId(projectId,activeId);
        return ajaxObject;
    }

    private Integer getGroupSqlCount(String groupFromId) {
        return groupDataMapper.getPatSetAggregationCount(groupFromId);
    }

    private Integer getPatientSqlCount(JSONArray patientSetId, String projectId) {
        List<String> patientSets = patientSetId.toJavaList(String.class);
        List<String> patientSetSql = patientsSetMapper.getPatientsetSqlAll(patientSets);
        String query = String.join(" or ",patientSetSql.stream().map( x -> "("+TransPatientSql.getAllPatientSql(TransPatientSql.getUncomPatientSnSql(x))+")").collect(toList()));
        JSONArray sourceFilter = new JSONArray();
        String newSql = UqlConfig.getPatientSetSql(projectId,query);
        String response = httpUtils.querySearch(projectId,newSql,1,1,null,sourceFilter,false);
        return UqlQureyResult.getTotal(response);
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
    public AjaxObject searchCalcResultByUql(String activeId, String projectId, JSONArray basicColumns, JSONArray visitColumns, Integer activeType, Integer pageNum, Integer pageSize, String activeResult, String groupFromId, JSONArray patientSetId, String groupId) throws InterruptedException, IOException, ExecutionException {
        Long startMysqlTime = System.currentTimeMillis();
        List<ActiveSqlMap> sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        if(sqlList == null || sqlList.size() == 0 ){
            referenceCalculate(activeId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null);
            sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        }
        LOG.info("事件 从mysql数据库读取时间为： "+(System.currentTimeMillis()-startMysqlTime));
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
                referenceCalculate(refActiveId.substring(1, refActiveId.length()),projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null);
                sql = activeSqlMapMapper.getActiveSqlByActiveId(refActiveId.substring(1, refActiveId.length()),groupId);
            }
            joinUqlClass.setJoinValue(refActiveId, sql);
        }
        /*处理 检验子项 和检验报告命名方式*/
        String activeResultValue = activeSqlMap.getActiveResultValue();
        String parts[] = activeResultValue.split("\\.");
        String visits = parts[parts.length - 1];
        if( parts.length>2 && parts[0].equals("visits") && parts[1].equals("inspection_reports") && parts[2].equals("sub_inspection") ){
            visits = "sub_inspection";
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
        String having ="";
        if( activeSqlMap.getUncomActiveSql().contains("having")){
            having = "having"+ activeSqlMap.getUncomActiveSql().split("having")[1];
        }
        String activeReuslt = activeSqlMap.getActiveResultDocId();
        String sql = "";
        String tmpSql = "";
        if("medical_record_home_page".equals(visits)){
            sql = "select " +activeReuslt +"as condition ,count(medical_record_home_page.DOC_ID) as jocount from " + activeSqlMap.getSqlFrom() +" where "+ activeSqlMap.getUncomSqlWhere() + " and medical_record_home_page.DOC_ID is not null group by patient_info.DOC_ID "+having;
        }else {
            sql = "select " +activeReuslt +"as condition ,count("+visits +".DOC_ID) as jocount from " + activeSqlMap.getSqlFrom() +" where "+ activeSqlMap.getUncomSqlWhere() + " and "+visits+ ".DOC_ID is not null group by patient_info.DOC_ID "+having;
        }

        String result = httpUtils.querySearch(projectId,sql,pageNum,pageSize,sourceFilter,source,false);
        /*处理结果*/
        Integer total = UqlQureyResult.getTotal(result);
        saveActiveIndexTask(activeId, projectId, total);
        JSONArray data = UqlQureyResult.getActiveVisitSn(result, activeId);
        String query = getVisitSns(data);

        /*组装新的 uql搜索 搜索新的数据*/
        UqlClass uqlClass = new StandardUqlClass();
        uqlClass.setFrom("rws_emr_" + projectId);
        JSONArray array = new JSONArray();
        array = getSource(basicColumns, "patient_info", array);
        /*处理病案首页 手术问题*/
        String repeaceActive = activeResult.substring(activeResult.lastIndexOf(".") + 1, activeResult.length());
        array = getSource(visitColumns, repeaceActive, array);

        int size = array == null ? 0 : array.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                uqlClass.setActiveSelect(uqlClass.getSelect() + ",");
            }
            uqlClass.setActiveSelect(uqlClass.getSelect() + array.getString(i));
        }

        uqlClass.setActiveSelect(uqlClass.getSelect() + ",visit_info.VISIT_SN");
        uqlClass.setActiveSelect(uqlClass.getSelect() + ",visit_info.PATIENT_SN");
        if(!"sub_inspection".equals(visits)){
            uqlClass.setActiveSelect(uqlClass.getSelect() + ","+visits+".PATIENT_SN");
        }
        if("inspection_reports".equals(visits)){
            uqlClass.setActiveSelect(uqlClass.getSelect() + ",inspection_reports.INSPECTION_SN");
        }
        if (StringUtils.isEmpty(uqlClass.getWhere())) {
            if("medical_record_home_page".equals(visits)){
                uqlClass.setWhere("medical_record_home_page.DOC_ID in (" + query + ")");
            }else {
                uqlClass.setWhere(visits+".DOC_ID in (" + query + ")");
            }
        } else {
            if("medical_record_home_page".equals(visits)){
                uqlClass.setWhere(uqlClass.getWhere() + "and medical_record_home_page.DOC_ID in (" + query + ")");
            }else {
                uqlClass.setWhere(uqlClass.getWhere() + "and "+visits+".DOC_ID in (" + query + ")");
            }
        }
        /*查询docId*/
        JSONArray resultSource = new JSONArray();
        String resultJson = httpUtils.querySearch(projectId,uqlClass.getVisitsSql(),1,Integer.MAX_VALUE-1,null,resultSource,false);
        JSONArray dataObj = getActiveResultData(resultJson, basicColumns, visitColumns, repeaceActive,result,visits);

        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
        Integer count = 0;

        if(StringUtils.isEmpty(groupFromId) && (patientSetId ==null || patientSetId.size()==0) && StringUtils.isNotEmpty(groupId)){
            groupFromId = groupMapper.getGroupParentId(groupId);
            if(StringUtils.isEmpty(groupFromId)){
                List<String> patSetIds = groupPatDataMapper.getPatSetByGroupId(groupId);
                patientSetId = JSONArray.parseArray(JSON.toJSONString(patSetIds));
            }
        }
        //获取总共人数
        if("1".equals(groupFromId) || "''".equals(groupFromId) || StringUtils.isEmpty(groupFromId)){
            count = getPatientSqlCount(patientSetId,projectId);
        }else {
            count = getGroupSqlCount(groupFromId);
        }
        ajaxObject.setCount(count);
        ajaxObject.setData(dataObj);
        WebAPIResult webAPIResult = new WebAPIResult(pageNum, pageSize, total);
        ajaxObject.setWebAPIResult(webAPIResult);
        deleteTmpActiveId(projectId,activeId);
        return ajaxObject;
    }

    private JSONArray getActiveResultData(String resultJson, JSONArray basicColumns, JSONArray visitColumns, String key, String result,String visitsKey) {

        JSONArray resultHtis = UqlQureyResult.getHitsArray(result);
        JSONArray hits = UqlQureyResult.getHitsArray(resultJson);
        JSONArray data = new JSONArray();
        int size = hits == null ? 0 : hits.size();
        int resultSize = resultHtis == null ? 0 : resultHtis.size();
        Map<String, JSONObject> patMap = new ConcurrentHashMap<>();
        Map<String,JSONObject> resultMap = new ConcurrentHashMap<>();
        Set<String> visSet = new HashSet<>();

        for (int i = 0; i < resultSize; i++) {
            String dockId = resultHtis.getJSONObject(i).getJSONObject("_source").getJSONObject("select_field").getString("condition");
            JSONObject patientInfo = resultHtis.getJSONObject(i).getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0);
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
            Object visitObj = hits.getJSONObject(i).getJSONObject("_source").get("visit_info");
            String visitSn = "";
            String patSn ="";
            String inspectionSn = "";
            if(visitObj instanceof JSONArray){
                 visitSn = ((JSONArray) visitObj).getJSONObject(0).getString("VISIT_SN");
                 patSn = ((JSONArray) visitObj).getJSONObject(0).getString("PATIENT_SN");
            }else if(visitObj instanceof JSONObject){
                visitSn = ((JSONObject) visitObj).getString("VISIT_SN");
                patSn = ((JSONObject) visitObj).getString("PATIENT_SN");
            }
            Object visitKeyObj = hits.getJSONObject(i).getJSONObject("_source").get(visitsKey);

            if(visitKeyObj instanceof JSONArray){
                patSn = StringUtils.isNotEmpty (patSn) ? patSn: ((JSONArray) visitKeyObj).getJSONObject(0).getString("PATIENT_SN");
                inspectionSn = ((JSONArray) visitKeyObj).getJSONObject(0).getString("INSPECTION_SN");
            }else if(visitKeyObj instanceof JSONObject){
                patSn = StringUtils.isNotEmpty (patSn) ? patSn:  ((JSONObject) visitKeyObj).getString("PATIENT_SN");
                inspectionSn = ((JSONObject) visitKeyObj).getString("INSPECTION_SN");
            }
            Object visitsObj = hits.getJSONObject(i).getJSONObject("_source").get(key);
            if(visitsObj == null) continue;
            JSONObject visits = null;
            if(visitKeyObj instanceof JSONArray){
                visits = ((JSONArray) visitsObj).getJSONObject(0);
            }else if(visitKeyObj instanceof JSONObject){
                visits = (JSONObject) visitsObj;
            }
//            if("sub_inspection".equals(key)){
//                docId = inspectionSn;
//            }
            if("inspection_reports".equals(key)){
                docId = visits.getString("INSPECTION_SN");
                visits.remove("INSPECTION_SN");
            }else if("visit_info".equals(key)  ){
                docId = visitSn;
            }else if("medical_record_home_page".equals(key)){
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
            data.add(patMap.get(keyTmp));
        }
        return data;
    }

    private JSONArray transforActiveData(JSONObject result, JSONArray showColumns){
        JSONArray data = new JSONArray();
        int size = showColumns ==null ? 0 :showColumns.size();
        for (int i = 0; i < size; i++) {
            JSONObject resObj = new JSONObject();
            JSONObject obj = showColumns.getJSONObject(i);
            String id = obj.getString("id");
            String name = obj.getString("name");
            String dmp = result.getString(id);
            if(StringUtils.isEmpty(dmp)){
                dmp = "-";
            }
            if(UqlConfig.ACTIVE_CONVER_TRUE_OR_FALSE.contains(id)){
                switch (dmp){
                    case "true": dmp = "是";
                        break;
                    case "false": dmp = "否";
                        break;
                    default: dmp = "-";
                }
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
        StringBuffer stringBuffer = new StringBuffer();
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
        int i = 0;
        for (String visTmp : visSet){
            if(i>0) stringBuffer.append(",");
            stringBuffer.append("'");
            stringBuffer.append(visTmp);
            stringBuffer.append("'");
            i++;
        }
        if (StringUtils.isEmpty(stringBuffer.toString())) {
            stringBuffer.append("''");
        }
        return stringBuffer.toString();
    }

    @Override
    public AjaxObject searchCalcExculeByUql(String activeId, String projectId, Integer pageSize, Integer pageNum, JSONArray basicColumns, String isExport, String groupId, String groupName, JSONArray patientSetId, String createId, String createName,String groupFromId,boolean autoExport) throws IOException, ExecutionException, InterruptedException {
        AjaxObject ajaxObject = new AjaxObject();
        Long startMysqlTime = System.currentTimeMillis();
        List<ActiveSqlMap> sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        if(sqlList == null || sqlList.size() == 0 ){
            referenceCalculate(activeId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null);
            sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        }
        LOG.info("那排 从mysql数据库读取时间为： "+(System.currentTimeMillis()-startMysqlTime));
        ActiveSqlMap activeSqlMap = sqlList.get(0);
        List<String> enumList = new ArrayList<>();
        JSONArray refActiveIds = JSONArray.parseArray(activeSqlMap.getRefActiveIds());
        JSONArray sourceValue = JSONArray.parseArray(activeSqlMap.getSourceValue());
        String sqlQuery = activeSqlMap.getUncomActiveSql();
        int refSize = refActiveIds == null ? 0 : refActiveIds.size();
        String refActiveIdTmp = null;

        for (int i = 0; i < refSize; i++) {
            //拼接columns
            JSONObject tmpObj = new JSONObject();
            String refActiveId = refActiveIds.getString(i);
            ActiveIndex activeIndex = activeIndexMapper.selectByPrimaryKey(refActiveId);
            String name = activeIndex.getName();
            tmpObj.put("name", name);
            tmpObj.put("id", refActiveId);
            basicColumns.add(tmpObj);
//            String sql = activeSqlMapMapper.getActiveSqlByActiveId(refActiveId.substring(1, refActiveId.length()),groupId);
        }

        if(StringUtils.isEmpty(groupFromId) && (patientSetId ==null || patientSetId.size()==0) && StringUtils.isNotEmpty(groupId)){
            groupFromId = groupMapper.getGroupParentId(groupId);
            if(StringUtils.isEmpty(groupFromId)){
                List<String> patSetIds = groupPatDataMapper.getPatSetByGroupId(groupId);
                patientSetId = JSONArray.parseArray(JSON.toJSONString(patSetIds));
            }
        }

        JSONArray source = new JSONArray();
        source.add("patient_info");

        if("1".equals(isExport)){//处理导出数据
            String result =  httpUtils.querySearch(projectId,activeSqlMap.getUncomActiveSql(),pageNum,Integer.MAX_VALUE-1,activeSqlMap.getSourceFiltere(),source,false);
            Integer total = UqlQureyResult.getTotal(result);
            JSONArray data = UqlQureyResult.getResultData(result, activeId,refActiveIds,false);
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
                String sql = getPatientSqlForIds(patientSetId,projectId,docIds);
                String removeQuery = "select patient_info.DOC_ID as pSn from rws_emr_"+projectId+" where "+sql+" group by patient_info.DOC_ID";
                String re =  httpUtils.querySearch(projectId,removeQuery,pageNum,Integer.MAX_VALUE-1,activeSqlMap.getSourceFiltere(),source,false);
                JSONArray dataRe = UqlQureyResult.getResultData(re, activeId,refActiveIds,false);
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
        String result =  httpUtils.querySearch(projectId,activeSqlMap.getUncomActiveSql(),pageNum,pageSize,activeSqlMap.getSourceFiltere(),source,false);
        JSONObject applyOutObj = JSONObject.parseObject( httpUtils.querySearch(projectId,activeSqlMap.getUncomActiveSql(),pageNum,Integer.MAX_VALUE-1,activeSqlMap.getSourceFiltere(),source,false));
        String applyCondition = getApplyCondition(applyOutObj);
        //处理结果
        Integer total = UqlQureyResult.getTotal(result);
        JSONArray data = UqlQureyResult.getResultData(result, activeId,refActiveIds,false);

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
            patSnWhere = "patient_info.DOC_ID IN ('')";
        }else {
            patSnWhere = "patient_info.DOC_ID " + TransPatientSql.transForExtContain(pasSn);
        }

        for (int i = 0; i < refSize; i++) {
            //拼接column
            String refActiveId = refActiveIds.getString(i);
            List<ActiveSqlMap> patSqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, refActiveId,groupId);
            if(patSqlList == null || patSqlList.size() == 0 ){
                referenceCalculate(refActiveId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null);
                patSqlList =activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, refActiveId,groupId);
            }
            String tmpAcId = refActiveId.split("_tmp")[0];
            List<ActiveIndexConfig> activeIndexConfigs = activeIndexConfigMapper.findAllByActiveIndexId(tmpAcId);
            String indexType = "";
            if(activeIndexConfigs.size()>0){
                indexType = activeIndexConfigs.get(0).getIndexTypeDesc();
            }
            ActiveSqlMap patActiveSqlMap = patSqlList.get(0);
            if("自定义枚举类型".equals(indexType)){//那排
                makeEnumResultData(patSqlList,patSnWhere,dataMap,projectId,pageSize,source,refActiveId);
            }else if("1".equals(patActiveSqlMap.getActiveType())){//事件
                String patSql = patActiveSqlMap.getUncomActiveSql();
                String[] patSqls = patSql.split("where");
                String where = patActiveSqlMap.getUncomSqlWhere()+"group by patient_info.DOC_ID";//patActiveSqlMap.getEventWhere() +"group by visit_info.PATIENT_SN";// +" and join_field='visit_info' ";
                String newWhere = patSnWhere+ " and " + where;
                String patSnResult = httpUtils.querySearch(projectId, patSqls[0] + " where "+ newWhere , 1, pageSize, "", source,false);
                JSONArray tmpHits = UqlQureyResult.getHitsArray(patSnResult);
                String activeResultDoc = patActiveSqlMap.getActiveResultDocId();
                boolean isFirst = activeResultDoc.startsWith("first") || activeResultDoc.startsWith("any") || activeResultDoc.startsWith("last");
                int tmpHitsSize = tmpHits.size();
                for (int j = 0; j < tmpHitsSize; j++) {
                    String colle = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONObject("select_field").getString("jocount");
                    String patSn = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0).getString("DOC_ID");
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
                String patSnResult = httpUtils.querySearch(projectId, patSqls[0] + " where "+ newWhere, 1, pageSize, "", source,false);
                JSONArray tmpHits = UqlQureyResult.getHitsArray(patSnResult);
                int tmpHitsSize = tmpHits.size();
                for (int j = 0; j < tmpHitsSize; j++) {
                    String colle = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONObject("select_field").getString("condition");
                    String patSn = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0).getString("DOC_ID");
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


        if("1".equals(groupFromId) || "''".equals(groupFromId) || StringUtils.isEmpty(groupFromId)){
            count = getPatientSqlCount(patientSetId,projectId);
        }else {
            count = getGroupSqlCount(groupFromId);
        }
//        Integer count = getProjectCount(projectId);
        AjaxObject.getReallyDataValue(data,basicColumns);
        ajaxObject.setCount(count);
        ajaxObject.setApplyOutCondition(applyCondition);
        ajaxObject.setWebAPIResult(new WebAPIResult<Object>(pageNum, pageSize, total));
        ajaxObject.setColumns(basicColumns);
        ajaxObject.setData(data);
        deleteTmpActiveId(projectId,activeId);

        return ajaxObject;

    }

    private void deleteTmpActiveId(String projectId, String activeId) {
//        if(activeId.endsWith("_tmp")){
//            activeSqlMapMapper.deleteByActiveIndexId(activeId);
//        }
    }


    public static boolean isConflictedPatients(List<String> patients1 ,List<String> patients2){
        for (String patSn1 :patients1){
            if(patients2.contains(patSn1)){
                return  true;
            }
        }
        return false;
    }

    private void makeEnumResultData(List<ActiveSqlMap> patSqlList, String patSnWhere, Map<String, JSONObject> dataMap,String projectId,Integer pageSize,JSONArray source,String refActiveId)throws IOException {//处理枚举
        String isOtherName = "";
        for(ActiveSqlMap activeSqlMap : patSqlList){
            if(activeSqlMap.getIsOther() != null && activeSqlMap.getIsOther()==1){
                isOtherName = activeSqlMap.getIndexResultValue();
            }
        }
        Map<String,EnumResult> map = new HashMap<>();
        for (ActiveSqlMap activeSqlMap : patSqlList){
            String patSql = activeSqlMap.getUncomActiveSql();
            String where =activeSqlMap.getUncomSqlWhere();
            String newWhere = patSnWhere + " and (" + where+")";
            activeSqlMap.setUncomSqlWhere(newWhere);
            String sql = activeSqlMap.getUql();
            String patSnResult = httpUtils.querySearch(projectId, sql, 1, pageSize, "", source,false);
            JSONArray tmpHits = UqlQureyResult.getHitsArray(patSnResult);
            int tmpHitsSize = tmpHits.size();
            String indexResultValue = activeSqlMap.getIndexResultValue();
            for (int j = 0; j < tmpHitsSize; j++) {
//                String patSn = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0).getString("PATIENT_SN");
                String patSn = tmpHits.getJSONObject(j).getString("_id");
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
    public AjaxObject getPatientListByAll(String patientSetId, String projectId, JSONArray showColumns, JSONArray actives, Integer pageNum, Integer pageSize, Integer type, String crfId) throws IOException {
        String patientSql = getPatientSql(patientSetId,projectId,crfId);
        String query = "select "+ IndexContent.getPatientDocId(crfId)+" from "+IndexContent.getIndexName(crfId,projectId) + " where "+patientSql +" and join_field='patient_info' ";
        JSONArray source = new JSONArray();
        source.add("patient_info");
        JSONObject jsonData = JSONObject.parseObject(httpUtils.querySearch(projectId,query,pageNum,pageSize,null,source,crfId));
        JSONArray data = UqlQureyResult.getQueryData(jsonData,crfId);
        Integer total = UqlQureyResult.getTotal(jsonData);
        WebAPIResult webAPIResult = new WebAPIResult(pageNum, pageSize, total);
        //获取样本条件参数
        JSONObject applyOutObj = JSONObject.parseObject(httpUtils.querySearch(projectId,query,1,Integer.MAX_VALUE-1,null,source,crfId));
        String applyCondition = getApplyCondition(applyOutObj);
        AjaxObject.getReallyDataValue(data,showColumns);
        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
        ajaxObject.setColumns(showColumns);
        ajaxObject.setApplyOutCondition(applyCondition);
        ajaxObject.setData(data);
        ajaxObject.setWebAPIResult(webAPIResult);
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


    @Override
    public AjaxObject getAggregationAll(String patientSetId, JSONArray aggregationTeam, String projectId, String crfId) throws IOException {
//        String patientSql = getPatientSql(patientSetId,projectId,crfId);
        String patientSetSql = TransPatientSql.getUncomPatientSnSql(patientsSetMapper.getPatientsetSql(patientSetId));
        if(StringUtils.isEmpty(patientSetSql)){
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"没有数据");
        }
        String  newpatientSetSql = TransPatientSql.getAllPatientSql(patientSetSql,crfId);
//        if(StringUtils.isEmpty(patientSql)){
//            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"没有数据");
//        }
        String sql = "select "+IndexContent.getPatientDocId(crfId)+"   from "+ IndexContent.getIndexName(crfId,projectId) + " where "+newpatientSetSql+ " and  join_field='patient_info'";
        JSONArray terms_aggs = new JSONArray();
        int size = aggregationTeam == null ? 0 : aggregationTeam.size();
        for (int i = 0; i < size; i++) {
            JSONObject tmpObj = new JSONObject();
            JSONObject aggregationObj = aggregationTeam.getJSONObject(i);
            tmpObj.put("field", IndexContent.getPatientInfo(crfId)+"."+aggregationObj.getString("domain_name"));
            tmpObj.put("topN", 0);
            terms_aggs.add(tmpObj);
        }
        JSONObject termObj = new JSONObject();
        termObj.put("terms_aggs", terms_aggs);
        JSONArray source = new JSONArray();
        String result = httpUtils.querySearch(projectId,sql,1,1,null,source,termObj,crfId);
        Integer total = UqlQureyResult.getTotal(result);
        JSONObject aggregations = UqlQureyResult.getAggs(result,crfId,aggregationTeam);
        JSONArray data = new JSONArray();
        for (String key : aggregations.keySet()) {
            JSONObject tmpObj = new JSONObject();
            JSONObject aggreObj = aggregations.getJSONObject(key);
            tmpObj.put("count", total);
            tmpObj.put("domain_desc", aggreObj.getString("domain_desc"));
            tmpObj.put("domain_data", processingBuckets(aggreObj.getJSONArray("buckets"),total));
            data.add(tmpObj);
        }
        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
        ajaxObject.setData(data);
        return ajaxObject;
    }

    @Override
    public List<Patient> getpatentByUql(String patientSetId, boolean isExport, String projectId, String crfId) throws IOException {
        List<Patient> patientList = new LinkedList<>();
        String patientSetQuery = getPatientSql(patientSetId,projectId,crfId);
        String query = "select "+IndexContent.getPatientDocId(crfId)+"  from "+ IndexContent.getIndexName(crfId, projectId) + " where join_field = 'patient_info' and "+patientSetQuery;
        JSONArray source = new JSONArray();
        source.add("patient_info");
        JSONObject jsonData = JSONObject.parseObject(httpUtils.querySearch(projectId,query,0,Integer.MAX_VALUE-1,null,source,crfId));
        JSONArray hits = UqlQureyResult.getHitsArray(jsonData);
        int size = hits ==null ? 0 :hits.size();
        for (int i = 0; i < size; i++) {
            JSONObject tmpObj = hits.getJSONObject(i);
            if(tmpObj == null ){
                throw new CustomerException(CustomerStatusEnum.SUCCESS.toString(), "数据异常，操作失败，请重试");
            }
            JSONObject sourceObj = tmpObj.getJSONObject("_source");
//            JSONObject patientInfo = sourceObj.getJSONArray(IndexContent.getPatientInfo(crfId)).getJSONObject(0);
            JSONObject patientInfo  = IndexContent.getPatientInfoObj(sourceObj,crfId);
            String patientSn = patientInfo.getString("PATIENT_SN");
            String efhic = patientInfo.getString("ETHNIC");
            String nationality = patientInfo.getString("NATIONALITY");
            String maritalStatus =  patientInfo.getString("MARITAL_STATUS");
            String gender = patientInfo.getString("GENDER");
            String DOC_ID = patientInfo.getString("DOC_ID");
            Patient patient = new Patient(patientSn,efhic,nationality,maritalStatus,gender,DOC_ID);
            patientList.add(patient);
        }
        return patientList;
    }

    @Override
    public JSONArray getPatientListByPatientSn(List<GroupData> groupDataList, JSONArray columns, Integer activeType, String projectId, String crfId) {
        String where = getGroupPatientSn(groupDataList,crfId);
        String query = "select "+IndexContent.getPatientDocId(crfId)+" as patSn  from "+ IndexContent.getIndexName(crfId,projectId) + " where "+where+IndexContent.getGroupBy(crfId);
        JSONArray source = new JSONArray();
        source.add(IndexContent.getPatientInfo(crfId));
        JSONObject jsonData = JSONObject.parseObject(httpUtils.querySearch(projectId,query,0,Integer.MAX_VALUE-1,null,source,crfId));
        JSONArray hits = UqlQureyResult.getHitsArray(jsonData);
        return hits;
    }

    private String getGroupPatientSn(List<GroupData> groupDataList,String crfId) {
        StringBuffer stringBuffer = new StringBuffer();
        int num = 0;
        stringBuffer.append(IndexContent.getPatientInfoPatientSn(crfId)+" in (");
        for (GroupData groupData : groupDataList){
            String patientSn = groupData.getPatientSn();
            stringBuffer.append("'");
            stringBuffer.append(patientSn);
            stringBuffer.append("'");
            if(num<groupDataList.size()-1) stringBuffer.append(",");
            num++;
        }
        stringBuffer.append(")");
        return stringBuffer.toString();
    }

    @Override
    public String searchByActive(JSONObject object, String resultOrderKey, Integer isSearch) throws ExecutionException, InterruptedException, IOException {
        UqlClass uqlClass = null;
        String id = object.getString("id");
        String activeType = object.getString("activeType");
        JSONArray patientSetId = object.getJSONArray("patientSetId");
        String groupFromId = object.getString("groupFromId");
        String isVariant = object.getString("isVariant");
        String groupToId = object.getString("groupToId");
        groupToId =StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId;
        //获取初筛 sql
        String projectId = object.getString("projectId").replaceAll("-", "");
        String patientSql =  getInitialSQL(groupFromId,isVariant,groupToId,patientSetId,projectId);

        JSONObject config = object.getJSONArray("config").getJSONObject(0);
        String activeIndexId = config.getString("activeIndexId");//指标id
        String R_activeIndexId = isSearch == CommonContent.ACTIVE_TYPE_TEMP ? activeIndexId.concat("_tmp") : activeIndexId;
        String T_activeIndexId = "t".concat(activeIndexId);

        String indexType = config.getString("indexType");
        JSONArray contitions = config.getJSONArray("conditions");
        String indexDate = "visit_info.ADMISSION_DATE";
        String order1 = null;
        //事件
        String activeResult = config.getString("activeResult");
        String operator = config.getString("operator");
        String operaotrNum = config.getString("operatorNum");
        String parts[] = activeResult.split("\\.");
        String visits = parts[parts.length - 1];
        if( parts.length>2 && parts[0].equals("visits") && parts[1].equals("inspection_reports") && parts[2].equals("sub_inspection") ){
            visits = "inspection_reports";
        }
        String activeResultDocId = "";
        //事件处理
        if (StringUtils.isNotEmpty(activeResult)) {
            uqlClass = new ActiveUqlClass(projectId);
            JSONObject orderKeyObjet = JSONObject.parseObject(resultOrderKey);
            String orderKey = disposeVisits(orderKeyObjet.getString(activeResult));
            String order = FunctionUtilMap.getUqlFunction(operator, operaotrNum, "visit_info.DOC_ID", indexType, orderKey);
            if( parts.length>2 && parts[0].equals("visits") && parts[1].equals("inspection_reports") && parts[2].equals("sub_inspection") ){
                activeResultDocId = FunctionUtilMap.getUqlFunction(operator, operaotrNum, "sub_inspection.DOC_ID", indexType, orderKey);
            }else if("medical_record_home_page".equals(visits)){
                activeResultDocId = FunctionUtilMap.getUqlFunction(operator, operaotrNum, visits+".DOC_ID", indexType, orderKey);
            }else {
                activeResultDocId = FunctionUtilMap.getUqlFunction(operator, operaotrNum, visits+".DOC_ID", indexType, orderKey);
            }
            uqlClass.setSelect(uqlClass.getSelect() + order);
            order1 = orderKey;

            uqlClass.setSqlHaving(operaotrNum);
            uqlClass.setResultValue("visit_info.VISIT_SN");
            uqlClass.setActiveSelect(uqlClass.getSelect() );
            uqlClass.setResultFunction(operator);
            uqlClass.setResultFunctionNum(operaotrNum);
        }
        UqlWhere where = new UqlWhere();

        String hasCount = getActiveHasCount(activeResult);

        uqlClass.setActiveSelect(uqlClass.getSelect() + hasCount);

        JSONObject contitionObj = contitions.getJSONObject(0);
        transforEnumCondition(contitionObj, uqlClass, where, T_activeIndexId,groupToId,projectId,patientSetId,patientSql);

        UqlClass sqlresult = null;
        String sqlMd5 = "";
        String allWhere = "";
        String eventWhere = "";
        if(where.isSameGroup(visits)){
            uqlClass.setWhere(visits + ".VISIT_SN IS NOT NULL AND ");
            if (!"all".equals(operator) ) {
                uqlClass.setWhere(uqlClass.getWhere()+order1 + " IS NOT NULL AND ");
            }
            uqlClass.setInitialPatients(isVariant,patientSql);
            where.deleteHasChild();
            where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
            uqlClass.setWhere(uqlClass.getWhereNotNull() + "(" + where.toString() + " ) ");
            boolean isGetVisisn = !where.isEmpty();
            String sqlNew = uqlClass.getSql();
            /*开始搞 MD5 替换sql*/
            LOG.info("输出sql： " + sqlNew);
            sqlMd5 = StringToMd5.stringToMd5(sqlNew);
            Integer sqlMd5count = activeSqlMapMapper.getCountByActiveAndsqlMd5(R_activeIndexId,sqlMd5,groupToId);
            if(sqlMd5count>0) return null;

            //构造新sql 只有 visitSn 搜错功能
            allWhere = uqlClass.getWhere();
            sqlresult = uqlClass;
            eventWhere = sqlresult.getWhere().contains("join_field") ?sqlresult.getWhere():sqlresult.getWhere()+" and join_field='visit_info'";
            if("inspection_reports".equals(visits)){
                sqlresult.setWhere(sqlresult.getWhere()+" AND join_field = 'sub_inspection'");
            }else {
                sqlresult.setWhere(sqlresult.getWhere()+" AND join_field = '"+visits+"'");
            }

        }else {
            uqlClass.setActiveWhereIsEmpty(visits,where);
            uqlClass.setNotAllWhere(operator,order1,null, null);
            uqlClass.setIndexWhereIsEmpty(order1,where);
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

            //构造新sql 只有 visitSn 搜错功能
            allWhere = uqlClass.getWhere();
            if (StringUtils.isNotEmpty(activeResult) && isGetVisisn) {
                sqlresult = getIndexSql(uqlClass, operator, operaotrNum, activeResult, indexType, indexDate, projectId,hasCount);
            } else {
                sqlresult = uqlClass;
            }
            eventWhere = sqlresult.getWhere().contains("join_field") ?sqlresult.getWhere():sqlresult.getWhere()+" and join_field='visit_info'";

            String andGroupCondition = getActiveGroupCondition(where,activeResult );

            if(StringUtils.isNotEmpty(andGroupCondition)){
                sqlresult.setWhere(sqlresult.getWhere() + " and ("+andGroupCondition+")");
            }
            if("inspection_reports".equals(visits)){
                sqlresult.setWhere(sqlresult.getWhere()+" AND join_field = 'sub_inspection'");
            }else {
                sqlresult.setWhere(sqlresult.getWhere()+" AND join_field = '"+visits+"'");
            }
        }
        sqlresult.setSqlHaving(operaotrNum);
        String newSql = sqlresult.getHavingSql();

        Set<String> patients= getProjectPatients(projectId,patientSql);
        String allSql = "select patient_info.DOC_ID from "+uqlClass.getFrom()+" where " +allWhere+" group by patient_info.DOC_ID";
        String activeOtherPat = httpUtils.querySearch(projectId,allSql,1,Integer.MAX_VALUE-1,null,new JSONArray(),true);
        Set<String> allPats = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(JSON.parseObject(activeOtherPat))
            .stream()
            .map(String.class::cast)
            .collect(toSet());
        patients.removeAll(allPats);
        String otherResult = "";
        if (patients.isEmpty()) {
            otherResult = "";
        } else {
            otherResult = patients.stream().map(s -> "'" + s + "'").collect(joining(","));
        }

        LOG.info("新的sql------------------： " + newSql);
        ActiveSqlMap activeSqlMap = new ActiveSqlMap(projectId,R_activeIndexId,GzipUtil.compress(newSql),
            sqlresult.getSelect(),sqlresult.getFrom(),uqlClass.getSourceFilter(),activeType,
            uqlClass.getActiveId().toJSONString(),JSON.toJSONString(uqlClass.getSource()),activeResultDocId,activeResult,
            sqlMd5,GzipUtil.compress(eventWhere),hasCount.split(",")[1],GzipUtil.compress(otherResult),hasCount.split(",")[1]);
        activeSqlMap.setGroupId(StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId);
        activeSqlMap.setUncomSqlWhere(sqlresult.getWhere());
        int count = activeSqlMapMapper.getCountByActiveIndexId(R_activeIndexId,groupToId);
        Long mysqlStartTime = System.currentTimeMillis();
        if (count > 0) {
            activeSqlMapMapper.deleteByIndexId(R_activeIndexId);
        }
        activeSqlMapMapper.insert(activeSqlMap);
        LOG.info("数据库用时 :  "+(System.currentTimeMillis()-mysqlStartTime));
        /*引用依赖计算*/
//        getReferenceActiveIndex(id,resultOrderKey);
        return sqlresult.getSql();
    }

    @Override
    public String SearchByEnume(JSONObject obj, String resultOrderKey, Integer isSearch) throws ExecutionException, InterruptedException, IOException {
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
        //获取初筛 sql
        String R_activeIndexId =  isSearch == CommonContent.ACTIVE_TYPE_TEMP ? activeIndexId.concat("_tmp") : activeIndexId;
        String T_activeIndexId = "t" + activeIndexId;

        String patientSql = getInitialSQL(groupFromId,isVariant,groupToId,patientSetId,projectId);
//        List<ActiveSqlMap> activeSqlMaps = new ArrayList<>();
//        /*枚举其他处理  暂时不要其他 */
//        ActiveSqlMap otherActiveSqlMap = null;
//        UqlClass otherUql = null;
//        Set<String> enumPatients = new HashSet<>();

        int size = configs == null ? 0 : configs.size();
        redisMapDataService.delete(UqlConfig.CORT_INDEX_REDIS_KEY.concat(R_activeIndexId));
        List<ActiveSqlMap> delList = activeSqlMapMapper.getDelRedisActiveSql(R_activeIndexId);
        if(delList.size()>0){
            for (ActiveSqlMap src : delList){
                redisMapDataService.delete(UqlConfig.CORT_CONT_ENUM_REDIS_KEY + src.getActiveIndexId() + "_" + src.getId());
            }
        }
        activeSqlMapMapper.deleteByActiveIndexId(R_activeIndexId,groupToId);
        List<ActiveSqlMap> activeSqlMaps = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            uqlClass = new EnumeUqlClass(projectId);
            uqlClass.setActiveSelect(" patient_info.DOC_ID as pSn ");
            JSONObject config = configs.getJSONObject(i);
            String indexType = config.getString("indexType");
            JSONArray contitions = config.getJSONArray("conditions");
            if(contitions == null || contitions.size()==0){
                continue;
            }
            String indexDate = "visit_info.ADMISSION_DATE";
            //普通指标
            String indexColumn = disposeVisits(config.getString("indexColumn"));
            String function = config.getString("function");
            String indexTypeValue = DataType.fromString(indexType).name();
            String indexResultValue = config.getString("indexResultValue");
            String functionParam = config.getString("functionParam");
            Integer isOther = config.getInteger("isOther");
            //指标处理
            UqlWhere where = new UqlWhere();
            JSONObject contitionObj = contitions.getJSONObject(0);

            transforEnumCondition(contitionObj, uqlClass, where, T_activeIndexId,groupToId,projectId,patientSetId, patientSql);
            uqlClass.setWhereIsEmpty(where,null,isVariant,patientSql, null);

            String hasCount = " ,count(visit_info.DOC_ID) as jocount ";
            uqlClass.setActiveSelect(uqlClass.getSelect() + hasCount);

            where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
            uqlClass.setWhere(uqlClass.getWhereNotNull() + " ( " + where.toString()+" ) ");
            String sqlNew = uqlClass.getSql();
            LOG.info("输出sql： " + sqlNew);
//            String allWhere = uqlClass.getWhere();
            UqlClass sqlresult = null;
            if (StringUtils.isNotEmpty(indexColumn)) {
                sqlresult = getIndexSql(uqlClass, function, functionParam, indexColumn, indexType, indexDate, projectId,hasCount);
            } else {
                sqlresult = uqlClass;
            }
            String newSql = sqlresult.getSql();
//            LOG.info("新的sql： " + newSql);
            ActiveSqlMap activeSqlMap = new ActiveSqlMap(projectId,R_activeIndexId,GzipUtil.compress(newSql),sqlresult.getSelect(),
                                                            sqlresult.getFrom(),uqlClass.getSourceFilter(),uqlClass.getActiveId().toJSONString(),
                                                            JSON.toJSONString(uqlClass.getSource()),indexResultValue,indexTypeValue,
                                                            "patient_info.DOC_ID",name);
            activeSqlMap.setUncomSqlWhere(sqlresult.getWhere());
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
                    saveEnumCortrastiveResultRedisMap(activeSqlMaps,projectId,"EMR",R_activeIndexId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        return uqlClass.getSql();
    }
    @Override
    public Map<String, String> saveEnumCortrastiveResultRedisMap(List<ActiveSqlMap> activeSqlMaps, String projectId, String crfId, String activeIndexId) throws IOException {
        Map<String,EnumResult> map = new HashMap<>();
        for (ActiveSqlMap activeSqlMap : activeSqlMaps){
            transforEnumCortrastiveResultRedisMap(activeSqlMap,projectId,crfId,map);
        }
        Map<String,String> resMap = new HashMap<>();
        foreach(map, (key,val) -> resMap.put(key,val.toString()));
        String res= redisMapDataService.hmset(UqlConfig.CORT_INDEX_REDIS_KEY.concat(activeIndexId),resMap);
        redisMapDataService.setOutTime(UqlConfig.CORT_INDEX_REDIS_KEY.concat(activeIndexId),7 * 24 * 60 * 60);
        LOG.info(activeIndexId +" 插入 ---- redis" + res);
        return resMap;
    }

    private void transforEnumCortrastiveResultRedisMap(ActiveSqlMap activeSqlMap1, String projectId, String crfId, Map<String, EnumResult> map) throws IOException {
        String indexValue = activeSqlMap1.getIndexResultValue();
        activeSqlMap1.setUncomSqlWhere(activeSqlMap1.getUncomSqlWhere());
        String result = httpUtils.querySearch(projectId,activeSqlMap1.getUql(crfId),1,Integer.MAX_VALUE-1,null,new JSONArray(),crfId);
        List<String> list = new KeyPath("hits", "hits", "_source", "select_field", IndexContent.getPatientDocId(crfId))
            .fuzzyResolve(JSON.parseObject(result))
            .stream()
            .map(String.class::cast)
            .collect(toList());
        for(String key : list){
            if( !map.containsKey(key)){
                map.put(key, new EnumResult());
            }
            map.get(key).add(indexValue);
        }
    }

    @Override
    public Map<String,String> saveCortrastiveResultRedisMap(ActiveSqlMap activeSqlMap, String projectId, String crfId, String activeIndexId) throws IOException {
        activeSqlMap.setUncomSqlWhere(activeSqlMap.getUncomSqlWhere());
        String result = httpUtils.querySearch(projectId,activeSqlMap.getUql(crfId),1,Integer.MAX_VALUE-1,null,new JSONArray(),crfId);
        Map<String, String> map = new KeyPath("hits", "hits")
            .resolveAsJSONArray(JSON.parseObject(result))
            .stream()
            .map(new KeyPath("_source", "select_field")::resolveAsJSONObject)
            .collect(toMap(o -> o.getString(IndexContent.getPatientDocId(crfId)), o -> {
                String val =o.get("condition") == null ? "-":o.getString("condition");
                if(val.contains(".")){
                    try {
                        val = String.format("%.2f",  Double.parseDouble(val));
                    }catch (Exception e){
                    }
                }
                return val;
            }));
        String res= redisMapDataService.hmset(UqlConfig.CORT_INDEX_REDIS_KEY.concat(activeIndexId),map);
        redisMapDataService.setOutTime(UqlConfig.CORT_INDEX_REDIS_KEY.concat(activeIndexId),7 * 24 * 60 * 60);
        LOG.info(activeIndexId +" 插入 ---- redis" + res);
        return map;
    }

    private String getEnumOtherWhere(String cond, Set<String> enumPatients) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("patient_info.DOC_ID " + cond + " (");
        int num = 0;
        for (String patientSn : enumPatients) {
            stringBuffer.append("'");
            stringBuffer.append(patientSn);
            stringBuffer.append("'");
            if (num < enumPatients.size() - 1) stringBuffer.append(",");
            num++;
        }
        if (num == 0) {
            stringBuffer.append("'')");
        } else {
            stringBuffer.append(")");
        }
        return stringBuffer.toString();
    }

    public Set<String> getProjectPatients(String projectId,String patientSql) {
        String sql = "select  patient_info.DOC_ID as pSn   from rws_emr_" + projectId + " where "+patientSql+" group by patient_info.DOC_ID ";
        JSONArray source = new JSONArray();
        String result = httpUtils.querySearch(projectId,sql,1,Integer.MAX_VALUE-1,null,source,true);
        Set<String> patients = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(JSON.parseObject(result))
            .stream()
            .map(String.class::cast)
            .collect(toSet());
//        UqlVariable.producePat.put(projectId, patients);
        return patients;
    }

    private void transforEnumCondition(JSONObject contitionObj, UqlClass uqlClass, UqlWhere where, String activeIndexId,
                                       String groupId, String projectId, JSONArray patientSetId, String patientSql) throws IOException, ExecutionException, InterruptedException {
        String operatorSign = contitionObj.getString("operatorSign");
        JSONArray details = contitionObj.getJSONArray("details");
        JSONArray inner = contitionObj.getJSONArray("inner");
        JSONObject sortObj = sortForDetailAndInner(details,inner);
        details = sortObj.getJSONArray("detail");
        inner = sortObj.getJSONArray("inner");
        transforEnumDetails(details, uqlClass, operatorSign, where, activeIndexId,groupId,projectId,patientSetId,patientSql);
        int innerSize = inner == null ? 0 : inner.size();
        for (int i = 0; i < innerSize; i++) {
            if(where.needsOperator()){
                where.addElem(new LiteralUqlWhereElem(operatorSign));
            }
            where.addElem(new LiteralUqlWhereElem("("));
            JSONObject tmpObj = inner.getJSONObject(i);
            transforEnumCondition(tmpObj, uqlClass, where, activeIndexId,groupId,projectId,patientSetId, patientSql);
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
    private void transforEnumDetails(JSONArray details, UqlClass uqlClass, String operatorSign, UqlWhere where,
                                     String activeIndexId, String groupId, String projectId, JSONArray patientSetId, String patientSql) throws IOException, ExecutionException, InterruptedException {
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
            transforEnumDetailModel(detailObj, detailOperatorSign, elems, uqlClass, activeIndexId,groupId,projectId,patientSetId,patientSql);
            if (strongRefSize > 0) {
                elems.add(new LiteralUqlWhereElem("AND"));
//                where.addElem(new LiteralUqlWhereElem("("));
                transforEnumStrongRef(strongRef, strongRefSize, uqlClass, elems, activeIndexId,groupId,projectId,patientSetId,patientSql);
                elems.add(new LiteralUqlWhereElem(")"));
            }
            elemLists.add(elems);
        }
        elemLists.stream()
            .map(list -> makePair(
                topGroup((
                    list.get(0) instanceof  ReferenceConditionUqlWhereElem ?
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
                if ("visit_info".equals(group)) {
                    where.addElems(elems);
                } else {
                    Map<String,SimpleConditionUqlWhereElem> simpleMap = new HashMap<>();
                    List<UqlWhereElem> newElems = new ArrayList<>();
                    if("or".equals(operatorSign) ){
                        for (UqlWhereElem elem : elems){
                            if(elem instanceof SimpleConditionUqlWhereElem && ((SimpleConditionUqlWhereElem) elem).getJsonType().equals("string")){
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
                    where.addElem(new LiteralUqlWhereElem("haschild("));
                    where.addElems(newElems);
                    where.addElem(new LiteralUqlWhereElem(")"));

                }
            });
    }

    private String topGroup(String s) {
        return "sub_inspection".equals(s) ? "inspection_reports" : s;
    }

    private void transforEnumStrongRef(JSONArray strongRef, int strongRefSize, UqlClass uqlClass, List<UqlWhereElem> elems,
                                       String activeIndexId, String groupId, String projectId,JSONArray patientSetId,String patientSql) throws IOException, ExecutionException, InterruptedException {
        for (int i = 0; i < strongRefSize; i++) {
            if(i>0) elems.add(new LiteralUqlWhereElem("AND"));
            JSONObject tmpObj = strongRef.getJSONObject(i);
            transforEnumDetailModel(tmpObj, "and", elems, uqlClass, activeIndexId,groupId,projectId,patientSetId, patientSql);
        }
    }

    private void transforEnumDetailModel(JSONObject detailObj, String detailOperatorSign, List<UqlWhereElem> elems, UqlClass uqlClass,
                                         String activeIndexId, String groupId, String projectId, JSONArray patientSetId, String patientSql) throws IOException, ExecutionException, InterruptedException {
        StringBuffer stringBuffer = new StringBuffer();
        String stitching = detailObj.getString("Stitching");
        if (StringUtils.isEmpty(stitching)) {
            stitching = detailObj.getString("operatorSign");
        }
        String sourceName = detailObj.getString("sourceTagName");
        if(StringUtils.isEmpty(sourceName)) return;
        String sourceTagName = disposeVisits(sourceName);
        String value = detailObj.getString("value");
        String refActiveId = detailObj.getString("refActiveId");
        String condition = ConditionUtilMap.getCondition(stitching);
        String jsonType = detailObj.getString("jsonType");
        if (StringUtils.isNotEmpty(refActiveId)) { // 引用数据
            List<ActiveSqlMap> activeSqlMaps = activeSqlMapMapper.getActiveSql(refActiveId,groupId);
            if(activeSqlMaps ==null || activeSqlMaps.size()==0 ){
                referenceCalculate(refActiveId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null);
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
            //同期增加 all_value   all_value(visit_info.VISIT_SN,diagnose.DIAGNOSTIC_DATE)
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
            elems.add(new ReferenceConditionUqlWhereElem(sql, singletonList(sourceValue), sourceTagName, uqlClass.getFrom(), detailOperatorSign,false,"EMR",patientSql));
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
        value = disposeValue(value,jsonType);
        stringBuffer.append(sourceTagName);
        stringBuffer.append(" " + condition);
        stringBuffer.append(" " + value);
        String result = stringBuffer.toString();
        elems.add(new SimpleConditionUqlWhereElem(result, detailOperatorSign,jsonType,sourceTagName,condition,value));

    }

    @Override
    public String SearchByExclude(JSONObject object, String resultOrderKey, Integer isSearch) throws ExecutionException, InterruptedException, IOException {
        String projectId = object.getString("projectId").replaceAll("-", "");
        JSONArray patientSetId = object.getJSONArray("patientSetId");
        String groupFromId = object.getString("groupFromId");
        String isVariant = object.getString("isVariant");
        String groupToId = object.getString("groupToId");
        groupToId =StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId;
        String activeIndexId = object.getString("id");
        JSONArray config = object.getJSONArray("config");
        String patientSql = getInitialSQL(groupFromId,isVariant,groupToId,patientSetId,projectId);
        UqlWhere where = new UqlWhere();
        UqlClass uqlClass = new ExcludeUqlClass(projectId);
        transforConditionForConfig(config, uqlClass, where,groupToId,projectId,patientSetId);
        uqlClass.setInitialPatients(isVariant,patientSql);
        activeIndexId = isSearch == CommonContent.ACTIVE_TYPE_TEMP ? activeIndexId.concat("_tmp") : activeIndexId;
        ActiveSqlMap activeSqlMap = new ActiveSqlMap(projectId,activeIndexId,GzipUtil.compress(uqlClass.getSql()),uqlClass.getSelect(),uqlClass.getFrom(),
                                                        uqlClass.getActiveId().toJSONString(),JSON.toJSONString(uqlClass.getSource()));
        activeSqlMap.setUncomSqlWhere(uqlClass.getWhere());
        activeSqlMap.setGroupId(StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId);
        int count = activeSqlMapMapper.getCountByActiveIndexId(activeIndexId,groupToId);
        if (count > 0) {
            activeSqlMapMapper.updateByActiveId(activeSqlMap);
        } else {
            activeSqlMapMapper.insert(activeSqlMap);
        }
        return uqlClass.getSql();
    }

    private String getGroupSql(String groupId) {
        List<String> groupDataPatSn = groupDataMapper.getPatientSnList(groupId);
        String query = null;
        if(groupDataPatSn ==null || groupDataPatSn.size()==0){
            query = "''";
        }else {
            query = TransPatientSql.transForExtContain(groupDataPatSn);
        }
        return  " visit_info.PATIENT_SN " + query;
    }


    private String getPatientSql(String patientSetId,String projectId,String crfId) throws IOException {
        String patientSetSql = TransPatientSql.getUncomPatientSnSql(patientsSetMapper.getPatientsetSql(patientSetId));
        if(StringUtils.isEmpty(patientSetSql)){
            return null;
        }
        String  newpatientSetSql = TransPatientSql.getAllPatientSql(patientSetSql,crfId);
        JSONArray sourceFilter = new JSONArray();
//        sourceFilter.add("patient_info");
        String result = null;
        String newSql = "select  "+IndexContent.getPatientDocId(crfId)+" as pSn from "+ IndexContent.getIndexName(crfId,projectId)+" where join_field = 'patient_info' and "+newpatientSetSql ;
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
    private String getPatientSqlForIds(JSONArray patientSetId,String projectId,Set<String> docIds) {
        List<String> patientSets = patientSetId.toJavaList(String.class);
        List<String> patientSetSql = patientsSetMapper.getPatientsetSqlAll(patientSets);
        String query = String.join(" or ",patientSetSql.stream().map( x -> "("+TransPatientSql.getAllPatientSql(TransPatientSql.getUncomPatientSnSql(x))+")").collect(toList()));
        JSONArray sourceFilter = new JSONArray();
        String result = null;
        String newSql = "select patient_info.DOC_ID as pSn from rws_emr_"+projectId+" where "+query+" group by patient_info.DOC_ID";
        String response = httpUtils.querySearch(projectId,newSql,1,Integer.MAX_VALUE-1,null,sourceFilter,true);
        Set<String> patients = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(JSON.parseObject(response))
            .stream()
            .map(String.class::cast)
            .collect(toSet());
        patients.removeAll(docIds);
        if (patients.isEmpty()) {
            result = "patient_info.DOC_ID IN ('')";
        } else {
            result = "patient_info.DOC_ID " + TransPatientSql.transForExtContain(patients);
        }
        return result;
    }

    private String getPatientSql(JSONArray patientSetId,String projectId) {
        List<String> patientSets = patientSetId.toJavaList(String.class);
        List<String> patientSetSql = patientsSetMapper.getPatientsetSqlAll(patientSets);
        String query = String.join(" or ",patientSetSql.stream().map( x -> "("+TransPatientSql.getAllPatientSql(TransPatientSql.getUncomPatientSnSql(x))+")").collect(toList()));
        JSONArray sourceFilter = new JSONArray();
        String result = null;
        String newSql = "select patient_info.DOC_ID as pSn from rws_emr_"+projectId+" where "+query+" group by patient_info.DOC_ID";
        String response = httpUtils.querySearch(projectId,newSql,1,Integer.MAX_VALUE-1,null,sourceFilter,true);
        Set<String> patients = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(JSON.parseObject(response))
            .stream()
            .map(String.class::cast)
            .collect(toSet());

        if (patients.isEmpty()) {
            result = "patient_info.DOC_ID IN ('')";
        } else {
            result = "patient_info.DOC_ID " + TransPatientSql.transForExtContain(patients);
        }
        return result;
    }

    private void transforConditionForConfig(JSONArray config, UqlClass uqlClass, UqlWhere where,String groupId,String projectId,JSONArray patientSetId) throws InterruptedException, ExecutionException {
        StringBuffer resultBuffer = new StringBuffer();
        JSONObject incs = config.stream().map(JSONObject.class::cast).filter(o -> "纳入标准".equals(o.getString("activeResult"))).findAny().get();
        JSONObject excs = config.stream().map(JSONObject.class::cast).filter(o -> "排除标准".equals(o.getString("activeResult"))).findAny().get();
        transforConditionForConditions(incs.getJSONArray("conditions"), uqlClass, where, false, true, true ,false,groupId,projectId,patientSetId);
        transforConditionForConditions(excs.getJSONArray("conditions"), uqlClass, where, true, where.isEmpty(), true ,true,groupId,projectId,patientSetId);
        where.addElem(new LiteralUqlWhereElem("AND join_field='visit_info'"));
        where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
        uqlClass.setWhere(where.toString());
        uqlClass.setSelect("patient_info.DOC_ID");
    }

    private void transforConditionForConditions(JSONArray conditions, UqlClass uqlClass, UqlWhere where, boolean not, boolean first,
                                                boolean op,boolean isNot,String groupId, String projectId,JSONArray patientSetId) {
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
                transforConditionForConditions(condition.getJSONArray("inner"), uqlClass, where, false, true, subOp ,isNot,groupId,projectId,patientSetId);
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
                            referenceCalculate(refId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null);
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
                                    false,
                                    "EMR"
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
                                false,
                                "EMR"
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
                                false,
                                "EMR"
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
                                    false)));
                                where.addElem(new LiteralUqlWhereElem("AND"));
                            }
                        );
                    }
                    where.addElem(new LiteralUqlWhereElem(type.serialize(
                        o.getString("sourceTagName"),
                        o.getString("operatorSign"),
                        o.get("value"),
                        false)));
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
    public String SearchByIndex(JSONObject object, String resultOrderKey, Integer isSearch) throws ExecutionException, InterruptedException, IOException  {
        UqlClass uqlClass = null;

        JSONArray patientSetId = object.getJSONArray("patientSetId");
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

        JSONObject resultObj = JSONObject.parseObject(resultOrderKey);

        String patientSql = getInitialSQL(groupFromId,isVariant,groupToId,patientSetId,projectId);

        String indexDate = "";
        String indexCol = config.getString("indexColumn").substring(0, config.getString("indexColumn").lastIndexOf("."));
        //普通指标
        String indexColumn = disposeVisits(config.getString("indexColumn"));
        String indexDateTmp = resultObj.getString(indexCol);
        String [] indexPath = indexDateTmp.split("\\.");
        indexDate = indexPath[indexPath.length-2]+"."+indexPath[indexPath.length-1];

        String function = config.getString("function");
        String functionParam = config.getString("functionParam");
        String order1 = null;
        String selectValue = null;
        String parts[] = indexColumn.split("\\.");
        String visits = parts[parts.length - 2];
        if( parts.length>1 && parts[0].equals("sub_inspection") ){
            visits = "inspection_reports";
        }
        //指标处理
        if (StringUtils.isNotEmpty(indexColumn)) {
            uqlClass = new IndexUqlClass(projectId);
            String order = FunctionUtilMap.getUqlFunction(function, functionParam, indexColumn, indexType, indexDate);
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

        String hasCount = getIndexHasCount(indexColumn);

        uqlClass.setActiveSelect(uqlClass.getSelect() + hasCount);

        //处理 条件
        transforEnumCondition(contitionObj, uqlClass, where, R_activeIndexId,groupToId,projectId,patientSetId, patientSql);
        UqlClass sqlresult = null;
        String sqlMd5 = "";
        redisMapDataService.delete(UqlConfig.CORT_INDEX_REDIS_KEY.concat(T_activeIndexId));
        redisMapDataService.delete(UqlConfig.CORT_CONT_ACTIVE_REDIS_KEY.concat(T_activeIndexId));
        /*------------------------------------------------------------------------------*/
        if(where.isSameGroup(visits)){
            uqlClass.setWhere(order1 + " IS NOT NULL AND ");
            uqlClass.setInitialPatients(isVariant,patientSql);

            if ("first".equals(function) || "last".equals(function)  || "index".equals(function) || "reverseindex".equals(function)  ) {
                uqlClass.setWhere(uqlClass.getWhere()+indexDate + " IS NOT NULL AND " );
            }

            where.deleteHasChild();
            where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
            uqlClass.setWhere(uqlClass.getWhereNotNull() + " ( "+ where.toString()+" ) ");
            boolean isGetVisisn = !where.isEmpty();
            String sqlNew = uqlClass.getHavingSql();
            /*开始搞 MD5 替换sql*/
            LOG.info("输出sql： " + sqlNew);
            sqlMd5 = StringToMd5.stringToMd5(sqlNew);
            Integer sqlMd5count = activeSqlMapMapper.getCountByActiveAndsqlMd5(T_activeIndexId,sqlMd5,groupToId);
            if(sqlMd5count>0) return null;

            sqlresult = uqlClass;

             if("inspection_reports".equals(visits)){
                sqlresult.setWhere(sqlresult.getWhere()+" and join_field = 'sub_inspection' ");
            }else {
                sqlresult.setWhere(sqlresult.getWhere()+" and join_field = '"+visits+"'");
            }
        }else{
            uqlClass.setWhereIsEmpty(where,order1,null,null, null);
            uqlClass.setInitialPatients(isVariant,patientSql);
            uqlClass.setNotAllWhere(function,order1,indexDate, null);

            where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());

            uqlClass.setWhere(uqlClass.getWhereNotNull() + " ( "+ where.toString()+" ) ");
            boolean isGetVisisn = !where.isEmpty();
            String sqlNew = uqlClass.getHavingSql();
            /*开始搞 MD5 替换sql*/
            LOG.info("输出sql： " + sqlNew);
            sqlMd5 = StringToMd5.stringToMd5(sqlNew);
            Integer sqlMd5count = activeSqlMapMapper.getCountByActiveAndsqlMd5(T_activeIndexId,sqlMd5,groupToId);
            if(sqlMd5count>0) return null;

            if (StringUtils.isNotEmpty(indexColumn) && isGetVisisn) {
                sqlresult = getIndexSql(uqlClass, function, functionParam, indexColumn, indexType, indexDate, projectId,hasCount);
            } else {
                sqlresult = uqlClass;
            }

            String andGroupCondition =getIndexGroupCondition(where,indexColumn);
            if(StringUtils.isNotEmpty(andGroupCondition)){
                sqlresult.setWhere(sqlresult.getWhere() + " and ("+andGroupCondition.toString()+")");
            }

             if("inspection_reports".equals(visits)){
                sqlresult.setWhere(sqlresult.getWhere()+" and join_field = 'sub_inspection' ");
            }else {
                sqlresult.setWhere(sqlresult.getWhere()+" and join_field = '"+visits+"'");
            }
        }
        sqlresult.setSqlHaving(functionParam);
        String newSql = sqlresult.getHavingSql();


        ActiveSqlMap activeSqlMap = new ActiveSqlMap(projectId,T_activeIndexId,GzipUtil.compress(newSql),
                                                        sqlresult.getSelect(),sqlresult.getFrom(),uqlClass.getSourceFilter(),
                                                        uqlClass.getActiveId().toJSONString(),JSON.toJSONString(uqlClass.getSource()),selectValue,
                                                        indexTypeValue,name,hasCount.split(",")[1],
                                                        sqlMd5);
        activeSqlMap.setUncomSqlWhere(sqlresult.getWhere());
        activeSqlMap.setGroupId(StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId);
        activeSqlMap.setSqlHaving(uqlClass.getHaving());
        if(StringUtils.isEmpty(groupToId) || UqlConfig.CORT_INDEX_ID.equals(groupToId)){
            SingleExecutorService.getInstance().getFlushCountGroupExecutor().submit(() -> {
                try {
                    saveCortrastiveResultRedisMap(activeSqlMap,projectId,"EMR",T_activeIndexId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        int count = activeSqlMapMapper.getCountByActiveIndexId(T_activeIndexId,groupToId);
        Long mysqlStartTime = System.currentTimeMillis();
        if (count > 0) {
//            activeSqlMapMapper.updateByActiveId(activeSqlMap);
            activeSqlMapMapper.deleteByIndexId(T_activeIndexId);
            referenceCalculate(T_activeIndexId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),null,UqlConfig.CORT_INDEX_ID,null);
        }
        activeSqlMapMapper.insert(activeSqlMap);
        LOG.info("数据库用时 :  "+(System.currentTimeMillis()-mysqlStartTime));
        /*引用依赖计算*/
        getReferenceActiveIndex(id,resultOrderKey,patientSetId,groupToId,groupFromId);
        return sqlresult.getSql();
    }

    private void disposeDataCondition(String value, String condition, String sourceTagName, StringBuffer stringBuffer) {
        JSONArray valueArray = JSONArray.parseArray(value);
        String date1 = "" ;
        String date2 = "" ;
        if("medical_record_home_page.BIRTH_DATE".equals(sourceTagName)){
            date1 = valueArray.getString(0);
            date2 = valueArray.getString(1);
        }else {
             date1 = valueArray.getString(0);
             date2 = valueArray.getString(1);
        }
        stringBuffer.append(sourceTagName + " between '" + date1 + "' and '" + date2 + "'");
    }

    private UqlClass getIndexSql(UqlClass uqlClass, String function, String functionParam, String indexColumn, String indexType, String indexDate, String projectId,String hascount) {
        UqlClass resultUql = new StandardUqlClass();
        resultUql.setActiveSelect(uqlClass.getSelect());
        resultUql.setFrom(uqlClass.getFrom());

        String where = getIndexResultWhere(uqlClass, function, functionParam, indexColumn, indexType, indexDate, projectId,hascount);
        resultUql.setWhere(where);
        return resultUql;
    }

    private String getIndexResultWhere(UqlClass uqlClass, String function, String functionParam, String indexColumn, String indexType, String indexDate, String projectId,String hasCount) {
        String order = FunctionUtilMap.getUqlFunction(function, functionParam, "visit_info.DOC_ID", indexType, indexDate);
        uqlClass.setSelect(order);
        uqlClass.setSelect(" group_concat(visit_info.DOC_ID,',') ");
        String sql = uqlClass.getSql();
        JSONArray source = new JSONArray();
        String result = httpUtils.querySearch(projectId,sql,1,Integer.MAX_VALUE-1,null,source,true);
        JSONObject jsonData = JSONObject.parseObject(result);
        String visitSnAll = UqlQureyResult.getVisitSnAll(jsonData);
        return "visit_info.DOC_ID " + visitSnAll;
    }

    private void disposeBooleanCondition(StringBuffer resultBuffer, String value, String condition, String sourceTagName) {
        JSONArray values = JSONArray.parseArray(value);
        int size = values == null ? 0 : values.size();
        for (int i = 0; i < size; i++) {
            if (size > 1) resultBuffer.append(" and ");
            resultBuffer.append(sourceTagName);
            resultBuffer.append(condition);
            String val = values.getString(i);
            val = "否".equals(val) ? "false" : val;
            val = "是".equals(val) ? "true" : val;
            resultBuffer.append(val);
        }

    }

    private String disposeValue(String value,String jsonType) {
        if (!value.startsWith("[") && !value.startsWith("(")) {
            try {
                if("string".equals(jsonType)){
                    value = "'" + value + "'";
                }else {
                    Integer.valueOf(value);
                }
            } catch (NumberFormatException e) {
                value = "'" + value + "'";
            }
            return value;
        }
        if (value.startsWith("[")) {
            value = value.substring(1, value.length() - 1);
        }
        value = value.replace("\"", "'");

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
        if (visits.startsWith("visits")) {
            return visits.substring(visits.indexOf(".") + 1, visits.length());
        }
        return visits;
    }

    private static JSONArray getSource(JSONArray showColumns, String keys, JSONArray array) {

        int size = showColumns == null ? 0 : showColumns.size();
        for (int i = 0; i < size; i++) {
            JSONObject tmpColumn = showColumns.getJSONObject(i);
            array.add(keys + "." + tmpColumn.getString("id"));
        }
        return array;
    }

    private JSONArray processingBuckets(JSONArray buckets, Integer total) {
        JSONArray domainData = new JSONArray();
        int size = buckets == null ? 0 : buckets.size();
        Integer tmpCount = 0;
        for (int i = 0; i < size; i++) {
            JSONObject bucketObj = buckets.getJSONObject(i);
            JSONObject tmpObj = new JSONObject();
            Integer count =  bucketObj.getInteger("doc_count");
            tmpObj.put("count",count);
            tmpObj.put("name", bucketObj.getString("key"));
            domainData.add(tmpObj);
            tmpCount +=count;
        }
        if(total - tmpCount !=0){
            JSONObject otherObj = new JSONObject();
            Integer otherCount = total - tmpCount;
            otherObj.put("count",otherCount);
            otherObj.put("name","无数据");
            domainData.add(otherObj);
        }
        return domainData;
    }

    /*获取枚举结果*/
    private AjaxObject searchClasEnumResultByUql(String activeId, List<ActiveSqlMap> sqlList, String projectId, Integer pageSize, Integer pageNum,
                                                 JSONArray basicColumns, String groupFromId, JSONArray patientSetIds, String groupId, String isVariant,JSONArray patientSetId) throws IOException, ExecutionException, InterruptedException {
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
                referenceCalculate(refActiveId.substring(1, refActiveId.length()),projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null);
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
                patientSql = getPatientSql(patientSetIds,projectId);
            }else{
                patientSql = getGroupSql(groupFromId);
            }
        }


        //查找全部数据
//        String allSql = UqlConfig.getPatientSetSql(projectId,patientSql);
        String allSql = UqlConfig.getEnumAllSql(projectId,sqlList,patientSql);
        JSONArray allSource = new JSONArray();
        allSource.add("patient_info");
        String result = httpUtils.querySearch(projectId,allSql,pageNum,pageSize,null, allSource,false);
        Integer total = UqlQureyResult.getTotal(result);

        JSONArray data  = transforEnumResult(JSON.parseObject(result), sqlList, projectId, activeId,pageSize);
        List<String> pasSn = new ArrayList<>();
        int daTasize = data == null ? 0 : data.size();
        Map<String, JSONObject> dataMap = new HashMap<>();
        for (int i = 0; i < daTasize; i++) {
            JSONObject tmpObj = data.getJSONObject(i);
            pasSn.add(tmpObj.getString("DOC_ID"));
            dataMap.put(tmpObj.getString("DOC_ID"), tmpObj);
        }
        String patSnWhere = "";
        if(pasSn.size()==0){
            patSnWhere = "patient_info.DOC_ID IN ('')";
        }else {
            patSnWhere = "patient_info.DOC_ID " +TransPatientSql.transForExtContain(pasSn);
        }
        JSONArray source = new JSONArray();
        source.add("patient_info.DOC_ID");
        Long tmie3 = System.currentTimeMillis();
        for (int i = 0; i < refSize; i++) {
            //拼接column
            String refActiveId = refActiveIds.getString(i);
            List<ActiveSqlMap> patSqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, refActiveId.substring(1),groupId);
            if(patSqlList == null || patSqlList.size() == 0 ){
                referenceCalculate(refActiveId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null);
                patSqlList =activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, refActiveId,groupId);
            }
            if(patSqlList.size() == 0 ) continue;
            ActiveSqlMap patActiveSqlMap = patSqlList.get(0);
            String patSql = patActiveSqlMap.getUncomActiveSql();
            String[] patSqls = patSql.split("where");
            String where = patSqls[1];
            String newWhere = patSnWhere + " and " + where;
            String patSnResult = httpUtils.querySearch(projectId, patSqls[0] + " where "+ newWhere, 1, pageSize, "", source,false);
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
                String patSn = tmpHits.getJSONObject(j).getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0).getString("DOC_ID");
                dataMap.get(patSn).put(refActiveId, colle);
            }
            for(String key : dataMap.keySet()){
                JSONObject obj = dataMap.get(key);
                if(!obj.containsKey(refActiveId)){
                    obj.put(refActiveId,"-");
                }
            }
        }
//        Integer count = getProjectCount(projectId);
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
            count = getPatientSqlCount(patientSetIds,projectId);
        }else {
            count = getGroupSqlCount(groupFromId);
        }
        saveActiveIndexTask(activeId, projectId, total);
        AjaxObject.getReallyDataValue(data,basicColumns);
        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
        ajaxObject.setData(data);
        ajaxObject.setCount(count);
        ajaxObject.setColumns(basicColumns);
        WebAPIResult webAPIResult = new WebAPIResult(pageNum, pageSize, total);
        ajaxObject.setWebAPIResult(webAPIResult);
        deleteTmpActiveId(projectId,activeId);
        return ajaxObject;
    }

    private JSONArray  transforEnumResult(JSONObject data, List<ActiveSqlMap> sqlList, String projectId, String activeId,Integer size) throws IOException {

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
            String newWhere = " patient_info.DOC_ID " + patsStr +" and ("+where+" )";
            activeSqlMap.setUncomSqlWhere(newWhere);
            String sql = activeSqlMap.getUql();
            JSONArray source= new JSONArray();
            source.add("patient_info.PATIENT_SN");
            String patData = httpUtils.querySearch(projectId,sql, 1,size,null,source,false);
            activeSqlMap.setUncomSqlWhere(where);
            Set<String> values = JSONObject.parseObject(patData)
                .getJSONObject("hits")
                .getJSONArray("hits")
                .stream()
                .map(JSONObject.class::cast)
                .map(o -> o.getJSONObject("_source"))
                .flatMap(o -> o.getJSONArray("patient_info")
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
            JSONObject obj = hitsArray.getJSONObject(i).getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0);
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


    public void getReferenceActiveIndex(String activeId,String resultOrderKey,JSONArray patientsSetId,String groupToId,String groupFromId){
        int isTmp = 0;
        List<ActiveIndex> activeIndices = activeIndexMapper.findReferenceActiveIndex(activeId,isTmp);

        for (ActiveIndex activeIndex : activeIndices){
            String projectId = activeIndex.getProjectId();
            String activeIdTmp = activeIndex.getId();
            Integer activeType = activeIndex.getActiveType();
            SingleExecutorService.getInstance().getReferenceActiveExecutor().submit(() -> {
                try {
                    if("1".equals(activeIndex.getIsVariant())){
                        referenceCalculate(activeIdTmp,projectId,activeType,resultOrderKey, null, UqlConfig.CORT_INDEX_ID, null);
                    }else {
                        referenceCalculate(activeIdTmp,projectId,activeType,resultOrderKey, patientsSetId, groupToId, groupFromId);
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

    public void referenceCalculate(String activeId, String projectId, Integer activeType, String resultOrderKey,JSONArray patientsSetId,String groupToId,String groupFromId) throws ExecutionException, InterruptedException, IOException {

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
        activeType = active.getActiveType();
        String sql = "";
        if (3 == activeType) {//那排
            sql =  this.SearchByExclude(obj, resultOrderKey,isSearch);
        } else if ("自定义枚举类型".equals(indexTypeDesc)) {//处理枚举
            sql =   this.SearchByEnume(obj, resultOrderKey,isSearch);
        } else if(2 == activeType) {//指标
            sql =   this.SearchByIndex(obj, resultOrderKey,isSearch);
        }else  if(1 == activeType){ //事件
            sql =   this.searchByActive(obj, resultOrderKey,isSearch);
        }

    }
    @Override
    public AjaxObject getPatientSnsByAll(String patientSetId, String projectId, JSONArray showColumns, JSONArray actives, Integer pageNum, Integer pageSize, Integer type, String crfId) throws IOException {
        String patientSetSql = TransPatientSql.getUncomPatientSnSql(patientsSetMapper.getPatientsetSql(patientSetId));
        if(StringUtils.isEmpty(patientSetSql)){
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"没有数据");
        }
        String newpatientSetSql = TransPatientSql.getAllPatientSql(patientSetSql,crfId);
        String newSql = "select  "+IndexContent.getPatientDocId(crfId)+" as pSn from "+ IndexContent.getIndexName(crfId,projectId)+" where "+newpatientSetSql + " and  join_field='patient_info'";
        JSONArray source = new JSONArray();
        source.add("patient_info");
        JSONObject jsonData = JSONObject.parseObject(httpUtils.querySearch(projectId,newSql,pageNum,pageSize,null,source,crfId));
        JSONArray data = UqlQureyResult.getQueryData(jsonData,crfId);
        Integer total = patientSetSql.split("\\|").length;
        WebAPIResult webAPIResult = new WebAPIResult(pageNum, pageSize, total);
        AjaxObject.getReallyDataValue(data,showColumns);
        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
        ajaxObject.setColumns(showColumns);
        ajaxObject.setData(data);
        ajaxObject.setWebAPIResult(webAPIResult);
        return ajaxObject;
    }

    @Override
    public AjaxObject getPatientListByAllByPatientSetIds(JSONArray patientSetIdTmp, String projectId, JSONArray showColumns, JSONArray actives, Integer pageNum, Integer pageSize, int i, String crfId) {
        List<String> patientSets = patientSetIdTmp.toJavaList(String.class);
        List<String> patientSetSql = patientsSetMapper.getPatientsetSqlAll(patientSets);
        String query1 = String.join(" or ",patientSetSql.stream().map( x -> "("+TransPatientSql.getAllPatientSql(TransPatientSql.getUncomPatientSnSql(x),crfId)+")").collect(toList()));
        String query = "select "+IndexContent.getPatientDocId(crfId)+"  from "+ IndexContent.getIndexName(crfId,projectId) + " where  ("+query1+" )"+IndexContent.getGroupBy(crfId);//join_field='visit_info' and
        JSONArray source = new JSONArray();
        source.add("patient_info");
        JSONObject jsonData = JSONObject.parseObject(httpUtils.querySearch(projectId,query,pageNum,pageSize,null,source,crfId,false));
        JSONArray data = UqlQureyResult.getQueryData(jsonData,crfId);
        Integer total = UqlQureyResult.getTotal(jsonData);
        WebAPIResult webAPIResult = new WebAPIResult(pageNum, pageSize, total);
        AjaxObject.getReallyDataValue(data,showColumns);
        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
        ajaxObject.setColumns(showColumns);
        ajaxObject.setData(data);
        ajaxObject.setWebAPIResult(webAPIResult);
        return ajaxObject;
    }

    /**
     * 获取患者的初筛条件
     * @param groupFromId 大组id
     * @param isVariant   是否为研究变量
     * @param groupToId   当前组id
     * @param patientSetId   引入的患者集id
     * @param projectId    项目id
     * @return
     */
    public String getInitialSQL(String groupFromId,String isVariant,String groupToId,JSONArray patientSetId,String projectId){
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
                patientSql = getPatientSql(patientSetId,projectId);
            }else{
                patientSql = getGroupSql(groupFromId);
            }
        }
        return patientSql;
    }

    public String getIndexHasCount(String indexColumn){
        String hasCount = "";
        String[] parts  = indexColumn.split("\\.");
        String visits = parts[parts.length - 2];
        if( parts.length>1 && parts[0].equals("sub_inspection") ){
            hasCount = " ,count(sub_inspection.INSPECTION_SN) as jocount ";
        }else if(parts.length>1 && parts[0].equals("inspection_reports")){
            hasCount = " , distinct_count("+visits+".INSPECTION_SN) as jocount ";
        }else {
            hasCount =" ,count(" + visits + ".DOC_ID) as jocount ";
        }
        return hasCount;
    }
    public String getActiveHasCount(String activeResult){
        String parts[] = activeResult.split("\\.");
        String visits = parts[parts.length - 1];
        String hasCount = "";
        if( parts.length>2 && parts[2].equals("sub_inspection") ){
            hasCount =" , count(sub_inspection.DOC_ID) as jocount ";
        }else if(parts.length>1 && parts[1].equals("inspection_reports")){
            hasCount = " , distinct_count(inspection_reports.DOC_ID) as jocount ";
        }else if("medical_record_home_page".equals(visits)){
            hasCount =" , distinct_count(medical_record_home_page.DOC_ID) as jocount ";
        } else {
            hasCount =" ,count(" + visits + ".DOC_ID) as jocount ";
        }
        return hasCount;
    }

    public String getIndexGroupCondition(UqlWhere where,String indexColumn){
        StringBuffer andGroupCondition = new StringBuffer();
        for (UqlWhereElem elem : where) {
            String s = elem.toString().trim();
            if (elem instanceof LiteralUqlWhereElem){
                andGroupCondition.append(" "+s.replaceAll("haschild","")+" ");
            }else if (elem instanceof SimpleConditionUqlWhereElem || elem instanceof ReferenceConditionUqlWhereElem) {
                String indexTarget = indexColumn.substring(0, indexColumn.indexOf("."));
                if (s.startsWith(indexTarget) || s.startsWith("sub_inspection") && indexTarget.startsWith("inspection_reports")) {
                    andGroupCondition.append(" "+s+" ");
                }else if(s.startsWith(indexTarget) || s.startsWith("inspection_reports") && indexTarget.startsWith("sub_inspection")){
                    andGroupCondition.append(" "+s+" ");
                }else if (s.startsWith("haschild("+indexTarget) || s.startsWith("haschild(sub_inspection") && indexTarget.startsWith("inspection_reports")) {
                    andGroupCondition.append(" " + s.substring(0,s.length()-1).replaceAll("haschild\\("," "));
                }else if(s.startsWith("haschild("+indexTarget) || s.startsWith("haschild(inspection_reports") && indexTarget.startsWith("sub_inspection")){
                    andGroupCondition.append(" " + s.substring(0,s.length()-1).replaceAll("haschild\\("," "));
                }else {
                    andGroupCondition.append("visit_info.VISIT_SN  IS NOT NULL");
                }
            }
        }
        return andGroupCondition.toString();
    }

    private String getActiveGroupCondition(UqlWhere where, String activeResult) {
        StringBuffer andGroupCondition = new StringBuffer();
        for (UqlWhereElem elem : where) {
            String s = elem.toString().trim();
            if (elem instanceof LiteralUqlWhereElem){
                andGroupCondition.append(" "+s.replaceAll("haschild","")+" ");
            }else if (elem instanceof SimpleConditionUqlWhereElem || elem instanceof ReferenceConditionUqlWhereElem) {
                String indexTarget = activeResult.split("\\.")[1];
                if (s.startsWith(indexTarget) || s.startsWith("sub_inspection") && indexTarget.startsWith("inspection_reports")) {
                    andGroupCondition.append(" "+s+" ");
                }else if(s.startsWith(indexTarget) || s.startsWith("inspection_reports") && indexTarget.startsWith("sub_inspection")){
                    andGroupCondition.append(" "+s+" ");
                }else if (s.startsWith("haschild("+indexTarget) || s.startsWith("haschild(sub_inspection") && indexTarget.startsWith("inspection_reports")) {
                    andGroupCondition.append(" " + s.substring(0,s.length()-1).replaceAll("haschild\\("," "));
                }else if(s.startsWith("haschild("+indexTarget) || s.startsWith("haschild(inspection_reports") && indexTarget.startsWith("sub_inspection")){
                    andGroupCondition.append(" " + s.substring(0,s.length()-1).replaceAll("haschild\\("," "));
                }else {
                    andGroupCondition.append("visit_info.VISIT_SN  IS NOT NULL");
                }
            }
        }
       return andGroupCondition.toString();
    }

}
