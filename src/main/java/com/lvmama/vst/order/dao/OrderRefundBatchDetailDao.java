package com.lvmama.vst.order.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrderRefundBatchDetail;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrderRefundBatchDetailDao extends MyBatisDao {

	public OrderRefundBatchDetailDao() {
		super("REFUND_BATCH_DETAIL");
	}
	/**
	 * 根据子订单ID和批次ID查询退款明细日志(按时间倒序)
	 * @param orderItem
	 * @return
	 */
	public List<OrderRefundBatchDetail> getOrderRefundBatchDetails(OrderRefundBatchDetail detail) {
		return super.queryForList("getOrderRefundBatchDetails", detail);
	}
	
	public void insert(OrderRefundBatchDetail detail){
		super.insert("insert", detail);
	}
}