/**
 * copyRight
 */

package com.gennlife.rws.service.impl;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.content.CommonContent;
import com.gennlife.rws.service.ModuleConvertService;
import com.gennlife.rws.util.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**

 * @author liuzhen

 * Created by liuzhen.

 * Date: 2018/7/10

 * Time: 14:32

 */

@Service

public class ModuleConvertServiceImpl implements ModuleConvertService {

    @Override
    public JSONObject uiToRws(JSONObject source) {

        JSONObject result = new JSONObject();

        basicInfo(source, result);

        firstIterators(source, result);

        JSONArray inners = result.getJSONArray("inner");

        convertDetails(result);

        //遍历inner

        iteratorAllInner(inners);

        result.put("inner", inners);

        return result;

    }

    @Override
    public JSONObject rwsToUi(JSONObject source) {

        return convertToFront(source);

    }

    @Override
    public JSONObject uiToSearch(JSONObject source) {

        return null;

    }

    @Override
    public JSONArray enumFormat(JSONArray configsst, Boolean enumEmity) {
        int size = configsst == null ? 0 : configsst.size();
        JSONArray result = new JSONArray();
        Set<String> set = new HashSet<>();
        for (int i = 0; i<size; i++) {
            JSONObject config = configsst.getJSONObject(i);
            String indexTypeDesc = config.getString("indexTypeDesc");
            if(!CommonContent.ACTIVE_INDEX_TYPE_DESC.equals(indexTypeDesc)){
                return configsst;
            }
            JSONArray conditions = config.getJSONArray("conditions");
            int length = conditions == null ? 0 : conditions.size();
            for (int j = 0; j<length; j++) {
                JSONObject newConfig = new JSONObject();
                formatConfig(newConfig,config);
                JSONObject condition = conditions.getJSONObject(j);
                Integer isOther = condition.getInteger("isOther");
                newConfig.put("isOther",isOther);
                if(isOther !=null && isOther == 1){
                    continue;
                }
                //加入枚举值名字
                String indexResultValue = condition.getString("indexResultValue").trim();
                newConfig.put("indexResultValue",indexResultValue);
                if(set.contains(indexResultValue)){
                    enumEmity = true;
                }
                set.add(indexResultValue);

                String id = condition.getString("id");
                newConfig.put("id",id);
                //增加 新的conditions
                JSONArray newConditions =new JSONArray();
                newConditions.add(condition);
                newConfig.put("conditions",newConditions);
                //加入新的configs
                result.add(newConfig);
            }
        }
        if(result.size()>0){
            result.getJSONObject(0).put("enumEmpty",enumEmity);
        }
        return result;
    }

    @Override
    public JSONArray enumFormatToUi(JSONArray configsst) {
        int size = configsst == null ? 0 : configsst.size();
        JSONArray newConditions = new JSONArray();
        JSONObject newConfig = new JSONObject();

        for (int i = 0; i<size; i++) {
            JSONObject config = configsst.getJSONObject(i);

            String indexTypeDesc = config.getString("indexTypeDesc");
            //判定是不是枚举 是否需要格式化
            if(!CommonContent.ACTIVE_INDEX_TYPE_DESC.equals(indexTypeDesc)){
                return configsst;
            }
            //遍历
            JSONArray conditions = config.getJSONArray("conditions");
            int length = conditions == null ? 0 : conditions.size();
            formatConfig(newConfig,config);
            for (int j = 0; j<length; j++) {
                //遍历conditions
                JSONObject condition = conditions.getJSONObject(j);
                //加回枚举值名字
                String indexResultValue = config.getString("indexResultValue");
                String id = config.getString("id");
                condition.put("indexResultValue",indexResultValue);
                condition.put("id",id);
                Integer isOther = config.getInteger("isOther");
                condition.put("isOther",isOther);

                //增加newconditions 数据
                newConditions.add(condition);
            }
        }
        newConfig.put("conditions",newConditions);
        JSONArray newConfigs = new JSONArray();
        newConfigs.add(newConfig);

        return newConfigs;
    }

    private void formatConfig(JSONObject newConfig, JSONObject config) {
        for(String key :config.keySet()){
            String value = config.getString(key);
            newConfig.put(key,value);
        }


    }

