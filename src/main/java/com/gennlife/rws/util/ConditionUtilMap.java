package com.gennlife.rws.util;

import com.alibaba.fastjson.JSONArray;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liumingxin
 * @create 2018 11 9:25
 * @desc
 **/
public class ConditionUtilMap {
    private static final Map<String, String> conditionUtilMap;

    private static final String NUMBER_ADD = "refNumberScope#NUMBER_ADD";
    private static final String NUMBER_ADD_TO = "refNumberScope#NUMBER_ADD#=";
    private static final String NUMBER_SUB = "refNumberScope#NUMBER_SUB";
    private static final String NUMBER_SUB_TO = "refNumberScope#NUMBER_SUB#=";

    private static final String NUMBER_RAISEPE_RCENT = "refNumberScope#NUMBER_RAISE_PERCENT";
    private static final String NUMBER_RAISEPE_RCENT_TO = "refNumberScope#NUMBER_RAISE_PERCENT#=";
    private static final String NUMBER_FAIL_PERCENT = "refNumberScope#NUMBER_FAIL_PERCENT";
    private static final String NUMBER_FAIL_PERCENT_TO = "refNumberScope#NUMBER_FAIL_PERCENT#=";

    private static final String TIME_SUB_DAY = "refDateScope#TIME_SUB_DAY";
    private static final String TIME_ADD_DAY = "refDateScope#TIME_ADD_DAY";

    private static final String ADD = "+";
    private static final String SUB = "-";
    private static final String PRODUCT = "*";
    private static final String DIVISOR = "/";

    static {
        conditionUtilMap = new HashMap<>();
        conditionUtilMap.put("!contain", "NOT CONTAIN");
        conditionUtilMap.put("contain", "CONTAIN");
        conditionUtilMap.put("equal", "=");
        conditionUtilMap.put("!equal", "!=");
        conditionUtilMap.put("simpleDate#<", "<");
        conditionUtilMap.put("simpleDate#>", ">");//第几次
        conditionUtilMap.put("simpleDate#<;=", "<=");//倒数第几次
        conditionUtilMap.put("simpleDate#>;=", ">=");//前n次
        conditionUtilMap.put("simpleDate#!=", "!=");//后n次
        conditionUtilMap.put("simpleNumber#>", ">");
        conditionUtilMap.put("simpleNumber#<", "<");
        conditionUtilMap.put("simpleNumber#=", "=");
        conditionUtilMap.put("simpleNumber#>;=", ">=");
        conditionUtilMap.put("simpleNumber#<;=", "<=");
        conditionUtilMap.put("simpleDate#scope", "between");
    }

    public static String getCondition(String condition) {
        return conditionUtilMap.get(condition);
    }

    public static String getIndexSourceValue(String stitching, String value) {
        switch (stitching) {
            case TIME_SUB_DAY: {
                List<Integer> valueList = JSONArray.parseArray(value).toJavaList(Integer.class);
                Integer value1 = valueList.get(0) == null ? Integer.MAX_VALUE : valueList.get(0);
                Integer value2 = valueList.get(1) == null ? Integer.MIN_VALUE : valueList.get(1);
                BigInteger bigInteger = new BigInteger(String.valueOf(1000 * 3600 * 24));
                BigInteger bigDate1 = new BigInteger(value1.toString());
                BigInteger bigDate2 = new BigInteger(value2.toString());
                BigInteger date1 = bigDate1.multiply(bigInteger);
                BigInteger date2 = bigDate2.multiply(bigInteger);
                return "near_all(t1.values,t2.condition," + date2 + ",true," + date1 + ",true," + 1 + ")";
            }
            case TIME_ADD_DAY: {
                List<Integer> valueList = JSONArray.parseArray(value).toJavaList(Integer.class);
                Integer value1 = valueList.get(0) == null ? Integer.MIN_VALUE : valueList.get(0);
                Integer value2 = valueList.get(1) == null ? Integer.MAX_VALUE : valueList.get(1);
                BigInteger bigInteger = new BigInteger(String.valueOf(1000 * 3600 * 24));
                BigInteger bigDate1 = new BigInteger(value1.toString());
                BigInteger bigDate2 = new BigInteger(value2.toString());
                BigInteger date1 = bigDate1.multiply(bigInteger);
                BigInteger date2 = bigDate2.multiply(bigInteger);
                return "near_all(t1.values,t2.condition," + date1 + ",true," + date2 + ",true," + 0 + ")";
            }
            default: {
                return null;
            }
        }
    }

