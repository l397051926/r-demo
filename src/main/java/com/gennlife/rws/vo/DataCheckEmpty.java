/**
 * 
 */
package com.gennlife.rws.vo;

import java.util.List;

import com.gennlife.exception.CustomerException;
import com.gennlife.exception.CustomerStatusEnum;
import com.gennlife.rws.util.StringUtils;

/**
 * @className DataCheckEmpty
 * @author zhengguohui
 * @description
 * @date 2018年7月13日
 */
public class DataCheckEmpty {
	/**
	 * @author zhengguohui
	 * @description 校验数据是否为空,为空则提示参数为空
	 * @date 2018年7月13日
	 * @param dataId
	 *            void
	 */
	public static void dataCheckEmpty(String... args) {
		if (StringUtils.isEmpty(args)) {
			throw new CustomerException(CustomerStatusEnum.PARAMISNULL);
		}
	}

	/**
	 * @author zhengguohui
	 * @description 判断集合List是否为空,空返回false 反之返回true
	 * @date 2018年7月17日
	 * @param list
	 * @return boolean
	 */
	public static <T> boolean listCheckEmpty(List<T> list) {
		if (list != null && !list.isEmpty()) {
			return true;
		} else {
			return false;
		}

	}

}
