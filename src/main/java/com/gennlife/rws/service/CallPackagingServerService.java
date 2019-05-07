package com.gennlife.rws.service;

import com.gennlife.rws.entity.ActiveIndex;
import com.gennlife.rws.util.AjaxObject;

import java.util.List;

/**
 * Created by liuzhen.
 * Date: 2017/10/26
 * Time: 11:08
 */
public interface CallPackagingServerService {
    public AjaxObject callPackagingService(ActiveIndex data, List<String> activeIds, String taskId, Integer isSearch);
    public void clearData(ActiveIndex data, Integer isSearch, String projectId, String tableName);
}
