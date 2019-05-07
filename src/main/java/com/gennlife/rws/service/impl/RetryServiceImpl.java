/**
 * copyRight
 */
package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.content.CommonContent;
import com.gennlife.rws.service.RetryService;
import com.gennlife.rws.util.AjaxObject;
import com.gennlife.rws.util.HttpUtils;
import com.gennlife.rws.util.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * @author  liuzhen.
 * Date: 2017/11/1
 * Time: 16:31
 */
@Service
public class RetryServiceImpl implements RetryService {
    private Logger LOG = LoggerFactory.getLogger(RetryServiceImpl.class);
    @Autowired
    private HttpUtils httpUtils;
    @Override
    @Retryable(value = {RuntimeException.class,Exception.class},maxAttempts = 3,backoff = @Backoff(delay = 1000,multiplier = 2))
    public AjaxObject callPackagingeForCalc(String conditions){

        String s = httpUtils.httpPost(conditions, httpUtils.getPackagingServiceUrl());
        AjaxObject ajaxObject = new AjaxObject();
        /*{"timestamp":1509346270006,"status":404,"error":"Not Found","message":"No message available","path":"/rw/test"}*/
        if(StringUtils.isEmpty(s)){
            throw new RuntimeException("请求失败，重试,未知异常",new Throwable("返回值为空"));
        }
        JSONObject object = JSONObject.parseObject(s);
        if(object.getInteger(CommonContent.REMOTE_STATUS) != HttpStatus.SC_OK){
            LOG.error("请求失败，重试,packaging service 返回信息未={}",s);
            throw new RuntimeException("请求失败，重试"+object.getString("message"),new Throwable(object.getString("message")));
        }else {
            ajaxObject.setData(s);
        }
        return ajaxObject;
    }
    @Override
    @Recover
    public void recover(RuntimeException e,String conditions){
        LOG.info("重试失败1................{}",conditions);
    }
    @Override
    @Recover
    public void recover(Exception e,String conditions){
        LOG.info("重试失败2................");
    }
}
