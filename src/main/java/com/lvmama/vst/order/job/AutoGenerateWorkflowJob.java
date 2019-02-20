package com.lvmama.vst.order.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.lvmama.vst.back.client.activiti.service.ProcesserClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.vo.ActivitiKey;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.route.IVstOrderRouteService;
import com.lvmama.vst.order.service.ComActivitiRelationService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrderLocalService;

@Service
public class AutoGenerateWorkflowJob implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(AutoGenerateWorkflowJob.class);
	
	@Autowired
	private ComActivitiRelationService comActivitiRelationService;
	
	@Autowired
	private ProcesserClientService processerClientService;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	@Autowired
	protected OrderService orderService;
	
	@Resource
	private IVstOrderRouteService vstOrderRouteService;
	
	@Override
	public void run() {
		//modify by zhujingfeng 2017-09-26
		if((!vstOrderRouteService.isJobRouteToNewSys()) && Constant.getInstance().isJobRunnable()){
			logger.info("AutoGenerateWorkflowJob start...");
			List<Long> orderIds = this.complexQueryService.findNeedGenWorkflowOrders();
			
			if(CollectionUtils.isEmpty(orderIds)) {
				return;
			}
			
			for(Long orderId : orderIds) {
				logger.info("start process order:" + orderId);
				try {
					OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
					if(order == null) {
						logger.error("can not find the order:" + orderId);
						continue;
					}
					
					String memKey = Constant._KEY_JMS_START_WORKFLOW_ORDER + orderId;
					if(MemcachedUtil.getInstance().keyExists(memKey)) { // 防止重复启动工作流
						logger.error("this order is starting workflow:" + orderId);
						continue;
					}else{
						MemcachedUtil.getInstance().set(memKey, 30, 1); // 防止重复启动工作流
					}
					
					if(order.hasCanceled()) {
						logger.error("order has been canceled:" + orderId);
						continue;
					}
					
					if(comActivitiRelationService.queryRelation(null, orderId, ComActivitiRelation.OBJECT_TYPE.ORD_ORDER) != null) {
						logger.info("the workflow already existed, orderId:" + orderId);
						continue;
					}
					
					String processId = processerClientService.queryProcessIdByBusinessKey(ActivitiUtils.createOrderBussinessKey(order));
					if(processId != null){
						comActivitiRelationService.saveRelation(order.getProcessKey(), processId, order.getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
						continue;
					}
				
					Map<String,Object> params = new HashMap<String, Object>();
					params.put("orderId", order.getOrderId());
					params.put("mainOrderItem", order.getMainOrderItem());
					params.put("order", order);
					processId = processerClientService.startProcesser(order.getProcessKey(),ActivitiUtils.createOrderBussinessKey(order), params);
					comActivitiRelationService.saveRelation(order.getProcessKey(), processId, order.getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
					
					if(order.getDistributorId().longValue() == 2) {
						orderLocalService.startBackOrder(orderId, "USER");
					}
					
					if(order.hasNeedPrepaid() && order.hasPayed() && ActivitiUtils.hasActivitiOrder(order)){
						processerClientService.paymentSuccess(createKeyByOrder(order));
					}
				
					Thread.sleep(200);
				} catch (Exception e) {
					logger.error("Generate workflow failed, orderId:" + orderId);
					logger.error("{}", e);
				}
				
				logger.info("end process order:" + orderId);
			}
			
			logger.info("AutoGenerateWorkflowJob end...");
		}
	}
	
	private ActivitiKey createKeyByOrder(OrdOrder order){
		ComActivitiRelation relation = orderService.getRelation(order);
		return new ActivitiKey(relation, ActivitiUtils.createOrderBussinessKey(order));
	}
}
