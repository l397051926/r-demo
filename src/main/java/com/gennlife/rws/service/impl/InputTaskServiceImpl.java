package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.catche.CortrastiveCache;
import com.gennlife.rws.content.InputStratus;
import com.gennlife.rws.content.LiminaryContent;
import com.gennlife.rws.content.UqlConfig;
import com.gennlife.rws.dao.InputTaskMapper;
import com.gennlife.rws.dao.PatientsSetMapper;
import com.gennlife.rws.dao.ProjectMapper;
import com.gennlife.rws.entity.InputTask;
import com.gennlife.rws.entity.Project;
import com.gennlife.rws.query.BuildIndexRws;
import com.gennlife.rws.rocketmq.ProducerService;
import com.gennlife.rws.service.InputTaskService;
import com.gennlife.rws.service.ProjectService;
import com.gennlife.rws.service.RedisMapDataService;
import com.gennlife.rws.util.AjaxObject;
import com.gennlife.rws.util.HttpUtils;
import com.gennlife.rws.util.SingleExecutorService;
import com.gennlife.rws.util.StringUtils;
import com.gennlife.rws.web.WebAPIResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;


@Service
@Transactional(rollbackFor = RuntimeException.class)
public class InputTaskServiceImpl implements InputTaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputTaskServiceImpl.class);

    @Autowired
    private InputTaskMapper inputTaskMapper;
    @Autowired
    private RedisMapDataService redisMapDataService;
    @Autowired
    private HttpUtils httpUtils;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private PatientsSetMapper patientsSetMapper;
    @Autowired
    private ProducerService producerService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private LiminaryContent liminaryContent;
    @Override
    public AjaxObject getAllInputTasks(JSONObject object) {
        JSONObject querObj = object.getJSONObject("query");
        if(querObj == null ){
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,AjaxObject.AJAX_MESSAGE_FAILURE);
        }
        String uid = querObj.getString("uid");
        String projectName = querObj.getString("projectName");
        String patientSetName = querObj.getString("patientSetName");
        Integer status = querObj.getInteger("status");
        Integer page = object.getInteger("page");
        Integer size = object.getInteger("size");
        if(page==null || size ==null  ){
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"参数不对 缺少 page 或者 size");
        }
        Integer startNum = (page-1)*size;
        List<InputTask> inputTasks = inputTaskMapper.getInputTasks(uid,projectName,patientSetName,status,startNum, size);
        for (InputTask inputTask : inputTasks){
            if(Objects.equals(InputStratus.FAILURE, inputTask.getStatus()) && inputTask.getRemainTime() != null && inputTask.getRemainTime() !=0){
                inputTask.setRemainTime(null);
                inputTaskMapper.updateInputTaskRemainTime(inputTask);
            }
            if(Objects.equals(InputStratus.UNDER_WAY, inputTask.getStatus()) || Objects.equals(InputStratus.IN_QUEUE, inputTask.getStatus())){
                String projectId = inputTask.getProjectId();
                String patientSetId = inputTask.getPatientSetId();
                Integer proCount = projectMapper.selectCountByProjectId(projectId);
                Integer patSetCount = patientsSetMapper.getPatientSetCount(patientSetId);
                if( proCount == null || proCount <= 0 ||  patSetCount == null || patSetCount <=0){
                    inputTask.setRemainTime(null);
                    inputTask.setFinishTime(new Date(0));
                    inputTask.setStatus(InputStratus.FAILURE);
                    inputTask.setUpdateTime(new Date());
                    inputTaskMapper.updateinputCancelDate(inputTask);
                }

            }
        }
        Integer total = inputTaskMapper.getInputTasksTotal(uid,projectName,patientSetName,status);
        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,AjaxObject.AJAX_MESSAGE_SUCCESS);
        ajaxObject.setData(inputTasks);
        WebAPIResult<Object> webAPIResult = new WebAPIResult<>(page,size,total);
        ajaxObject.setWebAPIResult(webAPIResult);
        return ajaxObject;
    }

    @Override
    public AjaxObject deleteInputTask(JSONObject object) {
        String inputId = object.getString("taskId");
        InputTask task = inputTaskMapper.getInputtaskByInputId(inputId);
        if(task == null ){
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"任务id不存在");
        }
        if(task.getStatus() == 3 || task.getStatus() == 2){
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"任务正在进行，无法删除");
        }
        inputTaskMapper.deleteInputTaskByInputId(inputId);
        return new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,AjaxObject.AJAX_MESSAGE_SUCCESS);
    }

    @Override
    public AjaxObject restartInputTask(JSONObject object) {
        String taskId = object.getString("taskId");
        InputTask task = inputTaskMapper.getInputtaskByInputId(taskId);
        if(task == null ){
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"任务id不存在");
        }
        String taskProjectId = task.getProjectId();
        String taskPatientSetId = task.getPatientSetId();
        Integer proCount = projectMapper.selectCountByProjectId(taskProjectId);
        if(proCount == null || proCount <= 0){
            return new AjaxObject(AjaxObject.AJAX_STATUS_TIPS,"无法找到相应项目，导入失败");
        }
        Project project = projectMapper.selectByProjectId(taskProjectId);
        if(StringUtils.isNotEmpty(project.getCrfId()) && !project.getCrfId().equals(task.getCrfId())){
            return new AjaxObject(AjaxObject.AJAX_STATUS_TIPS,"项目内已有不同数据源的患者，导入失败");
        }
        Integer patSetCount = patientsSetMapper.getPatientSetCount(taskPatientSetId);
        if(patSetCount == null || patSetCount <=0){
            return new AjaxObject(AjaxObject.AJAX_STATUS_TIPS,"无法找到相应患者集，导入失败");
        }
        //导出任务项目数据
        JSONObject obj = JSONObject.parseObject(redisMapDataService.getDataBykey(UqlConfig.getRwsService(taskId)));
        String crfId;
        String projectId;
        JSONObject esJson;
        String createId;
        String crfName;
        Long count = task.getPatientCount();
        if(obj == null ){
            InputTask tasks = inputTaskMapper.getInputtaskAllByInputId(taskId);
            crfId = tasks.getCrfId();
            projectId = tasks.getProjectId();
            esJson = JSONObject.parseObject(tasks.getEsJson());
            crfName = tasks.getCrfName();
            createId = tasks.getUid();
        }else {
            crfId = obj.getString("crfId");
            projectId = obj.getString("projectId");
            esJson = obj.getJSONObject("esJSon");
            createId = obj.getString("createId");
            crfName = obj.getString("crfName");
        }
        projectService.saveDatasource(projectId,crfId,crfName);

        Integer quereCount = inputTaskMapper.getInputQueueTask(createId);
        if(quereCount >2){
            return new AjaxObject(AjaxObject.AJAX_STATUS_TIPS,"排队任务数较多，请完成后再导入") ;
        }
        Integer maxMember = liminaryContent.getMaxMember();
        Integer allCount = patientsSetMapper.getSumCount(projectId) == null ? 0 :  patientsSetMapper.getSumCount(projectId);
        Integer runTaskSumCount = inputTaskMapper.getRunTaskSumCountByProjcetId(projectId);
        runTaskSumCount = runTaskSumCount == null ? 0 : runTaskSumCount;
        if(allCount +runTaskSumCount + count >maxMember){
            return new AjaxObject(AjaxObject.AJAX_STATUS_TIPS,"人数超出限制，无法导入") ;
        }
        //更新 input task mapper createTime
        InputTask inputTask = new InputTask();
        inputTask.setInputId(taskId);
        inputTask.setStartTime(new Date());
        inputTask.setStatus(InputStratus.IN_QUEUE);
        inputTask.setUpdateTime(new Date());
        inputTask.setRemainTime(null);
        inputTaskMapper.updateInputTask(inputTask);


        BuildIndexRws buildIndexRws =  StringUtils.isNotEmpty(crfId) && !"EMR".equals(crfId)
            ?
            new BuildIndexRws(projectId,esJson.getString("indexName"),esJson.getString("query"),crfId, esJson.getJSONObject("power"))
            :
            new BuildIndexRws(projectId,esJson.getString("indexName"),esJson.getString("query"), esJson.getJSONObject("power"));
        buildIndexRws.setBuildIndexID(taskId);
        buildIndexRws.setAction(1);
        buildIndexRws.setUid(createId);
        String result = httpUtils.buildIndexRws(buildIndexRws);

        LOGGER.info("taskId: "+taskId +" 重新创建索引成功 返回结果："+result);
        return new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,AjaxObject.AJAX_MESSAGE_SUCCESS);
    }

    @Override
    public AjaxObject cencelInputTasks(JSONObject object) {
        String taskId = object.getString("taskId");
        InputTask inputTask = inputTaskMapper.getInputtaskByInputId(taskId);
        String projectName = projectMapper.getProjectNameByProjectId(inputTask.getProjectId());
        String createId = projectMapper.getCreateIdByTaskId(taskId);
        if(Objects.isNull(inputTask) ){
            return new AjaxObject(AjaxObject.AJAX_STATUS_FAILURE,"任务id不存在");
        }
        Integer sum = patientsSetMapper.getSumCount(inputTask.getProjectId());
        if(sum == null || sum == 0){
            projectMapper.saveDatasource(inputTask.getProjectId(),"","");
        }
        updateCencelStatus(taskId,InputStratus.CANCEL);
        producerService.sendProExportField(createId,taskId,inputTask.getProjectId(),projectName);
        SingleExecutorService.getInstance().getCenterTaskeExecutor().submit(() -> cencelInputTaskByTaskId(taskId,createId,inputTask.getProjectId(),inputTask.getCrfId(),false));
        return new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,AjaxObject.AJAX_MESSAGE_SUCCESS);
    }

    private void updateCencelStatus(String taskId, Integer cancel) {
        InputTask inputTask = new InputTask();
        inputTask.setInputId(taskId);
        inputTask.setRemainTime(null);
        inputTask.setFinishTime(new Date(0));
        inputTask.setStatus(cancel);
        inputTask.setUpdateTime(new Date());
        inputTaskMapper.updateinputCancelDate(inputTask);
        LOGGER.info("取消任务i中。。。 ： taskid："+ taskId);
    }


    @Override
    public void cencelInputTasksOnDelPatSet(String patientsSetId, String userId, String projectId, String projectName, String crfId) {
        List<String> taskIds  = inputTaskMapper.getRunTimeInputIdsByPatientSetId(patientsSetId);
        cencelInputTasks(userId, projectId, projectName, taskIds,crfId,false);
    }

    private void cencelInputTasks(String userId, String projectId, String projectName, List<String> taskIds,String crfId,boolean isDeleteProject) {
        for (String taskId :taskIds){
            if(CortrastiveCache.getDelProjectOrPatientSetTaskSet().contains(taskId)){
                continue;
            }
            updateCencelStatus(taskId,InputStratus.CANCEL);
            CortrastiveCache.getDelProjectOrPatientSetTaskSet().add(taskId);
            Integer sum = patientsSetMapper.getSumCount(projectId);
            if(sum == null || sum == 0){
                projectMapper.saveDatasource(projectId,"","");
            }
            producerService.sendProExportField(userId,taskId,projectId,projectName);
            SingleExecutorService.getInstance().getCenterTaskeExecutor().submit(() -> cencelInputTaskByTaskId(taskId,userId, projectId,crfId,isDeleteProject));
        }
    }

    @Override
    public void updateCencelDate(String taskId) {
        InputTask inputTask = new InputTask();
        inputTask.setInputId(taskId);
        inputTask.setRemainTime(null);
        inputTask.setFinishTime(new Date(0));
        inputTask.setStatus(InputStratus.FAILURE);
        inputTask.setUpdateTime(new Date());
        inputTaskMapper.updateinputCancelDate(inputTask);
        LOGGER.info("删除数据成功 ： taskid："+ taskId);
    }

    @Override
    public Object judgeInputTaskStatus(JSONObject object) {
        String projectId = object.getString("projectId");
        String patientSetId = object.getString("patientSetId");
        String uid = object.getString("uid");
        InputTask inputTask = new InputTask();
        inputTask.setUid(uid);
        if(StringUtils.isNotEmpty(projectId)){
            inputTask.setProjectId(projectId);
            Integer count = inputTaskMapper.judgeInputTaskStatus(inputTask);
            if(count>0){
                return new JSONObject().fluentPut("status",300)
                                        .fluentPut("msg","项目内正有导入任务，删除会导致任务失败，是否继续");
            }
        }
        if(StringUtils.isNotEmpty(patientSetId)){
            inputTask.setPatientSetId(patientSetId);
            Integer count = inputTaskMapper.judgeInputTaskStatus(inputTask);
            if(count>0){
                return new JSONObject().fluentPut("status",300)
                                        .fluentPut("msg","项目内正有导入任务，删除会导致任务失败，是否继续");
            }
        }
        return new JSONObject().fluentPut("status",200)
                                .fluentPut("isHint",true);
    }

    @Override
    public void cencelInputTasksOnDelProject(String userId, String projectId, String projectName, String crfId) {
        List<String> taskIds  = inputTaskMapper.getRunTimeInputIdsByProjectId(projectId);
        cencelInputTasks(userId, projectId, projectName, taskIds,crfId,true);
    }

    @Override
    public Object decideInputs(JSONObject object) {
        String projectId = object.getString("projectId");
        Integer num = inputTaskMapper.getRunTimeTaskByProjectId(projectId);
        if(num>0 ){
            return new JSONObject().fluentPut("status",300).fluentPut("message","项目内患者正在变更，请稍后再试");
        }
        return new JSONObject().fluentPut("status",200).fluentPut("message","操作成功");
    }

    private void cencelInputTaskByTaskId(String taskId, String createId, String projectId, String crfId,Boolean isDeleteProject) {

        BuildIndexRws buildIndexRws =  new BuildIndexRws();
        buildIndexRws.setBuildIndexID(taskId);
        buildIndexRws.setAction(2);
        buildIndexRws.setUid(createId);

        String result = httpUtils.buildIndexRws(buildIndexRws);
        try {
            JSONObject object = JSONObject.parseObject(result);
            Integer status = object.getInteger("status");
            int num = 3;
            while (status == 500 && num >0 ){
                result = httpUtils.buildIndexRws(buildIndexRws);
                object = JSONObject.parseObject(result);
                status = object.getInteger("status");
                LOGGER.info("taskId: "+taskId +" 取消任务失败 返回结果："+result + "重试第"+num+"次");
                num --;
            }
            updateCencelDate(taskId);
            LOGGER.info("taskId: "+taskId +" 取消任务成功 返回结果："+result);
        }catch (Exception e){
            updateCencelDate(taskId);
            LOGGER.error("taskId: "+taskId +" 取消任务异常 但仍修改任务状态 返回结果："+result);
        }

        Integer inputCenterCount = inputTaskMapper.getCountByProjectIdAndStatus(projectId,InputStratus.CANCEL);
        LOGGER.info("此时正在 取消中的任务 数量 为 "+ inputCenterCount);
        if(inputCenterCount > 0) {
            return;
        }
        if(isDeleteProject){
            projectService.deleteProjectIndex(crfId,projectId);
        }else {
            //判定患者集 如果总数为0 则删除 索引
            Integer allCount = patientsSetMapper.getSumCount(projectId);
            if(allCount == null || allCount == 0){
                projectService.deleteProjectIndex(crfId,projectId);
            }
        }
        LOGGER.info("取消任务完成 taskid：" + taskId );
    }

}
