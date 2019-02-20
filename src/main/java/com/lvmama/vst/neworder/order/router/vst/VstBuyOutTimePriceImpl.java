package com.lvmama.vst.neworder.order.router.vst;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.lvmama.comm.utils.DateUtil;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.po.ResPreControlTimePrice;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.control.vo.ResPreControlTimePriceVO;
import com.lvmama.vst.back.goods.po.PresaleStampTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsNotimeTimePrice;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.neworder.order.EnhanceBeanUtils;
import com.lvmama.vst.neworder.order.router.ILookUpService;
import com.lvmama.vst.neworder.order.router.ITimePriceRouterService;
import com.lvmama.vst.neworder.order.vo.BaseTimePrice;
import com.lvmama.vst.neworder.order.vo.BuyOutTimePrice;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo.GoodsItem;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.timeprice.service.lvf.OrderLvfTimePriceServiceImpl;
import com.lvmama.vst.ticket.utils.DisneyUtils;
@Component("vstBuyOutTimePrice")
public class VstBuyOutTimePriceImpl implements ITimePriceRouterService {

	private static final Logger LOG = LoggerFactory.getLogger(VstBuyOutTimePriceImpl.class);

	@Resource
	ILookUpService lookUpService;

	@Autowired
	protected ResPreControlService resControlBudgetRemote;
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	
	@Autowired
	private SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;

	@Override
	public BaseTimePrice findTimePrice(SuppGoods goods, GoodsItem goodsItem,OrderHotelCombBuyInfo.Item item) {
		BuyOutTimePrice buyOutTimePrice = new BuyOutTimePrice();
	    OrderTimePriceService  orderTimePriceService =lookUpService.lookupTicketTimePrice(goods.getCategoryId());
		ResultHandleT<SuppGoodsBaseTimePrice> timePriceResultHandleT =  orderTimePriceService.getTimePrice(goodsItem.getGoodsId(),goodsItem.getCheckInDate(),true);
	    SuppGoodsBaseTimePrice timePrice = timePriceResultHandleT.getReturnContent();
	     Preconditions.checkArgument(timePriceResultHandleT!=null,"商品%s无可售信息",goodsItem.getGoodsId()+"");
	     Preconditions.checkArgument(timePriceResultHandleT.getReturnContent()!=null,"商品%s无可售信息",goodsItem.getGoodsId()+"");
         SuppGoodsNotimeTimePrice notimeTimePrice=  null;
         SuppGoodsAddTimePrice addTimePrice = null ;
         if(timePrice instanceof SuppGoodsNotimeTimePrice){
        	 notimeTimePrice =  (SuppGoodsNotimeTimePrice)timePrice;
        	 buyOutTimePrice =  this.getSuppGoodsBuyOUtNotimeTimePrice(buyOutTimePrice,notimeTimePrice,goods,item);
         }else if(timePrice instanceof SuppGoodsAddTimePrice){
        	 addTimePrice=(SuppGoodsAddTimePrice)timePrice; 
        	 buyOutTimePrice = this.getSuppGoodsBuyOutAddTimePrice( buyOutTimePrice, addTimePrice, goods,item);
         }
       return buyOutTimePrice;
	}
	// 构建资源对象
	private  OrdOrderStock createStock(Date visitTime,long quantity){
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
	private void makeNotNeedResourceConfirm(final OrdOrderStock stock){
		stock.setNeedResourceConfirm("false");
		stock.setInventory(OrderEnum.INVENTORY_STATUS.INVENTORY.name());
		stock.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.name());
	}

