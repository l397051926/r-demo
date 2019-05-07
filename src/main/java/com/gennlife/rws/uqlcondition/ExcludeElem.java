package com.gennlife.rws.uqlcondition;

/**
 * @author liumingxin
 * @create 2018 08 8:38
 * @desc
 **/
public class ExcludeElem {
    String s;
    Integer type; // 1是 括号 and or  2.是基本属性  3 是引入属性 4 是未知

    public ExcludeElem(String s, Integer type) {
        this.s = s;
        this.type = type;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return s;
    }
}
