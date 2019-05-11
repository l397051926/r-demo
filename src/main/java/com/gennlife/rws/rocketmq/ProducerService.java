package com.gennlife.rws.rocketmq;

/**
 * @author lmx
 * @create 2019 11 17:23
 * @desc
 **/
public interface ProducerService {
    void sendProExportField(String createId, String taskId, String projectId, String projectName);

    void sendProExportSucceed(String userId, String projectName, String projectId, String taskId);

    void romoveProMember(String uid, String creatorName, String projectName);

    void sendAddProMember(String uid, String creatorName, String projectName, String projectId);
}
