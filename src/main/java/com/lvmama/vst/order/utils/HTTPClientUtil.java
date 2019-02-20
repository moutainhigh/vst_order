package com.lvmama.vst.order.utils;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class HTTPClientUtil {
    public static final String METHOD_POST = "POST";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ACCEPT = "Accept";
    
    public static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded;charset=utf-8";
    public static final String CONTENT_TYPE_TEXT_XML = " text/xml; charset=utf-8";
    
    public static final int CONNECTION_TIME_OUT = 10000;
    public static final int CONNECTION_REQUEST_TIME_OUT = 10000;
    public static final int SOCKET_TIME_OUT = 10000;
    private static final String PARAMEN_CODING_UTF8 = "utf-8";
    private static final String PARAMEN_CODING_GBK = "gbk";
    
    /**
     * 以form表单格式提交http请求
     * @param url 请求的url
     * @param requestBody 用&链接的参数组成的字符串
     * @throws Exception 
     * @throws IOException
     */
    public static ResultHttpRequest doPost(final String url, String requestBody) throws Exception {
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        /*
         * Apache HttpClient 4.3以后设置超时在RequestConfig中，此处使用4.5.1版本。 
         */
        RequestConfig requestConfig = null;
        HttpResponse httpResponse = null;
        int statusCode;
        String responseBody = null;
        ResultHttpRequest resultHttpRequest = null;
        try {
            httpClient = HttpClients.createDefault();
            httpPost = new HttpPost(url);
            requestConfig = RequestConfig.custom()
                            .setConnectTimeout(CONNECTION_TIME_OUT)//设置连接超时时间，单位毫秒。
                            .setConnectionRequestTimeout(CONNECTION_REQUEST_TIME_OUT)//设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
                            .setSocketTimeout(SOCKET_TIME_OUT)//请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
                            .build();
            httpPost.setConfig(requestConfig);
            httpPost.setHeader(CONTENT_TYPE, FORM_CONTENT_TYPE);
            httpPost.setHeader(ACCEPT, CONTENT_TYPE_TEXT_XML);
            
            httpPost.setEntity(new StringEntity(requestBody, PARAMEN_CODING_UTF8));
            httpResponse = httpClient.execute(httpPost);
            
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                responseBody = EntityUtils.toString(httpResponse.getEntity());
            }
            
            resultHttpRequest = new ResultHttpRequest();
            resultHttpRequest.setStatusCode(new Integer(statusCode));
            resultHttpRequest.setResponseBody(responseBody);
        } catch (Exception e) {
            throw e;
        } finally {
            if (null != httpClient) {
                httpClient.close();
            }
        }
        return resultHttpRequest;
    }
    
}
