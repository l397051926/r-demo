/**
 * copyRight
 */
package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.darren.collection.keypath.KeyPath;
import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.content.UqlConfig;
import com.gennlife.rws.dao.ActiveIndexTaskMapper;
import com.gennlife.rws.dao.InputTaskMapper;
import com.gennlife.rws.dao.PatientsSetMapper;
import com.gennlife.rws.entity.ActiveIndexTask;
import com.gennlife.rws.query.BuildIndexRws;
import com.gennlife.rws.query.UqlQureyResult;
import com.gennlife.rws.service.DownLoadService;
import com.gennlife.rws.service.PreLiminaryService;
import com.gennlife.rws.util.AjaxObject;
import com.gennlife.rws.util.HttpUtils;
import com.gennlife.rws.util.StringUtils;
import com.gennlife.rws.util.TransPatientSql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toSet;

/**
 * Created by liuzhen.
 * Date: 2017/10/23
 * Time: 14:44
 */
@Service
public class DownLoadServiceImpl implements DownLoadService {
    private static Logger LOG = LoggerFactory.getLogger(DownLoadServiceImpl.class);
    @Autowired
    private HttpUtils httpUtils;
    @Autowired
    private ActiveIndexTaskMapper taskMapper;
    @Autowired
    private PatientsSetMapper patientsSetMapper;
    @Autowired
    private InputTaskMapper inputTaskMapper;
    @Autowired
    private PreLiminaryService preLiminaryService;
    @Value("${pre.liminary.maxMember}")
    private Integer maxMember;

