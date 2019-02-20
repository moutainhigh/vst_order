package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.order.dao.*;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.play.connects.po.OrderConnectsServiceProp;
import com.lvmama.vst.back.wifi.po.OrdOrderWifiPickingPoint;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderFlagParam;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.comm.vo.order.OrderMonitorCnd;
import com.lvmama.vst.ebooking.client.ebk.serivce.EbkCertifClientService;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif;
import com.lvmama.vst.order.builder.IComplexQuerySQLBuilder;
import com.lvmama.vst.order.builder.IComplexQuerySQLDirector;
import com.lvmama.vst.order.builder.impl.ComplexQuerySQLDirectorImpl;
import com.lvmama.vst.order.builder.sql.OrderCountSQLBuilderImpl;
import com.lvmama.vst.order.builder.sql.OrderQuerySQLBuilderImpl;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdOrderQueryInfoService;

/**
 * 综合查询业务实现类
 * 
 * @author wenzhengtao
 * 
 */
@Service("complexQueryService")
public class ComplexQueryServiceImpl implements IComplexQueryService {
	
	private final Logger log = LoggerFactory.getLogger(ComplexQueryServiceImpl.class);

	@Autowired
	private ComplexQueryDAO complexQueryDAO;
	@Autowired
	private OrdOrderItemExtendDao ordOrderItemExtendDao;

	@Autowired
	private OrdOrderDao ordOrderDao;

	@Autowired
	private OrdTicketPerformDao ordTicketPerformDao;
	
	@Autowired
	private OrdTicketPerformDetailDao ordTicketPerformDetailDao;
	
	@Autowired
	private OrdComplexSqlDao ordComplexSqlDao;

	@Autowired
	private OrdOrderStockDao ordOrderStaockDao;

	@Autowired
	private OrdOrderItemDao ordOrderItemDao;

	@Autowired
	private OrdOrderHotelTimeRateDao ordOrderHotelTimeRateDao;

	@Autowired
	private OrdItemPersonRelationDao ordItemPersonRelationDao;

	@Autowired
	private OrdPersonDao ordPersonDao;

	@Autowired
	private OrdAddressDao ordAddressDao;

	@Autowired
	private OrdAdditionStatusDAO ordAdditionStatusDao;

	@Autowired
	private OrdTravelContractDAO ordTravekContractDao;

	@Autowired
	private OrdOrderPackDao ordOrderPackDao;

	@Autowired
	private OrdOrderAmountItemDao ordOrderAmountItemDao;

	@Autowired
	private OrdGuaranteeCreditCardDao ordGuaranteeCreditCardDao;

	@Autowired
	private OrdCourierListingDao ordCourierListingDao;

	@Autowired
	private OrdFormInfoDao ordFormInfoDao;
	
	@Autowired
	private OrdMulPriceRateDAO ordMulPriceRateDAO;
	
	@Autowired
	private IOrdOrderQueryInfoService orderQueryInfoService;
	
	@Autowired
	private EbkCertifClientService ebkCertifClientService;

	@Autowired
	private OrderService orderService;
	
	private final int QUERY_LIST_COUNT = 500;

	/**
	 * 以订单为中心综合查询
	 */
	@Override
	public List<OrdOrder> queryOrderListByCondition(
			final ComplexQuerySQLCondition condition) {
		//当查询条件中没有订单号，也没有子订单号时走读写分离逻辑
		if (condition.getOrderIndentityParam() == null
				|| (condition.getOrderIndentityParam().getOrderId() == null && condition
						.getOrderIndentityParam().getOrderItemId() == null)) {
			return this.checkOrderListFromReadDB(condition);
		}
		final String completeSQL = getConditionSqlContent(condition);
		// 查询订单集合
		List<OrdOrder> orderList = complexQueryDAO.queryList(OrdOrder.class,
				completeSQL);
		// 返回结果集
		if (!orderList.isEmpty()) {
			// 合并相关表数据
			return this.fillOrderRalatedInfo(orderList, completeSQL,
					condition);
		}
		return orderList;
	}


