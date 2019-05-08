/**
 * copyRight
 */
package com.gennlife.rws.service.impl;

import com.gennlife.rws.dao.ActiveIndexTaskMapper;
import com.gennlife.rws.entity.ActiveIndexTask;
import com.gennlife.rws.service.ActiveIndexTaskService;
import com.gennlife.rws.util.AjaxObject;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * Created by liuzhen.
 * Date: 2017/10/26
 * Time: 14:35
 */
@Service
public class ActiveIndexTaskServiceImpl implements ActiveIndexTaskService {
    private static Logger LOG = LoggerFactory.getLogger(ActiveIndexTaskServiceImpl.class);
    @Autowired
    private ActiveIndexTaskMapper taskMapper;

    @Override
    public List<ActiveIndexTask> getTaskByActiveIdsAndStatus(List<String> list) {
        Map<String, Object> param = new HashMap<String, Object>(2);
        param.put("list",list);
        return taskMapper.findByActiveIdsAndStatus(param);
    }

    @Override
    public boolean isAllTaskIsNoComplate(List<String> activeId) {
        if(activeId == null || activeId.isEmpty()){
            return false;
        }
        List<ActiveIndexTask> tasks = this.getTaskByActiveIdsAndStatus(activeId);
        if(tasks != null && !tasks.isEmpty()){
            return true;
        }
        return false;
    }

    @Override
    public void initTask(String taskId,String activeId,String projectId,String message){
        ActiveIndexTask indexTask = new ActiveIndexTask();
        indexTask.setId(taskId);
        indexTask.setActiveIndexId(activeId);
        indexTask.setProjectId(projectId);
        indexTask.setSubmitTime(new Date());
        indexTask.setStatus(0);
        indexTask.setSubmitNum(1);
        indexTask.setMessage(message);
        taskMapper.insertSelective(indexTask);
    }

    @Override
    public void replace(ActiveIndexTask task) {
        taskMapper.replace(task);
    }

}
