package com.lvmama.vst.order.dynprops;

import java.util.List;

import org.junit.Test;

import com.lvmama.config.common.ZooKeeperConfigProperties;
import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.order.constant.config.DynConfigProp;

public class DynConfigPropTest extends OrderTestBase {
	
	@Test
	public void getErUnProviderIdsTest() {
		ZooKeeperConfigProperties.getProperties("jdbc.driverClassName");
		
		DynConfigProp dcp = DynConfigProp.getInstance();
		
		List<Long> erUnProviderIds = dcp.getErUnProviderIds();
		System.out.println(erUnProviderIds);
		
		boolean erDistChnl = dcp.getErDistChnl();
		System.out.println(erDistChnl);
		
		List<String> erDistChnlCodes = dcp.getErDistChnlCodes();
		System.out.println(erDistChnlCodes);
		
		int erPageRefreshTime = dcp.getErPageRefreshTime();
		System.out.println(erPageRefreshTime);
	}
	
	@Test
	public void cacheTest() {
		String key = "ER_CACHE_REFRESH_TIME-KEY";
		
		MemcachedUtil memcached = MemcachedUtil.getInstance();
		if (!memcached.keyExists(key)) {
			memcached.set(key, 2, "Y");
		} else {
			memcached.set(key, 1, "N");
		}
		
		try {
			for (int i = 0; i < 300; i++) {
			    Thread.sleep(2000);
			
			    String val = memcached.get(key);
			    System.out.println(val + "  " + i);
			    
			    if (val == null) {
			    	System.out.println(memcached.keyExists(key) + "  " + i);
			    }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
