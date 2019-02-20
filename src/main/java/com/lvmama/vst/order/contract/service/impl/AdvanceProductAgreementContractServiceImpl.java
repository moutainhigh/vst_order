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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.prod.curise.service.ProdCuriseProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.pdf.PdfUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.econtract.TravelContractVO;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.order.contract.service.IOrderElectricContactService;
import com.lvmama.vst.order.contract.vo.OutboundTourContractVO;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdItemContractRelationService;
import com.lvmama.vst.order.service.IOrdItemPersonRelationService;
import com.lvmama.vst.order.service.IOrdMulPriceRateService;
import com.lvmama.vst.order.service.IOrdOrderPackService;
import com.lvmama.vst.order.service.IOrdPersonService;
import com.lvmama.vst.order.service.IOrdTravelContractService;
import com.lvmama.vst.order.service.IOrderUpdateService;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 
 * @author zhangwei
 *
 */
@Service("advanceProductAgreementContractService")
public class AdvanceProductAgreementContractServiceImpl extends AbstractOrderTravelElectricContactService implements IOrderElectricContactService {
	
	private static final Log LOG = LogFactory.getLog(AdvanceProductAgreementContractServiceImpl.class);

	@Autowired
	private IOrdTravelContractService ordTravelContractService;
	
	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private ProdCuriseProductClientService prodCuriseProductClientService;
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private SuppSupplierClientService suppSupplierClientService;
	
	@Autowired
	private IOrdMulPriceRateService ordMulPriceRateService;

	@Autowired
	private CategoryClientService categoryClientService;

	@Autowired
	private IOrdItemContractRelationService ordItemContractRelationService;
	

	@Autowired
	private IOrdOrderPackService ordOrderPackService;
	
	@Autowired
	private IOrderUpdateService ordOrderUpdateService;
	
	@Autowired
	private IOrdPersonService ordPersonService;
	
	@Autowired
	private IOrdItemPersonRelationService ordItemPersonRelationService;
	
	private static final String SERVER_TYPE = "COM_AFFIX";
	
	private static final String contractName = "预付款产品协议书";
	private static final String templateName = "advanceProductAgreementTemplate.ftl";

	@Override
	public ResultHandle saveTravelContact(OrdTravelContract ordTravelContract, String operatorName) {
		ResultHandle resultHandle = new ResultHandle();
		
		if (ordTravelContract != null) {
			OrdOrder order = complexQueryService.queryOrderByOrderId(ordTravelContract.getOrderId());
			if (order == null) {
				resultHandle.setMsg("订单ID=" + ordTravelContract.getOrderId() + "不存在。");
				return resultHandle;
			}
			
			List<OrdOrderItem> ordOrderItemList =order.getOrderItemList();
			if (ordOrderItemList == null || ordOrderItemList.isEmpty()) {
				resultHandle.setMsg("订单ID=" + order.getOrderId() + "不存在子订单。");
				return resultHandle;
			}
			
			
			try {
				
				List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
				list.add(ordTravelContract);
				order.setOrdTravelContractList(list);
				
				File directioryFile = initDirectory();
				if (directioryFile == null || !directioryFile.exists()) {
					resultHandle.setMsg("合同模板目录不存在。");
					return resultHandle;
				}
				
				Map<String,Object> rootMap=this.captureContract(ordTravelContract,order,directioryFile);
				
				TravelContractVO travelContractVO =(TravelContractVO)rootMap.get("travelContractVO");

				
				//生成合同中应有字段为空时，发送预警邮件(委托服务协议，预付款)
				resultHandle = checkSaveAgreementData(travelContractVO,order);
				if(resultHandle.isFail()){
					LOG.info("---------------合同中应有字段为空时，发送预警邮件(委托服务协议，预付款)-------------");
					return resultHandle;
				}
				//end
				
				Configuration configuration = initConfiguration(directioryFile);
				if (configuration == null) {
					resultHandle.setMsg("初始化freemarker失败。");
					return resultHandle;
				}

				Template template = configuration.getTemplate(templateName.toString());
				if (template == null) {
					resultHandle.setMsg("初始化ftl模板失败。");
					return resultHandle;
				}

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
				
				
				String fileName = null;
				
				if(StringUtils.isNotEmpty(travelContractVO.getContractVersion())){
					fileName = "advanceProductAgreementTemplate_" + travelContractVO.getContractVersion() + ".pdf";
				}else{
					fileName = "advanceProductAgreementTemplate_emptyTemplate.pdf";
				}
				
				//调试时打开
				this.newContractDebug(fileBytes, fileName);
				
				
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
					
					//合同状态
					if (OrderEnum.PAYMENT_STATUS.PAYED.name().equalsIgnoreCase(order.getPaymentStatus())) {
						ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.EFFECT.name());
					}else if (OrderEnum.ORDER_STATUS.CANCEL.getCode().equalsIgnoreCase(order.getOrderStatus())) {
						ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.CANCEL.name());
					} else {
						ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.SIGNED_UNEFFECT.name());
					}
					
					
					ordTravelContract.setContractName(contractName.toString());
//					ordTravelContract.setSigningType(OrderEnum.ORDER_CONTRACT_SIGNING_TYPE.ONLINE.name());
					ordTravelContract.setCreateTime(new Date());
					
