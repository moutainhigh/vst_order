package com.lvmama.vst.order.contract.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.comm.pet.po.email.EmailAttachment;
import com.lvmama.comm.pet.po.email.EmailContent;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.po.ComFileMap;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.econtract.TravelContractVO;
import com.lvmama.vst.order.contract.service.IOrderElectricContactService;
import com.lvmama.vst.order.contract.vo.OutboundTourContractVO;
import com.lvmama.vst.order.dao.ComFileMapDAO;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdOrderPackDao;
import com.lvmama.vst.order.dao.OrdPersonDao;
import com.lvmama.vst.order.service.IOrdTravelContractService;
import com.lvmama.vst.pet.adapter.VstEmailServiceAdapter;

/**
 * 
 * @author Jesley.Sun
 *
 */

@Service("orderCommissionedServiceAgreementService")
public class OrderCommissionedServiceAgreementServiceImpl extends AbstractOrderTravelElectricContactService implements IOrderElectricContactService {
	
	private static final Log LOG = LogFactory.getLog(OrderCommissionedServiceAgreementServiceImpl.class);
	
	private static final String COMMISSIONED_SERVICE_AGREEMENT = "commissionedServiceAgreement";
	
	@Autowired
	private OrdOrderDao ordOrderDao;
	
	@Autowired
	private ComFileMapDAO comFileMapDAO;
	
	@Autowired
	private OrdPersonDao ordPersonDao;
	
	@Autowired
	private OrdOrderPackDao ordOrderPackDao;
	
	@Autowired
	private IOrdTravelContractService ordTravelContractService;
	
	@Autowired
	private VstEmailServiceAdapter vstEmailServiceAdapter;

	@Override
	public ResultHandle saveTravelContact(OrdTravelContract ordTravelContract, String operatorName) {

		ResultHandle resultHandle = new ResultHandle();
		
		if (ordTravelContract != null) {
			OrdOrder order = ordOrderDao.selectByPrimaryKey(ordTravelContract.getOrderId());
			if (order == null) {
				resultHandle.setMsg("订单ID=" + ordTravelContract.getOrderId() + "不存在。");
				return resultHandle;
			}
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderId", order.getOrderId());
			List<OrdOrderPack> ordOrderPackList = ordOrderPackDao.selectByParams(params);
			order.setOrderPackList(ordOrderPackList);
			Long  distributorId=order.getDistributorId();
			try {
				
				File directioryFile = initDirectory();
				if (directioryFile == null || !directioryFile.exists()) {
					resultHandle.setMsg("合同模板目录不存在。");
					return resultHandle;
				}
				
				List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
				list.add(ordTravelContract);
				order.setOrdTravelContractList(list);

				StringBuilder contractName = new StringBuilder();
				
				if (!findCommissionedServiceAgreement(directioryFile, contractName)) {
					resultHandle.setMsg("目录下不存在委托协议文件。");
					return resultHandle;
				}
				
				ResultHandleT<ComFileMap> handle = saveOrUpdateCommonFile(contractName.toString(), directioryFile);
				if (handle.isFail()) {
					resultHandle.setMsg(handle.getMsg());
					return resultHandle;
				}
				
				ordTravelContract.setFileId(handle.getReturnContent().getFileId());
				
				if (OrderEnum.PAYMENT_STATUS.PAYED.name().equalsIgnoreCase(order.getPaymentStatus())) {
					ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.EFFECT.name());
				} else {
					ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.SIGNED_UNEFFECT.name());
				}
				
				ordTravelContract.setContractName(contractName.toString());

				if (distributorId!=null && (distributorId.longValue()==Constant.DIST_O2O_SELL || distributorId.longValue()==Constant.DIST_O2O_APP_SELL)) {
					ordTravelContract.setSigningType(OrderEnum.ORDER_CONTRACT_SIGNING_TYPE.BRANCHES.name());
					}else {
						ordTravelContract.setSigningType(OrderEnum.ORDER_CONTRACT_SIGNING_TYPE.ONLINE.name());
					}
				
				ordTravelContract.setCreateTime(new Date());
				
				if (ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, operatorName) <= 0) {
					ordTravelContractService.saveOrdTravelContract(ordTravelContract, operatorName);
				}
				
