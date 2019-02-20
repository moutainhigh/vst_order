package com.lvmama.vst.neworder.order.cal.category.hotelcomb.price;

import java.util.List;

import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.order.vo.OrdOrderDTO;

public interface PromotionForHotelcomService {

	/**
	 * 验证促销
	 * 
	 * @author fangxiang
	 * @param promotiongList
	 * @param buyInfo
	 */
	public void checkPromotion(List<PromPromotion> promotionList, OrderHotelCombBuyInfo buyInfo);

	/**
	 * 促销促销列表
	 * 
	 * @author fangxiang
	 * @param order
	 * @param buyInfo
	 * @return
	 */
	public List<PromPromotion> findPromPromotion(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo);
}
