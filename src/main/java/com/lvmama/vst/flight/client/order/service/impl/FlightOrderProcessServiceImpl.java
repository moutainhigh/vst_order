package com.lvmama.vst.flight.client.order.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.order.timeprice.service.lvf.OrderLvfTimePriceServiceImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.lvf.openapi.vstclient.dto.BaseResponseVSTDto;
import com.lvmama.lvf.openapi.vstclient.dto.FitFliCallBackResponseVSTDto;
import com.lvmama.lvf.openapi.vstclient.request.FitFliBookingCallBackRequest;
import com.lvmama.lvf.openapi.vstclient.service.fit.FitFlightBookingCallBackService;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.prod.service.ProdAdditionFlagClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdTrafficClientService;
import com.lvmama.vst.back.order.po.OrdFlightTicketStatus;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdTraffic;
import com.lvmama.vst.back.prod.vo.ProdAdditionFlag;
import com.lvmama.vst.back.prod.vo.ProdTrafficVO;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.IdCardUtil;
import com.lvmama.vst.comm.utils.SynchronizedLock;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.flight.client.order.service.FlightOrderProcessService;
import com.lvmama.vst.flight.client.order.service.FlightOrderService;
import com.lvmama.vst.flight.client.order.vo.BookingSourceVO;
import com.lvmama.vst.flight.client.order.vo.FlightGoodsDetailVO;
import com.lvmama.vst.flight.client.order.vo.FlightOrderBookingRequestVO;
import com.lvmama.vst.flight.client.order.vo.FlightOrderCancelRequestVO;
import com.lvmama.vst.flight.client.order.vo.FlightOrderContacterVO;
import com.lvmama.vst.flight.client.order.vo.FlightOrderCustomerVO;
import com.lvmama.vst.flight.client.order.vo.FlightOrderPassengerVO;
import com.lvmama.vst.flight.client.order.vo.FlightOrderPayRequestVO;
import com.lvmama.vst.flight.client.order.vo.FlightOrderRemarkVO;
import com.lvmama.vst.flight.client.order.vo.FlightTripTypeVO;
import com.lvmama.vst.flight.client.order.vo.GenderVO;
import com.lvmama.vst.flight.client.order.vo.IDCardTypeVO;
import com.lvmama.vst.flight.client.order.vo.PassengerTypeVO;
import com.lvmama.vst.flight.client.order.vo.RemarkTypeVO;
import com.lvmama.vst.order.dao.ComJobConfigDAO;
import com.lvmama.vst.order.service.IComMessageService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdFlightTicketInfoService;
import com.lvmama.vst.order.service.IOrdFlightTicketStatusService;
import com.lvmama.vst.order.service.IOrdItemPersonRelationService;
import com.lvmama.vst.order.service.IOrderResponsibleService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.pet.adapter.IPayPaymentServiceAdapter;
import com.lvmama.vst.pet.vo.PayAndPreVO;

@Component("flightOrderProcessServiceRemote")
public class FlightOrderProcessServiceImpl implements FlightOrderProcessService {

	private static final Logger logger = LoggerFactory.getLogger(FlightOrderProcessServiceImpl.class);
	
	private static final int MAX_TRY_TIMES = 1;
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrdItemPersonRelationService ordItemPersonRelationService;
	
	@Autowired
	@Qualifier("flightOrderServiceRemote")
	private FlightOrderService flightOrderService;
	
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
    private ProdTrafficClientService prodTrafficClientServiceRemote;//得到交通的详细的数据
    
    @Autowired
	private ComJobConfigDAO comJobConfigDAO;
    @Autowired
    private FitFlightBookingCallBackService fitFlightBookingCallBackService;
    @Autowired
    private ProdAdditionFlagClientService prodAdditionFlagClientService;
	
	@Override
	public ResultHandle lockSeat(Long orderItemId) throws Exception {
		ResultHandle resultHandle = new ResultHandle();
		if(orderItemId == null) {
			resultHandle.setMsg("orderItemId is null");
			return resultHandle;
		}
		OrdOrderItem orderItem = orderUpdateService.getOrderItem(orderItemId);
		if(orderItem == null) {
			resultHandle.setMsg("锁舱失败, 找不到对应的子订单, 子订单ID: " + orderItemId);
			return resultHandle;
		}
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderItem.getOrderId());
		if(order == null) {
			resultHandle.setMsg("锁舱失败, 找不到对应的订单, 子订单ID: " + orderItemId);
			return resultHandle;
		}
		
		//组装请求信息
		FlightOrderBookingRequestVO orderBookingRequest = buildFlightBookingRequest(orderItem, order);
		
		long start = System.currentTimeMillis();
		try {
			logger.info("锁舱参数：----" + JSONObject.fromObject(orderBookingRequest).toString());
		} catch (Exception e) {
			logger.error("orderBookingRequest----" + e.getMessage());
		}
		int tryTime = 0;
		//第一次调用锁舱或锁舱失败并且小于最大尝试次数
		while(tryTime == 0 || (resultHandle.isFail() && tryTime < MAX_TRY_TIMES)) {
			tryTime ++;
			resultHandle = new ResultHandle();
			try {
				//调用机票系统的锁舱接口
				resultHandle = flightOrderService.bookingOrder(orderBookingRequest);
			} catch (Exception e) {
				logger.info("{}", e);
				resultHandle.setMsg(e);
			}
			
			if (resultHandle == null) {
				resultHandle = new ResultHandle();
				resultHandle.setMsg("锁舱失败, 调用无响应, 子订单ID: " + orderItemId);
			} 
			
			if(resultHandle.isFail() && tryTime < MAX_TRY_TIMES) {
				Thread.sleep(100);
			}
		}
		
