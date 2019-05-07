/*
package com.gennlife;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.content.CommonContent;
import com.gennlife.rws.dao.ProjectMapper;
import com.gennlife.rws.entity.Project;
import com.gennlife.rws.service.ProjectService;
import com.gennlife.rws.util.AjaxObject;
import org.aspectj.weaver.loadtime.Aj;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

*/
/**
 * @author liumingxin
 * @create 2018 02 9:39
 * @desc
 **//*

@RunWith(SpringRunner.class)
@SpringBootTest
public class PreLiminaryTest {

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectMapper projectMapper;
    @Test
    public void getProjectListByCrfIdTest() {
        String crfId = "EMR";
        String uid = "13";
        JSONObject paramObj = new JSONObject();
        paramObj.put("crfId",crfId);
        paramObj.put("uid",uid);
        List<Project> projectList = projectService.getProjectListByCrfId(paramObj);
        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS,AjaxObject.AJAX_MESSAGE_SUCCESS);
        ajaxObject.setData(projectList);
        System.out.println(projectList);
        System.out.println("ens");
    }
    @Test
    public void saveDataSource(){
        String projectId="123111";
        String crfId = "CVD";
        String crfName = "心血管疾病";
        saveDatasource(projectId,crfId,crfName);

    }

    public void saveDatasource(String projectId,String crfId, String crfName) {
        if(CommonContent.EMR_CRF_NAME.equals(crfName)){
            projectMapper.saveDatasource(projectId,crfName,"");
        }else {
            projectMapper.saveDatasource(projectId,"单病种-"+crfName,crfId);
        }
    }


}
*/
