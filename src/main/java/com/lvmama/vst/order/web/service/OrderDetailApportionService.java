package com.lvmama.vst.order.web.service;

import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.vo.order.OrderHotelTimeRateInfo;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;

import java.util.List;
import java.util.Map;

/**
 * Created by zhouyanqun on 2017/4/18.
 * 订单详情展示相关的服务
 */
public interface OrderDetailApportionService {
    /**
     * 计算订单详情中，子单的分摊信息
     * 目前订单详情中，子单的信息只有一列：实付金额
     * */
    void calcOrderDetailItemApportion(Long orderId, Map<String, List<OrderMonitorRst>> resultMap, Map<Long, List<OrdMulPriceRate>> itemIdWithMulPriceMap);

    /**
     * 根据子订单，生成子单分摊信息
     * */
    OrderItemApportionInfoPO generateItemApportionInfoPO4Detail(OrdOrderItem orderItem);

    /**
     * 根据子单，生成子单分摊信息列表
     * */
    List<OrderHotelTimeRateInfo> generateHotelTimeRateInfoList(OrdOrderItem orderItem);

    /**
     * 把分摊信息按照多价格的顺序排列
     * 如果多价格对应的分摊信息有残缺，补充上空的
     * */
    void sortAndCompleteApportionInfoByMulPrice(OrderItemApportionInfoPO orderItemApportionInfoPO, List<OrdMulPriceRate> ordMulPriceList);
}
