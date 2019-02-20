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

import com.lvmama.vst.comm.enumeration.CommEnumSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.curise.service.ProdCuriseProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdLineRouteClientService;
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
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.back.prod.po.ProdLineRouteDetail;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductProp;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
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
@Service("commissionedServiceAgreementService")
public class CommissionedServiceAgreementServiceImpl extends AbstractOrderTravelElectricContactService implements IOrderElectricContactService {
	
	private static final Log LOG = LogFactory.getLog(CommissionedServiceAgreementServiceImpl.class);

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
	@Autowired
	private ProdLineRouteClientService prodLineRouteClientService;
	@Autowired
    private OrderService orderService;
	
	
	private static final String SERVER_TYPE = "COM_AFFIX";
	
	private static final String contractName = "委托服务协议书";
	private static final String templateName = "commissionedServiceAgreementTemplate.ftl";

	@Override
	public ResultHandle saveTravelContact(OrdTravelContract ordTravelContract, String operatorName) {
		ResultHandle resultHandle = new ResultHandle();
		
		if (ordTravelContract != null) {
			
			LOG.info("---------------开始生成委托服务协议合同orderId:" + ordTravelContract.getOrderId() + "-------------");
			
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
				String contractTemplate = ordTravelContract.getContractTemplate();
				ProdProduct prodProduct = null;
				String productType = "";
				if ( travelContractVO.getProdProduct() != null) {
					prodProduct = travelContractVO.getProdProduct();
					productType = prodProduct.getProductType();
				}
				if ( prodProduct != null) {
					if (BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId())
							&& ProdProduct.PRODUCTTYPE.FOREIGNLINE.getCode().equals(productType)
							&& CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode().equals(contractTemplate)) {
						//行程单无效，发送预警邮件
						LOG.info("saveTravelContact---order="+ order.getOrderId()+ "checkprodLineRouteVOList start");
						resultHandle = checkprodLineRouteVOList(travelContractVO,order,travelContractVO.getProdProduct());
						if(resultHandle.isFail()){
							LOG.info("saveTravelContact---order="+ order.getOrderId()+ "---checkprodLineRouteVOList isFail()--------生成合同时行程单无效，发送预警邮件-------------");
							return resultHandle;
						}
						LOG.info("saveTravelContact---order="+ order.getOrderId()+ "checkprodLineRouteVOList end");
						//end
					}

				}

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
					fileName = "commissionedServiceAgreement_" + travelContractVO.getContractVersion() + ".pdf";
				}else{
					fileName = "commissionedServiceAgreement_emptyTemplate.pdf";
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
					
					//合同签约状态逻辑
					setOrdContractStatus(ordTravelContract, order,
							isCreateOrder);
					
					
					ordTravelContract.setContractName(contractName.toString());
//					ordTravelContract.setSigningType(OrderEnum.ORDER_CONTRACT_SIGNING_TYPE.ONLINE.name());
					ordTravelContract.setCreateTime(new Date());
					
					if (ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, operatorName) <= 0) {
						ordTravelContractService.saveOrdTravelContract(ordTravelContract, operatorName);
					}
					
					String content=contractName+"更新成功";
					if (isCreateOrder) {
						content=contractName+"生成成功";
					}
					this.insertOrderLog(ordTravelContract.getOrderId(), ordTravelContract.getOrdContractId(), operatorName, content,  null);
					
				} else {
					resultHandle.setMsg("合同上传失败。");
				}

				if ( prodProduct != null) {
					if (BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId())
							&& ProdProduct.PRODUCTTYPE.FOREIGNLINE.getCode().equals(productType)
							&& CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode().equals(contractTemplate)) {
						//行程单生成
						LOG.info("saveTravelContact---order="+ order.getOrderId()+ "saveTravelItineraryContract start");
						this.saveTravelItineraryContract(ordTravelContract,operatorName);
						LOG.info("saveTravelContact---order="+ order.getOrderId()+ "saveTravelItineraryContract end");
					}

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
				travelContractVO.setSuitableName(contractVO.getSuitableName());//修改委托方
				LOG.info("updateTravelContact suitableName="+contractVO.getSuitableName());
				if(order.getContactPerson() != null){
					order.getContactPerson().setMobile(contractVO.getContractMobile());//修改委托方联系电话
				}
				travelContractVO.setFilialeName(contractVO.getFilialeName());//修改受托方
				travelContractVO.setLvMobile(contractVO.getLvMobile());//修改受托方联系电话
