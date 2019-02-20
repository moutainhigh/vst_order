package com.lvmama.vst.order.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

public class RestClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);
	
	private static RestTemplate restTemplate;
	
	static {
		// set timeToLive to 30 seconds
		PoolingHttpClientConnectionManager pollingConnectionManager = new PoolingHttpClientConnectionManager(30, TimeUnit.SECONDS);
		// 连接池最大连接数
		pollingConnectionManager.setMaxTotal(500);
		//根据连接到的主机对MaxTotal的一个细分
		pollingConnectionManager.setDefaultMaxPerRoute(500);
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(10 * 1000)
				.setSocketTimeout(10 * 1000)
				.setConnectionRequestTimeout(10 * 1000)
				.build();
		HttpClientBuilder httpClientBuilder = HttpClients.custom();		
		httpClientBuilder.setConnectionManager(pollingConnectionManager);
		//set retry count to 2 
		httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(2, true));
		// add Keep-Alive
		httpClientBuilder.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE);
		httpClientBuilder.setDefaultHeaders(getHttpHeaders());
		httpClientBuilder.setDefaultRequestConfig(config);
		HttpClient httpClient = httpClientBuilder.build();
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
//		clientHttpRequestFactory.setConnectTimeout(5000);
//		clientHttpRequestFactory.setReadTimeout(5000);		
//		clientHttpRequestFactory.setConnectionRequestTimeout(200);
//		clientHttpRequestFactory.setBufferRequestBody(true);
		
		restTemplate = new RestTemplate(clientHttpRequestFactory);
		restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
		
		LOGGER.info("RestClient initialize finsished!");
	}
	
	private RestClient() {
		
	}
	
	public static RestTemplate getClient() {
		return restTemplate;
	}
	
	private static List<Header> getHttpHeaders() {
		List<Header> headers = new ArrayList<Header>();
		headers.add(new BasicHeader("Accept-Encoding", "gzip,deflate"));
		headers.add(new BasicHeader("Connection", "keep-alive"));
		headers.add(new BasicHeader("Client-Id", UUID.randomUUID().toString()));
		headers.add(new BasicHeader("Client-Name", "vst_order"));
		try {
			headers.add(new BasicHeader("Client-IP",InetAddress.getLocalHost().getHostAddress()));
		} catch (UnknownHostException e) {
			LOGGER.warn("unknown host: {}", e.getMessage());
		}
		return headers;
	}


}
