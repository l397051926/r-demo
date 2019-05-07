package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.dao.ContrastiveAnalysisActiveMapper;
import com.gennlife.rws.dao.ProjectUserMapMapper;
import com.gennlife.rws.entity.ContrastiveAnalysisActive;
import com.gennlife.rws.service.ActiveIndexService;
import com.gennlife.rws.service.ContrastiveAnalysisActiveService;
import com.gennlife.rws.util.AjaxObject;
import com.gennlife.rws.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author liumingxin
 * @create 2018 16 15:34
 * @desc
 **/
@Service
public class ContrastiveAnalysisActiveServiceImpl implements ContrastiveAnalysisActiveService {
    @Autowired
    ContrastiveAnalysisActiveMapper contrastiveAnalysisActiveMapper;
    @Autowired
    private ActiveIndexService activeIndexService;
    @Autowired
    private LogUtil logUtil;

    @Override
    public List<String> getActiveIndexes(String uid, String projectId,Integer cortType) {
        return contrastiveAnalysisActiveMapper.getActiveIndexes(uid,projectId, cortType);
    }

    @Override
    public AjaxObject saveContrastiveActive(JSONObject paramObj) {
        JSONArray activeIds = paramObj.getJSONArray("activeIds");
        String createId = paramObj.getString("createId");
        String projectId = paramObj.getString("projectId");
        Integer cortType = paramObj.getInteger("cortType");
        String uid = paramObj.getString("uid");
        Date createTime = new Date();
        int size = activeIds !=null ? activeIds.size() : 0;
        contrastiveAnalysisActiveMapper.deleteByUidAndProjectId(uid,projectId,cortType);
        for (int i = 0; i < size; i++) {
            String activeId = activeIds.getString(i);
            ContrastiveAnalysisActive contrastiveAnalysisActive = new ContrastiveAnalysisActive();
            contrastiveAnalysisActive.setActiveIndexId(activeId);
            contrastiveAnalysisActive.setCreateId(uid);
            contrastiveAnalysisActive.setProjectId(projectId);
            contrastiveAnalysisActive.setCreateTime(createTime);
            contrastiveAnalysisActive.setCortType(cortType);
            contrastiveAnalysisActiveMapper.insert(contrastiveAnalysisActive);
        }
        return new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,AjaxObject.AJAX_MESSAGE_SUCCESS);
    }

    @Autowired
    private ProjectUserMapMapper projectUserMapMapper;

    @Override
    public AjaxObject deleteContrastiveActive(JSONObject paramObj) {
        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,AjaxObject.AJAX_MESSAGE_SUCCESS);
        String activeIndexId = paramObj.getString("activeIndexId");
        String activeName = paramObj.getString("activeName");
        String createId = paramObj.getString("createId");
        String uid = paramObj.getString("uid");
        String createName = paramObj.getString("createName");
        String projectId = paramObj.getString("projectId");
        String content = createName + "删除 研究变量 ： " + activeName;
        logUtil.saveLog(projectId, content, createId, createName);
        ajaxObject = activeIndexService.deleteByActiveId(activeIndexId);
//        Map<String,>
        //同时删除 对比分析条件中的研究变量
//        contrastiveAnalysisActiveMapper.deleteByActiveId(paramMap);
        Map<String,Object> paramMap = (Map<String, Object>) paramObj;
        contrastiveAnalysisActiveMapper.deleteByActiveId(paramMap);
        List<String> uids = projectUserMapMapper.getUserIds(projectId);
        return ajaxObject;
    }

    @Override
    public void saveContrastiveNewActive(String id, String create_user, String projectId, String type) {
        Date createTime = new Date();
        ContrastiveAnalysisActive contrastiveAnalysisActive = new ContrastiveAnalysisActive();
        contrastiveAnalysisActive.setActiveIndexId(id);
        contrastiveAnalysisActive.setCreateId(create_user);
        contrastiveAnalysisActive.setProjectId(projectId);
        contrastiveAnalysisActive.setCreateTime(createTime);
        if("数值:double".equals(type) || "枚举:boolean".equals(type)){
            contrastiveAnalysisActive.setCortType(1);
            contrastiveAnalysisActiveMapper.insert(contrastiveAnalysisActive);
        }
        contrastiveAnalysisActive.setCortType(2);
        contrastiveAnalysisActiveMapper.insert(contrastiveAnalysisActive);
    }


}
