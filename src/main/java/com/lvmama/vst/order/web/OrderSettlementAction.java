package com.lvmama.vst.order.web;

import com.lvmama.comm.pet.po.email.EmailContent;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.finance.comm.finance.po.SetSettlementItem;
import com.lvmama.finance.comm.vst.service.SettlementService;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.dist.service.DistributorClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.client.precontrol.service.ResPrecontrolBindGoodsClientService;
import com.lvmama.vst.back.client.precontrol.service.ResPrecontrolPolicyClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.po.ResPrecontrolBindGoods;
import com.lvmama.vst.back.control.po.ResPrecontrolPolicy;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PRICE_RATE_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_SETTLEMENT_STATUS;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.supp.po.SuppSettleRule;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.utils.*;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.web.HttpServletLocalThread;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.order.*;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.vo.ordSettlementPriceRecordListVo;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;
import com.lvmama.vst.pet.adapter.SettlementPriceChangeServiceAdapter;
import com.lvmama.vst.pet.adapter.SettlementServiceAdapter;
import com.lvmama.vst.pet.adapter.VstEmailServiceAdapter;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 订单结算修改功能
 * 
 * @author 张伟
 *
 */
@Controller
@RequestMapping("/order/orderSettlementChange")
public class OrderSettlementAction extends BaseActionSupport {
	// 日志记录器
	//private static final Logger LOG = LoggerFactory.getLogger(OrderSettlementAction.class);
	protected static final Log LOG = LogFactory.getLog(OrderSettlementAction.class);
	/**
	 * 导出excel对应模板文件地址
	 */
	public static final String SETTLEMENT_HISTORY_TEMPLATE_PATH = "/WEB-INF/resources/orderSettlement/orderSettlementHistoryTemplate.xls";
	
	
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
	private IOrdOrderHotelTimeRateService ordOrderHotelTimeRateService;
	
	@Autowired
	private IOrdOrderItemService ordOrderItemService;
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
	private IOrdOrderService iOrdOrderService;
	
	@Autowired
	private IOrdOrderPackService ordOrderPackService;
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private IOrdMulPriceRateService ordMulPriceRateService;
	
	
	@Autowired
	private IOrdSettlementPriceRecordService ordSettlementPriceRecordService ;
	
	@Autowired
	private VstEmailServiceAdapter vstEmailService;
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	@Autowired
	private PermUserServiceAdapter permUserServiceAdapter;
	
	@Autowired
	private SettlementServiceAdapter settlementServiceAdapter;
	
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	
	@Autowired
	private SettlementPriceChangeServiceAdapter settlementPriceChangeService;

    @Autowired
    private SuppGoodsClientService suppGoodsClientService;

    @Autowired
    private ResPrecontrolBindGoodsClientService resPrecontrolBindGoodsClientService;

    @Autowired
    private ResPrecontrolPolicyClientService resPrecontrolPolicyClientService;
    
    @Autowired
	private ResPreControlService resControlBudgetRemote;

	@Autowired
	private OrderSettlementService orderSettlementService;

	@Autowired
	private IOrdOrderItemExtendService ordOrderItemExtendService;
	
	//结算状态改造 从支付获取
	@Autowired
	private SettlementService settlementService;
	
	/**
	 * 
	 * 
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/showOrderSettlementList")
	public String showOrderSettlementList(Model model,HttpServletRequest request, Integer page,Integer pageSize,OrderMonitorCnd monitorCnd) throws BusinessException{
		// 子订单价格状态字典
		setBaseModelProp(model);
		// 查询条件回显
		model.addAttribute("monitorCnd", monitorCnd);
		return "/order/orderSettlementManage/findOrderSettlementList";
	}
	

	/**
	 *订单结算记录查询
	 * 
	 * @param model
	 * @param page
	 * @param pageSize
	 * @param monitorCnd
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/findOrderSettlementList")
	public String findOrderSettlementList(Model model, Integer page,Integer pageSize,OrderMonitorCnd monitorCnd, HttpServletRequest request) throws BusinessException {
		int pagenum = page == null ? 1 : page;
		ComplexQuerySQLCondition condition = buildQueryConditionForMonitor(page, DEFAULT_PAGE_SIZE, monitorCnd);
		// 根据条件获取订单集合
		List<OrdOrder> orderList = complexQueryService.checkOnlyOrderListFromReadDB(condition);
		Long totalCount = 0L;
		if (CollectionUtils.isNotEmpty(orderList)) {
			if (page==null&&orderList.size() < DEFAULT_PAGE_SIZE) {
				totalCount = (long) orderList.size();
			} else {
				totalCount = complexQueryService.checkOrderCountFromReadDB(condition);
			}
		}
		List<Long> orderIdList =new ArrayList<Long>();
		for(OrdOrder order:orderList){
			orderIdList.add(order.getOrderId());
		}
		Page<OrderMonitorRst> pageParam = Page.page(totalCount,DEFAULT_PAGE_SIZE, pagenum);
		pageParam.buildUrl(request);
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		if (CollectionUtils.isNotEmpty(orderIdList)) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("_orderby","T.ORDER_ITEM_ID desc");
			params.put("orderIdList",orderIdList);
			if(StringUtils.isNotBlank(monitorCnd.getPriceConfirmStatus())){
				params.put("priceConfirmStatus", monitorCnd.getPriceConfirmStatus());//子订单价格确认状态
				if(StringUtils.equalsIgnoreCase(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.getCode(),monitorCnd.getPriceConfirmStatus())){
					params.put("priceConfirmStatusIsNull",true);
				}
			}
			if(null!=monitorCnd.getOrderItemId()) {
				params.put("orderItemId",monitorCnd.getOrderItemId());
			}
			params.put("suppGoodsId", monitorCnd.getSuppGoodsId());//商品ID
			params.put("supplierId", monitorCnd.getSupplierId());

			List<OrdOrderItem> orderItemList=null;
			try {
				orderItemList = this.orderUpdateService.queryOrderItemByParamsForOrdSettlement(params);
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
			}

			//只显示未支付的订单
			/*List<OrdOrderItem> removeList = new ArrayList<OrdOrderItem>();
			for (int i = 0; i < orderItemList.size(); i++) {
				if (OrderEnum.PAYMENT_STATUS.UNPAY.getCode().equals(orderItemList.get(i).getPaymentStatus())) {
					removeList.add(orderItemList.get(i));
				}
			}*/

