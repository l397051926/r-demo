package com.gennlife.rws.vo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class SearchUqlVisitSn {
    private static final JSONObject CONDITION = JSON.parseObject("{\n" +
            "    \"visit_info\": \"visit_info.VISIT_SN\",\n" +
            "    \"append_info\": \"append_info.VISIT_SN\",\n" +
            "    \"diagnose\": \"diagnose.VISIT_SN\",\n" +
            "    \"medical_record_home_page\": \"medical_record_home_page.VISIT_SN\",\n" +
            "    \"medical_record_home_page.operation\": \"medical_record_home_page.operation.VISIT_SN\",\n" +
            "    \"medical_record_home_page.pathology\": \"medical_record_home_page.pathology.VISIT_SN\",\n" +
            "    \"medical_record_home_page.injury\": \"medical_record_home_page.injury.VISIT_SN\",\n" +
            "    \"medical_record_home_page.admiss_diag\": \"medical_record_home_page.admiss_diag.VISIT_SN\",\n" +
            "    \"medical_record_home_page.dis_main_diag\": \"medical_record_home_page.dis_main_diag.VISIT_SN\",\n" +
            "    \"medical_record_home_page.dis_other_diag\": \"medical_record_home_page.dis_other_diag.VISIT_SN\",\n" +
            "    \"medical_record_home_page.clinic_diag\": \"medical_record_home_page.clinic_diag.VISIT_SN\",\n" +
            "    \"medical_record_home_page.drug_allergy\": \"medical_record_home_page.drug_allergy.VISIT_SN\",\n" +
            "    \"medical_record_home_page.icu\": \"medical_record_home_page.icu.VISIT_SN\",\n" +
            "    \"operation_records\": \"operation_records.VISIT_SN\",\n" +
            "    \"operation_info\": \"operation_info.VISIT_SN\",\n" +
            "    \"operation_pre_summary\": \"operation_pre_summary.VISIT_SN\",\n" +
            "    \"operation_pre_anesthesia_interview_records\": \"operation_pre_anesthesia_interview_records.VISIT_SN\",\n" +
            "    \"operation_pre_conference_records\": \"operation_pre_conference_records.VISIT_SN\",\n" +
            "    \"operation_post_anesthesia_interview_records\": \"operation_post_anesthesia_interview_records.VISIT_SN\",\n" +
            "    \"operation_post_course_records\": \"operation_post_course_records.VISIT_SN\",\n" +
            "    \"medicine_order\": \"medicine_order.VISIT_SN\",\n" +
            "    \"inspection_reports\": \"inspection_reports.VISIT_SN\",\n" +
            "    \"ct_reports\": \"ct_reports.VISIT_SN\",\n" +
            "    \"ect_reports\": \"ect_reports.VISIT_SN\",\n" +
            "    \"xray_image_reports\": \"xray_image_reports.VISIT_SN\",\n" +
            "    \"mr_reports\": \"mr_reports.VISIT_SN\",\n" +
            "    \"microscopic_exam_reports\": \"microscopic_exam_reports.VISIT_SN\",\n" +
            "    \"pet_ct_reports\": \"pet_ct_reports.VISIT_SN\",\n" +
            "    \"pet_mr_reports\": \"pet_mr_reports.VISIT_SN\",\n" +
            "    \"other_imaging_exam_diagnosis_reports\": \"other_imaging_exam_diagnosis_reports.VISIT_SN\",\n" +
            "    \"pathology_reports\": \"pathology_reports.VISIT_SN\",\n" +
            "    \"ultrasonic_diagnosis_reports\": \"ultrasonic_diagnosis_reports.VISIT_SN\",\n" +
            "    \"lung_functional_exam\": \"lung_functional_exam.VISIT_SN\",\n" +
            "    \"electrocardiogram_reports\": \"electrocardiogram_reports.VISIT_SN\",\n" +
            "    \"electrocardiographic_reports\": \"electrocardiographic_reports.VISIT_SN\",\n" +
            "    \"electroencephalogram_reports\": \"electroencephalogram_reports.VISIT_SN\",\n" +
            "    \"immunohistochemical\": \"immunohistochemical.VISIT_SN\",\n" +
            "    \"forsz_study\": \"forsz_study.VISIT_SN\",\n" +
            "    \"forsz_visit\": \"forsz_visit.VISIT_SN\",\n" +
            "    \"bone_marrow_blood_tests_reports\": \"bone_marrow_blood_tests_reports.VISIT_SN\",\n" +
            "    \"clinic_medical_records\": \"clinic_medical_records.VISIT_SN\",\n" +
            "    \"admissions_records\": \"admissions_records.VISIT_SN\",\n" +
            "    \"discharge_records\": \"discharge_records.VISIT_SN\",\n" +
            "    \"discharge_summary\": \"discharge_summary.VISIT_SN\",\n" +
            "    \"first_course_records\": \"first_course_records.VISIT_SN\",\n" +
            "    \"attending_physician_rounds_records\": \"attending_physician_rounds_records.VISIT_SN\",\n" +
            "    \"course_records\": \"course_records.VISIT_SN\",\n" +
            "    \"consultation_opinion_records\": \"consultation_opinion_records.VISIT_SN\",\n" +
            "    \"death_discuss_records\": \"death_discuss_records.VISIT_SN\",\n" +
            "    \"death_records\": \"death_records.VISIT_SN\",\n" +
            "    \"death_summary\": \"death_summary.VISIT_SN\",\n" +
            "    \"difficulty_case_records\": \"difficulty_case_records.VISIT_SN\",\n" +
            "    \"handover_record\": \"handover_record.VISIT_SN\",\n" +
            "    \"rescue_records\": \"rescue_records.VISIT_SN\",\n" +
            "    \"stage_summary\": \"stage_summary.VISIT_SN\",\n" +
            "    \"transferred_in_records\": \"transferred_in_records.VISIT_SN\",\n" +
            "    \"transferred_out_records\": \"transferred_out_records.VISIT_SN\",\n" +
            "    \"nurse_operation\": \"nurse_operation.VISIT_SN\",\n" +
            "    \"icu_record\": \"icu_record.VISIT_SN\",\n" +
            "    \"pathology_specimen_information\": \"pathology_specimen_information.VISIT_SN\",\n" +
            "    \"transfusion_exam_results\": \"transfusion_exam_results.VISIT_SN\",\n" +
            "    \"transfusion_records\": \"transfusion_records.VISIT_SN\",\n" +
            "    \"transfusion_special_requirements\": \"transfusion_special_requirements.VISIT_SN\",\n" +
            "    \"triple_test_table\": \"triple_test_table.VISIT_SN\",\n" +
            "    \"operation_nursing_record\": \"operation_nursing_record.VISIT_SN\",\n" +
            "    \"pain_nursing_record\": \"pain_nursing_record.VISIT_SN\",\n" +
            "    \"general_nursing_record\": \"general_nursing_record.VISIT_SN\",\n" +
            "    \"orders\": \"orders.VISIT_SN\",\n" +
            "    \"examination_request\": \"examination_request.VISIT_SN\",\n" +
            "    \"inspect_request\": \"inspect_request.VISIT_SN\",\n" +
            "    \"pathology_request\": \"pathology_request.VISIT_SN\",\n" +
            "    \"operation_request\": \"operation_request.VISIT_SN\",\n" +
            "    \"transfusion_request\": \"transfusion_request.VISIT_SN\",\n" +
            "    \"chinese_medicine_prescription\": \"chinese_medicine_prescription.VISIT_SN\",\n" +
            "    \"order_implemet_records\": \"order_implemet_records.VISIT_SN\",\n" +
            "    \"invasive_records\": \"invasive_records.VISIT_SN\",\n" +
            "    \"consultation_request_records\": \"consultation_request_records.VISIT_SN\",\n" +
            "    \"fee\": \"fee.VISIT_SN\"\n" +
            "}");

    public static String getSearchUqlVisitSn(String key){
        return CONDITION.getString(key);
    }
}
