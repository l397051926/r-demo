package com.gennlife.rws.uqlcondition;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.darren.collection.keypath.KeyPath;
import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

/**
 * @author liumingxin
 * @create 2018 08 11:00
 * @desc
 **/
public class IndexReferenceConditionUqlWhereElem extends UqlWhereElem {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexReferenceConditionUqlWhereElem.class);

    String value;
    String indexName;
    String whereCondition;
    String havingSign;
    Object havingValue;
    DataType type;
    boolean not = false;
    String activeOhterResult;
    boolean isActiveFirst = false;
    String countValue ;
    boolean crf = false;
    String visitInfo = "visit_info";
    String crfId ;

    public IndexReferenceConditionUqlWhereElem(String value, String indexName, String whereCondition, String havingSign, Object havingValue, DataType type,String countValue, boolean crf,String crfId) {
        super(null);
        this.value = value;
        this.indexName = indexName;
        this.whereCondition = whereCondition;
        this.havingSign = havingSign;
        this.havingValue = havingValue;
        this.type = type;
        this.countValue = countValue;
        this.crf = crf;
        this.crfId = crfId;
        this.visitInfo = crf ? "visitinfo" : "visit_info";
    }
    public IndexReferenceConditionUqlWhereElem(String value, String indexName,String whereCondition, String havingSign, Object havingValue, DataType type,boolean not,
                                               String activeOhterResult,boolean isActiveFirst,String countValue,boolean crf,String crfId) {
        super(null);
        this.value = value;
        this.indexName = indexName;
        this.whereCondition = whereCondition;
        this.havingSign = havingSign;
        this.havingValue = havingValue;
        this.type = type;
        this.not = not;
        this.activeOhterResult =activeOhterResult;
        this.isActiveFirst = isActiveFirst;
        this.countValue = countValue;
        this.crf = crf;
        this.visitInfo = crf ? "visitinfo" : "visit_info";
        this.crfId = crfId;
    }

    @Override
    public void execute() {
        HttpUtils client = ApplicationContextHelper.getBean(HttpUtils.class);
        if(StringUtils.isEmpty(countValue)) {
            countValue=" count(" + visitInfo + ".DOC_ID) AS jocount ";
        }
        if (!crf && StringUtils.isNotEmpty(activeOhterResult) && " distinct_count(inspection_reports.DOC_ID) as jocount ".equals(countValue)) {
            countValue=" count(" + visitInfo + ".DOC_ID) AS jocount ";
        }
        if (!crf && StringUtils.isNotEmpty(activeOhterResult) && " distinct_count(medical_record_home_page.DOC_ID) as jocount ".equals(countValue)) {
            countValue=" count(" + visitInfo + ".DOC_ID) AS jocount ";
        }
        String sql = "SELECT " + value + " AS value ," + countValue +" FROM " + indexName + " WHERE " + whereCondition;// + " GROUP BY visit_info.PATIENT_SN";
        JSONObject query = new JSONObject()
            .fluentPut("hospitalID", "public")
            .fluentPut("indexName", indexName)
            .fluentPut("page", 1)
            .fluentPut("query", sql)
            .fluentPut("search_dimension", 0)
            .fluentPut("size", Integer.MAX_VALUE - 1)
            .fluentPut("fetchAllGroupByResult",true)
            .fluentPut("source", new JSONArray().fluentAdd(IndexContent.getPatientDocId(crfId)));
        if (havingSign != null) {
            if (isActiveFirst) {
                query.put("source_filter", type.serialize("1", havingSign, havingValue,false));
            } else {
                query.put("source_filter", type.serialize("value", havingSign, havingValue,false));
            }
        }
        String response = null;
        Long startTime = System.currentTimeMillis();
        try {
            response = GzipUtil.uncompress(client.httpPost(GzipUtil.compress(query.toJSONString()), client.getEsSearchUqlCompress()).trim());
        } catch (IOException e) {
            LOGGER.error("请求uql发生异常");
        }
        LOGGER.info("搜索 -- 引用 耗时："+(System.currentTimeMillis() - startTime));
//        LOGGER.info("查询引用指标的数据 query："+query.toJSONString());
        Set<String> patients = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(JSON.parseObject(response))
            .stream()
            .map(String.class::cast)
            .collect(toSet());

        if(not){
            if (patients.isEmpty()) {//visitInfo +....
                result = IndexContent.getPatientDocId(crfId)+TransPatientSql.transForExtContain("'"+this.activeOhterResult+"'");
            } else {
                result = IndexContent.getPatientDocId(crfId)+TransPatientSql.transForExtContain(" '" + patients.stream().collect(joining("$")) +"$"+this.activeOhterResult+ "'");
            }
        }else {
            if (patients.isEmpty()) {
                result = IndexContent.getPatientDocId(crfId)+" IN ('')";
            } else {
                result = IndexContent.getPatientDocId(crfId)+TransPatientSql.transForExtContain(patients);
            }
        }
    }

}
