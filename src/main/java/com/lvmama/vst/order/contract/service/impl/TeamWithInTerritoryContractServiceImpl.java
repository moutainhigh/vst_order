package com.lvmama.vst.order.contract.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.lvmama.comm.user.utils.StringUtil;
import com.lvmama.commons.logging.LvmamaLog;
import com.lvmama.commons.logging.LvmamaLogFactory;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.prod.curise.service.ProdCuriseProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.order.po.OrdContractSnapshotData;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PRICE_RATE_TYPE;
import com.lvmama.vst.back.prod.po.ProdContractDetail;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductProp;
import com.lvmama.vst.back.prod.vo.ProdLineRouteVO;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comlog.LvmmLogEnum;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.po.ComFileMap;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.pdf.PdfUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.econtract.TravelContractVO;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.order.contract.service.IOrderContractSnapshotService;
import com.lvmama.vst.order.contract.service.IOrderElectricService;
import com.lvmama.vst.order.contract.vo.OutboundTourContractVO;
import com.lvmama.vst.order.contract.vo.TeamWithInTerritoryContractDataVO;
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
@Service("teamWithInTerritoryContractService")
public class TeamWithInTerritoryContractServiceImpl extends AbstractOrderTravelElectricContactService implements IOrderElectricService {


	
	private static final Logger LOG = LoggerFactory.getLogger(TeamWithInTerritoryContractServiceImpl.class);
	private static final LvmamaLog lvmamaLog = LvmamaLogFactory.getLog(TeamWithInTerritoryContractServiceImpl.class);

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
	private IOrderContractSnapshotService orderContractSnapshotService;
	
	private static final int MAX_COUNT_OF_PDF_LINE = 50;
	
	private static final String SERVER_TYPE = "COM_AFFIX";
	
	private static final String contractName = "团队境内旅游合同";
	private static final String templateName = "teamWithInTerritoryContractTemplate.ftl";
	private static final String fileNameA = "supplementary_safety_notice_territory.pdf";


	@Override
	public ResultHandle saveTravelContact(OrdTravelContract ordTravelContract, String operatorName) {
		ResultHandle resultHandle = new ResultHandle();
		
		if (ordTravelContract != null) {
			LOG.info("---------------开始生成团队境内旅游合同orderId:" + ordTravelContract.getOrderId() + "-------------");
			
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
				
				//合同中应有字段丢失，发送预警邮件
				resultHandle = checkSaveTravelContractData(travelContractVO,order);
				if(resultHandle.isFail()){
					LOG.info("---------------生成合同时应有字段丢失，发送预警邮件-------------");
					return resultHandle;
				}
				//end
				
				//行程单无效，发送预警邮件
				resultHandle = checkprodLineRouteVOList(travelContractVO,order,travelContractVO.getProdProduct());
				if(resultHandle.isFail()){
					LOG.info("---------------生成合同时行程单无效，发送预警邮件-------------");
					return resultHandle;
				}
				//end
				
				Configuration configuration = initConfiguration(directioryFile);
				if (configuration == null) {
					resultHandle.setMsg("初始化freemarker失败。");
					return resultHandle;
				}
				

				
//				StringBuilder contractName = new StringBuilder();
//				StringBuilder templateName = new StringBuilder();
//				StringBuilder fileNameA =  new StringBuilder();
//				StringBuilder fileNameB =  new StringBuilder();
				
//				if (!findTravelEcontractTemplate(directioryFile, contractName, templateName)) {
//					resultHandle.setMsg("目录下不存在合同模板。");
//					return resultHandle;
//				}
//				
//				if (!findTravelEcontractAdditions(directioryFile, fileNameA, fileNameB)) {
//					resultHandle.setMsg("目录下不存在合同附件模板。");
//					return resultHandle;
//				}

				Template template = configuration.getTemplate(templateName.toString());
				if (template == null) {
					resultHandle.setMsg("初始化ftl模板失败。");
					return resultHandle;
				}

				StringWriter sw = new StringWriter();
				template.process(rootMap, sw);
				String htmlString = sw.toString();
				
//				System.out.println(htmlString);
				
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
					fileName = "TeamWithInTerritoryContract_" + travelContractVO.getContractVersion() + ".pdf";
				}else{
					fileName = "TeamWithInTerritoryContract_emptyTemplate.pdf";
				}
				
				
				//调试时打开
				this.newContractDebug(fileBytes, fileName);
				
				ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
				Long fileId = fsClient.uploadFile(fileName, bai, SERVER_TYPE);
				bai.close();
				
				if (fileId != null && fileId != 0) {
					ResultHandleT<ComFileMap> handleA = null;
					
					handleA = saveOrUpdateCommonFile(fileNameA.toString(), directioryFile);
					if (handleA.isFail()) {
						resultHandle.setMsg(handleA.getMsg());
						return resultHandle;
					}

					ordTravelContract.setVersion(travelContractVO.getContractVersion());
					ordTravelContract.setFileId(fileId);
					
					//合同签约状态逻辑
					setOrdContractStatus(ordTravelContract, order,true);
					
					ordTravelContract.setContractName(contractName.toString());
//					ordTravelContract.setSigningType(OrderEnum.ORDER_CONTRACT_SIGNING_TYPE.ONLINE.name());
					
					String attachementURLs = fileNameA.toString() ;
					ordTravelContract.setAttachementUrl(attachementURLs);
					ordTravelContract.setCreateTime(new Date());
					if (ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, operatorName) <= 0) {
						ordTravelContractService.saveOrdTravelContract(ordTravelContract, operatorName);
					}
					
				} else {
					resultHandle.setMsg("合同上传失败。");
				}
				
