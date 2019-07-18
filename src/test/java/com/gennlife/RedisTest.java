package com.gennlife;

import com.gennlife.rws.service.RedisMapDataService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class RedisTest {

    @Autowired
    private RedisMapDataService redisMapDataService;

    @Test
    public void testDemo() {

    }

    @Test
    public void setMapTest(){
        Map<String,String> map = new HashMap<>();
        map.put("aaa","111");
        map.put("bbb","222");
        map.put("ccc","333");
        redisMapDataService.hmset("rws_redis_test",map);
        Map<String,String> tmp =  redisMapDataService.hmGetAll("rws_redis_test");
        System.out.println(tmp);
    }
}
