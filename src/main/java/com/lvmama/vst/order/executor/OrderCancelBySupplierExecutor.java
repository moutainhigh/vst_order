/**
 * 
 */
package com.lvmama.vst.order.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComJobConfig;
import com.lvmama.vst.back.pub.service.ComJobConfigExecutor;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleTT;
import com.lvmama.vst.order.service.IOrderLocalService;

/**
 * 避免工作流当中的废单问题，供应商下单当中应产生的直接废单等待在该位置来废单操作
 * @author lancey
 *
 */
@Component("orderCancelBySupplierExecutor")
public class OrderCancelBySupplierExecutor implements ComJobConfigExecutor{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderCancelBySupplierExecutor.class);
	
	@Autowired
	private IOrderLocalService orderLocalService;

	@Override
	public ResultHandleTT<ComJobConfig> execute(ComJobConfig comJobConfig) {
		ResultHandleTT<ComJobConfig> result = new ResultHandleTT<ComJobConfig>();
		ResultHandle handle = orderLocalService.cancelOrder(comJobConfig.getObjectId(), OrderEnum.ORDER_CANCEL_CODE.SUPPLIER_CREATE_FAIL.name(), "供应商下单失败直接废单", "SYSTEM", null);
		if (handle.isSuccess()) {
			LOGGER.info("SupplierOrderProcesser.process: orderService.cancelOrder success,OrderID=" + comJobConfig.getObjectId() + ", 废单成功。" );
			
		} else {
			LOGGER.error("SupplierOrderProcesser.process: orderService.cancelOrder fail,OrderID=" + comJobConfig.getObjectId() + ",废单失败，msg=" + handle.getMsg());
		}
		result.setReturnContent(comJobConfig);
		result.setCode(DElETE_COMJOBCONFIG);
		return result;
	}

}
