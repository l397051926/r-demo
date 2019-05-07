package com.gennlife.rws.util;

import com.gennlife.packagingservice.arithmetic.express.abstracts.AbstractLogicExpress;
import com.gennlife.packagingservice.arithmetic.express.abstracts.ExpressInterface;
import com.gennlife.packagingservice.arithmetic.express.enitity.StaticValueOperandDatas;
import com.gennlife.packagingservice.arithmetic.express.enums.ArrayOpEnum;
import com.gennlife.packagingservice.arithmetic.express.enums.NumberOpEnum;
import com.gennlife.packagingservice.arithmetic.express.exceptions.ConfigError;
import com.gennlife.packagingservice.arithmetic.express.factorys.ConditionOperatorFactory;
import com.gennlife.packagingservice.arithmetic.express.factorys.OperandDataFactory;
import com.gennlife.packagingservice.arithmetic.pretreatment.enums.InstructionOperatorEnum;
import com.gennlife.packagingservice.arithmetic.pretreatment.enums.LogicExpressEnum;
import com.gennlife.packagingservice.arithmetic.utils.JsonAttrUtil;
import com.gennlife.packagingservice.arithmetic.utils.StringUtil;
import com.gennlife.packagingservice.rws.ConfigExcept;
import com.gennlife.packagingservice.rws.RefEnum;
import com.gennlife.packagingservice.rws.RwsCountUtils;
import com.gennlife.packagingservice.rws.entity.RwsObjId;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.*;

import static com.gennlife.packagingservice.arithmetic.express.abstracts.AbstractLogicExpress.checkLogicExpress;
import static com.gennlife.packagingservice.arithmetic.express.abstracts.ExpressInterface.DETAILS_ARRAY_KEY;
import static com.gennlife.packagingservice.arithmetic.express.abstracts.ExpressInterface.OPERATOR_KEY;
import static com.gennlife.packagingservice.arithmetic.express.enitity.DirectOperandDatas.getDirectPath;
import static com.gennlife.packagingservice.arithmetic.express.factorys.OperandDataFactory.DYADIC_REF_ID_KEY;
import static com.gennlife.packagingservice.arithmetic.express.factorys.OperandDataFactory.UNARY_REF_ID_KEY;
import static com.gennlife.packagingservice.rws.RwsCountUtils.*;

/**
 * Created by Chenjinfeng on 2017/10/28.
 */
public class RwsConfigTransUtils {
    public static final String RWS_STATIC_VALUE_KEY = "indexResultValue";
    public static final String RWS_STATIC_MAP_TABLE_NAME = "rws_static_map_table";
    public static final String RIGHT_OP_KEY = "rightCountType";
    public static final String RIGHT_OP_PARAM_KEY = "rightCountParam";
    public static final String RESULTSQL_KEY = "resultsql";
    public static final String REF_ID_LIST_KEY = "idList";
    public static final String IS_TMP_KEY = "isTmp";
    public static final String UNIQUE_ID_KEY = "unique_id";
    public static final String PROJECT_ID_KEY = "projectId";
    public static final String RWS_OPERATOR_SIGN = "operatorSign";
    public static final String RWS_ATTR_CONDITION_KEY = "attr";
    public static final String RWS_CONDITIONS_KEY = "conditions";
    public static final String RWS_STRONG_REF_KEY = "strongRef";
    public static final String RWS_NEED_PATH_KEY = "needPath";
    public static final String RWS_DETAILS_KEY = "detail";
    public static final String RWS_VALUE_LEY = "value";
    public static final String RWS_ORIGIN_ID = "rws_origin_id";
    public static final String RWS_OBJ_HEAD = "rwsJavaObj";
    public static final String RWS_PATH_SET = "paths";

