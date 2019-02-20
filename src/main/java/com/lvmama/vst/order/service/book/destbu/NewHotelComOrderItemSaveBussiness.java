package com.lvmama.vst.order.service.book.destbu;

import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.service.book.AbstractOrderItemSaveBussiness;
import com.lvmama.vst.order.service.book.OrderItemSaveBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;

@Component("newOrderItemSaveBussiness")
public class NewHotelComOrderItemSaveBussiness extends AbstractOrderItemSaveBussiness implements OrderItemSaveBussiness{
	
	
	@Override
	public void saveAddition(OrdOrderDTO order, OrdOrderItem orderItem) {
		saveStock(orderItem);
     	saveOrderItemPersonRelation(orderItem);
	}

	@Override
	public void saveOrderItemPersonRelation(OrdOrderItem orderItem) {
		if(orderItem.getOrdItemPersonRelationList()!=null){//基本上不存在这个值，所以加个判断，只有自备签存在需要
			savePersonRelation(orderItem);
		}
	}

}
