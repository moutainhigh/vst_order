package com.lvmama.vst.order.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.pub.po.ComJobConfig;
import com.lvmama.vst.back.pub.service.ComJobConfigExecutor;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleTT;
import com.lvmama.vst.order.service.ISupplierOrderOperator;
import com.lvmama.vst.order.vo.OrderSupplierOperateResult;

/**
 * 
 * @author sunjian
 *
 */
@Component("supplierOrderCancelExecutor")
public class SupplierOrderCancelExecutor implements ComJobConfigExecutor {
	private static final Log LOG = LogFactory.getLog(SupplierOrderCancelExecutor.class);
	
	@Autowired
	private ISupplierOrderOperator supplierOrderOperator;
	
	@Override
	public ResultHandleTT<ComJobConfig> execute(ComJobConfig comJobConfig) {
		ResultHandleTT<ComJobConfig> resultHandle = new ResultHandleTT<ComJobConfig>();
		ComJobConfig updateComJobConfig=null;
		int operateCode = ComJobConfigExecutor.DElETE_COMJOBCONFIG;
		if (comJobConfig != null) {
			OrderSupplierOperateResult result = supplierOrderOperator.cancelSupplierOrder(comJobConfig.getObjectId());
			if (!result.isSuccess()) {
				LOG.error("SupplierOrderCancelExecutor.execute: createSupplierOrder fail, OrderID=" + comJobConfig.getObjectId() + ",msg:" + result.getErrMsg());
				
				if (result.isRetry()) {
					LOG.info("SupplierOrderCancelExecutor.execute: OrderID=" + comJobConfig.getObjectId() + ", cancelSupplierOrder fail,RetryCount=" + comJobConfig.getRetryCount());
					
					if (comJobConfig.getRetryCount() > 0) {
						updateComJobConfig = new ComJobConfig();
						updateComJobConfig.setComJobConfigId(comJobConfig.getComJobConfigId());
						updateComJobConfig.setRetryCount(comJobConfig.getRetryCount() - 1);
						updateComJobConfig.setPlanTime(DateUtil.getDateAfterMinutes(5));
						
						operateCode = ComJobConfigExecutor.UPDATE_COMJOBCONFIG;
					} else {
						LOG.info("SupplierOrderCancelExecutor.execute: RetryCount=" + comJobConfig.getRetryCount() + ",OrderID=" + comJobConfig.getObjectId() + ",cancelSupplierOrder fail,msg=" + result.getErrMsg());
						//因为创建第三方订单不成功的订单，需要创建订单取消活动
						ResultHandle handle = supplierOrderOperator.createCancelAuditForSupplierCreateFail(comJobConfig.getObjectId());
						LOG.info("SupplierOrderCancelExecutor.execute: supplierOrderOperator.createCancelAuditForSupplierCreateFail,isSuccess=" + handle.isSuccess() + ",msg=" + handle.getMsg());
					}
				} else {
					LOG.info("SupplierOrderCancelExecutor.execute: OrderID=" + comJobConfig.getObjectId() + ",cancelSupplierOrder fail,msg=" + result.getErrMsg());
					//因为创建第三方订单不成功的订单，需要创建订单取消活动
					ResultHandle handle = supplierOrderOperator.createCancelAuditForSupplierCreateFail(comJobConfig.getObjectId());
					LOG.info("SupplierOrderCancelExecutor.execute: supplierOrderOperator.createCancelAuditForSupplierCreateFail,isSuccess=" + handle.isSuccess() + ",msg=" + handle.getMsg());
				}
			} else {
				LOG.info("SupplierOrderCancelExecutor.execute: OrderID=" + comJobConfig.getObjectId() + "供应商方取消成功");
			}
		}
		
		if (operateCode == ComJobConfigExecutor.DElETE_COMJOBCONFIG) {
			updateComJobConfig = comJobConfig;
		}
		resultHandle.setCode(operateCode);
		resultHandle.setReturnContent(updateComJobConfig);
		return resultHandle;
	}
}
