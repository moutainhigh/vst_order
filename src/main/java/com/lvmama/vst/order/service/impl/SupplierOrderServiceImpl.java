package com.lvmama.vst.order.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.order.processer.SupplierOrderItemProcesser;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.ISupplierOrderService;

/**
 * 模拟支付后调用jms接口完成下单
 * @author wuqing
 *
 */
@Component("supplierOrderServiceRemote")
public class SupplierOrderServiceImpl implements ISupplierOrderService<OrdOrder>{

	@Autowired
	private SupplierOrderItemProcesser supplierOrderItemProcesser;
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Override
	public void handle(Message message, OrdOrder obj) {
		supplierOrderItemProcesser.handle(message, obj);
	}

	@Override
	public void updatePaymentStatus(Long orderId, Long amount) {
		orderUpdateService.addOrderActualAmount(orderId, amount);
		OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
		
		if(order.hasFullPayment()){
			order.setPaymentStatus(OrderEnum.PAYMENT_STATUS.PAYED.name());
		}else if(order.getActualAmount()>0){
			order.setPaymentStatus(OrderEnum.PAYMENT_STATUS.PART_PAY.name());
		}
		order.setPaymentTime(new Date());
		
		orderUpdateService.updateOrderAndChangeOrderItemPayment(order);
	}

}
