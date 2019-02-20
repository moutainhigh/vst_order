package com.lvmama.vst.order.utils;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PERSON_ID_TYPE;
import com.lvmama.vst.back.prod.po.ProdEcontract;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageDetailAddPrice;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.TimePriceUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.*;

public class OrderUtils {
	 private  static final Logger LOGGER = LoggerFactory.getLogger(OrderUtils.class);

	 // RSA私钥证书地址
	 public static final String RSA_PRIVATE_PATH = "/var/www/webapps/vst_order/WEB-INF/classes/HeartyPri.key";


	public static ProdPackageDetail getProdPackageDetail(final OrdOrderPackDTO pack,final Long suppGoodsId){
		List<ProdPackageDetail> list = pack.getPackageDetailList();
		for(ProdPackageDetail detail:list){
			if(detail.getObjectId().equals(suppGoodsId)){
				return detail;
			}
		}
		return null;
	}
	
	/**
	 * 分段加价
	 * @param pack
	 * @param detailId
	 * @param date 
	 * @return
	 */
	public static ProdPackageDetailAddPrice getProdPackageDetailAddPriceByDetailId(final OrdOrderPackDTO pack, final Long detailId, Date date){
		List<ProdPackageDetailAddPrice> list = pack.getPackageDetailAddPriceList();
		if(CollectionUtils.isNotEmpty(list)) {
			for(ProdPackageDetailAddPrice detail : list){
				if("PROD_BRANCH".equals(detail.getObjectType())){
					if(date != null) {
						if(detail.getDetailId().equals(detailId) && DateUtils.isSameDay(detail.getSpecDate(), date)){
							return detail;
						}
					} else {
						if(detail.getDetailId().equals(detailId)){
							return detail;
						}
					}
				}
				
			}
		}
		return null;
	}
	
	
	/**
	 * 特殊加价规则，特殊加价到商品上
	 * @param pack
	 * @param detailId
	 * @param date 
	 * @return
	 */
	public static ProdPackageDetailAddPrice getProdPackageDetailAddPriceByDetailIdSuppGoodsId(final OrdOrderPackDTO pack, final Long detailId, Date date,Long suppGoodsId){
		List<ProdPackageDetailAddPrice> list = pack.getPackageDetailAddPriceList();
		if(CollectionUtils.isNotEmpty(list)) {
			for(ProdPackageDetailAddPrice detail : list){
				if(date != null) {
					if(detail.getDetailId().equals(detailId) && DateUtils.isSameDay(detail.getSpecDate(), date)&&detail.getObjectId().equals(suppGoodsId)){
						return detail;
					}
				}
			}
		}
		return null;
	}
	
	public static ProdPackageDetail getProdPackageDetailByDetailId(final OrdOrderPackDTO pack,final Long detailId){
		List<ProdPackageDetail> list = pack.getPackageDetailList();
		for(ProdPackageDetail detail:list){
			if(detail.getDetailId().equals(detailId)){
				return detail;
			}
		}
		return null;
	}
	
	/**
	 * 使用时间价格表数据填充订单
	 * 
	 * @param timePrice
	 * @param orderItem
	 * @param ordOrderDTO
	 */
	public static void fillOrderWithTimePrice(SuppGoodsBaseTimePrice timePrice, OrdOrderDTO ordOrderDTO) {
		if ((timePrice != null) && (ordOrderDTO != null)) {
			// 最晚取消时间
			setOrderLastCancelTime(timePrice.getSpecDate(), timePrice.getLatestCancelTime(), ordOrderDTO);
//			// 支付等待时间（默认2小时）
//			Calendar calendar = Calendar.getInstance();
//			calendar.set(0, 0, 0, 2, 0, 0);
//			setOrderWaitPaymentTime(2*60, ordOrderDTO);
		}
	}
	
	/**
	 * 设置订单最晚取消时间为订单子项最早的取消时间
	 * 
	 * @param date
	 *            新取消时间
	 * @param ordOrderDTO
	 *            订单
	 */
	public static void setOrderLastCancelTime(Date visitTime,Long longDate, OrdOrderDTO ordOrderDTO) {
		if (longDate != null) {
			Date date = DateUtils.addMinutes(visitTime, (int)-longDate);
			if (ordOrderDTO.getLastCancelTime() == null) {
				ordOrderDTO.setLastCancelTime(date);
			} else {
				if (ordOrderDTO.getLastCancelTime().after(date)) {
					ordOrderDTO.setLastCancelTime(date);
				}
			}
		}
	}
	
	/**
	 * 设置订单支付等待为订单子项最早的支付等待时间
	 * 
	 * @param date
	 *            新取消时间
	 * @param ordOrderDTO
	 *            订单
	 */
	public static void setOrderWaitPaymentTime(int  minute, OrdOrderDTO ordOrderDTO) {
		if(minute>0){
			if (ordOrderDTO.getWaitPaymentTimeSec() == 0) {
				ordOrderDTO.setWaitPaymentTimeSec(minute);
			} else {
				ordOrderDTO.setWaitPaymentTimeSec(Math.min(minute,
						ordOrderDTO.getWaitPaymentTimeSec()));
			}
		}
	}
	
