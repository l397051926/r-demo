package com.gennlife.rws.entity;

import java.util.Date;

public class GroupData {

	private Integer id;

	private String patientSetId;

	private String groupId;

	private String patientSn;

	private String createId;

	private String createName;

	private Date createTime;

	private String updateId;

	private String updateName;

	private Date updateTime;

	private String remove;

	private String patientDocId;
	private String efhnic;
	private String nationality;
	private String maritalStatus;
	private String gender;
	private String groupName;

	public String getPatientDocId() {
		return patientDocId;
	}

	public void setPatientDocId(String patientDocId) {
		this.patientDocId = patientDocId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getEfhnic() {
		return efhnic;
	}

	public void setEfhnic(String efhnic) {
		this.efhnic = efhnic;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public String getMaritalStatus() {
		return maritalStatus;
	}

	public void setMaritalStatus(String maritalStatus) {
		this.maritalStatus = maritalStatus;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId == null ? null : groupId.trim();
	}

	public String getPatientSn() {
		return patientSn;
	}

	public void setPatientSn(String patientSn) {
		this.patientSn = patientSn == null ? null : patientSn.trim();
	}

	public String getPatientSetId() {
		return patientSetId;
	}

	public void setPatientSetId(String patientSetId) {
		this.patientSetId = patientSetId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public String getRemove() {
		return remove;
	}

	public void setRemove(String remove) {
		this.remove = remove == null ? null : remove.trim();
	}
}