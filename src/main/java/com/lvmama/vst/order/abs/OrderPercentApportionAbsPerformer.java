package com.lvmama.vst.order.abs;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderCostSharingItem;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.order.factory.ApportionParticleServiceFactory;
import com.lvmama.vst.order.service.apportion.particle.ApportionParticleService;
import com.lvmama.vst.order.utils.ApportionUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyanqun on 2017/4/21.
 * 需要按比例分摊的金额的分摊服务父类
 */
public abstract class OrderPercentApportionAbsPerformer extends OrderAmountApportionAbsPerformer {
    private static final Log log = LogFactory.getLog(OrderPercentApportionAbsPerformer.class);
    @Resource
    private ApportionParticleServiceFactory particleServiceFactory;

    protected void doApportionOrderAmount(OrdOrder order, List<OrdOrderItem> apportionOrderItemList, Long orderApportionAmount) {
        log.info("Now apportion " + getApportionType().name() + " amount for order [" + order.getOrderId() + "], total apportion amount is " + orderApportionAmount);
        //销售总价，最大销售价
        long totalItemSalesPrice = 0, maxItemSalesPrice = 0;
        //包含最大销售价的子单，其它子单按比例摊总优惠金额，这个子单摊其它子单摊剩下的金额
        OrdOrderItem itemContainMaxSalesPrice = apportionOrderItemList.get(0);
        //最大售价子单的商品id，名称，打日志用
        Long suppGoodsIdOfMaxSalesPriceItem = 0L;
        String suppGoodsNameOfMaxSalesPriceItem = null;
        //第一遍循环，算出总售价、售价最高的子单
        for (OrdOrderItem ordOrderItem : apportionOrderItemList) {
            Long itemTotalAmount = NumberUtils.isAboveZero(ordOrderItem.getTotalAmount()) ? ordOrderItem.getTotalAmount() : 0L;
            totalItemSalesPrice += itemTotalAmount;
            //如果当前子单的售价大于已有的最大总价，则把最大售价以及包含最大售价的子单变量更新
            if(itemTotalAmount > maxItemSalesPrice) {
                maxItemSalesPrice = itemTotalAmount;
                itemContainMaxSalesPrice = ordOrderItem;
                suppGoodsIdOfMaxSalesPriceItem = ordOrderItem.getSuppGoodsId();
                suppGoodsNameOfMaxSalesPriceItem =ordOrderItem.getSuppGoodsName();
            }
        }
        log.info("The first circulation completed, order is " + order.getOrderId() + ", max sale price is " + maxItemSalesPrice + ","
                + " the max sale price item's goods id is " + suppGoodsIdOfMaxSalesPriceItem + ", name is " + suppGoodsNameOfMaxSalesPriceItem
                + ", total " + getApportionType().name() + " amount is " + orderApportionAmount);

        //第二遍循环，分摊优惠总额
        //剩下的未分摊的金额
        long leftApportionAmount = orderApportionAmount;
        for (OrdOrderItem ordOrderItem : apportionOrderItemList) {
            //最大分摊金额对应的子单先不分摊，等其它子单都分摊好了再分摊
            if(ordOrderItem == itemContainMaxSalesPrice) {
                continue;
            }
            //分摊金额
            long apportionAmount = 0;
            //分摊方式，商品id，商品名称，按比例还是按剩余优惠价，打日志用
            String apportionType = "percent", suppGoodsName;
            Long suppGoodsId;

            //非最大售价子单，按比例分摊
            Long itemTotalAmount = NumberUtils.isAboveZero(ordOrderItem.getTotalAmount()) ? ordOrderItem.getTotalAmount() : 0L;
            apportionAmount = calculateAmountByPercent(itemTotalAmount, totalItemSalesPrice, orderApportionAmount);
            leftApportionAmount -= apportionAmount;
            suppGoodsId = ordOrderItem.getSuppGoodsId();
            suppGoodsName =ordOrderItem.getSuppGoodsName();

            log.info("Item is [" + ordOrderItem.getOrderItemId() + "] supp goods id is [" + suppGoodsId + "], name is [" + suppGoodsName + "], apportion amount is [" + apportionAmount
                    + "], type is: " + apportionType);
            //生成分摊信息，写入子单
            generateItemApportionInfo(ordOrderItem, apportionAmount);
            //生成子单分摊情况，并且加入子单
        }
        //生成最大分摊金额子单的分摊信息，写入子单
        generateItemApportionInfo(itemContainMaxSalesPrice, leftApportionAmount);
        log.info("The second circulation completed, order is [" + order.getOrderId() + "], now all " + getApportionType().name() + " apportion ends");
    }

    /**生成子单分摊信息，并且加入到子单中去
     * 包含两个步骤，1. 加入子单的分摊信息
     * 2.加入按粒度划分的分摊信息
    * */
    private void generateItemApportionInfo(OrdOrderItem orderItem, long apportionAmount){
        //加入单的分摊信息
        super.generateItemApportionState(orderItem, apportionAmount);
        //加入按粒度划分的分摊信息
        this.generateOrderCostSharingItemList(orderItem, apportionAmount);
    }

    //生成分摊信息，并且写入到子单中去
    private void generateOrderCostSharingItemList(OrdOrderItem orderItem, long apportionAmount) {
        Long categoryId = orderItem.getCategoryId();
        checkItemOrderCostSharingItemList(orderItem);
        //分摊粒度
        OrderEnum.ORDER_APPORTION_PARTICLE orderApportionParticle = ApportionUtil.judgeApportionParticle(categoryId);
        log.info("Order is " + orderItem.getOrderId() + ", item  is " + orderItem.getOrderItemId()
                + ",goods is " + orderItem.getSuppGoodsId() + ", name is " + orderItem.getSuppGoodsName()
                + ", apportion amount is " + apportionAmount
                + ", category is " + categoryId + ",particle is: " + orderApportionParticle.name());
        //根据分摊粒度获取按粒度分摊的服务
        ApportionParticleService apportionParticleService = particleServiceFactory.catchOrderDetailApportionCompleteService(orderApportionParticle);
        //调用按粒度分摊的服务，得到分摊数据
        List<OrdOrderCostSharingItem> orderCostSharingItemList = apportionParticleService.generateOrdOrderCostSharingItem(orderItem, getApportionType(), apportionAmount);
        //添加到分摊列表
        orderItem.getOrderCostSharingItemList().addAll(orderCostSharingItemList);
    }

    //检查订单的orderCostSharingItemList字段是否为null，如果是，给一个新的List
    private void checkItemOrderCostSharingItemList(OrdOrderItem orderItem) {
        if(orderItem.getOrderCostSharingItemList() == null) {
            orderItem.setOrderCostSharingItemList(new ArrayList<OrdOrderCostSharingItem>());
        }
    }

    /**根据分子、分母、基数，得到价格即返回 分子/分母*基数
     * 由于 分子/分母 会得到一个整型数，会丢失精度，所以先转换一个成浮点型，然后计算
     */
    private long calculateAmountByPercent(long currentLot, long totalLot, long capital){
        return (long) ((float) currentLot/totalLot * capital);
    }
}
