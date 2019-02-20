package com.lvmama.vst.order.contract.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.comm.pet.po.email.EmailAttachment;
import com.lvmama.comm.pet.po.email.EmailContent;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PRICE_RATE_TYPE;
import com.lvmama.vst.comm.po.ComFileMap;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.pdf.PdfUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.econtract.TravelContractVO;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.order.contract.service.IOrderElectricContactService;
import com.lvmama.vst.order.contract.service.IOrderTravelContractDataService;
import com.lvmama.vst.order.contract.vo.OutboundTourContractVO;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.dao.OrdOrderPackDao;
import com.lvmama.vst.order.dao.OrdPersonDao;
import com.lvmama.vst.order.dao.OrdTravelContractDAO;
import com.lvmama.vst.order.service.IOrdItemPersonRelationService;
import com.lvmama.vst.order.service.IOrdMulPriceRateService;
import com.lvmama.vst.order.service.IOrdTravelContractService;
import com.lvmama.vst.pet.adapter.VstEmailServiceAdapter;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 
 * @author sunjian
 *
 */
@Service("orderTravelElectricContactService")
public class OrderTravelElectricContactServiceImpl extends AbstractOrderTravelElectricContactService implements IOrderElectricContactService {
	
	private static final Log LOG = LogFactory.getLog(OrderTravelElectricContactServiceImpl.class);
	
	@Autowired
	private OrdOrderDao ordOrderDao;
	
	@Autowired
	private OrdOrderItemDao ordOrderItemDao;
	
	@Autowired
	private OrdOrderPackDao ordOrderPackDao;
	
	@Autowired
	private OrdTravelContractDAO ordTravelContractDAO;
	
	@Autowired
	private OrdPersonDao ordPersonDao;
	
	@Autowired
	private OrderTravelContractDataServiceFactory orderTravelContractDataServiceFactory;
	
	@Autowired
	private IOrdTravelContractService ordTravelContractService;
	
	@Autowired
	private IOrdMulPriceRateService ordMulPriceRateService;
	
	@Resource(name="orderCombCuriseTravelContractDataService")
	private IOrderTravelContractDataService orderCombCuriseTravelContractDataService;
	
	@Autowired
	private VstEmailServiceAdapter vstEmailServiceAdapter;
	
	@Autowired
	private IOrdItemPersonRelationService ordItemPersonRelationService;
	
	private static final String TRAVEL_ECONTRACT_TEMPLATE_SUFFIX = "travelContractTemplate.ftl";
	
	private static final String TRAVEL_ECONTRACT_ADDITION_A = "travelContractA";
	
	private static final String TRAVEL_ECONTRACT_ADDITION_B = "travelContractB";
	
	private static final String SERVER_TYPE = "COM_AFFIX";

