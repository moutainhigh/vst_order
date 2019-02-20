package com.lvmama.vst.order.service.apportion.particle.impl;

import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderCostSharingItem;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.order.abs.ApportionParticleByPriceTypeAbsService;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.apportion.particle.ApportionParticleByPriceTypeService;
import com.lvmama.vst.order.vo.OrderItemApportionInfoRelatedVO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by zhouyanqun on 2017/4/18.
 * 按价格类型(不含房差)分摊的子单的分摊信息补全服务
 */
@Service("apportionComplete4PriceTypeNoSpreadServiceImpl")
public class ApportionParticle4PriceTypeNoSpreadServiceImpl extends ApportionParticleByPriceTypeAbsService implements ApportionParticleByPriceTypeService {
    private static final Log log = LogFactory.getLog(ApportionParticle4PriceTypeNoSpreadServiceImpl.class);

    /**
     * 根据子订单和分摊金额，分摊类型，生成分摊信息集合
     *
     * @param ordOrderItem
     * @param orderApportionType
     * @param apportionAmount
     */
    @Override
    public List<OrdOrderCostSharingItem> generateOrdOrderCostSharingItem(OrdOrderItem ordOrderItem, OrderEnum.ORDER_APPORTION_TYPE orderApportionType, long apportionAmount) {
        return super.generateOrdOrderCostSharingItem(ordOrderItem, orderApportionType, apportionAmount);
    }

    /**
     * 给出所有合法的价格类型
     */
    @Override
    protected String[] getAllValidPriceType() {
        return new String[]{OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD.name()};
    }

    /**
     * 生成订单的实付金额分摊信息，实付金额比较特殊，用总价-优惠分摊-促销分摊-渠道优惠分摊-手工改价分摊
     *
     * @param ordOrderItem
     */
    @Override
    public List<OrdOrderCostSharingItem> generateOrdOrderCostSharingItem4Paid(OrdOrderItem ordOrderItem) {
        return super.doGenerateOrderCostSharingItem4Paid(ordOrderItem);
    }

    /**
     * 根据子订单和分摊信息(此处分摊信息是所有的分摊信息，也即包括非此子单的分摊信息，需要过滤)，生成子单分摊信息汇总PO的集合
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
}
