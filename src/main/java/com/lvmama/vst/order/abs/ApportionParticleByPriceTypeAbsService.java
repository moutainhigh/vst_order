package com.lvmama.vst.order.abs;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.vo.MemcachedEnum;
import com.lvmama.vst.order.service.IOrdOrderPackService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.book.util.OrderBookServiceDataUtil;
import com.lvmama.vst.order.utils.ApportionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by zhouyanqun on 2017/4/20.
 */
public abstract class ApportionParticleByPriceTypeAbsService extends ApportionParticleMeticulousAbsService {
    private static final Log log = LogFactory.getLog(ApportionParticleByPriceTypeAbsService.class);
    @Resource
    private IOrderUpdateService orderUpdateService;
    @Resource
    private OrderBookServiceDataUtil orderBookServiceDataUtil;
    @Resource
    private IOrdOrderPackService orderPackService;

    //根据子订单和分摊金额，分摊类型，按价格类型生成分摊信息集合
    protected List<OrdOrderCostSharingItem> generateOrdOrderCostSharingItem(OrdOrderItem ordOrderItem, OrderEnum.ORDER_APPORTION_TYPE orderApportionType, long apportionAmount){
        List<OrdOrderCostSharingItem> orderCostSharingItemList = new ArrayList<>();
        //剩下未分摊的金额，因为最后一个子单需要做减法得到，所以每分摊一点金额，就把这个变量减掉分摊掉的金额，等到了最后一个子单，这个数值就是分摊到的金额
        long leftApportionAmount = apportionAmount;
        //销售总价，最大销售价
        long totalOrdMulPriceRatePrice = 0, maxOrdMulPriceRatePrice = 0;
        List<OrdMulPriceRate> ordMulPriceRateList = ordOrderItem.getOrdMulPriceRateList();
        log.info("now generate order cost sharing item for item " + ordOrderItem.getOrderItemId() + ", ordMulPriceRateList size is " + ordMulPriceRateList.size() + ", apportionAmount is " + apportionAmount);
        List<OrdMulPriceRate> filteredOrdMulPriceRateList = filterOutRedundantRate(ordMulPriceRateList);
        //包含最大销售价的子单，其它子单按比例摊总优惠金额，这个子单摊其它子单摊剩下的金额
        OrdMulPriceRate rateContainMaxSalesPrice = filteredOrdMulPriceRateList.get(0);
        //价格项的单价、数量、总价
        long ratePrice, quantity, rateTotalAmount;
        //第一遍循环，算出总价、售价最高的价格项
        for (OrdMulPriceRate ordMulPriceRate : filteredOrdMulPriceRateList) {
            ratePrice = NumberUtils.isAboveZero(ordMulPriceRate.getPrice()) ? ordMulPriceRate.getPrice() : 0;
            quantity = NumberUtils.isAboveZero(ordMulPriceRate.getQuantity()) ? ordMulPriceRate.getQuantity() : 0;
            rateTotalAmount = ratePrice * quantity;
            totalOrdMulPriceRatePrice += rateTotalAmount;
            //如果当前子单的售价大于已有的最大总价，则把最大售价以及包含最大售价的子单变量更新
            if(rateTotalAmount > maxOrdMulPriceRatePrice) {
                maxOrdMulPriceRatePrice = rateTotalAmount;
                rateContainMaxSalesPrice = ordMulPriceRate;
            }
        }

        //第二遍循环，分摊优惠总额
        for (OrdMulPriceRate ordMulPriceRate : filteredOrdMulPriceRateList) {
            if(ordMulPriceRate == rateContainMaxSalesPrice) {
                //最大售价子单，先不计算，等循环结束后，剩下的金额就是
                continue;
            }
            //非最大售价子单，按比例分摊
            ratePrice = NumberUtils.isAboveZero(ordMulPriceRate.getPrice()) ? ordMulPriceRate.getPrice() : 0;
            quantity = NumberUtils.isAboveZero(ordMulPriceRate.getQuantity()) ? ordMulPriceRate.getQuantity() : 0;
            rateTotalAmount = ratePrice * quantity;
            //分摊金额
            long ordMulPriceRateApportionAmount = calculateAmountByPercent(rateTotalAmount, totalOrdMulPriceRatePrice, apportionAmount);
            leftApportionAmount -= ordMulPriceRateApportionAmount;

            //生成分摊信息，加入列表
            OrdOrderCostSharingItem orderCostSharingItem = createOrdOrderCostSharingItem(ordOrderItem);
            orderCostSharingItem.setCostCategory(orderApportionType.name());
            orderCostSharingItem.setCostType(ordMulPriceRate.getPriceType());
            orderCostSharingItem.setAmount(ordMulPriceRateApportionAmount);
            orderCostSharingItemList.add(orderCostSharingItem);
        }
        //生成最大分摊项的分摊信息，加入列表
        OrdOrderCostSharingItem orderCostSharingItem = createOrdOrderCostSharingItem(ordOrderItem);
        orderCostSharingItem.setCostCategory(orderApportionType.name());
        orderCostSharingItem.setCostType(rateContainMaxSalesPrice.getPriceType());
        orderCostSharingItem.setAmount(leftApportionAmount);
        orderCostSharingItemList.add(orderCostSharingItem);
        log.info("now completed generate order cost sharing item for item " + ordOrderItem.getOrderItemId() + ", orderCostSharingItemList size is " + orderCostSharingItemList.size() + ", apportionAmount is " + apportionAmount);
        return orderCostSharingItemList;
    }

