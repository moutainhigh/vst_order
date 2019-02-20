package com.lvmama.vst.order.processer.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.comm.utils.NewOrderSystemUtils;
import com.lvmama.vst.order.service.IOrderLocalService;

/**
 * 审核流程推送
 * @author xiaoyulin
 *
 */
public class OrdAuditProcessTaskProcesser implements MessageProcesser {
	private static final Log logger = LogFactory.getLog(OrdAuditProcessTaskProcesser.class);
	
	@Autowired
	private IOrderLocalService orderLocalService;

	@Override
	public void process(final Message message) {
		// ==判断消息来源是否新订单系统，若是，则不处理 start== 2017-09-20 by zhujingfeng
		if (NewOrderSystemUtils.isMessageFromNewOrderSystem(message.getSystemType())) {
			logger.info("OrdAuditProcessTaskProcesser process message from new order system,no need to deal !message:" + message.toString());
			return;
		}
		// ==判断消息来源是否新订单系统，若是，则不处理 end== 2017-09-20 by zhujingfeng
		if (MessageUtils.isOrderAuditReceiveMsg(message)) {
			logger.info("OrdAuditProcessTaskProcesser start,orderId:"+message.getObjectId());
			Long orderId = -1L;
			try {
				int count = 0;
				while(count < 3){// 最多重试三次
					if(count > 0){// 第二次开始调用，先sleep200ms
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							logger.error("paymentSuccess throw InterruptedException"); 
						}
					}else{
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							logger.error("paymentSuccess throw InterruptedException"); 
						}
					}
					count++;
				
					orderId = message.getObjectId();
					OrdOrder order = orderLocalService.queryOrdorderByOrderId(orderId);
					boolean flag = orderLocalService.handelAuditReceiveTask(order);
					if(flag){
						logger.info("OrdAuditProcessTaskProcesser success!"); 
						break;
					}else{
						logger.error("OrdAuditProcessTaskProcesser failure!"); 
					}
				}
			} catch (Exception e) {
				logger.error("Generate workflow failed, orderId:" + orderId);
				logger.error("{}", e);
			}
		}
	}
	
}
