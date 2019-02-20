/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.ticket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderItemSaveBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * @author lancey
 *
 */
@Component("ticketOrderItemSaveBussiness")
public class TicketOrderItemSaveBussiness extends AbstractBookService implements OrderItemSaveBussiness{

	@Autowired
	private OrdOrderStockDao orderStockDao;
	
	@Override
	public void saveAddition(OrdOrderDTO order, OrdOrderItem orderItem) {
		for(OrdOrderStock stock:orderItem.getOrderStockList()){
			stock.setOrderItemId(orderItem.getOrderItemId());
			stock.setObjectId(orderItem.getOrderItemId());
			stock.setObjectType(OrderEnum.ORDER_STOCK_OBJECT_TYPE.ORDERITEM.name());
			orderStockDao.insert(stock);
		}
		
		if(orderItem!=null && orderItem.getOrdMulPriceRateList()!=null){
			for (OrdMulPriceRate rate:orderItem.getOrdMulPriceRateList()){
				rate.setOrderItemId(orderItem.getOrderItemId());
				ordMulPriceRateService.addOrdMulPriceRate(rate);
			}
		}
	}

	@Override
	public void saveOrderItemPersonRelation(OrdOrderItem orderItem) {
		// TODO Auto-generated method stub
		
	}

}
