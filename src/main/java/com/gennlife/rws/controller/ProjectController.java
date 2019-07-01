package com.gennlife.rws.controller;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.OperLogs;
import com.gennlife.rws.entity.PcientificResearchType;
import com.gennlife.rws.entity.Project;
import com.gennlife.rws.service.CortrastiveAnalysisService;
import com.gennlife.rws.service.PcientificResearchTypeService;
import com.gennlife.rws.service.ProjectService;
import com.gennlife.rws.util.AjaxObject;
import com.gennlife.rws.vo.CustomerStatusEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/rws/project", produces = "application/json;charset=UTF-8")
public class ProjectController {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);

	@Autowired
	private ProjectService projectService;
	@Autowired
	private PcientificResearchTypeService pcientificResearchTypeService;
	@Autowired
	private CortrastiveAnalysisService cecortrastiveAnalysisService;
	// {projectId,page,pageSize}
	@RequestMapping(value = "/getOperLogsList", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject getOperLogsList(@RequestBody String param) {
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
			List<OperLogs> list = projectService.getOperLogsList(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			int count = projectService.getOperLogsCount(object);
			JSONObject result = new JSONObject();
			result.put("value", list);
			result.put("count", count);
			ajaxObject.setData(result);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

	@RequestMapping(value = "/getProjectList", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject getProjectList(@RequestBody String param) {
		AjaxObject ajaxObject = null;
		JSONObject object = null;
		try {
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
						CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			List<Project> list = projectService.getProjectList(object);
//			cecortrastiveAnalysisService.autoBackgroundCecort(list);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			ajaxObject.setData(list);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

	@RequestMapping(value = "/getProject", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject getProject(@RequestBody String param) {
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
			Project project = projectService.getProject(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			ajaxObject.setData(project);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

	@RequestMapping(value = "/saveProject", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject saveProject(@RequestBody String param) {
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
			Project project = projectService.saveProject(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			ajaxObject.setData(project);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

	@RequestMapping(value = "/updateProject", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject updateProject(@RequestBody String param) {
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
			Project project = projectService.updateProject(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			ajaxObject.setData(project);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

	@RequestMapping(value = "/deleteProject", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject deleteProject(@RequestBody String param) {
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
			projectService.deleteProject(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

	@RequestMapping(value = "/getScientific", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject getScientific() {
		AjaxObject ajaxObject = null;
		try {
			List<PcientificResearchType> pcientificResearchTypes = pcientificResearchTypeService
					.selectPcientificResearchTypeAll();
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			ajaxObject.setData(pcientificResearchTypes);
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
		}
		return ajaxObject;
	}

	@RequestMapping(value = "/checkName", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject checkName(@RequestBody String param) {
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
			String count = projectService.checkNameType(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			ajaxObject.setData(count);
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
		}
		return ajaxObject;
	}

	@RequestMapping(value = "/projectAggregation", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject projectAggregation(@RequestBody String param) {
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
			ajaxObject = projectService.getprojectAggregation(object);
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
		}
		return ajaxObject;
	}

	@RequestMapping(value = "/projectPowerExamine", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject projectPowerExamine(@RequestBody String param) {
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
			ajaxObject = projectService.projectPowerExamine(object);
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
		}
		return ajaxObject;
	}

	@RequestMapping(value = "/getProjectAttribute", method = { RequestMethod.POST, RequestMethod.GET })
	public Object getProjectAttribute(@RequestBody String param) {
		Object ajaxObject = null;
		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
					CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			ajaxObject = projectService.getProjectAttribute(object);
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
		}
		return ajaxObject;
	}

	@RequestMapping(value = "/eligible", method = { RequestMethod.POST, RequestMethod.GET })
	public Object eligible(@RequestBody String param) {
		Object ajaxObject = null;
		try {
			JSONObject object = null;
			try {
				object = JSONObject.parseObject(param);
			} catch (Exception e) {
				ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
					CustomerStatusEnum.FORMATJSONERROR.getMessage());
				return ajaxObject;
			}
			ajaxObject = projectService.eligible(object);
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
		}
		return ajaxObject;
	}


}
