package com.lvmama.vst.order.service.book.impl;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.book.IOrderDistinguishByBuBussiness;
/**
 * @author
 * 为国内BU单独写的服务类，现包含功能:
 * 1:获取支付等待时间默认时长.
 */
@Component("OrderDistinguishByBuBussinessImpl")
public class OrderDistinguishByBuBussinessImpl implements IOrderDistinguishByBuBussiness {
	@Autowired						
	private ProdProductClientService productClientService;
	@Override
	public String getBuType(String categoryCode,Long productId,OrdOrder ordOrder) {
		if(StringUtil.isEmptyString(categoryCode) || productId == null){
			return "";
		}
//		ResultHandleT<ProdProduct> resultProduct = productClientService.findProdProductById(productId);
		ResultHandleT<ProdProduct> resultProduct = productClientService.findProdProductByIdFromCache(productId);

		ProdProduct prodProduct = null;
		if(resultProduct.isSuccess() && resultProduct.getReturnContent()!=null){
			prodProduct = resultProduct.getReturnContent();
		}
		if(prodProduct == null){
			return "";
		}
		String routeType = prodProduct.getProductType();
		if(StringUtil.isEmptyString(routeType)){
			return "";
		}
		if(isLocalBu(categoryCode, routeType,ordOrder)){
			return CommEnumSet.BU_NAME.LOCAL_BU.getCode();
		}
		return "";
	}
	
