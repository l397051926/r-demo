package com.gennlife;

import com.gennlife.rws.service.ProjectService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
//@Ignore
public class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @Test
    public void testDemo(){

    }
    @Test
    public void deleteIndexByDelProject(){
        projectService.deleteProjectDelIndex();
    }
}
