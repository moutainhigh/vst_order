package com.lvmama.vst.neworder.order.cal;

import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.ProductAmountItem;
import com.lvmama.vst.neworder.order.vo.BaseBuyInfo;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * Created by dengcheng on 17/3/6.
 */
public interface IOrderCalFactory {
    /**
     * 构建金额原始数据
     * @param buyInfo
     * @return
     */
	ProductAmountItem buildProductAmountItem(OrdOrderDTO ordOrder,BaseBuyInfo<OrderHotelCombBuyInfo> baseBuyInfo);
}
