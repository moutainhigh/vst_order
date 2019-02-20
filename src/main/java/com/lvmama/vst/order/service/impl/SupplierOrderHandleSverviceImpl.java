package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.passport.service.PassCodeService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdOrderTracking;
import com.lvmama.vst.back.order.po.OrdPassCode;
import com.lvmama.vst.back.order.po.OrdTicketPerform;
import com.lvmama.vst.back.order.po.OrdTicketPerformDetail;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.passport.po.PassCodeImageVo;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderFlagParam;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.order.dao.OrdOrderHotelTimeRateDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.dao.OrdPassCodeDao;
import com.lvmama.vst.order.dao.OrdTicketPerformDao;
import com.lvmama.vst.order.dao.OrdTicketPerformDetailDao;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.IOrdOrderTrackingService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.ISupplierOrderHandleService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.supp.client.service.SupplierOrderOtherService;
import com.lvmama.vst.supp.elong.vo.SuppOrderRelated;
import com.lvmama.vst.supp.elong.vo.SuppOrderRelated.ChildOrder;
import com.lvmama.vst.suppTicket.client.product.po.IntfStylProdRela;
import com.lvmama.vst.suppTicket.client.product.service.SuppTicketProductClientService;

@Service
public class SupplierOrderHandleSverviceImpl implements ISupplierOrderHandleService {
	private static final Log LOG = LogFactory.getLog(SupplierOrderHandleSverviceImpl.class);
	
	@Autowired
	private OrdOrderItemDao ordOrderItemDao;
	
	@Autowired
	private OrdOrderHotelTimeRateDao ordOrderHotelTimeRateDao;
	
	@Autowired
	private OrdOrderStockDao ordOrderStockDao;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
	private IOrdOrderItemService ordOrderItemService;
	
	@Autowired
	private IOrdOrderTrackingService ordOrderTrackingService;
	
	//控制入参是否允许ItemOrderId为空
	private boolean isAllOrderItemIfOrderItemIdNull = false;
	
	//控制入参时间周期是否连续
	private boolean isTimePeriodContinuation = true;
	
	@Resource(name="orderMessageProducer")
	private TopicMessageProducer orderMessageProducer;
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	@Autowired
	private OrdTicketPerformDao ordTicketPerformDao;
	@Autowired
	private OrdTicketPerformDetailDao ordTicketPerformDetailDao;
	
	@Autowired
	private SupplierOrderOtherService supplierOrderOtherService;
	
	@Autowired
	private IOrdOrderItemService iOrdOrderItemService;
	
	@Autowired
	private  SuppTicketProductClientService suppTicketProductClientRemote;
	
	@Autowired
	private PassCodeService passCodeService;
	
