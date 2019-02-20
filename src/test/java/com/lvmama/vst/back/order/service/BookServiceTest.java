package com.lvmama.vst.back.order.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prom.service.PromotionService;
import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdGuaranteeCreditCard;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.back.supp.po.SuppCreditCardValidate;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.HotelAdditation;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.BuyInfo.ItemPersonRelation;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.GuaranteeCreditCard;
import com.lvmama.vst.comm.vo.order.OrderFlagParam;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.order.service.IBookService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.impl.OrderEcontractGeneratorService;
import com.lvmama.vst.pet.adapter.IOrdUserOrderServiceAdapter;
import com.lvmama.vst.supp.client.elong.service.SuppCommonClientService;

public class BookServiceTest extends OrderTestBase {
	
	private OrderService orderService = null;

	private IBookService bookService = null;

	private IOrderUpdateService orderUpdateService = null;
	
	private IComplexQueryService complexQueryService = null;
	
//	private PromPromotionDao promPromotionDao;
	private PromotionService promotionService;
	
	private IOrdUserOrderServiceAdapter ordUserOrderService;
	
	protected SuppCommonClientService suppCommonClientService;
	
	private OrderEcontractGeneratorService orderEcontractGeneratorService;
	
	private static String name1 = "促销单元测试1(关联数据，请勿修改)";
	private static String name2 = "促销单元测试2(关联数据，请勿修改)";
	private static String name3 = "促销单元测试3(关联数据，请勿修改)";
	private static String name4 = "促销单元测试4(关联数据，请勿修改)";
	private static String name5 = "促销单元测试5(关联数据，请勿修改)";
	
	@Before
	public void prepare() {
		super.prepare();
		if (applicationContext != null) {
//			bookService = (IBookService) applicationContext.getBean("orderCreateService");
//			orderUpdateService = (IOrderUpdateService) applicationContext.getBean("ordOrderUpdateService");
//			complexQueryService = (IComplexQueryService) applicationContext.getBean("complexQueryService");
////			promPromotionDao = (PromPromotionDao) applicationContext.getBean("promPromotionDao");
//			promotionService=(PromotionService)applicationContext.getBean("promotionService");
//			ordUserOrderService = (IOrdUserOrderServiceAdapter) applicationContext.getBean("ordUserOrderService");
//			suppCommonClientService  = (SuppCommonClientService) applicationContext.getBean("suppCommonClientRemote");
//			orderEcontractGeneratorService = (OrderEcontractGeneratorService) applicationContext.getBean("orderEcontractGeneratorService");
//			orderService = (OrderService) applicationContext.getBean("orderServiceRemote");
			
			orderEcontractGeneratorService = (OrderEcontractGeneratorService) applicationContext.getBean("orderEcontractGeneratorService");
		}
	}
	
	@Test
	public void testSuit() {
//		testCreateHotelOrder();
//		testCreateHotelOrderWithPromotionPrice();
//		testCreateHotelOrderWithPromotionSupplierPrice();
//		testCreateFreeSaleHotelOrder();
//		testGranteeCard();
//		testCreateCombCuriseOrder();
//		testCreateCombCuriseOrder2();
		testGenerateCombCuriseContract();
//		testCreateCombCuriseOrder3();
//		testCreateCombCuriseOrder4();
	}
	
//	@Test
	public void testCreateHotelOrder() {
		BuyInfo buyInfo = new BuyInfo();
		
		//联系人
		Person contact = new Person();
		contact.setBirthday("1992-10-2");
		contact.setEmail("test_zhou@sina.com");
		contact.setFax("02186337611");
		contact.setFirstName("涛");
		contact.setGender("男");
		contact.setIdNo("320199210027655");
		//身份证
		contact.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		contact.setLastName("周");
		contact.setMobile("15832669989");
		contact.setNationality("中国");
		contact.setPhone("02186337611");
		buyInfo.setContact(contact);
		
		//游玩人
		Person person = new Person();
		person.setBirthday("1995-11-2");
		person.setEmail("test2_zhou@sina.com");
		person.setFax("02186337623");
		person.setFirstName("华");
		person.setGender("女");
		person.setIdNo("320199511023267");
		//身份证
		person.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		person.setLastName("刘");
		person.setMobile("15836787766");
		person.setNationality("中国");
		person.setPhone("02186337623");
		List<Person> travellers = new ArrayList<Person>();
//		travellers.add(contact);
		travellers.add(person);
		buyInfo.setTravellers(travellers);
		
		//分销商ID
		buyInfo.setDistributionId(1L);
//		buyInfo.setDistributionId(3L);
		//分销商代码
		buyInfo.setDistributorCode("WH001");
		
		//担保信息
		GuaranteeCreditCard guaranteeCreditCard = new GuaranteeCreditCard();
		guaranteeCreditCard.setCvv("000");
		guaranteeCreditCard.setExpirationMonth(8L);
		guaranteeCreditCard.setExpirationYear(2016L);
		guaranteeCreditCard.setHolderName("周涛");
		guaranteeCreditCard.setIdNo("320199210027655");
		//身份证
		guaranteeCreditCard.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		guaranteeCreditCard.setCardNo("4033910000000000");
		buyInfo.setGuarantee(guaranteeCreditCard);
		//担保
		buyInfo.setNeedGuarantee(OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name());
		
		//是否发票
		buyInfo.setNeedInvoice(OrderEnum.NEED_INVOICE_STATUS.UNBILL.name());
		
		List<Item> itemList = new ArrayList<Item>();
		Item item = new Item();
		//商品ID
//		item.setGoodsId(220333L);
		item.setGoodsId(448290L);
		item.setQuantity(1);
		item.setVisitTime("2014-05-22");
		HotelAdditation additation = new HotelAdditation();
		additation.setArrivalTime("22:00");
		additation.setEarlyArrivalTime("20:00");
		additation.setLeaveTime("2014-05-24");
		item.setHotelAdditation(additation);
		item.setMainItem("true");
		itemList.add(item);
		
//		item = new Item();
//		//商品ID
//		item.setGoodsId(215861L);
////		item.setGoodsId(213194L);
//		item.setQuantity(1);
//		item.setVisitTime("2013-12-20");
//		additation = new HotelAdditation();
//		additation.setArrivalTime("18:30");
//		additation.setEarlyArrivalTime("15:30");
//		additation.setLeaveTime("2013-12-22");
//		item.setHotelAdditation(additation);
//		item.setMainItem("false");
//		itemList.add(item);
		
		buyInfo.setItemList(itemList);
		buyInfo.setIp("220.181.111.85");
		
//		buyInfo.setRemark("加一个床");
		
 	 	buyInfo.setUserId("ff8080812e56d5ea012e570941f800be");
		
//		ResultHandle resultHandle = orderService.checkStock(buyInfo);
//		
//		System.out.println("resultHandle.Msg=" + resultHandle.getMsg());
//		if (resultHandle.isFail()) {
//			return;
//		}
//		
//		assertTrue(resultHandle.isSuccess());
 	 	ResultHandleT<OrdOrder> resultMessage = orderService.createOrder(buyInfo, "Operator001");
//		ResultHandleT<OrdOrder> resultMessage = bookService.createOrder(buyInfo, "Operator001");
		
		System.out.println("errMsg=" + resultMessage.getMsg());
		
		//操作结果验证
		OrdOrder newOrder = resultMessage.getReturnContent();
		assertTrue(resultMessage.isSuccess());
		assertTrue(newOrder != null);
		
		//查询订单验证数据
		OrdOrder queryOrder = null;
		OrdOrderItem newOrderItem = null;
		OrdOrderItem queryOrderItem = null;
		OrdOrderHotelTimeRate newOrderHotelTimeRate = null;
		OrdOrderHotelTimeRate queryOrderHotelTimeRate = null;
		OrdOrderStock newOrderStock = null;
		OrdOrderStock queryOrderStock = null;
		OrdGuaranteeCreditCard newCard = null;
		OrdGuaranteeCreditCard queryCard = null;
		OrdPerson newPerson = null;
		OrdPerson queryPerson = null;
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(newOrder.getOrderId());
		
		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderGuaranteeCreditCardTableFlag(true);
		orderFlagParam.setOrderHotelTimeRateTableFlag(true);
		orderFlagParam.setOrderItemTableFlag(true);
		orderFlagParam.setOrderPageFlag(false);
		orderFlagParam.setOrderPersonTableFlag(true);
		orderFlagParam.setOrderStockTableFlag(true);
		orderFlagParam.setOrderGuaranteeCreditCardTableFlag(true);
		
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);
		
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		
		//验证订单查询操作结果
		assertNotNull(orderList);
		assertTrue(orderList.size() == 1);
		queryOrder = orderList.get(0);
		
		
		//订单验证
		assertEquals(newOrder.getOrderId(), queryOrder.getOrderId());
		assertEquals(newOrder.getDistributorCode(), queryOrder.getDistributorCode());
		assertEquals(newOrder.getDistributorId(), queryOrder.getDistributorId());
		
		//订单子项验证
		assertNotNull(newOrder.getOrderItemList());
		assertNotNull(queryOrder.getOrderItemList());
		assertEquals(newOrder.getOrderItemList().size(), queryOrder.getOrderItemList().size());
		
