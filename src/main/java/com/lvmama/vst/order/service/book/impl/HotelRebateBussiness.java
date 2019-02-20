package com.lvmama.vst.order.service.book.impl;

import java.math.BigDecimal;
import java.util.List;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsRebateClientService;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.REBATE_TYPE;
import com.lvmama.vst.back.prod.po.ProdProductAddtional;
import com.lvmama.vst.back.prom.po.HotelOrderRebate;
import com.lvmama.vst.back.prom.po.MobileRebateRule;
import com.lvmama.vst.back.prom.po.SuppGoodsRebate;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.DefaultRebateConfig;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.book.AbstractBookService;

/**
 * 酒店订单返现计算
 * 
 *
 */
@Component
public class HotelRebateBussiness extends AbstractBookService{
	private static final Logger logger = LoggerFactory.getLogger(HotelRebateBussiness.class);	
	
	@Autowired
	private CategoryClientService categoryClientService;
	@Autowired
	private SuppGoodsRebateClientService suppGoodsRebateClientService;
	
	
	/**
	 * 计算酒店订单返现金额
	 * @param hotelOrderRebate
	 * @return 
	 */
	public HotelOrderRebate calcHotelRebate(HotelOrderRebate hotelOrderRebate){
		long totalRebateAmount=0L;
		long totalPcRebateAmount=0;
		long totalMobileRebateAmount=0;
		String channel = getHotelChannel(hotelOrderRebate);
		if(channel.equals("")){
			return null;
		}
		String categoryCode =null;
		Long categoryId = 1L;
		BizCategory bizCategory = categoryClientService.findCategoryById(categoryId).getReturnContent();
		categoryCode = bizCategory.getCategoryCode();
		
		//获取是否该主品类定义了返现规则有则取出来-------------------------------------------------------------------------
		ProdProductAddtional  addtionalRebate = null ; 
		MobileRebateRule rebateRule = null ;
		if(OrderEnum.ORDER_CHANNEL.mobile.getCode().equals(channel)){
			addtionalRebate =  new ProdProductAddtional();
			addtionalRebate.setMobileRebate(0L);
			ResultHandleT<MobileRebateRule> resultRule = 
					suppGoodsRebateClientService.queryMobileRebateByCategoryCode(categoryCode);
			if(resultRule.isSuccess() && resultRule.getReturnContent()!=null ){
				rebateRule = resultRule.getReturnContent();
			}
			
		}		
		totalPcRebateAmount = calcSuppGoodsPcRebate(hotelOrderRebate,categoryCode,rebateRule,addtionalRebate);
		
		if(channel.equals(OrderEnum.ORDER_CHANNEL.pc.getCode())){
			totalRebateAmount+=totalPcRebateAmount;
		}
			 //手机端下单
		if(OrderEnum.ORDER_CHANNEL.mobile.getCode().equals(channel)){
			long mobileRebate = addtionalRebate.getMobileRebate();
			totalMobileRebateAmount=mobileRebate;
			totalRebateAmount=mobileRebate;
		}
		//此处待定看是否删除
		if(OrderEnum.ORDER_CHANNEL.mobile.getCode().equals(channel)){
			//含小数往上进位位整数
			totalMobileRebateAmount = (totalMobileRebateAmount+99)/100*100;
			totalPcRebateAmount=(totalPcRebateAmount+99)/100*100;
			hotelOrderRebate.setMobileMoreRebate(totalMobileRebateAmount-totalPcRebateAmount);
		}
		totalRebateAmount =(totalRebateAmount+99)/100*100;
		//----------------------------------------分转为元-----------------------------------------------------------
		totalRebateAmount =new BigDecimal(Math.ceil( PriceUtil.convertToYuan(totalRebateAmount))*100).longValue();
		//----------------------------------------------------------------------------------------------------
		logger.info("totalRebateAmount="+totalPcRebateAmount);
		//保存订单返现信息
		hotelOrderRebate.setRebateAmount(totalRebateAmount);
		hotelOrderRebate.setRebateFlag("N");
		return hotelOrderRebate;
	}
	

	private long calcSuppGoodsPcRebate(HotelOrderRebate hotelOrderRebate,String orderCategoryCode,MobileRebateRule rule,ProdProductAddtional addtionalRebate) {
		long totalPcRebateAmount = 0;
		List<OrdOrderItem> OrdOrderItemList;
		OrdOrderItemList =  hotelOrderRebate.getOrderItemList();
		if(OrdOrderItemList!=null){
			for(OrdOrderItem item:OrdOrderItemList){
				long rebateNow = calcOrderItemPcRebate(item,orderCategoryCode);
				totalPcRebateAmount += rebateNow;
				Log.info("--------------查看item是否有子订单价格--------------------------"+item.getOughtAmount()+"~~~~~~~~~");
				countMobileRebate(rebateNow, item.getQuantity()*item.getPrice(), item.getQuantity(), rule, addtionalRebate);
			}
		}
		return totalPcRebateAmount;
	}
	
