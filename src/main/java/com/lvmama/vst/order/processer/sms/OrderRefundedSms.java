package com.lvmama.vst.order.processer.sms;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.order.utils.OrderUtils;


/**
 * 订单退款
 * @author zhaomingzhu
 *
 */
public class OrderRefundedSms implements AbstractSms {
	private static final Logger logger = LoggerFactory.getLogger(OrderRefundedSms.class);

	//支付对象(预付)
	public boolean isPrepaid(OrdOrder order){
		if(order.hasNeedPrepaid()){
			return true;
		}else{
			return false;
		}
	}
	//是否是门票订单
	public boolean isTicketOrder(OrdOrder order){
		if(OrderUtils.isTicketByCategoryId(order.getCategoryId())){
			return true;
		}else{
			return false;
		}
	}
	@Override
	public List<String> exeSmsRule(OrdOrder order) {
		logger.info("OrderRefundedSms ===>>> isPrepaid(order)=" + isPrepaid(order)+"orderidexeSmsRule="+order.getOrderId());			
		//发送规则列表
		List<String> sendList = new ArrayList<String>();
		//不发送规则列表
		List<String> noneSendList = new ArrayList<String>();
		
		//1.[主订单]正常+退款	([主订单]正常+退款+预付)
		if(isPrepaid(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.ORDER_NORMAL_REFUND.name());
		}
		if(noneSendList.size() >0){
			for(String noneSend : noneSendList){
				if(sendList.contains(noneSend)){
					sendList.remove(noneSend);
				}
			}
		}
		return sendList;
	}

	@Override
	public String fillSms(String content, OrdOrder order) {
		return null;
	}

}
