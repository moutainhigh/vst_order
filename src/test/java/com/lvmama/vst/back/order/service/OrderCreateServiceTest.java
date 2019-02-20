/**
 * 
 */
package com.lvmama.vst.back.order.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.Person;

/**
 * @author lancey
 *
 */
//@TransactionConfiguration(transactionManager = "txManager", defaultRollback = false)
//@Transactional(readOnly=false)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:applicationContext-vst-order-beans.xml"})
public class OrderCreateServiceTest {
	
	@Autowired
	private OrderService orderService;

	@Test
	public void testCreateOrder(){
		BuyInfo buyInfo = new BuyInfo();
		List<BuyInfo.Item> itemList = new ArrayList<BuyInfo.Item>();
		for(long i:Arrays.asList(633906,515732)){
			BuyInfo.Item item = new BuyInfo.Item();
			item.setGoodsId(i);
			item.setVisitTime("2014-04-20");
			item.setQuantity(1);
			BuyInfo.HotelAdditation add= new BuyInfo.HotelAdditation();
			add.setArrivalTime("20:00");
			add.setLeaveTime("2014-04-21");
			item.setHotelAdditation(add);
			itemList.add(item);
		}
		buyInfo.setUserId("5ad32f1a1ccdf220011ccfc756ab0012");
		buyInfo.setUserNo(10932L);
		buyInfo.setDistributionId(2L);
		Person person = new Person();
		person.setFullName("张三");
		buyInfo.setTravellers(Arrays.asList(person));
		buyInfo.setItemList(itemList);
		ResultHandleT<OrdOrder> handle = orderService.createOrder(buyInfo, "admin");
		Assert.assertNotNull(handle);
		Assert.assertTrue(handle.isSuccess());
		System.out.println(handle.getReturnContent().getOrderId());
	}
}
