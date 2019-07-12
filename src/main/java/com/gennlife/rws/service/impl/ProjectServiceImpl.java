package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.content.CommonContent;
import com.gennlife.rws.dao.*;
import com.gennlife.rws.entity.*;
import com.gennlife.rws.exception.CustomerException;
import com.gennlife.rws.service.CortrastiveAnalysisService;
import com.gennlife.rws.service.GroupService;
import com.gennlife.rws.service.InputTaskService;
import com.gennlife.rws.service.ProjectService;
import com.gennlife.rws.util.*;
import com.gennlife.rws.vo.CustomerStatusEnum;
import com.gennlife.rws.vo.DataCheckEmpty;
import com.gennlife.rws.vo.DelFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.gennlife.rws.query.BuildIndexCrf.PROJECT_INDEX_NAME_PREFIX;
import static java.util.stream.Collectors.toList;

@Service
@Transactional(rollbackFor = RuntimeException.class)
public class ProjectServiceImpl implements ProjectService {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectServiceImpl.class);

	@Autowired
	private ProjectMapper projectMapper;
	@Autowired
	private LogUtil logUtil;
	@Autowired
	private OperLogsMapper operLogsMapper;
	@Autowired
	private ProjectMemberMapper projectMemberDao;
	@Autowired
	private PatientsSetMapper patientsSetMapper;
	@Autowired
	private ProjectUserMapMapper projectUserMapMapper;
	@Autowired
	private GroupMapper groupMapper;
	@Autowired
	private ProjectScientificMapMapper projectScientMapper;
	@Autowired
	private HttpUtils httpUtils;
	@Autowired
	private ActiveIndexMapper activeIndexMapper;
    @Autowired
    private InputTaskMapper inputTaskMapper;
	@Autowired
	private GroupDataMapper groupDataMapper;
	@Autowired
	private GroupService groupService;
	@Autowired
	private GroupConditionMapper groupConditionMapper;


    @Override
	public List<Project> getProjectList(JSONObject obj) {
		Map<String, Object> map = new HashMap<>();
		map.put("uid", obj.getString("uid"));
		DataCheckEmpty.dataCheckEmpty(obj.getString("uid"));
		List<Project> list = projectMapper.getProjectList(map);
		for (Project project : list) {
			// 获取成员信息 患者集 项目日志等信息
			getProjectDetail(project);
		}
		return list;

	}

	@Override
	public Project getProject(JSONObject obj) {
		String projectId = obj.getString("projectId");
		DataCheckEmpty.dataCheckEmpty(projectId);
		return projectMapper.selectByProjectId(projectId);
	}
	@Override
	public Project updateProject(JSONObject obj) {
		Project project = JSONObject.toJavaObject(obj, Project.class);
		DataCheckEmpty.dataCheckEmpty(project.getProjectId());
		Project project1 = projectMapper.selectByProjectId(project.getProjectId());
		if (project1 != null) {
			project.setId(project1.getId());
			project.setModifyTime(new Date());
			projectMapper.updateById(project);
			// 更新科研类型 如果科研类型不同则更新对应关系
			if (!StringUtils.isEmpty(project.getScientificId())
					&& !project.getScientificId().equals(project1.getScientificId())) {
				ProjectScientificMap projectSCienMap = projectScientMapper.selectByProjectId(project.getProjectId());
				if (projectSCienMap != null) {
					projectSCienMap.setScientificId(project.getScientificId());
					projectScientMapper.updateById(projectSCienMap);
				} else {
					throw new CustomerException(CustomerStatusEnum.UNKONW_ERROR.toString(), "科研类型不存在,无法更新！");
				}
			}
			// 修改 任务列表中项目名称
            Map<String,String> inputMaps = new HashMap<>();
			inputMaps.put("projectId",project.getProjectId());
			inputMaps.put("projectName",project.getProjectName());
			inputTaskMapper.updateInputTaskByMap(inputMaps);
		} else {
			LOG.error("项目：" + obj + "编辑失败,失败原因如下：不存在该项目!");
			throw new CustomerException(CustomerStatusEnum.UNKONW_ERROR.toString(), "项目不存在,无法更新！");
		}
		String content = project.getCreatorName() + "编辑了项目： " + project.getProjectName();
		logUtil.saveLog(project.getProjectId(), content, project.getCreatorId(), project.getCreatorName());
		return project;
	}
	@Autowired
	private InputTaskService inputTaskService;

	@Override
	public void deleteProject(JSONObject obj) {
		// 删除项目 删除项目和成员对照关系 患者分组 患者集 删除项目日志(暂不删除)
		String projectId = "";
		projectId = obj.getString("projectId");
		DataCheckEmpty.dataCheckEmpty(projectId);
		if (StringUtils.isEmpty(projectId)) {
			throw new CustomerException(CustomerStatusEnum.PARAMISNULL);
		}
		Project project = projectMapper.selectByProjectId(projectId);
		// 如果没有此数据也提示删除成功
		if (project != null) {
			project.setIsDelete(DelFlag.LOSE.toString());
			projectMapper.updateById(project);
			// 删除项目相关信息 可考虑异步处理
			deleteProjectDetail(projectId);
            deleteProjectIndex(project.getCrfId(),projectId);
            inputTaskService.cencelInputTasksOnDelProject(project.getCreatorId(),projectId,project.getProjectName(),project.getCrfId());
		}
		String content = project.getCreatorName() + "删除了项目： " + project.getProjectName();
		logUtil.saveLog(project.getProjectId(), content, project.getCreatorId(), project.getCreatorName());
	}

    @Override
    public void deleteProjectDelIndex() {
        List<Project> projects = projectMapper.getProjectByDelete(1);
        projects.forEach( project -> {
            try {
                Thread.sleep(100);
                deleteProjectIndex(project.getCrfId(),project.getProjectId());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

	/**
	 * 删除索引数据
	 * @param crfId  单病种id
	 * @param projectId 项目id
	 */
	@Override
	public void deleteProjectIndex(String crfId,String projectId) {
		String indexName = "";
		if(StringUtils.isEmpty(crfId) || "EMR".equals(crfId)){
			indexName = "rws_emr_"+projectId;
		}else {
			indexName = PROJECT_INDEX_NAME_PREFIX.get(crfId) +projectId;
		}
		JSONObject paramObj = new JSONObject();
		paramObj.put("indexName",indexName);
		String result = httpUtils.deleteIndex(paramObj.toJSONString());
		LOG.info("删除索引结果： "+ result +"projectId : "+ projectId);
	}

	@Override
	public Integer getCountByProjectIdAndProjectName(String projectId, String projectName) {
		return projectMapper.getCountByProjectIdAndProjectName(projectId,projectName);
	}
	@Autowired
	private CortrastiveAnalysisService cortrastiveAnalysisService;

	@Override
	public AjaxObject getCortastivePatientSn(JSONObject object) {
		String uid = object.getString("uid");
		String projectId = object.getString("projectId");
//		List<String> groupList = groupConditionMapper.getGroupIdByProjectIdAndUid(uid,projectId,2);
//		List<Group> groups = groupMapper.getGroupListByGroupIds(groupList);
		List<Group> groupList = cortrastiveAnalysisService.getCortastiveGroupList(uid,projectId);
		if (groupList == null || groupList.size() ==0){
			return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"没有符合的分组数据");
		}
		JSONObject data = new JSONObject();
		for (Group group : groupList){
			String groupId = group.getGroupId();
			List<String> patSns = groupDataMapper.getPatientSnList(groupId);
			String groupName = group.getGroupName();
			String groupPath = groupService.getGroupNamePath(groupId,groupName);
			data.put(groupPath,patSns);
		}
		AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,AjaxObject.AJAX_MESSAGE_SUCCESS);
		ajaxObject.setData(data);
		return ajaxObject;
	}

	@Override
	public List<Project> getProjectListByCrfId(JSONObject param) {

		String crfId = param.getString("crfId");
		String uid = param.getString("uid");
		DataCheckEmpty.dataCheckEmpty(crfId, uid);
		List<Project> projects = projectMapper.getProjectByUid(uid);
		List<Project> resultList = new ArrayList<>();
		for (Project project : projects) {
			if (crfId.equals(project.getCrfId()) || StringUtils.isEmpty(project.getCrfId())) {
				resultList.add(project);
				continue;
			}
			String projectId = project.getProjectId();
			Integer inputRunNum = inputTaskMapper.getWorkTaskByProjectId(projectId);
			if(inputRunNum>0){
				continue;
			}
			Integer allCount = patientsSetMapper.getSumCount(projectId);
			allCount  = allCount == null ? 0 :  allCount;
			Integer inputRunCount = inputTaskMapper.getRunTimeTaskByProjectId(projectId);
			inputRunCount = inputRunCount == null ? 0 : inputRunCount;
			if(allCount ==0 && inputRunCount ==0){
				resultList.add(project);
				continue;
			}
		}
		return resultList;

	}

	@Override
	public void saveDatasource(String projectId, String crfId, String crfName) {
		if (CommonContent.EMR_CRF_NAME.equals(crfName)) {
			projectMapper.saveDatasource(projectId, crfName, crfId);
		} else {
			projectMapper.saveDatasource(projectId, "单病种-" + crfName, crfId);
		}
	}

	@Override
	public List<OperLogs> getOperLogsList(JSONObject param) {
		String projectId = param.getString("projectId");
		int page = param.getInteger("page") - 1;
		int pageSize = param.getInteger("pageSize");
		if (page < 0 || pageSize < 0) {
			LOG.error("搜索 的参数为： page" + param.getInteger("page"), " pageSize: " + pageSize);
			throw new CustomerException(CustomerStatusEnum.PARAMISNULL);
		}
		DataCheckEmpty.dataCheckEmpty(projectId);
		Map<String, Object> mapParam = new HashMap<>();
		mapParam.put("projectId", projectId);
		mapParam.put("page", page * pageSize);
		mapParam.put("pageSize", pageSize);
		return operLogsMapper.getOperLogsList(mapParam);
	}

	@Override
	public int getOperLogsCount(JSONObject param) {
		String projectId = param.getString("projectId");
		return operLogsMapper.getOperLogsCount(projectId);
	}

	@Override
	public String checkNameType(JSONObject object) {
		String name = object.getString("name");
		String type = object.getString("type");
		String uid = object.getString("uid");
		String projectId = object.getString("projectId");
		DataCheckEmpty.dataCheckEmpty(name, type);
		Map<String, Object> map = new HashMap<>();
		map.put("name", name);
		map.put("type", type);
		map.put("uid",uid);
		map.put("projectId",projectId);
		// 根据不同type 检验不同表的名称增加不同的查询条件
		// type 参考枚举类 NameType
		int count = projectMapper.chengName(map);
		if (count != 0) {
			count = 1;
		} else {
			count = 0;
		}
		return String.valueOf(count);
	}

	@Override
	public AjaxObject getprojectAggregation(JSONObject object) {

		String projectId = object.getString("projectId");
		JSONObject variantAgg =  getVariantAggreagation(projectId);
		JSONObject patientSetAgg = getPatientSetAggregation(projectId);
		JSONArray data = new JSONArray()
			.fluentAdd(variantAgg)
			.fluentAdd(patientSetAgg);
		AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,AjaxObject.AJAX_MESSAGE_SUCCESS);
		ajaxObject.setData(data);
		return ajaxObject;
	}

	@Override
	public AjaxObject projectPowerExamine(JSONObject object) {
		String uid = object.getString("uid");
		String projectId = object.getString("projectId");
		String patientSetId = object.getString("patientSetId");
		String groupId = object.getString("groupId");
		if(StringUtils.isNotEmpty(uid) && StringUtils.isNotEmpty(projectId)){
			Integer count = projectUserMapMapper.selectCountByProjectIdAndUid(uid,projectId);
			Integer proCount = projectMapper.selectCountByProjectId(projectId);
			if(count == null || count <= 0 || proCount == null || proCount <= 0){
				return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"无此项目");
			}
		}
		if(StringUtils.isNotEmpty(patientSetId)){
			Integer count = patientsSetMapper.getPatientSetCount(patientSetId);
			if(count == null || count <= 0){
				return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"无此患者集");
			}
		}
		if(StringUtils.isNotEmpty(groupId)){
			Integer count = groupMapper.selectCountByGroupId(object.getString("groupId"));
			if(count == null || count <= 0){
				return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"无此分组");
			}
		}

		return new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,AjaxObject.AJAX_MESSAGE_SUCCESS);
	}

	@Override
	public Object getProjectAttribute(JSONObject object) {
		String userId = object.getString("userId");
		JSONArray projectId = object.getJSONArray("projectId");
		String projectName = object.getString("projectName");
        List<String> proIds = null;
		if(projectId != null){
            proIds = projectId.toJavaList(String.class);
        }
		List<Project> projects = projectMapper.getProjectAttribute(userId,proIds,projectName);
		for (Project project : projects){
			String id = project.getProjectId();
			List<ActiveIndex> activeIndices = activeIndexMapper.getClasActiveIdsNameAndIdsByProjectId(id);
//			List<ActiveIndex> activeIndices = activeIndexMapper.getClasActiveIdsNameAndIdsOnContCheck(id,userId,2);
			project.setActiveIndices(activeIndices);
		}
		JSONArray data = JSONArray.parseArray(JSON.toJSONString(projects));
		return new JSONObject().fluentPut("data",data);
	}
	@Override
	public Object eligible(JSONObject param) {
		String inputTaskId = param.getString("taskId");
		String projectId = param.getString("projectId");
		String uid = param.getString("uid");
        String patientSetId = "";
		if(StringUtils.isNotEmpty(inputTaskId)){
			InputTask inputTask = inputTaskMapper.getInputtaskByInputId(inputTaskId);
			if(inputTask != null ){
				uid = inputTask.getUid();
				projectId = inputTask.getProjectId();
                patientSetId = inputTask.getPatientSetId();
			}
		}

		Project project = projectMapper.selectByProjectId(projectId);
		if(project == null ){
			return  new JSONObject()
				.fluentPut("status",200)
				.fluentPut("message","操作成功")
				.fluentPut("code",1);
		}
		JSONObject data = new JSONObject().fluentPut("creatorName",project.getCreatorName()).fluentPut("crfId",project.getCrfId());
		Integer projectUserCount = projectUserMapMapper.selectCountByProjectIdAndUid(uid,projectId);
		if(projectUserCount<1){
			return  new JSONObject()
				.fluentPut("status",200)
				.fluentPut("message","操作成功")
				.fluentPut("code",2)
				.fluentPut("data",data);
		}
		Integer patientSetCount = StringUtils.isEmpty(patientSetId) ? 0 : patientsSetMapper.getPatientSetCount(patientSetId);
		if(StringUtils.isNotEmpty(patientSetId) && patientSetCount<1){
			return  new JSONObject()
				.fluentPut("status",200)
				.fluentPut("message","操作成功")
				.fluentPut("code",3)
				.fluentPut("data",data);
		}
		return  new JSONObject()
			.fluentPut("status",200)
			.fluentPut("message","操作成功")
			.fluentPut("code",0)
			.fluentPut("data",data);
	}

	private JSONObject getPatientSetAggregation(String projectId) {
		List<AggregationModel> groupAggregations = patientsSetMapper.getPatientSetAggreagation(projectId);
		Integer count = patientsSetMapper.getPatientSetCountByProjectId(projectId);
		JSONArray domainData = JSONArray.parseArray(JSON.toJSONString(groupAggregations));
		JSONObject result = new JSONObject();
		result.put("domain_data",domainData);
		result.put("count",count);
		result.put("domain_desc","患者集");
		return result;

	}

	private JSONObject getVariantAggreagation(String projectId) {
		List<AggregationModel> groupAggregation = activeIndexMapper.getActiveVariantAggregation(projectId);
		Integer count = activeIndexMapper.getVariantAggregationCount(projectId);
		JSONObject result = new JSONObject();
		JSONArray domainData = JSONArray.parseArray(JSON.toJSONString(groupAggregation));
		result.put("domain_data",domainData);
		result.put("count",count);
		result.put("domain_desc","研究变量");
		return result;
	}

	/**
	 * 删除项目相关的 患者分组和患者集对照 患者分组和患者
	 */
	private void deleteProjectDetail(String projectId) {
		// 删除项目成员对照信息
		List<ProjectUserMap> listProUserMap = projectUserMapMapper.selectByProjectId(projectId);
		if (listProUserMap != null && !listProUserMap.isEmpty()) {
			projectUserMapMapper.deleteByProjectId(projectId);
		}
		// 删除患者集信息
		List<PatientsSet> listPatients = patientsSetMapper.getPatientSetByProjectId(projectId);
		if (listPatients != null && !listPatients.isEmpty()) {
			patientsSetMapper.deleteByProjectID(projectId);
		}
		// 删除分组信息
		Map<String, Object> map = new HashMap<>();
		map.put("projectId", projectId);
		List<Group> listGroup = groupMapper.getGroupList(map);
		if (listGroup != null && !listGroup.isEmpty()) {
			groupMapper.deleteByProjectId(projectId);
		}
		// 删除项目科研类型对照表
		ProjectScientificMap proSciMap = projectScientMapper.selectByProjectId(projectId);
		if (proSciMap != null) {
			projectScientMapper.deleteByProjectId(projectId);
		}

	}
	// 事务控制一致性
	@Transactional
	@Override
	public Project saveProject(JSONObject obj) {
		Project project = JSONObject.toJavaObject(obj, Project.class);
		if (project.getStartTime() == null || project.getEndTime() == null) {
			throw new CustomerException(CustomerStatusEnum.PARAMISNULL);
		}
		// 科研类型 项目名称 合作负责人编码/名称
		DataCheckEmpty.dataCheckEmpty(project.getScientificId(), project.getProjectName());
		project.setProjectId(UUIDUtil.getUUID());
		project.setIsDelete(DelFlag.AVIABLE.toString());
		project.setCreatorTime(new Date());
		project.setModifyTime(new Date());
		projectMapper.insert(project);
		// 项目和科研类型对应表 扩展
		ProjectScientificMap projectSCienMap = new ProjectScientificMap();
		projectSCienMap.setProjectId(project.getProjectId());
		projectSCienMap.setScientificId(project.getScientificId());
		projectScientMapper.insert(projectSCienMap);
		String content = project.getCreatorName() + "创建了项目： " + project.getProjectName();
		logUtil.saveLog(project.getProjectId(), content, project.getCreatorId(), project.getCreatorName());
		return project;

	}

	/** 根据项目ID 获取该项目下的 成员信息 患者集 项目日志等信息 */
	private void getProjectDetail(Project project) {
		List<ProjectMember> listMember = projectMemberDao.getUserProjectList(project.getProjectId());
		project.setProMemberNum(0);
		// 获取成员信息
		if (listMember != null && !listMember.isEmpty()) {
			project.setProjectMemberList(listMember);
			project.setProMemberNum(listMember.size());
		}
		// 获取患者集信息
		List<PatientsSet> listPatients = patientsSetMapper.getPatientSetByProjectIdRemoveQuery(project.getProjectId());
		project.setPatientsNum(0);
		if (listPatients != null && !listPatients.isEmpty()) {
			project.setPatientsSetList(listPatients);
			project.setPatientsNum(listPatients.size());
		}
		// 获取项目日志
		List<OperLogs> listOperLogs = operLogsMapper.getOperLogsListBy2(project.getProjectId());
		if (listOperLogs != null && !listOperLogs.isEmpty()) {
			project.setOperLogsList(listOperLogs);
		}
	}

}
