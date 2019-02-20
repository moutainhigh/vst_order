package com.lvmama.vst.order.contract.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.contract.service.IOrderTravelContractDataService;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdOrderPackDao;

/**
 * 
 * @author sunjian
 *
 */
@Service
public class OrderTravelContractDataServiceFactory {
	
	@Autowired
	private OrdOrderPackDao ordOrderPackDao;
	
	@Resource(name="orderCombCuriseTravelContractDataService")
	private IOrderTravelContractDataService orderCombCuriseTravelContractDataService;
	
	@Autowired
	private CategoryClientService categoryClientService;
	
	public IOrderTravelContractDataService createTravelContractDataService(OrdOrder order) {
		IOrderTravelContractDataService travelContractDataService = null;
		if (order != null) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderId", order.getOrderId());
			List<OrdOrderPack> ordOrderPackList = ordOrderPackDao.selectByParams(params);
			if (ordOrderPackList != null && !ordOrderPackList.isEmpty() && ordOrderPackList.size() == 1) {
				order.setOrderPackList(ordOrderPackList);
				OrdOrderPack ordOrderPack = ordOrderPackList.get(0);
				String categoryCode = (String) ordOrderPack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
				if (categoryCode == null) {
					ResultHandleT<BizCategory> resultHandleBizCategory = categoryClientService.findCategoryById(ordOrderPack.getCategoryId());
					if (resultHandleBizCategory.isSuccess() && resultHandleBizCategory.getReturnContent() != null) {
						categoryCode = resultHandleBizCategory.getReturnContent().getCategoryCode();
					}
				}
				
				if (BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.name().equalsIgnoreCase(categoryCode)) {
					travelContractDataService = orderCombCuriseTravelContractDataService;
				}
			}
		}
		
		return travelContractDataService;
	}
}
