/**
 * 
 */
package com.lvmama.vst.order.processer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.vo.Constant.BU_NAME;
import com.lvmama.vst.order.service.IOrderUpdateService;

/**
 * @author lancey
 *
 */
public class UpdateOrderViewStatusProcesser implements MessageProcesser{
	
	private static final Log LOG = LogFactory.getLog(UpdateOrderViewStatusProcesser.class);
	
	@Autowired
	private IOrderUpdateService orderUpdateService;

	@Override
	public void process(Message message) {
		if(messageCheck(message)){
			Long orderId = message.getObjectId();
			OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
			//检查不是ActivitiOrder则处理
			if(ActivitiUtils.hasNotActivitiOrder(order)){
				if(MessageUtils.isOrderPaymentMsg(message)){
					orderPaymentMsg(message,order);
				}else if(MessageUtils.isOrderResourcePassMsg(message)){
					orderResourcePassMsg(message,order);
				}else if(MessageUtils.isOrderInfoPassMsg(message)) {
					orderInfoPassMsg(message,order);
				}else if(MessageUtils.isOrderCancelMsg(message)){
					orderCancelMsg(message);
				}
			}
		}
	}
	
	/**
	 * 消息检查
	 * @param message
	 * @return
	 */
	private boolean messageCheck(Message message){
		if(MessageUtils.isOrderPaymentMsg(message)){
			return true;
		}else if(MessageUtils.isOrderResourcePassMsg(message)){
			return true;
		}else if(MessageUtils.isOrderInfoPassMsg(message)) {
			return true;
		}else if(MessageUtils.isOrderCancelMsg(message)){
			return true;
		}
		return false;
	}
	
	
	/**
	 * 订单支付成功消息处理
	 * @param message
	 */
	private void orderPaymentMsg(Message message,OrdOrder order){
		LOG.info(message + "orderPaymentMsg begin");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("orderId", message.getObjectId());
		if(BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())){
			if(((BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()==order.getCategoryId().longValue()
					&&BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().longValue()==order.getSubCategoryId())
					||BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().longValue()==order.getCategoryId().longValue())
					&&OrdOrderUtils.isLocalBuFrontOrder(order)){
				map.put("viewOrderStatus", OrderEnum.ORDER_VIEW_STATUS.UNVERIFIED.name());
			}
		}else if(OrdOrderUtils.isBusHotelOrder(order)){
			map.put("viewOrderStatus", OrderEnum.ORDER_VIEW_STATUS.UNVERIFIED.name());
	    }else{
			map.put("viewOrderStatus", OrderEnum.ORDER_VIEW_STATUS.PAYED.name());
		}
		orderUpdateService.updateViewStatus(map);
		LOG.info(message + " orderPaymentMsg end " + map);
	}
	
	/**
	 * 资源审核通过消息处理
	 * @param message
	 */
	private void orderResourcePassMsg(Message message,OrdOrder order){
		LOG.info(message + "orderResourcePassMsg begin");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("orderId",message.getObjectId());
		if (!OrderEnum.PAYMENT_STATUS.PAYED.getCode().equals(order.getPaymentStatus())) {
			map.put("viewOrderStatus", OrderEnum.ORDER_VIEW_STATUS.WAIT_PAY.name());
		}else{
			map.put("viewOrderStatus", OrderEnum.ORDER_VIEW_STATUS.PAYED.name());
		}
		orderUpdateService.updateViewStatus(map);
		LOG.info(message + "orderResourcePassMsg end " + map);
	}
	
	/**
	 * 信息审核通过消息处理
	 * @param message
	 */
	private void orderInfoPassMsg(Message message,OrdOrder order){
		LOG.info(message + "orderInfoPassMsg begin");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("orderId",message.getObjectId());
		if (OrderEnum.RESOURCE_STATUS.AMPLE.name().equals(order.getResourceStatus())) {
			if (!OrderEnum.PAYMENT_STATUS.PAYED.getCode().equals(order.getPaymentStatus())) {
				map.put("viewOrderStatus", OrderEnum.ORDER_VIEW_STATUS.WAIT_PAY.name());
			}else{
				map.put("viewOrderStatus", OrderEnum.ORDER_VIEW_STATUS.PAYED.name());
			}
			orderUpdateService.updateViewStatus(map);
		} else if (OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(order.getResourceStatus())) {
			map.put("viewOrderStatus", OrderEnum.ORDER_VIEW_STATUS.APPROVING.name());
			orderUpdateService.updateViewStatus(map);
		}
		LOG.info(message + "orderInfoPassMsg end " + map);
	}
	
	/**
	 * 订单取消消息处理
	 * @param message
	 */
	private void orderCancelMsg(Message message){
		LOG.info(message + "orderCancelMsg begin");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("orderId", message.getObjectId());
		map.put("viewOrderStatus", OrderEnum.ORDER_VIEW_STATUS.CANCEL.name());
		orderUpdateService.updateViewStatus(map);
		LOG.info(message + "orderCancelMsg end " + map);
	}
	
}
