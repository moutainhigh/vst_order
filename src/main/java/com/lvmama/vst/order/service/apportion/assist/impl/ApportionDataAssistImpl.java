package com.lvmama.vst.order.service.apportion.assist.impl;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.client.goods.service.SuppGoodsHotelAdapterClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.vo.OrderCostSharingItemQueryVO;
import com.lvmama.vst.back.order.vo.OrderItemApportionStateQueryVO;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.constant.ApportionConstants;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.service.apportion.assist.ApportionDataAssist;
import com.lvmama.vst.order.utils.ApportionUtil;
import com.lvmama.vst.order.utils.OrdOrderItemUtils;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.ApportionQueryVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhouyanqun on 2017/5/15.
 */
@Service
public class ApportionDataAssistImpl implements ApportionDataAssist {
    private static final Log log = LogFactory.getLog(ApportionDataAssistImpl.class);
    @Resource
    private IOrdOrderService ordOrderService;
    @Resource
    private IOrdOrderItemService orderItemService;
    @Resource
    private IOrdOrderAmountItemService orderAmountItemService;
    @Resource
    private IOrderAmountChangeService orderAmountChangeService;
    @Resource
    private IOrdMulPriceRateService ordMulPriceRateService;
    @Resource
    private IOrdOrderHotelTimeRateService orderHotelTimeRateService;
    @Resource
    private OrdOrderCostSharingItemService orderCostSharingItemService;
    //子单分摊情况服务
    @Resource
    private OrderItemApportionStateService orderItemApportionStateService;
    /**
     * 此组件用于判断商品是否属于快递
     * */
    @Resource(name = "suppGoodsClientAdapterRemote")
    private SuppGoodsHotelAdapterClientService suppGoodsHotelAdapterClientService;

    /**
     * 批量补全下单金额信息，包括优惠、促销、渠道优惠信息
     *
     * @param orderList
     */
    @Override
    public void assignBookingAmount(List<OrdOrder> orderList) {
        if(CollectionUtils.isEmpty(orderList)) {
            log.warn("Order list is empty, can't assign booking amount");
            return;
        }
        //订单id集合
        log.info("Now collect order id list for booking");
        List<Long> orderIdList = OrderUtils.getOrderIdList(orderList);
        assignBookingAmount(orderList, orderIdList);
    }

    /**
     * 批量补全下单金额信息，包括优惠、促销、渠道优惠信息
     *
     * @param orderList
     * @param orderIdList
     */
    @Override
    public void assignBookingAmount(List<OrdOrder> orderList, List<Long> orderIdList) {
        String orderIdJson = GsonUtils.toJson(orderIdList);
        Map<String, Object> orderMountItemParam = new HashMap<>();
        orderMountItemParam.put("orderIdList", orderIdList);
        orderMountItemParam.put("orderAmountTypeArray", ApportionConstants.apportionRelatedAmountTypeArray);
        //金额列表，优惠、促销、渠道优惠都在这个List中
        log.info("order id list collected, ready to query amount items, order list is " + orderIdJson);
        List<OrdOrderAmountItem> orderAmountItemList = orderAmountItemService.findOrderAmountItemList(orderMountItemParam);
        log.info("Order amount items queried, result is " + GsonUtils.toJson(orderAmountItemList));
        //关联金额列表到订单
        if (CollectionUtils.isNotEmpty(orderAmountItemList)) {
            for (OrdOrderAmountItem ordOrderAmountItem : orderAmountItemList) {
                if (ordOrderAmountItem == null) {
                    continue;
                }
                for (OrdOrder order : orderList) {
                    if (order == null) {
                        continue;
                    }
                    if(order.getOrderAmountItemList() == null) {
                        order.setOrderAmountItemList(new ArrayList<OrdOrderAmountItem>());
                    }
                    if (Objects.equals(order.getOrderId(), ordOrderAmountItem.getOrderId())) {
                        order.getOrderAmountItemList().add(ordOrderAmountItem);
                    }
                }
            }
        } else {
            log.info("No amount item queried, order id list is " + orderIdJson);
        }
        log.info("All order booking amount assigned, order list is " + orderIdJson);
    }

