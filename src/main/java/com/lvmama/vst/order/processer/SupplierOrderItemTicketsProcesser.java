/**
 * 
 */
package com.lvmama.vst.order.processer;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.order.service.ISupplierOrderOperator;

/**
 * 门票供应商子订单创建处理类
 * @author yangzhenzhong
 * Created at 2015/9/15
 */
@Component
public class SupplierOrderItemTicketsProcesser implements IWorkflowProcesserT<Set<Long>>{

	private static final Log LOG = LogFactory.getLog(SupplierOrderItemTicketsProcesser.class);
		
	@Autowired
	private ISupplierOrderOperator supplierOrderOperator;
	
	@Override
	public void handle(Message message, Set<Long> orderItemIdList) {

		if(MessageUtils.isOrderInfoPassMsg(message)){
			
			LOG.info("SupplierOrderItemTicketsProcesser.process: OrderInfoPassMsg,orderItemId=" + message.getObjectId());
			supplierOrderOperator.createSupplierOrder(message.getObjectId(), orderItemIdList);
			
		}
	}

}
