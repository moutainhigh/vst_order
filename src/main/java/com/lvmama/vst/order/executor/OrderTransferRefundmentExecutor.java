/**
 * 
 */
package com.lvmama.vst.order.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.pub.po.ComJobConfig;
import com.lvmama.vst.back.pub.service.ComJobConfigExecutor;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.ResultHandleTT;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.pet.adapter.OrderRefundmentServiceAdapter;

/**
 * @author lancey
 *
 */
@Component("orderTransferRefundmentExecutor")
public class OrderTransferRefundmentExecutor implements ComJobConfigExecutor{
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
	private OrderRefundmentServiceAdapter orderRefundmentServiceAdapter;
	
	
	private static final Logger logger = LoggerFactory.getLogger(OrderTransferRefundmentExecutor.class);

	@Override
	public ResultHandleTT<ComJobConfig> execute(ComJobConfig comJobConfig) {
		ResultHandleTT<ComJobConfig> result = new ResultHandleTT<ComJobConfig>();
		OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(comJobConfig.getObjectId());
		boolean flag = false;
		
		int operateCode = ComJobConfigExecutor.UPDATE_COMJOBCONFIG;
		
		if(order!=null&&order.hasNeedPrepaid()){
			long refundmentAmount = order.getActualAmount()-order.getOughtAmount();
			if(order.hasPayed() && refundmentAmount>0){
				try {
					flag = orderRefundmentServiceAdapter.autoRefundment(false, order.getOrderId(), refundmentAmount, "SYSTEM", "订单资金转移超出部分自动退款");
					logger.info("order id:{},refundment amount:{},result:{}",new Object[]{order.getOrderId(),refundmentAmount,flag});
					} catch(Exception e) {
						logger.error(e.getMessage());
					}
				}
		}
		
		if (flag== false) {
			comJobConfig.setPlanTime(DateUtil.getDateAfterMinutes(5));
            operateCode = ComJobConfigExecutor.UPDATE_COMJOBCONFIG;
        } else {
        	operateCode = ComJobConfigExecutor.DElETE_COMJOBCONFIG;
        }
		
		result.setReturnContent(comJobConfig);
		result.setCode(operateCode);
		return result;
	}

}
