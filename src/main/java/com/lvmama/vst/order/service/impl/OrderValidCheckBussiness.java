/**
 * 订单创建的数据组装和业务逻辑验证
 */
package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.biz.service.CategoryPropClientService;
import com.lvmama.vst.back.client.prod.curise.service.ProdCuriseProductClientService;
import com.lvmama.vst.back.dist.po.TimePrice;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice.StockReduceVO;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice.TimePriceCheckVO;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrdAddress;
import com.lvmama.vst.back.order.po.OrdGuaranteeCreditCard;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdOrderAmountItem;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.curise.vo.CuriseProductParamsVO;
import com.lvmama.vst.back.prod.curise.vo.CuriseProductVO;
import com.lvmama.vst.back.prod.po.PropValue;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfo.HotelAdditation;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.BuyInfo.ItemPersonRelation;
import com.lvmama.vst.comm.vo.order.GuaranteeCreditCard;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.order.service.IOrdItemPersonRelationService;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.timeprice.service.impl.OrderTimePriceServiceFactory;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;

/**
 * @author lancey
 * 
 */
@Service
public class OrderValidCheckBussiness extends AbstractOrderService {

	private static final Log LOG = LogFactory.getLog(OrderValidCheckBussiness.class);
	
	@Autowired
	private OrderTimePriceServiceFactory orderTimePriceServiceFactory;
	
	@Autowired
	private CategoryClientService categoryClientService;
	
	@Autowired
	private CategoryPropClientService categoryPropClientService;
	
	@Autowired
	private ProdCuriseProductClientService prodCuriseProductClientService;
	
	@Autowired
	private IOrdItemPersonRelationService ordItemPersonRelationService;
	

	/**
	 * 构造订单数据列表
	 * 
	 * @param buyInfo
	 * @return
	 */
	public ResultHandleT<OrdOrderDTO> initOrderDTO(final BuyInfo buyInfo, final String operatorId) {
		ResultHandleT<OrdOrderDTO> handle = new ResultHandleT<OrdOrderDTO>();
		OrdOrderDTO order = new OrdOrderDTO(buyInfo);
		handle.setReturnContent(order);

		String errorMsg = null;

		//初始化后台下单用户ID
		order.setBackUserId(operatorId);
		
		// 预初始化订单
		errorMsg = preInitOrderDTO(order);
		
		// 初始化订单打包商品子项
		if (errorMsg == null) {
			errorMsg = initOrderPack(order);
		}
		
		// 初始化联系人、游玩人
		if (errorMsg == null) {
			errorMsg = InitOrderPerson(order);
		}

		// 初始化订单子项
		if (errorMsg == null) {
			errorMsg = initOrderItem(order);
		}

		// 初始化担保信用卡
		if (errorMsg == null) {
			errorMsg = InitGuaranteeCreditCard(order);
		}

		// 初始化发票信息
		if (errorMsg == null) {
			errorMsg = initInvoice(order);
		}
		
		// 初始化金额转换表
		if (errorMsg == null) {
			errorMsg = initAmountItem(order);
		}

		// 后初始化订单
		if (errorMsg == null) {
			errorMsg = postInitOrderDTO(order);
		}

		// 错误信息处理
		if (errorMsg != null) {
			handle.setMsg(errorMsg);
		}

		return handle;
	}

	/**
	 * 初始化订单相关属性默认值
	 * 
	 * @param order
	 * @return
	 */
	private String preInitOrderDTO(OrdOrderDTO order) {
		String errorMsg = null;

		if ((order != null) && (order.getBuyInfo() != null)) {
			order.setCreateTime(new Date());
			order.setOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.name());
			order.setPaymentStatus(OrderEnum.PAYMENT_STATUS.UNPAY.name());
			order.setPaymentTime(new Date());
			order.setInfoStatus(OrderEnum.INFO_STATUS.UNVERIFIED.name());
			order.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.UNVERIFIED.name());
			order.setActualAmount(0L);
			order.setCurrencyCode(OrderEnum.ORDER_CURRENCY_CODE.RMB.name());

			BuyInfo buyInfo = order.getBuyInfo();
			order.setDistributorId(buyInfo.getDistributionId());
			order.setDistributorCode(buyInfo.getDistributorCode());
			//设置分销商ID
			order.setDistributionChannel(buyInfo.getDistributionChannel());
			order.setUserId(buyInfo.getUserId());
			order.setUserNo(buyInfo.getUserNo());
			if(StringUtils.isNotEmpty(buyInfo.getNeedGuarantee())){
				order.setGuarantee(buyInfo.getNeedGuarantee());
			}else{
				order.setGuarantee(OrderEnum.CREDIT_CARDER_GUARANTEE.UNGUARANTEE.name());
			}
			if(StringUtils.isNotEmpty(buyInfo.getNeedInvoice())){
				order.setInvoiceStatus(buyInfo.getNeedInvoice());
			}else{
				order.setInvoiceStatus(OrderEnum.NEED_INVOICE_STATUS.UNBILL.name());
			}
			order.setRemark(buyInfo.getRemark());
			order.setClientIpAddress(buyInfo.getIp());
			
			order.setCertConfirmStatus(OrderEnum.CERT_CONFIRM_STATUS.UNCONFIRMED.name());
			order.setCancelCertConfirmStatus(OrderEnum.CANCEL_CERTCONFIRM_STATUS.UNCONFIRMED.name());
			
