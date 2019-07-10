package com.gennlife;

import com.gennlife.rws.service.CortrastiveAnalysisService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

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
        List<String> tmp = new ArrayList<>();
        tmp.add("42bf36391a2d4aa581717379c6095045");
        tmp.add("861006db76284105add381c1bbf4cd8f");
        tmp.forEach( x ->cortrastiveAnalysisService.deleteActiveIndexVariable(x));
    }

}
