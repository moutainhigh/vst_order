/**
 * 
 */
package com.lvmama.vst.order.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderLosc;

/**
 * @author pengyayun
 *
 */
public class OrdOrderLoscServiceImplTest extends OrderTestBase{
	
	private IOrdOrderLoscService ordOrderLoscService;
	
	@Before
	public void prepare() {
		super.prepare();
		if (applicationContext != null) {
			ordOrderLoscService = (IOrdOrderLoscService) applicationContext.getBean("ordOrderLoscServiceImpl");
		}
	}
	
	@Test
	public void testAddOrderLosc(){
		List<OrdOrderLosc> list=new ArrayList<OrdOrderLosc>();
		OrdOrderLosc losc1= new OrdOrderLosc();
		losc1.setLoscId("AAAAAABBBBBCCCCC");
		losc1.setOrderId(11233654L);
		list.add(losc1);
		OrdOrderLosc losc2= new OrdOrderLosc();
		losc2.setLoscId("DDDDDDDEEEEEEEFFFFFF");
		losc2.setOrderId(11233654L);
		list.add(losc2);
		OrdOrderLosc losc3= new OrdOrderLosc();
		losc3.setLoscId("AAAAAABBBBBCCCCC");
		losc3.setOrderId(11233654L);
		list.add(losc3);
		
		for (OrdOrderLosc ordOrderLosc: list) {
			int result=ordOrderLoscService.addOrderLosc(ordOrderLosc);
			Assert.assertEquals(1, result);
		}
		
	}
}
