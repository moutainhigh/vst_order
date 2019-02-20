package com.lvmama.vst.order.timeprice.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;

/**
 * 
 * @author sunjian
 *
 */
@Component
@Deprecated
public class OrderTimePriceServiceFactory {
	
	@Resource(name="orderTimePriceService")
	private OrderTimePriceService orderTimePriceService;
	
	@Resource(name="orderMultiTimePriceService")
	private OrderTimePriceService orderMultiTimePriceService;
	
	@Resource(name="orderSingleTimePriceService")
	private OrderTimePriceService orderSingleTimePriceService;
	
	@Resource(name="orderSimpleTimePriceService")
	private OrderTimePriceService orderSimpleTimePriceService;
	
	//@Resource(name="orderAddTimePriceService")
	private OrderTimePriceService orderAddTimePriceService;
	
	public OrderTimePriceService createTimePriceService(BizCategory category) {
		OrderTimePriceService retOrderTimePriceService = null;
		
		if (category != null) {
			String categoryCode = category.getCategoryCode();
			//酒店
			if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.name().equalsIgnoreCase(categoryCode))
			{
				retOrderTimePriceService = orderTimePriceService;
			//邮轮
			} else if (BizEnum.BIZ_CATEGORY_TYPE.category_cruise.name().equalsIgnoreCase(categoryCode)) {
				retOrderTimePriceService = orderMultiTimePriceService;
			//岸上观光、邮轮附加项
			} else if (BizEnum.BIZ_CATEGORY_TYPE.category_sightseeing.name().equalsIgnoreCase(categoryCode)
					|| BizEnum.BIZ_CATEGORY_TYPE.category_cruise_addition.name().equalsIgnoreCase(categoryCode)) {
				retOrderTimePriceService = orderSingleTimePriceService;
			//签证
			} else if (BizEnum.BIZ_CATEGORY_TYPE.category_visa.name().equalsIgnoreCase(categoryCode)) {
				retOrderTimePriceService = orderSimpleTimePriceService;
			}else if (BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.name().equalsIgnoreCase(categoryCode)) {
				retOrderTimePriceService = orderAddTimePriceService;
			} 
		}
		
		return retOrderTimePriceService;
	}
}
