package com.lvmama.vst.order.contract.service;

import com.lvmama.comm.pet.po.email.EmailAttachment;
import com.lvmama.comm.pet.po.email.EmailContent;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComLog.COM_LOG_LOG_TYPE;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.po.ComFileMap;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.order.dao.ComFileMapDAO;
import com.lvmama.vst.order.service.IOrdTravelContractService;
import com.lvmama.vst.pet.adapter.VstEmailServiceAdapter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 合同邮件服务
 * @Author: LuWei
 * @Date: 2018/6/26 14:27
 */
@Service("orderTravelElectricContactMailService")
public class OrderTravelElectricContactMailService {

	private static final Logger LOG = LoggerFactory.getLogger(OrderTravelElectricContactMailService.class);

	@Autowired
	private ComFileMapDAO comFileMapDAO;

	@Autowired
	private VstEmailServiceAdapter vstEmailServiceAdapter;

	@Autowired
	private LvmmLogClientService lvmmLogClientService;

	@Autowired
	private IOrdTravelContractService ordTravelContractService;

	/**
	 * 发送邮件给客户
	 * @param order
	 * @param ordTravelContractList
	 * @param operator
	 * @return
	 */
	public ResultHandle sendEmail(OrdOrder order,List<OrdTravelContract> ordTravelContractList,String operator){
		ResultHandle resultHandle = new ResultHandle();
		try{
			OrdPerson contactPerson = order.getContactPerson();
			if (contactPerson == null || StringUtils.isEmpty(contactPerson.getEmail())) {
				if(contactPerson == null)
					LOG.info("contactPerson>>is null");
				if(contactPerson!=null &&StringUtils.isEmpty(contactPerson.getEmail()))
					LOG.info("contactPerson>>fullName:"+contactPerson.getFullName());
				resultHandle.setMsg("订单ID=" + order.getOrderId() + "联系人邮箱没有填写。");
				return resultHandle;
			}
//		OrdOrderPack ordOrderPack = order.getOrdOrderPack();

//		 String productName =OrderUtils.getorderProductName(order);

			String productName =order.getProductName();


			EmailContent emailContent = new EmailContent();

			emailContent.setContentText("您好，您在驴妈妈旅游网预订的" + productName + "，电子合同已在附件中，请查收。\n\n祝您旅途愉快。");
			emailContent.setFromAddress(Constant.getInstance().getEcontractEmailAddress());
			emailContent.setFromName("驴妈妈旅游网");
			emailContent.setSubject("订单号：" + order.getOrderId() + "的旅游合同");
			emailContent.setToAddress(contactPerson.getEmail());



			List<EmailAttachment> emailAttachmentList = new ArrayList<EmailAttachment>();
			HashMap<String,ComFileMap> comFileHashMap=new HashMap<String,ComFileMap>();
			Map<Long,String> contractMaps =new HashMap<Long, String>();
			for (OrdTravelContract ordTravelContract : ordTravelContractList) {
				contractMaps.put(ordTravelContract.getOrdContractId(), ordTravelContract.getContractName());
				LOG.info("发送合同id"+ordTravelContract.getOrdContractId());
				EmailAttachment emailAttachment = new EmailAttachment();
				emailAttachment.setFileId(ordTravelContract.getFileId());
				emailAttachment.setFileName("合同_" + ordTravelContract.getVersion() + ".pdf");
				emailAttachmentList.add(emailAttachment);


				String attachementUrl = ordTravelContract.getAttachementUrl();


				if (org.apache.commons.lang3.StringUtils.isNotEmpty(attachementUrl)) {
					String[] attachements = attachementUrl.split(",");

					if (attachements != null && attachements.length >=1) {

						for (int i = 0; i < attachements.length; i++) {
							ComFileMap comFileMap = comFileMapDAO.getByFileName(attachements[0]);
							if (comFileMap != null && comFileMap.getFileId() != null) {
								comFileHashMap.put(comFileMap.getFileName(), comFileMap);
							}
						}

					}
				}



				String additionFileId= ordTravelContract.getAdditionFileId();
				if (org.apache.commons.lang3.StringUtils.isNotEmpty(additionFileId)) {
					String[] additionFileIds = additionFileId.split(",");

					if (additionFileIds != null && additionFileIds.length >=1) {

						for (int i = 0; i < additionFileIds.length; i++) {
							emailAttachment = new EmailAttachment();
							emailAttachment.setFileId(NumberUtils.toLong(additionFileIds[i]));
							emailAttachment.setFileName(ordTravelContract.getVersion()+"_行程单.pdf" );
							emailAttachmentList.add(emailAttachment);
						}
					}
				}

			}


			for(String fileName  : comFileHashMap.keySet()) {

				ComFileMap comFileMap=comFileHashMap.get(fileName);
				EmailAttachment emailAttachment = new EmailAttachment();
				emailAttachment.setFileId(comFileMap.getFileId());
				emailAttachment.setFileName("合同_" + comFileMap.getFileName());
				emailAttachmentList.add(emailAttachment);
			}


			Long mailId = vstEmailServiceAdapter.sendEmailFillAttachment(emailContent, emailAttachmentList);

			if (mailId == null || mailId == 0) {
				resultHandle.setMsg("订单ID=" + order.getOrderId() + "邮件系统内部发送失败。");
				LOG.info("sendEmail sendEcontractEmailWithFildId:fail,订单ID=" + order.getOrderId() + "邮件系统内部发送失败。");
			} else {

				Set<Long> contractIds = contractMaps.keySet();
				for(long id :contractIds){
					insertOrderLog(order.getOrderId(), id, operator, "发送合同至用户邮箱【"+order.getContactPerson().getEmail()+"】，合同名称:"+contractMaps.get(id), "");
				}
				if(contractIds.size()>0){
					ordTravelContractService.updateSendEmailFlag(contractIds);
					LOG.info("合同发送邮件，更新合同邮件标记"+contractIds.toString());
				}
				LOG.info("sendEmail sendEcontractEmailWithFildId:success,订单ID=" + order.getOrderId() + ",mailId=" + mailId);
			}

		}catch(Exception e){
			LOG.error("Error occurred while sending email.", e);
		}
		return resultHandle;
	}
	/**
	 * 发送指定邮箱 hetong@lvmama.com
	 * @param order
	 * @param ordTravelContractList
	 * @param operator
	 * @return
	 */
	public ResultHandle sendEmailToDesignatedAddress(OrdOrder order,List<OrdTravelContract> ordTravelContractList,String operator){
		ResultHandle resultHandle = new ResultHandle();
		try{
			String productName =order.getProductName();

			EmailContent emailContent = new EmailContent();

			emailContent.setContentText("您好，您在驴妈妈旅游网预订的" + productName + "，电子合同已在附件中，请查收。\n\n祝您旅途愉快。");
			emailContent.setFromAddress(Constant.getInstance().getEcontractEmailAddress());
			emailContent.setFromName("驴妈妈旅游网");
			emailContent.setSubject("订单号：" + order.getOrderId() + "的旅游合同");
			emailContent.setToAddress("hetong@lvmama.com");

			List<EmailAttachment> emailAttachmentList = new ArrayList<EmailAttachment>();
			HashMap<String,ComFileMap> comFileHashMap=new HashMap<String,ComFileMap>();
			Map<Long,String> contractMaps =new HashMap<Long, String>();
			for (OrdTravelContract ordTravelContract : ordTravelContractList) {
				contractMaps.put(ordTravelContract.getOrdContractId(), ordTravelContract.getContractName());
				LOG.info("发送合同id"+ordTravelContract.getOrdContractId());
				EmailAttachment emailAttachment = new EmailAttachment();
				emailAttachment.setFileId(ordTravelContract.getFileId());
				emailAttachment.setFileName("合同_" + ordTravelContract.getVersion() + ".pdf");
				emailAttachmentList.add(emailAttachment);

				String attachementUrl = ordTravelContract.getAttachementUrl();

				if (org.apache.commons.lang3.StringUtils.isNotEmpty(attachementUrl)) {
					String[] attachements = attachementUrl.split(",");

					if (attachements != null && attachements.length >=1) {

						for (int i = 0; i < attachements.length; i++) {
							ComFileMap comFileMap = comFileMapDAO.getByFileName(attachements[0]);
							if (comFileMap != null && comFileMap.getFileId() != null) {
								comFileHashMap.put(comFileMap.getFileName(), comFileMap);
							}
						}
					}
				}

				String additionFileId= ordTravelContract.getAdditionFileId();
				if (org.apache.commons.lang3.StringUtils.isNotEmpty(additionFileId)) {
					String[] additionFileIds = additionFileId.split(",");

					if (additionFileIds != null && additionFileIds.length >=1) {

						for (int i = 0; i < additionFileIds.length; i++) {
							emailAttachment = new EmailAttachment();
							emailAttachment.setFileId(NumberUtils.toLong(additionFileIds[i]));
							emailAttachment.setFileName(ordTravelContract.getVersion()+"_行程单.pdf" );
							emailAttachmentList.add(emailAttachment);
						}
					}
				}
			}

			for(String fileName  : comFileHashMap.keySet()) {

				ComFileMap comFileMap=comFileHashMap.get(fileName);
				EmailAttachment emailAttachment = new EmailAttachment();
				emailAttachment.setFileId(comFileMap.getFileId());
				emailAttachment.setFileName("合同_" + comFileMap.getFileName());
				emailAttachmentList.add(emailAttachment);
			}

			Long mailId = vstEmailServiceAdapter.sendEmailFillAttachment(emailContent, emailAttachmentList);

			if (mailId == null || mailId == 0) {
				resultHandle.setMsg("订单ID=" + order.getOrderId() + "邮件系统内部发送失败。");
				LOG.info("sendEmailToDesignatedAddress sendEcontractEmailWithFildId:fail,orderId=" + order.getOrderId() + "邮件系统内部发送失败。");
			} else {

				Set<Long> contractIds = contractMaps.keySet();
				for(long id :contractIds){
					insertOrderLog(order.getOrderId(), id, operator, "发送合同至用户邮箱【hetong@lvmama.com】，合同名称:"+contractMaps.get(id), "");
				}
				if(contractIds.size()>0){
					ordTravelContractService.updateSendEmailFlag(contractIds);
					LOG.info("合同发送邮件，更新合同邮件标记"+contractIds.toString());
				}
				LOG.info("sendEmailToDesignatedAddress sendEcontractEmailWithFildId:success,orderId=" + order.getOrderId() + ",mailId=" + mailId);
			}

		}catch(Exception e){
			LOG.error("sendEmailToDesignatedAddress Error occurred while sending email.", e);
		}
		return resultHandle;
	}

