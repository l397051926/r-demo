package com.gennlife.rws.vo;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.packagingservice.arithmetic.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class SearchUqlVisitSn {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchUqlVisitSn.class);
    private static final String searchUqlVisistSnPath = "/config/search_uql_visit_sn.json";
    private static JSONObject CONDITION = null;

    public static String getSearchUqlVisitSn(String key) {
        try {
            if (Objects.isNull(CONDITION)) {
                String fileString = FileUtil.readString(new BufferedReader(new InputStreamReader(SearchUqlVisitSn.class.getResourceAsStream(searchUqlVisistSnPath))));
                CONDITION = JSONObject.parseObject(fileString);
            }
            return CONDITION.getString(key);
        }catch (IOException e ){
            LOGGER.error("读取文件发生问题");
            return null;
        }
    }

    public static void main(String[] args) {
        getSearchUqlVisitSn("ff");
    }

}