			order.setBookLimitType(OrderEnum.GUARANTEE_TYPE.NONE.name());
			order.setPaymentType(SuppGoodsTimePrice.BOOKLIMITTYPE.NONE.name());
		} else {
			errorMsg = "您的订单不存在。";
		}

		return errorMsg;
	}

	/**
	 * 订单逻辑后处理
	 * 
	 * @param order
	 * @return
	 */
	private String postInitOrderDTO(OrdOrderDTO order) {
		String errorMsg = null;
		/**
		 * 需求更新，备注与资源状态无关系
		 * 
		if (order != null) {
			// 如果有备注
			if ((order.getRemark() != null) && !"".equals(order.getRemark().trim())) {
				processOrderWithRemark(order);
			}
		} else {
			errorMsg = "您还没有下单。";
		}
		**/
		
		return errorMsg;
	}
	
	/**
	 * 对于有备注的订单记性处理，订单状态：需要资源确认、资源待审核
	 * @param order
	 */
	private void processOrderWithRemark(OrdOrderDTO order) {
		if (order != null) {
			//订单子项列表
			if (order.getOrderItemList() != null) {
				for (OrdOrderItem orderItem : order.getOrderItemList()) {
					//订单子项列表
					if (orderItem != null) {
						//与订单子项关联的订单本地库存列表的资源状态
						if (orderItem.getOrderStockList() != null) {
							for (OrdOrderStock orderStock : orderItem.getOrderStockList()) {
								//订单本地库存
								if (orderStock != null) {
									orderStock.setNeedResourceConfirm("true");
									orderStock.setResourceStatus(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name());
								}
							}
						}
						//与酒店每天使用状态关联的订单本地库存列表的资源状态
						if (orderItem.getOrderHotelTimeRateList() != null) {
							for (OrdOrderHotelTimeRate timeRate : orderItem.getOrderHotelTimeRateList()) {
								if (timeRate != null) {
									if (timeRate.getOrderStockList() != null) {
										for (OrdOrderStock orderStock : timeRate.getOrderStockList()) {
											//订单本地库存
											if (orderStock != null) {
												orderStock.setNeedResourceConfirm("true");
												orderStock.setResourceStatus(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name());
											}
										}
									}
								}
							}
						}
						//更新订单子项的资源状态
						orderItem.setNeedResourceConfirm("true");
						orderItem.setResourceStatus(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name());
					}
				}
			}
			//更新订单的资源状态
			order.setResourceStatus(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name());
		}
	}

	/**
	 * 初始化订单打包商品子项
	 * 
	 * @param ordOrderDTO
	 * @return
	 */
	private String initOrderPack(OrdOrderDTO ordOrderDTO) {
		String errorMsg = null;
			
		BuyInfo buyInfo = ordOrderDTO.getBuyInfo();
		
		if (buyInfo != null) {
			LOG.info("initOrderPack:[CategoryId=" + buyInfo.getCategoryId() + ",ProductId= " + buyInfo.getProductId() + "]");
			if (buyInfo.getCategoryId() != null && buyInfo.getProductId() != null) {
				OrdOrderPack orderPack = new OrdOrderPack();
				orderPack.setCategoryId(buyInfo.getCategoryId());
				orderPack.setProductId(buyInfo.getProductId());
				
				ResultHandleT<BizCategory> resultHandleBizCategory = categoryClientService.findCategoryById(buyInfo.getCategoryId());
				if (resultHandleBizCategory.isSuccess()) {
					BizCategory category = resultHandleBizCategory.getReturnContent();
					orderPack.putContent(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name(), category.getCategoryCode());
					
					if (BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.name().equalsIgnoreCase(category.getCategoryCode())) {
						errorMsg = initCombCuriseOrderPack(ordOrderDTO, orderPack);
					}
				} else {
					errorMsg = "获取CategoryID=" + buyInfo.getCategoryId() + "品类失败，msg=" + resultHandleBizCategory.getMsg();
					return errorMsg;
				}
				
				List<OrdOrderPack> orderPackList = new ArrayList<OrdOrderPack>();
				orderPackList.add(orderPack);
				
				ordOrderDTO.setOrderPackList(orderPackList);
			}
		} else {
			errorMsg = "您未选购商品。";
		}

		return errorMsg;
	}
	
	/**
	 * 组合邮轮查询参数
	 * 
	 * @param buyInfo
	 * @param curiseProductParamsVO
	 * @return
	 */
	private String fillCuriseProductParamsVO(BuyInfo buyInfo, CuriseProductParamsVO curiseProductParamsVO) {
		String errorMsg = null;
		Date groupDate = getEarlyVisitDate(buyInfo);
		if (groupDate == null) {
			errorMsg = "请填写出游日期";
			return errorMsg;
		}
		
		if (buyInfo.getDistributionId() == null) {
			errorMsg = "请填写下单渠道ID。";
			return errorMsg;
		}
		
		
		List<String> propCodeList = new ArrayList<String>();
		propCodeList.add(OrderEnum.ORDER_CURISE_PROP_CODE.an_airline.name());
		propCodeList.add(OrderEnum.ORDER_CURISE_PROP_CODE.are_charter.name());
		propCodeList.add(OrderEnum.ORDER_CURISE_PROP_CODE.earnest.name());
		
		curiseProductParamsVO.setCategoryId(buyInfo.getCategoryId());
		curiseProductParamsVO.setProductId(buyInfo.getProductId());
		curiseProductParamsVO.setPropCodeList(propCodeList);
		curiseProductParamsVO.setGroupDate(groupDate);
		curiseProductParamsVO.setDistributionId(buyInfo.getDistributionId());
		
		curiseProductParamsVO.setQueryEcontract(true);
		curiseProductParamsVO.setQueryGroupDate(true);
		curiseProductParamsVO.setQueryRoute(true);
		curiseProductParamsVO.setQueryShipLine(true);
		
		return errorMsg;
	}
	
	/**
	 * 初始化组合邮轮OrderPack
	 * 
	 * @param ordOrderDTO
	 * @param orderPack
	 * @return
	 */
	private String initCombCuriseOrderPack(OrdOrderDTO ordOrderDTO, OrdOrderPack orderPack) {
		String errorMsg = null;
		BuyInfo buyInfo = ordOrderDTO.getBuyInfo();
		CuriseProductParamsVO curiseProductParamsVO = new CuriseProductParamsVO();
		
		errorMsg = fillCuriseProductParamsVO(buyInfo, curiseProductParamsVO);
		if (errorMsg != null) {
			return errorMsg;
		}
		
		OrdAdditionStatus ordAdditionStatus = OrderUtils.makeOrdAdditionStatus(OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.name(), 
				OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.NO_UPLOAD.name());
		ordOrderDTO.addOrdAdditionStatus(ordAdditionStatus);
		
		ResultHandleT<CuriseProductVO> resultHandleCuriseProductVO = prodCuriseProductClientService.findCombCuriseProduct(curiseProductParamsVO);
		if (resultHandleCuriseProductVO.isSuccess()) {
			CuriseProductVO curiseProductVO = resultHandleCuriseProductVO.getReturnContent();
			if (curiseProductVO != null) {
				if (curiseProductVO.getProduct() != null) {
					orderPack.setProductName(curiseProductVO.getProduct().getProductName());
					orderPack.putContent(OrderEnum.ORDER_PACK_TYPE.departure.name(), curiseProductVO.getDeparturePlace());
				}
				
				if (curiseProductVO.getProductPropMap() != null) {
					Map<String, Object> productPropeMap = curiseProductVO.getProductPropMap();
					
					String airlineStr = null;
					List<PropValue> airlinePropValueList = (List<PropValue>) productPropeMap.get(OrderEnum.ORDER_CURISE_PROP_CODE.an_airline.name());
					if (airlinePropValueList != null && !airlinePropValueList.isEmpty()) {
						airlineStr = airlinePropValueList.get(0).getName();
					}
					
					String charterStr = (String) productPropeMap.get(OrderEnum.ORDER_CURISE_PROP_CODE.are_charter.name());
					String earnestStr = (String) productPropeMap.get(OrderEnum.ORDER_CURISE_PROP_CODE.earnest.name());
					
					//包船逻辑
					if ("Y".equalsIgnoreCase(charterStr)) {
						orderPack.putContent(OrderEnum.ORDER_PACK_TYPE.chartered_boat.name(), "Y");
						
						Long curiseProductId = curiseProductVO.getCuriseProductId();
						Long travellerCapacity = curiseProductVO.getTravellerCapacity();
						
						if (curiseProductId != null && travellerCapacity != null) {
							Long personCount = ordItemPersonRelationService.getPersonCountByProductId(curiseProductId, curiseProductParamsVO.getGroupDate());
							if (personCount != null) {
								if (travellerCapacity < personCount + buyInfo.getTravellers().size()) {
									long leftCapacity = travellerCapacity - personCount;
									LOG.info("超出邮轮最大载客量，目前此邮轮剩余载客量为" + leftCapacity + "人。");
									
									if (leftCapacity < 0) {
										leftCapacity = 0;
									}
									
									errorMsg = "超出邮轮最大载客量，目前此邮轮剩余载客量为" + leftCapacity + "人。";
								}
							} else {
								errorMsg = "检测组合产品ID" + buyInfo.getProductId() + "载客量信息失败。";
							}
						} else {
							errorMsg = "获取组合产品ID" + buyInfo.getProductId() + "载客量信息失败。";
						}
					} else {
						orderPack.putContent(OrderEnum.ORDER_PACK_TYPE.chartered_boat.name(), "N");
					}
					
					//定金
					if (earnestStr != null && StringUtil.isNumber(earnestStr)) {
						List<Person> travellers = buyInfo.getTravellers();
						if (travellers != null && !travellers.isEmpty()) {
							//原本定金单位为元，现在定金单位又变成分了。
							ordOrderDTO.setDepositsAmount(Long.valueOf(earnestStr) * travellers.size());
						}
						
					}
					
					//航线
					if (airlineStr != null) {
						orderPack.putContent(OrderEnum.ORDER_PACK_TYPE.route.name(), airlineStr);
					}
				}
				
				Date beginDate = curiseProductParamsVO.getGroupDate();
				Date endDate = null;
				orderPack.putContent(OrderEnum.ORDER_PACK_TYPE.start_sailing_date.name(), DateUtil.formatDate(beginDate, "yyyy-MM-dd"));
				Long nDays = curiseProductVO.getTravelDays();
				if (nDays == null) {
					endDate = beginDate;
				} else {
					endDate = DateUtil.getDateAfterDays(beginDate, (nDays.intValue() - 1));
				}
				orderPack.putContent(OrderEnum.ORDER_PACK_TYPE.end_sailing_date.name(), DateUtil.formatDate(endDate, "yyyy-MM-dd"));
				
				//设置组合邮轮上船地点、下船地点
				setCombCuriseOnOffPlace(curiseProductVO.getShipLineList(), orderPack);
				
				//设置电子合同
				setTravelEContract(curiseProductVO, ordOrderDTO);
				
				//设置所属公司
				ordOrderDTO.setFilialeName(curiseProductVO.getProduct().getFiliale());
			}
			
		} else {
			errorMsg = resultHandleCuriseProductVO.getMsg();
		}
		
		return errorMsg;
	}
	
	/**
	 * 设置电子合同
	 * 
	 * @param curiseProductVO
	 * @param ordOrder
	 */
	private void setTravelEContract(CuriseProductVO curiseProductVO, OrdOrderDTO ordOrder) {
		if (curiseProductVO != null && ordOrder != null) {
			OrdTravelContract ordTravelContract = OrderUtils.makeOrdTravelContract(curiseProductVO.getEcontract(),ordOrder.getDistributorId());
			if (ordTravelContract != null) {
				List<OrdTravelContract> ordTravelContractList = new ArrayList<OrdTravelContract>();
				ordTravelContractList.add(ordTravelContract);
				
				ordOrder.setOrdTravelContractList(ordTravelContractList);
			}
		}
		
	}

	
	/**
	 * 设置组合邮轮上船地点、下船地点
	 * 
	 * @param shipLineList
	 * @param curiseProductVO
	 */
	private void setCombCuriseOnOffPlace(List<String> shipLineList, OrdOrderPack orderPack) {
		if (shipLineList != null) {
			String onShipPlace = null;
			String offShipPlace = null;
			for (String place : shipLineList) {
				if (place != null && !place.trim().equals("")) {
					if (onShipPlace == null) {
						onShipPlace = place;
						continue;
					} else if (offShipPlace == null) {
						offShipPlace = place;
						break;
					} else {
						break;
					}
				}
			}
			
			if (onShipPlace != null && offShipPlace == null) {
				offShipPlace = onShipPlace;
			}
			
			if (onShipPlace != null) {
				orderPack.putContent(OrderEnum.ORDER_PACK_TYPE.up_place.name(), onShipPlace);
			}
			
			if (offShipPlace != null) {
				orderPack.putContent(OrderEnum.ORDER_PACK_TYPE.down_place.name(), offShipPlace);
			}
		}
	}
	
	/**
	 * 返回最早游玩时间
	 * 
	 * @param buyInfo
	 * @return
	 */
	private Date getEarlyVisitDate(BuyInfo buyInfo) {
		Date retVisitDate = null;
		Date itemVisitDate = null;
		if (buyInfo != null && buyInfo.getItemList() != null) {
			for (Item item : buyInfo.getItemList()) {
				if (item != null) {
					itemVisitDate = item.getVisitTimeDate();
					if (itemVisitDate != null) {
						if (retVisitDate == null) {
							retVisitDate = itemVisitDate;
						} else {
							if (retVisitDate.after(itemVisitDate)) {
								retVisitDate = itemVisitDate;
							}
						}
					}
				}
			}
		}
		
		return retVisitDate;
	}
	
	/**
	 * 预初始化订单子项
	 * 
	 * @param ordOrderDTO
	 * @return
	 */
	private String initPreOrderItem(OrdOrderDTO ordOrderDTO) {
		String errorMsg = null;
		
		BuyInfo buyInfo = ordOrderDTO.getBuyInfo();

		List<OrdOrderItem> orderItemList = new ArrayList<OrdOrderItem>();
		OrdOrderItem orderItem = null;

		List<Item> itemList = buyInfo.getItemList();
		Item item = null;

		// 商品子项存在
		if ((itemList != null) && (itemList.size() > 0)) {
			for (int i = 0; i < itemList.size(); i++) {
				orderItem = new OrdOrderItem();
				orderItemList.add(orderItem);
				item = itemList.get(i);
				errorMsg = fillOrderItemWithOriginalData(orderItem, item, ordOrderDTO);

				if (errorMsg != null) {
					break;
				} 
			}
			
			// 设置订单子项列表
			if ((errorMsg == null) && (orderItemList.size() > 0)) {
				ordOrderDTO.setOrderItemList(orderItemList);
			}
		} else {
			errorMsg = "您未购买商品。";
		}
	
		return errorMsg;
	}
	
	private String initPostOrderItem(OrdOrderDTO ordOrderDTO) {
		String errorMsg = null;
		
		BuyInfo buyInfo = ordOrderDTO.getBuyInfo();

		List<OrdOrderItem> orderItemList = ordOrderDTO.getOrderItemList();
		OrdOrderItemDTO orderItem = null;

		List<Item> itemList = buyInfo.getItemList();
		Item item = null;

		// 商品子项存在
		if ((itemList != null) && (itemList.size() > 0)) {
			for (int i = 0; i < itemList.size(); i++) {
				orderItem = (OrdOrderItemDTO)orderItemList.get(i);
				item = itemList.get(i);
				errorMsg = fillOrderItemDataRecord(orderItem, item, ordOrderDTO);

				if (errorMsg != null) {
					break;
				} else {
					// 计算结算总价
					orderItem.setTotalSettlementPrice(orderItem.getSettlementPrice() * orderItem.getQuantity());
					
					//使用订单子项计算订单中的数据
					fillOrderWithOrderItem(orderItem, ordOrderDTO);
					
					//现付
					if (ordOrderDTO.hasNeedPay()) {
						setOrderGuaranteeType(ordOrderDTO);
					//预付
					} else if (ordOrderDTO.hasNeedPrepaid()) {
						//预付类型已经在遍历订单子项中设置
					}
				}
			}
			
			if (errorMsg == null) {
				//订单支付对象验证
				if (ordOrderDTO.getPaymentTarget() == null || "".equals(ordOrderDTO.getPaymentTarget().trim())) {
					errorMsg = "您购买的商品不存在支付对象。";
				}
			}
		} else {
			errorMsg = "您未购买商品。";
		}

		return errorMsg;
	}
	
	/**
	 * 初始化订单当中的子项列表
	 * 
	 * @param order
	 * @return
	 */
	private String initOrderItem(OrdOrderDTO ordOrderDTO) {
		String errorMsg = null;

		if ((ordOrderDTO != null) && (ordOrderDTO.getBuyInfo() != null)) {
			errorMsg = initPreOrderItem(ordOrderDTO);
			if (errorMsg == null) {
				errorMsg = initPostOrderItem(ordOrderDTO);
			}
		}
		// 没有下单
		else {
			errorMsg = "您还没有没有下单。";
		}

		return errorMsg;
	}
	
	/**
	 * 设置订单的担保类型
	 * 如果主订单有担保类型，则订单的担保类型与主订单子项一致。
	 * 否则按顺利遍历，设置为第一个需要担保的订单子项担保类型。
	 * 
	 * @param order
	 */
	private void setOrderGuaranteeType(OrdOrderDTO order) {
		if (order.getMainOrderItem() != null) {
			order.setBookLimitType(order.getMainOrderItem().getBookLimitType());
		}
		
		if (order.getBookLimitType() == null
				|| OrderEnum.GUARANTEE_TYPE.NONE.name().equalsIgnoreCase(order.getBookLimitType())) {
			if (order.getOrderItemList() != null) {
				for (OrdOrderItem orderItem : order.getOrderItemList()) {
					if (orderItem != null) {
						if (orderItem.getBookLimitType() == null
								&& !OrderEnum.GUARANTEE_TYPE.NONE.name().equalsIgnoreCase(orderItem.getBookLimitType())) {
							order.setBookLimitType(orderItem.getBookLimitType());
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * 组装订单子项记录
	 * 
	 * @param orderItem
	 *            被组装的订单子项
	 * @param item
	 *            原始订单子项vo
	 * @param ordOrderDTO
	 *            订单对象
	 * @return 错误信息，无错误返回null
	 */
	private String fillOrderItemDataRecord(OrdOrderItemDTO orderItem, Item item, OrdOrderDTO ordOrderDTO) {
		String errorMsg = null;

		SuppGoods suppGoods = orderItem.getSuppGoods();
		if (suppGoods == null) {
			//调用商品服务，查询订单子项商品（先从商品缓存中查找）
			ResultHandleT<SuppGoods> resultHandleGoods = suppGoodsClientService.findSuppGoodsForOrder(item.getGoodsId());
			if (resultHandleGoods.isSuccess()) {
				suppGoods = resultHandleGoods.getReturnContent();
			} else {
				errorMsg="无法获取购买商品(ID=" + item.getGoodsId() + ")信息。";
				LOG.info(errorMsg);
				return errorMsg;
			}
		}

		BizCategory bizCategory = null;
		if(suppGoods != null && suppGoods.getProdProduct() != null) {
			bizCategory = categoryClientService.findCategoryById(suppGoods.getProdProduct().getBizCategoryId()).getReturnContent();
		}
		if ((suppGoods != null) 
				&& (suppGoods.isValid()) 
				&& (suppGoods.getProdProduct() != null) 
				&& (suppGoods.getProdProductBranch() != null)
				&& (bizCategory != null)) {
			
			// 设置订单支付对象
			if (suppGoods.getPayTarget() != null) {
				if (ordOrderDTO.getPaymentTarget() == null) {
					ordOrderDTO.setPaymentTarget(suppGoods.getPayTarget());
				} else {
					if (!ordOrderDTO.getPaymentTarget().equals(suppGoods.getPayTarget())) {
						errorMsg = "订单支付对象（" + ordOrderDTO.getPaymentTarget() + "）与商品（ID=" + suppGoods.getSuppGoodsId() + "）" 
										+ suppGoods.getGoodsName() + "支付对象（" + suppGoods.getPayTarget() + "）冲突。";
					}
				}
				
				if (errorMsg == null) {
					//订单相关选项和与主订单子项相同
					if (orderItem.hasMainItem()) {
						//如果在initOrderPack中没有设置组合产品所属公司才设置
						if (ordOrderDTO.getFilialeName() == null) {
							//所属公司
							ordOrderDTO.setFilialeName(suppGoods.getFiliale());
						}
					}

					//品类ID
					orderItem.setCategoryId(suppGoods.getProdProduct().getBizCategoryId());
					
					//规格ID
					orderItem.setBranchId(suppGoods.getProdProductBranch().getProductBranchId());
					
					//合同ID
					orderItem.setContractId(suppGoods.getContractId());
					
					//产品ID
					orderItem.setProductId(suppGoods.getProductId());
					
					//供应商ID
					orderItem.setSupplierId(suppGoods.getSupplierId());
					
					//产品名称
					orderItem.setProductName(suppGoods.getProdProduct().getProductName());
					
					//商品名称
					orderItem.setSuppGoodsName(suppGoods.getGoodsName());
					
					//商品数量
					orderItem.setQuantity(Long.valueOf(item.getQuantity()));

					//履行状态
					orderItem.setPerformStatus(OrderEnum.ORDER_PERFORM_STATUS.UNPERFORM.name());
					
					//结算状态
					orderItem.setSettlementStatus(OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.name());
					
					// 信息状态-未确认
					orderItem.setInfoStatus(OrderEnum.INFO_STATUS.UNVERIFIED.name());
					
					//品类code
					orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name(), bizCategory.getCategoryCode());
					
					//添加子订单流程key
					orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), bizCategory.getProcessKey());
					
					//供应商标识
					orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.supplierApiFlag.name(), suppGoods.getApiFlag());
					
					ordOrderDTO.putApiFlag(suppGoods.getSuppGoodsId(),"Y".equals(suppGoods.getApiFlag()));
					
					//根据商品，查找对应产品的品类，获取品类代码，判断是否是酒店产品
					final boolean isHotel = isHotelCategoryCode(suppGoods);
					String branchName = suppGoods.getProdProductBranch().getBranchName();
					if (isHotel) {
						//填充酒店附加信息到订单子项
						errorMsg = fillOrderItemContentWithHotelAdditationInfo(orderItem, item, ordOrderDTO,suppGoods);
					}
					
					//传真规则
					if(suppGoods.getFaxRuleId()!=null){
						orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fax_rule.name(), suppGoods.getFaxRuleId());
					}
					
					//是否使用传真
					if(suppGoods.getFaxFlag()!=null){
						orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fax_flag.name(), suppGoods.getFaxFlag());
					}
					
					orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.branchName.name(), branchName.trim());
					
					if (errorMsg == null) {
						//验证时间价格表
						OrderTimePriceService orderTimePriceService = orderTimePriceServiceFactory.createTimePriceService(bizCategory);
						if (orderTimePriceService != null) {
							ResultHandle resultHandle = orderTimePriceService.validate(suppGoods, item, orderItem, ordOrderDTO);
							if (resultHandle.isFail()) {
								errorMsg = resultHandle.getMsg();
							}
						} else {
							errorMsg = "商品（ID=" + suppGoods.getSuppGoodsId() + "）" + suppGoods.getGoodsName() + "时间价格表不存在。";
						}
					}
				}
			} else {
				errorMsg = "商品（ID=" + suppGoods.getSuppGoodsId() + "）" + suppGoods.getGoodsName() + "支付对象不存在。";
			}
		} else {
			errorMsg = "您购买的商品-" + suppGoods.getGoodsName()  + "(ID=" + suppGoods.getSuppGoodsId() + ")不可售。";
		}

		return errorMsg;
	}

	/**
	 * 填充酒店附加信息到订单子项
	 * 
	 * @param orderItem 订单子项PO
	 * @param item 订单子项VO
	 * @param ordOrderDTO 订单
	 * @param branchName 规格名称
	 * @return
	 */
	private String fillOrderItemContentWithHotelAdditationInfo(OrdOrderItem orderItem, Item item, OrdOrderDTO ordOrderDTO,SuppGoods suppGoods) {
		String errorMsg = null;
		// 因为HotelAdditation对象属性并不是全部加入到JSON，所以不易采用反射机制。
		if ((item != null) && (item.getHotelAdditation() != null)) {
			errorMsg = checkHotelAdditationInfo(item, suppGoods);
			
			if (errorMsg == null) {
				orderItem.putContent(OrderEnum.HOTEL_CONTENT.lastArrivalTime.name(), item.getHotelAdditation().getArrivalTime());
				orderItem.putContent(OrderEnum.HOTEL_CONTENT.earlyArrivalTime.name(), item.getHotelAdditation().getEarlyArrivalTime());
				String str="无";
				if(suppGoods.getProdProductBranch().getPropValue().containsKey("add_bed_flag")){
					List<PropValue> list = (List<PropValue>)suppGoods.getProdProductBranch().getPropValue().get("add_bed_flag");
					if(CollectionUtils.isNotEmpty(list)){
						str =list.get(0).getName();
						if(StringUtils.isNotEmpty(list.get(0).getAddValue())){
							str += list.get(0).getAddValue();
						}
					}
				}
				orderItem.putContent(OrderEnum.HOTEL_CONTENT.addBedFlag.name(), str);
				
				str="无";
				if(suppGoods.getProdProductBranch().getPropValue().containsKey("internet")){
					List<PropValue> list = (List<PropValue>)suppGoods.getProdProductBranch().getPropValue().get("internet");
					if(CollectionUtils.isNotEmpty(list)){
						str =list.get(0).getName();
						if(StringUtils.isNotEmpty(list.get(0).getAddValue())){
							str += list.get(0).getAddValue();
						}
					}
				}
				orderItem.putContent(OrderEnum.HOTEL_CONTENT.internet.name(), str);
			}
		}

		return errorMsg;
	}
	
	/**
	 * 验证酒店附件信息
	 * 
	 * @param item
	 * @return
	 */
	private String checkHotelAdditationInfo(Item item, SuppGoods suppGoods) {
		String errorMsg = null;
		HotelAdditation hotelAdditation = item.getHotelAdditation();
		String timeReg = "(([0-1]?[0-9])|([2][0-3])):([0-5]?[0-9])";
		
		if (hotelAdditation != null) {
			if (hotelAdditation.getArrivalTime() != null) {
				if (hotelAdditation.getEarlyArrivalTime() == null) {
					String earlyArrivalTime = suppGoods.getProdProduct().getPropValue().get("earliest_arrive_time").toString();
					if (earlyArrivalTime != null) {
						if (earlyArrivalTime.indexOf(":") < 0) {
							earlyArrivalTime = earlyArrivalTime + ":00";
						}
						
						if (!earlyArrivalTime.matches(timeReg)) {
							errorMsg = "酒店入住的最早到达时间不正确，earlyArrivalTime=" + earlyArrivalTime;
							LOG.debug("method checkHotelAdditationInfo: msg=" + errorMsg);
						}
					}
					hotelAdditation.setEarlyArrivalTime(earlyArrivalTime);
				}
				
				if (!hotelAdditation.getArrivalTime().matches(timeReg)) {
					errorMsg = "酒店入住的到达时间格式不正确，arrivalTime=" + hotelAdditation.getArrivalTime();
					LOG.debug("method checkHotelAdditationInfo: msg=" + errorMsg);
				}
				
				if (hotelAdditation.getEarlyArrivalTime() != null) {
					if (!hotelAdditation.getEarlyArrivalTime().matches(timeReg)) {
						errorMsg = "酒店入住的最早到达时间格式格式不正确，earlyArrivalTime=" + hotelAdditation.getEarlyArrivalTime();
						LOG.debug("method checkHotelAdditationInfo: msg=" + errorMsg);
					} else {
						Date visitTime = item.getVisitTimeDate();
						String[] earlyTimeStrs = hotelAdditation.getEarlyArrivalTime().split(":");
						int hour = Integer.parseInt(earlyTimeStrs[0]);
						int min = Integer.parseInt(earlyTimeStrs[1]);
						visitTime = DateUtil.DsDay_HourOfDay(visitTime, hour);
						visitTime = DateUtil.DsDay_Minute(visitTime, min);
						Date now = new Date();
						
						if (visitTime.before(now)) {
							now = DateUtils.addMinutes(now, 40);
							Calendar c = Calendar.getInstance();
							c.setTime(now);
							hour = c.get(Calendar.HOUR_OF_DAY);
							min = c.get(Calendar.MINUTE);
							StringBuffer sb=new StringBuffer();
							sb.append(hour);
							sb.append(":");
							if(min<30){
								sb.append("00");
							}else{
								sb.append("30");
							}
							hotelAdditation.setEarlyArrivalTime(sb.toString());
							//errorMsg = "酒店入住的最早到达时间(" + DateUtil.formatDate(visitTime, "yyyy-MM-dd") + ")必须大于当前时间。";
							//LOG.debug("method checkHotelAdditationInfo: msg=" + errorMsg);
						}
					}
				}
			} else {
				errorMsg = "请您填写酒店入住的到达时间。";
			}
		} else {
			errorMsg = "请您填写酒店入住的相关信息。 ";
		}
		
		return errorMsg;
	}
	
	private String getHourTime(String time,boolean plus){
		int hour = NumberUtils.toInt(time.substring(0,time.indexOf(":")));
		if(plus){
			hour+=3;
		}else{
			hour-=3;
		}
		StringBuffer sb = new StringBuffer();
		if(hour<10){
			sb.append("0");
		}
		sb.append(hour);
		sb.append(time.substring(time.indexOf(":")));
		return sb.toString();
	}
	
	/**
	 * 判断商品是否是酒店类型
	 * 
	 * @param suppGoods
	 * @return
	 */
	private boolean isHotelCategoryCode(SuppGoods suppGoods) {
		boolean isHotel = false;

		if ((suppGoods != null) && (suppGoods.getProdProduct() != null)) {

			BizCategory bizCategory = suppGoods.getProdProduct().getBizCategory();

			if (bizCategory != null) {
				if ((bizCategory.getCategoryCode() != null) && bizCategory.getCategoryCode().toLowerCase().equals(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.name())) {
					isHotel = true;
				}
			}
		}

		return isHotel;
	}

	/**
	 * 使用值对象中订单子项填充持久对象中的订单子项，同时做简单验证
	 * 
	 * @param orderItem
	 *            持久对象中的订单子项
	 * @param item
	 *            值对象中的订单子项
	 * @param ordOrderDTO
	 *            订单
	 * @return 错误信息，无错误返回null
	 */
	private String fillOrderItemWithOriginalData(OrdOrderItem orderItem, Item item, OrdOrderDTO ordOrderDTO) {
		String errorMsg = null;

		// 设置主菜单子项
//		orderItem.setMainItem(item.getMainItem());
		
		//凭证确认状态
		orderItem.setCertConfirmStatus(OrderEnum.ITEM_CERT_CONFIRM_STATUS.UNCONFIRMED.name());
		
		//取消凭证确认
		orderItem.setCancelCertConfirmStatus(OrderEnum.ITEM_CANCEL_CERTCONFIRM_STATUS.UNCONFIRMED.name());
		
		//现付担保类型
		orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.NONE.name());
		
		//扣款类型
		orderItem.setDeductType(SuppGoodsTimePrice.DEDUCTTYPE.NONE.name());
		
		//担保总量
		orderItem.setDeductAmount(0L);

		// 商品ID判断
		if (item.getGoodsId() != null) {
			//设置供应商商品ID
			orderItem.setSuppGoodsId(item.getGoodsId());

			// 商品购买数量判断
			if (item.getQuantity() > 0) {
				// 游玩时间判断
				orderItem.setVisitTime(item.getVisitTimeDate());
				if (orderItem.getVisitTime() == null) {
					errorMsg = "您的订单中存在游玩时间不正确的商品。";
					return errorMsg;
				}
			} else {
				errorMsg = "您订单中的商品ID=" + item.getGoodsId() + "购买数量小于等于零。";
				return errorMsg;
			}
		} else {
			errorMsg = "您购买了不存在的商品。";
			return errorMsg;
		}
		
		//传真备注，设置在订单子项中
		String faxMemo = ordOrderDTO.getBuyInfo().getFaxMemo();
		if (faxMemo != null && !"".equals(faxMemo)) {
			orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fax_remark.name(), faxMemo);
		}
		
		if (item.getOwnerQuantity() > 0) {
			orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.ownerQuantity.name(), item.getOwnerQuantity());
		}
		
		List<ItemPersonRelation> itemPersonRelationList = item.getItemPersonRelationList();
		if (itemPersonRelationList != null && !itemPersonRelationList.isEmpty()) {
			List<OrdItemPersonRelation> ordItemPersonRelationList = new ArrayList<OrdItemPersonRelation>();
			List<Person> travellers = ordOrderDTO.getBuyInfo().getTravellers();
			for (ItemPersonRelation itemPersonRelation : itemPersonRelationList) {
				if (itemPersonRelation != null) {
					Person person = itemPersonRelation.getPerson();
					int index = travellers.indexOf(person);
					if (index >= 0) {
						OrdPerson ordPerson = ordOrderDTO.getOrdTravellerList().get(index);
						OrdItemPersonRelation ordItemPersonRelation = OrderUtils.makeItemOrdPersonRelationRecord(itemPersonRelation.getRoomNo(), itemPersonRelation.getOptionContent(), Long.valueOf(itemPersonRelation.getSeq()), ordPerson);
						if (ordItemPersonRelation != null) {
							ordItemPersonRelationList.add(ordItemPersonRelation);
						}
					}
				}
			}
			orderItem.setOrdItemPersonRelationList(ordItemPersonRelationList);
		}
		
		//调用商品服务，查询订单子项商品（先从商品缓存中查找）
		ResultHandleT<SuppGoods> resultHandleGoods = suppGoodsClientService.findSuppGoodsForOrder(item.getGoodsId());
		if (resultHandleGoods.isSuccess() && resultHandleGoods.getReturnContent() != null) {
			orderItem.setSuppGoods(resultHandleGoods.getReturnContent());
		} else {
			errorMsg="无法获取购买商品(ID=" + item.getGoodsId() + ")信息。";
			LOG.info(errorMsg);
			return errorMsg;
		}
		
		setOrderMainOrderItem(orderItem, ordOrderDTO);

		return errorMsg;
	}
	
	/**
	 * 设置主订单
	 * 
	 * @param orderItem
	 * @param orderDTO
	 */
	private void setOrderMainOrderItem(OrdOrderItem orderItem, OrdOrderDTO orderDTO) {
		OrdOrderItem filterMainOrderItem = orderDTO.getFilterMainOrderItem();
		if (filterMainOrderItem == null) {
			orderItem.setMainItem("true");
			orderDTO.setFilterMainOrderItem(orderItem);
		} else {
			int filterLevel = getCategoryLevelByCategoryCode(filterMainOrderItem.getSuppGoods().getProdProduct().getBizCategory());
			int level = getCategoryLevelByCategoryCode(orderItem.getSuppGoods().getProdProduct().getBizCategory());
			if (level < filterLevel) {
				filterMainOrderItem.setMainItem("false");
				orderItem.setMainItem("true");
				orderDTO.setFilterMainOrderItem(orderItem);
			} else {
				orderItem.setMainItem("false");
			}
		}
	}
	
	/**
	 * 获取主订单品类级别，数字越低级别越高
	 * 
	 * @param category
	 * @return
	 */
	private int getCategoryLevelByCategoryCode(BizCategory category) {
		int level = 100000;
		
		if (BizEnum.BIZ_CATEGORY_TYPE.category_addition.name().equalsIgnoreCase(category.getCategoryCode())) {
			
		} else if (BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.name().equalsIgnoreCase(category.getCategoryCode())) {
			
		} else if (BizEnum.BIZ_CATEGORY_TYPE.category_cruise.name().equalsIgnoreCase(category.getCategoryCode())) {
			level = 1;
		} else if (BizEnum.BIZ_CATEGORY_TYPE.category_cruise_addition.name().equalsIgnoreCase(category.getCategoryCode())) {
			
		} else if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.name().equalsIgnoreCase(category.getCategoryCode())) {
			level = 2;
		} else if (BizEnum.BIZ_CATEGORY_TYPE.category_insurance.name().equalsIgnoreCase(category.getCategoryCode())) {
			
		} else if (BizEnum.BIZ_CATEGORY_TYPE.category_sightseeing.name().equalsIgnoreCase(category.getCategoryCode())) {
			
		} else if (BizEnum.BIZ_CATEGORY_TYPE.category_visa.name().equalsIgnoreCase(category.getCategoryCode())) {
			
		}
		
		return level;
	}

	/**
	 * 获取时间价格表数据，做逻辑验证
	 * 
	 * @param orderItem
	 *            持久对象订单子项
	 * @param item
	 *            值对象订单那子项
	 * @param ordOrderDTO
	 *            订单
	 * @param isHotelCategoryCode
	 *            是否是酒店类订单子项
	 * @return 错误信息，无错误返回null
	 */
	private String processTimePriceTable(OrdOrderItem orderItem, Item item, OrdOrderDTO ordOrderDTO, boolean isHotelCategoryCode, SuppGoods suppGoods) {
		String errorMsg = null;

		if (isHotelCategoryCode) {
			//验证酒店类型的时间价格表
			errorMsg = processHotelTimePriceTable(orderItem, item, ordOrderDTO, suppGoods);
		} else {
			//验证其他类型（除酒店类型外）的时间价格表
			errorMsg = processOtherTimePriceTable(orderItem, item, ordOrderDTO, suppGoods);
		}

		return errorMsg;
	}

	/**
	 * 处理非酒店类型时间价格表数据
	 * 
	 * @param orderItem
	 * @param item
	 * @param ordOrderDTO
	 * @return
	 */
	private String processOtherTimePriceTable(OrdOrderItem orderItem, Item item, OrdOrderDTO ordOrderDTO, SuppGoods suppGoods) {
		String errorMsg = null;

		if ((orderItem != null) && (item != null) && (item.getHotelAdditation() != null) && (ordOrderDTO != null)) {
			TimePrice timePrice = null;
			TimePrice deductTimePrice = null;
			ResultHandleT<TimePrice> timePriceHolder = null;
			ResultHandleT<TimePrice> guaranteeTimePriceHolder = null;
			List<TimePrice> everydayTimePriceList = new ArrayList<TimePrice>();

			Date date = orderItem.getVisitTime();
			timePriceHolder = distGoodsTimePriceClientServiceAdaptor.findTimePrice(ordOrderDTO.getDistributorId(), item.getGoodsId(), date);
			
			//退改策略,默认为"可退改"
			String cancelStrategyTmp = SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name();
			
			if ((timePriceHolder != null) && timePriceHolder.isSuccess() && (timePriceHolder.getReturnContent() != null)) {
				List<OrdOrderStock> orderStockList = new ArrayList<OrdOrderStock>();
				timePrice = timePriceHolder.getReturnContent();
				errorMsg = checkTimePriceTable(timePrice, orderItem, ordOrderDTO, OrderEnum.ORDER_STOCK_OBJECT_TYPE.ORDERITEM.name(), date, orderStockList);
				if (errorMsg != null) {
					//使用时间价格表填充订单子项
					accumulateOrderItemDataWithTimePrice(timePrice, orderItem);
					
					//使用时间价格表填充订单
					fillOrderWithTimePrice(timePrice, ordOrderDTO);

					orderItem.setOrderStockList(orderStockList);
					
					//设置订单子项资源状态
					setOrderItemResourceStatusByOrderStockList(orderItem, orderStockList);
					
					everydayTimePriceList.add(timePrice);
					
					//主子订单
					if (orderItem.hasMainItem()) {
						//预付
						if (ordOrderDTO.hasNeedPrepaid()) {
							ordOrderDTO.setPaymentType(timePrice.getBookLimitType());
						//现付
						} else {
							ordOrderDTO.setBookLimitType(timePrice.getBookLimitType());
						}
					}
					if(timePrice != null) {
						orderItem.setDeductType(timePrice.getDeductType());
					}
					deductTimePrice = timePrice;

					//预付
					if (SuppGoods.PAYTARGET.PREPAID.name().equals(suppGoods.getPayTarget())) {
						ordOrderDTO.setPaymentType(timePrice.getBookLimitType());
						
						String cancelStrategy = timePrice.getCancelStrategy();
						if(StringUtils.isNotEmpty(cancelStrategy)){
							cancelStrategyTmp = cancelStrategy;
						}
					//现付
					} else if (SuppGoods.PAYTARGET.PAY.name().equals(suppGoods.getPayTarget())) {
						guaranteeTimePriceHolder = setHotelOrderItemGuaranteeInfo(orderItem, item, timePrice);
						if (guaranteeTimePriceHolder.isFail()) {
							errorMsg = guaranteeTimePriceHolder.getMsg();
						}
						
						String cancelStrategy = timePrice.getCancelStrategy();
						if(StringUtils.isNotEmpty(cancelStrategy)){
							cancelStrategyTmp = cancelStrategy;
						}
					} else {
						errorMsg = "商品（ID=" + suppGoods.getSuppGoodsId() + "）" + suppGoods.getGoodsName() + "支付对象不存在。";
						cancelStrategyTmp = null;
					}
					
					if (errorMsg == null) {
						//为优惠信息设置时间价格表
						ordOrderDTO.addItemDateTimeTableForPromotion(orderItem, date, timePrice.getPrice(), timePrice.getSettlementPrice());
						
						//计算退改价格
						long deductAmount = 0;
						if (deductTimePrice != null) {
							deductAmount = computeOrderItemDeductAmount(orderItem, deductTimePrice, everydayTimePriceList);
						}
						orderItem.setDeductAmount(deductAmount);
						
						//设置订单子项的退改策略
						orderItem.setCancelStrategy(cancelStrategyTmp);
					}
				}
			} else {
				errorMsg = "您购买的商品中存在下架商品。";
			}
		} else {
			errorMsg = "您的订单不存在。";
		}

		return errorMsg;
	}
	
	/**
	 * 处理酒店类型时间价格表数据
	 * 
	 * @param orderItem
	 * @param item
	 * @param ordOrderDTO
	 * @return
	 */
	private String processHotelTimePriceTable(OrdOrderItem orderItem, Item item, OrdOrderDTO ordOrderDTO, SuppGoods suppGoods) {
		String errorMsg = null;

		if ((orderItem != null) && (item != null) && (item.getHotelAdditation() != null) && (ordOrderDTO != null)) {
			TimePrice timePrice = null;
			TimePrice deductTimePrice = null;
			ResultHandleT<TimePrice> timePriceHolder = null;
			ResultHandleT<TimePrice> guaranteeTimePriceHolder = null;
			List<TimePrice> everydayTimePriceList = new ArrayList<TimePrice>();
			List<TimePrice> allTimeGuaranteeTimePriceList = new ArrayList<TimePrice>();

			//酒店类型的商品每天使用的状况表
			List<OrdOrderHotelTimeRate> ordOrderHotelTimeRateList = new ArrayList<OrdOrderHotelTimeRate>();

			//酒店额外信息
			HotelAdditation hotleAdditation = item.getHotelAdditation();
			
			//前台保证入店日期和离店日期都是日期类型，即时间为00:00:00
			Date startDate = item.getVisitTimeDate();
			Date endDate = hotleAdditation.getLeaveTimeDate();

			//获取入住时间日期列表
			List<Date> dateList = CalendarUtils.getDatesExtension(startDate, true, endDate, false);
			if (dateList != null && dateList.size()>0) {
				//退改策略,默认为"可退改"
				String cancelStrategyTmp = SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name();
				for (int i = 0; i < dateList.size(); i++) {
					Date date = dateList.get(i);
					//取得入住 每天的时间价格表
					timePriceHolder = distGoodsTimePriceClientServiceAdaptor.findTimePrice(ordOrderDTO.getDistributorId(), item.getGoodsId(), date);
					if ((timePriceHolder != null) && timePriceHolder.isSuccess() && (timePriceHolder.getReturnContent() != null)) {
						//构造订单本地库存列表
						List<OrdOrderStock> orderStockList = new ArrayList<OrdOrderStock>();
						timePrice = timePriceHolder.getReturnContent();
						//时间价格表验证
						errorMsg = checkTimePriceTable(timePrice, orderItem, ordOrderDTO, OrderEnum.ORDER_STOCK_OBJECT_TYPE.HOTEL_TIME_RATE.name(), date, orderStockList);
						if (errorMsg != null) {
							break;
						}
						
						//使用时间价格表填充订单子项
						accumulateOrderItemDataWithTimePrice(timePrice, orderItem);
						
						everydayTimePriceList.add(timePrice);
						
						//预付
						if (SuppGoods.PAYTARGET.PREPAID.name().equals(suppGoods.getPayTarget())) {
							//首日
							if (i == 0) {
								orderItem.setDeductType(timePrice.getDeductType());
								deductTimePrice = timePrice;
							}
							
							//订单的预授权
							if (timePrice.getBookLimitType() != null) {
								if (!SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name().equalsIgnoreCase(ordOrderDTO.getPaymentType())) {
									ordOrderDTO.setPaymentType(timePrice.getBookLimitType());
								}
							}
							
							//预付时,如果时间价格表的退改策略存在“不退不改”，则订单子项的退改策略就为"不退不改"
							String cancelStrategy = timePrice.getCancelStrategy();
							if(StringUtils.isNotEmpty(cancelStrategy) && cancelStrategy.equals(SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name())){
								cancelStrategyTmp = cancelStrategy;
							}
						//现付
						} else if (SuppGoods.PAYTARGET.PAY.name().equals(suppGoods.getPayTarget())) {
							//现付时，订单子项的担保规则取的是哪一天的时间价格，退改策略就设置为哪一天的退改策略
							//首日
							if (i == 0) {
								orderItem.setDeductType(timePrice.getDeductType());
								
								guaranteeTimePriceHolder = setHotelOrderItemGuaranteeInfo(orderItem, item, timePrice);
								if (guaranteeTimePriceHolder.isFail()) {
									errorMsg = guaranteeTimePriceHolder.getMsg();
									break;
								}
								deductTimePrice = guaranteeTimePriceHolder.getReturnContent();
								
								if (deductTimePrice != null
										&& SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equalsIgnoreCase(deductTimePrice.getBookLimitType())) {
									allTimeGuaranteeTimePriceList.add(deductTimePrice);
								}
								
								//逻辑是在这一天设置的担保类型，那么就在这一天取退改策略
								String cancelStrategy = timePrice.getCancelStrategy();
								if(StringUtils.isNotEmpty(cancelStrategy)){
									cancelStrategyTmp = cancelStrategy;
								}
							} else {
								if (SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equalsIgnoreCase(timePrice.getBookLimitType())) {
									guaranteeTimePriceHolder = setHotelOrderItemGuaranteeInfo(orderItem, item, timePrice);
									if (guaranteeTimePriceHolder.isFail()) {
										errorMsg = guaranteeTimePriceHolder.getMsg();
										break;
									}
									deductTimePrice = guaranteeTimePriceHolder.getReturnContent();
									if (deductTimePrice != null
											&& SuppGoodsTimePrice.BOOKLIMITTYPE.ALLTIMEGUARANTEE.name().equalsIgnoreCase(deductTimePrice.getBookLimitType())) {
										allTimeGuaranteeTimePriceList.add(deductTimePrice);
									}
								}
							}
						} else {
							errorMsg = "商品（ID=" + suppGoods.getSuppGoodsId() + "）" + suppGoods.getGoodsName() + "支付对象不存在。";
							cancelStrategyTmp = null;//出错了就不设置退改策略
							break;
						}
						//使用时间价格表填充订单
						fillOrderWithTimePrice(timePrice, ordOrderDTO);

						// 酒店类型订单子项中，添加各天使用情况表记录
						OrdOrderHotelTimeRate ordOrderHotelTimeRate = makeOrdOrderHotelTimeRateRecord(date, orderItem.getQuantity(), timePrice.getPrice(), timePrice.getSettlementPrice(), 0L,
								Long.valueOf(timePrice.getBreakfast()));
						ordOrderHotelTimeRate.setOrderStockList(orderStockList);
						ordOrderHotelTimeRateList.add(ordOrderHotelTimeRate);
						//为优惠信息设置时间价格表
						ordOrderDTO.addItemDateTimeTableForPromotion(orderItem, date, timePrice.getPrice(), timePrice.getSettlementPrice());
					} else {
						errorMsg = "您购买的商品中存在下架商品。";
						break;
					}
				}
				
				if (errorMsg == null) {
					//酒店各天使用状况和订单子项关联
					orderItem.setOrderHotelTimeRateList(ordOrderHotelTimeRateList);
					
					//设置订单子项资源状态
					setOrderItemResourceStatusByHotelRateTimeList(orderItem, ordOrderHotelTimeRateList);
					
					//存在全程担保
					if (allTimeGuaranteeTimePriceList.size() > 0) {
						deductTimePrice = getMaxDeductAmountTimePrice(orderItem, allTimeGuaranteeTimePriceList, everydayTimePriceList);
						orderItem.setDeductType(deductTimePrice.getDeductType());
					}
					
					//计算退改价格
					long deductAmount = 0;
					LOG.info("【calc deductAmount】 start");
					if (deductTimePrice != null) {
						deductAmount = computeOrderItemDeductAmount(orderItem, deductTimePrice, everydayTimePriceList);
					}
					LOG.info("【calc deductAmount】 end, deductAmount =" +deductAmount);
					orderItem.setDeductAmount(deductAmount);
					
					//设置订单子项最终的退改策略
					orderItem.setCancelStrategy(cancelStrategyTmp);
				}
			}
		} else {
			errorMsg = "您的订单不存在。";
		}

		return errorMsg;
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
		LOG.info("【calc deductAmount】  OrderValidCheckBussiness.computeOrderItemDeductAmount start...");
		long deductAmount = 0;
		LOG.info("【calc deductAmount】 applyTimePrice.getTimePriceId()="+ applyTimePrice.getTimePriceId()
				+ ",applyTimePrice.getDeductType() =" +applyTimePrice.getDeductType()
				+",applyTimePrice.getDeductValue()= " +applyTimePrice.getDeductValue()
				+",orderItem.getPrice()= " +orderItem.getPrice()
				+",orderItem.getQuantity()= " +orderItem.getQuantity());
		
		if (applyTimePrice.getDeductType() != null) {
			if (SuppGoodsTimePrice.DEDUCTTYPE.NONE.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.FULL.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
				deductAmount = orderItem.getPrice() * orderItem.getQuantity();
				
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.FIRSTDAY.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
				LOG.info("【calc deductAmount】  everydayTimePriceList.size =" +everydayTimePriceList.size());
				if (everydayTimePriceList.get(0) != null) {
					LOG.info("【calc deductAmount】  everydayTimePriceList.get(0).getPrice() =" +everydayTimePriceList.get(0).getPrice());
					deductAmount = everydayTimePriceList.get(0).getPrice() * orderItem.getQuantity();
				}
				
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.MONEY.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
				deductAmount = applyTimePrice.getDeductValue() * orderItem.getQuantity();
				
			} else if (SuppGoodsTimePrice.DEDUCTTYPE.PERCENT.name().equalsIgnoreCase(applyTimePrice.getDeductType())) {
				
				deductAmount = (long) ((orderItem.getPrice() * orderItem.getQuantity()) * applyTimePrice.getDeductValue() / 100.0 + 0.5);
				
			} else {
				LOG.info("【calc deductAmount】  throw IllegalArgumentException");
				throw new IllegalArgumentException("TimePrice(ID=" + applyTimePrice.getTimePriceId() + ")'s getDeductValue=" + applyTimePrice.getDeductType() + ", is illegal.");
			}
		} else {
			LOG.info("OrderValidCheckBussiness.computeOrderItemDeductAmount: TimePrice(ID=" + applyTimePrice.getTimePriceId() + ")'s getDeductValue=null.");
		}
		
		return deductAmount;
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
		String errorMsg = null;
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
				LOG.info("it is TIMEOUTGUARANTEE");
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
						LOG.info("TIMEOUTGUARANTEE return timePrice");
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
				LOG.info("it is QUANTITYGUARANTEE");
				//默认无限制
				orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.NONE.name());
				if (orderItem.getQuantity() > timePrice.getGuarQuantity()) {
					//房量担保
					orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.QUANTITYGUARANTEE.name());
					guaranteeTimePriceHolder.setReturnContent(timePrice);
					LOG.info("QUANTITYGUARANTEE return  timePrice");
				}
			} else if (bookLimitType == null || SuppGoodsTimePrice.BOOKLIMITTYPE.NONE.name().equalsIgnoreCase(bookLimitType)) {
				//默认无限制
				orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.NONE.name());
			}
		}
		
		return guaranteeTimePriceHolder;
	}
	
	/**
	 * 根据游玩时间，判断当前下单时间是否早于最晚预定时间
	 * 
	 * @param vistDate
	 * @param currDate
	 * @param lastHoldTimeMinute (分钟)
	 * @return
	 */
	private boolean currentTimeIsBeforeLastHoldTime(Date visitDate, Date currDate, Long lastHoldTimeMinute)
	{
		boolean isBefore = false;
		
		if (visitDate != null && currDate != null) {
			//convert Long to int type, Maybe block
			Date countDate = DateUtil.DsDay_Minute(visitDate, (int)(-lastHoldTimeMinute));
			if (currDate.before(countDate)){
				isBefore = true;
			}
		}
		
		return isBefore;
	}
	
	/**
	 * 判断下单时间是否在提前预定时间之前
	 * 
	 * @param visitDate
	 * @param currDate
	 * @param aheadBookTime
	 * @return
	 */
	private boolean currentTimeIsBeforeAheadBookTime(Date visitDate, Date currDate, Long aheadBookTime)
	{
		boolean isBefore = false;
		
		if (visitDate != null && currDate != null) {
			//convert Long to int type, Maybe block
			Date countDate = DateUtil.DsDay_Minute(visitDate, (int)(-aheadBookTime));
			if (currDate.before(countDate)){
				isBefore = true;
			}
		}
		
		return isBefore;
	}
	
	/**
	 * 根据OrdOrderStock列表中的各个OrdOrderStock状态，设置订单子项资源状态
	 * 
	 * @param orderItem
	 * @param orderStockList
	 */
	private void setOrderItemResourceStatusByOrderStockList(OrdOrderItem orderItem, List<OrdOrderStock> orderStockList) {
		if (orderItem != null && orderStockList != null) {
			for (OrdOrderStock orderStock : orderStockList) {
				//设置订单子项是否需要资源确认逻辑
				setOrderItemsNeedResourceConfirm(orderStock.getNeedResourceConfirm(), orderItem);
				// 设置订单子项资源状态逻辑
				setOrderItemResourceStatus(orderStock.getResourceStatus(), orderItem);
			}
		}
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
				setOrderItemResourceStatusByOrderStockList(orderItem, hotelTimeRate.getOrderStockList());
			}
		}
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
	/**
	private String checkTimePriceTable(TimePrice timePrice, OrdOrderItem orderItem, OrdOrderDTO ordOrderDTO, String stockObjectType, Date visitTime, List<OrdOrderStock> orderStockList) {
		String errorMsg = null;
		//订单本地库存记录
		OrdOrderStock ordOrderStock = null;
		//是否需要资源确认
		String needResourceConfirm = null;
		//资源状态
		String resourceStatus = null;
		
		if ("Y".equalsIgnoreCase(timePrice.getOnsaleFlag())) {
			//判断下单时间是否在提前预定时间之前
			if (currentTimeIsBeforeAheadBookTime(orderItem.getVisitTime(), ordOrderDTO.getCreateTime(), timePrice.getAheadBookTime())) {
				//Free Sale
				if ("Y".equalsIgnoreCase(timePrice.getFreeSaleFlag())) {
					//不需要资源确认
					needResourceConfirm = "false";
					//资源审核通过
					resourceStatus = OrderEnum.RESOURCE_STATUS.AMPLE.name();
					//添加一条订单库存记录
					ordOrderStock = makeOrdOrderStockRecord(stockObjectType, visitTime, orderItem.getQuantity(), OrderEnum.INVENTORY_STATUS.FREESALE.name(), needResourceConfirm, resourceStatus);
					orderStockList.add(ordOrderStock);
				//保留房
				} else if ("Y".equalsIgnoreCase(timePrice.getStockFlag())) {
					//判断是否过了保留房的保留时间
					if (currentTimeIsBeforeLastHoldTime(orderItem.getVisitTime(), ordOrderDTO.getCreateTime(), timePrice.getLatestHoldTime())) {
						//不需要资源确认
						needResourceConfirm = "false";
						//资源审核通过
						resourceStatus = OrderEnum.RESOURCE_STATUS.AMPLE.name();
					} else {
						//需要资源确认
						needResourceConfirm = "true";
						//资源不满足
						resourceStatus = OrderEnum.RESOURCE_STATUS.UNVERIFIED.name();
					}
					// 库存充足
					if (timePrice.getStock() >= orderItem.getQuantity()) {
						// 添加一条订单库存记录
						ordOrderStock = makeOrdOrderStockRecord(stockObjectType, visitTime, orderItem.getQuantity(), OrderEnum.INVENTORY_STATUS.INVENTORY.name(), needResourceConfirm, resourceStatus);
						orderStockList.add(ordOrderStock);
						
						//将减少的库存添加到缓存映射表中，方便后续更新库存
						ordOrderDTO.addUpdateStockCount(timePrice.getTimePriceId(), orderItem.getQuantity());
					} else {
						// 如果是超卖
						if (timePrice.hasOvershellFlag()) {
							//无剩余库存
							if (timePrice.getStock() <= 0) {
								//需要资源确认
								needResourceConfirm = "true";
								//需要资源审核
								resourceStatus = OrderEnum.RESOURCE_STATUS.UNVERIFIED.name();
								
								//添加一条订单库存记录
								ordOrderStock = makeOrdOrderStockRecord(stockObjectType, visitTime, orderItem.getQuantity(), OrderEnum.INVENTORY_STATUS.UNINVENTORY.name(), needResourceConfirm, resourceStatus);
								orderStockList.add(ordOrderStock);
							} else {
								// 添加一条有库存的订单库存记录，记录的资源状态从时间价格表判断得来
								ordOrderStock = makeOrdOrderStockRecord(stockObjectType, visitTime, timePrice.getStock(), OrderEnum.INVENTORY_STATUS.INVENTORY.name(), needResourceConfirm, resourceStatus);
								orderStockList.add(ordOrderStock);
								
								// 添加一条无库存的订单库存记录,记录的资源状态重新设定
								Long left = orderItem.getQuantity() - timePrice.getStock();
								//需要资源确认
								needResourceConfirm = "true";
								//需要资源审核
								resourceStatus = OrderEnum.RESOURCE_STATUS.UNVERIFIED.name();
								ordOrderStock = makeOrdOrderStockRecord(stockObjectType, visitTime, left, OrderEnum.INVENTORY_STATUS.UNINVENTORY.name(), needResourceConfirm, resourceStatus);
								orderStockList.add(ordOrderStock);
								
								//将减少的库存添加到缓存映射表中，方便后续更新库存
								ordOrderDTO.addUpdateStockCount(timePrice.getTimePriceId(), timePrice.getStock());
							}
						} else {
							errorMsg = "您订购的商品库存已不足。";
						}
					}
				//非保留房（随便卖，无库存概念，资源要确认）
				} else if ("N".equalsIgnoreCase(timePrice.getStockFlag())){
					//需要资源确认
					needResourceConfirm = "true";
					//需要资源审核
					resourceStatus = OrderEnum.RESOURCE_STATUS.UNVERIFIED.name();
					//添加一条订单库存记录
					ordOrderStock = makeOrdOrderStockRecord(stockObjectType, visitTime, orderItem.getQuantity(), OrderEnum.INVENTORY_STATUS.UNINVENTORY.name(), needResourceConfirm, resourceStatus);
					orderStockList.add(ordOrderStock);
				} else {
					errorMsg = "您的订单中存在无库存的商品。";
				}
			} else {
				errorMsg = "您下单时间已经过了提前预定时间。";
			}
		} else {
			errorMsg = "您的订单中存在不可售商品。";
		}
		
		return errorMsg;
	}
	**/
	
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
//								ordOrderDTO.addUpdateStockCount(timePrice, stockReduce.getStock());
							} else {
								//无库存下单
								inventory = OrderEnum.INVENTORY_STATUS.UNINVENTORY.name();
							}
							//FREESALE的强制为FREESALE下单
							if (stockReduce.getReduceType() == SuppGoodsTimePrice.REDUCETYPE.FREESALE) {
								inventory = OrderEnum.INVENTORY_STATUS.FREESALE.name();
							}
							
							//添加一条订单库存记录
							ordOrderStock = makeOrdOrderStockRecord(stockObjectType, visitTime, stockReduce.getStock(), inventory, needResourceConfirm, resourceStatus);
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
	 * 使用时间价格表数据填充订单
	 * 
	 * @param timePrice
	 * @param orderItem
	 * @param ordOrderDTO
	 */
	private void fillOrderWithTimePrice(TimePrice timePrice, OrdOrderDTO ordOrderDTO) {
		if ((timePrice != null) && (ordOrderDTO != null)) {
			// 最晚取消时间
			setOrderLastCancelTime(timePrice.getSpecDate(), timePrice.getLatestCancelTime(), ordOrderDTO);
//			// 支付等待时间（默认2小时）
//			Calendar calendar = Calendar.getInstance();
//			calendar.set(0, 0, 0, 2, 0, 0);
//			setOrderWaitPaymentTime(2*60, ordOrderDTO);
		}
	}
	
	/**
	 * 使用订单子项数据填充订单
	 * 
	 * @param orderItem
	 * @param ordOrderDTO
	 */
	private void fillOrderWithOrderItem(OrdOrderItem orderItem, OrdOrderDTO ordOrderDTO) {
		if (orderItem != null && ordOrderDTO != null) {
			//设置订单资源状态
			setOrderResourceStatus(orderItem.getResourceStatus(), ordOrderDTO);
			
			//订单应付金额累加
			long quantity = orderItem.getQuantity();
			Integer ownerQuantity = (Integer) orderItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.ownerQuantity.name());
			
			if (ownerQuantity != null) {
				quantity = quantity - ownerQuantity.intValue();
			}
			Long oneItemTotal = orderItem.getPrice() * quantity;
			accumulateOrderOughtAmount(oneItemTotal, ordOrderDTO);
			
			// 计算订单游玩时间
			setOrderVisitTime(orderItem, ordOrderDTO);
		}
	}

	/**
	 * 设置订单最晚取消时间为订单子项最早的取消时间
	 * 
	 * @param date
	 *            新取消时间
	 * @param ordOrderDTO
	 *            订单
	 */
	private void setOrderLastCancelTime(Date visitTime,Long longDate, OrdOrderDTO ordOrderDTO) {
		if (longDate != null) {
			Date date = DateUtils.addMinutes(visitTime, (int)-longDate);
			if (ordOrderDTO.getLastCancelTime() == null) {
				ordOrderDTO.setLastCancelTime(date);
			} else {
				if (ordOrderDTO.getLastCancelTime().after(date)) {
					ordOrderDTO.setLastCancelTime(date);
				}
			}
		}
	}
	
	/**
	 * 计算订单子项最晚无损取消时间
	 * 
	 * @param visitTime
	 * @param longDate
	 * @param orderItem
	 */
	private void setOrderItemLastCancelTime(TimePrice timePrice, OrdOrderItem orderItem) {
		if(timePrice.getLatestCancelTime()!=null){
			Date date = DateUtils.addMinutes(timePrice.getSpecDate(), -timePrice.getLatestCancelTime().intValue());
			if (orderItem.getLastCancelTime() == null) {
				orderItem.setLastCancelTime(date);
			} else {
				if (orderItem.getLastCancelTime().after(date)) {
					orderItem.setLastCancelTime(date);
				}
			}
		}
		if(timePrice.getAheadBookTime()!=null){
			Date aheadTime = DateUtils.addMinutes(timePrice.getSpecDate(),-timePrice.getAheadBookTime().intValue());
			if(orderItem.getAheadTime() == null){
				orderItem.setAheadTime(aheadTime);
			}else if(orderItem.getAheadTime().after(aheadTime)){
				orderItem.setAheadTime(aheadTime);
			}
		}
	}

	/**
	 * 设置订单支付等待为订单子项最早的支付等待时间
	 * 
	 * @param date
	 *            新取消时间
	 * @param ordOrderDTO
	 *            订单
	 */
	private void setOrderWaitPaymentTime(int  minute, OrdOrderDTO ordOrderDTO) {
		if(minute>0){
			if (ordOrderDTO.getWaitPaymentTimeSec() == 0) {
				ordOrderDTO.setWaitPaymentTimeSec(minute);
			} else {
				ordOrderDTO.setWaitPaymentTimeSec(Math.min(minute,
						ordOrderDTO.getWaitPaymentTimeSec()));
			}
		}
	}

	/**
	 * 构造一个OrdOrderStock实例
	 * 
	 * @param objectType
	 * @param visitTime
	 * @param quantity
	 * @param inventory
	 * @param resourceConfirm
	 * @param resourceStatus
	 * @return
	 */
	private OrdOrderStock makeOrdOrderStockRecord(String objectType, Date visitTime, Long quantity, String inventory, String resourceConfirm, String resourceStatus) {
		OrdOrderStock ordOrderStock = new OrdOrderStock();
		ordOrderStock.setObjectType(objectType);
		ordOrderStock.setVisitTime(visitTime);
		ordOrderStock.setQuantity(quantity);
		ordOrderStock.setInventory(inventory);
		ordOrderStock.setNeedResourceConfirm(resourceConfirm);
		ordOrderStock.setResourceStatus(resourceStatus);

		return ordOrderStock;
	}

	/**
	 * 构造一个OrdOrderHotelTimeRate实例
	 * 
	 * @param visitTime
	 * @param quantity
	 * @param price
	 * @param settlementPrice
	 * @param marketPrice
	 * @param breakfastTicket
	 * @return
	 */
	private OrdOrderHotelTimeRate makeOrdOrderHotelTimeRateRecord(Date visitTime, Long quantity, Long price, Long settlementPrice, Long marketPrice, Long breakfastTicket) {
		OrdOrderHotelTimeRate ordOrderHotelTimeRate = new OrdOrderHotelTimeRate();
		ordOrderHotelTimeRate.setVisitTime(visitTime);
		ordOrderHotelTimeRate.setQuantity(quantity);
		ordOrderHotelTimeRate.setPrice(price);
		ordOrderHotelTimeRate.setSettlementPrice(settlementPrice);
		ordOrderHotelTimeRate.setMarketPrice(marketPrice);
		ordOrderHotelTimeRate.setBreakfastTicket(breakfastTicket);

		return ordOrderHotelTimeRate;
	}

	/**
	 * 设置订单资源状态
	 * 
	 * @param resourceStatus
	 * @param ordOrder
	 */
	private void setOrderResourceStatus(String resourceStatus, OrdOrderDTO ordOrder) {
		if (ordOrder != null) {
			if (ordOrder.getResourceStatus() == null) {
				ordOrder.setResourceStatus(resourceStatus);
			} else {
				OrderEnum.RESOURCE_STATUS statusEnum = OrderEnum.RESOURCE_STATUS.valueOf(resourceStatus);
				OrderEnum.RESOURCE_STATUS orderResourceStatus = null;

				/**
				 * LOCK级别最高，然后是UNVERIFIED RESOURCEPASS AMPLE。
				 */
				switch (statusEnum) {
				case LOCK:
					ordOrder.setResourceStatus(resourceStatus);
					break;

				case UNVERIFIED:
					orderResourceStatus = OrderEnum.RESOURCE_STATUS.valueOf(ordOrder.getResourceStatus());

					if (orderResourceStatus != OrderEnum.RESOURCE_STATUS.LOCK) {
						ordOrder.setResourceStatus(resourceStatus);
					}
					break;
					
				//斌哥说没有这个状态了 20131125
//				case RESOURCEPASS:
//					orderResourceStatus = OrderEnum.RESOURCE_STATUS.valueOf(ordOrder.getResourceStatus());
//
//					if ((orderResourceStatus != OrderEnum.RESOURCE_STATUS.LOCK) && (orderResourceStatus != OrderEnum.RESOURCE_STATUS.UNVERIFIED)) {
//						ordOrder.setResourceStatus(resourceStatus);
//					}
//					break;

				case AMPLE:
					orderResourceStatus = OrderEnum.RESOURCE_STATUS.valueOf(ordOrder.getResourceStatus());

					if ((orderResourceStatus != OrderEnum.RESOURCE_STATUS.LOCK) && (orderResourceStatus != OrderEnum.RESOURCE_STATUS.UNVERIFIED)
							&& (orderResourceStatus != OrderEnum.RESOURCE_STATUS.AMPLE)) {
						ordOrder.setResourceStatus(resourceStatus);
					}
					break;
				}
			}

		}
	}

	/**
	 * 设置订单那子项是否需要资源确认
	 * 
	 * @param needResourceConfirm
	 * @param orderItem
	 */
	private void setOrderItemsNeedResourceConfirm(String needResourceConfirm, OrdOrderItem orderItem) {
		if (orderItem != null) {
			if (orderItem.getNeedResourceConfirm() == null) {
				orderItem.setNeedResourceConfirm(needResourceConfirm);
			} else if (!"true".equals(orderItem.getNeedResourceConfirm())) {
				orderItem.setNeedResourceConfirm(needResourceConfirm);
			}
		}
	}

	/**
	 * 设置订单子项资源状态
	 * 
	 * @param resourceStatus
	 * @param orderItem
	 */
	private void setOrderItemResourceStatus(String resourceStatus, OrdOrderItem orderItem) {
		if (orderItem != null) {
			//如果从未设置过，则设置后退出方法
			if (orderItem.getResourceStatus() == null) {
				orderItem.setResourceStatus(resourceStatus);
			} else {
				OrderEnum.RESOURCE_STATUS statusEnum = OrderEnum.RESOURCE_STATUS.valueOf(resourceStatus);
				OrderEnum.RESOURCE_STATUS orderResourceStatus = null;

				/**
				 * LOCK级别最高，然后是UNVERIFIED RESOURCEPASS AMPLE。
				 */
				switch (statusEnum) {
				case LOCK:
					orderItem.setResourceStatus(resourceStatus);
					break;

				case UNVERIFIED:
					orderResourceStatus = OrderEnum.RESOURCE_STATUS.valueOf(orderItem.getResourceStatus());

					if (orderResourceStatus != OrderEnum.RESOURCE_STATUS.LOCK) {
						orderItem.setResourceStatus(resourceStatus);
					}
					break;

//				case RESOURCEPASS:
//					orderResourceStatus = OrderEnum.RESOURCE_STATUS.valueOf(orderItem.getResourceStatus());
//
//					if ((orderResourceStatus != OrderEnum.RESOURCE_STATUS.LOCK) && (orderResourceStatus != OrderEnum.RESOURCE_STATUS.UNVERIFIED)) {
//						orderItem.setResourceStatus(resourceStatus);
//					}
//					break;

				case AMPLE:
					orderResourceStatus = OrderEnum.RESOURCE_STATUS.valueOf(orderItem.getResourceStatus());

					if ((orderResourceStatus != OrderEnum.RESOURCE_STATUS.LOCK) && (orderResourceStatus != OrderEnum.RESOURCE_STATUS.UNVERIFIED)
							&& (orderResourceStatus != OrderEnum.RESOURCE_STATUS.AMPLE)) {
						orderItem.setResourceStatus(resourceStatus);
					}
					break;
				}
			}
		}
	}

	/**
	 * 订单金额累加计算
	 * 
	 * @param amount
	 *            累加金额
	 * @param order
	 *            订单
	 */
	private void accumulateOrderOughtAmount(Long amount, OrdOrderDTO order) {
		if (order != null) {
			if (order.getOughtAmount() == null) {
				order.setOughtAmount(amount);
			} else {
				order.setOughtAmount(order.getOughtAmount() + amount);
			}
		}
	}

	/**
	 * 订单子项各个逻辑项累加
	 * 
	 * @param timePrice
	 * @param orderItem
	 */
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
			setOrderItemLastCancelTime(timePrice, orderItem);
		}
	}

	/**
	 * 初始化联系人游玩人
	 * 
	 * @param order
	 * @return
	 */
	private String InitOrderPerson(OrdOrderDTO order) {
		String errorMsg = null;

		if ((order != null) && (order.getBuyInfo() != null)) {
			List<OrdPerson> ordPersonList = new ArrayList<OrdPerson>();
			
			//下单人
			Person booker = order.getBuyInfo().getBooker();
			// 从vo的Person对象转换成po的OrdPerson对象。
			OrdPerson ordPerson = getOrdPersonFromPerson(booker);

			if (ordPerson != null) {
				// 设置下单人与订单关联
				ordPerson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
				// 设置下单人类型。
				ordPerson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.BOOKER.name());
				ordPersonList.add(ordPerson);
			}

			//联系人
			Person contact = order.getBuyInfo().getContact();
			// 从vo的Person对象转换成po的OrdPerson对象。
			ordPerson = getOrdPersonFromPerson(contact);

			if (ordPerson != null) {
				// 设置联系人与订单关联
				ordPerson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
				// 设置联系人类型。
				ordPerson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
				ordPersonList.add(ordPerson);
			}
			
			//快递联系人			
			BuyInfo.Expressage express=order.getBuyInfo().getExpressage();
			ordPerson=this.getOrdAddressFromExpresss(express);
			if(ordPerson!=null){
				// 设置联系人与订单关联
				ordPerson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
				// 设置联系人类型。
				ordPerson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.ADDRESS.name());				
				ordPersonList.add(ordPerson);
			}
			
			//紧急联系人
			Person emergencyPerson = order.getBuyInfo().getEmergencyPerson();
			// 从vo的Person对象转换成po的OrdPerson对象。
			ordPerson = getOrdPersonFromPerson(emergencyPerson);

			if (ordPerson != null) {
				// 设置紧急联系人与订单关联
				ordPerson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
				// 设置紧急联系人类型。
				ordPerson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name());
				ordPersonList.add(ordPerson);
			}
			
			//游玩人
			List<Person> personList = order.getBuyInfo().getTravellers();
			if (personList != null && !personList.isEmpty()) {
				List<OrdPerson> ordTravellerList = new ArrayList<OrdPerson>();
				for (Person traveller : personList) {
					// 从vo的Person对象转换成po的OrdPerson对象。
					ordPerson = getOrdPersonFromPerson(traveller);

					if (ordPerson != null) {
						// 设置游玩人与订单关联
						ordPerson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
						// 设置游玩人类型。
						ordPerson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
						ordPersonList.add(ordPerson);
						ordTravellerList.add(ordPerson);
					}
				}
				
				order.setOrdTravellerList(ordTravellerList);
			}

			order.setOrdPersonList(ordPersonList);
			
		} else {
			errorMsg = "您还没有下单。";
		}

		return errorMsg;
	}
	
	/**
	 * 计算订单的游玩时间
	 * 
	 * @param orderItem
	 * @param ordOrderDTO
	 */
	private void setOrderVisitTime(OrdOrderItem orderItem, OrdOrderDTO ordOrderDTO) {
		if (orderItem != null && ordOrderDTO != null && orderItem.getVisitTime() != null) {
			if (ordOrderDTO.getVisitTime() == null) {
				ordOrderDTO.setVisitTime(orderItem.getVisitTime());
			} else {
				if (ordOrderDTO.getVisitTime().after(orderItem.getVisitTime())) {
					ordOrderDTO.setVisitTime(orderItem.getVisitTime());
				}
			}
		}
	}

	/**
	 * vo中的Person对象转换成po中的OrdPerson对象。
	 * 
	 * @param person
	 * @return
	 */
	private OrdPerson getOrdPersonFromPerson(Person person) {
		OrdPerson ordPerson = null;

		if (person != null) {
			ordPerson = new OrdPerson();
			ordPerson.setEmail(person.getEmail());
			ordPerson.setFax(person.getFax());
			ordPerson.setFirstName(person.getFirstName());

			ordPerson.setGender(person.getGender());

			ordPerson.setLastName(person.getLastName());
			ordPerson.setMobile(person.getMobile());
			ordPerson.setNationality(person.getNationality());
			ordPerson.setPhone(person.getPhone());
			ordPerson.setIdNo(person.getIdNo());
			ordPerson.setIdType(person.getIdType());
			
			ordPerson.setPeopleType(person.getPeopleType());
			if(person.getBirthday() != null && !"".equals(person.getBirthday().trim())) {
				ordPerson.setBirthday(DateUtil.toDate(person.getBirthday().trim(), "yyyy-MM-dd"));
			}
			

			// 设置全名
			boolean isChineseName = false;
			String lastName = ordPerson.getLastName();
			String firstName = ordPerson.getFirstName();
			if (lastName != null) {
				if (StringUtil.hasChinese(lastName)) {
					isChineseName = true;
				}
			}

			if (!isChineseName && (firstName != null)) {
				if (StringUtil.hasChinese(firstName)) {
					isChineseName = true;
				}
			}
			ordPerson.setFullName(person.getFullName());
			if(StringUtils.isEmpty(person.getFullName())){
				if (isChineseName) {
					StringBuffer sb=new StringBuffer();
					if(StringUtils.isNotEmpty(lastName)){
						sb.append(lastName);
					}
					if(StringUtils.isNotEmpty(firstName)){
						sb.append(firstName);
					}
					ordPerson.setFullName(sb.toString());
				} else {
					StringBuffer sb=new StringBuffer();
					
					if(StringUtils.isNotEmpty(firstName)){
						sb.append(firstName);
					}
					if(StringUtils.isNotEmpty(lastName)){
						if(sb.length()>1){
							sb.append("/");
						}
						sb.append(lastName);
					}
					ordPerson.setFullName(sb.toString());
				}
			}

		}

		return ordPerson;
	}
	
	private OrdPerson getOrdAddressFromExpresss(BuyInfo.Expressage express){
		OrdPerson ordPerson=null;
		OrdAddress ordAddress=null;
		if(express!=null&&StringUtils.isNotEmpty(express.getRecipients())){
			ordAddress=new OrdAddress();
			ordAddress.setCity(express.getCityName());
			ordAddress.setProvince(express.getProvinceName());
			ordAddress.setPostalCode(express.getPostcode()+"");
			ordAddress.setStreet(express.getAddress());
			
			ordPerson=new OrdPerson();
			ordPerson.setFullName(express.getRecipients());
			ordPerson.setMobile(express.getContactNumber());
			List<OrdAddress> addressList=new ArrayList<OrdAddress>(1);
			addressList.add(ordAddress);
			ordPerson.setAddressList(addressList);			
		}
		return ordPerson;
	}

	/**
	 * 初始化担保信用卡信息(验证信用卡)
	 * 
	 * @param order
	 * @return
	 */
	/**
	private String InitGuaranteeCreditCard(OrdOrderDTO order) {
		String errorMsg = null;

		if ((order != null) && (order.getBuyInfo() != null)) {
			errorMsg = checkHasGuaranteeCreditCard(order);
			if (errorMsg == null) {
				// 构建信用卡信息
				if (OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name().equals(order.getBuyInfo().getNeedGuarantee())) {
					GuaranteeCreditCard guaranteeCreditCard = order.getBuyInfo().getGuarantee();
					if (guaranteeCreditCard != null && guaranteeCreditCard.getCardNo() != null) {
						ResultHandleT<SuppCreditCardValidate> resultHandle = suppCommonClientService.creditCardValidate(guaranteeCreditCard.getCardNo());
						if (resultHandle.isSuccess()) {
							SuppCreditCardValidate validate = resultHandle.getReturnContent();
							if (validate != null) {
								if (validate.isValid()) {
									if (validate.isNeedVerifyCode()) {
										if (guaranteeCreditCard.getCvv() == null || guaranteeCreditCard.getCvv().trim() == "") {
											errorMsg = "请提供CVV码。";
										}
										
										if (errorMsg == null) {
											OrdGuaranteeCreditCard ordCard = makeGuaranteeCreditCard(guaranteeCreditCard, order.getActualAmount());

											if (ordCard != null) {
												//计算担保金额
												ordCard.setGuaranteeAmount(computeOrderGuaranteeTotalAmount(order));
												
												List<OrdGuaranteeCreditCard> cardList = new ArrayList<OrdGuaranteeCreditCard>();
												ordCard.setGuaranteeAmount(order.getOughtAmount());
												cardList.add(ordCard);
												order.setOrdGuaranteeCreditCardList(cardList);
											}
										}
									}
								} else {
									errorMsg = "信用卡无效。";
								}
							} else {
								errorMsg = "信用卡无法验证。";
								LOG.debug("method:InitGuaranteeCreditCard,msg=creditCardValidate method return null content.");
							}
						} else {
							errorMsg = "信用卡验证失败。";
							LOG.debug("method:InitGuaranteeCreditCard,msg=" + resultHandle.getMsg());
						}
					} else {
						errorMsg = "请填写信用卡号码等信息。";
					}
				}
			}
		} else {
			errorMsg = "您还没有下单。";
		}

		return errorMsg;
	}
	**/
	
	/**
	 * 初始化担保信用卡信息
	 * 
	 * @param order
	 * @return
	 */
	private String InitGuaranteeCreditCard(OrdOrderDTO order) {
		String errorMsg = null;
		if ((order != null) && (order.getBuyInfo() != null)) {
			if (errorMsg == null) {
				// 构建信用卡信息
				if (OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name().equals(order.getBuyInfo().getNeedGuarantee())) {
					GuaranteeCreditCard guaranteeCreditCard = order.getBuyInfo().getGuarantee();
					if (guaranteeCreditCard != null && guaranteeCreditCard.getCardNo() != null) {
						OrdGuaranteeCreditCard ordCard = makeGuaranteeCreditCard(guaranteeCreditCard, order.getActualAmount());
						if (ordCard != null) {
							//计算担保金额
							ordCard.setGuaranteeAmount(computeOrderGuaranteeTotalAmount(order));
							List<OrdGuaranteeCreditCard> cardList = new ArrayList<OrdGuaranteeCreditCard>();
							ordCard.setGuaranteeAmount(order.getOughtAmount());
							cardList.add(ordCard);
							order.setOrdGuaranteeCreditCardList(cardList);
						}
					} else {
						errorMsg = "请填写信用卡信息。";
					}
				}
			}
		} else {
			errorMsg = "您还没有下单。";
		}

		return errorMsg;
	}
	
	/**
	 * 担保、预授权，对信用卡检验
	 * 
	 * @param order
	 * @return
	 */
	private String checkHasGuaranteeCreditCard(OrdOrderDTO order) {
		String errorMsg = null;
		//现付
		if (order.hasNeedPay()) {
			//订单需要担保
			if (order.getBookLimitType() != null
					&& !order.getBookLimitType().equalsIgnoreCase(OrderEnum.GUARANTEE_TYPE.NONE.name())) {
				if (!OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name().equals(order.getBuyInfo().getNeedGuarantee())) {
					errorMsg = "此订单需担保，请填写信用卡信息。";
				}
			}
		//预付
		} else if (order.hasNeedPrepaid()) {
			/**
			 * 预授权的走支付渠道
			//订单需要预授权
			if (order.getPaymentType() != null
					&& !order.getPaymentType().equalsIgnoreCase(SuppGoodsTimePrice.BOOKLIMITTYPE.NONE.name())) {
				if (!OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name().equals(order.getBuyInfo().getNeedGuarantee())) {
					errorMsg = "此订单需预授权，请填写信用卡信息。";
				}
			}
			**/
		}
		
		return errorMsg;
	}
	
	/**
	 * 计算订单担保金额
	 * 
	 * @param order
	 * @return
	 */
	private long computeOrderGuaranteeTotalAmount(OrdOrderDTO order) {
		long totalAmount = 0;
		if (order.getOrderItemList() != null) {
			for (OrdOrderItem orderItem : order.getOrderItemList()) {
				if (orderItem != null) {
					if (orderItem.getBookLimitType() != null && !orderItem.getBookLimitType().equalsIgnoreCase(OrderEnum.GUARANTEE_TYPE.NONE.name())) {
						totalAmount = totalAmount + orderItem.getDeductAmount();
					}
				}
			}
		}
		
		return totalAmount;
	}

	/**
	 * 构造担保信用卡信息
	 * 
	 * @param vCard
	 * @param guaranteeAmount
	 * @return
	 */
	private OrdGuaranteeCreditCard makeGuaranteeCreditCard(GuaranteeCreditCard vCard, Long guaranteeAmount) {
		OrdGuaranteeCreditCard ordCard = null;

		if (vCard != null) {
			ordCard = new OrdGuaranteeCreditCard();

			ordCard.setCvv(vCard.getCvv());
			ordCard.setExpirationMonth(vCard.getExpirationMonth());
			ordCard.setExpirationYear(vCard.getExpirationYear());
			ordCard.setGuaranteeAmount(guaranteeAmount);
			ordCard.setHolderName(vCard.getHolderName());
			ordCard.setIdNo(vCard.getIdNo());
			ordCard.setIdType(vCard.getIdType());
			ordCard.setCardNo(vCard.getCardNo());
		}

		return ordCard;
	}

	/**
	 * 初始化订单发票信息
	 * 
	 * @param orde
	 * @return
	 */
	private String initInvoice(OrdOrderDTO orde) {
		String errorMsg = null;

		return errorMsg;
	}
	
	/**
	 * 初始化金额转换表
	 * 
	 * @param orde
	 * @return
	 */
	private String initAmountItem(OrdOrderDTO order) {
		String errorMsg = null;
		if (order != null) {
			List<OrdOrderItem> orderItemList = order.getOrderItemList();
			if (orderItemList != null && orderItemList.size() > 0) {
				long totalPrice = 0;
				long totalSettlementPrice = 0;
				for (OrdOrderItem item : orderItemList) {
					if (item != null) {
						totalPrice = totalPrice + item.getPrice() * item.getQuantity();
						totalSettlementPrice = totalSettlementPrice + item.getTotalSettlementPrice();
					}
				}
				//更新订单金额表
				List<OrdOrderAmountItem> amountItemList = order.getOrderAmountItemList();
				if (amountItemList == null) {
					amountItemList = new ArrayList<OrdOrderAmountItem>();
					order.setOrderAmountItemList(amountItemList);
				}
				//订单售价金额子项
				if (totalPrice != 0) {
					OrdOrderAmountItem amountItem = makeOrdOrderAmountItem(totalPrice, OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_PRICE.name(), OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_ORDER.name());
					amountItemList.add(amountItem);
				}
				//订单结算价金额子项
				if (totalSettlementPrice != 0) {
					OrdOrderAmountItem amountItem = makeOrdOrderAmountItem(totalSettlementPrice, OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_SETTLEPRICE.name(), OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_ORDER.name());
					amountItemList.add(amountItem);
				}
			} else {
				errorMsg = "您还没有选购商品。";
			}
		} else {
			errorMsg = "您还没有下单。";
		}
		return errorMsg;
	}
	
	/**
	 * 构造OrdOrderAmountItem对象
	 * 
	 * @param amount
	 * @param amountType
	 * @param name
	 * @return
	 */
	private OrdOrderAmountItem makeOrdOrderAmountItem(Long amount, String amountType, String name) {
		OrdOrderAmountItem orderAmountItem = new OrdOrderAmountItem();
		orderAmountItem.setItemAmount(amount);
		orderAmountItem.setOrderAmountType(amountType);
		orderAmountItem.setItemName(name);
		return orderAmountItem;
	}
}