				//行程单生成
				this.saveTravelItineraryContract(ordTravelContract,operatorName);
				
				String content=contractName+"生成成功";
				
				this.insertOrderLog(ordTravelContract.getOrderId(), ordTravelContract.getOrdContractId(), operatorName, content,  null);
				
				/*合同快照部分*/
				//1.获取合同填充的数据和该合同对应的行程单的数据
				TeamWithInTerritoryContractDataVO teamWithInTerritoryContractDataVO = new TeamWithInTerritoryContractDataVO();
				teamWithInTerritoryContractDataVO.setSupplementaryTerms(travelContractVO.getSupplementaryTerms());
				teamWithInTerritoryContractDataVO.setRecommendDetailList(travelContractVO.getRecommendDetailList());
				teamWithInTerritoryContractDataVO.setShopingDetailList(travelContractVO.getShopingDetailList());
				
				Map<String,Object> travelItineraryContractMap = this.getContractContent(ordTravelContract,order);
				TravelContractVO travelItineraryVO = (TravelContractVO)travelItineraryContractMap.get("travelContractVO");
				if(null != travelItineraryVO.getShipLineRoute()){
					teamWithInTerritoryContractDataVO.setShipLineRoute(travelItineraryVO.getShipLineRoute());
				}else if(null != travelItineraryVO.getLineRoute()){
					teamWithInTerritoryContractDataVO.setLineRoute(travelItineraryVO.getLineRoute());
				}
				
