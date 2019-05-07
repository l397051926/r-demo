package com.gennlife.rws.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liumingxin
 * @create 2018 10 10:52
 * @desc
 **/
public class FunctionUtilMap {

    private static final Map<String,String> uqlFunctionMap ;
    static {
        uqlFunctionMap = new HashMap<>();
        uqlFunctionMap.put("all","");
        uqlFunctionMap.put("first","first_date");
        uqlFunctionMap.put("last","last_date");
        uqlFunctionMap.put("min","min");
        uqlFunctionMap.put("max","max");
        uqlFunctionMap.put("index","any_date");//第几次
        uqlFunctionMap.put("reverseindex","any_date");//倒数第几次
        uqlFunctionMap.put("previousList","");//前n次
        uqlFunctionMap.put("afterList","");//后n次
        uqlFunctionMap.put("avg","avg");
        uqlFunctionMap.put("sum","sum");
    }
    /*
    private static final String ALL ="all";
    private static final String FIRST ="first";
    private static final String LAST ="last";
    private static final String MIN ="min";
    private static final String MAX ="max";
    private static final String INDEX ="index";
    private static final String REVERSE_INDEX ="reverseindex";
    private static final String PREVIOUS_LIST ="previousList";
    private static final String AFFTER_LIST ="afterList";
    private static final String AVG ="avg";
    private static final String SUM ="sum";
    private static final String all ="all";
    */

    public static String getUqlFunction(String function,String num,String keyWords){
         String uqlFunction = uqlFunctionMap.get(function);
         if("first".equals(function)){

         }
         if(StringUtils.isEmpty(num)){
             return uqlFunction+"("+keyWords+")";
         }else {
             if("index".equals(function)){
                 return uqlFunction+"("+keyWords+","+num+")";
             }else {
                 return uqlFunction+"("+keyWords+",-"+num+")";
             }
         }
    }

    public static String getUqlFunction(String function,String num,String keyWords,String indexType,String indexDate){
        if("first".equals(function)){
            return getFirstFunction(function,keyWords,indexDate,indexType);
        }
        if("last".equals(function)){
            return getLastFunction(function,keyWords,indexDate,indexType);
        }
        if("index".equals(function)){
            return getIndexFunction(function,keyWords,indexDate,indexType,num);
        }
        if("reverseIndex".equals(function)){
            return getReverseIndex(function,keyWords,indexDate,indexType,num);
        }
        if("all".equals(function)){
            return getAllActive(function,keyWords,indexDate,indexType,num);
        }
        return getUqlFunction(function, num, keyWords);
    }

    private static String getAllActive(String function, String keyWords, String indexDate, String indexType, String num) {
        return "group_concat("+keyWords+",',') ";
    }

    private static String getReverseIndex(String function, String keyWords, String indexDate, String indexType, String num) {
        if(indexType.contains("date")  && !"visit_info.VISIT_SN".equals(keyWords) && !"visitinfo.DOC_ID".equals(keyWords)){
            return "any_date("+keyWords+",-"+num+")";
        }else {
            return "any_value("+keyWords+","+indexDate+",-"+num+")";
        }
    }

    private static String getIndexFunction(String function, String keyWords, String indexDate, String indexType, String num) {
        if(indexType.contains("date")  && !"visit_info.VISIT_SN".equals(keyWords) && !"visitinfo.DOC_ID".equals(keyWords)){
            return "any_date("+keyWords+","+num+")";
        }else {
            return "any_value("+keyWords+","+indexDate+","+num+")";
        }
    }

    private static String getLastFunction(String function, String keyWords, String indexDate, String indexType) {
        if(indexType.contains("date")  && !"visit_info.VISIT_SN".equals(keyWords) && !"visitinfo.DOC_ID".equals(keyWords)){
            return "last_date("+keyWords+")";
        }else {
            return "last_value("+keyWords+","+indexDate+")";
        }
    }

    private static String getFirstFunction(String function, String keyWords, String indexDate, String indexType) {
        if(indexType.contains("date") && !"visit_info.VISIT_SN".equals(keyWords) && !"visitinfo.DOC_ID".equals(keyWords)){
            return "first_date("+keyWords+")";
        }else {
            return "first_value("+keyWords+","+indexDate+")";
        }
    }

    public static String editHasParent(String condition){
        return "hasparent("+condition+")";
    }

    public static String editHasChild(String condition){
        return "haschild("+condition+")";
    }


}
