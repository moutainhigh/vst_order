package com.lvmama.vst.order.contract.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.biz.po.BizDest;
import com.lvmama.vst.back.biz.po.BizDest.DEST_TYPE;
import com.lvmama.vst.back.biz.po.BizDict;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.biz.service.DestClientService;
import com.lvmama.vst.back.client.biz.service.DictClientService;
import com.lvmama.vst.back.client.prod.curise.service.ProdCuriseProductClientService;
import com.lvmama.vst.back.client.prod.service.LineRouteClientService;
import com.lvmama.vst.back.client.prod.service.ProdLineRouteClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.dujia.comm.route.detail.utils.BuildRouteTemplateUtil;
import com.lvmama.vst.back.dujia.comm.route.detail.utils.RouteDetailFormat;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.line.po.LineRoute;
import com.lvmama.vst.back.line.po.LineRouteDetail;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.prod.po.LineRouteEnum;
import com.lvmama.vst.back.prod.po.ProdDestRe;
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.back.prod.po.ProdLineRouteDetail;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductProp;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.front.ProductPreorderUtil;
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
import com.lvmama.vst.order.snapshot.service.IOrderLinerouteSnapshotService;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 
 * @author zhangwei
 *
 */
@Service("travelItineraryContractService")
public class TravelItineraryContractServiceImpl extends AbstractOrderTravelElectricContactService implements IOrderElectricContactService {
	
	private static final Log LOG = LogFactory.getLog(TravelItineraryContractServiceImpl.class);

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
	private DictClientService dictClientService;
	
	@Autowired
	private ProdLineRouteClientService prodLineRouteClientService;
	
	@Autowired
	private LineRouteClientService lineRouteClientService;
	
	@Autowired
	private IOrdPersonService ordPersonService;
	
	@Autowired
	private IOrdItemPersonRelationService ordItemPersonRelationService;
	
	@Autowired
	private DestClientService destClientRemote;
	
	@Autowired
	private IOrderLinerouteSnapshotService orderLinerouteSnapshotService;
	
	
	private static final String SERVER_TYPE = "COM_AFFIX";
	
	private static final String contractName = "行程单";
	private static final String templateName = "travelItineraryTemplate.ftl";
	
	private static final Long TAI_WAN_ID =401L;
	
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
				
				
				String fileName = "travelItineraryTemplate.pdf";
				
				//调试时打开
				this.newContractDebug(fileBytes, fileName);
				
				
				boolean isCreateOrder=false;
				if (ordTravelContract.getAdditionFileId()==null) {
					isCreateOrder=true;
				}
				
				ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
				Long fileId = fsClient.uploadFile(fileName, bai, SERVER_TYPE);
				bai.close();
				
