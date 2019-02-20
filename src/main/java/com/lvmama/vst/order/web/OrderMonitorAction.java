package com.lvmama.vst.order.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClientException;

import com.lvmama.comm.pet.po.perm.PermPermission;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.comm.stamp.vo.StampOrderDetails;
import com.lvmama.order.enums.ticket.ItemCancelEnum;
import com.lvmama.order.search.vo.LvoOrderVo;
import com.lvmama.order.search.vo.PageVo;
import com.lvmama.order.service.api.comm.order.IApiOrderItemCancelService;
import com.lvmama.order.vo.comm.OrdItemTicketVo;
import com.lvmama.vst.back.biz.po.BizBuEnum;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.BizBuEnumClientService;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.dist.service.DistributorClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.dist.po.Distributor;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdTicketPerform;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.INFO_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.IS_TEST_ORDER;
import com.lvmama.vst.back.order.po.OrderEnum.NOTICE_REGIMENT_STATUS_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.PAYMENT_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.PERFORM_STATUS_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.RESOURCE_STATUS;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comm.enumeration.CommEnumSet.FILIALE_NAME;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.UtilityTool;
import com.lvmama.vst.comm.utils.WineSplitConstants;
import com.lvmama.vst.comm.utils.json.JSONOutput;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderMonitorCnd;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.comm.vo.order.OrderSortParam;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdAdditionStatusService;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.utils.RestClient;
import com.lvmama.vst.order.web.service.OrderESQueryService;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;

/**
 * 新订单监控
 * 
 * @author wenzhengtao
 *
 */
@Controller
public class OrderMonitorAction extends BaseActionSupport {
	// 日志记录器
	private static final Log LOG = LogFactory.getLog(OrderMonitorAction.class);
	private static final Logger logger = LoggerFactory.getLogger(OrderMonitorAction.class);
	// 新订单监控页面地址
	private static final String ORDER_MONITOR_PAGE = "/order/query/orderMonitorList";
	
	private static final String NEW_ORDER_MONITOR_PAGE="/order/query/newOrderMonitorList";
	
	private static final String ORDER_MONITOR_SHIP_PAGE="/order/query/orderMonitorShipList";
	
	private static final String NEW_ORDER_MONITOR_SHIP_PAGE="/order/query/newOrderMonitorShipList";
	// 默认分页大小配置名称
	private final Integer DEFAULT_PAGE_SIZE = 10; 
	
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
	private PermUserServiceAdapter permUserServiceAdapter;

	@Autowired
	private BizBuEnumClientService bizBuEnumClientService;
	@Autowired
	private DistrictClientService districtClientService;
	
	@Resource(name = "apiOrderItemCancelService")
	private IApiOrderItemCancelService apiOrderItemCancelService;
	@Autowired
	private OrderESQueryService orderESQueryService;
	
