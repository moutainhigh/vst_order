package com.lvmama.vst.order.timeprice.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONArray;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice.StockReduceVO;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice.TimePriceCheckVO;
import com.lvmama.vst.back.goods.po.SuppGoodsSingleTimePrice;
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
import com.lvmama.vst.order.service.IOrderPriceUnitService;
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
@Service("orderSingleTimePriceService")
public class OrderSingleTimePriceServiceImpl extends AbstractOrderTimePriceService implements OrderTimePriceService {
	
	private static final String ADULT = "adult";
	
	private static final String CHILDREN = "children";
	
	private static final Log LOG = LogFactory.getLog(OrderSingleTimePriceServiceImpl.class);
	
	@Autowired
	private DistGoodsTimePriceClientService distGoodsTimePriceClientService;
	
	@Autowired
	private IOrderPriceUnitService orderPriceUnitService;
	
	@Resource(name="goodsOraSingleTimePriceStockService")
	private IGoodsTimePriceStockService goodsTimePriceStockService;
	
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
		
		if ((orderItem != null) && (item != null) && (ordOrderDTO != null)) {
			SuppGoodsSingleTimePrice suppGoodsSingleTimePrice = null;
			ResultHandleT<SuppGoodsSingleTimePrice> timePriceHolder = null;


			Date date = orderItem.getVisitTime();
			timePriceHolder = distGoodsTimePriceClientService.findSuppGoodsSingleTimePrice(ordOrderDTO.getDistributorId(), item.getGoodsId(), date);
			
			if ((timePriceHolder != null) && timePriceHolder.isSuccess() && (timePriceHolder.getReturnContent() != null)) {
				List<OrdOrderStock> orderStockList = new ArrayList<OrdOrderStock>();
				suppGoodsSingleTimePrice = timePriceHolder.getReturnContent();
				errorMsg = checkTimePriceTable(suppGoodsSingleTimePrice, orderItem, ordOrderDTO, OrderEnum.ORDER_STOCK_OBJECT_TYPE.ORDERITEM.name(), date, orderStockList);
				if (errorMsg == null) {
					//使用时间价格表填充订单子项
					errorMsg = accumulateOrderItemDataWithTimePrice(suppGoodsSingleTimePrice, orderItem);
					
					if (errorMsg == null) {
						//使用时间价格表填充订单
						OrderUtils.fillOrderWithTimePrice(suppGoodsSingleTimePrice, ordOrderDTO);

						orderItem.setOrderStockList(orderStockList);
						
						//设置订单子项资源状态
						OrderUtils.setOrderItemResourceStatusByOrderStockList(orderItem, orderStockList);
						
						orderItem.setCancelStrategy(suppGoodsSingleTimePrice.getCancelStrategy());
					}
				}
			} else {
				errorMsg = "您购买的商品中存在下架商品。";
			}
		} else {
			resultHandle.setMsg("您的订单不存在。");
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
	private String checkTimePriceTable(SuppGoodsSingleTimePrice timePrice, OrdOrderItem orderItem, OrdOrderDTO ordOrderDTO, String stockObjectType, Date visitTime, List<OrdOrderStock> orderStockList) {
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
	private String accumulateOrderItemDataWithTimePrice(SuppGoodsSingleTimePrice timePrice, OrdOrderItem orderItem) {
		String errorMsg = null;
		if ((orderItem != null) && (timePrice != null)) {
			long allPriceAmount = 0;
			long allSettlementPriceAmount = 0;
			long allMarketPriceAmount = 0;
			
			/*List<OrdItemPersonRelation> ordItemPersonRelationList = orderItem.getOrdItemPersonRelationList();
			if (ordItemPersonRelationList == null || ordItemPersonRelationList.isEmpty()) {
				errorMsg = "商品" + orderItem.getSuppGoodsName() + "没有入住（游玩）人。";
				return errorMsg;
			}*/
			
			if (errorMsg == null) {
				List<OrdMulPriceRate> ordMulPriceRateList = new ArrayList<OrdMulPriceRate>();
				int count = 0;
				/**
				 * 计价方式验证放在包验证中
				 * 
				String priceUnit = orderPriceUnitService.getPriceUnit(orderItem);
				
				if (OrderEnum.ORDER_PRICE_UNIT.UNIT_PERSON.name().equals(priceUnit)) {
					if (ordItemPersonRelationList.size() != orderItem.getQuantity()) {
						errorMsg = "人数和订购份数不相等。";
					}
					
					if (errorMsg == null) {
						Map<String, List<OrdPerson>> splitedPersonMap = splitTravllerrPerson(ordItemPersonRelationList);
						
						
						List<OrdPerson> ordAdultPersonList = splitedPersonMap.get(ADULT);
						if (ordAdultPersonList != null && !ordAdultPersonList.isEmpty()) {
							count = ordAdultPersonList.size();
							OrdMulPriceRate ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getAuditPrice(), new Long(count), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name());
							ordMulPriceRateList.add(ordMulPriceRate);
							
							allPriceAmount += timePrice.getAuditPrice() * count;
							allSettlementPriceAmount += timePrice.getAuditSettlementPrice() * count;
							allMarketPriceAmount += timePrice.getAuditMarketPrice() * count;
						}
						
						List<OrdPerson> ordChildrenPersonList = splitedPersonMap.get(ADULT);
						if (ordChildrenPersonList != null && !ordChildrenPersonList.isEmpty()) {
							count = ordChildrenPersonList.size();
							OrdMulPriceRate ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getChildPrice(), new Long(count), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD.name());
							ordMulPriceRateList.add(ordMulPriceRate);
							
							allPriceAmount += timePrice.getChildPrice() * count;
							allSettlementPriceAmount += timePrice.getChildSettlementPrice() * count;
							allMarketPriceAmount += timePrice.getChildMarketPrice() * count;
						}
					}
				} else if (OrderEnum.ORDER_PRICE_UNIT.UNIT_PORTION.name().equals(priceUnit)) {
					count = orderItem.getQuantity().intValue();
					OrdMulPriceRate ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getAuditPrice(), new Long(count), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name());
					ordMulPriceRateList.add(ordMulPriceRate);
					
					allPriceAmount += timePrice.getAuditPrice() * count;
					allSettlementPriceAmount += timePrice.getAuditSettlementPrice() * count;
					allMarketPriceAmount += timePrice.getAuditMarketPrice() * count;
				}
				**/
				
				Map<String, List<OrdPerson>> splitedPersonMap = null;
				if(CollectionUtils.isNotEmpty(orderItem.getOrdItemPersonRelationList())){
					splitedPersonMap = splitTravllerrPerson(orderItem.getOrdItemPersonRelationList());
				}else{
					BuyInfo.Item item= ((OrdOrderItemDTO)orderItem).getItem();
					splitedPersonMap = splitTravllerrPerson(item);
				}
				
				
				List<OrdPerson> ordAdultPersonList = splitedPersonMap.get(ADULT);
				if (ordAdultPersonList != null && !ordAdultPersonList.isEmpty()) {
					count = ordAdultPersonList.size();
					OrdMulPriceRate ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getAuditPrice(),  Long.valueOf(count), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name());
					ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.PRICE.name());
					ordMulPriceRateList.add(ordMulPriceRate);
					
					ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getAuditSettlementPrice(), Long.valueOf(count), OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.name());
					ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.SETTLEMENT.name());
					ordMulPriceRateList.add(ordMulPriceRate);
					
					ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getAuditMarketPrice(), Long.valueOf(count), OrderEnum.ORDER_PRICE_RATE_TYPE.MARKET_ADULT.name());
					ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.MARKET.name());
					ordMulPriceRateList.add(ordMulPriceRate);
					
