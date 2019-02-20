package com.lvmama.vst.back.order.job;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.order.job.AutoTaskAssignJob;
import com.lvmama.vst.order.service.IOrderLocalService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class AutoTaskAssignJobTest{
	@Autowired
	AutoTaskAssignJob autoTaskAssignJob;
	@Autowired
	private IOrderLocalService orderLocalService;
	@Test
	public void testSuit() {
		autoTaskAssignJob.run();
	}
}
