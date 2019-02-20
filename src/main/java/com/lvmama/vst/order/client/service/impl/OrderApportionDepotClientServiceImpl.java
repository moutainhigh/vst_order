package com.lvmama.vst.order.client.service.impl;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.client.service.OrderApportionDepotClientService;
import com.lvmama.vst.order.service.OrderApportionDepotService;
import com.lvmama.vst.order.utils.ApportionUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by zhouyanqun on 2017/5/21.
 */
@Component("orderApportionDepotClientServiceRemote")
public class OrderApportionDepotClientServiceImpl implements OrderApportionDepotClientService {
    private static final Log log = LogFactory.getLog(OrderApportionDepotClientServiceImpl.class);
    @Resource
    private OrderApportionDepotService orderApportionDepotService;
    /**
     * 添加订单记录到分摊仓库,返回添加的记录id
     * 返回添加的记录的id
     *
     * @param orderApportionDepot
     */
    @Override
    public ResultHandleT<Long> addOrderApportionDepot(Long orderId) {
        ResultHandleT<Long> resultHandleT = new ResultHandleT<>();
        //判断分摊开关是否开启
        if(!ApportionUtil.isApportionEnabled()) {
            String errorMsg = "Apportion is not enabled can't add order depot, please check";
            log.info(errorMsg);
            resultHandleT.setMsg(errorMsg);
            return resultHandleT;
        }
        if(NumberUtils.isNotAboveZero(orderId)) {
            resultHandleT.setMsg("Order id is illegal!");
            return resultHandleT;
        }
        try {
            Long orderApportionDepotId = orderApportionDepotService.addOrderApportionDepot(orderId);
            resultHandleT.setReturnContent(orderApportionDepotId);
        } catch (Exception e) {
            log.error("Error occurs while add order " + orderId + " to depot by remote interface.", e);
            resultHandleT.setMsg(e.getMessage());
        }
        return resultHandleT;
    }
}