    //生成订单的实付金额分摊信息，实付金额比较特殊，用总价-优惠分摊-促销分摊-渠道优惠分摊-手工改价分摊
    protected List<OrdOrderCostSharingItem> doGenerateOrderCostSharingItem4Paid(OrdOrderItem ordOrderItem){
        if(CollectionUtils.isEmpty(ordOrderItem.getOrdMulPriceRateList())) {
            return null;
        }
        List<OrdOrderCostSharingItem> orderCostSharingItemList = new ArrayList<>();
        for (OrdMulPriceRate ordMulPriceRate : ordOrderItem.getOrdMulPriceRateList()) {
            if(!isApportionPriceType(ordMulPriceRate.getPriceType())){
                continue;
            }

            //总价格
            long totalMulAmount = ordMulPriceRate.getPrice() * ordMulPriceRate.getQuantity();
            //分摊金额，包括优惠分摊金额、促销分摊金额、渠道优惠分摊金额、手工改价分摊金额
            long beforePaymentApportionAmount = 0;
            if (CollectionUtils.isNotEmpty(ordOrderItem.getOrderCostSharingItemList())) {
                for (OrdOrderCostSharingItem orderCostSharingItem : ordOrderItem.getOrderCostSharingItemList()) {
                    if(!StringUtils.equals(orderCostSharingItem.getCostType(), ordMulPriceRate.getPriceType())) {
                        continue;
                    }
                    if(ApportionUtil.isActualPaymentApportionType(orderCostSharingItem.getCostCategory())) {
                        continue;
                    }
                    //分销渠道的供应商打包的跟团游，由于多价格表里面的成人价、儿童价、房差已经是分销价，不需要再减去渠道优惠
                    if (ApportionUtil.isDistributorActPaidCorrectEnabled()) {
                        if (this.needsSkipDistributor(ordOrderItem, orderCostSharingItem.getCostCategory())){
                            continue;
                        }
                    }
                    beforePaymentApportionAmount += orderCostSharingItem.getAmount();
                }
            }
            long actualPaidApportionAmount = totalMulAmount - beforePaymentApportionAmount;
            OrdOrderCostSharingItem ordOrderCostSharingItem = createOrdOrderCostSharingItem(ordOrderItem);
            ordOrderCostSharingItem.setCostCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment.name());
            ordOrderCostSharingItem.setCostType(ordMulPriceRate.getPriceType());
            ordOrderCostSharingItem.setAmount(actualPaidApportionAmount);
            orderCostSharingItemList.add(ordOrderCostSharingItem);
        }
        return orderCostSharingItemList;
    }

    //分销渠道的订单，如果是供应商打包的跟团游，由于多价格表里面的成人价、儿童价、房差已经是分销价，不需要再减去渠道优惠
    private boolean needsSkipDistributor(OrdOrderItem ordOrderItem, String costCategory) {
        if (ordOrderItem == null || StringUtils.isBlank(costCategory)) {
            return false;
        }
        //判断分摊类型是否是渠道优惠
        if (!StringUtils.equals(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_distributor.name(), costCategory)) {
            return false;
        }
        //查询订单，得到品类和渠道信息
        Long orderId = ordOrderItem.getOrderId();
        if (NumberUtils.isNotAboveZero(orderId)) {
            return false;
        }
        String key = MemcachedEnum.OrdOrderInfo.getKey() + orderId;
        OrdOrder ordOrder = MemcachedUtil.getInstance().get(key);
        if (ordOrder == null) {
            ordOrder = orderUpdateService.queryOrdOrderByOrderId(orderId);
            MemcachedUtil.getInstance().set(key, MemcachedEnum.OrdOrderInfo.getSec(), ordOrder);
        }
        if (ordOrder == null || NumberUtils.isNotAboveZero(ordOrder.getCategoryId())) {
            return false;
        }
        //判断品类是否是跟团游
        if (!Objects.equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId(), ordOrderItem.getCategoryId())) {
            return false;
        }
        //判断是否分销的订单
        if (!orderBookServiceDataUtil.isDistributorOrder(ordOrder)) {
            return false;
        }
        //判断打包类型是否是供应商打包
        List<Long> orderIdList = new ArrayList<>();
        orderIdList.add(orderId);
        List<OrdOrderPack> orderPackList = ordOrder.getOrderPackList();
        if (CollectionUtils.isEmpty(orderPackList)) {
            orderPackList = orderPackService.findOrdOrderByOrderIds(orderIdList);
            ordOrder.setOrderPackList(orderPackList);
        }
        return CollectionUtils.isNotEmpty(orderPackList) && orderPackList.get(0) != null
                && StringUtils.equals(ProdProduct.PACKAGETYPE.SUPPLIER.getCode(), orderPackList.get(0).getOwnPack());
    }
}
