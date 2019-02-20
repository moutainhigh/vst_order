package com.lvmama.vst.order.service.book.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.lvmama.vst.comm.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsRebateClientService;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_TICKET_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.REBATE_TYPE;
import com.lvmama.vst.back.prod.po.ProdProductAddtional;
import com.lvmama.vst.back.prom.po.MobileRebateRule;
import com.lvmama.vst.back.prom.po.SuppGoodsRebate;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.DefaultRebateConfig;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.OrderRebateBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;
/**
 * 订单返现计算
 * 
 *
 */
@Component("orderRebateBussiness")
public class OrderRebateBussinessImpl extends AbstractBookService implements OrderRebateBussiness{
	private static final Logger logger = LoggerFactory.getLogger(OrderRebateBussinessImpl.class);	
	
	@Autowired
	private CategoryClientService categoryClientService;
	@Autowired
	private SuppGoodsRebateClientService suppGoodsRebateClientService;
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	
	/**
	 *  订单返现计算
	 */
	@Override
	public void  calcRebate(OrdOrderDTO order){
		try {
			
			showOrderItemListLog(order);
			long totalRebateAmount=0L;
			long totalPcRebateAmount=0;
			long totalMobileRebateAmount=0;
			List<OrdOrderItem>  OrdOrderItemList =null;
			//下单渠道
			String channel = getOrderChannel(order);
			if(channel.equals("")){
				return;
			}
			String categoryCode =null;
			Long categoryId = order.getCategoryId();
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
			//-----------------------------------------------------------------------
			boolean isGroupProduct = isGroupProduct(categoryCode);

			//后台下单返现规则判断--start
			if(order.getDistributorId() == 2){
				boolean isBackRebate =true;
				if(isGroupProduct){
					//跟团游、自由行、邮轮组合产品
					if("category_route_group".equalsIgnoreCase(categoryCode)
							||"category_route_freedom".equalsIgnoreCase(categoryCode)
							|| "category_route_customized".equalsIgnoreCase(categoryCode)
							||"category_comb_cruise".equalsIgnoreCase(categoryCode)
							||"category_traffic_aero_other".equalsIgnoreCase(categoryCode)
							||"category_traffic_bus_other".equalsIgnoreCase(categoryCode)){
						isBackRebate = isBackRebatePack(order);
					}
					//组合套餐票
					if("category_comb_ticket".equalsIgnoreCase(categoryCode)){
						//自主打包
						if(order.getOrderPackList()!=null && !order.getOrderPackList().isEmpty()){
							isBackRebate = isBackRebatePack(order);
						}else{
							isBackRebate =isBackRebateSingle(order);
						}
					}
					//当地游、酒店套餐、酒套餐
					if("category_route_local".equalsIgnoreCase(categoryCode)||
							"category_route_hotelcomb".equalsIgnoreCase(categoryCode)||"category_route_new_hotelcomb".equalsIgnoreCase(categoryCode)){
						isBackRebate = isBackRebateSupplierPack(order);
					}
				}else{
					//其他单卖产品
					isBackRebate =isBackRebateSingle(order);
				}

				if(!isBackRebate){
					setRebateAmountZero(order);
					return;
				}
			}
			
			if(isGroupProduct){
				//跟团游、自由行、邮轮组合产品
				if("category_route_group".equalsIgnoreCase(categoryCode)||"category_route_freedom".equalsIgnoreCase(categoryCode) || "category_route_customized".equalsIgnoreCase(categoryCode)//新加定制游
						||"category_comb_cruise".equalsIgnoreCase(categoryCode)
						||"category_traffic_aero_other".equalsIgnoreCase(categoryCode)||"category_traffic_bus_other".equalsIgnoreCase(categoryCode)){
					totalPcRebateAmount = calcLvmamaPackOrderPcRebate(order, channel,categoryCode,rebateRule,addtionalRebate);
				}
				
				//组合套餐票
				if("category_comb_ticket".equalsIgnoreCase(categoryCode)){
					//自主打包
					if(order.getOrderPackList()!=null&&order.getOrderPackList().size()>0){
						totalPcRebateAmount = calcLvmamaPackOrderPcRebate(order, channel,categoryCode,rebateRule,addtionalRebate);
					}else{
						//totalPcRebateAmount = calcSupplierPackOrderPcRebate(order,channel);
						totalPcRebateAmount = calcSuppGoodsPcRebate(order,categoryCode,rebateRule,addtionalRebate);
					}
					
				}
				//当地游、酒店套餐、酒套餐
				if("category_route_local".equalsIgnoreCase(categoryCode)||
						"category_route_hotelcomb".equalsIgnoreCase(categoryCode)||"category_route_new_hotelcomb".equalsIgnoreCase(categoryCode)){
					totalPcRebateAmount = calcSupplierPackOrderPcRebate(order,channel,categoryCode,rebateRule,addtionalRebate);
				}
			}
			//其他单卖产品
			else{
				totalPcRebateAmount = calcSuppGoodsPcRebate(order,categoryCode,rebateRule,addtionalRebate);
			}
			
			if(channel.equals(OrderEnum.ORDER_CHANNEL.pc.getCode())){
				totalRebateAmount+=totalPcRebateAmount;
			}
				 //手机端下单
			if(OrderEnum.ORDER_CHANNEL.mobile.getCode().equals(channel)){
					//long mobileRebate = suppGoodsRebateClientService.countMobileRebate(categoryCode, totalPcRebateAmount,order.getOughtAmount());
					long mobileRebate = addtionalRebate.getMobileRebate();
					totalMobileRebateAmount=mobileRebate;
					totalRebateAmount=mobileRebate;
			}
			//此处待定看是否删除
			if(OrderEnum.ORDER_CHANNEL.mobile.getCode().equals(channel)){
				//含小数往上进位位整数
				totalMobileRebateAmount = (totalMobileRebateAmount+99)/100*100;
				totalPcRebateAmount=(totalPcRebateAmount+99)/100*100;
				order.setMobileMoreRebate(totalMobileRebateAmount-totalPcRebateAmount);
			}
			totalRebateAmount =(totalRebateAmount+99)/100*100;
			//----------------------------------------分转为元-----------------------------------------------------------
			totalRebateAmount =new BigDecimal(Math.ceil( PriceUtil.convertToYuan(totalRebateAmount))*100).longValue();
			//----------------------------------------------------------------------------------------------------
			logger.info("totalRebateAmount="+totalPcRebateAmount);
			//保存订单返现信息
			//修改点评返现金额大于100元的情况，默认为100元，rebateAmount保存到分
			if (totalRebateAmount > 10000) {
				totalRebateAmount = 10000;
			}
			order.setRebateAmount(totalRebateAmount);
			order.setRebateFlag("N");
		} catch (Exception e) {
			logger.error("计算订单返现异常",e);
		}
	}

