package com.lvmama.vst.order.client.ord.service.impl;

import java.util.*;

import javax.annotation.Resource;

import com.lvmama.order.workflow.api.IApiOrderWorkflowService;
import com.lvmama.order.workflow.vo.AuditActiviTask;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.neworder.order.EnhanceBeanUtils;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmProcessService;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmStatusService;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.vo.OrderSupplierOperateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.ord.service.DestOrderService;
import com.lvmama.vst.back.client.ord.service.OrdOrderItemPassCodeSMSService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.Confirm_Enum.API_ORDER_OPERATE_TYPE;
import com.lvmama.vst.back.order.po.Confirm_Enum.CONFIRM_STATUS;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.supp.po.SuppOrderResult;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.order.DestBuWorkflowUtils;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.vo.order.PriceInfo;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif;
import com.lvmama.vst.order.service.book.destbu.NewHotelBookComServiceImpl;
import com.lvmama.vst.order.service.refund.IOrderRefundRulesService;
import com.lvmama.vst.order.service.refund.adapter.OrderRefundProcesserAdapter;
import com.lvmama.vst.order.utils.PetUsrReceiversExchanger;
import com.lvmama.vst.pet.adapter.IReceiverUserServiceAdapter;
import com.lvmama.vst.pet.vo.PetUsrReceivers;

/**
 * 目的地订单远程接口实现
 * @version 1.0
 */
@Component("destOrderServiceRemote")
public class DestOrderClientServiceImpl implements DestOrderService {
	private static final Logger LOG = LoggerFactory.getLogger(DestOrderClientServiceImpl.class);
	@Autowired
	private OrderService orderService;
	@Autowired
	private IOrderAuditService orderAuditService;
	@Autowired
	private IOrderRefundRulesService orderRefundRulesService;
	@Autowired
	private OrdOrderItemPassCodeSMSService ordOrderItemPassCodeSMSServiceRemote;
	@Autowired
	private IOrdItemConfirmStatusService ordItemConfirmStatusService;
	@Autowired
	private IOrdItemConfirmProcessService ordItemConfirmProcessService;
	@Autowired
	private OrderRefundProcesserAdapter orderRefundProcesserAdapter;
	
	@Autowired
	private IOrderPriceService orderPriceService;
	@Autowired
	private    NewHotelBookComServiceImpl   newHotelBookComService ;
	@Autowired
	private ISupplierOrderOperator supplierOrderOperator;
	
	@Resource(name="receiverUserService")
	private IReceiverUserServiceAdapter receiverUserServiceAdapter;

	@Autowired
	private IApiOrderWorkflowService apiOrderWorkflowService;

	@Override
	public List<ComAudit> queryAuditListByParam(Map<String, Object> param) {
		return orderAuditService.queryAuditListByParam(param);
	}
	
	@Override
	public void completeTaskBySupplierConfirm(OrdOrder order, String supplierKey) {
		orderRefundProcesserAdapter.completeTaskBySupplierConfirm(order, supplierKey);
	}
	
	@Override
	public String getRouteOrderRefundRules(OrdOrder ordOrder) {
		if (ordOrder == null) {
			return null;
		}
		return orderRefundRulesService.getRouteOrderRefundRules(ordOrder);
	}
	
