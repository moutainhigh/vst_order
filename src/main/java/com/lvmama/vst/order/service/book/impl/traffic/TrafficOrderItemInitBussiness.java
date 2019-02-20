/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.traffic;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdTraffic;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderInitBussiness;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;

/**
 * 交通品类商品默认初始化
 * @author lancey
 *
 */
@Component("trafficOrderItemInitBussiness")
public class TrafficOrderItemInitBussiness extends AbstractBookService implements OrderInitBussiness {

	
	
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private DistrictClientService districtClientService;

	/* (non-Javadoc)
	 * @see com.lvmama.vst.order.service.book.OrderInitBussiness#initOrderItem(com.lvmama.vst.back.order.po.OrdOrderItem, com.lvmama.vst.order.vo.OrdOrderDTO)
	 */
	@Override
	public boolean initOrderItem(OrdOrderItem orderItem, OrdOrderDTO order) {
		OrdOrderPack orderPack = orderItem.getOrderPack();
		OrdOrderItemDTO orderItemDTO = (OrdOrderItemDTO)orderItem;
		BuyInfo.Item item =orderItemDTO.getItem();
		long quantity=0;
		if(item.getAdultQuantity()>0){
			orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.adult_quantity.name(), item.getAdultQuantity());
			quantity += item.getAdultQuantity();
		}
		if(item.getChildQuantity()>0){
			quantity+=item.getChildQuantity();
			orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.child_quantity.name(), item.getChildQuantity());
		}
		orderItem.setQuantity(quantity);
		int packageCount=1;
		if(orderPack!=null){
			ProdPackageDetail packageDetail = OrderUtils.getProdPackageDetail((OrdOrderPackDTO)orderPack, item.getGoodsId());
			if(packageDetail!=null){
				if(ProdPackageDetail.OBJECT_TYPE_DESC.SUPP_GOODS.name().equalsIgnoreCase(packageDetail.getObjectType())){
					if(packageDetail.getPackageCount()!=null){
						packageCount = packageDetail.getPackageCount().intValue();
					}
				}
			}
		}
		List<BuyInfo.PriceType> priceTypeList = new ArrayList<BuyInfo.PriceType>();
		Integer adult = getPersonCount(orderItem,OrderEnum.ORDER_TICKET_TYPE.adult_quantity);//(Integer)orderPack.getContentValueByKey(OrderEnum.ORDER_TICKET_TYPE.adult_quantity.name());
		if(adult==null||adult<1){
			throwNullException("成人数量不可以为空");
		}
		BuyInfo.PriceType pt = new BuyInfo.PriceType();
		pt.setPriceKey(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name());
		pt.setQuantity(adult*packageCount);
		priceTypeList.add(pt);
		Integer child=getPersonCount(orderItem,OrderEnum.ORDER_TICKET_TYPE.child_quantity);
		if(child!=null&&child>0){
			pt = new BuyInfo.PriceType();
			pt.setPriceKey(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD.name());
			pt.setQuantity(child*packageCount);
			priceTypeList.add(pt);
		}
		item.setPriceTypeList(priceTypeList);
		if(order.isCreateFlag()){
			//大交能的是否出境计算
			ProdProductParam param = new ProdProductParam();
			param.setTraffic(true);
			ResultHandleT<ProdProduct> ppHandle = prodProductClientService.findProdProductById(orderItem.getProductId(), param);
			if(ppHandle!=null&&ppHandle.isSuccess()&&!ppHandle.hasNull()){
				ProdTraffic prodTraffic = ppHandle.getReturnContent().getProdTraffic();
				if(prodTraffic==null){
					throwIllegalException("大交通产品无交通信息");
				}
				if(prodTraffic.getStartDistrict()!=null){
					ResultHandleT<BizDistrict> handle = districtClientService.findDistrictById(prodTraffic.getStartDistrict());
					if(!handle.hasNull()){
						orderItem.putContent(OrderEnum.ORDER_ROUTE_TYPE.is_from_foreign.name(), handle.getReturnContent().getForeighFlag());
					}
				}
				if(prodTraffic.getEndDistrict()!=null){
					ResultHandleT<BizDistrict> handle = districtClientService.findDistrictById(prodTraffic.getEndDistrict());
					if(!handle.hasNull()){
						orderItem.putContent(OrderEnum.ORDER_ROUTE_TYPE.is_to_foreign.name(), handle.getReturnContent().getForeighFlag());
					}
				}
			}
		}
		return true;
	}
	
	
	private Integer getPersonCount(OrdOrderItem orderItem,OrderEnum.ORDER_TICKET_TYPE key){
		return (Integer)orderItem.getContentValueByKey(key.name());
	}

	private static final String[] branch_code_array={"traffic_class"};
}
