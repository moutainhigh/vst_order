package com.lvmama.vst.order.utils;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.order.job.AutoTaskAssignJob;

public class OrderPropertyRule {
	/**
	 * 日志记录器
	 */
	private static final Log LOGGER = LogFactory.getLog(AutoTaskAssignJob.class);
	/**
	 * 目的地是否出境
	 * @param item
	 * @return
	 */
	private static boolean isToForeign(OrdOrderItem item){
		LOGGER.debug("OrderPropertyRule.isToForeign订单编号：" + item.getOrderId() + "目的地：" + item.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.is_to_foreign.name()));
		if("Y".equalsIgnoreCase(item.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.is_to_foreign.name()))){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 出发地是否出境
	 * @param item
	 * @return
	 */
	private static boolean isFromForeign(OrdOrderItem item){
		LOGGER.debug("OrderPropertyRule.isFromForeign订单编号：" + item.getOrderId() + "出发地：" + item.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.is_from_foreign.name()));
		if("Y".equalsIgnoreCase(item.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.is_from_foreign.name()))){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 是否国内短线线路
	 * @return
	 */
	private static boolean isInnerShortLine(OrdOrderItem item){
		LOGGER.debug("OrderPropertyRule.isInnerShortLine订单编号：" + item.getOrderId() + "国内短线：" + item.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name()));
		if(ProdProduct.PRODUCTTYPE.INNERSHORTLINE.getCode().equalsIgnoreCase(item.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name()))){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 是否国内长线 线路
	 * @param item
	 * @return
	 */
	private static boolean isInnerLongLine(OrdOrderItem item){
		LOGGER.debug("OrderPropertyRule.isInnerLongLine订单编号：" + item.getOrderId() + "国内线路：" + item.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name()));
		if(ProdProduct.PRODUCTTYPE.INNERLINE.getCode().equalsIgnoreCase(item.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name()))
			||ProdProduct.PRODUCTTYPE.INNERLONGLINE.getCode().equalsIgnoreCase(item.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name()))){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 是否出境线路
	 * @param item
	 * @return
	 */
	private static boolean isForeignLine(OrdOrderItem item){
		LOGGER.debug("OrderPropertyRule.isForeignLine订单编号：" + item.getOrderId() + "出境线路：" + item.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name()));
		if(ProdProduct.PRODUCTTYPE.FOREIGNLINE.getCode().equalsIgnoreCase(item.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name()))){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 是否含大交通
	 * @param item
	 * @return
	 */
	private static boolean isBigTrafficFlag(OrdOrderItem item){
		LOGGER.debug("OrderPropertyRule.isBigTrafficFlag订单编号：" + item.getOrderId() + "大交通：" + item.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.big_traffic_flag.name()));
		if("Y".equalsIgnoreCase(item.getContentStringByKey(OrderEnum.ORDER_ROUTE_TYPE.big_traffic_flag.name()))){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 执行规则
	 * @param rule
	 * @param order
	 * @param item
	 * @return
	 */
	
	public static boolean calculateRule(String rule, OrdOrderItem item){
		if(OrderEnum.ORDER_PROPERTY_RULE.r0.name().equals(rule)){
			return true;
		}else if(OrderEnum.ORDER_PROPERTY_RULE.r1.name().equals(rule)){//出发并且目的地不是出境
			if(!isFromForeign(item) && !isToForeign(item)){
				return true;
			}
		}else if(OrderEnum.ORDER_PROPERTY_RULE.r2.name().equals(rule)){//出发地或目的地是出境
			if(isFromForeign(item) || isToForeign(item)){
				return true;
			}
		}else if(OrderEnum.ORDER_PROPERTY_RULE.r3.name().equals(rule)){//目的地是不是出境
			if(!isToForeign(item)){
				return true;
			}
		}else if(OrderEnum.ORDER_PROPERTY_RULE.r4.name().equals(rule)){//目的地是出境
			if(isToForeign(item)){
				return true;
			}
		}else if(OrderEnum.ORDER_PROPERTY_RULE.r5.name().equals(rule)){//不含大交通，不是出境
			if(!isBigTrafficFlag(item) && !isToForeign(item)){
				return true;
			}
		}else if(OrderEnum.ORDER_PROPERTY_RULE.r6.name().equals(rule)){//短线，不是出境
			if(isInnerShortLine(item) && !isToForeign(item)){
				return true;
			}
		}else if(OrderEnum.ORDER_PROPERTY_RULE.r7.name().equals(rule)){//含大交通并且是长线（跟团游才有效）
			if(isBigTrafficFlag(item) && isInnerLongLine(item)){
				return true;
			}
		}else if(OrderEnum.ORDER_PROPERTY_RULE.r8.name().equals(rule)){//含大交通&&是线路出境
			if(isBigTrafficFlag(item) && isForeignLine(item)){
				return true;
			}
		}else if(OrderEnum.ORDER_PROPERTY_RULE.r9.name().equals(rule)){//不含大交通并且目的地出境
			if(!isBigTrafficFlag(item) && isToForeign(item)){
				return true;
			}
		}
		logger.warn("orderItem:{},code:{} 无法匹配分单细分规则",new Object[]{item.getOrderItemId(),rule});
		return false;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(OrderPropertyRule.class);
	
}
