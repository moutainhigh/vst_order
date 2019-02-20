package com.lvmama.vst.neworder.order.exception;

/**
 * Created by dengcheng on 17/2/20.
 */
public class ErrorMessageException extends  RuntimeException{

    private String message;

    private String errorCode;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorMessageException(String message, String... paras){
        this.message = String.format(message, paras);
    }

//    public ErrorMessageException(String message, String errorCode,String... paras){
//        this.message = String.format(message, paras);
//        this.errorCode = errorCode;
//    }

    public ErrorMessageException(String message){
        this.message = message;
    }

    public ErrorMessageException(String message,String errorCode){
        this.message = message;
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
