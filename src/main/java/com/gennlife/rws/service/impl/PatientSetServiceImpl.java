/**
 * 
 */
package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.darren.collection.keypath.KeyPath;
import com.gennlife.exception.CustomerException;
import com.gennlife.exception.CustomerStatusEnum;
import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.dao.*;
import com.gennlife.rws.entity.*;
import com.gennlife.rws.query.UqlQureyResult;
import com.gennlife.rws.service.*;
import com.gennlife.rws.util.*;
import com.gennlife.rws.vo.DataCheckEmpty;
import com.gennlife.rws.vo.DelFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(rollbackFor = RuntimeException.class)
public class PatientSetServiceImpl implements PatientSetService {
	private static final Logger LOG = LoggerFactory.getLogger(PatientSetServiceImpl.class);

	@Autowired
	private PatientsSetMapper patientsSetMapper;
	@Autowired
	private SearchLogMapper searchLogMapper;
	@Autowired
	private ContrastiveAnalysisCountMapper conAnlyCountMapper;
	@Autowired
	private LogUtil logUtil;
	@Autowired
	private ActiveIndexService activeIndexService;
	@Autowired
	private GroupPatientDataMapper groupPatientDataMapper;
	@Autowired
	private GroupDataMapper groupDataMapper;
	@Autowired
	private HttpUtils httpUtils;
	@Autowired
	private GroupMapper groupMapper;
	@Autowired
	private SearchByuqlService searchByuqlService;
    @Autowired
    private InputTaskMapper inputTaskMapper;
    @Autowired
    private SearchCrfByuqlService searchCrfByuqlService;

    private static final int exportMax = 2000;

