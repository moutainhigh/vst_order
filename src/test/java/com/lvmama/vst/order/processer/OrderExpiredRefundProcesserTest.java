package com.lvmama.vst.order.processer;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageFactory;

public class OrderExpiredRefundProcesserTest extends OrderTestBase {
	
	@Autowired
	private OrderExpiredRefundProcesser orderExpiredRefundProcesser;

	@Test
    public void testProcess() {
		// 62967730L, 62969404L, 62968878L, 62969899L
		Message message = MessageFactory.newExpiredRefundMessage(62972376L, "");
		orderExpiredRefundProcesser.process(message);
    }
	
}
