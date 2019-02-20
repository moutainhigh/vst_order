package com.lvmama.vst.order.timeprice.adapter;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.goods.po.SuppGoodsGroupStock;

public interface IHotelGoodsGroupQueryAdapterService {

	public List<SuppGoodsGroupStock> selectBySpecDateRangeAndGroupId(Map<String, Object> params);

}
