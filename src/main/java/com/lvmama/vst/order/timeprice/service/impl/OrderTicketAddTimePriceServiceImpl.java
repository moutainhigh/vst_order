/**
 * 
 */
package com.lvmama.vst.order.timeprice.service.impl;

import com.lvmama.price.api.strategy.model.vo.SuppGoodsAddTimePriceVo;
import com.lvmama.price.api.strategy.service.SuppGoodsAddTimePriceApiService;
import com.lvmama.price.api.strategy.service.SuppGoodsNotimeTimePriceApiService;
import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.client.pub.service.ComPushClientService;
import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.po.ResPreControlTimePrice;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.control.vo.ResPreControlTimePriceVO;
import com.lvmama.vst.back.goods.po.PresaleStampTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsBaseTimePriceStockService;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageDetailAddPrice;
import com.lvmama.vst.back.prom.rule.favor.FavorableAmount;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.order.service.book.util.OrderBookServiceDataUtil;
import com.lvmama.vst.order.timeprice.service.AbstractOrderTimePriceService;
import com.lvmama.vst.order.timeprice.service.lvf.OrderLvfTimePriceServiceImpl;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;
import com.lvmama.vst.ticket.utils.DisneyUtils;
import net.sf.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 普通的门票处理
 * @author lancey
 *
 */
@Component("orderTicketAddTimePriceService")
public class OrderTicketAddTimePriceServiceImpl extends AbstractOrderTimePriceService{
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(OrderTicketAddTimePriceServiceImpl.class);

	@Autowired
	private DistGoodsTimePriceClientService distGoodsTimePriceClientService;
	
	@Resource(name="goodsOraTicketAddTimePriceStockService")
	private IGoodsTimePriceStockService goodsTicketAddTimePriceStockService;
	@Autowired
	protected ResPreControlService resControlBudgetRemote;
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	@Autowired
	private SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;
	@Resource(name="orderBookServiceDataUtil")
	private OrderBookServiceDataUtil orderBookServiceDataUtil;
	@Autowired
	private SuppGoodsAddTimePriceApiService suppGoodsAddTimePriceApiServiceRemote;
	@Autowired
	private SuppGoodsNotimeTimePriceApiService suppGoodsNotimeTimePriceApiServiceRemote;
	@Autowired(required=false)
	protected ComPushClientService comPushServiceRemote;
	@Autowired
	private IGoodsBaseTimePriceStockService goodsBaseTimePriceStockServiceImpl;
	@Override
	public ResultHandleT<Object> checkStock(SuppGoods suppGoods, Item item,
			Long distributionId, Map<String, Object> dataMap) {
		ResultHandleT<Object> result = new ResultHandleT<Object>();
		try{
			SuppGoodsBaseTimePrice timePrice = getTimePriceAndCheck(suppGoods,item,item.getVisitTimeDate());
			SuppGoodsAddTimePrice addTimePrice = (SuppGoodsAddTimePrice)timePrice;
			LOG.info("shareTotalStock:" + item.getShareTotalStock()
					+ ", shareDayLimit:" + item.getShareDayLimit()
					+ ", 正在进行库存检查，该商品的价格类型为TicketAddTimePrice,得到的时间数据为 \n"
					+ JSONArray.fromObject(timePrice));
			
			//需要校验日库存/共享总库存/共享日限制
			if("Y".equalsIgnoreCase(addTimePrice.getStockFlag())){
				if (addTimePrice.getStock() < item.getQuantity()
						|| ((item.getCheckStockQuantity() != null && item
								.getCheckStockQuantity() > 0) && addTimePrice
								.getStock() < item.getCheckStockQuantity())
						|| addTimePrice.getShareTotalStock() < item
								.getShareTotalStock()
						|| addTimePrice.getShareDayLimit() < item
								.getShareDayLimit()) {// 库存不满足
					if("N".equalsIgnoreCase(addTimePrice.getOversellFlag())){
						throwIllegalException("库存不足");
					}
				}
			}
			
			if ("Y".equals(suppGoods.getStockApiFlag())) {
				com.lvmama.vst.comm.vo.SupplierProductInfo.Item supplierItem = new com.lvmama.vst.comm.vo.SupplierProductInfo.Item(suppGoods.getSuppGoodsId(), item.getVisitTimeDate());
				supplierItem.setQuantity((long)item.getQuantity());
				result.setReturnContent(supplierItem);
			}
			
		}catch(IllegalArgumentException ex){
			result.setMsg(ex);
		}
		return result;
	}