    public static JsonObject transRwsConditionConfig(JsonArray condition) throws ConfigExcept {
        if (JsonAttrUtil.isEmptyJsonElement(condition)) return null;
        JsonObject configJson = null;
        JsonObject onlyOne = condition.remove(0).getAsJsonObject();
        JsonAttrUtil.makeEmpty(condition);
        condition.add(onlyOne);
        opForStrongRefNeedPath(onlyOne, false);
        String operator = JsonAttrUtil.getStringValue(RWS_OPERATOR_SIGN, onlyOne);
        String value = JsonAttrUtil.getStringValue(RWS_VALUE_LEY, onlyOne);
        if (StringUtil.isEmptyStr(operator) && StringUtil.isEmptyStr(value))
            return null;
        JsonArray detailCheck = JsonAttrUtil.getJsonArrayValue(RWS_DETAILS_KEY, onlyOne);
        if (JsonAttrUtil.isEmptyJsonElement(detailCheck)) return null;
        if (detailCheck.size() == 1) {
            JsonObject detailOne = detailCheck.get(0).getAsJsonObject();
            operator = JsonAttrUtil.getStringValue(RWS_OPERATOR_SIGN, detailOne);
            value = JsonAttrUtil.getStringValue(RWS_VALUE_LEY, detailOne);
            if (StringUtil.isEmptyStr(operator) && StringUtil.isEmptyStr(value))
                return null;
        }
        for (JsonElement element : condition) {
            JsonObject config = new JsonObject();
            transActiveConfig(element.getAsJsonObject(), config);
            if (configJson == null)
                configJson = config;
            else {
                JsonArray detail = JsonAttrUtil.getJsonArrayValue(ExpressInterface.DETAILS_ARRAY_KEY, configJson);
                JsonArray tmpDetail = JsonAttrUtil.getJsonArrayValue(ExpressInterface.DETAILS_ARRAY_KEY, config);
                detail.addAll(tmpDetail);
            }
        }
        return configJson;
    }

    public static Set<String> getRefIdList(JsonObject condition) {
        Set<String> idList = new TreeSet<>();
        traceForRefIdList(condition, idList);
        return idList;
    }

    private static void traceForRefIdList(JsonObject condition, Collection<String> idList) {
        String operator = JsonAttrUtil.getStringValue(ExpressInterface.OPERATOR_KEY, condition);
        LogicExpressEnum logic = AbstractLogicExpress.checkLogicExpress(operator);
        if (logic != null) {
            JsonArray detail = JsonAttrUtil.getJsonArrayValue(ExpressInterface.DETAILS_ARRAY_KEY, condition);
            if (detail == null) return;
            for (JsonElement element : detail) {
                traceForRefIdList(element.getAsJsonObject(), idList);
            }
        } else {
            String id = JsonAttrUtil.getStringValue(UNARY_REF_ID_KEY, condition);
            if (!StringUtil.isEmptyStr(id)) idList.add(id);
            id = JsonAttrUtil.getStringValue(DYADIC_REF_ID_KEY, condition);
            if (!StringUtil.isEmptyStr(id)) idList.add(id);
            id = JsonAttrUtil.getStringValue(RWS_ORIGIN_ID, condition);
            if (!StringUtil.isEmptyStr(id)) idList.add(id);
        }

    }

