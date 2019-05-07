/**
 * 
 */
package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.exception.CustomerException;
import com.gennlife.exception.CustomerStatusEnum;
import com.gennlife.rws.dao.*;
import com.gennlife.rws.entity.*;
import com.gennlife.rws.rocketmq.ProducerService;
import com.gennlife.rws.rocketmq.RocketMqContent;
import com.gennlife.rws.service.ProjectMemberService;
import com.gennlife.rws.util.LogUtil;
import com.gennlife.rws.util.StringUtils;
import com.gennlife.rws.vo.DataCheckEmpty;
import com.gennlife.rws.vo.DelFlag;
import com.gennlife.rws.vo.ObligEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = RuntimeException.class)
public class ProjectMemberServiceImpl implements ProjectMemberService {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectMemberServiceImpl.class);

	@Autowired
	private GroupMapper groupMapper;
	@Autowired
	private GroupConditionMapper groupConditionMapper;
	@Autowired
	private ProjectMemberMapper projectMemberDao;
	@Autowired
	private ProjectUserMapMapper proUserMapperDao;
	@Autowired
	private LogUtil logUtil;
	@Autowired
	private ProjectMapper projectMapper;

	@Override
	public List<ProjectMember> getProjectMemberList(JSONObject obj) {
		String projectid = obj.getString("projectId");
		int page = obj.getInteger("page") - 1;
		int pageSize = obj.getInteger("pageSize");
		if (page < 0 || pageSize < 0) {
			LOG.error("搜索 的参数为： page" + obj.getInteger("page"), " pageSize: " + pageSize);
			throw new CustomerException(CustomerStatusEnum.PARAMISNULL);
		}
		Map<String, Object> mapParam = new HashMap<>();
		mapParam.put("projectId", projectid);
		mapParam.put("page", pageSize * page);
		mapParam.put("pageSize", pageSize);
		DataCheckEmpty.dataCheckEmpty(projectid);
		List<ProjectMember> list = projectMemberDao.getUserProjectListByLimit(mapParam);
		return list;
	}

	@Override
	public ProjectMember getProjectMember(JSONObject obj) {
		Integer memberId = obj.getInteger("memberId");
		if (memberId == null) {
			throw new CustomerException(CustomerStatusEnum.PARAMISNULL);
		}
		return projectMemberDao.selectById(memberId);

	}

	@Override
	public ProjectMember updateProjectMember(JSONObject obj) {
		String projectId = obj.getString("projectId");
		DataCheckEmpty.dataCheckEmpty(projectId);
		ProjectMember pm = JSONObject.toJavaObject(JSONObject.parseObject(obj.getString("data")), ProjectMember.class);
		ProjectMember pm1 = projectMemberDao.selectById(pm.getId());
		String cooperIs =projectMapper.getCooperIsByProjectId(projectId);
		Integer countPrincipal = projectMemberDao.getCountPrincipal(projectId,pm.getObligId());
		if(("1".equals(cooperIs) && "002".equals(pm.getObligId()) && countPrincipal>=2 )|| ( "0".equals(cooperIs) && "002".equals(pm.getObligId()) && countPrincipal >=1 ) ){
			LOG.info("projectId: "+projectId + "项目人数超标");
			throw new CustomerException(CustomerStatusEnum.UNKONW_ERROR.toString(),"负责人超过上限，无法更改");
		}
		if (pm1 != null) {
			pm.setId(pm1.getId());
			pm.setUpdateTime(new Date());
			projectMemberDao.updateById(pm);
			ProjectUserMap projectUserMap = new ProjectUserMap();
			projectUserMap.setProjectId(projectId);
			projectUserMap.setUid(pm1.getUid());
			projectUserMap.setObligId(pm.getObligId());
			proUserMapperDao.updateByProjectIdAndUid(projectUserMap);
			String content = pm.getUpdateName() + "编辑了项目成员：" + pm.getUname();
			logUtil.saveLog(projectId, content, pm.getCreateId(), pm.getCreateName());
			return pm;
		} else {
			LOG.error("项目成员：" + obj + "编辑失败,失败原因如下：不存在该项目成员!");
			throw new CustomerException(CustomerStatusEnum.UNKONW_ERROR.toString(), "项目成员不存在,无法更新！");
		}

	}
	@Autowired
	private ContrastiveAnalysisActiveMapper contrastiveAnalysisActiveMapper;
	@Override
	public void deleteProjectMember(JSONObject obj) {
		String createId = obj.getString("createId");
		String createName = obj.getString("createName");
		String projectId = obj.getString("projectId");
		Project project = projectMapper.selectByProjectId(projectId);
		// 该成员创建的患者集、研究变量、组等不会删除，也不会影响图形展示区中成员创建情况
		Integer memberId = obj.getInteger("memberId");
		if (memberId == null) {
			throw new CustomerException(CustomerStatusEnum.PARAMISNULL);
		}
		ProjectMember pm = projectMemberDao.selectById(memberId);
		if (pm != null) {
			// 如果存在成员没有的情况 对应关系存在的情况会出现bug
//			ProjectUserMap proUse = proUserMapperDao.selectByUidAndProjectId(projectId,pm.getUid());
//			if (proUse != null) {
//				proUserMapperDao.deleteById(proUse.getId());
//			}
			proUserMapperDao.deleteByUidProjectId(projectId,pm.getUid());
			contrastiveAnalysisActiveMapper.deleteByUidAndProId(projectId,pm.getUid());
			groupConditionMapper.deleteByproIdAndUid(projectId,pm.getUid());
			//同时删除 对比分析条件组的 id

		}
		producerService.romoveProMember(pm.getUid(),project.getCreatorName(),project.getProjectName());
		//传递 rocketMq 一条 项目消息  移除一名成员
		String content = createName + "删除了项目成员：" + pm.getUname();
		logUtil.saveLog(projectId, content, createId, createName);

	}

	@Override
	public int getProjectMemberCount(JSONObject object) {
		String projectId = object.getString("projectId");
		return projectMemberDao.getUserProjectCount(projectId);
	}

	@Transactional
	@Override
	public List<ProjectMember> saveProjectMember(JSONObject obj) {
		// 保存对应关系 将用户 信息集合转化为项目成员进行保存 事务控制 注意控制方式
		String projectId = obj.getString("projectId");
		Project project = projectMapper.selectByProjectId(projectId);
		String proCreateId = project.getCreatorId();
		DataCheckEmpty.dataCheckEmpty(projectId);
		List<ProjectMember> listUser = JSONObject.parseArray(obj.getString("data"), ProjectMember.class);
		for (ProjectMember pm : listUser) {
			pm.setIsDelete(DelFlag.AVIABLE.toString());
			if (StringUtils.isEmpty(pm.getObligId())) {
				// 成员默认职责均为参与人
				pm.setObligId(ObligEnum.PARTICIPANTS.toString());
			}
			pm.setCreateTime(new Date());
			pm.setUpdateTime(new Date());
			ProjectUserMap proUse = new ProjectUserMap();
			proUse.setProjectId(projectId);
			proUse.setUid(pm.getUid());
			proUse.setObligId(pm.getObligId());
			proUse.setCreateTime(pm.getCreateTime());
			// 增加 成员和分组数据关系
			saveGroupCondition(pm.getUid(), projectId);
			if(!proCreateId.equals(pm.getUid())){
				producerService.sendAddProMember(pm.getUid(),project.getCreatorName(),project.getProjectName(),projectId);
			}
			// 可改为批量插入或更新
			projectMemberDao.insert(pm);
			proUserMapperDao.insert(proUse);
			// 增加日志
			String content = pm.getCreateName() + "新增了项目成员：" + pm.getUname();
			logUtil.saveLog(projectId, content, pm.getCreateId(), pm.getCreateName());
		}
		return listUser;

	}

	@Autowired
	private ProducerService producerService;
	@Autowired
	private RocketMqContent rocketMqContent;

	private void saveGroupCondition(String uid, String projectId) {
		Map<String, Object> map = new HashMap<>();
		map.put("projectId", projectId);
		List<Group> groupList = groupMapper.getGroupList(map);
		for (Group group : groupList) {
			GroupCondition groupCondition = new GroupCondition();
			groupCondition.setGroupId(group.getGroupId());
			groupCondition.setProjectId(projectId);
			groupCondition.setUid(projectId);
			groupCondition.setCreateId(uid);
			groupCondition.setCreateTime(new Date());
			groupConditionMapper.insert(groupCondition);
		}

	}

}