		for (int i = 0; i < newOrder.getOrderItemList().size(); i++) {
			newOrderItem = newOrder.getOrderItemList().get(i);
			queryOrderItem = queryOrder.getOrderItemList().get(i);
			
			assertEquals(newOrderItem.getOrderItemId(), queryOrderItem.getOrderItemId());
			assertEquals(newOrderItem.getBranchId(), queryOrderItem.getBranchId());
			assertEquals(newOrderItem.getCategoryId(), queryOrderItem.getCategoryId());
			assertEquals(newOrderItem.getContractId(), queryOrderItem.getContractId());
			
			assertNotNull(newOrderItem.getOrderHotelTimeRateList());
			assertNotNull(queryOrderItem.getOrderHotelTimeRateList());
			assertEquals(newOrderItem.getOrderHotelTimeRateList().size(), queryOrderItem.getOrderHotelTimeRateList().size());
			
			//验证酒店每天使用情况
			for (int j = 0; j < newOrderItem.getOrderHotelTimeRateList().size(); j++) {
				newOrderHotelTimeRate = newOrderItem.getOrderHotelTimeRateList().get(j);
				queryOrderHotelTimeRate = queryOrderItem.getOrderHotelTimeRateList().get(j);
				
				assertEquals(newOrderHotelTimeRate.getHotelTimeRateId(), queryOrderHotelTimeRate.getHotelTimeRateId());
				
				assertNotNull(newOrderHotelTimeRate.getOrderStockList());
				assertNotNull(queryOrderHotelTimeRate.getOrderStockList());
				assertEquals(newOrderHotelTimeRate.getOrderStockList().size(), queryOrderHotelTimeRate.getOrderStockList().size());
				
				//验证订单本地库存项
				for (int k = 0; k < newOrderHotelTimeRate.getOrderStockList().size(); k++) {
					newOrderStock = newOrderHotelTimeRate.getOrderStockList().get(k);
					queryOrderStock = queryOrderHotelTimeRate.getOrderStockList().get(k);
					
					assertEquals(newOrderStock.getOrderStockId(), queryOrderStock.getOrderStockId());
				}
			}
		}
		
		//验证信用卡信息
		assertNotNull(newOrder.getOrdGuaranteeCreditCardList());
		assertNotNull(queryOrder.getOrdGuaranteeCreditCardList());
		assertEquals(newOrder.getOrdGuaranteeCreditCardList().size(), queryOrder.getOrdGuaranteeCreditCardList().size());
		
		for (int m = 0; m < newOrder.getOrdGuaranteeCreditCardList().size(); m++) {
			newCard = newOrder.getOrdGuaranteeCreditCardList().get(m);
			queryCard = queryOrder.getOrdGuaranteeCreditCardList().get(m);
			
			assertEquals(newCard.getOrdGuaranteeCreditCardId(), queryCard.getOrdGuaranteeCreditCardId());
		}
		
		//验证联系人、游玩人
		assertNotNull(newOrder.getOrdPersonList());
		assertNotNull(queryOrder.getOrdPersonList());
		assertEquals(newOrder.getOrdPersonList().size(), queryOrder.getOrdPersonList().size());
		
		for (int n = 0; n < newOrder.getOrdPersonList().size(); n++) {
			newPerson = newOrder.getOrdPersonList().get(n);
			queryPerson = queryOrder.getOrdPersonList().get(n);
			
			assertEquals(newPerson.getOrdPersonId(), queryPerson.getOrdPersonId());
			assertEquals(newPerson.getPersonType(), queryPerson.getPersonType());
		}
	}
	
//	@Test
	public void testCreateFailHotelOrder() {
		BuyInfo buyInfo = new BuyInfo();
		
		//联系人
		Person contact = new Person();
		contact.setBirthday("1992-10-2");
		contact.setEmail("test_zhou@sina.com");
		contact.setFax("02186337611");
		contact.setFirstName("涛");
		contact.setGender("男");
		contact.setIdNo("320199210027655");
		contact.setIdType("身份证");
		contact.setLastName("周");
		contact.setMobile("15832669989");
		contact.setNationality("中国");
		contact.setPhone("02186337611");
		buyInfo.setContact(contact);
		
		//游玩人
		Person person = new Person();
		person.setBirthday("1995-11-2");
		person.setEmail("test2_zhou@sina.com");
		person.setFax("02186337623");
		person.setFirstName("敏");
		person.setGender("女");
		person.setIdNo("320199511023267");
		person.setIdType("身份证");
		person.setLastName("刘");
		person.setMobile("15836787766");
		person.setNationality("中国");
		person.setPhone("02186337623");
		List<Person> travellers = new ArrayList<Person>();
		travellers.add(contact);
		travellers.add(person);
		buyInfo.setTravellers(travellers);
		
		//分销商ID
		buyInfo.setDistributionId(6L);
		//分销商代码
		buyInfo.setDistributorCode("WH001");
		
		//担保信息
		GuaranteeCreditCard guaranteeCreditCard = new GuaranteeCreditCard();
		guaranteeCreditCard.setCvv("236");
		guaranteeCreditCard.setExpirationMonth(8L);
		guaranteeCreditCard.setExpirationYear(2016L);
		guaranteeCreditCard.setHolderName("周涛");
		guaranteeCreditCard.setIdNo("320199210027655");
		guaranteeCreditCard.setIdType("身份证");
		guaranteeCreditCard.setCardNo("5673901285116723");
		buyInfo.setGuarantee(guaranteeCreditCard);
		//担保
		buyInfo.setNeedGuarantee(OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.toString());
		
		//是否发票
		buyInfo.setNeedInvoice("否");
		
		List<Item> itemList = new ArrayList<Item>();
		Item item = new Item();
		//商品ID
		item.setGoodsId(89L);
		item.setQuantity(2);
		item.setVisitTime("2013-12-3 18:30:00");
		HotelAdditation additation = new HotelAdditation();
		additation.setArrivalTime("2013-12-3 18:30:00");
		additation.setEarlyArrivalTime("2013-12-3 15:30:00");
		additation.setLeaveTime("2013-12-5 10:30:00");
		item.setHotelAdditation(additation);
		itemList.add(item);
		
		item = new Item();
		//商品ID
		item.setGoodsId(15L);
		item.setQuantity(2);
		item.setVisitTime("2013-12-15 18:30:00");
		additation = new HotelAdditation();
		additation.setArrivalTime("2013-12-15 18:30:00");
		additation.setEarlyArrivalTime("2013-12-15 15:30:00");
		additation.setLeaveTime("2013-12-16 10:30:00");
		item.setHotelAdditation(additation);
		itemList.add(item);
		
		buyInfo.setItemList(itemList);
		
		buyInfo.setRemark("要有阳台的房间");
		
		buyInfo.setUserId("User002");
		
		ResultHandleT<OrdOrder> resultMessage = bookService.createOrder(buyInfo, "Opertor002");
		
		assertTrue(resultMessage.isFail());
		assertTrue(resultMessage.getReturnContent() == null);
	}
	
