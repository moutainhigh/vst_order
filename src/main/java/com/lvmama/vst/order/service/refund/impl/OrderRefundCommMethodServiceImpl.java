package com.lvmama.vst.order.service.refund.impl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.comm.bee.po.ord.OrdRefundApply;
import com.lvmama.comm.vst.VstOrderEnum;
import com.lvmama.vst.api.vo.ResultHandleT;
import com.lvmama.vst.back.client.prom.service.SyncExecutePetService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.order.service.impl.PartRefundServiceImpl;
import com.lvmama.vst.order.service.refund.IOrderRefundCommMethodService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.web.OrderDetailAction;
import com.lvmama.vst.pet.adapter.OrdRefundApplyServiceAdapter;
/**
 * @author huangxin
 * 退款单公共的方法
 */

@Service("orderRefundCommMethodService")
public class OrderRefundCommMethodServiceImpl implements IOrderRefundCommMethodService {
	
	private static final Logger LOG = LoggerFactory.getLogger(OrderRefundCommMethodServiceImpl.class);
	
	@Resource(name="ordRefundApplyServiceRemote")
	private OrdRefundApplyServiceAdapter ordRefundApplyServiceRemote;
	
	@Resource(name="orderDetailAction")
	private OrderDetailAction orderDetailAction;
	
	/*
	  * 是否显示退款申请按钮(1.订单为门票类型 2.订单已支付（指全部支付）3.订单中无投诉退款单 满足这两个条件才能显示退款申请按钮)
	  */
	public String checkReFundButtonShow(OrdOrder order){
		LOG.info("checkReFundButtonShow log start, order id:"+order.getOrderId());
		// 1.判断订单是门票类型
		Long categoryId = order.getCategoryId();
		if(!OrderUtils.isTicketByCategoryId(categoryId)){
			return "订单不是门票类型。";
		}
		LOG.info("checkReFundButtonShow isTicket:" + true);
		//2.判断订单来源是否是分销
		if(order.getDistributorId() == 4){
			return "此订单为分销订单。";
		}
		// 3.判断订单是否已支付
		Boolean isPayed = VstOrderEnum.PAYMENT_STATUS.PAYED.name().equalsIgnoreCase(order.getPaymentStatus());
		if(!isPayed){
			return "订单未支付。";
		}
		LOG.info("checkReFundButtonShow isPayed:" + true);
		// 4.判断订单中有无投诉退款单
		Boolean isComplaintRefund = ordRefundApplyServiceRemote.isComplaintRefundment(order.getOrderId());
		if(isComplaintRefund){
			return "订单中有投诉退款单。";
		}
		LOG.info("checkReFundButtonShow no ComplaintRefund:" + true);
		// 5.判断订单金额是否有改动
		Long amountChange = orderDetailAction.getTotalAmountChange(order.getOrderId(),"ORDER",null);
		if(amountChange != null && amountChange.longValue() != 0L){
			return "订单金额有改动。";
		}
		LOG.info("checkReFundButtonShow no amountChange:" + true);
		//6.判断订单是否有紧急入园售后单
		Boolean hasUrgencySale = ordRefundApplyServiceRemote.hasUrgencySale(order.getOrderId());
		if(hasUrgencySale){
			return "订单含有紧急入园售后单。";
		}
		LOG.info("checkReFundButtonShow no UrgencySale:" + true);
		
		//7.判断订单是否发起过整单退(老入口)退款申请  
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("orderId", order.getOrderId());
		List<OrdRefundApply>  ordRefundApplys = ordRefundApplyServiceRemote.queryRefundApplyByParam(params);
		if(ordRefundApplys !=null && !ordRefundApplys.isEmpty()){			
			for(OrdRefundApply apply:ordRefundApplys){
				if(!"Y".equals(apply.getPartRefundFlag())){
					return "此订单发起过老入口整单退退款申请。";
				}
			}
		}
		LOG.info("checkReFundButtonShow no old refundApply:" + true);
		return "success";
	}

}
