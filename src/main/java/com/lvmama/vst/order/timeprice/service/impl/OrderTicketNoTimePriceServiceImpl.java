/**
 * 
 */
package com.lvmama.vst.order.timeprice.service.impl;

import com.lvmama.price.api.strategy.model.vo.SuppGoodsAddTimePriceVo;
import com.lvmama.price.api.strategy.model.vo.SuppGoodsNotimeTimePriceVo;
import com.lvmama.price.api.strategy.service.SuppGoodsAddTimePriceApiService;
import com.lvmama.price.api.strategy.service.SuppGoodsNotimeTimePriceApiService;
import com.lvmama.vst.back.biz.po.BizEnum;
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
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsNotimeTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsBaseTimePriceStockService;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prom.rule.favor.FavorableAmount;
import com.lvmama.vst.back.pub.po.ComIncreament;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.order.timeprice.service.AbstractOrderTimePriceService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 
 * 期票与实体票的时间价格表操作
 * @author lancey
 *
 */
@Component("orderTicketNoTimePriceService")
public class OrderTicketNoTimePriceServiceImpl extends AbstractOrderTimePriceService{

	@Resource(name="goodsOraTicketNotimeTimePriceStockService")
	private IGoodsTimePriceStockService goodsTicketNoTimePriceStockService;
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	@Autowired
	protected ResPreControlService resControlBudgetRemote;
	@Autowired
	private SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;
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
			getTimePriceAndCheck(suppGoods,item);
			if ("Y".equals(suppGoods.getStockApiFlag())) {
				com.lvmama.vst.comm.vo.SupplierProductInfo.Item supplierItem = new com.lvmama.vst.comm.vo.SupplierProductInfo.Item(suppGoods.getSuppGoodsId(), item.getVisitTimeDate());
				if (null != item.getCheckStockQuantity() && 0 != item.getCheckStockQuantity()) {
					supplierItem.setQuantity(item.getCheckStockQuantity()); //期票打包
				} else {
					supplierItem.setQuantity((long)item.getQuantity());  //非期票打包
				}
				result.setReturnContent(supplierItem);
			}
		}catch(Exception ex){
			result.setMsg(ex);
		}
		return result;
	}

	@Override
	public void updateStock(Long timePriceId, Long stock,
			Map<String, Object> dataMap) {
		//景乐商品库存扣减绕过ORACLE库
		if(null!=dataMap && dataMap.containsKey("categoryId") && dataMap.containsKey("aperiodicFlag")){
			Long categoryId = (Long) dataMap.get("categoryId");
			String aperiodicFlag = (String) dataMap.get("aperiodicFlag");
			if(categoryId.intValue() == BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().intValue()
					||categoryId.intValue() == BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().intValue()
					||categoryId.intValue() == BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().intValue()){
				Map<String, Object> params = new HashMap<>();
				params.put("timePriceId", timePriceId);
				params.put("stock", -stock);
				if(dataMap.containsKey("suppGoodsId")){
					params.put("suppGoodsId",dataMap.get("suppGoodsId"));
				}
				if(StringUtils.isNotBlank(aperiodicFlag)){
					if("Y".equalsIgnoreCase(aperiodicFlag.trim())){
						Integer updateStatus = suppGoodsNotimeTimePriceApiServiceRemote.updateStockForOrder(params).getReturnContent();
						if(updateStatus == 1){
							SuppGoodsNotimeTimePriceVo vo = suppGoodsNotimeTimePriceApiServiceRemote.getSuppGoodsNotimeTimePriceById(timePriceId).getReturnContent();
							comPushServiceRemote.pushTimePrice(vo.getSuppGoodsId(), Collections.singletonList(vo.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
						}
					}else if("N".equalsIgnoreCase(aperiodicFlag.trim())){
						Integer updateStatus = suppGoodsAddTimePriceApiServiceRemote.updateStockForOrder(params).getReturnContent();
						if(null != updateStatus && updateStatus.intValue() == 1){
							SuppGoodsAddTimePriceVo vo = suppGoodsAddTimePriceApiServiceRemote.selectByTimePriceId(timePriceId).getReturnContent();
							comPushServiceRemote.pushTimePrice(vo.getSuppGoodsId(), Collections.singletonList(vo.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
						}
					}
				}else{
					LOG.info("OrderTicketAddTimePriceServiceImpl#updateStock. categoryId is "+categoryId +", and timePriceId is" + timePriceId+". but aperiodicFlag is empty");
					throwIllegalException("景乐商品库存扣除操作失败");
				}

			}
		}else {
			if(!goodsTicketNoTimePriceStockService.updateStock(timePriceId, -stock)){
				throwIllegalException("库存扣减失败");
			}
		}
	}

	@Override
	public ResultHandle validate(SuppGoods suppGoods, Item item,
			OrdOrderItemDTO orderItem, OrdOrderDTO order) {
		ResultHandle handle = new ResultHandle();
		SuppGoodsBaseTimePrice timePrice = getTimePriceAndCheck(suppGoods,item);
		SuppGoodsNotimeTimePrice notimeTimePrice = (SuppGoodsNotimeTimePrice)timePrice;
		
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
		boolean overBuy = false;
		//如果是预售券的兑换订单就不走买断
		if(!OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(item.getOrderSubType())){
			//通过商品Id和游玩时间获取，该商品在该时间，所在的预控策略对象
			goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
			//如果能找到该有效预控的资源
			hasControled = goodsResPrecontrolPolicyVO != null && goodsResPrecontrolPolicyVO.isControl();
			overBuy = goodsResPrecontrolPolicyVO != null&&"Y".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay())?true:false;
			LOG.info("vst_order===goodsResPrecontrolPolicyVO==="+ GsonUtils.toJson(goodsResPrecontrolPolicyVO));
		}

		
		if(hasControled ){
			// --ziyuanyukong  通过接口获取该商品在这个时间的价格【参数：成人数，儿童数，商品Id,游玩时间】
			resPriceList = resControlBudgetRemote.queryPreControlTimePriceByParam(orderItem.getVisitTime(),orderItem.getCategoryId(), orderItem.getSuppGoodsId());
			if(resPriceList==null || (resPriceList!=null && resPriceList.size()<=0)){
				hasControled = false;
			}else{
				LOG.info("***资源预控***");
				LOG.info("no time 门票：" + orderItem.getSuppGoodsId() + "存在预控资源");
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
		
		OrdOrderStock stock = createStock(notimeTimePrice.getEndDate(), orderItem.getQuantity());
		
		makeNotNeedResourceConfirm(stock);
		
		
		setOrderItemsNeedResourceConfirm(stock.getNeedResourceConfirm(), orderItem);
		//设置提前预订时间
		if(ProductCategoryUtil.isTicket(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))){
			orderItem.setAheadTime(notimeTimePrice.getEndDate());
		}else{
			if(notimeTimePrice.getAheadBookTime()!=null){
				//throwNullException("提前预订时间为空");
				orderItem.setAheadTime(DateUtils.addMinutes(orderItem.getVisitTime(), -notimeTimePrice.getAheadBookTime().intValue()));
			}
		}
		
		orderItem.setSettlementPrice(notimeTimePrice.getSettlementPrice());
		orderItem.setActualSettlementPrice(notimeTimePrice.getSettlementPrice());
		orderItem.setPrice(notimeTimePrice.getPrice());
		orderItem.setMarketPrice(notimeTimePrice.getMarkerPrice());

		//只有期票打包需要重新计算价格，别的不需要。
        if(orderItem.getOrderPack()!=null&&(orderItem.getCategoryId()== BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId()||orderItem.getCategoryId()== BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId()||orderItem.getCategoryId()== BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId())){
			//此处仿照addTimePriceService来完成，由于期票没有特殊打包规则，所以去掉了ProdPackageDetailAddPrice查询
			if (orderItem.getItem().getDetailId() != null) {
				ProdPackageDetail detail = OrderUtils.getProdPackageDetailByDetailId((OrdOrderPackDTO) orderItem.getOrderPack(),
						orderItem.getItem().getDetailId());
				if (detail == null) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("期票打包被打包的产品数据不存在");
					}
					throwNullException("被打包的产品数据不存在");
				}
				fillPackageOrderItemPrice(orderItem, detail);
			}else {
				//门票自主打包才会出现直接打包到商品
				ProdPackageDetail detail = OrderUtils.getProdPackageDetail((OrdOrderPackDTO) orderItem.getOrderPack(), orderItem.getSuppGoodsId());
				if (detail == null) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("期票打包被打包的产品数据不存在"); //$NON-NLS-1$
					}
					throwNullException("被打包的产品数据不存在");
				}
				orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.ticket_pack_quantity.name(), detail.getPackageCount());
				orderItem.setQuantity(orderItem.getQuantity() * detail.getPackageCount());
				stock.setQuantity(orderItem.getQuantity());
				fillPackageOrderItemPrice(orderItem, detail);
			}
		}
        orderStockList.add(stock);
		orderItem.setOrderStockList(orderStockList);
		
		if(hasControled){
			

			

			String preControlType = goodsResPrecontrolPolicyVO.getControlType();
			if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equals(preControlType)){
				//记录买断和非买断的结算总额
				if(leftMoney==null ){
					leftMoney = goodsResPrecontrolPolicyVO.getLeftAmount().longValue() ;
				}
				long shouldSettleTotalPrice = orderItem.getQuantity()*notimeTimePrice.getSettlementPrice();
				if(shouldSettleTotalPrice>leftMoney&& leftMoney>0&&!overBuy){
					buyoutNum = (long) Math.ceil(leftMoney/orderItem.getSettlementPrice().doubleValue());
					//买断+非买断
					buyoutTotalPrice = buyoutTotalPrice + buyoutNum * notimeTimePrice.getSettlementPrice();
					long notBuyNum = (orderItem.getQuantity() - buyoutNum);
					if(notBuyNum>0){
						notBuyoutTotalPrice = notBuyoutTotalPrice + notBuyNum * notimeTimePrice.getBakPrice();
					}
				}else if(shouldSettleTotalPrice<=leftMoney||overBuy){
					//买断
					buyoutTotalPrice = buyoutTotalPrice + shouldSettleTotalPrice;
					buyoutNum = orderItem.getQuantity();
				}
				orderItem.setBuyoutQuantity(buyoutNum);
				orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
				orderItem.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
				leftMoney = leftMoney - shouldSettleTotalPrice;
				orderItem.setBuyoutPrice((long)orderItem.getBuyoutTotalPrice()/orderItem.getBuyoutQuantity());
				
			}else if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.inventory.name().equals(preControlType)){
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
				if(roomNum>leftQuantity&&!overBuy){
					orderItem.setBuyoutQuantity(buyoutsaledNum + leftQuantity);
					buyoutTotalPrice = buyoutTotalPrice + leftQuantity*notimeTimePrice.getSettlementPrice();
					notBuyoutTotalPrice = notBuyoutTotalPrice + (notimeTimePrice.getBakPrice() * (roomNum-leftQuantity));
					//酒店设置非买断的总价
					orderItem.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
					//设置买断的总价
					orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
				}else{
					orderItem.setBuyoutQuantity(buyoutsaledNum + roomNum);
					buyoutTotalPrice = buyoutTotalPrice + roomNum*notimeTimePrice.getSettlementPrice();
					orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
				}
				
				orderItem.setBuyoutPrice((long)orderItem.getBuyoutTotalPrice()/orderItem.getBuyoutQuantity());
			}
			orderItem.setBuyoutFlag("Y");
		
			
		
			
			
			orderItem.setBuyoutPrice(notimeTimePrice.getSettlementPrice());
			long leftQuantity = goodsResPrecontrolPolicyVO.getLeftNum();
			if(orderItem.getQuantity()>leftQuantity){
				orderItem.setBuyoutQuantity(leftQuantity);
				//酒店设置买断的总价
				orderItem.setNotBuyoutSettleAmout(notimeTimePrice.getBakPrice() * (orderItem.getQuantity()-leftQuantity));
			}else{
				orderItem.setBuyoutQuantity(orderItem.getQuantity());
			}
			orderItem.setBuyoutTotalPrice(orderItem.getBuyoutQuantity() * orderItem.getBuyoutPrice());
			orderItem.setBuyoutFlag("Y");
			orderItem.setNebulaProjectId(goodsResPrecontrolPolicyVO.getNebulaProjectId());
		}
		
		
		OrderTimePriceUtils.setTicketRefund(orderItem, suppGoodsClientService);
        OrderTimePriceUtils.setTicketReschedule(orderItem,suppGoodsClientService);
		if("Y".equalsIgnoreCase(notimeTimePrice.getStockFlag())){//限库存在的
			order.addUpdateStock(notimeTimePrice, stock.getQuantity(), this);			
		}
		fillOrdMulPriceRateListByOrdOrderItem(orderItem);

		//开始于外币项目，存放外币结算等附加信息 开始 at 2018.10.8
		setOrdOrderItemExtendDTOInfo(notimeTimePrice, suppGoods, orderItem);
		//开始于外币项目，存放外币结算等附加信息 结束 at 2018.10.8

		return handle;
	}

	public SuppGoodsBaseTimePrice getTimePriceAndCheck(SuppGoods suppGoods,BuyInfo.Item item) {
		Date date  =item.getVisitTimeDate();
		if(date == null){
			date = DateUtil.getDayStart(DateUtils.addDays(new Date(),1));
		}
		SuppGoodsBaseTimePrice timePrice = goodsTicketNoTimePriceStockService.getTimePrice(suppGoods.getSuppGoodsId(),date, true);
		if(timePrice==null){
			if (LOG.isDebugEnabled()) {
				LOG.debug("getTimePriceAndCheck(SuppGoods, BuyInfo.Item, Date) - timePriceHandle == null||timePriceHandle.hasNull(),suppGoodsId={},visitTime={}",new Object[]{suppGoods.getSuppGoodsId(),item.getVisitTimeDate()}); //$NON-NLS-1$
			}
			throwNullException("时间价格表为空");
		}
		/*if(!"Y".equalsIgnoreCase(timePrice.getOnsaleFlag())){
			throwIllegalException("商品不可售");
		}*/
		SuppGoodsNotimeTimePrice notimeTimePrice = (SuppGoodsNotimeTimePrice)timePrice;
		if("Y".equals(notimeTimePrice.getStockFlag())){
			if(timePrice.getStock()<item.getQuantity()){
				throwIllegalException("库存不足");
			}
		}
		super.checkParam(suppGoods, item, true);
		return timePrice;
	}
	/**
	 * 酒套餐商品影响订购的参数检查
	 * @param suppGoods
	 * @param item
	 */
	protected void checkParam(final SuppGoods suppGoods, final DestBuBuyInfo.Item item, boolean ck){
		
		if(suppGoods==null){
			throw new IllegalArgumentException("商品ID=" + item.getGoodsId() + "不存在");
		}
		if(ck){
			if (item.getQuantity() <= 0) {
				throw new IllegalArgumentException("商品 " + suppGoods.getGoodsName() + " 订购数量小于等于零");
			}

            if((null != suppGoods.getMaxQuantity()) && (item.getQuantity() > suppGoods.getMaxQuantity())){
//              throw new IllegalArgumentException("商品 " + suppGoods.getGoodsName() + " 订购数量超出最大值");
                throwIllegalException(OrderStatusEnum.ORDER_ERROR_CODE.OVER_MAX_BOOK_VALUE.getErrorCode(),
                                                    "商品 " + suppGoods.getGoodsName() + " 订购数量超出最大值");
            }
            
            if((null != suppGoods.getMinQuantity()) && (item.getQuantity() <suppGoods.getMinQuantity())){
                throw new IllegalArgumentException("商品 " + suppGoods.getGoodsName() + " 订购数量小于最小值");
            }
			
			if (item.getOwnerQuantity() > item.getQuantity()) {
				throw new IllegalArgumentException("商品" + suppGoods.getGoodsName() + "  实际订购数量小于零");
			}
		}
	}
	@Override
	public ResultHandleT<SuppGoodsBaseTimePrice> getTimePrice(Long goodsId,
			Date specDate, boolean checkAhead) {
		SuppGoodsBaseTimePrice timePrice = goodsTicketNoTimePriceStockService.getTimePrice(goodsId, specDate, checkAhead);
		ResultHandleT<SuppGoodsBaseTimePrice> handle = new ResultHandleT<SuppGoodsBaseTimePrice>();
		handle.setReturnContent(timePrice);
		return handle;
	}

	@Override
	public void updateRevertStock(Long suppGoodsId, Date specDate, Long stock,
			Map<String, Object> dataMap) {
		LOG.info("com.lvmama.vst.order.timeprice.service.impl.OrderTicketNoTimePriceServiceImpl.updateRevertStock;suppGoodsId is +"+suppGoodsId+" and stock is" +stock);
		//景乐商品库存扣减/返还绕过ORACLE库
		if(null!=dataMap && dataMap.containsKey("categoryId") && dataMap.containsKey("aperiodicFlag")) {
			Long categoryId = (Long) dataMap.get("categoryId");
			String aperiodicFlag = (String) dataMap.get("aperiodicFlag");
			LOG.info("com.lvmama.vst.order.timeprice.service.impl.OrderTicketNoTimePriceServiceImpl.updateRevertStock;category is "+categoryId +". and aperiodicFlag is "+aperiodicFlag);
			if (categoryId.intValue() == BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().intValue()
					|| categoryId.intValue() == BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().intValue()
					|| categoryId.intValue() == BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().intValue()) {

				if (StringUtils.isNotBlank(aperiodicFlag)) {
					try {
						if ("Y".equalsIgnoreCase(aperiodicFlag.trim())) {
							SuppGoodsNotimeTimePriceVo vo = suppGoodsNotimeTimePriceApiServiceRemote.getTimePrice(suppGoodsId, specDate, false).getReturnContent();
							if(null != vo){
								Long timePriceId = vo.getTimePriceId();
								Map<String,Object> params = new HashMap<String, Object>();
								params.put("timePriceId", timePriceId);
								params.put("stock", stock);
								if(dataMap.containsKey("suppGoodsId")){
									params.put("suppGoodsId",dataMap.get("suppGoodsId"));
								}
								Integer updateStatus = suppGoodsNotimeTimePriceApiServiceRemote.updateStockForOrder(params).getReturnContent();
								if (null != updateStatus &&  updateStatus == 1) {
									goodsBaseTimePriceStockServiceImpl.pushTimePrice(vo.getSuppGoodsId(), Collections.singletonList(vo.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
								}
							}else{
								LOG.info("cannot find SuppGoodsNotimeTimePriceVo by suppGoodsId "+ suppGoodsId +"specDate "+specDate);
							}
						} else if ("N".equalsIgnoreCase(aperiodicFlag.trim())) {
							SuppGoodsAddTimePriceVo vo =suppGoodsAddTimePriceApiServiceRemote.getBaseTimePrice(suppGoodsId, specDate, false).getReturnContent();
							if(null != vo){
								Long timePriceId = vo.getTimePriceId();
								Map<String,Object> params = new HashMap<String, Object>();
								params.put("timePriceId", timePriceId);
								params.put("stock", stock);
								if(dataMap.containsKey("suppGoodsId")){
									params.put("suppGoodsId",dataMap.get("suppGoodsId"));
								}
								Integer updateStatus = suppGoodsAddTimePriceApiServiceRemote.updateStockForOrder(params).getReturnContent();
								if (null != updateStatus &&  updateStatus == 1) {
									goodsBaseTimePriceStockServiceImpl.pushTimePrice(vo.getSuppGoodsId(), Collections.singletonList(vo.getSpecDate()), ComIncreament.DATA_SOURCE_TYPE.ORDER_STATUS);
								}
							}else{
								LOG.info("cannot find SuppGoodsAddTimePriceVo by suppGoodsId "+ suppGoodsId +"specDate "+specDate);
							}
						}
					}catch (Exception e){
						LOG.error(e.getMessage());
					}
				}
			}
		}else{
			SuppGoodsBaseTimePrice timePrice = goodsTicketNoTimePriceStockService.getTimePrice(suppGoodsId, specDate, false);
			if(timePrice!=null){
				goodsTicketNoTimePriceStockService.updateStock(timePrice.getTimePriceId(), stock);
			}
		}

	}

	@Override
	public String getTimePricePrefix() {
		return "TicketNotimeTimePrice";
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

	@Override
	public ResultHandleT<Object> destBucheckStock(SuppGoods suppGoods,
			DestBuBuyInfo.Item item, Long distributionId,
			Map<String, Object> dataMap) {
		ResultHandleT<Object> result = new ResultHandleT<Object>();
		try{
			getTimePriceAndCheckForNewHotelComb(suppGoods,item);
		}catch(Exception ex){
			result.setMsg(ex);
		}
		return result;

	}
	
	public SuppGoodsBaseTimePrice getTimePriceAndCheckForNewHotelComb(SuppGoods suppGoods,DestBuBuyInfo.Item item) {
		Date date  =item.getVisitTimeDate();
		if(date == null){
			date = DateUtil.getDayStart(DateUtils.addDays(new Date(),1));
		}
		SuppGoodsBaseTimePrice timePrice = goodsTicketNoTimePriceStockService.getTimePrice(suppGoods.getSuppGoodsId(),date, true);
		if(timePrice==null){
			if (LOG.isDebugEnabled()) {
				LOG.debug("getTimePriceAndCheck(SuppGoods, BuyInfo.Item, Date) - timePriceHandle == null||timePriceHandle.hasNull(),suppGoodsId={},visitTime={}",new Object[]{suppGoods.getSuppGoodsId(),item.getVisitTimeDate()}); //$NON-NLS-1$
			}
			throwNullException("时间价格表为空");
		}
		/*if(!"Y".equalsIgnoreCase(timePrice.getOnsaleFlag())){
			throwIllegalException("商品不可售");
		}*/
		SuppGoodsNotimeTimePrice notimeTimePrice = (SuppGoodsNotimeTimePrice)timePrice;
		if("Y".equals(notimeTimePrice.getStockFlag())){
			if(timePrice.getStock()<item.getQuantity()){
				throwIllegalException("库存不足");
			}
		}
		this.checkParam(suppGoods, item, true);
		return timePrice;
	}
	
	public ResultHandle validate(SuppGoods suppGoods, DestBuBuyInfo.Item item, OrdOrderItemDTO orderItem, OrdOrderDTO order){
		ResultHandleT<Object> result = new ResultHandleT<Object>();
		ResultHandle handle = new ResultHandle();
		SuppGoodsBaseTimePrice timePrice = getTimePriceAndCheckForNewHotelComb(suppGoods,item);
		SuppGoodsNotimeTimePrice notimeTimePrice = (SuppGoodsNotimeTimePrice)timePrice;
		
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
			resPriceList = resControlBudgetRemote.queryPreControlTimePriceByParam(orderItem.getVisitTime(),orderItem.getCategoryId(), orderItem.getSuppGoodsId());
			if(resPriceList==null || (resPriceList!=null && resPriceList.size()<=0)){
				hasControled = false;
			}else{
				LOG.info("***资源预控***");
				LOG.info("no time 门票：" + orderItem.getSuppGoodsId() + "存在预控资源");
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
		
		OrdOrderStock stock = createStock(notimeTimePrice.getEndDate(), orderItem.getQuantity());
		
		makeNotNeedResourceConfirm(stock);
		orderStockList.add(stock);
		
		setOrderItemsNeedResourceConfirm(stock.getNeedResourceConfirm(), orderItem);
		//设置提前预订时间
		if(ProductCategoryUtil.isTicket(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))){
			orderItem.setAheadTime(notimeTimePrice.getEndDate());
		}else{
			if(notimeTimePrice.getAheadBookTime()!=null){
				//throwNullException("提前预订时间为空");
				orderItem.setAheadTime(DateUtils.addMinutes(orderItem.getVisitTime(), -notimeTimePrice.getAheadBookTime().intValue()));
			}
		}
		
		orderItem.setOrderStockList(orderStockList);
		orderItem.setSettlementPrice(notimeTimePrice.getSettlementPrice());
		orderItem.setActualSettlementPrice(notimeTimePrice.getSettlementPrice());
		orderItem.setPrice(notimeTimePrice.getPrice());
		orderItem.setMarketPrice(notimeTimePrice.getMarkerPrice());

        //只有期票打包需要重新计算价格，别的不需要。
        if(orderItem.getOrderPack()!=null&&(orderItem.getCategoryId()== BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId()||orderItem.getCategoryId()== BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId()||orderItem.getCategoryId()== BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId())){
            //门票自主打包才会出现直接打包到商品
            ProdPackageDetail detail = OrderUtils.getProdPackageDetail((OrdOrderPackDTO)orderItem.getOrderPack(), orderItem.getSuppGoodsId());
            if(detail==null){
                if (LOG.isDebugEnabled()) {
                    LOG.debug("期票打包被打包的产品数据不存在"); //$NON-NLS-1$
                }
                throwNullException("被打包的产品数据不存在");
            }
            orderItem.putContent(OrderEnum.ORDER_TICKET_TYPE.ticket_pack_quantity.name(), detail.getPackageCount());
            orderItem.setQuantity(orderItem.getQuantity()*detail.getPackageCount());
            fillPackageOrderItemPrice(orderItem, detail);
        }
		
		
		if(hasControled){
			

			

			String preControlType = goodsResPrecontrolPolicyVO.getControlType();
			if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equals(preControlType)){
				//记录买断和非买断的结算总额
				if(leftMoney==null ){
					leftMoney = goodsResPrecontrolPolicyVO.getLeftAmount().longValue() ;
				}
				long shouldSettleTotalPrice = orderItem.getQuantity()*notimeTimePrice.getSettlementPrice();
				if(shouldSettleTotalPrice>leftMoney&& leftMoney>0){
					buyoutNum = (long) Math.ceil(leftMoney/orderItem.getSettlementPrice().doubleValue());
					//买断+非买断
					buyoutTotalPrice = buyoutTotalPrice + buyoutNum * notimeTimePrice.getSettlementPrice();
					long notBuyNum = (orderItem.getQuantity() - buyoutNum);
					if(notBuyNum>0){
						notBuyoutTotalPrice = notBuyoutTotalPrice + notBuyNum * notimeTimePrice.getBakPrice();
					}
				}else if(shouldSettleTotalPrice<=leftMoney){
					//买断
					buyoutTotalPrice = buyoutTotalPrice + shouldSettleTotalPrice;
					buyoutNum = orderItem.getQuantity();
				}
				orderItem.setBuyoutQuantity(buyoutNum);
				orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
				orderItem.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
				leftMoney = leftMoney - shouldSettleTotalPrice;
				orderItem.setBuyoutPrice((long)orderItem.getBuyoutTotalPrice()/orderItem.getBuyoutQuantity());
				
			}else if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.inventory.name().equals(preControlType)){
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
				if(roomNum>leftQuantity){
					orderItem.setBuyoutQuantity(buyoutsaledNum + leftQuantity);
					buyoutTotalPrice = buyoutTotalPrice + leftQuantity*notimeTimePrice.getSettlementPrice();
					notBuyoutTotalPrice = notBuyoutTotalPrice + (notimeTimePrice.getBakPrice() * (roomNum-leftQuantity));
					//酒店设置非买断的总价
					orderItem.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
					//设置买断的总价
					orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
				}else{
					orderItem.setBuyoutQuantity(buyoutsaledNum + roomNum);
					buyoutTotalPrice = buyoutTotalPrice + roomNum*notimeTimePrice.getSettlementPrice();
					orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
				}
				
				orderItem.setBuyoutPrice((long)orderItem.getBuyoutTotalPrice()/orderItem.getBuyoutQuantity());
			}
			orderItem.setBuyoutFlag("Y");
		
			
		
			
			
			orderItem.setBuyoutPrice(notimeTimePrice.getSettlementPrice());
			long leftQuantity = goodsResPrecontrolPolicyVO.getLeftNum();
			if(orderItem.getQuantity()>leftQuantity){
				orderItem.setBuyoutQuantity(leftQuantity);
				//酒店设置买断的总价
				orderItem.setNotBuyoutSettleAmout(notimeTimePrice.getBakPrice() * (orderItem.getQuantity()-leftQuantity));
			}else{
				orderItem.setBuyoutQuantity(orderItem.getQuantity());
			}
			orderItem.setBuyoutTotalPrice(orderItem.getBuyoutQuantity() * orderItem.getBuyoutPrice());
			orderItem.setBuyoutFlag("Y");
			orderItem.setNebulaProjectId(goodsResPrecontrolPolicyVO.getNebulaProjectId());
		}
		
		
		OrderTimePriceUtils.setTicketRefund(orderItem, suppGoodsClientService);
		if("Y".equalsIgnoreCase(notimeTimePrice.getStockFlag())){//限库存在的
			order.addUpdateStock(notimeTimePrice, stock.getQuantity(), this);			
		}
		return handle;
	}
}