	//打包
	private  boolean isBackRebatePack(OrdOrderDTO order){
		boolean isBackRebate=true;
		for(OrdOrderPack pack:order.getOrderPackList()){
			Long productId = pack.getProductId();
			com.lvmama.vst.back.prom.po.SuppGoodsRebate rebate = suppGoodsRebateClientService.getProdRebateByProductId(productId).getReturnContent();
			isBackRebate = checkIsBackRebate(rebate);
			if(!isBackRebate)
				break;
		}
		return isBackRebate;
	}

	//单卖
	private  boolean isBackRebateSingle(OrdOrderDTO order){
		boolean isBackRebate = true;
		if(order.getOrderItemList()!=null){
			for(OrdOrderItem item: order.getOrderItemList()){
				com.lvmama.vst.back.prom.po.SuppGoodsRebate rebate = suppGoodsRebateClientService.getGoodsRebateByGoodsIdChannel(item.getSuppGoodsId(), "pc").getReturnContent();
				isBackRebate = checkIsBackRebate(rebate);
				if(!isBackRebate)
					break;
			}
		}
		return isBackRebate;
	}

	//供应商打包
	private  boolean isBackRebateSupplierPack(OrdOrderDTO order){
		boolean isBackRebate = true;
		Long productId=0l;
		if(order.getOrderItemList()!=null) {
			for (OrdOrderItem item : order.getOrderItemList()) {
				productId = item.getProductId();
				com.lvmama.vst.back.prom.po.SuppGoodsRebate rebate = suppGoodsRebateClientService.getProdRebateByProductId(productId).getReturnContent();
				isBackRebate = checkIsBackRebate(rebate);
				if(!isBackRebate)
					break;
			}
		}
		return isBackRebate;
	}