    public static String getIndexSourceValueForNum(String stitching, String value) {
        switch (stitching) {
            case NUMBER_SUB: {
                List<Long> valueList = JSONArray.parseArray(value).toJavaList(Long.class);
                Long date1 = Long.valueOf(valueList.get(0) == null ? Integer.MAX_VALUE : valueList.get(0));
                Long date2 = Long.valueOf(valueList.get(1) == null ? Integer.MIN_VALUE : valueList.get(1));
                return "near_all(t1.values,t2.condition," + date2 + ",false," + date1 + ",false," + 1 + ")";
            }
            case NUMBER_ADD: {
                List<Long> valueList = JSONArray.parseArray(value).toJavaList(Long.class);
                Long date1 = Long.valueOf(valueList.get(0) == null ? Integer.MIN_VALUE : valueList.get(0));
                Long date2 = Long.valueOf(valueList.get(1) == null ? Integer.MAX_VALUE : valueList.get(1));
                return "near_all(t1.values,t2.condition," + date1 + ",false," + date2 + ",false," + 0 + ")";
            }
            case NUMBER_SUB_TO: {
                List<Long> valueList = JSONArray.parseArray(value).toJavaList(Long.class);
                Long date1 = Long.valueOf(valueList.get(0) == null ? Integer.MAX_VALUE : valueList.get(0));
                Long date2 = Long.valueOf(valueList.get(1) == null ? Integer.MIN_VALUE : valueList.get(1));
                return "near_all(t1.values,t2.condition," + date2 + ",true," + date1 + ",true," + 1 + ")";
            }
            case NUMBER_ADD_TO: {
                List<Long> valueList = JSONArray.parseArray(value).toJavaList(Long.class);
                Long date1 = Long.valueOf(valueList.get(0) == null ? Integer.MIN_VALUE : valueList.get(0));
                Long date2 = Long.valueOf(valueList.get(1) == null ? Integer.MAX_VALUE : valueList.get(1));
                return "near_all(t1.values,t2.condition," + date1 + ",true," + date2 + ",true," + 0 + ")";
            }
            default: {
                return null;
            }
        }
    }

    public static String getIndexSourceValueForDou(String stitching, String value) {
        switch (stitching) {
            case NUMBER_RAISEPE_RCENT: {
                List<Double> valueList = JSONArray.parseArray(value).toJavaList(Double.class);
                Double date1 = Double.valueOf(valueList.get(0) == null ? Integer.MIN_VALUE : valueList.get(0));
                Double date2 = Double.valueOf(valueList.get(1) == null ? Integer.MAX_VALUE : valueList.get(1));
                date1 = 1 + date1 / 100;
                date2 = 1 + date2 / 100;
                return "near_all(t1.values,t2.condition," + date2 + ",false," + date1 + ",false," + 2 + ")";
            }
            case NUMBER_FAIL_PERCENT: {
                List<Double> valueList = JSONArray.parseArray(value).toJavaList(Double.class);
                Double date1 = Double.valueOf(valueList.get(0) == null ? Integer.MAX_VALUE : valueList.get(0));
                Double date2 = Double.valueOf(valueList.get(1) == null ? Integer.MIN_VALUE : valueList.get(1));
                date1 = 1 - date1 / 100;
                date2 = 1 - date2 / 100;
                return "near_all(t1.values,t2.condition," + date1 + ",false," + date2 + ",false," + 2 + ")";
            }
            case NUMBER_RAISEPE_RCENT_TO: {
                List<Double> valueList = JSONArray.parseArray(value).toJavaList(Double.class);
                Double date1 = Double.valueOf(valueList.get(0) == null ? Integer.MIN_VALUE : valueList.get(0));
                Double date2 = Double.valueOf(valueList.get(1) == null ? Integer.MAX_VALUE : valueList.get(1));
                date1 = 1 + date1 / 100;
                date2 = 1 + date2 / 100;
                return "near_all(t1.values,t2.condition," + date2 + ",true," + date1 + ",true," + 2 + ")";
            }
            case NUMBER_FAIL_PERCENT_TO: {
                List<Double> valueList = JSONArray.parseArray(value).toJavaList(Double.class);
                Double date1 = Double.valueOf(valueList.get(0) == null ? Integer.MAX_VALUE : valueList.get(0));
                Double date2 = Double.valueOf(valueList.get(1) == null ? Integer.MIN_VALUE : valueList.get(1));
                date1 = 1 - date1 / 100;
                date2 = 1 - date2 / 100;
                return "near_all(t1.values,t2.condition," + date1 + ",true," + date2 + ",true," + 2 + ")";
            }
            default: {
                return null;
            }
        }
    }

}
