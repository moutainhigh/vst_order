package com.lvmama.vst.order.client.ord.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.lvmama.comm.pay.vo.Constant;
import com.lvmama.comm.pet.po.fin.SettlementPriceChange;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.comm.utils.PriceUtil;
import com.lvmama.comm.utils.ServletUtil;
import com.lvmama.finance.comm.finance.po.SetSettlementItem;
import com.lvmama.finance.comm.vst.service.SettlementService;
import com.lvmama.visa.api.utils.BusinessException;
import com.lvmama.vst.back.client.ord.service.OrderItemSettlementPriceChangeClientService;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PRICE_RATE_TYPE;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.supp.po.SuppSettleRule;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.ExceptionUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.web.HttpServletLocalThread;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.order.dao.OrdExpiredRefundDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.po.OverdueTicketSubOrderStatusPack;
import com.lvmama.vst.order.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service("orderItemSettlementPriceChangeServiceRemote")
public class OrderItemSettlementPriceChangeServiceImpl implements OrderItemSettlementPriceChangeClientService {

	private static final Logger logger = LoggerFactory.getLogger(OrderItemSettlementPriceChangeServiceImpl.class);

	@Autowired
	private IOrdSettlementPriceRecordService ordSettlementPriceRecordService;

	@Autowired
	private IOrderLocalService orderLocalService;

	@Autowired
	private SettlementService settlementService;

	@Autowired
	private OrderSettlementService orderSettlementService;

	@Autowired
	private IOrderUpdateService orderUpdateService;

	@Autowired
	private LvmmLogClientService lvmmLogClientService;

	@Autowired
	private IOrdMulPriceRateService ordMulPriceRateService;

	@Autowired
	private IOrdOrderItemService ordOrderItemService;

	@Autowired
	private ResPreControlService resControlBudgetRemote;

	// 注入供应商业务接口
	@Autowired
	private SuppSupplierClientService suppSupplierClientService;

	@Autowired
	private OrdExpiredRefundDao overdueTicketDao;
	
	@Autowired
	private OrdOrderItemDao subOrderDao;

	@Autowired
	private IOrdOrderItemExtendService ordOrderItemExtendService;

	@Autowired
	private IOrdOrderService iOrdOrderService;

