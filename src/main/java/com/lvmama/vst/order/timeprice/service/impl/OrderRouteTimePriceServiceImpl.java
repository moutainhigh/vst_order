/**
 * 
 */
package com.lvmama.vst.order.timeprice.service.impl;

import org.springframework.stereotype.Component;

import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;

/**
 * @author lancey
 *
 */
@Component("orderLineTimePriceService")
public class OrderRouteTimePriceServiceImpl extends
		AbstractOrderLineTimePriceServiceImpl {

	@Override
	protected void checkParam(SuppGoods suppGoods, Item item, boolean ck) {
		if(suppGoods==null){
			throw new IllegalArgumentException("商品ID=" + item.getGoodsId() + "不存在");
		}
		if(ck){
			if (item.getTotalPersonQuantity() <= 0) {
				throw new IllegalArgumentException("商品 " + suppGoods.getGoodsName() + " 订购数量小于等于零");
			}

			if(item.getTotalPersonQuantity() > suppGoods.getMaxQuantity()){
				throw new IllegalArgumentException("商品 " + suppGoods.getGoodsName() + " 订购数量超出最大值");
			}
			
			if(item.getTotalPersonQuantity() <suppGoods.getMinQuantity()){
				throw new IllegalArgumentException("商品 " + suppGoods.getGoodsName() + " 订购数量小于最小值");
			}
			
			if (item.getOwnerQuantity() > item.getTotalPersonQuantity()) {
				throw new IllegalArgumentException("商品" + suppGoods.getGoodsName() + "  实际订购数量小于零");
			}
		}
	}

	
}
