package com.lvmama.vst.order.confirm.client;

import com.lvmama.vst.back.client.ord.service.DestOrderWorkflowService;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.po.Confirm_Enum.CONFIRM_AUDIT_TYPE;
import com.lvmama.vst.back.order.po.Confirm_Enum.CONFIRM_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.AUDIT_OBJECT_TYPE;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.ebooking.client.ebk.serivce.EbkFaxTaskClientService;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif;
import com.lvmama.vst.ebooking.fax.po.EbkFaxTask;
import com.lvmama.vst.order.client.ord.service.impl.AbstractOrderClientService;
import com.lvmama.vst.order.confirm.ord.IOrdStatusManageConfirmProcessService;
import com.lvmama.vst.order.confirm.ord.IOrdStatusManageConfirmService;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmProcessService;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmStatusService;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderAuditSortRuleService;
import com.lvmama.vst.order.service.IOrderLocalService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @version 1.0
 */
@Component("destOrderWorkflowServiceRemote")
public class DestOrderWorkflowServiceImpl extends AbstractOrderClientService
		implements DestOrderWorkflowService{
	private static final Log LOG = LogFactory.getLog(DestOrderWorkflowServiceImpl.class);
	
	@Autowired
	private IOrderAuditService orderAuditService;
	@Resource(name="allocationMessageProducer")
	private TopicMessageProducer allocationMessageProducer;
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	@Autowired
	private IOrderLocalService orderLocalService;
	@Autowired
	private IOrderAuditSortRuleService orderAuditSortRuleService;
	@Autowired
	private EbkFaxTaskClientService ebkFaxTaskClientService;
	@Autowired
	private IOrdStatusManageConfirmProcessService ordStatusManageConfirmProcessService;
    @Autowired
    private IOrdStatusManageConfirmService ordStatusManageConfirmService;
	@Autowired
	private IOrdItemConfirmStatusService ordItemConfirmStatusService;
	@Autowired
	private IOrdItemConfirmProcessService ordItemConfirmProcessService;
	@Autowired
	private OrdOrderStockDao orderStockDao;
	
	@Override
	public Long createTask(Long orderId, Long objectId ,CONFIRM_AUDIT_TYPE type, String operator) {
		if (orderId != null && orderId.equals(objectId)) {
			return createTask(orderId, objectId, AUDIT_OBJECT_TYPE.ORDER, type, operator,null);
		}else{
			return createTask(orderId, objectId, AUDIT_OBJECT_TYPE.ORDER_ITEM, type, operator,null);
		}

	}
	@Override
	public Long createTask(Long orderId, Long objectId ,CONFIRM_AUDIT_TYPE type, String operator, Date remindTime) {
		if (orderId != null && orderId.equals(objectId)) {
			return createTask(orderId, objectId, AUDIT_OBJECT_TYPE.ORDER, type, operator,remindTime);
		}else{
			if(remindTime ==null){
				//传真计划发送时间
				ResultHandleT<List<EbkFaxTask>> resultHandleT = ebkFaxTaskClientService
						.selectEbkFaxTaskListByOrderItemId(objectId);
				if(resultHandleT.isSuccess() && CollectionUtils.isNotEmpty(resultHandleT.getReturnContent())){
					EbkFaxTask ebkFaxTask = resultHandleT.getReturnContent().get(0);
					if(ebkFaxTask != null && ebkFaxTask.getPlanTime() != null){
						remindTime = ebkFaxTask.getPlanTime();
					}
				}
			}
			return createTask(orderId, objectId, AUDIT_OBJECT_TYPE.ORDER_ITEM, type, operator,remindTime);
		}
	}
	@Override
	public void createTaskLog(Long orderId, Long objectId, CONFIRM_AUDIT_TYPE type, String operator) {
		if(type ==null) return;
		AUDIT_OBJECT_TYPE objectType =null;
		if (orderId != null && orderId.equals(objectId)) {
			objectType = AUDIT_OBJECT_TYPE.ORDER;
		}else{
			objectType = AUDIT_OBJECT_TYPE.ORDER_ITEM;
		}
		try {
			ComLog.COM_LOG_OBJECT_TYPE logObjectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER;
			if(AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(objectType)){
				logObjectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM;
			}
			// 添加操作日志
			lvmmLogClientService.sendLog(logObjectType,
					orderId,
					objectId,
					operator,
					 "新增[" + type.getCnName() + "]日志",
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CREATE_AUDIT.name(),
					"新增日志[" +type.getCnName()+"]",null);
		} catch (Exception e) {
			LOG.error("objectId=" +objectId +",objectType=" +objectType.name()
					+",addConfirmStatusLog failure " ,e);
		}

	}
	/**
	 * createTask
	 * */
	private Long createTask(Long orderId, Long objectId, AUDIT_OBJECT_TYPE objectType,
			CONFIRM_AUDIT_TYPE type, String operator, Date remindTime) {
		LOG.info("createTask，objectId:" +objectId +",type:" +type +",operator:" +operator);
		if(type ==null) return null;

		boolean isNeedQueryAudit = true;
		if(AUDIT_OBJECT_TYPE.ORDER_ITEM.equals(objectType)){
			//下面3个状态不查询，直接创建活动
			if(CONFIRM_AUDIT_TYPE.FULL_AUDIT.equals(type)
					|| CONFIRM_AUDIT_TYPE.PECULIAR_FULL_AUDIT.equals(type)
					|| CONFIRM_AUDIT_TYPE.CHANGE_PRICE_AUDIT.equals(type)){
				isNeedQueryAudit = false;
			}
		}
		if(isNeedQueryAudit){
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("auditType", type.name());
			params.put("objectType", objectType);
			params.put("objectId", objectId);
			List<ComAudit> auditList = orderAuditService.queryAuditListByCondition(params);
			if(auditList != null && auditList.size() > 0){
				return auditList.get(0).getAuditId();
			}
		}
		ComAudit audit = new ComAudit();
		audit.setAuditStatus(OrderEnum.AUDIT_STATUS.POOL.name());
		audit.setAuditType(type.name());
		audit.setCreateTime(new Date());
		audit.setObjectId(objectId);
		audit.setObjectType(objectType.name());
		if("SYSTEM".equals(audit.getOperatorName())){
			audit.setAuditFlag("SYSTEM");// 标记为系统自动过
		}
		//设置提醒时间和排序值
		if(AUDIT_OBJECT_TYPE.ORDER_ITEM.equals(objectType)){
			OrdOrderItem ordOrderItem = orderLocalService.getOrderItem(objectId);
			if(ordOrderItem != null){
				List<OrdOrderStock> orderStockList = orderStockDao.selectByOrderItemId(ordOrderItem.getOrderItemId());
                ordOrderItem.setOrderStockList(orderStockList);
				if(CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT.name().equals(type.name())){
					  audit.setStockFlag(ordOrderItem.isRoomReservations() ? Constants.Y_FLAG :Constants.N_FLAG);
				}
				ComAuditSortRule comAuditSortRule = orderAuditSortRuleService
						.getComAuditSortRuleByOrderItem(ordOrderItem);
				if(comAuditSortRule != null){
					audit.setRemindTime(DateUtil.DsDay_Second(new Date(),
							comAuditSortRule.getRemindTime().intValue()));
					//如果传真计划发送时间不为空，且比分组中调整时间大，使用计划发送时间替换分组中的调整时间
					if(remindTime != null && DateUtil.diffSec(remindTime, audit.getRemindTime()) > 0){
						audit.setRemindTime(remindTime);
					}
					audit.setSeq(comAuditSortRule.getSeq());
				}
			}
		}
		int result = orderAuditService.saveAudit(audit);
		LOG.info("createTask，objectId:" +objectId +",result:" +result);
		if(result > 0){
			//活动创建日志
			createTaskLog(orderId, audit);
			allocationAudit(audit);
		}
		return audit.getAuditId();
	}
	/**
	 * 分单
	 * @param audit 活动
	 */
	private void allocationAudit(ComAudit audit) {
		//任务没有指到到操作人的时候做一次分单
		if(StringUtils.isEmpty(audit.getOperatorName())
				|| "SYSTEM".equals(audit.getOperatorName())){
			try{
				allocationMessageProducer.sendMsg(
						MessageFactory.newOrderAllocationMessage(audit.getAuditId(), "TRUE"));
			}catch(Exception ex){
				LOG.error(ExceptionFormatUtil.getTrace(ex));
			}
		}
	}
	/**
	 * 生成创建活动任务日志
	 * @param orderId
	 * @param audit 活动
	 */
	private void createTaskLog(Long orderId, ComAudit audit) {
		try {
			ComLog.COM_LOG_OBJECT_TYPE logObjectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER;
			if(AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())){
				logObjectType=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM;
			}
			//日志备注中添加订单可操作时间
			String memo = "";
			if (audit.getRemindTime() != null){
				String remindTime = DateUtil.formatDate(audit.getRemindTime(), DateUtil.HHMMSS_DATE_FORMAT);
				memo = "实际入库展示时间为：" + remindTime;
			}

			lvmmLogClientService.sendLog(logObjectType,
					orderId,
					audit.getObjectId(),
					"SYSTEM",
					"创建编号为["+audit.getObjectId()+"]的订单活动["
							+CONFIRM_AUDIT_TYPE.getCnName(audit.getAuditType())+"]",
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CREATE_AUDIT.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CREATE_AUDIT.getCnName()
						+"["+CONFIRM_AUDIT_TYPE.getCnName(audit.getAuditType())+"]",
					memo);
		} catch (Exception e) {
			LOG.info(e.getMessage());
		}
	}
	@Override
	public String getAuditTypeByConfirmStatus(Long orderItemId) {
		//加载子订单确认状态
		OrdOrderItem orderItem=orderLocalService.getOrderItem(orderItemId);
		if(orderItem == null) return null;
		//审核类型
		CONFIRM_AUDIT_TYPE type = getAuditTypeByConfirmStatus(orderItem.getConfirmStatus());
		if(type ==null) return null;

		return type.name();
	}
	@Override
	public CONFIRM_AUDIT_TYPE getAuditTypeByConfirmStatus(String confirmStatus) {
		//审核类型
		CONFIRM_AUDIT_TYPE type = null;
		if(CONFIRM_STATUS.INCONFIRM.name().equals(confirmStatus)){
			type =CONFIRM_AUDIT_TYPE.INCONFIRM_AUDIT;
		}else if(CONFIRM_STATUS.FULL.name().equals(confirmStatus)){
			type =CONFIRM_AUDIT_TYPE.FULL_AUDIT;
		}else if(CONFIRM_STATUS.PECULIAR_FULL.name().equals(confirmStatus)){
			type =CONFIRM_AUDIT_TYPE.PECULIAR_FULL_AUDIT;
		}else if(CONFIRM_STATUS.CHANGE_PRICE.name().equals(confirmStatus)){
			type =CONFIRM_AUDIT_TYPE.CHANGE_PRICE_AUDIT;
		}
		return type;
	}

	@Override
	public void completeCertificateTask(Long objectId,  AUDIT_OBJECT_TYPE objectType) {
		String memo = "供应商通知己确认，系统自动通过凭证确认";
		ResultHandle result =null;
		LOG.info("objectId=" +objectId
				+",objectType=" +objectType.name());

		if(AUDIT_OBJECT_TYPE.ORDER.equals(objectType)){
			OrdOrder order = orderLocalService.querySimpleOrder(objectId);
			result =orderLocalService.updateCertificateStatus(order, new OrderAttachment(), "SYSTEM", memo);

		}else if(AUDIT_OBJECT_TYPE.ORDER_ITEM.equals(objectType)){
			result =orderLocalService.updateChildCertificateStatus(objectId, "SYSTEM", memo);
		}
		if (result !=null && !result.isSuccess()) {
			LOG.error("completeCertificateTask,failure：" +objectId);
		}
	}
	@Override
	public boolean completeResourceAmpleTask(Long objectId, AUDIT_OBJECT_TYPE objectType, String memo){
		LOG.info("objectId=" +objectId
				+",objectType=" +objectType.name());
		ResultHandle result = ordStatusManageConfirmProcessService.executeUpdateOrderResourceStatusAmple(objectId, objectType
				, "", "SYSTEM", memo);
		if (!result.isSuccess()) {
			LOG.error("completeResourceAmpleTask,failure：" +objectId);
		}
		return result.isSuccess();
	}
	@Override
	public CONFIRM_STATUS convertConfirmStatusByEbk(String reason) {
		//默认满房
		CONFIRM_STATUS status =CONFIRM_STATUS.FULL;
		if(EbkCertif.REASON_DESC.UNABLE_USER_REQUIREMENTS.name().equals(reason)){
			status =CONFIRM_STATUS.PECULIAR_FULL;
		}else if(EbkCertif.REASON_DESC.PRICE_CHANGE.name().equals(reason)){
			status =CONFIRM_STATUS.CHANGE_PRICE;
		}
		return status;
	}
	@Override
	public void markTaskValid(Long objectId, AUDIT_OBJECT_TYPE objectType) {
		//更新子订单确认活动
		Map<String,Object> params= new HashMap<String, Object>();
		params.put("objectId",objectId);
		params.put("objectType", objectType.name());
		params.put("auditStatusArray", Arrays.asList(OrderEnum.AUDIT_STATUS.UNPROCESSED.name()
				,OrderEnum.AUDIT_STATUS.POOL.name()));
		List<ComAudit> auditList =orderAuditService.queryAuditListByParam(params);
		//不更改的审核类型活动
		String[] AUDIT_TYPE_LIST ={CONFIRM_AUDIT_TYPE.CANCEL_CONFIRM_AUDIT.name()};
		if(!auditList.isEmpty()){
			for(ComAudit audit : auditList){
				if(ArrayUtils.contains(AUDIT_TYPE_LIST, audit.getAuditType())) continue;

				if(CONFIRM_AUDIT_TYPE.isConfirmAuditType(audit.getAuditType())){
					orderAuditService.markValid(audit.getAuditId());
				}
			}
		}
		//更新已处理活动状态为不可再分单
		params.remove("auditStatusArray");
		params.put("auditStatus", OrderEnum.AUDIT_STATUS.PROCESSED.name());
		auditList =orderAuditService.queryAuditListByParam(params);
		if(!auditList.isEmpty()){
			for (ComAudit audit : auditList) {
				orderAuditService.markCanNotReaudit(audit.getAuditId());
			}
		}
	}

	@Override
	public ResultHandleT<ComAudit> completeTask(Long auditId, String operator) {
        return ordStatusManageConfirmService.updateChildConfirmAudit(auditId, operator);
	}


	@Override
	public ResultHandleT<ComAudit> workbenchHandle(OrdOrderItem ordOrderItem, String orderMemo, String confirmId, CONFIRM_STATUS status, String operateName, Long linkId, EbkCertif.EBK_CERTIFICATE_CONFIRM_CHANNEL confirmChannel) throws Exception {
		return ordItemConfirmStatusService.workbenchHandle(ordOrderItem, orderMemo, confirmId, status, operateName, linkId, confirmChannel);
	}

}
