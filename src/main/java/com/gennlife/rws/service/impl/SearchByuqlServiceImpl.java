package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.darren.collection.Pair;
import com.gennlife.darren.collection.keypath.KeyPath;
import com.gennlife.rws.content.*;
import com.gennlife.rws.dao.*;
import com.gennlife.rws.entity.*;
import com.gennlife.rws.exception.CustomerException;
import com.gennlife.rws.query.UqlQureyResult;
import com.gennlife.rws.service.*;
import com.gennlife.rws.uql.*;
import com.gennlife.rws.uqlcondition.*;
import com.gennlife.rws.util.*;
import com.gennlife.rws.vo.CustomerStatusEnum;
import com.gennlife.rws.web.WebAPIResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collector;

import static com.gennlife.darren.collection.Pair.makePair;
import static com.gennlife.darren.controlflow.exception.Force.force;
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
    @Autowired
    private PatientSetService patientSetService;
    @Autowired
    private LiminaryContent liminaryContent;

    @Override
    public String SearchByIndex(JSONObject object, String resultOrderKey, Integer isSearch, PatientsIdSqlMap patientsIdSqlMap, String crfId) throws ExecutionException, InterruptedException, IOException  {
        UqlClass uqlClass = null;
        String patientSql = TransPatientSql.getAllPatientSql(patientsIdSqlMap.getPatientSnIds(),crfId);
        JSONArray patientSetId = object.getJSONArray("patientSetId");
        String name = object.getString("name");
        String isVariant = object.getString("isVariant");
        String groupToId = object.getString("groupToId");
        String projectId = object.getString("projectId");
        if( UqlConfig.CORT_INDEX_ID.equals(groupToId)){
            isVariant = "1";
        }
        groupToId =StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId;
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

        String indexDateTmp = resultObj.getString(indexCol);
        String [] indexPath = indexDateTmp.split("\\.");
        String indexDate = indexPath[indexPath.length-2]+"."+indexPath[indexPath.length-1];

        String parts[] = indexColumn.split("\\.");
        String visits = parts[parts.length - 2];

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
        uqlClass.setVisitsGroup(visits);
        //处理 条件
        long tranStartTime = System.currentTimeMillis();
        transforEnumCondition(contitionObj, uqlClass, where, R_activeIndexId,groupToId,projectId,patientSetId, patientSql, crfId, patientsIdSqlMap.getId());
        LOG.info("transforEnum use time "+ (System.currentTimeMillis()-tranStartTime));
        UqlClass sqlresult = null;
        String sqlMd5 = "";
        redisMapDataService.delete(UqlConfig.CORT_INDEX_REDIS_KEY.concat(T_activeIndexId));
        List<Group> groupList = groupMapper.getGroupListByProjectId(projectId);
        for (Group group : groupList){
            String groupId = group.getGroupId();
            redisMapDataService.delete(UqlConfig.CORT_CONT_ACTIVE_REDIS_KEY.concat(T_activeIndexId+"_"+groupId));
        }
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
            String sqlNew = uqlClass.getHavingSql();
            /*开始搞 MD5 替换sql*/
            sqlMd5 = StringToMd5.stringToMd5(sqlNew);
            Integer sqlMd5count = activeSqlMapMapper.getCountByActiveAndsqlMd5(T_activeIndexId,sqlMd5,groupToId);
            if(sqlMd5count>0) return null;
            sqlresult = uqlClass;
            if(parts.length>2 && parts[0].equals("visits") && parts[1].equals("inspection_reports") && parts[2].equals("sub_inspection")){
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

            if(parts.length>2 && parts[0].equals("visits") && parts[1].equals("inspection_reports") && parts[2].equals("sub_inspection")){
                sqlresult.setWhere(sqlresult.getWhere()+" and join_field = 'sub_inspection' ");
            }else {
                sqlresult.setWhere(sqlresult.getWhere()+" and join_field = '"+visits+"'");
            }
        }
        sqlresult.setSqlHaving(functionParam);
        String newSql = sqlresult.getHavingSql();
        String resultDocId = searchDocIdBySql(newSql,projectId,crfId);
        ActiveSqlMap activeSqlMap = new ActiveSqlMap(projectId,T_activeIndexId,GzipUtil.compress(newSql),
                                                        sqlresult.getSelect(),sqlresult.getFrom(),uqlClass.getSourceFilter(),
                                                        uqlClass.getActiveId().toJSONString(),JSON.toJSONString(uqlClass.getSource()),selectValue,
                                                        indexTypeValue,name,hasCount.split(",")[1],
                                                        sqlMd5);
        activeSqlMap.setUncomSqlWhere(sqlresult.getWhere());
        activeSqlMap.setGroupId(StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId);
        activeSqlMap.setSqlHaving(uqlClass.getHaving());
        activeSqlMap.setPatSqlGroup(patientsIdSqlMap.getId());
        activeSqlMap.setResultDocId(resultDocId);
        if(StringUtils.isEmpty(groupToId) || UqlConfig.CORT_INDEX_ID.equals(groupToId)){
            SingleExecutorService.getInstance().getFlushCountGroupExecutor().submit(() -> {
                try {
                    saveCortrastiveResultRedisMap(activeSqlMap,projectId,"EMR",T_activeIndexId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        Long mysqlStartTime = System.currentTimeMillis();
        activeSqlMapMapper.insert(activeSqlMap);
        LOG.info("数据库用时 :  "+(System.currentTimeMillis()-mysqlStartTime));
        /*引用依赖计算*/
//        getReferenceActiveIndex(id,resultOrderKey,patientSetId,groupToId,groupFromId, crfId);
        return sqlresult.getSql();
    }

    private String searchDocIdBySql(String newSql, String projectId,String crfId) {
        String search = httpUtils.querySearch(projectId,newSql,1,Integer.MAX_VALUE-1,null,new JSONArray(),crfId,false);
        Set<String> patients = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(JSON.parseObject(search))
            .stream()
            .map(String.class::cast)
            .collect(toSet());
        return String.join(SeparatorContent.VERTIVAL_BAR,patients);
    }

    @Override
    public String searchByActive(JSONObject object, String resultOrderKey, Integer isSearch, PatientsIdSqlMap patientsIdSqlMap, String crfId) throws ExecutionException, InterruptedException, IOException {
        UqlClass uqlClass = null;
        String patientSql = TransPatientSql.getAllPatientSql(patientsIdSqlMap.getPatientSnIds(),crfId);
        String activeType = object.getString("activeType");
        JSONArray patientSetId = object.getJSONArray("patientSetId");
        String isVariant = object.getString("isVariant");
        String groupToId = object.getString("groupToId");
        groupToId =StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId;
        //获取初筛 sql
        String projectId = object.getString("projectId");

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
            visits = "sub_inspection";
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
        uqlClass.setVisitsGroup(visits);
        uqlClass.setActiveSelect(uqlClass.getSelect() + hasCount);

        JSONObject contitionObj = contitions.getJSONObject(0);
        transforEnumCondition(contitionObj, uqlClass, where, T_activeIndexId,groupToId,projectId,patientSetId,patientSql, crfId,patientsIdSqlMap.getId());

        UqlClass sqlresult = null;
        String sqlMd5 = "";
        String allWhere = "";
        String eventWhere = "";
        if(where.isSameGroup(visits)){
            uqlClass.setWhere(("sub_inspection".equals(visits)
                ? "inspection_reports"
                : visits )
                + ".VISIT_SN IS NOT NULL AND ");
            if (!"all".equals(operator) ) {
                uqlClass.setWhere(uqlClass.getWhere()+order1 + " IS NOT NULL AND ");
            }
            uqlClass.setInitialPatients(isVariant,patientSql);
            where.deleteHasChild();
            where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
            uqlClass.setWhere(uqlClass.getWhereNotNull() + "(" + where.toString() + " ) ");
            String sqlNew = uqlClass.getSql();
            /*开始搞 MD5 替换sql*/
            sqlMd5 = StringToMd5.stringToMd5(sqlNew);
            Integer sqlMd5count = activeSqlMapMapper.getCountByActiveAndsqlMd5(R_activeIndexId,sqlMd5,groupToId);
            if(sqlMd5count>0) return null;

            //构造新sql 只有 visitSn 搜错功能
            allWhere = uqlClass.getWhere();
            sqlresult = uqlClass;
            eventWhere = sqlresult.getWhere().contains("join_field") ? sqlresult.getWhere() : sqlresult.getWhere()+" and join_field='visit_info'";
            if(parts.length>2 && parts[0].equals("visits") && parts[1].equals("inspection_reports") && parts[2].equals("sub_inspection")){
                sqlresult.setWhere(sqlresult.getWhere()+" AND join_field = 'sub_inspection'");
            }else {
                sqlresult.setWhere(sqlresult.getWhere()+" AND join_field = '"+visits+"'");
            }

        }else {
            uqlClass.setActiveWhereIsEmpty("sub_inspection".equals(visits) ? "inspection_reports" : visits ,where);
            uqlClass.setNotAllWhere(operator,order1,null, null);
            uqlClass.setIndexWhereIsEmpty(order1,where);
            uqlClass.setInitialPatients(isVariant,patientSql);

            where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
            uqlClass.setWhere(uqlClass.getWhereNotNull() + "(" + where.toString() + " ) ");
            boolean isGetVisisn = !where.isEmpty();
            String sqlNew = uqlClass.getHavingSql();
            /*开始搞 MD5 替换sql*/
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
            eventWhere = sqlresult.getWhere().contains("join_field") ? sqlresult.getWhere() : sqlresult.getWhere()+" and join_field='visit_info'";

            String andGroupCondition = getActiveGroupCondition(where,activeResult );

            if(StringUtils.isNotEmpty(andGroupCondition)){
                sqlresult.setWhere(sqlresult.getWhere() + " and ("+andGroupCondition+")");
            }
            if(parts.length>2 && parts[0].equals("visits") && parts[1].equals("inspection_reports") && parts[2].equals("sub_inspection")){
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
        String resultDocId =  String.join(SeparatorContent.VERTIVAL_BAR,allPats);
        patients.removeAll(allPats);
        String otherResult = "";
        if (patients.isEmpty()) {
            otherResult = "";
        } else {
            otherResult = patients.stream().collect(joining("$"));
        }

        ActiveSqlMap activeSqlMap = new ActiveSqlMap(projectId,R_activeIndexId,GzipUtil.compress(newSql),
            sqlresult.getSelect(),sqlresult.getFrom(),uqlClass.getSourceFilter(),activeType,
            uqlClass.getActiveId().toJSONString(),JSON.toJSONString(uqlClass.getSource()),activeResultDocId,activeResult,
            sqlMd5,GzipUtil.compress(eventWhere),hasCount.split(",")[1],GzipUtil.compress(otherResult),hasCount.split(",")[1]);
        activeSqlMap.setGroupId(StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId);
        activeSqlMap.setUncomSqlWhere(sqlresult.getWhere());
        activeSqlMap.setPatSqlGroup(patientsIdSqlMap.getId());
        activeSqlMap.setResultDocId(resultDocId);
        Long mysqlStartTime = System.currentTimeMillis();
        activeSqlMapMapper.insert(activeSqlMap);
        LOG.info("数据库用时 :  "+(System.currentTimeMillis()-mysqlStartTime));
        /*引用依赖计算*/
//        getReferenceActiveIndex(id,resultOrderKey);
        return sqlresult.getSql();
    }

    @Override
    public AjaxObject searchClacIndexResultByUql(String activeId, String projectId, Integer pageSize, Integer pageNum, JSONArray basicColumns, String groupFromId, JSONArray patientSetId, String groupId, String isVariant, String crfId) throws IOException, ExecutionException, InterruptedException {
        AjaxObject ajaxObject = new AjaxObject();

        Long startMysqlTime = System.currentTimeMillis();
        String tmpAcId = activeId.split("_tmp")[0];
        List<ActiveIndexConfig> activeIndexConfigs = activeIndexConfigMapper.findAllByActiveIndexId(tmpAcId);
        String indexType = "";
        if(activeIndexConfigs.size()>0){
            indexType = activeIndexConfigs.get(0).getIndexTypeDesc();
        }
        List<ActiveSqlMap> sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        if(sqlList == null || sqlList.size() == 0 ){    //重复计算功能
            referenceCalculate(activeId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null,crfId);
            sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        }
        LOG.info("指标 从mysql数据库读取时间为： "+(System.currentTimeMillis()-startMysqlTime));

        if ("自定义枚举类型".equals(indexType)) {//枚举类型处理
            return searchClasEnumResultByUql(activeId, sqlList, projectId, pageSize, pageNum, basicColumns,groupFromId,patientSetId,groupId,isVariant,patientSetId, crfId);
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
        List<String> allResutList = sqlList.stream().map( x ->x.getResultDocId().split(SeparatorContent.getRegexVartivalBar())).flatMap(Arrays ::stream).distinct().collect(toList());
        List<String> resultList = PagingUtils.getPageContentForString(allResutList,pageNum,pageSize);
        String  sql = TransPatientSql.getPatientDocIdSql(resultList,crfId);
        Integer total = allResutList.size(); // 计算后的总数
        Set<String> allTmpSet = new HashSet<>();
        //蛋疼的计算  pageNum pageSize
        Integer before = (pageNum - 1 ) * pageSize +1;
        Integer last = pageNum * pageSize;
        JSONArray dataAll = new JSONArray();

        for (ActiveSqlMap sqlMap : sqlList){
            Set<String> tmpSet = Arrays.stream(sqlMap.getResultDocId().split(SeparatorContent.getRegexVartivalBar())).collect(toSet());
            tmpSet.removeAll(allTmpSet);
            allTmpSet.addAll(tmpSet);
            if(allTmpSet.size() + 1  < before){
                continue;
            }
            if(dataAll.size() >= pageSize){
                break;
            }
            int page = 1;
            int size = last - dataAll.size() + pageNum;

            String newSql = sqlMap.getHavingSqlJoinSql(sql);
            String sourceFilter = sqlMap.getSourceFiltere();
            JSONArray source = new JSONArray();
            source.add("patient_info");
            JSONArray sourceValue = JSONArray.parseArray(sqlMap.getSourceValue());
            int sourceSize = sourceValue == null ? 0 : sourceValue.size();
            for (int i = 0; i < sourceSize; i++) {
                String sourceVal = sourceValue.getString(i);
                source.add(sourceVal);
            }
            String result = httpUtils.querySearch(projectId, newSql, page, size, sourceFilter, source,false);
            //处理结果

            JSONArray data = UqlQureyResult.getResultData(result, activeId,refActiveIds);
            List<String> pasSn = new ArrayList<>();
            Map<String, JSONObject> dataMap = new HashMap<>();
            for (int i = 0; i < data.size(); i++) {
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
            Long tmie3 = System.currentTimeMillis();
            for (int i = 0; i < refSize; i++) {
                //拼接column
                String refActiveId = refActiveIds.getString(i);
                List<ActiveSqlMap> patSqlList = activeSqlMapMapper.getActiveSqlMapByProjectIdAndSqlGroup(projectId, refActiveId.substring(1),groupId,sqlMap.getPatSqlGroup());
                if(patSqlList == null || patSqlList.size() == 0 ){
                    referenceCalculate(refActiveId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null,crfId);
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
            dataAll.addAll(data);
            LOG.info("处理引用结果"+(System.currentTimeMillis()-tmie3));
        }

        Integer count = getSearchUqlAllCount(groupFromId,patientSetId,groupId,projectId);
        AjaxObject.getReallyDataValue(dataAll,basicColumns);
        ajaxObject.setCount(count);
        ajaxObject.setWebAPIResult(new WebAPIResult<Object>(pageNum, pageSize, total));
        ajaxObject.setColumns(basicColumns);
        ajaxObject.setData(dataAll);
        return ajaxObject;
    }

    private Integer getSearchUqlAllCount(String groupFromId, JSONArray patientSetId, String groupId,String projectId) {
        if(StringUtils.isEmpty(groupFromId) && (patientSetId ==null || patientSetId.size()==0) && StringUtils.isNotEmpty(groupId)){
            groupFromId = groupMapper.getGroupParentId(groupId);
            if(StringUtils.isEmpty(groupFromId)){
                List<String> patSetIds = groupPatDataMapper.getPatSetByGroupId(groupId);
                patientSetId = JSONArray.parseArray(JSON.toJSONString(patSetIds));
            }
        }
        //获取总共人数
        if(patientSetId !=null && patientSetId.size()>0){
            return getPatientSqlCount(patientSetId,projectId);
        }else {
            return getGroupSqlCount(groupFromId);
        }
    }

    private Integer getGroupSqlCount(String groupFromId) {
        return groupDataMapper.getPatSetAggregationCount(groupFromId);
    }

    private Integer getPatientSqlCount(JSONArray patientSetId, String projectId) {
        List<String> patientSets = patientSetId.toJavaList(String.class);
        return   patientSetService.getPatientSetLocalCountByListForPatientSets(patientSets);
    }

    @Override/*获取事件结果集*/
    public AjaxObject searchCalcResultByUql(String activeId, String projectId, JSONArray basicColumns, JSONArray visitColumns, Integer activeType, Integer pageNum,
                                            Integer pageSize, String activeResult, String groupFromId, JSONArray patientSetId, String groupId,String crfId) throws InterruptedException, IOException, ExecutionException {
        Long startMysqlTime = System.currentTimeMillis();
        List<ActiveSqlMap> sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        if(sqlList == null || sqlList.size() == 0 ){
            referenceCalculate(activeId,projectId,CommonContent.ACTIVE_TYPE_EVENT,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null,crfId);
            sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        }
        LOG.info("事件 从mysql数据库读取时间为： "+(System.currentTimeMillis()-startMysqlTime));
        activeResult = activeIndexConfigMapper.getActiveResult(activeId.replaceAll("_tmp", ""));

        List<String> allResutList = sqlList.stream().map( x ->x.getResultDocId().split(SeparatorContent.getRegexVartivalBar())).flatMap(Arrays ::stream).distinct().collect(toList());
        List<String> resultList = PagingUtils.getPageContentForString(allResutList,pageNum,pageSize);
        String  sql = TransPatientSql.getPatientDocIdSql(resultList,crfId);
        Integer total = allResutList.size(); // 计算后的总数
        Set<String> allTmpSet = new HashSet<>();
        //蛋疼的计算  pageNum pageSize
        Integer before = (pageNum - 1 ) * pageSize + 1;
        Integer last = pageNum * pageSize;
        JSONArray dataAll = new JSONArray();

        // --------------开始分批查找
        for (ActiveSqlMap sqlMap : sqlList){

            Set<String> tmpSet = Arrays.stream(sqlMap.getResultDocId().split(SeparatorContent.getRegexVartivalBar())).collect(toSet());
            tmpSet.removeAll(allTmpSet);
            allTmpSet.addAll(tmpSet);
            if(allTmpSet.size() + 1  < before){
                continue;
            }
            if(dataAll.size() >= pageSize){
                break;
            }
            int page = 1;
            int size = last - dataAll.size() + pageNum;

            String sourceFilter = sqlMap.getSourceFiltere();
            /*处理 检验子项 和检验报告命名方式*/
            String activeResultValue = sqlMap.getActiveResultValue();
            String parts[] = activeResultValue.split("\\.");
            String visits = parts[parts.length - 1];

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
            String allSql =  "select " +activeReuslt +"as condition ,count("+visits +".DOC_ID) as jocount from " + sqlMap.getSqlFrom() +" where "+ sqlMap.getUncomSqlWhere() + " AND " + sql +  " and "+visits+ ".DOC_ID is not null group by patient_info.DOC_ID " + having;
            String result = httpUtils.querySearch(projectId,allSql,page,size,sourceFilter,source,false);
            /*处理结果*/
            JSONArray data = UqlQureyResult.getActiveVisitSn(result, activeId);
            String query = getVisitSns(data);
            /*组装新的 uql搜索 搜索新的数据*/
            UqlClass uqlClass = new StandardUqlClass();
            uqlClass.setFrom(IndexContent.getIndexName(crfId,projectId));
            JSONArray array = new JSONArray();
            array = getSource(basicColumns, "patient_info", array);
            /*处理病案首页 手术问题*/
            String repeaceActive = activeResult.substring(activeResult.lastIndexOf(".") + 1, activeResult.length());
            array = getSource(visitColumns, repeaceActive, array);
            array.remove("patient_info.DATE_OF_BIRTH");
            int arraySize = array == null ? 0 : array.size();
            List<String> selectList = new LinkedList<>();
            for (int i = 0; i < arraySize; i++) {
                selectList.add(array.getString(i));
            }
            selectList.add("visit_info.VISIT_SN");
            selectList.add("visit_info.PATIENT_SN");
            if(!"sub_inspection".equals(visits)){
                selectList.add(visits+".PATIENT_SN");
            }
            if("inspection_reports".equals(visits)){
                selectList.add("inspection_reports.INSPECTION_SN");
            }
            uqlClass.setActiveSelect(String.join(",",selectList));
            uqlClass.setWhere(visits+".DOC_ID in (" + query + ")");
            /*查询docId*/
            JSONArray resultSource = new JSONArray();
            String resultJson = httpUtils.querySearch(projectId,uqlClass.getVisitsSql(),1,Integer.MAX_VALUE-1,null,resultSource,false);
            JSONArray dataObj = getActiveResultData(resultJson, basicColumns, visitColumns, repeaceActive,result,visits);
            dataAll.addAll(dataObj);
        }
        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
        Integer count = getSearchUqlAllCount(groupFromId,patientSetId,groupId,projectId);
        ajaxObject.setCount(count);
        ajaxObject.setData(dataAll);
        WebAPIResult webAPIResult = new WebAPIResult(pageNum, pageSize, total);
        ajaxObject.setWebAPIResult(webAPIResult);
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
    public AjaxObject searchCalcExculeByUql(String activeId, String projectId, Integer pageSize, Integer pageNum, JSONArray basicColumns,
                                            String isExport, String groupId, String groupName, JSONArray patientSetId, String createId,
                                            String createName,String groupFromId,boolean autoExport, String crfId) throws IOException, ExecutionException, InterruptedException {
        AjaxObject ajaxObject = new AjaxObject();
        Long startMysqlTime = System.currentTimeMillis();
        List<ActiveSqlMap> sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        if(sqlList == null || sqlList.size() == 0 ){
            referenceCalculate(activeId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null,crfId);
            sqlList = activeSqlMapMapper.getActiveSqlMapByProjectId(projectId, activeId,groupId);
        }
        LOG.info("那排 从mysql数据库读取时间为： "+(System.currentTimeMillis()-startMysqlTime));
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
        patientSetId =  getAllPatientSetId(groupFromId,patientSetId,groupId);

        JSONArray source = new JSONArray().fluentAdd("patient_info");
        List<String> allResutList = sqlList.stream().map( x ->x.getResultDocId().split(SeparatorContent.getRegexVartivalBar())).flatMap(Arrays ::stream).distinct().collect(toList());

        if("1".equals(isExport)){//处理导出数据
            //向dataSource添加数据
            patientSetService.saveGroupDataByGroupBlock(groupId,allResutList,1);
            for (ActiveSqlMap sqlMap : sqlList){
                //TODO 可以优化计算逻辑
                String result =  httpUtils.querySearch(projectId,sqlMap.getUncomActiveSql(),pageNum,Integer.MAX_VALUE-1,sqlMap.getSourceFiltere(),source,false);
                JSONArray data = UqlQureyResult.getResultData(result, activeId,refActiveIds,false);
                boolean flag = true;
                if(patientSetId == null){
                    flag = groupService.exportToGroup(data,groupId,groupName,projectId,createId,createName,true,autoExport);
                }else {
                    exportToParentGroup(patientSetId, data, groupId, groupName, projectId, createId, createName, autoExport, result, pageNum, sqlMap, activeId, refActiveIds, source, crfId);
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
        }

        List<String> resultList = PagingUtils.getPageContentForString(allResutList,pageNum,pageSize);
        String  joinSql = TransPatientSql.getPatientDocIdSql(resultList,crfId);
        Integer total = allResutList.size(); // 计算后的总数
        Set<String> allTmpSet = new HashSet<>();
        //蛋疼的计算  pageNum pageSize
        Integer before = (pageNum - 1 ) * pageSize +1;
        Integer last = pageNum * pageSize;
        JSONArray dataAll = new JSONArray();

        for (ActiveSqlMap sqlMap : sqlList){
            Set<String> tmpSet = Arrays.stream(sqlMap.getResultDocId().split(SeparatorContent.getRegexVartivalBar())).collect(toSet());
            tmpSet.removeAll(allTmpSet);
            allTmpSet.addAll(tmpSet);
            if(allTmpSet.size() + 1  < before){
                continue;
            }
            if(dataAll.size() >= pageSize){
                break;
            }
            int page = 1;
            int size = last - dataAll.size() + pageNum;
            String newSql = sqlMap.getSqlJoinSql(joinSql);
            String result =  httpUtils.querySearch(projectId,newSql,page,size,sqlMap.getSourceFiltere(),source,false);
            //处理结果
            JSONArray data = UqlQureyResult.getResultData(result, activeId,refActiveIds,false);
            List<String> pasSn = new ArrayList<>();
            Map<String, JSONObject> dataMap = new HashMap<>();
            for (int i = 0; i < data.size(); i++) {
                JSONObject tmpObj = data.getJSONObject(i);
                pasSn.add(tmpObj.getString("DOC_ID"));
                dataMap.put(tmpObj.getString("DOC_ID"), tmpObj);
            }
            String patSnWhere = IndexContent.getPatientDocId(crfId) + TransPatientSql.transForExtContain(pasSn);

            for (int i = 0; i < refSize; i++) {
                //拼接column
                String refActiveId = refActiveIds.getString(i);
                List<ActiveSqlMap> patSqlList = activeSqlMapMapper.getActiveSqlBySqlGroup( refActiveId,groupId,sqlMap.getPatSqlGroup());
                if(patSqlList == null || patSqlList.size() == 0 ){
                    referenceCalculate(refActiveId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null,crfId);
                    patSqlList =activeSqlMapMapper.getActiveSqlBySqlGroup(refActiveId,groupId,sqlMap.getPatSqlGroup());
                }
                ActiveSqlMap patActiveSqlMap = patSqlList.get(0);
                String indexResultValue = patActiveSqlMap.getIndexResultValue();
                if(StringUtils.isNotEmpty(indexResultValue)){//那排
                    makeEnumResultData(patSqlList,patSnWhere,dataMap,projectId,pageSize,source,refActiveId);
                }else if("1".equals(patActiveSqlMap.getActiveType())){//事件
                    makeActiveResultData(patActiveSqlMap,patSnWhere,dataMap,projectId,pageSize,source,refActiveId);
                }else {//指标
                    makeIndexResultData(patActiveSqlMap,patSnWhere,dataMap,projectId,pageSize,source,refActiveId);
                }
            }
            dataAll.addAll(data);
        }
        Integer count = getSearchUqlAllCount(groupFromId,patientSetId,groupId,projectId);
        AjaxObject.getReallyDataValue(dataAll,basicColumns);
        ajaxObject.setCount(count);
//        ajaxObject.setApplyOutCondition(applyCondition);
        ajaxObject.setWebAPIResult(new WebAPIResult<>(pageNum, pageSize, total));
        ajaxObject.setColumns(basicColumns);
        ajaxObject.setData(dataAll);

        return ajaxObject;

    }

    private JSONArray getAllPatientSetId(String groupFromId, JSONArray patientSetId, String groupId) {
        if(StringUtils.isEmpty(groupFromId) && (patientSetId ==null || patientSetId.size()==0) && StringUtils.isNotEmpty(groupId)){
            groupFromId = groupMapper.getGroupParentId(groupId);
            if(StringUtils.isEmpty(groupFromId)){
                List<String> patSetIds = groupPatDataMapper.getPatSetByGroupId(groupId);
                patientSetId = JSONArray.parseArray(JSON.toJSONString(patSetIds));
            }
        }
        return patientSetId;
    }


    private void exportToParentGroup(JSONArray patientSetId, JSONArray data, String groupId, String groupName, String projectId, String createId, String createName,
                                     boolean autoExport, String result, Integer pageNum, ActiveSqlMap sqlMap, String activeId, JSONArray refActiveIds, JSONArray source, String crfId) {
        int size = patientSetId.size();
        List<String> patientSetIds = patientSetId.toJavaList(String.class);
        List<String> patients = patientsSetMapper.getpatientSetNameByPatSetIds(patientSetIds);
        for (int i = 0; i < size; i++) {
            String id = patientSetId.getString(i);
            groupService.exportToGroupById(data,groupId,groupName,id,projectId,createId,createName,true,autoExport);
        }
        //增加 移除患者列表
        Set<String> docIds = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(JSON.parseObject(result))
            .stream()
            .map(String.class::cast)
            .collect(toSet());
        String sql = getPatientSqlForIds(patientSetId,projectId,docIds,crfId);
        String removeQuery = "select patient_info.DOC_ID as pSn from rws_emr_"+projectId+" where "+sql+" group by patient_info.DOC_ID";
        String re =  httpUtils.querySearch(projectId,removeQuery,pageNum,Integer.MAX_VALUE-1,sqlMap.getSourceFiltere(),source,false);
        JSONArray dataRe = UqlQureyResult.getResultData(re, activeId,refActiveIds,false);
        for (int i = 0; i < size; i++) {
            String id = patientSetId.getString(i);
            groupService.exportToRemoveGroup(dataRe,groupId,groupName,id,projectId,createId,createName,true,autoExport);
        }
        if(!autoExport){
            String content = createName + "将患者集： " + String.join(",",patients) + " 添加到组:" + groupName;
            logUtil.saveLog(projectId, content, createId, createName);
        }
    }

    private void makeIndexResultData(ActiveSqlMap patActiveSqlMap, String patSnWhere, Map<String, JSONObject> dataMap, String projectId, Integer pageSize, JSONArray source, String refActiveId) throws IOException {
        String sql = patActiveSqlMap.getSqlJoinSql(patSnWhere);
        String patSnResult = httpUtils.querySearch(projectId, sql, 1, pageSize, "", source,false);
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

    private void makeActiveResultData(ActiveSqlMap patActiveSqlMap, String patSnWhere, Map<String, JSONObject> dataMap, String projectId, Integer pageSize, JSONArray source, String refActiveId) throws IOException {
        String sql = patActiveSqlMap.getSqlJoinSql(patSnWhere);
        String patSnResult = httpUtils.querySearch(projectId, sql , 1, pageSize, "", source,false);
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
            String sql = activeSqlMap.getSqlJoinSql(patSnWhere);
            String patSnResult = httpUtils.querySearch(projectId, sql, 1, pageSize, "", source,false);
            JSONArray tmpHits = UqlQureyResult.getHitsArray(patSnResult);
            int tmpHitsSize = tmpHits.size();
            String indexResultValue = activeSqlMap.getIndexResultValue();
            for (int j = 0; j < tmpHitsSize; j++) {
                String patSn = tmpHits.getJSONObject(j).getString("_id");
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
        String patientSetSql = patientSetService.getPatientSetLocalSql(patientSetId);
        String  newpatientSetSql = TransPatientSql.getAllPatientSql(patientSetSql,crfId);
        String query = "select " + IndexContent.getPatientDocId(crfId) +" from "+ IndexContent.getIndexName(crfId,projectId) + " where " + newpatientSetSql +" and join_field = 'patient_info' ";
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
    public AjaxObject getAggregationAll(String patientSetId, JSONArray aggregationTeam, String projectId, String crfId) {
        String patSns = patientSetService.getPatientSetLocalSql(patientSetId);
        if(StringUtils.isEmpty(patSns)){
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"没有数据");
        }
        String  newpatientSetSql = TransPatientSql.getAllPatientSql(patSns,crfId);
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
        String result = httpUtils.querySearch(projectId,sql,1,1,null,source,crfId,false,termObj);
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
    public List<Patient> getpatentByUql(String patientSetId, boolean isExport, String projectId, String crfId) {
        List<Patient> patientList = new LinkedList<>();
        String patientSetSql = patientSetService.getPatientSetLocalSql(patientSetId);
        String  newpatientSetSql = TransPatientSql.getAllPatientSql(patientSetSql,crfId);
        String query = "select "+IndexContent.getPatientDocId(crfId)+"  from "+ IndexContent.getIndexName(crfId, projectId) + " where join_field = 'patient_info' and " + newpatientSetSql;
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
        String query = "select "+IndexContent.getPatientDocId(crfId)+" as patSn  from "+ IndexContent.getIndexName(crfId,projectId) + " where "+where + " and  join_field='patient_info'";
        JSONArray source = new JSONArray();
        source.add(IndexContent.getPatientInfo(crfId));
        JSONObject jsonData = JSONObject.parseObject(httpUtils.querySearch(projectId,query,0,Integer.MAX_VALUE-1,null,source,crfId));
        JSONArray hits = UqlQureyResult.getHitsArray(jsonData);
        return hits;
    }

    private String getGroupPatientSn(List<GroupData> groupDataList,String crfId) {
        return IndexContent.getPatientInfoPatientSn(crfId) + TransPatientSql.transForExtContainForGroupData(groupDataList);
    }

    @Override
    public String SearchByEnume(JSONObject obj, String resultOrderKey, Integer isSearch, PatientsIdSqlMap patientsIdSqlMap, String crfId) throws ExecutionException, InterruptedException, IOException {
        UqlClass uqlClass = null;
        String patientSql = TransPatientSql.getAllPatientSql(patientsIdSqlMap.getPatientSnIds(),crfId);
        String projectId = obj.getString("projectId");
        String isVariant = obj.getString("isVariant");
        JSONArray configs = obj.getJSONArray("config");
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

            //指标处理
            UqlWhere where = new UqlWhere();
            JSONObject contitionObj = contitions.getJSONObject(0);

            transforEnumCondition(contitionObj, uqlClass, where, T_activeIndexId,groupToId,projectId,patientSetId, patientSql, crfId,patientsIdSqlMap.getId());
            uqlClass.setWhereIsEmpty(where,null,isVariant,patientSql, null);

            String hasCount = " ,count(visit_info.DOC_ID) as jocount ";
            uqlClass.setActiveSelect(uqlClass.getSelect() + hasCount);

            where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
            uqlClass.setWhere(uqlClass.getWhereNotNull() + " ( " + where.toString()+" ) ");
            UqlClass sqlresult = null;
            if (StringUtils.isNotEmpty(indexColumn)) {
                sqlresult = getIndexSql(uqlClass, function, functionParam, indexColumn, indexType, indexDate, projectId,hasCount);
            } else {
                sqlresult = uqlClass;
            }
            String newSql = sqlresult.getSql();
            ActiveSqlMap activeSqlMap = new ActiveSqlMap(projectId,R_activeIndexId,GzipUtil.compress(newSql),sqlresult.getSelect(),
                                                            sqlresult.getFrom(),uqlClass.getSourceFilter(),uqlClass.getActiveId().toJSONString(),
                                                            JSON.toJSONString(uqlClass.getSource()),indexResultValue,indexTypeValue,
                                                            "patient_info.DOC_ID",name);
            activeSqlMap.setUncomSqlWhere(sqlresult.getWhere());
            activeSqlMap.setGroupId(StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId);
            activeSqlMaps.add(activeSqlMap);
            activeSqlMap.setPatSqlGroup(patientsIdSqlMap.getId());
            Long mysqlStartTime = System.currentTimeMillis();
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
        if(resMap.size()==0){
            resMap.put("rws_tmp","rws_tmp");
        }
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
        String result = httpUtils.querySearch(projectId,activeSqlMap.getUncomActiveSql(),1,Integer.MAX_VALUE-1,null,new JSONArray(),crfId);
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
        if(map.size()==0){
            map.put("rws_tmp","rws_tmp");
        }
        String res= redisMapDataService.hmset(UqlConfig.CORT_INDEX_REDIS_KEY.concat(activeIndexId),map);
        redisMapDataService.setOutTime(UqlConfig.CORT_INDEX_REDIS_KEY.concat(activeIndexId),7 * 24 * 60 * 60);
        LOG.info(activeIndexId +" 插入 ---- redis" + res);
        return map;
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
                                       String groupId, String projectId, JSONArray patientSetId, String patientSql,
                                       String crfId, Integer patSqlGroup) throws IOException, ExecutionException, InterruptedException {
        String operatorSign = contitionObj.getString("operatorSign");
        JSONArray details = contitionObj.getJSONArray("details");
        JSONArray inner = contitionObj.getJSONArray("inner");
        JSONObject sortObj = sortForDetailAndInner(details,inner);
        details = sortObj.getJSONArray("detail");
        inner = sortObj.getJSONArray("inner");
        transforEnumDetails(details, uqlClass, operatorSign, where, activeIndexId, groupId, projectId, patientSetId, patientSql, crfId, patSqlGroup);
        int innerSize = inner == null ? 0 : inner.size();
        for (int i = 0; i < innerSize; i++) {
            if(where.needsOperator()){
                where.addElem(new LiteralUqlWhereElem(operatorSign));
            }
            where.addElem(new LiteralUqlWhereElem("("));
            JSONObject tmpObj = inner.getJSONObject(i);
            transforEnumCondition(tmpObj, uqlClass, where, activeIndexId,groupId,projectId,patientSetId, patientSql, crfId,1);
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
                                     String activeIndexId, String groupId, String projectId, JSONArray patientSetId,
                                     String patientSql, String crfId, Integer patSqlGroup) throws IOException, ExecutionException, InterruptedException {
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
            transforEnumDetailModel(detailObj, detailOperatorSign, elems, uqlClass, activeIndexId, groupId, projectId, patientSetId, patientSql, crfId, patSqlGroup);
            if (strongRefSize > 0) {
                elems.add(new LiteralUqlWhereElem("AND"));
//                where.addElem(new LiteralUqlWhereElem("("));
                transforEnumStrongRef(strongRef, strongRefSize, uqlClass, elems, activeIndexId, groupId, projectId, patientSetId, patientSql, crfId, patSqlGroup);
                elems.add(new LiteralUqlWhereElem(")"));
            }
            UqlWhere tmpWhere = new UqlWhere();
            boolean inspectionTransition = false;
            for (UqlWhereElem elem : elems) {
                if(elem instanceof SimpleConditionUqlWhereElem ){
                    String sourceTagName = ((SimpleConditionUqlWhereElem) elem).getSourceTagName();
                    String parts[] = sourceTagName.split("\\.");
                    String conditionGroup = parts[parts.length - 2];
                    String visitsGroup = uqlClass.getVisitsGroup();
                    if("inspection_reports".equals(visitsGroup) && "sub_inspection".equals(conditionGroup) ){
                        inspectionTransition = true;
                    }
                }
                tmpWhere.addElem(elem);
            }
            if(inspectionTransition){
                List<UqlWhereElem> tmpElems = new ArrayList<>();
                tmpWhere.execute();
                tmpElems.add(new InspectionConditionUqlWhereElem(tmpWhere.toString(), detailOperatorSign, projectId,"EMR",patientSql));
                elemLists.add(tmpElems);
            }else {
                elemLists.add(elems);
            }
        }
        elemLists.stream()
            .map(list -> makePair(
                topGroup((
                    list.get(0) instanceof  ReferenceConditionUqlWhereElem ?
                        ((ReferenceConditionUqlWhereElem)list.get(0)).group() :
                        list.get(0) instanceof SimpleConditionUqlWhereElem ?
                            list.get(0) :
                            list.get(0) instanceof InspectionConditionUqlWhereElem ?
                                list.get(0):
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
                                       String activeIndexId, String groupId, String projectId,JSONArray patientSetId,
                                       String patientSql, String crfId, Integer patSqlGroup) throws IOException, ExecutionException, InterruptedException {
        for (int i = 0; i < strongRefSize; i++) {
            if(i>0) elems.add(new LiteralUqlWhereElem("AND"));
            JSONObject tmpObj = strongRef.getJSONObject(i);
            transforEnumDetailModel(tmpObj, "and", elems, uqlClass, activeIndexId,groupId,projectId,patientSetId, patientSql, crfId, patSqlGroup);
        }
    }

    private void transforEnumDetailModel(JSONObject detailObj, String detailOperatorSign, List<UqlWhereElem> elems, UqlClass uqlClass,
                                         String activeIndexId, String groupId, String projectId, JSONArray patientSetId,
                                         String patientSql,String crfId,Integer patSqlGroup) throws IOException, ExecutionException, InterruptedException {
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
            List<ActiveSqlMap> activeSqlMaps = activeSqlMapMapper.getActiveSqlBySqlGroup(refActiveId,groupId,patSqlGroup);
            if(activeSqlMaps ==null || activeSqlMaps.size()==0 ){
                referenceCalculate(refActiveId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null,crfId);
                activeSqlMaps = activeSqlMapMapper.getActiveSqlBySqlGroup(refActiveId,groupId,patSqlGroup);
            }
            ActiveSqlMap activeSqlMap = activeSqlMaps.get(0);
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
    public String SearchByExclude(JSONObject object, String resultOrderKey, Integer isSearch, PatientsIdSqlMap patientsIdSqlMap, String crfId) throws ExecutionException, InterruptedException, IOException {
        String patientSql = TransPatientSql.getAllPatientSql(patientsIdSqlMap.getPatientSnIds(),crfId);
        String projectId = object.getString("projectId").replaceAll("-", "");
        JSONArray patientSetId = object.getJSONArray("patientSetId");
        String isVariant = object.getString("isVariant");
        String groupToId = object.getString("groupToId");
        groupToId =StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId;
        String activeIndexId = object.getString("id");
        JSONArray config = object.getJSONArray("config");

        UqlWhere where = new UqlWhere();
        UqlClass uqlClass = new ExcludeUqlClass(projectId);
        transforConditionForConfig(config, uqlClass, where,groupToId,projectId,patientSetId, crfId,patientsIdSqlMap.getId());
        uqlClass.setInitialPatients(isVariant,patientSql);
        activeIndexId = isSearch == CommonContent.ACTIVE_TYPE_TEMP ? activeIndexId.concat("_tmp") : activeIndexId;
        String resultDocId = searchDocIdBySql(uqlClass.getSql(),projectId,crfId);
        ActiveSqlMap activeSqlMap = new ActiveSqlMap(projectId,activeIndexId,GzipUtil.compress(uqlClass.getSql()),
                                                        uqlClass.getSelect(), uqlClass.getFrom(),
                                                        uqlClass.getActiveId().toJSONString(),JSON.toJSONString(uqlClass.getSource()));
        activeSqlMap.setUncomSqlWhere(uqlClass.getWhere());
        activeSqlMap.setPatSqlGroup(patientsIdSqlMap.getId());
        activeSqlMap.setGroupId(StringUtils.isEmpty(groupToId)? UqlConfig.CORT_INDEX_ID : groupToId);
        activeSqlMap.setResultDocId(resultDocId);
        activeSqlMapMapper.insert(activeSqlMap);
        return uqlClass.getSql();
    }

    private String getGroupSql(String groupId,String crfId) {
        List<String> groupDataPatSn = groupDataMapper.getPatientDocId(groupId);
        return  " "+IndexContent.getPatientDocId(crfId)+" " + TransPatientSql.transForExtContain(groupDataPatSn);
    }

    private String getPatientSql(String patientSetId,String projectId,String crfId){
        String patientSetSql = TransPatientSql.getUncomPatientSnSql(patientsSetMapper.getPatientsetSql(patientSetId));
        if(StringUtils.isEmpty(patientSetSql)){
            return null;
        }
        String  newpatientSetSql = TransPatientSql.getAllPatientSql(patientSetSql,crfId);
        JSONArray sourceFilter = new JSONArray();
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

    private String getPatientSqlForIds(JSONArray patientSetId, String projectId, Set<String> docIds, String crfId) {
        List<String> patientSets = patientSetId.toJavaList(String.class);
        List<String> dataList = patientSetService.getPatientSetLocalSqlByListForPatientSets(patientSets);
        String query = TransPatientSql.getAllPatientSqlForList(dataList,crfId);
        JSONArray sourceFilter = new JSONArray();
        String result = null;
        String newSql = "select  "+IndexContent.getPatientDocId(crfId)+" as pSn from "+ IndexContent.getIndexName(crfId,projectId)+" where " + query + " and  join_field='patient_info'";
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

    private String getPatientSql(JSONArray patientSetId,String projectId,String crfId) {
        List<String> patientSets = patientSetId.toJavaList(String.class);
        List<String> patientSetSql = patientsSetMapper.getPatientsetSqlAll(patientSets);
        String query = String.join(" or ",patientSetSql.stream().map( x -> "("+TransPatientSql.getAllPatientSql(TransPatientSql.getUncomPatientSnSql(x),crfId)+")").collect(toList()));
        JSONArray sourceFilter = new JSONArray();
        String result = null;
        String newSql = "select  "+ IndexContent.getPatientDocId(crfId)+" as pSn from "+ IndexContent.getIndexName(crfId, projectId)+" where "+query+IndexContent.getGroupBy(crfId);
        String response = httpUtils.querySearch(projectId,newSql,1,Integer.MAX_VALUE-1,null,sourceFilter,crfId,true);
        Set<String> patients = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(JSON.parseObject(response))
            .stream()
            .map(String.class::cast)
            .collect(toSet());
        result = IndexContent.getPatientDocId(crfId)+TransPatientSql.transForExtContain(patients);

        return result;
    }

    private  List<PatientsIdSqlMap> getPatientSqlTmp(JSONArray patientSetId,String projectId,String crfId) {
        List<String> patientSets = patientSetId.toJavaList(String.class);
        return  patientSetService.getPatientSetByListForInitialSql(patientSets);
    }

    private  List<PatientsIdSqlMap> getGroupSqlTmp(String patientSetId) {
        List<String> dataSourceIds = new LinkedList<>();
        dataSourceIds.add(patientSetId);
        return  patientSetService.getPatientSetByListForInitialSql(dataSourceIds);
    }
    private List<String> getPatientSqlList(JSONArray patientSetId) {
        List<String> patientSets = patientSetId.toJavaList(String.class);
        return  patientSetService.getPatientSetLocalSqlByListForPatientSets(patientSets);
    }

    private void transforConditionForConfig(JSONArray config, UqlClass uqlClass, UqlWhere where, String groupId, String projectId, JSONArray patientSetId, String crfId, Integer sqlGroupId) throws InterruptedException, ExecutionException {
        JSONObject incs = config.stream().map(JSONObject.class::cast).filter(o -> "纳入标准".equals(o.getString("activeResult"))).findAny().get();
        JSONObject excs = config.stream().map(JSONObject.class::cast).filter(o -> "排除标准".equals(o.getString("activeResult"))).findAny().get();
        transforConditionForConditions(incs.getJSONArray("conditions"), uqlClass, where, false, true, true ,false,groupId,projectId,patientSetId, crfId, sqlGroupId);
        transforConditionForConditions(excs.getJSONArray("conditions"), uqlClass, where, true, where.isEmpty(), true ,true,groupId,projectId,patientSetId, crfId, sqlGroupId);
        where.addElem(new LiteralUqlWhereElem("AND join_field='visit_info'"));
        where.execute(SingleExecutorService.getInstance().getSearchUqlExecutorService());
        uqlClass.setWhere(where.toString());
        uqlClass.setSelect("patient_info.DOC_ID");
    }

    private void transforConditionForConditions(JSONArray conditions, UqlClass uqlClass, UqlWhere where, boolean not, boolean first,
                                                boolean op, boolean isNot, String groupId, String projectId, JSONArray patientSetId, String crfId, Integer sqlGroupId) {
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
                transforConditionForConditions(condition.getJSONArray("inner"), uqlClass, where, false, true, subOp ,isNot,groupId,projectId,patientSetId, crfId, sqlGroupId);
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
                    List<ActiveSqlMap> activeSql = activeSqlMapMapper.getActiveSqlBySqlGroup(refId, groupId,sqlGroupId);
                    if(activeSql ==null || activeSql.size()==0 ){
                        try {
                            referenceCalculate(refId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null, crfId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        activeSql = activeSqlMapMapper.getActiveSqlBySqlGroup(refId,groupId,sqlGroupId);
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
    public void RunReferenceCalculate(String T_activeIndexId, String projectId, String crfId) {
        SingleExecutorService.getInstance().getReferenceActiveExecutor().submit(() -> {
            try {
                referenceCalculate(T_activeIndexId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),null,UqlConfig.CORT_INDEX_ID,null, crfId);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
            if(size > 1 && i==0 ) resultBuffer.append("(");
            if (i > 0) resultBuffer.append(" OR ");
            resultBuffer.append(sourceTagName);
            resultBuffer.append(condition);
            String val = values.getString(i);
            val = "否".equals(val) ? "false" : val;
            val = "是".equals(val) ? "true" : val;
            resultBuffer.append(val);
        }
        if(size>1){
            resultBuffer.append(")");
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
        if(visits.startsWith("visits.test.blood_routine")){
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
                                                 JSONArray basicColumns, String groupFromId, JSONArray patientSetIds, String groupId, String isVariant,
                                                 JSONArray patientSetId, String crfId) throws IOException, ExecutionException, InterruptedException {
        Set<String> refList = sqlList.stream()
            .map(sqlMap -> sqlMap.getRefActiveIds())
            .flatMap( array -> JSONArray.parseArray(array)
                .stream()
                .map(String.class ::cast))
            .collect(toSet());

        for (String refId : refList){
            ActiveIndex activeIndex = activeIndexMapper.selectByPrimaryKey(refId.substring(1));
            if(activeIndex == null ) continue;
            String name = activeIndex.getName();
            basicColumns.add(new JSONObject().fluentPut("name",name).fluentPut("id",refId));
        }
        List<String> patientSqlList =  getInitialSQLList(groupFromId,isVariant,groupId,patientSetId,projectId, crfId);
        Integer total = patientSqlList.size();
        List<String> pageList = PagingUtils.getPageContentForString(patientSqlList,pageNum,pageSize);

        Map<Integer, List<ActiveSqlMap>> groupMap = sqlList.stream().collect(groupingBy(ActiveSqlMap :: getPatSqlGroup,TreeMap::new,toList()));
        Iterator<Integer> iterator = groupMap.keySet().iterator();
        JSONArray dataAll  = new JSONArray();
        while (iterator.hasNext()){
            Integer mapKey = iterator.next();
            List<ActiveSqlMap> value = groupMap.get(mapKey);
            if(dataAll.size() >= pageSize){
                break;
            }
            Integer page = 1;
            Set<String> patientSetLocalSqlLists = patientSetService.getPatientSetLocalSqlListById(mapKey);
            String allSql = UqlConfig.getEnumSql(patientSetLocalSqlLists,projectId,crfId,pageList);
            String result = httpUtils.querySearch(projectId,allSql,page,pageSize,null, new JSONArray().fluentAdd("patient_info"),false);
            JSONArray data  = transforEnumResult(JSON.parseObject(result), value, projectId, activeId,pageSize);
            List<String> pasSn = new ArrayList<>();
            int daTasize = data == null ? 0 : data.size();
            Map<String, JSONObject> dataMap = new HashMap<>();
            for (int i = 0; i < daTasize; i++) {
                JSONObject tmpObj = data.getJSONObject(i);
                pasSn.add(tmpObj.getString("DOC_ID"));
                dataMap.put(tmpObj.getString("DOC_ID"), tmpObj);
            }
            String patSnWhere = "";
            patSnWhere = IndexContent.getPatientDocId(crfId) +TransPatientSql.transForExtContain(pasSn);
            JSONArray source = new JSONArray();
            source.add(IndexContent.getPatientDocId(crfId));

            for (String refActiveId :refList) {
                //拼接column
                List<ActiveSqlMap> patSqlList = activeSqlMapMapper.getActiveSqlMapByProjectIdAndSqlGroup(projectId, refActiveId.substring(1),groupId,mapKey);
                if(patSqlList == null || patSqlList.size() == 0 ){
                    referenceCalculate(refActiveId,projectId,CommonContent.ACTIVE_TYPE_INDEX,UqlConfig.RESULT_ORDER_KEY.get("EMR"),patientSetId,groupId,null, crfId);
                    patSqlList =activeSqlMapMapper.getActiveSqlMapByProjectIdAndSqlGroup(projectId, refActiveId,groupId,mapKey);
                }
                if(patSqlList.size() == 0 ) continue;
                ActiveSqlMap patActiveSqlMap = patSqlList.get(0);
                String patSql = patActiveSqlMap.getSqlJoinSql(patSnWhere);
                String patSnResult = httpUtils.querySearch(projectId, patSql, 1, pageSize, "", source,false);
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
            dataAll.addAll(data);
        }

        getSearchUqlAllCount(groupFromId,patientSetId,groupId,projectId);
        Integer count = getSearchUqlAllCount(groupFromId,patientSetId,groupId,projectId);
        AjaxObject.getReallyDataValue(dataAll,basicColumns);
        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
        ajaxObject.setData(dataAll);
        ajaxObject.setCount(count);
        ajaxObject.setColumns(basicColumns);
        WebAPIResult webAPIResult = new WebAPIResult(pageNum, pageSize, total);
        ajaxObject.setWebAPIResult(webAPIResult);
        return ajaxObject;
    }

    private JSONArray  transforEnumResult(JSONObject data, List<ActiveSqlMap> sqlList, String projectId, String activeId,Integer size) throws IOException {

        Set<String> pats = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(data)
            .stream()
            .map(String.class::cast)
            .collect(toSet());
        String patsStr ="";
        patsStr = TransPatientSql.transForExtContain(pats);

        Map<String, Set<String>> resultMap = new ConcurrentHashMap<>();

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

    public void getReferenceActiveIndex(String activeId,String resultOrderKey,JSONArray patientsSetId,String groupToId,String groupFromId, String crfId){
        int isTmp = 0;
        List<ActiveIndex> activeIndices = activeIndexMapper.findReferenceActiveIndex(activeId,isTmp);

        for (ActiveIndex activeIndex : activeIndices){
            String projectId = activeIndex.getProjectId();
            String activeIdTmp = activeIndex.getId();
            Integer activeType = activeIndex.getActiveType();
            SingleExecutorService.getInstance().getReferenceActiveExecutor().submit(() -> {
                try {
                    if("1".equals(activeIndex.getIsVariant())){
                        referenceCalculate(activeIdTmp,projectId,activeType,resultOrderKey, null, UqlConfig.CORT_INDEX_ID, null, crfId);
                    }else {
                        referenceCalculate(activeIdTmp,projectId,activeType,resultOrderKey, patientsSetId, groupToId, groupFromId, crfId);
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

    @Override
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
        String isVariant = active.getIsVariant();
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
        //TODO 循环做题
        List<PatientsIdSqlMap> patientSql = getInitialSQLTmp(groupFromId,isVariant,groupToId,patientsSetId,projectId,crfId);
        for (PatientsIdSqlMap p : patientSql){
            if (3 == activeType) {//那排
                this.SearchByExclude(obj, resultOrderKey,isSearch, p, crfId);
            } else if ("自定义枚举类型".equals(indexTypeDesc)) {//处理枚举
                this.SearchByEnume(obj, resultOrderKey,isSearch, p, crfId);
            } else if(2 == activeType) {//指标
                this.SearchByIndex(obj, resultOrderKey,isSearch, p, crfId);
            }else  if(1 == activeType){ //事件
                this.searchByActive(obj, resultOrderKey,isSearch, p, crfId);
            }
        }

    }

    @Override
    public AjaxObject getPatientSnsByAll(String patientSetId, String projectId, JSONArray showColumns, JSONArray actives, Integer pageNum, Integer pageSize, Integer type, String crfId){
        List<String> sqlList = patientSetService.getPatientSetLocalSqlByList(patientSetId);
        if(sqlList == null  || sqlList.size() == 0){
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"没有数据");
        }
        return getPatientListData(sqlList,pageNum,pageSize,crfId,projectId,showColumns);
    }
    @Override
    public AjaxObject getPatientListByAllByPatientSetIds(JSONArray patientSetIdTmp, String projectId, JSONArray showColumns, JSONArray actives, Integer pageNum, Integer pageSize, int i, String crfId) {
        List<String> patientSets = patientSetIdTmp.toJavaList(String.class);
        List<String> sqlList = patientSetService.getPatientSetLocalSqlByListForPatientSets(patientSets);
        return getPatientListData(sqlList,pageNum,pageSize,crfId,projectId,showColumns);
    }

    public AjaxObject getPatientListData( List<String> sqlList,Integer pageNum,Integer pageSize,String crfId,String projectId,JSONArray showColumns){
        Integer total = sqlList.size();
        List<String> pageList = PagingUtils.getPageContentForString(sqlList,pageNum,pageSize);
        String newpatientSetSql = TransPatientSql.getAllPatientSqlForList(pageList,crfId);
        String newSql = "select  "+IndexContent.getPatientDocId(crfId)+" as pSn from "+ IndexContent.getIndexName(crfId,projectId)+" where "+newpatientSetSql + " and  join_field='patient_info'";
        JSONArray source = new JSONArray();
        source.add("patient_info");
        JSONObject jsonData = JSONObject.parseObject(httpUtils.querySearch(projectId,newSql,1,pageSize,null,source,crfId));
        JSONArray data = UqlQureyResult.getQueryData(jsonData,crfId);
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
    @Override
    public String getInitialSQL(String groupFromId,String isVariant,String groupToId,JSONArray patientSetId,String projectId,String crfId){
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
                patientSql = getGroupSql(groupFromId,crfId);
            }
        }
        return patientSql;
    }

    private List<String> getInitialSQLList(String groupFromId, String isVariant, String groupToId, JSONArray patientSetId, String projectId, String crfId) {
        List<String> patientSqlList= new ArrayList<>();
        if(StringUtils.isEmpty(groupFromId) && (patientSetId ==null || patientSetId.size()==0) && StringUtils.isNotEmpty(groupToId)){
            groupFromId = groupMapper.getGroupParentId(groupToId);
            if(StringUtils.isEmpty(groupFromId)){
                List<String> patSetIds = groupPatDataMapper.getPatSetByGroupId(groupToId);
                patientSetId = JSONArray.parseArray(JSON.toJSONString(patSetIds));
            }
        }
        if(!"1".equals(isVariant)){
            if(patientSetId !=null && patientSetId.size()>0){
                patientSqlList = getPatientSqlList(patientSetId);
            }else{
//                patientSqlList = getGroupSql(groupFromId,crfId);
            }
        }
        return patientSqlList;
    }

    @Override
    public  List<PatientsIdSqlMap> getInitialSQLTmp(String groupFromId, String isVariant, String groupToId, JSONArray patientSetId, String projectId, String crfId){
        List<PatientsIdSqlMap> patientSql = null;
        if(StringUtils.isEmpty(groupFromId) && (patientSetId ==null || patientSetId.size()==0) && StringUtils.isNotEmpty(groupToId)){
            groupFromId = groupMapper.getGroupParentId(groupToId);
            if(StringUtils.isEmpty(groupFromId)){
                List<String> patSetIds = groupPatDataMapper.getPatSetByGroupId(groupToId);
                patientSetId = JSONArray.parseArray(JSON.toJSONString(patSetIds));
            }
        }
        if(!"1".equals(isVariant)){
            if(patientSetId !=null && patientSetId.size()>0){
                patientSql = getPatientSqlTmp(patientSetId,projectId,crfId);
            }else{
                patientSql = getGroupSqlTmp(groupFromId);
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
            }else if (elem instanceof SimpleConditionUqlWhereElem || elem instanceof ReferenceConditionUqlWhereElem || elem instanceof InspectionConditionUqlWhereElem ) {
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
            }else if (elem instanceof SimpleConditionUqlWhereElem || elem instanceof ReferenceConditionUqlWhereElem || elem instanceof  InspectionConditionUqlWhereElem ) {
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