	/**
	 * 进入订单监控列表
	 * 
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/intoOrderMonitor.do")
	public String intoOrderMonitor(Model model, OrderMonitorCnd monitorCnd,
			HttpServletRequest request) throws BusinessException {
		// 初始化查询表单,给字典项赋值
		initQueryForm(model, request);
		if (monitorCnd.getCreateTimeBegin() == null
				|| "".equals(monitorCnd.getCreateTimeBegin())) {
			monitorCnd.setCreateTimeBegin(getPreviousMonth(1));
		}
		monitorCnd.setWhichManagerId("managerId");
		model.addAttribute("monitorCnd", monitorCnd);
		return ORDER_MONITOR_PAGE;
	}

	/**
	 * 进入后台计调订单监控列表
	 * 
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/intoOrderMonitorShip.do")
	public String intoOrderMonitorShip(Model model, OrderMonitorCnd monitorCnd,
			HttpServletRequest request) throws BusinessException {
		// 初始化查询表单,给字典项赋值
		initShipQueryForm(model, request);
		if (monitorCnd.getCreateTimeBegin() == null
				|| "".equals(monitorCnd.getCreateTimeBegin())) {
			monitorCnd.setCreateTimeBegin(getPreviousMonth(1));
		}
		model.addAttribute("monitorCnd", monitorCnd);
		return ORDER_MONITOR_SHIP_PAGE;
	}
	
	/**
	 * 进入订单监控列表
	 * 
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/intoNewOrderMonitor.do")
	public String intoNewOrderMonitor(Model model, OrderMonitorCnd monitorCnd,
			HttpServletRequest request) throws BusinessException {
		// 初始化查询表单,给字典项赋值
		initQueryForm(model, request);
		if (monitorCnd.getCreateTimeBegin() == null
				|| "".equals(monitorCnd.getCreateTimeBegin())) {
			monitorCnd.setCreateTimeBegin(getPreviousMonth(1));
		}
		model.addAttribute("monitorCnd", monitorCnd);
		return NEW_ORDER_MONITOR_PAGE;
	}

	/**
	 * 进入后台计调订单监控列表
	 * 
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/ord/order/intoNewOrderMonitorShip.do")
	public String intoNewOrderMonitorShip(Model model, OrderMonitorCnd monitorCnd,
			HttpServletRequest request) throws BusinessException {
		// 初始化查询表单,给字典项赋值
		initShipQueryForm(model, request);
		if (monitorCnd.getCreateTimeBegin() == null
				|| "".equals(monitorCnd.getCreateTimeBegin())) {
			monitorCnd.setCreateTimeBegin(getPreviousMonth(1));
		}
		model.addAttribute("monitorCnd", monitorCnd);
		return NEW_ORDER_MONITOR_SHIP_PAGE;
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
	@RequestMapping(value = "/ord/order/newOrderMonitorList.do")
	public String newOrderMonitorList(Model model, Integer page,Integer pageSize,OrderMonitorCnd monitorCnd, HttpServletRequest request) throws BusinessException {
		// 初始化查询表单,给字典项赋值
		//查询条件中包含订单号时，走之前的逻辑
		//查询条件都包含ORD_ORDER中，并且需要查询一个月之前的订单，则走之前的逻辑
		if (monitorCnd.getOrderId() != null
				|| monitorCnd.onlyNeedOrdOrderTable()) {
			return orderMonitorList(model, page, pageSize, monitorCnd, request);
		}
		
		initQueryForm(model,request);
		Integer currentPageSize = pageSize == null? DEFAULT_PAGE_SIZE : pageSize;
		
		//组装订单分页类条件
		Integer currentPage = page == null ? 1 : page;
		//计算出每页的rownum
		monitorCnd.setBeginIndex((currentPage-1)*currentPageSize+1);
		monitorCnd.setEndIndex(currentPage*currentPageSize);
		if(monitorCnd.getPaymentTimeEnd() != null) {
			monitorCnd.setPaymentTimeEnd(com.lvmama.vst.comm.utils.DateUtil.DsDay_Second(monitorCnd.getPaymentTimeEnd(), 1440*60-1));
		}
		
		//保证每次请求都是一个新的对象
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();

		//组装订单标志类条件
		condition.getOrderFlagParam().setOrderTableFlag(true);//获得订单号
		condition.getOrderFlagParam().setOrderItemTableFlag(true);//获得产品名称
		condition.getOrderFlagParam().setOrderPersonTableFlag(true);//获得联系人
		condition.getOrderFlagParam().setOrderHotelTimeRateTableFlag(true);//获得离店时间
		
		condition.getOrderFlagParam().setOrdAdditionStatusTableFlag(true);
		condition.getOrderFlagParam().setOrdTravelContractTableFlag(true);
		
		condition.getOrderFlagParam().setOrderPackTableFlag(true);

		// 根据条件获取订单集合
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(monitorCnd, condition);
		
		if(LOG.isDebugEnabled()){
			if(null != orderList && orderList.size()>0){
				LOG.debug("orderList=="+ToStringBuilder.reflectionToString(orderList.get(0), ToStringStyle.MULTI_LINE_STYLE));
			}
		}

		// 减少数据库访问，当记录条数小于当前页面设置的分页条数,不用查询数据库
		Long totalCount = 0L;
		if (null != orderList) {
			if (page==null&&orderList.size() < DEFAULT_PAGE_SIZE) {
					totalCount = (long) orderList.size();
			} else {
					totalCount = complexQueryService.queryOrderCountByCondition(monitorCnd);
			}
		}
		// 根据条件获取订单总记录数
	//	Long totalCount = complexQueryService.queryOrderCountByCondition(condition);
		
		if(LOG.isDebugEnabled()){
			LOG.debug("totalCount=="+totalCount);
		}

		// 根据页面展示特色组装其想要的结果
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		if(null != orderList && orderList.size()>0){
			resultList = buildQueryResult(orderList,request);
		}
		
		if(LOG.isDebugEnabled()){
			if(null != resultList && resultList.size()>0){
				LOG.debug("resultList=="+ToStringBuilder.reflectionToString(resultList.get(0), ToStringStyle.MULTI_LINE_STYLE));
			}
		}

		// 组装分页结果
		@SuppressWarnings("rawtypes")
		Page resultPage = buildResultPage(resultList, page, pageSize, totalCount, request);
		//设置当前页面显示数据大小
		resultPage.setPageSize(currentPageSize);
		// 存储分页结果
		model.addAttribute("resultPage", resultPage);

		// 查询条件回显
		model.addAttribute("monitorCnd", monitorCnd);

		return NEW_ORDER_MONITOR_PAGE;
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
	@RequestMapping(value = "/ord/order/orderMonitorList.do")
	public String orderMonitorList(Model model, Integer page,Integer pageSize,OrderMonitorCnd monitorCnd, HttpServletRequest request) throws BusinessException {
		// 初始化查询表单,给字典项赋值
		initQueryForm(model,request);
		// 保留房查询条件初始
		if(SuppGoods.PAYTARGET.PREPAID.name().equals(monitorCnd.getPayTarget()) 
				&& (monitorCnd.getCategoryIdList() != null && monitorCnd.getCategoryIdList().size() == 1 
				&& Constant.VST_CATEGORY.CATEGORY_HOTEL.getCategoryId().equals(monitorCnd.getCategoryIdList().get(0))) 
				&& !StringUtils.isEmpty(monitorCnd.getStockFlag())){// 选择了酒店且商品支付方式选择了“预付（驴妈妈）”时
			// do nothing
		}else{
			monitorCnd.setStockFlag(null);
		}
		
		Integer currentPageSize = pageSize == null? DEFAULT_PAGE_SIZE : pageSize;
		//界面会子订单和主订单的产品经理id，根据radio判断是子订单的产品经理id还是主订单的产品经理id 二选一
		
		if("itemManagerId".equals(monitorCnd.getWhichManagerId())){
			monitorCnd.setItemManagerId(monitorCnd.getManagerId());
			monitorCnd.setManagerId(null);
		}
		
		// TODO 检查开关，开关开走ES，开关关走之前查oracle逻辑
		Long totalCount = 0L;
		List<OrdOrder> orderList = null;
		if (checkQueryESEnabled()) {
			// ====== 检索平台开发begin
			PageVo<LvoOrderVo> pageVo = orderESQueryService.queryOrderListFromES(page, currentPageSize, monitorCnd);
			
			List<LvoOrderVo> orderVoList = pageVo.getPageList();
			if (CollectionUtils.isNotEmpty(orderVoList)) {
				// orderVoList转换为orderList
				orderList = orderESQueryService.copyList(orderVoList);
			}
			totalCount = pageVo.getTotalResultSize();
			logger.info("query ES result,totalCount:{}", totalCount);
			
			// 当查询条件只有主/子订单而查询结果为空时，调用原来的查询方法，并且将查询结果保存到ES中
			if (checkParam(monitorCnd) && CollectionUtils.isEmpty(orderList)) {
				logger.info("查询ES结果为空切查询参数只有主子订单,orderId:{},orderItemId:{}",
						monitorCnd.getOrderId(), monitorCnd.getOrderItemId());
				ComplexQuerySQLCondition condition = buildQueryConditionForMonitor(
						page, pageSize, monitorCnd);
				orderList = complexQueryService.checkOrderListFromReadDB(condition);
				if (CollectionUtils.isNotEmpty(orderList)) {
					logger.info("查询的订单信息保存到ES,orderId:{},orderItemId:{}",
							monitorCnd.getOrderId(), monitorCnd.getOrderItemId());
					orderESQueryService.saveOrderList(orderList);

				}
			}
			// ====== 检索平台开发end
		} else {

			
			// 根据页面条件组装综合查询接口条件
			ComplexQuerySQLCondition condition = buildQueryConditionForMonitor(page, pageSize, monitorCnd);
		
			// 根据条件获取订单集合
			orderList = complexQueryService.checkOrderListFromReadDB(condition);
			if(orderList!=null){
				for(OrdOrder ordOrder: orderList ){
					if("STAMP".equals(ordOrder.getOrderSubType())){
					    LOG.info("------------------14------------------");
						try {
							String url = Constant.getInstance().getPreSaleBaseUrl() + "/customer/stamp/order/"+ordOrder.getOrderId();
							StampOrderDetails   stampOrderDetails =RestClient.getClient().getForObject(url, StampOrderDetails.class);
							if(stampOrderDetails.getStamp()!=null)
							ordOrder.setProductName(stampOrderDetails.getStamp().getName());
						} catch (RestClientException e) {
							LOG.info("调用微服务失败"+ordOrder.getOrderId(),e);
						}
					}
				}
				if(LOG.isDebugEnabled()){
					if(null != orderList && orderList.size()>0){
						LOG.debug("orderList=="+ToStringBuilder.reflectionToString(orderList.get(0), ToStringStyle.MULTI_LINE_STYLE));
					}
				}
			}

			// 减少数据库访问，当记录条数小于当前页面设置的分页条数,不用查询数据库
			if (null != orderList) {
				if (page==null&&orderList.size() < DEFAULT_PAGE_SIZE) {
						totalCount = (long) orderList.size();
				} else {
						totalCount = complexQueryService
									.checkOrderCountFromReadDB(condition);
				}
			}
			if(LOG.isDebugEnabled()){
				LOG.debug("totalCount=="+totalCount);
			}
		}

		// 根据页面展示特色组装其想要的结果
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		if(null != orderList && orderList.size()>0){
			resultList = buildQueryResult(orderList,request);
			for (OrderMonitorRst orderMonitorRst : resultList) {
				LOG.info("--------------------------------------------"+orderMonitorRst.getOrderSubType()+"orderid"+orderMonitorRst.getOrderId());
			}
		}
		
		if(LOG.isDebugEnabled()){
			if(null != resultList && resultList.size()>0){
				LOG.debug("resultList=="+ToStringBuilder.reflectionToString(resultList.get(0), ToStringStyle.MULTI_LINE_STYLE));
			}
		}

		// 组装分页结果
		@SuppressWarnings("rawtypes")
		Page resultPage = buildResultPage(resultList, page, pageSize, totalCount, request);
		//设置当前页面显示数据大小
		resultPage.setPageSize(currentPageSize);
		// 存储分页结果
		model.addAttribute("resultPage", resultPage);

		if("itemManagerId".equals(monitorCnd.getWhichManagerId())){
			monitorCnd.setManagerId(monitorCnd.getItemManagerId());
			monitorCnd.setItemManagerId(null);
		}
		if(StringUtils.isBlank(monitorCnd.getWhichManagerId())) {
			monitorCnd.setWhichManagerId("managerId");
		}
		// 查询条件回显
		model.addAttribute("monitorCnd", monitorCnd);
		

		return ORDER_MONITOR_PAGE;
	}
	
	private boolean checkParam(OrderMonitorCnd monitorCnd) {
		if ((monitorCnd.getOrderId() != null || monitorCnd.getOrderItemId() != null) && (!UtilityTool.isValid(monitorCnd.getBackUserId())
				&& !UtilityTool.isValid(monitorCnd.getBookerMobile())
				&& !UtilityTool.isValid(monitorCnd.getBookerName())
				&& !UtilityTool.isValid(monitorCnd.getContactEmail())
				&& !UtilityTool.isValid(monitorCnd.getContactMobile())
				&& !UtilityTool.isValid(monitorCnd.getContactName())
				&& !UtilityTool.isValid(monitorCnd.getContactPhone())
				&& CollectionUtils.isEmpty(monitorCnd.getFilialeNames())
				&& !UtilityTool.isValid(monitorCnd.getProductName())
				&& !UtilityTool.isValid(monitorCnd.getSuppGoodsName())
				&& !UtilityTool.isValid(monitorCnd.getSuppGoodsId())
				&& !UtilityTool.isValid(monitorCnd.getTravellerName())
				&& !UtilityTool.isValid(monitorCnd.getUserId())
				&& !UtilityTool.isValid(monitorCnd.getPayTarget())
				&& !UtilityTool.isValid(monitorCnd.getBelongBU())
				&& !UtilityTool.isValid(monitorCnd.getResponsiblePerson())
				&& !UtilityTool.isValid(monitorCnd.getStockFlag())
				&& CollectionUtils.isEmpty(monitorCnd.getCategoryIdList())
				&& CollectionUtils.isEmpty(monitorCnd.getSubCategoryIdList())
				&& !UtilityTool.isValid(monitorCnd.getCategoryId())
				&& CollectionUtils.isEmpty(monitorCnd.getDistributorIds())
				&& !UtilityTool.isValid(monitorCnd.getProductId())
				&& !UtilityTool.isValid(monitorCnd.getSupplierId())
				&& !UtilityTool.isValid(monitorCnd.getManagerId())
				&& !UtilityTool.isValid(monitorCnd.getItemManagerId())
				&& !UtilityTool.isValid(monitorCnd.getDistributorIdForWepAndApp())
				&& !UtilityTool.isValid(monitorCnd.getOrderStatus())
				&& !UtilityTool.isValid(monitorCnd.getInfoStatus())
				&& !UtilityTool.isValid(monitorCnd.getResourceStatus())
				&& !UtilityTool.isValid(monitorCnd.getPaymentStatus())
				&& !UtilityTool.isValid(monitorCnd.getCertConfirmStatus())
				&& !UtilityTool.isValid(monitorCnd.getContractStatus())
				&& !UtilityTool.isValid(monitorCnd.getNoticeRegimentStatus())
				&& !UtilityTool.isValid(monitorCnd.getPerformStatus())
				&& !UtilityTool.isValid(monitorCnd.getIsTestOrder())
				&& !UtilityTool.isValid(monitorCnd.getCreateTimeBegin())
				&& !UtilityTool.isValid(monitorCnd.getCreateTimeEnd())
				&& !UtilityTool.isValid(monitorCnd.getPaymentTimeBegin())
				&& !UtilityTool.isValid(monitorCnd.getPaymentTimeEnd())
				&& !UtilityTool.isValid(monitorCnd.getVisitTimeBegin())
				&& !UtilityTool.isValid(monitorCnd.getVisitTimeEnd())
				)) {
			return true;
		}
		return false;
	}
	private boolean checkQueryESEnabled() {
		String queryESEnabled = Constant.getInstance().getProperty("orderListQueryES.enabled");
		if (queryESEnabled != null) {
			return Boolean.valueOf(queryESEnabled);
		}
		return false;
	}

	/**
	 * 油轮新订单监控综合查询
	 * 
	 * @param model
	 * @param page
	 * @param pageSize
	 * @param monitorCnd
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/ord/order/newOrderMonitorShipList.do")
	public String newOrderMonitorShipList(Model model, Integer page,Integer pageSize,OrderMonitorCnd monitorCnd, HttpServletRequest request) throws BusinessException {
		// 初始化查询表单,给字典项赋值
		// 查询条件中包含订单号时，走之前的逻辑
		if (monitorCnd.getOrderId() != null
				|| monitorCnd.onlyNeedOrdOrderTable()) {
			return orderMonitorShipList(model, page, pageSize, monitorCnd,
					request);
		}
		initShipQueryForm(model, request);
		Integer currentPageSize = pageSize == null ? DEFAULT_PAGE_SIZE
				: pageSize;

		// 组装订单分页类条件
		Integer currentPage = page == null ? 1 : page;
		// 计算出每页的rownum
		monitorCnd.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
		monitorCnd.setPaymentStatus(OrderEnum.PAYMENT_STATUS.PAYED.getCode());
		monitorCnd.setBeginIndex((currentPage - 1) * currentPageSize + 1);
		monitorCnd.setEndIndex(currentPage * currentPageSize);
		if (monitorCnd.getPaymentTimeEnd() != null) {
			monitorCnd.setPaymentTimeEnd(com.lvmama.vst.comm.utils.DateUtil.DsDay_Second(
					monitorCnd.getPaymentTimeEnd(), 1440 * 60 - 1));
		}

		// 保证每次请求都是一个新的对象
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();

		// 组装订单标志类条件
		condition.getOrderFlagParam().setOrderTableFlag(true);// 获得订单号
		condition.getOrderFlagParam().setOrderItemTableFlag(true);// 获得产品名称
		condition.getOrderFlagParam().setOrderPersonTableFlag(true);// 获得联系人
		condition.getOrderFlagParam().setOrderHotelTimeRateTableFlag(true);// 获得离店时间

		condition.getOrderFlagParam().setOrdAdditionStatusTableFlag(true);
		condition.getOrderFlagParam().setOrdTravelContractTableFlag(true);

		condition.getOrderFlagParam().setOrderPackTableFlag(true);

		// 根据条件获取订单集合
		List<OrdOrder> orderList = complexQueryService
				.queryOrderListByCondition(monitorCnd, condition);

		if (LOG.isDebugEnabled()) {
			if (null != orderList && orderList.size() > 0) {
				LOG.debug("orderList=="
						+ ToStringBuilder.reflectionToString(orderList.get(0),
								ToStringStyle.MULTI_LINE_STYLE));
			}
		}

		// 减少数据库访问，当记录条数小于当前页面设置的分页条数,不用查询数据库
		Long totalCount = 0L;
		if (null != orderList) {
			if (page == null && orderList.size() < DEFAULT_PAGE_SIZE) {
				totalCount = (long) orderList.size();
			} else {
				totalCount = complexQueryService
						.queryOrderCountByCondition(monitorCnd);
			}
		}
		// 根据条件获取订单总记录数
		// Long totalCount =
		// complexQueryService.queryOrderCountByCondition(condition);

		if (LOG.isDebugEnabled()) {
			LOG.debug("totalCount==" + totalCount);
		}

		// 根据页面展示特色组装其想要的结果
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		if (null != orderList && orderList.size() > 0) {
			resultList = buildQueryShipResult(orderList, request);
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
		Page resultPage = buildResultPage(resultList, page, pageSize,
				totalCount, request);
		// 设置当前页面显示数据大小
		resultPage.setPageSize(currentPageSize);
		// 存储分页结果
		model.addAttribute("resultPage", resultPage);

		// 查询条件回显
		model.addAttribute("monitorCnd", monitorCnd);

		return NEW_ORDER_MONITOR_SHIP_PAGE;
	}
	
	
	
	/**
	 * 油轮新订单监控综合查询
	 * 
	 * @param model
	 * @param page
	 * @param pageSize
	 * @param monitorCnd
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/ord/order/orderMonitorShipList.do")
	public String orderMonitorShipList(Model model, Integer page,Integer pageSize,OrderMonitorCnd monitorCnd, HttpServletRequest request) throws BusinessException {
		// 初始化查询表单,给字典项赋值
		initShipQueryForm(model,request);

	/*	
		ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCode());
		BizCategory bizCategory=result.getReturnContent();
		
		monitorCnd.setPackCategoryId(bizCategory.getCategoryId());
		*/
		