	private List<OrdOrder> fillOrderRalatedInfo(
			final List<OrdOrder> orderList, final String queryOrderSql,
			final ComplexQuerySQLCondition condition) {
		List<Long> orderIds = new ArrayList<Long>();
		for (OrdOrder order : orderList) {
			orderIds.add(order.getOrderId());
		}
		// 初始化订单相关表list
		List<OrdOrderItem> orderItemList = null;// 订单子项
		List<OrdPerson> orderPersonList = null;// 订单人
		List<OrdAddress> orderAddressList = null;// 订单收货地址
		List<OrdOrderPack> orderPackList = null;// 订单打包
		List<OrdOrderAmountItem> orderAmountItemList = null;// 订单金额
		List<OrdGuaranteeCreditCard> orderGuaranteeCreditCardList = null;// 订单担保
		List<OrdOrderHotelTimeRate> orderHotelTimeRateList = null;// 订单酒店
		List<OrdOrderWifiTimeRate> orderWifiTimeRateList = null;// 订单Wifi
		List<OrdOrderWifiPickingPoint> ordOrderPickingPointList = null;//订单网点
		List<OrdOrderStock> orderStockList = null;// 订单库存
		List<OrdOrderStock> orderStockListForHotelTimeRate = new ArrayList<OrdOrderStock>();// 订单酒店的库存
		List<OrdOrderStock> orderStockListForWifiTimeRate = new ArrayList<OrdOrderStock>();// 订单Wifi的库存
		List<OrdAdditionStatus> ordAdditionStatusList = null;// 状态
		List<OrdTravelContract> ordTravelContractList = null;// 订单合同
		List<OrdItemPersonRelation> ordItemPersonRelationList = null;
		List<OrdCourierListing> ordCourierListingList = null;// 快递寄件清单
		List<OrderConnectsServiceProp> orderConnectsServicePropList = null; //当地玩乐的服务信息
		// 初始化订单相关表map
		Map<Long, List<OrdOrderItem>> orderItemMap = new HashMap<Long, List<OrdOrderItem>>();
		Map<Long, List<OrdPerson>> orderPersonMap = new HashMap<Long, List<OrdPerson>>();
		Map<Long, List<OrdAddress>> ordAddressMap = new HashMap<Long, List<OrdAddress>>();
		Map<Long, List<OrdOrderPack>> orderPackMap = new HashMap<Long, List<OrdOrderPack>>();
		Map<Long, List<OrdOrderAmountItem>> orderAmountItemMap = new HashMap<Long, List<OrdOrderAmountItem>>();
		Map<Long, List<OrdGuaranteeCreditCard>> orderGuaranteeCreditCardMap = new HashMap<Long, List<OrdGuaranteeCreditCard>>();
		Map<Long, List<OrdOrderHotelTimeRate>> orderHotelTimeRateMap = new HashMap<Long, List<OrdOrderHotelTimeRate>>();
		Map<Long, List<OrdOrderWifiTimeRate>> orderWifiTimeRateMap = new HashMap<Long, List<OrdOrderWifiTimeRate>>();
		Map<Long, List<OrdOrderWifiPickingPoint>> orderPickingPointMap = new HashMap<Long, List<OrdOrderWifiPickingPoint>>();
		Map<Long, List<OrdOrderStock>> orderStockMap = new HashMap<Long, List<OrdOrderStock>>();
		Map<Long, List<OrdOrderStock>> orderStockMapForHotelRate = new HashMap<Long, List<OrdOrderStock>>();
		Map<Long, List<OrdOrderStock>> orderStockMapForWifiRate = new HashMap<Long, List<OrdOrderStock>>();
		Map<Long, List<OrdAdditionStatus>> ordAdditionStatusMap = new HashMap<Long, List<OrdAdditionStatus>>();
		Map<Long, List<OrdTravelContract>> ordTravelContractMap = new HashMap<Long, List<OrdTravelContract>>();
		Map<Long, List<OrdItemPersonRelation>> ordItemPersonRelationMap = new HashMap<Long, List<OrdItemPersonRelation>>();
		Map<Long, List<OrdCourierListing>> ordCourierListingMap = new HashMap<Long, List<OrdCourierListing>>();
		//当地玩乐的交通接驳
		Map<Long, List<OrderConnectsServiceProp>> orderConnectsServicePropMap = new HashMap<Long, List<OrderConnectsServiceProp>>();
		/******************************** 填充与订单子项直接关联的数据 **********************************************/
		// 根据订单ID关联查询订单子项集合
		if (condition.getOrderFlagParam().isOrderItemTableFlag()) {

			orderItemList = ordComplexSqlDao
					.selectDistinctOrderItemsByorderIds(orderIds);
			// orderItemList = complexQueryDAO.queryOrderRelatedInfo(
			// OrdOrderItem.class, queryOrderSql, condition);
			if (orderItemList != null && !orderItemList.isEmpty()) {
				orderItemMap = this.getOrderItemMap(orderItemList);// 将list转为map方便处理
			}

			/******************************** 填充与订单子项直接关联的数据 **********************************************/
			// 填充订单子项的库存数据
			if (orderItemList != null && !orderItemList.isEmpty()
					&& condition.getOrderFlagParam().isOrderStockTableFlag()) {

				orderStockList = ordComplexSqlDao
						.selectDistinctStocksByOrderIds(orderIds);
				// orderStockList = complexQueryDAO.queryOrderRelatedInfo(
				// OrdOrderStock.class, queryOrderSql, condition);
				if (orderStockList != null && !orderStockList.isEmpty()) {
					orderStockMap = this.getOrderStockMap(orderStockList);
					for (OrdOrderItem orderItem : orderItemList) {
						if (!orderStockMap.isEmpty()
								&& orderStockMap.containsKey(orderItem
										.getOrderItemId())) {
							orderItem.setOrderStockList(orderStockMap
									.get(orderItem.getOrderItemId()));// 这里是全部的库存数据
						}
						if("STAMP".equals(orderItem.getOrderSubType())){
							
						}
						
					}
					// 切分酒店库存数据
					orderStockListForHotelTimeRate = this
							.getOrderStockByObjectType(
									OrderEnum.ORDER_STOCK_OBJECT_TYPE.HOTEL_TIME_RATE
											.toString(), orderStockList);
					// 切分Wifi库存数据
					orderStockListForWifiTimeRate = this
							.getOrderStockByObjectType(
									OrderEnum.ORDER_STOCK_OBJECT_TYPE.WIFI_TIME_RATE
											.toString(), orderStockList);
				}
			}

			// 填充订单子项的酒店使用情况数据
			if (!orderItemList.isEmpty()) {
				if (condition.getOrderFlagParam()
						.isOrderHotelTimeRateTableFlag()) {
					if (UtilityTool.isValid(condition
							.getOrderRelationSortParam()
							.getOrderHotelTimeRateSort())) {
						// 带排序
						orderHotelTimeRateList = ordComplexSqlDao
								.selectDistinctHotelTimeRatesSortByOrderIds(orderIds);
					} else {
						orderHotelTimeRateList = ordComplexSqlDao
								.selectDistinctHotelTimeRatesByOrderIds(orderIds);
					}
					// orderHotelTimeRateList = complexQueryDAO
					// .queryOrderRelatedInfo(OrdOrderHotelTimeRate.class,
					// queryOrderSql, condition);
					if (orderHotelTimeRateList != null
							&& !orderHotelTimeRateList.isEmpty()) {
						orderHotelTimeRateMap = this
								.getOrderHotelTimeRateMap(orderHotelTimeRateList);
						for (OrdOrderItem orderItem : orderItemList) {
							if (!orderHotelTimeRateMap.isEmpty()
									&& orderHotelTimeRateMap
											.containsKey(orderItem
													.getOrderItemId())) {
								orderItem
										.setOrderHotelTimeRateList(orderHotelTimeRateMap
												.get(orderItem.getOrderItemId()));
							}
						}

						// 填充酒店库存数据
						if (orderStockListForHotelTimeRate != null
								&& !orderStockListForHotelTimeRate.isEmpty()) {
							orderStockMapForHotelRate = this
									.getOrderStockForHotelTimeRateMap(orderStockListForHotelTimeRate);
							for (OrdOrderHotelTimeRate orderHotelTimeRate : orderHotelTimeRateList) {
								if (!orderStockMapForHotelRate.isEmpty()
										&& orderStockMapForHotelRate
												.containsKey(orderHotelTimeRate
														.getHotelTimeRateId())) {
									orderHotelTimeRate
											.setOrderStockList(orderStockMapForHotelRate.get(orderHotelTimeRate
													.getHotelTimeRateId()));
								}
							}
						}
					}
				}
				
				if (condition.getOrderFlagParam()
						.isOrderWifiTimeRateTableFlag()) {
					if (UtilityTool.isValid(condition
							.getOrderRelationSortParam()
							.getOrderWifiTimeRateSort())) {
						// 带排序
						orderWifiTimeRateList = ordComplexSqlDao
								.selectDistinctWifiTimeRatesSortByOrderIds(orderIds);
					} else {
						orderWifiTimeRateList = ordComplexSqlDao
								.selectDistinctWifiTimeRatesByOrderIds(orderIds);
					}
				
					if (orderWifiTimeRateList != null
							&& !orderWifiTimeRateList.isEmpty()) {
						orderWifiTimeRateMap = this
								.getOrderWifiTimeRateMap(orderWifiTimeRateList);
						for (OrdOrderItem orderItem : orderItemList) {
							if (!orderWifiTimeRateMap.isEmpty()
									&& orderWifiTimeRateMap
											.containsKey(orderItem
													.getOrderItemId())) {
								orderItem.setOrdOrderWifiTimeRateList(orderWifiTimeRateMap.get(orderItem.getOrderItemId()));
										
							}
						}

						// 填充Wifi库存数据
						if (orderStockListForWifiTimeRate != null
								&& !orderStockListForWifiTimeRate.isEmpty()) {
							orderStockMapForWifiRate = this
									.getOrderStockForHotelTimeRateMap(orderStockListForWifiTimeRate);
							for (OrdOrderWifiTimeRate ordOrderWifiTimeRate : orderWifiTimeRateList) {
								if (!orderStockMapForWifiRate.isEmpty()
										&& orderStockMapForWifiRate
												.containsKey(ordOrderWifiTimeRate
														.getWifiTimeRateId())) {
									ordOrderWifiTimeRate
											.setOrderStockList(orderStockMapForWifiRate.get(ordOrderWifiTimeRate
													.getWifiTimeRateId()));
								}
							}
						}
					}
				}
				
				if (condition.getOrderFlagParam()
						.isOrdOrderPickingPointTableFlag()) {
					ordOrderPickingPointList = ordComplexSqlDao
							.selectDistinctPickingPointByOrderIds(orderIds);
					if (ordOrderPickingPointList != null
							&& !ordOrderPickingPointList.isEmpty()) {
						orderPickingPointMap = this
								.getOrdOrderPickingPointMap(ordOrderPickingPointList);
						for (OrdOrderItem orderItem : orderItemList) {
							if (!orderPickingPointMap.isEmpty()
									&& orderPickingPointMap
											.containsKey(orderItem
													.getOrderItemId())) {
								orderItem.setOrdOrderWifiPickingPointList(orderPickingPointMap.get(orderItem.getOrderItemId()));
										
							}
						}

						
					}
				}
				
				
				
				

				if (condition.getOrderFlagParam()
						.isOrderItemPersonRelationFlag()) {
					ordItemPersonRelationList = ordComplexSqlDao
							.selectPersonRelationByorderIds(orderIds);
					// ordItemPersonRelationList = complexQueryDAO
					// .queryOrderRelatedInfo(OrdItemPersonRelation.class,
					// queryOrderSql, condition);
					if (ordItemPersonRelationList != null
							&& !ordItemPersonRelationList.isEmpty()) {
						ordItemPersonRelationMap = getOrdItemPersonMap(ordItemPersonRelationList);
					}
				}
			}
			
			//添加成人价、成人数量、儿童价、儿童数量、房差价格、房差数量
			//by yecheng 2016.10.13
			if(CollectionUtils.isNotEmpty(orderItemList)){
				for(OrdOrderItem orderItem:orderItemList){
					Map<String, Object> omprParams = new HashMap<String,Object>();
					omprParams.put("orderItemId", orderItem.getOrderItemId());
					List<OrdMulPriceRate> ordMulPriceRateList = ordMulPriceRateDAO.selectByParams(omprParams);
					orderItem.setOrdMulPriceRateList(ordMulPriceRateList);
					
					//设置是否pdf电子票
					if(orderItem.isTicket()) {
						String flag = this.orderService.getCodeImagePdfFlag(orderItem.getSuppGoodsId());
						orderItem.setCodeImagePdfFlag(flag);
					}
				}
			}					
		}

		// 根据订单ID关联查询订单人集合
		if (condition.getOrderFlagParam().isOrderPersonTableFlag()) {
			log.info("ComplexQueryServiceImpl.orderPerson start query");
			orderPersonList = ordComplexSqlDao
					.selectDistinctOrderPersonsByOrderIds(orderIds);
			// orderPersonList = complexQueryDAO.queryOrderRelatedInfo(
			// OrdPerson.class, queryOrderSql, condition);
			if (orderPersonList != null && !orderPersonList.isEmpty()) {
				log.info("ComplexQueryServiceImpl.orderPerson.size:"+orderPersonList.size());
				orderPersonMap = this.getOrderPersonMap(orderPersonList);
			}
		}

		// 根据订单ID关联查询订单人对应地址
		if (condition.getOrderFlagParam().isOrderPersonTableFlag()
				&& condition.getOrderFlagParam().isOrderAddressTableFlag()) {
			orderAddressList = ordComplexSqlDao
					.selectDistinctAddressByorderIds(orderIds);
			// orderAddressList = complexQueryDAO.queryOrderRelatedInfo(
			// OrdAddress.class, queryOrderSql, condition);
			if (orderAddressList != null && !orderAddressList.isEmpty()) {
				ordAddressMap = this.getOrderAddressMap(orderAddressList);
			}
		}

		// 根据订单ID关联状态集合
		if (condition.getOrderFlagParam().isOrdAdditionStatusTableFlag()) {
			ordAdditionStatusList = ordComplexSqlDao
					.selectDistinctAdditionStatusByOrderIds(orderIds);
			// ordAdditionStatusList = complexQueryDAO.queryOrderRelatedInfo(
			// OrdAdditionStatus.class, queryOrderSql, condition);
			if (ordAdditionStatusList != null
					&& !ordAdditionStatusList.isEmpty()) {
				ordAdditionStatusMap = this
						.getOrdAdditionStatusMap(ordAdditionStatusList);
			}
		}

		// 根据订单ID关联订单合同集合
		if (condition.getOrderFlagParam().isOrdTravelContractTableFlag()) {
			ordTravelContractList = ordComplexSqlDao
					.selectDistinctTravelContractByOrderIds(orderIds);
			// ordTravelContractList = complexQueryDAO.queryOrderRelatedInfo(
			// OrdTravelContract.class, queryOrderSql, condition);
			if (ordTravelContractList != null
					&& !ordTravelContractList.isEmpty()) {
				ordTravelContractMap = this
						.getOrdTravelContractMap(ordTravelContractList);
			}
		}

		// 根据订单ID关联查询订单打包集合
		if (condition.getOrderFlagParam().isOrderPackTableFlag()) {
			orderPackList = ordComplexSqlDao
					.selectDistinctOrderPacksByOrderIds(orderIds);
			// orderPackList = complexQueryDAO.queryOrderRelatedInfo(
			// OrdOrderPack.class, queryOrderSql, condition);
			if (orderPackList != null && !orderPackList.isEmpty()) {
				orderPackMap = this.getOrderPackMap(orderPackList);
			}
		}

		// 根据订单ID关联查询订单金额变换集合
		if (condition.getOrderFlagParam().isOrderAmountItemTableFlag()) {
			orderAmountItemList = ordComplexSqlDao
					.selectDistinctAmountItemByOrderIds(orderIds);
			// orderAmountItemList = complexQueryDAO.queryOrderRelatedInfo(
			// OrdOrderAmountItem.class, queryOrderSql, condition);
			if (orderAmountItemList != null && !orderAmountItemList.isEmpty()) {
				orderAmountItemMap = this
						.getOrderAmountItemMap(orderAmountItemList);
			}
		}

		// 根据订单ID关联查询订单担保信息集合
		if (condition.getOrderFlagParam().isOrderGuaranteeCreditCardTableFlag()) {
			orderGuaranteeCreditCardList = ordComplexSqlDao
					.selectDistinctCreditCardByOrderIds(orderIds);
			// orderGuaranteeCreditCardList = complexQueryDAO
			// .queryOrderRelatedInfo(OrdGuaranteeCreditCard.class,
			// queryOrderSql, condition);
			if (orderGuaranteeCreditCardList != null
					&& !orderGuaranteeCreditCardList.isEmpty()) {
				orderGuaranteeCreditCardMap = this
						.getGuaranteeCreditCardMap(orderGuaranteeCreditCardList);
			}
		}

		// 根据订单ID关联查询快递寄件清单集合
		if (condition.getOrderFlagParam().isOrdCourierListing()) {
			ordCourierListingList = ordComplexSqlDao
					.selectDistinctCourierListingByOrderIds(orderIds);
			// ordCourierListingList = complexQueryDAO.queryOrderRelatedInfo(
			// OrdCourierListing.class, queryOrderSql, condition);
			if (ordCourierListingList != null
					&& !ordCourierListingList.isEmpty()) {
				ordCourierListingMap = this
						.getOrdCourierListingMap(ordCourierListingList);
			}
		}

		//根据订单Id关联查询服务信息（当地玩乐）
		if(condition.getOrderFlagParam().isOrderConnectsServicePropFlag()){
			log.info("ComplexQueryServiceImpl.交通接驳###根据订单Id关联查询服务信息（当地玩乐)，orderIDs==="+orderIds.toString());
			List<Long> connectsOrderIds  = new ArrayList<Long>();
			if(null != orderItemList && orderItemList.size() > 0){
				for(OrdOrderItem item : orderItemList){
					if(BizEnum.BIZ_CATEGORY_TYPE.category_connects.getCategoryId().compareTo(item.getCategoryId()) == 0){
						connectsOrderIds.add(item.getOrderId());
					}
				}
			}else{
				connectsOrderIds = orderIds;
			}
			if(null != connectsOrderIds && connectsOrderIds.size() > 0 ){
				orderConnectsServicePropList = this.ordComplexSqlDao.selectConnectsServicePropByOrderIds(connectsOrderIds);
				log.info("ComplexQueryServiceImpl.交通接驳###查询###根据订单Id关联查询服务信息（当地玩乐)，orderIDs==="+connectsOrderIds.toString());
			}
			if(CollectionUtils.isNotEmpty(orderConnectsServicePropList)){
				orderConnectsServicePropMap = this.getOrderConnectsServicePropMap(orderConnectsServicePropList);
			}
		}

		/************************************* 填充与订单直接关联的数据 ********************************************/
		for (OrdOrder order : orderList) {
			if (!orderItemMap.isEmpty()
					&& orderItemMap.containsKey(order.getOrderId())) {
			    order.setOrderItemList(orderItemMap.get(order.getOrderId()));
				/**************** add by DongNingBo 2016-07-07 **************/
				//凭证对象
				if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().compareTo(order.getCategoryId()) == 0) {
					log.info("ComplexQueryServiceImpl.fillOrderRalatedInfo category_hotel, categoryId=" 
							+ order.getCategoryId() + ",orderId=" + order.getOrderId());
					Map<String, Object> ebkCertifParams = new HashMap<String, Object>();
					ebkCertifParams.put("orderId", order.getOrderId());
					//查询该订单下的凭证列表，倒叙排列
					try {
						ResultHandleT<List<EbkCertif>> handleT = ebkCertifClientService.findEbkCertifListByMap(ebkCertifParams);
						if (handleT.isSuccess()) {
							if(CollectionUtils.isNotEmpty(handleT.getReturnContent())){
								//得到最新一个凭证对象
								EbkCertif certif = handleT.getReturnContent().get(0);
								if (certif != null) {
									order.setEbkCertif(certif);
									log.info("ComplexQueryServiceImpl.fillOrderRalatedInfo, setEbkCertif success, orderId=" + order.getOrderId());
								}
							}else{
								log.info("ComplexQueryServiceImpl.fillOrderRalatedInfo, List<EbkCertif> is null, orderId=" + order.getOrderId());
							}
						}else{
							log.info("ComplexQueryServiceImpl.fillOrderRalatedInfo, get handleT faild, orderId=" 
									+ order.getOrderId() + ", messge = " + handleT.getMsg());
						}
					} catch (Exception e) {
						log.info("ComplexQueryServiceImpl.fillOrderRalatedInfo, findEbkCertifListByMap error, orderId=" + order.getOrderId());
						e.printStackTrace();
					}
				}
				/**************** add by DongNingBo 2016-07-07 **************/
			}
			if (!orderPersonMap.isEmpty()
					&& orderPersonMap.containsKey(order.getOrderId())) {
				log.info("ComplexQueryServiceImpl.orderPerson.set.orderId:"+order.getOrderId());
				List<OrdPerson> ordPeoples = orderPersonMap.get(order.getOrderId());
				if(ordPeoples!=null && ordPeoples.size()>0){
					log.info("ComplexQueryServiceImpl.orderPerson map size:"+ordPeoples.size());
					for (OrdPerson ordPerson:ordPeoples){
						log.info("ComplexQueryServiceImpl.orderPerson map foreach name:"+ordPerson.getFullName());
					}
				}
				if(ordPeoples==null )
					log.info("ComplexQueryServiceImpl.orderPerson map is null");

				order.setOrdPersonList(orderPersonMap.get(order.getOrderId()));
			}

			if (!ordItemPersonRelationMap.isEmpty()) {
				for (OrdOrderItem orderItem : order.getOrderItemList()) {
					if (ordItemPersonRelationMap.containsKey(orderItem
							.getOrderItemId())) {
						List<OrdItemPersonRelation> list = ordItemPersonRelationMap
								.get(orderItem.getOrderItemId());
						fillOrdItemPersonRelation(list,
								order.getOrdPersonList());
						orderItem.setOrdItemPersonRelationList(list);
					}
				}
			}
			if (!ordAddressMap.isEmpty()) {

				List<OrdPerson> personList = order.getOrdPersonList();
				if (CollectionUtils.isNotEmpty(personList)) {
					for (OrdPerson ordPerson : personList) {
						if (ordAddressMap.containsKey(ordPerson
								.getOrdPersonId())) {
							ordPerson.setAddressList(ordAddressMap
									.get(ordPerson.getOrdPersonId()));
						}
					}
				}

			}

			if (!ordAdditionStatusMap.isEmpty()
					&& ordAdditionStatusMap.containsKey(order.getOrderId())) {
				order.setOrdAdditionStatusList(ordAdditionStatusMap.get(order
						.getOrderId()));
			}
			if (!ordTravelContractMap.isEmpty()
					&& ordTravelContractMap.containsKey(order.getOrderId())) {
				order.setOrdTravelContractList(ordTravelContractMap.get(order
						.getOrderId()));
			}

			if (!orderPackMap.isEmpty()
					&& orderPackMap.containsKey(order.getOrderId())) {
				order.setOrderPackList(orderPackMap.get(order.getOrderId()));
				if (CollectionUtils.isNotEmpty(order.getOrderPackList())
						&& CollectionUtils.isNotEmpty(order.getOrderItemList())) {
					Map<Long, List<OrdOrderItem>> orderItemMapTemp = new HashMap<Long, List<OrdOrderItem>>();
					for (OrdOrderItem orderItem : order.getOrderItemList()) {
						List<OrdOrderItem> itemList = null;

						if (orderItem.getOrderPackId() != null) {
							if (orderItemMapTemp.containsKey(orderItem
									.getOrderPackId())) {
								itemList = orderItemMapTemp.get(orderItem
										.getOrderPackId());
							} else {
								itemList = new ArrayList<OrdOrderItem>();
								orderItemMapTemp.put(
										orderItem.getOrderPackId(), itemList);
							}
							itemList.add(orderItem);
						}
					}
					for (OrdOrderPack orderPack : order.getOrderPackList()) {
						orderPack.setOrderItemList(orderItemMapTemp
								.get(orderPack.getOrderPackId()));
					}
				}
			}
			fillNopackItem(order);

			if (!orderAmountItemMap.isEmpty()
					&& orderAmountItemMap.containsKey(order.getOrderId())) {
				order.setOrderAmountItemList(orderAmountItemMap.get(order
						.getOrderId()));
			}
			if (!orderGuaranteeCreditCardMap.isEmpty()
					&& orderGuaranteeCreditCardMap.containsKey(order
							.getOrderId())) {
				order.setOrdGuaranteeCreditCardList(orderGuaranteeCreditCardMap
						.get(order.getOrderId()));
			}
			if (!ordCourierListingMap.isEmpty()
					&& ordCourierListingMap.containsKey(order.getOrderId())) {
				order.setOrdCourierListingList(ordCourierListingMap.get(order
						.getOrderId()));
			}

			//装配交通接驳
			if(!orderConnectsServicePropMap.isEmpty()
					&& orderConnectsServicePropMap.containsKey(order.getOrderId())){
					order.setOrderConnectsServicePropList(orderConnectsServicePropMap.get(order.getOrderId()));
			}

		}

