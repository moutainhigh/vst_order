package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvmama.vst.comlog.LvmmLogClientService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.comm.pet.po.email.EmailAttachment;
import com.lvmama.comm.pet.po.email.EmailContent;
import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.order.po.OrderAttachment;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdAdditionStatusDAO;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdAdditionStatusService;
import com.lvmama.vst.order.service.IOrdOrderPackService;
import com.lvmama.vst.order.service.IOrderAttachmentService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderSendSmsService;
import com.lvmama.vst.pet.adapter.VstEmailServiceAdapter;

@Service
public class OrdAdditionStatusServiceImpl implements IOrdAdditionStatusService {

	private static final Log LOG = LogFactory
			.getLog(OrdAdditionStatusServiceImpl.class);
	@Autowired
	private OrdAdditionStatusDAO ordAdditionStatusDao;
	

	@Autowired
	private IOrderAttachmentService orderAttachmentService;

	@Autowired
	private VstEmailServiceAdapter vstEmailService;
	
	@Autowired
	private IOrderAuditService orderAuditService;
	
	@Autowired
	private IOrdOrderPackService ordOrderPackService;
	
	@Autowired
	private IOrderSendSmsService orderSendSmsService;
	
	@Autowired
	private IComplexQueryService complexQueryService;

	
	
	//公共操作日志业务
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Override
	public int addOrdAdditionStatus(OrdAdditionStatus ordAdditionStatus) {
		// TODO Auto-generated method stub
		return ordAdditionStatusDao.insert(ordAdditionStatus);
	}
	@Override
	public OrdAdditionStatus findOrdAdditionStatusById(Long id) {
		// TODO Auto-generated method stub
		return ordAdditionStatusDao.selectByPrimaryKey(id);
	}
	@Override
	public List<OrdAdditionStatus> findOrdAdditionStatusList(
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		return ordAdditionStatusDao.selectByParams(params);
	}
	@Override
	public int updateByPrimaryKeySelective(OrdAdditionStatus ordAdditionStatus) {
		// TODO Auto-generated method stub
		return ordAdditionStatusDao.updateByPrimaryKeySelective(ordAdditionStatus);
	}
	


	
	/**
	 * 上传出团通知书
	 * @param fileId
	 * @param fileName
	 * @param memo
	 * @param orderId
	 */
	public boolean saveNoticeRegiment(Long fileId, String fileName, String memo,Long orderId,String loginUserId) {
		
		
		Map<String, Object> paramsAdditionStatus = new HashMap<String, Object>();
		paramsAdditionStatus.put("orderId", orderId);
		paramsAdditionStatus.put("statusType", OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.getCode());
		paramsAdditionStatus.put("status", OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.NO_UPLOAD.getCode());
		
		List<OrdAdditionStatus> ordAdditionStatusList=ordAdditionStatusDao.selectByParams(paramsAdditionStatus);
		
		if (!ordAdditionStatusList.isEmpty()) {//再次上传不需要修改原来状态
			
			OrdAdditionStatus ordAdditionStatusOld=ordAdditionStatusList.get(0);
			
			OrdAdditionStatus ordAdditionStatusNew=new OrdAdditionStatus();
			ordAdditionStatusNew.setOrdAdditionStatusId(ordAdditionStatusOld.getOrdAdditionStatusId());
			ordAdditionStatusNew.setStatus(OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.UPLOAD_NO_SEND.getCode());
			
			ordAdditionStatusDao.updateByPrimaryKeySelective(ordAdditionStatusNew);
		}
		
		
		//创建附件表记录
		OrderAttachment orderAttachment = new OrderAttachment();
		orderAttachment.setOrderId(orderId);
		orderAttachment.setAttachmentType(OrderEnum.ATTACHMENT_TYPE.NOTICE_REGIMENT.name());
		orderAttachment.setAttachmentName(fileName);
		orderAttachment.setMemo(memo);
		orderAttachment.setCreateTime(Calendar.getInstance().getTime());
		orderAttachment.setFileId(fileId);
		orderAttachment.setConfirmType(null);//普通附件没有
		orderAttachment.setOrderItemId(null);
		orderAttachment.setFileType(OrderEnum.FILE_TYPE.COMMON.getCode());
		
		orderAttachmentService.saveOrderAttachment(orderAttachment,loginUserId,memo);
		
		return true;
		
	}

	
	public boolean updateSendNoticeRegiment(Long orderId, String email,String loginUserId) {
		
		
		Map<String, Object> paramPack = new HashMap<String, Object>();
		paramPack.put("orderId", orderId);//订单号

		OrdOrder order=this.complexQueryService.queryOrderByOrderId(orderId);
		String productName=order.getOrderProductName();
		
		String contentText="您好，您在驴妈妈旅游网预订的"+productName+"，出团通知书已在附件中，请查收。祝您旅途愉快。 电子邮件的附件内容： （1） 出团通知书；";
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("orderId",orderId);
		param.put("attachmentType",OrderEnum.ATTACHMENT_TYPE.NOTICE_REGIMENT.name());
		param.put("_orderby", "ORD_ATTACHMENT.CREATE_TIME");
		param.put("_order", "DESC");
		List<OrderAttachment>  orderAttachmentList=orderAttachmentService.findOrderAttachmentByCondition(param);

		OrderAttachment orderAttachment=null;
		if (!orderAttachmentList.isEmpty()) {
			orderAttachment=orderAttachmentList.get(0);
		}else{
			throw new BusinessException("未找到出团通知书");
		}
		String fileType=orderAttachment.getFileType();
		if (OrderEnum.FILE_TYPE.SMS.getCode().equals(fileType)) {
			String content=(String)orderAttachment.getContentValueByKey(OrderEnum.ORDER_ATTACHMENT_CONTENT.sms.name());
			String mobile=order.getContactPerson().getMobile();
			LOG.info("开始发送出团通知短信");
			orderSendSmsService.sendSmsByCustom(orderId, content, loginUserId, mobile);
			LOG.info("结束出团通知短信");
		}else{
			EmailContent emailContent = new EmailContent();
			emailContent.setContentText(contentText);
			emailContent.setFromAddress("service@cs.lvmama.com");
			emailContent.setFromName("驴妈妈旅游网"); 
			emailContent.setSubject("订单号："+orderId+"的出团通知书");
			emailContent.setToAddress(email);
			
			List<EmailAttachment> emailAttachments = new ArrayList<EmailAttachment>();
			EmailAttachment emailAttachment = new EmailAttachment();
			emailAttachment.setFileId(orderAttachment.getFileId());
			emailAttachment.setFileName(orderAttachment.getAttachmentName());
			emailAttachments.add(emailAttachment);
			
			LOG.info("开始发送出团通知书");
			
			vstEmailService.sendEmailDirectFillAttachment(emailContent, emailAttachments);
			
			LOG.info("结束发送出团通知书");
			
			//短信：订单正常-出团通知书发送
			orderSendSmsService.sendSms(orderId, OrdSmsTemplate.SEND_NODE.ORDER_NORMAL_GROUP_NOTICE_SENT, loginUserId);
			
		}
		
		updateOrdAdditionStatusSent(orderId,loginUserId);
		
		

		
		return true;
	}
	
