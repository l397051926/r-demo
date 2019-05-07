package com.gennlife.rws.uqlcondition;

import com.gennlife.rws.util.TransData;

/**
 * @author liumingxin
 * @create 2018 01 17:55
 * @desc
 **/
public class SimpleConditionUqlWhereElem extends UqlWhereElem {

    private String str;

    private String jsonType;
    private String sourceTagName;
    private String value;
    private String condition;


    public SimpleConditionUqlWhereElem(String str, String operator) {
        super(operator);
        this.str = str;
    }

    public SimpleConditionUqlWhereElem(String str, String operator, String jsonType, String sourceTagName,String condition, String value){
        super(operator);
        this.str = str;
        this.jsonType = jsonType;
        this.sourceTagName = sourceTagName;
        this.condition = condition;
        this.value = value;
    }
    public void updateStr(String newValue){
        value =  value + "," + newValue;
        str = sourceTagName + " " + condition + " " + TransData.transDataNumber(value) ;
    }
    public void updateResult(String newValue){
        value = value + "," + newValue;
        result = sourceTagName + " " + condition + " " + value ;
    }

    @Override
    public void execute() {
        result = str;
    }

    public String getJsonType() {
        return jsonType;
    }

    public void setJsonType(String jsonType) {
        this.jsonType = jsonType;
    }

    public String getSourceTagName() {
        return sourceTagName;
    }

    public void setSourceTagName(String sourceTagName) {
        this.sourceTagName = sourceTagName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
