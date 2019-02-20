package com.lvmama.vst.order.processer.payment;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.comm.pool.ThreadPoolExcutor;
import com.lvmama.vst.back.client.activiti.service.ProcesserClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.comm.vo.ActivitiKey;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.ComActivitiRelationService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderUpdateService;

/**
 * JMS驱动支付后续流程启动
 * 
 * @author minchun
 * @2016-01-21
 *
 */
public class JMSDrivenPaymentProcesser implements MessageProcesser {
	private static final Log logger = LogFactory.getLog(JMSDrivenPaymentProcesser.class);

	@Autowired
	private IOrderUpdateService orderUpdateService;

	@Autowired
	private IComplexQueryService complexQueryService;

	@Autowired
	private ProcesserClientService processerClientService;

	@Autowired
	private ComActivitiRelationService comActivitiRelationService;

	@Autowired
	private OrderService orderService;

	@Override
	public void process(final Message message) {

		Thread task = new Thread() {
			@Override
			public void run() {
				logger.info("JMSDrivenPaymentProcesser threading trigger payment startup.....");
				if (!message.hasOrderMessage()) {
					logger.info("JMSDrivenPaymentProcesser threading message is empty");
					return;
				}

				if (MessageUtils.isOrderPaymentMsg(message)) {
					Long orderID = message.getObjectId();
					OrdOrder order = orderService.queryOrdorderByOrderId(orderID);
					Long distributorID = order.getDistributorId();
					if (distributorID.intValue() == 2) {
						return;
					}
					if (ActivitiUtils.hasActivitiOrder(order)) {
						logger.info("JMSDrivenPaymentProcesser threading call processerClientService.paymentSuccess()" );
						logger.info("JMSDrivenPaymentProcesser ProcessKey:" + order.getProcessKey() + ", orderID:" + order.getOrderId());
						String lockKey = new StringBuffer(order.getProcessKey()).append(order.getOrderId()).toString();
						boolean isLock = MemcachedUtil.getInstance().tryLock(lockKey);
						if (!isLock) {
							String lockValue = MemcachedUtil.getInstance().get(lockKey);
							logger.info("JMSDrivenPaymentProcesser tryLock status:" + isLock + ", Lock key:" + lockKey + ", value :" +  lockValue);
							return;
						}
						logger.info("JMSDrivenPaymentProcesser createKeyByOrder");
						ActivitiKey activitiKey = createKeyByOrder(order);
						logger.info("JMSDrivenPaymentProcesser threading " + getClass().getName() + " trigger processerClientService.paymentSuccess("
								+ activitiKey.getBussinessKey() + ", " + activitiKey.getProcesserId() + ")");
						if(StringUtils.isNotBlank(activitiKey.getProcesserId())) {
							processerClientService.paymentSuccess(activitiKey);
							Map<String, Object> paramsMap = new HashMap<String, Object>();
							paramsMap.put("orderId", order.getOrderId());
							paramsMap.put("payProcTriggered", "Y");
							complexQueryService.updatePayProcTriggeredByOrderID(paramsMap);
						}
					}
				}

			}

			private ActivitiKey createKeyByOrder(OrdOrder order) {
				ComActivitiRelation relation = getRelation(order);
				return new ActivitiKey(relation, ActivitiUtils.createOrderBussinessKey(order));
			}

			/**
			 * 根据Order查找创建的工作流节点
			 * 
			 * @param order
			 * @return ComActivitiRelation
			 * @see com.lvmama.vst.order.client.ord.service.impl.OrdOrderClientServiceImpl#getRelation
			 */
			public ComActivitiRelation getRelation(OrdOrder order) {
				try {
					ComActivitiRelation comActiveRelation = comActivitiRelationService.queryRelation(order.getProcessKey(), order.getOrderId(),
							ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
					if (comActiveRelation == null) {// 补偿机制,通过工作流再次去触发查询
						String processId = processerClientService.queryProcessIdByBusinessKey(ActivitiUtils.createOrderBussinessKey(order));
						if (processId != null) {
							comActivitiRelationService.saveRelation(order.getProcessKey(), processId, order.getOrderId(),
									ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
							comActiveRelation = new ComActivitiRelation();
							comActiveRelation.setObjectId(order.getOrderId());
							comActiveRelation.setObjectType(ComActivitiRelation.OBJECT_TYPE.ORD_ORDER.name());
							comActiveRelation.setProcessId(processId);
							comActiveRelation.setProcessKey(order.getProcessKey());
						}
					}
					return comActiveRelation;
				} catch (Exception e) {
					logger.error("ComActivitiRelation getRelation error:" + e);
				}
				return null;
			}

		};
		String asyncSwitch = Constant.getInstance().getProperty(Constant.WORKFLOW_ASYNC_SWITCH);
		logger.info("是否开启工作流异步标识, order.workflow.async : " + asyncSwitch);
		if (StringUtils.isNotBlank(asyncSwitch) && Boolean.TRUE.toString().equalsIgnoreCase(asyncSwitch)) {
			ThreadPoolExcutor.execute(task);
		}
	}
}
