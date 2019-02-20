package com.lvmama.vst.back.order.job;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.order.job.OrderPerformPreviousDayJob;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:applicationContext-vst-back-beans.xml"})
public class OrderPerformPreviousDayJobTest{
	@Autowired
	OrderPerformPreviousDayJob orderPerformPreviousDayJob;
	
	@Test
	public void testSuit() {
		orderPerformPreviousDayJob.run();
	}
}