					allPriceAmount += timePrice.getAuditPrice() * count;
					allSettlementPriceAmount += timePrice.getAuditSettlementPrice() * count;
					allMarketPriceAmount += timePrice.getAuditMarketPrice() * count;
				}
				
				List<OrdPerson> ordChildrenPersonList = splitedPersonMap.get(CHILDREN);
				if (ordChildrenPersonList != null && !ordChildrenPersonList.isEmpty()) {
					count = ordChildrenPersonList.size();
					OrdMulPriceRate ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getChildPrice(), new Long(count), OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD.name());
					ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.PRICE.name());
					ordMulPriceRateList.add(ordMulPriceRate);
					
					ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getChildSettlementPrice(), new Long(count), OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.name());
					ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.SETTLEMENT.name());
					ordMulPriceRateList.add(ordMulPriceRate);
					
					ordMulPriceRate = OrderUtils.makeOrdMulPriceRateRecord(timePrice.getChildMarketPrice(), new Long(count), OrderEnum.ORDER_PRICE_RATE_TYPE.MARKET_CHILD.name());
					ordMulPriceRate.setAmountType(OrdMulPriceRate.AmountType.MARKET.name());
					ordMulPriceRateList.add(ordMulPriceRate);
					
					allPriceAmount += timePrice.getChildPrice() * count;
					allSettlementPriceAmount += timePrice.getChildSettlementPrice() * count;
					allMarketPriceAmount += timePrice.getChildMarketPrice() * count;
				}
				
				if (errorMsg == null) {
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
					makeOrderItemTime(orderItem, timePrice);
				}
			}
		}
		
		return errorMsg;
	}
	
	private Map<String, List<OrdPerson>> splitTravllerrPerson(BuyInfo.Item item){
		Map<String, List<OrdPerson>> personMap = new HashMap<String, List<OrdPerson>>();
		if(item.getAdultQuantity()<1){
			throwIllegalException("成人数为空");
		}
		List<OrdPerson> list = new ArrayList<OrdPerson>();
		for(int i=0;i<item.getAdultQuantity();i++){
			list.add(new OrdPerson());
		}
		personMap.put(ADULT, list);
		
		if(item.getChildQuantity()>0){
			list = new ArrayList<OrdPerson>();
			for(int i=0;i<item.getChildQuantity();i++){
				list.add(new OrdPerson());
			}
			personMap.put(CHILDREN, list);
		}
		return personMap;
	}

	private Map<String, List<OrdPerson>> splitTravllerrPerson(List<OrdItemPersonRelation> ordItemPersonRelationList) {
		Map<String, List<OrdPerson>> personMap = new HashMap<String, List<OrdPerson>>();
		String peopleType = null;
		OrdPerson ordPerson = null;
		for (OrdItemPersonRelation ordItemPersonRelation : ordItemPersonRelationList) {
			if (ordItemPersonRelation != null && ordItemPersonRelation.getOrdPerson() != null) {
				ordPerson = ordItemPersonRelation.getOrdPerson();
				peopleType = ordPerson.getPeopleType();
				if (peopleType == null || !peopleType.equals(OrderEnum.ORDER_PERSON_PEOPLE_TYPE.PEOPLE_TYPE_CHILD.name())) {
					List<OrdPerson> ordAdultPersonList = personMap.get(ADULT);
					if (ordAdultPersonList == null) {
						ordAdultPersonList = new ArrayList<OrdPerson>();
						personMap.put(ADULT, ordAdultPersonList);
					}
					
					ordAdultPersonList.add(ordPerson);
				} else {
					List<OrdPerson> ordChildrenPersonList = personMap.get(CHILDREN);
					if (ordChildrenPersonList == null) {
						ordChildrenPersonList = new ArrayList<OrdPerson>();
						personMap.put(CHILDREN, ordChildrenPersonList);
					}
					
					ordChildrenPersonList.add(ordPerson);
				}
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
			resultHandleSuppGoodsBaseTimePrice.setMsg("商品ID=" + goodsId + ",date=" + specDate + ",checkAhead" + checkAhead + ",时间价格表SuppGoodsSingleTimePrice不存在。");
		}
		
		return resultHandleSuppGoodsBaseTimePrice;
	}

	@Override
	public void updateRevertStock(Long suppGoodsId, Date specDate, Long stock, Map<String, Object> dataMap) {
		if (suppGoodsId != null && specDate != null && stock != null) {
			if (stock > 0) {
				SuppGoodsBaseTimePrice suppGoodsBaseTimePrice = goodsTimePriceStockService.getTimePrice(suppGoodsId, specDate, false);
				if (suppGoodsBaseTimePrice != null) {
					SuppGoodsSingleTimePrice timePrice = (SuppGoodsSingleTimePrice) suppGoodsBaseTimePrice;
					if ("Y".equalsIgnoreCase(timePrice.getRestoreFlag())) {
						if (SuppGoodsSingleTimePrice.STOCKTYPE.FREESALE.name().equalsIgnoreCase(timePrice.getStockType())) {
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
				
				ResultHandleT<SuppGoodsSingleTimePrice> timePriceHolder = distGoodsTimePriceClientService.findSuppGoodsSingleTimePrice(distributionId, suppGoods.getSuppGoodsId(), visitDate);
				LOG.info("正在进行库存检查，该商品的价格类型为SingleTimePrice,得到的时间数据为 \n"+JSONArray.fromObject(timePriceHolder));
				if (timePriceHolder.isSuccess() && timePriceHolder.getReturnContent() != null) {
					SuppGoodsSingleTimePrice timePrice = timePriceHolder.getReturnContent();
					if (this.checkTimePrice(timePrice, currDate, (long) (item.getQuantity()))) {
						
					} else {
						LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
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
		return "LineSingleTimePrice";
	}

	@Override
	public void calcSettlementPromotion(OrdOrderItem orderItem,List<OrdPromotion> promotions) {
		long adultAmount = 0;
		long childAmount = 0;
		
		for(OrdPromotion op:promotions){
			FavorableAmount fa = op.getPromotion().getFavorableAmount();
			adultAmount += fa.getAdultAmount();
			childAmount += fa.getChildAmount();
		}
		
		long oldTalSettlementPrice = orderItem.getTotalSettlementPrice();
		long totalPrice = orderItem.getTotalSettlementPrice()-adultAmount-childAmount;
		if(totalPrice<0){
			totalPrice=0;
		}
		orderItem.setTotalSettlementPrice(totalPrice);
		orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
		
		if(orderItem.getOrdMulPriceRateList()!=null){
			for(OrdMulPriceRate rate:orderItem.getOrdMulPriceRateList()){
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.name().equalsIgnoreCase(rate.getPriceType())){
					if(adultAmount>0){
						long oldActualSettlementPrice =rate.getPrice(); 
						rate.setPrice(rate.getPrice()-adultAmount/rate.getQuantity());
						initOrdSettlementPriceRecord(rate.getPriceType(),oldActualSettlementPrice,oldTalSettlementPrice, orderItem);
					}
					
				}else if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.name().equalsIgnoreCase(rate.getPriceType())){
					if(childAmount>0){
						long oldActualSettlementPrice =rate.getPrice(); 
						rate.setPrice(rate.getPrice()-childAmount/rate.getQuantity());
						initOrdSettlementPriceRecord(rate.getPriceType(),oldActualSettlementPrice,oldTalSettlementPrice, orderItem);
					}
					
				}
			}
		}
		
		
	}
}
