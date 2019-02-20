/**
 * 
 */
package com.lvmama.vst.order.processer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import com.lvmama.order.route.service.IOrder2RouteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComJobConfig;
import com.lvmama.vst.back.pub.service.ComJobConfigService;
import com.lvmama.vst.back.supp.po.SuppOrderResult;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.order.service.IOrdOrderItemPassCodeSMSService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.ISupplierOrderOperator;
import com.lvmama.vst.order.vo.OrderSupplierOperateResult;
import com.lvmama.vst.supp.client.service.SupplierOrderOtherService;
import com.lvmama.vst.supp.client.service.SupplierOrderService;
import com.lvmama.vst.ticket.utils.DisneyUtils;

/**
 * 供应商订单处理JMS
 * @author lancey
 *
 */
public class SupplierOrderProcesser implements MessageProcesser,IWorkflowProcesser{
	private static final Log LOG = LogFactory.getLog(SupplierOrderProcesser.class);
	
	@Autowired
	private ISupplierOrderOperator supplierOrderOperator;
	
	@Autowired
	private ComJobConfigService comJobConfigService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	@Autowired
	private IOrder2RouteService order2RouteService;
	
	// Added by yangzhenzhong
	@Autowired
	private IOrdOrderItemPassCodeSMSService ordOrderItemPassCodeSMSLocalService;
	@Autowired
	private SupplierOrderOtherService supplierOrderOtherService;
	
