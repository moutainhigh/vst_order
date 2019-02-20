package com.lvmama.vst.order.processer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.lvmama.order.api.base.vo.RequestBody;
import com.lvmama.order.api.base.vo.ResponseBody;
import com.lvmama.order.snap.api.IApiOrderSnapshotService;
import com.lvmama.order.snap.query.vo.SnapshotOrderItemParamVo;
import com.lvmama.order.snap.vo.SnapshotOrderItemVo;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdExpiredRefund;
import com.lvmama.vst.back.order.po.OrdOrderItemView;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.constant.config.DynConfigProp;
import com.lvmama.vst.order.service.IOrdOrderItemService;
import com.lvmama.vst.order.service.OrdExpiredRefundService;
import com.lvmama.vst.supp.client.po.PassProviderPo;
import com.lvmama.vst.supp.client.service.OverdueRefundService;

/**
 * 门票过期退意向消息处理器
 * 
 * <p>
 *  筛选符合条件的过期退意向消息并持久化
 *  1、基础条件数据筛选(SQL)
 *  2、业务逻辑条件校验(SERVICE)
 *  3、过期退意向单持久化(DB)
 * </p>
 */
public class OrderExpiredRefundProcesser implements MessageProcesser {

	private static final Log LOG = LogFactory.getLog(OrderExpiredRefundProcesser.class);
	
	@Autowired
	private IOrdOrderItemService orderService;
	
	@Autowired
	private OrdExpiredRefundService ordExpiredRefundService;
	
	@Autowired
	private SuppSupplierClientService suppSupplierClientService;
	
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
	
	@Resource
	private OverdueRefundService overdueRefundService;
	@Autowired
	private IApiOrderSnapshotService iApiOrderSnapshotService;
	
	@Override
	public void process(Message message) {
		if (MessageUtils.isExpiredRefundMsg(message)) {
			Long orderId = message.getObjectId();
			if (orderId == null || orderId.longValue() <= 0) {
				return;
			}

			LOG.info("Begin: Execute ExpiredRefundMsg, orderId=" + orderId);
			
			try {
			
				/*
				 * 1、基础条件数据筛选
				 *    (1)主订单: TICKET_BU, CategoryID=11/12/13, 主订单状态正常 , 非扫码购  
				 *    (2)子订单: TICKET_BU, CategoryID=11/12/13, 子订单状态正常
				 */
				List<OrdOrderItemView> orderItemList = dataFetch(orderId);
				if (CollectionUtils.isEmpty(orderItemList)) {
					LOG.info("End: Execute ExpiredRefundMsg, no valid data, orderId=" + orderId);
					return;
				}
				
				/* 2、业务逻辑条件校验
				 *    (1)子订单有效期判断
				 *    (2)无线渠道条件判断(开关配置)
				 *    (3)分销订单条件判断(开关配置)
				 *    (4)子订单对应商品支持过期退
				 *    (5)子订单中商品非期票
				 *    (6)可改期的票不做过期退
				 *    (7)对接方式不是传真的都需要处理
				 *    (8)过滤子订单商品不及时通关的
				 *    (9)调用passport接口
				 *        1)需支持废码接口
				 *        2)发码方为驴妈妈
				 *        3)过滤合并申码的订单、过滤部分特殊情况的独立申码(香港迪士尼providerId=62)
				 */
				businessSrvFilter(orderItemList, orderId);
				
				/*
				 * 3、过期退意向单持久化操作
				 */
				if (!CollectionUtils.isEmpty(orderItemList)) {
					
					// 持久化数据对象预处理
					dataPrepare(orderItemList);
					
					// 数据持久化
					dataPersistence(orderItemList);
				}
				
				LOG.info("End: Execute ExpiredRefundMsg, Persistence, orderId=" + orderId + ", orderItemSize=" + orderItemList.size());
			} catch (Exception e) {
				LOG.error("ERROR: Execute ExpiredRefundMsg, orderId=" + orderId, e);
			}
		}
	}
	