				//2.根据组装的数据dataVO转化为json,并上传到文件服务器，并返回保存的文件ID
				Long jsonfileId = null;
				try {
					String str = JSONObject.toJSONString(teamWithInTerritoryContractDataVO);
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
				LOG.error(ExceptionFormatUtil.getTrace(e));
				resultHandle.setMsg(e);
			}
		}else {
			LOG.info("---------------团队境内旅游合同不存在-------------");
		} 
		
		return resultHandle;
	}


	
	/**
	 *更新合同 根据OrdOrder生成旅游合同，上船至FTP服务器。
	 * 
	 * @param ordOrder
	 */
	public ResultHandle updateTravelContact(TravelContractVO travelContractVO, OrdOrder order ,OrdTravelContract ordTravelContract,String operatorName){
		
		ResultHandle resultHandle = new ResultHandle();
		
//		Long orderId=NumberUtils.toLong(travelContractVO.getOrderId());
		
//		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
//		
//		OrdTravelContract ordTravelContract=ordTravelContractService.findOrdTravelContractById(travelContractVO.getOrdContractId());
		
		
		File directioryFile = initDirectory();
		
		travelContractVO=this.buildTravelContractVOUpdateData(travelContractVO, ordTravelContract, order);
		if(travelContractVO != null) {
			travelContractVO.setTemplateDirectory("file:///" + directioryFile.getAbsolutePath());//图片单选按钮
		}
		
		//合同中应有字段丢失，发送预警邮件
		resultHandle = checkUpdateTravelContractData(travelContractVO,order);
		if(resultHandle.isFail()){
			LOG.info("---------------修改合同时应有字段丢失，发送预警邮件-------------");
			return resultHandle;
		}
		//end
		
		//行程单无效，发送预警邮件
		resultHandle = checkprodLineRouteVOList(travelContractVO,order,travelContractVO.getProdProduct());
		if(resultHandle.isFail()){
			LOG.info("---------------修改合同时行程单无效，发送预警邮件-------------");
			return resultHandle;
		}
		//end
		
		Map<String,Object> rootMap = new HashMap<String, Object>();
		rootMap.put("travelContractVO", travelContractVO);
		Map<String,List<OrderMonitorRst>>  chidOrderMap=findChildOrderList(ordTravelContract,order,false);
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

			Template template = configuration.getTemplate(templateName
					.toString());
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

			String fileName = "TeamWithInTerritoryContract_"
					+ travelContractVO.getContractVersion() + ".pdf";

			// 调试时打开
			this.updateContractDubg(fileBytes, fileName);
			
			
			ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
			Long fileId = fsClient.uploadFile(fileName, bai, SERVER_TYPE);
			bai.close();

			if (fileId != null && fileId != 0) {
				ResultHandleT<ComFileMap> handleA = null;

				handleA = saveOrUpdateCommonFile(fileNameA.toString(),
						directioryFile);
				if (handleA.isFail()) {
					resultHandle.setMsg(handleA.getMsg());
					return resultHandle;
				}

				ordTravelContract.setVersion(travelContractVO.getContractVersion());
				ordTravelContract.setFileId(fileId);

				// 合同签约状态逻辑
				setOrdContractStatus(ordTravelContract, order, false);

				ordTravelContract.setContractName(contractName.toString());
				// ordTravelContract.setSigningType(OrderEnum.ORDER_CONTRACT_SIGNING_TYPE.ONLINE.name());

				String attachementURLs = fileNameA.toString();
				ordTravelContract.setAttachementUrl(attachementURLs);
				ordTravelContract.setCreateTime(new Date());
				if (ordTravelContractService.updateByPrimaryKeySelective(
						ordTravelContract, operatorName) <= 0) {
					ordTravelContractService.saveOrdTravelContract(
							ordTravelContract, operatorName);
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
				
				LOG.info("开始组装合同数据orderId:" + order.getOrderId());
				
				travelContractVO = buildTravelContractVOData(ordTravelContract,order);
				
				//关联销售当地游，替换相关合同内容信息
				replaceTravelContractVOData(ordTravelContract,order,travelContractVO);
				chidOrderMap=findChildOrderList(ordTravelContract,order,false);
			}
			
			travelContractVO.setTemplateDirectory("file:///" + directioryFile.getAbsolutePath());//图片单选按钮
			LOG.info("TeamOutboundTourismContractServiceImpl.saveTravelContact,fileDir=" + travelContractVO.getTemplateDirectory());
			
			rootMap.put("travelContractVO", travelContractVO);
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
					
					order.setOrdTravellerList(OrdPersonList);

					//出发日期
					order.setVisitTime(orderContractItem.getVisitTime());
//					travelContractVO.setVistDate(DateUtil.formatDate(orderContractItem.getVisitTime(), "yyyy-MM-dd"));
//
//					travelContractVO.setVisitTime(DateUtil.formatDate(orderContractItem.getVisitTime(), "yyyy-MM-dd"));
					
					
					//总金额
					travelContractVO.setTraveAmount(orderContractItem.getTotalPriceYuan());
	
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
					
					//营业地址
					if (travelContractVO.getDelegateGroup()) {
						travelContractVO.setLocalTravelAgencyName(suppSupplier.getSupplierName());
						travelContractVO.setLocalTravelAgencyAddress(suppSupplier.getAddress());
					} else {
						travelContractVO.setLocalTravelAgencyName("/");
						travelContractVO.setLocalTravelAgencyAddress("/");
					}

					//结束日期
					
					Long productId=(Long)mapProduct.get("productId");
					ProdProductParam param = new ProdProductParam();
					param.setProductProp(true);
					param.setProductBranchValue(true);
					param.setProdEcontract(true);
					param.setLineRoute(true);
					ResultHandleT<ProdProduct> resultHandle=this.prodProductClientService.findLineProductByProductId(productId, param);
					
					ProdProduct prodProduct=resultHandle.getReturnContent();
					
					//共几天  饭店住宿几夜
					Integer routeNights =0;
					Integer routeDays =0;
					Map<String,Object> map=null;
					map =orderContractItem.getContentMap();
					routeDays =  (Integer) map.get(OrderEnum.ORDER_PACK_TYPE.route_days.name());
					routeNights =  (Integer) map.get(OrderEnum.ORDER_PACK_TYPE.route_nights.name());
					ProdLineRouteVO prodLineRouteVO = null;
					
					
					
					if(CollectionUtils.isNotEmpty(prodProduct.getProdLineRouteList())) {
						prodLineRouteVO = prodProduct.getProdLineRouteList().get(0);
					}
					if (routeDays != null) {
						travelContractVO.setRouteDays(routeDays+"");
					} else {
						if(prodLineRouteVO != null) {
							routeDays  = Integer.parseInt(prodLineRouteVO.getRouteNum() + "");
							travelContractVO.setRouteDays(routeDays+"");
						}
					}
					if (routeNights != null) {
						travelContractVO.setRouteNights(routeNights+"");
					} else {
						if(prodLineRouteVO != null) {
							routeNights  = Integer.parseInt(prodLineRouteVO.getStayNum() + "");
							travelContractVO.setRouteNights(routeNights+"");
						}	
					}
					if(prodLineRouteVO !=null){
						travelContractVO.setLineRouteId(prodLineRouteVO.getLineRouteId());//设置行程ID
					}
					
					//补充条款  最低成团人数  （费用包含和不包含）
					setSupplementAndMinPersonCount(travelContractVO, prodProduct,order);
					
					//自愿购物活动补充协议 自愿参加另行付费旅游项目补充协议
					fillProdContractDetail(order, travelContractVO, prodProduct);
					
					//结束日期
					Date beginDate = orderContractItem.getVisitTime();
					if (routeDays!=null) {
						travelContractVO.setOverDate(DateUtil.formatDate(DateUtils.addDays(beginDate,Integer.parseInt(travelContractVO.getRouteDays())-1), "yyyy-MM-dd"));
					}

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
	 * @param curiseProductVO
	 * @return
	 */
	private TravelContractVO buildTravelContractVOData(OrdTravelContract ordTravelContract,OrdOrder order) {
		LOG.info("TeamWithInTerritoryContractServiceImpl.buildTravelContractVOData.orderId:"+order.getOrderId());
		List<OrdOrderPack>  ordPackList=order.getOrderPackList();
		
		HashMap<String, Object> mapProduct=this.getProductIdAndName(ordTravelContract, order);
		Long productId=(Long)mapProduct.get("productId");
		String productName=(String)mapProduct.get("productName");
		OrdOrderItem orderContractItem=(OrdOrderItem)mapProduct.get("orderContractItem");
		
		
		TravelContractVO travelContractVO = new TravelContractVO();
		if(order.getLineRouteId() != null){
			travelContractVO.setLineRouteId(order.getLineRouteId());
		}
		
		ProdProductParam param = new ProdProductParam();
		param.setProductProp(true);
		param.setProductBranchValue(true);
		param.setProdEcontract(true);
		param.setLineRoute(true);
		ResultHandleT<ProdProduct> resultHandle=this.prodProductClientService.findLineProductByProductId(productId, param);
		
		ProdProduct prodProduct=resultHandle.getReturnContent();


		//组团方式
		if(StringUtils.isNotEmpty(prodProduct.getProdEcontract().getGroupType())){
			travelContractVO.setGroupType(prodProduct.getProdEcontract().getGroupType());
		}
		if(StringUtils.isNotEmpty(prodProduct.getProdEcontract().getGroupSupplierName())){
			travelContractVO.setEntrustTravelAgency(prodProduct.getProdEcontract().getGroupSupplierName());
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

		//自动打包交通 && 使用被打包产品费用说明
		if("Y".equals(autoPackTrafficCode) && "Y".equals(isusePackedCostExplanationCode)){
			resultHandle=this.prodProductClientService.findLineProductByProductId(Long.parseLong(packedProductId), param);
			prodProduct=resultHandle.getReturnContent();
            if(prodProduct.getProdLineRouteList().get(0).getLineRouteId() != null){
            	travelContractVO.setLineRouteId(prodProduct.getProdLineRouteList().get(0).getLineRouteId());
            	travelContractVO.setDescription("成人、2-12周岁儿童均含往返机票 ;以上报价已包含机票税和燃油附加费。");
            }
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
			
			//甲方
			String travellers = null;
			int travellerCount = 0;
			
			Map<String, Object> var1 = new HashMap<String, Object>();
			var1.put("orderId", order.getOrderId());
			List<OrdPerson> ordPersonList = ordPersonService.getBookPersonInfoByOrderId(var1);
			if(ordPersonList != null && ordPersonList.size() > 0){
				LOG.info("ordPersonListSize:"+ordPersonList.size());
				lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.CREATE_ORDER.name(), order.getOrderId(), LvmmLogEnum.BUSSINESS_TAG.USER.name(), "查询游玩人成功(", "查询游玩人成功，人数为：" + ordPersonList.size());
				for (OrdPerson ordPerson : ordPersonList) {
					if(ordPerson != null && OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name().equalsIgnoreCase(ordPerson.getObjectType())){
						if (OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equalsIgnoreCase(ordPerson.getPersonType())) {
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
						// 下单人信息
						if (OrderEnum.ORDER_PERSON_TYPE.CONTACT.name().equalsIgnoreCase(ordPerson.getPersonType())) {
							travelContractVO.setMobile(ordPerson.getMobile());
							travelContractVO.setEmail(ordPerson.getEmail());
						}
					}
				}
			}
			       
			travelContractVO.setTravellers(travellers);
			
			//出境社，乙方
			travelContractVO.setFilialeName(this.filialeNameMap.get(order.getFilialeName()));
			
			//监督电话
			travelContractVO.setJianduTel(this.jianduTelMap.get(order.getFilialeName()));
			//产品名称
			travelContractVO.setProductName(productName);
			
			//出发日期
			travelContractVO.setVistDate(DateUtil.getChineseDay(order.getVisitTime()));
			
			//省
			travelContractVO.setProvince(this.provinceMap.get(order.getFilialeName()));
			
			//市
			travelContractVO.setCity(this.cityMap.get(order.getFilialeName()));
			
			//投诉电话
			travelContractVO.setLvTSTelephone(this.lvTSTelephoneMap.get(order.getFilialeName()));
			
			//地址
			travelContractVO.setLvAddress(this.lvAddressMap.get(order.getFilialeName()));
			
			//邮编
			travelContractVO.setLvpostcode(this.lvpostcodeMap.get(order.getFilialeName()));
			
			//营业地址
			travelContractVO.setAddress(this.businessAddressMap.get(order.getFilialeName()));
			//邮编
			travelContractVO.setPostcode(this.businessPostCodeMap.get(order.getFilialeName()));
			
			if(StringUtil.isEmptyString(travelContractVO.getAddress())){
				travelContractVO.setAddress(this.businessAddressMap.get("SH_FILIALE"));
			}
			if(StringUtil.isEmptyString(travelContractVO.getPostcode())){
				travelContractVO.setPostcode(this.businessPostCodeMap.get("SH_FILIALE"));
			}
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
			if (routeNights != null) {
				travelContractVO.setRouteNights(routeNights+"");
			} else {
				if(prodLineRouteVO != null) {
					routeNights  = Integer.parseInt(prodLineRouteVO.getStayNum() + "");
					travelContractVO.setRouteNights(routeNights+"");
				}	
			}
			
			//成人价格 儿童价格  只有跟团游供应商打包情况才取，只获取打包上的
			String[] priceArray=getPriceAdultAndChild(ordTravelContract, order);
			travelContractVO.setPriceAdult(priceArray[0]);
			travelContractVO.setPriceChild(priceArray[1]);

			//国内游合同 金额计算
			Long traveAmount=0L;
			List<OrdOrderItem> insuranceOrderItemList = getInsuranceOrdOrderItem(order);
			StringBuffer sb = new StringBuffer();
			if (insuranceOrderItemList != null && !insuranceOrderItemList.isEmpty()) {
				travelContractVO.setInsuranceOrderItemList(insuranceOrderItemList);
				travelContractVO.setHasInsurance(true);
				long totalInsurancePrice = getTotalPrice(insuranceOrderItemList);//所有保险的总价
				
				travelContractVO.setInsuranceAmount(PriceUtil.trans2YuanStr(totalInsurancePrice));
				
				for(OrdOrderItem item : insuranceOrderItemList){
					sb.append(item.getProductName()).append(" ");
				}
				
				travelContractVO.setInsuranceCompanyAndProductName(sb.toString());
				
			}
			if (!this.isOrderPackTrigger(ordTravelContract)) {//关联销售引起的合同金额计算
				//成人价总和+儿童价总和
				Long[] orderItemIdArray= this.getTriggerOrderItemId(ordTravelContract);
				if (orderItemIdArray!=null) {
					
					String[] priceTypeArray = new String[] {
							// ORDER_PRICE_RATE_TYPE.PRICE_ADULT_12.getCode(),
							// ORDER_PRICE_RATE_TYPE.PRICE_ADULT_34.getCode(),
							// ORDER_PRICE_RATE_TYPE.PRICE_CHILD_34.getCode(),
							ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCode(),
							ORDER_PRICE_RATE_TYPE.PRICE_CHILD.getCode(),
							ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.getCode()};

					Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
					paramsMulPriceRate.put("orderItemIdArray",
							orderItemIdArray);
					paramsMulPriceRate
							.put("priceTypeArray", priceTypeArray);
					List<OrdMulPriceRate> ordMulPriceRateList = ordMulPriceRateService
							.findOrdMulPriceRateList(paramsMulPriceRate);
					for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {

						traveAmount+=ordMulPriceRate.getPrice()*ordMulPriceRate.getQuantity();

					}
				}
			}else{//打包引起的合同金额计算
				//订单应付金额-保险金额-关联销售的当地游金额
				long totalInsurancePrice = 0L;
				if (insuranceOrderItemList != null && !insuranceOrderItemList.isEmpty()) {
					totalInsurancePrice = getTotalPrice(insuranceOrderItemList);//所有保险的总价
				}
				
				ResultHandleT<BizCategory> result = categoryClientService
						.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_route_local
								.getCode());
				BizCategory bizCategory = result.getReturnContent();
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("orderId", ordTravelContract.getOrderId());//订单号
				params.put("categoryId",bizCategory.getCategoryId());
				params.put("packIdIsNull", "ok");
				List<OrdOrderItem>  orderItemList=this.ordOrderUpdateService.queryOrderItemByParams(params);//当地游子订单关联销售的
				
				long GLRouteLocalAmount=0L;
				for (OrdOrderItem ordOrderItem : orderItemList) {
					GLRouteLocalAmount+=ordOrderItem.getPrice()*ordOrderItem.getQuantity();
				}
				traveAmount= order.getOughtAmount() - totalInsurancePrice-GLRouteLocalAmount;
			}
			travelContractVO.setTraveAmount(PriceUtil.trans2YuanStr(traveAmount));
			
			
			travelContractVO.setPayWay("在线支付");
			
			
			//补充条款  最低成团人数 （费用包含和不包含）
			setSupplementAndMinPersonCount(travelContractVO, prodProduct,order);
			
			
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
//			
//			if (CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.name().equalsIgnoreCase(prodEcontract.getGroupType())) {
//				travelContractVO.setDelegateGroup(true);
//				
//				/*for(OrdOrderItem ordOrderItem : order.getOrderItemList()) {
//					if (ordOrderItem != null) {
//						String categoryCode = (String) ordOrderItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
//						if (BizEnum.BIZ_CATEGORY_TYPE.category_cruise.name().equalsIgnoreCase(categoryCode)) {
//							ResultHandleT<SuppSupplier> resultHandleSuppSupplier =suppSupplierClientService.findSuppSupplierById(ordOrderItem.getSupplierId());
//							if (resultHandleSuppSupplier.isSuccess()) {
//								suppSupplier = resultHandleSuppSupplier.getReturnContent();
//								break;
//							}
//						}
//					}
//				}*/
//				if (suppSupplier != null) {
//					travelContractVO.setDelegateGroupName(suppSupplier.getSupplierName());
//				}
//			} else {
//				travelContractVO.setDelegateGroup(false);
//				travelContractVO.setDelegateGroupName("");
//			}
			
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
				travelContractVO.setOverDate(DateUtil.getChineseDay(DateUtils.addDays(beginDate, routeDays-1)));
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
			
			
			
			//自愿购物活动补充协议 自愿参加另行付费旅游项目补充协议  （合同条款）
			fillProdContractDetail(order, travelContractVO, prodProduct);
			
			
			travelContractVO.setSuppSupplier(suppSupplier);
			
//			Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
//			paramsMulPriceRate.put("orderItemId", orderItemId); 
//			List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
//			
			
			
			
			travelContractVO.setCreateTime(DateUtil.getChineseDay(order.getCreateTime()));
			travelContractVO.setVisitTime(DateUtil.getChineseDay(order.getVisitTime()));
			travelContractVO.setPaymentTime((DateUtil.formatDate(order.getPaymentTime(), "yyyy-MM-dd HH:mm")));
			travelContractVO.setOrdTravellerList(order.getOrdTravellerList());
			
			travelContractVO.setTravellersSize(order.getOrdTravellerList().size()+"");
			travelContractVO.setPermit(this.permitlMap.get(order.getFilialeName()));
			
			travelContractVO.setFullName(traveller.getFullName());
			travelContractVO.setIdNo(traveller.getIdNo());
			
			travelContractVO.setFax(traveller.getFax());
			
			
			travelContractVO.setSingnDate(DateUtil.getChineseDay(order.getCreateTime()));
			travelContractVO.setLvSingnDate(DateUtil.getChineseDay(order.getCreateTime()));
			LOG.info("product is COMMISSIONED_TOUR or SELF_TOUR?"+prodProduct.getProdEcontract().getGroupType());
			//产品是否委托组团
			if(CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.getCode().equalsIgnoreCase(prodProduct.getProdEcontract().getGroupType()))
			{
				travelContractVO.setProductDelegate(CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.getCode());
				travelContractVO.setProductDelegateName(prodProduct.getProdEcontract().getGroupSupplierName());
				LOG.info(travelContractVO.getOrderId()+"product is COMMISSIONED_TOUR ");
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
	 * @param curiseProductVO
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
		List<ProdContractDetail> recommendDetailList = null;
		List<ProdContractDetail> shopingDetailList = null;
		if(travelContractVO.getSaveForUpdateFlag() != null){
			saveForUpdateFlag = travelContractVO.getSaveForUpdateFlag();
			productName = travelContractVO.getProductName();
			if(travelContractVO.getRecommendDetailList() != null){
				recommendDetailList = travelContractVO.getRecommendDetailList();
			}
			if(travelContractVO.getShopingDetailList() != null){
				shopingDetailList = travelContractVO.getShopingDetailList();
			}
		}
		
		ProdProductParam param = new ProdProductParam();
		param.setProductProp(true);
		param.setProductBranchValue(true);
		param.setProdEcontract(true);
	
		ResultHandleT<ProdProduct> resultHandle=this.prodProductClientService.findLineProductByProductId(productId, param);
		
		ProdProduct prodProduct=resultHandle.getReturnContent();
		
//		SuppSupplier suppSupplier = new SuppSupplier();
		if (order != null && prodProduct != null) {
			travelContractVO.setProdProduct(prodProduct);
			travelContractVO.setProductId(productId);
			travelContractVO.setLineRouteId(order.getLineRouteId());

			String appendVersion = getAppendVersion(ordTravelContract);
			//合同编号
			String version = DateUtil.formatDate(order.getVisitTime(), "yyyyMMdd") + "-" + order.getOrderId() + "-" + appendVersion;
			travelContractVO.setContractVersion(version);
			
			//订单编号
			travelContractVO.setOrderId(order.getOrderId().toString());
			
			//产品名称
			travelContractVO.setProductName(productName);

			//国内游合同 金额计算
			List<OrdOrderItem> insuranceOrderItemList = getInsuranceOrdOrderItem(order);
			StringBuffer sb = new StringBuffer();
			if (insuranceOrderItemList != null && !insuranceOrderItemList.isEmpty()) {
				travelContractVO.setHasInsurance(true);
				long totalInsurancePrice = getTotalPrice(insuranceOrderItemList);//所有保险的总价
				travelContractVO.setInsuranceAmount(PriceUtil.trans2YuanStr(totalInsurancePrice));
				
				for(OrdOrderItem item : insuranceOrderItemList){
					sb.append(item.getProductName()).append(" ");
				}
				
				travelContractVO.setInsuranceCompanyAndProductName(sb.toString());
			}
			//营业地址
			travelContractVO.setAddress(this.businessAddressMap.get(prodProduct.getFiliale()));
			//邮编
			travelContractVO.setPostcode(this.businessPostCodeMap.get(prodProduct.getFiliale()));
			if(StringUtil.isEmptyString(travelContractVO.getAddress())){
				travelContractVO.setAddress(this.businessAddressMap.get("SH_FILIALE"));
			}
			if(StringUtil.isEmptyString(travelContractVO.getPostcode())){
				travelContractVO.setPostcode(this.businessPostCodeMap.get("SH_FILIALE"));
			}
			//旅行社盖章
			travelContractVO.setStampImage(getStampImageNameByFilialeName(prodProduct.getFiliale()));
			// 根据“公司主体”, 差异化信息处理
			super.handleCompanyType(ordTravelContract, order, travelContractVO);
			travelContractVO.setCreateTime(DateUtil.getChineseDay(order.getCreateTime()));
			travelContractVO.setOrdTravellerList(order.getOrdTravellerList());
			travelContractVO.setContractMobile(order.getContactPerson().getMobile());
			//自愿购物活动补充协议 自愿参加另行付费旅游项目补充协议
			fillProdContractDetail(order, travelContractVO, prodProduct);
			
			LOG.info("product is COMMISSIONED_TOUR or SELF_TOUR?"+prodProduct.getProdEcontract().getGroupType());
			//产品是否委托组团
			if(CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.getCode().equalsIgnoreCase(prodProduct.getProdEcontract().getGroupType()))
			{
				travelContractVO.setProductDelegate(CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.getCode());
				travelContractVO.setProductDelegateName(prodProduct.getProdEcontract().getGroupSupplierName());
				LOG.info(travelContractVO.getOrderId()+"product is COMMISSIONED_TOUR ");
			}
			if(CommEnumSet.GROUP_TYPE.SELF_TOUR.getCode().equalsIgnoreCase(prodProduct.getProdEcontract().getGroupType())){
				travelContractVO.setProductDelegate(CommEnumSet.GROUP_TYPE.SELF_TOUR.getCode());
				LOG.info(travelContractVO.getOrderId()+"product is SELF_TOUR");
			}
			
		}
		
//		travelContractVO.setSuppSupplier(suppSupplier);
		
		/*修改时保存合同的数据,重新设置*/
		if(saveForUpdateFlag != null){
			travelContractVO.setProductName(productName);
			if(recommendDetailList != null){
				travelContractVO.setRecommendDetailList(recommendDetailList);
			}
			if(shopingDetailList != null){
				travelContractVO.setShopingDetailList(shopingDetailList);
			}
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
		return contractTemplateHtml(templateName.toString());
	}



	@Override
	public ResultHandleT<String> getContractTemplateHtml(Long productId) {
		return contractTemplateHtml(templateName, productId);
	}
	


}