    /**
     * 查询手工改价的金额信息
     *
     * @param priceChangeOrderList
     * @param priceChangeOrderIdList
     */
    @Override
    public Map<Long, List<OrdAmountChange>> catchPriceChangeAmountMap(List<OrdOrder> priceChangeOrderList, List<Long> priceChangeOrderIdList) {
        Map<Long, List<OrdAmountChange>> ordAmountChangeMap = new HashMap<>();
        if (CollectionUtils.isEmpty(priceChangeOrderList)) {
            log.warn("Order list is empty");
            return ordAmountChangeMap;
        }
        if (CollectionUtils.isEmpty(priceChangeOrderIdList)) {
            priceChangeOrderIdList = OrderUtils.getOrderIdList(priceChangeOrderList);
        }
        String orderIdJson = GsonUtils.toJson(priceChangeOrderIdList);
        //金额列表，优惠、促销、渠道优惠都在这个List中
        log.info("order id list collected, ready to query amount changes, order list is " + orderIdJson);
        List<OrdAmountChange> ordAmountChanges = orderAmountChangeService.queryOrderPassedAmountChangeList(priceChangeOrderIdList);
        log.info("Order amount changes queried, result is " + GsonUtils.toJson(ordAmountChanges));
        //生成映射
        if (CollectionUtils.isNotEmpty(ordAmountChanges)) {
            for (OrdAmountChange ordAmountChange : ordAmountChanges) {
                if (ordAmountChange == null) {
                    continue;
                }
                Long orderId = ordAmountChange.getOrderId();
                List<OrdAmountChange> singleOrdAmountChangeList = ordAmountChangeMap.get(orderId);
                if (singleOrdAmountChangeList == null) {
                    singleOrdAmountChangeList = new ArrayList<>();
                    ordAmountChangeMap.put(orderId, singleOrdAmountChangeList);
                }
                singleOrdAmountChangeList.add(ordAmountChange);
            }
        } else {
            log.info("No amount change queried, order id list is " + orderIdJson);
        }
        return ordAmountChangeMap;
    }

    /**
     * 批量分配除实付金额外的其它金额的分摊信息
     *
     * @param orderList
     */
    @Override
    public void assignApportionItemExceptActualPaid(List<OrdOrder> orderList, List<Long> orderIdList) {
        if(CollectionUtils.isEmpty(orderList)) {
            log.warn("Order list is empty, can't assign apportion item except actual paid");
            return;
        }
        log.info("Now collect order id for apportion item except actual paid");
        this.assignApportionItem(orderList, orderIdList, ApportionConstants.beforePaymentCostCategoryList);
        log.info("All order apportion item except actual paid assigned");
    }

    /**
     * 批量分配手工改价的分摊金额到各个订单的子单，用于下单项金额已经分配，而且需要计算实付分摊的场景
     *
     * @param orderList 订单集合
     * @param orderIdList 订单id集合，可以为空，为空时，会从订单集合中取
     */
    @Override
    public void assignApportionItemOfManualChange(List<OrdOrder> orderList, List<Long> orderIdList) {
        if(CollectionUtils.isEmpty(orderList)) {
            log.warn("Order list is empty, can't assign apportion item of manual change");
            return;
        }
        log.info("Now collect order id for apportion item of manual change");
        List<String> costCategoryList = new ArrayList<>();
        costCategoryList.add(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_manual.name());
        this.assignApportionItem(orderList, orderIdList, costCategoryList);
        log.info("All order apportion item except actual paid assigned");
    }

    /**
     * 分配分摊信息
     * @param costCategoryList 分摊类型集合，可以为空，为空时查询并分摊所有的分摊信息
     * */
    private void assignApportionItem(List<OrdOrder> orderList, List<Long> orderIdList, List<String> costCategoryList){

        if (CollectionUtils.isEmpty(orderList)) {
            log.warn("Order list is empty");
            return;
        }

        if (CollectionUtils.isEmpty(orderIdList)) {
            orderIdList = OrderUtils.getOrderIdList(orderList);
        }
        String orderIdJson = GsonUtils.toJson(orderIdList);
        String costCategoryListJson = GsonUtils.toJson(costCategoryList);
        //订单id集合
        log.info("Assign cost sharing item for order list " + orderIdJson + ", cost category list " + costCategoryListJson);
        OrderCostSharingItemQueryVO orderCostSharingItemQueryVO = new OrderCostSharingItemQueryVO();
        orderCostSharingItemQueryVO.setOrderIdList(orderIdList);
        if (CollectionUtils.isNotEmpty(costCategoryList)) {
            orderCostSharingItemQueryVO.setCostCategoryList(costCategoryList);
        }
        //金额列表，优惠、促销、渠道优惠都在这个List中
        log.info("order id list collected, ready to query cost sharing items, order list is " + orderIdJson);
        List<OrdOrderCostSharingItem> orderCostSharingItemList = orderCostSharingItemService.queryOrdOrderCostSharingItemList(orderCostSharingItemQueryVO);
        log.info("Order cost sharing items queried, size is " + orderCostSharingItemList.size() + ", list is " + GsonUtils.toJson(orderCostSharingItemList));
        //关联金额列表到订单
        if (CollectionUtils.isNotEmpty(orderCostSharingItemList)) {
            for (OrdOrderCostSharingItem orderCostSharingItem : orderCostSharingItemList) {
                if (orderCostSharingItem == null) {
                    continue;
                }
                for (OrdOrder order : orderList) {
                    if (order == null || CollectionUtils.isEmpty(order.getOrderItemList())) {
                        continue;
                    }
                    List<OrdOrderItem> orderItemList = order.getOrderItemList();
                    for (OrdOrderItem orderItem : orderItemList) {
                        if (orderItem == null) {
                            continue;
                        }
                        if (orderItem.getOrderCostSharingItemList() == null) {
                            orderItem.setOrderCostSharingItemList(new ArrayList<OrdOrderCostSharingItem>());
                        }

                        if (Objects.equals(orderItem.getOrderItemId(), orderCostSharingItem.getOrderItemId())) {
                            orderItem.getOrderCostSharingItemList().add(orderCostSharingItem);
                        }
                    }
                }
            }
        } else {
            log.info("No cost sharing item queried, order id list is " + orderIdJson);
        }
        log.info("Assign cost sharing item for order list " + orderIdJson + ", cost category list " + costCategoryListJson + " completed");
    }

