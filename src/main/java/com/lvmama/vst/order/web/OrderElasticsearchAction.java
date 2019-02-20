package com.lvmama.vst.order.web;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvmama.comm.pet.po.perm.PermPermission;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.vst.back.biz.po.BizBuEnum;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.BizBuEnumClientService;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.dist.service.DistributorClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.dist.po.Distributor;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTicketPerform;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.INFO_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.NOTICE_REGIMENT_STATUS_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.PAYMENT_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.PERFORM_STATUS_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.RESOURCE_STATUS;
import com.lvmama.vst.comm.enumeration.CommEnumSet.FILIALE_NAME;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.utils.WineSplitConstants;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.OrderMonitorCnd;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.elasticsearch.converter.BeanESParameterConverter;
import com.lvmama.vst.elasticsearch.converter.ParameterConverter;
import com.lvmama.vst.elasticsearch.params.ESParams;
import com.lvmama.vst.elasticsearch.params.ESQueryBuilder;
import com.lvmama.vst.elasticsearch.query.ESWrapper;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdAdditionStatusService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;

@Controller
public class OrderElasticsearchAction extends BaseActionSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2395791725291475612L;
	private static final Log logger = LogFactory.getLog(OrderElasticsearchAction.class);

	// 注入综合查询业务接口
	@Autowired
	private IComplexQueryService complexQueryService;
	// 注入分销商业务接口(订单来源、下单渠道)
	@Autowired
	private DistributorClientService distributorClientService;
	// 注入供应商业务接口
	@Autowired
	private SuppSupplierClientService suppSupplierClientService;
	// 产品类型业务接口
	@Autowired
	private CategoryClientService categoryClientService;

	@Autowired
	private IOrdAdditionStatusService ordAdditionStatusService;

	@Autowired
	private PermUserServiceAdapter permUserServiceAdapter;

	@Autowired
	private BizBuEnumClientService bizBuEnumClientService;

	private static final String ORDER_MONITOR_PAGE = "/order/query/orderESesarch";
	// 默认分页大小配置名称
	private final Integer DEFAULT_PAGE_SIZE = 10;

	public SearchResponse search(Integer page, Integer pageSize, ESParams params) {
		Integer currentPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
		// 组装订单分页类条件
		Integer currentPage = page == null ? 1 : page;

		Client client = ESWrapper.getInstance();
		String[] indices = ESWrapper.getIndices();
		String[] types = ESWrapper.getDocumentType();
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indices).setTypes(types)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);

		searchRequestBuilder = new ESQueryBuilder().requestParameters(searchRequestBuilder, params);
		SearchResponse response = searchRequestBuilder.setFrom((currentPage - 1) * currentPageSize)
				.addSort(SortBuilders.fieldSort("ORDER_ID").order(SortOrder.DESC))
				.setSize(currentPageSize).setExplain(true).execute().actionGet();
		
		logger.info("searchRequestBuilder : " + searchRequestBuilder);
		return response;
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
	private Page buildResultPage(List list, Integer currentPage, Integer pageSize, Long totalCount,
			HttpServletRequest request) {
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
	 * FORM表单初始化
	 * 
	 * @param model
	 */
	private void initQueryForm(Model model, HttpServletRequest request) throws BusinessException {
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
		
		// 门票使用状态字典	
		Map<String, String> performStatusMap = new LinkedHashMap<String, String>();
		performStatusMap.put("", "全部");
		for (PERFORM_STATUS_TYPE item : OrderEnum.PERFORM_STATUS_TYPE.values()) {
			performStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("performStatusMap", performStatusMap);

		// 产品类型字典
		List<BizCategory> productCategoryList = categoryClientService.findCategoryByAllValid().getReturnContent();
		Map<String, String> productCategoryMap = new LinkedHashMap<String, String>();
		// productCategoryMap.put("", "全部");
		if (null != productCategoryList && productCategoryList.size() > 0) {
			for (BizCategory category : productCategoryList) {
				if (category.getParentId() != null
						&& Constant.SEARCH_TYPE_TAG.FREETOUR.getCode() == category.getParentId().intValue()) {
					continue;
				}

				productCategoryMap.put(category.getCategoryId() + "", category.getCategoryName());

			}
		}
		model.addAttribute("productCategoryMap", productCategoryMap);
		// 加载自由行直系子品类
		List<BizCategory> freetourList = categoryClientService.getBizCategorysByParentCategoryId(
				WineSplitConstants.ROUTE_FREEDOM).getReturnContent();
		model.addAttribute("freetourList", freetourList);

		// 下单渠道
		HashMap<String, Object> dist_parm=new HashMap<String, Object>();
		dist_parm.put("cancelFlag", "Y");
		dist_parm.put("isShowPinTuan", "Y");
		List<Distributor> distributorList = distributorClientService.findDistributorList(dist_parm).getReturnContent();
		/*List<Distributor> distributorList = distributorClientService.findDistributorList(new HashMap<String, Object>())
				.getReturnContent();*/
		Map<String, String> distributorMap = new HashMap<String, String>();
		for (Distributor distributor : distributorList) {
			distributorMap.put(distributor.getDistributorId() + "", distributor.getDistributorName());
		}
		request.setAttribute("distributorMap", distributorMap);

		// 分公司字典
		Map<String, String> filialeMap = new LinkedHashMap<String, String>();
		filialeMap.put("", "全部");
		for (FILIALE_NAME item : FILIALE_NAME.values()) {
			filialeMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("filialeMap", filialeMap);

		// 商品支付方式
		Map<String, String> payTargetMap = new LinkedHashMap<String, String>();
		payTargetMap.put("", "全部");
		for (SuppGoods.PAYTARGET payTarget : SuppGoods.PAYTARGET.values()) {
			payTargetMap.put(payTarget.name(), payTarget.getCnName());
		}
		model.addAttribute("payTargetMap", payTargetMap);

		// 商品支付方式
		Map<String, String> stockFlagMap = new LinkedHashMap<String, String>();
		stockFlagMap.put("", "全部");
		stockFlagMap.put("Y", "保留房");
		stockFlagMap.put("N", "非保留房");
		model.addAttribute("stockFlagMap", stockFlagMap);

		// 确认单凭证
		Map<String, String> certConfirmStatusMap = new LinkedHashMap<String, String>();
		certConfirmStatusMap.put("", "全部");
		for (OrderEnum.CERT_CONFIRM_STATUS certConfirmStatus : OrderEnum.CERT_CONFIRM_STATUS.values()) {
			certConfirmStatusMap.put(certConfirmStatus.name(), certConfirmStatus.getCnName());
		}
		model.addAttribute("certConfirmStatusMap", certConfirmStatusMap);

		// 所属BU
		Map<String, String> belongBUMap = new LinkedHashMap<String, String>();
		belongBUMap.put("", "全部");
		ResultHandleT<List<BizBuEnum>> resultHandleT = bizBuEnumClientService.getAllBizBuEnumList();
		if (resultHandleT.isSuccess() && resultHandleT.getReturnContent() != null
				&& !resultHandleT.getReturnContent().isEmpty()) {
			//将国内bu和目的地bu合并成国内度假事业部,参考http://ipm.lvmama.com/index.php?m=story&f=view&t=html&id=12992
			String key = "LOCAL_BU|DESTINATION_BU";
			String text = "国内度假事业部";
			belongBUMap.put(key, text);
			for (BizBuEnum bizBuEnum : resultHandleT.getReturnContent()) {
				if(!key.contains(bizBuEnum.getCode())) {
					belongBUMap.put(bizBuEnum.getCode(), bizBuEnum.getCnName());
				}
			}
		}
		model.addAttribute("belongBUMap", belongBUMap);

		model.addAttribute("previousDate", getPreviousMonth(3));
		// 验证是否登录
		PermUser user = (PermUser) getSession(BaseActionSupport.SESSION_BACK_USER);
		if (user != null) {
			List<PermPermission> permList = user.getPermissionList();

			if (permList != null && permList.size() > 0) {
				for (PermPermission perm : permList) {
					if (StringUtils.isEmpty(perm.getUrl())) {
						continue;
					} else {
						if (perm.getUrl().equals("QUERY_ORDER_NO_REQUIRED") && !user.isAdministrator()) {

							model.addAttribute("orderNoRequired", "Y");
						}
					}
				}
			}
		}
		
		// 是否后置
		Map<String, String> travellerDelayFlagMap = new LinkedHashMap<String, String>();
		travellerDelayFlagMap.put("", "全部");
		travellerDelayFlagMap.put("Y", "是");
		travellerDelayFlagMap.put("N", "否");
		model.addAttribute("travellerDelayFlagMap", travellerDelayFlagMap);
		
		// 是否锁定
		Map<String, String> travellerLockFlagMap = new LinkedHashMap<String, String>();
		travellerLockFlagMap.put("", "全部");
		travellerLockFlagMap.put("Y", "是");
		travellerLockFlagMap.put("N", "否");
		model.addAttribute("travellerLockFlagMap", travellerLockFlagMap);
	}

	/**
	 * 获取当前日期的上一个月时间
	 * 
	 * @return
	 */
	private String getPreviousMonth(int month) {
		Calendar nowCal = Calendar.getInstance();
		nowCal.add(Calendar.MONTH, -month);
		Date previousDate = nowCal.getTime();
		return DateUtil.formatDate(previousDate, "yyyy-MM-dd HH:mm:ss");
	}

	@SuppressWarnings("unchecked")
	public List<OrdOrder> getOrderResult(SearchResponse esResponse) {
		List<OrdOrder> results = new ArrayList<OrdOrder>();
		SearchHits hits = esResponse.getHits();
		for (SearchHit hit : hits) {
			OrdOrder order = new OrdOrder();
			// 将订单来源转化为名称显示
			Object orderID = hit.getSource().get("ORDER_ID");
			BigDecimal orderIDDecimal = new BigDecimal(orderID.toString());
			order.setOrderId(orderIDDecimal.longValue());
			
			Object categoryID = hit.getSource().get("CATEGORY_ID");
			if(null !=categoryID &&!"".equals(categoryID.toString()) &&! "null".equals(categoryID.toString())){
			BigDecimal categoryIDDecimal = new BigDecimal(categoryID.toString());
			order.setCategoryId(categoryIDDecimal.longValue());}

			String isTestOrder = (String) hit.getSource().get("IS_TEST_ORDER");
			order.setIsTestOrder(null == isTestOrder ? 'N' : isTestOrder.toCharArray()[0]);

			Object distributorID = hit.getSource().get("DISTRIBUTOR_ID");
			BigDecimal distributorIDDecimal = new BigDecimal(distributorID.toString());
			order.setDistributorId(distributorIDDecimal.longValue());
			String distributorName = (String) hit.getSource().get("DISTRIBUTOR_NAME");
			order.setDistributorName(distributorName);

			List<String> productIDs = (List<String>) hit.getSource().get("PRODUCT_IDS");
			if (null != productIDs && productIDs.size() > 0)
				order.setProductId(Long.parseLong(productIDs.get(0)));

			List<String> productNames = (List<String>) hit.getSource().get("PRODUCT_NAMES");
			if (CollectionUtils.isNotEmpty(productNames)) {
				order.setOrderProductName(productNames.get(0));
			}

			// 订单状态
			String orderStatus = (String) hit.getSource().get("ORDER_STATUS");
			order.setOrderStatus(orderStatus);
			String paymentStatus = (String) hit.getSource().get("PAYMENT_STATUS");
			order.setPaymentStatus(paymentStatus);
			String resourceStatus = (String) hit.getSource().get("RESOURCE_STATUS");
			order.setResourceStatus(resourceStatus);
			String infoStatus = (String) hit.getSource().get("INFO_STATUS");
			order.setInfoStatus(infoStatus);
			//门票订单使用状态
			String performStatus = (String) hit.getSource().get("PERFORM_STATUS");
			order.setPerformStatus(performStatus);

			String buCode = (String) hit.getSource().get("BU_CODE");
			order.setBuCode(buCode);
	        //订单凭证确认状态
			String certConfirmStatus=(String) hit.getSource().get("CERT_CONFIRM_STATUS");
			order.setCertConfirmStatus(certConfirmStatus);
			// 联系人
			List<OrdPerson> ordPersonList = new ArrayList<OrdPerson>();
			String contactName = (String) hit.getSource().get("CONTACT_NAME");
			String contactMobile = (String) hit.getSource().get("CONTACT_MOBILE");
			if (StringUtils.isNotBlank(contactName)) {
				OrdPerson contact = new OrdPerson();
				contact.setPersonType(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
				contact.setFullName(contactName);
				contact.setMobile(contactMobile);
				ordPersonList.add(contact);
			}

			List<String> touristNames = (List<String>) hit.getSource().get("TOURIST_NAMES");
			List<String> touristMobiles = (List<String>) hit.getSource().get("TOURIST_MOBILES");
			if (CollectionUtils.isNotEmpty(touristNames)) {
				for (int i = 0; i < touristNames.size(); i++) {
					OrdPerson tourist = new OrdPerson();
					String touristName = touristNames.get(i);
					String touristMobile = touristMobiles.get(i);
					tourist.setPersonType(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
					tourist.setFullName(touristName);
					tourist.setMobile(touristMobile);
					ordPersonList.add(tourist);
				}
			}
			order.setOrdPersonList(ordPersonList);

			List<String> paymentTargets = (List<String>) hit.getSource().get("PAYMENT_METHODS");
			if (CollectionUtils.isNotEmpty(paymentTargets)) {
				order.setPaymentTarget(paymentTargets.get(0));
			}

			String paymentType = (String) hit.getSource().get("PAYMENT_TYPE");
			order.setPaymentType(paymentType);

			List<String> itemQuantities = (List<String>) hit.getSource().get("ITEM_QUANTITYS");
			OrdOrderItem itemOrder = new OrdOrderItem();
			List<OrdOrderItem> orderItemList = new ArrayList<OrdOrderItem>();
			// 新的子订单的数量
			List<String> itemFlags = (List<String>) hit.getSource().get("SUB_MAIN_ITEMS");
			if (CollectionUtils.isNotEmpty(itemFlags)) {
				if (CollectionUtils.isNotEmpty(itemQuantities)) {
					try {
						int i = 0;
						List<Integer> nums = new ArrayList<Integer>();
						for (String string : itemFlags) {
							if (string.equalsIgnoreCase("true")) {
								nums.add(i);
							}
							i++;
							String[] strs = itemQuantities.toArray(new String[itemQuantities.size()]);
							long count = 0;
							for (Integer integer : nums) {
								count += Long.parseLong(strs[integer]);
							}
							itemOrder.setQuantity(count);
							itemOrder.setMainItem(Boolean.TRUE.toString());
							orderItemList.add(itemOrder);
							order.setOrderItemList(orderItemList);
						}
					} catch (Exception e) {
						logger.error("es 查询错误，走老逻辑", e);
						long count = 0;
						for (String itemQuantity : itemQuantities) {
							count += Long.parseLong(itemQuantity);
						}
						itemOrder.setQuantity(count);
						itemOrder.setMainItem(Boolean.TRUE.toString());
						orderItemList.add(itemOrder);
						order.setOrderItemList(orderItemList);
					}
				}

			} else {
				if (CollectionUtils.isNotEmpty(itemQuantities)) {
					long count = 0;
					for (String itemQuantity : itemQuantities) {
						count += Long.parseLong(itemQuantity);
					}
					itemOrder.setQuantity(count);
					itemOrder.setMainItem(Boolean.TRUE.toString());
					orderItemList.add(itemOrder);
					order.setOrderItemList(orderItemList);
				}

			}
			
			// TODO 日期存储存在问题
			Long createTime = (Long) hit.getSource().get("CREATE_TIME");
			if (null != createTime) {
				Date createDate = new Date(createTime);
				order.setCreateTime(createDate);
			}
			Long visitTime = (Long) hit.getSource().get("VISIT_TIME");
			if (null != visitTime) {
				Date visitDate = new Date(visitTime);
				order.setVisitTime(visitDate);
				itemOrder.setVisitTime(visitDate);
			}

			String managerIDPerm = (String) hit.getSource().get("MANAGER_ID_PERM");
			if (StringUtils.isNotBlank(managerIDPerm)) {
				order.setManagerIdPerm(managerIDPerm);
			}
			results.add(order);
		}

		return results;
	}

	// ============================================================================================================
	/**
	 * 进入订单监控列表
	 * 
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/esinitialsearch.do")
	public String esInitialSearch(Model model, OrderMonitorCnd monitorCnd, HttpServletRequest request)
			throws BusinessException {
		// 初始化查询表单,给字典项赋值
		initQueryForm(model, request);
		if (monitorCnd.getCreateTimeBegin() == null || "".equals(monitorCnd.getCreateTimeBegin())) {
			monitorCnd.setCreateTimeBegin(getPreviousMonth(1));
		}
		monitorCnd.setWhichManagerId("managerId");
		model.addAttribute("monitorCnd", monitorCnd);
		return ORDER_MONITOR_PAGE;
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
	@RequestMapping(value = "/ord/order/esearchorder.do")
	public String esearchOrder(Model model, Integer page, Integer pageSize, OrderMonitorCnd monitorCnd,
			HttpServletRequest request) throws BusinessException {
		// 初始化查询表单,给字典项赋值
		initQueryForm(model, request);
		// 保留房查询条件初始
		if (SuppGoods.PAYTARGET.PREPAID.name().equals(monitorCnd.getPayTarget())
				&& (monitorCnd.getCategoryIdList() != null && monitorCnd.getCategoryIdList().size() == 1 && Constant.VST_CATEGORY.CATEGORY_HOTEL
						.getCategoryId().equals(monitorCnd.getCategoryIdList().get(0)))
				&& !StringUtils.isEmpty(monitorCnd.getStockFlag())) {// 选择了酒店且商品支付方式选择了“预付（驴妈妈）”时
			// do nothing
		} else {
			monitorCnd.setStockFlag(null);
		}

		Integer currentPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
		// 界面会子订单和主订单的产品经理id，根据radio判断是子订单的产品经理id还是主订单的产品经理id 二选一

		if ("itemManagerId".equals(monitorCnd.getWhichManagerId())) {
			monitorCnd.setItemManagerId(monitorCnd.getManagerId());
			monitorCnd.setManagerId(null);
		}

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		ParameterConverter<OrderMonitorCnd> paramConverter = new BeanESParameterConverter();
		ESParams params = paramConverter.convert(monitorCnd);
		for (String key : params.getNames()) {
			logger.info("Key =" + key + ", value = " + params.getParameter(key));
		}
		SearchResponse esResponse = search(page, currentPageSize, params);
		SearchHits hits = esResponse.getHits();
		logger.info("ESResponse total : " + hits.getTotalHits());
		logger.info("ESResponse:" + esResponse);
		List<OrdOrder> orderList = getOrderResult(esResponse);
		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

		if (logger.isDebugEnabled()) {
			if (null != orderList && orderList.size() > 0) {
				logger.debug("orderList=="
						+ ToStringBuilder.reflectionToString(orderList.get(0), ToStringStyle.MULTI_LINE_STYLE));
			}
		}

		// 减少数据库访问，当记录条数小于当前页面设置的分页条数,不用查询数据库
		Long totalCount = 0L;
		if (null != orderList) {
			if (page == null && orderList.size() < DEFAULT_PAGE_SIZE) {
				totalCount = (long) orderList.size();
			} else {
				totalCount = hits.getTotalHits();
			}
		}
		// 根据条件获取订单总记录数
		// Long totalCount =
		// complexQueryService.queryOrderCountByCondition(condition);

		if (logger.isDebugEnabled()) {
			logger.debug("totalCount==" + totalCount);
		}

		// 根据页面展示特色组装其想要的结果
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		if (null != orderList && orderList.size() > 0) {
			resultList = buildQueryResult(orderList, request);
		}

		if (logger.isDebugEnabled()) {
			if (null != resultList && resultList.size() > 0) {
				logger.debug("resultList=="
						+ ToStringBuilder.reflectionToString(resultList.get(0), ToStringStyle.MULTI_LINE_STYLE));
			}
		}

		// 组装分页结果
		@SuppressWarnings("rawtypes")
		Page resultPage = buildResultPage(resultList, page, pageSize, totalCount, request);
		// 设置当前页面显示数据大小
		resultPage.setPageSize(currentPageSize);
		// 存储分页结果
		model.addAttribute("resultPage", resultPage);

		if ("itemManagerId".equals(monitorCnd.getWhichManagerId())) {
			monitorCnd.setManagerId(monitorCnd.getItemManagerId());
			monitorCnd.setItemManagerId(null);
		}
		if (StringUtils.isBlank(monitorCnd.getWhichManagerId())) {
			monitorCnd.setWhichManagerId("managerId");
		}
		// 查询条件回显
		model.addAttribute("monitorCnd", monitorCnd);

		return ORDER_MONITOR_PAGE;
	}

	/**
	 * 组装页面上想要的结果
	 * 
	 * @param orderList
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<OrderMonitorRst> buildQueryResult(List<OrdOrder> orderList, HttpServletRequest request) {
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		Map<String, String> distributorMap = (Map<String, String>) request.getAttribute("distributorMap");
		for (OrdOrder order : orderList) {
			OrderMonitorRst orderMonitorRst = new OrderMonitorRst();
			// 将订单来源转化为名称显示
			if (null != distributorMap) {
				if (distributorMap.containsKey(order.getDistributorId() + "")) {
					orderMonitorRst.setDistributorName(distributorMap.get(order.getDistributorId() + ""));
				} else {
					orderMonitorRst.setDistributorName(order.getDistributorId() + "");
				}
			} else {
				orderMonitorRst.setDistributorName(order.getDistributorId() + "");
			}
			orderMonitorRst.setIsTestOrder(order.getIsTestOrder());
			orderMonitorRst.setOrderId(order.getOrderId());
			orderMonitorRst.setProductName(this.buildProductName(order));
			orderMonitorRst.setBuyCount(this.buildBuyCount(order));
			orderMonitorRst.setCreateTime(this.buildCreateTime(order));
			orderMonitorRst.setVisitTime(this.buildVisitTime(order));
			orderMonitorRst.setContactName(this.buildContactName(order));
			orderMonitorRst.setCurrentStatus(this.buildCurrentStatus(order));

			String key = "LOCAL_BU|DESTINATION_BU";
			if(key.contains(order.getBuCode())) {
				orderMonitorRst.setBelongBU("国内度假事业部");
			} else {
				ResultHandleT<BizBuEnum> resultHandleT = bizBuEnumClientService.getBizBuEnumByBuCode(order.getBuCode());
				if (resultHandleT.isSuccess()) {
					orderMonitorRst.setBelongBU(resultHandleT.getReturnContent().getCnName());
				}
			}

			orderMonitorRst.setPayTarget(SuppGoods.PAYTARGET.getCnName(order.getPaymentTarget()));
			orderMonitorRst.setGuarantee(order.getGuarantee());
			orderMonitorRst.setOrderPackList(order.getOrderPackList());
			orderMonitorRst.setManagerIdPerm(order.getManagerIdPerm());
			resultList.add(orderMonitorRst);
		}
		return resultList;
	}

	/**
	 * 根据订单和订单子项一对多的关系构建多个商品名称
	 * 
	 * @param orderList
	 * @return
	 */
	private String buildProductName(OrdOrder order) {

		String productName = order.getOrderProductName();
		if (StringUtils.isEmpty(productName)) {
			productName = "未知产品名称";
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
		String visitTimeStr = "未知日期";
		OrdOrderItem orderItem = order.getMainOrderItem();
		if (null != orderItem) {
            if (orderItem.hasTicketAperiodic()) {
                List<OrdTicketPerform> resultList = complexQueryService.selectByOrderItem(orderItem.getOrderItemId());
                if(CollectionUtils.isNotEmpty(resultList)){
                    Date performTime = resultList.get(0).getPerformTime();
                    if(performTime != null){
                        return DateUtil.SimpleFormatDateToString(performTime);
                    }
                    visitTimeStr = (String) orderItem.getContentMap().get(OrderEnum.ORDER_TICKET_TYPE.goodsExpInfo.name());
                    //期票不可游玩日期描述
                    String unvalidDesc =  (String) orderItem.getContentMap().get(OrderEnum.ORDER_TICKET_TYPE.aperiodic_unvalid_desc.name());
                    if(StringUtil.isNotEmptyString(unvalidDesc)){
                        visitTimeStr +="</br>(不适用日期:"+unvalidDesc+")";
                    }
                    return visitTimeStr;
                }
            }else{
                List<OrdOrderHotelTimeRate> orderHotelTimeRate = orderItem.getOrderHotelTimeRateList();
                String firstDay = DateUtil.formatDate(orderItem.getVisitTime(), "yyyy-MM-dd");
                visitTimeStr = firstDay;
                if (null != orderHotelTimeRate && orderHotelTimeRate.size() > 0) {
                    String lastDay = DateUtil.formatDate(
                            DateUtil.dsDay_Date(orderItem.getVisitTime(), orderHotelTimeRate.size()), "yyyy-MM-dd");
                    // visitTime = firstDay+"/"+lastDay;
                    visitTimeStr += "<br>" + lastDay;
                }
            }
		}
		return visitTimeStr;
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
		if (orderPerson == null) {// 无联系人默认第一个游客为联系人
			orderPerson = order.getFirstTravellerPerson();
		}
		if (null != orderPerson) {
			if (UtilityTool.isValid(orderPerson.getFullName()) && !orderPerson.getFullName().contains("null")) {
				contactPerson = orderPerson.getFullName();
			} else {
				// 如果没有显示手机号
				contactPerson = orderPerson.getMobile();
			}
		}
		return contactPerson;
	}

	/**
	 * 处理订单的当前状态
	 * 
	 * @param order
	 * @return
	 */
	private String buildCurrentStatus(OrdOrder order) {
		StringBuilder builder = new StringBuilder();
		// 组装订单状态
		if (OrderEnum.ORDER_STATUS.CANCEL.name().equals(order.getOrderStatus())) {
			builder.append("取消");
		} else if (OrderEnum.ORDER_STATUS.NORMAL.name().equals(order.getOrderStatus())) {
			builder.append("正常");
		} else if (OrderEnum.ORDER_STATUS.COMPLETE.name().equals(order.getOrderStatus())) {
			builder.append("完成");
		} else {
			builder.append(order.getOrderStatus());
		}

		builder.append("<br>");

		// 组装审核状态
		if (OrderEnum.INFO_STATUS.UNVERIFIED.name().equals(order.getInfoStatus())
				&& OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(order.getResourceStatus())) {
			builder.append("未审核");
		} else if (OrderEnum.INFO_STATUS.INFOFAIL.name().equals(order.getInfoStatus())
				|| OrderEnum.RESOURCE_STATUS.LOCK.name().equals(order.getResourceStatus())) {
			builder.append("审核不通过");
		} else if (OrderEnum.INFO_STATUS.INFOPASS.name().equals(order.getInfoStatus())
				&& OrderEnum.RESOURCE_STATUS.AMPLE.name().equals(order.getResourceStatus())) {
			builder.append("审核通过");
		} else {
			builder.append("审核中");
		}

		builder.append(" | ");

		// 组装凭证确认状态
		if (OrderEnum.CERT_CONFIRM_STATUS.UNCONFIRMED.name().equals(order.getCertConfirmStatus())) {
			builder.append("未确认");
		} else if (OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.name().equals(order.getCertConfirmStatus())) {
			builder.append("已确认");
		} else {
			builder.append("未确认");
		}

		builder.append("<br>");

		// 组装支付状态
		builder.append(OrderEnum.PAYMENT_STATUS.getCnName(order.getPaymentStatus()));
		String categoryType = "";
		if (order.getMainOrderItem() != null) {
			Map<String, Object> contentMap = order.getMainOrderItem().getContentMap();
			categoryType = (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
		}
		if (BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCode().equals(categoryType)) {
			builder.append(" | ");

			// 出团通知书状态
			String noticeStatusName = OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.getCnName(order.getNoticeRegimentStatus());
			builder.append(noticeStatusName);
		}

		// 门票业务类订单使用状态

		if (OrderUtils.isTicketByCategoryId(order.getCategoryId())) {
			builder.append(" | ");
			String performStatusName=OrderEnum.PERFORM_STATUS_TYPE.getCnName(order.getPerformStatus()) ;
			// 门票业务类订单使用状态
			List<OrdTicketPerform> resultList = new ArrayList<OrdTicketPerform>();
			List<OrdOrderItem> ordItemsList = order.getOrderItemList();
			// 订单使用状态
			List<String> perFormStatusList = new ArrayList<String>();
			if(StringUtil.isEmptyString(performStatusName)&& ordItemsList != null) {
				for (OrdOrderItem ordOrderItem : ordItemsList) {
					//门票业务类订单使用状态
					Map<String,Object> performMap = ordOrderItem.getContentMap();
					String categoryCode =  (String) performMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
					if (ProductCategoryUtil.isTicket(categoryCode)) {
					resultList = complexQueryService.selectByOrderItem(ordOrderItem.getOrderItemId());
					String performStatusName1=OrderEnum.PERFORM_STATUS_TYPE.getCnName(OrderUtils.calPerformStatus(resultList,order,ordOrderItem)) ;
					perFormStatusList.add(performStatusName1);
					}
				}
				performStatusName=OrderUtils.getMainOrderPerformStatus(perFormStatusList);
			}
			
			//门票业务类订单使用状态
			builder.append(performStatusName);

		}
		// 酒店业务类订单使用状态
		if (OrderUtils.isHotelByCategoryId(order.getCategoryId())) {
			builder.append(" | ");
			builder.append(OrderEnum.PERFORM_STATUS_TYPE.getCnName(order.getMainOrderItem().getPerformStatus()));
		}
		return builder.toString();
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
	}
}
