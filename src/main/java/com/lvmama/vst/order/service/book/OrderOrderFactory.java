/**
 * 
 */
package com.lvmama.vst.order.service.book;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;

/**
 * 
 * @author lancey
 *
 */
@Component
public class OrderOrderFactory {

	@Resource(name="orderOrderFactoryMap")
	private Map<String,Object> orderOrderFactoryMap;
	
	/**
	 * 创建初始化的品类对应的产品类
	 * @param item
	 * @return
	 */
	public OrderInitBussiness createInitProduct(final OrdOrderItem item){
		String key="init_"+item.getCategoryId();
		return (OrderInitBussiness)getAndCheck(key);
	}
	/**
	 * 创建打包初始化类
	 * @param pack
	 * @return
	 */
	public OrderPackInitBussiness createInitPackProduct(final OrdOrderPack pack){
		String key="initPack_"+pack.getCategoryId();
		String categoryCode=(String)pack.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
		if(StringUtils.equalsIgnoreCase(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.name(), categoryCode)
				||StringUtils.equalsIgnoreCase(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.name(), categoryCode)
				||StringUtils.equalsIgnoreCase(BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.name(), categoryCode)){
			if(pack.hasOwn()){
				key+="_lvmama";
			}
		}
		return (OrderPackInitBussiness)getAndCheck(key);
	}
	
	public OrderItemSaveBussiness createSaveProduct(final OrdOrderItem item){
		String key="saveItem_"+item.getCategoryId();
		return(OrderItemSaveBussiness)get(key, "saveItem_defaultOrderItemSaveBussiness");
	}
	
	public OrderTimePriceService createTimePrice(final OrdOrderItem item){
		String key="timePrice_"+item.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
		if(item.hasTicketAperiodic()){//如果是门票，key值添加notime
			key+="_notime";
		}
		return (OrderTimePriceService)getAndCheck(key);
	}
	
	public OrderPromotionBussiness createInitPromition(final String productInfo){
		String prefix=productInfo.substring(0,productInfo.indexOf("_"));
		String key="orderProm_"+prefix.toLowerCase();
		return (OrderPromotionBussiness)getAndCheck(key);
	}
	
	private Object getAndCheck(final String key){
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("KEY::::::::::"+key);
		}
		LOGGER.info("key:::::::::::::::::::::::"+key);
		if(!orderOrderFactoryMap.containsKey(key)){
			throw new RuntimeException("create object error "+key+" ");
		}
		return orderOrderFactoryMap.get(key);
	}
	
	private Object get(final String key, String defaultKey){
		if(orderOrderFactoryMap.containsKey(key)){
			return orderOrderFactoryMap.get(key);
		}else{
			if(StringUtils.isEmpty(defaultKey)){
				return null;
			}
			
			return orderOrderFactoryMap.get(defaultKey);
		}
	}
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderOrderFactory.class);
}
