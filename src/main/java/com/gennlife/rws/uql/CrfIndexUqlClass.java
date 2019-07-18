package com.gennlife.rws.uql;

import com.gennlife.darren.collection.keypath.KeyPath;
import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.schema.AbstractFieldAnalyzer;
import com.gennlife.rws.uqlcondition.UqlWhere;
import com.gennlife.rws.util.StringUtils;
import com.gennlife.rws.util.TransData;

/**
 * @author liumingxin
 * @create 2018 20 14:37
 * @desc
 **/
public class CrfIndexUqlClass extends UqlClass {

    public CrfIndexUqlClass(String from){
        super(from);
    }

    public CrfIndexUqlClass(String from,String crfId){
        super(from,crfId);
    }

    @Override
    public void setWhereIsEmpty(UqlWhere where, String order1, String isVariant, String patientSql, AbstractFieldAnalyzer schema) {
        if (where.isEmpty()) {
            if (schema.isPackagedField(order1)) {
                setWhere("haschild( " + (TransData.transDataNumber(KeyPath.compile(order1).removeLast(2).toString() )+ " IS NOT NULL ) "));
            } else {
                setWhere(TransData.transDataNumber(order1) + " IS NOT NULL ");
            }
        } else {
            if (schema.isPackagedField(order1)) {
                setWhere("haschild( " + (TransData.transDataNumber(KeyPath.compile(order1).removeLast(2).toString()) + " IS NOT NULL ) AND "));
            } else {
                setWhere(TransData.transDataNumber(order1) + " IS NOT NULL AND ");
            }
        }
    }

    @Override
    public void setNotAllWhere(String function, String order1, String indexDate, AbstractFieldAnalyzer schema) {
        if ("first".equals(function) || "last".equals(function)  || "index".equals(function) || "reverseindex".equals(function)  ) {
            if (schema.isPackagedField(indexDate)) {
                setWhere(getWhere()+"haschild( " + (TransData.transDataNumber(KeyPath.compile(indexDate).removeLast(2).toString()) + " IS NOT NULL ) AND "));
            } else {
                setWhere(getWhere()+TransData.transDataNumber(indexDate) + " IS NOT NULL AND " );
            }
        }
    }

    @Override
    public void setInitialPatients(String isVariant, String patientSql) {
        setWhere(getWhereNotNull() + patientSql+" AND ");
    }

    @Override
    public String getHavingSql(){
        if(StringUtils.isEmpty(getHaving())){
            return "select " + getSelect() +" from " + getFrom() +" where " + getWhere() +" group by patient_info.patient_basicinfo.DOC_ID";
        }else {
            return "select " + getSelect() +" from " + getFrom() +" where " + getWhere() +" group by patient_info.patient_basicinfo.DOC_ID " + getHaving();
        }
    }
    public String getHavingSql(String crfId) {
        if(StringUtils.isEmpty(getHaving())){
            return "select " + getSelect() +" from " + getFrom() +" where "+getWhere() + IndexContent.getGroupBy(crfId);
        }else{
            return "select " + getSelect() +" from " + getFrom() +" where "+getWhere() + IndexContent.getGroupBy(crfId) + getHaving();
        }
    }
}
