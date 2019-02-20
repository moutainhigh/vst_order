package com.lvmama.vst.neworder.order.api.impl;

import com.lvmama.order.enums.SmsEnum.SEND_NODE;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.order.utils.EnumUtilsEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.order.api.base.utils.StringUtils;
import com.lvmama.order.api.base.vo.BusinessException;
import com.lvmama.order.api.base.vo.RequestBody;
import com.lvmama.order.api.base.vo.ResponseBody;
import com.lvmama.order.comm.utils.ResponseBodyCreator;
import com.lvmama.order.comm.utils.StringUtil;
import com.lvmama.order.comm.validation.ApiValidation;
import com.lvmama.order.enums.ApiEnum;
import com.lvmama.order.vst.api.common.service.IApiOrderWorkflowService;
import com.lvmama.order.vst.api.common.vo.request.CancelOrderVo;
import com.lvmama.order.vst.api.common.vo.request.CompleteTaskBySupplierVo;
import com.lvmama.order.vst.api.common.vo.request.CompleteTaskVo;
import com.lvmama.order.vst.api.common.vo.request.CreateTaskVo;
import com.lvmama.order.vst.api.common.vo.request.MarkTaskVo;
import com.lvmama.order.vst.api.common.vo.request.SupplierOrderVo;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.ord.service.OrderWorkflowService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.order.service.refund.adapter.OrderRefundProcesserAdapter;

/** 
 * @ImplementProject vst_order
 * @Description 
 * @author chenlizhao
 * @date 2018年2月12日 下午4:13:05
 */
@Component("apiVstOrderWorkflowService")
public class ApiOrderWorkflowServiceImpl implements IApiOrderWorkflowService {

	private static final Log LOG = LogFactory.getLog(ApiOrderWorkflowServiceImpl.class);
	private final Logger logger = LoggerFactory.getLogger(ApiOrderWorkflowServiceImpl.class);
	
	@Autowired
	private OrderWorkflowService orderWorkflowService;
	@Autowired
	private OrderRefundProcesserAdapter orderRefundProcesserAdapter;
	@Autowired
	private OrderService orderService;
	
	/* (non-Javadoc)
	 * @see com.lvmama.order.vst.api.common.service.IApiOrderWorkflowService#cancelOrder(com.lvmama.order.api.base.vo.RequestBody)
	 */
	@Override
	public ResponseBody cancelOrder(RequestBody<CancelOrderVo> request) {
		try {
			LOG.info("cancelOrder with " + StringUtil.toSafeString(request));
			
			//检查非空
			BusinessException businessException = ApiValidation.checkNotNull(request);
			if(businessException != null) {
	        	return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.CANCEL_ORDER_ERROR.getCode(), request.toString(), "取消订单失败,所传参数为空!");
	        }
			//检查订单号
			CancelOrderVo vo = request.getT();
			if(vo.getOrderId() == null) {
				return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.CANCEL_ORDER_ERROR.getCode(), request.toString(), "取消订单失败,所传订单号为空!");
			}
			
