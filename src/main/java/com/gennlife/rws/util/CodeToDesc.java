/**
 * copyRight
 */
package com.gennlife.rws.util;

import com.gennlife.rws.content.CommonContent;

/**
 * 代码转中文描述
 * Created by liuzhen.
 * Date: 2017/10/20
 * Time: 11:34
 */
public class CodeToDesc {

    public static String activeTypeToDesc(Integer activeType) {
        if (CommonContent.ACTIVE_TYPE_EVENT.intValue() == activeType.intValue()) {
            return CommonContent.ACTIVE_TYPE_EVENT_DESC;
        } else if (CommonContent.ACTIVE_TYPE_INDEX.intValue() == activeType.intValue()) {
            return CommonContent.ACTIVE_TYPE_INDEX_DESC;
        } else {
            return CommonContent.ACTIVE_TYPE_INOUTN_DESC;
        }
    }
}
