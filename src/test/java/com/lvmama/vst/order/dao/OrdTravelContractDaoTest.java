package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdTravelContract;

/**
 * @author chenlizhao
*/

public class OrdTravelContractDaoTest extends OrderTestBase {
	@Autowired
	private OrdTravelContractDAO ordTravelContractDao;
	
	private Long id = 1075L;
		
	@Test
	public void testSelectByPrimaryKey() throws Exception {
		OrdTravelContract ordTravelContract = ordTravelContractDao.selectByPrimaryKey(id);
		Assert.assertNotNull(ordTravelContract);
	}
	
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdTravelContract ordTravelContract = new OrdTravelContract();
		ordTravelContract.setOrdContractId(id);
		ordTravelContract.setAdditionFileId("test");
		int i = ordTravelContractDao.updateByPrimaryKeySelective(ordTravelContract);
		Assert.assertTrue(i > 0);
	}
}