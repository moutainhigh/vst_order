/**
 * 
 */
package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.client.dist.adaptor.DistGoodsTimePriceClientServiceAdaptor;
import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceHotelClientService;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.supp.client.elong.service.SuppCommonClientService;

/**
 * @author lancey
 * 
 */
public abstract class AbstractOrderService {

	@Autowired
	protected DistGoodsTimePriceClientServiceAdaptor distGoodsTimePriceClientServiceAdaptor;

	@Autowired
	protected SuppCommonClientService suppCommonClientService;
	
	@Autowired
	protected SuppGoodsClientService suppGoodsClientService;
	
	@Autowired
	protected SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;
	
	@Autowired
	protected ProdProductClientService prodProductClientService;
	
	@Autowired
	protected IOrderUpdateService orderUpdateService;
}
