package com.lvmama.vst.order.service.book.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_TICKET_TYPE;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderBuyPresentBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;
/**
 * 满赠活动处理
 * @author yanliping
 *
 */
@Component("orderBuyPresentBussiness")
public class OrderBuyPresentBussinessImpl extends AbstractBookService implements OrderBuyPresentBussiness{
	private static final Logger logger = LoggerFactory.getLogger(OrderBuyPresentBussinessImpl.class);
	
	
	@Autowired
	private CategoryClientService categoryClientService;
	
	
	
	@Override
	public void OrderBuyPresentHandle(OrdOrderDTO order){
		String categoryCode =null;
		Long categoryId = order.getCategoryId();
		BizCategory bizCategory = categoryClientService.findCategoryById(categoryId).getReturnContent();
		categoryCode = bizCategory.getCategoryCode();
		//份数
		long quantity = 0;
		//主产品ID
		Long mainProductId=null;
		//主商品ID
		Long mainSuppGoodsId = null;
		//订单金额
		Long orderAmount = order.getOughtAmount();
		
		if(isGroupProduct(categoryCode)){
			
			//跟团游、自由行、
			if("category_route_group".equalsIgnoreCase(categoryCode)||"category_route_freedom".equalsIgnoreCase(categoryCode)){
				OrdOrderPack pack = order.getOrderPackList().get(0);
				mainProductId = pack.getProductId();
				quantity = calcOrderQuantity(order);
			}
			//邮轮组合产品
			else if("category_comb_cruise".equalsIgnoreCase(categoryCode)){
				OrdOrderPack pack = order.getOrderPackList().get(0);
				mainProductId = pack.getProductId();
				//邮轮组合产品份数为房间数
				quantity = calcCombCruiseQuantity(order);
			}
			//组合套餐票
			else if("category_comb_ticket".equalsIgnoreCase(categoryCode)){
				//自主打包
				if(order.getOrderPackList()!=null&&order.getOrderPackList().size()>0){
					OrdOrderPack pack = order.getOrderPackList().get(0);
					mainProductId = pack.getProductId();
					quantity = calcOrderQuantity(order);
				}else{
					List<OrdOrderItem> OrdOrderItemList =  order.getOrderItemList();
					if(OrdOrderItemList!=null){
						//根据主商品取数量和主产品id
						for(OrdOrderItem item:OrdOrderItemList){
							if("true".equals(item.getMainItem())){
								quantity = item.getQuantity();
								mainProductId = item.getProductId();
							}
						}
				}
				}
			}
			//当地游、酒店套餐
			if("category_route_local".equalsIgnoreCase(categoryCode)||
					"category_route_hotelcomb".equalsIgnoreCase(categoryCode)){
				List<OrdOrderItem> OrdOrderItemList =  order.getOrderItemList();
				if(OrdOrderItemList!=null){
					//根据主商品取数量和主产品id
					for(OrdOrderItem item:OrdOrderItemList){
						if("true".equals(item.getMainItem())){
							quantity = item.getQuantity();
							mainProductId = item.getProductId();
						}
					}
			}
			}
		}
		//其他单卖产品
		else{
			List<OrdOrderItem> OrdOrderItemList =  order.getOrderItemList();
			if(OrdOrderItemList!=null&&!OrdOrderItemList.isEmpty()){
				OrdOrderItem item = OrdOrderItemList.get(0);
				quantity = item.getQuantity();
				mainProductId =item.getProductId();
				mainSuppGoodsId = item.getSuppGoodsId();
			}
		}
	}
	

	public void SupplierPack(){
		
	}
	
	/**
	 * 验证品类是否为线路、组合套餐票、邮轮组合产品
	 * @param categoryCode
	 * @return
	 */
	public boolean isGroupProduct(String categoryCode){
		if(StringUtils.isNotEmpty(categoryCode)){
			if("category_route_group".equalsIgnoreCase(categoryCode)||"category_route_local".equalsIgnoreCase(categoryCode)||
					"category_route_hotelcomb".equalsIgnoreCase(categoryCode)||"category_route_freedom".equalsIgnoreCase(categoryCode)
					||"category_comb_cruise".equalsIgnoreCase(categoryCode)||"category_comb_ticket".equalsIgnoreCase(categoryCode)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 计算邮轮组合产品房间份数
	 * @param order
	 * @return
	 */
	private long calcCombCruiseQuantity(OrdOrderDTO order) {
		long quantity = 0;
		OrdOrderPack pack = order.getOrderPackList().get(0);
		for(OrdOrderItem item :pack.getOrderItemList()){
			if(item.getCategoryId()==2)
				quantity+=item.getQuantity();
		}
		return quantity;
	}
	
	/**
	 * 取订单份数
	 * @param order
	 * @return
	 */
	private long calcOrderQuantity(OrdOrderDTO order){
		long quantity = 0;
		OrdOrderPack pack = order.getOrderPackList().get(0);
		Object adult = pack.getContentValueByKey(ORDER_TICKET_TYPE.adult_quantity.name());
		if(adult!=null){
			quantity+=Long.valueOf(adult.toString());
		}
		Object child = pack.getContentValueByKey(ORDER_TICKET_TYPE.child_quantity.name());
		if(child!=null){
			quantity+=Long.valueOf(child.toString());
		}
		if(quantity==0){
			Object quantityObj=pack.getContentValueByKey(ORDER_TICKET_TYPE.quantity.name());
			if(quantityObj!=null){
				quantity=Long.valueOf(quantityObj.toString());
			}
		}
		return quantity;
	}
	
	
}
