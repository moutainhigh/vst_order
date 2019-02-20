package com.lvmama.vst.order.service.impl;

import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.service.OrdOrderTravellerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by zhouyanqun on 2016/5/7.
 */
@Service("orderTravellerService")
public class OrdOrderTravellerServiceImpl implements OrdOrderTravellerService {
    @Resource
    private OrdOrderDao orderDao;
    /**
     * 锁定订单游玩人
     * 为了使用事务，所以方法名中含有update
     * @param orderId
     */
    @Override
    public int updateOrderLockTraveller(Long orderId) {
        if(orderId == null || Long.valueOf(orderId) <= 0){
            return 0;
        }
        return orderDao.lockOrderTraveller(orderId);
    }
}
