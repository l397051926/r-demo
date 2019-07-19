package com.gennlife.rws.util;

import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.content.SeparatorContent;
import com.gennlife.rws.entity.GroupData;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.joining;

/**
 * @author
 * @create 2018 29 13:41
 * @desc
 **/
public class TransPatientSql {

    public static final String EXT_CONTAIN = "EXT_CONTAIN";

    public static String getPatientSnSql(String sql, String crfId) {
        String[] array = sql.split(SeparatorContent.getRegexVartivalBar());
        if (array.length == 1) {
            return " " + IndexContent.getPatientSn(crfId) + " IN ('" + sql + "') ";
        }
        String tmp = Arrays.stream(array).map(m -> "'" + m + "'").collect(joining(","));
        return " " + IndexContent.getPatientSn(crfId) + " IN (" + tmp + ") ";
    }

    public static String getUncomPatientSnSql(String sql) {
        try {
            return GzipUtil.uncompress(sql);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getAllPatientSql(String sql, String crfId) {
        String[] array = sql.split(SeparatorContent.getRegexVartivalBar());
        //TODO 判定方式太绝对了 后期修改 统一 docId
        if (array.length > 0 && array[0].startsWith("pat_")) {
            return " " + IndexContent.getPatientInfoPatientSn(crfId) + " " + transForExtContainForArray(array) + " ";
        } else {
            return " " + IndexContent.getPatientDocId(crfId) + " " + transForExtContainForArray(array) + " ";
        }
    }

    public static String getAllPatientSqlForList(List<String> sqlList, String crfId) {
        return " " + IndexContent.getPatientInfoPatientSn(crfId) + " " + transForExtContain(sqlList) + " ";
    }

    public static String getPatientDocIdSql(List<String> sqlList, String crfId) {
        return " " + IndexContent.getPatientDocId(crfId) + " " + transForExtContain(sqlList) + " ";
    }

    public static String getSqlByPatSns(List<GroupData> groupDatas, String crfId) {
        String tmp = getExtSqlForGrouPdata(groupDatas);
        return " " + IndexContent.getPatientDocId(crfId) + transForExtContain(tmp) + " ";
    }

    public static String getExtSqlForGrouPdata(List<GroupData> groupDatas) {
        return " '" + groupDatas.stream().map(x -> x.getPatientDocId()).collect(joining("$")) + "'";
    }

    public static String transForExtContain(Set<String> patients) {
        return " " + EXT_CONTAIN + "(" + getExtSet_Sql(patients) + ",'$') ";
    }

    public static String transForExtContain(List<String> patients) {
        return " " + EXT_CONTAIN + "(" + getExtSet_Sql(patients) + ",'$') ";
    }

    public static String transForExtContainForGroupData(List<GroupData> groupDataList) {
        return " " + EXT_CONTAIN + "(" + getExtSet_SqlForGroupData(groupDataList) + ",'$') ";
    }

    public static String transForExtContain(String sql) {
        return " " + EXT_CONTAIN + "(" + sql + ",'$') ";
    }

    public static String transForExtContainForArray(String[] arrs) {
        return " " + EXT_CONTAIN + "(" + getExtSet_SqlForArray(arrs) + ",'$') ";
    }

    public static String getExtSet_Sql(Set<String> patients) {
        if (patients == null || patients.size() == 0) {
            return "''";
        }
        return "'" + String.join("$", patients) + "'";
    }

    public static String getExtSet_Sql(List<String> patients) {
        if (patients == null || patients.size() == 0) {
            return "''";
        }
        return "'" + String.join("$", patients) + "'";
    }

    public static String getExtSet_SqlForGroupData(List<GroupData> groupDataList) {
        if (groupDataList == null || groupDataList.size() == 0) {
            return "''";
        }
        return "'" + groupDataList.stream().map(x -> x.getPatientSn()).collect(joining("$")) + "'";
    }

    public static String getExtSet_SqlForArray(String[] arrs) {
        if (arrs == null || arrs.length == 0) {
            return "''";
        }
        return "'" + Arrays.stream(arrs).collect(joining("$")) + "'";
    }


}
