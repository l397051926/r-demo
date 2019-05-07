/**
 * copyRight
 */
package com.gennlife.rws.web;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuzhen.
 * Date: 2017/10/20
 * Time: 9:45
 */
public class WebAPIResult<T> {
    private List<T> data = new ArrayList<T>();
    private int pageNum = 1;
    private int pageSize = 10;
    private int startRow = 0;
    private int endRow;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    private long total;
    private int pages;

    public WebAPIResult(int pageNum, int pageSize) {
        this(pageNum, pageSize, 0);
    }

    public WebAPIResult(int pageNum, int pageSize, int total) {
        this.pageNum = pageNum<=0? 1:pageNum;
        this.pageSize = pageSize<=0? 10:pageSize;
        this.total = (long)total;
        this.startRow = pageNum > 0?(pageNum - 1) * pageSize:0;
        this.endRow = pageNum * pageSize;
        this.pages = total/pageSize+((total%pageSize)==0? 0:1);
    }



    public int getPages() {
        return this.pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getEndRow() {
        return this.endRow;
    }

    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    public int getPageNum() {
        return this.pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getStartRow() {
        return this.startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public long getTotal() {
        return this.total;
    }

    public void setTotal(long total) {
        this.total = total;
        if (this.pageSize > 0) {
            this.pages = (int) (total / (long) this.pageSize + (long) (total % (long) this.pageSize == 0L ? 0 : 1));
        } else {
            this.pages = (int) total;
        }
    }
}
