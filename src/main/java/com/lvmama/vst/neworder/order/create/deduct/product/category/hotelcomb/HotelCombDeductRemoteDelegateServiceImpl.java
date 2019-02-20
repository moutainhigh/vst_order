package com.lvmama.vst.neworder.order.create.deduct.product.category.hotelcomb;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombOrderService;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombDbCheckResultVo;
import com.lvmama.dest.api.hotelcomb.vo.SuppGoodsTimeStockDeductVo;
import com.lvmama.dest.hotel.trade.utils.BusinessException;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderStatusEnum;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by dengcheng on 17/4/24.
 */
@Component("hotelCombDeductRemoteDelegateService")
public class HotelCombDeductRemoteDelegateServiceImpl implements IHotelCombDeductRemoteDelegateService {
    @Resource
    IHotelCombOrderService hotelCombOrderService ;
    private static final Log LOG = LogFactory.getLog(HotelCombDeductRemoteDelegateServiceImpl.class);
    @Override
    public void deductStock(OrdOrderDTO order) {
        List<SuppGoodsTimeStockDeductVo> suppGoodsTimeStockDeductVos = Lists.newArrayList();
        for (OrdOrderItem orderItem:order.getOrderItemList()) {
            /**
             * 如果是酒店套餐品类才会去酒店子系统扣减库存
             */
            if(32L==orderItem.getCategoryId()){
                SuppGoodsTimeStockDeductVo suppGoodsTimeStockDeductVo = new SuppGoodsTimeStockDeductVo();
                suppGoodsTimeStockDeductVo.setDeductStock(orderItem.getQuantity().intValue());
                suppGoodsTimeStockDeductVo.setPricePlaneId(orderItem.getPricePlanId());
                suppGoodsTimeStockDeductVo.setSuppGoodsId(orderItem.getSuppGoodsId());
                suppGoodsTimeStockDeductVo.setSpecDate(orderItem.getVisitTime());
                suppGoodsTimeStockDeductVo.setVstOrderId(orderItem.getOrderId());
                suppGoodsTimeStockDeductVo.setVstOrdOrderItemId(orderItem.getOrderItemId());
                suppGoodsTimeStockDeductVos.add(suppGoodsTimeStockDeductVo);
                LOG.info("=========== itemId ="+orderItem.getOrderItemId());
            }

        }
        LOG.info("begin hotelcomb deduct "+ JSON.toJSONString(suppGoodsTimeStockDeductVos));
        ResponseBody<List<HotelCombDbCheckResultVo>> response = hotelCombOrderService.submitOrder(new RequestBody<List<SuppGoodsTimeStockDeductVo>>().setTFlowStyle(suppGoodsTimeStockDeductVos,NewOrderConstant.VST_ORDER_TOKEN));
         LOG.info("HotelCombDeductRemoteDelegateServiceImpl--hotelCombOrderService.submitOrder--response:"+JSON.toJSONString(response));
         LOG.info("HotelCombDeductRemoteDelegateServiceImpl--hotelCombOrderService.submitOrder--response.getT():"+JSON.toJSONString(response.getT()));
        for (HotelCombDbCheckResultVo resultVo:response.getT()) {
            if (response.isSuccess() && resultVo.getEffectCount() > 0) {
                LOG.info("hotelcomb deduct success");
            } else {
                throw new BusinessException(String.format("%s,%s", OrderStatusEnum.ORDER_ERROR_CODE.HOTEL_COMB_DEDUCT_FAILURE.getErrorCode(), OrderStatusEnum.ORDER_ERROR_CODE.HOTEL_COMB_DEDUCT_FAILURE.getContent()));
            }
        }
    }
}
