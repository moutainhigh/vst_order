package com.lvmama.vst.order.service;

import java.util.List;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.pub.po.ComAudit;

/**
 * 订单分单业务接口
 * 
 * @author wenzhengtao
 *
 */
public interface IOrderDistributionBusiness{
	@Deprecated
	ComAudit makeOrderAuditForInfoAudit(final ComAudit audit);
	@Deprecated
	ComAudit makeOrderAuditForResourceAudit(final ComAudit audit);
	@Deprecated
	ComAudit makeOrderAuditForCertificateAudit(final ComAudit audit);
	@Deprecated
	ComAudit makeOrderAuditForPaymentAudit(final ComAudit audit);
	@Deprecated
	ComAudit makeOrderAuditForSaleAudit(final ComAudit audit);
	@Deprecated
	ComAudit makeOrderAuditForBookingAudit(final ComAudit audit);
	@Deprecated
	ComAudit makeOrderAuditForCancelAudit(final ComAudit audit);
	
	ComAudit makeOrderAudit(final ComAudit audit);
	
	ComAudit makeOrderAuditForBookingAudit(final ComAudit audit,final String assignor,final String isGroup,final String groupOrOperator);
	
	public ComAudit makeOrderAuditForManualAudit(final OrdOrder order,
			final ComAudit audit, final String status, final List<Long> orgIds,
			String operator, String assigner, boolean isForce);
	
	/**
	 * 人工分单
	 * @param audit
	 * @param auditStatus
	 * @param assignor
	 * @param operator
	 * @return
	 */
	public ComAudit makeOrderAuditForManualAudit(final ComAudit audit, final String auditStatus,final String assignor, final String operator);
	
	/**
	 * 分销订单分单
	 * @param audit
	 * @return
	 */
	ComAudit makeOrderAuditForDistribution(final ComAudit audit);
	
	/**
	 * 获取活动渠道
	 * @param audit
	 * @return
	 */
	String getDistributionChannelByAudit(ComAudit audit);
	
	/**
	 * 通过活动获取订单
	 * @param audit
	 * @return
	 */
	public OrdOrder findOrderFromOrder(ComAudit audit);

}
