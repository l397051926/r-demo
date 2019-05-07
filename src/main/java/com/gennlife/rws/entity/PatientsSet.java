package com.gennlife.rws.entity;

import com.gennlife.rws.util.GzipUtil;

import java.io.IOException;
import java.util.Date;

public class PatientsSet {
	
	private Integer id;

	private String projectId;

	private String patientsSetId;

	private String patientsSetName;

	private Long patientsCount;

	private String searchConditionId;

	private String createId;

	private String createName;

	private Date createTime;

	private String updateId;

	private String updateName;

	private Date updateTime;

	private String patientsSetDescribe;

	private String isDelete;

	private String uqlQuery;

	private Integer isFlush;  //1 是进行刷新 0是不进行刷新

	public String getUqlQuery() {
		return uqlQuery;
	}

	public String putUncomUqlQuery() throws IOException {
		return GzipUtil.uncompress(uqlQuery);
	}

	public void setUqlQuery(String uqlQuery) {
		this.uqlQuery = uqlQuery;
	}
	public void addComUqlQuery(String uqlQuery) throws IOException {
		this.uqlQuery = GzipUtil.compress(uqlQuery);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId == null ? null : projectId.trim();
	}

	public String getPatientsSetId() {
		return patientsSetId;
	}

	public void setPatientsSetId(String patientsSetId) {
		this.patientsSetId = patientsSetId == null ? null : patientsSetId.trim();
	}

	public String getPatientsSetName() {
		return patientsSetName;
	}

	public void setPatientsSetName(String patientsSetName) {
		this.patientsSetName = patientsSetName == null ? null : patientsSetName.trim();
	}

	public Long getPatientsCount() {
		return patientsCount;
	}

	public void setPatientsCount(Long patientsCount) {
		this.patientsCount = patientsCount;
	}

	public String getSearchConditionId() {
		return searchConditionId;
	}

	public void setSearchConditionId(String searchConditionId) {
		this.searchConditionId = searchConditionId == null ? null : searchConditionId.trim();
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

	public String getPatientsSetDescribe() {
		return patientsSetDescribe;
	}

	public void setPatientsSetDescribe(String patientsSetDescribe) {
		this.patientsSetDescribe = patientsSetDescribe == null ? null : patientsSetDescribe.trim();
	}

	public String getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(String isDelete) {
		this.isDelete = isDelete;
	}

	public Integer getIsFlush() {
		return isFlush;
	}

	public void setIsFlush(Integer isFlush) {
		this.isFlush = isFlush;
	}

	@Override
	public String toString() {
		return "PatientsSet [id=" + id + ", projectId=" + projectId + ", patientsSetId=" + patientsSetId
				+ ", patientsSetName=" + patientsSetName + ", patientsCount=" + patientsCount + ", searchConditionId="
				+ searchConditionId + ", createId=" + createId + ", createName=" + createName + ", createTime="
				+ createTime + ", updateId=" + updateId + ", updateName=" + updateName + ", updateTime=" + updateTime
				+ ", patientsSetDescribe=" + patientsSetDescribe + ", isDelete=" + isDelete + "]";
	}

}