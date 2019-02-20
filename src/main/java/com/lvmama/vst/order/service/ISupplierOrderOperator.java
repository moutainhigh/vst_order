package com.lvmama.vst.order.service;

import java.util.Set;

import com.lvmama.vst.back.passport.po.PassProvider;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.order.vo.OrderSupplierOperateResult;

/**
 * 
 * @author sunjian
 *
 */
public interface ISupplierOrderOperator {
	/**
	 * 根据vst订单ID创建供应商订单
	 * 
	 * @param orderId
	 * @return
	 */
	public OrderSupplierOperateResult createSupplierOrder(Long orderId);
	
	/**
	 * 根据vst订单ID创建供应商订单
	 * 
	 * @param orderId
	 * @return
	 */
	public OrderSupplierOperateResult createSupplierOrder(Long orderId,Set<Long> orderItemList);
	
	/**
	 * 修改订单操作
	 * @param orderId
	 * @param addition
	 * @return
	 */
	public OrderSupplierOperateResult updateSupplierOrder(Long orderId,String addition);
	
	/**
	 * 根据vst订单ID取消供应商订单
	 * 
	 * @param orderId
	 * @return
	 */
	public OrderSupplierOperateResult cancelSupplierOrder(Long orderId);
	
	/**
	 * 为创建第三方失败而取消的订单创建取消活动
	 * 
	 * @param orderId
	 * @return
	 */
	public ResultHandle createCancelAuditForSupplierCreateFail(Long orderId);
	
	
	/**
	 * 获取服务商服务信息
	 * @param orderItemId
	 * @return
	 */
	public PassProvider getProductServiceInfo(Long goodsId);
	
	/**
	 * 根据vst订单ID创建供应商订单
	 * @param isFromComJob 是否是从comjob调用的
	 * @param orderId
	 * @return
	 */
	public OrderSupplierOperateResult createSupplierOrder(Long orderId, boolean isFromComJob);
	
	/**
	 * 根据vst订单ID创建供应商订单
	 * @param isFromComJob 是否是从comjob调用的
	 * @param orderId
	 * @return
	 */
	public OrderSupplierOperateResult createSupplierOrder(Long orderId,
			Set<Long> orderItemList, boolean isFromComJob);
}
