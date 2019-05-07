package com.gennlife.rws.catche;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author lmx
 * @create 2019 16 11:53
 * @desc
 **/
public class CortrastiveCache {

    private static final Map<String,Map<String, List<JSONObject>>> redisMap = new ConcurrentHashMap();
    private static final Set<String> delProjectOrPatientSetTaskSet = new CopyOnWriteArraySet<>();

    public static Map<String,Map<String, List<JSONObject>>>  getRedisMap(){
        return redisMap;
    }

    public static Set<String> getDelProjectOrPatientSetTaskSet() {
        return delProjectOrPatientSetTaskSet;
    }

    public static void cleanRedisMap(){
        redisMap.clear();
        delProjectOrPatientSetTaskSet.clear();
    }

}