    public static void transConfigItemForCondition(JsonObject configJson) throws ConfigExcept {
        JsonArray attr = JsonAttrUtil.getJsonArrayValue(RWS_ATTR_CONDITION_KEY, configJson);
        String method = JsonAttrUtil.getStringValue(RWS_CONF_METHOD_KEY, configJson);
        if (isStaticMethod(method)) {
            for (JsonElement element : attr) {
                JsonObject attrItem = element.getAsJsonObject();
                JsonObject first = JsonAttrUtil.getJsonObjectValue(RWS_CONDITIONS_KEY, attrItem);
                String operator = JsonAttrUtil.getStringValue(RWS_OPERATOR_SIGN, first);
                if (StringUtil.isEmptyStr(operator)) {
                    attrItem.remove(RWS_CONDITIONS_KEY);
                }
            }
        }
        Set<String> idList = new TreeSet<>();
        TreeSet<String> pathSet = new TreeSet<>();
        for (JsonElement element : attr) {
            JsonObject attrItem = element.getAsJsonObject();
            if (attrItem.has(RWS_CONDITIONS_KEY)) {
                JsonObject conditionJson = transRwsConditionConfig(attrItem.getAsJsonArray(RWS_CONDITIONS_KEY));
                idList.addAll(getRefIdList(conditionJson));
                attrItem.remove(RWS_CONDITIONS_KEY);
                String id = JsonAttrUtil.getStringValue(com.gennlife.packagingservice.rws.RwsConfigTransUtils.UNIQUE_ID_KEY, attrItem);
                attrItem.add(RwsCountUtils.CONDTION_KEY, conditionJson);
                if (!StringUtil.isEmptyStr(id)) idList.add(id);
                setAllOpPath(pathSet, conditionJson);
            } else if (!isStaticMethod(method)) {
                throw new ConfigExcept("条件配置错误 : 条件组 attr 为空");
            }
        }
        configJson.add(REF_ID_LIST_KEY, JsonAttrUtil.toJsonTree(idList));
        configJson.add(com.gennlife.packagingservice.rws.RwsConfigTransUtils.RWS_PATH_SET, JsonAttrUtil.toJsonTree(pathSet));
    }
    public static void setAllOpPath(Set<String> pathSet, JsonObject conditionConf) {
        if (JsonAttrUtil.isEmptyJsonElement(conditionConf)) {
            return;
        }
        if (pathSet == null) return;
        String operator = JsonAttrUtil.getStringValue(OPERATOR_KEY, conditionConf);
        LogicExpressEnum logicExpressEnum = checkLogicExpress(operator);
        if (logicExpressEnum == null) {
            String path = getDirectPath(conditionConf);
            if (!StringUtil.isEmptyStr(path)) pathSet.add(path);
        } else {
            JsonArray details = JsonAttrUtil.getJsonArrayValue(DETAILS_ARRAY_KEY, conditionConf);
            if (details == null) throw new ConfigError(DETAILS_ARRAY_KEY + " is null ,operator == " + operator);
            for (JsonElement element : details) {
                setAllOpPath(pathSet, element.getAsJsonObject());
            }
        }


    }
    private static void transActiveConfig(JsonObject configItem, JsonObject result) throws ConfigExcept {
        String operator = JsonAttrUtil.getStringValue(RWS_OPERATOR_SIGN, configItem);
        String needPath = JsonAttrUtil.getStringValue(RWS_NEED_PATH_KEY, configItem);
        JsonArray originDetail = JsonAttrUtil.getJsonArrayValue(RWS_DETAILS_KEY, configItem);
        LogicExpressEnum logicExpressEnum = AbstractLogicExpress.checkLogicExpress(operator);
        if (logicExpressEnum == null) {
            InstructionOperatorEnum itemEnum = ConditionOperatorFactory.check(operator);
            if (itemEnum == null) {
                String ref = JsonAttrUtil.getStringValue("refRelation", configItem);
                RefEnum refEnum = null;
                try {
                    refEnum = RefEnum.valueOf(ref.toUpperCase());
                } catch (Exception e) {
                    throw new ConfigExcept("条件配置错误 ref " + ref);
                }
                JsonElement value = JsonAttrUtil.getJsonElement(RWS_VALUE_LEY, configItem);
                if (JsonAttrUtil.isEmptyJsonElement(value)) {
                    throw new ConfigExcept("条件配置错误  value is null");
                }
                if (StringUtil.isEmptyStr(operator))
                    throw new ConfigExcept("条件配置错误  operator is null");

                String[] ops = operator.split("#");
                if (ops.length <= 1) {
                    throw new ConfigExcept("条件配置错误  operator " + operator + " 最小长度 2");
                }
                String type = ops[0];

                //复合运算
                if (type.equalsIgnoreCase("simpleDate") || type.equalsIgnoreCase("simpleNumber")) {
                    if (refEnum == RefEnum.REF) {
                        throw new ConfigExcept("条件配置错误 不应该有右引用 ");
                    }
                    if (type.equalsIgnoreCase("simpleDate") && ops[1].equalsIgnoreCase("scope")) {
                        result.addProperty(ExpressInterface.OPERATOR_KEY, "and");
                        result.addProperty(ExpressInterface.NEED_PATH_KEY, needPath);
                        JsonArray array = value.getAsJsonArray();
                        LinkedList<JsonObject> newDetail = new LinkedList<>();
                        if (!JsonAttrUtil.isEmptyJsonElement(array.get(0))) {
                            JsonPrimitive newValue = new JsonPrimitive(">;=;" + array.get(0).getAsString());
                            JsonObject newChildJson = new JsonObject();
                            setSimpleProp(configItem, newChildJson, type, "", newValue);
                            newDetail.add(newChildJson);
                        }
                        if (!JsonAttrUtil.isEmptyJsonElement(array.get(1))) {
                            JsonPrimitive newValue = new JsonPrimitive("<;=;" + array.get(1).getAsString());
                            JsonObject newChildJson = new JsonObject();
                            setSimpleProp(configItem, newChildJson, type, "", newValue);
                            newDetail.add(newChildJson);
                        }
                        result.add(ExpressInterface.DETAILS_ARRAY_KEY, JsonAttrUtil.toJsonTree(newDetail));
                    } else {
                        JsonPrimitive newValue = new JsonPrimitive(ops[1] + ";" + value.getAsString());
                        setSimpleProp(configItem, result, type, needPath, newValue);
                    }
                } else {
                    if (refEnum != RefEnum.REF) {
                        throw new ConfigExcept("条件配置错误 必须有右引用 ");
                    }
                    String newop = null;
                    if (type.equalsIgnoreCase("refDateScope")) newop = "simpleDate";
                    else if (type.equalsIgnoreCase("refNumberScope")) newop = "simpleNumber";
                    else throw new ConfigExcept("条件配置错误 未知 operator  " + operator);
                    String countType = ops[1];
                    boolean equal = false;
                    if (ops.length == 3 && ops[2].equalsIgnoreCase("=")) equal = true;
                    if (newop.equalsIgnoreCase("simpleDate")) equal = true;
                    JsonArray array = value.getAsJsonArray();
                    LinkedList<JsonObject> newDetail = new LinkedList<>();
                    if (!JsonAttrUtil.isEmptyJsonElement(array.get(0))) {
                        String newValue = ">;";
                        if (equal) {
                            newValue = newValue + "=";
                        }
                        JsonObject newChildJson = new JsonObject();
                        newChildJson.addProperty(RIGHT_OP_KEY, countType);
                        newChildJson.add(RIGHT_OP_PARAM_KEY, array.get(0));
                        setSimpleProp(configItem, newChildJson, newop, "", new JsonPrimitive(newValue));
                        addExtraForDyaic(countType, array, newChildJson, array.get(0));
                        newDetail.add(newChildJson);
                    }
                    if (!JsonAttrUtil.isEmptyJsonElement(array.get(1))) {
                        String newValue = "<;";
                        if (equal) {
                            newValue = newValue + "=";
                        }
                        JsonObject newChildJson = new JsonObject();
                        newChildJson.addProperty(RIGHT_OP_KEY, countType);
                        newChildJson.add(RIGHT_OP_PARAM_KEY, array.get(1));
                        setSimpleProp(configItem, newChildJson, newop, "", new JsonPrimitive(newValue));
                        addExtraForDyaic(countType, array, newChildJson, array.get(1));
                        newDetail.add(newChildJson);
                    }
                    result.addProperty(ExpressInterface.OPERATOR_KEY, "and");
                    result.addProperty(ExpressInterface.NEED_PATH_KEY, needPath);
                    result.add(ExpressInterface.DETAILS_ARRAY_KEY, JsonAttrUtil.toJsonTree(newDetail));
                }

            } else {
                JsonElement value = JsonAttrUtil.getJsonElement(RWS_VALUE_LEY, configItem);
                if (JsonAttrUtil.isEmptyJsonElement(value)) {
                    throw new ConfigExcept("条件配置错误  value is null" + operator);
                }
                setSimpleProp(configItem, result, operator, needPath, value);
            }


        } else {
            result.addProperty(ExpressInterface.OPERATOR_KEY, operator);
            result.addProperty(ExpressInterface.NEED_PATH_KEY, needPath);
            LinkedList<JsonObject> detail = new LinkedList<>();
            for (JsonElement element : originDetail) {

                JsonObject asJsonObject = element.getAsJsonObject();
                JsonElement value = asJsonObject.get("value");
                JsonObject innerConfig = new JsonObject();
                detail.add(innerConfig);
                if(value != null){
                    transActiveConfig(asJsonObject, innerConfig);
                }else{
                    transActiveConfig(asJsonObject, innerConfig);
                    /* JsonArray details = JsonAttrUtil.getJsonArrayValue(RWS_DETAILS_KEY, asJsonObject);
                    for (JsonElement el : details) {
                        JsonObject asJsonObject1 = el.getAsJsonObject();
                        JsonElement va = asJsonObject1.get("value");
                        if(va != null){
                            JsonObject inner = new JsonObject();
                            transActiveConfig(asJsonObject, inner);
                            detail.add(inner);
                        }
                    }*/
                }
            }
            result.add(ExpressInterface.DETAILS_ARRAY_KEY, JsonAttrUtil.toJsonTree(detail));
        }
    }

