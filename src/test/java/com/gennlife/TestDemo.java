package com.gennlife;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

//@RunWith(SpringRunner.class)
//@SpringBootTest
//@Ignore
public class TestDemo {
    @Test
    public void testDemo(){
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
}
