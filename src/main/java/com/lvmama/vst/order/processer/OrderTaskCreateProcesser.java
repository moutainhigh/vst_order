/**
 * 
 */
package com.lvmama.vst.order.processer;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderFlagParam;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderAuditService;

/**
 * 订单任务生成处理器
 * @author lancey
 *
 */
public class OrderTaskCreateProcesser implements MessageProcesser{
	private static final Log LOG = LogFactory.getLog(OrderTaskCreateProcesser.class);
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrderAuditService orderAuditService;

	@Override
	public void process(Message message) {
		
		if(messageCheck(message)){
			Long orderId = message.getObjectId();
			OrdOrder order = getOrderWithOrderItemByOrderId(orderId);
			if(ActivitiUtils.hasNotActivitiOrder(order)){
				if(MessageUtils.isOrderCreateMsg(message)||MessageUtils.isOrderPaymentMsg(message)){
					//订单创建并且是强制预授权不创建
					if(MessageUtils.isOrderCreateMsg(message)&&order.isPayMentType()){
						return;
					}
					//订单支付如果不是强制授权不创建
					if(MessageUtils.isOrderPaymentMsg(message)&&!order.isPayMentType()){
						return;
					}
						
					//信息审核 
					// 1、信息状态：未确认
					if (OrderEnum.INFO_STATUS.UNVERIFIED.name().equals(order.getInfoStatus())) {
						ComAudit audit = makeComAuditForCreateProcesser(order, OrderEnum.AUDIT_TYPE.INFO_AUDIT.name(), OrderEnum.AUDIT_STATUS.POOL.name());
						if (audit != null) {
							//api订单
							if (order.isSupplierOrder()) {
								audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
								audit.setOperatorName("SYSTEM");
								LOG.info("OrderTaskCreateProcesser.process: msg=Order(ID=" + orderId + "), 第三方供应商订单信息状态审核通过活动处理完成。");
							//后台下单
							} else if (Constant.DIST_BACK_END == order.getDistributorId()) {
								audit.setAuditStatus(OrderEnum.AUDIT_STATUS.PROCESSED.name());
								audit.setOperatorName(order.getBackUserId());
								LOG.info("OrderTaskCreateProcesser.process: msg=Order(ID=" + orderId + "), 后台下单信息状态审核通过活动处理完成。");
							}
							orderAuditService.saveAudit(audit);
						}
					} 
				}else if(MessageUtils.isOrderInfoPassMsg(message)){
					//OrdOrder order = getOrderByOrderId(message.getObjectId());
					
					//资源审核
					//1、资源状态：未审核
					if (OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(order.getResourceStatus())) {
						ComAudit audit = makeComAuditForCreateProcesser(order, OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name(), OrderEnum.AUDIT_STATUS.POOL.name());
						if (audit != null) {
							if (order.isSupplierOrder()) {
								audit.setOperatorName("SYSTEM");
								audit.setAuditStatus(OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
							}
							orderAuditService.saveAudit(audit);
						}
					//2、资源状态：审核通过
					} else if (OrderEnum.RESOURCE_STATUS.AMPLE.name().equals(order.getResourceStatus())) {
						ComAudit audit = makeComAuditForCreateProcesser(order, OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.name(), OrderEnum.AUDIT_STATUS.PROCESSED.name());
						if (audit != null) {
							orderAuditService.saveAudit(audit);
						}
					}
				}
			}
		}
		
	}
	
	/**
	 * 消息检查
	 * @param message
	 * @return
	 */
	private boolean messageCheck(Message message){
		if(MessageUtils.isOrderCreateMsg(message)){
			return true;
		}else if(MessageUtils.isOrderInfoPassMsg(message)){
			return true;
		}
		return false;
	}
	

	/**
	 * 创建ComAudit对象
	 * 
	 * @param order
	 * @param auditType
	 * @param auditStatus
	 * @return
	 */
	private ComAudit makeComAuditForCreateProcesser(OrdOrder order, String auditType, String auditStatus) {
		ComAudit audit = null;
		if (order != null) {
			audit = new ComAudit();
			audit.setCreateTime(new Date());
			audit.setObjectId(order.getOrderId());
			audit.setObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
			audit.setAuditType(auditType);
			audit.setAuditStatus(auditStatus);

			OrdOrder simpleOrder = new OrdOrder();
			simpleOrder.setOrderId(order.getOrderId());
			audit.setOrder(simpleOrder);

			if (OrderEnum.AUDIT_STATUS.PROCESSED.name().equals(auditStatus)) {
				audit.setOperatorName("SYSTEM");
			}
		}
		
		return audit;
	}
	
	/**
	 * 根据OrderId返回单个带有订单子项Order对象
	 * 
	 * @param orderId
	 * @return
	 */
	private OrdOrder getOrderWithOrderItemByOrderId(Long orderId) {
		OrdOrder order = null;
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(orderId);
		
		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderItemTableFlag(true);

		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);
		
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		if (orderList != null && orderList.size() == 1) {
			order = orderList.get(0);
		}
		
		return order;
	}
	
	/**
	 * 根据OrderId返回单个带有订单子项Order对象
	 * 
	 * @param orderId
	 * @return
	 */
	private OrdOrder getOrderByOrderId(Long orderId) {
		OrdOrder order = null;
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(orderId);
		
		condition.setOrderIndentityParam(orderIndentityParam);
		
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		if (orderList != null && orderList.size() == 1) {
			order = orderList.get(0);
		}
		
		return order;
	}
}