	/**
	 * 根据OrdOrderStock列表中的各个OrdOrderStock状态，设置订单子项资源状态
	 * 
	 * @param orderItem
	 * @param orderStockList
	 */
	public static void setOrderItemResourceStatusByOrderStockList(OrdOrderItem orderItem, List<OrdOrderStock> orderStockList) {
		if (orderItem != null && orderStockList != null) {
			for (OrdOrderStock orderStock : orderStockList) {
				//设置订单子项是否需要资源确认逻辑
				setOrderItemsNeedResourceConfirm(orderStock.getNeedResourceConfirm(), orderItem);
				// 设置订单子项资源状态逻辑
				setOrderItemResourceStatus(orderStock.getResourceStatus(), orderItem);
			}
		}
	}
	
	/**
	 * 设置订单那子项是否需要资源确认
	 * 
	 * @param needResourceConfirm
	 * @param orderItem
	 */
	public static void setOrderItemsNeedResourceConfirm(String needResourceConfirm, OrdOrderItem orderItem) {
		if (orderItem != null) {
			if (orderItem.getNeedResourceConfirm() == null) {
				orderItem.setNeedResourceConfirm(needResourceConfirm);
			} else if (!"true".equals(orderItem.getNeedResourceConfirm())) {
				orderItem.setNeedResourceConfirm(needResourceConfirm);
			}
		}
	}
	
	/**
	 * 设置订单子项资源状态
	 * 
	 * @param resourceStatus
	 * @param orderItem
	 */
	public static void setOrderItemResourceStatus(String resourceStatus, OrdOrderItem orderItem) {
		if (orderItem != null) {
			//如果从未设置过，则设置后退出方法
			if (orderItem.getResourceStatus() == null) {
				orderItem.setResourceStatus(resourceStatus);
			} else {
				OrderEnum.RESOURCE_STATUS statusEnum = OrderEnum.RESOURCE_STATUS.valueOf(resourceStatus);
				OrderEnum.RESOURCE_STATUS orderResourceStatus = null;

				/**
				 * LOCK级别最高，然后是UNVERIFIED RESOURCEPASS AMPLE。
				 */
				switch (statusEnum) {
				case LOCK:
					orderItem.setResourceStatus(resourceStatus);
					break;

				case UNVERIFIED:
					orderResourceStatus = OrderEnum.RESOURCE_STATUS.valueOf(orderItem.getResourceStatus());

					if (orderResourceStatus != OrderEnum.RESOURCE_STATUS.LOCK) {
						orderItem.setResourceStatus(resourceStatus);
					}
					break;

//				case RESOURCEPASS:
//					orderResourceStatus = OrderEnum.RESOURCE_STATUS.valueOf(orderItem.getResourceStatus());
//
//					if ((orderResourceStatus != OrderEnum.RESOURCE_STATUS.LOCK) && (orderResourceStatus != OrderEnum.RESOURCE_STATUS.UNVERIFIED)) {
//						orderItem.setResourceStatus(resourceStatus);
//					}
//					break;

				case AMPLE:
					orderResourceStatus = OrderEnum.RESOURCE_STATUS.valueOf(orderItem.getResourceStatus());

					if ((orderResourceStatus != OrderEnum.RESOURCE_STATUS.LOCK) && (orderResourceStatus != OrderEnum.RESOURCE_STATUS.UNVERIFIED)
							&& (orderResourceStatus != OrderEnum.RESOURCE_STATUS.AMPLE)) {
						orderItem.setResourceStatus(resourceStatus);
					}
					break;
				}
			}
		}
	}
	
	/**
	 * 构造一个OrdOrderHotelTimeRate实例
	 * 
	 * @param visitTime
	 * @param quantity
	 * @param price
	 * @param settlementPrice
	 * @param marketPrice
	 * @param breakfastTicket
	 * @return
	 */
	public static OrdOrderHotelTimeRate makeOrdOrderHotelTimeRateRecord(Date visitTime, Long quantity, Long price, Long settlementPrice, Long marketPrice, Long breakfastTicket) {
		OrdOrderHotelTimeRate ordOrderHotelTimeRate = new OrdOrderHotelTimeRate();
		ordOrderHotelTimeRate.setVisitTime(visitTime);
		ordOrderHotelTimeRate.setQuantity(quantity);
		ordOrderHotelTimeRate.setPrice(price);
		ordOrderHotelTimeRate.setSettlementPrice(settlementPrice);
		ordOrderHotelTimeRate.setMarketPrice(marketPrice);
		ordOrderHotelTimeRate.setBreakfastTicket(breakfastTicket);

		return ordOrderHotelTimeRate;
	}
	
	
	
