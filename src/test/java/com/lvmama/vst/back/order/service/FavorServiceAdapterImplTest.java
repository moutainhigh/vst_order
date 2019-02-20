package com.lvmama.vst.back.order.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.Pair;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.Coupon;
import com.lvmama.vst.comm.vo.order.FavorStrategyInfo;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.pet.adapter.FavorServiceAdapter;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml" })
public class FavorServiceAdapterImplTest {

	@Autowired
	private FavorServiceAdapter favorServiceAdapter;

	// 注入综合查询业务接口
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
	private IOrderLocalService orderServiceRemote;
	

	@Resource(name="orderMessageProducer")
	private TopicMessageProducer orderMessageProducer;
	
	
	@Test
	public void test() {
		
		
		//orderMessageProducer.sendMsg(MessageFactory.newOrderCancelMessage(110L, ""));
		
		Long orderId=110L;
		Long goodsId=73188L;
		String code="ATEST6730395807";
		 
		
		
		BuyInfo buyInfo=new BuyInfo();
		List<BuyInfo.Item> itemList=new ArrayList<BuyInfo.Item>();
		BuyInfo.Item item=new BuyInfo.Item();
		item.setGoodsId(goodsId);
		item.setQuantity(2);
		
		itemList.add(item);
		
		buyInfo.setItemList(itemList);
		
		BuyInfo.Coupon coupon=new Coupon();
		coupon.setCode(code);
		
		List<BuyInfo.Coupon> couponList=new ArrayList<BuyInfo.Coupon>();
		couponList.add(coupon);
		
		ResultHandle validateHandle =favorServiceAdapter.validateCoupon(buyInfo.getItemList(),coupon );
		
		System.out.println("+++++++++++"+validateHandle.isSuccess()+"："+validateHandle.getMsg());
		
		OrdOrder orderVst=complexQueryService.queryOrderByOrderId(orderId);
		
		if (validateHandle.isSuccess()) {
			
			orderUpdateService.updateOrderUsedFavor(orderVst,coupon.getCode());
			
		}
		
		
		
		
		
		
	}
}
