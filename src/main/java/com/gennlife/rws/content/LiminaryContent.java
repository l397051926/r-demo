package com.gennlife.rws.content;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author lmx
 * @create 2019 04 17:15
 * @desc
 **/
@Component
@PropertySource("classpath:general.properties")
public class LiminaryContent {
    @Value("${pre.liminary.maxMember}")
    private Integer maxMember;

    @Value("${pre.liminary.groupBlock}")
    private Integer groupBlock;

    public Integer getMaxMember() {
        return maxMember;
    }

    public void setMaxMember(Integer maxMember) {
        this.maxMember = maxMember;
    }

    public Integer getGroupBlock() {
        return groupBlock;
    }

    public void setGroupBlock(Integer groupBlock) {
        this.groupBlock = groupBlock;
    }
}
