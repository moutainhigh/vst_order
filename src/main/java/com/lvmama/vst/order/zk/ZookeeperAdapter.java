package com.lvmama.vst.order.zk;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperAdapter {
	private static final Logger logger = LoggerFactory.getLogger(ZookeeperAdapter.class);

	private CuratorFramework zkClient;

	private String rootPathBase = "";

	public CuratorFramework getZkClient() {
		return zkClient;
	}

	public void setZkClient(CuratorFramework zkClient) {
		this.zkClient = zkClient;
	}

	public String getRootPathBase() {
		return rootPathBase;
	}

	public void setRootPathBase(String rootPathBase) {
		this.rootPathBase = rootPathBase;
	}

	/**
	 * 获取子节点列表
	 * 
	 * @param parentPath
	 * @return
	 * @throws Exception
	 */
	public List<String> nodesList(String parentPath) throws Exception {
		return zkClient.getChildren().forPath(rootPathBase + parentPath);
	}

	/**
	 * 创建一个节点
	 * 
	 * @param path
	 * @param message
	 * @throws Exception
	 */
	public void createOrSetNode(String path, String message) throws Exception {
		message = StringUtils.isBlank(message) ? "" : message;
		Stat stat = zkClient.checkExists().forPath(rootPathBase + path);
		if (stat == null) {
			zkClient.create().creatingParentsIfNeeded().forPath(rootPathBase + path, message.getBytes());
		} else {
			zkClient.setData().forPath(rootPathBase + path, message.getBytes());
		}
	}

	/**
	 * 检查是否存在
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public boolean checkExists(String path) throws Exception {
		Stat stat = zkClient.checkExists().forPath(rootPathBase + path);
		return stat != null ? true : false;
	}

	/**
	 * 获取指定节点中信息
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public String getDataNode(String path) throws Exception {
		Stat stat = zkClient.checkExists().forPath(rootPathBase + path);
		if (stat != null) {
			byte[] datas = zkClient.getData().forPath(rootPathBase + path);
			return new String(datas);
		}
		return null;
	}

	/**
	 * 设置某个节点的值
	 * 
	 * @param client
	 * @param path
	 * @param message
	 * @throws Exception
	 */
	public void setDataNode(String path, String message) throws Exception {
		Stat stat = zkClient.checkExists().forPath(rootPathBase + path);
		if (stat != null) {
			zkClient.setData().forPath(rootPathBase + path, message.getBytes());
		} else {
			createOrSetNode(path, message);
		}
	}

	/**
	 * 删除某个节点
	 * 
	 * @param path
	 * @throws Exception
	 */
	public void deleteDataNode(String path) throws Exception {
		Stat stat = zkClient.checkExists().forPath(rootPathBase + path);
		if (stat != null) {
			Void forPath = zkClient.delete().deletingChildrenIfNeeded().forPath(rootPathBase + path);
		}
	}

}
