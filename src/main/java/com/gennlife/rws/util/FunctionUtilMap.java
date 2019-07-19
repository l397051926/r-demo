package com.gennlife.rws.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liumingxin
 * @create 2018 10 10:52
 * @desc
 **/
public class FunctionUtilMap {

    private static final Map<String, String> uqlFunctionMap;

    static {
        uqlFunctionMap = new HashMap<>();
        uqlFunctionMap.put("all", "");
        uqlFunctionMap.put("first", "first_date");
        uqlFunctionMap.put("last", "last_date");
        uqlFunctionMap.put("min", "min");
        uqlFunctionMap.put("max", "max");
        uqlFunctionMap.put("index", "any_date");//第几次
        uqlFunctionMap.put("reverseindex", "any_date");//倒数第几次
        uqlFunctionMap.put("previousList", "");//前n次
        uqlFunctionMap.put("afterList", "");//后n次
        uqlFunctionMap.put("avg", "avg");
        uqlFunctionMap.put("sum", "sum");
    }

    public static String getUqlFunction(String function, String num, String keyWords) {
        String uqlFunction = uqlFunctionMap.get(function);
        if (StringUtils.isEmpty(num)) {
            return uqlFunction + "(" + keyWords + ")";
        } else {
            if ("index".equals(function)) {
                return uqlFunction + "(" + keyWords + "," + num + ")";
            } else {
                return uqlFunction + "(" + keyWords + ",-" + num + ")";
            }
        }
    }

    public static String getUqlFunction(String function, String num, String keyWords, String indexType, String indexDate) {
        switch (function) {
            case "first":
                return getFirstFunction(function, keyWords, indexDate, indexType);
            case "last":
                return getLastFunction(function, keyWords, indexDate, indexType);
            case "index":
                return getIndexFunction(function, keyWords, indexDate, indexType, num);
            case "reverseIndex":
                return getReverseIndex(function, keyWords, indexDate, indexType, num);
            case "all":
                return getAllActive(function, keyWords, indexDate, indexType, num);
            default:
                return getUqlFunction(function, num, keyWords);
        }
    }

    private static String getAllActive(String function, String keyWords, String indexDate, String indexType, String num) {
        return "group_concat(" + keyWords + ",',') ";
    }

    private static String getReverseIndex(String function, String keyWords, String indexDate, String indexType, String num) {
        if (indexType.contains("date") && !"visit_info.VISIT_SN".equals(keyWords) && !"visitinfo.DOC_ID".equals(keyWords)) {
            return "any_date(" + keyWords + ",-" + num + ")";
        } else {
            return "any_value(" + keyWords + "," + indexDate + ",-" + num + ")";
        }
    }

    private static String getIndexFunction(String function, String keyWords, String indexDate, String indexType, String num) {
        if (indexType.contains("date") && !"visit_info.VISIT_SN".equals(keyWords) && !"visitinfo.DOC_ID".equals(keyWords)) {
            return "any_date(" + keyWords + "," + num + ")";
        } else {
            return "any_value(" + keyWords + "," + indexDate + "," + num + ")";
        }
    }

    private static String getLastFunction(String function, String keyWords, String indexDate, String indexType) {
        if (indexType.contains("date") && !"visit_info.VISIT_SN".equals(keyWords) && !"visitinfo.DOC_ID".equals(keyWords)) {
            return "last_date(" + keyWords + ")";
        } else {
            return "last_value(" + keyWords + "," + indexDate + ")";
        }
    }

    private static String getFirstFunction(String function, String keyWords, String indexDate, String indexType) {
        if (indexType.contains("date") && !"visit_info.VISIT_SN".equals(keyWords) && !"visitinfo.DOC_ID".equals(keyWords)) {
            return "first_date(" + keyWords + ")";
        } else {
            return "first_value(" + keyWords + "," + indexDate + ")";
        }
    }

}
