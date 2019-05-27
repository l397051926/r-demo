package com.gennlife.rws.rocketmq;


import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.content.InputStratus;
import com.gennlife.rws.content.RedisContent;
import com.gennlife.rws.dao.InputTaskMapper;
import com.gennlife.rws.dao.PatientsSetMapper;
import com.gennlife.rws.dao.ProjectMapper;
import com.gennlife.rws.dao.SearchLogMapper;
import com.gennlife.rws.entity.InputTask;
import com.gennlife.rws.entity.PatientsSet;
import com.gennlife.rws.entity.SearchLog;
import com.gennlife.rws.query.BuildIndexRws;
import com.gennlife.rws.service.InputTaskService;
import com.gennlife.rws.service.PatientSetService;
import com.gennlife.rws.service.RedisMapDataService;
import com.gennlife.rws.service.SearchLogService;
import com.gennlife.rws.util.GzipUtil;
import com.gennlife.rws.util.HttpUtils;
import com.gennlife.rws.util.LogUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Component
public class ProjectConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectConsumer.class);
//    public static BlockingQueue<String> blockingQueue = new LinkedBlockingDeque();
    @Autowired
    private RocketMqContent rocketMqContent;
    @Autowired
    private InputTaskMapper inputTaskMapper;
    @Autowired
    private LogUtil logUtil;
    @Autowired
    private RedisMapDataService redisMapDataService;
    @Autowired
    private PatientSetService patientSetService;
    @Autowired
    private PatientsSetMapper patientsSetMapper;
    @Autowired
    private ProducerService producerService;
    @Autowired
    private InputTaskService inputTaskService;
    @Autowired
    private SearchLogService searchLogService;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private HttpUtils httpUtils;

    @PostConstruct
    public void defaultMQPushConsumer() {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(rocketMqContent.getConsumerGroup());
        consumer.setNamesrvAddr(rocketMqContent.getNamesrvAddr());
        try {
            consumer.subscribe(rocketMqContent.getTopicPro(), "*");

            // 如果是第一次启动，从队列头部开始消费
            // 如果不是第一次启动，从上次消费的位置继续消费
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            //设置广播消费
//            consumer.setMessageModel(MessageModel.BROADCASTING);

            consumer.registerMessageListener((MessageListenerConcurrently) (list, context) -> {
                try {
                    for (MessageExt messageExt : list) {
                        //避免重复消费工作
                        String msgId = messageExt.getMsgId();
                        Boolean msgSismember = redisMapDataService.sismemberSet(rocketMqContent.getMessageId(),msgId);
                        if(msgSismember){
                            continue;
                        }else {
                            redisMapDataService.AddSet(rocketMqContent.getMessageId(),msgId);
                        }
                        //TODO 判定messageExt 中的 tag 的类别 然后做不同的业务
                        String tag = messageExt.getTags();
                        String messageBody = new String(messageExt.getBody(), RemotingHelper.DEFAULT_CHARSET);
                        if(rocketMqContent.getRwsImportTag().equals(tag)){
                            transforamImportMessage(messageBody);
                        }else {
                            LOGGER.info("未知 tag ，无法处理");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
            consumer.start();
            System.out.println("[Consumer 已启动]");
        } catch (Exception e) {
            LOGGER.error("Consumer ---------------------启动失败  ");
            e.printStackTrace();
        }
    }

    private void transforamImportMessage(String  messageBody) {
        LOGGER.info("处理导出 message ，进行 业务处理 message = " + messageBody);
        try {
            JSONObject importMessage = JSONObject.parseObject(messageBody);
            String taskId = importMessage.getString("task_id");
            Long createTime = importMessage.getLong("create_time");
            Long startTime = importMessage.getLong("start_time");
            Long finishTime = importMessage.getLong("finish_time");
            Integer status = importMessage.getInteger("status");
            Integer progress = importMessage.getInteger("progress");
            Long remainTime = importMessage.getLong("estimate_cost_time");
            String userId = importMessage.getString("user_id");
            InputTask task = inputTaskMapper.getInputtaskByInputId(taskId);
            if(task == null ){
                LOGGER.warn("不是本套系统的项目 不进行数据处理！");
                return;
            }
            //如果已经是 失败 或者完成的任务 不在进行更新
            if(task.getStatus() == InputStratus.FAILURE  ){
//                BuildIndexRws buildIndexRws =  new BuildIndexRws();
//                buildIndexRws.setBuildIndexID(taskId);
//                buildIndexRws.setAction(2);
//                buildIndexRws.setUid(userId);
//                String result = httpUtils.buildIndexRws(buildIndexRws);
//                LOGGER.info("接受消息 再次 发送取消任务申请  ---  取消任务 取消任务结果: "+result);
                return;
            }
            if(task.getStatus() == InputStratus.FINISH  || task.getStatus() == InputStratus.CANCEL){
                return;
            }
            InputTask inputTask = new InputTask(taskId,createTime,startTime,finishTime,status,progress,remainTime);
            if(task.getStatus() != status){
                inputTask.setUpdateTime(new Date());
            }
            inputTaskMapper.updateInputTask(inputTask);

            if(InputStratus.FAILURE == status){//失败
                JSONObject obj = JSONObject.parseObject(redisMapDataService.getDataBykey(RedisContent.getRwsService(taskId)));
                Integer sum = patientsSetMapper.getSumCount(inputTask.getProjectId());
                if(sum == null || sum == 0){
                    projectMapper.saveDatasource(inputTask.getProjectId(),"","");
                }
//                producerService.sendProExportField(obj.getString("createId"),taskId,obj.getString("projectId"),obj.getString("patientName"));
                inputTaskService.updateCencelDate(taskId);
            }

            if(InputStratus.FINISH == status){//成功
                JSONObject obj = JSONObject.parseObject(redisMapDataService.getDataBykey(RedisContent.getRwsService(taskId)));
                searchLogService.saveSearchLog(obj);
                String content = obj.getString("createName")+"向患者集"+obj.getString("patientName")+"导入"+obj.getLong("curenntCount")+"名患者";
                logUtil.saveLog(obj.getString("projectId"),content,userId,obj.getString("createName"));
                projectMapper.updateCrfId(obj.getString("projectId"),obj.getString("crfId"));
                patientSetService.savePatientImport(obj);
                String projectName = projectMapper.getProjectNameByProjectId(obj.getString("projectId"));
                producerService.sendProExportSucceed(userId,projectName,obj.getString("projectId"),taskId);

            }

        } catch (Exception e ){
            LOGGER.error("处理导出业务发生问题   --- message : " + messageBody );
        }

    }
}