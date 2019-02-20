package com.lvmama.vst.neworder.order.api.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.lvmama.order.api.base.vo.BusinessException;
import com.lvmama.order.api.base.vo.RequestBody;
import com.lvmama.order.api.base.vo.ResponseBody;
import com.lvmama.order.comm.utils.ResponseBodyCreator;
import com.lvmama.order.comm.utils.StringUtil;
import com.lvmama.order.comm.validation.ApiValidation;
import com.lvmama.order.enums.ApiEnum;
import com.lvmama.order.enums.OrderEnum.AUDIT_OBJECT_TYPE;
import com.lvmama.order.enums.OrderEnum.AUDIT_SUB_TYPE;
import com.lvmama.order.enums.OrderEnum.AUDIT_TYPE;
import com.lvmama.order.vo.comm.audit.ComAuditVo;
import com.lvmama.order.vst.api.common.service.IApiOrderService;
import com.lvmama.order.vst.api.common.vo.request.OrderOperateVo;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.order.service.impl.OrderEcontractGeneratorService;
import com.lvmama.order.vst.api.common.vo.request.ComMessageVo;
import com.lvmama.order.vst.api.common.vo.request.CreateItemTaskVo;
import com.lvmama.order.vst.api.common.vo.request.OrderDeductAmountVo;
import com.lvmama.order.vst.api.common.vo.request.SaveReservationVo;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.pub.service.ComLogClientService;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.order.client.ord.service.impl.OrdOrderClientServiceImpl;
import com.lvmama.vst.order.service.IComMessageService;
import com.lvmama.vst.order.service.IOrderUpdateService;

@Component("apiOrderService")
public class ApiOrderServiceImpl implements IApiOrderService {

	private static final Log LOG = LogFactory.getLog(ApiOrderServiceImpl.class);

	
	@Autowired
	private OrderService orderService;
	@Resource
	private IComMessageService comMessageService;
	@Resource
	private IOrderUpdateService orderUpdateService;
	@Resource
	private ComLogClientService comLogClientService;
	
	@Autowired
	private OrderEcontractGeneratorService orderEcontractGeneratorService;
	
	@Override
	public ResponseBody newOrderAuditReceiveTaskMessage(RequestBody<Long> request) {
		try{
			LOG.info("newOrderAuditReceiveTaskMessage with " + StringUtil.toSafeString(request));

			//检查非空
			BusinessException businessException = ApiValidation.checkNotNull(request);
			if(businessException != null) {
	        	return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.OTHER.getCode(), request.toString(), "创建订单审核记录失败,所传参数为空!");
	        }
			
			//检查订单号
			Long orderId=request.getT();
			if(orderId == null) {
				return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.OTHER.getCode(), request.toString(), "创建订单审核记录失败,所传订单号为空!");
			}
			
