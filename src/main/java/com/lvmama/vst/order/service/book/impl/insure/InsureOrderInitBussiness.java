/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.insure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderInitBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * 保险初始化
 * @author lancey
 *
 */
@Component("insureOrderInitBussiness")
public class InsureOrderInitBussiness extends AbstractBookService implements OrderInitBussiness{

	@Autowired
	private ProdProductClientService prodProductClientService;
	private static final String days_key="days_of_insurance";
	@Override
	public boolean initOrderItem(OrdOrderItem orderItem, OrdOrderDTO order) {
		Map<String,Object> map = prodProductClientService.findProdProductProp(orderItem.getCategoryId(), orderItem.getProductId());
		if(map.containsKey(days_key)){
			orderItem.putContent(OrderEnum.ORDER_INSURE_TYPE.days_of_insurance.name(), NumberUtils.toInt(map.get(days_key).toString()));
		}
		/*if(order.getBuyInfo().hasAdditionalTravel()){
			if(order.getTotalPerson()>orderItem.getQuantity().intValue()){
				List<BuyInfo.ItemPersonRelation> list =order.getBuyInfo().getPersonRelationMap().get("GOODS_"+orderItem.getSuppGoodsId());
				if(list==null){
					throwIllegalException("保险对应游玩人信息不全");
				}
				
				List<OrdItemPersonRelation> relationList = new ArrayList<OrdItemPersonRelation>();
				for(BuyInfo.ItemPersonRelation item:list){
					OrdItemPersonRelation relation = new OrdItemPersonRelation();
					relation.setSeq((long)item.getSeq());
					relation.setOrdPerson(order.getOrdTravellerList().get(item.getSeq()));
					relationList.add(relation);
				}
				orderItem.setOrdItemPersonRelationList(relationList);
			}
		}*/
		return true;
	}

}
