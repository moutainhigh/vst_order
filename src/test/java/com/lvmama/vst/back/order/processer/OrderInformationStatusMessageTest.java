package com.lvmama.vst.back.order.processer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.order.processer.SupplierOrderProcesser;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:applicationContext-vst-back-beans.xml"})

public class OrderInformationStatusMessageTest {
	@Autowired
	private SupplierOrderProcesser supplierOrderProcesser;
	
	@Before
	public void before() {
		
	}
	@Test
	public void supplierOrderProcesser(){
		supplierOrderProcesser.process(MessageFactory.newOrderInformationStatusMessage(535L, ""));
	}
}
