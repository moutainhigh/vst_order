package com.lvmama.vst.order.service.apportion.particle;

import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderCostSharingItem;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.vo.OrderItemApportionInfoRelatedVO;

import java.util.List;

/**
 * Created by zhouyanqun on 2017/4/18.
 * 分摊粒度服务
 */
public interface ApportionParticleService {

    /**
     * 根据子订单和分摊金额，分摊类型，生成分摊信息集合(不包括实付)
     * */
    List<OrdOrderCostSharingItem> generateOrdOrderCostSharingItem(OrdOrderItem ordOrderItem, OrderEnum.ORDER_APPORTION_TYPE orderApportionType, long apportionAmount);


    /**
     * 生成订单的实付金额分摊信息，实付金额比较特殊，用总价-优惠分摊-促销分摊-渠道优惠分摊-手工改价分摊
     * */
    List<OrdOrderCostSharingItem> generateOrdOrderCostSharingItem4Paid(OrdOrderItem ordOrderItem);

    /**
     * 根据子订单和分摊相关信息(此处分摊信息是所有的分摊信息，也即包括非此子单的分摊信息，需要过滤)，生成子单分摊信息汇总PO的集合
     * */
    OrderItemApportionInfoPO generateItemApportionInfoPO(OrdOrderItem orderItem, OrderItemApportionInfoRelatedVO orderItemApportionInfoRelatedVO);
    /**
     * 把分摊信息按照多价格的顺序排列
     * 如果分摊数据有残缺，补上
     * */
    void sortAndCompleteApportionInfoByMulPrice(OrderItemApportionInfoPO orderItemApportionInfoPO, List<OrdMulPriceRate> ordMulPriceList);
}
