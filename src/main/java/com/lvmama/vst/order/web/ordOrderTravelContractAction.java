package com.lvmama.vst.order.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvmama.comm.pet.fs.client.FSClient;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.po.ComFileMap;
import com.lvmama.vst.comm.utils.ResourceUtil;
import com.lvmama.vst.comm.utils.pdf.PdfUtil;
import com.lvmama.vst.comm.utils.web.HttpServletLocalThread;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.econtract.TravelContractVO;
import com.lvmama.vst.order.contract.service.IOrderElectricContactService;
import com.lvmama.vst.order.contract.service.impl.AbstractOrderTravelElectricContactService;
import com.lvmama.vst.order.dao.OrdTravelContractDAO;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdTravelContractService;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Controller
public class ordOrderTravelContractAction extends  AbstractOrderTravelElectricContactService{

	/**
	 * @author liuxiuxiu
	 * @since  2016.10.31
	 * 团队境内旅游合同模板
	 */
	private static final long serialVersionUID = 4567936490701439555L;

	private static final Log LOG = LogFactory.getLog(ordOrderTravelContractAction.class);
	
	private static final String templateName = "teamWithInTerritoryContractTemplate.ftl";
	private static final String fileNameA = "supplementary_safety_notice_territory.pdf";
	private static final String contractNameT = "团队境内旅游合同";
	private static final String contractNameC = "委托服务协议";
	
	public static final String TRAVEL_ECONTRACT_DIRECTORY = "/WEB-INF/resources/econtractTemplate";
	
	protected static boolean  isDubgPdf = false;//开发的时候设置为true，上线设置为false
	
	private static final String SERVER_TYPE = "COM_AFFIX";
	
	@Autowired
	private OrdTravelContractDAO ordTravelContractDAO;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Resource(name="teamWithInTerritoryContractService")
	private IOrderElectricContactService travelItineraryContractService;
	
	@Resource(name="commissionedServiceAgreementService")
	private IOrderElectricContactService commissionedServiceAgreementService;
	
	
	@Autowired
	private IOrdTravelContractService ordTravelContractService;
	
