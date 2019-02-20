package com.lvmama.vst.elasticsearch.cnn;

import org.elasticsearch.client.Client;

public interface ESClientConnectionManager {

	Client getConnection();

	Client requestConnection();

	void releaseConnection();

	void shutdown();
}