//	@Test
//	public void testCreateHotelOrderNoGuarantee() {
//		BuyInfo buyInfo = new BuyInfo();
//		
//		//联系人
//		Person contact = new Person();
//		contact.setBirthday("1992-10-2");
//		contact.setEmail("test_zhou@sina.com");
//		contact.setFax("02186337611");
//		contact.setFirstName("涛");
//		contact.setGender("男");
//		contact.setIdNo("320199210027655");
//		contact.setIdType("身份证");
//		contact.setLastName("周");
//		contact.setMobile("15832669989");
//		contact.setNationality("中国");
//		contact.setPhone("02186337611");
//		buyInfo.setContact(contact);
//		
//		//游玩人
//		Person person = new Person();
//		contact.setBirthday("1995-11-2");
//		contact.setEmail("test2_zhou@sina.com");
//		contact.setFax("02186337623");
//		contact.setFirstName("敏");
//		contact.setGender("女");
//		contact.setIdNo("320199511023267");
//		contact.setIdType("身份证");
//		contact.setLastName("刘");
//		contact.setMobile("15836787766");
//		contact.setNationality("中国");
//		contact.setPhone("02186337623");
//		List<Person> travellers = new ArrayList<Person>();
//		travellers.add(contact);
//		travellers.add(person);
//		buyInfo.setTravellers(travellers);
//		
//		//分销商ID
//		buyInfo.setDistributionId(1L);
//		//分销商代码
//		buyInfo.setDistributorCode("WH001");
//		
//		//担保信息
//		buyInfo.setNeedGuarantee("不担保");
//		
//		//是否发票
//		buyInfo.setNeedInvoice("否");
//		
//		List<Item> itemList = new ArrayList<Item>();
//		Item item = new Item();
//		item.setGoodsId(1L);
//		item.setQuantity(2);
//		item.setVisitTime("2013-12-3 18:30:00");
//		HotelAdditation additation = new HotelAdditation();
//		additation.setArrivalTime("2013-12-3 18:30:00");
//		additation.setEarlyArrivalTime("2013-12-3 15:30:00");
//		additation.setLeaveTime("2013-12-5 10:30:00");
//		item.setHotelAdditation(additation);
//		itemList.add(item);
//		
//		item = new Item();
//		item.setGoodsId(1L);
//		item.setQuantity(2);
//		item.setVisitTime("2013-12-15 18:30:00");
//		additation = new HotelAdditation();
//		additation.setArrivalTime("2013-12-15 18:30:00");
//		additation.setEarlyArrivalTime("2013-12-15 15:30:00");
//		additation.setLeaveTime("2013-12-6 10:30:00");
//		item.setHotelAdditation(additation);
//		itemList.add(item);
//		
//		buyInfo.setItemList(itemList);
//		
//		buyInfo.setRemark("加一个床");
//		
//		buyInfo.setUserId("User001");
//		
//		ResultHandleT<OrdOrder> resultMessage = bookService.createOrder(buyInfo, "Opertor001");
//		
//		assertTrue(resultMessage.isSuccess());
//	}
//	
//	@Test
//	public void testCreateOtherOrder() {
//		BuyInfo buyInfo = new BuyInfo();
//		
//		//联系人
//		Person contact = new Person();
//		contact.setBirthday("1992-10-2");
//		contact.setEmail("test_zhou@sina.com");
//		contact.setFax("02186337611");
//		contact.setFirstName("涛");
//		contact.setGender("男");
//		contact.setIdNo("320199210027655");
//		contact.setIdType("身份证");
//		contact.setLastName("周");
//		contact.setMobile("15832669989");
//		contact.setNationality("中国");
//		contact.setPhone("02186337611");
//		buyInfo.setContact(contact);
//		
//		//游玩人
//		Person person = new Person();
//		contact.setBirthday("1995-11-2");
//		contact.setEmail("test2_zhou@sina.com");
//		contact.setFax("02186337623");
//		contact.setFirstName("敏");
//		contact.setGender("女");
//		contact.setIdNo("320199511023267");
//		contact.setIdType("身份证");
//		contact.setLastName("刘");
//		contact.setMobile("15836787766");
//		contact.setNationality("中国");
//		contact.setPhone("02186337623");
//		List<Person> travellers = new ArrayList<Person>();
//		travellers.add(contact);
//		travellers.add(person);
//		buyInfo.setTravellers(travellers);
//		
//		//分销商ID
//		buyInfo.setDistributionId(1L);
//		//分销商代码
//		buyInfo.setDistributorCode("WH001");
//		
//		//担保信息
//		GuaranteeCreditCard guaranteeCreditCard = new GuaranteeCreditCard();
//		guaranteeCreditCard.setCvv("236");
//		guaranteeCreditCard.setExpirationMonth(8L);
//		guaranteeCreditCard.setExpirationYear(2016L);
//		guaranteeCreditCard.setHolderName("周涛");
//		guaranteeCreditCard.setIdNo("320199210027655");
//		guaranteeCreditCard.setIdType("身份证");
//		guaranteeCreditCard.setNumber("500");
//		buyInfo.setGuarantee(guaranteeCreditCard);
//		buyInfo.setNeedGuarantee("担保");
//		
//		//是否发票
//		buyInfo.setNeedInvoice("否");
//		
//		List<Item> itemList = new ArrayList<Item>();
//		Item item = new Item();
//		item.setGoodsId(2L);
//		item.setQuantity(2);
//		item.setVisitTime("2013-12-3 18:30:00");
//		itemList.add(item);
//		
//		item = new Item();
//		item.setGoodsId(2L);
//		item.setQuantity(2);
//		item.setVisitTime("2013-12-15 18:30:00");
//		itemList.add(item);
//		
//		buyInfo.setItemList(itemList);
//		
//		buyInfo.setRemark("加一个床");
//		
//		buyInfo.setUserId("User001");
//		
//		ResultHandleT<OrdOrder> resultMessage = bookService.createOrder(buyInfo, "Opertor001");
//		
//		assertTrue(resultMessage.isSuccess());
//	}
//	
//	@Test
//	public void testCreateOtherOrderNoGuarantee() {
//		BuyInfo buyInfo = new BuyInfo();
//		
//		//联系人
//		Person contact = new Person();
//		contact.setBirthday("1992-10-2");
//		contact.setEmail("test_zhou@sina.com");
//		contact.setFax("02186337611");
//		contact.setFirstName("涛");
//		contact.setGender("男");
//		contact.setIdNo("320199210027655");
//		contact.setIdType("身份证");
//		contact.setLastName("周");
//		contact.setMobile("15832669989");
//		contact.setNationality("中国");
//		contact.setPhone("02186337611");
//		buyInfo.setContact(contact);
//		
//		//游玩人
//		Person person = new Person();
//		contact.setBirthday("1995-11-2");
//		contact.setEmail("test2_zhou@sina.com");
//		contact.setFax("02186337623");
//		contact.setFirstName("敏");
//		contact.setGender("女");
//		contact.setIdNo("320199511023267");
//		contact.setIdType("身份证");
//		contact.setLastName("刘");
//		contact.setMobile("15836787766");
//		contact.setNationality("中国");
//		contact.setPhone("02186337623");
//		List<Person> travellers = new ArrayList<Person>();
//		travellers.add(contact);
//		travellers.add(person);
//		buyInfo.setTravellers(travellers);
//		
//		//分销商ID
//		buyInfo.setDistributionId(1L);
//		//分销商代码
//		buyInfo.setDistributorCode("WH001");
//		
//		//担保信息
//		buyInfo.setNeedGuarantee("不担保");
//		
//		//是否发票
//		buyInfo.setNeedInvoice("否");
//		
//		List<Item> itemList = new ArrayList<Item>();
//		Item item = new Item();
//		item.setGoodsId(2L);
//		item.setQuantity(2);
//		item.setVisitTime("2013-12-3 18:30:00");
//		itemList.add(item);
//		
//		item = new Item();
//		item.setGoodsId(2L);
//		item.setQuantity(2);
//		item.setVisitTime("2013-12-15 18:30:00");
//		itemList.add(item);
//		
//		buyInfo.setItemList(itemList);
//		
//		buyInfo.setRemark("加一个床");
//		
//		buyInfo.setUserId("User001");
//		
//		ResultHandleT<OrdOrder> resultMessage = bookService.createOrder(buyInfo, "Opertor001");
//		
//		assertTrue(resultMessage.isSuccess());
//	}
	
//	@Test
	public void testCreateHotelOrder100Capability() {
		BuyInfo buyInfo = null;
		Person contact = null;
		Person person =  null;
		GuaranteeCreditCard guaranteeCreditCard = null;
		List<Item> itemList = null;
		
		System.out.println("------------------------------CreateHotelOrder100Capability  Start-----------------------------");
		Long startMillis = System.currentTimeMillis();
		
		for(int i = 0; i < 100; i++) {
			buyInfo = new BuyInfo();
			
			//联系人
			contact = new Person();
			contact.setBirthday("1992-10-2");
			contact.setEmail("test_zhou@sina.com");
			contact.setFax("02186337611");
			contact.setFirstName("涛");
			contact.setGender("男");
			contact.setIdNo("320199210027655");
			//身份证
			contact.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
			contact.setLastName("周");
			contact.setMobile("15832669989");
			contact.setNationality("中国");
			contact.setPhone("02186337611");
			buyInfo.setContact(contact);
			
			//游玩人
			person = new Person();
			person.setBirthday("1995-11-2");
			person.setEmail("test2_zhou@sina.com");
			person.setFax("02186337623");
			person.setFirstName("Carrie");
			person.setGender("女");
			person.setIdNo("320199511023267");
			person.setIdType("");
			//身份证
			person.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
			person.setLastName("Liu");
			person.setMobile("15836787766");
			person.setNationality("中国");
			person.setPhone("02186337623");
			List<Person> travellers = new ArrayList<Person>();
			travellers.add(contact);
			travellers.add(person);
			buyInfo.setTravellers(travellers);
			
			//分销商ID
			buyInfo.setDistributionId(6L);
			//分销商代码
			buyInfo.setDistributorCode("WH001");
			
			//担保信息
			guaranteeCreditCard = new GuaranteeCreditCard();
			guaranteeCreditCard.setCvv("236");
			guaranteeCreditCard.setExpirationMonth(8L);
			guaranteeCreditCard.setExpirationYear(2016L);
			guaranteeCreditCard.setHolderName("周涛");
			guaranteeCreditCard.setIdNo("320199210027655");
			//身份证
			guaranteeCreditCard.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
			guaranteeCreditCard.setCardNo("5673901285116723");
			buyInfo.setGuarantee(guaranteeCreditCard);
			//担保
			buyInfo.setNeedGuarantee(OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.toString());
			
			//是否发票
			buyInfo.setNeedInvoice(OrderEnum.NEED_INVOICE_STATUS.UNBILL.name());
			
			itemList = new ArrayList<Item>();
			Item item = new Item();
			//商品ID
			item.setGoodsId(112L);
			item.setQuantity(2);
			item.setVisitTime("2013-12-3 18:30:00");
			HotelAdditation additation = new HotelAdditation();
			additation.setArrivalTime("2013-12-3 18:30:00");
			additation.setEarlyArrivalTime("2013-12-3 15:30:00");
			additation.setLeaveTime("2013-12-5 10:30:00");
			item.setHotelAdditation(additation);
			item.setMainItem("true");
			itemList.add(item);
			
			item = new Item();
			//商品ID
			item.setGoodsId(112L);
			item.setQuantity(2);
			item.setVisitTime("2013-12-15 18:30:00");
			additation = new HotelAdditation();
			additation.setArrivalTime("2013-12-15 18:30:00");
			additation.setEarlyArrivalTime("2013-12-15 15:30:00");
			additation.setLeaveTime("2013-12-16 10:30:00");
			item.setHotelAdditation(additation);
			item.setMainItem("false");
			itemList.add(item);
			
			buyInfo.setItemList(itemList);
			
			buyInfo.setRemark("加一个床");
			
			buyInfo.setUserId("User001");
			
			ResultHandleT<OrdOrder> resultMessage = bookService.createOrder(buyInfo, "Operator001");
			
			//操作结果验证
			OrdOrder newOrder = resultMessage.getReturnContent();
			assertTrue(resultMessage.isSuccess());
			assertTrue(newOrder != null);
		}
		
		Long endMillis = System.currentTimeMillis();
		System.out.println("------------------------------CreateHotelOrder100Capability End-----------------------------");
		System.out.printf("------------------------------Total Millis = %dMillis-----------------------------\n", endMillis - startMillis);
		
	}
	
