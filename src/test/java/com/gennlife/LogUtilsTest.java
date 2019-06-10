
package com.gennlife;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.content.InputStratus;
import com.gennlife.rws.content.RedisContent;
import com.gennlife.rws.content.UqlConfig;
import com.gennlife.rws.dao.*;
import com.gennlife.rws.entity.*;
import com.gennlife.rws.rocketmq.ProducerService;
import com.gennlife.rws.rocketmq.RocketMqContent;
import com.gennlife.rws.service.CortrastiveAnalysisService;
import com.gennlife.rws.service.GroupService;
import com.gennlife.rws.service.RedisMapDataService;
import com.gennlife.rws.util.GzipUtil;
import com.gennlife.rws.util.LogUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * @author
 * @create 2018 07 14:55
 * @desc
 **/

@RunWith(SpringRunner.class)
@SpringBootTest
public class LogUtilsTest {
    @Autowired
    LogUtil logUtil;

    @Autowired
    private RedisMapDataService redisMapDataService;

    @Autowired
     private GroupService groupService;

    @Autowired
    private InputTaskMapper inputTaskMapper;

    @Autowired
    private SearchLogMapper searchLogMapper;

    @Autowired
    private PatientsSetMapper patientsSetMapper;

    @Autowired
    private ProducerService producerService;

    @Autowired
    private RocketMqContent rocketMqContent;
    @Autowired
    ActiveIndexMapper activeIndexMapper;

    @Autowired
    private GroupDataMapper groupDataMapper;
    @Autowired
    private CortrastiveAnalysisService cortrastiveAnalysisService;
    @Autowired
    private ProjectMapper projectMapper;

    @Test
    public void getCreateId(){
//        String id = projectMapper.getCreateIdByTaskId("1bf2852d-85fa-42f8-ab94-bdc9d13e9906");
//        System.out.println(id);
//        JSONObject obj = JSONObject.parseObject(redisMapDataService.getDataBykey(RedisContent.getRwsService("6f87691e-ab42-4649-bd7b-5914667a488a")));
        PatientsSet patientsSet = patientsSetMapper.selectByPatSetId("25db522d740c41d8a2f1bda64429dd21");
        System.out.println();

    }

    @Test
    public void test() throws InterruptedException, ExecutionException, IOException {
        JSONObject param = new JSONObject();
        param.put("projectId","7457dea269354dbd93cfc8e19f887b8f");
        param.put("uid","0884a6d1-fdc2-4f70-9366-e16dd7703f8b");
        param.put("crfId","EMR");
        param.put("calculations",new JSONArray().fluentAdd("426F3893DEFC461DB1B285775AED9E2A").fluentAdd("BDAC0A21EA0A4592B4C453725FA62E25"));
        param.put("patientSns",new JSONArray().fluentAdd("pat_004c6164300dbece57fc1ef7743ac1d1"));
        cortrastiveAnalysisService.calculationResultOne(param);
    }
    private static ExecutorService executorService1 = Executors.newFixedThreadPool(8);

    @Test
    public void redisMapTest() {

        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("projectId", "9ae8490e1a8c4501a8c62ea9653e1fee");
        mapParam.put("isVariant", 1);
        List<ActiveIndex> activeIndex = activeIndexMapper.getAllResearchVariable(mapParam);
        for (ActiveIndex activeIndex1 : activeIndex){
            String id = activeIndex1.getId();
            redisMapDataService.delete(UqlConfig.CORT_INDEX_REDIS_KEY.concat(id));
            System.out.println("删除 " + id);
        }
//        Map<String,String> map = new HashMap();
//        map.put("a","1");
//        map.put("b","2");
//        map.put("c","3");
//        map.put("d","4");
//        System.out.println(redisMapDataService.exists("rws_test_map"));
//        String aaa= redisMapDataService.hmset("rws_test_map",map);
//        System.out.println(redisMapDataService.exists("rws_test_map"));
//        redisMapDataService.setOutTime("rws_test_map",30);
//
//        System.out.println();
//        System.out.println(redisMapDataService.exists("rws_test_map"));


//        Map<String,String> dd = redisMapDataService.hmGetAll("rws_test_map");
//        System.out.println("rws_test_map" + dd);
//
////        redisMapDataService.delete("rws_test");
////        System.out.println("delete succefull");
//
//        String deletedd = redisMapDataService.getDataBykey("rws_test");
//        System.out.println("delete:" + deletedd);

    }

    @Test
    public void RedisSet(){
        Long a = redisMapDataService.AddSet("demo_rws","aaa");
        Long b = redisMapDataService.AddSet("demo_rws","bbb");
        Long c = redisMapDataService.AddSet("demo_rws","ccc");
        Long d = redisMapDataService.AddSet("demo_rws","ccc");
        Set<String> stringSet = redisMapDataService.getAllSet("demo_rws");
        boolean df = redisMapDataService.sismemberSet("demo_rws","aaa");
        System.out.println(df);
    }

