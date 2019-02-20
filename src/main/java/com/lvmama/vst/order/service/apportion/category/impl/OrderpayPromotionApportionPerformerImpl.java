package com.lvmama.vst.order.service.apportion.category.impl;

import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_APPORTION_TYPE;
import com.lvmama.vst.order.abs.OrderPercentApportionAbsPerformer;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.apportion.category.OrderInternalApportionPerformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单银行支付立减分摊
 * 
 * @author Administrator
 *
 */
@Component("orderpayPromotionApportionPerformer")
public class OrderpayPromotionApportionPerformerImpl extends OrderPercentApportionAbsPerformer implements OrderInternalApportionPerformer {
	
    private static final Log log = LogFactory.getLog(OrderpayPromotionApportionPerformerImpl.class);


    /**
     * 针对订单做支付立减分摊
     *
     * @param order 将被分摊的订单
     */
	@Override
	public void doApportionOrderAmount(OrdOrder order,List<OrdOrderItem> apportionOrderItemList) {
		 if(order == null || CollectionUtils.isEmpty(apportionOrderItemList)) {
	            return;
	        }
	        //银行立减金额
	        long payPromotionAmount = getReductionAmount(order);
	        log.info("Now apportion coupon amount for order " + order.getOrderId() + ", couponAmount is " + payPromotionAmount);
	        if(payPromotionAmount <= 0) {
	            return;
	        }
	        this.doApportionOrderAmount(order, apportionOrderItemList, payPromotionAmount);
	}
	
	/**
	 * 获得支付立减金额
	 * @param order
	 * @return
	 */
	private long getReductionAmount(OrdOrder order) {
		if(order == null) {
			return 0;
		}

		if(CollectionUtils.isEmpty(order.getOrderAmountItemList())) {
			return 0;
		}

		long discountAmount = 0;

		for (OrdOrderAmountItem ordOrderAmountItem : order.getOrderAmountItemList()) {
			if(StringUtils.equals(OrderEnum.ORDER_AMOUNT_TYPE.PAY_PROMOTION_AMOUNT.name(), ordOrderAmountItem.getOrderAmountType())) {
				discountAmount = ordOrderAmountItem.getItemAmount();
				break;
			}
		}

		return -discountAmount;
	}

	@Override
	protected ORDER_APPORTION_TYPE getApportionType() {
		 return OrderEnum.ORDER_APPORTION_TYPE.apportion_type_pay_promotion;
	}
    /**
     * 把分摊信息按照多价格的顺序排列
     * 如果分摊数据有残缺，补上
     */
	@Override
	public void sortAndCompleteApportionInfoByMulPrice(OrderItemApportionInfoPO orderItemApportionInfoPO,
			List<OrdMulPriceRate> ordMulPriceList) {
		log.info("怎么会调用到这里来呢？该方法用不上，但是接口要求需要实现");
	}
}
