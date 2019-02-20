package com.lvmama.vst.order.timeprice.service.lvf;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.MD5;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.flight.client.goods.vo.FlightNoVo;
import com.lvmama.vst.flight.client.product.service.FlightSearchService;
import com.lvmama.vst.flight.client.product.vo.LvfSignVo;
import com.lvmama.vst.flight.client.vo.FlightInfoVerifyRequest;
import com.lvmama.vst.flight.client.vo.FlightInfoVo;
import com.lvmama.vst.flight.client.vo.FlightSeatPriceDto;
import com.lvmama.vst.flight.client.vo.enumers.PassengerType;
import com.lvmama.vst.flight.client.vo.enumers.VerifyType;
import com.lvmama.vst.order.exception.GetVerifiedFlightInfoFailException;
import com.lvmama.vst.order.exception.HasRecommendFlightException;
import com.lvmama.vst.order.timeprice.po.OrderItemPricePO;
import com.lvmama.vst.order.timeprice.service.ItemOrdMulPriceRateService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class OrderLvfTimePriceServiceImpl {

	private static Logger logger = LoggerFactory.getLogger(OrderLvfTimePriceServiceImpl.class);

	//private static String VIRTUAL_GOODS_ID = Constant.getInstance().getProperty("category_route_aero_hotel.virtual.suppGoodsId");
	
	@Autowired
	@Qualifier("flightSearchService")
	protected FlightSearchService flightSearchService;

	@Resource(name = "itemOrdMulPriceRateServiceImpl")
	private ItemOrdMulPriceRateService itemOrdMulPriceRateService;


	/**
	 * 判断机票对接
	 * @param orderItem
	 * @return true:对接	false：非对接
	 */
	public static boolean isLvfItemByCatetory(OrdOrderItem orderItem){
		String categoryCode = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
		String supplierApiFlag = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.supplierApiFlag.name());
		//如果是“其他机票  && 对接”
		if(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCode().equals(categoryCode) && "Y".equalsIgnoreCase(supplierApiFlag)){
			return true;
		}
		return false;
	}

	/**
	 * 是否是动态打包产品下单
	 * @param buyInfo
	 * @return			true: 动态打包   false：非动态打包
	 */
	public static boolean isAutoPackProductOrder(BuyInfo buyInfo){
		if(buyInfo==null || buyInfo.getCategoryId()==null){
			return false;
		}
		return isAutoPackCategory(buyInfo.getCategoryId().longValue());
	}

	public static boolean isAutoPackCategory(long categoryId){
		return BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().longValue()==categoryId;
	}

	/**
	 * 检查动态打包的机票商品
	 * @param itemList
	 */
	public static void checkAutoPackProductFlightGoods(List<OrdOrderItem> itemList){
		if(itemList==null || itemList.size()<=0)	return;
		Long vGoodsId = Constant.getVirtualSuppGoodsId_Flight();
		if(vGoodsId==null)	return;
		for(OrdOrderItem ordOrderItem: itemList){
			if(isLvfItemByCatetory(ordOrderItem)){
				if( ordOrderItem.getSuppGoodsId()==null || vGoodsId.longValue() != ordOrderItem.getSuppGoodsId().longValue() ){
					throw new IllegalArgumentException("动态打包产品不能下单非虚拟对接机票["+ordOrderItem.getSuppGoodsId()+"].");
				}
			}
		}
	}

	/**
	 * 对接机票库存检查
	 * @param itemList
	 */
	public boolean checkStock_remoteLVF(List<OrdOrderItem> itemList){
		logger.info("checkStock_remoteLVF start---");
		List<OrdOrderItem> lvfItemList = filterLVFItem(itemList);
		if(lvfItemList==null || lvfItemList.size()<=0){
			return true;
		}
		List<FlightNoVo> beforeList = fillRequestFlightNoVos(lvfItemList);
		logger.info("After fillRequestFlightNoVos invoked, beforeList is " + GsonUtils.toJson(beforeList));
		List<FlightNoVo> afterList = queryFlightNoVos(beforeList);
		logger.info("After queryFlightNoVos invoked, afterList is " + GsonUtils.toJson(afterList));
		for(OrdOrderItem orderItem: lvfItemList){
			OrdOrderItemDTO orderItemDto = (OrdOrderItemDTO)orderItem;
			int adultNum = orderItemDto.getItem().getAdultQuantity();
			int childNum = orderItemDto.getItem().getChildQuantity();
			//核实价格
			long beforeAmt = orderItemDto.getItem().getAdultAmt()*adultNum;
			if(orderItemDto.getItem().getChildAmt()!=null){
				beforeAmt += orderItemDto.getItem().getChildAmt()*childNum;
			}
			for(FlightNoVo afterVo: afterList){
				//增加日志
				if(afterVo != null){
					logger.info("afterList->afterVo:"+GsonUtils.toJson(afterVo)+", item SuppGoodsId:"+orderItem.getSuppGoodsId());
				}else{
					logger.info("afterVo is null,item SuppGoodsId:"+orderItem.getSuppGoodsId());
				}
				if(orderItem.getSuppGoodsId().longValue()!=afterVo.getGoodsId().longValue()){
					continue;
				}
				checkQuantity(adultNum, childNum, afterVo);		//核实座位余数
				logger.debug("OrderItem check "+orderItem.hashCode()+".item "+orderItemDto.getItem().hashCode()+".adultAmt="+orderItemDto.getItem().getAdultAmt());
				long afterAmt = afterVo.getAdultAmt()*adultNum;
				if(childNum>0 && afterVo.getChildAmt()!=null){
					afterAmt += afterVo.getChildAmt()*childNum;
				}
				if(beforeAmt != afterAmt){
					String msgSt = "[lvf1002]航班价格有变动, ("+beforeAmt/100+" to "+afterAmt/100+")";
					logger.warn(msgSt);
					//throw new IllegalArgumentException(msgSt);
				}
				break;
			}
		}
		logger.info("checkStock_remoteLVF end---");
		return true;
	}
	
	/**
	 * 对接机票库存--初始化价格
	 * @param itemList
	 */
	public boolean initPriceByRemoteLVF( List<OrdOrderItem> itemList){
		logger.info("initPriceByRemoteLVF start---");
		List<OrdOrderItem> lvfItemList = filterLVFItem(itemList);
		if(lvfItemList==null || lvfItemList.size()<=0){
			return true;
		}
		List<FlightNoVo> beforeList = fillRequestFlightNoVos(lvfItemList);
		List<FlightNoVo> afterList = queryFlightNoVos(beforeList);
		logger.debug("OrderItem init_size "+afterList.size()+"/"+lvfItemList.size());
		for(OrdOrderItem orderItem: lvfItemList){
			for(FlightNoVo afterVo: afterList){
				//增加日志
				if(afterVo != null){
					logger.info("afterList:GoodsId:"+afterVo.getGoodsId()+"AdultAmt:"+afterVo.getAdultAmt()
							+"ChildAmt:"+afterVo.getChildAmt()+"item SuppGoodsId"+orderItem.getSuppGoodsId());
				}else{
					logger.info("afterVo is null item SuppGoodsId:"+orderItem.getSuppGoodsId());
				}
				if(orderItem.getSuppGoodsId().longValue()!=afterVo.getGoodsId().longValue()){
					continue;
				}
				OrdOrderItemDTO orderItemDto = (OrdOrderItemDTO)orderItem;
				List<OrdOrderStock> orderStockList = new ArrayList<OrdOrderStock>();
				int adultNum = orderItemDto.getItem().getAdultQuantity();	//不乘份数
				int childNum = orderItemDto.getItem().getChildQuantity();	//不乘份数
				checkQuantity(adultNum, childNum, afterVo);		//核实座位余数
//				if( afterVo.getRemain()==null || adultNum+childNum > afterVo.getRemain()){
//					throw new IllegalArgumentException("库存余数不足  ["+(adultNum+childNum)+"/"+(afterVo.getRemain()==null?0:afterVo.getRemain())+"]");
//				}
				long totalAmount = afterVo.getAdultAmt()*adultNum;
				if(childNum>0 && afterVo.getChildAmt()!=null){
					totalAmount += afterVo.getChildAmt()*childNum;
				}
//				orderItem.setSettlementPrice(afterVo.getAdultAmt());//以成人价为核算价，不精确
//				orderItem.setPrice(afterVo.getAdultAmt());
				orderItem.setTotalAmount(totalAmount);
				orderItem.setTotalSettlementPrice(totalAmount);
				accumulateOrderItemDataWithTimePrice(orderItem, afterVo.getAdultAmt(), afterVo.getChildAmt());
				logger.debug("Flight orderItem init " + orderItem.getSuppGoodsId() + "[" + orderItem.hashCode() + "]item=[" + orderItemDto.getItem().hashCode() + "]\t\t" + orderItem.getSuppGoodsName() + "\ttotalamount=" + orderItem.getTotalAmount());
				OrdOrderStock stock = createStock(orderItem.getVisitTime(), adultNum+childNum);
				makeNeedResourceConfirm(stock);
				makeInventoryFlag(stock);
				orderStockList.add(stock);
				makeNeedResourceConfirm(orderItem, orderStockList);
				orderItem.setOrderStockList(orderStockList);
			}
		}
		logger.info("initPriceByRemoteLVF end---");
		return true;
	}

	/**
	 * 对接机票库存--初始化价格
	 * 应用于：动态打包的  机+酒  品类
	 * @param itemList
	 */
	public boolean initPriceByAutoPackLVF( List<OrdOrderItem> itemList){
		List<OrdOrderItem> lvfItemList = filterLVFItem(itemList);
		if(lvfItemList==null || lvfItemList.size()<=0){
			return true;
		}
		for(OrdOrderItem orderItem: lvfItemList){
			BuyInfo.Item item = orderItem.getItem();
			OrdOrderItemDTO orderItemDto = (OrdOrderItemDTO)orderItem;
			List<OrdOrderStock> orderStockList = new ArrayList<OrdOrderStock>();
			int adultNum = item.getAdultQuantity();    //不乘份数
			int childNum = item.getChildQuantity();    //不乘份数
			//总价
			long totalAmount = item.getAdultAmt()*adultNum;
			//总结算价
			long totalSettlementAmount=0;
			if(item.getAdultSettlementAmt()!=null){
				totalSettlementAmount = item.getAdultSettlementAmt() * adultNum;
			}
			if(childNum>0){
				if(item.getChildAmt()!=null) {
					totalAmount += item.getChildAmt() * childNum;
				}
				if(item.getChildSettlementAmt() != null){
					totalSettlementAmount += item.getChildSettlementAmt() * childNum;
				}
			}
			if (orderItem.getTotalAmount() == null || orderItem.getTotalAmount() <= 0) {
				orderItem.setTotalAmount(totalAmount);
			}
			orderItem.setTotalSettlementPrice(totalSettlementAmount);
			OrderItemPricePO orderItemPricePO = new OrderItemPricePO(item.getAdultAmt(), item.getChildAmt(), item.getAdultSettlementAmt(), item.getChildSettlementAmt());
			itemOrdMulPriceRateService.calculateOrdMulPriceRate(orderItem, orderItemPricePO);

			logger.debug("Flight autoPack orderItem init " + orderItem.getSuppGoodsId() + "[" + orderItem.hashCode() + "]item=[" + orderItemDto.getItem().hashCode() + "]\t\t" + orderItem.getSuppGoodsName() + "\ttotalamount=" + orderItem.getTotalAmount());
			OrdOrderStock stock = createStock(orderItem.getVisitTime(), adultNum+childNum);
			makeNeedResourceConfirm(stock);
			makeInventoryFlag(stock);
			orderStockList.add(stock);
			makeNeedResourceConfirm(orderItem, orderStockList);
			orderItem.setOrderStockList(orderStockList);
		}
		return true;
	}

	/**
	 * 二次请求获取推荐的航班信息
	 * */
	public ResultHandleT<List<FlightNoVo>> catchRecommendFlight(List<OrdOrderItem> itemList){
		ResultHandleT<List<FlightNoVo>> resultHandleT = new ResultHandleT<List<FlightNoVo>>();
		Long startTimeMillis = System.currentTimeMillis();
		List<OrdOrderItem> lvfItemList = filterLVFItem(itemList);
		List<FlightNoVo> beforeList = fillRequestFlightNoVos(lvfItemList);
		String cacheKey = generateQueryKeyForCache(beforeList);
		logger.info("Catch recommend flight info for second request start, key is " + cacheKey);
		if(lvfItemList==null || lvfItemList.size()<=0){
			return resultHandleT;
		}
		List<FlightNoVo> afterList = MemcachedUtil.getInstance().get(cacheKey);
		if(CollectionUtils.isNotEmpty(afterList)){
			Long currentTimeMillis = System.currentTimeMillis();
			logger.info("The second request for stock check, key is [" + cacheKey + "], cache has value, will return cached value, cost time:" + (currentTimeMillis - startTimeMillis));
			resultHandleT.setReturnContent(afterList);
			return resultHandleT;
		}

		logger.info("The second request for stock check, remote invoke for flight info, key is " + cacheKey);
		LvfSignVo signVo = new LvfSignVo();
		ResultHandleT<List<FlightNoVo>> resultT = flightSearchService.queryFlightNoVosByFlightNoVos(signVo, beforeList);
		Long currentTimeMillis = System.currentTimeMillis();
		logger.info("The second request for stock check, remote invoke for flight info,key is " + cacheKey + ",cost time:" + (currentTimeMillis - startTimeMillis));
		if(!resultT.isSuccess() || resultT.getReturnContent()==null){
			throw new IllegalArgumentException("Error invoking remote service for second request for api flight info");
		}
		List<FlightNoVo> apiFlightList = resultT.getReturnContent();
		resultHandleT.setReturnContent(apiFlightList);
		return resultHandleT;
	}

	/**
	 * 航班余座数检查
	 * @param adultQuantity	成人数
	 * @param childQuantity	儿童数
	 * @param realTimeVo		实时航班信息
	 * @return
	 */
	private boolean checkQuantity(int adultQuantity, int childQuantity, FlightNoVo realTimeVo){
		try {
			logger.info("Now do stock check for api flight, flight info is " + JSONUtil.bean2Json(realTimeVo) + " adultQuantity is " + adultQuantity + " childQuantity is " + childQuantity);
		} catch (Exception e) {
			logger.error("Can't log info of api flight check:", e);
		}
		if(adultQuantity > realTimeVo.getRemain()){
			throw new IllegalArgumentException("[lvf1001]库存余数不足  ["+(adultQuantity)+"/"+realTimeVo.getRemain()+"]");
		}
		if(childQuantity>0 && realTimeVo.getChildAmt()==null){
			throw new IllegalArgumentException("[lvf1001]库存余数不足  [儿童票已售完]");
		}
		if(adultQuantity * 2 <childQuantity){
			throw new IllegalArgumentException("库存检查失败，儿童数不能大于成人数的2倍。");
		}
		return true;
	}

	private List<FlightNoVo> queryFlightNoVos(List<FlightNoVo> beforeList){
		List<FlightNoVo> afterList;
		String cacheKey = generateQueryKeyForCache(beforeList);
		logger.info("Trying to catch api flight info message, beforeList is " + GsonUtils.toJson(beforeList));
		if(cacheKey!=null){	//尝试取缓存
			Date start = new Date();
			afterList = MemcachedUtil.getInstance().get(cacheKey);
			if(afterList!=null){
				Date end = new Date();
				long millis = end.getTime()-start.getTime();
				logger.info("线路后台加载商品调用 LVF_byCache 用时@flightSearchService.queryFlightNoVosByFlightNoVos@" + (millis));
				logger.info("Query api flight from cache, result is " + GsonUtils.toJson(afterList));
				//如果返回的机票信息中包含推荐的航班，抛出异常，等待第二次请求时获取航班信息
				for (FlightNoVo flightNoVo : afterList) {
					if(StringUtils.equalsIgnoreCase(flightNoVo.getRecommendFlightFlag(), Constants.Y_FLAG)){
						logger.info("remote request key is " + cacheKey + ", Goods " + flightNoVo.getGoodsId() + ", original goods " + flightNoVo.getOriginalGoodsId() + ",flight no[" + flightNoVo.getFlightNo() + "] is recommend flight, this flight has been check before, now don't need to check, will throw HasRecommendFlightException");
						throw new HasRecommendFlightException();
					}
				}
				return afterList;
			}
		}
		LvfSignVo signVo = new LvfSignVo();
		Date start = new Date();
		logger.info(GsonUtils.toJson(beforeList));
		ResultHandleT<List<FlightNoVo>> resultT = flightSearchService.queryFlightNoVosByFlightNoVos(signVo, beforeList);

		Date end = new Date();
		long millis = end.getTime()-start.getTime();

//		logger.debug("Hessian service expend ["+millis+"] millis.\tsignature:com.lvmama.vst.flight.client.product.service.FlightSearchService.queryFlightNoVosByFlightNoVos");
		logger.info("线路后台加载商品调用 LVF 用时@flightSearchService.queryFlightNoVosByFlightNoVos@" + (millis));
		if(!resultT.isSuccess() || resultT.getReturnContent()==null){
			throw new IllegalArgumentException("库存调用失败");
		}
		afterList = resultT.getReturnContent();
		logger.info("Query api flight from remote flight interface, result is " + GsonUtils.toJson(afterList));
		//如果返回的机票信息中包含推荐的航班，则把返回的航班信息放到缓存中，等待第二次请求时获取，同时抛出异常
		for (FlightNoVo flightNoVo : afterList) {
			if(StringUtils.equalsIgnoreCase(flightNoVo.getRecommendFlightFlag(), Constants.Y_FLAG)){
				logger.info("remote request key is " + cacheKey + ",Goods " + flightNoVo.getGoodsId() + ", original goods " + flightNoVo.getOriginalGoodsId() + ",flight no[" + flightNoVo.getFlightNo() + "] is recommend flight, will be set to cache, and throw HasRecommendFlightException");
				MemcachedUtil.getInstance().set(cacheKey, 300, afterList);
				throw new HasRecommendFlightException();
			}
		}
		List<FlightNoVo> unmatchedList = new ArrayList<FlightNoVo>();
		for(FlightNoVo oneBeforeVo: beforeList){
			boolean isMatched = false;
			for(int i=0;i<afterList.size();i++){
				FlightNoVo oneAfterVo = afterList.get(i);
				logger.info("from flightSearchService.queryFlightNoVosByFlightNoVos data:GoodsId:"
						+oneAfterVo.getGoodsId()+"AdultAmt:"+oneAfterVo.getAdultAmt()
						+"ChildAmt:"+oneAfterVo.getChildAmt() + ",remain:" + oneAfterVo.getRemain());
				if( oneBeforeVo.getGoodsId().longValue()==oneAfterVo.getGoodsId().longValue()){
					isMatched = true;
					break;
				}
			}
			if( !isMatched )	unmatchedList.add(oneBeforeVo);
		}
		if(unmatchedList.size()>0){
			List<String> losedGoods = new ArrayList<String>();
			for(FlightNoVo oneVo : unmatchedList){
				losedGoods.add(String.valueOf(oneVo.getGoodsId()));
			}
			logger.info("Result is wrong while inquired from LVF. Losed goods "+losedGoods+". ");
			throw new IllegalArgumentException("库存调用结果不匹配");
		}
		//加入缓存  5 分钟
		MemcachedUtil.getInstance().set(cacheKey, 300, afterList);
		return afterList;
	}

	/**
	 *	生成缓存中的 KEY
	 * @param beforeList
	 * @return
	 */
	private String generateQueryKeyForCache(List<FlightNoVo> beforeList){
		String retKey = null;
		List<String> keyItems = new ArrayList<String>();
		for(FlightNoVo oneVo: beforeList){
			keyItems.add(String.valueOf(oneVo.getGoodsId())+ DateUtil.formatDate(oneVo.getGoTime(), "yyyyMMddHHmmss"));
		}
		Collections.sort(keyItems);
		retKey = "";
		for(String keyItem: keyItems){
			retKey += keyItem;
		}
		try{
			retKey = "K_LVF_QUERY_"+MD5.encode(retKey);
		}catch(Exception ex){
			retKey = null;
		}
		return retKey;
	}

	/**
	 * 过滤出 对接机票 数据
	 * @param itemList
	 * @return
	 * @throws IllegalArgumentException
	 */
	private List<OrdOrderItem> filterLVFItem(List<OrdOrderItem> itemList) throws IllegalArgumentException{
		List<OrdOrderItem> lvfItemList = new ArrayList<OrdOrderItem>();
		for(OrdOrderItem item: itemList){
			if(isLvfItemByCatetory(item)){
				OrdOrderItemDTO orderItemDto = (OrdOrderItemDTO)item;
				if(orderItemDto==null || orderItemDto.getItem()==null || orderItemDto.getSuppGoodsId()==null ){
					throw new IllegalArgumentException("Order item is NULL or it's suppGoodsId is NULL.");
				}
				if(orderItemDto.getItem().getAdultAmt()==null && orderItemDto.getItem().getAdultQuantity()>0){
					throw new IllegalArgumentException("Order item ["+orderItemDto.getSuppGoodsId()+"] need parameter 'adultAmt' when adult's quantity > 0 .");
				}
				if(orderItemDto.getItem().getChildAmt()==null && orderItemDto.getItem().getChildQuantity()>0){
					throw new IllegalArgumentException("Order item ["+orderItemDto.getSuppGoodsId()+"] need parameter 'childAmt' when child's quantity > 0 .");
				}
				logger.info("filter api flight item:SuppGoodsId:"+orderItemDto.getSuppGoodsId());
				lvfItemList.add(item);
			}
		}
		return lvfItemList;
	}
	
	/**
	 * 填充请求数据
	 * @param lvfItemList
	 * @return
	 */
	private List<FlightNoVo> fillRequestFlightNoVos(List<OrdOrderItem> lvfItemList){
		List<FlightNoVo> retList = new ArrayList<FlightNoVo>();
		for(OrdOrderItem orderItem: lvfItemList){
			FlightNoVo oneVo= new FlightNoVo();
			OrdOrderItemDTO orderItemDto = (OrdOrderItemDTO)orderItem;
			
			logger.info("beforeList:SuppGoodsId:"+orderItemDto.getSuppGoodsId()
					+"AdultAmt:"+orderItemDto.getItem().getAdultAmt()
					+"ChildAmt:"+orderItemDto.getItem().getChildAmt());
			
			oneVo.setAdultAmt(orderItemDto.getItem().getAdultAmt());
			oneVo.setChildAmt(orderItemDto.getItem().getChildAmt());
			oneVo.setGoodsId(orderItemDto.getSuppGoodsId());
			oneVo.setGoTime(orderItemDto.getVisitTime());
//			oneVo.setRemain(100L);		//测试用途
			if(oneVo.getGoodsId()==null || oneVo.getGoTime()==null){
				throw new IllegalArgumentException("Order item convert to FlightNoVo error.Parameters [suppGoodsId, visitTime] should not null or none.");
			}
			if(orderItemDto.getItem().getAdultAmt()==null){
				throw new IllegalArgumentException("Order item convert to FlightNoVo error.Parameters [adultAmt] should not null .");
			}
			if(orderItemDto.getItem().getChildAmt()==null && orderItemDto.getItem().getChildQuantity()>0){
				throw new IllegalArgumentException("Order item convert to FlightNoVo error.Parameters [childAmt] should not null when [childQuantity]>0.");
			}
			retList.add(oneVo);
		}
		return retList;
	}
	
	protected OrdOrderStock createStock(Date visitTime,long quantity){
		OrdOrderStock stock = new OrdOrderStock();
		stock.setQuantity(quantity);
//		stock.setInventory(OrderEnum.INVENTORY_STATUS.UNINVENTORY.name());
		stock.setVisitTime(visitTime);
//		stock.setNeedResourceConfirm("true");
//		stock.setResourceStatus(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name());
		return stock;
	}
	
	/**
	 * 不需要资源确认
	 * @param stock
	 */
	protected void makeNotNeedResourceConfirm(final OrdOrderStock stock){
		stock.setNeedResourceConfirm("false");
		stock.setInventory(OrderEnum.INVENTORY_STATUS.INVENTORY.name());
		stock.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.name());
	}

	/**
	 * 需要资源审核的库存项
	 * @param stock
	 */
	protected void makeNeedResourceConfirm(final OrdOrderStock stock){
		stock.setNeedResourceConfirm("true");
		stock.setInventory(OrderEnum.INVENTORY_STATUS.UNINVENTORY.name());
		stock.setResourceStatus(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name());
	}
	
	protected void makeNeedResourceConfirm(OrdOrderItem orderItem,
			List<OrdOrderStock> stockList) {
		for(OrdOrderStock stock:stockList){
			setOrderItemsNeedResourceConfirm(stock.getNeedResourceConfirm(), orderItem);
		}
	}
	
	/**
	 * 设置订单那子项是否需要资源确认
	 * 
	 * @param needResourceConfirm
	 * @param orderItem
	 */
	protected void setOrderItemsNeedResourceConfirm(String needResourceConfirm, OrdOrderItem orderItem) {
		if ("true".equals(orderItem.getNeedResourceConfirm())) {
			orderItem.setNeedResourceConfirm(needResourceConfirm);
		}
	}
	
	protected void makeInventoryFlag(final OrdOrderStock stock){
		stock.setInventory(OrderEnum.INVENTORY_STATUS.INVENTORY.name());
	}

	/**
	 * 商品价格处理
	 * @param orderItem
	 * @return
	 */
	private void accumulateOrderItemDataWithTimePrice(OrdOrderItem orderItem, Long adultAmt, Long childAmt) {
		if (orderItem == null ) {
			return;
		}
		long allPriceAmount = 0;
		long allSettlementPriceAmount = 0;
		long allMarketPriceAmount = 0;

		List<OrdMulPriceRate> ordMulPriceRateList = new ArrayList<OrdMulPriceRate>();
		int count = 0;

		OrdOrderItemDTO orderItemDto = (OrdOrderItemDTO)orderItem;
		int adultNum = orderItemDto.getItem().getAdultQuantity();	//不乘份数
		int childNum = orderItemDto.getItem().getChildQuantity();	//不乘份数

		if (adultNum>0) {
			OrdMulPriceRate ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(adultAmt, new Long(adultNum), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name());
			ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.PRICE.name());
			ordMulPriceRateList.add(ordMulPriceRate);

			ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(adultAmt, new Long(adultNum), OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.name());
			ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.SETTLEMENT.name());
			ordMulPriceRateList.add(ordMulPriceRate);

			ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(adultAmt, new Long(adultNum), OrderEnum.ORDER_PRICE_RATE_TYPE.MARKET_ADULT.name());
			ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.MARKET.name());
			ordMulPriceRateList.add(ordMulPriceRate);

			allPriceAmount += adultAmt * adultNum;
			allSettlementPriceAmount += adultAmt * adultNum;
			allMarketPriceAmount += adultAmt * adultNum;
		}

		if (childNum>0) {
			OrdMulPriceRate ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(childAmt, new Long(childNum), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD.name());
			ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.PRICE.name());
			ordMulPriceRateList.add(ordMulPriceRate);

			ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(childAmt, new Long(childNum), OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.name());
			ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.SETTLEMENT.name());
			ordMulPriceRateList.add(ordMulPriceRate);

			ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(childAmt, new Long(childNum), OrderEnum.ORDER_PRICE_RATE_TYPE.MARKET_CHILD.name());
			ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.MARKET.name());
			ordMulPriceRateList.add(ordMulPriceRate);

			allPriceAmount += childAmt * childNum;
			allSettlementPriceAmount += childAmt * childNum;
			allMarketPriceAmount += childAmt * childNum;
		}

		long priceAmount = (long)(allPriceAmount * 1.0 / orderItem.getQuantity() + 0.5);
		long settlementPriceAmount = (long)(allSettlementPriceAmount * 1.0 / orderItem.getQuantity() + 0.5);
		long marketPriceAmount = (long)(allMarketPriceAmount * 1.0 / orderItem.getQuantity() + 0.5);

		OrderUtils.accumulateOrderItemPrice(orderItem,
				priceAmount,
				settlementPriceAmount,
				settlementPriceAmount,
				marketPriceAmount);

		orderItem.setOrdMulPriceRateList(ordMulPriceRateList);
	}

	//处理测试用的航班数据，仅仅测试的时候调用，上线时禁止调用
	private void dealTestFlightList(List<FlightNoVo> afterList){
		long toSeatGoodsId = 1779673L;
		long backSeatGoodsId = 1753984L;
		
		if(CollectionUtils.isEmpty(afterList)){
			logger.info("test data flight list is empty===");
		}
		//是否更换过goodsId
		FlightNoVo toFlightNoVo = afterList.get(0);
		FlightNoVo backFlightNoVo = afterList.get(1);
		changeGoodsId(toFlightNoVo, toSeatGoodsId, "8L9609");
		changeGoodsId(backFlightNoVo, backSeatGoodsId, "HU761Z");
	}

	//处理测试用的航班数据，仅仅测试的时候调用，上线时禁止调用
	private void changeGoodsId(FlightNoVo flightNoVo, long recommendGoodsId, String flightNo){
		if(flightNoVo == null){
			return;
		}
		flightNoVo.setRemain(10L);
		flightNoVo.setChildAmt(52000L);
		flightNoVo.setAdultAmt(38000L);
		flightNoVo.setFlightNo(flightNo);

		Long goodsId = flightNoVo.getGoodsId();
		if(goodsId == null|| goodsId == recommendGoodsId){
			return;
		}
		flightNoVo.setOriginalGoodsId(goodsId);
		flightNoVo.setGoodsId(recommendGoodsId);
		flightNoVo.setRecommendFlightFlag(Constants.Y_FLAG);
		logger.info("Do test data change, from " + goodsId + " to " + recommendGoodsId);
	}
	
	public void initPriceByRemoteLVFNew(List<OrdOrderItem> itemList) {

		logger.info("initPriceByRemoteLVFNew start---");
		List<OrdOrderItem> lvfItemList = filterLVFItem(itemList);
		if(lvfItemList==null || lvfItemList.size()<=0){
			return ;
		}
		//List<FlightNoVo> beforeList = fillRequestFlightNoVos(lvfItemList);
		List<FlightInfoVerifyRequest> beforeList = fillRequestFlightInfo(lvfItemList);
		//List<FlightNoVo> afterList = queryFlightNoVos(beforeList);
		List<FlightInfoVo> afterList = queryFlightInfo(beforeList);
		logger.debug("OrderItem init_size "+afterList.size()+"/"+lvfItemList.size());
		for(OrdOrderItem orderItem: lvfItemList){
			for(FlightInfoVo afterVo: afterList){
				Long adultAmt=null;
				Long childAmt=null;
				String adultSeatCode = null;
				String childSeatCode = null;
				Integer adultInventoryCount=null;
				Integer childInventoryCount=null;
				for (FlightSeatPriceDto flightSeatPriceDto : afterVo.getFlightSeatPriceDtos()) {
					if(PassengerType.ADULT.getCnName().equals(flightSeatPriceDto.getPassengerType().getCnName())){
						adultInventoryCount = flightSeatPriceDto.getInventoryCount();
						adultAmt = flightSeatPriceDto.getSalesPrice().longValue();
					    adultSeatCode = flightSeatPriceDto.getSeatClassCode();
					}else if(PassengerType.CHILDREN.getCnName().equals(flightSeatPriceDto.getPassengerType().getCnName())){
						childInventoryCount = flightSeatPriceDto.getInventoryCount();
						childAmt = flightSeatPriceDto.getSalesPrice().longValue();
						childSeatCode = flightSeatPriceDto.getSeatClassCode();
					}
				}
				//增加日志
				if(afterVo != null){
					logger.info("afterList:GoodsId:"+afterVo.getGoodsId()+"AdultAmt:"+adultAmt
							+"ChildAmt:"+childAmt+"item SuppGoodsId"+orderItem.getSuppGoodsId());
				}else{
					logger.info("afterVo is null item SuppGoodsId:"+orderItem.getSuppGoodsId());
				}
				if(orderItem.getSuppGoodsId().longValue()!=afterVo.getGoodsId().longValue()){
					continue;
				}
				OrdOrderItemDTO orderItemDto = (OrdOrderItemDTO)orderItem;
				List<OrdOrderStock> orderStockList = new ArrayList<OrdOrderStock>();
				int adultNum = orderItemDto.getItem().getAdultQuantity();	//不乘份数
				int childNum = orderItemDto.getItem().getChildQuantity();	//不乘份数
				checkQuantity(adultNum, childNum, adultSeatCode,childSeatCode,adultInventoryCount,childInventoryCount);		//核实座位余数
//				if( afterVo.getRemain()==null || adultNum+childNum > afterVo.getRemain()){
//					throw new IllegalArgumentException("库存余数不足  ["+(adultNum+childNum)+"/"+(afterVo.getRemain()==null?0:afterVo.getRemain())+"]");
//				}
				if(null==adultAmt||(childNum>0&&null==childAmt)){
					throw new GetVerifiedFlightInfoFailException("[lvf1001]库存余数不足");
				}
				adultAmt=adultAmt*100;
				long totalAmount = adultAmt*adultNum;
				if(childNum>0 && childAmt!=null){
					childAmt=childAmt*100;
					totalAmount += childAmt*childNum;
				}
//				orderItem.setSettlementPrice(afterVo.getAdultAmt());//以成人价为核算价，不精确
//				orderItem.setPrice(afterVo.getAdultAmt());
				orderItem.setTotalAmount(totalAmount);
				orderItem.setTotalSettlementPrice(totalAmount);
				accumulateOrderItemDataWithTimePrice(orderItem, adultAmt, childAmt);
				logger.debug("Flight orderItem init " + orderItem.getSuppGoodsId() + "[" + orderItem.hashCode() + "]item=[" + orderItemDto.getItem().hashCode() + "]\t\t" + orderItem.getSuppGoodsName() + "\ttotalamount=" + orderItem.getTotalAmount());
				OrdOrderStock stock = createStock(orderItem.getVisitTime(), adultNum+childNum);
				makeNeedResourceConfirm(stock);
				makeInventoryFlag(stock);
				orderStockList.add(stock);
				makeNeedResourceConfirm(orderItem, orderStockList);
				orderItem.setOrderStockList(orderStockList);
			}
		}
		logger.info("initPriceByRemoteLVFNew end---");
		
	
	}

	private boolean checkQuantity(int adultQuantity, int childQuantity,
			 String adultSeatCode, String childSeatCode, Integer adultInventoryCount, Integer childInventoryCount) {
		try {
			logger.info("Now do stock check for api flight, flight info is " + " adultInventoryCount is " + adultInventoryCount + " childInventoryCount is " + childInventoryCount + " adultQuantity is " + adultQuantity + " childQuantity is " + childQuantity);
		} catch (Exception e) {
			logger.error("Can't log info of api flight check:", e);
		}
		if(null==adultSeatCode||null==adultInventoryCount){
			throw new GetVerifiedFlightInfoFailException("[lvf1001]库存余数不足  [成人票已售完]");
		}
		if(adultSeatCode.equals(childSeatCode)){
			if((adultQuantity+childQuantity)>adultInventoryCount){
				throw new GetVerifiedFlightInfoFailException("[lvf1001]库存余数不足  ["+adultQuantity+"+"+childQuantity+"/"+adultInventoryCount+"]");
			}
		}else{
			if(adultQuantity > adultInventoryCount){
				throw new GetVerifiedFlightInfoFailException("[lvf1001]库存余数不足  ["+(adultQuantity)+"/"+adultInventoryCount+"]");
			}
			if(childQuantity>0 && (null==childSeatCode||null==childInventoryCount)){
				throw new GetVerifiedFlightInfoFailException("[lvf1001]库存余数不足  [儿童票已售完]");
			}
			if(childQuantity>0 && childQuantity > childInventoryCount){
				throw new GetVerifiedFlightInfoFailException("[lvf1001]库存余数不足  ["+(childQuantity)+"/"+childInventoryCount+"]");
			}
			
		}
		if(adultQuantity * 2 <childQuantity){
			throw new GetVerifiedFlightInfoFailException("库存检查失败，儿童数不能大于成人数的2倍。");
		}
		return true;
		
	}

	private List<FlightInfoVo> queryFlightInfo(
			List<FlightInfoVerifyRequest> beforeList) {

		List<FlightInfoVo> afterList = new ArrayList<FlightInfoVo>();
		Date start = new Date();
		logger.info(GsonUtils.toJson(beforeList));
		for (FlightInfoVerifyRequest flightInfoVerifyRequest : beforeList) {
			ResultHandleT<FlightInfoVo> flightInfoVo = flightSearchService.getVerifiedFlightInfo(flightInfoVerifyRequest);
			if(!flightInfoVo.isSuccess() || flightInfoVo.getReturnContent()==null){
				logger.info("机酒调用对接机票查询新接口 用时@flightSearchService.getVerifiedFlightInfo@" + flightInfoVo.getMsg());
				throw new GetVerifiedFlightInfoFailException("库存调用失败");
			}
			afterList.add(flightInfoVo.getReturnContent());
		}
		for (FlightInfoVo flightInfoVo : afterList) {
			if(null==flightInfoVo.getFlightSeatPriceDtos()||flightInfoVo.getFlightSeatPriceDtos().size()<1){
				throw new GetVerifiedFlightInfoFailException("库存调用失败");
			}
		}
		Date end = new Date();
		
		long millis = end.getTime()-start.getTime();

//		logger.debug("Hessian service expend ["+millis+"] millis.\tsignature:com.lvmama.vst.flight.client.product.service.FlightSearchService.queryFlightNoVosByFlightNoVos");
		logger.info("机酒调用对接机票查询新接口 用时@flightSearchService.getVerifiedFlightInfo@" + (millis));
		logger.info("Query api flight from remote flight interface, result is " + GsonUtils.toJson(afterList));
		List<FlightInfoVerifyRequest> unmatchedList = new ArrayList<FlightInfoVerifyRequest>();
		for(FlightInfoVerifyRequest oneBeforeVo: beforeList){
			boolean isMatched = false;
			for(int i=0;i<afterList.size();i++){
				FlightInfoVo oneAfterVo = afterList.get(i);
				List<FlightSeatPriceDto> flightSeatPriceDtos = oneAfterVo.getFlightSeatPriceDtos();
				Long adultAmt=null;
				Long childAmt=null;
				Integer adultInventoryCount=null;
				Integer childInventoryCount=null;
				for (FlightSeatPriceDto flightSeatPriceDto : flightSeatPriceDtos) {
					if(PassengerType.ADULT.getCnName().equals(flightSeatPriceDto.getPassengerType().getCnName())){
						adultInventoryCount = flightSeatPriceDto.getInventoryCount();
						adultAmt = flightSeatPriceDto.getSalesPrice().longValue();
					}else if(PassengerType.CHILDREN.getCnName().equals(flightSeatPriceDto.getPassengerType().getCnName())){
						childInventoryCount = flightSeatPriceDto.getInventoryCount();
						childAmt = flightSeatPriceDto.getSalesPrice().longValue();
					}
				}
				logger.info("from flightSearchService.getVerifiedFlightInfo data:GoodsId:"
						+oneAfterVo.getGoodsId()+"AdultAmt:"+adultAmt
						+"ChildAmt:"+childAmt + ",AdultInventoryCount:" + adultInventoryCount+"ChildInventoryCount:"+childInventoryCount);
				if( oneBeforeVo.getGoodsId().longValue()==oneAfterVo.getGoodsId().longValue()){
					isMatched = true;
					break;
				}
			}
			if( !isMatched )	unmatchedList.add(oneBeforeVo);
		}
		if(unmatchedList.size()>0){
			List<String> losedGoods = new ArrayList<String>();
			for(FlightInfoVerifyRequest oneVo : unmatchedList){
				losedGoods.add(String.valueOf(oneVo.getGoodsId()));
			}
			logger.info("Result is wrong while inquired from LVF. Losed goods "+losedGoods+". ");
			throw new GetVerifiedFlightInfoFailException("库存调用结果不匹配");
		}
		return afterList;
	
	}

	private List<FlightInfoVerifyRequest> fillRequestFlightInfo(
			List<OrdOrderItem> lvfItemList) {

		List<FlightInfoVerifyRequest> retList = new ArrayList<FlightInfoVerifyRequest>();
		for(OrdOrderItem orderItem: lvfItemList){
			FlightInfoVerifyRequest oneVo= new FlightInfoVerifyRequest();
			OrdOrderItemDTO orderItemDto = (OrdOrderItemDTO)orderItem;
			
			logger.info("beforeList:SuppGoodsId:"+orderItemDto.getSuppGoodsId()
					+"AdultAmt:"+orderItemDto.getItem().getAdultAmt()
					+"ChildAmt:"+orderItemDto.getItem().getChildAmt());
			
			oneVo.setDepartureDate(orderItemDto.getVisitTime());
			oneVo.setGoodsId(orderItemDto.getSuppGoodsId());
			oneVo.setVerifyType(VerifyType.VERIFY_ALL);
//			oneVo.setRemain(100L);		//测试用途
			if(oneVo.getGoodsId()==null || oneVo.getDepartureDate()==null){
				throw new IllegalArgumentException("Order item convert to FlightNoVo error.Parameters [suppGoodsId, visitTime] should not null or none.");
			}
			if(orderItemDto.getItem().getAdultAmt()==null){
				throw new IllegalArgumentException("Order item convert to FlightNoVo error.Parameters [adultAmt] should not null .");
			}
			if(orderItemDto.getItem().getChildAmt()==null && orderItemDto.getItem().getChildQuantity()>0){
				throw new IllegalArgumentException("Order item convert to FlightNoVo error.Parameters [childAmt] should not null when [childQuantity]>0.");
			}
			retList.add(oneVo);
		}
		return retList;
	
	}

	public void initPriceByRemoteLVFNew(List<OrdOrderItem> orderItemList,
			Map<String, Object> attributes) {
		logger.info("initPriceByRemoteLVFNew start---");
		List<OrdOrderItem> lvfItemList = filterLVFItem(orderItemList);
		if(lvfItemList==null || lvfItemList.size()<=0){
			return ;
		}
		//List<FlightNoVo> beforeList = fillRequestFlightNoVos(lvfItemList);
		List<FlightInfoVerifyRequest> beforeList = fillRequestFlightInfo(lvfItemList);
		//List<FlightNoVo> afterList = queryFlightNoVos(beforeList);
		List<FlightInfoVo> afterList = queryFlightInfo(beforeList);
		StringBuffer sb = new StringBuffer();
		logger.debug("OrderItem init_size "+afterList.size()+"/"+lvfItemList.size());
		for(OrdOrderItem orderItem: lvfItemList){
			for(FlightInfoVo afterVo: afterList){
				Long adultAmt=null;
				Long childAmt=null;
				String adultSeatCode = null;
				String childSeatCode = null;
				Integer adultInventoryCount=null;
				Integer childInventoryCount=null;
				for (FlightSeatPriceDto flightSeatPriceDto : afterVo.getFlightSeatPriceDtos()) {
					if(PassengerType.ADULT.getCnName().equals(flightSeatPriceDto.getPassengerType().getCnName())){
						adultInventoryCount = flightSeatPriceDto.getInventoryCount();
						adultAmt = flightSeatPriceDto.getSalesPrice().longValue();
					    adultSeatCode = flightSeatPriceDto.getSeatClassCode();
					}else if(PassengerType.CHILDREN.getCnName().equals(flightSeatPriceDto.getPassengerType().getCnName())){
						childInventoryCount = flightSeatPriceDto.getInventoryCount();
						childAmt = flightSeatPriceDto.getSalesPrice().longValue();
						childSeatCode = flightSeatPriceDto.getSeatClassCode();
					}
				}
				//增加日志
				if(afterVo != null){
					logger.info("afterList:GoodsId:"+afterVo.getGoodsId()+"AdultAmt:"+adultAmt
							+"ChildAmt:"+childAmt+"item SuppGoodsId"+orderItem.getSuppGoodsId());
				}else{
					logger.info("afterVo is null item SuppGoodsId:"+orderItem.getSuppGoodsId());
				}
				if(orderItem.getSuppGoodsId().longValue()!=afterVo.getGoodsId().longValue()){
					continue;
				}
				sb.append(afterVo.getFlightNo()+"、");
				OrdOrderItemDTO orderItemDto = (OrdOrderItemDTO)orderItem;
				List<OrdOrderStock> orderStockList = new ArrayList<OrdOrderStock>();
				int adultNum = orderItemDto.getItem().getAdultQuantity();	//不乘份数
				int childNum = orderItemDto.getItem().getChildQuantity();	//不乘份数
				checkQuantity(adultNum, childNum, adultSeatCode,childSeatCode,adultInventoryCount,childInventoryCount);		//核实座位余数
//				if( afterVo.getRemain()==null || adultNum+childNum > afterVo.getRemain()){
//					throw new IllegalArgumentException("库存余数不足  ["+(adultNum+childNum)+"/"+(afterVo.getRemain()==null?0:afterVo.getRemain())+"]");
//				}
				if(null==adultAmt||(childNum>0&&null==childAmt)){
					throw new GetVerifiedFlightInfoFailException("[lvf1001]库存余数不足");
				}
				adultAmt=adultAmt*100;
				long totalAmount = adultAmt*adultNum;
				if(childNum>0 && childAmt!=null){
					childAmt=childAmt*100;
					totalAmount += childAmt*childNum;
				}
//				orderItem.setSettlementPrice(afterVo.getAdultAmt());//以成人价为核算价，不精确
//				orderItem.setPrice(afterVo.getAdultAmt());
				orderItem.setTotalAmount(totalAmount);
				orderItem.setTotalSettlementPrice(totalAmount);
				accumulateOrderItemDataWithTimePrice(orderItem, adultAmt, childAmt);
				logger.debug("Flight orderItem init " + orderItem.getSuppGoodsId() + "[" + orderItem.hashCode() + "]item=[" + orderItemDto.getItem().hashCode() + "]\t\t" + orderItem.getSuppGoodsName() + "\ttotalamount=" + orderItem.getTotalAmount());
				OrdOrderStock stock = createStock(orderItem.getVisitTime(), adultNum+childNum);
				makeNeedResourceConfirm(stock);
				makeInventoryFlag(stock);
				orderStockList.add(stock);
				makeNeedResourceConfirm(orderItem, orderStockList);
				orderItem.setOrderStockList(orderStockList);
			}
		}
		sb.deleteCharAt(sb.length()-1);
		attributes.put("flightNo", sb.toString());
		logger.info("initPriceByRemoteLVFNew end---");
		
	}
}
