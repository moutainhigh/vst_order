package com.lvmama.vst.order.service.book;

import com.lvmama.dest.api.order.vo.HotelOrderUpdateStockDTO;
import com.lvmama.vst.order.vo.OrdOrderDTO;

import java.util.List;

public interface IOrderSaveService {
	/**
	 * 保存订单，走事务
	 * @param order
	 */
	public List<HotelOrderUpdateStockDTO> saveOrder(OrdOrderDTO order,List<HotelOrderUpdateStockDTO> asynchronousOrdUpdateStockList);
}
