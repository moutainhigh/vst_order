package com.lvmama.vst.order.processer.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.order.api.base.utils.StringUtils;
import com.lvmama.order.api.base.vo.BusinessException;
import com.lvmama.order.api.base.vo.RequestBody;
import com.lvmama.order.api.base.vo.ResponseBody;
import com.lvmama.order.enums.OrdProcessKeyEnum;
import com.lvmama.order.route.service.IOrder2RouteService;
import com.lvmama.order.route.service.IOrderRouteService;
import com.lvmama.order.vo.comm.OrderItemVo;
import com.lvmama.order.vo.comm.OrderVo;
import com.lvmama.order.workflow.api.IApiOrderWorkflowService;
import com.lvmama.order.workflow.utils.BusinessKeyCreator;
import com.lvmama.order.workflow.vo.WorkflowStarterVo;
import com.lvmama.vst.back.client.activiti.service.ProcesserClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdProcessKey;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.comm.utils.NewOrderSystemUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.neworder.order.EnhanceBeanUtils;
import com.lvmama.vst.order.service.ComActivitiRelationService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.impl.OrdProcessKeyServiceImpl;
import com.lvmama.vst.pet.adapter.IOrdPrePayServiceAdapter;

/**
 * JMS驱动工作流程引擎启动后续工作
 * 
 * @author minchun
 * @2016-01-21
 *
 */
public class JMSDrivenWorkflowProcesser implements MessageProcesser {
	private static final Log logger = LogFactory.getLog(JMSDrivenWorkflowProcesser.class);

	@Autowired
	private ProcesserClientService processerClientService;

	@Autowired
	private IApiOrderWorkflowService apiOrderWorkflowService;

	@Autowired
	private IOrderRouteService orderRouteService;  
	
	@Autowired
	private ComActivitiRelationService comActivitiRelationService;

	@Autowired
	private OrderService orderService;
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	@Autowired
	private IOrdPrePayServiceAdapter ordPrePayServiceAdapter;

	@Autowired
	private IOrder2RouteService order2RouteService;

	@Autowired
	private OrdProcessKeyServiceImpl ordProcessKeyService;
	