	/**
	 * @param categoryCode
	 * @param routeType
	 * @return 判断是否是国内线路
	 */
	private boolean isLocalBu(String categoryCode,String routeType,OrdOrder ordOrder){
		//如果不是国内则返回false
		if(!ProdProduct.PRODUCTTYPE.INNERLINE.getCode().equalsIgnoreCase(routeType) && 
				!ProdProduct.PRODUCTTYPE.INNERSHORTLINE.getCode().equalsIgnoreCase(routeType) &&
				!ProdProduct.PRODUCTTYPE.INNERLONGLINE.getCode().equalsIgnoreCase(routeType)){
			return false;
		}
		//如果是酒店套餐则false
		if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.name().equalsIgnoreCase(categoryCode)){
			return false;
		}
		//自由行 如果是 只有酒店或者门票,酒店,保险，其他 则为false 
		if(BizEnum.BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(categoryCode)){
			List<OrdOrderItem> ordOrderItemList = ordOrder.getOrderItemList();
			if(CollectionUtils.isEmpty(ordOrderItemList)){
				return false;
			}
			//1.单门票+其他+保险为  延长
			boolean isSingleTicket = true; //true表示仅门票，false表示不是
			for(OrdOrderItem item : ordOrderItemList){
				String subCode = item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
				if(ProductCategoryUtil.isTicket(subCode) || 
						BizEnum.BIZ_CATEGORY_TYPE.category_other.name().equalsIgnoreCase(subCode) ||
						BizEnum.BIZ_CATEGORY_TYPE.category_insurance.name().equalsIgnoreCase(subCode)
						){
				}else{
					isSingleTicket = false;
					break;
				}
			}
			if(isSingleTicket){
				return true; //仅有门票的情况下，要走11点的规则
			}
			boolean isTickeOrHotel = true;
			for(OrdOrderItem item : ordOrderItemList){
				String subCode = item.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
				//含门票，酒店，快递, 保险 之外的为true
				if(ProductCategoryUtil.isTicket(subCode) || 
						BizEnum.BIZ_CATEGORY_TYPE.category_other.name().equalsIgnoreCase(subCode) ||
						BizEnum.BIZ_CATEGORY_TYPE.category_insurance.name().equalsIgnoreCase(subCode) ||
						BizEnum.BIZ_CATEGORY_TYPE.category_hotel.name().equalsIgnoreCase(subCode)
						){
				}else{
					isTickeOrHotel = false;
					break;
				}
			}
			if(isTickeOrHotel){
				return false;//是门+酒店的情况下，不按照
			}
		}
		return true;
	}

	@Override
	public boolean isConformBuRule(String currentBuName,String targetBuName, OrdOrder order) {
		if(StringUtil.isEmptyString(currentBuName) || StringUtil.isEmptyString(targetBuName) || order == null){
			return false;
		}
		//判断是否符合国内BU的规则
		if(CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(currentBuName)){
			if(CollectionUtils.isEmpty(order.getOrderItemList())){
				return false;
			}
			//游玩日期与下单日期是同一天.
			if(DateUtils.isSameDay(order.getCreateTime(), order.getVisitTime())){
				return false;
			}
			//在正常上班时间,仍然按照默认的计算
			Calendar startCal = Calendar.getInstance();
			int currentHour = startCal.get(Calendar.HOUR_OF_DAY);
			if(currentHour >= 9 && currentHour < 18){
				return false;
			}
			//有需要资源审核的.
			for(OrdOrderItem tempItem : order.getOrderItemList()){
				if(!OrderEnum.RESOURCE_STATUS.AMPLE.name().equalsIgnoreCase(tempItem.getResourceStatus())){
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * @param defaultWaitPaymentMinute 默认时间
	 * @param ordOrderItem 子订单信息
	 * @param buType BU类别
	 */
	@Override
	public int getOrderDefaultWaitPaymentTimeMinuteByBu(int defaultWaitPaymentMinute, String buType) {
		if(StringUtil.isEmptyString(buType)){
			return defaultWaitPaymentMinute;
		}
		//国内BU,前提 肯定是符合Bu计算规则
		if(CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(buType)){
			Calendar startCal = Calendar.getInstance();
			int currentHour = startCal.get(Calendar.HOUR_OF_DAY);
			// 1.在正常上班时间,仍然按照默认的计算 -- 如果这个符合了，就表示代码调错了，要纠正
			if(currentHour >= 9 && currentHour < 18){
				return defaultWaitPaymentMinute;
			}
			//2.不在正常的时间段内
			Calendar endCal = Calendar.getInstance();
			if(currentHour >= 18){
				endCal.add(Calendar.DATE, 1);//截止的11点为次日
			}
			endCal.set(Calendar.HOUR_OF_DAY, 11);
			endCal.set(Calendar.MINUTE, 0);
			long timDiff = endCal.getTimeInMillis() - startCal.getTimeInMillis();
			defaultWaitPaymentMinute = (int)timDiff/1000/60;
		}
		return defaultWaitPaymentMinute;
	}

	@Override
	public int getHotelDefaultWaitPaymentTimeMinuteByBu(int defaultWaitPaymentMinute, OrdOrderItem ordOrderItem) {
		if(ordOrderItem == null || ordOrderItem.getSuppGoodsTimePrice() == null){
			return defaultWaitPaymentMinute;
		}
		SuppGoodsTimePrice suppGoodsTimePrice = null;
		if(ordOrderItem.getSuppGoodsTimePrice() instanceof SuppGoodsTimePrice){
			suppGoodsTimePrice = (SuppGoodsTimePrice) ordOrderItem.getSuppGoodsTimePrice();
		}
		if(suppGoodsTimePrice == null){
			return defaultWaitPaymentMinute;
		}
		Calendar startTime = Calendar.getInstance();
		Calendar endTime = Calendar.getInstance();
		endTime.setTime(suppGoodsTimePrice.getSpecDate());//游玩日期
		if(suppGoodsTimePrice.getAheadBookTime() == null){
			suppGoodsTimePrice.setAheadBookTime(0L);
		}
		endTime.add(Calendar.MINUTE,(int)(0-120-suppGoodsTimePrice.getAheadBookTime()));//考虑提交预定时间 再提前2个小时
		long timDiff = endTime.getTimeInMillis() - startTime.getTimeInMillis();
		return (int)timDiff/1000/60;
	}
}
