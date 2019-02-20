package com.lvmama.vst.back.order.service;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.HotelAdditation;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.GuaranteeCreditCard;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.order.service.IOrderStockService;

public class OrderStockServiceTest extends OrderTestBase{
	private IOrderStockService orderService = null;
	
	@Before
	public void prepare() {
		super.prepare();
		if (applicationContext != null) {
			orderService = (IOrderStockService) applicationContext.getBean("orderStockServiceImpl");
		}
	}
	
	@Test
	public void testSuit() {
		testFreeSaleStockCheck();
	}
	
	@Test
	public void testFreeSaleStockCheck() {
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
		
		ResultHandle resultHandle = orderService.checkStock(buyInfo);
		
		System.out.println("resultHandle.Msg=" + resultHandle.getMsg());
		
		assertTrue(resultHandle.isSuccess());
	}
}
