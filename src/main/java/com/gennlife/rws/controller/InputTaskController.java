package com.gennlife.rws.controller;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.service.InputTaskService;
import com.gennlife.rws.util.AjaxObject;
import com.gennlife.rws.vo.CustomerStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/rws/inputs/", produces = "application/json;charset=UTF-8")
public class InputTaskController {
    private static final Logger LOG = LoggerFactory.getLogger(InputTaskController.class);

    @Autowired
    private InputTaskService inputTaskService;

    // 获取任务列表接口
    @RequestMapping(value = "/inputInfo", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject inputInfo(@RequestBody String param) {
        AjaxObject ajaxObject;
        try {
            JSONObject object;
            try {
                object = JSONObject.parseObject(param);
            } catch (Exception e) {
                ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
                    CustomerStatusEnum.FORMATJSONERROR.getMessage());
                return ajaxObject;
            }
            ajaxObject = inputTaskService.getAllInputTasks(object);
        } catch (Exception e) {
            LOG.error("获取数据列表，异常信息{}", e);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, AjaxObject.AJAX_MESSAGE_FAILURE);
        }
        return ajaxObject;
    }
    //取消导出接口
    @RequestMapping(value = "/cancel", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject cancel(@RequestBody String param) {
        AjaxObject ajaxObject;
        try {
            JSONObject object;
            try {
                object = JSONObject.parseObject(param);
            } catch (Exception e) {
                ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
                    CustomerStatusEnum.FORMATJSONERROR.getMessage());
                return ajaxObject;
            }
            ajaxObject = inputTaskService.cencelInputTasks(object);
        } catch (Exception e) {
            LOG.error("获取数据列表，异常信息{}", e);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, AjaxObject.AJAX_MESSAGE_FAILURE);
        }
        return ajaxObject;
    }

    //重新导出接口
    @RequestMapping(value = "/restart", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject restart(@RequestBody String param) {
        AjaxObject ajaxObject;
        try {
            JSONObject object;
            try {
                object = JSONObject.parseObject(param);
            } catch (Exception e) {
                ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
                    CustomerStatusEnum.FORMATJSONERROR.getMessage());
                return ajaxObject;
            }
            ajaxObject = inputTaskService.restartInputTask(object);
        } catch (Exception e) {
            LOG.error("获取数据列表，异常信息{}", e);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, AjaxObject.AJAX_MESSAGE_FAILURE);
        }
        return ajaxObject;
    }

    //删除接口
    @RequestMapping(value = "/delete", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxObject delete(@RequestBody String param) {
        AjaxObject ajaxObject;
        try {
            JSONObject object;
            try {
                object = JSONObject.parseObject(param);
            } catch (Exception e) {
                ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
                    CustomerStatusEnum.FORMATJSONERROR.getMessage());
                return ajaxObject;
            }
            ajaxObject = inputTaskService.deleteInputTask(object);
        } catch (Exception e) {
            LOG.error("获取数据列表，异常信息{}", e);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, AjaxObject.AJAX_MESSAGE_FAILURE);
        }
        return ajaxObject;
    }
    //判定 任务状态
    @RequestMapping(value = "/judgeTaskStatus", method = {RequestMethod.POST, RequestMethod.GET})
    public Object judgeInputTaskStatus(@RequestBody String param) {
        Object ajaxObject;
        try {
            JSONObject object;
            try {
                object = JSONObject.parseObject(param);
            } catch (Exception e) {
                ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
                    CustomerStatusEnum.FORMATJSONERROR.getMessage());
                return ajaxObject;
            }
            ajaxObject = inputTaskService.judgeInputTaskStatus(object);
        } catch (Exception e) {
            LOG.error("获取数据列表，异常信息{}", e);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, AjaxObject.AJAX_MESSAGE_FAILURE);
        }
        return ajaxObject;
    }

    @RequestMapping(value = "/decideInputs", method = {RequestMethod.POST, RequestMethod.GET})
    public Object decideInputs(@RequestBody String param) {
        Object ajaxObject;
        try {
            JSONObject object;
            try {
                object = JSONObject.parseObject(param);
            } catch (Exception e) {
                ajaxObject = new AjaxObject(CustomerStatusEnum.FORMATJSONERROR.getCode(),
                    CustomerStatusEnum.FORMATJSONERROR.getMessage());
                return ajaxObject;
            }
            ajaxObject = inputTaskService.decideInputs(object);
        } catch (Exception e) {
            LOG.error("获取数据列表，异常信息{}", e);
            ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE, AjaxObject.AJAX_MESSAGE_FAILURE);
        }
        return ajaxObject;
    }


}
