package com.lvmama.vst.order.abs;

import com.lvmama.comm.pet.refund.vo.RefundOrderItemSplit;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.order.PriceTypeNode;
import com.lvmama.vst.comm.vo.order.PriceTypeVO;
import com.lvmama.vst.order.constant.ApportionConstants;
import com.lvmama.vst.order.factory.ApportionCategoryServiceFactory;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.IOrdMulPriceRateService;
import com.lvmama.vst.order.service.apportion.category.OrderAmountApportionPerformer;
import com.lvmama.vst.order.utils.ApportionUtil;
import com.lvmama.vst.order.utils.OrdOrderItemUtils;
import com.lvmama.vst.order.vo.OrderItemApportionInfoRelatedVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhouyanqun on 2017/4/18.
 */
public abstract class ApportionParticleAbsService {
    private static final Log log = LogFactory.getLog(ApportionParticleAbsService.class);
    //获取各个按种类分摊的服务的工厂
    @Resource
    private ApportionCategoryServiceFactory apportionCategoryServiceFactory;

    @Resource
    private IOrdMulPriceRateService ordMulPriceRateService;

    /**根据分子、分母、基数，得到价格即返回 分子/分母*基数
     * 由于 分子/分母 会得到一个整弄数，会丢失精度，所以先转换一个成浮点型，然后计算
     */
    protected long calculateAmountByPercent(long currentLot, long totalLot, long capital){
        return (long) ((float) currentLot/totalLot * capital);
    }

    //拷贝子单的基本信息(即不需要计算就能获取到的信息)
    protected void copyItemInfo(OrdOrderItem orderItem, OrderItemApportionInfoPO orderItemApportionInfoPO) {
        if(orderItem == null) {
            return;
        }
        orderItemApportionInfoPO = orderItemApportionInfoPO == null ? new OrderItemApportionInfoPO() : orderItemApportionInfoPO;

        orderItemApportionInfoPO.setOrderItemId(orderItem.getOrderItemId());
        orderItemApportionInfoPO.setPrice(orderItem.getPrice());
        orderItemApportionInfoPO.setQuantity(orderItem.getQuantity());
        orderItemApportionInfoPO.setCategoryId(orderItem.getCategoryId());
    }

    /**
     * 根据子订单和分摊信息(此处分摊信息是所有的分摊信息，也即包括非此子单的分摊信息，需要过滤)，生成子单分摊信息汇总PO的集合
     * 此方法供除酒店外的其它订单调用，酒店的分摊需要覆盖本方法
     * */
    protected OrderItemApportionInfoPO doGenerateItemApportionInfoPO(OrdOrderItem orderItem, OrderItemApportionInfoRelatedVO orderItemApportionInfoRelatedVO) {
        if(orderItem == null) {
            return null;
        }
        log.info("Now generate item apportion info for item " + orderItem.getOrderItemId());
        OrderItemApportionInfoPO orderItemApportionInfoPO = new OrderItemApportionInfoPO();
        //拷贝基本信息
        copyItemInfo(orderItem, orderItemApportionInfoPO);

        //加入子单分摊情况信息
        assignItemApportionState(orderItem, orderItemApportionInfoRelatedVO, orderItemApportionInfoPO);
        log.info("For now order item " + orderItem.getOrderItemId() + " apportion state has assigned");
        //加入分摊信息和退款信息
        assignItemApportionAndRefundInfo(orderItem, orderItemApportionInfoRelatedVO, orderItemApportionInfoPO);
        log.info("Generate item apportion info for item " + orderItem.getOrderItemId() + " completed, result is " + GsonUtils.toJson(orderItemApportionInfoPO));
        return orderItemApportionInfoPO;
    }

