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

import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.goods.vo.ProdHotelcombGoodsExtVo;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombProductPropValueService;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelcombGoodsExtService;
import com.lvmama.dest.api.vst.goods.service.IHotelGoodsQueryVstApiService;
import com.lvmama.dest.api.vst.goods.vo.HotelGoodsVstVo;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.curise.service.ProdCuriseProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdLineRouteClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.vo.ProdLineRouteVO;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.pdf.PdfUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.econtract.TravelContractVO;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.neworder.order.NewOrderConstant;
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
 * @author chenpingfan
 * 目的地BU委托服务协议
 */
@Service("destCommissionedServiceAgreementService")
public class DestCommissionedServiceAgreementServiceImpl extends AbstractOrderTravelElectricContactService implements IOrderElectricContactService {
	
	private static final Log LOG = LogFactory.getLog(DestCommissionedServiceAgreementServiceImpl.class);

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
	private SuppGoodsClientService suppGoodsClientService;
	
	@Autowired
	private ProdLineRouteClientService prodLineRouteClientService;
	
	@Autowired
    private OrderService orderService;

	@Autowired
	private IHotelcombGoodsExtService hotelcombGoodsExtService;
	
	@Autowired
	private IHotelCombProductPropValueService hotelCombProductPropValueService;
	
	private static final String SERVER_TYPE = "COM_AFFIX";
	
	private static final String contractName = "委托服务协议书";
	private static final String templateName = "destCommissionedServiceAgreementTemplate.ftl";
	
	@Autowired
	private IHotelGoodsQueryVstApiService hotelGoodsQueryVstApiService;

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
			
			
			travelContractVO.setTemplateDirectory("file:///" + directioryFile.getAbsolutePath());//图片单选按钮
			LOG.info("advanceProductAgreementContractServiceImpl.saveTravelContact,fileDir=" + travelContractVO.getTemplateDirectory());
		
			
			rootMap.put("travelContractVO", travelContractVO);
			
			rootMap.put("order", order);
			
			//rootMap.put("product", product);
			
