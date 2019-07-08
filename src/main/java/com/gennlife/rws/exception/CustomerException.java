package com.gennlife.rws.exception;

import com.gennlife.rws.vo.CustomerStatusEnum;

/**
 * @author lmx
 * @create 2019 08 13:42
 * @desc
 **/
public class CustomerException extends RuntimeException {
    private String code;
    private String message;

    public CustomerException() {
    }

    public CustomerException(CustomerStatusEnum exceptionEnum) {
        this.message = exceptionEnum.getMessage();
        this.code = exceptionEnum.getCode().toString();
    }

    public CustomerException(String code, String message) {
        this.message = message;
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}