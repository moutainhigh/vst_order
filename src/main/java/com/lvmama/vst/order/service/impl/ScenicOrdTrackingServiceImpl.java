package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdOrderTracking;
import com.lvmama.vst.order.service.IOrdOrderTrackingService;
import com.lvmama.vst.order.service.ScenicOrdTrackingService;

@Service("scenicOrdTrackingService")
public class ScenicOrdTrackingServiceImpl implements ScenicOrdTrackingService {
    @Autowired
    private IOrdOrderTrackingService ordOrderTrackingService;

    @Override
    public int updateByPrimaryKeySelective(OrdOrderTracking ordOrderTracking) {
        return ordOrderTrackingService.updateByPrimaryKeySelective(ordOrderTracking);
    }

    @Override
    public int insert(OrdOrderTracking ordOrderTracking) {
        return ordOrderTrackingService.insert(ordOrderTracking);
    }

    @Override
    public int updateOrderStatusByOrderIdAndStatus(Map<String, Object> paramsMap) {
        return ordOrderTrackingService.updateOrderStatusByOrderIdAndStatus(paramsMap);
    }

    @Override
    public List<OrdOrderTracking> selectByOrderIdAndStatus(
            Map<String, Object> paramsMap) {
        return ordOrderTrackingService.selectByOrderIdAndStatus(paramsMap);
    }

    @Override
    public int deleteByPrimaryKey(Long trackingId) {
        return ordOrderTrackingService.deleteByPrimaryKey(trackingId);
    }

    @Override
    public List<OrdOrderTracking> findNowOrderStatusByOrderId(Long orderId) {
        return ordOrderTrackingService.findNowOrderStatusByOrderId(orderId);
    }

    @Override
    public void saveOrderTracking(OrdOrderTracking ordOrderTracking) {
        ordOrderTrackingService.saveOrderTracking(ordOrderTracking);
    }
}
