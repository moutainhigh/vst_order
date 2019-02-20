package com.lvmama.vst.route;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.order.route.service.IOrderRouteService;
import com.lvmama.vst.back.order.OrderTestBase;

/** 
 * @ImplementProject vst_order
 * @Description 
 * @author chenlizhao
 * @date 2018年3月1日 下午7:25:10
 */
public class LvmmOrderRouteTest extends OrderTestBase {

	@Autowired
	private IOrderRouteService orderRouteService;
	
	@Test
	public void testRouteToNewWorkflow() {
		boolean check = orderRouteService.isRouteToNewWorkflow(11L);
		Assert.assertTrue(check == true);
		
		check = orderRouteService.isRouteToNewWorkflow(1L);
		Assert.assertTrue(check == false);
	}
}
