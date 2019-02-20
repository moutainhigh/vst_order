package com.lvmama.vst.back.order.job;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.order.job.AutoClearActivitiDataJob;

public class AutoClearActivitiDataJobTest extends OrderTestBase{
	@Autowired
	AutoClearActivitiDataJob autoClearActivitiDataJob;
	
	@Before
	public void prepare() {
		super.prepare();
		if (applicationContext != null) {
			try {
				autoClearActivitiDataJob = (AutoClearActivitiDataJob) applicationContext.getBean("autoClearActivitiDataJob");
			}catch(Exception e) {
				e.printStackTrace();
			}
			System.out.println("autoClearActivitiDataJob:" + autoClearActivitiDataJob);
		}
	}
	
	@Test
	public void test() {
		System.out.println("autoClearActivitiDataJob testing");
		autoClearActivitiDataJob.run();
	}
}
