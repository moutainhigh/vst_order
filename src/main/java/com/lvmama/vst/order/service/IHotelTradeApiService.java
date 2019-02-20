package com.lvmama.vst.order.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.lvmama.dest.api.order.vo.HotelOrderUpdateStockDTO;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.SupplierProductInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.PriceInfo;
public interface IHotelTradeApiService {
	
	
	public boolean checkIsHotelProduct(BuyInfo buyInfo);

	/**
	 * 计算单酒店订单价格
	 * @param buyInfo
	 * @return
	 */
	public PriceInfo countPriceByHotel(BuyInfo buyInfo);

	/**
	 * 酒店下单商品库存检查
	 * 
	 * @param buyInfo
	 * @return ResponseBody<SupplierProductInfo>
	 */
	public ResultHandleT<SupplierProductInfo> checkStock(BuyInfo buyInfo);
	/**
	 * 
	 * @Title: deductStock
	 * @Description: 酒店库存扣减操作(下单)
	 * @param hotelUpdateStock
	 * @return ResultHandleT<HotelOrderUpdateStockDTO> 返回类型
	 */
	public ResultHandleT<HotelOrderUpdateStockDTO> deductStock(HotelOrderUpdateStockDTO hotelUpdateStock);
	/**
	 * 
	 * @Title: updateRevertStock
	 * @Description: 酒店商品库存恢复操作(取消下单)
	 * @param hotelStockMap
	 * @return ResultHandle 返回类型
	 */
	public ResultHandle updateRevertStock(Map<String, Map<Date, List<OrdOrderStock>>> hotelStockMap);

	/**
	 * 
	 * @Title: updateRevertStock
	 * @Description: 酒店商品库存恢复操作(下单扣减库存失败，事务性)
	 * @param revertStock
	 * @return ResultHandle 返回类型
	 */
	public ResultHandle revertStockForDeductFail(List<HotelOrderUpdateStockDTO> revertStocks);
	
	
	/**
	 * 共享库存
	 * @param groupId
	 * @param specDate
	 * @return
	 */
	public Long getHotelShareStock(Long groupId,Date specDate);
	
	/**
	 * 下单查询时间价格表，不走缓存
	 * @param goodsId
	 * @param specDate
	 * @return
	 */
	public SuppGoodsTimePrice getHotelGoodsTimePrice(Long goodsId,Date specDate);
	
	/**
	 * 根据路由来查询共享库存ID
	 * @param groupId
	 * @param visitTime
	 * @param suppGoodsId
	 * @return
	 */
	public Long hasSharedStock(Long groupId, Date visitTime,Long suppGoodsId);
	
	/**
	 * 酒店套餐共享库存还原
	 * @param hotelOrderUpdateStock
	 * @return
	 */
	public Integer revertHotelCombGroupStock(HotelOrderUpdateStockDTO hotelOrderUpdateStock);
}
