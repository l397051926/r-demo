package com.gennlife;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.service.SearchByuqlService;
import com.gennlife.rws.util.AjaxObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class SearchTest {

    @Autowired

    private SearchByuqlService searchByuqlService;
    @Test
    public void testDemo() {

    }
    @Test
    public void aggregationTest() throws IOException {
        String param = "{\"projectId\":\"390671b116bd486292dda5327e2463c1\",\"patientSetId\":\"aaaa\",\"aggregationTeam\":[{\"domain_name\":\"ETHNIC\",\"domain_desc\":\"民族\",\"domain_path\":\"patient_info.ETHNIC\",\"domain_id\":\"efhnic\"},{\"domain_name\":\"NATIONALITY\",\"domain_desc\":\"国籍\",\"domain_path\":\"patient_info.NATIONALITY\",\"domain_id\":\"nationality\"},{\"domain_name\":\"MARITAL_STATUS\",\"domain_desc\":\"婚姻\",\"domain_path\":\"patient_info.MARITAL_STATUS\",\"domain_id\":\"maritalStatus\"},{\"domain_name\":\"GENDER\",\"domain_desc\":\"性别\",\"domain_path\":\"patient_info.GENDER\",\"domain_id\":\"gender\"}],\"crfId\":\"EMR\"}";
        JSONObject params = JSONObject.parseObject(param);
        String patientSetId = params.getString("patientSetId");
        JSONArray aggregationTeam = params.getJSONArray("aggregationTeam");
        String projectId = params.getString("projectId");
        String crfId = params.getString("crfId");
        AjaxObject object = searchByuqlService.getAggregationAll(patientSetId,aggregationTeam,projectId,crfId);
        System.out.println();
    }
    @Test
    public void getInitialSqlTest(){
        JSONArray array = new JSONArray().fluentAdd("5c11da9c13294372bdd956f8518ab683");
        searchByuqlService.getInitialSQLTmp(null,null,null,array,null,"EMR");
    }
}
