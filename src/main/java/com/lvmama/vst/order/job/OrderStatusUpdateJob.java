package com.lvmama.vst.order.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.order.route.service.IOrder2RouteService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.service.IOrderUpdateService;

/**
 * 主订单信息审核与资源审核状态job补偿
 * @author yangruochen
 *
 */
public class OrderStatusUpdateJob implements Runnable {

	private static final Log LOG = LogFactory.getLog(OrderStatusUpdateJob.class);
	
	@Autowired
	private OrdOrderDao ordOrderDao;
	
	@Autowired
	private OrdOrderItemDao ordOrderItemDao;
	
	@Autowired
	protected IOrderUpdateService orderUpdateService;
	
	@Autowired
	private SuppGoodsTimePriceClientService suppGoodsTimePriceClientRemote;
	
	//公共操作日志业务
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Autowired
	private IOrder2RouteService order2RouteService;
	
	private OrderStatusUpdateJob() {}
	
	/**
	 * JOB是否运行
	 *
	 * @return
	 */
	public boolean isJobRunnable() {
		String jobEnabled = Constant.getInstance().getProperty("orderStatusCompenseJob.enabled");
		if (LOG.isDebugEnabled()) {
			LOG.debug("job is runnable: " + jobEnabled + " => "
					+ ("true".equals(jobEnabled)));
		}
		if (jobEnabled != null) {
			return Boolean.valueOf(jobEnabled);
		} else {
			return true;
		}
	}
	
	
	/*@Autowired
	private IOrderStatusManageService orderStatusManageService;
	
	@Autowired
	private IOrdPrePayServiceAdapter ordPrePayServiceAdapter;
	
	@Resource(name="orderMessageProducer")
	private TopicMessageProducer orderMessageProducer;
	
	@Autowired
	private PetOrderMessageServiceAdapter petOrderMessageService;
	
	@Autowired
	protected IComplexQueryService complexQueryService;*/
	
	@Override
	public void run() {
		
		boolean msgAndJobSwitch= false; //msg and job 总开关
		msgAndJobSwitch= order2RouteService.isMsgAndJobRouteToNewSys();
		if(msgAndJobSwitch){
		    return;
		}
				
		if(isJobRunnable()){

			//要把30天分成6段
			for (int i = 0; i < 6; i++) {
				
			Date today = new Date();
			
			//资源审核补偿，只是补偿，不需要发送消息，工作流完成及扣款申请
			updateOrderResourceStatus(i,today);
			
			//信息审核补偿
			updateOrderInfoStatus(i,today);
			

			}
			
		}
	}
		
