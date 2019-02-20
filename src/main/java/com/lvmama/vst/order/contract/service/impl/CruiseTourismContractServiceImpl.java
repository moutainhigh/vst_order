package com.lvmama.vst.order.contract.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.prod.curise.service.ProdCuriseProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.line.po.LineRoute;
import com.lvmama.vst.back.line.po.LineShipDetail;
import com.lvmama.vst.back.order.po.OrdContractSnapshotData;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdContractDetail;
import com.lvmama.vst.back.prod.po.ProdEcontract;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.vo.ProdLineRouteVO;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.po.ComFileMap;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.pdf.PdfUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.econtract.TravelContractVO;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.order.contract.service.IOrderContractSnapshotService;
import com.lvmama.vst.order.contract.service.IOrderElectricService;
import com.lvmama.vst.order.contract.vo.CruiseTourismContractDataVO;
import com.lvmama.vst.order.contract.vo.OutboundTourContractDataVO;
import com.lvmama.vst.order.contract.vo.OutboundTourContractVO;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdItemContractRelationService;
import com.lvmama.vst.order.service.IOrdMulPriceRateService;
import com.lvmama.vst.order.service.IOrdOrderPackService;
import com.lvmama.vst.order.service.IOrdTravelContractService;
import com.lvmama.vst.order.service.IOrderUpdateService;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 
 * @author luolihua
 *
 */
@Service("cruiseTourismContractService")
public class CruiseTourismContractServiceImpl extends AbstractOrderTravelElectricContactService implements IOrderElectricService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CruiseTourismContractServiceImpl.class);

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
	
