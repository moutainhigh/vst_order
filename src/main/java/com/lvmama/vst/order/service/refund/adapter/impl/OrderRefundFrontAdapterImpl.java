package com.lvmama.vst.order.service.refund.adapter.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.comm.utils.StringUtil;
import com.lvmama.comm.vo.Constant;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.order.service.refund.IOrderRefundFrontService;
import com.lvmama.vst.order.service.refund.OrderRefundComService;
import com.lvmama.vst.order.service.refund.OrderRefundComService.ORDER_REFUND_SERVICE_TYPE_KEY;
import com.lvmama.vst.order.service.refund.adapter.OrderRefundFrontAdapter;
import com.lvmama.vst.pet.adapter.refund.vo.OrderRefundDetailVO;
/**
 * 前台文案漏出服务
 * @version 1.0
 */
@Service("orderRefundFrontAdapter")
public class OrderRefundFrontAdapterImpl implements OrderRefundFrontAdapter{
	private static final Log LOG = LogFactory.getLog(OrderRefundFrontAdapterImpl.class);
	@Autowired
	private OrderService orderService;
	@Autowired
	private OrderRefundComService orderRefundComService;

	public String getRefundStatusByOrderId(Long orderId, String systemType, String orderRefundStatus) {
		//1、--------------入参校验
        if (orderId == null || StringUtil.isEmptyString(systemType)) {
        	LOG.info("orderId=" +orderId +",ordOrder is not null or systemType is empty");
            return Constant.VST_ORDER_REFUND_APPLY_STATUS.CANNOT_REFUND.getCode();
        }
        
        OrdOrder order = orderService.queryOrdorderByOrderId(orderId);
        LOG.info("destBU online refund getRefundStatusByOrderId, orderId:"+order.getOrderId()+" subCategoryId:"+order.getSubCategoryId());
        if(order != null && order.getOrderItemList() != null && order.getOrderItemList().size() > 0){
        	return newInstall(order).getOrderRefundApplyStatus(order, orderRefundStatus);
        }else{
        	return Constant.VST_ORDER_REFUND_APPLY_STATUS.CANNOT_REFUND.getCode();
        }
	}
	
	/**
	 * newInstall
	 */
	private IOrderRefundFrontService newInstall(OrdOrder ordOrder){
		return (IOrderRefundFrontService)orderRefundComService.newInstall(ordOrder,ORDER_REFUND_SERVICE_TYPE_KEY.FRONT);
	}

	@Override
	public void checkRefundOnlineByCommit(Long orderId)
			throws IllegalArgumentException {
		OrdOrder ordOrder =orderService.querySimpleOrder(orderId);
		if (ordOrder == null) {
        	LOG.info("orderId=" +orderId +",ordOrder is not null");
        	 throw new IllegalArgumentException("该订单不存在");
        }
		newInstall(ordOrder).checkRefundOnlineByCommit(ordOrder);
		
	}

	@Override
	public List<OrderRefundDetailVO> getOrderRefundDetailVO(long orderId) {
		OrdOrder ordOrder =orderService.querySimpleOrder(orderId);
		if (ordOrder == null) {
        	LOG.info("orderId=" +orderId +",ordOrder is not null");
        	 throw new IllegalArgumentException("该订单不存在");
        }
		return newInstall(ordOrder).getOrderRefundDetailVO(ordOrder);
	}
}
