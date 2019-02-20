package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdSettlementPriceRecord;

/**
 * @author chenlizhao
*/

public class OrdSettlementPriceRecordDaoTest extends OrderTestBase {
	@Autowired
	private OrdSettlementPriceRecordDao ordSettlementPriceRecordDao;
	
	private Long id = 121L;
		
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdSettlementPriceRecord ordSettlementPriceRecord = ordSettlementPriceRecordDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordSettlementPriceRecord);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdSettlementPriceRecord ordSettlementPriceRecord = new OrdSettlementPriceRecord();
		ordSettlementPriceRecord.setRecordId(id);
		ordSettlementPriceRecord.setApproveRemark("ok");
		int i = ordSettlementPriceRecordDao.updateByPrimaryKeySelective(ordSettlementPriceRecord);
		Assert.assertTrue(i > 0);
	}
}