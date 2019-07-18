package com.gennlife.rws.uql;

import com.gennlife.darren.collection.keypath.KeyPath;
import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.schema.AbstractFieldAnalyzer;
import com.gennlife.rws.uqlcondition.UqlWhere;
import com.gennlife.rws.util.StringUtils;

/**
 * @author liumingxin
 * @create 2018 20 16:31
 * @desc
 **/
public class CrfActiveUqlClass extends UqlClass {

    public CrfActiveUqlClass(String from){
        super(from);
    }

    public CrfActiveUqlClass(String from,String crfId){
        super(from,crfId);
    }

    public void setVistSnWhere(UqlWhere where,String visits,String order1, AbstractFieldAnalyzer schema){
        if (where.isEmpty()) {
            if (schema.isPackagedField(order1)) {
                setWhere("haschild( " + visits + ".VISIT_SN IS NOT NULL ) AND ");
            } else {
                setWhere(visits + ".VISIT_SN IS NOT NULL AND ");
            }
        } else {
            if (schema.isPackagedField(order1)) {
                setWhere("haschild( " + visits + ".VISIT_SN IS NOT NULL ) AND ");
            } else {
                setWhere(visits + ".VISIT_SN IS NOT NULL AND ");
            }
        }
    }

    @Override
    public void setNotAllWhere(String function, String order1, String indexDate, AbstractFieldAnalyzer schema) {
        if (!"all".equals(function) ) {
            if (order1.startsWith("visitinfo")) {
                setWhere(getNotEmptyWhere()+order1 + " IS NOT NULL AND ");
            } else {
                setWhere(getNotEmptyWhere()+" haschild( " + order1 + " IS NOT NULL ) AND ");
            }
        }
    }
    @Override
    public void setWhereIsEmpty(UqlWhere where, String order1, String isVariant, String patientSql, AbstractFieldAnalyzer schema) {
        if (where.isEmpty()) {
            if (schema.isPackagedField(order1)) {
                setIsEmptyWhere("haschild( " + (KeyPath.compile(order1).removeLast(2).toString() + " IS NOT NULL ) "));
            } else {
                setIsEmptyWhere(order1 + " IS NOT NULL ");
            }
        } else {
            if (schema.isPackagedField(order1)) {
                setIsEmptyWhere("haschild( " + (KeyPath.compile(order1).removeLast(2).toString() + " IS NOT NULL ) AND "));
            } else {
                setIsEmptyWhere(order1 + " IS NOT NULL AND ");
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
            return "select " + getSelect() +" from " + getFrom() +" where " + getWhere() +" group by patient_info.patient_basicinfo.DOC_ID ";
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