	private boolean checkIsBackRebate(com.lvmama.vst.back.prom.po.SuppGoodsRebate rebate){
		if(rebate==null){
			logger.info("无数据不返现");
			return false;
		}
		if(StringUtils.isEmpty(rebate.getIsBackRebate())){
			logger.info("后台默认不返现");
			return false;
		}
		if("N".equalsIgnoreCase(rebate.getIsBackRebate())){
			logger.info("后台设置不返现");
			return false;
		}
		return true;
	}

	private void setRebateAmountZero(OrdOrderDTO order){
		order.setRebateAmount(0l);
		order.setRebateFlag("N");
	}

	private long calcSuppGoodsPcRebate(OrdOrderDTO order,String orderCategoryCode,MobileRebateRule rule,ProdProductAddtional addtionalRebate) {
		long totalPcRebateAmount = 0;
		List<OrdOrderItem> OrdOrderItemList;
		OrdOrderItemList =  order.getOrderItemList();
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
	 * 供应商打包产品pc端返现计算
	 * @param order
	 * @param channel
	 * @return
	 */
	private long calcSupplierPackOrderPcRebate(OrdOrderDTO order,String channel,String categoryCode,MobileRebateRule rule,ProdProductAddtional addtionalRebate) {
		long quantity = 0; 
		Long  productId=null;
		long totalPcRebateAmount = 0;
		Double mainGoodsAmount = 0D;
		List<OrdOrderItem> OrdOrderItemList =  order.getOrderItemList();
		//产品自主打包部分毛利
		long totalProfit = 0;
		if(OrdOrderItemList!=null){
			//根据主商品取数量和主产品id
			for(OrdOrderItem item:OrdOrderItemList){
				if("true".equals(item.getMainItem())){
					quantity = item.getQuantity();
					productId = item.getProductId();
				}
			}
			for(OrdOrderItem item:OrdOrderItemList){
				if(item.getProductId().longValue()==productId){
					totalProfit+=calcOrderItemProfit(item,false,categoryCode);
					mainGoodsAmount+=item.getPrice()*item.getQuantity();
					
				}else{//关联销售
					//单个关联项目pc返现
					long rebateNow = calcOrderItemPcRebate(item,categoryCode);
					totalPcRebateAmount += rebateNow;
					countMobileRebate(rebateNow,item.getTotalAmount(), item.getQuantity(), rule, addtionalRebate);
				}
			}
		}
		//主商品返现
		long rebateMainNow = suppGoodsRebateClientService.countProductPcRebate(totalProfit, quantity, productId);
		totalPcRebateAmount += rebateMainNow;
		countMobileRebate(rebateMainNow,BigDecimal.valueOf(mainGoodsAmount).longValue(), quantity,rule, addtionalRebate);
		return totalPcRebateAmount;
	}
	
	
	
	/**
	 * 自主打包产品pc端返现计算
	 * @param order
	 */
	private long calcLvmamaPackOrderPcRebate(OrdOrderDTO order,String channel,String categoryCode,MobileRebateRule rule,ProdProductAddtional addtionalRebate){
		long totalPcRebateAmount = 0;
		long quantity=0;
		//邮轮组合产品取份数
		if("category_comb_cruise".equalsIgnoreCase(categoryCode)){
			quantity =  calcCombCruiseQuantity(order);
		}else{
			quantity = calcOrderQuantity(order);
		}
		OrdOrderPack pack = order.getOrderPackList().get(0);
		Long productId = pack.getProductId();
		long orderProfit = calcOrderProfit(order,categoryCode);
		totalPcRebateAmount = suppGoodsRebateClientService.countProductPcRebate(orderProfit, quantity, productId);
		
		countMobileRebate(totalPcRebateAmount, calcOrderPackAmount(order), quantity, rule, addtionalRebate);
		logger.info("suppGoodsRebateClientService.countProductPcRebate="+totalPcRebateAmount);
		//关联销售
		List<OrdOrderItem>  OrdOrderItemList =  order.getNopackOrderItemList();
		if(OrdOrderItemList!=null){
			for(OrdOrderItem item:OrdOrderItemList){
				logger.info("nopackOrderItemOrderId="+item.getSuppGoodsId());
				long noPackItemRebateNow = calcOrderItemPcRebate(item,categoryCode);
				totalPcRebateAmount += noPackItemRebateNow;
				countMobileRebate(noPackItemRebateNow,item.getTotalAmount(), item.getQuantity(), rule, addtionalRebate);
			}
		}else{
			logger.info("getNopackOrderItemList is null");
		}
		return totalPcRebateAmount;
	}
	
	
	/**
	 * 取订单份数
	 * @param order
	 * @return
	 */
	private long calcOrderQuantity(OrdOrderDTO order){
		long quantity = 0;
		OrdOrderPack pack = order.getOrderPackList().get(0);
		Object adult = pack.getContentValueByKey(ORDER_TICKET_TYPE.adult_quantity.name());
		if(adult!=null){
			quantity+=Long.valueOf(adult.toString());
		}
		Object child = pack.getContentValueByKey(ORDER_TICKET_TYPE.child_quantity.name());
		if(child!=null){
			quantity+=Long.valueOf(child.toString());
		}
		if(quantity==0){
			Object quantityObj=pack.getContentValueByKey(ORDER_TICKET_TYPE.quantity.name());
			if(quantityObj!=null){
				quantity=Long.valueOf(quantityObj.toString());
			}
		}
		return quantity;
	}

	/**
	 * 计算邮轮组合产品房间份数
	 * @param order
	 * @return
	 */
	private long calcCombCruiseQuantity(OrdOrderDTO order) {
		long quantity = 0;
		OrdOrderPack pack = order.getOrderPackList().get(0);
		for(OrdOrderItem item :pack.getOrderItemList()){
			if(item.getCategoryId()==2)
				quantity+=item.getQuantity();
		}
		return quantity;
	}

	
	/**
	 * 计算子订单返现
	 * @param channel
	 * @param item
	 * @return
	 */
	private long calcOrderItemPcRebate(OrdOrderItem item,String categoryCode) {
		long totalPcRebateAmount = 0;
		//自主打包组合套餐票
		if("category_comb_ticket".equalsIgnoreCase(item.getSuppGoods().getProdProduct().getBizCategory().getCategoryCode())
				//&& "LVMAMA".equalsIgnoreCase(item.getSuppGoods().getProdProduct().getPackageType())
				){
			long  itemProfit = calcOrderItemProfit(item,false,categoryCode);
			long itemProductId = item.getSuppGoods().getProdProduct().getProductId();
			totalPcRebateAmount = suppGoodsRebateClientService.countProductPcRebate(itemProfit, item.getQuantity(), itemProductId);
		}else{
			SuppGoodsRebate rebate = suppGoodsRebateClientService.getGoodsRebateByGoodsIdChannel(item.getSuppGoodsId(), "pc").getReturnContent();
			if(rebate!=null){
			   totalPcRebateAmount=pcGoodsRebate(item, rebate,categoryCode);
			}
		}
		logger.info("calcOrderItemRebate result:"+totalPcRebateAmount);
		return totalPcRebateAmount;
	}
	
	/**
	 * 计算自主打包产品打包部分毛利
	 * @param packs
	 * @return
	 */
	private long calcOrderProfit(OrdOrder order,String categoryCode){
		List<OrdOrderPack> packs = order.getOrderPackList();
		List<OrdOrderItem> orderItemList = new ArrayList<OrdOrderItem>();
		long orderProfit=0;
		//自主打包产品只计算打包商品毛利
		if(packs!=null&&packs.size()>0){
			for(OrdOrderPack pack:packs){
				orderItemList.addAll(pack.getOrderItemList());
			 }
			 for(OrdOrderItem item:orderItemList){
				 	logger.info("itemGoodsId ="+item.getSuppGoodsId());
					orderProfit+=(item.getPrice()-item.getSettlementPrice())*item.getQuantity();
			}
		}
		logger.info("calcOrderProfit result=="+orderProfit);
		if(orderProfit<=0L){
			orderProfit = BigDecimal.valueOf(Math.ceil(calcOrderPackAmount(order)*gainDefaultRebate(categoryCode))).longValue();
		}
		return orderProfit;
	}
	//--------------计算打包的总金额-----------------------------------------------
	private long calcOrderPackAmount(OrdOrder order){
		List<OrdOrderPack> packs = order.getOrderPackList();
		List<OrdOrderItem> orderItemList = new ArrayList<OrdOrderItem>();
		long orderProfit=0;
		//自主打包产品只计算打包商品毛利
		if(packs!=null&&packs.size()>0){
			for(OrdOrderPack pack:packs){
				orderItemList.addAll(pack.getOrderItemList());
			 }
			 for(OrdOrderItem item:orderItemList){
				 	logger.info("itemGoodsId ="+item.getSuppGoodsId());
					orderProfit+=item.getPrice()*item.getQuantity();
			}
		}
		logger.info("calcOrderProfit result=="+orderProfit);
		return orderProfit;
	}
	//-----------------------------------------------------------
	/**
	 * 取订单下单渠道
	 * @param order
	 * @return
	 */
	private String getOrderChannel(OrdOrderDTO order) {
		Long channelId = order.getDistributionChannel()==null?-1:order.getDistributionChannel();
		Long distributorId = order.getDistributorId()==null?-1:order.getDistributorId();
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
			//是否到付门票
			boolean payTicketFlag = suppGoodsClientService.checkPayTicket(orderItem.getSuppGoodsId());
			//固定金额返现
			if(REBATE_TYPE.fixed.getCode().equals(rebate.getRebateType())){
				long fixedAmount = rebate.getFixedAmount()==null?0:rebate.getFixedAmount();
				if(payTicketFlag){
					//到付门票
					return fixedAmount;
				}else{
					//非到付门票固定金额返现返现金额*商品数量
					return (fixedAmount*orderItem.getQuantity());
				}
			}else if(rebate.getRebateType().equals(REBATE_TYPE.rate.getCode())){
				long profit = calcOrderItemProfit(orderItem,true,categoryCode);
				
				//Long rebateFen =new BigDecimal((profit*(rebate.getRateAmount().floatValue()/100))).setScale(0,BigDecimal.ROUND_HALF_UP).longValue();
				//long totalRebate = (rebateFen+99)/100*100;
				
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
					//	=new BigDecimal(Math.ceil( PriceUtil.convertToYuan(totalRebateAmount))*100).longValue();
				//--------------------------------------------------------------------------------------------
				
				return totalRebate;
			}
		}
		return 0;
	}