	/**
	 * 合同或者行程单中应有字段丢失，发送预警邮件
	 * @param order
	 * @param emptyField
	 * @return
	 */
	public ResultHandle sendEmailByEmptyField(OrdOrder order,String emptyField){
		LOG.info("==========进入发送预警邮件=============");
		ResultHandle resultHandle = new ResultHandle();
		try{
			EmailContent emailContent = new EmailContent();
			emailContent.setSubject("合同中应有字段丢失！");
			emailContent.setContentText("订单ID=【"+order.getOrderId()+"】,合同中字段【"+emptyField+"】为空！！！");
			emailContent.setSendTime(new Date());
			emailContent.setFromAddress("service@cs.lvmama.com");
			emailContent.setFromName("驴妈妈旅游网");

			String sendAddressList = "";
			//国内游事业部
			if(CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())){
				sendAddressList = Constant.getInstance().getProperty("teamWithInContract");
				emailContent.setToAddress(sendAddressList);
				//出境游事业部
			}else if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equalsIgnoreCase(order.getBuCode())){
				sendAddressList = Constant.getInstance().getProperty("teamOutboundContract");
				emailContent.setToAddress(sendAddressList);
			}

			LOG.info("========发送邮件的地址sendAddressList:"+sendAddressList);
			vstEmailServiceAdapter.sendEmailDirect(emailContent);