//	@Test
	public void testCreateHotelOrderWithPromotionPrice() {
		BuyInfo buyInfo = new BuyInfo();
		
		//联系人
		Person contact = new Person();
		contact.setBirthday("1992-10-2");
		contact.setEmail("test_zhou@sina.com");
		contact.setFax("02186337611");
		contact.setFirstName("涛");
		contact.setGender("男");
		contact.setIdNo("320199210027655");
		//身份证
		contact.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		contact.setLastName("周");
		contact.setMobile("15832669989");
		contact.setNationality("中国");
		contact.setPhone("02186337611");
		buyInfo.setContact(contact);
		
		//游玩人
		Person person = new Person();
		person.setBirthday("1995-11-2");
		person.setEmail("test2_zhou@sina.com");
		person.setFax("02186337623");
		person.setFirstName("华");
		person.setGender("女");
		person.setIdNo("320199511023267");
		//身份证
		person.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		person.setLastName("刘");
		person.setMobile("15836787766");
		person.setNationality("中国");
		person.setPhone("02186337623");
		List<Person> travellers = new ArrayList<Person>();
//		travellers.add(contact);
		travellers.add(person);
		buyInfo.setTravellers(travellers);
		
		//分销商ID
		buyInfo.setDistributionId(1L);
		//分销商代码
		buyInfo.setDistributorCode("WH001");
		
		//担保信息
		GuaranteeCreditCard guaranteeCreditCard = new GuaranteeCreditCard();
		guaranteeCreditCard.setCvv("000");
		guaranteeCreditCard.setExpirationMonth(8L);
		guaranteeCreditCard.setExpirationYear(2016L);
		guaranteeCreditCard.setHolderName("周涛");
		guaranteeCreditCard.setIdNo("320199210027655");
		//身份证
		guaranteeCreditCard.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		guaranteeCreditCard.setCardNo("4033910000000000");
		buyInfo.setGuarantee(guaranteeCreditCard);
		//担保
		buyInfo.setNeedGuarantee(OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name());
		
		//是否发票
		buyInfo.setNeedInvoice(OrderEnum.NEED_INVOICE_STATUS.UNBILL.name());
		
		List<Item> itemList = new ArrayList<Item>();
		Item item = new Item();
		//商品ID
		item.setGoodsId(253230L);
		item.setQuantity(1);
		item.setVisitTime("2014-01-25");
		HotelAdditation additation = new HotelAdditation();
		additation.setArrivalTime("22:00");
		additation.setEarlyArrivalTime("20:00");
		additation.setLeaveTime("2014-01-30");
		item.setHotelAdditation(additation);
		item.setMainItem("true");
		itemList.add(item);
		
//		item = new Item();
//		//商品ID
//		item.setGoodsId(215861L);
////		item.setGoodsId(213194L);
//		item.setQuantity(1);
//		item.setVisitTime("2013-12-20");
//		additation = new HotelAdditation();
//		additation.setArrivalTime("18:30");
//		additation.setEarlyArrivalTime("15:30");
//		additation.setLeaveTime("2013-12-22");
//		item.setHotelAdditation(additation);
//		item.setMainItem("false");
//		itemList.add(item);
		
		buyInfo.setItemList(itemList);
		buyInfo.setIp("220.181.111.85");
		
//		buyInfo.setRemark("加一个床");
		
 	 	buyInfo.setUserId("ff8080812e56d5ea012e570941f800be");
 	 	
 	 	List<Long> promotionIdList = getPromotionIdListLikeName(name1);
 	 	buyInfo.setPromotionIdList(promotionIdList);
		
//		ResultHandle resultHandle = orderService.checkStock(buyInfo);
//		
//		System.out.println("resultHandle.Msg=" + resultHandle.getMsg());
//		if (resultHandle.isFail()) {
//			return;
//		}
//		
//		assertTrue(resultHandle.isSuccess());
		
		ResultHandleT<OrdOrder> resultMessage = bookService.createOrder(buyInfo, "Operator001");
		
		System.out.println("errMsg=" + resultMessage.getMsg());
		
		//操作结果验证
		OrdOrder newOrder = resultMessage.getReturnContent();
		assertTrue(resultMessage.isSuccess());
		assertTrue(newOrder != null);
		
		//查询订单验证数据
		OrdOrder queryOrder = null;
		OrdOrderItem newOrderItem = null;
		OrdOrderItem queryOrderItem = null;
		OrdOrderHotelTimeRate newOrderHotelTimeRate = null;
		OrdOrderHotelTimeRate queryOrderHotelTimeRate = null;
		OrdOrderStock newOrderStock = null;
		OrdOrderStock queryOrderStock = null;
		OrdGuaranteeCreditCard newCard = null;
		OrdGuaranteeCreditCard queryCard = null;
		OrdPerson newPerson = null;
		OrdPerson queryPerson = null;
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(newOrder.getOrderId());
		
		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderGuaranteeCreditCardTableFlag(true);
		orderFlagParam.setOrderHotelTimeRateTableFlag(true);
		orderFlagParam.setOrderItemTableFlag(true);
		orderFlagParam.setOrderPageFlag(false);
		orderFlagParam.setOrderPersonTableFlag(true);
		orderFlagParam.setOrderStockTableFlag(true);
		orderFlagParam.setOrderGuaranteeCreditCardTableFlag(true);
		
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);
		
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		
		//验证订单查询操作结果
		assertNotNull(orderList);
		assertTrue(orderList.size() == 1);
		queryOrder = orderList.get(0);
		
		//订单验证
		assertEquals(newOrder.getOrderId(), queryOrder.getOrderId());
		assertEquals(newOrder.getDistributorCode(), queryOrder.getDistributorCode());
		assertEquals(newOrder.getDistributorId(), queryOrder.getDistributorId());
		
		//订单子项验证
		assertNotNull(newOrder.getOrderItemList());
		assertNotNull(queryOrder.getOrderItemList());
		assertEquals(newOrder.getOrderItemList().size(), queryOrder.getOrderItemList().size());
		
		for (int i = 0; i < newOrder.getOrderItemList().size(); i++) {
			newOrderItem = newOrder.getOrderItemList().get(i);
			queryOrderItem = queryOrder.getOrderItemList().get(i);
			
			assertEquals(newOrderItem.getOrderItemId(), queryOrderItem.getOrderItemId());
			assertEquals(newOrderItem.getBranchId(), queryOrderItem.getBranchId());
			assertEquals(newOrderItem.getCategoryId(), queryOrderItem.getCategoryId());
			assertEquals(newOrderItem.getContractId(), queryOrderItem.getContractId());
			
			assertNotNull(newOrderItem.getOrderHotelTimeRateList());
			assertNotNull(queryOrderItem.getOrderHotelTimeRateList());
			assertEquals(newOrderItem.getOrderHotelTimeRateList().size(), queryOrderItem.getOrderHotelTimeRateList().size());
			
			//验证酒店每天使用情况
			for (int j = 0; j < newOrderItem.getOrderHotelTimeRateList().size(); j++) {
				newOrderHotelTimeRate = newOrderItem.getOrderHotelTimeRateList().get(j);
				queryOrderHotelTimeRate = queryOrderItem.getOrderHotelTimeRateList().get(j);
				
				assertEquals(newOrderHotelTimeRate.getHotelTimeRateId(), queryOrderHotelTimeRate.getHotelTimeRateId());
				
				assertNotNull(newOrderHotelTimeRate.getOrderStockList());
				assertNotNull(queryOrderHotelTimeRate.getOrderStockList());
				assertEquals(newOrderHotelTimeRate.getOrderStockList().size(), queryOrderHotelTimeRate.getOrderStockList().size());
				
				//验证订单本地库存项
				for (int k = 0; k < newOrderHotelTimeRate.getOrderStockList().size(); k++) {
					newOrderStock = newOrderHotelTimeRate.getOrderStockList().get(k);
					queryOrderStock = queryOrderHotelTimeRate.getOrderStockList().get(k);
					
					assertEquals(newOrderStock.getOrderStockId(), queryOrderStock.getOrderStockId());
				}
			}
		}
		
		//验证信用卡信息
		assertNotNull(newOrder.getOrdGuaranteeCreditCardList());
		assertNotNull(queryOrder.getOrdGuaranteeCreditCardList());
		assertEquals(newOrder.getOrdGuaranteeCreditCardList().size(), queryOrder.getOrdGuaranteeCreditCardList().size());
		
		for (int m = 0; m < newOrder.getOrdGuaranteeCreditCardList().size(); m++) {
			newCard = newOrder.getOrdGuaranteeCreditCardList().get(m);
			queryCard = queryOrder.getOrdGuaranteeCreditCardList().get(m);
			
			assertEquals(newCard.getOrdGuaranteeCreditCardId(), queryCard.getOrdGuaranteeCreditCardId());
		}
		
		//验证联系人、游玩人
		assertNotNull(newOrder.getOrdPersonList());
		assertNotNull(queryOrder.getOrdPersonList());
		assertEquals(newOrder.getOrdPersonList().size(), queryOrder.getOrdPersonList().size());
		
		for (int n = 0; n < newOrder.getOrdPersonList().size(); n++) {
			newPerson = newOrder.getOrdPersonList().get(n);
			queryPerson = queryOrder.getOrdPersonList().get(n);
			
			assertEquals(newPerson.getOrdPersonId(), queryPerson.getOrdPersonId());
			assertEquals(newPerson.getPersonType(), queryPerson.getPersonType());
		}
	}
	
