package com.gennlife.rws.service;

import com.gennlife.rws.entity.PcientificResearchType;

import java.util.List;

/**
 * @author liumingxin
 * @create 2018 07 17:34
 * @desc
 **/
public interface PcientificResearchTypeService {
    /**
     * 查找 科研类型
     * @return
     */
    List<PcientificResearchType> selectPcientificResearchTypeAll();
}