    @Test
    public void testGroupMapperBigData(){
        Long time = System.currentTimeMillis();
        List<List<GroupData>> list = new ArrayList<>();
        List<GroupData> groupDataList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            if(i != 0 &&  i % 5000 == 0  ){
                list.add(groupDataList);
                groupDataList = new ArrayList<>();
            }
            GroupData groupData = new GroupData();
            groupData.setGroupId("154e846e374d46ea85ed25efd27774df");
            groupData.setPatientSn("pat_004c6164300dbece57fc1ef7743ac1"+i);
            groupData.setRemove("0");
            groupData.setPatientSetId("355626762e944c5e8cb9fc74856447ds");
            groupData.setPatientDocId(i+"");
            groupDataList.add(groupData);
        }
        list.add(groupDataList);
        System.out.println("demo");
//        List<Future> futures = new ArrayList<>();
//        for (List<GroupData> li : list){
//            Future future =executorService1.submit(() -> groupDataMapper.batchInsert(li));
//            futures.add(future);
//        }
//        for (Future future : futures){
//            System.out.println("A");
//        }
        for (List<GroupData> li : list){
            groupDataMapper.batchInsert(li);
        }
        System.out.println("插入时间: " + (System.currentTimeMillis()-time));

    }

	@Test
	public void getGroupNamePath(){
	  String name =   groupService.getGroupNamePath("5901fcda26764a9eb04638672d8931b3","版本");
	  System.out.println(name);
    }

    @Test
    public void redisTest() {
        String tmp = redisMapDataService.set("rws_test", "rws_test_demo");
        System.out.println("set_tmp: " + tmp);

        String dd = redisMapDataService.getDataBykey("rws_test");
        System.out.println("gettmp" + dd);

        redisMapDataService.delete("rws_test");
        System.out.println("delete succefull");


        String deletedd = redisMapDataService.getDataBykey("rws_test");
        System.out.println("delete:" + deletedd);

    }

    @Test
    public void readRedisTest() throws IOException {
        String uid = "9cccc006-c89a-4864-a59f-662cbe560eb8";
        JSONObject obj = JSONObject.parseObject(redisMapDataService.getDataBykey(RedisContent.getRwsService(uid)));
        SearchLog searchLog = new SearchLog();
        searchLog.setCreateId(obj.getString("createId"));
        searchLog.setCreateTime(new Date());
        searchLog.setPatientSetId(obj.getString("patientSetId"));
        //处理搜索条件
        JSONObject searchObj = JSONObject.parseObject(obj.getString("searchCondition"));
        String query = searchObj.getString("query");
        searchLog.setSearchConditio(query);
        searchLogMapper.insert(searchLog);
        //存储业务日志
        String content = obj.getString("createName") + "向患者集" + obj.getString("patientName") + "导入" + obj.getLong("curenntCount") + "名患者";
        logUtil.saveLog(obj.getString("projectId"), content, obj.getString("createId"), obj.getString("createName"));

        Integer count = patientsSetMapper.getPatientSetCount(obj.getString("patientSetId"));
        PatientsSet patientsSet = new PatientsSet();
        patientsSet.setPatientsSetId(obj.getString("patientSetId"));
        patientsSet.setPatientsCount(obj.getLong("curenntCount"));
        patientsSet.addComUqlQuery(obj.getString("uqlQuery"));
        if (count == 0) {
            patientsSetMapper.insert(patientsSet);
        } else {
//            patientsSetMapper.updatePatientsCountAndQuery(obj.getString("patientSetId"), obj.getLong("curenntCount"), GzipUtil.compress(obj.getString("uqlQuery")));
        }
        //发送消息 表示已经完成
        JSONObject msgObj = new JSONObject()
            .fluentPut("user_id", obj.getString("createId"))
            .fluentPut("msg", "导入" + obj.getString("patientName") + "项目的任务已完成");
//        producerService.send(rocketMqContent.getTopicPro(), rocketMqContent.getRemoveProUserTag(), msgObj.toJSONString());
    }

    @Test
    public void setInputs() {
        InputTask inputTask = new InputTask();
        inputTask.setInputId("aa");
        inputTask.setProjectId("bb");
        inputTask.setProjectName("|cc");
        inputTask.setPatientSetId("dd");
        inputTask.setPatientSetName("ee");
        inputTask.setUid("ff");
        inputTask.setPatientCount(1234567L);
        inputTask.setCreateTime(new Date());
        inputTask.setStartTime(new Date());
        inputTask.setStatus(InputStratus.IN_QUEUE);
        inputTask.setUpdateTime(new Date());
        inputTaskMapper.insert(inputTask);
    }

    @Test
    public void updateInputs() {
        String messageBody = "{\"estimate_cost_time\":2075,\"user_id\":\"13157887-ff05-47a2-b34a-bcf09312be8f\",\"progress\":16.46090534979424,\"task_id\":\"d78a5475-1411-44fb-ba92-acc3f70129f3\",\"status\":3}";
        JSONObject importMessage = JSONObject.parseObject(messageBody);
        String taskId = importMessage.getString("task_id");
        Long createTime = importMessage.getLong("create_time");
        Long startTime = importMessage.getLong("start_time");
        Long finishTime = importMessage.getLong("finish_time");
        Integer status = importMessage.getInteger("status");
        Integer progress = importMessage.getInteger("progress");
        Long remainTime = importMessage.getLong("estimate_cost_time");

        InputTask inputTask = new InputTask(taskId, createTime, startTime, finishTime, status, progress, remainTime);
        inputTask.setUpdateTime(new Date());
        inputTaskMapper.updateInputTask(inputTask);
    }


    @Test
    public void savaLog() {
        String projectName = "AA";
        String projectId = "test1";
        String createId = "user1";
        String content = createId + "创建了 项目：" + projectName;
        logUtil.saveLog(projectId, content, createId, createId);
    }

}

