package com.lvmama.vst.back.order.processer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.HotelAdditation;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.GuaranteeCreditCard;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderLocalService;

public class OrderTaskCreateProcesserTest extends OrderTestBase{
	private IOrderAuditService orderAuditService;
	
	private IOrderLocalService orderLocalService;
	
	@Before
	public void prepare() {
		super.prepare();
		if (applicationContext != null) {
			orderAuditService = (IOrderAuditService) applicationContext.getBean("orderAuditService");
			orderLocalService = (IOrderLocalService) applicationContext.getBean("orderServiceRemote");
		}
	}
	
	@Test
	public void testSuit() throws InterruptedException {
		testResoruceAndInfoStatus();
	}
	
//	@Test
	public void testResoruceAndInfoStatus() throws InterruptedException {
		BuyInfo buyInfo = getBuyInfo();
		ResultHandleT<OrdOrder> orderHandle = orderLocalService.createOrder(buyInfo, "TestUser");
		
		assertNotNull(orderHandle);
		assertTrue(orderHandle.isSuccess());
		assertNotNull(orderHandle.getReturnContent());
		
		Thread.sleep(10000);
		boolean checkInfoStatus = false;
		boolean checkResourceStatus = false;
		String auditStatus = null;
		OrdOrder order = orderHandle.getReturnContent();
		if (OrderEnum.INFO_STATUS.UNVERIFIED.name().equals(order.getInfoStatus()))
		{
			checkInfoStatus = true;
			auditStatus = OrderEnum.AUDIT_STATUS.POOL.name();
		}
		if (OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(order.getResourceStatus())) {
			checkResourceStatus = true;
			auditStatus = OrderEnum.AUDIT_STATUS.POOL.name();
		} else if (OrderEnum.RESOURCE_STATUS.AMPLE.name().equals(order.getResourceStatus())) {
			checkResourceStatus = true;
			auditStatus = OrderEnum.AUDIT_STATUS.PROCESSED.name();
		}
		List<ComAudit> auditList = getComAuditByOrder(order);
		
		if (checkInfoStatus && checkResourceStatus) {
			assertTrue(auditList.size() == 2);
		} else if (checkInfoStatus || checkResourceStatus) {
			assertTrue(auditList.size() == 1);
		}
		
		if (checkInfoStatus) {
			ComAudit checkAudit = null;
			for (ComAudit audit: auditList) {
				if (OrderEnum.AUDIT_TYPE.INFO_AUDIT.name().equals(audit.getAuditType())
						&& auditStatus.equals(audit.getAuditStatus())) {
					checkAudit = audit;
				}
			}
			
			assertNotNull(checkAudit);
			assertTrue(checkAudit.getOperatorName() == null);
		}
		
		if (checkResourceStatus) {
			ComAudit checkAudit = null;
			for (ComAudit audit: auditList) {
				if (OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name().equals(audit.getAuditType())
						&& auditStatus.equals(audit.getAuditStatus())) {
					checkAudit = audit;
				}
			}
			
			assertNotNull(checkAudit);
			
			if (OrderEnum.AUDIT_STATUS.POOL.name().equals(auditStatus)) {
				assertTrue(checkAudit.getOperatorName() == null);
			} else if (OrderEnum.AUDIT_STATUS.PROCESSED.name().equals(auditStatus)) {
				assertTrue(checkAudit.getOperatorName() == "SYSTEM");
			}
		}
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
		buyInfo.setDistributionId(3L);
		//分销商代码
		buyInfo.setDistributorCode("WH001");
		
		//担保信息
		GuaranteeCreditCard guaranteeCreditCard = new GuaranteeCreditCard();
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
		buyInfo.setNeedGuarantee(OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name());
		
		//是否发票
		buyInfo.setNeedInvoice(OrderEnum.NEED_INVOICE_STATUS.UNBILL.name());
		
		List<Item> itemList = new ArrayList<Item>();
		Item item = new Item();
		//商品ID
		item.setGoodsId(5L);
		item.setQuantity(1);
		item.setVisitTime("2013-11-28");
		HotelAdditation additation = new HotelAdditation();
		additation.setArrivalTime("14:00");
		additation.setEarlyArrivalTime("11:00");
		additation.setLeaveTime("2013-11-30");
		item.setHotelAdditation(additation);
		item.setMainItem("true");
		itemList.add(item);
		
		item = new Item();
		//商品ID
		item.setGoodsId(5L);
		item.setQuantity(1);
		item.setVisitTime("2013-12-20");
		additation = new HotelAdditation();
		additation.setArrivalTime("18:30");
		additation.setEarlyArrivalTime("15:30");
		additation.setLeaveTime("2013-12-22");
		item.setHotelAdditation(additation);
		item.setMainItem("false");
		itemList.add(item);
		
		buyInfo.setItemList(itemList);
		
		buyInfo.setRemark("加一个床");
		
		buyInfo.setUserId("User001");
		
		return buyInfo;
	}
	
	private List<ComAudit> getComAuditByOrder(OrdOrder order) {
		List<ComAudit> auditList = null;
		if (order != null) {
			Map<String, Object> conditionMap = new HashMap<String, Object>();
			conditionMap.put("objectId", order.getOrderId());
			conditionMap.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
			auditList = orderAuditService.queryAuditListByCondition(conditionMap);
		}
		
		return auditList;
	}
}
