package com.gennlife;

import com.alibaba.fastjson.JSONArray;
import com.gennlife.rws.entity.ActiveSqlMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

//@RunWith(SpringRunner.class)
//@SpringBootTest
//@Ignore
public class TestDemo {
    @Test
    public void testDemo() {

    }

    @Test
    public void SetRemoveTest() {
        System.out.println("temp");
        Set<String> set1 = new HashSet<>();
        set1.add("aaa");
        set1.add("bbb");
        set1.add("ccc");
        set1.add("ddd");

        Set<String> set2 = new HashSet<>();
        set2.add("ccc");
        set2.add("ddd");
        set2.add("eee");
        set2.add("fff");
        set1.removeAll(set2);
        set2.removeAll(set1);
        System.out.println();
    }

    @Test
    public void refActiveIdLambd() {
        JSONArray array1 = new JSONArray().fluentAdd("aaa").fluentAdd("bbb").fluentAdd("ccc");
        JSONArray array2 = new JSONArray().fluentAdd("bbb").fluentAdd("ccc").fluentAdd("ddd");
        JSONArray array3 = new JSONArray().fluentAdd("eee").fluentAdd("fff").fluentAdd("ccc");

        ActiveSqlMap activeSqlMap1 = new ActiveSqlMap();
        activeSqlMap1.setRefActiveIds(array1.toJSONString());
        ActiveSqlMap activeSqlMap2 = new ActiveSqlMap();
        activeSqlMap2.setRefActiveIds(array2.toJSONString());
        ActiveSqlMap activeSqlMap3 = new ActiveSqlMap();
        activeSqlMap3.setRefActiveIds(array3.toJSONString());

        List<ActiveSqlMap> sqlList = new ArrayList<>();
        sqlList.add(activeSqlMap1);
        sqlList.add(activeSqlMap2);
        sqlList.add(activeSqlMap3);

        Set<String> refList = sqlList.stream().map(sqlMap -> sqlMap.getRefActiveIds()).flatMap(array -> JSONArray.parseArray(array).stream().map(String.class::cast)).collect(toSet());
        System.out.println();
    }
}
