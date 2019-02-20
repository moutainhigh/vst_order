package com.lvmama.vst.order.zk;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class ZookeeperFactory implements FactoryBean<ZookeeperAdapter>, InitializingBean, DisposableBean {
	
	private static final Logger logger = LoggerFactory.getLogger(ZookeeperFactory.class);

	private ZookeeperAdapter zookeeperAdapter;

	private String connectionString;
	private String rootPathBase;
	private String sessionTimeout;
	private String userName;
	private String password;
	private String reconnectPeriods;//重连时间间隔
	private String reconnectTimes;//重连次数
	private String connectionTimeout;//连接超时时间
	private String namespace;//命名空间
	

	@Override
	public void destroy() throws Exception {
		zookeeperAdapter.getZkClient().close();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		zookeeperAdapter = new ZookeeperAdapter();
		
		 if(StringUtils.isBlank(reconnectPeriods)){
			 reconnectPeriods = "1000";
		 }
		 
		 if(StringUtils.isBlank(reconnectTimes)){
			 reconnectTimes = "3";
		 }
		 
		 if(StringUtils.isBlank(connectionTimeout)){
			 connectionTimeout = "2000";
		 }
		 
		 if(StringUtils.isBlank(sessionTimeout)){
			 sessionTimeout = "60000";
		 }
		 
		final String authStr = userName + ":" + password;
		final String scheme ="digest";
		//认证基本信息
		ACLProvider aclProvider = new ACLProvider() {
			private List<ACL> acls;

			@Override
			public List<ACL> getDefaultAcl() {
				if (acls == null) {
					ArrayList<ACL> acls = ZooDefs.Ids.CREATOR_ALL_ACL;
					acls.clear();
					try {
						acls.add(new ACL(Perms.ALL, new Id(scheme, DigestAuthenticationProvider.generateDigest(authStr))));//此处需要将密码加密才起作用
					} catch (NoSuchAlgorithmException e) {
						logger.error("NoSuchAlgorithmException",e);
					}
					acls.add(new ACL(Perms.READ,Ids.ANYONE_ID_UNSAFE));
					this.acls = acls;
				}
				return acls;
			}

			@Override
			public List<ACL> getAclForPath(String path) {
				return acls;
			}
		};
		
		// 1000 是重试间隔时间基数，3 是重试次数
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(Integer.parseInt(reconnectPeriods), Integer.parseInt(reconnectTimes));
		CuratorFramework zkClient =CuratorFrameworkFactory.builder()
				.connectString(connectionString)
//				.namespace(null)
				.retryPolicy(retryPolicy)
				.connectionTimeoutMs(Integer.parseInt(connectionTimeout))
				.aclProvider(aclProvider)
				.authorization(scheme, authStr.getBytes())//配置操作节点认证提交时认证信息
				.sessionTimeoutMs(Integer.parseInt(sessionTimeout))
				.build()
				;
		zookeeperAdapter.setZkClient(zkClient);
		zookeeperAdapter.getZkClient().start();
		zookeeperAdapter.setRootPathBase(StringUtils.isNotBlank(rootPathBase)?rootPathBase.trim():"");
		
		logger.info("zookeeper connect "+connectionString+" has connected and start ok.");
	}


	@Override
	public ZookeeperAdapter getObject() throws Exception {
		return zookeeperAdapter;
	}

	@Override
	public Class<?> getObjectType() {
		return ZookeeperAdapter.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public ZookeeperAdapter getZookeeperAdapter() {
		return zookeeperAdapter;
	}

	public void setZookeeperAdapter(ZookeeperAdapter zookeeperAdapter) {
		this.zookeeperAdapter = zookeeperAdapter;
	}

	public String getConnectionString() {
		return connectionString;
	}

	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}

	public String getRootPathBase() {
		return rootPathBase;
	}

	public void setRootPathBase(String rootPathBase) {
		this.rootPathBase = rootPathBase;
	}

	public String getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(String sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getReconnectPeriods() {
		return reconnectPeriods;
	}

	public void setReconnectPeriods(String reconnectPeriods) {
		this.reconnectPeriods = reconnectPeriods;
	}

	public String getReconnectTimes() {
		return reconnectTimes;
	}

	public void setReconnectTimes(String reconnectTimes) {
		this.reconnectTimes = reconnectTimes;
	}

	public String getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(String connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	
	
	

}
