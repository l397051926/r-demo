/**
 * copyRight
 */
package com.gennlife.rws;

/**
 * Created by liuzhen.
 * Date: 2017/10/19
 * Time: 14:54
 */
public class BusinessException extends RuntimeException {

    public BusinessException(){
        super();
    }
    public BusinessException(String message){
        super(message);
    }
}
