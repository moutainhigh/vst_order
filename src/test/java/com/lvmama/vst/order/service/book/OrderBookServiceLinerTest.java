/**
 * 
 */
package com.lvmama.vst.order.service.book;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.SupplierProductInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.order.service.IBookService;

/**
 * @author lancey
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderBookServiceLinerTest {
	
	private BuyInfo buyInfo;
	@Before
	public void init(){
		buyInfo = new BuyInfo();
		buyInfo.setProductId(81561L);
		buyInfo.setCategoryId(8L);
		
		List<Person> personList = new ArrayList<Person>();
		Person p = new Person();
		p.setFullName("hahaha");
		p.setFirstName("ab");
		p.setLastName("cd");
		personList.add(p);
		
		p = new Person();
		p.setFullName("hahaha2");
		p.setFirstName("ab2");
		p.setLastName("cd2");
		personList.add(p);
		
		buyInfo.setTravellers(personList);
		
		BuyInfo.Item item = new BuyInfo.Item();
		item.setGoodsId(454070L);
		item.setVisitTime("2014-10-31");
		item.setQuantity(2);
		item.setMainItem("true");
		List<BuyInfo.Item> list = new ArrayList<BuyInfo.Item>();
		List<BuyInfo.ItemPersonRelation> personItemList = new ArrayList<BuyInfo.ItemPersonRelation>();
		BuyInfo.ItemPersonRelation itemPerson = new BuyInfo.ItemPersonRelation();
		itemPerson.setSeq(0);
		personItemList.add(itemPerson);
		
		itemPerson = new BuyInfo.ItemPersonRelation();
		itemPerson.setSeq(1);
		personItemList.add(itemPerson);
		item.setItemPersonRelationList(personItemList);
		list.add(item);
		buyInfo.setItemList(list);
		
		buyInfo.setItemList(list);
		buyInfo.setIp("192.168.0.10");
		buyInfo.setDistributionId(Constant.DIST_BACK_END);
//		buyInfo.setAdditionalTravel("false");
		buyInfo.setUserNo(8917L);
		buyInfo.setUserId("3428a92f475861e20147586a65a70001");
		Person person = new Person();
		person.setFullName("小二");
		person.setMobile("13800138000");
		buyInfo.setContact(person);
	}
	
	@Test
	public void testCreateOrder() {
		ResultHandleT<OrdOrder> result = bookService.createOrder(buyInfo, "admin");
		org.junit.Assert.assertTrue(result.isSuccess());
		System.out.println("orderId::::::::::::::"+result.getReturnContent().getOrderId());
	}
	@Autowired
	IBookService bookService;
}
