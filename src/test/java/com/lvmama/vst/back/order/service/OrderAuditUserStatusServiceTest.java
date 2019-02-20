package com.lvmama.vst.back.order.service;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.order.po.OrdAuditUserStatus;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.order.dao.ComAuditDao;
import com.lvmama.vst.order.dao.OrdAuditUserStatusDAO;
import com.lvmama.vst.order.service.IOrderAuditUserStatusService;
import com.lvmama.vst.order.service.impl.OrderDistributionBusiness;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;
/**
 * 员工状态业务单元测试
 * 
 * @author wenzhengtao
 *
 */
import com.lvmama.vst.supp.client.service.OrderDetailClientService;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderAuditUserStatusServiceTest{
	
	@Autowired
	@Resource(name="orderAuditUserStatusService")
	private IOrderAuditUserStatusService orderAuditUserStatusService;
	
	@Autowired
	private PermUserServiceAdapter permUserServiceAdapter;


//	@Test
	public void testInsert() {
		String operatorName = "lv1181";

		PermUser user = permUserServiceAdapter
				.getPermUserByUserName(operatorName);
		if (user == null) {
			throw new NullPointerException("员工不存在");
		}

		OrdAuditUserStatus auditUserStatusNew = new OrdAuditUserStatus();
		auditUserStatusNew.setOperatorName(operatorName);
		auditUserStatusNew.setUserStatus(OrderEnum.BACK_USER_WORK_STATUS.ONLINE
				.name());
		auditUserStatusNew.setOrgId(user.getDepartmentId());
		auditUserStatusNew.setCreateTime(new Date());
		orderAuditUserStatusService.insert(auditUserStatusNew);

		OrdAuditUserStatus old = orderAuditUserStatusService
				.selectByPrimaryKey(operatorName);
		Assert.assertNotNull(old);
	}
	
	@Autowired
	private OrdAuditUserStatusDAO auditUserStatusDAO;
	
//	@Test
	public void testRandomGetUser(){
		OrdAuditUserStatus status=auditUserStatusDAO.getRandomUserByOrgIds(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name(), Arrays.asList(1L,2L));
		Assert.assertNull(status);
		
		
		status=auditUserStatusDAO.getRandomUserByOrgIds(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name(), Arrays.asList(348L,2L));
		Assert.assertNotNull(status);
		
	}
	
	@Autowired
	private OrderDistributionBusiness orderDistributionBusiness;
	
	@Autowired
	private ComAuditDao comAuditDao;
	
	@Test
	public void testAssign(){
		ComAudit audit = comAuditDao.selectByPrimaryKey(6943L);
		orderDistributionBusiness.makeOrderAudit(audit);
		audit = comAuditDao.selectByPrimaryKey(6943L);
		Assert.assertEquals(audit.getAuditStatus(), OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
	}

//	@Test
//	public void testInsertSelective() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSelectByPrimaryKey() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testUpdateByPrimaryKeySelective() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testUpdateByPrimaryKey() {
//		fail("Not yet implemented");
//	}

}
