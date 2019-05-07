package com.gennlife.rws.util;

import com.alibaba.fastjson.JSONArray;
import com.gennlife.rws.uql.UqlClass;
import net.lecousin.framework.math.IntegerUnit;
import scala.Int;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    /**
     * @param condition         条件
     * @param value             value数组值
     * @param source            需要处理的数据
     * @param indexActive       查出的值
     * @param detemineCondition jsontyp
     * @return
     */
    public static String detemineDateOrNumber(String condition, String value, String source, String indexActive, String detemineCondition) {
        String result = null;
        if ("date".equals(detemineCondition)) {
            result = getIndexDateCondition(condition, value, source, indexActive);
        } else if ("long".equals(detemineCondition)) {
            result = getIndexNumberCondition(condition, value, source, indexActive);
        }
        return result;
    }


    public static String getIndexDateCondition(String condition, String value, String source, String indexActive, String detemineCondition) {
        String result = null;
        if ("date".equals(detemineCondition)) {
            result = getIndexSourceCondition(condition, value, source, indexActive);
        }
        return result;

    }

    private static String getIndexSourceCondition(String condition, String value, String source, String indexActive) {
        List<Integer> valueList = JSONArray.parseArray(value).toJavaList(Integer.class);
        String result = null;
        Integer value1 = valueList.get(0);
        Integer value2 = valueList.get(1);
        long date1 = value1 * 1000 * 3600 * 24;
        long date2 = value2 * 1000 * 3600 * 24;
        if (TIME_ADD_DAY.equals(condition)) {
            result = "near_first(t1.visitSnDiagnose,t2.firstdatE,1,1000000,1)";
        } else if (TIME_SUB_DAY.equals(condition)) {
            result = DateCondition(value1, value2, SUB, source, indexActive);
        }
        return result;
    }

    public static String getIndexDateCondition(String condition, String value, String source, String indexActive) {
        List<Integer> valueList = JSONArray.parseArray(value).toJavaList(Integer.class);
        String result = null;
        Integer value1 = valueList.get(0);
        Integer value2 = valueList.get(1);
        long date1 = value1 * 1000 * 3600 * 24;
        long date2 = value2 * 1000 * 3600 * 24;
        if (TIME_ADD_DAY.equals(condition)) {
            result = DateCondition(value1, value2, ADD, source, indexActive);
        } else if (TIME_SUB_DAY.equals(condition)) {
            result = DateCondition(value1, value2, SUB, source, indexActive);
        }
        return result;
    }

    private static String DateCondition(Integer value1, Integer value2, String symbol, String source, String indexActive) {
        if (value1 == null) {
            return source + "<=" + indexActive + symbol + "days(" + value2 + ")";
        }
        if (value2 == null) {
            return source + ">=" + indexActive + symbol + "days(" + value1 + ")";
        }
        return " between " + indexActive + symbol + " days(" + value1 + ")" + " and " + indexActive + symbol + " days(" + value2 + ")";
    }

    public static String getIndexNumberCondition(String condition, String value, String source, String indexActive) {
        List<Integer> valueList = JSONArray.parseArray(value).toJavaList(Integer.class);
        String result = null;
        Integer value1 = valueList.get(0);
        Integer value2 = valueList.get(1);
        if (NUMBER_ADD.equals(condition)) {
            result = NumberConditon(value1, value2, false, ADD, source, indexActive);
        } else if (NUMBER_ADD_TO.equals(condition)) {
            result = NumberConditon(value1, value2, true, ADD, source, indexActive);
        } else if (NUMBER_SUB.equals(condition)) {
            result = NumberConditon(value1, value2, false, SUB, source, indexActive);
        } else if (NUMBER_SUB_TO.equals(condition)) {
            result = NumberConditon(value1, value2, true, SUB, source, indexActive);
        } else if (NUMBER_RAISEPE_RCENT.equals(condition)) {
            result = NumberConditon(value1 == null ? null : 1 + value1 / 100, value2 == null ? null : 1 + value2 / 100, false, PRODUCT, source, indexActive);
        } else if (NUMBER_RAISEPE_RCENT_TO.equals(condition)) {
            result = NumberConditon(value1 == null ? null : 1 + value1 / 100, value2 == null ? null : 1 + value2 / 100, true, PRODUCT, source, indexActive);
        } else if (NUMBER_FAIL_PERCENT.equals(condition)) {
            result = NumberConditon(value1 == null ? null : 1 - value1 / 100, value2 == null ? null : 1 - value2 / 100, false, PRODUCT, source, indexActive);
        } else if (NUMBER_FAIL_PERCENT_TO.equals(condition)) {
            result = NumberConditon(value1 == null ? null : 1 - value1 / 100, value2 == null ? null : 1 - value2 / 100, true, PRODUCT, source, indexActive);
        }
        return result;
    }

    private static String NumberConditon(Integer value1, Integer value2, boolean isSign, String symbol, String source, String indexActive) {
        if (value1 == null && !isSign) {
            return source + "<" + indexActive + symbol + value2;
        }
        if (value2 == null && !isSign) {
            return source + ">" + indexActive + symbol + value1;
        }
        if (value1 == null && isSign) {
            return source + "<=" + indexActive + symbol + value2;
        }
        if (value2 == null && isSign) {
            return source + ">=" + indexActive + symbol + value1;
        }
        if (isSign) {
            return " between " + indexActive + symbol + value1 + " and " + indexActive + symbol + value2;

        } else {
            return source + ">" + indexActive + symbol + value1 + " and " + source + "<" + indexActive + symbol + value2;

        }
    }

    public static String getIndexSourceFilter(String stitching, String sourceTagName, String value, String refActiveId, String jsonType) {
        List<Integer> valueList = JSONArray.parseArray(value).toJavaList(Integer.class);
        String result = null;
        Integer value1 = valueList.get(0);
        Integer value2 = valueList.get(1);
        BigInteger bigInteger = new BigInteger(String.valueOf(1000 * 3600 * 24));
        BigInteger bigDate1 = new BigInteger(value1.toString());
        BigInteger bigDate2 = new BigInteger(value2.toString());
        BigInteger date1 = bigDate1.multiply(bigInteger);
        BigInteger date2 = bigDate2.multiply(bigInteger);
        if (TIME_SUB_DAY.equals(stitching)) {
            result = refActiveId + ".condition - " + "t1." + refActiveId + " < " + date1 + " and " + refActiveId + ".condition - " + "t1." + refActiveId + " > " + date2;
        } else if (TIME_ADD_DAY.equals(stitching)) {
            result = "t1." + refActiveId + " - " + refActiveId + ".condition > " + date1 + " and t1." + refActiveId + " - " + refActiveId + ".condition < " + date2;
        }
        return result;
    }

    public static String getExcludeDateCondition(String jsonType, String value, String refActiveId, String stitching) {
        try {
            String condition = getCondition(stitching);
            StringBuffer stringBuffer = new StringBuffer();
            JSONArray val1 = JSONArray.parseArray(value);
            int size = val1 == null ? 0 : val1.size();
            if ("between".equals(condition)) {
                for (int i = 0; i < size; i++) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(val1.getString(i)));
                    stringBuffer.append(refActiveId);
                    stringBuffer.append(".condition - ");
                    stringBuffer.append(calendar.getTimeInMillis());
                    if (i == 0) {
                        stringBuffer.append(" > ");
                    } else {
                        stringBuffer.append(" < ");
                    }
                    stringBuffer.append("0");
                    if (i < 1) {
                        stringBuffer.append(" and ");
                    }
                }
            } else {
                for (int i = 0; i < size; i++) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(val1.getString(i)));
                    stringBuffer.append(refActiveId);
                    stringBuffer.append(".condition - ");
                    stringBuffer.append(calendar.getTimeInMillis());
                    stringBuffer.append(condition);
                    stringBuffer.append("0");
                }
            }

            return stringBuffer.toString();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String getIndexSourceValue(String stitching, String sourceTagName, String value, String beforId, String afterId, String jsonType, String resultValue, String resultFunction, String resultFunctionNum, String activeIndexId) {
        if (TIME_SUB_DAY.equals(stitching)) {
            List<Integer> valueList = JSONArray.parseArray(value).toJavaList(Integer.class);
            Integer value1 = valueList.get(0) == null ? Integer.MAX_VALUE : valueList.get(0);
            Integer value2 = valueList.get(1) == null ? Integer.MIN_VALUE : valueList.get(1);
            BigInteger bigInteger = new BigInteger(String.valueOf(1000 * 3600 * 24));
            BigInteger bigDate1 = new BigInteger(value1.toString());
            BigInteger bigDate2 = new BigInteger(value2.toString());
            BigInteger date1 = bigDate1.multiply(bigInteger);
            BigInteger date2 = bigDate2.multiply(bigInteger);
            switch (resultFunction) {
                case "first": {
                    return "near_first(" + activeIndexId + "." + beforId + "," + afterId + ".condition," + date2 + ",true," + date1 + ",true," + 1 + ")";
                }
                case "last": {
                    return "near_last(" + activeIndexId + "." + beforId + "," + afterId + ".condition," + date2 + ",true," + date1 + ",true," + 1 + ")";
                }
                case "index": {
                    return "near_any(" + activeIndexId + "." + beforId + "," + afterId + ".condition," + date2 + ",true," + date1 + ",true," + resultFunctionNum + "," + 1 + ")";
                }
                case "reverseindex": {
                    return "near_any(" + activeIndexId + "." + beforId + "," + afterId + ".condition," + date2 + ",true," + date1 + ",true,-" + resultFunctionNum + "," + 1 + ")";
                }
                case "all": {
                    return "near_all(t1.values,t2.condition," + date2 + ",true," + date1 + ",true," + 1 + ")";
                }
            }
        } else if (TIME_ADD_DAY.equals(stitching)) {
            List<Integer> valueList = JSONArray.parseArray(value).toJavaList(Integer.class);
            Integer value1 = valueList.get(0) == null ? Integer.MIN_VALUE : valueList.get(0);
            Integer value2 = valueList.get(1) == null ? Integer.MAX_VALUE : valueList.get(1);
            BigInteger bigInteger = new BigInteger(String.valueOf(1000 * 3600 * 24));
            BigInteger bigDate1 = new BigInteger(value1.toString());
            BigInteger bigDate2 = new BigInteger(value2.toString());
            BigInteger date1 = bigDate1.multiply(bigInteger);
            BigInteger date2 = bigDate2.multiply(bigInteger);
            switch (resultFunction) {
                case "first": {
                    return "near_first(" + activeIndexId + "." + beforId + "," + afterId + ".condition," + date1 + ",true," + date2 + ",true," + 0 + ")";
                }
                case "last": {
                    return "near_last(" + activeIndexId + "." + beforId + "," + afterId + ".condition," + date1 + ",true," + date2 + ",true," + 0 + ")";
                }
                case "index": {
                    return "near_any(" + activeIndexId + "." + beforId + "," + afterId + ".condition," + date1 + ",true," + date2 + ",true," + resultFunctionNum + "," + 0 + ")";
                }
                case "reverseindex": {
                    return "near_any(" + activeIndexId + "." + beforId + "," + afterId + ".condition," + date1 + ",true," + date2 + ",true,-" + resultFunctionNum + "," + 0 + ")";
                }
                case "all": {
                    return "near_all(t1.values,t2.condition," + date1 + ",true," + date2 + ",true," + 0 + ")";
                }
            }
        }
        return null;
    }

    public static String getIndexSourceValueForNum(String stitching, String sourceTagName, String value, String refActiveId, String jsonType, String resultValue, String resultFunction, String resultFunctionNum, String activeIndexId) {
        if (NUMBER_SUB.equals(stitching)) {
            List<Long> valueList = JSONArray.parseArray(value).toJavaList(Long.class);
            Long date1 = Long.valueOf(valueList.get(0) == null ? Integer.MAX_VALUE : valueList.get(0));
            Long date2 = Long.valueOf(valueList.get(1) == null ? Integer.MIN_VALUE : valueList.get(1));
            switch (resultFunction) {
                case "first": {
                    return "near_first(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date2 + ",false," + date1 + ",false," + 1 + ")";
                }
                case "last": {
                    return "near_last(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date2 + ",false," + date1 + ",false," + 1 + ")";
                }
                case "index": {
                    return "near_any(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date2 + ",false," + date1 + ",false," + resultFunctionNum + "," + 1 + ")";
                }
                case "reverseindex": {
                    return "near_any(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date2 + ",false," + date1 + ",false,-" + resultFunctionNum + "," + 1 + ")";
                }
                case "all": {
                    return "near_all(t1.values,t2.condition," + date2 + ",false," + date1 + ",false," + 1 + ")";
                }
            }
        } else if (NUMBER_ADD.equals(stitching)) {
            List<Long> valueList = JSONArray.parseArray(value).toJavaList(Long.class);
            Long date1 = Long.valueOf(valueList.get(0) == null ? Integer.MIN_VALUE : valueList.get(0));
            Long date2 = Long.valueOf(valueList.get(1) == null ? Integer.MAX_VALUE : valueList.get(1));
            switch (resultFunction) {
                case "first": {
                    return "near_first(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date1 + ",false," + date2 + ",false," + 0 + ")";
                }
                case "last": {
                    return "near_last(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date1 + ",false," + date2 + ",false," + 0 + ")";
                }
                case "index": {
                    return "near_any(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date1 + ",false," + date2 + ",false," + resultFunctionNum + "," + 0 + ")";
                }
                case "reverseindex": {
                    return "near_any(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date1 + ",false," + date2 + ",false,-" + resultFunctionNum + "," + 0 + ")";
                }
                case "all": {
                    return "near_all(t1.values,t2.condition," + date1 + ",false," + date2 + ",false," + 0 + ")";
                }
            }
        } else if (NUMBER_SUB_TO.equals(stitching)) {
            List<Long> valueList = JSONArray.parseArray(value).toJavaList(Long.class);
            Long date1 = Long.valueOf(valueList.get(0) == null ? Integer.MAX_VALUE : valueList.get(0));
            Long date2 = Long.valueOf(valueList.get(1) == null ? Integer.MIN_VALUE : valueList.get(1));
            switch (resultFunction) {
                case "first": {
                    return "near_first(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date2 + ",true," + date1 + ",true," + 1 + ")";
                }
                case "last": {
                    return "near_last(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date2 + ",true," + date1 + ",true," + 1 + ")";
                }
                case "index": {
                    return "near_any(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date2 + ",true," + date1 + ",true," + resultFunctionNum + "," + 1 + ")";
                }
                case "reverseindex": {
                    return "near_any(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date2 + ",true," + date1 + ",true,-" + resultFunctionNum + "," + 1 + ")";
                }
                case "all": {
                    return "near_all(t1.values,t2.condition," + date2 + ",true," + date1 + ",true," + 1 + ")";
                }
            }
        } else if (NUMBER_ADD_TO.equals(stitching)) {
            List<Long> valueList = JSONArray.parseArray(value).toJavaList(Long.class);
            Long date1 = Long.valueOf(valueList.get(0) == null ? Integer.MIN_VALUE : valueList.get(0));
            Long date2 = Long.valueOf(valueList.get(1) == null ? Integer.MAX_VALUE : valueList.get(1));
            switch (resultFunction) {
                case "first": {
                    return "near_first(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date1 + ",true," + date2 + ",true," + 0 + ")";
                }
                case "last": {
                    return "near_last(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date1 + ",true," + date2 + ",true," + 0 + ")";
                }
                case "index": {
                    return "near_any(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date1 + ",true," + date2 + ",true," + resultFunctionNum + "," + 0 + ")";
                }
                case "reverseindex": {
                    return "near_any(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date1 + ",true," + date2 + ",true,-" + resultFunctionNum + "," + 0 + ")";
                }
                case "all": {
                    return "near_all(t1.values,t2.condition," + date1 + ",true," + date2 + ",true," + 0 + ")";
                }
            }
        }
        return null;
    }

    public static String getIndexSourceValueForDou(String stitching, String sourceTagName, String value, String refActiveId, String jsonType, String resultValue, String resultFunction, String resultFunctionNum, String activeIndexId) {
        if (NUMBER_RAISEPE_RCENT.equals(stitching)) {
            List<Double> valueList = JSONArray.parseArray(value).toJavaList(Double.class);
            Double date1 = Double.valueOf(valueList.get(0) == null ? Integer.MIN_VALUE : valueList.get(0));
            Double date2 = Double.valueOf(valueList.get(1) == null ? Integer.MAX_VALUE : valueList.get(1));
            switch (resultFunction) {
                case "first": {
                    date1 = 1 + date1 / 100;
                    date2 = 1 + date2 / 100;
                    return "near_first(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date2 + ",false," + date1 + ",false," + 2 + ")";
                }
                case "last": {
                    date1 = 1 + date1 / 100;
                    date2 = 1 + date2 / 100;
                    return "near_last(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date2 + ",false," + date1 + ",false," + 2 + ")";
                }
                case "index": {
                    date1 = 1 + date1 / 100;
                    date2 = 1 + date2 / 100;
                    return "near_any(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date2 + ",false," + date1 + ",false," + resultFunctionNum + "," + 2 + ")";
                }
                case "reverseindex": {
                    date1 = 1 + date1 / 100;
                    date2 = 1 + date2 / 100;
                    return "near_any(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date2 + ",false," + date1 + ",false,-" + resultFunctionNum + "," + 2 + ")";
                }
                case "all": {
                    date1 = 1 + date1 / 100;
                    date2 = 1 + date2 / 100;
                    return "near_all(t1.values,t2.condition," + date2 + ",false," + date1 + ",false," + 2 + ")";
                }
            }
        } else if (NUMBER_FAIL_PERCENT.equals(stitching)) {
            List<Double> valueList = JSONArray.parseArray(value).toJavaList(Double.class);
            Double date1 = Double.valueOf(valueList.get(0) == null ? Integer.MAX_VALUE : valueList.get(0));
            Double date2 = Double.valueOf(valueList.get(1) == null ? Integer.MIN_VALUE : valueList.get(1));
            switch (resultFunction) {
                case "first": {
                    date1 = 1 - date1 / 100;
                    date2 = 1 - date2 / 100;
                    return "near_first(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date1 + ",false," + date2 + ",false," + 2 + ")";
                }
                case "last": {
                    date1 = 1 - date1 / 100;
                    date2 = 1 - date2 / 100;
                    return "near_last(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date1 + ",false," + date2 + ",false," + 2 + ")";
                }
                case "index": {
                    date1 = 1 - date1 / 100;
                    date2 = 1 - date2 / 100;
                    return "near_any(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date1 + ",false," + date2 + ",false," + resultFunctionNum + "," + 2 + ")";
                }
                case "reverseindex": {
                    date1 = 1 - date1 / 100;
                    date2 = 1 - date2 / 100;
                    return "near_any(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date1 + ",false," + date2 + ",false,-" + resultFunctionNum + "," + 2 + ")";
                }
                case "all": {
                    date1 = 1 - date1 / 100;
                    date2 = 1 - date2 / 100;
                    return "near_all(t1.values,t2.condition," + date1 + ",false," + date2 + ",false," + 2 + ")";
                }
            }
        } else if (NUMBER_RAISEPE_RCENT_TO.equals(stitching)) {
            List<Double> valueList = JSONArray.parseArray(value).toJavaList(Double.class);
            Double date1 = Double.valueOf(valueList.get(0) == null ? Integer.MIN_VALUE : valueList.get(0));
            Double date2 = Double.valueOf(valueList.get(1) == null ? Integer.MAX_VALUE : valueList.get(1));
            switch (resultFunction) {
                case "first": {
                    date1 = 1 + date1 / 100;
                    date2 = 1 + date2 / 100;
                    return "near_first(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date2 + ",true," + date1 + ",true," + 2 + ")";
                }
                case "last": {
                    date1 = 1 + date1 / 100;
                    date2 = 1 + date2 / 100;
                    return "near_last(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date2 + ",true," + date1 + ",true," + 2 + ")";
                }
                case "index": {
                    date1 = 1 + date1 / 100;
                    date2 = 1 + date2 / 100;
                    return "near_any(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date2 + ",true," + date1 + ",true," + resultFunctionNum + "," + 2 + ")";
                }
                case "reverseindex": {
                    date1 = 1 + date1 / 100;
                    date2 = 1 + date2 / 100;
                    return "near_any(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date2 + ",true," + date1 + ",true,-" + resultFunctionNum + "," + 2 + ")";

                }
                case "all": {
                    date1 = 1 + date1 / 100;
                    date2 = 1 + date2 / 100;
                    return "near_all(t1.values,t2.condition," + date2 + ",true," + date1 + ",true," + 2 + ")";

                }
            }
        } else if (NUMBER_FAIL_PERCENT_TO.equals(stitching)) {
            List<Double> valueList = JSONArray.parseArray(value).toJavaList(Double.class);
            Double date1 = Double.valueOf(valueList.get(0) == null ? Integer.MAX_VALUE : valueList.get(0));
            Double date2 = Double.valueOf(valueList.get(1) == null ? Integer.MIN_VALUE : valueList.get(1));
            switch (resultFunction) {
                case "first": {
                    date1 = 1 - date1 / 100;
                    date2 = 1 - date2 / 100;
                    return "near_first(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date1 + ",true," + date2 + ",true," + 2 + ")";
                }
                case "last": {
                    date1 = 1 - date1 / 100;
                    date2 = 1 - date2 / 100;
                    return "near_last(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date1 + ",true," + date2 + ",true," + 2 + ")";
                }
                case "index": {
                    date1 = 1 - date1 / 100;
                    date2 = 1 - date2 / 100;
                    return "near_any(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date1 + ",true," + date2 + ",true," + resultFunctionNum + "," + 2 + ")";
                }
                case "reverseindex": {
                    date1 = 1 - date1 / 100;
                    date2 = 1 - date2 / 100;
                    return "near_any(" + activeIndexId + "." + refActiveId + "," + refActiveId + ".condition," + date1 + ",true," + date2 + ",true,-" + resultFunctionNum + "," + 2 + ")";
                }
                case "all": {
                    date1 = 1 - date1 / 100;
                    date2 = 1 - date2 / 100;
                    return "near_all(t1.values,t2.condition," + date1 + ",true," + date2 + ",true," + 2 + ")";
                }
            }
        }

        return null;

    }

}
