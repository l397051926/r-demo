package com.gennlife.rws.query;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.util.StringUtils;
import com.gennlife.rws.util.TransPatientSql;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author
 * @create 2018 21 12:25
 * @desc
 **/
public class UqlQureyResult {
    private static final String PATIENT_SN = "PATIENT_SN";

    public static JSONObject getHits(String result) {
        JSONObject object = JSONObject.parseObject(result);
        return object.getJSONObject("hits");
    }

    public static JSONObject getHits(JSONObject result) {
        return result.getJSONObject("hits");
    }

    public static Integer getTotal(String result) {
        try {
            JSONObject object = getHits(result);
            return object.getInteger("total");
        }catch (Exception e){
            return 0;
        }
    }

    public static Integer getTotal(JSONObject result) {
        JSONObject object = getHits(result);
        return object.getInteger("total");
    }

    public static JSONArray getHitsArray(String result) {
        JSONObject object = getHits(result);
        if (object == null) {
            return null;
        }
        return object.getJSONArray("hits");
    }

    public static JSONArray getHitsArray(JSONObject result) {
        JSONObject object = getHits(result);
        if (object == null) {
            return null;
        }
        return object.getJSONArray("hits");
    }


    public static JSONArray getResultData(String result, String activeId,JSONArray refActiveIds) {
        JSONArray data = new JSONArray();
        JSONArray hits = getHitsArray(result);
        int size = hits == null ? 0 : hits.size();

        for (int i = 0; i < size; i++) {
            JSONObject dataObj = new JSONObject();
            JSONObject patientInfo = hits.getJSONObject(i);
            String patientSn = patientInfo.getString("_id");
            dataObj.put(PATIENT_SN, patientSn);
            JSONObject selectField = patientInfo.getJSONObject("_source").getJSONObject("select_field");
            if(selectField != null){
                setResultDataByMap(dataObj, activeId, selectField);
            }
            JSONObject patient = patientInfo.getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0);
            setResultDataByMap(dataObj, activeId, patient);
            data.add(dataObj);
            int refSize = refActiveIds == null?0:refActiveIds.size();
            for (int j = 0; j < refSize; j++) {
                if(!dataObj.containsKey("t"+refActiveIds.getString(j))) dataObj.put("t"+refActiveIds.getString(j),"-");
            }
            if(!dataObj.containsKey(activeId)) dataObj.put(activeId,"-");
        }