    /**

     * 获取基本信息

     * @param json

     * @param result

     */
    public void basicInfo(JSONObject json, JSONObject result) {

        String operatorSign = json.getString("operatorSign");

        Integer acceptanceState = json.getInteger("acceptanceState");

        String uuid = json.getString("uuid");

        String before = json.getString("before");

        String after = json.getString("after");

        String nodeType = json.getString("nodeType");

        Integer innerLever = json.getInteger("innerLever");

        Integer orde = json.getInteger("orde");

        String conditionType = json.getString("conditionType");

        String id = json.getString("id") == null ? "" : json.getString("id");
        String parentId = json.getString("parentId") == null ? "" : json.getString("parentId");
        result.put("operatorSign", operatorSign);
        result.put("uuid", uuid);
        result.put("id", id);
        if (before != null && !"".equals(before)) {
            result.put("before", before);
        }
        if (acceptanceState != null && !"".equals(acceptanceState)) {
            result.put("acceptanceState", acceptanceState);
        }
        if (after != null && !"".equals(after)) {

            result.put("after", after);

        }
        if (nodeType != null && !"".equals(nodeType)) {
            result.put("nodeType", nodeType);
        }

        if (innerLever != null && !"".equals(innerLever)) {
            result.put("innerLever", innerLever);
        }

        result.put("parentId", parentId);
        result.put("conditionType",conditionType);
        result.put("orde",orde);
    }

    public void iteratorAllInners(JSONArray inners) {

        int size = inners == null ? 0 : inners.size();

        if (inners == null || inners.isEmpty()) {

            return;

        }

        for (int i = 0; i < size; i++) {

            JSONObject jsonObject = inners.getJSONObject(i);

            JSONObject resultInners = new JSONObject();

            firstIterator(jsonObject, resultInners);

            JSONArray inner1 = resultInners.getJSONArray("inner");

            JSONArray details = resultInners.getJSONArray("details");

            if (inner1 != null && inner1.size() > 0 && details != null && details.size() > 0) {

                JSONArray array = new JSONArray();

                array.add(resultInners);

                jsonObject.put("inner", array);

            }

            jsonObject.remove("positioningBox");

            iteratorAllInners(inner1);

        }

    }

    /**

     * 将参数转换为rws 结构

     * @param json

     * @param result

     */
    public void firstIterator(JSONObject json, JSONObject result) {

        JSONArray positioningBox = json.getJSONArray("positioningBox");

        int size = (positioningBox == null || positioningBox.isEmpty()) ? 0 : positioningBox.size();

        JSONArray details = new JSONArray();

        JSONArray inners = new JSONArray();

        for (int i = 0; i < size; i++) {

            JSONObject boxItem = positioningBox.getJSONObject(i);

            String nodeType = getDataType(boxItem);

            if ("details".equals(nodeType)) {

                details.add(boxItem);

            } else {

                JSONObject res = new JSONObject();

                basicInfo(boxItem, res);

                inners.add(res);

                firstIterator(boxItem, res);

            }

        }

        result.put("details", details);

        result.put("inner", inners);

    }

    public void iteratorAllInner(JSONArray inners) {

        int size = inners == null ? 0 : inners.size();

        if (inners == null || inners.isEmpty()) {

            return;

        }

        for (int i = 0; i < size; i++) {

            JSONObject jsonObject = inners.getJSONObject(i);

            JSONArray inner1 = jsonObject.getJSONArray("inner");

            convertDetails(jsonObject);

            jsonObject.remove("positioningBox");

            iteratorAllInner(inner1);

        }

    }

