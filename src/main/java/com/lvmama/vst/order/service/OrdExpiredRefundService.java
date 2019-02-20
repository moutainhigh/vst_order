package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.vst.back.order.po.OrdExpiredRefund;
import com.lvmama.vst.comm.vo.order.OrdExpiredRefundRst;

public interface OrdExpiredRefundService {
	
	/**
	 * 插入过期退意向单数据
	 * 
	 * @param ordExpiredRefund OrdExpiredRefund
	 * @return int
	 */
	int insert(OrdExpiredRefund ordExpiredRefund); 
	
	/**
	 * 批量插入过期退意向单数据
	 * 
	 * @param list List<OrdExpiredRefund>
	 * @return int
	 */
	int batchInsert(List<OrdExpiredRefund> list);
	
	/**
	 * 分页查询过期退数据列表
	 * 
	 * @param params Map<String, Object>
	 * @return List<OrdExpiredRefundRst>
	 */
	@ReadOnlyDataSource
	List<OrdExpiredRefundRst> queryListForPage(Map<String, Object> params);
	
	/**
	 * 分页查询过期退列表COUNT
	 * 
	 * @param params Map<String, Object>
	 * @return Long
	 */
	@ReadOnlyDataSource
	Long queryTotalCountForPage(Map<String, Object> params);

}
