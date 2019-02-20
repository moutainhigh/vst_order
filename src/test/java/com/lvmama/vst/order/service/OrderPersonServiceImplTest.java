package com.lvmama.vst.order.service;

import org.junit.Before;
import org.junit.Test;
import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrderEnum;

public class OrderPersonServiceImplTest extends OrderTestBase {

	private IOrdPersonService ordPersonService;

	@Before
	public void prepare() {
		super.prepare();
		if (this.applicationContext != null) {
			this.ordPersonService = (IOrdPersonService) this.applicationContext
					.getBean("ordPersonServiceImpl");
		}
	}

	@Test
	public void testFindPerson() {
		Long orderId = 26461L;
		String personType=OrderEnum.ORDER_PERSON_TYPE.ADDRESS.name();
		this.ordPersonService.getOrderPersonListWithAddress(orderId,personType);
	}
}
