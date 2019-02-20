package com.lvmama.vst.order.web;

import com.lvmama.comm.bee.vo.UserAddress;
import com.lvmama.comm.vst.vo.VstInvoiceAmountVo;
import com.lvmama.finance.service.InvoiceRemoteService;
import com.lvmama.finance.vo.InvoiceResponseVO;
import com.lvmama.finance.vo.InvoiceVO;
import com.lvmama.log.client.QueryLogClientService;
import com.lvmama.log.comm.bo.ComLogPams;
import com.lvmama.log.comm.utils.Pagination;
import com.lvmama.order.api.base.vo.RequestBody;
import com.lvmama.order.service.api.comm.invoice.IApiInvoiceApplyQueryService;
import com.lvmama.order.service.api.comm.invoice.IApiInvoiceApplyUpdateService;
import com.lvmama.order.vo.comm.invoice.OrdApplyInvoicePersonAddressVo;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.biz.service.BizCategoryQueryService;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.prod.po.ProdProduct.COMPANY_TYPE_DIC;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.*;
import com.lvmama.vst.comm.utils.json.*;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.web.HttpServletLocalThread;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Constant.*;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.ApplyInvoiceInfoResult;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderMonitorCnd;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.utils.InvoiceComp;
import com.lvmama.vst.order.utils.InvoiceResult;
import com.lvmama.vst.order.utils.InvoiceUtil;
import com.lvmama.vst.order.vo.OrdInvoiceListVo;
import com.lvmama.vst.pet.adapter.IReceiverUserServiceAdapter;
import com.lvmama.vst.pet.vo.PetUsrReceivers;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

@Controller
@RequestMapping("/order/orderInvoice")
public class OrderInvoiceAction extends BaseActionSupport {

	private static final Logger LOG = LoggerFactory.getLogger(OrderInvoiceAction.class);
	private static final long serialVersionUID = -8778128704309383962L;
	public static final String ORD_INVOICE_TEMPLATE_PATH = "/WEB-INF/resources/invoiceTemplate/invoiceTemplate.xls";
	public static final String ORD_ADDRESS_TEMPLATE_PATH = "/WEB-INF/resources/invoiceTemplate/invoiceAddressTemplate.xls";

	@Autowired
	private IOrdInvoiceService ordInvoiceService;

	@Autowired
	private IOrderInvoiceRelationService orderInvoiceRelationService;

	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private IOrdPersonService ordPersonService;

	@Autowired
	private IOrdAddressService ordAdressService;

	@Autowired
	private OrderService orderService;

	@Autowired
	protected DistrictClientService districtClientRemote;

	@Autowired
	private IReceiverUserServiceAdapter receiverUserServiceAdapter;

	@Autowired
	private IOrdOrderItemService ordOrderItemService;

	@Autowired
	private CategoryClientService categoryClientService;
	
	@Autowired
	private BizCategoryQueryService bizCategoryQueryService;
	
	@Autowired
	private DistributionBussiness distributionBussiness;
	
	@Autowired
	private IOrdOrderService ordOrderService;
	
	@Autowired
	private IOrdOrderPackService ordOrderPackService;
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private OrdApplyInvoiceInfoService ordApplyInvoiceInfoService;

	@Autowired
	private QueryLogClientService queryLogClientService;

	@Resource(name = "apiInvoiceApplyQueryService")
	private IApiInvoiceApplyQueryService apiInvoiceApplyQueryService;

	@Resource(name = "apiInvoiceApplyUpdateService")
	private IApiInvoiceApplyUpdateService apiInvoiceApplyUpdateService;

	@Resource(name = "invoiceRemoteService")
	private InvoiceRemoteService invoiceRemoteService;

	@Autowired
	private LvmmLogClientService lvmmLogClientService;

	
/*	@Autowired
	//private UserUserService userUserService;
*/	
	
	private final Integer DEFAULT_PAGE_SIZE = 50;
	private final Integer DESTHOTEL_PAGE_SIZE = 10;

	@RequestMapping(value = "/ord/goInvoceForm.do")
	public String goInvoiceForm() {
		resetInvoiceForm();
		return "/order/invoice/invoice_form";
	}
	
	@RequestMapping(value = "/ord/goInvoceFormDesthotel.do")
	public String goInvoiceFormDestbuHotel(Model model) {
		List<BizCategory> bizCategoryList = bizCategoryQueryService.getAllValidCategorys();
		model.addAttribute("bizCategoryList", bizCategoryList); // 所有品类
		// 查询自由行子品类
		List<BizCategory> subCategoryList = bizCategoryQueryService.getBizCategorysByParentCategoryId(WineSplitConstants.ROUTE_FREEDOM);
		model.addAttribute("subCategoryList", subCategoryList);
		return "/order/invoice/invoice_form_applyInvoice";
	}
	/**
	 * 读取能开票的订单列表
	 * 
	 * @return
	 */
	@RequestMapping(value = "/ord/waitInvoceOrder.do")
	public String waitInvoceOrder(Model model, Integer page, Integer pageSize,
			OrderMonitorCnd monitorCnd, HttpServletRequest request,
			HttpServletResponse response) {
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();

		condition.getOrderExcludedParam().setOrderStatus(OrderEnum.ORDER_STATUS.CANCEL.name());// 订单状态 不等于已取消

		condition.getOrderStatusParam().setPaymentStatus(OrderEnum.ORDER_VIEW_STATUS.PAYED.name());// 订单支付状态 已付款

		condition.getOrderContentParam().setPayTarget(SuppGoods.PAYTARGET.PREPAID.name()); // 支付方式为 预付

		condition.getOrderContentParam().setNeedInvoice("false,part");// 包含没有开发票以及开过部分票的订单
		
		condition.getOrderFlagParam().setOrderItemTableFlag(true);

		if (StringUtils.isNotEmpty(monitorCnd.getBookerName())) {// 驴妈妈账号
			condition.getOrderContentParam().setBookerName(monitorCnd.getBookerName());
		}
		if (StringUtils.isNotEmpty(monitorCnd.getBookerEmail())) {// 邮箱
			condition.getOrderContentParam().setBookEmail(monitorCnd.getBookerEmail());
		}

		if (StringUtils.isNotEmpty(monitorCnd.getBookerMobile())) {// 账号绑定的手机号
			condition.getOrderContentParam().setBookerMobile(monitorCnd.getBookerMobile());
		}

		if (StringUtils.isNotEmpty(monitorCnd.getContactName())) {// 联系人姓名
			condition.getOrderContentParam().setContactName(monitorCnd.getContactName());
		}
		if (StringUtils.isNotEmpty(monitorCnd.getContactMobile())) {// 联系人手机
			condition.getOrderContentParam().setContactMobile(monitorCnd.getContactMobile());
		}
		if (monitorCnd.getOrderId() != null) { // 订单编号
			condition.getOrderIndentityParam().setOrderId(monitorCnd.getOrderId());
		}
		
		//组装订单分页类条件
		Integer currentPage = page == null ? 1 : page;
		Integer currentPageSize = pageSize == null ? DEFAULT_PAGE_SIZE: pageSize;
		
		// 计算出每页的rownum
		condition.getOrderFlagParam().setOrderPageFlag(true);
		condition.getOrderPageIndexParam().setBeginIndex((currentPage - 1) * currentPageSize + 1);
		condition.getOrderPageIndexParam().setEndIndex(currentPage*currentPageSize);
		

		// 根据条件获取订单总记录数
		Long totalCount = complexQueryService.queryOrderCountByCondition(condition);
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);

		// 组装分页结果
		Page resultPage = buildResultPage(orderList, page, pageSize, totalCount, request);
		
		BizCategory bizCategory = null;
		if (null != orderList && !orderList.isEmpty()) {
			for (OrdOrder order : orderList) {
				List<OrdPerson> ordPersonList = new ArrayList<OrdPerson>();
				Map<String,Object> params = new HashMap<String, Object>();
				params.put("objectId", order.getOrderId());
				List<OrdPerson> personList = ordPersonService.findOrdPersonList(params);
				if(personList !=null && personList.size()>0){
					ordPersonList.addAll(personList);
				}
				order.setOrdPersonList(ordPersonList);
				
				bizCategory = categoryClientService.findCategoryById(order.getCategoryId()).getReturnContent(); 
				if(bizCategory != null && StringUtils.isNotEmpty(bizCategory.getCategoryName())){
					order.setCodeType(bizCategory.getCategoryName());
				}
				
				
			}
		}
		
		// 公司主体
		Map<String, String> companyTypeMap = new HashMap<String, String>();
		for (COMPANY_TYPE_DIC item : COMPANY_TYPE_DIC.values()) {
			companyTypeMap.put(item.name(), item.getTitle());
		}
		model.addAttribute("companyTypeMap", companyTypeMap);
		
