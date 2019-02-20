/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.route;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.vo.ProdLineRouteVO;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.book.OrderInitBussiness;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * 酒店套餐的初始化
 * @author lancey
 *
 */
@Component("hotelcombOrderItemBussiness")
public class HotelcombOrderItemBussiness implements OrderInitBussiness{
	private static final Log log = LogFactory.getLog(HotelcombOrderItemBussiness.class);
	@Autowired
	protected ProdProductClientService prodProductClientService;
	@Override
	public boolean initOrderItem(OrdOrderItem orderItem, OrdOrderDTO order) {
		if(orderItem.getOrderPack()==null){
			OrdAdditionStatus ordAdditionStatus = OrderUtils.makeOrdAdditionStatus(OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.name(), 
					OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.NO_UPLOAD.name());
			order.addOrdAdditionStatus(ordAdditionStatus);
		}
		/***酒店套餐在orderItem表中增加几天几晚字段记录 (开始)   李志强   2015-03-20****/
		ProdProductParam param = new ProdProductParam();
		param.setLineRoute(true);
		ResultHandleT<ProdProduct> product=prodProductClientService.findLineProductByProductId(orderItem.getProductId(), param);
			if (product!=null && product.getReturnContent()!=null && CollectionUtils.isNotEmpty(product.getReturnContent().getProdLineRouteList())) {//根据Item的产品ID获取其产品信息
				ProdLineRouteVO route = product.getReturnContent().getProdLineRouteList().get(0);
				if(route!=null){//获取产品上的线路信息
					orderItem.putContent(OrderEnum.ORDER_PACK_TYPE.route_days.name(), route.getRouteNum());
					orderItem.putContent(OrderEnum.ORDER_PACK_TYPE.route_nights.name(), route.getStayNum());
				}
			}
			/***酒店套餐在orderItem表中增加几天几晚字段记录（结束）    李志强   2015-03-20****/

		
		orderItem.putContent(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name(), orderItem.getSuppGoods().getProdProduct().getProductType());

		try {
			Map<String,Object> propMap = prodProductClientService.findProdProductProp(orderItem.getCategoryId(), orderItem.getProductId());
			//出境线路(酒店套餐)子订单添加团结算标识
			Object objFlag=propMap.get(OrderEnum.ORDER_ROUTE_TYPE.group_settle_flag.name());
			String buCode=orderItem.getBuCode();
			if(StringUtils.isEmpty(buCode)){
                buCode=order.getBuCode();
            }
			log.info("put groupSettlFlag orderId:"+orderItem.getOrderId()+"bu:"+buCode+"productId:"+orderItem.getProductId());
			if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equalsIgnoreCase(buCode) && objFlag!=null && StringUtils.isNotBlank(objFlag.toString())){
                orderItem.putContent(OrderEnum.ORDER_ROUTE_TYPE.group_settle_flag.name(), objFlag.toString());
            }
		} catch (Exception e) {
			log.error(ExceptionFormatUtil.getTrace(e));
		}

		return true;
	}

}
