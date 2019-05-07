package com.gennlife.rws.uql;

import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.schema.AbstractFieldAnalyzer;
import com.gennlife.rws.uqlcondition.UqlWhere;
import com.gennlife.rws.util.StringUtils;

/**
 * @author liumingxin
 * @create 2018 19 9:56
 * @desc
 **/
public class IndexUqlClass extends UqlClass {

    public IndexUqlClass(String from){
        super(from);
    }

    public IndexUqlClass(String from,String crfId){
        super(from,crfId);
    }

    @Override
    public void setWhereIsEmpty(UqlWhere where, String order1, String isVariant, String patientSql, AbstractFieldAnalyzer schema) {
        if (where.isEmpty()) {
            if (order1.startsWith("visit_info")) {
                setWhere(order1 + " IS NOT NULL");
            } else {
                setWhere("haschild( " + order1 + " IS NOT NULL )");
            }
        } else {
            if (order1.startsWith("visit_info")) {
                setWhere(order1 + " IS NOT NULL AND ");
            } else {
                setWhere("haschild( " + order1 + " IS NOT NULL ) AND ");
            }
        }
    }

    @Override
    public void setNotAllWhere(String function, String order1, String indexDate, AbstractFieldAnalyzer schema) {
        if ("first".equals(function) || "last".equals(function)  || "index".equals(function) || "reverseindex".equals(function)  ) {
            if (order1.startsWith("visit_info")) {
                setWhere(getWhere()+indexDate + " IS NOT NULL AND " );
            } else {
                setWhere(getWhere()+"haschild( " + indexDate + " IS NOT NULL ) AND ");
            }
        }
    }

    @Override
    public String getHavingSql() {
        if(StringUtils.isEmpty(getHaving())){
            return "select " + getSelect() +" from " + getFrom() +" where "+getWhere() +" group by patient_info.DOC_ID ";
        }else {
            return "select " + getSelect() +" from " + getFrom() +" where "+getWhere() +" group by patient_info.DOC_ID " + getHaving();
        }
    }

    @Override
    public void setInitialPatients(String isVariant, String patientSql) {
        if(!IS_VARIANT_TRUE.equals(isVariant)){
            setWhere(getWhereNotNull() + patientSql+" AND ");
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