    /**
     * 加入分摊明细和退款信息
     * */
    protected void assignItemApportionAndRefundInfo(OrdOrderItem orderItem, OrderItemApportionInfoRelatedVO orderItemApportionInfoRelatedVO, OrderItemApportionInfoPO orderItemApportionInfoPO) {
        if(orderItemApportionInfoPO == null) {
            return;
        }

        //分摊明细和退款明细
        List<OrdOrderCostSharingItem> orderCostSharingItemList = null;
        List<RefundOrderItemSplit> refundOrderItemSplitList = null;
        if (orderItemApportionInfoRelatedVO != null) {
            orderCostSharingItemList = orderItemApportionInfoRelatedVO.getOrderCostSharingItemList();
            refundOrderItemSplitList = orderItemApportionInfoRelatedVO.getRefundOrderItemSplitList();
        }
        //多价格数据
        List<OrdMulPriceRate> ordMulPriceList = null;
        //酒店套餐、邮轮两个品类的子单比较特殊，多价格与实际分摊产品的多价格不符合，使用后面补充的默认值就可以
        if (!OrdOrderItemUtils.isHotelComboOrderItem(orderItem) && !OrdOrderItemUtils.isShipOrderItem(orderItem)) {
            //根据子单id和多价格类型查询子单的多价格数据
            Map<String, Object> paramsMulPrice = new HashMap<>();
            paramsMulPrice.put("orderItemId", orderItem.getOrderItemId());
            Long categoryId = orderItem.getCategoryId();
            paramsMulPrice.put("priceTypeArray", ApportionUtil.catchRelatedPriceTypeArray(categoryId));
            ordMulPriceList = ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPrice);
        }
        //当多价格表不存在时(目前仅发现演出票没有多价格数据，酒店套餐和邮轮因为多价格与分摊方式不匹配，也不会查询出多价格数据)，补一个默认的多价格数据
        if (CollectionUtils.isEmpty(ordMulPriceList)) {
            if (ordMulPriceList == null) {
                ordMulPriceList = new ArrayList<>();
            }
            OrdMulPriceRate ordMulPriceRate = new OrdMulPriceRate();
            ordMulPriceRate.setPriceType(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name());
            ordMulPriceRate.setPrice(0L);
            ordMulPriceList.add(ordMulPriceRate);
        }