    /**
     * 根据订单id集合，查找包含按比例分摊的数据（也就是多价格和入住记录）的订单集合
     *
     * @param orderId
     */
    @Override
    public OrdOrder findOrderListWithPercentCases(Long orderId) {
        if (NumberUtils.isNotAboveZero(orderId)) {
            return null;
        }

        List<Long> orderIdList = new ArrayList<>();
        orderIdList.add(orderId);
        List<OrdOrder> orderListWithPercentCases = this.findOrderListWithPercentCases(orderIdList);
        if (CollectionUtils.isEmpty(orderListWithPercentCases)) {
            return null;
        }
        return orderListWithPercentCases.get(0);
    }

    /**
     * 根据订单id集合，查找包含按比例分摊的数据（也就是多价格和入住记录）的订单集合
     *
     * @param orderIdList
     */
    @Override
    public List<OrdOrder> findOrderListWithPercentCases(List<Long> orderIdList) {

        String orderListJson = GsonUtils.toJson(orderIdList);
        log.info("Prepare apportion data for order list :" + orderListJson);
        if(CollectionUtils.isEmpty(orderIdList)) {
            log.info("No order to prepare");
            return null;
        }
        //根据主键查询订单集合，单表，不含子单
        List<OrdOrder> ordOrderList = ordOrderService.getOrderList(orderIdList);
        if(CollectionUtils.isEmpty(ordOrderList)) {
            log.info("Can't get any order from db, order id list is " + orderListJson);
            return null;
        }
        log.info("Order list catch from database size is " + ordOrderList.size() + ", order id list is " + orderListJson);
        this.assignPercentCases(ordOrderList, orderIdList);
        return ordOrderList;
    }

    /**
     * 根据订单id集合，查找包含按比例分摊的数据（也就是多价格和入住记录）的订单集合
     *
     * @param orderList
     * @param orderIdList
     */
    @Override
    public void assignPercentCases(List<OrdOrder> orderList, List<Long> orderIdList) {
        if (CollectionUtils.isEmpty(orderIdList)) {
            return;
        }
        if (CollectionUtils.isEmpty(orderIdList)) {
            orderIdList = OrderUtils.getOrderIdList(orderList);
        }

        String orderListJson = GsonUtils.toJson(orderIdList);

        //根据订单号查子单，单表
        List<OrdOrderItem> orderItemList = orderItemService.selectOrderItemsByorderIds(orderIdList);
        if(CollectionUtils.isEmpty(orderItemList)) {
            log.info("Can't get any item from db, order id list is " + orderListJson);
            return;
        }
        log.info("Order item list catch from database size is " + orderItemList.size() + ", order id list is " + orderListJson);
        //子单id集合
        List<Long> orderItemIdList = new ArrayList<>();
        //是否包含酒店子单
        boolean hasHotelItem = false;
        for (OrdOrderItem orderItem : orderItemList) {
            if (orderItem == null) {
                continue;
            }
            orderItemIdList.add(orderItem.getOrderItemId());
            //一直检查是否有酒店子单，直到找到一个，或者遍历结束
            if (!hasHotelItem) {
                hasHotelItem = OrdOrderItemUtils.isHotelOrderItem(orderItem);
            }
        }
        //查询酒店入住记录信息
        List<OrdOrderHotelTimeRate> ordOrderHotelTimeRateList = null;
        if (hasHotelItem) {
            Map<String, Object> param = new HashMap<>();
            param.put("orderItemIdArray", orderItemIdList);
            ordOrderHotelTimeRateList = orderHotelTimeRateService.findOrdOrderHotelTimeRateList(param);
            int size = ordOrderHotelTimeRateList == null ? 0 : ordOrderHotelTimeRateList.size();
            log.info("Order item check in detail query end, size is " + size + ", order id list is " + orderListJson);
        } else {
            log.info("Order item check in detail query not needed, order id list is " + orderListJson);
        }
        //查询多价格集合
        Map<String, Object> param = new HashMap<>();
        param.put("orderItemIdArray", orderItemIdList);
        param.put("priceTypeArray", ApportionConstants.apportionAllRelatedPriceTypeArray);
        List<OrdMulPriceRate> ordMulPriceRateList = ordMulPriceRateService.findOrdMulPriceRateList(param);
        int size = ordMulPriceRateList == null ? 0 : ordMulPriceRateList.size();
        log.info("Order item multiple price queried, size is " + size + ", order id list is " + orderListJson);
        //遍历子单，把子单与主单、多价格、入住记录关联
        this.associateApportionDataColl(orderItemList, orderList, ordOrderHotelTimeRateList, ordMulPriceRateList);
        log.info("Associate order, item, multiple price and check in detail completed, order list is " + orderListJson);
    }

