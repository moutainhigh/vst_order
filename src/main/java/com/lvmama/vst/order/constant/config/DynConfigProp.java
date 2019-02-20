package com.lvmama.vst.order.constant.config;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.lvmama.config.common.ZooKeeperConfigProperties;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.Constant;

/**
 * 动态配置属性
 * <p>
 * 1、优先从sweet获取配置属性值, 且sweet属性的配置修改实时生效。
 *    SWEET(ZK)动态配置属性修改实时生效, 须配置(zookeeper.config.watcher.enabled=true)
 * 2、若sweet未获取相应的属性值, 则从当前应用的const.properties文件中获取。
 * </p>
 * 
 * @version 1.0
 */
public class DynConfigProp {

	private static final Logger LOG = LoggerFactory.getLogger(DynConfigProp.class);
	
	private static final String ER_UNPROVIDERIDS = "expiredRefund.unproviderids";
	
	private static final String ER_DISTCHNL = "expiredRefund.distchnl";
	
	private static final String ER_DISTCODE = "expiredRefund.distcode";
	
	private static final String ER_DISTCHNLCODES = "expiredRefund.distchnlcodes";
	
	private static final String ER_DISTCHNL_VALS = "expiredRefund.distchnlvals";
	
	private static final String ER_PAGE_REFRESH_TIME = "expiredRefund.page.refresh.time";
	
	private static final String ER_ROWNUM_MAX = "expiredRefund.rownum.max";
	
	private static List<Long> erUnprovideridsList;
	
	private static boolean erDistchnl = false;
	
	private static boolean erDistcode = false;
	
	private static List<String> erDistchnlcodesList;
	
	private static List<Long> erDistchnlvalsList;
	
	private static int erPageRefreshTime = 60 * 60;
	
	private static int erRownumMax = Constant.ROWNUM_MAX;

	private static Properties props = new Properties();

	private static DynConfigProp dynConfigProp = new DynConfigProp();

	public DynConfigProp() {
		try {
			URL url = DynConfigProp.class.getResource("/const.properties");
			if (url == null) {
				LOG.info("DynConfigProp: const.properties may not exist, Please check sweet-config!");
				return;
			}
			
			props.load(DynConfigProp.class.getResourceAsStream("/const.properties"));
			
			String erUnprovideridsProp = props.getProperty(ER_UNPROVIDERIDS);
			if (!StringUtils.isEmpty(erUnprovideridsProp)) {
				erUnprovideridsList = StringUtil.str2listOflong(erUnprovideridsProp);
			}
			
			String erDistchnlProp = props.getProperty(ER_DISTCHNL);
			if (!StringUtils.isEmpty(erDistchnlProp)) {
				erDistchnl = Boolean.valueOf(erDistchnlProp);
			}
			
			String erDistchnlcodesProp = props.getProperty(ER_DISTCHNLCODES);
			if (!StringUtils.isEmpty(erDistchnlcodesProp)) {
				erDistchnlcodesList = StringUtil.str2list(erDistchnlcodesProp);
			}
			
			String erDistchnlvalsProp = props.getProperty(ER_DISTCHNL_VALS);
			if (!StringUtils.isEmpty(erDistchnlvalsProp)) {
				erDistchnlvalsList = StringUtil.str2listOflong(erDistchnlvalsProp);
			}
			
			String erPageRefreshTimeProp = props.getProperty(ER_PAGE_REFRESH_TIME);
			if (!StringUtils.isEmpty(erPageRefreshTimeProp)) {
				erPageRefreshTime = Integer.valueOf(erPageRefreshTimeProp);
			}
			
			String erRownumMaxProp = props.getProperty(ER_ROWNUM_MAX);
			if (!StringUtils.isEmpty(erRownumMaxProp)) {
				erRownumMax = Integer.valueOf(erRownumMaxProp);
			}
		} catch (Exception e) {
			LOG.error("DynConfigProp: Init is error!", e);
		}
	}

	/**
	 * 门票过期退: 独立申码中不处理的服务商
	 * 
	 * @return List<Long>
	 */
	public List<Long> getErUnProviderIds() {
		String sweetValue = ZooKeeperConfigProperties.getProperties(ER_UNPROVIDERIDS);
		if (!StringUtils.isEmpty(sweetValue)) {
			return StringUtil.str2listOflong(sweetValue);
		}
		
		if (erUnprovideridsList != null) {
			return erUnprovideridsList;
		}
		
		LOG.info("DynConfigProp: erUnProviderIds is not config, use the default value!");
		return Constant.UNPROVIDERIDS;
	}
	
	/**
	 * 门票过期退: 无线渠道开关
	 * 
	 * @return boolean
	 */
	public boolean getErDistChnl() {
		String sweetValue = ZooKeeperConfigProperties.getProperties(ER_DISTCHNL);
		if (!StringUtils.isEmpty(sweetValue)) {
			return Boolean.valueOf(sweetValue);
		}
		
		return erDistchnl;
	}
	
	/**
	 * 门票过期退: 分销渠道开关
	 * 
	 * @return boolean
	 */
	public boolean getErDistCode() {
		String sweetValue = ZooKeeperConfigProperties.getProperties(ER_DISTCODE);
		if (!StringUtils.isEmpty(sweetValue)) {
			return Boolean.valueOf(sweetValue);
		}
		
		return erDistcode;
	}
	
	/**
	 * 门票过期退: 分销渠道CODE
	 * 
	 * @return List<String>
	 */
	public List<String> getErDistChnlCodes() {
		String sweetValue = ZooKeeperConfigProperties.getProperties(ER_DISTCHNLCODES);
		if (!StringUtils.isEmpty(sweetValue)) {
			return StringUtil.str2list(sweetValue);
		}
		
		if (erDistchnlcodesList != null) {
			return erDistchnlcodesList;
		}
		
		LOG.info("DynConfigProp: erDistChnlCodes is not config, use the default value!");
		return Constant.DIST_CHNL_CODES;
	}
	
	/**
	 * 门票过期退: 无线渠道
	 * 
	 * @return List<Long>
	 */
	public List<Long> getErDistChnlVals() {
		String sweetValue = ZooKeeperConfigProperties.getProperties(ER_DISTCHNL_VALS);
		if (!StringUtils.isEmpty(sweetValue)) {
			return StringUtil.str2listOflong(sweetValue);
		}
		
		if (erDistchnlvalsList != null) {
			return erDistchnlvalsList;
		}
		
		LOG.info("DynConfigProp: erDistChnlVals is not config, use the default value!");
		return Constant.DIST_CHNL_VALS;
	}
	
	/**
	 * 过期退页面刷新时间
	 * 
	 * @return int
	 */
	public int getErPageRefreshTime() {
		String sweetValue = ZooKeeperConfigProperties.getProperties(ER_PAGE_REFRESH_TIME);
		if (!StringUtils.isEmpty(sweetValue)) {
			return Integer.valueOf(sweetValue);
		}
		
	    return erPageRefreshTime;
	}
	
	/**
	 * 数据列表最大ROWNUM
	 * 
	 * @return int
	 */
	public int getErRownumMax() {
		String sweetValue = ZooKeeperConfigProperties.getProperties(ER_ROWNUM_MAX);
		if (!StringUtils.isEmpty(sweetValue)) {
			return Integer.valueOf(sweetValue);
		}
		
	    return erRownumMax;
	}

	public static DynConfigProp getInstance() {
		return dynConfigProp;
	}

}
