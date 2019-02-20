package com.lvmama.vst.neworder.order.router;

import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.neworder.order.vo.BuyOutTimePrice;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;

/**
 * Created by fangxiang on 176/20.
 */
public interface IBuyOutTimePrice {
	/**
	 *酒套餐时间价格表 包含预控相关数据
	 *@param suppGood
	 *@param Item
	 *@return
	 */
	public BuyOutTimePrice  getTimePrice(SuppGoods goods,OrderHotelCombBuyInfo.GoodsItem goodItem,OrderHotelCombBuyInfo.Item item);

}
