package com.lvmama.vst.neworder.order.create.deduct.product.category;

import com.google.common.collect.Lists;
import com.lvmama.cmt.comm.vo.BusinessException;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombOrderService;
import com.lvmama.dest.api.hotelcomb.vo.HotelOrdOrderVo;
import com.lvmama.vst.back.order.po.OrderStatusEnum;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.neworder.order.create.deduct.IDeductFactory;
import com.lvmama.vst.neworder.order.create.deduct.product.category.hotelcomb.IHotelCombDeductRemoteDelegateService;
import com.lvmama.vst.neworder.order.create.deduct.product.category.vst.VstDeductStockService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.annotation.Resource;

/**
 * Created by dengcheng on 17/4/24.
 */
@Component("hotelCombDeductServiceFactory")
public class HotelCombDeductServiceFactory implements IDeductFactory{
    @Resource
    IHotelCombDeductRemoteDelegateService hotelCombDeductRemoteDelegateService;
   
    @Resource
    VstDeductStockService  vstDeductStockService;
    
    @Resource
    IOrderLocalService  orderServiceRemote;
   
    @Resource
    IHotelCombOrderService hotelCombOrderService;
    
    @Override
    public void deductOrder(OrdOrderDTO order) {
//        step  1
        try{
        hotelCombDeductRemoteDelegateService.deductStock(order);
       //扣减门票保险基本库存以及预控库存
        vstDeductStockService.deductStock(order);
        }catch(BusinessException e){
        	throw new BusinessException(
					String.format("%s,%s", OrderStatusEnum.ORDER_ERROR_CODE.HOTEL_COMB_DEDUCT_FAILURE.getErrorCode(),
							OrderStatusEnum.ORDER_ERROR_CODE.HOTEL_COMB_DEDUCT_FAILURE.getContent()));
    
        }
    }
}
