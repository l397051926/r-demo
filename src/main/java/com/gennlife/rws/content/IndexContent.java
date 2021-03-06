package com.gennlife.rws.content;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.util.StringUtils;

import static com.gennlife.rws.query.BuildIndexCrf.PROJECT_INDEX_NAME_PREFIX;


public class IndexContent {
    private static final String EMR_INDXE = "rws_emr_";
    public static final String EMR_CRF_ID = "EMR";

    private static final String EMR_GROUP_BY = " group by patient_info.DOC_ID";
    private static final String CRF_GROUP_BY = " group by patient_info.patient_basicinfo.DOC_ID";

    private static final String EMR_PATIENT_SN = "visit_info.PATIENT_SN";
    private static final String CRF_PATIENT_SN = "visitinfo.PATIENT_SN";

    private static final String EMR_PATIENT_DOC_ID = "patient_info.DOC_ID";
    private static final String CRF_PATIENT_DOC_ID = "patient_info.patient_basicinfo.DOC_ID";

    private static final String EMR_PATIENT_INFO = "patient_info";
    private static final String CRF_PATIENT_INFO = "patient_info.patient_basicinfo";

    private static final String EMR_PATIENT_INFO_PATIENT_SN = "patient_info.PATIENT_SN";
    private static final String CRF_PATIENT_INFO_PATIENT_SN = "patient_info.patient_basicinfo.PATIENT_SN";

    public static String getIndexName(String crfId, String projectId) {
        if (StringUtils.isEmpty(crfId) || EMR_CRF_ID.equals(crfId)) {
            return EMR_INDXE + projectId;
        } else {
            return PROJECT_INDEX_NAME_PREFIX.get(crfId) + projectId;
        }
    }

    public static String getGroupBy(String crfId) {
        if (StringUtils.isEmpty(crfId) || EMR_CRF_ID.equals(crfId)) {
            return EMR_GROUP_BY;
        } else {
            return CRF_GROUP_BY;
        }
    }

    public static String getPatientSn(String crfId) {
        if (StringUtils.isEmpty(crfId) || EMR_CRF_ID.equals(crfId)) {
            return EMR_PATIENT_SN;
        } else {
            return CRF_PATIENT_SN;
        }
    }

    public static String getPatientDocId(String crfId) {
        if (StringUtils.isEmpty(crfId) || EMR_CRF_ID.equals(crfId)) {
            return EMR_PATIENT_DOC_ID;
        } else {
            return CRF_PATIENT_DOC_ID;
        }
    }

    public static String getPatientInfo(String crfId) {
        if (StringUtils.isEmpty(crfId) || EMR_CRF_ID.equals(crfId)) {
            return EMR_PATIENT_INFO;
        } else {
            return CRF_PATIENT_INFO;
        }
    }

    public static String getPatientInfoPatientSn(String crfId) {
        if (StringUtils.isEmpty(crfId) || EMR_CRF_ID.equals(crfId)) {
            return EMR_PATIENT_INFO_PATIENT_SN;
        } else {
            return CRF_PATIENT_INFO_PATIENT_SN;
        }
    }

    public static JSONObject getPatientInfoObj(JSONObject sourceObj, String crfId) {
        if (StringUtils.isEmpty(crfId) || IndexContent.EMR_CRF_ID.equals(crfId)) {
            return sourceObj.getJSONArray("patient_info").getJSONObject(0);
        } else {
            return sourceObj.getJSONArray("patient_info").getJSONObject(0).getJSONArray("patient_basicinfo").getJSONObject(0);
        }
    }
}