	@Override
	public void handle(Message message) {
	    
	    if(MessageUtils.isExcludedInsAccOrderItemsMsg(message)){
	        handleAccIns(message.getObjectId(), true);
	    }else if (MessageUtils.isSendInsAccOrderItemsMsg(message)) {
	        handleAccIns(message.getObjectId(), false);
        }else if(MessageUtils.isOrderInfoPassMsg(message)){
			LOG.info("SupplierOrderProcesser.process: OrderInfoPassMsg,OrderID=" + message.getObjectId());
			
			if (message.getObjectId() != null) {
				OrderSupplierOperateResult result = supplierOrderOperator.createSupplierOrder(message.getObjectId());
				
				//如果已经订单创建成功或者不需要重复操作直接清除敏感信息
				if(result.isSuccess()||!result.isRetry()){
					orderUpdateService.updateGuaranteeCC(message.getObjectId());
				}
				if (!result.isSuccess()) {
					LOG.error("SupplierOrderProcesser.process: createSupplierOrder fail, OrderID=" + message.getObjectId() + ",msg:" + result.getErrMsg());
					
					if (result.isRetry()) {
						ComJobConfig jobConfig = new ComJobConfig();
						jobConfig.setCreateTime(new Date());
						jobConfig.setJobType(ComJobConfig.JOB_TYPE.SUPP_ORDER_CREATE.name());
						jobConfig.setObjectId(message.getObjectId());
						
						jobConfig.setObjectType(ComJobConfig.OBJECT_TYPE.ORDER.name());
						jobConfig.setPlanTime(DateUtil.getDateAfterMinutes(5));
						jobConfig.setRetryCount(5L);
						comJobConfigService.saveComJobConfig(jobConfig);
						LOG.info("SupplierOrderProcesser.process: ComJobConfig(ID=" + jobConfig.getComJobConfigId() + " ,type=SUPP_ORDER_CREATE) is added.");
					} else {
						//废单
						//为了保证工作流的深度优先原则,不直接废单
						try {
							ComJobConfig jobConfig = new ComJobConfig();
							jobConfig.setCreateTime(new Date());
							jobConfig.setJobType(ComJobConfig.JOB_TYPE.ORDER_CANCEL_BY_SUPP.name());
							jobConfig.setObjectId(message.getObjectId());
							jobConfig.setObjectType(ComJobConfig.OBJECT_TYPE.ORDER.name());
							jobConfig.setPlanTime(DateUtil.getDateAfterMinutes(2));
							jobConfig.setRetryCount(1L);
							comJobConfigService.saveComJobConfig(jobConfig);
							LOG.info("ComJobConfig insert com job config");
							/*ResultHandle handle = orderService.cancelOrder(message.getObjectId(), OrderEnum.ORDER_CANCEL_CODE.SUPPLIER_CREATE_FAIL.name(), result.getErrMsg(), "SYSTEM", null);
							if (handle.isSuccess()) {
								LOG.info("SupplierOrderProcesser.process: orderService.cancelOrder success,OrderID=" + message.getObjectId() + ", 废单成功。" );
								
							} else {
								LOG.error("SupplierOrderProcesser.process: orderService.cancelOrder fail,OrderID=" + message.getObjectId() + ",废单失败，msg=" + handle.getMsg());
							}*/
						} catch (Exception ex) {
							LOG.info("SupplierOrderProcesser.process: OrderID=" + message.getObjectId() + ", 废单发生异常：" + ex.getMessage());
						}
					}
				} else {
					LOG.info("SupplierOrderProcesser.process: createSupplierOrder success, OrderID=" + message.getObjectId() + ",msg=" + result.getErrMsg());
				}
			}
		}else if(MessageUtils.isOrderPaymentMsg(message)){
			LOG.info("SupplierOrderProcesser.process: OrderPaymentMsg start OrderID="+message.getObjectId());
			if (message.getObjectId() != null) {
				
				List<OrdOrderItem> list = orderUpdateService.queryOrderItemByOrderId(message.getObjectId());
				Set<Long> set = new HashSet<Long>();
				
				boolean isExist = ordOrderItemPassCodeSMSLocalService.isExistOfFlagData(message.getObjectId());
				
				for(Iterator<OrdOrderItem> it = list.iterator();it.hasNext();){
					OrdOrderItem orderItem = it.next();
					//修改于20150204--兼容港捷旅订单
					/*if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.name().equalsIgnoreCase(orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name()))){
						if(5400 == orderItem.getSupplierId().longValue()){
							set.add(orderItem.getOrderItemId());
						}
					}else{
						set.add(orderItem.getOrderItemId());
					}*/
					
					// added by yangzhenzhong at 2015/8/27 begin
					// 排除掉提前申码,延迟发送短信，同时子订单是景点门票或者其它票的子订单创建, 这类子订单在子订单支付完成后就已经创建,此处跳过,避免重复下单
					if(isExist && (orderItem.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId() 
							   	   || orderItem.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId()
                                   || orderItem.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId())){
						continue;
					}
					// end
					
					//现在对接酒店除港捷旅外都是在资源审核时就创建供应商订单，此处跳过对接酒店，防止重复下单
					if (orderItem.isSupplierOrderItem()) {
						if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.name()
								.equalsIgnoreCase(orderItem
										.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode
												.name()))) {//如果是对接酒店
							if (SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name().equals(orderItem.getCancelStrategy())) {//不可退改的
								set.add(orderItem.getOrderItemId());
							}
						} else {
							set.add(orderItem.getOrderItemId());
						}
					}else{
						//订单二期非对接走询单
						if(order2RouteService.isOrderItemRouteToNewSys(orderItem.getOrderItemId())){
							set.add(orderItem.getOrderItemId());
						}
					}
				}
				LOG.info("SupplierOrderProcesser.process: OrderPaymentMsg orderId:"+message.getObjectId()+",ordItemIds:"+set);
				if (!set.isEmpty()) {
					OrderSupplierOperateResult result = supplierOrderOperator.createSupplierOrder(message.getObjectId(),set);
					LOG.info("SupplierOrderProcesser.process: OrderPaymentMsg end result="+result.getErrMsg());
					
					if (!result.isSuccess()) {
                        LOG.error("SupplierOrderProcesser.process: createSupplierOrder fail, OrderID=" + message.getObjectId());
                        if (result.isRetry()) {
                            ComJobConfig jobConfig = new ComJobConfig();
                            jobConfig.setCreateTime(new Date());
                            jobConfig.setJobType(ComJobConfig.JOB_TYPE.SUPP_ORDER_CREATE.name());
                            jobConfig.setObjectId(message.getObjectId());
                            jobConfig.setObjectType(ComJobConfig.OBJECT_TYPE.ORDER.name());
                            jobConfig.setPlanTime(DateUtil.getDateAfterMinutes(5));
                            jobConfig.setRetryCount(5L);
                            comJobConfigService.saveComJobConfig(jobConfig);
                            LOG.info("SupplierOrderProcesser.process: ComJobConfig(ID=" + jobConfig.getComJobConfigId() +
                                    ", OrderID=" + message.getObjectId() + ",type=SUPP_ORDER_CREATE) is added.");
                        }
                    }
				}
			}
		}else if(MessageUtils.hasOrderModifyPersonMessage(message)){
			LOG.info("SupplierOrderProcesser.process: Modify Person start OrderID="+message.getObjectId());
			String str=OrderEnum.OrderModifyType.ModifyPerson.name()+":"+message.getAddition();
			OrderSupplierOperateResult result = supplierOrderOperator.updateSupplierOrder(message.getObjectId(),str);
			LOG.info("SupplierOrderProcesser.process: Modify Person end result="+(result!=null));
		} else if(MessageUtils.isOrderCancelMsg(message)) {
			LOG.info("SupplierOrderProcesser.process: OrderCancelMsg,OrderID=" + message.getObjectId());
			if (message.getObjectId() != null) {
				OrderSupplierOperateResult result = supplierOrderOperator.cancelSupplierOrder(message.getObjectId());
				if (!result.isSuccess()) {
					LOG.error("SupplierOrderProcesser.process: cancelSupplierOrder fail, OrderID=" + message.getObjectId() + ",msg:" + result.getErrMsg());
					if (result.isRetry()) {
						ComJobConfig jobConfig = new ComJobConfig();
						jobConfig.setCreateTime(new Date());
						jobConfig.setJobType(ComJobConfig.JOB_TYPE.SUPP_ORDER_CANCEL.name());
						jobConfig.setObjectId(message.getObjectId());
						jobConfig.setObjectType(ComJobConfig.OBJECT_TYPE.ORDER.name());
						jobConfig.setPlanTime(DateUtil.getDateAfterMinutes(5));
						jobConfig.setRetryCount(5L);
						comJobConfigService.saveComJobConfig(jobConfig);
						LOG.info("SupplierOrderProcesser.process: ComJobConfig(ID=" + jobConfig.getComJobConfigId() + " ,type=SUPP_ORDER_CANCEL) is added.");
					} else {
						LOG.info("SupplierOrderProcesser.process: OrderID=" + message.getObjectId() + ",cancelSupplierOrder fail,msg=" + result.getErrMsg());
						//因为创建第三方订单不成功的订单，需要创建订单取消活动
						ResultHandle handle = supplierOrderOperator.createCancelAuditForSupplierCreateFail(message.getObjectId());
						LOG.info("SupplierOrderProcesser.process: supplierOrderOperator.createCancelAuditForSupplierCreateFail,isSuccess=" + handle.isSuccess() + ",msg=" + handle.getMsg());
					}
				} else {
					LOG.info("SupplierOrderProcesser.process: cancelSupplierOrder success,OrderID=" + message.getObjectId() + "供应商方取消成功");
				}
			}
		}
	}
	

	@Override
	public void process(Message message) {
		LOG.info("SupplierOrderProcesser.process: jms eventType=" + message.getEventType());
		if(messageCheck(message)){
			Long orderId = message.getObjectId();
			OrdOrder order = orderService.queryOrdorderByOrderId(orderId);
			if(ActivitiUtils.hasNotActivitiOrder(order)){
				handle(message);
			}else {
				String getSpecialTicket = getSpecialTicket(order);
				//if(DisneyUtils.isDisney(order)){
				if(StringUtil.isNotEmptyString(getSpecialTicket)){
					if(MessageUtils.isOrderCancelMsg(message)) {
						if (message.getObjectId() != null) {
							try{
								LOG.info("SupplierOrderProcesser.process: OrderCancelMsg,OrderID=" + message.getObjectId());	
							supplierOrderOtherService.freeSuppOderStock(order.getOrderId());
							LOG.info("SupplierOrderProcesser.process: OrderCancelMsg,OrderID=" + message.getObjectId()+"end....");	
							}catch(Exception e){
								LOG.info("supplierOrderOtherService.freeSuppOderStock: [orderId="
										+ order.getOrderId() + "] get failed." + e.getMessage());	
							}
							}
					}
			}
				
				
			}
		}else if(MessageUtils.hasOrderModifyPersonMessage(message)){
			handle(message);
		}
	}
	
	
	public String getSpecialTicket(OrdOrder order) {
		String specialTicketType = "";
		if (CollectionUtils.isEmpty(order.getOrderItemList())) {
			return specialTicketType;
		}
		for (OrdOrderItem item : order.getOrderItemList()) {
		    specialTicketType = item.getContentStringByKey("specialTicketType");
			if(StringUtil.isNotEmptyString(specialTicketType)){
				break;
			}
		}
		LOG.info("orderId==="+order.getOrderId()+"specialTicketType==="+specialTicketType);
		return specialTicketType;
	}
	
	/**
	 * 消息检查
	 * @param message
	 * @return
	 */
	private boolean messageCheck(Message message){
		if(MessageUtils.isOrderInfoPassMsg(message)){
			return true;
		}else if(MessageUtils.isOrderPaymentMsg(message)){
			return true;
		} else if(MessageUtils.isOrderCancelMsg(message)) {
			return true;
		}
		return false;
	}

    public void handleAccIns(final Long orderId, Boolean isExcludedInsAcc) {
        List<OrdOrderItem> list = orderUpdateService.queryOrderItemByOrderId(orderId);
        
        List<Long> orderItemList = getAccInsOrderItemList(list);
        
	    if(isExcludedInsAcc){
            LOG.info("SupplierOrderProcesser.process: ExcludedInsAccOrderItems start OrderID="+orderId);
            if (orderId != null) {
                
                Set<Long> set = new HashSet<Long>();
                
                boolean isExist = ordOrderItemPassCodeSMSLocalService.isExistOfFlagData(orderId);
                
                for(Iterator<OrdOrderItem> it = list.iterator();it.hasNext();){
                    OrdOrderItem orderItem = it.next();
                    // added by yangzhenzhong at 2015/8/27 begin
                    // 排除掉提前申码,延迟发送短信，同时子订单是景点门票或者其它票的子订单创建, 这类子订单在子订单支付完成后就已经创建,此处跳过,避免重复下单
                    if(isExist && (orderItem.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId() 
                                   || orderItem.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId()
                                   || orderItem.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId())){
                        continue;
                    }
                    // end
                    
                    //现在对接酒店除港捷旅外都是在资源审核时就创建供应商订单，此处跳过对接酒店，防止重复下单
                    if (orderItem.isSupplierOrderItem()) {
                        if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.name()
                                .equalsIgnoreCase(orderItem
                                        .getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode
                                                .name()))) {//如果是对接酒店
                            if (SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name().equals(orderItem.getCancelStrategy())) {//不可退改的
                                set.add(orderItem.getOrderItemId());
                            }
                        } else {
                            set.add(orderItem.getOrderItemId());
                        }
                    }
                }
                if (!set.isEmpty()) {
                            String beforeFilterSetItems = "";
                            for (Long long1 : set) {
                                beforeFilterSetItems += long1 + "\t;";
                            }
                            LOG.info("beforeFilterSetItems : " + beforeFilterSetItems);
                        
                            
                            String excludedOrderItems = "";
                            
                            for (Long item : orderItemList) {
                                excludedOrderItems += item + ";\t";
                            }
                            LOG.info("excludedOrderItems : " + excludedOrderItems);
                            
                            //过滤不需要现在推送的保险子订单
                            for (Iterator<Long> it = set.iterator(); it.hasNext();) {
                                if (orderItemList.contains(it.next())) {
                                    it.remove();
                                }
                            }
                            
                            String afterFilterSetItems = "";
                            for (Long long1 : set) {
                                afterFilterSetItems += long1 + ";\t";
                            }
                            LOG.info("afterFilterSetItems : " + afterFilterSetItems);
                            
                    OrderSupplierOperateResult result = supplierOrderOperator.createSupplierOrder(orderId,set);
                    LOG.info("SupplierOrderProcesser.process: OrderPaymentMsg end result="+result.getErrMsg());
                    
                }
                
                OrdOrder ordOrder = orderUpdateService.queryOrdOrderByOrderId(orderId);
                ordOrder.setOrderItemList(orderUpdateService.queryOrderItemByOrderId(orderId));
                
                boolean isAccInsOrderSent = false;
                
                List<OrdOrderItem> accInsOrderItem = OrdOrderUtils.getAccInsOrderItem(ordOrder);
                for (OrdOrderItem ordOrderItem : accInsOrderItem) {
                    if (ordOrderItem.isSupplierOrderItem()) {
                        String invokeInterfacePfStatus = ordOrderItem.getInvokeInterfacePfStatus();
                        if (StringUtils.isNotBlank(invokeInterfacePfStatus) 
                                && (
                                        invokeInterfacePfStatus.equalsIgnoreCase(OrderEnum.INVOKE_INTERFACE_PF_STATUS.CREATED.name())
                                        ||
                                        invokeInterfacePfStatus.equalsIgnoreCase(OrderEnum.INVOKE_INTERFACE_PF_STATUS.CANCELED.name())
                                        ) ) {
                            isAccInsOrderSent = true;
                        }else {
                            isAccInsOrderSent = false;
                            break;
                        }
                    }
                }
                
                if (isAccInsOrderSent) {
                    orderUpdateService.updateInvokeInterfacePfStatus(orderId, OrderEnum.INVOKE_INTERFACE_PF_STATUS.CREATED.name());
                }
                
            }
        }else if (!isExcludedInsAcc) {
            LOG.info("SupplierOrderProcesser.process: SentInsAccOrderItems start OrderID="+orderId);
            Set<Long> set = new HashSet<Long>();
            for (Long orderItemId : orderItemList) {
                set.add(orderItemId);
            }
            
            if (!set.isEmpty()) {
                
                String orderItemIds = "";
                for (Long long1 : set) {
                    orderItemIds += long1 + "\t;";
                }
                LOG.info("To be sent to create INSACC orderItemIds : " + orderItemIds);
                
            OrderSupplierOperateResult result = supplierOrderOperator.createSupplierOrder(orderId,set);
            LOG.info("SupplierOrderProcesser.process: SentInsAccOrderItems end result="+result.getErrMsg());
            
            OrdOrder ordOrder = orderUpdateService.queryOrdOrderByOrderId(orderId);
            ordOrder.setOrderItemList(orderUpdateService.queryOrderItemByOrderId(orderId));
            
            boolean isAccInsOrderSent = false;
            
            List<OrdOrderItem>  accInsOrderItem= OrdOrderUtils.getAccInsOrderItem(ordOrder);
            
         /* //将意外险子单供应商推送状态置为cancel
            StringBuffer sb = new StringBuffer();
            for (Iterator<OrdOrderItem> it = accInsOrderItem
                    .iterator(); it.hasNext();) {
                OrdOrderItem item = it.next();
                item.setInvokeInterfacePfStatus(OrderEnum.INVOKE_INTERFACE_PF_STATUS.CANCELED.name());
                if(sb.length() > 0) {
                    sb.append(",");
                }
                
                sb.append(item.getOrderItemId());
            }
            //更新调用状态
            if(sb.length() > 0) {
                orderUpdateService.updateItemInvokeInterfacePfStatus(sb.toString(), OrderEnum.INVOKE_INTERFACE_PF_STATUS.CANCELED.name());
            }*/
            
            
            if (!result.isSuccess()) {
                LOG.error("SupplierOrderProcesser.process: createSupplierOrder fail, OrderID=" + orderId);
                if (result.isRetry()) {
                    ComJobConfig jobConfig = new ComJobConfig();
                    jobConfig.setCreateTime(new Date());
                    jobConfig.setJobType(ComJobConfig.JOB_TYPE.SUPP_ORDER_CREATE.name());
                    jobConfig.setObjectId(orderId);
                    jobConfig.setObjectType(ComJobConfig.OBJECT_TYPE.ORDER.name());
                    jobConfig.setPlanTime(DateUtil.getDateAfterMinutes(5));
                    jobConfig.setRetryCount(5L);
                    comJobConfigService.saveComJobConfig(jobConfig);
                    LOG.info("SupplierOrderProcesser.process: ComJobConfig(ID=" + jobConfig.getComJobConfigId() +
                            ", OrderID=" + orderId + ",type=SUPP_ORDER_CREATE) is added.");
                }
            }
        }
            
        }
    }


    private List<Long> getAccInsOrderItemList(List<OrdOrderItem> list) {
        List<Long> accInsOrderItemList = new ArrayList<Long>();
        
        for (OrdOrderItem ordOrderItem : list) {
            boolean insAccOrderItem = isInsAccOrderItem(ordOrderItem);
            if (insAccOrderItem) {
                accInsOrderItemList.add(ordOrderItem.getOrderItemId());
            }
        }
        return accInsOrderItemList;
    }

    /**
     * 
     * @Description: 根据orderId获取意外险子单
     * @author Wangsizhi
     * @date 2016-12-21 下午8:03:54
     */
    private boolean isInsAccOrderItem(OrdOrderItem ordOrderItem) {
        boolean result = false;
        String destBuAccFlag = ordOrderItem.getContentStringByKey("destBuAccFlag");
        if (StringUtils.isNotBlank(destBuAccFlag) && StringUtils.equalsIgnoreCase(destBuAccFlag, "Y")) {
            result = true;
        }
        
        return result;
    }
	
}
