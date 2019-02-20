package com.lvmama.vst.back.order.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.order.job.AutoTaskAssignJob;

/**
 * 自动分单定时任务测试
 * 
 * @author wenzhengtao
 * 
 */
public class AutoTaskAssignJobTest extends OrderTestBase {
	@Autowired
	private AutoTaskAssignJob autoTaskAssignJob;

	
	@Test
	public void test() {
		//先判断注入对象是否存在
		Assert.assertNotNull(autoTaskAssignJob);
		//测试自动分单
		autoTaskAssignJob.run();
	}

}
