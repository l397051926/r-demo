package com.gennlife.rws.util;

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
        String tmp = groupDatas.stream().map( x -> "'"+x.getPatientDocId()+"'").collect(joining(","));
        return " "+ IndexContent.getPatientDocId(crfId)+" IN ("+tmp+") ";
    }
    public static String getSqlByPatSnsForOne(List<String> groupDatas, String crfId){
        String tmp = groupDatas.stream().map( x -> "'"+x+"'").collect(joining(","));
        return " "+ IndexContent.getPatientDocId(crfId)+" IN ("+tmp+") ";
    }
}
