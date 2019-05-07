package com.gennlife.rws.entity;

import com.alibaba.fastjson.JSONArray;

import java.util.Date;
import java.util.List;

public class Project {

	private Integer id;

	private String projectId;

	private String projectName;

	private Date startTime;

	private Date endTime;

	private String headId;

	private String headName;

	private String cooperIs;

	private String cooperId;

	private String cooperName;

	private String cooperHeadId;

	private String cooperHeadName;

	private String isDelete;

	private String creatorId;

	private String creatorName;

	private Date creatorTime;

	private String modifyId;

	private String modifyName;

	private Date modifyTime;

	private String dataSource;

	private String crfId;

	private String projectdesc;

	private String scientificId;

	private String scientificName;

	private Integer proMemberNum;

	private Integer patientsNum;

	private List<OperLogs> operLogsList;

	private List<ProjectMember> projectMemberList;

	private List<PatientsSet> patientsSetList;

	private List<ActiveIndex> activeIndices;

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

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName == null ? null : projectName.trim();
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getHeadId() {
		return headId;
	}

	public void setHeadId(String headId) {
		this.headId = headId == null ? null : headId.trim();
	}

	public String getHeadName() {
		return headName;
	}

	public void setHeadName(String headName) {
		this.headName = headName == null ? null : headName.trim();
	}

	public String getCooperIs() {
		return cooperIs;
	}

	public void setCooperIs(String cooperIs) {
		this.cooperIs = cooperIs == null ? null : cooperIs.trim();
	}

	public String getCooperId() {
		return cooperId;
	}

	public void setCooperId(String cooperId) {
		this.cooperId = cooperId == null ? null : cooperId.trim();
	}

	public String getCooperName() {
		return cooperName;
	}

	public void setCooperName(String cooperName) {
		this.cooperName = cooperName == null ? null : cooperName.trim();
	}

	public String getCooperHeadId() {
		return cooperHeadId;
	}

	public void setCooperHeadId(String cooperHeadId) {
		this.cooperHeadId = cooperHeadId == null ? null : cooperHeadId.trim();
	}

	public String getCooperHeadName() {
		return cooperHeadName;
	}

	public void setCooperHeadName(String cooperHeadName) {
		this.cooperHeadName = cooperHeadName == null ? null : cooperHeadName.trim();
	}

	public String getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(String isDelete) {
		this.isDelete = isDelete == null ? null : isDelete.trim();
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId == null ? null : creatorId.trim();
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName == null ? null : creatorName.trim();
	}

	public Date getCreatorTime() {
		return creatorTime;
	}

	public void setCreatorTime(Date creatorTime) {
		this.creatorTime = creatorTime;
	}

	public String getModifyId() {
		return modifyId;
	}

	public void setModifyId(String modifyId) {
		this.modifyId = modifyId == null ? null : modifyId.trim();
	}

	public String getModifyName() {
		return modifyName;
	}

	public void setModifyName(String modifyName) {
		this.modifyName = modifyName == null ? null : modifyName.trim();
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource == null ? null : dataSource.trim();
	}

	public String getCrfId() {
		return crfId;
	}

	public void setCrfId(String crfId) {
		this.crfId = crfId == null ? null : crfId.trim();
	}

	public String getProjectdesc() {
		return projectdesc;
	}

	public void setProjectdesc(String projectdesc) {
		this.projectdesc = projectdesc == null ? null : projectdesc.trim();
	}

	public String getScientificName() {
		return scientificName;
	}

	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	public List<OperLogs> getOperLogsList() {
		return operLogsList;
	}

	public void setOperLogsList(List<OperLogs> operLogsList) {
		this.operLogsList = operLogsList;
	}

	public List<ProjectMember> getProjectMemberList() {
		return projectMemberList;
	}

	public void setProjectMemberList(List<ProjectMember> projectMemberList) {
		this.projectMemberList = projectMemberList;
	}

	public List<PatientsSet> getPatientsSetList() {
		return patientsSetList;
	}

	public void setPatientsSetList(List<PatientsSet> patientsSetList) {
		this.patientsSetList = patientsSetList;
	}

	public Integer getProMemberNum() {
		return proMemberNum;
	}

	public void setProMemberNum(Integer proMemberNum) {
		this.proMemberNum = proMemberNum;
	}

	public Integer getPatientsNum() {
		return patientsNum;
	}

	public void setPatientsNum(Integer patientsNum) {
		this.patientsNum = patientsNum;
	}

	public String getScientificId() {
		return scientificId;
	}

	public void setScientificId(String scientificId) {
		this.scientificId = scientificId;
	}

	public List<ActiveIndex> getActiveIndices() {
		return activeIndices;
	}

	public void setActiveIndices(List<ActiveIndex> activeIndices) {
		this.activeIndices = activeIndices;
	}
}