		// 返回填充过的订单集合
		return orderList;
	}

	@Override
	public Long queryOrderCountByCondition(
			final ComplexQuerySQLCondition condition) {
		//当查询条件中没有订单号，也没有子订单号时走读写分离逻辑
		if (condition.getOrderIndentityParam() == null
				|| (condition.getOrderIndentityParam().getOrderId() == null && condition
						.getOrderIndentityParam().getOrderItemId() == null)) {
			return this.checkOrderCountFromReadDB(condition);
		}
		// 得到控制器
		final IComplexQuerySQLDirector complexQuerySQLDirector = new ComplexQuerySQLDirectorImpl();
		// 得到构建器
		final IComplexQuerySQLBuilder orderCountSQLBuilder = new OrderCountSQLBuilderImpl();
		// 设置条件
		complexQuerySQLDirector.setComplexQueryCondition(condition);
		// 组装SQL
		complexQuerySQLDirector.order(orderCountSQLBuilder, condition
				.getOrderFlagParam().isOrderPageFlag());
		// 获取完整SQL
		final String completeSQL = orderCountSQLBuilder
				.buildCompleteSQLStatement();
		// 统计订单记录数
		Long totalCount = complexQueryDAO.queryCount(OrdOrder.class,
				completeSQL);
		// 返回总记录数
		return totalCount;
	}

	@Override
	public OrdOrder queryOrderByOrderId(final Long orderId) {
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();

		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
		orderIndentityParam.setOrderId(orderId);

		OrderFlagParam orderFlagParam = new OrderFlagParam();
		orderFlagParam.setOrderItemTableFlag(true);
		orderFlagParam.setOrderPersonTableFlag(true);
		orderFlagParam.setOrderHotelTimeRateTableFlag(true);
		orderFlagParam.setOrderWifiTimeRateTableFlag(true);
		orderFlagParam.setOrdOrderPickingPointTableFlag(true);
		orderFlagParam.setOrderStockTableFlag(true);
		orderFlagParam.setOrderAmountItemTableFlag(true);
		orderFlagParam.setOrderPackTableFlag(true);
		orderFlagParam.setOrderItemPersonRelationFlag(true);
		orderFlagParam.setOrdTravelContractTableFlag(true);
		orderFlagParam.setOrderGuaranteeCreditCardTableFlag(true);
		orderFlagParam.setOrderAddressTableFlag(true);
		orderFlagParam.setOrderConnectsServicePropFlag(true);//设置当地玩乐服务信息
		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderFlagParam(orderFlagParam);

		condition.getOrderRelationSortParam().setOrderHotelTimeRateSort(
				"  ORD_ORDER_HOTEL_TIME_RATE.VISIT_TIME  ASC  ");
		condition.getOrderRelationSortParam().setOrderWifiTimeRateSort(
				"  ORD_ORDER_WIFI_TIME_RATE.VISIT_TIME  ASC  ");

		OrdOrder order = ordOrderDao.selectByPrimaryKey(orderId);
		if (order != null) {
			List<OrdOrder> orderList = new ArrayList<OrdOrder>(1);
			orderList.add(order);

			String sql = getConditionSqlContent(condition);
			log.info("sql is " + sql);
			orderList = this.fillOrderRalatedInfo(orderList, sql, condition);

			// 填充单个订单的数据
			order = orderList.get(0);
			this.fillAddition(order, sql, condition);
			//填充子单附加项信息
			this.fillOrderItemExtendInfo(order);
			return order;
		}
		return null;
	}

	/**
	 * 附加针对单个订单处理的数据
	 * @param order
	 */
	private void fillOrderItemExtendInfo(OrdOrder order) {
		List<OrdOrderItem> ordOrderItems = order.getOrderItemList();
		if (CollectionUtils.isNotEmpty(ordOrderItems)) {
			for (OrdOrderItem ordOrderItem : ordOrderItems) {
				OrdOrderItemExtend ordOrderItemExtend = ordOrderItemExtendDao.selectByPrimaryKey(ordOrderItem.getOrderItemId());
				ordOrderItem.setOrdOrderItemExtend(ordOrderItemExtend);
			}
		}
	}
	

	/**
	 * 附加针对单个订单处理的数据
	 * @param order
	 * @param sql
	 * @param condition
	 */
	private void fillAdditionNew(OrdOrder order, final String sql,
			ComplexQuerySQLCondition condition) {
		List<OrdFormInfo> formInfoList = complexQueryDAO.queryOrderRelatedInfo(
				OrdFormInfo.class, sql, condition);
		order.setFormInfoList(formInfoList);
	}

	/**
	 * 附加针对单个订单处理的数据
	 * 
	 * @param order
	 * @param sql
	 * @param condition
	 */
	private void fillAddition(OrdOrder order, final String sql,
			ComplexQuerySQLCondition condition) {
		List<Long> orderIds = new ArrayList<Long>();
		orderIds.add(order.getOrderId());
		List<OrdFormInfo> formInfoList = ordComplexSqlDao
				.selectDistinctFormInfoByOrderIds(orderIds);
		order.setFormInfoList(formInfoList);
	}

	private void fillNopackItem(OrdOrder order) {
		List<OrdOrderItem> nopackOrderItemList = new ArrayList<OrdOrderItem>();
		if (CollectionUtils.isNotEmpty(order.getOrderItemList())) {
			for (OrdOrderItem orderItem : order.getOrderItemList()) {
				if (orderItem.getOrderPackId() == null) {
					nopackOrderItemList.add(orderItem);
				}
			}
		}
		order.setNopackOrderItemList(nopackOrderItemList);
	}

	private String getConditionSqlContent(
			final ComplexQuerySQLCondition condition) {
		// 得到控制器
		final IComplexQuerySQLDirector complexQuerySQLDirector = new ComplexQuerySQLDirectorImpl();
		// 得到构建器
		final IComplexQuerySQLBuilder orderQuerySQLBuilder = new OrderQuerySQLBuilderImpl();
		// 设置条件
		complexQuerySQLDirector.setComplexQueryCondition(condition);
		// 组装SQL
		complexQuerySQLDirector.order(orderQuerySQLBuilder, condition
				.getOrderFlagParam().isOrderPageFlag());
		// 获取完整SQL
		final String completeSQL = orderQuerySQLBuilder
				.buildCompleteSQLStatement();
		return completeSQL;
	}

	/**
	 * 填充订单及订单相关表的数据
	 * 
	 * @param orderList
	 * @param queryOrderSql
	 * @param condition
	 * @return
	 */
	/**
	 * 填充订单及订单相关表的数据
	 * 
	 * @param orderList
	 * @param queryOrderSql
	 * @param condition
	 * @return
	 */
	private List<OrdOrder> fillOrderRalatedInfoBak(final List<OrdOrder> orderList,
			final String queryOrderSql, final ComplexQuerySQLCondition condition) {
		// 初始化订单相关表list
		List<OrdOrderItem> orderItemList = null;// 订单子项
		List<OrdPerson> orderPersonList = null;// 订单人
		List<OrdAddress> orderAddressList = null;// 订单收货地址
		List<OrdOrderPack> orderPackList = null;// 订单打包
		List<OrdOrderAmountItem> orderAmountItemList = null;// 订单金额
		List<OrdGuaranteeCreditCard> orderGuaranteeCreditCardList = null;// 订单担保
		List<OrdOrderHotelTimeRate> orderHotelTimeRateList = null;// 订单酒店
		List<OrdOrderStock> orderStockList = null;// 订单库存
		List<OrdOrderStock> orderStockListForHotelTimeRate = new ArrayList<OrdOrderStock>();// 订单酒店的库存
		List<OrdAdditionStatus> ordAdditionStatusList = null;// 状态
		List<OrdTravelContract> ordTravelContractList = null;// 订单合同
		List<OrdItemPersonRelation> ordItemPersonRelationList = null;
		List<OrdCourierListing> ordCourierListingList = null;// 快递寄件清单
		// 初始化订单相关表map
		Map<Long, List<OrdOrderItem>> orderItemMap = new HashMap<Long, List<OrdOrderItem>>();
		Map<Long, List<OrdPerson>> orderPersonMap = new HashMap<Long, List<OrdPerson>>();
		Map<Long, List<OrdAddress>> ordAddressMap = new HashMap<Long, List<OrdAddress>>();
		Map<Long, List<OrdOrderPack>> orderPackMap = new HashMap<Long, List<OrdOrderPack>>();
		Map<Long, List<OrdOrderAmountItem>> orderAmountItemMap = new HashMap<Long, List<OrdOrderAmountItem>>();
		Map<Long, List<OrdGuaranteeCreditCard>> orderGuaranteeCreditCardMap = new HashMap<Long, List<OrdGuaranteeCreditCard>>();
		Map<Long, List<OrdOrderHotelTimeRate>> orderHotelTimeRateMap = new HashMap<Long, List<OrdOrderHotelTimeRate>>();
		Map<Long, List<OrdOrderStock>> orderStockMap = new HashMap<Long, List<OrdOrderStock>>();
		Map<Long, List<OrdOrderStock>> orderStockMapForHotelRate = new HashMap<Long, List<OrdOrderStock>>();
		Map<Long, List<OrdAdditionStatus>> ordAdditionStatusMap = new HashMap<Long, List<OrdAdditionStatus>>();
		Map<Long, List<OrdTravelContract>> ordTravelContractMap = new HashMap<Long, List<OrdTravelContract>>();
		Map<Long, List<OrdItemPersonRelation>> ordItemPersonRelationMap = new HashMap<Long, List<OrdItemPersonRelation>>();
		Map<Long, List<OrdCourierListing>> ordCourierListingMap = new HashMap<Long, List<OrdCourierListing>>();
		// 根据订单ID关联查询订单子项集合
		if (condition.getOrderFlagParam().isOrderItemTableFlag()) {
			orderItemList = complexQueryDAO.queryOrderRelatedInfo(
					OrdOrderItem.class, queryOrderSql, condition);
			if (!orderItemList.isEmpty()) {
				orderItemMap = this.getOrderItemMap(orderItemList);// 将list转为map方便处理
			}

			/******************************** 填充与订单子项直接关联的数据 **********************************************/
			// 填充订单子项的库存数据
			if (!orderItemList.isEmpty()
					&& condition.getOrderFlagParam().isOrderStockTableFlag()) {
				orderStockList = complexQueryDAO.queryOrderRelatedInfo(
						OrdOrderStock.class, queryOrderSql, condition);
				if (!orderStockList.isEmpty()) {
					orderStockMap = this.getOrderStockMap(orderStockList);
					for (OrdOrderItem orderItem : orderItemList) {
						if (!orderStockMap.isEmpty()
								&& orderStockMap.containsKey(orderItem
										.getOrderItemId())) {
							orderItem.setOrderStockList(orderStockMap
									.get(orderItem.getOrderItemId()));// 这里是全部的库存数据
						}
					}
					// 切分酒店库存数据
					orderStockListForHotelTimeRate = this
							.getOrderStockByObjectType(
									OrderEnum.ORDER_STOCK_OBJECT_TYPE.HOTEL_TIME_RATE
											.toString(), orderStockList);
				}
			}

			// 填充订单子项的酒店使用情况数据
			if (!orderItemList.isEmpty()) {
				if (condition.getOrderFlagParam()
						.isOrderHotelTimeRateTableFlag()) {
					orderHotelTimeRateList = complexQueryDAO
							.queryOrderRelatedInfo(OrdOrderHotelTimeRate.class,
									queryOrderSql, condition);
					if (!orderHotelTimeRateList.isEmpty()) {
						orderHotelTimeRateMap = this
								.getOrderHotelTimeRateMap(orderHotelTimeRateList);
						for (OrdOrderItem orderItem : orderItemList) {
							if (!orderHotelTimeRateMap.isEmpty()
									&& orderHotelTimeRateMap
											.containsKey(orderItem
													.getOrderItemId())) {
								orderItem
										.setOrderHotelTimeRateList(orderHotelTimeRateMap
												.get(orderItem.getOrderItemId()));
							}
						}

						// 填充酒店库存数据
						if (!orderStockListForHotelTimeRate.isEmpty()) {
							orderStockMapForHotelRate = this
									.getOrderStockForHotelTimeRateMap(orderStockListForHotelTimeRate);
							for (OrdOrderHotelTimeRate orderHotelTimeRate : orderHotelTimeRateList) {
								if (!orderStockMapForHotelRate.isEmpty()
										&& orderStockMapForHotelRate
												.containsKey(orderHotelTimeRate
														.getHotelTimeRateId())) {
									orderHotelTimeRate
											.setOrderStockList(orderStockMapForHotelRate.get(orderHotelTimeRate
													.getHotelTimeRateId()));
								}
							}
						}
					}
				}

				if (condition.getOrderFlagParam()
						.isOrderItemPersonRelationFlag()) {
					ordItemPersonRelationList = complexQueryDAO
							.queryOrderRelatedInfo(OrdItemPersonRelation.class,
									queryOrderSql, condition);
					if (!ordItemPersonRelationList.isEmpty()) {
						ordItemPersonRelationMap = getOrdItemPersonMap(ordItemPersonRelationList);
					}
				}
			}
		}

		// 根据订单ID关联查询订单人集合
		if (condition.getOrderFlagParam().isOrderPersonTableFlag()) {
			orderPersonList = complexQueryDAO.queryOrderRelatedInfo(
					OrdPerson.class, queryOrderSql, condition);
			if (!orderPersonList.isEmpty()) {
				orderPersonMap = this.getOrderPersonMap(orderPersonList);
			}
		}

		// 根据订单ID关联查询订单人对应地址
		if (condition.getOrderFlagParam().isOrderPersonTableFlag()
				&& condition.getOrderFlagParam().isOrderAddressTableFlag()) {
			orderAddressList = complexQueryDAO.queryOrderRelatedInfo(
					OrdAddress.class, queryOrderSql, condition);
			if (!orderAddressList.isEmpty()) {
				ordAddressMap = this.getOrderAddressMap(orderAddressList);
			}
		}

		// 根据订单ID关联状态集合
		if (condition.getOrderFlagParam().isOrdAdditionStatusTableFlag()) {
			ordAdditionStatusList = complexQueryDAO.queryOrderRelatedInfo(
					OrdAdditionStatus.class, queryOrderSql, condition);
			if (!ordAdditionStatusList.isEmpty()) {
				ordAdditionStatusMap = this
						.getOrdAdditionStatusMap(ordAdditionStatusList);
			}
		}

		// 根据订单ID关联订单合同集合
		if (condition.getOrderFlagParam().isOrdTravelContractTableFlag()) {
			ordTravelContractList = complexQueryDAO.queryOrderRelatedInfo(
					OrdTravelContract.class, queryOrderSql, condition);
			if (!ordTravelContractList.isEmpty()) {
				ordTravelContractMap = this
						.getOrdTravelContractMap(ordTravelContractList);
			}
		}

		// 根据订单ID关联查询订单打包集合
		if (condition.getOrderFlagParam().isOrderPackTableFlag()) {
			orderPackList = complexQueryDAO.queryOrderRelatedInfo(
					OrdOrderPack.class, queryOrderSql, condition);
			if (!orderPackList.isEmpty()) {
				orderPackMap = this.getOrderPackMap(orderPackList);
			}
		}

		// 根据订单ID关联查询订单金额变换集合
		if (condition.getOrderFlagParam().isOrderAmountItemTableFlag()) {
			orderAmountItemList = complexQueryDAO.queryOrderRelatedInfo(
					OrdOrderAmountItem.class, queryOrderSql, condition);
			if (!orderAmountItemList.isEmpty()) {
				orderAmountItemMap = this
						.getOrderAmountItemMap(orderAmountItemList);
			}
		}

		// 根据订单ID关联查询订单担保信息集合
		if (condition.getOrderFlagParam().isOrderGuaranteeCreditCardTableFlag()) {
			orderGuaranteeCreditCardList = complexQueryDAO
					.queryOrderRelatedInfo(OrdGuaranteeCreditCard.class,
							queryOrderSql, condition);
			if (!orderGuaranteeCreditCardList.isEmpty()) {
				orderGuaranteeCreditCardMap = this
						.getGuaranteeCreditCardMap(orderGuaranteeCreditCardList);
			}
		}

		// 根据订单ID关联查询快递寄件清单集合
		if (condition.getOrderFlagParam().isOrdCourierListing()) {
			ordCourierListingList = complexQueryDAO.queryOrderRelatedInfo(
					OrdCourierListing.class, queryOrderSql, condition);
			if (!ordCourierListingList.isEmpty()) {
				ordCourierListingMap = this
						.getOrdCourierListingMap(ordCourierListingList);
			}
		}

		/************************************* 填充与订单直接关联的数据 ********************************************/
		for (OrdOrder order : orderList) {
			if (!orderItemMap.isEmpty()
					&& orderItemMap.containsKey(order.getOrderId())) {
				order.setOrderItemList(orderItemMap.get(order.getOrderId()));
			}
			if (!orderPersonMap.isEmpty()
					&& orderPersonMap.containsKey(order.getOrderId())) {
				order.setOrdPersonList(orderPersonMap.get(order.getOrderId()));
			}

			if (!ordItemPersonRelationMap.isEmpty()) {
				for (OrdOrderItem orderItem : order.getOrderItemList()) {
					if (ordItemPersonRelationMap.containsKey(orderItem
							.getOrderItemId())) {
						List<OrdItemPersonRelation> list = ordItemPersonRelationMap
								.get(orderItem.getOrderItemId());
						fillOrdItemPersonRelation(list,
								order.getOrdPersonList());
						orderItem.setOrdItemPersonRelationList(list);
					}
				}
			}
			if (!ordAddressMap.isEmpty()) {

				List<OrdPerson> personList = order.getOrdPersonList();
				if (CollectionUtils.isNotEmpty(personList)) {
					for (OrdPerson ordPerson : personList) {
						if (ordAddressMap.containsKey(ordPerson
								.getOrdPersonId())) {
							ordPerson.setAddressList(ordAddressMap
									.get(ordPerson.getOrdPersonId()));
						}
					}
				}

			}

			if (!ordAdditionStatusMap.isEmpty()
					&& ordAdditionStatusMap.containsKey(order.getOrderId())) {
				order.setOrdAdditionStatusList(ordAdditionStatusMap.get(order
						.getOrderId()));
			}
			if (!ordTravelContractMap.isEmpty()
					&& ordTravelContractMap.containsKey(order.getOrderId())) {
				order.setOrdTravelContractList(ordTravelContractMap.get(order
						.getOrderId()));
			}

			if (!orderPackMap.isEmpty()
					&& orderPackMap.containsKey(order.getOrderId())) {
				order.setOrderPackList(orderPackMap.get(order.getOrderId()));
				if (CollectionUtils.isNotEmpty(order.getOrderPackList())
						&& CollectionUtils.isNotEmpty(order.getOrderItemList())) {
					Map<Long, List<OrdOrderItem>> orderItemMapTemp = new HashMap<Long, List<OrdOrderItem>>();
					for (OrdOrderItem orderItem : order.getOrderItemList()) {
						List<OrdOrderItem> itemList = null;

						if (orderItem.getOrderPackId() != null) {
							if (orderItemMapTemp.containsKey(orderItem
									.getOrderPackId())) {
								itemList = orderItemMapTemp.get(orderItem
										.getOrderPackId());
							} else {
								itemList = new ArrayList<OrdOrderItem>();
								orderItemMapTemp.put(
										orderItem.getOrderPackId(), itemList);
							}
							itemList.add(orderItem);
						}
					}
					for (OrdOrderPack orderPack : order.getOrderPackList()) {
						orderPack.setOrderItemList(orderItemMapTemp
								.get(orderPack.getOrderPackId()));
					}
				}
			}
			fillNopackItem(order);

			if (!orderAmountItemMap.isEmpty()
					&& orderAmountItemMap.containsKey(order.getOrderId())) {
				order.setOrderAmountItemList(orderAmountItemMap.get(order
						.getOrderId()));
			}
			if (!orderGuaranteeCreditCardMap.isEmpty()
					&& orderGuaranteeCreditCardMap.containsKey(order
							.getOrderId())) {
				order.setOrdGuaranteeCreditCardList(orderGuaranteeCreditCardMap
						.get(order.getOrderId()));
			}
			if (!ordCourierListingMap.isEmpty()
					&& ordCourierListingMap.containsKey(order.getOrderId())) {
				order.setOrdCourierListingList(ordCourierListingMap.get(order
						.getOrderId()));
			}

		}

		// 返回填充过的订单集合
		return orderList;
	}

	private void fillOrdItemPersonRelation(List<OrdItemPersonRelation> list,
			List<OrdPerson> personList) {
		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		Map<Long, OrdPerson> personMap = new HashMap<Long, OrdPerson>();
		for (OrdPerson p : personList) {
			personMap.put(p.getOrdPersonId(), p);
		}
		for (OrdItemPersonRelation r : list) {
			r.setOrdPerson(personMap.get(r.getOrdPersonId()));
		}
	}

	/**
	 * 将订单子项list集合转化为map集合，方便处理
	 * 
	 * @param orderItemList
	 * @return
	 */
	private Map<Long, List<OrdOrderItem>> getOrderItemMap(
			final List<OrdOrderItem> orderItemList) {
		final Map<Long, List<OrdOrderItem>> map = new HashMap<Long, List<OrdOrderItem>>();
		for (OrdOrderItem item : orderItemList) {
			final List<OrdOrderItem> list;
			if (map.containsKey(item.getOrderId())) {
				list = map.get(item.getOrderId());
			} else {
				list = new ArrayList<OrdOrderItem>();
			}
			list.add(item);
			map.put(item.getOrderId(), list);
		}
		return map;
	}

	private Map<Long, List<OrdItemPersonRelation>> getOrdItemPersonMap(
			final List<OrdItemPersonRelation> itemPersonRelations) {
		final Map<Long, List<OrdItemPersonRelation>> map = new HashMap<Long, List<OrdItemPersonRelation>>();
		for (OrdItemPersonRelation r : itemPersonRelations) {
			List<OrdItemPersonRelation> list = null;
			if (map.containsKey(r.getOrderItemId())) {
				list = map.get(r.getOrderItemId());
			} else {
				list = new ArrayList<OrdItemPersonRelation>();
				map.put(r.getOrderItemId(), list);
			}
			list.add(r);
		}
		return map;
	}

	/**
	 * 将订单游客list转化为map方便处理
	 * 
	 * @param orderPersonList
	 * @return
	 */
	private Map<Long, List<OrdPerson>> getOrderPersonMap(
			final List<OrdPerson> orderPersonList) {
		final Map<Long, List<OrdPerson>> map = new HashMap<Long, List<OrdPerson>>();
		for (OrdPerson item : orderPersonList) {
			final List<OrdPerson> list;
			if (map.containsKey(item.getObjectId())) {
				list = map.get(item.getObjectId());
			} else {
				list = new ArrayList<OrdPerson>();
			}
			list.add(item);
			map.put(item.getObjectId(), list);
		}
		return map;
	}

	/**
	 * 将订单游客list转化为map方便处理
	 * 
	 * @param orderAddressList
	 * @return
	 */
	private Map<Long, List<OrdAddress>> getOrderAddressMap(
			final List<OrdAddress> orderAddressList) {
		final Map<Long, List<OrdAddress>> map = new HashMap<Long, List<OrdAddress>>();
		for (OrdAddress item : orderAddressList) {
			final List<OrdAddress> list;
			if (map.containsKey(item.getOrdPersonId())) {
				list = map.get(item.getOrdPersonId());
			} else {
				list = new ArrayList<OrdAddress>();
			}
			list.add(item);
			map.put(item.getOrdPersonId(), list);
		}
		return map;
	}

	/**
	 * 将订单状态list转化为map方便处理
	 * 
	 * @param OrdAdditionStatusList
	 * @return
	 */
	private Map<Long, List<OrdAdditionStatus>> getOrdAdditionStatusMap(
			final List<OrdAdditionStatus> OrdAdditionStatusList) {
		final Map<Long, List<OrdAdditionStatus>> map = new HashMap<Long, List<OrdAdditionStatus>>();
		for (OrdAdditionStatus item : OrdAdditionStatusList) {
			final List<OrdAdditionStatus> list;
			if (map.containsKey(item.getOrderId())) {
				list = map.get(item.getOrderId());
			} else {
				list = new ArrayList<OrdAdditionStatus>();
			}
			list.add(item);
			map.put(item.getOrderId(), list);
		}
		return map;
	}

	/**
	 * 将订单合同list转化为map方便处理
	 * 
	 * @param OrdTravelContractList
	 * @return
	 */
	private Map<Long, List<OrdTravelContract>> getOrdTravelContractMap(
			final List<OrdTravelContract> OrdTravelContractList) {
		final Map<Long, List<OrdTravelContract>> map = new HashMap<Long, List<OrdTravelContract>>();
		for (OrdTravelContract item : OrdTravelContractList) {
			final List<OrdTravelContract> list;
			if (map.containsKey(item.getOrderId())) {
				list = map.get(item.getOrderId());
			} else {
				list = new ArrayList<OrdTravelContract>();
			}
			list.add(item);
			map.put(item.getOrderId(), list);
		}
		return map;
	}

	/**
	 * 将订单打包信息转化为map
	 * 
	 * @param orderPackList
	 * @return
	 */
	private Map<Long, List<OrdOrderPack>> getOrderPackMap(
			final List<OrdOrderPack> orderPackList) {
		final Map<Long, List<OrdOrderPack>> map = new HashMap<Long, List<OrdOrderPack>>();
		for (OrdOrderPack item : orderPackList) {
			final List<OrdOrderPack> list;
			if (map.containsKey(item.getOrderId())) {
				list = map.get(item.getOrderId());
			} else {
				list = new ArrayList<OrdOrderPack>();
			}
			list.add(item);
			map.put(item.getOrderId(), list);
		}
		return map;
	}

	/**
	 * 将订单金额变换集合转化为map
	 * 
	 * @param orderAmountItemList
	 * @return
	 */
	private Map<Long, List<OrdOrderAmountItem>> getOrderAmountItemMap(
			final List<OrdOrderAmountItem> orderAmountItemList) {
		final Map<Long, List<OrdOrderAmountItem>> map = new HashMap<Long, List<OrdOrderAmountItem>>();
		for (OrdOrderAmountItem item : orderAmountItemList) {
			final List<OrdOrderAmountItem> list;
			if (map.containsKey(item.getOrderId())) {
				list = map.get(item.getOrderId());
			} else {
				list = new ArrayList<OrdOrderAmountItem>();
			}
			list.add(item);
			map.put(item.getOrderId(), list);
		}
		return map;
	}

	/**
	 * 将订单担保信息集合转化为map
	 * 
	 * @param orderGuaranteeCreditCardList
	 * @return
	 */
	private Map<Long, List<OrdGuaranteeCreditCard>> getGuaranteeCreditCardMap(
			final List<OrdGuaranteeCreditCard> orderGuaranteeCreditCardList) {
		final Map<Long, List<OrdGuaranteeCreditCard>> map = new HashMap<Long, List<OrdGuaranteeCreditCard>>();
		for (OrdGuaranteeCreditCard item : orderGuaranteeCreditCardList) {
			final List<OrdGuaranteeCreditCard> list;
			if (map.containsKey(item.getOrderId())) {
				list = map.get(item.getOrderId());
			} else {
				list = new ArrayList<OrdGuaranteeCreditCard>();
			}
			list.add(item);
			map.put(item.getOrderId(), list);
		}
		return map;
	}

	/**
	 * 将快递寄件清单集合转化为map
	 * 
	 * @param ordCourierListingList
	 * @return
	 */
	private Map<Long, List<OrdCourierListing>> getOrdCourierListingMap(
			final List<OrdCourierListing> ordCourierListingList) {
		final Map<Long, List<OrdCourierListing>> map = new HashMap<Long, List<OrdCourierListing>>();
		for (OrdCourierListing item : ordCourierListingList) {
			final List<OrdCourierListing> list;
			if (map.containsKey(item.getOrderId())) {
				list = map.get(item.getOrderId());
			} else {
				list = new ArrayList<OrdCourierListing>();
			}
			list.add(item);
			map.put(item.getOrderId(), list);
		}
		return map;
	}

	/**
	 * 将订单酒店集合转化为map
	 * 
	 * @param orderHotelTimeRateList
	 * @return
	 */
	private Map<Long, List<OrdOrderHotelTimeRate>> getOrderHotelTimeRateMap(
			final List<OrdOrderHotelTimeRate> orderHotelTimeRateList) {
		final Map<Long, List<OrdOrderHotelTimeRate>> map = new HashMap<Long, List<OrdOrderHotelTimeRate>>();
		for (OrdOrderHotelTimeRate item : orderHotelTimeRateList) {
			final List<OrdOrderHotelTimeRate> list;
			if (map.containsKey(item.getOrderItemId())) {
				list = map.get(item.getOrderItemId());
			} else {
				list = new ArrayList<OrdOrderHotelTimeRate>();
			}
			list.add(item);
			map.put(item.getOrderItemId(), list);
		}
		return map;
	}
	
	
	/**
	 * 将订单wifi网点集合转化为map
	 * 
	 * @param orderPickingPointList
	 * @return
	 */
	private Map<Long, List<OrdOrderWifiPickingPoint>> getOrdOrderPickingPointMap(
			final List<OrdOrderWifiPickingPoint> orderPickingPointList) {
		final Map<Long, List<OrdOrderWifiPickingPoint>> map = new HashMap<Long, List<OrdOrderWifiPickingPoint>>();
		for (OrdOrderWifiPickingPoint item : orderPickingPointList) {
			final List<OrdOrderWifiPickingPoint> list;
			if (map.containsKey(item.getOrderItemId())) {
				list = map.get(item.getOrderItemId());
			} else {
				list = new ArrayList<OrdOrderWifiPickingPoint>();
			}
			list.add(item);
			map.put(item.getOrderItemId(), list);
		}
		return map;
	}
	

	/**
	 * 将订单wifi集合转化为map
	 * 
	 * @param orderWifiTimeRateList
	 * @return
	 */
	private Map<Long, List<OrdOrderWifiTimeRate>> getOrderWifiTimeRateMap(
			final List<OrdOrderWifiTimeRate> orderWifiTimeRateList) {
		final Map<Long, List<OrdOrderWifiTimeRate>> map = new HashMap<Long, List<OrdOrderWifiTimeRate>>();
		for (OrdOrderWifiTimeRate item : orderWifiTimeRateList) {
			final List<OrdOrderWifiTimeRate> list;
			if (map.containsKey(item.getOrderItemId())) {
				list = map.get(item.getOrderItemId());
			} else {
				list = new ArrayList<OrdOrderWifiTimeRate>();
			}
			list.add(item);
			map.put(item.getOrderItemId(), list);
		}
		return map;
	}

	/**
	 * 将订单库存集合转化为map
	 * 
	 * @param orderStockList
	 * @return
	 */
	private Map<Long, List<OrdOrderStock>> getOrderStockMap(
			final List<OrdOrderStock> orderStockList) {
		final Map<Long, List<OrdOrderStock>> map = new HashMap<Long, List<OrdOrderStock>>();
		for (OrdOrderStock item : orderStockList) {
			final List<OrdOrderStock> list;
			if (map.containsKey(item.getOrderItemId())) {
				list = map.get(item.getOrderItemId());
			} else {
				list = new ArrayList<OrdOrderStock>();
			}
			list.add(item);
			map.put(item.getOrderItemId(), list);
		}
		return map;
	}

	/**
	 * 根据对象类型从原有的库存集合中切分出相应的库存
	 * 
	 * @param objectType
	 * @param orderStockList
	 * @return
	 */
	private List<OrdOrderStock> getOrderStockByObjectType(String objectType,
			List<OrdOrderStock> orderStockList) {
		List<OrdOrderStock> orderStockListNew = new ArrayList<OrdOrderStock>();
		for (OrdOrderStock orderStock : orderStockList) {
			if (orderStock.getObjectType().equals(objectType)) {
				orderStockListNew.add(orderStock);
			}
		}
		return orderStockListNew;
	}

	/**
	 * 将酒店库存数据转化为map
	 * 
	 * @param orderStockListForHotelTimeRate
	 * @return
	 */
	private Map<Long, List<OrdOrderStock>> getOrderStockForHotelTimeRateMap(
			final List<OrdOrderStock> orderStockListForHotelTimeRate) {
		final Map<Long, List<OrdOrderStock>> map = new HashMap<Long, List<OrdOrderStock>>();
		for (OrdOrderStock item : orderStockListForHotelTimeRate) {
			final List<OrdOrderStock> list;
			if (map.containsKey(item.getObjectId())) {
				list = map.get(item.getObjectId());
			} else {
				list = new ArrayList<OrdOrderStock>();
			}
			list.add(item);
			map.put(item.getObjectId(), list);
		}
		return map;
	}

	/**
	 * 将交通接驳的服务信息，装换成Map
	 * @param orderConnectsServicePropList
	 * @return
     */
	private final Map<Long, List<OrderConnectsServiceProp>> getOrderConnectsServicePropMap(
			final List<OrderConnectsServiceProp> orderConnectsServicePropList){
		final Map<Long, List<OrderConnectsServiceProp>> map = new HashMap<Long, List<OrderConnectsServiceProp>>();
		if(CollectionUtils.isNotEmpty(orderConnectsServicePropList)){
			for(OrderConnectsServiceProp orderConnectsServiceProp : orderConnectsServicePropList){
				if(orderConnectsServiceProp != null && orderConnectsServiceProp.getOrderId() != null){
					Long orderId = orderConnectsServiceProp.getOrderId();
					if(map.containsKey(orderId)){
						map.get(orderId).add(orderConnectsServiceProp);
					}else{
						final List<OrderConnectsServiceProp> list = new ArrayList<OrderConnectsServiceProp>();
						list.add(orderConnectsServiceProp);
						map.put(orderId,list);
					}
				}
			}
		}
		return map;
	}

	/**
	 * 查询订单使用状态信息
	 * 
	 * @param orderId
	 *            订单ID
	 * @return
	 */
	@Override
	public List<OrdTicketPerform> findOrdTicketPerformList(Long orderId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", orderId);
		return ordTicketPerformDao.findOrdTicketPerformList(params);
	}

	/**
	 * 查询子订单使用状态信息
	 * 
	 * @param orderItemId
	 *            子订单ID
	 * @return
	 */
	@Override
	public List<OrdTicketPerform> selectByOrderItem(Long orderItemId) {
		List<OrdTicketPerform> list = new ArrayList<OrdTicketPerform>();
		OrdTicketPerform otp = ordTicketPerformDao.selectByOrderItem(orderItemId);
		if (otp != null) {
			list.add(otp);
		}
		return list;

	}

	/**
	 * 查询子订单使用状态信息
	 * 
	 * @param orderItemIds
	 *            子订单ID
	 * @return
	 */
	@Override
	@ReadOnlyDataSource
	public List<OrdTicketPerform> selectByOrderItems(List<Long> orderItemIds) {
		if (orderItemIds.size() <= QUERY_LIST_COUNT) {
			return ordTicketPerformDao.selectByOrderItems(orderItemIds);
		}
		List<OrdTicketPerform> list = new ArrayList<OrdTicketPerform>();
		int fromIndex = 0;
		int toIndex = 0;
		while (fromIndex < orderItemIds.size()) {
			if (fromIndex + QUERY_LIST_COUNT >= orderItemIds.size()) {
				toIndex = orderItemIds.size();
			} else {
				toIndex = fromIndex + QUERY_LIST_COUNT;
			}
			list.addAll(ordTicketPerformDao.selectByOrderItems(orderItemIds
					.subList(fromIndex, toIndex)));

			fromIndex = toIndex;
		}

		return list;
	}
	
	@Override
	public List<OrdTicketPerformDetail> selectPerformDetailByOrderItem(Long orderItemId) {
		List<OrdTicketPerformDetail> ordTicketPerformDetails = ordTicketPerformDetailDao.selectByOrderItem(orderItemId);
		return ordTicketPerformDetails;

	}

	@Override
	public List<Map<String, Object>> findAllObjectsBySql(
			Map<String, Object> params) {
		return complexQueryDAO.selectListBySql(params);
	}

	@Override
	@ReadOnlyDataSource
	public List<Map<String, Object>> findAllObjectsBySqlFromReadDB(
			Map<String, Object> params) {
		return complexQueryDAO.selectListBySql(params);
	}

	@Override
	public List<Long> findNeedGenWorkflowOrders() {
		return ordOrderDao.findNeedGenWorkflowOrders();
	}


	@Override
	public List<Long> findNeedCreateSupplierOrders() {
		return ordOrderDao.findNeedCreateSupplierOrders();
	}

	@Override
	public List<Long> findNeedCancelSupplierOrders() {
		return ordOrderDao.findNeedCancelSupplierOrders();
	}


	@Override
	@ReadOnlyDataSource
	public Long checkOrderCountFromReadDB(ComplexQuerySQLCondition condition) {
		// TODO Auto-generated method stub
		// 得到控制器
		final IComplexQuerySQLDirector complexQuerySQLDirector = new ComplexQuerySQLDirectorImpl();
		// 得到构建器
		final IComplexQuerySQLBuilder orderCountSQLBuilder = new OrderCountSQLBuilderImpl();
		// 设置条件
		complexQuerySQLDirector.setComplexQueryCondition(condition);
		// 组装SQL
		complexQuerySQLDirector.order(orderCountSQLBuilder, condition
				.getOrderFlagParam().isOrderPageFlag());
		// 获取完整SQL
		final String completeSQL = orderCountSQLBuilder
				.buildCompleteSQLStatement();
		// 统计订单记录数
		Long totalCount = complexQueryDAO.queryCount(OrdOrder.class,
				completeSQL);
		// 返回总记录数
		return totalCount;
	}


	@Override
	@ReadOnlyDataSource
	public List<OrdOrder> checkOrderListFromReadDB(
			ComplexQuerySQLCondition condition) {
		final String completeSQL = getConditionSqlContent(condition);
		// 查询订单集合
		List<OrdOrder> orderList = complexQueryDAO.queryList(OrdOrder.class,
				completeSQL);
		// 返回结果集
		if (!orderList.isEmpty()) {
			// 合并相关表数据
			return this.fillOrderRalatedInfo(orderList, completeSQL,
					condition);
		}
		return orderList;
	}

	@Override
	@ReadOnlyDataSource
	public List<OrdOrder> checkOnlyOrderListFromReadDB(
			ComplexQuerySQLCondition condition) {
		final String completeSQL = getConditionSqlContent(condition);
		log.info("get orderList only"+completeSQL);
		// 查询订单集合
		List<OrdOrder> orderList = complexQueryDAO.queryList(OrdOrder.class,
				completeSQL);
		return orderList;
	}


	@Override
	@ReadOnlyDataSource
	public List<OrdOrder> queryOrderListByCondition(
			OrderMonitorCnd orderMonitorCnd, ComplexQuerySQLCondition condition) {
//		List<Long> orderIds = this.orderQueryInfoService.findOrderIdsByCondition(orderMonitorCnd);
//		List<OrdOrder> orderList = null;
//		if(CollectionUtils.isNotEmpty(orderIds)) {
//			orderList = this.ordOrderDao.sortSelectByPrimaryKeyList(orderIds);
//			if(CollectionUtils.isNotEmpty(orderList)) {
//				return this.fillOrderRalatedInfo(orderList, null,
//						condition);
//			}
//		}
		
		return new ArrayList<OrdOrder>();
	}


	@Override
	@ReadOnlyDataSource
	public Long queryOrderCountByCondition(OrderMonitorCnd orderMonitorCnd) {
//		return orderQueryInfoService.findOrderCountByCondition(orderMonitorCnd);
		return 1L;
	}
	
	@Override
	@ReadOnlyDataSource
	public List<Long> findNeedTiggerPayProcOrders() {
		return ordOrderDao.findNeedTiggerPayProcOrders();
	}
	
	@Override
	@ReadOnlyDataSource
	public int updatePayProcTriggeredByOrderID(Map<String, Object> paramsMap){
		return ordOrderDao.updatePayProcTriggeredByOrderID(paramsMap);
	}

	@Override
	public List<Map<String, Object>> selectOrdOrderByOrderIds(Map<String, Object> params){
		return complexQueryDAO.selectOrdOrderByOrderIds(params);
	}
	
	@Override
	public String findMobileId(Long orderId){
		return ordOrderDao.findMobileId(orderId);
	}

}
