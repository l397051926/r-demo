package com.gennlife.rws.service.impl;

import com.gennlife.rws.dao.PcientificResearchTypeMapper;
import com.gennlife.rws.entity.PcientificResearchType;
import com.gennlife.rws.service.PcientificResearchTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liumingxin
 * @create 2018 07 17:34
 * @desc
 **/
@Service
public class PcientificResearchTypeServiceImpl implements PcientificResearchTypeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PcientificResearchTypeServiceImpl.class);
    @Autowired
    private PcientificResearchTypeMapper pcientificResearchTypeMapper;

    @Override
    public List<PcientificResearchType> selectPcientificResearchTypeAll() {
        return  pcientificResearchTypeMapper.selectAll();
    }
}
