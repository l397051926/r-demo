package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.dao.SearchLogMapper;
import com.gennlife.rws.entity.SearchLog;
import com.gennlife.rws.service.SearchLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @create 2019 29 15:50
 * @desc
 **/
@Service
public class SearchLogServiceImpl implements SearchLogService{
    private static final Logger LOG = LoggerFactory.getLogger(SearchLogServiceImpl.class);
    @Autowired
    private SearchLogMapper searchLogMapper;

    @Override
    public void saveSearchLog(JSONObject obj) {
        SearchLog searchLog = new SearchLog();
        searchLog.setCreateId(obj.getString("createId"));
        searchLog.setCreateTime(new Date());
        searchLog.setPatientSetId(obj.getString("patientSetId"));
        //处理搜索条件
        JSONObject searchObj = JSONObject.parseObject(obj.getString("searchCondition"));
        String query = searchObj.getString("query");
        searchLog.setSearchConditio(query);
        searchLogMapper.insert(searchLog);

    }
}
