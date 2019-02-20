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
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.vo.order.PriceInfo;
import com.lvmama.vst.order.service.IBookService;
import com.lvmama.vst.order.service.IOrderPriceService;

/**
 * @author lancey
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderBookServiceRouteTest {
	
	private BuyInfo buyInfo;
	
	@Autowired
	private IBookService bookService;
	
	/**
	 * 
	 */
	//@Before
	public void init(){
		buyInfo = new BuyInfo();
		List<BuyInfo.Product> productList = new ArrayList<BuyInfo.Product>();
		
		BuyInfo.Product product = new BuyInfo.Product();
		product.setProductId(96341L);
		product.setAdultQuantity(2);
		product.setChildQuantity(0);
		product.setVisitTime("2014-09-20");
		product.setQuantity(2);
		
//		List<BuyInfo.Item> itemList = new ArrayList<BuyInfo.Item>();
//		BuyInfo.Item item = new BuyInfo.Item();
		
		productList.add(product);
		
		
		
		buyInfo.setProductList(productList);
		
		buyInfo.setDistributionId(Constant.DIST_BACK_END);

		buyInfo.setUserNo(8917L);
		buyInfo.setUserId("3428a92f475861e20147586a65a70001");
		Person person = new Person();
		person.setFullName("小二");
		person.setMobile("13800138000");
		buyInfo.setContact(person);
		
		List<Person> personList = new ArrayList<Person>();
		person = new Person();
		person.setFullName("小四");
		person.setMobile("13800138001");
		personList.add(person);
		
		person = new Person();
		person.setFullName("小五");
		person.setMobile("13800138002");
		personList.add(person);
		buyInfo.setTravellers(personList);
	}
	
//	@Before
	public void initHotelComp(){
		buyInfo = new BuyInfo();
//		List<BuyInfo.Product> productList = new ArrayList<BuyInfo.Product>();
		
//		BuyInfo.Product product = new BuyInfo.Product();
//		product.setProductId(96341L);
//		product.setAdultQuantity(2);
//		product.setChildQuantity(0);
//		product.setVisitTime("2014-09-20");
//		product.setQuantity(2);
		
		List<BuyInfo.Item> itemList = new ArrayList<BuyInfo.Item>();
		BuyInfo.Item item = new BuyInfo.Item();
		item.setAdultQuantity(2);
		item.setGoodsId(567424L);
		item.setVisitTime("2014-09-06");
		item.setRouteRelation(BuyInfo.ItemRelation.ADDITION);
		
		itemList.add(item);
		
		item = new BuyInfo.Item();
		item.setAdultQuantity(2);
		item.setGoodsId(567423L);
		item.setVisitTime("2014-09-06");
		item.setRouteRelation(BuyInfo.ItemRelation.MAIN);
		
		itemList.add(item);
//		productList.add(product);
		
//		buyInfo.setProductList(productList);
		buyInfo.setItemList(itemList);
		
		buyInfo.setDistributionId(Constant.DIST_BACK_END);

		buyInfo.setUserNo(8917L);
		buyInfo.setUserId("3428a92f475861e20147586a65a70001");
		Person person = new Person();
		person.setFullName("小二");
		person.setMobile("13800138000");
		buyInfo.setContact(person);
		
		List<Person> personList = new ArrayList<Person>();
		person = new Person();
		person.setFullName("小四");
		person.setMobile("13800138001");
		personList.add(person);
		
		person = new Person();
		person.setFullName("小五");
		person.setMobile("13800138002");
		personList.add(person);
		buyInfo.setTravellers(personList);
	}
	
	
	@Before
	public void initHotelComp2(){
		buyInfo = new BuyInfo();
//		List<BuyInfo.Product> productList = new ArrayList<BuyInfo.Product>();
		
//		BuyInfo.Product product = new BuyInfo.Product();
//		product.setProductId(96341L);
//		product.setAdultQuantity(2);
//		product.setChildQuantity(0);
//		product.setVisitTime("2014-09-20");
//		product.setQuantity(2);
		
		List<BuyInfo.Item> itemList = new ArrayList<BuyInfo.Item>();
		BuyInfo.Item item = new BuyInfo.Item();
		item.setAdultQuantity(2);
		item.setGoodsId(567424L);
		item.setVisitTime("2014-09-06");
		item.setRouteRelation(BuyInfo.ItemRelation.ADDITION);
		
		itemList.add(item);
		
		item = new BuyInfo.Item();
		item.setAdultQuantity(2);
		item.setGoodsId(573633L);
		item.setVisitTime("2014-11-16");
//		BuyInfo.HotelAdditation add = new HotelAdditation();
//		add.setArrivalTime("");
//		item.setRouteRelation(BuyInfo.ItemRelation.MAIN);
		
		itemList.add(item);
//		productList.add(product);
		
//		buyInfo.setProductList(productList);
		buyInfo.setItemList(itemList);
		
		buyInfo.setDistributionId(Constant.DIST_BACK_END);

		buyInfo.setUserNo(8917L);
		buyInfo.setUserId("3428a92f475861e20147586a65a70001");
		Person person = new Person();
		person.setFullName("小二");
		person.setMobile("13800138000");
		buyInfo.setContact(person);
		
		List<Person> personList = new ArrayList<Person>();
		person = new Person();
		person.setFullName("小四");
		person.setMobile("13800138001");
		personList.add(person);
		
		person = new Person();
		person.setFullName("小五");
		person.setMobile("13800138002");
		personList.add(person);
		buyInfo.setTravellers(personList);
	}
	
	@Test
	public void testCreateOrder(){
		PriceInfo priceInfo = orderPriceService.countPrice(buyInfo);
//		ResultHandleT<OrdOrder> result = bookService.createOrder(buyInfo, "admin");
//		Assert.assertTrue(result.isSuccess());
		System.out.println(priceInfo.getPrice());
//		System.out.println("orderId::::::::::::::"+result.getReturnContent().getOrderId());
	}
	@Autowired
	private IOrderPriceService orderPriceService;
}
