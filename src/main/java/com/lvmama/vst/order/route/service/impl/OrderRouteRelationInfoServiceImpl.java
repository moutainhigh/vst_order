package com.lvmama.vst.order.route.service.impl;

import com.lvmama.vst.order.route.po.OrderRouteRelationInfo;
import com.lvmama.vst.order.route.dao.OrderRouteRelationInfoDao;
import com.lvmama.vst.order.route.service.IOrderRouteRelationInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class OrderRouteRelationInfoServiceImpl implements IOrderRouteRelationInfoService {

    @Resource
    private OrderRouteRelationInfoDao orderRouteRelationInfoDao;

    @Override
    public int insert(OrderRouteRelationInfo orderRouteRelationInfo) {
        return orderRouteRelationInfoDao.insert(orderRouteRelationInfo);
    }

    /**
     * 根据条件查询订单路由表记录
     *
     * @param paramMap
     */
    @Override
    public OrderRouteRelationInfo queryOrderRouteRelationInfo(Map<String, Object> paramMap) {
        return orderRouteRelationInfoDao.queryOrderRouteRelationInfo(paramMap);
    }
}
