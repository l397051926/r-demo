/**
 * copyRight
 */
package com.gennlife.rws.service.impl;

import com.gennlife.rws.redis.JedisClusterFactory;
import com.gennlife.rws.service.RedisMapDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author liuzhen
 * Created by liuzhen.
 * Date: 2017/12/11
 * Time: 18:23
 */
@Service
public class RedisMapDataServiceImpl implements RedisMapDataService{
    private Logger LOG = LoggerFactory.getLogger(RedisMapDataServiceImpl.class);
    @Autowired
    private JedisClusterFactory jedisClusters;

    /**
     * <p>通过key同时设置 hash的多个field</p>
     * @param key
     * @param hash
     * @return 返回OK 异常返回null
     */
    @Override
    public String hmset(String key,Map<String, String> hash){
        JedisCluster jedis = null;
        String res = null;
        try {
            jedis = jedisClusters.getJedisCluster();
            res = jedis.hmset(key,hash);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return res;
    }
    @Override
    public List<String> hmGet(String key,String ... field){
        JedisCluster jedis = null;
        List<String> res = null;
        try {
            jedis = jedisClusters.getJedisCluster();
            res = jedis.hmget(key,field);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return res;
    }
    @Override
    public Map<String,String> hmGetAll(String key){
        JedisCluster jedis = null;
        Map<String,String> res = null;
        try {
            jedis = jedisClusters.getJedisCluster();
            res = jedis.hgetAll(key);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return res;
    }
    @Override
    public Long delete(String key){
        JedisCluster jedis = null;
        Long del = 0l;
        try {
            jedis = jedisClusters.getJedisCluster();
            del = jedis.del(key);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return del;
    }

    @Override
    public String getDataBykey(String key) {
        JedisCluster jedis = null;
        String res = null;
        try {
            jedis =  jedisClusters.getJedisCluster
                ();
            res = jedis.get(key);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return res;
    }

    @Override
    public String set(String key, String val) {
        JedisCluster jedis = null;
        String res = null;
        try {
            jedis = jedisClusters.getJedisCluster();
            res = jedis.set(key,val);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return res;
    }

    @Override
    public Long AddSet(String key, String val) {
        JedisCluster jedis = null;
        Long res = null;
        try {
            jedis = jedisClusters.getJedisCluster();
            res = jedis.sadd(key,val);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return res;
    }

    @Override
    public boolean sismemberSet(String key, String val) {
        JedisCluster jedis = null;
        Boolean res = null;
        try {
            jedis = jedisClusters.getJedisCluster();
            res = jedis.sismember(key,val);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return res;
    }

    @Override
    public  Set<String> getAllSet(String key) {
        JedisCluster jedis = null;
        Set<String> res = null;
        try {
            jedis = jedisClusters.getJedisCluster();
            res = jedis.smembers(key);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return res;
    }

    @Override
    public Long setOutTime(String key,Integer time){
        JedisCluster jedis = null;
        Long res = null;
        try {
            jedis = jedisClusters.getJedisCluster();
            res = jedis.expire(key,time);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return res;
    }
    @Override
    public Boolean exists(String key){
        JedisCluster jedis = null;
        Boolean res = null;
        try {
            jedis = jedisClusters.getJedisCluster();
            res = jedis.exists(key);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return res;
    }

    @Override
    public String hmGetKey(String key, String val) {
        JedisCluster jedis = null;
        String res = null;
        try {
            jedis = jedisClusters.getJedisCluster();
            res = jedis.hget(key,val);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return res;
    }
}
