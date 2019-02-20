package com.lvmama.vst.order.timeprice.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.lvmama.vst.back.goods.vo.CuriseHoldPeopleNumber;

import net.sf.json.JSONArray;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.client.prod.curise.service.ProdCuriseProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice.StockReduceVO;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice.TimePriceCheckVO;
import com.lvmama.vst.back.goods.po.SuppGoodsMultiTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prom.rule.favor.FavorableAmount;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.order.timeprice.service.AbstractOrderTimePriceService;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;

/**
 * 
 * @author sunjian
 *
 */
@Service("orderMultiTimePriceService")
public class OrderMultiTimePriceServiceImpl extends AbstractOrderTimePriceService implements OrderTimePriceService {
	
	private static final String FIRST_SENCOND_ADULT = "first_sencond_adult";
	
	private static final String THIRD_FOURTH_ADULT = "third_fourth_adult";
	
	private static final String THIRD_FOURTH_CHILDREN = "third_fourth_children";
	
	private static final Log LOG = LogFactory.getLog(OrderMultiTimePriceServiceImpl.class);
	
	@Autowired
	private DistGoodsTimePriceClientService distGoodsTimePriceClientService;
	
	@Resource(name="goodsOraMultiTimePriceStockService")
	private IGoodsTimePriceStockService goodsTimePriceStockService;
	
	@Autowired
	private ProdCuriseProductClientService prodCuriseProductClientService;

	@Override
	public void updateStock(Long timePriceId, Long stock, Map<String, Object> dataMap) {
		if (timePriceId != null && stock != null) {
			if (!goodsTimePriceStockService.updateStock(timePriceId, -stock)) {
				throw new RuntimeException("更新TimePrice库存失败。timePriceId=" + timePriceId);
			}
		}
	}

	@Override
	public ResultHandle validate(SuppGoods suppGoods, Item item, OrdOrderItemDTO orderItem, OrdOrderDTO ordOrderDTO) {
		ResultHandle resultHandle = new ResultHandle();
		String errorMsg = null;
		
		if ((suppGoods != null) && (orderItem != null) && (item != null) && (ordOrderDTO != null)) {
			SuppGoodsMultiTimePrice suppGoodsMultiTimePrice = null;
			ResultHandleT<SuppGoodsMultiTimePrice> timePriceHolder = null;


			Date date = orderItem.getVisitTime();
			timePriceHolder = distGoodsTimePriceClientService.findSuppGoodsMultiTimePrice(ordOrderDTO.getDistributorId(), item.getGoodsId(), date);
			
			if ((timePriceHolder != null) && timePriceHolder.isSuccess() && (timePriceHolder.getReturnContent() != null)) {
				List<OrdOrderStock> orderStockList = new ArrayList<OrdOrderStock>();
				suppGoodsMultiTimePrice = timePriceHolder.getReturnContent();
				errorMsg = checkTimePriceTable(suppGoodsMultiTimePrice, orderItem, ordOrderDTO, OrderEnum.ORDER_STOCK_OBJECT_TYPE.ORDERITEM.name(), date, orderStockList);
				if (errorMsg == null) {
					//使用时间价格表填充订单子项
					errorMsg = accumulateOrderItemDataWithTimePrice(suppGoodsMultiTimePrice, orderItem);
					
					if (errorMsg == null) {
						//使用时间价格表填充订单
						OrderUtils.fillOrderWithTimePrice(suppGoodsMultiTimePrice, ordOrderDTO);

						orderItem.setOrderStockList(orderStockList);
						
						//设置订单子项资源状态
						OrderUtils.setOrderItemResourceStatusByOrderStockList(orderItem, orderStockList);
						
						orderItem.setCancelStrategy(suppGoodsMultiTimePrice.getCancelStrategy());
					}
					
				}
			} else {
				errorMsg = "您购买的商品中存在下架商品。";
			}
		} else {
			errorMsg = "您的订单不存在。";
		}
		
		if (errorMsg != null) {
			resultHandle.setMsg(errorMsg);
		}
			
		return resultHandle;
	}
	
