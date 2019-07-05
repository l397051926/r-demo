package com.gennlife;

import com.gennlife.rws.dao.PatientsIdSqlMapMapper;
import com.gennlife.rws.service.PatientSetService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class patientSetServiceTest {
    @Autowired
    private PatientSetService patientSetService;
    @Autowired
    PatientsIdSqlMapMapper patientsIdSqlMapMapper;
    @Test
    public void testDemo(){

    }
    @Test
    public void savePatientSetGroupBlock(){
        String patientSetId = "aaaa";
        Set<String> allPats = new HashSet<>();
        allPats.add("qqq");
        allPats.add("www");
        allPats.add("eee");
        allPats.add("rrr");
        patientSetService.savePatientSetGroupBlock(patientSetId,allPats,null);
    }
    @Test
    public void getSizeForPatientSet(){
        String patientSetId = "aaaa";
        System.out.println(patientSetService.getPatientSetLocalCount(patientSetId));
    }
    @Test
    public void updateExport(){
        String patientSetId = "aaaa";
        patientsIdSqlMapMapper.updateExportByPatientSetId(patientSetId,1);
    }

}