	@Override
	public boolean isCreateCertifConfirmForTicket(OrdOrder order, OrdOrderItem ordOrderItem) {
		if(DestBuWorkflowUtils.isCreateCertifConfirm(order)){
			LOG.info("orderId=" +order.getOrderId()
					+",needResourceConfirm=" +ordOrderItem.getNeedResourceConfirm()
					+",hasSupplierApi=" +ordOrderItem.hasSupplierApi()
					+",ebk_flag=" +ordOrderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.ebk_flag.name()));
			//询位单-fax
			if("true".equals(ordOrderItem.getNeedResourceConfirm())){
				if(!ordOrderItem.hasSupplierApi() 
						&& !"N".equals(ordOrderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.ebk_flag.name()))){
					return false;
				}
			}
			//新订单-ebk
			List<OrdOrderItem> orderItemList =new ArrayList<OrdOrderItem>();
			orderItemList.add(ordOrderItem);
			//自由行酒景
			return ordOrderItemPassCodeSMSServiceRemote.createOrderHandle(order, orderItemList);
		}
		return false;
	}

	@Override
	public ResultHandleT<OrdOrder> createOrder(DestBuBuyInfo buyInfo,
			String operatorId) {
		ResultHandleT<OrdOrder>  result  =	newHotelBookComService.createOrder(buyInfo, operatorId) ;
		return result;
	}
	
	@Override
	public ResultHandleT<ComAudit> updateChildConfirmStatus(OrdOrderItem orderItem, CONFIRM_STATUS newStatus
		      ,String operator,String memo) throws Exception {
		return ordItemConfirmStatusService.updateChildConfirmStatus(orderItem, newStatus, operator, memo);
	}

	@Override
	public ResultHandleT<List<Object[]>> updateInConfirmStatusBySupplier(OrdOrder order, EbkCertif ebkCertif) {
		ResultHandleT<List<Object[]>> result =new ResultHandleT<List<Object[]>>();
		try {
			result = ordItemConfirmStatusService.updateInConfirmStatusBySupplier(order, ebkCertif);
		} catch (Exception ex) {
			LOG.error("OrderId=" +  order.getOrderId()
					+ ",Exception:" +ex);
			result.setMsg(ex.getMessage());
		}
		return result;
	}
	
	@Override
	public SuppOrderResult createSupplierOrder(Long orderId, Long orderItemId,
			API_ORDER_OPERATE_TYPE operateType) throws BusinessException{
		//初始化子订单集合
		Set<Long> orderItemIds = new HashSet<Long>();
		orderItemIds.add(orderItemId);
		//创建供应商订单
		if(API_ORDER_OPERATE_TYPE.CREATE.name().equals(operateType.name())){
			OrderSupplierOperateResult supplierOrder =supplierOrderOperator.createSupplierOrder(orderId, orderItemIds);
			return parseSuppOrderResult(supplierOrder);
		}
		SuppOrderResult suppOrderResult =new SuppOrderResult();
		suppOrderResult.setErrMsg("operateType is error," +operateType);
		return suppOrderResult;
	}

	@Override
	public SuppOrderResult createSupplierOrder(Long orderId, Set<Long> orderItemList) throws BusinessException {
		OrderSupplierOperateResult supplierOrder =supplierOrderOperator.createSupplierOrder(orderId, orderItemList);
		return parseSuppOrderResult(supplierOrder);
	}

	@Override
	public void completeTaskByAudit(OrdOrderItem orderItem, ComAudit audit) {
		Long orderId=orderItem.getOrderId();
		Long itemId=orderItem.getOrderItemId();
		OrdOrder order=orderService.findByOrderId(orderId);
		String newWorkflowFlag=order.getNewWorkflowFlag();
		LOG.info("完成任务(新老)工作流orderid={},itemid={},newWorkflowFlag={}",orderId,itemId,newWorkflowFlag);

		if("Y".equalsIgnoreCase(newWorkflowFlag) || "S".equalsIgnoreCase(newWorkflowFlag)){
			com.lvmama.order.api.base.vo.RequestBody<AuditActiviTask> request=new com.lvmama.order.api.base.vo.RequestBody<>();
			AuditActiviTask auditActiviTask=new AuditActiviTask();
			EnhanceBeanUtils.copyProperties(audit, auditActiviTask);
			auditActiviTask.setOrderId(orderId);
			request.setT(auditActiviTask);
			com.lvmama.order.api.base.vo.ResponseBody<String> responseBody= apiOrderWorkflowService.completeTaskByAudit(request);
			if(null!=responseBody&&responseBody.isSuccess()){
				LOG.info("完成新工作流成功:orderId={},orderItemId={}",orderId,itemId);
			}else{
				LOG.info("完成新工作流失败:orderId={},orderItemId={},msg={}",orderId,itemId,responseBody.getErrorMessage());
			}
		}else {
			ordItemConfirmProcessService.completeTaskByAuditHasCompensated(orderItem, audit);
		}

	}
	
	
	@Override
	public PriceInfo countPrice(DestBuBuyInfo buyInfo) {
		try {
			return orderPriceService.countPriceComb(buyInfo);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			return null;
		}
	}
	
	@Override
	public void createContact(List<Person> list, String userId){
		if (list != null && list.size() > 0) {
			List<PetUsrReceivers> petUsrReceiversList = new ArrayList<PetUsrReceivers>();
			PetUsrReceivers receivers = null;
			for (Person person : list) {
				if (person != null) {
					receivers = PetUsrReceiversExchanger.changePerson2PetUsrReceivers(person, OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
					if (receivers != null) {
						petUsrReceiversList.add(receivers);
					}
				}
			}

			if (petUsrReceiversList.size() > 0) {
				receiverUserServiceAdapter.createContact(petUsrReceiversList, userId);//ReceiverUserServiceAdapterImpl
			}
		}

	}
	@Override
	public ResultHandle cancelOrder(Long orderId, String cancelCode, String reason, String operatorId) {
		return orderService.cancelOrder(orderId, cancelCode, reason, operatorId, null);
	}
	/**
	 * 解析供应商返回的结果
	 * @param supplierOrder
	 * @return
	 */
	private SuppOrderResult parseSuppOrderResult(OrderSupplierOperateResult supplierOrder){
		SuppOrderResult suppOrderResult =new SuppOrderResult();
		if(!supplierOrder.isSuccess() && supplierOrder.isRetry()){
			//重试供应商下单
			throw new BusinessException("createSupplierOrder errMsg：" +supplierOrder.getErrMsg());
		}
		if(supplierOrder.getSuppOrderResultList() !=null){
			return supplierOrder.getSuppOrderResultList().get(0);
		}else{
			suppOrderResult.setErrMsg("suppOrderResultList is null and errMsg=" +supplierOrder.getErrMsg());
		}
		return suppOrderResult;
	}

	@Override
	public void completeTaskByUserTask(OrdOrderItem orderItem) {
		String taskKey = orderItem.hasSupplierApi() ? Confirm_Enum.API_CONFIRM_FAIL_USERTASK
				: Confirm_Enum.CONFIRM_FAIL_USERTASK;
		ordItemConfirmProcessService.completeUserTaskByConfirm(orderItem, taskKey, "SYSTEM");
	}

}