	public List<OrdOrder> updateOrderResourceStatus(Integer i, Date today) {
			
		Map<String, Object> params = new HashMap<String, Object>();
		Date startDate = DateUtils.addDays(today, -4-i*5);
		Date endDate = DateUtils.addDays(today, -i*5);
		params.put("resourceStatus", OrderEnum.RESOURCE_STATUS.UNVERIFIED.getCode());
		//params.put("orderStatus", OrderEnum.ORDER_STATUS.NORMAL.getCode());
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		List<OrdOrder> ordOrderListRecord = new ArrayList<OrdOrder>();
		List<OrdOrder> ordOrderList = ordOrderDao.queryResourceStatusOrderList(params);
		for (OrdOrder ordOrder : ordOrderList) {
			//如果子订单资源状态都为满足，则更新主订单资源状态逻辑
			Map<String,Object> paramsOrderItem= new HashMap<String, Object>();
			paramsOrderItem.put("orderId",ordOrder.getOrderId());
			
			int ordItemAllNum=ordOrderItemDao.getTotalCount(paramsOrderItem);
			
			paramsOrderItem.put("resourceStatus", OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
			
			int ordItemResAmpleNum=ordOrderItemDao.getTotalCount(paramsOrderItem);
			if (ordItemAllNum==ordItemResAmpleNum) {
				OrdOrder updateOrder = new OrdOrder();
				updateOrder.setOrderId(ordOrder.getOrderId());
				updateOrder.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
				updateOrder.setResourceAmpleTime(new Date());
				int n=ordOrderDao.updateByPrimaryKeySelective(updateOrder);
				if (n!=1) {
					LOG.info("updateOrderResourceStatus is failed, orderId is:" + ordOrder.getOrderId());
				}else{
					insertOrderLog(ordOrder.getOrderId(), OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCode(), "system","主订单资源审核通过状态定时任务补偿");
					LOG.info("updateOrderResourceStatus is success, orderId is:" + ordOrder.getOrderId());
					ordOrderListRecord.add(ordOrder);
				}
			}
		}
		return ordOrderListRecord;
	}
	
	public List<OrdOrder> updateOrderInfoStatus(Integer i, Date today) {
		Map<String, Object> params = new HashMap<String, Object>();
		Date startDate = DateUtils.addDays(today, -4-i*5);
		Date endDate = DateUtils.addDays(today, -i*5);
		params.put("infoStatus", OrderEnum.INFO_STATUS.UNVERIFIED.getCode());
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		List<OrdOrder> ordOrderListRecord = new ArrayList<OrdOrder>();
		List<OrdOrder> ordOrderList = ordOrderDao.queryInfoStatusOrderList(params);
		for (OrdOrder ordOrder : ordOrderList) {
			//更新主订单信息状态逻辑
			Map<String,Object> paramsOrderItem= new HashMap<String, Object>();
			paramsOrderItem.put("orderId",ordOrder.getOrderId());
			
			int ordItemAllNum=ordOrderItemDao.getTotalCount(paramsOrderItem);
			
			paramsOrderItem.put("infoStatus", OrderEnum.INFO_STATUS.INFOPASS.getCode());
			
			int ordItemInfoPassNum=ordOrderItemDao.getTotalCount(paramsOrderItem);
			
			if (ordItemAllNum==ordItemInfoPassNum) {
				OrdOrder updateOrder = new OrdOrder();
				updateOrder.setOrderId(ordOrder.getOrderId());
				updateOrder.setInfoStatus(OrderEnum.INFO_STATUS.INFOPASS.getCode());
				updateOrder.setInfoPassTime(new Date());
				int n=ordOrderDao.updateByPrimaryKeySelective(updateOrder);
				if (n!=1) {
					LOG.info("OrderStatusManageServiceImpl.updateOrderInfoStatus is failed, orderId is:" + ordOrder.getOrderId());
				}else{
					insertOrderLog(ordOrder.getOrderId(), OrderEnum.AUDIT_TYPE.INFO_AUDIT.getCode(), "system","主订单信息审核通过状态定时任务补偿");
					LOG.info("OrderStatusManageServiceImpl.updateOrderInfoStatus is success, orderId is:" + ordOrder.getOrderId());
					ordOrderListRecord.add(ordOrder);
				}
			}
		}
		return ordOrderListRecord;
	}
	
	/**
	 * @param orderId
	 * @param auditType
	 * @param assignor
	 * @param memo
	 * @param appendMessage
	 * 保存日志
	 */
	private void insertOrderLog(final Long orderId,String auditType,String assignor,String memo){
		this.insertOrderLog(orderId, auditType, assignor, memo, "");
	}
	
	/**
	 * 
	 * 保存日志
	 * 
	 */
	private void insertOrderLog(final Long orderId,String auditType,String assignor,String memo,String appendMessage){
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
				orderId, 
				orderId, 
				assignor, 
				"将编号为["+orderId+"]的订单活动变更["+OrderEnum.AUDIT_TYPE.valueOf(auditType).getCnName()+"通过]"+appendMessage, 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"["+OrderEnum.AUDIT_TYPE.valueOf(auditType).getCnName()+"通过]",
				memo);
	}
	