			resultHandle.setMsg("订单ID=" + order.getOrderId() + "预警邮件系统内部发送成功。");
			LOG.info("==========sendEmailByEmptyField()方法,订单ID=" + order.getOrderId() + "预警邮件系统内部发送成功。");
		}catch(Exception e){
			LOG.error("Error occurred while sending email.=="+ExceptionUtils.getFullStackTrace(e), e);
			resultHandle.setMsg(ExceptionUtils.getFullStackTrace(e)+"合同中应有字段丢失，发送预警邮件失败！");
		}
		return resultHandle;
	}

	/**
	 *
	 * 保存日志
	 *
	 */
	public void insertOrderLog(final Long orderId, Long contractId,String operatorName,String content, String memo){
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ECONTRACT,
				orderId,
				contractId,
				operatorName,
				content,
				COM_LOG_LOG_TYPE.ORD_ORDER_ECONTRACT_GENERATE.name(),
				COM_LOG_LOG_TYPE.ORD_ORDER_ECONTRACT_GENERATE.getCnName(),
				memo);
	}

	/**
	 *
	 * 保存日志
	 *
	 */
	public void sendEmailLog(final Long orderId, Long contractId,String operatorName,String content, String memo){
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ECONTRACT,
				orderId,
				contractId,
				operatorName,
				content,
				COM_LOG_LOG_TYPE.ORD_ORDER_ECONTRACT_EMAIL.name(),
				COM_LOG_LOG_TYPE.ORD_ORDER_ECONTRACT_EMAIL.getCnName(),
				memo);
	}

}
