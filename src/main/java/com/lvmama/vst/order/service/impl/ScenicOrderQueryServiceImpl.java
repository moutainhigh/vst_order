package com.lvmama.vst.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.order.po.OrdAddress;
import com.lvmama.vst.order.service.IOrdAddressService;
import com.lvmama.vst.order.service.ScenicOrderQueryService;

@Service("scenicOrderQueryService")
public class ScenicOrderQueryServiceImpl implements ScenicOrderQueryService {
    @Autowired
    private IOrdAddressService ordAddressService;

    @Override
    public List<OrdAddress> findOrdAddressList(Map<String, Object> params) {
        return ordAddressService.findOrdAddressList(params);
    }
}
