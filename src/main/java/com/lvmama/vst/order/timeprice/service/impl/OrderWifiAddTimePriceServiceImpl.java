/**
 * 
 */
package com.lvmama.vst.order.timeprice.service.impl;

import net.sf.json.JSONArray;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.wifi.service.WifiSuppGoodsClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsRefund;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.goods.vo.SuppGoodsVO;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdOrderWifiTimeRate;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProduct.WIFIPRODUCTTYPE;
import com.lvmama.vst.back.prom.rule.favor.FavorableAmount;
import com.lvmama.vst.back.wifi.po.SuppGoodsRentedLimit;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.BuyInfo.WifiAdditation;
import com.lvmama.vst.order.timeprice.service.AbstractOrderTimePriceService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;

/**
 * wifi/电话卡时间价格实现类
 * （借用门票时间价格表）
 *
 */
@Component("orderWifiAddTimePriceService")
public class OrderWifiAddTimePriceServiceImpl extends AbstractOrderTimePriceService{
	
	private static final Logger logger = LoggerFactory.getLogger(OrderWifiAddTimePriceServiceImpl.class);

	@Autowired
	private DistGoodsTimePriceClientService distGoodsTimePriceClientService;
	
	@Resource(name="goodsOraWifiAddTimePriceStockService")
	private IGoodsTimePriceStockService goodsWifiAddTimePriceStockService;
	
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	@Autowired
	private ProdProductClientService prodProductClientService;
	@Autowired
    private WifiSuppGoodsClientService  wifiSuppGoodsClientService;
	
