/**
 * 
 */
package com.lvmama.vst.precontrol.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.lvmama.precontrol.hotel.HotelResPrecontrolBindGoodsService;
import com.lvmama.precontrol.vo.VstOrderItemVo;
import com.lvmama.vst.back.order.OrderTestBase;

/**
 * @author chenlizhao
 *
 */
public class HotelResPrecontrolBindGoodsServiceImplTest extends OrderTestBase {
	@Autowired
	private  HotelResPrecontrolBindGoodsService hotelResPrecontrolBindGoodsService;
	
	@Test
    public void testGetOrderItemNum(){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("tradeEffectDate", new Date());
		param.put("tradeExpiryDate", new Date());
		param.put("goodId", 2584765L);
		Long num = hotelResPrecontrolBindGoodsService.getOrderItemNum(param);
		Assert.isTrue(num != null && num >= 0);
		System.out.println(num);
	}
	
	@Test
    public void testGetPreControlPolicyHistoryOrder(){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("from", new Date());
		params.put("to", new Date());
		params.put("statIndex", 0);
		params.put("endIndex", 10);
		params.put("goodsID", 2584765L);
		List<Long> ids = hotelResPrecontrolBindGoodsService.getPreControlPolicyHistoryOrder(params);
		Assert.notNull(ids);
		System.out.println(ids.size());
	}
	
	@Test
    public void testSetVstOrderItemBudgetFlag(){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("from", new Date());
		params.put("to", new Date());
		params.put("goodsID", 2584765L);
		int num = hotelResPrecontrolBindGoodsService.setVstOrderItemBudgetFlag(params);
		Assert.isTrue(num >= 0);
		System.out.println(num);
	}
	
	@Test
    public void testGetVstNotBuyoutOrderHotel(){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("startDate", new Date());
		params.put("endDate", new Date());
		List<Long> ids = new ArrayList<Long>();
		ids.add(2584765L);
		params.put("goodIds", ids);
		List<VstOrderItemVo> vos = hotelResPrecontrolBindGoodsService.getVstNotBuyoutOrderHotel(params);
		Assert.notNull(vos);
		System.out.println(vos.size());
	}
	
	@Test
    public void testUpdateVstBudgetFlagBylistHotel(){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("startDate", new Date());
		params.put("endDate", new Date());
		List<Long> ids = new ArrayList<Long>();
		ids.add(2584765L);
		params.put("goodIds", ids);
		int num = hotelResPrecontrolBindGoodsService.updateVstBudgetFlagBylistHotel(params);
		Assert.isTrue(num >= 0);
		System.out.println(num);
	}
	
	@Test
    public void testUpdateOrderBatchHotel(){
		List<VstOrderItemVo> items = new ArrayList<VstOrderItemVo>();
		VstOrderItemVo item = new VstOrderItemVo();
		item.setBuyoutPrice(0L);
		item.setBuyoutTotalPrice(0L);
		item.setBuyoutQuantity(0L);
		item.setOrderItemId(30000350510L);
		items.add(item);
		int num = hotelResPrecontrolBindGoodsService.updateOrderBatchHotel(items);
		Assert.isTrue(num >= 0);
		System.out.println(num);
	}
	
	@Test
    public void testGetHotelOrderItemNum(){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("tradeEffectDate", new Date());
		param.put("tradeExpiryDate", new Date());
		param.put("goodId", 2584765L);
		Long num = hotelResPrecontrolBindGoodsService.getHotelOrderItemNum(param);
		Assert.isTrue(num != null && num >= 0);
		System.out.println(num);
	}
}
