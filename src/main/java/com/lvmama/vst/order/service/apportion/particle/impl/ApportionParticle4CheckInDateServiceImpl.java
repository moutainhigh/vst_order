package com.lvmama.vst.order.service.apportion.particle.impl;

import com.lvmama.comm.pet.refund.vo.RefundOrderItemSplit;
import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.order.PriceTypeNode;
import com.lvmama.vst.comm.vo.order.PriceTypeVO;
import com.lvmama.vst.order.abs.ApportionParticleMeticulousAbsService;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.IOrdOrderHotelTimeRateService;
import com.lvmama.vst.order.service.apportion.particle.ApportionParticleService;
import com.lvmama.vst.order.utils.ApportionUtil;
import com.lvmama.vst.order.vo.OrderItemApportionInfoRelatedVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhouyanqun on 2017/4/18.
 * 按入住记录分摊的子单的分摊信息补全服务
 */
@Service("apportionComplete4CheckInDateServiceImpl")
public class ApportionParticle4CheckInDateServiceImpl extends ApportionParticleMeticulousAbsService implements ApportionParticleService {
    private static final Log log = LogFactory.getLog(ApportionParticle4CheckInDateServiceImpl.class);

    @Resource
    private IOrdOrderHotelTimeRateService orderHotelTimeRateService;

    /**
     * 根据子订单和分摊金额，分摊类型，生成分摊信息集合
     *
     * @param ordOrderItem
     * @param orderApportionType 分摊类型
     * @param apportionAmount
     */
    @Override
    public List<OrdOrderCostSharingItem> generateOrdOrderCostSharingItem(OrdOrderItem ordOrderItem, OrderEnum.ORDER_APPORTION_TYPE orderApportionType, long apportionAmount) {
        List<OrdOrderHotelTimeRate> orderHotelTimeRateList = ordOrderItem.getOrderHotelTimeRateList();
        List<OrdOrderCostSharingItem> orderCostSharingItemList = new ArrayList<>();
        //剩下未分摊的金额，因为最后一个子单需要做减法得到，所以每分摊一点金额，就把这个变量减掉分摊掉的金额，等到了最后一个子单，这个数值就是分摊到的金额
        long leftApportionAmount = apportionAmount;
        log.info("now generate order cost sharing item for item " + ordOrderItem.getOrderItemId() + ", orderHotelTimeRateList size is " + orderHotelTimeRateList.size() + ", apportionAmount is " + apportionAmount);

        //销售总价，最大销售价
        long totalOrdHotelTimeRatePrice = 0, maxOrdHotelTimeRatePrice = 0;
        //包含最大销售价的子单，其它子单按比例摊总优惠金额，这个子单摊其它子单摊剩下的金额
        OrdOrderHotelTimeRate rateContainMaxSalesPrice = orderHotelTimeRateList.get(0);
        //价格项的单价、数量、总价
        long ratePrice, quantity, rateTotalAmount;
        //第一遍循环，算出总价、售价最高的价格项
        for (OrdOrderHotelTimeRate orderHotelTimeRate : orderHotelTimeRateList) {
            ratePrice = NumberUtils.isAboveZero(orderHotelTimeRate.getPrice()) ? orderHotelTimeRate.getPrice() : 0;
            quantity = NumberUtils.isAboveZero(orderHotelTimeRate.getQuantity()) ? orderHotelTimeRate.getQuantity() : 0;
            rateTotalAmount = ratePrice * quantity;
            totalOrdHotelTimeRatePrice += rateTotalAmount;
            //如果当前子单的售价大于已有的最大总价，则把最大售价以及包含最大售价的子单变量更新
            if(ratePrice > maxOrdHotelTimeRatePrice) {
                //如果当前子单的售价大于已有的最大总价，则把最大售价以及包含最大售价的子单变量更新
                maxOrdHotelTimeRatePrice = ratePrice;
                rateContainMaxSalesPrice = orderHotelTimeRate;
            }
        }

        //第二遍循环，分摊优惠总额
        for (OrdOrderHotelTimeRate orderHotelTimeRate : orderHotelTimeRateList) {
            if(orderHotelTimeRate == rateContainMaxSalesPrice){
                //最大售价子单，先不计算，等循环结束后，剩下的金额就是
                continue;
            }

            //非最大售价子单，按比例分摊
            ratePrice = NumberUtils.isAboveZero(orderHotelTimeRate.getPrice()) ? orderHotelTimeRate.getPrice() : 0;
            quantity = NumberUtils.isAboveZero(orderHotelTimeRate.getQuantity()) ? orderHotelTimeRate.getQuantity() : 0;
            rateTotalAmount = ratePrice * quantity;
            //分摊金额
            long ordHotelTimeRateApportionAmount = calculateAmountByPercent(rateTotalAmount, totalOrdHotelTimeRatePrice, apportionAmount);
            leftApportionAmount -= ordHotelTimeRateApportionAmount;

            //生成分摊信息(不包含最大分摊项的)，加入列表
            OrdOrderCostSharingItem orderCostSharingItem = createOrdOrderCostSharingItem(ordOrderItem);
            orderCostSharingItem.setCostCategory(orderApportionType.name());
            orderCostSharingItem.setCostType(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCode());
            orderCostSharingItem.setPurpose(DateUtil.formatDate(orderHotelTimeRate.getVisitTime(), DateUtil.SIMPLE_DATE_FORMAT));
            orderCostSharingItem.setAmount(ordHotelTimeRateApportionAmount);
            log.info("Generated order cost sharing item for order " + ordOrderItem.getOrderId() + ", item " + ordOrderItem.getOrderItemId() + " is " + GsonUtils.toJson(orderCostSharingItem));
            orderCostSharingItemList.add(orderCostSharingItem);
        }

        //生成最大分摊项的分摊信息，加入列表
        OrdOrderCostSharingItem orderCostSharingItem = createOrdOrderCostSharingItem(ordOrderItem);
        orderCostSharingItem.setCostCategory(orderApportionType.name());
        orderCostSharingItem.setCostType(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCode());
        orderCostSharingItem.setPurpose(DateUtil.formatDate(rateContainMaxSalesPrice.getVisitTime(), DateUtil.SIMPLE_DATE_FORMAT));
        orderCostSharingItem.setAmount(leftApportionAmount);
        log.info("Generated max amount order cost sharing item for order " + ordOrderItem.getOrderId() + ", item " + ordOrderItem.getOrderItemId() + " is " + GsonUtils.toJson(orderCostSharingItem));
        orderCostSharingItemList.add(orderCostSharingItem);
        log.info("now completed generate order cost sharing item for item " + ordOrderItem.getOrderItemId() + ", orderCostSharingItemList size is " + orderCostSharingItemList.size() + ", apportionAmount is " + apportionAmount);
        return orderCostSharingItemList;
    }

