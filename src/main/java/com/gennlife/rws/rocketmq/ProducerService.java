package com.gennlife.rws.rocketmq;

import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class ProducerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerService.class);

    @Autowired
    private RocketMqContent rocketMqContent;

    private DefaultMQProducer producer;

    @PostConstruct
    public void initProducer() {
        producer = new DefaultMQProducer(rocketMqContent.getProducerGroup());
        producer.setNamesrvAddr(rocketMqContent.getNamesrvAddr());
        producer.setRetryTimesWhenSendFailed(3);
        try {
            producer.start();
            System.out.println("[Producer 已启动]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String send(String topic, String tags, String msg) {
        SendResult result = null;
        try {
            Message message = new Message(topic, tags, msg.getBytes(RemotingHelper.DEFAULT_CHARSET));
            result = producer.send(message);
            LOGGER.info("发送了一条消息  msgID(" + result.getMsgId() + ") 结果为： " + result.getSendStatus() + "msg:" +msg +"tags:" +tags + "topic: " + topic);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{\"MsgId\":\"" + result.getMsgId() + "\"}";
    }

    @PreDestroy
    public void shutDownProducer() {
        if (producer != null) {
            producer.shutdown();
        }
    }

    public void sendProExportField(String userId, String taskId, String projectId, String projectName) {
        String msg = "导入" + projectName + "项目的任务失败 ";
        JSONObject data = new JSONObject().fluentPut("user_id", userId)
            .fluentPut("msg", msg)
            .fluentPut("project_id", projectId)
            .fluentPut("task_id", taskId);
        send(rocketMqContent.getTopicPro(), rocketMqContent.getRwsImportField(), data.toJSONString());
    }

    public void sendProExportSucceed( String createId, String patientName,String projectId, String taskId) {
        JSONObject msgObj = new JSONObject()
            .fluentPut("user_id", createId)
            .fluentPut("msg", "导入" + patientName + "项目的任务已完成")
            .fluentPut("project_id", projectId)
            .fluentPut("task_id", taskId);
        send(rocketMqContent.getTopicPro(), rocketMqContent.getRwsImportSucceed(), msgObj.toJSONString());
    }

    public void romoveProMember(String uid, String creatorName, String projectName) {
        JSONObject msgObj = new JSONObject()
            .fluentPut("user_id", uid)
            .fluentPut("msg", addProjectMsg(creatorName + "将你从“" + projectName + "”项目中移除"));
        send(rocketMqContent.getTopicPro(), rocketMqContent.getRemoveProUserTag(), msgObj.toJSONString());
    }

    public void sendAddProMember(String uid, String creatorName, String projectName, String projectId) {
        JSONObject msgObj = new JSONObject()
            .fluentPut("user_id", uid)
            .fluentPut("msg", addProjectMsg(creatorName + "将你加入到“" + projectName + "”项目"))
            .fluentPut("project_id", projectId);
        //发送一条消息到 rocketmq
        send(rocketMqContent.getTopicPro(), rocketMqContent.getAddProUserTag(), msgObj.toJSONString());
    }

    private String addProjectMsg(String msg){
        return "【项目】 "+msg;
    }
}

