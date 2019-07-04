package com.gennlife;

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
@Ignore
public class patientSetServiceTest {
    @Autowired
    private PatientSetService patientSetService;
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
        Integer num = 0;
        patientSetService.savePatientSetGroupBlock(patientSetId,allPats,num);
    }

}
