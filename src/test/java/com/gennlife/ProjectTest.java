//package com.gennlife;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.gennlife.rws.entity.*;
//import com.gennlife.rws.service.ActiveIndexService;
//import com.gennlife.rws.service.GroupService;
//import com.gennlife.rws.service.PatientGroupService;
//import com.gennlife.rws.service.PcientificResearchTypeService;
//import com.gennlife.rws.util.AjaxObject;
//import com.gennlife.rws.util.StringUtils;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import com.gennlife.rws.dao.GroupDataMapper;
//import com.gennlife.rws.dao.GroupMapper;
//import com.gennlife.rws.dao.OperLogsMapper;
//import com.gennlife.rws.dao.PatientsSetMapper;
//import com.gennlife.rws.dao.ProjectMapper;
//import com.gennlife.rws.dao.ProjectMemberMapper;
//import com.gennlife.rws.dao.ProjectScientificMapMapper;
//import com.gennlife.rws.dao.ProjectUserMapMapper;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@Ignore
//public class ProjectTest {
//	@Autowired
//	private ProjectMapper projectDao;
//	@Autowired
//	private ProjectMemberMapper projectMemberDao;
//	@Autowired
//	private PatientsSetMapper patientsSetMapper;
//	@Autowired
//	private ProjectUserMapMapper projectUserMapMapper;
//	@Autowired
//	private GroupMapper groupMapper;
//	@Autowired
//	private OperLogsMapper operLogsMapper;
//	@Autowired
//	private ProjectScientificMapMapper proSeciMapDao;
//	@Autowired
//	private GroupDataMapper groupDataMapper;
//
//	@Autowired
//    private PcientificResearchTypeService pcientificResearchTypeService;
//	@Autowired
//     private GroupService groupService;
//
//	@Test
//	public void getGroupNamePath(){
//	  String name =   groupService.getGroupNamePath("5901fcda26764a9eb04638672d8931b3","版本");
//	  System.out.println(name);
//    }
//
//    @Test
//	public void  pcientific(){
//        List<PcientificResearchType> p=pcientificResearchTypeService.selectPcientificResearchTypeAll();
//        System.out.println("adbcd");
//    }
//	@Test
//    public void getColNum(){
//    	String gender;
//    	String efhnic;
//    	String maritalStatus;
//    	String nationality;
//    	String groupId="0A3535CBAA3949E88AAC8238F07CD948";
//    	Map<String,Object> mapParam = new HashMap<>();
//		mapParam.put("efhnic","");
//		mapParam.put("groupId",groupId);
//		List<GroupAggregation > test = groupDataMapper.getPatSetAggregation(mapParam);
//		JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(test));
//		System.out.println(test);
//	}
//	@Autowired
//	private ActiveIndexService indexService;
//
//
//
//    @Test
//	public void getData(){
//    	AjaxObject ajaxObject=null;
//    	String param = "{\"activeId\":\"4B05BDB7D2B548A2A4711C5960CABA64_tmp\",\"projectId\":\"c5d9cb4d-1c53-40a9-8f3b-76373501ba7e\",\"pageNum\":1,\"pageSize\":10,\"activeType\":3,\"basicColumns\":[{\"id\":\"PATIENT_SN\",\"name\":\"病人编号\"},{\"id\":\"GENDER\",\"name\":\"性别\"},{\"id\":\"BIRTH_DATE\",\"name\":\"出生日期\"},{\"id\":\"BLOOD_ABO\",\"name\":\"ABO血型名称\"},{\"id\":\"BLOOD_RH\",\"name\":\"RH血型\"},{\"id\":\"NATIONALITY\",\"name\":\"国籍\"}],\"indexColumns\":[{\"id\":\"4B05BDB7D2B548A2A4711C5960CABA64_tmp\",\"name\":\"\"}]}";
//		JSONObject jsonObject = JSONObject.parseObject(param);
//		String activeId = jsonObject.getString("activeId");
//		String projectId = jsonObject.getString("projectId");
//		Integer pageSize = jsonObject.getInteger("pageSize");
//		Integer pageNum = jsonObject.getInteger("pageNum");
//		JSONArray basicColumns = jsonObject.getJSONArray("basicColumns");
//		//JSONArray indexColumns = jsonObject.getJSONArray("indexColumns");
//		if(StringUtils.isEmpty(activeId) || StringUtils.isEmpty(projectId) || pageSize == null
//				|| pageNum == null || basicColumns == null || basicColumns.isEmpty()){
//		}
//		int activeType = jsonObject.getInteger("activeType");
//		//查找已定义的所有指标和事件
//		List<ActiveIndex> activeIndices = indexService.findeByProjectAndType(projectId, 5);
//		JSONArray indexColumns = new JSONArray();
//		String indexId = StringUtils.contains(activeId, "_") ? StringUtils.substringBeforeLast(activeId, "_") : activeId;
//		ActiveIndex activeIndex = indexService.findByActiveId(indexId);
//		List<String> ids = new ArrayList<String>();
//		if(activeIndex != null && activeType!=3){
//			JSONObject object = new JSONObject();
//			String id = activeIndex.getId();
//
//			if(activeType == 2 && StringUtils.contains(activeId,"_")&&activeIndex.getIsTmp()==1){
//				id = StringUtils.trim(id).concat("_tmp");
//			}
//			object.put("id", id);
//			object.put("name",activeIndex.getName());
//			ids.add(id);
//
//			ids.add(activeIndex.getName());
//			indexColumns.add(object);
//		}
//		//获得已定义的所有指标
//		if(activeIndices!=null && !activeIndices.isEmpty()){
//			for (ActiveIndex index:activeIndices) {
//				String id = index.getId();
//				if (!ids.contains(id)&&!ids.contains(index.getName())) {
//					JSONObject object = new JSONObject();
//					ids.add(id);
//					ids.add(index.getName());
//					ids.add(index.getName());
//					object.put("id", id);
//					object.put("name",index.getName());
//					indexColumns.add(object);
//				}
//			}
//		}
//	}
//@Autowired
//private PatientGroupService patGroupService;
//
//	@Test
//	public void groupData(){
//    	AjaxObject ajaxObject= null;
//    	String param="{\"groupId\":\"0A3535CBAA3949E88AAC8238F07CD948\",\"pageNum\":1,\"pageSize\":10,\"activeType\":3}";
//		JSONObject object= JSONObject.parseObject(param);
//		ajaxObject = patGroupService.getGroupParentData(object);
//		System.out.println();
//	}
//
//	@Test
//	public void savaLog() {
//		Map<String, Object> map = new HashMap<>();
//		map.put("projectId", "24BB7C2FF2D14564B956243D8A43CFEB");
//		map.put("groupTypeId", "001");
//		map.put("groupLevel", 0);
//		map.put("groupParentId", "24BB7C2FF2D14564B956243D8A43CFEB");
//		List<Group> listGroup = groupMapper.getGroupList(map);
//		System.out.println(listGroup);
//
////		ProjectScientificMap mmm = proSeciMapDao.selectByProjectId("D7AD893517434D6EB10FB5EBF6C451AF");
////		mmm.setScientificId("002");
////		proSeciMapDao.updateByPrimaryKeySelective(mmm);
////		System.out.println(mmm);
//		// Map<String, Object> map = new HashMap<>();
//		// map.put("uid", "00001");
//		// List<Project> list = projectDao.getProjectList(map);
//		// System.out.println(list);
//
//		// List<ProjectMember> listMember =
//		// projectMemberDao.getUserProjectList("D7AD893517434D6EB10FB5EBF6C451AF");
//		// System.out.println(listMember);
//		// List<PatientsSet> listPatients =
//		// patientsSetMapper.getPatientSetByProjectId("D7AD893517434D6EB10FB5EBF6C451AF");
//		// System.out.println(listPatients);
//		// List<OperLogs> listOperLogs =
//		// operLogsMapper.getOperLogsList("D7AD893517434D6EB10FB5EBF6C451AF");
//		// System.out.println(listOperLogs);
//	}
//
//}
