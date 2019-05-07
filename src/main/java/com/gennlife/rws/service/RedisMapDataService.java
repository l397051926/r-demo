package com.gennlife.rws.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author liuzhen
 *         Created by liuzhen.
 *         Date: 2017/12/11
 *         Time: 18:14
 */
public interface RedisMapDataService {
    String hmset(String key,Map<String, String> hash);
    public List<String> hmGet(String key, String ... field);
    public Long delete(String key);
    public String getDataBykey(String key);
    String set(String key , String val);
    Long AddSet(String key,String val);
    boolean sismemberSet(String key,String val);
    Set<String> getAllSet(String key);

}
