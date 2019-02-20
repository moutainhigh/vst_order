/**
 * 
 */
package com.lvmama.vst.order.timeprice.service.impl;

import com.lvmama.vst.order.vo.OrdOrderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.order.po.OrdOrderItem;

/**
 * 交通的退改机制使用门票的
 * @author lancey
 *
 */
@Component("orderTrafficTimePriceService")
public class OrderTrafficTimePriceServiceImpl extends AbstractOrderLineTimePriceServiceImpl{

	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	
	@Override
	protected void setCancel(OrdOrderDTO order,OrdOrderItem orderItem) {
		OrderTimePriceUtils.setOtherRefund(orderItem, suppGoodsClientService);
	}

}
