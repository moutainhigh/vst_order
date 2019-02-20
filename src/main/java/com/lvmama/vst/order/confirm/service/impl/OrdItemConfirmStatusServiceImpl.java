package com.lvmama.vst.order.confirm.service.impl;

import com.alibaba.fastjson.JSON;
import com.lvmama.comm.pet.po.email.EmailContent;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.comm.pet.po.pub.ComMessage;
import com.lvmama.comm.pet.service.pub.ComMessageService;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.vst.goods.po.HouseControlVstPo;
import com.lvmama.dest.api.vst.goods.service.IHotelHouseControlVstApiService;
import com.lvmama.order.enums.OrderEnum;
import com.lvmama.order.workflow.api.IApiOrderWorkflowService;
import com.lvmama.order.workflow.vo.AuditActiviTask;
import com.lvmama.order.workflow.vo.NormalActivitiTask;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsHotelAdapterClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsLineTimePrice;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.Confirm_Enum.CONFIRM_CHANNEL_OPERATE;
import com.lvmama.vst.back.order.po.Confirm_Enum.CONFIRM_STATUS;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComLog.COM_LOG_LOG_TYPE;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif;
import com.lvmama.vst.ebooking.ebk.vo.SuppGoodsLineVO;
import com.lvmama.vst.neworder.order.EnhanceBeanUtils;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmProcessService;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmStatusService;
import com.lvmama.vst.order.confirm.service.status.ConfirmStatusManagerProxy;
import com.lvmama.vst.order.confirm.service.status.ConfirmStatusManagerService;
import com.lvmama.vst.order.confirm.vo.ComfirmSuppGoodsSpecDateParamVo;
import com.lvmama.vst.order.confirm.vo.ConfirmStatusParamVo;
import com.lvmama.vst.order.service.IOrdOrderHotelTimeRateService;
import com.lvmama.vst.order.service.IOrdOrderService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;
import com.lvmama.vst.pet.adapter.VstEmailServiceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("ordItemConfirmStatusService")
public class OrdItemConfirmStatusServiceImpl implements IOrdItemConfirmStatusService {
	private static final Logger LOG = LoggerFactory.getLogger(OrdItemConfirmStatusServiceImpl.class);
	@Autowired
	private ConfirmStatusManagerProxy confirmStatusManagerProxy;
	@Autowired
	private IOrderLocalService orderLocalService;
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	@Autowired
	private SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;
	@Autowired
	private PermUserServiceAdapter permUserServiceAdapter;
	@Autowired
	private SuppGoodsClientService suppGoodsClientService;
    @Autowired
    private VstEmailServiceAdapter vstEmailService;
    @Autowired
    private ComMessageService comMessageService;
    @Autowired
    private SuppSupplierClientService suppSupplierClientService;
	@Autowired
	private IOrdItemConfirmProcessService ordItemConfirmProcessService;

    @Autowired
    private IHotelHouseControlVstApiService hotelHouseControlVstApiService;
    @Autowired
    private IOrdOrderHotelTimeRateService ordOrderHotelTimeRateService;
    @Autowired
    private SuppGoodsHotelAdapterClientService suppGoodsHotelAdapterClientService;
	@Autowired
	private IOrdOrderService ordOrderService;

	@Autowired
	private IApiOrderWorkflowService apiOrderWorkflowService;

	@Override
	public ResultHandleT<ComAudit> updateChildConfirmStatus(OrdOrderItem orderItem,
			CONFIRM_STATUS newStatus, String operator, String memo) throws Exception{
		ConfirmStatusParamVo paramVo =confirmStatusManagerProxy.initParam(orderItem,newStatus, operator, memo);
		return confirmStatusManagerProxy.updateChildConfirmStatus(ConfirmStatusManagerService.CONFIRM_STATUS_SERVICE.DEFAULT_SERVICE
				,paramVo);
	}

	@Override
	public ResultHandleT<List<Object[]>> updateInConfirmStatusBySupplier(OrdOrder order
			,EbkCertif ebkCertif) throws Exception{
		ConfirmStatusParamVo paramVo =confirmStatusManagerProxy.initParam(order, ebkCertif);
		return confirmStatusManagerProxy.handle(ConfirmStatusManagerService.CONFIRM_STATUS_SERVICE.INCONFIRM_SUPPLIER_SERVICE
				,paramVo);
	}
	
