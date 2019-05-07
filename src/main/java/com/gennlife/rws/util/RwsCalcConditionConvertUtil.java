/**
 * copyRight
 */
package com.gennlife.rws.util;

import com.gennlife.packagingservice.arithmetic.utils.JsonAttrUtil;
import com.gennlife.packagingservice.arithmetic.utils.StringUtil;
import com.gennlife.packagingservice.rws.ConfigExcept;
import com.gennlife.packagingservice.rws.RwsCountUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.UUID;

/**
 * @author liuzhen
 * Created by liuzhen.
 * Date: 2017/12/18
 * Time: 10:55
 */
public class RwsCalcConditionConvertUtil {
    private static Logger logger = LoggerFactory.getLogger(RwsCalcConditionConvertUtil.class);
    public static JsonObject conditionConvert(String param){
        JsonObject baseConfig = new JsonObject();
        try {
            logger.info("param = {}",param);
            JsonArray paramArray = JsonAttrUtil.toJsonArray(param);
            if (JsonAttrUtil.isEmptyJsonElement(paramArray)) {
                logger.error("参数异常,参数必须是合规的json格式");
                throw  new RuntimeException("参数不是json 数据");
            }
            final String uuid = UUID.randomUUID().toString();
            String key = StringUtil.bytesToMD5(param.getBytes());
            LinkedList<JsonObject> count = new LinkedList<>();
            JsonObject filter = null;
            JsonArray filters = new JsonArray();
            String projectId = null;
            for (JsonElement element : paramArray) {
                JsonObject tmp = element.getAsJsonObject();
                String type = JsonAttrUtil.getStringValue(RwsCountUtils.ACTIVE_TYPE_KEY, tmp);
                if (StringUtil.isEmptyStr(type) || Integer.valueOf(type) == RwsCountUtils.FILTER) {
                    filter = tmp;
                } else {
                    count.add(tmp);
                }
                if (tmp != null) {
                    tmp.remove("config");
                }
                if (StringUtil.isEmptyStr(projectId)) {
                    projectId = JsonAttrUtil.getStringValue(RwsConfigTransUtils.PROJECT_ID_KEY, tmp);
                }
            }
            logger.info(uuid + " param " + JsonAttrUtil.toJsonStr(paramArray));
            for (JsonObject elem : count) {
                try {
                    RwsConfigTransUtils.transActiveConfig(elem);
                    logger.info(uuid + ": activeId " + JsonAttrUtil.getStringValue(RwsConfigTransUtils.UNIQUE_ID_KEY, elem.getAsJsonObject()));
                } catch (ConfigExcept configExcept) {
                    logger.error(uuid + " param " + param, configExcept);
                    throw  new RuntimeException(configExcept.getMessage());
                }
            }
            if (count.size() > 0) {
                baseConfig.add(RwsCountUtils.COUNT_CONFIG_KEY, JsonAttrUtil.toJsonTree(count));
            }
            if (!JsonAttrUtil.isEmptyJsonElement(filter)) {
                try {
                    RwsConfigTransUtils.transFilterPatientConfig(filter);
                } catch (ConfigExcept configExcept) {
                    logger.error(uuid + " param " + param, configExcept);
                    throw new RuntimeException(configExcept.getMessage());
                }
                baseConfig.add(RwsCountUtils.FILTER_CONFIG_KEY, filter);
            }
            logger.info(uuid + ": projectId " + projectId);
            baseConfig.addProperty(RwsConfigTransUtils.PROJECT_ID_KEY, projectId);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return baseConfig;
    }
}
