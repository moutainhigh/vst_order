package com.lvmama.vst.order.utils;

public class ResultHttpRequest {
    private Integer statusCode;
    private String responseBody;
    
    public Integer getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
    public String getResponseBody() {
        return responseBody;
    }
    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
    @Override
    public String toString() {
        return "ResultHttpRequest [statusCode=" + statusCode
                + ", responseBody=" + responseBody + "]";
    }
    
}