//	@Autowired
//	private BizInsurCatRuleService bizInsurCatRuleService;
	
	@Autowired
	private IOrderContractSnapshotService orderContractSnapshotService;
	
	private static final String SERVER_TYPE = "COM_AFFIX";
	private static final String contractName = "上海邮轮旅游合同";
	private static final String contractTemplate = "cruiseTourismContractTemplate.ftl";
	private static final String supplementaryTemplate = "cruiseTourismSupplementaryTemplate.ftl";
	//邮轮合同产品
	private static final String YOULUN_INSURANCE = "INSURANCE_734";
	private static final String APPROVE_DELEGATE_INSURANCE = "同意";
	private static final String UNAPPROVE_DELEGATE_INSURANCE = "不同意";
	@Override
	public ResultHandle saveTravelContact(OrdTravelContract ordTravelContract, String operatorName) {
		ResultHandle resultHandle = new ResultHandle();
		if (ordTravelContract != null) {
			OrdOrder order = complexQueryService.queryOrderByOrderId(ordTravelContract.getOrderId());
			if (order == null) {
				resultHandle.setMsg("订单ID=" + ordTravelContract.getOrderId() + "不存在。");
				return resultHandle;
			}
			
			List<OrdOrderItem> ordOrderItemList = order.getOrderItemList();
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
				Configuration configuration = initConfiguration(directioryFile);
				if (configuration == null) {
					resultHandle.setMsg("初始化freemarker失败。");
					return resultHandle;
				}
				Map<String,Object> rootMap = this.captureContract(ordTravelContract, order, directioryFile);
				TravelContractVO travelContractVO = (TravelContractVO)rootMap.get("travelContractVO");
				
				//合同中应有字段丢失，发送预警邮件
				resultHandle = checkSaveTravelContractData(travelContractVO,order);
				if(resultHandle.isFail()){
					LOG.info("---------------合同中应有字段丢失，发送预警邮件-------------");
					return resultHandle;
				}
				//end
				
				//行程单无效，发送预警邮件
				resultHandle = checkprodLineRouteVOList(travelContractVO,order,travelContractVO.getProdProduct());
				if(resultHandle.isFail()){
					LOG.info("---------------行程单无效，发送预警邮件-------------");
					return resultHandle;
				}
				//end
				
				//针对下单后置的订单
				//条件 1.订单后置 2.游玩人未锁定  ，下载合同是空模板，反之则显示有值的正常模板
				if(("Y").equals(order.getTravellerDelayFlag()) && ("N").equals(order.getTravellerLockFlag())){
					travelContractVO.setPersonAccidentInsurance("");
					travelContractVO.setCruiseInsurance("");
				} else {
					//目前无人身意外伤害保险产品，因此一律不同意委托购买
					travelContractVO.setPersonAccidentInsurance(UNAPPROVE_DELEGATE_INSURANCE);
					//未购买邮轮保险填充不同意
					if( StringUtils.isEmpty( travelContractVO.getCruiseInsurance())){
						travelContractVO.setCruiseInsurance(UNAPPROVE_DELEGATE_INSURANCE);
					}
				}
				//合同名称
				String contractFileName = null;
				//补充条款名称（唯一）
				String supplementaryName = null;
				
				if(StringUtils.isNotEmpty(travelContractVO.getContractVersion())){
					contractFileName = "CruiseTourismContract_" + travelContractVO.getContractVersion() + ".pdf";
				}else{
					contractFileName = "CruiseTourismContract_emptyTemplate.pdf";
				}
				
				if(StringUtils.isNotEmpty(travelContractVO.getContractVersion())){
					supplementaryName = "CruiseTourismSupplementary_" + travelContractVO.getContractVersion() + ".pdf";
				}else{
					supplementaryName = "CruiseTourismSupplementary_emptyTemplate.pdf";
				}
				
				Long contractFileId = uploadContractPDF(rootMap, configuration, contractTemplate, contractFileName);
				
				if (contractFileId != null && contractFileId != 0) {
					ResultHandleT<ComFileMap> handleA = null;
					handleA = uploadSupplementaryPDF(rootMap, configuration, supplementaryName);
					if (handleA.isFail()) {
						resultHandle.setMsg(handleA.getMsg());
						return resultHandle;
					}

					ordTravelContract.setVersion(travelContractVO.getContractVersion());
					ordTravelContract.setFileId(contractFileId);
					
					//合同签约状态逻辑
					setOrdContractStatus(ordTravelContract, order, true);
					
					ordTravelContract.setContractName(contractName);
					if(handleA.getReturnContent() != null) {
						ordTravelContract.setAttachementUrl(supplementaryName);
					}
					ordTravelContract.setCreateTime(new Date());
					if (ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, operatorName) <= 0) {
						ordTravelContractService.saveOrdTravelContract(ordTravelContract, operatorName);
					}
				} else {
					resultHandle.setMsg("合同上传失败。");
				}
				
				//行程单生成
				this.saveTravelItineraryContract(ordTravelContract, operatorName);
				
				String content = contractName + "生成成功";
				
				this.insertOrderLog(ordTravelContract.getOrderId(), ordTravelContract.getOrdContractId(), operatorName, content,  null);
				
				/*合同快照部分*/
				//1.获取合同填充的数据和该合同对应的行程单的数据
				CruiseTourismContractDataVO cruiseTourismContractDataVO = new CruiseTourismContractDataVO();
				cruiseTourismContractDataVO.setProductName(travelContractVO.getProductName());
				cruiseTourismContractDataVO.setLineShipDesc(travelContractVO.getLineShipDesc());
				cruiseTourismContractDataVO.setMinPersonCountOfGroup(travelContractVO.getMinPersonCountOfGroup());
				cruiseTourismContractDataVO.setDeparturePlace(travelContractVO.getDeparturePlace());
				cruiseTourismContractDataVO.setReturnPlace(travelContractVO.getReturnPlace());
				cruiseTourismContractDataVO.setSupplementaryTerms(travelContractVO.getSupplementaryTerms());
				cruiseTourismContractDataVO.setRecommendDetailList(travelContractVO.getRecommendDetailList());
				cruiseTourismContractDataVO.setShopingDetailList(travelContractVO.getShopingDetailList());
				
				Map<String,Object> travelItineraryContractMap = this.getContractContent(ordTravelContract,order);
				TravelContractVO travelItineraryVO = (TravelContractVO)travelItineraryContractMap.get("travelContractVO");
				if(null != travelItineraryVO.getShipLineRoute()){
					cruiseTourismContractDataVO.setShipLineRoute(travelItineraryVO.getShipLineRoute());
				}else if(null != travelItineraryVO.getLineRoute()){
					cruiseTourismContractDataVO.setLineRoute(travelItineraryVO.getLineRoute());
				}
				
				//2.根据组装的数据dataVO转化为json,并上传到文件服务器，并返回保存的文件ID
				Long jsonfileId = null;
				try {
					String str = JSONObject.toJSONString(cruiseTourismContractDataVO);
					byte[] _fileBytes = str.getBytes("UTF-8");
					ByteArrayInputStream bytesInputStream = new ByteArrayInputStream(_fileBytes);
					Long orderId = order.getOrderId();
					String jsonfileName = orderId + ".json";
					jsonfileId = fsClient.uploadFile(jsonfileName, bytesInputStream, SERVER_TYPE);
					if(null == jsonfileId){
						LOG.error("上传.json格式文件失败！");
						resultHandle.setMsg("上传.json格式文件失败！");
					}
					bytesInputStream.close();
				} catch (IOException e) {
					LOG.error(e.getMessage());
					resultHandle.setMsg("上传.json格式文件失败！");
				}
				//3.将保存的文件ID插入到ORD_CONTRACT_SNAPSHOT_DATA，合同快照数据表
				OrdContractSnapshotData ordContractSnapshotData = new OrdContractSnapshotData();
				ordContractSnapshotData.setOrdContractId(ordTravelContract.getOrdContractId());
				ordContractSnapshotData.setJsonFileId(jsonfileId);
				ordContractSnapshotData.setCreateTime(new Date());
				int returnValue = orderContractSnapshotService.saveContractSnapshot(ordContractSnapshotData,operatorName);
				LOG.error("合同快照数据", returnValue);
				if(returnValue<=0){
					LOG.error("合同快照数据", returnValue);
				}
			} catch (Exception e) {
				LOG.error("{}", e);
				resultHandle.setMsg(e);
			}
		}
		return resultHandle;
	}
	/*正式合同修改
     * 自动填充《上海市邮轮旅游合同示范文本》第三条 旅游者保险，关于
     * 甲方________委托乙方办理个人投保的邮轮旅游意外保险(子订单中包含邮轮保险产品则填充同意，否则填充不同意)
     * 甲方________委托乙方办理个人投保的人生意外伤害保险(此条款默认不同意，因为目前没有人生意外伤害保险产品)
     */
	private boolean isAutoUpdateCruiseTourismContract(List<OrdOrderItem> ordOrderItemList ) {
		// 如果子订单中有购买邮轮险产品的，则视为同意委托购买邮轮旅游意外保险
		for (OrdOrderItem ordOrderItem : ordOrderItemList) {
			ResultHandleT<ProdProduct> prodProductHandle = prodProductClientService.findProdProductByIdFromCache(ordOrderItem.getProductId());
			if ( prodProductHandle.isFail() ){
				LOG.error("根据子订单ID调用产品查询接口状态异常 [{}]",prodProductHandle.getMsg());
				return false;
			}
			ProdProduct orderItemProduct = prodProductHandle.getReturnContent();
			if (YOULUN_INSURANCE.equals(orderItemProduct.getProductType())) {
				return true;
			}
		}
		return false;
	}
	//生成并上传合同
	private Long uploadContractPDF(Map<String, Object> rootMap, Configuration configuration, String templateName,
			String fileName) throws Exception {
		return uploadPDF(rootMap, configuration, templateName, fileName);
	}

	//ComFileMap
	private ResultHandleT<ComFileMap> uploadSupplementaryPDF(Map<String, Object> rootMap, Configuration configuration,
			String supplementaryName) {
		ResultHandleT<ComFileMap> handleT = new ResultHandleT<ComFileMap>();
		ComFileMap comFileMap = comFileMapDAO.getByFileName(supplementaryName);
		if (comFileMap == null) {
			try {
				Long fileId = uploadPDF(rootMap, configuration, supplementaryTemplate, supplementaryName);
				if (fileId != null && fileId != 0) {
					comFileMap = new ComFileMap();
					comFileMap.setFileName(supplementaryName);
					comFileMap.setFileId(fileId);
					comFileMap.setCreateTime(new Date());
					
					if (comFileMapDAO.insert(comFileMap) == 1) {
						handleT.setReturnContent(comFileMap);
					} else {
						handleT.setMsg("文件" + supplementaryName + "生成ComFileMap失败。");
					}
				} else {
					handleT.setMsg("文件"+ supplementaryName + "文件上传失败。");
				}
			} catch  (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
				handleT.setMsg("文件" + supplementaryName + "上传失败");
			}
		} else {
			handleT.setReturnContent(comFileMap);
		}
		return handleT;
	}

	/**
	 * 生成并上传
	 */
	private Long uploadPDF(Map<String, Object> rootMap,
			Configuration configuration, String templateName, String fileName) throws Exception {
		//合同
		Template template = configuration.getTemplate(templateName.toString());
		if (template == null) {
			LOG.info("初始化ftl模板失败，模板名称 = " + templateName);
			return null;
		}

		StringWriter sw = new StringWriter();
		template.process(rootMap, sw);
		String htmlString = sw.toString();
		if (htmlString == null) {
			LOG.info("模板HTML生成失败，模板名称 = " + templateName);
			return null;
		}
		
		ByteArrayOutputStream bao = PdfUtil.createPdfFile(htmlString);
		if (bao == null) {
			LOG.info("模板PDF生成失败，模板名称 = " + templateName);
			return null;
		}
		
		byte[] fileBytes = bao.toByteArray();
		bao.close();
		
		//调试时打开
		newContractDebug(fileBytes, fileName);
		
		ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
		
		Long fileId = fsClient.uploadFile(fileName, bai, SERVER_TYPE);
		bai.close();
		return fileId;
	}

	/**
	 *更新合同 根据OrdOrder生成旅游合同，上船至FTP服务器。
	 * 
	 * @param contractVO
	 */
	public ResultHandle updateTravelContact(TravelContractVO contractVO, String operatorName){
		return null;
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
				travelContractVO = buildTravelContractVOData(ordTravelContract,order);
				travelContractVO.setTemplateDirectory("file:///" + directioryFile.getAbsolutePath());//图片单选按钮
				chidOrderMap = findChildOrderList(ordTravelContract,order,true);
			}
			
			LOG.info("CruiseTourismContractServiceImpl.saveTravelContact,fileDir=" + travelContractVO.getTemplateDirectory());
			rootMap.put("travelContractVO", travelContractVO);
			rootMap.put("order", order);
			rootMap.put("chidOrderMap", chidOrderMap);
		}
		return rootMap;
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
		param.setNoProdLineRoute(true);
		param.setLineRoute(true);
		param.setLineShipDetail(true);
		ResultHandleT<ProdProduct> resultHandle=this.prodProductClientService.findLineProductByProductId(productId, param);
		
		ProdProduct prodProduct = resultHandle.getReturnContent();
		
		SuppSupplier suppSupplier = new SuppSupplier();
		if (order != null && prodProduct != null) {
			travelContractVO = new TravelContractVO();
			travelContractVO.setProdProduct(prodProduct);
			travelContractVO.setProductId(productId);
			travelContractVO.setLineRouteId(order.getLineRouteId());
//			travelContractVO.setOrder(order);
			
//			OrdTravelContract ordTravelContract=order.getOrdTravelContract();
			String appendVersion = getAppendVersion(ordTravelContract);
			//合同编号
			String version = DateUtil.formatDate(order.getVisitTime(), "yyyyMMdd") + "-" + order.getOrderId() + "-" + appendVersion;
			travelContractVO.setContractVersion(version);
			//订单编号
			travelContractVO.setOrderId(order.getOrderId().toString());
			
			//甲方（全部游玩人）
			String travellers = null;
			for (OrdPerson ordPerson : order.getOrdPersonList()) {
				if (ordPerson != null && OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equalsIgnoreCase(ordPerson.getPersonType())) {
					if (travellers == null) {
						travellers = ordPerson.getFullName();
					} else {
						travellers = travellers + "," + ordPerson.getFullName();
					}
				}
			}
			travelContractVO.setTravellers(travellers);
			travelContractVO.setFirstTravellerName(travellers);
			travelContractVO.setSecondTravellerName(travellers);
			travelContractVO.setThirdTravellerName(travellers);
			travelContractVO.setFourthTravellerName(travellers);
			
			//出境社（乙方）
			travelContractVO.setFilialeName(filialeNameMap.get(order.getFilialeName()));
			
			//经营许可证编号
			travelContractVO.setPermit(permitlMap.get(order.getFilialeName()));
			
			//经营范围
			travelContractVO.setBusinessScope(businessScopeMap.get(order.getFilialeName()));
			
			//营业地址
			travelContractVO.setAddress(this.businessAddressMap.get(order.getFilialeName()));
			//邮编
			travelContractVO.setLvPostcode(this.businessPostCodeMap.get(order.getFilialeName()));
			
			//适用于o2o分公司，显示总公司的地址
			if(StringUtil.isEmptyString(travelContractVO.getAddress())){
				travelContractVO.setAddress(this.businessAddressMap.get("SH_FILIALE"));
			}
			if(StringUtil.isEmptyString(travelContractVO.getLvPostcode())){
				travelContractVO.setLvPostcode(this.businessPostCodeMap.get("SH_FILIALE"));
			}
			
			//产品名称
			travelContractVO.setProductName(productName);
			
			//团号
			//产品ID+订单出发日期
			travelContractVO.setRegimentNum(productId + "-" + DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd"));
			
			//监督电话
			travelContractVO.setJianduTel(jianduTelMap.get(order.getFilialeName()));
			
			//出发日期
			travelContractVO.setVistDate(DateUtil.getChineseDay(order.getVisitTime()));
			
			//出发地点
			//travelContractVO.setDeparturePlace(prodProduct.getBizDistrict().getDistrictName());
			
			//返回地点
			travelContractVO.setReturnPlace(prodProduct.getPropValue()==null ? null: (prodProduct.getPropValue().get("return_place")==null?null:(String)prodProduct.getPropValue().get("return_place")));
			
			StringBuffer sb = new StringBuffer();
			String lineShipDesc = "/";
			if (CollectionUtils.isNotEmpty(prodProduct.getLineShipDetails())) {
				LineShipDetail lineShipDetail = null;
				for(int i = 0; i < prodProduct.getLineShipDetails().size(); i++) {
					lineShipDetail = prodProduct.getLineShipDetails().get(i);
					if(lineShipDetail.getDistrictId() != null && lineShipDetail.getDistrictId().longValue() != 3260L){
						if(lineShipDetail.getDestination() != null)
							sb.append("-" + lineShipDetail.getDestination());
					}
				}
				if(sb.toString().length() > 0) {
					lineShipDesc = sb.toString().substring(1);
				}
			}
			//邮轮途中停靠港口
			travelContractVO.setLineShipDesc(lineShipDesc);
			
			//共几天  饭店住宿几夜
			Integer routeNights = 0;
			Integer routeDays = 0;
			Map<String,Object> map=null;
			if (CollectionUtils.isNotEmpty(ordPackList)) {
				OrdOrderPack ordOrderPack = ordPackList.get(0);
				map = ordOrderPack.getContentMap();
			}else{
				map = orderContractItem.getContentMap();
			}
			routeDays =  (Integer) map.get(OrderEnum.ORDER_PACK_TYPE.route_days.name());
			routeNights =  (Integer) map.get(OrderEnum.ORDER_PACK_TYPE.route_nights.name());
			ProdLineRouteVO prodLineRouteVO = null;
			if(CollectionUtils.isNotEmpty(prodProduct.getProdLineRouteList())) {
				for(ProdLineRouteVO vo : prodProduct.getProdLineRouteList()) {
					if(order.getLineRouteId().longValue() == vo.getLineRouteId().longValue())
						prodLineRouteVO = vo;
				}
			}
			if (routeDays != null) {
				travelContractVO.setRouteDays(routeDays+"");
			} else {
				if(prodLineRouteVO != null) {
					routeDays  = Integer.parseInt(prodLineRouteVO.getRouteNum() + "");
					travelContractVO.setRouteDays(routeDays+"");
				}
			}
			/*if (routeNights != null) {
				travelContractVO.setRouteNights(routeNights+"");
			} else {
				if(prodLineRouteVO != null) {
					routeNights  = Integer.parseInt(prodLineRouteVO.getStayNum() + "");
					travelContractVO.setRouteNights(routeNights+"");
				}	
			}
			if(order.getCategoryId() == 8){
				travelContractVO.setRouteNights("/");
			}*/
			
			//出境游合同情况下
			//旅游费用合计
			//获取订单中的保险子订单
			List<OrdOrderItem> insuranceOrderItemList = getInsuranceOrdOrderItem(order);
			StringBuffer sb2 = new StringBuffer();
			if (insuranceOrderItemList != null && !insuranceOrderItemList.isEmpty()) {
				//是否有保险
				travelContractVO.setHasInsurance(true);
				OrdOrderItem insuranceOrdItem = insuranceOrderItemList.get(0);
				
				long totalInsurancePrice = getTotalPrice(insuranceOrderItemList);//所有保险的总价
				
				long totalPrice = order.getOughtAmount() - totalInsurancePrice;
				
				//旅游费用总金额
				travelContractVO.setTraveAmount(PriceUtil.trans2YuanStr(totalPrice));
				//保险总金额
				travelContractVO.setInsuranceAmount(PriceUtil.trans2YuanStr(totalInsurancePrice));
				
				//保险公司名称+产品名称
				for(OrdOrderItem item : insuranceOrderItemList){
					sb2.append(item.getProductName()).append("  ");
				}
				travelContractVO.setInsuranceCompanyAndProductName(sb2.toString());//insuranceOrdItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.insurance_company.name())
				
				//是否自动填充同意委托购买邮轮保险
				if( isAutoUpdateCruiseTourismContract(insuranceOrderItemList)){
					travelContractVO.setCruiseInsurance(APPROVE_DELEGATE_INSURANCE);
				}
				
			} else {
				travelContractVO.setInsuranceCompanyAndProductName("/");
				travelContractVO.setInsuranceAmount("/");
				travelContractVO.setTraveAmount(order.getOughtAmountYuan());
			}
			
			travelContractVO.setPayWay("在线支付");
			
			//描述信息、最低成团人数
			setSupplementAndMinPersonCount(travelContractVO, prodProduct,order);
			
			//旅游者代表签字
			OrdPerson traveller = order.getRepresentativePerson();
			if (traveller==null) {
				traveller = new OrdPerson();
			}
			travelContractVO.setFirstTravellerPerson(traveller);
//			travelContractVO.setSignaturePersonName(traveller.getFullName());
			
			//旅行社盖章
			travelContractVO.setStampImage(getStampImageNameByFilialeName(prodProduct.getFiliale()));
			
			ResultHandleT<SuppSupplier> resultHandleSuppSupplier = suppSupplierClientService.findSuppSupplierById(order.getMainOrderItem().getSupplierId());
			if (resultHandleSuppSupplier.isSuccess()) {
				suppSupplier = resultHandleSuppSupplier.getReturnContent();
			}
			//营业地址
			/*if (travelContractVO.getDelegateGroup()) {
				travelContractVO.setLocalTravelAgencyName(suppSupplier.getSupplierName());
				travelContractVO.setLocalTravelAgencyAddress(suppSupplier.getAddress());
			} else {
				travelContractVO.setLocalTravelAgencyName("/");
				travelContractVO.setLocalTravelAgencyAddress("/");
			}*/
			
			//结束日期
			Date beginDate = order.getVisitTime();
			if (routeDays != null) {
				travelContractVO.setOverDate(DateUtil.getChineseDay(DateUtils.addDays(beginDate,routeDays-1)));
			}
			if(order.getCategoryId() == 8){
				List<LineRoute> lineRouteList =  prodProduct.getLineRoutes();
				if(lineRouteList!=null && !lineRouteList.isEmpty()){
					if(lineRouteList.get(0).getDays() != null){
						Integer days  = lineRouteList.get(0).getDays().intValue();
						travelContractVO.setOverDate(DateUtil.getChineseDay(DateUtils.addDays(beginDate,days-1)));
						travelContractVO.setRouteDays(days.toString());
					}
					
				}
				ProdEcontract econtract = prodProduct.getProdEcontract();
				if(econtract!=null && econtract.getMinPerson() != null){
					//最低成团人数
					travelContractVO.setMinPersonCountOfGroup(econtract.getMinPerson().toString());
				}
				
			}
			
			//甲方代表
			travelContractVO.setFirstDelegatePersonName(traveller.getFullName());
			//联系电话
			if(order.getContactPerson() !=null){
				travelContractVO.setContactTelePhoneNo(order.getContactPerson().getMobile());
			}

			//日期
			//甲方签约日期
			travelContractVO.setFirstSignatrueDate(DateUtil.getChineseDay(order.getCreateTime()));
			//签约日期
			travelContractVO.setSignDateStr(DateUtil.getChineseDay(order.getCreateTime()));
			travelContractVO.setFirstSignDateStr(DateUtil.getChineseDay(order.getCreateTime()));
			travelContractVO.setSecondSignDateStr(DateUtil.getChineseDay(order.getCreateTime()));
			travelContractVO.setThirdSignDateStr(DateUtil.getChineseDay(order.getCreateTime()));
			travelContractVO.setFourthSignDateStr(DateUtil.getChineseDay(order.getCreateTime()));
			
			//乙方签约日期
			travelContractVO.setSecondSignatrueDate(DateUtil.getChineseDay(order.getCreateTime()));
			//自愿购物活动补充协议
			//自愿参加另行付费旅游项目补充协议
			fillProdContractDetail(order, travelContractVO, prodProduct);

			//订单创建时间
			travelContractVO.setCreateTime(DateUtil.getChineseDay(order.getCreateTime()));
			//出发时间（和出发日期相同）
			travelContractVO.setVisitTime(DateUtil.getChineseDay(order.getVisitTime()));
			//最后支付时间
			travelContractVO.setPaymentTime((DateUtil.formatDate(order.getPaymentTime(), "yyyy-MM-dd HH:mm")));
			//所有游玩人
			travelContractVO.setOrdTravellerList(order.getOrdTravellerList());
			//所有游玩人人数
			travelContractVO.setTravellersSize(order.getOrdTravellerList().size()+"");
			
			//甲方代表
			travelContractVO.setFullName(traveller.getFullName());
			travelContractVO.setIdNo(traveller.getIdNo());
			travelContractVO.setMobile(traveller.getMobile());
			travelContractVO.setFax(traveller.getFax());
			travelContractVO.setEmail(traveller.getEmail());
			
			//甲方签字日期
			travelContractVO.setSingnDate(DateUtil.getChineseDay(order.getCreateTime()));
			//乙方签字日期
			travelContractVO.setLvSingnDate(DateUtil.getChineseDay(order.getCreateTime()));
			//供应商
			travelContractVO.setSuppSupplier(suppSupplier);
			LOG.info("product is COMMISSIONED_TOUR or SELF_TOUR?"+prodProduct.getProdEcontract().getGroupType());
			//产品是否委托组团
			if(CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.getCode().equalsIgnoreCase(prodProduct.getProdEcontract().getGroupType())) {
				//产品是否委托
				travelContractVO.setProductDelegate(CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.getCode());
				//产品委托方名称
				travelContractVO.setProductDelegateName(prodProduct.getProdEcontract().getGroupSupplierName());
				LOG.info(travelContractVO.getOrderId()+"product is COMMISSIONED_TOUR ");
				if(order.getCategoryId() == 8){
					if(suppSupplier != null && StringUtils.isNotEmpty(suppSupplier.getSupplierName())){
						travelContractVO.setProductDelegateName(suppSupplier.getSupplierName());
					}
				}
			}
			if(CommEnumSet.GROUP_TYPE.SELF_TOUR.getCode().equalsIgnoreCase(prodProduct.getProdEcontract().getGroupType())){
				travelContractVO.setProductDelegate(CommEnumSet.GROUP_TYPE.SELF_TOUR.getCode());
				LOG.info(travelContractVO.getOrderId()+"product is SELF_TOUR");
			}
		}
		// 根据“公司主体”, 差异化信息处理
		super.handleCompanyType(ordTravelContract, order, travelContractVO);
		return travelContractVO;
	}

	/**
	 * 组装合同展示数据
	 * @param order
	 * @param travelContractVO
	 * @return
	 */
	private TravelContractVO buildTravelContractVOUpdateData(TravelContractVO travelContractVO,OrdTravelContract ordTravelContract,OrdOrder order) {
		
//		List<OrdOrderPack>  ordPackList=order.getOrderPackList();
		
		HashMap<String, Object> mapProduct=this.getProductIdAndName(ordTravelContract, order);
		Long productId=(Long)mapProduct.get("productId");
		String productName=(String)mapProduct.get("productName");
//		OrdOrderItem orderContractItem=(OrdOrderItem)mapProduct.get("orderContractItem");
		
		/*修改时保存合同的数据*/
		String saveForUpdateFlag = null;
		if(travelContractVO.getSaveForUpdateFlag() != null){
			saveForUpdateFlag = travelContractVO.getSaveForUpdateFlag();
			productName = travelContractVO.getProductName();
		}
		
		ProdProductParam param = new ProdProductParam();
		param.setProductProp(true);
		param.setProductBranchValue(true);
		param.setProdEcontract(true);
	
		ResultHandleT<ProdProduct> resultHandle=this.prodProductClientService.findLineProductByProductId(productId, param);
		
		ProdProduct prodProduct=resultHandle.getReturnContent();
		
		SuppSupplier suppSupplier = new SuppSupplier();
		if (order != null && prodProduct != null) {
			travelContractVO.setProdProduct(prodProduct);
			travelContractVO.setProductId(productId);
			travelContractVO.setLineRouteId(order.getLineRouteId());

			String appendVersion = getAppendVersion(ordTravelContract);
			//合同编号
			String version = DateUtil.formatDate(order.getVisitTime(), "yyyyMMdd") + "-" + order.getOrderId() + "-" + appendVersion;
			travelContractVO.setContractVersion(version);
			
			//甲方（全部游玩人）
			String travellers = null;
			for (OrdPerson ordPerson : order.getOrdPersonList()) {
				if (ordPerson != null && OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equalsIgnoreCase(ordPerson.getPersonType())) {
					if (travellers == null) {
						travellers = ordPerson.getFullName();
					} else {
						travellers = travellers + "," + ordPerson.getFullName();
					}
				}
			}
			travelContractVO.setTravellers(travellers);
			travelContractVO.setSignDateStr(DateUtil.formatDate(order.getCreateTime(), "yyyy年MM月dd日"));
			//订单编号
			travelContractVO.setOrderId(order.getOrderId().toString());
			
			travelContractVO.setVisitTime(DateUtil.formatDate(order.getVisitTime(), "yyyy年MM月dd日"));
			
			//产品名称
			travelContractVO.setProductName(productName);

			//国内游合同 金额计算
			List<OrdOrderItem> insuranceOrderItemList = getInsuranceOrdOrderItem(order);
			if (insuranceOrderItemList != null && !insuranceOrderItemList.isEmpty()) {
				travelContractVO.setHasInsurance(true);
				OrdOrderItem insuranceOrdItem = insuranceOrderItemList.get(0);
				
				long totalInsurancePrice = getTotalPrice(insuranceOrderItemList);//所有保险的总价
//				long totalPrice = order.getOughtAmount() - totalInsurancePrice;
				
				travelContractVO.setInsuranceAmount(PriceUtil.trans2YuanStr(totalInsurancePrice));
				travelContractVO.setInsuranceCompanyAndProductName(insuranceOrdItem.getProductName());//insuranceOrdItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.insurance_company.name())
			}
			
			//旅行社盖章
			travelContractVO.setStampImage(getStampImageNameByFilialeName(prodProduct.getFiliale()));
			
			ResultHandleT<SuppSupplier> resultHandleSuppSupplier =suppSupplierClientService.findSuppSupplierById(order.getMainOrderItem().getSupplierId());
			if (resultHandleSuppSupplier.isSuccess()) {
				suppSupplier = resultHandleSuppSupplier.getReturnContent();
			}
			
			travelContractVO.setCreateTime(DateUtil.getChineseDay(order.getCreateTime()));
			travelContractVO.setOrdTravellerList(order.getOrdTravellerList());
			travelContractVO.setContactTelePhoneNo(order.getContactPerson().getMobile());
			
			travelContractVO.setContractMobile(order.getContactPerson().getMobile());
			LOG.info("product is COMMISSIONED_TOUR or SELF_TOUR?"+prodProduct.getProdEcontract().getGroupType());
			//产品是否委托组团
			if(CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.getCode().equalsIgnoreCase(prodProduct.getProdEcontract().getGroupType()))
			{
				travelContractVO.setProductDelegate(CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.getCode());
				travelContractVO.setProductDelegateName(prodProduct.getProdEcontract().getGroupSupplierName());
				LOG.info(travelContractVO.getOrderId()+"product is COMMISSIONED_TOUR ");
				if(order.getCategoryId() == 8){
					if(suppSupplier != null && StringUtils.isNotEmpty(suppSupplier.getSupplierName())){
						travelContractVO.setProductDelegateName(suppSupplier.getSupplierName());
					}
				}
			}
			if(CommEnumSet.GROUP_TYPE.SELF_TOUR.getCode().equalsIgnoreCase(prodProduct.getProdEcontract().getGroupType())){
				travelContractVO.setProductDelegate(CommEnumSet.GROUP_TYPE.SELF_TOUR.getCode());
				LOG.info(travelContractVO.getOrderId()+"product is SELF_TOUR");
			}
			//自愿购物活动补充协议 自愿参加另行付费旅游项目补充协议
			fillProdContractDetail(order, travelContractVO, prodProduct);
		}
		
		travelContractVO.setSuppSupplier(suppSupplier);
		
		/*修改时保存合同的数据,重新设置*/
		if(saveForUpdateFlag != null){
			travelContractVO.setProductName(productName);
		}
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
		return contractTemplateHtml(contractTemplate.toString());
	}

	@Override
	public ResultHandle updateTravelContact(TravelContractVO travelContractVO,
			OrdOrder order, OrdTravelContract ordTravelContract,
			String operatorName) {
		ResultHandle resultHandle = new ResultHandle();
		File directioryFile = initDirectory();
		travelContractVO=this.buildTravelContractVOUpdateData(travelContractVO, ordTravelContract, order);
		if(travelContractVO != null) {
			travelContractVO.setTemplateDirectory("file:///" + directioryFile.getAbsolutePath());//图片单选按钮
		}
		
		//合同中应有字段丢失，发送预警邮件
		resultHandle = checkUpdateTravelContractData(travelContractVO,order);
		if(resultHandle.isFail()){
			LOG.info("---------------合同中应有字段丢失，发送预警邮件-------------");
			return resultHandle;
		}
		//end
		
		//行程单无效，发送预警邮件
		resultHandle = checkprodLineRouteVOList(travelContractVO,order,travelContractVO.getProdProduct());
		if(resultHandle.isFail()){
			LOG.info("---------------行程单无效，发送预警邮件-------------");
			return resultHandle;
		}
		//end
		
		Map<String,Object> rootMap = new HashMap<String, Object>();
		rootMap.put("travelContractVO", travelContractVO);
		Map<String,List<OrderMonitorRst>>  chidOrderMap=findChildOrderList(ordTravelContract,order,true);
		rootMap.put("chidOrderMap", chidOrderMap);	
		
		
		if (directioryFile == null || !directioryFile.exists()) {
			resultHandle.setMsg("合同模板目录不存在。");
			return resultHandle;
		}
		
		try {
			Configuration configuration = initConfiguration(directioryFile);

			if (configuration == null) {
				resultHandle.setMsg("初始化freemarker失败。");
				return resultHandle;
			}

			Template template = configuration.getTemplate(contractTemplate.toString());
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

			String fileName = "CruiseTourismContract_"+ travelContractVO.getContractVersion() + ".pdf";

			// 调试时打开自动本地成文件
			updateContractDubg(fileBytes, fileName);

			ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
			Long fileId = fsClient.uploadFile(fileName, bai, SERVER_TYPE);
			bai.close();

			//补充条款名称（唯一）
			String supplementaryName = "CruiseTourismSupplementary_" + travelContractVO.getContractVersion() + ".pdf";
			
			if (fileId != null && fileId != 0) {
				ResultHandleT<ComFileMap> handleA = null;
				handleA = uploadSupplementaryPDF(rootMap, configuration, supplementaryName);
				if (handleA.isFail()) {
					resultHandle.setMsg(handleA.getMsg());
					return resultHandle;
				}

				ordTravelContract.setVersion(travelContractVO
						.getContractVersion());
				ordTravelContract.setFileId(fileId);

				// 合同签约状态逻辑
				setOrdContractStatus(ordTravelContract, order, false);

				ordTravelContract.setContractName(contractName.toString());
				// ordTravelContract.setSigningType(OrderEnum.ORDER_CONTRACT_SIGNING_TYPE.ONLINE.name());

				//String attachementURLs = fileNameA.toString();
				if(handleA.getReturnContent() != null) {
					ordTravelContract.setAttachementUrl(supplementaryName);
				}
				ordTravelContract.setCreateTime(new Date());
				if (ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, operatorName) <= 0) {
					ordTravelContractService.saveOrdTravelContract(ordTravelContract, operatorName);
				}
				
				//更新修改状态，用于金棕榈同步
				if(ordTravelContract.getOrdContractId() != null) {
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("ordContractId", ordTravelContract.getOrdContractId());
					//将同步状态更新为已修改未提交
					params.put("syncStatus", OrderEnum.ORDER_TRAVEL_CONTRACT_SYNC_STATUS.MODIFIED_UNSUBMITTED);
					ordTravelContractService.updatePushDataByContractId(params);
				}
			} else {
				resultHandle.setMsg("合同上传失败。");
			}

			// 行程单生成
			this.saveTravelItineraryContract(ordTravelContract, operatorName);

			String content = contractName + "修改成功";
			
			this.insertOrderLog(ordTravelContract.getOrderId(),
					ordTravelContract.getOrdContractId(), operatorName,
					content, null);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			resultHandle.setMsg(e);
		}
		
		return resultHandle;
	}

	@Override
	public ResultHandleT<String> getContractTemplateHtml(Long productId) {
		return contractTemplateHtml(contractTemplate, productId);
	}
	
}
