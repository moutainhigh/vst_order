package com.lvmama.vst.order.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrderRefundBatch;
import com.lvmama.vst.comm.mybatis.MyBatisDao;

@Repository
public class OrderRefundBatchDao extends MyBatisDao {

	public OrderRefundBatchDao() {
		super("REFUND_BATCH");
	}
	
	/**
	 * 根据子订单ID查询退款明细(按时间倒序)
	 * @param orderItem
	 * @return
	 */
	public List<OrderRefundBatch> getOrderRefundBatch(Map<String,Object> map) {
		return super.queryForList("getOrderRefundBatch", map);
	}
	/**
	 * 根据订单ID查询退款明细(按退款申请时间倒序)
	 * @param orderId
	 * @return
	 */
	public List<OrderRefundBatch> getRefundBatchAndSuppGoods(Long orderId) {
		return super.queryForList("getRefundBatchAndSuppGoods", orderId);
	}
	
	public int insert(OrderRefundBatch batch){
		return super.insert("insert", batch);
	}

	public int updateStatus(Map<String,Object> map) {
		return super.insert("updateStatus", map);
		
	}
	
	public int update(Map<String,Object> map) {
		return super.insert("updateSelective", map);
		
	}
}