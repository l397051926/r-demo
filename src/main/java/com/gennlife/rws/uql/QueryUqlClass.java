package com.gennlife.rws.uql;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.uqlcondition.HighGradeUqlWhereElem;
import com.gennlife.rws.uqlcondition.LiteralUqlWhereElem;
import com.gennlife.rws.uqlcondition.SimpleConditionUqlWhereElem;
import com.gennlife.rws.uqlcondition.UqlWhere;
import com.gennlife.rws.util.DataType;
import com.gennlife.rws.util.StringUtils;

import javax.sound.midi.Soundbank;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class QueryUqlClass {

    private static final Map<String, String> relationMap;
    private static final Map<String, String> queryMap;

    static {
        relationMap = new HashMap<>();
        relationMap.put("包含", "contain");
        relationMap.put("不包含", "not contain");
        relationMap.put(">", ">");
        relationMap.put("<", "<");
        relationMap.put("=", "=");
        relationMap.put("<","早于");
        relationMap.put(">","晚于");
        relationMap.put("-","从...到...");


        queryMap = new HashMap<>();
        queryMap.put("[患者基本信息.婚姻]", "patient_info.MARITAL_STATUS");
        queryMap.put("[患者基本信息.性别]", "patient_info.GENDER");
        queryMap.put("[患者基本信息.民族]", "patient_info.ETHNIC");
        queryMap.put("就诊.检验报告.检验子项.检验子项编码", "visits.inspection_reports.sub_inspection.SUB_INSPECTION_ITEM_ID");
        queryMap.put("就诊.检验报告.检验子项.检验子项结果", "visits.inspection_reports.sub_inspection.SUB_INSPECTION_RESULT");
        queryMap.put("就诊.检验报告.检验子项.检验子项结果数值", "visits.inspection_reports.sub_inspection.SUB_INSPECTION_RESULT_NUMBER");
        queryMap.put("就诊.检验报告.检验子项.检验子项单位", "visits.inspection_reports.sub_inspection.SUB_INSPECTION_UNIT");
        queryMap.put("就诊.检验报告.检验子项.检验子项参考区间", "visits.inspection_reports.sub_inspection.SUB_INSPECTION_REFERENCE_INTERVAL");
    }

    public static void main(String[] args) {
//        String obj = "{\"condition\":\"([患者基本信息.民族] 包含 蒙古族,回族 AND [患者基本信息.性别] 包含 男) AND {[就诊.就诊基本信息.就诊次数] > 1 AND ([就诊.手术.手术名称] 不包含 肾切除 AND [就诊.病案首页.手术.手术及操作名称] 不包含 肾切除 AND [就诊.手术记录.手术名称] 不包含 肾切除 AND [就诊.手术信息.手术名称] 不包含 肾切除) AND [就诊.诊断.诊断名称] 不包含 肾癌} AND {{{[就诊.检验报告.检验子项.检验子项中文名] 不包含 安安 AND [就诊.检验报告.检验子项.检验子项结果] 不包含 烦烦烦 AND [就诊.检验报告.检验子项.检验子项结果数值] > 1 AND [就诊.检验报告.检验子项.检验子项参考区间] 不包含 发发发}} AND [就诊.诊断.诊断日期] 从 1918-04-12 00:00:00 到 2018-11-10 00:00:00}\",\"collectionList\":[{\"uuId\":\"e1201d89-3d07-4e5d-998c-564e1e89ee58\",\"collection\":[{\"uuId\":\"7699472f-3523-476f-a5d6-417a90b7eca5\",\"logic\":\"\",\"srchItem\":\"患者基本信息.民族\",\"indexFieldValue\":\"patient_info.ETHNIC\",\"dataType\":\"string\",\"dateFormat\":\"\",\"dataMap\":[\"汉族\",\"蒙古族\",\"回族\",\"藏族\",\"维吾尔族\",\"苗族\",\"彝族\",\"壮族\",\"布依族\",\"朝鲜族\",\"满族\",\"侗族\",\"瑶族\",\"白族\",\"土家族\",\"哈尼族\",\"哈萨克族\",\"傣族\",\"黎族\",\"僳僳族\",\"佤族\",\"畲族\",\"高山族\",\"拉祜族\",\"水族\",\"东乡族\",\"纳西族\",\"景颇族\",\"柯尔克孜族\",\"土族\",\"达斡尔族\",\"仫佬族\",\"羌族\",\"布朗族\",\"撒拉族\",\"毛南族\",\"仡佬族\",\"锡伯族\",\"阿昌族\",\"普米族\",\"塔吉克族\",\"怒族\",\"乌孜别克族\",\"俄罗斯族\",\"鄂温克族\",\"德昂族\",\"保安族\",\"裕固族\",\"京族\",\"塔塔尔族\",\"独龙族\",\"鄂伦春族\",\"赫哲族\",\"门巴族\",\"珞巴族\",\"基诺族\"],\"srchRelation\":\"包含\",\"srchRelationValue\":[\"蒙古族\",\"回族\"],\"hasRelatedItems\":false,\"relatedItems\":[],\"conceptPolymerization\":\"\",\"conceptPolymerizationSource\":\"\",\"recommen\":\"\",\"intelliSense\":\"\",\"uiItem\":\"患者基本信息.民族\"},{\"uuId\":\"310d8f41-c7e2-41bc-8ba5-d1209c54c6c6\",\"logic\":\"AND\",\"srchItem\":\"患者基本信息.性别\",\"indexFieldValue\":\"patient_info.GENDER\",\"dataType\":\"string\",\"dateFormat\":\"\",\"dataMap\":[\"男\",\"女\"],\"srchRelation\":\"包含\",\"srchRelationValue\":[\"男\"],\"hasRelatedItems\":false,\"relatedItems\":[],\"conceptPolymerization\":\"\",\"conceptPolymerizationSource\":\"\",\"recommen\":\"\",\"intelliSense\":\"\",\"uiItem\":\"患者基本信息.性别\"},{\"uuId\":\"4a63eb6f-f48e-48fd-8b5a-4120bfd5b474\",\"logic\":\"AND\",\"srchItem\":\"\",\"indexFieldValue\":\"\",\"dataType\":\"\",\"dateFormat\":\"\",\"dataMap\":[],\"srchRelation\":\"\",\"srchRelationValue\":\"\",\"hasRelatedItems\":false,\"relatedItems\":[],\"conceptPolymerization\":\"\",\"conceptPolymerizationSource\":\"\",\"recommen\":\"\",\"intelliSense\":\"\"}]},{\"uuId\":\"38c521e3-a43d-4730-94e7-152819860b17\",\"collection\":[{\"uuId\":\"36b55a23-aad6-41a2-b9ef-1fca018c3438\",\"logic\":\"\",\"srchItem\":\"就诊.就诊基本信息.就诊次数\",\"indexFieldValue\":\"visits.visit_info.VISIT_TIMES\",\"dataType\":\"long\",\"dateFormat\":\"\",\"srchRelation\":\">\",\"srchRelationValue\":\"1\",\"hasRelatedItems\":false,\"relatedItems\":[],\"conceptPolymerization\":\"\",\"conceptPolymerizationSource\":\"\",\"recommen\":\"\",\"intelliSense\":\"\",\"uiItem\":\"就诊.就诊基本信息.就诊次数\"},{\"uuId\":\"7bf6da45-cd6d-4572-a7cd-ecc7275a6c63\",\"logic\":\"AND\",\"srchItem\":\"就诊.手术.手术名称\",\"indexFieldValue\":\"visits.operation.OPERATION_NAME\",\"dataType\":\"string\",\"dateFormat\":\"\",\"srchRelation\":\"不包含\",\"srchRelationValue\":\"肾切除\",\"hasRelatedItems\":false,\"relatedItems\":[],\"conceptPolymerization\":\"是\",\"conceptPolymerizationSource\":\"visits.medical_record_home_page.operation.OPERATION_NAME; visits.operation_records.OPERATION; visits.operation_info.OPERATION\",\"recommen\":\"是\",\"intelliSense\":\"是\",\"uiItem\":\"就诊.手术.手术名称\"},{\"uuId\":\"07d4c86a-1ab8-45c1-87e9-f00fc1df37ea\",\"logic\":\"AND\",\"srchItem\":\"就诊.诊断.诊断名称\",\"indexFieldValue\":\"visits.diagnose.DIAGNOSIS\",\"dataType\":\"string\",\"dateFormat\":\"\",\"srchRelation\":\"不包含\",\"srchRelationValue\":\"肾癌\",\"hasRelatedItems\":false,\"relatedItems\":[],\"conceptPolymerization\":\"\",\"conceptPolymerizationSource\":\"\",\"recommen\":\"是\",\"intelliSense\":\"是\",\"uiItem\":\"就诊.诊断.诊断名称\"},{\"uuId\":\"a1d2c2d6-10de-4ac5-b028-9519f2c1f5cb\",\"logic\":\"AND\",\"srchItem\":\"\",\"indexFieldValue\":\"\",\"dataType\":\"\",\"dateFormat\":\"\",\"dataMap\":[],\"srchRelation\":\"\",\"srchRelationValue\":\"\",\"hasRelatedItems\":false,\"relatedItems\":[],\"conceptPolymerization\":\"\",\"conceptPolymerizationSource\":\"\",\"recommen\":\"\",\"intelliSense\":\"\"}]},{\"uuId\":\"7434bee1-8ad1-49ee-85a8-025e3b0cd6c4\",\"collection\":[{\"uuId\":\"c115d198-49f0-4b7c-8815-e7e1c5fcf909\",\"logic\":\"\",\"srchItem\":\"就诊.检验报告.检验子项.检验子项中文名\",\"indexFieldValue\":\"visits.inspection_reports.sub_inspection.SUB_INSPECTION_CN\",\"dataType\":\"string\",\"dateFormat\":\"\",\"srchRelation\":\"不包含\",\"srchRelationValue\":\"安安\",\"hasRelatedItems\":true,\"relatedItems\":[{\"uuId\":\"3046be36-b1c7-410b-a0d2-c1655a66ef3c\",\"logic\":\"AND\",\"srchItem\":\"就诊.检验报告.检验子项.检验子项结果\",\"indexFieldValue\":\"\",\"dataType\":\"string\",\"srchRelation\":\"不包含\",\"srchRelationValue\":\"烦烦烦\",\"hasRelatedItems\":false,\"relatedItems\":[],\"conceptPolymerization\":\"\",\"conceptPolymerizationSource\":\"\",\"recommen\":\"\",\"intelliSense\":\"\",\"uiItem\":\"检验子项结果\"},{\"uuId\":\"dcc35428-10be-47c5-8f07-add45b311668\",\"logic\":\"AND\",\"srchItem\":\"就诊.检验报告.检验子项.检验子项结果数值\",\"indexFieldValue\":\"\",\"dataType\":\"double\",\"srchRelation\":\">\",\"srchRelationValue\":\"1\",\"hasRelatedItems\":false,\"relatedItems\":[],\"conceptPolymerization\":\"\",\"conceptPolymerizationSource\":\"\",\"recommen\":\"\",\"intelliSense\":\"\",\"uiItem\":\"检验子项结果数值\"},{\"uuId\":\"d675f1ae-98b7-46b7-a6cd-15373eb5a358\",\"logic\":\"AND\",\"srchItem\":\"就诊.检验报告.检验子项.检验子项参考区间\",\"indexFieldValue\":\"\",\"dataType\":\"string\",\"srchRelation\":\"不包含\",\"srchRelationValue\":\"发发发\",\"hasRelatedItems\":false,\"relatedItems\":[],\"conceptPolymerization\":\"\",\"conceptPolymerizationSource\":\"\",\"recommen\":\"\",\"intelliSense\":\"\",\"uiItem\":\"检验子项参考区间\"}],\"conceptPolymerization\":\"\",\"conceptPolymerizationSource\":\"\",\"recommen\":\"是\",\"intelliSense\":\"是\",\"uiItem\":\"就诊.检验报告.检验子项.检验子项中文名\"},{\"uuId\":\"1e36c441-fb46-4b18-b806-2b1cc8ec822c\",\"logic\":\"AND\",\"srchItem\":\"就诊.诊断.诊断日期\",\"indexFieldValue\":\"visits.diagnose.DIAGNOSTIC_DATE\",\"dataType\":\"date\",\"dateFormat\":\"yyyy-MM-dd HH:mm:ss\",\"srchRelation\":\"从...到...\",\"srchRelationValue\":\"1918-04-12 00:00:00,2018-11-10 00:00:00\",\"hasRelatedItems\":false,\"relatedItems\":[],\"conceptPolymerization\":\"\",\"conceptPolymerizationSource\":\"\",\"recommen\":\"\",\"intelliSense\":\"\",\"uiItem\":\"就诊.诊断.诊断日期\"},{\"uuId\":\"1841b844-7aee-45ec-9be5-15778db564d4\",\"logic\":\"AND\",\"srchItem\":\"\",\"indexFieldValue\":\"\",\"dataType\":\"\",\"dateFormat\":\"\",\"dataMap\":[],\"srchRelation\":\"\",\"srchRelationValue\":\"\",\"hasRelatedItems\":false,\"relatedItems\":[],\"conceptPolymerization\":\"\",\"conceptPolymerizationSource\":\"\",\"recommen\":\"\",\"intelliSense\":\"\"}]}],\"projectId\":\"a9cf59ff2e2442bdba830dbdc483b76d\",\"patientSetId\":\"407b2473aded459281cee06d580811e1\",\"patientName\":\"患者集1\",\"createId\":\"9ee485b5-5bea-487a-b456-62eb19866348\",\"createName\":\"王梦\"}";
//        String obj = "{\"condition\":\"([患者基本信息.民族] 包含 回族,藏族) \",\"collectionList\":\"[{\\\"uuId\\\":\\\"6051c603-0338-472b-9b6c-eeb9ae8dc6d6\\\",\\\"collection\\\":[{\\\"uuId\\\":\\\"bd2b2a8d-cba0-4ace-998e-fa4f83cb20e3\\\",\\\"logic\\\":\\\"\\\",\\\"srchItem\\\":\\\"患者基本信息.民族\\\",\\\"indexFieldValue\\\":\\\"patient_info.ETHNIC\\\",\\\"dataType\\\":\\\"string\\\",\\\"dateFormat\\\":\\\"\\\",\\\"dataMap\\\":[\\\"汉族\\\",\\\"蒙古族\\\",\\\"回族\\\",\\\"藏族\\\",\\\"维吾尔族\\\",\\\"苗族\\\",\\\"彝族\\\",\\\"壮族\\\",\\\"布依族\\\",\\\"朝鲜族\\\",\\\"满族\\\",\\\"侗族\\\",\\\"瑶族\\\",\\\"白族\\\",\\\"土家族\\\",\\\"哈尼族\\\",\\\"哈萨克族\\\",\\\"傣族\\\",\\\"黎族\\\",\\\"僳僳族\\\",\\\"佤族\\\",\\\"畲族\\\",\\\"高山族\\\",\\\"拉祜族\\\",\\\"水族\\\",\\\"东乡族\\\",\\\"纳西族\\\",\\\"景颇族\\\",\\\"柯尔克孜族\\\",\\\"土族\\\",\\\"达斡尔族\\\",\\\"仫佬族\\\",\\\"羌族\\\",\\\"布朗族\\\",\\\"撒拉族\\\",\\\"毛南族\\\",\\\"仡佬族\\\",\\\"锡伯族\\\",\\\"阿昌族\\\",\\\"普米族\\\",\\\"塔吉克族\\\",\\\"怒族\\\",\\\"乌孜别克族\\\",\\\"俄罗斯族\\\",\\\"鄂温克族\\\",\\\"德昂族\\\",\\\"保安族\\\",\\\"裕固族\\\",\\\"京族\\\",\\\"塔塔尔族\\\",\\\"独龙族\\\",\\\"鄂伦春族\\\",\\\"赫哲族\\\",\\\"门巴族\\\",\\\"珞巴族\\\",\\\"基诺族\\\"],\\\"srchRelation\\\":\\\"包含\\\",\\\"srchRelationValue\\\":[\\\"回族\\\",\\\"藏族\\\"],\\\"hasRelatedItems\\\":false,\\\"relatedItems\\\":[],\\\"conceptPolymerization\\\":\\\"\\\",\\\"conceptPolymerizationSource\\\":\\\"\\\",\\\"recommen\\\":\\\"\\\",\\\"intelliSense\\\":\\\"\\\",\\\"uiItem\\\":\\\"患者基本信息.民族\\\"},{\\\"uuId\\\":\\\"67a4a583-513a-4994-87a6-bdb7366f73b0\\\",\\\"logic\\\":\\\"AND\\\",\\\"srchItem\\\":\\\"\\\",\\\"indexFieldValue\\\":\\\"\\\",\\\"dataType\\\":\\\"\\\",\\\"dateFormat\\\":\\\"\\\",\\\"dataMap\\\":[],\\\"srchRelation\\\":\\\"\\\",\\\"srchRelationValue\\\":\\\"\\\",\\\"hasRelatedItems\\\":false,\\\"relatedItems\\\":[],\\\"conceptPolymerization\\\":\\\"\\\",\\\"conceptPolymerizationSource\\\":\\\"\\\",\\\"recommen\\\":\\\"\\\",\\\"intelliSense\\\":\\\"\\\"}]},{\\\"uuId\\\":\\\"95360ddc-8346-4aed-8f22-772638493e8f\\\",\\\"collection\\\":[{\\\"uuId\\\":\\\"c2994f8d-e411-494c-b894-52ad96e1ee1b\\\",\\\"logic\\\":\\\"\\\",\\\"srchItem\\\":\\\"\\\",\\\"indexFieldValue\\\":\\\"\\\",\\\"dataType\\\":\\\"\\\",\\\"dateFormat\\\":\\\"\\\",\\\"dataMap\\\":[],\\\"srchRelation\\\":\\\"\\\",\\\"srchRelationValue\\\":\\\"\\\",\\\"hasRelatedItems\\\":false,\\\"relatedItems\\\":[],\\\"conceptPolymerization\\\":\\\"\\\",\\\"conceptPolymerizationSource\\\":\\\"\\\",\\\"recommen\\\":\\\"\\\",\\\"intelliSense\\\":\\\"\\\"}]}]\",\"projectId\":\"8a6f4177ae884ff5983c2550962db719\",\"patientSetId\":\"f04a724116ef4836989536a771d8a7dc\",\"patientName\":\"患者集2\",\"createId\":\"9ee485b5-5bea-487a-b456-62eb19866348\",\"createName\":\"王梦\"}\n";
//        String obj = "{\"condition\":\"(男) AND [患者基本信息.民族] 包含 苗族\",\"collectionList\":[],\"projectId\":\"b7bf5f3885e94c288a55aaf0a51a11cb\",\"patientSetId\":\"\",\"patientName\":\"\",\"createId\":\"4adda88e-823b-468c-849f-73198866eb9a\",\"createName\":\"陈\"}\n";
//        String obj = "{\"condition\":\"(男) AND [患者基本信息.民族] 包含 回族,苗族\",\"collectionList\":[],\"projectId\":\"b7bf5f3885e94c288a55aaf0a51a11cb\",\"patientSetId\":\"3c7d6fcfc89b4b5695b645ea43a2d8ba\",\"patientName\":\"dd\",\"createId\":\"4adda88e-823b-468c-849f-73198866eb9a\",\"createName\":\"陈\"}\n";
//        String obj = "{\"condition\":\"(([患者基本信息.民族] 包含 回族) ) AND [患者基本信息.性别] 包含 女\",\"collectionList\":\"[{\\\"uuId\\\":\\\"857b7372-1f90-4420-9aed-616c9e41406d\\\",\\\"collection\\\":[{\\\"uuId\\\":\\\"1ae5a9aa-7677-461a-b310-c25f9a149416\\\",\\\"logic\\\":\\\"\\\",\\\"srchItem\\\":\\\"患者基本信息.民族\\\",\\\"indexFieldValue\\\":\\\"patient_info.ETHNIC\\\",\\\"dataType\\\":\\\"string\\\",\\\"dateFormat\\\":\\\"\\\",\\\"dataMap\\\":[\\\"汉族\\\",\\\"蒙古族\\\",\\\"回族\\\",\\\"藏族\\\",\\\"维吾尔族\\\",\\\"苗族\\\",\\\"彝族\\\",\\\"壮族\\\",\\\"布依族\\\",\\\"朝鲜族\\\",\\\"满族\\\",\\\"侗族\\\",\\\"瑶族\\\",\\\"白族\\\",\\\"土家族\\\",\\\"哈尼族\\\",\\\"哈萨克族\\\",\\\"傣族\\\",\\\"黎族\\\",\\\"僳僳族\\\",\\\"佤族\\\",\\\"畲族\\\",\\\"高山族\\\",\\\"拉祜族\\\",\\\"水族\\\",\\\"东乡族\\\",\\\"纳西族\\\",\\\"景颇族\\\",\\\"柯尔克孜族\\\",\\\"土族\\\",\\\"达斡尔族\\\",\\\"仫佬族\\\",\\\"羌族\\\",\\\"布朗族\\\",\\\"撒拉族\\\",\\\"毛南族\\\",\\\"仡佬族\\\",\\\"锡伯族\\\",\\\"阿昌族\\\",\\\"普米族\\\",\\\"塔吉克族\\\",\\\"怒族\\\",\\\"乌孜别克族\\\",\\\"俄罗斯族\\\",\\\"鄂温克族\\\",\\\"德昂族\\\",\\\"保安族\\\",\\\"裕固族\\\",\\\"京族\\\",\\\"塔塔尔族\\\",\\\"独龙族\\\",\\\"鄂伦春族\\\",\\\"赫哲族\\\",\\\"门巴族\\\",\\\"珞巴族\\\",\\\"基诺族\\\"],\\\"srchRelation\\\":\\\"包含\\\",\\\"srchRelationValue\\\":[\\\"回族\\\"],\\\"hasRelatedItems\\\":false,\\\"relatedItems\\\":[],\\\"conceptPolymerization\\\":\\\"\\\",\\\"conceptPolymerizationSource\\\":\\\"\\\",\\\"recommen\\\":\\\"\\\",\\\"intelliSense\\\":\\\"\\\",\\\"uiItem\\\":\\\"患者基本信息.民族\\\"},{\\\"uuId\\\":\\\"36a7d693-ce3b-443e-923c-ab32b906410f\\\",\\\"logic\\\":\\\"AND\\\",\\\"srchItem\\\":\\\"\\\",\\\"indexFieldValue\\\":\\\"\\\",\\\"dataType\\\":\\\"\\\",\\\"dateFormat\\\":\\\"\\\",\\\"dataMap\\\":[],\\\"srchRelation\\\":\\\"\\\",\\\"srchRelationValue\\\":\\\"\\\",\\\"hasRelatedItems\\\":false,\\\"relatedItems\\\":[],\\\"conceptPolymerization\\\":\\\"\\\",\\\"conceptPolymerizationSource\\\":\\\"\\\",\\\"recommen\\\":\\\"\\\",\\\"intelliSense\\\":\\\"\\\",\\\"nestedFlag\\\":\\\"\\\"}]},{\\\"uuId\\\":\\\"95b0f456-c397-4747-8d89-131ac8979a66\\\",\\\"collection\\\":[{\\\"uuId\\\":\\\"564c121b-1029-48db-8f79-f8c444908903\\\",\\\"logic\\\":\\\"\\\",\\\"srchItem\\\":\\\"\\\",\\\"indexFieldValue\\\":\\\"\\\",\\\"dataType\\\":\\\"\\\",\\\"dateFormat\\\":\\\"\\\",\\\"dataMap\\\":[],\\\"srchRelation\\\":\\\"\\\",\\\"srchRelationValue\\\":\\\"\\\",\\\"hasRelatedItems\\\":false,\\\"relatedItems\\\":[],\\\"conceptPolymerization\\\":\\\"\\\",\\\"conceptPolymerizationSource\\\":\\\"\\\",\\\"recommen\\\":\\\"\\\",\\\"intelliSense\\\":\\\"\\\",\\\"nestedFlag\\\":\\\"\\\"}]}]\",\"projectId\":\"b7bf5f3885e94c288a55aaf0a51a11cb\",\"patientSetId\":\"1b9a3c21d7cf419bb069617b434fb21c\",\"patientName\":\"患者集1\",\"createId\":\"4adda88e-823b-468c-849f-73198866eb9a\",\"createName\":\"陈\"}\n";
        String obj = "{\"condition\":\" {[就诊.诊断.诊断日期] 从 2015-10-01 00:00:00 到 2015-10-01 8:30:00}\",\"collectionList\":\"[{\\\"uuId\\\":\\\"318ff0cf-136c-4a24-83b0-ba78cf9afdeb\\\",\\\"collection\\\":[{\\\"uuId\\\":\\\"ae858eea-091f-40c4-85c0-9bdd1a562f9f\\\",\\\"logic\\\":\\\"\\\",\\\"srchItem\\\":\\\"\\\",\\\"indexFieldValue\\\":\\\"\\\",\\\"dataType\\\":\\\"\\\",\\\"dateFormat\\\":\\\"\\\",\\\"dataMap\\\":[],\\\"srchRelation\\\":\\\"\\\",\\\"srchRelationValue\\\":\\\"\\\",\\\"hasRelatedItems\\\":false,\\\"relatedItems\\\":[],\\\"conceptPolymerization\\\":\\\"\\\",\\\"conceptPolymerizationSource\\\":\\\"\\\",\\\"recommen\\\":\\\"\\\",\\\"intelliSense\\\":\\\"\\\"}]},{\\\"uuId\\\":\\\"6d06fa1b-15dd-47cd-956a-86c8dde1d085\\\",\\\"collection\\\":[{\\\"uuId\\\":\\\"b6b62434-4d85-4404-9c50-70e0efe1a37e\\\",\\\"logic\\\":\\\"\\\",\\\"srchItem\\\":\\\"\\\",\\\"indexFieldValue\\\":\\\"\\\",\\\"dataType\\\":\\\"\\\",\\\"dateFormat\\\":\\\"\\\",\\\"dataMap\\\":[],\\\"srchRelation\\\":\\\"\\\",\\\"srchRelationValue\\\":\\\"\\\",\\\"hasRelatedItems\\\":false,\\\"relatedItems\\\":[],\\\"conceptPolymerization\\\":\\\"\\\",\\\"conceptPolymerizationSource\\\":\\\"\\\",\\\"recommen\\\":\\\"\\\",\\\"intelliSense\\\":\\\"\\\"}]}]\",\"projectId\":\"ffdd10a5bd99430db9a5aecd7532e251\",\"patientSetId\":\"111a49d9ef4647649698b7cb3bb36df2\",\"patientName\":\"zxcvb\",\"createId\":\"939f2a58-58d2-40bb-95e9-81111d730d85\",\"createName\":\"wangkaiyu\"}\n";
//        String obj = "";
        String sql = getSqlWhere(obj);
        System.out.println(sql);
        List<String> aa = new ArrayList<>();
        aa.add("aa");
        aa.add("bb");
        aa.remove(aa.size()-1);
        aa.forEach(x -> System.out.println(x));
    }


    public static String getSqlWhere(String obj) {
        UqlWhere where = new UqlWhere();
        JSONObject queryObj = JSONObject.parseObject(obj);
        JSONArray collectionList = queryObj.getJSONArray("collectionList");
        String condition = queryObj.getString("condition");
        int collectionSize = collectionList.size();
        for (int i = 0; i < collectionSize; i++) {
            JSONObject collectionObj = collectionList.getJSONObject(i);
            JSONArray collections = collectionObj.getJSONArray("collection");
            assembleCollections(collections, where);
            if(i==collectionSize-1){
                where.removeElem();
            }
        }
        if(StringUtils.isNotEmpty(condition) && collectionSize ==0){
            String[] conditionArr = condition.split(" AND ");
            for (int i = 0; i < conditionArr.length; i++) {
                if(i==0){
                    String value = conditionArr[i].substring(1,conditionArr[i].length()-1);
                    where.addElem(new LiteralUqlWhereElem("query('"+value+"')"));
                }else {
                    getPatient(conditionArr[i],where);
                    if(i<conditionArr.length-1){
                        where.addElem(new LiteralUqlWhereElem("AND"));
                    }
                }
            }
        }
        where.execute();
        return where.toString();
    }

    public static void getPatient(String conditions,UqlWhere where){
            String[] condArr = conditions.split(" ");
            String cod = queryMap.get(condArr[0]);
            String sign = " IN ";
            String val = condArr[2];
            String[] valArr = val.split(",");
            StringBuffer buffer = new StringBuffer();
            buffer.append("(");
            for (int j = 0; j < valArr.length ; j++) {
                buffer.append("'"+valArr[j]+"'");
                if(j<valArr.length-1){
                    buffer.append(",");
                }
            }
            buffer.append(")");
            String value =cod + sign + buffer.toString();
            where.addElem(new HighGradeUqlWhereElem(value));


    }

    private static void assembleCollections(JSONArray collections, UqlWhere where) {
        int size = collections.size();
        for (int i = 0; i < size; i++) {
            JSONObject collectionObj = collections.getJSONObject(i);
            assembleCollection(collectionObj, where);
            if (i == size - 1) {
                String logic = collectionObj.getString("logic");
                if(StringUtils.isNotEmpty(logic)) {
                    where.addElem(new LiteralUqlWhereElem(logic));
                }
            }
        }
    }

    private static void assembleCollection(JSONObject collection, UqlWhere where) {
        String value = null;
        String logic = collection.getString("logic");
        String indexFieldValue = collection.getString("indexFieldValue");
        String srchRelation = collection.getString("srchRelation");
        String srchItem = collection.getString("srchItem");
        JSONArray relatedItems = collection.getJSONArray("relatedItems");
        String srchRelationValue = collection.getString("srchRelationValue");
        String conceptPolymerization = collection.getString("conceptPolymerization");
        String conceptPolymerizationSource = collection.getString("conceptPolymerizationSource");
        String dataType = collection.getString("dataType");
        boolean hasRelastedItem = collection.getBoolean("hasRelatedItems");
        if ( StringUtils.isEmpty(srchRelation)) {
            return;
        }
        if(StringUtils.isEmpty(indexFieldValue) ){
            value = queryMap.get(srchItem);
        }else {
            value = indexFieldValue;
        }
        if (StringUtils.isNotEmpty(logic)) {//是否加 条件
            where.addElem(new LiteralUqlWhereElem(logic));
        }
        if (hasRelastedItem) {//是否有强相关
            setBufferValue(where, value, srchRelationValue, srchRelation, dataType);
            assembleRelatedItem(where, relatedItems);
        } else {
            setBufferValue(where, value, srchRelationValue, srchRelation, dataType);

        }
    }

    private static void assembleRelatedItem(UqlWhere where, JSONArray relatedItems) {
        int size = relatedItems.size();
        for (int i = 0; i < size; i++) {
            JSONObject releatem = relatedItems.getJSONObject(i);
            assembleCollection(releatem, where);
        }
    }

    private static void setBufferValue(UqlWhere where, String indexFieldValue, String srchRelationValue, String srchRelation, String dataType) {
        String[] values = indexFieldValue.split("\\.");
        String value = null;
        if(values.length>1){
            value = values[values.length-2] +"."+values[values.length-1];
        }else {
            value = indexFieldValue;
        }
        String valueto = setBufferSrchRelation(srchRelation, where, srchRelationValue, dataType);
        where.addElem(new HighGradeUqlWhereElem(value + valueto));
    }

    private static String setBufferSrchRelation(String srchRelation, UqlWhere where, String srchRelationValue, String dataType) {
        StringBuffer resultBuffer = new StringBuffer();
        if (srchRelationValue.startsWith("[")) {
            resultBuffer.append(" in ");
            JSONArray srchArray = JSONArray.parseArray(srchRelationValue);
            int size = srchArray == null ? 0 : srchArray.size();
            resultBuffer.append(" ( ");
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    resultBuffer.append(",");
                }
                String value = srchArray.getString(i);
                resultBuffer.append(" '" + value + "' ");
            }
            resultBuffer.append(" ) ");
        } else if ("long".equals(dataType)) {
            resultBuffer.append(" ");
            resultBuffer.append(" "+relationMap.get(srchRelation));
            resultBuffer.append(" ");
            resultBuffer.append(Long.valueOf(srchRelationValue));
        } else if ("date".equals(dataType) && "从...到...".equals(srchRelation)) {
            String date[] = srchRelationValue.split(",");
            if(date.length>1){
                resultBuffer.append(" between ");
                resultBuffer.append("'"+date[0]+"'");
                resultBuffer.append(" and ");
                resultBuffer.append("'"+date[1]+"'");
            }
        } else {
            resultBuffer.append(" "+relationMap.get(srchRelation));
            resultBuffer.append(" '" + srchRelationValue + "' ");
        }
        return resultBuffer.toString();
    }
