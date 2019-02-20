package com.lvmama.vst.order.job;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.lvmama.vst.back.client.activiti.service.ProcesserClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdAccInsDelayInfo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.service.ComActivitiRelationService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdAccInsDelayInfoService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.ISupplierOrderOperator;
import com.lvmama.vst.order.vo.OrderSupplierOperateResult;

@Service
public class AutoInvokeInterfacePlatformJob implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(AutoInvokeInterfacePlatformJob.class);
	
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
	
	@Autowired
	private ISupplierOrderOperator supplierOrderOperator;
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
    private IOrdAccInsDelayInfoService ordAccInsDelayInfoService;
	
	@Override
	public void run() {
		logger.info("AutoInvokeInterfacePlatformJob start...");
		if(Constant.getInstance().isJobRunnable()){
			logger.info("start create supplier order:");
			List<Long> orderIds = this.complexQueryService.findNeedCreateSupplierOrders();
			
			if(!CollectionUtils.isEmpty(orderIds)) {
				for(Long orderId : orderIds) {
					logger.info("start process order:" + orderId);
					try {
						OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
						if(order == null) {
							logger.error("can not find the order:" + orderId);
							continue;
						}
						
						if(order.hasCanceled()) {
							logger.error("order has been canceled:" + orderId);
							continue;
						}
						
						if(OrderEnum.INVOKE_INTERFACE_PF_STATUS.CREATED.name().equals(order.getInvokeInterfacePfStatus())) {
							logger.error("order has been created supplier order:" + orderId);
							continue;
						}
						
						List<OrdOrderItem> list = orderUpdateService.queryOrderItemByOrderId(orderId);
						Set<Long> set = new HashSet<Long>();
						for(Iterator<OrdOrderItem> it = list.iterator(); it.hasNext(); ){
							OrdOrderItem orderItem = it.next();
							if(orderItem.isSupplierOrderItem() && !OrderEnum.INVOKE_INTERFACE_PF_STATUS.CREATED.name().equals(orderItem.getInvokeInterfacePfStatus())) {
								set.add(orderItem.getOrderItemId());
							}
						}
						
						/**去掉在门票自动推送供应商订单job中加的过滤
						if (OrdOrderUtils.isDestBuFrontAppOrder(order)) {
	                        
						    OrdAccInsDelayInfo accInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(orderId);
						    if (null != accInsDelayInfo) {
						      //若是set中 是目的地自由行下酒+景 或者 酒店套餐 意外险子单，则不适用此job自动创建供应商订单
		                        List<OrdOrderItem> accInsOrderItems = OrdOrderUtils.getAccInsOrderItem(order);
		                        Set<Long> destBuAccInsOrderItemSet = new HashSet<Long>();
		                        for (OrdOrderItem ordOrderItem : accInsOrderItems) {
		                            destBuAccInsOrderItemSet.add(ordOrderItem.getOrderItemId());
		                        }
		                        
                                String travDelayFlag = accInsDelayInfo.getTravDelayFlag();
                                if (StringUtils.isNotBlank(travDelayFlag) && "Y".equalsIgnoreCase(travDelayFlag)) {
                                    for (Iterator<Long> it = set
                                            .iterator(); it.hasNext();) {
                                        Long item = it.next();
                                        if (destBuAccInsOrderItemSet.contains(item)) {
                                            it.remove();
                                        }
                                    }
                                }
                            }
                        }*/
						
						OrderSupplierOperateResult result = supplierOrderOperator.createSupplierOrder(orderId,set);
					} catch (Exception e) {
						logger.error("create supplier order failed, orderId:" + orderId);
						logger.error("{}", e);
					}
					
					logger.info("end process order:" + orderId);
				}
			}
			
			logger.info("start cancel supplier order:");
			orderIds = this.complexQueryService.findNeedCancelSupplierOrders();
			
			if(!CollectionUtils.isEmpty(orderIds)) {
				for(Long orderId : orderIds) {
					logger.info("start process order:" + orderId);
					try {
						OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
						if(order == null) {
							logger.error("can not find the order:" + orderId);
							continue;
						}
						
						if(!order.hasCanceled()) {
							logger.error("order status is not 'cancel':" + orderId);
							continue;
						}
						
						if(OrderEnum.INVOKE_INTERFACE_PF_STATUS.CANCELED.name().equals(order.getInvokeInterfacePfStatus())) {
							logger.error("order has been canceled supplier order:" + orderId);
							continue;
						}
						
						OrderSupplierOperateResult result = supplierOrderOperator.cancelSupplierOrder(orderId);
					} catch (Exception e) {
						logger.error("cancel supplier order failed, orderId:" + orderId);
						logger.error("{}", e);
					}
					
					logger.info("end process order:" + orderId);
				}
			}
		}
		logger.info("AutoInvokeInterfacePlatformJob end...");
	}
}
