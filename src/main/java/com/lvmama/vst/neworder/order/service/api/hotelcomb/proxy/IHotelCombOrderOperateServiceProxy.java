package com.lvmama.vst.neworder.order.service.api.hotelcomb.proxy;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.common.Preconditions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombOrderAdditionInfoService;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombPricePlanService;
import com.lvmama.dest.api.order.vo.newHotelcomb.AdditSuppGoodsVo;
import com.lvmama.dest.api.order.vo.newHotelcomb.ProdLineRouteVo;
import com.lvmama.dest.hotel.trade.common.RequestBody;
import com.lvmama.dest.hotel.trade.common.ResponseBody;
import com.lvmama.dest.hotel.trade.common.ResponseBody.RESPONSE_CODE;
import com.lvmama.dest.hotel.trade.hotelcomb.interfaces.IHotelCombOrderOperateService;
import com.lvmama.dest.hotel.trade.hotelcomb.interfaces.IHotelCombTradeOrderService;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombAdditSuppGoodsVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombOrderItemVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombOrderVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombPersonVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombProdLineRouteVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombTradeBuyInfoVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombTradeBuyInfoVo.GoodsItem;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelOrdOrderVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.OrdAddressVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.OrderPriceListVo;
import com.lvmama.dest.hotel.trade.utils.BusinessException;
import com.lvmama.dest.hotel.trade.vo.base.Person;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.order.service.IComplexQueryService;

@Component("hotelCombOrderOperateService")
public class IHotelCombOrderOperateServiceProxy implements IHotelCombOrderOperateService {

	private static final Log logger = LogFactory.getLog(IHotelCombOrderOperateServiceProxy.class);
	
	@Autowired
	IHotelCombTradeOrderService hotelCombTradeOrderService;
	
	@Autowired
	IHotelCombPricePlanService hotelCombPricePlanService;
	
	@Autowired
	IComplexQueryService complexQueryService;
	
	@Autowired
	IHotelCombOrderAdditionInfoService hotelCombOrderAdditionInfoService;
	
	@Override
	public ResponseBody<String> checkStock(RequestBody<HotelCombTradeBuyInfoVo> request) {
		logger.info("IHotelCombOrderOperateServiceProxy checkStock start");
		ResponseBody<String> response = new ResponseBody<String>();
		try{
			Preconditions.checkNotNull(request,"request param is null");
			HotelCombTradeBuyInfoVo hotelCombTradeBuyInfo = request.getT();
			Preconditions.checkNotNull(hotelCombTradeBuyInfo,"request param is null");			
			Preconditions.checkNotNull(hotelCombTradeBuyInfo.getGoodsList(),"goodsList is null");
			
			this.fillPricePlanId(hotelCombTradeBuyInfo.getGoodsList());			
			hotelCombTradeOrderService.checkOrder(request);
		}catch(BusinessException businessException){
			logger.error(businessException);
			response.setBusinessException(businessException);
			response.setCode(RESPONSE_CODE.FAILURE.getCode());
		}
		return response;
	}

