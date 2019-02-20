package com.lvmama.vst.order.processer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
/**
 * 订单创建消息异步处理
 * @author yanghaifeng@lvmama.com
 *
 */
public class OrderCreateMessageProcesser implements MessageProcesser {
	
	private static final Log LOG = LogFactory.getLog(OrderCreateMessageProcesser.class);
	
	@Autowired
	private TopicMessageProducer orderMessageProducer;

	@Override
	public void process(Message message) {
		LOG.info("OrderCreateMessageProcesser message objectId:"+message.getObjectId()+",eventType:"+message.getEventType());
		orderMessageProducer.sendMsg(MessageFactory.newOrderCreateMessage(message.getObjectId()));
	}

}