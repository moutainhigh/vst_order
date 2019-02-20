/**
 * 
 */
package com.lvmama.vst.order.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.dist.service.DistributorClientService;
import com.lvmama.vst.back.dist.po.Distributor;
import com.lvmama.vst.comm.vo.ResultHandleT;

/**
 * @author liuxiuxiu
 *
 */
@Service
public class DistributionBussiness implements InitializingBean{
	
	//@Autowired
	private DistributorClientService distributorClientService;
	
	private Map<Long,Distributor> cache = new HashMap<Long, Distributor>();

	public String getDistributionName(Long distId){
		Distributor dist = cache.get(distId);
		if(dist!=null){
			return dist.getDistributorName();
		}
		return null;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String,Object> params = new HashMap<String, Object>();
//		ResultHandleT<List<Distributor>> list = distributorClientService.findDistributorList(params);
//		for(Distributor dist:list.getReturnContent()){
//			cache.put(dist.getDistributorId(), dist);
//		}
	}
}
