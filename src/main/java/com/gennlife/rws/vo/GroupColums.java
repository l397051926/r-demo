package com.gennlife.rws.vo;

import com.alibaba.fastjson.JSONArray;
import com.gennlife.rws.util.StringUtils;


public class GroupColums {
    private static final String PATIENT_SET_COLUMN ="[{\"id\":\"PATIENT_SN\",\"name\":\"患者编号\"},{\"id\":\"GENDER\",\"name\":\"性别\"},{\"id\":\"BLOOD_ABO\",\"name\":\"ABO血型名称\"},{\"id\":\"BLOOD_RH\",\"name\":\"RH血型\"},{\"id\":\"BIRTH_PLACE\",\"name\":\"出生地\"},{\"id\":\"BIRTH_DATE\",\"name\":\"出生日期\"},{\"id\":\"NATIONALITY\",\"name\":\"国籍\"},{\"id\":\"MARITAL_STATUS\",\"name\":\"婚姻\"},{\"id\":\"NATIVE_PLACE\",\"name\":\"籍贯\"},{\"id\":\"ETHNIC\",\"name\":\"民族\"},{\"id\":\"EDUCATION_DEGREE\",\"name\":\"文化程度\"},{\"id\":\"OCCUPATION\",\"name\":\"职业\"},{\"id\":\"PATIENT_TYPE\",\"name\":\"患者类型\"}]";
    private static final String PATIENT_SET_COLUMN_CRF ="[{\"id\":\"PATIENT_SN\",\"name\":\"患者编号\"},{\"id\":\"GENDER\",\"name\":\"性别\"},{\"id\":\"BLOOD_ABO\",\"name\":\"ABO血型名称\"},{\"id\":\"BLOOD_RH\",\"name\":\"RH血型\"},{\"id\":\"BIRTH_PLACE\",\"name\":\"出生地\"},{\"id\":\"DATE_OF_BIRTH\",\"name\":\"出生日期\"},{\"id\":\"NATIONALITY\",\"name\":\"民族\"},{\"id\":\"MARITAL_STATUS\",\"name\":\"婚姻\"},{\"id\":\"NATIVE_PLACE\",\"name\":\"籍贯\"},{\"id\":\"EDUCATION_DEGREE\",\"name\":\"文化程度\"},{\"id\":\"OCCUPATION\",\"name\":\"职业\"},{\"id\":\"PATIENT_TYPE\",\"name\":\"患者类型\"}]";
    private static final String GROUP_COLUMN = "[{\"id\":\"PATIENT_SN\",\"name\":\"病人编号\"},{\"id\":\"GENDER\",\"name\":\"性别\"},{\"id\":\"BIRTH_DATE\",\"name\":\"出生日期\"},{\"id\":\"BLOOD_ABO\",\"name\":\"ABO血型名称\"},{\"id\":\"BLOOD_RH\",\"name\":\"RH血型\"},{\"id\":\"NATIONALITY\",\"name\":\"国籍\"}],\"indexColumns\":[{\"id\":\"13DCEDA5D4994618B7E40D0412AE0FDC_tmp\",\"name\":\"\"}]";

    public static JSONArray getPatientSetColumnJSON(String crfId){
        if(StringUtils.isEmpty(crfId) || "EMR".equals(crfId)){
            return JSONArray.parseArray(PATIENT_SET_COLUMN);
        }else {
            return JSONArray.parseArray(PATIENT_SET_COLUMN_CRF);
        }
    }
    public static JSONArray getGroupColumnJSON(){
        return JSONArray.parseArray(GROUP_COLUMN);
    }

}