	/**
	 * 验证价格时间表中的库存
	 * 
	 * @param timePrice
	 * @param orderItem
	 * @param ordOrderDTO
	 * @param stockObjectType
	 * @param visitTime
	 * @param orderStockList
	 * @return
	 */
	private String checkTimePriceTable(SuppGoodsMultiTimePrice timePrice, OrdOrderItem orderItem, OrdOrderDTO ordOrderDTO, String stockObjectType, Date visitTime, List<OrdOrderStock> orderStockList) {
		String errorMsg = null;
		//订单本地库存记录
		OrdOrderStock ordOrderStock = null;
		//是否需要资源确认
		String needResourceConfirm = null;
		//资源状态
		String resourceStatus = null;
		//下单类型
		String inventory = null;
		
		TimePriceCheckVO checkVO = timePrice.checkTimePriceForOrder(ordOrderDTO.getCreateTime(), orderItem.getQuantity());
		if (checkVO != null) {
			LOG.debug("OrderValidCheckBussiness.checkTimePriceTable:checkVO.isOrderAble=" + checkVO.isOrderAble());
			if (checkVO.isOrderAble()) {
				if (checkVO.getStockReduceList() != null && checkVO.getStockReduceList().size() > 0) {
					for (StockReduceVO stockReduce : checkVO.getStockReduceList()) {
						if (stockReduce != null) {
							LOG.debug("OrderValidCheckBussiness.checkTimePriceTable:stockReduce[isResourceConfirm=" + stockReduce.isResourceConfirm() + 
									",isReduceStock=" + stockReduce.isReduceStock() + ", ReduceType=" + stockReduce.getReduceType() + "]");
							
							//需要资源确认
							if (stockReduce.isResourceConfirm()) {
								//需要资源确认
								needResourceConfirm = "true";
								//需要资源审核
								resourceStatus = OrderEnum.RESOURCE_STATUS.UNVERIFIED.name();
							//不需要资源确认
							} else {
								//不需要资源确认
								needResourceConfirm = "false";
								//资源审核通过
								resourceStatus = OrderEnum.RESOURCE_STATUS.AMPLE.name();
							}
							
							//是否要减库存
							if (stockReduce.isReduceStock()) {
								//有库存下单
								inventory = OrderEnum.INVENTORY_STATUS.INVENTORY.name();
								//将减少的库存添加到缓存映射表中，方便后续更新库存
								ordOrderDTO.addUpdateStock(timePrice, stockReduce.getStock(), this);
							} else {
								//无库存下单
								inventory = OrderEnum.INVENTORY_STATUS.UNINVENTORY.name();
							}
							//FREESALE的强制为FREESALE下单
							if (stockReduce.getReduceType() == SuppGoodsTimePrice.REDUCETYPE.FREESALE) {
								inventory = OrderEnum.INVENTORY_STATUS.FREESALE.name();
							}
							
							//添加一条订单库存记录
							ordOrderStock = OrderUtils.makeOrdOrderStockRecord(stockObjectType, visitTime, stockReduce.getStock(), inventory, needResourceConfirm, resourceStatus);
							orderStockList.add(ordOrderStock);
						} else {
							errorMsg = "库存未知，无法下单。";
							LOG.debug("OrderValidCheckBussiness.checkTimePriceTable:stockReduce=null,msg=" + errorMsg);
						}
					}
				} else {
					errorMsg = "库存异常，无法下单。";
					if (checkVO.getStockReduceList() == null) {
						LOG.debug("OrderValidCheckBussiness.checkTimePriceTable:checkVO.getStockReduceList()=null,msg=" + errorMsg);
					} else {
						LOG.debug("OrderValidCheckBussiness.checkTimePriceTable:checkVO.getStockReduceList().size()=" + checkVO.getStockReduceList().size() + ",msg=" + errorMsg);
					}
				}
			} else {
				errorMsg = checkVO.getNotAbleReason();
				LOG.info("OrderValidCheckBussiness.checkTimePriceTable: checkVO.isOrderAble()=false,msg=" + errorMsg);
			}
		} else {
			errorMsg = "库存检验失败，无法下单。";
			LOG.debug("OrderValidCheckBussiness.checkTimePriceTable: checkVO=null,msg=" + errorMsg);
		}
		
		return errorMsg;
	}
	
