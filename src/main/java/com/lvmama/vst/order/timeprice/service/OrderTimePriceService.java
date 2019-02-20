package com.lvmama.vst.order.timeprice.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;

/**
 * 
 * @author sunjian
 *
 */
public interface OrderTimePriceService {

	public ResultHandleT<Object> checkStock(SuppGoods suppGoods, Item item, Long distributionId, Map<String, Object> dataMap);
	
	public void updateStock(Long timePriceId, Long stock, Map<String, Object> dataMap);
	
	public ResultHandle validate(SuppGoods suppGoods, Item item, OrdOrderItemDTO orderItem, OrdOrderDTO order);
	
	/**
	 * 获得供应商商品价格
	 * 
	 * @param checkAhead 是否加入查询条件：“提前预定”
	 */
	public ResultHandleT<SuppGoodsBaseTimePrice> getTimePrice(Long goodsId, Date specDate, boolean checkAhead);
	
	public void updateRevertStock(Long suppGoodsId, Date specDate, Long stock, Map<String, Object> dataMap);
	
	/**
	 * 返回一个时间价格表的前辍，
	 * 处理多个不同的时间价格表之间的主键相同问题
	 * @return
	 */
	public String getTimePricePrefix();
	
	/**
	 * 计算促销后结算价
	 */
	public void calcSettlementPromotion(final OrdOrderItem orderItem,final List<OrdPromotion> promotions);
	
	/**
	 * 目的地BU保险库存校验接口
	 * @param suppGoods
	 * @param item
	 * @param distributionId
	 * @param dataMap
	 * @return
	 */
	public ResultHandleT<Object> destBucheckStock(SuppGoods suppGoods, com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo.Item item, Long distributionId, Map<String, Object> dataMap);
 
	/**
	 * 目的地订单保险商品校验
	 * @param suppGoods
	 * @param item
	 * @param orderItem
	 * @param order
	 * @return
	 */
	public ResultHandle validate(SuppGoods suppGoods, DestBuBuyInfo.Item item, OrdOrderItemDTO orderItem, OrdOrderDTO order);
}