	@Override
	public ResultHandleT<ComAudit> updateInConfirmStatusByUser(OrdOrderItem orderItem,
			CONFIRM_STATUS newStatus,String supplierNo, String operator, String memo,Long linkId, EbkCertif.EBK_CERTIFICATE_CONFIRM_CHANNEL confirmChannel)
					throws Exception{
		ConfirmStatusParamVo paramVo =confirmStatusManagerProxy.initParam(orderItem,newStatus, operator, memo);
		//酒店预定号,凭证关联号
		paramVo.setSupplierNo(supplierNo);
		paramVo.setLinkId(linkId);
		paramVo.setConfirmChannel(confirmChannel);
		return confirmStatusManagerProxy.handle(ConfirmStatusManagerService.CONFIRM_STATUS_SERVICE.INCONFIRM_SERVICE
				,paramVo);
	}

	@Override
	public ResultHandleT<ComAudit> updateConfirmedStatus(OrdOrderItem orderItem,
			CONFIRM_STATUS newStatus, String operator, String memo) throws Exception{
		ConfirmStatusParamVo paramVo =confirmStatusManagerProxy.initParam(orderItem,newStatus, operator, memo);
		return confirmStatusManagerProxy.handle(ConfirmStatusManagerService.CONFIRM_STATUS_SERVICE.REJECT_SERVICE
				,paramVo);
	}

	@Override
	public ResultHandleT<ComAudit> updateOrderConfirmAudit(Long auditId, String operator, String memo) throws Exception{
		ConfirmStatusParamVo paramVo =confirmStatusManagerProxy.initParam(null, auditId, operator, memo);
		return confirmStatusManagerProxy.updateOrderConfirmAudit(ConfirmStatusManagerService.CONFIRM_STATUS_SERVICE.BOOKING_CONFIRM_SERVICE, paramVo);
	}

	@Override
	public ResultHandle createConfirmOrder(OrdOrderItem orderItem,
			CONFIRM_CHANNEL_OPERATE operate, String operator) throws Exception{
		ConfirmStatusParamVo paramVo =confirmStatusManagerProxy.initParam(orderItem, operate, operator);
		return confirmStatusManagerProxy.handle(ConfirmStatusManagerService.CONFIRM_STATUS_SERVICE.NEW_ORDER_SERVICE
				,paramVo);
	}

	@Override
	public ResultHandle cancelConfirm(Long auditId, String operator) throws Exception{
		ConfirmStatusParamVo paramVo =confirmStatusManagerProxy.initParam(auditId, operator);
		return confirmStatusManagerProxy.handle(ConfirmStatusManagerService.CONFIRM_STATUS_SERVICE.CANCEL_CONFIRM_SERVICE
				,paramVo);
	}


	@Override
	public ResultHandle inquiryConfirm(OrdOrderItem orderItem, Long auditId, String operator, String memo,String resourceRetentionTime) throws Exception{
		ConfirmStatusParamVo paramVo =confirmStatusManagerProxy.initParam(orderItem, auditId, operator, memo,resourceRetentionTime);
		return confirmStatusManagerProxy.handle(ConfirmStatusManagerService.CONFIRM_STATUS_SERVICE.INQUIRY_CONFIRM_SERVICE
				,paramVo);
	}

