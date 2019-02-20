package com.lvmama.vst.order.job.overdue.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.comm.vo.order.OrdItemResidueRefundInfo;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.PartRefundService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.lvmama.comm.pet.po.pub.TaskResult;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.passport.po.PassPortConstant;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.ebooking.client.ebk.serivce.EbkUserClientService;
import com.lvmama.vst.ebooking.ebk.po.EbkUser;
import com.lvmama.vst.order.dao.OrdExpiredRefundDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.job.overdue.service.OverdueTicketProcessingService;
import com.lvmama.vst.order.job.overdue.service.RefundProcessedOverdueTicketSettlementPriceWrapperService;
import com.lvmama.vst.order.po.OverdueTicketSubOrderStatusPack;
import com.lvmama.vst.supp.client.po.RefundResultPo;
import com.lvmama.vst.supp.client.service.OverdueRefundService;

public class OverdueTicketProcessingServiceImpl implements OverdueTicketProcessingService, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2285655256944246650L;

	private static final Log log = LogFactory.getLog(OverdueTicketProcessingServiceImpl.class);

	@Autowired
	private OrdOrderItemDao subOrderDao;

	@Autowired
	private OrdExpiredRefundDao overdueTicketOrderDao;

	@Autowired
	private OverdueRefundService overdueRefundService;

	@Autowired
	private EbkUserClientService ebkUserClientService;

	@Autowired
	private SuppSupplierClientService suppSupplierClientService;

	@Autowired
	private RefundProcessedOverdueTicketSettlementPriceWrapperService settlementService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private IOrderUpdateService orderUpdateService;

	static private Integer upperLimitOfSupplierIdToUse = new Integer(5);
	static private Integer extractionQuantityFromEachSupplier = new Integer(100);
	static private Integer totalExtractionToTotalFinallyPickUp = new Integer(50);
	static private Integer subOrderIdNum = new Integer(50);

	static private Map<Integer, Boolean> supplierIdNotInTimeFlagMap = new HashMap<Integer, Boolean>();
	
	private List<Long> idListOfSubOrdersToProcess = new ArrayList<Long>();
	
	private boolean processing = false;
	
	private boolean runningInSerial = true;
	
	/*
	 * 过期退意向订单定时处理任务の主要逻辑
	 * 
	 * @param logId 日志ID
	 * 
	 * @param parameter 参数
	 * 
	 * @return 定时任务运行结果
	 * 
	 */
	@Override
	public TaskResult execute(Long logId, String parameter) throws Exception {
		synchronized (this) {
			if (processing) {
				String msg = "another thread is running, pls try another time";
				log.info(msg);
				return generateTaskResult(TaskResult.RUN_STATUS.FAILED, msg);
			} else {
				processing = true;
				log.info("---> processing");
			}
		}

		log.info("try to process overdue ticket with param [" + parameter + "]");
		extractParamAndSet(parameter);

		if (idListOfSubOrdersToProcess != null && idListOfSubOrdersToProcess.size() > 0) {
			log.info("manual mode");
			log.info("runningInSerial -> true");
			return runningInSerial(idListOfSubOrdersToProcess);
		} else {
			log.info("auto mode");
			log.info("runningInSerial -> " + runningInSerial);
			if (runningInSerial) {
				return runningInSerial(null);
			} else {
				return runningInBatch();
			}
		} 
	}

	final static private String KEY_NAME_UPPER_LIMIT_OF_SUPPLIER_ID_TO_USE = "upperLimitOfSupplierIdToUse";
	final static private String KEY_NAME_EXTRACTION_QUANTITY_FROM_EACH_SUPPLIER = "extractionQuantityFromEachSupplier";
	final static private String KEY_NAME_TOTAL_EXTRACTION_TO_TOTAL_FINALLY_PICK_UP = "totalExtractionToTotalFinallyPickUp";
	final static private String KEY_NAME_ID_LIST_OF_SUBORDERS_TO_PROCESS = "idListOfSubOrdersToProcess";
	final static private String KEY_NAME_RUNNING_IN_SERIAL = "runningInSerial";
	final static private String KEY_NAME_SUBORDER_ID_NUM = "subOrderIdNum";

	/**
	 * 解析入参
	 * 
	 * @param paramInStr 入参, 以分号分隔的键值对
	 */
	void extractParamAndSet(String paramInStr) {
		try {
			if (paramInStr == null || paramInStr.trim().equals(""))
				return;

			paramInStr = paramInStr.trim();

			if (paramInStr.startsWith("{") && paramInStr.length() > 1)
				paramInStr = paramInStr.substring(paramInStr.indexOf("{") + 1);

			if (paramInStr.endsWith("}"))
				paramInStr = paramInStr.substring(0, paramInStr.lastIndexOf("}"));

			if (paramInStr.contains(KEY_NAME_ID_LIST_OF_SUBORDERS_TO_PROCESS)) {
				int idxOfKv = paramInStr.indexOf(KEY_NAME_ID_LIST_OF_SUBORDERS_TO_PROCESS);
				int idxOfRightBracket = paramInStr.indexOf("]", idxOfKv);
				if (idxOfRightBracket != -1) {
					int idxOfFollowingComma = paramInStr.indexOf(",", idxOfRightBracket);
					String kV = paramInStr.substring(idxOfKv,
							(idxOfFollowingComma != -1 ? idxOfFollowingComma : paramInStr.length()));
					kV = kV.trim();
					String[] keyAndValue = kV.split(":");
					if (keyAndValue.length == 2) {
						String valueStr = keyAndValue[1].trim();
						if (valueStr.startsWith("[") && valueStr.endsWith("]") && valueStr.length() >= 3) {
							String subOrderIds = valueStr.substring(1, valueStr.length() - 1).trim();
							if (subOrderIds.length() > 0) {
								String[] subOrderIdList = subOrderIds.split(",");
								if (subOrderIdList.length > 0) {
									for (String subOrderId : subOrderIdList) {
										Integer value = convertStrToNum(subOrderId.trim(),
												KEY_NAME_ID_LIST_OF_SUBORDERS_TO_PROCESS);
										if (value > 0)
											idListOfSubOrdersToProcess.add(value.longValue());
									}
								}
							}
						}
					}

					paramInStr = paramInStr.substring(0, idxOfKv)
							+ (idxOfFollowingComma != -1 ? paramInStr.substring(idxOfFollowingComma + 1) : "");
				}
			}

			String[] keyValuePairs = paramInStr.split(",");
			if (keyValuePairs.length < 1)
				return;
			for (String keyValuePair : keyValuePairs) {
				if (keyValuePair == null || keyValuePair.trim().equals(""))
					continue;

				keyValuePair = keyValuePair.trim();
				String[] keyAndValue = keyValuePair.split(":");
				if (keyAndValue.length != 2)
					continue;

				String key = keyAndValue[0].trim();
				String valueStr = keyAndValue[1].trim();

				if (!key.equalsIgnoreCase(KEY_NAME_RUNNING_IN_SERIAL)) {
					Integer value = convertStrToNum(valueStr, key);
					if (value < 1)
						continue;

					if (key.equalsIgnoreCase(KEY_NAME_UPPER_LIMIT_OF_SUPPLIER_ID_TO_USE)) {
						upperLimitOfSupplierIdToUse = value;
					} else if (key.equalsIgnoreCase(KEY_NAME_EXTRACTION_QUANTITY_FROM_EACH_SUPPLIER) && value <= 1000) {
						extractionQuantityFromEachSupplier = value;
					} else if (key.equalsIgnoreCase(KEY_NAME_TOTAL_EXTRACTION_TO_TOTAL_FINALLY_PICK_UP)) {
						totalExtractionToTotalFinallyPickUp = value;
					} else if (key.equalsIgnoreCase(KEY_NAME_SUBORDER_ID_NUM) && value <= 1000) {
						subOrderIdNum = value;
					}
				} else {
					if (valueStr.equalsIgnoreCase("false"))
						runningInSerial = false;
				}
			}
		} catch (Exception e) {
			log.error("fail 2 resolve parameter");
		}
	}
	
	private Integer convertStrToNum(String numberStr, String key) {
		Integer value = new Integer(0);
		try {
			value = Integer.valueOf(numberStr);
		} catch (NumberFormatException e) {
			if (key.equalsIgnoreCase(KEY_NAME_ID_LIST_OF_SUBORDERS_TO_PROCESS)) {
				log.error(numberStr + " NOT A VALID SUBORDER ID");
			} else {
				log.error(key + " NOT A NUMBER");
			}
		}
		return value;
	}
	
	TaskResult runningInSerial(List<Long> subOrderIdList) {
		try {
			// 提取还未处理的或还未完全处理成功的过期退意向单的ID
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("idList", subOrderIdList);
			param.put("rowNum", subOrderIdNum);
			List<OrdExpiredRefund> idAndStatusList = null;
			if (overdueTicketOrderDao != null)
				idAndStatusList = overdueTicketOrderDao.getIdAndStatusOfNotFullyProcessed(param);

			if (idAndStatusList == null || idAndStatusList.size() < 1)
				return generateTaskResultAndResetProcessingStatus(TaskResult.RUN_STATUS.SUCCESS,
						"no sub-order id to fetch");

			Map<Long, Integer> idAndStatusMap = new HashMap<Long, Integer>();
			for (OrdExpiredRefund idAndStatus : idAndStatusList) {
				idAndStatusMap.put(idAndStatus.getOrderItemId(), idAndStatus.getProcessStatus());
			}

			// 根据过期退意向单ID获取子订单
			List<OverdueTicketSubOrder> toProcessList = subOrderDao
					.getSubOrderByIdForOverdueRefundProcessing(new ArrayList<Long>(idAndStatusMap.keySet()));

			if (toProcessList == null || toProcessList.size() < 1)
				return generateTaskResultAndResetProcessingStatus(TaskResult.RUN_STATUS.SUCCESS,
						"no sub-order to process");

			List<OverdueTicketSubOrder> canNotBeProcessed = new ArrayList<OverdueTicketSubOrder>();
			for (OverdueTicketSubOrder toProcess : toProcessList) {
				try {
					if (idAndStatusMap.get(toProcess.getOrderItemId()).equals(0)) {
						log.info("sub-order[" + toProcess.getOrderId() + ":" + toProcess.getOrderItemId()
								+ "] not processed earlier, so process this time");
						
						// 检查信息状态
						if (!checkInfoStatus(toProcess)) {
							log.info("sub-order[" + toProcess.getOrderId() + ":" + toProcess.getOrderItemId()
									+ "] not info passed, ignore this time");
							continue;
						}

						// 检查主订单状态
						if (!checkMainOrderStatus(toProcess)) {
							log.info("sub-order[" + toProcess.getOrderId() + ":" + toProcess.getOrderItemId()
									+ "] not normal or resource not ample can not be process further");
							canNotBeProcessed.add(toProcess);
							continue;
						}

						// 检查子订单状态
						if (!checkSubOrderStatus(toProcess)) {
							log.info("sub-order[" + toProcess.getOrderId() + ":" + toProcess.getOrderItemId()
									+ "] not normal or resource not ample or performed already can not be process further");
							canNotBeProcessed.add(toProcess);
							continue;
						}

						// 对接订单
						if (isApiSubOrder(toProcess)) {
							log.info("sub-order[" + toProcess.getOrderId() + ":" + toProcess.getOrderItemId()
									+ "] is API order");

							// 废码
							ResultHandleT<RefundResultPo> result = destroyApiSubOrder(toProcess.getOrderItemId());
							if (result != null && result.getReturnContent() != null
									&& result.getReturnContent().getStatus() != null) {
								String status = result.getReturnContent().getStatus();
								log.info("sub-order[" + toProcess.getOrderId() + ":" + toProcess.getOrderItemId()
										+ "] destroy status -> " + status);
								if (status.equals(PassPortConstant.PASSCODE_STATUS.DESTROYED_SUCCESS.getCode())) {
									if (updateStateAndFlagInOneShot(
											Arrays.asList(new Long[] { toProcess.getOrderItemId() }),
											OrderEnum.ExpiredRefundState.ORDITEM_UNSETTLEMENT, "P"))
										updateSettlementPriceStepByStep(toProcess,
												OrderEnum.ExpiredRefundState.ORDITEM_UNSETTLEMENT.getCode(), false);
								} else if (status.equals(PassPortConstant.PASSCODE_STATUS.DESTROYED_FAILED.getCode())) {
									updateStateAndFlagInOneShot(
											Arrays.asList(new Long[] { toProcess.getOrderItemId() }),
											OrderEnum.ExpiredRefundState.FAILURE, "P");
								} else if (status.equals(PassPortConstant.PASSCODE_STATUS.DESTROYED_AUDIT.getCode())) {
									updateStateAndFlagInOneShot(
											Arrays.asList(new Long[] { toProcess.getOrderItemId() }),
											OrderEnum.ExpiredRefundState.AUDITING, "P");
								} else {
									log.info("sub-order[" + toProcess.getOrderId() + ":" + toProcess.getOrderItemId()
											+ "] with invalid destory status can not be process further");
									canNotBeProcessed.add(toProcess);
								}
							} else {
								log.info("sub-order[" + toProcess.getOrderId() + ":" + toProcess.getOrderItemId()
										+ "] with no destory status can not be process further");
								canNotBeProcessed.add(toProcess);
							}
							continue;
						}

						// EBK订单 进入意向单表的EBK订单都是及时通关的，所以不再做二次检查
						if (isEbkSubOrder(toProcess)) {
//							log.info("sub-order[" + toProcess.getOrderId() + ":" + toProcess.getOrderItemId()
//									+ "] is EBK order");
							// 如遇EBK可及时通关订单，不发送取消消息给EBK系统，也就是不做任何操作，默认按取消成功执行后续流程
//							if (canPassIntoInTime(toProcess)) {
								log.info("sub-order[" + toProcess.getOrderId() + ":" + toProcess.getOrderItemId()
										+ "] is \"tourist-can-pass-into-in-time\" EBK order");
							if (updateStateAndFlagInOneShot(Arrays.asList(new Long[] { toProcess.getOrderItemId() }),
									OrderEnum.ExpiredRefundState.ORDITEM_UNSETTLEMENT, "P"))
								updateSettlementPriceStepByStep(toProcess,
										OrderEnum.ExpiredRefundState.ORDITEM_UNSETTLEMENT.getCode(), true);
//							} else {
//								log.info("sub-order[" + toProcess.getOrderId() + ":" + toProcess.getOrderItemId()
//										+ "] is not \"tourist-can-pass-into-in-time\" EBK order");
//								canNotBeProcessed.add(toProcess);
//							}
							continue;
						}

						// 其他类型订单不处理
						log.info("sub-order[" + toProcess.getOrderId() + ":" + toProcess.getOrderItemId()
								+ "] is neither api type nor ebk type, can not be process further");
						canNotBeProcessed.add(toProcess);
					} else {
						updateSettlementPriceStepByStep(toProcess, idAndStatusMap.get(toProcess.getOrderItemId()),
								isEbkSubOrder(toProcess));
					}
				} catch (Exception e) {
					log.warn("sub-order[" + toProcess.getOrderId() + ":" + toProcess.getOrderItemId()
							+ "] was failed to be processed due to error below");
					log.error(e.getMessage(), e);
				}
			}

			// 标记不需要处理的意向单的处理状态
			if (canNotBeProcessed != null && canNotBeProcessed.size() > 0)
				if (!markSubOrderCanNotBeProcessed(canNotBeProcessed))
					log.info("fail to mark some overdue tickets unprocessable, wait for next round");
		} catch (Exception e) {
			log.warn("fail to get overdue tickets refunded due to error below");
			log.error(e.getMessage(), e);
			return generateTaskResultAndResetProcessingStatus(TaskResult.RUN_STATUS.FAILED,
					"fail to get overdue tickets refunded due to [" + e.getMessage() + "]");
		}

		return generateTaskResultAndResetProcessingStatus(TaskResult.RUN_STATUS.SUCCESS, null);
	}
	
	TaskResult runningInBatch() {
		List<Integer> supplierIdList = getSupplierIdList();
		if (supplierIdList == null || supplierIdList.isEmpty())
			return generateTaskResultAndResetProcessingStatus(TaskResult.RUN_STATUS.FAILED, "no supplier");

		Set<Integer> supplierIds = extractSupplierIdToUse(supplierIdList);
		if (supplierIds == null || supplierIds.isEmpty())
			return generateTaskResultAndResetProcessingStatus(TaskResult.RUN_STATUS.FAILED,
					"can't extract supplier");

		List<OverdueTicketSubOrder> overdueTicketSubOrderList = getOverdueTicketSubOrderListBySupplierIds(supplierIds);

		if (overdueTicketSubOrderList == null || overdueTicketSubOrderList.isEmpty())
			return generateTaskResultAndResetProcessingStatus(TaskResult.RUN_STATUS.FAILED,
					"can't get overdue ticket by supplier id");

		Map<String, List<OverdueTicketSubOrder>> sortedOverdueTicketSubOrderMap = sortOverdueTicketSubOrderList(
				overdueTicketSubOrderList);
		if (sortedOverdueTicketSubOrderMap == null || sortedOverdueTicketSubOrderMap.isEmpty())
			return generateTaskResultAndResetProcessingStatus(TaskResult.RUN_STATUS.FAILED,
					"can't divide overdue tickets into 'can be processed' and 'can not be processed'");

		List<OverdueTicketSubOrder> needMergingListOfdSubOrdercanNotBeProcesse = null;

		try {
			if (sortedOverdueTicketSubOrderMap.get(CAN_BE_PROCESSED_KEY) != null) {
				Map<String, List<OverdueTicketSubOrder>> subOrderMapSortedByProcessingResult = process(
						sortedOverdueTicketSubOrderMap.get(CAN_BE_PROCESSED_KEY));
				if (subOrderMapSortedByProcessingResult != null) {
					if (subOrderMapSortedByProcessingResult.get(SUCCESS_KEY_OF_SORTED_SUBORDER_MAP) != null) {
						if (updateOverdueTicketSubOrderStatus(
								subOrderMapSortedByProcessingResult.get(SUCCESS_KEY_OF_SORTED_SUBORDER_MAP),
								OrderEnum.ExpiredRefundState.SUCCESS.getCode(),
								OrderEnum.ExpiredRefundState.SUCCESS.getDesc()))
							if (updateOverdueTicketRefundProcessedFlagAndMemoInBatch(
									subOrderMapSortedByProcessingResult
											.get(SUCCESS_KEY_OF_SORTED_SUBORDER_MAP)))
								updateSettlementPrice(subOrderMapSortedByProcessingResult
										.get(SUCCESS_KEY_OF_SORTED_SUBORDER_MAP));
					}
					if (subOrderMapSortedByProcessingResult.get(FAILED_KEY_OF_SORTED_SUBORDER_MAP) != null) {
						updateOverdueTicketSubOrderStatus(
								subOrderMapSortedByProcessingResult.get(FAILED_KEY_OF_SORTED_SUBORDER_MAP),
								OrderEnum.ExpiredRefundState.FAILURE.getCode(),
								OrderEnum.ExpiredRefundState.FAILURE.getDesc());
					}
					if (subOrderMapSortedByProcessingResult.get(AUDITING_KEY_OF_SORTED_SUBORDER_MAP) != null) {
						updateOverdueTicketSubOrderStatus(
								subOrderMapSortedByProcessingResult.get(AUDITING_KEY_OF_SORTED_SUBORDER_MAP),
								OrderEnum.ExpiredRefundState.AUDITING.getCode(),
								OrderEnum.ExpiredRefundState.AUDITING.getDesc());
					}
					if (subOrderMapSortedByProcessingResult
							.get(UNPROCESSABLE_KEY_OF_SORTED_SUBORDER_MAP) != null) {
						needMergingListOfdSubOrdercanNotBeProcesse = subOrderMapSortedByProcessingResult
								.get(UNPROCESSABLE_KEY_OF_SORTED_SUBORDER_MAP);
					}
				}
			}

			if ((sortedOverdueTicketSubOrderMap.get(CAN_NOT_BE_PROCESSED_KEY) != null
					&& !sortedOverdueTicketSubOrderMap.get(CAN_NOT_BE_PROCESSED_KEY).isEmpty())
					|| (needMergingListOfdSubOrdercanNotBeProcesse != null
							&& !needMergingListOfdSubOrdercanNotBeProcesse.isEmpty())) {
				if (sortedOverdueTicketSubOrderMap.get(CAN_NOT_BE_PROCESSED_KEY) == null)
					sortedOverdueTicketSubOrderMap.put(CAN_NOT_BE_PROCESSED_KEY,
							new ArrayList<OverdueTicketSubOrder>());
				sortedOverdueTicketSubOrderMap.get(CAN_NOT_BE_PROCESSED_KEY)
						.addAll(needMergingListOfdSubOrdercanNotBeProcesse);
				markSubOrderCanNotBeProcessed(sortedOverdueTicketSubOrderMap.get(CAN_NOT_BE_PROCESSED_KEY));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return generateTaskResultAndResetProcessingStatus(TaskResult.RUN_STATUS.FAILED, e.getMessage());
		}
		
		return generateTaskResultAndResetProcessingStatus(TaskResult.RUN_STATUS.SUCCESS, null);
	}

	private TaskResult generateTaskResult(TaskResult.RUN_STATUS status, String msg) {
		TaskResult result = new TaskResult();
		result.setRunStatus(status);
		if (msg != null)
			result.setResult(msg);
		return result;
	}
	
	synchronized private TaskResult generateTaskResultAndResetProcessingStatus(TaskResult.RUN_STATUS status, String msg) {
		TaskResult result = new TaskResult();
		result.setRunStatus(status);
		if (msg != null)
			result.setResult(msg);
		processing = false;
		log.info("---> processed");
		return result;
	}

	/**
	 * 获取所有的过期退意向订单的供应商ID
	 * 
	 * @return 供应商ID列表
	 */
	List<Integer> getSupplierIdList() {
		List<Integer> supplierIdList = null;
		if (overdueTicketOrderDao != null) {
			try {
				supplierIdList = overdueTicketOrderDao.getSupplierIdList();
				log.debug("getSupplierIdList -> " + JSON.toJSONString(supplierIdList));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return supplierIdList;
	}

	/**
	 * 随机挑选几个不重复的供应商ID
	 * 
	 * @param supplierIdList
	 * @return
	 */
	Set<Integer> extractSupplierIdToUse(List<Integer> supplierIdList) {
		Set<Integer> supplierIdToUse = null;
		try {
			if (supplierIdList != null && supplierIdList.size() > 0) {
				Integer quantityOfSupplierIdToUse = upperLimitOfSupplierIdToUse;
				if (supplierIdList.size() < upperLimitOfSupplierIdToUse)
					quantityOfSupplierIdToUse = supplierIdList.size();
				Set<Integer> supplierIdExtracted = new HashSet<Integer>();
				for (int i = 0; i < quantityOfSupplierIdToUse; i++) {
					Integer tmpIdx = new Random().nextInt(supplierIdList.size());
					while (supplierIdExtracted.contains(tmpIdx))
						tmpIdx = new Random().nextInt(supplierIdList.size());
					if (supplierIdToUse == null)
						supplierIdToUse = new HashSet<Integer>();
					supplierIdToUse.add(supplierIdList.get(tmpIdx));
					supplierIdExtracted.add(tmpIdx);
				}
			}
			log.debug("extractSupplierIdToUse -> " + supplierIdToUse);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return supplierIdToUse;
	}

	/**
	 * 根据供应商ID获取可能符合过期退条件的子订单列表
	 * 
	 * @param supplierIds
	 *            供应商订单列表
	 * @return 可能符合过期退条件的子订单列表
	 */
	List<OverdueTicketSubOrder> getOverdueTicketSubOrderListBySupplierIds(Set<Integer> supplierIds) {
		List<OverdueTicketSubOrder> overdueTicketSubOrderList = null;

		if (supplierIds == null)
			return overdueTicketSubOrderList;

		if (subOrderDao == null)
			return overdueTicketSubOrderList;

		try {
			List<Integer> supplierIdList = new ArrayList<Integer>();
			supplierIdList.addAll(supplierIds);
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("supplierIdList", supplierIdList);
			paramMap.put("extractionQuantity", extractionQuantityFromEachSupplier);
			log.debug("try to extract " + extractionQuantityFromEachSupplier + " order(s) from " + supplierIds.size()
					+ " supplier(s) each");
			overdueTicketSubOrderList = subOrderDao.getOverdueTicketSubOrderListBySupplierIds(paramMap);
			log.debug("getOverdueTicketSubOrderListBySupplierIds -> " + JSON.toJSONString(overdueTicketSubOrderList));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return overdueTicketSubOrderList;
	}
	
	List<OverdueTicketSubOrder> getOverdueTicketSubOrderListBySpecifiedIds() {
		if (idListOfSubOrdersToProcess == null || idListOfSubOrdersToProcess.size() < 1)
			return null;
		
		if (subOrderDao == null)
			return null;
		
		return subOrderDao.getOverdueTicketSubOrderListBySpecifiedIds(idListOfSubOrdersToProcess);
	}

	final static private String CAN_BE_PROCESSED_KEY = "canBeProcessed";
	final static private String CAN_NOT_BE_PROCESSED_KEY = "canNotBeProcessed";

	/**
	 * 根据过期退条件将订单列表分成可进行过期退操作和不可进行过期退操作两种
	 * 
	 * @param overdueTicketSubOrderList
	 *            可能符合过期退条件的未处理的子订单列表
	 * @return key为处理类型（可过期退|不可过期退），value为子订单列表的map
	 */
	Map<String, List<OverdueTicketSubOrder>> sortOverdueTicketSubOrderList(
			List<OverdueTicketSubOrder> overdueTicketSubOrderList) {
		Map<String, List<OverdueTicketSubOrder>> sortedOverdueTicketSubOrderMap = null;

		if (overdueTicketSubOrderList == null || overdueTicketSubOrderList.size() < 1)
			return sortedOverdueTicketSubOrderMap;

		List<OverdueTicketSubOrder> canBeProcessed = new ArrayList<OverdueTicketSubOrder>();
		List<OverdueTicketSubOrder> canNotBeProcessed = new ArrayList<OverdueTicketSubOrder>();
		sortedOverdueTicketSubOrderMap = new HashMap<String, List<OverdueTicketSubOrder>>();
		sortedOverdueTicketSubOrderMap.put(CAN_BE_PROCESSED_KEY, canBeProcessed);
		sortedOverdueTicketSubOrderMap.put(CAN_NOT_BE_PROCESSED_KEY, canNotBeProcessed);

		for (OverdueTicketSubOrder overdueTicketSubOrder : overdueTicketSubOrderList) {
			try {
				if (checkMainOrderStatus(overdueTicketSubOrder) && checkSubOrder(overdueTicketSubOrder)) {
					sortedOverdueTicketSubOrderMap.get(CAN_BE_PROCESSED_KEY).add(overdueTicketSubOrder);
				} else {
					sortedOverdueTicketSubOrderMap.get(CAN_NOT_BE_PROCESSED_KEY).add(overdueTicketSubOrder);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		log.debug("sortedOverdueTicketSubOrderMap -> " + JSON.toJSONString(sortedOverdueTicketSubOrderMap));

		return sortedOverdueTicketSubOrderMap;
	}
	
	
	boolean checkInfoStatus(OverdueTicketSubOrder overdueTicketSubOrder) {
		if (overdueTicketSubOrder == null)
			return false;

		if (overdueTicketSubOrder.getMainOrderInfoStatus().equals(OrderEnum.INFO_STATUS.INFOPASS.getCode())
				&& overdueTicketSubOrder.getSubOrderInfoStatus().equals(OrderEnum.INFO_STATUS.INFOPASS.getCode()))
			return true;

		return false;
	}

	/**
	 * 检查主订单状态
	 * 
	 * @param overdueTicketSubOrder
	 *            过期退子订单
	 * @return 当主订单状态为【正常】且主订单资源审核状态为【资源满足】时为true，否则为false
	 */
	boolean checkMainOrderStatus(OverdueTicketSubOrder overdueTicketSubOrder) {
		if (overdueTicketSubOrder == null)
			return false;

		if (overdueTicketSubOrder.getMainOrderStatus() == null
				|| !overdueTicketSubOrder.getMainOrderStatus().equals(OrderEnum.ORDER_STATUS.NORMAL.toString()))
			return false;

		if (overdueTicketSubOrder.getMainOrderResourceStatus() == null || !overdueTicketSubOrder
				.getMainOrderResourceStatus().equals(OrderEnum.RESOURCE_STATUS.AMPLE.toString()))
			return false;

		return true;
	}

	/**
	 * 检查子订单
	 * 
	 * @param overdueTicketSubOrder
	 *            过期退子订单
	 * @return 当子订单状态为【正常】且子订单资源审核状态为【资源满足】且子订单履行状态是【未使用】且已过期时为true，否则为false
	 */
	boolean checkSubOrder(OverdueTicketSubOrder overdueTicketSubOrder) {
		if (overdueTicketSubOrder == null)
			return false;

		if (!checkSubOrderStatus(overdueTicketSubOrder))
			return false;

//		if (!checkExpiration(overdueTicketSubOrder))
//			return false;

		return true;
	}

	/**
	 * 检查子订单状态
	 * 
	 * @param overdueTicketSubOrder
	 *            过期退子订单
	 * @return 当子订单状态为【正常】且子订单资源审核状态为【资源满足】且子订单履行状态是【未使用】时为true，否则为false
	 * 		加入无损过滤
	 */
	boolean checkSubOrderStatus(OverdueTicketSubOrder overdueTicketSubOrder) {
		if (overdueTicketSubOrder == null)
			return false;

		if (overdueTicketSubOrder.getSubOrderStatus() == null
				|| !overdueTicketSubOrder.getSubOrderStatus().equals(OrderEnum.ORDER_STATUS.NORMAL.toString()))
			return false;

		if (overdueTicketSubOrder.getSubOrderResourceStatus() == null || !overdueTicketSubOrder
				.getSubOrderResourceStatus().equals(OrderEnum.RESOURCE_STATUS.AMPLE.toString()))
			return false;

		if (overdueTicketSubOrder.getPerformStatus() == null
				|| !overdueTicketSubOrder.getPerformStatus().equals(OrderEnum.PERFORM_STATUS_TYPE.UNPERFORM.toString()))
			return false;
		if(!IsLossless(overdueTicketSubOrder.getOrderItemId()))
			return false;
		return true;
	}

	/**
	 * 效验当前时间子订单是否无损
	 * @param orderItemId
	 * @return
	 */
	private boolean IsLossless(Long orderItemId) {
		OrdOrderItem orderItem = orderUpdateService.getOrderItem(orderItemId);
		Map<String, Object> ordItemRefundInfo = orderService.queryOrdItemRefundInfoForTicket(orderItemId, orderItem.getQuantity());
		if(ordItemRefundInfo==null){
			log.info("method:IsLossless ------ orderItemId:"+orderItemId+",queryOrdItemRefundInfoForTicket return null");
			return false;
		}
		Object lossAmountObject = ordItemRefundInfo.get("lossAmount");
		if(lossAmountObject==null){
			log.info("method:IsLossless ------ orderItemId:"+orderItemId+",lossAmount is null");
			return false;
		}
		Long lossAmount = Long.valueOf(String.valueOf(lossAmountObject));
		if(lossAmount==0){
			log.info("method:IsLossless ------ orderItemId:"+orderItemId+",lossAmount is 0,return true");
			return true;
		}
		return false;
	}

	/**
	 * 根据订单的【游玩日期+有效期】是否小于当前时间来判断是否过期，小于则为过期，然则为否
	 * 
	 * @param overdueTicketSubOrder
	 *            过期退子订单
	 * @return 过期结果为true, 然则为false
	 */
	boolean checkExpiration(OverdueTicketSubOrder overdueTicketSubOrder) {
		if (overdueTicketSubOrder == null)
			return false;

		if (overdueTicketSubOrder.getVisitTime() == null || overdueTicketSubOrder.getContent() == null
				|| overdueTicketSubOrder
						.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.cert_valid_day.toString()) == null)
			return false;

		String certValidDayStr = overdueTicketSubOrder
				.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.cert_valid_day.toString());
		Integer certValidDay = null;
		try {
			certValidDay = Integer.valueOf(certValidDayStr);
		} catch (NumberFormatException e) {
			log.error("cert_valid_day is not a number");
			return false;
		}
		if (certValidDay == null)
			return false;

		try {
			Date visitTime = overdueTicketSubOrder.getVisitTime();
			Long expirationInMillis = visitTime.getTime() + certValidDay * 24 * 3600 * 1000;
			log.info("expirationInMillis -> " + expirationInMillis);
			Long now = System.currentTimeMillis();
			log.info("now -> " + now);
			if (expirationInMillis > now)
				return false;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	final static private String SUCCESS_KEY_OF_SORTED_SUBORDER_MAP = "success";
	final static private String FAILED_KEY_OF_SORTED_SUBORDER_MAP = "failed";
	final static private String AUDITING_KEY_OF_SORTED_SUBORDER_MAP = "auditing";
	final static private String UNPROCESSABLE_KEY_OF_SORTED_SUBORDER_MAP = "unprocessable";

	/**
	 * 随机从订单列表中挑选一些订单，然后根据订单类型进行处理；对接订单尝试废码，EBK订单直接按取消成功处理；最后按处理结果对订单进行分类
	 * 
	 * @param canBeProcessed
	 * @return 按处理结果分类之后的订单列表
	 */
	Map<String, List<OverdueTicketSubOrder>> process(List<OverdueTicketSubOrder> canBeProcessed) {
		Map<String, List<OverdueTicketSubOrder>> subOrderMapSortedByProcessingResult = null;

		if (canBeProcessed == null)
			return subOrderMapSortedByProcessingResult;

		List<OverdueTicketSubOrder> canBeFinallyProcessed = randomPickUpSubOrders(canBeProcessed);
		log.debug("canBeFinallyProcessed -> " + JSON.toJSONString(canBeFinallyProcessed));
		if (canBeFinallyProcessed == null || canBeFinallyProcessed.size() < 1)
			return subOrderMapSortedByProcessingResult;

		subOrderMapSortedByProcessingResult = new HashMap<String, List<OverdueTicketSubOrder>>();
		for (OverdueTicketSubOrder subOrder : canBeFinallyProcessed) {
			try {
				if (isApiSubOrder(subOrder)) {
					log.info("subOrder[" + subOrder.getOrderId() + ":" + subOrder.getOrderItemId() + "] is API order");
					ResultHandleT<RefundResultPo> result = destroyApiSubOrder(subOrder.getOrderItemId());
					if (result != null && result.getReturnContent() != null
							&& result.getReturnContent().getStatus() != null) {
						String status = result.getReturnContent().getStatus();
						log.info("subOrder[" + subOrder.getOrderId() + ":" + subOrder.getOrderItemId() + "] status -> "
								+ status);
						if (status.equals(PassPortConstant.PASSCODE_STATUS.DESTROYED_SUCCESS.getCode())) {
							if (subOrderMapSortedByProcessingResult.get(SUCCESS_KEY_OF_SORTED_SUBORDER_MAP) == null)
								subOrderMapSortedByProcessingResult.put(SUCCESS_KEY_OF_SORTED_SUBORDER_MAP,
										new ArrayList<OverdueTicketSubOrder>());
							subOrderMapSortedByProcessingResult.get(SUCCESS_KEY_OF_SORTED_SUBORDER_MAP).add(subOrder);
						} else if (status.equals(PassPortConstant.PASSCODE_STATUS.DESTROYED_FAILED.getCode())) {
							if (subOrderMapSortedByProcessingResult.get(FAILED_KEY_OF_SORTED_SUBORDER_MAP) == null)
								subOrderMapSortedByProcessingResult.put(FAILED_KEY_OF_SORTED_SUBORDER_MAP,
										new ArrayList<OverdueTicketSubOrder>());
							subOrderMapSortedByProcessingResult.get(FAILED_KEY_OF_SORTED_SUBORDER_MAP).add(subOrder);
						} else if (status.equals(PassPortConstant.PASSCODE_STATUS.DESTROYED_AUDIT.getCode())) {
							if (subOrderMapSortedByProcessingResult.get(AUDITING_KEY_OF_SORTED_SUBORDER_MAP) == null)
								subOrderMapSortedByProcessingResult.put(AUDITING_KEY_OF_SORTED_SUBORDER_MAP,
										new ArrayList<OverdueTicketSubOrder>());
							subOrderMapSortedByProcessingResult.get(AUDITING_KEY_OF_SORTED_SUBORDER_MAP).add(subOrder);
						} else {
							log.info("subOrder[" + subOrder.getOrderId() + ":" + subOrder.getOrderItemId()
									+ "] destroy status[" + result.getReturnContent().getStatus()
									+ "], mark it unprocessable");
							if (subOrderMapSortedByProcessingResult
									.get(UNPROCESSABLE_KEY_OF_SORTED_SUBORDER_MAP) == null)
								subOrderMapSortedByProcessingResult.put(UNPROCESSABLE_KEY_OF_SORTED_SUBORDER_MAP,
										new ArrayList<OverdueTicketSubOrder>());
							subOrderMapSortedByProcessingResult.get(UNPROCESSABLE_KEY_OF_SORTED_SUBORDER_MAP)
									.add(subOrder);
						}
					} else {
						if (subOrderMapSortedByProcessingResult.get(UNPROCESSABLE_KEY_OF_SORTED_SUBORDER_MAP) == null)
							subOrderMapSortedByProcessingResult.put(UNPROCESSABLE_KEY_OF_SORTED_SUBORDER_MAP,
									new ArrayList<OverdueTicketSubOrder>());
						subOrderMapSortedByProcessingResult.get(UNPROCESSABLE_KEY_OF_SORTED_SUBORDER_MAP).add(subOrder);
					}
					continue;
				}
				if (isEbkSubOrder(subOrder)) {
					log.info("subOrder[" + subOrder.getOrderId() + ":" + subOrder.getOrderItemId() + "] is EBK order");
					// 如遇EBK可及时通关订单，不发送取消消息给EBK系统，也就是不做任何操作，默认按取消成功执行后续流程
					if (canPassIntoInTime(subOrder)) {
						log.info("subOrder[" + subOrder.getOrderId() + ":" + subOrder.getOrderItemId()
								+ "] is \"tourist-can-pass-into-in-time\" EBK order");
						if (subOrderMapSortedByProcessingResult.get(SUCCESS_KEY_OF_SORTED_SUBORDER_MAP) == null)
							subOrderMapSortedByProcessingResult.put(SUCCESS_KEY_OF_SORTED_SUBORDER_MAP,
									new ArrayList<OverdueTicketSubOrder>());
						subOrderMapSortedByProcessingResult.get(SUCCESS_KEY_OF_SORTED_SUBORDER_MAP).add(subOrder);
					} else {
						log.info("subOrder[" + subOrder.getOrderId() + ":" + subOrder.getOrderItemId()
								+ "] is not \"tourist-can-pass-into-in-time\" EBK order");
						if (subOrderMapSortedByProcessingResult.get(UNPROCESSABLE_KEY_OF_SORTED_SUBORDER_MAP) == null)
							subOrderMapSortedByProcessingResult.put(UNPROCESSABLE_KEY_OF_SORTED_SUBORDER_MAP,
									new ArrayList<OverdueTicketSubOrder>());
						subOrderMapSortedByProcessingResult.get(UNPROCESSABLE_KEY_OF_SORTED_SUBORDER_MAP).add(subOrder);
					}
					continue;
				}
				log.info("subOrder[" + JSON.toJSONString(subOrder)
						+ "] is neither api type nor ebk type, mark it unprocessable");
				if (subOrderMapSortedByProcessingResult.get(UNPROCESSABLE_KEY_OF_SORTED_SUBORDER_MAP) == null)
					subOrderMapSortedByProcessingResult.put(UNPROCESSABLE_KEY_OF_SORTED_SUBORDER_MAP,
							new ArrayList<OverdueTicketSubOrder>());
				subOrderMapSortedByProcessingResult.get(UNPROCESSABLE_KEY_OF_SORTED_SUBORDER_MAP).add(subOrder);
			} catch (Exception e) {
				log.warn("subOrder[" + subOrder.getOrderId() + ":" + subOrder.getOrderItemId()
						+ "] can't be processed due to error below, ignore");
				log.error(e.getMessage(), e);
			}
		}
		return subOrderMapSortedByProcessingResult;
	}

	final static private Integer DEFAULT_PICK_UP_QUANTITY = 10;

	/**
	 * 随机从订单列表中挑选一个子集
	 * 
	 * @param canBeProcessed
	 *            订单列表
	 * @return 最终被挑选出来的订单的列表
	 */
	List<OverdueTicketSubOrder> randomPickUpSubOrders(List<OverdueTicketSubOrder> canBeProcessed) {
		List<OverdueTicketSubOrder> canBeFinallyProcessed = new ArrayList<OverdueTicketSubOrder>();

		if (canBeProcessed == null || canBeProcessed.size() < 1)
			return canBeFinallyProcessed;

		Integer pickUpQuantity = null;
		try {
			pickUpQuantity = upperLimitOfSupplierIdToUse * extractionQuantityFromEachSupplier
					/ totalExtractionToTotalFinallyPickUp;
		} catch (Exception e) {
			log.warn("fail to calculate pickUpQuantity, use default");
		}

		if (pickUpQuantity == null)
			pickUpQuantity = DEFAULT_PICK_UP_QUANTITY;

		if (pickUpQuantity > canBeProcessed.size())
			pickUpQuantity = canBeProcessed.size();

		log.debug("try to pick up " + pickUpQuantity + " order(s) randomly");
		Set<Integer> idxUsed = new HashSet<Integer>();
		Integer lastSupplierId = new Integer(-1);
		for (int i = 0; i < pickUpQuantity; i++) {
			try {
				lastSupplierId = randomPickUpSubOrder(canBeProcessed, idxUsed, lastSupplierId, canBeFinallyProcessed);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		return canBeFinallyProcessed;
	}

	/**
	 * 随机从订单列表中挑选一个订单，如果索引值已经被使用或当前供应商ID与上一次重复的话则递归执行
	 * 
	 * @param canBeProcessed
	 *            订单列表
	 * @param idxUsed
	 *            使用过的索引值集合
	 * @param lastSupplierId
	 *            上一个供应商ID
	 * @param canBeFinallyProcessed
	 *            最终被挑选出来的订单的列表
	 * @return 当前被选中的订单的供应商ID
	 */
	Integer randomPickUpSubOrder(List<OverdueTicketSubOrder> canBeProcessed, Set<Integer> idxUsed,
			Integer lastSupplierId, List<OverdueTicketSubOrder> canBeFinallyProcessed) {
		if (lastSupplierId == null)
			lastSupplierId = new Integer(-1); // 消除lastSupplierId的二义性，默认值只能为-1

		if (canBeProcessed == null || canBeProcessed.size() < 1)
			return lastSupplierId;

		if (idxUsed == null)
			return lastSupplierId;

		if (canBeFinallyProcessed == null)
			return lastSupplierId;

		Integer idx = new Random().nextInt(canBeProcessed.size());
		while (idxUsed.contains(idx)) {
			idx = new Random().nextInt(canBeProcessed.size());
		}

		OverdueTicketSubOrder subOrder = canBeProcessed.get(idx);
		if (subOrder == null)
			return lastSupplierId;

		if (subOrder.getSupplierId() != null && subOrder.getSupplierId().equals(lastSupplierId)) {
			return randomPickUpSubOrder(canBeProcessed, idxUsed, lastSupplierId, canBeFinallyProcessed);
		} else {
			canBeFinallyProcessed.add(subOrder);
			idxUsed.add(idx);
			return subOrder.getSupplierId();
		}
	}

	Boolean isApiSubOrder(OverdueTicketSubOrder subOrder) {
		Boolean isApiSubOrder = new Boolean(false);
		try {
			if (subOrder != null) {
				isApiSubOrder = subOrder.isApiType();
			}
			log.debug((subOrder != null ? "subOrder[" + JSON.toJSONString(subOrder) + "] is " : "")
					+ (isApiSubOrder ? "" : "not " + "API order"));
		} catch (Exception e) {
			log.warn("can't recognize the type of sub-order due to error below");
			log.error(e.getMessage(), e);
		}
		return isApiSubOrder;
	}

	/**
	 * 废码
	 * 
	 * @param orderItemId
	 *            子订单号
	 * @return 废码状态
	 */
	ResultHandleT<RefundResultPo> destroyApiSubOrder(Long orderItemId) {
		if (orderItemId == null)
			return null;

		if (overdueRefundService == null)
			return null;

		try {
			log.debug("try to invoke supplier API to destory order...");
			return overdueRefundService.doDestroy(orderItemId, null);
		} catch (Exception e) {
			log.warn("failed to destroy subOrder[" + orderItemId + "] due to error below");
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 判断是否是EBK订单
	 * 
	 * @param subOrder
	 *            子订单
	 * @return
	 */
	Boolean isEbkSubOrder(OverdueTicketSubOrder subOrder) {
		Boolean isEbkSubOrder = new Boolean(false);
		if (subOrder != null && ebkUserClientService != null) {
			try {
				Map<String, Object> paramUser = new HashMap<String, Object>();
				paramUser.put("cancelFlag", "Y");
				paramUser.put("supplierId", subOrder.getSupplierId());
				ResultHandleT<List<EbkUser>> result = ebkUserClientService.getEbkUserList(paramUser);
				if (result != null && result.getReturnContent() != null && !result.getReturnContent().isEmpty())
					isEbkSubOrder = new Boolean(true);
				log.debug((subOrder != null ? "subOrder[" + JSON.toJSONString(subOrder) + "] is " : "")
						+ ((isEbkSubOrder ? "" : "not ") + "EBK order"));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return isEbkSubOrder;
	}

	/**
	 * 检查EBK订单是否可以及时通关
	 * 
	 * @param subOrder
	 *            子订单
	 * @return
	 */
	@Deprecated
	Boolean canPassIntoInTime(OverdueTicketSubOrder subOrder) {
		Boolean canPassIntoInTime = new Boolean(false);
		try {
			if (subOrder != null) {
				if (subOrder.getNotInTimeFlag() == null) {
					if (subOrder.getSupplierId() != null) {
						if (supplierIdNotInTimeFlagMap.get(subOrder.getSupplierId()) == null) {
							if (suppSupplierClientService != null) {
								ResultHandleT<SuppSupplier> result = suppSupplierClientService
										.findSuppSupplierById(subOrder.getSupplierId().longValue());
								if (result != null && result.getReturnContent() != null
										&& result.getReturnContent().getNotInTimeFlag() != null
										&& result.getReturnContent().getNotInTimeFlag().equals("N")) {
									canPassIntoInTime = new Boolean(true);
									supplierIdNotInTimeFlagMap.put(subOrder.getSupplierId(), canPassIntoInTime);
								}
							}
						} else {
							canPassIntoInTime = supplierIdNotInTimeFlagMap.get(subOrder.getSupplierId());
						}
					} else {
						log.info("subOrder[" + subOrder.getOrderId() + ":" + subOrder.getOrderItemId()
								+ "] has no not-in-time flag or can't get the flag by null supplier id");
					}
				} else {
					// 如果flag的值为Y或不为N的其他字符的话，认为订单是不可及时通关
					if (subOrder.getNotInTimeFlag().equals("N"))
						canPassIntoInTime = new Boolean(true);
				}
			}
			log.debug((subOrder != null
					? "tourist with subOrder[" + JSON.toJSONString(subOrder) + "] can " : "")
					+ (canPassIntoInTime ? "" : "not " + "pass into in time"));			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return canPassIntoInTime;
	}

	/**
	 * 更新过期退意向订单的状态
	 * 
	 * @param subOrderList
	 *            过期退意向订单列表
	 * @param status
	 *            状态
	 * @return
	 */
	@Deprecated
	Boolean updateOverdueTicketSubOrderStatus(List<OverdueTicketSubOrder> subOrderList, int status, String desc) {
		if (subOrderList == null || subOrderList.isEmpty())
			return false;

		if (overdueTicketOrderDao == null)
			return false;

		try {
			OverdueTicketSubOrderStatusPack param = new OverdueTicketSubOrderStatusPack();
			param.setStatus(status);
			param.setDesc(desc);
			for (OverdueTicketSubOrder subOrder : subOrderList) {
				param.getIdList().add(subOrder.getOrderItemId());
			}
			log.info("subOrderList -> " + param.getIdList() + ", update status to " + status);
			int updatedNum = overdueTicketOrderDao.updateStatusInBatch(param);
			log.info(updatedNum + " subOrder(s) updated");
			return true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * 更新子订单过期退处理结果标志位和备注（结算的时候提供参考）
	 * 
	 * @param subOrderList
	 *            子订单列表
	 */
	@Deprecated
	Boolean updateOverdueTicketRefundProcessedFlagAndMemoInBatch(List<OverdueTicketSubOrder> subOrderList) {
		try {
			log.debug("subOrderList -> " + JSON.toJSONString(subOrderList)
					+ ", update flag -> processedYes, memo -> 过期退");

			if (subOrderList == null)
				return false;

			List<Long> subOrderIdList = new ArrayList<Long>();
			for (OverdueTicketSubOrder subOrder : subOrderList) {
				subOrderIdList.add(subOrder.getOrderItemId());
			}

			if (subOrderIdList.size() < 1)
				return false;

			if (subOrderDao == null)
				return false;

			int updatedNum = subOrderDao.updateOverdueTicketRefundProcessedFlagAndMemoInBatch(subOrderIdList);
			log.info(updatedNum + " subOrder(s) updated");
			return true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	@Deprecated
	boolean updateSettlementPrice(List<OverdueTicketSubOrder> subOrderList) {
		log.info("update settlement price to zero");

		if (subOrderList == null || subOrderList.size() < 1)
			return false;

		if (settlementService == null)
			return false;

		try {
			if (settlementService.setSettlementPriceToZeroInBatch(subOrderList)) {
				log.info("update settlement price sucessfully updated to zero");
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		log.info("fail to set settlement price to zero");
		return false;
	}
	
	void updateSettlementPriceStepByStep(OverdueTicketSubOrder subOrder, Integer processStatus, Boolean isEbkSubOrder) {
		if (settlementService != null)
			settlementService.setSettlementPriceToZero(subOrder, processStatus, isEbkSubOrder);
	}

	final static private String OVERDUE_TICKET_SETTLEMENT_PRICE_UPDATED_MEMO = "【过期退】结算价格置为0";

	@Deprecated
	boolean updateSubOrderMemo(OrdOrderItem subOrder) {
		try {
			log.debug("updateSubOrderMemo subOrder -> " + JSON.toJSONString(subOrder));

			if (subOrder == null || subOrder.getOrderItemId() == null)
				return false;

			if (subOrderDao == null)
				return false;

			if (subOrder.getOrderMemo() == null)
				subOrder.setOrderMemo("");

			StringBuilder memo2Update = new StringBuilder(subOrder.getOrderMemo());
			if (!memo2Update.toString().equals("") && !memo2Update.toString().endsWith("\n")) {
				memo2Update.append("\n");
			}
			memo2Update.append(OVERDUE_TICKET_SETTLEMENT_PRICE_UPDATED_MEMO);

			OrdOrderItem subOrder2Update = new OrdOrderItem();
			subOrder2Update.setOrderItemId(subOrder.getOrderItemId());
			subOrder2Update.setOrderMemo(memo2Update.toString());
			subOrderDao.updateByPrimaryKeySelective(subOrder2Update);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
		return true;
	}
	
	/**
	 * 供外部调用，使用场景有【人工废码成功】、【废码状态由审核中变为成功】等
	 * 
	 * @param subOrderId 子订单号
	 * @return 
	 */
	@Transactional(readOnly = false, propagation = Propagation.NOT_SUPPORTED)
	public Boolean updateOverdueTicketSubOrderInOneShot(Long subOrderId) {
		log.info("try to update overdue ticket sub-order[" + subOrderId + "] refund processed status and flag");

		if (overdueTicketOrderDao == null)
			return false;

		if (subOrderDao == null)
			return false;

		OverdueTicketSubOrderStatusPack param = new OverdueTicketSubOrderStatusPack();
		param.setStatus(OrderEnum.ExpiredRefundState.ORDITEM_UNSETTLEMENT.getCode());
		param.setDesc(OrderEnum.ExpiredRefundState.ORDITEM_UNSETTLEMENT.getDesc());
		param.getIdList().add(subOrderId);
		overdueTicketOrderDao.updateStatusInBatch(param);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("flag", "P");
		paramMap.put("idList", Arrays.asList(new Long[] { subOrderId }));
		subOrderDao.updateOverdueTicketRefundProcessedFlag(paramMap);
			
		log.info("overdue ticket sub-order[" + subOrderId + "] refund processed status and flag successfully updated");
		return true;
	}
	
	@Transactional(readOnly = false, propagation = Propagation.NOT_SUPPORTED)
	Boolean updateStateAndFlagInOneShot(List<Long> subOrderIdList, OrderEnum.ExpiredRefundState state, String flag) {
		log.info("try to update overdue ticket sub-order(s) " + subOrderIdList + " refund processed status to ["
				+ state.getDesc() + "] and flag to [" + flag + "]");

		if (overdueTicketOrderDao == null)
			return false;

		if (subOrderDao == null)
			return false;

		OverdueTicketSubOrderStatusPack param = new OverdueTicketSubOrderStatusPack();
		param.setStatus(state.getCode());
		param.setDesc(state.getDesc());
		param.getIdList().addAll(subOrderIdList);
		overdueTicketOrderDao.updateStatusInBatch(param);

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("flag", flag);
		paramMap.put("idList", subOrderIdList);
		subOrderDao.updateOverdueTicketRefundProcessedFlag(paramMap);

		log.info("overdue ticket sub-order(s) " + subOrderIdList
				+ " refund processed status and flag successfully updated to [" + state.getDesc() + "] and [" + flag
				+ "] respectively");
		return true;
	}

	/**
	 * 把过期退意向订单的状态置为【不需要处理】
	 * 
	 * @param canNotBeProcessed
	 *            不需要被处理的过期退意向订单列表
	 */
	Boolean markSubOrderCanNotBeProcessed(List<OverdueTicketSubOrder> canNotBeProcessed) {
		if (canNotBeProcessed == null || canNotBeProcessed.size() < 1)
			return false;

		List<Long> subOrderIdList = new ArrayList<Long>();
		for (OverdueTicketSubOrder o : canNotBeProcessed) {
			subOrderIdList.add(o.getOrderItemId());
		}

		Boolean result = false;

		try {
			result = updateStateAndFlagInOneShot(subOrderIdList, OrderEnum.ExpiredRefundState.UNNEEDED, "N");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return result;
	}

	public OrdOrderItemDao getSubOrderDao() {
		return subOrderDao;
	}
	
	public void setSubOrderDao(OrdOrderItemDao subOrderDao) {
		this.subOrderDao = subOrderDao;
	}
	
	public OrdExpiredRefundDao getOverdueTicketOrderDao() {
		return overdueTicketOrderDao;
	}

	public void setOverdueTicketOrderDao(OrdExpiredRefundDao overdueTicketOrderDao) {
		this.overdueTicketOrderDao = overdueTicketOrderDao;
	}

	public static Integer getUpperLimitOfSupplierIdToUse() {
		return upperLimitOfSupplierIdToUse;
	}

	public static Integer getExtractionQuantityFromEachSupplier() {
		return extractionQuantityFromEachSupplier;
	}

	public static Integer getTotalExtractionToTotalFinallyPickUp() {
		return totalExtractionToTotalFinallyPickUp;
	}
	
	public static Integer getSubOrderIdNum() {
		return subOrderIdNum;
	}	
	
	List<Long> getIdListOfSubOrdersToProcess() {
		return idListOfSubOrdersToProcess;
	}

	boolean isRunningInSerial() {
		return runningInSerial;
	}
}
