package com.gennlife.rws.entity;

import java.util.Date;

public class ProjectMember {
	private Integer id;

	private String uid;

	private String uname;

	private String unumber;

	private String labId;

	private String labName;

	private String orgId;

	private String orgName;
	// 职责人编码
	private String obligId;
	// 职责人名称 对照表中存储
	private String obligName;

	private String isDelete;

	private String createId;

	private String createName;

	private Date createTime;

	private String updateId;

	private String updateName;

	private Date updateTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid == null ? null : uid.trim();
	}

	public String getUname() {
		return uname;
	}

	public void setUname(String uname) {
		this.uname = uname == null ? null : uname.trim();
	}

	public String getUnumber() {
		return unumber;
	}

	public void setUnumber(String unumber) {
		this.unumber = unumber == null ? null : unumber.trim();
	}

	public String getLabId() {
		return labId;
	}

	public void setLabId(String labId) {
		this.labId = labId == null ? null : labId.trim();
	}

	public String getLabName() {
		return labName;
	}

	public void setLabName(String labName) {
		this.labName = labName == null ? null : labName.trim();
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId == null ? null : orgId.trim();
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName == null ? null : orgName.trim();
	}

	public String getObligId() {
		return obligId;
	}

	public void setObligId(String obligId) {
		this.obligId = obligId == null ? null : obligId.trim();
	}

	public String getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(String isDelete) {
		this.isDelete = isDelete == null ? null : isDelete.trim();
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

	public String getObligName() {
		return obligName;
	}

	public void setObligName(String obligName) {
		this.obligName = obligName;
	}

	@Override
	public String toString() {
		return "ProjectMember [id=" + id + ", uid=" + uid + ", uname=" + uname + ", unumber=" + unumber + ", labId="
				+ labId + ", labName=" + labName + ", orgId=" + orgId + ", orgName=" + orgName + ", obligId=" + obligId
				+ ", obligName=" + obligName + ", isDelete=" + isDelete + ", createId=" + createId + ", createName="
				+ createName + ", createTime=" + createTime + ", updateId=" + updateId + ", updateName=" + updateName
				+ ", updateTime=" + updateTime + "]";
	}

}