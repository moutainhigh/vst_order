package com.lvmama.vst.order.service.book.impl.localplay;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.play.connects.po.OrderConnectsServiceProp;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.service.OrderConnectsServicePropService;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderItemSaveBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;


@Component("localPlayOrderItemSaveBussiness")
public class LocalPlayOrderItemSaveBussiness extends AbstractBookService implements OrderItemSaveBussiness{
	@Autowired
	private OrderConnectsServicePropService orderConnectsServicePropService;
	
	@Autowired
	private OrdOrderStockDao orderStockDao;
	
	@Override
	public void saveAddition(OrdOrderDTO order, OrdOrderItem orderItem) {
		if(order.getBuyInfo()!=null && CollectionUtils.isNotEmpty(order.getBuyInfo().getOrderConnectsServicePropList())){
			for(OrderConnectsServiceProp orderConnectsServiceProp :order.getBuyInfo().getOrderConnectsServicePropList()){
				if(orderConnectsServiceProp!=null && orderConnectsServiceProp.getPropId()!=null){
					orderConnectsServiceProp.setOrderId(order.getOrderId());
					orderConnectsServicePropService.addOrderConnectsServiceProp(orderConnectsServiceProp);
				}
			}
		}
		//插入订单库存
		if(CollectionUtils.isNotEmpty(orderItem.getOrderStockList())){
			for(OrdOrderStock stock:orderItem.getOrderStockList()){
				stock.setOrderItemId(orderItem.getOrderItemId());
				stock.setObjectId(orderItem.getOrderItemId());
				stock.setObjectType(OrderEnum.ORDER_STOCK_OBJECT_TYPE.ORDERITEM.name());
				orderStockDao.insert(stock);
			}
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
		
	}

}