			orderService.newOrderAuditReceiveTaskMessage(orderId);
			return ResponseBodyCreator.success(null);
		}catch(Exception e){
			return ResponseBodyCreator.exception(null, ApiEnum.BUSSINESS_CODE.SYSTEM_INTERNAL_ERROR.getCode(), null, "创建订单审核记录内部错误", e, StringUtil.toSafeString(request), ApiEnum.BUSSINESS_TAG.ORD_ORDER.name());
		}
	}
	
	public ResponseBody generateEcontract(RequestBody<OrderOperateVo> request){
		//检查非空
		BusinessException businessException = ApiValidation.checkNotNull(request);
		if(businessException != null) {
        	return ResponseBodyCreator.error(null, ApiEnum.ORDER_BUSSINESS_CODE.OTHER.getCode(), request.toString(), "生成合同失败,所传参数为空!");
        }
		
		OrderOperateVo orderOperateVo = request.getT();
		ResultHandle  resultHandle = orderEcontractGeneratorService.generateEcontract(orderOperateVo.getOrderId(), orderOperateVo.getOperatorName());
		if(resultHandle != null && resultHandle.isSuccess()){
			LOG.info("ApiOrderServiceImpl.generateEcontract success!orderId:"+orderOperateVo.getOrderId());
			return ResponseBodyCreator.success(null);
		}
		LOG.error("orderEcontractGeneratorService.generateEcontract error!orderId:"+orderOperateVo.getOrderId());
		return ResponseBodyCreator.error(null, ApiEnum.ORDER_BUSSINESS_CODE.OTHER.getCode(), request.toString(), "生成合同失败!");
	}
	
	public ResponseBody financeGenerateEcontract(RequestBody<OrderOperateVo> request){
		//检查非空
		BusinessException businessException = ApiValidation.checkNotNull(request);
		if(businessException != null) {
        	return ResponseBodyCreator.error(null, ApiEnum.ORDER_BUSSINESS_CODE.OTHER.getCode(), request.toString(), "生成合同失败,所传参数为空!");
        }
		
		OrderOperateVo orderOperateVo = request.getT();
		ResultHandle  resultHandle = orderEcontractGeneratorService.generateFinanceEcontract(orderOperateVo.getOrderId(), orderOperateVo.getOperatorName());
		if(resultHandle != null && resultHandle.isSuccess()){
			LOG.info("ApiOrderServiceImpl.generateEcontract success!orderId:"+orderOperateVo.getOrderId());
			return ResponseBodyCreator.success(null);
		}
		LOG.error("orderEcontractGeneratorService.generateEcontract error!orderId:"+orderOperateVo.getOrderId()+resultHandle.getMsg());
		return ResponseBodyCreator.error(null, ApiEnum.ORDER_BUSSINESS_CODE.OTHER.getCode(), request.toString(), "生成合同失败!");
	}


	@Override
	public ResponseBody saveReservation(RequestBody<SaveReservationVo> request) {

		BusinessException businessException = ApiValidation.checkNotNull(request);
		if (businessException != null) {
			return ResponseBodyCreator.exception(ApiEnum.ORDER_BUSSINESS_CODE.OTHER, ApiEnum.ORDER_ERROR_CODE.OTHER, "预订通知,所传参数为空!", businessException.getMessage(), businessException, null, ApiEnum.BUSSINESS_TAG.ORD_ORDER.name());
		}

		SaveReservationVo saveReservationVo = request.getT();
		ComMessageVo comMessage = saveReservationVo.getComMessage();
		Long objectId = saveReservationVo.getObjectId();
		String objectType = saveReservationVo.getObjectType();//AUDIT_OBJECT_TYPE
		String subType = saveReservationVo.getSubType();//AUDIT_SUB_TYPE
		String type = saveReservationVo.getType();//AUDIT_TYPE

		OrdOrderItem orderItem = null;
		try {
			Assert.hasText(comMessage.getMessageContent());
			if (StringUtils.isNotEmpty(comMessage.getSender())) {
				comMessage.setSender("SYSTEM");
			}
			if (type == null) {
				type = AUDIT_TYPE.RESOURCE_AUDIT.name();
			}
			ComMessage comMessage1 = new ComMessage();
			EnhanceBeanUtils.copyProperties(comMessage, comMessage1);

			if (OrderEnum.AUDIT_OBJECT_TYPE.ORDER.equals(objectType)) {
				comMessageService.saveReservation(comMessage1, type, subType, objectId, "system", null);
			} else {
				orderItem = orderUpdateService.getOrderItem(objectId);
				if (orderItem != null) {
					comMessageService.saveReservationChildOrder(comMessage1, type, subType, orderItem.getOrderId(), objectId, "system", null);
				}
			}
			return ResponseBodyCreator.success(null);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			String content = null;
			if (Constants.NO_PERSON.equals(e.getMessage())) {

				content = "找不到分单人，创建预定通知失败";
				if (OrderEnum.AUDIT_OBJECT_TYPE.ORDER.equals(objectType)) {
					this.comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER, objectId, objectId, comMessage.getSender(), content, ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_CHANGE.name(),
							ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_CHANGE.getCnName() + "[" + content + "]", content);
				} else {
					this.comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM, orderItem.getOrderId(), objectId, comMessage.getSender(), content, ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_CHANGE.name(),
							ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_CHANGE.getCnName() + "[" + content + "]", content);

				}
			} else {
				content = e.getMessage();
			}
			return ResponseBodyCreator.exception(ApiEnum.ORDER_BUSSINESS_CODE.OTHER, ApiEnum.ORDER_ERROR_CODE.OTHER, e, objectId, ApiEnum.BUSSINESS_TAG.ORD_ORDER.name());
		}
	}

	
	@Override
	public ResponseBody<ComAuditVo> createItemTask(RequestBody<CreateItemTaskVo> request) {
		BusinessException businessException = ApiValidation.checkNotNull(request);
		if (businessException != null) {
			return ResponseBodyCreator.exception(ApiEnum.ORDER_BUSSINESS_CODE.OTHER, ApiEnum.ORDER_ERROR_CODE.OTHER, "创建子订单活动,所传参数为空!", businessException.getMessage(), businessException, null, ApiEnum.BUSSINESS_TAG.ORD_ORDER.name());
		}
		CreateItemTaskVo createItemTaskVo = request.getT();
		Long objectId = createItemTaskVo.getObjectId();
		String auditType = createItemTaskVo.getAuditType();
		try {
			com.lvmama.vst.back.order.po.OrderEnum.AUDIT_TYPE type = null;
			for (com.lvmama.vst.back.order.po.OrderEnum.AUDIT_TYPE auditTypeE : OrderEnum.AUDIT_TYPE.values()) {
				if (auditTypeE.name().equalsIgnoreCase(auditType)) {
					type = auditTypeE;
					break;
				}
			}
			ComAudit comAudit = orderService.createItemTask(objectId, type);
			ComAuditVo vo = new ComAuditVo();
			EnhanceBeanUtils.copyProperties(comAudit, vo);
			return ResponseBodyCreator.success(vo);
		} catch (Exception e) {
			return ResponseBodyCreator.exception(ApiEnum.ORDER_BUSSINESS_CODE.OTHER, ApiEnum.ORDER_ERROR_CODE.OTHER, e, objectId, ApiEnum.BUSSINESS_TAG.ORD_ORDER.name());
		}
	}

	@Override
	public ResponseBody<OrderDeductAmountVo> calDeductAmountByOrderId(RequestBody<Long> request) {
		BusinessException businessException = ApiValidation.checkNotNull(request);
		if (businessException != null) {
			return ResponseBodyCreator.exception(ApiEnum.ORDER_BUSSINESS_CODE.OTHER, ApiEnum.ORDER_ERROR_CODE.OTHER, "计算订单扣款金额,所传参数为空!", businessException.getMessage(), businessException, null, ApiEnum.BUSSINESS_TAG.ORD_ORDER.name());
		}
		OrderDeductAmountVo vo = new OrderDeductAmountVo();
		Long orderId = request.getT();
		Map<String, Object> result = orderService.calcDeductAmtForDistribution(orderId);
		if(result != null) {
			if(result.get("IS_ERROR") != null) {
				vo.setSuccess((boolean)result.get("IS_ERROR"));
			}
			if(result.get("TOTAL_DEDUCT_AMOUNT") != null) {
				vo.setTotalDeductAmount((Long)result.get("TOTAL_DEDUCT_AMOUNT"));
			}
			if(result.get("TOTAL_REFUND_AMOUNT") != null) {
				vo.setTotalRefundAmount((Long)result.get("TOTAL_REFUND_AMOUNT"));
			}
		}
		return ResponseBodyCreator.success(vo);
	}
	
	

}