	/**
	 * 1、基础条件数据筛选
	 * 
	 * @param orderId Long
	 * @return List<OrdOrderItemView>
	 */
	private List<OrdOrderItemView> dataFetch(Long orderId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", orderId);
		params.put("buCode", CommEnumSet.BU_NAME.TICKET_BU.getCode());
		params.put("categoryIds", Constant.TICKET_CATEGORY_IDS);
		params.put("orderStatus", OrderEnum.ORDER_STATUS.NORMAL.getCode());
		params.put("unDistributorId", Constant.DIST_OFFLINE_EXTENSION);
		params.put("expiredRefundFlagN", Constant.N_FLAG);
		
		return orderService.selectListByParams(params);
	}
	
	/**
	 * 2、业务逻辑条件筛选
	 * 
	 * @param orderItemList List<OrdOrderItemView>
	 * @param orderId Long
	 */
	private void businessSrvFilter(List<OrdOrderItemView> orderItemList, Long orderId) {
		DynConfigProp dynProp = DynConfigProp.getInstance();
		
		// 动态配置: 独立申码中不处理的服务商
		List<Long> unproviderids = dynProp.getErUnProviderIds();
		
		// 动态配置: 无线、分销渠道开关配置
		boolean distchnl = dynProp.getErDistChnl();
        List<Long> distchnlvals = dynProp.getErDistChnlVals();
		
		boolean distcode = dynProp.getErDistCode();
		List<String> distcodevals = dynProp.getErDistChnlCodes();
		
		String logPrefix = "Execute ExpiredRefundMsg, ";
		
		Iterator<OrdOrderItemView> iterator = orderItemList.iterator();
		while (iterator.hasNext()) {
			OrdOrderItemView orderItem = iterator.next();
			Long orderItemId = orderItem.getOrderItemId();
			Long distId = orderItem.getDistributorId();
			Long distChnlVal = orderItem.getDistributionChannel();
			String distCodeVal = orderItem.getDistributorCode();
			
			String logSuffix = String.format(", orderId=%s, orderItemId=%s", orderId, orderItemId);
			
			// (1)子订单有效期判断
			boolean cvtbol = checkValidTime(orderItem, logSuffix);
			if (!cvtbol) {
		        LOG.info("Execute ExpiredRefundMsg, OrderItem-effectiveTime is invalid" + logSuffix);
			    iterator.remove();
				continue;
			}

			// 无线、分销渠道
			boolean disBol = distId != null && Constant.DIST_CHNL_IDS.contains(distId);
			if (disBol) {
				
				// (2)无线渠道条件判断
				if (distchnl && distchnlvals.contains(distChnlVal)) {
					LOG.info(logPrefix + "DISTRIBUTION_CHANNEL(" + distChnlVal + ") is valid" + logSuffix);
					
				// (3)分销渠道条件判断
				} else if (distcode && distcodevals.contains(distCodeVal)) {
					LOG.info(logPrefix + "DISTRIBUTOR_CODE(" + distCodeVal + ") is valid" + logSuffix);
				
				} else {
					LOG.info(logPrefix + "DISTRIBUTION_CHANNEL[" + distchnl + ", " + distChnlVal + "], DISTRIBUTOR_CODE[" + distcode + ", " + distCodeVal
							+ "] is invalid" + logSuffix);
					iterator.remove();
					continue;
				}
			}
			
			// (4)子订单对应商品支持过期退
			boolean ordItemGoodER = orderItem.isExpiredRefund();
			if (!ordItemGoodER) {
				LOG.info("Execute ExpiredRefundMsg, OrdItemGood ExpiredRefund(false) is invalid" + logSuffix);
				iterator.remove();
				continue;
			}
			
			// (5)子订单中商品非期票
			boolean aperiodicFlag = orderItem.hasTicketAperiodic();
			if (aperiodicFlag) {
				LOG.info("Execute ExpiredRefundMsg, OrdItemGood aperiodicFlag(true) is invalid" + logSuffix);
				iterator.remove();
				continue;
			}
			
			// (6)可改期的票不做过期退
			boolean rescheduleFlag = orderItem.isRescheduleFlag();
			if (rescheduleFlag) {
				LOG.info("Execute ExpiredRefundMsg, RescheduleFlag(true) is invalid" + logSuffix);
				iterator.remove();
				continue;
			}
			
			// (7)对接方式不是传真的都需要处理
			boolean faxFlag = orderItem.isFaxFlag();
			if (faxFlag) {
				LOG.info("Execute ExpiredRefundMsg, FaxFlag(true) is invalid" + logSuffix);
				iterator.remove();
				continue;
			}
			
			// (8)过滤子订单商品是不及时通关的
			boolean notIntimeFlag = isNotIntime(orderItem, logSuffix);
			if (notIntimeFlag) {
				LOG.info("Execute ExpiredRefundMsg, NotIntimeFlag(true) is invalid" + logSuffix);
				iterator.remove();
				continue;
			}
			
			// (9)调用passport接口
			ResultHandleT<PassProviderPo> providerResult = overdueRefundService.queryProviderInfo(orderItem.getSuppGoodsId(), orderItem.getVisitTime());
			PassProviderPo passProviderPo = providerResult.getReturnContent();
			
			// 若passProviderPo是null说明不是对接(未绑定)
			if (passProviderPo == null) {
				// 未绑定但是对接不处理
				if (SuppGoods.NOTICETYPE.QRCODE.name().equals(orderItem.getNotifyType())) {
					LOG.info("Execute ExpiredRefundMsg, PassProviderPo(null) and NotifyType(QRCODE) is invalid" + logSuffix);
					iterator.remove();
				    continue;
				}
				
				boolean ebkFlag = orderItem.isEbkFlag();
				LOG.info("Execute ExpiredRefundMsg, PassProviderPo(null) and EbkFlag(" + ebkFlag + ") is valid" + logSuffix);				    
			} else {
				// 9.1)需支持废码接口
				int canDestroy = passProviderPo.getCanDestroy();
				if (canDestroy != 1) {
					LOG.info("Execute ExpiredRefundMsg, canDestroy(" + canDestroy + ") is invalid" + logSuffix);
				    iterator.remove();
				    continue;
				}
				
				// 9.2)发码方为驴妈妈
				int sender = passProviderPo.getSender();
				if (sender != 0) {
					LOG.info("Execute ExpiredRefundMsg, sender(" + sender + ") is invalid" + logSuffix);
				    iterator.remove();
				    continue;
				}
				
				// 9.3)过滤合并申码的订单、过滤部分特殊情况的独立申码(香港迪士尼providerId=62)
				int applyType = passProviderPo.getApplyType();
				if (applyType == 1) {  // 1：独立
                	// 服务商id[providerId]
    				Long providerId = passProviderPo.getProviderId();
    				if (unproviderids.contains(providerId)) {
    					LOG.info("Execute ExpiredRefundMsg, applyType(1), providerId(" + providerId + ") is invalid" + logSuffix);
    				    iterator.remove();
    				    continue;
    				}
				} else {  // 0：合并, 2：两者共存, 3：空
					LOG.info("Execute ExpiredRefundMsg, applyType(" + applyType + ") is invalid" + logSuffix);
				    iterator.remove();
				    continue;
				}  
			}
		}
	}
//	2018-11-22 替换新的有效日期规则判断
//	/**
//	 * 2、(1)子订单有效期判断
//	 *
//	 * @param orderItem OrdOrderItemView
//	 * @param logSuffix String
//	 * @return boolean true-有效, false-无效
//	 */
//	private boolean checkValidTime(OrdOrderItemView orderItem, String logSuffix) {
//
//		Date visitTime = orderItem.getVisitTime();
//		String certValidDayStr = orderItem.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.cert_valid_day.name());
//
//		// 过滤无效数据(正常的有效数据visitTime、certValidDay均不能为空)
//		if (visitTime == null || StringUtils.isEmpty(certValidDayStr)) {
//			LOG.error("Execute ExpiredRefundMsg, CheckValidTime is invalid, " + String.format("visitTime=%s, certValidDay=%s", visitTime, certValidDayStr) + logSuffix);
//			return false;
//		}
//
//		try {
//			// 子订单有效期(有效期：游玩日期+订单快照中商品有效期X(指定游玩日X天内有效,X>1), 当X=1时,有效期为游玩日当天)
//		    Integer certValidDay = Integer.valueOf(certValidDayStr);
//		    Date effectiveTime = null;
//		    if (certValidDay == 1) {
//		    	effectiveTime = visitTime;
//		    } else {
//		    	effectiveTime = DateUtils.addDays(visitTime, certValidDay);
//		    }
//		    orderItem.setEffectiveTime(effectiveTime);
//
//			return true;
//		} catch (Exception e) {
//			LOG.error("Execute ExpiredRefundMsg, CheckValidTime is error" + logSuffix, e);
//			throw e;
//		}
//	}
	/**
	 * 2、(1)子订单有效期判断
	 *
	 * @param orderItem OrdOrderItemView
	 * @param logSuffix String
	 *  有效日期： PERIOD_TIME("按时间段使用"),
	 * 			  VISIT_TIME("按游玩日使用"),
	 * 	          ORDER_TIME("按下单日使用");
	 * @return boolean true-有效, false-无效
	 */
	private boolean checkValidTime(OrdOrderItemView orderItem, String logSuffix) {
		RequestBody<SnapshotOrderItemParamVo> requestBody = new RequestBody<>();
		SnapshotOrderItemParamVo request = new SnapshotOrderItemParamVo();
		request.setOrderItemId(orderItem.getOrderItemId());
		request.setSuppGoodsExp(true);
		requestBody.setT(request);
		ResponseBody<SnapshotOrderItemVo>  responseBody = iApiOrderSnapshotService.findSnapshotOrderItemByParam(requestBody);
		if(responseBody==null||responseBody.isFailure()){
			LOG.info("method:checkValidTime ------ orderItemId:"+orderItem.getOrderItemId()+",findSnapshotOrderItem,return false or null");
			return false;
		}
		SnapshotOrderItemVo snapItemVo = responseBody.getT();
		if(snapItemVo==null){
			LOG.info("method:checkValidTime ------ orderItemId:"+orderItem.getOrderItemId()+",snapItemVo is null");
			return false;
		}
		LOG.info("method:checkValidTime ------orderItemId:"+orderItem.getOrderItemId()+","+JSONUtil.bean2Json(snapItemVo));
		try{
			if(snapItemVo.getUseType().equals("PERIOD_TIME")){
				Date endTime = snapItemVo.getEndTime();
				if(endTime==null){
					return false;
				}
				orderItem.setEffectiveTime(endTime);
			}else if(snapItemVo.getUseType().equals("VISIT_TIME")){
				Date visitDate = orderItem.getVisitTime();
				Short days=snapItemVo.getDays();
				if(days==null||visitDate==null||days==0){
					return false;
				}
				orderItem.setEffectiveTime(DateUtils.addDays(visitDate, days-1));
			}else if(snapItemVo.getUseType().equals("ORDER_TIME")){
				Date createDate = orderItem.getCreateTime();
				Short days=snapItemVo.getDays();
				if(days==null||days==0||createDate==null){
					return false;
				}
				orderItem.setEffectiveTime(DateUtils.addDays(createDate, days-1));
			}else{
				return false;
			}
		} catch (Exception e) {
			LOG.error("method:checkValidTime ------ orderItemId:"+orderItem.getOrderItemId()+",CheckValidTime is error" + logSuffix, e);
			throw e;
		}
		LOG.info("method:checkValidTime ------orderItemId:"+orderItem.getOrderItemId()+", return true");
		return true;
	}