            if (CollectionUtils.isNotEmpty(orderItemList)) {
                resultList = buildQueryResult(orderItemList, request);
			}
		}

		// 存储分页结果
		pageParam.setItems(resultList);
		model.addAttribute("pageParam", pageParam);
		setBaseModelProp(model);
		model.addAttribute("resultList", resultList);
		// 查询条件回显
		model.addAttribute("monitorCnd", monitorCnd);
		
		return "/order/orderSettlementManage/findOrderSettlementList";
	}

	/**
	 * 设置共用基础属性
	 * @param model
	 */
	private void setBaseModelProp(Model model){
		// 子订单价格状态字典
		Map<String, String> priceConfirmMap = new LinkedHashMap<String, String>();
		priceConfirmMap.put("", "请选择");
		for (OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS item : OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.values()) {
			priceConfirmMap.put(item.getCode(), item.getCnName());
		}
		model.addAttribute("priceConfirmMap", priceConfirmMap);
	}

	/**
	 * 
	 * 
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/showOrderSettlementApproveList")
	public String showOrderSettlementApproveList(Model model,HttpServletRequest request) throws BusinessException{
		
		model.addAttribute("monitorCnd",new OrderMonitorCnd());
		
		
		
		return "/order/orderSettlementManage/findOrderSettlementApproveList";
	}

	/**
	 *订单结算审核记录查询
	 * 
	 * @param model
	 * @param page
	 * @param pageSize
	 * @param monitorCnd
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/findOrderSettlementApproveList")
	public String findOrderSettlementApproveList(Model model, Integer page,
			Integer pageSize, OrderMonitorCnd monitorCnd,
			HttpServletRequest request) throws BusinessException {

		List<OrdSettlementPriceRecord> recordList = null;
		// 根据页面展示特色组装其想要的结果
		List<OrdSettlementPriceRecordVO> resultList = new ArrayList<OrdSettlementPriceRecordVO>();
		Long orderId = monitorCnd.getOrderId();
		Long suppGoddsId = monitorCnd.getSuppGoodsId();

		/*
		 * List<OrdOrderItem> ordItemList=new ArrayList<OrdOrderItem>(); if
		 * (orderId!=null) { HashMap<String, Object> itemParams = new
		 * HashMap<String, Object>(); itemParams.put("orderId",orderId );
		 * itemParams.put("suppGoodsId",suppGoddsId );
		 * ordItemList=orderUpdateService.queryOrderItemByParams(itemParams);
		 * 
		 * }
		 */

		// if (CollectionUtils.isNotEmpty(ordItemList)) {

		HashMap<String, Object> params = new HashMap<String, Object>();
		// if (CollectionUtils.isNotEmpty(ordItemList)) {
		//
		// List<Long> itemIdList=new ArrayList<Long>();
		// for (int i = 0; i < ordItemList.size(); i++) {
		// OrdOrderItem orderItem=ordItemList.get(i);
		// itemIdList.add(orderItem.getOrderItemId());
		// }
		// params.put("orderItemIdArray",itemIdList );
		// }
		params.put("orderId", orderId);
		params.put("suppGoodsId", suppGoddsId);
		params.put("status", OrdAmountChange.APPROVESTATUS.TOAPPROVE.name());
		params.put("isApprove", "N");
		int count = this.ordSettlementPriceRecordService
				.findOrdSettlementPriceRecordCounts(params);

		int pagenum = page == null ? 1 : page;
		Page<OrdSettlementPriceRecordVO> pageParam = Page.page(count, 10,
				pagenum);
		pageParam.buildUrl(request);
		params.put("_start", pageParam.getStartRows());
		params.put("_end", pageParam.getEndRows());
		params.put("_orderby", "ORD_SETTLEMENT_PRICE_RECORD.CREATE_TIME desc");

		recordList = ordSettlementPriceRecordService
				.findOrdSettlementPriceRecordList(params);

		// 存储分页结果
		pageParam.setItems(resultList);
		model.addAttribute("pageParam", pageParam);

		// }

		if (null != recordList && recordList.size() > 0) {
			resultList = buildQueryRecordResult(recordList, request);
		}

		if (LOG.isDebugEnabled()) {
			if (null != resultList && resultList.size() > 0) {
				LOG.debug("resultList=="
						+ ToStringBuilder.reflectionToString(resultList.get(0),
								ToStringStyle.MULTI_LINE_STYLE));
			}
		}

		model.addAttribute("resultList", resultList);

		// 查询条件回显
		model.addAttribute("monitorCnd", monitorCnd);

		return "/order/orderSettlementManage/findOrderSettlementApproveList";
	}

	
	/**
	 * 保存
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value="/approveOrdSettlementChange")
	@ResponseBody
	public Object approveOrdSettlementChange(Model model,HttpServletRequest request,OrdSettlementPriceRecordVO ordSettlementPriceRecordVO) throws Exception{
		
		OrdSettlementPriceRecord oldOrdSettlementPriceRecord=this.ordSettlementPriceRecordService.selectByPrimaryKey(ordSettlementPriceRecordVO.getRecordId());
		Long orderId=ordSettlementPriceRecordVO.getOrderId();
		Long orderItemId=oldOrdSettlementPriceRecord.getOrderItemId();
//		OrdOrder order=this.orderLocalService.queryOrdorderByOrderId(orderId);
		
		if(!OrdAmountChange.APPROVESTATUS.TOAPPROVE.name().equalsIgnoreCase(oldOrdSettlementPriceRecord.getStatus())){
			return new ResultMessage(ResultMessage.SUCCESS," 审批已经完成");
		}
		
		OrdSettlementPriceRecord newOrdSettlementPriceRecord=new OrdSettlementPriceRecord();
		
		BeanUtils.copyProperties(oldOrdSettlementPriceRecord, newOrdSettlementPriceRecord);
		/*
		if(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name().equalsIgnoreCase(ordSettlementPriceRecordVO.getStatus())){
			
			
			//校验订单状态
			ResultMessage resultMessage=this.validateOrderStatus(order);
			if (!resultMessage.isSuccess()) {
				return resultMessage;
			}
		}
*/
//		oldOrdSettlementPriceRecord.setStatus(ordSettlementPriceRecordVO.getStatus());
//		oldOrdSettlementPriceRecord.setOperator(this.getLoginUserId());
//		oldOrdSettlementPriceRecord.setApproveRemark(ordSettlementPriceRecordVO.getApproveRemark());
//		oldOrdSettlementPriceRecord.setUpdateTime(new Date());
//		oldOrdSettlementPriceRecord.setOperatorApprove(this.getLoginUserId());
		oldOrdSettlementPriceRecord.setIsApprove("Y");
		
		
		newOrdSettlementPriceRecord.setRecordId(null);
		newOrdSettlementPriceRecord.setStatus(ordSettlementPriceRecordVO.getStatus());
		newOrdSettlementPriceRecord.setOperator(this.getLoginUserId());
		newOrdSettlementPriceRecord.setApproveRemark(ordSettlementPriceRecordVO.getApproveRemark());
		newOrdSettlementPriceRecord.setUpdateTime(new Date());
		newOrdSettlementPriceRecord.setOperatorApprove(this.getLoginUserId());
		newOrdSettlementPriceRecord.setIsApprove("Y");
		//审核通过
		if(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name().equalsIgnoreCase(newOrdSettlementPriceRecord.getStatus())){
			
			OrdOrderItem orderItem=this.orderUpdateService.getOrderItem(orderItemId);
			
			ResultMessage resultMessage=this.validateOrderSettlementStatus(orderItem);
			//是否已结算
			if (!resultMessage.isSuccess()) {
				return resultMessage;
			}                      
			int n=this.ordSettlementPriceRecordService.saveAfterApprove(oldOrdSettlementPriceRecord,newOrdSettlementPriceRecord,orderId);
			if (n==1) {
				
				sendOrdSettlementPriceChangeMsg(newOrdSettlementPriceRecord.getOrderItemId());
			}

		}else {
			this.ordSettlementPriceRecordService.saveAfterApprove(oldOrdSettlementPriceRecord,newOrdSettlementPriceRecord,orderId);
//			this.ordSettlementPriceRecordService.insert(newOrdSettlementPriceRecord);
		}
		
		StringBuffer content=new StringBuffer();

		String priceTypeName=OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(newOrdSettlementPriceRecord.getPriceType());
		if ("PRICE".equals(priceTypeName.trim())) {
			priceTypeName="价格";
		}
		
		content.append("修改子订单结算单价，修改值：").append("原价格类型").append(priceTypeName).append("的实际结算单价：").append(newOrdSettlementPriceRecord.getOldActualSettlementPrice()/100.0);
		content.append("修改该价格类型结算单价：").append(newOrdSettlementPriceRecord.getNewActualSettlementPrice()/100.0).append("。原价格类型的实际结算总价：").append(newOrdSettlementPriceRecord.getOldTotalSettlementPrice()/100.0);
		content.append("修改后该价格类型的结算总价:").append(newOrdSettlementPriceRecord.getNewTotalSettlementPrice()/100.0);
		
		
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
				orderId, 
				orderItemId, 
				getLoginUserId(), 
				"将编号为["+orderItemId+"]的子订单，"+content.toString(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName()+"：子订单结算单价修改操作"+OrdAmountChange.APPROVESTATUS.getCnName(newOrdSettlementPriceRecord.getStatus()),"");
		
		
		
		//发消息
		/*String assignor=getLoginUserId();
		
		int n=ordSettlementPriceRecordService.saveReservationForSettlementAmountChange(newOrdSettlementPriceRecord, assignor, content.toString());
		
		if(n<=0){
			return new ResultMessage(ResultMessage.SUCCESS,"审批操作完成[预定通知创建失败，找不到可以接单的人]");
		}
		*/
		
		
		return new ResultMessage(ResultMessage.SUCCESS," 审批操作完成");
		
	}


	private void sendOrdSettlementPriceChangeMsg(Long orderItemId) {
		/*List<OrdOrderItem> ordItemList=order.getOrderItemList();
		List<Long> itemIdList=new ArrayList<Long>();
		for (int i = 0; i < ordItemList.size(); i++) {
			OrdOrderItem orderItem=ordItemList.get(i);
			itemIdList.add(orderItem.getOrderItemId());
		}*/
		
//		String addition=new StringBuffer(StringUtils.join(itemIdList, ",")).append("|").append(this.getLoginUserId()).toString();
		String addition=new StringBuffer(orderItemId+"").append("|").append(this.getLoginUserId()).toString();
		orderLocalService.sendOrdSettlementPriceChangeMsg(orderItemId,addition);
	}

	private void sendOrdItemPriceConfirmChangeMsg(Long orderItemId){
		String addition=new StringBuffer(orderItemId+"").append("|").append(this.getLoginUserId()).toString();
		orderLocalService.sendOrdSettlementPriceChangeMsg(orderItemId,addition);

	}

	
	/**
	 * 
	 * 
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/showOrderSettlementHistoryList")
	public String showOrderSettlementHistoryList(Model model,HttpServletRequest request) throws BusinessException{
		
		model.addAttribute("monitorCnd",new OrderMonitorCnd());
		
		model.addAttribute("pageParam", new Page<OrdSettlementPriceRecordVO>());
		
		return "/order/orderSettlementManage/findOrderSettlementHistoryList";
	}

	
	/**
	 *订单结算审核记录历史查询
	 * 
	 * @param model
	 * @param page
	 * @param pageSize
	 * @param monitorCnd
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/findOrderSettlementHistoryList")
	public String findOrderSettlementHistoryList(Model model, Integer page,Integer pageSize,OrderMonitorCnd monitorCnd, HttpServletRequest request) throws BusinessException {
		
		
		// 根据页面展示特色组装其想要的结果
		List<OrdSettlementPriceRecordVO> resultList = null;
		
				
		Page<OrdSettlementPriceRecordVO> pageParam =null;
		
		resultList=this.getResultList(monitorCnd, page,pageParam,request);
		
//		List<OrdOrder> orderList = getOrderListCondition(monitorCnd);
				
		/*if (!CollectionUtils.isEmpty(orderList)) {
			
			
			
			
		}else{
			
			int pagenum = page == null ? 1 : page;
			pageParam = Page.page(0, 10, pagenum);
			pageParam.buildUrl(request);
		
			// 存储分页结果
			pageParam.setItems(resultList);
			
			model.addAttribute("pageParam", pageParam);
			
		}*/
		
		
		
		
		model.addAttribute("resultList", resultList);
		
		

		// 查询条件回显
		model.addAttribute("monitorCnd", monitorCnd);
		
		

		return "/order/orderSettlementManage/findOrderSettlementHistoryList";
	}


	private List<OrdSettlementPriceRecordVO> getResultList(
			OrderMonitorCnd monitorCnd, Integer page,
			Page<OrdSettlementPriceRecordVO> pageParam,
			HttpServletRequest request) {

		// 根据页面展示特色组装其想要的结果
		List<OrdSettlementPriceRecordVO> resultList = new ArrayList<OrdSettlementPriceRecordVO>();

		// if (!CollectionUtils.isEmpty(orderList)) {

		/*
		 * OrdOrder order=orderList.get(0);
		 * 
		 * List<OrdOrderItem> ordItemList=order.getOrderItemList();
		 * 
		 * List<Long> itemIdList=new ArrayList<Long>(); for (int i = 0; i <
		 * ordItemList.size(); i++) { OrdOrderItem orderItem=ordItemList.get(i);
		 * itemIdList.add(orderItem.getOrderItemId()); }
		 */

		HashMap<String, Object> params = new HashMap<String, Object>();
		// params.put("orderItemIdArray",itemIdList );
		params.put("orderId", monitorCnd.getOrderId());
		params.put("suppGoodsId", monitorCnd.getSuppGoodsId());
		params.put("visitTimeBegin", monitorCnd.getVisitTimeBegin());
		params.put("visitTimeEnd", monitorCnd.getVisitTimeEnd());
		int count = this.ordSettlementPriceRecordService
				.findOrdSettlementPriceRecordCounts(params);

		int pagenum = page == null ? 1 : page;
		pageParam = Page.page(count, 10, pagenum);
		pageParam.buildUrl(request);

		params.put("_start", pageParam.getStartRows());
		params.put("_end", pageParam.getEndRows());
		params.put("_orderby", "ORD_SETTLEMENT_PRICE_RECORD.CREATE_TIME desc,ORD_SETTLEMENT_PRICE_RECORD.STATUS asc,ORD_SETTLEMENT_PRICE_RECORD.UPDATE_TIME desc");

		List<OrdSettlementPriceRecord> recordList = ordSettlementPriceRecordService
				.findOrdSettlementPriceRecordList(params);

		if (null != recordList && recordList.size() > 0) {
			resultList = buildQueryRecordResult(recordList, request);
		}

		if (LOG.isDebugEnabled()) {
			if (null != resultList && resultList.size() > 0) {
				LOG.debug("resultList=="
						+ ToStringBuilder.reflectionToString(resultList.get(0),
								ToStringStyle.MULTI_LINE_STYLE));
			}
		}
		/*
		 * 
		 * 
		 * // params.put("_orderby",
		 * "ORD_SETTLEMENT_PRICE_RECORD.CREATE_TIME,ORD_SETTLEMENT_PRICE_RECORD.UPDATE_TIME desc"
		 * ); pageParam.buildUrl(request); // 存储分页结果
		 * pageParam.setItems(resultList); model.addAttribute("pageParam",
		 * pageParam);
		 */
		// 组装分页结果
		@SuppressWarnings("rawtypes")
		Page resultPage = buildResultPage(resultList, page, null, Long.valueOf(count), request);
		HttpServletLocalThread.getModel().addAttribute("pageParam", resultPage);

		// }

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
	
	private List<OrdOrder> getOrderListCondition(OrderMonitorCnd monitorCnd) {
		// 根据页面条件组装综合查询接口条件
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		
		//组装订单标志类条件
		condition.getOrderFlagParam().setOrderTableFlag(true);
		condition.getOrderFlagParam().setOrderItemTableFlag(true);
				
		condition.getOrderIndentityParam().setOrderId(monitorCnd.getOrderId());
		
		condition.getOrderContentParam().setSuppGoodstId(monitorCnd.getSuppGoodsId());
		
		condition.getOrderTimeRangeParam().setVisitTimeBegin(monitorCnd.getVisitTimeBegin());
		condition.getOrderTimeRangeParam().setVisitTimeEnd(monitorCnd.getVisitTimeEnd());

		
		
		// 根据条件获取订单集合
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		return orderList;
	}

	
	/**
	 * 
	 * 修改非买断结算单价
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/showOrderSettlementChange")
	public String showOrderSettlementChange(Model model,Long orderItemId,Long orderId,HttpServletRequest request) throws BusinessException{
		
		Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
		paramsMulPriceRate.put("orderItemId", orderItemId); 
		Long actualBuyoutTotalPrice = 0L;
		String[] priceTypeArray = new String[] {ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_34.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.getCode() ,
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.getCode(),
//				ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode() ,
//				ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_GAP.getCode()};

		paramsMulPriceRate.put("priceTypeArray",priceTypeArray); 
		                                                    
		List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
		model.addAttribute("ordMulPriceRateList",ordMulPriceRateList);
		model.addAttribute("ordMulPriceRateListCount",ordMulPriceRateList.size());

		Map<String,String> map = new TreeMap<String, String>();
		if(ordMulPriceRateList != null && ordMulPriceRateList.size() >0){
			for (long i = 0; i < ordMulPriceRateList.size(); i++) {
				map.put(i+"", ordMulPriceRateList.get((int) i).getZhpriceType());
			}
		}else{
			map.put(0+"", "");
		}
		model.addAttribute("ordMulPriceRateMap",map);
		//价格类型
		model.addAttribute("orderPriceRateTypeList", OrderEnum.ORDER_PRICE_RATE_TYPE.values());
		//修改原因
		model.addAttribute("orderAmountChangeTypeList", OrderEnum.ORDER_SETTLEMENT_PRICE_REASON.values());
		
		
		
		OrdOrderItem orderItem=this.orderUpdateService.getOrderItem(orderItemId);
		if("Y".equals(orderItem.getBuyoutFlag())){
			Long buyoutTotalPrice = orderItem.getBuyoutTotalPrice();
			buyoutTotalPrice = buyoutTotalPrice==null?0L:buyoutTotalPrice;
			orderItem.setTotalSettlementPrice(orderItem.getTotalSettlementPrice() - buyoutTotalPrice);
			
			Long buyoutQuantity = orderItem.getBuyoutQuantity();
			buyoutQuantity = buyoutQuantity==null?0L:buyoutQuantity;
			Long notBuyoutQuantity = orderItem.getQuantity()-buyoutQuantity;
			notBuyoutQuantity = notBuyoutQuantity==0L?1L:notBuyoutQuantity;
			orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/(notBuyoutQuantity));
			
		}
		model.addAttribute("orderItem",orderItem);
		return "/order/orderSettlementManage/showUpdateSettlement";
		
	}
	
	
	

	/**
	 * 
	 * 修改非买断结算总价
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/showOrderSettlementTotalChange")
	public String showOrderSettlementTotalChange(Model model,Long orderItemId,Long orderId,HttpServletRequest request) throws BusinessException{
		//修改原因
		model.addAttribute("orderAmountChangeTypeList", OrderEnum.ORDER_SETTLEMENT_PRICE_REASON.values());
		
		OrdOrderItem orderItem=this.orderUpdateService.getOrderItem(orderItemId);
		if("Y".equals(orderItem.getBuyoutFlag())){
			Long buyoutTotalPrice = orderItem.getBuyoutTotalPrice();
			buyoutTotalPrice = buyoutTotalPrice==null?0L:buyoutTotalPrice;
			orderItem.setTotalSettlementPrice(orderItem.getTotalSettlementPrice() - buyoutTotalPrice);
			Long buyoutQuantity = orderItem.getBuyoutQuantity();
			buyoutQuantity = buyoutQuantity==null?0L:buyoutQuantity;
			Long notBuyoutQuantity = orderItem.getQuantity()-buyoutQuantity;
			notBuyoutQuantity = notBuyoutQuantity==0L?1L:notBuyoutQuantity;
			orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/(notBuyoutQuantity));
			
		}
		model.addAttribute("orderItem",orderItem);
		
		Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
		paramsMulPriceRate.put("orderItemId", orderItemId); 

		String[] priceTypeArray = new String[] {ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_34.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.getCode() ,
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.getCode(),
				//非买断结算总价
				//ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode() ,
				//ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.getCode(),
				ORDER_PRICE_RATE_TYPE.SETTLEMENT_GAP.getCode()};

		paramsMulPriceRate.put("priceTypeArray",priceTypeArray); 
		                                                    
		List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
		model.addAttribute("ordMulPriceRateListCount",ordMulPriceRateList.size());
		
		return "/order/orderSettlementManage/showUpdateTotalSettlement";
		
	}

	/**
	 *
	 * 外币修改结算单价弹框
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/showOrderCurrencySettlementChange")
	public String showOrderCurrencySettlementChange(Model model,Long orderItemId,Long orderId,HttpServletRequest request) throws BusinessException{

		OrdOrderItemExtend ordOrderItemExtend = ordOrderItemExtendService.selectByPrimaryKey(orderItemId);
        model.addAttribute("orderId",orderId);
		model.addAttribute("ordOrderItemExtend",ordOrderItemExtend);
		//修改原因
		model.addAttribute("orderAmountChangeTypeList", OrderEnum.ORDER_SETTLEMENT_PRICE_REASON.values());
		return "/order/orderSettlementManage/showCurrencySettlementChange";

	}

	/**
	 *
	 * 修改非买断结算总价
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/showOrderCurrencySettlementTotalChange")
	public String showOrderCurrencySettlementTotalChange(Model model,Long orderItemId,Long orderId,HttpServletRequest request) throws BusinessException{
		OrdOrderItemExtend ordOrderItemExtend = ordOrderItemExtendService.selectByPrimaryKey(orderItemId);
        model.addAttribute("orderId",orderId);
		model.addAttribute("ordOrderItemExtend",ordOrderItemExtend);
		//修改原因
		model.addAttribute("orderAmountChangeTypeList", OrderEnum.ORDER_SETTLEMENT_PRICE_REASON.values());

		return "/order/orderSettlementManage/showCurrencySettlementTotalChange";

	}

    /**
     *
     * 查看该商品是否有买断价格
     * 1.通过商品上的买断标志位判断是否有买断价格
     * 2.再通过商品和预控买断的关联表，查询该订单的游玩时间在不在绑定的预控时间范围内
     * @param model
     * @return
     * @throws BusinessException
     */
    @RequestMapping("/checkUpdateBudgetPrice")
    @ResponseBody
    public ResultMessage checkUpdateBudgetPrice(Model model,Long orderItemId,Long orderId,HttpServletRequest request) throws BusinessException{
        OrdOrderItem ordOrderItem = orderUpdateService.getOrderItem(orderItemId);
        ResultHandleT<SuppGoods> resultHandleT = suppGoodsClientService.findSuppGoodsById(ordOrderItem.getSuppGoodsId());
        //判断商品是买断
        if(null != resultHandleT.getReturnContent()
                && "Y".equalsIgnoreCase(ordOrderItem.getBuyoutFlag())){

            ResultHandleT<List<ResPrecontrolBindGoods>> resultHandleTPrecontrol = resPrecontrolBindGoodsClientService.findResPrecontrolBindGoods(ordOrderItem.getSuppGoodsId());
            if(CollectionUtils.isNotEmpty(resultHandleTPrecontrol.getReturnContent())){
                //目前一个商品只能绑定一个预控方案
                ResPrecontrolBindGoods resPrecontrolBindGoods =  resultHandleTPrecontrol.getReturnContent().get(0);
                ResPrecontrolPolicy resPrecontrolPolicy = resPrecontrolPolicyClientService.findResPrecontrolPolicyById(resPrecontrolBindGoods.getPrecontrolPolicyId());
                //游玩时间在预控时间范围内
                if(null != resPrecontrolPolicy && ordOrderItem.getVisitTime().compareTo(resPrecontrolPolicy.getTradeEffectDate()) > -1
                        && ordOrderItem.getVisitTime().compareTo(resPrecontrolPolicy.getTradeExpiryDate())!=1 ){
                    return ResultMessage.FIND_BUDGET_SUCCESS_RESULT;
                }
            }
        }
        return ResultMessage.FIND_BUDGET_FAIL_RESULT;
    }

    /**
     *
     * 修改买断结算价（总价或单价）
     * @param model
     * @return
     * @throws BusinessException
     */
    @RequestMapping("/showOrderSettlementBudgetChange")
    public String showOrderSettlementBudgetChange(Model model,Long orderItemId,Long orderId,HttpServletRequest request) throws BusinessException{
    	
    	String singlePricePage = "/order/orderSettlementManage/showUpdateBudgetSettlement";
    	String multiPricePage = "/order/orderSettlementManage/showUpdateMultiBudgetSettlement";
        //修改原因
        model.addAttribute("orderAmountChangeTypeList", OrderEnum.ORDER_SETTLEMENT_PRICE_REASON.values());

        OrdOrderItem orderItem=this.orderUpdateService.getOrderItem(orderItemId);
        model.addAttribute("orderItem",orderItem);

        Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
        paramsMulPriceRate.put("orderItemId", orderItemId);

        String[] priceTypeArray = new String[] {
        		ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.getCode(),
                ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_34.getCode(),
                ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.getCode(),
                ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.getCode() ,
                ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.getCode(),
                ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode() ,
                ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode(),
                ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.getCode(),
                ORDER_PRICE_RATE_TYPE.SETTLEMENT_GAP.getCode()};

        paramsMulPriceRate.put("priceTypeArray",priceTypeArray);

        List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
        model.addAttribute("ordMulPriceRateListCount",ordMulPriceRateList.size());
        String pagePath = "";
        if(ordMulPriceRateList!=null && ordMulPriceRateList.size()>0){
        	paramsMulPriceRate.clear();
        	paramsMulPriceRate.put("orderItemId", orderItemId);
        	String[] priceTypes = new String[] {
                    ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode() ,
                    ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode(),
        	};
        	paramsMulPriceRate.put("priceTypeArray",priceTypes);
        	//买断结算价
        	ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
        	model.addAttribute("priceList", ordMulPriceRateList);
        	 
        	pagePath = multiPricePage;
        }else{
        	pagePath = singlePricePage;
        }
        
        
        return pagePath;

    }
	
	/**
	 * (批量生成结算子项页面)
	 * 
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/showOrderSettlementListForBatch")
	public String showOrderSettlementListForBatch(Model model,HttpServletRequest request) throws BusinessException{
		model.addAttribute("monitorCnd",new OrderMonitorCnd());
		return "/order/orderSettlementManage/findOrderSettlementListForBatch";
	}
	
	/**
	 * 订单结算记录查询(批量生成结算子项页面)
	 * 
	 * @param model
	 * @param page
	 * @param pageSize
	 * @param monitorCnd
	 * @param request
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/findOrderSettlementListForBatch")
	public String findOrderSettlementListForBatch(Model model, Integer page,Integer pageSize,OrderMonitorCnd monitorCnd, HttpServletRequest request) throws BusinessException {
		
		// 根据页面展示特色组装其想要的结果
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		
		// createTimeBegin createTimeEnd paymentTimeBegin paymentTimeEnd
		Map<String, Object> params = new HashMap<String, Object>();
		
		int  count = 0;
		if(monitorCnd.getPaymentTimeBegin()!=null && monitorCnd.getPaymentTimeEnd()!=null){
			params.put("settlementStatusNotEQ", OrderEnum.ORDER_SETTLEMENT_STATUS.SETTLEMENTED.getCode());
			params.put("paymentStatus", OrderEnum.ORDER_VIEW_STATUS.PAYED.getCode());
			params.put("resourceStatus", OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
			params.put("infoStatus", OrderEnum.INFO_STATUS.INFOPASS.getCode());
			
			params.put("paymentTimeBegin", monitorCnd.getPaymentTimeBegin());
			params.put("paymentTimeEnd", monitorCnd.getPaymentTimeEnd());
			count = this.orderUpdateService.getSettlementOrderItemTotalCount(params);
		}
		
		int pagenum = page == null ? 1 : page;
		Page<OrderMonitorRst> pageParam = Page.page(count, 100, pagenum);
		
		if(count>0){
			pageParam.buildUrl(request);
			params.put("_start", pageParam.getStartRows());
			params.put("_end", pageParam.getEndRows());
			params.put("_orderby","ORD_ORDER_ITEM.ORDER_ITEM_ID desc");
			
			List<OrdOrderItem> orderItemList=this.orderUpdateService.querySettlementOrderItemByParams(params);
			
			if(null != orderItemList && orderItemList.size()>0){
				resultList = buildQueryResult(orderItemList,request);
			}
			
			if(LOG.isDebugEnabled()){
				if(null != resultList && resultList.size()>0){
					LOG.debug("resultList=="+ToStringBuilder.reflectionToString(resultList.get(0), ToStringStyle.MULTI_LINE_STYLE));
				}
			}
		}
		
		model.addAttribute("resultList", resultList);
		
		// 存储分页结果
		pageParam.setItems(resultList);
		model.addAttribute("pageParam", pageParam);

		// 查询条件回显
		model.addAttribute("monitorCnd", monitorCnd);
		
		return "/order/orderSettlementManage/findOrderSettlementListForBatch";
	}
	
	/**
	 * 批量生成结算子项
	 * @return
	 */
	@RequestMapping(value="/batchManualSettlmente", method = {RequestMethod.POST})
	@ResponseBody
	public Object batchManualSettlmente(Model model,HttpServletRequest request,String orderItemJson ){
		// System.out.println("订单项："+orderItemJson);
		if(StringUtil.isNotEmptyString(orderItemJson)){
			Map<Long,OrdOrder> tempOrderMap = new HashMap<Long,OrdOrder>();
			try {
				List<OrdOrderItem> ordOrderItemList = toOrderItemList(orderItemJson);
				// System.out.println(ordOrderItemList.size());
				for(OrdOrderItem ordOrderItem : ordOrderItemList){
					OrdOrder orderObj = null;
					if(tempOrderMap.get(ordOrderItem.getOrderId())!=null){
						orderObj = tempOrderMap.get(ordOrderItem.getOrderId());
					}else{
						orderObj = this.orderUpdateService.queryOrdOrderByOrderId(ordOrderItem.getOrderId());
						tempOrderMap.put(ordOrderItem.getOrderId(), orderObj);
					}
					
					// 预付和下单渠道为非分销商订单才需要进入结算
					if (!orderObj.isNeedSettlement()) {
						continue;
					}
					// 已经产生过结算记录了就不可以再次生成
					List<SetSettlementItem> setSettlementItemList= orderSettlementService
							.findSetSettlementItemByParams(ordOrderItem.getOrderId(), ordOrderItem.getOrderItemId());
					if (CollectionUtils.isNotEmpty(setSettlementItemList)) {
						continue;
					}
					
					String addition=this.getLoginUserId();
					this.orderLocalService.sendManualSettlmenteMsg(ordOrderItem.getOrderItemId(), addition);
					
					lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
							ordOrderItem.getOrderId(), ordOrderItem.getOrderItemId(),
							this.getLoginUserId(), 
							"将编号为["+ordOrderItem.getOrderItemId()+"]的订单子项，手动发起生成结算子项", 
							ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
							ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"手动发起生成结算子项",
							null);
					
				}
			} catch (Exception e) {
				return new ResultMessage(ResultMessage.ERROR,"需进行结算的订单数据有问题。");
			}
			return new ResultMessage(ResultMessage.SUCCESS,"操作成功");
		}
		return new ResultMessage(ResultMessage.ERROR,"需选择要生成的结算子项的订单。");
	}
	
	/**
     * 将Json对象转换成Map
     * 
     * @param jsonObject
     *            json对象
     * @return Map对象
     * @throws JSONException
     */
    public static List<OrdOrderItem> toOrderItemList(String jsonString) throws Exception {
        JSONObject jsonObject = JSONObject.fromObject(jsonString);
        JSONArray jsonArray = jsonObject.getJSONArray("orderItemList");
        int size = jsonArray.size();
        List<OrdOrderItem> ordOrderItemList = new ArrayList<OrdOrderItem>();
        for(int i=0;i<size;i++){
        	JSONObject jo = jsonArray.getJSONObject(i);
        	OrdOrderItem ordOrderItem = new OrdOrderItem();
        	Long orderId = jo.getLong("orderId");
        	Long orderItemId = jo.getLong("orderItemId");
        	ordOrderItem.setOrderId(orderId);
        	ordOrderItem.setOrderItemId(orderItemId);
        	ordOrderItemList.add(ordOrderItem);
        }
        return ordOrderItemList;

    }

	
	/**
	 * 保存
	 * @return
	 */
	@RequestMapping(value="/manualSettlmente")
	@ResponseBody
	public Object manualSettlmente(Model model,HttpServletRequest request,Long orderId,Long orderItemId){
	
		
		OrdOrder orderObj=this.orderUpdateService.queryOrdOrderByOrderId(orderId);
		//boolean havaSettlement=settlementServiceAdapter.searchSettlementPayByOrderItemMetaId(orderItemId);
		if (!orderObj.isNeedSettlement()) {
			return new ResultMessage(ResultMessage.ERROR,"预付和下单渠道为非分销商订单才需要进入结算");
		}
		List<SetSettlementItem> setSettlementItemList= orderSettlementService.findSetSettlementItemByParams(orderId, orderItemId);
		if (CollectionUtils.isNotEmpty(setSettlementItemList)) {
			return new ResultMessage(ResultMessage.ERROR,"该子订单已经产生过结算记录了，不可以再次生成");
		}
		//如果没有结算规则则提示错误
		if (!checkSuppSettleRule(orderItemId)) {
			return new ResultMessage(ResultMessage.ERROR,"操作失败，原因：没有结算规则");
		}
		
		String addition=this.getLoginUserId();
		this.orderLocalService.sendManualSettlmenteMsg(orderItemId, addition);
		
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
				orderId, 
				orderItemId, 
				this.getLoginUserId(), 
				"将编号为["+orderItemId+"]的订单子项，手动发起生成结算子项", 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"手动发起生成结算子项",
				null);
		
		return new ResultMessage(ResultMessage.SUCCESS,"操作成功");
	}

	/**
	 * 修改子订单价格确认状态
	 * @param orderId
	 * @param orderItemId
	 * @return
	 */
	@RequestMapping(value="/updateConfirmPrice")
	@ResponseBody
	public Object updateConfirmPrice(Long orderId,Long orderItemId){
		if(orderId==null ||orderItemId==null){
			return new ResultMessage(ResultMessage.ERROR,"价格确认参数错误");
		}
		int result= 0;
		try {
			result=updateOrdItemPriceConfirm(orderItemId);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			return new ResultMessage(ResultMessage.ERROR,"价格确认失败");
		}
		if(result>0){
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
					orderId,
					orderItemId,
					this.getLoginUserId(),
					"将编号为["+orderItemId+"]的订单子项，手动确认了价格状态",
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"手动确认了价格状态",
					null);

			return new ResultMessage(ResultMessage.SUCCESS,"价格确认成功");
		}

		return new ResultMessage(ResultMessage.ERROR,"价格确认失败");
	}

	/**
	 * 修改子订单价格确认状态
	 * @param  orderItemId
	 * @return void
	 */
	private  Integer updateOrdItemPriceConfirm(Long orderItemId){
		OrdOrderItem ordOrderItem=new OrdOrderItem();
		ordOrderItem.setOrderItemId(orderItemId);
		ordOrderItem.setPriceConfirmStatus(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.getCode());

		int result=this.orderUpdateService.updateOrderItemByIdSelective(ordOrderItem);
		if(result>0){
			LOG.info("sendOrdSettlementPriceChangeMsg2 orderItemId="+orderItemId);
			sendOrdItemPriceConfirmChangeMsg(orderItemId);
		}
		return result;

	}


	private boolean checkSuppSettleRule(Long orderItemId) {
		//检查是否 有结算规则，如果没有则报错
		OrdOrderItem orderItem = orderLocalService.getOrderItem(orderItemId);
		Map<String, Object> params = new HashMap<String, Object>();
        params.put("contractId", orderItem.getContractId());
        params.put("supplierId", orderItem.getSupplierId());
        ResultHandleT<List<SuppSettleRule>> result = suppSupplierClientService.findSuppSettleRuleList(params);
        if (result != null && result.getReturnContent() != null && !result.getReturnContent().isEmpty()) {
        	return true;
        }
        return false;
	}	
	
	
	/**
	 * 保存非买断实际单价格
	 * @return
	 */
	@RequestMapping(value="/addOrderSettlementChange")
	@ResponseBody
	public Object addOrderSettlementChange(Model model,HttpServletRequest request,ordSettlementPriceRecordListVo newOrdSettlementPriceRecordListVo,
			Long orderId,Long orderItemId){
		try{
			int pos = 0;
			int pos1 = 0;
			boolean isAmount=false;
			OrdOrderItem  orderItem  = null;
			List<OrdSettlementPriceRecord> list=new ArrayList<OrdSettlementPriceRecord>();
			StringBuffer logContent=new StringBuffer();
			
			String[] priceTypes = request.getParameterValues("priceType");
			
			for(OrdSettlementPriceRecord record:newOrdSettlementPriceRecordListVo.getForm()){
				//checkbox
				String checkPriceType = request.getParameter("checkPriceType"+ (pos++));
				String newActualSettlementPrice = request.getParameter("newActualSettlementPrice"+ (pos1++));
				 
				String checkNewActualSettlementPrice="";
				//多价格类型类型的时候
				if (priceTypes!=null && checkPriceType != null) {
					for (int i = 0; i < priceTypes.length; i++) {
						if (checkPriceType.equals(priceTypes[i])) {
							checkNewActualSettlementPrice=newActualSettlementPrice;
							
							if (!StringUtils.isEmpty(checkNewActualSettlementPrice) && StringUtil.isNumberAmount(checkNewActualSettlementPrice)) {
								isAmount=true;
							}else{
								isAmount=false;
							}
							break;
						}
					}
				}else{
					if (!StringUtils.isEmpty(newActualSettlementPrice) && StringUtil.isNumberAmount(newActualSettlementPrice)) {
						isAmount=true;
					}else{
						isAmount=false;
					}
				}
				
				if (!isAmount) {
					return new ResultMessage(ResultMessage.ERROR," 请至少填写一个为正数(或者2位小数)金额");
				}
			}
				
			//查询订单
			//OrdOrder order=this.orderLocalService.queryOrdorderByOrderId(orderId);
			orderItem  = this.orderUpdateService.getOrderItem(orderItemId);
			Long buyNum = orderItem.getBuyoutQuantity();
			buyNum = buyNum==null?0L:buyNum;
			if("Y".equals(orderItem.getBuyoutFlag())){
				Long buyoutTotalPrice = orderItem.getBuyoutTotalPrice();
				buyoutTotalPrice = buyoutTotalPrice==null?0L:buyoutTotalPrice;
				Long buyoutQuantity = orderItem.getBuyoutQuantity();
				buyoutQuantity = buyoutQuantity==null?0L:buyoutQuantity;
				orderItem.setTotalSettlementPrice(orderItem.getTotalSettlementPrice() - buyoutTotalPrice);
				Long notBuyoutQuantity = orderItem.getQuantity()-buyoutQuantity;
				notBuyoutQuantity = notBuyoutQuantity==0L?1L:notBuyoutQuantity;
				orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/(notBuyoutQuantity));
			}
				
			pos = 0;
			pos1 = 0;
			for(OrdSettlementPriceRecord record:newOrdSettlementPriceRecordListVo.getForm()){
				//checkbox
				String checkPriceType = request.getParameter("checkPriceType"+ (pos++));
				String newActualSettlementPrice = request.getParameter("newActualSettlementPrice"+ (pos1++));
				String checkNewActualSettlementPrice="";
				//多价格类型类型的时候
				if (priceTypes!=null && checkPriceType != null) {
					for (int i = 0; i < priceTypes.length; i++) {
						if (checkPriceType.equals(priceTypes[i])) {
							checkNewActualSettlementPrice=newActualSettlementPrice;
							break;
						}
					}
				}
				
				//订单状态和是否有在审核记录
				/*ResultMessage resultMessage=this.validateAmountChange(order, orderItemId, priceTypes);
				if (!resultMessage.isSuccess()) {
					return resultMessage;
				}*/
				OrdSettlementPriceRecord newOrdSettlementPriceRecord = new OrdSettlementPriceRecord();
				newOrdSettlementPriceRecord.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.UNIT_PRICE.getCode());
				newOrdSettlementPriceRecord.setOperator(this.getLoginUserId());
				newOrdSettlementPriceRecord.setCreateTime(new Date());
				newOrdSettlementPriceRecord.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
				newOrdSettlementPriceRecord.setOrderId(orderId);
				newOrdSettlementPriceRecord.setOrderItemId(orderItemId);
				newOrdSettlementPriceRecord.setSuppGoodsId(orderItem.getSuppGoodsId());
				newOrdSettlementPriceRecord.setVisitTime(orderItem.getVisitTime());
				newOrdSettlementPriceRecord.setIsApprove("Y");
				newOrdSettlementPriceRecord.setReason(record.getReason());
				newOrdSettlementPriceRecord.setRemark(record.getRemark());
				newOrdSettlementPriceRecord.setSupplierId(orderItem.getSupplierId());
				
				Long newTotalSettlementPrice=0L;
				
				//单价格修改
				if (priceTypes==null || priceTypes.length==0) {
					//修改之前的结算单价
					newOrdSettlementPriceRecord.setOldActualSettlementPrice(orderItem.getActualSettlementPrice());
					//修改之前的结算总价
					newOrdSettlementPriceRecord.setOldTotalSettlementPrice(orderItem.getTotalSettlementPrice());
					//修改之后的结算单价
					newOrdSettlementPriceRecord.setNewActualSettlementPrice(PriceUtil.convertToFen(newActualSettlementPrice));
					
					newTotalSettlementPrice=newOrdSettlementPriceRecord.getNewActualSettlementPrice()*(orderItem.getQuantity()-buyNum);
					//修改之后的结算总价
					newOrdSettlementPriceRecord.setNewTotalSettlementPrice(newTotalSettlementPrice);
					//修改价格类型
					newOrdSettlementPriceRecord.setPriceType("PRICE");
					
					list.add(newOrdSettlementPriceRecord);
					
					logContent.append("原结算单价：").append(newOrdSettlementPriceRecord.getOldActualSettlementPrice()/100.0).append("新结算单价：").append(newOrdSettlementPriceRecord.getNewActualSettlementPrice()/100.0);
				} else if(checkPriceType != null){
					OrdSettlementPriceRecord ordSettlementPriceRecordObj = new OrdSettlementPriceRecord();
					BeanUtils.copyProperties(newOrdSettlementPriceRecord,ordSettlementPriceRecordObj);

					Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
					paramsMulPriceRate.put("orderItemId", orderItemId);
					paramsMulPriceRate.put("priceType", checkPriceType);
					List<OrdMulPriceRate> ordMulPriceRateList = ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
					
					OrdMulPriceRate ordMulPriceRate = ordMulPriceRateList.get(0);
					//修改之前的结算单价
					ordSettlementPriceRecordObj.setOldActualSettlementPrice(ordMulPriceRate.getPrice());
					//修改之前的结算总价
					ordSettlementPriceRecordObj.setOldTotalSettlementPrice(ordMulPriceRate.getPrice() * ordMulPriceRate.getQuantity());
					
					//修改之后的结算单价
					ordSettlementPriceRecordObj.setNewActualSettlementPrice(PriceUtil.convertToFen(checkNewActualSettlementPrice));
					ordSettlementPriceRecordObj.setPriceType(checkPriceType);

					newTotalSettlementPrice = 0L;
					newTotalSettlementPrice = ordSettlementPriceRecordObj.getNewActualSettlementPrice()* ordMulPriceRate.getQuantity();
					//修改之后的结算总价
					ordSettlementPriceRecordObj.setNewTotalSettlementPrice(newTotalSettlementPrice);

					list.add(ordSettlementPriceRecordObj);

					logContent.append("原").append(ORDER_PRICE_RATE_TYPE.PRICE_ADULT_12.getCnName(checkPriceType)).append("：")
							.append(ordSettlementPriceRecordObj.getOldActualSettlementPrice() / 100.0).append("新结算单价：")
							.append(ordSettlementPriceRecordObj.getNewActualSettlementPrice() / 100.0);

				}	
			}
			String changeResult=OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.UP.getCode();
			//订单结算状态校验
			ResultMessage resultMessage = this.validateOrderSettlementStatus(orderItem);
			for (OrdSettlementPriceRecord record : list) {
				if ( (record.getNewTotalSettlementPrice()-record.getOldTotalSettlementPrice())<0) {
					changeResult=OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.getCode();
				}
				record.setChangeResult(changeResult);
				if (resultMessage.isSuccess()) {
					record.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
					record.setOperator(this.getLoginUserId());
					//record.setOperatorApprove("system");
					//record.setUpdateTime(new Date());
					record.setIsApprove("Y");
					record.setChangeRemark("0");//没有结算过的
				} else {
					record.setChangeRemark("1");//结算过的
					//ordSettlementPriceRecordService.saveAfterApprove(null,record, orderId);
				}
				
				int n=ordSettlementPriceRecordService.saveAfterApprove(null,record, orderId);
				LOG.info("saveAfterApprove执行行数: n="+n);
				if (n==1) {//订单修改成功的才做推送结算和生成变价单
					if (resultMessage.isSuccess()) {//订单修改成功并且没有结算完成的，推送结算
						LOG.info("sendOrdSettlementPriceChangeMsg1 orderItemId="+orderItemId);
						sendOrdSettlementPriceChangeMsg(record.getOrderItemId());
					}
					//调用结算系统生成结算单
					LOG.info("order_item_id=" + record.getOrderItemId() + " vst_record_id=" + record.getRecordId());
					record.setChangeFlag("Normal");
					
					OrdOrder order=iOrdOrderService.findByOrderId(orderItem.getOrderId());
					if (order.hasPayed() && order.isPayToLvmama() && order.hasInfoAndResourcePass()) {
						LOG.info("保存变价信息开始");
						try {
							  orderSettlementService.insertRecord(record);
						} catch (Exception e) {
							LOG.error(ExceptionFormatUtil.getTrace(e));
								//LOG.info("保存变价信息失败");
						}
						LOG.info("保存变价信息结束");
					}else{
						 LOG.info( "order:" + order.getOrderId() + " isPayToLvmama:" + order.isPayToLvmama() + " hasPayed status:" + order.hasPayed() + " order status:" + order.getOrderStatus() + ", don't need to settlement!");
					}
					
				}
			}

			try {
				LOG.info("修改子订单价格确认状态开始");
				updateOrdItemPriceConfirm(orderItemId);
				LOG.info("修改子订单价格确认状态结束");
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
			}


			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
					orderId, 
					orderItemId, 
					getLoginUserId(), 
					"将编号为["+orderId+"]的子订单，修改子订单结算单价，修改值："+logContent.toString(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName()+"：发起订单结算价价格修改申请","");
			
			String info="修改申请已经提交。由于下述原因，需财务审核：a.子订单结算中 b.子订单已结算  c.有负毛利的风险";
			if (resultMessage.isSuccess()) {
				info="结算价修改成功";
			} else {
				info = "该订单已经结算过，生成结算单！";
			}
							
			return new ResultMessage(ResultMessage.SUCCESS,info);
			
		}catch(Exception e){
			LOG.error(ExceptionUtil.getExceptionDetails(e));
		}
		return new ResultMessage(ResultMessage.ERROR," 操作失败，系统内部异常");
	}
	
	
	
	/**
	 * 保存非买断的总价格
	 * @return
	 */
	@RequestMapping(value="/addOrderTotalSettlementChange")
	@ResponseBody
	public Object addOrderTotalSettlementChange(Model model,HttpServletRequest request){
		try{
			boolean isAmount=false;
			OrdOrderItem  orderItem  = null;
			List<OrdSettlementPriceRecord> list=new ArrayList<OrdSettlementPriceRecord>();
			StringBuffer logContent=new StringBuffer();
			
			Long orderId = Long.parseLong(request.getParameter("orderId"));
			Long orderItemId = Long.parseLong(request.getParameter("orderItemId"));
			Long ordMulPriceRateListCount = Long.parseLong(request.getParameter("ordMulPriceRateListCount"));//>1 ，表示是多价格类型
			String reason = request.getParameter("reason");//修改原因
			String remark = request.getParameter("remark");//备注
			String totalSettlementPrice = request.getParameter("totalSettlementPrice");// 修改后的总价格

			if (!StringUtils.isEmpty(totalSettlementPrice) && StringUtil.isNumberAmount(totalSettlementPrice)) {
				isAmount = true;
			} else {
				isAmount = false;
			}

			if (!isAmount) {
				return new ResultMessage(ResultMessage.ERROR," 请至少填写一个为正数(或者2位小数)金额");
			}
				
			// 查询子订单
			orderItem = this.orderUpdateService.getOrderItem(orderItemId);
			
			Long buyoutTotalPrice = 0L;
			Long buyNum = orderItem.getBuyoutQuantity();
			buyNum = buyNum==null?0L:buyNum;
			if("Y".equals(orderItem.getBuyoutFlag())){
				buyoutTotalPrice = orderItem.getBuyoutTotalPrice();
				buyoutTotalPrice = buyoutTotalPrice==null?0L:buyoutTotalPrice;
				orderItem.setTotalSettlementPrice(orderItem.getTotalSettlementPrice() - buyoutTotalPrice);
				
				Long buyoutQuantity = orderItem.getBuyoutQuantity();
				buyoutQuantity = buyoutQuantity==null?0L:buyoutQuantity;
				Long notBuyoutQuantity = orderItem.getQuantity()-buyoutQuantity;
				notBuyoutQuantity = notBuyoutQuantity==0L?1L:notBuyoutQuantity;
				orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/(notBuyoutQuantity));
				
			}
			

			OrdSettlementPriceRecord newOrdSettlementPriceRecord = new OrdSettlementPriceRecord();
			newOrdSettlementPriceRecord.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.TOTAL_PRICE.getCode());
			newOrdSettlementPriceRecord.setOperator(this.getLoginUserId());
			newOrdSettlementPriceRecord.setCreateTime(new Date());
			newOrdSettlementPriceRecord.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
			newOrdSettlementPriceRecord.setOrderId(orderId);
			newOrdSettlementPriceRecord.setOrderItemId(orderItemId);
			newOrdSettlementPriceRecord.setSuppGoodsId(orderItem.getSuppGoodsId());
			newOrdSettlementPriceRecord.setVisitTime(orderItem.getVisitTime());
			newOrdSettlementPriceRecord.setIsApprove("Y");
			newOrdSettlementPriceRecord.setApproveRemark("总价无需审核");
			newOrdSettlementPriceRecord.setReason(reason);
			newOrdSettlementPriceRecord.setRemark(remark);
			newOrdSettlementPriceRecord.setSupplierId(orderItem.getSupplierId());

			// 修改之前的结算单价
			newOrdSettlementPriceRecord.setOldActualSettlementPrice(orderItem.getActualSettlementPrice());
			// 修改之前的结算总价
			newOrdSettlementPriceRecord.setOldTotalSettlementPrice(orderItem.getTotalSettlementPrice());

			//计算后的单价格
			OrdOrderItem item = ordSettlementPriceRecordService.resetOrderItem4Settlement(orderItem, PriceUtil.convertToFen(totalSettlementPrice));

			// 修改之后的结算单价
			newOrdSettlementPriceRecord.setNewActualSettlementPrice(item.getActualSettlementPrice());

			// 修改之后的结算总价
			newOrdSettlementPriceRecord.setNewTotalSettlementPrice(item.getTotalSettlementPrice());
			// 修改价格类型
			newOrdSettlementPriceRecord.setPriceType("PRICE");

			newOrdSettlementPriceRecord.setOperator(this.getLoginUserId());
