/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.cruise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.order.dao.OrdMulPriceRateDAO;
import com.lvmama.vst.order.service.book.AbstractOrderItemSaveBussiness;
import com.lvmama.vst.order.service.book.OrderItemSaveBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * @author lancey
 *
 */
@Component("cruiseOrderItemSaveBussiness")
public class CruiseOrderItemSaveBussiness extends AbstractOrderItemSaveBussiness implements OrderItemSaveBussiness{

	@Autowired
	private OrdMulPriceRateDAO ordMulPriceRateDAO;
	
	@Override
	public void saveAddition(OrdOrderDTO order, OrdOrderItem orderItem) {
		saveStock(orderItem);
		if(orderItem!=null && orderItem.getOrdMulPriceRateList()!=null){
			for (OrdMulPriceRate rate:orderItem.getOrdMulPriceRateList()){
				rate.setOrderItemId(orderItem.getOrderItemId());
				ordMulPriceRateService.addOrdMulPriceRate(rate);
			}
		}
		Long categoryId = order.getCategoryId();
		if(categoryId == null){
			OrdOrderPack orderPack = order.getOrdOrderPack();
			if(orderPack != null){
				categoryId = orderPack.getCategoryId();
			}
		}
		Long itemCategoryId = orderItem.getCategoryId();
		boolean flag = false;
		boolean isAdd = false;
		if(itemCategoryId != null && (itemCategoryId.longValue() == 9 || itemCategoryId.longValue() == 10)){
			List<OrdItemPersonRelation> list = orderItem.getOrdItemPersonRelationList();
			if(list != null && list.size() > 0){
				 OrdItemPersonRelation personRelation = list.get(0);
				 if(personRelation != null){
					 Long roomnum = personRelation.getRoomNo();
					 if(roomnum == null){
						 flag = true;
					 }
				 }
			}
		}
		Map<String, String> map = new HashMap<String, String>();
		if(flag && categoryId != null && categoryId.longValue() == 8){
			for(OrdOrderItem item : order.getOrderItemList()){
				Long ca = item.getCategoryId();
				if(ca != null && ca.longValue() == 2){
					 isAdd = true; 
					 List<OrdItemPersonRelation> itemPersonRelations = item.getOrdItemPersonRelationList();
					 if(itemPersonRelations != null){
						 for(OrdItemPersonRelation relation : itemPersonRelations){
							 OrdPerson ordPerson = relation.getOrdPerson();
							 if(ordPerson != null){
								Long personId = ordPerson.getOrdPersonId();
								Long roomNum = relation.getRoomNo();
								map.put(String.valueOf(personId), String.valueOf(roomNum));
							 }
						 }
					 }
				}
			}
		}
		if(isAdd){
			for(OrdItemPersonRelation itemPersonRelation : orderItem.getOrdItemPersonRelationList()){
				OrdPerson ordPerson = itemPersonRelation.getOrdPerson();
				if(ordPerson != null && ordPerson.getOrdPersonId() != null){
					String roomNum = map.get(String.valueOf((ordPerson.getOrdPersonId())));
					itemPersonRelation.setRoomNo(Long.valueOf(roomNum));
				}
			}
		}
		savePersonRelation(orderItem);
		
	}

	@Override
	public void saveOrderItemPersonRelation(OrdOrderItem orderItem) {
		savePersonRelation(orderItem);
	}

	
}
