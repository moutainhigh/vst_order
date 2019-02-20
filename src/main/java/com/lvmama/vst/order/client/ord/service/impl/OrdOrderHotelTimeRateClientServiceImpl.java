package com.lvmama.vst.order.client.ord.service.impl;

import com.lvmama.vst.back.client.ord.service.OrdOrderHotelTimeRateClientService;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.order.service.IOrdOrderHotelTimeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by lijuntao on 2017/5/8.
 */
@Component("ordOrderHotelTimeRateClientServiceRemote")
public class OrdOrderHotelTimeRateClientServiceImpl implements OrdOrderHotelTimeRateClientService {

    @Autowired
    private IOrdOrderHotelTimeRateService ordOrderHotelTimeRateService;

    @Override
    public List<OrdOrderHotelTimeRate> findOrdOrderHotelTimeRateListByParams(Map<String, Object> params) {
        return ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateListByParams(params);
    }
}