	/**
	 * 2、(7)过滤子订单商品是不及时通关的
	 * 
     * @param orderItem OrdOrderItemView
	 * @param logSuffix String
	 * @return boolean true-不及时通关, false-及时通关
	 */
	private boolean isNotIntime(OrdOrderItemView orderItem, String logSuffix) {
		boolean bol = false;
		try {
			// 供应商: 是否及时通关(Y-不及时, N/NULL-及时)
		    Long supplierId = orderItem.getSupplierId();
			SuppSupplier suppSupplier = suppSupplierClientService.findSuppSupplierById(supplierId).getReturnContent();
			orderItem.setSupplierName(suppSupplier.getSupplierName());
			String notInTimeFlag1 =	suppSupplier.getNotInTimeFlag();
			
			// 商品: 是否及时通关(Y-不及时, N/NULL-及时)
			Long suppGoodsId = orderItem.getSuppGoodsId();
			SuppGoods suppGoods = suppGoodsClientService.findSuppGoodsById(suppGoodsId, true).getReturnContent();
			String notInTimeFlag2 = suppGoods.getNotInTimeFlag();
			
			String notInTimeFlag = Constant.N_FLAG;
			if (Constant.Y_FLAG.equals(notInTimeFlag1)) {
				notInTimeFlag = Constant.Y_FLAG;
			} else if (Constant.Y_FLAG.equals(notInTimeFlag2)) {
				notInTimeFlag = Constant.Y_FLAG;
			}
			
			bol = Constant.Y_FLAG.equals(notInTimeFlag);
			
			LOG.info("Execute ExpiredRefundMsg, NOTINTIME: " 
			    + String.format("SuppSupplier_NotInTimeFlag=%s,SuppGoods_NotInTimeFlag=%s,notInTimeFlag=%s", notInTimeFlag1, notInTimeFlag2, notInTimeFlag) + logSuffix);
		} catch (Exception e) {
			LOG.error("Execute ExpiredRefundMsg, isNotIntime() is error" + logSuffix, e);
			throw e;
		}
		return bol;
	}
	