	@Transactional(readOnly = false, propagation = Propagation.NOT_SUPPORTED)
	public ResultHandleT<String> updateSettlementChange(SettlementPriceChange settlementPriceChange,
			OrdOrderItem orderItem, Long ordMulPriceRateListCount) {
		ResultHandleT<String> resultSelltement = new ResultHandleT<String>();
		// 是否有买断
		if (StringUtil.isNotEmptyString(orderItem.getBuyoutFlag()) && "Y".equals(orderItem.getBuyoutFlag())) {
			Map<String, Object> paramsMulPriceRate = new HashMap<>();
			String multiPrePriceType[] = new String[] { ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode(),
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode() };
			paramsMulPriceRate.put("orderItemId", orderItem.getOrderItemId());
			paramsMulPriceRate.put("priceTypeArray", multiPrePriceType);
			List<OrdMulPriceRate> ordMulPriceRateLists = ordMulPriceRateService
					.findOrdMulPriceRateList(paramsMulPriceRate);
			if (CollectionUtils.isNotEmpty(ordMulPriceRateLists)) {
				resultSelltement = updateOrderMultiBudgetSettlementChange(orderItem, settlementPriceChange);
			} else {
				resultSelltement = updateOrderBudgetSettlementChange(orderItem, settlementPriceChange);
			}
			if (resultSelltement.isSuccess()) {
				logger.info("budget sendSettlementRecordChange orderItemId = " + orderItem.getOrderItemId());
				sendOrdSettlementPriceChangeMsg(orderItem.getOrderItemId());
			}
		} else {
			Map<String, OrdSettlementPriceRecord> resultMap = updateOrAddOrderTotalSettlementChange(
					settlementPriceChange, orderItem, ordMulPriceRateListCount);
			OrdSettlementPriceRecord record = resultMap.get("record");
			if (null != record) {
				logger.info("sendSettlementRecordChange orderItemId = " + orderItem.getOrderItemId());
				sendSettlementRecordChange(record, orderItem.getOrderItemId());
			}

		}
		return resultSelltement;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	@Override
	public Map<String, OrdSettlementPriceRecord> updateOrAddOrderTotalSettlementChange(
			SettlementPriceChange settlementPriceChange, OrdOrderItem orderItem, Long ordMulPriceRateListCount) {
		logger.info("method updateOrAddOrderTotalSettlementChange orderItemId_" + orderItem.getOrderItemId());
		Map<String, OrdSettlementPriceRecord> resultMap = new HashMap<String, OrdSettlementPriceRecord>();
		try {
			List<OrdSettlementPriceRecord> list = new ArrayList<OrdSettlementPriceRecord>();
			StringBuffer logContent = new StringBuffer();
			Long orderId = settlementPriceChange.getOrderId();
			Long orderItemId = settlementPriceChange.getOrderItemId();
			String reason = settlementPriceChange.getReason();// 修改原因
			String remark = settlementPriceChange.getRemark();// 备注
			String totalSettlementPrice = settlementPriceChange.getNewTotalSettlementPriceStr();// 修改后的总价格
			logger.info("updateOrAddOrderTotalSettlementChange totalSettlementPrice" + totalSettlementPrice);
			logger.info("-------------method updateOrAddOrderTotalSettlementChange 开始计算买断结算-----------");
			Long buyoutTotalPrice = 0L;
			Long buyNum = orderItem.getBuyoutQuantity();
			buyNum = buyNum == null ? 0L : buyNum;
			if ("Y".equals(orderItem.getBuyoutFlag())) {
				buyoutTotalPrice = orderItem.getBuyoutTotalPrice();
				buyoutTotalPrice = buyoutTotalPrice == null ? 0L : buyoutTotalPrice;
				orderItem.setTotalSettlementPrice(orderItem.getTotalSettlementPrice() - buyoutTotalPrice);

				Long buyoutQuantity = orderItem.getBuyoutQuantity();
				buyoutQuantity = buyoutQuantity == null ? 0L : buyoutQuantity;
				Long notBuyoutQuantity = orderItem.getQuantity() - buyoutQuantity;
				notBuyoutQuantity = notBuyoutQuantity == 0L ? 1L : notBuyoutQuantity;
				orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice() / (notBuyoutQuantity));
			}
			logger.info("-------------updateOrAddOrderTotalSettlementChange 初始化结算变价记录-----------");
			OrdSettlementPriceRecord newOrdSettlementPriceRecord = new OrdSettlementPriceRecord();
			newOrdSettlementPriceRecord.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.TOTAL_PRICE.getCode());
			newOrdSettlementPriceRecord.setOperator("system");
			newOrdSettlementPriceRecord.setCreateTime(new Date());
			newOrdSettlementPriceRecord.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
			newOrdSettlementPriceRecord.setOrderId(orderId);
			newOrdSettlementPriceRecord.setOrderItemId(orderItemId);
			newOrdSettlementPriceRecord.setSuppGoodsId(orderItem.getSuppGoodsId());
			newOrdSettlementPriceRecord.setVisitTime(orderItem.getVisitTime());
			newOrdSettlementPriceRecord.setIsApprove("Y");
			newOrdSettlementPriceRecord.setApproveRemark("总价无需审核");
			newOrdSettlementPriceRecord.setReason(reason);
			newOrdSettlementPriceRecord.setRemark(remark);
			newOrdSettlementPriceRecord.setSupplierId(orderItem.getSupplierId());
			logger.info("-------------method updateOrAddOrderTotalSettlementChange 初始化结算变价记录结算价相关-----------");
			// 修改之前的结算单价
			newOrdSettlementPriceRecord.setOldActualSettlementPrice(orderItem.getActualSettlementPrice());
			// 修改之前的结算总价
			newOrdSettlementPriceRecord.setOldTotalSettlementPrice(orderItem.getTotalSettlementPrice());
			logger.info(
					"-------------method updateOrAddOrderTotalSettlementChange resetOrderItem4Settlement-----------");
			// 计算后的单价格
			OrdOrderItem item = resetOrderItem4Settlement(orderItem, PriceUtil.convertToFen(totalSettlementPrice));

			logger.info(
					"-------------method updateOrAddOrderTotalSettlementChange resetOrderItem4Settlement-----------end");
			// 修改之后的结算单价
			newOrdSettlementPriceRecord.setNewActualSettlementPrice(item.getActualSettlementPrice());

			// 修改之后的结算总价
			newOrdSettlementPriceRecord.setNewTotalSettlementPrice(item.getTotalSettlementPrice());
			// 修改价格类型
			newOrdSettlementPriceRecord.setPriceType("PRICE");
			newOrdSettlementPriceRecord.setOperator("SYSTEM");

			list.add(newOrdSettlementPriceRecord);
			logger.info("-------------method updateOrAddOrderTotalSettlementChange end 结束结算变价记录结算价相关-----------");
			logContent.append("原结算总价：").append(newOrdSettlementPriceRecord.getOldTotalSettlementPrice() / 100.0)
					.append("新结算总价：").append(newOrdSettlementPriceRecord.getNewTotalSettlementPrice() / 100.0);
			logger.info("updateOrAddOrderTotalSettlementChange info is=" + logContent.toString());
			// 处理结算总价的记录
			String changeResult = OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.UP.getCode();
			OrdSettlementPriceRecord record = list.get(0);
			if ((record.getNewTotalSettlementPrice() - record.getOldTotalSettlementPrice()) < 0) {
				changeResult = OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.getCode();
			}
			record.setChangeResult(changeResult);
			resultMap.put("record", record);
			// 保存结算记录和价格变更记录
			boolean result = this.ordSettlementPriceRecordService.addSettlementTotalPrice(list, orderId);
			if (!result) {
				throw new BusinessException("保存结算记录或价格变更记录失败！");
			}
			// 如果含买断的，要将买断的加上去
			if ("Y".equals(orderItem.getBuyoutFlag())) {
				orderItem.setTotalSettlementPrice(PriceUtil.convertToFen(totalSettlementPrice) + buyoutTotalPrice);
				orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice() / orderItem.getQuantity());
			}
			// 修改子订单
			orderLocalService.updateOrderItem(orderItem);
			logger.info("updateOrAddOrderTotalSettlementChange updateOrderItem actualSettlementPrice="
					+ orderItem.getActualSettlementPrice() + " & totalSettlementPrice="
					+ orderItem.getTotalSettlementPrice());
			try {
				logger.info("method updateOrAddOrderTotalSettlementChange updateOrdItemPriceConfirm 1....");
				updateOrdItemPriceConfirm(orderItemId);
			} catch (Exception e) {
				logger.error(ExceptionFormatUtil.getTrace(e));
			}
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM, orderId, orderItemId, "system",
					"将编号为[" + orderId + "]的子订单，修改子订单结算总价，修改值：" + logContent.toString(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName() + "：发起订单结算价价格修改申请", "");
		} catch (Exception e) {
			logger.error("updateOrAddOrderTotalSettlementChange error:" + e, e);
			logger.error("修改结算价接口异常" + e.getMessage());
			throw new RuntimeException("修改结算价接口异常---" + e.getMessage());
		}
		return resultMap;
	}

	/**
	 * 推送财务结算数据
	 * 
	 * @param record
	 * @param orderItemId
	 * @return
	 */
	public ResultHandleT<String> sendSettlementRecordChange(OrdSettlementPriceRecord record, Long orderItemId) {

		ResultHandleT<String> resultSettlement = new ResultHandleT<String>();
		// 重新查询，测试是否为更新后的数据
		OrdOrderItem orderItemQuery = orderLocalService.getOrderItem(orderItemId);
		// 订单结算状态校验
		ResultMessage resultMessage = this.validateOrderSettlementStatus(orderItemQuery);
		// 子订单修改成功再去推送结算价及推送变价单
		logger.info("method updateOrAddOrderTotalSettlementChange sendSettlementRecordChange and totalSettlementPrice"
				+ orderItemQuery.getTotalSettlementPrice() + " & actualSettlementPrice"
				+ orderItemQuery.getActualSettlementPrice());
		// 如果是结算完成的订单则不推送结算也不更改订单数据，只记录修改历史和生成变价单
		if (resultMessage.isSuccess()) {
			record.setChangeRemark("0");// 结算前修改
			// 推送结算
			sendOrdSettlementPriceChangeMsg(record.getOrderItemId());
			logger.info("method updateOrAddOrderTotalSettlementChange sendSettlementRecordChange");
		} else {
			logger.info("method updateOrAddOrderTotalSettlementChange setChangeRemark 1....");
			record.setChangeRemark("1");// 结算后修改
		}
		// 调用结算系统生成结算单
		record.setChangeFlag("Normal");
		logger.info("updateOrAddOrderTotalSettlementChange insertRecord _JSON=" + GsonUtils.toJson(record));
		try {
			orderSettlementService.insertRecord(record);
			logger.info("updateOrAddOrderTotalSettlementChange insertRecord end....");
		} catch (Exception e) {
			logger.error("updateOrAddOrderTotalSettlementChange insertRecord error:" + e);
		}
		String info = "";
		if (resultMessage.isSuccess()) {
			info = "结算价修改成功";
		} else {
			info = "该订单已经结算过！";
		}
		resultSettlement.setReturnContent(info);
		return resultSettlement;
	}
	
	/**
	 * 推送财务结算数据bug修复版本
	 * 
	 * @param record
	 * @param orderItemId
	 * @return
	 */
	private ResultHandleT<String> sendSettlementRecordChangeRevisedVer(OrdSettlementPriceRecord record, Long orderItemId) {

		ResultHandleT<String> resultSettlement = new ResultHandleT<String>();
		// 重新查询，测试是否为更新后的数据
		OrdOrderItem orderItemQuery = orderLocalService.getOrderItem(orderItemId);
		// 订单结算状态校验
		ResultMessage resultMessage = this.validateOrderSettlementStatus(orderItemQuery);
		// 子订单修改成功再去推送结算价及推送变价单
		logger.info("method updateOrAddOrderTotalSettlementChange sendSettlementRecordChange and totalSettlementPrice"
				+ orderItemQuery.getTotalSettlementPrice() + " & actualSettlementPrice"
				+ orderItemQuery.getActualSettlementPrice());
		// 如果是结算完成的订单则不推送结算也不更改订单数据，只记录修改历史和生成变价单
		if (resultMessage.isSuccess()) {
			record.setChangeRemark("0");// 结算前修改
			// 推送结算
			sendOrdSettlementPriceChangeMsg(record.getOrderItemId());
			logger.info("method updateOrAddOrderTotalSettlementChange sendSettlementRecordChange");
		} else {
			logger.info("method updateOrAddOrderTotalSettlementChange setChangeRemark 1....");
			record.setChangeRemark("1");// 结算后修改
		}
		// 调用结算系统生成结算单
		boolean insertFailed = false;
		record.setChangeFlag("Normal");
		logger.info("updateOrAddOrderTotalSettlementChange insertRecord _JSON=" + GsonUtils.toJson(record));
		try {
			orderSettlementService.insertRecord(record);
			logger.info("updateOrAddOrderTotalSettlementChange insertRecord end....");
		} catch (Exception e) {
			insertFailed = true;
			logger.error("updateOrAddOrderTotalSettlementChange insertRecord error:" + e);
		}
		String info = "";
		if (resultMessage.isSuccess()) {
			if (insertFailed) {
				info = "财务接口调用失败";
			} else {
				info = "结算价修改成功";
			}
		} else {
			info = "该订单已经结算过！";
		}
		resultSettlement.setReturnContent(info);
		return resultSettlement;
	}	

	/**
	 * 订单结算状态校验
	 * 
	 * @param orderItem
	 * @return
	 */
	private ResultMessage validateOrderSettlementStatus(OrdOrderItem orderItem) {

		String itemStatus = getSetSettlementItemStatus(orderItem.getOrderItemId());

		if (OrderEnum.ORDER_SETTLEMENT_STATUS.SETTLEMENTED.getCode().equals(itemStatus)) {
			return new ResultMessage(ResultMessage.ERROR, "该子订单已经处于已结算状态不可进行此操作");
		}

		return ResultMessage.CHECK_SUCCESS_RESULT;
	}

	// 获取结算状态
	public String getSetSettlementItemStatus(Long itemId) {
		try {
			List<SetSettlementItem> setSettlementItems = new ArrayList<SetSettlementItem>();
			List<Long> itemIds = new ArrayList<Long>();
			itemIds.add(itemId);
			setSettlementItems = settlementService.searchSetSettlementItemByOrderItemIds(itemIds);
			if (null != setSettlementItems && setSettlementItems.size() > 0) {
				return setSettlementItems.get(0).getSettlementStatus();
			}
		} catch (Exception e) {
			throw new RuntimeException("调用支付接口获取结算状态异常---" + e.getMessage());
		}
		return OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.name();
	}

	/**
	 * 发送结算价修改消息
	 * 
	 * @param orderItemId
	 */
	private void sendOrdSettlementPriceChangeMsg(Long orderItemId) {
		String addition = new StringBuffer(orderItemId + "").append("|").append("system").toString();
		orderLocalService.sendOrdSettlementPriceChangeMsg(orderItemId, addition);
	}

	/**
	 * 修改子订单价格确认状态
	 * 
	 * @param orderItemId
	 * @return void
	 */
	private Integer updateOrdItemPriceConfirm(Long orderItemId) {
		OrdOrderItem ordOrderItem = new OrdOrderItem();
		ordOrderItem.setOrderItemId(orderItemId);
		ordOrderItem.setPriceConfirmStatus(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.getCode());

		int result = this.orderUpdateService.updateOrderItemByIdSelective(ordOrderItem);
		if (result > 0) {
			sendOrdItemPriceConfirmChangeMsg(orderItemId);
		}
		return result;

	}

	private OrdOrderItem resetOrderItem4Settlement(OrdOrderItem orderItem, Long totalPrice) {
		Long buyNum = orderItem.getBuyoutQuantity();
		buyNum = buyNum == null ? 0L : buyNum;
		// 非买断份数
		Long notBuyoutQuantity = orderItem.getQuantity() - buyNum;
		notBuyoutQuantity = notBuyoutQuantity == 0L ? 1L : notBuyoutQuantity;
		orderItem.setActualSettlementPrice(BigDecimal.valueOf(totalPrice)
				.divide(BigDecimal.valueOf(notBuyoutQuantity), 2, BigDecimal.ROUND_HALF_EVEN).longValue());
		orderItem.setTotalSettlementPrice(totalPrice);
		return orderItem;
	}

	private void sendOrdItemPriceConfirmChangeMsg(Long orderItemId) {
		// String addition=new
		// StringBuffer(orderItemId+"").append("|").append(this.getLoginUserId()).toString();
		String addition = new StringBuffer(orderItemId + "").append("|").append("system").toString();
		orderLocalService.sendOrdItemPriceConfirmChangeMsg(orderItemId, addition);

	}

	@Override
	public ResultHandleT<String> updateBudgetSettlementPriceChange(SettlementPriceChange settlementPriceChange,
			OrdOrderItem orderItem, Long ordMulPriceRateListCount) {
		ResultHandleT<String> resultHanle = new ResultHandleT<String>();
		// 判断订单买断价是否是多价格买断
		if (ordMulPriceRateListCount >= 1) {
			// 修改多价格买断
			resultHanle = updateOrderMultiBudgetSettlementChange(orderItem, settlementPriceChange);
		} else {
			// 修改单价格买断
			resultHanle = updateOrderBudgetSettlementChange(orderItem, settlementPriceChange);
		}
		return resultHanle;
	}

	/**
	 * 修改多价格订单买断结算价（仅成人价/儿童价，不含其它价格种类）
	 * 
	 * @param orderItem
	 * @param settlementPriceChange
	 * @return
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public ResultHandleT<String> updateOrderMultiBudgetSettlementChange(OrdOrderItem orderItem,
			SettlementPriceChange settlementPriceChange) {
		logger.info("--------method addOrderMultiBudgetSettlementChange  start" + orderItem.getOrderItemId());
		ResultHandleT<String> resultHanle = new ResultHandleT<String>();
		try {
			StringBuilder logContent = new StringBuilder(300);
			String reason = settlementPriceChange.getReason();
			String remark = settlementPriceChange.getRemark();
			String priceModel = "BUDGET_TOTAL_PRICE";
			String buyoutTotalPriceStr = settlementPriceChange.getNewTotalSettlementPriceStr();
			Long buyoutTotalPrice = PriceUtil.convertToFen(buyoutTotalPriceStr);

			List<OrdSettlementPriceRecord> list = new ArrayList<OrdSettlementPriceRecord>();
			Long orderId = orderItem.getOrderId();
			// 原先买断的总价
			Long preBuyoutTotalPrice = orderItem.getBuyoutTotalPrice();
			// 非买断的，其他结算价
			Long otherTotalPrice = orderItem.getTotalSettlementPrice() - orderItem.getBuyoutTotalPrice();

			// 记录差价
			Long buyoutTotalAmount = 0L;
			buyoutTotalAmount = buyoutTotalPrice - preBuyoutTotalPrice;
			String multiPrePriceType[] = new String[] { ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode(),
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode() };

			List<OrdMulPriceRate> ordMulPriceRateList = ordSettlementPriceRecordService
					.calcBuyoutSettlementUnitPrice(orderItem.getOrderItemId(), buyoutTotalPrice);
			logger.info("--------method addOrderMultiBudgetSettlementChange ordMulPriceRateList size:"
					+ ordMulPriceRateList.size());
			for (int x = 0, y = multiPrePriceType.length; x < y; x++) {
				String checkPriceType = multiPrePriceType[x];
				OrdMulPriceRate ordMulPriceRate = null;
				for (int m = 0, n = ordMulPriceRateList.size(); m < n; m++) {
					if (ordMulPriceRateList.get(m) != null
							&& checkPriceType.equals(ordMulPriceRateList.get(m).getPriceType())) {
						ordMulPriceRate = ordMulPriceRateList.get(m);
						break;
					}
				}
				if (ordMulPriceRate == null) {
					// 如果没有对应的结算价-则next
					continue;
				}
				logger.info("--------method addOrderMultiBudgetSettlementChange ordMulPriceRate id="
						+ ordMulPriceRate.getOrdMulPriceRateId());
				Long newPrice = null;
				if (checkPriceType.equals(ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode())) {
					newPrice = ordMulPriceRate.getPrice();
					logContent.append("原").append(ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCnName(checkPriceType))
							.append("：").append(ordMulPriceRate.getOrigPrice() / 100.0).append("新结算单价：")
							.append(newPrice);
				} else {
					newPrice = ordMulPriceRate.getPrice();
					logContent.append("原").append(ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCnName(checkPriceType))
							.append("：").append(ordMulPriceRate.getOrigPrice() / 100.0).append("新结算单价：")
							.append(newPrice);
				}
				OrdSettlementPriceRecord newOrdSettlementPriceRecord = new OrdSettlementPriceRecord();
				newOrdSettlementPriceRecord
						.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.BUDGET_TOTAL_PRICE.getCode());
				newOrdSettlementPriceRecord.setOperator("system");
				newOrdSettlementPriceRecord.setCreateTime(new Date());
				newOrdSettlementPriceRecord.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
				newOrdSettlementPriceRecord.setOrderId(orderId);
				newOrdSettlementPriceRecord.setOrderItemId(orderItem.getOrderItemId());
				newOrdSettlementPriceRecord.setSuppGoodsId(orderItem.getSuppGoodsId());
				newOrdSettlementPriceRecord.setVisitTime(orderItem.getVisitTime());
				newOrdSettlementPriceRecord.setIsApprove("Y");
				newOrdSettlementPriceRecord.setReason(reason);
				newOrdSettlementPriceRecord.setRemark(remark);
				newOrdSettlementPriceRecord.setSupplierId(orderItem.getSupplierId());
				newOrdSettlementPriceRecord.setPriceType(checkPriceType);
				// 修改之前的买断结算单价
				newOrdSettlementPriceRecord.setOldBudgetUnitSettlementPrice(ordMulPriceRate.getOrigPrice());
				// 修改之后的结算买断单价
				newOrdSettlementPriceRecord.setNewBudgetUnitSettlementPrice(newPrice);
				// 修改之前的买断结算总价
				newOrdSettlementPriceRecord.setOldBudgetTotalSettlementPrice(
						ordMulPriceRate.getOrigPrice() * ordMulPriceRate.getQuantity());
				// 修改之后的买断结算总价
				newOrdSettlementPriceRecord.setNewBudgetTotalSettlementPrice(newPrice * ordMulPriceRate.getQuantity());

				newOrdSettlementPriceRecord.setChangeFlag("BUYOUT");
				list.add(newOrdSettlementPriceRecord);
			}
			String changeResult = OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.UP.getCode();
			// 订单结算状态校验
			ResultMessage resultMessage = this.validateOrderSettlementStatus(orderItem);
			logger.info("--------method addOrderMultiBudgetSettlementChange ordMulPriceRate RECORDlIST="
					+ GsonUtils.toJson(list));
			int updateOrderItemCount = 0;
			Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();

			for (OrdSettlementPriceRecord record : list) {

				Long a = record.getNewTotalSettlementPrice();
				a = a == null ? 0L : a;
				Long b = record.getNewBudgetTotalSettlementPrice();
				b = b == null ? 0L : b;
				Long c = record.getOldTotalSettlementPrice();
				c = c == null ? 0L : c;
				Long d = record.getOldTotalSettlementPrice();
				d = d == null ? 0L : d;

				if ((a + b - c - d) < 0) {
					changeResult = OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.getCode();
				}
				logger.info("--------method addOrderMultiBudgetSettlementChange  changeResult=" + changeResult);
				record.setChangeResult(changeResult);
				if (resultMessage.isSuccess()) {
					record.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
					record.setOperator("system");
					record.setIsApprove("Y");
					record.setChangeRemark("0");// 没有结算过的
				} else {
					record.setChangeRemark("1");// 结算过的
				}
				logger.info(
						"--------method addOrderMultiBudgetSettlementChange  saveBuyoutMultiPriceAfterApprove buyoutTotalPrice="
								+ buyoutTotalPrice);
				int n = ordSettlementPriceRecordService.saveBuyoutMultiPriceAfterApprove(buyoutTotalPrice, record,
						orderId, priceModel);
				logger.info(
						"--------method addOrderMultiBudgetSettlementChange  saveBuyoutMultiPriceAfterApprove n=" + n);
				// 修改子订单的多价格的数据库
				paramsMulPriceRate.clear();
				paramsMulPriceRate.put("orderItemId", orderItem.getOrderItemId());
				paramsMulPriceRate.put("priceTypeArray", new String[] { record.getPriceType() });
				List<OrdMulPriceRate> ordMulPriceRateLists = ordMulPriceRateService
						.findOrdMulPriceRateList(paramsMulPriceRate);
				logger.info("--------method addOrderMultiBudgetSettlementChange  findOrdMulPriceRateList size="
						+ ordMulPriceRateLists.size());
				if (ordMulPriceRateList.size() > 0) {
					for (OrdMulPriceRate ordMulPrice : ordMulPriceRateLists) {
						if (record.getPriceType().equalsIgnoreCase(ordMulPrice.getPriceType())) {
							// 更新多价格类型对应结算价
							ordMulPrice.setPrice(record.getNewBudgetUnitSettlementPrice());
							ordMulPriceRateService.updateByPrimaryKeySelective(ordMulPrice);
						}
					}
				}
				logger.info("--------method addOrderMultiBudgetSettlementChange  findOrdMulPriceRateList size="
						+ ordMulPriceRateLists.size());
				// 订单修改成功的才做推送结算和生成变价单
				if (n == 1) {
					if (resultMessage.isSuccess() && updateOrderItemCount == 0) {// 订单修改成功并且没有结算完成的，推送结算
						logger.info("子订单，以及该子订单的推送，只要执行一次即可");
						orderItem.setBuyoutTotalPrice(buyoutTotalAmount + orderItem.getBuyoutTotalPrice());
						orderItem.setBuyoutPrice(orderItem.getBuyoutTotalPrice() / orderItem.getBuyoutQuantity());
						orderItem.setTotalSettlementPrice(orderItem.getBuyoutTotalPrice() + otherTotalPrice);
						orderItem.setActualSettlementPrice(
								orderItem.getTotalSettlementPrice() / orderItem.getQuantity());
						updateOrderItemCount = ordOrderItemService.updateOrdOrderItem(orderItem);
						if (updateOrderItemCount == 1L) {
							// 更改结算成功后，要对预控的金额进行更新，如果为0，那么要提醒变价；
							Long nowBuyoutTotalPrice = orderItem.getBuyoutTotalPrice();
							nowBuyoutTotalPrice = nowBuyoutTotalPrice == null ? 0L : nowBuyoutTotalPrice;
							if (nowBuyoutTotalPrice.longValue() != preBuyoutTotalPrice.longValue()) {
								Date visitDate = orderItem.getVisitTime();
								Long goodsId = orderItem.getSuppGoodsId();
								GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = resControlBudgetRemote
										.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
								long p = Math.min(preBuyoutTotalPrice, goodsResPrecontrolPolicyVO.getAmount())
										- nowBuyoutTotalPrice;
								if (goodsResPrecontrolPolicyVO != null
										&& ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name()
												.equals(goodsResPrecontrolPolicyVO.getControlType())) {
									Long amountId = goodsResPrecontrolPolicyVO.getAmountId();
									Long controlId = goodsResPrecontrolPolicyVO.getId();
									Long leftAmount = goodsResPrecontrolPolicyVO.getLeftAmount();
									logger.info(goodsId + "差价" + p);
									logger.info(goodsId + "剩余金额" + leftAmount);
									Long leftValue = leftAmount + p;
									leftValue = leftValue < 0 ? 0L : leftValue;
									leftValue = leftValue > goodsResPrecontrolPolicyVO.getAmount()
											? goodsResPrecontrolPolicyVO.getAmount() : leftValue;
									resControlBudgetRemote.updateAmountResPrecontrolPolicy(amountId, controlId,
											visitDate, leftValue);
									if (leftValue == 0L) {
										resControlBudgetRemote.handleResPrecontrolSaledOut(goodsResPrecontrolPolicyVO,
												orderItem.getVisitTime(), goodsId);
									}
								}

							}
						}

					}
					// 调用结算系统生成结算单
					logger.info("order_item_id=" + record.getOrderItemId() + " vst_record_id=" + record.getRecordId());
					try {
						orderSettlementService.insertRecord(record);
					} catch (Exception ex) {
						logger.error(ExceptionFormatUtil.getTrace(ex));
					}

				}
			}

			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM, orderId, orderItem.getOrderItemId(),
					"system", "将编号为[" + orderId + "]的子订单，修改子订单结算单价，修改值：" + logContent.toString(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName() + "：发起订单买断结算价价格修改申请", "");

			String info = "修改申请已经提交。由于下述原因，需财务审核：a.子订单结算中 b.子订单已结算  c.有负毛利的风险";

			if (resultMessage.isSuccess()) {
				info = "结算价修改成功";
			} else {
				info = "该订单已经结算过！";
			}
			resultHanle.setReturnContent(info);
		} catch (Exception e) {
			resultHanle.setMsg(e);
			throw new RuntimeException("修改结算价接口异常---" + e.getMessage());
		}
		return resultHanle;
	}

	/**
	 * 
	 * @param
	 * @param
	 * @return
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public ResultHandleT<String> updateOrderBudgetSettlementChange(OrdOrderItem orderItem,
			SettlementPriceChange settlementPriceChange) {
		logger.info(
				"-----------------addOrderBudgetSettlementChange info start------------" + orderItem.getOrderItemId());
		ResultHandleT<String> resultHanle = new ResultHandleT<String>();
		try {
			String change_flag = "BUYOUT";
			List<OrdSettlementPriceRecord> list = new ArrayList<OrdSettlementPriceRecord>();
			StringBuffer logContent = new StringBuffer();
			Long orderItemId = orderItem.getOrderItemId();
			String reason = settlementPriceChange.getReason();// 修改原因
			String remark = settlementPriceChange.getRemark();// 备注
			String settlementPrice = settlementPriceChange.getNewTotalSettlementPriceStr();// 修改后的总价格
			Long oldBuyoutTotalPrice = 0L;
			Long newBuyoutTotalPrice = 0L;

			oldBuyoutTotalPrice = orderItem.getBuyoutTotalPrice();
			oldBuyoutTotalPrice = oldBuyoutTotalPrice == null ? 0L : oldBuyoutTotalPrice;
			// 得到新订单新的结算价
			OrdSettlementPriceRecord newOrdSettlementPriceRecord = getNewOrdSettlementPriceRecord(orderItem, reason,
					remark, PriceUtil.convertToFen(Float.parseFloat(settlementPrice)));
			logger.info(
					"-----------------addOrderBudgetSettlementChange info getNewOrdSettlementPriceRecord end------------"
							+ orderItem.getOrderItemId());
			list.add(newOrdSettlementPriceRecord);

			if (newOrdSettlementPriceRecord.getOldTotalSettlementPrice() != null) {
				logContent.append("原非买断结算总价：").append(newOrdSettlementPriceRecord.getOldTotalSettlementPrice() / 100.0);
			}
			if (newOrdSettlementPriceRecord.getOldActualSettlementPrice() != null) {
				logContent.append("原非买断结算单价：")
						.append(newOrdSettlementPriceRecord.getOldActualSettlementPrice() / 100.0);
			}
			if (newOrdSettlementPriceRecord.getOldBudgetTotalSettlementPrice() != null) {
				logContent.append("原买断结算总价：")
						.append(newOrdSettlementPriceRecord.getOldBudgetTotalSettlementPrice() / 100.0);
			}
			if (newOrdSettlementPriceRecord.getOldBudgetUnitSettlementPrice() != null) {
				logContent.append("原买断结算单价：")
						.append(newOrdSettlementPriceRecord.getOldBudgetUnitSettlementPrice() / 100.0);
			}
			if (newOrdSettlementPriceRecord.getNewTotalSettlementPrice() != null) {
				logContent.append("新非买断结算总价：").append(newOrdSettlementPriceRecord.getNewTotalSettlementPrice() / 100.0);
			}
			if (newOrdSettlementPriceRecord.getNewActualSettlementPrice() != null) {
				logContent.append("新非买断结算单价：")
						.append(newOrdSettlementPriceRecord.getNewActualSettlementPrice() / 100.0);
			}
			if (newOrdSettlementPriceRecord.getNewBudgetTotalSettlementPrice() != null) {
				logContent.append("新买断结算总价：")
						.append(newOrdSettlementPriceRecord.getNewBudgetTotalSettlementPrice() / 100.0);
			}
			if (newOrdSettlementPriceRecord.getNewBudgetUnitSettlementPrice() != null) {
				logContent.append("新买断结算单价：")
						.append(newOrdSettlementPriceRecord.getNewBudgetUnitSettlementPrice() / 100.0);
			}

			// 处理结算总价的记录
			String changeResult = OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.UP.getCode();
			OrdSettlementPriceRecord record = list.get(0);
			Long newBudgetTotalSettlementPrice = record.getNewBudgetTotalSettlementPrice() != null
					? record.getNewBudgetTotalSettlementPrice() : 0;
			Long newTotalSettlementPrice = record.getNewTotalSettlementPrice() != null
					? record.getNewTotalSettlementPrice() : 0;

			Long oldTotalSettlementPrice = record.getOldTotalSettlementPrice() != null
					? record.getOldTotalSettlementPrice() : 0;
			Long oldBudgetTotalSettlementPrice = record.getOldBudgetTotalSettlementPrice() != null
					? record.getOldBudgetTotalSettlementPrice() : 0;

			if ((newBudgetTotalSettlementPrice + newTotalSettlementPrice - oldTotalSettlementPrice
					- oldBudgetTotalSettlementPrice) < 0) {
				changeResult = OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.getCode();
			}
			record.setChangeResult(changeResult);
			// 订单结算状态校验
			ResultMessage resultMessage = this.validateOrderSettlementStatus(orderItem);
			// 保存结算记录和价格变更记录
			this.ordSettlementPriceRecordService.addSettlementTotalPrice(list, orderItem.getOrderId());
			logger.info("-----------------addOrderBudgetSettlementChange info updateOrderItem start------------"
					+ orderItem.getOrderItemId());
			// 修改子订单
			ResultHandle resultHandle = orderLocalService.updateOrderItem(orderItem);
			logger.info("-----------------addOrderBudgetSettlementChange info updateOrderItem end------------"
					+ orderItem.getOrderItemId());
			if (resultHandle.isSuccess()) {// 子订单修改成功再去推送结算价及推送变价单
				// 修改多价格记录
				if (resultMessage.isSuccess()) {// 如果是结算完成的订单则不推送结算也不更改订单数据，只记录修改历史和生成变价单

					newBuyoutTotalPrice = orderItem.getBuyoutTotalPrice();
					newBuyoutTotalPrice = newBuyoutTotalPrice == null ? 0L : newBuyoutTotalPrice;
					if (newBuyoutTotalPrice.longValue() != oldBuyoutTotalPrice.longValue()) {
						Date visitDate = orderItem.getVisitTime();
						Long goodsId = orderItem.getSuppGoodsId();
						GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = resControlBudgetRemote
								.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
						long p = Math.min(oldBuyoutTotalPrice, goodsResPrecontrolPolicyVO.getAmount())
								- newBuyoutTotalPrice;
						if (goodsResPrecontrolPolicyVO != null && ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount
								.name().equals(goodsResPrecontrolPolicyVO.getControlType())) {
							Long amountId = goodsResPrecontrolPolicyVO.getAmountId();
							Long controlId = goodsResPrecontrolPolicyVO.getId();
							Long leftAmount = goodsResPrecontrolPolicyVO.getLeftAmount();
							logger.info(goodsId + "差价" + p);
							logger.info(goodsId + "剩余金额" + leftAmount);
							Long leftValue = leftAmount + p;
							leftValue = leftValue < 0 ? 0L : leftValue;
							leftValue = leftValue > goodsResPrecontrolPolicyVO.getAmount()
									? goodsResPrecontrolPolicyVO.getAmount() : leftValue;
							resControlBudgetRemote.updateAmountResPrecontrolPolicy(amountId, controlId, visitDate,
									leftValue);
							if (leftValue == 0L) {
								resControlBudgetRemote.handleResPrecontrolSaledOut(goodsResPrecontrolPolicyVO,
										orderItem.getVisitTime(), goodsId);
							}
						}

					}
					record.setChangeRemark("0");// 结算前修改
					// 推送结算
					// sendOrdSettlementPriceChangeMsg(record.getOrderItemId());
				} else {
					record.setChangeRemark("1");// 结算后修改
				}

				record.setChangeFlag(change_flag);
				try {
					logger.info("-----------------addOrderBudgetSettlementChange info insertRecord ------------"
							+ orderItem.getOrderItemId());
					// 调用结算系统生成结算单
					orderSettlementService.insertRecord(record);
				} catch (Exception e) {
					logger.error("insertRecord exception:" + e);
				}
			}

			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM, orderItem.getOrderId(), orderItemId,
					"system", "将编号为[" + orderItem.getOrderId() + "]的子订单，修改子订单结算总价，修改值：" + logContent.toString(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName() + "：发起订单结算价价格修改申请", "");

			String info = "修改申请已经提交。由于下述原因，需财务审核：a.子订单结算中 b.子订单已结算  c.有负毛利的风险";

			if (resultMessage.isSuccess()) {
				info = "结算价修改成功";
			} else {
				info = "该订单已经结算过！";
			}

			resultHanle.setReturnContent(info);
			return resultHanle;
		} catch (Exception e) {
			resultHanle.setMsg(e);
			logger.error(ExceptionUtil.getExceptionDetails(e));
			throw new RuntimeException("修改结算价接口异常---" + e.getMessage());
		}
	}

	/**
	 * 得到新的结算价
	 * 
	 * @param
	 * @param orderItem
	 * @param reason
	 * @param remark
	 * @param settlementPrice
	 * @return
	 */
	private OrdSettlementPriceRecord getNewOrdSettlementPriceRecord(OrdOrderItem orderItem, String reason,
			String remark, Long settlementPrice) {
		OrdSettlementPriceRecord newOrdSettlementPriceRecord = new OrdSettlementPriceRecord();

		newOrdSettlementPriceRecord
				.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.BUDGET_TOTAL_PRICE.getCode());
		newOrdSettlementPriceRecord.setOperator("system");
		newOrdSettlementPriceRecord.setCreateTime(new Date());
		newOrdSettlementPriceRecord.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
		newOrdSettlementPriceRecord.setOrderId(orderItem.getOrderId());
		newOrdSettlementPriceRecord.setOrderItemId(orderItem.getOrderItemId());
		newOrdSettlementPriceRecord.setSuppGoodsId(orderItem.getSuppGoodsId());
		newOrdSettlementPriceRecord.setVisitTime(orderItem.getVisitTime());
		newOrdSettlementPriceRecord.setIsApprove("Y");
		newOrdSettlementPriceRecord.setApproveRemark("总价无需审核");
		newOrdSettlementPriceRecord.setReason(reason);
		newOrdSettlementPriceRecord.setRemark(remark);
		newOrdSettlementPriceRecord.setSupplierId(orderItem.getSupplierId());

		// 修改之前买断单价
		newOrdSettlementPriceRecord.setOldBudgetUnitSettlementPrice(orderItem.getBuyoutPrice());
		// 修改之前买断总价
		newOrdSettlementPriceRecord.setOldBudgetTotalSettlementPrice(orderItem.getBuyoutTotalPrice());

		if (orderItem.getQuantity() - orderItem.getBuyoutQuantity() != 0) {
			// 修改之前的非买断总价
			newOrdSettlementPriceRecord
					.setOldTotalSettlementPrice(orderItem.getTotalSettlementPrice() - orderItem.getBuyoutTotalPrice());
			// 修改之前的非买断单价
			newOrdSettlementPriceRecord
					.setOldActualSettlementPrice(newOrdSettlementPriceRecord.getOldTotalSettlementPrice()
							/ (orderItem.getQuantity() - orderItem.getBuyoutQuantity()));
		}

		// 设置子订单结算价
		setCalcSettlementPrice(settlementPrice, orderItem);

		// 修改之后的买断单价
		newOrdSettlementPriceRecord.setNewBudgetUnitSettlementPrice(orderItem.getBuyoutPrice());
		// 修改之后的买断总价
		newOrdSettlementPriceRecord.setNewBudgetTotalSettlementPrice(orderItem.getBuyoutTotalPrice());

		if (orderItem.getQuantity() - orderItem.getBuyoutQuantity() != 0) {
			// 修改之后的非买断总价
			newOrdSettlementPriceRecord
					.setNewTotalSettlementPrice(orderItem.getTotalSettlementPrice() - orderItem.getBuyoutTotalPrice());
			// 修改之后的非买断单价
			newOrdSettlementPriceRecord
					.setNewActualSettlementPrice(newOrdSettlementPriceRecord.getNewTotalSettlementPrice()
							/ (orderItem.getQuantity() - orderItem.getBuyoutQuantity()));
		}

		// 修改价格类型
		newOrdSettlementPriceRecord.setPriceType("PRICE");

		newOrdSettlementPriceRecord.setOperator("system");

		return newOrdSettlementPriceRecord;
	}

	/**
	 * 计算买断结算价（单价格）
	 * 
	 * @param
	 * @param settlementPrice
	 * @param orderItem
	 */
	private void setCalcSettlementPrice(Long settlementPrice, OrdOrderItem orderItem) {
		// 先计算非买断的总价(总价-买断总价)
		Long unBudgetTotalPrice = orderItem.getTotalSettlementPrice() - orderItem.getBuyoutTotalPrice();
		logger.info("修改总价非买断总价：" + unBudgetTotalPrice);
		orderItem.setTotalSettlementPrice(unBudgetTotalPrice + settlementPrice);
		// 设置结算单价
		orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice() / orderItem.getQuantity());
		// 设置买断结算总价
		orderItem.setBuyoutTotalPrice(settlementPrice);
		logger.info("修改总价买断总价：" + orderItem.getBuyoutTotalPrice());
		// 设置买断结算单价
		orderItem.setBuyoutPrice(settlementPrice / orderItem.getBuyoutQuantity());
	}

	/**
	 * 订单-结算价修改(往财务推送接口)
	 * 
	 * @param orderId
	 * @param orderItemId
	 * @return
	 */
	public ResultMessage manualSettlmente(Long orderId, Long orderItemId) {
		OrdOrder orderObj = this.orderUpdateService.queryOrdOrderByOrderId(orderId);
		if (!orderObj.isNeedSettlement()) {
			return new ResultMessage(ResultMessage.ERROR, "预付和下单渠道为非分销商订单才需要进入结算");
		}
		// 如果没有结算规则则提示错误
		if (!checkSuppSettleRule(orderItemId)) {
			return new ResultMessage(ResultMessage.ERROR, "操作失败，原因：没有结算规则");
		}
		String addition = this.getLoginUserId();
		this.orderLocalService.sendManualSettlmenteMsg(orderItemId, addition);

		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM, orderId, orderItemId,
				this.getLoginUserId(), "将编号为[" + orderItemId + "]的订单子项，手动发起生成结算子项",
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName() + "手动发起生成结算子项", null);

		return new ResultMessage(ResultMessage.SUCCESS, "操作成功");

	}

	/**
	 * 订单-结算价批量修改(往财务推送接口 专用)
	 * 
	 * @param ordOrderItemList
	 * @return
	 */
	@Override
	public ResultMessage batchManualSettlmente(List<OrdOrderItem> ordOrderItemList) {
		logger.info(" [ OrderItemSettlementPriceChangeServiceImpl ] , batch modify settlement method start! ");
		if (CollectionUtils.isNotEmpty(ordOrderItemList)) {
			try {
				JSONObject obj = new JSONObject();
				obj.put("result", true);
				for (OrdOrderItem ordOrderItem : ordOrderItemList) {
					Long orderId = ordOrderItem.getOrderId();
					Long orderItemId = ordOrderItem.getOrderItemId();
					OrdOrder orderObj = this.orderUpdateService.queryOrdOrderByOrderId(orderId);
					if (!orderObj.isNeedSettlement()) {
						obj.put("error_" + orderItemId, "预付和下单渠道为非分销商订单才需要进入结算");
						obj.put("result", false);
						continue;
						// return new
						// ResultMessage(ResultMessage.ERROR,"预付和下单渠道为非分销商订单才需要进入结算");
					}
					// 如果没有结算规则则提示错误
					if (!checkSuppSettleRule(orderItemId)) {
						obj.put("error_" + orderItemId, "操作失败，原因：没有结算规则");
						obj.put("result", false);
						continue;
						// return new
						// ResultMessage(ResultMessage.ERROR,"操作失败，原因：没有结算规则");
					}
					String addition = this.getLoginUserId();
					this.orderLocalService.sendManualSettlmenteMsg(orderItemId, addition);

					lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM, orderId, orderItemId,
							this.getLoginUserId(), "将编号为[" + orderItemId + "]的订单子项，手动发起生成结算子项",
							ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
							ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName() + "手动发起生成结算子项", null);
				}

				if (!obj.getBoolean("result")) {
					return new ResultMessage(ResultMessage.ERROR,
							" [ OrderItemSettlementPriceChangeServiceImpl ]: batch modify settlement has error, json msg is "
									+ obj.toJSONString());
				}

			} catch (Exception e) {
				return new ResultMessage(ResultMessage.ERROR, "需进行结算的订单数据有问题。");
			}
			return new ResultMessage(ResultMessage.SUCCESS, "操作成功");
		}
		logger.info(" [ OrderItemSettlementPriceChangeServiceImpl ] , batch modify settlement method end! ");
		return new ResultMessage(ResultMessage.ERROR, "需选择要生成的结算子项的订单。");
	}

	private boolean checkSuppSettleRule(Long orderItemId) {
		// 检查是否 有结算规则，如果没有则报错
		OrdOrderItem orderItem = orderLocalService.getOrderItem(orderItemId);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("contractId", orderItem.getContractId());
		params.put("supplierId", orderItem.getSupplierId());
		ResultHandleT<List<SuppSettleRule>> result = suppSupplierClientService.findSuppSettleRuleList(params);
		if (result != null && result.getReturnContent() != null && !result.getReturnContent().isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * 提取登录用户ID
	 * 
	 * @return
	 */
	public String getLoginUserId() {
		PermUser pu = getLoginUser();
		if (null != pu) {
			return pu.getUserName();
		} else {
			return "";
		}
	}

	protected PermUser getLoginUser() {
		PermUser pu = (PermUser) getSession("SESSION_BACK_USER");
		return pu;
	}

	protected Object getSession(final String key) {
		if (StringUtils.isEmpty(key)) {
			return null;
		}
		return ServletUtil.getSession(HttpServletLocalThread.getRequest(), HttpServletLocalThread.getResponse(), key);
	}

	@Override
	// @Transactional(readOnly = false, propagation = Propagation.NOT_SUPPORTED)
	public void updateSettlementChange(SettlementPriceChange settlementPriceChange, OrdOrderItem orderItem,
			Long ordMulPriceRateListCount, Integer processStatus, Boolean isEbkSubOrder) {
		try {
			boolean buyout = StringUtil.isNotEmptyString(orderItem.getBuyoutFlag())
					&& "Y".equals(orderItem.getBuyoutFlag());
			logger.info((buyout ? "is" : "not") + " buyout order");
			if (processStatus.equals(OrderEnum.ExpiredRefundState.ORDITEM_UNSETTLEMENT.getCode())) {
				logger.info("sub-order[" + orderItem.getOrderId() + ":" + orderItem.getOrderItemId() + "] "
						+ OrderEnum.ExpiredRefundState.ORDITEM_UNSETTLEMENT.getDesc()
						+ ", so try to update settlement-price of sub-order and inform financial system");
				// 是否有买断
				if (buyout) {
					Map<String, Object> paramsMulPriceRate = new HashMap<>();
					String multiPrePriceType[] = new String[] { ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode(),
							ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode() };
					paramsMulPriceRate.put("orderItemId", orderItem.getOrderItemId());
					paramsMulPriceRate.put("priceTypeArray", multiPrePriceType);
					List<OrdMulPriceRate> ordMulPriceRateLists = ordMulPriceRateService
							.findOrdMulPriceRateList(paramsMulPriceRate);
					ResultHandleT<String> resultSelltement = new ResultHandleT<String>();
					if (CollectionUtils.isNotEmpty(ordMulPriceRateLists)) {
						resultSelltement = updateOrderMultiBudgetSettlementChange(orderItem, settlementPriceChange);
					} else {
						resultSelltement = updateOrderBudgetSettlementChange(orderItem, settlementPriceChange);
					}
					if (resultSelltement.isSuccess()) {
						logger.info("budget sendSettlementRecordChange orderItemId = " + orderItem.getOrderItemId());
						markOverdueTicketSettlementPriceChanged(orderItem,
								OrderEnum.ExpiredRefundState.FINANCE_UNSETTLEMENT.getCode(),
								OrderEnum.ExpiredRefundState.FINANCE_UNSETTLEMENT.getDesc(), isEbkSubOrder);
						sendOrdSettlementPriceChangeMsg(orderItem.getOrderItemId());
						markOverdueTicketSettlementPriceChanged(orderItem,
								OrderEnum.ExpiredRefundState.SUCCESS.getCode(),
								OrderEnum.ExpiredRefundState.SUCCESS.getDesc(), isEbkSubOrder);
					}
				} else {
					Map<String, OrdSettlementPriceRecord> resultMap = updateOrAddOrderTotalSettlementChange(
							settlementPriceChange, orderItem, ordMulPriceRateListCount);
					OrdSettlementPriceRecord record = resultMap.get("record");
					if (null != record) {
						logger.info("sendSettlementRecordChange orderItemId = " + orderItem.getOrderItemId());
						markOverdueTicketSettlementPriceChanged(orderItem,
								OrderEnum.ExpiredRefundState.FINANCE_UNSETTLEMENT.getCode(),
								OrderEnum.ExpiredRefundState.FINANCE_UNSETTLEMENT.getDesc(), isEbkSubOrder);
						informFinancialSystem(orderItem, record, isEbkSubOrder);
					}
				}
			} else if (processStatus.equals(OrderEnum.ExpiredRefundState.FINANCE_UNSETTLEMENT.getCode())) {
				logger.info("sub-order[" + orderItem.getOrderId() + ":" + orderItem.getOrderItemId() + "] "
						+ OrderEnum.ExpiredRefundState.FINANCE_UNSETTLEMENT.getDesc()
						+ ", so try to inform financial system");
				if (buyout) {
					sendOrdSettlementPriceChangeMsg(orderItem.getOrderItemId());
					markOverdueTicketSettlementPriceChanged(orderItem, OrderEnum.ExpiredRefundState.SUCCESS.getCode(),
							OrderEnum.ExpiredRefundState.SUCCESS.getDesc(), isEbkSubOrder);
				} else {
					informFinancialSystem(orderItem,
							regenerateNewSettlementPriceRecord(settlementPriceChange, orderItem), isEbkSubOrder);
				}
			} else {
				logger.info("subOrder[" + orderItem.getOrderId() + ":" + orderItem.getOrderItemId()
						+ "], invalid processStatus[" + processStatus + "]");
			}
		} catch (Exception e) {
			logger.warn("fail to change settlement price of sub-order[" + orderItem.getOrderId() + ":"
					+ orderItem.getOrderItemId() + "] due to error below");
			logger.error(e.getMessage(), e);
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.NOT_SUPPORTED)
	public void markOverdueTicketSettlementPriceChanged(OrdOrderItem orderItem, Integer processStatus, String desc,
			Boolean isEbkSubOrder) {
		OverdueTicketSubOrderStatusPack param = new OverdueTicketSubOrderStatusPack();
		param.getIdList().add(orderItem.getOrderItemId());
		param.setStatus(processStatus);
		param.setDesc(desc);
		logger.info("try to update expired subOrder["
				+ (orderItem != null ? orderItem.getOrderId() + ":" + orderItem.getOrderItemId() : "")
				+ "] refund status to " + processStatus);
		overdueTicketDao.updateStatusInBatch(param);
		logger.info("refund status of expired subOrder["
				+ (orderItem != null ? orderItem.getOrderId() + ":" + orderItem.getOrderItemId() : "")
				+ "] was updated to " + processStatus);

		if (processStatus.equals(OrderEnum.ExpiredRefundState.SUCCESS.getCode())) {
			logger.info("try to update expired subOrder["
					+ (orderItem != null ? orderItem.getOrderId() + ":" + orderItem.getOrderItemId() : "")
					+ "] refunded flag to Y");
			subOrderDao.updateOverdueTicketRefundProcessedFlagAndMemoInBatch(
					Arrays.asList(new Long[] { orderItem.getOrderItemId() }));
			logger.info("refunded flag of expired subOrder["
					+ (orderItem != null ? orderItem.getOrderId() + ":" + orderItem.getOrderItemId() : "")
					+ "] was updated to Y");
			if (isEbkSubOrder) {
				logger.info("try to send expired sub-order refuned msg to EBK");
				orderLocalService.sendExpiredOrderItemRefundedMsgForEbk(orderItem.getOrderItemId());
				logger.info("expired sub-order refuned msg for EBK sent");
			}
		}
	}

	private void informFinancialSystem(OrdOrderItem orderItem, OrdSettlementPriceRecord record, Boolean isEbkSubOrder) {
		ResultHandleT<String> result = sendSettlementRecordChangeRevisedVer(record, orderItem.getOrderItemId());
		if (result.getReturnContent().equals("结算价修改成功") || result.getReturnContent().equals("该订单已经结算过！")) {
			logger.info("new settlement-price[0] of sub-order[" + orderItem.getOrderId() + ":"
					+ orderItem.getOrderItemId()
					+ "] was informed to financial system successfully, mark the process of overdue refund of it complete");
			markOverdueTicketSettlementPriceChanged(orderItem, OrderEnum.ExpiredRefundState.SUCCESS.getCode(),
					OrderEnum.ExpiredRefundState.SUCCESS.getDesc(), isEbkSubOrder);
		}
	}

	private OrdSettlementPriceRecord regenerateNewSettlementPriceRecord(SettlementPriceChange settlementPriceChange,
			OrdOrderItem orderItem) {
		logger.info("try to regenerate settlement price record");
		OrdSettlementPriceRecord newOrdSettlementPriceRecord = new OrdSettlementPriceRecord();
		try {
			if (settlementPriceChange != null && orderItem != null) {
				newOrdSettlementPriceRecord
						.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.TOTAL_PRICE.getCode());
				newOrdSettlementPriceRecord.setOperator("system");
				newOrdSettlementPriceRecord.setCreateTime(new Date());
				newOrdSettlementPriceRecord.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
				newOrdSettlementPriceRecord.setOrderId(orderItem.getOrderId());
				newOrdSettlementPriceRecord.setOrderItemId(orderItem.getOrderItemId());
				newOrdSettlementPriceRecord.setSuppGoodsId(orderItem.getSuppGoodsId());
				newOrdSettlementPriceRecord.setVisitTime(orderItem.getVisitTime());
				newOrdSettlementPriceRecord.setIsApprove("Y");
				newOrdSettlementPriceRecord.setApproveRemark("总价无需审核");
				newOrdSettlementPriceRecord.setReason(settlementPriceChange.getReason());
				newOrdSettlementPriceRecord.setRemark(settlementPriceChange.getRemark());
				newOrdSettlementPriceRecord.setSupplierId(orderItem.getSupplierId());
				logger.info("-------------method updateOrAddOrderTotalSettlementChange 初始化结算变价记录结算价相关-----------");
				// 修改之前的结算单价
				newOrdSettlementPriceRecord.setOldActualSettlementPrice(orderItem.getActualSettlementPrice());
				// 修改之前的结算总价
				newOrdSettlementPriceRecord.setOldTotalSettlementPrice(orderItem.getTotalSettlementPrice());
				logger.info(
						"-------------method updateOrAddOrderTotalSettlementChange resetOrderItem4Settlement-----------");
				// 计算后的单价格
				OrdOrderItem item = resetOrderItem4Settlement(orderItem,
						PriceUtil.convertToFen(settlementPriceChange.getNewTotalSettlementPriceStr()));
				logger.info(
						"-------------method updateOrAddOrderTotalSettlementChange resetOrderItem4Settlement-----------end");
				// 修改之后的结算单价
				newOrdSettlementPriceRecord.setNewActualSettlementPrice(item.getActualSettlementPrice());
				// 修改之后的结算总价
				newOrdSettlementPriceRecord.setNewTotalSettlementPrice(item.getTotalSettlementPrice());
				// 修改价格类型
				newOrdSettlementPriceRecord.setPriceType("PRICE");
				newOrdSettlementPriceRecord.setOperator("SYSTEM");
				String changeResult = OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.UP.getCode();
				if ((newOrdSettlementPriceRecord.getNewTotalSettlementPrice()
						- newOrdSettlementPriceRecord.getOldTotalSettlementPrice()) < 0) {
					changeResult = OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.getCode();
				}
				newOrdSettlementPriceRecord.setChangeResult(changeResult);
			}
		} catch (Exception e) {
			logger.warn("fail to regenerate settlement price record due to error below");
			logger.error(e.getMessage(), e);
		}
		return newOrdSettlementPriceRecord;
	}


	@Override
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public String updateTotalSettlementPrice(Long orderId, Long orderItemId, String reason, String remark,
											 String totalSettlementPrice, String operator) {
		if (orderId == null) {
			return "订单号不能为空";
		}
		if (orderItemId == null) {
			return "子订单号不能为空";
		}
		if(StringUtils.isEmpty(totalSettlementPrice)){
			return "结算总价不能为空";
		}
		Boolean isNumber = totalSettlementPrice.matches("^(-)?[0-9]*$");
		if (!isNumber) {
			return "结算价请传入数字";
		}
		logger.info("-------method updateTotalSettlementPrice 同步子订单总结算价方法---orderId:--" + orderId + "orderItem" + orderItemId + "totalSettlementPrice" + totalSettlementPrice);

		// 查询子订单
		OrdOrderItem orderItem = this.orderUpdateService.getOrderItem(orderItemId);
		if(orderItem == null){
			return "子订单不存在";
		}
		OrdOrderItemExtend extend = ordOrderItemExtendService.selectByPrimaryKey(orderItemId);
		if (extend != null && !"CNY".equalsIgnoreCase(extend.getCurrencyCode())) {
			return updateCurrencyTotalSettlementPrice(orderId,orderItem,reason,remark,totalSettlementPrice,operator,extend);
		}
		if("Y".equals(orderItem.getBuyoutFlag())){
			return updateBuyoutTotalSettlementPrice(orderId,orderItem,reason,remark,totalSettlementPrice,operator);
		}else {
			return updateNormalTotalSettlementPrice(orderId,orderItem,reason,remark,totalSettlementPrice,operator);
		}


	}


	/**
	 * 修改非全买断订单结算价
	 * @param orderId
	 * @param orderItem
	 * @param reason
	 * @param remark
	 * @param totalSettlementPrice
	 * @param operator
	 * @return
	 */
	private String updateNormalTotalSettlementPrice(Long orderId, OrdOrderItem orderItem, String reason, String remark,
													String totalSettlementPrice, String operator){
		try {
			Long oriTotalPrice =orderItem.getTotalSettlementPrice();

			Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
			paramsMulPriceRate.put("orderItemId", orderItem.getOrderItemId());

			String[] priceTypeArray = new String[] {ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.getCode(),
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_34.getCode(),
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.getCode(),
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.getCode() ,
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.getCode(),
					//非买断结算总价
					//ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode() ,
					//ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode(),
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.getCode(),
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_GAP.getCode()};

			paramsMulPriceRate.put("priceTypeArray",priceTypeArray);

			List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
			Long ordMulPriceRateListCount = 0L;
			if(ordMulPriceRateList !=null){
				ordMulPriceRateListCount = Long.valueOf(ordMulPriceRateList.size());
			}

			List<OrdSettlementPriceRecord> list = new ArrayList<OrdSettlementPriceRecord>();
			StringBuffer logContent = new StringBuffer();

			// 查询子订单

			Long buyoutTotalPrice = 0L;
			Long buyNum = orderItem.getBuyoutQuantity();
			buyNum = buyNum==null?0L:buyNum;
			if("Y".equals(orderItem.getBuyoutFlag())){
				buyoutTotalPrice = orderItem.getBuyoutTotalPrice();
				buyoutTotalPrice = buyoutTotalPrice==null?0L:buyoutTotalPrice;
				orderItem.setTotalSettlementPrice(orderItem.getTotalSettlementPrice() - buyoutTotalPrice);

				Long buyoutQuantity = orderItem.getBuyoutQuantity();
				buyoutQuantity = buyoutQuantity==null?0L:buyoutQuantity;
				Long notBuyoutQuantity = orderItem.getQuantity()-buyoutQuantity;
				notBuyoutQuantity = notBuyoutQuantity==0L?1L:notBuyoutQuantity;
				orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/(notBuyoutQuantity));

			}


			OrdSettlementPriceRecord newOrdSettlementPriceRecord = new OrdSettlementPriceRecord();
			newOrdSettlementPriceRecord.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.TOTAL_PRICE.getCode());
			newOrdSettlementPriceRecord.setOperator(operator);
			newOrdSettlementPriceRecord.setCreateTime(new Date());
			newOrdSettlementPriceRecord.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
			newOrdSettlementPriceRecord.setOrderId(orderId);
			newOrdSettlementPriceRecord.setOrderItemId(orderItem.getOrderItemId());
			newOrdSettlementPriceRecord.setSuppGoodsId(orderItem.getSuppGoodsId());
			newOrdSettlementPriceRecord.setVisitTime(orderItem.getVisitTime());
			newOrdSettlementPriceRecord.setIsApprove("Y");
			newOrdSettlementPriceRecord.setApproveRemark("总价无需审核");
			newOrdSettlementPriceRecord.setReason(reason);
			newOrdSettlementPriceRecord.setRemark(remark);
			newOrdSettlementPriceRecord.setSupplierId(orderItem.getSupplierId());

			// 修改之前的结算单价
			newOrdSettlementPriceRecord.setOldActualSettlementPrice(orderItem.getActualSettlementPrice());
			// 修改之前的结算总价
			newOrdSettlementPriceRecord.setOldTotalSettlementPrice(orderItem.getTotalSettlementPrice());

			//计算后的单价格
			OrdOrderItem item = ordSettlementPriceRecordService.resetOrderItem4Settlement(orderItem, Long.valueOf(totalSettlementPrice));

			// 修改之后的结算单价
			newOrdSettlementPriceRecord.setNewActualSettlementPrice(item.getActualSettlementPrice());

			// 修改之后的结算总价
			newOrdSettlementPriceRecord.setNewTotalSettlementPrice(item.getTotalSettlementPrice());
			// 修改价格类型
			newOrdSettlementPriceRecord.setPriceType("PRICE");

			newOrdSettlementPriceRecord.setOperator(operator);
//			newOrdSettlementPriceRecord.setOperatorApprove("system");
			//newOrdSettlementPriceRecord.setUpdateTime(new Date());

			list.add(newOrdSettlementPriceRecord);

			logContent.append("原结算总价：").append(newOrdSettlementPriceRecord.getOldTotalSettlementPrice() / 100.0)
					.append("新结算总价：").append(newOrdSettlementPriceRecord.getNewTotalSettlementPrice() / 100.0);

			if (ordMulPriceRateListCount >= 1) {
				list.clear();
				//计算后的多价格
				ordMulPriceRateList = ordSettlementPriceRecordService.calcSettlementUnitPrice(orderItem.getOrderItemId(), Long.valueOf(totalSettlementPrice), oriTotalPrice);
				if (ordMulPriceRateList != null && ordMulPriceRateList.size() > 0) {
					for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {
						//修改总价的时候记录单价的变更记录，目的是为了取得原始的价格记录
						OrdSettlementPriceRecord ordSettlementPriceRecordObj = new OrdSettlementPriceRecord();
						BeanUtils.copyProperties(newOrdSettlementPriceRecord, ordSettlementPriceRecordObj);
						// 修改之前的结算单价
						ordSettlementPriceRecordObj.setOldActualSettlementPrice(ordMulPriceRate.getOrigPrice());
						// 修改之前的结算总价
						ordSettlementPriceRecordObj.setOldTotalSettlementPrice(ordMulPriceRate.getOrigPrice() * ordMulPriceRate.getQuantity());

						// 修改之后的结算单价,多价格的时候设置修改后的结算单价为null，以此为依据判断历史记录中是否显示
						//ordSettlementPriceRecordObj.setNewActualSettlementPrice(null);
						ordSettlementPriceRecordObj.setPriceType(ordMulPriceRate.getPriceType());

						// 修改之后的结算总价
						//newTotalSettlementPrice = ordSettlementPriceRecordObj.getNewActualSettlementPrice()* ordMulPriceRate.getQuantity();
						//ordSettlementPriceRecordObj.setNewTotalSettlementPrice(newTotalSettlementPrice);

						list.add(ordSettlementPriceRecordObj);
					}
				}
			}

			//处理结算总价的记录
			String changeResult = OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.UP.getCode();
			OrdSettlementPriceRecord record = list.get(0);
			if ((record.getNewTotalSettlementPrice() - record.getOldTotalSettlementPrice()) < 0) {
				changeResult=OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.getCode();
			}
			record.setChangeResult(changeResult);
			//boolean needApprove = isNeedApprove(orderItem, record);
			// 订单结算状态校验 TODO task=115256
			ResultMessage resultMessage = ResultMessage.CHECK_SUCCESS_RESULT;//this.validateOrderSettlementStatus(orderItem);
			//保存结算记录和价格变更记录
			this.ordSettlementPriceRecordService.addSettlementTotalPrice(list, orderId);

			//修改子订单
			//如果含买断的，要将买断的加上去
			if("Y".equals(orderItem.getBuyoutFlag())){
				orderItem.setTotalSettlementPrice(Long.valueOf(totalSettlementPrice) + buyoutTotalPrice);
				orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
			}
			orderItem.setPriceConfirmStatus(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.getCode());
			ResultHandle resultHandle = orderLocalService.updateOrderItem(orderItem);
			if (resultHandle.isSuccess()) {//子订单修改成功再去推送结算价及推送变价单
				//修改多价格记录
				ordSettlementPriceRecordService.updateMulPriceRates(ordMulPriceRateList);
				if (resultMessage.isSuccess()) {//如果是结算完成的订单则不推送结算也不更改订单数据，只记录修改历史和生成变价单
					record.setChangeRemark("0");//结算前修改
					//推送结算
					sendOrdSettlementPriceChangeMsg(record.getOrderItemId());

				} else {
					record.setChangeRemark("1");//结算后修改
				}

				//调用结算系统生成结算单
				record.setChangeFlag("Normal");
				orderSettlementService.insertRecord(record);
			}
			try {
				updateOrdItemPriceConfirm(orderItem.getOrderItemId());
			} catch (Exception e) {
				logger.error(ExceptionFormatUtil.getTrace(e));
			}

			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
					orderId,
					orderItem.getOrderItemId(),
					operator,
					"将编号为["+orderId+"]的子订单，修改子订单结算总价，修改值："+logContent.toString(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName()+"：财务发起订单结算价价格修改申请","");

			String info="";

			if (resultMessage.isSuccess()) {
				info = "结算价修改成功";
			} else {
				info = "该订单已经结算过！";
			}

			return info;

		}catch(Exception e){
			logger.error(ExceptionUtil.getExceptionDetails(e));
		}
		return "操作失败，系统内部异常";
	}

	/**
	 *
	 * @param orderId
	 * @param orderItem
	 * @param reason
	 * @param remark
	 * @param totalSettlementPrice
	 * @param operator
	 * @return
	 */
	private String updateBuyoutTotalSettlementPrice(Long orderId, OrdOrderItem orderItem, String reason, String remark,
													String totalSettlementPrice, String operator){
		try{
			Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
			paramsMulPriceRate.put("orderItemId", orderItem.getOrderItemId());

			String[] priceTypeArray = new String[] {
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.getCode(),
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_34.getCode(),
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.getCode(),
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.getCode() ,
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.getCode(),
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode() ,
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode(),
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.getCode(),
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_GAP.getCode()};

			paramsMulPriceRate.put("priceTypeArray",priceTypeArray);

			List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
			if(ordMulPriceRateList!=null && ordMulPriceRateList.size()>0){
				return "无法修改结算价";
			}
			String change_flag = "BUYOUT";
			String priceModel = "BUDGET_TOTAL_PRICE";


			List<OrdSettlementPriceRecord> list=new ArrayList<OrdSettlementPriceRecord>();
			StringBuffer logContent=new StringBuffer();

			Long oldBuyoutTotalPrice = 0L;
			Long newBuyoutTotalPrice = 0L;

			// 查询子订单
			oldBuyoutTotalPrice = orderItem.getBuyoutTotalPrice();
			oldBuyoutTotalPrice = oldBuyoutTotalPrice==null?0L:oldBuyoutTotalPrice;
			//得到新订单新的结算价
			OrdSettlementPriceRecord newOrdSettlementPriceRecord = getNewOrdSettlementPriceRecord(priceModel,orderItem,reason,remark,Long.valueOf(totalSettlementPrice),operator);

			list.add(newOrdSettlementPriceRecord);

			if(newOrdSettlementPriceRecord.getOldTotalSettlementPrice() != null){
				logContent.append("原非买断结算总价：").append(newOrdSettlementPriceRecord.getOldTotalSettlementPrice() / 100.0);
			}
			if(newOrdSettlementPriceRecord.getOldActualSettlementPrice() != null){
				logContent.append("原非买断结算单价：").append(newOrdSettlementPriceRecord.getOldActualSettlementPrice()/100.0);
			}
			if(newOrdSettlementPriceRecord.getOldBudgetTotalSettlementPrice() != null){
				logContent.append("原买断结算总价：").append(newOrdSettlementPriceRecord.getOldBudgetTotalSettlementPrice()/100.0);
			}
			if(newOrdSettlementPriceRecord.getOldBudgetUnitSettlementPrice() != null){
				logContent.append("原买断结算单价：").append(newOrdSettlementPriceRecord.getOldBudgetUnitSettlementPrice()/100.0);
			}
			if(newOrdSettlementPriceRecord.getNewTotalSettlementPrice() != null){
				logContent.append("新非买断结算总价：").append(newOrdSettlementPriceRecord.getNewTotalSettlementPrice() / 100.0);
			}
			if(newOrdSettlementPriceRecord.getNewActualSettlementPrice() != null){
				logContent.append("新非买断结算单价：").append(newOrdSettlementPriceRecord.getNewActualSettlementPrice()/100.0);
			}
			if(newOrdSettlementPriceRecord.getNewBudgetTotalSettlementPrice() != null){
				logContent.append("新买断结算总价：").append(newOrdSettlementPriceRecord.getNewBudgetTotalSettlementPrice()/100.0);
			}
			if(newOrdSettlementPriceRecord.getNewBudgetUnitSettlementPrice() != null){
				logContent.append("新买断结算单价：").append(newOrdSettlementPriceRecord.getNewBudgetUnitSettlementPrice()/100.0);
			}


			//处理结算总价的记录
			String changeResult = OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.UP.getCode();
			OrdSettlementPriceRecord record = list.get(0);
			Long newBudgetTotalSettlementPrice = record.getNewBudgetTotalSettlementPrice()!=null?record.getNewBudgetTotalSettlementPrice():0;
			Long newTotalSettlementPrice = record.getNewTotalSettlementPrice()!=null?record.getNewTotalSettlementPrice():0;

			Long oldTotalSettlementPrice = record.getOldTotalSettlementPrice()!=null?record.getOldTotalSettlementPrice():0;
			Long oldBudgetTotalSettlementPrice = record.getOldBudgetTotalSettlementPrice()!=null?record.getOldBudgetTotalSettlementPrice():0;

			if ((newBudgetTotalSettlementPrice+newTotalSettlementPrice - oldTotalSettlementPrice-oldBudgetTotalSettlementPrice) < 0) {
				changeResult=OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.getCode();
			}
			record.setChangeResult(changeResult);
			//订单结算状态校验 TODO task=115256
			ResultMessage resultMessage = ResultMessage.CHECK_SUCCESS_RESULT;//this.validateOrderSettlementStatus(orderItem);
			//保存结算记录和价格变更记录
			this.ordSettlementPriceRecordService.addSettlementTotalPrice(list, orderItem.getOrderId());

			orderItem.setPriceConfirmStatus(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.getCode());
			//修改子订单
			ResultHandle resultHandle = orderLocalService.updateOrderItem(orderItem);
			if (resultHandle.isSuccess()) {//子订单修改成功再去推送结算价及推送变价单
				//修改多价格记录
				if (resultMessage.isSuccess()) {//如果是结算完成的订单则不推送结算也不更改订单数据，只记录修改历史和生成变价单

					newBuyoutTotalPrice = orderItem.getBuyoutTotalPrice();
					newBuyoutTotalPrice = newBuyoutTotalPrice==null?0L:newBuyoutTotalPrice;
					if(newBuyoutTotalPrice.longValue() != oldBuyoutTotalPrice.longValue()){
						Date visitDate = orderItem.getVisitTime();
						Long goodsId = orderItem.getSuppGoodsId();
						GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
						long p = Math.min(oldBuyoutTotalPrice, goodsResPrecontrolPolicyVO.getAmount()) - newBuyoutTotalPrice;
						if(goodsResPrecontrolPolicyVO!=null && ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equals(goodsResPrecontrolPolicyVO.getControlType())){
							Long amountId = goodsResPrecontrolPolicyVO.getAmountId();
							Long controlId = goodsResPrecontrolPolicyVO.getId();
							Long leftAmount = goodsResPrecontrolPolicyVO.getLeftAmount();
							logger.info(goodsId+"差价" + p);
							logger.info(goodsId+"剩余金额" + leftAmount);
							Long leftValue = leftAmount+p;
							leftValue = leftValue< 0? 0L:leftValue;
							leftValue = leftValue> goodsResPrecontrolPolicyVO.getAmount()? goodsResPrecontrolPolicyVO.getAmount():leftValue;
							resControlBudgetRemote.updateAmountResPrecontrolPolicy(amountId,controlId, visitDate, leftValue);
							if(leftValue == 0L){
								resControlBudgetRemote.handleResPrecontrolSaledOut(goodsResPrecontrolPolicyVO, orderItem.getVisitTime(), goodsId);
							}
						}

					}

					record.setChangeRemark("0");//结算前修改
					//推送结算
					sendOrdSettlementPriceChangeMsg(record.getOrderItemId());
				} else {
					record.setChangeRemark("1");//结算后修改
				}

				record.setChangeFlag(change_flag);

				//调用结算系统生成结算单
				orderSettlementService.insertRecord(record);
			}

			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
					orderItem.getOrderId(),
					orderItem.getOrderItemId(),
					operator,
					"将编号为["+orderItem.getOrderId()+"]的子订单，修改子订单结算总价，修改值："+logContent.toString(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName()+"：财务发起订单结算价价格修改申请","");

			String info="修改申请已经提交。由于下述原因，需财务审核：a.子订单结算中 b.子订单已结算  c.有负毛利的风险";

			if (resultMessage.isSuccess()) {
				info = "结算价修改成功";
			} else {
				info = "该订单已经结算过！";
			}

			return info;

		}catch(Exception e){
			logger.error(ExceptionUtil.getExceptionDetails(e));
		}
		return "操作失败，系统内部异常";
	}

	/**
	 * 更新外币总实际结算价方法
	 * @param orderId 订单id
	 * @param orderItem 子单
	 * @param reason 修改原因
	 * @param remark 修改备注
	 * @param totalSettlementPrice 修改结算总价
	 * @param operator 操作人
	 * @param orderItemExtend 子单外币记录
	 * @return
	 */
	private String updateCurrencyTotalSettlementPrice(Long orderId, OrdOrderItem orderItem, String reason, String remark,
													String totalSettlementPrice, String operator,OrdOrderItemExtend orderItemExtend){
		try {
			StringBuffer logContent = new StringBuffer();
			String info = null;

			//查询主订单
			OrdOrder order=iOrdOrderService.findByOrderId(orderId);
			Long orderItemId = orderItem.getOrderItemId();
			//财务外币结算总价
			Long newTotalSettlementPrice = Long.valueOf(totalSettlementPrice);

			//设置外币结算单价与总价
			orderItemExtend.setForeignActualSettlementPrice(BigDecimal.valueOf(newTotalSettlementPrice).
					divide(BigDecimal.valueOf(orderItem.getQuantity()), 0, BigDecimal.ROUND_UP).longValue());
			orderItemExtend.setForeignActTotalSettlePrice(newTotalSettlementPrice);

			//汇率
			BigDecimal rate = orderItemExtend.getSettlementPriceRate();

			//结算价修改记录表赋值（保存人民币价格）
			OrdSettlementPriceRecord record = new OrdSettlementPriceRecord();
			record.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.TOTAL_PRICE.getCode());
			record.setOperator(this.getLoginUserId());
			record.setCreateTime(new Date());
			record.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
			record.setOrderId(orderId);
			record.setOrderItemId(orderItemId);
			record.setSuppGoodsId(orderItem.getSuppGoodsId());
			record.setVisitTime(orderItem.getVisitTime());
			record.setIsApprove("Y");
			record.setReason(reason);
			record.setRemark(remark);
			record.setSupplierId(orderItem.getSupplierId());
			record.setApproveRemark("总价无需审核");
			//修改之前的结算单价
			record.setOldActualSettlementPrice(orderItem.getActualSettlementPrice());
			//修改之前的结算总价
			record.setOldTotalSettlementPrice(orderItem.getTotalSettlementPrice());
			//修改之后的结算单价
			record.setNewActualSettlementPrice(com.lvmama.vst.comm.utils.order.PriceUtil.toCNY(orderItemExtend.getForeignActualSettlementPrice(),rate));
			//修改之后的结算总价
			record.setNewTotalSettlementPrice(com.lvmama.vst.comm.utils.order.PriceUtil.toCNY(orderItemExtend.getForeignActTotalSettlePrice(),rate));
			//修改价格类型
			record.setPriceType("PRICE");

			if ( (record.getNewTotalSettlementPrice() - record.getOldTotalSettlementPrice()) < 0) {
				record.setChangeResult(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.getCode());
			} else {
				record.setChangeResult(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.UP.getCode());
			}

			//订单结算状态校验TODO task=115256
			ResultMessage resultMessage = ResultMessage.CHECK_SUCCESS_RESULT;//this.validateOrderSettlementStatus(orderItem);
			if (resultMessage.isSuccess()) {
				//没有结算过的
				record.setChangeRemark("0");
			} else {
				//结算过的
				record.setChangeRemark("1");
			}

			int result = ordSettlementPriceRecordService.saveForeignAfterApprove(record,orderItemExtend);
			//订单修改成功的才做推送结算和生成变价单
			if (result == 1) {
				//订单修改成功并且没有结算完成的，推送结算
				if (resultMessage.isSuccess()) {
					logger.info("Finance currency total sendOrdSettlementPriceChangeMsg1 orderItemId=" + orderItemId);
					sendOrdSettlementPriceChangeMsg(record.getOrderItemId());
				}
				//调用结算系统生成结算单
				logger.info("Finance currency total order_item_id=" + record.getOrderItemId() + " vst_record_id=" + record.getRecordId());
				record.setChangeFlag("Normal");
				if (order.hasPayed() && order.isPayToLvmama() && order.hasInfoAndResourcePass()) {
					logger.info("保存外币变价信息开始");
					orderSettlementService.insertRecord(record);
				}else{
					logger.info( "Finance currency total order:" + order.getOrderId() + " isPayToLvmama:" + order.isPayToLvmama() + " hasPayed status:" + order.hasPayed() + " order status:" + order.getOrderStatus() + ", don't need to settlement!");
				}

			}

			if (resultMessage.isSuccess()) {
				info="结算价修改成功";
			} else {
				info = "该订单已经结算过";
			}

			logContent.append("原结算总价：").append(record.getOldTotalSettlementPrice()/100.0).append("新结算总价：").append(record.getNewTotalSettlementPrice()/100.0);

			updateOrdItemPriceConfirm(orderItemId);

			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
					orderId,
					orderItemId,
					operator,
					"将编号为["+orderItemId+"]的子订单，修改子订单结算总价，修改值："+logContent.toString(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName()+"：财务发起订单结算总价价格修改申请","");

			return info;

		}catch(Exception e){
			logger.error(ExceptionUtil.getExceptionDetails(e));
			return "操作失败，系统内部异常";
		}
	}

	/**
	 * 得到新的结算价
	 * @param priceModel
	 * @param orderItem
	 * @param reason
	 * @param remark
	 * @param settlementPrice
	 * @return
	 */
	private OrdSettlementPriceRecord getNewOrdSettlementPriceRecord(String priceModel,OrdOrderItem orderItem,String reason,String remark,Long settlementPrice,String operate){
		OrdSettlementPriceRecord newOrdSettlementPriceRecord = new OrdSettlementPriceRecord();

		if("BUDGET_UNIT_PRICE".equalsIgnoreCase(priceModel))
			newOrdSettlementPriceRecord.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.BUDGET_UNIT_PRICE.getCode());
		else
			newOrdSettlementPriceRecord.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.BUDGET_TOTAL_PRICE.getCode());
		newOrdSettlementPriceRecord.setOperator(operate);
		newOrdSettlementPriceRecord.setCreateTime(new Date());
		newOrdSettlementPriceRecord.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
		newOrdSettlementPriceRecord.setOrderId(orderItem.getOrderId());
		newOrdSettlementPriceRecord.setOrderItemId(orderItem.getOrderItemId());
		newOrdSettlementPriceRecord.setSuppGoodsId(orderItem.getSuppGoodsId());
		newOrdSettlementPriceRecord.setVisitTime(orderItem.getVisitTime());
		newOrdSettlementPriceRecord.setIsApprove("Y");
		newOrdSettlementPriceRecord.setApproveRemark("总价无需审核");
		newOrdSettlementPriceRecord.setReason(reason);
		newOrdSettlementPriceRecord.setRemark(remark);
		newOrdSettlementPriceRecord.setSupplierId(orderItem.getSupplierId());

		//修改之前买断单价
		newOrdSettlementPriceRecord.setOldBudgetUnitSettlementPrice(orderItem.getBuyoutPrice());
		//修改之前买断总价
		newOrdSettlementPriceRecord.setOldBudgetTotalSettlementPrice(orderItem.getBuyoutTotalPrice());

		if(orderItem.getQuantity()-orderItem.getBuyoutQuantity() != 0){
			//修改之前的非买断总价
			newOrdSettlementPriceRecord.setOldTotalSettlementPrice(orderItem.getTotalSettlementPrice()-orderItem.getBuyoutTotalPrice());
			//修改之前的非买断单价
			newOrdSettlementPriceRecord.setOldActualSettlementPrice(newOrdSettlementPriceRecord.getOldTotalSettlementPrice()/(orderItem.getQuantity()-orderItem.getBuyoutQuantity()));
		}

		//设置子订单结算价
		setCalcSettlementPrice(priceModel,settlementPrice,orderItem);

		//修改之后的买断单价
		newOrdSettlementPriceRecord.setNewBudgetUnitSettlementPrice(orderItem.getBuyoutPrice());
		//修改之后的买断总价
		newOrdSettlementPriceRecord.setNewBudgetTotalSettlementPrice(orderItem.getBuyoutTotalPrice());

		if(orderItem.getQuantity()-orderItem.getBuyoutQuantity() != 0){
			//修改之后的非买断总价
			newOrdSettlementPriceRecord.setNewTotalSettlementPrice(orderItem.getTotalSettlementPrice()-orderItem.getBuyoutTotalPrice());
			//修改之后的非买断单价
			newOrdSettlementPriceRecord.setNewActualSettlementPrice(newOrdSettlementPriceRecord.getNewTotalSettlementPrice()/(orderItem.getQuantity()-orderItem.getBuyoutQuantity()));
		}

		//修改价格类型
		newOrdSettlementPriceRecord.setPriceType("PRICE");

		newOrdSettlementPriceRecord.setOperator(operate);

		return newOrdSettlementPriceRecord;
	}

	//计算结算价
	private void setCalcSettlementPrice(String priceModel,Long settlementPrice,OrdOrderItem orderItem){
		if("BUDGET_UNIT_PRICE".equalsIgnoreCase(priceModel)){
			//修改买断单价
			//设置结算总价
			//先计算非买断的总价(总价-买断总价)
			Long unBudgetTotalPrice = orderItem.getTotalSettlementPrice()-orderItem.getBuyoutTotalPrice();
			logger.info("修改单价非买断总价：" + unBudgetTotalPrice);
			//买断的总价
			Long budgetTotalPrice = settlementPrice*orderItem.getBuyoutQuantity();
			logger.info("修改单价买断总价：" + budgetTotalPrice);
			//设置新的结算总价
			orderItem.setTotalSettlementPrice(unBudgetTotalPrice+budgetTotalPrice);
			//设置结算单价
			orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
			//设置买断结算单价
			orderItem.setBuyoutPrice(settlementPrice);
			//设置买断结算总价
			orderItem.setBuyoutTotalPrice(budgetTotalPrice);
		}else{
			//修改买断总价
			//设置新的结算总价
			//先计算非买断的总价(总价-买断总价)
			Long unBudgetTotalPrice = orderItem.getTotalSettlementPrice()-orderItem.getBuyoutTotalPrice();
			logger.info("修改总价非买断总价：" + unBudgetTotalPrice);
			orderItem.setTotalSettlementPrice(unBudgetTotalPrice+settlementPrice);
			//设置结算单价
			orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
			//设置买断结算总价
			orderItem.setBuyoutTotalPrice(settlementPrice);
			logger.info("修改总价买断总价：" + orderItem.getBuyoutTotalPrice());
			//设置买断结算单价
			orderItem.setBuyoutPrice(settlementPrice/orderItem.getBuyoutQuantity());
		}
	}

	private String reasonType(String reason) {
		if (Constant.ORD_SETTLEMENT_PRICE_RECORD_REASON.getCnName("SET_PRICE_CHANGE").equals(reason)) {
			reason = "SET_PRICE_CHANGE";
		} else if (Constant.ORD_SETTLEMENT_PRICE_RECORD_REASON.getCnName("COST_CHANGE").equals(reason)) {
			reason = "COST_CHANGE";
		} else if (Constant.ORD_SETTLEMENT_PRICE_RECORD_REASON.getCnName("SUPPLIER_DISCOUNT").equals(reason)) {
			reason = "SUPPLIER_DISCOUNT";
		} else if (Constant.ORD_SETTLEMENT_PRICE_RECORD_REASON.getCnName("PRICE_LIST_NO_UPDATE").equals(reason)) {
			reason = "PRICE_LIST_NO_UPDATE";
		} else if (Constant.ORD_SETTLEMENT_PRICE_RECORD_REASON.getCnName("REFUND_SUCCESS").equals(reason)) {
			reason = "REFUND_SUCCESS";
		} else if (Constant.ORD_SETTLEMENT_PRICE_RECORD_REASON.getCnName("SET_BUDGET_PRICE_CHANGE").equals(reason)) {
			reason = "SET_BUDGET_PRICE_CHANGE";
		} else {
			reason = "OTHER";
		}
		return reason;
	}

	@Override
	public void updateForeignSettlementChange(Long foreignTotalSettlementPrice, OrdOrderItem orderItem) {
		logger.info("method updateForeignSettlementChange orderItemId=" + orderItem.getOrderItemId());
		try {
			sendOrdSettlementPriceChangeMsg(orderItem.getOrderItemId());
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM, orderItem.getOrderId(),
					orderItem.getOrderItemId(), "system",
					"将编号为[" + orderItem.getOrderId() + "]的子订单，修改外币结算总价，修改值：" + foreignTotalSettlementPrice,
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName() + "：发起订单外币结算价价格修改申请", "");
		} catch (Exception e) {
			logger.error("updateForeignSettlementChange error:" + e, e);
			logger.error("修改结算价接口异常" + e.getMessage());
			throw new RuntimeException("修改结算价接口异常---" + e.getMessage());
		}
	}

}