	/**
	 * @Discribe： 计算门票支付等待时间
	 * 	原因：当日门票的支付时间在资源审核的时候被覆盖，导致等待支付时间错误
	 * @user ZM
	 * @date 2015年5月15日下午4:13:12	
	 * @param order void
	 */
	public void calPaymentWaitTime4Mp(OrdOrder order,List<OrdOrderItem> orderItemList){
		if(ProductCategoryUtil.isTicket(order.getCategoryId())){
			if(order.getWaitPaymentTime()!=null){
			// 门票品类支付等待时间业务变更 20141205 start 
			LOG.info("门票品类支付等待时间业务变更 20141205 orderId = "+order.getOrderId() +"start");
			List<Integer> minutes = new ArrayList<Integer>();
			//是否是门票品类
			boolean isTicketFlag = false; 
			//默认设为通用的支付等待时间
			Date newWaitPaymentTime = order.getWaitPaymentTime();
			LOG.info("处理前支付等待时间是"+ newWaitPaymentTime );
			if(orderItemList!=null){
				for(OrdOrderItem orderItem : orderItemList){
					if(orderItem.hasTicketAperiodic()){
						continue;
					}
					//品类code
					String categoryCode = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
					//门票品类
					LOG.info("品类code是"+ categoryCode);
					// 景区票、其它票、组合套餐
					if(ProductCategoryUtil.isTicket(categoryCode)){
						//取得出游日期对应的时间价格数据
						ResultHandleT<SuppGoodsBaseTimePrice> timePriceResultHandleT = 
								suppGoodsTimePriceClientRemote.getBaseTimePrice(orderItem.getSuppGoodsId(), orderItem.getVisitTime());
						SuppGoodsBaseTimePrice timePrice = timePriceResultHandleT == null ? null : timePriceResultHandleT.getReturnContent();
						//取得出游日期对应的时间价格数据
						if(timePrice != null ) {
							//时间价格表中提前预定时间
							minutes.add(timePrice.getAheadBookTime().intValue());
							LOG.info("时间价格表中提前预定时间是"+ timePrice.getAheadBookTime().intValue());
						}
						isTicketFlag = true;
					}
					LOG.info("门票品类判断结果"+ isTicketFlag);
				}
			}
			if(isTicketFlag){
				LOG.info("门票品类判断结果"+ isTicketFlag);
					//下单时间到出游日的剩余时间
					Long leftTime =(long)(DateUtil.getMinute(order.getCreateTime(),order.getVisitTime()));
					//品类中最大提前预定时间和下单时间之差
					Long waitMinute = leftTime - Collections.max(minutes);
					LOG.info("提前预定时间和下单时间之差是"+ waitMinute );
					//提前预定时间和下单时间之差大于0小于120分钟的时候
					//if(waitMinute < 120 && waitMinute >0){

					 //设置新的支付等待时间
					 newWaitPaymentTime = DateUtils.addMinutes(order.getCreateTime(), waitMinute.intValue());
					 //
					if(newWaitPaymentTime.before(order.getWaitPaymentTime())){
						order.setWaitPaymentTime(newWaitPaymentTime);
					}
					
					LOG.info("处理后门票的支付等待时间是"+ newWaitPaymentTime);
					//}
				}
				LOG.info("处理后的支付等待时间是"+ newWaitPaymentTime);
			 //门票品类支付等待时间业务变更 20141205 end
				LOG.info("门票品类支付等待时间业务变更 20141205 end");
		}
		}
	}
	
	/**
	 * 资源保留时间、最晚无损取消时间、支付等待时间中最小的 
	 * 逻辑修改 2015-01-12当 资源审核时间不为空的情况下,取该时间为支付等待时间
	 * @param orderId
	 * @param lastCancelTime
	 * @return
	 */
	private Date getMinDate(Long orderId, Date lastCancelTime, Date watiPaymentTime){
		//1.获取资源审核时间最小时间
		Date minDate=this.getMinDate(orderId, null);
		//2,在minDate不为空的情况下，返回即当前的值<子订单设置>
		if(minDate != null){
			return minDate;
		}
		//3,在资源审核时间为空，watiPaymentTime不为空则为watiPaymentTime
		if (watiPaymentTime != null && minDate == null ) {
			minDate=watiPaymentTime;
		}else if (watiPaymentTime!=null && minDate!=null ) {
			//4,在资源审核时间与watiPaymentTime都不为空的情况下，取最小值
			if (watiPaymentTime.before(minDate)) {
				minDate=watiPaymentTime;
			}
		}
		return minDate;
	}
	
	/**
	 * 资源保留时间、最晚无损取消时间中最小的
	 * @param orderId
	 * @param lastCancelTime
	 * @return
	 */
	public Date getMinDate(Long orderId, Date lastCancelTime){
		
		Date minDate=null;
		//最小资源保留时间
		Date minRetentionDate=null;
		boolean isFirstDate=true;
		List<OrdOrderItem> orderItemsList = orderUpdateService.queryOrderItemByOrderId(orderId);
		for (int i = 0; i < orderItemsList.size(); i++) {
			
			Map<String,Object> contentMap = orderItemsList.get(i).getContentMap();
			String resourceRetentionTime =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name());
			
			if (!StringUtils.isEmpty(resourceRetentionTime)) {
				Date retentionTime=DateUtil.toDate(resourceRetentionTime, "yyyy-MM-dd HH:mm:ss");
				if (isFirstDate) {
					minRetentionDate=retentionTime;
					isFirstDate=false;
				}else if(retentionTime.before(minRetentionDate)){
					minRetentionDate=retentionTime;
				}
			}
		}
		if (minRetentionDate!=null && lastCancelTime==null) {
			minDate=minRetentionDate;
		}else if (lastCancelTime!=null && minRetentionDate==null) {
			minDate=lastCancelTime;
		}else if (lastCancelTime!=null && minRetentionDate!=null) {
			
			if (lastCancelTime.before(minRetentionDate)) {
				minDate=lastCancelTime;
			}else{
				minDate=minRetentionDate;
			}
		}
		return minDate;
	
	}
}
