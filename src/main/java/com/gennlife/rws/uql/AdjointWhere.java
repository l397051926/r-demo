package com.gennlife.rws.uql;

/**
 * @author liumingxin
 * @create 2018 05 18:46
 * @desc
 **/
public class AdjointWhere {
    String adjointName ;
    Boolean isHasChild = false;
    Boolean isHasParent = false;
    String operatorSign ;
    String refActiveId  ;

    public AdjointWhere(String adjointName, String operatorSign,String refActiveId) {
        this.adjointName = adjointName;
        this.operatorSign = operatorSign;
        this.refActiveId = refActiveId;
    }

    public AdjointWhere(String adjointName, Boolean isHasChild, Boolean isHasParent) {
        this.adjointName = adjointName;
        this.isHasChild = isHasChild;
        this.isHasParent = isHasParent;
    }

    public String getAdjointName() {
        return adjointName;
    }

    public void setAdjointName(String adjointName) {
        this.adjointName = adjointName;
    }

    public Boolean getHasChild() {
        return isHasChild;
    }

    public void setHasChild(Boolean hasChild) {
        isHasChild = hasChild;
    }

    public Boolean getHasParent() {
        return isHasParent;
    }

    public void setHasParent(Boolean hasParent) {
        isHasParent = hasParent;
    }

    public String getOperatorSign() {
        return operatorSign;
    }

    public void setOperatorSign(String operatorSign) {
        this.operatorSign = operatorSign;
    }

    public String getRefActiveId() {
        return refActiveId;
    }

    public void setRefActiveId(String refActiveId) {
        this.refActiveId = refActiveId;
    }
}
