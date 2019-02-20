package com.lvmama.vst.order.web;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.order.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.lvmama.comm.pet.po.pay.PayPayment;
//import com.lvmama.comm.vo.Constant.ORDITEM_PRICE_CONFIRM_STATUS;
import com.lvmama.finance.comm.finance.po.SetSettlementItem;
import com.lvmama.finance.comm.vst.service.SettlementService;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.pub.service.ComLogClientService;
import com.lvmama.vst.back.order.po.OrdOrderDownpay.PAY_STATUS;
import com.lvmama.vst.back.order.po.OrdOrderDownpay.PAY_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.pet.adapter.IPayPaymentServiceAdapter;
import com.lvmama.vst.pet.adapter.OrderRefundmentServiceAdapter;
import com.lvmama.vst.pet.vo.PayAndPreVO;

/**
 * 订单支付信息业务
 * 
 * @author wenzhengtao
 * 
 */
@Controller
public class OrderPaymentAction extends BaseActionSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5506651839676717984L;
	// 记录日志
	private static final Log LOGGER = LogFactory.getLog(OrderPaymentAction.class);
	// 查看支付记录
	private static final String ORDER_PAYMENT_INFO_PAGE = "/order/orderPayment/viewOrderPaymentInfo";
	// 注入支付业务
	@Autowired
	private IPayPaymentServiceAdapter payPaymentServiceAdapter;

	@Autowired
	private IOrderLocalService orderService;

	@Autowired
	private IOrderUpdateService orderUpdateService;

	@Autowired
	private ComLogClientService comLogClientService;

	@Autowired
	private IOrderAmountChangeService orderAmountChangeService;
	
	@Autowired
	private OrdPayPromotionService ordPayPromotionService;
	

	@Autowired
	private IOrderLocalService orderLocalService;
	
	//结算状态改造 从支付获取
	@Autowired
	private SettlementService settlementService;
	
	@Autowired
	private IOrdOrderItemService ordOrderItemService;

	@Autowired
	private OrdDepositRefundAuditService ordDepositRefundAuditService;
	
	@RequestMapping(value = "/ord/order/showOrdPayment")
	public String showOrdPayment(Model model, HttpServletRequest request) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("start method<showOrdPayment>");
		}

		return "/order/orderPayment/ordPayment";
	}

	@RequestMapping(value = "/ord/order/ordPayment")
	@ResponseBody
	public Object ordPayment(HttpServletRequest request, ComMessage comMessage) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("start method<ordPayment>");
		}
		String orderId = request.getParameter("orderId");
		String orderRemark = request.getParameter("orderRemark");

		String paymentId = request.getParameter("paymentId");

		PayPayment payPayment = payPaymentServiceAdapter.selectByPaymentId(Long.parseLong(paymentId));
		payPayment.setStatus(Constant.PAYMENT_SERIAL_STATUS.SUCCESS.name());
		boolean result = payPaymentServiceAdapter.updatePayment(payPayment);
		ResultHandle resultHandle = orderService.paymentSuccess(payPayment);
		if (result && resultHandle.isSuccess()) {
			return ResultMessage.UPDATE_SUCCESS_RESULT;
		} else {
			return ResultMessage.ERROR;
		}

	}

	/**
	 * 查看订单的支付记录
	 * 
	 * @param model
	 * @param userId
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/viewOrderPaymentInfo.do")
	public String viewOrderPaymentInfo(Model model, Long orderId) throws BusinessException {

		OrdOrder order = orderService.queryOrdorderByOrderId(orderId);
		String bizType = OrderEnum.PAYMENT_BIZ_TYPE.VST_ORDER.name();


		  if(Constant.DIST_GROUP_PURCHASE==order.getDistributorId()){
			bizType=OrderEnum.PAYMENT_BIZ_TYPE.GROUP_PURCHASE_ORDER.name();
		  }


		/*
		 * if(Constant.DIST_O2O_SELL==order.getDistributorId()){//门店的渠道的bizType=
		 * "O2O_ORDER" bizType="O2O_ORDER"; }
		 */
		// 执行查询
		List<PayAndPreVO> payAndPreVOList = payPaymentServiceAdapter.findPaymentInfo(orderId, bizType);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(payAndPreVOList);
		}
		String paymentInfoFlag="N";
		List<OrdOrderDownpay> ordList= ordPayPromotionService.queryOrderDownpayByOrderId(orderId);
		//设置了定金支付，且不是全额支付,出境线路产品
		if(CollectionUtils.isNotEmpty(ordList)){
			if(PAY_TYPE.PART.toString().equals(ordList.get(0).getPayType()) && CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(order.getBuCode())
					&& com.lvmama.comm.pay.vo.Constant.PAYMENT_GATEWAY_DIST_MANUAL.DISTRIBUTOR_B2B.getCode().equals(order.getDistributorCode()) &&
					(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId()) ||
							BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId()) ||
							BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getCategoryId()) ||
							BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId()))
					){
				
				paymentInfoFlag="Y";
			}
		}
		
		model.addAttribute("paymentInfoFlag", paymentInfoFlag);
		// 保存结果
		model.addAttribute("payAndPreVOList", payAndPreVOList);
		model.addAttribute("orderId", orderId);
		// 把categoryId转换成String类型
		model.addAttribute("orderCategoryId", String.valueOf(order.getCategoryId()));

		// 目的地订单无需资源审核
		if (OrdOrderUtils.isDestBuFrontOrder(order)) {
			model.addAttribute("isDestBuFront", "Y");
		} else if(OrdOrderUtils.isLocalBuFrontOrder(order)){
			//国内借用目的地订单判断条件，修改时请注意
			model.addAttribute("isDestBuFront", "Y");
		}else {
			model.addAttribute("isDestBuFront", "N");
		}
		LOGGER.info("/ord/order/viewOrderPaymentInfo.do====isDestBuFront" + OrdOrderUtils.isDestBuFrontOrder(order));

		LOGGER.info("/ord/order/viewOrderPaymentInfo.do====" + String.valueOf(order.getCategoryId()));
		model.addAttribute("order", order);
		// 跳转页面
		return ORDER_PAYMENT_INFO_PAGE;
	}

	@RequestMapping("/ord/order/validateApprovingAmountChange.do")
	@ResponseBody
	public Object validateApprovingAmountChange(Long orderId) throws Exception {
		ResultMessage msg = ResultMessage.createResultMessage();
		OrdOrder order = orderService.queryOrdorderByOrderId(orderId);
		if (OrderEnum.PAYMENT_STATUS.UNPAY.name().equals(order.getPaymentStatus())) {
			Integer recordsCount = orderAmountChangeService.queryApprovingRecords(orderId);
			if (recordsCount == null) {
				msg.raise("查询正在审核中的价格修改记录时发生异常");
			} else {
				msg.addObject("valid", recordsCount <= 0);
			}
		} else {
			msg.addObject("valid", true);
		}

		return msg;
	}

	//批量获取结算状态
	public List<SetSettlementItem> getSetSettlementItem(List<OrdOrderItem> orderItemList){
		List<SetSettlementItem> setSettlementItems	 = new ArrayList<SetSettlementItem>();
		try {
				List<Long> itemIds = new ArrayList<Long>();
					for (OrdOrderItem ordOrderItem : orderItemList) {
						itemIds.add(ordOrderItem.getOrderItemId());
					}
					setSettlementItems  = settlementService.searchSetSettlementItemByOrderItemIds(itemIds);
		} catch (Exception e) {
			throw new RuntimeException("调用支付接口获取结算状态异常---"+e.getMessage());
		}
			
		return setSettlementItems;
	}

		
	public String getSettlementStatus(List<SetSettlementItem> setSettlementItems,Long orderItemId){
		if(null!=setSettlementItems && setSettlementItems.size()>0){
			for(int i=0;i<setSettlementItems.size();i++){
				if(setSettlementItems.get(i).getOrderItemMetaId().equals(orderItemId)){
					return setSettlementItems.get(i).getSettlementStatus();
				}
			}
		}
		return OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.name();
	}
		
	@RequestMapping("/ord/order/transferOrderAmount.do")
	@ResponseBody
	public Object transferOrder(OrdOrder order) {
		ResultMessage message = ResultMessage.createResultMessage();
		if (order.getOriOrderId() == null || order.getOrderId() == null) {
			message.raise("订单信息不存在");
			return message;
		}

		try {
			OrdOrder oriOrder = orderUpdateService.queryOrdOrderByOrderId(order.getOriOrderId());
			if (oriOrder == null) {
				throw new IllegalArgumentException("转出订单不存在");
			}

			//原订单产品经理审批未通过不做资金转移
//			if (CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(oriOrder.getBuCode()) &&
//					com.lvmama.comm.pay.vo.Constant.PAYMENT_GATEWAY_DIST_MANUAL.DISTRIBUTOR_B2B.getCode().equals(oriOrder.getDistributorCode()) &&
//					(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(oriOrder.getCategoryId()) ||
//							BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(oriOrder.getCategoryId()) ||
//							BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(oriOrder.getCategoryId()) ||
//							BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(oriOrder.getCategoryId()) ||
//							BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(oriOrder.getCategoryId()))) {
//
//				List<OrdOrderDownpay> ordList = ordPayPromotionService.queryOrderDownpayByOrderId(oriOrder.getOrderId());
//				if (CollectionUtils.isNotEmpty(ordList)) {
//					OrdOrderDownpay ordDownpay = ordList.get(0);
//					if (PAY_TYPE.PART.toString().equals(ordDownpay.getPayType()) || (PAY_TYPE.FULL.toString().equals(ordDownpay.getPayType()) && PAY_STATUS.UNPAY.toString().equals(ordDownpay.getPayStatus()))) {
//						int num = ordDepositRefundAuditService.findCount(new OrdDepositRefundAudit(order.getOriOrderId() , OrderEnum.ORD_DEPOSIT_REFUND_AUDIT.TRANSFER.name() , OrderEnum.ORD_DEPOSIT_REFUND_AUDIT.PASS.name(), null));
//						if (num == 0) {
//							throw new IllegalArgumentException("原订单为定金支付订单，产品经理未同意资金转移，请联系产品经理，谢谢！");
//						}
//					}
//				}
//			}

			if(oriOrder.getOrderSubType() != null && ("STAMP".equalsIgnoreCase(oriOrder.getOrderSubType()) || "STAMP_PROD".equalsIgnoreCase(oriOrder.getOrderSubType()))){
				throw new IllegalArgumentException("预售券订单/预售券兑换产品订单不支持资金转移");
			}
			
			if (!oriOrder.isCancel()) {
				throw new IllegalArgumentException("转出订单不是取消状态");
			}

			if (!(OrderEnum.PAYMENT_STATUS.PAYED.name().equals(oriOrder.getPaymentStatus()) || OrderEnum.PAYMENT_STATUS.PART_PAY.name()
					.equals(oriOrder.getPaymentStatus()))) {
				throw new IllegalArgumentException("当前订单不存在可转出的资金");
			}

			OrdOrder targetOrder = orderUpdateService.queryOrdOrderByOrderId(order.getOrderId());
			if (targetOrder == null) {
				throw new IllegalArgumentException("接收转入的订单不存在");
			}
			if (!targetOrder.hasNeedPrepaid()) {
				throw new IllegalArgumentException("只有预付订单才可以转入");
			}
			if (targetOrder.isCancel()) {
				throw new IllegalArgumentException("转入订单已经取消");
			}

			if (targetOrder.getOriOrderId() != null) {
				throw new IllegalArgumentException("当前订单已经与另外的订单绑定关系，不可以转入新的订单资金");
			}

			if (!oriOrder.getUserNo().equals(targetOrder.getUserNo())) {
				throw new IllegalArgumentException("订单不属于同一个用户不可以转入");
			}

			if (orderRefundmentServiceAdapter.hasValidOrdRefundmentByOrderId(oriOrder.getOrderId())) {
				throw new IllegalArgumentException("转出订单存在有效的退款不可以转入");
			}

			if(targetOrder.getOrderSubType() != null && ("STAMP".equalsIgnoreCase(targetOrder.getOrderSubType()) || "STAMP_PROD".equalsIgnoreCase(targetOrder.getOrderSubType()))){
				throw new IllegalArgumentException("预售券订单/预售券兑换产品订单不支持资金转移");
			}
			
			List<OrdOrderItem> list = orderUpdateService.queryOrderItemByOrderId(oriOrder.getOrderId());
			//支付获取结算状态
			List<SetSettlementItem> setSettlementItems =  getSetSettlementItem(list);
			
			
				if (CollectionUtils.isNotEmpty(list)) {
					for (OrdOrderItem orderItem : list) {
						if (!(StringUtils.equals(OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.name(), getSettlementStatus(setSettlementItems, orderItem.getOrderItemId())) || StringUtils.equals(OrderEnum.ORDER_SETTLEMENT_STATUS.NOSETTLEMENT.name(), getSettlementStatus(setSettlementItems, orderItem.getOrderItemId())))) {
							throw new IllegalArgumentException("订单存在非”未结算、不结算“的订单项");
						}	
						
					}
				}			
			
			targetOrder.setOriOrderId(oriOrder.getOrderId());
			int len = orderUpdateService.updateOrderAndChangeOrderItemPayment(targetOrder);
			if (len == 0) {
				throw new IllegalArgumentException("更新绑定关系失败");
			}

			payPaymentServiceAdapter.transferVstOrder(oriOrder.getOrderId(), targetOrder.getOrderId());
			//将源订单的子订单价格确认状态改为未确认
			ordOrderItemService.updatePriceConfirmStatusByOrderId(oriOrder.getOrderId(),ORDITEM_PRICE_CONFIRM_STATUS.UN_CONFIRMED.getCode());
			
			comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER, targetOrder.getOrderId(), targetOrder.getOrderId(),
					getLoginUserId(), "操作资金转移到当前订单", ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCode(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName(), "");
		} catch (Exception ex) {
			message.raise(ex.getMessage());
		}
		return message;
	}

	/**
	 * 定金支付跳转
	 */
	@RequestMapping("/ord/order/editPaymentTerm.do")
	public String editPaymentTerm(Model model, Long orderId,String oughtAmount) throws Exception {
			List<OrdOrderDownpay> ordList= ordPayPromotionService.queryOrderDownpayByOrderId(orderId);
			OrdOrderDownpay ord=null;
			if(CollectionUtils.isNotEmpty(ordList)){
				ord = ordList.get(0);
			}
			model.addAttribute("oughtAmount",oughtAmount);
			model.addAttribute("orderDownpay", ord);
			model.addAttribute("orderId",orderId);
		return "/order/orderPayment/editOrderPaymentTerm";
	}
	
	/**
	 * 定金支付save or update
	 * 
	 */
	@RequestMapping("/ord/order/saveOrUpdatePaymentManner.do")
	@ResponseBody
	public Object saveOrUpdatePaymentManner(OrdOrderDownpay ordOrderDownpay,HttpServletRequest request,HttpServletResponse response) throws Exception {
		LOGGER.info("BEGIN -----saveOrUpdatePaymentManner----");
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			if(ordOrderDownpay != null){
				List<OrdOrderDownpay> ordList= ordPayPromotionService.queryOrderDownpayByOrderId(ordOrderDownpay.getOrderId());
				if(CollectionUtils.isEmpty(ordList)){
					ordOrderDownpay.setPayStatus(PAY_STATUS.UNPAY.toString());
					int save = ordPayPromotionService.saveOrderDownpay(ordOrderDownpay);
					if(save == 0){
						msg.raise("定金支付失败！");
						return msg;
					}
				}else{
					OrdOrderDownpay ord=ordList.get(0);
					ordOrderDownpay.setPayStatus(ord.getPayStatus());
				    int update=  ordPayPromotionService.updateByPrimaryKeyOrderId(ordOrderDownpay);
				    if(update == 0){
						msg.raise("定金支付失败！");
						return msg;
					}
				}
				String jsonStr = JSONObject.toJSONString(ordOrderDownpay);
				String payStatus =PAY_STATUS.UNPAY.toString().equals(ordOrderDownpay.getPayStatus()) ? PAY_STATUS.UNPAY.getCnName() : PAY_STATUS.PAYED.getCnName();
				String payType = PAY_TYPE.FULL.toString().equals(ordOrderDownpay.getPayType()) ? PAY_TYPE.FULL.getCnName() : PAY_TYPE.PART.getCnName();
				orderLocalService.newOrdOrderDownpayMessage(ordOrderDownpay.getOrderId(),jsonStr);
				msg.addObject("payType", payType);
				msg.addObject("payStatus", payStatus);
				msg.addObject("payAmount", ordOrderDownpay.getPayAmount());
				double payAmount= (double)ordOrderDownpay.getPayAmount()/100;  
				DecimalFormat df = new DecimalFormat("0.00");  
				String formatPay = df.format(payAmount);
				comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER, ordOrderDownpay.getOrderId(), ordOrderDownpay.getOrderId(),
						getLoginUserId(), "设置支付方式为"+payType+","+payType+"金额"+formatPay+"元", ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_PAYMENT_MANNER.getCode(),
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_PAYMENT_MANNER.getCnName(), "");
			}
		} catch (Exception e) {
			msg.raise(e.getMessage());
		}
		LOGGER.info("END -----saveOrUpdatePaymentManner----");
		return msg;
	}
	
	
	@Autowired
	private OrderRefundmentServiceAdapter orderRefundmentServiceAdapter;
}