//
//
//    public static String getSqlWhere(String object) {
//        JSONObject queryObj = JSONObject.parseObject(object);
//        JSONArray collectionList = queryObj.getJSONArray("collectionList");
//        String query = queryObj.getString("condition");
//        StringBuffer resulBuffer = new StringBuffer();
//        if (collectionList == null || collectionList.size() == 0) {
//            if (query.contains("AND")) {
//                String[] queryArray = query.split("AND");
//                for (int i = 0; i < queryArray.length; i++) {
//                    String queryDimSearch = getQueryDimSearch(queryArray[0]);
//                    if (i == 0) {
//                        queryDimSearch=queryDimSearch.replaceAll("\\(","('");
//                        queryDimSearch=queryDimSearch.replaceAll("\\)","')");
//
//                        resulBuffer.append(queryDimSearch);
//                        continue;
//                    }
//                    resulBuffer.append(" AND ");
//                    String[] conditionArray = queryArray[i].split(" ");
//                    processingConditionArray(conditionArray, resulBuffer);
//                }
//
//                return resulBuffer.toString();
//            } else if (query.contains("([")) {
//                query = query.substring(1, query.length() - 1);
//                String[] conditionArray = query.split(" ");
//                processingConditionArray(conditionArray, resulBuffer);
//                return resulBuffer.toString();
//            } else {
//                return getQueryDimSearch(query);
//            }
//        }
//
//        return getQueryAdvaSearch(collectionList);
//
//
//    }
//
//    private static void processingConditionArray(String[] conditionArray, StringBuffer resulBuffer) {
//        for (int j = 0; j < conditionArray.length; j++) {
//            String condition = conditionArray[j].replaceAll("\\)","");
//            if (StringUtils.isEmpty(condition)) continue;
//            if (relationMap.containsKey(condition)) {
//                if (conditionArray[j + 1].split(",").length > 0) {
//                    resulBuffer.append(" in ");
//                } else {
//                    resulBuffer.append(" " + relationMap.get(condition) + " ");
//                }
//                continue;
//            }
//            if (queryMap.containsKey(condition)) {
//                String conditionEnglish = queryMap.get(condition);
//                if(conditionEnglish.contains("patient_info")){
//                    resulBuffer.append("  hasparent( ");
//                }
//                resulBuffer.append(" " + conditionEnglish + " ");
//                continue;
//            }
//            String[] tmpArray = condition.split(",");
//            if (tmpArray.length > 0) {
//                resulBuffer.append(" ( ");
//                for (int k = 0; k < tmpArray.length; k++) {
//                    resulBuffer.append("'");
//                    resulBuffer.append(tmpArray[k]);
//                    resulBuffer.append("'");
//                    if (k < tmpArray.length - 1) {
//                        resulBuffer.append(", ");
//                    }
//                }
//                resulBuffer.append(" ) ");
//            } else {
//                resulBuffer.append("'");
//                resulBuffer.append(condition);
//                resulBuffer.append("'");
//            }
//        }
//        if(resulBuffer.toString().contains("hasparent")){
//            resulBuffer.append(") ");
//        }
//    }
//
//
//    private static String getQueryAdvaSearch(JSONArray collectionList) {
//        StringBuffer resultBuffer = new StringBuffer();
//        int size = collectionList == null ? 0 : collectionList.size();
//        for (int i = 0; i < size; i++) {
//            JSONObject collection = collectionList.getJSONObject(i);
//            if (i > 0) {
//                resultBuffer.append(" and ");
//            }
//            disposeCollection(collection.getJSONArray("collection"), resultBuffer);
//        }
//        return resultBuffer.toString();
//    }
//
//    private static void disposeCollection(JSONArray collections, StringBuffer resultBuffer) {
//        int size = collections == null ? 0 : collections.size();
//        for (int i = 0; i < size; i++) {
//            JSONObject collection = collections.getJSONObject(i);
//            String logic = collection.getString("logic");
//            String indexFieldValue = collection.getString("indexFieldValue");
//            String srchRelation = collection.getString("srchRelation");
//            JSONArray relatedItems = collection.getJSONArray("relatedItems");
//            String srchRelationValue = collection.getString("srchRelationValue");
//            String conceptPolymerization = collection.getString("conceptPolymerization");
//            String conceptPolymerizationSource = collection.getString("conceptPolymerizationSource");
//            String dataType = collection.getString("dataType");
//            if (StringUtils.isEmpty(indexFieldValue) || StringUtils.isEmpty(srchRelation)) {
//                continue;
//            }
//            if (StringUtils.isNotEmpty(logic)) {//是否加 条件
//                resultBuffer.append(" " + logic + " ");
//            }
//            if (StringUtils.isEmpty(conceptPolymerization)) {//是否有强相关
//                setBufferValue(resultBuffer, indexFieldValue, srchRelationValue, srchRelation, dataType);
//            } else {
//                String[] conceptArray = conceptPolymerizationSource.split(";");
//                setBufferValue(resultBuffer, indexFieldValue, srchRelationValue, srchRelation, dataType);
//                for (int j = 0; j < conceptArray.length; j++) {
//                    resultBuffer.append(" or ");
//                    setBufferValue(resultBuffer, conceptArray[j], srchRelationValue, srchRelation, dataType);
//                }
//            }
//
//        }
//    }
//
//    private static void setBufferValue(StringBuffer resultBuffer, String indexFieldValue, String srchRelationValue, String srchRelation, String dataType) {
//       if(indexFieldValue.contains("patient_info")){
//           resultBuffer.append("  hasparent( ");
//       }
//        resultBuffer.append(indexFieldValue.replaceAll("visits.",""));
//        resultBuffer.append(" ");
//        setBufferSrchRelation(srchRelation, resultBuffer, srchRelationValue, dataType);
//        if(indexFieldValue.contains("patient_info")){
//            resultBuffer.append(" ) ");
//        }
//    }
//
//    private static void setBufferSrchRelation(String srchRelation, StringBuffer resultBuffer, String srchRelationValue, String dataType) {
//
//        if (srchRelationValue.startsWith("[")) {
//            resultBuffer.append(" in ");
//            JSONArray srchArray = JSONArray.parseArray(srchRelationValue);
//            int size = srchArray == null ? 0 : srchArray.size();
//            resultBuffer.append(" ( ");
//            for (int i = 0; i < size; i++) {
//                if (i > 0) {
//                    resultBuffer.append(",");
//                }
//                String value = srchArray.getString(i);
//                resultBuffer.append("'" + value + "'");
//            }
//            resultBuffer.append(" ) ");
//        } else if ("long".equals(dataType)) {
//            resultBuffer.append(relationMap.get(srchRelation));
//            resultBuffer.append( Long.valueOf(srchRelationValue));
//        } else {
//            resultBuffer.append(relationMap.get(srchRelation));
//            resultBuffer.append("'" + srchRelationValue + "'");
//        }
//
//    }
//
//    public static String getQueryDimSearch(String query) {
//        if (query.contains("(") && query.contains(")")) return "query" + query;
//        return "query(" + query + ")";
//    }

}
