package com.lvmama.vst.order.service.apportion.particle.impl;

import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderCostSharingItem;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.order.abs.ApportionParticleAbsService;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.apportion.particle.ApportionParticleService;
import com.lvmama.vst.order.utils.ApportionUtil;
import com.lvmama.vst.order.utils.OrdOrderItemUtils;
import com.lvmama.vst.order.vo.OrderItemApportionInfoRelatedVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyanqun on 2017/4/18.
 * 分摊粒度为子订单的分摊信息补全服务
 */
@Service("apportionComplete4ItemServiceImpl")
public class ApportionParticle4ItemServiceImpl extends ApportionParticleAbsService implements ApportionParticleService {
    private static final Log log = LogFactory.getLog(ApportionParticle4ItemServiceImpl.class);

    /**
     * 根据子订单和分摊金额，分摊类型，生成分摊信息集合
     *
     * @param ordOrderItem
     * @param orderApportionType 分摊类型，比如优惠、促销等
     * @param apportionAmount
     */
    @Override
    public List<OrdOrderCostSharingItem> generateOrdOrderCostSharingItem(OrdOrderItem ordOrderItem, OrderEnum.ORDER_APPORTION_TYPE orderApportionType, long apportionAmount) {
        List<OrdOrderCostSharingItem> orderCostSharingItemList = new ArrayList<>();
        OrdOrderCostSharingItem orderCostSharingItem = createOrdOrderCostSharingItem(ordOrderItem);
        orderCostSharingItem.setCostCategory(orderApportionType.name());
        orderCostSharingItem.setCostType(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name());
        orderCostSharingItem.setAmount(apportionAmount);
        log.info("Generated cost sharing item is " + GsonUtils.toJson(orderCostSharingItem));
        orderCostSharingItemList.add(orderCostSharingItem);
        return orderCostSharingItemList;
    }

    /**
     * 根据子订单和分摊信息(此处分摊信息是所有的分摊信息，也即包括非此子单的分摊信息，需要过滤)，生成子单分摊信息汇总PO的集合
     *
     *
     */
    @Override
    public OrderItemApportionInfoPO generateItemApportionInfoPO(OrdOrderItem orderItem, OrderItemApportionInfoRelatedVO orderItemApportionInfoRelatedVO) {
        return super.doGenerateItemApportionInfoPO(orderItem, orderItemApportionInfoRelatedVO);
    }

    /**
     * 把分摊信息按照多价格的顺序排列
     * 如果分摊数据有残缺，补上
     *
     * @param orderItemApportionInfoPO
     * @param ordMulPriceList
     */
    @Override
    public void sortAndCompleteApportionInfoByMulPrice(OrderItemApportionInfoPO orderItemApportionInfoPO, List<OrdMulPriceRate> ordMulPriceList) {
        super.doSortAndCompleteApportionInfoByMulPrice(orderItemApportionInfoPO, ordMulPriceList);
    }

    /**
     * 生成订单的实付金额分摊信息，实付金额比较特殊，用总价-优惠分摊-促销分摊-渠道优惠分摊-手工改价分摊
     *
     * @param ordOrderItem
     */
    @Override
    public List<OrdOrderCostSharingItem> generateOrdOrderCostSharingItem4Paid(OrdOrderItem ordOrderItem) {
        if(ordOrderItem == null) {
            return null;
        }
        List<OrdOrderCostSharingItem> orderCostSharingItemList = new ArrayList<>();
        //分摊金额，包括优惠分摊金额、促销分摊金额、渠道优惠分摊金额、手工改价分摊金额
        long beforePaymentApportionAmount = 0;
        if (CollectionUtils.isNotEmpty(ordOrderItem.getOrderCostSharingItemList())) {
            for (OrdOrderCostSharingItem orderCostSharingItem : ordOrderItem.getOrderCostSharingItemList()) {
                if(orderCostSharingItem==null||orderCostSharingItem.getCostCategory()==null||ApportionUtil.isActualPaymentApportionType(orderCostSharingItem.getCostCategory())) {
                    continue;
                }
                beforePaymentApportionAmount += orderCostSharingItem.getAmount();
            }
        }
        long actualPaidApportionAmount = ordOrderItem.getTotalAmount() - beforePaymentApportionAmount;
        OrdOrderCostSharingItem ordOrderCostSharingItem = createOrdOrderCostSharingItem(ordOrderItem);
        ordOrderCostSharingItem.setCostCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment.name());
        ordOrderCostSharingItem.setCostType(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name());
        ordOrderCostSharingItem.setAmount(actualPaidApportionAmount);
        orderCostSharingItemList.add(ordOrderCostSharingItem);
        return orderCostSharingItemList;
    }

    /**
     * 给出所有合法的价格类型
     */
    @Override
    protected String[] getAllValidPriceType() {
        return new String[] {OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name()};
    }
}
