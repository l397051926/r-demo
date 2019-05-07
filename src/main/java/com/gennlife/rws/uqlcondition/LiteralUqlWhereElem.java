package com.gennlife.rws.uqlcondition;

/**
 * @author liumingxin
 * @create 2018 01 16:58
 * @desc
 **/
public class LiteralUqlWhereElem extends UqlWhereElem {

    private String str;

    public LiteralUqlWhereElem(String str) {
        super(null);
        this.str = str;
    }

    public String value() {
        return str;
    }

    @Override
    public void execute() {
        result = str;
    }

}
