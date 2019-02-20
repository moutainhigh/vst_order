package com.lvmama.vst.order.service.refund.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrderRefundBatchDetail;
import com.lvmama.vst.order.dao.OrderRefundBatchDetailDao;
import com.lvmama.vst.order.service.refund.OrderRefundBatchDetailService;

@Service
public class OrderRefundBatchDetailServiceImpl implements OrderRefundBatchDetailService{
	private static Logger LOG = LoggerFactory.getLogger(OrderRefundBatchDetailServiceImpl.class);
	@Autowired
	private OrderRefundBatchDetailDao orderRefundBatchDetailDao;
	@Override
	public List<OrderRefundBatchDetail> getOrderRefundBatchDetails(
			OrderRefundBatchDetail detail) {
		return orderRefundBatchDetailDao.getOrderRefundBatchDetails(detail);
	}
	@Override
	public void insertRefundBatchDetail(OrderRefundBatchDetail detail) {
		orderRefundBatchDetailDao.insert(detail);
	}
	
}