	@Override
	public void process(final Message message) {
		//==判断消息来源是否新订单系统，若是，则不处理 start== 2017-09-20 by zhujingfeng 
		if (NewOrderSystemUtils.isMessageFromNewOrderSystem(message.getSystemType())) {
			logger.info("JMSDrivenWorkflowProcesser process message from new order system,no need to deal !message:" + message.toString());
			return;
		}
		//==判断消息来源是否新订单系统，若是，则不处理 end== 2017-09-20 by zhujingfeng
		if (MessageUtils.isOrderAscProcessMsg(message)) {
			logger.info("JMSDrivenWorkflowProcesser threading trigger wokflow startup.....orderId:"+message.getObjectId());
			Long orderId = -1L;
			try {
				orderId = message.getObjectId();
				
				String memKey = Constant._KEY_JMS_START_WORKFLOW_ORDER + orderId;
				if(MemcachedUtil.getInstance().keyExists(memKey)) { // 防止重复启动工作流
					logger.error("this order is starting workflow:" + orderId);
					return;
				}else{
					MemcachedUtil.getInstance().set(memKey, 30, 1); // 防止重复启动工作流
				}
				
				OrdOrder order = orderService.queryOrdorderByOrderId(orderId);
				if(order == null) {
					logger.error("can not find the order:" + orderId);
					return;
				}
				
				if(order.hasCanceled()) {
					logger.error("order has been canceled:" + orderId);
					return;
				}
				
				logger.info("JMSDrivenWorkflowProcesser needResourceConfirm:" + order.getMainOrderItem().getNeedResourceConfirm());
				
				if(comActivitiRelationService.queryRelation(null, orderId, ComActivitiRelation.OBJECT_TYPE.ORD_ORDER) != null) {
					logger.info("the workflow already existed, orderId:" + orderId);
					return;
				}
				
				//检查订单工作流路由
				String processId = null;
				OrderVo vo = this.initOrderVo(order);
				if (this.order2RouteService.isOrderRouteToNewWorkflow(vo)) {
					processId = startApproveWorkflowNewPlus(vo);
				} else if(this.orderRouteService.isOrderRouteToNewWorkflow(vo)) {										
					Map<String,Object> params = new HashMap<String, Object>();
					params.put("orderId", orderId);
					params.put("mainOrderItem", vo.getMainOrderItem());
					params.put("order", vo);
					
					WorkflowStarterVo startVo = new WorkflowStarterVo(orderId, order.getProcessKey(), params);
					ResponseBody<String> resp = this.apiOrderWorkflowService.startApproveProcess(new RequestBody<WorkflowStarterVo>(startVo));
					if(resp.isSuccess())
						processId = resp.getT();
				}else {
					processId = processerClientService.queryProcessIdByBusinessKey(ActivitiUtils.createOrderBussinessKey(order));
					if(processId != null){
						comActivitiRelationService.saveRelation(order.getProcessKey(), processId, order.getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
						return;
					}
	
					Map<String,Object> params = new HashMap<String, Object>();
					params.put("orderId", orderId);
					params.put("mainOrderItem", order.getMainOrderItem());
					params.put("order", order);
					processId = processerClientService.startProcesser(order.getProcessKey(),ActivitiUtils.createOrderBussinessKey(order), params);
					
					if(StringUtils.isNotEmptyString(processId)) {
						comActivitiRelationService.saveRelation(order.getProcessKey(), processId, order.getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
					}
				}
				
				if(StringUtils.isEmptyString(processId)) {
					throw new BusinessException(orderId + " return processId: " + processId);
				}				
				
//				if(order.getDistributorId().longValue() == 2) {
//					orderLocalService.startBackOrder(orderId, "USER");
//				}
				
				logger.info("order.hasNeedPrepaid="+order.hasNeedPrepaid());
				if(order.hasNeedPrepaid() && order.hasPayed() && ActivitiUtils.hasActivitiOrder(order)){
//				processerClientService.paymentSuccess(createKeyByOrder(order));
					orderLocalService.doPaymentSuccessMsg(order);
				}
				else if(order.hasNeedPrepaid() && order.getDistributorId() != Constant.DIST_BACK_END){
					if(order.getOughtAmount()==0&&!order.isNeedResourceConfirm()){//操作0元支付
						logger.info("ordPrePayServiceAdapter.vstOrder0YuanPayMsg()"+order.getOrderId());
						ordPrePayServiceAdapter.vstOrder0YuanPayMsg(order.getOrderId());
					}
				}
				
			} catch (Exception e) {
				logger.error("Generate workflow failed, orderId:" + orderId);
				logger.error("{}", e);
			}
		}
	}
	
	
	
	private String startApproveWorkflowNewPlus(OrderVo vo) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("orderId", vo.getOrderId());
		params.put("mainOrderItem", vo.getMainOrderItem());
		params.put("order", vo);
		List<OrdProcessKey> ordOrderProcessKey = this.getOrdProcessKeyById(vo.getOrderId(),null,OrdProcessKeyEnum.OBJECT_TYPE.ORD_ORDER.name(), OrdProcessKeyEnum.KEY_TYPE.approve.name()); 
		if (CollectionUtils.isNotEmpty(ordOrderProcessKey)) {
			String processKey = ordOrderProcessKey.get(0).getKeyValue();
			String businessKey = BusinessKeyCreator.createOrderBusinessKey(BusinessKeyCreator.WORKFLOW_TYPE.approve.name(), vo.getOrderId());
			WorkflowStarterVo startVo = new WorkflowStarterVo(vo.getOrderId(), processKey,businessKey, BusinessKeyCreator.WORKFLOW_TYPE.approve.name(), params);
			ResponseBody<String> resp = this.apiOrderWorkflowService.startProcess(new RequestBody<WorkflowStarterVo>(startVo));
			return resp == null ? null : resp.getT();
		}
		return null;
	}
	
	private List<OrdProcessKey> getOrdProcessKeyById(Long objectId, Set<Long> objectIdList, String objectType, String keyType) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectId", objectId);
		params.put("objectIdList", objectIdList);
		params.put("objectType", objectType);
		params.put("keyType", keyType);
		return ordProcessKeyService.selectOrdProcessKeyList(params);
	}
	
	private OrderVo initOrderVo(OrdOrder order) {
		OrderVo vo = new OrderVo();
		EnhanceBeanUtils.copyProperties(order, vo);
		this.initOrderItemSubProcessKey(vo);
		this.initMainOrderItemVo(vo);
		return vo;
	}
	
	private void initOrderItemSubProcessKey(OrderVo vo) {
		if(vo != null && CollectionUtils.isNotEmpty(vo.getOrderItemList())) {
			Map<Long,OrderItemVo> itemMap = new HashMap<Long,OrderItemVo>();
			for (OrderItemVo item : vo.getOrderItemList()) {
				itemMap.put(item.getOrderItemId(), item);
			}
			List<OrdProcessKey> list = getOrdProcessKeyById(null,itemMap.keySet(),OrdProcessKeyEnum.OBJECT_TYPE.ORD_ORDER_ITEM.name(),OrdProcessKeyEnum.KEY_TYPE.approve.name());
			if(list != null) {
				for (OrdProcessKey resKey : list) {
					OrderItemVo item = itemMap.get(resKey.getObjectId());
					item.setApproveSubProcessKey(resKey.getKeyValue());
				}
			}
		}
	}
	
	private void initMainOrderItemVo(OrderVo orderVo) {
		if(orderVo != null && CollectionUtils.isNotEmpty(orderVo.getOrderItemList())) {
			for (OrderItemVo item : orderVo.getOrderItemList()) {
				if("true".equalsIgnoreCase(item.getMainItem())) {
					orderVo.setMainOrderItem(item);
				}
			}
		}
	}
}