		long end = System.currentTimeMillis();
		String logContent = "锁舱结果：子订单号[" + orderItemId + "]，调用结果：isSuccess[" + resultHandle.isSuccess() + "],msg:[" + resultHandle.getMsg() + "], cost:" + (end-start) +"毫秒";
		logger.info(logContent);
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
				order.getOrderId(),
				orderItemId,
				"SYSTEM",
				logContent,
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName() + "[锁舱" + (resultHandle.isFail() ? "失败" : "成功") + "]",
				"");
		
		OrdFlightTicketStatus ordFlightTicketStatus = new OrdFlightTicketStatus();
		ordFlightTicketStatus.setOrderItemId(orderItemId);
		if(resultHandle.isFail()) {
			ordFlightTicketStatus.setStatusCode(OrderEnum.ORD_FLIGHT_TICKET_STATUS.LOCK_FAIL.name());
		} else {
			ordFlightTicketStatus.setStatusCode(OrderEnum.ORD_FLIGHT_TICKET_STATUS.LOCK_SUCCESS.name());
		}
		
		//保存机票状态
		ordFlightTicketStatusService.saveFlightTicketStatus(ordFlightTicketStatus);
		
		return resultHandle;
	}

	/**
	 * 锁仓【并发】
	 */
	public ResultHandle lockSeatMult(OrdOrder order, List<OrdOrderItem> apiFlightList) throws Exception {
		ResultHandle resultHandle = new ResultHandle();
		if(CollectionUtils.isEmpty(apiFlightList)) {
			logger.error("apiFlightList is empty");
			resultHandle.setMsg("锁舱通知失败, 机票子订单不存在");
			return resultHandle;
		}
		//超级自由行的品类也是用的29，是否是超级自由行
		boolean isSuperFreetour = StringUtils.equalsIgnoreCase(OrderEnum.ORDER_CREATING_MANNER.supperFree.getCode(),
					order.getOrderCreatingManner());
		//如果是交通+X 动态打包产品的订单，锁舱快速成功，无须VST发起锁舱通知
		if(OrderLvfTimePriceServiceImpl.isAutoPackCategory(order.getCategoryId()) && (!isSuperFreetour)){
			// 记录锁仓log
			String logContent = "锁舱通知结果：主订单ID[" + order.getOrderId() + "]，调用结果：isSuccess[" + resultHandle.isSuccess() + "],msg:[系统正在锁仓，请等待锁仓结果]";
			logger.info(logContent);
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					order.getOrderId(),
					order.getOrderId(),
					"SYSTEM",
					logContent,
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"[发送锁舱通知" + (resultHandle.isFail() ? "失败" : "成功") + "]",
					"");
			return resultHandle;
		}
		
		//组装请求信息-list
		List<FlightOrderBookingRequestVO> orderBookingRequestList = new ArrayList<FlightOrderBookingRequestVO>();
		for (OrdOrderItem item : apiFlightList) {
			FlightOrderBookingRequestVO orderBookingRequest = buildFlightBookingRequest(item, order);
			orderBookingRequestList.add(orderBookingRequest);
		}
		
		try {
			logger.info("锁舱参数：----" + JSONArray.fromObject(orderBookingRequestList).toString());
		} catch (Exception e) {
			logger.error("orderBookingRequest----" + e.getMessage());
		}
		int tryTime = 0;
		
		//第一次调用锁舱或锁舱失败并且小于最大尝试次数
		long start = System.currentTimeMillis();
		while(tryTime == 0 || (resultHandle.isFail() && tryTime < MAX_TRY_TIMES)) {
			tryTime ++;
			resultHandle = new ResultHandle();
			try {
				//调用机票系统的锁舱接口
				resultHandle = flightOrderService.bookingOrderAsync(orderBookingRequestList);
			} catch (Exception e) {
				logger.info("{}", e);
				resultHandle.setMsg(e);
			}
			
			if (resultHandle == null) {
				resultHandle = new ResultHandle();
				resultHandle.setMsg("锁舱通知失败, 调用无响应, 主订单ID: " + order.getOrderId());
			} 
			
			if(resultHandle.isFail() && tryTime < MAX_TRY_TIMES) {
				Thread.sleep(100);
			}
		}
		long end = System.currentTimeMillis();
		
		// 记录锁仓log
		String logContent = "锁舱通知结果：主订单ID[" + order.getOrderId() + "]，调用结果：isSuccess[" + resultHandle.isSuccess() + "],msg:[" + resultHandle.getMsg() + "], cost:" + (end-start) +"毫秒";
		logger.info(logContent);
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
				order.getOrderId(), 
				order.getOrderId(), 
				"SYSTEM", 
				logContent, 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"[发送锁舱通知" + (resultHandle.isFail() ? "失败" : "成功") + "]",
				"");
		
		for (OrdOrderItem orderItem : apiFlightList) {
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
					orderItem.getOrderItemId(), 
					orderItem.getOrderItemId(), 
					"system", 
					"已发起锁仓，等待对接系统回调", 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName(),
					"");
		}
		
		
		if(resultHandle.isFail()) {
			//创建预订通知
			saveReservation(order.getOrderId(), order.getOrderId(), OrderEnum.AUDIT_SUB_TYPE.FLIGHT_LOCKSEAT_FAIL.name(), "机票锁仓失败，请及时进行后续人工处理！");
		}
		
		//保存机票状态
		/*
		for (OrdOrderItem item : apiFlightList) {
			OrdFlightTicketStatus ordFlightTicketStatus = new OrdFlightTicketStatus();
			ordFlightTicketStatus.setOrderItemId(item.getOrderItemId());
			if(resultHandle.isFail()) {
				ordFlightTicketStatus.setStatusCode(OrderEnum.ORD_FLIGHT_TICKET_STATUS.LOCK_FAIL.name());
			} else {
				ordFlightTicketStatus.setStatusCode(OrderEnum.ORD_FLIGHT_TICKET_STATUS.LOCK_SUCCESS.name());
			}
			ordFlightTicketStatusService.saveFlightTicketStatus(ordFlightTicketStatus);
		}*/
		
		return resultHandle;
	}

	/**
	 * 构建锁舱的请求对象
	 * @param orderItem
	 * @param order
	 * @return
	 */
	private FlightOrderBookingRequestVO buildFlightBookingRequest(OrdOrderItem orderItem, OrdOrder order) {
		if(orderItem == null || order == null) {
			return null;
		}
		FlightOrderBookingRequestVO orderBookingRequest = new FlightOrderBookingRequestVO();
		orderBookingRequest.setVstMainOrderId(order.getOrderId());
		orderBookingRequest.setVstFlightOrderId(orderItem.getOrderItemId());
		if(order.getDistributorId() == 2) {
			orderBookingRequest.setBookingSource(BookingSourceVO.VST_PACKAGE_BACK);
		} else if(order.getDistributorId() == 3) {
			orderBookingRequest.setBookingSource(BookingSourceVO.VST_PACKAGE_FRONT);
		} else {
			orderBookingRequest.setBookingSource(BookingSourceVO.VST_DISTRIBUTION);
		}

		orderBookingRequest.setChannelValue(order.getDistributorCode());
		orderBookingRequest.setContacter(buildFlightContacter(order));
		long start = System.currentTimeMillis();
		orderBookingRequest.setFlightDetails(buildFlightGoodsDetails(orderItem));
		long end = System.currentTimeMillis();
		logger.info("前置锁舱发起锁仓构建参数setFlightDetails cost: "+(end-start) +"毫秒");
		orderBookingRequest.setFlightOrderCustomer(buildFlightOrderCustomer(order));
//		orderBookingRequest.setFlightOrderExpress(buildFlightOrderExpress(order));
		orderBookingRequest.setFlightOrderRemarks(buildFlightOrderRemark(order, orderItem));
		orderBookingRequest.setPassengers(buildPassengers(order, orderItem));
		
		orderBookingRequest.setTotalAmount(orderItem.getTotalAmount());
		
		return orderBookingRequest;
	}
	
	/**
	 * 构建前置锁舱的请求对象
	 * @param orderItem
	 * @param order
	 * @return
	 */
	private FlightOrderBookingRequestVO buildFlightBookingRequestForPreLockSeat(OrdOrderItem orderItem, OrdOrder order) {
		if(orderItem == null || order == null) {
			return null;
		}
		FlightOrderBookingRequestVO orderBookingRequest = new FlightOrderBookingRequestVO();
		orderBookingRequest.setVstMainOrderId(order.getOrderId());
		orderBookingRequest.setVstFlightOrderId(orderItem.getOrderItemId());
		if(order.getDistributorId() == 2) {
			orderBookingRequest.setBookingSource(BookingSourceVO.VST_PACKAGE_BACK);
		} else if(order.getDistributorId() == 3) {
			orderBookingRequest.setBookingSource(BookingSourceVO.VST_PACKAGE_FRONT);
		} else {
			orderBookingRequest.setBookingSource(BookingSourceVO.VST_DISTRIBUTION);
		}

		orderBookingRequest.setChannelValue(order.getDistributorCode());
		orderBookingRequest.setContacter(buildFlightContacter(order));
		long start = System.currentTimeMillis();
		List<FlightGoodsDetailVO> flightDetails = new ArrayList<FlightGoodsDetailVO>();
		FlightGoodsDetailVO flightDetail = new FlightGoodsDetailVO();
		flightDetails.add(flightDetail);
		flightDetail.setGoodsId(orderItem.getSuppGoodsId());
		flightDetail.setDepartureDate(DateUtil.formatSimpleDate(orderItem.getVisitTime()));
		flightDetail.setFlightTripType(FlightTripTypeVO.DEPARTURE);
		orderBookingRequest.setFlightDetails(flightDetails);
		long end = System.currentTimeMillis();
		logger.info("前置锁舱发起锁仓构建参数setFlightDetails cost: "+(end-start) +"毫秒");
		orderBookingRequest.setFlightOrderCustomer(buildFlightOrderCustomer(order));
//		orderBookingRequest.setFlightOrderExpress(buildFlightOrderExpress(order));
		orderBookingRequest.setFlightOrderRemarks(buildFlightOrderRemark(order, orderItem));
		orderBookingRequest.setPassengers(buildPassengers(order, orderItem));
		
		orderBookingRequest.setTotalAmount(orderItem.getTotalAmount());
		
		return orderBookingRequest;
	}
	
	/**
	 * 构建联系人信息
	 * @param order
	 * @return
	 */
	private FlightOrderContacterVO buildFlightContacter(OrdOrder order) {
		if(order == null) {
			return null;
		}
		FlightOrderContacterVO contacter = new FlightOrderContacterVO();
//		contacter.setOrderMainId(order.getOrderId());
		
		//confirmType		确认类型
		OrdPerson contactPerson = order.getContactPerson();
		if(contactPerson != null) {
			contacter.setName(contactPerson.getFullName());
			contacter.setCellphone(contactPerson.getMobile());
			contacter.setTelphone(contactPerson.getPhone());
			contacter.setEmail(contactPerson.getEmail());
		}
		
		OrdPerson emergencyContact = order.getEmergencyContact();
		if(emergencyContact != null) {
			contacter.setEmergencyCellphone(emergencyContact.getMobile());
			contacter.setEmergencyContactName(emergencyContact.getFullName());
			contacter.setEmergencyTelphone(emergencyContact.getPhone());
			contacter.setEmergencyEmail(emergencyContact.getEmail());
		}
		
		return contacter;
	}
	
	/**
	 * 构建机票商品信息
	 * @param orderItem
	 * @return
	 */
	private List<FlightGoodsDetailVO> buildFlightGoodsDetails(OrdOrderItem orderItem) {
		if(orderItem == null) {
			return null;
		}
		List<FlightGoodsDetailVO> flightDetails = new ArrayList<FlightGoodsDetailVO>();
		FlightGoodsDetailVO flightDetail = new FlightGoodsDetailVO();
		flightDetails.add(flightDetail);
		flightDetail.setGoodsId(orderItem.getSuppGoodsId());
		flightDetail.setDepartureDate(DateUtil.formatSimpleDate(orderItem.getVisitTime()));
		
		ProdTrafficVO prodTrafficVO = prodTrafficClientServiceRemote
				.getProdTrafficVOByProductId(orderItem.getProductId());
		if(prodTrafficVO != null) {
			ProdTraffic prodTraffic = prodTrafficVO.getProdTraffic();
			if(ProdTraffic.TRAFFICTYPE.FLIGHT.name().equalsIgnoreCase(prodTraffic.getBackType())) {
				flightDetail.setFlightTripType(FlightTripTypeVO.RETURN);
			}
			
			if(ProdTraffic.TRAFFICTYPE.FLIGHT.name().equalsIgnoreCase(prodTraffic.getToType())) {
				flightDetail.setFlightTripType(FlightTripTypeVO.DEPARTURE);
			}
			
			if (ProdTraffic.TRAFFICTYPE.FLIGHT.name().equalsIgnoreCase(
					prodTraffic.getBackType())
					&& ProdTraffic.TRAFFICTYPE.FLIGHT.name().equalsIgnoreCase(
							prodTraffic.getToType())) {
				flightDetail.setFlightTripType(FlightTripTypeVO.DEP_RET);
			}
		}
		return flightDetails;
	}
	
	/**
	 * 构建客户信息
	 * @param order
	 * @return
	 */
	private FlightOrderCustomerVO buildFlightOrderCustomer(OrdOrder order) {
		if(order == null) {
			return null;
		}
		FlightOrderCustomerVO flightOrderCustomer = new FlightOrderCustomerVO();
		flightOrderCustomer.setCustomerId(order.getUserId());
		if(order.getUserNo() != null) {
			flightOrderCustomer.setCustomerCode(String.valueOf(order.getUserNo()));
		}
		OrdPerson bookerPerson = order.getBookerPerson();
		if(bookerPerson != null) {
			flightOrderCustomer.setCustomerName(bookerPerson.getFullName());
		}
		return flightOrderCustomer;
	}
	
	/**
	 * 构建快递信息
	 * @param order
	 * @return
	 */
	/*private FlightOrderExpressVO buildFlightOrderExpress(OrdOrder order) {
		if(order == null) {
			return null;
		}
		
		FlightOrderExpressVO flightOrderExpress = new FlightOrderExpressVO();
		flightOrderExpress.setOrderMainId(order.getOrderId());

		OrdAddress ordAddress = order.getOrdAddress();
		OrdPerson addressPerson = order.getAddressPerson();
		
		if(addressPerson != null) {
			flightOrderExpress.setRecipient(addressPerson.getFullName());
			flightOrderExpress.setTelephone(addressPerson.getPhone());
			flightOrderExpress.setCellphone(addressPerson.getMobile());
		}
		StringBuffer addrStrBuffer = new StringBuffer();
		if(ordAddress != null) {
			addrStrBuffer.append(StringUtils.isNotBlank(ordAddress.getProvince()) ? ordAddress.getProvince() : "");
			addrStrBuffer.append(StringUtils.isNotBlank(ordAddress.getCity()) ? ordAddress.getCity() : "");
			addrStrBuffer.append(StringUtils.isNotBlank(ordAddress.getDistrict()) ? ordAddress.getDistrict() : "");
			addrStrBuffer.append(StringUtils.isNotBlank(ordAddress.getStreet()) ? ordAddress.getStreet() : "");
			flightOrderExpress.setAddress(addrStrBuffer.toString());
			flightOrderExpress.setPostCode(StringUtils.isNotBlank(ordAddress.getPostalCode()) ? ordAddress.getProvince() : "");
		}
		
		if(StringUtils.isNotBlank(order.getExpressAmountYuan())) {
			flightOrderExpress.setExpressPrice(new BigDecimal(order.getExpressAmountYuan()));
		}
		
		return flightOrderExpress;
	}*/
	
	/**
	 * 构建备注信息
	 * @param order
	 * @return
	 */
	private List<FlightOrderRemarkVO> buildFlightOrderRemark(OrdOrder order, OrdOrderItem orderItem) {
		if(order == null || orderItem == null) {
			return null;
		}
		List<FlightOrderRemarkVO> flightOrderRemarks = new ArrayList<FlightOrderRemarkVO>();
		
		FlightOrderRemarkVO flightOrderRemark = null;
		
		if(StringUtils.isNotBlank(order.getRemark())) {
			flightOrderRemark = new FlightOrderRemarkVO();
			flightOrderRemarks.add(flightOrderRemark);
			
//			flightOrderRemark.setOrderMainId(order.getOrderId());
//			flightOrderRemark.setOrderId(orderItem.getOrderItemId());
			
//			FlightOrderNoVO flightOrderNo = new FlightOrderNoVO();
//			flightOrderRemark.setFlightOrderNo(flightOrderNo);
//			flightOrderNo.setOrderNo(String.valueOf(orderItem.getOrderItemId()));
			
			flightOrderRemark.setRemarkType(RemarkTypeVO.CUSTOMER);
			flightOrderRemark.setRemark(order.getRemark());
		}
		
		if(StringUtils.isNotBlank(order.getOrderMemo())) {
			flightOrderRemark = new FlightOrderRemarkVO();
			flightOrderRemarks.add(flightOrderRemark);
			
//			flightOrderRemark.setOrderMainId(order.getOrderId());
//			flightOrderRemark.setOrderId(orderItem.getOrderItemId());
//			
//			FlightOrderNoVO flightOrderNo = new FlightOrderNoVO();
//			flightOrderRemark.setFlightOrderNo(flightOrderNo);
//			flightOrderNo.setOrderNo(String.valueOf(orderItem.getOrderItemId()));
			
			flightOrderRemark.setRemarkType(RemarkTypeVO.BACK);
			flightOrderRemark.setRemark(order.getOrderMemo());
		}
		
		if(StringUtils.isNotBlank(orderItem.getOrderMemo())) {
			flightOrderRemark = new FlightOrderRemarkVO();
			flightOrderRemarks.add(flightOrderRemark);
			
//			flightOrderRemark.setOrderMainId(order.getOrderId());
//			flightOrderRemark.setOrderId(orderItem.getOrderItemId());
//			
//			FlightOrderNoVO flightOrderNo = new FlightOrderNoVO();
//			flightOrderRemark.setFlightOrderNo(flightOrderNo);
//			flightOrderNo.setOrderNo(String.valueOf(orderItem.getOrderItemId()));
			
			flightOrderRemark.setRemarkType(RemarkTypeVO.BACK);
			flightOrderRemark.setRemark(orderItem.getOrderMemo());
		}
		
		
		return flightOrderRemarks;
	}
	
	/**
	 * 构建乘客信息
	 * @param order
	 * @return
	 */
	private List<FlightOrderPassengerVO> buildPassengers(OrdOrder order, OrdOrderItem orderItem) {
		if(order == null || orderItem == null) {
			return null;
		}
		
		List<OrdPerson> travellers = order.getOrdTravellerList();
		if(travellers == null) {
			return null;
		}
		
		List<FlightOrderPassengerVO> passengers = new ArrayList<FlightOrderPassengerVO>();
		FlightOrderPassengerVO passenger = null;
		for(OrdPerson traveller : travellers) {
			passenger = new FlightOrderPassengerVO();
			passengers.add(passenger);
			
//			passenger.setOrderMainId(order.getOrderId());
			passenger.setPassengerName(traveller.getFullName());
			passenger.setPassengerType(this.transPassengerType(traveller.getPeopleType()));
			passenger.setGender(this.transGender(traveller.getGender()));
			passenger.setPassengerIDCardType(this.transIDCardType(traveller.getIdType()));
			passenger.setPassengerIDCardNo(traveller.getIdNo());
			if(traveller.getBirthday() != null) {
				passenger.setPassengerBirthday(traveller.getBirthday());
			} else if (IDCardTypeVO.ID == passenger.getPassengerIDCardType() && StringUtils.isNotBlank(passenger.getPassengerIDCardNo())){
				// 截取身份证生日
				passenger.setPassengerBirthday(IdCardUtil.getBirthDate(passenger.getPassengerIDCardNo()));
			}
			passenger.setTelphone(traveller.getPhone());
			passenger.setCellphone(traveller.getMobile());
			
			/*
			List<FlightOrderInsuranceVO> flightOrderInsurances = new ArrayList<FlightOrderInsuranceVO>();
			passenger.setFlightOrderInsurances(flightOrderInsurances);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("ordPersonId", passenger.getId());
			
			List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(params);
			if(ordItemPersonRelationList != null) {
				for(OrdItemPersonRelation relation : ordItemPersonRelationList) {
					OrdOrderItem currOrderItem = orderUpdateService.getOrderItem(relation.getOrderItemId());
					if(currOrderItem == null || currOrderItem.getCategoryId() != 3) {
						continue;
					}
					FlightOrderInsuranceVO flightOrderInsurance = new FlightOrderInsuranceVO();
					flightOrderInsurances.add(flightOrderInsurance);
					flightOrderInsurance.setOrderMainId(order.getOrderId());
					flightOrderInsurance.setOrderPassengerId(passenger.getId());
					flightOrderInsurance.setInsuranceOrderNo(String.valueOf(currOrderItem.getOrderItemId()));
					
					InsuranceClassVO insuranceClass = new InsuranceClassVO();
					flightOrderInsurance.setInsuranceClass(insuranceClass);
					insuranceClass.setPrice(new BigDecimal(currOrderItem.getPriceYuan()));
					insuranceClass.setDesc(currOrderItem.getProductName()
									+ "("
									+ currOrderItem
											.getContentStringByKey("branchName")
									+ "-"
									+ currOrderItem.getSuppGoodsName()
									+ ")");
				}
			}*/
		}
		
		return passengers;
	}
	
	
	/**
	 * 乘客类型转换
	 * @param passengerType
	 * @return
	 */
	private PassengerTypeVO transPassengerType(String passengerType) {
		if(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_ADULT.name().equalsIgnoreCase(passengerType)) {
			return PassengerTypeVO.ADULT;
		}
		
		if(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_CHILD.name().equalsIgnoreCase(passengerType)) {
			return PassengerTypeVO.CHILDREN;
		}
		
		return PassengerTypeVO.ALL;
	}
	
	/**
	 * 证件类型转换
	 * @param idCardType
	 * @return
	 */
	private IDCardTypeVO transIDCardType(String idCardType) {
		if(OrderEnum.ORDER_PERSON_ID_TYPE.ID_CARD.name().equalsIgnoreCase(idCardType)) {
			return IDCardTypeVO.ID;
		}
		
		if(OrderEnum.ORDER_PERSON_ID_TYPE.HUZHAO.name().equalsIgnoreCase(idCardType)) {
			return IDCardTypeVO.PASSPORT;
		}
		
		if(OrderEnum.ORDER_PERSON_ID_TYPE.JUNGUAN.name().equalsIgnoreCase(idCardType)) {
			return IDCardTypeVO.OFFICER;
		}
		
		if(OrderEnum.ORDER_PERSON_ID_TYPE.SHIBING.name().equalsIgnoreCase(idCardType)) {
			return IDCardTypeVO.SOLDIER;
		}
		
		if(OrderEnum.ORDER_PERSON_ID_TYPE.TAIBAOZHENG.name().equalsIgnoreCase(idCardType)) {
			return IDCardTypeVO.TAIBAO;
		}
		
		return IDCardTypeVO.OTHER;
	}
	
	/**
	 * 性别转换
	 * @param gender
	 * @return
	 */
	private GenderVO transGender(String gender) {
		if(OrderEnum.ORDER_PERSON_GENDER_TYPE.MAN.name().equalsIgnoreCase(gender)) {
			return GenderVO.MALE;
		}
		
		if(OrderEnum.ORDER_PERSON_GENDER_TYPE.WOMAN.name().equalsIgnoreCase(gender)) {
			return GenderVO.FEMALE;
		}
		
		return null;
	}
	
	
	/**
	 * 返回状态
	 * @param result
	 * @return
	 */
	/*
	private boolean isSuccess(ResultVO result) {
		if(result == null) {
			return false;
		}
		return "0".equals(result.getResultCode());
	}
	*/
	/**
	 * 返回信息
	 * @param result
	 * @return
	 */
	/*
	private String getResultMessage(ResultVO result) {
		if(result == null) {
			return null;
		}
		return result.getMessage();
	}
	*/
	
	/*
	 * 支付通知，出票通知
	 * @see com.lvmama.vst.back.client.ord.service.OrderFlightTicketService#paymentNotify(java.lang.Long)
	 */
	@Override
	public ResultHandle paymentNotify(Long orderItemId) throws Exception {
		ResultHandle resultHandle = new ResultHandle();
		
		// 子订单校验
		if (orderItemId == null) {
			resultHandle.setMsg("“出票通知”, 通知失败, 子订单ID为空");
			return resultHandle;
		}
		OrdOrderItem orderItem = orderUpdateService.getOrderItem(orderItemId);
		if (orderItem == null) {
			resultHandle.setMsg("“出票通知”, 通知失败, 找不到对应的子订单, 子订单ID: " + orderItemId);
			return resultHandle;
		}
		
		//防止重复请求--start
		/*if (isOrderItemNotified(orderItemId)) {
			//改子订单已经发送过出票通知
			resultHandle.setMsg("“出票通知”, 通知失败, 重复出票, 子订单ID: " + orderItemId);
			return resultHandle;
		}*/
		//防止重复请求--end
		
		// 封装请求
		List<FlightOrderPayRequestVO> flightOrderPayRequestList = new ArrayList<FlightOrderPayRequestVO>();
		
		// 订单支付信息
		List<PayAndPreVO> payAndPreVOList = payPaymentServiceAdapter.findPaymentInfo(orderItemId, OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name());
		if (CollectionUtils.isNotEmpty(payAndPreVOList)) {
			for (PayAndPreVO item : payAndPreVOList) {
				FlightOrderPayRequestVO flightOrderPayRequest = new FlightOrderPayRequestVO();
				flightOrderPayRequest.setVstFlightOrderId(orderItemId);
				flightOrderPayRequest.setVstMainOrderId(orderItem.getOrderId());
				flightOrderPayRequest.setPaymentSerialNumber(item.getSerial());
				flightOrderPayRequest.setPaymentType(item.getPaymentType());
				flightOrderPayRequest.setPayedAmount(item.getAmount().intValue());
				flightOrderPayRequest.setPayApplyTime(item.getCreateTime());
				flightOrderPayRequest.setPayedTime(item.getCallbackTime());
				
				flightOrderPayRequestList.add(flightOrderPayRequest);
			}
		} else {
			// 支付系统，查询不到支付信息，补偿一条信息（传递订单ID）
			FlightOrderPayRequestVO flightOrderPayRequest = new FlightOrderPayRequestVO();
			flightOrderPayRequest.setVstFlightOrderId(orderItemId);
			flightOrderPayRequest.setVstMainOrderId(orderItem.getOrderId());
			
			flightOrderPayRequestList.add(flightOrderPayRequest);
		}
		
		long start = System.currentTimeMillis();
		try {
			logger.info("“出票通知”参数：----" + JSONArray.fromObject(flightOrderPayRequestList).toString());
		} catch (Exception e) {
			logger.error("“出票通知”参数异常：----" + e);
		}
		
		// 重发
		int tryTime = 0;
		do {
			resultHandle = new ResultHandle();
			try {
				// 发送通知
				resultHandle = flightOrderService.payFlightOrder(flightOrderPayRequestList);
				
				if (resultHandle == null) {
					resultHandle = new ResultHandle();
					resultHandle.setMsg("“出票通知”, 通知失败, 调用无响应, 子订单ID: " + orderItemId);
				} 
			} catch (Exception e) {
				logger.info("{}", e);
				resultHandle.setMsg(e);
			}
			
			tryTime ++;
			if(resultHandle.isFail() && tryTime < MAX_TRY_TIMES) {
				Thread.sleep(100);
			}
		} while (resultHandle.isFail() && tryTime < MAX_TRY_TIMES);
		long end = System.currentTimeMillis();
		
		// 定时JOB触发:机票子订单,支付通知,重发逻辑
		/*
		if (resultHandle.isFail()) {
			List<ComJobConfig> jobConfigs = comJobConfigDAO.selectByObjectId(ComJobConfig.JOB_TYPE.FLIGHT_ORDER_PAYMENT_NOTIFY.name(), orderItem.getOrderItemId());
			if (CollectionUtils.isEmpty(jobConfigs)) {
				ComJobConfig jobConfig = new ComJobConfig();
				jobConfig.setObjectId(orderItem.getOrderItemId());
				jobConfig.setObjectType(ComJobConfig.OBJECT_TYPE.ORDER.name());
				jobConfig.setJobType(ComJobConfig.JOB_TYPE.FLIGHT_ORDER_PAYMENT_NOTIFY.name());
				jobConfig.setRetryCount(3L);
				jobConfig.setPlanTime(DateUtil.getDateAfterMinutes(1));
				jobConfig.setCreateTime(new Date());
				comJobConfigDAO.insert(jobConfig);
				logger.info("flightOrderPaymentNotifyTryExecutor init, objectId:" + orderItem.getOrderItemId());
			}
		}*/
		
		// 保存机票状态 
		OrdFlightTicketStatus ordFlightTicketStatus = new OrdFlightTicketStatus(); 
		ordFlightTicketStatus.setOrderItemId(orderItemId);
		if (resultHandle.isSuccess()) {
			ordFlightTicketStatus.setStatusCode(OrderEnum.ORD_FLIGHT_TICKET_STATUS.TICKET_PROCESSING.name()); 
		} else {
			ordFlightTicketStatus.setStatusCode(OrderEnum.ORD_FLIGHT_TICKET_STATUS.PAYMENT_NOTIFY_FAIL.name());
			//发送出票通知失败，生成预订通知
			this.saveReservation(orderItem.getOrderId(), orderItemId, OrderEnum.AUDIT_SUB_TYPE.FLIGHT_PAYMENT_NOTIFY_FAIL.name(), "机票发送支付通知失败，请及时进行后续人工处理！");
		}
		logger.info("FlightOrderProcessServiceImpl::paymentNotify_orderItemId=" + orderItemId + " ticketStatus=" + ordFlightTicketStatus.getStatusCode());
		ordFlightTicketStatusService.saveFlightTicketStatus(ordFlightTicketStatus);
		
		// 日志入库
		String logContent = "机票“发起出票通知”：子订单号[" + orderItemId + "]，调用结果：isSuccess[" + resultHandle.isSuccess() + "],msg:[" + resultHandle.getMsg() + "], cost:" + (end - start) + "毫秒";
		logger.info(logContent);
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
				orderItem.getOrderItemId(), 
				orderItem.getOrderItemId(), 
				"system", 
				logContent, 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName(),
				"");
		
		return resultHandle;
	}
	
	@Override
	public ResultHandle paymentNotifyByOrder(Long orderId) throws Exception {
		ResultHandle resultHandle = new ResultHandle();
		if (orderId == null) {
			resultHandle.setMsg("“出票通知”, 通知失败, 订单ID: " + orderId + "为空！");
			return resultHandle;
		}
		
		OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
		if (order == null) {
			resultHandle.setMsg("“出票通知”, 通知失败, ID: " + orderId + "的订单为空！");
			return resultHandle;
		}
		
		if (!order.hasPayed() || !order.hasResourceAmple()) {
			resultHandle.setMsg("“出票通知”, 通知失败, 主订单状态不对, 订单ID: " + orderId + " " +
					"order.hasPayed=" + order.hasPayed() + " order.hasResourceAmple()" + order.hasResourceAmple());
			return resultHandle;
		}
		
		List<OrdOrderItem> orderItems = orderUpdateService.queryOrderItemByOrderId(orderId);
		if (CollectionUtils.isEmpty(orderItems)) {
			resultHandle.setMsg("“出票通知”, 通知失败, 订单中没有子订单：orderId=" + orderId);
			return resultHandle;
		}
		
		order.setOrderItemList(orderItems);
		if (!order.isContainApiFlightTicket()) {
			resultHandle.setMsg("“出票通知”, 通知失败, 订单中没有对接机票子订单：orderId=" + orderId);
			return resultHandle;
		}
		
		final String key="VST_FLIGHT_PAYMENT_NOTIFY_ORDER_ID_" + orderId;
		try {//添加同步方法
			if (SynchronizedLock.isOnDoingMemCached(key)) {
				resultHandle.setMsg("该订单重复出票：orderId=" + orderId);
				return resultHandle;
			}
			for (OrdOrderItem ordOrderItem : orderItems) {
				if (ordOrderItem.isApiFlightTicket()) {
					resultHandle = this.paymentNotify(ordOrderItem.getOrderItemId());
					if (resultHandle.isFail()) {
						//break;
						//临时处理，PM要求，便于补单
						logger.error("call payFlightOrder failed, msg:" + resultHandle.getMsg());
					}
				}
			}
		} finally {
			SynchronizedLock.releaseMemCached(key);
		}
		return resultHandle;
	}
	
	/**
	 * 判断子订单是否发送过出票通知
	 * @param orderItemId
	 * @return
	 */
	private boolean isOrderItemNotified(Long orderItemId) {
		Map<String, Object> flightStatusParams = new HashMap<String, Object>();
		flightStatusParams.put("orderItemId", orderItemId);
		List<OrdFlightTicketStatus> ordFlightTicketStatusList = ordFlightTicketStatusService.findByCondition(flightStatusParams);
		// 出票中和出票成功说明该子订单已经成功出过票了，跳过，防止重复出票
		if (CollectionUtils.isNotEmpty(ordFlightTicketStatusList)) {
			for (OrdFlightTicketStatus ordFlightTicketStatus : ordFlightTicketStatusList) {
				if (OrderEnum.ORD_FLIGHT_TICKET_STATUS.TICKET_PROCESSING.getCode().equals(ordFlightTicketStatus.getStatusCode())
						|| OrderEnum.ORD_FLIGHT_TICKET_STATUS.TICKET_SUCCESS.getCode().equals(ordFlightTicketStatus.getStatusCode())) {
					logger.info("该子订单已经出过票：orderItemId=" + orderItemId + ", statusCode=" + ordFlightTicketStatusList.get(0).getStatusCode());
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * 订单取消通知
	 * @see com.lvmama.vst.back.client.ord.service.OrderFlightTicketService#cancelOrderNotify(java.lang.Long)
	 */
	@Override
	public ResultHandle cancelOrderNotify(Long orderItemId) {
		ResultHandle resultHandle = new ResultHandle();
		
		// 子订单校验
		if (orderItemId == null) {
			resultHandle.setMsg("“订单取消”, 通知失败, 子订单ID为空");
			return resultHandle;
		}
		OrdOrderItem orderItem = orderUpdateService.getOrderItem(orderItemId);
		if (orderItem == null) {
			resultHandle.setMsg("“订单取消”, 通知失败, 找不到对应的子订单, 子订单ID: " + orderItemId);
			return resultHandle;
		}
			
		// 封装请求
		FlightOrderCancelRequestVO orderRequest = new FlightOrderCancelRequestVO();
		orderRequest.setVstFlightOrderId(orderItemId);
		orderRequest.setVstMainOrderId(orderItem.getOrderId());
		
		long start = System.currentTimeMillis();
		try {
			logger.info("“订单取消”参数：----" + JSONObject.fromObject(orderRequest).toString());
		} catch (Exception e) {
			logger.error("“订单取消”参数异常：----" + e);
		}
		
		try {
			// 发送通知
			resultHandle =  flightOrderService.cancelFlightOrder(orderRequest);
			
			if (resultHandle == null) {
				resultHandle = new ResultHandle();
				resultHandle.setMsg("“订单取消”, 通知失败, 调用无响应, 子订单ID: " + orderItemId);
			} 
		} catch (Exception e) {
			logger.info("{}", e);
			resultHandle.setMsg(e);
		}
		
		long end = System.currentTimeMillis();
		
		// 日志入库
		String logContent = "机票“订单取消通知”：子订单号[" + orderItemId + "]，调用结果：isSuccess[" + resultHandle.isSuccess() + "],msg:[" + resultHandle.getMsg() + "], cost:" + (end-start) +"毫秒";
		logger.info(logContent);
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
				orderItem.getOrderItemId(), 
				orderItem.getOrderItemId(), 
				"system", 
				logContent, 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName(),
				"");
		
		return resultHandle;
	}

	@Override
	public boolean isNeedCancelNotify(Long orderItemId) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		conditions.put("orderItemId", orderItemId);
		List<OrdFlightTicketStatus> statusList = ordFlightTicketStatusService
				.findByCondition(conditions);
		if (statusList != null && !statusList.isEmpty()) {
			String lockSeatStatus = statusList.get(0).getStatusCode();
			if (OrderEnum.ORD_FLIGHT_TICKET_STATUS.LOCK_SUCCESS.name().equals(
					lockSeatStatus)
					|| OrderEnum.ORD_FLIGHT_TICKET_STATUS.TICKET_PROCESSING
							.name().equals(lockSeatStatus)
					|| OrderEnum.ORD_FLIGHT_TICKET_STATUS.TICKET_SUCCESS.name()
							.equals(lockSeatStatus)) {
				return true;
			}
		}
		return false;
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
//		String messageContent="机票发送支付通知失败，请及时进行后续人工处理！";
		
		ComMessage comMessage=new ComMessage();
		comMessage.setMessageContent(messageContent);
		comMessage.setReceiver(receiver);
		
		return comMessageService.saveReservationChildOrder(comMessage, null, subType,
				orderId, orderItemId, "SYSTEM", messageContent);
	}

	@Override
	public ResultHandle lockSeatMultForTrafficX(OrdOrder order,List<OrdOrderItem> apiFlightList) throws Exception {
		ResultHandle result = new ResultHandle();
		if(CollectionUtils.isEmpty(apiFlightList)) {
			logger.error("apiFlightList is empty");
			result.setMsg("锁舱通知失败, 机票子订单不存在");
			return result;
		}
		List<FitFliBookingCallBackRequest> callBackRequests=new ArrayList<FitFliBookingCallBackRequest>();
		FitFliBookingCallBackRequest fc=null;
		for(OrdOrderItem item : apiFlightList){
			fc=new FitFliBookingCallBackRequest();
			logger.info("fitFlightBookingCallBackService.flightBookingCallBack:"+item.getOrderId()+",子单:"+item.getOrderItemId());
			fc.setVstOrderMainNo(item.getOrderId());
			fc.setVstOrderNo(item.getOrderItemId());
			callBackRequests.add(fc);
		}
		
		BaseResponseVSTDto<FitFliCallBackResponseVSTDto> resultHandle=null;
		try{
			resultHandle=fitFlightBookingCallBackService.flightBookingCallBack(callBackRequests);
			boolean tempFlag=false;
			if(resultHandle!=null){
					for (FitFliCallBackResponseVSTDto vstDto:resultHandle.getResults()) {
						logger.info("fitFlightBookingCallBackService.flightBookingCallBack:"+vstDto.getCallBackRequest().getVstOrderMainNo()+",子单:"+vstDto.getCallBackRequest().getVstOrderNo()+",状态:"+vstDto.isSuccessFlag());
					}
					String logContent ="==fitFlightBookingCallBackService.flightBookingCallBack==锁舱通知结果：主订单ID[" + order.getOrderId() + "]"+"[发送锁舱通知:"+ (resultHandle.isSuccess() ? "成功" : "失败")+"]";
					// 记录锁仓log
					logger.info(logContent);
					if(resultHandle.isSuccess())tempFlag=true;
				
			}
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					order.getOrderId(),
					order.getOrderId(),
					"SYSTEM",
					"[锁舱通知结果：主订单ID[" + order.getOrderId() + "]"+"[发送锁舱通知:"+ (tempFlag ? "成功" : "失败")+"]",
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"主订单["+order.getOrderId()+"]发送锁舱通知" + (tempFlag ? "成功" : "失败") + "]",
					"");
			
			for (OrdOrderItem orderItem : apiFlightList) {
				lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
						orderItem.getOrderItemId(), 
						orderItem.getOrderItemId(), 
						"system", 
						"已发起锁仓，等待对接系统回调", 
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName(),
						"");
			}
		}catch(Exception e){
			logger.info("=====orderId:"+order.getOrderId()+"===e:"+e.getMessage());
			result.setMsg(e);
			return result;
		}
		return result;
	}

	//前置锁仓
	@Override
	public ResultHandle preLockSeat(OrdOrder order,
			List<OrdOrderItem> apiFlightList) {
		ResultHandle resultHandle = new ResultHandle();
		//组装请求信息-list
		List<FlightOrderBookingRequestVO> orderBookingRequestList = new ArrayList<FlightOrderBookingRequestVO>();
		for (OrdOrderItem item : apiFlightList) {
			FlightOrderBookingRequestVO orderBookingRequest = buildFlightBookingRequestForPreLockSeat(item, order);
			orderBookingRequest.setNoticeVst(false);
			orderBookingRequestList.add(orderBookingRequest);
		}
		
		try {
			logger.info("前置锁舱参数：----" + JSONArray.fromObject(orderBookingRequestList).toString());
		} catch (Exception e) {
			logger.error("orderBookingRequest----" + e.getMessage());
		}
		
		long start = System.currentTimeMillis();
		resultHandle = new ResultHandle();
		try {
			//调用机票系统的锁舱接口
			resultHandle = flightOrderService.bookingOrderAsync(orderBookingRequestList);
		} catch (Exception e) {
			logger.info("{前置锁仓发起锁仓失败!}", e);
			resultHandle.setMsg(e);
		}
		
		if (resultHandle == null) {
			resultHandle = new ResultHandle();
			resultHandle.setMsg("前置锁舱通知失败, 调用无响应, 主订单ID: " + order.getOrderId());
		} 
		
		long end = System.currentTimeMillis();
		String logContent = "前置锁舱通知结果：主订单ID[" + order.getOrderId() + "]，调用结果：isSuccess[" + resultHandle.isSuccess() + "],msg:[" + resultHandle.getMsg() + "], cost:" + (end-start) +"毫秒";
		logger.info(logContent);
		return resultHandle;
	}
	
	//锁仓前置对接机票出票
	@Override
	public ResultHandle paymentNotifyByOrderForPreLockSeat(Long orderId) throws Exception {
		logger.info("paymentNotifyByOrderForPreLockSeat start");
		ResultHandle resultHandle = new ResultHandle();
		if (orderId == null) {
			resultHandle.setMsg("“出票通知”, 通知失败, 订单ID: " + orderId + "为空！");
			return resultHandle;
		}
		
		OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
		if (order == null) {
			resultHandle.setMsg("“出票通知”, 通知失败, ID: " + orderId + "的订单为空！");
			return resultHandle;
		}
		
		if (!order.hasPayed()) {
			resultHandle.setMsg("“出票通知”, 通知失败, 主订单状态不对, 订单ID: " + orderId + " " +
					"order.hasPayed=" + order.hasPayed() + " order.hasResourceAmple()" + order.hasResourceAmple());
			return resultHandle;
		}
		
		List<OrdOrderItem> orderItems = orderUpdateService.queryOrderItemByOrderId(orderId);
		if (CollectionUtils.isEmpty(orderItems)) {
			resultHandle.setMsg("“出票通知”, 通知失败, 订单中没有子订单：orderId=" + orderId);
			return resultHandle;
		}
		
		order.setOrderItemList(orderItems);
		if (!order.isContainApiFlightTicket()) {
			resultHandle.setMsg("“出票通知”, 通知失败, 订单中没有对接机票子订单：orderId=" + orderId);
			return resultHandle;
		}
		
		final String key="VST_FLIGHT_PAYMENT_NOTIFY_ORDER_ID_" + orderId;
		try {//添加同步方法
			if (SynchronizedLock.isOnDoingMemCached(key)) {
				resultHandle.setMsg("该订单重复出票：orderId=" + orderId);
				return resultHandle;
			}
			for (OrdOrderItem ordOrderItem : orderItems) {
				if (ordOrderItem.isApiFlightTicket()) {
					resultHandle = this.paymentNotify(ordOrderItem.getOrderItemId());
					if (resultHandle.isFail()) {
						//break;
						//临时处理，PM要求，便于补单
						logger.error("call payFlightOrder failed, msg:" + resultHandle.getMsg());
					}
				}
			}
		} finally {
			SynchronizedLock.releaseMemCached(key);
		}
		return resultHandle;
	}
}
