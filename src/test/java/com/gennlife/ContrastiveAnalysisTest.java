//package com.gennlife;
//
//import com.gennlife.rws.entity.ActiveIndex;
//import com.gennlife.rws.entity.Group;
//import com.gennlife.rws.entity.GroupCondition;
//import com.gennlife.rws.service.ActiveIndexService;
//import com.gennlife.rws.service.CortrastiveAnalysisService;
//import com.gennlife.rws.service.GroupService;
//import com.gennlife.rws.util.AjaxObject;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.List;
//import java.util.concurrent.ExecutionException;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@Ignore
//public class ContrastiveAnalysisTest {
//
//    @Autowired
//    private GroupService groupService;
//
//    @Autowired
//    private CortrastiveAnalysisService cortrastiveAnalysisService;
//
//    @Autowired
//    private ActiveIndexService activeIndexService;
//
//    @Test
//    public void helloTest() {
//        System.out.println("hello test this is contrastiveAnalysisTest!!");
//    }
////
////    @Test
////    public void getContResultTest() throws ExecutionException, InterruptedException {
////        String uid = "001";
////        String projectId = "project1";
////       AjaxObject ajaxObject=cortrastiveAnalysisService.getContResult(uid,projectId);
////        System.out.println(ajaxObject);
////    }
//
////    @Test
////    public void getPatientGroupCondition() {
////        String groupType = "病例组";
////        String projectId = "001";
////        String uid = "001";
////        //获取项目分组 信息
////        List<Group> groupList = groupService.getGroupByProjectId(groupType, projectId);
////        List<GroupCondition> groupConditionList = groupService.getGroupConditionProjectId(uid, projectId, cortType);
////        AjaxObject ajaxObject = cortrastiveAnalysisService.getPatientGroupCondition(groupList, groupConditionList);
////        System.out.println(ajaxObject);
////    }
//
//    @Test
//    public void getResearchVariable() {
//        String uid = "001";
//        String projectId = "4dc34756-213b-449b-ab1e-d6ee06e7a8bc";
//        List<ActiveIndex> activeIndices = activeIndexService.getActiveIndexByProjectId(uid, projectId);
//        AjaxObject ajaxObject = new AjaxObject(AjaxObject.AJAX_STATUS_SUCCESS, AjaxObject.AJAX_MESSAGE_SUCCESS);
//        ajaxObject.setData(activeIndices);
//        System.out.println(ajaxObject);
//    }
//
////    //获取结果 图形列表
////    @Test
////    public void getContResult() throws ExecutionException, InterruptedException {
////        String uid = "001";
////        String projectId = "001";
////        AjaxObject ajaxObject = cortrastiveAnalysisService.getContResult(uid, projectId);
////        System.out.println(ajaxObject);
////    }
//
//    //获取计算结果的患者列表
////    @Test
////    public void getContResultForPatient() {
////        String uid = "001";
////        String projectId = "001";
////        AjaxObject ajaxObject = cortrastiveAnalysisService.getContResultForPatient(uid, projectId);
////        System.out.println(ajaxObject);
////
////    }
//
//
//}
