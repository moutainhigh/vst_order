/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.hotel;

import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.service.book.AbstractOrderItemSaveBussiness;
import com.lvmama.vst.order.service.book.OrderItemSaveBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * @author lancey
 *
 */
@Component("preSaleOrderItemSaveBussiness")
public class PreSaleOrderItemSaveBussiness extends AbstractOrderItemSaveBussiness implements OrderItemSaveBussiness{
	
    
	@Override
	public void saveAddition(OrdOrderDTO order, OrdOrderItem orderItem) {
	}

	@Override
	public void saveOrderItemPersonRelation(OrdOrderItem orderItem) {
	}
}
