package com.lvmama.vst.order.service.refund.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrderRefundBatch;
import com.lvmama.vst.back.order.po.OrderRefundBatchDetail;
import com.lvmama.vst.order.dao.OrderRefundBatchDao;
import com.lvmama.vst.order.service.refund.OrderRefundBatchService;

@Service
public class OrderRefundBatchServiceImpl implements OrderRefundBatchService{
	private static Logger LOG = LoggerFactory.getLogger(OrderRefundBatchServiceImpl.class);
	@Autowired
	private OrderRefundBatchDao orderRefundBatchDao;
	
	@Override
	public List<OrderRefundBatch> getOrderRefundBatch(Map<String,Object> map) {
		return orderRefundBatchDao.getOrderRefundBatch(map);
	}
	
	@Override
	public List<OrderRefundBatch> getRefundBatchAndSuppGoods(Long orderId) {
		return orderRefundBatchDao.getRefundBatchAndSuppGoods(orderId);
	}
	
	@Override
	public void insertOrderRefundBatch(OrderRefundBatch batch) {
		orderRefundBatchDao.insert(batch);
	}
	
	@Override
	public void updateStatus(Map<String,Object> map) {
		orderRefundBatchDao.updateStatus(map);
	}
	
	@Override
	public void updateOrderRefundBatch(Map<String,Object> map) {
		orderRefundBatchDao.update(map);
	}
	
}