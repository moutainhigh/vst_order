package com.lvmama.vst.order.web.service.impl;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.vo.OrderCostSharingItemQueryVO;
import com.lvmama.vst.back.order.vo.OrderItemApportionStateQueryVO;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.OrderHotelTimeRateInfo;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.comm.vo.order.PriceTypeNode;
import com.lvmama.vst.comm.vo.order.PriceTypeVO;
import com.lvmama.vst.order.factory.ApportionParticleServiceFactory;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.OrdOrderCostSharingItemService;
import com.lvmama.vst.order.service.OrderItemApportionStateService;
import com.lvmama.vst.order.service.apportion.ApportionInfoQueryService;
import com.lvmama.vst.order.service.apportion.particle.ApportionParticleService;
import com.lvmama.vst.order.utils.ApportionUtil;
import com.lvmama.vst.order.vo.OrderItemApportionInfoQueryVO;
import com.lvmama.vst.order.web.service.OrderDetailApportionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhouyanqun on 2017/4/18.
 */
@Service
public class OrderDetailApportionServiceImpl implements OrderDetailApportionService {
    private static final Log log = LogFactory.getLog(OrderDetailApportionServiceImpl.class);
    @Resource
    private ApportionParticleServiceFactory apportionParticleServiceFactory;
    @Resource
    private OrdOrderCostSharingItemService orderCostSharingItemService;
    @Resource
    private OrderItemApportionStateService orderItemApportionStateService;
    @Resource
    private ApportionInfoQueryService apportionInfoQueryService;
    @Resource
    private SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;

    /**
     * 计算订单详情中，子单的分摊信息
     * 目前订单详情中，子单的信息只有一列：实付金额
     *
     * @param orderId
     * @param resultMap 订单详情中，子单按品类归类的Map，key的值是子单品类string，值是子单信息实体
     * @param itemIdWithMulPriceMap 多价格Map，用于对分摊信息排序，key是子单id，值是对应的多价格
     */
    @Override
        //判断分摊开关是否开启
    public void calcOrderDetailItemApportion(Long orderId, Map<String, List<OrderMonitorRst>> resultMap, Map<Long, List<OrdMulPriceRate>> itemIdWithMulPriceMap) {
        if(!ApportionUtil.isApportionEnabled()) {
            log.info("Apportion is not enabled, can't calculate order detail item apportion, please check");
            return;
        }

        if(MapUtils.isEmpty(resultMap)) {
            return;
        }
        log.info("Collect order monitor list for order " + orderId);

        //按多价格分摊的子单id集合
        List<Long> apportionByMulPriceItemIdList = new ArrayList<>();
        //按多价格分摊的子单实体集合，计算时，直接修改这个Map中的数据，resultMap的值因为是同一块内存，也就跟着改了
        Map<String, List<OrderMonitorRst>> apportionByMulPriceRstMap = new HashMap<>();
        //不按多价格分摊的子单id集合，由于按多价格分摊的子单，分摊金额显示方式与其它子单不一样，所以需要区分开来
        List<Long> apportionNotByMulPriceItemIdList = new ArrayList<>();
        //不按多价格分摊的子单实体集合，计算时，直接修改这个Map中的数据，resultMap的值因为是同一块内存，也就跟着改了
        Map<String, List<OrderMonitorRst>> apportionNotByMulPriceRstMap = new HashMap<>();
        
        //支付立减分摊总金额不按多价格
        Map<String, List<OrderMonitorRst>> payApportionNotByMulPriceRstMap = new HashMap<>();
        //支付立减分摊总金额按多价格
        Map<String, List<OrderMonitorRst>> payApportionByMulPriceRstMap = new HashMap<>();
        //遍历所有子单，把子单id归类
        for (String categoryIdStr : resultMap.keySet()) {
            if(StringUtils.isBlank(categoryIdStr)) {
                continue;
            }
            Long categoryId = Long.valueOf(categoryIdStr);
            if(NumberUtils.isNotAboveZero(categoryId)) {
                continue;
            }
            OrderEnum.ORDER_APPORTION_PARTICLE orderApportionParticle = ApportionUtil.judgeApportionParticle(categoryId);
            if(ApportionUtil.isPriceTypeParticle(orderApportionParticle)) {
                //按多价格分摊的子单，把id加入到apportionByMulPriceItemIdList中去
                addItemIdToList(apportionByMulPriceItemIdList, resultMap.get(categoryIdStr));
                apportionByMulPriceRstMap.put(categoryIdStr, resultMap.get(categoryIdStr));
                payApportionByMulPriceRstMap.put(categoryIdStr, resultMap.get(categoryIdStr));
            } else {
                //不按多价格分摊的子单，把id加入到apportionNotByMulPriceItemIdList中去，可能会包含不用分摊的子单，
                //比如保险和快递，但为了提升性能，不过滤这些子单，因为分摊信息表中不会有这些子单的记录
                addItemIdToList(apportionNotByMulPriceItemIdList, resultMap.get(categoryIdStr));
                apportionNotByMulPriceRstMap.put(categoryIdStr, resultMap.get(categoryIdStr));
                payApportionNotByMulPriceRstMap.put(categoryIdStr, resultMap.get(categoryIdStr));
            }
        }
        log.info("Collect order monitor list for order " + orderId + " completed");
        String apportionByMulPriceItemIdListJson = GsonUtils.toJson(apportionByMulPriceItemIdList);
        String apportionNotByMulPriceItemIdListJson = GsonUtils.toJson(apportionNotByMulPriceItemIdList);
        log.info("Begin to gather apportion info for order " + orderId + ", apportionByMulPriceItemIdList is " + apportionByMulPriceItemIdListJson
                + ", apportionNotByMulPriceItemIdList is " + apportionNotByMulPriceItemIdListJson);
        //计算按多价格分摊的子单的分摊信息
        calcRstMapApportionByPriceType(apportionByMulPriceRstMap, itemIdWithMulPriceMap, apportionByMulPriceItemIdList);
        //计算不按多价格分摊的子单的分摊信息
        calcRstMapApportionNotByPriceType(apportionNotByMulPriceRstMap, apportionNotByMulPriceItemIdList);
        log.info("Gather apportion info for order " + orderId + " completed, apportionByMulPriceItemIdList is " + apportionByMulPriceItemIdListJson
                + ", apportionNotByMulPriceItemIdList is " + apportionNotByMulPriceItemIdListJson);
        
        //计算支付立减不按多价格分摊
        calcRstMapPayApportionNotByPriceType(payApportionNotByMulPriceRstMap, apportionNotByMulPriceItemIdList);
       
        //计算支付立减按多价格分摊
        calcRstMapPayApportionByPriceType(payApportionByMulPriceRstMap, itemIdWithMulPriceMap, apportionByMulPriceItemIdList);
    }
    
