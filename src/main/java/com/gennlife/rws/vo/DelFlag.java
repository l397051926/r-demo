/**
 * 
 */
package com.gennlife.rws.vo;

public enum DelFlag {
	/**有效*/
	AVIABLE("0", "有效"),
	/**无效*/
	LOSE("1", "无效");

	private String index;

	private String name;

	private DelFlag(String index, String name) {
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
