package com.lvmama.vst.neworder.order.cal.category.hotelcomb;

import com.lvmama.vst.comm.vo.order.BuyPresentActivityInfo;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.BonusAmount;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.CouponAmount;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.ExpressAmount;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.GoodsAmount;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.ProductAmountItem;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.SaleAmount;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.order.vo.OrdOrderDTO;

import java.util.List;

/**
 * Created by dengcheng on 17/2/23.
 */
public interface ICalProduct {

    /**
     * 构建促销相关金额信息
     * @param buyInfo
     * @return
     */
    List<SaleAmount> buildSaleAmount(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo,ProductAmountItem ammountItem );

    /**
     * 构建快递相关金额信息
     * @param buyInfo
     * @return
     */
    List<ExpressAmount> buildExpressAmount(OrdOrderDTO order);

    /**
     * 构建商品相关金额信息
     * @param buyInfo
     * @return
     */
    List<GoodsAmount> buildGoodsAmount(OrdOrderDTO order);
    /**
     *优惠券金计算
     *@author fangxiang
     *@param order
     *@return
     */
	List<CouponAmount> buildCouponAmount(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo);
	
	/**
	 *最大奖金计算以及奖金计算 
	 *@author fangxiang
	 *@param  order
	 *@param buyInfo
	 *@return 
	 */
   public BonusAmount  buildBonusAmount(OrdOrderDTO order,OrderHotelCombBuyInfo buyInfo);
}