	@Override
	public ResultHandleT<ComAudit> workbenchHandle(OrdOrderItem ordOrderItem, String orderMemo, String confirmId, CONFIRM_STATUS status, String operateName, Long linkId, EbkCertif.EBK_CERTIFICATE_CONFIRM_CHANNEL confirmChannel) throws Exception {
		LOG.info("workbenchHandle start...orderItemId="+ordOrderItem.getOrderItemId());
		ResultHandleT<ComAudit> handle = new ResultHandleT<ComAudit>();
		Long orderItemId = ordOrderItem.getOrderItemId();
		if (Confirm_Enum.CONFIRM_STATUS.INCONFIRM.name().equals(ordOrderItem.getConfirmStatus())) {
			//已审更新状态
			handle = this.updateInConfirmStatusByUser(
					ordOrderItem, status, confirmId, operateName, orderMemo, linkId, confirmChannel);
			if (handle.isSuccess() && handle.getReturnContent() != null) {
				Long orderId=ordOrderItem.getOrderId();
				OrdOrder ordOrder=ordOrderService.findByOrderId(orderId);
				Long categoryId=ordOrder.getCategoryId()==null?-1L:ordOrder.getCategoryId();
				String newWorkFlowFlag=ordOrder.getNewWorkflowFlag();
				LOG.info("workbenchHandle|orderItemId="+ordOrderItem.getOrderItemId()+",newWorkFlowFlag="+newWorkFlowFlag+",categoryId="+categoryId);
				//调用新版工作流
				if("Y".equalsIgnoreCase(newWorkFlowFlag)||"S".equalsIgnoreCase(newWorkFlowFlag)){
					ComAudit comAudit=handle.getReturnContent();
					com.lvmama.order.api.base.vo.RequestBody<AuditActiviTask> request=new com.lvmama.order.api.base.vo.RequestBody<>();
					AuditActiviTask auditActiviTask=new AuditActiviTask();
					EnhanceBeanUtils.copyProperties(comAudit, auditActiviTask);
					auditActiviTask.setOrderId(orderId);
					request.setT(auditActiviTask);
					com.lvmama.order.api.base.vo.ResponseBody<String> responseBody= apiOrderWorkflowService.completeTaskByAudit(request);
					if(null!=responseBody&&responseBody.isSuccess()){
						LOG.info("老工单完成新工作流成功:orderId={},orderItemId={}",orderId,ordOrderItem);
					}else{

						LOG.info("老工单完成新工作流失败:orderId={},orderItemId={},msg={}",orderId,ordOrderItem,responseBody.getErrorMessage());
					}
				}else {
					ordItemConfirmProcessService.completeTaskByAuditHasCompensated(ordOrderItem, handle.getReturnContent());
				}

			} else {
				LOG.info("workbenchHandle update updateInConfirmStatusByUser error!msg:" + handle.getMsg() + "orderItemId:" + orderItemId);
			}
		} else if (Confirm_Enum.CONFIRM_STATUS.FULL.name().equals(ordOrderItem.getConfirmStatus())
				|| Confirm_Enum.CONFIRM_STATUS.PECULIAR_FULL.name().equals(ordOrderItem.getConfirmStatus())
				|| Confirm_Enum.CONFIRM_STATUS.CHANGE_PRICE.name().equals(ordOrderItem.getConfirmStatus())) {
			//满房，特满，变价更新状态
			handle = this.updateConfirmedStatus(ordOrderItem, status, operateName, orderMemo);
			if (handle.isSuccess()) {
				//确认成功
				if (Confirm_Enum.CONFIRM_STATUS.SUCCESS.equals(status)) {
					String taskKey = ordOrderItem.hasSupplierApi() ? Confirm_Enum.API_CONFIRM_FAIL_USERTASK
							: Confirm_Enum.CONFIRM_FAIL_USERTASK;
					Long orderId=ordOrderItem.getOrderId();
					OrdOrder ordOrder=ordOrderService.findByOrderId(orderId);
					Long categoryId=ordOrder.getCategoryId()==null?-1L:ordOrder.getCategoryId();
					String newWorkFlowFlag=ordOrder.getNewWorkflowFlag();
					LOG.info("workbenchHandle(full)|orderItemId="+ordOrderItem.getOrderItemId()+",newWorkFlowFlag="+newWorkFlowFlag+",categoryId="+categoryId);
					//调用新版工作流
					if("Y".equalsIgnoreCase(newWorkFlowFlag)||"S".equalsIgnoreCase(newWorkFlowFlag)){
						LOG.info("完成酒店审核开始:orderItemId={}",ordOrderItem.getOrderItemId());
						com.lvmama.order.api.base.vo.RequestBody<NormalActivitiTask> request=new com.lvmama.order.api.base.vo.RequestBody<>();
						NormalActivitiTask normalActivitiTask=new NormalActivitiTask();
						normalActivitiTask.setOperatorName(operateName);
						normalActivitiTask.setOrderId(orderId);
						normalActivitiTask.setObjectId(ordOrderItem.getOrderItemId());
						normalActivitiTask.setObjectType(OrderEnum.OBJECT_TYPE.ORD_ORDER_ITEM.name());
						normalActivitiTask.setTaskKey(taskKey);
						request.setT(normalActivitiTask);
						LOG.info("完成酒店审核请求参数:{}",normalActivitiTask.toString());
						com.lvmama.order.api.base.vo.ResponseBody<String>  resp=apiOrderWorkflowService.completeTask(request);
						if(resp!=null&&resp.isSuccess()){
							LOG.info("完成酒店审核成功:orderItemId={}",ordOrderItem.getOrderItemId());
						}else {
							LOG.info("完成酒店审核失败:orderItemId={},err={}",ordOrderItem.getOrderItemId(),resp.getErrorMessage());
						}
					}else{
						ordItemConfirmProcessService
								.completeUserTaskByConfirm(ordOrderItem,
										taskKey, operateName);
					}

				}
			} else {
				LOG.info("workbenchHandle update handledConfirmStatus error!msg:" + handle.getMsg() + "orderItemId:" + orderItemId);
			}
		}
		return handle;
	}

	@Override
	public ResultHandle cancelOrder(Long orderId, String cancelCode,
			String reason, String operator, String memo) throws Exception{
		return orderLocalService.cancelOrder(orderId, cancelCode, reason, operator, memo);
	}

