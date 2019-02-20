/**
 * 
 */
package com.lvmama.vst.order.service.book.impl;

import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.order.dao.OrdMulPriceRateDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.service.book.AbstractOrderItemSaveBussiness;
import com.lvmama.vst.order.service.book.OrderItemSaveBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * @author lancey
 *
 */
@Component("defaultOrderItemSaveBussiness")
public class DefaultOrderItemSaveBussiness extends AbstractOrderItemSaveBussiness implements OrderItemSaveBussiness{

	@Autowired
	private OrdMulPriceRateDAO ordMulPriceRateDAO;

	@Override
	public void saveAddition(OrdOrderDTO order, OrdOrderItem orderItem) {
		saveStock(orderItem);
		if(orderItem.getOrdMulPriceRateList()!=null){
			/*String isBuyout = orderItem.getIsBuyout();*/
			for(OrdMulPriceRate rate:orderItem.getOrdMulPriceRateList()){
				rate.setOrderItemId(orderItem.getOrderItemId());
				/*if("Y".equals(isBuyout) && rate.getBuyoutPrice()!=null){
					rate.setPrice( rate.getBuyoutPrice());
				}*/
				ordMulPriceRateDAO.insert(rate);
			}
		}
	}

	@Override
	public void saveOrderItemPersonRelation(OrdOrderItem orderItem) {
		// TODO Auto-generated method stub
		
	}

}
