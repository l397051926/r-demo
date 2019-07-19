package com.gennlife.rws.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.gson.JsonArray;
import scala.Int;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import static java.util.stream.Collectors.joining;


public enum DataType {

    BOOLEAN,
    LONG,
    DOUBLE,
    DATE,
    STRING;

    public static DataType fromString(String typeStr) {
        if ("事件".equals(typeStr)) {
            return LONG;
        }
        String strs[] = typeStr.split(":");
        return DataType.valueOf((strs.length == 1 ? strs[0] : strs[1]).toUpperCase());
    }

    public boolean isNumeric() {
        return this == LONG || this == DOUBLE;
    }

    public String serialize(String field, String sign, Object value, boolean linkFlag) {
        String op = ConditionUtilMap.getCondition(sign);
        if (op == null) {
            op = sign;
        }
        switch (this) {
            case BOOLEAN: {
                throw new RuntimeException("不应运行到这里");
            }
            case LONG:
            case DOUBLE: {
                if (value instanceof String) {
                    try {
                        value = JSON.parseArray((String) value);
                    } catch (Exception ignored) {
                    }
                }
                if (value instanceof JSONArray) {
                    value = ((JSONArray) value).getDoubleValue(0);
                }
                double v = (value instanceof Number ? ((Number) value) : new BigDecimal(value.toString())).doubleValue();
                return field + " " + op + " " + v;
            }
            case DATE: {
                String arr[] = (value instanceof JSONArray ? ((JSONArray) value) : JSON.parseArray(value.toString()))
                    .stream()
                    .map(String.class::cast)
                    .toArray(String[]::new);
                if(linkFlag){
                    String t1 = "'"+arr[0]+"'";
                    String t2 = "";
                    if(arr.length>1){
                        t2 = "'"+arr[1]+"'";
                    }

                    return "between".equals(op)
                        ? field + " between " + t1 + " and " + t2
                        : field + " " + op + " " + t1;

                }else {
                    long t1 = 0L;
                    long t2 = 0L;
                    try {
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(arr[0]));
                        t1 = calendar1.getTimeInMillis();
                        if(arr.length>1){
                            Calendar calendar2 = Calendar.getInstance();
                            calendar2.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(arr[1]));
                            t2 = calendar2.getTimeInMillis();
                        }
                    } catch (Exception e) {
                        try {
                            Calendar calendar1 = Calendar.getInstance();
                            calendar1.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(arr[0]));
                            t1 = calendar1.getTimeInMillis();
                            if(arr.length>1){
                                Calendar calendar2 = Calendar.getInstance();
                                calendar2.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(arr[1]));
                                t2 = calendar2.getTimeInMillis();
                            }
                        } catch (Exception e1) {
                            t1 = Long.valueOf(arr[0]);
                            if(arr.length>1){
                                t2 = Long.valueOf(arr[1]);
                            }
                        }
                    }
                    return "between".equals(op)
                        ? field + " between " + t1 + " and " + t2
                        : field + " " + op + " " + t1;
                }

            }
            case STRING: {
                try {
                    if("NOT CONTAIN".equals(op)){
                        return field + " NOT IN ( " + JSON.parseArray(value.toString()).stream().map(String.class::cast).map(s -> "'" + s + "'").collect(joining(",")) + " )";
                    }else {
                        return field + " IN ( " + JSON.parseArray(value.toString()).stream().map(String.class::cast).map(s -> "'" + s + "'").collect(joining(",")) + " )";
                    }
                } catch (Exception e) {
                    return field + " " + op + " '" + value.toString() +"'";
                }
            }
            default: {
                throw new RuntimeException("未知的类型：" + this);
            }
        }
    }

}
