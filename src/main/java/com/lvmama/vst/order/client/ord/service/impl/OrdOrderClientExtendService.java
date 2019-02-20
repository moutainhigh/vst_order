package com.lvmama.vst.order.client.ord.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.order.api.base.vo.RequestBody;
import com.lvmama.order.api.base.vo.ResponseBody;
import com.lvmama.order.enums.OrdProcessKeyEnum;
import com.lvmama.order.service.api.comm.order.IApiOrderQueryService;
import com.lvmama.order.vo.comm.OrderItemVo;
import com.lvmama.order.vo.comm.OrderVo;
import com.lvmama.order.workflow.api.IApiOrderWorkflowService;
import com.lvmama.order.workflow.utils.BusinessKeyCreator;
import com.lvmama.order.workflow.vo.WorkflowStarterVo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdProcessKey;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.order.service.IOrdProcessKeyService;

/**
 * @ClassName: OrdOrderClientExtendService
 * @Description: OrdOrderClientService辅助类
 * @author: lijunshuai
 * @date: 2018年11月2日 下午3:57:54
 */
@Component("ordOrderClientExtendService")
public class OrdOrderClientExtendService {

	private static final Logger LOG = LoggerFactory.getLogger(OrdOrderClientExtendService.class);

	@Autowired
	private IApiOrderWorkflowService apiOrderWorkflowService;

	@Autowired
	private IOrdProcessKeyService ordProcessKeyService;

	@Autowired
	private IApiOrderQueryService apiOrderQueryService;

	public void cancelOrderFromWorkflowNew(String cancelCode, String reason, String operatorId, String memo,
			OrderVo vo) {
		LOG.info("method cancelOrderFromWorkflowNew start,orderId=" + vo.getOrderId());
		Map<String, Object> params = this.getWorkflowMapParams(cancelCode, reason, operatorId, memo, vo);
		String processKey = this.getCancelProcessKey(vo.getCategoryId());
		WorkflowStarterVo workflowStarterVo = new WorkflowStarterVo(vo.getOrderId(), processKey, params);
		ResponseBody<String> responseBody = apiOrderWorkflowService
				.startCancelProcess(new RequestBody<>(workflowStarterVo));
		this.handResponseLog(vo.getOrderId(), responseBody);
	}

	public void cancelOrderFromWorkflowNewPlus(String cancelCode, String reason, String operatorId, String memo,
			OrderVo vo) {
		LOG.info("method cancelOrderFromWorkflowNewPlus start,orderId=" + vo.getOrderId());
		Map<String, Object> params = this.getWorkflowMapParams(cancelCode, reason, operatorId, memo, vo);
		String processKey = this.getOrdProcessKeyById(vo.getOrderId(), OrdProcessKeyEnum.OBJECT_TYPE.ORD_ORDER.name(),
				OrdProcessKeyEnum.KEY_TYPE.cancel.name());
		WorkflowStarterVo workflowStarterVo = new WorkflowStarterVo(vo.getOrderId(), processKey,
				BusinessKeyCreator.createOrderBusinessKey(OrdProcessKeyEnum.KEY_TYPE.cancel.name(), vo.getOrderId()),
				OrdProcessKeyEnum.KEY_TYPE.cancel.name(), params);
		ResponseBody<String> responseBody = apiOrderWorkflowService.startProcess(new RequestBody<>(workflowStarterVo));
		this.handResponseLog(vo.getOrderId(), responseBody);
	}

	public boolean paymentOrderFromWorkflowNewPlus(OrderVo vo) {
		LOG.info("method paymentOrderFromWorkflowNewPlus start,orderId=" + vo.getOrderId());
		Map<String, Object> params = getWorkflowMapParams(vo);
		String processKey = this.getOrdProcessKeyById(vo.getOrderId(), OrdProcessKeyEnum.OBJECT_TYPE.ORD_ORDER.name(),
				OrdProcessKeyEnum.KEY_TYPE.payment.name());
		WorkflowStarterVo startVo = new WorkflowStarterVo(vo.getOrderId(), processKey,
				BusinessKeyCreator.createOrderBusinessKey(OrdProcessKeyEnum.KEY_TYPE.payment.name(), vo.getOrderId()),
				OrdProcessKeyEnum.KEY_TYPE.payment.name(), params);
		ResponseBody<String> resp = this.apiOrderWorkflowService
				.startProcess(new RequestBody<WorkflowStarterVo>(startVo));
		if (resp != null && resp.isSuccess()) {
			LOG.info("orderId:{}启动支付工作流成功", vo.getOrderId());
			return resp.isSuccess();
		} else {
			LOG.info("orderId:{}启动支付工作流异常, response:{}", vo.getOrderId(), JSONObject.fromObject(resp));
			return false;
		}
	}

	public boolean paymentOrderFromWorkflowNew(OrderVo vo) {
		LOG.info("method paymentOrderFromWorkflowNew start,orderId=" + vo.getOrderId());
		Map<String, Object> params = getWorkflowMapParams(vo);
		WorkflowStarterVo startVo = new WorkflowStarterVo(vo.getOrderId(), vo.getProcessKey(), params);
		ResponseBody<String> resp = this.apiOrderWorkflowService
				.startPaymentProcess(new RequestBody<WorkflowStarterVo>(startVo));
		return resp == null ? false : resp.isSuccess();
	}