    /**
     * 根据子订单和分摊信息(此处分摊信息是所有的分摊信息，也即包括非此子单的分摊信息，需要过滤)，生成子单分摊信息汇总PO的集合
     *
     * @param orderItem
     * @param orderCostSharingItemList
     */
    @Override
    public OrderItemApportionInfoPO generateItemApportionInfoPO(OrdOrderItem orderItem, OrderItemApportionInfoRelatedVO orderItemApportionInfoRelatedVO) {
        return super.doGenerateItemApportionInfoPO(orderItem, orderItemApportionInfoRelatedVO);
    }

    //加入分摊信息
    @Override
    protected void assignItemApportionAndRefundInfo(OrdOrderItem orderItem, OrderItemApportionInfoRelatedVO orderItemApportionInfoRelatedVO, OrderItemApportionInfoPO orderItemApportionInfoPO) {
        if(orderItemApportionInfoPO == null) {
            return;
        }
        //分摊、退款明细
        List<OrdOrderCostSharingItem> orderCostSharingItemList = null;
        List<RefundOrderItemSplit> refundOrderItemSplitList = null;
        if (orderItemApportionInfoRelatedVO != null) {
            orderCostSharingItemList = orderItemApportionInfoRelatedVO.getOrderCostSharingItemList();
            refundOrderItemSplitList = orderItemApportionInfoRelatedVO.getRefundOrderItemSplitList();
        }

        //根据子单id查询入住记录明细
        Map<String, Object> param = new HashMap<>();
        param.put("orderItemId", orderItem.getOrderItemId());
        List<OrdOrderHotelTimeRate> ordOrderHotelTimeRateList = orderHotelTimeRateService.findOrdOrderHotelTimeRateList(param);
        if (CollectionUtils.isEmpty(ordOrderHotelTimeRateList)) {
            return;
        }

        //遍历，查找入住记录对应的分摊信息
        //子单总退款份数和金额
        Long totalRefundAmount = orderItemApportionInfoPO.getTotalRefundAmount() == null ? 0L : orderItemApportionInfoPO.getTotalRefundAmount();
        Long totalRefundQuantity = orderItemApportionInfoPO.getRefundQuantity() == null ? 0L : orderItemApportionInfoPO.getRefundQuantity();
        for (OrdOrderHotelTimeRate orderHotelTimeRate : ordOrderHotelTimeRateList) {
            if(orderHotelTimeRate == null) {
                continue;
            }
            Date visitTime = orderHotelTimeRate.getVisitTime();
            //补充价格类型vo
            this.assignCheckInDateVOs(orderItemApportionInfoPO, visitTime);
            //优惠价格类型实体
            PriceTypeVO couponPriceTypeVO = orderItemApportionInfoPO.getItemCouponApportionByCheckInMap().get(visitTime);
            //促销价格类型实体
            PriceTypeVO promotionPriceTypeVO = orderItemApportionInfoPO.getItemPromotionApportionByCheckInMap().get(visitTime);
            //渠道优惠价格类型实体
            PriceTypeVO distributorPriceTypeVO = orderItemApportionInfoPO.getItemDistributorApportionByCheckInMap().get(visitTime);
            //手工改价类型实体
            PriceTypeVO manualChangePriceTypeVO = orderItemApportionInfoPO.getItemManualChangeApportionByCheckInMap().get(visitTime);
            //实付价格类型实体
            PriceTypeVO actualPaidPriceTypeVO = orderItemApportionInfoPO.getItemActualPaidApportionByCheckInMap().get(visitTime);
            //退款价格类型实体
            PriceTypeNode priceTypeNode = orderItemApportionInfoPO.getItemRefundByCheckInMap().get(visitTime);
            List<PriceTypeVO> refundPriceTypeVOList = priceTypeNode.getPriceTypeVOList();
            
            //优惠价格类型实体
            PriceTypeVO reductPriceTypeVO = orderItemApportionInfoPO.getItemReductApportionByCheckInMap().get(visitTime);

            //设定分摊明细
            if (CollectionUtils.isNotEmpty(orderCostSharingItemList)) {
                for (OrdOrderCostSharingItem orderCostSharingItem : orderCostSharingItemList) {
                    //仅仅当子单id，价格类型都匹配时，才会设定值
                    if(orderCostSharingItem == null || !Objects.equals(orderCostSharingItem.getOrderItemId(), orderItem.getOrderItemId())
                            || !Objects.equals(visitTime, DateUtil.toDate(orderCostSharingItem.getPurpose(), DateUtil.SIMPLE_DATE_FORMAT))) {
                        continue;
                    }
                    String costCategory = orderCostSharingItem.getCostCategory();
                    OrderEnum.ORDER_APPORTION_TYPE orderApportionType = OrderEnum.ORDER_APPORTION_TYPE.catchByName(costCategory);
                    if(orderApportionType == null) {
                        continue;
                    }
                    PriceTypeVO [] priceTypeVOArray = new PriceTypeVO[] {
                            couponPriceTypeVO, promotionPriceTypeVO, distributorPriceTypeVO, manualChangePriceTypeVO, actualPaidPriceTypeVO,reductPriceTypeVO
                    };

                    assignAmount(orderCostSharingItem, priceTypeVOArray);
                }
            }
            //设定退款明细
            //结点总退款份数和金额
            Long nodeRefundAmount = priceTypeNode.getAmount() == null ? 0L : priceTypeNode.getAmount();
            Long nodeRefundQuantity = priceTypeNode.getQuantity() == null ? 0L : priceTypeNode.getQuantity();
            if (CollectionUtils.isNotEmpty(refundOrderItemSplitList)) {
                for (RefundOrderItemSplit refundOrderItemSplit : refundOrderItemSplitList) {
                    if (refundOrderItemSplit == null || !Objects.equals(refundOrderItemSplit.getOrderItemId(), orderItem.getOrderItemId())
                            || StringUtils.isBlank(refundOrderItemSplit.getRefundPriceValue())) {
                        continue;
                    }

                    Date refundDate = DateUtil.toDate(refundOrderItemSplit.getRefundPriceValue(), DateUtil.SIMPLE_DATE_FORMAT);
                    //日期不匹配时，执行下一次遍历
                    if (!Objects.equals(visitTime, refundDate)) {
                        continue;
                    }

                    //退款份数、金额
                    Long refundQuantity = refundOrderItemSplit.getRefundQuantity() == null ? 0L : refundOrderItemSplit.getRefundQuantity();
                    Long refundPrice = refundOrderItemSplit.getRefundPrice() == null ? 0L : refundOrderItemSplit.getRefundPrice();

                    //累加结点退款金额和份数
                    nodeRefundAmount += refundPrice;
                    nodeRefundQuantity += refundQuantity;

                    //退款明细信息设定
                    refundPriceTypeVOList.add(new PriceTypeVO(refundPrice, refundQuantity, OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCnName()));
                }
            }
            //回写结点退款金额和份数
            priceTypeNode.setAmount(nodeRefundAmount);
            priceTypeNode.setQuantity(nodeRefundQuantity);
            //累加子单退款金额和份数
            totalRefundAmount += nodeRefundAmount;
            totalRefundQuantity += nodeRefundQuantity;
        }
        //回写子单退款金额和份数
        orderItemApportionInfoPO.setRefundQuantity(totalRefundQuantity);
        orderItemApportionInfoPO.setTotalRefundAmount(totalRefundAmount);
    }

