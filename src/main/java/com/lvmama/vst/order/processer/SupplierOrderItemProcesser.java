/**
 * 
 */
package com.lvmama.vst.order.processer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.order.service.ISupplierOrderOperator;
import com.lvmama.vst.supp.client.service.SupplierOrderService;

/**
 * @author lancey
 *
 */
@Component
public class SupplierOrderItemProcesser implements IWorkflowProcesserT<OrdOrder>{

	private static final Log LOG = LogFactory.getLog(SupplierOrderItemProcesser.class);
	
	@Resource(name="supplierOrderService")
	private SupplierOrderService supplierOrderService;
	
	@Autowired
	private ISupplierOrderOperator supplierOrderOperator;
	
	@Override
	public void handle(Message message, OrdOrder obj) {
		//OrdOrderItem orderItem = obj.getOrderItemByOrderItemId(message.getObjectId());
		//更改子订单列表，只保留当前操作的子项
		//obj.setOrderItemList(Arrays.asList(orderItem));
		Set<Long> orderItemIds = new HashSet<Long>();
		orderItemIds.add(message.getObjectId());
		if(MessageUtils.isOrderInfoPassMsg(message)){
			LOG.info("SupplierOrderProcesser.process: OrderInfoPassMsg,orderItemId=" + message.getObjectId());
			supplierOrderOperator.createSupplierOrder(obj.getOrderId(), orderItemIds);
		}else if(MessageUtils.isOrderPaymentMsg(message)){
			LOG.info("SupplierOrderProcesser.process: OrderPaymentMsg orderItemId="+message.getObjectId());
			supplierOrderOperator.createSupplierOrder(obj.getOrderId(), orderItemIds);
		}else if(MessageUtils.isOrderCancelMsg(message)) {
			LOG.info("SupplierOrderProcesser.process: OrderCancelMsg orderItemId="+message.getObjectId());
			supplierOrderOperator.cancelSupplierOrder(obj.getOrderId());
		}
	}

}