	/**
	 * 计算子订单返现
	 * @param channel
	 * @param item
	 * @return
	 */
	private long calcOrderItemPcRebate(OrdOrderItem item,String categoryCode) {
		long totalPcRebateAmount = 0;
		SuppGoodsRebate rebate = suppGoodsRebateClientService.getGoodsRebateByGoodsIdChannel(item.getSuppGoodsId(), "pc").getReturnContent();
		if(rebate!=null){
		   totalPcRebateAmount=pcGoodsRebate(item, rebate,categoryCode);
		}
		logger.info("calcOrderItemRebate result:"+totalPcRebateAmount);
		return totalPcRebateAmount;
	}

	//-----------------------------------------------------------
	/**
	 * 取酒店下单渠道
	 * @param order
	 * @return
	 */
	private String getHotelChannel(HotelOrderRebate hotelRebate) {
		Long channelId = hotelRebate.getDistributionChannel()==null?-1:hotelRebate.getDistributionChannel();
		Long distributorId = hotelRebate.getDistributorId()==null?-1:hotelRebate.getDistributorId();
		String channel="";
		if(distributorId==2||distributorId==3){
			channel="pc";
		}
		if(distributorId==4){
			if(channelId==10000||channelId==10001||channelId==10002){
				channel="mobile";
			}
			if(channelId==107||channelId==108||channelId==110||channelId==103){
				channel="pc";
			}
		}
		return channel;
	} 
	
	public long pcGoodsRebate(OrdOrderItem orderItem,SuppGoodsRebate rebate,String categoryCode){
		if(rebate!=null){
			//固定金额返现
			if(REBATE_TYPE.fixed.getCode().equals(rebate.getRebateType())){
				long fixedAmount = rebate.getFixedAmount()==null?0:rebate.getFixedAmount();
				
				return (fixedAmount*orderItem.getQuantity());
			}else if(rebate.getRebateType().equals(REBATE_TYPE.rate.getCode())){
				long profit = calcOrderItemProfit(orderItem,true,categoryCode);
				
				//平均毛利乘以返现进行向上取证（元）在转为分 在乘以份数得出金额-----------------------------------------------------------------------------
				logger.info(profit+"-----------------pcGoodsRebate---------profit-----");
				long profitOne = new BigDecimal(Math.ceil((Double.valueOf(profit).doubleValue()/orderItem.getQuantity()))).longValue();
				logger.info(profitOne+"-----------------pcGoodsRebate----------profitOne----");
				logger.info(orderItem.getQuantity()+"--------pcGoodsRebate-------orderItem.getQuantity()-------");
				Long rebateFenOne = new BigDecimal(Math.ceil((profitOne*(rebate.getRateAmount().doubleValue()/100)))).longValue();
				logger.info(rebateFenOne+"--------pcGoodsRebate-------rebateFenOne-------");
				long totalRebateOne = (rebateFenOne+99)/100*100;
				logger.info(totalRebateOne+"--------pcGoodsRebate-------totalRebateOne-------");
				long totalRebate = totalRebateOne*orderItem.getQuantity();
				logger.info(totalRebate+"--------pcGoodsRebate-------totalRebate-------");
				return totalRebate;
			}
		}
		return 0;
	}

