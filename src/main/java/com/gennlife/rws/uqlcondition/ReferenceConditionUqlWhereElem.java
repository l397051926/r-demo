package com.gennlife.rws.uqlcondition;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.darren.collection.keypath.KeyPath;
import com.gennlife.rws.schema.AbstractFieldAnalyzer;
import com.gennlife.rws.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.gennlife.rws.service.impl.SearchCrfByuqlServiceImpl.SCHEMAS;
import static java.util.stream.Collectors.*;

public class ReferenceConditionUqlWhereElem extends UqlWhereElem {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceConditionUqlWhereElem.class);

    String ref;
    String field;
    String group;
    String indexName;
    String visits;
    String adjoint;
    List<String> nears;
    boolean crf;
    String visitInfo;
    String patientInfo;
    String crfId;

    public ReferenceConditionUqlWhereElem(String ref, List<String> nears, String field, String indexName, String operator, boolean crf,String crfId) {
        super(operator);
        this.adjoint = adjoint;
        this.ref = ref;
        this.nears = nears;
        this.indexName = indexName;
        this.field = field;
        this.crf = crf;
        this.crfId = crfId;
        if (crf) {
            visitInfo = "visitinfo";
            patientInfo = "patient_info.patient_basicinfo";
            AbstractFieldAnalyzer schema = SCHEMAS.get(crfId);
            KeyPath path = KeyPath.compile(field);
            if (schema.isPackagedField(path.join("."))) {
                this.group = path.removeLast(2).firstAsString();
            } else {
                this.group = visitInfo;
            }
        } else {
            visitInfo = "visit_info";
            patientInfo = "patient_info";
            String parts[] = field.split("\\.");
            this.group = parts[parts.length - 2];
            if (parts.length > 2  && parts[0].equals("medical_record_home_page") && parts[1].equals("operation")) {
                this.group = "medical_record_home_page.operation";
            }
            if (parts[0].equals("sub_inspection")){
                this.group = "sub_inspection";
            }
        }
    }

    public String group() {
        return group;
    }

    @Override
    public void execute() {
        HttpUtils client = ApplicationContextHelper.getBean(HttpUtils.class);
        String querySql = "SELECT t1.count,t2.jocount,t1.values, t2.condition FROM ( SELECT  count(" + visitInfo + ".DOC_ID) as count,all_value(" + group + ".DOC_ID, " + field + ") as values,"
                + " " + patientInfo + ".DOC_ID as pSn FROM " + indexName + " WHERE " + field + " IS NOT NULL "
                + "GROUP BY " + patientInfo + ".DOC_ID ) AS t1 JOIN ( " + ref + " ) AS t2 ON t1.pSn = t2.pSn";
        if(StringUtils.isNotEmpty(this.adjoint)){
            querySql = querySql +" adjoint t2."+this.adjoint;
        }
        JSONObject query = new JSONObject()
                .fluentPut("hospitalID", "public")
                .fluentPut("indexName", indexName)
                .fluentPut("page", 1)
                .fluentPut("query", querySql)
                .fluentPut("search_dimension", 0)
                .fluentPut("size", Integer.MAX_VALUE - 1)
                .fluentPut("fetchAllGroupByResult",true)
                .fluentPut("source", nears.stream().map(s -> s + " AS result").collect(toCollection(JSONArray::new)));
        query.put("source_filter","t1.count>0 and t2.jocount>0");
        String response = null;
        Long startTime = System.currentTimeMillis();
        try {
            response = GzipUtil.uncompress(client.httpPost(GzipUtil.compress(query.toJSONString()), client.getEsSearchUqlCompress()).trim());
        } catch (IOException e) {
            LOGGER.error("请求uql发生异常");
        }
        LOGGER.info("搜索 -- 引用 耗时："+(System.currentTimeMillis() - startTime));
        List<String> visitSns = JSON.parseObject(response)
                .getJSONObject("hits")
                .getJSONArray("hits")
                .stream()
                .map(JSONObject.class::cast)
                .map(o -> o.getJSONObject("_source"))
                .map(o -> o.getJSONObject("select_field"))
                .flatMap(o -> o.getJSONArray("result")
                        .stream()
                        .map(String.class::cast))
                .distinct()
                .collect(toList());
        if(visitSns ==null || visitSns.size() == 0){
            result = group + ".DOC_ID IN ('') ";
        }else {
            result = group + ".DOC_ID " + TransPatientSql.transForExtContain(visitSns);
        }
    }

    public String getVisits(){
        return visits;
    }
    public String getField(){return field;}
}