	@Override
	public void updateStock(Long timePriceId, Long stock,
			Map<String, Object> dataMap) {
		try {
			if(!goodsTicketAddTimePriceStockService.updateStock(timePriceId, -stock, dataMap)){
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
		logger.info("OrderTicketAddTimePriceServiceImpl.validate start");
		ResultHandle result = new ResultHandle();
        SuppGoodsBaseTimePrice timePrice = getTimePriceAndCheck(suppGoods,item,orderItem.getVisitTime());
		logger.info("获取timePrice");
		List<OrdOrderStock> stockList = new ArrayList<OrdOrderStock>();
		SuppGoodsAddTimePrice addTimePrice = (SuppGoodsAddTimePrice)timePrice;

        //如果是迪士尼剧场票，销售价和结算价从item里获取
        if(DisneyUtils.isDisneyShow(suppGoods)){
            if(StringUtils.isNotEmpty(item.getPrice()) && StringUtils.isNotEmpty(item.getSettlementPrice())){
                addTimePrice.setPrice(Long.valueOf(item.getPrice()));
                addTimePrice.setSettlementPrice(Long.valueOf(item.getSettlementPrice()));
                addTimePrice.setMarkerPrice(Long.valueOf(item.getPrice()));
            }
        }

		List<ResPreControlTimePriceVO> resPriceList = null;
		long buyoutTotalPrice = 0;
		long notBuyoutTotalPrice = 0;
		Long leftMoney = null;
		long buyoutNum = 0;
		/** 开始资源预控买断价格  **/
		SuppGoods goods = orderItem.getSuppGoods();
		Long goodsId = goods.getSuppGoodsId();
		Date visitDate = orderItem.getVisitTime();
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
			resPriceList = resControlBudgetRemote.queryPreControlTimePriceByParam(orderItem.getVisitTime(),orderItem.getCategoryId(), orderItem.getSuppGoodsId());
			if(resPriceList==null || (resPriceList!=null && resPriceList.size()<=0)){
				hasControled = false;
			}else{
				LOG.info("***资源预控***");
				LOG.info("add time 门票：" + orderItem.getSuppGoodsId() + "存在预控资源");
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
		/** 结束  **/

		Long productId = null;
		try {
			productId = order.getBuyInfo().getProductId();
		} catch (Exception e) {
			logger.error("Error get product id while validate price for goods " + suppGoods.getSuppGoodsId());
		}

		//如果是 交通+X 品类下单
		if(order!=null && order.getCategoryId()!=null && OrderLvfTimePriceServiceImpl.isAutoPackCategory(order.getCategoryId())){
			fillPrice_autoPack(orderItem, addTimePrice);
			//如果是自动打包产品的订单，且子订单的品类不是交通，走跟 交通+X 品类一样的逻辑
		} else if (orderBookServiceDataUtil.isAutoPackTrafficProduct(productId) && !orderBookServiceDataUtil.isTrafficGoods(suppGoods.getCategoryId())){
			fillPrice_autoPack(orderItem, addTimePrice);
		}
		else {
			fillPrice(orderItem, addTimePrice);
		}
		//如果是券
		if(OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(item.getOrderSubType())){
			Map<String,Object> map =new HashMap<String, Object>();
			map.put("goodsId", goodsId);
			map.put("applyDate", visitDate);
			//Long settlePrice=suppGoodsTimePriceClientService.getGoodsSettlePrice(map).getReturnContent();
			List<PresaleStampTimePrice> settlePrePrice=suppGoodsTimePriceClientService.selectPresaleStampTimePrices(map);
			
			//addTimePrice.setSettlementPrice(settlePrePrice.get(0).getValue());
			orderItem.setSettlementPrice(settlePrePrice.get(0).getValue());
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
				long shouldSettleTotalPrice = orderItem.getQuantity()*addTimePrice.getSettlementPrice();
				if(shouldSettleTotalPrice>leftMoney&& leftMoney>0&&"N".equalsIgnoreCase(overbuy)){
					buyoutNum = (long) Math.ceil(leftMoney/orderItem.getSettlementPrice().doubleValue());
					//买断+非买断
					buyoutTotalPrice = buyoutTotalPrice + buyoutNum * addTimePrice.getSettlementPrice();
					long notBuyNum = (orderItem.getQuantity() - buyoutNum);
					if(notBuyNum>0){
						notBuyoutTotalPrice = notBuyoutTotalPrice + notBuyNum * addTimePrice.getBakPrice();
					}
				}else if(shouldSettleTotalPrice<=leftMoney||"Y".equalsIgnoreCase(overbuy)){
					//买断
					buyoutTotalPrice = buyoutTotalPrice + shouldSettleTotalPrice;
					buyoutNum = orderItem.getQuantity();
				}
				orderItem.setBuyoutQuantity(buyoutNum);
				orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
				orderItem.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
				leftMoney = leftMoney - shouldSettleTotalPrice;
				orderItem.setBuyoutPrice((long)orderItem.getBuyoutTotalPrice()/orderItem.getBuyoutQuantity());
				
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
				if(orderItem.getQuantity()!=null ){
					roomNum = orderItem.getQuantity().longValue();
				}
				long leftQuantity = 0;
				if(goodsResPrecontrolPolicyVO.getLeftNum()!=null){
					leftQuantity = goodsResPrecontrolPolicyVO.getLeftNum().longValue();
				}
				long buyoutsaledNum = 0;
				if(orderItem.getBuyoutQuantity()!=null ){
					buyoutsaledNum = orderItem.getBuyoutQuantity().longValue();
				}
				if(roomNum>leftQuantity&&"N".equalsIgnoreCase(overbuy)){
					orderItem.setBuyoutQuantity(buyoutsaledNum + leftQuantity);
					buyoutTotalPrice = buyoutTotalPrice + leftQuantity*addTimePrice.getSettlementPrice();
					notBuyoutTotalPrice = notBuyoutTotalPrice + (addTimePrice.getBakPrice() * (roomNum-leftQuantity));
					//酒店设置非买断的总价
					orderItem.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
					//设置买断的总价
					orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
				}else{
					orderItem.setBuyoutQuantity(buyoutsaledNum + roomNum);
					buyoutTotalPrice = buyoutTotalPrice + roomNum*addTimePrice.getSettlementPrice();
					orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
				}
				
				orderItem.setBuyoutPrice((long)orderItem.getBuyoutTotalPrice()/orderItem.getBuyoutQuantity());
				
				/*buyoutMap.put(key, usedAmount + orderItem.getBuyoutQuantity());*/
			}
			orderItem.setBuyoutFlag("Y");
			
			
			
			/*order.setBuyoutMap(buyoutMap);*/
			orderItem.setNebulaProjectId(goodsResPrecontrolPolicyVO.getNebulaProjectId());
		}
		
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

		//开始于外币项目，存放外币结算等附加信息 开始 at 2018.10.8
		setOrdOrderItemExtendDTOInfo(addTimePrice, suppGoods, orderItem);
		//开始于外币项目，存放外币结算等附加信息 结束 at 2018.10.8

		logger.info("makeNeedResourceConfirm开始");	
		makeNeedResourceConfirm(orderItem, stockList);
		orderItem.setOrderStockList(stockList);
		fillOrdMulPriceRateListByOrdOrderItem(orderItem);
		return result;
	}

	private void fillPrice(OrdOrderItem item,SuppGoodsAddTimePrice timePrice){
		item.setPrice(timePrice.getPrice());
		item.setSettlementPrice(timePrice.getSettlementPrice());
		item.setMarketPrice(timePrice.getMarkerPrice());
		//表示是打包的产品
		if(item.getOrderPack()!=null){
			OrdOrderItemDTO orderItemDTO = (OrdOrderItemDTO)item;
			ProdPackageDetail detail = null;
			ProdPackageDetailAddPrice detailAddPrice = null;
			if(orderItemDTO.getItem().getDetailId()!=null){
				detailAddPrice = OrderUtils.getProdPackageDetailAddPriceByDetailId((OrdOrderPackDTO)item.getOrderPack(), orderItemDTO.getItem().getDetailId(), item.getVisitTime());
				if(detailAddPrice == null) {
					detail = OrderUtils.getProdPackageDetailByDetailId((OrdOrderPackDTO)item.getOrderPack(), orderItemDTO.getItem().getDetailId());
					if(detail==null){
						if (logger.isDebugEnabled()) {
							logger.debug("fillPrice(OrdOrderItem, SuppGoodsAddTimePrice) - detail == null"); //$NON-NLS-1$
						}
						throwNullException("被打包的产品数据不存在");
					}
				}
			}else{//门票自主打包才会出现直接打包到商品
				detail = OrderUtils.getProdPackageDetail((OrdOrderPackDTO)item.getOrderPack(), item.getSuppGoodsId());
				if(detail==null){
					if (logger.isDebugEnabled()) {
						logger.debug("fillPrice(OrdOrderItem, SuppGoodsAddTimePrice) - detail == null"); //$NON-NLS-1$
					}
					throwNullException("被打包的产品数据不存在");
				}
				item.putContent(OrderEnum.ORDER_TICKET_TYPE.ticket_pack_quantity.name(), detail.getPackageCount());
				item.setQuantity(item.getQuantity()*detail.getPackageCount());
			}
			if(detailAddPrice != null) {
				fillPackageOrderItemPrice(item, detailAddPrice);
			} else {
				fillPackageOrderItemPrice(item, detail);
			}
		}
		makeOrderItemTime(item, timePrice);
		OrderTimePriceUtils.setTicketRefund(item, suppGoodsClientService);
        OrderTimePriceUtils.setTicketReschedule(item,suppGoodsClientService);
	}

	/**
	 * 交通+X 品类下单时，价格填充逻辑
	 * @param item
	 * @param timePrice
	 */
	private void fillPrice_autoPack( OrdOrderItem item,SuppGoodsAddTimePrice timePrice){
		item.setPrice(timePrice.getPrice());
		item.setSettlementPrice(timePrice.getSettlementPrice());
		item.setMarketPrice(timePrice.getMarkerPrice());
		item.putContent(OrderEnum.ORDER_TICKET_TYPE.ticket_pack_quantity.name(), 1);
		item.setQuantity(item.getQuantity() * 1);
//		item.setPrice(item.getPrice());
		makeOrderItemTime(item, timePrice);
		OrderTimePriceUtils.setTicketRefund(item, suppGoodsClientService);
        OrderTimePriceUtils.setTicketReschedule(item,suppGoodsClientService);
	}

	@Override
	public ResultHandleT<SuppGoodsBaseTimePrice> getTimePrice(Long goodsId,
			Date specDate, boolean checkAhead) {
		SuppGoodsBaseTimePrice timePrice = goodsTicketAddTimePriceStockService.getTimePrice(goodsId, specDate, checkAhead);
		ResultHandleT<SuppGoodsBaseTimePrice> result = new ResultHandleT<SuppGoodsBaseTimePrice>();
		result.setReturnContent(timePrice);
		return result;
	}

	@Override
	public void updateRevertStock(Long suppGoodsId, Date specDate, Long stock,
			Map<String, Object> dataMap) {
		SuppGoodsAddTimePriceVo timePrice = suppGoodsAddTimePriceApiServiceRemote.getBaseTimePrice(suppGoodsId, specDate, false).getReturnContent();
		if(timePrice!=null){
			try {
				goodsTicketAddTimePriceStockService.updateStock(timePrice.getTimePriceId(), stock, dataMap);
			} catch (Exception e) {
				logger.error("库存更新发生异常，异常信息：{}", e);
			}
		}
	}

	@Override
	public String getTimePricePrefix() {
		return "TicketAddTimePrice";
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

}
