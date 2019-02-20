package com.lvmama.vst.order.contract.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.lvmama.vst.comm.vo.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizDest;
import com.lvmama.vst.back.biz.po.BizDest.DEST_TYPE;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.biz.service.DestClientService;
import com.lvmama.vst.back.client.prod.curise.service.ProdCuriseProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.line.po.LineRoute;
import com.lvmama.vst.back.order.po.OrdContractSnapshotData;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdContractDetail;
import com.lvmama.vst.back.prod.po.ProdDestRe;
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
import com.lvmama.vst.order.contract.service.IOrderElectricContactService;
import com.lvmama.vst.order.contract.service.IOrderElectricService;
import com.lvmama.vst.order.contract.vo.OutboundTourContractDataVO;
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
@Service("teamOutboundTourismContractService")
public class TeamOutboundTourismContractServiceImpl extends AbstractOrderTravelElectricContactService implements IOrderElectricService {

	private static final Logger LOG = LoggerFactory.getLogger(TeamOutboundTourismContractServiceImpl.class);

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

	@Autowired
	private DestClientService destClientRemote;

	private static final Long TAI_WAN_ID =401L;

	private static final int MAX_COUNT_OF_PDF_LINE = 50;

	private static final String SERVER_TYPE = "COM_AFFIX";

	private static final String contractName = "团队出境旅游合同";
	private static final String templateName = "teamOutboundTourismContractTemplate.ftl";
	private static final String fileNameA = "supplementary_safety_notice_exit.pdf";

