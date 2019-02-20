package com.lvmama.vst.back.order.service;

import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.HotelAdditation;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.Person;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderServiceTest {
	private static final Log log = LogFactory.getLog(OrderServiceTest.class);

	@Resource(name="orderServiceRemote")
	private OrderService orderService;
	
	//@Test
	public void testSuit() {
		testSupplierStockCheck();
	}
	
	//@Test
	public void testCreateOrder(){
		BuyInfo buyInfo = getBuyInfo();
		ResultHandle handle = orderService.createOrder(buyInfo, "admin");
		Assert.assertTrue(handle.isSuccess());
	}
	
//	@Test
	public void testCreateOrder2(){
		BuyInfo buyInfo = new BuyInfo();
		List<BuyInfo.Coupon> couponList = new ArrayList<BuyInfo.Coupon>();
		BuyInfo.Coupon coupon = new BuyInfo.Coupon();
		coupon.setCode("B110415259317063");
		couponList.add(coupon);
		
		buyInfo.setCouponList(couponList);
		
		List<BuyInfo.Item> itemList = new ArrayList<BuyInfo.Item>();
		BuyInfo.Item item = new BuyInfo.Item();
		item.setGoodsId(566505L);
		item.setQuantity(1);
		itemList.add(item);
		
		item = new BuyInfo.Item();
		item.setGoodsId(567960L);
		item.setQuantity(0);
		itemList.add(item);
		buyInfo.setItemList(itemList);
		
		buyInfo.setSameVisitTime("true");
		buyInfo.setVisitTime("2014-09-12");
		
		buyInfo.setYouhui("coupon");
		
		
//		List<Person> personList = new ArrayList<Person>();
		Person p = new Person();
		p.setFirstName("ceshi");
		p.setLastName("aaaa");
		p.setEmail("test@123.com");
		p.setFullName("测试乔");
		p.setIdNo("32092519841021451X");
		p.setIdType("ID_CARD");
		p.setMobile("13800138456");
		p.setPeopleType("PEOPLE_TYPE_ADULT");
		p.setReceiverId("402880794592667e014592667ef40000");
		buyInfo.setTravellers(Arrays.asList(p));
		buyInfo.setUserId("402880ca1d0ff4bc011d0ff4f60f0360");
		buyInfo.setUserNo(10563L);
		ResultHandle handle = orderService.createOrder(buyInfo, "admin");
		assertNotNull(handle);
		assertTrue(handle.isSuccess());
	}
	
//	@Test
	public void testSupplierStockCheck() {
		BuyInfo buyInfo = getBuyInfo();
		ResultHandle handle = orderService.checkStock(buyInfo);
		assertNotNull(handle);
		assertTrue(handle.isSuccess());
	}
	
	private BuyInfo getBuyInfo() {
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
		buyInfo.setDistributionId(2L);
		//分销商代码
		buyInfo.setDistributorCode("WH001");
		
		//担保信息
//		GuaranteeCreditCard guaranteeCreditCard = new GuaranteeCreditCard();
//		guaranteeCreditCard.setCvv("236");
//		guaranteeCreditCard.setExpirationMonth(8L);
//		guaranteeCreditCard.setExpirationYear(2016L);
//		guaranteeCreditCard.setHolderName("周涛");
//		guaranteeCreditCard.setIdNo("320199210027655");
//		//身份证
//		guaranteeCreditCard.setIdType(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name());
//		guaranteeCreditCard.setCardNo("5673901285116723");
//		buyInfo.setGuarantee(guaranteeCreditCard);
		//担保
//		buyInfo.setNeedGuarantee(OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name());
		
		//是否发票
		buyInfo.setNeedInvoice(OrderEnum.NEED_INVOICE_STATUS.UNBILL.name());
		
		List<Item> itemList = new ArrayList<Item>();
		Item item = new Item();
		//商品ID
		item.setGoodsId(1870L);
		item.setQuantity(1);
		item.setVisitTime("2014-2-15");
		HotelAdditation additation = new HotelAdditation();
		additation.setArrivalTime("14:00");
		additation.setEarlyArrivalTime("11:00");
		additation.setLeaveTime("2014-2-17");
		item.setHotelAdditation(additation);
		item.setMainItem("true");
		itemList.add(item);
		
//		item = new Item();
//		//商品ID
//		item.setGoodsId(11108L);
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
		
		buyInfo.setRemark("加一个床");
		
		buyInfo.setUserId("User001");
		
		return buyInfo;
	}

	@Test
	public void testCreateOrder1(){
		BuyInfo buyInfo = createBuyInfoFormJson();
		ResultHandleT<OrdOrder> order = orderService.createOrder(buyInfo, "User001");
		log.info("order is " + JSONUtil.bean2Json(order));
	}


	private BuyInfo createBuyInfoFormJson() {
		String jsonStr = "{\"distributionChannel\":10000,\n" +
				"\"distributorCode\":\"ANDROID_LVMM\",\n" +
				"\"distributorName\":\"\",\n" +
				"\"distributionId\":4,\n" +
				"\"needGuarantee\":\"\",\n" +
				"\"needInvoice\":\"\",\n" +
				"\"remark\":\"\",\n" +
				"\"ip\":\"127.0.0.1\",\n" +
				"\"itemList\":[],\n" +
				"\"productList\":[{\"visitTime\":\"2016-05-31\",\"adultQuantity\":\"1\",\"childQuantity\":\"0\",\"quantity\":\"1\",\"productId\":\"379125\",\"buCode\":\"\",\"taobaoETicket\":null,\"itemList\":[{\"additionalFlightNoVoList\":[{\"adultAmt\":0,\"arriveTerminal\":\"T2\",\"arriveTime\":\"2016-05-31 22:35:00\",\"arriveTimeStr\":\"\",\"childAmt\":0,\"companyCode\":\"\",\"companyName\":\"中国国际航空公司\",\"flightNo\":\"TG615\",\"flightNodeVoList\":[],\"flightType\":2,\"flyGroupId\":0,\"flyTime\":185,\"flyTimeStr\":\"03小时05分\",\"foodSupport\":false,\"fromAirPort\":\"上海浦东机场T1\",\"fromCityName\":\"上海市\",\"goTime\":\"2016-05-31 19:30:00\",\"goTimeStr\":\"\",\"goodsId\":1439876,\"planeCode\":\"中国国际航空公司\",\"remain\":0,\"seatCode\":\"\",\"seatName\":\"\",\"startTerminal\":\"T1\",\"throughFlag\":false,\"toAirPort\":\"北京南苑机场T2\",\"toCityName\":\"北京市\"}],\"adultAmt\":20000,\"adultQuantity\":1,\"backDate\":\"\",\"buCode\":\"\",\"checkStockQuantity\":1,\"childAmt\":20000,\"childQuantity\":0,\"circusActInfo\":null,\"content\":\"\",\"detailId\":5718,\"disneyItemOrderInfo\":\"\",\"displayTime\":\"\",\"flightNoVo\":null,\"gapQuantity\":0,\"goodType\":\"\",\"goodsId\":1439876,\"hotelAdditation\":null,\"hotelcombOptions\":[],\"isDisneyGood\":\"\",\"itemPersonRelationList\":[],\"mainItem\":\"\",\"ownerQuantity\":0,\"price\":\"\",\"priceTypeList\":[],\"productCategoryId\":0,\"quantity\":1,\"roomMaxInPerson\":0,\"routeRelation\":\"PACK\",\"settlementPrice\":\"\",\"shareDayLimit\":0,\"shareTotalStock\":0,\"sharedStockList\":[],\"taobaoETicket\":0,\"toDate\":\"\",\"totalAmount\":0,\"totalPersonQuantity\":1,\"totalSettlementPrice\":0,\"visitTime\":\"2016-05-31\",\"visitTimeDate\":\"2016-05-31 00:00:00\",\"wifiAdditation\":null}]}],\n" +
				"\"travellerDelayFlag\":\"\",\n" +
				"\"orderTravellerConfirm\":null,\n" +
				"\"travellers\":[{\"birthPlace\":\"\",\"birthday\":\"\",\"birthdayStr\":\"\",\"buyInsuranceFlag\":\"N\",\"email\":\"\",\"expDate\":null,\"fax\":\"\",\"firstName\":\"\",\"fullName\":\"ccguo\",\"gender\":\"\",\"idNo\":\"370285199312257446\",\"idType\":\"ID_CARD\",\"issueDate\":null,\"issued\":\"\",\"lastName\":\"\",\"mobile\":\"13044102235\",\"nationality\":\"\",\"notEmpty\":true,\"peopleType\":\"PEOPLE_TYPE_ADULT\",\"phone\":\"\",\"receiverId\":\"\",\"roomNo\":0,\"saveFlag\":\"false\"}],\n" +
				"\"userId\":\"5ad32f1a20d467450120d606b25100a7\",\n" +
				"\"userNo\":\"26637\",\n" +
				"\"buCode\":\"\",\n" +
				"\"faxMemo\":\"\",\n" +
				"\"categoryId\":null,\n" +
				"\"productId\":\"379125\",\n" +
				"\"additionalTravel\":\"true\",\n" +
				"\"anonymityBookFlag\":\"\",\n" +
				"\"sameVisitTime\":\"\",\n" +
				"\"visitTime\":\"2016-05-31\",\n" +
				"\"adultQuantity\":\"0\",\n" +
				"\"spreadQuantity\":\"0\",\n" +
				"\"quantity\":\"0\",\n" +
				"\"waitPayment\":null,\n" +
				"\"sendContractFlag\":\"Y\"\n" +
				"}";

		BuyInfo buyInfo = GsonUtils.fromJson(jsonStr, BuyInfo.class);

		return buyInfo;
	}
}
