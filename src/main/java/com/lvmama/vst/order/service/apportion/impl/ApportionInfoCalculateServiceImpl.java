package com.lvmama.vst.order.service.apportion.impl;

import com.lvmama.comm.pet.refund.vo.RefundOrderItemSplit;
import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.po.OrdOrderCostSharingItem;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderItemApportionState;
import com.lvmama.vst.back.order.vo.OrderCostSharingItemQueryVO;
import com.lvmama.vst.back.order.vo.OrderItemApportionStateQueryVO;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.OrdOrderCostSharingItemService;
import com.lvmama.vst.order.service.OrderItemApportionStateService;
import com.lvmama.vst.order.service.apportion.ApportionInfoCalculateService;
import com.lvmama.vst.order.vo.OrderApportionInfoQueryVO;
import com.lvmama.vst.order.vo.OrderItemApportionInfoQueryVO;
import com.lvmama.vst.order.vo.OrderItemApportionInfoRelatedVO;
import com.lvmama.vst.pet.adapter.refund.OrderRefundSplitServiceAdapter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyanqun on 2017/5/19.
 */
@Service
public class ApportionInfoCalculateServiceImpl implements ApportionInfoCalculateService {
    private static final Log log = LogFactory.getLog(ApportionInfoCalculateServiceImpl.class);
    @Resource
    private OrdOrderCostSharingItemService orderCostSharingItemService;
    @Resource
    private OrderItemApportionStateService orderItemApportionStateService;
    @Resource
    private IOrdOrderItemService orderItemService;
    @Resource
    private OrderRefundSplitServiceAdapter orderRefundSplitServiceAdapter;
    /**
     * 根据查询VO，准备必要的数据
     *
     * @param orderApportionInfoQueryVO
     */
    @Override
    public OrderItemApportionInfoRelatedVO prepareRelatedVO(OrderApportionInfoQueryVO orderApportionInfoQueryVO) {
        OrderItemApportionInfoRelatedVO orderItemApportionInfoRelatedVO = new OrderItemApportionInfoRelatedVO();
        //查询所有子单分摊信息
        OrderCostSharingItemQueryVO orderCostSharingItemQueryVO = generateOrderCostSharingItemQueryVO(orderApportionInfoQueryVO);
        List<OrdOrderCostSharingItem> orderCostSharingItemList = orderCostSharingItemService.queryOrdOrderCostSharingItemList(orderCostSharingItemQueryVO);
        orderItemApportionInfoRelatedVO.setOrderCostSharingItemList(orderCostSharingItemList);
        //查询所有的子单分摊情况集合
        OrderItemApportionStateQueryVO orderItemApportionStateQueryVO = generateOrderItemApportionStateQueryVO(orderApportionInfoQueryVO);
        List<OrderItemApportionState> orderItemApportionStateList = orderItemApportionStateService.queryOrderItemApportionStateList(orderItemApportionStateQueryVO);
        orderItemApportionInfoRelatedVO.setOrderItemApportionStateList(orderItemApportionStateList);
        //查询所有的子单
        List<OrdOrderItem> orderItemList = catchOrderItemList(orderApportionInfoQueryVO);
        orderItemApportionInfoRelatedVO.setOrderItemList(orderItemList);
        //查询退款信息
        Long orderId = orderApportionInfoQueryVO.getOrderId();
        ResultHandleT<List<RefundOrderItemSplit>> listResultHandleT = orderRefundSplitServiceAdapter.queryOrdRefundmentItemSplitAllByOrderId(orderId);
        if(listResultHandleT == null || listResultHandleT.isFail()) {
            String errorInfo = listResultHandleT == null ? "result is null" : listResultHandleT.getMsg();
            log.error("Error occurs while trying to query refund info for order " + orderId + ", error info is " + errorInfo);
            return null;
        }
        //订单的退款信息
        List<RefundOrderItemSplit> refundOrderItemSplitList = listResultHandleT.getReturnContent();
        log.info("Refund info query by param " + GsonUtils.toJson(orderApportionInfoQueryVO) + " is " + GsonUtils.toJson(refundOrderItemSplitList));
        orderItemApportionInfoRelatedVO.setRefundOrderItemSplitList(refundOrderItemSplitList);
        return orderItemApportionInfoRelatedVO;
    }

