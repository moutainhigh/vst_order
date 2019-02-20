package com.lvmama.vst.order.web;


import com.lvmama.comm.bee.po.distribution.DistributorInfo;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.comm.stamp.vo.PresaleEnum;
import com.lvmama.comm.stamp.vo.StampDeductionList;
import com.lvmama.comm.stamp.vo.StampOrderDetails;
import com.lvmama.comm.vst.VstOrderEnum;
import com.lvmama.comm.vst.vo.VstInvoiceAmountVo;
import com.lvmama.crm.enumerate.CsVipUserIdentityTypeEnum;
import com.lvmama.crm.enumerate.SrvMemberEnum;
import com.lvmama.crm.service.CsVipDubboService;
import com.lvmama.dest.api.utils.DynamicRouterUtils;
import com.lvmama.dest.api.vst.goods.po.HotelGoodsVstPo;
import com.lvmama.dest.api.vst.goods.service.IHotelGoodsQueryVstApiService;
import com.lvmama.dest.api.vst.goods.vo.HotelGoodsVstVo;
import com.lvmama.dest.api.vst.prod.vo.HotelProductBranchVstVo;
import com.lvmama.dest.api.vst.prod.vo.HotelProductPropVstVo;
import com.lvmama.dest.api.vst.prod.vo.HotelProductVstVo;
import com.lvmama.dest.dock.request.order.RequestSuppOrder;
import com.lvmama.dest.dock.service.interfaces.ApiOrderDetailClientService;
import com.lvmama.dest.dock.service.interfaces.ApiSuppOrderService;
import com.lvmama.finance.comm.finance.po.SetSettlementItem;
import com.lvmama.finance.comm.vst.service.SettlementService;
import com.lvmama.order.api.base.vo.RequestBody;
import com.lvmama.order.route.service.IOrder2RouteService;
import com.lvmama.order.service.api.comm.order.IApiOrderQueryService;
import com.lvmama.order.service.api.comm.order.IApiOrderTagService;
import com.lvmama.order.snap.api.IApiOrderSnapshotService;
import com.lvmama.order.snap.vo.SnapshotOrderVo;
import com.lvmama.order.vo.comm.OrderAttachmentVo;
import com.lvmama.order.vo.comm.OrderTagVo;
import com.lvmama.order.vo.comm.OrderVo;
import com.lvmama.order.vo.comm.status.OrderCertificateStatusUpdateVo;
import com.lvmama.order.vo.comm.status.OrderResourceStatusVo;
import com.lvmama.order.vo.comm.status.OrderStatusUpdateVo;
import com.lvmama.order.workorder.process.api.vstorder.IApiVstOrdComAuditProcessService;
import com.lvmama.vst.back.biz.po.BizBranch;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizDictDef;
import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.biz.po.BizDistrict.DISTRICT_TYPE;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.DictDefClientService;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.dist.service.DistributorClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceAdapterClientService;
import com.lvmama.vst.back.client.ord.service.OrderSendSMSService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductPropClientService;
import com.lvmama.vst.back.client.prom.service.BuyPresentClientService;
import com.lvmama.vst.back.client.pub.service.ComLogClientService;
import com.lvmama.vst.back.client.supp.service.SuppFaxClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.dist.po.Distributor;
import com.lvmama.vst.back.ebooking.vo.fax.EbkFaxVO;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsSaleRe;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.po.OrderEnum.INFO_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_AMOUNT_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_COMMON_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PRICE_RATE_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.RESOURCE_STATUS;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductProp;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.back.prod.po.PropValue;
import com.lvmama.vst.back.prom.po.OrdPayPromotion;
import com.lvmama.vst.back.prom.vo.PromBuyPresentCouponVo;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.back.supp.po.SuppFaxRule;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.*;
import com.lvmama.vst.comm.vo.Constant.BU_NAME;
import com.lvmama.vst.comm.vo.order.ComMessageVO;
import com.lvmama.vst.comm.vo.order.HotelTimeRateInfo;
import com.lvmama.vst.comm.vo.order.OrderHotelTimeRateInfo;
import com.lvmama.vst.comm.vo.order.OrderInvoiceInfoVst;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.ebooking.client.ebk.serivce.EbkCertifClientService;
import com.lvmama.vst.ebooking.client.ebk.serivce.EbkFaxTaskClientService;
import com.lvmama.vst.ebooking.client.ebk.serivce.EbkOrderClientService;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif.EBK_CERTIFICATE_CONFIRM_CHANNEL;
import com.lvmama.vst.ebooking.ebk.po.EbkCertifItem;
import com.lvmama.vst.order.dao.tag.TagEnum;
import com.lvmama.vst.order.po.OrderCallId;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.service.refund.adapter.OrderRefundProcesserAdapter;
import com.lvmama.vst.order.utils.ApportionUtil;
import com.lvmama.vst.order.utils.CallCenterUtils;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.utils.RestClient;
import com.lvmama.vst.order.web.service.OrderDetailApportionService;
import com.lvmama.vst.pet.adapter.FavorServiceAdapter;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;
import com.lvmama.vst.pet.adapter.TntDistributorServiceAdapter;
import com.lvmama.vst.pet.adapter.TntOrderQueryServiceAdapter;
import com.lvmama.vst.pet.adapter.refund.vo.OrderRefundConstant;
import com.lvmama.crm.outer.user.api.CrmUserLabelService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.lvmama.vst.back.biz.po.BizBranch;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.client.biz.service.BranchClientService;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductBranchClientService;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PLAY_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_TICKET_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_TRAV_DELAY_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_WIFI_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.PERFORM_STATUS_TYPE;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.back.supp.po.SuppSettlementEntities;
import com.lvmama.vst.comm.utils.order.OrderUtil;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import com.lvmama.order.service.api.ticket.IApiReviseDateFeeRelatedOrderService;

/**
 * 订单详情页面action
 * 
 * @author jszhangwei
 * @param <E>
 * 
 */
@Controller
@RequestMapping("/order/orderStatusManage")
public class OrderHotelDetailAction extends BaseActionSupport {

	private static final Log LOG = LogFactory.getLog(OrderHotelDetailAction.class);
	private static final Integer NORMAL_USER =9;
	
	@Autowired
	private IOrderStatusManageService orderStatusManageService;
	@Autowired
	private IComplexQueryService complexQueryService;
	@Autowired
	private FavorServiceAdapter favorService;
	@Autowired
	private SuppSupplierClientService suppSupplierClientService;
	@Autowired
	private IOrderUpdateService orderUpdateService;
	@Autowired
	protected SuppGoodsClientService suppGoodsClientService;
	@Autowired
	private PermUserServiceAdapter permUserServiceAdapter;
	@Autowired
	private DistrictClientService districtClientService;
	@Autowired
	private ProdProductClientService prodProductClientService;
	@Autowired
	private IOrderLocalService orderLocalService;
	@Autowired
	private OrderRefundProcesserAdapter orderRefundProcesserAdapter;
	// 注入分销商业务接口(订单来源、下单渠道)
	@Autowired
	private DistributorClientService distributorClientService;
	@Autowired
	private DictDefClientService dictDefClientService;
	@Autowired
	private OrdPayPromotionService ordPayPromotionService;
	@Autowired
	private LvmmLogClientService lvmmLogClientService;
	@Autowired
	private IOrderAuditService orderAuditService;
	@Autowired
	private IComMessageService comMessageService;
	@Autowired
	private IOrderAttachmentService orderAttachmentService;
	@Autowired
	private IOrdAuditConfigService  ordAuditConfigService;
	//短信发送业务接口
	@Autowired
	OrderSendSMSService orderSmsSendService;
	@Autowired
	private SuppFaxClientService suppFaxClientService;
	@Autowired
	private ApiOrderDetailClientService orderDetailClientService;
	@Autowired
	private ComLogClientService comLogClientService;
	@Autowired
	private EbkFaxTaskClientService ebkFaxTaskClientService;
	@Autowired
	private TntDistributorServiceAdapter tntDistributorServiceRemote;
	@Autowired
	private IOrderUpdateService ordOrderUpdateService;
	@Autowired
	private BuyPresentClientService buyPresentClientService;
	@Autowired
	private ProdProductPropClientService prodProductPropClientService;
    @Autowired
    private IOrderStockService orderStockService;
    @Autowired
    private ApiSuppOrderService suppOrderClientService;
	@Autowired
	private EbkCertifClientService ebkCertifClientService;
	@Autowired
	private OrdApplyInvoiceInfoService ordApplyInvoiceInfoService;
	@Autowired
	private SuppGoodsTimePriceAdapterClientService suppGoodsTimePriceAdapterClientService;
	@Autowired
    private OrderService orderService;
	//结算状态改造 从支付获取
	@Autowired
	private SettlementService settlementService;
	@Autowired
	private OrderDetailApportionService orderDetailApportionService;
	@Autowired
	private IOrderCallIdService orderCallIdService;	
	@Autowired(required=false)
	private CsVipDubboService csVipDubboService;
	@Autowired
	private IHotelGoodsQueryVstApiService hotelGoodsQueryVstApiService;
	@Autowired
	private EbkOrderClientService ebkOrderClientService;
	@Autowired
	private TntOrderQueryServiceAdapter tntOrderQueryServiceRemote;

	@Autowired
	private IOrderResponsibleService orderResponsibleService;
	@Autowired
	private IOrdMulPriceRateService ordMulPriceRateService;
	@Autowired
	private IOrdItemPersonRelationService ordItemPersonRelationService;


	@Autowired
	private CrmUserLabelService crmUserLabelService;

	@Resource(name="apiOrderTagService")
	private IApiOrderTagService apiOrderTagService;

	@Resource(name = "apiOrderSnapshotService")
	private IApiOrderSnapshotService apiOrderSnapshotService;
	
	@Autowired
	private ProdProductBranchClientService prodProductBranchClientService;
	@Autowired
	private CategoryClientService categoryClientService;
	@Autowired
	private IOrdAccInsDelayInfoService ordAccInsDelayInfoService;
	@Autowired
	private BranchClientService branchClientService;
	@Autowired
	private IApiReviseDateFeeRelatedOrderService apiReviseDateFeeRelatedOrderService;
	
	@Resource
    private IOrder2RouteService order2RouteService;
    @Resource
    private IApiOrderQueryService apiOrderQueryService;
    @Resource
	private IApiVstOrdComAuditProcessService apiVstOrdComAuditProcessService;
	
