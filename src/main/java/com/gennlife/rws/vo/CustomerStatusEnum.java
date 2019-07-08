/**
 * copyRight
 */
package com.gennlife.rws.vo;

/**
 * @author liuzhen Created by liuzhen. Date: 2018/6/23 Time: 19:01
 */
public enum CustomerStatusEnum {

	UNKONW_ERROR("0000", "未知错误"),
	SUCCESS("0001", "成功"),
	HTTPEXCEPTION("0002", "成功"),
	SYSTEMEXCEPTION("500", "系统异常"),
	SHARDINGDATANOTUNIQUE("0003", "根据inpatient_sn和patient_id获取到对应病人的patient_sn不唯一或者未获取到patient_sn,请调整参数"),
	SHARDINGDATANOTUNIFORMITY("0004", "根据inpatient_sn和patient_id获取的patient_sn于参数中的patient_sn不一致"),
	PARAMISNULL("0005", "参数为空"),
	AUTHENTICATIONEXCEPTION("0006", "权限不足，不能获取参数中任何组的数据"),
	COMPRESSEXCEPTION("0007", "解压缩异常"),
	SHARDINGPARAMMUSTERNOTALLEMPTY("0008", "inpatient_sn、patient_id和patient_sn不能同时为空"),
	FORMATJSONERROR("009","json解析异常");


	private String code;
	private String message;

	CustomerStatusEnum(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public Integer getCode() {
		return Integer.valueOf(code);
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
