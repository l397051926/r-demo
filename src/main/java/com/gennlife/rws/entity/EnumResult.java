package com.gennlife.rws.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @create 2018 12 17:42
 * @desc
 **/
public class EnumResult {
    private List<String> values ;

    public EnumResult() {
        this.values = new ArrayList<>();
    }

    public void add(String val){
        values.add(val);
    }

    public Boolean contain(String val){
        return values.contains(val);
    }

    @Override
    public String toString() {
        return String.join(" ; ",values);
    }

    public void repleaceAdd(String val) {
        values.clear();
        values.add(val);
    }
}
