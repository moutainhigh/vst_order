package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdPaymentInfo;

/**
 * @author chenlizhao
*/

public class OrdPaymentInfoDaoTest extends OrderTestBase {
	@Autowired
	private OrdPaymentInfoDao ordPaymentInfoDao;
	
	private Long id = 3346198L;
	
	@Test
	public void testSelectByParams() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("paymentId", id);
		List<OrdPaymentInfo> ordPaymentInfoList = ordPaymentInfoDao.findOrdPaymentInfoList(params);
		Assert.assertTrue(ordPaymentInfoList != null && ordPaymentInfoList.size() > 0);
	}
			
}