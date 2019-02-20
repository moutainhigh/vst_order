package com.lvmama.vst.order.processer.sms;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.comm.vo.Constant.BU_NAME;

/**
 * 订单取消
 * @author xujibai 2015-12-25
 *
 */
public class OrderCancelApplySms implements AbstractSms {
	
	
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(OrderCancelApplySms.class);
	
	
	//支付对象(预付)
	public boolean isPrepaid(OrdOrder order){
		if(order.hasNeedPrepaid()){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public List<String> exeSmsRule(OrdOrder order) {
		logger.info("OrderCancelApplySms ===>>> isPrepaid(order)=" + isPrepaid(order)+"orderidexeSmsRule="+order.getOrderId());	
		//发送规则列表
		List<String> sendList = new ArrayList<String>();
		//不发送规则列表
		List<String> noneSendList = new ArrayList<String>();
		
		//判断订单是否为国内机酒订单
		if(BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())
				&&BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()==order.getCategoryId().longValue()
				&&BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().longValue()==order.getSubCategoryId()){
			logger.info("OrderCancelApplySms:orderId:"+order.getOrderId()+"=enter=");
			//获取模板
			sendList.add(OrdSmsTemplate.SEND_NODE.PAYAHEAD_PAY_AERO_HOTEL_CANCEL_TIMEOUT.name());
			return sendList;
		}
		//1.[主订单]订单取消申请+预付
		/**
	   	 * 检查供应商是否取消订单
	   	 * @param orderId 订单编号
	   	 * @return 1：取消成功 -1：未取消 ，0处理中
	     * @author xujibai
	   	 */
		logger.info("取消订单申请,订单ID:"+order.getOrderId());
//		if(isPrepaid(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.CANCEL_ORDER_APPLY.name());
//		}else{
//			if (logger.isWarnEnabled()) {
//				logger.warn("OrderCancelApplySms.exeSmsRule(OrdOrder) - don't found cancel order template"); //$NON-NLS-1$
//			}				
//		}
		
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