	@Override
	public ResultHandle closeFullhotelAndForbidSale(OrdOrderItem orderItem, String operator, String memo,String sourceType,List<Date> dates,List<Long> suppGoodsIds,String orderMemo)
			throws Exception {
		LOG.info("closeFullhotelAndForbidSale.start");
		ResultHandle result=new ResultHandle();
		//校验参数
		checkVaildParam(orderItem, operator, sourceType, dates, suppGoodsIds);
		SuppSupplier suppSupplier = suppSupplierClientService.findSuppSupplierById(orderItem.getSupplierId()).getReturnContent();
		orderItem.setSupplierName(suppSupplier.getSupplierName());
		//获取full hotel的goodsId
		if(suppGoodsIds==null||suppGoodsIds.size()<=0){
			suppGoodsIds=new ArrayList<Long>(Arrays.asList(orderItem.getSuppGoodsId()));
		}
		List<SuppGoods> suppGoodslist=suppGoodsHotelAdapterClientService.findSuppGoodsByIdList(suppGoodsIds).getReturnContent();

		if(dates==null||dates.size()<=0){
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())){
				Date specDate=DateUtil.toSimpleDate(DateUtil.formatDate(orderItem.getVisitTime(), DateUtil.PATTERN_yyyy_MM_dd));
				dates=new ArrayList<>(Arrays.asList(specDate));
			}else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())){
				List<Date> leaveDateList=ordOrderHotelTimeRateService.findOrdOrderItemHotelLastLeaveTimeByItemId(orderItem.getOrderItemId());
				if(leaveDateList!=null&&leaveDateList.size()>0){
					dates=leaveDateList;
				}
			}
		}
		if(suppGoodslist !=null&&suppGoodslist.size()>0){
			LOG.info("closeFullhotelAndForbidSale.suppGoodslist="+suppGoodslist.size());
			ResultHandleT<List<ComfirmSuppGoodsSpecDateParamVo>> resulthander=null;
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())){
				//通过suppGoodsId获取suppGoods信息
				resulthander = changeHotelcombTimePice(orderItem, suppGoodsIds, dates);
			}else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())){
				resulthander = closeHouseTimepriceForHotel(orderItem, operator, memo, dates, suppGoodslist);
			}
			LOG.info("closeFullhotelAndForbidSale.resulthander="+resulthander.getMsg());
			if(resulthander!=null&&resulthander.isSuccess()){
				for (ComfirmSuppGoodsSpecDateParamVo comfirmSuppGoodsSpecDateParamVo : resulthander.getReturnContent()) {
					comfirmSuppGoodsSpecDateParamVo.setSuppGoods(getSuppGoodsByGoodsId(comfirmSuppGoodsSpecDateParamVo.getSuppGoodsId(), suppGoodslist));
				}
				//发送邮件
				notifyEmail(orderItem,operator,memo,resulthander.getReturnContent());
				//推送消息
				notifyMessage(orderItem,operator,memo,resulthander.getReturnContent());
				//记录产品日志
	            insertProductAndOrderLog(orderItem, operator, memo,sourceType,resulthander.getReturnContent(),orderMemo);
	            
			}else{
				result.setMsg("关房失败");
			}
		}
		return result;
	}

	private SuppGoods getSuppGoodsByGoodsId(Long suppGoodsId,List<SuppGoods> suppGoodsList){
		for (SuppGoods suppGoods : suppGoodsList) {
			if(suppGoodsId.equals(suppGoods.getSuppGoodsId())){
				return suppGoods;
			}
		}
		return null;
	}
	private void checkVaildParam(OrdOrderItem orderItem, String operator, String sourceType, List<Date> dates,
			List<Long> suppGoodsIds) throws Exception {
		if(orderItem == null){
			throw new Exception("orderItem is null");
		}
		if(operator == null){
			throw new Exception("operator is null");
		}
		if(dates!=null&&dates.size()>10){
			throw new Exception("close house date list too long");
		}
		if("PECULIAR_FULL".equals(sourceType)||"CHANGE_PRICE".equals(sourceType)||"COMBO_HOTEL".equals(sourceType)||"CONSOLE_FULL".equals(sourceType)){
			if(suppGoodsIds==null||suppGoodsIds.size()<=0){
				throw new Exception("close house suppGoodsId is null");
			}
			if(dates==null||dates.size()<=0){
				throw new Exception("close house date is null");
			}
		}
	}

	private ResultHandleT<List<ComfirmSuppGoodsSpecDateParamVo>> closeHouseTimepriceForHotel(OrdOrderItem orderItem, String operator, String memo,
			List<Date> dates, List<SuppGoods> suppGoodslist) {
		ResultHandleT<List<ComfirmSuppGoodsSpecDateParamVo>> resulthander=new ResultHandleT<List<ComfirmSuppGoodsSpecDateParamVo>>();
		List<ComfirmSuppGoodsSpecDateParamVo> ComfirmSuppGoodsSpecDateParamVoList=new ArrayList<>();
		ComfirmSuppGoodsSpecDateParamVo comfirmSuppGoodsSpecDateParamVo=null;
		String msg="";
		if(dates!=null&&dates.size()>0){
			
			for (SuppGoods suppGoods : suppGoodslist) {
				List<Date> listspecDate=new ArrayList<>();
				for (Date date : dates) {
					HouseControlVstPo houseVstPo=new HouseControlVstPo();
					houseVstPo.setOperateContent(contentTextLog(orderItem,operator,memo,Arrays.asList(date),Arrays.asList(suppGoods)));
					houseVstPo.setSuppGoodsId(suppGoods.getSuppGoodsId());
					houseVstPo.setUserId(operator);
					houseVstPo.setStartDate(date);
					houseVstPo.setEndDate(date);
					RequestBody<HouseControlVstPo> request =new RequestBody<HouseControlVstPo>();
					request.setT(houseVstPo);
					request.setToken(Constant.DEST_BU_HOTEL_TOKEN);
					request.setUserId(operator);
					try{
						ResponseBody<Integer> result=hotelHouseControlVstApiService.closeHouse(request);
						if(result.isSuccess()){
							listspecDate.add(date);
						}
					}catch(Exception e){
						LOG.error("===ordOrderItem:"+orderItem.getOrderItemId()+"远程接口调用异常",e);
						resulthander.setMsg(msg);
					}
				}
				if(listspecDate.size()>0){
					comfirmSuppGoodsSpecDateParamVo=new ComfirmSuppGoodsSpecDateParamVo();
					comfirmSuppGoodsSpecDateParamVo.setSuppGoodsId(suppGoods.getSuppGoodsId());
					comfirmSuppGoodsSpecDateParamVo.setSpecDateList(listspecDate);
				}
				if(comfirmSuppGoodsSpecDateParamVo!=null){
					ComfirmSuppGoodsSpecDateParamVoList.add(comfirmSuppGoodsSpecDateParamVo);
				}
			}
		}
		if(ComfirmSuppGoodsSpecDateParamVoList.size()<=0){
			resulthander.setMsg("所以禁售失败");
		}
		resulthander.setReturnContent(ComfirmSuppGoodsSpecDateParamVoList);
		return resulthander;
	}

	private void notifyMessage(OrdOrderItem orderItem, String operator, String memo,List<ComfirmSuppGoodsSpecDateParamVo> listcssdpvo){
		Long managerId = orderItem.getManagerId();
        PermUser manager = permUserServiceAdapter.getPermUserByUserId(managerId);
		if(manager!=null){
			sendMessage(orderItem, operator, memo, manager,listcssdpvo);
		}
		PermUser managerContent=getContentMange(orderItem);
		if(managerContent!=null){
			if(manager!=null&&managerContent.getUserId().equals(manager.getUserId())){
				return;
			}
			sendMessage(orderItem, operator, memo, managerContent,listcssdpvo);
		}
	}

	private void sendMessage(OrdOrderItem orderItem, String operator, String memo, PermUser manager,List<ComfirmSuppGoodsSpecDateParamVo> listcssdpvo) {
		ComMessage comMessage = new ComMessage();
		comMessage.setSender(manager.getUserName());
		comMessage.setReceiver(manager.getUserName());
		comMessage.setContent(contentTextMessage(orderItem,operator,memo,listcssdpvo));
		comMessage.setStatus("CREATE");
		comMessage.setCreateTime(new Date());
		comMessageService.insertComMessage(comMessage);
	}
	
	private void insertProductAndOrderLog(OrdOrderItem orderItem, String operator, String memo,String sourceType,List<ComfirmSuppGoodsSpecDateParamVo> listcssdpvo,String orderMemo)throws Exception {
		
		Long objectId=null;
		if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())){
			objectId=orderItem.getProductId();
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())){
			objectId=orderItem.getSuppGoodsId();
		}
		for (ComfirmSuppGoodsSpecDateParamVo comfirmSuppGoodsSpecDateParamVo : listcssdpvo) {
			String contentTime="";
			for (Date specDate : comfirmSuppGoodsSpecDateParamVo.getSpecDateList()) {
				contentTime+=DateUtil.formatDate(specDate,DateUtil.PATTERN_yyyy_MM_dd)+",";
			}
			contentTime=contentTime.substring(0, contentTime.length()-1);
			String content=String.format("禁售商品【"+comfirmSuppGoodsSpecDateParamVo.getSuppGoodsId()+"】,"
					+ "禁售商品名称【"+comfirmSuppGoodsSpecDateParamVo.getSuppGoods().getGoodsName()+"】,禁售时间【%s】", contentTime);
			content+="原因:主订单号：【"+orderItem.getOrderId()+"】,子订单号：【"+orderItem.getOrderItemId()+"】"+memo;
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())){
				insertSuppGoodsLog(objectId, operator,content);
			}
			//记录订单日志
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
					orderItem.getOrderItemId(), 
					orderItem.getOrderItemId(), 
					operator, 
					content, 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName(),orderMemo);
