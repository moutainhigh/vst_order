package com.lvmama.vst.order.zk;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.vo.Constant;

/**
 * 动态配置
 * @author xiaoyulin
 *
 */
@Component("dynamicPropertiesFactory")
public class DynamicPropertiesFactory implements InitializingBean {
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicPropertiesFactory.class);
	
	
	@Autowired(required=false)
	private ZookeeperAdapter zookeeperAdapter;
	
	private TreeCache treeCache;
	
	/**
	 * 内存映射关系
	 */
	public Map<String,Properties> mapProperties  = new ConcurrentHashMap<String,Properties>();
	
	private final static String POSTFIX = ".properties";
	
	private final static String BASEPATH = "/vst_order";
	
	private final static String DYNAMIC_NODE = "dynamic";
	
	
	private final static String DYNAMIC_PROPERTYFILE_NAME = DYNAMIC_NODE + POSTFIX;
	
	
	/**
	 * async_workflow
	 */
	private Properties dynamicProperties;
	

	@Override
	public void afterPropertiesSet() throws Exception {
		initBaseNode();
		initDynamicProperties();
	}
	
	private String getUserDir(){
		Enumeration<URL> urls = null;
		try {
			urls = DynamicPropertiesFactory.class.getClassLoader().getResources(".");
		} catch (IOException e) {
			LOGGER.error("{}", e);
		}
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			String path =url.toString();
			if(path.endsWith("/classes/")) {
				return path;
			}
			
		}
		
		return DynamicPropertiesFactory.class.getClassLoader().getResource("/").getPath();
	}
	
	/**
	 *初始化根node 并设置子节点监听模式
	 */
	private void initBaseNode(){
		try {
			String rootPath = zookeeperAdapter.getRootPathBase();
			boolean flag = zookeeperAdapter.checkExists(rootPath+BASEPATH);
			if (!flag) {
				zookeeperAdapter.createOrSetNode(rootPath+BASEPATH, "base");
			}
			treeCache = new TreeCache(zookeeperAdapter.getZkClient(),rootPath+BASEPATH);  
			
	        treeCache.start();  
	        treeCache.getListenable().addListener(new TreeCacheListener() {
				
				@Override
				public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
					switch (event.getType()) {
					case NODE_ADDED:  
						LOGGER.info("TreeNode added: " 
								+ event.getData().getPath() 
								+ " , data: " + new String(event.getData().getData()) 
								+ "," + getFileNameByZkPath(event.getData().getPath())); 
		                if (Pattern.compile(BASEPATH+".+").matcher(event.getData().getPath()).find()) {
		                	String filename = getFileNameByZkPath(event.getData().getPath());
		                	//新增节点，写入到本地文件并保存到map中
		                	refreshZkPropertiesMap(new String(event.getData().getData()), filename);
		                } else {
		                	LOGGER.info("is baseNode" + event.getData().getPath());
		                }
		                break;
					case NODE_UPDATED:  
						LOGGER.info("TreeNode updated: "
								+ event.getData().getPath() 
								+ " , data: " + new String(event.getData().getData()) 
								+ "," + getFileNameByZkPath(event.getData().getPath()));
		                if (Pattern.compile(BASEPATH+".+").matcher(event.getData().getPath()).find()) {
		                	String filename = getFileNameByZkPath(event.getData().getPath());
		                	//更新节点，写入到本地文件并保存到map中
		                	refreshZkPropertiesMap(new String(event.getData().getData()), filename);
		                } else {
		                	LOGGER.info("is baseNode" + event.getData().getPath());
		                }
		                break;  
					case NODE_REMOVED:  
						LOGGER.info("TreeNode removed: " + event.getData().getPath());
		                break;  
					default:
						break;
					}
				}
			});  
	       
		} catch (Exception e) {
			LOGGER.error("DynamicPropertiesFactory.initBaseNode,{}", e);
		}
	}
	
	/**
	 * 根据node Path 提取最后一级目录
	 * @param path
	 * @return
	 */
	private String getFileNameByZkPath(String path){
		return path.substring(path.lastIndexOf("/")+1);
	}
	
	/**
	 * 刷新properties 到内存
	 * @param json
	 * @param filename
	 */
	private void refreshZkPropertiesMap(String json,String filename){
		Properties properties = writeFileAndLoadProperties(json, filename);
		LOGGER.info("refreshZkPropertiesMap writeFileAndLoadProperties success, filename = " + filename);
		mapProperties.put(filename, properties);
		LOGGER.info("put properties to map success, filename = " + filename 
				+ ",mapProperties:" + JSON.toJSON(mapProperties));
		
		refreshDynamicProperties();
	}
	
	private void refreshDynamicProperties(){
		dynamicProperties = mapProperties.get(DYNAMIC_NODE);
		if(dynamicProperties == null){
			dynamicProperties = getDynamicProperties();
		}
	}
	
	/**
	 * 写文件到磁盘
	 * @param json
	 * @param filename
	 * @return
	 */
	private Properties writeFileAndLoadProperties(String json,String filename){
		OutputStream out = null;
		Properties properties = null;
		try {
			if(json!=null&&!"".equals(json)){
				properties = new Properties();
				InputStream inputStream = new ByteArrayInputStream(json.getBytes());
				properties.load(inputStream);
				File f = new File(this.getUserDir() + BASEPATH + "/" + filename + POSTFIX);
				if(!f.exists()){
					if(!f.getParentFile().exists()){
						f.getParentFile().mkdirs();
					}
					f.createNewFile();
				}
				out = new FileOutputStream(f);
				properties.store(out, filename);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		} finally{
			if (out !=null){
				try {
					out.flush();
				} catch (IOException e) {
					LOGGER.error("{}", e);
				}
				try {
					out.close();
				} catch (IOException e) {
					LOGGER.error("{}", e);
				}
			}
		}
		return properties;
	}
	
	private void initDynamicProperties() {
		try {
			String nodeDatasStr = new String(zookeeperAdapter.getDataNode(BASEPATH + "/" + DYNAMIC_NODE).getBytes());
			if(!StringUtils.isEmpty(nodeDatasStr)){
				refreshZkPropertiesMap(new String(nodeDatasStr), DYNAMIC_NODE);
			}
//			refreshDynamicProperties();
		} catch (Exception e) {
			LOGGER.warn("initDynamicProperties exception,{" + e.getMessage() + "}");
		}
	}
	
	/**
	 * 根据路径返回配置文件
	 * 
	 * @param propertyFileName
	 * @return
	 */
	private Properties getDynamicProperties() {
		Properties properties = new Properties();
		try {
			InputStream inputStream = DynamicPropertiesFactory.class.getClassLoader()
					.getResourceAsStream(BASEPATH + "/" + DYNAMIC_PROPERTYFILE_NAME);
			properties.load(inputStream);
		} catch (FileNotFoundException e) {
			LOGGER.error("{}", e);
		} catch (IOException e) {
			LOGGER.error("{}", e);
		}			
		return properties;
	}
	
	/**
	 * 订单是否异步启动工作流
	 * @param order
	 * @return
	 */
	public boolean isAsyncGenWorkflow(final OrdOrder order){
		try {
			if(order == null){
				return false;
			}
			if("grouptour_tuniu_main".equals(order.getProcessKey()))return true;
			if(Constant.DIST_BACK_END == order.getDistributorId() 
					|| Constant.DIST_O2O_APP_SELL == order.getDistributorId()){// 后台及o2o订单订单工作流不走异步
				return false;
			}
			
			String asyncGenWorkflowStr = dynamicProperties.getProperty("async_gen_workflow");
			if(asyncGenWorkflowStr != null && !"".equals(asyncGenWorkflowStr)){
				boolean asyncGenWorkflow = Boolean.valueOf(asyncGenWorkflowStr);
				if(!asyncGenWorkflow){
					String asyncGenWorkflowUnTicketStr = dynamicProperties.getProperty("async_gen_workflow_unticket");
					if(asyncGenWorkflowUnTicketStr != null && !"".equals(asyncGenWorkflowUnTicketStr)){
						boolean isAsyncGenWorkflowUnTicket = Boolean.valueOf(asyncGenWorkflowUnTicketStr);
						if(isAsyncGenWorkflowUnTicket){
							// 不对门票开放
							if(order.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId() 
								|| order.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId() 
								|| order.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId()
					            || order.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId()){
								return false;
							}
						}
					}
						
					String[] asyncGenWorkflowDcodeArray = null;
					String[] asyncGenWorkflowChannelArray = null;
					
					String asyncGenWorkflowDcodeStr = dynamicProperties.getProperty("async_gen_workflow_dcode");
					if(asyncGenWorkflowDcodeStr != null && !"".equals(asyncGenWorkflowDcodeStr)){
						asyncGenWorkflowDcodeArray = asyncGenWorkflowDcodeStr.split(",");
					}
					
					String asyncGenWorkflowChannelStr = dynamicProperties.getProperty("async_gen_workflow_channel");
					if(asyncGenWorkflowChannelStr != null && !"".equals(asyncGenWorkflowChannelStr)){
						asyncGenWorkflowChannelArray = asyncGenWorkflowChannelStr.split(",");
					}
					
					Long distributionChannel = order.getDistributionChannel();
					if(distributionChannel == null){
						distributionChannel = 0L;
					}
					String distributorCode = order.getDistributorCode();
					
					//分销商渠道ID:DISTRIBUTOR_API(101,"API分销渠道"),DISTRIBUTOR_LVTU(10000,"驴途分销渠道"),DISTRIBUTOR_LVTUTG(10001,"驴途团购分销渠道"),DISTRIBUTOR_LVTUMS(10002,"驴途秒杀分销渠道");
					//分销商ID=980，分销商名称=去哪儿_线路_度假；分销商ID=21306，分销商名称=	携程_线路_api对接；
					// Long[] DISTRIBUTION_CHANNEL_LIST ={10000L,10001L,10002L,980L,21306L};
					if((asyncGenWorkflowDcodeArray != null && asyncGenWorkflowDcodeArray.length > 0 
							&& ArrayUtils.contains(asyncGenWorkflowDcodeArray, distributorCode)) ||
							(asyncGenWorkflowChannelArray != null && asyncGenWorkflowChannelArray.length > 0 
							&& ArrayUtils.contains(asyncGenWorkflowChannelArray, String.valueOf(distributionChannel)))){
						return true;
					}
					
				}
				else{
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
	
}
