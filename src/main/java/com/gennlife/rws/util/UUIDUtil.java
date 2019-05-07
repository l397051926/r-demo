/**
 * 
 */
package com.gennlife.rws.util;

import java.util.UUID;

import java.util.UUID;

/**
 * @author zhengguohui
 * @className UUIDUtil
 * @description
 * @date 2018年7月11日
 */
public class UUIDUtil {

	public static String getUUID() {
		return UUID.randomUUID().toString().replace("-", "").toLowerCase();
	}

}
