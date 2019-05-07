//package com.gennlife;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.gennlife.rws.dao.GroupDataMapper;
//import com.gennlife.rws.entity.GroupData;
//import com.gennlife.rws.entity.Patient;
//import com.gennlife.rws.service.PatientGroupService;
//import com.gennlife.rws.service.RwsSearchService;
//import com.gennlife.rws.util.AjaxObject;
//import com.gennlife.rws.util.StringUtils;
//import com.gennlife.rws.web.WebAPIResult;
//import com.mongodb.*;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@Ignore
//public class MongoUtilTest {
//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//    @Autowired
//    private RwsSearchService rwsSearchService;
//
//    @Test
//    public void getMongoTest() {
//        String activeId = "a7dd985f-bd44-4276-920f-c946527898e6";
//        DBCollection collection = mongoTemplate.getCollection(activeId);
//        DBObject queryObject = Query.query(Criteria.where("is_match").is(true).and("countFlag").is(true)).getQueryObject();
//        DBObject fields = new BasicDBObject();
//        fields.put("_id", false);
//        fields.put("patient_sn", true);
//        fields.put("active_result", true);
//        DBCursor dbObjects = collection.find(queryObject, fields);
//        List<String> patients = new ArrayList<String>();
//        while (dbObjects.hasNext()) {
//            DBObject next = dbObjects.next();
//            String patient_sn = (String) next.get("patient_sn");
//            patients.add(patient_sn);
//        }
//        System.out.println("demo");
//    }
//
//    @Test
//    public void getSearch() {
//        String projectId = "";
//        String bcolumns = "{\"basicColumns\":[{\"id\":\"PATIENT_SN\",\"name\":\"病人编号\"},{\"id\":\"GENDER\",\"name\":\"性别\"},{\"id\":\"BIRTH_DATE\",\"name\":\"出生日期\"},{\"id\":\"BLOOD_ABO\",\"name\":\"ABO血型名称\"},{\"id\":\"BLOOD_RH\",\"name\":\"RH血型\"},{\"id\":\"NATIONALITY\",\"name\":\"国籍\"}]}";
//        JSONArray columns = null;
//        String patient_sn = "";
//        Integer type = null;
//
//        projectId = StringUtils.substringBeforeLast(projectId, "_");
//        DBCollection projects = mongoTemplate.getCollection(projectId);
//        DBObject projectQuery = Query.query(Criteria.where("type").is("source").and("patient_sn").is(patient_sn)).getQueryObject();
//        DBCursor projectResult = projects.find(projectQuery);
//        int columnsLength = columns.size();
//        JSONObject result = new JSONObject();
//        JSONArray array = new JSONArray();
//        if (projectResult != null && projectResult.size() > 0) {
//            while (projectResult.hasNext()) {
//                DBObject next1 = projectResult.next();
//                BasicDBList patient_info = (BasicDBList) next1.get("patient_info");
//                if (patient_info != null && !patient_info.isEmpty()) {
//                    int size1 = patient_info.size();
//                    for (int i = 0; i < size1; i++) {
//                        DBObject patientInfo = (DBObject) patient_info.get(i);
//                        if (patientInfo == null) {
//                            continue;
//                        }
//                        for (int j = 0; j < columnsLength; j++) {
//                            JSONObject jsonObject1 = columns.getJSONObject(j);
//                            if (jsonObject1 == null) {
//                                continue;
//                            }
//                            String id = jsonObject1.getString("id");
//                            String name = jsonObject1.getString("name");
//                            Object value = patientInfo.get(id);
//                            if ((type == 2 || type == 3)) {
//                                if (value != null) {
//                                    result.put(id, value);
//                                } else {
//                                    result.put(id, "-");
//                                }
//                            }
//                            if (type == 1) {
//                                JSONObject object = new JSONObject();
//                                object.put("name_des", name);
//                                object.put("name", id);
//                                if (value != null) {
//                                    object.put("value", value);
//                                } else {
//                                    object.put("value", "-");
//                                }
//                                array.add(object);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        result.put("patient_info", array);
//    }
//
//    @Autowired
//    private GroupDataMapper groupDataMapper;
//
//    @Test
//    public void saveGroupDataAll() {
//        String patientSetId = "36732b6e-03d1-4c4a-92cb-2b5159da7d62";
//        String groupId = "grouid1";
//        String createId = "createid1";
//        String createName = "createName1";
//        DBCollection collection = mongoTemplate.getCollection(patientSetId);
//        DBObject queryObject = Query.query(Criteria.where("is_match").is(true).and("countFlag").is(true)).getQueryObject();
//        DBObject fields = new BasicDBObject();
//        fields.put("_id", false);
//        fields.put("patient_sn", true);
//        fields.put("active_result", true);
////        DBCursor dbObjects = collection.find(queryObject, fields);
//        DBCursor dbObjects = collection.find();
//        List<String> patients = new ArrayList<String>();
//        while (dbObjects.hasNext()) {
//            DBObject next = dbObjects.next();
//            String patient_sn = (String) next.get("patient_sn");
//            GroupData groupData = new GroupData();
//            groupData.setGroupId(groupId);
//            groupData.setPatientSn(patient_sn);
//            groupData.setCreateId(createId);
//            groupData.setCreateName(createName);
//            groupDataMapper.insert(groupData);
//        }
//        System.out.println("success");
//    }
//
//    @Autowired
//    private PatientGroupService patGroupService;
//
////    @Test
////    public void getMongData() {
////        String data = "{\"groupId\":\"grouid1\",\"basicColumns\":[{\"id\":\"PATIENT_SN\",\"name\":\"病人编号\"},{\"id\":\"GENDER\",\"name\":\"性别\"},{\"id\":\"BIRTH_DATE\",\"name\":\"出生日期\"},{\"id\":\"BLOOD_ABO\",\"name\":\"ABO血型名称\"},{\"id\":\"BLOOD_RH\",\"name\":\"RH血型\"},{\"id\":\"NATIONALITY\",\"name\":\"国籍\"}],\"activeType\":1}";
////        JSONObject object = JSONObject.parseObject(data);
////        patGroupService.getPatientList(object);
////    }
//
//    @Test
//    public void getTestMongo() {
//        String mongoId = "a7dd985f-bd44-4276-920f-c946527898e6";
//        List<Patient> patientList = new LinkedList<>();
//        DBCollection collection = mongoTemplate.getCollection(mongoId);
//        DBCursor dbObjects = collection.find();
//        while (dbObjects.hasNext()) {
//            DBObject next = dbObjects.next();
//            String patient_sn = (String) next.get("patient_sn");
//            List<DBObject> array = (List<DBObject>) next.get("patient_info");
//            String nationality = (String) array.get(0).get("NATIONALITY");
//            String efhnic = (String) array.get(0).get("ETHNIC");
//            String maritalStatus = (String) array.get(0).get("MARITAL_STATUS");
//            String gender = (String) array.get(0).get("GENDER");
//            System.out.println(array);
//        }
//
//    }
//
//
//}
