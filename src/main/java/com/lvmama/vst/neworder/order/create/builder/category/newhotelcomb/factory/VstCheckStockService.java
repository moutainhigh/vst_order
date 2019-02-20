package com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory;

import com.lvmama.dest.hotel.trade.common.RequestBody;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombTradeBuyInfoVo;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.SupplierProductInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.web.BusinessException;

public interface VstCheckStockService {

	

	/**
	 * 检查商品库存情况
	 * 
	 * @param distributionId
	 * @param goodsId
	 * @param visitTime
	 * @return
	 * @throws BusinessException
	 */
	public ResultHandleT<SupplierProductInfo> checkStock(HotelCombTradeBuyInfoVo.Item item,Long distributionId);

}
