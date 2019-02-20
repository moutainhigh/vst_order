package com.lvmama.vst.order.vo;

/**
 * Created by zhouyanqun on 2016/5/16.
 * 保存返回信息
 */
public class TravellerOperateInfoCheckResult {
    /**
     *是否成功
     * */
    private Boolean successful;

    /**
     * 信息StringBuffer，用来追加信息
     *
     * */
    private StringBuffer msgStringBuffer;

    /**
     * 错误码
     * */
    private Integer errorCode;

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getMessage() {
        if(msgStringBuffer == null){
            return null;
        }
        return msgStringBuffer.toString();
    }

    public void appendMessage(String appendedMsg){
        if(msgStringBuffer == null){
            msgStringBuffer = new StringBuffer();
        }
        msgStringBuffer.append(appendedMsg).append(";");
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public TravellerOperateInfoCheckResult() {
        msgStringBuffer = new StringBuffer();
        successful = Boolean.FALSE;
    }

    public TravellerOperateInfoCheckResult(boolean successful, String message, Integer errorCode) {
        this();
        this.successful = successful;
        this.msgStringBuffer.append(message);
        this.errorCode = errorCode;
    }

    public TravellerOperateInfoCheckResult appendInfo(Boolean successful, Integer errorCode, String message){
        this.successful = successful;
        this.appendMessage(message);
        this.errorCode = errorCode;
        return this;
    }
}