	@Override
	public ResultHandle updateAmpleResourceStatus(SuppOrderRelated suppOrderRelated) {
		ResultHandle resultHandle = new ResultHandle();
		if (suppOrderRelated.getOrderId() != null) {
			OrdOrder order = getOrderWithOrderItemOrderStockByOrderRelated(suppOrderRelated);
			if (order != null) {
				if (!OrderEnum.ORDER_STATUS.NORMAL.name().equals(order.getOrderStatus())) {
					LOG.debug("SupplierOrderHandleSverviceImpl.updateAmpleResourceStatus:订单(OrderId=" + order.getOrderId() + ")订单状态已经为" + order.getOrderStatus() + "状态，无法执行资源审核。");
					
					throw new IllegalArgumentException("订单(OrderId=" + order.getOrderId() + ")订单状态已经为" + order.getOrderStatus() + "状态，无法执行资源审核。");
				}
				
				if (!OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(order.getResourceStatus())) {
					LOG.debug("SupplierOrderHandleSverviceImpl.updateAmpleResourceStatus:订单(OrderId=" + order.getOrderId() + ")资源状态已经为" + order.getResourceStatus() + "状态。");

					throw new IllegalArgumentException("订单(OrderId=" + order.getOrderId() + ")资源状态已经为" + order.getResourceStatus() + "状态。");
				}
				//获取要满足入参的订单子项
				List<OrdOrderItem> updateItemList = getUpdateOrdOrderItemList(order, suppOrderRelated);
				if (updateItemList != null && updateItemList.size() > 0) {
					//获取要审核的订单库存（dateList为null获取所有订单库存）
					List<OrdOrderStock> updateOrderStockList = getOrdOrderStockListByDateList(updateItemList, null);
					if (updateOrderStockList != null && updateOrderStockList.size() > 0) {
						OrdOrderStock updateOrderStock = null;
						//更新订单库存资源审核信息
						for (OrdOrderStock stock : updateOrderStockList) {
							stock.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.name());
							
							updateOrderStock = new OrdOrderStock();
							updateOrderStock.setOrderStockId(stock.getOrderStockId());
							updateOrderStock.setResourceStatus(stock.getResourceStatus());
							ordOrderStockDao.updateByPrimaryKeySelective(updateOrderStock);
						}
						
						for (OrdOrderItem handleItem : updateItemList) {
							if (handleItem != null) {
								//判断是否更新订单子项资源审核信息
								if (isUpdateOrderItmeAmpleResourceStatus(handleItem)) {
									LOG.info("SupplierOrderHandleSverviceImpl.updateAmpleResourceStatus:handleItem[ID=" + handleItem.getOrderItemId() + "]isUpdateOrderItmeAmpleResourceStatus=true");
									
									handleItem.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.name());
									
//									OrdOrderItem updateItem = new OrdOrderItem();
//									updateItem.setOrderItemId(handleItem.getOrderItemId());
//									updateItem.setResourceStatus(handleItem.getResourceStatus());
//									ordOrderItemDao.updateByPrimaryKeySelective(updateItem);
									//资源保留时间
									String resourceRetentionTime = "";
									orderLocalService
											.executeUpdateChildResourceStatus(
													handleItem,
													OrderEnum.RESOURCE_STATUS.AMPLE
															.name(),
													resourceRetentionTime,
													"SUPPLIER", "第三方供应商资源审核通过。",false);
								}
							}
						}
						
						//判断是否更新订单资源审核信息
						if (isUpdateOrderAmpleResourceStatus(order)) {
							LOG.info("SupplierOrderHandleSverviceImpl.updateAmpleResourceStatus:order[ID=" + order.getOrderId() + "]isUpdateOrderAmpleResourceStatus=true");
							
//							order.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.name());
//							
//							OrdOrder updateOrder = new OrdOrder();
//							updateOrder.setOrderId(order.getOrderId());
//							updateOrder.setResourceStatus(order.getResourceStatus());
//							ordOrderDao.updateByPrimaryKeySelective(updateOrder);
													
							//资源保留时间
							String resourceRetentionTime="";
							orderLocalService.executeUpdateResourceStatus(order.getOrderId(), OrderEnum.RESOURCE_STATUS.AMPLE.name(),resourceRetentionTime, "SUPPLIER", "第三方供应商资源审核通过。");
						}
					} else {
						LOG.debug("SupplierOrderHandleSverviceImpl.updateAmpleResourceStatus:不存在相关资源信息。");
						
						resultHandle.setMsg("不存在相关资源信息。");
					}
				} else {
					resultHandle.setMsg("资源审核的订单子项不存在。");
					
					LOG.debug("SupplierOrderHandleSverviceImpl.updateAmpleResourceStatus:资源审核的订单子项不存在。");
				}
			} else {
				resultHandle.setMsg("SuppOrderRelated.orderId[" + suppOrderRelated.getOrderId() + "] do not exist in order table.");
				
				LOG.debug("SupplierOrderHandleSverviceImpl.updateAmpleResourceStatus:SuppOrderRelated.orderId[" + suppOrderRelated.getOrderId() + "] do not exist in order table.");
			}
		} else {
			resultHandle.setMsg("SuppOrderRelated's orderId[" + suppOrderRelated.getOrderId() + "] is null.");
			
			LOG.debug("SupplierOrderHandleSverviceImpl.updateAmpleResourceStatus: SuppOrderRelated's orderId[" + suppOrderRelated.getOrderId() + "] is null.");
		}
		
		return resultHandle;
	}

	/**
	 * 根据OrderId获取整个Order对象图
	 * 
	 * @param orderId
	 * @return
	 */
	private OrdOrder getOrderWithOjbectDiagramByOrderId(Long orderId) {
		OrdOrder order = null;
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(orderId);
		
		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderItemTableFlag(true);
		orderFlagParam.setOrderHotelTimeRateTableFlag(true);
		orderFlagParam.setOrderStockTableFlag(true);
		orderFlagParam.setOrderGuaranteeCreditCardTableFlag(true);
		orderFlagParam.setOrderAmountItemTableFlag(true);
		orderFlagParam.setOrderPackTableFlag(true);
		orderFlagParam.setOrderPersonTableFlag(true);
		orderFlagParam.setOrderAddressTableFlag(true);
		orderFlagParam.setOrderPageFlag(false);
		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);
		
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		if (orderList != null) {
			if (orderList.size() == 1) {
				order = orderList.get(0);
			}
			
			LOG.info("SupplierOrderHandleSverviceImpl.getOrderWithOjbectDiagramByOrderId: orderList.size=" + orderList.size());
			
		} else {
			LOG.info("SupplierOrderHandleSverviceImpl.getOrderWithOjbectDiagramByOrderId: orderList=null");
		}
		
		return order;
	}
	
	/**
	 * 根据订单ID获取订单，订单关联订单子项，订单子项关联酒店每天使用情况。
	 * 
	 * @param orderId
	 * @return
	 */
	private OrdOrder getOrderWithItmeTimeRateByOrderId(Long orderId) {
		OrdOrder order = null;
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(orderId);
		
		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderItemTableFlag(true);
		orderFlagParam.setOrderHotelTimeRateTableFlag(true);
		
		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);
		
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		if (orderList != null) {
			if (orderList.size() == 1) {
				order = orderList.get(0);
				
				LOG.info("SupplierOrderHandleSverviceImpl.getOrderWithItmeTimeRateByOrderId:order[ID=" + order.getOrderId() + "].");
			}
			
			LOG.info("SupplierOrderHandleSverviceImpl.getOrderWithItmeTimeRateByOrderId:orderList.size=" + orderList.size());
		} else {
			LOG.info("SupplierOrderHandleSverviceImpl.getOrderWithItmeTimeRateByOrderId: orderList=null");
		}
		return order;
	}
	
	/**
	 * 根据订单ID获取订单，订单关联订单子项，订单子项关联订单库存
	 * 
	 * @param orderId
	 * @return
	 */
	private OrdOrder getOrderWithOrderItemOrderStockByOrderId(Long orderId) {
		OrdOrder order = null;
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(orderId);
		
		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderItemTableFlag(true);
		orderFlagParam.setOrderStockTableFlag(true);

		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);
		
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		if (orderList != null && orderList.size() == 1) {
			order = orderList.get(0);
		}
		return order;
	}
	
	/**
	 * 根据SuppOrderRelated获取订单，订单关联订单子项，订单子项关联订单库存
	 * 
	 * @param suppOrderRelated
	 * @return
	 */
	private OrdOrder getOrderWithOrderItemOrderStockByOrderRelated(SuppOrderRelated suppOrderRelated) {
		OrdOrder order = null;
		if (suppOrderRelated.getOrderId() != null ) {
			if (suppOrderRelated.getOrderItemId() == null && !isAllOrderItemIfOrderItemIdNull) {
				LOG.debug("SupplierOrderHandleSverviceImpl.getOrderWithOrderItemOrderStockByOrderRelated: SuppOrderRelated's orderId["
						+ suppOrderRelated.getOrderId() + ", suppOrderRelated.getOrderItemId=" + suppOrderRelated.getOrderItemId());
				
				throw new IllegalArgumentException("SuppOrderRelated.orderItemId is null.");
			}

			order = getOrderWithOrderItemOrderStockByOrderId(suppOrderRelated.getOrderId());
			
		} else {
			LOG.debug("SupplierOrderHandleSverviceImpl.getOrderWithOrderItemOrderStockByOrderRelated:SuppOrderRelated's orderId[" + suppOrderRelated.getOrderId() + "] or orderItemId[" + "] is null.");
			
			throw new RuntimeException("SuppOrderRelated's orderId[" + suppOrderRelated.getOrderId() + "] or orderItemId[" + "] is null.");
		}
		
		return order;
	}
	
	/**
	 * 根据SuppOrderRelated，获取要更新的订单子项列表
	 * 
	 * @param order
	 * @param suppOrderRelated
	 * @return
	 */
	private List<OrdOrderItem> getUpdateOrdOrderItemList(OrdOrder order, SuppOrderRelated suppOrderRelated) {
		List<OrdOrderItem> orderItemList = null;
		
		if (order != null && suppOrderRelated != null) {
			if (suppOrderRelated.getOrderItemId() != null) {
				
				if (order.getOrderItemList() != null) {
					OrdOrderItem handleItem = null;
					for (OrdOrderItem item : order.getOrderItemList()) {
						if (item.getOrderItemId().equals(suppOrderRelated.getOrderItemId())) {
							handleItem = item;
							break;
						}
					}
					if (handleItem != null) {
						orderItemList = new ArrayList<OrdOrderItem>();
						orderItemList.add(handleItem);
					} else {
						LOG.debug("SupplierOrderHandleSverviceImpl.getUpdateOrdOrderItemList:SuppOrderRelated.orderId[" + suppOrderRelated.getOrderId() + "]orderItemId[" + suppOrderRelated.getOrderItemId() + "] do not exist in table.");
						
						throw new IllegalArgumentException("SuppOrderRelated.orderId[" + suppOrderRelated.getOrderId() + "]orderItemId[" + suppOrderRelated.getOrderItemId() + "] do not exist in table.");
					}
				} else {
					LOG.debug("SupplierOrderHandleSverviceImpl.getUpdateOrdOrderItemList:SuppOrderRelated.orderId[" + suppOrderRelated.getOrderId() + "],orderItemId[" + suppOrderRelated.getOrderItemId() + "] do not exist in table.");
					
					throw new IllegalArgumentException("SuppOrderRelated.orderId[" + suppOrderRelated.getOrderId() + "],orderItemId[" + suppOrderRelated.getOrderItemId() + "] do not exist in table.");
				}
			} else {
				if (isAllOrderItemIfOrderItemIdNull) {
					orderItemList = order.getOrderItemList();
				} else {
					LOG.debug("SupplierOrderHandleSverviceImpl.getUpdateOrdOrderItemList:SuppOrderRelated.orderItemId is null.");
					
					throw new IllegalArgumentException("SuppOrderRelated.orderItemId is null.");
				}
			}
		}
		
		return orderItemList;
	}
	
	/**
	 * 只获取单个订单对象
	 * 
	 * @param orderId
	 * @return
	 */
	private OrdOrder getSingleOrderByOrderId(Long orderId) {
		OrdOrder order = null;
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(orderId);

		condition.setOrderIndentityParam(orderIndentityParam);
		
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		if (orderList != null && orderList.size() == 1) {
			order = orderList.get(0);
		}
		return order;
	}
	
	@Override
	public ResultHandle updateUnperformStatus(SuppOrderRelated suppOrderRelated) {
		ResultHandle resultHandle = null;
		OrdOrder order = getOrderWithItmeTimeRateByOrderRelated(suppOrderRelated);
		if (order != null)
		{
			if (!OrderEnum.ORDER_STATUS.COMPLETE.name().equals(order.getOrderStatus())) {
				LOG.debug("SupplierOrderHandleSverviceImpl.updateUnperformStatus:订单(" + order.getOrderId() + ")订单状态为：" + order.getOrderStatus() + "，不能操作履行状态。");
				
				throw new IllegalArgumentException("订单(" + order.getOrderId() + ")订单状态为：" + order.getOrderStatus() + "，不能操作履行状态。");
			}
			//更新操作
			resultHandle = updateTimeRatePerformStatus(order, suppOrderRelated, false);
		} else {
			resultHandle = new ResultHandle();
			resultHandle.setMsg("SuppOrderRelated.orderId[" + suppOrderRelated.getOrderId() + "]do not exist in order table.");
			
			LOG.debug("SupplierOrderHandleSverviceImpl.updateUnperformStatus:SuppOrderRelated.orderId[" + suppOrderRelated.getOrderId() + "]do not exist in order table.");
		}
		
		return resultHandle;
	}
	
	/**
	 * 按日期查找OrdOrderHotelTimeRate
	 * 
	 * @param orderItem
	 * @param visitTime
	 * @return
	 */
	private OrdOrderHotelTimeRate findTimeRateFromOrderItemByVisitTime(OrdOrderItem orderItem, Date visitTime) {
		OrdOrderHotelTimeRate timeRate = null;
		if (orderItem != null && orderItem.getOrderHotelTimeRateList() != null && visitTime != null) {
			for (OrdOrderHotelTimeRate rate : orderItem.getOrderHotelTimeRateList()) {
				if (rate != null && visitTime.equals(rate.getVisitTime())) {
					timeRate = rate;
					break;
				}
			}
		}
		return timeRate;
	}
	
	/**
	 * 按日期查找OrdOrderStock
	 * 
	 * @param orderItem
	 * @param visitTime
	 * @return
	 */
	private OrdOrderStock findOrderStockFromOrderItemByVisitTime(OrdOrderItem orderItem, Date visitTime) {
		OrdOrderStock orderStock = null;
		if (orderItem != null && orderItem.getOrderStockList() != null && visitTime != null) {
			for (OrdOrderStock stock : orderItem.getOrderStockList()) {
				if (stock != null && "true".equalsIgnoreCase(stock.getNeedResourceConfirm())&& visitTime.equals(stock.getVisitTime())) {
					orderStock = stock;
					break;
				}
			}
		}
		return orderStock;
	}
	
	/**
	 * 更新酒店每天使用情况表的为未履行标志
	 * 
	 * @param handleItem
	 * @param suppOrderRelated
	 * @return
	 */
	private ResultHandle updateTimeRatePerformStatus(OrdOrder order, SuppOrderRelated suppOrderRelated, boolean isPerform) {
		ResultHandle resultHandle = new ResultHandle();
		//获取要更新的订单子项
		List<OrdOrderItem> updateItemList = getUpdateOrdOrderItemList(order, suppOrderRelated);
		if (updateItemList != null && updateItemList.size() > 0) {
			List<OrdOrderHotelTimeRate> performTimeRateList = null;
			List<OrdOrderHotelTimeRate> unperformTimeRateList = null;
			//有子订单，取子订单的履行状态
			if (suppOrderRelated.getChildOrders() != null && suppOrderRelated.getChildOrders().size() > 0) {
				List<Date> performDateList = new ArrayList<Date>();
				List<Date> unperformDateList = new ArrayList<Date>();
				makeUpdateDateListByOrderRelated(suppOrderRelated, performDateList, unperformDateList);
				performTimeRateList = getHotelTimeRateListByDateList(updateItemList, performDateList);
				unperformTimeRateList = getHotelTimeRateListByDateList(updateItemList, unperformDateList);
			//没有子订单，取父订单的全部履行状态
			} else {
				LOG.info("SupplierOrderHandleSverviceImpl.updateTimeRatePerformStatus:suppOrderRelated(orderId= " + suppOrderRelated.getOrderId() + ").ChildOrders is empty");
				
				if (isPerform) {
					performTimeRateList = getAllHotelTimeRatesInOrderItemList(updateItemList);
				} else {
					unperformTimeRateList = getAllHotelTimeRatesInOrderItemList(updateItemList);
				}
			}
			
			if ((performTimeRateList != null && performTimeRateList.size() > 0)
					|| (unperformTimeRateList != null && unperformTimeRateList.size() > 0)) {
				if (performTimeRateList != null && performTimeRateList.size() > 0) {
					OrdOrderHotelTimeRate updateTimeRate = null;
					//更新酒店每天使用情况表
					for (OrdOrderHotelTimeRate hotelTimeRate : performTimeRateList) {
						hotelTimeRate.setPerformFlag("true");
						
						updateTimeRate = new OrdOrderHotelTimeRate();
						updateTimeRate.setHotelTimeRateId(hotelTimeRate.getHotelTimeRateId());
						updateTimeRate.setPerformFlag(hotelTimeRate.getPerformFlag());
						ordOrderHotelTimeRateDao.updateByPrimaryKeySelective(updateTimeRate);
					}
				}
				
				if (unperformTimeRateList != null && unperformTimeRateList.size() > 0) {
					OrdOrderHotelTimeRate updateTimeRate = null;
					//更新酒店每天使用情况表
					for (OrdOrderHotelTimeRate hotelTimeRate : unperformTimeRateList) {
						hotelTimeRate.setPerformFlag("false");
						
						updateTimeRate = new OrdOrderHotelTimeRate();
						updateTimeRate.setHotelTimeRateId(hotelTimeRate.getHotelTimeRateId());
						updateTimeRate.setPerformFlag(hotelTimeRate.getPerformFlag());
						ordOrderHotelTimeRateDao.updateByPrimaryKeySelective(updateTimeRate);
					}
				}
				
				for (OrdOrderItem handleItem : updateItemList) {
					if (handleItem != null) {
						//是否更新订单子项的未履行状态
						if (isUpdateOrderItmePerformStatus(handleItem)) {
							OrdOrderItem updateItem = new OrdOrderItem();
							updateItem.setOrderItemId(handleItem.getOrderItemId());
							updateItem.setPerformStatus(handleItem.getPerformStatus());
							ordOrderItemDao.updateByPrimaryKeySelective(updateItem);
						}
					}
				}
			} else {
				LOG.info("SupplierOrderHandleSverviceImpl.updateTimeRatePerformStatus:不存在相关入住信息。");
				
				resultHandle.setMsg("不存在相关入住信息。");
			}

		} else {
			LOG.info("SupplierOrderHandleSverviceImpl.updateTimeRatePerformStatus:未履行状态的订单子项不存在。");
			
			resultHandle.setMsg("未履行状态的订单子项不存在。");
		}
		
		return resultHandle;
	}
	
