/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.route;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.BranchClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsLineTimePrice;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.vo.ProdLineRouteVO;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.ItemRelation;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderInitBussiness;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;

/**
 * 针对供应商打包的线路数据的商品初始化,
 * 目前支持的供应商打包的线路商品，不包含酒店套餐
 * 自主打包不存在线路
 * @author lancey
 *
 */
@Component("routeOrderItemBussiness")
public class RouteOrderItemBussiness extends AbstractBookService implements OrderInitBussiness {

	private static final String TRAFFIC_FLAG = "traffic_flag";
	private static final Log log = LogFactory.getLog(RouteOrderItemBussiness.class);

	@Autowired
	private BranchClientService branchClientService;
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Override
	public boolean initOrderItem(OrdOrderItem orderItem, OrdOrderDTO order) {

		OrdOrderItemDTO orderItemDTO = (OrdOrderItemDTO)orderItem;

		orderItemDTO.setOrderDTO(order);
		if(orderItem.getOrderPack()==null){
			OrdAdditionStatus ordAdditionStatus = OrderUtils.makeOrdAdditionStatus(OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.name(), 
					OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.NO_UPLOAD.name());
			order.addOrdAdditionStatus(ordAdditionStatus);
		}
		Map<String,Object> propMap = prodProductClientService.findProdProductProp(orderItem.getCategoryId(), orderItem.getProductId());
		Object obj = propMap.get(TRAFFIC_FLAG);
		if(obj!=null){
			orderItem.putContent(OrderEnum.ORDER_ROUTE_TYPE.big_traffic_flag.name(), obj.toString());
		}
		//出境线路子订单添加团结算标识
		Object objFlag=propMap.get(OrderEnum.ORDER_ROUTE_TYPE.group_settle_flag.name());
		String buCode=orderItem.getBuCode();
		if(StringUtils.isEmpty(buCode)){
			buCode=order.getBuCode();
		}
		log.info("put groupSettlFlag orderId:"+orderItem.getOrderId()+"bu:"+buCode+"productId:"+orderItem.getProductId());
		if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equalsIgnoreCase(buCode) && objFlag!=null && StringUtils.isNotBlank(objFlag.toString())){
			orderItem.putContent(OrderEnum.ORDER_ROUTE_TYPE.group_settle_flag.name(), objFlag.toString());
		}
		orderItem.putContent(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name(), orderItem.getSuppGoods().getProdProduct().getProductType());
		if(orderItem.getSuppGoods().getProdProduct().getProducTourtType() != null){
			orderItem.putContent(OrderEnum.ORDER_ROUTE_TYPE.route_tour_type.name(), orderItem.getSuppGoods().getProdProduct().getProducTourtType());
		}
		//房差份数
		int fangchaQuantity = 0;
	
		if(orderItemDTO.getOrderDTO() != null){
		
			BuyInfo buyInfo = orderItemDTO.getOrderDTO().getBuyInfo();
			if(buyInfo != null){
				fangchaQuantity = buyInfo.getSpreadQuantity();
			}
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
			
			if (orderItem.getItem().getRouteRelation()!=null
					&&ItemRelation.ADDITION.name().equals(orderItem.getItem().getRouteRelation().name())) {
				orderItem.putContent(OrderEnum.ORDER_COMM_TYPE.room_price_differ.name(), orderItem.getItem().getGapQuantity());
			}else{
				orderItem.putContent(OrderEnum.ORDER_COMM_TYPE.room_price_differ.name(), fangchaQuantity);
			}
		
		initPriceType(orderItem);
		return true;
	}

