package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.ActiveIndex;
import com.gennlife.rws.entity.ActiveIndexConfig;
import com.gennlife.rws.entity.ActiveIndexConfigCondition;
import com.gennlife.rws.util.AjaxObject;

import java.util.List;
import java.util.Map;

/**
 * Created by liuzhen.
 * Date: 2017/10/19
 * Time: 12:10
 */
public interface ActiveIndexService {

    AjaxObject saveActive(JSONObject activeIndex);

    AjaxObject updateActive(JSONObject active);

    AjaxObject findByProjectId(String projectId, int type, String name, int pageNum, int pageSize);

    AjaxObject deleteByActiveId(String activeId);

    ActiveIndex findByActiveId(String activeId);

    AjaxObject saveOrUpdate(JSONObject active, String groupToId);

    List<ActiveIndex> findActiveIdByProject(String projectId, Integer type);

    AjaxObject findByProjectIdAndTypeNoPage(String activeId, String projectId, Integer type, String s, String name, String isTwiceIndex);

    List<ActiveIndex> findeByProjectAndType(String projectId, int type);

    boolean conditioIsnChange(JSONObject active);

    List<ActiveIndex> findeByActiveName(Map<String, Object> param);

    AjaxObject judgeActiveDependence(String activeId);

    boolean judgeLoopDep(List<ActiveIndexConfig> configs, String currentActiveId, StringBuffer depRelations);

    AjaxObject getContrastiveActive(String uid, String projectId, Integer cortType);

    boolean editActiveName(Map<String, Object> params);

    AjaxObject getAllResearchVariable(String createId, String projectId, Integer cortType, String uid);

    List<ActiveIndexConfigCondition> findByRefActiveId(String refActiveId);

    List<ActiveIndex> dependenceCurActiveByIsTmp(String activeId);

    String getindexType(String id);

    List<String> getActiveName(String id);
}