	@Override
	public ResultHandleT<Object> checkStock(SuppGoods suppGoods, Item item,
			Long distributionId, Map<String, Object> dataMap) {
		ResultHandleT<Object> resultHandleT = new ResultHandleT<Object>();
		
		try {
			if(suppGoods==null || !suppGoods.isValid()){
				resultHandleT.setMsg("商品不存在或无效。");
				return resultHandleT;
			}
			SuppGoodsRentedLimit suppGoodsRentedLimit =null;
			ProdProduct prodProduct = suppGoods.getProdProduct();
			if(prodProduct!=null){
				
				Short minRentedDays = null;
				Short maxRentedDays = null;
				String depositFlag = null;
				
				ResultHandleT<SuppGoodsRentedLimit> suppGoodsRentedLimitResult = wifiSuppGoodsClientService.findSuppGoodsRentedLimit(suppGoods.getSuppGoodsId());
		    	if(suppGoodsRentedLimitResult.hasNull()||suppGoodsRentedLimitResult.getReturnContent()==null){
		    		resultHandleT.setMsg("商品附加信息参数异常");
		    		return resultHandleT;
				}
		    	suppGoodsRentedLimit = suppGoodsRentedLimitResult.getReturnContent();
				if(suppGoodsRentedLimit!=null){
					
					minRentedDays = suppGoodsRentedLimit.getMinRentedDays();
					maxRentedDays = suppGoodsRentedLimit.getMaxRentedDays();
					depositFlag = suppGoodsRentedLimit.getDepositFlag();
				}
				if("Y".equals(depositFlag)){
					
					ResultHandleT<SuppGoodsVO> depositGoodResultHandle = suppGoodsClientService.findDepositSuppGoods(suppGoods.getSuppGoodsId(),item.getVisitTimeDate());
					if(depositGoodResultHandle==null 
						|| depositGoodResultHandle.getReturnContent()==null 
						|| depositGoodResultHandle.getReturnContent().getSuppGoodsNotimeTimePrice()==null
						){
						resultHandleT.setMsg("商品参数异常");
						LOG.error("关联押金信息异常   商品ID： "+suppGoods.getSuppGoodsId());
						return resultHandleT;
					}
				}
				if(WIFIPRODUCTTYPE.WIFI.name().equals(prodProduct.getProductType())){
					WifiAdditation wifiAdditation = item.getWifiAdditation();
					if(wifiAdditation == null){
						resultHandleT.setMsg("请选择结束时间");
						return resultHandleT;
					}
					if(minRentedDays==null && maxRentedDays==null){
						resultHandleT.setMsg("商品预订限制异常");
						return resultHandleT;
					}
					//商品参数验证
					checkParam(suppGoods, item, true);
					Date startDate = item.getVisitTimeDate();
					Date endDate = wifiAdditation.getBackTimeDate();
					if(startDate == null || endDate==null ){
						resultHandleT.setMsg("商品参数异常");
						return resultHandleT;
					}
					//计算日期天数
					int days =0;
					Date currDate = new Date();
					int betweenDays =DateUtil.getDaysBetween(item.getVisitTimeDate(), item.getWifiAdditation().getBackTimeDate())+1;
					for(int i = 0; i < betweenDays; i++){
						Date oneDate = DateUtils.addDays(startDate, i);
						ResultHandleT<SuppGoodsBaseTimePrice> timePriceHolder = this.getTimePrice(suppGoods.getSuppGoodsId(),oneDate,true);
						LOG.info("正在进行库存检查，获取时间价格表数据,得到的时间数据为 \n"+JSONArray.fromObject(timePriceHolder));
						LOG.info("OrderWifiAddTimePriceServiceImpl.checkStock(Date=" + oneDate + "): timePriceHolder.isSuccess=" + timePriceHolder.isSuccess());
						if(timePriceHolder.isFail() ){
							LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + ",时间" + oneDate + "时间价格表获取异常。");
							resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")时间价格表异常。");
							return resultHandleT;
						}
						if(timePriceHolder.getReturnContent() == null)continue;
						
						SuppGoodsBaseTimePrice timePrice = timePriceHolder.getReturnContent();
						if (!this.checkTimePrice(timePrice, currDate, (long) (item.getQuantity()))) {
							LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
							resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
							return resultHandleT;
						}
						
						SuppGoodsAddTimePrice addTimePrice = (SuppGoodsAddTimePrice)timePrice;
						
						//需要校验日库存/共享总库存/共享日限制
						if("Y".equalsIgnoreCase(addTimePrice.getStockFlag())){
							if (addTimePrice.getStock() < item.getQuantity()|| ((item.getCheckStockQuantity() != null && item.getCheckStockQuantity() > 0) && addTimePrice.getStock() < item.getCheckStockQuantity())) {// 库存不满足
								if("N".equalsIgnoreCase(addTimePrice.getOversellFlag())){
									continue;
								}
							}
						}
						days++;
					}
					if( days<minRentedDays.intValue() || days>maxRentedDays.intValue()){
						resultHandleT.setMsg("不满足最大最小租赁天数条件");
						LOG.error("租赁时间日期范围不满足条件,startDate:" + startDate + ", endDate:" + endDate);
						return resultHandleT;
					}
				}else if(WIFIPRODUCTTYPE.PHONE.name().equals(prodProduct.getProductType())){
					
						SuppGoodsBaseTimePrice timePrice = getTimePriceAndCheck(suppGoods,item,item.getVisitTimeDate());
						SuppGoodsAddTimePrice addTimePrice = (SuppGoodsAddTimePrice)timePrice;
						LOG.info("正在进行库存检查，该商品的价格类型为orderWifiAddTimePrice,得到的时间数据为 \n"
								+ JSONArray.fromObject(timePrice));
						if("Y".equalsIgnoreCase(addTimePrice.getStockFlag())){
							if (addTimePrice.getStock() < item.getQuantity()
									|| ((item.getCheckStockQuantity() != null && item
											.getCheckStockQuantity() > 0) && addTimePrice
											.getStock() < item.getCheckStockQuantity())
									) {// 库存不满足
								if("N".equalsIgnoreCase(addTimePrice.getOversellFlag())){
									throwIllegalException("库存不足");
								}
							}
						}
				}
			}
		
		} catch (Exception e) {
			resultHandleT.setMsg(e.getMessage());
		}

		return resultHandleT;
	
	}

	@Override
	public void updateStock(Long timePriceId, Long stock,
			Map<String, Object> dataMap) {
//		if(!goodsWifiAddTimePriceStockService.updateStock(timePriceId, -stock)){
		try {
			logger.info("com.lvmama.vst.order.timeprice.service.impl.OrderWifiAddTimePriceServiceImpl.updateStock# params is:timePriceId ="+timePriceId+" &stock =" + stock);
			if(!goodsWifiAddTimePriceStockService.updateStock(timePriceId, -stock, dataMap)){
				throwIllegalException("库存扣除操作失败");
			}
		} catch (Exception e) {
			logger.error("库存扣除操作失败, {}", e);
			throwIllegalException("库存扣除操作失败");
		}
	}
	

	@Override
	public ResultHandle validate(SuppGoods suppGoods, Item item,
			OrdOrderItemDTO orderItem, OrdOrderDTO order) {
		logger.info("OrderWifiAddTimePriceServiceImpl.validate start");
		ResultHandle result = new ResultHandle();
		String errorMsg = null;
		if ((orderItem != null) && (item != null)  && (order != null)) {
			
			List<OrdOrderStock> allList = new ArrayList<OrdOrderStock>();
			ProdProduct prodProduct = suppGoods.getProdProduct();
			SuppGoodsRentedLimit suppGoodsRentedLimit = null;
			if(prodProduct!=null){
				Short minRentedDays = null;
				Short maxRentedDays = null;
				String depositFlag = null;
				
				ResultHandleT<SuppGoodsRentedLimit> suppGoodsRentedLimitResult = wifiSuppGoodsClientService.findSuppGoodsRentedLimit(suppGoods.getSuppGoodsId());
		    	if(suppGoodsRentedLimitResult.hasNull()||suppGoodsRentedLimitResult.getReturnContent()==null){
		    		result.setMsg("商品附加信息参数异常");
		    		return result;
				}
		    	suppGoodsRentedLimit = suppGoodsRentedLimitResult.getReturnContent();
				if(suppGoodsRentedLimit!=null){
					
					minRentedDays = suppGoodsRentedLimit.getMinRentedDays();
					maxRentedDays = suppGoodsRentedLimit.getMaxRentedDays();
					depositFlag = suppGoodsRentedLimit.getDepositFlag();
				}
				
				if("Y".equals(depositFlag)){
					
					ResultHandleT<SuppGoodsVO> depositGoodResultHandle = suppGoodsClientService.findDepositSuppGoods(suppGoods.getSuppGoodsId(),item.getVisitTimeDate());
					if(depositGoodResultHandle==null 
						|| depositGoodResultHandle.getReturnContent()==null 
						|| depositGoodResultHandle.getReturnContent().getSuppGoodsNotimeTimePrice()==null
						){
						result.setMsg("商品参数异常");
						LOG.error("关联押金信息异常   商品ID： "+suppGoods.getSuppGoodsId());
						return result;
					}
					 BuyInfo buyInfo = order.getBuyInfo();
					 
					 if(buyInfo!=null&&buyInfo.getItemList()!=null){
						 boolean flag = false;
						 for(Item it :buyInfo.getItemList()){
							 if(!suppGoods.getSuppGoodsId().equals(it.getGoodsId())){
								 ResultHandleT<SuppGoods> suppGoodResult = suppGoodsClientService.findSuppGoodsById(it.getGoodsId());
								 if(suppGoodResult!=null&&suppGoodResult.getReturnContent()!=null){
									 SuppGoods good = suppGoodResult.getReturnContent();
									 if(BizEnum.BIZ_CATEGORY_TYPE.category_other.getCategoryId().equals(good.getCategoryId())){
										 
										 ResultHandleT<ProdProduct> productResult = prodProductClientService.getProdProductBy(good.getProductId());
										 if(productResult!=null && productResult.getReturnContent()!=null){
											 ProdProduct product = productResult.getReturnContent();
											 if(ProdProduct.PRODUCTTYPE.DEPOSIT.name().equals(product.getProductType())){
												 if(!orderItem.getQuantity().equals(Long.valueOf(it.getQuantity()))){
													 result.setMsg("押金份数和商品份数不一致!");
													 flag = true;
													 break;
												 }
											 }
											 
										 }
									 }
									 
								 }
							 }
							 
						 }
						 if(flag){
							 return result;
						 }
						 
					 }
				}
			if(WIFIPRODUCTTYPE.WIFI.name().equals(prodProduct.getProductType())){
				
				logger.info("获取timePrice");		
				//wifi类型的商品每天使用的状况表
				List<OrdOrderWifiTimeRate> ordOrderWifiTimeRateList = new ArrayList<OrdOrderWifiTimeRate>();
				WifiAdditation wifiAdditation = item.getWifiAdditation();
				Date startDate = item.getVisitTimeDate();
				Date endDate = wifiAdditation.getBackTimeDate();
				int days =0;
				if(minRentedDays!=null && maxRentedDays!=null){
					days = DateUtil.getDaysBetween(item.getVisitTimeDate(), item.getWifiAdditation().getBackTimeDate())+1;
					LOG.info("租赁天数："+days);
					if(startDate == null || endDate==null ){
						result.setMsg("日历起止日期参数异常");
						LOG.error("租赁时间日期范围不满足条件,startDate:" + startDate + ", endDate:" + endDate);
						return result;
						
					}
	                 ResultHandleT<SuppGoodsBaseTimePrice> dateTimePrice = getTimePrice(item.getGoodsId(), new Date(), false);
	                 LOG.error("OrderWifiAddTimePriceServiceImpl.validate 商品ID："+item.getGoodsId()+"当天的时间价格信息:" +  GsonUtils.toJson(dateTimePrice));
	                 if((dateTimePrice != null) && dateTimePrice.isSuccess() && (dateTimePrice.getReturnContent() != null)&&!compareDate((Long)dateTimePrice.getReturnContent().getAheadBookTime(),startDate)){
	                   result.setMsg("起始日期不满足提前预定时间！");
                       LOG.error("OrderWifiAddTimePriceServiceImpl.validate 商品ID："+item.getGoodsId()+"提前预定时间aheadBookTime:" + (Long)dateTimePrice.getReturnContent().getAheadBookTime());
                       return result;
	                 }
					//开始于外币项目，存放外币结算等附加信息 开始 at 2018.10.8
					setOrdOrderItemExtendDTOInfo(dateTimePrice.getReturnContent(), suppGoods, orderItem);
					//开始于外币项目，存放外币结算等附加信息 结束 at 2018.10.8
				}else{
					result.setMsg("商品参数异常");
					return result;
				}
				
				int betweenDay = 0;
				for (int i = 0; i < days; i++) {
					Date oneDate = DateUtils.addDays(startDate, i);
					ResultHandleT<SuppGoodsBaseTimePrice> timePriceHolder = getTimePrice(item.getGoodsId(), oneDate, true);
					if ((timePriceHolder != null) && timePriceHolder.isSuccess() && (timePriceHolder.getReturnContent() != null)) {
						SuppGoodsAddTimePrice addTimePrice = (SuppGoodsAddTimePrice)timePriceHolder.getReturnContent();
						List<OrdOrderStock> stockList = new ArrayList<OrdOrderStock>();
						
						//有库存限制
						if("Y".equalsIgnoreCase(addTimePrice.getStockFlag())){
							if(addTimePrice.getStock()<orderItem.getQuantity()){//库存不满足
								if("N".equalsIgnoreCase(addTimePrice.getOversellFlag())){
									continue;
								}else{
									if(addTimePrice.getStock()>0){//存在部分库存
										OrdOrderStock stock = createStock(oneDate, addTimePrice.getStock());
										makeNotNeedResourceConfirm(stock);
										stockList.add(stock);
										order.addUpdateStock(addTimePrice, stock.getQuantity(), this);
									}
									//超卖部分
									OrdOrderStock stock = createStock(oneDate, orderItem.getQuantity()-addTimePrice.getStock());
									makeNeedResourceConfirm(stock);
									stockList.add(stock);
								}
							}else{//库存满足的情况下
								OrdOrderStock stock = createStock(oneDate, orderItem.getQuantity());
								makeNotNeedResourceConfirm(stock);
								stockList.add(stock);
								order.addUpdateStock(addTimePrice, orderItem.getQuantity(), this);
							}
						}else{//支持不限情况下
							OrdOrderStock stock = createStock(oneDate, orderItem.getQuantity());
							makeNotNeedResourceConfirm(stock);
							stock.setInventory(OrderEnum.INVENTORY_STATUS.FREESALE.name());
							stockList.add(stock);
						}
						fillPrice(orderItem,addTimePrice);
						// 酒店类型订单子项中，添加各天使用情况表记录
						OrdOrderWifiTimeRate ordOrderWifiTimeRate = OrderUtils.makeOrdOrderWifiTimeRateRecord(oneDate, orderItem.getQuantity(), addTimePrice.getPrice(), addTimePrice.getSettlementPrice(), 0L,
								null);
						ordOrderWifiTimeRate.setTimePrice(addTimePrice);
						ordOrderWifiTimeRate.setOrderStockList(stockList);
						allList.addAll(stockList);
						makeOrderItemTime(orderItem,addTimePrice);
						ordOrderWifiTimeRateList.add(ordOrderWifiTimeRate);
						betweenDay++;
					}
				}
				if( betweenDay<minRentedDays.intValue() || betweenDay>maxRentedDays.intValue()){
					result.setMsg("不满足最大最小租赁天数条件");
					return result;
				}
				logger.info("OrderWifiAddTimePrice makeNeedResourceConfirm开始");	
				if(allList.size()>0){
					makeNeedResourceConfirm(orderItem, allList);
				}
				orderItem.setOrdOrderWifiTimeRateList(ordOrderWifiTimeRateList);
				orderItem.setOrderStockList(allList);
				
			}else if(WIFIPRODUCTTYPE.PHONE.name().equals(prodProduct.getProductType())){
				
				
				logger.info("OrderWifiAddTimePriceServiceImpl"+prodProduct.getProductType()+"validate start");
				SuppGoodsBaseTimePrice timePrice = getTimePriceAndCheck(suppGoods,item,orderItem.getVisitTime());
				logger.info("获取timePrice");		
				SuppGoodsAddTimePrice addTimePrice = (SuppGoodsAddTimePrice)timePrice;
				List<OrdOrderStock> stockList = new ArrayList<OrdOrderStock>();
				fillPrice(orderItem,addTimePrice);
				//有库存限制
				if("Y".equalsIgnoreCase(addTimePrice.getStockFlag())){
					if(addTimePrice.getStock()<orderItem.getQuantity()){//库存不满足
						if("N".equalsIgnoreCase(addTimePrice.getOversellFlag())){
							throwIllegalException("库存不足");
						}else{
							if(addTimePrice.getStock()>0){//存在部分库存
								OrdOrderStock stock = createStock(orderItem.getVisitTime(), addTimePrice.getStock());
								makeNotNeedResourceConfirm(stock);
								stockList.add(stock);
								order.addUpdateStock(addTimePrice, stock.getQuantity(), this);
							}
							//超卖部分
							OrdOrderStock stock = createStock(orderItem.getVisitTime(), orderItem.getQuantity()-addTimePrice.getStock());
							makeNeedResourceConfirm(stock);
							stockList.add(stock);
						}
					}else{//库存满足的情况下
						OrdOrderStock stock = createStock(orderItem.getVisitTime(), orderItem.getQuantity());
						makeNotNeedResourceConfirm(stock);
						stock.setShareTotalStockId(addTimePrice.getShareTotalStockId());
						stock.setShareDayLimitId(addTimePrice.getShareDayLimitId());
						stockList.add(stock);
						order.addUpdateStock(addTimePrice, orderItem.getQuantity(), this);
					}
				}else{//支持不限情况下
					OrdOrderStock stock = createStock(orderItem.getVisitTime(), orderItem.getQuantity());
					makeNotNeedResourceConfirm(stock);
					stock.setInventory(OrderEnum.INVENTORY_STATUS.FREESALE.name());
					stockList.add(stock);
				}
				logger.info("makeNeedResourceConfirm开始 - "+prodProduct.getProductType());	
				makeNeedResourceConfirm(orderItem, stockList);
				orderItem.setOrderStockList(stockList);

				//开始于外币项目，存放外币结算等附加信息 开始 at 2018.10.8
				setOrdOrderItemExtendDTOInfo(addTimePrice, suppGoods, orderItem);
				//开始于外币项目，存放外币结算等附加信息 结束 at 2018.10.8
				
			}else{
				errorMsg = "产品不可售";
			
			}
			}
		}else{
			errorMsg = "您的订单不存在。";
			
		}
		if (errorMsg != null) {
			result.setMsg(errorMsg);
		}
		fillOrdMulPriceRateListByOrdOrderItem(orderItem);
		 LOG.error("OrderWifiAddTimePriceServiceImpl.validate 商品ID："+item.getGoodsId()+"子订单信息:" + GsonUtils.toJson(orderItem));
		return result;
	}

	private void fillPrice(OrdOrderItem item,SuppGoodsAddTimePrice timePrice){
		//单价
		if (item.getPrice() == null) {
			item.setPrice(timePrice.getPrice());
		} else {
			item.setPrice(item.getPrice() + timePrice.getPrice());
		}
		// 结算单价
		if (item.getSettlementPrice() == null) {
			item.setSettlementPrice(timePrice.getSettlementPrice());
		} else {
			item.setSettlementPrice(item.getSettlementPrice() + timePrice.getSettlementPrice());
		}
		//市场价
		if (item.getMarketPrice() == null) {
			item.setMarketPrice(timePrice.getMarkerPrice());
		} else {
			item.setMarketPrice(item.getMarketPrice() + timePrice.getMarkerPrice());
		}
		makeOrderItemTime(item, timePrice);
		
		List<SuppGoodsRefund> list = suppGoodsClientService.getTicketRefund(item.getSuppGoodsId());
		if(!list.isEmpty()){
			SuppGoodsRefund suppGoodsRefund = list.get(0);
			item.setCancelStrategy(suppGoodsRefund.getCancelStrategy());
		
		}
	}
	

	@Override
	public ResultHandleT<SuppGoodsBaseTimePrice> getTimePrice(Long goodsId,
			Date specDate, boolean checkAhead) {
		SuppGoodsBaseTimePrice timePrice = goodsWifiAddTimePriceStockService.getTimePrice(goodsId, specDate, checkAhead);
		ResultHandleT<SuppGoodsBaseTimePrice> result = new ResultHandleT<SuppGoodsBaseTimePrice>();
		result.setReturnContent(timePrice);
		return result;
	}

	@Override
	public void updateRevertStock(Long suppGoodsId, Date specDate, Long stock,
			Map<String, Object> dataMap) {
		SuppGoodsBaseTimePrice timePrice = goodsWifiAddTimePriceStockService.getTimePrice(suppGoodsId, specDate, false);
		if(timePrice!=null){
			try {
				goodsWifiAddTimePriceStockService.updateStock(timePrice.getTimePriceId(), stock, dataMap);
			} catch (Exception e) {
				logger.error("库存更新发生异常，异常信息：{}", e);
			}
		}
	}

	@Override
	public String getTimePricePrefix() {
		return "WifiAddTimePrice";
	}

	@Override
	public void calcSettlementPromotion(OrdOrderItem orderItem,List<OrdPromotion> promotions) {
		long amount = 0;
		for(OrdPromotion op:promotions){
			FavorableAmount fa = op.getPromotion().getFavorableAmount();
			amount += fa.getAdultAmount();
		}
		long totalPrice = orderItem.getTotalSettlementPrice()-amount;
		if(totalPrice<0){
			totalPrice=0;
		}
		orderItem.setTotalSettlementPrice(totalPrice);
		orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
	}
	
	
	public Boolean compareDate(Long aheadBookTime,Date startDate){
	  Date date = new Date();
	  try {
		  //预定时间(毫秒)+需提前预定时间(分钟*60000=毫秒) < 游玩时间
	       if (date.getTime()+aheadBookTime*60000 < startDate.getTime()) {
	           return true;
	        } else {
	           return false;
	       }
	    } catch (Exception e) {
	      logger.error("wifi日期比较，异常信息：{}", e); 
	    }
	   return false;
	}
	

}
