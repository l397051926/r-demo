/**
 * copyRight
 */
package com.gennlife.rws.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by liuzhen.
 * Date: 2017/10/19
 * Time: 21:21
 */
public class ActiveIndexVo implements Serializable{
    private String id;
    //名称
    private String activeIndexName;
    //活动类型
    private String activeType;
    //活动结果或指标类型
    private String indexType;

    private Map<String,Object> dataMap;

    private Date createTime;

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    private String dataType;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    //依赖
    private List<Map<String,Object>> dependencies = new ArrayList<>();
    //被依赖
    private List<Map<String,Object>> dependenced = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActiveIndexName() {
        return activeIndexName;
    }

    public void setActiveIndexName(String activeIndexName) {
        this.activeIndexName = activeIndexName;
    }

    public String getActiveType() {
        return activeType;
    }

    public void setActiveType(String activeType) {
        this.activeType = activeType;
    }

    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    public List<Map<String, Object>> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Map<String, Object>> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependencies(Map<String,Object> param){
        dependencies.add(param);
    }
    public void addDependenced(Map<String,Object> param){
        dependenced.add(param);
    }
    public List<Map<String, Object>> getDependenced() {
        return dependenced;
    }

    public void setDependenced(List<Map<String, Object>> dependenced) {
        this.dependenced = dependenced;
    }
}
