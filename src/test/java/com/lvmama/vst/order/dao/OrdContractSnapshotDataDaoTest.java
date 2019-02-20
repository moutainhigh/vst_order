package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdContractSnapshotData;

/**
 * @author chenlizhao
*/

public class OrdContractSnapshotDataDaoTest extends OrderTestBase {
	@Autowired
	private OrderContactSnapshotDao ordContractSnapshotDataDao;
	
	private Long id = 1000008L;
	
	@Test
	public void testSelectByParam() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("snapshotDataId", id);
		List<OrdContractSnapshotData> ordContractSnapshotDataList = ordContractSnapshotDataDao.selectByParam(params);
		Assert.assertTrue(ordContractSnapshotDataList != null && ordContractSnapshotDataList.size() > 0);
	}
	
	@Test
	public void testInsertSelective() throws Exception {
		OrdContractSnapshotData ordContractSnapshotData = new OrdContractSnapshotData();
		ordContractSnapshotData.setOrdContractId(7305L);
		int i = ordContractSnapshotDataDao.insertSelective(ordContractSnapshotData);
		Assert.assertTrue(i > 0);
	}
}