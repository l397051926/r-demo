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

	FORMATJSONERROR("9000", "数据不是JSON格式,无法解析!"),

	SYSTEMEXCEPTION("0005", "参数为空");

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
