/**
 * 
 */
package com.lvmama.vst.order.dao;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;

/**
 * @author chenlizhao
 *
 */
public class ComActivitiRelationDaoTest extends OrderTestBase {

	@Autowired
	private ComActivitiRelationDao comActivitiRelationDao;
	
	private Long objectId = 20004822L;
	
	@Before
	public void testInsert() throws Exception {
		ComActivitiRelation rel = new ComActivitiRelation();
		rel.setObjectId(objectId);
		rel.setObjectType("ORD_ORDER");
		rel.setProcessId("120");
		rel.setProcessKey("test");
		int i = comActivitiRelationDao.insert(rel);
		Assert.assertTrue(i > 0);
	}
	
	@Test
	public void testQueryList() throws Exception {
		ComActivitiRelation rel = new ComActivitiRelation();
		rel.setObjectId(objectId);
		List<ComActivitiRelation> rels = comActivitiRelationDao.queryList(rel);
		Assert.assertTrue(rels != null && rels.size() > 0);
		System.out.println("return relation size: " + rels.size());
		for(ComActivitiRelation re : rels) {
			Assert.assertNotNull(re.getUpdateTime());
		}
	}
}
