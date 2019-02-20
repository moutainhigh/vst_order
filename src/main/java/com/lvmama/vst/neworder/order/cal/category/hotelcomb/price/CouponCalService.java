package com.lvmama.vst.neworder.order.cal.category.hotelcomb.price;

import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.CouponAmount;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.order.vo.OrdOrderDTO;

public interface CouponCalService {

	/**
	 * 验证优惠并获取优惠信息
	 * 
	 * @author fangxiang
	 * @param order
	 * @param buyInfo
	 * @return
	 */
	public CouponAmount getOrderCoupoAmount(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo);

}
