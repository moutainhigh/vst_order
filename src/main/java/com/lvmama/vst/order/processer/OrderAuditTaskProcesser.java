/**
 * 
 */
package com.lvmama.vst.order.processer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.client.ord.service.DestOrderWorkflowService;
import com.lvmama.vst.back.client.ord.service.OrderWorkflowService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdResponsible;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderFlagParam;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.order.service.IComMessageService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderResponsibleService;
import com.lvmama.vst.order.service.IOrderUpdateService;

/**
 * @author lancey
 * 
 */
public class OrderAuditTaskProcesser implements MessageProcesser {
	

	@Autowired
	private IOrderUpdateService orderUpdateService;

	@Autowired
	private IOrderAuditService orderAuditService;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private OrderWorkflowService orderWorkflowService;
	
	@Autowired
	private IOrderResponsibleService orderResponsibleService;
	
	@Autowired
	private IComMessageService comMessageService;
	
	@Autowired
	private DestOrderWorkflowService destOrderWorkflowService;
	
	private static final Logger logger = LoggerFactory.getLogger(OrderAuditTaskProcesser.class);

	@Override
	public void process(Message message) {
		logger.info("OrderAuditTaskProcesser:jms eventType="+message.getEventType()+
				",objectId="+message.getObjectId());
		if(messageCheck(message)){
			//带有订单子项的订单，主要是为了判断是否是api订单,也叫供应商订单，目前仅有艺龙订单
			OrdOrder order = this.getOrderWithOrderItemByOrderId(message.getObjectId());
			if(ActivitiUtils.hasNotActivitiOrder(order)){
				if (MessageUtils.isOrderResourcePassMsg(message)
						|| MessageUtils.isOrderInfoPassMsg(message)) {
					if (order.hasResourceAmple() && order.hasInfoPass()) {
						Map<String, Object> param = new HashMap<String, Object>();
						param.put("auditType",
								OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.name());
						param.put("objectId", order.getOrderId());
						param.put("objectType",
								OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
						// 杨斌：api取消订单不应该自动产生取消凭证活动,在产生取消凭证的位置做判断，确认凭证同理
						if (!order.isSupplierOrder()) {
							if (orderAuditService.getTotalCount(param) == 0) {
								orderAuditService.saveCreateOrderAudit(order.getOrderId(),
										OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.name());
							}
						}
					}
				} else if (MessageUtils.isOrderCancelMsg(message)) {
					if (order.hasCanceled()) {
						// 将主子单活动更新为无效
						orderWorkflowService.markTaskValid(order.getOrderId(), OrderEnum.AUDIT_OBJECT_TYPE.ORDER);
						if(order.getOrderItemList() != null && !order.getOrderItemList().isEmpty()){
							for(OrdOrderItem item : order.getOrderItemList()){
								orderWorkflowService.markTaskValid(item.getOrderItemId(), OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM);
								//将子订单确认活动更新为无效
								destOrderWorkflowService.markTaskValid(item.getOrderItemId(), OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM);
							}
						}
						
						Map<String, Object> param = new HashMap<String, Object>();
						param.put("auditType", OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.name());
						param.put("objectId", order.getOrderId());
						param.put("objectType",
								OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
						// 杨斌：api取消订单不应该自动产生取消凭证活动
						if (!order.isSupplierOrder()) {
							if (orderAuditService.getTotalCount(param) == 0) {
								orderAuditService.saveCreateOrderAudit(
										order.getOrderId(),
										OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.name());
							}
						}
					}

				}
			}
		}
	}

	public OrdResponsible getResponsibleUser(OrdOrderItem orderItem) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("objectId",orderItem.getOrderItemId());
		params.put("objectType",OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
		List<OrdResponsible> responsbleList = orderResponsibleService.findOrdResponsibleList(params);
		if(responsbleList.isEmpty()){
			return null;
		}
		OrdResponsible responsible = responsbleList.get(0);
		return responsible;
	}
	
	
	/**
	 * 消息检查
	 * @param message
	 * @return
	 */
	private boolean messageCheck(Message message){
		if(MessageUtils.isOrderResourcePassMsg(message)){
			return true;
		}else if(MessageUtils.isOrderInfoPassMsg(message)) {
			return true;
		}else if(MessageUtils.isOrderCancelMsg(message)){
			return true;
		}
		return false;
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

		List<OrdOrder> orderList = complexQueryService
				.queryOrderListByCondition(condition);
		if (orderList != null && orderList.size() == 1) {
			order = orderList.get(0);
		}
		return order;
	}

}
