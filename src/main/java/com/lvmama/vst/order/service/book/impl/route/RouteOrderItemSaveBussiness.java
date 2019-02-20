/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.order.dao.OrdMulPriceRateDAO;
import com.lvmama.vst.order.service.book.AbstractOrderItemSaveBussiness;
import com.lvmama.vst.order.service.book.OrderItemSaveBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * 线路价格类型保存
 * @author lancey
 *
 */
@Component("routeOrderItemSaveBussiness")
public class RouteOrderItemSaveBussiness extends AbstractOrderItemSaveBussiness implements OrderItemSaveBussiness{

	@Autowired
	private OrdMulPriceRateDAO ordMulPriceRateDAO;
	
	@Override
	public void saveAddition(OrdOrderDTO order, OrdOrderItem orderItem) {
		saveStock(orderItem);
		if(orderItem.getOrdMulPriceRateList()!=null){
			for(OrdMulPriceRate rate:orderItem.getOrdMulPriceRateList()){
				rate.setOrderItemId(orderItem.getOrderItemId());
				ordMulPriceRateDAO.insert(rate);
			}
		}
		saveOrderItemPersonRelation(orderItem);
	}

	@Override
	public void saveOrderItemPersonRelation(OrdOrderItem orderItem) {
		if(orderItem.getOrdItemPersonRelationList()!=null){//基本上不存在这个值，所以加个判断，只有自备签存在需要
			savePersonRelation(orderItem);
		}
	}

	
}
