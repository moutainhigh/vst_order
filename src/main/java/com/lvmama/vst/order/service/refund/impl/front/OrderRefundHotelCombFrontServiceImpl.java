package com.lvmama.vst.order.service.refund.impl.front;

import java.util.ArrayList;
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

import com.alibaba.fastjson.JSONArray;
import com.lvmama.comm.vo.Constant;
import com.lvmama.vst.api.vo.prod.SuppGoodsBaseTimePriceVo;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsRefund;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.utils.SuppGoodsRefundTools;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.back.prod.po.ProdRefundRule;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.refund.IOrderRefundFrontService;
import com.lvmama.vst.pet.adapter.refund.vo.OrderRefundConstant;
import com.lvmama.vst.pet.adapter.refund.vo.OrderRefundDetailVO;

/**
 * 酒店套餐
 * @author chenhao
 *
 */
@Service("orderRefundHotelCombFrontService")
public class OrderRefundHotelCombFrontServiceImpl implements IOrderRefundFrontService {
	private static final Log LOG = LogFactory.getLog(OrderRefundHotelCombFrontServiceImpl.class);
	@Autowired
	private OrderRefundComFrontService orderRefundComFrontService;
	
	@Autowired
	private CategoryClientService categoryClientService;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Override
	public String getOrderRefundApplyStatus(OrdOrder order, String refundApplyStatus) {
		Long orderId =order.getOrderId();
		String cancelStrategy = order.getCancelStrategy();//退改类型
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
		
	}

	@Override
	public List<OrderRefundDetailVO> getOrderRefundDetailVO(OrdOrder simpleOrdOrder) {
		OrdOrder ordOrder = complexQueryService.queryOrderByOrderId(simpleOrdOrder.getOrderId());
		//酒店套餐退款明细创建
		LOG.info("订单orderId = " + ordOrder.getOrderId() + "，退改策略cancelStrategy = " + ordOrder.getRealCancelStrategy());
		List<OrderRefundDetailVO> refundDetailList = new ArrayList<OrderRefundDetailVO>();
		List<OrdOrderItem> ordOrderItems = ordOrder.getOrderItemList();
		for(OrdOrderItem orderItem : ordOrderItems) {
			OrderRefundDetailVO orderRefundDetailVO = new OrderRefundDetailVO();
			//排除保险
			if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){
				continue;
			}

			LOG.info("订单orderId = " + ordOrder.getOrderId() + "，子订单orderItemId = " + orderItem.getOrderItemId() + "，categoryId = " + orderItem.getCategoryId());
			Map<String,Object> contentMap = orderItem.getContentMap();
			LOG.info("productName = " + orderItem.getProductName() + "，branchName = " + contentMap.get("branchName"));
			
			orderRefundDetailVO.setItemCategoryId(orderItem.getCategoryId());
			//产品名
			orderRefundDetailVO.setItemProductName(orderItem.getProductName());
			//规格名
			orderRefundDetailVO.setItemSuppGoodsName(contentMap.get("branchName").toString());
			if(orderItem.getOrderPackId() != null){//主订单自由行中被打包的商品
				LOG.info("订单orderId = " + ordOrder.getOrderId() + "，子订单orderItemId = " + orderItem.getOrderItemId() + "，categoryId = " + orderItem.getCategoryId());
				//不退不改
				if(ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){
					orderRefundDetailVO.setItemDeductExplain("该产品不可退改，入住如需修改或取消，一律收取订单的全部费用的100%作为损失费，敬请谅解！\n");
					//orderRefundDetailVO.setItemDeductAmount(itemDeductAmount);
				}else if(ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){//人工退改
					orderRefundDetailVO.setItemDeductExplain("该产品支持人工退改，可致电24小时服务热线1010-6060。\n");
				}else if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){//可退该
					LOG.info("orderItemId = " + orderItem.getOrderItemId() + "，refundRules = " + orderItem.getRefundRules());
					//List<ProdRefundRule> rulesList = JSONArray.parseArray(orderItem.getRefundRules(), ProdRefundRule.class);
					List<ProdRefundRule> rulesList =  new ArrayList<ProdRefundRule>();
					if(StringUtil.isNotEmptyString(orderItem.getRefundRules())){
						List<ProdRefundRule> refundList_ = com.alibaba.fastjson.JSONArray.parseArray(orderItem.getRefundRules(), ProdRefundRule.class);
						if(refundList_ != null){
							rulesList = refundList_;
						}
					}
					for(ProdRefundRule rule: rulesList){
						orderRefundDetailVO.setItemDeductExplain(rule.getRuleDesc(ordOrder.getVisitTime())+"\n");
					}
				}
			}else{//关联销售（只显示门票）
				String categoryCode =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
				ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(categoryCode);
				BizCategory bizCategory=result.getReturnContent();
				if((bizCategory.getParentId()!=null && bizCategory.getParentId().equals(5L))) {//门票
					//将门票的的退改规则快照转化为商品退改规则list
					//List<SuppGoodsRefund> refundList = JSONArray.parseArray(orderItem.getRefundRules(), SuppGoodsRefund.class);
					List<SuppGoodsRefund> refundList =  new ArrayList<SuppGoodsRefund>();
					if(StringUtil.isNotEmptyString(orderItem.getRefundRules())){
						List<SuppGoodsRefund> refundList_ = com.alibaba.fastjson.JSONArray.parseArray(orderItem.getRefundRules(), SuppGoodsRefund.class);
						if(refundList_ != null){
							refundList = refundList_;
						}
					}
					orderRefundDetailVO.setItemDeductExplain(SuppGoodsRefundTools.SuppGoodsRefundVOToStr(refundList,contentMap.get("aperiodic_flag").toString())+"\n");
				}
			}
			refundDetailList.add(orderRefundDetailVO);
		}
		LOG.info("orderId = " + ordOrder.getOrderId() + " getOrderRefundDetailVO End，refundDetailList size = " + refundDetailList.size());
		
		// TODO Auto-generated method stub
		return refundDetailList;
	}
	
    
    

}
