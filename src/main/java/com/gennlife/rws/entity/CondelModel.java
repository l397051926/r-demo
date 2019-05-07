package com.gennlife.rws.entity;

import com.gennlife.rws.uqlcondition.UqlWhereElem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author liumingxin
 * @create 2018 18 21:23
 * @desc
 **/
public class CondelModel {
//    private List<Elem> elems;
//    private String operatorSign;
//    private List<CondelModel> strongRefModel;
//    private Integer bracket;
//    private String condelSql;
//
//    public CondelModel() {
//        elems = new ArrayList<>();
//        strongRefModel = new ArrayList<>();
//    }
//
//    public CondelModel(Integer bracket) {
//        this.bracket = bracket;
//        elems = new ArrayList<>();
//        strongRefModel = new ArrayList<>();
//    }
//
//    public CondelModel(Integer bracket, String operatorSign) {
//        elems = new ArrayList<>();
//        strongRefModel = new ArrayList<>();
//        this.bracket = bracket;
//        this.operatorSign = operatorSign;
//    }
//
//    public String getCondetions() {
//        StringBuffer stringBuffer = new StringBuffer();
//
//        for (int i = 0; i < elems.size(); i++) {
//            String val = elems.get(i);
//            if (!val.startsWith("visit_info")) stringBuffer.append("haschild( ");
//            stringBuffer.append(val);
//
//            if (!val.startsWith("visit_info")) stringBuffer.append(")");
//
//            if (i < elems.size() - 1) {
//                stringBuffer.append(" ");
//                stringBuffer.append(operatorSign);
//                stringBuffer.append(" ");
//            }
//
//        }
//        for (CondelModel condelModel : strongRefModel) {
//            if (stringBuffer.length() > 0) {
//                stringBuffer.append(" ");
//                stringBuffer.append(operatorSign);
//                stringBuffer.append(" ");
//            }
//            stringBuffer.append(getStrongRefConditions(condelModel));
//
//        }
//
//        return stringBuffer.toString();
//    }
//
//    public String getStrongRefConditions(CondelModel condelModel) {
//        StringBuffer stringBuffer = new StringBuffer();
//        if (!condelModel.elems.get(0).startsWith("visit_info")) stringBuffer.append("haschild( ");
//        for (int i = 0; i < condelModel.elems.size(); i++) {
//            stringBuffer.append(condelModel.elems.get(i));
//            if (i < condelModel.elems.size() - 1) {
//                stringBuffer.append(" ");
//                stringBuffer.append(operatorSign);
//                stringBuffer.append(" ");
//            }
//        }
//        if (!condelModel.elems.get(0).startsWith("visit_info")) stringBuffer.append(")");
//
//        return stringBuffer.toString();
//    }
//
//    public String getStrongRefConditionsSql(CondelModel condelModel) {
//        StringBuffer stringBuffer = new StringBuffer();
//        for (int i = 0; i < condelModel.elems.size(); i++) {
//            stringBuffer.append(condelModel.elems.get(i));
//            if (i < condelModel.elems.size() - 1) {
//                stringBuffer.append(" ");
//                stringBuffer.append(operatorSign);
//                stringBuffer.append(" ");
//            }
//        }
//        return stringBuffer.toString();
//    }
//
//    public Integer getBracket() {
//        return bracket;
//    }
//
//    public void setBracket(Integer bracket) {
//        this.bracket = bracket;
//    }
//
//    public String getHasChild(String str) {
//        return "haschild(" + str + ")";
//    }
//
//
//    public List<CondelModel> getStrongRefModel() {
//        return strongRefModel;
//    }
//
//    public void setStrongRefModel(List<CondelModel> strongRefModel) {
//        this.strongRefModel = strongRefModel;
//    }
//
//    public List<String> getElems() {
//        return elems;
//    }
//
//    public void setElems(List<String> elems) {
//        this.elems = elems;
//    }
//
//    public String getOperatorSign() {
//        return operatorSign;
//    }
//
//    public void setOperatorSign(String operatorSign) {
//        this.operatorSign = operatorSign;
//    }
//
//    public String getCondelSql() {
//        return condelSql;
//    }
//
//    public void setCondelSql(String condelSql) {
//        this.condelSql = condelSql;
//    }

}
