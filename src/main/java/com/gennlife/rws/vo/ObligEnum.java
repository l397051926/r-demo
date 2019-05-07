/**
 * 
 */
package com.gennlife.rws.vo;

/**
 * @className ObligEnum
 * @author zhengguohui
 * @description
 * @date 2018年8月28日
 */
public enum ObligEnum {

	/** 创建人 */
	CREATER("001", "创建人"),
	/** 负责人 */
	CHARGE("002", "负责人"),
	/** 参与人 */
	PARTICIPANTS("003", "参与人");

	private String index;

	private String name;

	private ObligEnum(String index, String name) {
		this.index = index;
		this.setName(name);
	}

	@Override
	public String toString() {
		return this.index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
