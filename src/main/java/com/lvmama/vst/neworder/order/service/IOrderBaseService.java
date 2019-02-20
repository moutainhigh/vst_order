package com.lvmama.vst.neworder.order.service;

import com.google.common.base.Preconditions;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.neworder.order.cal.IOrderCalFactory;
import com.lvmama.vst.neworder.order.cancel.IOrderCancelService;
import com.lvmama.vst.neworder.order.create.builder.category.IOrderDTOFactory;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.ProductAmountItem;
import com.lvmama.vst.neworder.order.create.deduct.IDeductFactory;
import com.lvmama.vst.neworder.order.create.persistance.category.IOrderDbStoreFactory;
import com.lvmama.vst.neworder.order.vo.BaseBuyInfo;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by dengcheng on 17/2/22.
 */
@Component
public abstract class IOrderBaseService {


    /**
//     * 订单临时对象DTO构建器
//     * @param buyInfo
//     * @return
//     */

    protected  IOrderDTOFactory baseDTOFactory;

    protected  IOrderDbStoreFactory baseDbStoreFactory;

    @Resource
    protected IOrderCancelService newOrderCancelService;

    protected IDeductFactory baseDeductService;

    protected IOrderCalFactory calFactory;
    
    

    protected OrdOrderDTO orderDTOCreator(BaseBuyInfo buyInfo){
        Preconditions.checkNotNull(baseDTOFactory,"baseDTOFactory 实例不能为空");
       return baseDTOFactory.buildDTO(buyInfo);
    }

    
    protected OrdOrderDTO orderBaseCreator(BaseBuyInfo buyInfo){
    	 Preconditions.checkNotNull(baseDTOFactory,"baseDTOFactory 实例不能为空");
         return baseDTOFactory.buildBaseDTO(buyInfo);
    }

    protected  OrdOrder persistanceOrder(OrdOrderDTO order) {
        Preconditions.checkNotNull(baseDbStoreFactory,"baseStoreFactory 实例不能为空");
        return baseDbStoreFactory.persistanceOrder(order);
    }

    protected  void setOrderNormal(OrdOrderDTO order){
         baseDbStoreFactory.setOrderNormal(order);
    }

    protected  void deductOrder(OrdOrderDTO order){
        Preconditions.checkNotNull(baseDeductService,"baseDeductService 实例不能为空");
//        throw new RuntimeException("ddddd");
        baseDeductService.deductOrder(order);
    }

    protected ProductAmountItem calPrice(OrdOrderDTO order,BaseBuyInfo buyInfo){
    	
        return  calFactory.buildProductAmountItem(order,buyInfo);
    }



}