    /**
     * 根据查询VO，准备必要的数据
     *
     * @param orderItemApportionInfoQueryVO
     */
    @Override
    public OrderItemApportionInfoRelatedVO prepareRelatedVO(OrderItemApportionInfoQueryVO orderItemApportionInfoQueryVO) {
        if(orderItemApportionInfoQueryVO == null || NumberUtils.isNotAboveZero(orderItemApportionInfoQueryVO.getOrderItemId())) {
            return new OrderItemApportionInfoRelatedVO();
        }
        OrderItemApportionInfoRelatedVO orderItemApportionInfoRelatedVO = new OrderItemApportionInfoRelatedVO();
        //订单号和子单号
        Long orderId = orderItemApportionInfoQueryVO.getOrderId();
        Long orderItemId = orderItemApportionInfoQueryVO.getOrderItemId();
        //查询子单分摊信息
        OrderCostSharingItemQueryVO orderCostSharingItemQueryVO = new OrderCostSharingItemQueryVO();
        orderCostSharingItemQueryVO.setOrderItemId(orderItemId);
        List<OrdOrderCostSharingItem> orderCostSharingItemList = orderCostSharingItemService.queryOrdOrderCostSharingItemList(orderCostSharingItemQueryVO);
        orderItemApportionInfoRelatedVO.setOrderCostSharingItemList(orderCostSharingItemList);
        //查询子单分摊情况集合，因为是单个子单，应该只有一条，取第1第
        OrderItemApportionStateQueryVO orderItemApportionStateQueryVO = new OrderItemApportionStateQueryVO();
        orderItemApportionStateQueryVO.setOrderItemId(orderItemId);
        List<OrderItemApportionState> orderItemApportionStateList = orderItemApportionStateService.queryOrderItemApportionStateList(orderItemApportionStateQueryVO);
        orderItemApportionInfoRelatedVO.setOrderItemApportionStateList(orderItemApportionStateList);
        //查询子单
        OrdOrderItem orderItem = orderItemService.selectOrderItemByOrderItemId(orderItemId);
        List<OrdOrderItem> orderItemList = new ArrayList<>();
        orderItemList.add(orderItem);
        orderItemApportionInfoRelatedVO.setOrderItemList(orderItemList);
        //查询子单对应的退款信息
        ResultHandleT<List<RefundOrderItemSplit>> listResultHandleT = orderRefundSplitServiceAdapter.queryOrdRefundmentItemSplitAllByOrderItemId(orderItemId);
        if(listResultHandleT == null || listResultHandleT.isFail()) {
            String errorInfo = listResultHandleT == null ? "result is null" : listResultHandleT.getMsg();
            log.error("Error occurs while trying to query refund info for order " + orderId + ", item " + orderItemId + " error info is " + errorInfo);
            return null;
        }
        List<RefundOrderItemSplit> refundOrderItemSplitList = listResultHandleT.getReturnContent();
        log.info("Refund info query by param " + GsonUtils.toJson(orderItemApportionInfoQueryVO) + " is " + GsonUtils.toJson(refundOrderItemSplitList));
        orderItemApportionInfoRelatedVO.setRefundOrderItemSplitList(refundOrderItemSplitList);
        return orderItemApportionInfoRelatedVO;
    }

    /**
     * 根据查询参数获取子单列表，查询参数需要已经检查过
     * 参数优先级：先按照子单id集合查，然后按子单id查，最后按照订单id查
     * */
    private List<OrdOrderItem> catchOrderItemList(OrderApportionInfoQueryVO orderApportionInfoQueryVO) {
        //优先按照子单列表查
        if(CollectionUtils.isNotEmpty(orderApportionInfoQueryVO.getOrderItemIdList())) {
            return orderItemService.selectOrderItemsByIds(orderApportionInfoQueryVO.getOrderItemIdList());
        }
        if(NumberUtils.isAboveZero(orderApportionInfoQueryVO.getOrderItemId())) {
            OrdOrderItem orderItem = orderItemService.selectOrderItemByOrderItemId(orderApportionInfoQueryVO.getOrderItemId());
            List<OrdOrderItem> orderItemList = new ArrayList<>();
            orderItemList.add(orderItem);
            return orderItemList;
        }
        return orderItemService.selectByOrderId(orderApportionInfoQueryVO.getOrderId());
    }

    /**
     * 生成查询VO
     * */
    private OrderCostSharingItemQueryVO generateOrderCostSharingItemQueryVO(OrderApportionInfoQueryVO orderApportionInfoQueryVO) {
        if(orderApportionInfoQueryVO == null) {
            return null;
        }
        OrderCostSharingItemQueryVO orderCostSharingItemQueryVO = new OrderCostSharingItemQueryVO();
        EnhanceBeanUtils.copyProperties(orderApportionInfoQueryVO, orderCostSharingItemQueryVO);
        return orderCostSharingItemQueryVO;
    }

    /**生成子单分摊情况查询的VO*/
    private OrderItemApportionStateQueryVO generateOrderItemApportionStateQueryVO(OrderApportionInfoQueryVO orderApportionInfoQueryVO) {
        if(orderApportionInfoQueryVO == null) {
            return null;
        }
        OrderItemApportionStateQueryVO orderItemApportionStateQueryVO = new OrderItemApportionStateQueryVO();
        EnhanceBeanUtils.copyProperties(orderApportionInfoQueryVO, orderItemApportionStateQueryVO);
        return orderItemApportionStateQueryVO;
    }
}
