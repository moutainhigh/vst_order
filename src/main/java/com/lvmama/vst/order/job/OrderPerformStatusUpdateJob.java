package com.lvmama.vst.order.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.IOrdOrderHotelTimeRateService;
import com.lvmama.vst.order.service.IOrderUpdateService;

	/**
	 * 履行状态更新JOB
	 * @author leiwanli
	 *
	 */
	public class OrderPerformStatusUpdateJob implements Runnable{
		private static final Logger logger = LoggerFactory.getLogger(OrderPerformStatusUpdateJob.class);
		
		//订单取消操作人ID
		public static String ORDER_CANCEL_OPERATOR_ID = "SYSTEM";
		
		@Autowired
		private IOrderUpdateService ordOrderUpdateService;
		
		@Autowired
		private IOrdOrderHotelTimeRateService ordOrderHotelTimeRateService;

		@Override
		public void run() {
			if(Constant.getInstance().isJobRunnable()){
				logger.info("******酒店履行状态JOB开始******");
				Date currDate = new Date();
				//获取订单状态为NORMAL状态且非对接酒店的订单列表
				List<OrdOrderItem> orderItemList = ordOrderUpdateService.queryOrderItems();
				List<Date> days = new ArrayList<Date>();
				if (orderItemList != null && orderItemList.size()>0) {
					for (OrdOrderItem item : orderItemList) {
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("orderItemId", item.getOrderItemId()); 
						List<OrdOrderHotelTimeRate> orderHotelTimeRateList = ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateList(params);
						//最后入住时间
						Date vistTime = item.getVisitTime();
						Date calDate = DateUtils.addDays(vistTime, 8);
						if(orderHotelTimeRateList!= null && orderHotelTimeRateList.size()>0){
							for(OrdOrderHotelTimeRate timeRate : orderHotelTimeRateList){
								days.add(timeRate.getVisitTime());
							}
							//最后入住时间
							vistTime = Collections.max(days);
							days.clear();
						}
						
						if(vistTime!=null){
							calDate = DateUtils.addDays(vistTime, 8);
						}
						 
						try {
							logger.info("******酒店履行状态更新开始******");
							logger.info("******酒店履行状态更新开始******item.getOrderItemId()="+item.getOrderItemId()+
									"******item.getPerformStatus()="+item.getPerformStatus()+
									"**item.getVisitTime()="+item.getVisitTime()+
									"****最后入住时间="+vistTime+
									"最后入住时间超过7天="+calDate +
									"最后入住时间超过7天计算结果="+currDate.after(calDate)+
									"当前时间="+currDate
									);
							
							//订单已过入住时间，且订单状态为“正常”,履行状态是未履行的，且以最后入住时间为准，超过7天，则订单状态改为“已使用”
							if((!"PERFORM".equals(item.getPerformStatus()))&& currDate.after(calDate)){
								OrdOrderItem updateItem = new OrdOrderItem();
								updateItem.setOrderItemId(item.getOrderItemId());
								updateItem.setPerformStatus("PERFORM");
								//执行更新操作
								ordOrderUpdateService.updateOrderItemPerformStatus(updateItem);
								logger.info("******酒店履行状态更新orderItemId=******"+item.getOrderItemId());
							}
							logger.info("******酒店履行状态更新结束******");
						} catch (Exception e) {
							logger.error(ExceptionFormatUtil.getTrace(e));
							logger.error("method run :update performStatus order(id=" + item.getOrderId() + ") fail.");
						}
						
					}
				}
				logger.info("******酒店履行状态JOB结束******");
			}
			
		}

	}