		monitorCnd.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
		monitorCnd.setPaymentStatus(OrderEnum.PAYMENT_STATUS.PAYED.getCode());
		
		// 根据页面条件组装综合查询接口条件
		ComplexQuerySQLCondition condition = buildQueryShipConditionForMonitor(page, pageSize, monitorCnd);

		// 根据条件获取订单集合
		List<OrdOrder> orderList = complexQueryService.checkOrderListFromReadDB(condition);
		
		if(LOG.isDebugEnabled()){
			if(null != orderList && orderList.size()>0){
				LOG.debug("orderList=="+ToStringBuilder.reflectionToString(orderList.get(0), ToStringStyle.MULTI_LINE_STYLE));
			}
		}

		// 减少数据库访问，当记录条数小于当前页面设置的分页条数,不用查询数据库
		Long totalCount = 0L;
		if (null != orderList) {
			if (page==null&&orderList.size() < DEFAULT_PAGE_SIZE) {
					totalCount = (long) orderList.size();
			} else {
					totalCount = complexQueryService
								.checkOrderCountFromReadDB(condition);
			}
		}
		// 根据条件获取订单总记录数
		//Long totalCount = complexQueryService.queryOrderCountByCondition(condition);
		
		if(LOG.isDebugEnabled()){
			LOG.debug("totalCount=="+totalCount);
		}