//	@Test
	public void testCreateHotelOrderWithPromotionSupplierPrice() {
		BuyInfo buyInfo = new BuyInfo();
		
		//联系人
		Person contact = new Person();
		contact.setBirthday("1992-10-2");
		contact.setEmail("test_zhou@sina.com");
		contact.setFax("02186337611");
		contact.setFirstName("涛");
		contact.setGender("男");
		contact.setIdNo("320199210027655");
		//身份证
		contact.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		contact.setLastName("周");
		contact.setMobile("15832669989");
		contact.setNationality("中国");
		contact.setPhone("02186337611");
		buyInfo.setContact(contact);
		
		//游玩人
		Person person = new Person();
		person.setBirthday("1995-11-2");
		person.setEmail("test2_zhou@sina.com");
		person.setFax("02186337623");
		person.setFirstName("华");
		person.setGender("女");
		person.setIdNo("320199511023267");
		//身份证
		person.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		person.setLastName("刘");
		person.setMobile("15836787766");
		person.setNationality("中国");
		person.setPhone("02186337623");
		List<Person> travellers = new ArrayList<Person>();
//		travellers.add(contact);
		travellers.add(person);
		buyInfo.setTravellers(travellers);
		
		//分销商ID
		buyInfo.setDistributionId(1L);
		//分销商代码
		buyInfo.setDistributorCode("WH001");
		
		//担保信息
		GuaranteeCreditCard guaranteeCreditCard = new GuaranteeCreditCard();
		guaranteeCreditCard.setCvv("000");
		guaranteeCreditCard.setExpirationMonth(8L);
		guaranteeCreditCard.setExpirationYear(2016L);
		guaranteeCreditCard.setHolderName("周涛");
		guaranteeCreditCard.setIdNo("320199210027655");
		//身份证
		guaranteeCreditCard.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		guaranteeCreditCard.setCardNo("4033910000000000");
		buyInfo.setGuarantee(guaranteeCreditCard);
		//担保
		buyInfo.setNeedGuarantee(OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name());
		
		//是否发票
		buyInfo.setNeedInvoice(OrderEnum.NEED_INVOICE_STATUS.UNBILL.name());
		
		List<Item> itemList = new ArrayList<Item>();
		Item item = new Item();
		//商品ID
		item.setGoodsId(253230L);
		item.setQuantity(1);
		item.setVisitTime("2014-01-25");
		HotelAdditation additation = new HotelAdditation();
		additation.setArrivalTime("22:00");
		additation.setEarlyArrivalTime("20:00");
		additation.setLeaveTime("2014-01-30");
		item.setHotelAdditation(additation);
		item.setMainItem("true");
		itemList.add(item);
		
//		item = new Item();
//		//商品ID
//		item.setGoodsId(215861L);
////		item.setGoodsId(213194L);
//		item.setQuantity(1);
//		item.setVisitTime("2013-12-20");
//		additation = new HotelAdditation();
//		additation.setArrivalTime("18:30");
//		additation.setEarlyArrivalTime("15:30");
//		additation.setLeaveTime("2013-12-22");
//		item.setHotelAdditation(additation);
//		item.setMainItem("false");
//		itemList.add(item);
		
		buyInfo.setItemList(itemList);
		buyInfo.setIp("220.181.111.85");
		
//		buyInfo.setRemark("加一个床");
		
 	 	buyInfo.setUserId("ff8080812e56d5ea012e570941f800be");
 	 	
//		ResultHandle resultHandle = orderService.checkStock(buyInfo);
//		
//		System.out.println("resultHandle.Msg=" + resultHandle.getMsg());
//		if (resultHandle.isFail()) {
//			return;
//		}
//		
//		assertTrue(resultHandle.isSuccess());
		
		ResultHandleT<OrdOrder> resultMessage = bookService.createOrder(buyInfo, "Operator001");
		
		System.out.println("errMsg=" + resultMessage.getMsg());
		
		//操作结果验证
		OrdOrder newOrder = resultMessage.getReturnContent();
		assertTrue(resultMessage.isSuccess());
		assertTrue(newOrder != null);
		
		//查询订单验证数据
		OrdOrder queryOrder = null;
		OrdOrderItem newOrderItem = null;
		OrdOrderItem queryOrderItem = null;
		OrdOrderHotelTimeRate newOrderHotelTimeRate = null;
		OrdOrderHotelTimeRate queryOrderHotelTimeRate = null;
		OrdOrderStock newOrderStock = null;
		OrdOrderStock queryOrderStock = null;
		OrdGuaranteeCreditCard newCard = null;
		OrdGuaranteeCreditCard queryCard = null;
		OrdPerson newPerson = null;
		OrdPerson queryPerson = null;
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(newOrder.getOrderId());
		
		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderGuaranteeCreditCardTableFlag(true);
		orderFlagParam.setOrderHotelTimeRateTableFlag(true);
		orderFlagParam.setOrderItemTableFlag(true);
		orderFlagParam.setOrderPageFlag(false);
		orderFlagParam.setOrderPersonTableFlag(true);
		orderFlagParam.setOrderStockTableFlag(true);
		orderFlagParam.setOrderGuaranteeCreditCardTableFlag(true);
		
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);
		
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		
		//验证订单查询操作结果
		assertNotNull(orderList);
		assertTrue(orderList.size() == 1);
		queryOrder = orderList.get(0);
		
		//订单验证
		assertEquals(newOrder.getOrderId(), queryOrder.getOrderId());
		assertEquals(newOrder.getDistributorCode(), queryOrder.getDistributorCode());
		assertEquals(newOrder.getDistributorId(), queryOrder.getDistributorId());
		
		//订单子项验证
		assertNotNull(newOrder.getOrderItemList());
		assertNotNull(queryOrder.getOrderItemList());
		assertEquals(newOrder.getOrderItemList().size(), queryOrder.getOrderItemList().size());
		
		for (int i = 0; i < newOrder.getOrderItemList().size(); i++) {
			newOrderItem = newOrder.getOrderItemList().get(i);
			queryOrderItem = queryOrder.getOrderItemList().get(i);
			
			assertEquals(newOrderItem.getOrderItemId(), queryOrderItem.getOrderItemId());
			assertEquals(newOrderItem.getBranchId(), queryOrderItem.getBranchId());
			assertEquals(newOrderItem.getCategoryId(), queryOrderItem.getCategoryId());
			assertEquals(newOrderItem.getContractId(), queryOrderItem.getContractId());
			
			assertNotNull(newOrderItem.getOrderHotelTimeRateList());
			assertNotNull(queryOrderItem.getOrderHotelTimeRateList());
			assertEquals(newOrderItem.getOrderHotelTimeRateList().size(), queryOrderItem.getOrderHotelTimeRateList().size());
			
			//验证酒店每天使用情况
			for (int j = 0; j < newOrderItem.getOrderHotelTimeRateList().size(); j++) {
				newOrderHotelTimeRate = newOrderItem.getOrderHotelTimeRateList().get(j);
				queryOrderHotelTimeRate = queryOrderItem.getOrderHotelTimeRateList().get(j);
				
				assertEquals(newOrderHotelTimeRate.getHotelTimeRateId(), queryOrderHotelTimeRate.getHotelTimeRateId());
				
				assertNotNull(newOrderHotelTimeRate.getOrderStockList());
				assertNotNull(queryOrderHotelTimeRate.getOrderStockList());
				assertEquals(newOrderHotelTimeRate.getOrderStockList().size(), queryOrderHotelTimeRate.getOrderStockList().size());
				
				//验证订单本地库存项
				for (int k = 0; k < newOrderHotelTimeRate.getOrderStockList().size(); k++) {
					newOrderStock = newOrderHotelTimeRate.getOrderStockList().get(k);
					queryOrderStock = queryOrderHotelTimeRate.getOrderStockList().get(k);
					
					assertEquals(newOrderStock.getOrderStockId(), queryOrderStock.getOrderStockId());
				}
			}
		}
		
		//验证信用卡信息
		assertNotNull(newOrder.getOrdGuaranteeCreditCardList());
		assertNotNull(queryOrder.getOrdGuaranteeCreditCardList());
		assertEquals(newOrder.getOrdGuaranteeCreditCardList().size(), queryOrder.getOrdGuaranteeCreditCardList().size());
		
		for (int m = 0; m < newOrder.getOrdGuaranteeCreditCardList().size(); m++) {
			newCard = newOrder.getOrdGuaranteeCreditCardList().get(m);
			queryCard = queryOrder.getOrdGuaranteeCreditCardList().get(m);
			
			assertEquals(newCard.getOrdGuaranteeCreditCardId(), queryCard.getOrdGuaranteeCreditCardId());
		}
		
		//验证联系人、游玩人
		assertNotNull(newOrder.getOrdPersonList());
		assertNotNull(queryOrder.getOrdPersonList());
		assertEquals(newOrder.getOrdPersonList().size(), queryOrder.getOrdPersonList().size());
		
		for (int n = 0; n < newOrder.getOrdPersonList().size(); n++) {
			newPerson = newOrder.getOrdPersonList().get(n);
			queryPerson = queryOrder.getOrdPersonList().get(n);
			
			assertEquals(newPerson.getOrdPersonId(), queryPerson.getOrdPersonId());
			assertEquals(newPerson.getPersonType(), queryPerson.getPersonType());
		}
	}
	
	private List<Long> getPromotionIdListLikeName(String name) {
		List<Long> promotionIdList = new ArrayList<Long>();
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		List<PromPromotion> promotionList = null;
		
		paramMap.put("title", name);
//		promotionList = promPromotionDao.selectListByParams(paramMap);
		ResultHandleT<List<PromPromotion>> resultHandle = null;// =promotionService.findPromPromotion(paramMap);
		promotionList=resultHandle.getReturnContent();
		if (promotionList != null && promotionList.size() > 0) {
			for (PromPromotion p : promotionList) {
				if (p != null) {
					promotionIdList.add(p.getPromPromotionId());
				}
			}
		}
		
		return promotionIdList;
	}
	
