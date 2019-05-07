package com.gennlife.rws.util;


import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.dao.OperLogsMapper;
import com.gennlife.rws.entity.OperLogs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class LogUtil {

    @Autowired
    private OperLogsMapper operLogsMapper;

    public void saveLog(String projectId, String content, String createId, String createName) {
        Date date = new Date();
        OperLogs operLogs = new OperLogs();
        operLogs.setProjectId(projectId);
        operLogs.setContent(content);
        operLogs.setCreateId(createId);
        operLogs.setCreateTime(date);
        operLogs.setCreateName(createName);

        operLogsMapper.insert(operLogs);
    }

}