//				if(travelContractVO.getFirstTravellerPerson() != null){//甲方代表
//					travelContractVO.getFirstTravellerPerson().setFullName(contractVO.getFirstDelegatePersonName());
//				}
//				travelContractVO.getFirstTravellerPerson().setFullName(contractVO.getTravellers());
				travelContractVO.setSingnDate(contractVO.getSingnDate());//甲方代表日期
				travelContractVO.setLvSingnDate(contractVO.getLvSingnDate());//甲方代表日期
				
				
				//生成合同中应有字段为空时，发送预警邮件(委托服务协议，预付款)
				resultHandle = checkUpdateAgreementData(travelContractVO,order);
				if(resultHandle.isFail()){
					LOG.info("---------------合同中应有字段为空时，发送预警邮件(委托服务协议，预付款)-------------");
					return resultHandle;
				}
				//end
				String contractTemplate = ordTravelContract.getContractTemplate();
				ProdProduct prodProduct = null;
				String productType = "";
				if ( travelContractVO.getProdProduct() != null) {
					prodProduct = travelContractVO.getProdProduct();
					productType = prodProduct.getProductType();
				}
				if ( prodProduct != null) {
					if (BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId())
							&& ProdProduct.PRODUCTTYPE.FOREIGNLINE.getCode().equals(productType)
							&& CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode().equals(contractTemplate)) {
						//行程单无效，发送预警邮件
						LOG.info("updateTravelContact---order="+ order.getOrderId()+ "checkprodLineRouteVOList start");
						resultHandle = checkprodLineRouteVOList(travelContractVO,order,travelContractVO.getProdProduct());
						if(resultHandle.isFail()){
							LOG.info("updateTravelContact---order="+ order.getOrderId()+ "---checkprodLineRouteVOList isFail()--------生成合同时行程单无效，发送预警邮件-------------");
							return resultHandle;
						}
						LOG.info("updateTravelContact---order="+ order.getOrderId()+ "checkprodLineRouteVOList end");
						//end
					}

				}
				
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
				
				String fileName = "commissionedServiceAgreement_" + travelContractVO.getContractVersion() + ".pdf";
				
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
					
					//合同签约状态逻辑
					setOrdContractStatus(ordTravelContract, order,isCreateOrder);
					ordTravelContract.setContractName(contractName.toString());
