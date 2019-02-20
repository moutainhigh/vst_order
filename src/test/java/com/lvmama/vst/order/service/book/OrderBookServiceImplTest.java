package com.lvmama.vst.order.service.book;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.vo.order.PriceInfo;
import com.lvmama.vst.order.service.IBookService;
import com.lvmama.vst.order.service.IOrderPriceService;
import com.lvmama.vst.order.service.IOrderStockService;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderBookServiceImplTest {
	
	private BuyInfo buyInfo;
	
	@Resource(name="orderNewBookService")
	private IBookService bookService;
	
	@Before
	public void init(){
		buyInfo = new BuyInfo();
		BuyInfo.Item item = new BuyInfo.Item();
		item.setGoodsId(574517L);
		item.setVisitTime("2014-11-23");
		item.setQuantity(1);
		List<BuyInfo.Item> list = new ArrayList<BuyInfo.Item>();
		list.add(item);
		/*List<Coupon> couponList = new ArrayList<BuyInfo.Coupon>();
		Coupon e= new Coupon();
		e.setCode("B327848625709985");
		couponList.add(e);
		buyInfo.setCouponList(couponList);*/
		buyInfo.setItemList(list);
		buyInfo.setIp("192.168.0.10");
		buyInfo.setDistributionId(Constant.DIST_BACK_END);
		buyInfo.setAdditionalTravel("false");
		buyInfo.setUserNo(8917L);
		buyInfo.setUserId("3428a92f475861e20147586a65a70001");
//		buyInfo.setWaitPayment(20L);
		Person person = new Person();
		person.setFullName("小二");
		person.setMobile("13800138000");
		buyInfo.setYouhui("coupon");
		buyInfo.setContact(person);
	}
	//@Before
	public void init2(){
		buyInfo = new BuyInfo();
		BuyInfo.Product product = new BuyInfo.Product();
		product.setProductId(89416L);
		product.setQuantity(1);
		product.setVisitTime("2014-10-31");
		
		List<BuyInfo.Product> productList = new ArrayList<BuyInfo.Product>();
		productList.add(product);
		buyInfo.setProductList(productList);
//		buyInfo.setp(list);
		buyInfo.setIp("192.168.0.10");
		buyInfo.setDistributionId(Constant.DIST_BACK_END);
		buyInfo.setAdditionalTravel("false");
		buyInfo.setUserNo(8917L);
		buyInfo.setUserId("3428a92f475861e20147586a65a70001");
		Person person = new Person();
		person.setFullName("小二");
		person.setMobile("13800138000");
		buyInfo.setContact(person);
	}
	
	/**
	 * 门票+保险
	 */
	//@Before
	public void init3(){
		buyInfo = new BuyInfo();
		List<BuyInfo.Item> list = new ArrayList<BuyInfo.Item>();
		BuyInfo.Item item = new BuyInfo.Item();
		item.setGoodsId(463950L);
		buyInfo.setSameVisitTime("true");
		buyInfo.setVisitTime("2014-08-15");
		item.setQuantity(2);
		list.add(item);
		item = new BuyInfo.Item();
		item.setGoodsId(465211L);
//		item.setVisitTime("2014-08-14");
		item.setQuantity(1);
		list.add(item);
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
		List<BuyInfo.ItemPersonRelation> ll = new ArrayList<BuyInfo.ItemPersonRelation>();
		BuyInfo.ItemPersonRelation irr = new BuyInfo.ItemPersonRelation();
		irr.setSeq(0);
		ll.add(irr);
		//buyInfo.getPersonRelationMap().put("GOODS_465211", ll);

	}

	/**
	 * 酒店测试
	 */
//	@Before
	public void initHotel(){
		buyInfo = new BuyInfo();
		List<BuyInfo.Item> list = new ArrayList<BuyInfo.Item>();
		BuyInfo.Item item = new BuyInfo.Item();
		item.setGoodsId(573104L);
		item.setVisitTime("2014-10-29");
		BuyInfo.HotelAdditation add = new BuyInfo.HotelAdditation();
		add.setArrivalTime("18:00");
		add.setLeaveTime("2014-10-30");
		item.setQuantity(1);
		item.setHotelAdditation(add);
		list.add(item);
		
		buyInfo.setItemList(list);
		buyInfo.setIp("192.168.0.10");
		buyInfo.setDistributionId(Constant.DIST_BACK_END);
		buyInfo.setAdditionalTravel("false");
		buyInfo.setUserNo(8917L);
		buyInfo.setUserId("3428a92f475861e20147586a65a70001");
		Person person = new Person();
		person.setFullName("小二");
		person.setMobile("13800138000");
//		List<Long> ids = new ArrayList<Long>();
//		ids.add(2423L);
//		buyInfo.setPromotionIdList(ids);
		buyInfo.setContact(person);
	}
	
	/**
	 * 人员绑定操作
	 */
//	@Before
	public void init5(){
		buyInfo = new BuyInfo();
		List<Person> personList = new ArrayList<Person>();
		Person person = new Person();
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
	public void initVisa(){
		buyInfo = new BuyInfo();
		BuyInfo.Item item = new BuyInfo.Item();
		item.setGoodsId(567321L);
		item.setVisitTime("2014-10-10");
		item.setQuantity(1);
		List<BuyInfo.Item> list = new ArrayList<BuyInfo.Item>();
		list.add(item);
		buyInfo.setItemList(list);
		buyInfo.setIp("192.168.0.10");
		buyInfo.setDistributionId(Constant.DIST_BACK_END);
		buyInfo.setAdditionalTravel("false");
		buyInfo.setUserNo(8917L);
		buyInfo.setUserId("3428a92f475861e20147586a65a70001");
		Person person = new Person();
		person.setFullName("小二");
		person.setMobile("13800138000");
		buyInfo.setContact(person);
	}
	
	@Test
	public void testPriceInfo(){
		PriceInfo priceInfo = orderPriceService.countPrice(buyInfo);
		Assert.assertNotNull(priceInfo);
		Assert.assertTrue(priceInfo.isSuccess());
	}
	
//	@Test
	public void testCreateOrder() {
//		ResultHandleT<SupplierProductInfo> handle =orderStockService.checkStock(buyInfo);
//		PriceInfo priceInfo = orderPriceService.countPrice(buyInfo);
//		Assert.assertNotNull(priceInfo);
//		Assert.assertTrue(priceInfo.isSuccess());
//		Assert.assertNotNull(priceInfo.getPromotionList());
//		for(PromPromotion pp:priceInfo.getPromotionList()){
//			System.out.println(pp.getTitle());
//		}
		ResultHandleT<OrdOrder> result = bookService.createOrder(buyInfo, "admin");
		org.junit.Assert.assertTrue(result.isSuccess());
		System.out.println("orderId::::::::::::::"+result.getReturnContent().getOrderId());
	}
	//@Test
	public void testConcurrent(){
		ExecutorService executor = Executors.newFixedThreadPool(100);
		List<Future<ResultHandleT<OrdOrder>>> result = new Vector<Future<ResultHandleT<OrdOrder>>>();
		for(int i=0;i<1;i++){
			final int pos = i;
			Future<ResultHandleT<OrdOrder>> call = executor.submit(new Callable<ResultHandleT<OrdOrder>>() {

				@Override
				public ResultHandleT<OrdOrder> call() throws Exception {
					BuyInfo bb = org.apache.commons.lang3.SerializationUtils.clone(buyInfo);
//					bb.setIp("192.168.0.1"+pos);
					ResultHandleT<OrdOrder> result = bookService.createOrder(bb, "admin");
					return result;
				}
				
			});
			result.add(call);
		}
		int success = 0;
		int fail=0;
		try{
		for(Future<ResultHandleT<OrdOrder>> f:result){
			try {
				ResultHandleT<OrdOrder> handle = f.get();
				if(handle.isSuccess()){
					success++;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				fail++;
			}
		}
		}finally{
			System.out.println("success:"+success+"    fail:"+fail);
		}
	}
	@Autowired
	private IOrderStockService orderStockService;
	
	@Autowired
	private IOrderPriceService orderPriceService;
	
	//@Test
	public void testSavePerson(){
		Long orderId=31841L;
		ResultHandle handle = bookService.saveOrderPerson(orderId, buyInfo);
		
		Assert.assertNotNull(handle);
		System.out.println(handle.getMsg());
		Assert.assertTrue(handle.isSuccess());
		
	}

	public void test2(){
		OrdOrder order = new OrdOrder();
		
		List<OrdOrderPack> orderPackList = order.getOrderPackList();
		List<OrdOrderItem> itemList = order.getOrderItemList();
		
		Map<Long,List<OrdOrderItem>> map = new HashMap<Long, List<OrdOrderItem>>();
		if(CollectionUtils.isNotEmpty(orderPackList)){
			for(OrdOrderItem orderItem:itemList){
				if(orderItem.getOrderPackId()!=null){
					List<OrdOrderItem> list =null;
					if(map.containsKey(orderItem.getOrderPackId())){
						list = map.get(orderItem.getOrderPackId());
					}else{
						list = new ArrayList<OrdOrderItem>();
						map.put(orderItem.getOrderPackId(), list);
					}
					list.add(orderItem);
				}
			}
		}
		for(OrdOrderPack pack:orderPackList){
			pack.setOrderItemList(map.get(pack.getOrderPackId()));
		}
		//
	}
}