    private static void addExtraForDyaic(String countType, JsonArray array, JsonObject newChildJson, JsonElement param) {
        String id = JsonAttrUtil.getStringValue(DYADIC_REF_ID_KEY, newChildJson);
        newChildJson.addProperty(RWS_ORIGIN_ID, id);
        String newId = getNewUniqueId(id, countType, param);
        newChildJson.addProperty(DYADIC_REF_ID_KEY, newId);
    }

    public static boolean isObjId(String id) {
        if (StringUtil.isEmptyStr(id)) return false;
        return id.startsWith(com.gennlife.packagingservice.rws.RwsConfigTransUtils.RWS_OBJ_HEAD);
    }

    public static RwsObjId id2Obj(String id) {
        try {
            String[] strs = id.split("#");
            RwsObjId obj = new RwsObjId();
            obj.setId(strs[1]);
            obj.setKey(id);
            obj.setCountType(strs[2]);
            JsonElement param = JsonAttrUtil.toJsonElement(strs[3]);
            if (param != null) obj.setParam(param);
            else obj.setParam(new JsonPrimitive(strs[3]));
            return obj;
        } catch (Exception e) {
            return null;
        }
    }

    private static String getNewUniqueId(String id, String countType, JsonElement param) {
        StringBuffer buffer = new StringBuffer(RWS_OBJ_HEAD);
        buffer.append("#")
                .append(id)
                .append("#")
                .append(countType)
                .append("#");
        if (param != null && param.isJsonPrimitive())
            buffer.append(param.getAsString());
        else
            buffer.append(JsonAttrUtil.toJsonStr(param));
        buffer.append("#")
                .append(UUID.randomUUID());
        return buffer.toString();
    }

