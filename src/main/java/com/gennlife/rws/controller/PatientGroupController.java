package com.gennlife.rws.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.ActiveIndex;
import com.gennlife.rws.entity.Group;
import com.gennlife.rws.entity.GroupType;
import com.gennlife.rws.service.*;
import com.gennlife.rws.util.AjaxObject;
import com.gennlife.rws.util.StringUtils;
import com.gennlife.rws.vo.CustomerStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 
 * @className PatientGroupController
 * @author zhengguohui
 * @description 患者分组
 * @date 2018年7月5日
 */
@RestController
@RequestMapping("/rws/patientGroup")
public class PatientGroupController {

	private static final Logger LOG = LoggerFactory.getLogger(PatientGroupController.class);
	@Autowired
	private PatientGroupService patGroupService;
	@Autowired
	private ActiveIndexService activeIndexService;
	@Autowired
	private SearchByuqlService searchByuqlService;
	@Autowired
	private ModuleConvertService moduleConvertService;
	@Autowired
	private GroupService groupService;

	@RequestMapping(value = "/getPatientGroupList", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject getPatientGroupList(@RequestBody String param) {
		AjaxObject ajaxObject = null;
		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
						CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			List<Group> list = patGroupService.getPatientGroupList(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			ajaxObject.setData(list);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}

	}

	@RequestMapping(value = "/getGroupTypeList", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject getGroupTypeList(@RequestBody String param) {
		AjaxObject ajaxObject = null;
		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
						CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			List<GroupType> list = patGroupService.getGroupTypeList(object);
			ajaxObject = patGroupService.getGroupCountTypeList(object,list);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

	@RequestMapping(value = "/getPatientGroup", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject getPatientGroup(@RequestBody String param) {
		AjaxObject ajaxObject = null;
		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
						CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			Group group = patGroupService.getPatientGroup(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			ajaxObject.setData(group);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

	// 新增按钮保存功能
	@RequestMapping(value = "/savePatientGroup", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject savePatientGroup(@RequestBody String param) {
		AjaxObject ajaxObject = null;
		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
						CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			// 保存后是否需要将数据返回(取决于id是否需要)
			Group group = patGroupService.savePatientGroup(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			ajaxObject.setData(group);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}

	}

	// 新增组 添加患者 添加按钮 传递患者集ID
	@RequestMapping(value = "/saveGroupAndPatient", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject saveGroupAndPatient(@RequestBody String param) {
		AjaxObject ajaxObject = null;
		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
						CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			// 保存分组ID和患者的对应关系 传递患者集ID
			Integer count = patGroupService.saveGroupAndPatient(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			JSONObject data = new JSONObject();
			data.put("groupId",object.getString("groupId"));
			data.put("repetitionCount",count);
			ajaxObject.setData(data);
			return ajaxObject;
		} catch (Exception e) {
			e.printStackTrace();
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}

	}

	// 查看详情 进一步筛选保存功能
	@RequestMapping(value = "/insertGroupDataPatient", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject insertGroupDataPatient(@RequestBody String param) {
		AjaxObject ajaxObject = null;
		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
						CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			// 保存后是否需要将数据返回(取决于id是否需要)
			String message = patGroupService.insertGroupDataPatient(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			ajaxObject.setMessage(message);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}

	}

	// 查看详情 进一步导入本组
	@RequestMapping(value = "/exportGroupDataPatient", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject exportGroupDataPatient(@RequestBody String param) {
		AjaxObject ajaxObject = null;
		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
						CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			// 保存后是否需要将数据返回(取决于id是否需要)
//			patGroupService.exportGroupDataPatient(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}

	}

	@RequestMapping(value = "/updatePatientGroup", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject updatePatientGroup(@RequestBody String param) {
		AjaxObject ajaxObject = null;
		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
						CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			Group group = patGroupService.updatePatientGroup(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			ajaxObject.setData(group);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}

	}

	@RequestMapping(value = "/deletePatientGroup", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject deletePatientGroup(@RequestBody String param) {
		AjaxObject ajaxObject = null;
		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
						CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			patGroupService.deletePatientGroup(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

	// 进一步筛选 查询筛选条件 查询患者列表
	@RequestMapping(value = "/getPatientList", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject getPatientList(@RequestBody String param) {
		AjaxObject ajaxObject = null;
		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
						CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			ajaxObject = patGroupService.getPatientList(object);
//			ajaxObject = searchByuqlService.getPatientListByUal(object); //uql版本
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

	@RequestMapping(value = "/getActiveIndexList", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject getActiveIndexList(@RequestBody String param) {
		AjaxObject ajaxObject = null;

		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				return new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
						CustomerStatusEnum.FORMATJSONERROR.getMessage());

			}
			// 查询筛选条件 待定
			String uid = object.getString("uid");
			String groupId = object.getString("groupId");


			String activeId = null;
			ActiveIndex activeIndex = activeIndexService.findByActiveId(groupId);
			if(activeIndex != null){
				ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
				JSONObject o = (JSONObject) JSONObject.toJSON(activeIndex);
				//兼容前端json解析
				if (o != null) {
					//字符串格式的["",""]转成数组格式
					String text = o.toString();
					String toWebUI = StringUtils.replace(text, "\"[", "[").replace("]\"", "]").replace("\\\"", "\"");

					JSONObject webUi = JSONObject.parseObject(toWebUI);
					JSONArray webConfig = o.getJSONArray("config");
					JSONArray newWebConfig = new JSONArray();
					int webSize = webConfig == null ? 0 : webConfig.size();
					//如果数据为空 则结束
					if (webSize == 0) {
						ajaxObject.setData(null);
						return ajaxObject;
					}
					for (int i = 0; i < webSize; i++) {
						JSONObject config = webConfig.getJSONObject(i);
						JSONArray conditions = config.getJSONArray("conditions");
						int length = conditions == null ? 0 : conditions.size();
						JSONArray newCondition = new JSONArray();
						for (int j = 0; j < length; j++) {
							JSONObject condition = conditions.getJSONObject(j);
							JSONObject converted = moduleConvertService.rwsToUi(condition);
							newCondition.add(converted);
						}
						config.put("conditions", newCondition);
						newWebConfig.add(config);
					}
					//枚举修改
					JSONArray conditionNew = moduleConvertService.enumFormatToUi(newWebConfig);
					webUi.put("config", conditionNew);
					JSONObject activeResult = new JSONObject();
					activeResult.put("active", webUi);
                    activeResult.put("isGroupActive", 1);
					ajaxObject.setData(activeResult);
				}
			}else {
				ajaxObject =  patGroupService.getPatientSearchActive(groupId);
			}
			return ajaxObject;

		} catch (Exception e) {
			return new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
		}
	}

	@RequestMapping(value = "/saveActiveIndex", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject saveActiveIndex(@RequestBody String param) {
		AjaxObject ajaxObject = null;
		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
						CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			activeIndexService.saveActive(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

	@RequestMapping(value = "/groupAggregation", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject groupAggregation(@RequestBody String param){
		AjaxObject ajaxObject = null;
		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
						CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			ajaxObject = patGroupService.groupAggregation(object);
//			ajaxObject = searchByuqlService.getAggregation(object); //uql 版本
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

	@RequestMapping(value = "/getGroupParentData", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject getGroupParentData(@RequestBody String param){
		AjaxObject ajaxObject = null;
		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
						CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			ajaxObject = patGroupService.getGroupParentData(object);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

	@RequestMapping(value = "/getGroupIdPath", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject getGroupIdPath(@RequestBody String param){
		AjaxObject ajaxObject = null;
		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
					CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			ajaxObject = groupService.getGroupIdPath(object);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}



}
