/**
 * 
 */
package com.lvmama.vst.order.service.book;

import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.order.dao.OrdItemPersonRelationDao;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.service.IOrdMulPriceRateService;

/**
 * @author lancey
 *
 */
public abstract class AbstractOrderItemSaveBussiness {

	@Autowired
	protected OrdOrderStockDao orderStockDao;
	
	@Autowired
	private OrdItemPersonRelationDao itemPersonRelationDao;
	
	@Autowired
	protected IOrdMulPriceRateService ordMulPriceRateService;
	
	protected void saveStock(OrdOrderItem orderItem){
		for(OrdOrderStock stock:orderItem.getOrderStockList()){
			stock.setOrderItemId(orderItem.getOrderItemId());
			stock.setObjectId(orderItem.getOrderItemId());
			stock.setObjectType(OrderEnum.ORDER_STOCK_OBJECT_TYPE.ORDERITEM.name());
			orderStockDao.insert(stock);
		}
	}
	
	protected void savePersonRelation(OrdOrderItem orderItem){
		for(OrdItemPersonRelation relation:orderItem.getOrdItemPersonRelationList()){
			relation.setOrdPersonId(relation.getOrdPerson().getOrdPersonId());
			relation.setOrderItemId(orderItem.getOrderItemId());
			itemPersonRelationDao.insert(relation);
		}
	}
}
