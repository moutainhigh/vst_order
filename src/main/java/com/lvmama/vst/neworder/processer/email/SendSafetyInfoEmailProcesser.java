package com.lvmama.vst.neworder.processer.email;

import javax.annotation.Resource;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.order.service.IOrderLocalService;

/** 
* @ImplementProject vst_order
* @Description: 出境BU发送信息安全卡邮件 消息 
* @author xiaoyulin
* @date 2017年8月24日 上午10:02:01 
*/
public class SendSafetyInfoEmailProcesser implements MessageProcesser {
	
	@Resource
	private IOrderLocalService orderLocalService;

	@Override
	public void process(Message message) {
		if(MessageUtils.isSendSafetyInfoEmailMessage(message)){
			Long orderId = message.getObjectId();
			OrdOrder ordOrder = orderLocalService.queryOrdorderByOrderId(orderId);
			if(ordOrder != null){
				orderLocalService.sendSafetyInfoEmail(ordOrder);
			}
		}
	}

}
