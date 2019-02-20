package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.vo.order.OrderChannelJudgeVO;
import com.lvmama.vst.order.service.OrderChannelService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouyanqun on 2016/11/25.
 */
@Component
public class OrderChannelServiceImpl implements OrderChannelService {

    /**
     * 无线distributionChannel字段的枚举值，通过这个字段判断订单是否来自无线
     * */
    private static final List<Long> wirelessDistributionChannelList = new ArrayList<>(3);
    static {
        wirelessDistributionChannelList.add(10000L);
        wirelessDistributionChannelList.add(10001L);
        wirelessDistributionChannelList.add(10002L);
    }

    /**
     * 判断订单的下单渠道
     * 如果因为有字段缺导致无法确定下单渠道，则返回“未知渠道”
     * 目前只判断是否是无线的订单，后台如果有需求，可以在这个方法里面加
     * @param orderChannelJudgeVO
     */
    @Override
    public OrderEnum.OrderChannel judgeOrderChannel(OrderChannelJudgeVO orderChannelJudgeVO) {
        if(orderChannelJudgeVO == null || orderChannelJudgeVO.getDistributionId() == null) {
            return OrderEnum.OrderChannel.UNKNOWN;
        }

        if(isWirelessOrder(orderChannelJudgeVO)){
            return OrderEnum.OrderChannel.WIRELESS;
        }

        return OrderEnum.OrderChannel.UNKNOWN;
    }

    /**
     * 两个判断条件：distributorId = 4, distributionChannel in (10000, 10001, 10002)
     * */
    private boolean isWirelessOrder(OrderChannelJudgeVO orderChannelJudgeVO) {
        return orderChannelJudgeVO != null && orderChannelJudgeVO.getDistributionId() != null && orderChannelJudgeVO.getDistributionId() == 4
                && wirelessDistributionChannelList.contains(orderChannelJudgeVO.getDistributionChannel());
    }
}
