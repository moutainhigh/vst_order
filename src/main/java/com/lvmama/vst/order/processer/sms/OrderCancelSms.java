package com.lvmama.vst.order.processer.sms;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.utils.OrderUtils;

/**
 * 订单取消
 * @author zhaomingzhu
 *
 */
public class OrderCancelSms implements AbstractSms {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(OrderCancelSms.class);
	

	//支付对象(预付)
	public boolean isPrepaid(OrdOrder order){
		if(order.hasNeedPrepaid()){
			return true;
		}else{
			return false;
		}
	}
	
	//支付对象(现付)
	public boolean isToPay(OrdOrder order){
		if(order.hasNeedPay()){
			return true;
		}
		return false;
	}
	
	//取消类型(其它取消，审核不通过)
	public boolean isCancelOfNoPass(OrdOrder order){
		if(String.valueOf(Constants.ORDER_CANCEL_TYPE_RESOURCE_NO_CONFIM).equalsIgnoreCase(order.getCancelCode())){
			return true;
		}
		return false;
	}
	//取消类型(超时)
	public boolean isCancelOfTimeOut(OrdOrder order){
		if(OrderEnum.ORDER_CANCEL_CODE.TIME_OUT_WAIT.name().equalsIgnoreCase(order.getCancelCode())){
			return true;
		}
		return false;
	}
	//预订限制 (预授权)
	public boolean isPreauth(OrdOrder order){
		if(order.getPaymentType() != null && SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name().equalsIgnoreCase(order.getPaymentType())){
			return true;
		}else{
			return false;
		}
	}
	//支付状态(未支付)
	public boolean isUnpay(OrdOrder order){
		if(OrderEnum.PAYMENT_STATUS.UNPAY.name().equalsIgnoreCase(order.getPaymentStatus())){
			return true;
		}
		return false;
	}
	// 支付状态(已支付)
	public boolean isPayed(OrdOrder order) {
		if (OrderEnum.PAYMENT_STATUS.PAYED.name().equalsIgnoreCase(
				order.getPaymentStatus())) {
			return true;
		}
		return false;
	}
	//退款金额(有退款)
	public boolean hasRefundedAmount(OrdOrder order){
		if(order.getRefundedAmount()!=null && order.getRefundedAmount()>0L){
			return true;
		}
		return false;
	}
	//无退款金额(有退款)
	public boolean hasNoRefundedAmount(OrdOrder order){
		if(order.getRefundedAmount()==null || order.getRefundedAmount() == 0L){
			return true;
		}
		return false;
	}	
	//退款渠道(原路退回)
	public boolean isSamePaymentGateway(){
		return false;
	}
	
	// 是否是门票订单
	public boolean isTicketOrder(OrdOrder order) {
		if (OrderUtils.isTicketByCategoryId(order.getCategoryId())) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isDestBuOrder(OrdOrder order) {
		if (order.getBuCode().equals(Constant.BU_NAME.DESTINATION_BU.name())) {
			return true;
		} else {
			return false;
		}
	}
	@Override
	public List<String> exeSmsRule(OrdOrder order) {
//		Long orderId = order.getOrderId();
		
		logger.info("OrderCancelSms ===>>> isCancelOfNoPass(order)=" + order.getOrderId() + ",isCancelOfNoPass:" + isCancelOfNoPass(order)
				+ "isUnpay(order)=" + isUnpay(order)
				+ "isPreauth(order)=" + isPreauth(order)
				+ "isPrepaid(order)=" + isPrepaid(order)
				+ "isCancelOfTimeOut(order)=" + isCancelOfTimeOut(order)
				+ "hasNoRefundedAmount(order)=" + hasNoRefundedAmount(order)
				+"orderidexeSmsRule="+order.getOrderId()
			);	
		//发送规则列表
		List<String> sendList = new ArrayList<String>();
		//不发送规则列表
		List<String> noneSendList = new ArrayList<String>();
		
		//[目的地主订单]订单取消+审核不通过+已支付+预付
		if(isDestBuOrder(order) && isCancelOfNoPass(order) && isPayed(order) && isPrepaid(order) && !OrdOrderUtils.isBusHotelOrder(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.CANCEL_VERIFIED_PAYED_PREPAY.name());
		}
		//1.[主订单]订单取消+审核不通过+未支付+非预授权
		else if(isCancelOfNoPass(order) && isUnpay(order) && !isPreauth(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.CANCEL_VERIFIED_UNPAY_UNPREAUTH.name());
		}
		//2.[主订单]订单取消+审核不通过+已预授权	([主订单]订单取消+审核不通过+已预授权+预付)
		else if(isCancelOfNoPass(order) && isPreauth(order) && isPrepaid(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.CANCEL_VERIFIED_UNPAY_PREAUTH_PREPAY.name());
		}
		//3.1.[主订单]订单取消+超时未支付取消		([主订单]订单取消+超时未支付取消+预付)
		else if(isCancelOfTimeOut(order) && isUnpay(order) && isPrepaid(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.CANCEL_TIMEOUT_PREPAID.name());
		}
		//3.2.[主订单]订单取消成功+到付	
		else if(isToPay(order)){			
			sendList.add(OrdSmsTemplate.SEND_NODE.CANCEL_TO_PAY.name());
		}
		//国内机加酒或者超级自由行、已支付，审核不通过，取消
		else if(((BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId() == order.getCategoryId()
					&& BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(order.getSubCategoryId()))
				|| BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId() == order.getCategoryId())
				&&CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())
				&& isCancelOfNoPass(order) && isPayed(order) ){
			sendList.add(OrdSmsTemplate.SEND_NODE.CANCEL_NO_REFUND_ROUTE_FLIGHT_HOTEL.name());
		}
		//4.[主订单]订单取消申请+无退款				([主订单]订单取消申请+无退款)
		else if(hasNoRefundedAmount(order) && !isTicketOrder(order)){
			sendList.add(OrdSmsTemplate.SEND_NODE.CANCEL_NO_REFUND.name());
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
		return null;
	}
}