    private static void setSimpleProp(JsonObject configItem, JsonObject result, String operator, String needPath, JsonElement value) throws ConfigExcept {
        String ref = JsonAttrUtil.getStringValue("refRelation", configItem);
        RefEnum refEnum = null;
        try {
            refEnum = RefEnum.valueOf(ref.toUpperCase());
        } catch (Exception e) {
            throw new ConfigExcept("配置错误 ref " + ref);
        }
        if (JsonAttrUtil.isEmptyJsonElement(value)) {
            throw new ConfigExcept("配置错误  value is null");
        }
        result.add(StaticValueOperandDatas.VALUE_OLD_KEY, value);
        result.addProperty(ExpressInterface.NEED_PATH_KEY, needPath);
        result.addProperty(ExpressInterface.OPERATOR_KEY, operator);
        if (refEnum == RefEnum.DIRECT) {
            String unaryKey = JsonAttrUtil.getStringValue("sourceTagName", configItem);
            if (StringUtil.isEmptyStr(unaryKey)) {
                throw new ConfigExcept("配置错误  sourceTagName is null");
            }
            result.addProperty(ExpressInterface.DETAIL_KEY, unaryKey);
        } else {
            String refId = JsonAttrUtil.getStringValue("refActiveId", configItem);
            if (StringUtil.isEmptyStr(refId)) {
                throw new ConfigExcept("配置错误  refActiveId is null");
            }
            if (refEnum == RefEnum.LEFTREF) {
                result.addProperty(OperandDataFactory.UNARY_TYPE_KEY, OperandDataFactory.MAP_STATIC_TYPE);
                result.addProperty(UNARY_REF_ID_KEY, refId);
                result.addProperty(OperandDataFactory.UNARY_MAP_NAME_KEY, RWS_STATIC_MAP_TABLE_NAME);
            } else if (refEnum == RefEnum.REF) {
                String unaryKey = JsonAttrUtil.getStringValue("sourceTagName", configItem);
                if (StringUtil.isEmptyStr(unaryKey)) {
                    throw new ConfigExcept("配置错误  sourceTagName is null");
                }
                result.addProperty(ExpressInterface.DETAIL_KEY, unaryKey);
                result.addProperty(OperandDataFactory.DYADIC_TYPE_KEY, OperandDataFactory.MAP_STATIC_TYPE);
                result.addProperty(DYADIC_REF_ID_KEY, refId);
                result.addProperty(OperandDataFactory.DYADIC_MAP_NAME_KEY, RWS_STATIC_MAP_TABLE_NAME);
            }
        }
    }