		// 根据页面展示特色组装其想要的结果
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		if(null != orderList && orderList.size()>0){
			resultList = buildQueryShipResult(orderList,request);
		}
		
		if(LOG.isDebugEnabled()){
			if(null != resultList && resultList.size()>0){
				LOG.debug("resultList=="+ToStringBuilder.reflectionToString(resultList.get(0), ToStringStyle.MULTI_LINE_STYLE));
			}
		}

		// 组装分页结果
		@SuppressWarnings("rawtypes")
		Page resultPage = buildResultPage(resultList, page, pageSize, totalCount, request);
		
		// 存储分页结果
		model.addAttribute("resultPage", resultPage);

		// 查询条件回显
		model.addAttribute("monitorCnd", monitorCnd);

		return ORDER_MONITOR_SHIP_PAGE;
	}
	
	/**
	 * ajax查找所有供应商信息
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value="/ord/order/querySupplierList.do")
	public void querySupplierList(String search,HttpServletResponse response) throws BusinessException{
//		Map<String, Object> param = new HashMap<String, Object>();
//		param.put("supplierName", search);
		ResultHandleT<List<SuppSupplier>> resultHandleList = suppSupplierClientService.findSuppSupplierByName(search);
		if (resultHandleList.isSuccess()) {
			JSONArray jsonArray = new JSONArray();
			
			List<SuppSupplier> supplierList = resultHandleList.getReturnContent();
			for (SuppSupplier suppSupplier : supplierList) {
				ResultHandleT<BizDistrict> bizDistrict = districtClientService.findDistrictById(suppSupplier.getDistrictId());
				if(bizDistrict != null){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("id", suppSupplier.getSupplierId());
					jsonObject.put("text", suppSupplier.getSupplierName());
					jsonArray.add(jsonObject);
				}
			}
			JSONOutput.writeJSON(response, jsonArray);
		} else {
			LOG.info("method querySupplierList: resultHandleList.isFail,msg=" + resultHandleList.getMsg());
		}
	}
	
	/**
	 * ajax查找所有的用户信息
	 * */
	@RequestMapping(value="/ord/order/queryPermUserList.do")
	public void queryPermUserList(String search,HttpServletResponse response) throws BusinessException{
		List<PermUser> list = permUserServiceAdapter.findPermUser(search);
		JSONArray array = new JSONArray();
		if(CollectionUtils.isNotEmpty(list)){
			for(PermUser user:list){
				JSONObject obj=new JSONObject();
				obj.put("id", user.getUserId());
				obj.put("text", user.getRealName());
				array.add(obj);
			}
		}
		JSONOutput.writeJSON(response, array);
	}

	/**
	 * FORM表单初始化
	 * 
	 * @param model
	 */
	private void initQueryForm(Model model,HttpServletRequest request) throws BusinessException {
		// 订单状态字典
		Map<String, String> orderStatusMap = new LinkedHashMap<String, String>();
		orderStatusMap.put("", "全部");
		for (ORDER_STATUS item : ORDER_STATUS.values()) {
			orderStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("orderStatusMap", orderStatusMap);
		
		//lvcc支持 add by zjt
        String callId = StringUtils.isEmpty(request.getParameter("callid"))?"":request.getParameter("callid");
        LOG.info("initQueryForm:callId======" + callId);		
		model.addAttribute("callid", callId);
		
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
		// 门票使用状态字典	
		Map<String, String> performStatusMap = new LinkedHashMap<String, String>();
		performStatusMap.put("", "全部");
		for (PERFORM_STATUS_TYPE item : OrderEnum.PERFORM_STATUS_TYPE.values()) {
			performStatusMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("performStatusMap", performStatusMap);
	
		
		
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
//		productCategoryMap.put("", "全部");
		if(null!=productCategoryList && productCategoryList.size()>0){
			for(BizCategory category:productCategoryList){
				if (category.getParentId() != null && Constant.SEARCH_TYPE_TAG.FREETOUR.getCode() == category.getParentId().intValue()) {
					continue;
				}
				
				productCategoryMap.put(category.getCategoryId()+"", category.getCategoryName());

			}
		}
		model.addAttribute("productCategoryMap", productCategoryMap);
		// 是否显示测试订单	
		Map<String, String> isTestOrderMap = new LinkedHashMap<String, String>();
		isTestOrderMap.put("", "显示");
		for (IS_TEST_ORDER item : IS_TEST_ORDER.values()) {
			isTestOrderMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("isTestOrderMap", isTestOrderMap);
		//加载自由行直系子品类
		List<BizCategory> freetourList = categoryClientService.getBizCategorysByParentCategoryId(WineSplitConstants.ROUTE_FREEDOM).getReturnContent();
		model.addAttribute("freetourList", freetourList);
		
		//下单渠道
		HashMap<String, Object> dist_parm=new HashMap<String, Object>();
		dist_parm.put("cancelFlag", "Y");
		dist_parm.put("isShowPinTuan", "Y");
		List<Distributor> distributorList = distributorClientService.findDistributorList(dist_parm).getReturnContent();
		Map<String, String> distributorMap = new HashMap<String, String>();
		for(Distributor distributor:distributorList){
			distributorMap.put(distributor.getDistributorId()+"", distributor.getDistributorName());
		}
		request.setAttribute("distributorMap", distributorMap);
		
		//分公司字典
		Map<String, String> filialeMap = new LinkedHashMap<String, String>();
		filialeMap.put("", "全部");
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
		
		//商品支付方式
		Map<String, String> stockFlagMap = new LinkedHashMap<String, String>();
		stockFlagMap.put("", "全部");
		stockFlagMap.put("Y", "保留房");
		stockFlagMap.put("N", "非保留房");
		model.addAttribute("stockFlagMap", stockFlagMap);
		
		//确认单凭证
		Map<String, String> certConfirmStatusMap = new LinkedHashMap<String, String>();
		certConfirmStatusMap.put("", "全部");
		for(OrderEnum.CERT_CONFIRM_STATUS certConfirmStatus:OrderEnum.CERT_CONFIRM_STATUS.values()){
			certConfirmStatusMap.put(certConfirmStatus.name(), certConfirmStatus.getCnName());
		}
		model.addAttribute("certConfirmStatusMap", certConfirmStatusMap);
		
		//所属BU
		Map<String, String> belongBUMap = new LinkedHashMap<String, String>();
		belongBUMap.put("", "全部");
		//参考http://ipm.lvmama.com/index.php?m=story&f=view&t=html&id=12992
		String key = "LOCAL_BU|DESTINATION_BU";
		String text = "国内度假事业部";
		//belongBUMap.put(key, text);
		ResultHandleT<List<BizBuEnum>> resultHandleT = bizBuEnumClientService.getAllBizBuEnumList();
		if(resultHandleT.isSuccess() && resultHandleT.getReturnContent() != null && !resultHandleT.getReturnContent().isEmpty()){
			for (BizBuEnum bizBuEnum : resultHandleT.getReturnContent()) {
				//if(!key.contains(bizBuEnum.getCode())) {
					belongBUMap.put(bizBuEnum.getCode(), bizBuEnum.getCnName());
				//}
			}
		}
		model.addAttribute("belongBUMap", belongBUMap);
		
		
		model.addAttribute("previousDate",getPreviousMonth(3));
		//验证是否登录
		PermUser user = (PermUser) getSession(BaseActionSupport.SESSION_BACK_USER);
		if(user!=null){
		List<PermPermission> permList = user.getPermissionList();
		
		if(permList !=null && permList.size() > 0){
			for(PermPermission perm : permList){
				if(StringUtils.isEmpty(perm.getUrl())){
					continue;
				}else{
					if(perm.getUrl().equals("QUERY_ORDER_NO_REQUIRED")&&!user.isAdministrator()){
						
						model.addAttribute("orderNoRequired", "Y");
					}
				}
			}
		}
		}
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
		/*List<BizCategory> productCategoryList = categoryClientService.findCategoryByAllValid().getReturnContent();
		Map<String, String> productCategoryMap = new LinkedHashMap<String, String>();
		productCategoryMap.put("", "全部");
		if(null!=productCategoryList && productCategoryList.size()>0){
			for(BizCategory category:productCategoryList){
				productCategoryMap.put(category.getCategoryId()+"", category.getCategoryName());
			}
		}
		model.addAttribute("productCategoryMap", productCategoryMap);
*/
		Map<String, String> productCategoryMap = new LinkedHashMap<String, String>();
		ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCode());
		BizCategory category=result.getReturnContent();
		productCategoryMap.put("8,15,18,16,17,42", "全部");
		productCategoryMap.put(category.getCategoryId()+"", category.getCategoryName());
		
		result=categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCode());
		category=result.getReturnContent();
		productCategoryMap.put(category.getCategoryId()+"", category.getCategoryName());
		
		result=categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCode());
		category=result.getReturnContent();
		productCategoryMap.put(category.getCategoryId()+"", category.getCategoryName());

		//自由行bizFreedomList
		if (category != null) {
			List<BizCategory> bizFreedomList = categoryClientService.getBizCategorysByParentCategoryId(category.getCategoryId()).getReturnContent();
			model.addAttribute("bizFreedomList", bizFreedomList);
		}
		
		result=categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCode());
		category=result.getReturnContent();
		productCategoryMap.put(category.getCategoryId()+"", category.getCategoryName());
		
		result=categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCode());
		category=result.getReturnContent();
		productCategoryMap.put(category.getCategoryId()+"", category.getCategoryName());
		
		result=categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.getCode());
		category=result.getReturnContent();
		productCategoryMap.put(category.getCategoryId()+"", category.getCategoryName());
		
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
		model.addAttribute("previousDate",getPreviousMonth(3));

	}

	/**
	 * 新订单监控查询条件封装
	 * 
	 * @param currentPage
	 * @param pageSize
	 * @param monitorCnd
	 * @return
	 */
	private ComplexQuerySQLCondition buildQueryConditionForMonitor(Integer page, Integer pageSize, OrderMonitorCnd monitorCnd) {
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
		condition.getOrderContentParam().setSuppGoodstId(monitorCnd.getSuppGoodsId());
		condition.getOrderContentParam().setTravellerName(monitorCnd.getTravellerName());
		condition.getOrderContentParam().setUserId(monitorCnd.getUserId());
		condition.getOrderContentParam().setPayTarget(monitorCnd.getPayTarget());
		condition.getOrderContentParam().setBelongBU(monitorCnd.getBelongBU());
		condition.getOrderContentParam().setResponsiblePerson(monitorCnd.getResponsiblePerson());
		condition.getOrderContentParam().setStockFlag(monitorCnd.getStockFlag());//房间类型

		//组装订单标志类条件
		condition.getOrderFlagParam().setOrderTableFlag(true);//获得订单号
		condition.getOrderFlagParam().setOrderItemTableFlag(true);//获得产品名称
		condition.getOrderFlagParam().setOrderPersonTableFlag(true);//获得联系人
		condition.getOrderFlagParam().setOrderHotelTimeRateTableFlag(true);//获得离店时间
		condition.getOrderFlagParam().setOrderPageFlag(true);//需要分页
		
		condition.getOrderFlagParam().setOrdAdditionStatusTableFlag(true);
		condition.getOrderFlagParam().setOrdTravelContractTableFlag(true);
		
		condition.getOrderFlagParam().setOrderPackTableFlag(true);

		//以下为组装订单ID类条件
		//组装产品类型ID
		if(null != monitorCnd.getCategoryIdList() && monitorCnd.getCategoryIdList().size()>0){
			StringBuffer sbf = new StringBuffer();
			for(String categoryIdStr:monitorCnd.getCategoryIdList() ){
				sbf.append(","+categoryIdStr);				
			}
			monitorCnd.setCategoryIds(sbf.substring(1, sbf.length()));
		}
		if (null != monitorCnd.getSubCategoryIdList() && monitorCnd.getSubCategoryIdList().size() > 0) {
			StringBuffer sbf = new StringBuffer();
			for(String subCategoryIdStr : monitorCnd.getSubCategoryIdList()){
				sbf.append(","+subCategoryIdStr);				
			}
			if (sbf.length() > 0) {
				monitorCnd.setSubCategoryIds(sbf.substring(1, sbf.length()));
			}
		}
//		condition.getOrderIndentityParam().setCategoryId(monitorCnd.getCategoryId());
		condition.getOrderIndentityParam().setCategoryIds(monitorCnd.getCategoryIds());
		condition.getOrderIndentityParam().setSubCategoryIds(monitorCnd.getSubCategoryIds());
		condition.getOrderIndentityParam().setDistributorIds(monitorCnd.getDistributorIds());
		condition.getOrderIndentityParam().setOrderId(monitorCnd.getOrderId());
		condition.getOrderIndentityParam().setProductId(monitorCnd.getProductId());
		condition.getOrderIndentityParam().setSupplierId(monitorCnd.getSupplierId());
		condition.getOrderIndentityParam().setManagerId(monitorCnd.getManagerId());
		condition.getOrderIndentityParam().setItemManagerId(monitorCnd.getItemManagerId());
		condition.getOrderIndentityParam().setOrderItemId(monitorCnd.getOrderItemId());
		condition.getOrderIndentityParam().setDistributorIdForWepAndApp(monitorCnd.getDistributorIdForWepAndApp());
		

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
		//门票使用状态
		condition.getOrderStatusParam().setPerformStatus(monitorCnd.getPerformStatus());
		condition.getOrderStatusParam().setIsTestOrder(monitorCnd.getIsTestOrder());
		
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

		//组装订单分页类条件
		Integer currentPage = page == null ? 1 : page;
		Integer currentPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
		//计算出每页的rownum
		condition.getOrderPageIndexParam().setBeginIndex((currentPage-1)*currentPageSize+1);
		condition.getOrderPageIndexParam().setEndIndex(currentPage*currentPageSize);
		
		return condition;
	}
	
	/**
	 * 油轮新订单监控查询条件封装
	 * 
	 * @param currentPage
	 * @param pageSize
	 * @param monitorCnd
	 * @return
	 */
	private ComplexQuerySQLCondition buildQueryShipConditionForMonitor(Integer page, Integer pageSize, OrderMonitorCnd monitorCnd) {
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
		condition.getOrderContentParam().setResponsiblePerson(monitorCnd.getResponsiblePerson());
		
		condition.getOrderContentParam().setStatusType(OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.getCode());

		//组装订单标志类条件
		condition.getOrderFlagParam().setOrderTableFlag(true);//获得订单号
		condition.getOrderFlagParam().setOrderItemTableFlag(true);//获得产品名称
		condition.getOrderFlagParam().setOrderPersonTableFlag(true);//获得联系人
		//condition.getOrderFlagParam().setOrderHotelTimeRateTableFlag(true);//获得离店时间
		condition.getOrderFlagParam().setOrderPackTableFlag(true);
		
		condition.getOrderFlagParam().setOrdAdditionStatusTableFlag(true);
		condition.getOrderFlagParam().setOrdTravelContractTableFlag(true);
		
		condition.getOrderFlagParam().setOrderPageFlag(true);//需要分页

		//组装订单ID类条件
		condition.getOrderIndentityParam().setCategoryId(monitorCnd.getCategoryId());
		condition.getOrderIndentityParam().setDistributorIds(monitorCnd.getDistributorIds());
		condition.getOrderIndentityParam().setOrderId(monitorCnd.getOrderId());
		condition.getOrderIndentityParam().setProductId(monitorCnd.getProductId());
		condition.getOrderIndentityParam().setSupplierId(monitorCnd.getSupplierId());
		
//		condition.getOrderIndentityParam().setPackCategoryId(monitorCnd.getPackCategoryId());
//		condition.getOrderIndentityParam().setCategoryId(monitorCnd.getCategoryId());
		condition.getOrderIndentityParam().setCategoryIds(monitorCnd.getCategoryIds()); 
		condition.getOrderIndentityParam().setOrderItemId(monitorCnd.getOrderItemId());
		if (StringUtil.isNotEmptyString(monitorCnd.getCategoryIds())
				&& String.valueOf(Constant.SEARCH_TYPE_TAG.FREETOUR.getCode()).equals(monitorCnd.getCategoryIds())) {
			condition.getOrderIndentityParam().setSubCategoryId(monitorCnd.getSubCategoryId());
		}
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

		//组装订单分页类条件
		Integer currentPage = page == null ? 1 : page;
		Integer currentPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
		//计算出每页的rownum
		condition.getOrderPageIndexParam().setBeginIndex((currentPage-1)*currentPageSize+1);
		condition.getOrderPageIndexParam().setEndIndex(currentPage*currentPageSize);
		
		return condition;
	}

	/**
	 * 组装页面上想要的结果
	 * 
	 * @param orderList
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<OrderMonitorRst> buildQueryResult(List<OrdOrder> orderList,HttpServletRequest request) {
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		Map<String, String> distributorMap = (Map<String, String>)request.getAttribute("distributorMap");
		for (OrdOrder order : orderList) {
			OrderMonitorRst orderMonitorRst = new OrderMonitorRst();
			//将订单来源转化为名称显示
			if(null != distributorMap){
				if(distributorMap.containsKey(order.getDistributorId()+"")){
					orderMonitorRst.setDistributorName(distributorMap.get(order.getDistributorId()+""));
				}else{
					orderMonitorRst.setDistributorName(order.getDistributorId()+"");
				}
			}else{
				orderMonitorRst.setDistributorName(order.getDistributorId()+"");
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
			//if(key.contains(order.getBuCode())) {
			//	orderMonitorRst.setBelongBU("国内度假事业部");
			//} else {
				ResultHandleT<BizBuEnum> resultHandleT = bizBuEnumClientService.getBizBuEnumByBuCode(order.getBuCode());
				if(resultHandleT.isSuccess()){
					orderMonitorRst.setBelongBU(resultHandleT.getReturnContent().getCnName());
				}
			//}
			
			orderMonitorRst.setPayTarget(SuppGoods.PAYTARGET.getCnName(order.getPaymentTarget()));
			orderMonitorRst.setGuarantee(order.getGuarantee());
			orderMonitorRst.setOrderPackList(order.getOrderPackList());
			orderMonitorRst.setManagerIdPerm(order.getManagerIdPerm());
			orderMonitorRst.setOrderSubType(order.getOrderSubType());
			resultList.add(orderMonitorRst);
		}
		return resultList;
	}

	
	/**
	 * 组装页面上想要的结果
	 * 
	 * @param orderList
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<OrderMonitorRst> buildQueryShipResult(List<OrdOrder> orderList,HttpServletRequest request) {
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		Map<String, String> distributorMap = (Map<String, String>)request.getAttribute("distributorMap");
		
		
		for (OrdOrder order : orderList) {
			
			OrderMonitorRst orderMonitorRst = new OrderMonitorRst();
			
			//将订单来源转化为名称显示
			if(null != distributorMap){
				if(distributorMap.containsKey(order.getDistributorId()+"")){
					orderMonitorRst.setDistributorName(distributorMap.get(order.getDistributorId()+""));
				}else{
					orderMonitorRst.setDistributorName(order.getDistributorId()+"");
				}
			}else{
				orderMonitorRst.setDistributorName(order.getDistributorId()+"");
			}
			orderMonitorRst.setOrderId(order.getOrderId());
			orderMonitorRst.setProductName(this.buildProductName(order));
			orderMonitorRst.setBuyCount(this.buildBuyCount(order));
			orderMonitorRst.setCreateTime(this.buildCreateTime(order));
			orderMonitorRst.setVisitTime(this.buildShipVisitTime(order));
			orderMonitorRst.setContactName(this.buildContactName(order));
			orderMonitorRst.setContactNameEmail(this.buildContactNameEmail(order));
			orderMonitorRst.setCurrentStatus(this.buildCurrentStatus(order));
			
			orderMonitorRst.setOrderStatus(order.getOrderStatus());
			orderMonitorRst.setPayTarget(SuppGoods.PAYTARGET.getCnName(order.getPaymentTarget()));
			orderMonitorRst.setGuarantee(order.getGuarantee());
			
			
			
			orderMonitorRst.setTourisCount( this.buildTourisCount(order));
			orderMonitorRst.setNoticeRegimentStatusName(OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.getCnName(order.getNoticeRegimentStatus()) );
			orderMonitorRst.setNoticeRegimentStatus(order.getNoticeRegimentStatus());
			
			Map<String,Object> contentMap = order.getMainOrderItem().getContentMap();
			String categoryType =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
			
			orderMonitorRst.setCategoryType(categoryType);
			
			/*
			List<OrdAdditionStatus> ordAdditionStatusList=new ArrayList<OrdAdditionStatus>();
			if ( !StringUtils.isEmpty(contractStatus) ) {
				Map<String, Object> paramsOrdAdditionStatus = new HashMap<String, Object>();
				paramsOrdAdditionStatus.put("orderId", order.getOrderId());//订单号
				paramsOrdAdditionStatus.put("statusType", OrderEnum.ORD_ADDITION_STATUS_TYPE.CONTRACT_STATUS.getCode());
				paramsOrdAdditionStatus.put("status", contractStatus);
				
				ordAdditionStatusList=ordAdditionStatusService.findOrdAdditionStatusList(paramsOrdAdditionStatus);
				if (ordAdditionStatusList.isEmpty()) {
					continue;
				}
			}
			Map<String, Object> paramsOrdAdditionStatus = new HashMap<String, Object>();
			paramsOrdAdditionStatus.put("orderId", order.getOrderId());//订单号
			paramsOrdAdditionStatus.put("statusType", OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.getCode());
			paramsOrdAdditionStatus.put("status", noticeRegimentStatus);
			
			ordAdditionStatusList=ordAdditionStatusService.findOrdAdditionStatusList(paramsOrdAdditionStatus);
			if (ordAdditionStatusList.isEmpty()) {
				continue;
			}
			for (OrdAdditionStatus ordAdditionStatus : ordAdditionStatusList) {
				
				if (OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.getCode().equals(ordAdditionStatus.getStatusType())) {
					orderMonitorRst.setNoticeRegimentStatus(ordAdditionStatus.getStatus());
					orderMonitorRst.setNoticeRegimentStatusName(OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.getCnName(ordAdditionStatus.getStatus()));
				}
			}
			*/
			/*
			if ( !(StringUtils.isEmpty(contractStatus) &&  StringUtils.isEmpty(noticeRegimentStatus))) {
				String[]  statusArray=new String[]{contractStatus,noticeRegimentStatus};
				Map<String, Object> paramsOrdAdditionStatus = new HashMap<String, Object>();
				paramsOrdAdditionStatus.put("orderId", order.getOrderId());//订单号
				if ( !StringUtils.isEmpty(contractStatus) &&  !StringUtils.isEmpty(noticeRegimentStatus)) {
					paramsOrdAdditionStatus.put("statusType", OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.getCode());
					paramsOrdAdditionStatus.put("statusArray", statusArray);
				}else if (!StringUtils.isEmpty(contractStatus)) {
					paramsOrdAdditionStatus.put("statusType", contractStatus);
				}else if (!StringUtils.isEmpty(noticeRegimentStatus)) {
					paramsOrdAdditionStatus.put("statusType", noticeRegimentStatus);
				}
				ordAdditionStatusList=ordAdditionStatusService.findOrdAdditionStatusList(paramsOrdAdditionStatus);
				if (ordAdditionStatusList.isEmpty()) {
					continue;
				}
			}else{
				Map<String, Object> paramsOrdAdditionStatus = new HashMap<String, Object>();
				paramsOrdAdditionStatus.put("orderId", order.getOrderId());//订单号
				paramsOrdAdditionStatus.put("statusType", OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.getCode());
				ordAdditionStatusList=ordAdditionStatusService.findOrdAdditionStatusList(paramsOrdAdditionStatus);
			}*/
			
			
			
			resultList.add(orderMonitorRst);
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
	private String buildProductName(OrdOrder order) {
		
		String productName =order.getOrderProductName();
		if (StringUtils.isEmpty(productName)) {
			productName = "未知产品名称";
		}
		/*
		OrdOrderItem orderItem = order.getMainOrderItem();
		if (null != orderItem) {
			productName = orderItem.getProductName()+orderItem.getSuppGoodsName();
		}*/
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
		
		//判断是否期票自主打包
		if(null != order.getOrdOrderPack() && ProdProduct.PACKAGETYPE.LVMAMA.getCode().equals(order.getOrdOrderPack().getOwnPack())){
			Boolean aperiodicPackageFlag = true;//是否期票自主打包
			for(OrdOrderItem orderItem_:order.getOrderItemList()){
				if (!orderItem_.hasTicketAperiodic()) {
					aperiodicPackageFlag = false;
					break;
				}
			}
			if(aperiodicPackageFlag == true){//期票自主打包   游玩日期显示被打包期票分别的有效期
				String visitTime_aperiodicPack = "";
				for(OrdOrderItem orderItem_:order.getOrderItemList()){
					if (orderItem_.hasTicketAperiodic()) {
						Date vBeginTime = orderItem_.getValidBeginTime();
						Date vEndTime = orderItem_.getValidEndTime();
						if (vBeginTime != null && vEndTime != null) {
						    visitTime_aperiodicPack +=DateUtil.formatDate(vBeginTime, "yyyy-MM-dd");
						    visitTime_aperiodicPack +="——"+DateUtil.formatDate(vEndTime, "yyyy-MM-dd")+"<br>";
						} else {
							// 期票打包,游玩日期为空则展示下单日期
							visitTime_aperiodicPack += DateUtil.formatDate(orderItem_.getCreateTime(), "yyyy-MM-dd") + "<br>";
						}
					}else{
						visitTime_aperiodicPack += DateUtil.formatDate(orderItem_.getVisitTime(), "yyyy-MM-dd")+"<br>";
					}
				}
				return visitTime_aperiodicPack;
			}
		}
		
		
		String visitTime = "未知日期";
		OrdOrderItem orderItem = order.getMainOrderItem();
		if(null != orderItem){
			List<OrdOrderHotelTimeRate> orderHotelTimeRate = orderItem.getOrderHotelTimeRateList();
			String firstDay = DateUtil.formatDate(orderItem.getVisitTime(), "yyyy-MM-dd");
			visitTime = firstDay;
			//自由行机酒订单游玩日期取值逻辑调整，只取主订单的出发日期
			if(order.getCategoryId()!=null && order.getSubCategoryId()!=null && order.getCategoryId() == 18L && order.getSubCategoryId() == 182L){
				visitTime = DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd");
				return visitTime;
			}
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
		
		OrdOrderItem mainOrdItem=order.getMainOrderItem();
		if (2L==mainOrdItem.getCategoryId()) {//邮轮
			List<OrdOrderPack> orderPackList=order.getOrderPackList();
			if (CollectionUtils.isNotEmpty(orderPackList)) {
				String firstDay = DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd");
				OrdOrderPack ordOrderPack=orderPackList.get(0);
				//产品名称  ORD_ORDER_PACK
				//上船地点     下船地点 	 所属航线
				Map<String,Object> orderPackContentMap = ordOrderPack.getContentMap();
				visitTime = firstDay+"<br>"+orderPackContentMap.get(OrderEnum.ORDER_PACK_TYPE.end_sailing_date.name());
				
			}
		}else{
			visitTime=DateUtil.formatDate(order.getVisitTime(), "yyyy-MM-dd");
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
			builder.append("凭证未确认");
		}else if(OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.name().equals(order.getCertConfirmStatus())){
			builder.append("凭证已确认");
		}else{
			builder.append("凭证未确认");
		}
		
		builder.append("<br>");
		
		//组装支付状态
		builder.append(OrderEnum.PAYMENT_STATUS.getCnName(order.getPaymentStatus()));
		String categoryType = "";
		if(order.getMainOrderItem() != null){
			Map<String,Object> contentMap = order.getMainOrderItem().getContentMap();
			categoryType =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
		}
		if (BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCode().equals(categoryType)) {
			builder.append(" | ");
			
			//出团通知书状态
			String noticeStatusName=OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.getCnName(order.getNoticeRegimentStatus()) ;
			builder.append(noticeStatusName);
		}
		
		//门票业务类订单使用状态
	
		if (OrderUtils.isTicketByCategoryId(order.getCategoryId())) {
			builder.append(" | ");
			String performStatusName=OrderEnum.PERFORM_STATUS_TYPE.getCnName(order.getPerformStatus()) ;
			
			//门票业务类订单使用状态
			List<OrdTicketPerform> resultList = new ArrayList<OrdTicketPerform>();
			List<OrdOrderItem> ordItemsList =order.getOrderItemList();
			//订单使用状态
			List<String> perFormStatusList = new ArrayList<String>();
			if((!OrderEnum.PERFORM_STATUS_TYPE.PERFORM.name().equalsIgnoreCase(performStatusName)) && ordItemsList != null) {
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
		//酒店业务类订单使用状态
		if (OrderUtils.isHotelByCategoryId(order.getCategoryId())) {
			builder.append(" | ");
			builder.append(OrderEnum.PERFORM_STATUS_TYPE.getCnName(order.getMainOrderItem().getPerformStatus()));
		}
		
		//根据主单ID  子订单出票状态查询  ord_item_ticket
	        OrdItemTicketVo ordItemTicketOv = new OrdItemTicketVo();
	        com.lvmama.order.api.base.vo.RequestBody<OrdItemTicketVo> request2 = new com.lvmama.order.api.base.vo.RequestBody<OrdItemTicketVo>();
	        ordItemTicketOv.setOrderId(order.getOrderId());
	        request2.setT(ordItemTicketOv);
	        com.lvmama.order.api.base.vo.ResponseBody<OrdItemTicketVo> responseBody2 = apiOrderItemCancelService.selectOrdItemTicketNum(request2);
	        if (null != responseBody2 && null != responseBody2.getT()) {
	            OrdItemTicketVo ordItemTicket = responseBody2.getT();
	            if (ordItemTicket != null && 
			        ordItemTicket.getActualCount() != null && 
			        ordItemTicket.getTicketCount() != null) {
			    if (ordItemTicket.getActualCount() == ordItemTicket.getTicketCount()) {
				builder.append(" | ");
			        builder.append(ItemCancelEnum.ORDER_CANCEL_CODE.ALREADY.getCnName());
			    } else if (ordItemTicket.getTicketCount() != Long.getLong(ItemCancelEnum.ORDER_CANCEL_CODE.WITHOUT.getCode()) &&
			            ordItemTicket.getTicketCount() < ordItemTicket.getActualCount()) {
				builder.append(" | ");
			        builder.append(ItemCancelEnum.ORDER_CANCEL_CODE.PART.getCnName());
			    }
			}
	        }
		return builder.toString();
	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
	}
}