/*			//记录主订单日志
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					orderItem.getOrderId(), 
					orderItem.getOrderId(), 
					operator, 
					content, 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName(),orderMemo);*/
			//记录酒店套餐关房订单日志
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM_COLSE_HOUSE,
					orderItem.getOrderId(), 
					orderItem.getOrderItemId(), 
					operator, 
					content, 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE_CLOSE_HOUSE.name(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE_CLOSE_HOUSE.getCnName(),orderMemo);
		}
		
		
		
		
	}

	private void insertSuppGoodsLog(Long ObjectId, String operator,String context) throws Exception {
		try {
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.SUPP_GOODS_GOODS,
					ObjectId, 
					ObjectId, 
					operator, 
					context, 
					ComLog.COM_LOG_LOG_TYPE.SUPP_GOODS_GOODS_TIME.name(), 
					ComLog.COM_LOG_LOG_TYPE.SUPP_GOODS_GOODS_TIME.getCnName()+"","");
		} catch (Exception e) {
		    LOG.error("Record Log failure ！Log type:" + COM_LOG_LOG_TYPE.SUPP_GOODS_GOODS_CHANGE.name());
		    LOG.error(e.getMessage());
		    throw new Exception("log insert fail");
		}
	}
	
	private String contentText(OrdOrderItem ordOrderItem, String operator, String memo,List<ComfirmSuppGoodsSpecDateParamVo> listcssdpvo){
		StringBuffer content = new StringBuffer();
		content.append("<br/>产品Id："+ordOrderItem.getProductId());
		content.append("<br/>产品名称："+ordOrderItem.getProductName());
		for (ComfirmSuppGoodsSpecDateParamVo cvo: listcssdpvo) {
			content.append("<br/>商品Id：");
			content.append(cvo.getSuppGoodsId()+",");
			content.append("<br/>商品名称：");
			content.append(cvo.getSuppGoods().getGoodsName()+",");
			content.append("<br/>时间：【");
			for (Date specDate : cvo.getSpecDateList()) {
				content.append(DateUtil.formatDate(specDate,DateUtil.PATTERN_yyyy_MM_dd)+",");
			}
			content=content.deleteCharAt(content.length()-1);
			content.append("】");
			content.append("<br/>");
		}
		content.append("<br/>");
		content.append("<br/>以上商品已被禁售，");
		content.append("<br/>禁售原因：订单号：【"+ordOrderItem.getOrderId()+"】子订单号：【"+ordOrderItem.getOrderItemId()+"】拒订原因【"+memo+"】");
		content.append("<br/>操作人：【"+operator+"】");
		return content.toString();
	}
	
	private String contentTextMessage(OrdOrderItem ordOrderItem, String operator, String memo,List<ComfirmSuppGoodsSpecDateParamVo> listcssdpvo){
		StringBuffer content = new StringBuffer();
		content.append("产品Id："+ordOrderItem.getProductId());
		content.append("    ,产品名称："+ordOrderItem.getProductName());
		for (ComfirmSuppGoodsSpecDateParamVo cvo: listcssdpvo) {
			content.append("    ,商品Id：");
			content.append(cvo.getSuppGoodsId()+",");
			content.append("    ,商品名称：");
			content.append(cvo.getSuppGoods().getGoodsName()+",");
			content.append("   ,时间：【");
			for (Date specDate : cvo.getSpecDateList()) {
				content.append(DateUtil.formatDate(specDate,DateUtil.PATTERN_yyyy_MM_dd)+",");
			}
			content=content.deleteCharAt(content.length()-1);
			content.append("】");
		}
		content.append("    ,以上商品已被禁售，");
		content.append("    禁售原因：订单号：【"+ordOrderItem.getOrderId()+"】子订单号：【"+ordOrderItem.getOrderItemId()+"】【"+memo+"】");
		content.append("    ,操作人：【"+operator+"】");
		return content.toString();
	}
	
	private String contentTextLog(OrdOrderItem ordOrderItem, String operator, String memo,List<Date> dates,List<SuppGoods> suppGoodslist){
		StringBuffer content = new StringBuffer();
		content.append("产品Id："+ordOrderItem.getProductId());
		content.append("    ,产品名称："+ordOrderItem.getProductName());
		content.append("    ,商品Id：");
		for (SuppGoods suppGood: suppGoodslist) {
			content.append(suppGood.getSuppGoodsId()+",");
		}
		content=content.deleteCharAt(content.length()-1);
		content.append("    ,商品名称：");
		for (SuppGoods suppGood: suppGoodslist) {
			content.append(suppGood.getGoodsName()+",");
		}
		if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(ordOrderItem.getCategoryId())){
			content.append("   ,时间：【"+DateUtil.formatDate(ordOrderItem.getVisitTime(),DateUtil.PATTERN_yyyy_MM_dd)+"】");
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(ordOrderItem.getCategoryId())){
			if(dates!=null&&dates.size()>0){
				content.append("   ,时间：【");
				for (Date date : dates) {
					content.append(DateUtil.formatDate(date,DateUtil.PATTERN_yyyy_MM_dd)+",");
				}
				content=content.deleteCharAt(content.length()-1);
				content.append("】");
			}else{
				
			}
		}
		content.append("    ,以上商品已被禁售，");
		content.append("    禁售原因：订单号：【"+ordOrderItem.getOrderId()+"】子订单号：【"+ordOrderItem.getOrderItemId()+"】【"+memo+"】");
		content.append("    ,操作人：【"+operator+"】");
		return content.toString();
	}

	private void notifyEmail(OrdOrderItem ordOrderItem, String operator, String memo,List<ComfirmSuppGoodsSpecDateParamVo> listcssdpvo)throws Exception {
		String fromAddress = "service@cs.lvmama.com";
        String fromName = "";
        String toAddress = "";
        String ccAddress = "";
        String subject = "";
        String contentText = "";
		try {
	            //子订单所属产品经理
			Long managerId = ordOrderItem.getManagerId();
			PermUser manager = permUserServiceAdapter.getPermUserByUserId(managerId);
            if (manager != null) {
                toAddress = manager.getEmail();
                LOG.info("OrdItemConfirmEmailServiceImpl.notifyManager toAddress="+toAddress);
            }
            //查询商品对应的维护人员
            PermUser contentmManager =getContentMange(ordOrderItem);
    		if(contentmManager != null){
    			if(manager!=null&&!contentmManager.getUserId().equals(manager.getUserId())){
    				toAddress +=","+ contentmManager.getEmail();
    				LOG.info("OrdItemConfirmStatusServiceImpl.notifyManager toAddress="+toAddress);
    			}else if(manager==null){
    				toAddress +=","+ contentmManager.getEmail();
    				LOG.info("OrdItemConfirmStatusServiceImpl.notifyManager toAddress="+toAddress);
    			}
    		}
    		//获取邮件信息
    		contentText=contentText(ordOrderItem,operator,memo,listcssdpvo);
    		LOG.info("OrdItemConfirmStatusServiceImpl.notifyManager contentText="+contentText);
    		//邮件主题
    		subject=getSubject(ordOrderItem);
	           
    		//发送邮件对象
    		EmailContent emailContent = new EmailContent();
    		emailContent.setFromAddress(fromAddress);
    		emailContent.setFromName(fromName);
    		emailContent.setSubject(subject);
    		emailContent.setToAddress(toAddress);
    		emailContent.setCcAddress(ccAddress);
    		emailContent.setContentText(contentText);
    		sendEmail(emailContent);
		}catch(Exception e){
			LOG.error("sendmail is error"+" orderItemId:"+ordOrderItem.getOrderItemId(),e);
			throw new Exception("sendmail is error");
	    }
	}
	
	private PermUser getContentMange(OrdOrderItem ordOrderItem){
		//查询商品对应的维护人员
        ResultHandleT<SuppGoods> resultSuppGoods= suppGoodsClientService.findSuppGoodsById(ordOrderItem.getSuppGoodsId());
        if(resultSuppGoods.isSuccess()){
        	SuppGoods suppgoods=resultSuppGoods.getReturnContent();
        	if(suppgoods!=null){
        		PermUser contentmManager = permUserServiceAdapter.getPermUserByUserId(suppgoods.getContentManagerId());
        		if(contentmManager != null){
        			return contentmManager;
        		}
        	}
        }
        return null;
	}
	
	/**
     * 发送邮件
     * @param emailContent
     */
    private void sendEmail(EmailContent emailContent) {
        try {
        	LOG.info("工作台（房态/价格）变更提醒邮件");
        	LOG.info("FromAddress:"+emailContent.getFromAddress());
        	LOG.info("FromName:"+emailContent.getFromName());
        	LOG.info("Subject:"+emailContent.getSubject());
            LOG.info("ToAddress:"+emailContent.getToAddress());
            LOG.info("CcAddress:"+emailContent.getCcAddress());
            LOG.info("ContentText:" + emailContent.getContentText());
            vstEmailService.sendEmail(emailContent);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(ExceptionFormatUtil.getTrace(e));
        }
    }
	
	private ResultHandleT<List<ComfirmSuppGoodsSpecDateParamVo>> changeHotelcombTimePice(OrdOrderItem orderItem, List<Long> suppGoodsIds, List<Date> dates) throws Exception {
		ResultHandleT<List<ComfirmSuppGoodsSpecDateParamVo>> resulthander=new ResultHandleT<List<ComfirmSuppGoodsSpecDateParamVo>>();
		List<ComfirmSuppGoodsSpecDateParamVo> ComfirmSuppGoodsSpecDateParamVoList=new ArrayList<>();
		ComfirmSuppGoodsSpecDateParamVo comfirmSuppGoodsSpecDateParamVo=null;
		for (Long suppGoodsId : suppGoodsIds) {
			List<Date> listspecDate=new ArrayList<>();
			for (Date specDate : dates) {
				Map<String, Object> parameters=new HashMap<>();
				parameters.put("suppGoodsId", suppGoodsId);
				parameters.put("specDate", specDate);
				ResultHandleT<List<SuppGoodsLineTimePrice>> resulteSuppGoodsTime=suppGoodsTimePriceClientService.findSuppGoodsLineTimePriceList(parameters);
				if(resulteSuppGoodsTime.isSuccess()){
					List<SuppGoodsLineTimePrice> timePriceList=resulteSuppGoodsTime.getReturnContent();
					SuppGoodsLineVO suppGoodsLineVO=new SuppGoodsLineVO();
					for (SuppGoodsLineTimePrice suppGoodsLineTimePrice : timePriceList) {
						suppGoodsLineTimePrice.setOnsaleFlag(Constants.N_FLAG);
					}
					suppGoodsLineVO.setTimePriceList(timePriceList);
					suppGoodsLineVO.setIsSetPrice(Constants.Y_FLAG);
					suppGoodsLineVO.setSpecDates(new String[]{DateUtil.formatDate(specDate,DateUtil.PATTERN_yyyy_MM_dd)});
					ResultHandleT<Integer> result=suppGoodsTimePriceClientService.editSuppGoodsLineTimePrice(suppGoodsLineVO);
					if(result==null || result.isFail()||result.getReturnContent()<=0){
						continue;
					}
					listspecDate.add(specDate);
				}else{
					LOG.error("query SuppGoodsBaseTimePrice is error"+" orderItemId:"+orderItem.getOrderItemId()+",resulteSuppGoodsTime:"+JSON.toJSONString(resulteSuppGoodsTime));
				}
			}
			if(listspecDate.size()>0){
				comfirmSuppGoodsSpecDateParamVo=new ComfirmSuppGoodsSpecDateParamVo();
				comfirmSuppGoodsSpecDateParamVo.setSuppGoodsId(suppGoodsId);
				comfirmSuppGoodsSpecDateParamVo.setSpecDateList(listspecDate);
			}
			if(comfirmSuppGoodsSpecDateParamVo!=null){
				ComfirmSuppGoodsSpecDateParamVoList.add(comfirmSuppGoodsSpecDateParamVo);
			}
		}
		if(ComfirmSuppGoodsSpecDateParamVoList.size()<=0){
			resulthander.setMsg("所以禁售失败");
		}
		resulthander.setReturnContent(ComfirmSuppGoodsSpecDateParamVoList);
		return resulthander;
	}
	
	/**
     * 邮件标题
     * @param ordOrderItem
     * @param hasStock
     * @param confirmStatus
     * @param fromName
     * @param toName
     * @return
     */
    private String getSubject(OrdOrderItem ordOrderItem) {
        String subject = "";
        subject="产品名称："+ordOrderItem.getProductName()+"（"+ordOrderItem.getProductId()+"）因订单号："+ordOrderItem.getOrderId()+",满房/变价，自动关房";
    /*    if (Confirm_Enum.CONFIRM_STATUS.CHANGE_PRICE.name().equals(confirmStatus)) {
            confirmStatusStr = "变价";
        } else {
            confirmStatusStr = "满房";
        }*/
        //邮件标题  例如：XX资审发送给XX产品经理--订单XXX保留房满房拒单，请处理
        return subject;
    }
}
