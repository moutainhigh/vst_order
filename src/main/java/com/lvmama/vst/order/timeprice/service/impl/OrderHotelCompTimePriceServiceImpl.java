/**
 * 
 */
package com.lvmama.vst.order.timeprice.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mortbay.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.po.ResPreControlTimePrice;
import com.lvmama.vst.back.control.po.ResPrecontrolPolicy;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.control.vo.ResPreControlTimePriceVO;
import com.lvmama.vst.back.goods.po.PresaleStampTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsLineTimePrice;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdPackageDetail;
import com.lvmama.vst.back.prod.po.ProdPackageDetailAddPrice;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;

/**
 * 酒店套餐只会存在一个价格，
 * 不存在priceType相关的数据
 * @author lancey
 *
 */
@Component("orderHotelCompTimePriceService")
public class OrderHotelCompTimePriceServiceImpl extends AbstractOrderLineTimePriceServiceImpl{
	@Autowired
	private SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;

	@Override
	protected long doCalcPriceInfo(SuppGoods suppGoods, Item item,
			OrdOrderItem orderItem, SuppGoodsLineTimePrice lineTimePrice, 
			ProdPackageDetailAddPrice detailAddPrice, ProdPackageDetail detail) {
		long settlementPrice = getSettlementPriceTypeValue(suppGoods, lineTimePrice, OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name());
		long price = getPriceTypeValue(suppGoods, lineTimePrice, OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name());
		lineTimePrice.setBakPrice(settlementPrice);
		/** 开始资源预控买断价格  **/
		Long precontrolSettlePrice = null;
		Long precontrolSalePrice = null;
		List<ResPreControlTimePriceVO> resPriceList = null;
		long buyoutTotalPrice = 0;
		long notBuyoutTotalPrice = 0;
		Long leftMoney = null;
		long buyoutNum = 0;
		SuppGoods goods = orderItem.getSuppGoods();
		Long goodsId = goods.getSuppGoodsId();
		Date visitDate = orderItem.getVisitTime();
		GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO=new GoodsResPrecontrolPolicyVO();
		boolean hasControled=false;
		List<PresaleStampTimePrice> presales=new ArrayList<PresaleStampTimePrice>();
		boolean overBuy = false;
		

		
		
		if(!OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(item.getOrderSubType())){
			//通过商品Id和游玩时间获取，该商品在该时间，所在的预控策略对象
			 goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
			//如果能找到该有效预控的资源
			 hasControled = goodsResPrecontrolPolicyVO != null && goodsResPrecontrolPolicyVO.isControl();
			 overBuy = goodsResPrecontrolPolicyVO != null&&"Y".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay())?true:false;
			 LOG.info("vst_order===goodsResPrecontrolPolicyVO==="+ GsonUtils.toJson(goodsResPrecontrolPolicyVO));
		}
		
		if(hasControled ){
			resPriceList = resControlBudgetRemote.queryPreControlTimePriceByParam(orderItem.getVisitTime(),orderItem.getCategoryId(), orderItem.getSuppGoodsId());
			if(resPriceList==null || (resPriceList!=null && resPriceList.size()<=0)){
				hasControled = false;
			}else{
				LOG.info("***资源预控***");
				LOG.info("酒店套餐：" + orderItem.getSuppGoodsId() + "存在预控资源");
			}
			if(resPriceList!=null && resPriceList.size()>0){
				
				
				precontrolSettlePrice = getPrecontrolSettlementPriceTypeValue(suppGoods,resPriceList,OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENTPRICE_PRE.name());
				precontrolSalePrice = getPrecontrolPriceTypeValue(suppGoods,resPriceList,OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_PRE.name());
				if(precontrolSettlePrice!=null ){
					settlementPrice = precontrolSettlePrice.longValue();
				}
				if(precontrolSalePrice!=null ){
					price = precontrolSalePrice.longValue();
				}
			}
		}
		/** end **/
		orderItem.setSettlementPrice(settlementPrice);
		orderItem.setPrice(price);
		if(detailAddPrice != null) {
			fillPackageOrderItemPrice(orderItem, detailAddPrice);
		} else if(detail != null){//针对打包重新计算单价
			fillPackageOrderItemPrice(orderItem, detail);
		}
		orderItem.setQuantity((long)item.getQuantity());
		//如果是券
		if(OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(item.getOrderSubType())){
			Map<String,Object> map =new HashMap<String, Object>();
			map.put("goodsId", goodsId);
			map.put("applyDate", visitDate);
			presales=suppGoodsTimePriceClientService.selectPresaleStampTimePrices(map);
			Log.info("酒店套餐的预售结算价是-----------------------------------------------------------------"+presales.get(0).getValue());
			//只修改结算价，不修改销售价格
			settlementPrice=presales.get(0).getValue();
			orderItem.setSettlementPrice(settlementPrice);
		}
		//设置买断的数量和总价
		if(hasControled){
			String preControlType = goodsResPrecontrolPolicyVO.getControlType();
			if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equals(preControlType)){
				//记录买断和非买断的结算总额
				if(leftMoney==null ){
					leftMoney = goodsResPrecontrolPolicyVO.getLeftAmount().longValue() ;
				}
				long shouldSettleTotalPrice = orderItem.getQuantity()*settlementPrice;
				if(shouldSettleTotalPrice>leftMoney&& leftMoney>0&&!overBuy){
					buyoutNum = (long) Math.ceil(leftMoney/orderItem.getSettlementPrice().doubleValue());
					//买断+非买断
					buyoutTotalPrice = buyoutTotalPrice + buyoutNum *settlementPrice;
					long notBuyNum = (orderItem.getQuantity() - buyoutNum);
					if(notBuyNum>0){
						notBuyoutTotalPrice = notBuyoutTotalPrice + notBuyNum* lineTimePrice.getBakPrice();
					}
				}else if(shouldSettleTotalPrice<=leftMoney||overBuy){
					buyoutNum = orderItem.getQuantity();
					//买断
					buyoutTotalPrice = buyoutTotalPrice + shouldSettleTotalPrice;
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
					buyoutTotalPrice = buyoutTotalPrice + leftQuantity*precontrolSettlePrice;
					notBuyoutTotalPrice = notBuyoutTotalPrice + (lineTimePrice.getBakPrice() * (roomNum-leftQuantity));
					//酒店设置非买断的总价
					orderItem.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
					//设置买断的总价
					orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
				}else{
					orderItem.setBuyoutQuantity(buyoutsaledNum + roomNum);
					buyoutTotalPrice = buyoutTotalPrice + roomNum*precontrolSettlePrice;
					orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
				}
				
				orderItem.setBuyoutPrice((long)orderItem.getBuyoutTotalPrice()/orderItem.getBuyoutQuantity());
			}
			orderItem.setBuyoutFlag("Y");
			orderItem.setNebulaProjectId(goodsResPrecontrolPolicyVO.getNebulaProjectId());
		}
		
		orderItem.setActualSettlementPrice(orderItem.getSettlementPrice());
		orderItem.setTotalSettlementPrice(orderItem.getSettlementPrice()*orderItem.getQuantity());
		fillOrdMulPriceRateListByOrdOrderItem(orderItem);
		return orderItem.getQuantity();
	}

	@Override
	protected long getNeedStock(Item item) {
		return item.getQuantity();
	}

	
	
}
