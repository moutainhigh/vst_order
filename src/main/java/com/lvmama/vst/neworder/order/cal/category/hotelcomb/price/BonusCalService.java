package com.lvmama.vst.neworder.order.cal.category.hotelcomb.price;

import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.BonusAmount;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.order.vo.OrdOrderDTO;

public interface BonusCalService {
	
	public BonusAmount  getBonusAmountOfBuyUserNo(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo);

}
