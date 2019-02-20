package com.lvmama.vst.order.web;


import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.lvmama.comm.bee.po.distribution.DistributorInfo;
import com.lvmama.comm.bee.po.ord.OrdRefundment;
import com.lvmama.comm.pet.fs.client.FSClient;
import com.lvmama.comm.pet.fs.vo.ComFile;
import com.lvmama.comm.pet.po.pay.PayPayment;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.comm.search.vst.vo.SuppGoodsRefund;
import com.lvmama.comm.stamp.vo.*;
import com.lvmama.comm.sync.util.BizEnum;
import com.lvmama.comm.vst.VstOrderEnum;
import com.lvmama.comm.vst.vo.VstInvoiceAmountVo;
import com.lvmama.coupon.api.order.service.CouponOrderService;
import com.lvmama.crm.enumerate.CsVipUserIdentityTypeEnum;
import com.lvmama.crm.enumerate.SrvMemberEnum;
import com.lvmama.crm.outer.user.api.CrmUserLabelService;
import com.lvmama.crm.service.CsVipDubboService;
import com.lvmama.dest.dock.request.order.RequestSuppOrder;
import com.lvmama.dest.dock.service.interfaces.ApiSuppOrderService;
import com.lvmama.finance.comm.finance.po.SetSettlementItem;
import com.lvmama.finance.comm.vst.service.SettlementService;
import com.lvmama.log.client.QueryLogClientService;
import com.lvmama.log.comm.bo.ComLogPams;
import com.lvmama.order.enums.TagEnum;
import com.lvmama.order.enums.ticket.ItemCancelEnum;
import com.lvmama.order.grouppurchase.api.order.IApiGroupOrderDetailService;
import com.lvmama.order.grouppurchase.vo.GroupPurchaseOrderInfoVo;
import com.lvmama.order.process.api.cancel.IApiOrderCancelProcessService;
import com.lvmama.order.process.api.ebk.IApiEbkEmailProcessService;
import com.lvmama.order.service.api.comm.ebk.IApiEbkEmailQueryService;
import com.lvmama.order.service.api.comm.order.IApiOrdOrderCommissionQueryService;
import com.lvmama.order.service.api.comm.order.IApiOrderItemCancelService;
import com.lvmama.order.process.api.express.IApiOrderExpressProcessService;
import com.lvmama.order.service.api.comm.order.IApiOrderQueryService;
import com.lvmama.order.service.api.comm.order.IApiOrderTagService;
import com.lvmama.order.service.api.ticket.IApiReviseDateFeeRelatedOrderService;
import com.lvmama.order.snap.api.IApiOrderSnapshotService;
import com.lvmama.order.snap.vo.SnapshotOrderItemVo;
import com.lvmama.order.snap.vo.SnapshotOrderVo;
import com.lvmama.order.vo.comm.*;
import com.lvmama.order.vo.comm.cancel.OrderCancelParamVo;
import com.lvmama.order.vo.comm.courier.OrdExpressVo;
import com.lvmama.order.vo.comm.status.OrdEbkEmailVo;
import com.lvmama.order.vo.ticket.ReviseDateFeeRelatedOrderVo;
import com.lvmama.pay.api.service.TradeOrderQueryService;
import com.lvmama.pay.api.vo.ProcessResult;
import com.lvmama.pay.api.vo.TradeTradeOrderVO;
import com.lvmama.scenic.api.back.client.goods.service.ScenicSuppGoodsExpClientService;
import com.lvmama.scenic.api.back.goods.po.ScenicSuppGoodsDesc;
import com.lvmama.scenic.api.back.goods.po.SuppGoodsDescJson;
import com.lvmama.scenic.api.back.goods.service.ScenicSuppGoodsDescJsonService;
import com.lvmama.scenic.api.back.goods.service.ScenicSuppGoodsDescService;
import com.lvmama.scenic.api.play.good.service.ScenicSportSuppGoodsService;
import com.lvmama.scenic.api.play.good.vo.SportGoodsFormattedDescVo;
import com.lvmama.visa.api.base.VisaResultHandleT;
import com.lvmama.visa.api.service.VisaApprovalClientService;
import com.lvmama.visa.api.vo.approval.VisaApprovalVo;
import com.lvmama.vst.back.biz.po.BizBranch;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizDictDef;
import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.BranchClientService;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.biz.service.DictDefClientService;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.dist.service.DistributorClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsHotelAdapterClientService;
import com.lvmama.vst.back.client.ord.service.*;
import com.lvmama.vst.back.client.passport.service.PassportProductService;
import com.lvmama.vst.back.client.passport.service.PassportService;
import com.lvmama.vst.back.client.prod.service.ProdProductBranchClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductPropClientService;
import com.lvmama.vst.back.client.prom.service.BuyPresentClientService;
import com.lvmama.vst.back.client.pub.service.ComLogClientService;
import com.lvmama.vst.back.client.supp.service.SuppFaxClientService;
import com.lvmama.vst.back.client.supp.service.SuppMailClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.dist.po.Distributor;
import com.lvmama.vst.back.ebooking.vo.fax.EbkFaxVO;
import com.lvmama.vst.back.goods.po.FinanceInterestsBonus;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsSaleRe;
import com.lvmama.vst.back.goods.po.SuppGoods.GOODSSPEC;
import com.lvmama.vst.back.goods.po.SuppGoods.SPECIAL_TICKET_TYPE;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.utils.SuppGoodsRefundTools;
import com.lvmama.vst.back.goods.vo.SuppGoodsRefundVO;
import com.lvmama.vst.back.goods.vo.SuppGoodsRescheduleVO;
import com.lvmama.vst.back.order.exception.OrderException;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.po.OrdOrderDownpay.PAY_STATUS;
import com.lvmama.vst.back.order.po.OrdOrderDownpay.PAY_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.*;
import com.lvmama.vst.back.order.vo.OrdTravelContractVo;
import com.lvmama.vst.back.passport.po.PassProvider;
import com.lvmama.vst.back.play.connects.po.OrderConnectsServiceProp;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdProductBranch;
import com.lvmama.vst.back.prod.po.ProdProductProp;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.back.prom.po.OrdPayPromotion;
import com.lvmama.vst.back.prom.vo.PromBuyPresentCouponVo;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.back.supp.po.SuppFaxRule;
import com.lvmama.vst.back.supp.po.SuppMailRule;
import com.lvmama.vst.back.supp.po.SuppOrderResult;
import com.lvmama.vst.back.supp.po.SuppSettlementEntities;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.enumeration.CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.po.ComFileMap;
import com.lvmama.vst.comm.utils.*;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.json.JSONOutput;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.OrderUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.utils.web.HttpServletLocalThread;
import com.lvmama.vst.comm.utils.web.HttpsUtil;
import com.lvmama.vst.comm.vo.*;
import com.lvmama.vst.comm.vo.Constant.BU_NAME;
import com.lvmama.vst.comm.vo.econtract.TravelContractVO;
import com.lvmama.vst.comm.vo.order.*;
import com.lvmama.vst.comm.vo.pass.EbkEmailAttch;
import com.lvmama.vst.comm.vo.pass.EbkTicketPost;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.ebooking.client.ebk.serivce.*;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif.EBK_CERTIFICATE_CONFIRM_CHANNEL;
import com.lvmama.vst.ebooking.ebk.po.EbkCertifItem;
import com.lvmama.vst.ebooking.ebk.po.EbkUser;
import com.lvmama.vst.flight.client.order.service.FlightOrderProcessService;
import com.lvmama.vst.order.confirm.service.IOrdItemConfirmStatusService;
import com.lvmama.vst.order.contract.service.IOrderContractSnapshotService;
import com.lvmama.vst.order.contract.service.IOrderElectricContactService;
import com.lvmama.vst.order.contract.service.impl.AbstractOrderTravelElectricContactService;
import com.lvmama.vst.order.contract.vo.CruiseTourismContractDataVO;
import com.lvmama.vst.order.contract.vo.OutboundTourContractDataVO;
import com.lvmama.vst.order.contract.vo.TeamWithInTerritoryContractDataVO;
import com.lvmama.vst.order.dao.OrdFormInfoDao;
import com.lvmama.vst.order.po.OrderCallId;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.service.refund.IOrderRefundCommMethodService;
import com.lvmama.vst.order.service.refund.IOrderRefundRulesService;
import com.lvmama.vst.order.service.refund.OrderRefundBatchDetailService;
import com.lvmama.vst.order.service.refund.OrderRefundBatchService;
import com.lvmama.vst.order.service.refund.adapter.OrderRefundProcesserAdapter;
import com.lvmama.vst.order.service.reschedule.IOrderRescheduleService;
import com.lvmama.vst.order.utils.ApportionUtil;
import com.lvmama.vst.order.utils.CallCenterUtils;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.utils.RestClient;
import com.lvmama.vst.order.web.service.OrderDetailApportionService;
import com.lvmama.vst.pet.adapter.*;
import com.lvmama.vst.supp.client.service.OrderDetailClientService;
import com.lvmama.vst.supp.client.service.SupplierOrderOtherService;
import com.lvmama.vst.suppTicket.client.product.po.IntfStylOrderInfo;
import com.lvmama.vst.suppTicket.client.product.service.SuppTicketProductClientService;
import com.lvmama.vst.ticket.utils.DisneyUtils;
import com.lvmama.vst.ticket.utils.ShowTicketUtils;
import com.lvmama.vst.ticket.vo.DisneyItemInfo;
import com.lvmama.vst.ticket.vo.DisneyShowSeatVo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 全品类订单详情页面action
 *
 * @author jszhangwei
 *
 */
@Component("orderDetailAction")
@RequestMapping("/order/orderManage")
public class OrderDetailAction extends BaseActionSupport {

	private static final long serialVersionUID = 7930920688799245580L;

	private static final Logger LOG = LoggerFactory.getLogger(OrderDetailAction.class);

	private static final Integer NORMAL_USER =9;
	@Autowired
	private IOrderAmountChangeService orderAmountChangeService;
	@Autowired
	private OrdPayPromotionService ordPayPromotionService;

	@Autowired
	private OrdApplyInvoiceInfoService ordApplyInvoiceInfoService;
	@Resource(name="ebkEmailAttchClientServiceRemote")
	private EbkEmailAttchClientService ebkEmailAttchClientServiceRemote;

	@Autowired
	private OrderService orderService;

	@Autowired
	private IOrderStatusManageService orderStatusManageService;

	@Autowired
	private IComplexQueryService complexQueryService;

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

	// 注入分销商业务接口(订单来源、下单渠道)
	@Autowired
	private DistributorClientService distributorClientService;

	@Autowired
	private DictDefClientService dictDefClientService;

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

	@Autowired
	private BranchClientService branchClientService;

	//短信发送业务接口--旧
	@Autowired
	private OrderSendSMSService orderSmsSendService;

	//新系统 订单快递单接口
	@Autowired
	private IApiOrderQueryService apiOrderExpressService;

	//新系统 订单快递单工作流
	@Autowired
	private IApiOrderExpressProcessService apiOrderExpressProcessService;

	//新系统 ebk邮件凭证发送记录
	@Autowired
	private IApiEbkEmailQueryService apiEbkEmailQueryService;

	@Autowired
	private IApiEbkEmailProcessService apiEbkEmailProcessService;

	@Autowired
	private SuppFaxClientService suppFaxClientService;

	@Autowired
	private SuppMailClientService suppMailClientService;

	@Autowired
	private OrderDetailClientService orderDetailClientService;

	@Autowired
	private ComLogClientService comLogClientService;

	@Autowired
	private EbkFaxTaskClientService ebkFaxTaskClientService;


	@Autowired
	private IOrderUpdateService ordOrderUpdateService;

	@Autowired
	private IOrdItemPersonRelationService ordItemPersonRelationService;

	@Autowired
	private IOrdOrderPackService ordOrderPackService;


	@Autowired
	private IOrdAdditionStatusService ordAdditionStatusService;

	@Autowired
	private IOrdItemAdditionStatusService ordItemAdditionStatusService;

	@Autowired
	private CrmUserLabelService crmUserLabelService;

	@Autowired
	private IOrdPersonService ordPersonService;

	@Autowired
	private IOrdMulPriceRateService ordMulPriceRateService;

	@Autowired
	private CategoryClientService categoryClientService;

	@Autowired
	private FSClient vstFSClient;

	@Autowired
	private IOrdTravelContractService ordTravelContractService;

	@Autowired
	private IOrderResponsibleService orderResponsibleService;

	@Resource(name="orderTravelElectricContactService")
	private IOrderElectricContactService orderTravelElectricContactService;

	@Resource
	private VstEmailServiceAdapter vstEmailService;

	@Autowired
	private IOrdAddressService ordAddressService;

	@Resource(name="teamOutboundTourismContractService")
	private IOrderElectricContactService teamOutboundTourismContractService;

	@Resource(name="teamWithInTerritoryContractService")
	private IOrderElectricContactService teamWithInTerritoryContractService;

	@Resource(name="teamDonggangZhejiangContractService")
	private IOrderElectricContactService teamDonggangZhejiangContractService;

	@Autowired
	private IOrdItemContractRelationService iOrdItemContractRelationService;

	@Resource(name="advanceProductAgreementContractService")
	private IOrderElectricContactService advanceProductAgreementContractService;

	@Resource(name="travelItineraryContractService")
	private IOrderElectricContactService travelItineraryContractService;

	@Resource(name="commissionedServiceAgreementService")
	private IOrderElectricContactService commissionedServiceAgreementService;

	@Resource(name="destCommissionedServiceAgreementService")
	private IOrderElectricContactService destCommissionedServiceAgreementService;

	@Resource(name="beijingDayTourContractService")
	private IOrderElectricContactService beijingDayTourContractService;

	@Resource(name="taiwanTravelContractService")
	private IOrderElectricContactService taiwanTravelContractService;
	
	@Resource(name="financeContractService")
	private IOrderElectricContactService financeContractService;

	@Resource(name="orderMessageProducer")
	private TopicMessageProducer orderMessageProducer;

	@Autowired
	private OrderRefundmentServiceAdapter orderRefundmentService;

	@Autowired
	private FavorServiceAdapter favorService;
	@Autowired
	private CouponOrderService  couponOrderService;

	@Autowired
	private OrderPromotionService promotionService;

	@Autowired
	private OrdFormInfoDao ordFormInfoDao;

	@Autowired
	private TntDistributorServiceAdapter tntDistributorServiceRemote;

	@Autowired
	private BuyPresentClientService buyPresentClientService;

	@Autowired
	private IOrdProductNotice ordProductNotice;

//	private String TRAVEL_ECONTRACT_DIRECTORY = "/WEB-INF/resources/econtractTemplate";

	private final String TRAVEL_ECONTRACT_DIRECTORY = AbstractOrderTravelElectricContactService.TRAVEL_ECONTRACT_DIRECTORY;

	@Autowired
	private ISupplierOrderOperator supplierOrderOperator;

	@Autowired
	private EbkUserClientService ebkUserClientService;

	@Resource(name="cruiseTourismContractService")
	private IOrderElectricContactService cruiseTourismContractService;

	@Autowired
	private VisaApprovalClientService visaApprovalClientServiceRemote;

	@Autowired
	private IPayPaymentServiceAdapter payPaymentServiceAdapter;

	@Autowired
	private IOrderContractSnapshotService orderContractSnapshotService;

	@Autowired
	protected FSClient fsClient;

	@Autowired
	private EbkCertifClientService ebkCertifClientService;

	@Autowired
	private FlightOrderProcessService flightOrderProcessService;

	@Autowired
	private OrdOrderDisneyInfoQueryService ordOrderDisneyInfoQueryService;

	@Autowired
	private SupplierOrderOtherService supplierOrderOtherService;

	@Autowired
	private PassportService passportService;

	@Autowired
	private ProdProductPropClientService prodProductPropClientService;

	@Autowired
	private IOrdPassCodeService ordPassCodeService;

	@Autowired
	private IOrdOrderItemService orderItemService;

	@Autowired
	private IOrderStockService orderStockService;

	@Autowired
	private IApiOrdOrderCommissionQueryService apiOrdOrderCommissionQueryService;


	@Autowired
	private OrderTravellerConfirmClientService orderTravellerConfirmClientService;

	@Autowired
	private IOrdFormInfoService  ordFormInfoService;

	@Autowired
	private PreRefundService preRefundService;

	@Autowired
	private IOrderRefundRulesService orderRefundRulesService;

	@Autowired
	private ProdProductBranchClientService prodProductBranchClientService;

	@Autowired
	private TntOrderQueryServiceAdapter tntOrderQueryServiceRemote;

	@Autowired
	private EbkTicketPostClientService ebkTicketPostClientServiceRemote;
	@Autowired
	private ApiSuppOrderService suppOrderClientService;

	@Autowired
	private OrderRefundProcesserAdapter orderRefundProcesserAdapter;

	@Autowired
	private IOrderRescheduleService orderRescheduleService;
	@Resource
	private PassportProductService passportProductService;

	@Autowired
	private OrderRefundBatchService orderRefundBatchService;
	@Autowired
	private QueryLogClientService queryLogClientService;

	@Autowired
	private DestOrderService destOrderService;

	@Autowired
	private IOrdItemConfirmStatusService ordItemConfirmStatusService;


	@Autowired
	private OrderRefundBatchDetailService orderRefundBatchDetailService;


	@Autowired
	private IOrdAccInsDelayInfoService ordAccInsDelayInfoService;


	@Resource
	SuppTicketProductClientService suppTicketProductClientService;

	//结算状态改造 从支付获取
	@Autowired
	private SettlementService settlementService;


	@Autowired
	private IOrderRefundCommMethodService orderRefundCommMethodService;

	@Autowired
	private OrderDetailApportionService orderDetailApportionService;

	@Resource(name="orderCallIdService")
	private IOrderCallIdService orderCallIdService;
	@Autowired
	private SuppGoodsHotelAdapterClientService suppGoodsHotelAdapterClientService;
	@Autowired(required=false)
	private CsVipDubboService csVipDubboService;
	@Autowired
	private ScenicSuppGoodsDescService scenicSuppGoodsDescServiceRemote;
	@Autowired
	private ScenicSportSuppGoodsService scenicSportSuppGoodsServiceRemote;//娱乐美食
	@Autowired
	private ScenicSuppGoodsExpClientService scenicSuppGoodsExpClientService;
	@Autowired
	private ScenicSuppGoodsDescJsonService scenicSuppGoodsDescJsonServiceRemote;
	@Autowired
	private com.lvmama.comm.bee.service.ord.OrderService orderServiceProxy;
	@Resource
	private IApiGroupOrderDetailService apiGroupOrderDetailService;
	@Autowired
	private EbkOrderClientService ebkOrderClientService;
	@Autowired
	private IApiOrderTagService apiOrderTagService;
	@Autowired
	private TradeOrderQueryService tradeOrderQueryService;
	@Autowired
	private IApiReviseDateFeeRelatedOrderService apiReviseDateFeeRelatedOrderService;
	
	@Autowired
	private IApiOrderCancelProcessService apiOrderCancelProcessService;
	
	@Resource(name = "apiOrderSnapshotService")
	private IApiOrderSnapshotService apiOrderSnapshotService;
	
	@Resource(name = "apiOrderItemCancelService")
	private IApiOrderItemCancelService apiOrderItemCancelService;
	
	@Autowired
	private EbkEmailClientService ebkEmailClientService;
	
	private static final String SERVER_TYPE = "COM_AFFIX";

	@RequestMapping(value = "/showOrderProductDetail")
	public String showOrderProductDetail(Model model, HttpServletRequest request) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showOrderProductDetail>");
		}
		String orderIdStr = getRequestParameter("orderId", request);
		String orderItemIdStr = getRequestParameter("orderItemId", request);
		Long orderId = NumberUtils.toLong(orderIdStr);
		Long orderItemId = NumberUtils.toLong(orderItemIdStr);
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		List<OrdOrderItem> ordItemsList = order.getOrderItemList();
		List<OrdOrderPack> orderPacksList = order.getOrderPackList();
		String packType = null;
		if(CollectionUtils.isNotEmpty(orderPacksList)) {
			packType = orderPacksList.get(0).getOwnPack();
		}
		/**
		 * category_single_ticket(11L,"景点门票"),
		 * category_other_ticket(12L,"其它票"),
		 * category_comb_ticket(13L,"组合套餐票"),
		 * category_food(43L,"美食"),
		 * category_sport(44L,"娱乐"),
		 * category_hotel(1L,"酒店"),
		 */
		Long categoryId = 0L;
		Boolean isSUPPLIER = false;//判断是否供应商打包
		for (OrdOrderItem ordOrderItem : ordItemsList) {
			if(!ObjectUtils.equals(orderItemId, ordOrderItem.getOrderItemId())) {
				continue;
			}
			packType = ordOrderItem.getPackageType();
			if(ObjectUtils.equals(packType, ProdProduct.PACKAGETYPE.SUPPLIER.getCode())) {
				isSUPPLIER = true;
			}
			Long suppGoodsId = ordOrderItem.getSuppGoodsId();
			categoryId = ordOrderItem.getCategoryId();
			Map<String, Object> contentMap = ordOrderItem.getContentMap();
			boolean aperiodicFlag = contentMap.get("aperiodic_flag") == null ? false : ObjectUtils.equals("Y", (String) contentMap.get("aperiodic_flag"));
			model.addAttribute("aperiodicFlag", aperiodicFlag);
			//娱乐、美食
			if(BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(categoryId)
					|| BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(categoryId)){
				/**
				 * private String priceIncludes;//费用包含
				 * private String bookingDesc; //预定需知
				 * private String backInfo;  //退改说明
				 * private String orderInfo;  //预约说明
				 */
				SportGoodsFormattedDescVo sportGoodsFormattedDescVo = null;
				try{
					com.lvmama.scenic.api.vo.ResultHandleT<SportGoodsFormattedDescVo> content =
							scenicSportSuppGoodsServiceRemote.findSuppGoodsDesc(suppGoodsId);
					if (null != content && null != content.getReturnContent()){
						sportGoodsFormattedDescVo = content.getReturnContent();
					}
				}catch(Exception e){
					LOG.error(ExceptionFormatUtil.getTrace(e));
				}
				model.addAttribute("suppGoodsDesc", sportGoodsFormattedDescVo);
			}
			//门票、其他票、组合套餐
			if(BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(categoryId)
					|| BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(categoryId)
					|| BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(categoryId)) {
				/**
				 * private String priceIncludes;//费用包含
				 *  typeDesc//票种说明
				 *  needTicket//是否需取票
				 *  limitTime//入园时间
				 *  changeTime//取票时间
				 *  visitAddress//入园地点
				 *  changeAddress//取票地点
				 *  enterStyle//入园/取票方式
				 *  passLimitTime//通关时间限制
				 *  //有效期
				 *  //重要提示
				 */
				ScenicSuppGoodsDesc scenicSuppGoodsDesc = null;
				boolean formattedFlag = false;//入园限制
				try {
					scenicSuppGoodsDesc = scenicSuppGoodsDescServiceRemote.selectBySuppGoodsId(suppGoodsId);
					if (StringUtil.isNotEmptyString(scenicSuppGoodsDesc.getVisitAddress())) {
						formattedFlag = true;
					}
				} catch (Exception e) {
					LOG.error(ExceptionFormatUtil.getTrace(e));
				}
				//处理重要提示换行问题
				if(StringUtil.isNotEmptyString(scenicSuppGoodsDesc.getOthers())){
					scenicSuppGoodsDesc.setOthers(scenicSuppGoodsDesc.getOthers().replaceAll("\r\n", "<br/>"));
				}
				/**
				 * 只有单门票和组合套餐的供应商打包才调用findSuppGoodsDescJsonListByParams接口
				 */
				if(BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(categoryId) ||
						(BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(categoryId) && isSUPPLIER) ){
					List<SuppGoodsDescJson> suppGoodsDescList = null;
					SuppGoodsDescJson suppGoodsDescJson = null;
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("suppGoodsId", suppGoodsId);
					try {
						suppGoodsDescList = scenicSuppGoodsDescJsonServiceRemote.findSuppGoodsDescJsonListByParams(params);
					} catch (Exception e) {
						LOG.error(ExceptionFormatUtil.getTrace(e));
					}
					if (CollectionUtils.isNotEmpty(suppGoodsDescList)) {
						suppGoodsDescJson = suppGoodsDescList.get(0);
					}
					String textString = null;
					if (suppGoodsDescJson != null) {
						textString = suppGoodsDescJson.getContent();
						if(null != JSONObject.fromObject(textString)) {
							Object fetchLimit = JSONObject.fromObject(textString).get("fetchLimit");
							String changeTime = JSONObject.fromObject(fetchLimit) == null ? null :
									(String) JSONObject.fromObject(fetchLimit).get("limitTime");
							scenicSuppGoodsDesc.setChangeTime(changeTime);
							String changeAddress = (String) JSONObject.fromObject(textString).get("fetchSite");
							scenicSuppGoodsDesc.setChangeAddress(changeAddress);
						}
					}
				}
				model.addAttribute("formattedFlag", formattedFlag);
				model.addAttribute("suppGoodsDesc", scenicSuppGoodsDesc);
			}
		}
		model.addAttribute("isSUPPLIER", isSUPPLIER);
		model.addAttribute("categoryId", categoryId);
		return "/order/orderStatusManage/allCategory/showOrderProductDetail";
	}
	@RequestMapping(value = "/showOrderStatusManage")
	public String showOrderStatusManage(Model model, HttpServletRequest request, HttpServletResponse resp) {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<showOrderStatusManage>");
			}

			String loginUserId=this.getLoginUserId();
			model.addAttribute("loginUserId", loginUserId);
			String orderIdStr = getRequestParameter("orderId", request);
			Long orderId=NumberUtils.toLong(orderIdStr);
			Long productId=0L;
			Long bizCategoryId=null;
			String productName = null;
			String isPaymentFlag="N";
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);

			model.addAttribute("order", order);
			
			//设置团结算标识
			if(null!=order&&StringUtil.isNotEmptyString(order.getGroupSettleFlag())){
				if("Y".equals(order.getGroupSettleFlag())){
					model.addAttribute("groupSettleFlag", "是");
				}else{
					model.addAttribute("groupSettleFlag", "否");
				}
			}

			OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(orderId);
			order.setOrdAccInsDelayInfo(ordAccInsDelayInfo);

			/*设置已弃保状态的意外险信息*/
			String t = "*****************************************************************************************";
			String tag = t + "\n" + t + "\n" + t + "\n" + t + "\n" + t + "\n" + t + "\n" + t;
			LOG.info(tag);

			String travDelayFlag = null;
			String travDelayStatus = null;

			if (null != ordAccInsDelayInfo) {
				travDelayFlag = ordAccInsDelayInfo.getTravDelayFlag();
				travDelayStatus = ordAccInsDelayInfo.getTravDelayStatus();
			}

			LOG.info("orderId = " + orderId);
			LOG.info("travDelayFlag = " + travDelayFlag);
			LOG.info("travDelayStatus = " + travDelayStatus);

			if (StringUtils.isNotBlank(travDelayFlag) && StringUtils.equalsIgnoreCase(travDelayFlag, "Y")
					&& StringUtils.isNotBlank(travDelayStatus)
					&& StringUtils.equalsIgnoreCase(travDelayStatus,  ORDER_TRAV_DELAY_STATUS.ABANDON.name())) {
//	            order = OrdOrderUtils.removeTravDelayOrderItem(order);
				model.addAttribute("travDelayFlag", travDelayFlag);
				model.addAttribute("travDelayStatus", ORDER_TRAV_DELAY_STATUS.ABANDON.name());

				List<OrdOrderItem> accInsOrderItemList = OrdOrderUtils.getAccInsOrderItem(order);
				Long accInsQuantity = 0L;
				Long accInsRefundedAmount = 0L;

				if (null != accInsOrderItemList && accInsOrderItemList.size() > 0) {
					accInsQuantity = accInsOrderItemList.get(0).getQuantity();
					accInsRefundedAmount = accInsOrderItemList.get(0).getTotalAmount();

				}

				model.addAttribute("accInsRefundedAmount", PriceUtil.trans2YuanStr(accInsRefundedAmount));
				model.addAttribute("accInsQuantity", accInsQuantity);

				LOG.info("accInsRefundedAmount = " + PriceUtil.trans2YuanStr(order.getRefundedAmount()));
				LOG.info("accInsQuantity = " + accInsQuantity);
			}



			if (order.getDistributorId() !=null && (order.getDistributorId().equals(Constant.DIST_O2O_SELL) || order.getDistributorId().equals(Constant.DIST_O2O_APP_SELL))) {
				try {
					String sign=DESCoder.encrypt(order.getBackUserId());
					model.addAttribute("o2oUserNameSign",sign);
					model.addAttribute("o2oUserName",order.getBackUserId());
				} catch (Exception e) {
					log.error("orderId"+orderIdStr+e);
				}
			}
					String isDownpayFlag="N";
					OrdOrderDownpay ord=null;
					//是否已支付
					if(!PAYMENT_STATUS.PAYED.name().equals(order.getPaymentStatus())){
						//定金支付设置
						if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(order.getBuCode()) &&
								com.lvmama.comm.pay.vo.Constant.PAYMENT_GATEWAY_DIST_MANUAL.DISTRIBUTOR_B2B.getCode().equals(order.getDistributorCode()) &&
								(BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId()) ||
										BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId()) ||
										BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getCategoryId()) ||
										BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId()))
								){
							isPaymentFlag="Y";
							List<OrdOrderDownpay> ordList= ordPayPromotionService.queryOrderDownpayByOrderId(orderId);
							if(CollectionUtils.isNotEmpty(ordList)){
								ord = ordList.get(0);
								String payStatus =PAY_STATUS.UNPAY.toString().equals(ord.getPayStatus()) ? PAY_STATUS.UNPAY.getCnName() : PAY_STATUS.PAYED.getCnName();
								String payType = PAY_TYPE.FULL.toString().equals(ord.getPayType()) ? PAY_TYPE.FULL.getCnName() : PAY_TYPE.PART.getCnName();
								model.addAttribute("payType", payType);
								model.addAttribute("payStatus", payStatus);
								model.addAttribute("payAmount", ord.getPayAmount());
								isDownpayFlag="Y";

							}
							model.addAttribute("isDownpayFlag", isDownpayFlag);
							model.addAttribute("isPaymentFlag", isPaymentFlag);

						}
					}
					// vst组织鉴权
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
							LOG.error("{}", e.getMessage());
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
							LOG.error("paypromotion{}", e.getMessage());
						}
					}


					if(order.getStartDistrictId()!=null){
						ResultHandleT<BizDistrict> districtResultHandleT = districtClientService.findDistrictById(order.getStartDistrictId());
						if(districtResultHandleT.isSuccess() && districtResultHandleT.getReturnContent()!=null){
							BizDistrict startDistrictBo = districtResultHandleT.getReturnContent();
							model.addAttribute("startDistrictBo", startDistrictBo);
						}
					}
					OrdOrderItem mainOrderItem=order.getMainOrderItem();

					String distributionPrice = orderAmountChangeService.getOrderAmountItemByType(order, ORDER_AMOUNT_TYPE.DISTRIBUTION_PRICE.name());
					model.addAttribute("distributionPrice",distributionPrice);
					OrdOrderPack ordOrderPack=new OrdOrderPack();
					Map<String, Object> paramPack = new HashMap<String, Object>();
					paramPack.put("orderId", orderId);//订单号

					List<OrdOrderPack> orderPackList=ordOrderPackService.findOrdOrderPackList(paramPack);
					if (!orderPackList.isEmpty()) {
						ordOrderPack=orderPackList.get(0);
						productId=ordOrderPack.getProductId();
						bizCategoryId=ordOrderPack.getCategoryId();
						productName = ordOrderPack.getProductName();
						//model.addAttribute("ordOrderPack",ordOrderPack);
					}else{
						productId=mainOrderItem.getProductId();
						bizCategoryId=ordOrderPack.getCategoryId();
						productName = mainOrderItem.getProductName();
					}
					model.addAttribute("productId",productId);
					model.addAttribute("productName",productName);
					model.addAttribute("categoryId",bizCategoryId);

					//退款
					Long refunds = 0L;
					Long compensations = 0L;
					List<OrdRefundment> ordRefundments=new ArrayList<OrdRefundment>();
					try {
						ordRefundments = orderRefundmentService.findOrderRefundmentByOrderIdStatus(order.getOrderId(), Constant.REFUNDMENT_STATUS.REFUNDED.name());
					} catch (Exception e) {
						LOG.error("orderRefundmentService findOrderRefundmentByOrderIdStatus error", e);
					}
					if(CollectionUtils.isNotEmpty(ordRefundments)){
						for (OrdRefundment ordRefundment : ordRefundments) {
							//退款总金额
							if(Constant.REFUND_TYPE.ORDER_REFUNDED.name().equals(ordRefundment.getRefundType())){
								refunds += ordRefundment.getAmount();
							}

							//补偿金额
							if(Constant.REFUND_TYPE.COMPENSATION.name().equals(ordRefundment.getRefundType())){
								compensations += ordRefundment.getAmount();
							}

						}
					}
					model.addAttribute("refunds",PriceUtil.trans2YuanStr(refunds));
					model.addAttribute("compensations",PriceUtil.trans2YuanStr(compensations));

					//此订单优惠总金额
					Long favorUsageAmount = favorService.getSumUsageAmount(order.getOrderId());
					model.addAttribute("favorUsageAmount",favorUsageAmount==null?0:new BigDecimal(favorUsageAmount.floatValue()/100).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue());

					//促销减少总金额
					String totalOrderAmount = order.getOrderAmountItemByType(ORDER_AMOUNT_TYPE.PROMOTION_PRICE.name());
					model.addAttribute("totalOrderAmount",totalOrderAmount);

					//门店优惠金额
					Long favourableO2oAmount = 0L;
					HashMap<String, Object> amountItemParams = new HashMap<String, Object>();
					amountItemParams.put("orderId", order.getOrderId());
					amountItemParams.put("itemName", ORDER_AMOUNT_NAME.AMOUNT_NAME_O2OCHANEL.name());
					List<OrdOrderAmountItem> orderItemList = orderAmountChangeService.findOrderAmountItemList(amountItemParams);
					if(CollectionUtils.isNotEmpty(orderItemList)){
						for (OrdOrderAmountItem ordOrderAmountItem : orderItemList) {
							if(null !=ordOrderAmountItem.getItemAmount()){
								favourableO2oAmount += ordOrderAmountItem.getItemAmount();
							}
						}
						if(favourableO2oAmount<0L){
							favourableO2oAmount = favourableO2oAmount*-1;
						}
						model.addAttribute("favourableO2oAmount",favourableO2oAmount/(double)100);
					}
					//主订单负责人
					String objectType="ORDER";
					Long objectId=orderId;
					PermUser permUserPrincipal= orderResponsibleService.getOrderPrincipal(objectType, objectId);
					model.addAttribute("orderPrincipal",permUserPrincipal.getRealName());

					ProdProduct prodProduct=prodProductClientService.findProdProductByIdFromCache(productId).getReturnContent();

//	    	model.addAttribute("productName",prodProduct.getProductName());

					String visaDocLastTime=ordFormInfoService.findVisaDocLastTime(orderId);
					LOG.info("@@@@@@@+++++====visaDocLastTime:"+visaDocLastTime);
					if(visaDocLastTime!=null && visaDocLastTime!=""){
						model.addAttribute("lastDate",DateUtil.toSimpleDate(visaDocLastTime));
					}

					//下单渠道
					Distributor distributor = distributorClientService.findDistributorById(order.getDistributorId()).getReturnContent();

					//白条支付总金额
					Long btOrderPaidAmountFen=0L;
					List<PayPayment> btPaymentList =new ArrayList<PayPayment>();
					try{
						btPaymentList = payPaymentServiceAdapter.selectPayPaymentByObjectIdAndPaymentGateway(orderId, Constant.PAYMENT_GATEWAY.SAN_BAI_TIAO.name(), Constant.PAYMENT_SERIAL_STATUS.SUCCESS.name());
					}catch(Exception e){
						LOG.error("payPaymentServiceAdapter selectPayPaymentByObjectIdAndPaymentGateway error",e);
					}
					if (CollectionUtils.isNotEmpty(btPaymentList)) {
						for (PayPayment p : btPaymentList) {
							btOrderPaidAmountFen += p.getAmount();
						}
					}
					model.addAttribute("btOrderPaidAmountFen",PriceUtil.trans2YuanStr(btOrderPaidAmountFen));

					String cancelStrategyType=order.getCancelStrategy();
					//交通+X 同步商品退改
					if(BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().equals(order.getCategoryId())){
						order.setRealCancelStrategy(ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.name());
					}
					//退改政策
					if("STAMP".equals(order.getOrderSubType())){
						model.addAttribute("cancelStrategyType", cancelStrategyType);
					}else{
						if (!StringUtils.isEmpty(cancelStrategyType)) {
							//			model.addAttribute("cancelStrategyType", cancelStrategyType);
							model.addAttribute("cancelStrategyTypeStr", SuppGoodsTimePrice.CANCELSTRATEGYTYPE.getCnName(cancelStrategyType));
						}
						//酒店套餐和自由行景酒显示详细退改规则
						String cancelStrategyRules = null;
						try {
							cancelStrategyRules = destOrderService.getRouteOrderRefundRules(order);
						} catch (Exception e) {
							LOG.error(ExceptionFormatUtil.getTrace(e));
						}
						model.addAttribute("cancelStrategyRules", cancelStrategyRules);

					}
					//获取主订单上的产品退改策略
					String realCancelStrategy = order.getRealCancelStrategy();
					if (!StringUtils.isEmpty(cancelStrategyType)) {
						//特卖会秒杀景酒主订单退改规则为‘同步商品退改’，改为‘不退不改’
						if(BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId())
								&& BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())
								&& ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.getCode().equals(order.getRealCancelStrategy())
								&& CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())
								&& order.getDistributionChannel()!=null && order.getDistributionChannel() == 110L){
							realCancelStrategy = "UNRETREATANDCHANGE";
						}
						OrdOrderItem ordOrderItem = order.getOrderItemList().get(0);
						if(ordOrderItem.getContentStringByKey("refundRules") != null && !"".equals(ordOrderItem.getContentStringByKey("refundRules"))){
							model.addAttribute("realCancelStrategyTypeStr", ProdRefund.CANCELSTRATEGYTYPE.getCnName(realCancelStrategy)+"<br/>"+ordOrderItem.getContentStringByKey("refundRules"));
						}else{
							model.addAttribute("realCancelStrategyTypeStr", ProdRefund.CANCELSTRATEGYTYPE.getCnName(realCancelStrategy));
						}
					}
					Long deductAmount=order.getAllDeductAmount();
					model.addAttribute("deductAmountStr", PriceUtil.trans2YuanStr(deductAmount));

					Date minLastCanTime =order.getMinLastCancelTime();
					if(minLastCanTime!=null){
						Date now=new Date();
						model.addAttribute("isGreaterNow", now.compareTo(minLastCanTime));
					}
					model.addAttribute("minLastCanTime", minLastCanTime);

					//产品经理逻辑修改 --改为为直接去ord_order上的manager_id字段
					Long managerId = order.getManagerId();
					if(managerId != null){
						PermUser permUser=permUserServiceAdapter.getPermUserByUserId(managerId);
						if(permUser != null){
							model.addAttribute("productManager",permUser );
						}
					}
					model.addAttribute("distributorName", distributor.getDistributorName());
					
					List<OrderTagVo> orderTagVos = null;
					try {
						OrderTagVo orderTagvo = new OrderTagVo();
						orderTagvo.setTagType("SNAPSHOT_TAG");
						orderTagvo.setObjectType("ORD_ORDER");
						orderTagvo.setObjectId(orderId);
						com.lvmama.order.api.base.vo.ResponseBody<List<OrderTagVo>> tagResponseBody = 
								apiOrderTagService.queryOrderTags(new com.lvmama.order.api.base.vo.RequestBody<OrderTagVo>(orderTagvo));
						if (tagResponseBody != null && tagResponseBody.isSuccess()) {
							orderTagVos = tagResponseBody.getT();
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
					//判断是否分销门票和线路品类下的单,满足的话,新增tntOrderChannel属性
					if(OrderUtils.isTicketAndRouteByCategoryId(order.getCategoryId()) && Constant.DIST_BRANCH_SELL == order.getDistributorId()) {
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
					model.addAttribute("orderStatusStr", ORDER_STATUS.getCnName(order.getOrderStatus()));
					model.addAttribute("paymentStatusStr", PAYMENT_STATUS.getCnName(order.getPaymentStatus()));

					LOG.info("order object is "+JSON.toJSONString(order));
					model.addAttribute("order", order);

					//上车点
					Map<String,String> trafficMap = findFrontBusStop(orderId);
					String frontBusStop=trafficMap.get("frontBusStop");
					String backBusStop=trafficMap.get("backBusStop");
					model.addAttribute("frontBusStop", frontBusStop);
					model.addAttribute("backBusStop", backBusStop);

					LOG.info("==L543=="+backBusStop);

					if(StringUtil.isNotEmptyString(frontBusStop)){
						//上车地点：
						String startStr = "上车地点：";
						int start = frontBusStop.indexOf(startStr);
						if(start != -1){
							int end = frontBusStop.indexOf(";", start+startStr.length());
							if(end != -1){
								frontBusStop = frontBusStop.substring(start+startStr.length(), end);
							}else{
								frontBusStop = frontBusStop.substring(start+startStr.length());
							}
						}
					}
					model.addAttribute("frontBusStop", frontBusStop);
					if(StringUtil.isNotEmptyString(backBusStop)){
						//上车地点：
						String startStr = "上车地点：";
						int start = backBusStop.indexOf(startStr);
						if(start != -1){
							int end = backBusStop.indexOf(";", start+startStr.length());
							if(end != -1){
								backBusStop = backBusStop.substring(start+startStr.length(), end);
							}else{
								backBusStop = backBusStop.substring(start+startStr.length());
							}
						}
					}
					model.addAttribute("backBusStop", backBusStop);

					//当地玩乐交通接驳的配置
					this.configOrderConnectsServiceProp(model,order,null);

					//游客姓名   联系人
					List<OrdPerson> personList = order.getOrdPersonList();
					OrdPerson ordPersonContact = null;
					OrdPerson ordPersonBooker = null;
					OrdPerson firstTravellerPerson = null;
					StringBuilder tnSB = new StringBuilder();

					int travellerNum=0;
					if (personList!=null) {
						for (OrdPerson ordPerson : personList) {
							String personType = ordPerson.getPersonType();
							if (ORDER_PERSON_TYPE.CONTACT.name().equals(personType)) {
								ordPersonContact = ordPerson;
								continue;
							} if (OrderEnum.ORDER_PERSON_TYPE.BOOKER.name().equals(personType)) {

								ordPersonBooker = ordPerson;
								continue;
							} else if (OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equals(personType)) {

								if (firstTravellerPerson==null) {
									firstTravellerPerson=ordPerson;
								}
								if(tnSB.length() > 0) {
									tnSB.append(",");
								}
								tnSB.append(ordPerson.getFullName());
								travellerNum += 1;
							}
						}
					}
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
					//游玩人姓名拼接
					String travellerName = tnSB.toString();
					//门票没有联系人   默认第一游客为联系人
					if (ordPersonContact==null && firstTravellerPerson!=null) {
						ordPersonContact=firstTravellerPerson;
					}
					model.addAttribute("ordPersonContact", ordPersonContact);
					model.addAttribute("ordPersonBooker", ordPersonBooker);
					model.addAttribute("travellerName", travellerName);
					//游玩人数
					model.addAttribute("travellerNum", travellerNum);

					//节省系统开销，不进行查询，统一设置为true，前台显示“另有订单”
					model.addAttribute("otherOrder",true);

					//定金是否存在
					boolean hasDepositsAmount=false;
					if (order.getDepositsAmount()!=null && order.getDepositsAmount()>0 ) {
						hasDepositsAmount=true;
					}
					model.addAttribute("hasDepositsAmount",hasDepositsAmount);

					//定金应收款
					double depositsOughtAmount;
					//订单已收款大于定金应收款的时候
					if ( (order.getActualAmount()-order.getDepositsAmount()) >0) {
						depositsOughtAmount=order.getDepositsAmount();
					}else{
						depositsOughtAmount=order.getActualAmount();
					}
					model.addAttribute("depositsOughtAmount",depositsOughtAmount/100.0);

					//定金剩余款
					double  excessFunds=order.getDepositsAmount()-order.getActualAmount();
					if (excessFunds<0) {
						excessFunds=0.0;
					}
					model.addAttribute("excessFunds",excessFunds/100.0);

					//支付等待时间
					boolean hasPreauthBook=false;//强制预授权是否
					if (TimePriceUtils.hasPreauthBook(order.getLastCancelTime(),order.getCreateTime())) {
						hasPreauthBook=true;
					}
					model.addAttribute("hasPreauthBook",hasPreauthBook);

					//订单取消类型
					Map<String, Object> dictDefPara = new HashMap<String, Object>();
					dictDefPara.put("dictCode", Constants.ORDER_CANCEL_TYPE);
					dictDefPara.put("cancelFlag", "Y");
					List<BizDictDef> dictDefs = dictDefClientService.findDictDefList(dictDefPara).getReturnContent();
					List<BizDictDef> dictDefList = new ArrayList<BizDictDef>();
					for (BizDictDef def : dictDefs) {
						if (VstOrderEnum.ORDER_CANCEL_TYPE_RESOURCE_NO_CONFIM_REFUND_PROCESS.equals(def.getDictDefId())) {
							continue;
						}
						dictDefList.add(def);
					}
					model.addAttribute("orderCancelTypeList", dictDefList);

					//预定通知
					int messageCount;
					Long[] auditIdArray = findBookingAuditIds("ORDER",orderId,"");
					LOG.info("findBookingAuditIds: orderId={}, result={}", orderId, auditIdArray);
					if (auditIdArray.length==0) {
						messageCount=0;
					}else{
						Map<String, Object> parameters = new HashMap<String, Object>();
						parameters.put("messageStatus", MESSAGE_STATUS.UNPROCESSED.getCode());
						parameters.put("auditIdArray",auditIdArray);
						//parameters.put("receiver",loginUserId);
						messageCount=comMessageService.findComMessageCount(parameters);
					}
					model.addAttribute("messageCount", messageCount);


					//活动判断逻辑
					//订单预审
					boolean isDonePretrialAudit = this.isDonePretrialAudit(order);
					model.addAttribute("isDonePretrialAudit", isDonePretrialAudit);

					//通知出团
					boolean isDoneNoticeRegimentAudit = this.isDoneNoticeRegimentAudit(order);
					model.addAttribute("isDoneNoticeRegimentAudit", isDoneNoticeRegimentAudit);

					//催支付
					boolean isDonePaymentAudit = this.isDonePaymentAudit(order);
					model.addAttribute("isDonePaymentAudit", isDonePaymentAudit);

					//小驴分期催支付
					boolean isDoneTimePaymentAudit = this.isDoneTimePaymentAudit(order);
					model.addAttribute("isDoneTimePaymentAudit", isDoneTimePaymentAudit);

					//订单取消确认
					boolean isDoneCancleConfirmedtAudit =this.isDoneCancleConfirmedAudit(order);
					model.addAttribute("isDoneCancleConfirmedtAudit", isDoneCancleConfirmedtAudit);

					//订单是否完成在线退款
					boolean isDoneOnlineRefundAudit =this.isDoneOnlineRefundAudit(order);
					model.addAttribute("isDoneOnlineRefundAudit", isDoneOnlineRefundAudit);

					model.addAttribute("auditMap", this.getUnprocessedAudit(order));

					//申请定金核损
					boolean showLosses = this.showLosses(order , ord);
					model.addAttribute("showLosses" , showLosses);

					//申请资金转移
					boolean showTransfer = this.showTransfer(showLosses, order);
					model.addAttribute("showTransfer" , showTransfer);

					//附件数量
					Map<String, Object> param = new HashMap<String, Object>();
					param.put("orderId", orderId);
					param.put("orderItemId", 0L);
					int orderAttachmentNumber = orderAttachmentService.countOrderAttachmentByCondition(param);
					model.addAttribute("orderAttachmentNumber", orderAttachmentNumber);
					//订单状态显示
					//infoStatu && resourceStatus
					model.addAttribute("hasInfoAndResourcePass", order.hasInfoAndResourcePass());

					//合同状态
					List<OrdTravelContract>  ordTravelContractList  = findOrdTravelContract(orderId);

					if (CollectionUtils.isNotEmpty(ordTravelContractList)) {
						model.addAttribute("isExistContract",true);
						for(OrdTravelContract contract:ordTravelContractList){

							//如果有预付款协议
							if(!contract.getContractTemplate().equals(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode())){
								if(StringUtils.isEmpty(contract.getStatus())){
									contract.setStatus(ORDER_TRAVEL_CONTRACT_STATUS.UNSIGNED.getCode());
								}
								model.addAttribute("contractStatusName", ORDER_TRAVEL_CONTRACT_STATUS.getCnName(contract.getStatus()));
								break;
							}
						}

					}

					//查回传
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("orderId", orderId);//订单号
					String readUserStatus =EbkFaxVO.READ_USER_STATUS_YES;
					params.put("isReadUser", readUserStatus);//是否读取，Y：已读取 N：未读取
					params.put("cancelFlag", "Y");//是否有效
					ResultHandleT<Integer> resultHandleInteger = ebkFaxTaskClientService.findEbkFaxRecvCount(params);
					if (resultHandleInteger.isSuccess()) {
						model.addAttribute("ebkCount",resultHandleInteger.getReturnContent());
					}

					boolean hasTravelContract=false;
					boolean hasVisa=false;
					boolean isContainTicket = false;
					//订单使用状态
					List<String> perFormStatusList = new ArrayList<String>();

					//子订单列表展示
					Map<String,List<OrderMonitorRst>> resultMap=new HashMap<String,List<OrderMonitorRst>>();
					Map<String,List<Boolean>> showMap=new HashMap<String,List<Boolean>>();
					List<OrdOrderItem> ordItemsList =order.getOrderItemList();
					Map<String, Object> attachmentParam = new HashMap<String, Object>();
					//期票有效期描述
					List<String> ordItemsAperiodicExpList = new ArrayList<String>();
					model.addAttribute("ordItemsAperiodicExpList",ordItemsAperiodicExpList);

					List<String> ordItemsAperiodicUnvalidList = new ArrayList<>();
					model.addAttribute("ordItemsAperiodicUnvalidList",ordItemsAperiodicUnvalidList);

					//为了保证子订单中，分摊信息中显示的多价格顺序与其它多价格顺序一致，保存子单的id与多价格的对应关系，用于后来排序
					Map<Long, List<OrdMulPriceRate>> itemIdWithMulPriceMap = new HashMap<>();
					//订单发放状态
					String rightStatus = null;
					
					for (OrdOrderItem ordOrderItem : ordItemsList) {


						Long branchId = null;
						if(BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(ordOrderItem.getCategoryId())
								|| BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(ordOrderItem.getCategoryId())
								|| BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(ordOrderItem.getCategoryId())
								|| BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(ordOrderItem.getCategoryId()) ){

							Long prodBranchId = ordOrderItem.getBranchId();
							try {
								ResultHandleT<ProdProductBranch> resultHandleT =
										this.prodProductBranchClientService.findProdProductBranchById(prodBranchId);
								if(resultHandleT != null && resultHandleT.getReturnContent() != null){
									branchId = resultHandleT.getReturnContent().getBranchId();
								}
							}catch (Exception e){
								log.error(ExceptionFormatUtil.getTrace(e));
							}
						}


						String categoryId=ordOrderItem.getCategoryId()+"";
						List<OrderMonitorRst> childOrderResultList;
						if(!resultMap.containsKey(categoryId)){
							childOrderResultList =  new ArrayList<OrderMonitorRst>();
							resultMap.put(categoryId, childOrderResultList);
						}else{
							childOrderResultList=resultMap.get(categoryId);
						}

						Map<String,Object> contentMap = ordOrderItem.getContentMap();
						String categoryType =  (String) contentMap.get(ORDER_COMMON_TYPE.categoryCode.name());
						//获取当地玩乐美食 娱乐属性
						if (BIZ_CATEGORY_TYPE.category_sport.getCode().equals(categoryType)|| BIZ_CATEGORY_TYPE.category_food.getCode().equals(categoryType)) {
							this.configPlayPropConvertByMap(model, categoryType, contentMap, ordOrderItem);
						}
						ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(categoryType);
						BizCategory bizCategory=result.getReturnContent();


						if (BIZ_CATEGORY_TYPE.category_route_group.getCode().equals(categoryType)
								|| BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(categoryType)
								|| BIZ_CATEGORY_TYPE.category_route_local.getCode().equals(categoryType)
								|| BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCode().equals(categoryType)
								|| BIZ_CATEGORY_TYPE.category_cruise.getCode().equals(categoryType)) {
							hasTravelContract=true;
						}
						//查询是否包含签证缩小查询签证范围
						try {
							if(order.getCategoryId()!=null && OrderUtil.showVisaApprovalFlag(order)){
								VisaResultHandleT<ArrayList<VisaApprovalVo>> visaApprovalListResult = visaApprovalClientServiceRemote.findVisaApprovalList(params);
								if(visaApprovalListResult!=null && CollectionUtils.isNotEmpty(visaApprovalListResult.getReturnContent())){
									hasVisa = true;
								}
							}
						} catch (Exception e) {
							log.error(e.getMessage());
						}

						objectType="ORDER_ITEM";
						objectId=ordOrderItem.getOrderItemId();
						PermUser orderPrincipal=new PermUser();
						try{
							orderPrincipal=orderResponsibleService.getOrderPrincipal(objectType, objectId);
						}catch(Exception e){
							log.error("orderResponsibleService getOrderPrincipal error",e);
						}
						//资源审核人
						String resourceApprover = "";
						try{
							resourceApprover = orderResponsibleService.getResourceApprover(objectId, objectType).getRealName();
						}catch(Exception e){
							log.error("orderResponsibleService getResourceApprover error",e);
						}
						OrderMonitorRst orderMonitorRst = new OrderMonitorRst();
						orderMonitorRst.setOrderItemMemo(ordOrderItem.getOrderMemo());//子订单备注
						orderMonitorRst.setPrincipal(orderPrincipal.getRealName());//负责人
						orderMonitorRst.setResourceApprover(resourceApprover);//资源审核人
						orderMonitorRst.setExpiredRefundFlag(ordOrderItem.getExpiredRefundFlag());
						orderMonitorRst.setOrderId(ordOrderItem.getOrderItemId());
						orderMonitorRst.setOrderItemId(ordOrderItem.getOrderItemId());
						String buildCurrentStatus = this.buildCurrentStatus(order,ordOrderItem);
						orderMonitorRst.setCurrentStatus(buildCurrentStatus);
						orderMonitorRst.setChildOrderType(categoryType);
						orderMonitorRst.setUseTime(ordOrderItem.getUseTime());//使用时间
						orderMonitorRst.setLocalHotelAddress(ordOrderItem.getLocalHotelAddress());//当地酒店地址
						orderMonitorRst.setPriceConfirmStatus(ordOrderItem.getPriceConfirmStatus());
						//设置子订单类别
						String childOrderTypeName = BIZ_CATEGORY_TYPE.getCnName(categoryType);
						if(BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(ordOrderItem.getCategoryId())){
							//交通接驳，子订单类型名称，增加规格名称
							if(branchId != null){
								ResultHandleT<BizBranch> resultHandleT = branchClientService.findBranchById(branchId);
								if(resultHandleT != null && resultHandleT.getReturnContent() != null){
									childOrderTypeName = childOrderTypeName + "-" +resultHandleT.getReturnContent().getBranchName();
								}
							}
						}
						
						//金融TODO
						if(BIZ_CATEGORY_TYPE.category_finance.getCategoryId().equals(order.getCategoryId())) {
							if(rightStatus == null) {
								try {
									ProcessResult<TradeTradeOrderVO> tradeOrderVoResult = tradeOrderQueryService.selectTradeOrderByOrderId(order.getOrderId(), "VST_ORDER");
									if(tradeOrderVoResult != null && tradeOrderVoResult.getSuccess()) {
										TradeTradeOrderVO tradeOrderVO = tradeOrderVoResult.getData();
										rightStatus = tradeOrderVO.getStatus();
									}
								}catch(Exception e) {
									LOG.error("query finance status is error orderId="+order.getOrderId(), e);
								}
							}
							orderMonitorRst.setRightStatus(rightStatus);
							
							String financeInterestsBonusVoJson = ordOrderItem.getContentStringByKey("financeInterestsBonusVo");
							if(financeInterestsBonusVoJson != null && !"".equals(financeInterestsBonusVoJson)) {
								FinanceInterestsBonus financeInterestsBonusVo = com.alibaba.fastjson.JSONObject.toJavaObject(JSON.parseObject(financeInterestsBonusVoJson), FinanceInterestsBonus.class);
								Double interestsPercent = financeInterestsBonusVo.getInterestsPercent();
								String rightPrice = PriceUtil.StrToStrtrans2Yuan(String.valueOf(ordOrderItem.getTotalAmount()),interestsPercent);
								orderMonitorRst.setRightPrice(PriceUtil.trans2YuanStr(rightPrice)+"元");
							}
						}
						
						orderMonitorRst.setProductName(this.buildProductName(ordOrderItem));
						orderMonitorRst.setProducTourtType(prodProduct.getProducTourtType());
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

						this.buildBuyCountAndPrice(ordOrderItem, orderMonitorRst, itemIdWithMulPriceMap);// 构建子订单购买份数、销售单价和总价

						orderMonitorRst.setVisitTime(this.buildVisitTime(ordOrderItem));
						this.buildVisitTimeByValidDay(orderMonitorRst,ordOrderItem);
						
						String apiFlag="N";
						if (bizCategory.getParentId()!=null && (bizCategory.getParentId().equals(5L) ||
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
						// ***************************************************************//
						// 通过对接获取服务商供应信息,只对景点门票和其他票类型
						Long goodsId = ordOrderItem.getSuppGoodsId();
						Long isSupportDestroyCode = 0L;// 默认不支持,1:支持;0:不支持
						PassProvider provider = null;
						if (StringUtils.equals(categoryType, BIZ_CATEGORY_TYPE.category_single_ticket.name())
								|| StringUtils.equals(categoryType, BIZ_CATEGORY_TYPE.category_other_ticket.name())
								|| StringUtils.equals(categoryType, BIZ_CATEGORY_TYPE.category_show_ticket.name())) {
							provider=supplierOrderOperator.getProductServiceInfo(goodsId);
						}
						if (provider != null) {
							isSupportDestroyCode = provider.getIsSupportDestroyCode();
						}
						orderMonitorRst.setIsSupportDestroyCode(isSupportDestroyCode);
						// ***************************************************************//

						// ***************************************************************//
						//是否是EBK能及时处理通关
						String isEBKAndEnterInTime = "N";
						try {
							if (StringUtils.equals(categoryType, BIZ_CATEGORY_TYPE.category_single_ticket.name())
									|| StringUtils.equals(categoryType, BIZ_CATEGORY_TYPE.category_other_ticket.name())
									|| StringUtils.equals(categoryType, BIZ_CATEGORY_TYPE.category_show_ticket.name())
									|| StringUtils.equals(categoryType, BIZ_CATEGORY_TYPE.category_comb_ticket.name())) {
								Boolean IS_EBK_NOTICE = Boolean.FALSE;
								String fax = ORDER_COMMON_TYPE.fax_flag.name();
								Boolean IS_FAX_NOTICE = ordOrderItem.hasContentValue(fax,"Y");
								if (!IS_FAX_NOTICE) {// 与在线退款逻辑保持一致，传真与EBK共存时，不是EBK订单
									if (!IS_EBK_NOTICE) {
										Map<String,Object> paramUser=new HashMap<String,Object>();
										paramUser.put("cancelFlag", "Y");
										paramUser.put("supplierId", ordOrderItem.getSupplierId());
										List<EbkUser> ebkUserList = ebkUserClientService.getEbkUserList(paramUser).getReturnContent();
										if(ebkUserList!=null&& !ebkUserList.isEmpty()){
											IS_EBK_NOTICE= true;
										}
									}
									if (IS_EBK_NOTICE) {
										String notInTimeFlag = ordOrderItem.getNotInTimeFlag();
										if (notInTimeFlag == null|| "".equals(notInTimeFlag)) {
											Long supplierId = ordOrderItem.getSupplierId();
											SuppSupplier suppSupplier = suppSupplierClientService.findSuppSupplierById(supplierId).getReturnContent();
											String notInTimeFlag_supplier = suppSupplier.getNotInTimeFlag();
											if (!"Y".equals(notInTimeFlag_supplier)) {
												SuppGoods suppgoods = suppGoodsClientService.findSuppGoodsById(ordOrderItem.getSuppGoodsId()).getReturnContent();
												String notInTimeFlag_suppgoods =  suppgoods.getNotInTimeFlag();
												if(!"Y".equals(notInTimeFlag_suppgoods)){
													isEBKAndEnterInTime = "Y";
												}
											}
										}else{
											if (!"Y".equals(notInTimeFlag)) {
												isEBKAndEnterInTime = "Y";
											}
										}
									}
								}
							}
						} catch (Exception e) {
							LOG.error("query isEBKAndEnterInTime error!", e);
						}
						orderMonitorRst.setIsEbkAndEnterInTime(isEBKAndEnterInTime);
						// ***************************************************************//


						Map<String, Object> paramOrdItemPersonRelation = new HashMap<String, Object>();
						paramOrdItemPersonRelation.put("orderItemId", ordOrderItem.getOrderItemId());
						List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(paramOrdItemPersonRelation);

						orderMonitorRst.setPersonCount(ordItemPersonRelationList.size());

						//附件数量
						attachmentParam.put("orderId", orderId);
						attachmentParam.put("orderItemId", ordOrderItem.getOrderItemId());
						orderMonitorRst.setOrderAttachmentNumber(orderAttachmentService.countOrderAttachmentByCondition(attachmentParam));

						//门票业务类订单使用状态
						Map<String,Object> performMap = ordOrderItem.getContentMap();
						String categoryCode =  (String) performMap.get(ORDER_COMMON_TYPE.categoryCode.name());
						if (ProductCategoryUtil.isTicket(categoryCode)) {
							isContainTicket = true;
							//门票业务类订单使用状态
							List<OrdTicketPerform> resultList;
							OrdTicketPerform  ordTicketPerform;
							resultList = complexQueryService.selectByOrderItem(ordOrderItem.getOrderItemId());
							//门票状态
							String performStatusName= PERFORM_STATUS_TYPE.getCnName(OrderUtils.calPerformStatus(resultList,order,ordOrderItem)) ;
							perFormStatusList.add(performStatusName);
							if("部分使用".equals(performStatusName)){
								ordTicketPerform = resultList.get(0);
								performStatusName=performStatusName+":成人票"+(ordTicketPerform.getActualAdult()==null?0:ordTicketPerform.getActualAdult())
										+"儿童票"+(ordTicketPerform.getActualChild()==null?0:ordTicketPerform.getActualChild());
							}
							orderMonitorRst.setPerFormStatus(performStatusName);
							//迪斯尼演出票显示
							if(DisneyUtils.isDisneyShow(ordOrderItem)){
								String disneyItemInfoStr = (String) ordOrderItem.getContentValueByKey("DisneyItemInfo");
								DisneyItemInfo disneyItemInfo = new Gson().fromJson(disneyItemInfoStr, DisneyItemInfo.class);
								List<DisneyShowSeatVo> seats = disneyItemInfo.getSeats();
								StringBuilder seatsStr = new StringBuilder();
								if(seats!=null && seats.size()>0){
									for(DisneyShowSeatVo seat:seats){
										seatsStr.append(seat).append("<br>");
									}
								}
								orderMonitorRst.setSectionDetail(seatsStr.toString());
								String showTime = ordOrderDisneyInfoQueryService.queryDisneyShowTime(orderId, ordOrderItem.getOrderItemId());
								orderMonitorRst.setShowTime(showTime);
								orderMonitorRst.setSpecialTicketType(SPECIAL_TICKET_TYPE.DISNEY_SHOW.getCode());
							}

							//演出票显示
							if(BIZ_CATEGORY_TYPE.category_show_ticket.getCode()
									.equals(categoryType)){
								String startTime = (String) ordOrderItem.getContentValueByKey(ORDER_TICKET_TYPE.showTicketEventStartTime.name());
								String endTime = (String) ordOrderItem.getContentValueByKey(ORDER_TICKET_TYPE.showTicketEventEndTime.name());
								String showTime = (StringUtil.isEmptyString(startTime)?"":startTime) + (StringUtil.isEmptyString(endTime)?"":("-"+endTime));
								if(StringUtil.isEmptyString(showTime)){
									showTime="通场";
								}
								String region = (String) ordOrderItem.getContentValueByKey(ORDER_TICKET_TYPE.showTicketRegion.name());
								//在线选座订单详情显示座位号
								String seats = (String) ordOrderItem.getContentValueByKey(ORDER_TICKET_TYPE.showTicketSeats.name());
								orderMonitorRst.setShowTime(showTime);
								orderMonitorRst.setSectionDetail(region);
								orderMonitorRst.setSeatsDetail(seats);
								//ResultHandleT<SuppGoods> resultHandleSuppGoods = suppGoodsClientService.findSuppGoodsById(ordOrderItem.getSuppGoodsId(), Boolean.TRUE, Boolean.TRUE);
								ResultHandleT<SuppGoods> resultHandleSuppGoods = suppGoodsHotelAdapterClientService.findSuppGoodsById(ordOrderItem.getSuppGoodsId());
								SuppGoods suppGoods = new SuppGoods();
								if (resultHandleSuppGoods.isSuccess() && resultHandleSuppGoods.getReturnContent() != null) {
									suppGoods = resultHandleSuppGoods.getReturnContent();
								}
								orderMonitorRst.setProductName(ordOrderItem.getSuppGoodsName()+"-"+GOODSSPEC.getSpecName(suppGoods.getGoodsSpec()));
								orderMonitorRst.setSpecialTicketType("SHOW_TICKET");
							}
						}else if(ProductCategoryUtil.isWifi(categoryCode) ||  ProductCategoryUtil.isConnects(categoryCode) || ProductCategoryUtil.isPlay(categoryCode)){
							List<OrdTicketPerform> resultList=complexQueryService.selectByOrderItem(ordOrderItem.getOrderItemId());
							String performStatusName= PERFORM_STATUS_TYPE.getCnName(OrderUtils.calPalyNoShowticketPerformStatus(resultList,order,ordOrderItem)) ;
							perFormStatusList.add(performStatusName);
						}

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
						//end
					}

					if (ApportionUtil.isApportionEnabled()) {
						log.info("Now process apportion info for order " + orderId);
						orderDetailApportionService.calcOrderDetailItemApportion(orderId, resultMap, itemIdWithMulPriceMap);
						log.info("Process apportion info completed for order " + orderId);
					}

					//判断是否含有迪斯尼演出票、玩乐演出票(disney_show,show_ticket)
					for (Map.Entry<String, List<OrderMonitorRst>> entry : resultMap.entrySet()) {
						List<OrderMonitorRst> childOrderResultList =  entry.getValue();
						Boolean hasNormal = false;
						Boolean hasShow = false;
						List<Boolean> result = new ArrayList<Boolean>();
						for(OrderMonitorRst orderMonitorRst:childOrderResultList){
							if(DisneyUtils.isDisneyShow(orderMonitorRst.getSpecialTicketType()) || ShowTicketUtils.isShowTicket(orderMonitorRst.getSpecialTicketType())){
								hasShow = true;
							}else{
								hasNormal = true;
							}
						}
						result.add(hasNormal);
						result.add(hasShow);
						showMap.put(entry.getKey(), result);
					}
					model.addAttribute("showMap",showMap);
					model.addAttribute("resultMap",resultMap);

					//支付等待时间
					String waitPaymentTime=DateUtil.formatDate(order.getWaitPaymentTime(), "yyyy-MM-dd HH:mm");
					if (waitPaymentTime==null) {
						waitPaymentTime="";
					}
					model.addAttribute("waitPaymentTime", waitPaymentTime);
					//预售等待时间
					String waitRetainageTime=DateUtil.formatDate(order.getWaitRetainageTime(), "yyyy-MM-dd HH:mm");
					if (waitRetainageTime==null) {
						waitRetainageTime="";
					}
					model.addAttribute("waitRetainageTime", waitRetainageTime);

					//是否有合同
					model.addAttribute("hasTravelContract", hasTravelContract);
					model.addAttribute("hasVisa", hasVisa);
					model.addAttribute("prodProduct",prodProduct);
					//订单金额-价格修改
					Long totalAmountChange=getTotalAmountChange(orderId,null,null);
					model.addAttribute("totalAmountChange", PriceUtil.trans2YuanStr(totalAmountChange));

					//门票业务类订单使用状态
					if (OrderUtils.isTicketByCategoryId(order.getCategoryId())) {
						//门票业务类订单使用状态
						String performStatus = OrderUtils.getMainOrderPerformStatus(perFormStatusList);
						model.addAttribute("performStatus", performStatus);
						if(!"已使用".equals(performStatus)){
							//门票是否允许提前退
							model.addAttribute("canPreRefund", preRefundService.canPreRefund(order));
						}
					}else if(OrderUtils.isPlayNoShowticketByCategoryId(order.getCategoryId())){
						String performStatus = OrderUtils.getMainOrderPerformStatus(perFormStatusList);
						model.addAttribute("performStatus", performStatus);
					}
					model.addAttribute("isContainTicket",isContainTicket);
					// 门票阶梯退改规则
					if(BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(order.getCategoryId())
							|| BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(order.getCategoryId())
							|| BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().equals(order.getCategoryId())
							|| BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(order.getCategoryId())){

						Map<String, Object> mp = differentialRulePrice(order,showMap,resultMap);
						LOG.info("refound order"+order.getOrderId()+",refoundStr="+mp.get("refoundStr"));
						model.addAttribute("refoundStr", mp.get("refoundStr"));
						model.addAttribute("backAmount", mp.get("backAmount"));
						if(showBackRule(order)){
							model.addAttribute("isTicket", "Y");
						}else{
							model.addAttribute("isTicket", "N");
						}
					}else {
						model.addAttribute("isTicket", "N");
					}
					// TODO
					//目的地BU前台下单特殊显示
					LOG.info("+++++++++isDestBuFrontOrder++++++++"+OrdOrderUtils.isDestBuFrontOrder(order)+"----orderId----"+order.getOrderId());
					LOG.info("+++++++++isLocalBuFrontOrder++++++++"+OrdOrderUtils.isLocalBuFrontOrder(order)+"----orderId----"+order.getOrderId());
					if(OrdOrderUtils.isDestBuFrontOrder(order) || OrdOrderUtils.isLocalBuFrontOrder(order)){
						model.addAttribute("isDestBuFrontOrder", true);
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
								LOG.info("+++++++++invoiceAmountLong++++++++"+invoiceAmountLong);
							}
							model.addAttribute("orderInvoiceInfoVst", orderInvoiceInfoVst);

						}

						//组装凭证确认状态
						boolean certConfirmStatus = false;
						if(CERT_CONFIRM_STATUS.UNCONFIRMED.name().equals(order.getCertConfirmStatus())){
							certConfirmStatus = false;
						}else if(CERT_CONFIRM_STATUS.CONFIRMED.name().equals(order.getCertConfirmStatus())){
							certConfirmStatus = true;
						}else{
							certConfirmStatus = false;
						}
						model.addAttribute("certConfirmStatus", certConfirmStatus);
						//目的地自驾游儿童价后台订单详情显示
						List<OrdOrderAmountItem> orderAmountItemList = order.getOrderAmountItemList();
						if(CollectionUtils.isNotEmpty(orderAmountItemList)){
							for (OrdOrderAmountItem ordOrderAmountItem : orderAmountItemList) {
								if(ORDER_AMOUNT_TYPE.SELFDRIVING_CHILDPRICE.name().equals(ordOrderAmountItem.getOrderAmountType()) &&
										ordOrderAmountItem.getItemAmount()!=null && ordOrderAmountItem.getItemAmount()>0){
									LOG.info("目的地自驾游儿童价后台订单详情显示。 orderAmountItemList orderID="+orderId);
									List<OrdFormInfo> formInfoList = order.getFormInfoList();
									if(CollectionUtils.isNotEmpty(formInfoList)){
										for (OrdFormInfo ordFormInfo : formInfoList) {
											LOG.info("目的地自驾游儿童价后台订单详情显示。 ordFormInfo orderID="+orderId);
											if(OrdFormInfoContentTypeEnum.SELF_CHILD_QUANTITY.getContentType().equals(ordFormInfo.getContentType()) &&
													StringUtil.isNotEmptyString(ordFormInfo.getContent())){
												int selfDrivingChildQuantity = Integer.parseInt(ordFormInfo.getContent());
												Long selfDrivingChildAmount = 0L;
												if(selfDrivingChildQuantity > 0){
													selfDrivingChildAmount = ordOrderAmountItem.getItemAmount()/selfDrivingChildQuantity;
												}
												LOG.info("目的地自驾游儿童价后台订单详情显示。 ordFormInfo orderID="+orderId+"selfDrivingChildQuantity:"+selfDrivingChildQuantity);
												model.addAttribute("selfDrivingChild", "Y");
												model.addAttribute("selfDrivingChildCount", selfDrivingChildQuantity);
												model.addAttribute("selfDrivingChildAmount", selfDrivingChildAmount);
												model.addAttribute("selfDrivingChildTotalAmount", ordOrderAmountItem.getItemAmount());
												if(order.getVisitTime()!=null){
													model.addAttribute("selfDrivingChildVisitTime", DateUtil.formatSimpleDate(order.getVisitTime()));
												}else{
													model.addAttribute("selfDrivingChildVisitTime", "");
												}
												break;
											}
										}
									}
									break;
								}
							}
						}
					}else{
						model.addAttribute("isDestBuFrontOrder", false);
					}


					this.buildChangableFlag(model, orderId, ordItemsList);
					model.addAttribute("ordRescheduleStatus",orderRescheduleService.getOrderRescheduleStatus(order));
					//是否保留房
					String hasHotel = "N";
					if(OrdOrderUtils.hasHotelItem(order)){
						hasHotel = "Y";
					}
					model.addAttribute("hasHotel", hasHotel);
					model.addAttribute("isPartStockFlag", OrdOrderUtils.getStockFlag(order));

//	        //是否显示退款申请按钮
//	        model.addAttribute("isReFundButtonShow",orderRefundCommMethodService.checkReFundButtonShow(order).equalsIgnoreCase("success"));
					//屏蔽退款申请按钮
					model.addAttribute("isReFundButtonShow",false);
					//判断是否是改期服务费订单,首先订单必须正常且已支付,通过改期主订单id调用改期服务费接口,看是否存在服务费订单
					String serviceFeeOrderFlag = "N";
					if(order.getOrderStatus().equals(VstOrderEnum.ORDER_STATUS.NORMAL.name())&&order.getPaymentStatus().equals(VstOrderEnum.PAYMENT_STATUS.PAYED.name())){
						com.lvmama.order.api.base.vo.RequestBody<Long> requestBody = new com.lvmama.order.api.base.vo.RequestBody<Long>();
						requestBody.setT(orderId);
						com.lvmama.order.api.base.vo.ResponseBody<List<ReviseDateFeeRelatedOrderVo>> listResponseBody = apiReviseDateFeeRelatedOrderService.queryReviseDateFeeRelatedOrderListByReviseDateOrderId(requestBody);
						if(CollectionUtils.isNotEmpty(listResponseBody.getT())&&listResponseBody.getT().size()>=1){
							serviceFeeOrderFlag = "Y";
						}
					}
					model.addAttribute("serviceFeeOrderFlag",serviceFeeOrderFlag);
					//add by zjt
					String callId = StringUtils.isEmpty(request.getParameter("callid"))?"":request.getParameter("callid");
					model.addAttribute("callid", callId);
			//查询拼团订单
			com.lvmama.order.api.base.vo.RequestBody requestBody = new com.lvmama.order.api.base.vo.RequestBody(orderId);
			com.lvmama.order.api.base.vo.ResponseBody<GroupPurchaseOrderInfoVo> responseBody =
					apiGroupOrderDetailService.isGroupOrderByVstOrderId(requestBody);
			
				// 判断是否是发票快递子单（品类是“其它”并且是客服BU）
				if (BizEnum.BIZ_CATEGORY_TYPE.category_other.getCategoryId().equals(order.getCategoryId())
						&& SuppSettlementEntities.CONTRACT_SETTLE_BU.CUSTOMER_SERVICE_CENTER_BU.name().equals(order.getBuCode())) {
					// 此时打开的是发票快递主单详情页
					// 查询旅游主单的Id
					/*com.lvmama.order.api.base.vo.ResponseBody<Long> mainOrderIdResponseBody = apiReviseDateFeeRelatedOrderService
							.queryMainOrderIdByExpressOrderId(new com.lvmama.order.api.base.vo.RequestBody<Long>(orderId));
					if (mainOrderIdResponseBody != null && mainOrderIdResponseBody.getT() != null) {
						// 旅游主单Id
						Long mainTourismOrderId = mainOrderIdResponseBody.getT();
						model.addAttribute("mainTourismOrderId", mainTourismOrderId);
					}*/
				} else {
					// 此时打开的是旅游主单详情页
					// 查询旅游主单对应的发票快递主单（不一定存在）
					/*com.lvmama.order.api.base.vo.ResponseBody<Long> expressOrderIdResponseBody = apiReviseDateFeeRelatedOrderService
							.queryExpressOrderIdByOrderId(new com.lvmama.order.api.base.vo.RequestBody<Long>(orderId));
					if (expressOrderIdResponseBody != null && expressOrderIdResponseBody.getT() != null) {
						// 旅游主单对应的发票快递主单Id
						Long invoiceExpressOrderId = expressOrderIdResponseBody.getT();
						model.addAttribute("invoiceExpressOrderId", invoiceExpressOrderId);
					}*/
				}
			
			if(responseBody.isSuccess() && responseBody.getT() != null){
				model.addAttribute("groupPurchaseOrder", responseBody.getT());
			}
				}
		catch(Exception e){
					LOG.error("OrderDetailAction showOrderStatusManage error", e);
				}
				return "/order/orderStatusManage/allCategory/orderDetails";
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
		private boolean isOrderItemChangable(OrdOrderItem orderItem) {
			if(isShiyuanhuiTicket(orderItem)){
				return true;
			}
			if(DisneyUtils.isDisneyTicket(orderItem)){
				return true;
			}
			if (StringUtils.equals(orderItem.getContentStringByKey(ORDER_TICKET_TYPE.notify_type.name()),
					SuppGoods.NOTICETYPE.QRCODE.name())) {
				PassProvider passProvider = passportService.getPassProvide(orderItem.getSuppGoodsId(), orderItem.getVisitTime());
				if(passProvider != null){
					LOG.info("isOrderItemChangable OrderItemId:" + orderItem.getOrderItemId() + ", ProviderName:" + passProvider.getProviderName());
					if ("万达".equalsIgnoreCase(passProvider.getProviderName())) {
						return true;
					}
					if ("方特新对接".equalsIgnoreCase(passProvider.getProviderName())) {
						return true;
					}
				}
			}
			return false;
		}

		private void buildChangableFlag(Model model, Long orderId, List<OrdOrderItem> orderItemList) {
			List<Long> orderItemIdList = new ArrayList<Long>();
			for (OrdOrderItem ordOrderItem : orderItemList) {
				if (this.isOrderItemChangable(ordOrderItem)) {
					orderItemIdList.add(ordOrderItem.getOrderItemId());
				}
			}
			model.addAttribute("OrdChangableFlag",orderItemIdList.isEmpty() ? Constants.N_FLAG : Constants.Y_FLAG);
			if (! orderItemIdList.isEmpty()) {
				for(int i=0; i<orderItemIdList.size(); i++){
					for (OrdOrderItem ordOrderItem : orderItemList) {
						if(ordOrderItem.getOrderItemId().equals(orderItemIdList.get(i))){
							Long changeTimes[] = getChangeTimes(orderId, ordOrderItem);
							LOG.info("BuildChangableFlag OrderId:" + orderId +", OrderItemId:" + orderItemIdList.get(i) + ", changeTimes:" + changeTimes[0]);
							if (!this.isOrdItemVisitDateChangable(model, orderId, orderItemIdList.get(i), changeTimes[0])) {
								orderItemIdList.remove(orderItemIdList.get(i));
							}
							break;
						}
					}
				}
			}
			model.addAttribute("changableItemIdList", orderItemIdList);
		}

		@RequestMapping(value = "/showChangeVisitDate")
		public String showChangeVisitDate(Model model, Long orderId, Long orderItemId, HttpServletRequest request, HttpServletResponse resp) {
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			OrdOrderItem orderItem = order.getOrderItemByOrderItemId(orderItemId);
			Long changeTimes[] = getChangeTimes(orderId, orderItem);
			LOG.info("ShowChangeVisitDate OrderId:" + orderId +", OrderItemId:" + orderItemId + ", changeTimes:" + changeTimes[0]);
			String changableFlag = this.isOrdItemVisitDateChangable(model, orderId, orderItemId, changeTimes[0]) ? Constants.Y_FLAG : Constants.N_FLAG;
			model.addAttribute("changableFlag", changableFlag);
			model.addAttribute("changeTimes", changeTimes[0]);
			SuppGoods suppGoods = orderItem.getSuppGoods();
			if(changeTimes.length > 1){
				model.addAttribute("changeDays", changeTimes[1]);
				model.addAttribute("visitDate", new SimpleDateFormat("yyyy-MM-dd").format(orderItem.getVisitTime()));
			}
			if(isShiyuanhuiTicket(orderItem)){
				model.addAttribute("visitDate", new SimpleDateFormat("yyyy-MM-dd").format(orderItem.getVisitTime()));
				model.addAttribute("isShiyuanhuiTicket",true);
			}
			if(isFangteTicket(orderItem)){
				model.addAttribute("isFangteTicket",true);
			}
			return "/order/orderVisitDateChange/show";
		}

		/** 判断是否为世园会订单*/
		private boolean isShiyuanhuiTicket(OrdOrderItem orderItem){
			String specialTicketType = (String) orderItem.getContentMap().get(OrderEnum.ORDER_TICKET_TYPE.specialTicketType.name());
			if(SuppGoods.SPECIAL_TICKET_TYPE.SHIYUANHUI_TICKET.name().equalsIgnoreCase(specialTicketType)){
				return true;
			}
			return false;
		}

		private boolean isFangteTicket(OrdOrderItem orderItem){
			if (StringUtils.equals(orderItem.getContentStringByKey(ORDER_TICKET_TYPE.notify_type.name()),
					SuppGoods.NOTICETYPE.QRCODE.name())) {
				PassProvider passProvider = passportService.getPassProvide(orderItem.getSuppGoodsId(), orderItem.getVisitTime());
				if(passProvider != null){
					LOG.info("isOrderItemChangable OrderItemId:" + orderItem.getOrderItemId() + ", ProviderName:" + passProvider.getProviderName());
					if ("方特新对接".equalsIgnoreCase(passProvider.getProviderName())) {
						return true;
					}
				}
			}
			return false;
		}

		private Long[] getChangeTimes(Long orderId, OrdOrderItem orderItem){
			Long changeTimes[] = new Long[]{0L};
			if(isShiyuanhuiTicket(orderItem)){
				changeTimes = new Long[1];
				changeTimes[0] = 1L;
				return changeTimes;
			}
			if (StringUtils.equals(orderItem.getContentStringByKey(ORDER_TICKET_TYPE.notify_type.name()),
					SuppGoods.NOTICETYPE.QRCODE.name())) {
				PassProvider passProvider = passportService.getPassProvide(orderItem.getSuppGoodsId(), orderItem.getVisitTime());
				if(passProvider != null){
					LOG.info("isOrderItemChangable OrderItemId:" + orderItem.getOrderItemId() + ", ProviderName:" + passProvider.getProviderName());
					if ("方特新对接".equalsIgnoreCase(passProvider.getProviderName())) {
						changeTimes = new Long[1];
						changeTimes[0] = 1L;
						return changeTimes;
					}
				}
			}
			if(DisneyUtils.isDisneyTicket(orderItem)){
				changeTimes = new Long[1];
				changeTimes[0] = 2L;
			} else {
				List<SuppOrderResult> resultList =  supplierOrderOtherService.getTicketExchangeMsg(orderId, orderItem.getOrderItemId());
				if (CollectionUtils.isNotEmpty(resultList)) {
					for (SuppOrderResult suppOrderResult : resultList) {
						if (suppOrderResult.isSuccess()) {
							String[] times = StringUtils.isBlank(suppOrderResult.getMemo())?"0".split(","):suppOrderResult.getMemo().split(",");
							changeTimes = new Long[times.length];
							for(int i=0; i<times.length; i++){
								if(StringUtils.isNotBlank(times[i])){
									changeTimes[i] = Long.valueOf(times[i]);
								}
							}
							break;
						} else {
							LOG.info("OrderId:" + orderId +", OrderItemId:" + orderItem.getOrderItemId() + " getTicketExchangeStatus failed, Msg: " + suppOrderResult.getErrMsg());
						}
					}
				} else {
					LOG.info("OrderId:" + orderId +", OrderItemId:" + orderItem.getOrderItemId() + " getTicketExchangeStatus failed, no result!");
				}
			}
			LOG.info("OrderId:" + orderId +", OrderItemId:" + orderItem.getOrderItemId() + " getTicketExchangeStatus success, value: " + Arrays.toString(changeTimes));
			return changeTimes;
		}

		private boolean isOrdItemVisitDateChangable(Model model, Long orderId, Long orderItemId, Long changeTimes) {
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			OrdOrderItem orderItem = order.getOrderItemByOrderItemId(orderItemId);
			Map<String, Object> params =new HashMap<String, Object>();
			params.put("orderItemId", orderItemId);
			params.put("statusType", ORD_ITEM_ADDITION_STATUS_TYPE.CHANGE_STATUS.getCode());
			params.put("status", ORD_ITEM_ADDITION_STATUS.CHANGED.getCode());

			List<OrdItemAdditionStatus> list = ordItemAdditionStatusService.findOrdItemAdditionStatusList(params);
			if (CollectionUtils.isNotEmpty(list)) {
				Long count = list.get(0).getExchangeCount();
				if(isShiyuanhuiTicket(orderItem)){
					if(count.longValue() == 1l){
						LOG.info("OrderItemId:" + orderItemId + " 世园会订单只能改期一次");
						return false;
					}
				}
				if (count != null && count >= changeTimes)
				{
					model.addAttribute("unchangableMsg", count.longValue() == 1? "订单已改期过一次且仅可改期一次" :"订单已改期次数已超过限制");
					LOG.info("OrderItemId:" + orderItemId + " has change record in database");
					return false;
				}
			}

			if ((! ORDER_STATUS.NORMAL.getCode().equals(order.getOrderStatus())) || (! PAYMENT_STATUS.PAYED.getCode().equals(order.getPaymentStatus()))) {
				LOG.info("OrderId:" + orderId +" order status not normal or payment status not payed");
				model.addAttribute("unchangableMsg", "已超过最晚修改订单时间");
				return false;
			}

			if(isFangteTicket(orderItem)){
				List<OrdTicketPerform> resultList = new ArrayList<OrdTicketPerform>();
				//门票业务类订单使用状态
				String performStatusName= PERFORM_STATUS_TYPE.getCnName(OrderUtils.calPerformStatus(resultList,order,orderItem)) ;
				if(performStatusName.equalsIgnoreCase("部分使用") || performStatusName.equalsIgnoreCase("已使用")){
					model.addAttribute("unchangableMsg", "订单已使用");
					return false;
				}
			}

			params.clear();
			params.put("orderItemId", orderItemId);
			List<OrdPassCode> ordPassCodeList = ordPassCodeService.findByParams(params);
			if (CollectionUtils.isEmpty(ordPassCodeList)) {
				LOG.info("OrderItemId:" + orderItemId +" passcode status not applied success");
				return false;
			}
			List<SuppOrderResult> resultList =  supplierOrderOtherService.getTicketExchangeStatus(orderId, orderItemId);
			StringBuilder errorMsg = new StringBuilder();
			if (CollectionUtils.isNotEmpty(resultList)) {
				for (SuppOrderResult suppOrderResult : resultList) {
					if (suppOrderResult.isSuccess()) {
						return true;
					} else {
						errorMsg.append(suppOrderResult.getOrderItemId());
						errorMsg.append(": ");
						errorMsg.append(suppOrderResult.getErrMsg());
						errorMsg.append(", ");
					}
				}
			} else {
				errorMsg.append("no SuppOrderResult");
			}
			LOG.info("OrderId:" + orderId +" received error msg from vst_passport, Msg: " + errorMsg);
			return false;
		}

		@RequestMapping(value = "/changeVisitDate")
		@ResponseBody
		public Object changeVisitDate(Model model, Long orderId, Long orderItemId, Date changeVisitDate, HttpServletRequest request, HttpServletResponse resp) {
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			OrdOrderItem orderItem = order.getOrderItemByOrderItemId(orderItemId);
			Long changeTimes[] = getChangeTimes(orderId, orderItem);
			LOG.info("ChangeVisitDate OrderId:" + orderId +", OrderItemId:" + orderItemId + ", changeTimes:" + changeTimes[0]);
			if (! isOrdItemVisitDateChangable(model, orderId, orderItemId, changeTimes[0])) {
				return  new ResultMessage(ResultMessage.ERROR, "改期失败：只能修改" + changeTimes[0] + "次");
			}

			Pair<Date, List<Long>> resultHandle = orderUpdateService.updateOrderItemVisitTime(orderId, orderItemId, changeVisitDate);
			String itemIdStr = resultHandle.isSuccess() ? StringUtil.connectLongListToString(resultHandle.getSecond()) : null;
			itemIdStr = (itemIdStr == null ? orderItemId.toString(): itemIdStr);
			ComLog.COM_LOG_LOG_TYPE logType = resultHandle.isSuccess() ? ComLog.COM_LOG_LOG_TYPE.CHANGE_VISIT_DATE_SUCCESS : ComLog.COM_LOG_LOG_TYPE.CHANGE_VISIT_DATE_FAIL;
			String changeVisitDateStr = CalendarUtils.getDateFormatString(changeVisitDate, "YYYY-MM-dd");
			String errorMsg = changeVisitDateStr+ " "+ resultHandle.getMsg();
			String logContent = resultHandle.isSuccess() ?
					"【改期成功】子订单"+itemIdStr+ComLogUtil.getLogTxt("游玩日期", changeVisitDateStr, CalendarUtils.getDateFormatString(resultHandle.getFirst(), "YYYY-MM-dd"))
					: ("【改期失败】子订单"+itemIdStr+ " "+ errorMsg);
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					orderId, orderId,
					this.getLoginUser() == null ? null : this.getLoginUser().getUserName(),
					logContent,
					logType.getCode(),
					logType.getCnName(), null);
			if (resultHandle.isSuccess()) {
				return new ResultMessage(ResultMessage.SUCCESS, "改期成功");
			} else {
				return new ResultMessage(ResultMessage.ERROR, errorMsg);
			}
		}
		/******************/
		@RequestMapping(value = "/showOrdReschedule")
		public String showOrdReschedule(Model model, Long orderId, HttpServletRequest request, HttpServletResponse resp) {
			LOG.info("showOrdReschedule orderId:"+orderId);
			OrdOrder ordOrder = complexQueryService.queryOrderByOrderId(orderId);
			SuppGoodsRescheduleVO.OrdRescheduleStatus orderRescheduleStatus = orderRescheduleService.getOrderRescheduleStatus(ordOrder);
			List<OrdOrderItem> orderItemList = ordOrder.getOrderItemList();
			if(CollectionUtils.isNotEmpty(orderItemList)){
				for(OrdOrderItem orderItem:orderItemList){
					if(StringUtils.isNotBlank(orderItem.getMainItem()) && "true".equalsIgnoreCase(orderItem.getMainItem())){
						model.addAttribute("mainItem", orderItem);
					}
				}
			}
			if(BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(ordOrder.getCategoryId())){
				Boolean isMerge = orderRescheduleService.isMerge(ordOrder);
				if(isMerge){
					model.addAttribute("showType","order");
				}else{
					model.addAttribute("showType","orderItem");
				}

			}
			if(BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(ordOrder.getCategoryId()) || BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(ordOrder.getCategoryId())){
				model.addAttribute("showType","order");
			}
			model.addAttribute("suppChangeCount", orderRescheduleStatus.getSuppChangeCount());
			model.addAttribute("ordRescheduleFlag", orderRescheduleStatus.getOrdRescheduleFlag());
			model.addAttribute("orderItemList",orderItemList);
			model.addAttribute("orderId",orderId);
			return "/order/orderReschedule/show";
		}

		@RequestMapping(value = "/ordReschedule")
		@ResponseBody
		public Object changeVisitDate(Model model,@RequestBody SuppGoodsRescheduleVO.RescheduleData rescheduleData, HttpServletRequest request, HttpServletResponse resp) {
			try {
				List<SuppGoodsRescheduleVO.Item> list = rescheduleData.getItems();
				Long orderId = rescheduleData.getOrderId();
				ResultHandle resultHandle = orderRescheduleService.rescheduleCheck(orderId, list);
				if (resultHandle.isFail()) {
					return new ResultMessage(ResultMessage.ERROR, resultHandle.getMsg());
				}
				ResultHandleT<List<SuppGoodsRescheduleVO.Item>> resultHandleT = orderRescheduleService.updateOrderItemVisitTime(orderId, list);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userName", this.getLoginUser() == null ? null : this.getLoginUser().getUserName());
				orderRescheduleService.addRescheduleLog(resultHandleT, orderId, map);
				if (resultHandleT.isSuccess()) {
					return new ResultMessage(ResultMessage.SUCCESS, "改期成功");
				} else {
					return new ResultMessage(ResultMessage.ERROR, resultHandleT.getMsg());
				}
			} catch (OrderException e) {
				return new ResultMessage(ResultMessage.ERROR, e.getMessage());
			}
		}
		/******************/


		/**
		 * 上车点
		 * @param orderId
		 * @return
		 */
		public Map<String,String> findFrontBusStop(Long orderId) {
			Map<String,String> resultMap=new HashMap<String, String>();
			Map<String, Object> parametersOrdFormInfo = new HashMap<String, Object>();
			parametersOrdFormInfo.put("orderId",orderId);
			parametersOrdFormInfo.put("contentType",BuyInfoAddition.frontBusStop.name());
			List<OrdFormInfo> ordFormInfoList=this.ordFormInfoDao.findOrdFormInfoList(parametersOrdFormInfo);
			LOG.info("===parametersOrdFormInfo===" + parametersOrdFormInfo);
			LOG.info("===ordFormInfoList.size===" + ordFormInfoList.size());
			LOG.info("===ordFormInfoList==="+ordFormInfoList);
			String frontBusStop=null;
			if (CollectionUtils.isNotEmpty(ordFormInfoList)) {
				OrdFormInfo ordFormInfo=ordFormInfoList.get(0);
				frontBusStop=ordFormInfo.getContent();
			}
			parametersOrdFormInfo.put("contentType",BuyInfoAddition.backBusStop.name());
			List<OrdFormInfo> ordFormInfoListBack=this.ordFormInfoDao.findOrdFormInfoList(parametersOrdFormInfo);
			LOG.info("===parametersOrdFormInfo.back==="+parametersOrdFormInfo);
			LOG.info("===ordFormInfoListBack.size===" + ordFormInfoListBack.size());
			LOG.info("===ordFormInfoListBack===" + ordFormInfoListBack);
			String backBusStop=null;
			if (CollectionUtils.isNotEmpty(ordFormInfoListBack)) {
				OrdFormInfo ordFormInfoBack=ordFormInfoListBack.get(0);
				backBusStop=ordFormInfoBack.getContent();
			}
			resultMap.put("frontBusStop", frontBusStop);
			resultMap.put("backBusStop", backBusStop);
			LOG.info("==findFrontBusStop=="+resultMap);
			return resultMap;
		}


		private List<OrdTravelContract> findOrdTravelContract(Long orderId) {
			//合同状态
//		OrdTravelContract ordTravelContract=new OrdTravelContract();
			Map<String, Object> parametersTravelContract = new HashMap<String, Object>();
			parametersTravelContract.put("orderId", orderId);
			return ordTravelContractService.findOrdTravelContractList(parametersTravelContract);
		}


		/**
		 * 订单信息展示页面 主订单促销信息
		 * @param model
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "/showOrderPromotionDetails")
		public String showOrderPromotionDetails(Model model, HttpServletRequest request) {
			String orderIdStr = request.getParameter("orderId");
			Long orderId = NumberUtils.toLong(orderIdStr);
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			List<OrdOrderItem> orderItems = order.getOrderItemList();
			List<Long> orderItemIdList = new ArrayList<Long>();
			Map<String,Object> params = new HashMap<String, Object>();
			for (OrdOrderItem orderItem : orderItems) {
				orderItemIdList.add(orderItem.getOrderItemId());
			}

			//打包的情况
			Map<String, Object> paramPack = new HashMap<String, Object>();
			//订单号
			paramPack.put("orderId", orderId);
			List<OrdOrderPack> orderPackList=ordOrderPackService.findOrdOrderPackList(paramPack);
			if (!orderPackList.isEmpty()) {

				List<Long> orderItemIdList1 = new ArrayList<Long>();
				for (OrdOrderPack ordOrderPack : orderPackList) {
					orderItemIdList1.add(ordOrderPack.getOrderPackId());
				}
				params.put("objectType1","ORDER_PACK");
				params.put("orderItemIdList1", orderItemIdList1);
			}
			params.put("objectType","ORDER_ITEM");
			params.put("orderItemIdList", orderItemIdList);

			List<OrdPromotion> ordPromotions = promotionService.selectOrdPromotionsByOrderItemId(params);
			model.addAttribute("ordPromotions", ordPromotions);
			return "/order/orderStatusManage/allCategory/orderPromotionDetail";
		}

		/**
		 * 订单信息展示页面 主订单优惠券明细信息
		 * @param model
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "/showOrderFavorUsageDetails")
		public String showOrderFavorUsageDetails(Model model, HttpServletRequest request) {
			String orderIdStr = request.getParameter("orderId");
			Long orderId = NumberUtils.toLong(orderIdStr);
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			if(order != null){
				try {
					model.addAttribute("markCouponUsageList",couponOrderService.getMarkCouponUsageByOrderId(orderId));
				} catch (Exception e) {
					model.addAttribute("msg", "调用接口异常!");
					LOG.error("showOrderFavorUsageDetails",e);
				}
			}
			return "/order/orderStatusManage/allCategory/orderFavorUsageDetail";
		}


		/**
		 * 显示产品公告
		 * @param model
		 * @return
		 */

		@RequestMapping(value = "/showProductNotice")
		public String showProductNotice(Model model,Long productId,Long orderId){
			Map<String, Object> query = new HashMap<String, Object>();
			query.put("ordOrderId", orderId);
			query.put("productId", productId);
			List<OrdOrderNotice> productNotice = null;
			productNotice = ordProductNotice.findOrdNoticeList_notice(query);

			if(CollectionUtils.isNotEmpty(productNotice)){

				model.addAttribute("productNoticList", productNotice);

			}else{

				return "/order/orderStatusManage/allCategory/noProductNotice";
			}

			return "/order/orderStatusManage/allCategory/showProductNotice";
		}

		/**
		 * 订单信息展示页面 主订单改期服务费记录信息(必须是订单正常且已支付)
		 * @param model
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "/showServiceFeeDetails")
		public String showServiceFeeDetails(Long orderId,Model model, HttpServletRequest request) {
			com.lvmama.order.api.base.vo.RequestBody<Long> requestBody = new com.lvmama.order.api.base.vo.RequestBody<Long>();
			requestBody.setT(orderId);
			//调用改期服务费关联接口,查询获取订单正常且支付的服务费订单数据
			com.lvmama.order.api.base.vo.ResponseBody<List<ReviseDateFeeRelatedOrderVo>> listResponseBody = apiReviseDateFeeRelatedOrderService.queryReviseDateFeeRelatedOrderListByReviseDateOrderId(requestBody);
			List<ReviseDateFeeRelatedOrderVo> reviseDateFeeRelatedOrderVoList = listResponseBody.getT();
			if(CollectionUtils.isNotEmpty(reviseDateFeeRelatedOrderVoList)){
				model.addAttribute("reviseDateFeeRelatedOrderVoList",reviseDateFeeRelatedOrderVoList);
			}
			return "/order/orderStatusManage/allCategory/serviceFeeOrderDetail";
		}

		@RequestMapping(value = "/showOrderFavorO2oDetails")
		public String showOrderFavorO2oDetails(Model model,Long orderId){
			HashMap<String, Object> amountItemParams = new HashMap<String, Object>();
			amountItemParams.put("orderId", orderId);
			amountItemParams.put("itemName", ORDER_AMOUNT_NAME.AMOUNT_NAME_O2OCHANEL.name());
			List<OrdOrderAmountItem> o2oItemList = orderAmountChangeService.findOrderAmountItemList(amountItemParams);
			if(CollectionUtils.isNotEmpty(o2oItemList)){
				for (OrdOrderAmountItem ordOrderAmountItem : o2oItemList) {
					if(null !=ordOrderAmountItem.getItemAmount()){
						double amount = ordOrderAmountItem.getItemAmount()/(double)100;
						Long itemAmount = (long)amount*-1;
						ordOrderAmountItem.setItemAmount(itemAmount);
					}

				}
				model.addAttribute("o2oItemList", o2oItemList);
			}
			return "/order/orderStatusManage/allCategory/orderFavorO2oDetail";
		}

		//供应商备注(门票)
		@RequestMapping(value = "/showSupplierMemo")
		public Object showEbkTicketMemoList(Model model, Long orderId, HttpServletRequest req, HttpServletResponse res){
			if(orderId != null){
//			Map<String,Object> paramsMap = new HashMap<String,Object>();
//			paramsMap.put("objectId", orderId);
//			paramsMap.put("objectType", ComLog.COM_LOG_OBJECT_TYPE.EBK_TICKET_PASS_MEMO.name());
//			ResultHandleT<List<ComLog>> comLogHandle = comLogClientService.queryComLogListByCondition(paramsMap);
//			if(comLogHandle != null){
//				List<ComLog> comLogList = comLogHandle.getReturnContent();
//				model.addAttribute("ebkTicketMemoList", comLogList);
//			}
				Integer curPage = 1;
				Integer pageSize = 100;

				ComLogPams comLogPams = new ComLogPams();
				comLogPams.setSysName(ComLogPams.SYS_NAME.VST);
				comLogPams.setObjectId(orderId);
				comLogPams.setObjectType(ComLog.COM_LOG_OBJECT_TYPE.EBK_TICKET_PASS_MEMO.name());

				com.lvmama.log.comm.bo.ResultHandle<com.lvmama.log.comm.utils.Pagination<com.lvmama.log.comm.po.ComLog>> resultHandle = queryLogClientService.findLog(comLogPams,curPage,pageSize);
				if(resultHandle != null && resultHandle.getT() != null){
					List<com.lvmama.log.comm.po.ComLog> comLogList = resultHandle.getT().getItemList();
					model.addAttribute("ebkTicketMemoList", comLogList);
				}
				model.addAttribute("orderId", orderId);

			}else{
				return new ResultMessage("error", "传递参数错误");
			}
			return "/order/orderStatusManage/allCategory/showEbkTicketMemoList";
		}

		//供应商物流信息(门票-邮寄)
		@RequestMapping(value = "/showSupplierPost")
		public Object showSupplierPost(Model model, Long orderItemId, HttpServletRequest req, HttpServletResponse res){
			//永乐演出票物流信息
			Map<String,String> map = new HashMap<String,String>();
			map.put("orderItemId", String.valueOf(orderItemId));
			List<String> list = passportProductService.queryPassCodeByOrderItemId(map);
			if(!CollectionUtils.isEmpty(list)){
				String serialNo = list.get(0);
				IntfStylOrderInfo intfStylOrderInfo = suppTicketProductClientService.queryBySerialNo(serialNo);
				if(intfStylOrderInfo!=null){
					model.addAttribute("ylPost", intfStylOrderInfo);
					return "/order/orderStatusManage/allCategory/showYlTicketPostList";
				}
			}

			List<EbkTicketPost> postList =new ArrayList<EbkTicketPost>();
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("orderItemId", orderItemId);
			ResultHandleT<List<EbkTicketPost>> resultHandle = ebkTicketPostClientServiceRemote.selectByParams(params);
			if(resultHandle != null){
				postList = resultHandle.getReturnContent();
			}
			model.addAttribute("ebkTicketPostList", postList);
			return "/order/orderStatusManage/allCategory/showEbkTicketPostList";
		}

		/**
		 * 订单价格修改明细
		 * @param model
		 * @param req
		 * @param page
		 * @return
		 */
		@RequestMapping(value="/showAmountChangeQueryList")
		public String showOrdAmountChangeQueryList(Model model,HttpServletRequest req,Integer page){
			String approveStatus = HttpServletLocalThread.getRequest().getParameter("approveStatus");
			if(approveStatus==null)
				approveStatus = OrdAmountChange.APPROVESTATUS.TOAPPROVE.name();

			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("objectType", HttpServletLocalThread.getRequest().getParameter("objectType"));
			params.put("orderId", HttpServletLocalThread.getRequest().getParameter("orderId"));
			params.put("approveStatus", approveStatus);
			//查询总行数
			Integer counts = orderAmountChangeService.findOrdAmountChangeCounts(params);
			int pagenum = page == null ? 1 : page;
			Page<OrdAmountChange> pageParam = Page.page(counts, 20, pagenum);
			pageParam.buildUrl(req);
			params.put("_start", pageParam.getStartRows());
			params.put("_end", pageParam.getEndRows());
			List<OrdAmountChange> list = orderAmountChangeService.findOrdAmountChangeList(params);
			pageParam.setItems(list);

			for (int i = 0; i < list.size(); i++) {

				OrdAmountChange ordAmountChange=list.get(i);

				Map<String, Object> map=ordAmountChange.getContentMap();

				if ("ORDER_ITEM".equals(ordAmountChange.getObjectType()) && !map.isEmpty()) {

					StringBuilder amountChangeDesc=new StringBuilder();
					for (Map.Entry<String, Object> m : map.entrySet()) {
						if ("price".equals(m.getKey())) {
							amountChangeDesc.append("销售价");
						}else{
							amountChangeDesc.append(ORDER_PRICE_RATE_TYPE.getCnName(m.getKey()));
						}

						Long amountChange=NumberUtils.toLong(m.getValue()+"");

						amountChangeDesc.append(":").append(PriceUtil.trans2YuanStr(amountChange));
						amountChangeDesc.append("</br>");
					}
//				for (String key : map.keySet()) {
//
//					if ("price".equals(key)) {
//						amountChangeDesc.append("销售价");
//					}else{
//						amountChangeDesc.append(OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(key));
//					}
//
//					Long amountChange=NumberUtils.toLong(map.get(key)+"");
//
//					amountChangeDesc.append(":").append(PriceUtil.trans2YuanStr(amountChange));
//					amountChangeDesc.append("</br>");
//				}

					ordAmountChange.setAmountChangeDesc(amountChangeDesc.toString());

//				OrdOrderItem ordItem=this.orderUpdateService.getOrderItem(ordAmountChange.getObjectId());

				}else{

					ordAmountChange.setAmountChangeDesc(PriceUtil.trans2YuanStr(ordAmountChange.getAmount()));
				}
			}

			model.addAttribute("pageParam", pageParam);
			model.addAttribute("objectId", HttpServletLocalThread.getRequest().getParameter("orderId"));
			model.addAttribute("approveStatus", approveStatus);
			return "/order/orderStatusManage/allCategory/showAmountChangeList";
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
//				throw new RuntimeException("调用支付接口获取结算状态异常---"+e.getMessage());
			}
			return  ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.name();
		}




		@RequestMapping(value = "/showChildMergeOrderStatusManage")
		public String showChildMergeOrderStatusManage(Model model, HttpServletRequest request,Long orderItemId)
		{
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<showChildMergeOrderStatusManage>");
			}

			OrdOrderItem orderItem=ordOrderUpdateService.getOrderItem(orderItemId);
			OrdOrder order = ordOrderUpdateService.queryOrdOrderByOrderId(orderItem.getOrderId());
			//complexQueryService.queryOrderByOrderId(orderItem.getOrderId());

			Long orderId=order.getOrderId();
			model.addAttribute("order", order);
			model.addAttribute("orderItem", orderItem);

			Map<String,Object> contentMap = orderItem.getContentMap();
			model.addAttribute("contentMap", contentMap);


			//子订单负责人
			String objectType="ORDER_ITEM";
			Long objectId=orderItemId;
			PermUser permUser= orderResponsibleService.getOrderPrincipal(objectType, objectId);
			model.addAttribute("orderPrincipal",permUser.getRealName());


			//请勿删除
			int messageCount;
			Long[] auditIdArray = findBookingAuditIds("ORDER_ITEM",orderItemId,"");
			if (auditIdArray.length==0) {
				messageCount=0;
			}else{
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("messageStatus", MESSAGE_STATUS.UNPROCESSED.getCode());
				parameters.put("auditIdArray",auditIdArray);
				//parameters.put("receiver",loginUserId);

				messageCount=comMessageService.findComMessageCount(parameters);
			}
			model.addAttribute("messageCount", messageCount);



			//附件数量
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("orderId", orderId);
			param.put("orderItemId", orderItem.getOrderItemId());
			int orderAttachmentNumber = orderAttachmentService.countOrderAttachmentByCondition(param);
			model.addAttribute("orderAttachmentNumber", orderAttachmentNumber);

			//主订单附件数量
			param.put("orderId", orderId);
			param.put("orderItemId", 0L);
			model.addAttribute("parentOrderAttachmentNumber", orderAttachmentService.countOrderAttachmentByCondition(param));


			//审核状态
			model.addAttribute("hasInfoAndResourcePass", order.hasInfoAndResourcePass());

			//支付状态
			model.addAttribute("paymentStatusStr", PAYMENT_STATUS.getCnName(order.getPaymentStatus()));

			//合同状态
			OrdTravelContract ordTravelContract=new OrdTravelContract();
			List<OrdTravelContract>  ordTravelContractList  = findOrdTravelContract(orderId);
			if (CollectionUtils.isNotEmpty(ordTravelContractList)) {
				ordTravelContract=ordTravelContractList.get(0);
			}
			model.addAttribute("ordTravelContract",ordTravelContract);
			model.addAttribute("contractStatusName", ORDER_TRAVEL_CONTRACT_STATUS.getCnName(ordTravelContract.getStatus()));

			//活动操作逻辑判断
			boolean isDonePretrialAudit = this.isDonePretrialAudit(order);
			model.addAttribute("isDonePretrialAudit", isDonePretrialAudit);

			boolean isDoneCertificate=this.isDoneCertificate(orderItem.getCertConfirmStatus());
			model.addAttribute("isDoneCertificate", isDoneCertificate);

			boolean isDoneCancleConfirmedtAudit =this.isDoneCancleConfirmedAudit(order);
			model.addAttribute("isDoneCancleConfirmedtAudit", isDoneCancleConfirmedtAudit);

			model.addAttribute("auditMap", this.getOrderItemUnprocessedAudit(orderItem));



			//查看回传
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderId", orderId);//订单号
			String readUserStatus =EbkFaxVO.READ_USER_STATUS_YES;
			params.put("isReadUser", readUserStatus);//是否读取，Y：已读取 N：未读取
			params.put("cancelFlag", "Y");//是否有效
			ResultHandleT<Integer> resultHandleInteger = ebkFaxTaskClientService.findEbkFaxRecvCount(params);
			if (resultHandleInteger.isSuccess()) {
				model.addAttribute("ebkCount",resultHandleInteger.getReturnContent());
			}

			//订单状态
			model.addAttribute("orderStatusStr", ORDER_STATUS.getCnName(order.getOrderStatus()));
			//结算状态
			/**
			 * 2017/03/06 结算状态改造
			 */
			//model.addAttribute("settlementStatusStr", OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.getCnName(orderItem.getSettlementStatus()));

			model.addAttribute("settlementStatusStr", ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.getCnName(getSetSettlementItemStatus(orderItem.getOrderItemId())));



			//订单取消类型
			Map<String, Object> dictDefPara = new HashMap<String, Object>();
			dictDefPara.put("dictCode", Constants.ORDER_CANCEL_TYPE);
			dictDefPara.put("cancelFlag", "Y");
			List<BizDictDef> dictDefs = dictDefClientService.findDictDefList(dictDefPara).getReturnContent();
			boolean isStart = false;
			//当订单为目的地BU，且是酒店
			if (BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())
					&& CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())
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




			return "/order/orderStatusManage/allCategory/orderChildMergeDetails";
		}

		@RequestMapping(value = "/showChildOrderStatusManage")
		public String showChildOrderStatusManage(Model model, HttpServletRequest request,Long orderItemId) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<showChildOrderStatusManage>");
			}
//		String loginUserId=this.getLoginUserId();


			OrdOrderItem orderItem=ordOrderUpdateService.getOrderItem(orderItemId);
//		OrdOrder order = ordOrderUpdateService.queryOrdOrderByOrderId(orderItem.getOrderId());
			OrdOrder order=complexQueryService.queryOrderByOrderId(orderItem.getOrderId());
			if( "STAMP".equals(order.getOrderSubType())){
				getStampOrdOrder(order,model);
			}
			//当地玩乐交通接驳的配置
			this.configOrderConnectsServiceProp(model,order,orderItem);

			Long orderId=order.getOrderId();
			model.addAttribute("order", order);
			model.addAttribute("orderItem", orderItem);

			//如果是酒店并且是对接则查询供应商订单号
			if(orderItem.getCategoryId()==1 && orderItem.hasSupplierApi()){
				//获取suppOrder信息
				LOG.info("OrderDetailAction===showChildOrderStatusManage OrderItemId="+orderItem.getOrderItemId());
				RequestSuppOrder suppOrder = suppOrderClientService.getSuppOrderByOrderItemId(orderItem.getOrderItemId());
				if(suppOrder != null){
					String suppOrderId = suppOrder.getSuppOrderId();
					model.addAttribute("suppOrderId", suppOrderId);
				}
			}

			//如果是酒店套餐并且是对接则查询供应商订单号
			if(orderItem.getCategoryId()==17 && orderItem.hasSupplierApi()){
				//获取suppOrder信息
				LOG.info("OrderDetailAction===showChildOrderStatusManage OrderItemId="+orderItem.getOrderItemId());
				RequestSuppOrder suppOrder = suppOrderClientService.getSuppOrderByOrderItemId(orderItem.getOrderItemId());
				if(suppOrder != null){
					String suppOrderId = suppOrder.getSuppOrderId();
					model.addAttribute("suppOrderId", suppOrderId);
				}
			}

			Map<String,Object> contentMap = orderItem.getContentMap();
			model.addAttribute("contentMap", contentMap);


			//子订单负责人
			String objectType="ORDER_ITEM";
			Long objectId=orderItemId;
			PermUser permUser= orderResponsibleService.getOrderPrincipal(objectType, objectId);
			model.addAttribute("orderPrincipal",permUser.getRealName());


			//请勿删除
			int messageCount;
			Long[] auditIdArray = findBookingAuditIds("ORDER_ITEM",orderItemId,"");
			if (auditIdArray.length==0) {
				messageCount=0;
			}else{
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("messageStatus", MESSAGE_STATUS.UNPROCESSED.getCode());
				parameters.put("auditIdArray",auditIdArray);
				//parameters.put("receiver",loginUserId);

				messageCount=comMessageService.findComMessageCount(parameters);
			}
			model.addAttribute("messageCount", messageCount);



			//附件数量
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("orderId", orderId);
			param.put("orderItemId", orderItem.getOrderItemId());
			int orderAttachmentNumber = orderAttachmentService.countOrderAttachmentByCondition(param);
			model.addAttribute("orderAttachmentNumber", orderAttachmentNumber);

			//主订单附件数量
			param.put("orderId", orderId);
			param.put("orderItemId", 0L);
			model.addAttribute("parentOrderAttachmentNumber", orderAttachmentService.countOrderAttachmentByCondition(param));


			//审核状态
			model.addAttribute("hasInfoAndResourcePass", order.hasInfoAndResourcePass());

			//支付状态
			model.addAttribute("paymentStatusStr", PAYMENT_STATUS.getCnName(order.getPaymentStatus()));

			//合同状态
			OrdTravelContract ordTravelContract=new OrdTravelContract();
			List<OrdTravelContract>  ordTravelContractList  = findOrdTravelContract(orderId);
			if (CollectionUtils.isNotEmpty(ordTravelContractList)) {
				ordTravelContract=ordTravelContractList.get(0);
			}
			model.addAttribute("ordTravelContract",ordTravelContract);
			model.addAttribute("contractStatusName", ORDER_TRAVEL_CONTRACT_STATUS.getCnName(ordTravelContract.getStatus()));

			//酒店子单添加确认操作
			Map<String, Object> searchAuditMap = new HashMap<String,Object>();
			String cancelConfirmAudit = "CANCEL_CONFIRM_AUDIT";
			searchAuditMap.put("auditType", cancelConfirmAudit);
			searchAuditMap.put("objectId", orderItemId);
			List<ComAudit> comAuditList = orderAuditService.queryAuditListByParam(searchAuditMap);
			if(CollectionUtils.isNotEmpty(comAuditList)){
				model.addAttribute("showStatusChange", "Y");
				model.addAttribute("showStatusChangeProcessed", comAuditList.get(0).getAuditStatus());
				model.addAttribute("auditId",comAuditList.get(0).getAuditId());
			}

			boolean isDonePretrialAudit = this.isDonePretrialAudit(order);
			model.addAttribute("isDonePretrialAudit", isDonePretrialAudit);


			boolean isDoneCertificate=this.isDoneCertificate(orderItem.getCertConfirmStatus());
			model.addAttribute("isDoneCertificate", isDoneCertificate);

			boolean isDoneChildCancleConfirmedAudit =this.isDoneChildCancleConfirmedAudit(orderItem, CollectionUtils.isNotEmpty(comAuditList));
			model.addAttribute("isDoneChildCancleConfirmedAudit", isDoneChildCancleConfirmedAudit);

			model.addAttribute("auditMap", this.getOrderItemUnprocessedAudit(orderItem));



			//查看回传
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderId", orderId);//订单号
			String readUserStatus =EbkFaxVO.READ_USER_STATUS_YES;
			params.put("isReadUser", readUserStatus);//是否读取，Y：已读取 N：未读取
			params.put("cancelFlag", "Y");//是否有效
			ResultHandleT<Integer> resultHandleInteger = ebkFaxTaskClientService.findEbkFaxRecvCount(params);
			if (resultHandleInteger.isSuccess()) {
				model.addAttribute("ebkCount",resultHandleInteger.getReturnContent());
			}

			//订单状态
			model.addAttribute("orderStatusStr", ORDER_STATUS.getCnName(order.getOrderStatus()));
			//结算状态
			/**
			 * 2017/03/06 结算状态改造
			 */
			//model.addAttribute("settlementStatusStr", OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.getCnName(orderItem.getSettlementStatus()));

			model.addAttribute("settlementStatusStr", ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.getCnName(getSetSettlementItemStatus(orderItem.getOrderItemId())));


			//订单取消类型
			Map<String, Object> dictDefPara = new HashMap<String, Object>();
			dictDefPara.put("dictCode", Constants.ORDER_CANCEL_TYPE);
			dictDefPara.put("cancelFlag", "Y");
			List<BizDictDef> dictDefs = dictDefClientService.findDictDefList(dictDefPara).getReturnContent();
			boolean isStart = false;
			//当订单为目的地BU，且是酒店
			if (BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())
					&& CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())
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

			// 机票子订单,内嵌页地址
			if (orderItem.isApiFlightTicket()) {
				String flightOrderDetailUrl = Constant.getInstance().getFlightOrderDetailUrl();
				model.addAttribute("flightOrderDetailUrl", MessageFormat.format(flightOrderDetailUrl,
						String.valueOf(orderItem.getOrderId()), String.valueOf(orderItem.getOrderItemId())));
			}

			//目的地酒店套餐,自由行景+酒。子订单显示酒店预定号，Added by yangzhenzhong
			if(CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equalsIgnoreCase(order.getBuCode()) ){

				if(BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())
						|| (order.getSubCategoryId()!=null && BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())
						&& BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId()))){

					ResultHandleT<List<EbkCertifItem>> resultHandle=ebkFaxTaskClientService.selectEbkCertifItemListByOrderItemId(orderItemId);
					if(resultHandle.getReturnContent()!=null && resultHandle.getReturnContent().size()>0){
						//获取最新创建的数据
						EbkCertifItem ebkCertifItem = resultHandle.getReturnContent().get(resultHandle.getReturnContent().size()-1);
						model.addAttribute("supplierNo",ebkCertifItem.getSupplierNo());
					}
				}
			}
			//国内酒店子单添加酒店预订号
			if (CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())) {
				if(BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId())
						|| (order.getSubCategoryId()!=null && BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(order.getSubCategoryId()))){

					ResultHandleT<List<EbkCertifItem>> resultHandle=ebkFaxTaskClientService.selectEbkCertifItemListByOrderItemId(orderItemId);
					if(resultHandle.getReturnContent()!=null && resultHandle.getReturnContent().size()>0){
						//获取最新创建的数据
						EbkCertifItem ebkCertifItem = resultHandle.getReturnContent().get(resultHandle.getReturnContent().size()-1);
						model.addAttribute("supplierNo",ebkCertifItem.getSupplierNo());
					}
				}
			}
			//目的地BU前台下单特殊显示(排除巴士+酒)
			model.addAttribute("isDestBuFrontHotelOrderItem", OrdOrderUtils.isDestBuFrontOrder(order)
					&& ((BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == orderItem.getCategoryId() && !orderItem.hasSupplierApi())
					|| BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId() == orderItem.getCategoryId()));

			if((BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())||BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode()))
					&&BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()==order.getCategoryId().longValue()
					&&BIZ_CATEGORY_TYPE.category_route_bus_hotel.getCategoryId().longValue()==order.getSubCategoryId()){
				model.addAttribute("isDestBuFrontHotelOrderItem",false);
			}

			//董宁波 2016年4月17日 15:57:09 酒店类型显示规则 start
			List<OrdOrderStock> stockList = orderStockService.findOrderStockListByOrderItemId(orderItem.getOrderItemId());
			orderItem.setOrderStockList(stockList);
			model.addAttribute("isPartStockFlag", orderItem.getRoomReservations());
			//end
			//是否需要显示订单状态
			if((BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())
					&&("3.0".equals(order.getWorkVersion())||"3.1".equals(order.getWorkVersion()))&&!order.isCancel()
					&&((BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId()))
					|| (BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId())
					&& BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())))
					&&((BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId()))
					|| BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())))
					||OrdOrderUtils.isLocalBuOrderNew(order)){
				if("SUCCESS".equals(orderItem.getConfirmStatus())){
					model.addAttribute("isNeedShowConfirmStatus",true);
				}else{
					Map<String, Object> auditParam = new HashMap<String, Object>();
					auditParam.put("auditStatus", AUDIT_STATUS.UNPROCESSED.name());
					auditParam.put("objectType", AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
					List<Long> categoryIds = Arrays.asList(
							BIZ_CATEGORY_TYPE.category_hotel.getCategoryId(),
							BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId(),
							BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId());
					auditParam.put("categoryIds", categoryIds);
					auditParam.put("objectId", orderItemId);
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
			String bu = orderItem.getRealBuType();

			//传真方式
			model.addAttribute("faxFlag",orderItem.getContentValueByKey(ORDER_COMMON_TYPE.fax_flag.name()));

			if (isDestBu(order, orderItem, bu)) {
				String cancelCertConfirmStatus = ebkCertifClientService.checkCertifCancelApply(orderItem.getOrderItemId());
				model.addAttribute("cancelCertConfirmStatus", cancelCertConfirmStatus);
			} else {
				//是否显示重新取消的按钮
				model.addAttribute("isReCancelBtn",ebkCertifClientService.checkCertifCancelApply(orderItemId));
			}


			return "/order/orderStatusManage/allCategory/orderChildDetails";
		}

//		@RequestMapping("/orderCancelConfirm")
//		@ResponseBody
//		public Object orderCancelConfirm(Model model, Long orderItemId, Long auditId, HttpServletRequest request) {
//			ResultMessage msg = ResultMessage.createResultMessage();
//			msg.setCode(ResultMessage.SUCCESS);
//			try {
//				if (auditId == null || orderItemId == null) {
//					msg.setCode(ResultMessage.ERROR);
//					msg.setMessage("参数不能为空");
//					return msg;
//				}
//				LOG.info("OrderItemConfirmStatusAction orderCancelConfirm orderItemId:" + orderItemId + ",auditId:" + auditId);
//				OrdOrderItem ordOrderItem = orderItemService.selectOrderItemByOrderItemId(orderItemId);
//				if (ordOrderItem == null) {
//					msg.setCode(ResultMessage.ERROR);
//					msg.setMessage("记录不存在");
//					return msg;
//				}
//				LOG.info("OrderItemConfirmStatusAction orderCancelConfirm ordOrderItem orderItemId:" + ordOrderItem.getOrderItemId() + ",confirmStatus:" + ordOrderItem.getConfirmStatus());
//				String operateName = getLoginUserId();
//				ResultHandle handle = ordItemConfirmStatusService.cancelConfirm(auditId, operateName);
//				if (handle.isFail()) {
//					LOG.info("OrderItemConfirmStatusAction orderCancelConfirm orderItemId:" + orderItemId + ",cancelConfirm error!msg:" + handle.getMsg());
//				}
//				LOG.info("OrderItemConfirmStatusAction orderCancelConfirm orderItemId:" + orderItemId);
//			} catch (Exception e) {
//				msg.setCode(ResultMessage.ERROR);
//				msg.setMessage("运行出现异常"+e);
//				LOG.error("OrderItemConfirmStatusAction orderCancelConfirm error,msg:" + e.getMessage());
//				e.printStackTrace();
//			}
//			return msg;
//		}

		/**
		 * 品类库存修改
		 * @param model
		 * @param req
		 * @return
		 */
		@RequestMapping(value = "/goodsInventoryChange")
		public ModelAndView  goodsInventoryChange(Model model,Long suppGoodsId,Long productId,Long categoryId,HttpServletRequest req){
			ResultHandleT<BizCategory> result=categoryClientService.findCategoryById(categoryId);
			BizCategory category=result.getReturnContent();
			if(category == null){
				throw new BusinessException("品类不存在");
			}

			ResultHandleT<SuppGoods> resultHandleSuppGoods = suppGoodsClientService.findSuppGoodsById(suppGoodsId, Boolean.TRUE, Boolean.TRUE);
			SuppGoods suppGoods = new SuppGoods();
			if (resultHandleSuppGoods.isSuccess() && resultHandleSuppGoods.getReturnContent() != null) {
				suppGoods = resultHandleSuppGoods.getReturnContent();
			}
			//酒店品类
			if("category_hotel".equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/goods/timePrice/showGoodsTimePrice.do?prodProduct.productId="+productId+"&prodProductBranch.bizBranch.branchId="+suppGoods.getBranchId()+"&suppSupplier.supplierId="+suppGoods.getSupplierId()+"&suppSupplier.supplierName="+suppGoods.getSuppSupplier().getSupplierName());
				//邮轮品类
			}else if("category_cruise".equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/ship/goods/timePrice/showGoodsTimePrice.do?prodProduct.productId="+productId+"&prodProductBranch.bizBranch.branchId="+suppGoods.getBranchId()+"&suppSupplier.supplierId="+suppGoods.getSupplierId()+"&suppSupplier.supplierName="+suppGoods.getSuppSupplier().getSupplierName()+"&cancelFlag=Y");
				//签证品类
			}else if("category_visa".equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/visa/goods/timePrice/showGoodsTimePrice.do?prodProduct.productId="+productId+"&prodProductBranch.bizBranch.branchId="+suppGoods.getBranchId()+"&suppSupplier.supplierId="+suppGoods.getSupplierId()+"&suppSupplier.supplierName="+suppGoods.getSuppSupplier().getSupplierName());
				//岸上观光
			}else if("category_sightseeing".equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/goods/shoreExcursions/showGoodsTimePrice.do?prodProduct.productId="+productId+"&prodProductBranch.bizBranch.branchId="+suppGoods.getBranchId()+"&suppSupplier.supplierId="+suppGoods.getSupplierId()+"&suppSupplier.supplierName="+suppGoods.getSuppSupplier().getSupplierName()+"&cancelFlag=Y");
				//邮轮组合产品
			}else if("category_comb_cruise".equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/prod/compship/baseinfo/toCruiseCombPage.do?categoryId="+categoryId+"&productId="+productId+"&categoryName="+category.getCategoryName()+"&isView=null");
				//邮轮附加项
			}else if("category_cruise_addition".equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/goods/shipAddition/showGoodsTimePrice.do?prodProduct.productId="+productId+"&prodProductBranch.bizBranch.branchId="+suppGoods.getBranchId()+"&suppSupplier.supplierId="+suppGoods.getSupplierId()+"&suppSupplier.supplierName="+suppGoods.getSuppSupplier().getSupplierName()+"&cancelFlag=Y");
				//跟团游
			}else if(BIZ_CATEGORY_TYPE.category_route_group.name().equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/tour/goods/timePrice/showGoodsTimePrice.do?categoryId="+categoryId+"&prodProductId="+productId);
				//自由行
			}else if(BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/tour/goods/timePrice/showGoodsTimePrice.do?categoryId="+categoryId+"&prodProductId="+productId);
				//当地游
			}else if(BIZ_CATEGORY_TYPE.category_route_local.name().equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/tour/goods/timePrice/showGoodsTimePrice.do?categoryId="+categoryId+"&prodProductId="+productId);
				//酒店套餐
			}else if(BIZ_CATEGORY_TYPE.category_route_hotelcomb.name().equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/tour/goods/timePrice/showGoodsTimePrice.do?categoryId="+categoryId+"&prodProductId="+productId);
				//定制游
			}else if(BIZ_CATEGORY_TYPE.category_route_customized.name().equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/tour/goods/timePrice/showGoodsTimePrice.do?categoryId="+categoryId+"&prodProductId="+productId);
				//景点门票
			}else if("category_single_ticket".equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/ticket/goods/timePrice/showGoodsTimePrice.do?suppGoodsId="+suppGoodsId+"&prodProduct.productId="+productId);
				//其它票
			}else if("category_other_ticket".equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/ticket/goods/timePrice/showGoodsTimePrice.do?suppGoodsId="+suppGoodsId+"&prodProduct.productId="+productId);
				//组合套餐票
			}else if("category_comb_ticket".equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/ticket/goods/timePrice/showGoodsTimePrice.do?suppGoodsId="+suppGoodsId+"&prodProduct.productId="+productId);
				//玩乐演出票
			}else if("category_show_ticket".equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/ticket/goods/timePrice/showGoodsTimePrice.do?suppGoodsId="+suppGoodsId+"&prodProduct.productId="+productId);
				//保险
			}else if("category_insurance".equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/insurance/goods/showSuppGoodsList.do?productId="+productId);
				//飞机
			}else if("category_traffic_aero_other".equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/prod/traffic/showProductMaintain.do?categoryId="+categoryId);
				//火车
			}else if("category_traffic_train_other".equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/prod/traffic/showProductMaintain.do?categoryId="+categoryId);
				//轮船
			}else if("category_traffic_ship_other".equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/prod/traffic/showProductMaintain.do?categoryId="+categoryId);
				//巴士
			}else if("category_traffic_bus_other".equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/prod/traffic/showProductMaintain.do?categoryId="+categoryId);
				//其它
			}else if("category_other".equalsIgnoreCase(category.getCategoryCode())){
				return new ModelAndView("redirect:http://super.lvmama.com/vst_back/other/prod/product/showProductMaintain.do?categoryId="+categoryId);
			}
			//throw new BusinessException("未设置跳转地址");
			return new ModelAndView("redirect:http://super.lvmama.com/vst_back/prod/baseProduct/productNoRealize.do");

		}


		@RequestMapping(value = "/logList")
		public String showlogList(Model model,Long objectId,String objectType,Integer page,HttpServletRequest req){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<showlogList>");
			}
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("objectType", objectType);
			parameters.put("objectId",objectId);
			int count = comLogClientService.getTotalCount(parameters).getReturnContent().intValue();

			int pagenum = page == null ? 1 : page;
			Page<ComLog> pageParam = Page.page(count, 10, pagenum);
			pageParam.buildJSONUrl(req);
			//pageParam.buildJSONUrl(req,true);
			parameters.put("_start", pageParam.getStartRows());
			parameters.put("_end", pageParam.getEndRows());
			parameters.put("_orderby","Com_Log.create_time desc");
			List<ComLog> logList=comLogClientService.queryComLogListByCondition(parameters).getReturnContent();
			pageParam.setItems(logList);

			model.addAttribute("pageParam", pageParam);

			model.addAttribute("logList", logList);

			return "/order/orderStatusManage/findLogList";
		}


		/**
		 * 查看日志,包括录音文件(如果存在)
		 * @param model
		 * @param objectId
		 * @param objectType
		 * @param page
		 * @param req
		 * @return
		 */
		@SuppressWarnings("unused")
		@RequestMapping(value = "/showSoundRecList")
		public String showSoundRecList(Model model,Long objectId,String objectType,Integer page,HttpServletRequest req){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<OrderDetailAction.showlogAndSoundRecList>");
			}
/*		Map<String, String> urlParamMap = new HashMap<String, String>();
		urlParamMap.put("objectType", objectType);
		urlParamMap.put("objectId", objectId.toString());
		urlParamMap.put("sysName", "VST");
		Integer curPage = (page == null || page <= 0)?1:page;
		urlParamMap.put("curPage", curPage.toString());
		Page<ComLog> comLogPage = CallCenterUtils.getRealComLogPageInfo(objectId, curPage, urlParamMap);*/

			Page<ComLog> comLogPage = CallCenterUtils.adapterOrderCallToComLog(objectId, orderCallIdService);


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
		public void saveCallIdAndOrderId(Model model,Long objectId, String callId){
			JSONObject jsonResult=new JSONObject();
			if (StringUtils.isNotBlank(callId) && objectId != null){
				try{
					DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String currDateTime = f.format(new Date());
					String createDateOfCallid = getRequest().getParameter("createdateofcallid")==null?currDateTime:this.getRequest().getParameter("createdateofcallid");
					String content = CallCenterUtils.getContent(objectId, callId, createDateOfCallid);
					String operatorName = getLoginUserId();

/*				//保存到日志表(先判断是否已经关联过了.规则:parentId为objectId+CallCenterUtils.PARENT_DELTA)
				Map<String, String> urlParamMap = new HashMap<String, String>();
				urlParamMap.put("objectType", ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER.getCode());
				urlParamMap.put("objectId", objectId.toString());
				urlParamMap.put("sysName", "VST");
				urlParamMap.put("curPage", "1");
				//urlParamMap.put("content", content);未提供content条件
				urlParamMap.put("parentId", String.valueOf(objectId + CallCenterUtils.PARENT_DELTA));

				Page<ComLog> comLogPage = CallCenterUtils.getOrdLogInfo(objectId, 1L, urlParamMap);
				if (comLogPage == null || comLogPage.getItems() == null || comLogPage.getItems().size() <= 0 ||
						comLogPage.getTotalResultSize() <= 0 || !CallCenterUtils.isExistsCallIdJoin(comLogPage, objectId, callId, createDateOfCallid)){
					comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER, objectId + CallCenterUtils.PARENT_DELTA, objectId, operatorName,
							content, ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_EBK.getCode(),ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_EBK.getCnName() , "");
				}*/

					//保存到order_callid表
					if (orderCallIdService != null){
						OrderCallId orderCallId = new OrderCallId();
						orderCallId.setCallId(callId);
						orderCallId.setOrderId(objectId);
						orderCallId.setOperUserName(operatorName);
						orderCallIdService.insert(orderCallId);
					}

					jsonResult.put("err", "N");
					jsonResult.put("msg", "");


				}
				catch(Exception e){
					jsonResult.put("err", "Y");
					jsonResult.put("msg", e.getMessage());
				}
			}
			else{
				jsonResult.put("err", "Y");
				jsonResult.put("msg", "callId或orderId为空");
			}

			sendAjaxResultByJson(jsonResult.toString());
		}


		/**
		 * @param order
		 * @param orderItem
		 * @param orderAttachment
		 * @return
		 */
		@RequestMapping(value = "/updateOrderStatus")
		@ResponseBody
		public Object updateOrderStatus( HttpServletRequest request,OrdOrder order,OrdOrderItem orderItem,OrderAttachment orderAttachment) {
			if (log.isDebugEnabled()) {
				log.debug("start method<updateOrderStatus>");
			}
			ResultHandle result=new ResultHandle();
			try {
				String operation=request.getParameter("operation");
				String cancelCode=request.getParameter("cancelCode");
				String cancleReasonText=request.getParameter("cancleReasonText");
				//String orderRemark=request.getParameter("orderRemark");
				String orderRemark=request.getParameter("orderRemark");
				Long orderId=order.getOrderId();
				OrdOrder oldOrder = complexQueryService.queryOrderByOrderId(orderId);
				//OrdOrderItem oldOrderItem=oldOrder.getOrderItemIdList().get(0);

				//UserUser loginUser=this.getLoginUser();
				//String loginUserName=loginUser.getUserName();
				String loginUserId=this.getLoginUserId();
				String newStatus="";
				String name="";
				String msg="当前订单操作状态已被修改:";

				if ("updateOrderRemark".equals(operation)) {

					orderStatusManageService.updateOrderMemo(orderId, orderRemark);

					lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
							orderId,
							orderId,
							loginUserId,
							"将编号为["+orderId+"]的订单，更新订单备注",
							ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
							ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"更新订单备注",
							orderRemark);


				}else if ("paymentAudit".equals(operation)) {//催支付

					if (isDonePaymentAudit(oldOrder)) {
						result.setMsg(msg+" 催支付活动状态处于已处理，不可再次催支付  ");
					}else{
						result = orderStatusManageService.updatePaymentAudit(oldOrder, loginUserId, orderRemark , AUDIT_TYPE.PAYMENT_AUDIT.getCode());
					}
				}else if ("timePaymentAudit".equals(operation)) {//小驴分期催支付

					if (isDoneTimePaymentAudit(oldOrder)) {
						result.setMsg(msg+" 小驴分期催支付活动状态处于已处理，不可再次催支付  ");
					}else{
						result = orderStatusManageService.updatePaymentAudit(oldOrder, loginUserId, orderRemark, AUDIT_TYPE.TIME_PAYMENT_AUDIT.getCode());
					}
				}else if ("noticeRegimentAudit".equals(operation)) {//通知出团

					if (isDoneNoticeRegimentAudit(oldOrder)) {
						result.setMsg(msg+" 通知出团活动状态处于已处理，不可再次通知出团 ");
					}else{
						result = orderStatusManageService.updateNoticeRegimentAudit(oldOrder, loginUserId, orderRemark);
					}
				}else if ("cancelStatusConfim".equals(operation)) {//取消订单已确认

					result =orderLocalService.updateCancelConfim(oldOrder, loginUserId, orderRemark);
				}else if ("onlineRefundConfirm".equals(operation)) {//在线退款已确认
					result =orderLocalService.updateOnlineRefundConfim(oldOrder, loginUserId, orderRemark);
				}else if ("pretrialAudit".equalsIgnoreCase(operation)){
					result = orderLocalService.updatePretrialAudit(oldOrder, loginUserId, orderRemark);
				}else if ("cancelStatus".equals(operation)) {

					if (oldOrder.getOrderStatus().equals(ORDER_STATUS.CANCEL.name())) {
						name= ORDER_STATUS.CANCEL.getCnName(oldOrder.getOrderStatus());
						result.setMsg(msg+"订单取消状态  "+name);
					}else{
						if("STAMP".equals(oldOrder.getOrderSubType())){
							LOG.info("券订单取消接口-------------开始");
							LOG.info("------------------7------------------");
							String url = Constant.getInstance().getPreSaleBaseUrl()+ "/customer/stamp/order/cancel";
							Map<String, Object> map=new HashMap<String, Object>();
							map.put("orderId", oldOrder.getOrderId());
							map.put("cancelCode", cancelCode);
							map.put("memo", cancleReasonText);
							map.put("operatorId", loginUserId);
							map.put("reason", orderRemark);
							LOG.info("券订单取消接口-------------请求参数:"+map.toString()+"url:"+url);
							RestClient.getClient().put(url, map);
							LOG.info("券订单取消接口-------------结束");
						}else if(oldOrder.getCategoryId() != null && oldOrder.getCategoryId().longValue()==188L){
							//超级会员取消订单
							result = cancelSuperMemberOrder(oldOrder.getOrderId(), cancelCode, cancleReasonText, loginUserId, orderRemark);
						}else{
							result = orderLocalService.cancelOrder(oldOrder.getOrderId(), cancelCode, cancleReasonText, loginUserId, orderRemark);
						}


					}
				}
				if (result.isFail()) {
					return new ResultMessage(ResultMessage.ERROR, result.getMsg()+" 请勿再次操作，页面即将刷新");
				}
			} catch (Exception e) {
				LOG.error("订单状态修改发生异常:", e);
				return new ResultMessage(ResultMessage.ERROR,"订单状态修改发生异常:"+e.getMessage());
			}
			return ResultMessage.UPDATE_SUCCESS_RESULT;
		}


		/** 
		 * @Title: cancelSuperMemberOrder 
		 * @Description: 超级会员取消订单方法
		 * @param orderId
		 * @param cancelCode
		 * @param cancleReason
		 * @param loginUserId
		 * @param orderRemark
		 * @return
		 * @return: ResultHandle
		 */
		private ResultHandle cancelSuperMemberOrder(Long orderId, String cancelCode, String cancleReason, String loginUserId, String orderRemark) {
			ResultHandle result = new ResultHandle();
			OrderCancelParamVo reqVo = new OrderCancelParamVo(orderId, cancelCode, cancleReason, loginUserId, orderRemark);
			com.lvmama.order.api.base.vo.RequestBody<OrderCancelParamVo> req = new com.lvmama.order.api.base.vo.RequestBody<OrderCancelParamVo>(reqVo);
			com.lvmama.order.api.base.vo.ResponseBody res = apiOrderCancelProcessService.cancelOrder(req);
			if(res != null && res.isFailure()) {
				result.setMsg(res.getMessage());
			}
			return result;
		}

		/**
		 * @param orderItem
		 * @param orderAttachment
		 * @return
		 */
		@RequestMapping(value = "/updateChildOrderStatus")
		@ResponseBody
		public Object updateChildOrderStatus( HttpServletRequest request,OrdOrderItem orderItem,OrderAttachment orderAttachment) {
			if (log.isDebugEnabled()) {
				log.debug("start method<updateChildOrderStatus>");
			}
			String operation=request.getParameter("operation");
			String cancelCode=request.getParameter("cancelCode");
			String cancleReasonText=request.getParameter("cancleReasonText");
			//String orderRemark=request.getParameter("orderRemark");
			String orderRemark=request.getParameter("orderRemark");
			String showStatusChange = request.getParameter("showStatusChange");
			String showStatusChangeProcessed = request.getParameter("showStatusChangeProcessed");

			OrdOrderItem oldOrderItem=ordOrderUpdateService.getOrderItem(orderItem.getOrderItemId());
			Long orderItemId=oldOrderItem.getOrderItemId();
			Long orderId=oldOrderItem.getOrderId();

			OrdOrder oldOrder = this.orderUpdateService.queryOrdOrderByOrderId(orderId);

			String loginUserId=this.getLoginUserId();
			String newStatus="";
			String name="";
			String msg="当前订单操作状态已被修改:";
			ResultHandle result=new ResultHandle();
			if ("updateOrderRemark".equals(operation)) {

				//orderStatusManageService.updateOrderMemo(orderItemId, orderRemark);
				OrdOrderItem orderItemObj=new OrdOrderItem();
				orderItemObj.setOrderItemId(orderItemId);
				orderItemObj.setOrderMemo(orderRemark);

				this.orderUpdateService.updateOrderItemByIdSelective(orderItemObj);

				lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
						orderId,
						orderItemId,
						loginUserId,
						"将编号为["+orderItemId+"]的订单子项，更新订单备注",
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"更新订单备注",
						orderRemark);


			}else if ("infoStatus".equals(operation)) {
				if (!oldOrderItem.getInfoStatus().equals(orderItem.getInfoStatus())) {
					name= INFO_STATUS.INFOPASS.getCnName(oldOrderItem.getInfoStatus());
					result.setMsg(msg+"信息审核状态 "+name);
				}else{
					newStatus=INFO_STATUS.INFOPASS.name();
					result = orderLocalService.executeUpdateChildInfoStatus(oldOrderItem, newStatus,loginUserId,orderRemark);

				/*if (result.isSuccess()) {
					String faxFlag=request.getParameter("faxFlag");
					String faxRemark=request.getParameter("messageContent");
					result=orderStatusManageService.saveOrderFaxRemark(orderId,faxFlag, faxRemark, loginUserId, orderRemark);
					if (result.isSuccess()){
						orderLocalService.sendOrderSendFaxMsg(order,"");
					}

				}*/

				}
			}else if ("resourceStatus".equals(operation)) {
				if (!oldOrderItem.getResourceStatus().equals(orderItem.getResourceStatus())) {
					if(!oldOrderItem.isApiFlightTicket()) {
						name= RESOURCE_STATUS.AMPLE.getCnName(oldOrderItem.getInfoStatus());
						result.setMsg(msg+"资源审核状态 "+name);
					} else {
						//资源审核先后顺序判断
						OrdOrder queryOrder=orderLocalService.queryOrdorderByOrderId(orderId);
						if(OrdOrderUtils.isLocalBuOrderNew(queryOrder)){
							boolean isResourceAmpleExcludeFlightHotel=true;
							//资源审核如果是机票或者是酒店，则判断其他资源是否通过
							if(BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()
									|| BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()
									|| BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()
									|| BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()){
								for (OrdOrderItem item : queryOrder.getOrderItemList()) {
									if(!item.hasResourceAmple()&&
											BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()!=item.getCategoryId().longValue()
											&& BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()!=item.getCategoryId().longValue()
											&& BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().longValue()!=item.getCategoryId().longValue()
											&& BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().longValue()!=item.getCategoryId().longValue()){
										isResourceAmpleExcludeFlightHotel=false;
										result.setMsg("请首先审核除酒店和机票的资源");
										break;
									}
									if(item.isApiFlightTicket()&&!item.hasResourceAmple()
											&& BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()
											&&!queryOrder.isPayMentType()&&!queryOrder.isHotelCerConfirmStatus()){
										for (OrdOrderItem element : queryOrder.getOrderItemList()) {
											if(BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==element.getCategoryId().longValue()&&!element.isRoomReservations()){
												if(!element.hasCertConfirmStatusConfirmed()){
													isResourceAmpleExcludeFlightHotel=false;
													result.setMsg("请首先审核酒店的凭证");
													break;
												}
											}
										}
									}
								}
								if(!isResourceAmpleExcludeFlightHotel){
									return new ResultMessage(ResultMessage.ERROR, result.getMsg()+" 请勿再次操作，页面即将刷新");
								}
							}
						}else if(BU_NAME.LOCAL_BU.getCode().equals(queryOrder.getBuCode())&&queryOrder.isIncludeFlightHotel()){
							boolean isResourceAmpleExcludeFlightHotel=true;
							//资源审核如果是机票或者是酒店，则判断其他资源是否通过
							if(BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()
									|| BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()){
								for (OrdOrderItem item : queryOrder.getOrderItemList()) {
									if(!item.hasResourceAmple()&&
											BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()!=item.getCategoryId().longValue()
											&& BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()!=item.getCategoryId().longValue()){
										isResourceAmpleExcludeFlightHotel=false;
										result.setMsg("请首先审核除酒店和机票的资源");
										break;
									}
									if(item.isApiFlightTicket()&&!item.hasResourceAmple()
											&& BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()
											&&!queryOrder.isPayMentType()&&!queryOrder.isHotelCerConfirmStatus()){
										for (OrdOrderItem element : queryOrder.getOrderItemList()) {
											if(BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==element.getCategoryId().longValue()&&!element.isRoomReservations()){
												if(!element.hasCertConfirmStatusConfirmed()){
													isResourceAmpleExcludeFlightHotel=false;
													result.setMsg("请首先审核酒店的凭证");
													break;
												}
											}
										}
									}
								}
								if(!isResourceAmpleExcludeFlightHotel){
									return new ResultMessage(ResultMessage.ERROR, result.getMsg()+" 请勿再次操作，页面即将刷新");
								}
							}

						}
						//设置资源保留时长
						//对接机票，如果页面输入的资源保留时长较早，则用该时间对原资源保留时间进行覆盖
						String resourceRetentionTimeStr = request.getParameter("resourceRetentionTime");
						LOG.info("页面输入资源保留时间：" + resourceRetentionTimeStr + ", orderItemId:" + oldOrderItem.getOrderItemId());
						if(StringUtils.isNotBlank(resourceRetentionTimeStr)) {
							Date resourceRetentionTime = DateUtil.toDate(resourceRetentionTimeStr, DateUtil.HHMMSS_DATE_FORMAT);
							String currRetentionTimeStr = oldOrderItem.getContentStringByKey(ORDER_COMMON_TYPE.res_retention_time.name());
							LOG.info("原资源保留时间：" + currRetentionTimeStr);
							Date currRetentionTime = null;
							if(StringUtils.isNotBlank(currRetentionTimeStr)){
								currRetentionTime = DateUtil.toDate(currRetentionTimeStr, DateUtil.HHMMSS_DATE_FORMAT);
							}

							if(resourceRetentionTime != null && (currRetentionTime == null || resourceRetentionTime.before(currRetentionTime))) {

								oldOrderItem.putContent(ORDER_COMMON_TYPE.res_retention_time.name(),
										DateUtil.formatDate(resourceRetentionTime,DateUtil.HHMMSS_DATE_FORMAT));
								orderLocalService.updateOrderItem(oldOrderItem);

								OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(oldOrderItem.getOrderId());
								//设置支付等待时间
								LOG.info("原支付等待时间：" + order.getWaitPaymentTime());
								if(order.getWaitPaymentTime() == null || resourceRetentionTime.before(order.getWaitPaymentTime())) {
									order.setWaitPaymentTime(resourceRetentionTime);
								}
								orderLocalService.updateOrdOrder(order);
							}
						}
					}
				}else{
					newStatus=RESOURCE_STATUS.AMPLE.name();
					String resourceRetentionTime=request.getParameter("resourceRetentionTime");
					OrdOrder order=orderLocalService.queryOrdorderByOrderId(orderId);
					if(OrdOrderUtils.isLocalBuOrderNew(order)){
						boolean isResourceAmpleExcludeFlightHotel=true;
						//资源审核如果是机票或者是酒店，则判断其他资源是否通过
						if(BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()
								|| BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()
								|| BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()
								|| BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()){
							for (OrdOrderItem item : order.getOrderItemList()) {
								if(!item.hasResourceAmple()&&
										BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()!=item.getCategoryId().longValue()
										&& BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()!=item.getCategoryId().longValue()
										&& BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().longValue()!=item.getCategoryId().longValue()
										&& BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().longValue()!=item.getCategoryId().longValue()){
									isResourceAmpleExcludeFlightHotel=false;
									result.setMsg("请首先审核除酒店和机票的资源");
									break;
								}
								if(item.isApiFlightTicket()&&!item.hasResourceAmple()
										&& BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()
										&&!order.isPayMentType()&&!order.isHotelCerConfirmStatus()){
									for (OrdOrderItem element : order.getOrderItemList()) {
										if(BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==element.getCategoryId().longValue()&&!element.isRoomReservations()){
											if(!element.hasCertConfirmStatusConfirmed()){
												isResourceAmpleExcludeFlightHotel=false;
												result.setMsg("请首先审核酒店的凭证");
												break;
											}
										}
									}
								}
							}
							if(!isResourceAmpleExcludeFlightHotel){
								return new ResultMessage(ResultMessage.ERROR, result.getMsg()+" 请勿再次操作，页面即将刷新");
							}
						}
					}else if(BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())&&order.isIncludeFlightHotel()){
						boolean isResourceAmpleExcludeFlightHotel=true;
						//资源审核如果是机票或者是酒店，则判断其他资源是否通过
						if(BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()
								|| BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()){
							for (OrdOrderItem item : order.getOrderItemList()) {
								if(!item.hasResourceAmple()&&
										BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()!=item.getCategoryId().longValue()
										&& BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()!=item.getCategoryId().longValue()){
									isResourceAmpleExcludeFlightHotel=false;
									result.setMsg("请首先审核除酒店和机票的资源");
									break;
								}
								if(item.isApiFlightTicket()&&!item.hasResourceAmple()
										&& BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()
										&&!order.isPayMentType()&&!order.isHotelCerConfirmStatus()){
									for (OrdOrderItem element : order.getOrderItemList()) {
										if(BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==element.getCategoryId().longValue()&&!element.isRoomReservations()){
											if(!element.hasCertConfirmStatusConfirmed()){
												isResourceAmpleExcludeFlightHotel=false;
												result.setMsg("请首先审核酒店的凭证");
												break;
											}
										}
									}
								}
							}
							if(!isResourceAmpleExcludeFlightHotel){
								return new ResultMessage(ResultMessage.ERROR, result.getMsg()+" 请勿再次操作，页面即将刷新");
							}
						}

					}
					result =orderLocalService.executeUpdateChildResourceStatus(oldOrderItem, newStatus, resourceRetentionTime,loginUserId, orderRemark,false);

				}
			}else if ("certificateStatus".equals(operation)) {//凭证确认
				//根据该orderId和凭证确认类型查询附件,判读是否已经凭证确认活动过
				if (isDoneCertificate(oldOrderItem.getCertConfirmStatus())) {
					result.setMsg(msg+" 凭证确认已经完成,不可再次凭证确认  ");
				}else{
					OrdOrder order=orderLocalService.queryOrdorderByOrderId(orderId);
					if(BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())&&order.isIncludeFlightHotel()){
						boolean isResourceAmpleExcludeFlightHotel=true;
						//资源审核如果是机票或者是酒店，则判断其他资源是否通过
						if(BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()
								|| BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==oldOrderItem.getCategoryId().longValue()){
							for (OrdOrderItem item : order.getOrderItemList()) {
								if(!item.hasResourceAmple()&&
										BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()!=item.getCategoryId().longValue()
										&& BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()!=item.getCategoryId().longValue()){
									isResourceAmpleExcludeFlightHotel=false;
									result.setMsg("请首先审核除酒店和机票的资源");
									break;
								}
							}
							if(!isResourceAmpleExcludeFlightHotel){
								return new ResultMessage(ResultMessage.ERROR, result.getMsg()+" 请勿再次操作，页面即将刷新");
							}
						}
					}
					orderAttachment.setOrderId(orderId);
					orderAttachment.setAttachmentType(ATTACHMENT_TYPE.CERTIFICATE.name());
					orderAttachment.setMemo("凭证确认附件");
					orderAttachment.setCreateTime(Calendar.getInstance().getTime());
					orderAttachment.setFileId(orderAttachment.getFileId());

					result = orderLocalService.updateChildCertificateStatus(oldOrderItem, orderAttachment, loginUserId, orderRemark);
				}
			}else if ("cancelStatusConfim".equals(operation)) {//取消订单已确认  或进行确认取消操作
				if ("Y".equals(showStatusChange) && "UNPROCESSED".equals(showStatusChangeProcessed)){
					try{
						String auditId = request.getParameter("auditId");
						if(auditId == null){
							LOG.error("audit is null,Can not CancelConfirm");
						}
						LOG.info("OrderItemCancelConfirm auditId" + auditId);
						String operateName = getLoginUserId();
						result = ordItemConfirmStatusService.cancelConfirm(Long.parseLong(auditId), operateName);
					}catch (Exception e){
						LOG.error(e.getMessage());
						e.printStackTrace();
					}
				}else{
					result =orderLocalService.updateChildCancelConfim(oldOrderItem, loginUserId, orderRemark);
				}
//			orderLocalService.updateCancelConfim(oldOrder, loginUserId, orderRemark);
			}else if ("cancelStatus".equals(operation)) {
				if (oldOrder.getOrderStatus().equals(ORDER_STATUS.CANCEL.name())) {
					name= ORDER_STATUS.CANCEL.getCnName(oldOrder.getOrderStatus());
					result.setMsg(msg+"订单取消状态  "+name);
				}else{
					if("STAMP".equals(oldOrder.getOrderSubType())){
						LOG.info("券订单取消接口-------------开始");
						LOG.info("------------------8------------------");
						String url = Constant.getInstance().getPreSaleBaseUrl()+ "/customer/stamp/order/cancel";
						Map<String, Object> map=new HashMap<String, Object>();
						map.put("orderId", oldOrder.getOrderId());
						map.put("cancelCode", cancelCode);
						map.put("memo", cancleReasonText);
						map.put("operatorId", loginUserId);
						map.put("reason", orderRemark);
						LOG.info("券订单取消接口-------------请求参数:"+map.toString()+"url:"+url);
						RestClient.getClient().put(url, map);
						LOG.info("券订单取消接口-------------结束");
					}else{
						result = orderLocalService.cancelOrder(oldOrder.getOrderId(), cancelCode, cancleReasonText, loginUserId, orderRemark);
					}
				}
			}
			if (result.isFail()) {
				return new ResultMessage(ResultMessage.ERROR, result.getMsg()+" 请勿再次操作，页面即将刷新");
			}

			return ResultMessage.UPDATE_SUCCESS_RESULT;
		}

		@RequestMapping(value= "/showSaledApplied")
		public String showSaledApplied(HttpServletRequest request, Model model) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<showSaledApplied>");
			}
			String orderId = request.getParameter("orderId");
			String actualAmount = request.getParameter("actualAmount");
			String operation = request.getParameter("operation");
			String cancelCode = request.getParameter("cancelCode");
			String cancleReasonText = request.getParameter("cancleReasonText");
			String orderRemark = request.getParameter("orderRemark");
			/**
			 * UNVERIFIED("未审核"),
			 * REFUND_APPLY("退款单申请"),
			 * APPLY_CONFIRM("已确认"),
			 * REFUND_VERIFIED("退款单审核通过"),
			 * REJECTED(" 不通过（拒绝、驳回）"),
			 * VERIFIED("退款审核通过(等待退款)"),
			 * REFUNDED("已退款"),
			 * REFUNDED_PART("部分退款"),
			 * CANCEL("退款单取消"),
			 * PROCESSING("退款处理中"),
			 * FAIL("退款失败"),
			 * VERIFIED和REFUNDED  两个状态用于查询该订单下已退款有多少
			 */
			Long refundedAmount = 0L;
			try {
				List<OrdRefundment> verifiedRefuncmentList = orderRefundmentService.findOrderRefundmentByOrderIdStatus(NumberUtils.toLong(orderId), Constant.REFUNDMENT_STATUS.VERIFIED.name());
				LOG.info("###orderId###" + orderId + "REFUND_VERIFIED退款单审核通过===" + com.alibaba.fastjson.JSONObject.toJSONString(verifiedRefuncmentList));
				List<OrdRefundment> refundedRefuncmentList = orderRefundmentService.findOrderRefundmentByOrderIdStatus(NumberUtils.toLong(orderId), Constant.REFUNDMENT_STATUS.REFUNDED.name());
				LOG.info("###orderId###" + orderId + "REFUNDED已退款===" + com.alibaba.fastjson.JSONObject.toJSONString(refundedRefuncmentList));
				for (OrdRefundment refundment : verifiedRefuncmentList) {
					refundedAmount += refundment.getAmount();
				}
				for (OrdRefundment refundment : refundedRefuncmentList) {
					refundedAmount += refundment.getAmount();
				}
			} catch (Exception e) {
				LOG.info("###orderId###" + orderId + "invoking findOrderRefundmentByOrderIdStatus appear exception", e);
			}
			LOG.info("###orderId###" + orderId + "actualAmount=" + actualAmount + "refundedAmount=" + PriceUtil.convertToYuan(refundedAmount));
			model.addAttribute("orderId", orderId);
			model.addAttribute("actualAmount", NumberUtils.toFloat(actualAmount));//已收款
			model.addAttribute("refundedAmount", PriceUtil.convertToYuan(refundedAmount));//已退款
			model.addAttribute("operation", operation);
			model.addAttribute("cancelCode", cancelCode);
			model.addAttribute("cancleReasonText", cancleReasonText);
			model.addAttribute("orderRemark", orderRemark);
			return "/order/orderStatusManage/allCategory/showSaledApplied";
		}

		/**
		 * 取消订单和生成售后单
		 *
		 * @param model
		 * @param request
		 * @return
		 */
		@RequestMapping(value= "/refundAmount")
		@ResponseBody
		public Object refundAmount(Model model, HttpServletRequest request) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<refundAmount>");
			}
			/**
			 *  当手续费=0，退款金额+已退款=已收款， 默认勾选取消订单和生成售后单
			 *  退款金额+已退款＜已收款，默认只勾选创建售后单
			 *  当手续费≠0，退款金额+已退款+手续费=已收款，默认勾选取消订单和生成售后单
			 *  退款金额+已退款+手续费＜已收款，默认只勾选创建售后单
			 */
			String serviceAmount = request.getParameter("serviceAmount");//手续费
			String refundAmount = request.getParameter("refundAmount");//退款金额
			String orderIdStr = request.getParameter("orderId");//
			String cancelCode = request.getParameter("cancelCode");
			String cancleReasonText = request.getParameter("cancleReasonText");
			String orderRemark = request.getParameter("orderRemark");
			String remark = request.getParameter("remark");
			String cancelOrder = request.getParameter("cancelOrder");
			String createOrder = request.getParameter("createOrder");
			String loginUserId=this.getLoginUserId();
			try {
				orderRemark = java.net.URLDecoder.decode(orderRemark,"UTF-8");
				remark = java.net.URLDecoder.decode(remark,"UTF-8");
				cancleReasonText = java.net.URLDecoder.decode(cancleReasonText,"UTF-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			Long orderId = NumberUtils.toLong(orderIdStr);
			OrdOrder oldOrder = complexQueryService.queryOrderByOrderId(orderId);
			String msg="当前订单操作状态已被修改:";
			String name="";
			ResultHandle result=new ResultHandle();
			if (oldOrder.getOrderStatus().equals(ORDER_STATUS.CANCEL.name())) {
				name= ORDER_STATUS.CANCEL.getCnName(oldOrder.getOrderStatus());
				result.setMsg(msg+"订单取消状态  "+name);
			}else{
				if(ObjectUtils.equals("1", cancelOrder) && ObjectUtils.equals("0", createOrder)) {//执行取消订单
					if("STAMP".equals(oldOrder.getOrderSubType())){
						LOG.info("券订单取消接口-------------开始");
						LOG.info("------------------7------------------");
						String url = Constant.getInstance().getPreSaleBaseUrl()+ "/customer/stamp/order/cancel";
						Map<String, Object> map=new HashMap<String, Object>();
						map.put("orderId", oldOrder.getOrderId());
						map.put("cancelCode", cancelCode);
						map.put("memo", cancleReasonText);
						map.put("operatorId", loginUserId);
						map.put("reason", orderRemark);
						LOG.info("券订单取消接口-------------请求参数:"+map.toString()+"url:"+url);
						RestClient.getClient().put(url, map);
						LOG.info("券订单取消接口-------------结束");
					}else{
						result = orderLocalService.cancelOrder(oldOrder.getOrderId(), cancelCode, cancleReasonText, loginUserId, orderRemark);
					}
				}
				if(ObjectUtils.equals("1", cancelOrder) && ObjectUtils.equals("1", createOrder)) {//执行创建售后单并取消订单
					StringBuffer memo = new StringBuffer();
					memo.append(loginUserId);
					memo.append(" 后台申请退款，所以转售后，退款金额：");
					memo.append(refundAmount);
					memo.append("，手续费：");
					memo.append(serviceAmount);
					memo.append("，单位（元），备注：");
					memo.append(remark);
					memo.append("。");
					result = orderLocalService.createSaleVstAndCancelOrder(oldOrder, cancelCode, cancleReasonText, loginUserId, orderRemark, memo.toString());
				}
				if(ObjectUtils.equals("0", cancelOrder) && ObjectUtils.equals("1", createOrder)) {//创建售后服务单
					StringBuffer memo = new StringBuffer();
					memo.append(loginUserId);
					memo.append(" 后台申请退款，所以转售后，退款金额：");
					memo.append(refundAmount);
					memo.append("，手续费：");
					memo.append(serviceAmount);
					memo.append("，单位（元），备注：");
					memo.append(remark);
					memo.append("。");
					String saleServiceType = "FX";
					String saleServiceSource = "FX_BACK";//FX_BACK(\"分销后台售后单\")
					try {
						Long saleId = orderServiceProxy.createSaleVst(orderId, saleServiceType, saleServiceSource, loginUserId, memo.toString());
						LOG.info("###orderId###" + orderId + "invoking createSaleVst successed and return saleId==" + saleId);
					} catch (Exception e) {
						LOG.info("###OrderId###" + orderId + "invoking createSaleVst failed", e);
						result.setMsg("创建售后单失败");
						result.isFail();
					}
				}
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

		@RequestMapping(value = "/showUpdateRetentionTime")
		public String showUpdateRetentionTime(Model model, HttpServletRequest request,Long orderItemId,Long orderId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<showUpdateRetentionTime>");
			}

			OrdOrderItem orderItem=ordOrderUpdateService.getOrderItem(orderItemId);

			Map<String,Object> contentMap = orderItem.getContentMap();
			String resourceRetentionTime =  (String) contentMap.get(ORDER_COMMON_TYPE.res_retention_time.name());

			model.addAttribute("resourceRetentionTime", resourceRetentionTime);
			model.addAttribute("kssj",DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm")+":00");
			model.addAttribute("jssj",DateUtil.formatDate(orderItem.getVisitTime(), "yyyy-MM-dd")+" 23:59:00");

			return "/order/orderStatusManage/allCategory/showUpdateRetentionTime";
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

			return "/order/orderStatusManage/allCategory/sendOrderFax";
		}
		@RequestMapping(value = "/showManualSendOrderFax")
		public String showManualSendOrderFax(Model model, HttpServletRequest request,Long orderId,Long orderItemId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<showManualSendOrderFax>");
			}

			OrdOrder order =this.orderUpdateService.queryOrdOrderByOrderId(orderId);
			OrdOrderItem orderItem=this.orderUpdateService.getOrderItem(orderItemId);

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
			if ("Y".equals(faxFlag) ) {
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
			}else if ("Y".equals(mailFlag) ) {
				ResultHandleT<SuppGoods> resultHandleSuppGoods = suppGoodsClientService.findSuppGoodsById(orderItem.getSuppGoodsId(), Boolean.TRUE, Boolean.TRUE);
				if (resultHandleSuppGoods.isSuccess() && resultHandleSuppGoods.getReturnContent() != null) {
					SuppGoods suppGoods = resultHandleSuppGoods.getReturnContent();
					Long mailRuleId= suppGoods.getMailRuleId();
					ResultHandleT<SuppMailRule> resultHandleSuppMailRule = suppMailClientService.findSuppMailRuleById(mailRuleId);
					if (resultHandleSuppMailRule.isSuccess()) {
						model.addAttribute("suppMailRule", resultHandleSuppMailRule.getReturnContent());
					} else {
						LOG.info("method showManualSendOrderFax:findSuppMailRuleById(ID=" + mailRuleId + ") is fail,msg=" + resultHandleSuppMailRule.getMsg());
					}
				}
			}
			model.addAttribute("faxFlag", faxFlag);

			model.addAttribute("mailFlag", mailFlag);

			model.addAttribute("cancelCertConfirmStatus", request.getParameter("cancelCertConfirmStatus"));

			return "/order/orderStatusManage/allCategory/manualSendOrderFax";
		}

		@RequestMapping(value = "/showOrderCertifDialog")
		public String showOrderCertifDialog(Model model, HttpServletRequest request, String cancelCertConfirmStatus){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<showOrderCertifDialog>");
			}
			model.addAttribute("cancelCertConfirmStatus", cancelCertConfirmStatus);
			return "/order/orderStatusManage/allCategory/orderCertifDialog";
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
		private boolean isDestBu(OrdOrder order, OrdOrderItem orderItem, String itemRealBuType) {
			//TODO 打包类型要在订单快照上线后加上，目前通过反查实现
			Long productId = order.getProductId();
			String packageType = null;
			String productType = null;
			try {
				ResultHandleT<ProdProduct> handleT = prodProductClientService.findProdProductSimpleById(productId);
				if (handleT != null && handleT.getReturnContent() != null) {
					ProdProduct prodProduct = handleT.getReturnContent();
					//打包类型
					packageType = prodProduct.getPackageType();

				}
				ResultHandleT<ProdProduct> handleT1 = prodProductClientService.findProdProductSimpleById(orderItem.getProductId());
				if (handleT1 != null && handleT1.getReturnContent() != null) {
					ProdProduct prodProduct = handleT1.getReturnContent();
					//产品类型：国内
					productType = prodProduct.getProductType();

				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("isDestBu packageType error."+e.getMessage());
			}
//		if (!ProdProduct.PRODUCTTYPE.INNERLINE.name().equalsIgnoreCase(productType) && !ProdProduct.PRODUCTTYPE.INNERLONGLINE.name().equalsIgnoreCase(productType) && !ProdProduct.PRODUCTTYPE.INNERSHORTLINE.name().equalsIgnoreCase(productType)) {
//			return false;
//		}
			if (!CommEnumSet.BU_NAME.DESTINATION_BU.name().equals(itemRealBuType) && !CommEnumSet.BU_NAME.LOCAL_BU.name().equals(itemRealBuType)) {
				return false;
			}
			if (BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId()) || BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())) {
				return true;
			}
			if (ProdProduct.PACKAGETYPE.SUPPLIER.name().equalsIgnoreCase(packageType)) {
				if (BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(order.getSubCategoryId())
						|| BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId())
						|| BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(order.getSubCategoryId())) {
					return true;
				}
			} else {	//子单品类判断
				if (BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())
						|| BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(orderItem.getCategoryId())
						|| BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(orderItem.getCategoryId())) {
					return true;
				}
			}
			return false;
		}

		@RequestMapping(value = "/manualSendOrderFax")
		@ResponseBody
		public Object manualSendOrderFax( HttpServletRequest request,OrdPerson ordPerson){

			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<manualSendOrderFax>");
			}
			String assignor=getLoginUserId();
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
			OrdOrderItem orderItem=this.orderUpdateService.getOrderItem(orderItemId);
			String faxFlag=(String)orderItem.getContentMap().get(ORDER_COMMON_TYPE.fax_flag.name());

			ResultHandle result=this.orderStatusManageService.manualSendOrderItemFax(orderItem, toFax, faxRemark, assignor, certifTypeMemo);
			if (result.isSuccess()) {
				if ("Y".equals(faxFlag)) {
					addition=certifType+"_"+toFax;
				}else if("N".equals(faxFlag)){
					addition=certifType;
				}
				if (StringUtil.isEmptyString(memo)) {
					orderLocalService.sendOrderItemSendFaxMsg(orderItem, addition);
				} else {
					orderLocalService.sendOrderSendTwiceFaxMsg(orderItem.getOrderItemId(), addition);
				}
			}
			try {
				//变更单生成
				if ("N".equals(faxFlag)) {
					if (BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())
							|| BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())) {
						OrdOrder order = complexQueryService.queryOrderByOrderId(orderItem.getOrderId());
						//分销商渠道ID
						boolean isNotDistribution = true;
						Long[] DISTRIBUTION_CHANNEL_LIST = {10000L, 107L, 108L, 110L, 10001L, 10002L};
						if (Constant.DIST_BRANCH_SELL == order.getDistributorId()
								&& !(org.apache.commons.lang.ArrayUtils.contains(DISTRIBUTION_CHANNEL_LIST, order.getDistributionChannel().longValue())
								|| "DISTRIBUTOR_TAOBAO".equalsIgnoreCase(order.getDistributorCode()))) {
							isNotDistribution = false;
						}
						LOG.info("-------orderId:"+order.getOrderId()+",isNotDistribution="+isNotDistribution);
						if (order.hasPayed() && "CONFIRMED".equalsIgnoreCase(orderItem.getCertConfirmStatus()) && isNotDistribution) {
							if (BU_NAME.LOCAL_BU.getCode().equals(orderItem.getBuCode()) ||
									BU_NAME.DESTINATION_BU.getCode().equals(orderItem.getBuCode())) {
								Map<String, Object> param = new HashMap<String, Object>();
								param.put("objectId", orderItem.getOrderItemId());
								param.put("objectType", AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
								param.put("auditType", OrderEnum.AUDIT_TYPE.BOOKING_AUDIT.name());
								param.put("auditSubtype", OrderEnum.AUDIT_SUB_TYPE.CHANG_ORDER.name());
								String[] auditStatusArray = new String[]{OrderEnum.AUDIT_STATUS.POOL.getCode(), OrderEnum.AUDIT_STATUS.UNPROCESSED.getCode()};
								param.put("auditStatusArray", auditStatusArray);
								List<ComAudit> comAuditList = orderAuditService.queryAuditListByParam(param);
								if (null == comAuditList || comAuditList.size() < 1) {
									LOG.info("-------生成变更单---------orderId:" + order.getOrderId());
									ComMessage comMessage = new ComMessage();
									comMessage.setMessageContent("订单号" + order.getOrderId() + "，子订单"+orderItem.getOrderItemId()+"生成变更单.");
									comMessage.setMessageStatus(OrderEnum.MESSAGE_STATUS.UNPROCESSED.name());
									comMessageService.saveReservationChildOrder(comMessage, null, OrderEnum.AUDIT_SUB_TYPE.CHANG_ORDER.name(), orderItem.getOrderId(), orderItem.getOrderItemId(), assignor, comMessage.getMessageContent());
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
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

			ResultHandle result=this.orderStatusManageService.manualSendOrderFax(order, toFax, faxRemark, assignor, EBK_CERTIFICATE_CONFIRM_CHANNEL.CHANGE_FAX.getCnName());
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

			OrdPerson ordPerson=orderUpdateService.findOrderPersonById(Long.valueOf(ordPersonId));

			model.addAttribute("ordPerson", ordPerson);

			return "/order/orderStatusManage/allCategory/updatePerson";
		}






		@RequestMapping(value = "/showUpdateOrdAddress")
		public String showUpdateOrdAddress(Model model,OrdPerson ordPerson,OrdAddress ordAddress, HttpServletRequest request,Long orderId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<showUpdateOrdAddress>");
			}
			OrdOrder order=this.orderLocalService.findExpressAddress(orderId);
			model.addAttribute("ordAddress", order.getOrdAddress());
			model.addAttribute("addressPerson", order.getAddressPerson());
			model.addAttribute("order", order);
		/*
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectType", ORDER_PERSON_OBJECT_TYPE.ORDER.name());
		params.put("objectId", orderId);
		params.put("personType", ORDER_PERSON_TYPE.EMERGENCY.name());
		List<OrdPerson>  emergencyPersonList=ordPersonService.findOrdPersonList(params);
		*/


			return "/order/orderStatusManage/allCategory/showUpdateOrdAddress";
		}



		@RequestMapping(value = "/updateOrdAddress")
		@ResponseBody
		public Object updateOrdAddress(Model model,OrdPerson ordPerson,OrdAddress ordAddress, HttpServletRequest request,Long orderId){

		/*String ordPersonId=request.getParameter("ordPersonId");

		OrdPerson ordPersonObj=new OrdPerson()	;
		ordPerson.setOrdPersonId(new Long(ordPersonId));*/

			String loginUserId=this.getLoginUserId();
			this.ordAddressService.updateOrdAddressAndPerson(ordPerson, ordAddress, orderId, loginUserId);

			String addition= ORDER_PERSON_TYPE.ADDRESS.name();
			orderMessageProducer.sendMsg(MessageFactory.newOrderModifyPersonMessage(orderId, addition));

			return ResultMessage.UPDATE_SUCCESS_RESULT;

		}


	@RequestMapping(value = "/showInsertOrdExpress")
	public String showInsertOrdExpress(Model model, HttpServletRequest request, Long orderId){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showInsertOrdExpress>");
		}

		model.addAttribute("orderId", orderId);
		return "/order/orderStatusManage/allCategory/showInsertOrdExpress";
	}


	@RequestMapping(value = "/insertOrdExpress")
	@ResponseBody
	public Object insertOrdExpress(Model model, HttpServletRequest request,OrdExpressVo newOrdExpressVo){

		newOrdExpressVo.setWriterId(this.getLoginUserId());
		com.lvmama.order.api.base.vo.RequestBody<OrdExpressVo> requestBody = new com.lvmama.order.api.base.vo.RequestBody<>();
		requestBody.setT(newOrdExpressVo);
		com.lvmama.order.api.base.vo.ResponseBody<Integer> responseBody =  this.apiOrderExpressProcessService.executeAddOrdExpress(requestBody);
		if(null!=responseBody && responseBody.isSuccess()){
			return ResultMessage.ADD_SUCCESS_RESULT;
		}
		return ResultMessage.ADD_FAIL_RESULT;
	}


	@RequestMapping(value = "/showUpdateOrdExpress")
	public String showUpdateOrdExpress(Model model,HttpServletRequest request, Long ordOrderId, Long ordExpressId){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showUpdateOrdExpress>");
		}

		com.lvmama.order.api.base.vo.RequestBody<Long> requestBody = new com.lvmama.order.api.base.vo.RequestBody<>();
		requestBody.setT(ordExpressId);
		com.lvmama.order.api.base.vo.ResponseBody<OrdExpressVo> responseBody = this.apiOrderExpressProcessService.selectOrdExpressByOrdExpressId(requestBody);

		model.addAttribute("ordExpress", responseBody.getT());
		return "/order/orderStatusManage/allCategory/showUpdateOrdExpress";
	}


	@RequestMapping(value = "/updateOrdExpress")
	@ResponseBody
	public Object updateOrdExpress(Model model,HttpServletRequest request,OrdExpressVo ordExpressVo){

		ordExpressVo.setWriterId(this.getLoginUserId());
		com.lvmama.order.api.base.vo.RequestBody<OrdExpressVo> requestBody = new com.lvmama.order.api.base.vo.RequestBody<>();
		requestBody.setT(ordExpressVo);
		com.lvmama.order.api.base.vo.ResponseBody<Integer> responseBody =  this.apiOrderExpressProcessService.executeUpdateOrdExpressByOrdExpressId(requestBody);
		if(null!=responseBody && responseBody.isSuccess()){
			return ResultMessage.UPDATE_SUCCESS_RESULT;
		}
		return ResultMessage.UPDATE_FAIL_RESULT;
	}

	@RequestMapping(value = "/sendOrdExpressSMS")
	@ResponseBody
	public Object sendOrdExpressSMS(Model model,HttpServletRequest request, OrdExpressVo ordExpressVo){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<sendOrdExpressMessage>");
		}

		ordExpressVo.setWriterId(this.getLoginUserId());
		com.lvmama.order.api.base.vo.RequestBody<OrdExpressVo> requestBody = new com.lvmama.order.api.base.vo.RequestBody<>();
		requestBody.setT(ordExpressVo);
		com.lvmama.order.api.base.vo.ResponseBody<Integer> responseBody =  this.apiOrderExpressProcessService.executeSendOrdExpressSMS(requestBody);
		if(null!=responseBody && responseBody.isSuccess()){
			return ResultMessage.SMS_SEND_SUCCESS_RESULT;
		}
		return ResultMessage.SMS_SEND_FAIL_RESULT;
	}



		@RequestMapping(value = "/showUpdateEmergencyPerson")
		public String showUpdateEmergencyPerson(Model model, HttpServletRequest request,Long orderId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<showUpdateEmergencyPerson>");
			}
			if(null !=orderId){
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("objectType", ORDER_PERSON_OBJECT_TYPE.ORDER.name());
				params.put("objectId", orderId);
				params.put("personType", ORDER_PERSON_TYPE.EMERGENCY.name());
				List<OrdPerson>  emergencyPersonList=ordPersonService.findOrdPersonList(params);


				model.addAttribute("emergencyPerson", emergencyPersonList.get(0));

				return "/order/orderStatusManage/allCategory/updateEmergencyPerson";
			}
			return "";
		}


		@RequestMapping(value = "/updatePerson")
		@ResponseBody
		public Object updatePerson( HttpServletRequest request,OrdPerson ordPerson,Long orderId){

		/*String ordPersonId=request.getParameter("ordPersonId");

		OrdPerson ordPersonObj=new OrdPerson()	;
		ordPerson.setOrdPersonId(new Long(ordPersonId));*/

			OrdPerson oldPerson=ordPersonService.findOrdPersonById(ordPerson.getOrdPersonId());

			ordPerson.setPersonType(oldPerson.getPersonType());
			ordPerson.setObjectType(oldPerson.getObjectType());
			ordPerson.setObjectId(oldPerson.getObjectId());

			orderUpdateService.updateOrderPerson(ordPerson);



			//添加操作日志
			String str="";
			StringBuffer logStr = new StringBuffer("");
			logStr.append(ComLogUtil.getLogTxt("姓名",ordPerson.getFullName(),oldPerson.getFullName()));
			logStr.append(ComLogUtil.getLogTxt("手机", ordPerson.getMobile(), oldPerson.getMobile()));
			if (ORDER_PERSON_TYPE.EMERGENCY.name().equals(oldPerson.getPersonType())) {
				str="紧急";

			}else{
				logStr.append(ComLogUtil.getLogTxt("email",ordPerson.getEmail(),oldPerson.getEmail()));
			}

			String loginUserId=this.getLoginUserId();

			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					orderId,
					orderId,
					loginUserId,
					"将编号为[" + orderId + "]的订单，更新" + str + "联系人",
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName() + "更新" + str + "联系人",
					logStr.toString());


			String addition=oldPerson.getPersonType();
			orderMessageProducer.sendMsg(MessageFactory.newOrderModifyPersonMessage(orderId, addition));

			return ResultMessage.UPDATE_SUCCESS_RESULT;

		}


		@RequestMapping(value = "/showAddMessage")
		public String showAddMessage(Model model, HttpServletRequest request){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<showAddMessage>");
			}
			String orderId=request.getParameter("orderId");
			OrdOrder order = complexQueryService.queryOrderByOrderId(Long.valueOf(orderId));
			List<Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_SUB_TYPE> list1 = new ArrayList<Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_SUB_TYPE>();
			list1.add(Confirm_Booking_Enum.CONFIRM_BOOKING_AUDIT_SUB_TYPE.CONFIRM_APPROVAL);
			model.addAttribute("auditSubTypeList2", list1);
			model.addAttribute("auditSubTypeList1", AUDIT_SUB_TYPE.values());
			if (OrdOrderUtils.isDestBuFrontOrderNew(order)) {
				model.addAttribute("isNew", true);
			} else {
				model.addAttribute("isNew", false);
			}
			model.addAttribute("auditTypeList", AUDIT_TYPE.values());

			String messageObjectValue="";
			String orderItemId="";
			String orderType=request.getParameter("orderType");
			if ("parent".equals(orderType)) {

				messageObjectValue=orderId+"-ORDER";
			}else{
				orderItemId=request.getParameter("orderItemId");
				messageObjectValue=orderItemId+"-ORDER_ITEM";
			}


			Map<String, String> messageObjectMap = new LinkedHashMap<String, String>();
			Map<String, Long> orderCategoryMap = new LinkedHashMap<String, Long>();
			if (!OrdOrderUtils.isDestBuFrontOrderNew(order)) {
				messageObjectMap.put(orderId + "-ORDER", "主订单-" + orderId);
			}
			List<OrdOrderItem> orderItemsList = ordOrderUpdateService.queryOrderItemByOrderId(NumberUtils.toLong(orderId));
			for (OrdOrderItem ordOrderItem : orderItemsList) {

				if(OrdOrderUtils.isLocalBuOrderNew(order)){
					model.addAttribute("isNew", true);
				}
				messageObjectMap.put(ordOrderItem.getOrderItemId()+"-ORDER_ITEM", ordOrderItem.getProductName().trim()+"-"+ordOrderItem.getSuppGoodsName()+"-"+ordOrderItem.getOrderItemId());
				orderCategoryMap.put(ordOrderItem.getOrderItemId()+"-ORDER_ITEM", ordOrderItem.getCategoryId());

			}

			model.addAttribute("messageObjectMap", messageObjectMap);
			model.addAttribute("orderCategoryMap", orderCategoryMap);

			model.addAttribute("messageObjectValue", messageObjectValue);

			return "/order/orderStatusManage/allCategory/addMessage";
		}

		@RequestMapping(value = "/addMessage")
		@ResponseBody
		public Object addMessage( HttpServletRequest request,ComMessage comMessage){

			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<addMessage>");
			}
			Long orderId=NumberUtils.toLong(request.getParameter("orderId"));


			String messageObject=request.getParameter("messageObject");
			String objectIdStr=messageObject.split("-")[0];
			String orderType=messageObject.split("-")[1];

			Long objectId=NumberUtils.toLong(objectIdStr);

			String auditType=request.getParameter("auditType");
			String auditSubType=request.getParameter("auditSubType");
			String orderRemark=request.getParameter("orderRemark");

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
				log.info("addMessage objectIdStr="+objectIdStr+", objectType="+orderType);
				if ("ORDER".equals(orderType)) {//发送给主订单
					comMessageService.saveReservation(comMessage, auditType,auditSubType,
							orderId, assignor,orderRemark);
				}else{//发送给子订单
					comMessageService.saveReservationChildOrder(comMessage, auditType,auditSubType,
							orderId,objectId, assignor,orderRemark);
				}

			} catch (BusinessException e) {
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



		@RequestMapping(value = "/findPrincipal")
		public void findPrincipal( HttpServletRequest request){

			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<findPrincipal>");
			}
//		Long orderId=NumberUtils.toLong(request.getParameter("orderId"));


			String messageObject=request.getParameter("messageObject");
			String objectIdStr=messageObject.split("-")[0];
			String orderType=messageObject.split("-")[1];

			Long objectId=NumberUtils.toLong(objectIdStr);

			//子订单负责人
			PermUser permUserPrincipal=null;
			String objectType="ORDER_ITEM";
			if (!"ORDER".equals(orderType)) {//发送给子订单
				permUserPrincipal= orderResponsibleService.getOrderPrincipal(objectType, objectId);
			}
			String json="";
			if (permUserPrincipal!=null && permUserPrincipal.getUserName()!=null) {
				json=JSONObject.fromObject(permUserPrincipal).toString();
			}

			this.sendAjaxResultByJson(json, HttpServletLocalThread.getResponse());
		}


		@RequestMapping(value = "/findComMessageList")
		public String findComMessageList(Model model,Long orderId,Long orderItemId,String auditSubtype,Integer page,HttpServletRequest req){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<findComMessageList>");
			}

			String orderType=HttpServletLocalThread.getRequest().getParameter("orderType");
//		String auditSubtype=request.getParameter("auditSubType");
			//String loginUserId=this.getLoginUserId();
			String objectType="";
			Long objectId=null;
			if ("parent".equals(orderType)) {
				objectId=orderId;
				objectType="ORDER";
			}else{

//			String orderItemId=request.getParameter("orderItemId");
//			objectId=NumberUtils.toLong(orderItemId);
				objectId=orderItemId;
				objectType="ORDER_ITEM";
			}

			Page<ComMessageVO> pageParam = new Page<ComMessageVO>();

			Long[] auditIdArray = findBookingAuditIds(objectType, objectId, auditSubtype);
			List<ComMessage> messageList=new ArrayList<ComMessage>();
			if (auditIdArray.length>0) {

				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("messageStatus", MESSAGE_STATUS.UNPROCESSED.getCode());
				parameters.put("auditIdArray",auditIdArray);

				//parameters.put("receiver",loginUserId);

				int count=comMessageService.findComMessageCount(parameters);
				int pagenum = page == null ? 1 : page;
				pageParam = Page.page(count, 10, pagenum);
				pageParam.buildJSONUrl(req);

				parameters.put("_start", pageParam.getStartRows());
				parameters.put("_end", pageParam.getEndRows());
				parameters.put("_orderby", "COM_MESSAGE.CREATE_TIME");
				parameters.put("_order", "DESC");


				messageList=comMessageService.findComMessageList(parameters);




			}

			List<ComMessageVO> reusltList=new ArrayList<ComMessageVO>();

			for (int i = 0; i < messageList.size(); i++) {

				ComMessage comMessage=messageList.get(i);

				ComMessageVO comMessageVO=new ComMessageVO();
				BeanUtils.copyProperties(comMessage,comMessageVO );


				ComAudit comAudit=orderAuditService.queryAuditById(comMessage.getAuditId());
				String auditSubtypeName="";
				if (!StringUtils.isEmpty(comAudit.getAuditSubtype())) {
					auditSubtypeName= AUDIT_SUB_TYPE.getCnName(comAudit.getAuditSubtype());
				}
				comMessageVO.setAuditSubTypeName(auditSubtypeName);

				reusltList.add(comMessageVO);
			}

			pageParam.setItems(reusltList);
			model.addAttribute("pageParam", pageParam);

			model.addAttribute("messageList", reusltList);


			model.addAttribute("auditSubTypeList", AUDIT_SUB_TYPE.values());




			return "/order/orderStatusManage/allCategory/findComMessageList";

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

			model.addAttribute("certificateTypeList", CERTIFICATE_TYPE.values());



			return "/order/orderStatusManage/allCategory/addCertificate";
		}




		@RequestMapping(value = "/findExpressAddressList")
		public String findExpressAddressList(Model model, HttpServletRequest request,Long orderId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<findExpressAddressList>");
			}
			String haveExpress="false";
			OrdOrder order=this.orderLocalService.findExpressAddress(orderId);
			if (order==null) {
				haveExpress="false";
				order= new OrdOrder();
				model.addAttribute("ordAddress", new OrdAddress());
				model.addAttribute("addressPerson", new OrdPerson());

				model.addAttribute("order", order);

			}else{
				haveExpress="true";
			}
			model.addAttribute("haveExpress", haveExpress);
			model.addAttribute("ordAddress", order.getOrdAddress());
			model.addAttribute("addressPerson", order.getAddressPerson());

			model.addAttribute("order", order);
			return "/order/orderStatusManage/allCategory/findExpressAddressList";
		}


		@RequestMapping(value = "/findExpressOrderList")
		public String findExpressOrderList(Model model, HttpServletRequest request,Long orderId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<findExpressOrderList>");
			}
			String haveExpressOrder="false";
			com.lvmama.order.api.base.vo.RequestBody<Long> requestBody = new com.lvmama.order.api.base.vo.RequestBody<>();//lvxz
			requestBody.setT(orderId);
			com.lvmama.order.api.base.vo.ResponseBody<OrderVo> responseBody = this.apiOrderExpressService.findExpressOrderList(requestBody);

			if (responseBody != null && responseBody.isSuccess()) {
				haveExpressOrder="true";
				model.addAttribute("order", responseBody.getT());
				model.addAttribute("addressPerson", responseBody.getT().getAddressPerson());
				model.addAttribute("expressOrderList", responseBody.getT().getOrdExpressList());
			}else{
				haveExpressOrder="false";
				model.addAttribute("order", new OrdOrder());
				model.addAttribute("addressPerson", new OrdPerson());
				model.addAttribute("expressOrderList", new OrdExpressVo());
			}
			model.addAttribute("haveExpressOrder", haveExpressOrder);


			return "/order/orderStatusManage/allCategory/findExpressOrderList";
		}


		@RequestMapping(value = "/findGoodsPersonList")
		public String findGoodsPersonList(Model model, HttpServletRequest request,Long orderId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<findGoodsPersonList>");
			}

//		List<OrdPerson> travellerPersonList = queryTravellerPersonList(orderId);

			List<GoodsPersonVO> goodsPersonList=new ArrayList<GoodsPersonVO>();

			List<OrdOrderItem> ordItemList=ordOrderUpdateService.queryOrderItemByOrderId(orderId);
			for (OrdOrderItem ordOrderItem : ordItemList) {

				Map<String, Object> params = new HashMap<String, Object>();
				params.put("orderItemId", ordOrderItem.getOrderItemId());
				List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(params);
				if (CollectionUtils.isEmpty(ordItemPersonRelationList)) {
					continue;
				}

				GoodsPersonVO goodsPersonVO=new GoodsPersonVO();

				goodsPersonVO.setGoodsName(ordOrderItem.getSuppGoodsName());

/*
			Map<String,Object> contentMap = ordOrderItem.getContentMap();
			String childQuantity =  (String) contentMap.get(OrderEnum.ORDER_TICKET_TYPE.child_quantity.name());
			String adultQuantity =  (String) contentMap.get(OrderEnum.ORDER_TICKET_TYPE.adult_quantity.name());
			int associationCount=0;
			if (!StringUtils.isEmpty(childQuantity)) {
				associationCount+=NumberUtils.createInteger(childQuantity).intValue();
			}
			if (!StringUtils.isEmpty(adultQuantity)) {
				associationCount+=NumberUtils.createInteger(adultQuantity).intValue();
			}

			goodsPersonVO.setAssociationCount(associationCount); //ordItem的成人数+儿童数
*/
				goodsPersonVO.setAssociationCount(ordItemPersonRelationList.size());
				StringBuffer associationPerson=new StringBuffer();

				goodsPersonVO.setNoAssociationCount(goodsPersonVO.getAssociationCount()-ordItemPersonRelationList.size());//AssociationCount-ordItemPersonRelationList.size()

				for (int i = 0; i < ordItemPersonRelationList.size(); i++) {

					OrdItemPersonRelation ordItemPersonRelation = ordItemPersonRelationList.get(i);

					OrdPerson ordPerson=ordPersonService.findOrdPersonById(ordItemPersonRelation.getOrdPersonId());

					if (i>0) {
						associationPerson.append("</br>");
					}
					associationPerson.append(ordPerson.getFullName());
				}

				goodsPersonVO.setAssociationPerson(associationPerson.toString());

				goodsPersonList.add(goodsPersonVO);

			}


			model.addAttribute("goodsPersonList",goodsPersonList);


			return "/order/orderStatusManage/allCategory/findGoodsPersonList";
		}
		@RequestMapping(value = "/findPreSaleList")
		public String findPreSaleList(Model model, HttpServletRequest request,Long orderId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<findPreSaleList>");
			}
			List<StampCode> stampCodeList = null;
			LOG.info("------------------9------------------");
			String url = Constant.getInstance().getPreSaleBaseUrl() + "/customer/stamp/order/"+orderId;
			StampOrderDetails stampOrderDetails = null;
			try{
				stampOrderDetails = RestClient.getClient().getForObject(url, StampOrderDetails.class);
			}catch(Exception e){
				LOG.error("findPreSaleList查询预售券码信息异常"+e.getMessage());
			}

			// TODO check
			String stampNo = null;
			if(stampOrderDetails!=null){
				stampNo = stampOrderDetails.getStamp().getStampNo();
				stampCodeList=stampOrderDetails.getStampCodes();
				if(stampCodeList != null) {
					for(StampCode stampCode:stampCodeList){
						stampCode.setStampStatus(PresaleEnum.StampStatus.getCnName(stampCode.getStampStatus()));
						if(CollectionUtils.isNotEmpty(stampCode.getUseOrderHis())) {
							for(StampUseOrderSimple stampUse : stampCode.getUseOrderHis()) {
								stampUse.setUnbindStatus(STAMP_UNBIND_STATUS.getCnName(stampUse.getUnbindStatus()));
							}
						}
					}
				}
			}
			model.addAttribute("stampNo", stampNo);
			model.addAttribute("stampCodeList",stampCodeList);

			return "/order/orderStatusManage/allCategory/findPreSaleList";
		}
		@RequestMapping(value = "/findTravellerPersonList")
		public String findTravellerPersonList(Model model, HttpServletRequest request,Long orderId, Long orderItemId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<findTravellerPersonList>");
			}
			if(null!=orderId){
				List<OrdPerson> travellerPersonList = queryTravellerPersonList(orderId);

				// 酒店的子单，和其他的品类不同，子单游玩人不一定是traveller：取消险是ordPerson的类型是traveller并且身份证号不为空，意外险的类型是insure
				OrdOrder order_temp = complexQueryService.queryOrderByOrderId(orderId);
				OrdOrderItem orderItem_temp = order_temp.getOrderItemByOrderItemId(orderItemId);
				// 说明主单品类是酒店
				if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order_temp.getCategoryId())) {
					// 说明子单是保险
					if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem_temp.getCategoryId())){
						List<Long> itemIdList_temp = new ArrayList<Long>();
						itemIdList_temp.add(orderItemId);
						// 说明子单是意外险
						if (SuppGoodsSaleRe.INSURANCE_TYPE.INSURANCE_730.name().equals(orderItem_temp.getProductType())) {
							travellerPersonList = queryInsurerPersonList(orderId);
							Iterator<OrdPerson> it = travellerPersonList.iterator();
							while(it.hasNext()){
								OrdPerson ordPerson = it.next();
								Map<String, Object> paramsOrdPerson = new HashMap<String, Object>();
								paramsOrdPerson.put("ordPersonId", ordPerson.getOrdPersonId());
								paramsOrdPerson.put("orderItemIdArray", itemIdList_temp);
								List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(paramsOrdPerson);
								if (CollectionUtils.isEmpty(ordItemPersonRelationList)) {
									it.remove();
								}
							}
						}
						// 说明是酒店取消险
						if (SuppGoodsSaleRe.INSURANCE_TYPE.INSURANCE_30006.name().equals(orderItem_temp.getProductType())) {

							Iterator<OrdPerson> it = travellerPersonList.iterator();
							while(it.hasNext()){
								OrdPerson ordPerson = it.next();
								Map<String, Object> paramsOrdPerson = new HashMap<String, Object>();
								paramsOrdPerson.put("ordPersonId", ordPerson.getOrdPersonId());
								paramsOrdPerson.put("orderItemIdArray", itemIdList_temp);
								List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(paramsOrdPerson);
								if (CollectionUtils.isEmpty(ordItemPersonRelationList)) {
									it.remove();
								}
							}
						}
					}
				}

				HashMap<Long, OrdOrderItem> ordItemMap=new HashMap<Long, OrdOrderItem>();
				List<OrdOrderItem> ordItemList=ordOrderUpdateService.queryOrderItemByOrderId(orderId);
				boolean hasConnects = false;
				List<Long> itemIdList=new ArrayList<Long>();
				for (int i = 0; i < ordItemList.size(); i++) {
					OrdOrderItem orderItem=ordItemList.get(i);
					itemIdList.add(orderItem.getOrderItemId());
					ordItemMap.put(orderItem.getOrderItemId(), orderItem);
					if(BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(orderItem.getCategoryId())){
						hasConnects = true;
					}
				}

				//游客列表展示
				for (OrdPerson ordPerson : travellerPersonList) {

					//身份标识（护照、士兵证）
					ordPerson.setIdTypeName(ORDER_PERSON_ID_TYPE.getCnName(ordPerson.getIdType()));

					Map<String, Object> paramsOrdPerson = new HashMap<String, Object>();
					paramsOrdPerson.put("ordPersonId", ordPerson.getOrdPersonId());
					paramsOrdPerson.put("orderItemIdArray",itemIdList );
					List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(paramsOrdPerson);

					if (CollectionUtils.isNotEmpty(ordItemPersonRelationList)) {
						StringBuffer goodsName=new StringBuffer();
						for (int i = 0; i < ordItemPersonRelationList.size(); i++) {

							OrdItemPersonRelation ordItemPersonRelation =ordItemPersonRelationList.get(i);
							OrdOrderItem ordItem=ordItemMap.get(ordItemPersonRelation.getOrderItemId());
							if (i>0) {
								goodsName.append("</br>");
							}

							goodsName.append(ordItem.getSuppGoodsName());

						}

						ordPerson.setCheckInRoomName(goodsName.toString());//关联的商品
					}

				}

				model.addAttribute("ordItemList", ordItemList);
				model.addAttribute("personList", travellerPersonList);
				model.addAttribute("hasConnects", hasConnects);

				OrdOrder order=ordOrderUpdateService.queryOrdOrderByOrderId(orderId);
				OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(orderId);
				order.setOrdAccInsDelayInfo(ordAccInsDelayInfo);
				showPlayOutTypeInfo(order,model);
				model.addAttribute("order", order);



				return "/order/orderStatusManage/allCategory/findTravellerPersonList";
			}
			return "";
		}

		/**
		 *判断玩乐订单是否为出境
		 *是否显示出境相关信息
		 *@param order
		 *@param model
		 *@return boolean
		 */
		private void  showPlayOutTypeInfo(OrdOrder order,Model model){
			boolean hasPlayOut = Boolean.FALSE;
			if (order != null && model != null) {
				if (BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(order.getCategoryId())
						|| BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(order.getCategoryId())
						|| BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(order.getCategoryId())) {
					hasPlayOut = Boolean.TRUE;
				}
			}
			model.addAttribute("hasPlayOut", hasPlayOut);
		}

		private List<OrdPerson> queryTravellerPersonList(Long orderId) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("objectType", ORDER_PERSON_OBJECT_TYPE.ORDER.name());
			params.put("objectId", orderId);
			params.put("personType", ORDER_PERSON_TYPE.TRAVELLER.name());
			List<OrdPerson> travellerPersonList=ordPersonService.findOrdPersonList(params);
			return travellerPersonList;
		}

		private List<OrdPerson> queryInsurerPersonList(Long orderId) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("objectType", ORDER_PERSON_OBJECT_TYPE.ORDER.name());
			params.put("objectId", orderId);
			params.put("personType", ORDER_PERSON_TYPE.INSURER.name());
			List<OrdPerson> travellerPersonList=ordPersonService.findOrdPersonList(params);
			return travellerPersonList;
		}

		private List<OrdPerson> queryGuideInfo(Long orderId) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("objectType", ORDER_PERSON_OBJECT_TYPE.ORDER.name());
			params.put("objectId", orderId);
			params.put("personType", ORDER_PERSON_TYPE.GUIDE.name());
			List<OrdPerson> guideList=ordPersonService.findOrdPersonList(params);
			return guideList;
		}

		@RequestMapping(value = "/findEmergencyPersonList")
		public String findEmergencyPersonList(Model model, HttpServletRequest request,Long orderId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<findEmergencyPersonList>");
			}
			if(null != orderId){
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("objectType", ORDER_PERSON_OBJECT_TYPE.ORDER.name());
				params.put("objectId", orderId);
				params.put("personType", ORDER_PERSON_TYPE.EMERGENCY.name());
				List<OrdPerson>  emergencyPersonList=ordPersonService.findOrdPersonList(params);

				model.addAttribute("emergencyPersonList", emergencyPersonList);

				OrdOrder order=ordOrderUpdateService.queryOrdOrderByOrderId(orderId);
				model.addAttribute("order", order);


				return "/order/orderStatusManage/allCategory/findEmergencyPersonList";
			}else{
				return "";
			}

		}

		@RequestMapping(value = "/findTouristList")
		public String findTouristList(Model model, HttpServletRequest request,Long orderItemId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<findTouristList>");
			}

			String branchName  = "";

			OrdOrderItem orderItemObj=ordOrderUpdateService.getOrderItem(orderItemId);
			Map<String,Object> contentMap = orderItemObj.getContentMap();

			ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(BIZ_CATEGORY_TYPE.category_cruise.getCode());
			BizCategory bizCategoryShip=result.getReturnContent();

			ResultHandleT<BizCategory> resultVisa=categoryClientService.findCategoryByCode(BIZ_CATEGORY_TYPE.category_visa.getCode());
			BizCategory bizCategoryVisa=resultVisa.getReturnContent();

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderItemId", orderItemId);
			List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(params);
			List<OrdPerson> personList =new ArrayList<OrdPerson>();

			if (orderItemObj.getCategoryId().equals(bizCategoryShip.getCategoryId())||orderItemObj.getCategoryId().equals(bizCategoryVisa.getCategoryId())) {//油轮 签证 取规格

				for (OrdItemPersonRelation ordItemPersonRelation : ordItemPersonRelationList) {


					OrdPerson ordPerson=ordPersonService.findOrdPersonById(ordItemPersonRelation.getOrdPersonId());
					ordPerson.setIdTypeName(ORDER_PERSON_ID_TYPE.getCnName(ordPerson.getIdType()));

					branchName  =  (String) contentMap.get(ORDER_COMMON_TYPE.branchName.name());
					ordPerson.setCheckInRoomName(branchName);//入住房间

					personList.add(ordPerson);
				}
			}else if (orderItemObj.getCategoryId().longValue() != bizCategoryShip.getCategoryId().longValue()) {//非邮轮

				List<OrdOrderItem> ordItemList=ordOrderUpdateService.queryOrderItemByOrderId(orderItemObj.getOrderId());
				List<Long> itemIdList=new ArrayList<Long>();
				for (int i = 0; i < ordItemList.size(); i++) {

					OrdOrderItem orderItem=ordItemList.get(i);
					if (orderItem.getCategoryId()==bizCategoryShip.getCategoryId()){
						itemIdList.add(orderItem.getOrderItemId());
					}

				}

				for (OrdItemPersonRelation ordItemPersonRelation : ordItemPersonRelationList) {

					OrdPerson ordPerson=ordPersonService.findOrdPersonById(ordItemPersonRelation.getOrdPersonId());
					personList.add(ordPerson);

				}


				//游客列表展示
				for (OrdPerson ordPerson : personList) {

					ordPerson.setIdTypeName(ORDER_PERSON_ID_TYPE.getCnName(ordPerson.getIdType()));

					Map<String, Object> paramsOrdPerson = new HashMap<String, Object>();
					paramsOrdPerson.put("ordPersonId", ordPerson.getOrdPersonId());
					paramsOrdPerson.put("orderItemIdArray",itemIdList.toArray() );
					List<OrdItemPersonRelation> ordItemShipPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(paramsOrdPerson);

					if (!ordItemShipPersonRelationList.isEmpty()) {
						OrdItemPersonRelation ordItemPersonRelation=ordItemShipPersonRelationList.get(0);
						OrdOrderItem orderItemShip=ordOrderUpdateService.getOrderItem(ordItemPersonRelation.getOrderItemId());

						Map<String,Object> contentMapShip = orderItemShip.getContentMap();
						branchName =  (String) contentMapShip.get(ORDER_COMMON_TYPE.branchName.name());

						ordPerson.setCheckInRoomName(branchName);//入住房间
					}

				}
			}


			model.addAttribute("personList", personList);

			String categoryCode =  (String) contentMap.get(ORDER_COMMON_TYPE.categoryCode.name());
			model.addAttribute("categoryCode",categoryCode);


			return "/order/orderStatusManage/allCategory/findTouristList";
		}




		@RequestMapping(value = "/findOrderSuppGoodsInfo")
		public String findOrderSuppGoodsInfo(Model model, HttpServletRequest request,Long orderItemId,Long orderId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<findOrderSuppGoodsInfo>");
			}

			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			OrdOrderItem orderItem=order.getOrderItemByOrderItemId(orderItemId);

			Map<String,Object> contentMap = orderItem.getContentMap();
			String categoryCode =  (String) contentMap.get(ORDER_COMMON_TYPE.categoryCode.name());
			ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(categoryCode);
			BizCategory bizCategory=result.getReturnContent();
			model.addAttribute("bizCategory",bizCategory);
			if(orderItem.hasTicketAperiodic()){
				String goodsExpiryDate = (String)contentMap.get(ORDER_COMMON_TYPE.goodsExpiryDate.name());
				model.addAttribute("goodsExpiryDate", goodsExpiryDate);
				String goodsUnvalidDate = (String)contentMap.get(ORDER_COMMON_TYPE.goodsUnvalidDate.name());
				model.addAttribute("goodsUnvalidDate", goodsUnvalidDate);
			}

			ProdProduct prodProduct = null;
			if(bizCategory.getCategoryId() == 15l || bizCategory.getCategoryId() == 16l){
				prodProduct=prodProductClientService.findProdProductByIdFromCache(orderItem.getProductId()).getReturnContent();
			}
			/** 给订单添加酒店联系电话 >===start*/
			if(bizCategory.getCategoryId() == 1L){//酒店
				Map<String,Object> propParams = new HashMap<String, Object>();
				propParams.put("productId", orderItem.getProductId());
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
			}
			/** 给订单添加酒店联系电话 =========end*/

			model.addAttribute("order",order );
			model.addAttribute("orderItem",orderItem );

			if (bizCategory.getParentId()!=null && (bizCategory.getParentId().equals(5L)
					|| BIZ_CATEGORY_TYPE.category_connects.getCode().equalsIgnoreCase(categoryCode))) {//门票

				String apiFlag="N";
				if (StringUtils.equals(orderItem.getContentStringByKey(ORDER_TICKET_TYPE.notify_type.name()),
						SuppGoods.NOTICETYPE.QRCODE.name())) {

					apiFlag="Y";
				}
				model.addAttribute("apiFlag",apiFlag);
			}else{
				model.addAttribute("apiFlag", orderItem.hasSupplierApi()==true?"Y":"");
			}


			String vistTime=this.buildVisitTime(orderItem);
			model.addAttribute("productType",OrderUtil.getProductType(orderItem));

			//wifi业务
			if(BIZ_CATEGORY_TYPE.category_wifi.getCode().equals(
					categoryCode)){
				if(ProdProduct.WIFIPRODUCTTYPE.WIFI.name().equals(OrderUtil.getProductType(orderItem))){
					model.addAttribute("endTime",orderItem.getContentStringByKey(ORDER_WIFI_TYPE.lease_endDay.name()) );
				}
			}

			model.addAttribute("vistTime",vistTime );

			/**
			 * 2017/03/06 结算状态改造
			 */
			//model.addAttribute("settlementStatusStr", OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.getCnName(orderItem.getSettlementStatus()));

			model.addAttribute("settlementStatusStr", ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.getCnName(getSetSettlementItemStatus(orderItem.getOrderItemId())));



			ResultHandleT<SuppSupplier> resultHandleSuppSupplier = suppSupplierClientService.findSuppSupplierById(orderItem.getSupplierId());
			if (resultHandleSuppSupplier.isSuccess()) {
				SuppSupplier  suppSupplier= resultHandleSuppSupplier.getReturnContent();
				model.addAttribute("suppSupplier", suppSupplier);
			} else {
				LOG.info("method:showOrderStatusManage,resultHandleSuppSupplier.isFial,msg=" + resultHandleSuppSupplier.getMsg());
			}

			//上车点
			Map<String,String> trafficMap = findFrontBusStop(orderItem.getOrderId());
			String frontBusStop=trafficMap.get("frontBusStop");
			String backBusStop=trafficMap.get("backBusStop");
			model.addAttribute("backBusStop", backBusStop);
			model.addAttribute("frontBusStop", frontBusStop);

			LOG.info("==L2803=="+backBusStop);

			int travellerNum=0;
			StringBuffer tnSB = new StringBuffer();

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderItemId", orderItemId);
			List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(params);

			if (CollectionUtils.isNotEmpty(ordItemPersonRelationList)) {

				List<OrdPerson> ordPersonList =new ArrayList<OrdPerson>();
				for (OrdItemPersonRelation ordItemPersonRelation : ordItemPersonRelationList) {

					OrdPerson ordPerson=ordPersonService.findOrdPersonById(ordItemPersonRelation.getOrdPersonId());
					ordPersonList.add(ordPerson);
					if(tnSB.length() > 0) {
						tnSB.append(",");
					}
					tnSB.append(ordPerson.getFullName());
				}
				travellerNum=ordPersonList.size();
			}else{

				List<OrdPerson> personList=order.getOrdPersonList();

				if (personList != null) {
					for (OrdPerson ordPerson : personList) {
						String personType = ordPerson.getPersonType();
						if (ORDER_PERSON_TYPE.TRAVELLER.name().equals(personType)) {
							if(tnSB.length() > 0) {
								tnSB.append(",");
							}
							tnSB.append(ordPerson.getFullName());
							travellerNum += 1;
						}
					}
				}

			}
			String travellerName = tnSB.toString();
			model.addAttribute("travellerName", travellerName);
			model.addAttribute("travellerNum", travellerNum);

			OrdOrderPack ordOrderPack=new OrdOrderPack();
			List<OrdOrderPack> orderPackList=order.getOrderPackList();
			if (CollectionUtils.isNotEmpty(orderPackList)) {
				ordOrderPack=orderPackList.get(0);
			}
			model.addAttribute("ordOrderPack",ordOrderPack);

			Map<String,Object> orderPackContentMap = ordOrderPack.getContentMap();
			String endDate =  (String) orderPackContentMap.get(ORDER_PACK_TYPE.end_sailing_date.name());

			model.addAttribute("endDate",endDate);
			if(!StringUtils.isEmpty(endDate)){
				model.addAttribute("arrivalDays", 1+CalendarUtils.getDayCounts(orderItem.getVisitTime(), DateUtil.toDate(endDate, "yyyy-MM-dd")));
			}

			//驴妈妈价
			StringBuffer lvmamaPrice=new StringBuffer();
			//结算价
			StringBuffer settlementPrice=new StringBuffer();

			//份数
			StringBuffer priceQuantity=new StringBuffer();

			Map<String, Object> paramsMulPrice = new HashMap<String, Object>();
			paramsMulPrice.put("orderItemId", orderItemId);
			List<OrdMulPriceRate> ordMulPriceList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPrice);

			//需要过滤酒店+X 优惠价格
			Iterator<OrdMulPriceRate> ordMulPriceListIter=ordMulPriceList.iterator();
			while(ordMulPriceListIter.hasNext()){
				OrdMulPriceRate rate=ordMulPriceListIter.next();
				if(StringUtils.equals(rate.getPriceType(), ORDER_PRICE_RATE_TYPE.PRICE_HOTEL_PROMOTION.getCode())){
					ordMulPriceListIter.remove();
				}
			}
			//因为门店系统需详细价格数据，此处逻辑影响子单价格信息展示 加此判断 by 李志强  ---2017-03-06
			boolean useordMulPriceList=	BIZ_CATEGORY_TYPE.category_sightseeing.getCode().equals(categoryCode)
					|| BIZ_CATEGORY_TYPE.category_route_group.getCode().equals(categoryCode)
					|| BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(categoryCode)
					|| BIZ_CATEGORY_TYPE.category_route_local.getCode().equals(categoryCode)
					|| BIZ_CATEGORY_TYPE.category_route_customized.getCode().equals(categoryCode)
					|| BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCode().equals(categoryCode)  || BIZ_CATEGORY_TYPE.category_cruise.getCode().equals(categoryCode);

			if (CollectionUtils.isNotEmpty(ordMulPriceList)&& useordMulPriceList) {

				for (OrdMulPriceRate ordMulPriceRate : ordMulPriceList) {
					if (BIZ_CATEGORY_TYPE.category_cruise.getCode().equals(categoryCode)) {
						appendCruisePrice(lvmamaPrice, settlementPrice,ordMulPriceRate);
					} else if (BIZ_CATEGORY_TYPE.category_sightseeing.getCode().equals(categoryCode)
							|| BIZ_CATEGORY_TYPE.category_route_group.getCode().equals(categoryCode)
							|| BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(categoryCode)
							|| BIZ_CATEGORY_TYPE.category_route_local.getCode().equals(categoryCode)
							|| BIZ_CATEGORY_TYPE.category_route_customized.getCode().equals(categoryCode)
							|| BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCode().equals(categoryCode) ) {
						appendLinePrice(lvmamaPrice, settlementPrice,priceQuantity,ordMulPriceRate);
					}
				}
			} else {

				model.addAttribute("singlePrice", "true");

				lvmamaPrice.append("销售价：").append(PriceUtil.trans2YuanStr(orderItem.getPrice())).append("元");
				priceQuantity.append(orderItem.getQuantity()).append("份");

				//门票外币结算项目修改 at 2018/10/10 begin
				OrdOrderItemExtend ordOrderItemExtend = orderItemService.selectOrdOrderItemExtendByOrderItemId(orderItem.getOrderItemId());
				LOG.info("门票外币结算项目子单附加信息ordOrderItemExtend=" + GsonUtils.toJson(ordOrderItemExtend));
				if (ordOrderItemExtend != null) {
					settlementPrice.append("结算价：");
					String foreignSettlementPriceStr = ordOrderItemExtend.getForeignSettlementPrice() != null ?
							new Double(ordOrderItemExtend.getForeignSettlementPrice()) / 100 + "" : "";
					if (StringUtils.isEmpty(foreignSettlementPriceStr)) {
						settlementPrice.append(new Double(orderItem.getSettlementPrice()) / 100 + "");
					}else {
						settlementPrice.append(foreignSettlementPriceStr);
					}
					settlementPrice.append("人民币".equals(ordOrderItemExtend.getCurrencyName()) ? "元" : ordOrderItemExtend.getCurrencyName());
				}else{
					settlementPrice.append("结算价：");
					settlementPrice.append(PriceUtil.trans2YuanStr(orderItem.getActualSettlementPrice())).append("元");
				}
				//门票外币结算项目修改 at 2018/10/10 end

			}


			model.addAttribute("lvmamaPrice",lvmamaPrice.toString());
			model.addAttribute("priceQuantity",priceQuantity.toString());
			model.addAttribute("settlementPrice",settlementPrice.toString());


			if (BIZ_CATEGORY_TYPE.category_hotel.getCode().equals(categoryCode)) {
				//酒店计算入住记录信息
				List<OrdOrderHotelTimeRate> orderHotelTimeRateList=orderItem.getOrderHotelTimeRateList();

				List<OrderHotelTimeRateInfo> orderHotelTimeRateInfoList = orderDetailApportionService.generateHotelTimeRateInfoList(orderItem);
				model.addAttribute("hotelTimeRateInfoList", orderHotelTimeRateInfoList);
				//酒店业务类订单使用状态
				model.addAttribute("performStatus", PERFORM_STATUS_TYPE.getCnName(orderItem.getPerformStatus()));
			} else {
				//非酒店子单计算分摊信息
				OrderItemApportionInfoPO orderItemApportionInfoPO = null;
				if (ApportionUtil.isApportionEnabled()) {
					orderItemApportionInfoPO = orderDetailApportionService.generateItemApportionInfoPO4Detail(orderItem);
				}
				//按照多价格对分摊信息排序
//			orderDetailApportionService.sortAndCompleteApportionInfoByMulPrice(orderItemApportionInfoPO, ordMulPriceList);
				model.addAttribute("orderItemApportionInfo", orderItemApportionInfoPO);
			}

			//门票业务类订单使用状态
			if (ProductCategoryUtil.isTicket(categoryCode)) {
				//门票业务类订单使用状态
				List<OrdTicketPerform> resultList = new ArrayList<OrdTicketPerform>();
				resultList=complexQueryService.selectByOrderItem(orderItemId);
				String performStatusName= PERFORM_STATUS_TYPE.getCnName(OrderUtils.calPerformStatus(resultList,order,orderItem)) ;
				//performStatusName=performStatusName+":成人票"+ordTicketPerform.getActualAdult()+"儿童票"+ordTicketPerform.getActualChild();
				if ("部分使用".equals(performStatusName) && resultList != null && resultList.size() > 0 && orderItem.getAdultQuantity() + orderItem.getChildQuantity() > 0) {
					OrdTicketPerform ordTicketPerform = resultList.get(0);
					performStatusName += ", 未使用："
							+ ((orderItem.getQuantity() == null ? 0: orderItem.getQuantity()) - ((ordTicketPerform
							.getActualAdult() == null ? 0: ordTicketPerform.getActualAdult()) + (ordTicketPerform
							.getActualChild() == null ? 0: ordTicketPerform.getActualChild()))
							/ (orderItem.getAdultQuantity() + orderItem.getChildQuantity()));
				}
				model.addAttribute("performStatus", performStatusName);

				String performTimeStr = "";
				if(CollectionUtils.isNotEmpty(resultList)) {
					List<OrdTicketPerformDetail> list =complexQueryService.selectPerformDetailByOrderItem(orderItemId);
					if(list != null && list.size()>0){
						for(OrdTicketPerformDetail ordTicketPerformDetail : list)
							performTimeStr = performTimeStr + DateUtil.formatDate(ordTicketPerformDetail.getPerformTime(), DateUtil.HHMMSS_DATE_FORMAT)
									+"(成人： "+ordTicketPerformDetail.getActualAdult()+",儿童："+ordTicketPerformDetail.getActualChild()+");";

					}else{
						for(OrdTicketPerform ticketPerform : resultList) {
							if (ticketPerform.getPerformTime() == null || performTimeStr.indexOf(DateUtil.formatSimpleDate(ticketPerform.getPerformTime())) > -1) {
								continue;
							}
							if(performTimeStr.length() > 0) {
								performTimeStr = performTimeStr + "<br/>";
							}

							performTimeStr = performTimeStr + DateUtil.formatDate(ticketPerform.getPerformTime(), DateUtil.HHMMSS_DATE_FORMAT);
						}
					}
				}

				model.addAttribute("performTimeStr", performTimeStr);

				//如果子订单是景点门票，其他票，组合套餐票，长隆多园多日票，且订单未使用计算是否过期
				if (BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(order.getCategoryId())
						|| BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(order.getCategoryId())
						|| BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(order.getCategoryId())){
					String isExpired="";
					if (PERFORM_STATUS_TYPE.UNPERFORM.name().equalsIgnoreCase(OrderUtils.calPerformStatus(resultList,order,orderItem))){
						LOG.info("###orderId=" + order.getOrderId() + "查询子订单快照-商品有效期信息:ordOrderItemId" + orderItem.getOrderItemId());
						SnapshotOrderItemVo snapshotOrderItemVo=null;
						try {
							com.lvmama.order.api.base.vo.ResponseBody<SnapshotOrderItemVo> snapshotOrderItemResponse =
									apiOrderSnapshotService.findSnapshotOrderItem(new com.lvmama.order.api.base.vo.RequestBody<Long>(orderItem.getOrderItemId()));
							if (snapshotOrderItemResponse != null && snapshotOrderItemResponse.isSuccess()) {
								snapshotOrderItemVo = snapshotOrderItemResponse.getT();
								if (null!=snapshotOrderItemVo){
									String useType=snapshotOrderItemVo.getUseType();
									if (StringUtil.isEmptyString(useType)){
										useType="VISIT_TIME";//默认从游玩日起x天有效
									}
									Date now =DateUtil.getTodayDate();
									if ("VISIT_TIME".equalsIgnoreCase(useType)){//按游玩日期使用
										Date visitDate = orderItem.getVisitTime();
										Short days=snapshotOrderItemVo.getDays();
										if (null!=visitDate&&null!=days){
											Date limitDate = CalendarUtils.addDates(visitDate,days.intValue()-1);
											if (now.after(DateUtil.getDayEnd(limitDate))){
												isExpired="是";
											}
										}
									}else if ("ORDER_TIME".equalsIgnoreCase(useType)){//按下单日期使用
										Date createDate = orderItem.getCreateTime();
										Short days=snapshotOrderItemVo.getDays();
										if (null!=createDate&&null!=days){
											Date limitDate = CalendarUtils.addDates(createDate,days.intValue()-1);
											if (now.after(DateUtil.getDayEnd(limitDate))){
												isExpired="是";
											}
										}
									}else if ("PERIOD_TIME".equalsIgnoreCase(useType)){//按时间段使用
										//Date startTime=suppGoodsExpVo.getStartTime();
										Date endTime=snapshotOrderItemVo.getEndTime();
										if (null!=endTime&&now.after(DateUtil.getDayEnd(endTime))){
											isExpired="是";
										}
									}
								}else {
									LOG.warn("查询订快照，商品有效期信息为空,orderItemId=" + orderItem.getOrderItemId());
								}
							} else {
								LOG.error("查询快照异常,orderItemId=" + orderItem.getOrderItemId() + ",异常信息:" + snapshotOrderItemResponse.getMessage());
							}
						} catch (Exception e) {
							LOG.error("###orderItemId=" + orderItem.getOrderItemId() + "查询快照信息异常", e);
						}
					}
					if (StringUtil.isNotEmptyString(isExpired)){
						model.addAttribute("isExpired", isExpired);
					}
				}

				//迪斯尼演出票显示
				if(DisneyUtils.isDisneyShow(orderItem)){
					String disneyItemInfoStr = (String) orderItem.getContentValueByKey("DisneyItemInfo");
					DisneyItemInfo disneyItemInfo = new Gson().fromJson(disneyItemInfoStr, DisneyItemInfo.class);
					List<DisneyShowSeatVo> seats = disneyItemInfo.getSeats();
					String seatsStr ="";
					if(seats!=null && seats.size()>0){
						for(DisneyShowSeatVo seat:seats){
							seatsStr += seat+"<br>";
						}
					}
					model.addAttribute("specialTicketType", SPECIAL_TICKET_TYPE.DISNEY_SHOW.getCode());
					model.addAttribute("sectionDetail", seatsStr);
					String showTime = vistTime+" "+ordOrderDisneyInfoQueryService.queryDisneyShowTime(orderId, orderItemId);
					model.addAttribute("showTime", showTime);
				}
				//玩乐演出票显示
				if(BIZ_CATEGORY_TYPE.category_show_ticket.getCode().equalsIgnoreCase(categoryCode)){
					String startTime = (String) orderItem.getContentValueByKey(ORDER_TICKET_TYPE.showTicketEventStartTime.name());
					String endTime = (String) orderItem.getContentValueByKey(ORDER_TICKET_TYPE.showTicketEventEndTime.name());
					String showTime = (StringUtil.isEmptyString(startTime)?"":startTime) + (StringUtil.isEmptyString(endTime)?"":("-"+endTime));
					if(StringUtil.isEmptyString(showTime)){
						showTime="通场";
					}
					String region = (String) orderItem.getContentValueByKey(ORDER_TICKET_TYPE.showTicketRegion.name());
					String seats = (String) orderItem.getContentValueByKey(ORDER_TICKET_TYPE.showTicketSeats.name());
					model.addAttribute("specialTicketType", "SHOW_TICKET");
					model.addAttribute("sectionDetail", region);
					model.addAttribute("seatsDetail", seats);
					model.addAttribute("showTime", showTime);

					//ResultHandleT<SuppGoods> resultHandleSuppGoods = suppGoodsClientService.findSuppGoodsById(orderItem.getSuppGoodsId(), Boolean.TRUE, Boolean.TRUE);
					ResultHandleT<SuppGoods> resultHandleSuppGoods = suppGoodsHotelAdapterClientService.findSuppGoodsById(orderItem.getSuppGoodsId());
					SuppGoods suppGoods = new SuppGoods();
					if (resultHandleSuppGoods.isSuccess() && resultHandleSuppGoods.getReturnContent() != null) {
						suppGoods = resultHandleSuppGoods.getReturnContent();
					}
					if(suppGoods!=null){
						model.addAttribute("goodsSpecName", GOODSSPEC.getSpecName(suppGoods.getGoodsSpec()));//票种信息
					}
				}
			}else if(ProductCategoryUtil.isWifi(categoryCode) ||  ProductCategoryUtil.isConnects(categoryCode) || ProductCategoryUtil.isPlay(categoryCode)){
				List<OrdTicketPerform> resultList = new ArrayList<OrdTicketPerform>();
				resultList=complexQueryService.selectByOrderItem(orderItemId);
				String performStatusName= PERFORM_STATUS_TYPE.getCnName(OrderUtils.calPalyNoShowticketPerformStatus(resultList,order,orderItem)) ;
				if ("部分使用".equals(performStatusName) && resultList != null && resultList.size() > 0 ) {
					OrdTicketPerform ordTicketPerform = resultList.get(0);
					//当地玩乐除演出票（已属门票）默认每份成人票儿童票1/0
					performStatusName += ", 未使用："
							+ ((orderItem.getQuantity() == null ? 0: orderItem.getQuantity()) - ((ordTicketPerform
							.getActualAdult() == null ? 0: ordTicketPerform.getActualAdult()) + (ordTicketPerform
							.getActualChild() == null ? 0: ordTicketPerform.getActualChild())));
				}
				model.addAttribute("performStatus", performStatusName);
			}
			//是否门票订单 因为保险也要显示使用主订单判断
			if(OrderUtils.isTicketByCategoryId(order.getCategoryId())){
				//门票子订单详情部分退新增 总退款份数 退款明细
				Long refundQuantity=orderItem.getRefundQuantity();
				model.addAttribute("refundQuantity", refundQuantity==null?0L:refundQuantity);
				//是门票订单
				model.addAttribute("isTicketOrder", "Y");
			}


			model.addAttribute("isbuyoutFlag", orderItem.getBuyoutFlag());

			model.addAttribute("prodProductTourType", prodProduct != null ? (prodProduct.getProducTourtType() == null ? null : (prodProduct.getProducTourtType().equals("ONEDAYTOUR") ? "一日游" : "多日游")) : null);
			String itemCancelStatus = null;
			//根据主单ID  子订单出票状态查询  ord_item_ticket
		        OrdItemTicketVo ordItemTicketOv = new OrdItemTicketVo();
		        com.lvmama.order.api.base.vo.RequestBody<OrdItemTicketVo> request2 = new com.lvmama.order.api.base.vo.RequestBody<OrdItemTicketVo>();
		        ordItemTicketOv.setOrderId(order.getOrderId());
		        ordItemTicketOv.setOrderItemId(orderItemId);
		        request2.setT(ordItemTicketOv);
		        com.lvmama.order.api.base.vo.ResponseBody<OrdItemTicketVo> responseBody2 = apiOrderItemCancelService.selectOrdItemTicketNum(request2);
		        if (null != responseBody2 && null != responseBody2.getT()) {
		            OrdItemTicketVo ordItemTicket = responseBody2.getT();
		            if (ordItemTicket != null && 
				        ordItemTicket.getActualCount() != null && 
				        ordItemTicket.getTicketCount() != null) {
				    if (ordItemTicket.getActualCount() == ordItemTicket.getTicketCount()) {
				        itemCancelStatus = ItemCancelEnum.ORDER_CANCEL_CODE.ALREADY.getCnName();
				    } else if (ordItemTicket.getTicketCount() != Long.getLong(ItemCancelEnum.ORDER_CANCEL_CODE.WITHOUT.getCode()) &&
				            ordItemTicket.getTicketCount() < ordItemTicket.getActualCount()) {
				        itemCancelStatus = ItemCancelEnum.ORDER_CANCEL_CODE.PART.getCnName();
				    }
				}
		        }
			model.addAttribute("itemCancelStatus", itemCancelStatus);


			//判断ebk邮件是否发送了--将页面EBK发送状态由 查询EBK库 改成 查询订单系统本地数据库--页面不改变lvxz
			com.lvmama.order.api.base.vo.RequestBody<Long> requestBody = new com.lvmama.order.api.base.vo.RequestBody<>();
			requestBody.setT(orderItemId);
			com.lvmama.order.api.base.vo.ResponseBody<OrdEbkEmailVo> responseBody = this.apiEbkEmailQueryService.selectOrdEbkEmailByOrderItemId(requestBody);
			if( responseBody.isSuccess() && null != responseBody.getT()){
				model.addAttribute("alreadySend", true);
			}else{
				model.addAttribute("alreadySend", false);
			}

			//判断ebk邮件是否发送了old
//			Map<String,Object> ebkEmailAttachMap = new HashMap<>();
//			ebkEmailAttachMap.put("orderItemId", orderItemId);
//			ResultHandleT<List<EbkEmailAttch>> ebkEmailAttaches = ebkEmailAttchClientServiceRemote.selectEbkEmailListByPrams(ebkEmailAttachMap);
//			if(ebkEmailAttaches.getReturnContent()!=null&&ebkEmailAttaches.getReturnContent().size()>0){
//				model.addAttribute("alreadySend", true);
//			}else{
//				model.addAttribute("alreadySend", false);
//			}
			// 修改成直接调用ebk的接口处理 ebk
			//boolean alreadySend = ebkEmailClientService.isSendMail(orderId.toString(), orderItemId).getReturnContent();
			//model.addAttribute("alreadySend", alreadySend);

			if(BIZ_CATEGORY_TYPE.category_finance.getCategoryId().longValue()==orderItem.getCategoryId().longValue()) {
				Long buyItemTotalPrice = orderItem.getPrice()*orderItem.getQuantity();
			
				model.addAttribute("buyItemTotalPrice",PriceUtil.trans2YuanStr(buyItemTotalPrice)+"元");
				
				String financeInterestsBonusVoJson = orderItem.getContentStringByKey("financeInterestsBonusVo");
				if(financeInterestsBonusVoJson != null && !"".equals(financeInterestsBonusVoJson)) {
					FinanceInterestsBonus financeInterestsBonusVo = com.alibaba.fastjson.JSONObject.toJavaObject(JSON.parseObject(financeInterestsBonusVoJson), FinanceInterestsBonus.class);
					Double interestsPercent = financeInterestsBonusVo.getInterestsPercent();
					String rightPrice = PriceUtil.StrToStrtrans2Yuan(String.valueOf(orderItem.getTotalAmount()),interestsPercent);
					orderItem.setRightPrice(PriceUtil.trans2YuanStr(rightPrice)+"元");
					model.addAttribute("financeInterestsBonusVo", financeInterestsBonusVo);
				}
				return "/order/orderStatusManage/allCategory/findFinanceOrderSuppGoodsInfo";
			}else {
				return "/order/orderStatusManage/allCategory/findOrderSuppGoodsInfo";
			}
			
		}

		private void appendLinePrice(StringBuffer lvmamaPrice,
				StringBuffer settlementPrice, StringBuffer priceQuantity,OrdMulPriceRate ordMulPriceRate) {
			if (ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCode().equals(ordMulPriceRate.getPriceType())) {

				lvmamaPrice.append(ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCnName(ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCode())).append(":").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");
				priceQuantity.append(ordMulPriceRate.getQuantity()).append("份");
			}else if (ORDER_PRICE_RATE_TYPE.PRICE_CHILD.getCode().equals(ordMulPriceRate.getPriceType())) {

				if (lvmamaPrice.length() > 0) {
					lvmamaPrice.append("</br>");
				}
				lvmamaPrice.append(ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCnName(ORDER_PRICE_RATE_TYPE.PRICE_CHILD.getCode())).append(":").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");

				if (priceQuantity.length() > 0) {
					priceQuantity.append("</br>");
				}
				priceQuantity.append(ordMulPriceRate.getQuantity()).append("份");

			}else if (ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.getCode().equals(ordMulPriceRate.getPriceType())) {

				if (lvmamaPrice.length() > 0) {
					lvmamaPrice.append("</br>");
				}
				lvmamaPrice.append(ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCnName(ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.getCode())).append(":").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");

				if (priceQuantity.length() > 0) {
					priceQuantity.append("</br>");
				}
				priceQuantity.append(ordMulPriceRate.getQuantity()).append("份");

			}else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.getCode().equals(ordMulPriceRate.getPriceType())) {

				settlementPrice.append(ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCnName(ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT.getCode())).append(":").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");

			}else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.getCode().equals(ordMulPriceRate.getPriceType())) {

				if (settlementPrice.length() > 0) {
					settlementPrice.append("</br>");
				}
				settlementPrice.append(ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCnName(ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD.getCode())).append(":").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");

			}else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.getCode().equals(ordMulPriceRate.getPriceType())) {

				if (settlementPrice.length() > 0) {
					settlementPrice.append("</br>");
				}
				settlementPrice.append(ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCnName(ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD.getCode())).append(":").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");

			}else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode().equals(ordMulPriceRate.getPriceType())) {

				settlementPrice.append(ORDER_PRICE_RATE_TYPE.PRICE_ADULT_PRE.getCnName(ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.getCode())).append(":").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");

			}else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode().equals(ordMulPriceRate.getPriceType())) {

				if (settlementPrice.length() > 0) {
					settlementPrice.append("</br>");
				}
				settlementPrice.append(ORDER_PRICE_RATE_TYPE.PRICE_ADULT_PRE.getCnName(ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.getCode())).append(":").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");

			}
		}


		private void appendCruisePrice(StringBuffer lvmamaPrice,
				StringBuffer settlementPrice, OrdMulPriceRate ordMulPriceRate) {
			if (ORDER_PRICE_RATE_TYPE.PRICE_ADULT_12.getCode().equals(ordMulPriceRate.getPriceType())) {

				lvmamaPrice.append("第1、2人价格：").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");

			}else if (ORDER_PRICE_RATE_TYPE.PRICE_ADULT_34.getCode().equals(ordMulPriceRate.getPriceType())) {

				if (lvmamaPrice.length() > 0) {
					lvmamaPrice.append("</br>");
				}

				lvmamaPrice.append("第3、4人价格：").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");

			}else if (ORDER_PRICE_RATE_TYPE.PRICE_CHILD_34.getCode().equals(ordMulPriceRate.getPriceType())) {

				if (lvmamaPrice.length() > 0) {
					lvmamaPrice.append("</br>");
				}
				lvmamaPrice.append("第3、4人儿童价格：").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");

			}else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_12.getCode().equals(ordMulPriceRate.getPriceType())) {

				settlementPrice.append("第1、2人结算价格：").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");

			}else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_34.getCode().equals(ordMulPriceRate.getPriceType())) {

				if (lvmamaPrice.length() > 0) {
					settlementPrice.append("</br>");
				}
				settlementPrice.append("第3、4人结算价格：").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");

			}else if (ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_34.getCode().equals(ordMulPriceRate.getPriceType())) {

				if (lvmamaPrice.length() > 0) {
					settlementPrice.append("</br>");
				}
				settlementPrice.append("第3、4人儿童结算价格：").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");

			}
		}


		@RequestMapping(value = "/findOrderBaseInfo")
		public String findOrderBaseInfo(Model model, HttpServletRequest request,Long orderItemId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<findOrderBaseInfo>");
			}

			OrdOrderItem orderItem=ordOrderUpdateService.getOrderItem(orderItemId);

			OrdOrder order = complexQueryService.queryOrderByOrderId(orderItem.getOrderId());
			if( "STAMP".equals(order.getOrderSubType())){
				getStampOrdOrder(order,model);
			}
			model.addAttribute("order",order );
			model.addAttribute("orderItem",orderItem );





			//下单渠道
			Distributor distributor = distributorClientService.findDistributorById(order.getDistributorId()).getReturnContent();

			model.addAttribute("distributorName", distributor.getDistributorName());
			
			Long orderId = order.getOrderId();
			List<OrderTagVo> orderTagVos = null;
			try {
				OrderTagVo orderTagvo = new OrderTagVo();
				orderTagvo.setTagType("SNAPSHOT_TAG");
				orderTagvo.setObjectType("ORD_ORDER");
				orderTagvo.setObjectId(orderId);
				com.lvmama.order.api.base.vo.ResponseBody<List<OrderTagVo>> tagResponseBody = 
						apiOrderTagService.queryOrderTags(new com.lvmama.order.api.base.vo.RequestBody<OrderTagVo>(orderTagvo));
				if (tagResponseBody != null && tagResponseBody.isSuccess()) {
					orderTagVos = tagResponseBody.getT();
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

			//所属公司   产品经理
			ResultHandleT<SuppGoods> resultHandleuppGoods = suppGoodsClientService.findSuppGoodsById(orderItem.getSuppGoodsId(), Boolean.TRUE, Boolean.TRUE);
			if (resultHandleuppGoods.isFail() || resultHandleuppGoods.getReturnContent() == null) {
				throw new RuntimeException("商品(ID=" + orderItem.getSuppGoodsId() + ")获取失败。msg=" + resultHandleuppGoods.getMsg());
			}
			SuppGoods suppGoods = resultHandleuppGoods.getReturnContent();
			model.addAttribute("filialeName", CommEnumSet.FILIALE_NAME.getCnName(suppGoods.getFiliale()));
			if(orderItem.getManagerId() != null){
				PermUser permUser=permUserServiceAdapter.getPermUserByUserId(orderItem.getManagerId());
				if(permUser != null){
					model.addAttribute("productManager",permUser );
				}
			}
		/*
		OrdOrderPack ordOrderPack=new OrdOrderPack();
		Map<String, Object> paramPack = new HashMap<String, Object>();
		paramPack.put("orderId", orderItem.getOrderId());//订单号

		List<OrdOrderPack> orderPackList=ordOrderPackService.findOrdOrderPackList(paramPack);
		if (!orderPackList.isEmpty()) {
			ordOrderPack=orderPackList.get(0);
		}
		ProdProduct prodProduct=prodProductClientService.findProdProductById(ordOrderPack.getProductId(), Boolean.TRUE, Boolean.TRUE);
		*/



			//退改政策
			String cancelStrategyType=orderItem.getCancelStrategy();
			if("STAMP".equals(order.getOrderSubType())){
				model.addAttribute("cancelStrategyType", order.getCancelStrategy());
			}else{
				if (!StringUtils.isEmpty(cancelStrategyType)) {
//			model.addAttribute("cancelStrategyType", cancelStrategyType);
					if(orderItem.getContentStringByKey("refundRules") != null && !"".equals(orderItem.getContentStringByKey("refundRules"))){
						model.addAttribute("cancelStrategyTypeStr", SuppGoodsTimePrice.CANCELSTRATEGYTYPE.getCnName(cancelStrategyType)+"<br/>"+orderItem.getContentStringByKey("refundRules"));
					}else{
						model.addAttribute("cancelStrategyTypeStr", SuppGoodsTimePrice.CANCELSTRATEGYTYPE.getCnName(cancelStrategyType));
					}

				}
			}
			model.addAttribute("deductAmountStr", PriceUtil.trans2YuanStr(orderItem.getDeductAmount()));


			if(orderItem.getLastCancelTime()!=null){
				Date now=new Date();
				Date lastCancelTime=orderItem.getLastCancelTime();
				model.addAttribute("isGreaterNow", now.compareTo(lastCancelTime));
			}

			if(BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(orderItem.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(orderItem.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().equals(orderItem.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(orderItem.getCategoryId())){
				//显示子订单的阶梯退改规则
				Map<String, Object> mp = differentialChildRule(orderItem);
				model.addAttribute("refoundStr", mp.get("refoundStr"));
				model.addAttribute("backAmount", mp.get("backAmount") == null ? null : mp.get("backAmount").toString());
				if(showBackRule(order)){
					model.addAttribute("isTicket", "Y");
				}else{
					model.addAttribute("isTicket", "N");
				}
			}else{
				model.addAttribute("isTicket", "N");
			}
			if(!BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){//排除保险
				//判断是否是新的退改规则的自由行或者酒店套餐
				if(!(ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.getCode().equals(order.getRealCancelStrategy()) ||
						ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(order.getRealCancelStrategy()))){
					//显示子订单的阶梯退改规则
					// TODO
					model.addAttribute("isFreedomNewRule", "Y");
					Map<String, Object> mp;
					if(ProductCategoryUtil.isTicket(orderItem.getCategoryId())){//门票
						mp = differentialChildRule(orderItem);
					}else{
						mp = getChildRule(orderItem, order.getVisitTime());
					}
					model.addAttribute("refoundStr", mp.get("refoundStr"));
					model.addAttribute("backAmount", mp.get("backAmount").toString());
				}else if(ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.getCode().equals(order.getRealCancelStrategy()) &&
						ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy()) &&
						BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())){//自由行打包酒店套餐（自由行同步商品退改，酒店套餐可退该）
					model.addAttribute("isFreedomNewRule", "Y");
					Map<String, Object> mp = getChildRule(orderItem, orderItem.getVisitTime());
					model.addAttribute("refoundStr", mp.get("refoundStr"));
					model.addAttribute("backAmount", mp.get("backAmount").toString());
				}else{
					model.addAttribute("isFreedomNewRule", "N");
				}
			}

			Map<String,Object> contentMap = orderItem.getContentMap();
			//如果是期票，加入有效期信息
			if("Y".equals(contentMap.get(ORDER_TICKET_TYPE.aperiodic_flag.name()))){
				model.addAttribute("ordItemAperiodicExp",contentMap.get(ORDER_TICKET_TYPE.goodsExpInfo.name()));
			}


			return "/order/orderStatusManage/allCategory/findOrderBaseInfo";
		}



		@RequestMapping(value = "/findOrdPersonBooker")
		public String findOrdPersonBooker(Model model, HttpServletRequest request,Long orderId,Long orderItemId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<findOrdPersonBooker>");
			}

			//OrdOrderItem orderItem=ordOrderUpdateService.getOrderItem(orderItemId);

			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
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
				log.info("======id:"+id);
				Map<String, Object> map = csVipDubboService.getCsVipByCondition(id,
						CsVipUserIdentityTypeEnum.USER_ID);
				isCsVip = (Boolean) map.get("isCsVip");
				//20180726   接口添加新字段 userType 会员类型
				Integer userType = (Integer) map.get("userType");
				log.info("========userType:"+userType);
				if(userType!=null){
					userTypeStr =  SrvMemberEnum.USER_TYPE.getCnName(userType);
				}else {
					userTypeStr =  SrvMemberEnum.USER_TYPE.getCnName( NORMAL_USER);
				}
				
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}

			List<OrdPerson> personList=order.getOrdPersonList();
			OrdPerson ordPersonContact = new OrdPerson();
			OrdPerson firstTravellerPerson = null;
			OrdPerson ordPersonBooker = new OrdPerson();

			//游客列表展示
			for (OrdPerson ordPerson : personList) {

				String personType = ordPerson.getPersonType();
				if (ORDER_PERSON_TYPE.CONTACT.name().equals(personType)) {

					ordPersonContact = ordPerson;
					continue;
				}else if (ORDER_PERSON_TYPE.BOOKER.name().equals(personType)) {

					ordPersonBooker = ordPerson;
					continue;
				} else if (ORDER_PERSON_TYPE.TRAVELLER.name().equals(
						personType)) {

					if (firstTravellerPerson==null) {
						firstTravellerPerson=ordPerson;
					}
					// travellerPersonList.add(ordPerson);
				/*if (travellerName.length() > 0) {
					travellerName += ",";
				}
				travellerName += ordPerson.getFullName();
				travellerNum+=1;*/
				}

			}

			//门票没有联系人   默认第一游客为联系人
			if (ordPersonContact!=null && ordPersonContact.getOrdPersonId()==null && firstTravellerPerson!=null) {
				ordPersonContact=firstTravellerPerson;
			}
			model.addAttribute("userSuperVip",userSuperVip);

			//添加会员类型
			model.addAttribute("userTypeStr",userTypeStr);
			
			model.addAttribute("isCsVip", isCsVip);
			model.addAttribute("ordPersonContact", ordPersonContact);
			model.addAttribute("ordPersonBooker", ordPersonBooker);
			showPlayOutTypeInfo(order,model);
			
			model.addAttribute("order",order );
			//model.addAttribute("orderItem",orderItem );
			//判断ebk邮件是否发送了
			if(orderItemId!=null){
				model.addAttribute("orderItemId",orderItemId);
				model.addAttribute("orderId",orderId);

				com.lvmama.order.api.base.vo.RequestBody<Long> requestBody = new com.lvmama.order.api.base.vo.RequestBody<>();
				requestBody.setT(orderItemId);
				com.lvmama.order.api.base.vo.ResponseBody<OrdEbkEmailVo> responseBody = this.apiEbkEmailQueryService.selectOrdEbkEmailByOrderItemId(requestBody);
				if( responseBody.isSuccess() && null!= responseBody.getT()){
					model.addAttribute("alreadySend", true);
				}else{
					model.addAttribute("alreadySend", false);
				}

//				Map<String,Object> ebkEmailAttachMap = new HashMap<>();
//				ebkEmailAttachMap.put("orderItemId", orderItemId);
//				ResultHandleT<List<EbkEmailAttch>> ebkEmailAttaches = ebkEmailAttchClientServiceRemote.selectEbkEmailListByPrams(ebkEmailAttachMap);
//				if(ebkEmailAttaches.getReturnContent()!=null&&ebkEmailAttaches.getReturnContent().size()>0){
//					model.addAttribute("alreadySend", true);
//				}else{
//					model.addAttribute("alreadySend", false);
//				}

				// 修改成直接调用ebk的接口处理new
				//boolean alreadySend = ebkEmailClientService.isSendMail(orderId.toString(), orderItemId).getReturnContent();
				//model.addAttribute("alreadySend", alreadySend);


			}
			String email = ordPersonContact.getEmail();
			model.addAttribute("email",email );
			firstTravellerPerson = order.getFirstAdultTravellerPerson();
			if(firstTravellerPerson!=null){
				email = firstTravellerPerson.getEmail();
				if(!StringUtil.isEmptyString(email)){
					model.addAttribute("email",email );
				}
			}

			return "/order/orderStatusManage/allCategory/findOrdPersonBooker";
		}



		@RequestMapping(value = "/showUpdateTourist")
		public String showUpdateTourist(Model model, HttpServletRequest request,Long orderId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<showUpdateTourist>");
			}
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);

			List<OrdPerson> personList = order.getOrdPersonList();

			List<OrdPerson> travellerList = new ArrayList<OrdPerson>();
			//游客列表展示
			for (OrdPerson ordPerson : personList) {


				ordPerson.setIdTypeName(ORDER_PERSON_ID_TYPE.getCnName(ordPerson.getIdType()));

				Map<String, Object> params = new HashMap<String, Object>();
				params.put("ordPersonId", ordPerson.getOrdPersonId());
				List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(params);


				if (!ordItemPersonRelationList.isEmpty()) {
					OrdItemPersonRelation ordItemPersonRelation=ordItemPersonRelationList.get(0);
					OrdOrderItem orderItemObj=ordOrderUpdateService.getOrderItem(ordItemPersonRelation.getOrderItemId());

					Map<String,Object> contentMap = orderItemObj.getContentMap();
					String branchName =  (String) contentMap.get(ORDER_COMMON_TYPE.branchName.name());

					ordPerson.setCheckInRoomName(branchName);//入住房间

					ordPerson.setOrderItemId(ordItemPersonRelation.getOrderItemId());

				}

				if (ORDER_PERSON_TYPE.TRAVELLER.name().equals(ordPerson.getPersonType())) {

					travellerList.add(ordPerson);
				}

			}

			//BizCategory additionBizCategory = categoryService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_addition.getCode());

			Map<String, String> roomNameMap = new HashMap<String, String>();
			List<OrdOrderItem> orderItemList=ordOrderUpdateService.queryOrderItemByOrderId(orderId);
			for (OrdOrderItem ordOrderItem : orderItemList) {

				ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(BIZ_CATEGORY_TYPE.category_cruise.getCode());
				BizCategory bizCategory=result.getReturnContent();

				if (ordOrderItem.getCategoryId().longValue() == bizCategory.getCategoryId().longValue()) {//邮轮

					Map<String,Object> contentMap = ordOrderItem.getContentMap();
					String branchName =  (String) contentMap.get(ORDER_COMMON_TYPE.branchName.name());

					roomNameMap.put(ordOrderItem.getOrderItemId()+"", branchName);
				}

			}



			model.addAttribute("personList", travellerList);
			model.addAttribute("roomNameMap", roomNameMap);

			// 性别
		/*Map<String, String> genderMap = new LinkedHashMap<String, String>();
		//List gennderList=new ArrayList<String>();
		for (ORDER_PERSON_GENDER_TYPE item : ORDER_PERSON_GENDER_TYPE.values()) {
			genderMap.put(item.getCode(), item.getCnName());
		}*/
			model.addAttribute("genderTypeList", ORDER_PERSON_GENDER_TYPE.values());

			model.addAttribute("ordItemPersonRelation", new OrderDetailAction());

			return "/order/orderStatusManage/allCategory/showUpdateTourist";
		}



		@RequestMapping(value = "/updateTourist")
		@ResponseBody
		public Object updateTourist( HttpServletRequest request,OrdOrder order,OrdItemPersonRelation ordItemPersonRelation){

			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<updateTourist>");
			}
			Long orderId=order.getOrderId();

			//逻辑校验
			//Map<Long, Object> roomNameMap = new HashMap<Long, Object>();
			Map<Long, Integer> oldMap = new HashMap<Long, Integer>();
			List<OrdOrderItem> orderItemList=ordOrderUpdateService.queryOrderItemByOrderId(orderId);
			for (OrdOrderItem ordOrderItem : orderItemList) {


				ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(BIZ_CATEGORY_TYPE.category_cruise.getCode());
				BizCategory bizCategory=result.getReturnContent();

				if (ordOrderItem.getCategoryId().longValue() == bizCategory.getCategoryId().longValue()) {//邮轮

					Map<String, Object> params = new HashMap<String, Object>();
					params.put("orderItemId", ordOrderItem.getOrderItemId());
					List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(params);
					oldMap.put(ordOrderItem.getOrderItemId(), ordItemPersonRelationList.size());
				}

			}

			List<OrdPerson>  ordPersonList=ordItemPersonRelation.getOrdPersonList();

			Map<Long, Integer> newMap = new HashMap<Long, Integer>();
			for (OrdPerson ordPerson : ordPersonList) {

				Long ordItemId=ordPerson.getCheckInRoom();
				if (newMap.containsKey(ordItemId)) {
					int num=newMap.get(ordItemId);
					newMap.put(ordItemId, ++num);

				} else {
					newMap.put(ordItemId, 1);
				}
			}

			ResultHandle result=new ResultHandle();
			for(Map.Entry<Long, Integer> entry : oldMap.entrySet()) {

				int oldNum=entry.getValue();
				int newNum=0;
				if (newMap.containsKey(entry.getKey())) {
					newNum=newMap.get(entry.getKey());
				}

				if (oldNum!=newNum) {

					OrdOrderItem ordOrderItem=ordOrderUpdateService.getOrderItem(entry.getKey());
					Map<String,Object> contentMap = ordOrderItem.getContentMap();
					String branchName =  (String) contentMap.get(ORDER_COMMON_TYPE.branchName.name());

					result.setMsg(branchName+" 新的房间数量:"+newNum+"和原有房间数量"+oldNum+",不一致 ");
					break;
				}
			}


			if (result.isFail()) {
				return new ResultMessage(ResultMessage.ERROR, result.getMsg());

			}


			for (OrdPerson ordPerson : ordPersonList) {
				if(!ORDER_PERSON_ID_TYPE.HUIXIANG.name().equals(ordPerson.getIdType())
						&&!ORDER_PERSON_ID_TYPE.TAIBAOZHENG.name().equals(ordPerson.getIdType())){
					ordPerson.setIssued("");
					ordPerson.setExpDate(null);
				}

				this.ordPersonService.updateByPrimaryKeySelective(ordPerson);

			/*OrdItemPersonRelation ordItemPersonRelationObj=new OrdItemPersonRelation();
			ordItemPersonRelationObj.setOrdPersonId(ordPerson.getOrdPersonId());
			ordItemPersonRelationObj.setOrderItemId(ordPerson.getCheckInRoom());

			this.ordItemPersonRelationService.updateByPrimaryKeySelective(ordItemPersonRelationObj);
			*/

				Map<String, Object> params = new HashMap<String, Object>();
				params.put("ordPersonId", ordPerson.getOrdPersonId());
				params.put("orderItemId", ordPerson.getCheckInRoom());
				params.put("oldOrderItemId", ordPerson.getOrderItemId());

				this.ordItemPersonRelationService.updateSelective(params);
			}

			return ResultMessage.UPDATE_SUCCESS_RESULT;

		}





		@RequestMapping(value = "/showUpdateWaitPaymentTime")
		public String showUpdateWaitPaymentTime(Model model, HttpServletRequest request){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<showUpdateWaitPaymentTime>");
			}




			return "/order/orderStatusManage/allCategory/showUpdateWaitPaymentTime";
		}

		@RequestMapping(value = "/updateWaitPaymentTime")
		@ResponseBody
		public Object updateWaitPaymentTime( HttpServletRequest request,OrdOrder order){

			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<updateWaitPaymentTime>");
			}
			Long orderId=order.getOrderId();

//		String addWaitPaymentTime=request.getParameter("addWaitPaymentTime");
			OrdOrder oldOrder=ordOrderUpdateService.queryOrdOrderByOrderId(orderId);
			List<OrdOrderItem> orderItems = orderUpdateService.queryOrderItemByOrderId(orderId);
			if(oldOrder != null){
				order.setOrderItemList(orderItems);
			}
			//Date newDate=DateUtils.addHours(oldOrder.getWaitPaymentTime(), Integer.parseInt(addWaitPaymentTime));

			String waitPaymentTime=request.getParameter("waitPaymentTime");
			Date newWaitPaymentTime=DateUtil.toDate(waitPaymentTime, "yyyy-MM-dd HH:mm");
			if (oldOrder.isCancel()) {
				String message="订单已经取消不可修改";
				return new ResultMessage(ResultMessage.ERROR,message);
			}

			Date lastCancelTime=oldOrder.getLastCancelTime();
			if (lastCancelTime!=null) {

				Date maxDate=DateUtil.DsDay_Minute(lastCancelTime, -10);

				if (newWaitPaymentTime.after(maxDate)) {
					String message="游玩日期："+DateUtil.formatDate(oldOrder.getVisitTime(), "yyyy-MM-dd")+", 最晚支付等待时间最晚为（最晚取消时间前10分钟）："+DateUtil.formatDate(maxDate, "yyyy-MM-dd HH:mm");
					return new ResultMessage(ResultMessage.ERROR,message);
				}

			}

			//对接机票不需要下面这个限制
			if (!order.isContainApiFlightTicket()) {
				Date minDate=this.orderStatusManageService.getMinDate(orderId, lastCancelTime);
				if (minDate!=null && newWaitPaymentTime!=null && newWaitPaymentTime.after(minDate)) {
					String message=" 最晚支付等待时间最晚为（资源保留时间、最晚无损取消时间中最小的）："+DateUtil.formatDate(minDate, "yyyy-MM-dd HH:mm");
					return new ResultMessage(ResultMessage.ERROR,message);
				}
			}

			/**
			 * 新增判断，如果修改过后的支付等待时间不能小于下单时间
			 * */
			if(newWaitPaymentTime != null && newWaitPaymentTime.before(oldOrder.getCreateTime())){
				String message=" 支付等待时间不得早于下单时间!";
				return new ResultMessage(ResultMessage.ERROR,message);
			}
			/**
			 * 新增判断,如果修改后的支付等待时间大于出玩日期
			 * */
			if(newWaitPaymentTime != null && newWaitPaymentTime.after(DateUtil.dsDay_Date(oldOrder.getVisitTime(), 1))){
				String message=" 支付等待时间不得晚于出发日期!";
				return new ResultMessage(ResultMessage.ERROR,message);
			}

			/**
			 * 新增判断,修改后的支付等待时间不能在当前时间之前
			 * */
			if(newWaitPaymentTime != null && newWaitPaymentTime.before(new Date())){
				String message=" 支付等待时间不得晚于早于当前操作时间!";
				return new ResultMessage(ResultMessage.ERROR,message);
			}


			order.setWaitPaymentTime(newWaitPaymentTime);



			int n=this.orderUpdateService.updateByPrimaryKeySelective(order);

			if (n!=1) {
				String message="更新失败";
				return new ResultMessage(ResultMessage.ERROR,message);
			}else{

				String loginUserId=this.getLoginUserId();

				lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
						orderId,
						orderId,
						loginUserId,
						"将编号为["+orderId+"]的订单，更新支付等待时间",
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"更新支付等待时间",
						"原有支付等待时间："+DateUtil.formatDate(oldOrder.getWaitPaymentTime(), "yyyy-MM-dd HH:mm")+"，新的支付等待时间："+DateUtil.formatDate(newWaitPaymentTime, "yyyy-MM-dd HH:mm"));

				//发送等待支付时间更改消息
				orderMessageProducer.sendMsg(MessageFactory.newOrderWaitPaymentTimeChangeMessage(orderId));
				return ResultMessage.UPDATE_SUCCESS_RESULT;
			}


		}


		@RequestMapping(value = "/showUpdateWaitRetainageTime")
		public String showUpdateWaitRetainageTime(Model model, HttpServletRequest request,String stampId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<showUpdateWaitPaymentTime>");
			}

			model.addAttribute("stampId", stampId);


			return "/order/orderStatusManage/allCategory/showUpdateWaitRetainageTime";
		}

		@RequestMapping(value = "/updateWaitRetainageTime")
		@ResponseBody
		public Object updateWaitRetainageTime( HttpServletRequest request,OrdOrder order){

			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<updateWaitPaymentTime>");
			}
			Long orderId=order.getOrderId();
			try {
//		String addWaitPaymentTime=request.getParameter("addWaitPaymentTime");
				OrdOrder oldOrder=ordOrderUpdateService.queryOrdOrderByOrderId(orderId);
				if( "STAMP".equals(order.getOrderSubType())){
					getStampOrdOrder(order,HttpServletLocalThread.getModel());
				}
				List<OrdOrderItem> orderItems = orderUpdateService.queryOrderItemByOrderId(orderId);
				if(oldOrder != null){
					order.setOrderItemList(orderItems);
				}
				//Date newDate=DateUtils.addHours(oldOrder.getWaitPaymentTime(), Integer.parseInt(addWaitPaymentTime));

				String waitRetainageTime=request.getParameter("waitRetainageTime");
				Date newWaitRetainageTime=DateUtil.toDate(waitRetainageTime, "yyyy-MM-dd HH:mm");
				if (oldOrder.isCancel()) {
					String message="订单已经取消不可修改";
					return new ResultMessage(ResultMessage.ERROR,message);
				}


				/**
				 * 新增判断，如果修改过后的支付等待时间不能小于下单时间
				 * */
				if(newWaitRetainageTime != null && newWaitRetainageTime.before(oldOrder.getCreateTime())){
					String message=" 尾款支付等待时间不得早于下单时间!";
					return new ResultMessage(ResultMessage.ERROR,message);
				}


				/**
				 * 新增判断,修改后的尾款支付等待时间不能在当前时间之前
				 * */
				if(newWaitRetainageTime != null && newWaitRetainageTime.before(new Date())){
					String message=" 尾款支付等待时间不得晚于早于当前操作时间!";
					return new ResultMessage(ResultMessage.ERROR,message);
				}
				order.setWaitRetainageTime(newWaitRetainageTime);
				Map<String, String> map=new HashMap<String, String>();
				map.put("orderId", orderId.toString());
				map.put("lastPayTime", waitRetainageTime);
				LOG.info("------------------10------------------");
				boolean ResultFlag= Boolean.valueOf(HttpsUtil.requestPostFormResponse(Constant.getInstance().getPreSaleBaseUrl()+"/admin/stamp/updateOrdOrderLastPayTime", map).getResponseString());
				if(!ResultFlag){
					String message="更新失败";
					return new ResultMessage(ResultMessage.ERROR,message);
				}else{
					String loginUserId=this.getLoginUserId();
					lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
							orderId,
							orderId,
							loginUserId,
							"将编号为["+orderId+"]的订单，更新尾款支付等待时间",
							ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
							ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"更新尾款支付等待时间",
							"原有尾款支付等待时间："+DateUtil.formatDate(oldOrder.getWaitPaymentTime(), "yyyy-MM-dd HH:mm")+"，新的尾款支付等待时间："+DateUtil.formatDate(newWaitRetainageTime, "yyyy-MM-dd HH:mm"));


					return ResultMessage.UPDATE_SUCCESS_RESULT;
				}
			} catch (Exception e) {
				log.error(e.getMessage());
				String message="系统异常，请联系管理员！";
				return new ResultMessage(ResultMessage.ERROR,message);
			}

		}
		/**
		 * 进入上传附件页面
		 *
		 * @param model
		 * @return
		 * @throws BusinessException
		 */
		@RequestMapping("/showUploadNoticeRegiment")
		public String showUploadNoticeRegiment(Model model, HttpServletRequest request) throws BusinessException{






			return "/order/orderStatusManage/allCategory/uploadNoticeRegiment";
		}




		/**
		 * 上传出团通知书
		 * @param model
		 * @param orderAttachmentVO
		 * @throws BusinessException
		 */
		@RequestMapping("/addOrderNoticeRegiment")
		public void addOrderNoticeRegiment(Model model,OrderAttachmentVO orderAttachmentVO,HttpServletResponse response,HttpServletRequest req) throws BusinessException{
			//构造返回的json数据
			JSONObject jsonObject = new JSONObject();
			try {
				String[] orderIds = req.getParameter("orderIds").split(",");

				Long fileId = orderAttachmentVO.getFileId();
				String fileName = orderAttachmentVO.getFileName();
				String memo = orderAttachmentVO.getMemo();

				for (int i = 0; i < orderIds.length; i++) {

					Long orderId= Long.valueOf(orderIds[i].split("_")[0]);

					ordAdditionStatusService.saveNoticeRegiment(fileId, fileName, memo, orderId,this.getLoginUserId());

				}


				//构造json数据
				jsonObject.put("result", "success");
				JSONOutput.writeJSON(response, jsonObject);
			} catch (Exception e) {
				LOG.error("{}", e);
				jsonObject.put("result", "failure");
				JSONOutput.writeJSON(response, jsonObject);
			}
		}




		/**
		 * 发送出团通知书
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "/sendNoticeRegiment")
		@ResponseBody
		public Object sendNoticeRegiment( HttpServletRequest request){

			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<sendNoticeRegiment>");
			}
			String oneData=request.getParameter("oneData");
			String[] orderIds = request.getParameter("orderIds").split(",");
			String succMessage="发送出团通知书成功";



			int num=0;
			for (int i = 0; i < orderIds.length; i++) {

				String[] data=orderIds[i].split("_");
				Long orderId= Long.valueOf(data[0]);
				String email="";
				if (data.length>1) {
					email=data[1];
				}
				if (!StringUtils.isEmpty(email)) {

					ordAdditionStatusService.updateSendNoticeRegiment(orderId, email,this.getLoginUserId());

					num++;
				}

			}

			if ("false".equals(oneData)) {
				succMessage="出团通知书选中订单"+orderIds.length+"条,实际发送成功"+num+"条。（如果发送不成功，请检查联系人email是否存在）";
			}else{
				if (num==0) {
					return new ResultMessage("ERROR", "发送失败，请检查联系人email是否存在");
				}
			}
			return new ResultMessage("success", succMessage);

		}





		@RequestMapping(value = "/fileDownLoad")
		public void fileDownLoad(Long orderId, HttpServletRequest request, HttpServletResponse response) {
			OutputStream os = null;
			try {

				os = response.getOutputStream();

				Map<String,Object> param = new HashMap<String,Object>();
				param.put("orderId",orderId);
				param.put("attachmentType", ATTACHMENT_TYPE.NOTICE_REGIMENT.name());
				param.put("_orderby", "ORD_ATTACHMENT.CREATE_TIME");
				param.put("_order", "DESC");
				List<OrderAttachment>  orderAttachmentList=orderAttachmentService.findOrderAttachmentByCondition(param);

				OrderAttachment orderAttachment = null;
				if (orderAttachmentList==null || orderAttachmentList.isEmpty()) {
					os.write("还没有出团通知书".getBytes());
				}else{
					orderAttachment=orderAttachmentList.get(0);
					ComFile resultFile = vstFSClient.downloadFile(orderAttachment.getFileId());
					if(resultFile != null) {
						response.setHeader("Content-Disposition","attachment; filename="+ java.net.URLEncoder.encode(resultFile.getFileName(), "UTF-8"));

						byte[] data = resultFile.getFileData();
						if (resultFile != null && data != null) {
							os.write(data);
						}
					}
				}

				os.flush();
			} catch (IOException ex) {
				LOG.error("{}", ex);
			} finally {
				IOUtils.closeQuietly(os);
			}
		}





		/**
		 * 进入修改合同页面
		 *
		 * @param model
		 * @param orderId
		 * @return
		 * @throws BusinessException
		 */
		@RequestMapping("/showUpdateTravelContract")
		public String showUpdateTravelContract(Model model, HttpServletRequest request,Long orderId,Long ordContractId) throws BusinessException{

			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);


			OrdTravelContract ordTravelContract=ordTravelContractService.findOrdTravelContractById(ordContractId);

			List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
			list.add(ordTravelContract);
			order.setOrdTravelContractList(list);


			File directioryFile = ResourceUtil.getResourceFile(TRAVEL_ECONTRACT_DIRECTORY);
			if (ordTravelContract != null) {
				if (ELECTRONIC_CONTRACT_TEMPLATE.TEAM_OUTBOUND_TOURISM.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {

					Map<String,Object> rootMap=teamOutboundTourismContractService.captureContract(ordTravelContract,order, directioryFile);
					TravelContractVO travelContractVO = (TravelContractVO)rootMap.get("travelContractVO");

				/*合同快照数据替代原有数据部分*/
					//根据订单合同ID,从合同快照数据表中获取最新的记录
					Map<String, Object> params  = new HashMap<String, Object>();
					params.put("ordContractId", ordContractId);
					OrdContractSnapshotData OrdContractSnapshotData = orderContractSnapshotService.selectByParam(params);
					if(OrdContractSnapshotData != null){
						Long fileId = OrdContractSnapshotData.getJsonFileId();
						ComFile comFile = fsClient.downloadFile(fileId);
						String fileStr = null;
						try {
							fileStr = new String(comFile.getFileData(),"UTF-8");
						} catch (UnsupportedEncodingException e) {
							log.error("解析json文件出错："+e.getMessage());
						}
						OutboundTourContractDataVO outboundTourContractDataVO = com.alibaba.fastjson.JSONObject.parseObject(fileStr,OutboundTourContractDataVO.class);
						if(outboundTourContractDataVO != null){
							if(StringUtil.isNotEmptyString(outboundTourContractDataVO.getSupplementaryTerms())){
								travelContractVO.setSupplementaryTerms(outboundTourContractDataVO.getSupplementaryTerms());
							}
							if(outboundTourContractDataVO.getRecommendDetailList() != null){
								travelContractVO.setRecommendDetailList(outboundTourContractDataVO.getRecommendDetailList());
							}
							if(outboundTourContractDataVO.getShopingDetailList() != null){
								travelContractVO.setShopingDetailList(outboundTourContractDataVO.getShopingDetailList());
							}
						}

					}
					model.addAttribute("travelContractVO", travelContractVO);
					model.addAttribute("order", order);
					model.addAttribute("chidOrderMap", rootMap.get("chidOrderMap"));

					return "/order/econtractTemplate/teamOutboundTourismContractTemplate";

				} else if (ELECTRONIC_CONTRACT_TEMPLATE.DONGGANG_ZHEJIANG_CONTRACT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {
					Map<String, Object> rootMap = teamDonggangZhejiangContractService.captureContract(ordTravelContract, order,directioryFile);
					model.addAttribute("travelContractVO",rootMap.get("travelContractVO"));
					model.addAttribute("order", order);
					model.addAttribute("chidOrderMap", rootMap.get("chidOrderMap"));

					return "/order/econtractTemplate/DonggangZhejiangContractTemplate";
				} else if (ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {

					Map<String,Object> rootMap=teamWithInTerritoryContractService.captureContract(ordTravelContract,order, directioryFile);
					TravelContractVO travelContractVO = (TravelContractVO)rootMap.get("travelContractVO");

				/*合同快照数据替代原有数据部分*/
					//根据订单合同ID,从合同快照数据表中获取最新的记录
					Map<String, Object> params  = new HashMap<String, Object>();
					params.put("ordContractId", ordContractId);
					OrdContractSnapshotData OrdContractSnapshotData = orderContractSnapshotService.selectByParam(params);
					if(OrdContractSnapshotData != null){
						Long fileId = OrdContractSnapshotData.getJsonFileId();
						ComFile comFile = fsClient.downloadFile(fileId);
						String fileStr = null;
						try {
							fileStr = new String(comFile.getFileData(),"UTF-8");
						} catch (UnsupportedEncodingException e) {
							log.error("解析json文件出错："+e.getMessage());
						}
						TeamWithInTerritoryContractDataVO teamWithInTerritoryContractDataVO = com.alibaba.fastjson.JSONObject.parseObject(fileStr,TeamWithInTerritoryContractDataVO.class);
						if(teamWithInTerritoryContractDataVO != null){
							if(StringUtil.isNotEmptyString(teamWithInTerritoryContractDataVO.getSupplementaryTerms())){
								travelContractVO.setSupplementaryTerms(teamWithInTerritoryContractDataVO.getSupplementaryTerms());
							}
							if(teamWithInTerritoryContractDataVO.getRecommendDetailList() != null){
								travelContractVO.setRecommendDetailList(teamWithInTerritoryContractDataVO.getRecommendDetailList());
							}
							if(teamWithInTerritoryContractDataVO.getShopingDetailList() != null){
								travelContractVO.setShopingDetailList(teamWithInTerritoryContractDataVO.getShopingDetailList());
							}
						}

					}

					model.addAttribute("travelContractVO", travelContractVO);
					model.addAttribute("order", order);
					model.addAttribute("chidOrderMap", rootMap.get("chidOrderMap"));

					return "/order/econtractTemplate/teamWithInTerritoryContractTemplate";
				}else if (ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {

					Map<String,Object> rootMap=advanceProductAgreementContractService.captureContract(ordTravelContract,order, directioryFile);
					model.addAttribute("travelContractVO", rootMap.get("travelContractVO"));
					model.addAttribute("order", order);
					model.addAttribute("chidOrderMap", rootMap.get("chidOrderMap"));

					return "/order/econtractTemplate/advanceProductAgreementTemplate";
				}else if (ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {

					Map<String,Object> rootMap=commissionedServiceAgreementService.captureContract(ordTravelContract,order, directioryFile);
					model.addAttribute("travelContractVO", rootMap.get("travelContractVO"));
					model.addAttribute("order", order);
					model.addAttribute("chidOrderMap", rootMap.get("chidOrderMap"));

					return "/order/econtractTemplate/commissionedServiceAgreementTemplate";
				}else if (ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {

					Map<String,Object> rootMap=destCommissionedServiceAgreementService.captureContract(ordTravelContract,order, directioryFile);
					model.addAttribute("travelContractVO", rootMap.get("travelContractVO"));
					model.addAttribute("order", order);
					model.addAttribute("chidOrderMap", rootMap.get("chidOrderMap"));

					return "/order/econtractTemplate/destCommissionedServiceAgreementTemplate";
				}
				else if (ELECTRONIC_CONTRACT_TEMPLATE.BEIJING_DAY_TOUR.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {

					Map<String,Object> rootMap=beijingDayTourContractService.captureContract(ordTravelContract,order, directioryFile);
					model.addAttribute("travelContractVO", rootMap.get("travelContractVO"));
					model.addAttribute("order", order);
					model.addAttribute("chidOrderMap", rootMap.get("chidOrderMap"));

					return "/order/econtractTemplate/beijingDayTourContractTemplate";
				}else if(ELECTRONIC_CONTRACT_TEMPLATE.CRUISE_TOURISM_SHANGHAI.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {

					Map<String, Object> rootMap = cruiseTourismContractService.captureContract(ordTravelContract, order, directioryFile);
					TravelContractVO travelContractVO = (TravelContractVO)rootMap.get("travelContractVO");

				/*合同快照数据替代原有数据部分*/
					//根据订单合同ID,从合同快照数据表中获取最新的记录
					Map<String, Object> params  = new HashMap<String, Object>();
					params.put("ordContractId", ordContractId);
					OrdContractSnapshotData OrdContractSnapshotData = orderContractSnapshotService.selectByParam(params);
					if(OrdContractSnapshotData != null){
						Long fileId = OrdContractSnapshotData.getJsonFileId();
						ComFile comFile = fsClient.downloadFile(fileId);
						String fileStr = null;
						try {
							fileStr = new String(comFile.getFileData(),"UTF-8");
						} catch (UnsupportedEncodingException e) {
							log.error("解析json文件出错："+e.getMessage());
						}
						CruiseTourismContractDataVO cruiseTourismContractDataVO = com.alibaba.fastjson.JSONObject.parseObject(fileStr,CruiseTourismContractDataVO.class);
						if(cruiseTourismContractDataVO != null){
							if(StringUtil.isNotEmptyString(cruiseTourismContractDataVO.getProductName())){
								travelContractVO.setProductName(cruiseTourismContractDataVO.getProductName());
							}
							if(StringUtil.isNotEmptyString(cruiseTourismContractDataVO.getMinPersonCountOfGroup())){
								travelContractVO.setMinPersonCountOfGroup(cruiseTourismContractDataVO.getMinPersonCountOfGroup());
							}
							if(StringUtil.isNotEmptyString(cruiseTourismContractDataVO.getLineShipDesc())){
								travelContractVO.setLineShipDesc(cruiseTourismContractDataVO.getLineShipDesc());
							}
							if(StringUtil.isNotEmptyString(cruiseTourismContractDataVO.getDeparturePlace())){
								travelContractVO.setDeparturePlace(cruiseTourismContractDataVO.getDeparturePlace());
							}
							if(StringUtil.isNotEmptyString(cruiseTourismContractDataVO.getReturnPlace())){
								travelContractVO.setReturnPlace(cruiseTourismContractDataVO.getReturnPlace());
							}
							if(StringUtil.isNotEmptyString(cruiseTourismContractDataVO.getSupplementaryTerms())){
								travelContractVO.setSupplementaryTerms(cruiseTourismContractDataVO.getSupplementaryTerms());
							}
							if(cruiseTourismContractDataVO.getRecommendDetailList() != null){
								travelContractVO.setRecommendDetailList(cruiseTourismContractDataVO.getRecommendDetailList());
							}
							if(cruiseTourismContractDataVO.getShopingDetailList() != null){
								travelContractVO.setShopingDetailList(cruiseTourismContractDataVO.getShopingDetailList());
							}
						}

					}
					model.addAttribute("travelContractVO", travelContractVO);
					model.addAttribute("order", order);
					model.addAttribute("chidOrderMap", rootMap.get("chidOrderMap"));

					return "/order/econtractTemplate/cruiseTourismContractTemplate";
				}
			 /*else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.TRAVEL_ITINERARY.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {

				 Map<String,Object> rootMap=travelItineraryContractService.captureContract(ordTravelContract,order, directioryFile);
				model.addAttribute("travelContractVO", rootMap.get("travelContractVO"));
				model.addAttribute("order", order);
				model.addAttribute("chidOrderMap", rootMap.get("chidOrderMap"));

				return "/order/econtractTemplate/travelItineraryTemplate";
			}*/

			}
			return "/order/orderStatusManage/allCategory/lvmama_travelContractTemplate";
		}

		/**
		 * 查询订单合同列表
		 * @param model
		 * @param request
		 * @param orderId
		 * @return
		 * @throws BusinessException
		 */
		@RequestMapping("/showTravelContractList")
		public String showTravelContractDetail(Model model, HttpServletRequest request,Long orderId) throws BusinessException{

			List<OrdTravelContract> ordTravelContractList=findOrdTravelContract(orderId);
			List<OrdTravelContractVo> travelContractVoList = new ArrayList<OrdTravelContractVo>();
			//订单信息
			OrdOrder order = orderLocalService.queryOrdorderByOrderId(orderId);
			model.addAttribute("order", order);
			OrdPerson person = order.getContactPerson();
			if(person!=null){
				//联系人姓名
				String contacts = person.getFullName();
				//联系人邮箱
				String email= person.getEmail();
				model.addAttribute("contacts", contacts);
				model.addAttribute("email", email);
			}
			if(ordTravelContractList!=null){
				Map<Long,OrdOrderItem> orderItemMap= new HashMap<Long,OrdOrderItem>();
				for(OrdOrderItem orderItem:order.getOrderItemList()){
					orderItemMap.put(orderItem.getOrderItemId(), orderItem);
				}
				for(OrdTravelContract contract:ordTravelContractList){
					OrdTravelContractVo contractVo = new OrdTravelContractVo();
					BeanUtils.copyProperties(contract, contractVo);
					HashMap<String, Object> params = new HashMap<String, Object>();
					params.put("ordContractId", contract.getOrdContractId());
					List<OrdItemContractRelation> conRelList = iOrdItemContractRelationService.findOrdItemContractRelationList(params);
					//合同类型
					contractVo.setContractTemplateName(ELECTRONIC_CONTRACT_TEMPLATE.getCnName(contract.getContractTemplate()));
					//合同状态
					contractVo.setStatusName(ORDER_TRAVEL_CONTRACT_STATUS.getCnName(contract.getStatus()));
					//签约方式
					contractVo.setSigningTypeName(ORDER_CONTRACT_SIGNING_TYPE.getCnName(contract.getSigningType()));
					//与金棕榈的同步状态
					contractVo.setSyncStatusName(ORDER_TRAVEL_CONTRACT_SYNC_STATUS.getCnName(contract.getSyncStatus()));

					String attUrl = contract.getAttachementUrl();
					if(StringUtils.isNotEmpty(attUrl)){
						ComFileMap comFile= ordTravelContractService.getComFileMapByFileName(attUrl);
						if(comFile!=null&&comFile.getFileId()!=null){
							contractVo.setAttachementFileId(comFile.getFileId().toString());
						}
						else{
							contractVo.setAttachementFileId(null);
						}
					}
					contractVo.setOrderItemList(getOrderItemByContract(orderItemMap,conRelList));
					travelContractVoList.add(contractVo);
				}
			}

			model.addAttribute("travelContractVoList",travelContractVoList);

			return "/order/orderStatusManage/allCategory/showTravelContractList";

		}

		private List<OrdOrderItem> getOrderItemByContract(Map<Long,OrdOrderItem> orderItemMap,List<OrdItemContractRelation> conRelList){
			List<OrdOrderItem> orderItemList = new ArrayList<OrdOrderItem>();
			for(OrdItemContractRelation item:conRelList){
				if(orderItemMap.containsKey(item.getOrderItemId())){
					orderItemList.add(orderItemMap.get(item.getOrderItemId()));
				}
			}
			return orderItemList;
		}

		@RequestMapping(value = "/sendContractEmail")
		@ResponseBody
		public Object sendContractEmail(Long orderId,Long contractId){

//		ResultHandle resultHandle=new ResultHandle() ;
			//订单信息
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			OrdTravelContract tract = ordTravelContractService.findOrdTravelContractById(contractId);
			// 发送合同
			ResultHandle resultHandle = null;
			if(ELECTRONIC_CONTRACT_TEMPLATE.FINANCE_CONTRACT.name().equals(tract.getContractTemplate())){
				resultHandle=financeContractService.sendContractEmail(order,contractId,getLoginUserId());
			}else{
				resultHandle=teamOutboundTourismContractService.sendContractEmail(order,contractId,getLoginUserId());
			}

			if(StringUtils.isEmpty(resultHandle.getMsg())){
			/*try {
				orderTravelElectricContactService.insertOrderLog(orderId, contractId, getLoginUserId(), "发送合同至用户邮箱【"+order.getContactPerson().getEmail()+"】，合同名称:"+tract.getContractName(), "");
			} catch (Exception e) {
				LOG.error("插入日志异常",e);
			}*/

				//修改合同状态
				if(StringUtils.isEmpty(tract.getStatus())|| tract.getStatus().equals(ORDER_TRAVEL_CONTRACT_STATUS.UNSIGNED.getCode())){
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("orderId", orderId);
					if(PAYMENT_STATUS.PAYED.getCode().equals(order.getPaymentStatus())){
						params.put("status", ORDER_TRAVEL_CONTRACT_STATUS.EFFECT.getCode());
					}else{
						params.put("status", ORDER_TRAVEL_CONTRACT_STATUS.SIGNED_UNEFFECT.getCode());
					}
					ordTravelContractService.updateContractStatusByOrderId(params);
					try {
						orderTravelElectricContactService.insertOrderLog(orderId, contractId, getLoginUserId(), "修改订单【"+orderId+"】所有合同状态为:"+params.get("status"), "");
					} catch (Exception e) {
						LOG.error("插入日志异常",e);
					}
				}
				return new ResultMessage(ResultMessage.SUCCESS,"邮件发送成功");
			}
			else{
				return new ResultMessage(ResultMessage.ERROR,resultHandle.getMsg());
			}

		}


		@RequestMapping("/toChangeContractSignTypePage")
		public String toChangeContractSignTypePage(Long contractId){
			OrdTravelContract contract = ordTravelContractService.findOrdTravelContractById(contractId);
			HttpServletLocalThread.getModel().addAttribute("contract",contract);
			return "/order/orderStatusManage/allCategory/changeContractSignType";
		}

		@RequestMapping(value = "/changeContractSignType")
		@ResponseBody
		public Object changeContractSignType(Long contractId,HttpServletRequest request,String signType){
			try {
				OrdTravelContract contract = ordTravelContractService.findOrdTravelContractById(contractId);
				if(contract!=null&&StringUtils.isNotEmpty(signType)){
					contract.setSigningType(signType);
					ordTravelContractService.updateByPrimaryKeySelective(contract);
					return ResultMessage.UPDATE_SUCCESS_RESULT;
				}
				String message="参数不正确或合同不存在";
				return new ResultMessage(ResultMessage.ERROR,message);

			} catch (Exception e) {
				String message="操作异常:"+e.getMessage();
				return new ResultMessage(ResultMessage.ERROR,message);
			}

		}



		@RequestMapping(value = "/updateTravelContract")
		@ResponseBody
		public Object updateTravelContract( HttpServletRequest request,TravelContractVO travelContractVO,Long orderId,Long ordContractId) {

			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<updateTravelContract>");
			}
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);

			OrdTravelContract ordTravelContract=ordTravelContractService.findOrdTravelContractById(ordContractId);

			if (order.getOrderStatus().equals(ORDER_STATUS.CANCEL.name())) {
				//name=OrderEnum.ORDER_STATUS.CANCEL.getCnName(oldOrder.getOrderStatus());
				return new ResultMessage(ResultMessage.ERROR,"订单已经取消不可修改合同");
			}

			ResultHandle resultHandle=new ResultHandle() ;
			String operatorName=this.getLoginUserId();

			if (ELECTRONIC_CONTRACT_TEMPLATE.TEAM_OUTBOUND_TOURISM.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {

				resultHandle = this.teamOutboundTourismContractService.updateTravelContact(travelContractVO, order, ordTravelContract, operatorName);

			/*合同快照部分*/
				//1.获取合同填充的数据和该合同对应的行程单的数据
				OutboundTourContractDataVO outboundTourContractDataVO = new OutboundTourContractDataVO();
				outboundTourContractDataVO.setSupplementaryTerms(travelContractVO.getSupplementaryTerms());
				outboundTourContractDataVO.setRecommendDetailList(travelContractVO.getRecommendDetailList());
				outboundTourContractDataVO.setShopingDetailList(travelContractVO.getShopingDetailList());

				//2.根据组装的数据dataVO转化为json,并上传到文件服务器，并返回保存的文件ID
				uploadFileAndCreateContractSnapshotDate(orderId, ordTravelContract, operatorName,outboundTourContractDataVO,resultHandle);

			}else if (ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {

				resultHandle = this.teamWithInTerritoryContractService.updateTravelContact(travelContractVO, order, ordTravelContract, operatorName);

			/*合同快照部分*/
				//1.获取合同填充的数据和该合同对应的行程单的数据
				TeamWithInTerritoryContractDataVO teamWithInTerritoryContractDataVO = new TeamWithInTerritoryContractDataVO();
				teamWithInTerritoryContractDataVO.setSupplementaryTerms(travelContractVO.getSupplementaryTerms());
				teamWithInTerritoryContractDataVO.setRecommendDetailList(travelContractVO.getRecommendDetailList());
				teamWithInTerritoryContractDataVO.setShopingDetailList(travelContractVO.getShopingDetailList());

				//2.根据组装的数据dataVO转化为json,并上传到文件服务器，并返回保存的文件ID
				uploadFileAndCreateContractSnapshotDate(orderId, ordTravelContract, operatorName,teamWithInTerritoryContractDataVO,resultHandle);

			}else if (ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {

				resultHandle = this.advanceProductAgreementContractService.updateTravelContact(travelContractVO, order, ordTravelContract, operatorName);
			}else if (ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {

				resultHandle=commissionedServiceAgreementService.updateTravelContact(travelContractVO, order, ordTravelContract, operatorName);
			}else if (ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {

				resultHandle=destCommissionedServiceAgreementService.updateTravelContact(travelContractVO, order, ordTravelContract, operatorName);
			}
			else if (ELECTRONIC_CONTRACT_TEMPLATE.BEIJING_DAY_TOUR.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {

				resultHandle=beijingDayTourContractService.saveTravelContact(ordTravelContract, operatorName);
			}else if(ELECTRONIC_CONTRACT_TEMPLATE.TAIWAN_AGREEMENT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())){
				resultHandle = taiwanTravelContractService.saveTravelContact(ordTravelContract, operatorName);

			}else if(ELECTRONIC_CONTRACT_TEMPLATE.DONGGANG_ZHEJIANG_CONTRACT.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())){
				resultHandle = this.teamDonggangZhejiangContractService.updateTravelContact(travelContractVO, order, ordTravelContract, operatorName);
			}else if(ELECTRONIC_CONTRACT_TEMPLATE.CRUISE_TOURISM_SHANGHAI.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())){
				resultHandle = this.cruiseTourismContractService.updateTravelContact(travelContractVO, order, ordTravelContract, operatorName);

			/*合同快照部分*/
				//1.获取合同填充的数据和该合同对应的行程单的数据
				CruiseTourismContractDataVO cruiseTourismContractDataVO = new CruiseTourismContractDataVO();
				cruiseTourismContractDataVO.setProductName(travelContractVO.getProductName());
				cruiseTourismContractDataVO.setLineShipDesc(travelContractVO.getLineShipDesc());
				cruiseTourismContractDataVO.setMinPersonCountOfGroup(travelContractVO.getMinPersonCountOfGroup());
				cruiseTourismContractDataVO.setDeparturePlace(travelContractVO.getDeparturePlace());
				cruiseTourismContractDataVO.setReturnPlace(travelContractVO.getReturnPlace());
				cruiseTourismContractDataVO.setSupplementaryTerms(travelContractVO.getSupplementaryTerms());

				//2.根据组装的数据dataVO转化为json,并上传到文件服务器，并返回保存的文件ID
				uploadFileAndCreateContractSnapshotDate(orderId, ordTravelContract, operatorName,cruiseTourismContractDataVO,resultHandle);
			}

		/*else if (CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE.TRAVEL_ITINERARY.name().equalsIgnoreCase(ordTravelContract.getContractTemplate())) {

			resultHandle=travelItineraryContractService.saveTravelContact(ordTravelContract, operatorName);
		}*/
			if(ELECTRONIC_CONTRACT_TEMPLATE.FINANCE_CONTRACT.name().equals(ordTravelContract.getContractTemplate())){
				resultHandle=financeContractService.saveTravelContact(ordTravelContract,getLoginUserId());
			}else{
				if (resultHandle.isSuccess()) {

					resultHandle=teamOutboundTourismContractService.sendOrderEcontractEmail(order,this.getLoginUserId());
				}
			}
			

			if (resultHandle.isFail()) {
				LOG.info("orderId is"+order.getOrderId()+"error info is:"+resultHandle.getMsg());
				String message="更新失败";

				return new ResultMessage(ResultMessage.ERROR,message);
			}

			return ResultMessage.UPDATE_SUCCESS_RESULT;

		}


		private <T> ResultHandle uploadFileAndCreateContractSnapshotDate(Long orderId,OrdTravelContract ordTravelContract,
				String operatorName,T dataVO,ResultHandle resultHandle) {
			Long jsonfileId = null;
			try {
				String str = com.alibaba.fastjson.JSONObject.toJSONString(dataVO);
				byte[] _fileBytes = str.getBytes("UTF-8");
				ByteArrayInputStream bytesInputStream = new ByteArrayInputStream(_fileBytes);
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

			return null;
		}



		private String buildVisitTime(OrdOrderItem orderItem) {

			if(null != orderItem){
				if (orderItem.hasTicketAperiodic()) {
					//取通关时间
					List<OrdTicketPerform> resultList = complexQueryService.selectByOrderItem(orderItem.getOrderItemId());
					if(CollectionUtils.isNotEmpty(resultList)){
						Date performTime = resultList.get(0).getPerformTime();
						if(performTime != null){
							return DateUtil.SimpleFormatDateToString(performTime);
						}
					}
					String visitTimeStr = (String) orderItem.getContentMap().get(ORDER_TICKET_TYPE.goodsExpInfo.name());
                /*if(orderItem.getValidBeginTime() != null){
                    visitTimeStr=DateUtil.formatDate(orderItem.getValidBeginTime(), "yyyy-MM-dd");
                }else{
                    visitTimeStr = "";
                }
                visitTimeStr += "</br>";
				if(orderItem.getValidEndTime() != null){
                    visitTimeStr += DateUtil.formatDate(orderItem.getValidEndTime(), "yyyy-MM-dd");
                }*/

					//期票不可游玩日期描述
					String unvalidDesc =  (String) orderItem.getContentMap().get(ORDER_TICKET_TYPE.aperiodic_unvalid_desc.name());
					if(StringUtil.isNotEmptyString(unvalidDesc)){
						visitTimeStr +="</br>(不适用日期:"+unvalidDesc+")";
					}
					return visitTimeStr;
				}else{
					StringBuffer visitTime = new StringBuffer();
					visitTime.append(DateUtil.formatDate(orderItem.getVisitTime(), "yyyy-MM-dd"));

					//马戏票场次信息展示
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

		/** 
		 * @Title: buildVisitTimeByDay 
		 * @Description: 长隆多园多日组合套餐票有效期以及游玩日期设置
		 * @param orderMonitorRst
		 * @param orderItem
		 * @return: void
		 */
		private void buildVisitTimeByValidDay(OrderMonitorRst orderMonitorRst,OrdOrderItem orderItem) {
			//判断组合套餐票
			if(orderItem.getCategoryId().longValue() == BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().longValue()) {
				//长隆多园多日标识
				String parkDaysFlag = this.getParkDaysFlag(orderItem.getOrderId());
				if("Y".equalsIgnoreCase(parkDaysFlag)) {
					//有效期
					String certValidDayStr = orderItem.getContentStringByKey("cert_valid_day");
					if(StringUtil.isNotEmptyString(certValidDayStr)) {
						try {
							//游玩时间
							Date visitTime = orderItem.getVisitTime();
							String visitTimeStr = DateUtil.SimpleFormatDateToString(visitTime);
							int certValidDay = Integer.valueOf(certValidDayStr);
							if(certValidDay > 1) {
								//有效期大于一天，游玩时间显示为时间段
								Date visitTimeEnd = DateUtil.addDays(visitTime, certValidDay-1);
								String visitTimeEndStr = DateUtil.SimpleFormatDateToString(visitTimeEnd);
								//游玩时间
								orderMonitorRst.setVisitTime(visitTimeStr + " ~ " + visitTimeEndStr);
								//有效期
								orderMonitorRst.setCertValidDay(certValidDay);
							}else if(certValidDay == 1){
								//游玩时间
								orderMonitorRst.setVisitTime(visitTimeStr);
								//有效期
								orderMonitorRst.setCertValidDay(certValidDay);
							}
						} catch (Exception e) {
							LOG.error("多园多日有效期及游玩时间设置失败",e);
						}
					}
					LOG.info("多园多日有效期及游玩时间设置成功");
				}
			}
		}
		
		//查询多园多日标识
		private String getParkDaysFlag(Long orderId) {
			//增加多园多日判断
			OrderTagVo tagVo = new OrderTagVo();
			tagVo.setObjectId(orderId);
			tagVo.setObjectType(TagEnum.ORD_OBJECT_TAG.ORD_ORDER.name());
			tagVo.setTagType(TagEnum.ORD_ORDER_TAG.PARK_DAYS_FLAG.name());
			com.lvmama.order.api.base.vo.RequestBody<OrderTagVo> requestBD = new com.lvmama.order.api.base.vo.RequestBody<OrderTagVo>(tagVo);
			com.lvmama.order.api.base.vo.ResponseBody<List<OrderTagVo>> result = apiOrderTagService.queryOrderTags(requestBD);
		    if(result.isSuccess()){
			      List<OrderTagVo> tagList = result.getT();
			      if(tagList != null && tagList.size() > 0){
				       OrderTagVo tag = tagList.get(0);
				       return tag.getTagValue();
			      }
		    }
		    return null;
		}
		
		private String buildProductName(OrdOrderItem ordOrderItem) {
			String productName = "未知产品名称";
			if (null != ordOrderItem) {

				Map<String,Object> contentMap = ordOrderItem.getContentMap();

				String branchName =  (String) contentMap.get(ORDER_COMMON_TYPE.branchName.name());
				if(branchName != null && !"".equals(branchName)){
					// 如果是交通接驳，包含商品这一列显示  产品 + 商品
					if(BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(ordOrderItem.getCategoryId())){
						productName = ordOrderItem.getProductName()+"-"+ordOrderItem.getSuppGoodsName();
					}else{
						productName = ordOrderItem.getProductName()+"-"+branchName+"("+ordOrderItem.getSuppGoodsName()+")";
					}
				}else{
					productName = ordOrderItem.getProductName()+"-"+ordOrderItem.getSuppGoodsName();
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

		/**
		 * 处理订单的当前状态
		 *
		 * @param ordOrderItem
		 * @return
		 */
		private String buildCurrentStatus(OrdOrder order,OrdOrderItem ordOrderItem) {
			StringBuilder builder = new StringBuilder();
			OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(order.getOrderId());

		/*若子单为意外险游玩人后置子单，当意外险弃保时，子单状态显示已取消*/
			String travDelayFlag = null;
			String travDelayStatus = null;

			if (null != ordAccInsDelayInfo) {
				travDelayFlag = ordAccInsDelayInfo.getTravDelayFlag();
				travDelayStatus = ordAccInsDelayInfo.getTravDelayStatus();
			}
			//TODO:remove log
        /*String t = "@@@@@@@@@@@@@@@@@@@@@@@@@@@";
        String tag = t + "\n" + t + "\n" + t + "\n" + t + "\n" + t + "\n" + t + "\n" + t;
        LOG.info(tag);
        LOG.info("orderId = " + order.getOrderId());
        LOG.info("ordOrderItem = " + ordOrderItem.getOrderItemId());
        LOG.info("travDelayFlag = " + travDelayFlag);
        LOG.info("travDelayStatus = " + travDelayStatus);*/

			if (StringUtils.isNotBlank(travDelayFlag) && "Y".equalsIgnoreCase(travDelayFlag)
					&& StringUtils.isNotBlank(travDelayStatus)
					&& travDelayStatus.equalsIgnoreCase(ORDER_TRAV_DELAY_STATUS.ABANDON.name())) {

				Map<String, Object> contentMap = ordOrderItem.getContentMap();

           /* LOG.info("contentMap = " + contentMap);

            Object destBuAccFlag1 = contentMap.get("destBuAccFlag");
            LOG.info("destBuAccFlag = " + destBuAccFlag);
            LOG.info("destBuAccFlag1 = " + destBuAccFlag1);*/

				Object destBuAccFlag = ordOrderItem.getContentValueByKey("destBuAccFlag");
				if (null != destBuAccFlag && StringUtils.equalsIgnoreCase(destBuAccFlag.toString(), "Y")){
					return "已取消";
				}
			}

			if (order.hasCanceled()) {
				return "废单";
			}
			
			//查询子单取消状态
            	    	OrdItemCancelVo ordItemCancelVo = new OrdItemCancelVo();
                        com.lvmama.order.api.base.vo.RequestBody<OrdItemCancelVo> request = new com.lvmama.order.api.base.vo.RequestBody<OrdItemCancelVo>();
                        ordItemCancelVo.setOrderItemId(ordOrderItem.getOrderItemId());
                        request.setT(ordItemCancelVo);
                        com.lvmama.order.api.base.vo.ResponseBody<OrdItemCancelVo> responseBody = apiOrderItemCancelService.selectOrderCancel(request);
                        if (null != responseBody && null != responseBody.getT()) {
                    	OrdItemCancelVo ordVo = responseBody.getT();
                            String ordItemTicketStatus = ordVo.getCancelStatus();
                            if (ordItemTicketStatus != null) {
                                builder.append(ItemCancelEnum.ORDER_CANCEL_CODE.getCnName(ordItemTicketStatus));
                            } else {
                                builder.append("正常");
                            }
                        } else {
                            builder.append("正常");
                        }
                        builder.append("<br>");

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

		/*
		//组装凭证确认状态
		if(OrderEnum.CERT_CONFIRM_STATUS.UNCONFIRMED.name().equals(ordOrderItem.getCertConfirmStatus())){
			builder.append("未确认");
		}else if(OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.name().equals(ordOrderItem.getCertConfirmStatus())){
			builder.append("已确认");
		}else{
			builder.append("未确认");
		}

		builder.append("<br>");
		*/

			//门票业务类订单使用状态
			List<OrdTicketPerform> resultList = null;
			//订单使用状态
//		List<String> perFormStatusList = new ArrayList<String>();
			//门票业务类订单使用状态
			Map<String,Object> performMap = ordOrderItem.getContentMap();
			String categoryCode =  (String) performMap.get(ORDER_COMMON_TYPE.categoryCode.name());
			if (ProductCategoryUtil.isTicket(categoryCode)) {
				builder.append("使用状态（");
				resultList = complexQueryService.selectByOrderItem(ordOrderItem.getOrderItemId());
				String performStatusName= PERFORM_STATUS_TYPE.getCnName(OrderUtils.calPerformStatus(resultList,order,ordOrderItem)) ;
//			perFormStatusList.add(performStatusName);
				//只有ebk订单才需要显示未使用份数
			/*具体未使用数量挪至子订单页面显示
			if ("部分使用".equals(performStatusName)
					&& resultList != null
					&& resultList.size() > 0 && ordOrderItem.getAdultQuantity() + ordOrderItem
					.getChildQuantity() > 0) {
				OrdTicketPerform ordTicketPerform = resultList.get(0);

//				performStatusName += ", 未使用："
//						+ (((ordOrderItem.getAdultQuantity() + ordOrderItem
//								.getChildQuantity()) * (ordOrderItem
//								.getQuantity() == null ? 0 : ordOrderItem
//								.getQuantity())) - ((ordTicketPerform
//								.getActualAdult() == null ? 0
//								: ordTicketPerform.getActualAdult()) + (ordTicketPerform
//								.getActualChild() == null ? 0
//								: ordTicketPerform.getActualChild())));
				performStatusName += ", 未使用："
						+ ((ordOrderItem.getQuantity() == null ? 0
								: ordOrderItem.getQuantity()) - ((ordTicketPerform
								.getActualAdult() == null ? 0
								: ordTicketPerform.getActualAdult()) + (ordTicketPerform
								.getActualChild() == null ? 0
								: ordTicketPerform.getActualChild()))
								/ (ordOrderItem.getAdultQuantity() + ordOrderItem
										.getChildQuantity()));
			}*/
				//门票业务类订单使用状态
				builder.append(performStatusName);
				builder.append("）");
			}
			//酒店业务类订单使用状态
			if (ProductCategoryUtil.isHotel(categoryCode)) {
				builder.append("使用状态（");
				String performStatusName= PERFORM_STATUS_TYPE.getCnName(ordOrderItem.getPerformStatus()) ;
				//perFormStatusList.add(performStatusName);

				//酒店业务类订单使用状态
				builder.append(performStatusName);
				builder.append("）");
			}
			builder.append("<br>");
        		//根据子单ID  子订单出票状态查询  ord_item_ticket
        	        OrdItemTicketVo ordItemTicketOv = new OrdItemTicketVo();
        	        com.lvmama.order.api.base.vo.RequestBody<OrdItemTicketVo> request2 = new com.lvmama.order.api.base.vo.RequestBody<OrdItemTicketVo>();
        	        ordItemTicketOv.setOrderId(ordOrderItem.getOrderId());
        	        ordItemTicketOv.setOrderItemId(ordOrderItem.getOrderItemId());
        	        request2.setT(ordItemTicketOv);
        	        com.lvmama.order.api.base.vo.ResponseBody<OrdItemTicketVo> responseBody2 = apiOrderItemCancelService.selectOrdItemTicketNum(request2);
        	        if (null != responseBody2 && null != responseBody2.getT()) {
        	            OrdItemTicketVo ordItemTicket = responseBody2.getT();
        		        if (ordItemTicket != null && 
        		                ordItemTicket.getActualCount() != null && 
        		                ordItemTicket.getTicketCount() != null) {
        		            builder.append("出票状态（");
        		            if (ordItemTicket.getActualCount() == ordItemTicket.getTicketCount()) {
        		                builder.append(ItemCancelEnum.ORDER_CANCEL_CODE.ALREADY.getCnName());
        		            } else if (ordItemTicket.getTicketCount() != Long.getLong(ItemCancelEnum.ORDER_CANCEL_CODE.WITHOUT.getCode()) &&
        		                    ordItemTicket.getTicketCount() < ordItemTicket.getActualCount()) {
        		                builder.append(ItemCancelEnum.ORDER_CANCEL_CODE.PART.getCnName());
        		            }
        		            builder.append("）<br>");
        		        }
        	        }

			//如果子订单是景点门票，其他票，组合套餐票，长隆多园多日票，且订单未使用计算是否过期
			if (BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(order.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(order.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(order.getCategoryId())) {
        	        	if (PERFORM_STATUS_TYPE.UNPERFORM.name().equalsIgnoreCase(OrderUtils.calPerformStatus(resultList,order,ordOrderItem))) {
							LOG.info("###orderId=" + order.getOrderId() + "查询子订单快照-商品有效期信息:ordOrderItemId" + ordOrderItem.getOrderItemId());
							SnapshotOrderItemVo snapshotOrderItemVo = null;
							try {
								com.lvmama.order.api.base.vo.ResponseBody<SnapshotOrderItemVo> snapshotOrderItemResponse =
										apiOrderSnapshotService.findSnapshotOrderItem(new com.lvmama.order.api.base.vo.RequestBody<Long>(ordOrderItem.getOrderItemId()));
								if (snapshotOrderItemResponse != null && snapshotOrderItemResponse.isSuccess()) {
									snapshotOrderItemVo = snapshotOrderItemResponse.getT();
									if (null != snapshotOrderItemVo) {
										String useType = snapshotOrderItemVo.getUseType();
										if (StringUtil.isEmptyString(useType)) {
											useType = "VISIT_TIME";//默认从游玩日起x天有效
										}
										Date now = DateUtil.getTodayDate();
										if ("VISIT_TIME".equalsIgnoreCase(useType)) {//按游玩日期使用
											Date visitDate = ordOrderItem.getVisitTime();
											Short days = snapshotOrderItemVo.getDays();
											if (null != visitDate && null != days) {
												Date limitDate = CalendarUtils.addDates(visitDate, days.intValue() - 1);
												if (now.after(DateUtil.getDayEnd(limitDate))) {
													builder.append("已过期");
												}
											}
										} else if ("ORDER_TIME".equalsIgnoreCase(useType)) {//按下单日期使用
											Date createDate = ordOrderItem.getCreateTime();
											Short days = snapshotOrderItemVo.getDays();
											if (null != createDate && null != days) {
												Date limitDate = CalendarUtils.addDates(createDate, days.intValue() - 1);
												if (now.after(DateUtil.getDayEnd(limitDate))) {
													builder.append("已过期");
												}
											}
										} else if ("PERIOD_TIME".equalsIgnoreCase(useType)) {//按时间段使用
											//Date startTime=suppGoodsExpVo.getStartTime();
											Date endTime = snapshotOrderItemVo.getEndTime();
											if (null != endTime && now.after(DateUtil.getDayEnd(endTime))) {
												builder.append("已过期");
											}
										}
									} else {
										LOG.warn("查询订快照，商品有效期信息为空,orderItemId=" + ordOrderItem.getOrderItemId());
									}
								} else {
									LOG.error("查询快照异常,orderItemId=" + ordOrderItem.getOrderItemId() + ",异常信息:" + snapshotOrderItemResponse.getMessage());
								}
							} catch (Exception e) {
								LOG.error("###orderItemId=" + ordOrderItem.getOrderItemId() + "查询快照信息异常", e);
							}
						}
			}
			return builder.toString();
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
			param.put("objectType", AUDIT_OBJECT_TYPE.ORDER.toString());
			param.put("auditType", AUDIT_TYPE.PAYMENT_AUDIT.getCode());
			param.put("auditStatus", AUDIT_STATUS.PROCESSED.getCode() );
			List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);

			boolean paymentAuditProcessed=comAuditList.size()>0?true:false;
		/*if ( orderStatusNormal && !paymentStatusPayed && !paymentAuditProcessed) {
			isDonePaymentAudit=true;
		}*/

			return paymentAuditProcessed;
		}

		/**
		 *
		 *
		 *
		 * 检验是否已经做过小驴分期催支付活动
		 * @param order
		 * @return
		 */
		private boolean isDoneTimePaymentAudit(OrdOrder order) {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("objectId", order.getOrderId());
			param.put("objectType", AUDIT_OBJECT_TYPE.ORDER.toString());
			param.put("auditType", AUDIT_TYPE.TIME_PAYMENT_AUDIT.getCode());
			param.put("auditStatus", AUDIT_STATUS.PROCESSED.getCode() );
			List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);
			boolean timePaymentAuditProcessed=comAuditList.size()>0?true:false;
			return timePaymentAuditProcessed;
		}

		/**
		 *
		 *
		 *
		 * 检验是否已经做过通知出团活动
		 * @param order
		 * @return
		 */
		private boolean isDoneNoticeRegimentAudit(OrdOrder order) {
//		boolean orderStatusNormal=OrderEnum.ORDER_STATUS.NORMAL.getCode().equals(order.getOrderStatus());
//		boolean paymentStatusPayed=OrderEnum.PAYMENT_STATUS.PAYED.equals(order.getPaymentStatus());
//
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("objectId", order.getOrderId());
			param.put("objectType", AUDIT_OBJECT_TYPE.ORDER.toString());
			param.put("auditType", AUDIT_TYPE.NOTICE_AUDIT.getCode());
			param.put("auditStatus", AUDIT_STATUS.PROCESSED.getCode() );
			List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);

			boolean noticeRegimentAuditProcessed=comAuditList.size()>0?true:false;
		/*if ( orderStatusNormal && !paymentStatusPayed && !paymentAuditProcessed) {
			isDonePaymentAudit=true;
		}*/

			return noticeRegimentAuditProcessed;
		}


		/**
		 *
		 *
		 *
		 * 检验是否已经做过信息预审活动
		 * @param order
		 * @return
		 */
		private boolean isDonePretrialAudit(OrdOrder order) {
//		boolean orderStatusNormal=OrderEnum.ORDER_STATUS.NORMAL.getCode().equals(order.getOrderStatus());
//		boolean paymentStatusPayed=OrderEnum.PAYMENT_STATUS.PAYED.equals(order.getPaymentStatus());
//
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("objectId", order.getOrderId());
			param.put("objectType", AUDIT_OBJECT_TYPE.ORDER.toString());
			param.put("auditType", AUDIT_TYPE.PRETRIAL_AUDIT.getCode());
			param.put("auditStatus", AUDIT_STATUS.PROCESSED.getCode() );
			List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);

			boolean pretrialAuditProcessed=comAuditList.size()>0?true:false;
		/*if ( orderStatusNormal && !paymentStatusPayed && !paymentAuditProcessed) {
			isDonePaymentAudit=true;
		}*/

			return pretrialAuditProcessed;
		}

		/**
		 *
		 *
		 *
		 * 查出当前订单有未处理的活动情况
		 * @param order
		 * @return
		 */
		private Map<String, Boolean> getUnprocessedAudit(OrdOrder order) {
//		boolean orderStatusNormal=OrderEnum.ORDER_STATUS.NORMAL.getCode().equals(order.getOrderStatus());
//		boolean paymentStatusPayed=OrderEnum.PAYMENT_STATUS.PAYED.equals(order.getPaymentStatus());
//		OrderEnum.AUDIT_STATUS.POOL.getCode() ,
			String[]  auditStatusArray=new String[]{AUDIT_STATUS.UNPROCESSED.getCode() };

			Map<String, Object> param = new HashMap<String, Object>();
			param.put("objectId", order.getOrderId());
			param.put("objectType", AUDIT_OBJECT_TYPE.ORDER.toString());
			param.put("auditStatusArray",auditStatusArray);
			//param.put("auditType", auditType);
			List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);
			Map<String, Boolean> map=new HashMap<String, Boolean>();
			for (ComAudit comAudit : comAuditList) {

				if (AUDIT_TYPE.PAYMENT_AUDIT.getCode().equals(comAudit.getAuditType())) {
					map.put("PAYMENT_AUDIT", true);
				}else if (AUDIT_TYPE.TIME_PAYMENT_AUDIT.getCode().equals(comAudit.getAuditType())) {
					map.put("TIME_PAYMENT_AUDIT", true);
				}else if (AUDIT_TYPE.CANCEL_AUDIT.getCode().equals(comAudit.getAuditType())) {
					map.put("CANCEL_AUDIT", true);
				}else if (AUDIT_TYPE.ONLINE_REFUND_AUDIT.getCode().equals(comAudit.getAuditType())) {
					map.put("ONLINE_REFUND_AUDIT", true);
				}else if (AUDIT_TYPE.NOTICE_AUDIT.getCode().equals(comAudit.getAuditType())) {
					map.put("NOTICE_REGIMENT_AUDIT", true);
				}else if (AUDIT_TYPE.PRETRIAL_AUDIT.getCode().equals(comAudit.getAuditType())) {
					map.put("PRETRIAL_AUDIT", true);
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
		 * 查出当前子订单有未处理的活动情况
		 * @param order
		 * @return
		 */
		private Map<String, Boolean> getOrderItemUnprocessedAudit(OrdOrderItem orderItem) {
//		boolean orderStatusNormal=OrderEnum.ORDER_STATUS.NORMAL.getCode().equals(order.getOrderStatus());
//		boolean paymentStatusPayed=OrderEnum.PAYMENT_STATUS.PAYED.equals(order.getPaymentStatus());
//		OrderEnum.AUDIT_STATUS.POOL.getCode() ,
			String[]  auditStatusArray=new String[]{AUDIT_STATUS.UNPROCESSED.getCode() };

			Map<String, Object> param = new HashMap<String, Object>();
			param.put("objectId", orderItem.getOrderItemId());
			param.put("objectType", AUDIT_OBJECT_TYPE.ORDER_ITEM.toString());
			param.put("auditStatusArray",auditStatusArray);
			//param.put("auditType", auditType);
			List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);
			Map<String, Boolean> map=new HashMap<String, Boolean>();
			for (ComAudit comAudit : comAuditList) {

				if (AUDIT_TYPE.INFO_AUDIT.getCode().equals(comAudit.getAuditType())) {
					map.put("INFO_AUDIT", true);
				}else if (AUDIT_TYPE.RESOURCE_AUDIT.getCode().equals(comAudit.getAuditType())) {
					map.put("RESOURCE_AUDIT", true);
				}else if (AUDIT_TYPE.CERTIFICATE_AUDIT.getCode().equals(comAudit.getAuditType())) {
					map.put("CERTIFICATE_AUDIT", true);
				}else if (AUDIT_TYPE.CANCEL_AUDIT.getCode().equals(comAudit.getAuditType())) {
					map.put("CANCEL_AUDIT", true);
				}else if ("CANCEL_CONFIRM_AUDIT".equals(comAudit.getAuditType())) {
					map.put("CANCEL_CONFIRM_AUDIT", true);
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
		 * 检验是否已经做订单取消确认活动
		 * @param order
		 * @return
		 */
		private boolean isDoneCancleConfirmedAudit(OrdOrder order) {
//		boolean orderStatusNormal=OrderEnum.ORDER_STATUS.NORMAL.getCode().equals(order.getOrderStatus());
//		boolean paymentStatusPayed=OrderEnum.PAYMENT_STATUS.PAYED.equals(order.getPaymentStatus());
//
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("objectId", order.getOrderId());
			param.put("objectType", AUDIT_OBJECT_TYPE.ORDER.toString());
			param.put("auditType", AUDIT_TYPE.CANCEL_AUDIT.getCode());
			param.put("auditStatus", AUDIT_STATUS.PROCESSED.getCode() );
			List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);

			boolean cancleConfirmedProcessed=comAuditList.size()>0?true:false;
		/*if ( orderStatusNormal && !paymentStatusPayed && !paymentAuditProcessed) {
			isDonePaymentAudit=true;
		}*/

			return cancleConfirmedProcessed;
		}


		/**
		 * 是否显示定金审核
		 */
		private boolean showLosses(OrdOrder order , OrdOrderDownpay orderDownpay) {
		/*
		1.订单支付方式为定金支付。
		2.品类为出境线路和邮轮订单。
		3.支付状态为部分支付。
		4.订单定金已支付。
		5.订单取消状态。
		 */

			if(orderDownpay == null){
				return false;
			}


			//定金支付
			if(PAY_TYPE.FULL.toString().equals(orderDownpay.getPayType())){
				return false;
			}

			//出境
			if (!Constant.BU_NAME.OUTBOUND_BU.toString().equals(order.getBuCode())) {
				return false;
			}

			// 线路
			if (!(BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId())
			)) {
				return false;
			}

			//订单部分支付
			if (!PAYMENT_STATUS.PART_PAY.toString().equals(order.getPaymentStatus())) {
				return false;
			}

			//定金已支付
			if (!PAYMENT_STATUS.PAYED.toString().equals(orderDownpay.getPayStatus())) {
				return false;
			}

			//订单已取消
			if (!ORDER_STATUS.CANCEL.toString().equals(order.getOrderStatus())) {
				return false;
			}

			return true;
		}

		/**
		 * 是否显示申请资金转移
		 */
		private boolean showTransfer(boolean showLosses, OrdOrder order) {
		/*
		1.订单支付方式为定金支付。
		2.品类为出境线路和邮轮订单。
		3.支付状态为部分支付。
		4.订单定金已支付。
		5.订单取消状态。
	 	*/

			if(!showLosses){
				return false;
			}


			//废单重下
			if(!OrderEnum.ORDER_CANCEL_TYPE_ABANDON_ORDER_REPEAT.toString().equals(order.getCancelCode())){
				return false;
			}

			//未资金转移
			if (OrderEnum.PAYMENT_STATUS.TRANSFERRED.equals(order.getPaymentType())) {
				return false;
			}

			return true;
		}


		/**
		 * 检验是否已经在线退款确认操作
		 * @param order
		 * @return
		 */
		private boolean isDoneOnlineRefundAudit(OrdOrder order) {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("objectId", order.getOrderId());
			param.put("objectType", AUDIT_OBJECT_TYPE.ORDER.toString());
			param.put("auditType", AUDIT_TYPE.ONLINE_REFUND_AUDIT.getCode());
			param.put("auditStatus", AUDIT_STATUS.PROCESSED.getCode() );
			List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);

			boolean cancleConfirmedProcessed=comAuditList.size()>0?true:false;

			return cancleConfirmedProcessed;
		}


		/**
		 *
		 *
		 *
		 * 检验是否已经做子订单订单取消确认活动
		 * @param order
		 * @return
		 */
		private boolean isDoneChildCancleConfirmedAudit(OrdOrderItem orderItem, boolean flag) {
//		boolean orderStatusNormal=OrderEnum.ORDER_STATUS.NORMAL.getCode().equals(order.getOrderStatus());
//		boolean paymentStatusPayed=OrderEnum.PAYMENT_STATUS.PAYED.equals(order.getPaymentStatus());
//
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("objectId",orderItem.getOrderItemId());
			param.put("objectType", AUDIT_OBJECT_TYPE.ORDER_ITEM.toString());
			if(flag){
				param.put("auditType", "CANCEL_CONFIRM_AUDIT");
			}else{
				param.put("auditType", AUDIT_TYPE.CANCEL_AUDIT.getCode());
			}
			param.put("auditStatus", AUDIT_STATUS.PROCESSED.getCode() );
			List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);

			boolean cancleConfirmedProcessed=comAuditList.size()>0?true:false;
		/*if ( orderStatusNormal && !paymentStatusPayed && !paymentAuditProcessed) {
			isDonePaymentAudit=true;
		}*/

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
			OrdAuditConfig ordAuditConfig=ordAuditConfigService.findOrdAuditConfig(orderItem.getCategoryId(), AUDIT_TYPE.CERTIFICATE_AUDIT.getCode(), loginUserId);
			if (ordAuditConfig==null) {
				certificateAuthority=false;
			}
			return certificateAuthority;
		}

		/**
		 * 根据orderId,传凭证确认类型附件,判读是否已经做过凭证确认活动
		 * @param order
		 * @return
		 */
		private boolean isDoneCertificate(String certConfirmStatus) {

			boolean result=false;
		/*Map<String,Object> param = new HashMap<String,Object>();
		param.put("orderId",order.getOrderId());
		param.put("attachmentType",OrderEnum.ATTACHMENT_TYPE.CERTIFICATE.name());

		List<OrderAttachment> orderAttachmentList=orderAttachmentService.findOrderAttachmentByCondition(param);

		if (!orderAttachmentList.isEmpty()) {
			result=true;
		}*/


			if (CERT_CONFIRM_STATUS.CONFIRMED.name().equals(certConfirmStatus)) {
				result=true;
			}



			return result;
		}

		/**
		 *
		 * 查询出未分配或者待处理的 预订通知 auditIdArray
		 * @param orderId
		 * @return
		 */
		private Long[] findBookingAuditIds(String objectType,Long objectId,String auditSubtype) {

			Map<String, Object> param = new HashMap<String, Object>();
			param.put("objectId", objectId);
			param.put("objectType", objectType);
			param.put("auditType", AUDIT_TYPE.BOOKING_AUDIT.getCode());
			param.put("auditSubtype", auditSubtype);
			String[] auditStatusArray=new String[]{AUDIT_STATUS.POOL.getCode(), AUDIT_STATUS.UNPROCESSED.getCode()};
			param.put("auditStatusArray",auditStatusArray );
			List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);

			Long[] auditIdArray=new Long[comAuditList.size()];
			for (int i = 0; i < comAuditList.size(); i++) {
				auditIdArray[i]=comAuditList.get(i).getAuditId();
			}
			return auditIdArray;
		}

		/**
		 * 订单价格修改总计
		 * @param orderId 订单Id
		 * @return
		 */
		public Long getTotalAmountChange(Long orderId,String objectType,Long orderItemId){

			HashMap<String,Object> params = new HashMap<String,Object>();
			if(StringUtils.isNotEmpty(objectType)){
				params.put("objectType", objectType);
			}
			if(null !=orderItemId){
				params.put("objectId", orderItemId);
			}
			params.put("orderId",orderId);
			params.put("approveStatus", "APPROVE_PASSED");
			List<OrdAmountChange> list = orderAmountChangeService.findOrdAmountChangeList(params);

			//订单价格修改总计
			long totalAmountChange = 0;
			for (int i = 0; i < list.size(); i++) {

				OrdAmountChange ordAmountChange = list.get(i);

				// 订单价格减少
				if (StringUtils.isNotEmpty(ordAmountChange.getFormulas())
						&& "SUBTRACT".equals(ordAmountChange.getFormulas())) {
					if (i < 1) {
						totalAmountChange = -ordAmountChange.getAmount();
					} else {
						totalAmountChange -= ordAmountChange.getAmount();
					}

				} else {
					if (i < 1) {
						totalAmountChange = ordAmountChange.getAmount();
					} else {
						totalAmountChange += ordAmountChange.getAmount();
					}

				}

			}

			return totalAmountChange;
		}

		/**
		 * 订单阶梯计价规则
		 * @param order
		 * @param resultMap
		 * @param showMap
		 * @return
		 */
		public Map<String, Object> differentialRulePrice(OrdOrder order, Map<String, List<Boolean>> showMap, Map<String, List<OrderMonitorRst>> resultMap){
			Map<String, Object> mp = new HashMap<String, Object>();
			try
			{
				Long amountChange = getTotalAmountChange(order.getOrderId(),"ORDER",null);
				Long amount = 0L;//定义主订单扣款金额
				List<OrdOrderItem> orderItemList = null;
				StringBuffer refoundStr = new StringBuffer("");
				List<Long> itemRuleList = new ArrayList<Long>();
				//如果订单结果修改
				if(amountChange!=0)
				{
					orderItemList = calculateOrderPrice(order,amountChange);
				}else{
					orderItemList = order.getOrderItemList();
				}

				List<OrdOrderItem> sortedOrdItems = sortOrderItem(orderItemList,showMap,resultMap);

				List<SuppGoodsRefundVO> goodsRefundList = null;
				Date cancleDate = new Date();
				int i = 0;
				for (OrdOrderItem orderItem : sortedOrdItems) {
					Date lastTime = orderRefundRulesService.getLastTime(orderItem);
					LOG.info("子订单"+orderItem.getOrderItemId()+":lastTime为"+lastTime);
					goodsRefundList = SuppGoodsRefundTools.calcDeductAmt(orderItem, lastTime, cancleDate);
					if(null !=goodsRefundList && goodsRefundList.size()>0){
						i+=1;
						//退改信息
						for (SuppGoodsRefundVO suppGoodsRefundVO : goodsRefundList) {

							LOG.info("refound order"+order.getOrderId()+",refoundStr="+suppGoodsRefundVO.getRefundId()+suppGoodsRefundVO.getCancelStrategy()+suppGoodsRefundVO.getCancelTimeType()+suppGoodsRefundVO.getIsCurrent());
							//根据当前时间匹配的规则
							if(null != suppGoodsRefundVO.getIsCurrent()&&suppGoodsRefundVO.getIsCurrent()){
								amount +=suppGoodsRefundVO.getDeductAmt();
								itemRuleList.add(suppGoodsRefundVO.getRefundId());
							}
							refoundStr.append(isBackRule(goodsRefundList.size() > 1, suppGoodsRefundVO, order));
						}
					} else {
						if (SuppGoods.PAYTARGET.PAY.getCode().equalsIgnoreCase(order.getPaymentTarget())) {
							refoundStr.append(isBackRule(false, null, order));
						}else{
							refoundStr.append("无</br>");
						}
					}
				}

				if(itemRuleList.size() !=i || i==0){
					mp.put("backAmount", "本次订单取消不满足退改规则，请手动计算扣款金额，确认取消？");
				}else{
					mp.put("backAmount", "本次订单取消需扣除用户"+PriceUtil.convertToYuan(amount)+"元，确认取消？");
				}

				if(amount>order.getActualAmount()){
					mp.put("backAmount", "扣款金额大于付款金额，请人工计算，确认取消？");
				}
				mp.put("refoundStr", refoundStr.toString());
			}catch(Exception e){
				LOG.error("{}", e);
			}

			return mp;
		}

		private List<OrdOrderItem> sortOrderItem(List<OrdOrderItem> orderItemList, Map<String, List<Boolean>> showMap, Map<String, List<OrderMonitorRst>> resultMap){
			//将子订单按照订单详情页子订单列表的显示规则排序
			List<OrdOrderItem> sortedOrdItems = new ArrayList<OrdOrderItem>();
			Map<Long,OrdOrderItem> orderItemMaps = new HashMap<Long, OrdOrderItem>();
			for(OrdOrderItem orderItem :orderItemList){
				orderItemMaps.put(orderItem.getOrderItemId(), orderItem);
			}
			for (Map.Entry<String, List<OrderMonitorRst>> entry : resultMap.entrySet()) {
				List<OrderMonitorRst> childOrderResultList =  entry.getValue();

				List<Boolean> result = showMap.get(entry.getKey());
				if(result.get(0)){//包含非show票
					for(OrderMonitorRst orderMonitorRst:childOrderResultList){
						if(!DisneyUtils.isDisneyShow(orderMonitorRst.getSpecialTicketType()) && !ShowTicketUtils.isShowTicket(orderMonitorRst.getSpecialTicketType())){
							sortedOrdItems.add(orderItemMaps.get(orderMonitorRst.getOrderItemId()));
						}
					}
				}
				if(result.get(1)){//包含show票
					for(OrderMonitorRst orderMonitorRst:childOrderResultList){
						if(DisneyUtils.isDisneyShow(orderMonitorRst.getSpecialTicketType()) || ShowTicketUtils.isShowTicket(orderMonitorRst.getSpecialTicketType())){
							sortedOrdItems.add(orderItemMaps.get(orderMonitorRst.getOrderItemId()));
						}
					}
				}
			}
			LOG.info(GsonUtils.toJson(sortedOrdItems));
			return sortedOrdItems;
		}


		/**
		 * 订单价格有过修改则计算各子订单单价
		 * @param order
		 * @param AmountChange
		 */
		public List<OrdOrderItem> calculateOrderPrice(OrdOrder order,Long AmountChange){
			BigDecimal totalAmountChange = BigDecimal.valueOf(AmountChange);
			List<OrdOrderItem> orderItemList = order.getOrderItemList();
			//定义初始主订单总金额
			BigDecimal orderTotalAmount = BigDecimal.ZERO;
			//如果主订单是景点门票
			if(BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(order.getCategoryId())|| BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().equals(order.getCategoryId()))
			{
				List<OrdOrderItem> itemList = calculateSingleTicket(orderItemList, totalAmountChange, orderTotalAmount);
				return itemList;
			}else if(BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(order.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(order.getCategoryId()))
			{
				List<OrdOrderItem> otherItemList = new ArrayList<OrdOrderItem>();
				if(null !=orderItemList){
					//过滤非门票系列子订单
					for (OrdOrderItem ordOrderItem : orderItemList) {
						//只平摊门票系列的子订单的（如：保险，快递等均不计入平摊）
						if(BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(ordOrderItem.getCategoryId())
								|| BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(ordOrderItem.getCategoryId())
								|| BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().equals(ordOrderItem.getCategoryId())
								|| BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(ordOrderItem.getCategoryId())){
							otherItemList.add(ordOrderItem);
						}
					}
					if(otherItemList.size() == 1){
						//计算初始单价
						hasCalculateOneItem(totalAmountChange, otherItemList);
					}else
					{
						for (OrdOrderItem ordOrderItem : otherItemList) {
							orderTotalAmount = orderTotalAmount.add(BigDecimal.valueOf(ordOrderItem.getTotalAmount()));
							BigDecimal quantity = BigDecimal.valueOf(ordOrderItem.getQuantity());
							//计算初始的单价
							BigDecimal totalAmount = BigDecimal.valueOf(ordOrderItem.getTotalAmount());
							BigDecimal price = totalAmount.divide(quantity, 2, BigDecimal.ROUND_HALF_EVEN);
							ordOrderItem.setPrice(price.longValue());
						}
						hasCalculateItems(otherItemList, orderTotalAmount, totalAmountChange);
					}
				}
				return otherItemList;
			}

			return null;
		}

		public List<OrdOrderItem> calculateSingleTicket(List<OrdOrderItem> orderItemList,BigDecimal totalAmountChange,BigDecimal orderTotalAmount){

			for (OrdOrderItem orderItem : orderItemList) {
				BigDecimal quantity = BigDecimal.valueOf(orderItem.getQuantity());
				BigDecimal totalAmount = BigDecimal.valueOf(orderItem.getTotalAmount());
				//计算出初始的单价
				BigDecimal price = totalAmount.divide(quantity, 2,  BigDecimal.ROUND_HALF_EVEN);
				//累计主订单总的初始应付金额
				orderTotalAmount = orderTotalAmount.add(totalAmount);
				orderItem.setPrice(price.longValue());
			}
			if(orderItemList.size()==1)
			{
				hasCalculateOneItem(totalAmountChange, orderItemList);
			}else{
				hasCalculateItems(orderItemList, orderTotalAmount, totalAmountChange);
			}
			return orderItemList;
		}

		/**
		 * 单个子订单的平摊计算
		 * @param totalAmountChange
		 * @param orderItemList
		 */
		private void hasCalculateOneItem(BigDecimal totalAmountChange,List<OrdOrderItem> orderItemList){
			BigDecimal itemTotal = BigDecimal.valueOf(orderItemList.get(0).getTotalAmount());
			//修改后的总金额
			BigDecimal itemTotalChangeAmount = itemTotal.add(totalAmountChange);
			orderItemList.get(0).setTotalAmount(itemTotalChangeAmount.longValue());

			BigDecimal quantity = BigDecimal.valueOf(orderItemList.get(0).getQuantity());
			//修改后每份的单价
			BigDecimal quantityAmount = itemTotalChangeAmount.divide(quantity, 2, BigDecimal.ROUND_HALF_EVEN);

			orderItemList.get(0).setPrice(quantityAmount.longValue());
		}

		/**
		 * 多个子订单的平摊计算
		 * @param orderItemList
		 * @param orderTotalAmount
		 * @param totalAmountChange
		 */
		private void hasCalculateItems(List<OrdOrderItem> orderItemList,BigDecimal orderTotalAmount,BigDecimal totalAmountChange){
			BigDecimal sumPercent = BigDecimal.ZERO;
			BigDecimal quantity = BigDecimal.ZERO;
			for(int i =0;i<orderItemList.size()-1;i++){
				quantity = BigDecimal.valueOf(orderItemList.get(i).getQuantity());//2
				BigDecimal totalAmount = BigDecimal.valueOf(orderItemList.get(i).getTotalAmount());	//30
				BigDecimal percent = totalAmount.divide(orderTotalAmount, 6, BigDecimal.ROUND_HALF_EVEN);//0.375
				sumPercent = sumPercent.add(percent);//  0.375
				//本子订单的平摊的金额
				BigDecimal amount = percent.multiply(totalAmountChange);//3.75
				//计算出每一份的平摊金额
				BigDecimal quantityAmount = amount.divide(quantity, 2, BigDecimal.ROUND_HALF_EVEN);//-1.875
				BigDecimal price = BigDecimal.valueOf(orderItemList.get(i).getPrice()).add(quantityAmount);	//13.125
				orderItemList.get(i).setPrice(price.longValue());
			}

			OrdOrderItem orderItem = orderItemList.get(orderItemList.size()-1);
			quantity = BigDecimal.valueOf(orderItem.getQuantity());//2
			BigDecimal lastPercent = BigDecimal.ONE.subtract(sumPercent);//0.625
			BigDecimal lastChangeAmount =  lastPercent.multiply(totalAmountChange);//-6.25
			//计算获取最后一个子订单的总金额
			BigDecimal lastTotalAmount = BigDecimal.valueOf(orderItem.getTotalAmount()).add(lastChangeAmount);//43.75

			BigDecimal lastQuantityAmount = lastChangeAmount.divide(quantity, 2, BigDecimal.ROUND_HALF_EVEN);//21.875
			BigDecimal lastPrice = BigDecimal.valueOf(orderItemList.get(orderItemList.size()-1).getPrice());//25
			orderItemList.get(orderItemList.size()-1).setPrice(lastPrice.add(lastQuantityAmount).longValue());	//21.875

			orderItemList.get(orderItemList.size()-1).setTotalAmount(lastTotalAmount.longValue());
		}


		public Map<String, Object> differentialChildRule(OrdOrderItem orderItem){
			Map<String, Object> mp = new HashMap<String, Object>();
			try{
				OrdOrder order = complexQueryService.queryOrderByOrderId(orderItem.getOrderId());
				Long amountChange = getTotalAmountChange(order.getOrderId(),"ORDER",null);
				Long amount = 0L;
				List<OrdOrderItem> orderItemList = null;
				//如果订单结果修改
				if(amountChange!=0)
				{
					orderItemList = calculateOrderPrice(order,amountChange);
				}else
				{
					orderItemList = order.getOrderItemList();
				}
				StringBuffer refoundStr = new StringBuffer("");
				List<SuppGoodsRefundVO> goodsRefundList = null;
				Date cancleDate = new Date();
				Boolean hasRule = false;//判断是否匹配到规则
				for (OrdOrderItem ordItem : orderItemList) {
					if(orderItem.getOrderItemId().equals(ordItem.getOrderItemId())){
						Date lastTime = orderRefundRulesService.getLastTime(orderItem);
						goodsRefundList = SuppGoodsRefundTools.calcDeductAmt(ordItem, lastTime, cancleDate);
					}
				}

				if(CollectionUtils.isNotEmpty(goodsRefundList)){
					if(orderItem.hasTicketAperiodic() && ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.getCode().equalsIgnoreCase(order.getRealCancelStrategy())){
						List<com.lvmama.vst.back.goods.po.SuppGoodsRefund> suppGoodsRefunds = new ArrayList<com.lvmama.vst.back.goods.po.SuppGoodsRefund>();
						for(SuppGoodsRefundVO suppGoodsRefundVO : goodsRefundList){
							com.lvmama.vst.back.goods.po.SuppGoodsRefund suppGoodsRefund = new com.lvmama.vst.back.goods.po.SuppGoodsRefund();
							BeanUtils.copyProperties(suppGoodsRefundVO, suppGoodsRefund);
							suppGoodsRefunds.add(suppGoodsRefund);
							if(null !=suppGoodsRefundVO.getIsCurrent() && suppGoodsRefundVO.getIsCurrent()){
								amount+=suppGoodsRefundVO.getDeductAmt();
								hasRule = true;
							}
						}
						refoundStr.append(SuppGoodsRefundTools.SuppGoodsRefundVOToStr(suppGoodsRefunds, "Y"));
					}else{
						for (SuppGoodsRefundVO suppGoodsRefundVO : goodsRefundList) {
							refoundStr.append(isBackRule(goodsRefundList.size() > 1, suppGoodsRefundVO, order));
							if(null !=suppGoodsRefundVO.getIsCurrent() && suppGoodsRefundVO.getIsCurrent()){
								amount+=suppGoodsRefundVO.getDeductAmt();
								hasRule = true;
							}
						}
					}
				} else {
					if (SuppGoods.PAYTARGET.PAY.getCode().equalsIgnoreCase(order.getPaymentTarget())) {
						refoundStr.append(isBackRule(false, null, order));
					}
				}
				mp.put("refoundStr", refoundStr);
				if(!hasRule){
					mp.put("backAmount", "当前订单不满足退改规则,请手动计算扣款金额，确认取消？");
				}else
				{
					mp.put("backAmount", "本次订单取消需扣除用户"+PriceUtil.convertToYuan(amount)+"元，确认取消？");
				}
				Long totalPrice =orderItem.getQuantity()*orderItem.getPrice();
				if(amount>totalPrice){
					mp.put("backAmount", "扣款金额大于实际金额，请人工计算，确认取消？");
				}
				return mp;
			}catch(Exception e){
				LOG.error("{}", e);
			}
			return mp;
		}

		private Map<String,Object> getChildRule(OrdOrderItem orderItem, Date visitDate){
			Map<String, Object> mp = new HashMap<String, Object>();
			try{
				StringBuffer refoundStr = new StringBuffer("");
				OrdOrder order = complexQueryService.queryOrderByOrderId(orderItem.getOrderId());
				if(ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){
					mp.put("refoundStr", ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCnName()+"</br>");
					mp.put("backAmount", "当前订单不满足退改规则,请手动计算扣款金额，确认取消？");
					return mp;
				}
				if( ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(orderItem.getCancelStrategy())){
					mp.put("refoundStr", ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCnName()+"</br>");
					mp.put("backAmount", "当前订单不满足退改规则,请手动计算扣款金额，确认取消？");
					return mp;
				}

				Long amountChange = getTotalAmountChange(order.getOrderId(),"ORDER",null);
				Long amount = 0L;
				List<OrdOrderItem> orderItemList = null;
				//如果订单结果修改
				if(amountChange!=0)
				{
					orderItemList = calculateOrderPrice(order,amountChange);
				}else{
					orderItemList = order.getOrderItemList();
				}

				List<SuppGoodsRefundVO> goodsRefundList = null;
				Date cancleDate = new Date();
				Boolean hasRule = false;//判断是否匹配到规则
				for (OrdOrderItem ordItem : orderItemList) {
					if(orderItem.getOrderItemId().equals(ordItem.getOrderItemId())){
						goodsRefundList = SuppGoodsRefundTools.calcDeductAmt(ordItem, visitDate, cancleDate);
					}
				}

				if(CollectionUtils.isNotEmpty(goodsRefundList)){
					for (SuppGoodsRefundVO suppGoodsRefundVO : goodsRefundList) {

						//修改游玩日标准 add by lijuntao
						if(!SuppGoodsRefundVO.CANCEL_TIME_TYPE.OTHER.getCode().equals(suppGoodsRefundVO.getCancelTimeType())){
							Date refundDate = DateUtils.addMinutes(visitDate, -suppGoodsRefundVO.getLatestCancelTime().intValue());
							suppGoodsRefundVO.setRefundDate(refundDate);
						}


						refoundStr.append(isBackDestBuRule(goodsRefundList.size() > 1, suppGoodsRefundVO, order));
						if(null !=suppGoodsRefundVO.getIsCurrent() && suppGoodsRefundVO.getIsCurrent()){
							amount+=suppGoodsRefundVO.getDeductAmt();
							hasRule = true;
						}
					}
				} else {
					if (SuppGoods.PAYTARGET.PAY.getCode().equalsIgnoreCase(order.getPaymentTarget())) {
						refoundStr.append(isBackDestBuRule(false, null, order));
					}
				}
				mp.put("refoundStr", refoundStr);
				if(!hasRule){
					mp.put("backAmount", "当前订单不满足退改规则,请手动计算扣款金额，确认取消？");
				}else{
					mp.put("backAmount", "本次订单取消需扣除用户"+PriceUtil.convertToYuan(amount)+"元，确认取消？");
				}
				Long totalPrice =orderItem.getQuantity()*orderItem.getPrice();
				if(amount>totalPrice){
					mp.put("backAmount", "扣款金额大于实际金额，请人工计算，确认取消？");
				}
				return mp;
			}catch(Exception e){
				LOG.error("{}", e);
			}
			return mp;
		}

		private String isBackRule(boolean moreThanOneRule, SuppGoodsRefundVO suppGoodsRefundVO, OrdOrder order){
			if (SuppGoods.PAYTARGET.PAY.getCode().equalsIgnoreCase(order.getPaymentTarget())) {
				return "景区支付订单无退改规则</br>";
			}

			if(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsRefundVO.getCancelStrategy())){
				return "不可退</br>";
			}

			if (StringUtils.isBlank(suppGoodsRefundVO.getCancelStrategy())
					|| SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.MANUALCHANGE.name().equalsIgnoreCase(suppGoodsRefundVO.getCancelStrategy())) {
				return "人工退改</br>";
			}

//		String isBack ="人工退改";
//		if(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsRefundVO.getCancelStrategy())){
		String expCssPrefix = "";
		String expCssSuffix = "";
		Date currDate = new Date();
		if (suppGoodsRefundVO.getRefundDate() != null && currDate
				.compareTo(suppGoodsRefundVO.getRefundDate()) > 0) {
			expCssPrefix = "<span class='lineae_line'>";
			expCssSuffix = "</span>";
		}
		//外币结算 begin
		String currencyRefundNum = "";
		LOG.info("退改政策:" + GsonUtils.toJson(suppGoodsRefundVO));
		if (StringUtils.isNotEmpty(suppGoodsRefundVO.getCurrencyDeductType()) && suppGoodsRefundVO.getCurrencyDeductValue() != null) {
			currencyRefundNum = (suppGoodsRefundVO.getCurrencyDeductAmt() == null ? "" : new Double(suppGoodsRefundVO.getCurrencyDeductAmt()) / 100) + "";
			LOG.info("currencyRefundNum=" + currencyRefundNum);
			if ("0.0".equals(currencyRefundNum) || "0".equals(currencyRefundNum)) {//容错处理
				currencyRefundNum = "";
			}
		}
		String settlementDeductStr = "与供应商结算(" + currencyRefundNum + suppGoodsRefundVO.getCurrencyName() + ")";
		// （禅道:项目-任务,id=115002）订单详情页景乐产品类型的【退票政策】不出现“与供应商结算”等字样
			if(BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(order.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(order.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(order.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_wifi.getCategoryId().equals(order.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().equals(order.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(order.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(order.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(order.getCategoryId())
					|| BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(order.getCategoryId())){
				settlementDeductStr="";
			}
		//外币结算 end

		String isBack = "人工退改</br>";
		if(suppGoodsRefundVO.getRefundDate() != null) {
			isBack = "可退，扣款金额("
					+ PriceUtil.convertToYuan(suppGoodsRefundVO.getDeductAmt())
					+ "元)";
			if (StringUtils.isNotEmpty(currencyRefundNum)) {
				if("".equals(settlementDeductStr)){
					isBack = isBack  + "，"
							+ expCssPrefix
							+ DateUtil.formatDate(suppGoodsRefundVO.getRefundDate(),
							DateUtil.HHMM_DATE_FORMAT) + "前" + expCssSuffix
							+ "</br>";
				}else{
					isBack = isBack + "，" +settlementDeductStr + "，"
							+ expCssPrefix
							+ DateUtil.formatDate(suppGoodsRefundVO.getRefundDate(),
							DateUtil.HHMM_DATE_FORMAT) + "前" + expCssSuffix
							+ "</br>";
				}
			} else {
				isBack = isBack + "，" + expCssPrefix
						+ DateUtil.formatDate(suppGoodsRefundVO.getRefundDate(),
						DateUtil.HHMM_DATE_FORMAT) + "前" + expCssSuffix
						+ "</br>";
			}
		}

		if (!SuppGoodsRefund.CANCEL_TIME_TYPE.OTHER.name().equals(suppGoodsRefundVO.getCancelTimeType())) {
			//阶梯退改的情况，直接返回
			return isBack;
		}
		//固定金额/不满足上述条件
		if (!moreThanOneRule) {//只有一条规则，即选择的是“订单未使用，扣除每张（份）”
			if (suppGoodsRefundVO.getDeductValue() <= 0) {
				if (DateUtil.diffDay(order.getVisitTime(), new Date()) <= 365) {
					isBack = "未使用，无损退</br>";
				} else {
					isBack = "<SPAN style='TEXT-DECORATION: line-through'>未使用，无损退</SPAN></br>";
				}
			} else {
				isBack = "未使用，扣款金额(" + PriceUtil.convertToYuan(suppGoodsRefundVO.getDeductAmt()) + "元)";
				if (StringUtils.isNotEmpty(currencyRefundNum)) {
					if("".equals(settlementDeductStr)){
						isBack = isBack  + "</br>";
					}else{
						isBack = isBack + "，" + settlementDeductStr + "</br>";
					}
				} else {
					isBack = isBack + "</br>";
				}
			}

			return isBack;
		}
		//规则多余一条，阶梯退改的情况
		if (SuppGoodsRefund.DEDUCTTYPE.AMOUNT.name().equals(suppGoodsRefundVO.getDeductType())) { //固定金额
			if (suppGoodsRefundVO.getDeductValue() <= 0) {
				isBack = "逾期未使用，无损退</br>";
			} else {
				isBack = "逾期未使用，扣款金额(" + PriceUtil.convertToYuan(suppGoodsRefundVO.getDeductAmt()) + "元)";
				if (StringUtils.isNotEmpty(currencyRefundNum)) {
					if("".equals(settlementDeductStr)){
						isBack = isBack + "，可退</br>";
					}else{
						isBack = isBack + "，" + settlementDeductStr + "，可退</br>";
					}
				} else {
					isBack = isBack + "，可退</br>";
				}
			}
		} else if (SuppGoodsRefund.DEDUCTTYPE.PERCENT.name().equals(suppGoodsRefundVO.getDeductType())) {
			if (suppGoodsRefundVO.getDeductValue() <= 0) {
				isBack = "逾期未使用，无损退</br>";
			} else if (suppGoodsRefundVO.getDeductValue() == 10000) {//100%
				isBack = "逾期未使用，不可退</br>";
			} else {
				isBack = "逾期未使用，扣款金额(" + PriceUtil.convertToYuan(suppGoodsRefundVO.getDeductAmt()) + "元)";
				if (StringUtils.isNotEmpty(currencyRefundNum)) {
					if("".equals(settlementDeductStr)){
						isBack = isBack + "，可退</br>";
					}else{
						isBack = isBack + "，" + settlementDeductStr + "，可退</br>";
					}
				} else {
					isBack = isBack + "，可退</br>";
				}
			}
		}

		return isBack;
	}


		/**
		 * 目的地bu产品退改规则生成方法 add by lijuntao
		 * @param moreThanOneRule
		 * @param suppGoodsRefundVO
		 * @param order
		 * @return
		 */
		private String isBackDestBuRule(boolean moreThanOneRule, SuppGoodsRefundVO suppGoodsRefundVO, OrdOrder order){
			if (SuppGoods.PAYTARGET.PAY.getCode().equalsIgnoreCase(order.getPaymentTarget())) {
				return "现付订单无退改规则</br>";
			}

			if(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsRefundVO.getCancelStrategy())){
				return "不可退</br>";
			}

			if (StringUtils.isBlank(suppGoodsRefundVO.getCancelStrategy())
					|| SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.MANUALCHANGE.name().equalsIgnoreCase(suppGoodsRefundVO.getCancelStrategy())) {
				return "人工退改</br>";
			}

//		String isBack ="人工退改";
//		if(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equalsIgnoreCase(suppGoodsRefundVO.getCancelStrategy())){
			String expCssPrefix = "";
			String expCssSuffix = "";
			Date currDate = new Date();
			if (suppGoodsRefundVO.getRefundDate() != null && currDate
					.compareTo(suppGoodsRefundVO.getRefundDate()) > 0) {
				expCssPrefix = "<span class='lineae_line'>";
				expCssSuffix = "</span>";
			}

			//外币结算扣款 begin
			String currencyRefundNum = "";
			LOG.info("退改政策:" + GsonUtils.toJson(suppGoodsRefundVO));
			if (StringUtils.isNotEmpty(suppGoodsRefundVO.getCurrencyDeductType()) && suppGoodsRefundVO.getCurrencyDeductValue() != null) {
				currencyRefundNum = (suppGoodsRefundVO.getCurrencyDeductAmt() == null ? "" : new Double(suppGoodsRefundVO.getCurrencyDeductAmt()) / 100) + "";
				LOG.info("currencyRefundNum=" + currencyRefundNum);
				if ("0.0".equals(currencyRefundNum) || "0".equals(currencyRefundNum)) {//容错处理
					currencyRefundNum = "";
				}
			}
			String settlementDeductStr = "与供应商结算扣除(" + currencyRefundNum + suppGoodsRefundVO.getCurrencyName() + ")";
			//外币结算扣款 end

			String isBack = "人工退改</br>";
			if(suppGoodsRefundVO.getRefundDate() != null) {
				isBack = "可退，扣款金额("
						+ PriceUtil.convertToYuan(suppGoodsRefundVO.getDeductAmt())
						+ "元)，";
				if (StringUtils.isNotEmpty(currencyRefundNum)) {
					isBack = isBack + settlementDeductStr + "，"
							+ expCssPrefix
							+ DateUtil.formatDate(suppGoodsRefundVO.getRefundDate(),
							DateUtil.HHMM_DATE_FORMAT) + "前" + expCssSuffix
							+ "</br>";
				} else {
					isBack = isBack + expCssPrefix
							+ DateUtil.formatDate(suppGoodsRefundVO.getRefundDate(),
							DateUtil.HHMM_DATE_FORMAT) + "前" + expCssSuffix
							+ "</br>";
				}
			}

			if(!SuppGoodsRefund.CANCEL_TIME_TYPE.OTHER.name().equals(suppGoodsRefundVO.getCancelTimeType())) {
				//阶梯退改的情况，直接返回
				return isBack;
			}
			//固定金额/不满足上述条件
			if(!moreThanOneRule) {//只有一条规则，即选择的是“订单未使用，扣除每份”
//			if(suppGoodsRefundVO.getDeductValue() <= 0) {
//				if (DateUtil.diffDay(order.getVisitTime(), new Date()) <= 30) {
//					isBack = "可退，无损退</br>";
//				} else {
//					isBack = "不可退改</br>";
//				}
//			} else {
				isBack = "可退，扣款金额(" + PriceUtil.convertToYuan(suppGoodsRefundVO.getDeductAmt()) + "元)";
				if (StringUtils.isNotEmpty(currencyRefundNum)) {
					isBack = isBack + "，" + settlementDeductStr + "</br>";
				} else {
					isBack = isBack + "</br>";
				}
//			}

				return isBack;
			}
			//规则多余一条，阶梯退改的情况
			if(SuppGoodsRefund.DEDUCTTYPE.AMOUNT.name().equals(suppGoodsRefundVO.getDeductType())) { //固定金额

				isBack = "可退，不满足以上条件，扣款金额(" + +PriceUtil.convertToYuan(suppGoodsRefundVO.getDeductAmt())+ "元)";
				if (StringUtils.isNotEmpty(currencyRefundNum)) {
					isBack = isBack + "，" + settlementDeductStr + "</br>";
				} else {
					isBack = isBack + "</br>";
				}

			} else if(SuppGoodsRefund.DEDUCTTYPE.PERCENT.name().equals(suppGoodsRefundVO.getDeductType())) {

				isBack = "可退，不满足以上条件，扣款金额(" + +PriceUtil.convertToYuan(suppGoodsRefundVO.getDeductAmt())+ "元)";
				if (StringUtils.isNotEmpty(currencyRefundNum)) {
					isBack = isBack + "，" + settlementDeductStr + "</br>";
				} else {
					isBack = isBack + "</br>";
				}

			}

			return isBack;
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


		/**
		 * 根据下单时间来判断显示新规则or旧规则
		 * @param order
		 * @return
		 */
		private boolean showBackRule(OrdOrder order){
			Date createTime = order.getCreateTime();
			String ruleTime = "2015-05-26 00:00:00";//新规则上线日期
			Date ruleDate = null;
			boolean newTicketRule = false;
			try
			{
				if(null !=createTime){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					ruleDate = sdf.parse(ruleTime);
					if(createTime.compareTo(ruleDate)==1)
					{
						//下单时间晚于规则时间
						newTicketRule = true;
					}
				}
			}catch(Exception e){
				LOG.error("{}", e);
			}
			return newTicketRule;
		}

		/**
		 * 重新发送取消申请
		 * @param orderId
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
//			certif.setConfirmTime(new Date());
//			certif.setConfirmUser(getLoginUserId());
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
		 * 新订单监控的界面上点击发送支付通知会发起这个请求
		 * @param model
		 * @param request
		 * @param orderId
		 */
		@RequestMapping(value = "/sendFlightPaymentInfo")
		@ResponseBody
		public Object sendFlightPaymentInfo(Long orderId) {
			if (orderId == null) {
				LOG.error("orderId不能为空！");
			}
			try {
				ResultHandle result = flightOrderProcessService
						.paymentNotifyByOrder(orderId);
				if (result.isFail()) {
					LOG.info("OrderDetailAction::sendFlightPaymentInfo_orderId="
							+ orderId + " errorMessage=" + result.getMsg());
					return new ResultMessage(ResultMessage.ERROR, "出票失败");
				} else {
					LOG.info("OrderDetailAction::sendFlightPaymentInfo_orderId="
							+ orderId + " result=SUCCESS");
					return new ResultMessage(ResultMessage.SUCCESS, "出票通知发送成功");
				}
			} catch (Exception e) {
				LOG.error("{}", e);
				return new ResultMessage(ResultMessage.ERROR, e.getMessage());
			}
		}
		/**
		 *查看 使用状态明细
		 *
		 * @param model
		 * @param orderItemId
		 * @param performStatus
		 * @param categoryCode
		 * @return
		 */
		@RequestMapping(value = "/showPerformTimeDetail")
		public String showPerformTimeDetail(Model model, Long orderItemId, String performStatus, String categoryCode){
			//查询通关记录
			List<OrdTicketPerform> resultList = new ArrayList<OrdTicketPerform>();
			resultList=complexQueryService.selectByOrderItem(orderItemId);
			OrdTicketPerformTimeInfo ordTicketPerformTimeInfo=null;
			List<OrdTicketPerformTimeInfo> ordTicketPerformTimeInfoList=new ArrayList<OrdTicketPerformTimeInfo>();
			Boolean isPlay=false;
			if(ProductCategoryUtil.isWifi(categoryCode) ||  ProductCategoryUtil.isConnects(categoryCode) || ProductCategoryUtil.isPlay(categoryCode)){
				isPlay=true;
			}
			if(CollectionUtils.isNotEmpty(resultList)) {
				OrdTicketPerform ordTicketPerform=resultList.get(0);
				String[] operators=null;
				if(StringUtil.isNotEmptyString(ordTicketPerform.getOperator())){
					operators=ordTicketPerform.getOperator().split(",");
				}
				List<OrdTicketPerformDetail> list =complexQueryService.selectPerformDetailByOrderItem(orderItemId);
				if(list!=null && list.size()>0){
					for(int i=0;i<list.size();i++){
						ordTicketPerformTimeInfo=new OrdTicketPerformTimeInfo();
						ordTicketPerformTimeInfo.setOrderItemId(ordTicketPerform.getOrderItemId());
						if(operators!=null && i<operators.length){
							ordTicketPerformTimeInfo.setOperator(operators[i]);
						}
						if(list.get(i).getPerformTime()!=null){
							ordTicketPerformTimeInfo.setPerformTime(DateUtil.formatDate(list.get(i).getPerformTime(), DateUtil.HHMMSS_DATE_FORMAT));
						}
						if(i+1==list.size()){
							ordTicketPerformTimeInfo.setPerformStatus(performStatus);
							if(StringUtil.isNotEmptyString(performStatus) && performStatus.contains("部分使用")){
								ordTicketPerformTimeInfo.setPerformStatus("部分使用");
							}
						}else{
							ordTicketPerformTimeInfo.setPerformStatus("部分使用");
						}
						ordTicketPerformTimeInfo.setAdultNumber(ordTicketPerform.getAdultQuantity() == null ? 0 : ordTicketPerform.getAdultQuantity());
						ordTicketPerformTimeInfo.setChildNumber(ordTicketPerform.getChildQuantity() == null ? 0 : ordTicketPerform.getChildQuantity());
						if(isPlay){
							ordTicketPerformTimeInfo.setPerformNumber(String.valueOf((list.get(i)
									.getActualAdult() == null ? 0
									: list.get(i).getActualAdult()) + (list.get(i)
									.getActualChild() == null ? 0
									: list.get(i).getActualChild())));
						}else{
							ordTicketPerformTimeInfo.setPerformNumber(String.valueOf(((list.get(i)
									.getActualAdult() == null ? 0
									: list.get(i).getActualAdult()) + (list.get(i)
									.getActualChild() == null ? 0
									: list.get(i).getActualChild()))
									/ (ordTicketPerform.getAdultQuantity() + ordTicketPerform
									.getChildQuantity())));
						}
						ordTicketPerformTimeInfoList.add(ordTicketPerformTimeInfo);
					}
				}else{
					ordTicketPerformTimeInfo=new OrdTicketPerformTimeInfo();
					ordTicketPerformTimeInfo.setOrderItemId(ordTicketPerform.getOrderItemId());
					ordTicketPerformTimeInfo.setOperator(ordTicketPerform.getOperator());
					ordTicketPerformTimeInfo.setPerformStatus(performStatus);
					ordTicketPerformTimeInfo.setAdultNumber(ordTicketPerform.getAdultQuantity() == null ? 0 : ordTicketPerform.getAdultQuantity());
					ordTicketPerformTimeInfo.setChildNumber(ordTicketPerform.getChildQuantity() == null ? 0 : ordTicketPerform.getChildQuantity());
					if(ordTicketPerform.getPerformTime()!=null){
						ordTicketPerformTimeInfo.setPerformTime(DateUtil.formatDate(ordTicketPerform.getPerformTime(), DateUtil.HHMMSS_DATE_FORMAT));
					}
					if(isPlay){
						ordTicketPerformTimeInfo.setPerformNumber(String.valueOf((ordTicketPerform
								.getActualAdult() == null ? 0
								: ordTicketPerform.getActualAdult()) + (ordTicketPerform
								.getActualChild() == null ? 0
								: ordTicketPerform.getActualChild())));
					}else {
						ordTicketPerformTimeInfo.setPerformNumber(String.valueOf(((ordTicketPerform
								.getActualAdult() == null ? 0
								: ordTicketPerform.getActualAdult()) + (ordTicketPerform
								.getActualChild() == null ? 0
								: ordTicketPerform.getActualChild()))
								/ (ordTicketPerform.getAdultQuantity() + ordTicketPerform
								.getChildQuantity())));
					}

					ordTicketPerformTimeInfoList.add(ordTicketPerformTimeInfo);
				}
			}
			model.addAttribute("performTimeInfoList", ordTicketPerformTimeInfoList);
			model.addAttribute("isPlay", isPlay.toString());
			return "/order/orderStatusManage/allCategory/showPerformTimeDetail";
		}




		@RequestMapping(value = "/findOrdOrderTravellerConfirm")
		public String findOrdOrderTravellerConfirm(Model model, Long orderId){

			try {

				if(orderId!=null){
					OrdOrderTravellerConfirm
							ordOrderTravellerConfirm =
							orderTravellerConfirmClientService.
									findOrderTravellerConfirmInfoByOrderId(orderId);
					if(ordOrderTravellerConfirm!=null){
						model.addAttribute("ordOrderTravellerConfirm", ordOrderTravellerConfirm);
					}
				}
			} catch (Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
			}

			return "/order/orderStatusManage/allCategory/findOrdOrderTravellerConfirm";
		}


		@RequestMapping(value = "/editPersonCountiune")
		public String editPersonCountiune(Model model, Long orderId){


			return "/order/orderStatusManage/allCategory/editPersonCountiune";
		}

		@RequestMapping(value = "/showUpdateVisaDocLastTime")
		public String showUpdateVisaDocLastTime(Model model, HttpServletRequest request){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<showUpdateWaitPaymentTime>");
			}
			return "/order/orderStatusManage/allCategory/showUpdateVisaDocLastTime";
		}
		@RequestMapping(value = "/updateVisaDocLastTime")
		@ResponseBody
		public Object updateVisaDocLastTime( HttpServletRequest request,Long orderId,String oldVisaDocLastTime,String visaDocLastTime){
			try {
				ordFormInfoService.updateVisaDocLastDate(orderId, visaDocLastTime);
				String loginUserId=this.getLoginUserId();
				lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
						orderId,
						orderId,
						loginUserId,
						"将编号为["+orderId+"]的订单，变更材料截止收取时间",
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"变更材料截止收取时间",
						"原有料截止收取时间："+oldVisaDocLastTime+"，新的料截止收取时间："+visaDocLastTime);
				return ResultMessage.UPDATE_SUCCESS_RESULT;
			} catch (Exception e) {
				LOG.error("{}", e);
				return new ResultMessage(ResultMessage.ERROR, e.getMessage());
			}
		}
		/**************
		 * 获取券订单信息
		 * @param ordOrder
		 * @return
		 */
		public OrdOrder getStampOrdOrder(OrdOrder ordOrder,Model model){
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
				LOG.error("{}", e.getMessage());
				return ordOrder;
			}
		}



		/**
		 *可对换商品详细信息
		 * @param model
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "/boundGoods")
		public String boundGoods(Model model, HttpServletRequest request) {
			String stampId = request.getParameter("stampId");
			List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
			LOG.info("------------------12------------------");
			String url =Constant.getInstance().getPreSaleBaseUrl() + "/customer/stamp/bindGoods/"+stampId;
			try {
				if(stampId!=null){
					StampGoods[] stampGoods = RestClient.getClient().getForObject(url,StampGoods[].class);
					List<StampGoods> goods= new ArrayList<StampGoods>();
					if(stampGoods !=null){
						goods= Arrays.asList(stampGoods);
					}
					if(goods != null && goods.size()>0){
						for(int i=0;i<goods.size();i++){
							Map<String, Object> map=new HashMap<String, Object>();
							String branchType=null;
							if( goods.get(i).getCategoryId()!=null&&!"".equals( goods.get(i).getCategoryId())){
								ResultHandleT<BizCategory> result=categoryClientService.findCategoryById(Long.valueOf(goods.get(i).getCategoryId()));
								BizCategory bizCategory=result.getReturnContent();
								branchType=bizCategory.getCategoryName();
							}
							map.put("branchType",branchType);
							map.put("goodsId", goods.get(i).getGoodsId());
							map.put("goodsName", goods.get(i).getGoodsName());
							list.add(map);
						}
					}

				}
				model.addAttribute("boundGoodsList",list);
			} catch (Exception e) {
				model.addAttribute("msg", "调用接口异常!");
				LOG.error(ExceptionFormatUtil.getTrace(e));
			}
			return "/order/orderStatusManage/allCategory/showBoundGoodsList";
		}
		/**
		 *预售券抵扣详细信息
		 * @param model
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "/showstampDeductionList")
		public String showstampDeductionList(Model model, HttpServletRequest request) {
			// TODO check
			String orderIdStr = request.getParameter("orderId");
//		 List<StampDeductionList> list=new ArrayList<StampDeductionList>();
//		try {
//		if(orderIdStr!=null){
//			list=getStampDeductionList(orderIdStr);
//		}
			List<UseStampCodeInfo> list = getStampDeductionListNew(orderIdStr);
			model.addAttribute("stampCodeList",list);
//		} catch (Exception e) {
//			model.addAttribute("msg", "调用接口异常!");
//			LOG.error(ExceptionFormatUtil.getTrace(e));
//		}
			return "/order/orderStatusManage/allCategory/showstampDeductionList";
		}

		private List<UseStampCodeInfo> getStampDeductionListNew(String orderId) {

			if(StringUtils.isBlank(orderId))
				return null;

			String url = Constant.getInstance().getPreSaleBaseUrl() + "/customer/stamp/useStamp/{useOrderId}";
			UseStampCodeResponse resp = null;
			try {
				resp = RestClient.getClient().getForObject(url, UseStampCodeResponse.class, orderId);
			} catch(Exception e) {
				LOG.error(ExceptionFormatUtil.getTrace(e));
			}

			if(resp == null || !StringUtils.equals(resp.getCode(), "1000") || CollectionUtils.isEmpty(resp.getUseStampCodes()))
				return null;

			List<UseStampCodeInfo> list = resp.getUseStampCodes();

			for(UseStampCodeInfo stamp : list) {
				stamp.setOrderStatus(ORDER_STATUS.getCnName(stamp.getOrderStatus()));
				stamp.setBindStatus(STAMP_UNBIND_STATUS.getCnName(stamp.getBindStatus()));
			}

			return list;
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
										deductionList.setOrderStatus(ORDER_STATUS.getCnName(order.getOrderStatus()));
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
		//转换当地玩乐下交通接驳的 服务信息
		private final void configOrderConnectsServiceProp(Model model, OrdOrder ordOrder,OrdOrderItem orderItem){
			if(model != null && ordOrder != null && CollectionUtils.isNotEmpty(ordOrder.getOrderConnectsServicePropList())){

				Map<String,Object> accessServicePropMap = new HashMap<String, Object>();
				Map<String,Object> giveServicePropMap = new HashMap<String, Object>();
				Map<String,Object> rentServicePropMap = new HashMap<String, Object>();
				//规格名称
				boolean hasAccess = false;
				boolean hasGive = false;
				boolean hasRent = false;
				List<OrderConnectsServiceProp> orderConnectsServicePropList = ordOrder.getOrderConnectsServicePropList();
				for(OrderConnectsServiceProp orderConnectsServiceProp : orderConnectsServicePropList){
					String prodCode = orderConnectsServiceProp.getPropCode();
					String prodValue = orderConnectsServiceProp.getPropValue();
					if(StringUtil.isNotEmptyString(prodCode)){
						if(Long.valueOf(50).equals(orderConnectsServiceProp.getBranchId())){
							accessServicePropMap.put(prodCode,prodValue);
							hasAccess = true;
						}else if(Long.valueOf(51).equals(orderConnectsServiceProp.getBranchId())){
							giveServicePropMap.put(prodCode,prodValue);
							hasGive = true;
						}else if(Long.valueOf(52).equals(orderConnectsServiceProp.getBranchId())){
							rentServicePropMap.put(prodCode,prodValue);
							hasRent = true;
						}
					}
				}
				if(orderItem!=null && orderItem.getContentValueByKey("branchCode")!=null){
					if("access_air".equalsIgnoreCase((String)orderItem.getContentValueByKey("branchCode"))){
						hasGive = false;
						hasRent = false;
					}else if("give_air".equalsIgnoreCase((String)orderItem.getContentValueByKey("branchCode"))){
						hasAccess = false;
						hasRent = false;
					}else if("rent_car".equalsIgnoreCase((String)orderItem.getContentValueByKey("branchCode"))){
						hasAccess = false;
						hasGive = false;
					}
				}
				model.addAttribute("hasOrderServiceProp", true);
				model.addAttribute("accessServicePropMap", accessServicePropMap);
				model.addAttribute("giveServicePropMap", giveServicePropMap);
				model.addAttribute("rentServicePropMap", rentServicePropMap);
				model.addAttribute("hasAccess", hasAccess);
				model.addAttribute("hasGive", hasGive);
				model.addAttribute("hasRent", hasRent);
			}

		}
		//获取玩乐（美食 娱乐 属性）
		private final void configPlayPropConvertByMap(Model model,String categoryType,Map<String,Object> contentMap,OrdOrderItem ordOrderItem){
			if (contentMap != null && StringUtils.isNotBlank(categoryType)) {
				String useTime = (String) contentMap.get(ORDER_PLAY_TYPE.useTime.name());
				String localHotelAddress = (String) contentMap.get(ORDER_PLAY_TYPE.localHotelAddress.name());
				ordOrderItem.setUseTime(useTime);
				ordOrderItem.setLocalHotelAddress(localHotelAddress);
			}

		}


		/**
		 * 查看分销商信息
		 * @param model
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "/showDistributorInfo")
		public String showDistributorInfo(Model model, HttpServletRequest request) {
			String distributionChannel = request.getParameter("distributionChannel");
			TntUserInfoVo tntUserInfoVo = new TntUserInfoVo();
			try {
				LOG.info("showDistributorInfo getDistributorInfoDetail param=="+distributionChannel);
				if(StringUtil.isNotEmptyString(distributionChannel)){
					ResultHandleT<TntUserInfoVo> res=tntDistributorServiceRemote.getDistributorInfoDetail(Long.parseLong(distributionChannel));
					if(res.isSuccess() && res.getReturnContent()!=null){
						tntUserInfoVo=res.getReturnContent();
					}else{
						LOG.info("showDistributorInfo getDistributorInfoDetail=="+distributionChannel+" fail=="+res.getMsg());
					}
				}
			} catch (Exception e) {
				LOG.error("showDistributorInfo distribution=="+distributionChannel+" error "+e.toString());
			}
			model.addAttribute("tntUserInfoVo", tntUserInfoVo);
			return "/order/orderStatusManage/allCategory/showDistributorInfo";
		}

		/**
		 * 查看退款明细
		 * @param model
		 * @param orderItemId
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "/queryRefundInfo")
		public String queryRefundInfo(Model model,  Long orderItemId,HttpServletRequest request) {
			List<OrderRefundBatch> orderRefundBatchList = new ArrayList<OrderRefundBatch>();
			try {
				LOG.info("queryRefundInfo paramorderItemId=="+orderItemId);
				if(null!=orderItemId){
					Map<String,Object> params = new HashMap<String, Object>();
					params.put("orderItemId", orderItemId);
					orderRefundBatchList=orderRefundBatchService.getOrderRefundBatch(params);
					if(null==orderRefundBatchList || orderRefundBatchList.size()<=0){
						LOG.info("queryRefundInfo orderItemId=="+orderItemId+" data empty");
					}
				}
			} catch (Exception e) {
				LOG.error("queryRefundInfo orderItemId=="+orderItemId+" error "+e.toString());
			}
			model.addAttribute("orderRefundBatchList", orderRefundBatchList);
			return "/order/orderStatusManage/allCategory/showRefundInfo";
		}

	/**
	 * 佣金信息
	 * @param model
	 * @param orderItemId
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/commissionInfo")
	public String commissionInfo(Model model,  Long orderItemId,HttpServletRequest request){
		com.lvmama.order.api.base.vo.ResponseBody<List<OrdOrderCommissionVo>> ordOrderCommissions = new com.lvmama.order.api.base.vo.ResponseBody<>();
		try{
			LOG.info("commissionInfo paramorderItemId==" + orderItemId);
			if(null!=orderItemId){
				OrdOrderCommissionVo ordOrderCommissionVo = new OrdOrderCommissionVo();
				ordOrderCommissionVo.setOrderItemId(orderItemId);
				ordOrderCommissions = apiOrdOrderCommissionQueryService.findByParams(new com.lvmama.order.api.base.vo.RequestBody<>(ordOrderCommissionVo));
				if(CollectionUtils.isEmpty(ordOrderCommissions.getT())){
					LOG.info("commissionInfo orderItemId=="+orderItemId+" data empty");
				}
			}
		}catch (Exception e){
			LOG.error("commissionInfo orderItemId=="+orderItemId+" error "+e.toString());
		}
		model.addAttribute("ordOrderCommissions", ordOrderCommissions.getT());
		return "/order/orderStatusManage/allCategory/showCommissionInfo";
	}

		/**
		 * 查看退款明细日志
		 * @param model
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "/queryRefundDetailInfo")
		public String queryRefundDetailInfo(Model model,HttpServletRequest request) {
			String batchId=request.getParameter("batchId");
			String ordItemId=request.getParameter("ordItemId");
			List<OrderRefundBatchDetail> orderRefundBatchDetailList = new ArrayList<OrderRefundBatchDetail>();
			try {
				LOG.info("queryRefundDetailInfo parambatchId=="+batchId+"  ordItemId=="+ordItemId);
				if(StringUtil.isNotEmptyString(batchId) && StringUtil.isNotEmptyString(ordItemId)){
					OrderRefundBatchDetail detail=new OrderRefundBatchDetail();
					detail.setOrderItemId(Long.parseLong(ordItemId));
					detail.setRefundItemBatchId(Long.parseLong(batchId));
					orderRefundBatchDetailList=orderRefundBatchDetailService.getOrderRefundBatchDetails(detail);
					if(null==orderRefundBatchDetailList || orderRefundBatchDetailList.size()<=0){
						LOG.info("queryRefundDetailInfo parambatchId=="+batchId+"  ordItemId=="+ordItemId+" data empty");
					}
				}
			} catch (Exception e) {
				LOG.error("queryRefundDetailInfo parambatchId=="+batchId+"  ordItemId=="+ordItemId+" error "+e.toString());
			}
			model.addAttribute("orderRefundBatchDetailList", orderRefundBatchDetailList);
			return "/order/orderStatusManage/allCategory/showRefundDetailInfo";
		}


		/**
		 * 查询门票订单使用状态
		 * @param model
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "/queryTicketPerformStatus")
		@ResponseBody
		public String queryTicketPerformStatus(Model model,Long orderId,HttpServletRequest request) {
			OrdOrder order=	complexQueryService.queryOrderByOrderId(orderId);

			//门票业务类订单使用状态
			List<OrdTicketPerform> resultList = new ArrayList<OrdTicketPerform>();
			List<OrdOrderItem> ordItemsList =order.getOrderItemList();
			//订单使用状态
			List<String> perFormStatusList = new ArrayList<String>();
			if(ordItemsList != null) {
				for (OrdOrderItem ordOrderItem : ordItemsList) {
					//门票业务类订单使用状态
					Map<String,Object> performMap = ordOrderItem.getContentMap();
					String categoryCode =  (String) performMap.get(ORDER_COMMON_TYPE.categoryCode.name());
					if (ProductCategoryUtil.isTicket(categoryCode)) {
						resultList = complexQueryService.selectByOrderItem(ordOrderItem.getOrderItemId());
						String performStatusName=OrderUtils.calPerformStatus(resultList,order,ordOrderItem);
						perFormStatusList.add(performStatusName);
					}
				}
			}
			return OrderUtils.getMainOrderPerformStatusCode(perFormStatusList);
		}


//此处调用EBK接口进行组装，不在vst自己组装，因此注释下面这个方法
//		private Boolean ebkSendMail(String eamil, Long orderId, String productName, Long orderItemId, List<EbkEmailAttch> ebkEmailAttchList) {
//			Boolean falg=false;
//			try{
//				List<EmailAttachment> files=new ArrayList<EmailAttachment>();
//				EmailAttachment emailAttachment=null;
//				for (EbkEmailAttch ebkEmailAttch:ebkEmailAttchList){
//					emailAttachment=new EmailAttachment();
//					emailAttachment.setFileId(ebkEmailAttch.getFileId());
//					emailAttachment.setFileName(ebkEmailAttch.getFileName());
//					files.add(emailAttachment);
//				}
//				String content = "<html><p>亲爱的客户您好!</p><p>您于驴妈妈预定的 产品："+productName+"  订单号："+orderId+"</p><p>已经帮您预订成功，附件是您的电子确认函，请查收!并仔细阅读凭证内容，谢谢!</p>";
//				content+="<p>使用方式烦请仔细阅读驴妈妈旅游网您预订的产品页面!</p><p>凭证上的时间为当地时间，与国内时间有时间差，请注意安排，以免耽误您的行程。如有问题，您也可</br>以拨打凭证上的中文服务热线或24小时紧急联系人进行咨询。</p>";
//				content+="<p>您所预订的项目在游玩时若出现突发状况或不满意的情况，请立刻系我们，我们会立刻帮您核实并协调处理，如果行程结束之后回国才告知我们，取证会非常困难，无法</br>无法处理，谢谢配合!</p>";
//				content+="<p>如有疑问，请致电1010-6060,境外请拔打+86-21-61800981，购买平台进行咨询，谢谢。</p>";
//				content+="<p>祝您的旅途，一切安心顺利，期待您的下次光临~</p>";
//				content+="<p>自助游天下，就找驴妈妈!</p>";
//				content+="</html>";
//				EmailContent email = new EmailContent();
//				email.setFromName("驴妈妈旅游");
//				email.setToAddress(eamil);
//				String subJect = "驴妈妈确认函-订单号："+orderId;
//				email.setSubject(subJect);
//				LOG.info("ebksendemail orderItemId=="+orderItemId+" ,"+email.getSubject());
//				email.setContentText(content);
//				LOG.info("ebksendemail orderItemId=="+orderItemId+" ,"+email.getContentText());
//				email.setCreateTime(new Date());
//				LOG.info("ebksendemail orderItemId=="+orderItemId+"  toAddress="+eamil);
//				Long result = vstEmailService.sendEmailDirectFillAttachment(email, files);
//				LOG.info("ebksendemail orderItemId=="+orderItemId+"  result="+result);
//				falg=true;
//			}catch (Exception e){
//				LOG.error("ebksendemail orderItemId=="+orderItemId+" ,"+e.toString());
//			}
//			return  falg;
//		}
		@RequestMapping(value = "/showResendEmail")
		public String showReSendEmail(Model model,Long orderId,Long orderItemId, String email) {
			model.addAttribute("orderId", orderId);
			model.addAttribute("orderItemId", orderItemId);
			model.addAttribute("email", email);
			return "/order/orderStatusManage/allCategory/showResendEmail";
		}

		@RequestMapping(value = "/resendemail")
		@ResponseBody
		public Object resendEmail(Model model,@RequestParam("orderItemId")Long orderItemId,@RequestParam("orderId")Long orderId,@RequestParam("email")String email){
			Map<String,Object> map = new HashMap<>();
			map.put("message", "重发邮件失败");

			com.lvmama.order.api.base.vo.RequestBody<Long> requestBody = new com.lvmama.order.api.base.vo.RequestBody<>();
			requestBody.setT(orderItemId);
			com.lvmama.order.api.base.vo.ResponseBody<OrdEbkEmailVo> responseBody = this.apiEbkEmailQueryService.selectOrdEbkEmailByOrderItemId(requestBody);
			if( responseBody.isSuccess() && null!=responseBody.getT()){
				model.addAttribute("alreadySend", true);
			}else{
				model.addAttribute("alreadySend", false);
			}
			OrdEbkEmailVo ordEbkEmailVo = responseBody.getT();
			for(String fileId : ordEbkEmailVo.getFileId()){
				EbkEmailAttch ebkEmailAttch = new EbkEmailAttch();
				ebkEmailAttch.setFileId(Long.valueOf(fileId));
				ebkEmailAttch.setFileName(ordEbkEmailVo.getFileName().get(fileId));
			}

			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			OrdOrderItem orderItem=order.getOrderItemByOrderItemId(orderItemId);
			Long userId = this.getLoginUser() == null ? null : this.getLoginUser().getUserId();
			String userName = this.getLoginUser() == null ? null : this.getLoginUser().getUserName();
			ResultHandleT<Boolean> resultHandleT =
					ebkEmailClientService.resendMail(email, orderId.toString(), orderItemId,
							orderItem.getProductName(), orderItem.getSuppGoodsName(), orderItem.getQuantity(),
							userId, userName);
			StringBuffer logger = new StringBuffer();
			if(resultHandleT.getReturnContent()){
				com.lvmama.order.api.base.vo.RequestBody<OrdEbkEmailVo> request = new com.lvmama.order.api.base.vo.RequestBody<>();
				ordEbkEmailVo.setEmail(email);
				request.setT(ordEbkEmailVo);
				com.lvmama.order.api.base.vo.ResponseBody<Integer> response = this.apiEbkEmailProcessService.executeUpdateEbkEmail(request);
				if( responseBody.isSuccess() && response.getT()==1){
					logger.append("主订单Id：" + orderId + "; 子订单Id：" + orderItemId + "邮件凭证重新发送成功.邮箱地址："+email);
					map.put("message", "重发邮件成功");
				}else{
					logger.append("主订单Id：" + orderId + "; 子订单Id：" + orderItemId + "邮件凭证重新发送成功,分销推送失败.邮箱地址："+email);
				}
			}else{
				logger.append("主订单Id：" + orderId + "; 子订单Id：" + orderItemId + "邮件凭证重新发送失败.邮箱地址："+email);
			}
			insertLog(orderId, orderItemId,  logger);
		    return map;
			
		}

		private void insertLog(Long orderId, Long orderItemId,  StringBuffer log) {
			try{
				comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.EBK_SEND_EMAIL_MEMO, Long.valueOf(orderId), orderItemId,
						getLoginUserId(),
						log.toString(),
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_ITEM_EBK_SEND_EMAIL.name(),
						"重新发EBK入园邮件操作", ComLog.COM_LOG_OBJECT_TYPE.EBK_RESEND_EMAIL.getCnName());
			}catch (Exception e){
				LOG.error("ebksendemail orderItemId=="+orderItemId+" insertLog error "+e.toString());
			}
		}

		@RequestMapping(value = "/showEbkEmailLog")
		public ModelAndView showEbkEmailLog(Long objectId,String objectType ,String sysName){
			final Map<String,Object> params = new HashMap<String,Object>();

			int curPage = 1;
			int pageSize = 100;
			ComLogPams comLogPams = new ComLogPams();
			comLogPams.setSysName(ComLogPams.SYS_NAME.VST);
			comLogPams.setObjectId(objectId);
			comLogPams.setObjectType(objectType);

			com.lvmama.log.comm.bo.ResultHandle<com.lvmama.log.comm.utils.Pagination<com.lvmama.log.comm.po.ComLog>> resultHandle = queryLogClientService.findLog(comLogPams,curPage,pageSize);
			com.lvmama.log.comm.utils.Pagination<com.lvmama.log.comm.po.ComLog>bizLogPage = null;

			if(resultHandle != null && resultHandle.getT() != null){
				bizLogPage = resultHandle.getT();
			}
			String stastus = bizLogPage == null ? "EMPTY ... " : "TotalRows: " + bizLogPage.getTotalRows()
					+ "," + "getCurPage: " + bizLogPage.getCurPage();
			log.info(" bizLogPage status:  " + stastus);


			params.put("bizLogPage", bizLogPage);
			params.put("sysName", sysName);

			params.put("objectId", objectId);
			params.put("objectType", objectType);


			return new ModelAndView("/order/orderStatusManage/allCategory/showEbkEmailLog",params);

		}

		@RequestMapping(value = "/findGuideInfo")
		public String findGuideInfo(Model model, HttpServletRequest request,Long orderId){
			if (LOG.isDebugEnabled()) {
				LOG.debug("start method<findGuideInfo>");
			}
			String flag="N";
			if(null!=orderId){
				List<OrdPerson> guideInfoList = queryGuideInfo(orderId);
				if(null!=guideInfoList && guideInfoList.size()>0){
					flag="Y";
					model.addAttribute("personList", guideInfoList);
				}
			}
			model.addAttribute("flag", flag);
			return "/order/orderStatusManage/allCategory/findGuideInfo";
		}
		
		@RequestMapping(value = "/findRightList")
		public String findRightList(Model model, Long orderId,HttpServletRequest req) throws BusinessException {
			if (orderId != null) {
				OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
				List<OrdOrderItem> ordItemsList = order.getOrderItemList();
				OrdOrderItem ordOrderItem = null;
				if(ordItemsList != null && ordItemsList.size() > 0) {
					ordOrderItem = ordItemsList.get(0);
				}
				String json = ordOrderItem.getContentStringByKey("financeOtherInterestsVoList");
				List<HashMap> financeOtherInterestsVoList = new ArrayList<HashMap>();
				if(StringUtils.isNotBlank(json)) {
					financeOtherInterestsVoList = JSON.parseArray(json.toString(), HashMap.class);
				}
				
				String financeInterestsBonusVoJson = ordOrderItem.getContentStringByKey("financeInterestsBonusVo");
				if(financeInterestsBonusVoJson != null && !"".equals(financeInterestsBonusVoJson)) {
					FinanceInterestsBonus financeInterestsBonusVo = com.alibaba.fastjson.JSONObject.toJavaObject(JSON.parseObject(financeInterestsBonusVoJson), FinanceInterestsBonus.class);
					String banCategory = financeInterestsBonusVo.getBanCategory();
					if(banCategory != null && !"".equals(banCategory)){
						String [] banCategorys = banCategory.split(",");
						StringBuffer sb = new StringBuffer();
						for (int i=0; i< banCategorys.length ;i++) {
							if(i != 0){
								sb.append(",");
							}
							if(banCategorys[i] != null && !"".equals(banCategorys[i])){
								sb.append(BIZ_CATEGORY_TYPE.getCnName(Long.valueOf(banCategorys[i])));
							}
						}
						financeInterestsBonusVo.setBanCategory(sb.toString());
					}
					model.addAttribute("financeInterestsBonusVo", financeInterestsBonusVo);
				}
				
				model.addAttribute("financeOtherInterestsVoList", financeOtherInterestsVoList);
				
				model.addAttribute("orderItem", ordOrderItem);
				
				model.addAttribute("orderItemId",ordOrderItem.getOrderItemId());
			}
			return "/order/orderStatusManage/findRightList";
		}
		
		public static void main(String[] args) {
			List<OrdPerson> guideInfoList = new ArrayList<>();
			OrdPerson person=new OrdPerson();
			person.setFirstName("wangguoqing");
			person.setEmail("111@11.11");
			guideInfoList.add(person);
			Object json =  com.alibaba.fastjson.JSONObject.toJSON(person);
			System.out.println(json);
			OrdPerson person1 = null;
			person1 =  com.alibaba.fastjson.JSONObject.toJavaObject(JSON.parseObject(json.toString()), OrdPerson.class);
			System.out.println(person.getFirstName());
		}
	}


