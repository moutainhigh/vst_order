package com.lvmama.vst.order.service.impl;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComJobConfig;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.flight.client.order.service.FlightOrderProcessService;
import com.lvmama.vst.order.dao.ComJobConfigDAO;
import com.lvmama.vst.order.job.ComJobConfigExecutorJob;

/**
 * 机票先关测试
 * @author xuxueli
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)  
@Transactional
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public class OrderFlightTicketServiceImplTest {
	
	@Autowired
	private FlightOrderProcessService flightOrderProcessServiceRemote;

	/**
	 * 锁仓
	 * @throws Exception
	 */
	@Test
	public void lockSeat() throws Exception {
		
		long orderItemId = 2000029665L;
		
		long start = System.currentTimeMillis();
		ResultHandle result = flightOrderProcessServiceRemote.lockSeat(orderItemId);
		long stop = System.currentTimeMillis();
		System.out.println(":::::::: cost :" + (stop - start)/1000);
		
		Assert.assertNotNull(result);
		System.out.println(BeanUtils.describe(result));
		
	}
	
	/**
	 * 支付通知
	 * @throws Exception
	 */
	@Test
	public void paymentNotify() throws Exception {
		
		long orderItemId = 2000029665L;	// 20026135L
		
		long start = System.currentTimeMillis();
		ResultHandle result = flightOrderProcessServiceRemote.paymentNotify(orderItemId);
		long stop = System.currentTimeMillis();
		System.out.println(":::::::: cost :" + (stop - start)/1000);
		
		Assert.assertNotNull(result);
	}
	
	/**
	 * 订单取消通知
	 * @throws Exception
	 */
	@Test
	public void cancelOrderNotify() throws Exception {
		
		long orderItemId = 2000029665L;
		
		long start = System.currentTimeMillis();
		ResultHandle result = flightOrderProcessServiceRemote.cancelOrderNotify(orderItemId);
		long stop = System.currentTimeMillis();
		System.out.println(":::::::: cost :" + (stop - start)/1000);
		
		Assert.assertNotNull(result);
	}
	
	@Autowired
	private ComJobConfigExecutorJob comJobConfigExecutorJob;
	@Autowired
	private ComJobConfigDAO comJobConfigDAO;
	
	/**
	 * 支付通知JOB方式重发逻辑
	 * @throws Exception
	 */
	@Test
	public void TestflightOrderPaymentNotifyTryExecutor() throws Exception {
		List<ComJobConfig> jobConfigs = comJobConfigDAO.selectByObjectId(ComJobConfig.JOB_TYPE.FLIGHT_ORDER_PAYMENT_NOTIFY.name(), 2000029526L);
		if (CollectionUtils.isNotEmpty(jobConfigs)) {
			comJobConfigExecutorJob.run();
		}
	}
	
	/**
	 * 测试用，上线前删除 【放在ActivitiTaskFinishAction里面，用来模拟支付】
	 * 
	 * @param orderId
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception
	 *//*
	@RequestMapping(value = "/ord/order/mockPayment")
	public String mockPayment(Long orderId, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		if (orderId == null) {
			return null;
		}
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		if (order == null) {
			logger.error("找不到ID为[" + orderId + "]的订单");
			return null;
		}

		order.setPaymentStatus(OrderEnum.PAYMENT_STATUS.PAYED.name());
		order.setPaymentTime(new Date());

		int n = orderUpdateService.updateOrderAndChangeOrderItemPayment(order);
		processerClientService.paymentSuccess(createKeyByOrder(order));
		return null;
	}*/

}
