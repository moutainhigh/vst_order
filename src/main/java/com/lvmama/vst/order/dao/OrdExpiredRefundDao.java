package com.lvmama.vst.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.lvmama.vst.back.order.po.OrdExpiredRefund;
import com.lvmama.vst.comm.mybatis.MyBatisDao;
import com.lvmama.vst.comm.vo.order.OrdExpiredRefundRst;
import com.lvmama.vst.order.po.OverdueTicketSubOrderStatusPack;

/**
 * 过期退意向单DAO
 */
@Repository
public class OrdExpiredRefundDao extends MyBatisDao {

	public OrdExpiredRefundDao() {
		super("ORD_EXPIRED_REFUND");
	}
	
	/**
	 * 新增过期退意向单
	 * 
	 * @param ordExpiredRefund OrdExpiredRefund
	 * @return int
	 */
	public int insert(OrdExpiredRefund ordExpiredRefund) {
		return super.insert("insert", ordExpiredRefund);
	}
	
	/**
	 * 批量新增过期退意向单
	 * 
	 * @param list List<OrdExpiredRefund>
	 * @return int
	 */
	public int batchInsert(List<OrdExpiredRefund> list) {
		return super.insert("batchInsert", list);
	}
	
	/**
	 * 更新过期退意向单
	 * 
	 * @param ordExpiredRefund OrdExpiredRefund
	 * @return int
	 */
	public int update(OrdExpiredRefund ordExpiredRefund) {
		return super.update("update", ordExpiredRefund);
	}
	
	/**
	 * 根据订单id查询过期退意向单数据集
	 * 
	 * @param orderId Long
	 * @return List<OrdExpiredRefund>
	 */
	public List<OrdExpiredRefund> selectByOrderId(Long orderId) {
		return super.queryForList("selectByOrderId", orderId);
	}
	
	/**
	 * 根据子订单id查询过期退意向单
	 * 
	 * @param orderItemId Long
	 * @return OrdExpiredRefund
	 */
	public OrdExpiredRefund selectByOrderItemId(Long orderItemId) {
		return super.get("selectByOrderItemId", orderItemId);
	}
	
	/**
	 * 分页查询过期退数据列表
	 * 
	 * @param params Map<String, Object>
	 * @return List<OrdExpiredRefundRst>
	 */
	public List<OrdExpiredRefundRst> queryListForPage(Map<String, Object> params) {
		return super.queryForList("queryListForPage", params);
	}
	
	/**
	 * 分页查询过期退列表COUNT
	 * 
	 * @param params Map<String, Object>
	 * @return Long
	 */
	public Long queryTotalCountForPage(Map<String, Object> params) {
		return super.get("queryTotalCountForPage", params);
	}
	
	/**
	 * 根据主键ID删除过期退意向单
	 * 
	 * @param id Long
	 * @return int
	 */
	public int deleteByPrimaryKey(Long id) {
		return super.delete("deleteByPrimaryKey", id);
	}
	
	/**
	 * 根据订单ID删除过期退意向单集
	 * 
	 * @param orderId Long
	 * @return int
	 */
	public int deleteByOrderId(Long orderId) {
		return super.delete("deleteByOrderId", orderId);
	}
	
	/**
	 * 根据子订单ID删除过期退意向单集
	 * 
	 * @param orderItemId Long
	 * @return int
	 */
	public int deleteByOrderItemId(Long orderItemId) {
		return super.delete("deleteByOrderItemId", orderItemId);
	}
	
	/**
	 * 获取所有的过期退意向订单的供应商ID
	 * 
	 * @return 供应商ID列表
	 */
	public List<Integer> getSupplierIdList() {
		return super.getList("getSupplierIdList");
	}
	
	/**
	 * 查询大于指定id的，指定行数，指定状态的数据
	 * @param params
	 * @return
	 */
	public List<OrdExpiredRefund> queryAuditingByMinId(Map<String, Object> params) {
		return super.getList("queryAuditingByMinId", params);
	}
	
	public int updateStatusInBatch(OverdueTicketSubOrderStatusPack param) {
		return super.update("updateStatusInBatch", param);
	}
	
	public List<OrdExpiredRefund> getIdAndStatusOfNotFullyProcessed(Map<String, Object> param) {
		if (param == null)
			param = new HashMap<String, Object>();
		return super.getList("getIdAndStatusOfNotFullyProcessed", param);
	}	
}