	/**
	 * 取子订单毛利
	 * @param orderItem
	 * calcSpread 是否排除房差
	 * @return
	 */
	private long calcOrderItemProfit(OrdOrderItem orderItem,boolean calcSpread,String categoryCode) {
		long spreadProfit=0;
		
		//单个商品返现=毛利*折扣比例
		if(calcSpread&&orderItem.getOrdMulPriceRateList()!=null){
			//房差售价
			long priceSpread=0;
			//房差结算价
			long settlementSpread=0;
			//房差数量
			long spreadQuantity=0;
			for(OrdMulPriceRate rate:orderItem.getOrdMulPriceRateList()){
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.name().equalsIgnoreCase(rate.getPriceType())){
					priceSpread=rate.getPrice();
					spreadQuantity= rate.getQuantity();
				}
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.name().equalsIgnoreCase(rate.getPriceType())){
					settlementSpread=rate.getPrice();
				}
			}
			//房差毛利
			spreadProfit= (priceSpread-settlementSpread)*spreadQuantity;
		}
		 
		//返现毛利=总毛利-房差毛利
		long profit = (orderItem.getPrice()-orderItem.getSettlementPrice())*orderItem.getQuantity()-spreadProfit;
		logger.info("calcOrderItemProfit result="+profit);
		if(profit<=0){
			profit=  BigDecimal.valueOf(Math.ceil(orderItem.getPrice()*orderItem.getQuantity()*gainDefaultRebate(categoryCode))).longValue();
		}
		
		logger.info(orderItem.getPrice()+"~~~~~~~calcOrderItemProfit~~~~~~~orderItem.getPrice()~~~~~~~");
		logger.info(categoryCode+"~~~~~~~calcOrderItemProfit~~~~~~~categoryCode~~~~~~~");
		
		logger.info(gainDefaultRebate(categoryCode)+"~~~~~~~calcOrderItemProfit~~~~~~~categoryCode~~~~~~~");
		return profit;
	}
	
	/**
	 * 验证品类是否为线路、组合套餐票、邮轮组合产品
	 * @param categoryCode
	 * @return
	 */
	public boolean isGroupProduct(String categoryCode){
		if(StringUtils.isNotEmpty(categoryCode)){
			if("category_route_group".equalsIgnoreCase(categoryCode)||"category_route_local".equalsIgnoreCase(categoryCode)||
					"category_route_hotelcomb".equalsIgnoreCase(categoryCode)||"category_route_new_hotelcomb".equalsIgnoreCase(categoryCode) ||"category_route_freedom".equalsIgnoreCase(categoryCode)
					||"category_comb_cruise".equalsIgnoreCase(categoryCode)||"category_comb_ticket".equalsIgnoreCase(categoryCode)
					||"category_traffic_aero_other".equalsIgnoreCase(categoryCode)||"category_traffic_bus_other".equalsIgnoreCase(categoryCode) || "category_route_customized".equalsIgnoreCase(categoryCode)){//新加定制游
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 输出orderItemList结构
	 * @param order
	 */
	public void showOrderItemListLog(OrdOrderDTO order){
		try {
			List<OrdOrderPack> packList = order.getOrderPackList();
			List<OrdOrderItem> itemList = null;
			if(packList!=null&&packList.size()>0){
				itemList = packList.get(0).getOrderItemList();
				if(itemList!=null){
					for(OrdOrderItem item:itemList){
						logger.info("packList.orderItemGoodId=="+item.getSuppGoodsId());
					}
				}else{
					logger.info("packList.get(0).getOrderItemList() is null");
				}
				
			}else{
				logger.info("order.getOrderPackList is null");
			}
			
			itemList = order.getNopackOrderItemList();
			if(itemList!=null){
				for(OrdOrderItem item:itemList){
					logger.info("nopackOrderItem.orderItemGoodId=="+item.getSuppGoodsId());
				}
			}else{
				logger.info("order.getNopackOrderItemList() is null");
			}
			
			itemList = order.getOrderItemList();
			if(itemList!=null){
				for(OrdOrderItem item:itemList){
					logger.info("getOrderItemList.orderItemGoodId=="+item.getSuppGoodsId());
				}
			}else{
				logger.info("order.getOrderItemList() is null");
			}
		} catch (Exception e) {
			logger.error(ExceptionFormatUtil.getTrace(e));
		}
		
		
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