//			newOrdSettlementPriceRecord.setOperatorApprove("system");
			//newOrdSettlementPriceRecord.setUpdateTime(new Date());

			list.add(newOrdSettlementPriceRecord);

			logContent.append("原结算总价：").append(newOrdSettlementPriceRecord.getOldTotalSettlementPrice() / 100.0)
					.append("新结算总价：").append(newOrdSettlementPriceRecord.getNewTotalSettlementPrice() / 100.0);
			
			List<OrdMulPriceRate> ordMulPriceRateList = null;
			if (ordMulPriceRateListCount >= 1) {
				list.clear();
				//计算后的多价格
				ordMulPriceRateList = ordSettlementPriceRecordService.calcSettlementUnitPrice(orderItemId, PriceUtil.convertToFen(totalSettlementPrice));
				if (ordMulPriceRateList != null && ordMulPriceRateList.size() > 0) {
					for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {
						//修改总价的时候记录单价的变更记录，目的是为了取得原始的价格记录
						OrdSettlementPriceRecord ordSettlementPriceRecordObj = new OrdSettlementPriceRecord();
						BeanUtils.copyProperties(newOrdSettlementPriceRecord, ordSettlementPriceRecordObj);
						// 修改之前的结算单价
						ordSettlementPriceRecordObj.setOldActualSettlementPrice(ordMulPriceRate.getOrigPrice());
						// 修改之前的结算总价
						ordSettlementPriceRecordObj.setOldTotalSettlementPrice(ordMulPriceRate.getOrigPrice() * ordMulPriceRate.getQuantity());
	
						// 修改之后的结算单价,多价格的时候设置修改后的结算单价为null，以此为依据判断历史记录中是否显示
					    //ordSettlementPriceRecordObj.setNewActualSettlementPrice(null);
						ordSettlementPriceRecordObj.setPriceType(ordMulPriceRate.getPriceType());
						
						// 修改之后的结算总价
						//newTotalSettlementPrice = ordSettlementPriceRecordObj.getNewActualSettlementPrice()* ordMulPriceRate.getQuantity();
					    //ordSettlementPriceRecordObj.setNewTotalSettlementPrice(newTotalSettlementPrice);
	
						list.add(ordSettlementPriceRecordObj);
					}
				}
			}
			
			//处理结算总价的记录
			String changeResult = OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.UP.getCode();
			OrdSettlementPriceRecord record = list.get(0);
			if ((record.getNewTotalSettlementPrice() - record.getOldTotalSettlementPrice()) < 0) {
				changeResult=OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.getCode();
			}
			record.setChangeResult(changeResult);
			//boolean needApprove = isNeedApprove(orderItem, record);
			// 订单结算状态校验
			ResultMessage resultMessage = this.validateOrderSettlementStatus(orderItem);
			//保存结算记录和价格变更记录
			this.ordSettlementPriceRecordService.addSettlementTotalPrice(list, orderId);
			
			//修改子订单
			//如果含买断的，要将买断的加上去
			if("Y".equals(orderItem.getBuyoutFlag())){
				orderItem.setTotalSettlementPrice(PriceUtil.convertToFen(totalSettlementPrice) + buyoutTotalPrice);
				orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
			}
			ResultHandle resultHandle = orderLocalService.updateOrderItem(orderItem);
			if (resultHandle.isSuccess()) {//子订单修改成功再去推送结算价及推送变价单


				//外币保存（存在extend为人民币情况默认保存一样的结算价）
				ordSettlementPriceRecordService.saveExtendPrice(orderItem);

				//修改多价格记录
				ordSettlementPriceRecordService.updateMulPriceRates(ordMulPriceRateList);
				if (resultMessage.isSuccess()) {//如果是结算完成的订单则不推送结算也不更改订单数据，只记录修改历史和生成变价单
					record.setChangeRemark("0");//结算前修改
					//推送结算
					sendOrdSettlementPriceChangeMsg(record.getOrderItemId());
					
				} else {
					record.setChangeRemark("1");//结算后修改
				}
				
				//调用结算系统生成结算单
				record.setChangeFlag("Normal");
				orderSettlementService.insertRecord(record);
			}
			try {
				updateOrdItemPriceConfirm(orderItemId);
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
			}
			
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
					orderId, 
					orderItemId, 
					getLoginUserId(), 
					"将编号为["+orderId+"]的子订单，修改子订单结算总价，修改值："+logContent.toString(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName()+"：发起订单结算价价格修改申请","");
			
			String info="修改申请已经提交。由于下述原因，需财务审核：a.子订单结算中 b.子订单已结算  c.有负毛利的风险";
			
			if (resultMessage.isSuccess()) {
				info = "结算价修改成功";
			} else {
				info = "该订单已经结算过！";
			}
			
			return new ResultMessage(ResultMessage.SUCCESS,info);
			
		}catch(Exception e){
			LOG.error(ExceptionUtil.getExceptionDetails(e));
		}
		return new ResultMessage(ResultMessage.ERROR," 操作失败，系统内部异常");
	}
	
	/**修改买断结算价-多价格-单价和总价
	 * @param model
	 * @param request
	 * @param orderItemId
	 * @param priceModel
	 * @param reason
	 * @param remark
	 * @return
	 */
	@RequestMapping(value="/addOrderMultiBudgetSettlementChange")
    @ResponseBody
    public Object addOrderMultiBudgetSettlementChange(Model model,HttpServletRequest request,Long orderItemId,String priceModel,
    		String reason,String remark){
		
		String settlementChildPriceStr = request.getParameter("settlementChildPrice");
		Long settlementChildPrice = settlementChildPriceStr!=null?Long.valueOf(settlementChildPriceStr):null;
		String settlementAdultPriceStr = request.getParameter("settlementAdultPrice");
		Long settlementAdultPrice = settlementAdultPriceStr!=null?Long.valueOf(settlementAdultPriceStr):null;
		String buyoutTotalPriceStr = request.getParameter("buyoutTotalPrice");
		Long buyoutTotalPrice = buyoutTotalPriceStr!=null?Long.valueOf(buyoutTotalPriceStr):null;
		LOG.info("--------method addOrderMultiBudgetSettlementChange start:"+orderItemId+"_buyoutTotalPrice="+buyoutTotalPrice);
		
		List<OrdSettlementPriceRecord> list=new ArrayList<OrdSettlementPriceRecord>();
		OrdOrderItem orderItem = this.orderUpdateService.getOrderItem(orderItemId);
		Long orderId = orderItem.getOrderId();
		Long quantity = orderItem.getQuantity();
		Long buyoutQuantity = orderItem.getBuyoutQuantity();
		//原先买断的总价
		Long preBuyoutTotalPrice = orderItem.getBuyoutTotalPrice();
		//非买断的，其他结算价
		Long otherTotalPrice = orderItem.getTotalSettlementPrice() - orderItem.getBuyoutTotalPrice();
		//非买断总价
		Long notBuyoutTotalPrice = quantity.intValue() == buyoutQuantity.intValue()?null:otherTotalPrice;
		//非买断单价
		Long notBuyoutPrice = quantity.intValue() == buyoutQuantity.intValue()?null:notBuyoutTotalPrice/(orderItem.getQuantity() - orderItem.getBuyoutQuantity());
		LOG.info("--------method addOrderMultiBudgetSettlementChange :"+orderItemId+"——otherTotalPrice"+otherTotalPrice);
		LOG.info("--------method addOrderMultiBudgetSettlementChange :"+orderItemId+"——notBuyoutTotalPrice"+notBuyoutTotalPrice);
		LOG.info("--------method addOrderMultiBudgetSettlementChange :"+orderItemId+"买断价格设置完成，preBuyoutTotalPrice"+preBuyoutTotalPrice);
		//记录差价
		Long buyoutTotalAmount = 0L;
		
		if(!"BUDGET_UNIT_PRICE".equalsIgnoreCase(priceModel) && !"BUDGET_TOTAL_PRICE".equalsIgnoreCase(priceModel)){
            return new ResultMessage(ResultMessage.ERROR,"价格模式错误！！！");
		}
		StringBuilder logContent = new StringBuilder(300);
		if("BUDGET_UNIT_PRICE".equals(priceModel)){
			Long newTotalPrice = 0L;
			
			//修改单价
			Long newChildPrice = settlementChildPrice;
			Long newAdultPrice = settlementAdultPrice;
			String multiPrePriceType[] = new String[]{
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode(),
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode()
			};
			for(int x=0,y=multiPrePriceType.length;x<y;x++){
				String checkPriceType = multiPrePriceType[x];
				Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
				paramsMulPriceRate.put("orderItemId", orderItemId);
				paramsMulPriceRate.put("priceType", checkPriceType);
				List<OrdMulPriceRate> ordMulPriceRateList = ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
				if(ordMulPriceRateList!=null && ordMulPriceRateList.size()<=0){
					continue;
				}
				OrdMulPriceRate ordMulPriceRate = ordMulPriceRateList.get(0);
				boolean hasChanged = false;
				Long newPrice = null;
				if(checkPriceType.equals(ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode())){
					if(newAdultPrice!=null&&newAdultPrice.compareTo(ordMulPriceRate.getPrice()) != 0){
						hasChanged = true;
						newPrice = newAdultPrice;
						buyoutTotalAmount += (newAdultPrice - ordMulPriceRate.getPrice()) * ordMulPriceRate.getQuantity();
						logContent.append("原").append(ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCnName(checkPriceType)).append("：")
						.append(ordMulPriceRate.getPrice() / 100.0).append("新结算单价：")
						.append(newPrice);
					}
				}else{
					if(newChildPrice!=null&&newChildPrice.compareTo(ordMulPriceRate.getPrice()) != 0){
						hasChanged = true;
						newPrice = newChildPrice;
						buyoutTotalAmount += (newChildPrice - ordMulPriceRate.getPrice()) * ordMulPriceRate.getQuantity();
						logContent.append("原").append(ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCnName(checkPriceType)).append("：")
						.append(ordMulPriceRate.getPrice() / 100.0).append("新结算单价：")
						.append(newPrice);
					}
				}
				if(hasChanged){
					OrdSettlementPriceRecord newOrdSettlementPriceRecord = new OrdSettlementPriceRecord();
					newOrdSettlementPriceRecord.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.BUDGET_UNIT_PRICE.getCode());
					newOrdSettlementPriceRecord.setOperator(this.getLoginUserId());
					newOrdSettlementPriceRecord.setCreateTime(new Date());
					newOrdSettlementPriceRecord.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
					newOrdSettlementPriceRecord.setOrderId(orderId);
					newOrdSettlementPriceRecord.setOrderItemId(orderItemId);
					newOrdSettlementPriceRecord.setSuppGoodsId(orderItem.getSuppGoodsId());
					newOrdSettlementPriceRecord.setVisitTime(orderItem.getVisitTime());
					newOrdSettlementPriceRecord.setIsApprove("Y");
					newOrdSettlementPriceRecord.setReason(reason);
					newOrdSettlementPriceRecord.setRemark(remark);
					newOrdSettlementPriceRecord.setSupplierId(orderItem.getSupplierId());
					newOrdSettlementPriceRecord.setPriceType(checkPriceType);
					//因为只修改买断的-所以非买断的不管怎么样都是不变的
//					newOrdSettlementPriceRecord.setOldActualSettlementPrice(notBuyoutPrice);
//					newOrdSettlementPriceRecord.setNewActualSettlementPrice(notBuyoutPrice);
//					newOrdSettlementPriceRecord.setOldTotalSettlementPrice(notBuyoutTotalPrice);
//					newOrdSettlementPriceRecord.setNewTotalSettlementPrice(notBuyoutTotalPrice);
					
					//修改之前的买断结算单价
					newOrdSettlementPriceRecord.setOldBudgetUnitSettlementPrice(ordMulPriceRate.getPrice());
					//修改之后的结算买断单价
					newOrdSettlementPriceRecord.setNewBudgetUnitSettlementPrice(newPrice);
					//修改之前的买断结算总价
					newOrdSettlementPriceRecord.setOldBudgetTotalSettlementPrice(ordMulPriceRate.getPrice() * ordMulPriceRate.getQuantity());
					//修改之后的买断结算总价
					newOrdSettlementPriceRecord.setNewBudgetTotalSettlementPrice(newPrice* ordMulPriceRate.getQuantity());
					
					newOrdSettlementPriceRecord.setChangeFlag("BUYOUT");
					newTotalPrice += newOrdSettlementPriceRecord.getNewBudgetTotalSettlementPrice();
					list.add(newOrdSettlementPriceRecord);
				}
			}
			buyoutTotalAmount = newTotalPrice - preBuyoutTotalPrice;
		}else if("BUDGET_TOTAL_PRICE".equals(priceModel)){
			
			buyoutTotalAmount = buyoutTotalPrice - preBuyoutTotalPrice;
			
			String multiPrePriceType[] = new String[]{
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode(),
					ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode()
			};
			List<OrdMulPriceRate> ordMulPriceRateList = ordSettlementPriceRecordService.calcBuyoutSettlementUnitPrice(orderItemId, buyoutTotalPrice);
			LOG.info("--------method addOrderMultiBudgetSettlementChange ordMulPriceRateList size:"+ordMulPriceRateList.size());
			for(int x=0,y=multiPrePriceType.length;x<y;x++){
				String checkPriceType = multiPrePriceType[x];
				OrdMulPriceRate ordMulPriceRate = null;
				for(int m=0,n=ordMulPriceRateList.size();m<n;m++){
					if(ordMulPriceRateList.get(m)!=null&& checkPriceType.equals(ordMulPriceRateList.get(m).getPriceType())){
						ordMulPriceRate = ordMulPriceRateList.get(m);
						break;
					}
				}
				if(ordMulPriceRate==null){
					//如果没有对应的结算价-则next
					continue;
				}
				LOG.info("--------method addOrderMultiBudgetSettlementChange ordMulPriceRate id="+ordMulPriceRate.getOrdMulPriceRateId());
				Long newPrice = null;
				if(checkPriceType.equals(ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode())){
					newPrice = ordMulPriceRate.getPrice();
					logContent.append("原").append(ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCnName(checkPriceType)).append("：")
						.append(ordMulPriceRate.getOrigPrice() / 100.0).append("新结算单价：")
						.append(newPrice);
				}else{
					newPrice = ordMulPriceRate.getPrice();
					logContent.append("原").append(ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCnName(checkPriceType)).append("：")
						.append(ordMulPriceRate.getOrigPrice() / 100.0).append("新结算单价：")
						.append(newPrice);
				}
				OrdSettlementPriceRecord newOrdSettlementPriceRecord = new OrdSettlementPriceRecord();
				newOrdSettlementPriceRecord.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.BUDGET_TOTAL_PRICE.getCode());
				newOrdSettlementPriceRecord.setOperator(this.getLoginUserId());
				newOrdSettlementPriceRecord.setCreateTime(new Date());
				newOrdSettlementPriceRecord.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
				newOrdSettlementPriceRecord.setOrderId(orderId);
				newOrdSettlementPriceRecord.setOrderItemId(orderItemId);
				newOrdSettlementPriceRecord.setSuppGoodsId(orderItem.getSuppGoodsId());
				newOrdSettlementPriceRecord.setVisitTime(orderItem.getVisitTime());
				newOrdSettlementPriceRecord.setIsApprove("Y");
				newOrdSettlementPriceRecord.setReason(reason);
				newOrdSettlementPriceRecord.setRemark(remark);
				newOrdSettlementPriceRecord.setSupplierId(orderItem.getSupplierId());
				newOrdSettlementPriceRecord.setPriceType(checkPriceType);
				//因为只修改买断的-所以非买断的不管怎么样都是不变的
//				newOrdSettlementPriceRecord.setOldActualSettlementPrice(notBuyoutPrice);
//				newOrdSettlementPriceRecord.setNewActualSettlementPrice(notBuyoutPrice);
//				newOrdSettlementPriceRecord.setOldTotalSettlementPrice(notBuyoutTotalPrice);
//				newOrdSettlementPriceRecord.setNewTotalSettlementPrice(notBuyoutTotalPrice);
				//修改之前的买断结算单价
				newOrdSettlementPriceRecord.setOldBudgetUnitSettlementPrice(ordMulPriceRate.getOrigPrice());
				//修改之后的结算买断单价
				newOrdSettlementPriceRecord.setNewBudgetUnitSettlementPrice(newPrice);
				//修改之前的买断结算总价
				newOrdSettlementPriceRecord.setOldBudgetTotalSettlementPrice(ordMulPriceRate.getOrigPrice() * ordMulPriceRate.getQuantity());
				//修改之后的买断结算总价
				newOrdSettlementPriceRecord.setNewBudgetTotalSettlementPrice(newPrice * ordMulPriceRate.getQuantity());
		
				newOrdSettlementPriceRecord.setChangeFlag("BUYOUT");
				list.add(newOrdSettlementPriceRecord);
			}
			
		}
		String changeResult=OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.UP.getCode();
		//订单结算状态校验
		ResultMessage resultMessage = this.validateOrderSettlementStatus(orderItem);
		LOG.info("--------method addOrderMultiBudgetSettlementChange ordMulPriceRate RECORDlIST="+GsonUtils.toJson(list));
		int updateOrderItemCount = 0;
		Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
		for (OrdSettlementPriceRecord record : list) {
			
			Long a = record.getNewTotalSettlementPrice();
			a = a==null?0L:a;
			Long b = record.getNewBudgetTotalSettlementPrice();
			b = b==null?0L:b;
			Long c = record.getOldTotalSettlementPrice();
			c = c==null?0L:c;
			Long d = record.getOldTotalSettlementPrice();
			d = d==null?0L:d;
			
			if ( (a+b-c-d)<0) {
				changeResult=OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.getCode();
			}
			LOG.info("--------method addOrderMultiBudgetSettlementChange  changeResult="+changeResult);
			record.setChangeResult(changeResult);
			if (resultMessage.isSuccess()) {
				record.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
				record.setOperator(this.getLoginUserId());
				record.setIsApprove("Y");
				record.setChangeRemark("0");//没有结算过的
			} else {
				record.setChangeRemark("1");//结算过的
			}
			LOG.info("--------method addOrderMultiBudgetSettlementChange  saveBuyoutMultiPriceAfterApprove buyoutTotalPrice="+buyoutTotalPrice);
			int n=ordSettlementPriceRecordService.saveBuyoutMultiPriceAfterApprove(buyoutTotalPrice,record, orderId,priceModel);
			LOG.info("--------method addOrderMultiBudgetSettlementChange  saveBuyoutMultiPriceAfterApprove n="+n);
			//修改子订单的多价格的数据库
			paramsMulPriceRate.clear();
			paramsMulPriceRate.put("orderItemId", orderItem.getOrderItemId()); 
			paramsMulPriceRate.put("priceTypeArray",new String[]{record.getPriceType()}); 
			List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
			LOG.info("--------method addOrderMultiBudgetSettlementChange  findOrdMulPriceRateList size="+ordMulPriceRateList.size());
			if (ordMulPriceRateList.size()>0) {
				for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {
					if (record.getPriceType().equalsIgnoreCase(ordMulPriceRate.getPriceType())) {
						//更新多价格类型对应结算价
						ordMulPriceRate.setPrice(record.getNewBudgetUnitSettlementPrice());
						ordMulPriceRateService.updateByPrimaryKeySelective(ordMulPriceRate);
					}
				}
			}
			LOG.info("--------method addOrderMultiBudgetSettlementChange  findOrdMulPriceRateList size="+ordMulPriceRateList.size());
			//订单修改成功的才做推送结算和生成变价单
			if (n==1) {
				if (resultMessage.isSuccess()&& updateOrderItemCount==0) {//订单修改成功并且没有结算完成的，推送结算
					LOG.info("子订单，以及该子订单的推送，只要执行一次即可");
					orderItem.setBuyoutTotalPrice(buyoutTotalAmount + orderItem.getBuyoutTotalPrice());
					orderItem.setBuyoutPrice(orderItem.getBuyoutTotalPrice()/orderItem.getBuyoutQuantity());
					orderItem.setTotalSettlementPrice(orderItem.getBuyoutTotalPrice()  + otherTotalPrice);
					orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
					updateOrderItemCount = ordOrderItemService.updateOrdOrderItem(orderItem);
					if(updateOrderItemCount == 1L){
						//更改结算成功后，要对预控的金额进行更新，如果为0，那么要提醒变价；
						Long nowBuyoutTotalPrice = orderItem.getBuyoutTotalPrice();
	                    nowBuyoutTotalPrice = nowBuyoutTotalPrice==null?0L:nowBuyoutTotalPrice;
	                    if(nowBuyoutTotalPrice.longValue() != preBuyoutTotalPrice.longValue()){
	                    	Date visitDate = orderItem.getVisitTime();
	                    	Long goodsId = orderItem.getSuppGoodsId();
	                    	GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
	                    	long p = Math.min(preBuyoutTotalPrice, goodsResPrecontrolPolicyVO.getAmount()) - nowBuyoutTotalPrice;
	                    	if(goodsResPrecontrolPolicyVO!=null && ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equals(goodsResPrecontrolPolicyVO.getControlType())){
	                    		Long amountId = goodsResPrecontrolPolicyVO.getAmountId();
	                    		Long controlId = goodsResPrecontrolPolicyVO.getId();
	                    		Long leftAmount = goodsResPrecontrolPolicyVO.getLeftAmount();
	                    		LOG.info(goodsId+"差价" + p);
	                    		LOG.info(goodsId+"剩余金额" + leftAmount);
	                    		Long leftValue = leftAmount+p;
	                    		leftValue = leftValue< 0? 0L:leftValue;
	                    		leftValue = leftValue> goodsResPrecontrolPolicyVO.getAmount()? goodsResPrecontrolPolicyVO.getAmount():leftValue;
	                    		resControlBudgetRemote.updateAmountResPrecontrolPolicy(amountId,controlId, visitDate, leftValue);
	                    		if(leftValue == 0L){
	                    			resControlBudgetRemote.handleResPrecontrolSaledOut(goodsResPrecontrolPolicyVO, orderItem.getVisitTime(), goodsId);
	                    		}
	                    	}
	                    	
	                    }
					}
					LOG.info("--------method addOrderMultiBudgetSettlementChange  sendOrdSettlementPriceChangeMsg begin");
					//更新子订单
					sendOrdSettlementPriceChangeMsg(record.getOrderItemId());
					LOG.info("--------method addOrderMultiBudgetSettlementChange  sendOrdSettlementPriceChangeMsg end");
				}
				//调用结算系统生成结算单
				LOG.info("order_item_id=" + record.getOrderItemId() + " vst_record_id=" + record.getRecordId());
				orderSettlementService.insertRecord(record);
			}
		}
		
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
				orderId, 
				orderItemId, 
				getLoginUserId(), 
				"将编号为["+orderId+"]的子订单，修改子订单结算单价，修改值："+logContent.toString(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName()+"：发起订单买断结算价价格修改申请","");
		
		
		
		String info="修改申请已经提交。由于下述原因，需财务审核：a.子订单结算中 b.子订单已结算  c.有负毛利的风险";

        if (resultMessage.isSuccess()) {
            info = "结算价修改成功";
        } else {
            info = "该订单已经结算过！";
        }
		return new ResultMessage(ResultMessage.SUCCESS,info);
	}
	
    /**
     * 保存修改的实际买断价格
     * @return
     */
    @RequestMapping(value="/addOrderBudgetSettlementChange")
    @ResponseBody
    public Object addOrderBudgetSettlementChange(Model model,HttpServletRequest request,String priceModel){
        try{
        	String change_flag = "BUYOUT";
            if(!"BUDGET_UNIT_PRICE".equalsIgnoreCase(priceModel) && !"BUDGET_TOTAL_PRICE".equalsIgnoreCase(priceModel))
                return new ResultMessage(ResultMessage.ERROR,"价格模式错误！！！");

            boolean isAmount=false;
            OrdOrderItem  orderItem  = null;
            List<OrdSettlementPriceRecord> list=new ArrayList<OrdSettlementPriceRecord>();
            StringBuffer logContent=new StringBuffer();

            Long orderItemId = Long.parseLong(request.getParameter("orderItemId"));
            Long ordMulPriceRateListCount = Long.parseLong(request.getParameter("ordMulPriceRateListCount"));//>=1 ，表示是多价格类型
            String reason = request.getParameter("reason");//修改原因
            String remark = request.getParameter("remark");//备注
            String settlementPrice = request.getParameter("settlementPrice");// 修改后的总价格
            Long oldBuyoutTotalPrice = 0L;
            Long newBuyoutTotalPrice = 0L;
            

            if (!StringUtils.isEmpty(settlementPrice) && StringUtil.isNumberAmount(settlementPrice)) {
                isAmount = true;
            } else {
                isAmount = false;
            }

            if (!isAmount) {
                return new ResultMessage(ResultMessage.ERROR," 请至少填写一个为正数(或者2位小数)金额");
            }

            // 查询子订单
            orderItem = this.orderUpdateService.getOrderItem(orderItemId);
            oldBuyoutTotalPrice = orderItem.getBuyoutTotalPrice();
            oldBuyoutTotalPrice = oldBuyoutTotalPrice==null?0L:oldBuyoutTotalPrice;
            //得到新订单新的结算价
            OrdSettlementPriceRecord newOrdSettlementPriceRecord = getNewOrdSettlementPriceRecord(priceModel,orderItem,reason,remark,PriceUtil.convertToFen(Float.parseFloat(settlementPrice)));

            list.add(newOrdSettlementPriceRecord);

            if(newOrdSettlementPriceRecord.getOldTotalSettlementPrice() != null){
                logContent.append("原非买断结算总价：").append(newOrdSettlementPriceRecord.getOldTotalSettlementPrice() / 100.0);
            }
            if(newOrdSettlementPriceRecord.getOldActualSettlementPrice() != null){
                logContent.append("原非买断结算单价：").append(newOrdSettlementPriceRecord.getOldActualSettlementPrice()/100.0);
            }
            if(newOrdSettlementPriceRecord.getOldBudgetTotalSettlementPrice() != null){
                logContent.append("原买断结算总价：").append(newOrdSettlementPriceRecord.getOldBudgetTotalSettlementPrice()/100.0);
            }
            if(newOrdSettlementPriceRecord.getOldBudgetUnitSettlementPrice() != null){
                logContent.append("原买断结算单价：").append(newOrdSettlementPriceRecord.getOldBudgetUnitSettlementPrice()/100.0);
            }
            if(newOrdSettlementPriceRecord.getNewTotalSettlementPrice() != null){
                logContent.append("新非买断结算总价：").append(newOrdSettlementPriceRecord.getNewTotalSettlementPrice() / 100.0);
            }
            if(newOrdSettlementPriceRecord.getNewActualSettlementPrice() != null){
                logContent.append("新非买断结算单价：").append(newOrdSettlementPriceRecord.getNewActualSettlementPrice()/100.0);
            }
            if(newOrdSettlementPriceRecord.getNewBudgetTotalSettlementPrice() != null){
                logContent.append("新买断结算总价：").append(newOrdSettlementPriceRecord.getNewBudgetTotalSettlementPrice()/100.0);
            }
            if(newOrdSettlementPriceRecord.getNewBudgetUnitSettlementPrice() != null){
                logContent.append("新买断结算单价：").append(newOrdSettlementPriceRecord.getNewBudgetUnitSettlementPrice()/100.0);
            }


            //处理结算总价的记录
            String changeResult = OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.UP.getCode();
            OrdSettlementPriceRecord record = list.get(0);
            Long newBudgetTotalSettlementPrice = record.getNewBudgetTotalSettlementPrice()!=null?record.getNewBudgetTotalSettlementPrice():0;
            Long newTotalSettlementPrice = record.getNewTotalSettlementPrice()!=null?record.getNewTotalSettlementPrice():0;
            
            Long oldTotalSettlementPrice = record.getOldTotalSettlementPrice()!=null?record.getOldTotalSettlementPrice():0;
            Long oldBudgetTotalSettlementPrice = record.getOldBudgetTotalSettlementPrice()!=null?record.getOldBudgetTotalSettlementPrice():0;
            
            if ((newBudgetTotalSettlementPrice+newTotalSettlementPrice - oldTotalSettlementPrice-oldBudgetTotalSettlementPrice) < 0) {
                changeResult=OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.getCode();
            }
            record.setChangeResult(changeResult);
            //订单结算状态校验
            ResultMessage resultMessage = this.validateOrderSettlementStatus(orderItem);
            //保存结算记录和价格变更记录
            this.ordSettlementPriceRecordService.addSettlementTotalPrice(list, orderItem.getOrderId());

            //修改子订单
            ResultHandle resultHandle = orderLocalService.updateOrderItem(orderItem);
            if (resultHandle.isSuccess()) {//子订单修改成功再去推送结算价及推送变价单
                //修改多价格记录
                if (resultMessage.isSuccess()) {//如果是结算完成的订单则不推送结算也不更改订单数据，只记录修改历史和生成变价单
                	
                	newBuyoutTotalPrice = orderItem.getBuyoutTotalPrice();
                    newBuyoutTotalPrice = newBuyoutTotalPrice==null?0L:newBuyoutTotalPrice;
                    if(newBuyoutTotalPrice.longValue() != oldBuyoutTotalPrice.longValue()){
                    	Date visitDate = orderItem.getVisitTime();
                    	Long goodsId = orderItem.getSuppGoodsId();
                    	GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
                    	long p = Math.min(oldBuyoutTotalPrice, goodsResPrecontrolPolicyVO.getAmount()) - newBuyoutTotalPrice;
                    	if(goodsResPrecontrolPolicyVO!=null && ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equals(goodsResPrecontrolPolicyVO.getControlType())){
                    		Long amountId = goodsResPrecontrolPolicyVO.getAmountId();
                    		Long controlId = goodsResPrecontrolPolicyVO.getId();
                    		Long leftAmount = goodsResPrecontrolPolicyVO.getLeftAmount();
                    		LOG.info(goodsId+"差价" + p);
                    		LOG.info(goodsId+"剩余金额" + leftAmount);
                    		Long leftValue = leftAmount+p;
                    		leftValue = leftValue< 0? 0L:leftValue;
                    		leftValue = leftValue> goodsResPrecontrolPolicyVO.getAmount()? goodsResPrecontrolPolicyVO.getAmount():leftValue;
                    		resControlBudgetRemote.updateAmountResPrecontrolPolicy(amountId,controlId, visitDate, leftValue);
                    		if(leftValue == 0L){
                    			resControlBudgetRemote.handleResPrecontrolSaledOut(goodsResPrecontrolPolicyVO, orderItem.getVisitTime(), goodsId);
                    		}
                    	}
                    	
                    }
                    
                    record.setChangeRemark("0");//结算前修改
                    //推送结算
                    sendOrdSettlementPriceChangeMsg(record.getOrderItemId());
                } else {
                    record.setChangeRemark("1");//结算后修改
                }
                
                record.setChangeFlag(change_flag);
                
                //调用结算系统生成结算单
                orderSettlementService.insertRecord(record);
            }

            lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
                    orderItem.getOrderId(),
                    orderItemId,
                    getLoginUserId(),
                    "将编号为["+orderItem.getOrderId()+"]的子订单，修改子订单结算总价，修改值："+logContent.toString(),
                    ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(),
                    ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName()+"：发起订单结算价价格修改申请","");

            String info="修改申请已经提交。由于下述原因，需财务审核：a.子订单结算中 b.子订单已结算  c.有负毛利的风险";

            if (resultMessage.isSuccess()) {
                info = "结算价修改成功";
            } else {
                info = "该订单已经结算过！";
            }

            return new ResultMessage(ResultMessage.SUCCESS,info);

        }catch(Exception e){
            LOG.error(ExceptionUtil.getExceptionDetails(e));
        }
        return new ResultMessage(ResultMessage.ERROR," 操作失败，系统内部异常");
    }

	/**
	 * 保存外币结算单价
	 * @return
	 */
	@RequestMapping(value="/addCurrencySettlementChange")
	@ResponseBody
	public Object addCurrencySettlementChange(Model model,OrdSettlementPriceRecord request,HttpServletRequest re){
		try {
			Long orderId = request.getOrderId();
			Long orderItemId = request.getOrderItemId();
			StringBuffer logContent = new StringBuffer();
			String info = null;

			//页面填写外币单价参数
			String newActPrice = re.getParameter("newActPrice");

            if (newActPrice == null || !StringUtil.isNumberAmount(newActPrice)) {
                return new ResultMessage(ResultMessage.ERROR," 请至少填写一个为正数(或者2位小数)金额");
            }

			//查询主订单
			OrdOrder order = iOrdOrderService.findByOrderId(orderId);
			//查询子订单
			OrdOrderItem orderItem = this.orderUpdateService.getOrderItem(orderItemId);
			//查询子单外币记录
			OrdOrderItemExtend orderItemExtend = ordOrderItemExtendService.selectByPrimaryKey(orderItemId);
			//外币结算单价转化分
			Long newActualSettlementPrice = PriceUtil.convertToFen(newActPrice);
			//外币结算单价计算总价转化分
			Long newTotalSettlementPrice = newActualSettlementPrice * orderItem.getQuantity();
			orderItemExtend.setForeignActualSettlementPrice(newActualSettlementPrice);
			orderItemExtend.setForeignActTotalSettlePrice(newTotalSettlementPrice);

			if (orderItemExtend != null && !"CNY".equalsIgnoreCase(orderItemExtend.getCurrencyCode())) {
				//汇率
				BigDecimal rate = orderItemExtend.getSettlementPriceRate();
				if (rate == null) {
					return new ResultMessage(ResultMessage.ERROR,"外币记录数据汇率为空，请联系技术人员");
				}

				OrdSettlementPriceRecord record = new OrdSettlementPriceRecord();
				record.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.UNIT_PRICE.getCode());
				record.setOperator(this.getLoginUserId());
				record.setCreateTime(new Date());
				record.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
				record.setOrderId(orderId);
				record.setOrderItemId(orderItemId);
				record.setSuppGoodsId(orderItem.getSuppGoodsId());
				record.setVisitTime(orderItem.getVisitTime());
				record.setIsApprove("Y");
				record.setReason(request.getReason());
				record.setRemark(request.getRemark());
				record.setSupplierId(orderItem.getSupplierId());
				//修改之前的结算单价
				record.setOldActualSettlementPrice(orderItem.getActualSettlementPrice());
				//修改之前的结算总价
				record.setOldTotalSettlementPrice(orderItem.getTotalSettlementPrice());
				//修改之后的结算单价
				record.setNewActualSettlementPrice(PriceUtil.toCNY(newActualSettlementPrice,rate));
				//修改之后的结算总价
				record.setNewTotalSettlementPrice(PriceUtil.toCNY(newTotalSettlementPrice,rate));
				//修改价格类型
				record.setPriceType("PRICE");

				if ( (record.getNewTotalSettlementPrice() - record.getOldTotalSettlementPrice()) < 0) {
					record.setChangeResult(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.getCode());
				} else {
					record.setChangeResult(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.UP.getCode());
				}

				//订单结算状态校验
				ResultMessage resultMessage = this.validateOrderSettlementStatus(orderItem);
				if (resultMessage.isSuccess()) {
					//没有结算过的
					record.setChangeRemark("0");
				} else {
					//结算过的
					record.setChangeRemark("1");
				}

				int result = ordSettlementPriceRecordService.saveForeignAfterApprove(record,orderItemExtend);
				//订单修改成功的才做推送结算和生成变价单
				if (result == 1) {
					//订单修改成功并且没有结算完成的，推送结算
					if (resultMessage.isSuccess()) {
						LOG.info("currency sendOrdSettlementPriceChangeMsg1 orderItemId=" + orderItemId);
						sendOrdSettlementPriceChangeMsg(record.getOrderItemId());
					}
					//调用结算系统生成结算单
					LOG.info("currency order_item_id=" + record.getOrderItemId() + " vst_record_id=" + record.getRecordId());
					record.setChangeFlag("Normal");
					if (order.hasPayed() && order.isPayToLvmama() && order.hasInfoAndResourcePass()) {
						LOG.info("保存外币变价信息开始");
						orderSettlementService.insertRecord(record);
					}else{
						LOG.info( "currency order:" + order.getOrderId() + " isPayToLvmama:" + order.isPayToLvmama() + " hasPayed status:" + order.hasPayed() + " order status:" + order.getOrderStatus() + ", don't need to settlement!");
					}

				}

                if (resultMessage.isSuccess()) {
                    info="外币结算单价修改成功";
                } else {
                    info = "该订单已经结算过";
                }

				logContent.append("原外币结算单价：").append(record.getOldActualSettlementPrice()/100.0).append("新外币结算单价：").append(record.getNewActualSettlementPrice()/100.0);
			}

			updateOrdItemPriceConfirm(request.getOrderItemId());

			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
					orderId,
					orderItemId,
					getLoginUserId(),
					"将编号为["+orderItemId+"]的子订单，修改子订单外币结算单价，修改值："+logContent.toString(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName()+"：发起订单外币结算单价价格修改申请","");



			return new ResultMessage(ResultMessage.SUCCESS,info);
		}catch(Exception e){
			LOG.error("保存外币结算单价失败：" + e.getMessage(),e);
			return new ResultMessage(ResultMessage.ERROR," 操作失败，系统内部异常");
		}
	}

	/**
	 * 保存外币结算总价
	 * @return
	 */
	@RequestMapping(value="/addCurrencyTotalSettlementChange")
	@ResponseBody
	public Object addCurrencyTotalSettlementChange(Model model,OrdSettlementPriceRecord request,HttpServletRequest re){
		try {
            Long orderId = request.getOrderId();
            Long orderItemId = request.getOrderItemId();
            StringBuffer logContent = new StringBuffer();
            String info = null;

            //页面填写外币总价参数
			String newTotalPrice = re.getParameter("newTotalPrice");

            if (newTotalPrice == null || !StringUtil.isNumberAmount(newTotalPrice)) {
                return new ResultMessage(ResultMessage.ERROR," 请至少填写一个为正数(或者2位小数)金额");
            }

            //查询主订单
            OrdOrder order=iOrdOrderService.findByOrderId(orderId);
            //查询子订单
            OrdOrderItem orderItem = this.orderUpdateService.getOrderItem(orderItemId);
            //查询子单外币记录
            OrdOrderItemExtend orderItemExtend = ordOrderItemExtendService.selectByPrimaryKey(orderItemId);
			//页面填写外币结算总价
			Long newTotalSettlementPrice = PriceUtil.convertToFen(newTotalPrice);

			//设置外币结算单价与总价
			orderItemExtend.setForeignActualSettlementPrice(BigDecimal.valueOf(newTotalSettlementPrice).
					divide(BigDecimal.valueOf(orderItem.getQuantity()), 0, BigDecimal.ROUND_UP).longValue());
			orderItemExtend.setForeignActTotalSettlePrice(newTotalSettlementPrice);

            if (orderItemExtend != null) {
				//汇率
				BigDecimal rate = orderItemExtend.getSettlementPriceRate();

                OrdSettlementPriceRecord record = new OrdSettlementPriceRecord();
                record.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.TOTAL_PRICE.getCode());
                record.setOperator(this.getLoginUserId());
                record.setCreateTime(new Date());
                record.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
                record.setOrderId(orderId);
                record.setOrderItemId(orderItemId);
                record.setSuppGoodsId(orderItem.getSuppGoodsId());
                record.setVisitTime(orderItem.getVisitTime());
                record.setIsApprove("Y");
                record.setReason(request.getReason());
                record.setRemark(request.getRemark());
                record.setSupplierId(orderItem.getSupplierId());
				record.setApproveRemark("总价无需审核");
				//修改之前的结算单价
				record.setOldActualSettlementPrice(orderItem.getActualSettlementPrice());
				//修改之前的结算总价
				record.setOldTotalSettlementPrice(orderItem.getTotalSettlementPrice());
				//修改之后的结算单价
				record.setNewActualSettlementPrice(PriceUtil.toCNY(orderItemExtend.getForeignActualSettlementPrice(),rate));
				//修改之后的结算总价
				record.setNewTotalSettlementPrice(PriceUtil.toCNY(orderItemExtend.getForeignActTotalSettlePrice(),rate));
                //修改价格类型
                record.setPriceType("PRICE");

                if ( (record.getNewTotalSettlementPrice() - record.getOldTotalSettlementPrice()) < 0) {
                    record.setChangeResult(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.DOWN.getCode());
                } else {
                    record.setChangeResult(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_RESULT.UP.getCode());
                }

                //订单结算状态校验
                ResultMessage resultMessage = this.validateOrderSettlementStatus(orderItem);
                if (resultMessage.isSuccess()) {
                    //没有结算过的
                    record.setChangeRemark("0");
                } else {
                    //结算过的
                    record.setChangeRemark("1");
                }

                int result = ordSettlementPriceRecordService.saveForeignAfterApprove(record,orderItemExtend);
                //订单修改成功的才做推送结算和生成变价单
                if (result == 1) {
                    //订单修改成功并且没有结算完成的，推送结算
                    if (resultMessage.isSuccess()) {
                        LOG.info("currency total sendOrdSettlementPriceChangeMsg1 orderItemId=" + orderItemId);
                        sendOrdSettlementPriceChangeMsg(record.getOrderItemId());
                    }
                    //调用结算系统生成结算单
                    LOG.info("currency total order_item_id=" + record.getOrderItemId() + " vst_record_id=" + record.getRecordId());
                    record.setChangeFlag("Normal");
                    if (order.hasPayed() && order.isPayToLvmama() && order.hasInfoAndResourcePass()) {
                        LOG.info("保存外币变价信息开始");
                        orderSettlementService.insertRecord(record);
                    }else{
                        LOG.info( "currency total order:" + order.getOrderId() + " isPayToLvmama:" + order.isPayToLvmama() + " hasPayed status:" + order.hasPayed() + " order status:" + order.getOrderStatus() + ", don't need to settlement!");
                    }

                }

                if (resultMessage.isSuccess()) {
                    info="外币结算总价修改成功";
                } else {
                    info = "该订单已经结算过";
                }

                logContent.append("原外币结算总价：").append(record.getOldTotalSettlementPrice()/100.0).append("新外币结算总价：").append(record.getNewTotalSettlementPrice()/100.0);
            }

            updateOrdItemPriceConfirm(request.getOrderItemId());

            lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
                    orderId,
                    orderItemId,
                    getLoginUserId(),
                    "将编号为["+orderItemId+"]的子订单，修改子订单外币结算总价，修改值："+logContent.toString(),
                    ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.name(),
                    ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_AMOUNT_CHANGE.getCnName()+"：发起订单结算总价价格修改申请","");



            return new ResultMessage(ResultMessage.SUCCESS,info);
		}catch(Exception e){
			LOG.error("保存外币结算总价失败：" + e.getMessage());
			return new ResultMessage(ResultMessage.ERROR," 操作失败，系统内部异常");
		}

	}

    /**
     * 得到新的结算价
     * @param priceModel
     * @param orderItem
     * @param reason
     * @param remark
     * @param settlementPrice
     * @return
     */
    private OrdSettlementPriceRecord getNewOrdSettlementPriceRecord(String priceModel,OrdOrderItem orderItem,String reason,String remark,Long settlementPrice){
        OrdSettlementPriceRecord newOrdSettlementPriceRecord = new OrdSettlementPriceRecord();

        if("BUDGET_UNIT_PRICE".equalsIgnoreCase(priceModel))
            newOrdSettlementPriceRecord.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.BUDGET_UNIT_PRICE.getCode());
        else
            newOrdSettlementPriceRecord.setChangeType(OrderEnum.ORD_SETTLEMENT_PRICE_CHANGE_TYPE.BUDGET_TOTAL_PRICE.getCode());
        newOrdSettlementPriceRecord.setOperator(this.getLoginUserId());
        newOrdSettlementPriceRecord.setCreateTime(new Date());
        newOrdSettlementPriceRecord.setStatus(OrdAmountChange.APPROVESTATUS.APPROVE_PASSED.name());
        newOrdSettlementPriceRecord.setOrderId(orderItem.getOrderId());
        newOrdSettlementPriceRecord.setOrderItemId(orderItem.getOrderItemId());
        newOrdSettlementPriceRecord.setSuppGoodsId(orderItem.getSuppGoodsId());
        newOrdSettlementPriceRecord.setVisitTime(orderItem.getVisitTime());
        newOrdSettlementPriceRecord.setIsApprove("Y");
        newOrdSettlementPriceRecord.setApproveRemark("总价无需审核");
        newOrdSettlementPriceRecord.setReason(reason);
        newOrdSettlementPriceRecord.setRemark(remark);
        newOrdSettlementPriceRecord.setSupplierId(orderItem.getSupplierId());

        //修改之前买断单价
        newOrdSettlementPriceRecord.setOldBudgetUnitSettlementPrice(orderItem.getBuyoutPrice());
        //修改之前买断总价
        newOrdSettlementPriceRecord.setOldBudgetTotalSettlementPrice(orderItem.getBuyoutTotalPrice());

        if(orderItem.getQuantity()-orderItem.getBuyoutQuantity() != 0){
            //修改之前的非买断总价
            newOrdSettlementPriceRecord.setOldTotalSettlementPrice(orderItem.getTotalSettlementPrice()-orderItem.getBuyoutTotalPrice());
            //修改之前的非买断单价
            newOrdSettlementPriceRecord.setOldActualSettlementPrice(newOrdSettlementPriceRecord.getOldTotalSettlementPrice()/(orderItem.getQuantity()-orderItem.getBuyoutQuantity()));
        }

        //设置子订单结算价
        setCalcSettlementPrice(priceModel,settlementPrice,orderItem);

        //修改之后的买断单价
        newOrdSettlementPriceRecord.setNewBudgetUnitSettlementPrice(orderItem.getBuyoutPrice());
        //修改之后的买断总价
        newOrdSettlementPriceRecord.setNewBudgetTotalSettlementPrice(orderItem.getBuyoutTotalPrice());

        if(orderItem.getQuantity()-orderItem.getBuyoutQuantity() != 0){
            //修改之后的非买断总价
            newOrdSettlementPriceRecord.setNewTotalSettlementPrice(orderItem.getTotalSettlementPrice()-orderItem.getBuyoutTotalPrice());
            //修改之后的非买断单价
            newOrdSettlementPriceRecord.setNewActualSettlementPrice(newOrdSettlementPriceRecord.getNewTotalSettlementPrice()/(orderItem.getQuantity()-orderItem.getBuyoutQuantity()));
        }

        //修改价格类型
        newOrdSettlementPriceRecord.setPriceType("PRICE");

        newOrdSettlementPriceRecord.setOperator(this.getLoginUserId());

        return newOrdSettlementPriceRecord;
    }

    //计算结算价
    private void setCalcSettlementPrice(String priceModel,Long settlementPrice,OrdOrderItem orderItem){
        if("BUDGET_UNIT_PRICE".equalsIgnoreCase(priceModel)){
            //修改买断单价
            //设置结算总价
            //先计算非买断的总价(总价-买断总价)
            Long unBudgetTotalPrice = orderItem.getTotalSettlementPrice()-orderItem.getBuyoutTotalPrice();
            LOG.info("修改单价非买断总价：" + unBudgetTotalPrice);
            //买断的总价
            Long budgetTotalPrice = settlementPrice*orderItem.getBuyoutQuantity();
            LOG.info("修改单价买断总价：" + budgetTotalPrice);
            //设置新的结算总价
            orderItem.setTotalSettlementPrice(unBudgetTotalPrice+budgetTotalPrice);
            //设置结算单价
            orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
            //设置买断结算单价
            orderItem.setBuyoutPrice(settlementPrice);
            //设置买断结算总价
            orderItem.setBuyoutTotalPrice(budgetTotalPrice);
        }else{
            //修改买断总价
            //设置新的结算总价
            //先计算非买断的总价(总价-买断总价)
            Long unBudgetTotalPrice = orderItem.getTotalSettlementPrice()-orderItem.getBuyoutTotalPrice();
            LOG.info("修改总价非买断总价：" + unBudgetTotalPrice);
            orderItem.setTotalSettlementPrice(unBudgetTotalPrice+settlementPrice);
            //设置结算单价
            orderItem.setActualSettlementPrice(orderItem.getTotalSettlementPrice()/orderItem.getQuantity());
            //设置买断结算总价
            orderItem.setBuyoutTotalPrice(settlementPrice);
            LOG.info("修改总价买断总价：" + orderItem.getBuyoutTotalPrice());
            //设置买断结算单价
            orderItem.setBuyoutPrice(settlementPrice/orderItem.getBuyoutQuantity());
        }
    }


	
	
	/**
     * 导出excel数据
     * @author 张伟
     */ 
	@RequestMapping(value = "/exportExcelData")
	public void exportExcelData(Model model, Integer page,Integer pageSize,OrderMonitorCnd monitorCnd, HttpServletRequest request, HttpServletResponse response) throws BusinessException {
			
		/*if (monitorCnd.getOrderId()==null) {
			this.sendAjaxMsg("订单id为空", request, response);
			return ;
		}*/
		
		List<OrdSettlementPriceRecordVO> resultList = new ArrayList<OrdSettlementPriceRecordVO>();
		
		
//		List<OrdOrder> orderList = getOrderListCondition(monitorCnd);
		
		Page<OrdSettlementPriceRecordVO> pageParam =null;
		resultList=this.getResultList(monitorCnd, page,pageParam,request);
		/*if (!CollectionUtils.isEmpty(orderList)) {
			
			resultList=this.getResultList(orderList, page,pageParam,request);
			
			
		}*/

		Map<String, Object> beans = new HashMap<String, Object>();
		beans.put("resultList", resultList);
//		beans.put("seoTypeName", BizSeoFriendLink.SEO_TYPE.getCnName(seoType));
		
		String destFileName = writeExcelByjXls(beans, SETTLEMENT_HISTORY_TEMPLATE_PATH);
		writeAttachment(destFileName, "orderSettlementHistoryExcel" + DateUtil.formatDate(new Date(), "yyyy MM dd"), response);
	
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

	//批量获取结算状态
	public List<SetSettlementItem> getSetSettlementItem(List<OrdOrderItem> orderItemList){
			List<SetSettlementItem> setSettlementItems  = new ArrayList<SetSettlementItem>();;
			try {
					List<Long> itemIds = new ArrayList<Long>();
						for (OrdOrderItem ordOrderItem : orderItemList) {
							itemIds.add(ordOrderItem.getOrderItemId());
						}
						setSettlementItems  = settlementService.searchSetSettlementItemByOrderItemIds(itemIds);
			} catch (Exception e) {
				throw new RuntimeException("调用支付接口获取结算状态异常---"+e.getMessage());
			}
		return setSettlementItems;
	}

			
	public String getSettlementStatus(List<SetSettlementItem> setSettlementItems,Long orderItemId){
		if(null!=setSettlementItems && setSettlementItems.size()>0){
			for(int i=0;i<setSettlementItems.size();i++){
				if(setSettlementItems.get(i).getOrderItemMetaId().equals(orderItemId)){
					return setSettlementItems.get(i).getSettlementStatus();
				}
			}
		}
			return OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.name();
	}	
	
	//获取结算状态
	public String getSetSettlementItemStatus(Long itemId){
			try {
				List<SetSettlementItem> setSettlementItems = new ArrayList<SetSettlementItem>();
					List<Long> itemIds = new ArrayList<Long>();
					itemIds.add(itemId);
					setSettlementItems  = settlementService.searchSetSettlementItemByOrderItemIds(itemIds);
					if(null!=setSettlementItems&&setSettlementItems.size()>0){
						return setSettlementItems.get(0).getSettlementStatus();
					}
			} catch (Exception e) {
				throw new RuntimeException("调用支付接口获取结算状态异常---"+e.getMessage());
			}
		return  OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.name();
	}
		
	/**
	 * 组装页面上想要的结果
	 * 
	 * @param orderList
	 * @return
	 */
	private List<OrderMonitorRst> buildQueryResult(List<OrdOrderItem> orderItemList,HttpServletRequest request) {
		List<OrderMonitorRst> resultList = new ArrayList<OrderMonitorRst>();
		//支付获取结算状态
		List<SetSettlementItem> setSettlementItems = getSetSettlementItem(orderItemList);
		
		
		for (OrdOrderItem orderItem : orderItemList) {
			OrderMonitorRst orderMonitorRst = new OrderMonitorRst();
			orderMonitorRst.setPriceConfirmStatus(orderItem.getPriceConfirmStatus());
			orderMonitorRst.setQuantity(orderItem.getQuantity());
			orderMonitorRst.setBuyoutQuantity(orderItem.getBuyoutQuantity());
			orderMonitorRst.setBuyoutTotalPrice(orderItem.getBuyoutTotalPrice());
			orderMonitorRst.setTotalSettlementPrice(orderItem.getTotalSettlementPrice());
			
			orderMonitorRst.setOrderId(orderItem.getOrderId());
			orderMonitorRst.setOrderItemId(orderItem.getOrderItemId());
			//orderMonitorRst.setSettlementStatus(OrderEnum.ORDER_SETTLEMENT_STATUS.SETTLEMENTED.getCnName(orderItem.getSettlementStatus()));
						
			
			orderMonitorRst.setSettlementStatus(OrderEnum.ORDER_SETTLEMENT_STATUS.SETTLEMENTED.getCnName(getSettlementStatus(setSettlementItems, orderItem.getOrderItemId())));
			

			//订单的原结算总价
			Long yuanOrderTotalSettlementPrice = (long) (orderItem.getSettlementPrice() * orderItem.getQuantity());
			orderMonitorRst.setYuanOrderTotalSettlementPrice(PriceUtil.trans2YuanStr(yuanOrderTotalSettlementPrice));
			
			SuppSupplier  suppSupplier= new SuppSupplier();
			ResultHandleT<SuppSupplier> resultHandleSuppSupplier = suppSupplierClientService.findSuppSupplierById(orderItem.getSupplierId());
			if (resultHandleSuppSupplier.isSuccess()) {
				 suppSupplier= resultHandleSuppSupplier.getReturnContent();
			} 
			if (suppSupplier!=null) {
				orderMonitorRst.setSupplierName(suppSupplier.getSupplierName());
			}
			orderMonitorRst.setSupplierId(orderItem.getSupplierId());
			
			Long productId = null;
			OrdOrderPack ordOrderPack = null;
			Map<String, Object> paramPack = new HashMap<String, Object>();
			paramPack.put("orderId", orderItem.getOrderId());//订单号
			List<OrdOrderPack> orderPackList=ordOrderPackService.findOrdOrderPackList(paramPack);
			if (!orderPackList.isEmpty()) {
				ordOrderPack=orderPackList.get(0);
				productId=ordOrderPack.getProductId();
			}else{
				productId=orderItem.getProductId();
			}
//			ProdProduct prodProduct=prodProductClientService.findProdProductById(productId, Boolean.TRUE, Boolean.TRUE);

			orderMonitorRst.setProductId(productId);
			orderMonitorRst.setProductName(orderItem.getProductName());
			orderMonitorRst.setSuppGoodsId(orderItem.getSuppGoodsId());
			orderMonitorRst.setSuppGoodsName(orderItem.getSuppGoodsName());
			orderMonitorRst.setVisitTime(this.buildVisitTime(orderItem));
			
			Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
			paramsMulPriceRate.put("orderItemId", orderItem.getOrderItemId()); 
//			paramsMulPriceRate.put("priceType",ORDER_PRICE_RATE_TYPE.PRICE_ADULT_12.getCode() ); 
			Long buyoutTotalPrice = orderItem.getBuyoutTotalPrice();
			buyoutTotalPrice = buyoutTotalPrice==null?0L:buyoutTotalPrice;
			Long actualBuyoutTotalPrice = 0L;
			Long actualNonBuyoutTotalPrice = 0L;
			List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
			if (!CollectionUtils.isEmpty(ordMulPriceRateList)) {
				if (ordMulPriceRateList.size()>0) {
					Map<String, Long> ordMulPriceRateMap = new HashMap<String, Long>();
					for (OrdMulPriceRate ordMulPriceRate : ordMulPriceRateList) {
						ordMulPriceRateMap.put(ordMulPriceRate.getPriceType(), ordMulPriceRate.getPrice());
					}
					
					/*String[] priceTypeArray = new String[] {
							ORDER_PRICE_RATE_TYPE.PRICE_ADULT_12.getCode(),
							ORDER_PRICE_RATE_TYPE.PRICE_ADULT_34.getCode(),
							ORDER_PRICE_RATE_TYPE.PRICE_CHILD_34.getCode(),
							ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCode(),
							ORDER_PRICE_RATE_TYPE.PRICE_CHILD.getCode(),
						};*/
					String[] priceTypeArray = new String[] {
							ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.getCode(),
							ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_34.getCode(),
							ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.getCode(),
							ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode(),
							ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode(),
							ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD_PRE.getCode(),
							ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.getCode() ,
							ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.getCode(),
							ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.getCode(), ORDER_PRICE_RATE_TYPE.SETTLEMENT_GAP.getCode() };
							
					StringBuffer priceTypeBuffer=new StringBuffer();
					StringBuffer buyCountBuffer=new StringBuffer();
					StringBuffer oldSettlementPriceBuffer=new StringBuffer();
					StringBuffer actualSettlementPriceBuffer=new StringBuffer();
					
					
					for (int i = 0; i < ordMulPriceRateList.size(); i++) {
						
						OrdMulPriceRate ordMulPriceRate=ordMulPriceRateList.get(i);
						if (!ArrayUtils.contains(priceTypeArray, ordMulPriceRate.getPriceType())) {
							continue;
						}
						
//						OrderMonitorRst orderMonitorRstObj = new OrderMonitorRst();
//						BeanUtils.copyProperties(orderMonitorRst, orderMonitorRstObj);
						
						if (priceTypeBuffer.length()>0) {
							priceTypeBuffer.append("</br>");
						}
						priceTypeBuffer.append(ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.getCnName(ordMulPriceRate.getPriceType()));
						
						buyCountBuffer.append(ordMulPriceRate.getQuantity().intValue());
						if (buyCountBuffer.length()>0) {
							buyCountBuffer.append("</br>");
						}
						
						Long settlementPrice = 0L;
//						Long actualSettlementPrice=0L;
//						Long actualTotalSettlementPrice=0L;
						if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.getCode().equals(ordMulPriceRate.getPriceType())) {
							settlementPrice=ordMulPriceRateMap.get(ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.getCode());
						}else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_34.getCode().equals(ordMulPriceRate.getPriceType())) {
							settlementPrice=ordMulPriceRateMap.get(ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_34.getCode());
						}else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.getCode().equals(ordMulPriceRate.getPriceType())) {
							settlementPrice=ordMulPriceRateMap.get(ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.getCode());
						}else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.getCode().equals(ordMulPriceRate.getPriceType())) {
							settlementPrice=ordMulPriceRateMap.get(ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.getCode());
						}else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.getCode().equals(ordMulPriceRate.getPriceType())) {
							settlementPrice=ordMulPriceRateMap.get(ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.getCode());
						}else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.getCode().equals(ordMulPriceRate.getPriceType())) {
							settlementPrice=ordMulPriceRateMap.get(ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.getCode());
						}else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_GAP.getCode().equals(ordMulPriceRate.getPriceType())) {
							settlementPrice=ordMulPriceRateMap.get(ORDER_PRICE_RATE_TYPE.SETTLEMENT_GAP.getCode());
						}
						else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode().equals(ordMulPriceRate.getPriceType())) {
							settlementPrice=ordMulPriceRateMap.get(ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode());
							actualBuyoutTotalPrice += settlementPrice * ordMulPriceRate.getQuantity();
						}else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode().equals(ordMulPriceRate.getPriceType())) {
							settlementPrice=ordMulPriceRateMap.get(ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode());
							actualBuyoutTotalPrice += settlementPrice * ordMulPriceRate.getQuantity();
						}else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD_PRE.getCode().equals(ordMulPriceRate.getPriceType())) {
							settlementPrice=ordMulPriceRateMap.get(ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD_PRE.getCode());
						}
						
						if(ordMulPriceRate.getPriceType().indexOf("_PRE")<0){
							actualNonBuyoutTotalPrice += settlementPrice * ordMulPriceRate.getQuantity();
						}
						
						if (actualSettlementPriceBuffer.length()>0) {
							actualSettlementPriceBuffer.append("</br>");
						}
						if (settlementPrice==null) {
							actualSettlementPriceBuffer.append(0);
						}else{
							actualSettlementPriceBuffer.append(PriceUtil.trans2YuanStr(settlementPrice));
						}
						
						
						if (oldSettlementPriceBuffer.length()>0) {
							oldSettlementPriceBuffer.append("</br>");
						}
						HashMap<String, Object> paramsRecord = new HashMap<String, Object>();
						paramsRecord.put("orderItemId", orderItem.getOrderItemId()); 
						paramsRecord.put("priceType", ordMulPriceRate.getPriceType());
						if("Y".equals(orderItem.getBuyoutFlag())){
							paramsRecord.put("buyoutRecord", "Y");
						}
						paramsRecord.put("_orderby", "ORD_SETTLEMENT_PRICE_RECORD.CREATE_TIME ASC");
						List<OrdSettlementPriceRecord> recordList = ordSettlementPriceRecordService.findOrdSettlementPriceRecordList(paramsRecord);
						
						if (CollectionUtils.isNotEmpty(recordList)) {
							OrdSettlementPriceRecord ordSettlementPriceRecord=recordList.get(0);
							if(ordMulPriceRate.getPriceType().contains("_PRE")){
								oldSettlementPriceBuffer.append(PriceUtil.trans2YuanStr(ordSettlementPriceRecord.getOldBudgetUnitSettlementPrice()));
							}else{
								oldSettlementPriceBuffer.append(PriceUtil.trans2YuanStr(ordSettlementPriceRecord.getOldActualSettlementPrice()));
							}
						}else{
							
							oldSettlementPriceBuffer.append(PriceUtil.trans2YuanStr(settlementPrice));
						}
						
						
					}
					
					
					
					orderMonitorRst.setPriceType(priceTypeBuffer.toString());
					//购买份数
					orderMonitorRst.setBuyItemCount(buyCountBuffer.toString());
					
