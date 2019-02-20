/**
 * 
 */
package com.lvmama.vst.order.service;

import java.util.Arrays;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.lvmama.vst.back.client.ord.service.OrderSupplierNotifyService;
import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdPassCode;
import com.lvmama.vst.back.order.po.OrdTicketPerform;
import com.lvmama.vst.comm.vo.ResultHandle;

/**
 * @author pengyayun
 *
 */
public class OrderSupplierNotifyServiceTest extends OrderTestBase{
	
	private OrderSupplierNotifyService OrderSupplierNotifyService;
	
	@Before
	public void prepare() {
		super.prepare();
		if (applicationContext != null) {
			OrderSupplierNotifyService = (OrderSupplierNotifyService) applicationContext.getBean("orderSupplierNotifyServiceRemote");
		}
	}
	
	//@Test
	public void testOrderTicketPerform() {
		OrdTicketPerform ordTicketPerform=new OrdTicketPerform();
		ordTicketPerform.setVisitTime(new Date());
		ordTicketPerform.setAdultQuantity(1L);
		ordTicketPerform.setChildQuantity(1L);
		ordTicketPerform.setCreateTime(new Date());
		ordTicketPerform.setOrderId(21461L);
		ordTicketPerform.setOrderItemId(21401L);
		ordTicketPerform.setMemo("订单履行");
		ResultHandle resultHandle=OrderSupplierNotifyService.orderTicketPerform(ordTicketPerform);
		Assert.assertTrue(resultHandle.isSuccess());
	}
	
	@Test
	public void testPassCodeAppy(){
		OrdPassCode passCode = new OrdPassCode();
		passCode.setAddCode("122323");
		passCode.setCheckingId(1L);
		passCode.setCode("2323123");
		passCode.setCodeImage(new byte[10000]);
		passCode.setCreateTime(new Date());
		passCode.setOrderItemId(1234L);
		passCode.setServiceId(1122L);
		
		ResultHandle handle = OrderSupplierNotifyService.passCodeNotify(Arrays.asList(passCode));
		Assert.assertTrue(handle.isSuccess());
	}
}
