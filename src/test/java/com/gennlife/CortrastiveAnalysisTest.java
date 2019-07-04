package com.gennlife;

import com.gennlife.rws.service.CortrastiveAnalysisService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class CortrastiveAnalysisTest {
    @Autowired
    private CortrastiveAnalysisService cortrastiveAnalysisService;
    @Test
    public void testDemo(){

    }

    @Test
    public void deletCortRedisMapByProjectId(){
        String projectId = "861006db76284105add381c1bbf4cd8f";
        cortrastiveAnalysisService.deleteActiveIndexVariable(projectId);
    }

}
