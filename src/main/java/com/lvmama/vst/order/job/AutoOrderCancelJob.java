/**
 * 
 */
package com.lvmama.vst.order.job;

import java.util.Date;
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
 * 自动废单
 * 针对已经资源审核，并且过了支付最晚时间的订单做废单处理
 * @author lancey
 *
 */
@Service
public class AutoOrderCancelJob implements Runnable {
	private static final Log LOG = LogFactory.getLog(AutoOrderCancelJob.class);
	
	//订单取消原因
	public static String ORDER_CANCEL_REASON = "超过支付等待时间自动废单";
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
		LOG.info(Constant.getInstance().isJobRunnable());
		if(Constant.getInstance().isJobRunnable()) {
			
			LOG.info("AutoOrderCancelJob start");
			Date currDate = new Date();
			//获取订单状态为NORMAL状态且支付等待时间小于当前时间的订单ID
			List<Long> orderIdList = ordOrderUpdateService.getPaymentTimeoutOrderIds(currDate);
			// 需要增加 路由大开关判断 &&　2017-12-27 add by zhujingfeng
			boolean isJobRouteToNewSys = vstOrderRouteService.isJobRouteToNewSys();
			LOG.info("isJobRouteToNewSys="+isJobRouteToNewSys);
			
			if (orderIdList != null) {
				LOG.info("Auto Cancel Order List Size:"+orderIdList.size());
				for (Long orderId : orderIdList) {
					LOG.info("process order, orderId=" + orderId);
					if(orderId == null)
						continue;
					OrdOrder order = ordOrderUpdateService.queryOrdOrderByOrderId(orderId);
					try {
						// 需要增加 路由大开关判断 &&　2017-12-27 add by zhujingfeng
						// 订单二期时加上判断品类是否是酒店套餐、门票剩余6个品类 zhangbin 2018-11-16
						if(isJobRouteToNewSys && (CategoryUtils.isHotelOrTicket(order.getCategoryId())
								|| CategoryUtils.isOrd2Category(order.getCategoryId()))){
							LOG.info("Auto Cancel Order not execute,orderId="+orderId+",categoryId="+order.getCategoryId());
							continue;
						}
						
						if(StringUtils.equals(order.getOrderSubType(), OrderEnum.ORDER_STAMP.STAMP.name())) {
						    LOG.info("------------------3------------------");
	                    	String url = Constant.getInstance().getPreSaleBaseUrl()+ "/customer/stamp/order/cancel";
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("orderId", order.getOrderId());
							map.put("cancelCode", OrderEnum.ORDER_CANCEL_CODE.TIME_OUT_WAIT.name());
							map.put("operatorId", AutoOrderCancelJob.ORDER_CANCEL_OPERATOR_ID);
							map.put("reason", AutoOrderCancelJob.ORDER_CANCEL_REASON);
							map.put("memo", null);
							LOG.info("券订单取消接口-------------请求参数:"+map.toString()+"url:"+url);
//							JSONObject json = new JSONObject();
//							json.putAll(map);
							RestClient.getClient().put(url, map);
							LOG.info("券订单取消接口-------------调用结束。orderId="+order.getOrderId());
						} else {
							
							//执行取消操作
							orderLocalService.cancelOrder(orderId, OrderEnum.ORDER_CANCEL_CODE.TIME_OUT_WAIT.name(), AutoOrderCancelJob.ORDER_CANCEL_REASON, AutoOrderCancelJob.ORDER_CANCEL_OPERATOR_ID, null);
						}
					} catch (Exception e) {
						LOG.error(ExceptionFormatUtil.getTrace(e));
						LOG.error("method run :canceling order(id=" + orderId + ") fail." + e.getMessage());
					}
				}
				
				LOG.info("开始记录JOB执行的订单号！");
				if(CollectionUtils.isNotEmpty(orderIdList)) {
					ordOrderUpdateService.markCancelTimes(orderIdList);
				}
				LOG.info("结束记录JOB执行的订单号！");
			}
			LOG.info("AutoOrderCancelJob end");
		}
		
	}
	

}
