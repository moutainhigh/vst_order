package com.lvmama.vst.neworder.processer.audit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.order.service.IComMessageService;

/** 
* @ImplementProject vst_order
* @Description: 取消订单预定通知消息处理 
* @author xiaoyulin
* @date 2017年8月29日 下午2:23:42 
*/
public class ComMessageCreateProcesser implements MessageProcesser {
	
	private static final Log LOG = LogFactory
			.getLog(ComMessageCreateProcesser.class);
	
	@Autowired
	private IComMessageService comMessageService;

	@Override
	public void process(Message message) {
		Long orderId = message.getObjectId();
		String loginUserId = message.getAddition();
//		String eventType = message.getEventType();
		int num = 0;
		LOG.info("ComMessageCreateProcesser,orderId:" + orderId + ",message:" + message.toString());
		if(MessageUtils.isComMsgAfterCancelMsg(message)){
			num = comMessageService.saveReservationAfterCan(orderId, loginUserId);
			if(num < 1){
				LOG.error("ComMessageCreateProcesser-创建取消订单预定通知失败，orderId:" + orderId);
			}
		}else if(MessageUtils.isCloseHousePaidMsg(message)){
			num = comMessageService.savaReservationAfterCalOfCloseHourse(orderId, loginUserId);
			if(num < 1){
				LOG.error("ComMessageCreateProcesser-创建酒店关房预定通知失败，orderId:" + orderId);
			}
		}
	}

}