//	/**
//	 * 根据SuppOrderRelated对象，获取入住时间、离店时间之间的日期列表
//	 * 
//	 * @param suppOrderRelated
//	 * @return
//	 */
//	private List<Date> getUpdateDateListByOrderRelated(SuppOrderRelated suppOrderRelated) {
//		List<Date> dateList = null;
//		Date startDate = null;
//		Date endDate = null;
//		if (suppOrderRelated != null) {
//			//如果有存在子订单的情况
//			if (suppOrderRelated.getChildOrders() != null && suppOrderRelated.getChildOrders().size() > 0) {
//				dateList = new ArrayList<Date>();
//				for (ChildOrder childOrder : suppOrderRelated.getChildOrders()) {
//					if (childOrder != null) {
//						startDate = childOrder.getArrivalDate();
//						endDate = childOrder.getDepartureDate();
//						if (startDate != null && endDate != null) {
//							startDate = CalendarUtils.getDateFromDateTime(startDate);
//							endDate = CalendarUtils.getDateFromDateTime(endDate);
//							List<Date> aDateList = CalendarUtils.getDatesExtension(startDate, true, endDate, false);
//							if (aDateList != null && aDateList.size() > 0) {
//								dateList.addAll(aDateList);
//							}
//						} else {
//							throw new IllegalArgumentException("arrivalDate or departureDate is null in suppOrderRelated.childOrders");
//						}
//					}
//				}
//				
//				if (dateList.size() == 0) {
//					throw new IllegalArgumentException("arrivalDate or departureDate is error");
//				}
//			}
//		} else {
//			throw new IllegalArgumentException("参数SuppOrderRelated为空。");
//		}
//		return dateList;
//	}
	
	/**
	 * 根据SuppOrderRelated对象，获取入住时间、离店时间之间的履行和未履行日期列表
	 * 
	 * @param suppOrderRelated
	 * @return
	 */
	private void makeUpdateDateListByOrderRelated(SuppOrderRelated suppOrderRelated, List<Date> performDateList, List<Date> unperformDateList) {
		Date startDate = null;
		Date endDate = null;
		if (suppOrderRelated != null) {
			//如果有存在子订单的情况
			if (suppOrderRelated.getChildOrders() != null && suppOrderRelated.getChildOrders().size() > 0) {
				for (ChildOrder childOrder : suppOrderRelated.getChildOrders()) {
					if (childOrder != null) {
						startDate = childOrder.getArrivalDate();
						endDate = childOrder.getDepartureDate();
						if (startDate != null && endDate != null) {
							startDate = CalendarUtils.getDateFromDateTime(startDate);
							endDate = CalendarUtils.getDateFromDateTime(endDate);
							List<Date> aDateList = CalendarUtils.getDatesExtension(startDate, true, endDate, false);
							if (aDateList != null && aDateList.size() > 0) {
								if (OrderEnum.ORDER_PERFORM_STATUS.PERFORM.name().equals(childOrder.getStatus())) {
									performDateList.addAll(aDateList);
								} else if (OrderEnum.ORDER_PERFORM_STATUS.UNPERFORM.name().equals(childOrder.getStatus())) {
									unperformDateList.addAll(aDateList);
								} else {
									LOG.info("SupplierOrderHandleSverviceImpl.makeUpdateDateListByOrderRelated:子订单履行类型(" + childOrder.getStatus() + ")不正确。");
									
									throw new IllegalArgumentException("子订单履行类型(" + childOrder.getStatus() + ")不正确。");
								}
							} else {
								LOG.info("SupplierOrderHandleSverviceImpl.makeUpdateDateListByOrderRelated:子订单履行类型(" + childOrder.getStatus() + ")入住、离店不正确,ArrivalDate=" + startDate + ",DepartureDate=" + endDate);
								
								throw new IllegalArgumentException("子订单履行类型(" + childOrder.getStatus() + ")入住、离店不正确。");
							}
						} else {
							LOG.info("SupplierOrderHandleSverviceImpl.makeUpdateDateListByOrderRelated:子订单入住、离店存在空值,ArrivalDate=" + startDate + ",DepartureDate=" + endDate);
							
							throw new IllegalArgumentException("arrivalDate or departureDate is null in suppOrderRelated.childOrders");
						}
					}
				}
			}
		} else {
			LOG.debug("SupplierOrderHandleSverviceImpl.makeUpdateDateListByOrderRelated:参数SuppOrderRelated为空。");
			
			throw new IllegalArgumentException("参数SuppOrderRelated为空。");
		}
	}
	
	/**
	 * 根据日期列表获取OrdOrderHotelTimeRate列表
	 * 
	 * @param orderItemList
	 * @param dateList
	 * @param performFlag
	 * @return
	 */
	private List<OrdOrderHotelTimeRate> getHotelTimeRateListByDateList(List<OrdOrderItem> orderItemList, List<Date> dateList) {
		OrdOrderHotelTimeRate timeRate = null;
		List<OrdOrderHotelTimeRate> updateTimeRateList = new ArrayList<OrdOrderHotelTimeRate>();
		if (orderItemList != null && orderItemList.size() > 0 && dateList != null && dateList.size() > 0) {
			for (OrdOrderItem handleItem : orderItemList) {
				if (handleItem != null) {
					for (Date visitDate : dateList) {
						timeRate = findTimeRateFromOrderItemByVisitTime(handleItem, visitDate);
						if (timeRate != null) {
							updateTimeRateList.add(timeRate);
						} else {
							LOG.info("SupplierOrderHandleSverviceImpl.getHotelTimeRateListByDateList:OrderItem[ID=" + handleItem.getOrderItemId() + "], VisitDate[" + visitDate + "] OrdOrderHotelTimeRate do not exist.");
							
							if (isTimePeriodContinuation) {
								throw new RuntimeException("OrderItem[ID=" + handleItem.getOrderItemId() + "], VisitDate[" + visitDate + "] OrdOrderHotelTimeRate do not exist.");
							}
						}
					}
				}
			}
		}
		
		return updateTimeRateList;
	}
	
	/**
	 * 获取所有的OrdOrderHotelTimeRate对象
	 * 
	 * @param orderItemList
	 * @return
	 */
	private List<OrdOrderHotelTimeRate> getAllHotelTimeRatesInOrderItemList(List<OrdOrderItem> orderItemList) {
		List<OrdOrderHotelTimeRate> timeRateList = null;
		if (orderItemList != null && orderItemList.size() > 0) {
			timeRateList = new ArrayList<OrdOrderHotelTimeRate>();
			for (OrdOrderItem item : orderItemList) {
				if (item != null && item.getOrderHotelTimeRateList() != null && item.getOrderHotelTimeRateList().size() > 0) {
					timeRateList.addAll(item.getOrderHotelTimeRateList());
				} else {
					LOG.info("SupplierOrderHandleSverviceImpl.getAllHotelTimeRatesInOrderItemList:OrderItem[ID=" + item.getOrderItemId() + "] is not satified.");
				}
			}
		}
		
		return timeRateList;
	}
	
	/**
	 * 根据日期列表获取OrdOrderStock列表
	 * 
	 * @param orderItemList
	 * @param dateList
	 * @return
	 */
	private List<OrdOrderStock> getOrdOrderStockListByDateList(List<OrdOrderItem> orderItemList, List<Date> dateList) {
		OrdOrderStock orderStock = null;
		List<OrdOrderStock> updateOrderStockList = new ArrayList<OrdOrderStock>();
		if (orderItemList != null && orderItemList.size() > 0) {
			for (OrdOrderItem handleItem : orderItemList) {
				if (handleItem != null) {
					if (!OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(handleItem.getResourceStatus())) {
						LOG.info("SupplierOrderHandleSverviceImpl.getOrdOrderStockListByDateList:OrdOrderItem[ID=" + handleItem.getOrderItemId() + "],ResourceStatus=" + handleItem.getResourceStatus());
						
						throw new IllegalArgumentException("订单子项(OrderItemId=" + handleItem.getOrderItemId() + ")资源状态已经为" + handleItem.getResourceStatus() + "状态。");
					}
					
					if (dateList != null && dateList.size() > 0) {
						for (Date visitDate : dateList) {
							orderStock = findOrderStockFromOrderItemByVisitTime(handleItem, visitDate);
							if (orderStock != null) {
								LOG.info("SupplierOrderHandleSverviceImpl.getOrdOrderStockListByDateList:orderStock[ID=" + orderStock.getOrderStockId() + "] is going to update.");
								updateOrderStockList.add(orderStock);
							} else {
								LOG.info("SupplierOrderHandleSverviceImpl.getOrdOrderStockListByDateList:handleItem[ID=" + handleItem.getOrderItemId() + "],Date[" + visitDate + "],orderStock is null");
								if (isTimePeriodContinuation) {
									throw new RuntimeException("VisitDate[" + visitDate + "]资源信息不存在。");
								}
							}
						}
					} else {
						LOG.info("SupplierOrderHandleSverviceImpl.getOrdOrderStockListByDateList:dateList == null or dateList.size() = 0");
						
						updateOrderStockList.addAll(handleItem.getOrderStockList());
					}
				}
			}
		}
		
		return updateOrderStockList;
	}
	
	/**
	 * 根据OrdOrderHotelTimeRate判断是否更新订单子项的未履行状态
	 * 
	 * @param handleItem
	 * @return
	 */
	private boolean isUpdateOrderItmePerformStatus(OrdOrderItem handleItem) {
		boolean isUpdateOrderItme = false;
		int performCount = 0;
		
		if (handleItem != null && handleItem.getOrderHotelTimeRateList() != null && handleItem.getOrderHotelTimeRateList().size() > 0) {
			for (OrdOrderHotelTimeRate timeRate: handleItem.getOrderHotelTimeRateList()) {
				if (timeRate != null) {
					LOG.info("SupplierOrderHandleSverviceImpl.isUpdateOrderItmePerformStatus:OrdOrderHotelTimeRate[ID=" + timeRate.getHotelTimeRateId() + "],PerformFlag=" + timeRate.getPerformFlag());
					
					if(timeRate.getPerformFlag() == null || "false".equals(timeRate.getPerformFlag())) {
						LOG.info("SupplierOrderHandleSverviceImpl.isUpdateOrderItmePerformStatus:OrdOrderHotelTimeRate[ID=" + timeRate.getHotelTimeRateId() + "],PerformStatus=" + handleItem.getPerformStatus());
						
						if (!OrderEnum.ORDER_PERFORM_STATUS.UNPERFORM.name().equals(handleItem.getPerformStatus())) {
							handleItem.setPerformStatus(OrderEnum.ORDER_PERFORM_STATUS.UNPERFORM.name());
							isUpdateOrderItme = true;
						}
						
						break;
					} else if("true".equals(timeRate.getPerformFlag())) {
						performCount++;
					} else {
						break;
					}
				}
			}
			
			LOG.info("SupplierOrderHandleSverviceImpl.isUpdateOrderItmePerformStatus:performCount=" + performCount + ",OrderHotelTimeRateList=" + handleItem.getOrderHotelTimeRateList().size());
			
			if (performCount == handleItem.getOrderHotelTimeRateList().size()) {
				if (!OrderEnum.ORDER_PERFORM_STATUS.PERFORM.name().equals(handleItem.getPerformStatus())) {
					handleItem.setPerformStatus(OrderEnum.ORDER_PERFORM_STATUS.PERFORM.name());
					isUpdateOrderItme = true;
				}
			}
		}
		
		return isUpdateOrderItme;
	}
	
	
	/**
	 * 根据OrdOrderStock判断是否更新订单子项的审核通过状态
	 * 
	 * @param handleItem
	 * @return
	 */
	private boolean isUpdateOrderItmeAmpleResourceStatus(OrdOrderItem handleItem) {
		boolean isUpdateOrderItme = false;
		
		if (handleItem != null && handleItem.getOrderStockList() != null && handleItem.getOrderStockList().size() > 0) {
			isUpdateOrderItme = true;
			for (OrdOrderStock stock: handleItem.getOrderStockList()) {
				if (stock != null) {
					LOG.info("SupplierOrderHandleSverviceImpl.isUpdateOrderItmeAmpleResourceStatus:OrdOrderStock[ID=" + stock.getOrderStockId() + "],ResourceStatus=" + stock.getResourceStatus());
					
					if (!OrderEnum.RESOURCE_STATUS.AMPLE.name().equals(stock.getResourceStatus())) {
						isUpdateOrderItme = false;
						break;
					}
				}
			}
		}
		
		return isUpdateOrderItme;
	}
	
	/**
	 * 根据订单子项判断是否更新订单的审核通过状态
	 * 
	 * @param order
	 * @return
	 */
	private boolean isUpdateOrderAmpleResourceStatus(OrdOrder order) {
		boolean isUpdateOrder = false;
		
		if (order != null && order.getOrderItemList() != null && order.getOrderItemList().size() > 0) {
			isUpdateOrder = true;
			for (OrdOrderItem item : order.getOrderItemList()) {
				if (item != null) {
					LOG.info("SupplierOrderHandleSverviceImpl.isUpdateOrderAmpleResourceStatus:OrdOrderItem[ID=" + item.getOrderItemId() + "],ResourceStatus=" + item.getResourceStatus());
					
					if (!OrderEnum.RESOURCE_STATUS.AMPLE.name().equals(item.getResourceStatus())) {
						isUpdateOrder = false;
						break;
					}
				}
			}
		}
		
		return isUpdateOrder;
	}
	
	/**
	 * 查询订单，订单关联订单子项，订单子项关联酒店每天使用情况
	 * 
	 * @param suppOrderRelated
	 * @return
	 */
	private OrdOrder getOrderWithItmeTimeRateByOrderRelated(SuppOrderRelated suppOrderRelated) {
		OrdOrder order = null;
		if (suppOrderRelated.getOrderId() != null ) {
			if (suppOrderRelated.getOrderItemId() == null && !isAllOrderItemIfOrderItemIdNull) {
				LOG.debug("SupplierOrderHandleSverviceImpl.getOrderWithItmeTimeRateByOrderRelated:OrderId[" + suppOrderRelated.getOrderId() + "] SuppOrderRelated.orderItemId is null.");
				
				throw new IllegalArgumentException("SuppOrderRelated.orderItemId is null.");
			}

			order = getOrderWithItmeTimeRateByOrderId(suppOrderRelated.getOrderId());
			
		} else {
			LOG.debug("SupplierOrderHandleSverviceImpl.getOrderWithItmeTimeRateByOrderRelated:SuppOrderRelated's orderId[" + suppOrderRelated.getOrderId() + "] or orderItemId[" + "] is null.");
			
			throw new RuntimeException("SuppOrderRelated's orderId[" + suppOrderRelated.getOrderId() + "] or orderItemId[" + "] is null.");
		}
		
		return order;
	}

	@Override
	public ResultHandle updatePerformStatus(SuppOrderRelated suppOrderRelated) {
		ResultHandle resultHandle = null;
		OrdOrder order = getOrderWithItmeTimeRateByOrderRelated(suppOrderRelated);
		if (order != null)
		{
			if (OrderEnum.ORDER_STATUS.CANCEL.name().equals(order.getOrderStatus())) {
				LOG.debug("SupplierOrderHandleSverviceImpl.updatePerformStatus:订单(" + order.getOrderId() + ")订单状态为：" + order.getOrderStatus() + "，不能操作履行状态。");
				
				throw new IllegalArgumentException("订单(" + order.getOrderId() + ")订单状态为：" + order.getOrderStatus() + "，不能操作履行状态。");
			}
			//更新操作
			resultHandle = updateTimeRatePerformStatus(order, suppOrderRelated, true);
		} else {
			resultHandle = new ResultHandle();
			resultHandle.setMsg("SuppOrderRelated.orderId[" + suppOrderRelated.getOrderId() + "]do not exist");
			
			LOG.debug("SupplierOrderHandleSverviceImpl.updatePerformStatus:SuppOrderRelated.orderId[" + suppOrderRelated.getOrderId() + "]do not exist");
		}
		
		return resultHandle;
	}

	@Override
	public ResultHandle updateOrderCancelStatus(
			SuppOrderRelated suppOrderRelated) {
		ResultHandle resultHandle = null;
		if (suppOrderRelated.getOrderId() != null) {
			//废单重下
			if (suppOrderRelated.getChildOrders() != null && suppOrderRelated.getChildOrders().size() > 0) {
				//TODO :废单重下实现
				resultHandle = new ResultHandle();
				resultHandle.setMsg("暂时不支持废单重下。");
				
				LOG.info("SupplierOrderHandleSverviceImpl.updateOrderCancelStatus:order[ID=" + suppOrderRelated.getOrderId() + "]暂时不支持废单重下。");
			//取消订单
			} else {
				Long orderId=suppOrderRelated.getOrderId();
				String cancelCode=OrderEnum.ORDER_CANCEL_CODE.SUPPLIER_CANCEL.name();
				String reason="供应商取消";
				String operatorId="SUPPLIER";
				
				resultHandle = orderLocalService.cancelOrder(orderId, cancelCode, reason, operatorId, "第三方供应商取消订单。");
				LOG.info("SupplierOrderHandleSverviceImpl.updateOrderCancelStatus:orderId=" + orderId + ", resultHandle.isSuccess=" + resultHandle.isSuccess() + ",msg=" + resultHandle.getMsg());
				
//				resultHandle = orderUpdateService.updateCancelOrder(orderId,cancelCode ,reason , operatorId, null);
//				LOG.info("SupplierOrderHandleSverviceImpl.updateOrderCancelStatus:orderId=" + orderId + ", resultHandle.isSuccess=" + resultHandle.isSuccess());
//				
//				String addition = cancelCode + "_=_" + reason + "_=_" + operatorId;
//				if (resultHandle.isSuccess()) {
//					orderMessageProducer.sendMsg(MessageFactory.newOrderCancelMessage(orderId, addition));
//					
//					LOG.info("SupplierOrderHandleSverviceImpl.updateOrderCancelStatus: send rderCancelMessage");
//				}
			}
		} else {
			resultHandle = new ResultHandle();
			resultHandle.setMsg("SuppOrderRelated.orderId[" + suppOrderRelated.getOrderId() + "] is null.");
			
			LOG.debug("SupplierOrderHandleSverviceImpl.updateOrderCancelStatus:SuppOrderRelated.orderId[" + suppOrderRelated.getOrderId() + "] is null.");
		}
		
		return resultHandle;
	}
	
	@Override
	public ResultHandleT<List<OrdOrderItem>> checkOrderTicketValid(final String  addCode) {
		ResultHandleT<List<OrdOrderItem>> handle = new ResultHandleT<List<OrdOrderItem>>();
		List<OrdPassCode> passCodeList = ordPassCodeDao.getOrdPassCodeByCheckInAndCode(null, addCode);
		if(passCodeList.isEmpty()){
			handle.setMsg("不存在当前的辅助码信息");
			return handle;
		}
		
		OrdTicketPerform ordTicketPerform = new OrdTicketPerform();
		ordTicketPerform.setVisitTime(new Date());
		List<OrdOrderItem> orderItemList = new ArrayList<OrdOrderItem>();
		for(OrdPassCode pc:passCodeList){
			try{
				
				OrdOrderItem orderItem = orderUpdateService.getOrderItem(pc.getOrderItemId());
				checkOrderItemValid(ordTicketPerform, orderItem);
				orderItemList.add(orderItem);
			}catch(Exception ex){
				if(!(ex instanceof IllegalArgumentException)){
					LOG.error(ExceptionFormatUtil.getTrace(ex));
				}
				handle.setMsg(ex);
			}
			
		}
		if(orderItemList.isEmpty()){
			handle.setMsg("不存在有效的子订单");
		}
		handle.setReturnContent(orderItemList);
		return handle;
	}

	public void checkParam(OrdTicketPerform ordTicketPerform) {
		Assert.notNull(ordTicketPerform.getOrderItemId(),"订单子项不可以为空");
		Assert.notNull(ordTicketPerform.getVisitTime(),"游玩时间不可以为空");
		Assert.notNull(ordTicketPerform.getActualAdult(),"实际游玩成人人数不可以为空");
		Assert.notNull(ordTicketPerform.getActualChild(),"实际游玩儿童人数");
	}

	public void checkOrderItemValid(OrdTicketPerform ordTicketPerform,
			OrdOrderItem orderItem) {
		if(orderItem==null){
			throw new IllegalArgumentException("订单子项不存在");
		}
		OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderItem.getOrderId());
		LOG.info("OrderId:"+order.getOrderId()+"OrderStatus:"+order.getOrderStatus());
