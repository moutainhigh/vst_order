package com.lvmama.vst.order.service.refund.impl.front;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.comm.utils.DateUtil;
import com.lvmama.comm.vo.Constant;
import com.lvmama.vst.api.vo.prod.SuppGoodsBaseTimePriceVo;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.back.prod.po.ProdRefundRule;
import com.lvmama.vst.back.prod.po.ProdRefundRule.CANCEL_TIME_TYPE;
import com.lvmama.vst.back.prod.po.ProdRefundRule.DEDUCTTYPE;
import com.lvmama.vst.comm.utils.json.JSONUtil;

@Service("orderRefundComFrontService")
public class OrderRefundComFrontService{
	private static final Log LOG = LogFactory.getLog(OrderRefundComFrontService.class);
	
	@Autowired
    private OrderService orderService;

	/**
     * 获取酒店订单供应商状态来返回订单的状态
     *
     * @param orderId
     * @return
     */
    public String getHotelOrderCancelStatus(Long orderId, String orderStatus) {
        if (Constant.ORDER_STATUS.NORMAL.getCode().equals(orderStatus)) {//未审批状态，显示其他
            return Constant.VST_ORDER_CANCEL_STATUS.OTHER.getCode();
        }
        int isCancelOrderBySupp = orderService.isCancelOrderWithHotelSupp(orderId);
        if (isCancelOrderBySupp == 1) {
            return Constant.VST_ORDER_CANCEL_STATUS.ORDER_CANCEL_SUCCESS.getCode();//订单取消成功
        } else if (isCancelOrderBySupp == 0) {
            return Constant.VST_ORDER_CANCEL_STATUS.ORDER_CANCELING.getCode();//订单取消中
        } else {
            return Constant.VST_ORDER_CANCEL_STATUS.ORDER_CANCEL_FAIL.getCode();//订单取消失败
        }

    }
    
    public Calendar setVisitTime(Date visitTime) {
        Calendar calendar = Calendar.getInstance();
        int year= DateUtil.getYear(visitTime);
        int month=DateUtil.getMonth(visitTime);
        int day=DateUtil.getDay(visitTime);
        String visitTime_tmp= year + "-" + month + "-" + day;
        Date getVisitDate= DateUtil.toDate(visitTime_tmp, "yyyy-MM-dd");
        calendar.setTime(getVisitDate);
        calendar.add(Calendar.HOUR, 12);
        return calendar;
    }
    
    public Boolean isOverLastCancelTime(Long orderId) {
		  OrdOrder order = orderService.queryOrdorderByOrderId(orderId);
	        if (order == null) {
	            return null;
	        }
	        if(ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.getCode().equalsIgnoreCase(order.getRealCancelStrategy()) ||
	        		ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equalsIgnoreCase(order.getRealCancelStrategy())){
	        	for (OrdOrderItem orderItem : order.getOrderItemList()) {
	        		
	        		if(SuppGoodsBaseTimePriceVo.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equalsIgnoreCase(orderItem.getCancelStrategy()) ||
	        				SuppGoodsBaseTimePriceVo.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equalsIgnoreCase(orderItem.getCancelStrategy()) ){
	        			continue;
	        		}
	        		
	        		LOG.info("订单orderId = " + orderId + "，子订单orderItemId = " + orderItem.getOrderItemId() + "，categoryId = " + orderItem.getCategoryId());
	        		//排除保险
					if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){
						continue;
					}
					
					//同步商品退改酒店子单
					if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId()) && ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.getCode().equalsIgnoreCase(order.getRealCancelStrategy())){
						LOG.info("订单orderId = " + orderId + "，子订单orderItemId = " + orderItem.getOrderItemId() + "，lastCancelTime = " + orderItem.getLastCancelTime());
						if(orderItem.getCancelStrategy().equalsIgnoreCase(SuppGoodsBaseTimePriceVo.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode())){
							if(orderItem.getLastCancelTime().after(new Date())){//没有超过最晚取消时间
								return false;
							}else{
								continue;
							}
						}else{
							continue;
						}
					}
					
		        	List<ProdRefundRule> rulesList = com.alibaba.fastjson.JSONArray.parseArray(orderItem.getRefundRules(), ProdRefundRule.class);
		        	LOG.info("订单orderId = " + orderId + "，子订单orderItemId = " + orderItem.getOrderItemId() + "，rulesList size = " + rulesList.size());
		        	if(rulesList != null && rulesList.size() == 1){
		        		ProdRefundRule rule = rulesList.get(0);
		        		LOG.info("订单orderId = " + orderId + "，子订单orderItemId = " + orderItem.getOrderItemId() + "，rule DeductType = " + rule.getDeductType() + "，rule DeductValue = " + rule.getDeductValue() + "，rule CancelTimeType = " + rule.getCancelTimeType());
		        		if(DEDUCTTYPE.PERCENT.getCode().equals(rule.getDeductType()) && rule.getDeductValue() == 10000) {//全额扣100%，不退不改
		        			//超过,继续循环子单
		        			continue;
		        		}else{
		        			if(CANCEL_TIME_TYPE.OTHER.getCode().equals(rule.getCancelTimeType())){//不满足以上规则
		        				return false;
		        			}else{//设置规则
		        				Date newDate = DateUtils.addMinutes(order.getVisitTime(), -rule.getCancelTime().intValue());
		        				if(newDate.after(new Date())){
									return false;
								}else{
									continue;
								}
		        			}
		        		}
		        	}else if(rulesList != null && rulesList.size() > 1){
		        		boolean ruleCancleflag = false;//子单退改规则
		        		for(int i = rulesList.size(); i > 0; i--){
		        			ProdRefundRule rule = rulesList.get(i - 1);
		        			LOG.info("订单orderId = " + orderId + "，子订单orderItemId = " + orderItem.getOrderItemId() + "，rule DeductType = " + rule.getDeductType() + "，rule DeductValue = " + rule.getDeductValue() + "，rule CancelTimeType = " + rule.getCancelTimeType());
		        			if(DEDUCTTYPE.PERCENT.getCode().equals(rule.getDeductType()) && rule.getDeductValue() == 10000) {//全额扣100%，不退不改
		        				continue;
		        			}else{
		        				if(CANCEL_TIME_TYPE.OTHER.getCode().equals(rule.getCancelTimeType())){//不满足以上规则
		        					ruleCancleflag = true;
		        					break;
		        				}else{
		        					Date newDate = DateUtils.addMinutes(order.getVisitTime(), -rule.getCancelTime().intValue());
			        				if(newDate.after(new Date())){
			        					ruleCancleflag = true;
			        					break;
									}else{
										continue;
									}
		        				}
		        			}
		        		}
		        		if(ruleCancleflag){//没有超过可退改时间
		        			return false;
		        		}else{
		        			continue;
		        		}
		        	}
		        }
	        }
		return true;
	}
}