	@Override
	public ResultHandle saveTravelContact(OrdTravelContract ordTravelContract, String operatorName) {
		ResultHandle resultHandle = new ResultHandle();
		
		if (ordTravelContract != null) {
			OrdOrder order = ordOrderDao.selectByPrimaryKey(ordTravelContract.getOrderId());
			if (order == null) {
				resultHandle.setMsg("订单ID=" + ordTravelContract.getOrderId() + "不存在。");
				return resultHandle;
			}
			Long distributorId=order.getDistributorId();
			List<OrdOrderItem> ordOrderItemList = ordOrderItemDao.selectByOrderId(order.getOrderId());
			if (ordOrderItemList == null || ordOrderItemList.isEmpty()) {
				resultHandle.setMsg("订单ID=" + order.getOrderId() + "不存在子订单。");
				return resultHandle;
			} else {
				order.setOrderItemList(ordOrderItemList);
			}
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderId", order.getOrderId());
			List<OrdOrderPack> ordOrderPackList = ordOrderPackDao.selectByParams(params);
			order.setOrderPackList(ordOrderPackList);
			
			try {
				IOrderTravelContractDataService orderTravelContractDataService = orderTravelContractDataServiceFactory.createTravelContractDataService(order);
				if (orderTravelContractDataService == null) {
					resultHandle.setMsg("无法抓取合同所需的数据。");
					return resultHandle;
				}
				List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
				list.add(ordTravelContract);
				order.setOrdTravelContractList(list);
				
				File directioryFile = initDirectory();
				if (directioryFile == null || !directioryFile.exists()) {
					resultHandle.setMsg("合同模板目录不存在。");
					return resultHandle;
				}
				Configuration configuration = initConfiguration(directioryFile);
				if (configuration == null) {
					resultHandle.setMsg("初始化freemarker失败。");
					return resultHandle;
				}
				

				StringBuilder contractName = new StringBuilder();
				StringBuilder templateName = new StringBuilder();
				StringBuilder fileNameA =  new StringBuilder();
				StringBuilder fileNameB =  new StringBuilder();
				
				if (!findTravelEcontractTemplate(directioryFile, contractName, templateName)) {
					resultHandle.setMsg("目录下不存在合同模板。");
					return resultHandle;
				}
				
				if (!findTravelEcontractAdditions(directioryFile, fileNameA, fileNameB)) {
					resultHandle.setMsg("目录下不存在合同附件模板。");
					return resultHandle;
				}

				Template template = configuration.getTemplate(templateName.toString());
				if (template == null) {
					resultHandle.setMsg("初始化ftl模板失败。");
					return resultHandle;
				}

				ResultHandleT<OutboundTourContractVO> resultHandleT = orderTravelContractDataService.captureOutboundTourContract(order);
				if (resultHandleT.isFail()) {
					resultHandle.setMsg("抓取邮轮数据失败。");
					return resultHandle;
				}
				

				OutboundTourContractVO contractVO = resultHandleT.getReturnContent();
				contractVO.setPayWay("在线支付给驴妈妈");
				contractVO.setTemplateDirectory("file:///" + directioryFile.getAbsolutePath());
				LOG.info("OrderTravelElectricContactServiceImpl.saveTravelContact,fileDir=" + contractVO.getTemplateDirectory());
				String fileName = "TravelContract_" + contractVO.getContractVersion() + ".pdf";
				
				Map<String,Object> rootMap = new HashMap<String, Object>();
				rootMap.put("contractVO", contractVO);
				StringWriter sw = new StringWriter();
				template.process(rootMap, sw);
				String htmlString = sw.toString();
				if (htmlString == null) {
					resultHandle.setMsg("合同HTML生成失败。");
					return resultHandle;
				}
				
				ByteArrayOutputStream bao = PdfUtil.createPdfFile(htmlString);
				if (bao == null) {
					resultHandle.setMsg("合同PDF生成失败。");
					return resultHandle;
				}
				

				byte[] fileBytes = bao.toByteArray();
				bao.close();
				
				//调试时打开
//				if (true) {
//					FileOutputStream fileOutputStream = new FileOutputStream(new File(directioryFile, fileName));
//					fileOutputStream.write(fileBytes);
//					fileOutputStream.close();
//					
//					FileWriter fileWriter = new FileWriter(new File(directioryFile, fileName + ".html"));
//					fileWriter.write(htmlString);
//					fileWriter.close();
//				}
				
				ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
				Long fileId = fsClient.uploadFile(fileName, bai, SERVER_TYPE);
				bai.close();
				
				if (fileId != null && fileId != 0) {
					ResultHandleT<ComFileMap> handleA = null;
					ResultHandleT<ComFileMap> handleB = null;
					
					handleA = saveOrUpdateCommonFile(fileNameA.toString(), directioryFile);
					if (handleA.isFail()) {
						resultHandle.setMsg(handleA.getMsg());
						return resultHandle;
					}
					
					handleB = saveOrUpdateCommonFile(fileNameB.toString(), directioryFile);
					if (handleB.isFail()) {
						resultHandle.setMsg(handleB.getMsg());
						return resultHandle;
					}
					
					ordTravelContract.setVersion(contractVO.getContractVersion());
					ordTravelContract.setFileId(fileId);
					
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
					
					String attachementURLs = fileNameA.toString() + "," + fileNameB.toString();
					ordTravelContract.setAttachementUrl(attachementURLs);
					ordTravelContract.setCreateTime(new Date());
					
					if (ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, operatorName) <= 0) {
						ordTravelContractService.saveOrdTravelContract(ordTravelContract, operatorName);
					}
					
					sendEcontractEmailWithFildId(order, handleA.getReturnContent(), handleB.getReturnContent());
				} else {
					resultHandle.setMsg("合同上传失败。");
				}
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
				resultHandle.setMsg(e);
			}
		} else {
			
		}
		