//		if(OrderEnum.ORDER_STATUS.CANCEL.name().equals(order.getOrderStatus())||OrderEnum.ORDER_STATUS.CANCEL.name().equals(orderItem.getOrderStatus())){
//			throw new IllegalArgumentException("订单已经取消");
//		}
		String processKey = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.processKey.name());
		if(!"ticket".equalsIgnoreCase(processKey)
				&&!"ticket_new".equalsIgnoreCase(processKey)
				&&!"ticket_neworder".equalsIgnoreCase(processKey)
			    &&!(StringUtil.isNotEmptyString(processKey) && processKey.startsWith("ticket"))){
			throw new IllegalArgumentException("订单子项不属于门票不可以操作");
		}
		if(OrderEnum.ORDER_PERFORM_STATUS.PERFORM.name().equalsIgnoreCase(orderItem.getPerformStatus())){
			throw new IllegalArgumentException("订单子项履行状态不可以再次更改");
		}

		if(orderItem.hasTicketAperiodic()){
            if(orderItem.getValidEndTime() != null){
                Date date = DateUtils.addDays(orderItem.getValidEndTime(), 1);
                if(!ordTicketPerform.getVisitTime().before(date)){
                    throw new IllegalArgumentException("期票超过有效期截止日期不可以操作");
                }
            }
		}else{
			//二维码的
			//去掉对游玩有效期的控制
			/*
			if(SuppGoods.NOTICETYPE.QRCODE.name()
					.equals(orderItem.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()))){
				Date end = DateUtils.addDays(orderItem.getVisitTime(), orderItem.getValidDays().intValue());
				if(ordTicketPerform.getPerformTime()!=null){
					//传过来的时间晚于截止时间或当前时间晚于截止时间
					if(ordTicketPerform.getVisitTime().after(end)||ordTicketPerform.getPerformTime().after(end)){
						throw new IllegalArgumentException("通关时间晚于截止时间不可以操作");
					}
				}else{
					//传过来的时间晚于截止时间或当前时间晚于截止时间
					if(ordTicketPerform.getVisitTime().after(end)||new Date().after(end)){
						throw new IllegalArgumentException("当前时间晚于截止时间不可以操作");
					}
				}
				
			}*/
		}
		
	}


	@Override
	public ResultHandleT<OrdPassCode> saveOrdTicketPerform(OrdTicketPerform ordTicketPerform) {
		// TODO Auto-generated method stub
		ResultHandleT<OrdPassCode> resultHandle  = new ResultHandleT<OrdPassCode>();;
		checkParam(ordTicketPerform);
		
		OrdOrderItem orderItem = orderUpdateService.getOrderItem(ordTicketPerform.getOrderItemId());
		checkOrderItemValid(ordTicketPerform, orderItem);
		OrdTicketPerform entity = ordTicketPerformDao.selectByOrderItem(ordTicketPerform.getOrderItemId());
		saveOrdTicketPerformDetail(entity, ordTicketPerform, orderItem);//子履行状态
		if (entity == null) {//履行记录不存在时，补偿履行记录
			entity = new OrdTicketPerform();
			entity.setOrderId(orderItem.getOrderId());
			entity.setOrderItemId(orderItem.getOrderItemId());
			entity.setVisitTime(orderItem.getVisitTime());
			entity.setActualAdult(ordTicketPerform.getActualAdult());
			entity.setActualChild(ordTicketPerform.getActualChild());
			entity.setCreateTime(new Date());
			
			//当地玩乐：美食 娱乐 购物 交通接驳 
			if(isLocalPlayAndNoAdultChild(orderItem.getCategoryId())){
				entity.setAdultQuantity(1L);// 成人数
				entity.setChildQuantity(0L);// 儿童数
			}else{
				entity.setAdultQuantity(orderItem.getAdultQuantity());// 成人数
				entity.setChildQuantity(orderItem.getChildQuantity());// 儿童数
			}
			
			entity.setMemo(ordTicketPerform.getMemo());
			if (ordTicketPerform.getPerformTime() != null) {
				entity.setPerformTime(ordTicketPerform.getPerformTime());
			} else {
				entity.setPerformTime(new Date());
			}
			entity.setVisitTime(ordTicketPerform.getVisitTime());
			entity.setOperator(ordTicketPerform.getOperator());
			
			int result = ordTicketPerformDao.insert(entity);
			if(result <= 0){
				throw new IllegalArgumentException("二维码门票履行记录补偿失败!");
			}
		} else {//履行记录存在
			if((ordTicketPerform.getActualAdult()==null?0:ordTicketPerform.getActualAdult()) +(ordTicketPerform.getActualChild()==null?0:ordTicketPerform.getActualChild())
					>(entity.getActualAdult()==null?0:entity.getActualAdult()) +(entity.getActualChild()==null?0:entity.getActualChild())){
				entity.setActualAdult(ordTicketPerform.getActualAdult());
				entity.setActualChild(ordTicketPerform.getActualChild());
				//当地玩乐：美食 娱乐 购物 交通接驳
				if(isLocalPlayAndNoAdultChild(orderItem.getCategoryId())){
					entity.setAdultQuantity(1L);// 成人数
					entity.setChildQuantity(0L);// 儿童数
				}
				entity.setMemo(ordTicketPerform.getMemo());
				if (ordTicketPerform.getPerformTime() != null) {
					entity.setPerformTime(ordTicketPerform.getPerformTime());
				} else {
					entity.setPerformTime(new Date());
				}
				entity.setVisitTime(ordTicketPerform.getVisitTime());
				if(StringUtil.isEmptyString(entity.getOperator())){
					entity.setOperator(ordTicketPerform.getOperator());
				}else{
					//如果已经有operator，则添加逗号
					entity.setOperator(entity.getOperator()+","+ordTicketPerform.getOperator());
				}
				int result = ordTicketPerformDao.updateByPrimaryKey(entity);
				if (result <= 0) {
					throw new IllegalArgumentException("二维码门票履行记录更新失败!");
				}
			}
		}
		//更新子订单履行状态
		//当地玩乐：美食 娱乐 购物 交通接驳 
		if(isLocalPlayAndNoAdultChild(orderItem.getCategoryId())){
			if (((ordTicketPerform.getActualAdult() == null ? 0 : ordTicketPerform.getActualAdult()) 
					+ (ordTicketPerform.getActualChild() == null ? 0 : ordTicketPerform.getActualChild())) 
				!=  (orderItem.getQuantity() == null ? 0 : orderItem.getQuantity())){
				orderItem.setPerformStatus(OrderEnum.ORDER_PERFORM_STATUS.PART_PERFORM.name());
			}else{
				orderItem.setPerformStatus(OrderEnum.ORDER_PERFORM_STATUS.PERFORM
					.name());
			}
		}else{
			if (((ordTicketPerform.getActualAdult() == null ? 0 : ordTicketPerform.getActualAdult()) 
					+ (ordTicketPerform.getActualChild() == null ? 0 : ordTicketPerform.getActualChild())) 
				!= ( orderItem.getAdultQuantity()+ orderItem.getChildQuantity())
					         * (orderItem.getQuantity() == null ? 0 : orderItem.getQuantity())){
				orderItem.setPerformStatus(OrderEnum.ORDER_PERFORM_STATUS.PART_PERFORM.name());
			}else{
				orderItem.setPerformStatus(OrderEnum.ORDER_PERFORM_STATUS.PERFORM
					.name());
			}
		}
		
		orderUpdateService.updateOrderItemByIdSelective(orderItem);
		
		updateOrderPerformStatus(orderItem);
		OrdPassCode opc = ordPassCodeDao.getOrdPassCodeByOrderItemId(ordTicketPerform.getOrderItemId());
		if(opc!=null){
			resultHandle.setReturnContent(opc);
		}
		return resultHandle;
	}
	private void updateOrderPerformStatus(OrdOrderItem orderItem ){
	OrdOrder order=	complexQueryService.queryOrderByOrderId(orderItem.getOrderId());
	if (OrderUtils.isTicketByCategoryId(order.getCategoryId())) {
		
		//门票业务类订单使用状态
		List<OrdTicketPerform> resultList = new ArrayList<OrdTicketPerform>();
		List<OrdOrderItem> ordItemsList =order.getOrderItemList();
		//订单使用状态
		List<String> perFormStatusList = new ArrayList<String>();
		if(ordItemsList != null) {
			for (OrdOrderItem ordOrderItem : ordItemsList) {
				//门票业务类订单使用状态
				Map<String,Object> performMap = ordOrderItem.getContentMap();
				String categoryCode =  (String) performMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
				if (ProductCategoryUtil.isTicket(categoryCode)) {
				resultList = complexQueryService.selectByOrderItem(ordOrderItem.getOrderItemId());
				String performStatusName=OrderUtils.calPerformStatus(resultList,order,ordOrderItem);
				perFormStatusList.add(performStatusName);
				}
			}
		}
		String orderPerformStatus=OrderUtils.getMainOrderPerformStatusCode(perFormStatusList);
		orderUpdateService.updateOrderPerformStatus(orderItem.getOrderId(), orderPerformStatus);
		
	}
	};
	
	
	private void saveOrdTicketPerformDetail(OrdTicketPerform entity, OrdTicketPerform ordTicketPerform, OrdOrderItem orderItem){
		OrdTicketPerformDetail ordTicketPerformDetail = new OrdTicketPerformDetail();
		ordTicketPerformDetail.setOrderItemId(orderItem.getOrderItemId());
		ordTicketPerformDetail.setCreateTime(new Date());
		ordTicketPerformDetail.setMemo(ordTicketPerform.getMemo());
		if (ordTicketPerform.getPerformTime() != null)
			ordTicketPerformDetail.setPerformTime(ordTicketPerform.getPerformTime());
		else
			ordTicketPerformDetail.setPerformTime(new Date());

		if (entity == null) {//主履行记录不存在时
			ordTicketPerformDetail.setActualAdult(ordTicketPerform.getActualAdult());
			ordTicketPerformDetail.setActualChild(ordTicketPerform.getActualChild());
			int result = ordTicketPerformDetailDao.insert(ordTicketPerformDetail);
			if(result <= 0){
				throw new IllegalArgumentException("二维码门票子履行记录新增失败!");
			}
		} else {//主履行记录存在
			Long actualAdultDiff = (ordTicketPerform.getActualAdult() == null ? 0 : ordTicketPerform.getActualAdult())
					- (entity.getActualAdult() == null ? 0 : entity.getActualAdult());
			Long actualChildDiff = (ordTicketPerform.getActualChild() == null ? 0 : ordTicketPerform.getActualChild())
					- (entity.getActualChild() == null ? 0 : entity.getActualChild());
			if(actualAdultDiff>0 || actualChildDiff>0){
				ordTicketPerformDetail.setActualAdult(actualAdultDiff);
				ordTicketPerformDetail.setActualChild(actualChildDiff);
				int result = ordTicketPerformDetailDao.insert(ordTicketPerformDetail);
				if(result <= 0){
					throw new IllegalArgumentException("二维码门票子履行记录新增失败!");
				}
			}
		}
	}

	@Override
	public void savePassCode(List<OrdPassCode> list) {
		for(OrdPassCode code:list){
//			if(StringUtils.isEmpty(code.getCode())){ by yongchun
//				throw new IllegalArgumentException("辅助码为空");
//			}
			if(ordPassCodeDao.selectCountByOrderItemId(code.getOrderItemId())>0){
				OrdPassCode entity = ordPassCodeDao.getOrdPassCodeByOrderItemId(code.getOrderItemId());
				if(entity!=null){
					entity.setAddCode(code.getAddCode());
					entity.setCode(code.getCode());
//					entity.setCodeImage(code.getCodeImage());
					entity.setPassSerialno(code.getPassSerialno());
					entity.setUrl(code.getUrl());//第三方二维码Url
					entity.setCodeImageFlag(code.getCodeImageFlag()); //是否含有二维码
					entity.setPicFilePath(code.getPicFilePath());
					entity.setPassExtid(code.getPassExtid());
					ordPassCodeDao.update(entity);
				}
			}else{
				ordPassCodeDao.insert(code);
			}
		}
		if(CollectionUtils.isNotEmpty(list)){
			OrdOrderItem ordOrderItem = ordOrderItemService.selectOrderItemByOrderItemId(list.get(0).getOrderItemId());
			Map<String,Object> paramMap = new HashMap<String, Object>();
			if(ordOrderItem!=null){
				paramMap.put("orderId", ordOrderItem.getOrderId());
				List<OrdOrderItem> ordOrderItemlist  = iOrdOrderItemService.selectByParams(paramMap);
				OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(ordOrderItem.getOrderId());
				boolean isFinish =false;  //订单是否完成生成凭证
					for (OrdOrderItem orderItem : ordOrderItemlist) {//如果为对接
						boolean isSupplierOrder = false;
						String supplierApiFlag =  (String)orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.supplierApiFlag.name());
						if ("Y".equalsIgnoreCase(supplierApiFlag)) {
								isSupplierOrder = true;
							}
						if(!isSupplierOrder && StringUtils.equals(orderItem.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()), SuppGoods.NOTICETYPE.QRCODE.name())){
								isSupplierOrder = true;
							}
						if(!isSupplierOrder){//非对接订单验证是否生成凭证
							if(order.getPaymentTarget().equals(SuppGoods.PAYTARGET.PREPAID.name())){ //预付则判断是否已全部支付
								isFinish = orderItem.getPaymentStatus().equals(OrderEnum.PAYMENT_STATUS.PAYED.getCode());
							}else{
								isFinish = true;  //到付则直接通过
							}
						}else{//对接订单验证是否生成凭证
							isFinish = (ordPassCodeDao.selectCountByOrderItemId(orderItem.getOrderItemId())>0);
						}
					}
					if(isFinish){
						OrdOrderTracking ordOrderTracking = new OrdOrderTracking();
						ordOrderTracking.setOrderId(ordOrderItem.getOrderId());
						ordOrderTracking.setCategoryId(order.getCategoryId());
						ordOrderTracking.setChangeStatusTime(new Date());
						ordOrderTracking.setCreateTime(new Date());
						ordOrderTracking.setOrderStatus(OrderEnum.ORDER_TRACKING_STATUS.CREDITED.getCode());
						ordOrderTrackingService.saveOrderTracking(ordOrderTracking);
						LOG.info("SupplierOrderHandleServiceImpl#savePassCode:"+ordOrderTracking.getOrderId());
					}
			}
		}else{
			LOG.info("SupplierOrderHandleServiceImpl#savePassCode: list is null");
		}
	}
	@Autowired
	private OrdPassCodeDao ordPassCodeDao;

	@Override
	public void updatePassCode(List<OrdPassCode> list) {
		for(OrdPassCode code:list){
			 ordPassCodeDao.updatePicFilePath(code);
			 LOG.info("SupplierOrderHandleServiceImpl#updatePassCode:"+code.getPassSerialno()+","+code.getPicFilePath());
		}
		
	}

	@Override
	public List<OrdPassCode> getOrdPassCodeByCheckInAndCode(Long checkInId,String addCode) {
		List<OrdPassCode> ordPassCodeList = ordPassCodeDao.getOrdPassCodeByCheckInAndCode(checkInId, addCode);
		if(CollectionUtils.isEmpty(ordPassCodeList)) {
			return ordPassCodeList;
		}
		
		addCodeImage(ordPassCodeList);
		
		return ordPassCodeList;
	}

	@Override
	public OrdPassCode getOrdPassCodeByOrderItemId(Long orderItemInId) {
		OrdPassCode ordPassCode = ordPassCodeDao.getOrdPassCodeByOrderItemId(orderItemInId);
		if(ordPassCode != null) {
			ordPassCode.setCodeImage(this.supplierOrderOtherService.getCodeImage(ordPassCode.getPassSerialno()));
			PassCodeImageVo passCodeImageVo = passCodeService.getPassCodeImageBySerialNo(ordPassCode.getPassSerialno());
			if(passCodeImageVo!=null){
				ordPassCode.setPicFilePath(passCodeImageVo.getPicFilePath());
				ordPassCode.setPicFilePathList(passCodeImageVo.getPicFilePathList());
			}
			ordPassCode.setFileId(this.supplierOrderOtherService.getPdfFileId(ordPassCode.getPassSerialno()));

			LOG.info("getOrdPassCodeByOrderItemId, orderItemInId:"
					+ orderItemInId + ", passSerialNo:"
					+ ordPassCode.getPassSerialno() + ", codeImage:"
					+ ordPassCode.getCodeImage() + ", fileId:"
					+ ordPassCode.getFileId());
		}
		
		return ordPassCode;
	}

	@Override
	public List<OrdPassCode> selectOrdPassCodeByParams(Map<String, Object> params) {
		List<OrdPassCode> ordPassCodeList = ordPassCodeDao.findByParams(params);
		if(CollectionUtils.isEmpty(ordPassCodeList)) {
			return ordPassCodeList;
		}
		
		addCodeImage(ordPassCodeList);
		
		return ordPassCodeList;
	}
	
	private void addCodeImage(List<OrdPassCode> ordPassCodeList) {
		if(CollectionUtils.isEmpty(ordPassCodeList)) {
			return;
		}
		List<String> passSerialNos = new ArrayList<String>();
		int idx = 0;
		Map<String, byte[]> codeImageMap = new HashMap<String, byte[]>();
		for(OrdPassCode ordPassCode : ordPassCodeList) {
			if(StringUtils.isNotBlank(ordPassCode.getPassSerialno())) {
				passSerialNos.add(ordPassCode.getPassSerialno());
			}
			idx ++;
			if((passSerialNos.size() >= 100 || idx == ordPassCodeList.size()) && CollectionUtils.isNotEmpty(passSerialNos)) {
				codeImageMap.putAll(supplierOrderOtherService.getCodeImages(passSerialNos));
				passSerialNos = new ArrayList<String>();
			}
		}
		
		for(OrdPassCode ordPassCode : ordPassCodeList) {
			if(StringUtils.isNotBlank(ordPassCode.getPassSerialno()) && codeImageMap.containsKey(ordPassCode.getPassSerialno())) {
				ordPassCode.setCodeImage(codeImageMap.get(ordPassCode.getPassSerialno()));
			}
			ordPassCode.setFileId(this.supplierOrderOtherService.getPdfFileId(ordPassCode.getPassSerialno()));
		}
	}

	@Override
	public List<OrdTicketPerform> selectOrdTicketPerforms(
			Map<String, Object> params) {
		return ordTicketPerformDao.findOrdTicketPerformList(params);
	}
	
	private boolean isLocalPlayAndNoAdultChild(Long categoryId){
		//当地玩乐：美食 娱乐 购物 交通接驳 
		if(BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(categoryId)||
			BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(categoryId)||
				BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(categoryId)||
					BizEnum.BIZ_CATEGORY_TYPE.category_wifi.getCategoryId().equals(categoryId)||
						BizEnum.BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(categoryId)){
			return true;
		}
		return false;
	}

	@Override
	public boolean getProductIfYL(Long productId) {
		boolean result = false;
		try {
			IntfStylProdRela intfStylProdRela = suppTicketProductClientRemote.queryExistSuppProdRela(productId);
			if(null != intfStylProdRela && intfStylProdRela.getProdRelaId()!=null && intfStylProdRela.getYlProdId() !=null){
				result = true;
			}
		} catch (Exception e) {
			LOG.error("SupplierOrderHandleServiceImpl#getProductIfYL: " + e);
		}
		return result;
	}

}
