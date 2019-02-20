package com.lvmama.vst.order.service.refund.impl.front;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.comm.utils.DateUtil;
import com.lvmama.comm.vo.Constant;
import com.lvmama.vst.api.vo.prod.SuppGoodsBaseTimePriceVo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.order.service.refund.IOrderRefundFrontService;
import com.lvmama.vst.pet.adapter.refund.vo.OrderRefundConstant;
import com.lvmama.vst.pet.adapter.refund.vo.OrderRefundDetailVO;

/**
 * 单酒
 * @author chenhao
 *
 */
@Service("orderRefundHotelFrontService")
public class OrderRefundHotelFrontServiceImpl implements IOrderRefundFrontService {
	private static final Log LOG = LogFactory.getLog(OrderRefundHotelFrontServiceImpl.class);
	@Autowired
	private OrderRefundComFrontService orderRefundComFrontService;
	
	/**
     * 获取订单申请退款的状态，主要是用来判断退款申请按钮漏出逻辑
     *
     * @param vstOrdOrderVo 订单对象
     * @return 退款按钮逻辑状态
     * @author Zhang.Wei
     * @update Chen.Hao
     * @time 2015.12.30
     * @email zhangwei@lvmama.com
     */
    public String getOrderRefundApplyStatus(OrdOrder order, String refundApplyStatus) {
    	Long orderId =order.getOrderId();
    	String cancelStrategy = "";
    	if(order.isHotel()){
    		cancelStrategy = order.getHotelCancelStrategy();//退改类型
    	}else{
    		cancelStrategy = order.getCancelStrategy();
    	}
        String paymentTarget = order.getPaymentTarget();//支付方式
        String paymentStatus = order.getPaymentStatus();//支付状态
        String paymentType = order.getPaymentType();//预定限制类型
        String guarantee = order.getGuarantee();//是否需要担保 @see com.lvmama.vst.back.order.po.OrderEnum.CREDIT_CARDER_GUARANTEE
        Date visitTime = order.getVisitTime();//入住日期
        String orderStatus = order.getOrderStatus();//订单状态
        String deductType = "";//扣款类型
        if(order.getMainOrderItem() != null ){
        	deductType = order.getMainOrderItem().getDeductType();
        }
        Date nowTime = new Date();

        //判断是否为目的地在线退款
        LOG.info("destBU online refund getOrderRefundApplyStatus ,orderId:"+orderId+" isDestBU :"+order.isDestBuRefund()+",cancelStrategy:"+cancelStrategy+",paymentTarget:"+paymentTarget+",paymentStatus:"+paymentStatus+" ,paymentType:"+paymentType+" ,guarantee :"+guarantee+" ,visitTime:"+visitTime+" ,orderStatus:"+orderStatus+"");
        if (!order.isDestBuRefund()) {
            return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.CANNOT_REFUND.getCode();
        }
        
        if (SuppGoodsBaseTimePriceVo.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(cancelStrategy) || SuppGoodsBaseTimePriceVo.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(cancelStrategy)) {//可退改or 景+酒品类人工处理
            if (OrderRefundConstant.PAYTARGET.PREPAID.getCode().equals(paymentTarget)) {//预付
                if (OrderRefundConstant.ORDER_VIEW_STATUS.UNPAY.getCode().equals(paymentStatus)) {//待支付
                    return orderRefundComFrontService.getHotelOrderCancelStatus(orderId, orderStatus);

                } else if (OrderRefundConstant.ORDER_VIEW_STATUS.PAYED.getCode().equals(paymentStatus)) {//已支付
                	//未设置扣款类型，订单不退不改
                    if(order.isHotel() && StringUtils.isEmpty(deductType)){
                    	return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.CANNOT_CHANGE_ORDER.getCode();
                    }

                    //  --------------根据orderId查询refund_apply表是否有退款申请记录
                    Map<String, Object> paramMap = new HashMap<String, Object>();
                    paramMap.put("orderId", orderId);
                    //List<VstOrderRefundApply> refundApplyList = vstOrderRefundApplyService.queryRefundApplyByParam(paramMap);

                    if (!StringUtil.isEmptyString(refundApplyStatus)) {//如有退款单记录
                        if (OrderRefundConstant.REFUNDMENT_STATUS.REFUNDED.getCode().equals(refundApplyStatus)) {    //退款成功
                            LOG.info("RefundStatus is REFUNDED");
                            return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.REFUND_SUCCESS.getCode();
                        } else if (OrderRefundConstant.REFUNDMENT_STATUS.FAIL.getCode().equals(refundApplyStatus)
                                || OrderRefundConstant.REFUNDMENT_STATUS.CANCEL.getCode().equals(refundApplyStatus)
                                || OrderRefundConstant.REFUNDMENT_STATUS.REJECTED.getCode().equals(refundApplyStatus)) {  //退款失败 || 订单取消 || 审核拒绝
                            LOG.info("RefundStatus is FAIL or CANCEL or REJECTED");
                            return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.REFUND_FAIL.getCode();
                        } else if (OrderRefundConstant.REFUNDMENT_STATUS.UNVERIFIED.getCode().equals(refundApplyStatus)
                                || OrderRefundConstant.REFUNDMENT_STATUS.VERIFIEDING.getCode().equals(refundApplyStatus)
                                || OrderRefundConstant.REFUNDMENT_STATUS.REFUND_APPLY.getCode().equals(refundApplyStatus)) {  //售后单正在审核
                            LOG.info("RefundStatus is UNVERIFIED or VERIFIEDING");
                            return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.REFUND_CHECKING.getCode();
                        } else if (OrderRefundConstant.REFUNDMENT_STATUS.WORKORDER.getCode().equals(refundApplyStatus)) { //工单特殊处理
                            LOG.info("RefundStatus is WORKORDER");
                            return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.ORDER_REFUND_BTN_DISABLE.getCode();
                        } else {
                            LOG.info("RefundStatus is Other");
                            return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.REFUND_PROCESSING.getCode();//默认为退款处理中
                        }
                    } else {
                        if (OrderRefundConstant.ORDER_STATUS.CANCEL.getCode().equals(orderStatus)) {//已取消的订单
                            return orderRefundComFrontService.getHotelOrderCancelStatus(orderId, orderStatus);
                        }
                        Long distributorId = order.getDistributorId();
                        Long channelId = order.getDistributionChannel() == null ? -1 : order.getDistributionChannel();
                        
                        if(order.isHotel()){
                        	Date lastCancelTime = order.getLastCancelTime();
                            //判断设置了最晚无损取消时间
                            if (lastCancelTime != null) {
                            	LOG.info("destBU online refund,orderId:"+orderId+", isFlag:"+lastCancelTime.after(nowTime));
                                if (lastCancelTime.after(nowTime)) {
                                    //只有2后台，3前台下单、已经旅途分销和特卖会才可申请退款，其他不支持退款
                                	//在线退款二期去除(channelId == 10001 || channelId == 10002) || channelId == 107
                                    if (distributorId == 2 || distributorId == 3 || (distributorId == 4 && channelId == 10000)) {
                                        return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.REFUND_APPLY.getCode();//申请退款
                                    }
                                }else{//超过最晚取消时间
                                	return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.CANNOT_REFUND_APPLY.getCode();//申请退款按钮灰掉
                                }
                            } else {
                            	return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.CANNOT_CHANGE_ORDER.getCode();//未设置最晚无损取消时间，不退不改
                            }
                        }else{
                        	//自由行和酒店套餐订单是否超过退改时间
                        	if(!orderRefundComFrontService.isOverLastCancelTime(orderId)){
                        		 //未超过入住当天12点，方可申请退款
                                Calendar calendar = orderRefundComFrontService.setVisitTime(visitTime);
                                LOG.info("destBU online refund,orderId:"+orderId+", isFlag:"+calendar.getTime().after(nowTime));
                                if (calendar.getTime().after(nowTime)) {//未超过入住当天12点，方可申请退款
                                    //只有2后台，3前台下单、已经旅途分销和特卖会才可申请退款，其他不支持退款
                                    if (distributorId == 2 || distributorId == 3 || (channelId == 10000 || channelId == 10001 || channelId == 10002) || channelId == 107) {
                                        return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.REFUND_APPLY.getCode();//申请退款
                                    }
                                }
                        	}
                        }
                        
                    }

                }
                
            } else if (OrderRefundConstant.PAYTARGET.PAY.getCode().equals(paymentTarget)) {//现付
                if (visitTime.after(nowTime)) {//待入住
                    if (OrderRefundConstant.CREDIT_CARDER_GUARANTEE.GUARANTEE.name().equals(guarantee)) {//担保
                        return orderRefundComFrontService.getHotelOrderCancelStatus(orderId, orderStatus);
                    } else if (OrderRefundConstant.CREDIT_CARDER_GUARANTEE.UNGUARANTEE.name().equals(guarantee)) {//未担保
                        return orderRefundComFrontService.getHotelOrderCancelStatus(orderId, orderStatus);
                    }
                }
            }

        } else if (SuppGoodsBaseTimePriceVo.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(cancelStrategy)) {//不可退改

            if (OrderRefundConstant.PAYTARGET.PREPAID.getCode().equals(paymentTarget)) {//预付
                if (OrderRefundConstant.ORDER_VIEW_STATUS.UNPAY.getCode().equals(paymentStatus)) {//待支付
                    return orderRefundComFrontService.getHotelOrderCancelStatus(orderId, orderStatus);//取消订单
                } else if (OrderRefundConstant.ORDER_VIEW_STATUS.PAYED.getCode().equals(paymentStatus)) {//已支付
                	return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.CANNOT_CHANGE_ORDER.getCode();//该订单不退不改
                }

            } else if (OrderRefundConstant.PAYTARGET.PAY.getCode().equals(paymentTarget)) {//现付
                if (visitTime.after(nowTime)) {//待入住
                    if (OrderRefundConstant.CREDIT_CARDER_GUARANTEE.GUARANTEE.name().equals(guarantee)) {//担保
                        return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.CANNOT_CHANGE_ORDER.getCode();//该订单不退不改
                    } else if (OrderRefundConstant.CREDIT_CARDER_GUARANTEE.UNGUARANTEE.name().equals(guarantee)) {//未担保
                        return orderRefundComFrontService.getHotelOrderCancelStatus(orderId, orderStatus);//取消订单
                    }
                }

            }
        }
        
        return OrderRefundConstant.VST_ORDER_REFUND_APPLY_STATUS.CANNOT_REFUND.getCode();
    }

	@Override
	public void checkRefundOnlineByCommit(OrdOrder ordOrder)
			throws IllegalArgumentException {
		if (!ordOrder.isDestBuRefund() ||
                !Constant.PAYMENT_STATUS.PAYED.getCode().equals(ordOrder.getPaymentStatus()) ||
                Constant.ORDER_STATUS.CANCEL.getCode().equals(ordOrder.getOrderStatus())) {
            String s = "订单ID:" + ordOrder.getOrderId() + "不是目的地在线退款品类或未支付或已取消!";
            LOG.error(s);
            throw new IllegalArgumentException(s);
        }
		
		if(ordOrder.getLastCancelTime().before(new Date())){
    		String errorMsg = "您的订单已超过最晚退改时间:" +  DateUtil.formatDate(ordOrder.getLastCancelTime(), "yyyy年MM月dd日 HH:mm:ss");
    		LOG.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
    	}
		
	}

	@Override
	public List<OrderRefundDetailVO> getOrderRefundDetailVO(OrdOrder ordOrder) {
		// TODO Auto-generated method stub
		return null;
	}
    
}
