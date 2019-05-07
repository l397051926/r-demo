package com.gennlife;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.gennlife.rws.dao")
@EnableTransactionManagement
@EnableRetry
@ServletComponentScan({"com.gennlife.rws.datasource","com.gennlife.rws.redis"})
public class RwsServiceApplication extends SpringBootServletInitializer{

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(RwsServiceApplication.class);
    }
    public static void main(String[] args) {
        SpringApplication.run(RwsServiceApplication.class, args);
    }

}
