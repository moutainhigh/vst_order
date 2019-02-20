package com.lvmama.vst.order.service.refund.adapter.impl;

import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import com.lvmama.vst.back.biz.po.BizEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.order.service.refund.IOrderRefundProcesserService;
import com.lvmama.vst.order.service.refund.OrderRefundComService;
import com.lvmama.vst.order.service.refund.OrderRefundComService.ORDER_REFUND_SERVICE_TYPE_KEY;
import com.lvmama.vst.order.service.refund.adapter.OrderRefundProcesserAdapter;
/**
 * 退款流程适配
 * @version 1.0
 */
@Service("orderRefundProcesserAdapter")
public class OrderRefundProcesserAdapterImpl implements OrderRefundProcesserAdapter{
	private static final Log LOG = LogFactory.getLog(OrderRefundProcesserAdapterImpl.class);
	@Autowired
	private OrderService orderService;
	@Autowired
	private OrderRefundComService orderRefundComService;
	
	@Resource(name ="orderRefundHotelProcesserService")
	private IOrderRefundProcesserService orderRefundHotelProcesserService;
	
	@Override
	public void startProcesserByRefund(Long orderId,
			Map<String, Object> params) {
		OrdOrder ordOrder = orderService.queryOrdorderByOrderId(orderId);
        if (ordOrder == null || !isOrderRefund(ordOrder)) {
        	LOG.info("orderId=" +orderId +",ordOrder is not null or isOrderRefund == false");
        	return ;
        }
		LOG.info("orderId=" +orderId +",params=" +params);
        newInstall(ordOrder).startProcesserByRefund(ordOrder, params);
	}

	@Override
	public void completeTaskBySupplierConfirm(OrdOrder order, String supplierKey) {
		//目前只有酒店调用
		orderRefundHotelProcesserService.completeTaskBySupplierConfirm(order, supplierKey);
	}


	@Override
	public void completeTaskByOnlineRefundAudit(OrdOrder order,
			ComAudit comAudit) {
		newInstall(order).completeTaskByOnlineRefundAudit(comAudit);
	}
	
	@Override
	public boolean isStartProcessByRefund(OrdOrder order, String operateName) {
		//目前只有酒店调用
		return orderRefundHotelProcesserService.isStartProcessByRefund(order, operateName);
	}

	@Override
	public void updateOrderStatusToOrderRefund(OrdOrder order, Map<String, Object> params, Date applyDate, String operateName) {
		//目前只有酒店调用
		orderRefundHotelProcesserService.updateOrderStatusToOrderRefund(order, params, applyDate, operateName);
	}

	/**
	 * newInstall
	 */
	private IOrderRefundProcesserService newInstall(OrdOrder ordOrder){
		return (IOrderRefundProcesserService)orderRefundComService.newInstall(ordOrder,ORDER_REFUND_SERVICE_TYPE_KEY.PROCESS);
	}
	/**
	 * 是否在线退款订单
	 * @param ordOrder
	 * @return
	 */
	private static boolean isOrderRefund(OrdOrder ordOrder){
		//酒店、自由行酒景
		if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(ordOrder.getCategoryId())
				|| BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(ordOrder.getSubCategoryId())){
			return true;
		}
		return false;
	}
}
