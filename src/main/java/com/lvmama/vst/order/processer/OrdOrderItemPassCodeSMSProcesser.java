package com.lvmama.vst.order.processer;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItemPassCodeSMS;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.comm.utils.NewOrderSystemUtils;
import com.lvmama.vst.order.service.IOrdOrderItemPassCodeSMSService;
import com.lvmama.vst.order.service.IOrderUpdateService;

/**
 * Created at 2015/9/6
 * @author yangzhenzhong
 * 新建一个process 处理资源审核通过的订单，当该订单是当天创建，BU是目的地，产品是自由行，已经申码成功且延迟发送短信的，在这里检测到资源审核通过后发送短信。
 * 申码和资源审核都是异步操作，需要考虑先后的问题。
 */
public class OrdOrderItemPassCodeSMSProcesser implements MessageProcesser {
	

	@Autowired
	private IOrderUpdateService orderUpdateService;
	
    @Resource
    private TopicMessageProducer passportSmsMessageProducer;
	
	@Autowired
	private IOrdOrderItemPassCodeSMSService ordOrderItemPassCodeSMSLocalService; 
	
	private static final Logger logger = LoggerFactory.getLogger(OrdOrderItemPassCodeSMSProcesser.class);

	@Override
	public void process(Message message) {
		
		// ==判断消息来源是否新订单系统，若是，则不处理 start== 2017-09-20 by zhujingfeng
		if (NewOrderSystemUtils.isMessageFromNewOrderSystem(message.getSystemType())) {
			logger.info("OrdOrderItemPassCodeSMSProcesser process message from new order system,no need to deal !message:" + message.toString());
			return;
		}
		// ==判断消息来源是否新订单系统，若是，则不处理 end== 2017-09-20 by zhujingfeng
		
		logger.info("OrdOrderItemPassCodeSMSProcesser:jms eventType="+message.getEventType()+",objectId="+message.getObjectId());
		/**
		 * update by xiexun 此处增加信息审核类型的消息
		 */
		if(MessageUtils.isOrderResourcePassMsg(message) || MessageUtils.isOrderInfoPassMsg(message)){
			
			OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(message.getObjectId());
			
			if(OrderEnum.INFO_STATUS.INFOPASS.name().equals(order.getInfoStatus()) && OrderEnum.RESOURCE_STATUS.AMPLE.name().equals(order.getResourceStatus()) 
					 &&  order.getBuCode().equals(CommEnumSet.BU_NAME.DESTINATION_BU.name())
					 && BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId())){
				
				//根据orderId查询延迟发送短信的数据表，检索出status=Y 即未操作的记录(只考虑申码成功早于审核完成的情况，晚于的情况在判断是否需要延迟发短信时已处理，处理过的记录status为N)
				List<OrdOrderItemPassCodeSMS> ordOrderItemPassCodeSMSList= ordOrderItemPassCodeSMSLocalService.queryByOrderId(order.getOrderId());
				
				//如果未处理过的数据存在
				if(ordOrderItemPassCodeSMSList!=null && ordOrderItemPassCodeSMSList.size()>0){
					for(OrdOrderItemPassCodeSMS ordOrderItemPassCodeSMS:ordOrderItemPassCodeSMSList){
					
						//发送申码成功短信
						passportSmsMessageProducer.sendMsg(MessageFactory.newPasscodeApplySuccessMessage(ordOrderItemPassCodeSMS.getPassCodeId(),"false"));
						
						//更新处理过的数据的状态，set status=N
						ordOrderItemPassCodeSMSLocalService.updateStatus(ordOrderItemPassCodeSMS.getId());
						
					}
				}
			}
		}
	}
}