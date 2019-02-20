package com.lvmama.vst.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.biz.po.BizCategoryProp;
import com.lvmama.vst.back.client.biz.service.CategoryPropClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductPropClientService;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProductProp;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.IOrderPriceUnitService;
/**
 * 
 * @author sunjian
 *
 */
@Service
public class OrderPriceUnitServiceImpl implements IOrderPriceUnitService {
	
	@Autowired
	private CategoryPropClientService categoryPropClientService;
	
	@Autowired
	private ProdProductPropClientService prodProductPropClientService;

	@Override
	public String getPriceUnit(OrdOrderItem orderItem) {
		String priceUnit = null;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("categoryId", orderItem.getCategoryId());
		params.put("propCode", OrderEnum.ORDER_COMMON_TYPE.pricing_type.name());
		ResultHandleT<List<BizCategoryProp>> resultHandleT = categoryPropClientService.findCategoryPropList(params);
		if (resultHandleT.isSuccess()) {
			List<BizCategoryProp> bizCategoryPropList = resultHandleT.getReturnContent();
			if (bizCategoryPropList != null && !bizCategoryPropList.isEmpty()) {
				BizCategoryProp bizCategoryProp = bizCategoryPropList.get(0);
				
				Map<String, Object> propParams = new HashMap<String, Object>();
				propParams.put("productId", orderItem.getProductId());
				propParams.put("propId", bizCategoryProp.getPropId());
				ResultHandleT<List<ProdProductProp>> ResultHandleProdProductPropList = prodProductPropClientService.findProdProductPropList(propParams);
				if (ResultHandleProdProductPropList.isSuccess()) {
					List<ProdProductProp> prodProductPropList = ResultHandleProdProductPropList.getReturnContent();
					if (prodProductPropList != null && !prodProductPropList.isEmpty()) {
						ProdProductProp prodProductProp = prodProductPropList.get(0);
						String prodValue = prodProductProp.getPropValue();
						
						if (prodValue != null) {
							if (prodValue.trim().equals("600")) {
								priceUnit = OrderEnum.ORDER_PRICE_UNIT.UNIT_VEHICLES.name();
							} else if (prodValue.trim().equals("601")) {
								priceUnit = OrderEnum.ORDER_PRICE_UNIT.UNIT_PERSON.name();
							} else if (prodValue.trim().equals("602")) {
								priceUnit = OrderEnum.ORDER_PRICE_UNIT.UNIT_PORTION.name();
							}
						}
					}
				}
			}
		}
		
		return priceUnit;
	}

}
