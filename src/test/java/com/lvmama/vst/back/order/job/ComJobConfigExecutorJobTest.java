package com.lvmama.vst.back.order.job;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.pub.po.ComJobConfig;
import com.lvmama.vst.back.pub.service.ComJobConfigService;
import com.lvmama.vst.comm.utils.DateUtil;

public class ComJobConfigExecutorJobTest extends OrderTestBase{
	private ComJobConfigService comJobConfigService;
	@Before
	public void prepare() {
		super.prepare();
		if (applicationContext != null) {
			comJobConfigService = (ComJobConfigService) applicationContext.getBean("comJobConfigServiceImpl");
		}
	}
	
	@Test
	public void testSuit() {
//		testCancelExecutor();
		testCreateExecutor();
	}
	
//	@Test
	public void testCancelExecutor() {
		ComJobConfig jobConfig = new ComJobConfig();
		jobConfig.setCreateTime(new Date());
		jobConfig.setJobType(ComJobConfig.JOB_TYPE.SUPP_ORDER_CANCEL.name());
		jobConfig.setObjectId(223L);
		jobConfig.setObjectType(ComJobConfig.OBJECT_TYPE.ORDER.name());
		jobConfig.setPlanTime(DateUtil.getDateAfterMinutes(5));
		jobConfig.setRetryCount(5L);
		comJobConfigService.saveComJobConfig(jobConfig);
	}
	
//	@Test
	public void testCreateExecutor() {
		ComJobConfig jobConfig = new ComJobConfig();
		jobConfig.setCreateTime(new Date());
		jobConfig.setJobType(ComJobConfig.JOB_TYPE.SUPP_ORDER_CREATE.name());
		jobConfig.setObjectId(223L);
		jobConfig.setObjectType(ComJobConfig.OBJECT_TYPE.ORDER.name());
		jobConfig.setPlanTime(DateUtil.getDateAfterMinutes(5));
		jobConfig.setRetryCount(5L);
		comJobConfigService.saveComJobConfig(jobConfig);
	}
}
