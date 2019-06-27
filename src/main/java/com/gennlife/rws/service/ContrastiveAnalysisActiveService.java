package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.util.AjaxObject;

import java.util.List;

public interface ContrastiveAnalysisActiveService {

    /**
     * 获取研究变量 activeIds
     * @param uid
     * @param projectId
     * @return
     */
    List<String> getActiveIndexes(String uid, String projectId,Integer cortType);

    /**
     * 保存 研究变量中间变化
     * @return
     */
    AjaxObject saveContrastiveActive(JSONObject paramObj);

    /**
     * 删除研究变量
     * @param paramObj
     * @return
     */
    AjaxObject deleteContrastiveActive(JSONObject paramObj);

    void saveContrastiveNewActive(String id, String create_user, String projectId, String type);

    void deleteContrastiveActiveById(String id, String projectId);
}
