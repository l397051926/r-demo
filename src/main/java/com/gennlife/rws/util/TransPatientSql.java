package com.gennlife.rws.util;

import com.alibaba.fastjson.JSON;
import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.entity.GroupData;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

/**
 * @author liumingxin
 * @create 2018 29 13:41
 * @desc
 **/
public class TransPatientSql {

    public static final String EXT_CONTAIN = "EXT_CONTAIN";

    public static String getPatientSql(String sql){
        String[] array = sql.split("\\|");
        String tmp = Arrays.stream(array).map(m -> "'"+m+"'").collect(joining(","));
        return " ("+tmp+") ";
    }

    public static Set<String> getSetPatientSql(String sql){
        return Arrays.stream(sql.split("\\|")).collect(toSet());
    }

    public static String getPatientSnSql(String sql){
        String[] array = sql.split("\\|");
        if(array.length==1){
            return " visit_info.PATIENT_SN IN ('"+sql+"') ";
        }
        String tmp = Arrays.stream(array).map(m -> "'"+m+"'").collect(joining(","));
        return " visit_info.PATIENT_SN IN ("+tmp+") ";
    }

    public static String getPatientSnSql(String sql,String crfId){
        String[] array = sql.split("\\|");
        if(array.length==1){
            return " "+ IndexContent.getPatientSn(crfId)+" IN ('"+sql+"') ";
        }
        String tmp = Arrays.stream(array).map(m -> "'"+m+"'").collect(joining(","));
        return " "+ IndexContent.getPatientSn(crfId)+" IN ("+tmp+") ";
    }

    public static String getUncomPatientSnSql(String sql){
        try {
            return GzipUtil.uncompress(sql);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }


    public static String getAllPatientSql(String sql, String crfId) {
        String[] array = sql.split("\\|");
        if(array.length==1){
            return " "+ IndexContent.getPatientInfoPatientSn(crfId)+" IN ('"+sql+"') ";
        }
        String tmp = Arrays.stream(array).map(m -> "'"+m+"'").collect(joining(","));
        return " "+ IndexContent.getPatientInfoPatientSn(crfId)+" IN ("+tmp+") ";
    }

    public static String getAllPatientSql(String sql) {
        String[] array = sql.split("\\|");
        if(array.length==1){
            return " patient_info.PATIENT_SN IN ('"+sql+"') ";
        }
        String tmp = Arrays.stream(array).map(m -> "'"+m+"'").collect(joining(","));
        return  " patient_info.PATIENT_SN IN ("+tmp+") ";
    }

    public static String getSqlByPatSns(List<GroupData> groupDatas, String crfId){
        String tmp = getExtSqlForGrouPdata(groupDatas);
        return " "+ IndexContent.getPatientDocId(crfId)+transForExtContain(tmp) + " ";
    }
    public static String getSqlByPatSnsForIn(List<GroupData> groupDatas, String crfId){
        String tmp = getSqlForGrouPdata(groupDatas);
        return " "+ IndexContent.getPatientDocId(crfId)+" IN ("+tmp+") ";
    }
    public static String getSqlForGrouPdata(List<GroupData> groupDatas){
      return  groupDatas.stream().map( x -> "'"+x.getPatientDocId()+"'").collect(joining(","));
    }
    public static String getExtSqlForGrouPdata(List<GroupData> groupDatas){
        return  " '" + groupDatas.stream().map( x -> x.getPatientDocId()).collect(joining("$")) + "'";
    }

    public static String transForInSql(Set<String> patients){
        return  " IN ("+getSet_Sql(patients)+")";
    }
    public static String transForInSql(String sql){
        return  " IN ("+sql+")";
    }
    public static String transForExtContain(Set<String> patients){
        return " " + EXT_CONTAIN + "(" +getExtSet_Sql(patients) +",'$') ";
    }
    public static String transForExtContain(List<String> patients){
        return " " + EXT_CONTAIN + "(" +getExtSet_Sql(patients) +",'$') ";
    }
    public static String transForExtContainForGroupData(List<GroupData> groupDataList){
        return " " + EXT_CONTAIN + "(" +getExtSet_SqlForGroupData(groupDataList) +",'$') ";
    }
    public static String transForExtContain(String sql){
        return " " + EXT_CONTAIN + "(" +sql +",'$') ";
    }
    public static String transForExtContainForDataType(Object val){
        return " " + EXT_CONTAIN + "(" + JSON.parseArray(val.toString()).stream().map(String.class::cast).collect(joining("$"))  +",'$') ";
    }
    public static String getSet_Sql(Set<String> patients){
        return patients.stream().map(s -> "'" + s + "'").collect(joining(","));
    }
    public static String getExtSet_Sql(Set<String> patients){
        return "'" + String.join("$",patients) + "'";
    }
    public static String getExtSet_Sql(List<String> patients){
        return "'" + String.join("$",patients) + "'";
    }
    public static String getExtSet_SqlForGroupData(List<GroupData> groupDataList){
        if(groupDataList.size() == 0){
            return "''";
        }
        return "'" + groupDataList.stream().map(x -> x.getPatientSn()).collect(joining("$")) + "'";
    }

}
