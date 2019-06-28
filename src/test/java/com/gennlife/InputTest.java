package com.gennlife;

import com.gennlife.rws.dao.InputTaskMapper;
import com.gennlife.rws.entity.InputTask;
import org.aspectj.lang.annotation.Aspect;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * @author lmx
 * @create 2019 28 14:16
 * @desc
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class InputTest {
    @Autowired
    private InputTaskMapper inputTaskMapper;

    // {"estimate_cost_time":812627,"user_id":"ad880b5b-0841-4d19-aefb-1fed5ff348bc","progress":16,"task_id":"2239a8bc-0eb1-4f7a-968a-75448b2fd673","status":3}
    @Test
    public void UpdateInputTask(){
        String taskId = "test_ceshi_111";
        Long startTime = null;
        Long createTime = null;
        Long finishTime = null;
        Integer status = 2;
        Integer progress = 16;
        Long remainTime = 81321L;

        InputTask inputTask = new InputTask(taskId,createTime,startTime,finishTime,status,progress,remainTime);
        inputTask.setUpdateTime(new Date());
        inputTaskMapper.updateInputTaskOnDecideStatus(inputTask);
    }

    @Test
    public void testDemo(){

    }
}