	@Override
	public List<PatientsSet> getPatientSetList(JSONObject obj) throws IOException {
		Map<String, Object> map = new HashMap<>();
		String crfId = obj.getString("crfId");
		String projectId = obj.getString("projectId");
		map.put("projectId", projectId);
		DataCheckEmpty.dataCheckEmpty(obj.getString("projectId"));
		List<PatientsSet> patientsSetList = patientsSetMapper.getPatientsSetList(map);
		for (PatientsSet patientsSet : patientsSetList){
			String patientSetId = patientsSet.getPatientsSetId();
			long count =getPatientSqlCount(patientSetId,projectId,crfId);
			patientsSet.setPatientsCount(count);
			patientsSetMapper.updatePatientsCountByPateintSetId(patientSetId,count);
			Integer isFlush = patientsSet.getIsFlush() == null ? 0 : patientsSet.getIsFlush(); //1 是刷新并置为0 0是不刷新
			if(isFlush != null && isFlush==0){
				continue;
			}else {
				patientsSetMapper.updateIsFlush(--isFlush,patientSetId);
			}
			//自动更新下面组的筛选功能
			SingleExecutorService.getInstance().getFlushCountGroupExecutor().submit(() -> {
				try {
					flushCountGroup(patientSetId,projectId,crfId);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			});
		}
		return patientsSetList;
	}

    @Override
	public PatientsSet getPatientSet(JSONObject obj) {
		String patientsSetId = obj.getString("patientsSetId");
		DataCheckEmpty.dataCheckEmpty(patientsSetId);
		return patientsSetMapper.selectByPatSetId(patientsSetId);
	}
	@Override
	public PatientsSet savePatientSet(JSONObject obj) {
		PatientsSet patSet = JSONObject.toJavaObject(obj, PatientsSet.class);
		DataCheckEmpty.dataCheckEmpty(patSet.getPatientsSetName());
		patSet.setPatientsSetId(UUIDUtil.getUUID());
		patSet.setIsDelete(DelFlag.AVIABLE.toString());// 枚举值
		// 服务器时间
		patSet.setCreateTime(new Date());
		patSet.setUpdateTime(new Date());
		patientsSetMapper.insert(patSet);
		String content = patSet.getCreateName() + "新增了患者集： " + patSet.getPatientsSetName();
		logUtil.saveLog(patSet.getProjectId(), content, patSet.getCreateId(), patSet.getCreateName());
		return patSet;
	}

    @Override
	public PatientsSet updatePatientSet(JSONObject obj) {
		PatientsSet patSet = JSONObject.toJavaObject(obj, PatientsSet.class);
		PatientsSet patSet1 = patientsSetMapper.selectByPatSetId(patSet.getPatientsSetId());
		String oldName = patSet1.getPatientsSetName();
		if (patSet1 != null) {
			patSet.setId(patSet1.getId());
			patSet.setUpdateTime(new Date());
			if(patSet.getPatientsCount()==0){
				patSet.setPatientsCount(null);
			}
			patientsSetMapper.updateById(patSet);
			String content = patSet.getUpdateName() + "编辑了患者集： " + oldName;
			logUtil.saveLog(patSet.getProjectId(), content, patSet.getCreateId(), patSet.getCreateName());
			//更新 任务中 患者集名称
            Map<String,String> inputMaps = new HashMap<>();
            inputMaps.put("patientSetId",patSet.getPatientsSetId());
            inputMaps.put("patientSetName",patSet.getPatientsSetName());
            inputTaskMapper.updateInputTaskByMap(inputMaps);
		} else {
			throw new CustomerException(CustomerStatusEnum.UNKONW_ERROR.toString(), "该患者集不存在无法更新");
		}
		return patSet;
	}
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjectMapper projectMapper;
	@Override
	public void deletePatientSet(JSONObject obj) throws IOException {
		String patientsSetId = obj.getString("patientsSetId");
		DataCheckEmpty.dataCheckEmpty(patientsSetId);
		PatientsSet patSet = patientsSetMapper.selectByPatSetId(patientsSetId);
		String createId = obj.getString("createId");
		String crfId = obj.getString("crfId");
		String createName = obj.getString("createName");
		String projectId = patSet.getProjectId();
		String projectName = "";
		if(StringUtils.isNotEmpty(projectId)){
			projectName = projectMapper.getProjectNameByProjectId(projectId);
		}
		if (patSet != null) {
			patSet.setUpdateTime(new Date());
			patSet.setIsDelete(DelFlag.LOSE.toString());
			// 删除患者集和分组的关系
			List<GroupPatientData> listGpData = groupPatientDataMapper.selectByPatientSetId(patientsSetId);
			if (listGpData != null && listGpData.size() > 0) {
				for (GroupPatientData gpdata : listGpData) {
					// 可考虑批量删除
					groupPatientDataMapper.deleteById(gpdata.getId());
					List<String> patSns = getPatientSetSn(patientsSetId,patSet.getProjectId(),crfId);
					List<String> otherPatSns = getGroupIdPatientSn(gpdata.getGroupId(),patSet.getProjectId(),crfId);
					patSns.removeAll(otherPatSns);
					if(patSns !=null && patSns.size()>0){
						groupDataMapper.deleteByPatSn(gpdata.getGroupId(),patSns);
						//若 group ID 有子组  同时删掉数据
						deleteGroupData(gpdata.getGroupId(),patSns);
					}

				}
			}
			patientsSetMapper.updateById(patSet);
			String content = createName + "删除了患者集： " + patSet.getPatientsSetName();
			logUtil.saveLog(patSet.getProjectId(), content, createId, createName);

			//患者集删除 任务进行失败更改
			if(StringUtils.isNotEmpty(projectId) && StringUtils.isNotEmpty(patientsSetId) && StringUtils.isNotEmpty(createId)){
				inputTaskService.cencelInputTasksOnDelPatSet(patientsSetId,createId,projectId,projectName,crfId);
			}

		}

	}
	@Autowired
	private InputTaskService inputTaskService;
	@Override
	public List<PatientsSet> getPatientSetByProjectId(JSONObject paramObj) {
		String projectId = paramObj.getString("projectId");
		DataCheckEmpty.dataCheckEmpty(projectId);
		return patientsSetMapper.getPatientSetByProjectId(projectId);

	}
	@Override
	public AjaxObject getPatientSetForList(JSONObject params) {
		// 根据患者集ID查询患者数据列表
		try {
			String patientsSetId = params.getString("patientsSetId");
			String name = params.getString("name");
			Integer type = params.getInteger("type");
			Integer pageNum = params.getInteger("pageNum");
			Integer pageSize = params.getInteger("pageSize");
			if (StringUtils.isEmpty(patientsSetId) || type == null || pageNum == null || pageSize == null) {
				throw new CustomerException(CustomerStatusEnum.PARAMISNULL);
			}
			AjaxObject object = activeIndexService.findByProjectId(patientsSetId, type, name, pageNum, pageSize);
			return object;
		} catch (Exception e) {
			LOG.error("查询患者集信息失败:" + params + "失败原因如下：" + e.getMessage());
			throw new CustomerException(CustomerStatusEnum.UNKONW_ERROR.toString(), e.getMessage());
		}
	}

	@Override
	public void savePatientImport(JSONObject obj) throws IOException {
		Integer count = patientsSetMapper.getPatientSetCount(obj.getString("patientSetId"));
		Integer allCount = getPatientSqlCount(obj.getString("patientSetId"),obj.getString("projectId"),obj.getString("crfId"),obj.getString("uqlQuery"));
		Long currentCount = obj.getLong("curenntCount");
		PatientsSet patientsSet = new PatientsSet();
		patientsSet.setPatientsSetId(obj.getString("patientSetId"));
		patientsSet.setPatientsCount(Long.valueOf(allCount)+currentCount);
		patientsSet.addComUqlQuery(obj.getString("uqlQuery"));
		patientsSet.setIsFlush(5);//让相关患者分组进行更新
		if(count ==0){
			patientsSetMapper.insert(patientsSet);
		}else {
			patientsSetMapper.updatePatientsCountAndQuery(obj.getString("patientSetId"),Long.valueOf(allCount), GzipUtil.compress(obj.getString("uqlQuery")),5);
		}
	}

	@Override
	public List<ContrastiveAnalysisCount> getContrasAnalyList(JSONObject obj) {
		try {
			String uid = obj.getString("uid");
			String projectId = obj.getString("projectId");
			DataCheckEmpty.dataCheckEmpty(uid, projectId);
			return conAnlyCountMapper.getContrastiveByUidAndPro(uid, projectId);
		} catch (Exception e) {
			throw new CustomerException(CustomerStatusEnum.UNKONW_ERROR.toString(), e.getMessage());
		}
	}
	@Override
	public List<SearchLog> getSearchLog(JSONObject param) {
		String patientsSetId = param.getString("patientsSetId");
		DataCheckEmpty.dataCheckEmpty(patientsSetId);
		return searchLogMapper.selectByPrtisntId(patientsSetId);
	}


	private void flushCountGroup(String patientSetId,String projectId,String crfId) throws IOException, ExecutionException, InterruptedException {
		List<String> groupIds = groupPatientDataMapper.getGroupIds(patientSetId);
		for (String groupId : groupIds){
			List<String> patientSetIds = groupPatientDataMapper.getPatSetByGroupId(groupId);
			Group group = groupMapper.getGroupByGroupId(groupId);
			ActiveIndex active = null;
			try {
                active = activeIndexService.findByActiveId(groupId);
                if(active==null) savePatientToGroup(patientSetIds,projectId,crfId,groupId);
                active = active == null ? new ActiveIndex() : active;
                JSONObject obj = (JSONObject) JSONObject.toJSON(active);

                obj.put("patientSetId",patientSetIds);
                obj.put("groupToId",groupId);
                obj.put("groupFromId",null);
                if(StringUtils.isNotEmpty(crfId) && !crfId.equals("EMR")){
                    searchCrfByuqlService.SearchByExclude(obj, null,0,crfId);
                    searchCrfByuqlService.searchCalcExculeByUql(groupId,projectId,1,1,new JSONArray(),crfId,"1",groupId,group.getGroupName(),
                        JSONArray.parseArray(JSON.toJSONString(patientSetIds)),group.getCreateId(),group.getCreateName(),null,true);
                }else {
                    searchByuqlService.SearchByExclude(obj, null,0);
                    searchByuqlService.searchCalcExculeByUql(groupId,projectId,1,1,new JSONArray(),"1",groupId,group.getGroupName(),JSONArray.parseArray(JSON.toJSONString(patientSetIds)),group.getCreateId(),group.getCreateName(),null,true);
                }
            } catch (ExecutionException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

    private void savePatientToGroup(List<String> patientSns,String projectId,String crfId,String groupId) throws IOException {
        boolean isExport = false;
        List<List<GroupData>> list = new ArrayList<>();
        List<GroupData> listdata = new ArrayList<>();

        for (int i = 0; i < patientSns.size(); i++) {
            String patientSetId = patientSns.get(i);
            List<Patient> listPatients = searchByuqlService.getpatentByUql(patientSetId,isExport,projectId,crfId);
            for (int j = 0; j < listPatients.size(); j++) {
                Patient patient = listPatients.get(j);
                if(j != 0 &&  j % exportMax == 0  ){
                    list.add(listdata);
                    listdata = new ArrayList<>();
                }
                GroupData groupData = new GroupData();
                groupData.setPatientSetId(patientSetId);
                groupData.setGroupId(groupId);
                groupData.setPatientSn(patient.getPatientSn());
                groupData.setEfhnic(patient.getEfhnic());
                groupData.setNationality(patient.getNationality());
                groupData.setPatientDocId(patient.getDOC_ID());
                groupData.setMaritalStatus(patient.getMaritalStatus());
                groupData.setGender(patient.getGender());
                groupData.setRemove(DelFlag.AVIABLE.toString());
                groupData.setCreateTime(new Date());
                groupData.setUpdateTime(new Date());
                listdata.add(groupData);
            }
        }
        list.add(listdata);
        for (List<GroupData> li : list){
            if (li != null && !li.isEmpty()) {
                groupDataMapper.batchInsert(li);
            }
        }
    }

    private List<String> getPatientSetSn(String patientsSetId, String projectId,String crfId) throws IOException {
		String querWhere = TransPatientSql.getUncomPatientSnSql(patientsSetMapper.getPatientsetSql(patientsSetId));
		String newquerWhere = TransPatientSql.getAllPatientSql(querWhere,crfId);
		JSONArray sourceFilter = new JSONArray();
//		sourceFilter.add(IndexContent.getPatientInfoPatientSn(crfId));
		String newSql = "select "+IndexContent.getPatientDocId(crfId)+" as pSn from "+ IndexContent.getIndexName(crfId,projectId)+" where "+newquerWhere+IndexContent.getGroupBy(crfId);
		String response = httpUtils.querySearch(projectId,newSql,1,Integer.MAX_VALUE-1,null,sourceFilter,crfId,true);
		List<String> patients = new KeyPath("hits", "hits", "_id")
			.fuzzyResolve(JSON.parseObject(response))
			.stream()
			.map(String.class::cast)
			.collect(toList());
		return patients;
	}

	private List<String> getGroupIdPatientSn(String groupId,String projectId,String crfId) {
		List<String> patSetSql = patientsSetMapper.getPatientSetSqlByGroupId(groupId);
		String sqlWhere = String.join(" or ",patSetSql.stream().map(x -> "("+TransPatientSql.getPatientSnSql(TransPatientSql.getUncomPatientSnSql(x),crfId)+")").collect(toList()));
		JSONArray sourceFilter = new JSONArray();
//		sourceFilter.add(IndexContent.getPatientInfoPatientSn(crfId));
		String result = null;
		String newSql = "select "+IndexContent.getPatientDocId(crfId)+" as pSn from "+IndexContent.getIndexName(crfId,projectId)+" where "+sqlWhere+IndexContent.getGroupBy(crfId);
		String response = httpUtils.querySearch(projectId,newSql,1,Integer.MAX_VALUE-1,null,sourceFilter,crfId,true);
		List<String> patients = new KeyPath("hits", "hits", "_id")
			.fuzzyResolve(JSON.parseObject(response))
			.stream()
			.map(String.class::cast)
			.collect(toList());
		return patients;
	}

	private void deleteGroupData(String groupId,List<String> patSns) {
		List<Group> groupChildList = groupMapper.getgroupChildIds(groupId);
		for (Group group : groupChildList){

			groupDataMapper.deleteByPatSn(group.getGroupId(),patSns);
			deleteGroupData(group.getGroupId(),patSns);
		}
	}

	private Integer getPatientSqlCount(String patientSetId, String projectId,String crfId)  {
		String patientSetSql = TransPatientSql.getUncomPatientSnSql(patientsSetMapper.getPatientsetSql(patientSetId));
		if(StringUtils.isEmpty(patientSetSql)){
		    return 0;
        }
		String newpatientSetSql = TransPatientSql.getAllPatientSql(patientSetSql,crfId);
		JSONArray sourceFilter = new JSONArray();
		String newSql = "select  "+IndexContent.getPatientDocId(crfId)+" as pSn from "+ IndexContent.getIndexName(crfId,projectId)+" where "+newpatientSetSql + " and  join_field='patient_info'";
		String response = httpUtils.querySearch(projectId,newSql,1,1,null,sourceFilter,crfId,false);
		return UqlQureyResult.getTotal(response);
	}

	private Integer getPatientSqlCount(String patientSetId, String projectId,String crfId,String query)  {
		if(StringUtils.isEmpty(query)){
			return 0;
		}
		String newpatientSetSql = TransPatientSql.getAllPatientSql(query,crfId);
		JSONArray sourceFilter = new JSONArray();
		String newSql = "select  "+IndexContent.getPatientDocId(crfId)+" as pSn from "+ IndexContent.getIndexName(crfId,projectId)+" where "+newpatientSetSql + " and  join_field='patient_info'";
		String response = httpUtils.querySearch(projectId,newSql,1,1,null,sourceFilter,crfId,false);
		return UqlQureyResult.getTotal(response);
	}


}