				if (fileId != null && fileId != 0) {
					
					ordTravelContract.setAdditionFileId(fileId+"");
					ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, operatorName);
							
					
				} else {
					resultHandle.setMsg("行程单上传失败。");
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
			
			rootMap.put("routeDetailFormat", new RouteDetailFormat());

			
		}
		
		
		
		
		return rootMap;
	}
	
	
	public Map<String,Object> captureContract(OrdTravelContract ordTravelContract,OrdOrder order) {
		
		Map<String,Object> rootMap = new HashMap<String, Object>();
		
		if (order != null) {
			
			TravelContractVO travelContractVO = buildTravelContractVOData(ordTravelContract,order);
			
			//关联销售当地游，替换相关合同内容信息
			replaceTravelContractVOData(ordTravelContract,order,travelContractVO);
			
//			travelContractVO.setTemplateDirectory("file:///" + directioryFile.getAbsolutePath());//图片单选按钮
//			LOG.info("advanceProductAgreementContractServiceImpl.saveTravelContact,fileDir=" + travelContractVO.getTemplateDirectory());
			rootMap.put("travelContractVO", travelContractVO);
			
			rootMap.put("order", order);
			
			Map<String,List<OrderMonitorRst>>  chidOrderMap=findChildOrderList(ordTravelContract,order,false);
			
			rootMap.put("chidOrderMap", chidOrderMap);
			
			rootMap.put("routeDetailFormat", new RouteDetailFormat());

			
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
					
					
					//住宿
					List<BizDict> bizDictList = dictClientService.findDictListByDefId(515L).getReturnContent();
					HashMap<String ,String> stayTypeMap=new HashMap<String, String>();
					
					for (BizDict bizDict : bizDictList) {
						
						stayTypeMap.put(bizDict.getDictId()+"", bizDict.getDictName());
						
					}
					
					//行程
					Long productId = orderContractItem.getProductId();
					List<ProdLineRouteDetail> prodLineRouteDetailList = null;
					if(productId != null) {
						ResultHandleT<List<ProdLineRoute>> lineRouteResults = null;
						try {
							lineRouteResults = this.prodLineRouteClientService.findProdLineRouteByParam(productId);
							
							if(lineRouteResults != null && CollectionUtils.isNotEmpty(lineRouteResults.getReturnContent())) {
								ProdProductParam param = new ProdProductParam();
								ResultHandleT<ProdProduct> resultHandle=this.prodProductClientService.findLineProductByProductId(productId, param);
								ProdProduct prodProduct=resultHandle.getReturnContent();
								//如果是当地游或者跟团游 且不是目的地BU 则使用新行程结构，否则走老流程
								if( prodProduct!=null && 
										ProductPreorderUtil.isNewRoute(prodProduct.getBizCategoryId(), prodProduct.getSubCategoryId())&&
				            				!ProductPreorderUtil.isDestinationBUDetail(prodProduct)){
									ProdLineRoute lineRouteTemp = lineRouteResults.getReturnContent().get(0);
									
									//行程使用新结构
									ProdLineRoute lineRouteResult = null;
									ResultHandleT<List<ProdLineRoute>> newLineRouteResults = new ResultHandleT<List<ProdLineRoute>>();
									newLineRouteResults = this.prodLineRouteClientService.findCacheLineRouteListByProductId(prodProduct.getProductId());
									if(CollectionUtils.isNotEmpty(newLineRouteResults.getReturnContent())){
										for (ProdLineRoute lineRoute : newLineRouteResults.getReturnContent()) {
											if(lineRouteTemp.getLineRouteId().equals(lineRoute.getLineRouteId())){
												lineRouteResult = lineRoute;
											}
										}
									}
									ProdLineRoute lineRoute = BuildRouteTemplateUtil.buildTemplate(lineRouteResult);
									travelContractVO.setLineRoute(lineRoute);
									travelContractVO.setIsNewRoute("Y");
								}else{
									ProdLineRoute lineRouteTemp = lineRouteResults.getReturnContent().get(0);
									
									Map<String, Object> params = new HashMap<String, Object>();
									params.put("lineRouteId", lineRouteTemp.getLineRouteId());
									lineRouteResults = this.prodLineRouteClientService.findProdLineRouteAllList(params, true);
									
									ProdLineRoute lineRoute = lineRouteResults.getReturnContent().get(0);
									travelContractVO.setLineRoute(lineRoute);
									prodLineRouteDetailList = lineRoute.getProdLineRouteDetailList();
									travelContractVO.setIsNewRoute("N");
								}
							}
						} catch (Exception e) {
							LOG.error("找不到对应的行程，行程ID：" + order.getLineRouteId()+ "，异常信息： {}", e);
						}
					}
					if(CollectionUtils.isNotEmpty(prodLineRouteDetailList)) {
						for (ProdLineRouteDetail prodLineRouteDetail : prodLineRouteDetailList) {
							prodLineRouteDetail.setStayType(stayTypeMap.get(prodLineRouteDetail.getStayType()));
							String trafficType=prodLineRouteDetail.getTrafficType();
							String[] trafficTypeArray=null;
							if (StringUtils.isNotEmpty(trafficType)) {
								trafficTypeArray=trafficType.split(",");
								String[] codeArray=new String[trafficTypeArray.length];
								String codeValue="";
								for (int i = 0; i < trafficTypeArray.length; i++) {
									String code = trafficTypeArray[i];
									codeArray[i]=LineRouteEnum.TRAFFIC_TYPE.BARS.getCnName(code);
									
									if (i>0) {
										codeValue+=",";
									}
									
									codeValue+=codeArray[i];
								}
								prodLineRouteDetail.setTrafficType(codeValue);
							}
						}
					}
					
					//供应商
					SuppSupplier suppSupplier = new SuppSupplier();
					ResultHandleT<SuppSupplier> resultHandleSuppSupplier =suppSupplierClientService.findSuppSupplierById(orderContractItem.getSupplierId());
					if (resultHandleSuppSupplier.isSuccess()) {
						suppSupplier = resultHandleSuppSupplier.getReturnContent();
					}
					travelContractVO.setSuppSupplier(suppSupplier);
					
					if (CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.name().equalsIgnoreCase(travelContractVO.getProdProduct().getProdEcontract().getGroupType())) {
						travelContractVO.setDelegateGroup(true);

						if (suppSupplier != null) {
							travelContractVO.setDelegateGroupName(suppSupplier.getSupplierName());
						}
					} else {
						travelContractVO.setDelegateGroup(false);
						travelContractVO.setDelegateGroupName("");
					}

			}else{

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
		
		HashMap<String, Object> mapProduct=this.getProductIdAndName(ordTravelContract, order);
		Long productId=(Long)mapProduct.get("productId");
		String productName=(String)mapProduct.get("productName");
		
		
		TravelContractVO travelContractVO  = new TravelContractVO();
		
		ProdProductParam param = new ProdProductParam();
		param.setProductProp(true);
		param.setProductBranchValue(true);
		param.setProdEcontract(true);
		param.setLineRoute(true);
		param.setDest(true);
		
		ResultHandleT<ProdProduct> resultHandle=this.prodProductClientService.findLineProductByProductId(productId, param);
		
		ProdProduct prodProduct=resultHandle.getReturnContent();
		
		//产品名称
		travelContractVO.setProductName(productName);
		
		String packedProductId = "";//被打包产品ID
		String autoPackTrafficCode = "";//自动打包交通
		String isusePackedRouteDetailsCode = "";//是否使用被打包产品行程明细
		List<ProdProductProp> productPropList = prodProduct.getProdProductPropList();
		if(productPropList != null && productPropList.size() > 0){
			for(ProdProductProp prop : productPropList){
				if (prop != null && prop.getBizCategoryProp() != null && StringUtils.isNotEmpty(prop.getBizCategoryProp().getPropCode())) {
					if (prop.getBizCategoryProp().getPropCode().equals("packed_product_id")) {
						packedProductId = prop.getPropValue();
					}
					if (prop.getBizCategoryProp().getPropCode().equals("auto_pack_traffic")) {
						autoPackTrafficCode = prop.getPropValue();
					}
					if (prop.getBizCategoryProp().getPropCode().equals("isuse_packed_route_details")) {
						isusePackedRouteDetailsCode = prop.getPropValue();
					}
				}
			}
		}
		
		//自动打包交通 && 使用被打包产品行程明细
		if("Y".equals(autoPackTrafficCode) && "Y".equals(isusePackedRouteDetailsCode)){
			resultHandle=this.prodProductClientService.findLineProductByProductId(Long.parseLong(packedProductId), param);
			prodProduct=resultHandle.getReturnContent();
			//产品名称
			travelContractVO.setProductName(prodProduct.getProductName());
		}
		
		
		SuppSupplier suppSupplier = new SuppSupplier();
		if (order != null && prodProduct != null) {
			travelContractVO.setProdProduct(prodProduct);
			travelContractVO.setProductId(productId);
			travelContractVO.setContractVersion(ordTravelContract.getVersion());
			
			//订单编号
			travelContractVO.setOrderId(order.getOrderId().toString());
			
			//判断是否是台湾的 
			if(CollectionUtils.isNotEmpty(prodProduct.getProdDestReList())){
				Map<String,Object> prodDestParams;
				for(ProdDestRe prodDestRe:prodProduct.getProdDestReList()){
					if(prodDestRe.getDestId()!=null){
						prodDestParams = new HashMap<String, Object>();
						prodDestParams.put("destId", prodDestRe.getDestId());
						prodDestParams.put("destType", DEST_TYPE.PROVINCE.name());
						ResultHandleT<List<BizDest>> bizDestListH  = destClientRemote.getFullPathDestByParams(prodDestParams);
						if(bizDestListH!=null && bizDestListH.isSuccess() ){
							for(BizDest bizDest:bizDestListH.getReturnContent()){
								if(TAI_WAN_ID.equals(bizDest.getDestId())){
									travelContractVO.setTaiwanFlag("Y");
								}
							}
						}
					}
				}
			}
			Long supplierId=0L;
			if (order.getOrderItemList().size()>1) {
				supplierId=order.getMainOrderItem().getSupplierId();
			}else{
				supplierId=order.getOrderItemList().get(0).getSupplierId();
			}
			ResultHandleT<SuppSupplier> resultHandleSuppSupplier =suppSupplierClientService.findSuppSupplierById(supplierId);
			if (resultHandleSuppSupplier.isSuccess()) {
				suppSupplier = resultHandleSuppSupplier.getReturnContent();
			}
			
			//出境社
			LOG.info("order_id:"+order.getOrderId()
					+"group_type:"+prodProduct.getProdEcontract().getGroupType()
					+"taiwan_flag:"+travelContractVO.getTaiwanFlag()
					+"category_id:"+order.getCategoryId()
					+"filale_name:"+this.filialeNameMap.get(order.getFilialeName()));
			if (order.getOrdOrderPack()!=null) {
				if ("false".equals(order.getOrdOrderPack().getOwnPack())){//1）供应商打包的线路产品，抓取供应商；

					travelContractVO.setFilialeName(suppSupplier.getSupplierName());
					
				}else{//（2）自主打包的线路产品，不抓取供应商
					travelContractVO.setFilialeName(this.filialeNameMap.get(order.getFilialeName()));
				}
			}else{
				if(CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.getCode().equalsIgnoreCase(prodProduct.getProdEcontract().getGroupType())
						&& (!"Y".equalsIgnoreCase(travelContractVO.getTaiwanFlag())
							|| !ProdProduct.PRODUCTTYPE.FOREIGNLINE.name().equalsIgnoreCase(prodProduct.getProductType()))
						&& order.getCategoryId().longValue() == 16l){
					travelContractVO.setFilialeName(this.filialeNameMap.get(order.getFilialeName()));
				}else{
					travelContractVO.setFilialeName(suppSupplier.getSupplierName());
				}

			}
			
			//住宿
			List<BizDict> bizDictList = dictClientService.findDictListByDefId(515L).getReturnContent();
			HashMap<String ,String> stayTypeMap=new HashMap<String, String>();
			
			for (BizDict bizDict : bizDictList) {
				
				stayTypeMap.put(bizDict.getDictId()+"", bizDict.getDictName());
				
			}
			
			//邮轮
			if(order.getCategoryId() == 8L) {
				LOG.info("生成邮轮行程单开始, categorId = "+order.getCategoryId());
				ResultHandleT<List<LineRoute>> resultHandleT = null;
				List<LineRouteDetail> lineRouteDetailList = null;
				try {
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("productId", prodProduct.getProductId());
					resultHandleT = lineRouteClientService.findLineRouteList(params);
				} catch (Exception e) {
					LOG.error("找不到对应的行程，产品ID：" + order.getProductId()+ "，异常信息： {}", e);
				}
				if(resultHandleT != null && CollectionUtils.isNotEmpty(resultHandleT.getReturnContent())) {
					LineRoute lineRoute = resultHandleT.getReturnContent().get(0);
					travelContractVO.setShipLineRoute(lineRoute);
					lineRouteDetailList = lineRoute.getLineRouteDetails();
				}
				if(CollectionUtils.isNotEmpty(lineRouteDetailList)) {
					for (LineRouteDetail prodLineRouteDetail : lineRouteDetailList) {
						//交通工具（以逗号分隔）
						String trafficType = prodLineRouteDetail.getTrafficTool();
						String[] trafficTypeArray = null;
						if (StringUtils.isNotEmpty(trafficType)) {
							trafficTypeArray = trafficType.split(",");
							String[] codeArray=new String[trafficTypeArray.length];
							String codeValue="";
							for (int i = 0; i < trafficTypeArray.length; i++) {
								String code = trafficTypeArray[i];
								//交通
								codeArray[i] = CommEnumSet.TRAFFIC_TOOL.getCnName(code.toUpperCase());
								
								if (i > 0) {
									codeValue+=",";
								}
								codeValue+=codeArray[i];
							}
							prodLineRouteDetail.setTrafficTool(codeValue);
						}
					}
				}
				LOG.info("生成邮轮行程单结束");
			} else {
				
				List<ProdLineRouteDetail> prodLineRouteDetailList = null;
				if(order.getLineRouteId() != null) {
					
					//1、	判断当前下单产品是否属于跟团游长线或者属于长线当地游
					//2、	若属于，根据orderId查询mongodb中是否存在当前订单的行程快照
					//3、	若存在，将行程信息set到travelContractVO中，并且设置当前行程的IsNewRoute状态
					//4、	若不存在，执行原有查询行程信息逻辑，在结束时将行程信息保存到mongodb中
					boolean fromMongo = false;
					if ((BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId()) || 
							BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getCategoryId()))
							&& ProdProduct.PRODUCTTYPE.INNERLONGLINE.getCode().equals(prodProduct.getProductType())) {
						ProdLineRoute lineRouteResult = orderLinerouteSnapshotService.findOneLineRouteSnapShotByOrderId(order.getOrderId());
						if (lineRouteResult != null) {
							ProdLineRoute lineRoute = BuildRouteTemplateUtil.buildTemplate(lineRouteResult);
							travelContractVO.setLineRoute(lineRoute);
							fromMongo = true;
							if( ProductPreorderUtil.isNewRoute(prodProduct.getBizCategoryId(), prodProduct.getSubCategoryId())&&
			            			!ProductPreorderUtil.isDestinationBUDetail(prodProduct)){
								travelContractVO.setIsNewRoute("Y");
							}else {
								travelContractVO.setIsNewRoute("N");
							}
						}
					}
					
					//如果是当地游或者跟团游 且不是目的地BU 则使用新行程结构，否则走老流程
					if (!fromMongo) {
						if( ProductPreorderUtil.isNewRoute(prodProduct.getBizCategoryId(), prodProduct.getSubCategoryId())&&
								!ProductPreorderUtil.isDestinationBUDetail(prodProduct)){
							try {
								ProdLineRoute lineRouteResult = null;
								
								ResultHandleT<List<ProdLineRoute>> newLineRouteResults = new ResultHandleT<List<ProdLineRoute>>();
								newLineRouteResults = this.prodLineRouteClientService.findCacheLineRouteListByProductId(prodProduct.getProductId());
								if(CollectionUtils.isNotEmpty(newLineRouteResults.getReturnContent())){
									for (ProdLineRoute lineRoute : newLineRouteResults.getReturnContent()) {
										if ("Y".equals(autoPackTrafficCode) && "Y".equals(isusePackedRouteDetailsCode)) {//自动打包交通的产品，取被
											if(lineRoute.getLineRouteId() != null){
												lineRouteResult = lineRoute;
												break;
											}
										} else {//非自动打包交通的产品，取订单上的行程
											if (order.getLineRouteId().equals(lineRoute.getLineRouteId())) {
												lineRouteResult = lineRoute;
											}
										}
									}
								}
								if(lineRouteResult!=null){
									ProdLineRoute lineRoute = BuildRouteTemplateUtil.buildTemplate(lineRouteResult);
									travelContractVO.setLineRoute(lineRoute);
								}
								travelContractVO.setIsNewRoute("Y");
							} catch (Exception e) {
								LOG.error("找不到对应的行程，行程ID：" + order.getLineRouteId()+ "，异常信息： {}", e);
							}
						}else{
							ResultHandleT<List<ProdLineRoute>> lineRouteResults = null;
							try {
								Map<String, Object> params = new HashMap<String, Object>();
								params.put("lineRouteId", order.getLineRouteId());
								lineRouteResults = this.prodLineRouteClientService.findProdLineRouteAllList(params, true);
							} catch (Exception e) {
								LOG.error("找不到对应的行程，行程ID：" + order.getLineRouteId()+ "，异常信息： {}", e);
							}
							if(lineRouteResults != null && CollectionUtils.isNotEmpty(lineRouteResults.getReturnContent())) {
								ProdLineRoute lineRoute = lineRouteResults.getReturnContent().get(0);
								travelContractVO.setLineRoute(lineRoute);
								prodLineRouteDetailList = lineRoute.getProdLineRouteDetailList();
							}
							travelContractVO.setIsNewRoute("N");
						}
						//保存到mongo
						if ((BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId()) || 
								BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getCategoryId()))
								&& ProdProduct.PRODUCTTYPE.INNERLONGLINE.getCode().equals(prodProduct.getProductType())
								&& travelContractVO.getLineRoute() != null) {
							orderLinerouteSnapshotService.insertOneLineRouteSnapshot(order.getOrderId(), travelContractVO.getLineRoute());
						}
					}
				}
				
				if(CollectionUtils.isNotEmpty(prodLineRouteDetailList)) {
					for (ProdLineRouteDetail prodLineRouteDetail : prodLineRouteDetailList) {
						prodLineRouteDetail.setStayType(stayTypeMap.get(prodLineRouteDetail.getStayType()));
						String trafficType=prodLineRouteDetail.getTrafficType();
						if (StringUtils.isNotEmpty(trafficType)) {
							trafficType = trafficType.replace("CRUISE,", "");
							trafficType = trafficType.replace(",CRUISE", "");
							trafficType = trafficType.replace("AIRCRAFT", "PLANE");
							trafficType = trafficType.replace("CAR", "BARS");
							String[] trafficTypeArray = trafficType.split(",");
							String codeValue = "";
							boolean plane = false;
							boolean bars = false;
							for(String t : trafficTypeArray) {
								if("PLANE".equalsIgnoreCase(t)) {
									if(plane) continue;
									if(!plane) {
										plane = true;
										codeValue += LineRouteEnum.TRAFFIC_TYPE.getCnName(t) + ",";
										continue;
									}
								}
								if("BARS".equalsIgnoreCase(t)) {
									if(bars) continue;
									if(!bars) {
										bars = true;
										codeValue += LineRouteEnum.TRAFFIC_TYPE.getCnName(t) + ",";
										continue;
									}
								}
								codeValue += LineRouteEnum.TRAFFIC_TYPE.getCnName(t) + ",";
							}
							
							if(codeValue.length() > 0)
								codeValue = codeValue.substring(0, codeValue.length() - 1);
							prodLineRouteDetail.setTrafficType(codeValue);
						}
					}
				}
			}
			
			
			List<BizDict> hotelStarList=dictClientService.findDictListByDefId(515L).getReturnContent();;
			travelContractVO.setHotelStarList(hotelStarList);
			//供应商
			travelContractVO.setSuppSupplier(suppSupplier);
			LOG.info("product is COMMISSIONED_TOUR or SELF_TOUR?"+prodProduct.getProdEcontract().getGroupType());
			//产品是否委托组团
			if(CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.getCode().equalsIgnoreCase(prodProduct.getProdEcontract().getGroupType())) {
				travelContractVO.setProductDelegate(CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.getCode());
				travelContractVO.setProductDelegateName(prodProduct.getProdEcontract().getGroupSupplierName());
				LOG.info(travelContractVO.getOrderId()+"product is COMMISSIONED_TOUR ");
				if(order.getCategoryId() == 8){
					if(suppSupplier != null && StringUtils.isNotEmpty(suppSupplier.getSupplierName())){
						travelContractVO.setProductDelegateName(suppSupplier.getSupplierName());
					}
				}
			}
			if(CommEnumSet.GROUP_TYPE.SELF_TOUR.getCode().equalsIgnoreCase(prodProduct.getProdEcontract().getGroupType()))
			{
				travelContractVO.setProductDelegate(CommEnumSet.GROUP_TYPE.SELF_TOUR.getCode());
				LOG.info(travelContractVO.getOrderId()+"product is SELF_TOUR");
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
		return null;
	}
	
	@Override
	public ResultHandleT<String> getContractTemplateHtml() {
		return contractTemplateHtml(templateName.toString());
	}


	@Override
	public ResultHandle updateTravelContact(TravelContractVO travelContractVO,
			OrdOrder order, OrdTravelContract ordTravelContract,
			String operatorName) {
		return null;
	}
	
	public static void main(String[] args) {

		//AIRCRAFT
		//CAR
		//CRUISE
		
		//TRAIN,PLANE,BARS
		String trafficType = "TRAIN,AIRCRAFT,CAR,PLANE,BARS,CRUISE";
		trafficType = trafficType.replace("CRUISE,", "");
		trafficType = trafficType.replace(",CRUISE", "");
		trafficType = trafficType.replace("AIRCRAFT", "PLANE");
		trafficType = trafficType.replace("CAR", "BARS");
		String[] trafficTypeArray = trafficType.split(",");
		String trafficStr = "";
		boolean plane = false;
		boolean bars = false;
		for(String t : trafficTypeArray) {
			if("PLANE".equalsIgnoreCase(t)) {
				if(plane) continue;
				if(!plane) {
					plane = true;
					trafficStr += LineRouteEnum.TRAFFIC_TYPE.getCnName(t) + ",";
					continue;
				}
			}
			if("BARS".equalsIgnoreCase(t)) {
				if(bars) continue;
				if(!bars) {
					bars = true;
					trafficStr += LineRouteEnum.TRAFFIC_TYPE.getCnName(t) + ",";
					continue;
				}
			}
			trafficStr += LineRouteEnum.TRAFFIC_TYPE.getCnName(t) + ",";
		}
		
		if(trafficStr.length() > 0)
			System.out.println(trafficStr.substring(0, trafficStr.length() - 1));
		/*int indexOf = trafficType.indexOf("AIRCRAFT");
		int indexOf2 = trafficType.indexOf("PLANE");
		
		int indexOf = trafficType.indexOf("CAR");
		int indexOf = trafficType.indexOf("BARS");*/
		
		//LineRouteEnum.TRAFFIC_TYPE.getCnName(code.toUpperCase())
		
	}
	
	
}