//	@Test
	public void testCreateFreeSaleHotelOrder() {
		BuyInfo buyInfo = new BuyInfo();
		
		//联系人
		Person contact = new Person();
		contact.setBirthday("1992-10-2");
		contact.setEmail("test_zhou@sina.com");
		contact.setFax("02186337611");
		contact.setFirstName("涛");
		contact.setGender("男");
		contact.setIdNo("320199210027655");
		//身份证
		contact.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		contact.setLastName("周");
		contact.setMobile("15832669989");
		contact.setNationality("中国");
		contact.setPhone("02186337611");
		buyInfo.setContact(contact);
		
		//游玩人
		Person person = new Person();
		person.setBirthday("1995-11-2");
		person.setEmail("test2_zhou@sina.com");
		person.setFax("02186337623");
		person.setFirstName("华");
		person.setGender("女");
		person.setIdNo("320199511023267");
		//身份证
		person.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		person.setLastName("刘");
		person.setMobile("15836787766");
		person.setNationality("中国");
		person.setPhone("02186337623");
		List<Person> travellers = new ArrayList<Person>();
		travellers.add(person);
		buyInfo.setTravellers(travellers);
		
		//分销商ID
		buyInfo.setDistributionId(1L);
		//分销商代码
		buyInfo.setDistributorCode("WH001");
		
		//担保信息
		GuaranteeCreditCard guaranteeCreditCard = new GuaranteeCreditCard();
		guaranteeCreditCard.setCvv("000");
		guaranteeCreditCard.setExpirationMonth(8L);
		guaranteeCreditCard.setExpirationYear(2016L);
		guaranteeCreditCard.setHolderName("周涛");
		guaranteeCreditCard.setIdNo("320199210027655");
		//身份证
		guaranteeCreditCard.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		guaranteeCreditCard.setCardNo("4033910000000000");
		buyInfo.setGuarantee(guaranteeCreditCard);
		//担保
		buyInfo.setNeedGuarantee(OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name());
		
		//是否发票
		buyInfo.setNeedInvoice(OrderEnum.NEED_INVOICE_STATUS.UNBILL.name());
		
		List<Item> itemList = new ArrayList<Item>();
		Item item = new Item();
		//商品ID
		item.setGoodsId(331222L);
		item.setQuantity(2);
		item.setVisitTime("2014-01-08");
		HotelAdditation additation = new HotelAdditation();
		additation.setArrivalTime("22:00");
		additation.setEarlyArrivalTime("20:00");
		additation.setLeaveTime("2014-01-10");
		item.setHotelAdditation(additation);
		item.setMainItem("true");
		itemList.add(item);
		
		buyInfo.setItemList(itemList);
		buyInfo.setIp("220.181.111.85");
		
		
 	 	buyInfo.setUserId("ff8080812e56d5ea012e570941f800be");
		
		ResultHandleT<OrdOrder> resultMessage = bookService.createOrder(buyInfo, "Operator001");
		
		System.out.println("errMsg=" + resultMessage.getMsg());
		
		//操作结果验证
		OrdOrder newOrder = resultMessage.getReturnContent();
		assertTrue(resultMessage.isSuccess());
		assertTrue(newOrder != null);
		
		//查询订单验证数据
		OrdOrder queryOrder = null;
		OrdOrderItem newOrderItem = null;
		OrdOrderItem queryOrderItem = null;
		OrdOrderHotelTimeRate newOrderHotelTimeRate = null;
		OrdOrderHotelTimeRate queryOrderHotelTimeRate = null;
		OrdOrderStock newOrderStock = null;
		OrdOrderStock queryOrderStock = null;
		OrdGuaranteeCreditCard newCard = null;
		OrdGuaranteeCreditCard queryCard = null;
		OrdPerson newPerson = null;
		OrdPerson queryPerson = null;
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(newOrder.getOrderId());
		
		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderGuaranteeCreditCardTableFlag(true);
		orderFlagParam.setOrderHotelTimeRateTableFlag(true);
		orderFlagParam.setOrderItemTableFlag(true);
		orderFlagParam.setOrderPageFlag(false);
		orderFlagParam.setOrderPersonTableFlag(true);
		orderFlagParam.setOrderStockTableFlag(true);
		orderFlagParam.setOrderGuaranteeCreditCardTableFlag(true);
		
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);
		
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		
		//验证订单查询操作结果
		assertNotNull(orderList);
		assertTrue(orderList.size() == 1);
		queryOrder = orderList.get(0);
		
		
		//订单验证
		assertEquals(newOrder.getOrderId(), queryOrder.getOrderId());
		assertEquals(newOrder.getDistributorCode(), queryOrder.getDistributorCode());
		assertEquals(newOrder.getDistributorId(), queryOrder.getDistributorId());
		
		//订单子项验证
		assertNotNull(newOrder.getOrderItemList());
		assertNotNull(queryOrder.getOrderItemList());
		assertEquals(newOrder.getOrderItemList().size(), queryOrder.getOrderItemList().size());
		
		for (int i = 0; i < newOrder.getOrderItemList().size(); i++) {
			newOrderItem = newOrder.getOrderItemList().get(i);
			queryOrderItem = queryOrder.getOrderItemList().get(i);
			
			assertEquals(newOrderItem.getOrderItemId(), queryOrderItem.getOrderItemId());
			assertEquals(newOrderItem.getBranchId(), queryOrderItem.getBranchId());
			assertEquals(newOrderItem.getCategoryId(), queryOrderItem.getCategoryId());
			assertEquals(newOrderItem.getContractId(), queryOrderItem.getContractId());
			
			assertNotNull(newOrderItem.getOrderHotelTimeRateList());
			assertNotNull(queryOrderItem.getOrderHotelTimeRateList());
			assertEquals(newOrderItem.getOrderHotelTimeRateList().size(), queryOrderItem.getOrderHotelTimeRateList().size());
			
			//验证酒店每天使用情况
			for (int j = 0; j < newOrderItem.getOrderHotelTimeRateList().size(); j++) {
				newOrderHotelTimeRate = newOrderItem.getOrderHotelTimeRateList().get(j);
				queryOrderHotelTimeRate = queryOrderItem.getOrderHotelTimeRateList().get(j);
				
				assertEquals(newOrderHotelTimeRate.getHotelTimeRateId(), queryOrderHotelTimeRate.getHotelTimeRateId());
				
				assertNotNull(newOrderHotelTimeRate.getOrderStockList());
				assertNotNull(queryOrderHotelTimeRate.getOrderStockList());
				assertEquals(newOrderHotelTimeRate.getOrderStockList().size(), queryOrderHotelTimeRate.getOrderStockList().size());
				
				//验证订单本地库存项
				for (int k = 0; k < newOrderHotelTimeRate.getOrderStockList().size(); k++) {
					newOrderStock = newOrderHotelTimeRate.getOrderStockList().get(k);
					queryOrderStock = queryOrderHotelTimeRate.getOrderStockList().get(k);
					
					assertEquals(newOrderStock.getOrderStockId(), queryOrderStock.getOrderStockId());
					assertEquals(OrderEnum.INVENTORY_STATUS.FREESALE.name(), queryOrderStock.getInventory());
				}
			}
		}
		
		//验证信用卡信息
		assertNotNull(newOrder.getOrdGuaranteeCreditCardList());
		assertNotNull(queryOrder.getOrdGuaranteeCreditCardList());
		assertEquals(newOrder.getOrdGuaranteeCreditCardList().size(), queryOrder.getOrdGuaranteeCreditCardList().size());
		
		for (int m = 0; m < newOrder.getOrdGuaranteeCreditCardList().size(); m++) {
			newCard = newOrder.getOrdGuaranteeCreditCardList().get(m);
			queryCard = queryOrder.getOrdGuaranteeCreditCardList().get(m);
			
			assertEquals(newCard.getOrdGuaranteeCreditCardId(), queryCard.getOrdGuaranteeCreditCardId());
		}
		
		//验证联系人、游玩人
		assertNotNull(newOrder.getOrdPersonList());
		assertNotNull(queryOrder.getOrdPersonList());
		assertEquals(newOrder.getOrdPersonList().size(), queryOrder.getOrdPersonList().size());
		
		for (int n = 0; n < newOrder.getOrdPersonList().size(); n++) {
			newPerson = newOrder.getOrdPersonList().get(n);
			queryPerson = queryOrder.getOrdPersonList().get(n);
			
			assertEquals(newPerson.getOrdPersonId(), queryPerson.getOrdPersonId());
			assertEquals(newPerson.getPersonType(), queryPerson.getPersonType());
		}
	}
	
