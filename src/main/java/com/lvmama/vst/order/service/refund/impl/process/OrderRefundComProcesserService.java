package com.lvmama.vst.order.service.refund.impl.process;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mortbay.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.activiti.service.ProcesserClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.vo.ActivitiKey;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.ComActivitiRelationService;

/**
 * @version 1.0
 */
@Service("orderRefundComProcesserService")
public class OrderRefundComProcesserService {
	private static Logger LOG = LoggerFactory.getLogger(OrderRefundComProcesserService.class);
	@Autowired
	private OrderService orderService;
	@Autowired
	private ProcesserClientService processerClientService;
	@Autowired
	private ComActivitiRelationService comActivitiRelationService;
	
	public void startProcesserByRefund(final String processKey, Long orderId ,Map<String, Object> params) {
		//not try catch，避免调用者无法捕捉
		LOG.info("startProcesser orderId =" +orderId);
		OrdOrder order= orderService.querySimpleOrder(orderId);
		if(order== null) return;
		if(params == null){
			params = new HashMap<String, Object>();
		}
		String methodName = "OrderRefundProcesserServiceImpl#startProcesser【"+orderId+"】";  
		fillStartProcessParamByRefund(order, params);
		Long startTime = System.currentTimeMillis();
		String processId = processerClientService.startProcesser(processKey, ActivitiUtils.createOrderRefundBussinessKey(order), params);
		LOG.info(ComLogUtil.printTraceInfo(processId, "启动订单在线退款流程", "processerClientService.startProcesser", System.currentTimeMillis() - startTime));

		startTime = System.currentTimeMillis();
		comActivitiRelationService.saveRelation(processKey, processId, orderId, ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
		Log.info(ComLogUtil.printTraceInfo(methodName,"保存订单在线退款工作流信息", "comActivitiRelationService.saveRelation", System.currentTimeMillis() - startTime));
		
	}
	
	public void completeTaskByOnlineRefundAudit(ComAudit comAudit) {
		ActivitiKey activitiKey =null;
		//主订单-在线退款通知
		if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(comAudit.getObjectType().trim())){
			OrdOrder order =orderService.querySimpleOrder(comAudit.getObjectId());
			if(order ==null) return;
			if(ActivitiUtils.hasActivitiOrder(order)){
				activitiKey =new ActivitiKey((String)null, ActivitiUtils.createOrderRefundBussinessKey(order));
			}
		}else if((OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(comAudit.getObjectType().trim()))){
			OrdOrderItem orderItem = orderService.getOrderItem(comAudit.getAuditId());
			if(orderItem ==null) return;
			activitiKey =new ActivitiKey((String)null, ActivitiUtils.createOrderItemRefundBussinessKey(orderItem));
		}
		//start key
		if(activitiKey !=null){
			LOG.info("completeTaskByOnlineRefundAudit activitiKey:"+activitiKey);
			processerClientService.completeTaskByAudit(activitiKey, comAudit);
			LOG.info("completeTaskByOnlineRefundAudit success.");
		}
	}
	
	/**
	 * fillStartProcessParamByRefund
	 */
	private void fillStartProcessParamByRefund(OrdOrder order, Map<String, Object> params) throws BusinessException{
		order.setOrderItemList(orderService.queryOrderItemByOrderId(order.getOrderId()));
		if(CollectionUtils.isEmpty(order.getOrderItemList())){
			throw new BusinessException("子订单不存在 orderId="+ order.getOrderId());
		}
		params.put("orderId", order.getOrderId());
		params.put("mainOrderItem", order.getMainOrderItem());
		params.put("order", order);
	}
}
