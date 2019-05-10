package com.gennlife.rws.exception;

/**
 * @author lmx
 * @create 2019 10 14:43
 * @desc
 **/
public class SaveGroupAndPatientException extends RuntimeException {
    private String message;
    private String code;

    public SaveGroupAndPatientException() {
    }

    public SaveGroupAndPatientException(String code,String message) {
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