    @Override//type = 1 首页面 展示数据
    public AjaxObject findTotalByActiveIdAndProjectId(String projectId, String activeId, Integer type) {
        Map<String,Object> param = new ConcurrentHashMap<>();
        param.put("status",1);
        if(StringUtils.isNotEmpty(activeId)){
            param.put("activeIndexId",activeId);
        }
        param.put("projectId",projectId);
        List<ActiveIndexTask> tasks = taskMapper.findByParam(param);
        ActiveIndexTask task = tasks != null&&!tasks.isEmpty() ? tasks.get(0) : null;
//        DBCollection collection = mongoTemplate.getCollection(projectId);
        JSONObject object = new JSONObject();
//        Integer count = patientsSetMapper.getPatientSetCount(projectId);
        if (type == 1) {
            String sql = UqlConfig.getAllProjectSql(projectId);
            JSONArray source = new JSONArray();
            String result = httpUtils.querySearch(projectId,sql,1,1,null,source,false);
            Integer count = UqlQureyResult.getTotal(result);

        }
        Integer   count =getProjectCount(projectId) ;

        long filterCount = count == null ? 0 : count;
        object.put("total",filterCount);
        if(StringUtils.isEmpty(activeId)){
            tasks = taskMapper.findByParam(param);
            task = tasks != null&&!tasks.isEmpty() ? tasks.get(0) : null;
            filterCount = (task==null||task.getId()==null)? count:task.getMarketApply() ==null ?0: task.getMarketApply();
            object.put("applyTotal",filterCount);
        }else if(task == null){
            object.put("applyTotal","计算中........");
        }else{
            filterCount = task.getSearchResult();
            object.put("applyTotal",filterCount);
            taskMapper.deleteByPrimaryKey(task.getId());
        }
        return new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,AjaxObject.AJAX_MESSAGE_SUCCESS,object);
    }


    @Override
    public String buildIndex(JSONObject esJson, String projectId, String crfId, String createId) {
        esJson.remove("source");
        LOG.debug("查询条件{}",esJson.toJSONString());
        BuildIndexRws buildIndexRws =  StringUtils.isNotEmpty(crfId) && !"EMR".equals(crfId)
                            ?
                            new BuildIndexRws(projectId,esJson.getString("indexName"),esJson.getString("query"),crfId, esJson.getJSONObject("power"))
                            :
                            new BuildIndexRws(projectId,esJson.getString("indexName"),esJson.getString("query"), esJson.getJSONObject("power"));
        buildIndexRws.setAction(0);
        buildIndexRws.setUid(createId);
        String result = httpUtils.buildIndexRws(buildIndexRws);
        return JSONObject.parseObject(result).getString("buildIndexID");
    }

    @Override
    public AjaxObject sysBuildIndex(DownLoadService downLoadService, String patientSetId, JSONObject esJSon, String crfId, String createId, String createName, String patientName, String projectId, String uqlQuery, String projectName, String crfName) throws IOException {
        long start = System.currentTimeMillis();
        String whereQuery = "";
        String sqlQuery = TransPatientSql.getUncomPatientSnSql(patientsSetMapper.getPatientsetSql(patientSetId));
        String where = StringUtils.isEmpty(sqlQuery)? "" : sqlQuery ;
        String esServiceUrl = httpUtils.getEsExceport();
        JSONArray array = new JSONArray();
        array.add(IndexContent.getPatientInfoPatientSn(crfId));
        esJSon.put("source",array);
        esJSon.put("size",1);
        String value1 = httpUtils.httpPost( JSON.toJSONString(esJSon),esServiceUrl);
        try {
            JSONObject object = JSONObject.parseObject(value1);
            if (!object.containsKey("hits")){
                LOG.error("导出条件错误 参数："+esJSon + " 结果为: "+ value1);
                return new AjaxObject(AjaxObject.AJAX_STATUS_TIPS,AjaxObject.AJAX_MESSAGE_FAILURE) ;
            }
        }catch (Exception e ){
            LOG.error("导出条件错误 参数："+esJSon + " 结果为: "+ value1);
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,AjaxObject.AJAX_MESSAGE_FAILURE) ;
        }
        Integer count = UqlQureyResult.getTotal(value1);//本次导出人数
        if( count >maxMember){
            return new AjaxObject(AjaxObject.AJAX_STATUS_TIPS,"导出数据超过 "+ maxMember + "人， 无法导出数据") ;
        }
        esJSon.put("size",Integer.MAX_VALUE-1);
        String value = httpUtils.httpPost( JSON.toJSONString(esJSon),esServiceUrl);
        //曾经的人数
        Integer allCount = patientsSetMapper.getSumCount(projectId) == null ? 0 :  patientsSetMapper.getSumCount(projectId);
        Integer runTaskSumCount = inputTaskMapper.getRunTaskSumCountByProjcetId(projectId);
        runTaskSumCount = runTaskSumCount == null ? 0 : runTaskSumCount;
        if(allCount +runTaskSumCount + count >maxMember){
            return new AjaxObject(AjaxObject.AJAX_STATUS_TIPS,"导出数据超过 "+ maxMember + "人， 无法导出数据") ;
        }
        Set<String> allPats = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(JSON.parseObject(value))
            .stream()
            .map(String.class::cast)
            .collect(toSet());
        whereQuery = String.join("|",allPats);
        if(StringUtils.isNotEmpty(where)){
            whereQuery= where+"|"+whereQuery;
        }
//        patientsSetMapper.updatePatientsCountAndQuery(patientSetId,0, GzipUtil.compress(whereQuery));
        String esJsonString = esJSon.toJSONString();
        String buildIndex = buildIndex(esJSon, projectId, crfId, createId);
        LOG.debug("传给检索服务的条件为{}", esJSon.toJSONString());

        LOG.info("patientSetId 为{}的项目导出数据用时{},倒数数据个数{}", patientSetId, (System.currentTimeMillis() - start) / 1000, count);
        LOG.info("本次导入新增数据条数{}", count);
        //存储 搜索日志 数据条目 数据信息
        preLiminaryService.saveLogMoreData(count,esJsonString,createId,createName,projectId,patientName,patientSetId,whereQuery,buildIndex,crfId,esJSon,count+allCount,projectName,crfName);
        return new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,AjaxObject.AJAX_MESSAGE_SUCCESS);
//        executorService.submit((Callable<Object>) () -> {
//
//        });
    }

    public Integer getProjectCount(String projectId){
        String sql = UqlConfig.getAllProjectSql(projectId);
        JSONArray source = new JSONArray();
        String result = httpUtils.querySearch(projectId,sql,1,1,null,source,false);
        return UqlQureyResult.getTotal(result);
    }

}