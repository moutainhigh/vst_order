package com.lvmama.vst.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.order.service.ScenicOrderSaveService;
import com.lvmama.vst.order.service.book.OrderSaveService;
import com.lvmama.vst.order.vo.OrdOrderDTO;

@Service("scenicOrderSaveService")
public class ScenicOrderSaveServiceImpl implements ScenicOrderSaveService {

    @Autowired
    private  OrderSaveService orderSaveService;

    @Override
    public void saveOrder(String jsonOrder) {
        OrdOrderDTO ordOrderDTO = JSONObject.parseObject(jsonOrder, OrdOrderDTO.class);
        orderSaveService.saveOrder(ordOrderDTO);
    }
}
