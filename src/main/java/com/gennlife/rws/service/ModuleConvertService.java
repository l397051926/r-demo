/**
 * copyRight
 */
package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;

/**
 * @author liuzhen
 * Created by liuzhen.
 * Date: 2018/7/10
 * Time: 14:32
 */
public interface ModuleConvertService {
    /**
     * 前端参数转后端格式
     * @param source
     * @return
     */
    public JSONObject uiToRws(JSONObject source);

    /**
     * 后端格式转前端格式
     * @param source
     * @return
     */
    public JSONObject rwsToUi(JSONObject source);

    /**
     * 前端格式转统一查询平台格式
     * @param source
     * @return
     */
    public JSONObject uiToSearch(JSONObject source);


    JSONArray enumFormatToUi(JSONArray newWebConfig);

    JSONArray enumFormat(JSONArray configss, Boolean enumEmity);
}
