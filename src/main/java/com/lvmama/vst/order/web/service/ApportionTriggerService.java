package com.lvmama.vst.order.web.service;

import com.lvmama.vst.order.web.vo.OrderApportionMsgVO;
import com.lvmama.vst.order.web.vo.OrderApportionVO;

/**
 * Created by zhouyanqun on 2017/6/1.
 */
public interface ApportionTriggerService {
    /**
     * 分摊并且保存订单上所有的金额，包括优惠、促销、渠道优惠、手工改价、实付，一共5种类型的金额
     * 1. 作废以前的所有分摊项
     * 2. 执行分摊
     * 3. 删除order_apportion_depot表的记录
     * */
    void apportionAndSaveFullAmount(OrderApportionVO orderApportionVO);

    /**
     * 处理消息，用于模拟消息
     * */
    void sendOrderMsg(OrderApportionMsgVO orderApportionMsgVO);
}