   public  BuyOutTimePrice  getSuppGoodsBuyOUtNotimeTimePrice(BuyOutTimePrice buyOutTimePrice, SuppGoodsNotimeTimePrice notimeTimePrice,SuppGoods goods,OrderHotelCombBuyInfo.Item item){
	   buyOutTimePrice.setGoodsId(notimeTimePrice.getSuppGoodsId());
		 List<ResPreControlTimePriceVO> resPriceList = null;
		 long buyoutTotalPrice = 0;
		 long notBuyoutTotalPrice = 0;
		 Long leftMoney = null;
		 long buyoutNum = 0;
		/** 开始资源预控买断价格  **/
	//	SuppGoods goods = orderItem.getSuppGoods();
		Long goodsId = goods.getSuppGoodsId();
		Date visitDate = DateUtil.stringToDate(item.getVisitTime(),DateUtil.PATTERN_yyyy_MM_dd);
		GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO =new GoodsResPrecontrolPolicyVO();
		boolean hasControled=false;
		//如果是预售券的兑换订单就不走买断
		if(!OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(item.getOrderSubType())){
			//通过商品Id和游玩时间获取，该商品在该时间，所在的预控策略对象
			goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
			//如果能找到该有效预控的资源
			hasControled = goodsResPrecontrolPolicyVO != null && goodsResPrecontrolPolicyVO.isControl();
			LOG.info("vst_order===goodsResPrecontrolPolicyVO==="+ GsonUtils.toJson(goodsResPrecontrolPolicyVO));
		}

		
		if(hasControled ){
			// --ziyuanyukong  通过接口获取该商品在这个时间的价格【参数：成人数，儿童数，商品Id,游玩时间】
			resPriceList = resControlBudgetRemote.queryPreControlTimePriceByParam(visitDate,goods.getCategoryId(), goods.getSuppGoodsId());
			if(resPriceList==null || (resPriceList!=null && resPriceList.size()<=0)){
				hasControled = false;
			}else{
				LOG.info("***资源预控***");
				LOG.info("no time 门票：" + goods.getSuppGoodsId() + "存在预控资源");
			}
			Long prePrice = null;
			Long preSettlePrice = null;
			Long preMarketPrice = null;
			if(resPriceList!=null && resPriceList.size()>0){
				for(int m=0,n=resPriceList.size();m<n;m++){
					ResPreControlTimePrice resTimePrice = resPriceList.get(m);
					if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENTPRICE_PRE.name().equals(resTimePrice.getPriceClassificationCode())){
						preSettlePrice = resTimePrice.getValue();
						notimeTimePrice.setBakPrice(notimeTimePrice.getSettlementPrice());
						notimeTimePrice.setSettlementPrice(preSettlePrice);
					}
					if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_PRE.name().equals(resTimePrice.getPriceClassificationCode())){
						prePrice = resTimePrice.getValue();
						notimeTimePrice.setPrice(prePrice);
					}
					if(OrderEnum.ORDER_PRICE_RATE_TYPE.MARKERPRICE_PRE.name().equals(resTimePrice.getPriceClassificationCode())){
						preMarketPrice = resTimePrice.getValue();
						notimeTimePrice.setMarkerPrice(preMarketPrice);
					}
				}
			}
			
		}
		
		//如果是券
		if(OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(item.getOrderSubType())){
			Map<String,Object> map =new HashMap<String, Object>();
			map.put("goodsId", goodsId);
			map.put("applyDate", visitDate);
			//Long settlePrice=suppGoodsTimePriceClientService.getGoodsSettlePrice(map).getReturnContent();
			List<PresaleStampTimePrice> settlePrice=suppGoodsTimePriceClientService.selectPresaleStampTimePrices(map);
			notimeTimePrice.setSettlementPrice(settlePrice.get(0).getValue());
			//notimeTimePrice.setPrice(Long.valueOf(item.getPrice()));
		}
		
		/** 结束  **/
		
		List<OrdOrderStock> orderStockList = new ArrayList<OrdOrderStock>();
		
		OrdOrderStock stock = createStock(notimeTimePrice.getEndDate(), item.getQuantity());
		
		makeNotNeedResourceConfirm(stock);
		orderStockList.add(stock);
		
	//	setOrderItemsNeedResourceConfirm(stock.getNeedResourceConfirm(), orderItem);
		//设置提前预订时间
		if(ProductCategoryUtil.isTicket(goods.getBizCategory().getCategoryCode())){
			buyOutTimePrice.setAheadTime(notimeTimePrice.getEndDate());
		}else{
			if(notimeTimePrice.getAheadBookTime()!=null){
				//throwNullException("提前预订时间为空");
				buyOutTimePrice.setAheadTime((DateUtils.addMinutes(visitDate, -notimeTimePrice.getAheadBookTime().intValue())));
			}
		}
		
		buyOutTimePrice.setOrderStockList(orderStockList);
		buyOutTimePrice.setSettmentPrice(notimeTimePrice.getSettlementPrice());
		//orderItem.setActualSettlementPrice(notimeTimePrice.getSettlementPrice());
		buyOutTimePrice.setSalePrice(notimeTimePrice.getPrice());
		buyOutTimePrice.setMarkerPrice(notimeTimePrice.getMarkerPrice());

