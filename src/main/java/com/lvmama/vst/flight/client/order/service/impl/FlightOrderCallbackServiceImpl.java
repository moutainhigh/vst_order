package com.lvmama.vst.flight.client.order.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.BizSystemConfigureClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.order.po.OrdFlightTicketInfo;
import com.lvmama.vst.back.order.po.OrdFlightTicketStatus;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.Constant.BU_NAME;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.flight.client.order.service.FlightOrderCallbackService;
import com.lvmama.vst.flight.client.order.vo.BookTicketResultVO;
import com.lvmama.vst.flight.client.order.vo.FlightLockSeatCallbackInfoVO;
import com.lvmama.vst.flight.client.order.vo.FlightTicketCallbackInfoVO;
import com.lvmama.vst.flight.client.order.vo.FlightTicketDetailInfoVO;
import com.lvmama.vst.flight.client.order.vo.LockSeatResultVO;
import com.lvmama.vst.order.service.IComMessageService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdFlightTicketInfoService;
import com.lvmama.vst.order.service.IOrdFlightTicketStatusService;
import com.lvmama.vst.order.service.IOrdItemPersonRelationService;
import com.lvmama.vst.order.service.IOrderResponsibleService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.pet.adapter.IPayPaymentServiceAdapter;

@Component("flightOrderCallbackServiceRemote")
public class FlightOrderCallbackServiceImpl implements
		FlightOrderCallbackService {

	private static final Log LOG = LogFactory.getLog(FlightOrderCallbackServiceImpl.class);
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrdItemPersonRelationService ordItemPersonRelationService;
	
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Autowired
	private IPayPaymentServiceAdapter payPaymentServiceAdapter;
	
	@Autowired
	private IOrdFlightTicketStatusService ordFlightTicketStatusService;
	
	@Autowired
	private IOrdFlightTicketInfoService ordFlightTicketInfoService;
	
	@Autowired
	private IComMessageService comMessageService;
	
	@Autowired
	private IOrderResponsibleService orderResponsibleService;
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private BizSystemConfigureClientService bizSystemConfigureClientRemote;
	
	@Override
	public ResultHandle flightTicketCallback(
			FlightTicketCallbackInfoVO flightTicketCallbackInfoVO) throws Exception {
		ResultHandle resultHandle = new ResultHandle();
		if(flightTicketCallbackInfoVO == null) {
			LOG.error("机票出票回调失败：flightTicketCallbackInfo is null");
			resultHandle.setMsg("flightTicketCallbackInfo is null");
			return resultHandle;
		}
		
		LOG.info("机票出票回调，orderId:" + flightTicketCallbackInfoVO.getOrderId()
				+ ", orderItemId:"
				+ flightTicketCallbackInfoVO.getOrderItemId());
		
		if(flightTicketCallbackInfoVO.getOrderId() == null || flightTicketCallbackInfoVO.getOrderItemId() == null) {
			LOG.error("机票出票回调失败：orderId/orderItemId is null");
			resultHandle.setMsg("orderId/orderItemId is null");
			return resultHandle;
		}
		
		//出票成功或者正在出票或者为空则为true,否则为false 
		boolean isTicketSuccessful = true;
		//是否为正在出票
		boolean isTicketProcessing = false;
		
		String ticketStatusLog = null;
		
		List<FlightTicketDetailInfoVO> flightTicketDetailInfos = flightTicketCallbackInfoVO.getFlightTicketDetailInfos();
		StringBuffer failPNameSB = new StringBuffer();
		StringBuffer successPNameSB = new StringBuffer();
		StringBuffer timeOutPNameSB = new StringBuffer();
		StringBuffer processingPNameSB = new StringBuffer();
		if(CollectionUtils.isEmpty(flightTicketDetailInfos)) {
			isTicketSuccessful = false;
		} else {
			for(FlightTicketDetailInfoVO flightTicketDetailInfo : flightTicketDetailInfos) {
				//其中一张出票失败，则认为出票失败
				if(BookTicketResultVO.TICKET_FAIL.equals(flightTicketDetailInfo.getStatus())) {
					isTicketSuccessful = false;
					if(failPNameSB.length() > 0) {
						failPNameSB.append(",");
					} 
					failPNameSB.append(flightTicketDetailInfo.getPassengerName());
				} else if (BookTicketResultVO.TICKET_TIMEOUT.equals(flightTicketDetailInfo.getStatus())) {
					isTicketSuccessful = false;
					if (timeOutPNameSB.length() > 0) {
						timeOutPNameSB.append(",");
					}
					timeOutPNameSB.append(flightTicketDetailInfo.getPassengerName());
				}else if (BookTicketResultVO.TICKET_PROCESSING.equals(flightTicketDetailInfo.getStatus())) {
					isTicketProcessing = true;
					if (processingPNameSB.length() > 0) {
						processingPNameSB.append(",");
					}
					processingPNameSB.append(flightTicketDetailInfo.getPassengerName());
				} else {
					if(successPNameSB.length() > 0) {
						successPNameSB.append(",");
					} 
					successPNameSB.append(flightTicketDetailInfo.getPassengerName());
				}
				OrdFlightTicketInfo ordFlightTicketInfo = new OrdFlightTicketInfo();
				ordFlightTicketInfo.setOrderItemId(flightTicketCallbackInfoVO.getOrderItemId());
				ordFlightTicketInfo.setPassengerName(flightTicketDetailInfo.getPassengerName());
				ordFlightTicketInfo.setTicketNo(flightTicketDetailInfo.getTicketNo());
				if (flightTicketDetailInfo.getStatus() != null) {
					ordFlightTicketInfo.setTicketStatus(flightTicketDetailInfo.getStatus().name());
				}
				//保存出票信息
				ordFlightTicketInfoService.saveFlightTicketInfo(ordFlightTicketInfo);
			}
		}
		
		OrdFlightTicketStatus ordFlightTicketStatus = new OrdFlightTicketStatus();
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", flightTicketCallbackInfoVO.getOrderItemId());
		List<OrdFlightTicketStatus> ordFlightTicketStatusList = ordFlightTicketStatusService.findByCondition(params);
		String currStatus = null;
		if(ordFlightTicketStatusList != null && ordFlightTicketStatusList.size() > 0) {
			currStatus = ordFlightTicketStatusList.get(0).getStatusCode();
		}
		ordFlightTicketStatus.setOrderItemId(flightTicketCallbackInfoVO.getOrderItemId());
		if(isTicketSuccessful) {
			if(isTicketProcessing){
				ticketStatusLog = BookTicketResultVO.TICKET_PROCESSING.getCnName();
				ordFlightTicketStatus.setStatusCode(BookTicketResultVO.TICKET_PROCESSING.name());
			}else{
				ticketStatusLog = BookTicketResultVO.TICKET_SUCCESS.getCnName();
				ordFlightTicketStatus.setStatusCode(BookTicketResultVO.TICKET_SUCCESS.name());
			}
		} else {
			ticketStatusLog = BookTicketResultVO.TICKET_FAIL.getCnName();
			ordFlightTicketStatus.setStatusCode(BookTicketResultVO.TICKET_FAIL.name());
			//创建预订通知
			saveReservation(
					flightTicketCallbackInfoVO.getOrderId(),
					flightTicketCallbackInfoVO.getOrderItemId(),
					OrderEnum.AUDIT_SUB_TYPE.FLIGHT_TICKET_FAIL.name(),
					"机票订单["
							+ flightTicketCallbackInfoVO.getOrderItemId()
							+ "]"
							+ (failPNameSB.length() > 0 ? "[乘客:"
									+ failPNameSB.toString() + "]出票失败" : "")
							+ (timeOutPNameSB.length() > 0 ? "[乘客:"
									+ timeOutPNameSB.toString() + "]出票超时" : "")
							+ "，请及时进行后续人工处理！");
		}
		
		//保存机票状态
		//如果本来状态为出票失败，即使此次回调为出票成功，对于整个机票订单还是出票失败，所以不做更新
		if(!OrderEnum.ORD_FLIGHT_TICKET_STATUS.TICKET_FAIL.name().equals(currStatus)) {
			LOG.info("FlightOrderCallbackServiceImpl::flightTicketCallback_orderItemId=" + ordFlightTicketStatus.getOrderItemId() + " ticketStatus=" + ordFlightTicketStatus.getStatusCode());
			ordFlightTicketStatusService.saveFlightTicketStatus(ordFlightTicketStatus);
		}
		
		lvmmLogClientService.sendLog(
				ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
				flightTicketCallbackInfoVO.getOrderId(),
				flightTicketCallbackInfoVO.getOrderItemId(),
				"SYSTEM",
				"子订单"
						+ flightTicketCallbackInfoVO.getOrderItemId()
						+ (successPNameSB.length() > 0 ? "，乘客:"
								+ successPNameSB.toString() + "出票成功" : "")
						+ (failPNameSB.length() > 0 ? "，乘客:"
								+ failPNameSB.toString() + "出票失败" : "")
						+ (timeOutPNameSB.length() > 0 ? "，乘客:"
								+ timeOutPNameSB.toString() + "出票超时" : "")
						+ (processingPNameSB.length() > 0 ? "，乘客:"
								+ processingPNameSB.toString() + "正在出票" : ""),
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()
						+ "[" + ticketStatusLog + "]", "");
		
		return resultHandle;
	}

	
	
	/**
	 * 出票失败后产生预定通知
	 * @param orderId
	 * @param loginUserId
	 */
	public int saveReservation(Long orderId, Long orderItemId, String subType, String messageContent) {
		//发送给主订单
		String objectType = "ORDER";
		PermUser permUserPrincipal = orderResponsibleService.getOrderPrincipal(objectType, orderId);
		String orderPrincipal = permUserPrincipal.getUserName();
		
		String receiver = null;
		if (!StringUtils.isEmpty(orderPrincipal)) {
			receiver = orderPrincipal;
		}
//		String messageContent="机票出票失败，请及时进行后续人工处理！";
		
		ComMessage comMessage=new ComMessage();
		comMessage.setMessageContent(messageContent);
		comMessage.setReceiver(receiver);
		
		return comMessageService.saveReservationChildOrder(comMessage, null, subType,
				orderId, orderItemId, "SYSTEM", messageContent);
	}
	
	/**
	 * 出票失败后产生预定通知
	 * @param orderId
	 * @param loginUserId
	 */
	public ComAudit newReservation(Long orderId, Long orderItemId, String subType, String messageContent) {
		//发送给主订单
		String objectType = "ORDER";
		PermUser permUserPrincipal = orderResponsibleService.getOrderPrincipal(objectType, orderId);
		String orderPrincipal = permUserPrincipal.getUserName();
		
		String receiver = null;
		if (!StringUtils.isEmpty(orderPrincipal)) {
			receiver = orderPrincipal;
		}
//		String messageContent="机票出票失败，请及时进行后续人工处理！";
		
		ComMessage comMessage=new ComMessage();
		comMessage.setMessageContent(messageContent);
		comMessage.setReceiver(receiver);
		
		return comMessageService.newReservationChildOrder(comMessage, null, subType,
				orderId, orderItemId, "SYSTEM", messageContent);
	}



	@Override
	public ResultHandle flightLockSeatCallback(
			List<FlightLockSeatCallbackInfoVO> flightLockSeatCallbackInfoVOs)
			throws Exception {
		ResultHandle resultHandle = new ResultHandle();
		if(CollectionUtils.isEmpty(flightLockSeatCallbackInfoVOs)) {
			LOG.error("机票锁舱回调失败：flightLockSeatCallbackInfoVOs is empty");
			resultHandle.setMsg("flightLockSeatCallbackInfoVOs is null");
			return resultHandle;
		}
		
		for(FlightLockSeatCallbackInfoVO flightLockSeatCallbackInfoVO : flightLockSeatCallbackInfoVOs) {
			ResultHandle result = flightLockSeatCallback(flightLockSeatCallbackInfoVO);
			if(result == null || result.isFail()) {
				String errMsg = result == null ? "子订单" + flightLockSeatCallbackInfoVO.getOrderItemId() + "锁舱回调失败" : result.getMsg();
				resultHandle.setMsg(StringUtils.isBlank(resultHandle.getMsg()) ? errMsg : resultHandle.getMsg() + "," + errMsg);
			}
		}
		
		return resultHandle;
	}



	@Override
	public ResultHandle flightLockSeatCallback(
			FlightLockSeatCallbackInfoVO flightLockSeatCallbackInfoVO)
			throws Exception {
		ResultHandle resultHandle = new ResultHandle();
		if(flightLockSeatCallbackInfoVO == null) {
			LOG.error("机票锁舱回调失败：flightLockSeatCallbackInfoVO is null");
			resultHandle.setMsg("flightLockSeatCallbackInfoVO is null");
			return resultHandle;
		}
		LOG.info("机票锁舱回调，orderId:" + flightLockSeatCallbackInfoVO.getOrderId()
				+ ", orderItemId:"
				+ flightLockSeatCallbackInfoVO.getOrderItemId() + ", result:"
				+ (flightLockSeatCallbackInfoVO.getLockSeatResult() == null ? null
				: flightLockSeatCallbackInfoVO.getLockSeatResult().getCnName())
				+ " flightLockSeatCallbackInfoVO.getLockSeatResult()="
				+ flightLockSeatCallbackInfoVO.getLockSeatResult());
		
		if(flightLockSeatCallbackInfoVO.getOrderId() == null || flightLockSeatCallbackInfoVO.getOrderItemId() == null) {
			LOG.error("机票锁舱回调失败：orderId/orderItemId is null");
			resultHandle.setMsg("orderId/orderItemId is null");
			return resultHandle;
		}
		
		OrdOrder order = orderService.queryOrdorderByOrderId(flightLockSeatCallbackInfoVO.getOrderId());
		
		if(order == null) {
			LOG.error("机票锁舱回调失败：找不到对应的订单，orderId:" + flightLockSeatCallbackInfoVO.getOrderId());
			resultHandle.setMsg("can't find the order by orderId " + flightLockSeatCallbackInfoVO.getOrderId());
			return resultHandle;
		}
		OrdOrderItem orderItem = orderUpdateService.getOrderItem(flightLockSeatCallbackInfoVO.getOrderItemId());
		
		if(orderItem == null) {
			LOG.error("机票锁舱回调失败：找不到对应的子订单，orderItemId:" + flightLockSeatCallbackInfoVO.getOrderItemId());
			resultHandle.setMsg("can't find the order by orderItemId " + flightLockSeatCallbackInfoVO.getOrderItemId());
			return resultHandle;
		}
		
		OrdFlightTicketStatus ordFlightTicketStatus = new OrdFlightTicketStatus();
		ordFlightTicketStatus.setOrderItemId(flightLockSeatCallbackInfoVO.getOrderItemId());
		boolean isLockSeatSuccessful = false;
		
		if (flightLockSeatCallbackInfoVO.getLockSeatResult() != null
				&& LockSeatResultVO.SUCCESS.name()
						.equals(flightLockSeatCallbackInfoVO
								.getLockSeatResult().name())) {
			isLockSeatSuccessful = true;
			ordFlightTicketStatus.setStatusCode(OrderEnum.ORD_FLIGHT_TICKET_STATUS.LOCK_SUCCESS.name());
		} else {
			ordFlightTicketStatus.setStatusCode(OrderEnum.ORD_FLIGHT_TICKET_STATUS.LOCK_FAIL.name());
		}
		
		//保存机票状态
		ordFlightTicketStatusService.saveFlightTicketStatus(ordFlightTicketStatus);
		
		if (isLockSeatSuccessful) {
			// 将更改资源保留时长的逻辑拿到vst_workfolw各子单审核通过以后才进行设置
			/*BizSystemConfigure bizSystemConfigure = bizSystemConfigureClientRemote.getCurrentSysConfigureByKey("VST_ORDER-AIRTICKET_ORDERITEM_KEEPTIME");
			//设置资源保留时长 
			Date retentionTime = DateUtil.getDateAfterMinutes(60); 
			if(bizSystemConfigure != null && StringUtil.isNotEmptyString(bizSystemConfigure.getConfigureValue()) && StringUtil.isNumber(bizSystemConfigure.getConfigureValue())){
				retentionTime = DateUtil.getDateAfterMinutes(Long.parseLong(bizSystemConfigure.getConfigureValue()));
			}
			String currRetentionTimeStr = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name());
			Date currRetentionTime = null;
			if(StringUtils.isNotBlank(currRetentionTimeStr)){
				currRetentionTime = DateUtil.toDate(currRetentionTimeStr, DateUtil.HHMMSS_DATE_FORMAT);
			}
			
			if(currRetentionTime == null || retentionTime.before(currRetentionTime)) {
				String resourceRetentionTime = DateUtil.formatDate(retentionTime, DateUtil.HHMMSS_DATE_FORMAT);
				
				orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name(), resourceRetentionTime);
				orderService.updateOrderItem(orderItem);
				
				//设置支付等待时间
				if(order.getWaitPaymentTime() == null || retentionTime.before(order.getWaitPaymentTime())) {
					order.setWaitPaymentTime(retentionTime);
				}
				orderService.updateOrdOrder(order);
			}*/
			
			//如果该子订单资源未审核，则将资源审核通过
			if(!OrderEnum.RESOURCE_STATUS.AMPLE.name().equals(orderItem.getResourceStatus())) {
				String newStatus = OrderEnum.RESOURCE_STATUS.AMPLE.name();
				String resourceRetentionTime = (String)orderItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name());
				/*国内设置锁仓资源保留时间*/
				if(BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())){
					orderItem = orderUpdateService.getOrderItem(flightLockSeatCallbackInfoVO.getOrderItemId());
					order = orderService.queryOrdorderByOrderId(flightLockSeatCallbackInfoVO.getOrderId());
					setLockTimeForResource(flightLockSeatCallbackInfoVO, order, orderItem);
				}
				orderService.executeUpdateChildResourceStatus(orderItem, newStatus, resourceRetentionTime, "SYSTEM", orderItem.getOrderMemo(),false);
			}
		} else {
			//创建预订通知
			newReservation(flightLockSeatCallbackInfoVO.getOrderId(), flightLockSeatCallbackInfoVO.getOrderItemId(), OrderEnum.AUDIT_SUB_TYPE.FLIGHT_LOCKSEAT_FAIL.name(), "机票锁仓失败，请及时进行后续人工处理！");
		}
		
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
						flightLockSeatCallbackInfoVO.getOrderId(),
						flightLockSeatCallbackInfoVO.getOrderItemId(),
						"SYSTEM",
						"子订单[" + flightLockSeatCallbackInfoVO.getOrderItemId()
								+ "]锁仓" + (isLockSeatSuccessful ? "成功" : "失败"),
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE
								.getCnName()
								+ "[锁仓"
								+ (isLockSeatSuccessful ? "成功" : "失败")
								+ "]"
								+ (StringUtils
										.isBlank(flightLockSeatCallbackInfoVO
												.getRemark()) ? "" : ", "
										+ flightLockSeatCallbackInfoVO
												.getRemark()), "");
		
		return resultHandle;
	}
	
	private void setLockTimeForResource(FlightLockSeatCallbackInfoVO flightLockSeatCallbackInfoVO, OrdOrder order,
			OrdOrderItem orderItem) {
		if(null!=flightLockSeatCallbackInfoVO.getOrderLimitTime()){
			LOG.info("机票锁舱回调，orderId:" + flightLockSeatCallbackInfoVO.getOrderId()+",orderItemId:"+orderItem.getOrderItemId()+",flightLockSeatCallbackInfoVO.getOrderLimitTime():"+flightLockSeatCallbackInfoVO.getOrderLimitTime());
			Long orderLimitTime=flightLockSeatCallbackInfoVO.getOrderLimitTime();
			Date maxLastCancelTime = null;
			Date limitTime = new Date(orderLimitTime.longValue());
			Date retentionTime=DateUtil.DsDay_Second(limitTime, -300);
			if(!order.isPayMentType()){
				for (OrdOrderItem orderItems : order.getOrderItemList()) {
					Date lastCancelTime = orderItems.getLastCancelTime();
					if (lastCancelTime != null) {
						if (maxLastCancelTime == null) {
							maxLastCancelTime = lastCancelTime;
						} else {
							if (lastCancelTime.before(maxLastCancelTime)) {
								maxLastCancelTime = lastCancelTime;
							}
						}
					}
				}
				if(maxLastCancelTime != null && maxLastCancelTime.before(retentionTime)){
					retentionTime = maxLastCancelTime;
				}
				//设置支付等待时间
				order.setWaitPaymentTime(retentionTime);
				orderService.updateOrdOrder(order);
			}
			OrdOrderItem newOrderItem=new OrdOrderItem();
			newOrderItem.setOrderItemId(orderItem.getOrderItemId());
			newOrderItem.setContent(orderItem.getContent());
			newOrderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name(), DateUtil.formatDate(retentionTime, DateUtil.HHMMSS_DATE_FORMAT));
			newOrderItem.setResourceAmpleTime(new Date());
			orderService.updateOrderItem(newOrderItem);
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
					flightLockSeatCallbackInfoVO.getOrderId(),
					flightLockSeatCallbackInfoVO.getOrderItemId(),
					"SYSTEM",
					"子订单[" + flightLockSeatCallbackInfoVO.getOrderItemId()
							+ "]机票保留时间:" +DateUtil.getDateTime(DateUtil.PATTERN_yyyy_MM_dd_HH_mm_ss, limitTime),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE
							.getCnName()
							+ "[最晚取消时间:"
							+ (retentionTime)
							+ "]"
							+ (StringUtils
									.isBlank(flightLockSeatCallbackInfoVO
											.getRemark()) ? "" : ", "
									+ flightLockSeatCallbackInfoVO
											.getRemark()), "");
		}else{
			LOG.info("机票锁舱回调，orderId:" + flightLockSeatCallbackInfoVO.getOrderId()+",orderItemId:"+orderItem.getOrderItemId()+",flightLockSeatCallbackInfoVO.getTime is null:"+(flightLockSeatCallbackInfoVO!=null?flightLockSeatCallbackInfoVO.getOrderLimitTime():"null"));
		}
	}
}