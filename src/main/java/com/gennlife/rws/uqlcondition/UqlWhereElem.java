package com.gennlife.rws.uqlcondition;

/**
 * @author liumingxin
 * @create 2018 01 16:53
 * @desc
 **/
public abstract class UqlWhereElem {
    protected String result = null;
    private String operator;
    private StackTraceElement position[];

    public UqlWhereElem(String operator) {
        this.operator = operator;
        position = Thread.currentThread().getStackTrace();
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    abstract public void execute();

    @Override
    public String toString() {
        if (result == null) {
            execute();
        }
        return result;
    }
}