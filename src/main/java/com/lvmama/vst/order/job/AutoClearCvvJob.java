/**
 * 
 */
package com.lvmama.vst.order.job;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.IOrderUpdateService;

/**
 * 清除敏感信息
 * @author lancey
 *
 */
public class AutoClearCvvJob implements Runnable{
	
	@Autowired
	private IOrderUpdateService orderUpdateService;

	@Override
	public void run() {
		if(Constant.getInstance().isJobRunnable()){
			List<Long> orderIds = orderUpdateService.queryGuaranteeOrderIds();
			if(CollectionUtils.isNotEmpty(orderIds)){
				for(Long orderId:orderIds){
					orderUpdateService.updateGuaranteeCC(orderId);
				}
			}
		}
	}

}
