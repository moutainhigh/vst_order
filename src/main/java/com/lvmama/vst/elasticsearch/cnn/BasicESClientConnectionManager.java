package com.lvmama.vst.elasticsearch.cnn;

import org.elasticsearch.client.Client;

public class BasicESClientConnectionManager implements
		ESClientConnectionManager {

	@Override
	public Client getConnection() {
		return null;
	}

	@Override
	public Client requestConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void releaseConnection() {

	}

	@Override
	public void shutdown() {

	}

}