//					ordTravelContract.setSigningType(OrderEnum.ORDER_CONTRACT_SIGNING_TYPE.ONLINE.name());
					ordTravelContract.setCreateTime(new Date());
					
					if (ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, operatorName) <= 0) {
						ordTravelContractService.saveOrdTravelContract(ordTravelContract, operatorName);
					}
					
					String content=contractName+"更新成功";
					if (isCreateOrder) {
						content=contractName+"生成成功";
					}
					this.insertOrderLog(ordTravelContract.getOrderId(), ordTravelContract.getOrdContractId(), operatorName, content,  null);
					
				} else {
					resultHandle.setMsg("合同上传失败。");
				}

				if ( prodProduct != null) {
					if (BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId())
							&& ProdProduct.PRODUCTTYPE.FOREIGNLINE.getCode().equals(productType)
							&& CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode().equals(contractTemplate)) {
						//行程单生成
						LOG.info("updateTravelContact---order="+ order.getOrderId()+ "saveTravelItineraryContract start");
						this.saveTravelItineraryContract(ordTravelContract, operatorName);
						LOG.info("updateTravelContact---order="+ order.getOrderId()+ "saveTravelItineraryContract end");
					}

				}
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
		TravelContractVO travelContractVO = null;
		Map<String,List<OrderMonitorRst>>  chidOrderMap = null;
		
		if (order != null) {
			
			//针对下单后置的订单
			//条件 1.订单后置 2.游玩人未锁定  ，下载合同是空模板，反之则显示有值的正常模板
			if(("Y").equals(order.getTravellerDelayFlag()) && ("N").equals(order.getTravellerLockFlag())){
				travelContractVO = new TravelContractVO();
				chidOrderMap = new HashMap<String, List<OrderMonitorRst>>();
			}else{
				LOG.info("开始组装委托服务协议合同orderId:" + order.getOrderId() );
				travelContractVO = buildTravelContractVOData(ordTravelContract,order);
				//关联销售当地游，替换相关合同内容信息
				replaceTravelContractVOData(ordTravelContract,order,travelContractVO);
				chidOrderMap=findChildOrderList(ordTravelContract,order,false);
			}
			
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
				
				//甲方
				String travellers = null;
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("orderItemId", orderContractItem.getOrderItemId()); //当前子订单Id
				
				List<OrdItemPersonRelation> ordItemPersonRelationList= ordItemPersonRelationService.findOrdItemPersonRelationList(params);
				
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
		LOG.info("CommissionedServiceAgreementServiceImpl.buildTravelContractVOData.orderId:"+order.getOrderId());
		List<OrdOrderPack>  ordPackList=order.getOrderPackList();
		
		HashMap<String, Object> mapProduct=this.getProductIdAndName(ordTravelContract, order);
		Long productId=(Long)mapProduct.get("productId");
		String productName=(String)mapProduct.get("productName");
		OrdOrderItem orderContractItem=(OrdOrderItem)mapProduct.get("orderContractItem");

		TravelContractVO travelContractVO = new TravelContractVO();
		
		ProdProductParam param = new ProdProductParam();
		param.setProductProp(true);
		param.setProductBranchValue(true);
		param.setProdEcontract(true);

		ResultHandleT<ProdProduct> resultHandle=this.prodProductClientService.findLineProductByProductId(productId, param);
		
		ProdProduct prodProduct=resultHandle.getReturnContent();
		
		//退改说明
		Map<String, Object> productPropMap = prodProduct.getPropValue();
		

		//根据行程ID获取费用包含和费用不包含
		if(order.getLineRouteId()!=null && productPropMap!=null){
			productPropMap.putAll(getCostIncExc(order.getLineRouteId()));
		}
				
		String packedProductId = "";//被打包产品ID
		String autoPackTrafficCode = "";//自动打包交通
		String isusePackedCostExplanationCode = "";//是否使用被打包产品费用说明
		List<ProdProductProp> productPropList = prodProduct.getProdProductPropList();
		if(productPropList != null && productPropList.size() > 0){
			for(ProdProductProp prop : productPropList){
				if(prop != null && prop.getBizCategoryProp() != null && StringUtils.isNotEmpty(prop.getBizCategoryProp().getPropCode())){
					if(prop.getBizCategoryProp().getPropCode().equals("packed_product_id")){
						packedProductId = prop.getPropValue();
					}
					if(prop.getBizCategoryProp().getPropCode().equals("auto_pack_traffic")){
						autoPackTrafficCode = prop.getPropValue();
					}
					if(prop.getBizCategoryProp().getPropCode().equals("isuse_packed_cost_explanation")){
						isusePackedCostExplanationCode = prop.getPropValue();
					}
				}
			}
		}
		//当地游一日游增加行程详情
        LOG.info("commissionedServiceAgreementService BizCategoryId:"+prodProduct.getBizCategoryId()+",ProducTourtType:"+prodProduct.getProducTourtType());
		if(prodProduct.getBizCategoryId()==16L&&prodProduct.getProducTourtType().equals("ONEDAYTOUR")){
		  ResultHandleT<ProdLineRoute> lineRouteResult = null;
	        try {
	          if(order.getLineRouteId()!=null){
	            lineRouteResult = this.prodLineRouteClientService.findProdLineRouteById(order.getLineRouteId());
	            ResultHandleT<List<ProdLineRouteDetail>> result = prodLineRouteClientService.findProdLineRouteDetailByLineRouteId(order.getLineRouteId());
                if (result != null && result.isSuccess() && !"".equals(result.getReturnContent())) {
                  lineRouteResult.getReturnContent().setProdLineRouteDetailList(result.getReturnContent());
                } 
                travelContractVO.setLineRoute(lineRouteResult.getReturnContent());
                LOG.info("commissionedServiceAgreementService orderId:"+order.getOrderId()+"行程明细:"+GsonUtils.toJson(lineRouteResult.getReturnContent()));
	          }
	        } catch (Exception e) {
	            LOG.error("找不到对应的行程明细，行程ID：" + order.getLineRouteId() + "，异常信息： {}", e);
	        }
		}
		
		//自动打包交通 && 使用被打包产品费用说明
		if("Y".equals(autoPackTrafficCode) && "Y".equals(isusePackedCostExplanationCode)){
			LOG.info("Now begin to get line route for packaged product " + packedProductId);
			resultHandle=this.prodProductClientService.findLineProductByProductId(Long.parseLong(packedProductId), param);
			prodProduct=resultHandle.getReturnContent();
			List<ProdLineRoute> prodLineRouteList = prodLineRouteClientService.findCacheLineRouteListByProductId(Long.parseLong(packedProductId)).getReturnContent();;
			if (prodLineRouteList != null) {
				productPropMap.putAll(getCostIncExc(prodLineRouteList.get(0).getLineRouteId()));
			} 
			travelContractVO.setDescription("成人、2-12周岁儿童均含往返机票 ;以上报价已包含机票税和燃油附加费。");
		}
		
		List<OrdOrderItem> insuranceOrderItemList = getInsuranceOrdOrderItem(order);
		if (insuranceOrderItemList != null && !insuranceOrderItemList.isEmpty()) {
			travelContractVO.setInsuranceOrderItemList(insuranceOrderItemList);
		}
		
		
		SuppSupplier suppSupplier = new SuppSupplier();
		if (order != null && prodProduct != null) {
			travelContractVO.setProdProduct(prodProduct);
			travelContractVO.setProductId(productId);
			String appendVersion = getAppendVersion(ordTravelContract);
			//合同编号
			String version = DateUtil.formatDate(order.getVisitTime(), "yyyyMMdd") + "-" + order.getOrderId() + "-" + appendVersion;
			travelContractVO.setContractVersion(version);
			
			//订单编号
			travelContractVO.setOrderId(order.getOrderId().toString());
			
			//订单支付金额
			travelContractVO.setTraveAmount(PriceUtil.trans2YuanStr(order.getOughtAmount()));
			//判断是否为出境跟团游打包签证
			boolean packageTour_outbound=ispackageTourOutbound(order);
			if(packageTour_outbound){
				for (OrdOrderItem ordOrderItem : order.getOrderItemList()) {
					//判断为出境跟团游打包签证
					//判断是否为跟团游
					if (ordOrderItem.getCategoryId() != null && ordOrderItem.getCategoryId() == 15 && CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(ordOrderItem.getBuCode())) {
						//剔除跟团游的价格
						Long totalAmount = ordOrderItem.getTotalAmount();
						travelContractVO.setTraveAmount(PriceUtil.trans2YuanStr(order.getOughtAmount()-totalAmount));
					}
				}
			}

			
			//甲方
			String travellers = null;
			int travellerCount = 0;
			//下单联系人+出游人
			String suitableName = null;
			int suitableCount = 0;
			//判断是否为亲子游学订单
			boolean isParentageFlag=isParentageOrder(order);
			travelContractVO.setParentageGroup(isParentageFlag);

			Map<String, Object> var1 = new HashMap<String, Object>();
			var1.put("orderId", order.getOrderId());
			List<OrdPerson> ordPersonList = ordPersonService.getBookPersonInfoByOrderId(var1);
			if(ordPersonList != null && ordPersonList.size() > 0){
				LOG.info("ordPersonListSize:"+ordPersonList.size());
				for (OrdPerson ordPerson : ordPersonList) {
					if (ordPerson != null && OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equalsIgnoreCase(ordPerson.getPersonType()) &&
							OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name().equalsIgnoreCase(ordPerson.getObjectType())) {
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
					if (isParentageFlag && ordPerson != null && OrderEnum.ORDER_PERSON_TYPE.CONTACT.name().equalsIgnoreCase(ordPerson.getPersonType()) &&
							OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name().equalsIgnoreCase(ordPerson.getObjectType())) {
						if (suitableName == null) {
							suitableName = ordPerson.getFullName();
						} else {
							if (suitableCount % 5 == 0) {
								suitableName = suitableName + ",<br />" + ordPerson.getFullName();
							} else {
								suitableName = suitableName + "," + ordPerson.getFullName();
							}

						}
						suitableCount++;
					}
				}
				for (OrdPerson ordPerson : ordPersonList) {
					if (isParentageFlag && ordPerson != null && OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equalsIgnoreCase(ordPerson.getPersonType()) &&
							OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name().equalsIgnoreCase(ordPerson.getObjectType())) {
						if (suitableName == null) {
							suitableName = ordPerson.getFullName();
						} else {
							if (suitableCount % 5 == 0) {
								suitableName = suitableName + ",<br />" + ordPerson.getFullName();
							} else {
								suitableName = suitableName + "," + ordPerson.getFullName();
							}

						}
						suitableCount++;
					}
				}
			}
			travelContractVO.setTravellers(travellers);
			travelContractVO.setSuitableName(suitableName);
			LOG.info("buildTravelContractVOData  suitableName=" + suitableName);
			
			//存储游玩人列表
			List<OrdPerson> orderPersonList = new ArrayList<OrdPerson>();
			for (OrdPerson ordPerson : order.getOrdPersonList()) {
				if (ordPerson != null) {
					if(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name().equalsIgnoreCase(ordPerson.getPersonType())){
						travellers = ordPerson.getFullName();
					}
					if(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equalsIgnoreCase(ordPerson.getPersonType())){
						orderPersonList.add(ordPerson);
					}
				}
				
			}
//			travelContractVO.setOrdTravellerList(orderPersonList);
			
			travelContractVO.setOrdTravellerList(order.getOrdTravellerList());
			
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
			travelContractVO.setStampImage(getStampImageNameByFilialeName(order.getFilialeName()));
			
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
			if(order.getContactPerson() !=null){
				travelContractVO.setContactTelePhoneNo(order.getContactPerson().getMobile());
			}

			//日期
			travelContractVO.setFirstSignatrueDate(DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd"));
			travelContractVO.setSecondSignatrueDate(DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd"));
			
			travelContractVO.setCreateTime(DateUtil.getChineseDay(order.getCreateTime()));
			
			getCostIncExc(travelContractVO.getLineRouteId());
			
			
			if(productPropMap != null){
				if(null != productPropMap.get(BizEnum.LINE_PROP_CODE.cost_free.getCode())){//费用不包含
					String priceNotIncludes = (String) productPropMap.get(BizEnum.LINE_PROP_CODE.cost_free.getCode());				
					if(StringUtil.isNotEmptyString(priceNotIncludes)){
						String _priceNotIncludes = priceNotIncludes.replaceAll("</?[^<]+>", "");
						if(StringUtil.isNotEmptyString(_priceNotIncludes)){
							travelContractVO.setPriceNotIncludes(_priceNotIncludes);
						}
					}
				}
				if(null != productPropMap.get(BizEnum.LINE_PROP_CODE.the_fee_includes.getCode())){//费用包含
					String priceIncludes= (String) productPropMap.get(BizEnum.LINE_PROP_CODE.the_fee_includes.getCode());				
					if(StringUtil.isNotEmptyString(priceIncludes)){
						String _priceIncludes = priceIncludes.replaceAll("</?[^<]+>", "");
						if(StringUtil.isNotEmptyString(_priceIncludes)){
							travelContractVO.setPriceIncludes(_priceIncludes);
						}
					}
				}
				if(null != productPropMap.get(BizEnum.LINE_PROP_CODE.change_and_cancellation_instructions.getCode())){//退改说明
					String backToThat= (String) productPropMap.get(BizEnum.LINE_PROP_CODE.change_and_cancellation_instructions.getCode());				
					if(StringUtil.isNotEmptyString(backToThat)){
						String _backToThat = backToThat.replaceAll("</?[^<]+>", "");
						if(StringUtil.isNotEmptyString(_backToThat)){
							travelContractVO.setBackToThat(_backToThat);
						}
					}
				}
				
				if(null != productPropMap.get(BizEnum.LINE_PROP_CODE.important.getCode())){//行前须知
					String travelNotes= (String) productPropMap.get(BizEnum.LINE_PROP_CODE.important.getCode());				
					if(StringUtil.isNotEmptyString(travelNotes)){
						String _travelNotes = travelNotes.replaceAll("</?[^<]+>", "");
						if(StringUtil.isNotEmptyString(_travelNotes)){
							travelContractVO.setTravelNotes(_travelNotes);
						}
					}
				}
				if(null != productPropMap.get(BizEnum.LINE_PROP_CODE.warning.getCode())){//出行警示及说明
					String travelWarnings= (String) productPropMap.get(BizEnum.LINE_PROP_CODE.warning.getCode());				
					if(StringUtil.isNotEmptyString(travelWarnings)){
						String _travelWarnings = travelWarnings.replaceAll("</?[^<]+>", "");
						if(StringUtil.isNotEmptyString(_travelWarnings)){
							travelContractVO.setTravelWarnings(_travelWarnings);
						}
					}
				}
			}else{
				LOG.info("委托服务协议合同获取退改说明productPropMap为空");
			}
			
			//自由行(景酒)  国内bu退改说明
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())
			        && ProdProduct.PRODUCTTYPE.INNERLINE.name().equals(prodProduct.getProductType())){
				String rules= orderService.getRouteOrderRefundRules(order.getOrderId());
				LOG.info("国内bu退改说明：" + rules);
				travelContractVO.setBackToThat(rules);
			}
			
			
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
