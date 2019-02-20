package com.lvmama.vst.order.client.ord.service.impl;

import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.vst.back.client.ord.service.OrdUserCouponOrderClientService;
import com.lvmama.vst.order.dao.OrdOrderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by Administrator on 2018/9/4.
 */
@Component("ordUserCouponOrderServiceRemote")
public class OrderUserCouponOrderClientServiceImpl implements OrdUserCouponOrderClientService {

    @Autowired
    private OrdOrderDao ordOrderDao;

    @Override
    @ReadOnlyDataSource
    public Long queryUserOrderCountByParams(Map<String, Object> params) {
        return ordOrderDao.queryUserOrderCountByParams(params);
    }
}
