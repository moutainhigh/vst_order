/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.other;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.visa.api.service.VisaProductService;
import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.PropValue;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.book.OrderInitBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * 签证子项初始化,
 * 关联使用，默前不存在打包的概念
 * @author lancey
 *
 */
@Component("visaOrderItemInitBussiness")
public class VisaOrderItemInitBussiness implements OrderInitBussiness{

//	@Autowired
//	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private DistrictClientService districtClientService;
	
	@Autowired
	private VisaProductService visaProductService;
	
	@Override
	public boolean initOrderItem(OrdOrderItem orderItem, OrdOrderDTO order) {
		ProdProduct product = orderItem.getSuppGoods().getProdProduct();
		Map<String, Object> propMap= visaProductService.findProdProductProp(product.getProductId());//findProdProductProp(product.getBizCategoryId(), product.getProductId());
		if(propMap.get("visa_country")!=null){
			ResultHandleT<BizDistrict> bizDistrict = districtClientService.findDistrictById(Long.valueOf(propMap.get("visa_country").toString()));
			if(bizDistrict!=null&&!bizDistrict.hasNull()){
				orderItem.putContent(OrderEnum.ORDER_VISA_TYPE.visa_country.name(),bizDistrict.getReturnContent().getDistrictName());
			}
		}
		if(propMap.get("visa_type")!=null){
			orderItem.putContent(OrderEnum.ORDER_VISA_TYPE.visa_visaType.name(), ((List<PropValue>)propMap.get("visa_type")).get(0).getName());
		}
		if(propMap.get("visa_city")!=null){
			orderItem.putContent(OrderEnum.ORDER_VISA_TYPE.visa_visaCity.name(), ((List<PropValue>)propMap.get("visa_city")).get(0).getName());
		}
		return true;
	}

}