//					orderMonitorRst.setSettlementPrice(actualSettlementPriceBuffer.toString());
					//修改之前单价
					orderMonitorRst.setOldSettlementPrice(oldSettlementPriceBuffer.toString());
					
					orderMonitorRst.setActualSettlementPrice(actualSettlementPriceBuffer.toString());
					orderMonitorRst.setIsMultPrice("true");
					
				}
				
				
			}else{
				orderMonitorRst.setPriceType("价格");
				orderMonitorRst.setBuyItemCount(this.buildBuyCount(orderItem)+"");
				
//				orderMonitorRst.setSettlementPrice(PriceUtil.trans2YuanStr(orderItem.getSettlementPrice())+"");
				
				orderMonitorRst.setOldSettlementPrice(PriceUtil.trans2YuanStr(orderItem.getSettlementPrice())+"");
				
				orderMonitorRst.setActualSettlementPrice(PriceUtil.trans2YuanStr(orderItem.getActualSettlementPrice())+"");
				actualNonBuyoutTotalPrice +=orderItem.getTotalSettlementPrice();
			}
			
			
			orderMonitorRst.setActualTotalSettlementPrice(PriceUtil.trans2YuanStr(orderItem.getTotalSettlementPrice() )+"");
			
			//查看子单外币结算
			if (orderItem.getOrdOrderItemExtend() != null) {
				OrdOrderItemExtend extend = orderItem.getOrdOrderItemExtend();
				//非人命币时走新的外币结算按钮
				if (!"CNY".equalsIgnoreCase(extend.getCurrencyCode())) {
					LOG.info("orderItem has extend,orderItemId:" + orderItem.getOrderItemId() + ",extend:" + JSONUtil.bean2Json(extend));
					orderMonitorRst.setCurrencyName(extend.getCurrencyName());
					orderMonitorRst.setIsCurrency("Y");
					orderMonitorRst.setOldSettlementPrice(PriceUtil.trans2YuanStr(extend.getForeignSettlementPrice()));
					orderMonitorRst.setActualSettlementPrice(PriceUtil.trans2YuanStr(extend.getForeignActualSettlementPrice()));
					orderMonitorRst.setYuanOrderTotalSettlementPrice(PriceUtil.trans2YuanStr(extend.getForeignTotalSettlementPrice()));
					orderMonitorRst.setActualTotalSettlementPrice(PriceUtil.trans2YuanStr(extend.getForeignActTotalSettlePrice()));
				}
			}

			orderMonitorRst.setIsBuyoutFlag(orderItem.getBuyoutFlag());
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
	private List<OrdSettlementPriceRecordVO> buildQueryRecordResult(List<OrdSettlementPriceRecord> recordList,HttpServletRequest request) {
		List<OrdSettlementPriceRecordVO> resultList = new ArrayList<OrdSettlementPriceRecordVO>();
		for (OrdSettlementPriceRecord record : recordList) {
			
			OrdOrderItem orderItem=this.orderUpdateService.getOrderItem(record.getOrderItemId());
			String pushReason="";
			String itemStatus =  getSetSettlementItemStatus(orderItem.getOrderItemId());

				if (ORDER_SETTLEMENT_STATUS.SETTLEMENTED.getCode().equals(itemStatus)
						|| ORDER_SETTLEMENT_STATUS.SETTLEMENTING.getCode()
								.equals(orderItem.getSettlementStatus())) {

					pushReason=ORDER_SETTLEMENT_STATUS.SETTLEMENTING.getCnName(itemStatus);
				}else if(orderItem.getPrice() * orderItem.getQuantity()
						- record.getNewTotalSettlementPrice() < 0){
					pushReason="负毛利";
							
				}
			
			
			OrdSettlementPriceRecordVO recordVO=new OrdSettlementPriceRecordVO();
			BeanUtils.copyProperties(record, recordVO);
			
			String operator=record.getOperator();
			if (!StringUtils.isEmpty(operator)) {
				PermUser permUser=permUserServiceAdapter.getPermUserByUserName(operator);
				if (permUser!=null) {
					operator+="(";
					operator+=permUser.getRealName();
					operator+=")";
				}
			}else{
				operator="";
			}
			
			String operatorApprove=record.getOperatorApprove();
			if (!StringUtils.isEmpty(operatorApprove)) {
				PermUser permUser=permUserServiceAdapter.getPermUserByUserName(operatorApprove.trim());
				if (permUser!=null) {
					operatorApprove+="(";
					operatorApprove+=permUser.getRealName();
					operatorApprove+=")";
				}
			}else{
				operatorApprove="";
			}
			
			
			String statusName=OrdAmountChange.APPROVESTATUS.TOAPPROVE.getCnName(record.getStatus());
			recordVO.setStatusName(statusName);
			recordVO.setOperator(operator);
			recordVO.setOperatorApprove(operatorApprove);
			recordVO.setPushReason(pushReason);
			recordVO.setOrderId(orderItem.getOrderId());
			recordVO.setReasonName(OrderEnum.ORDER_SETTLEMENT_PRICE_REASON.getCnName(record.getReason()));
			SuppSupplier  suppSupplier= new SuppSupplier();
			ResultHandleT<SuppSupplier> resultHandleSuppSupplier = suppSupplierClientService.findSuppSupplierById(orderItem.getSupplierId());
			if (resultHandleSuppSupplier.isSuccess()) {
				 suppSupplier= resultHandleSuppSupplier.getReturnContent();
			} 
			if (suppSupplier!=null) {
				recordVO.setSupplierName(suppSupplier.getSupplierName());
			}
			recordVO.setSupplierId(orderItem.getSupplierId());
			
			Long productId = null;
			OrdOrderPack ordOrderPack= null;
			Map<String, Object> paramPack = new HashMap<String, Object>();
			paramPack.put("orderId", orderItem.getOrderId());//订单号
			List<OrdOrderPack> orderPackList=ordOrderPackService.findOrdOrderPackList(paramPack);
			if (!orderPackList.isEmpty()) {
				ordOrderPack=orderPackList.get(0);
				productId=ordOrderPack.getProductId();
			}else{
				productId=orderItem.getProductId();
			}
//			ProdProduct prodProduct=prodProductClientService.findProdProductById(productId, Boolean.TRUE, Boolean.TRUE);

			recordVO.setProductId(productId);
			recordVO.setProductName(orderItem.getProductName());
			
			
//			recordVO.setSuppGoodsId(orderItem.getSuppGoodsId());
			recordVO.setSuppGoodsName(orderItem.getSuppGoodsName());
			recordVO.setVisitTimeStr(this.buildVisitTime(orderItem));
			
			String priceTypeName=OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.getCnName(record.getPriceType());
			if ("PRICE".equals(priceTypeName.trim())) {
				priceTypeName="价格";
			}
			recordVO.setPriceTypeName(priceTypeName);
			
			Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
			paramsMulPriceRate.put("orderItemId", record.getOrderItemId()); 
			paramsMulPriceRate.put("priceType",record.getPriceType()); 
			
			List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
			
			if (!CollectionUtils.isEmpty(ordMulPriceRateList)) {
				
				OrdMulPriceRate ordMulPriceRate=ordMulPriceRateList.get(0);
			
				recordVO.setBuyItemCount(ordMulPriceRate.getQuantity()+"");
			}else{
				recordVO.setBuyItemCount(this.buildBuyCount(orderItem)+"");
			}
			
			
			recordVO.setOldActualSettlementPriceYuan(PriceUtil.trans2YuanStr(recordVO.getOldActualSettlementPrice()));
			recordVO.setOldTotalSettlementPriceYuan(PriceUtil.trans2YuanStr(recordVO.getOldTotalSettlementPrice()));
			recordVO.setNewActualSettlementPriceYuan(PriceUtil.trans2YuanStr(recordVO.getNewActualSettlementPrice()));
			recordVO.setNewTotalSettlementPriceYuan(PriceUtil.trans2YuanStr(recordVO.getNewTotalSettlementPrice()));
			
			resultList.add(recordVO);
			
		}
		return resultList;
	}

	
	


	/**
	 * 构建订单购买商品数量
	 * 
	 * @param order
	 * @return
	 */
	private Integer buildBuyCount(OrdOrderItem orderItem ) {
		Integer buyCount = 0;
		if (null != orderItem) {
			buyCount = orderItem.getQuantity().intValue();
		}
		return buyCount;
	}

	/**
	 * 根据订单和订单子项一对多的关系构建多条游玩和入住时间
	 * 
	 * @param order
	 * @return
	 */
	private String buildVisitTime(OrdOrderItem orderItem) {
		String visitTime = "未知日期";
		if(null != orderItem){
			
			if (1L==orderItem.getCategoryId()) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("orderItemId", orderItem.getOrderItemId()); 
				List<OrdOrderHotelTimeRate> orderHotelTimeRate =ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateList(params);
				String firstDay = DateUtil.formatDate(orderItem.getVisitTime(), "yyyy-MM-dd");
				visitTime = firstDay;
				if(null != orderHotelTimeRate && orderHotelTimeRate.size()>0){
					String lastDay = DateUtil.formatDate(DateUtil.dsDay_Date(orderItem.getVisitTime(), orderHotelTimeRate.size()),"yyyy-MM-dd");
					//visitTime = firstDay+"/"+lastDay;
					visitTime +="<br>"+lastDay;
				}
			}else{
				
				visitTime=DateUtil.formatDate(orderItem.getVisitTime(), "yyyy-MM-dd");
			}
			
		}
		return visitTime;
	}

	/**
	 * 订单状态和是否有在审核记录
	 * @param order
	 * @param orderItemId
	 * @param priceTypes
	 * @return
	 */
	private ResultMessage validateAmountChange(OrdOrder order, Long orderItemId,String [] priceTypes) {
		//订单正常 未支付完成状态 才可以发起申请
		
		/*
		if (!order.isNormal() ) {
			String orderStatus=OrderEnum.ORDER_STATUS.CANCEL.getCnName(order.getOrderStatus());
			return new ResultMessage(ResultMessage.ERROR,"订单状态正常可申请。当前订单状态："+orderStatus);
		}*/
		//是否 有正在审核记录 
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", orderItemId); 
		
		/*String priceType="";
		if (priceTypes==null || priceTypes.length==0) {
			priceType="PRICE";
			params.put("priceType",priceType); 
		}else{
			params.put("priceTypeArray",checkPriceTypes); 
			
		}*/
		params.put("approveStatus", OrdAmountChange.APPROVESTATUS.TOAPPROVE.name());
		params.put("isApprove","N");
		
		int m=ordSettlementPriceRecordService.findOrdSettlementPriceRecordCounts(params);
		
		
		if( m>0 ){
			return new ResultMessage(ResultMessage.ERROR,"该子订单存在未审批的结算价修改申请。请联系财务，将该申请通过或取消");
		}
		
		
		return ResultMessage.CHECK_SUCCESS_RESULT;
	}
	
	/**
	 * 订单结算状态校验
	 * @param order
	 * @param orderItemId
	 * @param priceTypes
	 * @return
	 */
	private ResultMessage validateOrderSettlementStatus(OrdOrderItem orderItem) {

		String itemStatus=getSetSettlementItemStatus(orderItem.getOrderItemId());

		if(OrderEnum.ORDER_SETTLEMENT_STATUS.SETTLEMENTED.getCode().equals(itemStatus)){
			return new ResultMessage(ResultMessage.ERROR,"该子订单已经处于已结算状态不可进行此操作");
		}

		return ResultMessage.CHECK_SUCCESS_RESULT;
	}

	/**
	 * 获取当前日期的上几个月时间
	 * @param month
	 * @return String
	 */
	private String getPreviousMonth(int month) {
		Calendar nowCal = Calendar.getInstance();
		nowCal.add(Calendar.MONTH, -month);
		Date previousDate = nowCal.getTime();
		return DateUtil.formatDate(previousDate, "yyyy-MM-dd HH:mm:ss");
	}


	private Long saveSendOrderSettlementEmail(Long goodsId, Long orderItemId) {

		String contentText = "结算价修改  商品ID=" + goodsId + ",子订单号=" + orderItemId;

		EmailContent emailContent = new EmailContent();
		emailContent.setContentText(contentText);
		emailContent.setFromAddress("service@cs.lvmama.com");
		emailContent.setFromName("驴妈妈旅游网");
		emailContent.setSubject(contentText);
		emailContent.setToAddress("csmoney@lvmama.com");

		return vstEmailService.sendEmailDirect(emailContent);
	}
	
	
	private ResultMessage validateOrderStatus(OrdOrder order) {
		
		if (!order.isNormal()) {
			String orderStatus=OrderEnum.ORDER_STATUS.CANCEL.getCnName(order.getOrderStatus());
			return new ResultMessage(ResultMessage.ERROR,"订单状态正常才可操作。当前订单状态："+orderStatus);
		}
		return ResultMessage.CHECK_SUCCESS_RESULT;
	}
	@InitBinder
	public void initBinder(WebDataBinder binder){
		binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
	}



	/**
	 * 查询条件封装
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
		condition.getOrderContentParam().setSuppGoodstId(monitorCnd.getSuppGoodsId());
		if(StringUtils.isNotBlank(monitorCnd.getPriceConfirmStatus())){
			condition.getOrderContentParam().setPriceConfirmStatus(monitorCnd.getPriceConfirmStatus());
		}
		//组装订单标志类条件
		condition.getOrderFlagParam().setOrderTableFlag(true);//获得订单号
		condition.getOrderFlagParam().setOrderItemTableFlag(true);//获得产品名称
		condition.getOrderFlagParam().setOrderPersonTableFlag(true);//获得联系人
		condition.getOrderFlagParam().setOrderHotelTimeRateTableFlag(true);//获得离店时间
		condition.getOrderFlagParam().setOrderPageFlag(true);//需要分页
		condition.getOrderFlagParam().setOrdAdditionStatusTableFlag(true);
		condition.getOrderFlagParam().setOrdTravelContractTableFlag(true);
		condition.getOrderFlagParam().setOrderPackTableFlag(true);
		condition.getOrderIndentityParam().setOrderId(monitorCnd.getOrderId());
		condition.getOrderIndentityParam().setSupplierId(monitorCnd.getSupplierId());
		condition.getOrderIndentityParam().setOrderItemId(monitorCnd.getOrderItemId());
		//组装订单排序类条件
		condition.getOrderSortParams().add(OrderSortParam.CREATE_TIME_DESC);
		//组装订单时间类条件
		if(StringUtils.isNotBlank(monitorCnd.getCreateTimeBegin())){
			condition.getOrderTimeRangeParam().setCreateTimeBegin(DateUtil.toDate(monitorCnd.getCreateTimeBegin(), "yyyy-MM-dd HH:mm:ss"));
		}
		if(StringUtils.isNotBlank(monitorCnd.getCreateTimeEnd())){
			condition.getOrderTimeRangeParam().setCreateTimeEnd(DateUtil.toDate(monitorCnd.getCreateTimeEnd(), "yyyy-MM-dd HH:mm:ss"));
		}
		condition.getOrderTimeRangeParam().setPaymentTimeBegin(monitorCnd.getPaymentTimeBegin());
		condition.getOrderTimeRangeParam().setPaymentTimeEnd(monitorCnd.getPaymentTimeEnd());

		if(StringUtils.isNotBlank(monitorCnd.getStartVisitTime())) {
			condition.getOrderTimeRangeParam().setVisitTimeBegin(DateUtil.toDate(monitorCnd.getStartVisitTime(), "yyyy-MM-dd HH:mm:ss"));
		}
		if(StringUtils.isNotBlank(monitorCnd.getEndVisitTime())) {
			condition.getOrderTimeRangeParam().setVisitTimeEnd(DateUtil.toDate(monitorCnd.getEndVisitTime(), "yyyy-MM-dd HH:mm:ss"));
		}
		//组装订单分页类条件
		Integer currentPage = page == null ? 1 : page;
		Integer currentPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
		//计算出每页的rownum
		condition.getOrderPageIndexParam().setBeginIndex((currentPage-1)*currentPageSize+1);
		condition.getOrderPageIndexParam().setEndIndex(currentPage*currentPageSize);

		return condition;
	}
}
