package com.lvmama.vst.order.service.book;

import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.OrderCheckAheadTimeVo;

/**
 * 订单下单检测
 * @author Rumly
 */
public interface OrderCheckService {

    /**
     * 检查线路提前预定时间
     * @param buyInfo
     * @return
     */
    @ReadOnlyDataSource
    ResultHandleT<OrderCheckAheadTimeVo> checkAheadTime(BuyInfo buyInfo);

}