    /**
     * 过滤得到需要参与分摊的子单集合
     * 目前过滤快递和保险
     *
     * @param orderItemList
     */
    @Override
    public List<OrdOrderItem> filterOrderItems(List<OrdOrderItem> orderItemList) {

        if(CollectionUtils.isEmpty(orderItemList)) {
            return null;
        }
        log.info("Now filter apportion item list");
        //第一步，去掉保险子单，收集剩下子单的suppGoodsId，顺便判断有无酒店子单
        //不含保险的所有子单的商品id集合，子单id集合，子单的集合
        Set<Long> suppGoodsIdSetWithOutInsurance = new HashSet<>();
        Set<Long> orderItemIdSetWithOutInsurance = new HashSet<>();
        List<OrdOrderItem> orderItemListWithOutInsurance = new ArrayList<>();

        for (OrdOrderItem ordOrderItem : orderItemList) {
            if(ordOrderItem == null || NumberUtils.isNotAboveZero(ordOrderItem.getOrderItemId()) || NumberUtils.isNotAboveZero(ordOrderItem.getSuppGoodsId())) {
                continue;
            }
            if(OrdOrderItemUtils.isInsuranceOrderItem(ordOrderItem)) {
                continue;
            }
            suppGoodsIdSetWithOutInsurance.add(ordOrderItem.getSuppGoodsId());
            orderItemListWithOutInsurance.add(ordOrderItem);
            orderItemIdSetWithOutInsurance.add(ordOrderItem.getOrderItemId());
        }
        String orderItemWithoutInsuranceIdListJson = GsonUtils.toJson(orderItemIdSetWithOutInsurance);
        log.info("Insurance item filtered, orderItemIdList is " + orderItemWithoutInsuranceIdListJson);
        //第二步，去掉快递子单
        //需要分摊的子单集合、子单id集合
        List<OrdOrderItem> apportionOrderItemList = new ArrayList<>();
        //不含保险的所有子单的商品集合
        ResultHandleT<List<SuppGoods>> suppGoodsListResultHandleT = suppGoodsHotelAdapterClientService.findSuppGoodsListWithProduct(new ArrayList<>(suppGoodsIdSetWithOutInsurance));
        if(suppGoodsListResultHandleT == null || suppGoodsListResultHandleT.isFail()) {
            String errorMsg = suppGoodsListResultHandleT == null ? "result handle is null" : suppGoodsListResultHandleT.getMsg();
            log.error("Now judge whether items " + orderItemWithoutInsuranceIdListJson + " are expresses, but the product type can't get, will return null, Error msg is " + errorMsg);
            return null;
        }
        //所有不含保险的子单的商品
        List<SuppGoods> suppGoodsListWithoutInsurance = suppGoodsListResultHandleT.getReturnContent();
        if(CollectionUtils.isEmpty(suppGoodsListWithoutInsurance)) {
            log.error("No supp goods can found from interface, orderItemIdList is " + orderItemWithoutInsuranceIdListJson);
            return null;
        }
        log.info("Supp goods without insurance got form db, order item list is " + orderItemWithoutInsuranceIdListJson + ", supp goods list size is " + suppGoodsListWithoutInsurance.size());

        //对于不是保险，也不是快递的子商品，把子单加入返回结果中
        for (OrdOrderItem orderItem : orderItemListWithOutInsurance) {
            if (orderItem == null) {
                continue;
            }
            //遍历商品，如果需要不属于快递的商品与子单的商品id相同，则把需要分摊标识置为true
            for (SuppGoods suppGoods : suppGoodsListWithoutInsurance) {
                if(suppGoods == null || suppGoods.getProdProduct() == null || StringUtils.equals(suppGoods.getProdProduct().getProductType(), "EXPRESS")) {
                    continue;
                }
                if(Objects.equals(orderItem.getSuppGoodsId(), suppGoods.getSuppGoodsId())) {
                    apportionOrderItemList.add(orderItem);
                    break;
                }
            }
        }

        return apportionOrderItemList;
    }

