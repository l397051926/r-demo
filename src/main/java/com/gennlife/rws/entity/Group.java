package com.gennlife.rws.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Group {

	private Integer id;

	private Integer groupNum;

	private String groupId;

	private String groupName;

	private String groupParentId;

	private Integer groupLevel;
	/** 001 病例组 002 对照组 */
	private String groupTypeId;

	private String groupTypeName;

	private String projectId;

	private String createId;

	private String createName;

	private Date createTime;

	private String updateId;

	private String updateName;

	private Date updateTime;

	private String groupDescribe;

	private String isDelete;

	private List<GroupData> gruopDatas;

	private List<Group> childGroup = new ArrayList<>();

	private Boolean checkable;

	private String querySearch;

	private List<String> plainOptions;

	private Integer existType;

	public Integer getExistType() {
		return existType;
	}

	public void setExistType(Integer existType) {
		this.existType = existType;
	}

	public String getQuerySearch() {
		return querySearch;
	}

	public void setQuerySearch(String querySearch) {
		this.querySearch = querySearch;
	}

	public Boolean getCheckable() {
		return checkable;
	}

	public void setCheckable(Boolean checkable) {
		this.checkable = checkable;
	}

	public List<Group> getChildGroup() {
		return childGroup;
	}

	public void setChildGroup(List<Group> childGroup) {
		this.childGroup = childGroup;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId == null ? null : groupId.trim();
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName == null ? null : groupName.trim();
	}

	public String getGroupParentId() {
		return groupParentId;
	}

	public void setGroupParentId(String groupParentId) {
		this.groupParentId = groupParentId == null ? null : groupParentId.trim();
	}

	public Integer getGroupLevel() {
		return groupLevel;
	}

	public void setGroupLevel(Integer groupLevel) {
		this.groupLevel = groupLevel;
	}

	public String getGroupTypeId() {
		return groupTypeId;
	}

	public void setGroupTypeId(String groupTypeId) {
		this.groupTypeId = groupTypeId == null ? null : groupTypeId.trim();
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId == null ? null : projectId.trim();
	}

	public String getCreateId() {
		return createId;
	}

	public void setCreateId(String createId) {
		this.createId = createId == null ? null : createId.trim();
	}

	public String getCreateName() {
		return createName;
	}

	public void setCreateName(String createName) {
		this.createName = createName == null ? null : createName.trim();
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getUpdateId() {
		return updateId;
	}

	public void setUpdateId(String updateId) {
		this.updateId = updateId == null ? null : updateId.trim();
	}

	public String getUpdateName() {
		return updateName;
	}

	public void setUpdateName(String updateName) {
		this.updateName = updateName == null ? null : updateName.trim();
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getGroupDescribe() {
		return groupDescribe;
	}

	public void setGroupDescribe(String groupDescribe) {
		this.groupDescribe = groupDescribe == null ? null : groupDescribe.trim();
	}

	public String getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(String isDelete) {
		this.isDelete = isDelete;
	}

	public List<GroupData> getGruopDatas() {
		return gruopDatas;
	}

	public void setGruopDatas(List<GroupData> gruopDatas) {
		this.gruopDatas = gruopDatas;
	}

	public String getGroupTypeName() {
		return groupTypeName;
	}

	public void setGroupTypeName(String groupTypeName) {
		this.groupTypeName = groupTypeName;
	}

	public Group() {
	}

	public Group(String groupTypeId, String groupTypeName,String groupName) {
		this.groupTypeId = groupTypeId;
		this.groupTypeName = groupTypeName;
		this.groupName = groupName;
	}

	public Integer getGroupNum() {
		return groupNum;
	}

	public void setGroupNum(Integer groupNum) {
		this.groupNum = groupNum;
	}

}