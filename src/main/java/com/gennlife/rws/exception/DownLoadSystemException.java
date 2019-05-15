package com.gennlife.rws.exception;

/**
 * @author lmx
 * @create 2019 15 15:56
 * @desc
 **/
public class DownLoadSystemException extends RuntimeException {
    private String message;
    private String code;

    public DownLoadSystemException() {
    }

    public DownLoadSystemException(String code,String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
