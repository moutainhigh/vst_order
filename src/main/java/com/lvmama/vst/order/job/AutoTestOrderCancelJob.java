/**
 * 
 */
package com.lvmama.vst.order.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.CategoryUtils;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.route.IVstOrderRouteService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.utils.RestClient;

/**
 * 测试订单自动取消job
 */
@Service
public class AutoTestOrderCancelJob implements Runnable {
	private static final Log LOG = LogFactory.getLog(AutoTestOrderCancelJob.class);
	
	//订单取消原因
	public static String ORDER_CANCEL_REASON = "后测试订单自动取消";
	//订单取消操作人ID
	public static String ORDER_CANCEL_OPERATOR_ID = "SYSTEM";
	
	@Autowired
	private IOrderUpdateService ordOrderUpdateService;
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	@Resource
	private IVstOrderRouteService vstOrderRouteService;
	
	@Override
	public void run() {
		if( Constant.getInstance().isJobRunnable()) {
			String testOrderCancelEnabled = Constant.getInstance().getProperty("job.testOrderCancel.enabled");
			String orderPendingMinute = Constant.getInstance().getProperty("job.order.pending.minute");
			if(testOrderCancelEnabled == null || testOrderCancelEnabled.isEmpty() || testOrderCancelEnabled.equals("true")) {
				LOG.info("AutoTestOrderCancelJob start");
				Map<String, Object> parameters = new HashMap<String, Object>();
				Integer minute = 30;
				if (orderPendingMinute != null && !orderPendingMinute.isEmpty()) {
					try {
						minute = Integer.valueOf(orderPendingMinute.trim());
						if (minute < 10) {
							minute = 10;
						} else if (minute > 50) {
							minute = 50;
						}
					} catch (NumberFormatException e) {
					}
				}
				parameters.put("order_pending_minute", minute);
				List<Long> orderIdList = ordOrderUpdateService.getPendingCancelTestOrderIdList(parameters);
				// 需要增加 路由大开关判断 &&　2017-12-27 add by zhujingfeng
				boolean isJobRouteToNewSys = vstOrderRouteService.isJobRouteToNewSys();
				LOG.info("isJobRouteToNewSys="+isJobRouteToNewSys);
				
				if (orderIdList != null) {
					for (Long orderId : orderIdList) {
						
						LOG.info("auto test cancel order, orderId=" + orderId);
						String cancelReason = minute + "分钟后后测试订单自动取消";
						OrdOrder order = ordOrderUpdateService.queryOrdOrderByOrderId(orderId);
						try {
							// 需要增加 路由大开关判断 &&　2017-12-27 add by zhujingfeng
							if(isJobRouteToNewSys && CategoryUtils.isHotelOrTicket(order.getCategoryId())){
								LOG.info("auto test cancel order not execute,orderId="+orderId+",categoryId="+order.getCategoryId());
								continue;
							}
							
							if(StringUtils.equals(order.getOrderSubType(), OrderEnum.ORDER_STAMP.STAMP.name())) {
							    LOG.info("------------------4------------------");
		                    	String url = Constant.getInstance().getPreSaleBaseUrl()+ "/customer/stamp/order/cancel";
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("orderId", orderId);
								map.put("cancelCode", OrderEnum.ORDER_CANCEL_CODE.SUPER_CANCEL.name());
								map.put("operatorId", AutoOrderCancelJob.ORDER_CANCEL_OPERATOR_ID);
								map.put("reason", cancelReason);
								map.put("memo", null);
								LOG.info("券订单取消接口-------------请求参数:"+map.toString()+"url:"+url);
								RestClient.getClient().put(url, map);
								LOG.info("券订单取消接口-------------调用结束。orderId="+order.getOrderId());
							} else {
								//执行取消操作
								orderLocalService.cancelOrder(orderId, OrderEnum.ORDER_CANCEL_CODE.SUPER_CANCEL.name(), cancelReason, 
										AutoTestOrderCancelJob.ORDER_CANCEL_OPERATOR_ID, null);
							}
						} catch (Exception e) {
							LOG.error(ExceptionFormatUtil.getTrace(e));
							LOG.error("method run :canceling order(id=" + orderId + ") fail." + e.getMessage());
						}
					}
					
					if(CollectionUtils.isNotEmpty(orderIdList)) {
						ordOrderUpdateService.markCancelTimes(orderIdList);
					}
					LOG.info("have successful canceled " + orderIdList.size() + " test orders");
				}
				LOG.info("AutoTestOrderCancelJob end");
			}
		}
		
	}
	

}