	@Autowired
	protected FSClient fsClient;
	
	
	@RequestMapping(value = "/ord/order/findContractTemplateHtmlOrPdf.do")
	public String findContractTemplateHtmlOrPdf(HttpServletRequest request,HttpServletResponse response,Model model) {
		ResultHandle resultHandle = new ResultHandle();
		
		String orderId = request.getParameter("orderId");
		
		String html = request.getParameter("htmlString");
		
		String isPdf = request.getParameter("isPdf");
		
		String isUpdatePdf = request.getParameter("isUpdatePdf");
		
		LOG.info("findContractTemplateHtml==>orderId=" + orderId + "isPdf==>" + isPdf + "isUpdatePdf==>" + isUpdatePdf);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", orderId);
		List<OrdTravelContract> ordTravelContractList = ordTravelContractDAO.selectByParam(params);
		if(ordTravelContractList != null && ordTravelContractList.size() > 0 ){
			for (OrdTravelContract ordTravelContract : ordTravelContractList) {
				LOG.info("ordTravelContract.contractTemplate:"+ordTravelContract.getContractTemplate());
	            if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
	            	LOG.info("orderId:"+orderId + "国内旅游合同");
						OrdOrder order = complexQueryService.queryOrderByOrderId(Long.parseLong(orderId));
		        		if (order == null) {
							model.addAttribute("error", "订单ID=" + orderId + "不存在。");
		        			return "/order/econtractTemplate/conTractTemplateHtml";
		        		}
		        		
		        		List<OrdOrderItem> ordOrderItemList =order.getOrderItemList();
		        		if (ordOrderItemList == null || ordOrderItemList.isEmpty()) {
							model.addAttribute("error", "订单ID=" + order.getOrderId() + "不存在子订单。");
		        			return "/order/econtractTemplate/conTractTemplateHtml";
		        		}
		        		
		        		List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
		    			list.add(ordTravelContract);
		    			order.setOrdTravelContractList(list);
		    			
		    			File directioryFile = initDirectory();
		    			if (directioryFile == null || !directioryFile.exists()) {
							model.addAttribute("error", "合同模板目录不存在。");
		        			return "/order/econtractTemplate/conTractTemplateHtml";
		    			}
		    			
					try {
						Map<String, Object> rootMap = travelItineraryContractService.captureContract(ordTravelContract,order,directioryFile);
						
						TravelContractVO travelContractVO =(TravelContractVO)rootMap.get("travelContractVO");
						
						Configuration configuration = null;
						Template template = null;
						try {
							configuration = initConfiguration(directioryFile);
							if (configuration == null) {
								model.addAttribute("error", "初始化freemarker失败。");
			        			return "/order/econtractTemplate/conTractTemplateHtml";
							}

							template = configuration.getTemplate(templateName.toString());
							if (template == null) {
								model.addAttribute("error", "初始化ftl模板失败。");
			        			return "/order/econtractTemplate/conTractTemplateHtml";
							}
							StringWriter sw = new StringWriter();
							template.process(rootMap, sw);

							String htmlString = sw.toString();

//							System.out.println(htmlString);

							if (htmlString == null) {
								model.addAttribute("error", "合同HTML生成失败。");
			        			return "/order/econtractTemplate/conTractTemplateHtml";
							}
							
							if(htmlString != null && "Y".equals(html)){
								model.addAttribute("htmlString", htmlString);
								return "/order/econtractTemplate/conTractTemplateHtml";
							}
							
							
							
							//生成pdf到本地，不上传
							if(htmlString != null && "Y".equals(isPdf)){
								ByteArrayOutputStream bao = PdfUtil.createPdfFile(htmlString);
								if (bao == null) {
									model.addAttribute("error", "合同PDF生成失败。");
				        			return "/order/econtractTemplate/conTractTemplateHtml";
								}
								
//								FileOutputStream fileStream = new FileOutputStream("C:/teamWithInTerritoryContractTemplate.pdf");
//								fileStream.write(bao.toByteArray());
//								fileStream.close();
								
								//设置文件MIME类型  
						        response.setContentType("application/octet-stream");  
						        //设置Content-Disposition  
						        response.setHeader("Content-Disposition", "attachment;filename=teamWithInTerritoryContractTemplate.pdf");  
								
								 OutputStream out = response.getOutputStream();  
								 out.write(bao.toByteArray());
								 out.close();
								 return null;
							}
							
							//手动上传pdf文件
							if(htmlString != null && "Y".equals(isUpdatePdf)){
								ByteArrayOutputStream bao = PdfUtil.createPdfFile(htmlString);
								if (bao == null) {
									model.addAttribute("error", "合同PDF生成失败。");
				        			return "/order/econtractTemplate/conTractTemplateHtml";
								}
								
								byte[] fileBytes = bao.toByteArray();
								bao.close();
								
								String fileName = "teamWithInTerritoryContractTemplate.pdf";

								ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
								Long fileId = fsClient.uploadFile(fileName, bai, SERVER_TYPE);
								bai.close();
								
								
								if (fileId != null && fileId != 0) {
									ResultHandleT<ComFileMap> handleA = null;
									String operatorName = "Lucy";
									
									handleA = saveOrUpdateCommonFile(fileNameA.toString(), directioryFile);
									if (handleA.isFail()) {
										model.addAttribute("error", "上传附件失败.");
					        			return "/order/econtractTemplate/conTractTemplateHtml";
									}

									ordTravelContract.setVersion(travelContractVO.getContractVersion());
									ordTravelContract.setFileId(fileId);
									
									//合同签约状态逻辑
									setOrdContractStatus(ordTravelContract, order,true);
									
									ordTravelContract.setContractName(contractNameT.toString());
									
									String attachementURLs = fileNameA.toString() ;
									ordTravelContract.setAttachementUrl(attachementURLs);
									ordTravelContract.setCreateTime(new Date());
									if (ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, operatorName) <= 0) {
										ordTravelContractService.saveOrdTravelContract(ordTravelContract, operatorName);
									}
									
									
									//行程单生成
									this.saveTravelItineraryContract(ordTravelContract,operatorName);
									
									String content=contractNameT+"生成成功";
									
									this.insertOrderLog(ordTravelContract.getOrderId(), ordTravelContract.getOrdContractId(), operatorName, content,  null);
									
									model.addAttribute("success", "合同上传成功");
				        			return "/order/econtractTemplate/conTractTemplateHtml";
									
								}else{
									model.addAttribute("error", "国内合同PDF上传失败。");
				        			return "/order/econtractTemplate/conTractTemplateHtml";
								}
								
							}
							

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} catch (TemplateException e) {
						// TODO Auto-generated catch block
						resultHandle.setMsg(e);
					}	
	            	
	            }else if(CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())){
	            	LOG.info("orderId:"+orderId + "委托旅游合同");
	            	
	            	OrdOrder order = complexQueryService.queryOrderByOrderId(Long.parseLong(orderId));
	        		
	        		
	        		List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
	    			list.add(ordTravelContract);
	    			order.setOrdTravelContractList(list);
	    			
	    			File directioryFile = initDirectory();
	    			if (directioryFile == null || !directioryFile.exists()) {
						model.addAttribute("error", "合同模板目录不存在。");
	        			return "/order/econtractTemplate/conTractTemplateHtml";
	    			}
	    			
	    			Map<String, Object> rootMap = commissionedServiceAgreementService.captureContract(ordTravelContract,order,directioryFile);
	    			TravelContractVO travelContractVO =(TravelContractVO)rootMap.get("travelContractVO");
	    			
					Configuration configuration = null;
					Template template = null;
					
					try {
						configuration = initConfiguration(directioryFile);
						
						if (configuration == null) {
							model.addAttribute("error", "初始化freemarker失败。");
		        			return "/order/econtractTemplate/conTractTemplateHtml";
						}

						template = configuration.getTemplate("commissionedServiceAgreementTemplate.ftl");
						if (template == null) {
							model.addAttribute("error", "初始化ftl模板失败。");
		        			return "/order/econtractTemplate/conTractTemplateHtml";
						}
						StringWriter sw = new StringWriter();
						try {
							template.process(rootMap, sw);
						} catch (TemplateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						String htmlString = sw.toString();

//						System.out.println(htmlString);

						if (htmlString == null) {
							model.addAttribute("error", "合同HTML生成失败。");
		        			return "/order/econtractTemplate/conTractTemplateHtml";
						}
						
						if(htmlString != null && "Y".equals(html)){
							model.addAttribute("htmlString", htmlString);
							return "/order/econtractTemplate/conTractTemplateHtml";
						}
						
						//生成pdf到本地，不上传
						if(htmlString != null && "Y".equals(isPdf)){
							ByteArrayOutputStream bao = PdfUtil.createPdfFile(htmlString);
							if (bao == null) {
								model.addAttribute("error", "合同PDF生成失败。");
			        			return "/order/econtractTemplate/conTractTemplateHtml";
							}
							
//							FileOutputStream fileStream = new FileOutputStream("C:/commissionedServiceAgreementTemplate.pdf");
//							fileStream.write(bao.toByteArray());
//							fileStream.close();
							
							//设置文件MIME类型  
					        response.setContentType("application/octet-stream");  
					        //设置Content-Disposition  
					        response.setHeader("Content-Disposition", "attachment;filename=commissionedServiceAgreementTemplate.pdf");  
							
							 OutputStream out = response.getOutputStream();  
							 out.write(bao.toByteArray());
							 out.close();
							 return null;
						}
						
						//手动上传pdf文件
						if(htmlString != null && "Y".equals(isUpdatePdf)){
							ByteArrayOutputStream bao = PdfUtil.createPdfFile(htmlString);
							if (bao == null) {
								model.addAttribute("error", "合同PDF生成失败。");
			        			return "/order/econtractTemplate/conTractTemplateHtml";
							}
							
							byte[] fileBytes = bao.toByteArray();
							bao.close();
							
							String fileName = "commissionedServiceAgreementTemplate.pdf";
							String operatorName = "Lucy";

							boolean isCreateOrder=false;
							if (ordTravelContract.getFileId()==null) {
								isCreateOrder=true;
							}
							
							ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
							Long fileId = fsClient.uploadFile(fileName, bai, SERVER_TYPE);
							bai.close();
							
							
							if (fileId != null && fileId != 0) {
								ordTravelContract.setVersion(travelContractVO.getContractVersion());
								ordTravelContract.setFileId(fileId);
								
								//合同签约状态逻辑
								setOrdContractStatus(ordTravelContract, order,isCreateOrder);
								
								
								ordTravelContract.setContractName(contractNameC.toString());
								ordTravelContract.setCreateTime(new Date());
								
								if (ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, operatorName) <= 0) {
									ordTravelContractService.saveOrdTravelContract(ordTravelContract, operatorName);
								}
								
								String content=contractNameC+"更新成功";
								if (isCreateOrder) {
									content=contractNameC+"生成成功";
								}
								this.insertOrderLog(ordTravelContract.getOrderId(), ordTravelContract.getOrdContractId(), operatorName, content,  null);
								
							}else{
								model.addAttribute("error", "委托合同PDF手动上传失败。");
			        			return "/order/econtractTemplate/conTractTemplateHtml";
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }else{
					model.addAttribute("error", "合同模板不存在，非团队境内旅游合同。");
	    			return "/order/econtractTemplate/conTractTemplateHtml";
	    			
	    			
	    			
	    			
	            }
			}
		}else{
			model.addAttribute("error", "订单ID=" + orderId + ",合同模板不存在。");
			return "/order/econtractTemplate/conTractTemplateHtml";
		}
		return "/order/econtractTemplate/conTractTemplateHtml";
	}
	
	
	
	
	protected File initDirectory() {
		if (isDubgPdf) {   
			 return new File("D:/Ted/workspace/vst_order/src/main/webapp/WEB-INF/resources/econtractTemplate/");
		}
		return ResourceUtil.getResourceFile(TRAVEL_ECONTRACT_DIRECTORY);
	}
	
	protected Configuration initConfiguration(File directioryFile) throws IOException {
		Configuration configuration = null;
		
		if(directioryFile != null && directioryFile.exists()){
			configuration  =new Configuration();
			configuration.setDefaultEncoding("UTF-8");
			configuration.setOutputEncoding("UTF-8");
			configuration.setNumberFormat("###");
			configuration.setClassicCompatible(true);
			configuration.setDirectoryForTemplateLoading(directioryFile);
		}
		
		return configuration;
	}
	
	
	
}