	/**
	 * 构造一个OrdOrderWifiTimeRate实例
	 * 
	 * @param visitTime
	 * @param quantity
	 * @param price
	 * @param settlementPrice
	 * @param marketPrice
	 * @return
	 */
	public static OrdOrderWifiTimeRate makeOrdOrderWifiTimeRateRecord(Date visitTime, Long quantity, Long price, Long settlementPrice, Long marketPrice, Long breakfastTicket) {
		OrdOrderWifiTimeRate ordOrderWifiTimeRate = new OrdOrderWifiTimeRate();
	    ordOrderWifiTimeRate.setVisitTime(visitTime);
		ordOrderWifiTimeRate.setQuantity(quantity);
		ordOrderWifiTimeRate.setPrice(price);
		ordOrderWifiTimeRate.setSettlementPrice(settlementPrice);
		ordOrderWifiTimeRate.setMarketPrice(marketPrice);
		return ordOrderWifiTimeRate;
	}
	
	
	/**
	 * 构造一个OrdOrderStock实例
	 * 
	 * @param objectType
	 * @param visitTime
	 * @param quantity
	 * @param inventory
	 * @param resourceConfirm
	 * @param resourceStatus
	 * @return
	 */
	public static OrdOrderStock makeOrdOrderStockRecord(String objectType, Date visitTime, Long quantity, String inventory, String resourceConfirm, String resourceStatus) {
		OrdOrderStock ordOrderStock = new OrdOrderStock();
		ordOrderStock.setObjectType(objectType);
		ordOrderStock.setVisitTime(visitTime);
		ordOrderStock.setQuantity(quantity);
		ordOrderStock.setInventory(inventory);
		ordOrderStock.setNeedResourceConfirm(resourceConfirm);
		ordOrderStock.setResourceStatus(resourceStatus);

		return ordOrderStock;
	}
	
	/**
	 * 
	 * @param optionContent
	 * @return
	 */
	public static OrdItemPersonRelation makeItemOrdPersonRelationRecord(Long roomNo, String optionContent, Long seq, OrdPerson ordPerson) {
		OrdItemPersonRelation ordItemPersonRelation = new OrdItemPersonRelation();
		ordItemPersonRelation.setOptionContent(optionContent);
		ordItemPersonRelation.setSeq(seq);
		ordItemPersonRelation.setOrdPerson(ordPerson);
		ordItemPersonRelation.setRoomNo(roomNo);
		return ordItemPersonRelation;
	}
	
	/**
	 * 
	 * @param price
	 * @param quantity
	 * @param priceType
	 * @return
	 */
	public static OrdMulPriceRate makeOrdMulPriceRateRecord(Long price, Long quantity, String priceType) {
		OrdMulPriceRate ordMulPriceRate = new OrdMulPriceRate();
		ordMulPriceRate.setPrice(price);
		ordMulPriceRate.setQuantity(quantity);
		ordMulPriceRate.setPriceType(priceType);
		
		return ordMulPriceRate;
	}
	
	public static void accumulateOrderItemPrice(OrdOrderItem orderItem, Long price, Long settlementPrice, Long actualSettlementPrice, Long marketPrice) {
		if (orderItem != null) {
			if (price != null) {
				// 单价
				if (orderItem.getPrice() == null) {
					orderItem.setPrice(price);
				} else {
					orderItem.setPrice(orderItem.getPrice() + price);
				}
			}
			
			if (settlementPrice != null) {
				// 结算单价
				if (orderItem.getSettlementPrice() == null) {
					orderItem.setSettlementPrice(settlementPrice);
				} else {
					orderItem.setSettlementPrice(orderItem.getSettlementPrice() + settlementPrice);
				}
			}
			
			if (actualSettlementPrice != null) {
				// 实际结算单价
				if (orderItem.getActualSettlementPrice() == null) {
					orderItem.setActualSettlementPrice(actualSettlementPrice);
				} else {
					orderItem.setActualSettlementPrice(orderItem.getActualSettlementPrice() + actualSettlementPrice);
				}
			}
			
			if (marketPrice != null) {
				// 市场单价
				if (orderItem.getMarketPrice() == null) {
					orderItem.setMarketPrice(marketPrice);
				} else {
					orderItem.setMarketPrice(orderItem.getMarketPrice() + marketPrice);
				}
			}
		}
	}
	
	public static OrdAdditionStatus makeOrdAdditionStatus(String statusType, String status) {
		OrdAdditionStatus ordAdditionStatus = new OrdAdditionStatus();
		ordAdditionStatus.setStatus(status);
		ordAdditionStatus.setStatusType(statusType);
		
		return ordAdditionStatus;
	}
	
