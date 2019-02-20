package com.lvmama.vst.order.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComJobConfig;
import com.lvmama.vst.back.pub.service.ComJobConfigExecutor;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleTT;
import com.lvmama.vst.order.service.ISupplierOrderOperator;
import com.lvmama.vst.order.vo.OrderSupplierOperateResult;

/**
 * 创建供应商订单处理器
 * 
 * @author sunjian
 *
 */
@Component("supplierOrderCreateExecutor")
public class SupplierOrderCreateExecutor implements ComJobConfigExecutor {
	private static final Log LOG = LogFactory.getLog(SupplierOrderCreateExecutor.class);
	
	@Autowired
	private ISupplierOrderOperator supplierOrderOperator;
	
	@Autowired
	private OrderService orderService;

	@Override
	public ResultHandleTT<ComJobConfig> execute(ComJobConfig comJobConfig) {
		ResultHandleTT<ComJobConfig> resultHandle=new ResultHandleTT<ComJobConfig>();
		int operateCode = ComJobConfigExecutor.DElETE_COMJOBCONFIG;
		ComJobConfig updateComJobConfig=null;
		if (comJobConfig != null) {
			OrderSupplierOperateResult result = supplierOrderOperator.createSupplierOrder(comJobConfig.getObjectId(), true);
			if (!result.isSuccess()) {
				LOG.info("SupplierOrderCreateExecutor.execute: supplierOrderOperator.createSupplierOrder fail,OrderID=" + comJobConfig.getObjectId() + ",msg=" +result.getErrMsg());
				
				if (result.isRetry()) {
					if (comJobConfig.getRetryCount() > 0) {
						updateComJobConfig = new ComJobConfig();
						updateComJobConfig.setComJobConfigId(comJobConfig.getComJobConfigId());
						updateComJobConfig.setRetryCount(comJobConfig.getRetryCount() - 1);
						updateComJobConfig.setPlanTime(DateUtil.getDateAfterMinutes(5));
						
						operateCode = ComJobConfigExecutor.UPDATE_COMJOBCONFIG;
					} else {
						//废单
						try {
							ResultHandle handle = orderService.cancelOrder(comJobConfig.getObjectId(), OrderEnum.ORDER_CANCEL_CODE.SUPPLIER_CREATE_FAIL.name(), result.getErrMsg() + "（已重试）", "SYSTEM", null);
							if (handle.isSuccess()) {
								LOG.info("SupplierOrderCreateExecutor.execute: orderService.cancelOrder success,RetryCount=" + comJobConfig.getRetryCount() + ",OrderID=" + comJobConfig.getObjectId() + ", 废单成功。" );
								
							} else {
								LOG.error("SupplierOrderCreateExecutor.execute: orderService.cancelOrder fail,RetryCount=" + comJobConfig.getRetryCount() + ",OrderID=" + comJobConfig.getObjectId() + ",废单失败，msg=" + handle.getMsg());
							}
						} catch (Exception ex) {
							LOG.error(ExceptionFormatUtil.getTrace(ex));
							LOG.info("SupplierOrderCreateExecutor.execute: Exception,RetryCount=" + comJobConfig.getRetryCount() + ",OrderID=" + comJobConfig.getObjectId() + ", 废单发生异常：" + ex.getMessage());
						}
					}
				} else {
					LOG.info("SupplierOrderCreateExecutor.execute: supplierOrderOperator.createSupplierOrder fail,isRetry=" + result.isRetry() + ",OrderID=" + comJobConfig.getObjectId() + ",msg=" + result.getErrMsg());
					
					//废单
					try {
						ResultHandle handle = orderService.cancelOrder(comJobConfig.getObjectId(), OrderEnum.ORDER_CANCEL_CODE.SUPPLIER_CREATE_FAIL.name(), result.getErrMsg().substring(0, 180), "SYSTEM", null);
						if (handle.isSuccess()) {
							LOG.info("SupplierOrderCreateExecutor.execute: orderService.cancelOrder success,OrderID=" + comJobConfig.getObjectId() + ", 废单成功。" );
							
						} else {
							LOG.error("SupplierOrderCreateExecutor.execute: orderService.cancelOrder fail,OrderID=" + comJobConfig.getObjectId() + ",废单失败，msg=" + handle.getMsg());
						}
					} catch (Exception ex) {
						LOG.error(ExceptionFormatUtil.getTrace(ex));
						LOG.info("SupplierOrderCreateExecutor.execute: Exception,OrderID=" + comJobConfig.getObjectId() + ", 废单发生异常：" + ex.getMessage());
					}
				}
			} else {
				LOG.debug("SupplierOrderCreateExecutor.execute: supplierOrderOperator.createSupplierOrder success,OrderID=" + comJobConfig.getObjectId());
			}
		}
		
		if (operateCode == ComJobConfigExecutor.DElETE_COMJOBCONFIG) {
			updateComJobConfig = comJobConfig;
		}
		resultHandle.setReturnContent(updateComJobConfig);
		resultHandle.setCode(operateCode);
		return resultHandle;
	}
}
