package com.lvmama.vst.neworder.order.router;

import org.springframework.stereotype.Component;

import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;

import java.util.Date;

import javax.annotation.Resource;

/**
 * Created by dengcheng on 17/4/14.
 */
@Component
public interface ILookUpService {
    /**
     * 查找商品服务
     * @param categoryId
     * @return
     */
    IGoodsRouterService lookUpGoodsService(Long categoryId);

    /**
     * 查找时间价格服务
     * @param categoryId
     * @return
     */
    ITimePriceRouterService lookUptTimePriceService(Long categoryId,Long goodsId,Date date,boolean withBuyOutPrice);

    /**
     * 查找产品服务
     * @param categoryId
     * @return
     */
    IProductRouterService  lookUpProductService(Long categoryId);
    

   /**
    * 
    * 查找门票保险时间价格表路由
    */
    OrderTimePriceService lookupTicketTimePrice(Long categoryId);
}
