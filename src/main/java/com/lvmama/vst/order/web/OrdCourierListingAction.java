package com.lvmama.vst.order.web;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lvmama.vst.comm.utils.web.HttpServletLocalThread;
import net.sf.jxls.transformer.XLSTransformer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.dist.service.DistributorClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.dist.po.Distributor;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoods.EXPRESSTYPE;
import com.lvmama.vst.back.order.po.OrdCourierListing;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.COURIER_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.INFO_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.NOTICE_REGIMENT_STATUS_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.PAYMENT_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.RESOURCE_STATUS;
import com.lvmama.vst.comm.enumeration.CommEnumSet.BU_NAME;
import com.lvmama.vst.comm.enumeration.CommEnumSet.FILIALE_NAME;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.ResourceUtil;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderMonitorCnd;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.comm.vo.order.OrderSortParam;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdAdditionStatusService;
import com.lvmama.vst.order.service.IOrdOrderItemService;

/**
 * 快递寄件清单
 * 
 * @author zhangwei
 *
 */
@Controller
public class OrdCourierListingAction extends BaseActionSupport {
	// 日志记录器
	private static final Log LOG = LogFactory.getLog(OrdCourierListingAction.class);
	
	// 默认分页大小配置名称
	private final Integer DEFAULT_PAGE_SIZE = 10; 
	
	/**
	 * 导出excel对应模板文件地址
	 */
	public static final String ORD_COURIERLISTING_TEMPLATE_PATH = "/WEB-INF/resources/ordCourierListing/ordCourierListingListTemplate.xls";
	
	
	// 注入综合查询业务接口
	@Autowired
	private IComplexQueryService complexQueryService;
	// 注入分销商业务接口(订单来源、下单渠道)
	@Autowired
	private DistributorClientService distributorClientService;
	//注入供应商业务接口
	@Autowired
	private SuppSupplierClientService suppSupplierClientService;
	//产品类型业务接口
	@Autowired
	private CategoryClientService categoryClientService;
	