//	@Test
	public void testGranteeCard() {
		ResultHandleT<SuppCreditCardValidate> resultHandle = suppCommonClientService.creditCardValidate("1234456789098711");
		if (resultHandle.isSuccess()) {
			
		}
	}
	
	public void testCreateCombCuriseOrder() {
		BuyInfo buyInfo = new BuyInfo();
		
		//联系人
		Person contact = new Person();
		contact.setBirthday("1992-10-2");
		contact.setEmail("test_zhou@sina.com");
		contact.setFax("02186337611");
		contact.setFirstName("涛");
		contact.setGender("男");
		contact.setIdNo("320199210027655");
		contact.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name());
		//身份证
		contact.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		contact.setLastName("周");
		contact.setMobile("15832669989");
		contact.setNationality("中国");
		contact.setPhone("02186337611");
		buyInfo.setContact(contact);
		
		List<Person> travellers = new ArrayList<Person>();
		
		//游玩人
		Person person1 = new Person();
		person1.setBirthday("1995-11-2");
		person1.setEmail("test2_zhou@sina.com");
		person1.setFax("02186337623");
		person1.setFirstName("华");
		person1.setGender("女");
		person1.setIdNo("320199511023267");
		person1.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name());
		//身份证
		person1.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		person1.setLastName("刘");
		person1.setMobile("15836787766");
		person1.setNationality("中国");
		person1.setPhone("02186337623");
		travellers.add(person1);
		
		//游玩人
		Person person2 = new Person();
		person2.setBirthday("1995-11-2");
		person2.setEmail("test2_zhou@sina.com");
		person2.setFax("02186337623");
		person2.setFirstName("芳");
		person2.setGender("女");
		person2.setIdNo("320199511023267");
		person2.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name());
		//身份证
		person2.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		person2.setLastName("刘");
		person2.setMobile("15836787766");
		person2.setNationality("中国");
		person2.setPhone("02186337623");
		travellers.add(person2);
		
		//游玩人
		Person person3 = new Person();
		person3.setBirthday("1995-11-2");
		person3.setEmail("test2_zhou@sina.com");
		person3.setFax("02186337623");
		person3.setFirstName("涛");
		person3.setGender("男");
		person3.setIdNo("320199511023267");
		person3.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name());
		//身份证
		person3.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		person3.setLastName("王");
		person3.setMobile("15836787766");
		person3.setNationality("中国");
		person3.setPhone("02186337623");
		travellers.add(person3);
		
		//游玩人
		Person person4 = new Person();
		person4.setBirthday("1995-11-2");
		person4.setEmail("test2_zhou@sina.com");
		person4.setFax("02186337623");
		person4.setFirstName("小涛");
		person4.setGender("男");
		person4.setIdNo("320199511023267");
		person4.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_CHILD.name());
		//身份证
		person4.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		person4.setLastName("王");
		person4.setMobile("15836787766");
		person4.setNationality("中国");
		person4.setPhone("02186337623");
		travellers.add(person4);
		
		buyInfo.setTravellers(travellers);
		
		//分销商ID
		buyInfo.setDistributionId(Constants.DISTRIBUTOR_2);
		
		//是否发票
		buyInfo.setNeedInvoice(OrderEnum.NEED_INVOICE_STATUS.UNBILL.name());
		
		buyInfo.setCategoryId(8L);
		buyInfo.setProductId(79741L);
		
		List<Item> itemList = new ArrayList<Item>();
		
		//商品ID  内舱两人间
		Item item = new Item();
		item.setGoodsId(451451L);
		item.setQuantity(1);
		item.setVisitTime("2014-07-01");
		item.setMainItem("true");
		itemList.add(item);
		List<ItemPersonRelation> itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		ItemPersonRelation itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person2);
		itemPersonRelationList.add(itemPersonRelation);
		
		//商品ID 外舱两人间
		item = new Item();
		item.setGoodsId(451450L);
		item.setQuantity(1);
		item.setVisitTime("2014-07-01");
		item.setMainItem("false");
		itemList.add(item);
		itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person3);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person4);
		itemPersonRelationList.add(itemPersonRelation);
		
		//商品ID 岸上观光
		item = new Item();
		item.setGoodsId(451390L);
		item.setQuantity(4);
		item.setVisitTime("2014-07-01");
		item.setMainItem("false");
		itemList.add(item);
		itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person2);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person3);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person4);
		itemPersonRelationList.add(itemPersonRelation);
		
		//商品ID 签证
		item = new Item();
		item.setGoodsId(452090L);
		item.setQuantity(4);
		item.setVisitTime("2014-07-01");
		item.setMainItem("false");
		itemList.add(item);
		itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person2);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person3);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person4);
		itemPersonRelationList.add(itemPersonRelation);
		
		//商品ID 签证
		item = new Item();
		item.setGoodsId(452110L);
		item.setQuantity(4);
		item.setVisitTime("2014-07-01");
		item.setMainItem("false");
		itemList.add(item);
		itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person2);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person3);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person4);
		itemPersonRelationList.add(itemPersonRelation);
		
		
		
		buyInfo.setItemList(itemList);
		buyInfo.setIp("220.181.111.85");
		
 	 	buyInfo.setUserId("ff8080812e56d5ea012e570941f800be");
 	 	
