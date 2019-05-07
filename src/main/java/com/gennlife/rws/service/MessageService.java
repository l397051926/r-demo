package com.gennlife.rws.service;

import java.util.List;

/**
 * @author liuzhen
 *         Created by liuzhen.
 *         Date: 2017/12/15
 *         Time: 14:10
 */
public interface MessageService {
    /**
     *  发送消息
     * @param topic
     * @param key
     * @param message
     */
    public void sendMessage(String topic, String key,Object message);

    public void receivedMessage();

    public void sendMutiMesage(String topic, String key, List<Object> message);

}
