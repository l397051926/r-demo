package com.gennlife.rws.service;

import com.gennlife.rws.util.AjaxObject;

/**
 * Created by liuzhen.
 * Date: 2017/11/1
 * Time: 16:30
 */
public interface RetryService {
    public AjaxObject callPackagingeForCalc(String conditions);
    public void recover(Exception e,String conditions);
    public void recover(RuntimeException e,String conditions);
}
