package com.lvmama.vst.order.confirm.inquiry.service.impl;

import com.lvmama.order.api.base.vo.RequestBody;
import com.lvmama.order.api.base.vo.ResponseBody;
import com.lvmama.order.enums.OrderEnum;
import com.lvmama.order.inquiry.api.service.IApiSupplierInquiryService;
import com.lvmama.order.inquiry.vo.certif.api.WorkOrderCertifVo;
import com.lvmama.order.inquiry.vo.comm.api.SupplierParamVo;
import com.lvmama.order.inquiry.vo.comm.response.InquirySuppOrderVo;
import com.lvmama.order.process.api.status.IApiOrderStatusUpdateProcessService;
import com.lvmama.order.route.service.IOrderRouteService;
import com.lvmama.order.vo.comm.OrderItemVo;
import com.lvmama.order.vo.comm.audit.ComAuditVo;
import com.lvmama.order.vo.comm.status.OrderConfirmStatusUpdateVo;
import com.lvmama.vst.back.order.po.Confirm_Enum.CONFIRM_STATUS;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.vo.ConfirmParamVo;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.supp.po.SuppOrderResult;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif.EBK_CERTIFICATE_CONFIRM_CHANNEL;
import com.lvmama.vst.order.confirm.inquiry.service.NewOrderConfirmService;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
@Component("newOrderConfirmService")
public class NewOrderConfirmServiceImpl implements NewOrderConfirmService {
	private static final Logger LOG = LoggerFactory.getLogger(NewOrderConfirmServiceImpl.class);

	@Autowired
	private IApiSupplierInquiryService apiSupplierInquiryService;
	@Autowired
	private IOrdItemConfirmStatusService ordItemConfirmStatusService;
	@Autowired
	private IOrderRouteService orderRouteService;

	@Resource(name = "apiOrderStatusUpdateProcessService")
	IApiOrderStatusUpdateProcessService apiOrderStatusUpdateProcessService;

