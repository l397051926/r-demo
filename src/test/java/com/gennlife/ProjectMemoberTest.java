/*
package com.gennlife;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.ProjectMember;
import com.gennlife.rws.service.ProjectMemberService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

*/
/**
 * @author liumingxin
 * @create 2018 11 14:29
 * @desc
 **//*

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProjectMemoberTest {
    @Autowired
    private ProjectMemberService projectMemberService;

    @Test
    public void getProjectMemoberTest(){
        JSONObject object = new JSONObject();
        object.put("projectid","123111");
        List<ProjectMember> list = projectMemberService.getProjectMemberList(object);
        String d = JSONObject.toJSONString(list);
        System.out.println(d);
    }

}
*/