	@Autowired
	private IOrdAdditionStatusService ordAdditionStatusService;
	@Autowired
	private IOrdOrderItemService ordOrderItemService;
	
	
	/**
	 * 进入订单监控列表
	 * 
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/intoOrdCourierListingQuery.do")
	public String intoOrderMonitor(Model model,OrderMonitorCnd monitorCnd,HttpServletRequest request) throws BusinessException{
		// 初始化查询表单,给字典项赋值
		initQueryForm(model,request);
		model.addAttribute("monitorCnd",monitorCnd);
		model.addAttribute("pageParam", new Page<OrderMonitorRst>());
		return "/order/query/ordCourierListingList";
	}

	

	
	/**
	 * 新订单监控综合查询
	 * 
	 * @param model
	 * @param page
	 * @param pageSize
	 * @param monitorCnd
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/ord/order/ordCourierListingList.do")
	public String orderMonitorList(Model model, Integer page,Integer pageSize,OrderMonitorCnd monitorCnd, HttpServletRequest request) throws BusinessException {
		// 初始化查询表单,给字典项赋值
		initQueryForm(model,request);
		Page<OrderMonitorRst> pageParam =null;
		List<OrderMonitorRst> resultList = this.getResultList(monitorCnd, page,pageParam,pageSize,request,false);
		
		// 查询结果,按照order分组封装
		Map<String, List<OrderMonitorRst>> resultMap = new HashMap<String, List<OrderMonitorRst>>();
		if (CollectionUtils.isNotEmpty(resultList)) {
			for (OrderMonitorRst item : resultList) {
				List<OrderMonitorRst> tempList = resultMap.get(String.valueOf(item.getOrderId()));
				if (tempList == null) {
					tempList = new ArrayList<OrderMonitorRst>();
				}
				tempList.add(item);
				resultMap.put(String.valueOf(item.getOrderId()), tempList);
			}
		}
	
		// 查询条件回显
		model.addAttribute("monitorCnd", monitorCnd);
		model.addAttribute("resultList", resultList);
		model.addAttribute("resultMap", resultMap);
		return "/order/query/ordCourierListingList";
	}
	
	@RequestMapping(value = "/ord/order/finishCourier")
	@ResponseBody
	public ResultMessage finishCourier(Long orderItemId, String courierStatus){
		String newStatus = ordOrderItemService.updateCourierStatus(orderItemId, courierStatus);
		if (newStatus != null) {
			return new ResultMessage(ResultMessage.SUCCESS, newStatus);
		}
		return new ResultMessage(ResultMessage.ERROR, newStatus);
	}
	
	
	
	/**
     * 导出excel数据
     * @author 张伟
     */ 
	@RequestMapping(value = "/ord/order/exportOrdCourierListingExcelData")
	public void exportExcelData(Model model, Integer page,Integer pageSize,OrderMonitorCnd monitorCnd, HttpServletRequest request, HttpServletResponse response) throws BusinessException {
			
		/*if (monitorCnd.getOrderId()==null) {
			this.sendAjaxMsg("订单id为空", request, response);
			return ;
		}*/
		
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		
		
//		List<OrdOrder> orderList = getOrderListCondition(monitorCnd);
		
		Page<OrderMonitorRst> pageParam =null;
		resultList=this.getResultList(monitorCnd, page,pageParam,pageSize,request,true);
		/*if (!CollectionUtils.isEmpty(orderList)) {
			
			resultList=this.getResultList(orderList, page,pageParam,request);
			
			
		}*/

		Map<String, Object> beans = new HashMap<String, Object>();
		
		// excel模板中，显示逻辑处理
		if (CollectionUtils.isNotEmpty(resultList)) {
			for (OrderMonitorRst resultItem : resultList) {
				for (BU_NAME item : BU_NAME.values()) {
					if (resultItem.getBelongBU() != null && item.name().equals(resultItem.getBelongBU())) {
						resultItem.setBelongBU(item.getCnName());
					}
				}
				for (COURIER_STATUS item : COURIER_STATUS.values()) {
					if (resultItem.getCourierStatus() != null && item.name().equals(resultItem.getCourierStatus())) {
						resultItem.setCourierStatus(item.desc());
					}
				}
			}
		}
		
		beans.put("resultList", resultList);
//		beans.put("seoTypeName", BizSeoFriendLink.SEO_TYPE.getCnName(seoType));
		
		String destFileName = writeExcelByjXls(beans, ORD_COURIERLISTING_TEMPLATE_PATH);
		writeAttachment(destFileName, "ordCourierListingExcel" + DateUtil.formatDate(new Date(), "yyyy MM dd"), response);
	
	}
	/**
  	 * 写excel通过模板 bean
  	 * @param beans
  	 * @param template
  	 * @return
  	 * @throws Exception
  	 */
	public static String writeExcelByjXls(Map<String,Object> beans, String template){
		try {
			File templateResource = ResourceUtil.getResourceFile(template);
			XLSTransformer transformer = new XLSTransformer();
			String destFileName = getTempDir() + "/excel" + new Date().getTime()+".xls";
			transformer.transformXLS(templateResource.getAbsolutePath(), beans, destFileName);
			return destFileName;
		}catch(Exception e){
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		return null;
	}
	
	public static String getTempDir() {
		return System.getProperty("java.io.tmpdir");
	}
	
	
	private List<OrderMonitorRst> getResultList(
			OrderMonitorCnd monitorCnd, Integer page,
			Page<OrderMonitorRst> pageParam,Integer pageSize,
			HttpServletRequest request,boolean isExportExcel) {

		// 根据页面条件组装综合查询接口条件
		ComplexQuerySQLCondition condition = buildQueryConditionForMonitor(
				page, pageSize, monitorCnd,isExportExcel);

		// 根据条件获取订单集合
		List<OrdOrder> orderList = complexQueryService
				.queryOrderListByCondition(condition);

		if (LOG.isDebugEnabled()) {
			if (null != orderList && orderList.size() > 0) {
				LOG.debug("orderList=="
						+ ToStringBuilder.reflectionToString(orderList.get(0),
								ToStringStyle.MULTI_LINE_STYLE));
			}
		}

		// 根据条件获取订单总记录数
		Long totalCount = complexQueryService
				.queryOrderCountByCondition(condition);

		if (LOG.isDebugEnabled()) {
			LOG.debug("totalCount==" + totalCount);
		}

		// 根据页面展示特色组装其想要的结果
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		if (null != orderList && orderList.size() > 0) {
			resultList = buildQueryResult(orderList, request, monitorCnd);
		}

		if (LOG.isDebugEnabled()) {
			if (null != resultList && resultList.size() > 0) {
				LOG.debug("resultList=="
						+ ToStringBuilder.reflectionToString(resultList.get(0),
								ToStringStyle.MULTI_LINE_STYLE));
			}
		}

		// 组装分页结果
		@SuppressWarnings("rawtypes")
		Page resultPage = buildResultPage(resultList, page, null, new Long(
				totalCount), request);
		HttpServletLocalThread.getModel().addAttribute("pageParam", resultPage);

		// }

		return resultList;

	}
	/**
	 * FORM表单初始化
	 * 
	 * @param model
	 */
	private void initQueryForm(Model model,HttpServletRequest request) throws BusinessException {
		Map<String, String> expresstypeMap = new LinkedHashMap<String, String>();
		
		StringBuffer buffer=new StringBuffer();
		for (EXPRESSTYPE item : EXPRESSTYPE.values()) {
			if (buffer.length()>0) {
				buffer.append(",");
			}
			buffer.append("'");
			buffer.append(item.getCode());
			buffer.append("'");
		}
		expresstypeMap.put(buffer.toString(), "全部");
		for (EXPRESSTYPE item : EXPRESSTYPE.values()) {
			expresstypeMap.put("'"+item.getCode()+"'", item.getCnName());
		}
		model.addAttribute("expresstypeMap", expresstypeMap);
		
		// BU
		HashMap<String, String> belongBUMap = new HashMap<String, String>();
		belongBUMap.put("", "全部");
		for (BU_NAME item : BU_NAME.values()) {
			belongBUMap.put(item.name(), item.getCnName());
		}
		model.addAttribute("belongBUMap", belongBUMap);
		// 快递状态
		HashMap<String, String> courierStatusMap = new HashMap<String, String>();
		courierStatusMap.put("", "全部");
		for (COURIER_STATUS item : COURIER_STATUS.values()) {
			courierStatusMap.put(item.name(), item.desc());
		}
		model.addAttribute("courierStatusMap", courierStatusMap);
		
	}
	/**
	 * FORM表单初始化
	 * 
	 * @param model
	 */
	private void initShipQueryForm(Model model,HttpServletRequest request) throws BusinessException {
		// 订单状态字典
		Map<String, String> orderStatusMap = new LinkedHashMap<String, String>();
		orderStatusMap.put("", "全部");
		for (ORDER_STATUS item : ORDER_STATUS.values()) {
			orderStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("orderStatusMap", orderStatusMap);

		// 信息状态字典
		Map<String, String> infoStatusMap = new LinkedHashMap<String, String>();
		infoStatusMap.put("", "全部");
		for (INFO_STATUS item : INFO_STATUS.values()) {
			infoStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("infoStatusMap", infoStatusMap);

		// 资源状态字典
		Map<String, String> resourceStatusMap = new LinkedHashMap<String, String>();
		resourceStatusMap.put("", "全部");
		for (RESOURCE_STATUS item : RESOURCE_STATUS.values()) {
			resourceStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("resourceStatusMap", resourceStatusMap);

		// 支付状态字典	
		Map<String, String> paymentStatusMap = new LinkedHashMap<String, String>();
		paymentStatusMap.put("", "全部");
		for (PAYMENT_STATUS item : PAYMENT_STATUS.values()) {
			paymentStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("paymentStatusMap", paymentStatusMap);
		
		
		// 合同状态字典	
		Map<String, String> contractStatusMap = new LinkedHashMap<String, String>();
		contractStatusMap.put("", "全部");
		for (ORDER_TRAVEL_CONTRACT_STATUS item : ORDER_TRAVEL_CONTRACT_STATUS.values()) {
			contractStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("contractStatusMap", contractStatusMap);
		
		// 出团通知书状态字典	
		Map<String, String> noticeRegimentStatusMap = new LinkedHashMap<String, String>();
		noticeRegimentStatusMap.put("", "全部");
		for (NOTICE_REGIMENT_STATUS_TYPE item : NOTICE_REGIMENT_STATUS_TYPE.values()) {
			noticeRegimentStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("noticeRegimentStatusMap", noticeRegimentStatusMap);
				
				
		//产品类型字典
		List<BizCategory> productCategoryList = categoryClientService.findCategoryByAllValid().getReturnContent();
		Map<String, String> productCategoryMap = new LinkedHashMap<String, String>();
		productCategoryMap.put("", "全部");
		if(null!=productCategoryList && productCategoryList.size()>0){
			for(BizCategory category:productCategoryList){
				productCategoryMap.put(category.getCategoryId()+"", category.getCategoryName());
			}
		}
		model.addAttribute("productCategoryMap", productCategoryMap);

		//下单渠道
		List<Distributor> distributorList = distributorClientService.findDistributorList(new HashMap<String, Object>()).getReturnContent();
		Map<String, String> distributorMap = new HashMap<String, String>();
		for(Distributor distributor:distributorList){
			distributorMap.put(distributor.getDistributorId()+"", distributor.getDistributorName());
		}
		request.setAttribute("distributorMap", distributorMap);
		
		//分公司字典
		Map<String, String> filialeMap = new LinkedHashMap<String, String>();
		for (FILIALE_NAME item : FILIALE_NAME.values()) {
			filialeMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("filialeMap", filialeMap);
		
		//商品支付方式
		Map<String, String> payTargetMap = new LinkedHashMap<String, String>();
		payTargetMap.put("", "全部");
		for(SuppGoods.PAYTARGET payTarget:SuppGoods.PAYTARGET.values()){
			payTargetMap.put(payTarget.name(), payTarget.getCnName());
		}
		model.addAttribute("payTargetMap", payTargetMap);
		
		//确认单凭证
		Map<String, String> certConfirmStatusMap = new LinkedHashMap<String, String>();
		certConfirmStatusMap.put("", "全部");
		for(OrderEnum.CERT_CONFIRM_STATUS certConfirmStatus:OrderEnum.CERT_CONFIRM_STATUS.values()){
			certConfirmStatusMap.put(certConfirmStatus.name(), certConfirmStatus.getCnName());
		}
		model.addAttribute("certConfirmStatusMap", certConfirmStatusMap);
	}

	/**
	 * 新订单监控查询条件封装
	 * 
	 * @param currentPage
	 * @param pageSize
	 * @param monitorCnd
	 * @return
	 */
	private ComplexQuerySQLCondition buildQueryConditionForMonitor(Integer page, Integer pageSize, OrderMonitorCnd monitorCnd,boolean isExportExcel) {
		//检查页面条件封装信息
		if(LOG.isDebugEnabled()){
			LOG.debug(" order monitor cnd "+monitorCnd);
		}
		//保证每次请求都是一个新的对象
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		//组装订单内容类条件
		condition.getOrderContentParam().setBackUserId(monitorCnd.getBackUserId());
		condition.getOrderContentParam().setBookerMobile(monitorCnd.getBookerMobile());
		condition.getOrderContentParam().setBookerName(monitorCnd.getBookerName());
		condition.getOrderContentParam().setContactEmail(monitorCnd.getContactEmail());
		condition.getOrderContentParam().setContactMobile(monitorCnd.getContactMobile());
		condition.getOrderContentParam().setContactName(monitorCnd.getContactName());
		condition.getOrderContentParam().setContactPhone(monitorCnd.getContactPhone());
		condition.getOrderContentParam().setFilialeNames(monitorCnd.getFilialeNames());
		condition.getOrderContentParam().setProductName(monitorCnd.getProductName());
		condition.getOrderContentParam().setSuppGoodsName(monitorCnd.getSuppGoodsName());
		condition.getOrderContentParam().setTravellerName(monitorCnd.getTravellerName());
		condition.getOrderContentParam().setUserId(monitorCnd.getUserId());
		condition.getOrderContentParam().setPayTarget(monitorCnd.getPayTarget());
		condition.getOrderContentParam().setBelongBU(monitorCnd.getBelongBU());
		condition.getOrderContentParam().setCourierStatus(monitorCnd.getCourierStatus());

		HashSet<Long> set=new HashSet<Long>();
		if (monitorCnd.getOrderId()!=null) {
			set.add(monitorCnd.getOrderId());
		}
		condition.getOrderContentParam().setOrderIds(set);
		condition.getOrderContentParam().setPersoType(OrderEnum.ORDER_PERSON_TYPE.ADDRESS.name());
		condition.getOrderContentParam().setExpressType(monitorCnd.getExpresstype());
		
		
		
		//组装订单标志类条件
		condition.getOrderFlagParam().setOrderTableFlag(true);//获得订单号
		condition.getOrderFlagParam().setOrderItemTableFlag(true);//获得产品名称
		condition.getOrderFlagParam().setOrderPersonTableFlag(true);//获得联系人
		condition.getOrderFlagParam().setOrderAddressTableFlag(true);
		condition.getOrderFlagParam().setOrdCourierListing(true);
		
		
		
	

		//组装订单ID类条件
		condition.getOrderIndentityParam().setCategoryId(monitorCnd.getCategoryId());
		condition.getOrderIndentityParam().setDistributorIds(monitorCnd.getDistributorIds());
		condition.getOrderIndentityParam().setOrderId(monitorCnd.getOrderId());
		condition.getOrderIndentityParam().setProductId(monitorCnd.getProductId());
		condition.getOrderIndentityParam().setSupplierId(monitorCnd.getSupplierId());
		
		condition.getOrderIndentityParam().setOrderItemId(monitorCnd.getOrderItemId());
		

		//组装订单排序类条件
		condition.getOrderSortParams().add(OrderSortParam.CREATE_TIME_DESC);

		//组装订单状态类条件
		condition.getOrderStatusParam().setOrderStatus(monitorCnd.getOrderStatus());
		condition.getOrderStatusParam().setInfoStatus(monitorCnd.getInfoStatus());
		condition.getOrderStatusParam().setResourceStatus(monitorCnd.getResourceStatus());
		condition.getOrderStatusParam().setPaymentStatus(monitorCnd.getPaymentStatus());
		condition.getOrderStatusParam().setCertConfirmStatus(monitorCnd.getCertConfirmStatus());

		condition.getOrderStatusParam().setContractStatus(monitorCnd.getContractStatus());
		condition.getOrderStatusParam().setNoticeRegimentStatus(monitorCnd.getNoticeRegimentStatus());
		
		//组装订单时间类条件
		if(!StringUtils.isEmpty(monitorCnd.getCreateTimeBegin())){
			condition.getOrderTimeRangeParam().setCreateTimeBegin(DateUtil.toDate(monitorCnd.getCreateTimeBegin(), "yyyy-MM-dd HH:mm:ss"));
		}
		if(!StringUtils.isEmpty(monitorCnd.getCreateTimeEnd())){
			condition.getOrderTimeRangeParam().setCreateTimeEnd(DateUtil.toDate(monitorCnd.getCreateTimeEnd(), "yyyy-MM-dd HH:mm:ss"));
		}
		condition.getOrderTimeRangeParam().setPaymentTimeBegin(monitorCnd.getPaymentTimeBegin());
		condition.getOrderTimeRangeParam().setPaymentTimeEnd(monitorCnd.getPaymentTimeEnd());
		condition.getOrderTimeRangeParam().setVisitTimeBegin(monitorCnd.getVisitTimeBegin());
		condition.getOrderTimeRangeParam().setVisitTimeEnd(monitorCnd.getVisitTimeEnd());

		
		if(!isExportExcel){
			condition.getOrderFlagParam().setOrderPageFlag(true);//需要分页
			//组装订单分页类条件
			Integer currentPage = page == null ? 1 : page;
			Integer currentPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
			//计算出每页的rownum
			condition.getOrderPageIndexParam().setBeginIndex((currentPage-1)*currentPageSize+1);
			condition.getOrderPageIndexParam().setEndIndex(currentPage*currentPageSize);
		}else{
			condition.getOrderFlagParam().setOrderPageFlag(false);//不需要分页
		}
		return condition;
	}
	
	/**
	 * 组装页面上想要的结果
	 * 
	 * @param orderList
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<OrderMonitorRst> buildQueryResult(List<OrdOrder> orderList,HttpServletRequest request, OrderMonitorCnd monitorCnd) {
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		Map<String, String> distributorMap = (Map<String, String>)request.getAttribute("distributorMap");
		for (OrdOrder order : orderList) {
			
			HashMap<Long, OrdOrderItem> map=new HashMap<Long,OrdOrderItem>();
			List<OrdOrderItem> orderItemList=order.getOrderItemList();
			for (OrdOrderItem ordOrderItem : orderItemList) {
				map.put(ordOrderItem.getOrderItemId(), ordOrderItem);
			}
			List<OrdCourierListing> ordCourierListingList=order.getOrdCourierListingList();
			for (OrdCourierListing ordCourierListing : ordCourierListingList) {

				Long orderItemId = ordCourierListing.getOrderItemId();
				OrdOrderItem ordOrderItem = map.get(orderItemId);
				
				// 综合查询BU参数处理
				if (StringUtils.isNotBlank(monitorCnd.getBelongBU())) {
					if (!monitorCnd.getBelongBU().equals(ordOrderItem.getBuCode())) {
						continue;
					}
				}
				// 综合查询CourierStatus参数处理
				if (StringUtils.isNotBlank(monitorCnd.getCourierStatus())) {
					if (COURIER_STATUS.Y.name().equals(monitorCnd.getCourierStatus().trim())) {
						if (!(ordOrderItem.getCourierStatus() != null && COURIER_STATUS.Y.name().equals(ordOrderItem.getCourierStatus().trim()))) {
							continue;
						}
					} else if (COURIER_STATUS.N.name().equals(monitorCnd.getCourierStatus().trim())) {
						if (!( ordOrderItem.getCourierStatus() == null || COURIER_STATUS.N.name().equals(ordOrderItem.getCourierStatus().trim()) )) {
							continue;
						}
					}
				}
				
				OrderMonitorRst orderMonitorRst = new OrderMonitorRst();
				// 将订单来源转化为名称显示
				if (null != distributorMap) {
					if (distributorMap.containsKey(order.getDistributorId()
							+ "")) {
						orderMonitorRst.setDistributorName(distributorMap
								.get(order.getDistributorId() + ""));
					} else {
						orderMonitorRst.setDistributorName(order
								.getDistributorId() + "");
					}
				} else {
					orderMonitorRst.setDistributorName(order.getDistributorId()
							+ "");
				}
				orderMonitorRst.setOrderId(order.getOrderId());
				orderMonitorRst.setOrderItemId(ordOrderItem.getOrderItemId());
				orderMonitorRst.setProductName(this
						.buildProductName(ordOrderItem));
				orderMonitorRst.setBuyCount(this.buildBuyCount(ordOrderItem));
				orderMonitorRst.setCreateTime(this.buildCreateTime(order));
				orderMonitorRst.setVisitTime(this.buildVisitTime(order));
				orderMonitorRst.setContactName(this.buildContactName(order));
				orderMonitorRst
						.setCurrentStatus(this.buildCurrentStatus(order));

				orderMonitorRst.setPayTarget(SuppGoods.PAYTARGET
						.getCnName(order.getPaymentTarget()));
				orderMonitorRst.setGuarantee(order.getGuarantee());
				orderMonitorRst.setOrderPackList(order.getOrderPackList());

				orderMonitorRst.setAddressPerson(order.getAddressPerson());
				orderMonitorRst.setOrdAddress(order.getOrdAddress());
				
				orderMonitorRst.setOrderStatus(order.getOrderStatus());
				orderMonitorRst.setBelongBU(ordOrderItem.getBuCode());
				orderMonitorRst.setCourierStatus(ordOrderItem.getCourierStatus());
				
				resultList.add(orderMonitorRst);

			}

		}
		return resultList;
	}

	
	/**
	 * 组装分页对象
	 * 
	 * @author wenzhengtao
	 * @param model
	 * @param currentPage
	 * @param totalCount
	 * @param request
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Page buildResultPage(List list, Integer currentPage, Integer pageSize, Long totalCount, HttpServletRequest request) {
		// 如果当前页是空，默认为1
		Integer currentPageTmp = currentPage == null ? 1 : currentPage;
		// 从配置文件读取分页大小
		Integer defaultPageSize = DEFAULT_PAGE_SIZE;
		Integer pageSizeTmp = pageSize == null ? defaultPageSize : pageSize;
		// 构造分页对象
		Page page = Page.page(totalCount, pageSizeTmp, currentPageTmp);
		// 构造分页URL
		page.buildUrl(request);
		// 设置结果集
		page.setItems(list);
		return page;
	}

	/**
	 * 根据订单和订单子项一对多的关系构建多个商品名称
	 * 
	 * @param orderList
	 * @return
	 */
	private String buildProductName(OrdOrderItem orderItem ) {
		String productName = "未知产品名称";
		if (null != orderItem) {
			productName = orderItem.getProductName()+orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.branchName.name())+orderItem.getSuppGoodsName();
		}
		return productName;
	}

	/**
	 * 构建订单购买商品数量
	 * 
	 * @param order
	 * @return
	 */
	private Integer buildBuyCount(OrdOrder order) {
		Integer buyCount = 0;
		OrdOrderItem orderItem = order.getMainOrderItem();
		if (null != orderItem) {
			buyCount = orderItem.getQuantity().intValue();
		}
		return buyCount;
	}

    private Integer buildBuyCount(OrdOrderItem orderItem) {
        Integer buyCount = 0;
        if (null != orderItem) {
            buyCount = orderItem.getQuantity().intValue();
        }
        return buyCount;
    }

	/**
	 * 处理下单时间
	 * 
	 * @param order
	 * @return
	 */
	private String buildCreateTime(OrdOrder order) {
		String createTimeStr = "未知下单时间";
		Date createTime = order.getCreateTime();
		if (null != createTime) {
			// 保留年月日时分
			createTimeStr = DateUtil.formatDate(createTime, "yyyy-MM-dd HH:mm");
		}
		return createTimeStr;
	}

	/**
	 * 根据订单和订单子项一对多的关系构建多条游玩和入住时间
	 * 
	 * @param order
	 * @return
	 */
	private String buildVisitTime(OrdOrder order) {
		String visitTime = "未知日期";
		OrdOrderItem orderItem = order.getMainOrderItem();
		if(null != orderItem){
			List<OrdOrderHotelTimeRate> orderHotelTimeRate = orderItem.getOrderHotelTimeRateList();
			String firstDay = DateUtil.formatDate(orderItem.getVisitTime(), "yyyy-MM-dd");
			visitTime = firstDay;
			if(null != orderHotelTimeRate && orderHotelTimeRate.size()>0){
				String lastDay = DateUtil.formatDate(DateUtil.dsDay_Date(orderItem.getVisitTime(), orderHotelTimeRate.size()),"yyyy-MM-dd");
				//visitTime = firstDay+"/"+lastDay;
				visitTime +="<br>"+lastDay;
			}
		}
		return visitTime;
	}

	
	/**
	 * 
	 * @param order
	 * @return
	 */
	private String buildShipVisitTime(OrdOrder order) {
		String visitTime = "未知日期";
		List<OrdOrderPack> orderPackList=order.getOrderPackList();
		if (!orderPackList.isEmpty()) {
			String firstDay = DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd");
			OrdOrderPack ordOrderPack=orderPackList.get(0);
			//产品名称  ORD_ORDER_PACK
			//上船地点     下船地点 	 所属航线
			Map<String,Object> orderPackContentMap = ordOrderPack.getContentMap();
			visitTime = firstDay+"<br>"+orderPackContentMap.get(OrderEnum.ORDER_PACK_TYPE.end_sailing_date.name());
			
		}
		return visitTime;
	}

	/**
	 * 处理联系人
	 * 
	 * @param order
	 * @return
	 */
	private String buildContactName(OrdOrder order) {
		String contactPerson = "";
		OrdPerson orderPerson = order.getContactPerson();
		if (orderPerson==null) {//无联系人默认第一个游客为联系人
			orderPerson=order.getFirstTravellerPerson();
		}
		if(null != orderPerson){
			if(UtilityTool.isValid(orderPerson.getFullName()) && !orderPerson.getFullName().contains("null")){
				contactPerson = orderPerson.getFullName();
			}else{
				//如果没有显示手机号
				contactPerson = orderPerson.getMobile();
			}
		}
		return contactPerson;
	}
	
	/**
	 * 出游人数
	 * 
	 * @param order
	 * @return
	 */
	private int buildTourisCount(OrdOrder order) {
		
		int travellerNum=0;
		//游客列表展示
		for (OrdPerson ordPerson : order.getOrdPersonList()) {

			String personType = ordPerson.getPersonType();
			if (OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equals(
					personType)) {
				travellerNum+=1;
			}

		}
		return travellerNum;
	}
	
	
	/**
	 * 处理联系人email
	 * 
	 * @param order
	 * @return
	 */
	private String buildContactNameEmail(OrdOrder order) {
		String email = "";
		OrdPerson orderPerson = order.getContactPerson();
		if(null != orderPerson){
			email = orderPerson.getEmail();
		}
		return email;
	}

	/**
	 * 处理订单的当前状态
	 * 
	 * @param order
	 * @return
	 */
	private String buildCurrentStatus(OrdOrder order) {
		StringBuilder builder = new StringBuilder();
		//组装订单状态
		if(OrderEnum.ORDER_STATUS.CANCEL.name().equals(order.getOrderStatus())){
			builder.append("取消");
		}else if(OrderEnum.ORDER_STATUS.NORMAL.name().equals(order.getOrderStatus())){
			builder.append("正常");
		}else if(OrderEnum.ORDER_STATUS.COMPLETE.name().equals(order.getOrderStatus())){
			builder.append("完成");
		}else{
			builder.append(order.getOrderStatus());
		}
		
		builder.append("<br>");
		
		//组装审核状态
		if(OrderEnum.INFO_STATUS.UNVERIFIED.name().equals(order.getInfoStatus())
				&& OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(order.getResourceStatus())){
			builder.append("未审核");
		}else if(OrderEnum.INFO_STATUS.INFOFAIL.name().equals(order.getInfoStatus())
				||OrderEnum.RESOURCE_STATUS.LOCK.name().equals(order.getResourceStatus())){
			builder.append("审核不通过");
		}else if(OrderEnum.INFO_STATUS.INFOPASS.name().equals(order.getInfoStatus())
				&&OrderEnum.RESOURCE_STATUS.AMPLE.name().equals(order.getResourceStatus())){
			builder.append("审核通过");
		}else{
			builder.append("审核中");
		}
		
		builder.append(" | ");
		
		//组装凭证确认状态
		if(OrderEnum.CERT_CONFIRM_STATUS.UNCONFIRMED.name().equals(order.getCertConfirmStatus())){
			builder.append("未确认");
		}else if(OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.name().equals(order.getCertConfirmStatus())){
			builder.append("已确认");
		}else{
			builder.append("未确认");
		}
		
		builder.append("<br>");
		
		//组装支付状态
		builder.append(OrderEnum.PAYMENT_STATUS.getCnName(order.getPaymentStatus()));
		
		Map<String,Object> contentMap = order.getMainOrderItem().getContentMap();
		String categoryType =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
		
		if (BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCode().equals(categoryType)) {
			builder.append(" | ");
			
			//出团通知书状态
			String noticeStatusName=OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.getCnName(order.getNoticeRegimentStatus()) ;
			builder.append(noticeStatusName);
		}
		
		
		
		return builder.toString();
	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
	}
}