	/**
	 * 取子订单毛利
	 * @param orderItem
	 * @return
	 */
	private long calcOrderItemProfit(OrdOrderItem orderItem,boolean calcSpread,String categoryCode) {	 
		//毛利
		long profit = (orderItem.getPrice()-orderItem.getSettlementPrice())*orderItem.getQuantity();
		logger.info("calcOrderItemProfit result="+profit);
		if(profit<=0){
			profit=  BigDecimal.valueOf(Math.ceil(orderItem.getPrice()*orderItem.getQuantity()*gainDefaultRebate(categoryCode))).longValue();
		}
		
		logger.info(orderItem.getPrice()+"~~~~~~~calcOrderItemProfit~~~~~~~orderItem.getPrice()~~~~~~~");
		logger.info(categoryCode+"~~~~~~~calcOrderItemProfit~~~~~~~categoryCode~~~~~~~");
		
		logger.info(gainDefaultRebate(categoryCode)+"~~~~~~~calcOrderItemProfit~~~~~~~categoryCode~~~~~~~");
		return profit;
	}

	
	private  Long countMobileRebate(Long pcRebateAmount,Long orderAmount,Long quantity,
			MobileRebateRule rule,ProdProductAddtional  addtionalRebate){
		if(rule ==null){
			return 0L;
		}
		Long pcRebateAmountOne =  new BigDecimal(Math.ceil(pcRebateAmount.doubleValue()/quantity)).longValue();
		Long orderAmountOne = new BigDecimal(Math.ceil(orderAmount.doubleValue()/quantity)).longValue();
		logger.info(pcRebateAmountOne+"^^^^^^^^^^^countMobileRebate^^^^^^^^^pcRebateAmountOne^^^");
		logger.info(orderAmountOne+"^^^^^^^^^^^countMobileRebate^^^^^^^^^orderAmountOne^^^");
		
		try {
				String ruleType = rule.getRuleType();
				float ruleValue= rule.getRuleValue();
				if(Constant.MOBILE_REBATE_RULE.eqPc.name().equals(ruleType)){
					addtionalRebate.setMobileRebate(addtionalRebate.getMobileRebate()+pcRebateAmount);
					return pcRebateAmount;
				}
				if(Constant.MOBILE_REBATE_RULE.morePc.name().equals(ruleType)){
					if(pcRebateAmount==0){
						addtionalRebate.setMobileRebate(addtionalRebate.getMobileRebate()+0L);
						return 0L;
					}else{
						addtionalRebate.setMobileRebate(addtionalRebate.getMobileRebate()+pcRebateAmount+PriceUtil.convertToFen(ruleValue));
						return pcRebateAmount+PriceUtil.convertToFen(ruleValue);
					}
				}
				if(Constant.MOBILE_REBATE_RULE.multiplePc.name().equals(ruleType)){
					//---------------------------------
					float amount = pcRebateAmountOne*ruleValue;
					logger.info(amount+"^^^^^^^multiplePc^^^^^countMobileRebate^^^^^^^^^^^^amount^^^^^^^^");
					long returnAmount = new BigDecimal(Math.ceil(amount)).longValue();
					returnAmount = (returnAmount+99)/100*100;
					logger.info(returnAmount+"^^^^^^multiplePc^^^^^^countMobileRebate^^^^^^^^^^^^returnAmount^^^^^^^^");
					addtionalRebate.setMobileRebate(addtionalRebate.getMobileRebate()+returnAmount*quantity);
					logger.info(addtionalRebate.getMobileRebate()+"^^^^^multiplePc^^^^^^^countMobileRebate^^^^^^^^^^^^addtionalRebate.getMobileRebate()^^^^^^^^");
					logger.info(quantity+"^^^^^multiplePc^^^^^^^countMobileRebate^^^^^^^^^^^^quantity^^^^^^^^");
					return returnAmount*quantity;
					//-----------------------------------------------------------------
				}
				if(Constant.MOBILE_REBATE_RULE.multipleOrder.name().equals(ruleType)){
					//-----------------------------------------------------------------------------------
					float rv = ruleValue/100;
					float amount = orderAmountOne*rv;
					logger.info(amount+"^^^^^^^multipleOrder^^^^^countMobileRebate^^^^^^^^^^^^amount^^^^^^^^");
					//long returnRv = new BigDecimal(Math.ceil(PriceUtil.convertToYuan(new BigDecimal(amount).setScale(0, BigDecimal.ROUND_HALF_UP)))*100).longValue();
					long returnRv = new BigDecimal(Math.ceil(amount)).longValue();
					returnRv = (returnRv+99)/100*100;
					logger.info(returnRv+"^^^^^^multipleOrder^^^^^^countMobileRebate^^^^^^^^^^^^returnAmount^^^^^^^^");
					addtionalRebate.setMobileRebate(addtionalRebate.getMobileRebate()+returnRv*quantity);
					logger.info(addtionalRebate.getMobileRebate()+"^^^^^multipleOrder^^^^^^^countMobileRebate^^^^^^^^^^^^addtionalRebate.getMobileRebate()^^^^^^^^");
					logger.info(quantity+"^^^^^multipleOrder^^^^^^^countMobileRebate^^^^^^^^^^^^quantity^^^^^^^^");

					return returnRv*quantity;
					//-----------------------------------------------------------------------------------
				}
			
		} catch (Exception e) {
			logger.error(ExceptionFormatUtil.getTrace(e));
		}
		addtionalRebate.setMobileRebate(addtionalRebate.getMobileRebate()+0L);
		return 0L;
	}
	
	private double gainDefaultRebate(String categoryCode){
		return DefaultRebateConfig.getInstance().getDefaultRebateByCategory(categoryCode);
	}

}