    /**
     * 关联分摊数据集
     *
     * @param apportionOrderItemList
     * @param ordOrderList
     * @param ordOrderHotelTimeRateList
     * @param ordMulPriceRateList
     */
    private void associateApportionDataColl(List<OrdOrderItem> apportionOrderItemList, List<OrdOrder> ordOrderList, List<OrdOrderHotelTimeRate> ordOrderHotelTimeRateList, List<OrdMulPriceRate> ordMulPriceRateList) {
        if(CollectionUtils.isEmpty(apportionOrderItemList)) {
            return;
        }
        for (OrdOrderItem orderItem : apportionOrderItemList) {
            if(orderItem == null) {
                continue;
            }
            log.info("Now associate order, multiple price and check in detail for order " + orderItem.getOrderId() + ", item " + orderItem.getOrderItemId());
            //关联主单到子单
            for (OrdOrder order : ordOrderList) {
                if(order == null) {
                    continue;
                }
                if(order.getOrderItemList() == null) {
                    order.setOrderItemList(new ArrayList<OrdOrderItem>());
                }
                //关联子单到主单
                if(Objects.equals(order.getOrderId(), orderItem.getOrderId())) {
                    order.getOrderItemList().add(orderItem);
                }
            }
            //关联多价格到子单
            if(CollectionUtils.isNotEmpty(ordMulPriceRateList)) {
                if(orderItem.getOrdMulPriceRateList() == null) {
                    orderItem.setOrdMulPriceRateList(new ArrayList<OrdMulPriceRate>());
                }
                for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {
                    if(ordMulPriceRate == null){
                        continue;
                    }
                    if(Objects.equals(ordMulPriceRate.getOrderItemId(), orderItem.getOrderItemId())) {
                        orderItem.getOrdMulPriceRateList().add(ordMulPriceRate);
                    }
                }
            }
            //关联入住记录到子单
            if(CollectionUtils.isNotEmpty(ordOrderHotelTimeRateList)) {
                if(orderItem.getOrderHotelTimeRateList() == null) {
                    orderItem.setOrderHotelTimeRateList(new ArrayList<OrdOrderHotelTimeRate>());
                }
                for (OrdOrderHotelTimeRate orderHotelTimeRate : ordOrderHotelTimeRateList) {
                    if(orderHotelTimeRate == null) {
                        continue;
                    }
                    if(Objects.equals(orderHotelTimeRate.getOrderItemId(), orderItem.getOrderItemId())) {
                        orderItem.getOrderHotelTimeRateList().add(orderHotelTimeRate);
                    }
                }
            }
            log.info("Complete associate order, multiple price and check in detail for order " + orderItem.getOrderId() + ", item " + orderItem.getOrderItemId());
        }
    }

    /**
     * 根据条件作废以前的分摊记录
     *
     * @param apportionQueryVO
     */
    @Override
    public void invalidOrderApportionData(ApportionQueryVO apportionQueryVO) {
        //判断分摊开关是否开启
        if(!ApportionUtil.isApportionEnabled()) {
            log.error("Apportion is not enabled, please check");
            return;
        }
        if (apportionQueryVO == null) {
            return;
        }
        //作废子单分摊情况表
        OrderItemApportionStateQueryVO orderItemApportionStateQueryVO = new OrderItemApportionStateQueryVO();
        EnhanceBeanUtils.copyProperties(apportionQueryVO, orderItemApportionStateQueryVO);
        orderItemApportionStateService.updateRecords4invalid(orderItemApportionStateQueryVO);
        //作废分摊明细表
        OrderCostSharingItemQueryVO orderCostSharingItemQueryVO = new OrderCostSharingItemQueryVO();
        EnhanceBeanUtils.copyProperties(apportionQueryVO, orderCostSharingItemQueryVO);
        orderCostSharingItemService.updateRecords4Invalid(orderCostSharingItemQueryVO);
    }
}
