package com.lvmama.vst.neworder.order.cancel.category.hotelcomb.handler;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombOrderService;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelOrdOrderVo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.neworder.order.cal.category.hotelcomb.product.CalProductServiceImpl;

/**
 * Created by dengcheng on 17/4/12.
 * 取消后酒店套餐库存还原逻辑
 */
@Component
public class CancelForStockProcessor {

    private static final Logger LOG = LoggerFactory
            .getLogger(CalProductServiceImpl.class);
    @Resource
    IHotelCombOrderService hotelCombOrderService;
   
    @Subscribe
    public void handler(OrdOrder order) {
        List<HotelOrdOrderVo> hotelOrdOrderVoList = Lists.newArrayList();
        for (OrdOrderItem item:
             order.getOrderItemList()) {
            //如果是酒套餐
             if("32".equals(item.getCategoryId().toString())){
                 HotelOrdOrderVo hotelOrdOrderVo = new HotelOrdOrderVo();
                 hotelOrdOrderVo.setOrderId(item.getOrderId());
                 //hotelOrdOrderVo.setOrdOrderItemId(item.getOrderItemId());
                 hotelOrdOrderVoList.add(hotelOrdOrderVo);
             }
        }
        LOG.info("CancelForStockProcessor---returnHotelCombOrderStock--orderId:"+order.getOrderId());
        //hotelCombOrderService.returnHotelCombOrderStock(new RequestBody<List<HotelOrdOrderVo>>().setTFlowStyle(hotelOrdOrderVoList, NewOrderConstant.VST_ORDER_TOKEN));
    }
}
