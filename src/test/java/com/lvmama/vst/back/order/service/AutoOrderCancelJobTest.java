package com.lvmama.vst.back.order.service;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.order.job.AutoOrderCancelJob;

public class AutoOrderCancelJobTest extends OrderTestBase{
	private AutoOrderCancelJob autoOrderCancelJob;	
	@Before
	public void prepare() {
		super.prepare();
		if (applicationContext != null) {
			autoOrderCancelJob = (AutoOrderCancelJob) applicationContext.getBean("autoOrderCancelJob");
		}
	}
	
	@Test
	public void testCancleOrderJob() {
		assertNotNull(autoOrderCancelJob);
		autoOrderCancelJob.run();
	}
}
