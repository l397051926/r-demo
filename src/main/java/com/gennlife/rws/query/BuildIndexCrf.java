package com.gennlife.rws.query;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.gennlife.darren.controlflow.exception.Force.force;


public class BuildIndexCrf {
    private String  hospitalId;
    private String indexName;
    private JSONArray data;

    public static Map<String, String> PROJECT_INDEX_NAME_PREFIX = force(() -> {
        Map<String, String> ret= new HashMap<>();
        ret.put("lymphoma_release_1.0", "rws_crf_lymphadenoma_");
        ret.put("lymphoma", "rws_crf_lymphadenoma_");
        return ret;
    });

    public BuildIndexCrf(){}

    public BuildIndexCrf(String indexName, String crfId) {
        this.hospitalId="public";
        this.indexName = PROJECT_INDEX_NAME_PREFIX.get(crfId) + indexName;
        this.data = new JSONArray();
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
