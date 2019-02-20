package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.O2oOrder;
import com.lvmama.vst.back.order.po.OrdAuditAllocationRelation;

/**
 * @author chenlizhao
*/

public class OrdAuditAllocationRelationDaoTest extends OrderTestBase {
	@Autowired
	private OrdAuditAllocationRelationDao ordAuditAllocationRelationDao;
	
	private Long id = 281L;
	
	@Test
	public void testQueryOrdAuditAllocationRelationListByParam() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("relationId", id);
		List<OrdAuditAllocationRelation> relList = ordAuditAllocationRelationDao.queryOrdAuditAllocationRelationListByParam(params);
		Assert.assertTrue(relList != null && relList.size() > 0);
	}
		
	@Test
	public void testUpdateByPrimaryKeySelective() throws Exception {
		OrdAuditAllocationRelation ordAuditAllocationRelation = new OrdAuditAllocationRelation();
		ordAuditAllocationRelation.setRelationId(id);
		ordAuditAllocationRelation.setOrdFunctionId(2L);
		int i = ordAuditAllocationRelationDao.updateByPrimaryKeySelective(ordAuditAllocationRelation);
		Assert.assertTrue(i > 0);
	}
}