	/**
	 * 3、1)持久化数据对象预处理
	 * 
	 * @param orderItemList List<OrdOrderItemView>
	 */
	private void dataPrepare(List<OrdOrderItemView> orderItemList) {
		for (OrdOrderItemView orderItem : orderItemList) {
			if (StringUtils.isNotEmpty(orderItem.getSupplierName())) {
				continue;
			}
			
			Long supplierId = orderItem.getSupplierId();
			
			try {
				// 查询供应商名称(冗余字段, 目的:后续分页查询)
				ResultHandleT<SuppSupplier> handleT = suppSupplierClientService.findSuppSupplierById(supplierId);
				SuppSupplier supplier = handleT.getReturnContent();
				if (handleT.isSuccess() && supplier != null) {
					orderItem.setSupplierName(supplier.getSupplierName());
				}
			} catch (Exception e) {
				LOG.error("ERROR: Execute ExpiredRefundMsg, orderItemId=" + orderItem.getOrderItemId() + ", supplierId=" + supplierId, e);
			}
		}
	}

	/**
	 * 3、2)过期退意向单持久化操作
	 * 
	 * @param orderItemList List<OrdOrderItemView>
	 */
	private void dataPersistence(List<OrdOrderItemView> orderItemList) {
		List<OrdExpiredRefund> list = new ArrayList<OrdExpiredRefund>();
		
		for (OrdOrderItemView orderItem : orderItemList) {
			OrdExpiredRefund ordER = new OrdExpiredRefund();
			ordER.setOrderId(orderItem.getOrderId());
			ordER.setOrderItemId(orderItem.getOrderItemId());
			ordER.setSupplierId(orderItem.getSupplierId());
			ordER.setProcessStatus(OrderEnum.ExpiredRefundState.UNPROCESS.getCode());
			ordER.setProcessDesc(OrderEnum.ExpiredRefundState.UNPROCESS.getDesc());
			ordER.setProcessNum(0);
			ordER.setSupplierName(orderItem.getSupplierName());
			ordER.setEffectiveTime(orderItem.getEffectiveTime());
			ordER.setProductName(orderItem.getProductName());
			ordER.setDistributorId(orderItem.getDistributorId());
			list.add(ordER);
		}
		
		ordExpiredRefundService.batchInsert(list);
	}
	
}
