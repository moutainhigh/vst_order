package com.lvmama.vst.order.job;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrderItemPassCodeSMS;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.IOrdOrderItemPassCodeSMSService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderUpdateService;

/**
 * 每天10点--12点，每二十分钟执行一次。检测需要提前申码的订单，未在订单创建第二天10点前资源审核通过的情况，并做废单处理
 * Created at 2015/9/10
 * @author yangzhenzhong
 */
@Service
public class AutoCancelOrderJob4OrdOrderItemPassCode implements Runnable {

	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
	private IOrdOrderItemPassCodeSMSService ordOrderItemPassCodeSMSLocalService; 
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	//订单取消原因
	public static String ORDER_CANCEL_REASON = "规定时间内没有完成资源审核";
	//订单取消操作人ID
	public static String ORDER_CANCEL_OPERATOR_ID = "SYSTEM";
	
	private static final Log LOG = LogFactory.getLog(AutoCancelOrderJob4OrdOrderItemPassCode.class);
	
	@Override
	public void run() {
		
		LOG.info(Constant.getInstance().isJobRunnable());

		if(Constant.getInstance().isJobRunnable()) {
			
			LOG.info("AutoCancelOrderJob4OrdOrderItemPassCode start");
			
			//查询创建时间是昨天，且未处理过的数据
			List<OrdOrderItemPassCodeSMS> ordOrderItemPassCodeSMSList = ordOrderItemPassCodeSMSLocalService.queryYesterdayByStatus("Y");
			
			List<Long> orderIdList = new ArrayList<Long>();
			
			
			//如果数据存在
			if(ordOrderItemPassCodeSMSList!=null && ordOrderItemPassCodeSMSList.size()>0){
				for(OrdOrderItemPassCodeSMS ordOrderItemPassCodeSMS :ordOrderItemPassCodeSMSList){
					
					//执行取消订单操作
					try {
						LOG.info("process order, orderId=" + ordOrderItemPassCodeSMS.getOrderId());
						
						if(!isExist(orderIdList,ordOrderItemPassCodeSMS.getOrderId())){
							orderLocalService.cancelOrder(ordOrderItemPassCodeSMS.getOrderId(), OrderEnum.ORDER_CANCEL_CODE.SUPPLIER_CREATE_FAIL.name(), 
									AutoCancelOrderJob4OrdOrderItemPassCode.ORDER_CANCEL_REASON, AutoCancelOrderJob4OrdOrderItemPassCode.ORDER_CANCEL_OPERATOR_ID, null);
							
							orderIdList.add(ordOrderItemPassCodeSMS.getOrderId());
						}
						ordOrderItemPassCodeSMSLocalService.updateStatus(ordOrderItemPassCodeSMS.getId());	
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						LOG.error("method run :canceling order(id=" + ordOrderItemPassCodeSMS.getOrderId() + ") fail." + e.getMessage());
					}
				}
			}
			LOG.info("开始记录JOB执行的订单号！");
			if(CollectionUtils.isNotEmpty(orderIdList)) {
				orderUpdateService.markCancelTimes(orderIdList);
			}
			LOG.info("结束记录JOB执行的订单号！");
			LOG.info("AutoCancelOrderJob4OrdOrderItemPassCode end");
		}
	}
	
	private boolean isExist(List<Long> orderIdList, Long orderId){
		
		for(Long id:orderIdList){
			if(id==orderId){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 传入的参数日期是否是昨天
	 * @param createTime
	 * @return
	 */
//	private boolean isYesterday(Date createTime){
//		
//		boolean flag=false;
//		Calendar createDate = Calendar.getInstance();
//		Calendar yesterday = Calendar.getInstance();
//		
//		yesterday.add(Calendar.DATE, -1);
//		createDate.setTime(createTime);
//		
//		if(yesterday.get(Calendar.YEAR)==createDate.get(Calendar.YEAR) 
//				&& yesterday.get(Calendar.DAY_OF_YEAR)== createDate.get(Calendar.DAY_OF_YEAR)){
//			flag=true;
//		}
//		return flag;
//	}	
}