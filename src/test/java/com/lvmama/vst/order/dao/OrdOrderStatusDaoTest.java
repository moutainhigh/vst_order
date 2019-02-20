package com.lvmama.vst.order.dao;

import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.back.order.po.OrdOrderStatus;
import com.lvmama.vst.back.order.po.OrderStatusEnum;

public class OrdOrderStatusDaoTest extends OrderTestBase {
    
    @Autowired
    private OrdOrderStatusDao orderStatusDao;

    private Long statusId;
    
    private Long orderId = 200615912L;
    
    @After
    public void testDeleteByPrimaryKey() {
    	Assert.assertNotNull(statusId);
    	int i = orderStatusDao.deleteByPrimaryKey(statusId);
    	Assert.assertTrue(i > 0);
    }

    @Before
    public void testInsertSelective() {        
        OrdOrderStatus status = new OrdOrderStatus();        
        status.setOrderId(orderId);
        status.setOrderItemId(100102L);
        status.setStatus(OrderStatusEnum.ORDER_PROCESS_STATUS.SUCCESS.getStatusCode());
        status.setCreateTime(new Date());                        
        int i = orderStatusDao.insertSelective(status);
        Assert.assertTrue(i > 0);
		statusId = orderStatusDao.get("selectStatusId");
		Assert.assertNotNull(statusId);
    }

    @Test
    public void testSelectByOrderId() {
        OrdOrderStatus orderStatus = orderStatusDao.selectByOrderId(orderId);
        Assert.assertTrue(orderStatus != null && orderStatus.getUpdateTime() != null);
        System.out.println("order = " + orderId + "\t orderStatus = " + OrderStatusEnum.ORDER_PROCESS_STATUS.getOrderProcessStatusByStatusCode(orderStatus.getOrdStatusId()).getStatus());
    }

}