				sendEcontractEmailWithFildId(order);
				
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
				resultHandle.setMsg(e);
			}
		} else {
			
		}
		
		return resultHandle;
	
	}

	@Override
	public ResultHandle sendEcontractWithEmail(OrdTravelContract ordTravelContract) {
		ResultHandle resultHandle = new ResultHandle();
		
		if (ordTravelContract != null) {
			OrdOrder order = ordOrderDao.selectByPrimaryKey(ordTravelContract.getOrderId());
			if (order == null) {
				resultHandle.setMsg("订单ID=" + ordTravelContract.getOrderId() + "不存在。");
				return resultHandle;
			}
			
			if (ordTravelContract.getFileId() == null) {
				resultHandle.setMsg("合同记录ID=" + ordTravelContract.getOrdContractId() + "上传文件不存在。");
				return resultHandle;
			}
			
			ComFileMap comFileMap = comFileMapDAO.getByFileId(ordTravelContract.getFileId());
			if (comFileMap == null || comFileMap.getFileId() == null) {
				resultHandle.setMsg("合同记录ID=" + ordTravelContract.getOrdContractId() + "委托服务协议上传文件不存在。");
				return resultHandle;
			}
			
			List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
			list.add(ordTravelContract);
			order.setOrdTravelContractList(list);
			
			sendEcontractEmailWithFildId(order);
			
		} else {
			resultHandle.setMsg("合同信息不存在。");
		}
			
		return resultHandle;
	}
	
	/**
	 * 
	 * 
	 * @param order
	 * @param comFileMapA
	 * @param comFileMapB
	 * @return
	 */
	private ResultHandle sendEcontractEmailWithFildId(OrdOrder order) {
		ResultHandle resultHandle = new ResultHandle();
		OrdTravelContract ordTravelContract = order.getOrdTravelContract();
		OrdOrderPack ordOrderPack = order.getOrdOrderPack();
		String productName = null;
		if (ordOrderPack == null) {
			if (order.getMainOrderItem() == null) {
				resultHandle.setMsg("无法获取订单ID=" + order.getOrderId() + "的产品名称。");
				return resultHandle;
			}
			productName = order.getMainOrderItem().getProductName();
		} else {
			productName = ordOrderPack.getProductName();
		}
		
		OrdPerson contactPerson = null;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectId", order.getOrderId());
		params.put("objectType", OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
		params.put("personType", OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
		List<OrdPerson> ordPersonList = ordPersonDao.findOrdPersonList(params);
		if (ordPersonList == null || ordPersonList.isEmpty()) {
			resultHandle.setMsg("订单ID=" + order.getOrderId() + "没有联系人。");
			return resultHandle;
		}
		
		for (OrdPerson ordPerson : ordPersonList) {
			if (ordPerson != null && ordPerson.getEmail() != null) {
				contactPerson = ordPerson;
			}
		}
		
		if (contactPerson == null) {
			resultHandle.setMsg("订单ID=" + order.getOrderId() + "联系人邮箱没有填写。");
			return resultHandle;
		}
		
		EmailContent emailContent = new EmailContent();
		List<EmailAttachment> emailAttachmentList = new ArrayList<EmailAttachment>();
		emailContent.setContentText("您好，您在驴妈妈旅游网预订的" + productName + "，电子合同已在附件中，请查收。\n\n祝您旅途愉快。");
		emailContent.setFromAddress(Constant.getInstance().getEcontractEmailAddress());
		emailContent.setFromName("驴妈妈旅游网");
		emailContent.setSubject("订单号：" + order.getOrderId() + "的旅游合同");
		emailContent.setToAddress(contactPerson.getEmail());
		
		
		EmailAttachment emailAttachment = new EmailAttachment();
		emailAttachment.setFileId(ordTravelContract.getFileId());
		emailAttachment.setFileName("委托服务协议_" + ordTravelContract.getVersion() + ".pdf");
		emailAttachmentList.add(emailAttachment);
		
		
		Long mailId = vstEmailServiceAdapter.sendEmailFillAttachment(emailContent, emailAttachmentList);
		
		if (mailId == null || mailId == 0) {
			resultHandle.setMsg("订单ID=" + ordTravelContract.getOrderId() + "邮件系统内部发送失败。");
			LOG.info("OrderCommissionedServiceAgreementServiceImpl.sendEcontractEmailWithFildId:fail,订单ID=" + ordTravelContract.getOrderId() + "邮件系统内部发送失败。");
		} else {
			LOG.info("OrderCommissionedServiceAgreementServiceImpl.sendEcontractEmailWithFildId:success,订单ID=" + ordTravelContract.getOrderId() + ",mailId=" + mailId);
		}
		
		return resultHandle;
	}
	
	private boolean findCommissionedServiceAgreement(File directioryFile, StringBuilder fileNameBuilder) {
		boolean isSucess = false;
		String fileName = null;
		if (directioryFile != null && directioryFile.isDirectory() && directioryFile.exists()) {
			File[] files = directioryFile.listFiles();
			if (files != null) {
				int fileNameNo = 0;
				int fileNo = 0;
				String fileNameTemp = null;
				String fileNameWithoutExtend = null;
				String[] splitedFileNames = null;;
				for (File file : files) {
					if (file != null && file.isFile()) {
						fileNameTemp = file.getName();
						if (fileNameTemp.lastIndexOf(".pdf") == fileNameTemp.length() - 4) {
							fileNameWithoutExtend = fileNameTemp.substring(0, fileNameTemp.length() - 4);
							splitedFileNames = fileNameWithoutExtend.split("_");
							if (splitedFileNames != null && splitedFileNames.length >= 3) {
								if (COMMISSIONED_SERVICE_AGREEMENT.equalsIgnoreCase(splitedFileNames[splitedFileNames.length - 2])
										&& StringUtils.isNumeric(splitedFileNames[splitedFileNames.length - 1])) {
									if (fileName == null) {
										fileName = fileNameTemp;
										fileNameNo = Integer.valueOf(splitedFileNames[splitedFileNames.length - 1]);
									} else {
										fileNo = Integer.valueOf(splitedFileNames[splitedFileNames.length - 1]);
										if (fileNo > fileNameNo) {
											fileName = fileNameTemp;
											fileNameNo = fileNo;
										}
									}
								}
							}
						}
						
					}
				}
			}
		}
		
		if (fileName != null) {
			fileNameBuilder.append(fileName);
			isSucess = true;
		}
		
		return isSucess;
	}
	

	@Override
	public ResultHandle updateTravelContact(OutboundTourContractVO contractVO,
			String operatorName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> captureContract(OrdTravelContract ordTravelContract,OrdOrder order,
			File directioryFile) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ResultHandleT<String> getContractTemplateHtml() {
		return null;
	}

	@Override
	public ResultHandle updateTravelContact(TravelContractVO travelContractVO,
			OrdOrder order, OrdTravelContract ordTravelContract,
			String operatorName) {
		// TODO Auto-generated method stub
		return null;
	}

}
