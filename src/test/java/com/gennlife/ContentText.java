package com.gennlife;

import com.gennlife.rws.content.LiminaryContent;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class ContentText {

    @Autowired
    private LiminaryContent liminaryContent;

    @Test
    public void testDemo(){

    }
    @Test
    public void soutLiminaryContent(){
        System.out.println(liminaryContent.getMaxMember());
        System.out.println(liminaryContent.getGroupBlock());
    }
}
