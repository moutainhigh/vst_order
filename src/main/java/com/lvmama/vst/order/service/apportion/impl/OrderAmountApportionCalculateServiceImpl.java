package com.lvmama.vst.order.service.apportion.impl;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.po.OrdAmountChange;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.vo.MemcachedEnum;
import com.lvmama.vst.order.service.IOrderAmountChangeService;
import com.lvmama.vst.order.service.apportion.ApportionOrderDataPrepareService;
import com.lvmama.vst.order.service.apportion.OrderAmountApportionCalculateService;
import com.lvmama.vst.order.service.apportion.assist.ApportionDataAssist;
import com.lvmama.vst.order.service.apportion.category.OrderInternalApportionPerformer;
import com.lvmama.vst.order.service.apportion.category.OrderManualChangeApportionPerformer;
import com.lvmama.vst.order.vo.ApportionQueryVO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zhouyanqun on 2017/4/11.
 */
@Service
public class OrderAmountApportionCalculateServiceImpl implements OrderAmountApportionCalculateService {
    private static final Log log = LogFactory.getLog(OrderAmountApportionCalculateServiceImpl.class);
    /**优惠分摊*/
    @Resource(name = "orderCouponApportionPerformerImpl")
    private OrderInternalApportionPerformer orderCouponApportionPerformer;
    /**促销分摊*/
    @Resource(name = "orderPromotionApportionPerformerImpl")
    private OrderInternalApportionPerformer orderPromotionApportionPerformer;
    /**分销优惠分摊*/
    @Resource(name = "orderDistributorApportionPerformerImpl")
    private OrderInternalApportionPerformer orderDistributorApportionPerformer;
    /**客服手工改价分摊*/
    @Resource
    private OrderManualChangeApportionPerformer orderManualChangeApportionPerformer;
    
    /**银行支付立减分摊*/
    @Resource(name = "orderpayPromotionApportionPerformer")
    private OrderInternalApportionPerformer orderpayPromotionApportionPerformer;
    
    /**实付分摊*/
    @Resource(name = "orderActualPaymentApportionPerformerImpl")
    private OrderInternalApportionPerformer orderActualPaymentApportionPerformer;
    
    @Resource
    private ApportionOrderDataPrepareService apportionOrderDataPrepareService;
    
    @Resource
    private IOrderAmountChangeService orderAmountChangeService;
    @Resource
    private ApportionDataAssist apportionDataAssist;

    /**
     * 分摊订单金额
     * 此方法不对子单作过滤，就是订单中的子单必须已经过滤过
     *
     * @param order
     */
    @Override
    public void apportionOrderAmount(OrdOrder order) {
        if(order == null) {
            return;
        }
        log.info("Now apportion order amount for order " + order.getOrderId());
        List<OrdOrderItem> apportionOrderItemList = apportionDataAssist.filterOrderItems(order.getOrderItemList());
        orderCouponApportionPerformer.doApportionOrderAmount(order, apportionOrderItemList);
        orderPromotionApportionPerformer.doApportionOrderAmount(order, apportionOrderItemList);
        orderDistributorApportionPerformer.doApportionOrderAmount(order, apportionOrderItemList);
        log.info("Apportion order amount for order " + order.getOrderId() + " completed");
    }

    /**
     * 分摊订单上的手工改价
     *
     */
    @Override
    public OrdOrder apportionOrderManualAmount(Long orderId, OrdAmountChange oldOrdAmountChange) {
        if(orderId == null || orderId < 0) {
            return null;
        }
        if(oldOrdAmountChange == null || oldOrdAmountChange.getAmount() == null | oldOrdAmountChange.getAmount() < 0) {
            return null;
        }
        OrdOrder order;
        log.info("Now apportion order manual change amount for order" + orderId + ", object type is " + oldOrdAmountChange.getObjectType());
        if(StringUtils.equals(oldOrdAmountChange.getObjectType(), OrderEnum.ORDER_AMOUNT_CHANG_OBJECT_TYPE.ORDER.name())) {
            //准备数据
            order = apportionOrderDataPrepareService.prepareOrderDataForManualChange(orderId);
            //带符号的修改金额(如果订单金额是减少，那么这个数字应该是正)
            long manualChangeAmount = oldOrdAmountChange.getAmount();
            if(StringUtils.equals(oldOrdAmountChange.getFormulas(), OrderEnum.ORDER_AMOUNT_FORMULAS.PLUS.name())) {
                manualChangeAmount = -manualChangeAmount;
            }
            //当修改的价格是主单时，分摊掉修改的价格
            List<OrdOrderItem> apportionOrderItemList = apportionDataAssist.filterOrderItems(order.getOrderItemList());
            orderManualChangeApportionPerformer.apportionOrderAmount(order, apportionOrderItemList, manualChangeAmount);
        } else {
            //准备数据
            order = apportionOrderDataPrepareService.prepareApportionDataForBookingApportion(orderId);
            //作废以前的分摊数据
            log.info("Invalid previous apportion record, order is " + orderId);
            ApportionQueryVO apportionQueryVO = new ApportionQueryVO();
            apportionQueryVO.setOrderId(orderId);
            apportionDataAssist.invalidOrderApportionData(apportionQueryVO);
            log.info("Invalid previous apportion record completed, order is " + orderId);
            apportionOrderAmount(order); //重新分摊优惠、促销、分销优惠金额
            List<OrdAmountChange> ordAmountChangeList = orderAmountChangeService.queryOrderPassedAmountChangeList(orderId);
            List<OrdOrderItem> apportionOrderItemList = apportionDataAssist.filterOrderItems(order.getOrderItemList());
            orderManualChangeApportionPerformer.reDoManualChangeApportion(order, apportionOrderItemList, ordAmountChangeList); //分摊手工改价
        }
        log.info("Apportion order manual change amount completed, order is " + orderId + ", object type is " + oldOrdAmountChange.getObjectType());
        return order;
    }

