package com.lvmama.vst.order.service.book.destbu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.comm.pet.po.user.UserUser;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsRebateClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prom.service.PromForbidBuyClientService;
import com.lvmama.vst.back.client.prom.service.PromotionService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_TICKET_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.REBATE_TYPE;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductAddtional;
import com.lvmama.vst.back.prom.po.MobileRebateRule;
import com.lvmama.vst.back.prom.po.PromForbidKeyPo;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.back.prom.po.SuppGoodsRebate;
import com.lvmama.vst.back.prom.rule.IPromFavorable;
import com.lvmama.vst.back.prom.rule.PromFavorableFactory;
import com.lvmama.vst.back.prom.vo.PromForbidBuyQuery;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.DefaultRebateConfig;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.BuyInfo.Product;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.NewHotelComOrderBussiness;
import com.lvmama.vst.order.service.book.impl.OrderRebateBussinessImpl;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderPackDTO;
import com.lvmama.vst.pet.adapter.UserUserProxyAdapter;

@Component("newHotelComOrderBussiness")
public class NewHotelComOrderBussinessImpl extends AbstractBookService  implements NewHotelComOrderBussiness {
	private static final Logger logger = LoggerFactory.getLogger(NewHotelComOrderBussinessImpl.class);	
	@Autowired
	private CategoryClientService categoryClientService;
	@Autowired
	private SuppGoodsRebateClientService suppGoodsRebateClientService;
	@Autowired  
	private SuppGoodsClientService suppGoodsClientService;
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private PromForbidBuyClientService  promForbidBuyClientService;
	
	@Autowired
	private UserUserProxyAdapter userProxyAdapter;
	
	@Autowired
	private PromotionService promotionService;
	
	//@Autowired
	private PromFavorableFactory promFavorableFactory;
	
