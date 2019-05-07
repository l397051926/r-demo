package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONArray;
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

    public AjaxObject saveActive(JSONObject activeIndex);
    public AjaxObject updateActive(JSONObject active);
    public String findById(Long id);
    public String findAllWithPage(ActiveIndex activeIndex, int pageNum, int pageSize);
    AjaxObject findByProjectId(String projectId, int type, String name, int pageNum, int pageSize);
    AjaxObject deleteByActiveId(String activeId);
    ActiveIndex findByActiveId(String activeId);
    public AjaxObject saveOrUpdate(JSONObject active, String groupToId);
    public List<ActiveIndexConfigCondition> convertJsonToCondditionNew(JSONArray condition, String configId, Integer level, Integer isTemp);
    public AjaxObject comConditonToPackaging(String projectId, String activeid, String taskId, Integer isSearch);
    public AjaxObject comConditonToWebUI(String projectId, String activeid);

    List<ActiveIndex> findActiveIdByProject(String projectId, Integer type);

    public AjaxObject findByProjectIdAndTypeNoPage(String activeId, String projectId, Integer type, String s, String name, String isTwiceIndex);
    public AjaxObject findDepRelation(String activeId, String taskId, Integer isSearch);

    public List<ActiveIndex> findeByProjectAndType(String projectId, int type);
    public List<String> findRefActiveIdByConfigId(List<String> configIds);
    public List<ActiveIndexConfigCondition> dependenceCurActive(String activeId);
    public boolean conditioIsnChange(JSONObject active);
    public List<ActiveIndex> findeByActiveName(Map<String, Object> param);
    public List<String> getRefActiveIds(List<ActiveIndexConfig> configs);
    public AjaxObject judgeActiveDependence(String activeId);
    public boolean judgeLoopDep(List<ActiveIndexConfig> configs, String currentActiveId, StringBuffer depRelations);

    List<ActiveIndex> getActiveIndexByProjectId(String uid, String projectId);

    AjaxObject getContrastiveActive(String uid, String projectId,Integer cortType);
    /**
     * 修改指标或者事件名称
     * @param params
     */
    boolean editActiveName(Map<String, Object> params);

    AjaxObject getAllResearchVariable(String createId, String projectId, Integer cortType, String uid);
    public List<ActiveIndexConfigCondition> findByRefActiveId(String refActiveId);

    List<ActiveIndex> dependenceCurActiveByIsTmp(String activeId);

    String getindexType(String id);

    List<String> getActiveName(String id);
}