        return data;
    }

    public static JSONArray getResultData(String result, String activeId,JSONArray refActiveIds,boolean isFirst) {
        JSONArray data = new JSONArray();
        JSONArray hits = getHitsArray(result);
        int size = hits == null ? 0 : hits.size();

        for (int i = 0; i < size; i++) {
            JSONObject dataObj = new JSONObject();
            JSONObject patientInfo = hits.getJSONObject(i);
            String patientSn = patientInfo.getString("_id");
            dataObj.put(PATIENT_SN, patientSn);
            JSONObject selectField = patientInfo.getJSONObject("_source").getJSONObject("select_field");
            setResultDataByEnum(dataObj, activeId, selectField,isFirst);
            JSONObject patient = patientInfo.getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0);
            setResultDataByMap(dataObj, activeId, patient);
            int refSize = refActiveIds == null?0:refActiveIds.size();
            for (int j = 0; j < refSize; j++) {
                if(!dataObj.containsKey("t"+refActiveIds.getString(j))) dataObj.put("t"+refActiveIds.getString(j),"-");
            }
            data.add(dataObj);
        }
        return data;
    }
    public static JSONArray getResultData(String result, String activeId,JSONArray refActiveIds,boolean isFirst,String crfId) {
        JSONArray data = new JSONArray();
        JSONArray hits = getHitsArray(result);
        int size = hits == null ? 0 : hits.size();

        for (int i = 0; i < size; i++) {
            JSONObject dataObj = new JSONObject();
            JSONObject patientInfo = hits.getJSONObject(i);
            String patientSn = patientInfo.getString("_id");
            dataObj.put(PATIENT_SN, patientSn);
            JSONObject selectField = patientInfo.getJSONObject("_source").getJSONObject("select_field");
            setResultDataByEnum(dataObj, activeId, selectField,isFirst);
            JSONObject patient = null;
            if(StringUtils.isNotEmpty(crfId)){
                 patient = patientInfo.getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0).getJSONArray("patient_basicinfo").getJSONObject(0);
            }else {
                 patient = patientInfo.getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0);
            }
            setResultDataByMap(dataObj, activeId, patient);
            int refSize = refActiveIds == null?0:refActiveIds.size();
            for (int j = 0; j < refSize; j++) {
                if(!dataObj.containsKey("t"+refActiveIds.getString(j))) dataObj.put("t"+refActiveIds.getString(j),"-");
            }
            data.add(dataObj);
        }
        return data;
    }

    public static JSONArray getActiveVisitSn(String result,String activeIndexId) {
        JSONArray data = new JSONArray();
        JSONArray hits = getHitsArray(result);
        int size = hits == null ? 0 : hits.size();

        for (int i = 0; i < size; i++) {
            JSONObject tmpObj = hits.getJSONObject(i);
            String visitSn = tmpObj.getJSONObject("_source").getJSONObject("select_field").getString("condition");

            if(StringUtils.isEmpty(visitSn)){
                visitSn = tmpObj.getJSONObject("_source").getJSONObject("select_field").getString("t"+activeIndexId+".condition");
            }
            data.add(visitSn);
        }
        return data;
    }


    private static void setResultDataByMap(JSONObject dataObj, String activeId, JSONObject resultObj) {
        for (String key : resultObj.keySet()) {
            if(key.contains(activeId+".near")){
                dataObj.put(activeId, resultObj.getString(key));
            }else if(key.contains(activeId+".condition") && !dataObj.containsKey(activeId)) {
                dataObj.put(activeId, resultObj.getString(key));
            }
            else if ("condition".equals(key) || "t1.condition".equals(key) ) {
                String colle = resultObj.getString(key);
                if(colle.contains(".")){
                    try {
                        colle = String.format("%.2f",  Double.parseDouble(colle));
                    }catch (Exception e){

                    }
                }
                dataObj.put(activeId, colle);
            }  else if (key.contains("condition") ||key.contains(".count") ) {
                dataObj.put(key.substring(0, key.indexOf(".")), resultObj.getString(key));
            } else  if(key.contains("count")){
                dataObj.put(key, resultObj.getString(key));
            } else {
                dataObj.put(key, resultObj.getString(key));
            }
        }
    }

    public static void main(String[] args) {
        long maxLong = Long.MAX_VALUE;
        long minLong = Long.MIN_VALUE;

        double max = Double.MAX_VALUE;
        double min = Double.MIN_VALUE;
        System.out.println("maxlong"+maxLong);
        System.out.println("minlong"+minLong);
        System.out.println("maxDoublt"+max);
        System.out.println("minDouble"+min);

        System.out.println(max > maxLong);

        String val = "{\"a\":13,\"b\":10.1,\"c\":\"aaa\"}";
        JSONObject obj = JSONObject.parseObject(val);
        String a = obj.getString("a");
        long b = obj.getLong("a");
        double c =obj.getDouble("a");

        long d = obj.getLong("b");
        double e = obj.getDouble("b");
        String f = obj.getString("b");

//        String xx = "123";
//        String yy = "abd";
//        long zz = Long.valueOf(xx);
//        long z2 = Long.valueOf(yy);

//        long g =obj.getLong("c");
//        double h = obj.getDouble("c");
//        String i = obj.getString("c");

        System.out.println("----");
    }

    private static Object transForExcludeValue(Object value){
        try {
            if(value instanceof Long){
                Long result = Long.valueOf(value.toString());
                if(result >= Long.MAX_VALUE){
                    value = 0;
                }else {
                    value = result;
                }
            }else if(value instanceof Double){
                Double result = Double.valueOf(value.toString());
                if(result >= Long.MAX_VALUE){
                    value = 0;
                }else {
                    value = result;
                }
            }else {
                if("9.223372036854776E+18".equals(value.toString())){
                    value =  0;
                }
            }
            return value;
        }catch (Exception e){
            return value;
        }
    }
    private static void setResultDataByEnum(JSONObject dataObj, String activeId, JSONObject resultObj,boolean isFirst) {
        for (String key : resultObj.keySet()) {
            if ("condition".equals(key) || "t1.condition".equals(key) ) {
                Object value = transForExcludeValue(resultObj.get(key));
                dataObj.put("t"+activeId, value);
            } else if(key.contains("near")){
                String keyTmp = key.substring(0, key.indexOf("."));
                dataObj.put(keyTmp,transForExcludeValue(resultObj.get(key)));
            }  else if (key.contains("condition")  ) {
                String keyTmp = key.substring(0, key.indexOf("."));
                dataObj.put(keyTmp, transForExcludeValue(resultObj.get(key)));
            } else if( key.contains(".count")){
                dataObj.put(key.split("\\.")[0]+"count",resultObj.getString(key));
            } else  if(key.contains("count")){
                if(isFirst){
                    dataObj.put(key, 1);
                }else {
                    dataObj.put(key, resultObj.getString(key));
                }
            } else {
                dataObj.put(key, transForExcludeValue(resultObj.get(key)));
            }
        }
    }

    public static JSONObject getAggs(String result) {
        JSONObject object = JSONObject.parseObject(result);
        return getAggs(object);

    }
    public static JSONObject getAggs(String result,String crfId,JSONArray aggretionTeam) {
        JSONObject object = JSONObject.parseObject(result);
        return getAggs(object,crfId,aggretionTeam);

    }

    public static JSONObject getAggs(JSONObject object) {
        JSONObject aggregations = object.getJSONObject("aggregations");
        aggregations.getJSONObject("patient_info.GENDER_terms_agg").put("domain_desc", "性别");
        aggregations.getJSONObject("patient_info.ETHNIC_terms_agg").put("domain_desc", "民族");
        aggregations.getJSONObject("patient_info.MARITAL_STATUS_terms_agg").put("domain_desc", "婚姻");
        aggregations.getJSONObject("patient_info.NATIONALITY_terms_agg").put("domain_desc", "国籍");
        return aggregations;
    }

    public static JSONObject getAggs(JSONObject object,String crfId,JSONArray aggregationTeam) {
        JSONObject aggregations = object.getJSONObject("aggregations");
        for (int i = 0; i < aggregationTeam.size(); i++) {
            JSONObject aggObj = aggregationTeam.getJSONObject(i);
            String name = aggObj.getString("domain_name");
            String desc = aggObj.getString("domain_desc");
            aggregations.getJSONObject(IndexContent.getPatientInfo(crfId)+"."+name+"_terms_agg").put("domain_desc", desc);
        }
//        aggregations.getJSONObject(IndexContent.getPatientInfo(crfId)+".GENDER_terms_agg").put("domain_desc", "性别");
//        aggregations.getJSONObject(IndexContent.getPatientInfo(crfId)+".ETHNIC_terms_agg").put("domain_desc", "民族");
//        aggregations.getJSONObject(IndexContent.getPatientInfo(crfId)+".MARITAL_STATUS_terms_agg").put("domain_desc", "婚姻");
//        aggregations.getJSONObject(IndexContent.getPatientInfo(crfId)+".NATIONALITY_terms_agg").put("domain_desc", "国籍");
        return aggregations;
    }

    public static JSONArray getQueryData(JSONObject jsonData) {
        JSONArray hits = getHitsArray(jsonData);
        int size = hits == null ? 0 : hits.size();
        JSONArray data = new JSONArray();
        for (int i = 0; i < size; i++) {
            JSONObject tmpObj = hits.getJSONObject(i);
            data.add(tmpObj.getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0));
        }
        return data;

    }
    public static JSONArray getQueryData(JSONObject jsonData,String crfId) {
        JSONArray hits = getHitsArray(jsonData);
        int size = hits == null ? 0 : hits.size();
        JSONArray data = new JSONArray();
        for (int i = 0; i < size; i++) {
            JSONObject tmpObj = hits.getJSONObject(i);
            if(StringUtils.isEmpty(crfId) || IndexContent.EMR_CRF_ID.equals(crfId)){
                data.add(tmpObj.getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0));
            }else {
                data.add(tmpObj.getJSONObject("_source").getJSONArray("patient_info").getJSONObject(0).getJSONArray("patient_basicinfo").getJSONObject(0));
            }
        }
        return data;

    }


    public static JSONArray getActiveData(JSONObject searchResult, String keys, String visitSns) {
        JSONArray hits = getHitsArray(searchResult);
        String[] arrayKey = keys.split("\\.");

        JSONObject tmpObj = hits.getJSONObject(0);
        if ("patient_info".equals(keys)) {
            return tmpObj.getJSONObject("_source").getJSONArray(keys);
        } else {
            JSONArray tmpArray = tmpObj.getJSONObject("_source").getJSONArray("visits");
            JSONArray resultArray = new JSONArray();
            disposeHitsData(arrayKey[arrayKey.length - 1], tmpArray, resultArray, false, visitSns);
            return resultArray;
        }
    }

    public static void disposeHitsData(String target, JSONArray data, JSONArray result, Boolean isTarget, String visitSns) {
        int size = data == null ? 0 : data.size();
        for (int i = 0; i < size; i++) {
            JSONObject tmpData = data.getJSONObject(i);
            for (String key : tmpData.keySet()) {
                if (target.equals(key)) {
                    String visitSn = tmpData.getJSONArray(key).getJSONObject(0).getString("VISIT_SN");
                    if ( StringUtils.isNotEmpty(visitSn) && visitSns.contains(visitSn)) isTarget = true;
                }
                Object tmpArray = tmpData.get(key);
                if (tmpArray instanceof JSONArray) {
                    disposeHitsData(target, (JSONArray) tmpArray, result, isTarget, visitSns);
                }
            }
            if (isTarget) {
                result.add(tmpData);
                isTarget = false;
            }
        }
    }

    public static String getVisitSnAll(JSONObject jsonData) {
        JSONArray hitst = getHitsArray(jsonData);
        int size = hitst == null ? 0 : hitst.size();
        Set<String> visSet =  new HashSet<>();

        for (int i = 0; i < size; i++) {
            JSONObject hitsObj = hitst.getJSONObject(i);
            String visitSn = hitsObj.getJSONObject("_source").getJSONObject("select_field").getString("condition");
            String[] vistArray = visitSn.split(",");
            Arrays.stream(vistArray).forEach(x -> visSet.add(x));
        }

        return TransPatientSql.transForExtContain(visSet);
    }

    public static JSONObject getSelectField(JSONObject object) {
        JSONArray hitst = getHitsArray(object);
        if (hitst.size()==0){
            return new JSONObject();
        }
         return hitst.getJSONObject(0).getJSONObject("_source").getJSONObject("select_field");
    }
}
