package com.lvmama.vst.order.service.apportion.impl;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.order.factory.ApportionParticleServiceFactory;
import com.lvmama.vst.order.po.OrderApportionInfoPO;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.apportion.ApportionInfoCalculateService;
import com.lvmama.vst.order.service.apportion.ApportionInfoQueryService;
import com.lvmama.vst.order.service.apportion.particle.ApportionParticleService;
import com.lvmama.vst.order.utils.ApportionUtil;
import com.lvmama.vst.order.vo.OrderApportionInfoQueryVO;
import com.lvmama.vst.order.vo.OrderItemApportionInfoQueryVO;
import com.lvmama.vst.order.vo.OrderItemApportionInfoRelatedVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyanqun on 2017/4/19.
 */
@Service
public class ApportionInfoQueryServiceImpl implements ApportionInfoQueryService {
    private static final Log log = LogFactory.getLog(ApportionInfoQueryServiceImpl.class);
    @Resource
    private ApportionInfoCalculateService apportionInfoCalculateService;
    @Resource
    private ApportionParticleServiceFactory apportionParticleServiceFactory;

    /**
     * 查询单个子单的分摊信息
     *
     * @param orderItemApportionInfoQueryVO
     */
    @Override
    public OrderItemApportionInfoPO calcOrderItemApportionInfo(OrderItemApportionInfoQueryVO orderItemApportionInfoQueryVO) {
        log.info("Now calculate item apportion info for query vo " + GsonUtils.toJson(orderItemApportionInfoQueryVO));
        if(NumberUtils.isNotAboveZero(orderItemApportionInfoQueryVO.getOrderItemId())) {
            log.error("Order item id can't be illegal, orderItemApportionInfoQueryVO is " + GsonUtils.toJson(orderItemApportionInfoQueryVO));
            return new OrderItemApportionInfoPO();
        }
        OrderItemApportionInfoRelatedVO orderItemApportionInfoRelatedVO = apportionInfoCalculateService.prepareRelatedVO(orderItemApportionInfoQueryVO);
        OrdOrderItem orderItem = orderItemApportionInfoRelatedVO.getOrderItemList().get(0);
        OrderItemApportionInfoPO orderItemApportionInfoPO = calculateItemApportionInfo(orderItem, orderItemApportionInfoRelatedVO);
        log.info("calculate item apportion info for query vo " + GsonUtils.toJson(orderItemApportionInfoQueryVO) + " completed ,result is " + GsonUtils.toJson(orderItemApportionInfoPO));
        return orderItemApportionInfoPO;
    }
    /**
     * 查询订单的分摊信息
     *
     * @param orderApportionInfoQueryVO 查询条件，其中orderId必须有值，否则返回null，因为此接口是计算整个订单的分摊信息
     */
    @Override
    public OrderApportionInfoPO calculateOrderApportionInfo(OrderApportionInfoQueryVO orderApportionInfoQueryVO) {//判断分摊开关是否开启
        if(!ApportionUtil.isApportionEnabled()) {
            log.error("Apportion is not enabled, please check");
            return null;
        }
        log.info("Now calculate apportion info for query vo " + GsonUtils.toJson(orderApportionInfoQueryVO));
        if(!checkQueryVO(orderApportionInfoQueryVO)) {
            log.error("Either order id or order item id shall have valid value! orderApportionInfoQueryVO is " + GsonUtils.toJson(orderApportionInfoQueryVO));
            return new OrderApportionInfoPO();
        }
        OrderItemApportionInfoRelatedVO orderItemApportionInfoRelatedVO = apportionInfoCalculateService.prepareRelatedVO(orderApportionInfoQueryVO);
        log.info("Related data get, result is " + GsonUtils.toJson(orderItemApportionInfoRelatedVO));
        OrderApportionInfoPO orderApportionInfoPO = calculateOrderApportionInfoPO(orderItemApportionInfoRelatedVO);
        log.info("Calculate apportion info for query vo " + GsonUtils.toJson(orderApportionInfoQueryVO) + " completed, result is " + GsonUtils.toJson(orderApportionInfoPO));
        return orderApportionInfoPO;
    }

    //检查查询参数是否合法
    private boolean checkQueryVO(OrderApportionInfoQueryVO orderApportionInfoQueryVO) {
        return !(orderApportionInfoQueryVO == null
                || (NumberUtils.isNotAboveZero(orderApportionInfoQueryVO.getOrderId()) && NumberUtils.isNotAboveZero(orderApportionInfoQueryVO.getOrderItemId())
                    && CollectionUtils.isEmpty(orderApportionInfoQueryVO.getOrderItemIdList())));
    }

    /**
     * 根据订单(包含子单)、分摊信息、子单分摊情况信息、退款信息，计算出订单的分摊相关信息
     * */
    private OrderApportionInfoPO calculateOrderApportionInfoPO(OrderItemApportionInfoRelatedVO orderItemApportionInfoRelatedVO) {
        if(orderItemApportionInfoRelatedVO == null || CollectionUtils.isEmpty(orderItemApportionInfoRelatedVO.getOrderItemList())) {
            return new OrderApportionInfoPO();
        }
        List<OrdOrderItem> orderItemList = orderItemApportionInfoRelatedVO.getOrderItemList();
        if(CollectionUtils.isEmpty(orderItemList)) {
            log.warn("Order item result is empty, please check");
            return new OrderApportionInfoPO();
        }
        List<OrderItemApportionInfoPO> orderItemApportionInfoPOList = new ArrayList<>();
        for (OrdOrderItem orderItem : orderItemList) {
            OrderItemApportionInfoPO orderItemApportionInfoPO = calculateItemApportionInfo(orderItem, orderItemApportionInfoRelatedVO);
            if(orderItemApportionInfoPO == null) {
                continue;
        }
        orderItemApportionInfoPOList.add(orderItemApportionInfoPO);
        }
        OrderApportionInfoPO orderApportionInfoPO = new OrderApportionInfoPO();
        orderApportionInfoPO.setOrderId(orderItemList.get(0).getOrderId());
        orderApportionInfoPO.setOrderItemApportionInfoPOList(orderItemApportionInfoPOList);
        return orderApportionInfoPO;
    }

    /**
     * 计算子单的分摊信息
     * */
    private OrderItemApportionInfoPO calculateItemApportionInfo(OrdOrderItem orderItem, OrderItemApportionInfoRelatedVO orderItemApportionInfoRelatedVO) {
        if(orderItemApportionInfoRelatedVO == null) {
            return new OrderItemApportionInfoPO();
        }
        OrderEnum.ORDER_APPORTION_PARTICLE orderApportionParticle = ApportionUtil.judgeApportionParticle(orderItem.getCategoryId());
        ApportionParticleService apportionParticleService = apportionParticleServiceFactory.catchOrderDetailApportionCompleteService(orderApportionParticle);

        return apportionParticleService.generateItemApportionInfoPO(orderItem, orderItemApportionInfoRelatedVO);
    }
}
