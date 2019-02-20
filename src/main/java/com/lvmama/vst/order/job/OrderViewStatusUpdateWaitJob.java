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
 * 目的地BU修改订单展示状态为入住/行程中
 * @author ryan
 *
 */
public class OrderViewStatusUpdateWaitJob implements Runnable{

	private static final Log LOG = LogFactory.getLog(OrderViewStatusUpdateWaitJob.class);
	@Autowired
	private OrdOrderDao ordOrderDao;
	
	@Resource
	private IVstOrderRouteService vstOrderRouteService;
	
	@Override
	public void run() {
		LOG.info(Constant.getInstance().isJobRunnable());
		//modify by zhujingfeng 2017-09-26
		if((!vstOrderRouteService.isJobRouteToNewSys()) && Constant.getInstance().isJobRunnable()){
			Date currentDate = new Date();			
			String currentDateStr = DateUtil.formatSimpleDate(currentDate);
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("currentTime", currentDateStr);
			List<OrdOrder> orderList = ordOrderDao.findDestBuWaitViewStatus(params);			
			for (OrdOrder ordOrder : orderList) {
				LOG.info("update orderViewStatus job orderId is "+ordOrder.getOrderId());
				if(OrderUtils.isHotelByCategoryId(ordOrder.getCategoryId())){					
					ordOrder.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.IN_HOTEL.getCode());
				}else{
					ordOrder.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.TRIPING.getCode());										
				}			
			}
			ordOrderDao.batchUpdateViewStatusByList(orderList);
		}	
	}

}
