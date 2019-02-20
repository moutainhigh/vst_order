package com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory;

import java.util.List;

import com.lvmama.dest.api.hotelcomb.vo.HotelCombBuyInfoVo;
import com.lvmama.dest.hotel.trade.common.RequestBody;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombTradeBuyInfoVo;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.order.BuyInfo;

public interface ICheckOrderStock {
  /**
   *  酒套餐商品校验库存
   *  @author fangxiang
   *  @param hotelCombBuyInfoVo
   *  @return
   */
	boolean   checkStock(List<HotelCombBuyInfoVo> hotelCombBuyInfoVo)  ;
	
	/**
	 * 酒店套餐关联销售商品校验库存
	 * @param request
	 * @return
	 */
	public boolean checkStock(HotelCombTradeBuyInfoVo.Item item,Long distributionId);

}
