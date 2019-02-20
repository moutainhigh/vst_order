package com.lvmama.vst.order.timeprice.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONArray;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.dist.service.DistGoodsTimePriceClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.dist.po.TimePrice;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice.StockReduceVO;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice.TimePriceCheckVO;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.service.IGoodsTimePriceStockService;
import com.lvmama.vst.back.order.dao.OrdOrderGroupStockDao;
import com.lvmama.vst.back.order.po.OrdOrderGroupStock;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prom.rule.favor.FavorableAmount;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.HotelAdditation;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.order.dao.OrdOrderSharedStockDao;
import com.lvmama.vst.order.timeprice.service.AbstractOrderTimePriceService;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.utils.PropertiesUtil;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;
import com.lvmama.vst.pet.goods.PetProdGoodsAdapter;

@Service("orderPreSaleTimePriceService")
public class OrderPreSaleTimePriceServiceImpl extends AbstractOrderTimePriceService implements OrderTimePriceService{
	
	private static final Log LOG = LogFactory.getLog(OrderPreSaleTimePriceServiceImpl.class);
	
	@Autowired
	private DistGoodsTimePriceClientService distGoodsTimePriceClientService;
	
	@Autowired
	private PetProdGoodsAdapter petProdGoodsAdapter;
	
	@Autowired
	protected SuppGoodsClientService suppGoodsClientService;
	
	@Resource(name="goodsOraTimePriceStockService")
	private IGoodsTimePriceStockService goodsTimePriceStockService;
	
	@Autowired
	private  OrdOrderGroupStockDao ordOrderGroupStockDao;
	@Autowired
	private ResPreControlService resControlBudgetRemote;
    @Autowired
    private OrdOrderSharedStockDao ordOrderSharedStockDao;

    @Resource(name="goodsOraTimePriceStockService")
    protected IGoodsTimePriceStockService iGoodsTimePriceStockService;

	@Override
	public void updateStock(Long timePriceId, Long stock, Map<String, Object> dataMap) {
	   LOG.info("购买预售券下单");
	}
	
	private void accumulateOrderItemDataWithTimePrice(TimePrice timePrice, OrdOrderItem orderItem) {
		if ((orderItem != null) && (timePrice != null)) {
			// 单价
			if (orderItem.getPrice() == null) {
				orderItem.setPrice(timePrice.getPrice());
			} else {
				orderItem.setPrice(orderItem.getPrice() + timePrice.getPrice());
			}

			// 结算单价
			if (orderItem.getSettlementPrice() == null) {
				orderItem.setSettlementPrice(timePrice.getSettlementPrice());
			} else {
				orderItem.setSettlementPrice(orderItem.getSettlementPrice() + timePrice.getSettlementPrice());
			}

			// 实际结算单价
			orderItem.setActualSettlementPrice(orderItem.getSettlementPrice());

			// 市场单价（先默认值0）
			orderItem.setMarketPrice(0L);
			
			// 最晚取消时间
			makeOrderItemTime(orderItem,timePrice);
		}
	}