		return resultHandle;
	}
	
	
	/**
	 *更新合同 根据OrdOrder生成旅游合同，上船至FTP服务器。
	 * 
	 * @param ordOrder
	 */
	public ResultHandle updateTravelContact(OutboundTourContractVO contractVO, String operatorName){
		
		ResultHandle resultHandle = new ResultHandle();
		
		OrdTravelContract ordTravelContract=new OrdTravelContract();
		Map<String, Object> parametersTravelContract = new HashMap<String, Object>();
		parametersTravelContract.put("orderId",new Long(contractVO.getOrderId()));
		List<OrdTravelContract> ordTravelContractList=ordTravelContractService.findOrdTravelContractList(parametersTravelContract);
		if (!ordTravelContractList.isEmpty()) {
			ordTravelContract=ordTravelContractList.get(0);
		}
		
		
		
		if (ordTravelContract != null) {
			OrdOrder order = ordOrderDao.selectByPrimaryKey(ordTravelContract.getOrderId());
			if (order == null) {
				resultHandle.setMsg("订单ID=" + ordTravelContract.getOrderId() + "不存在。");
				return resultHandle;
			}
			
			List<OrdOrderItem> ordOrderItemList = ordOrderItemDao.selectByOrderId(order.getOrderId());
			if (ordOrderItemList == null || ordOrderItemList.isEmpty()) {
				resultHandle.setMsg("订单ID=" + order.getOrderId() + "不存在子订单。");
				return resultHandle;
			} else {
				order.setOrderItemList(ordOrderItemList);
			}
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderId", order.getOrderId());
			List<OrdOrderPack> ordOrderPackList = ordOrderPackDao.selectByParams(params);
			order.setOrderPackList(ordOrderPackList);
			
			try {
				IOrderTravelContractDataService orderTravelContractDataService = orderTravelContractDataServiceFactory.createTravelContractDataService(order);
				if (orderTravelContractDataService == null) {
					resultHandle.setMsg("无法抓取合同所需的数据。");
					return resultHandle;
				}
				List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
				list.add(ordTravelContract);
				order.setOrdTravelContractList(list);
				
				File directioryFile = initDirectory();
				if (directioryFile == null || !directioryFile.exists()) {
					resultHandle.setMsg("合同模板目录不存在。");
					return resultHandle;
				}
				Configuration configuration = initConfiguration(directioryFile);
				if (configuration == null) {
					resultHandle.setMsg("初始化freemarker失败。");
					return resultHandle;
				}
				

				StringBuilder contractName = new StringBuilder();
				StringBuilder templateName = new StringBuilder();
				StringBuilder fileNameA =  new StringBuilder();
				StringBuilder fileNameB =  new StringBuilder();
				
				if (!findTravelEcontractTemplate(directioryFile, contractName, templateName)) {
					resultHandle.setMsg("目录下不存在合同模板。");
					return resultHandle;
				}
				
				if (!findTravelEcontractAdditions(directioryFile, fileNameA, fileNameB)) {
					resultHandle.setMsg("目录下不存在合同附件模板。");
					return resultHandle;
				}

				Template template = configuration.getTemplate(templateName.toString());
				if (template == null) {
					resultHandle.setMsg("初始化ftl模板失败。");
					return resultHandle;
				}
				
				//订单为邮轮组合
				if(BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(order.getCategoryId())){
					List<OrdOrderItem> itemList = order.getOrderItemList();
					List<OrderMonitorRst> rstList = null;
					if(null != itemList && itemList.size()>0){
						rstList = new ArrayList<OrderMonitorRst>();
						
						for (OrdOrderItem ordOrderItem : itemList) 
						{
							
							OrderMonitorRst orderMonitorRst = new OrderMonitorRst();
							Map<String,Object> contentMap = ordOrderItem.getContentMap();
							String categoryType =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
							orderMonitorRst.setChildOrderTypeName(BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCnName(categoryType));
							String branchName =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());					
							String productName = ordOrderItem.getProductName()+"-"+branchName+"";							
							orderMonitorRst.setProductName(productName);
							orderMonitorRst.setVisitTime(DateUtil.formatSimpleDate(ordOrderItem.getVisitTime()));
							orderMonitorRst.setOrderItemId(ordOrderItem.getOrderItemId());
							orderMonitorRst.setSuppGoodsName(ordOrderItem.getSuppGoodsName());
							orderMonitorRst.setChildOrderType(categoryType);
							orderMonitorRst.setBuyCount(ordOrderItem.getQuantity().intValue());
							buildBuyCountAndPrice(ordOrderItem, orderMonitorRst);
							rstList.add(orderMonitorRst);					
						}
						contractVO.setOrderMonitorRstList(rstList);
					}			
				}
				
				
				contractVO.setTemplateDirectory("file:///" + directioryFile.getAbsolutePath());
				LOG.info("OrderTravelElectricContactServiceImpl.saveTravelContact,fileDir=" + contractVO.getTemplateDirectory());
				
				
				String appendVersion=orderCombCuriseTravelContractDataService.getAppendVersion(ordTravelContract);
				String version = DateUtil.formatDate(order.getVisitTime(), "yyyyMMdd") + "-" + order.getOrderId() + "-" + appendVersion;
				contractVO.setContractVersion(version);
				
				String fileName = "TravelContract_" + contractVO.getContractVersion() + ".pdf";
				
				
				
				
				Map<String,Object> rootMap = new HashMap<String, Object>();
				rootMap.put("contractVO", contractVO);
				StringWriter sw = new StringWriter();
				template.process(rootMap, sw);
				String htmlString = sw.toString();
				if (htmlString == null) {
					resultHandle.setMsg("合同HTML生成失败。");
					return resultHandle;
				}
				
				ByteArrayOutputStream bao = PdfUtil.createPdfFile(htmlString);
				if (bao == null) {
					resultHandle.setMsg("合同PDF生成失败。");
					return resultHandle;
				}
				

				byte[] fileBytes = bao.toByteArray();
				bao.close();
				
				//调试时打开
//				if (true) {
//					FileOutputStream fileOutputStream = new FileOutputStream(new File(directioryFile, fileName));
//					fileOutputStream.write(fileBytes);
//					fileOutputStream.close();
//					
//					FileWriter fileWriter = new FileWriter(new File(directioryFile, fileName + ".html"));
//					fileWriter.write(htmlString);
//					fileWriter.close();
//				}
				
				ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
				Long fileId = fsClient.uploadFile(fileName, bai, SERVER_TYPE);
				bai.close();
				
				if (fileId != null && fileId != 0) {
					ResultHandleT<ComFileMap> handleA = null;
					ResultHandleT<ComFileMap> handleB = null;
					
					handleA = saveOrUpdateCommonFile(fileNameA.toString(), directioryFile);
					if (handleA.isFail()) {
						resultHandle.setMsg(handleA.getMsg());
						return resultHandle;
					}
					
					handleB = saveOrUpdateCommonFile(fileNameB.toString(), directioryFile);
					if (handleB.isFail()) {
						resultHandle.setMsg(handleB.getMsg());
						return resultHandle;
					}
					
					

					
					ordTravelContract.setVersion(version);
					ordTravelContract.setFileId(fileId);
					
					/*if (OrderEnum.PAYMENT_STATUS.PAYED.name().equalsIgnoreCase(order.getPaymentStatus())) {
						ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.EFFECT.name());
					} else {
						ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.SIGNED_UNEFFECT.name());
					}
					*/
					ordTravelContract.setContractName(contractName.toString());
					/*ordTravelContract.setSigningType(OrderEnum.ORDER_CONTRACT_SIGNING_TYPE.ONLINE.name());
					
					String attachementURLs = fileNameA.toString() + "," + fileNameB.toString();
					ordTravelContract.setAttachementUrl(attachementURLs);
					ordTravelContract.setCreateTime(new Date());
					*/

					if (ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, operatorName) <= 0) {
						resultHandle.setMsg("更新合同表数据失败。");
						return resultHandle;
					}
					sendEcontractEmailWithFildId(order, handleA.getReturnContent(), handleB.getReturnContent());
				} else {
					resultHandle.setMsg("合同上传失败。");
				}
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
				resultHandle.setMsg(e);
			}
		} else {
			
		}
		
		return resultHandle;
	}
	/**
	 * 
	 * @param directioryFile
	 * @param name
	 * @param templateName
	 * @return
	 */
	private boolean findTravelEcontractTemplate(File directioryFile, StringBuilder contractName, StringBuilder templateName) {
		boolean isSuccess = false;
				
		if (directioryFile != null && directioryFile.isDirectory() && directioryFile.exists()) {
			File[] files = directioryFile.listFiles();
			if (files != null) {
				String fileName = null;
				String[] splitedFileNames = null;;
				for (File file : files) {
					if (file != null && file.isFile()) {
						fileName = file.getName();
						splitedFileNames = fileName.split("_");
						if (splitedFileNames != null && splitedFileNames.length >= 2) {
							if (TRAVEL_ECONTRACT_TEMPLATE_SUFFIX.equalsIgnoreCase(splitedFileNames[splitedFileNames.length - 1])) {
								contractName.append(fileName.substring(0, fileName.length() - TRAVEL_ECONTRACT_TEMPLATE_SUFFIX.length() - 1));
								templateName.append(fileName);
								isSuccess = true;
								break;
							}
						}
					}
				}
			}
		}
		return isSuccess;
	}
	
	/**
	 * 查找附件最新版本，并拼接成字符串
	 * 
	 * @param directioryFile
	 * @param fileNameA
	 * @param fileNameB
	 * @return
	 */
	private String findTravelEcontractAdditions(File directioryFile) {
		String attachementURLs = null;
		String fileNameA = null;
		String fileNameB = null;
		if (directioryFile != null && directioryFile.isDirectory() && directioryFile.exists()) {
			File[] files = directioryFile.listFiles();
			if (files != null) {
				int fileNameANo = 0;
				int fileNameBNo = 0;
				int fileNo = 0;
				String fileName = null;
				String fileNameWithoutExtend = null;
				String[] splitedFileNames = null;;
				for (File file : files) {
					if (file != null && file.isFile()) {
						fileName = file.getName();
						if (fileName.lastIndexOf(".pdf") == fileName.length() - 4) {
							fileNameWithoutExtend = fileName.substring(0, fileName.length() - 4);
							splitedFileNames = fileNameWithoutExtend.split("_");
							if (splitedFileNames != null && splitedFileNames.length >= 3) {
								if (TRAVEL_ECONTRACT_ADDITION_A.equalsIgnoreCase(splitedFileNames[splitedFileNames.length - 2])
										&& StringUtils.isNumeric(splitedFileNames[splitedFileNames.length - 1])) {
									if (fileNameA == null) {
										fileNameA = fileName;
										fileNameANo = Integer.valueOf(splitedFileNames[splitedFileNames.length - 1]);
									} else {
										fileNo = Integer.valueOf(splitedFileNames[splitedFileNames.length - 1]);
										if (fileNo > fileNameANo) {
											fileNameA = fileName;
											fileNameANo = fileNo;
										}
									}
								} else if (TRAVEL_ECONTRACT_ADDITION_B.equalsIgnoreCase(splitedFileNames[splitedFileNames.length - 2])
										&& StringUtils.isNumeric(splitedFileNames[splitedFileNames.length - 1])) {
									if (fileNameB == null) {
										fileNameB = fileName;
										fileNameBNo = Integer.valueOf(splitedFileNames[splitedFileNames.length - 1]);
									} else {
										fileNo = Integer.valueOf(splitedFileNames[splitedFileNames.length - 1]);
										if (fileNo > fileNameBNo) {
											fileNameB = fileName;
											fileNameBNo = fileNo;
										}
									}
								}
							}
						}
						
					}
				}
			}
		}
		
		if (fileNameA != null) {
			attachementURLs = fileNameA;
		}
		if (fileNameB != null) {
			if (attachementURLs == null) {
				attachementURLs = fileNameB;
			} else {
				attachementURLs = attachementURLs + "," + fileNameB;
			}
		}
		
		return attachementURLs;
	}
	
	/**
	 * 查找附件最新版本
	 * 
	 * @param directioryFile
	 * @param fileNameBuilderA
	 * @param fileNameBuilderB
	 * @return
	 */
	private boolean findTravelEcontractAdditions(File directioryFile, StringBuilder fileNameBuilderA, StringBuilder fileNameBuilderB) {
		boolean isSucess = false;
		String fileNameA = null;
		String fileNameB = null;
		if (directioryFile != null && directioryFile.isDirectory() && directioryFile.exists()) {
			File[] files = directioryFile.listFiles();
			if (files != null) {
				int fileNameANo = 0;
				int fileNameBNo = 0;
				int fileNo = 0;
				String fileName = null;
				String fileNameWithoutExtend = null;
				String[] splitedFileNames = null;;
				for (File file : files) {
					if (file != null && file.isFile()) {
						fileName = file.getName();
						if (fileName.lastIndexOf(".pdf") == fileName.length() - 4) {
							fileNameWithoutExtend = fileName.substring(0, fileName.length() - 4);
							splitedFileNames = fileNameWithoutExtend.split("_");
							if (splitedFileNames != null && splitedFileNames.length >= 3) {
								if (TRAVEL_ECONTRACT_ADDITION_A.equalsIgnoreCase(splitedFileNames[splitedFileNames.length - 2])
										&& StringUtils.isNumeric(splitedFileNames[splitedFileNames.length - 1])) {
									if (fileNameA == null) {
										fileNameA = fileName;
										fileNameANo = Integer.valueOf(splitedFileNames[splitedFileNames.length - 1]);
									} else {
										fileNo = Integer.valueOf(splitedFileNames[splitedFileNames.length - 1]);
										if (fileNo > fileNameANo) {
											fileNameA = fileName;
											fileNameANo = fileNo;
										}
									}
								} else if (TRAVEL_ECONTRACT_ADDITION_B.equalsIgnoreCase(splitedFileNames[splitedFileNames.length - 2])
										&& StringUtils.isNumeric(splitedFileNames[splitedFileNames.length - 1])) {
									if (fileNameB == null) {
										fileNameB = fileName;
										fileNameBNo = Integer.valueOf(splitedFileNames[splitedFileNames.length - 1]);
									} else {
										fileNo = Integer.valueOf(splitedFileNames[splitedFileNames.length - 1]);
										if (fileNo > fileNameBNo) {
											fileNameB = fileName;
											fileNameBNo = fileNo;
										}
									}
								}
							}
						}
						
					}
				}
			}
		}
		
		if (fileNameA != null) {
			fileNameBuilderA.append(fileNameA);
		}
		if (fileNameB != null) {
			fileNameBuilderB.append(fileNameB);
		}
		
		if (fileNameA != null && fileNameB != null) {
			isSucess = true;
		}
		
		return isSucess;
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
			
			List<OrdOrderItem> ordOrderItemList = ordOrderItemDao.selectByOrderId(order.getOrderId());
			if (ordOrderItemList == null || ordOrderItemList.isEmpty()) {
				resultHandle.setMsg("订单ID=" + order.getOrderId() + "不存在子订单。");
				return resultHandle;
			} else {
				order.setOrderItemList(ordOrderItemList);
			}
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderId", order.getOrderId());
			List<OrdOrderPack> ordOrderPackList = ordOrderPackDao.selectByParams(params);
			order.setOrderPackList(ordOrderPackList);
			
			if (ordTravelContract.getFileId() == null) {
				resultHandle.setMsg("合同记录ID=" + ordTravelContract.getOrdContractId() + "上传文件不存在。");
				return resultHandle;
			}
			
			String attachementUrl = ordTravelContract.getAttachementUrl();
			if (attachementUrl == null || attachementUrl.trim().isEmpty()) {
				resultHandle.setMsg("合同记录ID=" + ordTravelContract.getOrdContractId() + "附件文件不存在。");
				return resultHandle;
			}
			
			String[] attachements = attachementUrl.split(",");
			if (attachements == null || attachements.length != 2) {
				resultHandle.setMsg("合同记录ID=" + ordTravelContract.getOrdContractId() + "附件URL解析错误。");
				return resultHandle;
			}
			
			ComFileMap comFileMapA = comFileMapDAO.getByFileName(attachements[0]);
			if (comFileMapA == null || comFileMapA.getFileId() == null) {
				resultHandle.setMsg("合同记录ID=" + ordTravelContract.getOrdContractId() + "附件A上传文件不存在。");
				return resultHandle;
			}
			
			ComFileMap comFileMapB = comFileMapDAO.getByFileName(attachements[0]);
			if (comFileMapB == null || comFileMapB.getFileId() == null) {
				resultHandle.setMsg("合同记录ID=" + ordTravelContract.getOrdContractId() + "附件A上传文件不存在。");
				return resultHandle;
			}
			
			List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
			list.add(ordTravelContract);
			order.setOrdTravelContractList(list);
			
			sendEcontractEmailWithFildId(order, comFileMapA, comFileMapB);
			
		} else {
			resultHandle.setMsg("合同信息不存在。");
		}
			
		return resultHandle;
	}
	
	