					if (ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, operatorName) <= 0) {
						ordTravelContractService.saveOrdTravelContract(ordTravelContract, operatorName);
					}
					
				} else {
					resultHandle.setMsg("合同上传失败。");
				}
				
				String content=contractName+"更新成功";
				if (isCreateOrder) {
					content=contractName+"生成成功";
				}
				this.insertOrderLog(ordTravelContract.getOrderId(), ordTravelContract.getOrdContractId(), operatorName, content,  null);
				
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
	 * @param ordTravelContract
	 */
	public ResultHandle updateTravelContact(OrdTravelContract ordTravelContract, TravelContractVO contractVO, String operatorName){
		ResultHandle resultHandle = new ResultHandle();
		
		if (ordTravelContract != null) {
			OrdOrder order = complexQueryService.queryOrderByOrderId(ordTravelContract.getOrderId());
			if (order == null) {
				resultHandle.setMsg("订单ID=" + ordTravelContract.getOrderId() + "不存在。");
				return resultHandle;
			}
			
			List<OrdOrderItem> ordOrderItemList =order.getOrderItemList();
			if (ordOrderItemList == null || ordOrderItemList.isEmpty()) {
				resultHandle.setMsg("订单ID=" + order.getOrderId() + "不存在子订单。");
				return resultHandle;
			}
			
			
			try {
				
				List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
				list.add(ordTravelContract);
				order.setOrdTravelContractList(list);
				
				File directioryFile = initDirectory();
				if (directioryFile == null || !directioryFile.exists()) {
					resultHandle.setMsg("合同模板目录不存在。");
					return resultHandle;
				}
				
				Map<String,Object> rootMap=this.captureContract(ordTravelContract,order,directioryFile);
				
				TravelContractVO travelContractVO =(TravelContractVO)rootMap.get("travelContractVO");
				
				travelContractVO.setTravellers(contractVO.getTravellers());//修改委托方
				if(order.getContactPerson() != null){
					order.getContactPerson().setMobile(contractVO.getContractMobile());//修改委托方联系电话
				}
				travelContractVO.setFilialeName(contractVO.getFilialeName());//修改受托方
				travelContractVO.setLvMobile(contractVO.getLvMobile());//修改受托方联系电话
				if(travelContractVO.getFirstTravellerPerson() != null){//甲方代表
					travelContractVO.getFirstTravellerPerson().setFullName(contractVO.getFirstDelegatePersonName());
				}
				travelContractVO.setSingnDate(contractVO.getSingnDate());//甲方代表日期
				travelContractVO.setLvSingnDate(contractVO.getLvSingnDate());//甲方代表日期
				if(StringUtil.isNotEmptyString(contractVO.getTraveAmount())){//人民币
					boolean isNum = contractVO.getTraveAmount().matches("-?[0-9]+.*[0-9]*");
					if(isNum){
						travelContractVO.setTraveAmount(contractVO.getTraveAmount());
					}
				}
				travelContractVO.setCopies(contractVO.getCopies());//工作日
				
				//生成合同中应有字段为空时，发送预警邮件(委托服务协议，预付款)
				resultHandle = checkUpdateAgreementData(travelContractVO,order);
				if(resultHandle.isFail()){
					LOG.info("---------------合同中应有字段为空时，发送预警邮件(委托服务协议，预付款)-------------");
					return resultHandle;
				}
				//end
				
				Configuration configuration = initConfiguration(directioryFile);
				if (configuration == null) {
					resultHandle.setMsg("初始化freemarker失败。");
					return resultHandle;
				}

				Template template = configuration.getTemplate(templateName.toString());
				if (template == null) {
					resultHandle.setMsg("初始化ftl模板失败。");
					return resultHandle;
				}

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
				
				
				String fileName = "advanceProductAgreementTemplate_" + travelContractVO.getContractVersion() + ".pdf";
				
				//调试时打开
				this.newContractDebug(fileBytes, fileName);
				
				
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
					
					//合同状态
					if (OrderEnum.PAYMENT_STATUS.PAYED.name().equalsIgnoreCase(order.getPaymentStatus())) {
						ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.EFFECT.name());
					}else if (OrderEnum.ORDER_STATUS.CANCEL.getCode().equalsIgnoreCase(order.getOrderStatus())) {
						ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.CANCEL.name());
					} else {
						ordTravelContract.setStatus(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.SIGNED_UNEFFECT.name());
					}
					
					
					ordTravelContract.setContractName(contractName.toString());
//					ordTravelContract.setSigningType(OrderEnum.ORDER_CONTRACT_SIGNING_TYPE.ONLINE.name());
					ordTravelContract.setCreateTime(new Date());
					
					if (ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, operatorName) <= 0) {
						ordTravelContractService.saveOrdTravelContract(ordTravelContract, operatorName);
					}
					
				} else {
					resultHandle.setMsg("合同上传失败。");
				}
				
				String content=contractName+"更新成功";
				if (isCreateOrder) {
					content=contractName+"生成成功";
				}
				this.insertOrderLog(ordTravelContract.getOrderId(), ordTravelContract.getOrdContractId(), operatorName, content,  null);
				
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
		
		return null;
	}
	


	
	
	

	
	public Map<String,Object> captureContract(OrdTravelContract ordTravelContract,OrdOrder order,File directioryFile) {
		Map<String,Object> rootMap = new HashMap<String, Object>();
		
		if (order != null) {
			TravelContractVO travelContractVO = buildTravelContractVOData(ordTravelContract,order);
			//关联销售当地游，替换相关合同内容信息
			replaceTravelContractVOData(ordTravelContract,order,travelContractVO);
			Map<String,List<OrderMonitorRst>> chidOrderMap = findChildOrderList(ordTravelContract,order,false);//生成订购清单
			
			travelContractVO.setTemplateDirectory("file:///" + directioryFile.getAbsolutePath());//图片单选按钮
			LOG.info("advanceProductAgreementContractServiceImpl.saveTravelContact,fileDir=" + travelContractVO.getTemplateDirectory());
			
			rootMap.put("travelContractVO", travelContractVO);
			rootMap.put("order", order);
			rootMap.put("chidOrderMap", chidOrderMap);
		}
		
		
		
		
		return rootMap;
	}


	

	private void replaceTravelContractVOData(OrdTravelContract ordTravelContract, OrdOrder order,
			TravelContractVO travelContractVO) {


		HashMap<String, Object> mapProduct = this.getProductIdAndName(ordTravelContract, order);

		OrdOrderItem orderContractItem = (OrdOrderItem)mapProduct.get("orderContractItem");
		
		boolean relatedMarketingFlag = isExsitLocalRouteItemInOrder(order);//判断订单是否有"关联销售当地游"订单
		
		boolean orderItemLocalRouteFlag = isLocalRouteOrderItem(orderContractItem);//该子订单是否是关联当地游
		
		if(relatedMarketingFlag){//该订单选择了关联销售当地游产品
			
			if(orderItemLocalRouteFlag){//该子订单是关联当地游子订单
				//合同编号
				String appendVersion = getAppendVersion(ordTravelContract);
				String version = DateUtil.formatDate(orderContractItem.getVisitTime(), "yyyyMMdd") + "-" + orderContractItem.getOrderItemId() + "-" + appendVersion;
				travelContractVO.setContractVersion(version);
				
				
				//订单编号
				travelContractVO.setOrderId(orderContractItem.getOrderItemId().toString());
				
				
				//出发日期
				order.setVisitTime(orderContractItem.getVisitTime());
				travelContractVO.setVistDate(DateUtil.formatDate(orderContractItem.getVisitTime(), "yyyy-MM-dd"));
				
				//甲方
				String travellers = "";
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("orderItemId", orderContractItem.getOrderItemId()); //当前子订单Id
				
				List<OrdItemPersonRelation> ordItemPersonRelationList = ordItemPersonRelationService.findOrdItemPersonRelationList(params);
				
				List<OrdPerson> OrdPersonList = new ArrayList<OrdPerson>();
				
				for (int i = 0; i < ordItemPersonRelationList.size(); i++) {
					OrdItemPersonRelation ordItemPersonRelation = ordItemPersonRelationList.get(i);
					OrdPerson ordPerson=  ordPersonService.findOrdPersonById(ordItemPersonRelation.getOrdPersonId());
					OrdPersonList.add(ordPerson);
				}
				
				for(int i=0;i<OrdPersonList.size();i++){
					if(i>0 && i%5==0 && OrdPersonList.size()>5) {
						travellers+="<br />";
					}
					if (i < OrdPersonList.size()) {
						travellers += OrdPersonList.get(i).getFullName()+ ",";
					}else {
						travellers += OrdPersonList.get(i).getFullName();
					}

				}
				travellers = travellers.substring(0,travellers.length()-1);
				travelContractVO.setTravellers(travellers);
				
				//订单支付金额
				travelContractVO.setTraveAmount(orderContractItem.getTotalPriceYuan());
				order.setOughtAmount(orderContractItem.getTotalAmount());
				
				//日期
				travelContractVO.setFirstSignatrueDate(DateUtil.formatDate(orderContractItem.getVisitTime(), "yyyy-MM-dd"));
				travelContractVO.setSecondSignatrueDate(DateUtil.formatDate(orderContractItem.getVisitTime(), "yyyy-MM-dd"));

			}else{
				//订单支付金额：结算总价-当地游产品销售价
				Long totalLocalRoutePrices = 0L;
				List<OrdOrderItem> orderItemList = order.getOrderItemList();
				for(OrdOrderItem item : orderItemList){
					boolean flag = isLocalRouteOrderItem(item);//该子订单是否是关联当地游
					if(flag){
						totalLocalRoutePrices += item.getTotalAmount();
					}
				}
				travelContractVO.setTraveAmount(PriceUtil.trans2YuanStr(order.getOughtAmount()-totalLocalRoutePrices));
				
				order.setOughtAmount(order.getOughtAmount()-totalLocalRoutePrices);
			}
		}

	
		
	}




	/**
	 * 组装合同展示数据
	 * @param order
	 * @param ordTravelContract
	 * @return
	 */
	private TravelContractVO buildTravelContractVOData(OrdTravelContract ordTravelContract,OrdOrder order) {
		
		List<OrdOrderPack>  ordPackList=order.getOrderPackList();
		
		HashMap<String, Object> mapProduct=this.getProductIdAndName(ordTravelContract, order);
		Long productId=(Long)mapProduct.get("productId");
		String productName=(String)mapProduct.get("productName");
		OrdOrderItem orderContractItem=(OrdOrderItem)mapProduct.get("orderContractItem");
		
		
		TravelContractVO travelContractVO = null;
		
		
		ProdProductParam param = new ProdProductParam();
		param.setProductProp(true);
		param.setProductBranchValue(true);
		param.setProdEcontract(true);

		ResultHandleT<ProdProduct> resultHandle=this.prodProductClientService.findLineProductByProductId(productId, param);
		
		ProdProduct prodProduct=resultHandle.getReturnContent();
		
		
		SuppSupplier suppSupplier = new SuppSupplier();
		if (order != null && prodProduct != null) {
			travelContractVO = new TravelContractVO();
			travelContractVO.setProdProduct(prodProduct);
			travelContractVO.setProductId(productId);
			String appendVersion = getAppendVersion(ordTravelContract);
			//合同编号
			String version = DateUtil.formatDate(order.getVisitTime(), "yyyyMMdd") + "-" + order.getOrderId() + "-" + appendVersion;
			travelContractVO.setContractVersion(version);
			
			//订单编号
			travelContractVO.setOrderId(order.getOrderId().toString());
			travelContractVO.setTraveAmount(PriceUtil.trans2YuanStr(order.getOughtAmount()));
			//甲方
			String travellers = null;
			int travellerCount = 0;
			for (OrdPerson ordPerson : order.getOrdPersonList()) {
				if (ordPerson != null && OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equalsIgnoreCase(ordPerson.getPersonType())) {
					if (travellers == null) {
						travellers = ordPerson.getFullName();
					} else {
						if (travellerCount % 5 == 0) {
							travellers = travellers + ",<br />" + ordPerson.getFullName();
						} else {
							travellers = travellers + "," + ordPerson.getFullName();
						}
						
					}
					
					travellerCount++;
				}
			}
			
			travelContractVO.setTravellers(travellers);
			
			//出境社
			travelContractVO.setFilialeName(this.filialeNameMap.get(order.getFilialeName()));
			
			//监督电话
			travelContractVO.setJianduTel(this.jianduTelMap.get(order.getFilialeName()));
			
			//营业地址
			travelContractVO.setAddress(this.businessAddressMap.get(order.getFilialeName()));
			//邮编
			travelContractVO.setLvPostcode(this.businessPostCodeMap.get(order.getFilialeName()));
			
			//产品名称
			travelContractVO.setProductName(productName);
			
			//出发日期
			travelContractVO.setVistDate(DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd"));
			//出发地点
			//travelContractVO.setDeparturePlace();
			
			//共几天  饭店住宿几夜
			Integer routeNights =0;
			Integer routeDays =0;
			Map<String,Object> map=null;
			if (CollectionUtils.isNotEmpty(ordPackList)) {
				OrdOrderPack ordOrderPack=ordPackList.get(0);
				map = ordOrderPack.getContentMap();
			}else{
				map =orderContractItem.getContentMap();
			}
			routeDays =  (Integer) map.get(OrderEnum.ORDER_PACK_TYPE.route_days.name());
			routeNights =  (Integer) map.get(OrderEnum.ORDER_PACK_TYPE.route_nights.name());
			if (routeDays!=null) {
				travelContractVO.setRouteDays(routeDays+"");
			}
			if (routeNights!=null) {
				travelContractVO.setRouteNights(routeNights+"");
			}
			

			travelContractVO.setPayWay("在线支付");
			
			
			
			
			//旅游者代表签字
			OrdPerson traveller = order.getRepresentativePerson();
			if (traveller==null) {
				traveller = new OrdPerson();
			}
			travelContractVO.setFirstTravellerPerson(traveller);
//			travelContractVO.setSignaturePersonName(traveller.getFullName());
			
			//旅行社盖章
			travelContractVO.setStampImage(getStampImageNameByFilialeName(prodProduct.getFiliale()));
			
			ResultHandleT<SuppSupplier> resultHandleSuppSupplier =suppSupplierClientService.findSuppSupplierById(order.getMainOrderItem().getSupplierId());
			if (resultHandleSuppSupplier.isSuccess()) {
				suppSupplier = resultHandleSuppSupplier.getReturnContent();
			}
			//营业地址
			if (travelContractVO.getDelegateGroup()) {
				travelContractVO.setLocalTravelAgencyName(suppSupplier.getSupplierName());
				travelContractVO.setLocalTravelAgencyAddress(suppSupplier.getAddress());
			} else {
				travelContractVO.setLocalTravelAgencyName("/");
				travelContractVO.setLocalTravelAgencyAddress("/");
			}
			

			//旅行社监督、投诉电话：                  

			
			//结束日期
			Date beginDate = order.getVisitTime();
			if (routeDays!=null) {
				travelContractVO.setOverDate(DateUtil.formatDate(DateUtils.addDays(beginDate, routeDays-1), "yyyy-MM-dd"));
			}
			
			
		
			//甲方代表
			travelContractVO.setFirstDelegatePersonName(traveller.getFullName());
			//联系电话
			if(order.getContactPerson() != null){
				travelContractVO.setContactTelePhoneNo(order.getContactPerson().getMobile());
			}

			//日期
			travelContractVO.setFirstSignatrueDate(DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd"));
			travelContractVO.setSecondSignatrueDate(DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd"));
			
		}
		
		
		travelContractVO.setSuppSupplier(suppSupplier);
		
		// 根据“公司主体”, 差异化信息处理
		super.handleCompanyType(ordTravelContract, order, travelContractVO);
		
		return travelContractVO;
	}

	

	@Override
	public ResultHandle updateTravelContact(OutboundTourContractVO contractVO,
			String operatorName) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ResultHandleT<String> getContractTemplateHtml() {
		return contractTemplateHtml(templateName.toString());
	}




	@Override
	public ResultHandle updateTravelContact(TravelContractVO travelContractVO, OrdOrder order, OrdTravelContract ordTravelContract, String operatorName) {
		return this.updateTravelContact(ordTravelContract, travelContractVO, operatorName);
	}
	
	
	
}