//      //只有期票打包需要重新计算价格，别的不需要。
//      if(orderItem.getOrderPack()!=null&&(goods.getCategoryId()== BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId()||goods.getCategoryId()== BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId()||orderItem.getCategoryId()== BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId())){
//          //门票自主打包才会出现直接打包到商品
//          ProdPackageDetail detail = OrderUtils.getProdPackageDetail((OrdOrderPackDTO)orderItem.getOrderPack(), orderItem.getSuppGoodsId());
//          if(detail==null){
//              if (LOG.isDebugEnabled()) {
//                  LOG.debug("期票打包被打包的产品数据不存在"); //$NON-NLS-1$
//              }
//              throwNullException("被打包的产品数据不存在");
//          }
//          orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.ticket_pack_quantity.name(), detail.getPackageCount());
//          orderItem.setQuantity(orderItem.getQuantity()*detail.getPackageCount());
//          fillPackageOrderItemPrice(orderItem, detail);
//      }
//		
		
		if(hasControled){
			

			

			String preControlType = goodsResPrecontrolPolicyVO.getControlType();
			if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equals(preControlType)){
				//记录买断和非买断的结算总额
				if(leftMoney==null ){
					leftMoney = goodsResPrecontrolPolicyVO.getLeftAmount().longValue() ;
				}
				long shouldSettleTotalPrice = item.getQuantity()*notimeTimePrice.getSettlementPrice();
				if(shouldSettleTotalPrice>leftMoney&& leftMoney>0){
					buyoutNum = (long) Math.ceil(leftMoney/Double.valueOf(item.getSettlementPrice()));
					//买断+非买断
					buyoutTotalPrice = buyoutTotalPrice + buyoutNum * notimeTimePrice.getSettlementPrice();
					long notBuyNum = (item.getQuantity() - buyoutNum);
					if(notBuyNum>0){
						notBuyoutTotalPrice = notBuyoutTotalPrice + notBuyNum * notimeTimePrice.getBakPrice();
					}
				}else if(shouldSettleTotalPrice<=leftMoney){
					//买断
					buyoutTotalPrice = buyoutTotalPrice + shouldSettleTotalPrice;
					buyoutNum = item.getQuantity();
				}
				buyOutTimePrice.setBuyoutQuantity(buyoutNum);
				buyOutTimePrice.setBuyoutTotalPrice(buyoutTotalPrice);
				buyOutTimePrice.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
				leftMoney = leftMoney - shouldSettleTotalPrice;
				buyOutTimePrice.setBuyoutPrice((long)buyOutTimePrice.getBuyoutTotalPrice()/buyOutTimePrice.getBuyoutQuantity());
				
			}else if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.inventory.name().equals(preControlType)){
				//记录买断的库存，以及各自的结算总额
				long roomNum = 0;
				if(item.getQuantity()!=0 ){
					roomNum = item.getQuantity();
				}
				long leftQuantity = 0;
				if(goodsResPrecontrolPolicyVO.getLeftNum()!=null){
					leftQuantity = goodsResPrecontrolPolicyVO.getLeftNum().longValue();
				}
				long buyoutsaledNum = 0;
				if(buyOutTimePrice.getBuyoutQuantity()!=null ){
					buyoutsaledNum = buyOutTimePrice.getBuyoutQuantity().longValue();
				}
				if(roomNum>leftQuantity){
					buyOutTimePrice.setBuyoutQuantity(buyoutsaledNum + leftQuantity);
					buyoutTotalPrice = buyoutTotalPrice + leftQuantity*notimeTimePrice.getSettlementPrice();
					notBuyoutTotalPrice = notBuyoutTotalPrice + (notimeTimePrice.getBakPrice() * (roomNum-leftQuantity));
					//酒店设置非买断的总价
					buyOutTimePrice.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
					//设置买断的总价
					buyOutTimePrice.setBuyoutTotalPrice(buyoutTotalPrice);
				}else{
					buyOutTimePrice.setBuyoutQuantity(buyoutsaledNum + roomNum);
					buyoutTotalPrice = buyoutTotalPrice + roomNum*notimeTimePrice.getSettlementPrice();
					buyOutTimePrice.setBuyoutTotalPrice(buyoutTotalPrice);
				}
				
				buyOutTimePrice.setBuyoutPrice((long)buyOutTimePrice.getBuyoutTotalPrice()/buyOutTimePrice.getBuyoutQuantity());
			}
			buyOutTimePrice.setBuyoutFlag("Y");
		    buyOutTimePrice.setBuyoutPrice(notimeTimePrice.getSettlementPrice());
			long leftQuantity = goodsResPrecontrolPolicyVO.getLeftNum();
			if(item.getQuantity()>leftQuantity){
				buyOutTimePrice.setBuyoutQuantity(leftQuantity);
				//酒店设置买断的总价
				buyOutTimePrice.setNotBuyoutSettleAmout(notimeTimePrice.getBakPrice() * (item.getQuantity()-leftQuantity));
			}else{
				buyOutTimePrice.setBuyoutQuantity((long)item.getQuantity());
			}
			buyOutTimePrice.setBuyoutTotalPrice(buyOutTimePrice.getBuyoutQuantity() * buyOutTimePrice.getBuyoutPrice());
			buyOutTimePrice.setBuyoutFlag("Y");
			buyOutTimePrice.setNebulaProjectId(goodsResPrecontrolPolicyVO.getNebulaProjectId());
		}		
		//需要(提取出去)统一处理退改
		
	//	OrderTimePriceUtils.setTicketRefund(orderItem, suppGoodsClientService);
		
		buyOutTimePrice.setStockFlag(notimeTimePrice.getStockFlag());
		return buyOutTimePrice;
   }
   
  public BuyOutTimePrice getSuppGoodsBuyOutAddTimePrice(BuyOutTimePrice buyOutTimePrice, SuppGoodsAddTimePrice addTimePrice,SuppGoods goods,OrderHotelCombBuyInfo.Item item){

     
		List<ResPreControlTimePriceVO> resPriceList = null;
		long buyoutTotalPrice = 0;
		long notBuyoutTotalPrice = 0;
		Long leftMoney = null;
		long buyoutNum = 0;
		/** 开始资源预控买断价格  **/
	//	SuppGoods goods = orderItem.getSuppGoods();
		Long goodsId = goods.getSuppGoodsId();
		Date visitDate = DateUtil.stringToDate(item.getVisitTime(),DateUtil.PATTERN_yyyy_MM_dd);
		GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO =new GoodsResPrecontrolPolicyVO();
		boolean hasControled=false;
		String overbuy ="N";
	    
		//如果是预售券的兑换订单就不走买断
		if(!OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(item.getOrderSubType())){
			//通过商品Id和游玩时间获取，该商品在该时间，所在的预控策略对象
			goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
			//如果能找到该有效预控的资源
			hasControled = goodsResPrecontrolPolicyVO != null && goodsResPrecontrolPolicyVO.isControl();
			overbuy = goodsResPrecontrolPolicyVO == null ? "N":goodsResPrecontrolPolicyVO.getIsCanDelay();
			LOG.info("vst_order===goodsResPrecontrolPolicyVO==="+ GsonUtils.toJson(goodsResPrecontrolPolicyVO));
		}
		
		/*Map<String, Long> map = order.getBuyoutMap();
		String k = "";
		Long usedQuantity = -1L;
		if(map.size()>0 && hasControled){
			if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equals(goodsResPrecontrolPolicyVO.getControlType())){
				k = "amount_" + goodsResPrecontrolPolicyVO.getAmountId();
				usedQuantity = map.get(k);
				if(usedQuantity == null){
					usedQuantity = 0L;
				}
				hasControled = hasControled && goodsResPrecontrolPolicyVO.getLeftAmount() > usedQuantity;
				if(hasControled){
					goodsResPrecontrolPolicyVO.setLeftAmount(goodsResPrecontrolPolicyVO.getLeftAmount() - usedQuantity);
				}
			}else{
				k = "inventory_" + goodsResPrecontrolPolicyVO.getStoreId();
				usedQuantity = map.get(k);
				if(usedQuantity == null){
					usedQuantity = 0L;
				}
				hasControled = hasControled && goodsResPrecontrolPolicyVO.getLeftNum() > usedQuantity;
				if(hasControled){
					goodsResPrecontrolPolicyVO.setLeftNum(goodsResPrecontrolPolicyVO.getLeftNum() - usedQuantity);
				}
			}
		}*/
		
		
	
		
		
		if(hasControled ){
			// --ziyuanyukong  通过接口获取该商品在这个时间的价格【参数：成人数，儿童数，商品Id,游玩时间】
			resPriceList = resControlBudgetRemote.queryPreControlTimePriceByParam(visitDate,goods.getCategoryId(), goods.getSuppGoodsId());
			if(resPriceList==null || (resPriceList!=null && resPriceList.size()<=0)){
				hasControled = false;
			}else{
				LOG.info("***资源预控***");
				LOG.info("add time 门票：" + goods.getSuppGoodsId() + "存在预控资源");
			}
			Long prePrice = null;
			Long preSettlePrice = null;
			Long preMarketPrice = null;
			if(resPriceList!=null && resPriceList.size()>0){
				for(int m=0,n=resPriceList.size();m<n;m++){
					ResPreControlTimePrice resTimePrice = resPriceList.get(m);
					if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENTPRICE_PRE.name().equals(resTimePrice.getPriceClassificationCode())){
						preSettlePrice = resTimePrice.getValue();
						addTimePrice.setBakPrice(addTimePrice.getSettlementPrice());
						addTimePrice.setSettlementPrice(preSettlePrice);
					}
					if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_PRE.name().equals(resTimePrice.getPriceClassificationCode())){
						prePrice = resTimePrice.getValue();
						addTimePrice.setPrice(prePrice);
					}
					if(OrderEnum.ORDER_PRICE_RATE_TYPE.MARKERPRICE_PRE.name().equals(resTimePrice.getPriceClassificationCode())){
						preMarketPrice = resTimePrice.getValue();
						addTimePrice.setMarkerPrice(preMarketPrice);
					}
				}
			}
		}
