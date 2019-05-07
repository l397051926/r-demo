package com.gennlife.rws.service;

import com.gennlife.rws.entity.ActiveIndexTask;
import com.gennlife.rws.util.AjaxObject;

import java.util.List;
import java.util.Map;

/**
 * Created by liuzhen.
 * Date: 2017/10/26
 * Time: 14:31
 */
public interface ActiveIndexTaskService {
    public List<ActiveIndexTask> getTaskByActiveIdsAndStatus(List<String> activeId);
    public boolean isAllTaskIsNoComplate(List<String> activeId);

    void initTask(String taskId, String activeId, String projectId, String message);


    public void replace(ActiveIndexTask task);

}