    public static void transConfigItemForNewNeedPath(JsonObject configJson, String needPath) {
        JsonArray attr = JsonAttrUtil.getJsonArrayValue(RWS_ATTR_CONDITION_KEY, configJson);
        for (JsonElement element : attr) {
            JsonObject attrItem = element.getAsJsonObject();
            JsonObject conditionJson = JsonAttrUtil.getJsonObjectValue(RwsCountUtils.CONDTION_KEY, attrItem);
            if (!StringUtil.isEmptyStr(needPath) && !JsonAttrUtil.isEmptyJsonElement(conditionJson))
                traceForNeedPath(conditionJson, needPath);
        }
    }

    private static void traceForNeedPath(JsonObject condition, String needPath) {
        String operator = JsonAttrUtil.getStringValue(ExpressInterface.OPERATOR_KEY, condition);
        LogicExpressEnum logic = AbstractLogicExpress.checkLogicExpress(operator);
        if (logic != null) {
            JsonArray detail = JsonAttrUtil.getJsonArrayValue(ExpressInterface.DETAILS_ARRAY_KEY, condition);
            if (detail == null) return;
            String oldNeedPath = JsonAttrUtil.getStringValue(ExpressInterface.NEED_PATH_KEY, condition);
            if (".".equals(oldNeedPath)) {
                condition.addProperty(ExpressInterface.NEED_PATH_KEY, needPath);
            }
            for (JsonElement element : detail) {
                traceForNeedPath(element.getAsJsonObject(), needPath);
            }
        } else {
            String oldNeedPath = JsonAttrUtil.getStringValue(ExpressInterface.NEED_PATH_KEY, condition);
            if (".".equals(oldNeedPath)) {
                condition.addProperty(ExpressInterface.NEED_PATH_KEY, needPath);
            }
        }

    }

    public static void transActiveConfig(JsonObject configJson) throws ConfigExcept {
        checkActiveConfig(configJson);
        String countPath = JsonAttrUtil.getStringValue(ACTIVE_RESULT_KEY, configJson);
        transConfigItemForCondition(configJson);
        transConfigItemForNewNeedPath(configJson, countPath);
    }

    /**
     * rws的强关联的处理,要么根节点，要么叶子节点
     */
    public static void opForStrongRefNeedPath(JsonObject item, boolean setLeaf) {
        if (setLeaf) item.addProperty(ExpressInterface.NEED_PATH_KEY, "");
        if (item.has(RWS_STRONG_REF_KEY)) {
            JsonArray array = item.getAsJsonArray(RWS_STRONG_REF_KEY);
            if (array != null && array.size() > 0) {
                //String needPath = JsonAttrUtil.getStringValue(ExpressInterface.NEED_PATH_KEY, item);
                JsonObject newLeafJson = new JsonObject();
                JsonAttrUtil.addAllJsonValueIntoAnotherJson(item, newLeafJson);
                JsonAttrUtil.makeEmpty(item);
                item.addProperty(ExpressInterface.NEED_PATH_KEY, ".");
                item.addProperty(RWS_OPERATOR_SIGN, LogicExpressEnum.AND.name());
                newLeafJson.remove(RWS_STRONG_REF_KEY);
                newLeafJson.addProperty(ExpressInterface.NEED_PATH_KEY, "");
                array.add(newLeafJson);
                item.add(RWS_DETAILS_KEY, array);
                int size = array == null ? 0 : array.size();
                for (int i=0; i<size;i++) {
                    JsonElement element = array.get(i);
                    opForStrongRefNeedPath(element.getAsJsonObject(), true);
                }
                return;
            }
        }
        if (item.has(RWS_DETAILS_KEY)) {
            JsonArray array = item.getAsJsonArray(RWS_DETAILS_KEY);
            int size = array == null ? 0 : array.size();
            for (int i=0; i<size;i++) {
                JsonElement element = array.get(i);
                opForStrongRefNeedPath(element.getAsJsonObject(), setLeaf);
            }
        }
    }

    public static void checkFilterPatientConfig(JsonObject configJson) throws ConfigExcept {
        String project_id = JsonAttrUtil.getStringValue(PROJECT_ID_KEY, configJson);
        if (StringUtil.isEmptyStr(project_id))
            throw new ConfigExcept("project_id null");
        String resultsql = JsonAttrUtil.getStringValue(RESULTSQL_KEY, configJson);
        if (StringUtil.isEmptyStr(resultsql)) {
            throw new ConfigExcept("resultsql is null");
        }
    }

