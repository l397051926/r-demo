package com.gennlife.rws.uql;

import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.schema.AbstractFieldAnalyzer;
import com.gennlife.rws.uqlcondition.UqlWhere;
import com.gennlife.rws.util.StringUtils;

/**
 * @author liumingxin
 * @create 2018 20 17:08
 * @desc
 **/
public class CrfEnumUqlClass extends UqlClass{


    public CrfEnumUqlClass(String from){
        super(from);
    }

    public CrfEnumUqlClass(String from,String crfId){
        super(from,crfId);
    }

    @Override
    public void setInitialPatients(String isVariant, String patientSql) {
        setWhere(getWhereNotNull() + " "+patientSql+" AND ");
    }

    @Override
    public void setWhereIsEmpty(UqlWhere where, String order1, String isVariant, String patientSql, AbstractFieldAnalyzer schema) {
        if (where.isEmpty()) {
            setWhere("visitinfo.DOC_ID  IS NOT NULL");
        } else {
            setWhere("visitinfo.DOC_ID  IS NOT NULL AND ");
            setInitialPatients(isVariant,patientSql);
        }
    }

    @Override
    public void setNotAllWhere(String function, String order1, String indexDate, AbstractFieldAnalyzer schema) {

    }

    @Override
    public String getHavingSql() {
        if(StringUtils.isEmpty(getHaving())){
            return "select " + getSelect() +" from " + getFrom() +" where "+getWhere() +" group by patient_info.patient_basicinfo.DOC_ID ";
        }else {
            return "select " + getSelect() +" from " + getFrom() +" where "+getWhere() +" group by patient_info.patient_basicinfo.DOC_ID " + getHaving();
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