	public static OrdTravelContract makeOrdTravelContract(ProdEcontract prodEcontract) {
		OrdTravelContract ordTravelContract = null;
		if (prodEcontract != null) {
			ordTravelContract = new OrdTravelContract();
			ordTravelContract.setContractTemplate(prodEcontract.getEcontractTemplate());
			ordTravelContract.setSigningType(OrderEnum.ORDER_CONTRACT_SIGNING_TYPE.ONLINE.name());
			ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.UNSIGNED.name());
		}
		
		return ordTravelContract;
	}
	
	public static OrdTravelContract makeOrdTravelContract(ProdEcontract prodEcontract,Long distributorId) {
		OrdTravelContract ordTravelContract = null;
		if (prodEcontract != null) {
			ordTravelContract = new OrdTravelContract();
			ordTravelContract.setContractTemplate(prodEcontract.getEcontractTemplate());
			if (distributorId!=null && (distributorId.longValue()==com.lvmama.vst.comm.vo.Constant.DIST_O2O_SELL || distributorId.longValue()==com.lvmama.vst.comm.vo.Constant.DIST_O2O_APP_SELL)) {
				ordTravelContract.setSigningType(OrderEnum.ORDER_CONTRACT_SIGNING_TYPE.BRANCHES.name());
			}else {
				ordTravelContract.setSigningType(OrderEnum.ORDER_CONTRACT_SIGNING_TYPE.ONLINE.name());

			}
			ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.UNSIGNED.name());
		}
		
		return ordTravelContract;
	}
	/**
	 * 不需要强制预授权 信息资源都审核通过  原有waitPaymentTime后10分钟  该时间如果没有超过最晚取消时间  还是原有waitPaymentTime，否则新的waitPaymentTime为最晚取消时间前10分钟
	 * @param waitPaymentTime
	 * @param order
	 */
	public static void setOrderBusinessWaitPaymentTime(Date waitPaymentTime, OrdOrder order) {
		if (!TimePriceUtils.hasPreauthBook(order.getCancelTime(), order.getCreateTime())) {
			if (order.hasResourceAmple()) {
//				Date lastCancelTime = order.getLastCancelTime();
//				if (lastCancelTime != null) {
//					Date newWaitPaymentTime = DateUtil.DsDay_Minute(waitPaymentTime, 10);
//					if (newWaitPaymentTime.before(lastCancelTime)) {
//						order.setWaitPaymentTime(waitPaymentTime);
//					} else {
//						order.setWaitPaymentTime(DateUtil.DsDay_Minute(lastCancelTime, -10));
//					}
//				} else {
					order.setWaitPaymentTime(waitPaymentTime);
//				}
			}
		}
	}
	
	public static void fillNopackOrderItem(OrdOrder order){
		List<OrdOrderItem> list = new ArrayList<OrdOrderItem>();
		if(CollectionUtils.isNotEmpty(order.getOrderPackList())){
			for(OrdOrderItem orderItem:order.getOrderItemList()){
				if(orderItem.getOrderPackId()==null){
					list.add(orderItem);
				}
			}
		}else{
			list = order.getOrderItemList();
		}
		order.setNopackOrderItemList(list);
	}
	

	/**
	 * 判断一个订单子项是不是当地游或酒店套餐
	 * @param orderItem
	 * @return
	 */
	public static boolean  hasRouteSingleSuppGoods(OrdOrderItem orderItem){
		return BIZ_CATEGORY_TYPE.category_route_hotelcomb.name().equals(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
				|| BIZ_CATEGORY_TYPE.category_route_local.name().equals(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()));
	}
	/**
	 * 判断一个订单子项是不是交通产品
	 * @param orderItem
	 * @return
	 */
	public static boolean  hasRouteTrafficSuppGoods(OrdOrderItem orderItem){
		return BIZ_CATEGORY_TYPE.category_traffic_aero_other.name().equals(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
				|| BIZ_CATEGORY_TYPE.category_traffic_bus.name().equals(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
				|| BIZ_CATEGORY_TYPE.category_traffic_bus_other.name().equals(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
				|| BIZ_CATEGORY_TYPE.category_traffic_ship.name().equals(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
				|| BIZ_CATEGORY_TYPE.category_traffic_ship_other.name().equals(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
				|| BIZ_CATEGORY_TYPE.category_traffic_train.name().equals(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))
				|| BIZ_CATEGORY_TYPE.category_traffic_train_other.name().equals(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()));
	}
	
	
	/**
	 * 判断一个打包是线路打包
	 * @param categoryCode
	 * @return
	 */
	public static boolean hasRoutePackCategory(String categoryCode){
		return BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(categoryCode) ||
				BIZ_CATEGORY_TYPE.category_route_group.name().equals(categoryCode);
	}
	
	/**
	 * 判断一个打包是邮轮打包
	 * @param categoryCode
	 * @return
	 */
	public static boolean hasLinePackCategory(String categoryCode){
		return BIZ_CATEGORY_TYPE.category_comb_cruise.name().equals(categoryCode);
	}
	
	/**
	 * 判断一个组合套餐票
	 * @param categoryCode
	 * @return
	 */
	public static boolean hasTicketPackCategory(String categoryCode){
		return BIZ_CATEGORY_TYPE.category_comb_ticket.name().equals(categoryCode);
	}
	
	/**
	 * 判断一个酒店套餐票
	 * @param categoryCode
	 * @return
	 */
	public static boolean hasCategoryRouteHotelcomb(String categoryCode){
		return BIZ_CATEGORY_TYPE.category_route_hotelcomb.name().equals(categoryCode);
	}
	/**
	 * 判断一个酒套餐票
	 * @param categoryCode
	 * @return
	 */
	public static boolean hasCateoryNewHotelComb(String categoryCode){
		
		return BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.name().equals(categoryCode);
	}
	
	/**
	 * 判断门票商品
	 * @param categoryCode
	 * @return
	 */
	public static boolean hasTicketItemCategory(String categoryCode){
		return BIZ_CATEGORY_TYPE.category_comb_ticket.name().equals(categoryCode)
				|| BIZ_CATEGORY_TYPE.category_single_ticket.name().equals(categoryCode)
				|| BIZ_CATEGORY_TYPE.category_other_ticket.name().equals(categoryCode);
	}
	
	public static boolean hasRouteItemCategory(String categoryCode){
		return BIZ_CATEGORY_TYPE.category_route_hotelcomb.name().equals(categoryCode)
				|| BIZ_CATEGORY_TYPE.category_route_local.name().equals(categoryCode)
				|| BIZ_CATEGORY_TYPE.category_route_group.name().equals(categoryCode)
				|| BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(categoryCode);
	}
	
	/**
	 * 自主打包计算真实价格
	 * @param settlementPrice
	 * @param Saleprice
	 * @param packageDetailPrice
	 * @param packageDetailType
	 * @return
	 */
	public static Long fillPackageOrderItemPrice(Long settlementPrice,Long Saleprice,Long packageDetailPrice,String packageDetailType){
		if(packageDetailPrice==null||StringUtils.isEmpty(packageDetailType)){
			return Saleprice;
		}
		if(settlementPrice==null||Saleprice==null){
			return 0L;
		}		
		if("FIXED_PRICE".equalsIgnoreCase(packageDetailType)){
			return settlementPrice+packageDetailPrice;
		}else if("MAKEUP_PRICE".equalsIgnoreCase(packageDetailType)){
			return settlementPrice+(Saleprice-settlementPrice)*packageDetailPrice/10000;
		}
		return 0L;
	}
	
	public static boolean hasRouteBranch(String branchCode){
		return ArrayUtils.contains(branch_code_array, branchCode);
	}
	
	public static void resetPersonInfo(OrdPerson oldPerson) {
		if(StringUtils.isEmpty(oldPerson.getIdType())
				||ORDER_PERSON_ID_TYPE.ID_CARD.name().equalsIgnoreCase(oldPerson.getIdType())
				||ORDER_PERSON_ID_TYPE.CUSTOMER_SERVICE_ADVICE.name().equals(oldPerson.getIdType())){
			oldPerson.setBirthday(null);
			oldPerson.setGender(null);
		}
	}

	private static final String[] branch_code_array={"adult_child_diff","upgrad","changed_hotel","traffic_class","advanced_seat","ordinary_seat"};
	
	/**
	 * 计算门票是否已经使用
	 * @param resultList
	 * @param order
	 * @param quantity
	 * @return
	 */
	public static String calPerformStatus(List<OrdTicketPerform> resultList, OrdOrder order,OrdOrderItem ordOrderItem){
		if(ordOrderItem == null && order != null){
			ordOrderItem =order.getMainOrderItem();
		}
		
		return calPerformStatus(resultList, ordOrderItem);
	}

	/**
	 * 计算门票履行状态，提高结算查询性能
	 * @param resultList
	 * @param item
	 * @return
	 */
	public static String calulatePerformStatus(List<OrdTicketPerform> resultList,OrdOrderItem item){
		String performStatus ="";
		List<Boolean> performFlgList = new ArrayList<Boolean>();
		List<Boolean> partPerformFlgList = new ArrayList<Boolean>();
		//取不到值
		if(resultList==null||resultList.size()==0){
			//未过指定游玩日
			if(DateUtils.addMinutes(item.getVisitTime(), 1439).after(new Date())){
				performStatus ="UNPERFORM";
			//已过指定游玩日
			}else{
				performStatus ="NEED_CONFIRM";
			}
		//取到数据
		}else{
			for(int i=0;i<resultList.size();i++){
				if(resultList.get(i).getPerformTime()==null){
					//未使用
					performFlgList.add(true);
				}else{
					//已使用
					performFlgList.add(false);
					if (((resultList.get(i).getActualAdult() == null ? 0
							: resultList.get(i).getActualAdult()) + (resultList
							.get(i).getActualChild() == null ? 0 : resultList
							.get(i).getActualChild())) != ((resultList.get(i)
							.getAdultQuantity() == null ? 0 : resultList.get(i)
							.getAdultQuantity()) + (resultList.get(i)
							.getChildQuantity() == null ? 0 : resultList.get(i)
							.getChildQuantity()))
							* (item.getQuantity() == null ? 0
									: item.getQuantity())) {
						// 有使用时间但是总分数和实际使用的份数不一致
						partPerformFlgList.add(true);
					}
				}
			}
			if(!performFlgList.contains(false)){
				performStatus ="UNPERFORM";
			}else if(!performFlgList.contains(true)){
				performStatus ="PERFORM";
				//部分使用时
				if(partPerformFlgList.contains(true)){
					performStatus ="PART_PERFORM";
				}
			}else{
				performStatus ="PART_PERFORM";
			}
		}
		
		return performStatus;
	}
	
	
	/**
	 * 计算门票是否已经使用
	 * @param resultList
	 * @param order
	 * @param quantity
	 * @return
	 */
	public static String calPerformStatus(List<OrdTicketPerform> resultList, OrdOrderItem ordOrderItem){
		String performStatus ="";
		List<Boolean> performFlgList = new ArrayList<Boolean>();
		List<Boolean> partPerformFlgList = new ArrayList<Boolean>();
		//取不到值
		if(resultList==null||resultList.size()==0){
			//未过指定游玩日
			if(DateUtils.addMinutes(ordOrderItem.getVisitTime(), 1439).after(new Date())){
				performStatus ="UNPERFORM";
			//已过指定游玩日
			}else{
				performStatus ="NEED_CONFIRM";
			}
		//取到数据
		}else{
			for(int i=0;i<resultList.size();i++){
				if(resultList.get(i).getPerformTime()==null){
					//未使用
					performFlgList.add(true);
				}else{
					//已使用
					performFlgList.add(false);
					if (((resultList.get(i).getActualAdult() == null ? 0
							: resultList.get(i).getActualAdult()) + (resultList
							.get(i).getActualChild() == null ? 0 : resultList
							.get(i).getActualChild())) != ((ordOrderItem
							.getAdultQuantity() + ordOrderItem
							.getChildQuantity()) * (ordOrderItem.getQuantity() == null ? 0
							: ordOrderItem.getQuantity()))) {
						// 有使用时间但是总分数和实际使用的份数不一致
						partPerformFlgList.add(true);
					}
				}
			}
			if(!performFlgList.contains(false)){
				performStatus ="UNPERFORM";
			}else if(!performFlgList.contains(true)){
				performStatus ="PERFORM";
				//部分使用时
				if(partPerformFlgList.contains(true)){
					performStatus ="PART_PERFORM";
				}
			}else{
				performStatus ="PART_PERFORM";
			}
		}
		
		return performStatus;
	}
	
	/**
	 * 
	 * @param perFormStatusList
	 * @return
	 */
	public static  String getMainOrderPerformStatus(List<String> perFormStatusList){
		if(perFormStatusList.contains("待人工确认")){
			return "待人工确认";
		}
		if(perFormStatusList.contains("部分使用")){
			return "部分使用";
		}
		if(perFormStatusList.contains("已使用")&&perFormStatusList.contains("未使用")){
			return "部分使用";
		}
		if(!perFormStatusList.contains("已使用")){
			return "未使用";
		}
		if(!perFormStatusList.contains("未使用")){
			return "已使用";
		}
		return "";
	}
	
	
	/**
	 * 
	 * @param perFormStatusList
	 * @return
	 * UNPERFORM   ("未使用"),
		PART_PERFORM   ("部分使用"),
		PERFORM ("已使用"),
		NEED_CONFIRM ("待人工确认");
	 */
	public static  String getMainOrderPerformStatusCode(List<String> perFormStatusList){
		if(perFormStatusList.contains("NEED_CONFIRM")){
			return "NEED_CONFIRM";
		}
		if(perFormStatusList.contains("PART_PERFORM")){
			return "PART_PERFORM";
		}
		if(perFormStatusList.contains("PERFORM")&&perFormStatusList.contains("UNPERFORM")){
			return "PART_PERFORM";
		}
		if(!perFormStatusList.contains("PERFORM")){
			return "UNPERFORM";
		}
		if(!perFormStatusList.contains("UNPERFORM")){
			return "PERFORM";
		}
		return "";
	}
	
	public static boolean isTicketByCategoryId(Long categoryId){
		if(BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(categoryId)||
				BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(categoryId)||
				BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(categoryId)||
				BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().equals(categoryId)){
			return true;
		}
		return false;
		
	}

	/**
	 * 判断是否是分销门票和线路品类
	 * @param categoryId
	 * @return
     */
	public static boolean isTicketAndRouteByCategoryId(Long categoryId){
		if(BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(categoryId)||
				BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(categoryId)||
				BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(categoryId)||
				BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().equals(categoryId)||
				BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(categoryId)||
				BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(categoryId)||
				BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(categoryId)||
				BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(categoryId)){
			return true;
		}
		return false;
	}

	
	public static boolean isTicketByCategoryCode(String categoryCode){
		if(BIZ_CATEGORY_TYPE.category_single_ticket.getCode().equals(categoryCode)||
				BIZ_CATEGORY_TYPE.category_other_ticket.getCode().equals(categoryCode)||
				BIZ_CATEGORY_TYPE.category_comb_ticket.getCode().equals(categoryCode)||
				BIZ_CATEGORY_TYPE.category_show_ticket.getCode().equals(categoryCode)){
			return true;
		}
		return false;
		
	}
	
	
	public static boolean isHotelByCategoryId(Long categoryId){
		if(BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(categoryId)){
			return true;
		}
		return false;
		
	}
	
	/**
	 * 查看订单下是否包含机票子订单且不是“交通+X”
	 * @param order
	 * @return
	 */
	public static boolean isIncludeFlightExcludeXOrderItem(OrdOrder order){
		boolean flag = false;
		if(order != null && BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().equals(order.getCategoryId())){
			return flag;
		}
		List<OrdOrderItem> ordOrderItemList = order.getOrderItemList();
		if(ordOrderItemList != null && ordOrderItemList.size() >0){
			for(OrdOrderItem ordOrderItem : ordOrderItemList){
				if(ordOrderItem != null 
						&& (BIZ_CATEGORY_TYPE.category_traffic_aeroplane.getCategoryId().equals(ordOrderItem.getCategoryId())
								|| BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().equals(ordOrderItem.getCategoryId()))){
					flag = true;
					break;
				}
			}
		}
		LOGGER.info("isIncludeFlightExcludeXOrderItem flag="+flag);
		return flag;
	}


	/**
	 * 提供 rsa加密入口
	 * @param dataMap
	 * @return
	 */
	public static String signature(Map<String, String> dataMap) {
		try {
			LOGGER.info("RSA加签源数据：" + dataMap.toString());
			String context = createLinkString(dataMap, true);
			LOGGER.info("RSA加签字符串：" + context);
			String sign = sign(context, RSA_PRIVATE_PATH, "utf-8");
			LOGGER.info("RSA加签值：" + sign);
			return sign;
		} catch (Exception e) {
			LOGGER.error("payfront orderView RSA 加签出现异常" + e);
		}
		return null;
	}

	private static   String sign(String text, String privateKeyPath, String charset) throws Exception {
		FileInputStream fis = new FileInputStream(privateKeyPath);
		ObjectInputStream ois = new ObjectInputStream(fis);
		PrivateKey privateK = (PrivateKey) ois.readObject();

		Signature signature = Signature.getInstance("SHA1withRSA");
		signature.initSign(privateK);
		signature.update(getContentBytes(text, charset));
		byte[] result = signature.sign();
		return org.apache.commons.codec.binary.Base64.encodeBase64String(result);
	}

	private static byte[] getContentBytes(String content, String charset) {
		if (charset == null || "".equals(charset)) {
			return content.getBytes();
		}
		try {
			return content.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
		}
	}


	protected static String createLinkString(Map<String, String> params, boolean encode) {

		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		String prestr = "";
		Charset charset = Charset.forName("UTF-8");
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key);
			if (org.apache.commons.lang.StringUtils.isEmpty(value)){
				continue;
			}
			if (encode) {
				try {
					value = URLEncoder.encode(value, charset.name());
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}

			if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
				prestr = prestr + key + "=" + value;
			} else {
				prestr = prestr + key + "=" + value + "&";
			}
		}

		if (prestr.endsWith("&")){
			return prestr.substring(0, prestr.length() - 1);
		}
		return prestr;
	}


	/**
	 * 计算当地玩乐（不包含演出票）是否已经使用
	 * @param resultList
	 * @param order
	 * @param ordOrderItem
	 * @return
	 */
	public static String calPalyNoShowticketPerformStatus(List<OrdTicketPerform> resultList, OrdOrder order,OrdOrderItem ordOrderItem){
		if(ordOrderItem == null && order != null){
			ordOrderItem =order.getMainOrderItem();
		}

		return calPalyNoShowticketPerformStatus(resultList, ordOrderItem);
	}
	/**
	 * 计算门票是否已经使用
	 * @param resultList
	 * @param order
	 * @param quantity
	 * @return
	 */
	public static String calPalyNoShowticketPerformStatus(List<OrdTicketPerform> resultList, OrdOrderItem ordOrderItem){
		String performStatus ="";
		List<Boolean> performFlgList = new ArrayList<Boolean>();
		List<Boolean> partPerformFlgList = new ArrayList<Boolean>();
		//取不到值
		if(resultList==null||resultList.size()==0){
			//未过指定游玩日
			if(DateUtils.addMinutes(ordOrderItem.getVisitTime(), 1439).after(new Date())){
				performStatus ="UNPERFORM";
				//已过指定游玩日
			}else{
				performStatus ="NEED_CONFIRM";
			}
			//取到数据
		}else{
			for(int i=0;i<resultList.size();i++){
				if(resultList.get(i).getPerformTime()==null){
					//未使用
					performFlgList.add(true);
				}else{
					//已使用
					performFlgList.add(false);
					if (((resultList.get(i).getActualAdult() == null ? 0
							: resultList.get(i).getActualAdult()) + (resultList
							.get(i).getActualChild() == null ? 0 : resultList
							.get(i).getActualChild())) !=  (ordOrderItem.getQuantity() == null ? 0
							: ordOrderItem.getQuantity())) {
						// 有使用时间但是总份数和实际使用的份数不一致
						partPerformFlgList.add(true);
					}
				}
			}
			if(!performFlgList.contains(false)){
				performStatus ="UNPERFORM";
			}else if(!performFlgList.contains(true)){
				performStatus ="PERFORM";
				//部分使用时
				if(partPerformFlgList.contains(true)){
					performStatus ="PART_PERFORM";
				}
			}else{
				performStatus ="PART_PERFORM";
			}
		}

		return performStatus;
	}

	public static boolean isPlayNoShowticketByCategoryId(Long categoryId){
		if(BIZ_CATEGORY_TYPE.category_wifi.getCategoryId().equals(categoryId)||
				BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(categoryId)||
				BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(categoryId)||
				BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(categoryId)
				|| BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(categoryId)){
			return true;
		}
		return false;

	}

	
	/**
	 * 是否是邮轮子订单、
	 * @param Long categoryId
	 * @author chenguangyao
	 * */
	public static boolean isShipOrderItem(Long categoryId){
		
		if(BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCategoryId().equals(categoryId)
				||BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(categoryId)
				||BizEnum.BIZ_CATEGORY_TYPE.category_sightseeing.getCategoryId().equals(categoryId)
				||BizEnum.BIZ_CATEGORY_TYPE.category_cruise_addition.getCategoryId().equals(categoryId)){
			
			return true;
		}
		
		return false;
	}

	/**
	 * 判断订单是否是出境自由行
	 * @param order
	 * @return
	 */
	public static boolean hasOutAndFreed(OrdOrder order) {
		if (order == null) {
			LOGGER.info("hasOutAndFreed param order is null");
			return Boolean.FALSE;
		}
		LOGGER.info("hasOutAndFreed: orderId" + order.getOrderId() + "," + order.getBuCode() + "," + order.getCategoryId());
		if (CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equalsIgnoreCase(order.getBuCode())
				&& BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId())) {
			LOGGER.info("hasOuteAndFred return true");
			return Boolean.TRUE;
		}
		return Boolean.FALSE;

	}

	/**
	 * 根据订单集合收集订单id集合
	 * */
	public static List<Long> getOrderIdList(List<OrdOrder> orderList){
		if (CollectionUtils.isEmpty(orderList)) {
			return null;
		}

		List<Long> orderIdList = new ArrayList<>();
		for (OrdOrder order : orderList) {
			if(order == null || order.getOrderId() == null) {
				continue;
			}
			Long orderId = order.getOrderId();
			orderIdList.add(orderId);
		}
		return orderIdList;
	}
	
	/**
	 * 输入为空时返回缺省值0
	 * @param val
	 * @return
	 */
	public static long getLongByDefault(Long val) {
		if(val == null)
			return 0;
		return val.longValue();
	}
	
	//获取服务器的IP地址
	 public static String  getServerIp(boolean onlyFirst){
         String SERVER_IP = null;
         try {
             Enumeration<java.net.NetworkInterface> netInterfaces = java.net.NetworkInterface.getNetworkInterfaces();
             java.net.InetAddress ip = null;
             while (netInterfaces.hasMoreElements()) {
            	 java.net.NetworkInterface ni = (java.net.NetworkInterface) netInterfaces.nextElement();
            	 if(!ni.getInetAddresses().hasMoreElements())
            		 continue;
                 ip = (java.net.InetAddress) ni.getInetAddresses().nextElement();
                 if (!ip.isLoopbackAddress()
                         && ip.getHostAddress().indexOf(":") == -1) {
                	 if(onlyFirst){
                		 SERVER_IP = ip.getHostAddress(); 
                		 break;
                	 } else {
	                     if(SERVER_IP == null){
	                    	 SERVER_IP = ip.getHostAddress();                    	 
	                     } else {
	                    	 SERVER_IP = SERVER_IP + "," + ip.getHostAddress(); 
	                     }
                	 }
                 }
             }
         } catch (java.net.SocketException e) {
         }
         return SERVER_IP;
     }

}