			ResultHandle result = orderWorkflowService.cancelOrder(vo.getOrderId(), vo.getCancelCode(), vo.getReason(), vo.getOperatorId(), vo.getMemo());
			if(result.isFail()) {
				return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.CANCEL_ORDER_ERROR.getCode(), request.toString(), "取消订单失败: " + result.getMsg());
			}
			
			return ResponseBodyCreator.success(null);
		} catch (Exception ex) {
			return ResponseBodyCreator.exception(null, ApiEnum.BUSSINESS_CODE.SYSTEM_INTERNAL_ERROR.getCode(), null, "取消订单内部错误", ex, StringUtil.toSafeString(request), ApiEnum.BUSSINESS_TAG.ORD_ORDER.name());
		}
	}

	@Override
	public ResponseBody markTaskValid(RequestBody<MarkTaskVo> request) {
		try {
			LOG.info("markTaskValid with " + StringUtil.toSafeString(request));
			
			//检查非空
			BusinessException businessException = ApiValidation.checkNotNull(request);
			if(businessException != null) {
	        	return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.OTHER.getCode(), request.toString(), "标志未处理活动失败,所传参数为空!");
	        }
			//检查标记活动对象
			MarkTaskVo vo = request.getT();
			if(vo.getObjectId() == null || vo.getObjectType() == null) {
				return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.OTHER.getCode(), request.toString(), "标志未处理活动失败,所传标记活动对象的ID或者Type为空!");
			}
			
			orderWorkflowService.markTaskValid(vo.getObjectId(), OrderEnum.AUDIT_OBJECT_TYPE.valueOf(vo.getObjectType().name()));
			
			return ResponseBodyCreator.success(null);
		} catch (Exception ex) {
			return ResponseBodyCreator.exception(null, ApiEnum.BUSSINESS_CODE.SYSTEM_INTERNAL_ERROR.getCode(), null, "标志未处理活动内部错误", ex, StringUtil.toSafeString(request), ApiEnum.BUSSINESS_TAG.ORD_ORDER.name());
		}
	}

	@Override
	public ResponseBody<Boolean> createSupplierOrder(RequestBody<SupplierOrderVo> request) {
		try {
			LOG.info("createSupplierOrder with " + StringUtil.toSafeString(request));
			
			//检查非空
			BusinessException businessException = ApiValidation.checkNotNull(request);
			if(businessException != null) {
	        	return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.CREATE_SUPPLIER_FAILED.getCode(), request.toString(), "创建供应商订单失败,所传参数为空!");
	        }
			//检查供应商订单vo
			SupplierOrderVo vo = request.getT();
			if(vo.getOrderId() == null || StringUtils.isEmptyString(vo.getType())) {
				return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.CREATE_SUPPLIER_FAILED.getCode(), request.toString(), "创建供应商订单失败,所传参数订单号或者类型为空!");
			}
			
			boolean status = this.orderWorkflowService.createSupplierOrder(vo.getOrderId(), vo.getType());			
			
			return ResponseBodyCreator.success(status);
		} catch (Exception ex) {
			return ResponseBodyCreator.exception(null, ApiEnum.BUSSINESS_CODE.SYSTEM_INTERNAL_ERROR.getCode(), null, "创建供应商订单内部错误", ex, StringUtil.toSafeString(request), ApiEnum.BUSSINESS_TAG.ORD_ORDER.name());
		}
	}

	@Override
	public ResponseBody<Boolean> createSupplierOrderByItem(RequestBody<SupplierOrderVo> request) {
		try {
			LOG.info("createSupplierOrderByItem with " + StringUtil.toSafeString(request));
			//检查非空
			BusinessException businessException = ApiValidation.checkNotNull(request);
			if(businessException != null) {
	        	return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.CREATE_SUPPLIER_FAILED.getCode(), request.toString(), "创建供应商订单失败,所传参数为空!");
	        }
			//检查供应商订单vo
			SupplierOrderVo vo = request.getT();
			if(vo.getOrderId() == null || vo.getOrderItemIdList() == null ||StringUtils.isEmptyString(vo.getType())) {
				return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.CREATE_SUPPLIER_FAILED.getCode(), request.toString(), "创建供应商订单失败,所传订单号或者子订单号或者类型为空!");
			}
			boolean status = this.orderWorkflowService.createSupplierOrder(vo.getOrderId(),vo.getOrderItemIdList(), vo.getType());			
			LOG.info("createSupplierOrderByItem end,status="+status);
			return ResponseBodyCreator.success(status);
		} catch (Exception ex) {
			return ResponseBodyCreator.exception(null, ApiEnum.BUSSINESS_CODE.SYSTEM_INTERNAL_ERROR.getCode(), null, "创建供应商订单内部错误", ex, StringUtil.toSafeString(request), ApiEnum.BUSSINESS_TAG.ORD_ORDER.name());
		}
	}


	
	@Override
	public ResponseBody completeTask(RequestBody<CompleteTaskVo> request) {
		try{
			LOG.info("completeTask with " + StringUtil.toSafeString(request));
			
			//检查非空
			BusinessException businessException = ApiValidation.checkNotNull(request);
			if(businessException != null) {
	        	return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.OTHER.getCode(), request.toString(), "完成活动失败,所传参数为空!");
	        }
			
			//检查对象
			CompleteTaskVo vo=request.getT();
			if(vo.getOperator()==null || vo.getAsList()==null){
				return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.OTHER.getCode(), request.toString(), "完成活动失败,所传对象的auditId或者operator为空!");
			}
			
			orderWorkflowService.completeTask(vo.getAsList(), vo.getOperator(), vo.isCover());
			
			return ResponseBodyCreator.success(null);
			
		}catch(Exception e){
			return ResponseBodyCreator.exception(null, ApiEnum.BUSSINESS_CODE.SYSTEM_INTERNAL_ERROR.getCode(), null, "完成活动内部错误", e, StringUtil.toSafeString(request), ApiEnum.BUSSINESS_TAG.ORD_ORDER.name());
		}
	}
	
	@Override
	public ResponseBody<Long> createTask(RequestBody<CreateTaskVo> request) {
		try{
			LOG.info("createTask with " + StringUtil.toSafeString(request));
			
			//检查非空
			BusinessException businessException = ApiValidation.checkNotNull(request);
			if(businessException != null) {
	        	return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.OTHER.getCode(), request.toString(), "创建活动失败,所传参数为空!");
	        }
			
			//检查对象
			CreateTaskVo vo=request.getT();
			if(vo.getObjectId()==null){
				return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.OTHER.getCode(), request.toString(), "创建活动失败,所传对象的objectId为空!");
			}else if(vo.getObjectType()==null){
				return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.OTHER.getCode(), request.toString(), "创建活动失败,所传对象的objectType为空!");
			}else if(vo.getOrderId()==null){
				return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.OTHER.getCode(), request.toString(), "创建活动失败,所传对象的orderId为空!");
			}else if(vo.getType()==null){
				return ResponseBodyCreator.error(null, ApiEnum.BUSSINESS_CODE.OTHER.getCode(), request.toString(), "创建活动失败,所传对象的type为空!");
			}
			
			Long id=orderWorkflowService.createTask(vo.getOrderId(),vo.getObjectId(),OrderEnum.AUDIT_OBJECT_TYPE.valueOf(vo.getObjectType().name()), OrderEnum.AUDIT_TYPE.valueOf(vo.getType().name()), vo.getOperator());
			return ResponseBodyCreator.success(id);
		}catch(Exception e){
			LOG.error("createTask with error",e);
			return ResponseBodyCreator.exception(null, ApiEnum.BUSSINESS_CODE.SYSTEM_INTERNAL_ERROR.getCode(), null, "创建活动内部错误", e, StringUtil.toSafeString(request), ApiEnum.BUSSINESS_TAG.ORD_ORDER.name());
		}
	}

	@Override
	public ResponseBody completeTaskBySupplierConfirm(RequestBody<CompleteTaskBySupplierVo> request) {
		try {
			LOG.info("completeTaskBySupplierConfirm with " + StringUtil.toSafeString(request));

			// 检查非空
			BusinessException businessException = ApiValidation.checkNotNull(request);
			if (businessException != null) {
				return ResponseBodyCreator.error(ApiEnum.ORDER_BUSSINESS_CODE.OTHER, ApiEnum.ORDER_ERROR_CODE.OTHER, request.toString(), "供应商确认活动失败,所传参数为空!");
			}

			// 检查对象
			CompleteTaskBySupplierVo vo = request.getT();
			Long orderId = vo.getOrderId();
			String supplierKey = vo.getSupplierKey();

			OrdOrder order = orderService.queryOrdorderByOrderId(orderId);
			orderRefundProcesserAdapter.completeTaskBySupplierConfirm(order, supplierKey);

			return ResponseBodyCreator.success(null);
		} catch (Exception e) {
			LOG.error("completeTaskBySupplierConfirm with error", e);
			return ResponseBodyCreator.exception(ApiEnum.ORDER_BUSSINESS_CODE.OTHER, ApiEnum.ORDER_ERROR_CODE.OTHER, null, "创建活动内部错误", e, StringUtil.toSafeString(request), ApiEnum.BUSSINESS_TAG.ORD_ORDER.name());
		}
	}
	

	@Override
	public void saveTaskRelation(String taskId, Long objectId, String type) {
		if(!StringUtil.isNotEmptyString(taskId)
				||!StringUtil.isNotEmptyString(type)
				||null==objectId){
			return;
		}
		ComActivitiRelation.OBJECT_TYPE object_type=EnumUtilsEx.getEnum(ComActivitiRelation.OBJECT_TYPE.class,type);
		orderWorkflowService.saveTaskRelation(taskId,objectId,object_type);
	}

	@Override
	public void sendOrderSms(Long objectId, SEND_NODE sendNode,String orderType) {
		if (ObjectUtils.equals(orderType, "ORDER_PAYMENT_MSG")) {
			orderType = "PAYMENT";
		} else if (ObjectUtils.equals(orderType, "ORDER_CANCEL_MSG")) {
			orderType = "CANCEL";
		} else if (ObjectUtils.equals(orderType, "ORDER_RESOURCE_MSG")) {
			orderType = "AMPLE";
		} else {
			orderType = null;
		}
		logger.info("orderId:{},  orderType:{}", objectId, orderType);
		if (orderType != null) {
			orderWorkflowService.sendOrderSms(objectId, null, orderType);
		} else {
			logger.info("orderId:{},  orderType是空,不调用发送短信", objectId);
		}
		
	}
}