		// 存储分页结果
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("monitorCnd", monitorCnd);
		return "/order/invoice/invoice_form";
	}
	
	
	/**
	 * 查询酒店待申请发票信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/ord/prepareApplyInvoiceInfo.do")
	public String ApplyInvoiceInfo(Model model, Integer page, Integer pageSize,
			OrderMonitorCnd monitorCnd, HttpServletRequest request,
			HttpServletResponse response) {
		
		Map <String, Object> map = new HashMap <String, Object>();
		if(StringUtils.isNotBlank(monitorCnd.getBookerName())){// 下单人姓名
			map.put("bookName", monitorCnd.getBookerName().trim());
		}
		if(StringUtils.isNotBlank(monitorCnd.getBookerMobile())){// 下单人手机
			map.put("bookMobile", monitorCnd.getBookerMobile().trim());
		}
		
		if(null != monitorCnd.getOrderId()){// 订单编号
			map.put("orderId", monitorCnd.getOrderId());
		}
		
		if(StringUtils.isNotBlank(monitorCnd.getContactName())){// 联系人姓名
			map.put("contactName", monitorCnd.getContactName().trim());
		}
		
		if(StringUtils.isNotBlank(monitorCnd.getContactMobile())){// 联系人手机
			map.put("contactMobile", monitorCnd.getContactMobile().trim());
		}
		
		if(StringUtils.isNotBlank(monitorCnd.getOrderStatus())){// 申请状态
			map.put("status", monitorCnd.getOrderStatus().trim());
		}
		
	    if(StringUtils.isNotBlank(monitorCnd.getPurchaseWay())){// 申请状态
	        map.put("purchaseWay", monitorCnd.getPurchaseWay().trim());
	    }
		
		if(monitorCnd.getCategoryId() != null){// 申请状态
			map.put("categoryId", monitorCnd.getCategoryId());
		}
		
		if(monitorCnd.getSubCategoryId() != null){// 申请状态
			map.put("subCategoryId", monitorCnd.getSubCategoryId());
		}
		// 查询品类
		List<BizCategory> bizCategoryList = bizCategoryQueryService.getAllValidCategorys();
		//除去非跟团游，当地游，自由行，酒店套餐的所有品类
//		Iterator<BizCategory> bizCategory = bizCategoryList.iterator();
//		while (bizCategory.hasNext()) {
//		    BizCategory biz = bizCategory.next();
//		    if((biz.getCategoryId().intValue()<15 && biz.getCategoryId().intValue()!=1) || (biz.getCategoryId().intValue()>18 && biz.getCategoryId().intValue() != 32)){
//		        bizCategory.remove();
//		    }
//            
//        }

		model.addAttribute("bizCategoryList", bizCategoryList); // 所有品类

		// 查询自由行子品类
		List<BizCategory> subCategoryList = bizCategoryQueryService.getBizCategorysByParentCategoryId(WineSplitConstants.ROUTE_FREEDOM);
		model.addAttribute("subCategoryList", subCategoryList);
		
		
//		log.info("$$$$$$$$$$$ map = " + map);
		
		Integer currentPageSize = pageSize == null? DESTHOTEL_PAGE_SIZE : pageSize;
		//同步已申请状态订单数据到临时表
		ordApplyInvoiceInfoService.updateApplyInvoiceList(map);
		// 根据条件获取待申请发票信息总条数
		Integer totalCount = ordApplyInvoiceInfoService.getApplyInvoiceInfoCount(map);
		//构建分页对象
		Page pageParam = buildResultPage(page, currentPageSize, totalCount, request);
		map.put("_start", pageParam.getStartRows());
		map.put("_end", pageParam.getEndRows());
		List<ApplyInvoiceInfoResult> orderList = ordApplyInvoiceInfoService.getPreparyApplyInvoiceInfo(map);
		
		//设置当前页面显示数据大小
		pageParam.setPageSize(currentPageSize);
		// 存储分页结果
		pageParam.setItems(orderList);
		model.addAttribute("resultPage", pageParam);
		model.addAttribute("monitorCnd", monitorCnd);//查询参数
		return "/order/invoice/invoice_form_applyInvoice";
	}
	
	
	/**
	 * 组装分页对象
	 * 
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
	
	
	@RequestMapping(value = "/ord/invoiceAddReq.do")
	@ResponseBody
	public Object addSession(ModelMap model, String orderids) {
		ResultMessage msg = ResultMessage.createResultMessage();
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		
		String[] orders = orderids.split(",");
		OrdOrder ordOrder = null;
		Map<Long,OrdOrder> content = getInvoiceSession();
		content.clear();
		Map<String, Object> attributes = new HashMap<String, Object>();
		Long userNo=null;
		int invoiceCount = 0;
		
		
		try {
			if(!content.isEmpty()){
				userNo = content.values().iterator().next().getUserNo();
				invoiceCount=content.size();
			}
			BizCategory bizCategoryOne = null;
			BizCategory bizCategory = null;
			String companyTypeOne = null;	// 公司主体类型
			
			if(orders.length >= 2){
				Long orderOneId = NumberUtils.toLong(orders[0]);
				OrdOrder orderone = orderService.queryOrdorderByOrderId(orderOneId);
				bizCategoryOne = categoryClientService.findCategoryById(orderone.getCategoryId()).getReturnContent();
				
				// 公司主体
				companyTypeOne = orderone.getCompanyType();
				if (StringUtils.isBlank(companyTypeOne)) {
					companyTypeOne = COMPANY_TYPE_DIC.XINGLV.name();
				}
			}
			
			for (String orderId : orders) {
				Long ordId = NumberUtils.toLong(orderId);
				condition.getOrderIndentityParam().setOrderId(ordId);           
				condition.getOrderContentParam().setNeedInvoice("false,part");// 包含没有发票以及开过部分票的订单
				List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
			
				for(OrdOrder order: orderList){
					bizCategory = categoryClientService.findCategoryById(order.getCategoryId()).getReturnContent();
					if(bizCategory != null && bizCategory.getCategoryId().longValue() == 3){
						throw new IllegalArgumentException("此订单为保险订单，不能申请开票，请联系客服开具定额发票！");
					}
					
					if(bizCategoryOne != null && bizCategory != null){
						Constant.CODE_TYPE type = InvoiceUtil.getInvoiceDetail(bizCategoryOne.getCategoryCode());
						Constant.CODE_TYPE newType = InvoiceUtil.getInvoiceDetail(bizCategory.getCategoryCode());
						
						if(!bizCategoryOne.getCategoryCode().equals(bizCategory.getCategoryCode())){
							if(!type.equals(newType)){
								throw new IllegalArgumentException("类型不相同的不能申请和开票！");
							}
						}
					}       
					
					// 多订单开票，公司主体必须相同
					String companyTypeItem = order.getCompanyType();
					if (StringUtils.isBlank(companyTypeItem)) {
						companyTypeItem = COMPANY_TYPE_DIC.XINGLV.name();
					}
					if(orders.length >= 2 && !companyTypeItem.equals(companyTypeOne)){
						throw new IllegalArgumentException("公司主体不相同的不能申请和开票！");
					}
				}
				if(orderList.size() == 0){
					throw new IllegalArgumentException("申请过订单不能重复申请开发票！");
				}
				
				ordOrder = orderService.queryOrdorderByOrderId(NumberUtils.toLong(orderId));
				if(ordOrder!=null){
					if (userNo == null) {
						userNo = ordOrder.getUserNo();
					} else if (!userNo.equals(ordOrder.getUserNo()) && orders.length>1) {
						throw new IllegalArgumentException("不是同一个用户的订单不能申请合开发票！");
					}
					invoiceCount++;
					if(StringUtils.equals("part", ordOrder.getNeedInvoice())){
						if(invoiceCount>1){
							throw new IllegalArgumentException("申请过订单不能和其他票合开发票！");
						}
					}
					if(ordOrder.getOughtAmount()<0.1){
						throw new IllegalArgumentException("申请订单金额低于0。1不能开发票！");
					}
					content.put(ordOrder.getOrderId(), ordOrder);
				}
			}
			
					
			
		} catch (IllegalArgumentException ex) {
			attributes.put("error", ex);
			msg.setAttributes(attributes);
			msg.setCode("error");
		}
		putInvoiceSession(content);
		return msg;
	}
	
	
	static class NotSameException extends Exception{
		/**
		 * 
		 */
		private static final long serialVersionUID = 5278253298534519214L;
		Long orderId;
		public NotSameException(Long orderId) {
			super();
			this.orderId=orderId;
		}
		/**
		 * @return the orderId
		 */
		public Long getOrderId() {
			return orderId;
		}
		
	}
	

	/**
	 * 加载地址列表
	 * */
	@RequestMapping(value = "/ord/loadAddresses.do")
	public String loadAddresses(ModelMap model, Long orderId, Long index) {
		OrdOrder ordorder = orderService.queryOrdorderByOrderId(orderId);
		List<UserAddress> userAddressList = receiverUserServiceAdapter.queryAddressByUserNo(ordorder.getUserId());
		model.put("userAddressList", userAddressList);
		model.put("index", index);
		model.put("orderId", orderId);
		model.put("userAddressListCount", userAddressList.size());
		
//		List<UserAddress> userAddressListNew = new ArrayList<UserAddress>();
//		for (int i = 0; i < userAddressList.size(); i++) {
//			if(i<=20){
//				userAddressListNew.add(userAddressList.get(i));
//			}
//		}
//		model.put("userAddressList", userAddressListNew);
//		model.put("index", index);
//		model.put("orderId", orderId);
//		model.put("userAddressCount", userAddressList.size());
//		if(userAddressList.size() > 20){
//			model.put("userAddressSubtractCount", userAddressList.size()-20);
//		}else{
//			model.put("userAddressSubtractCount", 0);
//		}
		return "/order/invoice/address";
	}

	/**
	 * 最多可开发票张数 合并开票只能开一张票
	 * 
	 * @return
	 */
	public int getInvoiceNum(List<OrdOrder> orderList) {
		if(orderList.size()==1){
			return orderList.get(0).getOrdTravellerList().size();
		}else{
			return 1;
		}
	}

	@RequestMapping("/ord/waitToAddList.do")
	public String waitToAdd(ModelMap model, String orderIds) {
		List<OrdOrder> orderList = new ArrayList<OrdOrder>(getContentsOrderId());
		int InvoiceNum = getInvoiceNum(orderList);
		
		Map<String,List<Object>> map = new HashMap<String, List<Object>>();
		BizCategory bizCategory = null;
		try {
			for(OrdOrder order: orderList ){
				bizCategory = categoryClientService.findCategoryById(order.getCategoryId()).getReturnContent();
				if(bizCategory != null){
					List<Object> objects = InvoiceUtil.getInvoiceContents(bizCategory);
					map.put("inv_"+order.getOrderId(), objects);
				}
			}
			
		} catch (Exception e) {
			e.getStackTrace();
		}
		long t = getTotalAmount(orderList);
		String amountYuan = PriceUtil.trans2YuanStr(t);
		
		// 公司主体
		Map<String, String> companyTypeMap = new HashMap<String, String>();
		for (COMPANY_TYPE_DIC item : COMPANY_TYPE_DIC.values()) {
			companyTypeMap.put(item.name(), item.getTitle());
		}
		model.addAttribute("companyTypeMap", companyTypeMap);

		model.put("orderIds", orderIds);
		model.put("InvoiceNum", InvoiceNum);
		model.put("invoiceContentMap", map);
		model.put("orderList", orderList);
		model.put("amountYuan", amountYuan);

		return "/order/invoice/wait_to_add";
	}

	@RequestMapping("/ord/goAddInvoiceInfo.do")
	public String goAddInvoiceInfo(ModelMap model, String orderIds,
			String selectId, Long invoiceNumber, String amountYuan) {
		// 公司主体
		Map<String, String> companyTypeMap = new HashMap<String, String>();
		for (COMPANY_TYPE_DIC item : COMPANY_TYPE_DIC.values()) {
			companyTypeMap.put(item.name(), item.getTitle());
		}
		model.addAttribute("companyTypeMap", companyTypeMap);
				
		Map<String, Object> mapId = new HashMap<String, Object>();
		String[] orders = orderIds.split(",");
		for (String orderId : orders) {
			OrdOrder ordorder = orderService.queryOrdorderByOrderId(Long.parseLong(orderId));
			mapId.put("orderId", orderId);
			mapId.put("userId", ordorder.getUserId());
			mapId.put("companyType", ordorder.getCompanyType());
		}
		Set<String> set = new HashSet<String>();
		String[] strs = selectId.split(",");
		for (int i = 0; i < strs.length; i++) {
			set.add(strs[i]);
		}
		model.put("selectSet", set);

		List<Long> array = new ArrayList<Long>();
		if (invoiceNumber != null) {
			array = new ArrayList<Long>(invoiceNumber.intValue());
			for (long i = 0; i < invoiceNumber; i++) {
				array.add(i);
			}
		} else {// 只开一张发票
			array.add(NumberUtils.toLong("0"));
		}
		model.put("invoiceNumber", array);
		model.put("ordInvoiceList", mapId);
		model.put("amountYuan", amountYuan);

		DELIVERY_TYPE[] deliveryTypeList = Constant.DELIVERY_TYPE.values();
		model.put("deliveryTypeList", deliveryTypeList);

		/*Map<String, Object> map = new HashMap<String, Object>();
		map.put("objectType", "ORD_INVOICE");
		List<OrdPerson> personList = ordPersonService.findOrdPersonList(map);
		for (OrdPerson person : personList) {
			map.put("ordPersonId", person.getOrdPersonId());
			List<OrdAddress> list = ordAdressService.findOrdAddressList(map);
			person.setAddressList(list);
		}
<<<<<<< .working
		model.put("personList", personList);*/
		
		model.put("orderIds", orderIds);
		return "/order/invoice/add_invoice_info";
	}

	/**
	 * 删除订单
	 * 
	 * @param model
	 * @param orderId
	 * @return
	 */
	@RequestMapping("/ord/removeOrderInInvoice.do")
	@ResponseBody
	public Object removeOrder(ModelMap model, String orderId) {
		ResultMessage msg = ResultMessage.createResultMessage();
		Map<String, Object> attributes = new HashMap<String, Object>();
		try {
			if (StringUtils.isNotEmpty(orderId)) {
				if (remove(NumberUtils.toLong(orderId))) {
					Collection<OrdOrder> orderList = getContentsOrderId();
					Long t = getTotalAmount(orderList);
					String amountYuan = PriceUtil.trans2YuanStr(t);
					attributes.put("amountYuan", amountYuan);
					msg.setAttributes(attributes);
					msg.setCode("success");
				} else {
					throw new Exception("没有找到订单内容");
				}
			}
		} catch (Exception ex) {
			msg.setCode("error");
		}
		return msg;
	}
	
	/**
	 * 删除地址
	 * 
	 * @param model
	 * @param orderId
	 * @return
	 */
	@RequestMapping("/ord/removeAddressInvoice.do")
	@ResponseBody
	public Object removeAddress(ModelMap model, String addressNo,String userNo) {
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			if (StringUtils.isNotEmpty(addressNo)) {
				receiverUserServiceAdapter.deleteAddress(addressNo,userNo);
				msg.setCode("success");
			}
		} catch (Exception ex) {
			msg.setCode("error");
		}
		return msg;
		
	}
	
	
	@SuppressWarnings("unchecked")
	private Map<Long,OrdOrder> getInvoiceSession(){
		Map<Long,OrdOrder> map = (Map<Long,OrdOrder>)getSession("INVOICE_FORM");
		if(map==null){
			map = new LinkedHashMap<Long, OrdOrder>();
		}
		return map;
	}
	private void resetInvoiceForm(){
		Map<Long,OrdOrder> content = getInvoiceSession();
		if(!content.isEmpty()){
			content = new LinkedHashMap<Long, OrdOrder>();
		}
		putInvoiceSession(content);
	}
	
	private void putInvoiceSession(Map<Long,OrdOrder> content){
		putSession("INVOICE_FORM", content);
	}


	public Collection<OrdOrder> getContentsOrderId() {
		return getInvoiceSession().values();
	}

	public boolean remove(Long orderId) {
		Map<Long,OrdOrder> map = getInvoiceSession();
		if(map.containsKey(orderId)){
			map.remove(orderId);
			putInvoiceSession(map);
			return true;
		}
		return false;
	}

	/**
	 * 弹出新增地址框
	 */
	@RequestMapping("/ord/doAddAddress.do")
	public String doAddAddress(ModelMap model, Long orderId) {
		OrdOrder ordorder = orderService.queryOrdorderByOrderId(orderId);
		Map<String, Object> mapUserId = new HashMap<String, Object>();
		mapUserId.put("userId", ordorder.getUserId());
		mapUserId.put("orderId", orderId);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("parentId", 8);
		List<BizDistrict> provinceNameList = districtClientRemote.findDistrictList(params).getReturnContent();
		Collections.sort(provinceNameList, new Comparator<BizDistrict>() {
			@Override
			public int compare(BizDistrict o1, BizDistrict o2) {
				if (o1.getDistrictId() > o2.getDistrictId()) {
					return 1;
				} else if (o1.getDistrictId() < o2.getDistrictId()) {
					return -1;
				}
				return 0;
			}
		});
		model.put("provinceNameList", provinceNameList);
		model.put("mapUserId", mapUserId);

		return "/order/invoice/insertAddress";
	}

	@RequestMapping(value = "/ord/selCityName.do")
	@ResponseBody
	public void selCityName(ModelMap model, Long districtId) {
		// 组装查询条件
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("parentId", districtId);
		// 调用联想查询接口
		List<BizDistrict> provienceList = districtClientRemote.findDistrictList(params).getReturnContent();

		Collections.sort(provienceList, new Comparator<BizDistrict>() {
			@Override
			public int compare(BizDistrict o1, BizDistrict o2) {
				if (o1.getDistrictId() > o2.getDistrictId()) {
					return 1;
				} else if (o1.getDistrictId() < o2.getDistrictId()) {
					return -1;
				}
				return 0;
			}
		});
		
		String json = JSONArray.fromObject(provienceList).toString();
		this.sendAjaxResultByJson(json, HttpServletLocalThread.getResponse());
	}

	/**
	 * 新增地址
	 */
	@RequestMapping("/ord/saveAddress.do")
	@ResponseBody
	public Object saveAddress(PetUsrReceivers petUsrReceivers, String userId) {
		ResultMessage msg = ResultMessage.createResultMessage();
		UserAddress user = new UserAddress();
		
		String province = petUsrReceivers.getProvince().split("@")[0];
		String provinceName = petUsrReceivers.getProvince().split("@")[1];
		
		String city = petUsrReceivers.getCity().split("@")[0];
		String cityName = petUsrReceivers.getCity().split("@")[1];
		
		try {
			if(province.equals("0")){
				user.setProvince("");
			}else{
				user.setProvince(provinceName);
			}
			
			if(!StringUtils.isNotEmpty(city) || city.equals("0")){
				user.setCity("");
			}else{
				user.setCity(cityName);
			}
			
			user.setAddress(petUsrReceivers.getAddress());
			user.setUserName(petUsrReceivers.getReceiverName());
			user.setMobileNumber(petUsrReceivers.getMobileNumber());
			user.setPostCode(petUsrReceivers.getPostCode());
			user.setUserNo(userId);
			user.setIsValid(PetUsrReceivers.VALID);
//			user.setAddressId(UUID.randomUUID().toString().replace("-", "").toUpperCase());

			//receiverUserServiceAdapter.createAddress(petUsrReceivers);
			
			receiverUserServiceAdapter.saveOrUpdateAddress(user, userId);

		} catch (Exception e) {
			e.getStackTrace();
		}
		return msg;
	}

	/**
	 * 计算发票总金额 减去奖金支付: order.getBonusAmount() 
	 *             退款金额：order.getRefundedAmount()
	 *             保险金额：order.getInsuranceAmountYuan()
	 * @param orderIds
	 * @return
	 */
	public long getTotalAmount(Collection<OrdOrder> orderList) {
		long total = 0;
		
		if(CollectionUtils.isNotEmpty(orderList)){
			for(OrdOrder order:orderList){
//				total +=order.getInvoiceAmount();
				VstInvoiceAmountVo vstInvoiceAmountVo = ordInvoiceService.getInvoiceAmount(order.getOrderId());
				Long invoiceAmount = vstInvoiceAmountVo.getInvoiceAmount();
				total +=invoiceAmount;
			}
			if (orderList.size() == 1) {
				OrdOrder order = orderList.iterator().next();
				if (StringUtils.equals("part", order.getNeedInvoice())) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("orderId", order.getOrderId());
					Long invoiceAmount=ordInvoiceService.getInvoiceAmountSum(map);
					if(invoiceAmount!=null){
						total -= invoiceAmount;
					}
				}
			}
		}
		return total;
	}

	/**
	 * 添加发票
	 * @throws Exception 
	 */
	@RequestMapping("/ord/saveCompositeInvoice.do")
	@ResponseBody
	public void saveCompositeInvoice(OrdInvoiceListVo invoicepos) throws Exception {
		Collection<OrdOrder> tmp = getContentsOrderId();		
		List<OrdOrder> orderList = new ArrayList<OrdOrder>(tmp);

		JSONResult result = new JSONResult();

		// 计算总金额
		long m = getTotalAmount(orderList);

		long temp = 0L;
		List<Pair<OrdInvoice, OrdPerson>> invoices = new ArrayList<Pair<OrdInvoice, OrdPerson>>();
		try {
			int pos = 0;
			for (OrdInvoice or : invoicepos.getForm()) {
				OrdPerson ordPerson = new OrdPerson();
				String invoiceAddressId = getRequest().getParameter("invoiceAddressId" + (pos++)); // 送货方式

			 	if (or.getAmount() != null) {// 发票金额
					temp = temp + or.getAmount();
				}
				if (pos == invoicepos.getForm().size()) {
					or.setAmount(m - temp);
					if (or.getAmount() <100) {
						throw new Exception("开票金额少于1元不能申请开发票！");
					}
				}

				if (!or.getDeliveryType().equals("SELF") && invoiceAddressId != null) {
					if (StringUtils.isEmpty(invoiceAddressId)) {
						throw new Exception("送货地址不可以为空");
					}
					ordPerson = initAddress(invoiceAddressId); // 游玩人和地址进行关联 
				}
				or.setContent(or.getContent()); // 发票内容
				or.setCompany(Constant.INVOICE_COMPANY.COMPANY_3.name());//公司
				or.setPurchaseWay(or.getPurchaseWay());//购买方式
				or.setTaxNumber(or.getTaxNumber());//纳税人识别号
				or.setBuyerAddress(or.getBuyerAddress());//购买方地址
				or.setBuyerTelephone(or.getBuyerTelephone());//购买方电话
				or.setBankAccount(or.getBankAccount());//开户银行
				or.setAccountBankAccount(or.getAccountBankAccount());//开户银行账号
				
				Pair<OrdInvoice, OrdPerson> kv = Pair.make_pair(or, ordPerson);
				invoices.add(kv);
			}

			ResultHandle handle = insertInvoiceByOrders(invoices, orderList,getLoginUserId());
			if (handle.isFail()) {
				result.raise(handle.getMsg());
			}
			resetInvoiceForm();
		} catch (Exception e) {
			result.raise(e);
		}
		result.output(getResponse());
	}
	
	private ResultHandle insertInvoiceByOrders(
			final List<Pair<OrdInvoice, OrdPerson>> invoices,
			final List<OrdOrder> orderIds, final String operatorId) {
		ResultHandle handle = new ResultHandle();
		if (CollectionUtils.isEmpty(orderIds)) {
			handle.setMsg("订单号为空");
		} else {
			
			// 同步“公司主体”信息至发票 (使用页面传递过来的“公司主体”)
			/*if (CollectionUtils.isNotEmpty(invoices)) {
				for (Pair<OrdInvoice, OrdPerson> item : invoices) {
					OrdInvoice ordInvoice = item.getFirst();
					ordInvoice.setCompanyType(orderIds.get(0).getCompanyType());
				}
			}*/
			
			// 公司主体
			String companyTypeOne = orderIds.get(0).getCompanyType();
			if (StringUtils.isBlank(companyTypeOne)) {
				companyTypeOne = COMPANY_TYPE_DIC.XINGLV.name();
			}
			// 多订单，公司主体不同，不允许开票
			if (orderIds.size() > 1) {
				for (OrdOrder item : orderIds) {
					String companyTypeItem = item.getCompanyType();
					if (StringUtils.isBlank(companyTypeItem)) {
						companyTypeItem = COMPANY_TYPE_DIC.XINGLV.name();
					}
					if(!companyTypeItem.equals(companyTypeOne)){
						throw new IllegalArgumentException("公司主体不相同的不能申请和开票！");
					}
				}
			}
			
			try {
				if (orderIds.size() == 1) {
					ordInvoiceService.insert(invoices, orderIds.get(0).getOrderId(),operatorId);
				} else {
					if (invoices.size() > 1) {
						throw new Exception("多个订单号不可开多张发票");
					}
					ordInvoiceService.insert(invoices.get(0), orderIds,operatorId);
				}
			} catch (Exception ex) {
				handle.setMsg(ex.getMessage());
			}
		}
		return handle;
	}

	private OrdPerson initAddress(String id) {
//		PetUsrReceivers petUsrReceivers = receiverUserServiceAdapter.getRecieverByPk(id);
		
		UserAddress userAddress = receiverUserServiceAdapter.queryAddressByAddressNo(id);
		 
		if (userAddress == null) {
			throw new NullPointerException("收件地址不存在");
		}
		// 地址
		List<OrdAddress> addressList = new ArrayList<OrdAddress>();
		OrdAddress ordAddress = new OrdAddress();
		ordAddress.setProvince(userAddress.getProvince());
		ordAddress.setCity(userAddress.getCity());
		ordAddress.setStreet(userAddress.getAddress());
		ordAddress.setPostalCode(userAddress.getPostCode());
		addressList.add(ordAddress);

		// 游玩人
		OrdPerson ordPerson = new OrdPerson();
		ordPerson.setFullName(userAddress.getUserName());
		ordPerson.setMobile(userAddress.getMobileNumber());
		ordPerson.setObjectType("ORD_INVOICE");
		ordPerson.setPersonType(IReceiverUserServiceAdapter.RECEIVERS_TYPE.ADDRESS.name());
		ordPerson.setAddressList(addressList);
		return ordPerson;
	}

	/**
	 * 发票管理
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping("/ord/goInvoiceList.do")
	public String goInvoiceList(ModelMap model) {
		INVOICE_STATUS[] invoiceStatus = Constant.INVOICE_STATUS.values();
		model.addAttribute("invoiceStatus", invoiceStatus);
		INVOICE_COMPANY[] invoiceCompany = Constant.INVOICE_COMPANY.values();
		model.addAttribute("invoiceCompany", invoiceCompany);
		INVOICE_LOGISTICS[] logistics = Constant.INVOICE_LOGISTICS.values();
		model.addAttribute("logistics", logistics);
		DELIVERY_TYPE[] deliveryType = Constant.DELIVERY_TYPE.values();
		model.addAttribute("deliveryType", deliveryType);
		FILIALE_NAME[] filialeName = Constant.FILIALE_NAME.values();
		model.addAttribute("filialeName", filialeName);
		return "/order/invoice/invoice_list";
	}

	/**
	 * 发票红冲管理
	 * 
	 * @return
	 */
	@RequestMapping("/ord/goRedInvoiceList.do")
	public String goRedInvoiceList() {
		return "/order/invoice/redInvoice_list";
	}

	/**
	 * 初始化ComplexQuerySQLCondition
	 * 
	 * @return
	 */
	private Map<String, Object> init(OrdInvoice ordInvoice, Date startTime, Date endTime,Date billStartTime,Date billEndTime,OrdOrder order) {
		Map<String, Object> param = new HashMap<String, Object>();
		if (ordInvoice.getOrderId() != null) {
			param.put("orderId", order.getOrderId());
		}
		if (StringUtils.isNotEmpty(ordInvoice.getInvoiceNo())) { // 发票单号
			param.put("invoiceNo", ordInvoice.getInvoiceNo());
		}
		if (StringUtils.isNotEmpty(order.getUserId())){ //订票人
			param.put("userId", order.getUserId());
		}
		if (ordInvoice.getOrdInvoiceId() != null) {
			param.put("ordInvoiceId", ordInvoice.getOrdInvoiceId());
		}
		if (StringUtils.isNotEmpty(ordInvoice.getDeliveryType())
				&& !ordInvoice.getDeliveryType().equals("0")) {
			param.put("deliveryType", ordInvoice.getDeliveryType());
		}
		if (StringUtils.isNotEmpty(ordInvoice.getDeliverStatus())
				&& !ordInvoice.getDeliverStatus().equals("0")) {
			param.put("deliverStatus", ordInvoice.getDeliverStatus());
		}
		if (StringUtils.isNotEmpty(ordInvoice.getStatus())
				&& !ordInvoice.getStatus().equals("0")) {
			param.put("status", ordInvoice.getStatus());
		}
		if (StringUtils.isNotEmpty(order.getFilialeName())
				&& !order.getFilialeName().equals("0")) {
			param.put("filialeName", order.getFilialeName());  //公司
		}
        
        param.put("visitTime", DateUtil.getDayEnd(new Date()));// 游玩时间必须在今天之前
        
		boolean flag = false;
		if (startTime != null && endTime != null) {
			if (startTime.before(endTime) || startTime.equals(endTime)) {
				param.put("startTime", DateUtil.getDayStart(startTime)); 
				param.put("endTime", DateUtil.getDayEnd(endTime));
				flag = true;
			}
		}
		
		if (!flag) {
			if (startTime != null) {
				param.put("startTime", DateUtil.getDayStart(startTime));
			} else if (endTime != null) {
				param.put("endTime", DateUtil.getDayEnd(endTime));
			}
		}
		flag = false;
		if (billStartTime != null && billEndTime != null) {
			if (billStartTime.before(billEndTime) || billStartTime.equals(billEndTime)) {
				param.put("billStartTime", DateUtil.getDayStart(billStartTime));
				param.put("billEndTime", DateUtil.getDayEnd(billEndTime));
				flag = true;
			}
		}
		if (!flag) {
			if (billStartTime != null) {
				param.put("billStartTime", DateUtil.getDayStart(billStartTime));
			} else if (billEndTime != null) {
				param.put("billEndTime", DateUtil.getDayEnd(billEndTime));
			}
		}
		return param;
	}


	/**
	 * 发票管理 查询信息
	 * 
	 * @return
	 */ 
	@RequestMapping("/ord/invoiceList.do")
	public String loadDataList(ModelMap model, Integer page, Integer pageSize,
			OrdInvoice ordInvoice,HttpServletRequest request, HttpServletResponse response,
			Date startTime, Date endTime,Date billStartTime,Date billEndTime,OrdOrder order) {
		
		Map<String, Object> param = init(ordInvoice, startTime, endTime,billStartTime, billEndTime, order);
		
		// 根据条件获取订单总记录数
		Long totalCount = ordInvoiceService.getInvoiceCount(param);

		Integer currentPage = page == null ? 1 : page;
		Integer currentPageSize = pageSize == null ? 10: pageSize;
		Page<OrdInvoice> resultPage = Page.page(totalCount.intValue(),currentPageSize, currentPage);
		resultPage.buildUrl(request);

		param.put("_start", resultPage.getStartRows());
		param.put("_end", resultPage.getEndRows());
		param.put("_orderby", "oi.CREATE_TIME asc");

		List<OrdInvoice> ordInvoiceList = ordInvoiceService.getOrdInvoiceListByParam(param);

		Map<String, Object> map = new HashMap<String, Object>();
		if (ordInvoiceList != null && ordInvoiceList.size() > 0) {
			for (OrdInvoice ordInvoices : ordInvoiceList) {
				List<OrdOrder> list = new ArrayList<OrdOrder>();
				map.put("ordInvoiceId", ordInvoices.getOrdInvoiceId());
				List<OrdInvoiceRelation> orderIdList = orderInvoiceRelationService.getListByParam(map);
				for (OrdInvoiceRelation ordInvoiceRelation : orderIdList) {
					OrdOrder ordorder = orderService.queryOrdorderByOrderId(ordInvoiceRelation.getOrderId());
					if (ordorder != null && ordInvoices.getOrdInvoiceId().equals(ordInvoiceRelation.getOrdInvoiceId())) {
						//发票金额
						VstInvoiceAmountVo vstInvoiceAmountVo = ordInvoiceService.getInvoiceAmount(ordorder.getOrderId());
						ordorder.setOrderInvoiceAmount(vstInvoiceAmountVo.getInvoiceAmount());
						list.add(ordorder);
					}
				}
				ordInvoices.setOrderList(list);
			}
		}
		resultPage.setItems(ordInvoiceList);
		
		// 存储分页结果
 		model.addAttribute("resultPage", resultPage);
		model.put("ordInvoice", ordInvoice);
		model.put("order", order);
		
		INVOICE_STATUS[] invoiceStatus = Constant.INVOICE_STATUS.values();
		model.addAttribute("invoiceStatus", invoiceStatus);
		INVOICE_LOGISTICS[] logistics = Constant.INVOICE_LOGISTICS.values();
		model.addAttribute("logistics", logistics);
		DELIVERY_TYPE[] deliveryType = Constant.DELIVERY_TYPE.values();
		model.addAttribute("deliveryType", deliveryType);
		FILIALE_NAME[] filialeName = Constant.FILIALE_NAME.values();
		model.addAttribute("filialeName", filialeName);
		return "/order/invoice/invoice_list";
	}
	
	/**
	 * 自动补全订票人userId
	 */
	@RequestMapping(value = "/ord/searchInvoice.do")
	@ResponseBody
	public void searchInvoice(String search, HttpServletResponse resp){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<searchUserId>");
		}
		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("userId", search);
		List<OrdOrder> list = ordOrderService.getordOrderList(param);
		
		JSONArray array = new JSONArray();
		if(list != null && list.size() > 0){
			for(OrdOrder order:list){
				JSONObject obj=new JSONObject();
				obj.put("text", order.getUserId());
				array.add(obj);
			}
		}
		JSONOutput.writeJSON(resp, array);
	}

	/**
	 * 发票红冲 查询信息
	 * 
	 * @param model
	 * @param invoiceNo
	 * @param orderId
	 * @param invoiceId
	 * @return
	 */
	@RequestMapping("/ord/redInvoiceList.do")
	public String loadRedDataList(ModelMap model, Integer page,Integer pageSize, OrdInvoice ordInvoice,OrdOrder ordorders) {
		Map<String, Object> param = init(ordInvoice,null,null, null,null,ordorders);
		param.put("redFlag", "true");

		// 根据条件获取订单总记录数
		Long totalCount = ordInvoiceService.getInvoiceCount(param);

		Integer currentPage = page == null ? 1 : page;
		Integer currentPageSize = pageSize == null ? DEFAULT_PAGE_SIZE: pageSize;
		Page<OrdInvoice> resultPage = Page.page(totalCount.intValue(),currentPageSize, currentPage);
		resultPage.buildUrl(HttpServletLocalThread.getRequest());
		
		param.put("_start", resultPage.getStartRows());
		param.put("_end", resultPage.getEndRows());
		param.put("_orderby","oi.CREATE_TIME desc");
		

		List<OrdInvoice> list = ordInvoiceService.getOrdInvoiceListByParam(param);
		resultPage.setItems(list);

		Map<String, Object> map = new HashMap<String, Object>();
		if (list != null && list.size() > 0) {
			for (OrdInvoice ordInvoices : list) {
				List<OrdOrder> lists = new ArrayList<OrdOrder>();
				map.put("ordInvoiceId", ordInvoices.getOrdInvoiceId());
				List<OrdInvoiceRelation> orderIdList = orderInvoiceRelationService.getListByParam(map);
				for (OrdInvoiceRelation ordInvoiceRelation : orderIdList) {
					OrdOrder ordorder = orderService.queryOrdorderByOrderId(ordInvoiceRelation.getOrderId());
					if (ordorder != null&& ordInvoices.getOrdInvoiceId().equals(ordInvoiceRelation.getOrdInvoiceId())) {
						//发票金额
						VstInvoiceAmountVo vstInvoiceAmountVo = ordInvoiceService.getInvoiceAmount(ordorder.getOrderId());
						ordorder.setOrderInvoiceAmount(vstInvoiceAmountVo.getInvoiceAmount());
						lists.add(ordorder);
					}
				}
				ordInvoices.setOrderList(lists);
			}
		}
		model.put("resultPage", resultPage);
		return "/order/invoice/redInvoice_list";
	}

	/**
	 * 根据订单id查询信息
	 */
	@RequestMapping("/ord/loadInvoices.do")
	public String loadInvoices(ModelMap model, Long orderId) {
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		Map<String, Object> map = new HashMap<String, Object>();

		if (orderId == null) {
			return null;
		}
		map.put("orderId", orderId);
		List<OrdInvoice> ordInvoiceList = ordInvoiceService
				.getOrdInvoiceListByOrderId(map);
		if (ordInvoiceList != null && ordInvoiceList.size() > 0) {
			for (OrdInvoice ordInvoice : ordInvoiceList) {
				List<OrdOrder> list = new ArrayList<OrdOrder>();
				map.clear();
				map.put("ordInvoiceId", ordInvoice.getOrdInvoiceId());
				List<OrdInvoiceRelation> ordInvoiceRelationList = orderInvoiceRelationService
						.getListByParam(map);
				for (OrdInvoiceRelation ordInvoiceRelation : ordInvoiceRelationList) {
					condition.getOrderIndentityParam().setOrderId(ordInvoiceRelation.getOrderId());
					List<OrdOrder> orderList = orderService.getOrdListByCondition(condition);
					if (orderList != null && orderList.size() > 0) {
						list.addAll(orderList);
					}
				}
				ordInvoice.setOrderList(list);
				
				if (ordInvoice.getDeliveryType() != null) {
					if (!ordInvoice.getDeliveryType().equals("SELF")) {
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("objectId", ordInvoice.getOrdInvoiceId());
						params.put("objectType", "ORD_INVOICE");
						List<OrdPerson> ordPersonList = ordPersonService.findOrdPersonList(params);
						if (ordPersonList != null && ordPersonList.size() > 0) {
							ordInvoice.setDeliveryAddress(ordPersonList.get(0));
							params.put("ordPersonId", ordPersonList.get(0).getOrdPersonId());
							List<OrdAddress> ordAddressList = ordAdressService.findOrdAddressList(params);
							if (ordAddressList != null && ordAddressList.size() > 0) {
								ordInvoice.getDeliveryAddress().setAddressList(ordAddressList);
							}
						}
					}
				}
			}
		}
 
		model.put("invoiceList", ordInvoiceList);
		return "/order/invoice/ord_invoice";
	}

	/**
	 * 查看发票详情
	 * 
	 * @param model
	 * @param invoiceId
	 * @return
	 */
	@RequestMapping("/ord/invoiceDetail.do") 
	public String doDetail(ModelMap model, Long invoiceId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ordInvoiceId", invoiceId);
		List<OrdInvoiceRelation> orderIdList = orderInvoiceRelationService.getListByParam(map);

		List<OrdOrder> orderList = new ArrayList<OrdOrder>();
		Map<String,List<OrdOrderItem>> ordOrderItemMap = new HashMap<String, List<OrdOrderItem>>();
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		if (orderIdList != null && orderIdList.size() > 0) {
			for (OrdInvoiceRelation ordInvoiceRelation : orderIdList) {
				condition.getOrderIndentityParam().setOrderId(ordInvoiceRelation.getOrderId());
				List<OrdOrder> list = orderService.getOrdListByCondition(condition);
				if (list != null && list.size() > 0) {
					orderList.addAll(list);
				}
				List<OrdOrderItem> ordOrderItemList = ordOrderItemService.selectByOrderId(ordInvoiceRelation.getOrderId());
				ordOrderItemMap.put("inv_"+ordInvoiceRelation.getOrderId(), ordOrderItemList);
			}
		}

		OrdInvoice ordInvoice = ordInvoiceService.selectByPrimaryKey(invoiceId);
		if (ordInvoice.getDeliveryType() != null) {
			if (!ordInvoice.getDeliveryType().equals("SELF")) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("objectId", ordInvoice.getOrdInvoiceId());
				params.put("objectType", "ORD_INVOICE");
				List<OrdPerson> ordPersonList = ordPersonService.findOrdPersonList(params);
				if (ordPersonList != null && ordPersonList.size() > 0) {
					ordInvoice.setDeliveryAddress(ordPersonList.get(0));
					params.put("ordPersonId", ordPersonList.get(0).getOrdPersonId());
					List<OrdAddress> list = ordAdressService.findOrdAddressList(params);
					if (list != null && list.size() > 0) {
						ordInvoice.getDeliveryAddress().setAddressList(list);
					}
				}
			}
		}

		List<com.lvmama.log.comm.po.ComLog> comlogList = this.getLog("ORD_INVOICE",invoiceId);

		boolean changInvoiceNo = false;
		if (ordInvoice.getStatus() != null)
			changInvoiceNo = checkChangeInvoiceNo(ordInvoice);
		model.put("orderList", orderList);
		model.put("orderItemMap", ordOrderItemMap);
		model.put("comLogList", comlogList);
		model.put("ordInvoice", ordInvoice);
		model.put("changInvoiceNo", changInvoiceNo);
		return "/order/invoice/invoice_detail";
	}
	
	/**
	 * 发票状态 操作: 取消,红冲,完成 ,审核
	 */
	@RequestMapping("/ord/invoiceChangeStatus.do")
	public void doChangeStatus(Long ordInvoiceId, String status) {
		JSONResult result = new JSONResult();
		try {
			Assert.hasLength(status, "变更的状态不可以为空");
			Assert.isTrue(!(ordInvoiceId == null || ordInvoiceId < 1),"发票序号不正确");

			OrdInvoice ordInvoice = ordInvoiceService.selectByPrimaryKey(ordInvoiceId);
			Assert.notNull(ordInvoice, "发票不存在");

			if (status.equals(ordInvoice.getStatus())) {
				throw new Exception("准备变更的状态与实际的状态是一样的");
			}
			InvoiceResult ir = ordInvoiceService.update(status, ordInvoiceId,getLoginUserId());

			if (ir.isError()) {
				throw new Exception(ir.getMsg());
			} else if (ir.isCancel()) {
				throw new JSONResultException(-2, ir.getMsg());
			} else {
				// 该数据给后面取其当前的值使用
				ordInvoice.setStatus(status);
			}
		} catch (JSONResultException ex) {
			result.raise(ex);
		} catch (Exception ex) {
			result.raise(new JSONResultException(ex.getMessage()));
		}
		result.output(getResponse());
	}

	/**
	 * 发票申请红冲操作.
	 */
	@RequestMapping("/ord/doReqRedInvoice.do")
	public void dochangeRedStatus(Long ordInvoiceId) {
		JSONResult result = new JSONResult();
		try {
			if (ordInvoiceId < 1) {
				throw new Exception("发票ID不存在");
			}

			OrdInvoice ordInvoice = ordInvoiceService.selectByPrimaryKey(ordInvoiceId);
			Assert.notNull(ordInvoice, "发票不存在");

			String redFlag = "true";
			InvoiceResult ir = ordInvoiceService.updateInvoiceRedFlag(ordInvoiceId, redFlag, getLoginUserId());
			if (ir.isError()) {
				throw new Exception(ir.getMsg());
			}
		} catch (Exception ex) {
			result.raise(ex);
		}

		result.output(getResponse());
	}

	/**
	 * 发票关闭红冲操作.
	 */
	@RequestMapping("/ord/doCloseRedInvoice.do")
	@ResponseBody
	public void doCloseRedStatus(ModelMap model, Long ordInvoiceId) {
		JSONResult result = new JSONResult();
		try {
			OrdInvoice ordInvoice = ordInvoiceService.selectByPrimaryKey(ordInvoiceId);
			Assert.notNull(ordInvoice, "发票不存在");

			String redFlag = "false";
			InvoiceResult ir = ordInvoiceService.updateInvoiceRedFlag(ordInvoiceId, redFlag, ordInvoice.getUserId());
			if (ir.isError()) {
				throw new Exception(ir.getMsg());
			}
		} catch (Exception ex) {
			result.raise(ex);
		}
		result.output(getResponse());
	}

	/**
	 * 快递单号修改
	 * 
	 * @param model
	 * @param ordInvoiceId
	 * @param expressNo
	 */
	@RequestMapping("/ord/updateInvoiceExpress.do")
	@ResponseBody
	public void doChangeExpress(ModelMap model, Long ordInvoiceId,
			String expressNo) {
		JSONResult result = new JSONResult();
		try {
			Assert.isTrue(!(ordInvoiceId == null || ordInvoiceId < 1),"发票序号不正确");

			OrdInvoice ordInvoice = ordInvoiceService.selectByPrimaryKey(ordInvoiceId);
			Assert.notNull(ordInvoice, "发票不存在");

			if(InvoiceUtil.checkChangeExpressNo(ordInvoice)){
				throw new Exception("只有已经开票的状态才可以添加快递单号");
			}
			
			if (!isShowInvoiceForm(ordInvoice)) {
				throw new Exception("当前状态不可以再变更发票号");
			}
			ordInvoiceService.updateInvoiceExpressNo(ordInvoiceId, expressNo,getLoginUserId());
		} catch (Exception ex) {
			result.raise(ex);
		}
		result.output(getResponse());
	}

	/**
	 * 修改发票号
	 * 
	 * @param model
	 * @param ordInvoiceId
	 * @param invoiceNo
	 */
	@RequestMapping("/ord/updateInvoiceNo.do")
	@ResponseBody
	public void doChangeNo(ModelMap model, Long ordInvoiceId, String invoiceNo) {
		JSONResult result = new JSONResult();
		try {
			Assert.isTrue(!(ordInvoiceId == null || ordInvoiceId < 1),"发票序号不正确");

			OrdInvoice ordInvoice = ordInvoiceService.selectByPrimaryKey(ordInvoiceId);
			Assert.notNull(ordInvoice, "发票不存在");

			if (!isShowInvoiceForm(ordInvoice)) {
				throw new Exception("当前状态不可以再变更发票号");
			}

			ordInvoiceService.updateInvoiceNo(ordInvoiceId, invoiceNo,getLoginUserId());

		} catch (Exception ex) {
			result.raise(new JSONResultException(ex.getMessage()));
		}

		result.output(getResponse());
	}

	/**
	 * 完成或取消的单号不再显示修改框
	 * 
	 * @return
	 */
	public boolean isShowInvoiceForm(OrdInvoice ordInvoice) {
		return !InvoiceUtil.checkChangeInvoiceNo(ordInvoice);
	}

	/**
	 * 审核状态
	 * 
	 * @param invoices
	 * @return
	 */
	@RequestMapping("/ord/invoiceListApprove.do")
	@ResponseBody
	public Object doApprove(String invoices) {
		String[] array = invoices.split(",");
		Map<String, Object> attributes = new HashMap<String, Object>();
		ResultMessage msg = ResultMessage.createResultMessage();
		JSONArray successObj;
		StringBuffer cancel;
		successObj = new JSONArray();
		cancel = new StringBuffer();
		JSONResult result = new JSONResult();
		try {
			changeStatusList(new InvoiceComp() {
				public boolean hasChangeAble(OrdInvoice ordInvoice) {
					return InvoiceUtil.checkChangeApproveAble(ordInvoice);
				}
			}, Constant.INVOICE_STATUS.APPROVE.name(), invoices);
			result.put("results", successObj);
			boolean hasCancel = false;
			if (StringUtils.isNotEmpty(cancel.toString())) {
				hasCancel = true;
				result.put("cancelMsg", cancel.toString());
			}
			result.put("cancel", hasCancel);
			attributes.put("hasCancel", hasCancel);
			attributes.put("cancel", cancel.toString());
		} catch (Exception ex) {
			result.raise(new JSONResultException(ex.getMessage()));
		}
		attributes.put("length", array.length);
		msg.setAttributes(attributes);
		return msg;
	}

	void changeStatusList(InvoiceComp comp, String status, String invoices) {
		JSONArray successObj;
		StringBuffer cancel;
		successObj = new JSONArray();
		cancel = new StringBuffer();
		String array[];
		array = StringUtils.split(invoices, ",");
		for (String id : array) {
			Long invoice = NumberUtils.toLong(id);
			if (invoice > 0) {
				try {
					OrdInvoice ordInvoice = ordInvoiceService.selectByPrimaryKey(invoice);
					// 为空或是已经取消的就跳过
					/*
					 * if (comp.hasChangeAble(ordInvoice)) { continue; }
					 */

					InvoiceResult ir = ordInvoiceService.update(status,invoice, getLoginUserId());
					if (ir.isSuccess()) {
						successObj.add(invoice);
					} else if (ir.isCancel()) {
						cancel.append(ir.getMsg());
						cancel.append("\n");
					}
				} catch (Exception ex) {
					LOG.error("{}", ex);
				}
			}
		}
	}

	/**
	 * 导出报表
	 * 
	 * @param model
	 * @param ids
	 */
	@RequestMapping("/ord/invoiceData.do")
	public void doOutputData(ModelMap model, OrdInvoice ordInvoice,Date startTime, Date endTime,Date billStartTime,Date billEndTime,OrdOrder order) {
		Map<String, Object> param = init(ordInvoice,startTime,endTime, billStartTime,billEndTime,order);
		param.put("_orderby", "oi.CREATE_TIME asc");
		List<OrdInvoice> ordInvoiceList = ordInvoiceService.getOrdInvoiceListByParam(param);

		Map<String, Object> map = new HashMap<String, Object>();
		OrdOrder ordorder = null;

		if (ordInvoiceList != null && ordInvoiceList.size() > 0) {
			for (OrdInvoice ordInvoices : ordInvoiceList) {
				List<OrdOrder> ordOrderList = new ArrayList<OrdOrder>();
				map.put("ordInvoiceId", ordInvoices.getOrdInvoiceId());
				List<OrdInvoiceRelation> orderIdList = orderInvoiceRelationService.getListByParam(map);
				for (OrdInvoiceRelation ordInvoiceRelation : orderIdList) {
					ordorder = orderService.queryOrdorderByOrderId(ordInvoiceRelation.getOrderId());
					
					if (ordorder != null) {
						ordorder.setDistributionName(distributionBussiness.getDistributionName(ordorder.getDistributorId()));//下单渠道
						//发票金额
						VstInvoiceAmountVo vstInvoiceAmountVo = ordInvoiceService.getInvoiceAmount(ordorder.getOrderId());
						ordorder.setOrderInvoiceAmount(vstInvoiceAmountVo.getInvoiceAmount());
						ordOrderList.add(ordorder);
						map.clear();
						map.put("objectType", "ORD_INVOICE");
						map.put("objectId", ordInvoices.getOrdInvoiceId());
						List<OrdPerson> personList = ordPersonService.findOrdPersonList(map);
						if (personList != null && personList.size() > 0) {
							for(OrdPerson person:personList){
								ordInvoices.setDeliveryAddress(person);
								if(person.getOrdPersonId()!=null){
									map.clear();
									map.put("ordPersonId", person.getOrdPersonId());
									List<OrdAddress> addressList = ordAdressService.findOrdAddressList(map);
									if(addressList!=null && addressList.size()>0){
										for(OrdAddress address:addressList){
											ordorder.setOrdAddress(address);
										}
									}
								}
							}
						}
					}
				}
				ordInvoices.setOrderList(ordOrderList);
			}
		}
		Map<String, Object> beans = new HashMap<String, Object>();
		beans.put("invoiceList", ordInvoiceList);
		String destFileName = writeExcelByXls(beans, ORD_INVOICE_TEMPLATE_PATH);
		writeAttachment(destFileName,"invoiceTemplateExcel"+ DateUtil.formatDate(new Date(), "yyyy MM dd"),HttpServletLocalThread.getResponse());
	}

	/**
	 * 导出地址
	 * 
	 * @param model
	 * @param ids
	 */
	@RequestMapping("/ord/invoiceAddress.do")
	public void doOutputAddress(ModelMap model, OrdInvoice ordInvoice,Date startTime, Date endTime,Date billStartTime,Date billEndTime,OrdOrder order) {
		Map<String, Object> param = init(ordInvoice,startTime,endTime, billStartTime,billEndTime,order);
		List<OrdInvoice> ordInvoiceList = ordInvoiceService.getOrdInvoiceListByParam(param);
		
		Map<String, Object> map = new HashMap<String, Object>();
		if (ordInvoiceList != null && ordInvoiceList.size() > 0) {
			for (OrdInvoice ordInvoices : ordInvoiceList) {
				map.clear();
				map.put("objectType", "ORD_INVOICE");
				map.put("objectId", ordInvoices.getOrdInvoiceId());
				List<OrdPerson> personList = ordPersonService.findOrdPersonList(map);
				if (personList != null && personList.size() > 0) {
					for(OrdPerson person:personList){
						List<OrdAddress> ordAddressList = new ArrayList<OrdAddress>();
						ordInvoices.setDeliveryAddress(person);
						map.clear();
						map.put("ordPersonId", person.getOrdPersonId());
						List<OrdAddress> addressList = ordAdressService.findOrdAddressList(map);
						if (addressList != null && addressList.size() > 0) {
							for (OrdAddress address : addressList) {
								ordAddressList.add(address);
							}
						}
						person.setAddressList(ordAddressList);
					}
				}
			}
		}
		
		Map<String, Object> beans = new HashMap<String, Object>();
		beans.put("invoiceList", ordInvoiceList);
		String destFileName = writeExcelByXls(beans, ORD_ADDRESS_TEMPLATE_PATH);
		writeAttachment(destFileName,"invoiceAddressExcel"+ DateUtil.formatDate(new Date(), "yyyy MM dd"),HttpServletLocalThread.getResponse());
	}

	/**
	 * 写excel通过模板 bean
	 * 
	 * @param beans
	 * @param template
	 * @return
	 * @throws Exception
	 */
	public static String writeExcelByXls(Map<String, Object> beans,
			String template) {
		try {
			File templateResource = ResourceUtil.getResourceFile(template);
			XLSTransformer transformer = new XLSTransformer();
			String destFileName = getTempDir() + "/excel"
					+ new Date().getTime() + ".xls";
			transformer.transformXLS(templateResource.getAbsolutePath(), beans,
					destFileName);
			return destFileName;
		} catch (Exception e) {
			LOG.error("", e);
		}
		return null;
	}

	public static String getTempDir() {
		return System.getProperty("java.io.tmpdir");
	}

	/**
	 * 已开票
	 * 
	 * @param invoices
	 * @return
	 */
	@RequestMapping("/ord/invoiceListBill.do")
	@ResponseBody
	public Object doBill(String invoices) {
		Map<String, Object> attributes = new HashMap<String, Object>();
		ResultMessage msg = ResultMessage.createResultMessage();
		String[] array = invoices.split(",");
		JSONArray successObj;
		successObj = new JSONArray();
		JSONResult result = new JSONResult();
		try {
			changeStatusList(new InvoiceComp() {
				public boolean hasChangeAble(OrdInvoice ordInvoice) {
					return InvoiceUtil.checkChangeApproveAble(ordInvoice);
				}
			}, Constant.INVOICE_STATUS.BILLED.name(), invoices);
			result.put("results", successObj);
		} catch (Exception ex) {
			result.raise(new JSONResultException(ex.getMessage()));
		}
		attributes.put("length", array.length);
		msg.setAttributes(attributes);
		return msg;
	}
	
	/**
	 * 根据状态查询发票信息并返回json
	 */
	public void getStatusOrdInvoice(HttpServletResponse response){
		try {
			// 组装查询条件
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("billedStatus", Constant.INVOICE_STATUS.BILLED.name()); //已开票
			param.put("approveStatus", Constant.INVOICE_STATUS.APPROVE.name());//已审核
			
			List<OrdInvoice> ordInvoiceList = ordInvoiceService.getStatusOrdInvoiceListByParam(param);
			
			if(ordInvoiceList!=null && ordInvoiceList.size()>0){
				String json = JSONArray.fromObject(ordInvoiceList).toString();
				this.sendAjaxResultByJson(json, response);
			}
			
//			//组装JSON数据
//			JSONArray jsonArray = new JSONArray();
//			if(null != ordInvoiceList && !ordInvoiceList.isEmpty()){
//				for(OrdInvoice invoice:ordInvoiceList){
//					JSONObject jsonObject = new JSONObject();
//					jsonObject.put("ordInvoiceId", invoice.getOrdInvoiceId());
//					jsonObject.put("status", invoice.getStatus());
//					jsonArray.add(jsonObject);
//				}
//			}
//			//返回JSON数据
//			JSONOutput.writeJSON(response, jsonArray);
			
		} catch (Exception e) {
			e.getStackTrace();
		}
	}
	
	public static boolean checkChangeInvoiceNo(OrdInvoice ordInvoice) {
		return ordInvoice == null
				|| (ordInvoice.getStatus()
						.equals(Constant.INVOICE_STATUS.CANCEL.name()))
				|| (ordInvoice.getStatus().equals(
						Constant.INVOICE_STATUS.COMPLETE.name()) || (ordInvoice
						.getStatus().equals(Constant.INVOICE_STATUS.RED.name())));
	}

	static class FindOrder implements org.apache.commons.collections.Predicate {
		private Long orderid;

		public FindOrder(Long orderid) {
			super();
			this.orderid = orderid;
		}

		@Override
		public boolean evaluate(Object arg0) {
			if (arg0 instanceof OrdOrder) {
				return ((OrdOrder) arg0).getOrderId().equals(orderid);
			}
			return false;
		}

	}
    /**
     * 开具发票(list)
     * 
     * @return
     */ 
    @ResponseBody
    @RequestMapping(value="/ord/issueInvoice.do",  produces = "application/json; charset=utf-8")
    public String issueInvoiceList(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        String operator = null;
        operator = getLoginUserId();
        
        ResultHandle issueInvoice = null;
        String params = request.getParameter("selectedInvoiceIds");
        List<String> invoiceIdsList = new ArrayList<String>();
        
        try {
            if (StringUtil.isEmptyString(params)) {
                issueInvoice = new ResultHandle();
                throw new RuntimeException("选中0个！");
            }
            
            String[] split = params.split("&");
            if (split.length > 0) {
                for (String item : split) {
                    String[] split2 = item.split("=");
                    if (split2.length == 2) {
                        invoiceIdsList.add(split2[1]);
                    }else {
                      throw new RuntimeException("参数异常， " + item);
                    }
                }
            }
            
            if (null != invoiceIdsList && invoiceIdsList.size() > 0) {
                issueInvoice = ordInvoiceService.issueInvoice(invoiceIdsList, operator);
                if (null == issueInvoice) {
                    throw new RuntimeException("开具发票失败！");
                }
            }else {
                throw new RuntimeException("解析得到的发票数量为0！");
            }
        } catch (Exception e) {
            issueInvoice = new ResultHandle();
            issueInvoice.setMsg(e.getMessage());
        }
        String result = JSONUtil.bean2Json(issueInvoice);
        return result;
    }
    
    /**
     * 重新打印发票
     * 
     * @return
     */ 
    @ResponseBody
    @RequestMapping(value="/ord/reprintInvoice.do",  produces = "application/json; charset=utf-8")
    public String reprintInvoice(HttpServletRequest request) {
        String params = request.getParameter("selectedInvoiceIds");
        List<String> invoiceIdsList = new ArrayList<String>();
        ResultHandle reprintResult = null; 

        try {
            
            if (StringUtil.isEmptyString(params)) {
                throw new RuntimeException("选中了0了");
            }
            
            String[] split = params.split("&");
            if (split.length > 0) {
                for (String item : split) {
                    String[] split2 = item.split("=");
                    if (split2.length == 2) {
                        invoiceIdsList.add(split2[1]);
                    }else {
                        throw new RuntimeException("参数异常， " + item);
                    }
                }
            }
            
            if (null != invoiceIdsList && invoiceIdsList.size() > 0) {
                reprintResult = ordInvoiceService.reprintInvoice(invoiceIdsList);
                if (null == reprintResult) {
                    throw new RuntimeException("重新打印发票失败！");
                }
            }else {
                throw new RuntimeException("解析得到的发票数量为0！");
            }
        } catch (Exception e) {
            reprintResult = new ResultHandle();
            reprintResult.setMsg(e.getMessage());
        }
        String result = JSONUtil.bean2Json(reprintResult);
        return result;
    }

    /**
     * 开具发票(single)
     * 
     * @return
     */ 
    @ResponseBody
    @RequestMapping(value="/ord/issueInvoiceSingle.do",  produces = "application/json; charset=utf-8")
    public String issueInvoiceSingle(ModelMap model, Long invoiceId) {
        String operator = null;
        operator = getLoginUserId();
        ResultHandle issueInvoice = null;
        
        List<String> invoiceIdsList = new ArrayList<String>();
        invoiceIdsList.add(invoiceId.toString());
        try {
            if (null == invoiceId || invoiceId <1) {
                throw new RuntimeException("发票号错误！");
            }
            issueInvoice = ordInvoiceService.issueInvoice(invoiceIdsList, operator);
            if (null == issueInvoice) {
                throw new RuntimeException("打印发票失败！");
            }
        } catch (Exception e) {
            issueInvoice = new ResultHandle();
            issueInvoice.setMsg(e.getMessage());
        }
        String result = JSONUtil.bean2Json(issueInvoice);
        return result;
    }

    /**
	 * 组装分页对象
	 * 
	 * @author wenzhengtao
	 * @param model
	 * @param currentPage
	 * @param count
	 * @param request
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Page buildResultPage(Integer currentPage, Integer pageSize, Integer count, HttpServletRequest request) {
		// 如果当前页是空，默认为1
		Integer currentPageTmp = currentPage == null ? 1 : currentPage;
		// 从配置文件读取分页大小
		Integer defaultPageSize = DEFAULT_PAGE_SIZE;
		Integer pageSizeTmp = pageSize == null ? defaultPageSize : pageSize;
		// 构造分页对象
		Page page = Page.page(count, pageSizeTmp, currentPageTmp);
		// 构造分页URL
		page.buildUrl(request);
		return page;
	}

	/**
	 * 查询日志
	 */
	private List<com.lvmama.log.comm.po.ComLog> getLog(String objectType,Long invoiceId){
		ComLogPams comLogPams = new ComLogPams();
		comLogPams.setObjectId(invoiceId);
		comLogPams.setObjectType("ORD_INVOICE");
		int curPage = 1;
		int pageSize = 1000;
		com.lvmama.log.comm.bo.ResultHandle<Pagination<com.lvmama.log.comm.po.ComLog>> resultHandle = null;
		try {
			resultHandle = queryLogClientService.findLog(comLogPams, curPage,pageSize);
		}catch (Exception e){
			log.error("bizLogPage "+ExceptionFormatUtil.getTrace(e));
		}
		Pagination<com.lvmama.log.comm.po.ComLog> bizLogPage = null;
		if(resultHandle != null && resultHandle.getT() != null){
			bizLogPage = resultHandle.getT();
		}
		String stastus = bizLogPage == null ? "EMPTY ... " : "TotalRows: "+bizLogPage.getTotalRows()+","+"getCurPage: "+bizLogPage.getCurPage();
		log.info("bizLogPage status:  " + stastus);
		return bizLogPage == null ? null : bizLogPage.getItemList();
	}

	/**
	 * 订单详情页面发票信息
	 * @param model
	 * @param orderId
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/showInvoiceInfo")
	public Object showInvoiceInfo(Model model, Long orderId, HttpServletRequest req, HttpServletResponse res){

		if (orderId == null) {
			LOG.error("查询发票信息orderId为空");
			return new ResultMessage(ResultMessage.ERROR, "请传入订单ID");
		}

		//财务有数据优先跳转财务
		InvoiceResponseVO<List<InvoiceVO>> responseVO = invoiceRemoteService.findInvoiceByOrderId(orderId);
		if (responseVO != null && responseVO.isSuccess()) {
			if (CollectionUtils.isNotEmpty(responseVO.getBody())) {
				return "redirect:http://super.lvmama.com/gps/#/invoice/manage/detail/" + orderId;
			}
		}

		//查询vst端发票申请
		RequestBody<Long> requestBody = new com.lvmama.order.api.base.vo.RequestBody<>();
		requestBody.setT(orderId);
		List<OrdApplyInvoicePersonAddressVo> resultList = new ArrayList<>();
		com.lvmama.order.api.base.vo.ResponseBody<List<OrdApplyInvoicePersonAddressVo>> responseBody = apiInvoiceApplyQueryService.findAppInvFullInfoByOrderId(requestBody);
		if (responseBody != null && responseBody.isSuccess()) {
			List<OrdApplyInvoicePersonAddressVo> invoiceList = responseBody.getT();
			if (CollectionUtils.isNotEmpty(invoiceList)) {
				for (OrdApplyInvoicePersonAddressVo invoiceInfo : invoiceList) {
					if (!OrdApplyInvoiceInfo.ApplyInvoiceStatus.APPLIED.getCode().equals(invoiceInfo.getStatus())) {
						resultList.add(invoiceInfo);
					} else {
						//存在已推送的发票记录则跳转至财务页面
						return "redirect:http://super.lvmama.com/gps/#/invoice/manage/detail/" + orderId;
					}
				}
			}
			model.addAttribute("invoiceList",responseBody.getT());
		}

		if (CollectionUtils.isEmpty(resultList)) {
			return "redirect:http://super.lvmama.com/gps/#/invoice/manage/detail/" + orderId;
		} else {
			return "/order/invoice/new_invoice";
		}
	}

	@RequestMapping(value = "/showUpdateInvoiceInfo")
	public Object showUpdateInvoiceInfo(Model model, Long invoiceId, HttpServletRequest req, HttpServletResponse res){

		if (invoiceId == null) {
			LOG.error("查看发票信息invoiceId为空");
			return new ResultMessage(ResultMessage.ERROR, "请传入发票ID");
		}

		//加载省份数据
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("parentId", 8);
		// 调用联想查询接口
		List<BizDistrict> provinceList = districtClientRemote
				.findDistrictList(params).getReturnContent();
		Collections.sort(provinceList, new Comparator<BizDistrict>() {
			@Override
			public int compare(BizDistrict o1, BizDistrict o2) {
				// TODO Auto-generated method stub
				if (o1.getDistrictId() > o2.getDistrictId()) {
					return 1;
				} else if (o1.getDistrictId() < o2.getDistrictId()) {
					return -1;
				}
				return 0;
			}
		});
		model.addAttribute("provinceList", provinceList);

		RequestBody<Long> requestBody = new RequestBody<>();
		requestBody.setT(invoiceId);
		com.lvmama.order.api.base.vo.ResponseBody<OrdApplyInvoicePersonAddressVo> responseBody = apiInvoiceApplyQueryService.findAppInvFullInfoById(requestBody);
		if (responseBody != null && responseBody.isSuccess()) {
			OrdOrder ordOrder = orderService.queryOrdorderByOrderId(responseBody.getT().getOrderId());
			if (ordOrder != null) {

				//自由行品类特殊处理，走传入subCategory方法
				if (ordOrder.getCategoryId() == 18L) {
					List<Long> subList = new ArrayList<>();
					subList.add(ordOrder.getSubCategoryId());
					InvoiceResponseVO<List<String>> responseVO = invoiceRemoteService.getInvoiceContentsByCategoryIds(ordOrder.getCategoryId(),subList);
					if (responseVO != null && responseBody.isSuccess()) {
						model.addAttribute("contents",responseVO.getBody());
					}
				}else {
					InvoiceResponseVO<List<String>> responseVO = invoiceRemoteService.getInvoiceContentsByCategoryId(ordOrder.getCategoryId());
					if (responseVO != null && responseBody.isSuccess()) {
						model.addAttribute("contents",responseVO.getBody());
					}
				}


			}

			model.addAttribute("invoiceInfo",responseBody.getT());

			return "/order/invoice/showUpdateInvoiceInfo";
		} else {
            LOG.error("根据id查询发票信息失败");
            return new ResultMessage(ResultMessage.ERROR, "根据id查询发票信息失败");
		}
	}

	@RequestMapping(value = "/updateInvoiceInfo")
	@ResponseBody
	public Object updateInvoiceInfo(Model model, OrdApplyInvoicePersonAddressVo invoiceVo, HttpServletRequest req, HttpServletResponse res){
		ResultMessage msg = ResultMessage.createResultMessage();
		if (invoiceVo == null) {
			LOG.error("修改发票信息invoiceId为空");
			return new ResultMessage(ResultMessage.ERROR, "请传入发票ID");
		}

		String loginUserId=this.getLoginUserId();
		//修改发票构建请求参数
		RequestBody<OrdApplyInvoicePersonAddressVo> requestBody = new RequestBody<>();
		requestBody.setT(invoiceVo);
		com.lvmama.order.api.base.vo.ResponseBody<Integer> responseBody = apiInvoiceApplyUpdateService.updateInvoiceInfo(requestBody);
		if (responseBody != null && responseBody.isSuccess() && responseBody.getT() == 1) {
			msg.setMessage("修改成功！");
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					invoiceVo.getOrderId(),
					invoiceVo.getOrderId(),
					loginUserId,
					"将编号为["+invoiceVo.getOrderId()+"]的订单，更新发票信息",
					null,
					"更新发票信息",
					null);
			return msg;
		} else {
			msg.raise("修改失败");
			return ResultMessage.ERROR;
		}
	}

	@RequestMapping(value = "/cancelInvoiceInfo")
	@ResponseBody
	public Object updateInvoiceInfo(Model model,Long invoiceId,Long orderId){
		ResultMessage msg = ResultMessage.createResultMessage();
		if (invoiceId == null || orderId == null) {
			LOG.error("修改发票状态参数为空");
			return new ResultMessage(ResultMessage.ERROR, "请传入发票ID与订单ID");
		}
		String loginUserId=this.getLoginUserId();

		//修改发票构建请求参数
		RequestBody<OrdApplyInvoicePersonAddressVo> requestBody = new RequestBody<>();
		OrdApplyInvoicePersonAddressVo invoiceVo = new OrdApplyInvoicePersonAddressVo();
		invoiceVo.setId(invoiceId);
		invoiceVo.setStatus(OrdApplyInvoiceInfo.ApplyInvoiceStatus.CANCEL.getCode());
		requestBody.setT(invoiceVo);
		com.lvmama.order.api.base.vo.ResponseBody<Integer> responseBody = apiInvoiceApplyUpdateService.updateInvoiceInfoStatusById(requestBody);
		if (responseBody != null && responseBody.isSuccess() && responseBody.getT() == 1) {
			msg.setMessage("取消发票申请成功！");
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					orderId,
					orderId,
					loginUserId,
					"将编号为["+orderId+"]的订单，取消发票申请",
					null,
					"取消发票申请",
					null);
			return msg;
		} else {
			msg.raise("取消发票申请失败");
			return ResultMessage.ERROR;
		}
	}

	/**
     * 触发job申请发票
     * 
     * @return
     */
    @RequestMapping(value = "/ord/triggerApplyJob.do")
    public void triggerApplyJob(HttpServletRequest request, HttpServletResponse response) {
        log.info("TriggerApplyInvoiceJob by manual");
        ordApplyInvoiceInfoService.autoApplyInvoice();
    }
	
}