        //遍历，查找多价格对应的分摊信息
        //子单总退款份数、金额
        Long totalRefundAmount = orderItemApportionInfoPO.getTotalRefundAmount() == null ? 0L : orderItemApportionInfoPO.getTotalRefundAmount();
        Long totalRefundQuantity = orderItemApportionInfoPO.getRefundQuantity() == null ? 0L : orderItemApportionInfoPO.getRefundQuantity();
        for (OrdMulPriceRate ordMulPriceRate : ordMulPriceList) {
            if(ordMulPriceRate == null) {
                continue;
            }
            String priceType = ordMulPriceRate.getPriceType();
            //补充价格类型Map及对应的VO
            this.assignPriceTypeVOs(orderItemApportionInfoPO, priceType);
            //优惠价格类型实体
            PriceTypeVO couponPriceTypeVO = orderItemApportionInfoPO.getItemCouponApportionByPriceTypeMap().get(priceType);
            //促销价格类型实体
            PriceTypeVO promotionPriceTypeVO = orderItemApportionInfoPO.getItemPromotionApportionByPriceTypeMap().get(priceType);
            //渠道优惠价格类型实体
            PriceTypeVO distributorPriceTypeVO = orderItemApportionInfoPO.getItemDistributorApportionByPriceTypeMap().get(priceType);
            //手工改价类型实体
            PriceTypeVO manualChangePriceTypeVO = orderItemApportionInfoPO.getItemManualChangeApportionByPriceTypeMap().get(priceType);
            //实付价格类型实体
            PriceTypeVO actualPaidPriceTypeVO = orderItemApportionInfoPO.getItemActualPaidApportionByPriceTypeMap().get(priceType);
            //退款价格类型实体
            PriceTypeNode priceTypeNode = orderItemApportionInfoPO.getItemRefundByPriceTypeMap().get(priceType);
            List<PriceTypeVO> refundPriceTypeVOList = priceTypeNode.getPriceTypeVOList();

           
            //支付立减分摊金额
            PriceTypeVO payReductionTypeVO = orderItemApportionInfoPO.getItemPromApportionPayReductionByPriceTypeMap().get(priceType);

            PriceTypeVO [] priceTypeVOArray = new PriceTypeVO[] {
                    couponPriceTypeVO, promotionPriceTypeVO, distributorPriceTypeVO, manualChangePriceTypeVO, actualPaidPriceTypeVO,payReductionTypeVO

            };
            
            //设定分摊明细
            if(CollectionUtils.isNotEmpty(orderCostSharingItemList)) {
                for (OrdOrderCostSharingItem orderCostSharingItem : orderCostSharingItemList) {
                    //仅仅当子单id，价格类型都匹配时，才会设定值
                    if(orderCostSharingItem == null || !Objects.equals(orderCostSharingItem.getOrderItemId(), orderItem.getOrderItemId())
                            || !StringUtils.equals(priceType, orderCostSharingItem.getCostType())) {
                        continue;
                    }
                    assignAmount(orderCostSharingItem, priceTypeVOArray);

                }
            }

            //设定退款明细
            //结点退款份数、金额
            Long nodeRefundQuantity = priceTypeNode.getQuantity() == null ? 0L : priceTypeNode.getQuantity();
            Long nodeRefundAmount = priceTypeNode.getAmount() == null ? 0L : priceTypeNode.getAmount();
            if (CollectionUtils.isNotEmpty(refundOrderItemSplitList)) {
                for (RefundOrderItemSplit refundOrderItemSplit : refundOrderItemSplitList) {
                    if (refundOrderItemSplit == null || !Objects.equals(refundOrderItemSplit.getOrderItemId(), orderItem.getOrderItemId())) {
                        continue;
                    }

                    String refundPriceValue = refundOrderItemSplit.getRefundPriceValue();
                    //当退款价格类型值是空串时，把空串设定为销售单价格的价格类型
                    if (StringUtils.isBlank(refundPriceValue)) {
                        refundPriceValue = OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCode();
                        refundOrderItemSplit.setRefundPriceValue(refundPriceValue);
                    }

                    if (!StringUtils.equals(priceType, refundPriceValue)) {
                        continue;
                    }

                    //退款份数、金额
                    Long refundQuantity = refundOrderItemSplit.getRefundQuantity() == null ? 0L : refundOrderItemSplit.getRefundQuantity();
                    Long refundPrice = refundOrderItemSplit.getRefundPrice() == null ? 0L : refundOrderItemSplit.getRefundPrice();

                    //累加结点退款金额和份数
                    nodeRefundAmount += refundPrice;
                    nodeRefundQuantity += refundQuantity;

                    //退款明细信息设定
                    refundPriceTypeVOList.add(new PriceTypeVO(refundPrice, refundQuantity, refundPriceValue, OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(refundPriceValue)));
                }
            }
            //回写结点退款金额和份数
            priceTypeNode.setAmount(nodeRefundAmount);
            priceTypeNode.setQuantity(nodeRefundQuantity);
            //累加子单总退款份数、金额
            totalRefundAmount += nodeRefundAmount;
            totalRefundQuantity += nodeRefundQuantity;
        }
        //回写子单总退款金额和份数
        orderItemApportionInfoPO.setTotalRefundAmount(totalRefundAmount);
        orderItemApportionInfoPO.setRefundQuantity(totalRefundQuantity);
    }

    /**
     * 根据分摊信息给值
     * */
    protected void assignAmount(OrdOrderCostSharingItem orderCostSharingItem, PriceTypeVO[] priceTypeVOArray) {
        String costCategory = orderCostSharingItem.getCostCategory();
        OrderEnum.ORDER_APPORTION_TYPE orderApportionType = OrderEnum.ORDER_APPORTION_TYPE.catchByName(costCategory);
        if(orderApportionType == null) {
            return;
        }
        int apportionTypeCode = orderApportionType.getApportionTypeCode();

        /*//TODO支付立减分摊，后期待做
        if(apportionTypeCode==6){
        	return;
        }*/

        PriceTypeVO priceTypeVO = priceTypeVOArray[apportionTypeCode - 1];
        //手工改价，需要累加
        if(apportionTypeCode == 4) {
            priceTypeVO.setPrice(orderCostSharingItem.getAmount() + priceTypeVO.getPrice());
            return;
        }
        //非手工改价，直接覆盖
        priceTypeVO.setPrice(orderCostSharingItem.getAmount());
    }


    //给价格类型对应的PriceType赋值
    private void assignPriceTypeVOs(OrderItemApportionInfoPO orderItemApportionInfoPO, String priceType) {
        if(orderItemApportionInfoPO == null || StringUtils.isBlank(priceType)) {
            return;
        }
        PriceTypeVO priceTypeVO;
        //设定优惠Map对应的价格类型VO
        if(orderItemApportionInfoPO.getItemCouponApportionByPriceTypeMap() == null) {
            orderItemApportionInfoPO.setItemCouponApportionByPriceTypeMap(new HashMap<String, PriceTypeVO>());
        }
        priceTypeVO = orderItemApportionInfoPO.getItemCouponApportionByPriceTypeMap().get(priceType);
        if(priceTypeVO == null) {
            priceTypeVO = new PriceTypeVO(0L, priceType, OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(priceType));
            orderItemApportionInfoPO.getItemCouponApportionByPriceTypeMap().put(priceType, priceTypeVO);
        }
        //设定促销Map对应的价格类型VO
        if(orderItemApportionInfoPO.getItemPromotionApportionByPriceTypeMap() == null) {
            orderItemApportionInfoPO.setItemPromotionApportionByPriceTypeMap(new HashMap<String, PriceTypeVO>());
        }
        priceTypeVO = orderItemApportionInfoPO.getItemPromotionApportionByPriceTypeMap().get(priceType);
        if(priceTypeVO == null) {
            priceTypeVO = new PriceTypeVO(0L, priceType, OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(priceType));
            orderItemApportionInfoPO.getItemPromotionApportionByPriceTypeMap().put(priceType, priceTypeVO);
        }
        //设定渠道优惠Map对应的价格类型VO
        if(orderItemApportionInfoPO.getItemDistributorApportionByPriceTypeMap() == null) {
            orderItemApportionInfoPO.setItemDistributorApportionByPriceTypeMap(new HashMap<String, PriceTypeVO>());
        }
        priceTypeVO = orderItemApportionInfoPO.getItemDistributorApportionByPriceTypeMap().get(priceType);
        if(priceTypeVO == null) {
            priceTypeVO = new PriceTypeVO(0L, priceType, OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(priceType));
            orderItemApportionInfoPO.getItemDistributorApportionByPriceTypeMap().put(priceType, priceTypeVO);
        }
        //设定手工改价Map对应的价格类型VO
        if(orderItemApportionInfoPO.getItemManualChangeApportionByPriceTypeMap() == null) {
            orderItemApportionInfoPO.setItemManualChangeApportionByPriceTypeMap(new HashMap<String, PriceTypeVO>());
        }
        priceTypeVO = orderItemApportionInfoPO.getItemManualChangeApportionByPriceTypeMap().get(priceType);
        if(priceTypeVO == null) {
            priceTypeVO = new PriceTypeVO(0L, priceType, OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(priceType));
            orderItemApportionInfoPO.getItemManualChangeApportionByPriceTypeMap().put(priceType, priceTypeVO);
        }
        //设定实付Map对应的价格类型VO
        if(orderItemApportionInfoPO.getItemActualPaidApportionByPriceTypeMap() == null) {
            orderItemApportionInfoPO.setItemActualPaidApportionByPriceTypeMap(new HashMap<String, PriceTypeVO>());
        }
        priceTypeVO = orderItemApportionInfoPO.getItemActualPaidApportionByPriceTypeMap().get(priceType);
        if(priceTypeVO == null) {
            priceTypeVO = new PriceTypeVO(0L, priceType, OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(priceType));
            orderItemApportionInfoPO.getItemActualPaidApportionByPriceTypeMap().put(priceType, priceTypeVO);
        }
        //设定退款Map对应的价格类型VO的list
        if (orderItemApportionInfoPO.getItemRefundByPriceTypeMap() == null) {
            orderItemApportionInfoPO.setItemRefundByPriceTypeMap(new HashMap<String, PriceTypeNode>());
        }
        PriceTypeNode priceTypeNode = orderItemApportionInfoPO.getItemRefundByPriceTypeMap().get(priceType);
        if (priceTypeNode == null) {
            priceTypeNode = new PriceTypeNode(0L, 0L, priceType, OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(priceType), new ArrayList<PriceTypeVO>());
            orderItemApportionInfoPO.getItemRefundByPriceTypeMap().put(priceType, priceTypeNode);
        }
        List<PriceTypeVO> priceTypeVOList = priceTypeNode.getPriceTypeVOList();
        if (priceTypeVOList == null) {
            priceTypeVOList = new ArrayList<>();
            priceTypeNode.setPriceTypeVOList(priceTypeVOList);
        }

        
        //支付立减金额
        //设定优惠Map对应的价格类型VO
        if(orderItemApportionInfoPO.getItemPromApportionPayReductionByPriceTypeMap() == null) {
            orderItemApportionInfoPO.setItemPromApportionPayReductionByPriceTypeMap(new HashMap<String, PriceTypeVO>());
        }
        priceTypeVO = orderItemApportionInfoPO.getItemPromApportionPayReductionByPriceTypeMap().get(priceType);
        if(priceTypeVO == null) {
            priceTypeVO = new PriceTypeVO(0L, priceType, OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(priceType));
            orderItemApportionInfoPO.getItemPromApportionPayReductionByPriceTypeMap().put(priceType, priceTypeVO);
        }

    }

    //加入子单分摊情况
    private void assignItemApportionState(OrdOrderItem orderItem, OrderItemApportionInfoRelatedVO orderItemApportionInfoRelatedVO, OrderItemApportionInfoPO orderItemApportionInfoPO) {
        if(orderItem == null){
            return;
        }

        if(orderItemApportionInfoPO == null) {
            return;
        }
        List<OrderItemApportionState> orderItemApportionStateList = orderItemApportionInfoRelatedVO.getOrderItemApportionStateList();
        //5种分摊类型
        if (CollectionUtils.isNotEmpty(orderItemApportionStateList)) {
            for (OrderItemApportionState orderItemApportionState : orderItemApportionStateList) {
                if(orderItemApportionState == null || !Objects.equals(orderItemApportionState.getOrderItemId(), orderItem.getOrderItemId())) {
                    continue;
                }

                if (StringUtils.equals(orderItemApportionState.getCostCategory(), OrderEnum.ORDER_APPORTION_TYPE.apportion_type_coupon.name())) {
                    orderItemApportionInfoPO.setItemTotalCouponAmount(orderItemApportionState.getApportionAmount());
                } else if (StringUtils.equals(orderItemApportionState.getCostCategory(), OrderEnum.ORDER_APPORTION_TYPE.apportion_type_promotion.name())) {
                    orderItemApportionInfoPO.setItemTotalPromotionAmount(orderItemApportionState.getApportionAmount());
                } else if (StringUtils.equals(orderItemApportionState.getCostCategory(), OrderEnum.ORDER_APPORTION_TYPE.apportion_type_distributor.name())) {
                    orderItemApportionInfoPO.setItemTotalDistributorAmount(orderItemApportionState.getApportionAmount());
                } else if (StringUtils.equals(orderItemApportionState.getCostCategory(), OrderEnum.ORDER_APPORTION_TYPE.apportion_type_manual.name())) {
                    orderItemApportionInfoPO.setItemTotalManualChangeAmount( orderItemApportionState.getApportionAmount());
                } else if (StringUtils.equals(orderItemApportionState.getCostCategory(), OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment.name())) {
                    orderItemApportionInfoPO.setItemTotalActualPaidAmount(orderItemApportionState.getApportionAmount());
                }else if (StringUtils.equals(orderItemApportionState.getCostCategory(), OrderEnum.ORDER_APPORTION_TYPE.apportion_type_pay_promotion.name())) {
                    orderItemApportionInfoPO.setItemPayReductionAmount(orderItemApportionState.getApportionAmount());

                }
            }
        }
    }

    private void fillNewDate(OrdOrderCostSharingItem orderCostSharingItem){
        if(orderCostSharingItem == null) {
            return;
        }
        Date currentDate = Calendar.getInstance().getTime();
        if(orderCostSharingItem.getCreateDate() == null){
            orderCostSharingItem.setCreateDate(currentDate);
        }
        if(orderCostSharingItem.getUpdateDate() == null || orderCostSharingItem.getUpdateDate().before(currentDate)) {
            orderCostSharingItem.setUpdateDate(currentDate);
        }
    }

    //根据子单，生成分摊实体，实体包含不需要任何计算或者识别就能获取的信息，比如订单号，子单号等
    protected OrdOrderCostSharingItem createOrdOrderCostSharingItem(OrdOrderItem orderItem){
        if(orderItem == null) {
            return null;
        }
        OrdOrderCostSharingItem orderCostSharingItem = new OrdOrderCostSharingItem();
        orderCostSharingItem.setOrderId(orderItem.getOrderId());
        orderCostSharingItem.setOrderItemId(orderItem.getOrderItemId());
        orderCostSharingItem.setStatus(Constants.Y_FLAG);
        fillNewDate(orderCostSharingItem);
        return orderCostSharingItem;
    }

    /**
     * 把分摊信息按照多价格的顺序排列
     * 如果分摊数据有残缺，补上
     * */
    protected void doSortAndCompleteApportionInfoByMulPrice(OrderItemApportionInfoPO orderItemApportionInfoPO, List<OrdMulPriceRate> ordMulPriceList) {
        List<OrdMulPriceRate> filteredOrdMulPriceRateList = filterOutRedundantRate(ordMulPriceList);
        log.info("Order item [" + orderItemApportionInfoPO.getOrderItemId() + "], filtered mul price list is " + GsonUtils.toJson(filteredOrdMulPriceRateList));
        //优惠分摊服务
        OrderAmountApportionPerformer couponApportionPerformer = apportionCategoryServiceFactory.catchApportionPerformerByCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_coupon);
        //促销分摊服务
        OrderAmountApportionPerformer orderPromotionApportionPerformer = apportionCategoryServiceFactory.catchApportionPerformerByCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_promotion);
        //渠道优惠分摊服务
        OrderAmountApportionPerformer orderDistributorApportionPerformer = apportionCategoryServiceFactory.catchApportionPerformerByCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_distributor);
        //手工改价分摊服务
        OrderAmountApportionPerformer orderManualChangeApportionPerformer = apportionCategoryServiceFactory.catchApportionPerformerByCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_manual);
        //实付分摊服务
        OrderAmountApportionPerformer orderActualPaidApportionPerformer = apportionCategoryServiceFactory.catchApportionPerformerByCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment);
        
        //支付立减分摊
        OrderAmountApportionPerformer orderReductPaidApportionPerformer = apportionCategoryServiceFactory.catchApportionPerformerByCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_pay_promotion);
        couponApportionPerformer.sortAndCompleteApportionInfoByMulPrice(orderItemApportionInfoPO, filteredOrdMulPriceRateList);
        orderPromotionApportionPerformer.sortAndCompleteApportionInfoByMulPrice(orderItemApportionInfoPO, filteredOrdMulPriceRateList);
        orderDistributorApportionPerformer.sortAndCompleteApportionInfoByMulPrice(orderItemApportionInfoPO, filteredOrdMulPriceRateList);
        orderManualChangeApportionPerformer.sortAndCompleteApportionInfoByMulPrice(orderItemApportionInfoPO, filteredOrdMulPriceRateList);
        orderActualPaidApportionPerformer.sortAndCompleteApportionInfoByMulPrice(orderItemApportionInfoPO, filteredOrdMulPriceRateList);
        orderReductPaidApportionPerformer.sortAndCompleteApportionInfoByMulPrice(orderItemApportionInfoPO, filteredOrdMulPriceRateList);
    }

    //过滤掉多余的多价格数据
    protected List<OrdMulPriceRate> filterOutRedundantRate(List<OrdMulPriceRate> ordMulPriceRateList) {
        if(CollectionUtils.isEmpty(ordMulPriceRateList)) {
            return new ArrayList<>();
        }

        List<OrdMulPriceRate> filteredOrdMulPriceRateList = new ArrayList<>();
        for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {
            String priceType = ordMulPriceRate.getPriceType();
            if(isApportionPriceType(priceType)) {
                filteredOrdMulPriceRateList.add(ordMulPriceRate);
            }
        }
        return filteredOrdMulPriceRateList;
    }

    /**
     * 判断价格类型是否与分摊有关
     * */
    protected boolean isApportionPriceType(String priceType) {
        for (String validPriceType : getAllValidPriceType()) {
            if(StringUtils.equals(validPriceType, priceType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 给出所有合法的价格类型
     * */
    protected abstract String[] getAllValidPriceType();
}
