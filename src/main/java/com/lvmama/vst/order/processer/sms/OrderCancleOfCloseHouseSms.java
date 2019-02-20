package com.lvmama.vst.order.processer.sms;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.vo.Constant;

public class OrderCancleOfCloseHouseSms  implements AbstractSms {

	
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(OrderCancleOfCloseHouseSms.class);
	@Override
	public List<String> exeSmsRule(OrdOrder order) {
     Long orderId = order.getOrderId();
		
		logger.info("OrderCancelSms ===>>> isCancelOfNoPass(order)=" + order.getOrderId());	
		//发送规则列表
		List<String> sendList = new ArrayList<String>();
		//不发送规则列表
		List<String> noneSendList = new ArrayList<String>();
		
		
		if(isHotel(order)&&!isPayed(order)&&orderStatus(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.CANCEL_ORDER_CLOSEHOUSE.name());
		}
	   else{
			if (logger.isWarnEnabled()) {
				logger.warn("exeSmsRule(OrdOrder) - don't found cancel order template"); //$NON-NLS-1$
			}				
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
		// TODO Auto-generated method stub
		return null;
	}
	//品类(酒店)
		public boolean isHotel(OrdOrder order){
			for(OrdOrderItem item : order.getOrderItemList()){
				if(item.hasCategory(BizEnum.BIZ_CATEGORY_TYPE.category_hotel)){
					return true;
				}
			}
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId())){
				return true;
			}
			return false;
		}
	public boolean isPayed(OrdOrder order) {
			if (OrderEnum.PAYMENT_STATUS.PAYED.name().equalsIgnoreCase(
					order.getPaymentStatus())) {
				return true;
			}
			return false;
    }
	public boolean  orderStatus(OrdOrder order){
		if(Constant.ORDER_STATUS_ENUM.NORMAL.name().equalsIgnoreCase(order.getOrderStatus())){
			return true;
		}
		return false;
	}
} 
