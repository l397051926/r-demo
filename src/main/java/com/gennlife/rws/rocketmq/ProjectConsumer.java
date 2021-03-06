package com.gennlife.rws.rocketmq;


import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.content.InputStratus;
import com.gennlife.rws.content.UqlConfig;
import com.gennlife.rws.dao.InputTaskMapper;
import com.gennlife.rws.dao.PatientsSetMapper;
import com.gennlife.rws.dao.ProjectMapper;
import com.gennlife.rws.entity.InputTask;
import com.gennlife.rws.entity.Project;
import com.gennlife.rws.service.*;
import com.gennlife.rws.util.LogUtil;
import com.gennlife.rws.util.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

@Component
public class ProjectConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectConsumer.class);
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
    private CortrastiveAnalysisService cortrastiveAnalysisService;

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
                        Boolean msgSismember = redisMapDataService.sismemberSet(rocketMqContent.getMessageId(), msgId);
                        if (msgSismember) {
                            continue;
                        } else {
                            redisMapDataService.AddSet(rocketMqContent.getMessageId(), msgId);
                        }
                        //TODO 判定messageExt 中的 tag 的类别 然后做不同的业务
                        String tag = messageExt.getTags();
                        String messageBody = new String(messageExt.getBody(), RemotingHelper.DEFAULT_CHARSET);
                        if (rocketMqContent.getRwsImportTag().equals(tag)) {
                            transforamImportMessage(messageBody);
                        } else {
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

    private void transforamImportMessage(String messageBody) {
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
            if (task == null) {
                LOGGER.warn("不是本套系统的项目 不进行数据处理！");
                return;
            }
            String projectName = projectMapper.getProjectNameByProjectId(task.getProjectId());
            //如果已经是 失败 或者完成的任务 不在进行更新
            if (task.getStatus() == InputStratus.FAILURE || task.getStatus() == InputStratus.FINISH || task.getStatus() == InputStratus.CANCEL) {
                return;
            }
            InputTask inputTask = new InputTask(taskId, createTime, startTime, finishTime, status, progress, remainTime);
            inputTask.setUpdateTime(new Date());
            inputTaskMapper.updateInputTaskOnDecideStatus(inputTask);

            if (InputStratus.FAILURE == status) {//失败
                Integer sum = patientsSetMapper.getSumCount(inputTask.getProjectId());
                if (sum == null || sum == 0) {
                    projectMapper.saveDatasource(inputTask.getProjectId(), "", "");
                }
                producerService.sendProExportField(task.getUid(), taskId, task.getProjectId(), projectName);
                inputTaskService.updateCencelDate(taskId);
            }
            if (InputStratus.FINISH == status) {//成功
                JSONObject obj = JSONObject.parseObject(redisMapDataService.getDataBykey(UqlConfig.getRwsService(taskId)));
                if (obj == null || StringUtils.isEmpty(obj.getString("uqlQuery")) || obj.getLong("curenntCount") == null || StringUtils.isEmpty(obj.getString("patientSetId"))) {
                    Project project = projectMapper.selectByProjectId(task.getProjectId());
                    InputTask taskAll = inputTaskMapper.getInputtaskAllByInputId(task.getInputId());
                    obj = new JSONObject()
                        .fluentPut("createId", taskAll.getUid())
                        .fluentPut("patientSetId", taskAll.getPatientSetId())
                        .fluentPut("searchCondition", taskAll.getEsJson())
                        .fluentPut("createName", project.getCreatorName())
                        .fluentPut("patientName", taskAll.getPatientSetName())
                        .fluentPut("curenntCount", taskAll.getPatientCount())
                        .fluentPut("projectId", taskAll.getProjectId())
                        .fluentPut("crfId", taskAll.getCrfId())
                        .fluentPut("uqlQuery", taskAll.getUqlQuery());
                }
                String content = obj.getString("createName") + "向患者集" + obj.getString("patientName") + "导入" + obj.getLong("curenntCount") + "名患者";
                searchLogService.saveSearchLog(obj);
                logUtil.saveLog(obj.getString("projectId"), content, userId, obj.getString("createName"));
                projectMapper.updateCrfId(obj.getString("projectId"), obj.getString("crfId"));
                patientSetService.savePatientImport(obj);
                cortrastiveAnalysisService.deleteActiveIndexVariable(obj.getString("projectId"));
                producerService.sendProExportSucceed(userId, projectName, obj.getString("projectId"), taskId);
            }
        } catch (Exception e) {
            LOGGER.error("处理导出业务发生问题   --- message : " + messageBody);
        }

    }
}