package com.gennlife.rws.entity;

import java.util.List;

/**
 * @author liumingxin
 * @create 2018 25 15:38
 * @desc
 **/
public class CortrastiveGroupView {
    private String title;
    private String dataIndex;
    private String key;
    private Integer groupNum;
    private List<CortrastiveGroupView> children;

    public CortrastiveGroupView() {
    }
    public CortrastiveGroupView(String title) {
        this.title = title;
    }

    public CortrastiveGroupView(String title, String dataIndex, String key) {
        this.title = title;
        this.dataIndex = dataIndex;
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(String dataIndex) {
        this.dataIndex = dataIndex;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getGroupNum() {
        return groupNum;
    }

    public void setGroupNum(Integer groupNum) {
        this.groupNum = groupNum;
    }

    public List<CortrastiveGroupView> getChildren() {
        return children;
    }

    public void setChildren(List<CortrastiveGroupView> children) {
        this.children = children;
    }
}