//	/**
//	 * 
//	 * @param ordTravelContract
//	 * @param comFileMapA
//	 * @param comFileMapB
//	 */
//	private ResultHandle sendEcontractEmailWithFildId(OrdTravelContract ordTravelContract, ComFileMap comFileMapA, ComFileMap comFileMapB) {
//		ResultHandle resultHandle = new ResultHandle();
//		OrdPerson contactPerson = null;
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("objectId", ordTravelContract.getOrderId());
//		params.put("objectType", OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
//		params.put("personType", OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
//		List<OrdPerson> ordPersonList = ordPersonDao.findOrdPersonList(params);
//		if (ordPersonList == null || ordPersonList.isEmpty()) {
//			resultHandle.setMsg("订单ID=" + ordTravelContract.getOrderId() + "没有联系人。");
//			return resultHandle;
//		}
//		
//		for (OrdPerson ordPerson : ordPersonList) {
//			if (ordPerson != null && ordPerson.getEmail() != null) {
//				contactPerson = ordPerson;
//			}
//		}
//		
//		if (contactPerson == null) {
//			resultHandle.setMsg("订单ID=" + ordTravelContract.getOrderId() + "联系人邮箱没有填写。");
//			return resultHandle;
//		}
//		
//		EmailContent emailContent = new EmailContent();
//		List<EmailAttachment> emailAttachmentList = new ArrayList<EmailAttachment>();
//		emailContent.setContentText("组合邮轮电子合同测试。");
//		emailContent.setFromAddress("webmaster@lvmama.org");
//		emailContent.setFromName("驴妈妈旅游网");
//		emailContent.setSubject("旅游电子合同");
//		emailContent.setToAddress("andy_suen@sina.com");
////		emailContent.setToAddress(contactPerson.getEmail());
//		
//		
//		EmailAttachment emailAttachment = new EmailAttachment();
//		emailAttachment.setFileId(ordTravelContract.getFileId());
//		emailAttachment.setFileName("合同_" + ordTravelContract.getVersion());
//		
//		emailAttachment = new EmailAttachment();
//		emailAttachment.setFileId(comFileMapA.getFileId());
//		emailAttachment.setFileName("合同_" + comFileMapA.getFileName());
//		
//		emailAttachment = new EmailAttachment();
//		emailAttachment.setFileId(comFileMapB.getFileId());
//		emailAttachment.setFileName("合同_" + comFileMapB.getFileName());
//		
//		Long mailId = vstEmailServiceAdapter.sendEmailFillAttachment(emailContent, emailAttachmentList);
//		
//		if (mailId == null || mailId == 0) {
//			resultHandle.setMsg("订单ID=" + ordTravelContract.getOrderId() + "邮件系统内部发送失败。");
//		}
//		
//		return resultHandle;
//	}
	
	private ResultHandle sendEcontractEmailWithFildId(OrdOrder order, ComFileMap comFileMapA, ComFileMap comFileMapB) {
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
		emailAttachment.setFileName("合同_" + ordTravelContract.getVersion() + ".pdf");
		emailAttachmentList.add(emailAttachment);
		
		emailAttachment = new EmailAttachment();
		emailAttachment.setFileId(comFileMapA.getFileId());
		emailAttachment.setFileName("合同_" + comFileMapA.getFileName());
		emailAttachmentList.add(emailAttachment);
		
		emailAttachment = new EmailAttachment();
		emailAttachment.setFileId(comFileMapB.getFileId());
		emailAttachment.setFileName("合同_" + comFileMapB.getFileName());
		emailAttachmentList.add(emailAttachment);
		
		Long mailId = vstEmailServiceAdapter.sendEmailFillAttachment(emailContent, emailAttachmentList);
		
		if (mailId == null || mailId == 0) {
			resultHandle.setMsg("订单ID=" + ordTravelContract.getOrderId() + "邮件系统内部发送失败。");
			LOG.info("OrderTravelElectricContactServiceImpl.sendEcontractEmailWithFildId:fail,订单ID=" + ordTravelContract.getOrderId() + "邮件系统内部发送失败。");
		} else {
			LOG.info("OrderTravelElectricContactServiceImpl.sendEcontractEmailWithFildId:success,订单ID=" + ordTravelContract.getOrderId() + ",mailId=" + mailId);
		}
		
		return resultHandle;
	}


	@Override
	public Map<String, Object> captureContract(OrdTravelContract ordTravelContract,OrdOrder order,
			File directioryFile) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ResultHandleT<String> getContractTemplateHtml() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ResultHandle updateTravelContact(TravelContractVO travelContractVO,
			OrdOrder order, OrdTravelContract ordTravelContract,
			String operatorName) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * 构建子订单购买商品数量、销售单价与总价
	 * @param orderItem
	 * @param orderMonitorRst
	 */
private void buildBuyCountAndPrice(OrdOrderItem orderItem, OrderMonitorRst orderMonitorRst) {
	   orderMonitorRst.setBuyItemCount(orderItem.getQuantity()+"份");
		Map<String, Object> paramOrdItemPersonRelation = new HashMap<String, Object>();
		paramOrdItemPersonRelation.put("orderItemId", orderItem.getOrderItemId()); 
		List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(paramOrdItemPersonRelation);
		orderMonitorRst.setPersonCount(ordItemPersonRelationList.size());
}
}
