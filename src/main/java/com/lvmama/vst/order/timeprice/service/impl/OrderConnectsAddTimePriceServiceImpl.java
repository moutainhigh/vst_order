package com.lvmama.vst.order.timeprice.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsRefund;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.play.connects.client.SuppGoodsConnectsAdditionalClientService;
import com.lvmama.vst.back.play.connects.po.SuppGoodsConnectsAdditional;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prom.rule.favor.FavorableAmount;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.order.timeprice.service.AbstractOrderTimePriceService;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;

/**
 * 交通接驳时间价格实现类
 * （借用门票时间价格表）
 *
 */
@Component("orderConnectsAddTimePriceService")
public class OrderConnectsAddTimePriceServiceImpl extends AbstractOrderTimePriceService{
	private static final Logger logger = LoggerFactory.getLogger(OrderConnectsAddTimePriceServiceImpl.class);
	@Autowired
	private SuppGoodsConnectsAdditionalClientService suppGoodsConnectsAdditionalClientService;
	
	@Resource(name="goodsOraTicketAddTimePriceStockService")
	private IGoodsTimePriceStockService goodsTicketAddTimePriceStockService;
	
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	
	@Override
	public ResultHandleT<Object> checkStock(SuppGoods suppGoods, Item item,
			Long distributionId, Map<String, Object> dataMap) {
		ResultHandleT<Object> resultHandleT = new ResultHandleT<Object>();

		try {
			if (suppGoods == null || !suppGoods.isValid()) {
				resultHandleT.setMsg("商品不存在或无效。");
				return resultHandleT;
			}
			ProdProduct prodProduct = suppGoods.getProdProduct();
			if (prodProduct != null) {
				ResultHandleT<SuppGoodsConnectsAdditional> goodsAdditionalHandleT= suppGoodsConnectsAdditionalClientService.selectSuppGoodsConnectsAdditional(suppGoods.getSuppGoodsId());
				if(goodsAdditionalHandleT.hasNull()||goodsAdditionalHandleT.getReturnContent()==null){
					resultHandleT.setMsg("商品附加信息参数异常");
					return resultHandleT;
				}

				if (item.getVisitTime() == null) {
					resultHandleT.setMsg("请选择时间");
					return resultHandleT;
				}
				// 商品参数验证
				checkParam(suppGoods, item, true);
				ResultHandleT<SuppGoodsBaseTimePrice> timePriceHolder = this.getTimePrice(suppGoods.getSuppGoodsId(),DateUtil.stringToDate(item.getVisitTime(), "yyyy-MM-dd"), true);
				LOG.info("正在进行库存检查，获取时间价格表数据,得到的时间数据为 \n"+ JSONArray.fromObject(timePriceHolder));
				if (timePriceHolder.isFail() || timePriceHolder.getReturnContent() == null) {
					LOG.info("商品ID=" + suppGoods.getSuppGoodsId()+ ",时间" + item.getVisitTime() + "时间价格表不存在。");
					resultHandleT.setMsg("商品  " + suppGoods.getGoodsName()+ " (ID:"+ suppGoods.getSuppGoodsId()+ ")时间价格表不存在。");
					return resultHandleT;
				}
				SuppGoodsBaseTimePrice timePrice = timePriceHolder.getReturnContent();
				if (!this.checkTimePrice(timePrice, new Date(), (long) (item.getQuantity()))) {
					LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
					resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
					return resultHandleT;
				}
				SuppGoodsAddTimePrice addTimePrice = (SuppGoodsAddTimePrice) timePrice;
				//需要校验日库存/共享总库存/共享日限制
				if("Y".equalsIgnoreCase(addTimePrice.getStockFlag())){
					if (addTimePrice.getStock() < item.getQuantity() || ((item.getCheckStockQuantity() != null
							&& item.getCheckStockQuantity() > 0) && addTimePrice.getStock() < item.getCheckStockQuantity())) {// 库存不满足
						if("N".equalsIgnoreCase(addTimePrice.getOversellFlag())){
							throwIllegalException("库存不足");
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
		ResultHandle result = new ResultHandle();
		if ((orderItem != null) && (item != null)  && (order != null)) {
			List<OrdOrderStock> allList = new ArrayList<OrdOrderStock>();
			ProdProduct prodProduct = suppGoods.getProdProduct();
			if(prodProduct!=null){
				ResultHandleT<SuppGoodsConnectsAdditional> goodsAdditionalHandleT= suppGoodsConnectsAdditionalClientService.selectSuppGoodsConnectsAdditional(suppGoods.getSuppGoodsId());
				if(goodsAdditionalHandleT.hasNull()||goodsAdditionalHandleT.getReturnContent()==null){
					result.setMsg("商品附加信息参数异常");
					return result;
				}
				if (item.getVisitTime() == null) {
					result.setMsg("请选择时间");
					return result;
				}
				checkParam(suppGoods, item, true);
				ResultHandleT<SuppGoodsBaseTimePrice> timePriceHolder = this.getTimePrice(suppGoods.getSuppGoodsId(),DateUtil.stringToDate(item.getVisitTime(), "yyyy-MM-dd"), true);
				if ((timePriceHolder != null) && timePriceHolder.isSuccess() && (timePriceHolder.getReturnContent() != null)) {
					SuppGoodsAddTimePrice addTimePrice = (SuppGoodsAddTimePrice)timePriceHolder.getReturnContent();
					List<OrdOrderStock> stockList = new ArrayList<OrdOrderStock>();
					fillPrice(orderItem,addTimePrice);
					//有库存限制
					if("Y".equalsIgnoreCase(addTimePrice.getStockFlag())){
						if(addTimePrice.getStock()<orderItem.getQuantity()){//库存不满足
							if("N".equalsIgnoreCase(addTimePrice.getOversellFlag())){
								throwIllegalException("库存不足");
							}else{
								if(addTimePrice.getStock()>0){//存在部分库存
									OrdOrderStock stock = createStock(DateUtil.stringToDate(item.getVisitTime(), "yyyy-MM-dd"), addTimePrice.getStock());
									makeNotNeedResourceConfirm(stock);
									stockList.add(stock);
									order.addUpdateStock(addTimePrice, stock.getQuantity(), this);
								}
								//超卖部分
								OrdOrderStock stock = createStock(DateUtil.stringToDate(item.getVisitTime(), "yyyy-MM-dd"), orderItem.getQuantity()-addTimePrice.getStock());
								makeNeedResourceConfirm(stock);
								stockList.add(stock);
							}
						}else{//库存满足的情况下
							OrdOrderStock stock = createStock(DateUtil.stringToDate(item.getVisitTime(), "yyyy-MM-dd"), orderItem.getQuantity());
							makeNotNeedResourceConfirm(stock);
							stockList.add(stock);
							order.addUpdateStock(addTimePrice, orderItem.getQuantity(), this);
						}
					}else{//支持不限情况下
						OrdOrderStock stock = createStock(DateUtil.stringToDate(item.getVisitTime(), "yyyy-MM-dd"), orderItem.getQuantity());
						makeNotNeedResourceConfirm(stock);
						stock.setInventory(OrderEnum.INVENTORY_STATUS.FREESALE.name());
						stockList.add(stock);
					}
					allList.addAll(stockList);
					makeOrderItemTime(orderItem,addTimePrice);

					//开始于外币项目，存放外币结算等附加信息 开始 at 2018.10.8
					setOrdOrderItemExtendDTOInfo(addTimePrice, suppGoods, orderItem);
					//开始于外币项目，存放外币结算等附加信息 结束 at 2018.10.8

				}
			}	
			if(allList.size()>0){
				makeNeedResourceConfirm(orderItem, allList);
			}
			orderItem.setOrderStockList(allList);
			fillOrdMulPriceRateListByOrdOrderItem(orderItem);
		}	

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
		SuppGoodsBaseTimePrice timePrice = goodsTicketAddTimePriceStockService.getTimePrice(goodsId, specDate, checkAhead);
		ResultHandleT<SuppGoodsBaseTimePrice> result = new ResultHandleT<SuppGoodsBaseTimePrice>();
		result.setReturnContent(timePrice);
		return result;
	}

	@Override
	public void updateRevertStock(Long suppGoodsId, Date specDate, Long stock,
			Map<String, Object> dataMap) {
		SuppGoodsBaseTimePrice timePrice = goodsTicketAddTimePriceStockService.getTimePrice(suppGoodsId, specDate, false);
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
		return "ConnectsAddTimePrice";
	}

	@Override
	public void calcSettlementPromotion(OrdOrderItem orderItem,
			List<OrdPromotion> promotions) {
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
