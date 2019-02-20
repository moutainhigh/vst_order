package com.lvmama.vst.elasticsearch.query;

import java.util.Properties;

import com.lvmama.config.common.ZooKeeperConfigProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.lvmama.vst.comm.utils.ExceptionFormatUtil;

public class ESWrapper {

	/**
	 * Elasticsearch客户端
	 * 
	 * @param minchun
	 */
	private static Log logger = LogFactory.getLog(ESWrapper.class);

	private static volatile Client instance = null;

	private static final Properties esProp = new Properties();

	/**
	 * 默认端口
	 */
	private static final int DEFAULT_PORT = 9300;

	static {
		try {
			esProp.load(ESWrapper.class.getResourceAsStream("/elasticsearch.properties"));
		} catch (Exception e) {
			logger.warn("Cannot load elasticsearch.properties file,Check to see if the file has been moved in sweet!");
		}
	}

	public static synchronized Client getInstance() {
		if (instance == null) {
			String esHost = getValue("esHost");
			String clusterName = getValue("clusterName");
			String esPort = getValue("esPort");
			int port = DEFAULT_PORT;
			if (StringUtils.isNotBlank(esPort)) {
				port = Integer.parseInt(esPort);
			}
			if (StringUtils.isNotEmpty(esHost) && StringUtils.isNotEmpty(clusterName)) {
				String[] esNoses = esHost.split(",");
				try {
					Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", clusterName)
							.put("client.transport.sniff", true).build();
					instance = new TransportClient(settings);
					for (String ips : esNoses) {
						((TransportClient) instance).addTransportAddress(new InetSocketTransportAddress(ips, port));
					}
				} catch (Exception e) {
					logger.error("ElasticSearch  init failed ，the reason [" + e.getMessage() + "]");
				}
			} else {
				logger.error("the es server host or clusterName  is required！，can not found the argument");
			}
		}
		return instance;
	}

	public static String[] getIndices() {
		String indiceNodes = getValue("indices");
		String[] indices = indiceNodes.split(",");
		return indices;
	}

	public static String[] getDocumentType() {
		String types = getValue("queryType");
		String[] docTypes = types.split(",");
		return docTypes;
	}

	public static String getValue(String key){
		if(StringUtils.isNotBlank(ZooKeeperConfigProperties.getProperties(key))){
			return ZooKeeperConfigProperties.getProperties(key);
		}
		String value = "";
		if(esProp != null){
			value = esProp.getProperty(key);
		}

		return value;
	}
}
