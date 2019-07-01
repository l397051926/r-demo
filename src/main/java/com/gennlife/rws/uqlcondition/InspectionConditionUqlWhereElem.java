package com.gennlife.rws.uqlcondition;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.util.ApplicationContextHelper;
import com.gennlife.rws.util.HttpUtils;
import com.gennlife.rws.util.StringUtils;
import com.gennlife.rws.util.TransPatientSql;

import java.util.Arrays;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * @author lmx
 * @create 2019 28 19:04
 * @desc
 **/
public class InspectionConditionUqlWhereElem extends UqlWhereElem {
    private String str;
    private String crfId;
    private String projectId;
    private String initPatientSql;

    public InspectionConditionUqlWhereElem(String str, String operator, String projectId,String crfId,String initPatientSql) {
        super(operator);
        this.str = str;
        this.crfId = crfId;
        this.projectId = projectId;
        this.initPatientSql = initPatientSql;
    }

    @Override
    public void execute() {
        HttpUtils client = ApplicationContextHelper.getBean(HttpUtils.class);
        String sql = "SELECT patient_info.DOC_ID as pSn,group_concat(sub_inspection.INSPECTION_SN,',') as result FROM " +
            IndexContent.getIndexName(crfId,projectId)+" WHERE "+str + " AND join_field = 'sub_inspection' " +  (StringUtils.isEmpty(initPatientSql) ? "" : " AND " + initPatientSql ) + IndexContent.getGroupBy(crfId);
        JSONArray source = new JSONArray();
        String tmp = client.querySearch(projectId,sql,1,Integer.MAX_VALUE-1,null,source,true);
        Set<String> visSet =JSON.parseObject(tmp)
            .getJSONObject("hits")
            .getJSONArray("hits")
            .stream()
            .map(JSONObject.class::cast)
            .map(o -> o.getJSONObject("_source"))
            .map(o -> o.getJSONObject("select_field"))
            .flatMap(o -> Arrays.stream(o.getString("result").split(",")))
            .collect(toSet());
        result =  " inspection_reports.INSPECTION_SN " + TransPatientSql.transForExtContain(visSet);
    }
}
