package com.lvmama.vst.order.contract.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.biz.po.BizDict;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.DictClientService;
import com.lvmama.vst.back.client.prod.service.ProdLineRouteClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.dujia.comm.route.detail.po.ProdRouteDetailActivity;
import com.lvmama.vst.back.dujia.comm.route.detail.po.ProdRouteDetailGroup;
import com.lvmama.vst.back.dujia.comm.route.detail.utils.BuildRouteTemplateUtil;
import com.lvmama.vst.back.dujia.comm.route.detail.utils.RouteDetailFormat;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdContractDetail;
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.back.prod.po.ProdLineRouteDetail;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProduct.COMPANY_TYPE_DIC;
import com.lvmama.vst.back.prod.po.ProdTrafficGroup;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComLog.COM_LOG_LOG_TYPE;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.front.ProductPreorderUtil;
import com.lvmama.vst.comm.utils.pdf.PdfUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.econtract.EcontractBuilder;
import com.lvmama.vst.comm.vo.econtract.TravelContractVO;
import com.lvmama.vst.order.contract.service.IOrderNoticeRegimentService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdAdditionStatusService;
import com.lvmama.vst.order.snapshot.service.IOrderLinerouteSnapshotService;

/**
 * 
 * @author zhangwei
 *
 */
@Service("noticeRegimentService")
public class NoticeRegimentServiceImpl extends AbstractOrderTravelElectricContactService implements IOrderNoticeRegimentService {
	
	private static final Log LOG = LogFactory.getLog(NoticeRegimentServiceImpl.class);

	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private ProdProductClientService prodProductClientService;

	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Autowired
	private IOrdAdditionStatusService ordAdditionStatusService;
	
	@Autowired
	private DictClientService dictClientService;
	
	@Autowired
	private ProdLineRouteClientService prodLineRouteClientService;
	
	@Autowired
	private IOrderLinerouteSnapshotService orderLinerouteSnapshotService;
	
	private static final String SERVER_TYPE = "COM_AFFIX";
	
	private static final String contractName = "出团通知书";
	
	@Override
	public ResultHandle saveNoticeRegiment(TravelContractVO travelContractVo, String operatorName) {
		ResultHandle resultHandle = new ResultHandle();
		
		if (travelContractVo != null) {
			Long orderId=NumberUtils.toLong(travelContractVo.getOrderId());
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			if (order == null) {
				resultHandle.setMsg("订单ID=" + orderId + "不存在。");
				return resultHandle;
			}
			
			List<OrdOrderItem> ordOrderItemList =order.getOrderItemList();
			if (ordOrderItemList == null || ordOrderItemList.isEmpty()) {
				resultHandle.setMsg("订单ID=" + order.getOrderId() + "不存在子订单。");
				return resultHandle;
			}
			
			try {
				buildTravelContractVOData(travelContractVo,order);
				EcontractBuilder econtractBuilder = new EcontractBuilder();
				String htmlString =econtractBuilder.makeContent(travelContractVo, order);
//				htmlString=htmlString.replaceAll("\r\n", "");
				LOG.info("htmlString=="+htmlString);
				ByteArrayOutputStream bao = PdfUtil.createPdfFile(htmlString);
				if (bao == null) {
					resultHandle.setMsg("出团通知书PDF生成失败。");
					return resultHandle;
				}
				

				byte[] fileBytes = bao.toByteArray();
				bao.close();
				
				
				String fileName = "noticeRegiment.pdf";
				
				//调试时打开
				this.updateContractDubg(fileBytes, fileName);
				
				
				
				
				ByteArrayInputStream bai = new ByteArrayInputStream(fileBytes);
				Long fileId = fsClient.uploadFile(fileName, bai, SERVER_TYPE);
				bai.close();
				
				if (fileId == null || fileId == 0) {
					resultHandle.setMsg("出团通知书上传失败。");
				}
				String email="";
				if (order.getContactPerson()!=null) {
					email=order.getContactPerson().getEmail();
				}
				
				if (StringUtils.isEmpty(email)) {
					resultHandle.setMsg("联系人email没有，无法发送出团通知书");
				}
				
				ordAdditionStatusService.addUploadAndSendNoticeRegiment(fileId, fileName, "", orderId, email,operatorName);
				
				String content=contractName+"使用邮件模板生成成功";
				
				lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
						orderId, 
						orderId, 
						operatorName, 
						content, 
						COM_LOG_LOG_TYPE.ORD_ORDER_NOTICE_REGIMENT.name(), 
						COM_LOG_LOG_TYPE.ORD_ORDER_NOTICE_REGIMENT.getCnName(),
						null);
				
				
				
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
				resultHandle.setMsg(e);
			}
		} else {
			
		}
		