    /**
     * 分摊订单上所有的手工改价
     * 此方法仅仅生成数据，并不写入数据库
     */
    @Override
    public OrdOrder apportionFullManualAmount(Long orderId) {
        if(NumberUtils.equalsOrBelowZero(orderId)) {
            log.warn("Order id is illegal, can't apportion full manual amount");
            return null;
        }
        log.info("Now apportion full manual amount for order " + orderId);
        //准备数据
        OrdOrder order = apportionOrderDataPrepareService.prepareOrderDataForManualChange(orderId);
        List<OrdAmountChange> ordAmountChangeList = orderAmountChangeService.queryOrderPassedAmountChangeList(orderId);
        List<OrdOrderItem> apportionOrderItemList = apportionDataAssist.filterOrderItems(order.getOrderItemList());
        orderManualChangeApportionPerformer.reDoManualChangeApportion(order, apportionOrderItemList, ordAmountChangeList); //分摊手工改价
        log.info("Apportion full manual amount for order " + orderId + " completed");
        return order;
    }

    /**
     * 分摊实付金额
     *
     */
    @Override
    public OrdOrder apportionActualPaidAmount(Long orderId) {
        if(orderId == null || orderId < 0) {
            return null;
        }
        log.info("Now apportion actual payment amount for order, order id is " + orderId);
        //准备订单数据
        OrdOrder order = apportionOrderDataPrepareService.prepareOrderDataForPayment(orderId);
        this.cacheOrderInfo(order);
        //过滤保险快递
        List<OrdOrderItem> apportionOrderItemList = apportionDataAssist.filterOrderItems(order.getOrderItemList());    
        //做支付立减分摊
        orderpayPromotionApportionPerformer.doApportionOrderAmount(order,apportionOrderItemList);
        log.info("Apportion pay reduction amount for order completed, order id is " + orderId);
        //做实付分摊
        orderActualPaymentApportionPerformer.doApportionOrderAmount(order, order.getOrderItemList());
        log.info("Apportion actual payment amount for order completed, order id is " + orderId);
        return order;
    }

    /**
     * 分摊实付金额
     * 数据需要已经准备好，子单需要已经过滤
     *
     * @param order
     */
    @Override
    public void apportionActualPaidAmount(OrdOrder order) {
        if(order == null) {
            return;
        }
        this.cacheOrderInfo(order);
        Long orderId = order.getOrderId();
        log.info("Now apportion actual payment amount for order, order id is " + orderId);
      //过滤保险快递
        List<OrdOrderItem> apportionOrderItemList = apportionDataAssist.filterOrderItems(order.getOrderItemList()); 
        //做支付立减分摊
        orderpayPromotionApportionPerformer.doApportionOrderAmount(order, apportionOrderItemList);
        log.info("Apportion pay reduction amount for order completed, order id is " + orderId);
        //做实付分摊
        orderActualPaymentApportionPerformer.doApportionOrderAmount(order, order.getOrderItemList());
        log.info("Apportion actual payment amount for order completed, order id is " + orderId);
    }

    //把订单放入缓存，方便后面判断是否需要略过渠道优惠的信息
    private void cacheOrderInfo(OrdOrder order){
        String key = MemcachedEnum.OrdOrderInfo.getKey() + order.getOrderId();
        MemcachedUtil.getInstance().set(key, MemcachedEnum.OrdOrderInfo.getSec(), order);
    }
}
