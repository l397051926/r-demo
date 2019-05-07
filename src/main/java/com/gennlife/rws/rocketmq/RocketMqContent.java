package com.gennlife.rws.rocketmq;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author liumingxin
 * @create 2019 02 14:45
 * @desc
 **/
@Component
public class RocketMqContent {
    @Value("${apache.rocketmq.producer.producerGroup}")
    private String producerGroup;
    @Value("${apache.rocketmq.consumer.PushConsumer}")
    private String consumerGroup;
    @Value("${apache.rocketmq.namesrvAddr}")
    private String namesrvAddr;  //地址
    @Value("${apache.rocketmq.topicSys}")
    private String topicSys;    //系统消息
    @Value("${apache.rocketmq.topicPro}")
    private String topicPro;    //项目消息
    @Value("${apache.rocketmq.topicAuth}")
    private String topicAuth;   //权限消息
    @Value("${apache.rocketmq.sysUpdateTag}")
    private String sysUpdateTag;   //版本升级
    @Value("${apache.rocketmq.addProUserTag}")
    private String addProUserTag;   //被项目创建者拉入某个项目
    @Value("${apache.rocketmq.removeProUserTag}")
    private String removeProUserTag;   //某个项目的参与者被创建者移除
    @Value("${apache.rocketmq.rwsImport}")
    private String rwsImportTag;   //RWS数据导入
    @Value("${apache.rocketmq.rwsImportField}")
    private String rwsImportField;   //RWS数据导入
    @Value("${apache.rocketmq.changeUserPowerTag}")
    private String changeUserPowerTag;   //用户被管理员调整了权限
    @Value("${apache.rocketmq.messageId}")
    private String messageId ;
    @Value("${apache.rocketmq.rwsImportSucceed}")
    private String rwsImportSucceed ;

    public String getProducerGroup() {
        return producerGroup;
    }

    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getNamesrvAddr() {
        return namesrvAddr;
    }

    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }

    public String getTopicSys() {
        return topicSys;
    }

    public void setTopicSys(String topicSys) {
        this.topicSys = topicSys;
    }

    public String getTopicPro() {
        return topicPro;
    }

    public void setTopicPro(String topicPro) {
        this.topicPro = topicPro;
    }

    public String getTopicAuth() {
        return topicAuth;
    }

    public void setTopicAuth(String topicAuth) {
        this.topicAuth = topicAuth;
    }

    public String getSysUpdateTag() {
        return sysUpdateTag;
    }

    public void setSysUpdateTag(String sysUpdateTag) {
        this.sysUpdateTag = sysUpdateTag;
    }

    public String getAddProUserTag() {
        return addProUserTag;
    }

    public void setAddProUserTag(String addProUserTag) {
        this.addProUserTag = addProUserTag;
    }

    public String getRemoveProUserTag() {
        return removeProUserTag;
    }

    public void setRemoveProUserTag(String removeProUserTag) {
        this.removeProUserTag = removeProUserTag;
    }

    public String getRwsImportTag() {
        return rwsImportTag;
    }

    public void setRwsImportTag(String rwsImportTag) {
        this.rwsImportTag = rwsImportTag;
    }

    public String getChangeUserPowerTag() {
        return changeUserPowerTag;
    }

    public void setChangeUserPowerTag(String changeUserPowerTag) {
        this.changeUserPowerTag = changeUserPowerTag;
    }

    public String getRwsImportField() {
        return rwsImportField;
    }

    public void setRwsImportField(String rwsImportField) {
        this.rwsImportField = rwsImportField;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getRwsImportSucceed() {
        return rwsImportSucceed;
    }

    public void setRwsImportSucceed(String rwsImportSucceed) {
        this.rwsImportSucceed = rwsImportSucceed;
    }
}