		return resultHandle;
	}



	



	
	
	
	
	public Map<String,Object> captureContract(TravelContractVO travelContractVo,OrdOrder order,File directioryFile) {
		
		
		
		Map<String,Object> rootMap = new HashMap<String, Object>();
		
		if (order != null) {
			
			TravelContractVO travelContractVO = buildTravelContractVOData(travelContractVo,order);
			ProdProduct prodProduct=travelContractVO.getProdProduct();
			Map<String, String> propValueMap=travelContractVO.getPropValueMap();
			
			travelContractVO.setPriceIncludes(PdfUtil.convertHtml(propValueMap.get("the_fee_includes")));
			travelContractVO.setPriceNotIncludes(PdfUtil.convertHtml(propValueMap.get("cost_free")));
			if (prodProduct.getBizCategoryId()==17L) {
				travelContractVO.setTravelWarnings(PdfUtil.convertHtml(propValueMap.get("important")));
			}else{
				travelContractVO.setTravelWarnings(PdfUtil.convertHtml(propValueMap.get("warning")));
//				travelContractVO.setTravelWarnings(propValueMap.get("warning"));
			}
			travelContractVO.setBackToThat(PdfUtil.convertHtml(propValueMap.get("change_and_cancellation_instructions")));
			
//			travelContractVO.setTemplateDirectory("file:///" + directioryFile.getAbsolutePath());//图片单选按钮
			LOG.info("NoticeRegimentServiceImpl.saveNoticeRegiment,fileDir=" + travelContractVO.getTemplateDirectory());
			
			
			rootMap.put("travelContractVO", travelContractVO);
			
			rootMap.put("order", order);
			
			rootMap.put("routeDetailFormat", new RouteDetailFormat());
			
		}
		return rootMap;
	}


	

	
	/**
	 * 组装合同展示数据
	 * @param order
	 * @param curiseProductVO
	 * @return
	 */
	@SuppressWarnings("static-access")
    private TravelContractVO buildTravelContractVOData(TravelContractVO travelContractVO,OrdOrder order) {
		
		List<OrdOrderPack>  ordPackList=order.getOrderPackList();

		Long productId=0L;
		String productName="";
		if (!CollectionUtils.isEmpty(ordPackList)) {
			
			productId=order.getOrdOrderPack().getProductId();
			productName=order.getOrdOrderPack().getProductName();
		}else{
			productId=order.getMainOrderItem().getProductId();
			productName=order.getMainOrderItem().getProductName();
			
		}
		ProdProductParam param = new ProdProductParam();
		param.setProductProp(true);
		param.setProductBranchValue(true);
		param.setProdEcontract(true);
		param.setLineRoute(true);
		
		ResultHandleT<ProdProduct> resultHandle=this.prodProductClientService.findLineProductByProductId(productId, param);
		
		ProdProduct prodProduct=resultHandle.getReturnContent();
		
		if (order != null && prodProduct != null) {
			if (travelContractVO==null) {
				travelContractVO = new TravelContractVO();
			}
			
			travelContractVO.setProdProduct(prodProduct);
			travelContractVO.setProductId(productId);
			travelContractVO.setLineRouteId(order.getLineRouteId());
			
			
			boolean isLocalBu = false;
			//判断当前产品是否是国内BU的产品（国内，国内长线，国内短线，国内边境游）
			if (StringUtil.isNotEmptyString(prodProduct.getProductType())) {
				if ("INNERLINE".equals(prodProduct.getProductType()) || "INNERSHORTLINE".equals(prodProduct.getProductType()) || "INNERLONGLINE".equals(prodProduct.getProductType()) || "INNER_BORDER_LINE".equals(prodProduct.getProductType())) {
					isLocalBu = true;
					travelContractVO.setIsLocalBu("Y");
				}
			}
			
			// 获取国内供应商打包的其它巴士信息
			if("SUPPLIER".equals(prodProduct.getPackageType()) && isLocalBu){
				List<ProdTrafficGroup> prodTrafficGroupList = prodProductClientService.findProdTrafficByProdTrafficBus(productId);
				if(prodTrafficGroupList != null && prodTrafficGroupList.size() > 0){
					for(ProdTrafficGroup ptg : prodTrafficGroupList){
						if(ptg.getProdTrafficBusList() != null && ptg.getProdTrafficBusList().size() > 0){
							travelContractVO.setProdTrafficBusList(ptg.getProdTrafficBusList());
						}
					}
				}
			}
			
			//产品名称
			travelContractVO.setProductName(productName);
			
			//订单编号
			travelContractVO.setOrderId(order.getOrderId().toString());
			//出发日期
			travelContractVO.setVistDate(DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd"));
			
			int routeDays =0;
			Map<String,Object> map=null;
			if (CollectionUtils.isNotEmpty(ordPackList)) {
				OrdOrderPack ordOrderPack=ordPackList.get(0);
				map = ordOrderPack.getContentMap();
			}else{
				map =order.getMainOrderItem().getContentMap();
			}
			Object routDayObj=map.get(OrderEnum.ORDER_PACK_TYPE.route_days.name());
			if (routDayObj!=null) {
			
				routeDays =  (Integer) map.get(OrderEnum.ORDER_PACK_TYPE.route_days.name());
			}
			
			//结束日期
			Date beginDate = order.getVisitTime();
			travelContractVO.setOverDate(DateUtil.formatDate(DateUtils.addDays(beginDate, routeDays-1), "yyyy-MM-dd"));
			
			
			//交通信息  
			//供应商打包，该订单是否包含大交通，是就直接去查产品信息。 任意一个子订单上获取产品id对应供应商打包产品的交通信息,取第一个参考。
			//自主打包去查询该订单包含的交通子订单上产品对应产品信息。  产品id对应自主打包产品的交通信息
			if(order.getLineRouteId() != null) {
				//1、	判断当前下单产品是否属于跟团游长线或者属于长线当地游
				//2、	根据orderId从mongodb中取对应的行程信息
				//3、	若取到行程信息，travelContractVO.setLineRoute(lineRouteFromMongo)同时设置travelContractVO.setIsNewRoute("Y"/”N”)，否则走原有逻辑
				boolean fromMongo = false;
				if ((BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId()) || 
						BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getCategoryId()))
						&& ProdProduct.PRODUCTTYPE.INNERLONGLINE.getCode().equals(prodProduct.getProductType())) {
					ProdLineRoute lineRouteResult = orderLinerouteSnapshotService.findOneLineRouteSnapShotByOrderId(order.getOrderId());
					if (lineRouteResult != null) {
						ProdLineRoute lineRoute = BuildRouteTemplateUtil.buildTemplate(lineRouteResult);
						travelContractVO.setLineRoute(lineRoute);
						fromMongo = true;
						if(ProductPreorderUtil.isNewRoute(prodProduct.getBizCategoryId(), prodProduct.getSubCategoryId()) &&
								!ProductPreorderUtil.isDestinationBUDetail(prodProduct)){
							travelContractVO.setIsNewRoute("Y");
						}else {
							travelContractVO.setIsNewRoute("N");
						}
					}else {
						LOG.error("orderLinerouteSnapshotService.findOneLineRouteSnapShotByOrderI result is null,order id = "+order.getOrderId());
					}
				}
				if (!fromMongo) {
					//如果是当地游或者跟团游 且不是目的地BU 则使用新行程结构，否则走老流程
					if(ProductPreorderUtil.isNewRoute(prodProduct.getBizCategoryId(), prodProduct.getSubCategoryId()) &&
							!ProductPreorderUtil.isDestinationBUDetail(prodProduct)){
						try {
							ProdLineRoute lineRouteResult = null;
							//行程使用新结构
							ResultHandleT<List<ProdLineRoute>> lineRouteResults = new ResultHandleT<List<ProdLineRoute>>();
							lineRouteResults = this.prodLineRouteClientService.findCacheLineRouteListByProductId(prodProduct.getProductId());
							if(CollectionUtils.isNotEmpty(lineRouteResults.getReturnContent())){
								for (ProdLineRoute lineRoute : lineRouteResults.getReturnContent()) {
									if(order.getLineRouteId().equals(lineRoute.getLineRouteId())){
										lineRouteResult = lineRoute;
									}
								}
							}
							if(lineRouteResult != null) {
								//BuildRouteTemplateUtil.buildTemplate拼装话术模板
								travelContractVO.setLineRoute(BuildRouteTemplateUtil.buildTemplate(lineRouteResult));
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
						}
						travelContractVO.setIsNewRoute("N");
					}
					
				}
			}
			
			//自愿购物活动补充协议 自愿参加另行付费旅游项目补充协议
			List<ProdContractDetail> contractDetailList= getProdContractDetails(travelContractVO.getLineRouteId());
			if (CollectionUtils.isNotEmpty(contractDetailList)) {

				for (ProdContractDetail prodContractDetail : contractDetailList) {
					Date vistStartTime = null;
					Short dayShort = prodContractDetail.getnDays();
					if(dayShort != null && dayShort != 0){
						vistStartTime = DateUtils.addDays(order.getVisitTime(), dayShort.intValue()-1);
					}
					prodContractDetail.setVistStartTime(vistStartTime);
					
				}
			}
			
			//费用包含 不包含   出行警示  退改说明
			Map<String, String> propValueMap=new HashMap<String, String>();
			Map<String, Object> productPropMap =prodProduct.getPropValue();
			if(productPropMap == null) {
				productPropMap = new HashMap<String, Object>();
			}
			//获取费用包含&费用不包含，放进productPropMap
			productPropMap.putAll(getCostIncExc(travelContractVO.getLineRouteId()));
			
			if (productPropMap != null && !productPropMap.isEmpty()) {
				String code = null;
				String value = null;
				String cnName = null;
				
				for (Entry<String, Object> entry : productPropMap.entrySet()) {
					
					if (entry != null) {
						code = entry.getKey();
						if (entry.getValue()==null || !entry.getValue().getClass().equals(String.class)) {
							continue;
						}
						value = (String) entry.getValue();
						
						cnName = BizEnum.LINE_PROP_CODE.getCnName(code);
						if (cnName.equals(code)) {
							continue;
						}
						
						propValueMap.put(code, value);
						
					}
				}			
			}
			//不管产品设置费用包含不包含与否均为合同VO中设置propValueMap
			travelContractVO.setPropValueMap(propValueMap);
			
			
			List<BizDict> hotelStarList=dictClientService.findDictListByDefId(515L).getReturnContent();;
			travelContractVO.setHotelStarList(hotelStarList);
			
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
			
			// 委托组团时设置代理方，不是委托组团时设置组团社
			travelContractVO.setFilialeName(this.filialeNameMap.get(order.getFilialeName()));
			if (order.getCompanyType() != null 
	                && COMPANY_TYPE_DIC.GUOLV.name().equals(order.getCompanyType().trim())) {
	            travelContractVO.setFilialeName(GUOLV_filialeName); // 国旅-旅行社名称
	        }
	        // 如果当前合同的合同主体不是国旅且所属分公司也不是5大分公司时，默认设置为SH_FILIALE，
	        if(StringUtils.isEmpty(travelContractVO.getFilialeName())) {
	            travelContractVO.setFilialeName(filialeNameMap.get(CommEnumSet.FILIALE_NAME.SH_FILIALE.getCode()));
	        }
		}	
		filteHtmlTag(prodProduct, travelContractVO);
		List<String> priceIncludesList = new ArrayList<String>();//费用包含
		List<String> priceNotIncludesList = new ArrayList<String>();//费用不含
		List<String> travelWarningsList = new ArrayList<String>();//出行警示及说明
		List<String> backToThatList = new ArrayList<String>();//退改说明
		
		String[] priceIncludesArr = travelContractVO.getPriceIncludes().split("\r\n");
		if (priceIncludesArr != null && priceIncludesArr.length > 0) {
			for (int i = 0; i < priceIncludesArr.length; i++) {
				priceIncludesList.add(priceIncludesArr[i]);
			}
			travelContractVO.setPriceIncludesList(priceIncludesList);
		}
		String[] travelWarningsArr = travelContractVO.getTravelWarnings().split("\r\n");
	    if(travelWarningsArr != null && travelWarningsArr.length > 0 ){
	    	for(int i = 0; i < travelWarningsArr.length; i++){
	    		travelWarningsList.add(travelWarningsArr[i]);
	    	}
	    	travelContractVO.setTravelWarningsList(travelWarningsList);
	    }
		String[] backToThatArr = travelContractVO.getBackToThat().split("\r\n");
		if (backToThatArr != null && backToThatArr.length > 0) {
			for (int i = 0; i < backToThatArr.length; i++) {
				backToThatList.add(backToThatArr[i]);
			}
			travelContractVO.setBackToThatList(backToThatList);
		}
	    String[] priceNotIncludesArr = travelContractVO.getPriceNotIncludes().split("\r\n");
	    if(priceNotIncludesArr != null && priceNotIncludesArr.length > 0 ){
	    	for(int i = 0; i < priceNotIncludesArr.length; i++){
	    		priceNotIncludesList.add(priceNotIncludesArr[i]);
	    	}
	    	travelContractVO.setPriceNotIncludesList(priceNotIncludesList);
	    }
		//删除行程明细中其他活动的html标签内容
		this.modifyActivityDesc(travelContractVO);
		return travelContractVO;
	}
	
	public void modifyActivityDesc(TravelContractVO travelContractVO) {
		ProdLineRoute lineRoute = travelContractVO.getLineRoute();
		if (null != lineRoute) {
			List<ProdLineRouteDetail> prodLineRouteDetailList = lineRoute.getProdLineRouteDetailList();
			if (CollectionUtils.isNotEmpty(prodLineRouteDetailList)) {
				for (ProdLineRouteDetail prodLineRouteDetail : prodLineRouteDetailList) {
					List<ProdRouteDetailGroup> prodRouteDetailGroupList = prodLineRouteDetail.getProdRouteDetailGroupList();
					if (CollectionUtils.isNotEmpty(prodRouteDetailGroupList)) {
						for (ProdRouteDetailGroup group : prodRouteDetailGroupList) {
							List<ProdRouteDetailActivity> activityList = group.getProdRouteDetailActivityList();
							if (CollectionUtils.isNotEmpty(activityList)) {
								for (ProdRouteDetailActivity activity : activityList) {
									activity.setActivityDesc(deleteHtmlString(activity.getActivityDesc()));
									LOG.info("activityDesc=="+activity.getActivityDesc());
								}
							}
						}
					}

				}
			}
		}
	}

	/**
	 * @param str
	 * @return 删除Html标签
	 */
	public static String deleteHtmlString(String htmlStr) {
		if(StringUtils.isEmpty(htmlStr)){
			return "";
		}
		Pattern p_html = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);
		Matcher m_html = p_html.matcher(htmlStr);
		htmlStr = m_html.replaceAll("").replaceAll(" ", "");
		return htmlStr;
	}

	private void filteHtmlTag(ProdProduct product,TravelContractVO travelContractVO)
	{
		if(travelContractVO.getLineRoute() != null) {
			List<ProdLineRouteDetail> routeDetailList = travelContractVO.getLineRoute().getProdLineRouteDetailList();
			if(CollectionUtils.isNotEmpty(routeDetailList)){
				for (ProdLineRouteDetail prodLineRouteDetail : routeDetailList) {
					prodLineRouteDetail.setTitle(StringUtil.filterOutHTMLTags(prodLineRouteDetail.getTitle()));
					prodLineRouteDetail.setContent(StringUtil.filterOutHTMLTags(prodLineRouteDetail.getContent()));
					prodLineRouteDetail.setBreakfastDesc(StringUtil.filterOutHTMLTags(prodLineRouteDetail.getBreakfastDesc()));
					prodLineRouteDetail.setLunchDesc(StringUtil.filterOutHTMLTags(prodLineRouteDetail.getLunchDesc()));
					prodLineRouteDetail.setDinnerDesc(StringUtil.filterOutHTMLTags(prodLineRouteDetail.getDinnerDesc()));
					prodLineRouteDetail.setStayDesc(StringUtil.filterOutHTMLTags(prodLineRouteDetail.getStayDesc()));
				}
			}
		}
		travelContractVO.setPriceIncludes(StringUtil.filterOutHTMLTags(travelContractVO.getPriceIncludes()));
		travelContractVO.setPriceNotIncludes(StringUtil.filterOutHTMLTags(travelContractVO.getPriceNotIncludes()));
		travelContractVO.setBackToThat(StringUtil.filterOutHTMLTags(travelContractVO.getBackToThat()));
		travelContractVO.setMemo(StringUtil.filterOutHTMLTags(travelContractVO.getMemo()));
		travelContractVO.setGroupWayOtherContent(StringUtil.filterOutHTMLTags(travelContractVO.getGroupWayOtherContent()));
		travelContractVO.setGuideTelephone(StringUtil.filterOutHTMLTags(travelContractVO.getGuideTelephone()));
		travelContractVO.setEmergencyTelephone(StringUtil.filterOutHTMLTags(travelContractVO.getEmergencyTelephone()));
		travelContractVO.setLvmamaEmergencyTelephone(StringUtil.filterOutHTMLTags(travelContractVO.getLvmamaEmergencyTelephone()));
		travelContractVO.setLvmamaQualityPhone(StringUtil.filterOutHTMLTags(travelContractVO.getLvmamaQualityPhone()));
		travelContractVO.setOrderTraffic(StringUtil.filterOutHTMLTags(travelContractVO.getOrderTraffic())) ;
		travelContractVO.setTravelWarnings(StringUtil.filterOutHTMLTags(travelContractVO.getTravelWarnings()));

		for (OrdOrderItem orderItem : travelContractVO.getOrderItemList()) {
			orderItem.setProductName(StringUtil.filterOutHTMLTags(orderItem.getProductName()));
			orderItem.setDeductType(StringUtil.filterOutHTMLTags(orderItem.getDeductType()));
			orderItem.setOrderMemo(StringUtil.filterOutHTMLTags(orderItem.getOrderMemo()));
		}
		travelContractVO.setProdProduct(product);
	}

	


	
}
