package com.lvmama.vst.order.web.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.lvmama.bridge.utils.date.DateUtil;
import com.lvmama.order.api.base.vo.RequestBody;
import com.lvmama.order.api.base.vo.ResponseBody;
import com.lvmama.order.search.api.comm.IApiLvoOrderService;
import com.lvmama.order.search.vo.LvoAdditionStatusVo;
import com.lvmama.order.search.vo.LvoHotelTimeRateVo;
import com.lvmama.order.search.vo.LvoItemVo;
import com.lvmama.order.search.vo.LvoOrderVo;
import com.lvmama.order.search.vo.LvoPackVo;
import com.lvmama.order.search.vo.LvoPersonVo;
import com.lvmama.order.search.vo.LvoTravelContractVo;
import com.lvmama.order.search.vo.PageVo;
import com.lvmama.order.search.vo.param.LvoOrderOrderParamVo;
import com.lvmama.order.search.vo.param.LvoOrderParamVo;
import com.lvmama.order.search.vo.param.LvoPageParamVo;
import com.lvmama.order.search.vo.param.LvoPersonParamVo;
import com.lvmama.order.search.vo.param.LvoProductParamVo;
import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.vo.order.OrderMonitorCnd;
import com.lvmama.vst.order.web.service.OrderESQueryService;