  //计算支付立减按多价格分摊
    private void calcRstMapPayApportionByPriceType(Map<String, List<OrderMonitorRst>> payApportionByMulPriceRstMap,Map<Long, List<OrdMulPriceRate>> itemIdWithMulPriceMap, List<Long> apportionByMulPriceItemIdList) {
    	 if(MapUtils.isEmpty(payApportionByMulPriceRstMap)) {
             return;
         }
         log.info("Now calculate apportion info for order detail by price type, apportionByMulPriceItemIdList is " + GsonUtils.toJson(apportionByMulPriceItemIdList));
         if(CollectionUtils.isEmpty(apportionByMulPriceItemIdList)) {
             //传个空值进去，方法会分配0值给子单
        	 assignPayApportionInfoByPriceType(payApportionByMulPriceRstMap, itemIdWithMulPriceMap,  null);
             return;
         }

         OrderCostSharingItemQueryVO orderCostSharingItemQueryVO = new OrderCostSharingItemQueryVO();
         orderCostSharingItemQueryVO.setOrderItemIdList(apportionByMulPriceItemIdList);
         orderCostSharingItemQueryVO.setCostCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_pay_promotion.name());
         List<OrdOrderCostSharingItem> orderCostSharingItemList = orderCostSharingItemService.queryOrdOrderCostSharingItemList(orderCostSharingItemQueryVO);
         assignPayApportionInfoByPriceType(payApportionByMulPriceRstMap, itemIdWithMulPriceMap, orderCostSharingItemList);
         log.info("Calculate apportion info for order detail by price type completed, orderCostSharingItemList is " + GsonUtils.toJson(orderCostSharingItemList));
		
	}

	/***
     * 计算支付立减不按多价格分摊
     * @param payApportionNotByMulPriceRstMap
     * @param apportionNotByMulPriceItemIdList
     */
    private void calcRstMapPayApportionNotByPriceType(Map<String, List<OrderMonitorRst>> payApportionNotByMulPriceRstMap,List<Long> apportionNotByMulPriceItemIdList) {
    	if(MapUtils.isEmpty(payApportionNotByMulPriceRstMap)) {
            return;
        }
        log.info("Now calculate apportion info for order detail not by price type, apportionNotByMulPriceItemIdList is " + GsonUtils.toJson(apportionNotByMulPriceItemIdList));
        //如果子单id集合为空，给所有子单根本0值，返回
        if(CollectionUtils.isEmpty(apportionNotByMulPriceItemIdList)) {
            //传入一个空的orderItemApportionStateList，方法会赋0值
        	assignPayApportionInfoNotByPriceType(payApportionNotByMulPriceRstMap, null);
            return;
        }
        //根据条件查询所有子单实付分摊情况list，先查后关联
        OrderItemApportionStateQueryVO orderItemApportionStateQueryVO = new OrderItemApportionStateQueryVO();
        orderItemApportionStateQueryVO.setOrderItemIdList(apportionNotByMulPriceItemIdList);
        orderItemApportionStateQueryVO.setCostCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_pay_promotion.name());
        List<OrderItemApportionState> orderItemApportionStateList = orderItemApportionStateService.queryOrderItemApportionStateList(orderItemApportionStateQueryVO);

        //遍历子单实体，把子单与子单分摊情况关联，如果子单没有查询到对应的分摊情况，设定一个空值
        assignPayApportionInfoNotByPriceType(payApportionNotByMulPriceRstMap, orderItemApportionStateList);

        log.info("Calculate apportion info for order detail not by price type completed, orderItemApportionStateList is " + GsonUtils.toJson(orderItemApportionStateList));
    }

	/**
     * 计算不按多价格分摊的子单的分摊信息
     * @param apportionNotByMulPriceRstMap 不按多价格分摊的子单实体Map，可能包含不需要分摊的子单
     * @param apportionNotByMulPriceItemIdList 不按多价格分摊的子单id集合
     * */
    private void calcRstMapApportionNotByPriceType(Map<String, List<OrderMonitorRst>> apportionNotByMulPriceRstMap, List<Long> apportionNotByMulPriceItemIdList) {
        if(MapUtils.isEmpty(apportionNotByMulPriceRstMap)) {
            return;
        }
        log.info("Now calculate apportion info for order detail not by price type, apportionNotByMulPriceItemIdList is " + GsonUtils.toJson(apportionNotByMulPriceItemIdList));
        //如果子单id集合为空，给所有子单根本0值，返回
        if(CollectionUtils.isEmpty(apportionNotByMulPriceItemIdList)) {
            //传入一个空的orderItemApportionStateList，方法会赋0值
            assignApportionInfoNotByPriceType(apportionNotByMulPriceRstMap, null);
            return;
        }
        //根据条件查询所有子单实付分摊情况list，先查后关联
        OrderItemApportionStateQueryVO orderItemApportionStateQueryVO = new OrderItemApportionStateQueryVO();
        orderItemApportionStateQueryVO.setOrderItemIdList(apportionNotByMulPriceItemIdList);
        orderItemApportionStateQueryVO.setCostCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment.name());
        List<OrderItemApportionState> orderItemApportionStateList = orderItemApportionStateService.queryOrderItemApportionStateList(orderItemApportionStateQueryVO);

        //遍历子单实体，把子单与子单分摊情况关联，如果子单没有查询到对应的分摊情况，设定一个空值
        assignApportionInfoNotByPriceType(apportionNotByMulPriceRstMap, orderItemApportionStateList);

        log.info("Calculate apportion info for order detail not by price type completed, orderItemApportionStateList is " + GsonUtils.toJson(orderItemApportionStateList));
    }

    /**
     * 从子单分摊情况列表中查找子单对应的分摊信息，如果没有找到，给子单一个0值的分摊信息
     * */
    private void assignApportionInfoNotByPriceType(Map<String, List<OrderMonitorRst>> apportionNotByMulPriceRstMap, List<OrderItemApportionState> orderItemApportionStateList) {
        if (MapUtils.isEmpty(apportionNotByMulPriceRstMap)) {
            return;
        }
        //遍历Map
        for (List<OrderMonitorRst> orderMonitorRstList : apportionNotByMulPriceRstMap.values()) {
            if(CollectionUtils.isEmpty(orderMonitorRstList)) {
                continue;
            }
            //遍历Map中的子单集合
            for (OrderMonitorRst orderMonitorRst : orderMonitorRstList) {
                if (orderMonitorRst == null) {
                    continue;
                }

                //如果子单中的实付分摊集合为空,申请一个
                if(orderMonitorRst.getActualPaidAmountList() == null) {
                    orderMonitorRst.setActualPaidAmountList(new ArrayList<PriceTypeVO>());
                }
                List<PriceTypeVO> actualPaidAmountList = orderMonitorRst.getActualPaidAmountList();
                if(CollectionUtils.isEmpty(orderItemApportionStateList)) {
                    actualPaidAmountList.add(new PriceTypeVO(0L, OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCnName()));
                    continue;
                }
                //子单是否匹配到了分摊情况实体
                boolean matched = false;
                for (OrderItemApportionState orderItemApportionState : orderItemApportionStateList) {
                    if (orderItemApportionState == null) {
                        continue;
                    }
                    if(Objects.equals(orderMonitorRst.getOrderItemId(), orderItemApportionState.getOrderItemId())) {
                        actualPaidAmountList.add(new PriceTypeVO(orderItemApportionState.getApportionAmount(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCnName()));
                        matched = true;
                    }
                }

                //没有匹配到，那么分配一个空值
                if(!matched) {
                    actualPaidAmountList.add(new PriceTypeVO(0L, OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCnName()));
                }
            }
        }
    }
    /**
     * 从子单分摊情况列表中查找子单对应的支付立减分摊信息，如果没有找到，给子单一个0值的分摊信息
     * */
    private void assignPayApportionInfoNotByPriceType(Map<String, List<OrderMonitorRst>> payapportionNotByMulPriceRstMap, List<OrderItemApportionState> orderItemApportionStateList) {
        if (MapUtils.isEmpty(payapportionNotByMulPriceRstMap)) {
            return;
        }
        //遍历Map
        for (List<OrderMonitorRst> orderMonitorRstList : payapportionNotByMulPriceRstMap.values()) {
            if(CollectionUtils.isEmpty(orderMonitorRstList)) {
                continue;
            }
            //遍历Map中的子单集合
            for (OrderMonitorRst orderMonitorRst : orderMonitorRstList) {
                if (orderMonitorRst == null) {
                    continue;
                }

                //如果子单中的实付分摊集合为空,申请一个
                if(orderMonitorRst.getPayProAmountList() == null) {
                    orderMonitorRst.setPayProAmountList(new ArrayList<PriceTypeVO>());
                }
                List<PriceTypeVO> payProAmountList = orderMonitorRst.getPayProAmountList();
                if(CollectionUtils.isEmpty(orderItemApportionStateList)) {
                	payProAmountList.add(new PriceTypeVO(0L, OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCnName()));
                    continue;
                }
                //子单是否匹配到了分摊情况实体
                boolean matched = false;
                for (OrderItemApportionState orderItemApportionState : orderItemApportionStateList) {
                    if (orderItemApportionState == null) {
                        continue;
                    }
                    if(Objects.equals(orderMonitorRst.getOrderItemId(), orderItemApportionState.getOrderItemId())) {
                    	payProAmountList.add(new PriceTypeVO(orderItemApportionState.getApportionAmount(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCnName()));
                        matched = true;
                    }
                }

                //没有匹配到，那么分配一个空值
                if(!matched) {
                	payProAmountList.add(new PriceTypeVO(0L, OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.name(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCnName()));
                }
            }
        }
    }

    /**
     * 计算按多价格分摊的子单的分摊信息
     * @param apportionByMulPriceRstMap 按多价格分摊的子单实体Map
     * @param itemIdWithMulPriceMap 多价格Map，用于对按多价格分摊的子单的多价格排序，使用时以子单id作为key
     * @param apportionByMulPriceItemIdList 按多价格分摊的子单集合.
     * */
    private void calcRstMapApportionByPriceType(Map<String, List<OrderMonitorRst>> apportionByMulPriceRstMap, Map<Long, List<OrdMulPriceRate>> itemIdWithMulPriceMap, List<Long> apportionByMulPriceItemIdList) {
        if(MapUtils.isEmpty(apportionByMulPriceRstMap)) {
            return;
        }
        log.info("Now calculate apportion info for order detail by price type, apportionByMulPriceItemIdList is " + GsonUtils.toJson(apportionByMulPriceItemIdList));
        if(CollectionUtils.isEmpty(apportionByMulPriceItemIdList)) {
            //传个空值进去，方法会分配0值给子单
            assignApportionInfoByPriceType(apportionByMulPriceRstMap, itemIdWithMulPriceMap,  null);
            return;
        }

        OrderCostSharingItemQueryVO orderCostSharingItemQueryVO = new OrderCostSharingItemQueryVO();
        orderCostSharingItemQueryVO.setOrderItemIdList(apportionByMulPriceItemIdList);
        orderCostSharingItemQueryVO.setCostCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment.name());
        List<OrdOrderCostSharingItem> orderCostSharingItemList = orderCostSharingItemService.queryOrdOrderCostSharingItemList(orderCostSharingItemQueryVO);
        assignApportionInfoByPriceType(apportionByMulPriceRstMap, itemIdWithMulPriceMap, orderCostSharingItemList);
        log.info("Calculate apportion info for order detail by price type completed, orderCostSharingItemList is " + GsonUtils.toJson(orderCostSharingItemList));
    }

    /**
     * 根据分摊信息，生成子单的多价格分摊信息，如果子单没有匹配到分摊信息，分配一个0值的分摊信息，类型是 PRICE_ADULT
     * */
    private void assignApportionInfoByPriceType(Map<String, List<OrderMonitorRst>> apportionByMulPriceRstMap, Map<Long, List<OrdMulPriceRate>> itemIdWithMulPriceMap, List<OrdOrderCostSharingItem> orderCostSharingItemList) {
        if (MapUtils.isEmpty(apportionByMulPriceRstMap)) {
            return;
        }
        //遍历Map
        for (List<OrderMonitorRst> orderMonitorRstList : apportionByMulPriceRstMap.values()) {
            if(CollectionUtils.isEmpty(orderMonitorRstList)) {
                continue;
            }
            //遍历Map中的子单集合
            for (OrderMonitorRst orderMonitorRst : orderMonitorRstList) {
                if (orderMonitorRst == null) {
                    continue;
                }
                List<OrdMulPriceRate> ordMulPriceRateList = null;
                //子单的多价格
                if(itemIdWithMulPriceMap == null || itemIdWithMulPriceMap.get(orderMonitorRst.getOrderItemId()) == null) {
                    ordMulPriceRateList = new ArrayList<>();
                    OrdMulPriceRate ordMulPriceRate = new OrdMulPriceRate();
                    ordMulPriceRate.setPriceType(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name());
                    ordMulPriceRateList.add(ordMulPriceRate);
                } else {
                    ordMulPriceRateList = itemIdWithMulPriceMap.get(orderMonitorRst.getOrderItemId());
                }

                //如果子单中的实付分摊集合为空,申请一个
                if(orderMonitorRst.getActualPaidAmountList() == null) {
                    orderMonitorRst.setActualPaidAmountList(new ArrayList<PriceTypeVO>());
                }
                List<PriceTypeVO> actualPaidAmountList = orderMonitorRst.getActualPaidAmountList();
                //如果没有获取到分摊值，对于每个多价格，分配一个0值的PriceTypeVO实体
                if(CollectionUtils.isEmpty(orderCostSharingItemList)) {
                    for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {
                        String priceType = ordMulPriceRate.getPriceType();
                        actualPaidAmountList.add(new PriceTypeVO(0L, priceType, OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(priceType)));
                    }

                    continue;
                }
                //子单是否匹配到了分摊情况实体
                boolean matched = false;
                for (OrdOrderCostSharingItem orderCostSharingItem : orderCostSharingItemList) {
                    if (orderCostSharingItem == null) {
                        continue;
                    }
                    if(Objects.equals(orderMonitorRst.getOrderItemId(), orderCostSharingItem.getOrderItemId())) {
                        actualPaidAmountList.add(new PriceTypeVO(orderCostSharingItem.getAmount() , orderCostSharingItem.getCostType(), OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(orderCostSharingItem.getCostType())));
                        matched = true;
                    }
                }

                //没有匹配到，那么分配一个空值
                if(!matched) {
                    actualPaidAmountList.add(new PriceTypeVO(0L, OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCnName()));
                }
            }

            //对子单中的多价格信息做排序
//            sortItemApportionAmountByMulPrice(orderMonitorRstList, itemIdWithMulPriceMap);
        }
    }
    
    /**
     * 根据分摊信息，生成子单的多价格分摊信息，如果子单没有匹配到分摊信息，分配一个0值的分摊信息，类型是 PRICE_ADULT
     * */
    private void assignPayApportionInfoByPriceType(Map<String, List<OrderMonitorRst>> apportionByMulPriceRstMap, Map<Long, List<OrdMulPriceRate>> itemIdWithMulPriceMap, List<OrdOrderCostSharingItem> orderCostSharingItemList) {
        if (MapUtils.isEmpty(apportionByMulPriceRstMap)) {
            return;
        }
        //遍历Map
        for (List<OrderMonitorRst> orderMonitorRstList : apportionByMulPriceRstMap.values()) {
            if(CollectionUtils.isEmpty(orderMonitorRstList)) {
                continue;
            }
            //遍历Map中的子单集合
            for (OrderMonitorRst orderMonitorRst : orderMonitorRstList) {
                if (orderMonitorRst == null) {
                    continue;
                }
                List<OrdMulPriceRate> ordMulPriceRateList = null;
                //子单的多价格
                if(itemIdWithMulPriceMap == null || itemIdWithMulPriceMap.get(orderMonitorRst.getOrderItemId()) == null) {
                    ordMulPriceRateList = new ArrayList<>();
                    OrdMulPriceRate ordMulPriceRate = new OrdMulPriceRate();
                    ordMulPriceRate.setPriceType(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name());
                    ordMulPriceRateList.add(ordMulPriceRate);
                } else {
                    ordMulPriceRateList = itemIdWithMulPriceMap.get(orderMonitorRst.getOrderItemId());
                }

                //如果子单中的实付分摊集合为空,申请一个
                if(orderMonitorRst.getPayProAmountList() == null) {
                    orderMonitorRst.setPayProAmountList(new ArrayList<PriceTypeVO>());
                }
                List<PriceTypeVO> payProAmountList = orderMonitorRst.getPayProAmountList();
                //如果没有获取到分摊值，对于每个多价格，分配一个0值的PriceTypeVO实体
                if(CollectionUtils.isEmpty(orderCostSharingItemList)) {
                    for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {
                        String priceType = ordMulPriceRate.getPriceType();
                        payProAmountList.add(new PriceTypeVO(0L, priceType, OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(priceType)));
                    }

                    continue;
                }
                //子单是否匹配到了分摊情况实体
                boolean matched = false;
                for (OrdOrderCostSharingItem orderCostSharingItem : orderCostSharingItemList) {
                    if (orderCostSharingItem == null) {
                        continue;
                    }
                    if(Objects.equals(orderMonitorRst.getOrderItemId(), orderCostSharingItem.getOrderItemId())) {
                    	payProAmountList.add(new PriceTypeVO(orderCostSharingItem.getAmount() , orderCostSharingItem.getCostType(), OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(orderCostSharingItem.getCostType())));
                        matched = true;
                    }
                }

                //没有匹配到，那么分配一个空值
                if(!matched) {
                	payProAmountList.add(new PriceTypeVO(0L, OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name(), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCnName()));
                }
            }

            //对子单中的多价格信息做排序
//            sortItemApportionAmountByMulPrice(orderMonitorRstList, itemIdWithMulPriceMap);
        }
    }

    private void addItemIdToList(List<Long> apportionByMulPriceItemIdList, List<OrderMonitorRst> orderMonitorRstList) {
        if(apportionByMulPriceItemIdList == null || CollectionUtils.isEmpty(orderMonitorRstList)) {
            return;
        }
        for (OrderMonitorRst orderMonitorRst : orderMonitorRstList) {
            if (orderMonitorRst == null || NumberUtils.isNotAboveZero(orderMonitorRst.getOrderItemId())) {
                continue;
            }
            apportionByMulPriceItemIdList.add(orderMonitorRst.getOrderItemId());
        }
    }

    /**
     * 根据多价格信息，对子单实体作排序
     * */
    private void sortItemApportionAmountByMulPrice(List<OrderMonitorRst> orderMonitorRstList, Map<Long, List<OrdMulPriceRate>> itemIdWithMulPriceMap) {
        if(CollectionUtils.isEmpty(orderMonitorRstList) || MapUtils.isEmpty(itemIdWithMulPriceMap)) {
            return;
        }

        log.info("Now sort apportion info for item monitor po list, itemIdWithMulPriceMap is " + GsonUtils.toJson(itemIdWithMulPriceMap));
        for (OrderMonitorRst orderMonitorRst : orderMonitorRstList) {
            List<OrdMulPriceRate> ordMulPriceRateList = itemIdWithMulPriceMap.get(orderMonitorRst.getOrderItemId());
            if(CollectionUtils.isEmpty(ordMulPriceRateList)) {
                continue;
            }
            List<PriceTypeVO> actualPaidAmountList = orderMonitorRst.getActualPaidAmountList();
            if(CollectionUtils.isEmpty(actualPaidAmountList)) {
                continue;
            }
            //查检并且排序子单中的多价格
            checkAndSortMulPrice(orderMonitorRst.getOrderItemId(), ordMulPriceRateList, actualPaidAmountList);
        }
        log.info("Sort apportion info for item monitor po list completed, itemIdWithMulPriceMap is " + GsonUtils.toJson(itemIdWithMulPriceMap));
    }

    /**
     * 检查并且排序多价格
     * */
    private void checkAndSortMulPrice(Long orderItemId, List<OrdMulPriceRate> ordMulPriceRateList, List<PriceTypeVO> actualPaidAmountList) {
        for (int mulPriceIndex = 0; mulPriceIndex < ordMulPriceRateList.size(); mulPriceIndex++) {
            OrdMulPriceRate ordMulPriceRate = ordMulPriceRateList.get(mulPriceIndex);
            PriceTypeVO priceTypeVO = actualPaidAmountList.get(mulPriceIndex);
            if(StringUtils.equals(ordMulPriceRate.getPriceType(), priceTypeVO.getPriceType())) {
                continue;
            }
            //如果发现有顺序不匹配时，根据多价格的顺序，重新生成一个初值分摊列表
            log.info("Order item " + orderItemId + ", price type vo needs to sort, mul price is "
                    + GsonUtils.toJson(ordMulPriceRateList) + ", actual paid amount list is "
                    + GsonUtils.toJson(actualPaidAmountList));
            actualPaidAmountList = generateNewActualPaidAmountList(ordMulPriceRateList, actualPaidAmountList);
            log.info("Order item " + orderItemId + ", resorted actual paid amount list is "
                    + GsonUtils.toJson(actualPaidAmountList));
            return;
        }
    }

    /**
     * 根据多价格的顺序，重新生成一个初值分摊列表
     * */
    private List<PriceTypeVO> generateNewActualPaidAmountList(List<OrdMulPriceRate> ordMulPriceRateList, List<PriceTypeVO> actualPaidAmountList) {
        if(CollectionUtils.isEmpty(ordMulPriceRateList) || CollectionUtils.isEmpty(actualPaidAmountList)) {
            return null;
        }
        List<PriceTypeVO> newPriceTypeVOList = new ArrayList<>();
        for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {
            for (PriceTypeVO priceTypeVO : actualPaidAmountList) {
                if (StringUtils.equals(ordMulPriceRate.getPriceType(), priceTypeVO.getPriceType())) {
                    newPriceTypeVOList.add(priceTypeVO);
                }
            }
        }
        return newPriceTypeVOList;
    }

    /**
     * 根据子订单，生成子单分摊信息
     *
     * @param orderItem
     */
    @Override
    public OrderItemApportionInfoPO generateItemApportionInfoPO4Detail(OrdOrderItem orderItem) {
        if(orderItem == null || orderItem.getOrderItemId() == null) {
            return null;
        }
        //判断分摊开关是否开启
        if(!ApportionUtil.isApportionEnabled()) {
            log.info("Apportion is not enabled, please check, order item id is " + orderItem.getOrderItemId());
            return null;
        }

        Long orderItemId = orderItem.getOrderItemId();
        log.info("Now query apportion info for order " + orderItem.getOrderId() + ", item " + orderItemId);
        OrderItemApportionInfoQueryVO orderItemApportionInfoQueryVO = new OrderItemApportionInfoQueryVO();
        orderItemApportionInfoQueryVO.setOrderId(orderItem.getOrderId());
        orderItemApportionInfoQueryVO.setOrderItemId(orderItemId);
        OrderItemApportionInfoPO orderItemApportionInfoPO = apportionInfoQueryService.calcOrderItemApportionInfo(orderItemApportionInfoQueryVO);
        log.info("Query apportion info for order " + orderItem.getOrderId() + ", item " + orderItemId + " completed, result is " + GsonUtils.toJson(orderItemApportionInfoPO));
        return orderItemApportionInfoPO;
    }

    /**
     * 根据子单，生成子单分摊信息列表
     *
     * @param orderItem
     */
    @Override
    public List<OrderHotelTimeRateInfo> generateHotelTimeRateInfoList(OrdOrderItem orderItem) {
        log.info("Generate hotel time rate for order " + orderItem.getOrderId() + " , item " + orderItem.getOrderItemId());
        List<OrdOrderHotelTimeRate> orderHotelTimeRateList = orderItem.getOrderHotelTimeRateList();
        if(CollectionUtils.isEmpty(orderHotelTimeRateList) || NumberUtils.equalsOrBelowZero(orderItem.getSuppGoodsId())) {
            return null;
        }
        List<OrderHotelTimeRateInfo> orderHotelTimeRateInfoList = new ArrayList<>();
        OrderItemApportionInfoPO orderItemApportionInfoPO = generateItemApportionInfoPO4Detail(orderItem);

        log.info("Fill apportion info for order " + orderItem.getOrderId() + " item " + orderItem.getOrderItemId());
        Date startDate = orderItem.getVisitTime();
        Date endDate = startDate;
        for (OrdOrderHotelTimeRate orderHotelTimeRate : orderHotelTimeRateList) {
            //分摊相关的金额
            Long couponApportionAmount = 0L, promotionApportionAmount = 0L, distributorApportionAmount = 0L, manualChangeApportionAmount = 0L, actualPaidAmount = 0L,payAmountReductTotalAmount=0L;
            Date visitTime = orderHotelTimeRate.getVisitTime();
            //当前日期下，所有的退款金额、退款数量
            Long refundAmount = 0L, refundQuantity = 0L;
            if(orderItemApportionInfoPO != null) {
                //按入住日期分摊的优惠金额Map
                Map<Date, PriceTypeVO> itemCouponApportionByCheckInMap = orderItemApportionInfoPO.getItemCouponApportionByCheckInMap();
                couponApportionAmount = getAmountFromMapByDate(itemCouponApportionByCheckInMap, visitTime);
                //按入住日期分摊的促销金额Map
                Map<Date, PriceTypeVO> itemPromotionApportionByCheckInMap = orderItemApportionInfoPO.getItemPromotionApportionByCheckInMap();
                promotionApportionAmount = getAmountFromMapByDate(itemPromotionApportionByCheckInMap, visitTime);
                //按入住日期分摊的渠道优惠金额Map
                Map<Date, PriceTypeVO> itemDistributorApportionByCheckInMap = orderItemApportionInfoPO.getItemDistributorApportionByCheckInMap();
                distributorApportionAmount = getAmountFromMapByDate(itemDistributorApportionByCheckInMap, visitTime);
                //按入住日期分摊的手工改价金额Map
                Map<Date, PriceTypeVO> itemManualChangeApportionByCheckInMap = orderItemApportionInfoPO.getItemManualChangeApportionByCheckInMap();
                manualChangeApportionAmount = getAmountFromMapByDate(itemManualChangeApportionByCheckInMap, visitTime);
                //按入住日期分摊的实付金额Map
                Map<Date, PriceTypeVO> itemActualPaidApportionByCheckInMap = orderItemApportionInfoPO.getItemActualPaidApportionByCheckInMap();
                actualPaidAmount = getAmountFromMapByDate(itemActualPaidApportionByCheckInMap, visitTime);
                //按入住日期归类的退款信息
                Map<Date, PriceTypeNode> itemRefundByCheckInMap = orderItemApportionInfoPO.getItemRefundByCheckInMap();
                PriceTypeNode priceTypeNode = getFixedNodeByDate(itemRefundByCheckInMap, visitTime);
                refundAmount = priceTypeNode.getAmount();
                refundQuantity = priceTypeNode.getQuantity();
                
                //按入住日期分摊的支付立减金额
                Map<Date, PriceTypeVO> itemReductByCheckInMap = orderItemApportionInfoPO.getItemReductApportionByCheckInMap();
                payAmountReductTotalAmount=getAmountFromMapByDate(itemReductByCheckInMap, visitTime);
            }

            OrderHotelTimeRateInfo orderHotelTimeRateInfo = new OrderHotelTimeRateInfo();
            orderHotelTimeRateInfo.setOrderItemId(orderHotelTimeRate.getOrderItemId());
            orderHotelTimeRateInfo.setVisitTime(visitTime);
            orderHotelTimeRateInfo.setPrice(orderHotelTimeRate.getPrice());
            orderHotelTimeRateInfo.setSettlementPrice(orderHotelTimeRate.getSettlementPrice());
            //担保时间
            String guaranteeTimeStr = orderItem.getContentMap().get(OrderEnum.HOTEL_CONTENT.latestUnguarTime.name()) + "";
            orderHotelTimeRateInfo.setGuaranteeTime(guaranteeTimeStr);
            //赋值，分摊信息
            orderHotelTimeRateInfo.setCouponApportionAmount(couponApportionAmount);
            orderHotelTimeRateInfo.setPromotionApportionAmount(promotionApportionAmount);
            orderHotelTimeRateInfo.setDistributorApportionAmount(distributorApportionAmount);
            orderHotelTimeRateInfo.setManualChangeApportionAmount(manualChangeApportionAmount);
            orderHotelTimeRateInfo.setActualPaidAmount(actualPaidAmount);
            orderHotelTimeRateInfo.setRefundAmount(refundAmount);
            orderHotelTimeRateInfo.setRefundQuantity(refundQuantity);
            
            orderHotelTimeRateInfo.setPayAmountReductTotalAmount(payAmountReductTotalAmount);
            orderHotelTimeRateInfoList.add(orderHotelTimeRateInfo);

            //设定最后1天游玩的日期，用于查询时间价格表
            if(endDate == null) {
                endDate = visitTime;
            } else {
                if (endDate.before(visitTime)) {
                    endDate = visitTime;
                }
            }
        }

        if (CollectionUtils.isEmpty(orderHotelTimeRateInfoList)) {
            log.info("Now hotel time rate info get, for order " + orderItem.getOrderId() + " item " + orderItem.getOrderItemId());
        }

        //设定最晚预订时间
        log.info("Query time price for order " + orderItem.getOrderId() + " item " + orderItem.getOrderItemId()
                + ", start date " + DateUtil.formatDate(startDate, DateUtil.SIMPLE_DATE_FORMAT) + ", end date "
                + DateUtil.formatDate(endDate, DateUtil.SIMPLE_DATE_FORMAT));
        Map<String, Object> paramMap = new HashMap<>();
        List<Long> suppGoodsIdList = new ArrayList<>();
        suppGoodsIdList.add(orderItem.getSuppGoodsId());
        paramMap.put("goodsIds", suppGoodsIdList);
        paramMap.put("startDate", startDate);
        //由于目的地查询时间价格表时，默认最右边会用“<”，导致最后一天查询不到时间价格表，所以把结束日期加1天
        endDate = DateUtil.addDays(endDate, 1);
        paramMap.put("endDate", endDate);
        ResultHandleT<List<SuppGoodsTimePrice>> suppGoodsTimePriceListResultHandle = suppGoodsTimePriceClientService.findTimePriceBySpecDate(paramMap);
        log.info("Interface invoked.Query time price for order " + orderItem.getOrderId() + " item " + orderItem.getOrderItemId()
                + ", start date " + DateUtil.formatDate(startDate, DateUtil.SIMPLE_DATE_FORMAT) + ", end date "
                + DateUtil.formatDate(endDate, DateUtil.SIMPLE_DATE_FORMAT));
        //遍历时间价格表，设定到
        if (suppGoodsTimePriceListResultHandle != null && suppGoodsTimePriceListResultHandle.isSuccess() && suppGoodsTimePriceListResultHandle.getReturnContent() != null) {
            List<SuppGoodsTimePrice> suppGoodsTimePriceList = suppGoodsTimePriceListResultHandle.getReturnContent();
            if(CollectionUtils.isNotEmpty(suppGoodsTimePriceList)) {
                for (SuppGoodsTimePrice suppGoodsTimePrice : suppGoodsTimePriceList) {
                    if (suppGoodsTimePrice == null || suppGoodsTimePrice.getSpecDate() == null) {
                        continue;
                    }
                    String lastTime="";
                    if (suppGoodsTimePrice.getAheadBookTime() != null) {
                        //最晚预定时间
                        Date time = CalendarUtils.getEndDateByMinute(suppGoodsTimePrice.getSpecDate(), -suppGoodsTimePrice.getAheadBookTime());
                        lastTime = CalendarUtils.getDateFormatString(time, CalendarUtils.YYYY_MM_DD_HH_MM_PATTERN);
                    }
                    for (OrderHotelTimeRateInfo orderHotelTimeRateInfo : orderHotelTimeRateInfoList) {
                        if (orderHotelTimeRateInfo == null) {
                            continue;
                        }
                        if(Objects.equals(suppGoodsTimePrice.getSpecDate().getTime(), orderHotelTimeRateInfo.getVisitTime().getTime())) {
                            orderHotelTimeRateInfo.setLastTime(lastTime);
                            orderHotelTimeRateInfo.setBreakfastTicket(Long.valueOf(suppGoodsTimePrice.getBreakfast()));
                        }
                    }
                }
            }
        }

        log.info("Generate hotel time rate for order " + orderItem.getOrderId() + " , item " + orderItem.getOrderItemId() + " completed, result is " + GsonUtils.toJson(orderHotelTimeRateInfoList));
        return orderHotelTimeRateInfoList;
    }

    /**
     * 根据日期，从Map中取出分摊的金额，如果没有取到，返回0
     * */
    private Long getAmountFromMapByDate(Map<Date, PriceTypeVO> priceTypeVOMap, Date checkInDate){
        if(MapUtils.isEmpty(priceTypeVOMap) || checkInDate == null) {
            return 0L;
        }
        PriceTypeVO priceTypeVO = priceTypeVOMap.get(checkInDate);
        if(priceTypeVO == null) {
            return 0L;
        }
        return priceTypeVO.getPrice();
    }

    /**
     * 根据日期，从Map中取出退款结点中的金额，如果结点不存在，或者没有值，补全上
     * */
    private PriceTypeNode getFixedNodeByDate(Map<Date, PriceTypeNode> priceTypeNodeMap, Date checkInDate){
        if(MapUtils.isEmpty(priceTypeNodeMap) || checkInDate == null || priceTypeNodeMap.get(checkInDate) == null) {
            return new PriceTypeNode(0L, 0L,OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCode(),OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE.getCnName(), new ArrayList<PriceTypeVO>());
        }
        PriceTypeNode priceTypeNode = priceTypeNodeMap.get(checkInDate);
        if (priceTypeNode.getAmount() == null) {
            priceTypeNode.setAmount(0L);
        }
        if (priceTypeNode.getQuantity() == null) {
            priceTypeNode.setQuantity(0L);
        }
        if (priceTypeNode.getPriceTypeVOList() == null) {
            priceTypeNode.setPriceTypeVOList(new ArrayList<PriceTypeVO>());
        }
        return priceTypeNode;
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
        if(orderItemApportionInfoPO == null || CollectionUtils.isEmpty(ordMulPriceList)) {
            return;
        }
        log.info("Now sort apportion info for order item " + orderItemApportionInfoPO.getOrderItemId() + " ordMulPriceList is " + GsonUtils.toJson(ordMulPriceList));
        //判断分摊的粒度
        OrderEnum.ORDER_APPORTION_PARTICLE orderApportionParticle = ApportionUtil.judgeApportionParticle(orderItemApportionInfoPO.getCategoryId());
        ApportionParticleService apportionParticleService = apportionParticleServiceFactory.catchOrderDetailApportionCompleteService(orderApportionParticle);
        if(apportionParticleService == null) {
            log.warn("Order item [" + orderItemApportionInfoPO.getOrderItemId() + "], category id is " + orderItemApportionInfoPO.getCategoryId() + ", can't get apportionParticleService");
            return;
        }
        apportionParticleService.sortAndCompleteApportionInfoByMulPrice(orderItemApportionInfoPO, ordMulPriceList);

        log.info("Sort apportion info for order item " + orderItemApportionInfoPO.getOrderItemId() + " completed, result is " + GsonUtils.toJson(orderItemApportionInfoPO));
    }
}
