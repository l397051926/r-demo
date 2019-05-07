package com.gennlife.rws.controller;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.ProjectMember;
import com.gennlife.rws.service.ProjectMemberService;

import java.util.List;

import com.gennlife.rws.util.AjaxObject;
import com.gennlife.rws.vo.CustomerStatusEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @className ProjectMemberController
 * @author zhengguohui
 * @description 项目成员相关操作
 */
@RestController
@RequestMapping("/rws/projectMember")
public class ProjectMemberController {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);

	@Autowired
	private ProjectMemberService ProMemBerService;

	@RequestMapping(value = "/getProjectMemberList", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject getProjectMemberList(@RequestBody String param) {
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
			List<ProjectMember> list = ProMemBerService.getProjectMemberList(object);
			JSONObject result = new JSONObject();
			result.put("value",list);
			int count = ProMemBerService.getProjectMemberCount(object);
			result.put("count",count);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			ajaxObject.setData(result);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

	@RequestMapping(value = "/getProjectMember", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject getProjectMember(@RequestBody String param) {
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
			ProjectMember proMember = ProMemBerService.getProjectMember(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			ajaxObject.setData(proMember);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(),
					CustomerStatusEnum.FORMATJSONERROR.toString());
			return ajaxObject;
		}
	}

	/**
	 * data{ data:[{},{},{}],projectId:xx,content:xx,createId:Xx,createName:xx}
	 * 
	 * @param param
	 */
	@RequestMapping(value = "/saveProjectMember", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject saveProjectMember(@RequestBody String param) {
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
			List<ProjectMember> listUser = ProMemBerService.saveProjectMember(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			ajaxObject.setData(listUser);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

	@RequestMapping(value = "/updateProjectMember", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject updateProjectMember(@RequestBody String param) {
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
			ProjectMember proMember = ProMemBerService.updateProjectMember(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			ajaxObject.setData(proMember);
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

	@RequestMapping(value = "/deleteProjectMember", method = { RequestMethod.POST, RequestMethod.GET })
	public AjaxObject deleteProjectMember(@RequestBody String param) {
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
			ProMemBerService.deleteProjectMember(object);
			ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
			ajaxObject.setData(object.get("projectId"));
			return ajaxObject;
		} catch (Exception e) {
			ajaxObject = new AjaxObject(CustomerStatusEnum.UNKONW_ERROR.getCode(), e.getMessage());
			return ajaxObject;
		}
	}

}