    public static void transFilterPatientConfig(JsonObject configJson) throws ConfigExcept {
        checkFilterPatientConfig(configJson);
        JsonArray match = getConditionArray("match", configJson);
        JsonArray filter = getConditionArray("filter", configJson);
        configJson.remove("filter");
        configJson.remove("match");
        Set<String> idList = new TreeSet<>();
        int matchSize = match == null ? 0 : match.size();
        JsonArray matches = new JsonArray();
        for (int i = 0; i<matchSize; i++) {
            JsonElement element = match.get(i);
            JsonArray re = new JsonArray();
            re.add(element);
            JsonObject matchJson = transRwsConditionConfig(re);

            if (matchJson != null) {
                traceForRefIdList(matchJson, idList);
                matches.add(matchJson);
            }
        }
        configJson.add("match", matches);
        int filterSize = filter == null ? 0 : filter.size();
        JsonArray filters = new JsonArray();
        for (int i=0; i<filterSize; i++) {
            JsonElement element = filter.get(i);
            JsonArray array = new JsonArray();
            array.add(element);
            JsonObject filterJson = transRwsConditionConfig(array);
            if (filterJson != null) {
                traceForRefIdList(filterJson, idList);
                filters.add(filterJson);
            }
        }
        configJson.add("filter", filters);
        configJson.add(REF_ID_LIST_KEY, JsonAttrUtil.toJsonTree(idList));
    }

    private static JsonArray getConditionArray(String key, JsonObject configJson) {
        JsonArray match = null;
        JsonElement matchElem = JsonAttrUtil.getJsonElement(key, configJson);
        if (matchElem != null) {
            if (matchElem.isJsonArray()) {
                match = matchElem.getAsJsonArray();
            } else if (matchElem.isJsonObject()) {
                match = new JsonArray();
                match.add(matchElem);
            }
        }
        return match;
    }

    public static void checkActiveConfig(JsonObject configJson) throws ConfigExcept {
        String countPath = JsonAttrUtil.getStringValue(ACTIVE_RESULT_KEY, configJson);
        String resultsql = JsonAttrUtil.getStringValue(RESULTSQL_KEY, configJson);
        if (StringUtil.isEmptyStr(resultsql)) {
            throw new ConfigExcept("resultsql is null");
        }
        String projectId = JsonAttrUtil.getStringValue(PROJECT_ID_KEY, configJson);
        String unique_id = JsonAttrUtil.getStringValue(UNIQUE_ID_KEY, configJson);
        if (StringUtil.isEmptyStr(projectId)) {
            throw new ConfigExcept("projectId is null");
        }
        if (StringUtil.isEmptyStr(unique_id)) {
            throw new ConfigExcept("unique_id is null");
        }
        String method = JsonAttrUtil.getStringValue(RWS_CONF_METHOD_KEY, configJson);
        if (StringUtil.isEmptyStr(method)) {
            throw new ConfigExcept("函数名为空 is null");
        }
        if (!isStaticMethod(method)) {
            if (StringUtil.isEmptyStr(countPath)) {
                throw new ConfigExcept("count path is null [ 指标的计算字段，活动的检索结果 ]");
            }
        }
        JsonArray attr = JsonAttrUtil.getJsonArrayValue(com.gennlife.packagingservice.rws.RwsConfigTransUtils.RWS_ATTR_CONDITION_KEY, configJson);
        if (JsonAttrUtil.isEmptyJsonElement(attr)) {
            throw new ConfigExcept("attr is null");
        }
        int activeType = JsonAttrUtil.getJsonElement(ACTIVE_TYPE_KEY, configJson).getAsInt();
        String sortKey = JsonAttrUtil.getStringValue(SORT_PATH_KEY, configJson);
        if (isActivity(activeType)) {
            if (StringUtil.isEmptyStr(sortKey)) {
                throw new ConfigExcept("排序字段为空(sortKey) is null");
            }
        }
        if (!isStaticMethod(method)) {
            method = method.toUpperCase();
            try {
                NumberOpEnum.valueOf(method);
            } catch (Exception e) {
                try {
                    ArrayOpEnum.valueOf(method);
                } catch (Exception e2) {
                    throw new ConfigExcept("未知函数： " + method);
                }
            }
        }
    }
}
