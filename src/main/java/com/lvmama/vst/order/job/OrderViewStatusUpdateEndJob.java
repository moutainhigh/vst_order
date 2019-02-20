package com.lvmama.vst.order.job;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.route.IVstOrderRouteService;
import com.lvmama.vst.order.utils.OrderUtils;

/**
 * 目的地BU修改订单展示状态为离店or完成
 * @author ryan
 *
 */
public class OrderViewStatusUpdateEndJob implements Runnable{
	@Autowired
	private OrdOrderDao ordOrderDao;
	
	@Resource
	private IVstOrderRouteService vstOrderRouteService;

	private static final Log LOG = LogFactory.getLog(OrderViewStatusUpdateEndJob.class);
	@Override
	public void run() {
		LOG.info(Constant.getInstance().isJobRunnable());
		//modify by zhujingfeng 2017-09-26
		if((!vstOrderRouteService.isJobRouteToNewSys()) && Constant.getInstance().isJobRunnable()){
			Date currentDate = new Date();				
			Date endTime = DateUtil.addDays(currentDate, -1);
			String endTimeStr = DateUtil.formatSimpleDate(endTime);				
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("endTime", endTimeStr);
			List<OrdOrder> orderList = ordOrderDao.findDestBuOrderUpdateViewStatus(params);
			LOG.info("OrderViewStatusUpdateEndJob start endTime is"+endTimeStr+" and orderList size is"+orderList.size());
			for (OrdOrder ordOrder : orderList) {
				LOG.info("update orderViewStatus job orderId is "+ordOrder.getOrderId());
				if(OrderUtils.isHotelByCategoryId(ordOrder.getCategoryId())){
					ordOrder.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.LEAVE_HOTEL.getCode());	
				}else{					
					ordOrder.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.COMPLETE.getCode());	
				}										
			}			
			//批量修改订单状态
			ordOrderDao.batchUpdateViewStatusByList(orderList);
			
		}
	}

}