    public void convertDetails(JSONObject result) {

        JSONArray details = result.getJSONArray("details");

        int size = details == null ? 0 : details.size();

        JSONArray newDetails = new JSONArray();

        for (int i = 0; i < size; i++) {

            JSONObject detail = details.getJSONObject(i);

            if (detail.containsKey("Treedetails")) {
                //treedetails! 获取
                JSONArray newDetails1 = detail.getJSONArray("Treedetails");

                int i1 = newDetails == null ? 0 : newDetails1.size();

                for (int j = 0; j < i1; j++) {

                    JSONObject jsonObject = newDetails1.getJSONObject(j);

//                    jsonObject.put("titleInfo",detail.getString("titleInfo"));
                    String sourceTagName = jsonObject.getString("sourceTagName");
                    jsonObject.put("nodeType", detail.getString("nodeType"));

                    jsonObject.put("acceptanceState", detail.getInteger("acceptanceState"));

                    jsonObject.put("before", detail.getString("before"));

                    jsonObject.put("after", detail.getString("after"));

                    jsonObject.put("innerLever", detail.getString("innerLever"));
                    String[] name = StringUtils.split(sourceTagName, ".");
                    int  nameLength = name ==null? 0:name.length;
//                    if(nameLength>2){
                        if(i1>1 && j==0){
                            JSONArray strongRef = jsonObject.getJSONArray("strongRef");
                            for (int k = 1; k < i1 ; k++) {
                                strongRef.add(newDetails1.getJSONObject(k));
                            }
                        }
                        if(j>0) continue;

                        newDetails.add(jsonObject);

                    /*}else{
                        newDetails.add(jsonObject);
                    }*/

                }


            } else {

                newDetails.add(detail);

            }

        }

        result.put("details", newDetails);

    }

    public void firstIterators(JSONObject json, JSONObject result) {

        JSONArray positioningBox = json.getJSONArray("positioningBox");

        int size = (positioningBox == null || positioningBox.isEmpty()) ? 0 : positioningBox.size();

        JSONArray details = new JSONArray();

        JSONArray inners = new JSONArray();

        for (int i = 0; i < size; i++) {

            JSONObject boxItem = positioningBox.getJSONObject(i);

            String nodeType = getDataType(boxItem);

            if ("details".equals(nodeType)) {

                details.add(boxItem);

            } else {

                JSONObject res = new JSONObject();

                basicInfo(boxItem, res);

                inners.add(res);

                firstIterators(boxItem, res);

            }

        }

        result.put("details", details);

        result.put("inner", inners);

    }

    /**

     * 获取数据类型

     * @param json

     * @return

     */
    public String getDataType(JSONObject json) {

        String nodeType = json.getString("nodeType");

        if ("placeholder".equals(nodeType) || "details".equals(nodeType)) {

            return "details";

        } else {

            return "inner";

        }

    }

    public JSONObject convertToFront(JSONObject source) {

        JSONObject result = new JSONObject();

        basicInfo(source, result);

        JSONArray positioningBox = new JSONArray();

        JSONArray details = source.getJSONArray("details");

        convertDetails(details, positioningBox);

        JSONArray inners = source.getJSONArray("inner");

        convertInner(inners, positioningBox);

        result.put("positioningBox", positioningBox);

        List<String> exist = new ArrayList<String>();

        sort(result, exist);

        return result;

    }

    public void sort(JSONObject active, List<String> exist) {

        JSONArray positioningBox = active.getJSONArray("positioningBox");

        int size = positioningBox == null ? 0 : positioningBox.size();

        if (size == 0) {

            return;

        }

        JSONObject first = findFirt(positioningBox);

        JSONArray sortAfter = new JSONArray();

        sortAfter.add(first);

        sortBox(positioningBox, first.getString("after"), sortAfter, exist);

        active.put("positioningBox", sortAfter);

        for (int i = 0; i < size; i++) {

            JSONObject jsonObject = positioningBox.getJSONObject(i);

            sort(jsonObject, exist);

        }


    }