// 	 	ResultHandle resultHandle = orderService.checkStock(buyInfo);
// 	 	System.out.println("1------------------------------MSG: " + resultHandle.getMsg());
// 	 	assertTrue(resultHandle.isSuccess());
// 	 	
// 	 	ResultHandleT<OrdOrder> resultMessage = bookService.createOrder(buyInfo, "Operator001");
// 	 	System.out.println("2------------------------------MSG: " + resultMessage.getMsg());
// 	 	assertTrue(resultMessage.isSuccess());
 	 	
 	 	ResultHandleT<OrdOrder> resultMessage2 = orderService.createOrder(buyInfo, "Operator001");
 	 	System.out.println("3------------------------------MSG: " + resultMessage2.getMsg());
 	 	assertTrue(resultMessage2.isSuccess());
	}
	
	public void testGenerateCombCuriseContract() {
		orderEcontractGeneratorService.generateEcontract(7181L, "admin");
//		OrdOrder order = complexQueryService.queryOrderByOrderId(6941L);
//		
//		boolean isCanCancel = order.isCanCancel();
//		
//		boolean isWaitingPay = order.isWaitingPay();
//		
//		if (isCanCancel && isWaitingPay) {
//			System.out.println("isCanCancel && isWaitingPay");
//		} else {
//			System.out.println("no");
//		}chineseNumeralTraveAmount.length() - 2chineseNumeralTraveAmount.length() - 2chineseNumeralTraveAmount.length() - 2chineseNumeralTraveAmount.length() - 2chineseNumeralTraveAmount.length() - 2chineseNumeralTraveAmount.length() - 2
	}
	
	public void testCreateCombCuriseOrder2() {
		BuyInfo buyInfo = new BuyInfo();
		
		//联系人
		Person contact = new Person();
		contact.setBirthday("1992-10-2");
		contact.setEmail("test_zhou@sina.com");
		contact.setFax("02186337611");
		contact.setFirstName("涛");
		contact.setGender("男");
		contact.setIdNo("320199210027655");
		contact.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name());
		//身份证
		contact.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		contact.setLastName("周");
		contact.setMobile("15832669989");
		contact.setNationality("中国");
		contact.setPhone("02186337611");
		buyInfo.setContact(contact);
		
		List<Person> travellers = new ArrayList<Person>();
		
		//游玩人
		Person person1 = new Person();
		person1.setBirthday("1995-11-2");
		person1.setEmail("test2_zhou@sina.com");
		person1.setFax("02186337623");
		person1.setFirstName("华");
		person1.setGender("女");
		person1.setIdNo("320199511023267");
		person1.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name());
		//身份证
		person1.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		person1.setLastName("刘");
		person1.setMobile("15836787766");
		person1.setNationality("中国");
		person1.setPhone("02186337623");
		travellers.add(person1);
		
		//游玩人
		Person person2 = new Person();
		person2.setBirthday("1995-11-2");
		person2.setEmail("test2_zhou@sina.com");
		person2.setFax("02186337623");
		person2.setFirstName("芳");
		person2.setGender("女");
		person2.setIdNo("320199511023267");
		person2.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name());
		//身份证
		person2.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		person2.setLastName("刘");
		person2.setMobile("15836787766");
		person2.setNationality("中国");
		person2.setPhone("02186337623");
		travellers.add(person2);
		
		buyInfo.setTravellers(travellers);
		
		//分销商ID
		buyInfo.setDistributionId(Constants.DISTRIBUTOR_2);
		
		//是否发票
		buyInfo.setNeedInvoice(OrderEnum.NEED_INVOICE_STATUS.UNBILL.name());
		
		buyInfo.setCategoryId(8L);
		buyInfo.setProductId(79741L);
		
		List<Item> itemList = new ArrayList<Item>();
		
		//商品ID  内舱两人间
		Item item = new Item();
		item.setGoodsId(452290L);
		item.setQuantity(1);
		item.setVisitTime("2014-05-04");
		item.setMainItem("true");
		itemList.add(item);
		List<ItemPersonRelation> itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		ItemPersonRelation itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person2);
		itemPersonRelationList.add(itemPersonRelation);
		
		//商品ID 岸上观光
		item = new Item();
		item.setGoodsId(452330L);
		item.setQuantity(4);
		item.setVisitTime("2014-05-04");
		item.setMainItem("false");
		itemList.add(item);
		itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person2);
		itemPersonRelationList.add(itemPersonRelation);
		
		//商品ID 签证
		item = new Item();
		item.setGoodsId(452431L);
		item.setQuantity(4);
		item.setVisitTime("2014-05-04");
		item.setMainItem("false");
		itemList.add(item);
		itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person2);
		itemPersonRelationList.add(itemPersonRelation);
		
		//商品ID 签证
		item = new Item();
		item.setGoodsId(452431L);
		item.setQuantity(4);
		item.setVisitTime("2014-05-04");
		item.setMainItem("false");
		itemList.add(item);
		itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person2);
		itemPersonRelationList.add(itemPersonRelation);
		
		
		//商品ID 邮轮附加项
		item = new Item();
		item.setGoodsId(452410L);
		item.setQuantity(4);
		item.setVisitTime("2014-05-04");
		item.setMainItem("false");
		itemList.add(item);
		itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person2);
		itemPersonRelationList.add(itemPersonRelation);
		
		
		
		
		buyInfo.setItemList(itemList);
		buyInfo.setIp("220.181.111.85");
		
 	 	buyInfo.setUserId("ff8080812e56d5ea012e570941f800be");
 	 	
 	 	ResultHandleT<OrdOrder> resultMessage = orderService.createOrder(buyInfo, "Operator001");
 	 	
 	 	System.out.println("12------------------------------MSG: " + resultMessage.getMsg());
 	 	assertTrue(resultMessage.isSuccess());
	}
	
	public void testCreateCombCuriseOrder3() {
		BuyInfo buyInfo = new BuyInfo();
		
		//联系人
		Person contact = new Person();
		contact.setBirthday("1992-10-2");
		contact.setEmail("test_zhou@sina.com");
		contact.setFax("02186337611");
		contact.setFirstName("涛");
		contact.setGender("男");
		contact.setIdNo("320199210027655");
		contact.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name());
		//身份证
		contact.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		contact.setLastName("周");
		contact.setMobile("15832669989");
		contact.setNationality("中国");
		contact.setPhone("02186337611");
		buyInfo.setContact(contact);
		
		//紧急联系人
		contact = new Person();
		contact.setBirthday("1992-10-2");
		contact.setEmail("test_zhou@sina.com");
		contact.setFax("02186337611");
		contact.setFirstName("小涛");
		contact.setGender("男");
		contact.setIdNo("320199210027655");
		contact.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name());
		//身份证
		contact.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		contact.setLastName("周");
		contact.setMobile("15832669989");
		contact.setNationality("中国");
		contact.setPhone("02186337611");
		buyInfo.setEmergencyPerson(contact);
		
		//下单人
		contact = new Person();
		contact.setBirthday("1992-10-2");
		contact.setEmail("test_zhou@sina.com");
		contact.setFax("02186337611");
		contact.setFirstName("下单");
		contact.setGender("男");
		contact.setIdNo("320199210027655");
		contact.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name());
		//身份证
		contact.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		contact.setLastName("周");
		contact.setMobile("15832669989");
		contact.setNationality("中国");
		contact.setPhone("02186337611");
		buyInfo.setBooker(contact);
		
		List<Person> travellers = new ArrayList<Person>();
		
		//游玩人
		Person person1 = new Person();
		person1.setBirthday("1995-11-2");
		person1.setEmail("test2_zhou@sina.com");
		person1.setFax("02186337623");
		person1.setFirstName("华");
		person1.setGender("女");
		person1.setIdNo("320199511023267");
		person1.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name());
		//身份证
		person1.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		person1.setLastName("刘");
		person1.setMobile("15836787766");
		person1.setNationality("中国");
		person1.setPhone("02186337623");
		travellers.add(person1);
		
		//游玩人
		Person person2 = new Person();
		person2.setBirthday("1995-11-2");
		person2.setEmail("test2_zhou@sina.com");
		person2.setFax("02186337623");
		person2.setFirstName("芳");
		person2.setGender("女");
		person2.setIdNo("320199511023267");
		person2.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name());
		//身份证
		person2.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		person2.setLastName("刘");
		person2.setMobile("15836787766");
		person2.setNationality("中国");
		person2.setPhone("02186337623");
		travellers.add(person2);
		
		buyInfo.setTravellers(travellers);
		
		//分销商ID
		buyInfo.setDistributionId(Constants.DISTRIBUTOR_2);
		
		//是否发票
		buyInfo.setNeedInvoice(OrderEnum.NEED_INVOICE_STATUS.UNBILL.name());
		
		buyInfo.setCategoryId(8L);
		buyInfo.setProductId(79941L);
		
		List<Item> itemList = new ArrayList<Item>();
		
		//商品ID  内舱两人间
		Item item = new Item();
		item.setGoodsId(452530L);
		item.setQuantity(1);
		item.setVisitTime("2014-05-30");
		item.setMainItem("true");
		itemList.add(item);
		List<ItemPersonRelation> itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		ItemPersonRelation itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person2);
		itemPersonRelationList.add(itemPersonRelation);
		
		//商品ID 岸上观光
		item = new Item();
		item.setGoodsId(452330L);
		item.setQuantity(2);
		item.setVisitTime("2014-05-30");
		item.setMainItem("false");
		itemList.add(item);
		itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person2);
		itemPersonRelationList.add(itemPersonRelation);
		
		//商品ID 签证
		item = new Item();
		item.setGoodsId(452730L);
		item.setQuantity(2);
		item.setVisitTime("2014-05-30");
		item.setMainItem("false");
		itemList.add(item);
		itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person2);
		itemPersonRelationList.add(itemPersonRelation);
		
		//商品ID 签证
		item = new Item();
		item.setGoodsId(452690L);
		item.setQuantity(2);
		item.setVisitTime("2014-05-30");
		item.setMainItem("false");
		itemList.add(item);
		itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person2);
		itemPersonRelationList.add(itemPersonRelation);
		
		
		//商品ID 邮轮附加项
		item = new Item();
		item.setGoodsId(452850L);
		item.setQuantity(2);
		item.setVisitTime("2014-05-30");
		item.setMainItem("false");
		itemList.add(item);
		itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person2);
		itemPersonRelationList.add(itemPersonRelation);
		
		
		//商品ID 邮轮附加项
		item = new Item();
		item.setGoodsId(452410L);
		item.setQuantity(2);
		item.setVisitTime("2014-05-30");
		item.setMainItem("false");
		itemList.add(item);
		itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person2);
		itemPersonRelationList.add(itemPersonRelation);
		
		
		
		buyInfo.setItemList(itemList);
		buyInfo.setIp("220.181.111.85");
		
 	 	buyInfo.setUserId("ff8080812e56d5ea012e570941f800be");
 	 	
 	 	ResultHandleT<OrdOrder> resultMessage = orderService.createOrder(buyInfo, "Operator001");
 	 	
 	 	System.out.println("12------------------------------MSG: " + resultMessage.getMsg());
 	 	assertTrue(resultMessage.isSuccess());
	}
	
	public void testCreateCombCuriseOrder4() {
		BuyInfo buyInfo = new BuyInfo();
		
		//联系人
		Person contact = new Person();
		contact.setBirthday("1992-10-2");
		contact.setEmail("test_zhou@sina.com");
		contact.setFax("02186337611");
		contact.setFirstName("涛");
		contact.setGender("男");
		contact.setIdNo("320199210027655");
		contact.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name());
		//身份证
		contact.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		contact.setLastName("周");
		contact.setMobile("15832669989");
		contact.setNationality("中国");
		contact.setPhone("02186337611");
		buyInfo.setContact(contact);
		
		//紧急联系人
		contact = new Person();
		contact.setBirthday("1992-10-2");
		contact.setEmail("test_zhou@sina.com");
		contact.setFax("02186337611");
		contact.setFirstName("小涛");
		contact.setGender("男");
		contact.setIdNo("320199210027655");
		contact.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name());
		//身份证
		contact.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		contact.setLastName("周");
		contact.setMobile("15832669989");
		contact.setNationality("中国");
		contact.setPhone("02186337611");
		buyInfo.setEmergencyPerson(contact);
		
		//下单人
		contact = new Person();
		contact.setBirthday("1992-10-2");
		contact.setEmail("test_zhou@sina.com");
		contact.setFax("02186337611");
		contact.setFirstName("下单");
		contact.setGender("男");
		contact.setIdNo("320199210027655");
		contact.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name());
		//身份证
		contact.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		contact.setLastName("周");
		contact.setMobile("15832669989");
		contact.setNationality("中国");
		contact.setPhone("02186337611");
		buyInfo.setBooker(contact);
		
		List<Person> travellers = new ArrayList<Person>();
		
		//游玩人
		Person person1 = new Person();
		person1.setBirthday("1995-11-2");
		person1.setEmail("test2_zhou@sina.com");
		person1.setFax("02186337623");
		person1.setFirstName("华");
		person1.setGender("女");
		person1.setIdNo("320199511023267");
		person1.setPeopleType(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name());
		//身份证
		person1.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
		person1.setLastName("刘");
		person1.setMobile("15836787766");
		person1.setNationality("中国");
		person1.setPhone("02186337623");
		travellers.add(person1);
		
		
		buyInfo.setTravellers(travellers);
		
		//分销商ID
		buyInfo.setDistributionId(Constants.DISTRIBUTOR_2);
		
		//是否发票
		buyInfo.setNeedInvoice(OrderEnum.NEED_INVOICE_STATUS.UNBILL.name());
		
		buyInfo.setCategoryId(8L);
		buyInfo.setProductId(80081L);
		
		List<Item> itemList = new ArrayList<Item>();
		
		//商品ID  内舱两人间
		Item item = new Item();
		item.setGoodsId(452450L);
		item.setQuantity(1);
		item.setVisitTime("2014-06-09");
		item.setMainItem("true");
		itemList.add(item);
		List<ItemPersonRelation> itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		ItemPersonRelation itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		//商品ID 岸上观光
		item = new Item();
		item.setGoodsId(452790L);
		item.setQuantity(1);
		item.setVisitTime("2014-06-09");
		item.setMainItem("false");
		itemList.add(item);
		itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		//商品ID 邮轮附加项
		item = new Item();
		item.setGoodsId(453170L);
		item.setQuantity(1);
		item.setVisitTime("2014-06-09");
		item.setMainItem("false");
		itemList.add(item);
		itemPersonRelationList = new ArrayList<ItemPersonRelation>();
		item.setItemPersonRelationList(itemPersonRelationList);
		
		itemPersonRelation = new ItemPersonRelation();
		itemPersonRelation.setOptionContent(OrderEnum.ORDER_PERSON_RELATION_TYPE.NONE.name());
		itemPersonRelation.setSeq(0);
		itemPersonRelation.setPerson(person1);
		itemPersonRelationList.add(itemPersonRelation);
		
		buyInfo.setItemList(itemList);
		buyInfo.setIp("220.181.111.85");
		
 	 	buyInfo.setUserId("ff8080812e56d5ea012e570941f800be");
 	 	
 	 	ResultHandleT<OrdOrder> resultMessage = orderService.createOrder(buyInfo, "Operator001");
 	 	
 	 	System.out.println("stock------------------------------MSG: " + resultMessage.getMsg());
 	 	assertTrue(resultMessage.isSuccess());
	}
}