	@Override
	public ResponseBody<HotelCombOrderVo> submitOrder(RequestBody<HotelCombTradeBuyInfoVo> request) {
		logger.info("IHotelCombOrderOperateServiceProxy submitOrder start");
		ResponseBody<HotelCombOrderVo>  result = new ResponseBody<HotelCombOrderVo>();
		HotelCombTradeBuyInfoVo hotelCombTradeBuyInfo = request.getT();
		Preconditions.checkNotNull(hotelCombTradeBuyInfo,"request param is null");
		Preconditions.checkNotNull(hotelCombTradeBuyInfo.getGoodsList(),"goodsList is null");
		
		this.fillPricePlanId(hotelCombTradeBuyInfo.getGoodsList());
		ResponseBody<HotelOrdOrderVo> response = hotelCombTradeOrderService.submitOrder(request);
		Preconditions.checkNotNull(response.getT(),"response param is null");
		Preconditions.checkNotNull(response.getT().getOrderId(),"create orderId is null");
		// 订单详情信息
		OrdOrder ordOrder = complexQueryService.queryOrderByOrderId(response.getT().getOrderId());
		result.setT(this.coverOrdOrderToHotelCombOrder(ordOrder));
		result.setCode(RESPONSE_CODE.OK.getCode());		
		return result;
		
	}
	@Override
	public ResponseBody<OrderPriceListVo> calOrderPriceList(RequestBody<HotelCombTradeBuyInfoVo> request) {
		logger.info("IHotelCombOrderOperateServiceProxy calOrderPriceList start");
		Preconditions.checkNotNull(request,"request param is null");
		HotelCombTradeBuyInfoVo hotelCombTradeBuyInfo = request.getT();
		Preconditions.checkNotNull(hotelCombTradeBuyInfo,"request param is null");			
		Preconditions.checkNotNull(hotelCombTradeBuyInfo.getGoodsList(),"goodsList is null");
		
		this.fillPricePlanId(hotelCombTradeBuyInfo.getGoodsList());
		List<Person> travellers = new ArrayList<Person>();
		travellers.add(new Person());
		hotelCombTradeBuyInfo.setTravellers(travellers);
		
		ResponseBody<OrderPriceListVo> response = hotelCombTradeOrderService.calOrderPriceList(request);
		// 分销暂时不传优惠券,满赠,奖金,促销金额
		response.getT().setCouponAmounts(null);
		response.getT().setBuyPresentInfo(null);
		response.getT().setBonusAmount(null);
		response.getT().setSaleAmountList(null);
		return response;
	}
	

	@Override
	public ResponseBody<HotelCombOrderVo> getHotelCombOrderInfo(RequestBody<Long> request) {
		logger.info("IHotelCombOrderOperateServiceProxy getHotelCombOrderInfo start");
		ResponseBody<HotelCombOrderVo>  result = new ResponseBody<HotelCombOrderVo>();
		Preconditions.checkNotNull(request,"request param is null");
		Preconditions.checkNotNull(request.getT(),"request orderId is null");
		// 订单详情信息
		OrdOrder ordOrder = complexQueryService.queryOrderByOrderId(request.getT());
		result.setT(this.coverOrdOrderToHotelCombOrder(ordOrder));
		result.setCode(RESPONSE_CODE.OK.getCode());	
		return result;
	}
	
