package com.gennlife;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.dao.PatientsIdSqlMapMapper;
import com.gennlife.rws.service.PatientSetService;
import com.gennlife.rws.service.SearchByuqlService;
import com.gennlife.rws.util.AjaxObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class patientSetServiceTest {
    @Autowired
    private PatientSetService patientSetService;
    @Autowired
    private PatientsIdSqlMapMapper patientsIdSqlMapMapper;
    @Autowired
    private SearchByuqlService searchByuqlService;

    @Test
    public void testDemo() {
    }

    @Test
    public void savePatientSetGroupBlock() {
        String patientSetId = "aaaa";
        Set<String> allPats = new HashSet<>();
        allPats.add("qqq");
        allPats.add("www");
        allPats.add("eee");
        allPats.add("rrr");
        patientSetService.savePatientSetGroupBlock(patientSetId, allPats, null);
    }

    @Test
    public void getSizeForPatientSet() {
        String patientSetId = "aaaa";
        System.out.println(patientSetService.getPatientSetLocalCountByExclude(patientSetId,0));
    }

    @Test
    public void updateExport() {
        String patientSetId = "aaaa";
        patientsIdSqlMapMapper.updateExportByPatientSetId(patientSetId, 1);
    }

    @Test
    public void getPatientSetLocalSqlSet() {
        String patientSetId = "aaaa";
        System.out.println(patientSetService.getPatientSetLocalSql(patientSetId));
    }

    @Test
    public void getPatientSetDataList() {
        String param = "{\"crfId\":\"EMR\",\"projectId\":\"390671b116bd486292dda5327e2463c1\",\"patientsSetId\":\"cd49af5f79514f0ca000cf376a19ec96\",\"pageNum\":2,\"pageSize\":10,\"showColumns\":[{\"id\":\"PATIENT_SN\",\"name\":\"患者编号\"},{\"id\":\"GROUP_NAME\",\"name\":\"所属分组\"},{\"id\":\"GENDER\",\"name\":\"性别\"},{\"id\":\"BLOOD_ABO\",\"name\":\"ABO血型名称\"},{\"id\":\"BLOOD_RH\",\"name\":\"RH血型\"},{\"id\":\"BIRTH_PLACE\",\"name\":\"出生地\"},{\"id\":\"BIRTH_DATE\",\"name\":\"出生日期\"},{\"id\":\"NATIONALITY\",\"name\":\"国籍\"},{\"id\":\"MARITAL_STATUS\",\"name\":\"婚姻\"},{\"id\":\"NATIVE_PLACE\",\"name\":\"籍贯\"},{\"id\":\"ETHNIC\",\"name\":\"民族\"},{\"id\":\"EDUCATION_DEGREE\",\"name\":\"文化程度\"},{\"id\":\"OCCUPATION\",\"name\":\"职业\"},{\"id\":\"PATIENT_TYPE\",\"name\":\"患者类型\"}]}";
        JSONObject params = JSONObject.parseObject(param);
        JSONArray showColumns = params.getJSONArray("showColumns");
        String patientsSetId = "aaaa";
        String projectId = params.getString("projectId");
        Integer pageNum = 1;
        String crfId = params.getString("crfId");
        Integer type = 1;
        Integer pageSize = 2;
        //查找活动和指标
        JSONArray actives = new JSONArray();
        searchByuqlService.getPatientSnsByAll(patientsSetId, projectId, showColumns, actives, pageNum, pageSize, type, crfId);
    }
}