	/**
	 * 订单子项各个逻辑项累加
	 * 
	 * @param timePrice
	 * @param orderItem
	 */
	private String accumulateOrderItemDataWithTimePrice(SuppGoodsMultiTimePrice timePrice, OrdOrderItemDTO orderItem) {
		String errorMsg = null;
		if ((orderItem != null) && (timePrice != null)) {
			long adultPersonCount12 = 0;
			long adultPersonCount34 = 0;
			long childrenPersonCount34 = 0;
			long bedPersonCount=0;
			long allPriceAmount = 0;
			long allSettlementPriceAmount = 0;
			long allMarketPriceAmount = 0;
			long houseNum=orderItem.getQuantity();//当前子订单(商品)仓房订购数量
			long child=orderItem.getItem().getChildQuantity();
			long adult=orderItem.getItem().getAdultQuantity();
			
			/*List<OrdItemPersonRelation> ordItemPersonRelationList = orderItem.getOrdItemPersonRelationList();
			if (ordItemPersonRelationList == null || ordItemPersonRelationList.isEmpty()) {
				errorMsg = "商品" + orderItem.getSuppGoodsName() + "没有入住（游玩）人。";
				return errorMsg;
			}*/
			
			if (errorMsg == null) {
				List<OrdMulPriceRate> ordMulPriceRateList = new ArrayList<OrdMulPriceRate>();

				ResultHandleT<CuriseHoldPeopleNumber> resultHandleLong = prodCuriseProductClientService.getCuriseMaxHoldPeopleNumber(orderItem.getCategoryId(), orderItem.getBranchId());
				if (resultHandleLong.isSuccess()) {
					long maxPersonCount = resultHandleLong.getReturnContent().getMaxHoldPeopleNumber().intValue();
					long minPersonCount = resultHandleLong.getReturnContent().getMinHoldPeopleNumber().intValue();
					//根据子订单中的 最大最小入住人数以及子订单中的成人数儿童数验证房间有效信息
					errorMsg=checkCurrnItemtotalPersonforHouseNum(adult,child,maxPersonCount,minPersonCount,houseNum,orderItem);
					
					if(errorMsg != null){
						return errorMsg;
					}
					orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.maxPersonCount.name(), (maxPersonCount + ""));
						//当前商品成人数和儿童数之和
						long totalPerson=adult+child;
						//需要支付的床位费人数=房间数*房间最大入住人数-总人数
						bedPersonCount=houseNum*maxPersonCount-totalPerson;
						
						if(houseNum * 2 >= totalPerson){
							//支付第一二成人价人数等于总人数
							adultPersonCount12 = totalPerson;
				        }else{
				            if((totalPerson - houseNum*2) <= child){
				            	adultPersonCount12=houseNum*2;//需要支付成人价的份数等于房间数*2
				            	childrenPersonCount34=totalPerson -houseNum*2;//需要支付三四儿童人数=总人数-支付一二人价格的人数
				            }else{
				            	adultPersonCount12=houseNum*2;//需要支付成人价的份数等于房间数*2
				            	childrenPersonCount34=child;//需要支付三四儿童价格
				            	adultPersonCount34=adult -houseNum*2;
				                
				            }
				        }
						
						
				
				
				} else {
					errorMsg = resultHandleLong.getMsg();
				}
				
				if (errorMsg == null) {
					//第一二人价格
					OrdMulPriceRate ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getFstPrice(),  Long.valueOf(adultPersonCount12), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT_12.name());
					ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.PRICE.name());
					ordMulPriceRateList.add(ordMulPriceRate);
					
					ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getFstSettlementPrice(), Long.valueOf(adultPersonCount12), OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.name());
					ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.SETTLEMENT.name());
					ordMulPriceRateList.add(ordMulPriceRate);
					
					ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getFstMarketPrice(), Long.valueOf(adultPersonCount12), OrderEnum.ORDER_PRICE_RATE_TYPE.MARKET_ADULT_12.name());
					ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.MARKET.name());
					ordMulPriceRateList.add(ordMulPriceRate);
					
					allPriceAmount += timePrice.getFstPrice() * adultPersonCount12;
					allSettlementPriceAmount += timePrice.getFstSettlementPrice() * adultPersonCount12;
					allMarketPriceAmount += timePrice.getFstMarketPrice() * adultPersonCount12;
					
					//第三四成人价格
					if (adultPersonCount34 > 0) {
						if(timePrice.getSecPrice()==null){
							timePrice.setSecPrice(timePrice.getFstPrice());
							timePrice.setSecMarketPrice(timePrice.getFstMarketPrice());
							timePrice.setSecSettlementPrice(timePrice.getFstSettlementPrice());
						}
						ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getSecPrice(), new Long(adultPersonCount34), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT_34.name());
						ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.PRICE.name());
						ordMulPriceRateList.add(ordMulPriceRate);
						
						ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getSecSettlementPrice(), new Long(adultPersonCount34), OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_34.name());
						ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.SETTLEMENT.name());
						ordMulPriceRateList.add(ordMulPriceRate);
						
						ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getSecMarketPrice(), new Long(adultPersonCount34), OrderEnum.ORDER_PRICE_RATE_TYPE.MARKET_ADULT_34.name());
						ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.MARKET.name());
						ordMulPriceRateList.add(ordMulPriceRate);
						
						allPriceAmount += timePrice.getSecPrice() * adultPersonCount34;
						allSettlementPriceAmount += timePrice.getSecSettlementPrice() * adultPersonCount34;
						allMarketPriceAmount += timePrice.getSecMarketPrice() * adultPersonCount34;
					}
					
					//第三四儿童价格
					if (childrenPersonCount34 > 0) {
						if(timePrice.getChildPrice()==null){
							timePrice.setChildPrice(timePrice.getFstPrice());
							timePrice.setChildMarketPrice(timePrice.getFstMarketPrice());
							timePrice.setChildSettlementPrice(timePrice.getFstSettlementPrice());
						}
						ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getChildPrice(), new Long(childrenPersonCount34), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD_34.name());
						ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.PRICE.name());
						ordMulPriceRateList.add(ordMulPriceRate);
						
						ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getChildSettlementPrice(), new Long(childrenPersonCount34), OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.name());
						ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.SETTLEMENT.name());
						ordMulPriceRateList.add(ordMulPriceRate);
						
						ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getChildMarketPrice(), new Long(childrenPersonCount34), OrderEnum.ORDER_PRICE_RATE_TYPE.MARKET_CHILD_34.name());
						ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.MARKET.name());
						ordMulPriceRateList.add(ordMulPriceRate);
						
						allPriceAmount += timePrice.getChildPrice() * childrenPersonCount34;
						allSettlementPriceAmount += timePrice.getChildSettlementPrice() * childrenPersonCount34;
						allMarketPriceAmount += timePrice.getChildMarketPrice() * childrenPersonCount34;
					}
					
					//床位费
					if (bedPersonCount > 0) {
						if(timePrice.getGapPrice()==null){
							timePrice.setGapPrice(timePrice.getFstPrice());
							timePrice.setGapMarketPrice(timePrice.getFstMarketPrice());
							timePrice.setGapSettlementPrice(timePrice.getFstSettlementPrice());
						}
						ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getGapPrice(), new Long(bedPersonCount), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_GAP.name());
						ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.PRICE.name());
						ordMulPriceRateList.add(ordMulPriceRate);
						
						ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getGapSettlementPrice(), new Long(bedPersonCount), OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_GAP.name());
						ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.SETTLEMENT.name());
						ordMulPriceRateList.add(ordMulPriceRate);
						
						ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getGapMarketPrice(), new Long(bedPersonCount), OrderEnum.ORDER_PRICE_RATE_TYPE.MARKET_GAP.name());
						ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.MARKET.name());
						ordMulPriceRateList.add(ordMulPriceRate);
						
						allPriceAmount += timePrice.getGapPrice() * bedPersonCount;
						allSettlementPriceAmount += timePrice.getGapSettlementPrice() * bedPersonCount;
						allMarketPriceAmount += timePrice.getGapMarketPrice() * bedPersonCount;
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
					
					// 最晚取消时间
					makeOrderItemTime(orderItem,timePrice);
				}
			}
			
		} else {
			errorMsg = "订单不存在。";
		}
		
		return errorMsg;
	}
	/***
	 * 根据子订单中的 最大最小入住人数以及子订单中的成人数儿童数验证房间有效信息
	 * @param adult
	 * @param child
	 * @param maxPersonCount
	 * @param minPersonCount
	 * @param houseNum
	 * @param orderItem
	 * @return
	 */
	private String checkCurrnItemtotalPersonforHouseNum(long adult, long child,
			long maxPersonCount, long minPersonCount,long houseNum,OrdOrderItemDTO orderItem) {
		String erroMessage=null;
		if(adult<=0){//成人数小于等于0
			erroMessage=orderItem.getSuppGoodsName()+":成人数不符合下单要求";
			return erroMessage;
		}
		if(houseNum<=0){//房间数小于等于0
			erroMessage=orderItem.getSuppGoodsName()+":房间订购数量不能为0";
			return erroMessage;
		}
		if(adult<houseNum){//成人数小于房间数
			erroMessage=orderItem.getSuppGoodsName()+":成人数不能小于房间订购数量";
			return erroMessage;
		}
		double agvPerson=((double)adult+child)/houseNum;
		if(agvPerson<minPersonCount){//均入住人数少于该舱房最小入住人数
			erroMessage=orderItem.getSuppGoodsName()+":平均入住人数少于该舱房最小入住人数"+minPersonCount+"人";
			return erroMessage;
		}
		if(agvPerson>maxPersonCount){//平均入住人数大于该舱房最大入住人数
			erroMessage=orderItem.getSuppGoodsName()+":平均入住人数大于该舱房最大入住人数"+maxPersonCount+"人";
			return erroMessage;
		}
		return erroMessage;
	}

	private Map<String, List<OrdPerson>> splitTravllerrPerson(List<OrdPerson> ordPersonList) {
		Map<String, List<OrdPerson>> personMap = new HashMap<String, List<OrdPerson>>();
		String peopleType = null;
		for (OrdPerson ordPeson : ordPersonList) {
			if (ordPeson != null) {
				peopleType = ordPeson.getPeopleType();
				if (peopleType == null || !peopleType.equals(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_CHILD.name())) {
					List<OrdPerson> ordPersonList1_2 = personMap.get(FIRST_SENCOND_ADULT);
					if (ordPersonList1_2 == null) {
						ordPersonList1_2 = new ArrayList<OrdPerson>();
						ordPersonList1_2.add(ordPeson);
						personMap.put(FIRST_SENCOND_ADULT, ordPersonList1_2);
					} else {
						if (ordPersonList1_2.size() < 2) {
							ordPersonList1_2.add(ordPeson);
						} else {
							List<OrdPerson> ordPersonList3_4 = personMap.get(THIRD_FOURTH_ADULT);
							if (ordPersonList3_4 == null) {
								ordPersonList3_4 = new ArrayList<OrdPerson>();
								ordPersonList3_4.add(ordPeson);
								personMap.put(THIRD_FOURTH_ADULT, ordPersonList3_4);
							} else {
								if (ordPersonList3_4.size() < 2) {
									ordPersonList3_4.add(ordPeson);
								} else {
									throw new IllegalArgumentException("成人人数与时间价格表SUPP_GOODS_MULTI_TIME_PRICE不匹配。");
								}
							}
						}
					}
				} else {
					List<OrdPerson> ordPersonListChild3_4 = personMap.get(THIRD_FOURTH_CHILDREN);
					if (ordPersonListChild3_4 == null) {
						ordPersonListChild3_4 = new ArrayList<OrdPerson>();
						ordPersonListChild3_4.add(ordPeson);
						personMap.put(THIRD_FOURTH_CHILDREN, ordPersonListChild3_4);
					} else {
						if (ordPersonListChild3_4.size() < 2) {
							ordPersonListChild3_4.add(ordPeson);
						} else {
							throw new IllegalArgumentException("儿童人数与时间价格表SUPP_GOODS_MULTI_TIME_PRICE不匹配。");
						}
					}
				}
			}
		}
		
		List<OrdPerson> ordPersonList1_2 = personMap.get(FIRST_SENCOND_ADULT);
		if (ordPersonList1_2 == null || ordPersonList1_2.isEmpty()) {
			throw new IllegalArgumentException("不存在第一二成年人。");
		}
		
		if (ordPersonList1_2.size() < 2) {
			List<OrdPerson> ordPersonListChild3_4 = personMap.get(THIRD_FOURTH_CHILDREN);
			if (ordPersonListChild3_4 != null && !ordPersonListChild3_4.isEmpty()) {
				ordPersonList1_2.add(ordPersonListChild3_4.remove(0));
				if (ordPersonListChild3_4.isEmpty()) {
					personMap.remove(THIRD_FOURTH_CHILDREN);
				}
			}
		}
		
		return personMap;
	}
	
	private void splitTravllerrPersonByAdultAndChildren(List<OrdItemPersonRelation> ordItemPersonRelationList, List<OrdPerson> ordAdultPersonList, List<OrdPerson> ordChildrenPersonList) {
		String peopleType = null;
		if (ordItemPersonRelationList != null && !ordItemPersonRelationList.isEmpty()) {
			for (OrdItemPersonRelation ordItemPersonRelation : ordItemPersonRelationList) {
				if (ordItemPersonRelation != null && ordItemPersonRelation.getOrdPerson() != null) {
					OrdPerson ordPerson = ordItemPersonRelation.getOrdPerson();
					peopleType = ordPerson.getPeopleType();
					if (peopleType == null || !peopleType.equals(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_CHILD.name())) {
						ordAdultPersonList.add(ordPerson);
					} else {
						ordChildrenPersonList.add(ordPerson);
					}
				}
			}
		}
		
	}
	
	private void splitTravllerrPersonByAdultAndChildren(BuyInfo.Item item, List<OrdPerson> ordAdultPersonList, List<OrdPerson> ordChildrenPersonList) {
		if(item.getAdultQuantity()<1){
			throwIllegalException("成人数不可以为空");
		}
		for(int i=0;i<item.getAdultQuantity();i++){
			ordAdultPersonList.add(new OrdPerson());
		}
		
		for(int i=0;i<item.getChildQuantity();i++){
			ordChildrenPersonList.add(new OrdPerson());
		}
	}
	
	private List<Map<String, List<OrdPerson>>> splitTravllerrPersonWithSequence(OrdOrderItemDTO orderItem, long quantity, int maxPersonCount) {
		
		List<Map<String, List<OrdPerson>>> splitedSequencePersonList = new ArrayList<Map<String, List<OrdPerson>>>();
		List<OrdPerson> ordAdultPersonList = new ArrayList<OrdPerson>();
		List<OrdPerson> ordChildrenPersonList = new ArrayList<OrdPerson>();
		
		Map<String, List<OrdPerson>> personMap = null;
		List<OrdPerson> personList = null;
		
		int quantityIndex = 0;
		int personCountInRoom = 0;
		
		List<OrdItemPersonRelation> ordItemPersonRelationList = orderItem.getOrdItemPersonRelationList();
		if(CollectionUtils.isNotEmpty(ordItemPersonRelationList)){
			splitTravllerrPersonByAdultAndChildren(ordItemPersonRelationList, ordAdultPersonList, ordChildrenPersonList);
		}else if(orderItem.getItem().getAdultQuantity()>0){
			splitTravllerrPersonByAdultAndChildren(orderItem.getItem(),ordAdultPersonList, ordChildrenPersonList);
		}else{
			throwIllegalException("参数不正确");
		}

		
		for (int k = 0; k < quantity; k++) {
			personMap = new HashMap<String, List<OrdPerson>>();
			splitedSequencePersonList.add(personMap);
		}
		
		for (OrdPerson ordPerson : ordAdultPersonList) {
			personMap = splitedSequencePersonList.get(quantityIndex);
			personCountInRoom = computeAllPersonCount(personMap);
			if (personCountInRoom >= maxPersonCount) {
				throwIllegalException("人数超过最大入住人数。");
			}
			
			//第一二人成人
			personList = personMap.get(FIRST_SENCOND_ADULT);
			if (personList == null) {
				personList = new ArrayList<OrdPerson>();
				personMap.put(FIRST_SENCOND_ADULT, personList);
			}
			
			if (personList.size() < 2) {
				personList.add(ordPerson);
				quantityIndex = (int) ((quantityIndex + 1) % quantity);
				continue;
			}
			
			//第三四人成人
			personList = personMap.get(THIRD_FOURTH_ADULT);
			if (personList == null) {
				personList = new ArrayList<OrdPerson>();
				personMap.put(THIRD_FOURTH_ADULT, personList);
			}
			
			personList.add(ordPerson);
			quantityIndex = (int) ((quantityIndex + 1) % quantity);
		}
		
		for (OrdPerson ordPerson : ordChildrenPersonList) {
			personMap = splitedSequencePersonList.get(quantityIndex);
			personCountInRoom = computeAllPersonCount(personMap);
			if (personCountInRoom >= maxPersonCount) {
				throwIllegalException("人数超过最大入住人数。");
			}
			
			//第一二人儿童
			personList = personMap.get(FIRST_SENCOND_ADULT);
			if (personList == null || personList.isEmpty()) {
				throwIllegalException("人数和订购数量不匹配（儿童不能单独入住一个房间）。");
			}
			
			if (personList.size() < 2) {
				personList.add(ordPerson);
				quantityIndex = (int) ((quantityIndex + 1) % quantity);
				continue;
			}
			
			//第三四人儿童
			personList = personMap.get(THIRD_FOURTH_CHILDREN);
			if (personList == null) {
				personList = new ArrayList<OrdPerson>();
				personMap.put(THIRD_FOURTH_CHILDREN, personList);
			}
			
			personList.add(ordPerson);
			quantityIndex = (int) ((quantityIndex + 1) % quantity);
		}
		
		return splitedSequencePersonList;
	}
	
	private int computeAllPersonCount(Map<String, List<OrdPerson>> itemPersonMap) {
		int personCount = 0;
		
		List<OrdPerson> adultPerson12List = itemPersonMap.get(FIRST_SENCOND_ADULT);
		if (adultPerson12List != null) {
			personCount = personCount + adultPerson12List.size();
		}
		
		List<OrdPerson> adultPerson34List = itemPersonMap.get(THIRD_FOURTH_ADULT);
		if (adultPerson34List != null) {
			personCount = personCount + adultPerson34List.size();
		}
		
		List<OrdPerson> childrenPerson34List = itemPersonMap.get(THIRD_FOURTH_CHILDREN);
		if (childrenPerson34List != null) {
			personCount = personCount + childrenPerson34List.size();
		}
		
		return personCount;
	}
	
	private Map<String, List<OrdPerson>> findMinPersonInPersonList(List<Map<String, List<OrdPerson>>> sequencePersonList) {
		Map<String, List<OrdPerson>> personMap = null;
		Map<String, List<OrdPerson>> itemPersonMap = null;
		int minPersonCount = 0;
		int personCount = 0;
		for (int i = 0; i < sequencePersonList.size(); i++) {
			itemPersonMap = sequencePersonList.get(i);
			personCount = computeAllPersonCount(itemPersonMap);
			if (0 == i) {
				minPersonCount = personCount;
				personMap = itemPersonMap;
			} else if (personCount < minPersonCount) {
				minPersonCount = personCount;
				personMap = itemPersonMap;
			}
		}
		
		return personMap;
	}
	
	@Override
	public ResultHandleT<SuppGoodsBaseTimePrice> getTimePrice(Long goodsId, Date specDate, boolean checkAhead) {
		ResultHandleT<SuppGoodsBaseTimePrice> resultHandleSuppGoodsBaseTimePrice = new ResultHandleT<SuppGoodsBaseTimePrice>();
		
		SuppGoodsBaseTimePrice suppGoodsBaseTimePrice = goodsTimePriceStockService.getTimePrice(goodsId, specDate, checkAhead);
		if (suppGoodsBaseTimePrice != null) {
			resultHandleSuppGoodsBaseTimePrice.setReturnContent(suppGoodsBaseTimePrice);
		} else {
			resultHandleSuppGoodsBaseTimePrice.setMsg("商品ID=" + goodsId + ",date=" + specDate + ",checkAhead" + checkAhead + ",时间价格表SuppGoodsMultiTimePrice不存在。");
		}
		
		return resultHandleSuppGoodsBaseTimePrice;
	}

	@Override
	public void updateRevertStock(Long suppGoodsId, Date specDate, Long stock, Map<String, Object> dataMap) {
		if (suppGoodsId != null && specDate != null && stock != null) {
			if (stock > 0) {
				SuppGoodsBaseTimePrice suppGoodsBaseTimePrice = goodsTimePriceStockService.getTimePrice(suppGoodsId, specDate, false);
				if (suppGoodsBaseTimePrice != null) {
					SuppGoodsMultiTimePrice timePrice = (SuppGoodsMultiTimePrice) suppGoodsBaseTimePrice;
					if ("Y".equalsIgnoreCase(timePrice.getRestoreFlag())) {
						if (SuppGoodsMultiTimePrice.STOCKTYPE.FREESALE.name().equalsIgnoreCase(timePrice.getStockType())) {
							LOG.info("method:updateRevertStock[库存返回],message=商品（ID=" + suppGoodsId + "）日期（"  + specDate + ")时间价格表已经FreeSale状态。");
						} else {
							//恢复库存
							goodsTimePriceStockService.updateStock(timePrice.getTimePriceId(), stock);
						}
					}
				} else {
					throw new IllegalArgumentException("商品(ID=" + suppGoodsId + ")，时间(" + specDate + ")时间价格表不存在。");
				}
			} else {
				throw new IllegalArgumentException("返回的库存数要大于0(stock=" + stock + ")");
			}
		}
	}

	@Override
	public ResultHandleT<Object> checkStock(SuppGoods suppGoods, Item item, Long distributionId, Map<String, Object> dataMap) {

		ResultHandleT<Object> resultHandleT = new ResultHandleT<Object>();
		
		try {
			//商品是否可售
			if (suppGoods != null && suppGoods.isValid()) {
				
				//商品参数验证
				checkParam(suppGoods, item, true);
				
				Date currDate = new Date();
				Date visitDate = item.getVisitTimeDate();
				
				ResultHandleT<SuppGoodsMultiTimePrice> timePriceHolder = distGoodsTimePriceClientService.findSuppGoodsMultiTimePrice(distributionId, suppGoods.getSuppGoodsId(), visitDate);
				LOG.info("正在进行库存检查，该商品的价格类型为MultiTimePrice,得到的时间数据为 \n"+JSONArray.fromObject(timePriceHolder));
				if (timePriceHolder.isSuccess() && timePriceHolder.getReturnContent() != null) {
					SuppGoodsMultiTimePrice timePrice = timePriceHolder.getReturnContent();
					if (this.checkTimePrice(timePrice, currDate, (long) (item.getQuantity()))) {
						
					} else {
						LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + ",时间价格表ID=" + timePrice.getTimePriceId() + "库存不足。");
						resultHandleT.setMsg("商品 " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
					}
				} else {
					LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + ",时间" + currDate + "时间价格表不存在。");
					resultHandleT.setMsg("商品 " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")时间价格表不存在。");
				}
			}
		} catch (Exception e) {
			resultHandleT.setMsg(e.getMessage());
		}
		
		return resultHandleT;
	}

	@Override
	public String getTimePricePrefix() {
		return "CuriseTimePrice";
	}

	@Override
	public void calcSettlementPromotion(OrdOrderItem orderItem,List<OrdPromotion> promotions) {
		long adultAmount12 = 0;
		long adultAmount34 = 0;
		long childAmount34 = 0;
		
		for(OrdPromotion op:promotions){
			FavorableAmount fa = op.getPromotion().getFavorableAmount();
			adultAmount12 += fa.getAdultAmount12();
			adultAmount34 += fa.getAdultAmount34();
			childAmount34 += fa.getChildAmount34();
		}
		
		long oldTalSettlementPrice = orderItem.getTotalSettlementPrice();
		long totalPrice =orderItem.getTotalSettlementPrice()-adultAmount12-adultAmount34-childAmount34;
		if(totalPrice<0){
			totalPrice=0;
		}
		orderItem.setTotalSettlementPrice(totalPrice);
		orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
		
		
		if(orderItem.getOrdMulPriceRateList()!=null){
			for(OrdMulPriceRate rate:orderItem.getOrdMulPriceRateList()){
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.name().equalsIgnoreCase(rate.getPriceType())){
					if(adultAmount12>0){
						long oldActualSettlementPrice =rate.getPrice(); 
						rate.setPrice(rate.getPrice()-adultAmount12/(orderItem.getQuantity()*2));
						initOrdSettlementPriceRecord(rate.getPriceType(),oldActualSettlementPrice,oldTalSettlementPrice, orderItem);

					}
					
				}else if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_34.name().equalsIgnoreCase(rate.getPriceType())){
					if(adultAmount34>0){
						long oldActualSettlementPrice =rate.getPrice(); 
						rate.setPrice(rate.getPrice()-adultAmount34/rate.getQuantity());
						initOrdSettlementPriceRecord(rate.getPriceType(),oldActualSettlementPrice,oldTalSettlementPrice, orderItem);
					}
					
				}else if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.name().equalsIgnoreCase(rate.getPriceType())){
					if(childAmount34>0){
						long oldActualSettlementPrice =rate.getPrice(); 
						rate.setPrice(rate.getPrice()-childAmount34/rate.getQuantity());
						initOrdSettlementPriceRecord(rate.getPriceType(),oldActualSettlementPrice,oldTalSettlementPrice, orderItem);
					}
					
				}
			}
		}
	}
}
