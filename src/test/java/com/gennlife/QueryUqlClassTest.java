//package com.gennlife;
//
//import com.gennlife.rws.uql.QueryUqlClass;
//
///**
// * @author liumingxin
// * @create 2018 22 16:17
// * @desc
// **/
//public class QueryUqlClassTest {
//   static   String test1 = "{\n" +
//            "    \"hospitalID\": \"public\",\n" +
//            "    \"patientSn\": \"pat_63b21255d2715ba1d58cae80bb6e4ff2\",\n" +
//            "    \"query\": \"([患者基本信息.性别] 包含 男) AND {([就诊.手术.手术名称] 包含 肝 OR [就诊.病案首页.手术.手术及操作名称] 包含 肝 OR [就诊.手术记录.手术名称] 包含 肝 OR [就诊.手术信息.手术名称] 包含 肝) OR [就诊.就诊基本信息.就诊次数] > 1}\",\n" +
//            "    \"isHighlightAllfields\": false,\n" +
//            "    \"highlightfields\": [\n" +
//            "        \"patient_info.GENDER\",\n" +
//            "        \"visits.operation.OPERATION_NAME\",\n" +
//            "        \"visits.visit_info.VISIT_TIMES\"\n" +
//            "    ],\n" +
//            "    \"collectionList\": [\n" +
//            "        {\n" +
//            "            \"uuId\": \"c181163f-94aa-446e-8666-646842deeb92\",\n" +
//            "            \"collection\": [\n" +
//            "                {\n" +
//            "                    \"uuId\": \"a8496547-65a1-4969-92cf-b7eee9669c14\",\n" +
//            "                    \"logic\": \"\",\n" +
//            "                    \"srchItem\": \"患者基本信息.性别\",\n" +
//            "                    \"indexFieldValue\": \"patient_info.GENDER\",\n" +
//            "                    \"dataType\": \"string\",\n" +
//            "                    \"dateFormat\": \"\",\n" +
//            "                    \"dataMap\": [\n" +
//            "                        \"男\",\n" +
//            "                        \"女\"\n" +
//            "                    ],\n" +
//            "                    \"srchRelation\": \"包含\",\n" +
//            "                    \"srchRelationValue\": [\n" +
//            "                        \"男\"\n" +
//            "                    ],\n" +
//            "                    \"hasRelatedItems\": false,\n" +
//            "                    \"relatedItems\": [],\n" +
//            "                    \"conceptPolymerization\": \"\",\n" +
//            "                    \"conceptPolymerizationSource\": \"\",\n" +
//            "                    \"recommen\": \"\",\n" +
//            "                    \"intelliSense\": \"\",\n" +
//            "                    \"uiItem\": \"患者基本信息.性别\"\n" +
//            "                },\n" +
//            "                {\n" +
//            "                    \"uuId\": \"ec7133d8-9f03-4513-a0da-5cb1a974671e\",\n" +
//            "                    \"logic\": \"AND\",\n" +
//            "                    \"srchItem\": \"\",\n" +
//            "                    \"indexFieldValue\": \"\",\n" +
//            "                    \"dataType\": \"\",\n" +
//            "                    \"dateFormat\": \"\",\n" +
//            "                    \"dataMap\": [],\n" +
//            "                    \"srchRelation\": \"\",\n" +
//            "                    \"srchRelationValue\": \"\",\n" +
//            "                    \"hasRelatedItems\": false,\n" +
//            "                    \"relatedItems\": [],\n" +
//            "                    \"conceptPolymerization\": \"\",\n" +
//            "                    \"conceptPolymerizationSource\": \"\",\n" +
//            "                    \"recommen\": \"\",\n" +
//            "                    \"intelliSense\": \"\"\n" +
//            "                }\n" +
//            "            ]\n" +
//            "        },\n" +
//            "        {\n" +
//            "            \"uuId\": \"464b87c7-79e2-4df4-9dc0-1763c56589fd\",\n" +
//            "            \"collection\": [\n" +
//            "                {\n" +
//            "                    \"uuId\": \"32b3fe06-9fed-4304-9af4-25a822a7b433\",\n" +
//            "                    \"logic\": \"\",\n" +
//            "                    \"srchItem\": \"就诊.手术.手术名称\",\n" +
//            "                    \"indexFieldValue\": \"visits.operation.OPERATION_NAME\",\n" +
//            "                    \"dataType\": \"string\",\n" +
//            "                    \"dateFormat\": \"\",\n" +
//            "                    \"srchRelation\": \"包含\",\n" +
//            "                    \"srchRelationValue\": \"肝\",\n" +
//            "                    \"hasRelatedItems\": false,\n" +
//            "                    \"relatedItems\": [],\n" +
//            "                    \"conceptPolymerization\": \"是\",\n" +
//            "                    \"conceptPolymerizationSource\": \"visits.medical_record_home_page.operation.OPERATION_NAME; visits.operation_records.OPERATION; visits.operation_info.OPERATION\",\n" +
//            "                    \"recommen\": \"是\",\n" +
//            "                    \"intelliSense\": \"是\",\n" +
//            "                    \"uiItem\": \"就诊.手术.手术名称\"\n" +
//            "                },\n" +
//            "                {\n" +
//            "                    \"uuId\": \"67a4607f-30ef-4362-a508-0de625a57613\",\n" +
//            "                    \"logic\": \"OR\",\n" +
//            "                    \"srchItem\": \"就诊.就诊基本信息.就诊次数\",\n" +
//            "                    \"indexFieldValue\": \"visits.visit_info.VISIT_TIMES\",\n" +
//            "                    \"dataType\": \"long\",\n" +
//            "                    \"dateFormat\": \"\",\n" +
//            "                    \"srchRelation\": \">\",\n" +
//            "                    \"srchRelationValue\": \"1\",\n" +
//            "                    \"hasRelatedItems\": false,\n" +
//            "                    \"relatedItems\": [],\n" +
//            "                    \"conceptPolymerization\": \"\",\n" +
//            "                    \"conceptPolymerizationSource\": \"\",\n" +
//            "                    \"recommen\": \"\",\n" +
//            "                    \"intelliSense\": \"\",\n" +
//            "                    \"uiItem\": \"就诊.就诊基本信息.就诊次数\"\n" +
//            "                },\n" +
//            "                {\n" +
//            "                    \"uuId\": \"9f935d72-a767-48f8-878b-9cd3f1bcbf39\",\n" +
//            "                    \"logic\": \"AND\",\n" +
//            "                    \"srchItem\": \"\",\n" +
//            "                    \"indexFieldValue\": \"\",\n" +
//            "                    \"dataType\": \"\",\n" +
//            "                    \"dateFormat\": \"\",\n" +
//            "                    \"dataMap\": [],\n" +
//            "                    \"srchRelation\": \"\",\n" +
//            "                    \"srchRelationValue\": \"\",\n" +
//            "                    \"hasRelatedItems\": false,\n" +
//            "                    \"relatedItems\": [],\n" +
//            "                    \"conceptPolymerization\": \"\",\n" +
//            "                    \"conceptPolymerizationSource\": \"\",\n" +
//            "                    \"recommen\": \"\",\n" +
//            "                    \"intelliSense\": \"\"\n" +
//            "                }\n" +
//            "            ]\n" +
//            "        }\n" +
//            "    ],\n" +
//            "    \"sort\": [\n" +
//            "        {\n" +
//            "            \"field\": \"visits.visit_info.ADMISSION_DATE\",\n" +
//            "            \"order\": \"desc\"\n" +
//            "        }\n" +
//            "    ]\n" +
//            "}";
//   static String test2="{\"hospitalID\":\"public\",\"patientSn\":\"pat_c7296a3e0996a75212b270696f526a47\",\"query\":\"(男) AND [患者基本信息.婚姻] 包含 已婚\",\"isHighlightAllfields\":true,\"highlightfields\":[],\"collectionList\":[],\"sort\":[{\"field\":\"visits.visit_info.ADMISSION_DATE\",\"order\":\"desc\"}]}";
//   static String test3 = "{\"hospitalID\":\"public\",\"patientSn\":\"pat_e9cb14d9ff0a31213afaa3c935aa1e67\",\"query\":\"(男) AND [患者基本信息.性别] 包含 男 AND [患者基本信息.婚姻] 包含 已婚 AND [患者基本信息.民族] 包含 汉族\",\"isHighlightAllfields\":true,\"highlightfields\":[],\"collectionList\":[],\"sort\":[{\"field\":\"visits.visit_info.ADMISSION_DATE\",\"order\":\"desc\"}]}\n";
//   static String test4 = "{\"hospitalID\":\"public\",\"patientSn\":\"pat_e9cb14d9ff0a31213afaa3c935aa1e67\",\"query\":\"(男) AND [患者基本信息.性别] 包含 男 AND [患者基本信息.婚姻] 包含 已婚 AND [患者基本信息.民族] 包含 汉族,朝鲜族\",\"isHighlightAllfields\":true,\"highlightfields\":[],\"collectionList\":[],\"sort\":[{\"field\":\"visits.visit_info.ADMISSION_DATE\",\"order\":\"desc\"}]}\n";
//   static String test5 = "{\"hospitalID\":\"public\",\"patientSn\":\"pat_e9cb14d9ff0a31213afaa3c935aa1e67\",\"query\":\"(男) AND [患者基本信息.性别] 包含 男 AND [患者基本信息.婚姻] 包含 已婚,丧偶 AND [患者基本信息.民族] 包含 汉族,朝鲜族\",\"isHighlightAllfields\":true,\"highlightfields\":[],\"collectionList\":[],\"sort\":[{\"field\":\"visits.visit_info.ADMISSION_DATE\",\"order\":\"desc\"}]}\n";
//
//   static String test6 = "{\n" +
//           "        \"hospitalID\": \"public\",\n" +
//           "        \"patientSn\": \"pat_6d7a7e12ba0d0c61da54aa78a0035776\",\n" +
//           "        \"query\": \"([患者基本信息.民族] 包含 汉族,蒙古族,回族,藏族,维吾尔族,苗族)\",\n" +
//           "        \"isHighlightAllfields\": true,\n" +
//           "        \"highlightfields\": [],\n" +
//           "        \"collectionList\": [],\n" +
//           "        \"sort\": [\n" +
//           "            {\n" +
//           "                \"field\": \"visits.visit_info.ADMISSION_DATE\",\n" +
//           "                \"order\": \"desc\"\n" +
//           "            }\n" +
//           "        ]\n" +
//           "    }";
//   static String test7 = "{\n" +
//           "        \"hospitalID\": \"public\",\n" +
//           "        \"patientSn\": \"pat_60bd399cb7b54b99ef32932aae4aec77\",\n" +
//           "        \"query\": \"([患者基本信息.民族] 包含 回族,藏族,维吾尔族) AND {[就诊.就诊基本信息.就诊年龄（岁）] > 0}\",\n" +
//           "        \"isHighlightAllfields\": false,\n" +
//           "        \"highlightfields\": [\n" +
//           "            \"patient_info.ETHNIC\",\n" +
//           "            \"visits.visit_info.AGE\"\n" +
//           "        ],\n" +
//           "        \"collectionList\": [\n" +
//           "            {\n" +
//           "                \"uuId\": \"ba806724-5f75-4a3a-8040-0291865f19b5\",\n" +
//           "                \"collection\": [\n" +
//           "                    {\n" +
//           "                        \"uuId\": \"f8886d9a-1540-4db5-a1e8-d72c1cfff635\",\n" +
//           "                        \"logic\": \"\",\n" +
//           "                        \"srchItem\": \"患者基本信息.民族\",\n" +
//           "                        \"indexFieldValue\": \"patient_info.ETHNIC\",\n" +
//           "                        \"dataType\": \"string\",\n" +
//           "                        \"dateFormat\": \"\",\n" +
//           "                        \"dataMap\": [\n" +
//           "                            \"汉族\",\n" +
//           "                            \"蒙古族\",\n" +
//           "                            \"回族\",\n" +
//           "                            \"藏族\",\n" +
//           "                            \"维吾尔族\",\n" +
//           "                            \"苗族\",\n" +
//           "                            \"彝族\",\n" +
//           "                            \"壮族\",\n" +
//           "                            \"布依族\",\n" +
//           "                            \"朝鲜族\",\n" +
//           "                            \"满族\",\n" +
//           "                            \"侗族\",\n" +
//           "                            \"瑶族\",\n" +
//           "                            \"白族\",\n" +
//           "                            \"土家族\",\n" +
//           "                            \"哈尼族\",\n" +
//           "                            \"哈萨克族\",\n" +
//           "                            \"傣族\",\n" +
//           "                            \"黎族\",\n" +
//           "                            \"僳僳族\",\n" +
//           "                            \"佤族\",\n" +
//           "                            \"畲族\",\n" +
//           "                            \"高山族\",\n" +
//           "                            \"拉祜族\",\n" +
//           "                            \"水族\",\n" +
//           "                            \"东乡族\",\n" +
//           "                            \"纳西族\",\n" +
//           "                            \"景颇族\",\n" +
//           "                            \"柯尔克孜族\",\n" +
//           "                            \"土族\",\n" +
//           "                            \"达斡尔族\",\n" +
//           "                            \"仫佬族\",\n" +
//           "                            \"羌族\",\n" +
//           "                            \"布朗族\",\n" +
//           "                            \"撒拉族\",\n" +
//           "                            \"毛南族\",\n" +
//           "                            \"仡佬族\",\n" +
//           "                            \"锡伯族\",\n" +
//           "                            \"阿昌族\",\n" +
//           "                            \"普米族\",\n" +
//           "                            \"塔吉克族\",\n" +
//           "                            \"怒族\",\n" +
//           "                            \"乌孜别克族\",\n" +
//           "                            \"俄罗斯族\",\n" +
//           "                            \"鄂温克族\",\n" +
//           "                            \"德昂族\",\n" +
//           "                            \"保安族\",\n" +
//           "                            \"裕固族\",\n" +
//           "                            \"京族\",\n" +
//           "                            \"塔塔尔族\",\n" +
//           "                            \"独龙族\",\n" +
//           "                            \"鄂伦春族\",\n" +
//           "                            \"赫哲族\",\n" +
//           "                            \"门巴族\",\n" +
//           "                            \"珞巴族\",\n" +
//           "                            \"基诺族\"\n" +
//           "                        ],\n" +
//           "                        \"srchRelation\": \"包含\",\n" +
//           "                        \"srchRelationValue\": [\n" +
//           "                            \"回族\",\n" +
//           "                            \"藏族\",\n" +
//           "                            \"维吾尔族\"\n" +
//           "                        ],\n" +
//           "                        \"hasRelatedItems\": false,\n" +
//           "                        \"relatedItems\": [],\n" +
//           "                        \"conceptPolymerization\": \"\",\n" +
//           "                        \"conceptPolymerizationSource\": \"\",\n" +
//           "                        \"recommen\": \"\",\n" +
//           "                        \"intelliSense\": \"\",\n" +
//           "                        \"uiItem\": \"患者基本信息.民族\"\n" +
//           "                    },\n" +
//           "                    {\n" +
//           "                        \"uuId\": \"71d4c1af-a53a-499d-ba91-a56bc0c9c7c8\",\n" +
//           "                        \"logic\": \"AND\",\n" +
//           "                        \"srchItem\": \"\",\n" +
//           "                        \"indexFieldValue\": \"\",\n" +
//           "                        \"dataType\": \"\",\n" +
//           "                        \"dateFormat\": \"\",\n" +
//           "                        \"dataMap\": [],\n" +
//           "                        \"srchRelation\": \"\",\n" +
//           "                        \"srchRelationValue\": \"\",\n" +
//           "                        \"hasRelatedItems\": false,\n" +
//           "                        \"relatedItems\": [],\n" +
//           "                        \"conceptPolymerization\": \"\",\n" +
//           "                        \"conceptPolymerizationSource\": \"\",\n" +
//           "                        \"recommen\": \"\",\n" +
//           "                        \"intelliSense\": \"\"\n" +
//           "                    }\n" +
//           "                ]\n" +
//           "            },\n" +
//           "            {\n" +
//           "                \"uuId\": \"f87672ba-d19a-48c5-883c-9afc9b0f4e76\",\n" +
//           "                \"collection\": [\n" +
//           "                    {\n" +
//           "                        \"uuId\": \"13cec4ca-b7e8-4044-a34a-e8ef6b1dcb90\",\n" +
//           "                        \"logic\": \"\",\n" +
//           "                        \"srchItem\": \"就诊.就诊基本信息.就诊年龄（岁）\",\n" +
//           "                        \"indexFieldValue\": \"visits.visit_info.AGE\",\n" +
//           "                        \"dataType\": \"long\",\n" +
//           "                        \"dateFormat\": \"\",\n" +
//           "                        \"srchRelation\": \">\",\n" +
//           "                        \"srchRelationValue\": \"0\",\n" +
//           "                        \"hasRelatedItems\": false,\n" +
//           "                        \"relatedItems\": [],\n" +
//           "                        \"conceptPolymerization\": \"\",\n" +
//           "                        \"conceptPolymerizationSource\": \"\",\n" +
//           "                        \"recommen\": \"\",\n" +
//           "                        \"intelliSense\": \"\",\n" +
//           "                        \"uiItem\": \"就诊.就诊基本信息.就诊年龄（岁）\"\n" +
//           "                    },\n" +
//           "                    {\n" +
//           "                        \"uuId\": \"a83ce599-ff51-4856-a273-38b5bacef73d\",\n" +
//           "                        \"logic\": \"AND\",\n" +
//           "                        \"srchItem\": \"\",\n" +
//           "                        \"indexFieldValue\": \"\",\n" +
//           "                        \"dataType\": \"\",\n" +
//           "                        \"dateFormat\": \"\",\n" +
//           "                        \"dataMap\": [],\n" +
//           "                        \"srchRelation\": \"\",\n" +
//           "                        \"srchRelationValue\": \"\",\n" +
//           "                        \"hasRelatedItems\": false,\n" +
//           "                        \"relatedItems\": [],\n" +
//           "                        \"conceptPolymerization\": \"\",\n" +
//           "                        \"conceptPolymerizationSource\": \"\",\n" +
//           "                        \"recommen\": \"\",\n" +
//           "                        \"intelliSense\": \"\"\n" +
//           "                    }\n" +
//           "                ]\n" +
//           "            }\n" +
//           "        ],\n" +
//           "        \"sort\": [\n" +
//           "            {\n" +
//           "                \"field\": \"visits.visit_info.ADMISSION_DATE\",\n" +
//           "                \"order\": \"desc\"\n" +
//           "            }\n" +
//           "        ]\n" +
//           "    }";
//    public static void main(String[] args) {
//        QueryUqlClass queryUqlClass = new QueryUqlClass();
////        String sql = queryUqlClass.getSqlWhere(test1);
//        String sql = queryUqlClass.getSqlWhere(test7);
//        System.out.println(sql);
//    }
//}