	private int updateOrdAdditionStatusSent(Long orderId,String loginUserId) {
		Map<String, Object> paramsAdditionStatus = new HashMap<String, Object>();
		paramsAdditionStatus.put("orderId", orderId);
		paramsAdditionStatus.put("statusType", OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.getCode());
		paramsAdditionStatus.put("status", OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.SENT.getCode());
		
		List<OrdAdditionStatus> ordAdditionStatusList=ordAdditionStatusDao.selectByParams(paramsAdditionStatus);
		
		int n=0;
		
		if (CollectionUtils.isEmpty(ordAdditionStatusList)) {
			
			paramsAdditionStatus = new HashMap<String, Object>();
			paramsAdditionStatus.put("orderId", orderId);
			paramsAdditionStatus.put("statusType", OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.getCode());
//			paramsAdditionStatus.put("status", OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.SENT.getCode());
			
			ordAdditionStatusList=ordAdditionStatusDao.selectByParams(paramsAdditionStatus);
			OrdAdditionStatus ordAdditionStatusOld=ordAdditionStatusList.get(0);
			
			OrdAdditionStatus ordAdditionStatusNew=new OrdAdditionStatus();
			ordAdditionStatusNew.setOrdAdditionStatusId(ordAdditionStatusOld.getOrdAdditionStatusId());
			ordAdditionStatusNew.setStatus(OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.SENT.getCode());
			
			LOG.info("开始更新出团通知书状态已发送");
			n=ordAdditionStatusDao.updateByPrimaryKeySelective(ordAdditionStatusNew);
			LOG.info("结束更新出团通知书状态");
			
		}

		if (n == 1) {

			orderAuditService.saveCreateOrderAudit(orderId,
					OrderEnum.AUDIT_TYPE.NOTICE_AUDIT.name());

			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					orderId, orderId, loginUserId, "编号为[" + orderId
							+ "]的订单上传出团通知书成功，产生出团通知活动",
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()
							+ "[产生出团通知活动]", null);

		}
		
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
				orderId, 
				orderId, 
				loginUserId, 
				"编号为["+orderId+"]的订单发送出团通知书成功", 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"[发送出团通知书成功]",
				"");
		
		return n;
	}
	
	
	/**
	 * 上传且立即发送
	 * @param fileId
	 * @param fileName
	 * @param memo
	 * @param orderId
	 * @param email
	 */
	public  boolean addUploadAndSendNoticeRegiment(Long fileId, String fileName,String memo, Long orderId, String email,String loginUserId) {
		

		//创建附件表记录
		OrderAttachment orderAttachment = new OrderAttachment();
		orderAttachment.setOrderId(orderId);
		orderAttachment.setAttachmentType(OrderEnum.ATTACHMENT_TYPE.NOTICE_REGIMENT.name());
		orderAttachment.setAttachmentName(fileName);
		orderAttachment.setMemo(memo);
		orderAttachment.setCreateTime(Calendar.getInstance().getTime());
		orderAttachment.setFileId(fileId);
		orderAttachment.setConfirmType(null);//普通附件没有
		orderAttachment.setOrderItemId(null);
		orderAttachment.setFileType(OrderEnum.FILE_TYPE.COMMON.getCode());
		
		orderAttachmentService.saveOrderAttachment(orderAttachment,loginUserId,memo);
		
		this.updateSendNoticeRegiment(orderId, email,loginUserId);
	
		return true;
	}
	
	/**
	 * 出团通知短信发送
	 * @param fileId
	 * @param fileName
	 * @param memo
	 * @param orderId
	 * @param email
	 */
	public  boolean addSMSNoticeRegiment( Long orderId, String smsContent,String mobile,String loginUserId) {
		
		//创建附件表记录
		OrderAttachment orderAttachment = new OrderAttachment();
		orderAttachment.setOrderId(orderId);
		orderAttachment.setAttachmentType(OrderEnum.ATTACHMENT_TYPE.NOTICE_REGIMENT.name());
//		orderAttachment.setAttachmentName(fileName);
//		orderAttachment.setMemo(memo);
		orderAttachment.setCreateTime(Calendar.getInstance().getTime());
//		orderAttachment.setFileId(fileId);
//		orderAttachment.setConfirmType(null);//普通附件没有
//		orderAttachment.setOrderItemId(null);
		orderAttachment.setFileType(OrderEnum.FILE_TYPE.SMS.getCode());
		
		orderAttachment.getContentMap().put(OrderEnum.ORDER_ATTACHMENT_CONTENT.sms.name(), smsContent);
		
		ComLog log = new ComLog();
		log.setParentType(ComLog.COM_LOG_PARENT_TYPE.ORD_ORDER.name());
		log.setParentId(orderAttachment.getOrdAttachmentId());
		log.setObjectType(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER.name());
		log.setObjectId(orderAttachment.getOrderId());
		log.setLogType(ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name());
		log.setLogName(ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName());
		log.setOperatorName(loginUserId);
		log.setContentType(ComLog.COM_LOG_CONTENT_TYPE.VARCHAR.name());
		log.setContent("给编号为["+orderAttachment.getOrderId()+"]的订单出团通知书短信通知");
		log.setCreateTime(Calendar.getInstance().getTime());//当前时间
		log.setMemo("短信内容为["+smsContent+"]");//
		
		orderAttachmentService.saveOrderAttachment(orderAttachment, log);
		
//		orderAttachmentService.saveOrderAttachment(orderAttachment,loginUserId,smsContent);
		
		int n = updateOrdAdditionStatusSent(orderId,loginUserId);
		
		/*
		 * update by xiexun 此处传入业务类型用于标识调用方
		 * this.orderSendSmsService.sendSMS(smsContent, mobile, orderId);*/
		this.orderSendSmsService.sendSMS(smsContent, mobile, "NOTICE", orderId);
		
		
		return true;
	}
	@Override
	public OrdAdditionStatus selectByOrderIdKey(Long orderId) {
		// TODO Auto-generated method stub
		return ordAdditionStatusDao.selectByOrderIdKey(orderId);
	}
	
}
