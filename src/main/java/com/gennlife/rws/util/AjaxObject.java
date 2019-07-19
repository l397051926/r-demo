/**
 * copyRight
 */
package com.gennlife.rws.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gennlife.rws.web.WebAPIResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuzhen.
 * Date: 2017/10/19
 * Time: 15:37
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AjaxObject {
    public static final int AJAX_STATUS_SUCCESS = 200;
    /*页面提示类结果*/
    public static final int AJAX_STATUS_TIPS = 300;
    public static final int AJAX_STATUS_FAILURE = 100;
    public static final String AJAX_MESSAGE_SUCCESS = "操作成功";
    public static final String AJAX_MESSAGE_FAILURE = "操作失败";
    private int status = AJAX_STATUS_SUCCESS;
    private String message = AJAX_MESSAGE_SUCCESS;
    private Object data;
    private boolean flag;
    private Object columns;
    private WebAPIResult<Object> webAPIResult;
    //总数量  针对 指标
    private Object count;
    //针对指标结果列表修改的参数
    private Integer modifiable = 1;
    private Object plainOptions;
    //样本提供的条件参数
    private String applyOutCondition;


    public Object getPlainOptions() {
        return plainOptions;
    }

    public void setPlainOptions(Object plainOptions) {
        this.plainOptions = plainOptions;
    }

    public Object getCount() {
        return count;
    }

    public void setCount(Object count) {
        this.count = count;
    }

    public WebAPIResult<Object> getWebAPIResult() {
        return webAPIResult;
    }

    public void setWebAPIResult(WebAPIResult<Object> webAPIResult) {
        this.webAPIResult = webAPIResult;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public AjaxObject() {
    }

    public AjaxObject(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public AjaxObject(int status, String message, int modifiable) {
        this.status = status;
        this.message = message;
        this.modifiable = modifiable;
    }

    public AjaxObject(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public AjaxObject(int status, String message, Object data, boolean flag) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.flag = flag;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getColumns() {
        return columns;
    }

    public void setColumns(Object columns) {
        this.columns = columns;
    }

    public static void getReallyDataValue(JSONArray data, JSONArray showColumns) {
        int size = showColumns.size();
        Map<String, String> showMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            JSONObject obj = showColumns.getJSONObject(i);
            showMap.put(obj.getString("id"), obj.getString("name"));
        }
        for (int i = 0; i < data.size(); i++) {
            JSONObject obj = data.getJSONObject(i);
            for (Map.Entry<String, String> entry : showMap.entrySet()) {
                if (!obj.containsKey(entry.getKey())) {
                    obj.put(entry.getKey(), "-");
                }
            }
        }
    }

    public Integer getModifiable() {
        return modifiable;
    }

    public void setModifiable(Integer modifiable) {
        this.modifiable = modifiable;
    }

    public String getApplyOutCondition() {
        return applyOutCondition;
    }

    public void setApplyOutCondition(String applyOutCondition) {
        this.applyOutCondition = applyOutCondition;
    }
}