	/**
	 * 初始化价格类型
	 * @param orderItem
	 */
	private void initPriceType(OrdOrderItem orderItem){
		OrdOrderPack orderPack = orderItem.getOrderPack();
		OrdOrderItemDTO orderItemDTO = (OrdOrderItemDTO)orderItem;

		Long suppGoodsId = orderItem.getSuppGoods().getSuppGoodsId();
		
		//房差份数
		int fangchaQuantity = 0;
		if(orderItemDTO.getOrderDTO() != null){
			BuyInfo buyInfo = orderItemDTO.getOrderDTO().getBuyInfo();
			if(buyInfo != null){
				if (orderItem.getItem().getRouteRelation()!=null&&ItemRelation.ADDITION.name().equals(orderItem.getItem().getRouteRelation().name())) {
					fangchaQuantity =orderItem.getItem().getGapQuantity();
					log.info("supp goods is " + suppGoodsId + " fangchaQuantity is " + fangchaQuantity + ", come from orderItem.getItem().getGapQuantity()");
				}else{
					fangchaQuantity = buyInfo.getSpreadQuantity();
					log.info("supp goods is " + suppGoodsId + " fangchaQuantity is " + fangchaQuantity + ", come from buyInfo.getSpreadQuantity()");
				}
			}
		}
		
		BuyInfo.Item item =orderItemDTO.getItem();
		long quantity=0;
		if(item.getAdultQuantity()>0){
			orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.adult_quantity.name(), item.getAdultQuantity());
			quantity += item.getAdultQuantity();
		}
		if(OrderUtils.hasRouteBranch(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchCode.name()))){
			if(item.getChildQuantity()>0){
				quantity+=item.getChildQuantity();
				orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.child_quantity.name(), item.getChildQuantity());
			}
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
		BuyInfo.PriceType pt = new BuyInfo.PriceType();
		pt.setPriceKey(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name());
		pt.setQuantity(adult*packageCount);
		priceTypeList.add(pt);
		int total=pt.getQuantity();
		Integer child=getPersonCount(orderItem,OrderEnum.ORDER_TICKET_TYPE.child_quantity);
		if(child!=null&&child>0){
			pt = new BuyInfo.PriceType();
			pt.setPriceKey(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD.name());
			pt.setQuantity(child*packageCount);
			priceTypeList.add(pt);
			total += pt.getQuantity();
		}
		total = fangchaQuantity;
		if(OrderUtils.hasRouteBranch(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchCode.name()))){
			boolean hasForeignGroup = ProdProduct.PRODUCTTYPE.FOREIGNLINE
					.name().equals(orderItem.getSuppGoods().getProdProduct()
									.getProductType())
					&& orderItem.hasCategory(BIZ_CATEGORY_TYPE.category_route_group);
//			if(total==1&&!hasForeignGroup){
			if(total > 0){
				OrderTimePriceService orderTimePriceService = orderOrderFactory.createTimePrice(orderItem);
				ResultHandleT<SuppGoodsBaseTimePrice> baseTimePrice = orderTimePriceService.getTimePrice(orderItem.getSuppGoodsId(), orderItem.getVisitTime(), true);
				if(baseTimePrice.hasNull()){
					throwNullException("时间价格表为空");
				}
				SuppGoodsLineTimePrice mulTimePrice = (SuppGoodsLineTimePrice) baseTimePrice.getReturnContent();
				if(mulTimePrice.getGapPrice()!=null){
					pt = new BuyInfo.PriceType();
					pt.setPriceKey(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.name());
					pt.setQuantity(total);
					priceTypeList.add(pt);
				}
			}
		}

		log.info("orderItem " + orderItem.getSuppGoodsId() + " price type list size is " + priceTypeList.size() + " =========begin to print");
		for (BuyInfo.PriceType priceType : priceTypeList) {
			log.info("==========" + priceType.getPriceKey() + "==========" + priceType.getQuantity());
		}
		log.info("orderItem " + orderItem.getSuppGoodsId() + " price type list size is " + priceTypeList.size() + " =========print completed");
		
		item.setPriceTypeList(priceTypeList);
	}
	
	private Integer getPersonCount(OrdOrderItem orderItem,OrderEnum.ORDER_TICKET_TYPE key){
		Integer count = (Integer)orderItem.getContentValueByKey(key.name());
//		if(count==null){
//			return (Integer)orderItem.getOrderPack().getContentValueByKey(key.name());
//		}
		return count;
	}
	
	
}
