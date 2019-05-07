//package com.gennlife;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import java.util.*;
//import java.util.concurrent.*;
//
///**
// * @author liumingxin
// * @create 2018 19 18:08
// * @desc
// **/
//public class TestDemo {
//
//    private static ConcurrentLinkedQueue<String> task;
//
//    private static ConcurrentHashMap<String, Object> resultMap;
//
//    private static ConcurrentHashMap<String, Thread> thread;
//
//    private static String template = "{\n" +
//
//            "\t\"operatorSign\": \"and\",\n" +
//
//            "\t\"uuid\": \"aadsa\",\n" +
//
//            "\t\"id\": \"null\",\n" +
//
//            "\t\"parentId\": \"\",\n" +
//
//            "\t\"positioningBox\": [\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"占位符0\",\n" +
//
//            "\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\"after\": \"details-3-2\"\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\"before\": \"占位符0\",\n" +
//
//            "\t\t\t\"after\": \"占位符1\",\n" +
//
//            "\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t}]\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"占位符1\",\n" +
//
//            "\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\"before\": \"details-3-2\",\n" +
//
//            "\t\t\t\"after\": \"inner的括号2\"\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"1-1\",\n" +
//
//            "\t\t\t\"before\": \"占位符1\",\n" +
//
//            "\t\t\t\"after\": \"占位符2\",\n" +
//
//            "\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\"operatorSign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"positioningBox\": [\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"占位符2-0\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\"after\": \"details-13-2\"\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\"before\": \"占位符2-0\",\n" +
//
//            "\t\t\t\t\t\"after\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\"after\": \"inner\"\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"1-2\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\"before\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\"after\": \"占位符2-3\",\n" +
//
//            "\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"positioningBox\": [{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-2-1\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-2-2\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符3-5\",\n" +
//
//            "\t\t\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"positioningBox\": []\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-2-3\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t]\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"占位符2-3\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\"before\": \"inner\",\n" +
//
//            "\t\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t\t}\n" +
//
//            "\t\t\t]\n" +
//
//            "\t\t}, \n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"2-1\",\n" +
//
//            "\t\t\t\"before\": \"占位符1\",\n" +
//
//            "\t\t\t\"after\": \"占位符2\",\n" +
//
//            "\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\"operatorSign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"positioningBox\": [\n" +
//
//            "\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"占位符2-0\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\"after\": \"details-13-2\"\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\"before\": \"占位符2-0\",\n" +
//
//            "\t\t\t\t\t\"after\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\"after\": \"inner\"\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"2-2\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\"before\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\"after\": \"占位符2-3\",\n" +
//
//            "\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"positioningBox\": [\n" +
//
//            "\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-2-1\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-2-2\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符3-5\",\n" +
//
//            "\t\t\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"positioningBox\": []\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-2-3\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t]\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"占位符2-3\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\"before\": \"inner\",\n" +
//
//            "\t\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t\t}\n" +
//
//            "\t\t\t]\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"占位符2\",\n" +
//
//            "\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\"before\": \"inner的括号2\",\n" +
//
//            "\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t}\n" +
//
//            "\t]\n" +
//
//            "}";
//
//    private String template1 = "{\n" +
//
//            "\t\"operatorSign\": \"and\",\n" +
//
//            "\t\"uuid\": \"aadsa\",\n" +
//
//            "\t\"id\": \"null\",\n" +
//
//            "\t\"parentId\": \"\",\n" +
//
//            "\t\"positioningBox\": [{\n" +
//
//            "\t\t\t\"uuid\": \"占位符0\",\n" +
//
//            "\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\"after\": \"details-3-2\"\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\"before\": \"占位符0\",\n" +
//
//            "\t\t\t\"after\": \"占位符1\",\n" +
//
//            "\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t}]\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"占位符1\",\n" +
//
//            "\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\"before\": \"details-3-2\",\n" +
//
//            "\t\t\t\"after\": \"inner的括号2\"\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"inner的括号2\",\n" +
//
//            "\t\t\t\"before\": \"占位符1\",\n" +
//
//            "\t\t\t\"after\": \"占位符2\",\n" +
//
//            "\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\"operatorSign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"positioningBox\": [{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"占位符2-0\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\"after\": \"details-13-2\"\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\"before\": \"占位符2-0\",\n" +
//
//            "\t\t\t\t\t\"after\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\"after\": \"inner\"\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"inner\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\"before\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\"after\": \"占位符2-3\",\n" +
//
//            "\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"positioningBox\": [{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-2-1\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-2-2\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符3-5\",\n" +
//
//            "\t\t\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"positioningBox\": []\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-2-3\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t]\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"占位符2-3\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\"before\": \"inner\",\n" +
//
//            "\t\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t\t}\n" +
//
//            "\t\t\t]\n" +
//
//            "\t\t}, {\n" +
//
//            "\t\t\t\"uuid\": \"inner的括号2\",\n" +
//
//            "\t\t\t\"before\": \"占位符1\",\n" +
//
//            "\t\t\t\"after\": \"占位符2\",\n" +
//
//            "\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\"operatorSign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"positioningBox\": [{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"占位符2-0\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\"after\": \"details-13-2\"\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\"before\": \"占位符2-0\",\n" +
//
//            "\t\t\t\t\t\"after\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\"after\": \"inner\"\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"inner\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\"before\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\"after\": \"占位符2-3\",\n" +
//
//            "\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"positioningBox\": [{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-2-1\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-2-2\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符3-5\",\n" +
//
//            "\t\t\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"positioningBox\": []\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-2-3\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t]\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"占位符2-3\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\"before\": \"inner\",\n" +
//
//            "\t\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t\t}\n" +
//
//            "\t\t\t]\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"占位符2\",\n" +
//
//            "\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\"before\": \"inner的括号2\",\n" +
//
//            "\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t}\n" +
//
//            "\t]\n" +
//
//            "}";
//
//
//
//    private static String template2="\n" +
//
//            "{\n" +
//
//            "\t\"operatorSign\": \"and\",\n" +
//
//            "\t\"uuid\": \"aadsa\",\n" +
//
//            "\t\"id\": \"null\",\n" +
//
//            "\t\"parentId\": \"\",\n" +
//
//            "\t\"positioningBox\": [\n" +
//
//            "\t{\n" +
//
//            "\t\t\"uuid\": \"1-1\",\n" +
//
//            "\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\"after\": \"1-2\"\n" +
//
//            "\t},\n" +
//
//            "\t{\n" +
//
//            "\t\t\"uuid\": \"1-2\",\n" +
//
//            "\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\"before\": \"1-1\",\n" +
//
//            "\t\t\"after\": \"1-3\",\n" +
//
//            "\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\"strongRef\": []\n" +
//
//            "\t\t}]\n" +
//
//            "\t},\n" +
//
//            "\t{\n" +
//
//            "\t\t\"uuid\": \"1-3\",\n" +
//
//            "\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\"before\": \"1-2\",\n" +
//
//            "\t\t\"after\": \"1-4\"\n" +
//
//            "\t},\n" +
//
//            "\t{\n" +
//
//            "\t\t\"uuid\": \"1-4\",\n" +
//
//            "\t\t\"before\": \"1-3\",\n" +
//
//            "\t\t\"after\": \"1-5\",\n" +
//
//            "\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\"operatorSign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\"positioningBox\": [\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"2-1\",\n" +
//
//            "\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\"after\": \"2-2\"\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"2-2\",\n" +
//
//            "\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\"before\": \"2-1\",\n" +
//
//            "\t\t\t\"after\": \"2-3\",\n" +
//
//            "\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t}]\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"2-3\",\n" +
//
//            "\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\"before\": \"2-2\",\n" +
//
//            "\t\t\t\"after\": \"2-4\"\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"2-4\",\n" +
//
//            "\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\"before\": \"2-3\",\n" +
//
//            "\t\t\t\"after\": \"2-5\",\n" +
//
//            "\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"positioningBox\": [\n" +
//
//            "\t\t\t{\n" +
//
//            "\t\t\t\t\"uuid\": \"3-1\",\n" +
//
//            "\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\"after\": \"3-2\"\n" +
//
//            "\t\t\t},\n" +
//
//            "\t\t\t{\n" +
//
//            "\t\t\t\t\"uuid\": \"3-2\",\n" +
//
//            "\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\"before\": \"3-1\",\n" +
//
//            "\t\t\t\t\"after\": \"3-3\",\n" +
//
//            "\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t}]\n" +
//
//            "\t\t\t},\n" +
//
//            "\t\t\t{\n" +
//
//            "\t\t\t\t\"uuid\": \"3-2\",\n" +
//
//            "\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\"before\": \"3-1\",\n" +
//
//            "\t\t\t\t\"after\": \"3-3\"\n" +
//
//            "\t\t\t},\n" +
//
//            "\t\t\t{\n" +
//
//            "\t\t\t\t\"uuid\": \"3-3\",\n" +
//
//            "\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\"before\": \"3-2\",\n" +
//
//            "\t\t\t\t\"after\": \"3-4\",\n" +
//
//            "\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\"positioningBox\": []\n" +
//
//            "\t\t\t},\n" +
//
//            "\t\t\t{\n" +
//
//            "\t\t\t\t\"uuid\": \"3-4\",\n" +
//
//            "\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\"before\": \"3-3\",\n" +
//
//            "\t\t\t\t\"after\": \"3-5\"\n" +
//
//            "\t\t\t},\n" +
//
//            "\t\t\t{\n" +
//
//            "\t\t\t\t\"uuid\": \"3-5\",\n" +
//
//            "\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\"before\": \"3-4\",\n" +
//
//            "\t\t\t\t\"after\": \"3-6\",\n" +
//
//            "\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t}]\n" +
//
//            "\t\t\t},\n" +
//
//            "\t\t\t{\n" +
//
//            "\t\t\t\t\"uuid\": \"3-6\",\n" +
//
//            "\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\"before\": \"3-5\",\n" +
//
//            "\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t}]\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"2-5\",\n" +
//
//            "\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\"before\": \"2-4\",\n" +
//
//            "\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t}]\n" +
//
//            "\t},\n" +
//
//            "\t{\n" +
//
//            "\t\t\"uuid\": \"1-5\",\n" +
//
//            "\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\"before\": \"1-4\",\n" +
//
//            "\t\t\"after\": \"undefined\"\n" +
//
//            "\t}]\n" +
//
//            "}";
//
//
//
//    private static String templat3="{\n" +
//
//            "\t\"operatorSign\": \"and\",\n" +
//
//            "\t\"details\": [{\n" +
//
//            "\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\"after\": \"details-3-2\",\n" +
//
//            "\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\"uuid\": \"占位符0\"\n" +
//
//            "\t},\n" +
//
//            "\t{\n" +
//
//            "\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\"before\": \"占位符0\",\n" +
//
//            "\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"value\": \"强关联2\"\n" +
//
//            "\t\t}],\n" +
//
//            "\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\"after\": \"占位符1\",\n" +
//
//            "\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\"\n" +
//
//            "\t},\n" +
//
//            "\t{\n" +
//
//            "\t\t\"before\": \"details-3-2\",\n" +
//
//            "\t\t\"after\": \"inner的括号2\",\n" +
//
//            "\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\"uuid\": \"占位符1\"\n" +
//
//            "\t},\n" +
//
//            "\t{\n" +
//
//            "\t\t\"before\": \"inner的括号2\",\n" +
//
//            "\t\t\"after\": \"undefined\",\n" +
//
//            "\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\"uuid\": \"占位符2\"\n" +
//
//            "\t}],\n" +
//
//            "\t\"id\": \"null\",\n" +
//
//            "\t\"uuid\": \"aadsa\",\n" +
//
//            "\t\"inner\": [\n" +
//
//            "\t{\n" +
//
//            "\t\t\"before\": \"占位符1\",\n" +
//
//            "\t\t\"operatorSign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\"after\": \"占位符2\",\n" +
//
//            "\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\"uuid\": \"inner的括号2\",\n" +
//
//            "\t\t\"details\": [{\n" +
//
//            "\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\"after\": \"details-13-2\",\n" +
//
//            "\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\"uuid\": \"占位符2-0\"\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"before\": \"占位符2-0\",\n" +
//
//            "\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\"value\": \"强关联2\"\n" +
//
//            "\t\t\t}],\n" +
//
//            "\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\"uuid\": \"details-13-2\",\n" +
//
//            "\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\"after\": \"占位符2-1\",\n" +
//
//            "\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\"\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\"after\": \"inner\",\n" +
//
//            "\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\"uuid\": \"占位符2-1\"\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"before\": \"inner\",\n" +
//
//            "\t\t\t\"after\": \"undefined\",\n" +
//
//            "\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\"uuid\": \"占位符2-3\"\n" +
//
//            "\t\t}],\n" +
//
//            "\t\t\"inner\": [\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"before\": \"占位符2-1\",\n" +
//
//            "\t\t\t\"after\": \"占位符2-3\",\n" +
//
//            "\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\"uuid\": \"inner\",\n" +
//
//            "\t\t\t\"details\": [{\n" +
//
//            "\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\"after\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\"uuid\": \"占位符3-1\"\n" +
//
//            "\t\t\t},\n" +
//
//            "\t\t\t{\n" +
//
//            "\t\t\t\t\"before\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"value\": \"强关联2\"\n" +
//
//            "\t\t\t\t}],\n" +
//
//            "\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\"uuid\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\"after\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\"\n" +
//
//            "\t\t\t},\n" +
//
//            "\t\t\t{\n" +
//
//            "\t\t\t\t\"before\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\"after\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\"uuid\": \"占位符3-2\"\n" +
//
//            "\t\t\t},\n" +
//
//            "\t\t\t{\n" +
//
//            "\t\t\t\t\"before\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\"after\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\"uuid\": \"占位符3-3\"\n" +
//
//            "\t\t\t},\n" +
//
//            "\t\t\t{\n" +
//
//            "\t\t\t\t\"before\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"value\": \"强关联2\"\n" +
//
//            "\t\t\t\t}],\n" +
//
//            "\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\"uuid\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\"after\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\"\n" +
//
//            "\t\t\t},\n" +
//
//            "\t\t\t{\n" +
//
//            "\t\t\t\t\"before\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\"after\": \"undefined\",\n" +
//
//            "\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\"uuid\": \"占位符3-4\"\n" +
//
//            "\t\t\t}],\n" +
//
//            "\t\t\t\"inner\": [{\n" +
//
//            "\t\t\t\t\"before\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\"after\": \"占位符3-5\",\n" +
//
//            "\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\"uuid\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\"parentId\": \"新定义的指标不需要此项\"\n" +
//
//            "\t\t\t}],\n" +
//
//            "\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\"parentId\": \"新定义的指标不需要此项\"\n" +
//
//            "\t\t}],\n" +
//
//            "\t\t\"parentId\": \"新定义的指标不需要此项\"\n" +
//
//            "\t}],\n" +
//
//            "\t\"parentId\": \"\"\n" +
//
//            "}";
//
//    private String template4="{\n" +
//
//            "\t\"operatorSign\": \"and\",\n" +
//
//            "\t\"uuid\": \"aadsa\",\n" +
//
//            "\t\"id\": null,\n" +
//
//            "\t\"parentId\": \"\",\n" +
//
//            "\t\"positioningBox\": [{\n" +
//
//            "\t\t\t\"uuid\": \"占位符0\",\n" +
//
//            "\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\"after\": \"details-3-2\"\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\"before\": \"占位符0\",\n" +
//
//            "\t\t\t\"after\": \"占位符1\",\n" +
//
//            "\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\"newDetails\": [{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t}\n" +
//
//            "\t\t\t]\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"占位符1\",\n" +
//
//            "\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\"before\": \"details-3-2\",\n" +
//
//            "\t\t\t\"after\": \"inner的括号2\"\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"inner的括号2\",\n" +
//
//            "\t\t\t\"before\": \"占位符1\",\n" +
//
//            "\t\t\t\"after\": \"占位符2\",\n" +
//
//            "\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\"operatorSign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\"positioningBox\": [{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"占位符2-0\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\"after\": \"details-13-2\"\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\"before\": \"占位符2-0\",\n" +
//
//            "\t\t\t\t\t\"after\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\"newDetails\": [{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t]\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\"after\": \"inner\"\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"inner\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\"before\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\"after\": \"占位符2-3\",\n" +
//
//            "\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\"positioningBox\": [{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-2-1\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\t\t\"newDetails\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-2-2\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符3-5\",\n" +
//
//            "\t\t\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\"positioningBox\": []\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-2-3\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\t\t\"newDetails\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t]\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"uuid\": \"占位符2-3\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\"before\": \"inner\",\n" +
//
//            "\t\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t\t}\n" +
//
//            "\t\t\t]\n" +
//
//            "\t\t},\n" +
//
//            "\t\t{\n" +
//
//            "\t\t\t\"uuid\": \"占位符2\",\n" +
//
//            "\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\"before\": \"inner的括号2\",\n" +
//
//            "\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t}\n" +
//
//            "\t]\n" +
//
//            "}";
//
//    private static String template5="{\n" +
//
//            "\t\"active\": {\n" +
//
//            "\t\t\"projectId\": \"09d8b1b0-a477-4667-8aaf-71ab7e156efc\",\n" +
//
//            "\t\t\"projectName\": \"聚合搜索\",\n" +
//
//            "\t\t\"activeId\": \"\",\n" +
//
//            "\t\t\"name\": \"纳排\",\n" +
//
//            "\t\t\"updateMode\": \"\",\n" +
//
//            "\t\t\"activeType\": 3,\n" +
//
//            "\t\t\"config\": [{\n" +
//
//            "\t\t\t\t\"activeResultDesc\": \"纳入标准\",\n" +
//
//            "\t\t\t\t\"activeResult\": \"纳入标准\",\n" +
//
//            "\t\t\t\t\"indexResultValue\": \"\",\n" +
//
//            "\t\t\t\t\"IndexResultValueIsEqual\": 1,\n" +
//
//            "\t\t\t\t\"operator\": \"\",\n" +
//
//            "\t\t\t\t\"operatorNum\": \"\",\n" +
//
//            "\t\t\t\t\"function\": \"\",\n" +
//
//            "\t\t\t\t\"functionParam\": \"\",\n" +
//
//            "\t\t\t\t\"id\": \"\",\n" +
//
//            "\t\t\t\t\"activeId\": \"\",\n" +
//
//            "\t\t\t\t\"indexColumn\": \" \",\n" +
//
//            "\t\t\t\t\"indexColumnDesc\": \"\",\n" +
//
//            "\t\t\t\t\"dateFormat\": \"\",\n" +
//
//            "\t\t\t\t\"searchScope\": \"\",\n" +
//
//            "\t\t\t\t\"indexType\": \"\",\n" +
//
//            "\t\t\t\t\"indexTypeDesc\": \"\",\n" +
//
//            "\t\t\t\t\"update_mode\": \"\",\n" +
//
//            "\t\t\t\t\"conditions\": [{\n" +
//
//            "\t\t\t\t\t\t\"operatorSign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\"uuid\": \"aadsa\",\n" +
//
//            "\t\t\t\t\t\t\"acceptanceState\": false,\n" +
//
//            "\t\t\t\t\t\t\"id\": null,\n" +
//
//            "\t\t\t\t\t\t\"parentId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\"positioningBox\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"uuid\": \"占位符0\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"after\": \"details-3-2\"\n" +
//
//            "\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"acceptanceState\": false,\n" +
//
//            "\t\t\t\t\t\t\t\t\"before\": \"占位符0\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"after\": \"占位符1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"Treedetails\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\"uuid\": \"占位符1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\"before\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"after\": \"inner的括号2\"\n" +
//
//            "\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\"uuid\": \"inner的括号2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"acceptanceState\": false,\n" +
//
//            "\t\t\t\t\t\t\t\t\"before\": \"占位符1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"after\": \"占位符2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"positioningBox\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符2-0\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"after\": \"details-13-2\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": false,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"before\": \"占位符2-0\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"after\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"Treedetails\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"before\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"after\": \"inner\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"uuid\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": false,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"before\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"after\": \"占位符2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"positioningBox\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"after\": \"details-2-1\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": false,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"before\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"after\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"Treedetails\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"before\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"after\": \"details-2-2\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"before\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"after\": \"占位符3-5\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"positioningBox\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"before\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"after\": \"details-2-3\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": false,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"before\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"after\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"Treedetails\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"before\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"before\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\"uuid\": \"占位符2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\"before\": \"inner的括号2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\"operatorSign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\"uuid\": \"aadsa\",\n" +
//
//            "\t\t\t\t\t\t\"acceptanceState\": false,\n" +
//
//            "\t\t\t\t\t\t\"id\": null,\n" +
//
//            "\t\t\t\t\t\t\"parentId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\"positioningBox\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"uuid\": \"占位符0\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"after\": \"details-3-2\"\n" +
//
//            "\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"acceptanceState\": false,\n" +
//
//            "\t\t\t\t\t\t\t\t\"before\": \"占位符0\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"after\": \"占位符1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"Treedetails\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\"uuid\": \"占位符1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\"before\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"after\": \"inner的括号2\"\n" +
//
//            "\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\"uuid\": \"inner的括号2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"acceptanceState\": false,\n" +
//
//            "\t\t\t\t\t\t\t\t\"before\": \"占位符1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"after\": \"占位符2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"positioningBox\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符2-0\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"after\": \"details-13-2\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": false,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"before\": \"占位符2-0\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"after\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"Treedetails\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"before\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"after\": \"inner\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"uuid\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": false,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"before\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"after\": \"占位符2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"positioningBox\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"after\": \"details-2-1\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": false,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"before\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"after\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"Treedetails\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"before\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"after\": \"details-2-2\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"before\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"after\": \"占位符3-5\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"operato Sign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"parentId\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"positioningBox\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"before\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"after\": \"details-2-3\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": false,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"before\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"after\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"Treedetails\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"before\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"before\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\"uuid\": \"占位符2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\"before\": \"inner的括号2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"after\": \"undefined\"\n" +
//
//            "\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t}\n" +
//
//            "\t\t\t\t]\n" +
//
//            "\t\t\t},\n" +
//
//            "\t\t\t{\n" +
//
//            "\t\t\t\t\"activeResultDesc\": \"排除标准\",\n" +
//
//            "\t\t\t\t\"activeResult\": \"排除标准\",\n" +
//
//            "\t\t\t\t\"indexResultValue\": \"\",\n" +
//
//            "\t\t\t\t\"IndexResultValueIsEqual\": 1,\n" +
//
//            "\t\t\t\t\"operator\": \"\",\n" +
//
//            "\t\t\t\t\"operatorNum\": \"\",\n" +
//
//            "\t\t\t\t\"function\": \"\",\n" +
//
//            "\t\t\t\t\"functionParam\": \"\",\n" +
//
//            "\t\t\t\t\"id\": \"\",\n" +
//
//            "\t\t\t\t\"activeId\": \"\",\n" +
//
//            "\t\t\t\t\"indexColumn\": \" \",\n" +
//
//            "\t\t\t\t\"indexColumnDesc\": \"\",\n" +
//
//            "\t\t\t\t\"dateFormat\": \"\",\n" +
//
//            "\t\t\t\t\"searchScope\": \"\",\n" +
//
//            "\t\t\t\t\"indexType\": \"\",\n" +
//
//            "\t\t\t\t\"indexTypeDesc\": \"\",\n" +
//
//            "\t\t\t\t\"update_mode\": \"\",\n" +
//
//            "\t\t\t\t\"conditions\": [{\n" +
//
//            "\t\t\t\t\t\"operatorSign\": \"and\",\n" +
//
//            "\t\t\t\t\t\"uuid\": \"aadsa\",\n" +
//
//            "\t\t\t\t\t\"acceptanceState\": false,\n" +
//
//            "\t\t\t\t\t\"id\": null,\n" +
//
//            "\t\t\t\t\t\"parentId\": \"\",\n" +
//
//            "\t\t\t\t\t\"positioningBox\": [{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符0\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-3-2\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\"acceptanceState\": false,\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符0\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符1\",\n" +
//
//            "\t\t\t\t\t\t\t\"Treedetails\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operator_sign\": \"and\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"dataType\": \"数据类型（日期、数值、字符串、整数、浮点数据、枚举）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"dateFormat\": \"日期格式（yyyy-MM-dd HH:mm:ss）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\",\n" +
//
//            "\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"isShowInPage\": true,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"id\": \"新定义的指标不需要此项\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dataType\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"dateFormat\": \"新增\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"strongRef\": []\n" +
//
//            "\t\t\t\t\t\t\t\t\t}]\n" +
//
//            "\t\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t\t]\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符1\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"acceptanceState\": true,\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"inner的括号2\"\n" +
//
//            "\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t]\n" +
//
//            "\t\t\t\t}]\n" +
//
//            "\t\t\t}\n" +
//
//            "\t\t]\n" +
//
//            "\t},\n" +
//
//            "\t\"crfId\": \"EMR\",\n" +
//
//            "\t\"isSearch\": 1\n" +
//
//            "}";
//
//    private String template6="{\n" +
//
//            "\t\"data\": {\n" +
//
//            "\t\t\"activeType\": 3,\n" +
//
//            "\t\t\"config\": [{\n" +
//
//            "\t\t\t\"activeIndexId\": \"3D221C09C8BF4DEEAB6DB6887A9FEBB9\",\n" +
//
//            "\t\t\t\"activeResult\": \"纳入标准\",\n" +
//
//            "\t\t\t\"activeResultDesc\": \"纳入标准\",\n" +
//
//            "\t\t\t\"conditions\": [{\n" +
//
//            "\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\"after\": \"占位符2\",\n" +
//
//            "\t\t\t\t\t\"before\": \"占位符1\",\n" +
//
//            "\n" +
//
//            "\t\t\t\t\t\"details\": [{\n" +
//
//            "\t\t\t\t\t\t\t\"acceptanceState\": 1,\n" +
//
//            "\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"3B466D02F02844E3B17B123B5435EC99\",\n" +
//
//            "\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"parentId\": \"06F990FE426F42C29B993D672DAF1783\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符0\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"acceptanceState\": 0,\n" +
//
//            "\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符1\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符0\",\n" +
//
//            "\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"3FC5CFF76A2C4C1C90A9DD788B9C5DEF\",\n" +
//
//            "\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\"parentId\": \"06F990FE426F42C29B993D672DAF1783\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"C28A1EF79B1D4CB2829D26CE65874D55\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"parentId\": \"3FC5CFF76A2C4C1C90A9DD788B9C5DEF\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"type\": 3,\n" +
//
//            "\t\t\t\t\t\t\t\t\"value\": \"强关联2\"\n" +
//
//            "\t\t\t\t\t\t\t}],\n" +
//
//            "\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"acceptanceState\": 0,\n" +
//
//            "\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符1\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符0\",\n" +
//
//            "\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"A60E88BEA234496FB48253001CA48D6C\",\n" +
//
//            "\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\"parentId\": \"06F990FE426F42C29B993D672DAF1783\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"464D14CBD9AD4C63A0D7163D94A7D988\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"parentId\": \"A60E88BEA234496FB48253001CA48D6C\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"type\": 3,\n" +
//
//            "\t\t\t\t\t\t\t\t\"value\": \"强关联2\"\n" +
//
//            "\t\t\t\t\t\t\t}],\n" +
//
//            "\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"acceptanceState\": 1,\n" +
//
//            "\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"inner的括号2\",\n" +
//
//            "\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"BA4895A67A3F436AB0D0DE4C6C0D8268\",\n" +
//
//            "\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"parentId\": \"06F990FE426F42C29B993D672DAF1783\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符2\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"acceptanceState\": 1,\n" +
//
//            "\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"inner的括号2\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"BAF1B95A092C4D92AC72428A03553949\",\n" +
//
//            "\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"parentId\": \"06F990FE426F42C29B993D672DAF1783\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符1\"\n" +
//
//            "\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t],\n" +
//
//            "\t\t\t\t\t\"id\": \"06F990FE426F42C29B993D672DAF1783\",\n" +
//
//            "\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\"level\": 1,\n" +
//
//            "\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\"operatorSign\": \"and\",\n" +
//
//            "\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\"type\": 1,\n" +
//
//            "\t\t\t\t\t\"uuid\": \"aadsa\"\n" +
//
//            "\t\t\t\t},\n" +
//
//            "\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\"after\": \"占位符2\",\n" +
//
//            "\t\t\t\t\t\"before\": \"占位符1\",\n" +
//
//            "\t\t\t\t\t\"details\": [{\n" +
//
//            "\t\t\t\t\t\t\t\"acceptanceState\": 1,\n" +
//
//            "\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"inner的括号2\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"0AAD06C748D94698A5255CB065AA3A00\",\n" +
//
//            "\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"parentId\": \"7D00D8306EA84D959F2F0E65CE4F4956\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符1\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"acceptanceState\": 1,\n" +
//
//            "\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"inner的括号2\",\n" +
//
//            "\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"453459364B954398B517708C335DE1B1\",\n" +
//
//            "\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"parentId\": \"7D00D8306EA84D959F2F0E65CE4F4956\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符2\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"acceptanceState\": 0,\n" +
//
//            "\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符1\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符0\",\n" +
//
//            "\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"C6DD27FA877041BB9019F66918BB068B\",\n" +
//
//            "\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\"parentId\": \"7D00D8306EA84D959F2F0E65CE4F4956\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"6B08FC9C972146E0BC6E23D4A86086DA\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"parentId\": \"C6DD27FA877041BB9019F66918BB068B\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"type\": 3,\n" +
//
//            "\t\t\t\t\t\t\t\t\"value\": \"强关联2\"\n" +
//
//            "\t\t\t\t\t\t\t}],\n" +
//
//            "\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"acceptanceState\": 0,\n" +
//
//            "\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符1\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符0\",\n" +
//
//            "\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"D98769F57387403C803D6BDB06FD263C\",\n" +
//
//            "\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\"parentId\": \"7D00D8306EA84D959F2F0E65CE4F4956\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"1A63FD3105E84200B60F0416CBD897BD\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"parentId\": \"D98769F57387403C803D6BDB06FD263C\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"type\": 3,\n" +
//
//            "\t\t\t\t\t\t\t\t\"value\": \"强关联2\"\n" +
//
//            "\t\t\t\t\t\t\t}],\n" +
//
//            "\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\"\n" +
//
//            "\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\"acceptanceState\": 1,\n" +
//
//            "\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"details-3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"F6A31AAC2250407E9C7AC26D23B5F46A\",\n" +
//
//            "\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\"parentId\": \"7D00D8306EA84D959F2F0E65CE4F4956\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"占位符0\"\n" +
//
//            "\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t],\n" +
//
//            "\t\t\t\t\t\"id\": \"7D00D8306EA84D959F2F0E65CE4F4956\",\n" +
//
//            "\t\t\t\t\t\"inner\": [{\n" +
//
//            "\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\"after\": \"占位符2-3\",\n" +
//
//            "\t\t\t\t\t\t\"before\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\t\"details\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"acceptanceState\": 0,\n" +
//
//            "\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"after\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"before\": \"占位符2-0\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"0E31AAE596FE41088BE73C9A4586A7DB\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"parentId\": \"B8417FA246FF40459FAA08FAE579BB67\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"id\": \"A3B8D2B5F96A4EB1A7F2FBC102AA2C90\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"parentId\": \"0E31AAE596FE41088BE73C9A4586A7DB\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"type\": 3,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\"\n" +
//
//            "\t\t\t\t\t\t\t\t}],\n" +
//
//            "\t\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\t\"uuid\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\"\n" +
//
//            "\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\"acceptanceState\": 0,\n" +
//
//            "\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"after\": \"占位符2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"before\": \"占位符2-0\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"299B2A44ED584CA78CB82F57A6104839\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"parentId\": \"B8417FA246FF40459FAA08FAE579BB67\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"id\": \"7C42B9491A934036A7FCE68382BA3F0B\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"parentId\": \"299B2A44ED584CA78CB82F57A6104839\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"type\": 3,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\"\n" +
//
//            "\t\t\t\t\t\t\t\t}],\n" +
//
//            "\t\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\t\"uuid\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\"\n" +
//
//            "\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\"acceptanceState\": 1,\n" +
//
//            "\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"after\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"579ACF0D047B42D1BF8420FD3523ACB6\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"parentId\": \"B8417FA246FF40459FAA08FAE579BB67\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\t\"uuid\": \"占位符2-0\"\n" +
//
//            "\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\"acceptanceState\": 1,\n" +
//
//            "\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"after\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"before\": \"details-13-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"86678A69F6424DECA996D97E95155FB6\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"parentId\": \"B8417FA246FF40459FAA08FAE579BB67\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\t\"uuid\": \"占位符2-1\"\n" +
//
//            "\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\"acceptanceState\": 1,\n" +
//
//            "\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"after\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"before\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"EA7739FECE4D4CF08AF7B1BE4B2D05EA\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"parentId\": \"B8417FA246FF40459FAA08FAE579BB67\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\t\"uuid\": \"占位符2-3\"\n" +
//
//            "\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t],\n" +
//
//            "\t\t\t\t\t\t\"id\": \"B8417FA246FF40459FAA08FAE579BB67\",\n" +
//
//            "\t\t\t\t\t\t\"inner\": [{\n" +
//
//            "\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\"after\": \"占位符3-5\",\n" +
//
//            "\t\t\t\t\t\t\t\"before\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\"details\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"acceptanceState\": 0,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"after\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"before\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"id\": \"13876FB274F04975892B8D8D767A4CCD\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"parentId\": \"7D20E6F2ECC946B1B68033FB5709B5C1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"id\": \"CB552B9D41D343EAB3D3FB8DFCFAE3BD\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"parentId\": \"13876FB274F04975892B8D8D767A4CCD\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"type\": 3,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t}],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\"\n" +
//
//            "\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"acceptanceState\": 1,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"after\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"before\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"id\": \"67E5C00BC3D74D14B46F88918A3689C6\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"parentId\": \"7D20E6F2ECC946B1B68033FB5709B5C1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符3-4\"\n" +
//
//            "\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"acceptanceState\": 1,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"after\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"before\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"id\": \"7B6E41DA2BC643A9BF74D98A00A2F99C\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"parentId\": \"7D20E6F2ECC946B1B68033FB5709B5C1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符3-2\"\n" +
//
//            "\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"acceptanceState\": 0,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"after\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"before\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"id\": \"7EA45CC6EFE34795A858AC3FC5D8A2CD\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"parentId\": \"7D20E6F2ECC946B1B68033FB5709B5C1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"id\": \"60B2D6405ED646019A6B4CCBC5B8A677\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"parentId\": \"7EA45CC6EFE34795A858AC3FC5D8A2CD\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"type\": 3,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t}],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\"\n" +
//
//            "\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"acceptanceState\": 1,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"after\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"before\": \"undefined\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"id\": \"9D83206E18AA4E8AA2E4AADB32421D6F\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"parentId\": \"7D20E6F2ECC946B1B68033FB5709B5C1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符3-1\"\n" +
//
//            "\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"acceptanceState\": 0,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"after\": \"占位符3-4\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"before\": \"占位符3-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"id\": \"AD8D47EF3CA84724AB77499F1500ADD2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"parentId\": \"7D20E6F2ECC946B1B68033FB5709B5C1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"id\": \"E8E6CEB32B2B45D6AF723C3D2ABA39F1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"parentId\": \"AD8D47EF3CA84724AB77499F1500ADD2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"type\": 3,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t}],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\"\n" +
//
//            "\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"acceptanceState\": 0,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"after\": \"占位符3-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"before\": \"占位符3-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"id\": \"C5674F0347574E30998C0214C7000DB4\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"nodeType\": \"details\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSign\": \"操作符（!contain）\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"parentId\": \"7D20E6F2ECC946B1B68033FB5709B5C1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveId\": \"引用事件Id\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refActiveName\": \"引用事件名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"refRelation\": \"字段和值的比较方式 (direct,ref other index)\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.SPECIMEN_NAME\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.标本名称\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"strongRef\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"id\": \"7DF0250DB73B44B59EFD1689E23BA6ED\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSign\": \"!contain\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"operatorSignDesc\": \"不包含\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"parentId\": \"C5674F0347574E30998C0214C7000DB4\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveId\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refActiveName\": \"\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"refRelation\": \"direct\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagName\": \"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"sourceTagNameDesc\": \"就诊.检验报告.检验子项.检验子项中文名\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"type\": 3,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\t\"value\": \"强关联2\"\n" +
//
//            "\t\t\t\t\t\t\t\t\t}],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"uuid\": \"details-2-1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"value\": \"2011-11-11 条件数值（肿瘤）\"\n" +
//
//            "\t\t\t\t\t\t\t\t},\n" +
//
//            "\t\t\t\t\t\t\t\t{\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"acceptanceState\": 1,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"after\": \"details-2-3\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"before\": \"details-2-2\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"id\": \"E8780F10AA424A5EA03007B6A71AB0DE\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"nodeType\": \"placeholder\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"parentId\": \"7D20E6F2ECC946B1B68033FB5709B5C1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"type\": 2,\n" +
//
//            "\t\t\t\t\t\t\t\t\t\"uuid\": \"占位符3-3\"\n" +
//
//            "\t\t\t\t\t\t\t\t}\n" +
//
//            "\t\t\t\t\t\t\t],\n" +
//
//            "\t\t\t\t\t\t\t\"id\": \"7D20E6F2ECC946B1B68033FB5709B5C1\",\n" +
//
//            "\t\t\t\t\t\t\t\"inner\": [{\n" +
//
//            "\t\t\t\t\t\t\t\t\"activeIndexConfigId\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"detail\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"details\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"id\": \"92E7B6A20CCA444699F4C4452556E313\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"inner\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"level\": 4,\n" +
//
//            "\t\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"parentId\": \"7D20E6F2ECC946B1B68033FB5709B5C1\",\n" +
//
//            "\t\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\t\"type\": 1,\n" +
//
//            "\t\t\t\t\t\t\t\t\"uuid\": \"details-2-2\"\n" +
//
//            "\t\t\t\t\t\t\t}],\n" +
//
//            "\t\t\t\t\t\t\t\"level\": 3,\n" +
//
//            "\t\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\t\"parentId\": \"B8417FA246FF40459FAA08FAE579BB67\",\n" +
//
//            "\t\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\t\"type\": 1,\n" +
//
//            "\t\t\t\t\t\t\t\"uuid\": \"inner\"\n" +
//
//            "\t\t\t\t\t\t}],\n" +
//
//            "\t\t\t\t\t\t\"level\": 2,\n" +
//
//            "\t\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\t\"operatorSign\": \"操作符 （and，or,not)\",\n" +
//
//            "\t\t\t\t\t\t\"parentId\": \"7D00D8306EA84D959F2F0E65CE4F4956\",\n" +
//
//            "\t\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\t\"type\": 1,\n" +
//
//            "\t\t\t\t\t\t\"uuid\": \"inner的括号2\"\n" +
//
//            "\t\t\t\t\t}],\n" +
//
//            "\t\t\t\t\t\"level\": 1,\n" +
//
//            "\t\t\t\t\t\"needPath\": \".\",\n" +
//
//            "\t\t\t\t\t\"nodeType\": \"inner\",\n" +
//
//            "\t\t\t\t\t\"operatorSign\": \"and\",\n" +
//
//            "\t\t\t\t\t\"strongRef\": [],\n" +
//
//            "\t\t\t\t\t\"type\": 1,\n" +
//
//            "\t\t\t\t\t\"uuid\": \"aadsa\"\n" +
//
//            "\t\t\t\t}\n" +
//
//            "\t\t\t],\n" +
//
//            "\t\t\t\"dateFormat\": \"\",\n" +
//
//            "\t\t\t\"function\": \"\",\n" +
//
//            "\t\t\t\"functionParam\": \"\",\n" +
//
//            "\t\t\t\"id\": \"52D406B85E4F4265A566F4884AACEE94\",\n" +
//
//            "\t\t\t\"indexColumn\": \"\",\n" +
//
//            "\t\t\t\"indexColumnDesc\": \"\",\n" +
//
//            "\t\t\t\"indexResultValue\": \"\",\n" +
//
//            "\t\t\t\"indexResultValueIsEqual\": \"1\",\n" +
//
//            "\t\t\t\"indexType\": \"\",\n" +
//
//            "\t\t\t\"indexTypeDesc\": \"\",\n" +
//
//            "\t\t\t\"operator\": \"\",\n" +
//
//            "\t\t\t\"operatorNum\": \"\",\n" +
//
//            "\t\t\t\"searchScope\": \"\"\n" +
//
//            "\t\t}],\n" +
//
//            "\t\t\"confirmActiveId\": \"2D9CB39672A941B1847B21B071B9F628\",\n" +
//
//            "\t\t\"createTime\": 1531561974000,\n" +
//
//            "\t\t\"dataGroup\": \"[\\\"patient_info\\\",\\\"inspection_reports\\\"]\",\n" +
//
//            "\t\t\"id\": \"3D221C09C8BF4DEEAB6DB6887A9FEBB9\",\n" +
//
//            "\t\t\"isTmp\": 1,\n" +
//
//            "\t\t\"name\": \"纳排\",\n" +
//
//            "\t\t\"projectId\": \"09d8b1b0-a477-4667-8aaf-71ab7e156efc\",\n" +
//
//            "\t\t\"projectName\": \"聚合搜索\",\n" +
//
//            "\t\t\"sortKey\": \"\",\n" +
//
//            "\t\t\"updateTime\": 1531561974000\n" +
//
//            "\t},\n" +
//
//            "\t\"flag\": true,\n" +
//
//            "\t\"message\": \"操作成功\",\n" +
//
//            "\t\"status\": 200\n" +
//
//            "}";
//
//    @BeforeClass
//
//    public static void before() {
//
//        task = new ConcurrentLinkedQueue<String>();
//
//        resultMap = new ConcurrentHashMap<String, Object>();
//
//        thread = new ConcurrentHashMap<String, Thread>();
//
//    }
//
//
//
//
//
//    /**
//
//     * 前端转后端
//
//     */
//
//    @Test
//
//    public void convertNewToBack() {
//
//        JSONObject json = JSONObject.parseObject(template5);
//
//        //System.out.println(template2);
//
//        //所有的details
//
//        JSONObject result = new JSONObject();
//
//        basicInfo(json, result);
//
//        firstIterator(json, result);
//
//        JSONArray inners = result.getJSONArray("inner");
//
//        convertDetails(result);
//
//        //遍历inner
//
//        iteratorAllInners(inners);
//
//        result.put("inner", inners);
//
//        System.out.println(JSON.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect));
//
//        JSONObject rs = JSONObject.parseObject(template6);
//
//        JSONObject data = rs.getJSONObject("data");
//
//        JSONObject config = data.getJSONArray("config").getJSONObject(0);
//
//        JSONArray conditions = config.getJSONArray("conditions");
//
//        JSONObject jsonObject = conditions.getJSONObject(0);
//
//        jsonObject.remove("detail");
//
//
//
//        convertToFront(result);
//
//    }
//
//    @Test
//    public void convertTo(){
//
//        JSONObject rs = JSONObject.parseObject(template6);
//
//        System.out.println(template6);
//
//        JSONObject data = rs.getJSONObject("data");
//
//        JSONObject config = data.getJSONArray("config").getJSONObject(0);
//
//        JSONArray conditions = config.getJSONArray("conditions");
//
//        JSONObject jsonObject = conditions.getJSONObject(1);
//
//        jsonObject.remove("detail");
//
//        JSONArray positioningBox = new JSONArray();
//
//        JSONArray details = jsonObject.getJSONArray("details");
//
//        convertToUiDetail(details,positioningBox);
//
//        JSONArray inner = jsonObject.getJSONArray("inner");
//
//        JSONObject result = new JSONObject();
//
//        convertToUiInner(positioningBox,inner);
//
//        List<String> exist = new ArrayList<String>();
//
//        result.put("positioningBox",positioningBox);
//
//        sort(result,exist);
//
//        System.out.println(result);
//
//    }
//
//    public void convertToUiInner(JSONArray positioningBox,JSONArray inners){
//
//        int size = inners == null ? 0 : inners.size();
//
//        for (int i = 0; i<size; i++) {
//
//            JSONArray innerBox = new JSONArray();
//
//            JSONObject inR = new JSONObject();
//
//            JSONObject inner = inners.getJSONObject(i);
//
//            basicInfo(inner,inR);
//
//            JSONArray details = inner.getJSONArray("details");
//
//            convertToUiDetail(details,innerBox);
//
//            inR.put("positioningBox",innerBox);
//
//            positioningBox.add(inR);
//
//            JSONArray innerss = inner.getJSONArray("inner");
//
//            convertToUiInner(innerBox,innerss);
//
//        }
//
//    }
//
//    public void convertToUiDetail(JSONArray details,JSONArray positioningBox){
//
//        int size = details == null ? 0 : details.size();
//
//        Map<String,JSONArray> Treedetails = new ConcurrentHashMap<String, JSONArray>();
//
//        for (int i = 0; i<size; i++) {
//
//            JSONObject detail = details.getJSONObject(i);
//
//            String uuid = detail.getString("uuid");
//
//            if(Treedetails.containsKey(uuid)){
//
//                JSONArray array = Treedetails.get(uuid);
//
//                array.add(detail);
//
//                Treedetails.put(uuid,array);
//
//            }else{
//
//                JSONArray array = new JSONArray();
//
//                array.add(detail);
//
//                Treedetails.put(uuid,array);
//
//            }
//
//        }
//
//        Set<String> keys = Treedetails.keySet();
//
//        for (String key:keys) {
//
//            JSONArray array = Treedetails.get(key);
//
//            int sizes = array == null ? 0 : array.size();
//
//            if(sizes>0){
//
//                JSONObject jsonObject = array.getJSONObject(0);
//
//                String nodeType = jsonObject.getString("nodeType");
//
//                if("placeholder".equals(nodeType)){
//
//                    positioningBox.add(jsonObject);
//
//                }else {
//
//                    JSONObject result = new JSONObject();
//
//                    result.put("uuid", jsonObject.getString("uuid"));
//
//                    result.put("nodeType", jsonObject.getString("nodeType"));
//
//                    result.put("before", jsonObject.getString("before"));
//
//                    result.put("after", jsonObject.getString("after"));
//
//                    result.put("acceptanceState", jsonObject.getInteger("acceptanceState"));
//
//                    result.put("Treedetails", array);
//
//                    positioningBox.add(result);
//
//                }
//
//            }
//
//        }
//
//        System.out.println();
//
//    }
//
//    public void convertDetails(JSONObject result){
//
//        JSONArray details = result.getJSONArray("details");
//
//        int size = details == null ? 0 : details.size();
//
//        JSONArray newDetails = new JSONArray();
//
//        for (int i = 0; i<size; i++) {
//
//            JSONObject detail = details.getJSONObject(i);
//
//            if(detail.containsKey("newDetails")){
//
//                JSONArray newDetails1 = detail.getJSONArray("newDetails");
//
//                int i1 = newDetails == null ? 0 : newDetails1.size();
//
//                for (int j = 0; j<i1;j++) {
//
//                    JSONObject jsonObject = newDetails1.getJSONObject(j);
//
//                    newDetails.add(jsonObject);
//
//                }
//
//
//
//            }else{
//
//                newDetails.add(detail);
//
//            }
//
//        }
//
//        result.put("details",newDetails);
//
//    }
//
//    public void convertToFront(JSONObject source) {
//
//        JSONObject result = new JSONObject();
//
//        basicInfo(source, result);
//
//        JSONArray positioningBox = new JSONArray();
//
//        JSONArray details = source.getJSONArray("details");
//
//        convertDetails(details, positioningBox);
//
//        JSONArray inners = source.getJSONArray("inner");
//
//        convertInner(inners, positioningBox);
//
//        result.put("positioningBox", positioningBox);
//
//        List<String> exist = new ArrayList<String>();
//
//        sort(result,exist);
//
//        System.out.println(JSON.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect));
//
//    }
//
//    public void sort(JSONObject active,List<String> exist){
//
//        JSONArray positioningBox = active.getJSONArray("positioningBox");
//
//        int size = positioningBox == null ? 0 : positioningBox.size();
//
//        if(size == 0){
//
//            return;
//
//        }
//
//        JSONObject first = findFirt(positioningBox);
//
//        JSONArray sortAfter = new JSONArray();
//
//        sortAfter.add(first);
//
//        sortBox(positioningBox, first.getString("after"), sortAfter,exist);
//
//        active.put("positioningBox",sortAfter);
//
//        for (int i = 0; i<size; i++) {
//
//            JSONObject jsonObject = positioningBox.getJSONObject(i);
//
//            sort(jsonObject,exist);
//
//        }
//
//
//
//    }
//
//
//
//    public void convertInner(JSONArray inners, JSONArray positioningBox) {
//
//        int size = inners == null ? 0 : inners.size();
//
//        JSONArray inBox = new JSONArray();
//
//        JSONObject result = new JSONObject();
//
//        for (int i = 0; i < size; i++) {
//
//            JSONObject inner = inners.getJSONObject(i);
//
//            if(inner.containsKey("uuid")){
//
//
//
//            }
//
//            basicInfo(inner, result);
//
//            JSONArray details = inner.getJSONArray("details");
//
//            convertDetails(details, inBox);
//
//            JSONArray inn = inner.getJSONArray("inner");
//
//            if(inn!=null&&!inn.isEmpty()){
//
//                convertInner(inn, inBox);
//
//            }
//
//            result.put("positioningBox", inBox);
//
//            positioningBox.add(result);
//
//        }
//
//    }
//
//
//
//    public JSONObject findFirt(JSONArray box) {
//
//        int size = box == null ? 0 : box.size();
//
//        for (int i = 0; i < size; i++) {
//
//            JSONObject jsonObject = box.getJSONObject(i);
//
//            String before = jsonObject.getString("before");
//
//            if (before != null && before.equals("undefined")) {
//
//                return jsonObject;
//
//            }
//
//
//
//        }
//
//        return null;
//
//    }
//
//
//
//    public void sortBox(JSONArray box, String after, JSONArray sortAfter,List<String> exist) {
//
//        int size = box == null ? 0 : box.size();
//
//        if(size == 0){
//
//            return;
//
//        }
//
//        for (int i = 0; i < size; i++) {
//
//            JSONObject jsonObject = box.getJSONObject(i);
//
//            String uuid = jsonObject.getString("uuid");
//
//            if (after != null && after.equals(uuid)) {
//
//                int sizes = sortAfter == null ? 0 : sortAfter.size();
//
//                boolean exists = isExists(sortAfter, uuid, sizes);
//
//                if(!exists){
//
//                    sortAfter.add(jsonObject);
//
//                }
//
//                sortBox(box, jsonObject.getString("after"), sortAfter,exist);
//
//            }
//
//        }
//
//
//
//    }
//
//
//
//    private boolean isExists(JSONArray sortAfter, String uuid, int sizes) {
//
//        boolean exists = false;
//
//        for(int j = 0; j<sizes;j++){
//
//            JSONObject jsonObject1 = sortAfter.getJSONObject(j);
//
//            if(uuid!=null&&uuid.equals(jsonObject1.getString("uuid"))){
//
//                exists = true;
//
//            }
//
//        }
//
//        return exists;
//
//    }
//
//
//
//
//
//    public void convertDetails(JSONArray details, JSONArray positioningBox) {
//
//        int size = details == null ? 0 : details.size();
//
//        Set<String> exist = new HashSet<String>();
//
//        for (int j = 0; j<size; j++) {
//
//            JSONArray newDetails = new JSONArray();
//
//            Map<String,String> map = new ConcurrentHashMap<String, String>();
//
//            JSONObject detail = details.getJSONObject(j);
//
//            String uuid1 = detail.getString("uuid");
//
//            if(exist.contains(uuid1)){
//
//                continue;
//
//            }
//
//            JSONObject newDetail = new JSONObject();
//
//            basicInfo(detail,newDetail);
//
//            for (int i = 0; i < size; i++) {
//
//                JSONObject det = new JSONObject();
//
//                JSONObject now = details.getJSONObject(i);
//
//                det.putAll(now);
//
//                String uuid = det.getString("uuid");
//
//                boolean isContain = !exist.contains(uuid);
//
//                boolean isMapContain = map.keySet().size() == 0 || map.containsKey(uuid);
//
//                if(isMapContain && isContain && !"placeholder".equals(det.getString("nodeType"))){
//
//                    newDetails.add(det);
//
//                    map.put(uuid,uuid);
//
//                }else{
//
//                    exist.add(uuid1);
//
//                    if(isContain){
//
//                        positioningBox.add(det);
//
//
//
//                    }
//
//                }
//
//            }
//
//            if(newDetails.size()>0){
//
//
//
//                newDetail.put("newDetails",newDetails);
//
//                positioningBox.add(newDetail);
//
//            }
//
//            exist.add(uuid1);
//
//        }
//
//    }
//
//
//
//    public void iteratorAllInners(JSONArray inners) {
//
//        int size = inners == null ? 0 : inners.size();
//
//        if (inners == null || inners.isEmpty()) {
//
//            return;
//
//        }
//
//        for (int i = 0; i < size; i++) {
//
//            JSONObject jsonObject = inners.getJSONObject(i);
//
//            JSONArray inner1 = jsonObject.getJSONArray("inner");
//
//            convertDetails(jsonObject);
//
//            /*if (inner1 != null && inner1.size() > 0 && details != null && details.size() > 0) {
//
//                JSONArray array = new JSONArray();
//
//                array.add(jsonObject);
//
//                jsonObject.put("inner", array);
//
//            }*/
//
//            jsonObject.remove("positioningBox");
//
//            iteratorAllInners(inner1);
//
//        }
//
//    }
//
//
//
//    public void basicInfo(JSONObject json, JSONObject result) {
//
//        String operatorSign = json.getString("operatorSign");
//
//        String uuid = json.getString("uuid");
//
//        String before = json.getString("before");
//
//        String after = json.getString("after");
//
//        String nodeType = json.getString("nodeType");
//
//        String id = json.getString("id") == null ? "" : json.getString("id");
//
//        String parentId = json.getString("parentId") == null ? "" : json.getString("parentId");
//
//        result.put("operatorSign", operatorSign);
//
//        result.put("uuid", uuid);
//
//        result.put("id", id);
//
//        if (before != null && !"".equals(before)) {
//
//            result.put("before", before);
//
//        }
//
//        if (after != null && !"".equals(after)) {
//
//            result.put("after", after);
//
//        }
//
//        if (nodeType != null && !"".equals(nodeType)) {
//
//            result.put("nodeType", nodeType);
//
//        }
//
//        result.put("parentId", parentId);
//
//    }
//
//
//
//    public void firstIterator(JSONObject json, JSONObject result) {
//
//        JSONArray positioningBox = json.getJSONArray("positioningBox");
//
//        int size = (positioningBox == null || positioningBox.isEmpty()) ? 0 : positioningBox.size();
//
//        JSONArray details = new JSONArray();
//
//        JSONArray inners = new JSONArray();
//
//        for (int i = 0; i < size; i++) {
//
//            JSONObject boxItem = positioningBox.getJSONObject(i);
//
//            String nodeType = getDataType(boxItem);
//
//            if ("details".equals(nodeType)) {
//
//                details.add(boxItem);
//
//                /*if(boxItem!=null&&boxItem.containsKey("newDetails")){
//
//                 *//*JSONArray newDetails = boxItem.getJSONArray("newDetails");
//
//                    int i1 = newDetails == null ? 0 : newDetails.size();
//
//                    for (int n = 0; n<i1; n++) {*//*
//
//                        details.add(boxItem);*//**//*
//
//                }else{
//
//                    details.add(boxItem);
//
//                }*/
//
//            } else {
//
//                JSONObject res = new JSONObject();
//
//                basicInfo(boxItem, res);
//
//                inners.add(res);
//
//                firstIterator(boxItem, res);
//
//            }
//
//        }
//
//        result.put("details", details);
//
//        result.put("inner", inners);
//
//    }
//
//
//
//    public String getDataType(JSONObject json) {
//
//        String nodeType = json.getString("nodeType");
//
//        if ("placeholder".equals(nodeType) || "details".equals(nodeType)) {
//
//            return "details";
//
//        } else {
//
//            return "inner";
//
//        }
//
//    }
//
//
//
//    @Test
//
//    public void convert() {
//
//        JSONArray array = JSONArray.parseArray(template);
//
//        int size = array.size();
//
//        JSONArray arrays = new JSONArray();
//
//        JSONArray inner = new JSONArray();
//
//        for (int i = 0; i < size; i++) {
//
//            JSONObject jsonObject = array.getJSONObject(i);
//
//            String nodeType = jsonObject.getString("nodeType");
//
//            String operator_sign = jsonObject.getString("operator_Sign");
//
//            if ("placeholder".equals(nodeType)) {
//
//                arrays.add(jsonObject);
//
//            } else if ("details".equals(nodeType)) {
//
//                JSONObject object = new JSONObject();
//
//                JSONArray ar = new JSONArray();
//
//                ar.add(jsonObject);
//
//                object.put("operatorSign", operator_sign);
//
//                object.put("details", ar);
//
//                arrays.add(object);
//
//            } else {
//
//                ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> onBox = new ConcurrentHashMap<String, ConcurrentHashMap<String, Object>>();
//
//                iteratorBox(jsonObject, onBox);
//
//
//
//            }
//
//
//
//        }
//
//        System.out.println(array);
//
//        /*int size1 = arrays.size();
//
//        JSONArray ay = new JSONArray();
//
//        for (int i = 0; i<size1; i++) {
//
//            JSONObject jsonObject = arrays.getJSONObject(i);
//
//            if(jsonObject.containsKey("inner")){
//
//                JSONArray inner = jsonObject.getJSONArray("inner");
//
//                ConcurrentHashMap<String,JSONArray> onBox = new ConcurrentHashMap<String, JSONArray>();
//
//                convertInner(jsonObject,inner,onBox);
//
//                System.out.println("inner="+ay.toJSONString());
//
//            }
//
//        }*/
//
//    }
//
//
//
//    public void iteratorBox(JSONObject jsonObject, ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> onBox) {
//
//        JSONArray box = jsonObject.getJSONArray("positioningBox");
//
//        int i1 = (box == null || box.isEmpty()) ? 0 : box.size();
//
//        ConcurrentHashMap<String, Object> in = new ConcurrentHashMap<String, Object>();
//
//        for (int j = 0; j < i1; j++) {
//
//            JSONObject boxItem = box.getJSONObject(j);
//
//            String boxType = boxItem.getString("nodeType");
//
//            String operatorSign = boxItem.getString("operator_Sign");
//
//            if ("placeholder".equals(boxType)) {
//
//                //innerArray.add(jsonObject);
//
//            } else if ("details".equals(boxType)) {
//
//                innerCom("details", in, boxItem, operatorSign);
//
//            } else {
//
//
//
//            }
//
//        }
//
//    }
//
//
//
//    private void innerCom(String key, ConcurrentHashMap<String, Object> in, JSONObject boxItem, String operatorSign) {
//
//        if (!in.containsKey(key)) {
//
//            JSONArray ar = new JSONArray();
//
//            ar.add(boxItem);
//
//            in.put("operatorSign", operatorSign == null ? "un" : operatorSign);
//
//            in.put(key, ar);
//
//        } else {
//
//            JSONArray array = (JSONArray) in.get(key);
//
//            array.add(boxItem);
//
//            in.put(key, array);
//
//            System.out.println(array);
//
//        }
//
//    }
//
//
//
//
//
//    public void iterator(JSONArray array) {
//
//
//
//    }
//
//
//
//
//
//    public JSONObject convertj(JSONObject jsonObject) {
//
//        String nodeType = jsonObject.getString("nodeType");
//
//        if ("placeholder".equals(nodeType)) {
//
//            return jsonObject;
//
//        } else {
//
//            JSONObject object = new JSONObject();
//
//            JSONArray ar = new JSONArray();
//
//            ar.add(jsonObject);
//
//            String operator_sign = jsonObject.getString("operator_Sign");
//
//            String s = "details".equals(nodeType) ? "details" : "inner";
//
//            object.put("operatorSign", operator_sign);
//
//            object.put(s, ar);
//
//            return object;
//
//        }
//
//    }
//
//
//
//    public void convertInner(JSONObject in, JSONArray inner, ConcurrentHashMap<String, JSONArray> onBox) {
//
//        int size = inner.size();
//
//        for (int i = 0; i < size; i++) {
//
//            JSONObject jsonObject = inner.getJSONObject(i);
//
//            JSONArray positioningBox = jsonObject.getJSONArray("positioningBox");
//
//            convertInnerBox(positioningBox, onBox);
//
//        }
//
//    }
//
//
//
//
//    public void convertInnerBox(JSONArray box, ConcurrentHashMap<String, JSONArray> onBox) {
//
//        int size = box.size();
//
//        for (int i = 0; i < size; i++) {
//
//            JSONObject jsonObject = box.getJSONObject(i);
//
//            String nodeType = jsonObject.getString("nodeType");
//
//            if (!"placeholder".equals(nodeType)) {
//
//                if ("details".equals(nodeType)) {
//
//                    JSONObject convertj = convertj(jsonObject);
//
//                    JSONArray details = convertj.getJSONArray("details");
//
//                    if (!onBox.containsKey("details")) {
//
//                        onBox.put("details", details);
//
//                    } else {
//
//                        JSONArray json = onBox.get("details");
//
//                        json.addAll(details);
//
//                    }
//
//                    System.out.println(onBox);
//
//                } else {
//
//
//
//                }
//
//            }
//
//        }
//
//    }
//}