    /**
     * 分配入住日期对应的实体
     * */
    private void assignCheckInDateVOs(OrderItemApportionInfoPO orderItemApportionInfoPO, Date visitTime) {

        if(orderItemApportionInfoPO == null || visitTime == null) {
            return;
        }
        PriceTypeVO priceTypeVO;
        //设定优惠Map对应的价格类型VO
        if(orderItemApportionInfoPO.getItemCouponApportionByCheckInMap() == null) {
            orderItemApportionInfoPO.setItemCouponApportionByCheckInMap(new HashMap<Date, PriceTypeVO>());
        }
        priceTypeVO = orderItemApportionInfoPO.getItemCouponApportionByCheckInMap().get(visitTime);
        if(priceTypeVO == null) {
            priceTypeVO = generateZeroPriceTypeVO();
            orderItemApportionInfoPO.getItemCouponApportionByCheckInMap().put(visitTime, priceTypeVO);
        }
        //设定促销Map对应的价格类型VO
        if(orderItemApportionInfoPO.getItemPromotionApportionByCheckInMap() == null) {
            orderItemApportionInfoPO.setItemPromotionApportionByCheckInMap(new HashMap<Date, PriceTypeVO>());
        }
        priceTypeVO = orderItemApportionInfoPO.getItemPromotionApportionByCheckInMap().get(visitTime);
        if(priceTypeVO == null) {
            priceTypeVO = generateZeroPriceTypeVO();
            orderItemApportionInfoPO.getItemPromotionApportionByCheckInMap().put(visitTime, priceTypeVO);
        }
        //设定渠道优惠Map对应的价格类型VO
        if(orderItemApportionInfoPO.getItemDistributorApportionByCheckInMap() == null) {
            orderItemApportionInfoPO.setItemDistributorApportionByCheckInMap(new HashMap<Date, PriceTypeVO>());
        }
        priceTypeVO = orderItemApportionInfoPO.getItemDistributorApportionByCheckInMap().get(visitTime);
        if(priceTypeVO == null) {
            priceTypeVO = generateZeroPriceTypeVO();
            orderItemApportionInfoPO.getItemDistributorApportionByCheckInMap().put(visitTime, priceTypeVO);
        }
        //设定手工改价Map对应的价格类型VO
        if(orderItemApportionInfoPO.getItemManualChangeApportionByCheckInMap() == null) {
            orderItemApportionInfoPO.setItemManualChangeApportionByCheckInMap(new HashMap<Date, PriceTypeVO>());
        }
        priceTypeVO = orderItemApportionInfoPO.getItemManualChangeApportionByCheckInMap().get(visitTime);
        if(priceTypeVO == null) {
            priceTypeVO = generateZeroPriceTypeVO();
            orderItemApportionInfoPO.getItemManualChangeApportionByCheckInMap().put(visitTime, priceTypeVO);
        }
        //设定实付Map对应的价格类型VO
        if(orderItemApportionInfoPO.getItemActualPaidApportionByCheckInMap() == null) {
            orderItemApportionInfoPO.setItemActualPaidApportionByCheckInMap(new HashMap<Date, PriceTypeVO>());
        }
        priceTypeVO = orderItemApportionInfoPO.getItemActualPaidApportionByCheckInMap().get(visitTime);
        if(priceTypeVO == null) {
            priceTypeVO = generateZeroPriceTypeVO();
            orderItemApportionInfoPO.getItemActualPaidApportionByCheckInMap().put(visitTime, priceTypeVO);
        }
        //设定退款Map对应的价格类型VO
        if (orderItemApportionInfoPO.getItemRefundByCheckInMap() == null) {
            orderItemApportionInfoPO.setItemRefundByCheckInMap(new HashMap<Date, PriceTypeNode>());
        }
        PriceTypeNode priceTypeNode = orderItemApportionInfoPO.getItemRefundByCheckInMap().get(visitTime);
        if (priceTypeNode == null) {
            priceTypeNode = new PriceTypeNode(0L, 0L,OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCode(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCnName(), new ArrayList<PriceTypeVO>());
            orderItemApportionInfoPO.getItemRefundByCheckInMap().put(visitTime, priceTypeNode);
        }
        if (priceTypeNode.getPriceTypeVOList() == null) {
            priceTypeNode.setPriceTypeVOList(new ArrayList<PriceTypeVO>());
        }
        
        //设定支付立减分摊Map对应的价格类型VO
        if(orderItemApportionInfoPO.getItemReductApportionByCheckInMap()== null) {
            orderItemApportionInfoPO.setItemReductApportionByCheckInMap(new HashMap<Date, PriceTypeVO>());
        }
        priceTypeVO = orderItemApportionInfoPO.getItemReductApportionByCheckInMap().get(visitTime);
        if(priceTypeVO == null) {
            priceTypeVO = generateZeroPriceTypeVO();
            orderItemApportionInfoPO.getItemReductApportionByCheckInMap().put(visitTime, priceTypeVO);
        }
    }

    /**
     * 生成值为0的priceTypeVO
     * */
    private PriceTypeVO generateZeroPriceTypeVO(){
        return new PriceTypeVO(0L, OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCnName());
    }

    /**
     * 生成订单的实付金额分摊信息，实付金额比较特殊，用总价-优惠分摊-促销分摊-渠道优惠分摊-手工改价分摊
     *
     * @param ordOrderItem
     */
    @Override
    public List<OrdOrderCostSharingItem> generateOrdOrderCostSharingItem4Paid(OrdOrderItem ordOrderItem) {
        if(CollectionUtils.isEmpty(ordOrderItem.getOrderHotelTimeRateList())) {
            return null;
        }
        List<OrdOrderCostSharingItem> orderCostSharingItemList = new ArrayList<>();
        for (OrdOrderHotelTimeRate ordOrderHotelTimeRate : ordOrderItem.getOrderHotelTimeRateList()) {
            long totalTimeAmount = ordOrderHotelTimeRate.getPrice() * ordOrderHotelTimeRate.getQuantity();
            //分摊金额，包括优惠分摊金额、促销分摊金额、渠道优惠分摊金额、手工改价分摊金额
            long beforePaymentApportionAmount = 0;
            if (CollectionUtils.isNotEmpty(ordOrderItem.getOrderCostSharingItemList())) {
                for (OrdOrderCostSharingItem orderCostSharingItem : ordOrderItem.getOrderCostSharingItemList()) {
                    if(!Objects.equals(ordOrderHotelTimeRate.getVisitTime(), DateUtil.toDate(orderCostSharingItem.getPurpose(), DateUtil.SIMPLE_DATE_FORMAT))) {
                        continue;
                    }
                    if(orderCostSharingItem==null||orderCostSharingItem.getCostCategory()==null||ApportionUtil.isActualPaymentApportionType(orderCostSharingItem.getCostCategory())) {
                        continue;
                    }
                    beforePaymentApportionAmount += orderCostSharingItem.getAmount();
                }
            }
            long actualPaidApportionAmount = totalTimeAmount - beforePaymentApportionAmount;
            OrdOrderCostSharingItem ordOrderCostSharingItem = createOrdOrderCostSharingItem(ordOrderItem);
            ordOrderCostSharingItem.setCostCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment.name());
            ordOrderCostSharingItem.setPurpose(DateUtil.formatDate(ordOrderHotelTimeRate.getVisitTime(), DateUtil.SIMPLE_DATE_FORMAT));
            ordOrderCostSharingItem.setAmount(actualPaidApportionAmount);
            orderCostSharingItemList.add(ordOrderCostSharingItem);
        }
        return orderCostSharingItemList;
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
     * 给出所有合法的价格类型
     */
    @Override
    protected String[] getAllValidPriceType() {
        return new String[]{OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name()};
    }
}