	@Override
	public ResultHandle validate(SuppGoods suppGoods, Item item, OrdOrderItemDTO orderItem, OrdOrderDTO ordOrderDTO) {
		ResultHandle resultHandle = new ResultHandle();
		String errorMsg = null;
		OrdOrderDTO order =  ordOrderDTO;
		if ((orderItem != null) && (item != null)) {
			orderItem.setPrice(NumberUtils.toLong(item.getPrice()));
			orderItem.setActualSettlementPrice(NumberUtils.toLong(item.getSettlementPrice()));
			orderItem.setSettlementPrice(NumberUtils.toLong(item.getSettlementPrice()));
			orderItem.setResourceStatus("AMPLE");
			ordOrderDTO.setResourceStatus("AMPLE");
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
	private String checkTimePriceTable(TimePrice timePrice, OrdOrderItem orderItem, OrdOrderDTO ordOrderDTO, String stockObjectType, Date visitTime, List<OrdOrderStock> orderStockList) {
		String errorMsg = null;
		//订单本地库存记录
		OrdOrderStock ordOrderStock = null;
		//是否需要资源确认
		String needResourceConfirm = null;
		//资源状态
		String resourceStatus = null;
		//下单类型
		String inventory = null;

        LOG.info("#######酒店通过日期判定是否有共享库存库存########");
        Long groupId = orderItem.getSuppGoods().getGroupId();
        Long shareStock = null;
        LOG.info("SuppGoodsId"+orderItem.getSuppGoodsId());
        if(null != groupId){
            LOG.info("查询共享库存：groupId="+groupId+"    visitTime="+visitTime);
            shareStock = iGoodsTimePriceStockService.getShareStock(groupId,visitTime);
            LOG.info("visitTime="+visitTime+"    groupId="+groupId+"     shareStock="+shareStock);
        }
        TimePriceCheckVO checkVO = null;
        if(null != shareStock){
            checkVO = timePrice.checkTimePriceForShareOrder(new Date(), (long) (orderItem.getQuantity()), shareStock);
        }else{
        	// 新增供应商对接酒店 一次最大可预订房间数
        	Long supplierId = orderItem.getSupplierId();
        	if (supplierId != null && !supplierId.equals(0)) {
        		String maxBookingStock = PropertiesUtil.getValue("maxQuantity_" + supplierId);
        		if (StringUtils.isNumeric(maxBookingStock)) {
        			long maxBookingStockLong = Long.valueOf(maxBookingStock);
        			if (timePrice.getStock() > maxBookingStockLong) {
        				LOG.info("供应商ID:[" + supplierId + "]对接酒店，一次最大可预订房间数为:[" + maxBookingStock + "]");
        				timePrice.setStock(maxBookingStockLong);
					}
				}
			}
            checkVO = timePrice.checkTimePriceForOrder(new Date(), (long) (orderItem.getQuantity()));
        }
		
		//TimePriceCheckVO checkVO = timePrice.checkTimePriceForOrder(ordOrderDTO.getCreateTime(), orderItem.getQuantity());
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
	 * 设置担保类型
	 * 
	 * @param orderItem
	 * @param item
	 * @param timePrice
	 * @return
	 */
	private ResultHandleT<TimePrice> setHotelOrderItemGuaranteeInfo(OrdOrderItem orderItem, Item item, TimePrice timePrice) {
		ResultHandleT<TimePrice> guaranteeTimePriceHolder = new ResultHandleT<TimePrice>();
		
		if (item.getHotelAdditation() != null) {
			HotelAdditation hotelAdditation = item.getHotelAdditation();
			String bookLimitType = timePrice.getBookLimitType();
			//全程担保
			if (SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equalsIgnoreCase(bookLimitType)) {
				orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.ALLTIMEGUARANTEE.name());
				guaranteeTimePriceHolder.setReturnContent(timePrice);
			//一律担保
			} else if (SuppGoodsTimePrice.BOOKLIMITTYPE.ALLGUARANTEE.name().equalsIgnoreCase(bookLimitType)) {
				orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.ALLGUARANTEE.name());
				guaranteeTimePriceHolder.setReturnContent(timePrice);
			//超时担保
			} else if (SuppGoodsTimePrice.BOOKLIMITTYPE.TIMEOUTGUARANTEE.name().equalsIgnoreCase(bookLimitType)) {
				//默认无限制
				orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.NONE.name());
				if (timePrice.getLatestUnguarTime() != null && timePrice.getLatestUnguarTime() > 0) {
					int totalMinute = 0;
					String arrivaltime = hotelAdditation.getArrivalTime();
					String[] timeStrings = arrivaltime.split(":");
					int hour = Integer.valueOf(timeStrings[0]).intValue();
					int minute = Integer.valueOf(timeStrings[1]).intValue();
					totalMinute = hour * 60 + minute;
					
					if (totalMinute > timePrice.getLatestUnguarTime() * 60) {
						//超时担保
						orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.TIMEOUTGUARANTEE.name());
						guaranteeTimePriceHolder.setReturnContent(timePrice);
					} 
					
//					if (timePrice.getLatestUnguarTime() > 0) {
//						int totalMinute = 0;
//						String arrivaltime = hotelAdditation.getArrivalTime();
//						String[] timeStrings = arrivaltime.split(":");
//						int hour = Integer.valueOf(timeStrings[0]).intValue();
//						int minute = Integer.valueOf(timeStrings[1]).intValue();
//						totalMinute = hour * 60 + minute;
//						
//						if (totalMinute > timePrice.getLatestUnguarTime() * 60) {
//							//超时担保
//							orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.TIMEOUTGUARANTEE.name());
//							guaranteeTimePriceHolder.setReturnContent(timePrice);
//							//扣款规则
//							orderItem.setDeductType(timePrice.getDeductType());
//						} 
//					} 
//					else {
//						errorMsg = "时间价格表（ID=" + timePrice.getTimePriceId() + "）最晚保留时间为" + timePrice.getLatestUnguarTime();
//						guaranteeTimePriceHolder.setMsg(errorMsg);
//						LOG.info("method processHotelTimePriceTable: " + errorMsg);
//					}
				}
			//房量担保
			} else if (timePrice.getGuarQuantity() != null && timePrice.getGuarQuantity() > 0) {
				//默认无限制
				orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.NONE.name());
				if (orderItem.getQuantity() > timePrice.getGuarQuantity()) {
					//房量担保
					orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.QUANTITYGUARANTEE.name());
					guaranteeTimePriceHolder.setReturnContent(timePrice);
				}
			} else if (bookLimitType == null || SuppGoodsTimePrice.BOOKLIMITTYPE.NONE.name().equalsIgnoreCase(bookLimitType)) {
				//默认无限制
				orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.NONE.name());
			}
		}
		
		return guaranteeTimePriceHolder;
	}
	
	/**
	 * 根据OrdOrderHotelTimeRate列表中的各个OrdOrderStock状态，设置订单子项资源状态
	 * 
	 * @param orderItem
	 * @param hotelRateTimeList
	 */
	private void setOrderItemResourceStatusByHotelRateTimeList(OrdOrderItem orderItem, List<OrdOrderHotelTimeRate> hotelRateTimeList) {
		if (orderItem != null && hotelRateTimeList != null) {
			for (OrdOrderHotelTimeRate hotelTimeRate : hotelRateTimeList) {
				OrderUtils.setOrderItemResourceStatusByOrderStockList(orderItem, hotelTimeRate.getOrderStockList());
			}
		}
	}
	
	/**
	 * 在时间价格表List中，找出退改最大的时间价格表
	 * 
	 * @param orderItem
	 * @param timePriceList
	 * @param everydayTimePriceList
	 * @return
	 */
	private TimePrice getMaxDeductAmountTimePrice(OrdOrderItem orderItem, List<TimePrice> timePriceList, List<TimePrice> everydayTimePriceList) {
		TimePrice maxDeductTimePrice = null;
		long maxDeductAmount = -1;
		long deductAmount = -1;
		for (TimePrice timePrice : timePriceList) {
			deductAmount = computeOrderItemDeductAmount(orderItem, timePrice, everydayTimePriceList);
			if (maxDeductTimePrice == null) {
				maxDeductTimePrice = timePrice;
				maxDeductAmount = deductAmount;
			} else {
				if (deductAmount > maxDeductAmount) {
					maxDeductTimePrice = timePrice;
					maxDeductAmount = deductAmount;
				}
			}
		}
		
		return maxDeductTimePrice;
	}
	
	/**
	 * 计算退改金额
	 * 
	 * @param orderItem
	 * @param applyTimePrice
	 * @param everydayTimePriceList
	 * @return
	 */
	private long computeOrderItemDeductAmount(OrdOrderItem orderItem, TimePrice applyTimePrice, List<TimePrice> everydayTimePriceList) {
		long deductAmount = 0;
		if (applyTimePrice.getDeductType() != null) {
			if (SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.FULL.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
				deductAmount = orderItem.getPrice() * orderItem.getQuantity();
				/*Long deductBuyoutAmout = orderItem.getBuyoutPrice();
				if(deductBuyoutAmout!=null){
					orderItem.setDeductBuyoutAmout(deductBuyoutAmout);
				}*/
				
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.FIRSTDAY.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
				if (everydayTimePriceList.get(0) != null) {
					deductAmount = everydayTimePriceList.get(0).getPrice() * orderItem.getQuantity();
					
					/*Long buyoutPrice = everydayTimePriceList.get(0).getBuyoutPrice();
					buyoutPrice = buyoutPrice== null ? everydayTimePriceList.get(0).getPrice() : buyoutPrice;
					orderItem.setDeductBuyoutAmout(buyoutPrice * orderItem.getQuantity());*/
				}
				
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.MONEY.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
				deductAmount = applyTimePrice.getDeductValue() * orderItem.getQuantity();
				/*orderItem.setDeductBuyoutAmout(deductAmount);*/
				
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.PERCENT.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
				deductAmount = (long) ((orderItem.getPrice() * orderItem.getQuantity()) * applyTimePrice.getDeductValue() / 100.0 + 0.5);
				/*Long deductBuyoutAmout = orderItem.getBuyoutPrice();
				if(deductBuyoutAmout!=null){
					orderItem.setDeductBuyoutAmout((long)(deductBuyoutAmout* applyTimePrice.getDeductValue() / 100.0 + 0.5));
				}*/
				
			} else {
				throw new IllegalArgumentException("TimePrice(ID=" + applyTimePrice.getTimePriceId() + ")'s getDeductValue=" + applyTimePrice.getDeductType() + ", is illegal.");
			}
		} else {
			LOG.info("OrderValidCheckBussiness.computeOrderItemDeductAmount: TimePrice(ID=" + applyTimePrice.getTimePriceId() + ")'s getDeductValue=null.");
		}
		
		return deductAmount;
	}

	@Override
	public ResultHandleT<SuppGoodsBaseTimePrice> getTimePrice(Long goodsId, Date specDate, boolean checkAhead) {
		ResultHandleT<SuppGoodsBaseTimePrice> resultHandleSuppGoodsBaseTimePrice = new ResultHandleT<SuppGoodsBaseTimePrice>();
		
		SuppGoodsBaseTimePrice suppGoodsBaseTimePrice = goodsTimePriceStockService.getTimePrice(goodsId, specDate, checkAhead);
		if (suppGoodsBaseTimePrice != null) {
			resultHandleSuppGoodsBaseTimePrice.setReturnContent(suppGoodsBaseTimePrice);
		} else {
			resultHandleSuppGoodsBaseTimePrice.setMsg("商品ID=" + goodsId + ",date=" + specDate + ",checkAhead" + checkAhead + ",时间价格表SuppGoodsTimePrice不存在。");
		}
		
		return resultHandleSuppGoodsBaseTimePrice;
	}

	@Override
	public void updateRevertStock(Long suppGoodsId, Date specDate, Long stock, Map<String, Object> dataMap) {
		if (suppGoodsId != null && specDate != null && stock != null) {
			if (stock > 0) {
				SuppGoodsBaseTimePrice suppGoodsBaseTimePrice = goodsTimePriceStockService.getTimePrice(suppGoodsId, specDate, false);
				if (suppGoodsBaseTimePrice != null) {
					SuppGoodsTimePrice timePrice = (SuppGoodsTimePrice) suppGoodsBaseTimePrice;
					//if ("Y".equalsIgnoreCase(timePrice.getRestoreFlag())) {
						if ("Y".equalsIgnoreCase(timePrice.getFreeSaleFlag())) {
							LOG.info("method:updateRevertStock[库存返回],message=商品（ID=" + suppGoodsId + "）日期（"  + specDate + ")时间价格表已经FreeSale状态。");
						} else {
							if (dataMap != null&&!dataMap.isEmpty()) {
								Long orderItemId=(Long)dataMap.get("orderItemId");
								Map<String, Object> params=new HashMap<String, Object>(1);
								params.put("orderItemId", orderItemId);
								params.put("visitTime", specDate);
								List<OrdOrderGroupStock> groupStockList=ordOrderGroupStockDao.selectByParams(params);
								//共享库存修改start
//								if(CollectionUtils.isNotEmpty(groupStockList)){ 
//									SuppGoodsBaseTimePrice itemTimePrice=null;
//									for (OrdOrderGroupStock ordOrderGroupStock : groupStockList) {
//										itemTimePrice=goodsTimePriceStockService.getTimePrice(ordOrderGroupStock.getSuppGoodsId(), ordOrderGroupStock.getVisitTime(), false);
//										//恢复库存
//										goodsTimePriceStockService.updateStock(itemTimePrice.getTimePriceId(), ordOrderGroupStock.getQuantity());
//									}
//								}else{
									//恢复库存
									goodsTimePriceStockService.updateStock(timePrice.getTimePriceId(), stock);
//								}
									//共享库存修改end
//								
//								Boolean isUpdateSuperStock = (Boolean)dataMap.get("isUpdateSuperStock");
//								
//								if (isUpdateSuperStock != null && isUpdateSuperStock.booleanValue()) {
//									
//									if(CollectionUtils.isNotEmpty(groupStockList)){
//										for (OrdOrderGroupStock ordOrderGroupStock : groupStockList) {
//											petProdGoodsAdapter.updateStockByOrder(ordOrderGroupStock.getSuppGoodsId(), ordOrderGroupStock.getQuantity(), ordOrderGroupStock.getVisitTime(), ordOrderGroupStock.getVisitTime());
//										}
//									}else{
//										petProdGoodsAdapter.updateStockByOrder(suppGoodsId, stock, specDate, specDate);
//									}
//								}
							}else{
								//恢复库存
								goodsTimePriceStockService.updateStock(timePrice.getTimePriceId(), stock);
							}
						}
					//}
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
//		try {
//			//商品是否可售
//			if(suppGoods==null || !suppGoods.isValid()){
//				resultHandleT.setMsg("商品不存在或无效。");
//				return resultHandleT;
//			}
//			//商品参数验证
//			checkParam(suppGoods, item, true);
//			Date currDate = new Date();
//			Date visitDate = item.getVisitTimeDate();
//
//			int days = DateUtil.getDaysBetween(item.getVisitTimeDate(), item.getHotelAdditation().getLeaveTimeDate());
//			LOG.info("入住时间天数："+days);
//			long totalSettlementPrice = 0L;
//			Map<String,Long> settlementPriceMap=new HashMap<String, Long>();
//			for(int i = 0; i < days; i++){
//				Date oneDate = DateUtils.addDays(visitDate, i);
//				ResultHandleT<TimePrice> timePriceHolder = distGoodsTimePriceClientService.findTimePrice(distributionId, suppGoods.getSuppGoodsId(), oneDate);
//				LOG.info("正在进行库存检查，获取时间价格表数据,得到的时间数据为 \n"+JSONArray.fromObject(timePriceHolder));
//				LOG.info("OrderTimePriceServiceImpl.checkStock(Date=" + oneDate + "): timePriceHolder.isSuccess=" + timePriceHolder.isSuccess());
//				if(timePriceHolder.isFail() || timePriceHolder.getReturnContent() == null){
//					LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + ",时间" + oneDate + "时间价格表不存在。");
//					resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")时间价格表不存在。");
//					return resultHandleT;
//				}
//
//				SuppGoodsTimePrice timePrice = timePriceHolder.getReturnContent();
//				LOG.info("#######LAST酒店通过日期判定是否有共享库存库存LAST########");
//				Long groupId = suppGoods.getGroupId();
//				Long shareStock = null;
//				LOG.info("groupId="+groupId+"    SuppGoodsId"+suppGoods.getSuppGoodsId());
//				if(null != groupId){
//					LOG.info("查询共享库存：groupId="+groupId+"    visitTime="+oneDate);
//					shareStock = iGoodsTimePriceStockService.getShareStock(groupId,oneDate);
//					LOG.info("oneDate="+oneDate+"    groupId="+groupId+"     shareStock="+shareStock);
//				}
//				if(null != shareStock){
//					if(!this.checkSharedTimePrice(timePrice,currDate,(long)(item.getQuantity()),shareStock)){
//						LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
//						resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
//						return resultHandleT;
//					}
//				}else{
//					if (!this.checkTimePrice(timePrice, currDate, (long) (item.getQuantity()))) {
//						LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
//						resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
//						return resultHandleT;
//					}
//				}
//				totalSettlementPrice+=timePrice.getSettlementPrice();
//				settlementPriceMap.put(DateUtil.formatDate(timePrice.getSpecDate(), "yyyy-MM-dd"), timePrice.getSettlementPrice());
//			}
//
//			if (suppGoods != null && suppGoods.getSupplierId() != null && suppGoods.getSuppSupplier() != null) {
//				if ("Y".equals(suppGoods.getStockApiFlag())) {
//					com.lvmama.vst.comm.vo.SupplierProductInfo.Item supplierItem = makeSupplierItem(item,totalSettlementPrice,settlementPriceMap);
//					if (supplierItem != null && supplierItem.getHotelAdditation() == null) {
//						LOG.info("商品ID:" + item.getGoodsId() + "没有填写酒店信息。");
//						resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")没有填写酒店信息。");
//						return resultHandleT;
//					}
//					resultHandleT.setReturnContent(supplierItem);
//				}
//			}
//		} catch (Exception e) {
//			resultHandleT.setMsg(e.getMessage());
//		}

		return resultHandleT;
	}

//	@Override
//	public ResultHandleT<Object> checkStock(SuppGoods suppGoods, Item item, Long distributionId, Map<String, Object> dataMap) {
//		ResultHandleT<Object> resultHandleT = new ResultHandleT<Object>();
//
//		try {
//			//商品是否可售
//			if (suppGoods != null && suppGoods.isValid()) {
//
//				//商品参数验证
//				checkParam(suppGoods, item, true);
//
//				Date currDate = new Date();
//				Date visitDate = item.getVisitTimeDate();
//
//				ResultHandleT<TimePrice> timePriceHolder = distGoodsTimePriceClientService.findTimePrice(distributionId, suppGoods.getSuppGoodsId(), visitDate);
//
//				if (timePriceHolder.isSuccess() && timePriceHolder.getReturnContent() != null) {
//					SuppGoodsTimePrice timePrice = timePriceHolder.getReturnContent();
//					// 入住日期内总结算价
//					long totalSettlementPrice=timePrice.getSettlementPrice();
//					Map<String,Long> settlementPriceMap=new HashMap<String, Long>();
//					settlementPriceMap.put(DateUtil.formatDate(timePrice.getSpecDate(), "yyyy-MM-dd"), timePrice.getSettlementPrice());
//					if (this.checkTimePrice(timePrice, currDate, (long) (item.getQuantity()))) {
//						int days = DateUtil.getDaysBetween(item.getVisitTimeDate(), item.getHotelAdditation().getLeaveTimeDate());
//						LOG.info("入住时间天数："+days);
//                        //Date oneDate = null;
//                        List<OrdOrderSharedStock> sharedStockList = new ArrayList<OrdOrderSharedStock>();
//						for(int i = 0; i < days; i++){
//                            Date oneDate = DateUtils.addDays(visitDate, i);
//							timePriceHolder = distGoodsTimePriceClientService.findTimePrice(distributionId, suppGoods.getSuppGoodsId(), oneDate);
//
//							LOG.info("OrderTimePriceServiceImpl.checkStock(Date=" + oneDate + "): timePriceHolder.isSuccess=" + timePriceHolder.isSuccess());
//							if(timePriceHolder.isFail() || timePriceHolder.getReturnContent() == null){
//								LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + ",时间" + oneDate + "时间价格表不存在。");
//								resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")时间价格表不存在。");
//								return resultHandleT;
//							}
//
//							timePrice = timePriceHolder.getReturnContent();
//
//                            LOG.info("#######LAST酒店通过日期判定是否有共享库存库存LAST########");
//                            Long groupId = suppGoods.getGroupId();
//                            Long shareStock = null;
//                            LOG.info("groupId="+groupId+"    SuppGoodsId"+suppGoods.getSuppGoodsId());
//                            if(null != groupId){
//                                LOG.info("查询共享库存：groupId="+groupId+"    visitTime="+oneDate);
//                                shareStock = iGoodsTimePriceStockService.getShareStock(groupId,oneDate);
//                                LOG.info("oneDate="+oneDate+"    groupId="+groupId+"     shareStock="+shareStock);
//                            }
//                            if(null != shareStock){
//                                if(!this.checkSharedTimePrice(timePrice,currDate,(long)(item.getQuantity()),shareStock)){
//                                    LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
//                                    resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
//                                    return resultHandleT;
//                                }else {
//                                    if(timePrice.isBeforeLastHoldTime(currDate)){
//                                        //设置库存
//                                        LOG.info("共享库存：商品ID=" + suppGoods.getSuppGoodsId() + "   VisitTime=" + oneDate + "   Quantity="+(long) item.getQuantity());
////                                        OrdOrderSharedStock sharedStock = new OrdOrderSharedStock();
////                                        sharedStock.setGroupId(groupId);
////                                        sharedStock.setVisitTime(oneDate);
////                                        sharedStock.setGoodsId(suppGoods.getSuppGoodsId());
////                                        sharedStock.setQuantity((long) item.getQuantity());
////                                        sharedStockList.add(sharedStock);
//                                    }
//                                }
//                            }else{
//                                if (!this.checkTimePrice(timePrice, currDate, (long) (item.getQuantity()))) {
//                                    LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
//                                    resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
//                                    return resultHandleT;
//                                }else{
//                                    //设置库存
//                                    LOG.info("库存：商品ID=" + suppGoods.getSuppGoodsId() + "   VisitTime=" + oneDate + "   Quantity="+(long) item.getQuantity());
////                                    OrdOrderSharedStock sharedStock = new OrdOrderSharedStock();
////                                    sharedStock.setGroupId(null);
////                                    sharedStock.setVisitTime(oneDate);
////                                    sharedStock.setGoodsId(suppGoods.getSuppGoodsId());
////                                    sharedStock.setQuantity((long)item.getQuantity());
////                                    sharedStockList.add(sharedStock);
//                                }
//                            }
//
//							totalSettlementPrice+=timePrice.getSettlementPrice();
//							settlementPriceMap.put(DateUtil.formatDate(timePrice.getSpecDate(), "yyyy-MM-dd"), timePrice.getSettlementPrice());
//						}
//                        item.setSharedStockList(sharedStockList);
//					} else {
//						LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + "TimePriceId=" + timePrice.getTimePriceId() + "库存不足。");
//						resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")库存不足。");
//					}
//
//					if (suppGoods != null && suppGoods.getSupplierId() != null && suppGoods.getSuppSupplier() != null) {
//						if ("Y".equals(suppGoods.getStockApiFlag())) {
//							com.lvmama.vst.comm.vo.SupplierProductInfo.Item supplierItem = makeSupplierItem(item,totalSettlementPrice,settlementPriceMap);
//							if (supplierItem != null && supplierItem.getHotelAdditation() == null) {
//								LOG.info("商品ID:" + item.getGoodsId() + "没有填写酒店信息。");
//								resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")没有填写酒店信息。");
//								return resultHandleT;
//							}
//
//							resultHandleT.setReturnContent(supplierItem);
//						}
//
//					}
//				} else {
//					LOG.info("商品ID=" + suppGoods.getSuppGoodsId() + ",时间" + currDate + "时间价格表不存在。");
//					resultHandleT.setMsg("商品  " + suppGoods.getGoodsName() + " (ID:" + suppGoods.getSuppGoodsId() + ")时间价格表不存在。");
//				}
//			}
//		} catch (Exception e) {
//			resultHandleT.setMsg(e.getMessage());
//		}
//
//		return resultHandleT;
//	}
	
	private com.lvmama.vst.comm.vo.SupplierProductInfo.Item makeSupplierItem(Item buyInfoItem,Long totalSettlementPrice,Map<String,Long> settlementPriceMap) {
		com.lvmama.vst.comm.vo.SupplierProductInfo.Item item = null;
		
		if (buyInfoItem != null) {
			item = new com.lvmama.vst.comm.vo.SupplierProductInfo.Item(null, buyInfoItem.getVisitTimeDate());
			item.setQuantity(new Long(buyInfoItem.getQuantity()));
			item.setSuppGoodsId(buyInfoItem.getGoodsId());
			item.setHotelAdditation(makeSupplierHotelAdditation(buyInfoItem.getHotelAdditation()));
			item.setSettlementPrice(totalSettlementPrice);
			item.setSettlementPriceMap(settlementPriceMap);
		}
		
		return item;
	}
	
	private com.lvmama.vst.comm.vo.SupplierProductInfo.HotelAdditation makeSupplierHotelAdditation(HotelAdditation buyInfoHotelAdditation) {
		com.lvmama.vst.comm.vo.SupplierProductInfo.HotelAdditation hotelAdditation = null;
		if (buyInfoHotelAdditation != null) {
			hotelAdditation = new com.lvmama.vst.comm.vo.SupplierProductInfo.HotelAdditation();
			BeanUtils.copyProperties(buyInfoHotelAdditation, hotelAdditation);
		}
		
		return hotelAdditation;
	}
	
	/**
	 * 商品影响订购的参数检查
	 * @param suppGoods
	 * @param item
	 */
	protected void checkParam(final SuppGoods suppGoods, final BuyInfo.Item item, boolean ck){
		
		super.checkParam(suppGoods, item, ck);
		
		if(ck){
			if(item.getHotelAdditation()==null||item.getHotelAdditation().getLeaveTimeDate()==null){
				throw new IllegalArgumentException("酒店商品 "+suppGoods.getGoodsName()+" 缺少离店日期");
			}
			int days=DateUtil.getDaysBetween(item.getVisitTimeDate(), item.getHotelAdditation().getLeaveTimeDate());
			if(days>suppGoods.getMaxStayDay()){
				throw new IllegalArgumentException("商品 "+suppGoods.getGoodsName()+" 超出最大可下单天数");
			}
			
			if(days<suppGoods.getMinStayDay()){
				throw new IllegalArgumentException("商品 "+suppGoods.getGoodsName()+" 少于最少入住天数");
			}
		}
	}

	@Override
	public String getTimePricePrefix() {
		return "HotelTimePrice";
	}

	@Override
	public void calcSettlementPromotion(OrdOrderItem orderItem,List<OrdPromotion> promotions) {
		long amount = 0;
		for(OrdPromotion op:promotions){
			FavorableAmount fa = op.getPromotion().getFavorableAmount();
			amount += fa.getAdultAmount();
		}
		long totalPrice =orderItem.getTotalSettlementPrice()-amount;
		if(totalPrice<0){
			totalPrice=0;
		}
		orderItem.setTotalSettlementPrice(totalPrice);
		orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
	}
}