@Service
public class OrderESQueryServiceImpl implements OrderESQueryService {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderESQueryServiceImpl.class);
	
	@Resource(name = "apiLvoOrderService")
	private IApiLvoOrderService apiLvoOrderService;


	@Override
	public PageVo<LvoOrderVo> queryOrderListFromES(Integer page, Integer pageSize, OrderMonitorCnd monitorCnd) {
		LvoOrderParamVo orderParamVo = new LvoOrderParamVo();
		buildLvoOrderParamVo(page, pageSize, monitorCnd, orderParamVo);
		ResponseBody<PageVo<LvoOrderVo>> responseBody = apiLvoOrderService.listOrder(new RequestBody<LvoOrderParamVo>(orderParamVo));
		if (responseBody != null && responseBody.isSuccess()) {
			return responseBody.getT();
		}
		logger.warn("queryOrderListFromES query failed, OrderMonitorCnd:{}, LvoOrderParamVo:{}", JSONObject.fromObject(monitorCnd),
				JSONObject.fromObject(orderParamVo));
		return null;
	}

	@Override
	public void saveOrderList(List<OrdOrder> orderList) {
		for (OrdOrder ordOrder : orderList) {
			LvoOrderVo orderVo = new LvoOrderVo();
			this.copy(ordOrder, orderVo);
			
			apiLvoOrderService.saveOrder(new RequestBody<LvoOrderVo>(orderVo));
		}
	}

	@Override
	public List<OrdOrder> copyList(List<LvoOrderVo> lvoOrderVoList) {
		List<OrdOrder> orderList = new ArrayList<OrdOrder>();
		for (LvoOrderVo lvoOrderVo : lvoOrderVoList) {
			OrdOrder ordOrder = new OrdOrder();
			try {
				EnhanceBeanUtils.copyProperties(lvoOrderVo, ordOrder);
				if (CollectionUtils.isNotEmpty(lvoOrderVo.getItemList())) {
					List<LvoItemVo> lvoOrderItemList = lvoOrderVo.getItemList();
					List<OrdOrderItem> orderItemList = new ArrayList<OrdOrderItem>();
					for (LvoItemVo lvoItemVo : lvoOrderItemList) {
						OrdOrderItem orderItem = new OrdOrderItem();
						EnhanceBeanUtils.copyProperties(lvoItemVo, orderItem);
						List<LvoHotelTimeRateVo> lvoHotelTimeRateVoList = lvoItemVo.getHotelTimeRateList();
						List<OrdOrderHotelTimeRate> ordOrderHotelTimeRateList = 
								EnhanceBeanUtils.copyList(lvoHotelTimeRateVoList, OrdOrderHotelTimeRate.class);
						orderItem.setOrderHotelTimeRateList(ordOrderHotelTimeRateList);
						orderItemList.add(orderItem);
					}
					ordOrder.setOrderItemList(orderItemList);
				}
				if (CollectionUtils.isNotEmpty(lvoOrderVo.getAdditionStatusList())) {
					List<LvoAdditionStatusVo> lvoAdditionStatusList = lvoOrderVo.getAdditionStatusList();
					List<OrdAdditionStatus> ordAdditionStatusList = EnhanceBeanUtils.copyList(lvoAdditionStatusList, OrdAdditionStatus.class);
					ordOrder.setOrdAdditionStatusList(ordAdditionStatusList);
				}
				if (CollectionUtils.isNotEmpty(lvoOrderVo.getPackList())) {
					List<LvoPackVo> lvoPackList = lvoOrderVo.getPackList();
					List<OrdOrderPack> orderPackList = EnhanceBeanUtils.copyList(lvoPackList, OrdOrderPack.class);
					ordOrder.setOrderPackList(orderPackList);
				}
				if (CollectionUtils.isNotEmpty(lvoOrderVo.getPersonList())) {
					List<LvoPersonVo> lvoPersonList = lvoOrderVo.getPersonList();
					List<OrdPerson> ordPersonList = EnhanceBeanUtils.copyList(lvoPersonList, OrdPerson.class);
					ordOrder.setOrdPersonList(ordPersonList);
				}
				if (CollectionUtils.isNotEmpty(lvoOrderVo.getResponsibleList())) {
					// 目前OrdOrder不包含OrdResponsible,暂不作处理
//					List<LvoResponsibleVo> lvoResponsibleList = lvoOrderVo.getResponsibleList();
//					logger.warn("lvoResponsibleList:{}", JSONArray.fromObject(lvoResponsibleList));
//					for (LvoResponsibleVo responsibleVo : lvoResponsibleList) {
//					}
				}
				if (CollectionUtils.isNotEmpty(lvoOrderVo.getTravelContractList())) {
					List<LvoTravelContractVo> lvoTravelContractList = lvoOrderVo.getTravelContractList();
					List<OrdTravelContract> ordTravelContractList = EnhanceBeanUtils.copyList(lvoTravelContractList, OrdTravelContract.class);
					ordOrder.setOrdTravelContractList(ordTravelContractList);
				}
				orderList.add(ordOrder);
			} catch (Exception e) {
				logger.error("查询ES订单结果转换为订单集合异常, 异常信息:{}", e);
			}
		}
		return orderList;
	}

	private void copy(OrdOrder ordOrder, LvoOrderVo orderVo) {
		try {
			EnhanceBeanUtils.copyProperties(ordOrder, orderVo, null, "createTime", "lastCancelTime", "waitPaymentTime", "paymentTime", 
					"approveTime", "visitTime", "cancelTime", "resourceAmpleTime", "infoPassTime", "orderUpdateTime", "updateTime", 
					"endTime", "ticketLastConfirmTime");
			if (UtilityTool.isValid(ordOrder.getCreateTime())) {
				orderVo.setCreateTime(ordOrder.getCreateTime());
			}
			if (UtilityTool.isValid(ordOrder.getUpdateTime())) {
				orderVo.setUpdateTime(ordOrder.getUpdateTime());
			} 
			if (UtilityTool.isValid(ordOrder.getLastCancelTime())) {
				orderVo.setLastCancelTime(ordOrder.getLastCancelTime());
			}
			if (UtilityTool.isValid(ordOrder.getWaitPaymentTime())) {
				orderVo.setWaitPaymentTime(ordOrder.getWaitPaymentTime());
			}
			if (UtilityTool.isValid(ordOrder.getPaymentTime())) {
				orderVo.setPaymentTime(ordOrder.getPaymentTime());
			}
			if (UtilityTool.isValid(ordOrder.getApproveTime())) {
				orderVo.setApproveTime(ordOrder.getApproveTime());
			}
			if (UtilityTool.isValid(ordOrder.getVisitTime())) {
				orderVo.setVisitTime(ordOrder.getVisitTime());
			}
			if (UtilityTool.isValid(ordOrder.getCancelTime())) {
				orderVo.setCancelTime(ordOrder.getCancelTime());
			}
			if (UtilityTool.isValid(ordOrder.getResourceAmpleTime())) {
				orderVo.setResourceAmpleTime(ordOrder.getResourceAmpleTime());
			}
			if (UtilityTool.isValid(ordOrder.getInfoPassTime())) {
				orderVo.setInfoPassTime(ordOrder.getInfoPassTime());
			}
			if (UtilityTool.isValid(ordOrder.getOrderUpdateTime())) {
				orderVo.setOrderUpdateTime(ordOrder.getOrderUpdateTime());
			}
			if (UtilityTool.isValid(ordOrder.getEndTime())) {
				orderVo.setEndTime(ordOrder.getEndTime());
			}
			if (UtilityTool.isValid(ordOrder.getTicketLastConfirmTime())) {
				orderVo.setTicketLastConfirmTime(ordOrder.getTicketLastConfirmTime());
			}
			if (CollectionUtils.isNotEmpty(ordOrder.getOrderItemList())) {
				List<LvoItemVo> lvoOrderItemList = EnhanceBeanUtils.copyList(ordOrder.getOrderItemList(), LvoItemVo.class, 
						"resourceAmpleTime", "infoPassTime", "createTime", "orderUpdateTime", "updateTime", "lastAheadTime", 
						"visitTime", "lastCancelTime");
				List<OrdOrderItem> orderItemList = ordOrder.getOrderItemList();
				Map<Long, OrdOrderItem> map = new HashMap<Long, OrdOrderItem>();
				for (OrdOrderItem item : orderItemList) {
					map.put(item.getOrderItemId(), item);
				}
				for (LvoItemVo lvoItemVo : lvoOrderItemList) {
					OrdOrderItem item = map.get(lvoItemVo.getOrderItemId());
					if (UtilityTool.isValid(item.getResourceAmpleTime())) {
						lvoItemVo.setResourceAmpleTime(item.getResourceAmpleTime());
					}
					if (UtilityTool.isValid(item.getInfoPassTime())) {
						lvoItemVo.setInfoPassTime(item.getInfoPassTime());
					}
					if (UtilityTool.isValid(item.getCreateTime())) {
						lvoItemVo.setCreateTime(item.getCreateTime());
					}
					if (UtilityTool.isValid(item.getOrderUpdateTime())) {
						lvoItemVo.setOrderUpdateTime(item.getOrderUpdateTime());
					}
					if (UtilityTool.isValid(item.getUpdateTime())) {
						lvoItemVo.setUpdateTime(item.getUpdateTime());
					}
					if (UtilityTool.isValid(item.getLastAheadTime())) {
						lvoItemVo.setLastAheadTime(item.getLastAheadTime());
					}
					if (UtilityTool.isValid(item.getVisitTime())) {
						lvoItemVo.setVisitTime(item.getVisitTime());
					}
					if (UtilityTool.isValid(item.getLastCancelTime())) {
						lvoItemVo.setLastCancelTime(item.getLastCancelTime());
					}
				}
				orderVo.setItemList(lvoOrderItemList);
				
			}
			if (CollectionUtils.isNotEmpty(ordOrder.getOrdAdditionStatusList())) {
				List<LvoAdditionStatusVo> additionStatusList = EnhanceBeanUtils.copyList(ordOrder.getOrdAdditionStatusList(), LvoAdditionStatusVo.class,
						"updateTime");
				List<OrdAdditionStatus> ordAdditionStatusList = ordOrder.getOrdAdditionStatusList();
				Map<Long, OrdAdditionStatus> map = new HashMap<Long, OrdAdditionStatus>();
				for (OrdAdditionStatus ordAdditionStatus : ordAdditionStatusList) {
					map.put(ordAdditionStatus.getOrdAdditionStatusId(), ordAdditionStatus);
				}
				for (LvoAdditionStatusVo lvoAdditionStatusVo : additionStatusList) {
					OrdAdditionStatus status = map.get(lvoAdditionStatusVo.getOrdAdditionStatusId());
					if (UtilityTool.isValid(status.getUpdateTime())) {
						lvoAdditionStatusVo.setUpdateTime(status.getUpdateTime());
					}
				}
				orderVo.setAdditionStatusList(additionStatusList);
			}
			if (CollectionUtils.isNotEmpty(ordOrder.getOrderPackList())) {
				List<LvoPackVo> lvoPackList = EnhanceBeanUtils.copyList(ordOrder.getOrderPackList(), LvoPackVo.class, "createTime", "updateTime");
				List<OrdOrderPack> ordOrderPackList = ordOrder.getOrderPackList();
				Map<Long, OrdOrderPack> map = new HashMap<Long, OrdOrderPack>();
				for (OrdOrderPack ordOrderPack : ordOrderPackList) {
					map.put(ordOrderPack.getOrderPackId(), ordOrderPack);
				}
				for (LvoPackVo lvoPackVo : lvoPackList) {
					OrdOrderPack ordPack = map.get(lvoPackVo.getOrderPackId());
					if (UtilityTool.isValid(ordPack.getCreateTime())) {
						lvoPackVo.setCreateTime(ordPack.getCreateTime());
					}
					if (UtilityTool.isValid(ordPack.getUpdateTime())) {
						lvoPackVo.setUpdateTime(ordPack.getUpdateTime());
					}
				}
				orderVo.setPackList(lvoPackList);
			}
			if (CollectionUtils.isNotEmpty(ordOrder.getOrdPersonList())) {
				List<LvoPersonVo> lvoPersonList = EnhanceBeanUtils.copyList(ordOrder.getOrdPersonList(), LvoPersonVo.class, "expDate", "issueDate", "updateTime");
				List<OrdPerson> ordPersonList = ordOrder.getOrdPersonList();
				Map<Long, OrdPerson> map = new HashMap<Long, OrdPerson>();
				for (OrdPerson ordPerson : ordPersonList) {
					map.put(ordPerson.getOrdPersonId(), ordPerson);
				}
				for (LvoPersonVo lvoPersonVo : lvoPersonList) {
					OrdPerson ordPerson = map.get(lvoPersonVo.getOrdPersonId());
					if (UtilityTool.isValid(ordPerson.getExpDate())) {
						lvoPersonVo.setExpDate(ordPerson.getExpDate());
					}
					if (UtilityTool.isValid(ordPerson.getIssueDate())) {
						lvoPersonVo.setIssueDate(ordPerson.getIssueDate());
					}
					if (UtilityTool.isValid(ordPerson.getUpdateTime())) {
						lvoPersonVo.setUpdateTime(ordPerson.getUpdateTime());
					}
				}
				orderVo.setPersonList(lvoPersonList);
			}
			if (CollectionUtils.isNotEmpty(ordOrder.getOrdTravelContractList())) {
				List<LvoTravelContractVo> lvoTravelContractList = EnhanceBeanUtils.copyList(ordOrder.getOrdTravelContractList(), LvoTravelContractVo.class, "createTime", "updateTime");
				List<OrdTravelContract> ordTravelContractList = ordOrder.getOrdTravelContractList();
				Map<Long, OrdTravelContract> map = new HashMap<Long, OrdTravelContract>();
				for (OrdTravelContract ordTravelContract : ordTravelContractList) {
					map.put(ordTravelContract.getOrdContractId(), ordTravelContract);
				}
				for (LvoTravelContractVo lvoTravelContractVo : lvoTravelContractList) {
					OrdTravelContract ordTravelContract = map.get(lvoTravelContractVo.getOrdContractId());
					if (UtilityTool.isValid(ordTravelContract.getCreateTime())) {
						lvoTravelContractVo.setCreateTime(ordTravelContract.getCreateTime());
					}
					if (UtilityTool.isValid(ordTravelContract.getUpdateTime())) {
						lvoTravelContractVo.setUpdateTime(ordTravelContract.getUpdateTime());
					}
				}
				orderVo.setTravelContractList(lvoTravelContractList);
			}
			
		} catch (Exception e) {
			logger.error("保存到ES前copy时发生异常,异常信息:{}", e);
		}
		
	}

	/**
	 * 构建ES查询订单参数
	 * @param page
	 * @param pageSize
	 * @param monitorCnd
	 * @param orderParamVo
	 */
	private void buildLvoOrderParamVo(Integer page, Integer pageSize,
			OrderMonitorCnd monitorCnd, LvoOrderParamVo orderParamVo) {
		LvoOrderOrderParamVo orderParam = new LvoOrderOrderParamVo();
		buildOrderParam(monitorCnd, orderParam);
		orderParamVo.setOrderParam(orderParam);
		
		LvoProductParamVo productParam = new LvoProductParamVo();
		buildProductParam(monitorCnd, productParam);
		orderParamVo.setProductParam(productParam);
		
		List<LvoPersonParamVo> personParamList = new ArrayList<LvoPersonParamVo>();
		buildPersonParam(monitorCnd, personParamList);
		orderParamVo.setPersonParamList(personParamList);
		
		LvoPageParamVo pageParam = new LvoPageParamVo();
		pageParam.setPage(page);
		pageParam.setPageSize(pageSize);
		orderParamVo.setPageParam(pageParam);
	}
	
	/**
	 * 构建游玩人参数
	 * @param monitorCnd
	 * @param personParam
	 */
	private void buildPersonParam(OrderMonitorCnd monitorCnd, List<LvoPersonParamVo> personParamList) {
		if (UtilityTool.isValid(monitorCnd.getBookerName()) || UtilityTool.isValid(monitorCnd.getBookerMobile())) {// 驴妈妈账号 || 已绑定手机号
			LvoPersonParamVo personParam = new LvoPersonParamVo();
			personParam.setFullName(monitorCnd.getBookerName());
			personParam.setMobilePrefix(monitorCnd.getBookerMobile());
			personParam.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
			personParam.setPersonType(OrderEnum.ORDER_PERSON_TYPE.BOOKER.name());
			personParamList.add(personParam);
		}
		if (UtilityTool.isValid(monitorCnd.getTravellerName())) {//  出游人姓名
			LvoPersonParamVo personParam = new LvoPersonParamVo();
			personParam.setFullName(monitorCnd.getTravellerName());
			personParam.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
			personParam.setPersonType(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
			personParamList.add(personParam);
		}
		if (UtilityTool.isValid(monitorCnd.getContactName())) {// 联系人姓名
			LvoPersonParamVo personParam = new LvoPersonParamVo();
			personParam.setFullName(monitorCnd.getContactName());
			personParam.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
			personParam.setPersonType(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
			personParamList.add(personParam);
		}
		if (UtilityTool.isValid(monitorCnd.getContactEmail()) || UtilityTool.isValid(monitorCnd.getContactMobile())
				|| UtilityTool.isValid(monitorCnd.getContactPhone())) {// 联系人邮箱 || 联系人固话 || 联系人手机
			LvoPersonParamVo personParam = new LvoPersonParamVo();
			personParam.setEmailPrefix(monitorCnd.getContactEmail());
			personParam.setPhonePrefix(monitorCnd.getContactPhone());
			personParam.setMobilePrefix(monitorCnd.getContactMobile());
			personParam.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
			personParam.setPersonType(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
			personParamList.add(personParam);
		}
	}

	/**
	 * 构建产品参数
	 * @param monitorCnd
	 * @param productParam
	 */
	private void buildProductParam(OrderMonitorCnd monitorCnd, LvoProductParamVo productParam) {
		if (UtilityTool.isValid(monitorCnd.getProductId())) {// 产品编号
			productParam.setProductId(monitorCnd.getProductId());
		}
		if (UtilityTool.isValid(monitorCnd.getProductName())) {// 产品名称
			productParam.setProductNamePrefix(monitorCnd.getProductName());
		}
		if (UtilityTool.isValid(monitorCnd.getSuppGoodsName())) {// 商品名称
			productParam.setSuppGoodsNamePrefix(monitorCnd.getSuppGoodsName());
		}
		if (UtilityTool.isValid(monitorCnd.getSuppGoodsId())) {// 商品ID
			productParam.setSuppGoodsId(monitorCnd.getSuppGoodsId());
		}
		if (UtilityTool.isValid(monitorCnd.getManagerId())) {// 主订单产品经理
			productParam.setOrderManagerId(monitorCnd.getManagerId());
		}
		if (UtilityTool.isValid(monitorCnd.getItemManagerId())) {// 子订单产品经理
			productParam.setOrderItemManagerId(monitorCnd.getItemManagerId());
		}
		
		if (UtilityTool.isValid(monitorCnd.getBelongBU())) {// 所属BU
			String buCode = monitorCnd.getBelongBU();
			String[] arr = buCode.split("\\|");
			if(arr.length > 1) {
				List<String> buCodeList = new ArrayList<String>();
				for (int i=0; i< arr.length; i++) {
					buCodeList.add(arr[i]);
				}
				productParam.setBuCodeList(buCodeList);
			} else {
				productParam.setBuCode(monitorCnd.getBelongBU());
			}
		}
		if (CollectionUtils.isNotEmpty(monitorCnd.getCategoryIdList())) {// 产品类型
			List<Long> categoryIds = new ArrayList<Long>();
			for (String categoryId : monitorCnd.getCategoryIdList()) {
				categoryIds.add(Long.parseLong(categoryId));
			}
			productParam.setCategoryIdList(categoryIds);
			if (CollectionUtils.isNotEmpty(monitorCnd.getSubCategoryIdList())) {
				List<Long> subCategoryIds = new ArrayList<Long>();
				for (String subCategoryId : monitorCnd.getSubCategoryIdList()) {
					subCategoryIds.add(Long.parseLong(subCategoryId));
				}
				productParam.setSubCategoryIdList(subCategoryIds);
			}
		}
		if (UtilityTool.isValid(monitorCnd.getSupplierId())) {// 供应商名称
			productParam.setSupplierId(monitorCnd.getSupplierId());
		}
		if (UtilityTool.isValid(monitorCnd.getPayTarget())) {// 商品支付方式
			productParam.setPaymentTarget(monitorCnd.getPayTarget());
		}
		if (CollectionUtils.isNotEmpty(monitorCnd.getFilialeNames())) {// 所属公司
			if (1 == monitorCnd.getFilialeNames().size()) {
				productParam.setFilialeName(monitorCnd.getFilialeNames().get(0));
			} else {
				productParam.setFilialeNameList(monitorCnd.getFilialeNames());
			}
		}
		if (UtilityTool.isValid(monitorCnd.getStockFlag())) {// 商品支付方式 
			productParam.setStockFlag(monitorCnd.getStockFlag());
		}
	}

	/**
	 * 构建订单参数
	 * @param monitorCnd
	 * @param orderParam
	 */
	private void buildOrderParam(OrderMonitorCnd monitorCnd, LvoOrderOrderParamVo orderParam) {
		if (UtilityTool.isValid(monitorCnd.getOrderId())) {
			orderParam.setOrderId(monitorCnd.getOrderId());
		}
		if (UtilityTool.isValid(monitorCnd.getOrderItemId())) {
			orderParam.setOrderItemId(monitorCnd.getOrderItemId());
		}
		if (UtilityTool.isValid(monitorCnd.getBackUserId())) {// 下单人工号
			orderParam.setBackUserId(monitorCnd.getBackUserId());
		}
		if (UtilityTool.isValid(monitorCnd.getResponsiblePerson())) {// 审核人工号
			orderParam.setOperatorName(monitorCnd.getResponsiblePerson());
			orderParam.setResponsibleObjectType(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
		}
		if (UtilityTool.isValid(monitorCnd.getCreateTimeBegin())) {
			orderParam.setCreateTimeBegin(DateUtil.toDate(monitorCnd.getCreateTimeBegin(), "yyyy-MM-dd HH:mm:ss"));;
		}
		if (UtilityTool.isValid(monitorCnd.getCreateTimeEnd())) {
			orderParam.setCreateTimeEnd(DateUtil.toDate(monitorCnd.getCreateTimeEnd(), "yyyy-MM-dd HH:mm:ss"));;
		}
		if (UtilityTool.isValid(monitorCnd.getOrderStatus())) {// 订单状态
			orderParam.setOrderStatus(monitorCnd.getOrderStatus());
		}
		if (UtilityTool.isValid(monitorCnd.getInfoStatus())) {// 信息审核
			orderParam.setInfoStatus(monitorCnd.getInfoStatus());
		}
		if (UtilityTool.isValid(monitorCnd.getResourceStatus())) {// 资源审核
			orderParam.setResourceStatus(monitorCnd.getResourceStatus());
		}
		if (UtilityTool.isValid(monitorCnd.getPaymentStatus())) {// 支付状态
			orderParam.setPaymentStatus(monitorCnd.getPaymentStatus());
		}
		if (UtilityTool.isValid(monitorCnd.getPaymentTimeBegin())) {
			orderParam.setPaymentTimeBegin(monitorCnd.getPaymentTimeBegin());
		}
		if (UtilityTool.isValid(monitorCnd.getPaymentTimeEnd())) {
			orderParam.setPaymentTimeEnd(monitorCnd.getPaymentTimeEnd());
		}
		if (UtilityTool.isValid(monitorCnd.getCertConfirmStatus())) {// 凭证确认状态
			orderParam.setCertConfirmStatus(monitorCnd.getCertConfirmStatus());
		}
		if (UtilityTool.isValid(monitorCnd.getContractStatus())) {// 合同状态
			orderParam.setTravelContractStatus(monitorCnd.getContractStatus());
		}
		if (UtilityTool.isValid(monitorCnd.getNoticeRegimentStatus())) {// 出团通知书状态
			orderParam.setAdditionStatus(monitorCnd.getNoticeRegimentStatus());
			orderParam.setAdditionStatusType("NOTICE_REGIMENT_STATUS");
		}
		if (CollectionUtils.isNotEmpty(monitorCnd.getDistributorIds())) {// 下单渠道
			orderParam.setDistributorIdList(monitorCnd.getDistributorIds());
			if (1 == monitorCnd.getDistributorIds().size()) {
				orderParam.setDistributorId(monitorCnd.getDistributorIds().get(0));
			}
		}
		if (UtilityTool.isValid(monitorCnd.getDistributorIdForWepAndApp())) {// 无线订单
			if ("Y".equals(monitorCnd.getDistributorIdForWepAndApp())) {
				List<Long> distributionChannels = Arrays.asList(107L, 108L,	110L, 10000L, 10001L, 10002L);
				orderParam.setDistributionChannelList(distributionChannels);
				orderParam.setDistributorIdForWepAndApp(monitorCnd.getDistributorIdForWepAndApp());
			}
		}
		if (UtilityTool.isValid(monitorCnd.getIsTestOrder())) {// 显示测试订单
			orderParam.setIsTestOrder(monitorCnd.getIsTestOrder().charAt(0));
		}
		if (UtilityTool.isValid(monitorCnd.getVisitTimeBegin())) {// 入住时间
			orderParam.setVisitTimeBegin(monitorCnd.getVisitTimeBegin());
		}
		if (UtilityTool.isValid(monitorCnd.getVisitTimeEnd())) {// 入住时间
			orderParam.setVisitTimeEnd(monitorCnd.getVisitTimeEnd());
		}
		if (UtilityTool.isValid(monitorCnd.getPerformStatus())) {// 使用状态 
			orderParam.setPerformStatus(monitorCnd.getPerformStatus());
		}
	}
}