//		/** 结束  **/
//
//		Long productId = null;
//		try {
//			productId = order.getBuyInfo().getProductId();
//		} catch (Exception e) {
//			LOG.error("Error get product id while validate price for goods " + suppGoods.getSuppGoodsId());
//		}

		//如果是 交通+X 品类下单
//		if(order!=null && order.getCategoryId()!=null && OrderLvfTimePriceServiceImpl.isAutoPackCategory(order.getCategoryId())){
//			fillPrice_autoPack(orderItem, addTimePrice);
//			//如果是自动打包产品的订单，且子订单的品类不是交通，走跟 交通+X 品类一样的逻辑
//		} else if (orderBookServiceDataUtil.isAutoPackTrafficProduct(productId) && !orderBookServiceDataUtil.isTrafficGoods(suppGoods.getCategoryId())){
//			fillPrice_autoPack(orderItem, addTimePrice);
//		}
//		else {
			//fillPrice(orderItem, addTimePrice);
	//	}

		buyOutTimePrice.setSettmentPrice(addTimePrice.getSettlementPrice());
        buyOutTimePrice.setSalePrice(addTimePrice.getPrice());
		buyOutTimePrice.setMarkerPrice(addTimePrice.getMarkerPrice());

		//如果是券
		if(OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(item.getOrderSubType())){
			Map<String,Object> map =new HashMap<String, Object>();
			map.put("goodsId", goodsId);
			map.put("applyDate", visitDate);
			//Long settlePrice=suppGoodsTimePriceClientService.getGoodsSettlePrice(map).getReturnContent();
			List<PresaleStampTimePrice> settlePrePrice=suppGoodsTimePriceClientService.selectPresaleStampTimePrices(map);
			
			//addTimePrice.setSettlementPrice(settlePrePrice.get(0).getValue());
			addTimePrice.setSettlementPrice(settlePrePrice.get(0).getValue());
         //addTimePrice.setPrice(Long.valueOf(item.getPrice()));
		}
		if(hasControled){
			/*Map<String, Long> buyoutMap = order.getBuyoutMap();
			String key = "";*/
			String preControlType = goodsResPrecontrolPolicyVO.getControlType();
			if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equals(preControlType)){
				
				/*key = "amount_"+goodsResPrecontrolPolicyVO.getAmountId();
				Long usedAmount = 0L; 
				if(buyoutMap.size()>0){
					usedAmount = buyoutMap.get(key);
					if(usedAmount ==null){
						usedAmount = 0L;
					}
				}*/
				
				//记录买断和非买断的结算总额
				if(leftMoney==null ){
					leftMoney = goodsResPrecontrolPolicyVO.getLeftAmount().longValue() ;
				}
				long shouldSettleTotalPrice = item.getQuantity()*addTimePrice.getSettlementPrice();
				if(shouldSettleTotalPrice>leftMoney&& leftMoney>0&&"N".equalsIgnoreCase(overbuy)){
					buyoutNum = (long) Math.ceil(leftMoney/addTimePrice.getSettlementPrice().doubleValue());
					//买断+非买断
					buyoutTotalPrice = buyoutTotalPrice + buyoutNum * addTimePrice.getSettlementPrice();
					long notBuyNum = (item.getQuantity() - buyoutNum);
					if(notBuyNum>0){
						notBuyoutTotalPrice = notBuyoutTotalPrice + notBuyNum * addTimePrice.getBakPrice();
					}
				}else if(shouldSettleTotalPrice<=leftMoney||"Y".equalsIgnoreCase(overbuy)){
					//买断
					buyoutTotalPrice = buyoutTotalPrice + shouldSettleTotalPrice;
					buyoutNum = item.getQuantity();
				}
				buyOutTimePrice.setBuyoutQuantity(buyoutNum);
				buyOutTimePrice.setBuyoutTotalPrice(buyoutTotalPrice);
				buyOutTimePrice.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
				leftMoney = leftMoney - shouldSettleTotalPrice;
				buyOutTimePrice.setBuyoutPrice((long)buyOutTimePrice.getBuyoutTotalPrice()/buyOutTimePrice.getBuyoutQuantity());
				
				/*buyoutMap.put(key, usedAmount + orderItem.getBuyoutTotalPrice());*/
				
			}else if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.inventory.name().equals(preControlType)){
				/*key = "inventory_"+goodsResPrecontrolPolicyVO.getStoreId();
				Long usedAmount = 0L; 
				if(buyoutMap.size()>0){
					usedAmount = buyoutMap.get(key);
					if(usedAmount ==null){
						usedAmount = 0L;
					}
				}*/
				
				
				//记录买断的库存，以及各自的结算总额
				long roomNum = 0;
				if(item.getQuantity()!=0 ){
					roomNum = item.getQuantity();
				}
				long leftQuantity = 0;
				if(goodsResPrecontrolPolicyVO.getLeftNum()!=null){
					leftQuantity = goodsResPrecontrolPolicyVO.getLeftNum().longValue();
				}
				long buyoutsaledNum = 0;
				if(buyOutTimePrice.getBuyoutQuantity()!=null ){
					buyoutsaledNum = buyOutTimePrice.getBuyoutQuantity().longValue();
				}
				if(roomNum>leftQuantity&&"N".equalsIgnoreCase(overbuy)){
					buyOutTimePrice.setBuyoutQuantity(buyoutsaledNum + leftQuantity);
					buyoutTotalPrice = buyoutTotalPrice + leftQuantity*addTimePrice.getSettlementPrice();
					notBuyoutTotalPrice = notBuyoutTotalPrice + (addTimePrice.getBakPrice() * (roomNum-leftQuantity));
					//酒店设置非买断的总价
					buyOutTimePrice.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
					//设置买断的总价
					buyOutTimePrice.setBuyoutTotalPrice(buyoutTotalPrice);
				}else{
					buyOutTimePrice.setBuyoutQuantity(buyoutsaledNum + roomNum);
					buyoutTotalPrice = buyoutTotalPrice + roomNum*addTimePrice.getSettlementPrice();
					buyOutTimePrice.setBuyoutTotalPrice(buyoutTotalPrice);
				}
				
				buyOutTimePrice.setBuyoutPrice((long)buyOutTimePrice.getBuyoutTotalPrice()/buyOutTimePrice.getBuyoutQuantity());
				
				/*buyoutMap.put(key, usedAmount + orderItem.getBuyoutQuantity());*/
			}
			buyOutTimePrice.setBuyoutFlag("Y");
		
			buyOutTimePrice.setShareDayLimitId(addTimePrice.getShareDayLimitId());
			buyOutTimePrice.setShareTotalStockId(addTimePrice.getShareTotalStockId());
			
			/*order.setBuyoutMap(buyoutMap);*/
			buyOutTimePrice.setNebulaProjectId(goodsResPrecontrolPolicyVO.getNebulaProjectId());
		}
	  
	  return buyOutTimePrice;
  }
 }