	@RequestMapping(value = "/showOrderHotelProductDetail") 
	public String showOrderProductDetail(Model model, HttpServletRequest request) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showOrderHotelProductDetail>");
		}
		String orderIdStr = getRequestParameter("orderId", request);
		Long orderId = NumberUtils.toLong(orderIdStr);
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		List<OrdOrderItem> ordItemsList = order.getOrderItemList();
		if(CollectionUtils.isEmpty(ordItemsList)) {
			return "/order/orderStatusManage/showOrderHotelProductDetail";
		}
		//单酒店的商品ID是一样的
		Long suppGoodsId = ordItemsList.get(0).getSuppGoodsId();
		HotelGoodsVstPo hotelGoodsVstPo = new HotelGoodsVstPo();
		hotelGoodsVstPo.setSuppGoodsId(suppGoodsId);
		hotelGoodsVstPo.setHasProp(true);
	    hotelGoodsVstPo.setHasPropValue(true);
		com.lvmama.dest.api.common.RequestBody<HotelGoodsVstPo> requestBody = 
				new com.lvmama.dest.api.common.RequestBody<HotelGoodsVstPo>();
		requestBody.setT(hotelGoodsVstPo);
		requestBody.setToken(Constant.DEST_BU_HOTEL_TOKEN);
		com.lvmama.dest.api.common.ResponseBody<HotelGoodsVstVo> responseBody = null;
		try {
			responseBody = hotelGoodsQueryVstApiService.findSuppGoodsByParam(requestBody);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		HotelGoodsVstVo hotelGoodsVstVo = responseBody.getT();
		
		//产品规格 加床、床型、无烟房、面积、楼层、窗户、描述、宽带
		StringBuffer hotelProductBranchInfo = new StringBuffer();
		if(null != hotelGoodsVstVo) {
			HotelProductBranchVstVo hotelProductBranchVstVo = hotelGoodsVstVo.getProdProductBranch();
			Map<String, Object> hotelProductBranchMap = 
					hotelProductBranchVstVo == null ? null:hotelProductBranchVstVo.getPropValue();
			if(null != hotelProductBranchMap){
				if(null != hotelProductBranchMap.get("add_bed_flag")){
					String add_bed_flag = getPropValue(hotelProductBranchMap, "add_bed_flag");
					if(StringUtil.isNotEmptyString(add_bed_flag)){
						hotelProductBranchInfo.append("加床：");
						hotelProductBranchInfo.append(add_bed_flag);
						hotelProductBranchInfo.append("；");
					}
				}
				if(null != hotelProductBranchMap.get("bed_type")){
					String bed_type = getPropValue(hotelProductBranchMap, "bed_type");
					if(StringUtil.isNotEmptyString(bed_type)){
						hotelProductBranchInfo.append("床型：");
						hotelProductBranchInfo.append(bed_type);
						hotelProductBranchInfo.append("；");
					}
				}
				if(null != hotelProductBranchMap.get("smokeless_room")){
					String smokeless_room = getPropValue(hotelProductBranchMap, "smokeless_room");
					if(StringUtil.isNotEmptyString(smokeless_room)){
						hotelProductBranchInfo.append("无烟房：");
						hotelProductBranchInfo.append(smokeless_room);
						hotelProductBranchInfo.append("；");
					}
				}
				if(!ObjectUtils.equals("", ObjectUtils.defaultIfNull(hotelProductBranchMap.get("area"), ""))){
					hotelProductBranchInfo.append("面积：");
					hotelProductBranchInfo.append(hotelProductBranchMap.get("area"));
					hotelProductBranchInfo.append("；");
				}
				if(!ObjectUtils.equals("", ObjectUtils.defaultIfNull(hotelProductBranchMap.get("floor"), ""))){
					hotelProductBranchInfo.append("楼层：");
					hotelProductBranchInfo.append(hotelProductBranchMap.get("floor"));
					hotelProductBranchInfo.append("；");
				}
				if(!ObjectUtils.equals("", ObjectUtils.defaultIfNull(hotelProductBranchMap.get("window"), ""))){
					String window = getPropValue(hotelProductBranchMap, "window");
					if(null != window){
						hotelProductBranchInfo.append("窗户：");
						hotelProductBranchInfo.append(window);
						hotelProductBranchInfo.append("；");
					}
				}
				if(!ObjectUtils.equals("", ObjectUtils.defaultIfNull(hotelProductBranchMap.get("branch_desc"), ""))){
					hotelProductBranchInfo.append("描述：");
					hotelProductBranchInfo.append(hotelProductBranchMap.get("branch_desc"));
					hotelProductBranchInfo.append("；");
				}
				if(!ObjectUtils.equals("", ObjectUtils.defaultIfNull(hotelProductBranchMap.get("internet"), ""))){
					String internet = getPropValue(hotelProductBranchMap, "internet");
					if(null != internet){
						hotelProductBranchInfo.append("宽带：");
						hotelProductBranchInfo.append(internet);
						hotelProductBranchInfo.append("；");
					}
				}
			}
		}
		model.addAttribute("hotelProductBranchInfo", hotelProductBranchInfo.toString());

		//基本信息 最早到店时间、最晚离店时间、外宾接待、宠物携带
		HotelProductVstVo hotelProductVstVo = hotelGoodsVstVo.getProdProduct();
		Map<String, Object> hotelProductMap = 
				hotelProductVstVo == null ? null : hotelProductVstVo.getPropValue();
		String hotelProductInfoStr = null;
		if(null != hotelProductMap){
			StringBuffer hotelProductInfo = new StringBuffer();
			if(null != hotelProductMap.get("earliest_arrive_time")){
				hotelProductInfo.append("最早到店时间：");
				hotelProductInfo.append(hotelProductMap.get("earliest_arrive_time"));
				hotelProductInfo.append("；");
			}
			if(null != hotelProductMap.get("latest_leave_time")){
				hotelProductInfo.append("最晚离店时间：");
				hotelProductInfo.append(hotelProductMap.get("latest_leave_time"));
				hotelProductInfo.append("；");
			}
			if(!ObjectUtils.equals("", ObjectUtils.defaultIfNull(hotelProductMap.get("foreign_flag"), ""))){
				hotelProductInfo.append("外宾接待：");
				if(ObjectUtils.equals("Y", hotelProductMap.get("foreign_flag"))){
					hotelProductInfo.append("是");
				}else{
					hotelProductInfo.append("否");
				}
				hotelProductInfo.append("；");
			}
			if(!ObjectUtils.equals("", ObjectUtils.defaultIfNull(hotelProductMap.get("pet_flag"), ""))){
				hotelProductInfo.append("宠物携带：");
				if(ObjectUtils.equals("Y", hotelProductMap.get("pet_flag"))){
					hotelProductInfo.append("是");
				}else{
					hotelProductInfo.append("否");
				}
				hotelProductInfo.append("；");
			}
			if(hotelProductInfo.length() > 1) {
				hotelProductInfoStr = hotelProductInfo.substring(0, hotelProductInfo.length() - 1);
			}
		}
		model.addAttribute("hotelProductInfo", hotelProductInfoStr);
		return "/order/orderStatusManage/showOrderHotelProductDetail";
	}
	/**
	 * 仅限查询酒店产品的规则属性做处理
	 * @param map
	 * @param key
	 * @return
	 */
	private String getPropValue(Map<String, Object> map, String key) {
		if(map == null){
			return null;
		}
		try {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) map.get(key);
			if(null == list || list.size() < 1) {
				return null;
			}
			if(list.get(0) instanceof com.lvmama.dest.comm.po.prod.PropValue) {
				com.lvmama.dest.comm.po.prod.PropValue propValue = (com.lvmama.dest.comm.po.prod.PropValue) list.get(0);
				return propValue.getName();
			}else{
				return null;
			}
		} catch (Exception e) {
			LOG.info("获取产品信息出现异常", e);
			return null;
		}
		
	}
    /**************
	 * 获取券订单信息
	 * @param ordOrder
	 * @return
	 */
	public OrdOrder getStampOrdOrder(OrdOrder ordOrder,Model model) {
		try{
		     StampOrderDetails stampOrderDetails= new StampOrderDetails();
		     LOG.info("------------------11------------------");
		     String url = Constant.getInstance().getPreSaleBaseUrl() + "/customer/stamp/order/"+ordOrder.getOrderId();
		     if(!"".equals(ordOrder.getOrderId())){
			     stampOrderDetails =RestClient.getClient().getForObject(url, StampOrderDetails.class);
			     ordOrder.setStampId(stampOrderDetails.getStamp().getId());
			     ordOrder.setOrderProductName(stampOrderDetails.getStamp().getName());
				 ordOrder.setCreateTime(stampOrderDetails.getOrderDate());
				 ordOrder.setRealCancelStrategy(stampOrderDetails.getStamp().getRuleRestrict().toString());
				 if(stampOrderDetails.getStamp().getProductManagerId()!=null&&!"".equals(stampOrderDetails.getStamp().getProductManagerId())){
					 ordOrder.setManagerId(Long.valueOf(stampOrderDetails.getStamp().getProductManagerId().toString()));
				 }else{
					 ordOrder.setManagerId(0L);
				 }
				 ordOrder.setWaitRetainageTime(stampOrderDetails.getBalanceDueWaitDate());
				 ordOrder.setStampPayType(stampOrderDetails.getPayType());
				 ordOrder.setBoundProduct(stampOrderDetails.getStamp().getBoundMerchant().getProductName()+"("+stampOrderDetails.getStamp().getBoundMerchant().getProductId()+")");
				 String subsidyAmount = stampOrderDetails.getSubsidyAmount()==null?"":stampOrderDetails.getSubsidyAmount().toString(); 
				 if(StringUtils.isEmpty(subsidyAmount)){
				     model.addAttribute("subsidyAmount", subsidyAmount);
				 }else{
				     model.addAttribute("subsidyAmount", PriceUtil.trans2YuanStr(stampOrderDetails.getSubsidyAmount())+"元");
				 }
				 if(null!=stampOrderDetails.getStamp()&&null!=stampOrderDetails.getStamp().getAssociatedProdAvailTime()){
						List<Date> listDates=stampOrderDetails.getStamp().getAssociatedProdAvailTime();
					    List<Date> list=new ArrayList<Date>();
						if(listDates.size()>0){
							list.add(listDates.get(0));
							list.add(listDates.get(listDates.size()-1));
							model.addAttribute("listDates",list);
							}
					 } 
		     }
		     return ordOrder;
	   } catch (Exception e) {
		LOG.error(e.getMessage());
		return ordOrder;
	  }
	}
	
	public List<StampDeductionList> getStampDeductionList(String orderId)throws Exception{
   	 List<StampDeductionList> list=new ArrayList<StampDeductionList>();
   	 LOG.info("------------------13------------------");
   	 String url = Constant.getInstance().getPreSaleBaseUrl() + "/customer/stamp/getStampCodeOrder?orderId="+orderId+"";
   	 try{
   		 if(orderId!=null){
   				JSONArray arr = RestClient.getClient().getForObject(url,JSONArray.class);
   				for(int i=0;i<arr.size();i++){
   				 JSONObject obj = arr.getJSONObject(i);
   				 if(obj.get("stampCodes")!=null){
   					 JSONArray jsonArray=obj.getJSONArray("stampCodes");
   					 JSONObject stampObj=obj.getJSONObject("stamp");
   					 String name=stampObj.getString("name");
   					 for(int j=0;j<jsonArray.size();j++){
   						 String useOrderId=jsonArray.getJSONObject(j).getString("useOrderId");
   						 String stampStatus=jsonArray.getJSONObject(j).getString("stampStatus");
   						 StampDeductionList deductionList=new StampDeductionList();
   						 if(orderId.equals(useOrderId)&&PresaleEnum.StampStatus.USED.getCode().equals(stampStatus)){
   							 deductionList.setId(stampObj.getString("stampNo"));
   							 deductionList.setOrderId(jsonArray.getJSONObject(j).getString("orderId"));
   							 if(null!=jsonArray.getJSONObject(j).getString("orderId")&&!"".equals(jsonArray.getJSONObject(j).getString("orderId"))){
   								 OrdOrder order = complexQueryService.queryOrderByOrderId(Long.valueOf(jsonArray.getJSONObject(j).getString("orderId")));
   								 deductionList.setOrderStatus(OrderEnum.ORDER_STATUS.getCnName(order.getOrderStatus()));
   							 }
   							 deductionList.setSerialNumber(jsonArray.getJSONObject(j).getString("serialNumber"));
   							 deductionList.setPrice(jsonArray.getJSONObject(j).getInt("price"));
   							 deductionList.setStampName(name);
   							 list.add(deductionList);
   						}
   					}
   				}
   			}
   		}	 
   	    return list; 
   	 }catch(Exception e){
   		 LOG.error("{}", e);
   		    throw new Exception(e.getMessage()); 
   	 }
   	
   }
	
	//获取结算状态
	public String getSetSettlementItemStatus(Long itemId){
		    try {
				List<SetSettlementItem> setSettlementItems = new ArrayList<SetSettlementItem>();
				List<Long> itemIds = new ArrayList<Long>();
				itemIds.add(itemId);
				setSettlementItems  = settlementService.searchSetSettlementItemByOrderItemIds(itemIds);
				if(null!=setSettlementItems && setSettlementItems.size()>0){
					if(null!=setSettlementItems&&setSettlementItems.size()>0){
						return setSettlementItems.get(0).getSettlementStatus();
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("调用支付接口获取结算状态异常---"+e.getMessage());
			}
		return  OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.name();
	}
		
	@RequestMapping(value = "/showOrderStatusManage")
	public String showOrderStatusManage(Model model, HttpServletRequest request)
			 {
		try{

			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<showOrderStatusManage>");
			}
			String loginUserId=this.getLoginUserId();
			model.addAttribute("loginUserId", loginUserId);
			String orderIdStr = getRequestParameter("orderId", request);
			Long orderId=NumberUtils.toLong(orderIdStr);

			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);

			model.addAttribute("order", order);
			//获取该用户是否为超级会员
			boolean userSuperVip = false;
			try {
				Long id = order.getUserNo();
				userSuperVip = crmUserLabelService.hasPayMemberLabelForUser(id);
				log.info("======== id="+id+"userSuperVip:"+userSuperVip);
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
			//获取该用户是否VIP会员
			boolean isCsVip = false;
			String userTypeStr = null;
			try {
				String id = String.valueOf(order.getUserNo());
				Map<String, Object> map = csVipDubboService.getCsVipByCondition(id, 
						CsVipUserIdentityTypeEnum.USER_ID);
				isCsVip = (Boolean) map.get("isCsVip");
				//20180726   接口添加新字段 userType 会员类型
				Integer userType = (Integer) map.get("userType");
				log.info("========  userType:"+userType+",id="+id);
				if(userType!=null){
					userTypeStr =  SrvMemberEnum.USER_TYPE.getCnName(userType);
				}else {
					userTypeStr = SrvMemberEnum.USER_TYPE.getCnName(NORMAL_USER);
				}
				
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
			//vst组织鉴权
			super.vstOrgAuthentication(OrderDetailAction.class, order.getManagerIdPerm());
			
			if("STAMP".equals(order.getOrderSubType())){

				getStampOrdOrder(order,model);

			}
			if("STAMP_PROD".equals(order.getOrderSubType())){
				int stampDeductionCountAmount=0;
				List<StampDeductionList> stampCodeList=null;
				try {
					stampCodeList = getStampDeductionList(String.valueOf(order.getOrderId()));
				} catch (Exception e) {
					LOG.error(e.getMessage());
				}
				if(stampCodeList!=null){
				for(StampDeductionList stampCode:stampCodeList){
					stampDeductionCountAmount+=stampCode.getPrice();
				}
				order.setStampDeductionCountAmount(Long.valueOf(stampDeductionCountAmount));
				}
			}
			
			if("PAY_PROMOTION".equals(order.getOrderSubType())){
				try {
					OrdPayPromotion ordPayPromotion = ordPayPromotionService.queryOrdPayPromotionByOrderId(order.getOrderId());
					if(ordPayPromotion.getFavorableAmount()!=null)
						model.addAttribute("payPromotion", ordPayPromotion.getFavorableAmount());
					else
						model.addAttribute("payPromotion", 0);
				} catch (Exception e) {
					LOG.error(e.getMessage());
				}
			}
			OrdOrderItem orderItem=order.getMainOrderItem();
			String distributionPrice = order.getOrderAmountItemByType(ORDER_AMOUNT_TYPE.DISTRIBUTION_PRICE.name());
			model.addAttribute("distributionPrice",distributionPrice);

			try {
				//此订单优惠总金额
				Long favorUsageAmount = favorService.getSumUsageAmount(order.getOrderId());
				model.addAttribute("favorUsageAmount",favorUsageAmount==null?0:new BigDecimal(favorUsageAmount.floatValue()/100).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue());
				
				//促销减少总金额
				String totalOrderAmount = order.getOrderAmountItemByType(ORDER_AMOUNT_TYPE.PROMOTION_PRICE.name());
				model.addAttribute("totalOrderAmount",totalOrderAmount);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			List<OrdOrderHotelTimeRate> orderHotelTimeRateList=orderItem.getOrderHotelTimeRateList();
			//酒店订单使用状态
			model.addAttribute("performStatus",OrderEnum.PERFORM_STATUS_TYPE.getCnName(orderItem.getPerformStatus()));
			
			/*Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("supplierId", orderItem.getSupplierId());*/
			ResultHandleT<SuppSupplier> resultHandleSuppSupplier = suppSupplierClientService.findSuppSupplierById(orderItem.getSupplierId());
			if (resultHandleSuppSupplier.isSuccess()) {
				SuppSupplier  suppSupplier= resultHandleSuppSupplier.getReturnContent();
				model.addAttribute("suppSupplier", suppSupplier);
			} else {
				LOG.info("method:showOrderStatusManage,resultHandleSuppSupplier.isFial,msg=" + resultHandleSuppSupplier.getMsg());
			}
			//设置信用住标识
			setCreditTag(orderItem);
			
			
			/*
			// 保险取消规则及保险子订单显示
			boolean isContainInsurance = false;
			// 保险人修改展示
			boolean ishowInsurance = false;
			StringBuilder insuranceRules = null;
			
			
			//子订单列表展示
			List<OrdOrderItem> ordOrderItems = order.getOrderItemList();
			if (null != ordOrderItems && ordOrderItems.size() > 0) {
				for(OrdOrderItem orderItemTemp : ordOrderItems) {
					LOG.info("订单orderId = " + order.getOrderId() + "，子订单orderItemId = " + orderItemTemp.getOrderItemId() + "，categoryId = " + orderItemTemp.getCategoryId());
					Map<String,Object> contentMap = orderItemTemp.getContentMap();
					LOG.info("productName = " + orderItemTemp.getProductName() + "，branchName = " + contentMap.get("branchName"));
					if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItemTemp.getCategoryId())){
						if (SuppGoodsSaleRe.INSURANCE_TYPE.INSURANCE_730.name().equals(orderItemTemp.getProductType())) {
							ishowInsurance = true;
						}

						if (false == isContainInsurance) {
							isContainInsurance = true;
						}
						if (insuranceRules == null) {
							insuranceRules = new StringBuilder("");
						}
						insuranceRules.append(orderItemTemp.getProductName() + "-" + contentMap.get("branchName") + "<br/>");
						//不退不改
						if(ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(orderItemTemp.getCancelStrategy())){
							insuranceRules.append("不可退改<br/>");
						}else if(ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(orderItemTemp.getCancelStrategy())){
							insuranceRules.append("人工退改<br/>");
						}else if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(orderItemTemp.getCancelStrategy())){
							LOG.info("orderItemId = " + orderItemTemp.getOrderItemId() + "，refundRules = " + orderItemTemp.getRefundRules());
							Date minLastCancelTime = orderItemTemp.getLastCancelTime();
							String lineae = "", end = "";
							if (null != minLastCancelTime) {
								if (new Date().compareTo(minLastCancelTime) == 1) {
									lineae = "<span class='lineae_line'>";
									end = "</span>";
								}
								insuranceRules.append("可退改 扣款金额[("+orderItemTemp.getDeductAmountToYuan()+"元)]" +lineae+ DateUtil.getFormatDate(orderItemTemp.getLastCancelTime(), DateUtil.HHMM_DATE_FORMAT) + end +"前无损取消<br/>");
							}else{
								insuranceRules.append("可退改 扣款金额[("+orderItemTemp.getDeductAmountToYuan()+"元)]"+"<br/>");	
							}
						}
						
					}
				
				}
			}

			model.addAttribute("isContainInsurance", isContainInsurance);
			model.addAttribute("ishowInsurance", ishowInsurance);
			if (insuranceRules != null) {
				model.addAttribute("insuranceRules", insuranceRules.toString());
			}
			*/
			List<OrdPerson> personList = order.getOrdPersonList();
			// List<OrdPerson> travellerPersonList=new ArrayList<OrdPerson>();
			OrdPerson ordPersonContact = new OrdPerson();
			OrdPerson ordPersonBooker = new OrdPerson();
			String travellerName = "";
			int travellerNum=0;
			
			List<OrdPerson> insurePersonList = new ArrayList<OrdPerson>();
			for (OrdPerson ordPerson : personList) {

				String personType = ordPerson.getPersonType();
				if (OrderEnum.ORDER_PERSON_TYPE.CONTACT.name().equals(personType)) {

					ordPersonContact = ordPerson;
					continue;
				} if (OrderEnum.ORDER_PERSON_TYPE.BOOKER.name().equals(personType)) {

					ordPersonBooker = ordPerson;
					continue;
				} else if (OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equals(
						personType)) {

					// travellerPersonList.add(ordPerson);
					// 因为酒店取消险也是存的游玩人类型，但是身份证不能够为空，而酒店入住人的身份证是为空的，所以加如下判断，判断该联系人为酒店入住人
					if (StringUtil.isEmptyString(ordPerson.getIdType())) {
						if (travellerName.length() > 0) {
							travellerName += ",";
						}
						travellerName += ordPerson.getFullName();
						travellerNum+=1;
					}
				} else if (OrderEnum.ORDER_PERSON_TYPE.INSURER.name().equals(personType)) {
					//String goodsName = this.getGoodsName(ordItemMap, itemIdList, ordPerson.getOrdPersonId());
					//log.info("insurePersonList ordPerson OrdPersonId=" + ordPerson.getOrdPersonId() + " goodsName=" + goodsName);
					//ordPerson.setCheckInRoomName(goodsName);//关联的商品
					insurePersonList.add(ordPerson);
				}

			}
			model.addAttribute("insurePersonList", insurePersonList);
			//调用获取"未处理用户信息"的数量
			Integer compliantCallsCount = 0;
			try {
				StringBuffer url = new StringBuffer();
				url.append("http://super.lvmama.com/sales_front/rest/complaintCall/countPendingComplaintCall/order/");
				url.append(orderId);
				url.append("/user/");
				url.append(ordPersonBooker.getFullName());
				url.append(".do");
				compliantCallsCount = RestClient.getClient().getForObject(url.toString(), Integer.class);
				log.info("请求URL==== " + url.toString() + " ,请求结果===" + compliantCallsCount);
			} catch (Exception e) {
				e.printStackTrace();
				ExceptionFormatUtil.getTrace(e);
			}
			model.addAttribute("compliantCallsCount", compliantCallsCount);
			List<OrderHotelTimeRateInfo> hotelTimeRateInfoList=new ArrayList<OrderHotelTimeRateInfo>();
			try{
				hotelTimeRateInfoList= orderDetailApportionService.generateHotelTimeRateInfoList(orderItem);
			} 
			catch(Exception e){
				log.error("orderDetailApportionService generateHotelTimeRateInfoList error",e);
			}
			
			PermUser permUser= null;
			if(order.getManagerId() != null){
				try{
					permUser = permUserServiceAdapter.getPermUserByUserId(order.getManagerId());//改为直接去静态值
				} 
				catch(Exception e){
					log.error("permUserServiceAdapter getPermUserByUserId error",e);
				}
			}
			String city = "";
			String bedType = "", addValue = "";
			try {
				
				if (CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())) {
					Object bedObject = orderItem.getContentValueByKey(OrderEnum.HOTEL_CONTENT.bedType.name());
					bedType = bedObject.toString();
					Object addObject = orderItem.getContentValueByKey(OrderEnum.HOTEL_CONTENT.addValue.name());
					if (addObject != null) {
						addValue = addObject.toString();
					}
				} else {
					ResultHandleT<SuppGoods> resultHandleuppGoods = suppGoodsClientService.findSuppGoodsById(orderItem.getSuppGoodsId(), Boolean.TRUE, Boolean.TRUE);
					if (resultHandleuppGoods.isFail() || resultHandleuppGoods.getReturnContent() == null) {
						throw new RuntimeException("商品(ID=" + orderItem.getSuppGoodsId() + ")获取失败。msg=" + resultHandleuppGoods.getMsg());
					}
					SuppGoods suppGoods = resultHandleuppGoods.getReturnContent();
					Map<String, Object> propValueMap = suppGoods.getProdProductBranch().getPropValue();
					List<PropValue> propValueList = (List<PropValue>) propValueMap.get("bed_type");
					
					if (propValueList != null && propValueList.size() > 0) {
						bedType = propValueList.get(0).getName();
						addValue = propValueList.get(0).getAddValue();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				ExceptionFormatUtil.getTrace(e);
			}
			model.addAttribute("bedType", bedType);
			model.addAttribute("addValue", addValue);
			try {
				Long bizDistrictId = null;
				if (CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())) {
					Object districtObj = orderItem.getContentValueByKey(OrderEnum.HOTEL_CONTENT.bizDistrictId.name());
					if (districtObj != null) {
						bizDistrictId = Long.valueOf(districtObj.toString());
					}
				} else {
					ResultHandleT<ProdProduct>	result = prodProductClientService.findProdProductByIdFromCache(orderItem.getProductId());
					ProdProduct prodProduct = result.getReturnContent();
					bizDistrictId=prodProduct.getBizDistrictId();
				}
				if (bizDistrictId != null) {
					ResultHandleT<HashMap<String, BizDistrict>> resultHandlt=districtClientService.findCompleteDistrictById(bizDistrictId);
					HashMap<String, BizDistrict> districtMap=resultHandlt.getReturnContent();
					BizDistrict bizDistrict=districtMap.get(DISTRICT_TYPE.CITY.name());
					if (bizDistrict!=null) {
						city=bizDistrict.getDistrictName();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				ExceptionFormatUtil.getTrace(e);
			}

			List<OrderTagVo> orderTagVos = null;
			try {
				OrderTagVo orderTagvo = new OrderTagVo();
				orderTagvo.setTagType("SNAPSHOT_TAG");
				orderTagvo.setObjectType("ORD_ORDER");
				orderTagvo.setObjectId(orderId);
				com.lvmama.order.api.base.vo.ResponseBody<List<OrderTagVo>> responseBody = 
						apiOrderTagService.queryOrderTags(new com.lvmama.order.api.base.vo.RequestBody<OrderTagVo>(orderTagvo));
				if (responseBody != null && responseBody.isSuccess()) {
					orderTagVos = responseBody.getT();
				} else {
					LOG.error("查询TAG信息异常,TAG查询条件:" + JSONObject.fromObject(orderTagvo));
				}
			} catch (Exception e1) {
				LOG.error("###orderId=" + orderId + "查询tag信息异常", e1);
			}
			LOG.info("###orderId=" + orderId + "查询TAG信息:" + JSONArray.fromObject(orderTagVos));
			SnapshotOrderVo snapshotOrderVo = null;
			if (orderTagVos != null && CollectionUtils.isNotEmpty(orderTagVos)) {
				try {
					com.lvmama.order.api.base.vo.ResponseBody<SnapshotOrderVo> snapshotOrderResponse = 
							apiOrderSnapshotService.findSnapshotOrder(new com.lvmama.order.api.base.vo.RequestBody<Long>(orderId));
					if (snapshotOrderResponse != null && snapshotOrderResponse.isSuccess()) {
						snapshotOrderVo = snapshotOrderResponse.getT();
					} else {
						LOG.error("查询快照异常,orderId=" + orderId + ",异常信息:" + snapshotOrderResponse.getMessage());
					}
				} catch (Exception e) {
					LOG.error("###orderId=" + orderId + "查询快照信息异常", e);
				}
			}
			LOG.info("###orderId=" + orderId + "查询快照信息:" + JSONObject.fromObject(snapshotOrderVo));
			if(order.getDistributionChannel() != null) {
				if (snapshotOrderVo != null) {
					model.addAttribute("distributionChannelName", snapshotOrderVo.getDistributorName());
				} else {
					try {
						ResultHandleT<DistributorInfo> distributorInfoResult = tntDistributorServiceRemote.getDistributorById(order.getDistributionChannel());
						if (distributorInfoResult.isFail() || distributorInfoResult.getReturnContent() == null) {
							LOG.error("获取渠道代码" + order.getDistributionChannel() + "对应的渠道信息失败");
						}
						if(distributorInfoResult.getReturnContent() != null) {
							model.addAttribute("distributionChannelName", distributorInfoResult.getReturnContent().getDistributorName());
						}
					} catch (Exception e) {
						LOG.error("###orderId=" + orderId + "获取渠道代码异常,异常信息:", e);
					}
				}
			}
			//判断是否分销酒店品类下的单,满足的话,新增tntOrderChannel属性
			if(OrderUtils.isHotelByCategoryId(order.getCategoryId()) && Constant.DIST_BRANCH_SELL == order.getDistributorId()) {
				if (snapshotOrderVo != null && checkHasTntOrderChannel(orderTagVos, orderId) && snapshotOrderVo.getTntOrderChannel() != null) {
					model.addAttribute("tntOrderChannel", snapshotOrderVo.getTntOrderChannel());
				} else {
					try {
						ResultHandleT<String> tntOrderChannelResult = tntOrderQueryServiceRemote.getTntOrderChannel(orderId);
						if (tntOrderChannelResult.isFail() || tntOrderChannelResult.getReturnContent() == null) {
							LOG.error("getTntOrderChannel error:" + tntOrderChannelResult.getMsg());
						} else {
							model.addAttribute("tntOrderChannel", tntOrderChannelResult.getReturnContent());
						}
					} catch (Exception e) {
						LOG.error("###orderId=" + orderId + "getTntOrderChannel error:", e);
					}
				}
			}

			/** 给订单添加酒店联系电话 =========start*/
			Map<String,Object> propParams = new HashMap<String, Object>();
			propParams.put("productId", order.getProductId());
			propParams.put("propId", 5L);
			ResultHandleT<List<ProdProductProp>> resultHandleT = prodProductPropClientService.findProdProductPropList(propParams);
			List<ProdProductProp> prodProductPropList = resultHandleT.getReturnContent();
			StringBuffer hotelTels = new StringBuffer();
			if(prodProductPropList != null && prodProductPropList.size() > 0){
				for(ProdProductProp prodProductProp : prodProductPropList){
					if(StringUtil.isNotEmptyString(prodProductProp.getPropValue())){
						hotelTels.append(prodProductProp.getPropValue()+"  ");
					}
				}
			}
			if(hotelTels.length() < 1){
				hotelTels.append("无");
			}
			order.setHotelTel(hotelTels.toString());
			/** 给订单添加酒店联系电话 =========end*/
			//下单渠道
			Distributor distributor = distributorClientService.findDistributorById(order.getDistributorId()).getReturnContent();

			model.addAttribute("distributorName", distributor.getDistributorName());
			model.addAttribute("orderStatusStr", OrderEnum.ORDER_STATUS.getCnName(order.getOrderStatus()));
			//model.addAttribute("settlementStatusStr", OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.getCnName(orderItem.getSettlementStatus()));
			/**
			 * 2017/03/06 结算状态改造
			 */

			model.addAttribute("settlementStatusStr", OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.getCnName(getSetSettlementItemStatus(orderItem.getOrderItemId())));

			
			
			model.addAttribute("paymentStatusStr", OrderEnum.PAYMENT_STATUS.getCnName(order.getPaymentStatus()));
			model.addAttribute("order", order);
			
			//如果是酒店并且是对接则查询供应商订单号
			if(orderItem.getCategoryId()==1 && orderItem.hasSupplierApi()){
				//获取suppOrder信息
				LOG.info("OrderHotelDetailAction===showOrderStatusManage OrderItemId="+orderItem.getOrderItemId());
				RequestSuppOrder suppOrder = suppOrderClientService.getSuppOrderByOrderItemId(orderItem.getOrderItemId());
				if(suppOrder != null){
					String suppOrderId = suppOrder.getSuppOrderId();
					String reservationNo=suppOrder.getReservationNo();
					LOG.info("OrderHotelDetailAction===showOrderStatusManage OrderItemId="+orderItem.getOrderItemId()+",suppOrderId:"+suppOrderId+",reservationNo:"+reservationNo);
					model.addAttribute("suppOrderId", suppOrderId);
					model.addAttribute("reservationNo", reservationNo);
				}
			}
			//携程促销酒店，获取促销描述
			if ("Y".equalsIgnoreCase(orderItem.getContentStringByKey("ctripPromotionFlag"))){
				try {
					com.lvmama.order.api.base.vo.ResponseBody<List<com.lvmama.order.snap.vo.SnapshotCtripHotelPromotionVo>> snapshotOrderResponse=apiOrderSnapshotService.findSnapshotCtripHotelPromotion(new com.lvmama.order.api.base.vo.RequestBody<Long>(orderItem.getOrderItemId()));
					if (snapshotOrderResponse != null && snapshotOrderResponse.isSuccess()) {
						List<com.lvmama.order.snap.vo.SnapshotCtripHotelPromotionVo> snapshotCtripHotelPromotion = snapshotOrderResponse.getT();
						if(null!=snapshotCtripHotelPromotion&&snapshotCtripHotelPromotion.size()>0){
							model.addAttribute("snapshotCtripHotelPromotion", GsonUtils.toJson(snapshotCtripHotelPromotion));
						}
					} else {
						LOG.error("查询携程促销快照异常,orderId=" + orderId + ",异常信息:" + snapshotOrderResponse.getMessage());
					}
				} catch (Exception e) {
					LOG.error("###orderId=" + orderId + "查询携程促销快照信息异常", e);
				}
			}
			
			if(order.getLastCancelTime()!=null){
				Date now=new Date();
				Date lastCancelTime=order.getLastCancelTime();
				model.addAttribute("isGreaterNow", now.compareTo(lastCancelTime));
			}
			String deductTypeStr="";
			String deductType=orderItem.getDeductType();
			if (!StringUtils.isEmpty(deductType)) {
				deductTypeStr=SuppGoodsTimePrice.DEDUCTTYPE.getCnName(orderItem.getDeductType());
			}
			String deductAmountStr="";
			Long deductAmount=orderItem.getDeductAmount();
			if (deductAmount!=null) {
				deductAmountStr=deductAmount/100.0+"";
			}else{
				deductAmountStr="0";
			}
			model.addAttribute("deductAmountStr", deductAmountStr);
			
			
			model.addAttribute("deductTypeStr", deductTypeStr);
			
			model.addAttribute("orderItem", orderItem);
			
			String currencyCode = orderItem.getContentStringByKey("currencyCode");
			String cashSellRate = orderItem.getContentStringByKey("cashSellRate");
			
			model.addAttribute("currencyCode", currencyCode);
			model.addAttribute("cashSellRate", cashSellRate);
			
			String stag1 = "=======================================================";
			String stag2 = stag1 + "\n" + stag1 + "\n" + stag1 + "\n" + stag1 + "\n" + stag1;
	 		
			log.info(stag2);
			log.info("OrderItemId = " + orderItem.getOrderItemId());
			log.info("currencyCode = " + currencyCode);
			log.info("cashSellRate = " + cashSellRate);
			
			//订单详情页，[满房通知] 走灰度
			Long suppGoodsId = orderItem.getSuppGoodsId();
			//start...为酒店推荐提供数据
			HotelGoodsVstPo hotelGoodsVstPo = new HotelGoodsVstPo();
			hotelGoodsVstPo.setSuppGoodsId(suppGoodsId);
			hotelGoodsVstPo.setHasProp(true);
		    hotelGoodsVstPo.setHasPropValue(true);
			com.lvmama.dest.api.common.RequestBody<HotelGoodsVstPo> requestBody = 
					new com.lvmama.dest.api.common.RequestBody<HotelGoodsVstPo>();
			requestBody.setT(hotelGoodsVstPo);
			requestBody.setToken(Constant.DEST_BU_HOTEL_TOKEN);
			String starId = null;//酒店星级
			String mapType = null;//地图类型
			String baiduGeo = null;//经纬度
			try {
				com.lvmama.dest.api.common.ResponseBody<HotelGoodsVstVo> responseBody = 
						hotelGoodsQueryVstApiService.findSuppGoodsByParam(requestBody);
				HotelGoodsVstVo hotelGoodsVstVo = responseBody.getT();
				List<HotelProductPropVstVo> list = hotelGoodsVstVo.getProdProduct().getProdProductPropList();
				for(HotelProductPropVstVo productPropVstVo : list) {
					if(productPropVstVo.getPropId() == 19L ){
						starId = productPropVstVo.getPropValue();
					}
					if(productPropVstVo.getPropId() == 28L ){
						baiduGeo = productPropVstVo.getPropValue();
					}
					if(productPropVstVo.getPropId() == 963L ){
						mapType = productPropVstVo.getPropValue();
					}
				}
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
			}
			model.addAttribute("mapType", mapType);
			model.addAttribute("baiduGeo", baiduGeo);
			model.addAttribute("starId", starId);
			//end...为酒店推荐提供数据
			if(DynamicRouterUtils.getInstance().isGrayGoodsId(suppGoodsId)){
				model.addAttribute("showHouseStatus", "/lvmm_dest_back/goods/house/showHouseStatus.do");
		     }else{
				model.addAttribute("showHouseStatus", "/vst_back/goods/house/showHouseStatus.do");
			}
			model.addAttribute("userSuperVip", userSuperVip);
			model.addAttribute("isCsVip", isCsVip);
			model.addAttribute("userTypeStr",userTypeStr);
			model.addAttribute("city", city);
			model.addAttribute("ordPersonContact", ordPersonContact);
			model.addAttribute("ordPersonBooker", ordPersonBooker);
			
			model.addAttribute("travellerName", travellerName);
			model.addAttribute("travellerNum", travellerNum);
			
			if (permUser!=null) {
				StringBuilder sb = new StringBuilder();
				sb.append(permUser.getRealName());
				sb.append("(");
				sb.append(permUser.getUserName());
				sb.append(")");
				model.addAttribute("productManager",sb.toString() );
			}
			
			
			model.addAttribute("isRoomReservations", orderItem.isRoomReservations());//是否房型保留
			
			OrdOrderHotelTimeRate lastOrderHotelTimeRate=new OrdOrderHotelTimeRate();
			if (CollectionUtils.isNotEmpty(orderHotelTimeRateList)) {
				lastOrderHotelTimeRate=orderHotelTimeRateList.get(orderHotelTimeRateList.size()-1);
			}
			if (lastOrderHotelTimeRate.getVisitTime()!=null) {
				Date visitTime=DateUtils.addDays(lastOrderHotelTimeRate.getVisitTime(), 1);
				lastOrderHotelTimeRate.setVisitTime(visitTime);
				
				model.addAttribute("arrivalDays", CalendarUtils.getDayCounts(orderItem.getVisitTime(), visitTime));
			}
			
			
			model.addAttribute("lastOrderHotelTimeRate", lastOrderHotelTimeRate);
			model.addAttribute("lastTime", orderItem.getContentMap().get(OrderEnum.HOTEL_CONTENT.lastArrivalTime.name()));
			model.addAttribute("orderHotelTimeRateList", orderHotelTimeRateList);
			model.addAttribute("hotelTimeRateInfoList", hotelTimeRateInfoList);
			
			
			Map<String, Object> dictDefPara = new HashMap<String, Object>();
			dictDefPara.put("dictCode", Constants.ORDER_CANCEL_TYPE);
			dictDefPara.put("cancelFlag", "Y");
			List<BizDictDef> dictDefs = dictDefClientService.findDictDefList(dictDefPara).getReturnContent();
			boolean isStart = false;
			//当订单为目的地BU，且是酒店
			if (BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId()) && CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())
					&& order.hasPayed()) {
				//是否开启退款流程
				isStart = orderRefundProcesserAdapter.isStartProcessByRefund(order, getLoginUserId());
			}
			List<BizDictDef> dictDefList = new ArrayList<BizDictDef>();
			for (BizDictDef def : dictDefs) {
				if (!isStart && VstOrderEnum.ORDER_CANCEL_TYPE_RESOURCE_NO_CONFIM_REFUND_PROCESS.equals(def.getDictDefId())) {
					continue;
				}
				dictDefList.add(def);
			}
			model.addAttribute("orderCancelTypeList", dictDefList);	
			model.addAttribute("vst_front_Ip", Constant.getInstance().getProperty(Constant.VST_FRONT_IP));
			model.addAttribute("broadband",orderItem.getContentValueByKey(OrderEnum.HOTEL_CONTENT.internet.name()));

			//传真方式
			model.addAttribute("faxFlag",orderItem.getContentValueByKey(ORDER_COMMON_TYPE.fax_flag.name()));

			int messageCount;
			//根据主订单的id
			Long[] auditIdArray = findBookingAuditIds(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString(),orderId);
			//查出子订单所有的审查信息
			Long[] itemAuditIdArray = getHotelItemsFormAudit(order);				
			if (auditIdArray.length==0 && itemAuditIdArray.length==0) {
				messageCount=0;
			}else{
				//合并主订单和子订单的ID，根据合并后的ID查询消息表中的数据
				Long[] allAuditIdArrays = (Long[]) ArrayUtils.addAll(auditIdArray, itemAuditIdArray);
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("messageStatus",OrderEnum.MESSAGE_STATUS.UNPROCESSED.getCode());
				parameters.put("auditIdArray",allAuditIdArrays);			
				messageCount=comMessageService.findComMessageCount(parameters);
			}
			model.addAttribute("messageCount", messageCount);
			
			/*
			boolean certificateAuthority = this.certificateAuthority(loginUserId,orderItem);
			model.addAttribute("certificateAuthority", certificateAuthority);
			*/
			
			boolean isDoneCertificate=this.isDoneCertificate(order);
			model.addAttribute("isDoneCertificate", isDoneCertificate);
			
			
			boolean isDonePaymentAudit = this.isDonePaymentAudit(order);
			model.addAttribute("isDonePaymentAudit", isDonePaymentAudit);
			
			//小驴分期催支付
			boolean isDoneTimePaymentAudit = this.isDoneTimePaymentAudit(order);
			model.addAttribute("isDoneTimePaymentAudit", isDoneTimePaymentAudit); 
			
			boolean isDoneCancleConfirmedtAudit =this.isDoneCancleConfirmedAudit(order);
			model.addAttribute("isDoneCancleConfirmedtAudit", isDoneCancleConfirmedtAudit);
			
			boolean isDoneOnlineRefundAudit =this.isDoneOnlineRefundAudit(order);
			model.addAttribute("isDoneOnlineRefundAudit", isDoneOnlineRefundAudit);
			
			
			//查询订单附件数量,added by wztyunbo 2013-12-19
			int orderAttachmentNumber = orderAttachmentService.countOrderAttachment(orderId);
			model.addAttribute("orderAttachmentNumber", orderAttachmentNumber);
			
			model.addAttribute("isSupplierOrder", order.isSupplierOrder()+"");
			
			
			model.addAttribute("auditMap", this.getUnprocessedAudit(order));

			//节省系统开销，不进行查询，统一设置为true，前台显示“另有订单”
			model.addAttribute("otherOrder",true);
			
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderId", orderId);//订单号
			String readUserStatus =EbkFaxVO.READ_USER_STATUS_YES;
			params.put("isReadUser", readUserStatus);//是否读取，Y：已读取 N：未读取
			params.put("cancelFlag", "Y");//是否有效
			
			ResultHandleT<Integer> resultHandleInteger = ebkFaxTaskClientService.findEbkFaxRecvCount(params);
			if (resultHandleInteger.isSuccess()) {
				model.addAttribute("ebkCount",resultHandleInteger.getReturnContent());
			}

			//Added by yangzhenzhong 目的地酒店订单详情，添加酒店预定号
			if(CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equalsIgnoreCase(order.getBuCode())){
				ResultHandleT<List<EbkCertifItem>> resultHandle=ebkFaxTaskClientService.selectEbkCertifItemListByOrderId(orderId);
				if(resultHandle.getReturnContent()!=null && resultHandle.getReturnContent().size()>0){
					//获取最新创建的对象
					EbkCertifItem ebkCertifItem = resultHandle.getReturnContent().get(resultHandle.getReturnContent().size()-1);
					model.addAttribute("supplierNo",ebkCertifItem.getSupplierNo());
				}
			}
			//目的地BU前台下单特殊显示
			model.addAttribute("isDestBuFrontOrder", OrdOrderUtils.isDestBuFrontOrder(order) && !orderItem.hasSupplierApi());
			
			// 期票不适用日期集合
			List<String> ordItemsAperiodicUnvalidList = new ArrayList<>();
			// 期票有效期描述
			List<String> ordItemsAperiodicExpList = new ArrayList<String>();
			// 子单附件集合
			Map<String, Object> attachmentParam = new HashMap<String, Object>();
			// 为了保证子订单中，分摊信息中显示的多价格顺序与其它多价格顺序一致，保存子单的id与多价格的对应关系，用于后来排序
			Map<Long, List<OrdMulPriceRate>> itemIdWithMulPriceMap = new HashMap<>();
			// 子订单列表展示
			Map<String,List<OrderMonitorRst>> resultMap = new HashMap<String,List<OrderMonitorRst>>();
			model.addAttribute("ordItemsAperiodicUnvalidList", ordItemsAperiodicUnvalidList);
			model.addAttribute("ordItemsAperiodicExpList",ordItemsAperiodicExpList);
			model.addAttribute("resultMap", resultMap);
			
			// 保险取消规则及保险子订单显示
			boolean isContainInsurance = false;
			// 保险人修改展示
			boolean ishowInsurance = false;
			StringBuilder insuranceRules = null;
			
			// 董宁波 2016年4月26日 11:27:56 订单详情保留房显示
			for (OrdOrderItem ordOrderItem : order.getOrderItemList()) {

				// 保险退改规则
				LOG.info("订单orderId = " + order.getOrderId() + "，子订单orderItemId = " + ordOrderItem.getOrderItemId() + "，categoryId = " + ordOrderItem.getCategoryId());
				Map<String,Object> contentMap_temp = ordOrderItem.getContentMap();
				LOG.info("productName = " + ordOrderItem.getProductName() + "，branchName = " + contentMap_temp.get("branchName"));
				if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(ordOrderItem.getCategoryId())){
					if (SuppGoodsSaleRe.INSURANCE_TYPE.INSURANCE_730.name().equals(ordOrderItem.getProductType())) {
						ishowInsurance = true;
					}

					if (false == isContainInsurance) {
						isContainInsurance = true;
					}
					if (insuranceRules == null) {
						insuranceRules = new StringBuilder("");
					}
					insuranceRules.append(ordOrderItem.getProductName() + "-" + contentMap_temp.get("branchName") + "<br/>");
					//不退不改
					if(ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(ordOrderItem.getCancelStrategy())){
						insuranceRules.append("不可退改<br/>");
					}else if(ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(ordOrderItem.getCancelStrategy())){
						insuranceRules.append("人工退改<br/>");
					}else if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(ordOrderItem.getCancelStrategy())){
						LOG.info("orderItemId = " + ordOrderItem.getOrderItemId() + "，refundRules = " + ordOrderItem.getRefundRules());
						Date minLastCancelTime = ordOrderItem.getLastCancelTime();
						String lineae = "", end = "";
						if (null != minLastCancelTime) {
							if (new Date().compareTo(minLastCancelTime) == 1) {
								lineae = "<span class='lineae_line'>";
								end = "</span>";
							}
							insuranceRules.append("可退改 扣款金额[("+ordOrderItem.getDeductAmountToYuan()+"元)]" +lineae+ DateUtil.getFormatDate(ordOrderItem.getLastCancelTime(), DateUtil.HHMM_DATE_FORMAT) + end +"前无损取消<br/>");
						}else{
							insuranceRules.append("可退改 扣款金额[("+ordOrderItem.getDeductAmountToYuan()+"元)]"+"<br/>");	
						}
					}
				}
			
				// 判断是否是酒店品类
				if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(ordOrderItem.getCategoryId())) {
					List<OrdOrderStock> stockList = orderStockService.findOrderStockListByOrderItemId(ordOrderItem.getOrderItemId());
					ordOrderItem.setOrderStockList(stockList);
				} else {
					// 规格Id
					Long branchId = null;
					// 判断是否是交通接驳、美食、娱乐、购物
					if (BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(ordOrderItem.getCategoryId())
							|| BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(ordOrderItem.getCategoryId())
							|| BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(ordOrderItem.getCategoryId())
							|| BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(ordOrderItem.getCategoryId())) {
						Long prodBranchId = ordOrderItem.getBranchId();
						try {
							// 查询产品规格信息
							ResultHandleT<ProdProductBranch> branchResultHandleT = this.prodProductBranchClientService.findProdProductBranchById(prodBranchId);
							if (branchResultHandleT != null && branchResultHandleT.getReturnContent() != null) {
								branchId = branchResultHandleT.getReturnContent().getBranchId();
							}
						} catch (Exception e) {
							e.printStackTrace();
							log.error(ExceptionFormatUtil.getTrace(e));
						}
					}
					
					// 子单品类Id
					String categoryId = String.valueOf(ordOrderItem.getCategoryId());
					// 新订单监控页面结果集
					List<OrderMonitorRst> childOrderResultList;
					if (!resultMap.containsKey(categoryId)) {
						childOrderResultList =  new ArrayList<OrderMonitorRst>();
						resultMap.put(categoryId, childOrderResultList);
					} else {
						childOrderResultList = resultMap.get(categoryId);
					}
					
					Map<String,Object> contentMap = ordOrderItem.getContentMap();
					String categoryType =  (String) contentMap.get(ORDER_COMMON_TYPE.categoryCode.name());
					// 获取当地玩乐美食娱乐属性
					if (BIZ_CATEGORY_TYPE.category_sport.getCode().equals(categoryType)
							|| BIZ_CATEGORY_TYPE.category_food.getCode().equals(categoryType)) {
						if (contentMap != null && StringUtils.isNotBlank(categoryType)) {
							// 使用时间
							String useTime = (String) contentMap.get(ORDER_PLAY_TYPE.useTime.name());
							// 酒店地址
							String localHotelAddress = (String) contentMap.get(ORDER_PLAY_TYPE.localHotelAddress.name());
							ordOrderItem.setUseTime(useTime);
							ordOrderItem.setLocalHotelAddress(localHotelAddress);
						}
					}
					// 获取品类对象
					ResultHandleT<BizCategory> result = categoryClientService.findCategoryByCode(categoryType);
					BizCategory bizCategory = result.getReturnContent();
					
					String objectType = "ORDER_ITEM";
					Long objectId = ordOrderItem.getOrderItemId();
					// 订单负责人
					PermUser orderPrincipal = new PermUser();
					try{
						// 获取子单负责人
						orderPrincipal = orderResponsibleService.getOrderPrincipal(objectType, objectId);
					}catch(Exception e){
						log.error("orderResponsibleService getOrderPrincipal error",e);
					}
					// 资源审核人真实名称
					String resourceApprover = "";
					try{
						resourceApprover = orderResponsibleService.getResourceApprover(objectId, objectType).getRealName();
					}catch(Exception e){
						log.error("orderResponsibleService getResourceApprover error",e);
					}
					// 新订单监控页面结果集对象
					OrderMonitorRst orderMonitorRst = new OrderMonitorRst();
					// 子订单备注
					orderMonitorRst.setOrderItemMemo(ordOrderItem.getOrderMemo());
					// 负责人
					orderMonitorRst.setPrincipal(orderPrincipal.getRealName());
					// 资源审核人
					orderMonitorRst.setResourceApprover(resourceApprover);
					// 是否做过过期退标识（Y|N）
					orderMonitorRst.setExpiredRefundFlag(ordOrderItem.getExpiredRefundFlag());
					// 子单Id
					orderMonitorRst.setOrderId(ordOrderItem.getOrderItemId());
					// 子单Id
					orderMonitorRst.setOrderItemId(ordOrderItem.getOrderItemId());
					// 处理订单的当前状态
					String buildCurrentStatus = this.buildCurrentStatus(order,ordOrderItem);
					// 订单组合状态
					orderMonitorRst.setCurrentStatus(buildCurrentStatus);
					// 子订单类型
					orderMonitorRst.setChildOrderType(categoryType);
					// 使用时间
					orderMonitorRst.setUseTime(ordOrderItem.getUseTime());
					// 当地酒店地址
					orderMonitorRst.setLocalHotelAddress(ordOrderItem.getLocalHotelAddress());
					// 子订单价格确认状态
					orderMonitorRst.setPriceConfirmStatus(ordOrderItem.getPriceConfirmStatus());
					// 设置子订单类别
					String childOrderTypeName = BIZ_CATEGORY_TYPE.getCnName(categoryType);
					// 交通接驳
					if(BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(ordOrderItem.getCategoryId())){
						// 交通接驳，子订单类型名称，增加规格名称
						if(branchId != null){
							// 查询基础规格对象
							ResultHandleT<BizBranch> branchResultHandleT = branchClientService.findBranchById(branchId);
							if(branchResultHandleT != null && branchResultHandleT.getReturnContent() != null){
								childOrderTypeName = childOrderTypeName + "-" + branchResultHandleT.getReturnContent().getBranchName();
							}
						}
					}
					// 产品名称
					orderMonitorRst.setProductName(this.buildProductName(ordOrderItem));
//					orderMonitorRst.setProducTourtType(prodProduct.getProducTourtType());
					// 子订单类型名字
					orderMonitorRst.setChildOrderTypeName(childOrderTypeName);
					
					//wifi品类
					if(BIZ_CATEGORY_TYPE.category_wifi.getCode().equals(categoryType)){
						orderMonitorRst.setProductType(OrderUtil.getProductType(ordOrderItem));
						OrderWifiAdditionInfo orderWifiAdditionInfo = new OrderWifiAdditionInfo();
						//wifi租赁天数计算
						if(ProdProduct.WIFIPRODUCTTYPE.WIFI.name().equals(OrderUtil.getProductType(ordOrderItem))){
							if(ordOrderItem.getContentStringByKey(ORDER_WIFI_TYPE.lease_endDay.name())!=null &&
									ordOrderItem.getContentStringByKey(ORDER_WIFI_TYPE.lease_startDay.name())!=null	){
								Date endDay = DateUtil.toDate(ordOrderItem.getContentStringByKey(ORDER_WIFI_TYPE.lease_endDay.name()), "yyyy-MM-dd");
								Date startDay = DateUtil.toDate(ordOrderItem.getContentStringByKey(ORDER_WIFI_TYPE.lease_startDay.name()),"yyyy-MM-dd");
								Long days = (long) (DateUtil.getDaysBetween(startDay, endDay)+1);
								orderWifiAdditionInfo.setRentDays(days);
							}
						}
						orderWifiAdditionInfo.setPickingType(ordOrderItem.getContentStringByKey(ORDER_WIFI_TYPE.picking_type.name())) ;
						orderWifiAdditionInfo.setEndDay(ordOrderItem.getContentStringByKey(ORDER_WIFI_TYPE.lease_endDay.name())) ;
						orderWifiAdditionInfo.setTackCityName(ordOrderItem.getContentStringByKey(ORDER_WIFI_TYPE.take_city_name.name())) ;
						orderWifiAdditionInfo.setBackCityName(ordOrderItem.getContentStringByKey(ORDER_WIFI_TYPE.back_city_name.name()));
						orderWifiAdditionInfo.setTakePickingPoint(ordOrderItem.getContentStringByKey(ORDER_WIFI_TYPE.take_picking_point.name())) ;
						orderWifiAdditionInfo.setBackPickingPoint(ordOrderItem.getContentStringByKey(ORDER_WIFI_TYPE.back_picking_point.name())) ;
						model.addAttribute("wifiAddition",orderWifiAdditionInfo);
					}else if(BIZ_CATEGORY_TYPE.category_other.getCode().equals(categoryType)){
						if(ProdProduct.PRODUCTTYPE.EXPRESS.getCode().equals(OrderUtil.getProductType(ordOrderItem))||
								ProdProduct.PRODUCTTYPE.DEPOSIT.getCode().equals(OrderUtil.getProductType(ordOrderItem))){
							orderMonitorRst.setProductType(OrderUtil.getProductType(ordOrderItem));
							OrderWifiAdditionInfo orderWifiAdditionInfo = new OrderWifiAdditionInfo();
							orderWifiAdditionInfo.setPickingType(ordOrderItem.getContentStringByKey(ORDER_WIFI_TYPE.picking_type.name())) ;
							orderWifiAdditionInfo.setEndDay(ordOrderItem.getContentStringByKey(ORDER_WIFI_TYPE.lease_endDay.name())) ;
						}
					}
					
					// 构建子订单购买份数、销售单价和总价
					this.buildBuyCountAndPrice(ordOrderItem, orderMonitorRst, itemIdWithMulPriceMap);
					orderMonitorRst.setVisitTime(this.buildVisitTime(ordOrderItem));
					
					String apiFlag="N";
					if (bizCategory.getParentId() != null && (bizCategory.getParentId().equals(5L) ||
							BIZ_CATEGORY_TYPE.category_connects.getCode().equalsIgnoreCase(categoryType))) {//门票
						if (StringUtils.equals(ordOrderItem.getContentStringByKey(ORDER_TICKET_TYPE.notify_type.name()),
								SuppGoods.NOTICETYPE.QRCODE.name())) {
							apiFlag="Y";
						}
					}else{
						if(ordOrderItem.hasSupplierApi()){
							apiFlag="Y";
						}
					}
					orderMonitorRst.setApiFlag(apiFlag);
					
					Map<String, Object> paramOrdItemPersonRelation = new HashMap<String, Object>();
					paramOrdItemPersonRelation.put("orderItemId", ordOrderItem.getOrderItemId());
					List<OrdItemPersonRelation> ordItemPersonRelationList = ordItemPersonRelationService.findOrdItemPersonRelationList(paramOrdItemPersonRelation);

					orderMonitorRst.setPersonCount(ordItemPersonRelationList.size());

					// 附件数量
					attachmentParam.put("orderId", orderId);
					attachmentParam.put("orderItemId", ordOrderItem.getOrderItemId());
					orderMonitorRst.setOrderAttachmentNumber(orderAttachmentService.countOrderAttachmentByCondition(attachmentParam));
					
					childOrderResultList.add(orderMonitorRst);
					//董宁波 2016年4月26日 11:27:56 订单详情保留房显示
					List<OrdOrderStock> stockList = orderStockService.findOrderStockListByOrderItemId(ordOrderItem.getOrderItemId());
					ordOrderItem.setOrderStockList(stockList);

					//如果子订单是期票，填充有效期信息
					String aperiodicFlag =  (String) contentMap.get(ORDER_TICKET_TYPE.aperiodic_flag.name());
					if("Y".equals(aperiodicFlag)){
						String goodsExpStr =  (String) contentMap.get(ORDER_TICKET_TYPE.goodsExpInfo.name());
						ordItemsAperiodicExpList.add("(子订单号"+ordOrderItem.getOrderItemId()+")"+goodsExpStr);

						//期票不适用日期
						String unvalidDesc = (String) contentMap.get(ORDER_TICKET_TYPE.aperiodic_unvalid_desc.name());
						if (StringUtil.isNotEmptyString(unvalidDesc)) {
							ordItemsAperiodicUnvalidList.add("(子订单号" + ordOrderItem.getOrderItemId() + ")" + unvalidDesc);
						}
					}
				}
			}

			if (ApportionUtil.isApportionEnabled()) {
				log.info("Now process apportion info for order " + orderId);
				orderDetailApportionService.calcOrderDetailItemApportion(orderId, resultMap, itemIdWithMulPriceMap);
				log.info("Process apportion info completed for order " + orderId);
			}

			model.addAttribute("isContainInsurance", isContainInsurance);
			model.addAttribute("ishowInsurance", ishowInsurance);
			if (insuranceRules != null) {
				model.addAttribute("insuranceRules", insuranceRules.toString());
			}

			//end
	        model.addAttribute("isPartStockFlag", OrdOrderUtils.getStockFlag(order));
		 	if (isDestBu(order, order.getBuCode())) {
				String cancelCertConfirmStatus = ebkCertifClientService.checkCertifCancelApply(orderItem.getOrderItemId());
				model.addAttribute("cancelCertConfirmStatus", cancelCertConfirmStatus);
			} else {
				// 是否显示重新取消的按钮
				model.addAttribute("isReCancelBtn",ebkCertifClientService.checkCertifCancelApply(orderItem.getOrderItemId()));
			}
			//目的地酒店订单详情页，添加发票模块  addBy wangyongfang
			/*
			boolean isDestBuSingleHotelFrontOrder = OrdOrderUtils.isDestBuSingleHotelFrontOrder(order);
	        model.addAttribute("isDestBuSingleHotelFrontOrder", isDestBuSingleHotelFrontOrder);
	        if (isDestBuSingleHotelFrontOrder) {
				
				//若订单已取消，同步更新发票信息表状态为 已取消
				if( StringUtils.isNotBlank(order.getOrderStatus()) && order.getOrderStatus().equals("CANCEL")){
					ordApplyInvoiceInfoService.updateApplyInfoStatusByOrderStatus(orderId);
				}
				OrderInvoiceInfoVst  orderInvoiceInfoVst = ordApplyInvoiceInfoService.getVstOrderInvoiceInfo(orderId);
				if (null != orderInvoiceInfoVst) {
					 long invoiceAmountLong = 0 ;
			            if(("false").equals(order.getNeedInvoice())) {
			                 //计算发票金额	                 
			                 VstInvoiceAmountVo vstInvoiceAmountVo = orderService.getInvoiceAmount(orderId);
			              	 invoiceAmountLong = vstInvoiceAmountVo.getInvoiceAmount(); 
			              	 orderInvoiceInfoVst.setAmount(invoiceAmountLong);
			            }
					    model.addAttribute("orderInvoiceInfoVst", orderInvoiceInfoVst);
	            }
				
			}*/
			//凡是存在发票申请信息即展示（不分BU、不分渠道）
	        OrderInvoiceInfoVst  orderInvoiceInfoVst = ordApplyInvoiceInfoService.getVstOrderInvoiceInfo(orderId);
	        if (null != orderInvoiceInfoVst) {
	            
	            //若订单已取消，同步更新发票信息表状态为 已取消
	            if (StringUtils.isNotBlank(orderInvoiceInfoVst.getStatus()) && !"CANCEL".equalsIgnoreCase(orderInvoiceInfoVst.getStatus())) {
	                if( StringUtils.isNotBlank(order.getOrderStatus()) && order.getOrderStatus().equals("CANCEL")){
	                    ordApplyInvoiceInfoService.updateApplyInfoStatusByOrderStatus(orderId);
	                    orderInvoiceInfoVst = ordApplyInvoiceInfoService.getVstOrderInvoiceInfo(orderId);
	                }
	            }
	            
	             long invoiceAmountLong = 0 ;
	                if(("false").equals(order.getNeedInvoice())) {
	                     //计算发票金额                    
	                     VstInvoiceAmountVo vstInvoiceAmountVo = orderService.getInvoiceAmount(orderId);
	                     invoiceAmountLong = vstInvoiceAmountVo.getInvoiceAmount(); 
	                     orderInvoiceInfoVst.setAmount(invoiceAmountLong);
	                }
	                model.addAttribute("isDestBuSingleHotelFrontOrder", true);
	                model.addAttribute("orderInvoiceInfoVst", orderInvoiceInfoVst);
	        }else{
	        	//防止ftl报错
				model.addAttribute("orderInvoiceInfoVst", orderInvoiceInfoVst=new OrderInvoiceInfoVst());
			}
			
	        
	        //add by zjt
	        String callId = StringUtils.isEmpty(request.getParameter("callid"))?"":request.getParameter("callid");
	        model.addAttribute("callId", callId);        
	        //是否需要显示订单状态
	        if(BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())
	        		&&("3.0".equals(order.getWorkVersion())||"3.1".equals(order.getWorkVersion()))&&!order.isCancel()
	        		&& BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())
	        		&& BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())){
	        	if("SUCCESS".equals(orderItem.getConfirmStatus())){
	        		model.addAttribute("isNeedShowConfirmStatus",true);
	        	}else{
			        Map<String, Object> auditParam = new HashMap<String, Object>();
			        auditParam.put("auditStatus", OrderEnum.AUDIT_STATUS.UNPROCESSED.name());
			        auditParam.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
			        List<Long> categoryIds = Arrays.asList(
			                BIZ_CATEGORY_TYPE.category_hotel.getCategoryId(),
			                BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId(),
			                BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId());
			        auditParam.put("categoryIds", categoryIds);
			        auditParam.put("objectId", orderItem.getOrderItemId());
			        List<ComAudit> auditList = orderAuditService.queryDestAuditListByCriteria(auditParam);
			        if(auditList!=null&&auditList.size()>0){
			        	String[] auditTypes = {"INCONFIRM_AUDIT","FULL_AUDIT","PECULIAR_FULL_AUDIT","CHANGE_PRICE_AUDIT"};
			        	for (ComAudit comAudit : auditList) {
			        		String auditType = comAudit.getAuditType();
			        		if(ArrayUtils.contains(auditTypes, auditType)){
			        			model.addAttribute("isNeedShowConfirmStatus",true);
			        		}
						}
			        }
	        	}   
	        }
	        
	     // 判断是否是发票快递子单（品类是“其它”并且是客服BU）
			if (BizEnum.BIZ_CATEGORY_TYPE.category_other.getCategoryId().equals(order.getCategoryId())
					&& SuppSettlementEntities.CONTRACT_SETTLE_BU.CUSTOMER_SERVICE_CENTER_BU.name().equals(order.getBuCode())) {
				
			} else {
				// 此时打开的是旅游主单详情页
				// 查询旅游主单对应的发票快递主单（不一定存在）
				com.lvmama.order.api.base.vo.ResponseBody<Long> expressOrderIdResponseBody = apiReviseDateFeeRelatedOrderService
						.queryExpressOrderIdByOrderId(new com.lvmama.order.api.base.vo.RequestBody<Long>(orderId));
				if (expressOrderIdResponseBody != null && expressOrderIdResponseBody.getT() != null) {
					// 旅游主单对应的发票快递主单Id
					Long invoiceExpressOrderId = expressOrderIdResponseBody.getT();
					model.addAttribute("invoiceExpressOrderId", invoiceExpressOrderId);
				}
			}
		
		}catch(Exception e){
			log.error("OrderHotelDetailAction showOrderStatusManage error",e);
		}
		return "/order/orderStatusManage/orderHotelDetails";
			 }

	/**
	 * 判断TntOrderChannel是否已经快照
	 * @param orderTagVos
	 * @param orderId
	 * @return
	 */
	private boolean checkHasTntOrderChannel(List<OrderTagVo> orderTagVos, Long orderId) {
		Boolean hasSnapshot = false;
		if (CollectionUtils.isNotEmpty(orderTagVos)) {
			OrderTagVo tagVo = null;
			for (OrderTagVo vo : orderTagVos) {
				if (vo.getObjectId().equals(orderId)) {
					tagVo = vo;
				}
			}
			if (tagVo != null && (Integer.parseInt(tagVo.getTagValue()) > 2)) {
				hasSnapshot = true;
			}
		}
		return hasSnapshot;
	}
	/**
	 *

	 品类：自由行景+酒   单酒店  酒店套餐 自由行机票+酒店

	 所属bu  国内度假事业部

	 打包方式 供应商打包

	 凭证发送渠道：传真，ebk

	 * @param order
	 * @return
	 */
	private boolean isDestBu(OrdOrder order, String bu) {
		if (BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId()) && (CommEnumSet.BU_NAME.DESTINATION_BU.name().equals(bu) || CommEnumSet.BU_NAME.LOCAL_BU.name().equals(bu))) {
			return true;
		}
		return false;
	}

	/**
	 * Added by yangzhenzhong
	 * 重新发送取消申请
	 * @param orderItemId
	 * @return
	 */
	@RequestMapping(value = "/reSendCancelApply")
	@ResponseBody
	public Object reSendCancelApply(Long orderItemId){
		if (orderItemId == null) {
			LOG.error("orderItemId不能为空！");
		}
		try {
			EbkCertif certif = new EbkCertif();
			ResultHandleT<Integer> result = ebkCertifClientService.reSendEbkCertifCancelApply(orderItemId, certif);
			if (result.isFail()) {
				LOG.info("OrderDetailAction::reSendCancelApply_orderItemId="
						+ orderItemId + " errorMessage=" + result.getMsg());
				return new ResultMessage(ResultMessage.ERROR, "改订单不符合重新取消的条件");
			} else {

				comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM, orderItemId, orderItemId, getLoginUserId(),
						"取消单重新取消", ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_EBK.getCode(),ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_EBK.getCnName() , "");
				LOG.info("OrderDetailAction::reSendCancelApply_orderItemId="
						+ orderItemId + " result=SUCCESS");
				return new ResultMessage(ResultMessage.SUCCESS, "重新取消成功");
			}
		} catch (Exception e) {
			LOG.error("{}", e);
			return new ResultMessage(ResultMessage.ERROR, e.getMessage());
		}
	}
	/**
	 * 
	 * 
	 * 
	 * 检验是否已经做过催支付活动
	 * @param order
	 * @return
	 */
	private boolean isDonePaymentAudit(OrdOrder order) {
//		boolean orderStatusNormal=OrderEnum.ORDER_STATUS.NORMAL.getCode().equals(order.getOrderStatus());
//		boolean paymentStatusPayed=OrderEnum.PAYMENT_STATUS.PAYED.equals(order.getPaymentStatus());
//		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("objectId", order.getOrderId());
		param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString());
		param.put("auditType", OrderEnum.AUDIT_TYPE.PAYMENT_AUDIT.getCode());
		param.put("auditStatus",OrderEnum.AUDIT_STATUS.PROCESSED.getCode() );
		List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);
		
		boolean paymentAuditProcessed=comAuditList.size()>0?true:false;
		/*if ( orderStatusNormal && !paymentStatusPayed && !paymentAuditProcessed) {
			isDonePaymentAudit=true;
		}*/
		
		return paymentAuditProcessed;
	}
	
	private boolean isDoneTimePaymentAudit(OrdOrder order) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("objectId", order.getOrderId());
		param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString());
		param.put("auditType", OrderEnum.AUDIT_TYPE.TIME_PAYMENT_AUDIT.getCode());
		param.put("auditStatus",OrderEnum.AUDIT_STATUS.PROCESSED.getCode() );
		List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);
		boolean paymentAuditProcessed=comAuditList.size()>0?true:false;
		
		return paymentAuditProcessed;
	}
	
	
	/**
	 * 
	 * 
	 * 
	 * 查出当前订单有未处理的活动情况
	 * @param order
	 * @return 
	 */
	private Map getUnprocessedAudit(OrdOrder order) {
//		boolean orderStatusNormal=OrderEnum.ORDER_STATUS.NORMAL.getCode().equals(order.getOrderStatus());
//		boolean paymentStatusPayed=OrderEnum.PAYMENT_STATUS.PAYED.equals(order.getPaymentStatus());
//		OrderEnum.AUDIT_STATUS.POOL.getCode() ,
		String[]  auditStatusArray=new String[]{OrderEnum.AUDIT_STATUS.UNPROCESSED.getCode() };
		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("objectId", order.getOrderId());
		param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString());
		param.put("auditStatusArray",auditStatusArray);
		//param.put("auditType", auditType);
		List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);
		Map map=new HashMap<String, Boolean>();
		for (ComAudit comAudit : comAuditList) {
			
			if (OrderEnum.AUDIT_TYPE.INFO_AUDIT.getCode().equals(comAudit.getAuditType())) {
				map.put("INFO_AUDIT", true);
			}else if (OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT.getCode().equals(comAudit.getAuditType())) {
				map.put("RESOURCE_AUDIT", true);
			}else if (OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCode().equals(comAudit.getAuditType())) {
				map.put("CERTIFICATE_AUDIT", true);
			}else if (OrderEnum.AUDIT_TYPE.PAYMENT_AUDIT.getCode().equals(comAudit.getAuditType())) {
				map.put("PAYMENT_AUDIT", true);
			}else if (OrderEnum.AUDIT_TYPE.TIME_PAYMENT_AUDIT.getCode().equals(comAudit.getAuditType())) {
				map.put("TIME_PAYMENT_AUDIT", true);
			}else if (OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.getCode().equals(comAudit.getAuditType())) {
				map.put("CANCEL_AUDIT", true);
			}else if (OrderEnum.AUDIT_TYPE.ONLINE_REFUND_AUDIT.getCode().equals(comAudit.getAuditType())) {
				map.put("ONLINE_REFUND_AUDIT", true);
			}else if (OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.getCode().equals(comAudit.getAuditType())) {
				map.put("BOOKING_AUDIT", true);
			}
		}
		
		
		//boolean isHaveUnprocessedAudit=comAuditList.size()>0?true:false;
		/*if ( orderStatusNormal && !paymentStatusPayed && !paymentAuditProcessed) {
			isDonePaymentAudit=true;
		}*/
		
		return map;
	}

	
	
	/**
	 * 
	 * 
	 * 
	 * 检验是否已经做过催支付活动
	 * @param order
	 * @return
	 */
	private boolean isDoneCancleConfirmedAudit(OrdOrder order) {
//		boolean orderStatusNormal=OrderEnum.ORDER_STATUS.NORMAL.getCode().equals(order.getOrderStatus());
//		boolean paymentStatusPayed=OrderEnum.PAYMENT_STATUS.PAYED.equals(order.getPaymentStatus());
//		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("objectId", order.getOrderId());
		param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString());
		param.put("auditType", OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.getCode());
		param.put("auditStatus",OrderEnum.AUDIT_STATUS.PROCESSED.getCode() );
		List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);
		
		boolean cancleConfirmedProcessed=comAuditList.size()>0?true:false;
		/*if ( orderStatusNormal && !paymentStatusPayed && !paymentAuditProcessed) {
			isDonePaymentAudit=true;
		}*/
		
		return cancleConfirmedProcessed;
	}
	
	/**
	 * 检验是否已经在线退款确认操作
	 * @param order
	 * @return
	 */
	private boolean isDoneOnlineRefundAudit(OrdOrder order) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("objectId", order.getOrderId());
		param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString());
		param.put("auditType", OrderEnum.AUDIT_TYPE.ONLINE_REFUND_AUDIT.getCode());
		param.put("auditStatus",OrderEnum.AUDIT_STATUS.PROCESSED.getCode() );
		List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);
		
		boolean cancleConfirmedProcessed=comAuditList.size()>0?true:false;
		
		return cancleConfirmedProcessed;
	}

	/**
	 * 查看当前登录人是否拥有  凭证确认活动组权限
	 * @param loginUserId
	 * @param orderItem
	 * @return
	 */
	private boolean certificateAuthority(String loginUserId,
			OrdOrderItem orderItem) {
		boolean certificateAuthority=true;
		OrdAuditConfig ordAuditConfig=ordAuditConfigService.findOrdAuditConfig(orderItem.getCategoryId(), OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT.getCode(), loginUserId);
		if (ordAuditConfig==null) {
			certificateAuthority=false;
		}
		return certificateAuthority;
	}
	

	/**
	 * 查看日志（单酒店日志查询合并主订单和子订单的日志）
	 * @param model
	 * @param orderId
	 * @param page
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/logList")
	public String showlogList(Model model,Long orderId,Integer page,HttpServletRequest req){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showlogList>");
		}
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);		
		int pagenum = page == null ? 1 : page;
		//查询合并日志数据
		Map<String, Object> parameters = new HashMap<String, Object>();
		//查询主订单
		parameters.put("objectId",orderId);
		//查询子订单
		parameters.put("parentId", orderId);
		int count = comLogClientService.getTotalCountBySingleHotel(parameters).getReturnContent().intValue();
		Page pageParam = Page.page(count, 10, pagenum);
		pageParam.buildJSONUrl(req);
		parameters.put("_start", pageParam.getStartRows());
		parameters.put("_end", pageParam.getEndRows());
		
		List<ComLog> logList = comLogClientService.querySingleHotleLogByCondition(parameters).getReturnContent();
		pageParam.setItems(logList);
		model.addAttribute("pageParam", pageParam);
		model.addAttribute("logList", logList);
		return "/order/orderStatusManage/findLogList";
	}
	
	/**
	 * 查看日志（单酒店日志查询合并主订单和子订单的日志）(包括关联录音)
	 * @param model
	 * @param orderId
	 * @param page
	 * @param callId
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/showSoundRecList")
	public String showSoundRecList(Model model,Long orderId,Integer page,HttpServletRequest req){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showlogAndSoundRecList>");
		}
/*		Map<String, String> urlParamMap = new HashMap<String, String>();
		urlParamMap.put("objectType", "ORD_ORDER_ORDER");		
		urlParamMap.put("objectId", orderId.toString());		
		urlParamMap.put("sysName", "VST");
		if (page == null || page <= 0){
			urlParamMap.put("curPage", "1");
		}
		else{
			urlParamMap.put("curPage", page.toString());
		}
		Page<ComLog> comLogPage = CallCenterUtils.getRealComLogPageInfo(orderId, page, urlParamMap);*/
		
		Page<ComLog> comLogPage = CallCenterUtils.adapterOrderCallToComLog(orderId, orderCallIdService);
		
		if (comLogPage != null){
			comLogPage.buildJSONUrl(req);
			model.addAttribute("pageParam", comLogPage);
			model.addAttribute("logList", comLogPage.getItems());
		}
		else{
			model.addAttribute("pageParam", null);
			model.addAttribute("logList", null);
		}
		
		return "/order/orderStatusManage/findLogList";
	}
	
	@RequestMapping(value = "/saveCallIdAndOrderId")
	public void saveCallIdAndOrderId(Model model,Long orderId, String callId){
		JSONObject jsonResult=new JSONObject();		
		if (StringUtils.isNotBlank(callId) && orderId != null){
			try{
	    		DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    		String currDateTime = f.format(new Date());
				String createDateOfCallid = getRequest().getParameter("createdateofcallid")==null?currDateTime:this.getRequest().getParameter("createdateofcallid");				
				String content = CallCenterUtils.getContent(orderId, callId, createDateOfCallid);
			    String operatorName = getLoginUserId();
/*				//保存到日志表(先判断是否已经关联过了)
				Map<String, String> urlParamMap = new HashMap<String, String>();
				urlParamMap.put("objectType", ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER.getCode());		
				urlParamMap.put("objectId", orderId.toString());				
				urlParamMap.put("sysName", "VST");
				urlParamMap.put("curPage", "1");
				//urlParamMap.put("content", content);
				urlParamMap.put("parentId", String.valueOf(orderId + CallCenterUtils.PARENT_DELTA));
				
				Page<ComLog> comLogPage = CallCenterUtils.getOrdLogInfo(orderId, 1L, urlParamMap);
				if (comLogPage == null || comLogPage.getItems() == null || comLogPage.getItems().size() <= 0 ||  
						comLogPage.getTotalResultSize() <= 0 || !CallCenterUtils.isExistsCallIdJoin(comLogPage, orderId, callId, createDateOfCallid)){
					//设置了objectType其parentType自动确定
					comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER, orderId + CallCenterUtils.PARENT_DELTA, orderId, operatorName,
							content, ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_EBK.getCode(),ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_EBK.getCnName() , "");				
				}*/
				//保存到order_callid表
				if (orderCallIdService != null){
					OrderCallId orderCallId = new OrderCallId();
					orderCallId.setCallId(callId);
					orderCallId.setOrderId(orderId);
					orderCallId.setOperUserName(operatorName);
					orderCallIdService.insert(orderCallId);
				}
				
				jsonResult.put("err", "N");
				jsonResult.put("msg", "");
				
				
			}
			catch(Exception e){
				jsonResult.put("err", "Y");
				jsonResult.put("msg", e.getMessage().substring(0,100));				
			}
		}
		else{
			jsonResult.put("err", "Y");
			jsonResult.put("msg", "callId或orderId为空");				
		}
		
		sendAjaxResultByJson(jsonResult.toString());
	}	
	
	private List<HotelTimeRateInfo>  handleHouseTimeRateInfo(
			List<OrdOrderHotelTimeRate> orderHotelTimeRateList,OrdOrderItem orderItem) {
		List<ArrayList> settleList=new ArrayList<ArrayList>();
		ArrayList<OrdOrderHotelTimeRate> hotelTimeTateList=null;
		if (orderHotelTimeRateList!=null) {
			for (OrdOrderHotelTimeRate orderHotelTimeRate : orderHotelTimeRateList) {
				
				if (settleList.size()==0) {
					hotelTimeTateList=new ArrayList();
					hotelTimeTateList.add(orderHotelTimeRate);
					settleList.add(hotelTimeTateList);
				}else{
					OrdOrderHotelTimeRate preOrderHotelTimeRate=hotelTimeTateList.get(hotelTimeTateList.size()-1);
					Long prePrice=preOrderHotelTimeRate.getPrice();
					Long price=orderHotelTimeRate.getPrice();
					
					Long preBreakfastTicket= preOrderHotelTimeRate.getBreakfastTicket();
					Long breakfastTicket= orderHotelTimeRate.getBreakfastTicket();
					if (!prePrice.equals(price) || !preBreakfastTicket.equals(breakfastTicket)) {
						hotelTimeTateList=new ArrayList();
						hotelTimeTateList.add(orderHotelTimeRate);
						settleList.add(hotelTimeTateList);
						
					}else{
						hotelTimeTateList.add(orderHotelTimeRate);
	}
				}
			}
		}
		
		
		ResultHandleT<SuppGoodsTimePrice> resultHandleSuppGoodsTimePrice = suppGoodsTimePriceAdapterClientService.getTimePrice(orderItem.getSuppGoodsId(), orderItem.getVisitTime(), false);
		String lastTime="";
		String guaranteeTime=null;
		if (resultHandleSuppGoodsTimePrice.isSuccess()) {
			SuppGoodsTimePrice suppGoodsTimePrice = resultHandleSuppGoodsTimePrice.getReturnContent();
			if (suppGoodsTimePrice!=null) {
				if (suppGoodsTimePrice.getAheadBookTime()!=null) {
					//最晚预定时间
					Date time=CalendarUtils.getEndDateByMinute(suppGoodsTimePrice.getSpecDate(), -suppGoodsTimePrice.getAheadBookTime());
					lastTime=CalendarUtils.getDateFormatString(time, CalendarUtils.YYYY_MM_DD_HH_MM_PATTERN);
				}
				if (SuppGoodsTimePrice.BOOKLIMITTYPE.TIMEOUTGUARANTEE.name()
						.equals(suppGoodsTimePrice.getBookLimitType())
						&& suppGoodsTimePrice.getLatestUnguarTime() != null) {
					// 担保时间
					// Date
					// time=DateUtils.addHours(suppGoodsTimePrice.getSpecDate(),
					// -suppGoodsTimePrice.getLatestUnguarTime().intValue());
					// Date
					// time=CalendarUtils.getEndDateByMinute(suppGoodsTimePrice.getSpecDate(),
					// -suppGoodsTimePrice.getLatestUnguarTime());
					// guaranteeTime=CalendarUtils.getDateFormatString(time,
					// CalendarUtils.HH_MM_PATTERN);
					/*guaranteeTime = suppGoodsTimePrice.getLatestUnguarTime()
							+ "";*/
					//guaranteeTime=orderItem.getContentMap().get(OrderEnum.HOTEL_CONTENT.latestUnguarTime.name())+"";
					Object guaranteeTimObject =orderItem.getContentMap().get(OrderEnum.HOTEL_CONTENT.latestUnguarTime.name());
					if (guaranteeTimObject!=null) {
						guaranteeTime=guaranteeTimObject+"";
					}
				}
			}
		}
		
		List<HotelTimeRateInfo> resultList=new ArrayList<HotelTimeRateInfo>();
		for (ArrayList settleObjList : settleList) {
			
			OrdOrderHotelTimeRate orderHotelTimeRateFirst=(OrdOrderHotelTimeRate)settleObjList.get(0);
			OrdOrderHotelTimeRate orderHotelTimeRateLast=(OrdOrderHotelTimeRate)settleObjList.get(settleObjList.size()-1);
			
			HotelTimeRateInfo hotelTimeRateInfo=new HotelTimeRateInfo();
			hotelTimeRateInfo.setHouseType("");//房型暂时显示为商品名称
			hotelTimeRateInfo.setStartDate(orderHotelTimeRateFirst.getVisitTime());
			hotelTimeRateInfo.setEndDate(DateUtils.addDays(orderHotelTimeRateLast.getVisitTime(), 1));
			hotelTimeRateInfo.setHousePrice(orderHotelTimeRateFirst.getPrice());
			hotelTimeRateInfo.setSettlementPrice(orderHotelTimeRateFirst.getSettlementPrice());
			hotelTimeRateInfo.setBreakfastTicket(orderHotelTimeRateFirst.getBreakfastTicket());
			
			if (resultList.size()==0) {
				hotelTimeRateInfo.setGuaranteeTime(guaranteeTime);
				hotelTimeRateInfo.setLastTime(lastTime);
			}else{
				hotelTimeRateInfo.setGuaranteeTime("");
				hotelTimeRateInfo.setLastTime("");
			}
			
			resultList.add(hotelTimeRateInfo);
			
		}
		
		return resultList;
	}

	/**
	 * @param order
	 * @param orderItem
	 * @param operation
	 * @param cancelCode
	 * @param cancleReason
	 * @return
	 */
	@RequestMapping(value = "/updateOrderStatus")
	@ResponseBody
	public Object updateOrderStatus( HttpServletRequest request,OrdOrder order,OrdOrderItem orderItem,OrderAttachment orderAttachment) {
		if (log.isDebugEnabled()) {
			log.debug("start method<updateOrderStatus>");
		}
		String operation=request.getParameter("operation");
		String cancelCode=request.getParameter("cancelCode");
		String cancleReasonText=request.getParameter("cancleReasonText");
		//String orderRemark=request.getParameter("orderRemark");
		String orderRemark=request.getParameter("orderRemark");
		String orderItemRemark=request.getParameter("orderItemRemark");
		Long orderId=order.getOrderId();
		OrdOrder oldOrder = complexQueryService.queryOrderByOrderId(orderId);
		//OrdOrderItem oldOrderItem=oldOrder.getOrderItemList().get(0);
		
		//UserUser loginUser=this.getLoginUser();
		//String loginUserName=loginUser.getUserName();
		String loginUserId=this.getLoginUserId();
		String newStatus="";
		String name="";
		String msg="当前订单操作状态已被修改:";
		ResultHandle result=new ResultHandle();
		if ("updateOrderRemark".equals(operation)) {
			if(("3.0".equals(oldOrder.getWorkVersion())||"3.1".equals(oldOrder.getWorkVersion()))&&BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(oldOrder.getCategoryId())){
				if(StringUtil.isNotEmptyString(orderItemRemark)){
					OrdOrderItem ordOrderItem=new OrdOrderItem();
					ordOrderItem.setOrderItemId(orderItem.getOrderItemId());
					if(StringUtil.isEmptyString(orderItemRemark)){
						orderItemRemark=" ";
					}
					ordOrderItem.setOrderMemo(orderItemRemark);
					ordOrderUpdateService.updateOrderItemByIdSelective(ordOrderItem);
					lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
							orderItem.getOrderItemId(),
							orderItem.getOrderItemId(),
							loginUserId, 
							"将编号为["+orderItem.getOrderItemId()+"]的子订单，更新子订单备注",
							ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
							ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"更新子订单备注",
							orderItemRemark);
				}
			}else{
				orderStatusManageService.updateOrderMemo(orderId, orderRemark);
				lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
						orderId, 
						orderId, 
						loginUserId, 
						"将编号为["+orderId+"]的订单，更新订单备注", 
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"更新订单备注",
						orderRemark);
			}
			
			
			
		}else if ("infoStatus".equals(operation)) {

		        	if (!oldOrder.getInfoStatus().equals(order.getInfoStatus())) {
						name= INFO_STATUS.INFOPASS.getCnName(oldOrder.getInfoStatus());
						result.setMsg(msg+"信息审核状态 "+name);
					}else{
						boolean isNewSys = order2RouteService.isOrderRouteToNewSys(orderId);
						log.info("updateOrderStatus resourceStatus orderId="+orderId+",isNewSys"+isNewSys);
						if (isNewSys) {
							try{
								com.lvmama.order.api.base.vo.ResponseBody<OrderVo> res = apiOrderQueryService.selectOrder(new RequestBody<Long>(orderId));
								RequestBody<OrderStatusUpdateVo> requestBody = new RequestBody<>();
								OrderStatusUpdateVo orderStatusUpdateVo = new OrderStatusUpdateVo();
								orderStatusUpdateVo.setAssignor(loginUserId);
								orderStatusUpdateVo.setMemo(orderRemark);
								orderStatusUpdateVo.setOrder(res.getT());
								orderStatusUpdateVo.setNewStatus(com.lvmama.order.enums.OrderEnum.INFO_STATUS.INFOPASS.name());
								orderStatusUpdateVo.setAuditType(com.lvmama.order.enums.OrderEnum.AUDIT_TYPE.INFO_AUDIT);
								requestBody.setT(orderStatusUpdateVo);
								apiVstOrdComAuditProcessService.handleOrderAuditWithWorkflow(requestBody);
							}catch(Exception e){
								log.error("updateOrderStatus resourceStatus is error orderId="+orderId,e);
							}
						}else{
						newStatus=INFO_STATUS.INFOPASS.name();
						result = orderLocalService.executeUpdateInfoStatus(oldOrder, newStatus,loginUserId,orderRemark);
						if (result.isSuccess()) {
							String faxFlag=request.getParameter("faxFlag");
							String faxRemark=request.getParameter("messageContent");
							result=orderStatusManageService.saveOrderFaxRemark(orderId,faxFlag, faxRemark, loginUserId, orderRemark);
							if (result.isSuccess()){
								orderLocalService.sendOrderSendFaxMsg(order,"");
							}
							
						}
						
					}
		        }
		}else if ("resourceStatus".equals(operation)) {
			
		        	if (!oldOrder.getResourceStatus().equals(order.getResourceStatus())) {
						name= RESOURCE_STATUS.AMPLE.getCnName(oldOrder.getResourceStatus());
						result.setMsg(msg+"资源审核状态 "+name);
					}else{
						boolean isNewSys = order2RouteService.isOrderRouteToNewSys(orderId);
						log.info("updateOrderStatus resourceStatus orderId="+orderId+",isNewSys"+isNewSys);
						if (isNewSys) {
							try{
								OrderResourceStatusVo orderResourceStatusVo = new OrderResourceStatusVo();
								orderResourceStatusVo.setOrderId(orderId);
								orderResourceStatusVo.setObjectId(orderId);
								orderResourceStatusVo.setNewStatus(com.lvmama.order.enums.OrderEnum.RESOURCE_STATUS.AMPLE.name());
								orderResourceStatusVo.setAuditType(com.lvmama.order.enums.OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT);
								orderResourceStatusVo.setAssignor("SYSTEM");
								orderResourceStatusVo.setResourceRetentionTime(request.getParameter("resourceRetentionTime"));
								RequestBody<OrderResourceStatusVo> requestBody = new RequestBody<OrderResourceStatusVo>(orderResourceStatusVo);
								apiVstOrdComAuditProcessService.handleOrderAuditWithWorkflow(requestBody);
							}catch(Exception e){
								log.error("updateOrderStatus resourceStatus is error orderId="+orderId,e);
							}
						}else{
						newStatus=RESOURCE_STATUS.AMPLE.name();
						String resourceRetentionTime=request.getParameter("resourceRetentionTime");
						
						result =orderLocalService.executeUpdateResourceStatus(orderId, newStatus,resourceRetentionTime, loginUserId, orderRemark);
						
					}
		        }
			
 		}else if ("certificateStatus".equals(operation)) {//凭证确认
		        	//根据该orderId和凭证确认类型查询附件,判读是否已经凭证确认活动过
					if (isDoneCertificate(oldOrder)) {
						result.setMsg(msg+" 凭证确认已经完成,不可再次凭证确认  ");
					}else{
						boolean isNewSys = order2RouteService.isOrderRouteToNewSys(orderId);
						log.info("updateOrderStatus certificateStatus orderId="+orderId+",isNewSys"+isNewSys);
						if (isNewSys) {
							try{
								com.lvmama.order.api.base.vo.ResponseBody<OrderVo> res = apiOrderQueryService.selectOrder(new RequestBody<Long>(orderId));
								RequestBody<OrderCertificateStatusUpdateVo> requestBody = new RequestBody<>();
								OrderCertificateStatusUpdateVo orderStatusUpdateVo = new OrderCertificateStatusUpdateVo();
								
								orderStatusUpdateVo.setOrder(res.getT());
								orderStatusUpdateVo.setNewStatus(com.lvmama.order.enums.OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.name());
								
								orderStatusUpdateVo.setAuditType(com.lvmama.order.enums.OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT);
								orderStatusUpdateVo.setAssignor(loginUserId);
								orderStatusUpdateVo.setMemo(orderRemark);
								OrderAttachmentVo orderAttachmentVo=new OrderAttachmentVo();
								orderAttachmentVo.setOrderId(orderId);
								orderAttachmentVo.setAttachmentType(com.lvmama.order.enums.OrderEnum.ATTACHMENT_TYPE.CERTIFICATE.name());
								orderAttachmentVo.setMemo("凭证确认附件");
								orderAttachmentVo.setCreateTime(Calendar.getInstance().getTime());
								orderAttachmentVo.setFileId(orderAttachment.getFileId());
								orderStatusUpdateVo.setOrderAttachment(orderAttachmentVo);
								requestBody.setT(orderStatusUpdateVo);
								apiVstOrdComAuditProcessService.handleOrderAuditWithWorkflow(requestBody);
							}catch(Exception e){
								log.error("updateOrderStatus certificateStatus is error orderId="+orderId,e);
							}
						}else{
			 			orderAttachment.setOrderId(orderId);
			 			orderAttachment.setAttachmentType(com.lvmama.order.enums.OrderEnum.ATTACHMENT_TYPE.CERTIFICATE.name());
			 			orderAttachment.setMemo("凭证确认附件");
			 			orderAttachment.setCreateTime(Calendar.getInstance().getTime());
			 			orderAttachment.setFileId(orderAttachment.getFileId());
			 			
			 			result = orderLocalService.updateCertificateStatus(oldOrder, orderAttachment, loginUserId, orderRemark);

					}
		        }
		}else if ("paymentAudit".equals(operation)) {//催支付
			
			if (isDonePaymentAudit(oldOrder)) {
				result.setMsg(msg+" 催支付活动状态处于已处理，不可再次催支付  ");
			}else{
				result = orderStatusManageService.updatePaymentAudit(oldOrder, loginUserId, orderRemark,OrderEnum.AUDIT_TYPE.PAYMENT_AUDIT.getCode());
			}
		}else if ("timePaymentAudit".equals(operation)) {//小驴分期催支付
			
			if (isDoneTimePaymentAudit(oldOrder)) {
				result.setMsg(msg+" 小驴分期催支付活动状态处于已处理，不可再次催支付  ");
			}else{
				result = orderStatusManageService.updatePaymentAudit(oldOrder, loginUserId, orderRemark,OrderEnum.AUDIT_TYPE.TIME_PAYMENT_AUDIT.getCode());
			}
		}else if ("cancelStatusConfim".equals(operation)) {//取消订单已确认

			
			result =orderLocalService.updateCancelConfim(oldOrder, loginUserId, orderRemark);
			
			
		}else if ("onlineRefundConfirm".equals(operation)) {//在线退款已确认
			
			
			result =orderLocalService.updateOnlineRefundConfim(oldOrder, loginUserId, orderRemark);
			
			
		}else if ("cancelStatus".equals(operation)) {
			
			if (oldOrder.getOrderStatus().equals(OrderEnum.ORDER_STATUS.CANCEL.name())) {
				name=OrderEnum.ORDER_STATUS.CANCEL.getCnName(oldOrder.getOrderStatus());
				result.setMsg(msg+"订单取消状态  "+name);
			}else{
				result = orderLocalService.cancelOrder(oldOrder.getOrderId(), cancelCode, cancleReasonText, loginUserId, orderRemark);
				
				/*if (result.isSuccess() && oldOrder.getActualAmount()>0) {
					
					this.comMessageService.saveReservationAfterCan(orderId, loginUserId);
				}*/
				if(result.isSuccess()){
					//资源不确定
					if(String.valueOf(VstOrderEnum.ORDER_CANCEL_TYPE_RESOURCE_NO_CONFIM_REFUND_PROCESS).equals(cancelCode)){
						//酒店,目的地BU
						if(BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(oldOrder.getCategoryId())
								&& CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equalsIgnoreCase(oldOrder.getBuCode())){
							Map<String, Object> params =new HashMap<String, Object>();
							Date applyDate =new Date();
					        params.put(OrderRefundConstant.APPLY_DATE, applyDate);
					        params.put(OrderRefundConstant.OPERATOR_NAME,getLoginUser().getUserName());
					        params.put(OrderRefundConstant.CANCEL_CODE,VstOrderEnum.ORDER_CANCEL_TYPE_RESOURCE_NO_CONFIM_REFUND_PROCESS);
					        params.put(OrderRefundConstant.CANCEL_REASON,cancleReasonText);
							orderRefundProcesserAdapter.updateOrderStatusToOrderRefund(oldOrder, params, applyDate,getLoginUserId());
						}
					}
				}
			}
		}
		if (result.isFail()) {
			return new ResultMessage(ResultMessage.ERROR, result.getMsg()+" 请勿再次操作，页面即将刷新");
			
		}

		return ResultMessage.UPDATE_SUCCESS_RESULT;
	}

	/**
	 * 根据orderId,传凭证确认类型附件,判读是否已经做过凭证确认活动
	 * @param order
	 * @return
	 */
	private boolean isDoneCertificate(OrdOrder order) {
		
		boolean result=false;
		/*Map<String,Object> param = new HashMap<String,Object>();
		param.put("orderId",order.getOrderId());
		param.put("attachmentType",OrderEnum.ATTACHMENT_TYPE.CERTIFICATE.name());
		
		List<OrderAttachment> orderAttachmentList=orderAttachmentService.findOrderAttachmentByCondition(param);
		
		if (!orderAttachmentList.isEmpty()) {
			result=true;
		}*/
		
		
		if (OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.name().equals(order.getCertConfirmStatus())) {
			result=true;
		}
		
		
		
		return result;
	}
	
	
	@RequestMapping(value = "/showOrderSendSms")
	public String showOrderSendSms(Model model, HttpServletRequest request){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showOrderSendSms>");
		}
		/*
		String orderIdStr=request.getParameter("orderId");
		Long orderId=NumberUtils.toLong(orderIdStr);
		
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		//OrdOrderItem orderItem=order.getMainOrderItem();
		
		String orderStatus=order.getOrderStatus();
		String infoStatus=order.getInfoStatus();
		//String resourceStatus=order.getResourceStatus();
		String paymentTarget=order.getPaymentTarget();
		

		//boolean resourceStautsPass=true;//OrderEnum.RESOURCE_STATUS.AMPLE.getCode().equals(resourceStatus);
		
		
		String sendNode="";
		if (OrderEnum.INFO_STATUS.INFOPASS.getCode().equals(infoStatus)) {
			sendNode = OrdSmsTemplate.SEND_NODE.AUDIT_PASS.getCode();
		} else if (OrderEnum.ORDER_STATUS.NORMAL.getCode().equals(orderStatus)
				&& SuppGoods.PAYTARGET.PAY.getCode().equals(paymentTarget)) {
			sendNode = OrdSmsTemplate.SEND_NODE.PAY_RESOURCE_AUDIT.getCode();
		} else if (OrderEnum.ORDER_STATUS.NORMAL.getCode().equals(orderStatus)
				&& SuppGoods.PAYTARGET.PREPAID.getCode().equals(paymentTarget)) {
			sendNode = OrdSmsTemplate.SEND_NODE.PREPAID_RESOURCE_AUDIT
					.getCode();
		}
		String content ="";
		if (!StringUtils.isEmpty(sendNode)) {
			content = orderSmsSendService.getContent(orderId, OrdSmsTemplate.SEND_NODE.valueOf(sendNode));
		}
		
		model.addAttribute("content", content);*/
		
		
		return "/order/orderStatusManage/sendOrderSms";
	}	
	@RequestMapping(value = "/showSendOrderFax")
	public String showSendOrderFax(Model model, HttpServletRequest request){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showSendOrderFax>");
		}
		
		String orderIdStr=request.getParameter("orderId");
		Long orderId=NumberUtils.toLong(orderIdStr);
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		OrdOrderItem orderItem=order.getMainOrderItem();
		
		model.addAttribute("order", order);
		model.addAttribute("orderItem", orderItem);
		
		//model.addAttribute("orderAffirmTypeList", OrderEnum.ORDER_AFFIRM_TYPE.values());
		
		return "/order/orderStatusManage/sendOrderFax";
	}	
	@RequestMapping(value = "/showManualSendOrderFax")
	public String showManualSendOrderFaxOrMail(Model model, HttpServletRequest request){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showManualSendOrderFax>");
		}
		
		String orderIdStr=request.getParameter("orderId");
		Long orderId=NumberUtils.toLong(orderIdStr);
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		OrdOrderItem orderItem=order.getMainOrderItem();

		//返回子订单集
		Set<Long> orderItemIdSet = new HashSet<Long>();
		orderItemIdSet.add(orderItem.getOrderItemId());

		//获取子订单合并集合
		List<OrdOrderItem> ordOrderItemList = ebkOrderClientService.getMergeOrderItemList(orderItem).getReturnContent();
		if(null != ordOrderItemList && ordOrderItemList.size() > 0){
			for(OrdOrderItem item : ordOrderItemList){
				orderItemIdSet.add(item.getOrderItemId());
			}
		}
		model.addAttribute("ordOrderItemIdList",orderItemIdSet.toString());

		model.addAttribute("order", order);
		model.addAttribute("orderItem", orderItem);
		
		String faxFlag=(String)orderItem.getContentValueByKey(ORDER_COMMON_TYPE.fax_flag.name());
		String mailFlag=(String)orderItem.getContentValueByKey(ORDER_COMMON_TYPE.mail_flag.name());
		if ("Y".equals(faxFlag)) {
			ResultHandleT<SuppGoods> resultHandleSuppGoods = suppGoodsClientService.findSuppGoodsById(orderItem.getSuppGoodsId(), Boolean.TRUE, Boolean.TRUE);
			if (resultHandleSuppGoods.isSuccess() && resultHandleSuppGoods.getReturnContent() != null) {
				SuppGoods suppGoods = resultHandleSuppGoods.getReturnContent();
				Long faxRuleId= suppGoods.getFaxRuleId();
				ResultHandleT<SuppFaxRule> resultHandleSuppFaxRule = suppFaxClientService.findSuppFaxRuleById(faxRuleId);
				if (resultHandleSuppFaxRule.isSuccess()) {
					model.addAttribute("suppFaxRule", resultHandleSuppFaxRule.getReturnContent());
				} else {
					LOG.info("method showManualSendOrderFax:findSuppFaxRuleById(ID=" + faxRuleId + ") is fail,msg=" + resultHandleSuppFaxRule.getMsg());
				}
			}
		}
		model.addAttribute("faxFlag", faxFlag);
		model.addAttribute("mailFlag", mailFlag);
        model.addAttribute("cancelCertConfirmStatus", request.getParameter("cancelCertConfirmStatus"));
		
		return "/order/orderStatusManage/manualSendOrderFax";
	}

	/*
	@RequestMapping(value = "/sendOrderFax")
	@ResponseBody
	public Object sendOrderFax( HttpServletRequest request,OrdPerson ordPerson){
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<sendOrderFax>");
		}
		Long orderId=NumberUtils.toLong(request.getParameter("orderId"));
		String orderRemark=request.getParameter("orderRemark");
		String assignor=getLoginUserId();
		String orderAffirmType=request.getParameter("orderAffirmType");
		String faxRemark=request.getParameter("messageContent");
		
		
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		OrdOrderItem orderItem=order.getMainOrderItem();
		String faxFlag=(String)orderItem.getContentMap().get(OrderEnum.ORDER_COMMON_TYPE.fax_flag.name());
		
		ResultHandle result=orderStatusManageService.saveOrderFaxRemark(orderId,faxFlag, faxRemark, assignor, orderRemark);
		if (result.isSuccess()){
			orderLocalService.sendOrderSendFaxMsg(order,"");
		}
		if (result.isFail()) {
			return new ResultMessage(ResultMessage.ERROR, result.getMsg());
			
		}

		return ResultMessage.UPDATE_SUCCESS_RESULT;
		
	}*/
	@RequestMapping(value = "/manualSendOrderFax")
	@ResponseBody
	public Object manualSendOrderFax( HttpServletRequest request,OrdPerson ordPerson){
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<manualSendOrderFax>");
		}
		String assignor=getLoginUserId();
		Long orderId=NumberUtils.toLong(request.getParameter("orderId"));
		Long orderItemId=NumberUtils.toLong(request.getParameter("orderItemId"));
		String toFax=request.getParameter("toFax");
		String certifType=request.getParameter("certifType");
		String faxRemark=request.getParameter("messageContent");
		String memo=request.getParameter("memo");
		String certifTypeMemo = memo;
		if (StringUtil.isEmptyString(memo)) {
			certifTypeMemo = EbkCertif.EBK_CERTIFICATE_TYPE.getCnName(certifType);
		}else{
			//校验凭证二次取消的状态，只有是已取消和已拒绝的才能二次取消
			Map<String, Object> params = new HashMap<String,Object>();
			List<Long> orderItemIds = new ArrayList<Long>();
			orderItemIds.add(orderItemId);
			params.put("orderItemIds",orderItemIds);
			List<EbkCertif> certifList = ebkCertifClientService.selectByOrderItemIds(params).getReturnContent();
			if(null != certifList && certifList.size() > 0 ){
				EbkCertif ebkCertif = certifList.get(0);
				String certifStatus = ebkCertif.getCertifStatus();

				if(null != certifStatus && !certifStatus.equals(EbkCertif.EBK_CERTIF_STATUS.CANCEL.toString()) && !certifStatus.equals(EbkCertif.EBK_CERTIF_STATUS.REJECT.toString())){
					return ResultMessage.UPDATE_TWICE_CANCEL_RESULT;
				}
			}
		}


		String addition="";
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		OrdOrderItem orderItem=order.getMainOrderItem();
		String faxFlag=(String)orderItem.getContentMap().get(ORDER_COMMON_TYPE.fax_flag.name());
		
		ResultHandle result=this.orderStatusManageService.manualSendOrderFax(order, toFax,faxRemark, assignor, certifTypeMemo);
		if (result.isSuccess()) {
			if ("Y".equals(faxFlag)) {
				addition=certifType+"_"+toFax;
			}else if("N".equals(faxFlag)){
				addition=certifType;
			}
			if (StringUtil.isEmptyString(memo)) {
				orderLocalService.sendOrderSendFaxMsg(order, addition);
			} else {
				orderLocalService.sendOrderSendTwiceFaxMsg(orderItemId, addition);
			}
			if("N".equals(faxFlag)){
				//分销商渠道ID
				boolean isNotDistribution=true;
				Long[] DISTRIBUTION_CHANNEL_LIST ={10000L,107L,108L,110L,10001L,10002L};
					if(Constant.DIST_BRANCH_SELL==order.getDistributorId()
						&& !(org.apache.commons.lang.ArrayUtils.contains(DISTRIBUTION_CHANNEL_LIST, order.getDistributionChannel().longValue())
						|| "DISTRIBUTOR_TAOBAO".equalsIgnoreCase(order.getDistributorCode()))) {
						isNotDistribution=false;
				}
				LOG.info("-------orderId:"+order.getOrderId()+",isNotDistribution="+isNotDistribution);
				if(order.hasPayed()&&"CONFIRMED".equalsIgnoreCase(order.getCertConfirmStatus())&&isNotDistribution){
					if(BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())){
						if(BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())||
								BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())){
							Map<String, Object> param = new HashMap<String, Object>();
							param.put("objectId", order.getOrderId());
							param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name());
							param.put("auditType", OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name());
							param.put("auditSubtype", OrderEnum.AUDIT_SUB_TYPE.CHANG_ORDER.name());
							String[] auditStatusArray=new String[]{OrderEnum.AUDIT_STATUS.POOL.getCode(),OrderEnum.AUDIT_STATUS.UNPROCESSED.getCode()};
							param.put("auditStatusArray",auditStatusArray );
							List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);
							if(null==comAuditList||comAuditList.size()<1){
								LOG.info("-------生成变更单---------orderId:"+order.getOrderId());
								ComMessage comMessage = new ComMessage();
								comMessage.setMessageContent("订单号"+order.getOrderId()+"，生成变更单.");
								comMessage.setMessageStatus(OrderEnum.MESSAGE_STATUS.UNPROCESSED.name());
								comMessageService.saveReservation(comMessage,null,OrderEnum.AUDIT_SUB_TYPE.CHANG_ORDER.name(),orderItem.getOrderId(),assignor,comMessage.getMessageContent());
							}
						}
					}	
				}	
			}
		}
		
		if (result.isFail()) {
			return new ResultMessage(ResultMessage.ERROR, result.getMsg());
		}

		return ResultMessage.UPDATE_SUCCESS_RESULT;
	}
	
	@RequestMapping(value = "/showForwardSendOrderFax")
	public String showForwardSendOrderFax(Model model, HttpServletRequest request){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showForwardSendOrderFax>");
		}
		
		String orderIdStr=request.getParameter("orderId");
		Long orderId=NumberUtils.toLong(orderIdStr);
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		OrdOrderItem orderItem=order.getMainOrderItem();
		
		model.addAttribute("order", order);
		model.addAttribute("orderItem", orderItem);
		
		ResultHandleT<SuppGoods> resultHandleSuppGoods = suppGoodsClientService.findSuppGoodsById(orderItem.getSuppGoodsId(), Boolean.TRUE, Boolean.TRUE);
		if (resultHandleSuppGoods.isSuccess() && resultHandleSuppGoods.getReturnContent() != null) {
			SuppGoods suppGoods = resultHandleSuppGoods.getReturnContent();
			Long faxRuleId= suppGoods.getFaxRuleId();
			
			if (faxRuleId!=null) {
				ResultHandleT<SuppFaxRule> resultHandleSuppFaxRule = suppFaxClientService.findSuppFaxRuleById(faxRuleId);
				if (resultHandleSuppFaxRule.isSuccess()) {
					model.addAttribute("suppFaxRule", resultHandleSuppFaxRule.getReturnContent());
				}
			}
		}

		return "/order/orderStatusManage/forwardSendOrderFax";
	}
	@RequestMapping(value = "/forwardSendOrderFax")
	@ResponseBody
	public Object forwardSendOrderFax( HttpServletRequest request){
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<forwardSendOrderFax>");
		}
		String assignor=getLoginUserId();
		Long orderId=NumberUtils.toLong(request.getParameter("orderId"));
		String toFax=request.getParameter("toFax");
		String faxRemark=request.getParameter("messageContent");
		Long  certifId=NumberUtils.toLong(request.getParameter("certifId"));
		
		//String addition="";
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		//OrdOrderItem orderItem=order.getMainOrderItem();
		//String faxFlag=(String)orderItem.getContentMap().get(OrderEnum.ORDER_COMMON_TYPE.fax_flag.name());
		
		ResultHandle result=this.orderStatusManageService.manualSendOrderFax(order, toFax,faxRemark, assignor, EBK_CERTIFICATE_CONFIRM_CHANNEL.CHANGE_FAX.getCnName());
		if (result.isSuccess()) {
			//addition="forwardSendOrderFax_"+toFax;
			ebkFaxTaskClientService.ebkOrderCreateFaxTask(certifId, orderId, toFax);
			//orderLocalService.sendOrderSendFaxMsg(order,addition);
		}
		
		if (result.isFail()) {
			return new ResultMessage(ResultMessage.ERROR, result.getMsg());
			
		}

		return ResultMessage.UPDATE_SUCCESS_RESULT;
		
	}
		
	
	@RequestMapping(value = "/showUpdatePerson")
	public String showUpdatePerson(Model model, HttpServletRequest request){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showUpdatePerson>");
		}
		
		String ordPersonId=request.getParameter("ordPersonId");
		
		OrdPerson ordPerson=orderUpdateService.findOrderPersonById(new Long(ordPersonId));
		
		model.addAttribute("ordPerson", ordPerson);
		
		return "/order/orderStatusManage/updatePerson";
	}	
	
	@RequestMapping(value = "/updatePerson")
	@ResponseBody
	public Object updatePerson( HttpServletRequest request,OrdPerson ordPerson){
		
		/*String ordPersonId=request.getParameter("ordPersonId");
		
		OrdPerson ordPersonObj=new OrdPerson()	;
		ordPerson.setOrdPersonId(new Long(ordPersonId));*/
		
		orderUpdateService.updateOrderPerson(ordPerson);
		
		return ResultMessage.UPDATE_SUCCESS_RESULT;
		
	}
	@RequestMapping(value = "/showYLDeduct")
	public String showYLDeduct(Model model, HttpServletRequest request) throws Exception{
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showYLDeduct>");
		}
		
		Long orderId=NumberUtils.toLong(this.getRequestParameter("orderId", request));
		Long orderItemId=NumberUtils.toLong(this.getRequestParameter("orderItemId", request));
		com.lvmama.dest.dock.response.ResponseBody<Map<String, Object>> resultHandle=orderDetailClientService.findOrderDetail(orderId,orderItemId);
		Map<String, Object>  resultMap=resultHandle.getT();

		String processType="";
		String status="";
		if (resultMap!=null) {

			processType=(String) resultMap.get(Constant.ORDER_PROCESS_TYPE);
			status=(String) resultMap.get(Constant.ORDER_STATUS);
			if (StringUtils.isEmpty(processType)) {
				processType="";
			}else{
				processType=Constant.PROCESS_TYPE.getCnName(processType);
			}
			if (StringUtils.isEmpty(status)) {
				status="";
			}else{
				status=Constant.STATUS.getCnName(status);
			}
		}else{
			resultMap=new HashMap<String, Object>();
		}

		
		model.addAttribute("YLMap",resultMap);
		model.addAttribute("processType",processType);
		model.addAttribute("status",status);
		
		
		
		return "/order/orderStatusManage/viewYLDeduct";
	}

    @RequestMapping(value = "/showAddMessage")
    public String showAddMessage(Model model, Long orderId, HttpServletRequest request){
        if (LOG.isDebugEnabled()) {
            LOG.debug("start method<showAddMessage>");
        }
        OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
        if (OrdOrderUtils.isDestBuFrontOrderNew(order)) {
            List<Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_SUB_TYPE> list = new ArrayList<Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_SUB_TYPE>();
            list.add(Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_SUB_TYPE.CONFIRM_APPROVAL);
            model.addAttribute("auditTypeList", list);
        } else {
            model.addAttribute("auditTypeList", OrderEnum.AUDIT_TYPE.values());
        }
		
		
		return "/order/orderStatusManage/addMessage";
	}	
	
	@RequestMapping(value = "/addMessage")
	@ResponseBody
	public Object addMessage( HttpServletRequest request,ComMessage comMessage){
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<addMessage>");
		}
		//String orderId=request.getParameter("orderId");
		Long orderId=NumberUtils.toLong(request.getParameter("orderId"));
		Long orderItemId=NumberUtils.toLong(request.getParameter("orderItemId"));
		String auditType=request.getParameter("auditType");
		String orderRemark=request.getParameter("orderRemark");
		LOG.info("orderId="+orderId+",orderItemId="+orderItemId+",auditType="+auditType+",orderRemark="+orderRemark);
		String userName=comMessage.getReceiver();
		if (!StringUtils.isEmpty(userName)) {
			PermUser permUser=permUserServiceAdapter.getPermUserByUserName(userName);
			if (permUser==null) {
				String message="此员工不存在";
				return new ResultMessage(ResultMessage.ERROR,message);
			}
		}
		
		String assignor=getLoginUserId();
        try {
			 comMessageService.saveChildReservation(comMessage, auditType,
                     orderId, orderItemId, assignor,orderRemark);
		} catch (BusinessException e) {
			// TODO: handle exception
			if (Constants.NO_PERSON.equals(e.getMessage())) {
				String message="找不到可以接单的人";
				return new ResultMessage(ResultMessage.ERROR,message);
			}else{
				throw e;
			}
		}
		/*if (n==0) {
			String message="找不到可以接单的人";
			return new ResultMessage(ResultMessage.ERROR,message);
		}else{
			return ResultMessage.ADD_SUCCESS_RESULT;
		}*/
		
		return ResultMessage.ADD_SUCCESS_RESULT;
	}

	@RequestMapping(value = "/findComMessageList")
	public String findComMessage(Model model,Long orderId,Integer page,HttpServletRequest req){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<findComMessageList>");
		}
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		Long[] auditIdArray = findBookingAuditIds(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString(),orderId);
		Long[] itemAuditIdArray = getHotelItemsFormAudit(order);
		//合并主订单和子订单数组
		Long[] allAuditIdArrays = (Long[]) ArrayUtils.addAll(auditIdArray, itemAuditIdArray);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("messageStatus",OrderEnum.MESSAGE_STATUS.UNPROCESSED.getCode());
		parameters.put("auditIdArray",allAuditIdArrays);
		
		int count=comMessageService.findComMessageCount(parameters);
		int pagenum = page == null ? 1 : page;
		Page pageParam = Page.page(count, 10, pagenum);
		//pageParam.buildUrl(req);
		pageParam.buildJSONUrl(req);
		parameters.put("_start", pageParam.getStartRows());
		parameters.put("_end", pageParam.getEndRows());
				
		List<ComMessage> messageList=comMessageService.findComMessageList(parameters);

		List<ComMessageVO> reusltList=new ArrayList<ComMessageVO>();

		if(messageList != null){
			for (int i = 0; i < messageList.size(); i++) {

				ComMessage comMessage=messageList.get(i);

				ComMessageVO comMessageVO=new ComMessageVO();
				BeanUtils.copyProperties(comMessage,comMessageVO );


				ComAudit comAudit=orderAuditService.queryAuditById(comMessage.getAuditId());
				String auditSubtypeName="";
				if (!StringUtils.isEmpty(comAudit.getAuditSubtype())) {
					auditSubtypeName=OrderEnum.AUDIT_SUB_TYPE.getCnName(comAudit.getAuditSubtype());
				}
				comMessageVO.setAuditSubTypeName(auditSubtypeName);

				reusltList.add(comMessageVO);
			}
		}

		model.addAttribute("messageList", reusltList);
		pageParam.setItems(reusltList);
		model.addAttribute("pageParam", pageParam);
		
		return "/order/orderStatusManage/findComMessageList";
		
	}

	/**
	 * 
	 * 查询出未分配或者待处理的 预订通知 auditIdArray
	 * @param orderId
	 * @return
	 */
	private Long[] findBookingAuditIds(String objectType,Long orderId) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("objectId", orderId);
		param.put("objectType", objectType);
		param.put("auditType", OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.getCode());
		String[] auditStatusArray=new String[]{OrderEnum.AUDIT_STATUS.POOL.getCode(),OrderEnum.AUDIT_STATUS.UNPROCESSED.getCode()};
		param.put("auditStatusArray",auditStatusArray );
		List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);
		
		Long[] auditIdArray=new Long[comAuditList.size()];
		for (int i = 0; i < comAuditList.size(); i++) {
			auditIdArray[i]=comAuditList.get(i).getAuditId();
		}
		return auditIdArray;
	}	

	
	@RequestMapping(value = "/updateMessage")
	@ResponseBody
	public Object updateMessage( HttpServletRequest request,ComMessage comMessage){
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<updateMessage>");
		}
		String orderId=request.getParameter("orderId");
		String orderRemark="预定通知已处理";
		String assignor=getLoginUserId();
		String messageIds=request.getParameter("messageIds");
		String auditIds=request.getParameter("auditIds");
		String[] messageIdArray=messageIds.split(",");
		String[] auditIdArray=auditIds.split(",");
		int n=comMessageService.updateReservationListProcessed(orderId,messageIdArray,auditIdArray, assignor,orderRemark);
		
		if (n!=messageIdArray.length) {
			String message="更新失败";
			return new ResultMessage(ResultMessage.ERROR,message);
		}else{
			return ResultMessage.UPDATE_SUCCESS_RESULT;
		}
		
		
	}
	
	
	@RequestMapping(value = "/showAddCertificate")
	public String showAddCertificate(Model model, HttpServletRequest request){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showAddCertificate>");
		}
		
		model.addAttribute("certificateTypeList", OrderEnum.CERTIFICATE_TYPE.values());
		
		
		
		return "/order/orderStatusManage/addCertificate";
	}	
	
	
	/**
	 * 查询单酒店中子订单的预订通知（未分配OR待处理）
	 * @param order
	 * @return
	 */
	public Long[] getHotelItemsFormAudit(OrdOrder order){
		List<Long> itemIdList = new ArrayList<Long>();
		//如果是单酒店订单
		if(isHotelUrl(order)){									
			List<OrdOrderItem> itemList = order.getOrderItemList();
			//遍历子订单
			if(CollectionUtils.isNotEmpty(itemList)){
				Long[] itemAuditIds = null;
				for (OrdOrderItem ordOrderItem : itemList) {
					itemAuditIds = findBookingAuditIds(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.toString(), ordOrderItem.getOrderItemId());		
					itemIdList.addAll(Arrays.asList(itemAuditIds));
				}			
			}			
		}
		return itemIdList.toArray(new Long[itemIdList.size()]);
	}
	
	
	/**
	 * 判断订单是否走单酒店的URL
	 * @param order
	 * @return
	 */
	private boolean isHotelUrl(OrdOrder order) {
		//主订单品类为单酒店
		if(BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())) {
			return true;
		}	
		//三月份的订单key为null，都是酒店订单，走酒店品类的流转页面
		String processKey=order.getProcessKey();
		if (StringUtils.isEmpty(processKey)) {
			return true;
		}
		//子订单中只有一个酒店子订单并且key为single_hotel_prepaid_order或者single_hotel_pay_order的时候走单独酒店品类的流转页面
		/*List<OrdOrderItem> orderItemsList = ordOrderUpdateService.queryOrderItemByOrderId(order.getOrderId());
		if (("single_hotel_prepaid_order".equals(processKey) || "single_hotel_pay_order"
				.equals(processKey))
				&& CollectionUtils.isNotEmpty(orderItemsList)
				&& orderItemsList.size() == 1
				&& 1L == orderItemsList.get(0).getCategoryId())
		{
			return true;
		}*/
		
		return false;
	}
	
	/**
	 * 后台订单详情页显示赠品.
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/findBuyPresentList")
	public String findBuyPresentsOfOrder (Model model, HttpServletRequest request, Long orderId) {
		List<PromBuyPresentCouponVo> buyPresentCouponVos = getOrderPromBuyPresents (orderId);
		model.addAttribute("buyPresentCouponVos", buyPresentCouponVos);
		return "/order/orderStatusManage/allCategory/findBuyPresentList";
	}
	
	private List<PromBuyPresentCouponVo> getOrderPromBuyPresents (Long orderId) {
		List<PromBuyPresentCouponVo> buyPresents = null;
		ResultHandleT<List<PromBuyPresentCouponVo>> result = buyPresentClientService.queryActivityBuyOrderId(orderId);
		if (result.isSuccess()) {
			buyPresents = result.getReturnContent();
		} else {
			LOG.info("获取赠品列表出错：orderId=" + orderId + "  " + result.getMsg());
		}
		return buyPresents;
	}

	private void setCreditTag(OrdOrderItem orderItem) {
		Long orderItemId=orderItem.getOrderItemId();
		try {
			RequestBody<OrderTagVo> req=new RequestBody<>();
			OrderTagVo vo=new OrderTagVo();
			vo.setObjectId(orderItemId);
			vo.setObjectType(TagEnum.ORD_OBJECT_TAG.ORD_ORDER_ITEM.name());
			vo.setTagType(TagEnum.ORD_ORDER_TAG.ORD_CREDIT_TAG.name());//信用住
			req.setT(vo);
			com.lvmama.order.api.base.vo.ResponseBody<List<OrderTagVo>> res=apiOrderTagService.queryOrderTags(req);
			if(null==res||res.isFailure()){
				LOG.info("查询品牌馆标识返回空orderItemId="+orderItemId);
				orderItem.setCreditTag("N");
			}else if(res.isSuccess()){
				List<OrderTagVo> omev=res.getT();
				LOG.info("查询品牌馆标识返回成功orderItemId="+orderItemId+",返回值:"+res.toString());
				if(null==omev||omev.isEmpty()){
					orderItem.setCreditTag("N");
				}else{
					String markFlag=omev.get(0).getTagValue();
					orderItem.setCreditTag(markFlag);
				}

			}

		}catch (Exception e){
			LOG.error("查询品牌馆异常orderItemId="+orderItemId,e);
		}
	}
	
	/**
	 * 处理订单的当前状态
	 *
	 * @param ordOrderItem
	 * @return
	 */
	private String buildCurrentStatus(OrdOrder order,OrdOrderItem ordOrderItem) {
		StringBuilder builder = new StringBuilder();
		OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(order.getOrderId());
		/* 若子单为意外险游玩人后置子单，当意外险弃保时，子单状态显示已取消 */
		String travDelayFlag = null;
		String travDelayStatus = null;
		
		if (null != ordAccInsDelayInfo) {
			travDelayFlag = ordAccInsDelayInfo.getTravDelayFlag();
			travDelayStatus = ordAccInsDelayInfo.getTravDelayStatus();
		}

		if (StringUtils.isNotBlank(travDelayFlag)
				&& "Y".equalsIgnoreCase(travDelayFlag)
				&& StringUtils.isNotBlank(travDelayStatus)
				&& travDelayStatus.equalsIgnoreCase(ORDER_TRAV_DELAY_STATUS.ABANDON.name())) {
			Map<String, Object> contentMap = ordOrderItem.getContentMap();

			Object destBuAccFlag = ordOrderItem.getContentValueByKey("destBuAccFlag");
			if (null != destBuAccFlag && StringUtils.equalsIgnoreCase(destBuAccFlag.toString(), "Y")) {
				return "已取消";
			}
		}

		if (order.hasCanceled()) {
			return "废单";
		}

		//组装审核状态
		if(INFO_STATUS.UNVERIFIED.name().equals(ordOrderItem.getInfoStatus())
				&& RESOURCE_STATUS.UNVERIFIED.name().equals(ordOrderItem.getResourceStatus())){
			builder.append("未审核");
		}else if(INFO_STATUS.INFOFAIL.name().equals(ordOrderItem.getInfoStatus())
				|| RESOURCE_STATUS.LOCK.name().equals(ordOrderItem.getResourceStatus())){
			builder.append("审核不通过");
		}else if(INFO_STATUS.INFOPASS.name().equals(ordOrderItem.getInfoStatus())
				&& RESOURCE_STATUS.AMPLE.name().equals(ordOrderItem.getResourceStatus())){
			builder.append("审核通过");
		}else{
			builder.append("审核中");
		}

		builder.append("<br>");

		builder.append("资源审核（");
		//组装资源确认状态
		if(RESOURCE_STATUS.UNVERIFIED.name().equals(ordOrderItem.getResourceStatus())){
			builder.append("未审核");
		}else if(RESOURCE_STATUS.AMPLE.name().equals(ordOrderItem.getResourceStatus())){
			builder.append("确认通过");
		}else{
			builder.append("确认不通过");
		}

		builder.append("）<br>");

		builder.append("信息审核（");

		//组装信息确认状态
		if(INFO_STATUS.UNVERIFIED.name().equals(ordOrderItem.getInfoStatus())){
			builder.append("未审核");
		}else if(INFO_STATUS.INFOPASS.name().equals(ordOrderItem.getInfoStatus())){
			builder.append("确认通过");
		}else{
			builder.append("确认不通过");
		}

		builder.append("）<br>");

		//门票业务类订单使用状态
		List<OrdTicketPerform> resultList = null;
		//门票业务类订单使用状态
		Map<String,Object> performMap = ordOrderItem.getContentMap();
		String categoryCode =  (String) performMap.get(ORDER_COMMON_TYPE.categoryCode.name());
		if (ProductCategoryUtil.isTicket(categoryCode)) {
			builder.append("使用状态（");
			resultList = complexQueryService.selectByOrderItem(ordOrderItem.getOrderItemId());
			String performStatusName= OrderEnum.PERFORM_STATUS_TYPE.getCnName(OrderUtils.calPerformStatus(resultList,order,ordOrderItem)) ;
			//门票业务类订单使用状态
			builder.append(performStatusName);
			builder.append("）");
		}
		//酒店业务类订单使用状态
		if (ProductCategoryUtil.isHotel(categoryCode)) {
			builder.append("使用状态（");
			String performStatusName= OrderEnum.PERFORM_STATUS_TYPE.getCnName(ordOrderItem.getPerformStatus()) ;
			//perFormStatusList.add(performStatusName);

			//酒店业务类订单使用状态
			builder.append(performStatusName);
			builder.append("）");
		}
		return builder.toString();
	}

	private String getGoodsName(Map<Long, OrdOrderItem> ordItemMap, List<Long> itemIdList, Long ordPersonId) {
		Map<String, Object> paramsOrdPerson = new HashMap<String, Object>();
		paramsOrdPerson.put("ordPersonId", ordPersonId);
		paramsOrdPerson.put("orderItemIdArray", itemIdList );
		List<OrdItemPersonRelation> ordItemPersonRelationList = ordItemPersonRelationService.findOrdItemPersonRelationList(paramsOrdPerson);

		StringBuffer goodsName = new StringBuffer();
		if (CollectionUtils.isNotEmpty(ordItemPersonRelationList)) {
			for (int i = 0; i < ordItemPersonRelationList.size(); i++) {

				OrdItemPersonRelation ordItemPersonRelation =ordItemPersonRelationList.get(i);
				OrdOrderItem ordItem=ordItemMap.get(ordItemPersonRelation.getOrderItemId());
				if (i>0) {
					goodsName.append("</br>");
				}

				goodsName.append(ordItem.getSuppGoodsName());

			}

		}
		return goodsName.toString();
	}

	private String buildProductName(OrdOrderItem ordOrderItem) {
		String productName = "未知产品名称";
		if (null != ordOrderItem) {
			Map<String, Object> contentMap = ordOrderItem.getContentMap();
			String branchName = (String) contentMap.get(ORDER_COMMON_TYPE.branchName.name());
			if (branchName != null && !"".equals(branchName)) {
				// 如果是交通接驳，包含商品这一列显示 产品 + 商品
				if (BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(ordOrderItem.getCategoryId())) {
					productName = ordOrderItem.getProductName() + "-" + ordOrderItem.getSuppGoodsName();
				} else {
					productName = ordOrderItem.getProductName() + "-" + branchName + "("
							+ ordOrderItem.getSuppGoodsName() + ")";
				}
			} else {
				productName = ordOrderItem.getProductName() + "-" + ordOrderItem.getSuppGoodsName();
			}
		}
		return productName;
	}

	/**
	 * 构建子订单购买商品数量、销售单价与总价
	 *
	 * @param order
	 * @param itemIdWithMulPriceMap
	 * @return
	 */
	private void buildBuyCountAndPrice(OrdOrderItem orderItem, OrderMonitorRst orderMonitorRst, Map<Long, List<OrdMulPriceRate>> itemIdWithMulPriceMap) {

		StringBuffer buyCount = new StringBuffer();
		StringBuffer buyItemPrice = new StringBuffer(); // 销售价
		Long buyItemTotalPrice = 0L; // 总价
		String[] priceTypeArray = new String[] {
				ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCode(),
				ORDER_PRICE_RATE_TYPE.PRICE_CHILD.getCode(),
				ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.getCode()};

		Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
		paramsMulPriceRate.put("orderItemId", orderItem.getOrderItemId());
		paramsMulPriceRate.put("priceTypeArray",priceTypeArray );

		List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
		if (CollectionUtils.isNotEmpty(ordMulPriceRateList)) {
			//保存子单id与多价格的对应关系，用于后面排序
			itemIdWithMulPriceMap.put(orderItem.getOrderItemId(), ordMulPriceRateList);
			for (int i = 0; i < ordMulPriceRateList.size(); i++) {
				OrdMulPriceRate ordMulPriceRate = ordMulPriceRateList.get(i);
				if (i>0) {
					buyCount.append("</br>");
					buyItemPrice.append("</br>");
				}
				buyCount.append(ORDER_PRICE_RATE_TYPE.getCnName(ordMulPriceRate.getPriceType())).append(" ").append(ordMulPriceRate.getQuantity()).append("份");;
				buyItemPrice.append(ORDER_PRICE_RATE_TYPE.getCnName(ordMulPriceRate.getPriceType())).append(":").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");
				buyItemTotalPrice += ordMulPriceRate.getPrice()*ordMulPriceRate.getQuantity();
			}
		}else{
			buyCount.append(orderItem.getQuantity()).append("份");
			buyItemPrice.append(PriceUtil.trans2YuanStr(orderItem.getPrice())).append("元");
			buyItemTotalPrice += orderItem.getPrice()*orderItem.getQuantity();
		}

		orderMonitorRst.setBuyItemCount(buyCount.toString());
		orderMonitorRst.setBuyItemPrice(buyItemPrice.toString());
		orderMonitorRst.setBuyItemTotalPrice(PriceUtil.trans2YuanStr(buyItemTotalPrice)+"元");

	}
	
	
	private String buildVisitTime(OrdOrderItem orderItem) {
		if (null != orderItem) {
			if (orderItem.hasTicketAperiodic()) {
				// 取通关时间
				List<OrdTicketPerform> resultList = complexQueryService
						.selectByOrderItem(orderItem.getOrderItemId());
				if (CollectionUtils.isNotEmpty(resultList)) {
					Date performTime = resultList.get(0).getPerformTime();
					if (performTime != null) {
						return DateUtil.SimpleFormatDateToString(performTime);
					}
				}
				String visitTimeStr = (String) orderItem.getContentMap().get(
						ORDER_TICKET_TYPE.goodsExpInfo.name());
				// 期票不可游玩日期描述
				String unvalidDesc = (String) orderItem.getContentMap().get(
						ORDER_TICKET_TYPE.aperiodic_unvalid_desc.name());
				if (StringUtil.isNotEmptyString(unvalidDesc)) {
					visitTimeStr += "</br>(不适用日期:" + unvalidDesc + ")";
				}
				return visitTimeStr;
			} else {
				StringBuffer visitTime = new StringBuffer();
				visitTime.append(DateUtil.formatDate(orderItem.getVisitTime(), "yyyy-MM-dd"));
				// 马戏票场次信息展示
				String startTime = (String) orderItem.getContentMap().get(ORDER_TICKET_TYPE.circusActStartTime.name());
				String endTime = (String) orderItem.getContentMap().get(ORDER_TICKET_TYPE.circusActEndtime.name());
				if (StringUtil.isNotEmptyString(startTime)) {
					String regex = " ";
					String[] split = startTime.split(regex);
					startTime = split[split.length - 1];
					visitTime.append("</br>").append(startTime);
					if (StringUtil.isNotEmptyString(endTime)) {
						split = endTime.split(regex);
						endTime = split[split.length - 1];
						visitTime.append(" - ").append(endTime);
					}
				}
				return visitTime.toString();
			}
		}
		return "未知日期";
	}

}
