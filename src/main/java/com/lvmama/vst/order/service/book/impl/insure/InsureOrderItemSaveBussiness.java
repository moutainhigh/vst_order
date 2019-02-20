/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.insure;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.service.book.AbstractOrderItemSaveBussiness;
import com.lvmama.vst.order.service.book.OrderItemSaveBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * @author lancey
 *
 */
@Component("insureOrderItemSaveBussiness")
public class InsureOrderItemSaveBussiness extends AbstractOrderItemSaveBussiness implements OrderItemSaveBussiness {

	
	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.service.book.OrderItemSaveBussiness#saveAddition(com.lvmama.vst.order.vo.OrdOrderDTO, com.lvmama.vst.back.order.po.OrdOrderItem)
	 */
	@Override
	public void saveAddition(OrdOrderDTO order, OrdOrderItem orderItem) {
		saveStock(orderItem);
		
		//如果存在绑定到指定人员
		if(CollectionUtils.isNotEmpty(orderItem.getOrdItemPersonRelationList())){
			savePersonRelation(orderItem);
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
		savePersonRelation(orderItem);
	}

}
