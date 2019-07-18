package com.gennlife.rws.uql;

import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.schema.AbstractFieldAnalyzer;
import com.gennlife.rws.uqlcondition.UqlWhere;
import com.gennlife.rws.util.StringUtils;

/**
 * @author liumingxin
 * @create 2018 18 21:07
 * @desc
 **/
public class EnumeUqlClass extends UqlClass {

    public EnumeUqlClass(String from){
        super(from);
    }

    public EnumeUqlClass(String from,String crfId){
        super(from,crfId);
    }

    @Override
    public void setInitialPatients(String isVariant, String patientSql) {
        setWhere(getWhereNotNull() + " "+patientSql+" AND ");
    }

    @Override
    public void setWhereIsEmpty(UqlWhere where, String order1, String isVariant, String patientSql, AbstractFieldAnalyzer schema) {
        if (where.isEmpty()) {
            setWhere("visit_info.DOC_ID  IS NOT NULL");
        } else {
            setWhere("visit_info.DOC_ID  IS NOT NULL AND ");
            setInitialPatients(isVariant,patientSql);
        }
    }

    @Override
    public void setNotAllWhere(String function, String order1, String indexDate, AbstractFieldAnalyzer schema) {

    }

    @Override
    public String getHavingSql() {
        if(StringUtils.isEmpty(getHaving())){
            return "select " + getSelect() +" from " + getFrom() +" where "+getWhere() +" group by patient_info.DOC_ID ";
        }else {
            return "select " + getSelect() +" from " + getFrom() +" where "+getWhere() +" group by patient_info.DOC_ID " + getHaving();
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
