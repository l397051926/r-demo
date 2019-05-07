package com.gennlife.rws.uqlcondition;

/**
 * @author liumingxin
 * @create 2018 11 18:18
 * @desc
 **/
public class HighGradeUqlWhereElem extends UqlWhereElem {
    private String str;

    public HighGradeUqlWhereElem(String str) {
        super(null);
        this.str = str;
    }


    @Override
    public void execute() {
        if(str.startsWith("patient_info")){
            result = str;
        }else {
            result = "haschild("+str+")";
        }
    }
}