	private HotelCombOrderVo coverOrdOrderToHotelCombOrder(OrdOrder ordOrder){		
		HotelCombOrderVo hotelCombOrderVo = this.setBasicInfo(ordOrder);  		
   		// 设置子项主记录
		if (null != ordOrder.getMainOrderItem()) {
			HotelCombOrderItemVo orderItemVo = new HotelCombOrderItemVo();
			BeanUtils.copyProperties(ordOrder.getMainOrderItem(), orderItemVo);
			hotelCombOrderVo.setMainOrderItem(orderItemVo);
		}
		// 设置非主记录子项
		if (CollectionUtils.isNotEmpty(ordOrder.getOrderItemList())) {
			List<HotelCombOrderItemVo> otherOrderItems = new ArrayList<HotelCombOrderItemVo>();
			for (OrdOrderItem ordOrderItem : ordOrder.getOrderItemList()) {
				if (!ordOrderItem.getSuppGoodsId().equals(hotelCombOrderVo.getMainOrderItem().getSuppGoodsId())) {
					HotelCombOrderItemVo otherOrderItem = new HotelCombOrderItemVo();
					BeanUtils.copyProperties(ordOrderItem, otherOrderItem);
					otherOrderItems.add(otherOrderItem);
				}
			}
			hotelCombOrderVo.setOtherOrderItems(otherOrderItems);
		}
   		//设置订单寄送地址
		if (null != ordOrder.getOrdAddress()) {
			OrdAddressVo ordAddressVo = new OrdAddressVo();
			BeanUtils.copyProperties(ordOrder.getOrdAddress(), ordAddressVo);
			hotelCombOrderVo.setOrdAddressVo(ordAddressVo);
			// 设置寄送人
			if (null != ordOrder.getAddressPerson()) {
				HotelCombPersonVo addressPerson = new HotelCombPersonVo();
				BeanUtils.copyProperties(ordOrder.getAddressPerson(), addressPerson);
				hotelCombOrderVo.setAddressPerson(addressPerson);
			}
		}
		
   		//设置游玩人列表
   		if(CollectionUtils.isNotEmpty(ordOrder.getOrdPersonList())){
   	   		List<HotelCombPersonVo> ordPersonList = new ArrayList<HotelCombPersonVo>();
   	   		for (OrdPerson person : ordOrder.getOrdPersonList()) {
   	   			HotelCombPersonVo personVo = new HotelCombPersonVo();
   	   			BeanUtils.copyProperties(person, personVo);
   	   			ordPersonList.add(personVo);
   	   		}
   	   		hotelCombOrderVo.setOrdPersonList(ordPersonList);
   		}  		
   		hotelCombOrderVo.setProductId(ordOrder.getProductId());
   		hotelCombOrderVo.setProductName(ordOrder.getProductName());
   		hotelCombOrderVo.setCancelStrategy(ordOrder.getCancelStrategy());
   		hotelCombOrderVo.setHotelCombProdLineRouteVo(this.fillLineRoute(ordOrder.getProductId()));
   		hotelCombOrderVo.setHotelCombAdditSuppGoods(this.fillAdditSuppGoods(ordOrder.getProductId()));
		return hotelCombOrderVo;
	}
	/**
	 * 
	* @Title: fillPricePlanId
	* @Description: 填充价格计划
	* @param goodItems  
	* @return void    返回类型
	 */
	private void fillPricePlanId(List<GoodsItem> goodItems) {
		for(GoodsItem goodItem : goodItems){
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId() == (goodItem.getCategoryId())){
				Preconditions.checkNotNull(goodItem.getGoodsId(), "goodsId is null");
				com.lvmama.dest.api.common.RequestBody<Long> suppGoodsId = new com.lvmama.dest.api.common.RequestBody<Long>();
				suppGoodsId.setTFlowStyle(goodItem.getGoodsId(), NewOrderConstant.VST_ORDER_TOKEN);
				com.lvmama.dest.api.common.ResponseBody<Long> result = hotelCombPricePlanService
						.selectBySuppGoodsId(suppGoodsId);
				goodItem.setPricePlanId(result.getT());
				goodItem.setCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId());
				goodItem.setSubCategoryId(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId());
			}
		}
	}
	
	/**
	 * 
	* @Title: fillLineRoute
	* @Description: 获取行程信息
	* @param lineRouteId
	* @return HotelCombProdLineRouteVo    返回类型
	 */
	private HotelCombProdLineRouteVo fillLineRoute(Long productId){
		HotelCombProdLineRouteVo hotelCombProdLineRouteVo = new HotelCombProdLineRouteVo();
	   	com.lvmama.dest.api.common.RequestBody<Long> request = new com.lvmama.dest.api.common.RequestBody<Long>();
	   	request.setTFlowStyle(productId, NewOrderConstant.VST_ORDER_TOKEN);   		
	   	com.lvmama.dest.api.common.ResponseBody<ProdLineRouteVo> response = hotelCombOrderAdditionInfoService.getProdLineRoute(request);
	   	if(null != response && null != response.getT()){
	   		BeanUtils.copyProperties(response.getT(),hotelCombProdLineRouteVo);
	   	}
   		return hotelCombProdLineRouteVo;
	}
	
	/**
	 * 
	* @Title: fillAdditSuppGoods
	* @Description: 获取附加商品
	* @param productId
	* @return List<HotelCombAdditSuppGoodsVo>    返回类型
	 */
	private List<HotelCombAdditSuppGoodsVo> fillAdditSuppGoods(Long productId){
		List<HotelCombAdditSuppGoodsVo> list = new ArrayList<HotelCombAdditSuppGoodsVo>();
		com.lvmama.dest.api.common.RequestBody<Long> request = new com.lvmama.dest.api.common.RequestBody<Long>();
   		request.setTFlowStyle(productId, NewOrderConstant.VST_ORDER_TOKEN);  
   		com.lvmama.dest.api.common.ResponseBody<List<AdditSuppGoodsVo>> response = hotelCombOrderAdditionInfoService.getHotelcombAdditSuppGoodsList(request);
   		if(null != response){
   			if(CollectionUtils.isNotEmpty(response.getT())){
   				for(AdditSuppGoodsVo additSuppGoods:response.getT()){
   					HotelCombAdditSuppGoodsVo hotelCombAdditSuppGoodsVo = new HotelCombAdditSuppGoodsVo();
   					BeanUtils.copyProperties(additSuppGoods,hotelCombAdditSuppGoodsVo);
   					list.add(hotelCombAdditSuppGoodsVo);
   				}
   			}
   		}
		return list;
	}
	
	private HotelCombOrderVo setBasicInfo(OrdOrder ordOrder){
		HotelCombOrderVo hotelCombOrderVo = new HotelCombOrderVo();
		hotelCombOrderVo.setApproveTime(ordOrder.getApproveTime());
		hotelCombOrderVo.setOrderId(ordOrder.getOrderId());
		hotelCombOrderVo.setBookLimitType(ordOrder.getBookLimitType());
		hotelCombOrderVo.setCancelCertConfirmStatus(ordOrder.getCancelCertConfirmStatus());
		hotelCombOrderVo.setCancelReason(ordOrder.getReason());
		hotelCombOrderVo.setClientIpAddress(ordOrder.getClientIpAddress());
   		hotelCombOrderVo.setCreateTime(ordOrder.getCreateTime());
   		hotelCombOrderVo.setCancelTime(ordOrder.getCancelTime());
   		hotelCombOrderVo.setCurrencyCode(ordOrder.getCurrencyCode());
   		hotelCombOrderVo.setDistributionChannel(ordOrder.getDistributionChannel());
   		hotelCombOrderVo.setGuarantee(ordOrder.getGuarantee());
   		hotelCombOrderVo.setInvoiceStatus(ordOrder.getInvoiceStatus());
   		hotelCombOrderVo.setLastCancelTime(ordOrder.getLastCancelTime());
   		hotelCombOrderVo.setOughtAmount(ordOrder.getOughtAmount());
   		hotelCombOrderVo.setPaymentStatus(ordOrder.getPaymentStatus());
   		hotelCombOrderVo.setPaymentTarget(ordOrder.getPaymentTarget());
   		hotelCombOrderVo.setPaymentTime(ordOrder.getPaymentTime());
   		hotelCombOrderVo.setPaymentType(ordOrder.getPaymentType());
   		hotelCombOrderVo.setRefundedAmount(ordOrder.getRefundedAmount());
   		hotelCombOrderVo.setRemark(ordOrder.getRemark());
   		hotelCombOrderVo.setUserId(ordOrder.getUserId());
   		hotelCombOrderVo.setUserNo(ordOrder.getUserNo());
   		hotelCombOrderVo.setViewOrderStatus(ordOrder.getViewOrderStatus());
   		hotelCombOrderVo.setVisitTime(ordOrder.getVisitTime());
   		hotelCombOrderVo.setWaitPaymentTime(ordOrder.getWaitPaymentTime());
   		//是否预授权
   		hotelCombOrderVo.setNeedPayMentType(ordOrder.isPayMentType());
   		//是否资源审核
   		hotelCombOrderVo.setHasResourceAmple(ordOrder.hasResourceAmple());
   		hotelCombOrderVo.setOrderStatus(ordOrder.getOrderStatus());
   		hotelCombOrderVo.setResourceStatus(ordOrder.getResourceStatus());
   		//审核状态
   		hotelCombOrderVo.setHasInfoAndResourcePass(ordOrder.hasInfoAndResourcePass());
   		hotelCombOrderVo.setPerformStatus(ordOrder.getPerformStatus()); 
   		
   		return hotelCombOrderVo;
	}

}