	@Override
	public void DestBucalcRebate(OrdOrderDTO order) throws BusinessException {
 try {
			
		//	showOrderItemListLog(order);
			long totalRebateAmount=0L;
			long totalPcRebateAmount=0;
			long totalMobileRebateAmount=0;
			List<OrdOrderItem>  OrdOrderItemList =null;
			//下单渠道
			String channel = getOrderChannel(order);
			if(channel.equals("")){
				 throw new BusinessException("下单渠道未明");
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
	
			
			
			if("category_route_new_hotelcomb".equals(categoryCode)){
		
			
				//酒套餐
				
					totalPcRebateAmount = calcSupplierPackOrderPcRebate(order,channel,categoryCode,rebateRule,addtionalRebate);
				
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
			logger.info("保存订单返现信息totalRebateAmount="+totalPcRebateAmount);
			//保存订单返现信息
			order.setRebateAmount(totalRebateAmount);
			order.setRebateFlag("N");
		} catch (Exception e) {
			logger.error("计算订单返现异常",e);
			 throw new BusinessException("计算订单返现异常");
		}
		
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
		/**
		 * 供应商打包产品pc端返现计算
		 * @param order
		 * @param channel
		 * @return
		 */
		private long 	calcSupplierPackOrderPcRebate(OrdOrderDTO order,String channel,String categoryCode,MobileRebateRule rule,ProdProductAddtional addtionalRebate) {
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
		private double gainDefaultRebate(String categoryCode){
			return DefaultRebateConfig.getInstance().getDefaultRebateByCategory(categoryCode);
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

		@Override
		public PromForbidKeyPo isPromForbidBuyOrder(DestBuBuyInfo buyInfo) {
			// TODO Auto-generated method stub
			PromForbidKeyPo promForbidKeyPo = null;
			logger.info("start  isPromForbidBuyOrder");
			//默认不属于限购
			//boolean  checkBuy = false;
			try
			{
				PromForbidBuyQuery query = new PromForbidBuyQuery();
				Long categoryId = 0L;
				Long productId=0L;
				logger.info("buyInfo  productid is"+buyInfo.getProductId());
				
				ProdProduct prodProduct = null;
				if(null !=buyInfo.getProductId()){
//					prodProduct = prodProductClientService.findProdProductById(buyInfo.getProductId()).getReturnContent();
					prodProduct = prodProductClientService.findProdProductByIdFromCache(buyInfo.getProductId()).getReturnContent();
					categoryId = prodProduct.getBizCategoryId();
					productId = buyInfo.getProductId();
				}else
				{
					Long mainSuppGoodsId= 0L;
					if(CollectionUtils.isNotEmpty(buyInfo.getItemList())){
						for (com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo.Item item : buyInfo.getItemList()) {		
							if("true".equals(item.getMainItem())){
								mainSuppGoodsId = item.getGoodsId();
								break;
							}		
						}
					}
					
					if(mainSuppGoodsId.intValue() !=0){				
						ResultHandleT<SuppGoods> resultHandle = suppGoodsClientService.findSuppGoodsById(mainSuppGoodsId);
						if(null !=resultHandle.getReturnContent()){
							SuppGoods suppGoods = resultHandle.getReturnContent();
							categoryId = suppGoods.getCategoryId();
						}
					}

				}
				
				
				if(categoryId.intValue()>0){				
					query = toPromForbidBuy(buyInfo,categoryId,productId);
					logger.info("query to sring"+query.toString());
					promForbidKeyPo = promForbidBuyClientService.checkExistRestrainedBuy(query);
					logger.info("promForbidBuyClientService  checkExistRestrainedBuy return"+buyInfo.getUserId()+"promForbidKeyPo.tostring::"+promForbidKeyPo.toString());
					return promForbidKeyPo;
				}else
				{	
					logger.error("限购检查异常:categoryId is 0");
				}		
			}catch(Exception e){
				//checkBuy = false;
				logger.error("限购检查异常", e);
				e.printStackTrace();
			}	
			return promForbidKeyPo;
		}
		
		 public  PromForbidBuyQuery toPromForbidBuy(DestBuBuyInfo buyInfo,Long categoryId,Long productId){
			 logger.info("start toPromForbidBuy is and categoryId is"+categoryId);
		    	PromForbidBuyQuery query = new PromForbidBuyQuery();
				query.setObjectIds(new ArrayList<Long>());
				query.setCategoryId(categoryId);
		       
				logger.info("其它产品验证 ");
					if(null!=buyInfo.getProductId()){
						query.getObjectIds().add(buyInfo.getProductId());
					}else{
						query.getObjectIds().add(productId);
					}
					
					query.setObjectType("PRODUCT");
				
				query.setVisitDate(DateUtil.toDate(getVisitTime(buyInfo, categoryId), "yyyy-MM-dd"));
				List<Person> travellers = buyInfo.getTravellers();
				if(CollectionUtils.isNotEmpty(travellers))
				{
					List<String> phoneNums = new ArrayList<String>();
					List<Map<String, String>> certificateMapList = new ArrayList<Map<String,String>>();
					for (Person person : travellers) {
						Map<String, String> certificateMap = new HashMap<String, String>();
						if(StringUtils.isNotEmpty(person.getMobile())){
							phoneNums.add(person.getMobile());
						}		
						certificateMap.put("certificateType", person.getIdType());
						certificateMap.put("certificateVal", person.getIdNo());
						certificateMapList.add(certificateMap);
					}
						query.setPhoneNumbers(phoneNums);
					
					query.setCertificateMap(certificateMapList);
				}
				if(null != buyInfo.getUserNo()){
					logger.info("buyInfo userNo is "+buyInfo.getUserNo());
					UserUser user = userProxyAdapter.getUserUserByPk(buyInfo.getUserNo());
					if(user!=null){
						query.setUserName(user.getUserName());
						query.setUserNo(user.getId());
					}
				}else{
					logger.info("buyInfo userNo is null");
				}
				
				if(StringUtils.isNotEmpty(buyInfo.getMobileEquipmentNo())){
					logger.info("buyInfo MobileEquipmentNo is "+buyInfo.getMobileEquipmentNo());
					query.setMobileId(buyInfo.getMobileEquipmentNo());
				}
				//设置渠道的对应匹配的这张表PROM_FORBID_BUY 的渠道数字
				/*
				 * 后台---2
				 * 前台---3
				 * 无线---4
				 * 兴旅同业 ---5
				 * 特卖会---6
				 * 其他分销---7
				 * 
				 * **/
				if(null != buyInfo.getDistributionId()){
					logger.info("buyInfo distributionId is " + buyInfo.getDistributionId());
					Long buyinfoDistributionId =buyInfo.getDistributionId();
					
					if(buyinfoDistributionId==1L||buyinfoDistributionId==2L||buyinfoDistributionId==3L||buyinfoDistributionId==5L){
						logger.info("buyInfo distributionId 1235");
						query.setDistributorId(buyInfo.getDistributionId());
					}
					else if(buyinfoDistributionId==4L && null != buyInfo.getDistributionChannel()){
						 Long distributionChannel = buyInfo.getDistributionChannel();
						 if(distributionChannel==10000){
							 logger.info("distributionChannel 10000");
							 query.setDistributorId(4L);
						 }
						 else if(distributionChannel==10001||distributionChannel==10002||distributionChannel==108||distributionChannel==110){
							 logger.info("distributionChannel 10001");
							 query.setDistributorId(6L);
						 }
						 else{
							 logger.info("distributionChannel else 7L");
							 query.setDistributorId(7L);
						 }
					} else if(buyinfoDistributionId==6L){
						logger.info("distributionChannel else 8L");
		                query.setDistributorId(8L);
					}else if(buyinfoDistributionId == 21L){
						//立体设备使用  不限购
						logger.info("distributionChannel else 21L");
		                query.setDistributorId(21L);
					}else{
						logger.info("distributionChannel else waiceng 7L");
						 query.setDistributorId(7L);
					}

				}
				
				if(null != buyInfo.getDistributionChannel()){
					logger.info("buyInfo distributionChannel is " + buyInfo.getDistributionChannel());
					query.setDistributionChannel(buyInfo.getDistributionChannel());
				}
				
				if(null!=buyInfo.getMobileEquipmentNo()){
					logger.info("buyInfo MobileEquipmentNo is " + buyInfo.getMobileEquipmentNo());
					query.setMobileEquipmentNo(buyInfo.getMobileEquipmentNo());
				}
				
					query.setQuantity(buyInfo.getQuantity());
					Map<Long,Integer> goodsIdsAndquantity =new HashMap<Long, Integer>();
					Map<String,List<Long>> typeWithids =new HashMap<String, List<Long>>();
					
					List<Long> ids=new ArrayList<Long>();
					List<Long> idsproduct=new ArrayList<Long>();
					idsproduct.add(productId);
				
					if(CollectionUtils.isNotEmpty(buyInfo.getItemList())){
						for (com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo.Item item : buyInfo.getItemList()) {		
							if(item.getQuantity()>0){
								goodsIdsAndquantity.put(item.getGoodsId(), item.getQuantity());
								ids.add(item.getGoodsId());
							}
						}
						typeWithids.put("GOODS",ids);
						typeWithids.put("PRODUCT",idsproduct);
						goodsIdsAndquantity.put(productId, 1);
					}
					
					query.setIdsWhitsType(typeWithids);
					query.setGoodsIdsAndquantity(goodsIdsAndquantity);
					query.setCreateDate(new Date());
					logger.info("IdsWhitsType ==" +typeWithids +"goodsIdsAndquantity" +goodsIdsAndquantity);
					//设置是否限购
					query.setIsUseForbidBuy(buyInfo.getIsUseForbidBuy());
				return query;

		}
		 public String getVisitTime(DestBuBuyInfo buyInfo,Long categoryId){
		    	String visitTime = null;
		    	visitTime = buyInfo.getVisitTime();
		    	if(StringUtils.isEmpty(visitTime)){
		    		
		    		if(StringUtils.isEmpty(visitTime) && CollectionUtils.isNotEmpty(buyInfo.getItemList())){
						visitTime = buyInfo.getItemList().get(0).getVisitTime();
					}
		    	}  	
		    	logger.info(" visitTime is "+visitTime);
		    	return visitTime;
		    }
		@Override
		public List<OrdPromotion> initPromotion(OrdOrderDTO order, String key, List<Long> promotionIds) {
			String[] array = StringUtils.split(key,"_");
			if(array.length!=5){
				throwIllegalException("促销信息错误");
			}
			Long objectId=NumberUtils.toLong(array[1]);
			List<OrdPromotion> promotionList = new ArrayList<OrdPromotion>();
			for(Long promId:promotionIds){
				
			
				logger.info("promotionService.getPromPromotionById params promId="+promId+",objectId+"+objectId+",array="+array[2]);
				PromPromotion promotion = promotionService.getPromPromotionById(promId, objectId, array[2],order.getUserNo());
				if(promotion==null){
					logger.info("promotion is null promid="+promId);
					continue;
				}
				
				IPromFavorable ipf = fillFavorableData(order, promotion);
				
				OrdPromotion op = new OrdPromotion();
				//检查促销可用余额是否满足
				if(checkOrderChannel(order)&& promotion.getPromAmount()!=null&&"DISTRIBUTOR_TYPE".equals(promotion.getPriceType())){
					long usedAmount = promotion.getUsedAmount()==null?0L:promotion.getUsedAmount();
					long balance =promotion.getPromAmount()-usedAmount;
					//活动可用余额大于等于促销金额才存
					if(balance<ipf.getDiscountAmount()){
						continue;
					}
					//占用促销额度标记
					op.setOccupyAmountFlag("Y");	
				}
				
				
				op.setCode(promotion.getCode());
				op.setPromPromotionId(promotion.getPromPromotionId());
				op.setPriceType(promotion.getPriceType());
				op.setPromTitle(promotion.getTitle());
				op.setObjectType(OrdPromotion.ObjectType.ORDER_PACK.name());
				op.setTarget(null);
				op.setPromFavorable(ipf);
				op.setPromotion(promotion);
				promotionList.add(op);
			}
			return promotionList; 

		}
		@Override
		public IPromFavorable fillFavorableData(Object obj, PromPromotion promotion) {
	
			logger.info("$$--------$$" + "fillFavorableData() start ->");
			
			if(promotion==null){
				if (logger.isDebugEnabled()) {
					logger.debug("fillFavorableData(Object, PromPromotion) - promotion==null"); //$NON-NLS-1$
				}
				throwNullException("促销不存在");
			}
			if(obj==null){
				if (logger.isDebugEnabled()) {
					logger.debug("fillFavorableData(Object, PromPromotion) - obj==null"); //$NON-NLS-1$
				}
				throwNullException("促销数据对象不存在");
			}
			promotion.setPromResult(promotionService.getPromResultByPromotionId(promotion.getPromPromotionId()));
			IPromFavorable ipf = promFavorableFactory.createFavorable(promotion);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderDate", new Date());
				
			OrdOrderDTO order = (OrdOrderDTO) obj;
			if(!checkPromValid(promotion,order.getVisitTime())){
				if (logger.isDebugEnabled()) {
					logger.debug("fillFavorableData(Object, PromPromotion) - checkPromValid==false"); //$NON-NLS-1$
				}
				throwIllegalException("促销不满足使用");
			}
			params.put("visitDate", order.getVisitTime());
			calcRouteAmount(order ,params, promotion);
			params.put("categoryIsRoute", false);
			ipf.setData(params);
			
			logger.info("$$--------$$" + "fillFavorableData() end ->");
			
			return ipf;
		}
		/**
		 * 验证下单渠道是否为前台和手机端
		 * @param order
		 * @return
		 */
		public boolean checkOrderChannel(OrdOrderDTO order){
			Long distributorId = order.getDistributorId()==null?-1:order.getDistributorId();
			Long channelId = order.getDistributionChannel()==null?-1:order.getDistributionChannel();
			boolean check=false;
			if(distributorId==3){
				check=true;
			}
			if(distributorId==4){
				if(channelId==10000||channelId==10001||channelId==10002||channelId==107||channelId==108||channelId==110){
					check=true;
				}
			}
			return check;
		}
		
		
		private boolean checkPromValid(PromPromotion prom,Date visitTime){
			if(prom.getStartVistTime()!=null){
				if(visitTime.before(prom.getStartVistTime())){
					return false;
				}
			}
			if(prom.getEndVistTime()!=null){
				if(visitTime.after(prom.getEndVistTime())){
					return false;
				}
			}
			
			return true;
		}
		
		
		
		private void calcRouteAmount(OrdOrderDTO order, Map<String,Object> params, PromPromotion promPromotion) {
			
			logger.info("$$--------$$" + "calcRouteAmount() start ->");
			

			
			long totalPrice=0;
			long adultPrice=0;
			long childPrice=0;
			long noMulPrice=0;
		
	
				logger.info("$$--------$$" + "existAdultChild(pack.getProduct()) = false" );
				if(BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(order.getCategoryId())){
					Long quantity = null ;
			
					
					for(OrdOrderItem orderItem:order.getOrderItemList()){
						//次规格产品不参与促销
						if("Y".equals(orderItem.getSuppGoods().getProdProductBranch().getBizBranch().getAttachFlag())){
							totalPrice+=orderItem.getPrice()*orderItem.getQuantity();
							
						}
						adultPrice = totalPrice/orderItem.getQuantity();
					}
					
					params.put("adultQuantity", quantity);
				}
				
			
			
			
			params.put("noMulPrice", noMulPrice);
			params.put("adultPrice", adultPrice);
			logger.info("$$--------$$" + "param(adultPrice)=" + adultPrice);
			params.put("childQuantity", 0);
		//	logger.info("$$--------$$" + "param(childQuantity)=" + pack.getContentValueByKey(ORDER_TICKET_TYPE.child_quantity.name()));
			
			params.put("childPrice", childPrice);
			logger.info("$$--------$$" + "param(childPrice)=" + childPrice);
			params.put("categoryIsRoute", false);
			
			logger.info("$$--------$$" + "calcRouteAmount() end ->");
			
		}
		public void afterPropertiesSet() throws Exception {
			promFavorableFactory = new PromFavorableFactory();
		}
		
	
}