    public void convertDetails(JSONArray details, JSONArray positioningBox) {

//        int size = details == null ? 0 : details.size();
//
//        for (int i = 0; i < size; i++) {
//
//            JSONObject detail = details.getJSONObject(i);
//
//            positioningBox.add(detail);
//
//        }
        int size = details == null ? 0 : details.size();

        Map<String, JSONArray> Treedetails = new ConcurrentHashMap<String, JSONArray>();

        for (int i = 0; i < size; i++) {

            JSONObject detail = details.getJSONObject(i);
            String value = detail.getString("value");
            try {
                JSONArray valuearray = JSONArray.parseArray(value);
                detail.put("value", valuearray);
            } catch (Exception e) {
                detail.put("value", value);
            }


            String uuid = detail.getString("uuid");
            if(StringUtils.isEmpty(uuid)){
                continue;
            }

            if (Treedetails.containsKey(uuid)) {

                JSONArray array = Treedetails.get(uuid);

                array.add(detail);

                Treedetails.put(uuid, array);

            } else {

                JSONArray array = new JSONArray();

                array.add(detail);

                Treedetails.put(uuid, array);

            }

        }

        Set<String> keys = Treedetails.keySet();

        for (String key : keys) {

            JSONArray array = Treedetails.get(key);

            int sizes = array == null ? 0 : array.size();

            if (sizes > 0) {

                JSONObject jsonObject = array.getJSONObject(0);

                String nodeType = jsonObject.getString("nodeType");

                String innerLever = jsonObject.getString("innerLever");

                if ("placeholder".equals(nodeType)) {

                    positioningBox.add(jsonObject);

                } else {

                    JSONObject result = new JSONObject();

                    JSONArray strongRef = jsonObject.getJSONArray("strongRef");

                    int strongRefSize = strongRef !=null ? strongRef.size():0;
                    //将 strongRef 返回给Treedietls
                    for (int i = 0; i < strongRefSize; i++) {
                        array.add(strongRef.getJSONObject(i));
                    }
                    jsonObject.put("strongRef",new JSONArray());

                    //调整value 数据格式
                    int arrSize = array == null ? 0 : array.size();
                    for (int i = 0; i < arrSize; i++) {
                        JSONObject arrayObj = array.getJSONObject(i);
                        String value = arrayObj.getString("value");
                        try {
                            JSONArray valuearray = JSONArray.parseArray(value);
                            arrayObj.put("value", valuearray);
                        } catch (Exception e) {
                            arrayObj.put("value", value);
                        }
                    }
                    result.put("titleInfo",jsonObject.getString("titleInfo"));

                    result.put("titleType",jsonObject.getString("titleType"));

                    result.put("enumActiveConfigId",jsonObject.getString("enumActiveConfigId"));

                    result.put("childrenKey",jsonObject.getString("childrenKey"));

                    result.put("uuid", jsonObject.getString("uuid"));

                    result.put("nodeType", jsonObject.getString("nodeType"));

                    result.put("innerLever", jsonObject.getString("innerLever"));

                    result.put("before", jsonObject.getString("before"));

                    result.put("after", jsonObject.getString("after"));

                    result.put("acceptanceState", jsonObject.getInteger("acceptanceState"));

                    result.put("Treedetails", array);

                    positioningBox.add(result);

                }

            }

        }

    }

    public void convertInner(JSONArray inners, JSONArray positioningBox) {

        int size = inners == null ? 0 : inners.size();


        for (int i = 0; i < size; i++) {
            JSONObject result = new JSONObject();

            JSONArray inBox = new JSONArray();
            JSONObject inner = inners.getJSONObject(i);
            if (inner.containsKey("uuid")) {
            }
            basicInfo(inner, result);

            JSONArray details = inner.getJSONArray("details");

            convertDetails(details, inBox);

            JSONArray inn = inner.getJSONArray("inner");

            if (inn != null && !inn.isEmpty()) {

                convertInner(inn, inBox);
            }
            result.put("positioningBox", inBox);

            positioningBox.add(result);

        }

    }

    public JSONObject findFirt(JSONArray box) {

        int size = box == null ? 0 : box.size();

        for (int i = 0; i < size; i++) {

            JSONObject jsonObject = box.getJSONObject(i);

            String before = jsonObject.getString("before");

            if (before != null && before.equals("undefined")) {

                return jsonObject;

            }


        }

        return null;

    }

    public void sortBox(JSONArray box, String after, JSONArray sortAfter, List<String> exist) {

        int size = box == null ? 0 : box.size();

        if (size == 0) {

            return;

        }

        for (int i = 0; i < size; i++) {

            JSONObject jsonObject = box.getJSONObject(i);

            String uuid = jsonObject.getString("uuid");

            if (after != null && after.equals(uuid)) {

                int sizes = sortAfter == null ? 0 : sortAfter.size();
                sortAfter.add(jsonObject);

                boolean exists = isExists(sortAfter, uuid, sizes);

                if (!exists) {


                }

                sortBox(box, jsonObject.getString("after"), sortAfter, exist);

            }

        }


    }

    private boolean isExists(JSONArray sortAfter, String uuid, int sizes) {

        boolean exists = false;

        for (int j = 0; j < sizes; j++) {

            JSONObject jsonObject1 = sortAfter.getJSONObject(j);

            if (uuid != null && uuid.equals(jsonObject1.getString("uuid"))) {

                exists = true;

            }

        }

        return exists;

    }

}

