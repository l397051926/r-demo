package com.gennlife.rws.query;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import static com.gennlife.rws.query.BuildIndexCrf.PROJECT_INDEX_NAME_PREFIX;


public class BuildIndexQuery {
    private String  hospitalId;
    private String indexName;
    private JSONArray data;
    private String fromIndexName;

    public BuildIndexQuery(){}

    public BuildIndexQuery(String indexName) {
        this.hospitalId="public";
        this.indexName = "rws_emr_"+indexName;
        this.data = new JSONArray();
    }

    public BuildIndexQuery(String indexName, String crfId) {
        this.hospitalId="public";
        this.indexName = PROJECT_INDEX_NAME_PREFIX.get(crfId) + indexName;
        this.data = new JSONArray();
    }

    public String getFromIndexName() {
        return fromIndexName;
    }

    public void setFromIndexName(String fromIndexName) {
        this.fromIndexName = fromIndexName;
    }

    public Integer disposeData(JSONObject json){
        JSONArray hits = json.getJSONObject("hits").getJSONArray("hits");
        Integer size = hits==null?0:hits.size();
        for (int i = 0; i < size; i++) {
            JSONObject object = new JSONObject();
            JSONObject hitsData = hits.getJSONObject(i);
            String patientSn = hitsData.getString("_id");
            JSONObject data = hitsData.getJSONObject("_source");
            object.put("patient_sn",patientSn);
            object.put("data",data);
            this.data.add(object);
        }
        return size;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public JSONArray getData() {
        return data;
    }

    public void setData(JSONArray data) {
        this.data = data;
    }


}