			Map<String,List<OrderMonitorRst>>  chidOrderMap=findChildOrderList(ordTravelContract,order,false);
			
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
				String travellers = "";
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
	 * @param curiseProductVO
	 * @return
	 */
	private TravelContractVO buildTravelContractVOData(OrdTravelContract ordTravelContract,OrdOrder order) {
		
		List<OrdOrderPack>  ordPackList=order.getOrderPackList();
		
		HashMap<String, Object> mapProduct=this.getProductIdAndName(ordTravelContract, order);
		Long productId=(Long)mapProduct.get("productId");
		String productName=(String)mapProduct.get("productName");
		OrdOrderItem orderContractItem=(OrdOrderItem)mapProduct.get("orderContractItem");
		
		TravelContractVO travelContractVO = null;
		ProdProductParam params = new ProdProductParam();
		params.setProductProp(true);
		params.setProductPropValue(true);
//		params.setProductBranch(true);
//		params.setProductBranchValue(true);
//		params.setProdEcontract(true);
		//合同需要即时数据 不走缓存
		ResultHandleT<ProdProduct> resultHandle=this.prodProductClientService.findProdProductById(productId, params);
		
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
			
			//订单支付金额
			travelContractVO.setTraveAmount(PriceUtil.trans2YuanStr(order.getOughtAmount()));
			//甲方
			String travellers = "";
			//存储游玩人列表
			List<OrdPerson> orderPersonList = new ArrayList<OrdPerson>();
			for (OrdPerson ordPerson : order.getOrdPersonList()) {
				if (ordPerson != null) {
//					if(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name().equalsIgnoreCase(ordPerson.getPersonType())){
//						travellers = ordPerson.getFullName();
//					}
					if(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equalsIgnoreCase(ordPerson.getPersonType())){
						orderPersonList.add(ordPerson);
					}
				}
				
			}
			Map<String, Object> filialmap = getFilialeMap(prodProduct);
			String filialName = (String) filialmap.get("secondParty");
			String stampImg = (String) filialmap.get("stampImg");
			
			for(int i=0;i<orderPersonList.size();i++){
				if(i>0 && i%5==0 && orderPersonList.size()>5) {
					travellers+="<br />";
				}
				if(StringUtils.isNotBlank(orderPersonList.get(i).getFullName())){
					if (i < orderPersonList.size()) {
						travellers += orderPersonList.get(i).getFullName()+ ",";
					}else {
						travellers += orderPersonList.get(i).getFullName();
					}	
				}
			}
			travellers = travellers.substring(0,travellers.length()-1);
			travelContractVO.setTravellers(travellers);
			//游玩人集合
			travelContractVO.setOrdTravellerList(orderPersonList);
			//受托方 (产品要求写死)
			travelContractVO.setFilialeName(filialName);
			
			//监督电话
			travelContractVO.setJianduTel(this.jianduTelMap.get(order.getFilialeName()));
			//产品名称
			travelContractVO.setProductName(productName);
			
			//出发日期
			travelContractVO.setVistDate(DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd"));
			
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
			List<OrdOrderItem> insuranceOrderItemList = getInsuranceOrdOrderItem(order);
			if (CollectionUtils.isNotEmpty(insuranceOrderItemList)) {				
				travelContractVO.setInsuranceOrderItemList(insuranceOrderItemList);
				
			}
			//旅游者代表签字
			OrdPerson traveller = order.getRepresentativePerson();
			if (traveller==null) {
				traveller = new OrdPerson();
			}
			travelContractVO.setFirstTravellerPerson(traveller);
			//旅行社盖章
			travelContractVO.setStampImage(stampImg);
			
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
			if(null != order.getContactPerson() && null !=order.getContactPerson().getMobile()){
				//联系电话
				travelContractVO.setContactTelePhoneNo(order.getContactPerson().getMobile());	
			}			
			//日期
			travelContractVO.setFirstSignatrueDate(DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd"));
			travelContractVO.setSecondSignatrueDate(DateUtil.formatDate(order.getCreateTime(), "yyyy-MM-dd"));
			travelContractVO.setCreateTime(DateUtil.getChineseDay(order.getCreateTime()));
			//退改说明
			Map<String, Object> productPropMap = prodProduct.getPropValue();
			
			// 酒套餐的产品属性查询酒店库
			if (BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId()
					.equals(orderContractItem.getCategoryId())) {
				RequestBody<Long> requestBody = new RequestBody<Long>();
				requestBody.setTFlowStyle(productId, NewOrderConstant.VST_ORDER_TOKEN);
				ResponseBody<Map<String, Object>> response = hotelCombProductPropValueService.getProductPropValue(requestBody);
				productPropMap = response.getT();
			}
			if(productPropMap != null) 
			{	
				//费用包含
				String _priceIncludes = "";
				//费用不包含
				String priceNotIncludes = "";

				if(StringUtil.isEmptyString(_priceIncludes)&&StringUtil.isEmptyString(priceNotIncludes)){
					LOG.info("start calc _priceIncludes and priceNotIncludes 1");
					// 酒套餐费用，关联商品
					if(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId()
							.equals(orderContractItem.getCategoryId())){
						if (null != orderContractItem.getSuppGoodsId()) {
							RequestBody<Long> requestBody = new RequestBody<Long>();
							requestBody.setTFlowStyle(orderContractItem.getSuppGoodsId(), NewOrderConstant.VST_ORDER_TOKEN);
							ResponseBody<ProdHotelcombGoodsExtVo> response = hotelcombGoodsExtService.findHotelcombGoodsByGoodsId(requestBody);
							if (null != response && null != response.getT()) {
								priceNotIncludes = response.getT().getCostExclude();
								_priceIncludes = response.getT().getCostInclude();
							}
						}
					}else{
						ResultHandleT<ProdLineRoute> lineRouteResult = null;
						ProdLineRoute prodLineRoute=null;
						if(null != order.getLineRouteId()){
							lineRouteResult = prodLineRouteClientService.findByProdLineRouteId(order.getLineRouteId());
							if (lineRouteResult!=null) {
								prodLineRoute=lineRouteResult.getReturnContent();
							}
							priceNotIncludes = prodLineRoute.getCostExclude();
							_priceIncludes = prodLineRoute.getCostInclude();	
						}	
					}
				}
				
				if(StringUtil.isEmptyString(_priceIncludes)&&StringUtil.isEmptyString(priceNotIncludes)){	
					LOG.info("start calc _priceIncludes and priceNotIncludes 2");
					if(CollectionUtils.isNotEmpty(prodProduct.getProdLineRouteList())){							
						if(prodProduct.getProdLineRouteList().size() == 1){
							_priceIncludes = prodProduct.getProdLineRouteList().get(0).getCostInclude();
							priceNotIncludes = prodProduct.getProdLineRouteList().get(0).getCostExclude();
						}else{
							for (ProdLineRouteVO lineRoute : prodProduct.getProdLineRouteList()) {
								if("Y".equalsIgnoreCase(lineRoute.getCancleFlag())){
									_priceIncludes = lineRoute.getCostInclude();
									priceNotIncludes = lineRoute.getCostExclude();
								}
							}
						}
					}
				}
				
				if(StringUtil.isNotEmptyString(_priceIncludes)){
					_priceIncludes = _priceIncludes.replaceAll("</?[^<]+>", "");
					travelContractVO.setPriceIncludes(_priceIncludes);
				}
				if(StringUtil.isNotEmptyString(priceNotIncludes)){
					priceNotIncludes = priceNotIncludes.replaceAll("</?[^<]+>", "");
					travelContractVO.setPriceNotIncludes(priceNotIncludes);
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

	private Map<String, Object> getFilialeMap(ProdProduct product){
		String filiale = null;
		String companyType = null;
		Map<String, Object> map = new HashMap<String, Object>();
		//如果是酒店套餐  2017/10/16 add by ltwangwei 加入新酒套餐支持
		if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(product.getBizCategoryId())
				|| BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(product.getBizCategoryId())){
			//新酒套餐走子系统接口拿商品信息
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(product.getBizCategoryId())){
				RequestBody<Long> suppGoodsIdRq = new RequestBody<Long>();
				suppGoodsIdRq.setT(product.getProductId());
				suppGoodsIdRq.setToken(Constant.DEST_BU_HOTEL_TOKEN);//token必须设置，否则会报错
				
				ResponseBody<List<HotelGoodsVstVo>> responseBody = hotelGoodsQueryVstApiService.findSuppGoodsByProductId(suppGoodsIdRq);
				if (responseBody==null) {
					LOG.error("DestCommissionedServiceAgreementServiceImpl.getFilialeMap use new service: IHotelGoodsQueryVstApiService#findSuppGoodsByProductId!responseBody is null! for product " + product.getProductId() + " fail!");
					return null;
				}
				if (responseBody.isFailure()) {
					LOG.error("DestCommissionedServiceAgreementServiceImpl.getFilialeMap use new service: IHotelGoodsQueryVstApiService#findSuppGoodsByProductId for product " + product.getProductId() + " fail!"+" message is "+responseBody.getMessage());
					return null;
				}
				
				List<HotelGoodsVstVo> hotelGoodsVstVoList = responseBody.getT();
				if (CollectionUtils.isEmpty(hotelGoodsVstVoList)) {
					LOG.warn("DestCommissionedServiceAgreementServiceImpl.getFilialeMap use new service: IHotelGoodsQueryVstApiService#findSuppGoodsByProductId!hotelGoodsVstVoList is empty! for product " + product.getProductId() + " fail!");
					return null;
				}
				//取第一个商品为基础信息商品
				HotelGoodsVstVo goodsVstVo = hotelGoodsVstVoList.get(0);
				filiale = goodsVstVo.getFiliale();
				companyType = goodsVstVo.getCompanyType();
				LOG.info("DestCommissionedServiceAgreementServiceImpl.getFilialeMap get data from hotelcomb,filiale is "+filiale +"and companyType is"+companyType);
			}else{
				ResultHandleT<List<SuppGoods>> resultHandle = suppGoodsClientService.findSuppGoodsByProductId(product.getProductId());
				if(null != resultHandle && null != resultHandle.getReturnContent()){
					List<SuppGoods> suppGoodsList = resultHandle.getReturnContent();
					if(CollectionUtils.isNotEmpty(suppGoodsList)){
						//取第一个商品为基础信息商品
						filiale = suppGoodsList.get(0).getFiliale();
						companyType = suppGoodsList.get(0).getCompanyType();				
					}
				}
			}
			LOG.info("filiale is "+filiale +"and companyType is"+companyType);
			if(StringUtils.isNotEmpty(companyType) && StringUtils.isNotEmpty(filiale)){
				if("XINGLV".equals(companyType)){
					map.put("secondParty", filialeNameMap.get(filiale));
					map.put("stampImg", getStampImageNameByFilialeName(filiale));
				}
				if("GUOLV".equals(companyType)){
					map.put("secondParty", Constant.SETTLEMENT_COMPANY.COMPANY_2.getCnName());
					map.put("stampImg", GUOLV_stampImage);
				}
			}
			if(StringUtils.isEmpty(companyType) && StringUtils.isEmpty(filiale)){
				map.put("secondParty", Constant.SETTLEMENT_COMPANY.COMPANY_3.getCnName());
				map.put("stampImg","SH_ECONTRACT.png");
			}			
			if(StringUtils.isNotEmpty(companyType) && StringUtils.isEmpty(filiale)){
				if("XINGLV".equals(companyType)){
					map.put("secondParty", Constant.SETTLEMENT_COMPANY.COMPANY_3.getCnName());
					map.put("stampImg","SH_ECONTRACT.png");
				}else if("GUOLV".equals(companyType)){
					map.put("secondParty", Constant.SETTLEMENT_COMPANY.COMPANY_2.getCnName());
					map.put("stampImg", GUOLV_stampImage);
				}else{
					map.put("secondParty", Constant.SETTLEMENT_COMPANY.COMPANY_3.getCnName());
					map.put("stampImg", "SH_ECONTRACT.png");
				}
				
			}
		}else{	
			if(StringUtils.isNotEmpty(product.getFiliale())){
				filiale = product.getFiliale();
				if(null==filialeNameMap.get(filiale)){
					map.put("secondParty", Constant.SETTLEMENT_COMPANY.COMPANY_2.getCnName());
					map.put("stampImg", GUOLV_stampImage);
				}else{
					if(filiale.equals("SY_FILIALE")){
						map.put("secondParty", Constant.SETTLEMENT_COMPANY.COMPANY_2.getCnName());
						map.put("stampImg", GUOLV_stampImage);
					}else{
						map.put("secondParty", filialeNameMap.get(filiale));
						map.put("stampImg",getStampImageNameByFilialeName(filiale));	
					}
				}
			}else
			{
				map.put("secondParty", Constant.SETTLEMENT_COMPANY.COMPANY_3.getCnName());
				map.put("stampImg", "SH_ECONTRACT.png");
			}	
			
		}
		return map;
	}
	
	
}