	@Override
	public ResultHandleT<ComAudit> createConfirmOrder(ConfirmParamVo confirmParamVo) throws Exception {
		OrdOrderItem orderItem =confirmParamVo.getOrderItem();
		//初始化子订单集合
		List<Long> orderItemIds = new ArrayList<Long>();
		orderItemIds.add(orderItem.getOrderItemId());

		LOG.info("start createSupplierOrder orderId:"+orderItem.getOrderId()+",orderItemId:"+orderItem.getOrderItemId());
		Long orderId =orderItem.getOrderId();
		//组装请求参数
		RequestBody<SupplierParamVo> requestBody=fillRequestBdForCreate(orderId, orderItemIds);
		//调用询单系统
		ResponseBody<List<InquirySuppOrderVo>> responseBd = apiSupplierInquiryService.createInquiryTicket(requestBody);

		//解析供应商返回的结果
		if(responseBd.isFailure()){
			throw new BusinessException("远程调用询单系统失败," +orderId
					+"," +responseBd.getErrorMessage());
		}
		if(responseBd.getT() ==null){
			throw new BusinessException("responseBd.getT() is null ," +orderId
					+"," +responseBd.getErrorMessage());
		}
		SuppOrderResult result =new SuppOrderResult();
		EnhanceBeanUtils.copyProperties(responseBd.getT().get(0), result);

		CONFIRM_STATUS status =CONFIRM_STATUS.INCONFIRM;
		//对接是否及时确认
		if(orderItem.hasSupplierApi()){
			status=CONFIRM_STATUS.getCode(result.getStatus());
		}
		LOG.info("end createSupplierOrder orderId:"+orderItem.getOrderId()+",confirm status:"+status);
		//更新子订单确认状态
		return updateChildConfirmStatus(orderItem, status, "SYSTEM",null);
	}
	@Override
	public List<SuppOrderResult> createSupplierOrder(OrdOrder order) {
		List<SuppOrderResult> suppOrderResultList =new ArrayList<SuppOrderResult>();
		//组装请求参数
		RequestBody<SupplierParamVo> requestBody=fillRequestBdForCreate(order);
		//调用询单系统
		ResponseBody<List<InquirySuppOrderVo>>  responseBd = apiSupplierInquiryService.createInquiryTicket(requestBody);
		if(responseBd.isFailure()){
			LOG.error("远程调用询单系统失败," +order.getOrderId());
			return suppOrderResultList;
		}
		for (InquirySuppOrderVo orderResult : responseBd.getT()) {
			SuppOrderResult suppOrderResult=new SuppOrderResult();
			suppOrderResultList.add(suppOrderResult);
			EnhanceBeanUtils.copyProperties(orderResult,suppOrderResult);
		}
		return suppOrderResultList;
	}
	@Override
	public ResultHandle updateSupplierProcess(OrdOrderItem orderItem,
											  CONFIRM_STATUS newStatus, String supplierNo, String operator,
											  Long linkId, EBK_CERTIFICATE_CONFIRM_CHANNEL confirmChannel) {
		ResultHandle result =new ResultHandle();
		RequestBody<WorkOrderCertifVo> requestBody=new RequestBody<WorkOrderCertifVo>();
		WorkOrderCertifVo vo = new WorkOrderCertifVo();
		vo.setNewStatus(newStatus.name());
		vo.setSupplierNo(supplierNo);
		vo.setOperator(operator);
		vo.setLinkId(linkId);
		if(confirmChannel != null){
			vo.setConfirmChannel(confirmChannel.name());
		}
		vo.setOrderItemId(orderItem.getOrderItemId());
		requestBody.setT(vo);
		ResponseBody responseBd=apiSupplierInquiryService.orderProcess(requestBody);
		if(responseBd.isSuccess()){
			return result;
		}else{
			result.setMsg(responseBd.getMessage());
			return result;
		}
	}
	/**
	 * updateChildConfirmStatus
	 * @param orderItem
	 * @param newStatus
	 * @param operator
	 * @param memo
	 * @return
	 * @throws Exception
	 */
	private ResultHandleT<ComAudit> updateChildConfirmStatus(
			OrdOrderItem orderItem, CONFIRM_STATUS newStatus, String operator,
			String memo) throws Exception {
		ResultHandleT<ComAudit> handle=new ResultHandleT<ComAudit>();
		if(orderRouteService.isOrderItemRouteToNewSys(orderItem.getOrderItemId())){
			LOG.info("enter new order system updateChildConfirmStatus,orderItemId:" + orderItem.getOrderItemId());
			RequestBody<OrderConfirmStatusUpdateVo>  request=new RequestBody<OrderConfirmStatusUpdateVo> ();
			OrderConfirmStatusUpdateVo updateVo=new OrderConfirmStatusUpdateVo();
			OrderItemVo orderItemVo=new OrderItemVo();
			//EnhanceBeanUtils.copyProperties(orderItem,orderItemVo);
			orderItemVo.setOrderId(orderItem.getOrderId());
			orderItemVo.setOrderItemId(orderItem.getOrderItemId());
			updateVo.setOrderItem(orderItemVo);
			com.lvmama.order.enums.Confirm_Enum.CONFIRM_STATUS status=com.lvmama.order.enums.Confirm_Enum.CONFIRM_STATUS.getCode(newStatus.name());
			updateVo.setNewStatus(status);
			updateVo.setOperator(operator);
			updateVo.setMemo(memo);
			updateVo.setAuditType(OrderEnum.CONFIRM_AUDIT_TYPE.NEW_ORDER_AUDIT);
			request.setT(updateVo);
			LOG.info("status:"+status+",operator"+operator+",memo:"+memo);

			ResponseBody<ComAuditVo> response= apiOrderStatusUpdateProcessService.updateChildConfirmStatus(request);
			LOG.info("itemId:" +orderItem.getOrderItemId() +",response:"+response);

			if(response.isSuccess()){
				ComAuditVo comAuditVo=response.getT();
				if(comAuditVo != null){
					ComAudit comAudit=new ComAudit();
					EnhanceBeanUtils.copyProperties(comAuditVo,comAudit);
					handle.setReturnContent(comAudit);
				}
			}else{
				handle.setMsg(response.getErrorMessage());
			}
		}else{
			LOG.info("enter new order system updateChildConfirmStatus,orderItemId:" + orderItem.getOrderItemId());
			handle=ordItemConfirmStatusService.updateChildConfirmStatus(orderItem, newStatus, operator,memo);
		}

		return handle;
	}
	/**
	 * fillRequestBdForCreate
	 * @param orderId
	 * @param orderItemIds
	 * @return
	 */
	private RequestBody<SupplierParamVo> fillRequestBdForCreate(Long orderId, List<Long> orderItemIds){
		RequestBody<SupplierParamVo> requestBody =new RequestBody<SupplierParamVo>();

		//初始化请求参数
		SupplierParamVo supplierParamVo =new SupplierParamVo();
		supplierParamVo.setOrderId(orderId);
		supplierParamVo.setOrderItemList(orderItemIds);
		requestBody.setT(supplierParamVo);

		return requestBody;
	}
	/**
	 * fillRequestBdForCreate
	 * @param order
	 * @return
	 */
	private RequestBody<SupplierParamVo> fillRequestBdForCreate(OrdOrder order){
		Long orderId =order.getOrderId();
		List<Long> orderItemIds =new ArrayList<Long>();
		for(OrdOrderItem orderItem :order.getOrderItemList()){
			orderItemIds.add(orderItem.getOrderItemId());
		}
		return fillRequestBdForCreate(orderId, orderItemIds);
	}

}