	@Override
	public ResultHandle saveTravelContact(OrdTravelContract ordTravelContract, String operatorName) {
		ResultHandle resultHandle = new ResultHandle();

		if (ordTravelContract != null) {
			OrdOrder order = complexQueryService.queryOrderByOrderId(ordTravelContract.getOrderId());
			if (order == null) {
				resultHandle.setMsg("订单ID=" + ordTravelContract.getOrderId() + "不存在。");
				return resultHandle;
			}
			LOG.info("---------------合同1------1-----");
			List<OrdOrderItem> ordOrderItemList =order.getOrderItemList();
			if (ordOrderItemList == null || ordOrderItemList.isEmpty()) {
				resultHandle.setMsg("订单ID=" + order.getOrderId() + "不存在子订单。");
				return resultHandle;
			}

			try {
				List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
				list.add(ordTravelContract);
				order.setOrdTravelContractList(list);
				LOG.info("---------------合同1------2-----");
				File directioryFile = initDirectory();
				if (directioryFile == null || !directioryFile.exists()) {
					resultHandle.setMsg("合同模板目录不存在。");
					return resultHandle;
				}
				LOG.info("---------------合同1------2-1-----");
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


				LOG.info("---------------合同1------2-2-----");
				Configuration configuration = initConfiguration(directioryFile);
				if (configuration == null) {
					resultHandle.setMsg("初始化freemarker失败。");
					return resultHandle;
				}
				LOG.info("---------------合同1------2-3-----");
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
				LOG.info("---------------合同1------2-4-----");
				StringWriter sw = new StringWriter();
				template.process(rootMap, sw);
				String htmlString = sw.toString();
				if (htmlString == null) {
					resultHandle.setMsg("合同HTML生成失败。");
					return resultHandle;
				}
				LOG.info("---------------合同1------2-5-----");
				ByteArrayOutputStream bao = PdfUtil.createPdfFile(htmlString);
				if (bao == null) {
					resultHandle.setMsg("合同PDF生成失败。");
					return resultHandle;
				}
				LOG.info("---------------合同1------2-6-----");

				byte[] fileBytes = bao.toByteArray();
				bao.close();
				String fileName = null;
				LOG.info("---------------合同1------3-----");
				if(StringUtils.isNotEmpty(travelContractVO.getContractVersion())){
					fileName = "TeamOutboundTourismContract_" + travelContractVO.getContractVersion() + ".pdf";
				}else{
					fileName = "TeamOutboundTourismContract_emptyTemplate.pdf";
				}


				//调试时打开
				newContractDebug(fileBytes, fileName);

				ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
				Long fileId = fsClient.uploadFile(fileName, bai, SERVER_TYPE);
				bai.close();
				//上传合同
				if (fileId != null && fileId != 0) {
					ResultHandleT<ComFileMap> handleA = null;

					handleA = saveOrUpdateCommonFile(fileNameA.toString(), directioryFile);
					if (handleA.isFail()) {
						resultHandle.setMsg(handleA.getMsg());
						return resultHandle;
					}

					ordTravelContract.setVersion(travelContractVO.getContractVersion());
					ordTravelContract.setFileId(fileId);

					LOG.info("---------------合同1------4-----");
					//合同签约状态逻辑
					setOrdContractStatus(ordTravelContract, order,true);


					ordTravelContract.setContractName(contractName.toString());
//					ordTravelContract.setSigningType(OrderEnum.ORDER_CONTRACT_SIGNING_TYPE.ONLINE.name());

					String attachementURLs = fileNameA.toString() ;
					ordTravelContract.setAttachementUrl(attachementURLs);
					ordTravelContract.setCreateTime(new Date());
					LOG.info("---------------合同1------5-----");
					if (ordTravelContractService.updateByPrimaryKeySelective(ordTravelContract, operatorName) <= 0) {
						ordTravelContractService.saveOrdTravelContract(ordTravelContract, operatorName);
					}

				} else {
					resultHandle.setMsg("合同上传失败。");
				}
				LOG.info("---------------合同1------6-----");
				//行程单生成
				this.saveTravelItineraryContract(ordTravelContract,operatorName);

				String content=contractName+"生成成功";

				this.insertOrderLog(ordTravelContract.getOrderId(), ordTravelContract.getOrdContractId(), operatorName, content,  null);

				/*合同快照部分*/
				//1.获取合同填充的数据和该合同对应的行程单的数据
				OutboundTourContractDataVO outboundTourContractDataVO = new OutboundTourContractDataVO();
				outboundTourContractDataVO.setSupplementaryTerms(travelContractVO.getSupplementaryTerms());
				outboundTourContractDataVO.setRecommendDetailList(travelContractVO.getRecommendDetailList());
				outboundTourContractDataVO.setShopingDetailList(travelContractVO.getShopingDetailList());

				Map<String,Object> travelItineraryContractMap = this.getContractContent(ordTravelContract,order);
				TravelContractVO travelItineraryVO = (TravelContractVO)travelItineraryContractMap.get("travelContractVO");
				if(null != travelItineraryVO.getShipLineRoute()){
					outboundTourContractDataVO.setShipLineRoute(travelItineraryVO.getShipLineRoute());
				}else if(null != travelItineraryVO.getLineRoute()){
					outboundTourContractDataVO.setLineRoute(travelItineraryVO.getLineRoute());
				}

				//2.根据组装的数据dataVO转化为json,并上传到文件服务器，并返回保存的文件ID
				Long jsonfileId = null;
				try {
					String str = JSONObject.toJSONString(outboundTourContractDataVO);
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
		} else {

		}

		return resultHandle;
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
				//关联销售当地游，替换相关合同内容信息
				replaceTravelContractVOData(ordTravelContract,order,travelContractVO);
				chidOrderMap=findChildOrderList(ordTravelContract,order,true);
			}

			travelContractVO.setTemplateDirectory("file:///" + directioryFile.getAbsolutePath());//模板目录
			LOG.info("TeamOutboundTourismContractServiceImpl.saveTravelContact,fileDir=" + travelContractVO.getTemplateDirectory());
			rootMap.put("travelContractVO", travelContractVO);
			rootMap.put("order", order);
			//rootMap.put("product", product);
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
					travelContractVO.setVistDate(DateUtil.formatDate(orderContractItem.getVisitTime(), "yyyy年MM月dd日"));

					travelContractVO.setVisitTime(DateUtil.formatDate(orderContractItem.getVisitTime(), "yyyy年MM月dd日"));


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
					/*if (routeNights != null) {
						travelContractVO.setRouteNights(routeNights+"");
					} else {
						if(prodLineRouteVO != null) {
							routeNights  = Integer.parseInt(prodLineRouteVO.getStayNum() + "");
							travelContractVO.setRouteNights(routeNights+"");
						}
					}*/
					if(prodLineRouteVO !=null){
						travelContractVO.setLineRouteId(prodLineRouteVO.getLineRouteId());//设置行程ID
					}

					//补充条款  最低成团人数
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
		param.setDest(true);
		ResultHandleT<ProdProduct> resultHandle=this.prodProductClientService.findLineProductByProductId(productId, param);

		ProdProduct prodProduct=resultHandle.getReturnContent();

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
			//判断产品类型
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(prodProduct.getBizCategoryId())
					||BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(prodProduct.getBizCategoryId())
					||BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(prodProduct.getBizCategoryId())){
				//判断是否是台湾的 和出境的
				if(CollectionUtils.isNotEmpty(prodProduct.getProdDestReList()) && ProdProduct.PRODUCTTYPE.FOREIGNLINE.name().equals(prodProduct.getProductType())){
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
			}

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

			//省份
			travelContractVO.setProvince(this.provinceMap.get(order.getFilialeName()));
			//城市
			travelContractVO.setCity(this.cityMap.get(order.getFilialeName()));
			//投诉电话
			travelContractVO.setLvTSTelephone(this.lvTSTelephoneMap.get(order.getFilialeName()));
			//地址
			travelContractVO.setLvAddress(this.lvAddressMap.get(order.getFilialeName()));
			//邮编
			travelContractVO.setLvpostcode(this.lvpostcodeMap.get(order.getFilialeName()));

			//监督电话
			travelContractVO.setJianduTel(this.jianduTelMap.get(order.getFilialeName()));

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

			//出发日期
			travelContractVO.setVistDate(DateUtil.getChineseDay(order.getVisitTime()));
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
			/*if (routeNights != null) {
				travelContractVO.setRouteNights(routeNights+"");
			} else {
				if(prodLineRouteVO != null) {
					routeNights  = Integer.parseInt(prodLineRouteVO.getStayNum() + "");
					travelContractVO.setRouteNights(routeNights+"");
				}
			}
			if(order.getCategoryId()==8){
				travelContractVO.setRouteNights("/");
			}*/


			//成人价格 儿童价格  只有跟团游供应商打包情况才取，只获取打包上的
			String[] priceArray=getPriceAdultAndChild(ordTravelContract, order);
			travelContractVO.setPriceAdult(priceArray[0]);
			travelContractVO.setPriceChild(priceArray[1]);
			//出境游合同情况下
			//旅游费用合计 抓取订单的合同金额；不包含保险的费用；
			List<OrdOrderItem> insuranceOrderItemList = getInsuranceOrdOrderItem(order);
			//判断是否为出境跟团游打包签证并返回签证列表
			List<OrdOrderItem> visaOrderItemList = getvisaOrderItem(order);
			StringBuffer sb = new StringBuffer();
			if (insuranceOrderItemList != null && !insuranceOrderItemList.isEmpty()) {
				travelContractVO.setHasInsurance(true);
				OrdOrderItem insuranceOrdItem = insuranceOrderItemList.get(0);

				long totalInsurancePrice = getTotalPrice(insuranceOrderItemList);//所有保险的总价

				long totalPrice = order.getOughtAmount() - totalInsurancePrice;

				travelContractVO.setTraveAmount(PriceUtil.trans2YuanStr(totalPrice));
				travelContractVO.setInsuranceAmount(PriceUtil.trans2YuanStr(totalInsurancePrice));
				//保险公司名称+产品名称
				for(OrdOrderItem item : insuranceOrderItemList){
					sb.append(item.getProductName()).append("  ");
				}
				travelContractVO.setInsuranceCompanyAndProductName(sb.toString());//insuranceOrdItem.getContentValueByKey(OrderEnum.ORDER_COMMON_TYPE.insurance_company.name())
			} else {
				travelContractVO.setInsuranceCompanyAndProductName("/");
				travelContractVO.setInsuranceAmount("/");

				travelContractVO.setTraveAmount(order.getOughtAmountYuan());
			}
			if (visaOrderItemList != null && !visaOrderItemList.isEmpty()) {
				//剔除跟团游关联签证的签证的费用
				long totalPrice=PriceUtil.convertToFen(travelContractVO.getTraveAmount());
				long totalVisaAmount=getTotalPrice(visaOrderItemList);
				travelContractVO.setTraveAmount(PriceUtil.trans2YuanStr(totalPrice-totalVisaAmount));
			}

			travelContractVO.setPayWay("在线支付");

			//补充条款  最低成团人数
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
				travelContractVO.setOverDate(DateUtil.getChineseDay(DateUtils.addDays(beginDate,routeDays-1)));
			}
			if(order.getCategoryId()==8){
				List<LineRoute> lineRouteList =  prodProduct.getLineRoutes();
				if(lineRouteList!=null&&!lineRouteList.isEmpty()){
					if(lineRouteList.get(0).getDays()!=null){
						Integer days  = lineRouteList.get(0).getDays().intValue();
						travelContractVO.setOverDate(DateUtil.getChineseDay(DateUtils.addDays(beginDate,days-1)));
						travelContractVO.setRouteDays(days.toString());
					}

				}
				ProdEcontract econtract = prodProduct.getProdEcontract();
				if(econtract!=null&&econtract.getMinPerson()!=null){
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

			//电子邮件
			travelContractVO.setEmail(order.getContactPerson().getEmail());

			//日期
			travelContractVO.setFirstSignatrueDate(DateUtil.getChineseDay(order.getCreateTime()));
			travelContractVO.setSecondSignatrueDate(DateUtil.getChineseDay(order.getCreateTime()));
			//自愿购物活动补充协议 自愿参加另行付费旅游项目补充协议
			fillProdContractDetail(order, travelContractVO, prodProduct);

			travelContractVO.setCreateTime(DateUtil.getChineseDay(order.getCreateTime()));
			travelContractVO.setVisitTime(DateUtil.getChineseDay(order.getVisitTime()));
			travelContractVO.setPaymentTime((DateUtil.formatDate(order.getPaymentTime(), "yyyy-MM-dd HH:mm")));
			travelContractVO.setOrdTravellerList(order.getOrdTravellerList());

			travelContractVO.setTravellersSize(order.getOrdTravellerList().size()+"");
			travelContractVO.setPermit(this.permitlMap.get(order.getFilialeName()));

			travelContractVO.setFullName(traveller.getFullName());
			//travelContractVO.setIdNo(traveller.getIdNo());//证件号码
			travelContractVO.setMobile(traveller.getMobile());
			//travelContractVO.setFax(traveller.getFax());
			//travelContractVO.setEmail(traveller.getEmail());

			travelContractVO.setSingnDate(DateUtil.getChineseDay(order.getCreateTime()));
			travelContractVO.setLvSingnDate(DateUtil.getChineseDay(order.getCreateTime()));

			travelContractVO.setSuppSupplier(suppSupplier);
			LOG.info("product is COMMISSIONED_TOUR or SELF_TOUR?"+prodProduct.getProdEcontract().getGroupType());
			//产品是否委托组团
			if(CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.getCode().equalsIgnoreCase(prodProduct.getProdEcontract().getGroupType()))
			{
				travelContractVO.setProductDelegate(CommEnumSet.GROUP_TYPE.COMMISSIONED_TOUR.getCode());
				travelContractVO.setProductDelegateName(prodProduct.getProdEcontract().getGroupSupplierName());
				LOG.info(travelContractVO.getOrderId()+"product is COMMISSIONED_TOUR ");
				travelContractVO.setTourdiy(prodProduct.getProdEcontract().getGroupType());

				if(order.getCategoryId()==8){
					if(suppSupplier!=null&&StringUtils.isNotEmpty(suppSupplier.getSupplierName())){
						travelContractVO.setProductDelegateName(suppSupplier.getSupplierName());
					}
				}

			}
			if(CommEnumSet.GROUP_TYPE.SELF_TOUR.getCode().equalsIgnoreCase(prodProduct.getProdEcontract().getGroupType())){
				travelContractVO.setProductDelegate(CommEnumSet.GROUP_TYPE.SELF_TOUR.getCode());
				LOG.info(travelContractVO.getOrderId()+"product is SELF_TOUR");
			}
		}

//		Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
//		paramsMulPriceRate.put("orderItemId", orderItemId);
//		List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
//

		// 根据“公司主体”, 差异化信息处理
		super.handleCompanyType(ordTravelContract, order, travelContractVO);

		return travelContractVO;
	}

	private List<OrdOrderItem> getvisaOrderItem(OrdOrder order) {
		List<OrdOrderItem> orderItemList = order.getOrderItemList();
		boolean isoutBound_packageTour=false;
		boolean contantVisaItem=false;
		List<OrdOrderItem> viasOrderItem=new ArrayList<>();
		for(OrdOrderItem item : orderItemList){
			boolean ismainItem = item.getMainItem().equals("true");
			if(ismainItem){
				if(item.getCategoryId()!=null&&item.getCategoryId()==15&& CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(item.getBuCode())){
					//为出境跟团游
					isoutBound_packageTour=true;

				}
			}else if(item.getCategoryId()!=null&&item.getCategoryId()==4){
				contantVisaItem=true;
				viasOrderItem.add(item);
			}
		}

		if(isoutBound_packageTour&&contantVisaItem){
			return viasOrderItem;
		}else{
			return null;
		}
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
		param.setDest(true);

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

			//判断是否是台湾的
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(prodProduct.getBizCategoryId())
					||BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(prodProduct.getBizCategoryId())
					||BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(prodProduct.getBizCategoryId())){
				if(CollectionUtils.isNotEmpty(prodProduct.getProdDestReList()) && ProdProduct.PRODUCTTYPE.FOREIGNLINE.name().equals(prodProduct.getProductType()) ){
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
			}
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

//			ResultHandleT<SuppSupplier> resultHandleSuppSupplier =suppSupplierClientService.findSuppSupplierById(order.getMainOrderItem().getSupplierId());
//			if (resultHandleSuppSupplier.isSuccess()) {
//				suppSupplier = resultHandleSuppSupplier.getReturnContent();
//			}

			travelContractVO.setCreateTime(DateUtil.getChineseDay(order.getCreateTime()));
			travelContractVO.setOrdTravellerList(order.getOrdTravellerList());

			travelContractVO.setContractMobile(order.getContactPerson().getMobile());
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
			//自愿购物活动补充协议 自愿参加另行付费旅游项目补充协议
			fillProdContractDetail(order, travelContractVO, prodProduct);
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
	public ResultHandle updateTravelContact(TravelContractVO travelContractVO,
			OrdOrder order, OrdTravelContract ordTravelContract,
			String operatorName) {
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
		Map<String,List<OrderMonitorRst>>  chidOrderMap=findChildOrderList(ordTravelContract,order,true);
		rootMap.put("chidOrderMap", chidOrderMap);
		rootMap.put("order", order);


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

			String fileName = "TeamOutboundTourismContract_"
					+ travelContractVO.getContractVersion() + ".pdf";

			// 调试时打开自动本地成文件
			updateContractDubg(fileBytes, fileName);

			ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
			Long fileId = fsClient.uploadFile(fileName, bai, SERVER_TYPE);
			bai.close();

			if (fileId != null && fileId != 0) {
				ResultHandleT<ComFileMap> handleA = null;

				handleA = saveOrUpdateCommonFile(fileNameA.toString(),directioryFile);
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
		return contractTemplateHtml(templateName, productId);
	}
}