	public OrderVo initOrderVo(OrdOrder order, String keyType) {
		if (order == null || order.getOrderId() == null)
			return null;
		ResponseBody<OrderVo> resp = apiOrderQueryService
				.selectOrderWithItem(new RequestBody<Long>(order.getOrderId()));
		if (resp == null || resp.getT() == null)
			return null;
		OrderVo vo = resp.getT();
		EnhanceBeanUtils.copyProperties(order, vo);
		this.initOrderItemSubProcessKey(vo);
		this.initMainOrderItemVo(vo);
		return vo;
	}

	private Map<String, Object> getWorkflowMapParams(OrderVo vo) {
		return getWorkflowMapParams(null, null, null, null, vo);
	}

	public Map<String, Object> getWorkflowMapParams(String cancelCode, String reason, String operatorId, String memo) {
		return getWorkflowMapParams(cancelCode, reason, operatorId, memo, null);
	}

	private Map<String, Object> getWorkflowMapParams(String cancelCode, String reason, String operatorId, String memo,
			OrderVo vo) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("cancelCode", cancelCode);
		params.put("reason", isNullReason(reason) ? "SYSTEM" : reason);
		params.put("operatorId", operatorId);
		params.put("memo", memo);
		if (vo != null) {
			params.put("orderId", vo.getOrderId());
			params.put("mainOrderItem", vo.getMainOrderItem());
			params.put("order", vo);
		}
		return params;
	}

	private String getCancelProcessKey(Long categoryId) {
		if (categoryId.equals(1L)) {// 单酒店
			return "cancel_single_hotel_main_v5";
		} else if (categoryId.equals(188L)) { // 超级会员
			return "cancel_super_member_process_v1";
		} else {
			return "cancel_main";
		}
	}

	private String getOrdProcessKeyById(Long orderId, String objectType, String keyType) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectId", orderId);
		params.put("objectType", objectType);
		params.put("keyType", keyType);
		List<OrdProcessKey> res = ordProcessKeyService.selectOrdProcessKeyList(params);
		if (CollectionUtils.isNotEmpty(res)) {
			return res.get(0).getKeyValue();
		}
		return null;
	}

	private List<OrdProcessKey> getOrdProcessKeyById(Set<Long> objectIdList, String objectType) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectIdList", objectIdList);
		params.put("objectType", objectType);
		return ordProcessKeyService.selectOrdProcessKeyList(params);
	}

	private void handResponseLog(Long orderId, ResponseBody<String> responseBody) {
		if (responseBody.isFailure() || StringUtils.isEmpty(responseBody.getT())) {
			if (responseBody.getBusinessException() != null) {
				LOG.error("orderId:" + orderId + "调用取消工作流error", responseBody.getBusinessException());
			} else {
				LOG.error("orderId:" + orderId + "调用取消工作流error,error msg:{}", responseBody.getErrorMessage());
			}
		} else {
			LOG.info("orderId:" + orderId + "调用取消工作流成功");
		}
	}

	private boolean isNullReason(String reason) {
		return StringUtils.isEmpty(reason) || reason.equalsIgnoreCase("null");
	}

	private void initOrderItemSubProcessKey(OrderVo vo) {
		if (vo != null && CollectionUtils.isNotEmpty(vo.getOrderItemList())) {
			Map<Long, OrderItemVo> itemMap = new HashMap<Long, OrderItemVo>();
			for (OrderItemVo item : vo.getOrderItemList()) {
				itemMap.put(item.getOrderItemId(), item);
			}
			List<OrdProcessKey> list = getOrdProcessKeyById(itemMap.keySet(),OrdProcessKeyEnum.OBJECT_TYPE.ORD_ORDER_ITEM.name());
			if (list != null) {
				for (OrdProcessKey resKey : list) {
					OrderItemVo item = itemMap.get(resKey.getObjectId());
					if (resKey.getKeyType().equals(OrdProcessKeyEnum.KEY_TYPE.payment.name()))
						item.setPaymentSubProcessKey(resKey.getKeyValue());
					if (resKey.getKeyType().equals(OrdProcessKeyEnum.KEY_TYPE.approve.name()))
						item.setApproveSubProcessKey(resKey.getKeyValue());
					if (resKey.getKeyType().equals(OrdProcessKeyEnum.KEY_TYPE.cancel.name()))
						item.setCancelSubProcessKey(resKey.getKeyValue());
				}
			}
		}
	}

	private void initMainOrderItemVo(OrderVo orderVo) {
		if (orderVo != null && CollectionUtils.isNotEmpty(orderVo.getOrderItemList())) {
			for (OrderItemVo item : orderVo.getOrderItemList()) {
				if ("true".equalsIgnoreCase(item.getMainItem())) {
					orderVo.setMainOrderItem(item);
				}
			}
		}
	}
}
