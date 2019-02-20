/**
 * 
 */
package com.lvmama.vst.order.client.ord.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.lvmama.annotation.ReadOnlyDataSource;
import com.lvmama.comm.bee.po.ord.OrdRefundApply;
import com.lvmama.comm.bee.po.ord.OrdRefundment;
import com.lvmama.comm.bee.po.ord.OrdRefundmentLog;
import com.lvmama.comm.pet.fs.client.FSClient;
import com.lvmama.comm.pet.po.email.EmailAttachment;
import com.lvmama.comm.pet.po.email.EmailContent;
import com.lvmama.comm.pet.po.mark.MarkCouponUsage;
import com.lvmama.comm.pet.po.pay.PayPayment;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.comm.pet.refund.vo.OrdRefundUpdateVO;
import com.lvmama.comm.pet.refund.vo.OrdRefundmentItemSplit;
import com.lvmama.comm.pet.refund.vo.RefundOrderItemSplit;
import com.lvmama.comm.pet.service.perm.PermUserService;
import com.lvmama.comm.pet.service.sale.OrdRefundApplyService;
import com.lvmama.comm.pet.service.sale.OrdSaleServiceService;
import com.lvmama.comm.pet.vo.refund.UserDeductDetailVO;
import com.lvmama.comm.pet.vo.refund.UserRefundApplyEnum;
import com.lvmama.comm.pet.vo.refund.UserRefundApplyVO;
import com.lvmama.comm.vst.VstOrderEnum.INFO_STATUS;
import com.lvmama.comm.vst.vo.*;
import com.lvmama.commons.logging.LvmamaLog;
import com.lvmama.commons.logging.LvmamaLogFactory;
import com.lvmama.coupon.api.order.service.CouponOrderService;
import com.lvmama.coupon.mark.order.dto.CouponOrderDTO;
import com.lvmama.lvf.openapi.vstclient.dto.BaseResponseVSTDto;
import com.lvmama.lvf.openapi.vstclient.form.FlightOrderForm;
import com.lvmama.lvf.openapi.vstclient.service.PCOrderService;
import com.lvmama.order.api.base.vo.RequestBody;
import com.lvmama.order.api.base.vo.ResponseBody;
import com.lvmama.order.apportion.IApiOrderApportionService;
import com.lvmama.order.enums.OrdProcessKeyEnum;
import com.lvmama.order.enums.ticket.ItemCancelEnum;
import com.lvmama.order.inquiry.api.service.IApiSupplierInquiryService;
import com.lvmama.order.inquiry.vo.comm.api.PersonUpdateVo;
import com.lvmama.order.process.api.cancel.IApiOrderCancelProcessService;
import com.lvmama.order.process.api.cancel.IApiOrderItemCancelProcessService;
import com.lvmama.order.route.service.IOrder2RouteService;
import com.lvmama.order.route.service.IOrderRouteService;
import com.lvmama.order.service.api.comm.workflow.IApiOrdProcessKeyService;
import com.lvmama.order.vo.comm.OrdOrderCancelInfoVo;
import com.lvmama.order.vo.comm.OrderItemVo;
import com.lvmama.order.vo.comm.OrderVo;
import com.lvmama.order.vo.comm.cancel.OrderCancelParamVo;
import com.lvmama.order.vo.comm.person.OrdPersonVo;
import com.lvmama.order.workflow.api.IApiOrderWorkflowService;
import com.lvmama.order.workflow.utils.BusinessKeyCreator;
import com.lvmama.order.workflow.vo.AuditActiviTask;
import com.lvmama.order.workflow.vo.WorkflowStarterVo;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizCategoryProp;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.biz.po.BizSystemConfigure;
import com.lvmama.vst.back.client.activiti.service.ProcesserClientService;
import com.lvmama.vst.back.client.biz.service.*;
import com.lvmama.vst.back.client.dist.service.DistDistributorProdClientService;
import com.lvmama.vst.back.client.dist.service.DistGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsCircusDetailClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsHotelAdapterClientService;
import com.lvmama.vst.back.client.ord.service.OrdMulPriceRateClientService;
import com.lvmama.vst.back.client.ord.service.OrdOrderStatusClientService;
import com.lvmama.vst.back.client.ord.vo.DoUserCancelApplyInvoiceRequest;
import com.lvmama.vst.back.client.ord.vo.SettlementDetailAcquisitionReq;
import com.lvmama.vst.back.client.ord.vo.TicketSettlementDetail;
import com.lvmama.vst.back.client.passport.service.PassCodeService;
import com.lvmama.vst.back.client.passport.service.PassportSendSmsService;
import com.lvmama.vst.back.client.passport.service.PassportService;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.client.prod.service.*;
import com.lvmama.vst.back.client.prom.service.MarkCouponLimitClientService;
import com.lvmama.vst.back.client.prom.service.PromForbidBuyClientService;
import com.lvmama.vst.back.client.prom.service.PromotionService;
import com.lvmama.vst.back.client.pub.service.ComOrderRequiredClientService;
import com.lvmama.vst.back.client.pub.service.ComPushClientService;
import com.lvmama.vst.back.client.show.service.ShowTicketBaseInfoClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.control.vo.HisResPrecontrolOrderVo;
import com.lvmama.vst.back.control.vo.ResPrecontrolOrderVo;
import com.lvmama.vst.back.ebooking.vo.ProdOrdRoute;
import com.lvmama.vst.back.goods.po.*;
import com.lvmama.vst.back.goods.utils.SuppGoodsCircusUtils;
import com.lvmama.vst.back.goods.utils.SuppGoodsRefundTools;
import com.lvmama.vst.back.goods.vo.*;
import com.lvmama.vst.back.order.exception.OrderException;
import com.lvmama.vst.back.order.po.*;
import com.lvmama.vst.back.order.po.OrdSmsTemplate.SEND_NODE;
import com.lvmama.vst.back.order.po.OrderEnum.AUDIT_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS;
import com.lvmama.vst.back.order.vo.*;
import com.lvmama.vst.back.order.vo.OrdTravelContractVo;
import com.lvmama.vst.back.passport.po.PassCodeImageVo;
import com.lvmama.vst.back.passport.po.PassProvider;
import com.lvmama.vst.back.prod.adapter.ProdProductHotelAdapterClientService;
import com.lvmama.vst.back.prod.po.*;
import com.lvmama.vst.back.prod.po.ProdProduct.PACKAGETYPE;
import com.lvmama.vst.back.prod.po.ProdProduct.PRODUCTTYPE;
import com.lvmama.vst.back.prom.po.*;
import com.lvmama.vst.back.pub.po.*;
import com.lvmama.vst.back.pub.service.ComJobConfigService;
import com.lvmama.vst.back.supp.po.SuppOrderResult;
import com.lvmama.vst.back.supp.po.SuppSettlementEntities;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.back.supp.vo.SuppGoodsBaseTimePriceVo;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comlog.LvmmLogEnum;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.enumeration.CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.po.ComFileMap;
import com.lvmama.vst.comm.utils.*;
import com.lvmama.vst.comm.utils.ActivitiUtils.TASK_KEY;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.comm.utils.front.ProductPreorderUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.OrderUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.utils.redis.RedisEnum;
import com.lvmama.vst.comm.vo.*;
import com.lvmama.vst.comm.vo.Constant.BU_NAME;
import com.lvmama.vst.comm.vo.Constant.ORDER_FAVORABLE_TYPE;
import com.lvmama.vst.comm.vo.econtract.TravelContractVO;
import com.lvmama.vst.comm.vo.order.*;
import com.lvmama.vst.comm.vo.order.BuyInfo.Coupon;
import com.lvmama.vst.comm.vo.order.BuyInfo.Item;
import com.lvmama.vst.comm.vo.order.BuyInfo.Product;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.ebooking.client.ebk.serivce.EbkCertifClientService;
import com.lvmama.vst.ebooking.client.ebk.serivce.EbkUserClientService;
import com.lvmama.vst.ebooking.ebk.po.EbkUser;
import com.lvmama.vst.ebooking.vo.DepartureNoticeVo;
import com.lvmama.vst.elasticsearch.converter.BeanESParameterConverter;
import com.lvmama.vst.elasticsearch.converter.ParameterConverter;
import com.lvmama.vst.elasticsearch.params.ESParams;
import com.lvmama.vst.elasticsearch.params.ESQueryBuilder;
import com.lvmama.vst.elasticsearch.query.ESWrapper;
import com.lvmama.vst.flight.client.goods.vo.FlightNoVo;
import com.lvmama.vst.flight.client.order.service.FlightOrderProcessService;
import com.lvmama.vst.insurant.client.service.InsPolicyClientService;
import com.lvmama.vst.insurant.po.InsPolicy;
import com.lvmama.vst.neworder.order.cancel.IOrderCancelService;
import com.lvmama.vst.neworder.order.cancel.vo.OrderCancelInfo;
import com.lvmama.vst.order.cache.OrderContextCache;
import com.lvmama.vst.order.contract.service.IOrderNoticeRegimentService;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.dao.OrdStampOrderDao;
import com.lvmama.vst.order.dao.OrdStampOrderItemDao;
import com.lvmama.vst.order.dao.datamodel.RawTicketOrderInfo;
import com.lvmama.vst.order.exception.ErrorCodeEnum;
import com.lvmama.vst.order.exception.GetVerifiedFlightInfoFailException;
import com.lvmama.vst.order.exception.HasRecommendFlightException;
import com.lvmama.vst.order.po.*;
import com.lvmama.vst.order.processer.OrderSmsSendProcesser;
import com.lvmama.vst.order.redis.JedisTemplate2;
import com.lvmama.vst.order.refundSplit.OrdRefundBussinessService;
import com.lvmama.vst.order.route.IVstOrderRouteService;
import com.lvmama.vst.order.route.constant.VstRouteConstants;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.service.apportion.ApportionInfoQueryService;
import com.lvmama.vst.order.service.apportion.OrderAmountApportionService;
import com.lvmama.vst.order.service.book.OrderCheckService;
import com.lvmama.vst.order.service.book.OrderSaveService;
import com.lvmama.vst.order.service.book.impl.HotelRebateBussiness;
import com.lvmama.vst.order.service.impl.OrderDistributionBusiness;
import com.lvmama.vst.order.service.impl.OrderEcontractGeneratorService;
import com.lvmama.vst.order.service.impl.PromBuyPresentforClient;
import com.lvmama.vst.order.service.refund.IOrderRefundRulesService;
import com.lvmama.vst.order.service.refund.adapter.OrderRefundProcesserAdapter;
import com.lvmama.vst.order.timeprice.service.lvf.OrderLvfTimePriceServiceImpl;
import com.lvmama.vst.order.utils.ApportionUtil;
import com.lvmama.vst.order.utils.OrderUtils;
import com.lvmama.vst.order.utils.PetUsrReceiversExchanger;
import com.lvmama.vst.order.utils.RestClient;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.*;
import com.lvmama.vst.order.vo.o2o.O2oOrdConstant;
import com.lvmama.vst.order.web.OrderDetailAction;
import com.lvmama.vst.order.zk.DynamicPropertiesFactory;
import com.lvmama.vst.pet.adapter.*;
import com.lvmama.vst.pet.adapter.refund.OrderRefundSplitServiceAdapter;
import com.lvmama.vst.pet.vo.CouponCheckParam;
import com.lvmama.vst.pet.vo.PetUsrReceivers;
import com.lvmama.vst.pet.vo.UserCouponVO;
import com.lvmama.vst.pet.vo.VstCashAccountVO;
import com.lvmama.vst.supp.client.service.*;
import com.lvmama.vst.ticket.utils.DisneyUtils;
import com.lvmama.vst.ticket.vo.PagedTicketOrderInfo;
import com.lvmama.vst.ticket.vo.TicketOrderInfo;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lvmama.vst.order.utils.ForbidBuyUtils.*;

/**
 * 远程调用实现
 *
 * @author lancey
 *
 */
@Component("orderServiceRemote")
public class OrdOrderClientServiceImpl extends AbstractOrderClientService implements IOrderLocalService {

	private static final Logger LOG = LoggerFactory.getLogger(OrdOrderClientServiceImpl.class);
	
	//新加事件日志
	private static final LvmamaLog lvmamaLog = LvmamaLogFactory.getLog(OrdOrderClientServiceImpl.class);
	
	private final long DEFAULT_WAIT_MINUTES = 30;

	private final long DEFAULT_RETENTION_MINUTES = 30;
	
	@Autowired
	private OrdOrderDao ordOrderDao;
	
	@Autowired
	private OrdOrderItemDao ordOrderItemDao;
	
	@Autowired
	private OrdStampOrderDao ordStampOrderDao;
	
	@Autowired
	private OrdStampOrderItemDao ordStampOrderItemDao;
	
	@Autowired
	private IBookService bookService;

	@Resource(name="vstOrderRefundServiceVstSupp")
    private VstOrderRefundService vstOrderRefundServiceVstSupp;
	@Resource(name="vstOrderRefundServiceVstBack")
	private VstOrderRefundService vstOrderRefundServiceVstBack;

	@Autowired
	private IOrderPriceService orderPriceService;

	@Autowired
	private IOrderStockService orderStockService;
	
	@Autowired
	private PassCodeService passCodeService;

	@Autowired
	private IOrdPaymentInfoService ordPaymentInfoService;

	@Autowired
	private IOrderUpdateService orderUpdateService;

	@Autowired
	private IOrderStatusManageService orderStatusManageService;

	@Resource(name="supplierStockCheckService")
    private SupplierStockCheckService supplierStockCheckService;

    @Resource(name="supplierIdNoCheckService")
    private SupplierIdNoCheckService supplierIdNoCheckService;

	@Autowired
	private SuppGoodsCircusDetailClientService suppGoodsCircusDetailClientService;

	@Resource(name="orderMessageProducer")
	private TopicMessageProducer orderMessageProducer;
	

	@Resource(name="receiverUserService")
	private IReceiverUserServiceAdapter receiverUserServiceAdapter;

	@Autowired
	private ProcesserClientService processerClientService;

	@Autowired
	private ComActivitiRelationService comActivitiRelationService;

	@Autowired
	private OrderDistributionBusiness orderDistributionBusiness;

	@Autowired
	private IOrdPrePayServiceAdapter ordPrePayServiceAdapter;

	@Autowired
	private IOrderCombPackService orderCombPackService;
	//推送服务接口
	@Autowired
	private ComPushClientService comPushClientService;

	@Autowired
	private IOrdMulPriceRateService ordMulPriceRateService;

	@Autowired
	private OrderEcontractGeneratorService orderEcontractGeneratorService;

	@Autowired
	private IOrdOrderLoscService ordOrderLoscService;

	@Autowired
	private FavorServiceAdapter favorServiceAdapter;

	@Autowired
	private ICouponService couponService;

	@Autowired
	private ComJobConfigService comJobConfigService;

	@Autowired
	private IOrderAuditService orderAuditService;

	@Autowired
	private ISupplierOrderHandleService supplierOrderHandleService;

	@Autowired
	private IComMessageService comMessageService;

	@Autowired
	private IOrdTravelContractService ordTravelContractService;

	@Autowired
	private IOrdItemContractRelationService iOrdItemContractRelationService;

	@Autowired
	private IOrdOrderItemService iOrdOrderItemService;

	@Autowired
	private DistGoodsClientService distGoodsClientService;// 商品

	@Autowired
	private ResPreControlService resControlBudgetRemote;

	@Autowired
	private ProdProductClientService prodProductClientService;

	/**
	 * 产品适配器，决定调用vst还是目的地酒店服务
	 * */
	@Autowired
	private ProdProductHotelAdapterClientService productHotelAdapterClientService;

	@Autowired
	private BonusPayServiceAdapter bonusPayService;

	@Autowired
	private ProdProductClientService productClientService;

	@Autowired
	private SuppGoodsClientService suppGoodsService;

	@Autowired
	private IOrdOrderService iOrdOrderService;

	@Autowired
	private IOrderExpressService orderExpressService;

	@Autowired
	private IOrderNoticeRegimentService noticeRegimentService;

	@Autowired
	private IOrdInvoiceService ordInvoiceService;

	@Autowired
	private IOrderAttachmentService orderAttachmentService;
	@Autowired
	private OrderPromotionService orderPromotionService;
	@Autowired
	private DestContentClientService destContentClientService;
	@Autowired
	private IOrderInitService orderInitService;

	@Autowired
	private FlightOrderProcessService flightOrderProcessService;

	@Autowired
	private IOrdFlightTicketStatusService ordFlightTicketStatusService;

	@Autowired
	private IOrderResponsibleService orderResponsibleService;

	@Autowired
	private EbkUserClientService ebkUserClientService;

	@Autowired
	private IOrdItemFreebieService ordItemFreebieService;


	@Autowired
	private PetOrderMessageServiceAdapter petOrderMessageService;

	@Autowired
	private MarkCouponLimitClientService markCouponLimitClientService;

	@Autowired
	private SuppSupplierClientService suppSupplierClientService;

	//分单消息生产
	@Resource(name="allocationMessageProducer")
	private TopicMessageProducer allocationMessageProducer;
	@Autowired
	private IOrdOrderHotelTimeRateService ordOrderHotelTimeRateService;

	@Autowired
	private PetMessageServiceAdapter petMessageService;
	@Autowired
	private IOrdOrderPackService iOrdOrderPackService;

    @Autowired
    private SupplierOrderOtherService supplierOrderOtherService;
    @Autowired
    private SupplierOrderService supplierOrderService;
	@Autowired
	private OrderSaveService orderSaveService;

	@Autowired
	private IOrdProductNotice ordProductNotice;
	@Autowired
	private ProdProductNoticeClientService prodProductNoticeClientService;
	@Autowired
	private IO2oOrderService o2oOrderService;
	@Autowired
    private IOrdAdditionStatusService ordAdditionStatusService;
    @Autowired
    private IOrderSendSmsService orderSendSmsService;

	@Autowired
	private OrderRefundmentServiceAdapter orderRefundmentService;

	@Autowired
	private BizSystemConfigureClientService bizSystemConfigureClientRemote;
	@Autowired
    private IOrdPersonService iOrdPersonService;

	@Autowired
    private OrdOrderStatusClientService ordOrderStatusClientService;

    @Autowired
    private DictClientService dictClientService;

	@Autowired
    private OrdOrderTravellerConfirmService ordOrderTravellerConfirmService;

	@Autowired
	private OrderRefundProcesserAdapter orderRefundProcesserAdapter;
	
	@Autowired
    private VstOrderUserRefundServiceAdapter vstOrderRefundServiceRemote;
	
	@Autowired
	private IOrderRefundRulesService orderRefundRulesService;
	
	@Autowired
	private IOrdPayProcessJobService ordPayProcessJobService;

	@Autowired
	private PetOrderPayMessageServiceAdapter petOrderPayMessageService;

	@Autowired
	private IntfPassCodeService intfPassCodeService;

	@Autowired
	protected OrderLvfTimePriceServiceImpl orderLvfTimePriceServiceImpl;

	@Autowired
	private OrdTicketPostService ordTicketPostService;

	@Autowired
	private IOrdOrderTrackingService ordOrderTrackingService;

	@Autowired
    private ShowTicketBaseInfoClientService showTicketBaseInfoClientService;
	
	@Autowired
    private ProdProductPropClientService prodProductPropClientRemote;
	
	@Autowired
	private CategoryPropClientService categoryPropClientRemote;

	@Autowired
	private InsPolicyClientService insPolicyClientService;

	private static final int LIMIT_SELECT_PRODUCT=20;
	private static final String BOUNDARY_TIMER="boundarytimer1";
	
	@Autowired
    private PassportService passportServiceRemote;
	
	@Autowired
	private EbkCertifClientService ebkCertifClientService;

	@Autowired
	private SuppGoodsClientService suppGoodsClientService;

	@Autowired
	private SuppGoodsHotelAdapterClientService suppGoodsHotelAdapterClientService;

	@Autowired
	private IOrdPremUserRelService iOrdPremUserRelService;


	@Autowired
	private PromForbidBuyClientService  promForbidBuyClientService;

	@Autowired
	private IOrderPromService orderPromServiceImpl;

	@Autowired
	private CategoryClientService categoryClientService;

	@Autowired
	private ISupplierOrderOperator supplierOrderOperator;

	@Resource(name="orderDetailAction")
	private OrderDetailAction orderDetailAction;

    @Autowired
    private ProdPackageGroupClientService prodPackageGroupClientRemote;
	@Autowired
	private ComOrderRequiredClientService comOrderRequiredClientRemote;

    @Autowired
	private IOrdAddressService ordAdressService;

    @Autowired
	private IOrderInvoiceRelationService orderInvoiceRelationService;

    @Autowired
	private IOrderSmsSendService orderSmsSendService;

	@Autowired
	private PassportSendSmsService passportSendSmsService;

	@Autowired
    private IOrdItemAdditionStatusService ordItemAdditionStatusService;

	@Autowired
	private OrdPayPromotionService ordPayPromotionService;
	
	@Autowired
	private DistDistributorProdClientService distDistributorProdClientRemote;

	@Autowired
	private LvmmLogClientService lvmmLogClientService;

	@Autowired
	private VstEmailServiceAdapter vstEmailServiceAdapter;

	@Autowired
	private FSClient vstFSClient;

	@Autowired
	private PromBuyPresentforClient promBuyPresentforClient;
	
	@Autowired
	private OrdApplyInvoiceInfoService ordApplyInvoiceInfoService;

	@Autowired
	private ProdProductAdditionClientService prodProductAdditionClientService;

	@Autowired
	private HotelRebateBussiness hotelRebateBussiness;

	@Autowired
	private IHotelTradeApiService hotelTradeApiService;

	@Autowired
	private IOrdOrderAmountItemService ordOrderAmountItemService;

	@Autowired
	private IComplexQueryService complexQueryService;

	@Autowired
	private OrderChannelService orderChannelService;

	@Autowired
	private ProdDestReClientService prodDestReClientRemote;
	
    @Autowired
    private IOrdTravAdditionConfService ordTravAdditionConfService;
    
    @Autowired
    private OrderSmsSendProcesser orderSmsSendProcesser;
    
    @Autowired
    private IOrdAccInsDelayInfoService ordAccInsDelayInfoService;
	
	//注入支付业务
	@Autowired
	private IPayPaymentServiceAdapter payPaymentServiceAdapter;
	
    @Autowired
    private IOrdItemPersonRelationService iOrdItemPersonRelationService;
    
    @Autowired
	private DynamicPropertiesFactory dynamicPropertiesFactory;

    @Autowired
    private IOrdAuditProcessTaskService ordAuditProcessTaskService;

    @Autowired
    private  IOrderCancelService   orderCancelService ;
    
    @Resource(name="orderWorkflowMessageProducer")
	private TopicMessageProducer orderWorkflowMessageProducer;
    @Autowired
    private OrderCheckService orderCheckService;
    //分摊服务
	@Autowired
	private OrderAmountApportionService orderAmountApportionService;

    @Autowired
	private OrderRefundSplitServiceAdapter orderRefundSplitServiceAdapter;
    
    @Autowired
	private ApportionInfoQueryService apportionInfoQueryService;
    
    @Autowired
    private PermUserService permUserService;
    
    @Autowired
    private OrdRefundItemService ordRefundItemService;
    
    @Autowired
    private IOrdRefundmentLogService ordRefundmentLogService;
    
    @Autowired
    private SuperComMessageClientServiceAdapter popupHintService;

	/**
	 * 分摊信息服务
	 * */
	@Resource
	private OrdOrderCostSharingItemService orderCostSharingItemService;
	/**
	 * 子单分摊情况服务
	 * */
	@Resource
	private OrderItemApportionStateService orderItemApportionStateService;

	@Resource
	private IOrdOrderItemService ordOrderItemService;
	
	@Autowired
    private OrdRefundBussinessService OrdRefundBussinessServiceRemote;
	
	@Autowired
	OrdMulPriceRateClientService ordMulPriceRateClientService;
	
	@Resource
	private IVstOrderRouteService vstOrderRouteService;
	
	@Autowired
	private OrdRefundApplyService ordRefundApplyService;
	
	@Autowired
    private PCOrderService pcOrderService;

	@Resource
	private TopicMessageProducer notifyCrmForO2OrderMessageProducer;

	@Autowired
	private PromotionService promotionService;

	@Autowired
	private VstOrdOrderStatusService vstOrdOrderStatusService;
	
	@Autowired
	private com.lvmama.comm.bee.service.ord.OrderService orderServiceProxy;

	@Autowired
	private IOrderRouteService orderRouteService;
	
	@Autowired
	private IApiOrderWorkflowService apiOrderWorkflowService;

	@Autowired
	private OrdDepositRefundAuditService ordDepositRefundAuditService;

	@Autowired
	private OrdSaleServiceService ordSaleServiceService;
	
	@Autowired
	private IApiOrderApportionService apiOrderApportionService;
	
	@Autowired
	private CouponOrderService couponOrderService;

	@Autowired
	private IApiOrderCancelProcessService apiOrderCancelProcessService;

	@Autowired
	private IOrdOrderItemExtendService ordOrderItemExtendService;
	@Autowired
	private IOrder2RouteService order2RouteService;
	@Autowired
	private IApiOrdProcessKeyService apiOrdProcessKeyService;
	@Autowired
	private IApiSupplierInquiryService apiSupplierInquiryService;
	
	@Autowired
	private OrdOrderClientExtendService ordOrderClientExtendService;
	@Autowired
	private IOrdProcessKeyService ordProcessKeyService;

    @Autowired
    private IApiOrderItemCancelProcessService apiOrderItemCancelProcessService;
	
	@Override
	public ResultHandleT<OrdOrder> createOrder(BuyInfo buyInfo, String operatorId) {
		OrderContextCache.setContextFlag();
		//身份证输入规范中的字母为大写，所以在此统一转为大写
		if(buyInfo != null){
			List<Person> travellers = buyInfo.getTravellers();
			if(travellers != null && travellers.size() > 0){
				for(Person person : travellers){
					if(person != null && !StringUtil.isEmptyString(person.getIdNo())){
						person.setIdNo(person.getIdNo().toUpperCase());
					}
				}
			}
			//若存在导游身份证号也转为大写
			Person guide=buyInfo.getGuide();
			if(null!=guide && !StringUtil.isEmptyString(guide.getIdNo())){
				guide.setIdNo(guide.getIdNo().toUpperCase());
			}
		}
		
        Long startCreateOrderTime = System.currentTimeMillis();
        Long startTime = null;
        String methodName = "OrdOrderClientServiceImpl#createOrder==>"+buyInfo.getProductId();
		ResultHandleT<OrdOrder> handle = null;
		//是否成功生成订单                                            
		boolean isOrderCreated = false;
		LOG.info(methodName + "createOrder下单方法中接收到的JSON:"+ GsonUtils.toJson(buyInfo));
		lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.CREATE_ORDER.name(), buyInfo.getUserNo(), LvmmLogEnum.BUSSINESS_TAG.USER.name(), "createOrder下单方法中接收到的JSON", methodName + "createOrder下单方法中接收到的JSON:"+ GsonUtils.toJson(buyInfo));

		PromForbidKeyPo promForbidBuyOrder=null;
		if (orderPromServiceImpl != null) {
			 promForbidBuyOrder = orderPromServiceImpl.isPromForbidBuyOrder(buyInfo);
			if (promForbidBuyOrder != null) {

				if (promForbidBuyOrder.isIsforbid()) {
					handle = new ResultHandleT<OrdOrder>();
					if (StringUtils.isNotBlank(promForbidBuyOrder.getMsg())) {
						handle.setMsg(promForbidBuyOrder.getMsg());
						return handle;
					}
					handle.setMsg("抱歉，您的订单不满足限购要求，下单失败！");
					return handle;
				}
			}
		}else{
			LOG.info("is null forbid buy");
		}
		try {
            //OrdOrder temOrder = this.bookService.initOrderAndCalc(buyInfo);
			OrdOrder temOrder = this.bookService.initOrderBasic(buyInfo);
            
            lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.INIT_ORDER.name(), buyInfo.getUserNo(), LvmmLogEnum.BUSSINESS_TAG.USER.name(), "OrdOrderClientServiceImpl-订单初始化成功", "OrdOrderClientServiceImpl-订单初始化成功");
            
            LOG.info("isDisneyOrder:" + DisneyUtils.isDisney(temOrder));
            if(buyInfo.getDistributionId() != Constant.DIST_BACK_END && temOrder != null && DisneyUtils.isDisney(temOrder) && !checkLimit(buyInfo)){
                handle = new ResultHandleT<OrdOrder>();
                handle.setMsg("由于当前预订人数较多，请您稍后再试！");
                return handle;
            }
            
            //锁仓前置订单下单校验
			String isPreLockSeat = buyInfo.getIsPreLockSeat();
			String lockSetOrderId = buyInfo.getLockSetOrderId();
			if(StringUtils.isNotBlank(isPreLockSeat)&&"true".equals(isPreLockSeat)){
				boolean hasApiFlight = false;
				for (OrdOrderItem orderItem : temOrder.getOrderItemList()) {
					if(orderItem.isApiFlightTicket()){
						hasApiFlight=true;
						break;
					}
				}
				if(hasApiFlight){
					if(StringUtils.isBlank(lockSetOrderId)){
						 handle = new ResultHandleT<OrdOrder>();
			             handle.setMsg("前置锁仓校验失败！");
			             return handle;
					}
					Map<String, String> map=new HashMap<String, String>();
					JSONObject json = JSONObject.fromObject(lockSetOrderId);
	            	map=(Map<String, String>) JSONObject.toBean(json,Map.class);
					for (OrdOrderItem orderItem : temOrder.getOrderItemList()) {
						if(orderItem.isApiFlightTicket()){
							String orderItemId = map.get(orderItem.getSuppGoodsId().toString());
							if(StringUtils.isBlank(orderItemId)){
								 handle = new ResultHandleT<OrdOrder>();
					             handle.setMsg("前置锁仓校验失败！");
					             return handle;
							}
						}
					}
					Long orderId = Long.valueOf(map.get("0"));
					if(orderId==null){
						 handle = new ResultHandleT<OrdOrder>();
			             handle.setMsg("前置锁仓校验失败！");
			             return handle;
					}
					String token = buyInfo.getToken();
					if(StringUtils.isBlank(token)){
						 handle = new ResultHandleT<OrdOrder>();
			             handle.setMsg("前置锁仓校验失败！");
			             return handle;
					}
					String lockSeatOrderId = MemcachedUtil.getInstance().get(token);
					if(StringUtils.isBlank(lockSeatOrderId)||!lockSeatOrderId.equals(lockSetOrderId)){
						 handle = new ResultHandleT<OrdOrder>();
			             handle.setMsg("前置锁仓校验失败！");
			             return handle;
					}
					MemcachedUtil.getInstance().remove(token);
				}
			}else{
				if(StringUtils.isNotBlank(lockSetOrderId)){
					 handle = new ResultHandleT<OrdOrder>();
		             handle.setMsg("前置锁仓校验失败！");
		             return handle;
				}
			}
			
			buyInfo.setSubmitOrderFlag(true);
			//订单退改金额计算
            Long startCancelOrderTime = System.currentTimeMillis();
			Long deductAmount=orderPriceService.cancelOrderDeductAmount(buyInfo);
			LOG.info(ComLogUtil.printTraceInfo(methodName,"退改金额计算", "orderPriceService.cancelOrderDeductAmount", System.currentTimeMillis() - startCancelOrderTime));
			
			lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.CANCEL_ORDER_DEDUCT_AMOUNT.name(), buyInfo.getUserNo(), LvmmLogEnum.BUSSINESS_TAG.USER.name(), "退改金额计算成功", "退改金额计算成功，deductAmount："+deductAmount+",用时："+(System.currentTimeMillis() - startCancelOrderTime));
			
			buyInfo.setGuaranteeRate(deductAmount);
			//检查库存
			startTime = System.currentTimeMillis();
			ResultHandle stockHandle = checkStock(buyInfo);
			LOG.info(ComLogUtil.printTraceInfo(methodName,"库存检查", "this.checkStock", System.currentTimeMillis() - startTime));

            
			if(stockHandle.isFail()){
		        String stockHandleErrorCode = stockHandle.getErrorCode();

                if (StringUtil.isNotEmptyString(stockHandleErrorCode)) {
                    handle = new ResultHandleT<OrdOrder>();
                    handle.setErrorCode(stockHandleErrorCode);
                    handle.setMsg(stockHandle.getMsg());
                    return handle;
                }
				throw new IllegalArgumentException(stockHandle.getMsg());
			}			
			lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.STOCK_CHECK.name(), buyInfo.getUserNo(), LvmmLogEnum.BUSSINESS_TAG.USER.name(), "库存检查成功", "库存检查成功，用时："+(System.currentTimeMillis() - startTime));
			Coupon coupon=null;
			String code=null;
			boolean useCoupon = CollectionUtils.isNotEmpty(buyInfo.getCouponList());
			String youhuiType = buyInfo.getYouhui();
			/*************************新版支付页优惠券、现金、奖金、礼品卡、储值卡 *******/
			startTime = System.currentTimeMillis();
			//无线转换优惠券
			if(ORDER_FAVORABLE_TYPE.coupon.getCode().equals(youhuiType)){
				if(buyInfo.getCouponList()!=null){
					List<UserCouponVO> listCoupon = new ArrayList<>();
					for (Coupon couponOld : buyInfo.getCouponList()) {
						UserCouponVO vo = new UserCouponVO();
						vo.setCouponCode(couponOld.getCode());
						listCoupon.add(vo);
					}
					if(useCoupon){
						LOG.info("==========cm============wuxianyouhuiquantongyiUserCouponVoList");
						buyInfo.setUserCouponVoList(listCoupon);
					}
					LOG.info("==========cm============wuxianyouhuiquantongyiCouponVoList"+"");
					
				}
			}

			String checkResult=this.checkOrderForPayOther(buyInfo);
			LOG.info("==========cm============checkResult:"+checkResult);

			if(checkResult!=""){
				throw new IllegalArgumentException(checkResult);
			}
			
			lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.COUPON_CHECK.name(), buyInfo.getUserNo(), LvmmLogEnum.BUSSINESS_TAG.USER.name(), "新版支付页优惠券、现金、奖金、礼品卡、储值卡验证通过", "新版支付页优惠券、现金、奖金、礼品卡、储值卡验证通过");
            
			/*************************新版支付页优惠券、现金、奖金、礼品卡、储值卡验证结束******/
			LOG.info("==========cm============youhuiType:"+youhuiType);
			if(StringUtils.isNotEmpty(youhuiType)){
                Long startCountPriceTime = System.currentTimeMillis();
                buyInfo.setPromBuyFlag(false);
				PriceInfo info = orderPriceService.countPriceBase(buyInfo);
                Log.info(ComLogUtil.printTraceInfo(methodName,"价格计算", "orderPriceService.countPrice", System.currentTimeMillis() - startCountPriceTime));

				if(ORDER_FAVORABLE_TYPE.coupon.getCode().equals(youhuiType)){
					if(useCoupon){
						coupon=buyInfo.getCouponList().get(0);
						code=coupon.getCode();
						LOG.info("==========cm============useCoupon code:"+code);
						if (!StringUtils.isEmpty(code)) {//有优惠劵
							if(CollectionUtils.isNotEmpty(info.getCouponResutHandles())){
								List<ResultHandle> rs = info.getCouponResutHandles();
								StringBuilder str = new StringBuilder("优惠券使用异常");
								for (ResultHandle resultHandle : rs) {
									if( null == resultHandle ){
										continue;
									}
									String msg = resultHandle.getMsg();
									str.append("[").append(msg).append("]");
								}
								throw new IllegalArgumentException(str.toString());
							}
						}
						
//						UserCouponVO vo = new UserCouponVO();
//						vo.setCouponCode(code);
//						buyInfo.getUserCouponVoList().add(vo);
//						LOG.info("==========cm============UserCouponVO:"+vo);
					}
				}

				if(ORDER_FAVORABLE_TYPE.bonus.getCode().equals(youhuiType)){
					Float userBonus = buyInfo.getBonusYuan();
					if(userBonus!=null&&userBonus>0){

						//最大可抵扣数
						Long maxBonus = info.getMaxBonus();
						Float maxBonusYuan = PriceUtil.convertToYuan(maxBonus);
						if(userBonus>maxBonusYuan){
							throw new IllegalArgumentException("超过最高奖金抵扣数");
						}
					}
				}
			}

            Long startBookingCreateOrderTime = System.currentTimeMillis();
            
            

            //生成订单，并扣减预控资源
            //---------------------------------
            // 判断购买人和紧急联系人包含游玩人名称中是否包含“测试下单”关键字，包含测试关键字时，将该订单标识为测试订单，标识设置成Y。默认是N
       	 //获取测试订单
       	//得到定义的订单测试的常量
   	 		char isTest='N';
   	 		try {
   	 			isTest = buyInfo.getIsTestOrder();
   	 			} catch (NullPointerException e) {

   	 			}
   	 		if(isTest!='Y'){
   	 			String orderValue=Constant.getInstance().getOrderValue();
   	 		    LOG.info("静态变量的值"+orderValue);
   	 			String  contactName="";
   	 		    String  emergencyPersonName="";
   	 		    try {
   	 					contactName = buyInfo.getContact().getFullName();
   	 			    } catch (NullPointerException e) {
   	 					}
   	 			try {
   	 					emergencyPersonName = buyInfo.getEmergencyPerson().getFullName();
   	 			} catch (NullPointerException e) {

   	 			}
   	 		     // 如果购买人和联系人都不包含测试下单，那么就在游玩人中查找是否包含。直接包含直接设置成测试订单的标记。
   	 			if (contactName.contains(orderValue)||emergencyPersonName.contains(orderValue)) {
   	 				buyInfo.setIsTestOrder('Y');
   	 			} else{
 					try {
							List<Person> travellers = buyInfo.getTravellers();
						    for (Person person : travellers) {
 							    if (person.getFullName().contains(orderValue)) {
   	 								buyInfo.setIsTestOrder('Y');
   	 								break;
 								}
						    }
 					} catch (NullPointerException e) {
 					}
 				 }
   	 		}
       	 	LOG.info("createOrder设置了istsestorderbuyInfo的itsest值"+buyInfo.getIsTestOrder()+"值在中间");
            //--------------------------------

			handle = bookService.createOrder(buyInfo, operatorId);
			/*目的地 意外险后置 需要后置的游玩人 保存 开始*/
            if (null != handle && handle.isSuccess() && null != handle.getReturnContent()) {
                OrdOrder order = handle.getReturnContent();
                //判断并设置意外险后置订单；若需要后置，设置订单后置信息
				Long startInitDely = System.currentTimeMillis();
                orderInitService.initDestBuAccTravDelayed(buyInfo, order);
				LOG.info(" orderId="+order.getOrderId()+" initDestBuAccTravDelayed耗时"+(System.currentTimeMillis() - startInitDely));
                OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(order.getOrderId());
                
                this.logTraceNumber(order);
                
                if (null != ordAccInsDelayInfo) {
                    //计算需要补充的游玩人，并保存
                    String travDelayFlag = ordAccInsDelayInfo.getTravDelayFlag();
                    if (StringUtils.isNotBlank(travDelayFlag) && "Y".equals(travDelayFlag)) {
                        orderInitService.saveDestBuAccTrav(buyInfo, order);
                    }
                }
            }
            /*目的地 意外险后置 需要后置的游玩人 保存 结束*/    
			//TODO 要判断迪士尼产品，初始化订单哈偶不回复
			LOG.info(ComLogUtil.printTraceInfo(methodName,"订单生成接口", "bookService.createOrder", System.currentTimeMillis() - startBookingCreateOrderTime));

			if(handle == null) {
				handle = new ResultHandleT<OrdOrder>();
				handle.setMsg("生成订单失败");
			}
			LOG.info("OrdOrderClientServiceImpl.createOrder: bookService.createOrder,isSuccess=" + handle.isSuccess() + ",msg=" + handle.getMsg());

			if (handle != null && handle.isSuccess() && !handle.hasNull()) {
				isOrderCreated = true;
				LOG.info("OrdOrderClientServiceImpl.createOrder: bookService.createOrder,orderId=" + handle.getReturnContent().getOrderId());
				startTime = System.currentTimeMillis();
				OrdOrder order = queryOrdorderByOrderId(handle.getReturnContent().getOrderId());
				LOG.info(ComLogUtil.printTraceInfo(methodName,"查询单个订单详情", "this.queryOrdorderByOrderId", System.currentTimeMillis() - startTime));

				//增加出团模式标识
				if (order != null && BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId())) {
					updateGroupMainItemContent(order);
				}

				String specialTicket = getSpecialTicket(order);
				LOG.info("specialTicket======="+specialTicket+",OrdOrderClientServiceImpl.createOrder: begin==============");
				if(order.getDistributorId() != Constant.DIST_BACK_END &&  StringUtils.isNotEmpty(specialTicket)) {
					LOG.info(specialTicket+"OrdOrderClientServiceImpl.createOrder: begin==============order is cancel");
					Object[] array = reservationSupplierOrder(order);
	                boolean isSuccess = (Boolean) array[0];
	                if(!isSuccess){
						cancelAfterLockStockFail(order);
						LOG.info(ComLogUtil.printTraceInfo(methodName,specialTicket+"占用库存失败", "this.reservationOrder", System.currentTimeMillis() - startTime));
						handle = new ResultHandleT<OrdOrder>();
						Map<String,String> prmMap = new HashMap<String,String>();
						if(SuppGoods.SPECIAL_TICKET_TYPE.DALI_TICKET.getCode().equals(specialTicket)){
							String idNo = getIdNo(order);
							prmMap.put("idNo", idNo);
						}
						if(SuppGoods.SPECIAL_TICKET_TYPE.AI_PIAO_TICKET.getCode().equals(specialTicket)||SuppGoods.SPECIAL_TICKET_TYPE.YONGLE_SHOW_TICKET.getCode().equals(specialTicket)||SuppGoods.SPECIAL_TICKET_TYPE.ZHI_YOU_BAO_CHECK_TICKET.getCode().equals(specialTicket)){
							String errMsg = (String) array[1];
							prmMap.put("errMsg", errMsg);
						}
						String failMsg = failReservationMsg(specialTicket,prmMap);
						handle.setMsg(failMsg);
						return handle;
					}else{
						LOG.info("OrdOrderClientServiceImpl.createOrder:orderstatus"+order.getOrderStatus());
						LOG.info(specialTicket+"OrdOrderClientServiceImpl.createOrder:  begin==============change order status to normal");

						order.setOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.name());
						order.setCancelCode("");
						order.setReason("");
						try{
							orderSaveService.resetOrderToNormal(order.getOrderId());
						}catch(Exception e){
							LOG.info("orderSaveService.resetOrderToNormal异常"+e.getMessage());
						}
						try {
							/*****************订单添加迪士尼标识开始********************/
							//目的地前台-王伟-订单添加迪士尼标识-更新TAG字段
							boolean updateFlag = orderSaveService.updateTagAndWaitPaymentTime4ShanghaiDisneyOrder(order);
							LOG.info((updateFlag == true ? "订单添加"+specialTicket+"标识成功，orderId:" : "订单添加"+specialTicket+"标识成功，orderId:") + order.getOrderId());
							/*****************订单添加迪士尼标识结束********************/
						} catch (Exception e) {
							LOG.info("orderSaveService.updateTag4ShanghaiDisneyOrder异常"+e.getMessage());
						}
						LOG.info("OrdOrderClientServiceImpl.createOrder:cancelcode"+order.getCancelCode() +"reason -----"+order.getReason());
					}
					LOG.info(specialTicket+"OrdOrderClientServiceImpl.createOrder:  end==============");


				}
				//景点门票下单，保险子订单子订单和游玩人信息关联（排除特卖会渠道下单）
				if(order.getDistributionChannel()!=null&&order.getDistributionChannel()!=107L && order.getDistributionChannel()!=108L && order.getDistributionChannel()!=110L){
				if(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(order.getCategoryId())||
                    BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(order.getCategoryId())||
                    BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(order.getCategoryId())){
                  List<OrdItemPersonRelation> list = new ArrayList<OrdItemPersonRelation>();
                  try{
                      for (OrdOrderItem orderItem : order.getOrderItemList()) {
                         if (BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())) {
                          ProdProduct Product =productClientService.findProdProductById(orderItem.getProductId()).getReturnContent();
                          if (SuppGoodsSaleRe.INSURANCE_TYPE.INSURANCE_730.getCode().equals(Product.getProductType())|| SuppGoodsSaleRe.INSURANCE_TYPE.INSURANCE_736.getCode().equals(Product.getProductType())) {
                            for (OrdPerson ordPerson : order.getOrdTravellerList()) {
                              OrdItemPersonRelation itemPersonRelation = new OrdItemPersonRelation();
                              itemPersonRelation.setOrderItemId(orderItem.getOrderItemId());
                              itemPersonRelation.setOrdPersonId(ordPerson.getOrdPersonId());
                              list.add(itemPersonRelation);
                            }
                          }else if (SuppGoodsSaleRe.INSURANCE_TYPE.INSURANCE_739.getCode().equals(Product.getProductType())|| SuppGoodsSaleRe.INSURANCE_TYPE.INSURANCE_738.getCode().equals(Product.getProductType())) {
                            OrdItemPersonRelation itemPersonRelation = new OrdItemPersonRelation();
                            itemPersonRelation.setOrderItemId(orderItem.getOrderItemId());
                            itemPersonRelation.setOrdPersonId(order.getOrdTravellerList().get(0).getOrdPersonId());
                            list.add(itemPersonRelation);
                          }
                        }
                      }
                      if(list.size()>0){
                      iOrdItemPersonRelationService.insertBatch(list);
                      }
                   }catch(Exception e){
                    LOG.info("iOrdItemPersonRelationService.insertBatch 批量插入保险子订单子订单和游玩人信息关联异常"+e.getMessage());
                 }
                }
				}
				//门票邮寄订单       将需要的信息存到表中    以便ebk查询邮寄订单
				if(order.getDistributorId() != Constant.DIST_BACK_END && order.getAddressPerson() != null){
					for (OrdOrderItem ordOrderItem : handle.getReturnContent().getOrderItemList()){
						//后台下单  实体票     门票/WIFI      寄件方是供应商
						if(ordOrderItem.hasExpresstypeDisplay()
								&&(OrderUtils.isTicketByCategoryId(ordOrderItem.getCategoryId()) || ordOrderItem.getCategoryId() == 28L)
								&&SuppGoods.EXPRESSTYPE.SUPPLIER.name().equals(ordOrderItem.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.express_type.name()))){
							OrdTicketPost post = new OrdTicketPost();
							BeanUtils.copyProperties(ordOrderItem, post);

							//取票人   先取第一游玩人  第一游玩人为空  取联系人
							OrdPerson traveller  = order.getFirstTravellerPerson();
							if(traveller != null){
								post.setFullName(traveller.getFullName());
								post.setMobie(traveller.getMobile());
							}else{
								OrdPerson contact = order.getOnlyContactPerson();
								post.setFullName(contact.getFullName());
								post.setMobie(contact.getMobile());
							}

							OrdPerson addresser = order.getAddressPerson();
							post.setAddressName(addresser.getFullName());
							post.setAddressMobile(addresser.getMobile());

							OrdAddress  address = order.getOrdAddress();
							post.setAddress(StringUtil.coverNullStrValue(address.getProvince())
									+StringUtil.coverNullStrValue(address.getCity())
									+StringUtil.coverNullStrValue(address.getDistrict())
									+StringUtil.coverNullStrValue(address.getStreet()));


							try {
								ordTicketPostService.insertOrdTicketPost(post);
							} catch (Exception e) {
								LOG.info("ordTicketPostService.insertOrdTicketPost error:"+e.getMessage());
							}
						}
					}
				}

				if(!order.hasCanceled()) {
					
					LOG.info("OrdOrderClientServiceImpl.hasCanceled: ============="+order.hasCanceled());
					 
			        try {
			        	// 分销单酒店下单且目的地BU的单酒店，且渠道为手工渠道 资源审核自动通过
						calDistributorOrderResourceStatus(order);
					}  catch (Exception e) {
						LOG.error("OrdOrderClientServiceImpl.calDistributorOrderResourceStatus error:{}",e);
					}
			        
//					List<OrdOrderItem> listItem=new ArrayList<OrdOrderItem>();
//					OrdOrderItem item=null;
//					for (OrdOrderItem ordOrderItem : handle.getReturnContent().getOrderItemList()) {
//						item =new OrdOrderItem();
//						BeanUtils.copyProperties(ordOrderItem, item);
//						listItem.add(item);
//
//					}
			        handle.setReturnContent(order);
					

//					boolean isLockSeatSuccess = true;

					/*if(order.getDistributorId() != Constant.DIST_BACK_END) {
	                    Long startLockSeatTime = System.currentTimeMillis();
						isLockSeatSuccess = lockSeatForFlightOrderMult(order);
	                    Log.info(ComLogUtil.printTraceInfo(methodName,"对接机票锁舱--并发调用", "flightOrderProcessService", System.currentTimeMillis() - startLockSeatTime));
					}*/

	//				if(!isLockSeatSuccess) {
	//					handle.setMsg("机票锁舱失败，请重新下单或联系客服下单");
	//					cancelAfterLockSeatFail(order);
	//				} else {
					/*	if (useCoupon && handle.getReturnContent().hasNeedPrepaid()
								&& coupon != null && !StringUtils.isEmpty(code)
								&& ORDER_FAVORABLE_TYPE.coupon.getCode().equals(youhuiType)) {
		//					fillOrderData(order, buyInfo);
							//老的下单页使用优惠券的，后续等新版全部上线后删除
							startTime = System.currentTimeMillis();

							order.setOrderDfp(buyInfo.getOrderDfp());
							orderUpdateService.updateOrderUsedFavor(order, code);

							LOG.info(ComLogUtil.printTraceInfo(methodName,"订单优惠劵使用后 依据订单优惠策略算出优惠金额，操作订单相关操作",
											"orderUpdateService.updateOrderUsedFavor", System.currentTimeMillis() - startTime));

						}*/
						LOG.info("==========cm============老的下单页使用优惠券的，后续等新版全部上线后删除");
						//新优惠券 奖金、现金、礼品卡、储值卡使用修改订单应付金额
						startTime = System.currentTimeMillis();
						String resultStr = orderUpdateService.updateOrderForBuyInfo(order, buyInfo);
						LOG.info("==========cm============resultStr:"+resultStr);
						LOG.info(ComLogUtil.printTraceInfo(methodName,"新优惠券 奖金、现金、礼品卡、储值卡使用修改订单应付金额",
											"orderUpdateService.updateOrderForBuyInfo", System.currentTimeMillis() - startTime));

						lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.COUPON_DEDUCT.name(), order.getOrderId(), LvmmLogEnum.BUSSINESS_TAG.ORD_ORDER.name(), "优惠券 奖金、现金、礼品卡、储值卡扣减成功", "优惠券 奖金、现金、礼品卡、储值卡扣减成功");
				           
						//分摊订单数据
//						orderAmountApportionService.calcAndSaveBookingApportionAmount(order.getOrderId());
						//添加订单id到分摊仓库表
					Long orderId = order.getOrderId();
					LOG.info("Order created, now add order " + orderId + " to apportion table");
					if (ApportionUtil.isApportionEnabled()) {
						orderAmountApportionService.addToOrderApportionDepot(orderId);
						LOG.info("Order " + orderId + " added to apportion table");
					} else {
						LOG.info("Apportion key is not enable, order " + orderId + " will not add to apportion table");
					}

						if(StringUtil.isNotEmptyString(resultStr)){
							cancelOrder(order.getOrderId(), OrderEnum.CANCEL_CODE_TYPE.OTHER_REASON.name(), resultStr+"废单", "SYSTEM", "");
							handle.setMsg("下单失败：" + resultStr);
							isOrderCreated = false;
						}
						//Thread.sleep(10*1000);
						LOG.info("OrdOrderClientServiceImpl.createOrder(orderID=" + handle.getReturnContent().getOrderId() + "): send OrderCreateMessage");
						startTime = System.currentTimeMillis();

						try{
							orderMessageProducer.sendMsg(MessageFactory.newOrderCreateMessage(handle.getReturnContent().getOrderId()));
							
							lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.CREATE_ORDER.name(), orderId, LvmmLogEnum.BUSSINESS_TAG.ORD_ORDER.name(), "发送创建订单消息", "发送创建订单消息成功");
				            
						}catch(Exception ex){
							LOG.info("sendMsg Error",ex);
						}
						LOG.info(ComLogUtil.printTraceInfo(methodName,"推送消息", "orderMessageProducer.sendMsg", System.currentTimeMillis() - startTime));

						if(LOG.isDebugEnabled()){
							LOG.info("api::::::::::"+order.getMainOrderItem().hasSupplierApi());
							LOG.info("distrion:::::::::"+(order.getDistributorId()==2L));
						}
						
						if(!dynamicPropertiesFactory.isAsyncGenWorkflow(order)){
							Map<String,Object> params = new HashMap<String, Object>();
							//订单对象中缓存是否发送合同的标示，在工作流中根据此标示决定是否发送合同。
							order.setSendContractFlag(buyInfo.getSendContractFlag());
							try{
								LOG.info("-----@orderId="+orderId+"--------------@---order.getDistributorId()"+order.getDistributorId()+"----getProcessKey:"+handle.getReturnContent().getProcessKey()+"--"+ActivitiUtils.createOrderBussinessKey(order) + "--buCode:" + order.getBuCode());
								startTime = System.currentTimeMillis();
								LOG.info("orderId=" + orderId + ",newWorkflowFlag=" + order.getNewWorkflowFlag());
								if (VstRouteConstants.FLAG_S.equals(order.getNewWorkflowFlag())) {
									OrderVo vo = initOrderVo(order);
									params.put("orderId", vo.getOrderId());
									params.put("mainOrderItem", vo.getMainOrderItem());
									params.put("order", vo);
									//新工作流
									String processKey = getOrdProcessKey(orderId, OrdProcessKeyEnum.KEY_TYPE.approve.name());
									LOG.info("orderId=" + orderId + ",审核流程key=" + processKey);
									if (StringUtils.isNotEmpty(processKey)) {
										String businessKey = BusinessKeyCreator.createOrderBusinessKey(BusinessKeyCreator.WORKFLOW_TYPE.approve.name(), orderId);
										WorkflowStarterVo startVo = new WorkflowStarterVo(orderId, processKey, businessKey, BusinessKeyCreator.WORKFLOW_TYPE.approve.name(), params);
										ResponseBody<String> resp = apiOrderWorkflowService.startProcess(new RequestBody<WorkflowStarterVo>(startVo));
										if (null == resp || resp.isFailure()) {
											LOG.info("启动审核工作流失败:orderId=" + order.getOrderId() + ",原因:" + resp.getErrorMessage());
										}
									}
								} else {
									fillStartProcessParam(order, params);
//								boolean isSingleHotel=vstOrderRouteService.isRouteToNewWorkflowBySingleHotel(order);
//								Log.info("isSingleHotel={},orderId={}",orderId,isSingleHotel);
//								if(!isSingleHotel){
									String processId = processerClientService.startProcesser(handle.getReturnContent().getProcessKey(),ActivitiUtils.createOrderBussinessKey(order), params);
									Log.info(ComLogUtil.printTraceInfo(methodName,"启动订单流传流程", "processerClientService.startProcesser", System.currentTimeMillis() - startTime));

									startTime = System.currentTimeMillis();
									comActivitiRelationService.saveRelation(handle.getReturnContent().getProcessKey(), processId, handle.getReturnContent().getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
									Log.info(ComLogUtil.printTraceInfo(methodName,"保存工作流信息", "comActivitiRelationService.saveRelation", System.currentTimeMillis() - startTime));
									lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.WORK_FLOW_START.name(), handle.getReturnContent().getOrderId(), LvmmLogEnum.BUSSINESS_TAG.ORD_ORDER.name(), "工作流启动成功", "工作流启动成功");
//								}
								}

								LOG.info("order.hasNeedPrepaid="+order.hasNeedPrepaid());
								if(order.hasNeedPrepaid() && order.getDistributorId() != Constant.DIST_BACK_END){
									if(order.getOughtAmount()==0&&!order.isNeedResourceConfirm()){//操作0元支付
										LOG.info("ordPrePayServiceAdapter.vstOrder0YuanPayMsg()"+order.getOrderId());
										ordPrePayServiceAdapter.vstOrder0YuanPayMsg(order.getOrderId());
									}
								/*	//因为老支付接口不能使用，查无结果，为了保证功能，暂使用新接口支付
									else if(order.getBonusAmount()>0){
										LOG.info("payFromBonusForVstOrder params:userId"+order.getUserId()+",orderId="+order.getOrderId()+",bonusAmount="+order.getBonusAmount());
										payFromBonusForVstOrder(order.getUserId(), order.getOrderId(), order.getBonusAmount());
									}*/
								}
								
					            
							}catch(Exception exx){
								cancelOrder(order.getOrderId(), OrderEnum.CANCEL_CODE_TYPE.OTHER_REASON.name(), "生成流程错误废单", "SYSTEM", "");
								isOrderCreated = false;
								throw exx;
	
							}
						}else{ // 工作流异步
							orderWorkflowMessageProducer.sendMsg(MessageFactory.newOrderAscProcessMessage(handle.getReturnContent().getOrderId()));
							
							lvmamaLog.infoLogicEventLog(LvmmLogEnum.ORDER_BUSSINESS_CODE.WORK_FLOW_START.name(), handle.getReturnContent().getOrderId(), LvmmLogEnum.BUSSINESS_TAG.ORD_ORDER.name(), "工作流异步发送消息", "工作流异步启动发送消息成功");
				            
						}
//				}
					//生成订单权限
					startTime = System.currentTimeMillis();
					synManagerIdPerm(order);
					Log.info(ComLogUtil.printTraceInfo(methodName,"同步订单权限", "this.synManagerIdPerm", System.currentTimeMillis() - startTime));

//					if(order.getDistributorId()==2L){
//						order.setOrderItemList(listItem);//设置itemlist用于传递orderitem和buyinfo中item的对应关系
//					}
					//设置下单公告
					LOG.info("-----create order start method setOrderNotice----");
					setOrderNotice(order);
				}
				//订单跟踪信息保存
				if(handle.getReturnContent().getCategoryId()==11L||handle.getReturnContent().getCategoryId()==12L||handle.getReturnContent().getCategoryId()==13L){
					OrdOrderTracking orderTracking = new OrdOrderTracking();
					orderTracking.setOrderId(handle.getReturnContent().getOrderId());
					orderTracking.setOrderStatus(OrderEnum.ORDER_TRACKING_STATUS.UNPAY.getCode());
					orderTracking.setChangeStatusTime(new Date());
					orderTracking.setCreateTime(new Date());
					orderTracking.setCategoryId(handle.getReturnContent().getCategoryId());
					ordOrderTrackingService.saveOrderTracking(orderTracking); //保存未支付状态
					LOG.info("create orderTracking start method saveOrderTracking:"+orderTracking.getOrderId()+","+orderTracking.getOrderStatus());
					if(order.getPaymentTarget().equals(SuppGoods.PAYTARGET.PAY.name())){ //如果为到付
						orderTracking.setOrderStatus(OrderEnum.ORDER_TRACKING_STATUS.CREDITED.getCode());
						orderTracking.setChangeStatusTime(order.getVisitTime());
						orderTracking.setCreateTime(new Date());
						ordOrderTrackingService.saveOrderTracking(orderTracking); //保存凭证已经生成
						LOG.info("create orderTracking start method saveOrderTracking:"+orderTracking.getOrderId()+","+orderTracking.getOrderStatus());
					}
				}
				//限购存redis
				saveLimitationsRedis(promForbidBuyOrder);
			}

		} catch (OrderException ex){
            handle = new ResultHandleT<OrdOrder>();
            String errorCode = ex.getErrorCode();
            if (StringUtil.isNotEmptyString(errorCode)) {
                handle.setErrorCode(errorCode);
            }
            handle.setMsg("下单失败：" + ex.getMessage());
            LOG.info("OrdOrderClientServiceImpl.createOrder: Exception msg=" + ex.getMessage());
            
            //记录响应日志
            LvmmLogEnum.recordLvmmLog(ex,buyInfo.getUserNo(),LvmmLogEnum.BUSSINESS_TAG.USER.name());
            
            ex.printStackTrace();
	    } catch (IllegalArgumentException ex){
			handle = new ResultHandleT<OrdOrder>();
			handle.setMsg("下单失败：" + ex.getMessage());
			
			//记录响应日志
            LvmmLogEnum.recordLvmmLog(ex,buyInfo.getUserNo(),LvmmLogEnum.BUSSINESS_TAG.USER.name());
			
			ex.printStackTrace();
		} catch (BusinessException ex){
			handle = new ResultHandleT<OrdOrder>();
			handle.setMsg("下单失败：" + ex.getMessage());
			
			//记录响应日志
            LvmmLogEnum.recordLvmmLog(ex,buyInfo.getUserNo(),LvmmLogEnum.BUSSINESS_TAG.USER.name());
			
			ex.printStackTrace();
		} catch (Exception ex){
			handle = new ResultHandleT<OrdOrder>();
			handle.setMsg("下单失败：系统内部异常，请重新下单或联系客服下单");
			LOG.info("----Xiaojing111111111111111----");
			LOG.error(ex.getMessage());
			
			//记录响应日志
            LvmmLogEnum.recordLvmmLog(ex,buyInfo.getUserNo(),LvmmLogEnum.BUSSINESS_TAG.USER.name());
			
			ex.printStackTrace();
		}
		//通过拦截器放入的MD5码作为key放入orderId
		if(handle.isSuccess()){
			if(StringUtils.isNotEmpty(buyInfo.getOrderRepeatedMd5())){
				MemcachedUtil.getInstance().set(buyInfo.getOrderRepeatedMd5(), handle.getReturnContent().getOrderId());
			}
		}
		LOG.info("end create order...");
        Log.info(ComLogUtil.printTraceInfo(methodName,"创建订单整个方法耗时", "", System.currentTimeMillis() - startCreateOrderTime));
        // 记录订单状态码*************************************************************************************************************************************
        if(handle != null){
            recordOrdOrderStatus(handle);
        }
        //酒店异步更新訂單子項content字段
        if(handle!=null && handle.isSuccess()  &&  !handle.hasNull() && isCategoryHotel(handle.getReturnContent())){
        	updateOrdOrderItemContent(handle);
        }
        if(handle.getReturnContent()!=null){
			LOG.info("return create order,success:"+handle.isSuccess()+",orderid:"+handle.getReturnContent().getOrderId());
		}else{
			LOG.info("return create order,success:"+handle.isSuccess()+",msg:"+handle.getMsg());
		}
        //前置锁仓订单下单失败后取消锁仓
        try {
			if(handle == null || handle.isFail()){
				if(StringUtils.isNotBlank(buyInfo.getIsPreLockSeat())&&"true".equals(buyInfo.getIsPreLockSeat())){
					if(StringUtils.isNotBlank(buyInfo.getLockSetOrderId())){
						Map<String, String> map=new HashMap<String, String>();
						JSONObject json = JSONObject.fromObject(buyInfo.getLockSetOrderId());
				    	map=(Map<String, String>) JSONObject.toBean(json,Map.class);
				    	for (Map.Entry<String, String> entry : map.entrySet()) {
							if(!"0".equals(entry.getKey())){
								 String value = entry.getValue();
								 flightOrderProcessService.cancelOrderNotify(Long.valueOf(value));
							}
						}
					}
				}
				
			}
		} catch (Exception e) {
			LOG.info("前置锁仓订单下单失败后取消锁仓失败"+e.getMessage());
		}
		return handle;
	}
	
	private OrderVo initOrderVo(OrdOrder order) {
		OrderVo vo = new OrderVo();
		EnhanceBeanUtils.copyProperties(order, vo);
		this.initOrderItemSubProcessKey(vo);
		this.initMainOrderItemVo(vo);
		return vo;
	}

	private void initMainOrderItemVo(OrderVo orderVo) {
		if(orderVo != null && CollectionUtils.isNotEmpty(orderVo.getOrderItemList())) {
			for (OrderItemVo item : orderVo.getOrderItemList()) {
				if("true".equalsIgnoreCase(item.getMainItem())) {
					orderVo.setMainOrderItem(item);
				}
			}
		}
	}

	private void initOrderItemSubProcessKey(OrderVo vo) {
		if (vo != null && CollectionUtils.isNotEmpty(vo.getOrderItemList())) {
			Map<Long, OrderItemVo> itemMap = new HashMap<Long, OrderItemVo>();
			for (OrderItemVo item : vo.getOrderItemList()) {
				itemMap.put(item.getOrderItemId(), item);
			}
			List<OrdProcessKey> list = getOrdProcessKeyById(itemMap.keySet(),OrdProcessKeyEnum.OBJECT_TYPE.ORD_ORDER_ITEM.name());
			if (list != null) {
				for (OrdProcessKey resKey : list) {
					OrderItemVo item = itemMap.get(resKey.getObjectId());
					if (resKey.getKeyType().equals(OrdProcessKeyEnum.KEY_TYPE.payment.name()))
						item.setPaymentSubProcessKey(resKey.getKeyValue());
					if (resKey.getKeyType().equals(OrdProcessKeyEnum.KEY_TYPE.approve.name()))
						item.setApproveSubProcessKey(resKey.getKeyValue());
					if (resKey.getKeyType().equals(OrdProcessKeyEnum.KEY_TYPE.cancel.name()))
						item.setCancelSubProcessKey(resKey.getKeyValue());
				}
			}
		}
	}
	
	private List<OrdProcessKey> getOrdProcessKeyById(Set<Long> objectIdList, String objectType) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectIdList", objectIdList);
		params.put("objectType", objectType);
		return ordProcessKeyService.selectOrdProcessKeyList(params);
	}

	private String getOrdProcessKey(Long objectId, String keyType) {
		OrdProcessKey ordProcessKey = new OrdProcessKey(objectId, OrdProcessKeyEnum.OBJECT_TYPE.ORD_ORDER.name(), keyType);
		List<OrdProcessKey> list = ordProcessKeyService.query(ordProcessKey);
		return CollectionUtils.isNotEmpty(list) ? list.get(0).getKeyValue() : null;
	}

	/**
 * 异步更新content快照
 * 
 * @param handle
 */
  @Async
  private void updateOrdOrderItemContent(ResultHandleT<OrdOrder> handle) {
    	OrdOrder ordOrder = handle.getReturnContent();
    	 try {
			 ProdProduct prodProduct = null;
			 Object productBasicCache = MemcachedUtil.getInstance().get(Constant.MEM_CACH_KEY.HOTELS_VST_PRODUCT_BASIC_.toString()+ordOrder.getProductId());
				if (null != productBasicCache) {
					prodProduct = (ProdProduct) productBasicCache;
					if(LOG.isDebugEnabled()){
						LOG.debug("get Basic data of hotel from MemCache");
					}
				} else {
					prodProduct = prodProductClientService.findHotelProduct4Front(ordOrder.getProductId(), Boolean.TRUE,Boolean.TRUE).getReturnContent();
				}
			 
			 
			if(prodProduct!=null){
				ProdProductAddtional productAddtional = prodProductAdditionClientService.selectByPrimaryKey(prodProduct.getProductId());
				prodProduct.setProductAddtional(productAddtional);
			}		
				
			 List<OrdOrderItem>ordOrderItems=ordOrder.getOrderItemList();
			 if(CollectionUtils.isNotEmpty(ordOrderItems)){ 
				 OrdOrderItem ordOrderItem =ordOrderItems.get(0);
				 ordOrderItem.putContent(OrderEnum.HOTEL_CONTENT.telephone.name(),prodProduct.getPropValue().get("telephone") );
				 ordOrderItem.putContent(OrderEnum.HOTEL_CONTENT.address.name(), prodProduct.getPropValue().get("address"));	
				 this.updateOrderItem(ordOrderItems.get(0));
				 LOG.info("异步更新订单子项content字段成功。orderitemid="+ordOrderItem.getOrderItemId());
			 }
		} catch (Exception e) {
		   LOG.info("异步更新订单子项content字段失败。orderID="+ordOrder.getOrderId());
		}
    }

	/**
	 * 更新跟团主子单content快照
	 *
	 * @param ordOrder
	 */
	private void updateGroupMainItemContent(OrdOrder ordOrder) {
		try {
			ProdProductParam param = new ProdProductParam();
			param.setProdEcontract(true);
			ProdProduct product = productClientService.findLineProductByProductId(ordOrder.getProductId(), param).getReturnContent();
			if (product != null && product.getProdEcontract() != null && PRODUCTTYPE.FOREIGNLINE.getCode().equals(product.getProductType())) {
				String groupMode = product.getProdEcontract().getGroupMode();
				OrdOrderItem mainOrderItem = ordOrder.getMainOrderItem();
				if(mainOrderItem != null){
					mainOrderItem.putContent(OrderEnum.ORDER_ROUTE_TYPE.group_mode.name(),groupMode);
					this.updateOrderItem(mainOrderItem);
					LOG.info("updateGroupMainItemContent success,orderId="+ordOrder.getOrderId() + ",mainItemId="+mainOrderItem.getOrderItemId());
				}
			}
		} catch (Exception e) {
			LOG.info("updateGroupMainItemContent fail,orderId="+ordOrder.getOrderId());
		}
	}
  
    public String getSpecialTicket(OrdOrder order) {
		LOG.info("check getSpecialTicket-==============" + order.getOrderId());
		String specialTicket = "";
		if (CollectionUtils.isEmpty(order.getOrderItemList())) {
			return specialTicket;
		}
		for (OrdOrderItem item : order.getOrderItemList()) {
			String specialTicketType = item
					.getContentStringByKey("specialTicketType");
			if(StringUtils.isNotEmpty(specialTicketType)){
				return specialTicketType;
			}
		}
		LOG.info("check getSpecialTicket-==============" + order.getOrderId()+ "success+==========" + specialTicket);
		return specialTicket;
	}


	private boolean isDisney(BuyInfo buyInfo){
        //非打包
        if(CollectionUtils.isNotEmpty(buyInfo.getItemList())){
            List<Long> suppGoodsIds = new ArrayList<Long>();
            for(Item item : buyInfo.getItemList()){
                suppGoodsIds.add(item.getGoodsId());
            }
            ResultHandleT<List<SuppGoods>> resultHandleT = suppGoodsClientService.findSuppGoodsByIdList(suppGoodsIds);
            if(resultHandleT != null && CollectionUtils.isNotEmpty(resultHandleT.getReturnContent())){
                for(SuppGoods suppGoods : resultHandleT.getReturnContent()){
                    if(DisneyUtils.isDisney(suppGoods)){
                        return true;
                    }
                }
            }
        }
        //打包
        if(CollectionUtils.isNotEmpty(buyInfo.getProductList())){
            for(Product product : buyInfo.getProductList()){
                List<ProdPackageGroup> groupList = prodPackageGroupClientRemote.getProdPackageGroupByProductId(product.getProductId());
                if(CollectionUtils.isNotEmpty(groupList)){
                    List<ProdPackageDetail> list = prodPackageGroupClientRemote.getProdPackageDetailByGroupId(groupList.get(0).getGroupId());
                    if(CollectionUtils.isNotEmpty(list)) {
                        for(ProdPackageDetail detail : list){
                            ResultHandleT<SuppGoods> resultHandleT = suppGoodsClientService.findSuppGoodsById(detail.getObjectId());
                            if(resultHandleT != null && resultHandleT.getReturnContent() != null){
                                if(DisneyUtils.isDisney(resultHandleT.getReturnContent())){
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean checkLimit(BuyInfo buyInfo) {

        String sessionKey = "KEY_SESSION_TICKET_LOCK_STOCK_LIMIT";
        String key = "KEY_TICKET_LOCK_STOCK_LIMIT";

        //如果memcached中存在key，则说明是一分钟之内
        LOG.info("checkLimit---MemcachedUtil.getInstance().keyExists(key):" + MemcachedUtil.getInstance().keyExists(key));

        if(MemcachedUtil.getInstance().keyExists(key) && MemcachedUtil.getInstance().keyExists(sessionKey)) {
            if(MemcachedUtil.getInstance().decr(sessionKey) <= 0) {
                return false;
            }
        } else {
            String limitNumStr = "";
            String limitNumKey = "MEM_KEY_SESSION_TICKET_LOCK_STOCK_LIMIT";
            if(MemcachedUtil.getInstance().keyExists(limitNumKey) && MemcachedUtil.getInstance().get(limitNumKey) != null){
                limitNumStr = MemcachedUtil.getInstance().get(limitNumKey);
            }else if (Constant.getInstance().getLockStockLimit() != null) {
                limitNumStr = String.valueOf(Constant.getInstance().getLockStockLimit());
            }

            if (StringUtils.isBlank(limitNumStr)) {
                return true;
            }

            Integer limitTime = null;
            String limitTimeKey = "MEM_KEY_SESSION_TICKET_LOCK_TIME_LIMIT";
            if(MemcachedUtil.getInstance().keyExists(limitTimeKey) && MemcachedUtil.getInstance().get(limitTimeKey) != null){
                limitTime = Integer.valueOf((String) MemcachedUtil.getInstance().get(limitTimeKey));
            }else if (Constant.getInstance().getLockTimeLimit() != null){
                limitTime = Constant.getInstance().getLockTimeLimit().intValue();
            }
            if(limitTime == null){
                return true;
            }

            //超过一分钟，重新计数
            MemcachedUtil.getInstance().set(key, limitTime, 1);
            MemcachedUtil.getInstance().get("KEY_LOCK_STOCK_LIMIT");
            MemcachedUtil.getInstance().set(sessionKey, 3600, limitNumStr);
        }

        return true;
    }

	private void fireWorkflowActivate(String methodName, String processKey, OrdOrder order) {
		Long startTime = System.currentTimeMillis();
		Map<String, Object> params = new HashMap<String, Object>();
		fillStartProcessParam(order, params);
		String processId = processerClientService.startProcesser(processKey, ActivitiUtils.createOrderBussinessKey(order), params);
		Log.info(ComLogUtil.printTraceInfo(methodName, "启动订单流传流程", "processerClientService.startProcesser", System.currentTimeMillis() - startTime));

		startTime = System.currentTimeMillis();
		comActivitiRelationService.saveRelation(processKey, processId, order.getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
		Log.info(ComLogUtil.printTraceInfo(methodName, "保存工作流信息", "comActivitiRelationService.saveRelation", System.currentTimeMillis() - startTime));
	}

	/**
	 * 对接机票锁舱
	 * @param order
	 * @return
	 * @throws Exception
	 */
	private boolean lockSeatForFlightOrder(OrdOrder order) throws Exception {
		if(!order.isContainApiFlightTicket()) {
			return true;
		}

		List<OrdOrderItem> orderItemList = order.getOrderItemList();

		if(orderItemList == null) {
			return true;
		}

		for(OrdOrderItem orderItem : orderItemList) {
			if(orderItem.isApiFlightTicket()) {
				ResultHandle result = flightOrderProcessService.lockSeat(orderItem.getOrderItemId());
				if(result == null || result.isFail()) {
					return false;
				}

				//设置资源保留时长
				Date retentionTime = DateUtil.getDateAfterMinutes(DEFAULT_RETENTION_MINUTES);
				String resourceRetentionTime = DateUtil.formatDate(retentionTime, "yyyy-MM-dd HH:mm:ss");
				orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name(), resourceRetentionTime);
				this.updateOrderItem(orderItem);

				//设置支付等待时间
				Date waitPaymentTime = DateUtil.getDateAfterMinutes(DEFAULT_WAIT_MINUTES);
				if(order.getWaitPaymentTime() == null || waitPaymentTime.before(order.getWaitPaymentTime())) {
					order.setWaitPaymentTime(waitPaymentTime);
				}
				this.updateOrdOrder(order);

			}
		}

		return true;
	}


	/**
	 * 对接机票锁舱【并发调用】
	 * @param order
	 * @return
	 * @throws Exception
	 */
	private boolean lockSeatForFlightOrderMult(OrdOrder order) throws Exception {
		if(!order.isContainApiFlightTicket()) {
			return true;
		}

		List<OrdOrderItem> orderItemList = order.getOrderItemList();

		if(orderItemList == null) {
			return true;
		}

		// 机票子订单
		List<OrdOrderItem> apiFlightList = new ArrayList<OrdOrderItem>();
		for(OrdOrderItem orderItem : orderItemList) {
			if(orderItem.isApiFlightTicket()) {
				apiFlightList.add(orderItem);
			}
		}

		// 机票子订单，并发锁仓
		if (CollectionUtils.isNotEmpty(apiFlightList)) {
			ResultHandle result = flightOrderProcessService.lockSeatMult(order, apiFlightList);
			if(result == null || result.isFail()) {
				return false;
			}

			/*
			for(OrdOrderItem orderItem : apiFlightList) {
				//设置资源保留时长
				Date retentionTime = DateUtil.getDateAfterMinutes(DEFAULT_RETENTION_MINUTES);
				String resourceRetentionTime = DateUtil.formatDate(retentionTime, "yyyy-MM-dd HH:mm:ss");
				orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name(), resourceRetentionTime);
				this.updateOrderItem(orderItem);

				//设置支付等待时间
				Date waitPaymentTime = DateUtil.getDateAfterMinutes(DEFAULT_WAIT_MINUTES);
				if(order.getWaitPaymentTime() == null || waitPaymentTime.before(order.getWaitPaymentTime())) {
					order.setWaitPaymentTime(waitPaymentTime);
				}
				this.updateOrdOrder(order);
			}
			*/
		}

		return true;
	}


	/**
	 * 机票锁舱失败后修改订单状态
	 * @return
	 * @throws Exception
	 */
	private void cancelAfterLockSeatFail(OrdOrder order) throws Exception {
		if(order == null) {
			return;
		}

		//将订单置为无效
		this.orderUpdateService.invalidOrder(order.getOrderId());

		String cancelCode = OrderEnum.ORDER_CANCEL_CODE.FLIGHT_LOCKSEAT_FAIL.name();
		String reason = "机票锁舱失败自动废单";
		String operatorId = "SYSTEM";
		//更新订单状态
		ResultHandle resultHandle = orderUpdateService.updateCancelOrder(
				order.getOrderId(),
				OrderEnum.ORDER_CANCEL_CODE.FLIGHT_LOCKSEAT_FAIL.name(),
				reason, "SYSTEM", null);

		if(resultHandle == null || resultHandle.isFail()) {
			LOG.error("取消订单失败，订单ID为:" + order.getOrderId() + ", 错误信息："
					+ (resultHandle == null ? "接口调用异常" : resultHandle.getMsg()));
			return;
		}

		//重设置状态，避免状态覆盖
		order.setOrderStatus(OrderEnum.ORDER_STATUS.CANCEL.name());
		//非后台没有发送订单生成消息，所以不需要发送取消订单消息
		if(order.getDistributorId() == Constant.DIST_BACK_END && resultHandle.isSuccess()) {
			String addition = cancelCode + "_=_" + reason + "_=_" + operatorId;
			orderMessageProducer.sendMsg(MessageFactory.newOrderCancelMessage(order.getOrderId(), addition));
			LOG.info("send OrderCancelMessage");
		}

		//新增预订通知
		this.saveReservation(order.getOrderId());

		String cancelStr = "取消类型："+ OrderEnum.ORDER_CANCEL_CODE.FLIGHT_LOCKSEAT_FAIL.getCnName() +",取消原因：机票锁舱失败自动废单";
		String content = "将编号为["+order.getOrderId()+"]的订单活动变更为["+ OrderEnum.ORDER_STATUS.CANCEL.getCnName() +"]"+cancelStr;

		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
				order.getOrderId(),
				order.getOrderId(),
				"SYSTEM",
				content,
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.name(),
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.getCnName()+"["+ OrderEnum.ORDER_STATUS.CANCEL.getCnName() +"]",
				"");

		order.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.CANCEL.name());

		this.updateOrdOrder(order);

		List<OrdOrderItem> orderItemList = order.getOrderItemList();

		if(orderItemList == null) {
			return;
		}

		for(OrdOrderItem orderItem : orderItemList) {
			if(flightOrderProcessService.isNeedCancelNotify(orderItem.getOrderItemId())) {
				flightOrderProcessService.cancelOrderNotify(orderItem.getOrderItemId());
			}
		}
	}

	/**
	 * 机票锁舱失败后修改订单状态
	 * @return
	 * @throws Exception
	 */
	private void cancelAfterLockStockFail(OrdOrder order) throws Exception {
		if(order == null) {
			return;
		}


		//将订单置为无效
		this.orderUpdateService.invalidOrder(order.getOrderId());

		String cancelCode = OrderEnum.ORDER_CANCEL_CODE.ORDER_INITIAL_CANCEL.name();
		//String reason = "上海迪士尼占用失败自动废单";
		String reason = "该订单到供应商系统库存占用失败，自动废单";
		String operatorId = "SYSTEM";
		//更新订单状态
//		ResultHandle resultHandle = orderUpdateService.updateCancelOrder(
//				order.getOrderId(),
//				OrderEnum.ORDER_CANCEL_CODE.ORDER_INITIAL_CANCEL.name(),
//				reason, "SYSTEM", null);
//
//		if(resultHandle == null || resultHandle.isFail()) {
//			LOG.error("取消订单失败，订单ID为:" + order.getOrderId() + ", 错误信息："
//					+ (resultHandle == null ? "接口调用异常" : resultHandle.getMsg()));
//			return;
//		}
//
		//重设置状态，避免状态覆盖
		order.setOrderStatus(OrderEnum.ORDER_STATUS.CANCEL.name());
		//非后台没有发送订单生成消息，所以不需要发送取消订单消息
		if(order.getDistributorId() == Constant.DIST_BACK_END ) {
			ResultHandle resultHandle = orderUpdateService.updateCancelOrder(
					order.getOrderId(),
					OrderEnum.ORDER_CANCEL_CODE.ORDER_INITIAL_CANCEL.name(),
					reason, "SYSTEM", null);

			if(resultHandle == null || resultHandle.isFail()) {
				LOG.error("取消订单失败，订单ID为:" + order.getOrderId() + ", 错误信息："
						+ (resultHandle == null ? "接口调用异常" : resultHandle.getMsg()));
				return;
			}
			if(resultHandle.isSuccess()){
			String addition = cancelCode + "_=_" + reason + "_=_" + operatorId;
			orderMessageProducer.sendMsg(MessageFactory.newOrderCancelMessage(order.getOrderId(), addition));}
			LOG.info("send OrderCancelMessage");
		}

		//新增预订通知
//		this.saveReservation(order.getOrderId());

		//String cancelStr = "取消类型：上海迪士尼占用失败" + ",取消原因：上海迪士尼占用失败自动废单";
		String cancelStr = "取消类型："+"该订单占用库存失败" + ",取消原因："+"该订单到供应商系统库存占用失败，自动废单";
		String content = "将编号为["+order.getOrderId()+"]的订单活动变更为["+ OrderEnum.ORDER_STATUS.CANCEL.getCnName() +"]"+cancelStr;

		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
				order.getOrderId(),
				order.getOrderId(),
				"SYSTEM",
				content,
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.name(),
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.getCnName()+"["+ OrderEnum.ORDER_STATUS.CANCEL.getCnName() +"]",
				"");

		order.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.CANCEL.name());

		this.updateOrdOrder(order);
	}

	/**
	 * 锁舱失败后产生预定通知
	 * @param orderId
	 */
	private int saveReservation(Long orderId) {
		//发送给主订单
		String objectType = "ORDER";
		PermUser permUserPrincipal = orderResponsibleService.getOrderPrincipal(objectType, orderId);
		String orderPrincipal = permUserPrincipal.getUserName();

		String receiver = null;
		if (!StringUtils.isEmpty(orderPrincipal)) {
			receiver = orderPrincipal;
		}
		String messageContent="机票锁舱失败后取消订单！";

		ComMessage comMessage=new ComMessage();
		comMessage.setMessageContent(messageContent);
		comMessage.setReceiver(receiver);

		return comMessageService.saveReservation(comMessage, null, orderId, "SYSTEM", "机票锁舱失败后取消订单产生预定通知，内容："+messageContent);
	}

	public void fillOrderData(OrdOrder order,BuyInfo buyInfo){
		OrdOrderDTO orderDto = orderInitService.initOrderAndCalc(buyInfo);

		List<OrdOrderItem> nopackOrderItemList = orderDto.getNopackOrderItemList();
		if(nopackOrderItemList!=null){
			List<OrdOrderItem> itemList = new ArrayList<OrdOrderItem>();
			for(OrdOrderItem orderItem:nopackOrderItemList){
				OrdOrderItem item = new OrdOrderItem();
				item.setPrice(orderItem.getPrice());
				item.setQuantity(orderItem.getQuantity());
				item.setSuppGoods(orderItem.getSuppGoods());
				item.setSuppGoodsId(orderItem.getSuppGoodsId());
				item.setCategoryId(orderItem.getCategoryId());
				item.setSuppGoods(orderItem.getSuppGoods());
				itemList.add(item);
			}
			order.setNopackOrderItemList(itemList);
		}
		List<OrdOrderPack> packList = orderDto.getOrderPackList();
		if(packList!=null){
			List<OrdOrderPack> orderPackList = new ArrayList<OrdOrderPack>();
			for(OrdOrderPack packDto :packList){
				OrdOrderPack pack = new OrdOrderPack();
				pack.setCategoryId(packDto.getCategoryId());
				pack.setContent(packDto.getContent());
				pack.setProductId(packDto.getProductId());

				List<OrdOrderItem> itemDtoList = packDto.getOrderItemList();
				if(itemDtoList!=null){
					List<OrdOrderItem> itemList = new ArrayList<OrdOrderItem>();
					for(OrdOrderItem orderItem:itemDtoList){
						OrdOrderItem item = new OrdOrderItem();
						item.setPrice(orderItem.getPrice());
						item.setQuantity(orderItem.getQuantity());
						item.setSuppGoods(orderItem.getSuppGoods());
						item.setSuppGoodsId(orderItem.getSuppGoodsId());
						item.setCategoryId(orderItem.getCategoryId());
						itemList.add(item);
					}
					pack.setOrderItemList(itemList);
				}
				orderPackList.add(pack);
			}
			order.setOrderPackList(orderPackList);
		}
	}

	@Override
	public ResultHandleT<List<String>> getOrderGoodsCategory(Long orderId){
		ResultHandleT<List<String>> handle = new ResultHandleT<List<String>>();
		try {
			List<String> goodsCategorys = new ArrayList<String>();
			OrdOrder order = queryOrdorderByOrderId(orderId);
			if (order != null) {
				List<OrdOrderItem> itemList = order.getOrderItemList();
				if (itemList != null) {
					for (OrdOrderItem item : itemList) {
						if (item.getOrderPackId() == null) {
							Long goodsId = item.getSuppGoodsId();
							// 查询商品
							SuppGoods suppGoods = null;
							try {
								ResultHandleT<SuppGoods> suppGoodsHandleT = distGoodsClientService
										.findSuppGoodsById(
												Constant.DIST_FRONT_END,
												goodsId);
								suppGoods = suppGoodsHandleT.getReturnContent();
							} catch (Exception e) {
								LOG.error(ExceptionFormatUtil.getTrace(e));
							}
							if (suppGoods != null) {
								BizCategory category = suppGoods
										.getProdProduct().getBizCategory();
								String categoryCode = category
										.getCategoryCode();
								// 记录商品品类以及距离类型
								if (BizEnum.BIZ_CATEGORY_TYPE.category_route_group
										.getCode().equals(categoryCode)
										|| BizEnum.BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(categoryCode)
										|| BizEnum.BIZ_CATEGORY_TYPE.category_route_local
												.getCode().equals(categoryCode)
										|| BizEnum.BIZ_CATEGORY_TYPE.category_route_customized
												.getCode().equals(categoryCode)
										|| BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb
												.getCode().equals(categoryCode)) {
									String type = category.getCategoryCode()
											+ "_"
											+ suppGoods.getProdProduct()
													.getProductType();
									goodsCategorys.add(type);
								} else {
									goodsCategorys.add(categoryCode);
								}
							}
						}
					}
				}

				List<OrdOrderPack> orderPackList = order.getOrderPackList();
				if (orderPackList != null) {
					for (OrdOrderPack pack : orderPackList) {
						Long productId = pack.getProductId();
						ResultHandleT<ProdProduct> prodResultHandleT = prodProductClientService
								.findHotelProduct4Front(productId, false, false);

						if (!prodResultHandleT.isFail()
								|| prodResultHandleT.getReturnContent() != null) {
							ProdProduct prodProduct = prodResultHandleT
									.getReturnContent();
							BizCategory category = prodProduct.getBizCategory();
							String categoryCode = category.getCategoryCode();

							// 记录产品品类以及距离类型
							if (BizEnum.BIZ_CATEGORY_TYPE.category_route_group
									.getCode().equals(categoryCode)
									|| BizEnum.BIZ_CATEGORY_TYPE.isCategoryTrafficRouteFreedom(categoryCode)
									|| BizEnum.BIZ_CATEGORY_TYPE.category_route_local
											.getCode().equals(categoryCode)
									|| BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb
											.getCode().equals(categoryCode)) {
								String type = category.getCategoryCode() + "_"
										+ prodProduct.getProductType();
								goodsCategorys.add(type);
							} else {
								goodsCategorys.add(categoryCode);
							}
						}
					}
				}
			}
			 handle.setReturnContent(goodsCategorys);
			 return handle;
		} catch (Exception e) {
			handle.setMsg("统计订单商品品类异常");
			LOG.error("OrdOrderClientServiceImpl.getOrderGoodsCategory:Exception msg="+e.getMessage());
			return handle;

		}
}

	@Override
	public PriceInfo countPrice(BuyInfo buyInfo) {
		try {
			LOG.info("countPrice1  方法中接收到的JSON##############"+GsonUtils.toJson(buyInfo));
			return orderPriceService.countPriceBase(buyInfo);
		}catch (GetVerifiedFlightInfoFailException e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			throw e;
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			return null;
		}
	}

	@Override
	public String checkOrderForPayOther(BuyInfo buyInfo) {
		try {
			return bookService.chechOrderPayForOther(buyInfo);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			return "";
		}
	}


	public ResultHandleT<PriceInfo> countPrice2(BuyInfo buyInfo){
		LOG.info("method countPrice2 方法中接收到的JSON##############"+GsonUtils.toJson(buyInfo));
		ResultHandleT<PriceInfo> retResult = new ResultHandleT<PriceInfo>();
		try{
			PriceInfo priceInfo = orderPriceService.countPriceBase(buyInfo);
			if(priceInfo!=null) {
				retResult.setReturnContent(priceInfo);
			}else{
				LOG.info("method countPrice2 result is null");
			}
			LOG.info("method countPrice2 result is"+GsonUtils.toJson(priceInfo));
		} catch (HasRecommendFlightException hasRecommendEx){
			retResult.setErrorCode(ErrorCodeEnum.ErrorCode.HAS_RECOMMEND_FLIGHT.getErrorCode());
			retResult.setMsg(ErrorCodeEnum.ErrorCode.HAS_RECOMMEND_FLIGHT.getSpecification());
		} catch(Exception ex){
			LOG.error("{}", ex);
			retResult.setMsg(ex.getMessage());
		}
		return retResult;
	}



	@Override
	public ResultHandleT<List<PromPromotion>>  checkPromAmount(BuyInfo buyInfo) {
			return orderPriceService.checkPromAmount(buyInfo);
	}

	@Override
	public ResultHandleT<List<PromPromotion>> queryPromPromotion(BuyInfo buyInfo) {
		try {
			return orderPriceService.queryPromPromotion(buyInfo);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			return null;
		}
	}

	@Override
	public long queryMaxBounsAmount(BuyInfo buyInfo){
		try {
			return orderPriceService.queryMaxBounsAmount(buyInfo);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			return 0;
		}
	}

	@Override
	public ResultHandle checkStock(BuyInfo buyInfo) {
		ResultHandleT<SupplierProductInfo> handleContent;
		LOG.info("start vst_order checkStock下单方法中接收到的JSON##############"+GsonUtils.toJson(buyInfo));
		// 酒店库存检查
		if(hotelTradeApiService.checkIsHotelProduct(buyInfo)){
			LOG.info(":checkHotelRouteEnableByProductId is true and start hotelTradeApiService checkStock and product is hotel");
			LOG.info(":checkHotelRouteEnableByProductId is true and start hotelTradeApiService checkStock and product is hotel");
			handleContent = hotelTradeApiService.checkStock(buyInfo);
		}else{
			LOG.info(":checkHotelRouteEnableByProductId is false");
			String methodName = "OrdOrderClientServiceImpl#checkStock【"+ buyInfo.getProductId() +"】";
			Long startTime = System.currentTimeMillis();
			handleContent = orderStockService.checkStock(buyInfo);
			LOG.info(ComLogUtil.printTraceInfo(methodName,"检查商品库存情况", "orderStockService.checkStock", System.currentTimeMillis() - startTime));

			LOG.info("local:------This log tell us that inventory has been conducted,ID【"+buyInfo.getProductId()+"】调用本地服务检查库存返回结果详细：for buyInfo  the result is"+JSONArray.fromObject(handleContent).toString());
			LOG.info("OrdOrderClientServiceImpl.checkStock: orderStockService.checkStock,ID【"+buyInfo.getProductId()+"】调用本地服务检查库存返回结果  isSuccess=" + handleContent.isSuccess() + ",msg=" + handleContent.getMsg());
			if (handleContent.isSuccess()) {
				SupplierProductInfo supplierProductInfo = handleContent.getReturnContent();
				if (supplierProductInfo != null) {
					LOG.info("loacl------OrdOrderClientServiceImpl.checkStock: handleContent.hasNull=" + handleContent.hasNull() + ", supplierProductInfo.isEmpty=" + supplierProductInfo.isEmpty());
					if (!handleContent.hasNull() && !supplierProductInfo.isEmpty()) {
						startTime = System.currentTimeMillis();
						handleContent = supplierStockCheckService.checkStock(buyInfo, supplierProductInfo);
						LOG.info(ComLogUtil.printTraceInfo(methodName,"库存检查，并且返回库存检查无库存不足的产品", "supplierStockCheckService.checkStock", System.currentTimeMillis() - startTime));
						LOG.info("server:------This log tell us that inventory has been conducted, ID【"+buyInfo.getProductId()+"】调用第三方服务检查库存返回结果详细：for buyInfo  the result is"+JSONArray.fromObject(handleContent).toString());
					}
				} else {
					LOG.info("OrdOrderClientServiceImpl.checkStock: supplierProductInfo=null");
				}
			}
		}
		return handleContent;
	}

	/**
	 * 二次请求获取推荐的航班信息
	 * */
	@Override
	public ResultHandleT<List<FlightNoVo>> doCatchRecommendFlight(BuyInfo buyInfo){
		ResultHandleT<List<FlightNoVo>> resultHandleT = new ResultHandleT<List<FlightNoVo>>();
		OrdOrderDTO order = new OrdOrderDTO(buyInfo);
		order = orderInitService.initOrderLightly(order);
		if(CollectionUtils.isEmpty(order.getOrderItemList())){
			resultHandleT.setMsg("二次请求推荐航班信息时，订单结构不正常");
			return resultHandleT;
		}
		try {
			ResultHandleT<List<FlightNoVo>> resultHandleTOfTimePrice = orderLvfTimePriceServiceImpl.catchRecommendFlight(order.getOrderItemList());
			if(resultHandleTOfTimePrice.isFail()){
				resultHandleT.setMsg(resultHandleTOfTimePrice.getMsg());
			} else {
				resultHandleT.setReturnContent(resultHandleTOfTimePrice.getReturnContent());
			}
		} catch (Exception e) {
			LOG.error("Error occurs while catch flight info for second request:", e);
			resultHandleT.setMsg(e.getMessage());
		}
		return resultHandleT;
	}

    @Override
    public ResultHandle checkIdNo(BuyInfo buyInfo) {
        ResultHandle handle = supplierIdNoCheckService.checkIdNo(buyInfo);
        return handle;
    }

    @Override
	public OrdOrder queryOrdorderByOrderId(Long orderId) {
		return complexQueryService.queryOrderByOrderId(orderId);
	}



	@Override
	public Page<OrdOrder> compositeQuery(Page<OrdOrder> page,
			ComplexQuerySQLCondition condition) {
		long totalCount=complexQueryService.queryOrderCountByCondition(condition);
		if(totalCount>0L){
			page.setTotalResultSize(totalCount);
			OrderPageIndexParam param = new OrderPageIndexParam();
			param.setBeginIndex((int)page.getStartRows());
			param.setEndIndex((int)page.getEndRows());
			condition.setOrderPageIndexParam(param);
			condition.getOrderFlagParam().setOrderPageFlag(true);
			condition.getOrderFlagParam().setOrderItemTableFlag(true);
			page.setItems(complexQueryService.queryOrderListByCondition(condition));
			try {
				initOrdOrderShowOneKeyOrderFlag(page.getItems());
			} catch (Exception e) {
				LOG.error("initOrdOrderShowOneKeyOrderFlag is error :"+ExceptionFormatUtil.getTrace(e));
			}
			
		}
		return page;
	}
	
    /**
     * 判断是否需要显示一键重下
     * @param
     * @return
     */
    private void initOrdOrderShowOneKeyOrderFlag(List<OrdOrder> orderList) {
		if(CollectionUtils.isEmpty(orderList)){
			return;
		}
		Set<Long> productIds=new HashSet<Long>();
		for (OrdOrder ordOrder : orderList) {
			//初始化不能一键下单
			ordOrder.setShowOneKeyOrderFlag("N");
			if(ordOrder.getProductId()!=null){
				productIds.add(ordOrder.getProductId());
			}
		}
		
		if(productIds.size()>LIMIT_SELECT_PRODUCT){
			LOG.warn("initOrdOrderShowOneKeyOrderFlag  size:"+productIds.size());
			return;
		}
		ResultHandleT<Map<Long,ProdProduct>> result=this.prodProductClientService.findRealProdProductsByOrders(orderList);
		if(result.isFail() || result.hasNull()){
			LOG.error("findRealProdProductsByOrders error :"+result.getMsg());
			return;
		}
		//key值为订单id
		Map<Long,ProdProduct> prodMap=result.getReturnContent();
		
		//批量获取是否前台可售
		ResultHandleT<List<Long>> resultHandle=distDistributorProdClientRemote.verfiedProdDistributor(productIds, 3L);
		List<Long> verList=Collections.emptyList();
		if(resultHandle.isSuccess() && !resultHandle.hasNull()){
			verList=resultHandle.getReturnContent();
		}
		for (OrdOrder order : orderList) {
			if(order.getOrderId()==null){
				continue;
			}
			ProdProduct product=prodMap.get(order.getOrderId());
			if(product==null){
				LOG.info("initOrdOrderShowOneKeyOrderFlag product is null, orderid:"+order.getOrderId()+" ");
				continue;
			}
			//判断是否可以前端销售
			if(!verList.contains(product.getProductId())){
				LOG.info("initOrdOrderShowOneKeyOrderFlag verfiedProd is false , orderid:"+order.getOrderId()+" productid:"+product.getProductId());
				continue;
			}
			//判断是否是支持的品类
	    	Boolean isSupport=isOrderSupportOneKeyRecreate(order, product);
	    	if(isSupport==false){
	    		LOG.info("initOrdOrderShowOneKeyOrderFlag notSupport, orderid:"+order.getOrderId()+" ");
	    		continue;
	    	}
	    	
	    	Boolean isShow=isShowOneKeyRecreateButton(order, product);
	    	if(isShow){
	    		LOG.info("initOrdOrderShowOneKeyOrderFlag Y order:"+order.getOrderId());
	    		order.setShowOneKeyOrderFlag("Y");
	    	}
		}
	}
    
    private boolean isShowOneKeyRecreateButton(OrdOrder order,ProdProduct product){
    	if(order==null || product==null){
    		LOG.info("initOrdOrderShowOneKeyOrderFlag N");
    		return Boolean.FALSE;
    	}
    	String saleFlag=product.getSaleFlag();
    	String orderStatus=order.getOrderStatus();
    	String paymentStatus=order.getPaymentStatus();
    	LOG.info("cal isShowOneKeyRecreateButton  order:"+order.getOrderId()+"  saleFlag:"+saleFlag+" orderStatus:"+orderStatus+" paymentStatus:"+paymentStatus);
    	if(StringUtils.equalsIgnoreCase("Y", saleFlag)==false){
    		return Boolean.FALSE;
    	}
    	//判断是否可售
    	if(order.hasCanceled()){
    		return Boolean.TRUE;
		}else if(!order.hasCanceled() && order.hasPayed()){
			return Boolean.TRUE;
		}else{
			return Boolean.FALSE;
		}
    }

	private boolean isBackUser(String operatorId) {
		boolean isBackUser = false;
		if (operatorId != null) {
			if ("system".equalsIgnoreCase(operatorId)
					|| "admin".equalsIgnoreCase(operatorId)
					|| operatorId.startsWith("cs")
					|| operatorId.startsWith("lv")) {
				isBackUser = true;
			}
		}

		return isBackUser;
	}

	private boolean isO2oBackUser(String operatorId) {
		boolean isBackUser = false;
		if (operatorId != null) {
			for (O2oOrdConstant.O2O_BACK_USERS item : O2oOrdConstant.O2O_BACK_USERS.values()) {
				if(item.getO2oName().equals(operatorId)){
					isBackUser = true;
				}
			}
		}

		return isBackUser;
	}

	@Override
	public ResultHandle cancelOrder(Long orderId, String cancelCode,
			String reason, String operatorId, String memo) {
		ResultHandle resultHandle = new ResultHandle();
		OrdOrder order = queryOrdorderByOrderId(orderId);
		if(order==null){
			resultHandle.setMsg("订单不存在");
			resultHandle.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.NOT_EXIST_ORDER.getErrorCode());
			return resultHandle;
		}
		// 主子单
		OrdOrderItem mainOrdOrderItem = order.getMainOrderItem();
		// 主子单的产品类型
		String mainProductType = mainOrdOrderItem != null ? mainOrdOrderItem.getProductType() : null;
		// 超级会员、快递订单取消走lvmm_order_precess
		if(Long.valueOf(188).equals(order.getCategoryId()) 
				|| (Long.valueOf(90).equals(order.getCategoryId()) && "EXPRESS".equals(mainProductType))){
			resultHandle = cancelOrderRouteToProcess(orderId, cancelCode, reason, operatorId, memo);
			return resultHandle;
		}
		if(!(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId()==(order.getCategoryId()))){
		LOG.info("OrdOrderClientServiceImpl.cancelOrder: orderId=" + orderId);
		final String key="VST_CANCEL_ORDER_"+orderId;
		try{
			if (SynchronizedLock.isOnDoingMemCached(key)) {
				resultHandle.setMsg("订单在重复废单操作");
				resultHandle.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.REPEAT_CANCEL_ORDER.getErrorCode());
				return resultHandle;
			}
			if (orderId != null) {
				//OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
				if(order==null){
					resultHandle.setMsg("订单不存在");
					resultHandle.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.NOT_EXIST_ORDER.getErrorCode());
				}else if(order.isCancel()){
					resultHandle.setMsg("订单已经被废单");
					resultHandle.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.CANCELED_ORDER.getErrorCode());
				}
				if(resultHandle.isFail()) {
                    return resultHandle;
                }
				
				/*设置订单子项，用于isCanCancel()中调用开始*/
                List<OrdOrderItem> orderItems = orderUpdateService.queryOrderItemByOrderId(orderId);
                order.setOrderItemList(orderItems);
                /*设置订单子项，用于isCanCancel()中调用结束*/
				
				// 如果销售渠道是O2O门店
				if(order.getDistributorId().equals(10L) || order.getDistributorId().equals(20L)){
					LOG.info("OrdOrderClientServiceImpl.cancelOrder: orderId=" + orderId + ";参数operatorId=" + operatorId);
					if(!isO2oBackUser(operatorId) && !order.isCanCancel()){
						LOG.info("OrdOrderClientServiceImpl.cancelOrder: orderId=" + orderId + ";参数operatorId=" + operatorId + ";订单已经不能取消。");
						resultHandle.setMsg("订单已经不能取消。");
						resultHandle.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.CAN_NOT_CANCAEL_ORDER.getErrorCode());
					}
				}else{
					//TODO
					if(!isBackUser(operatorId) && !order.isCanCancel()){
						LOG.info("订单不能取消的原因"+isBackUser(operatorId)+"是否是取消状态"+order.isCanCancel()) ;
						resultHandle.setMsg("订单已经不能取消。");
						resultHandle.setErrorCode(OrderStatusEnum.ORDER_ERROR_CODE.CAN_NOT_CANCAEL_ORDER.getErrorCode());
					}
				}
				if(resultHandle.isFail()) {
                    return resultHandle;
                }
				LOG.info("-------lxh---------"+orderId);
				if (OrderEnum.ORDER_CANCEL_CODE.TIME_OUT_WAIT.name().equals(
						cancelCode)
						&& OrderEnum.PAYMENT_STATUS.PAYED.name().equals(
								order.getPaymentStatus())) {
					LOG.info("该订单已支付，不能进行支付等待超时取消, orderId:" + orderId);
					resultHandle.setMsg("该订单已支付，不能进行支付等待超时取消");
				}
				LOG.info("-------lxh---------"+orderId);
				if(resultHandle.isFail()){
					return resultHandle;
				}
				//提前退
				if("提前退".equals(reason)){
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("preRefundStatus", OrderEnum.PRE_REFUND_STATUS.APPLY.name());
					params.put("orderId", orderId);
					orderUpdateService.updatePreRefundStatus(params);
					//生成2张售后单(一张售后 一张资审)
					vstOrderRefundServiceRemote.createOrdSaleForPreRufund(orderId, operatorId);
				}
				boolean activitiNotCancel=false;
				LOG.info("-------lxh---------"+orderId);
				//存在流程的数据走流程废单
				if(ActivitiUtils.hasActivitiOrder(order)){
					LOG.info(order.getOrderId() + "在流程废单");
					OrderVo vo = ordOrderClientExtendService.initOrderVo(order, "cancel");
					if(order2RouteService.isOrderRouteToNewWorkflow(vo)) {
						ordOrderClientExtendService.cancelOrderFromWorkflowNewPlus(cancelCode, reason, operatorId, memo, vo);
					}else if(this.orderRouteService.isOrderRouteToNewWorkflow(vo)) {
						ordOrderClientExtendService.cancelOrderFromWorkflowNew(cancelCode, reason, operatorId, memo, vo);
					} else {
						Map<String,Object> params = ordOrderClientExtendService.getWorkflowMapParams(cancelCode, reason, operatorId, memo);
						//流程取消，并且退回预控资源
						ResultHandle handle = processerClientService.cancelOrder(createKeyByOrder(order), params);
	
						if(handle.isFail()){
							LOG.info("cancel fail in  activiti,use default cancel orderId:"+orderId);
							order = queryOrdorderByOrderId(orderId);
							fillStartProcessParam(order, params);
							String processId = processerClientService.startProcesser("order_cancel_prepaid", "order_cancel_id:"+order.getOrderId(), params);
							LOG.info("CANCEL ORDER id:"+order.getOrderId()+" ProcesserId:"+processId);
						}else{
							LOG.info("order id:"+order.getOrderId()+" activiti cancel");
						}
					}
				}

				if(ActivitiUtils.hasNotActivitiOrder(order)||activitiNotCancel){
					LOG.info("-------lxh---------"+orderId);
					LOG.info(order.getOrderId()+"不在流程废单");
					if(activitiNotCancel){
						orderUpdateService.updateClearOrderProcess(orderId);
					}
					resultHandle= cancelOrderLocal(orderId, cancelCode, reason, operatorId, memo);
					LOG.info("OrdOrderClientServiceImpl.cancelOrder: resultHandle.isSuccess=" + resultHandle.isSuccess() + ", resultHandle.getMsg=" + resultHandle.getMsg());
					if(resultHandle.isFail()) {
						return resultHandle;
					}else{
						//ziyuanyukong 取消订单后要将资源放入资源预控当中去
						/*updateResBackToPrecontrol(order.getOrderId());*/
					}
					OrdOrder canceledOrder = queryOrdorderByOrderId(orderId);
					this.insertOrderLog(canceledOrder, OrderEnum.ORDER_STATUS.CANCEL.getCode(), operatorId, memo,cancelCode,reason);
					//这个改成人工来废单
					/*if(StringUtils.equals(OrderEnum.ORDER_CANCEL_TYPE_RESOURCE_NO_CONFIM.toString(), cancelCode)&&canceledOrder.hasNeedPrepaid()&& canceledOrder.hasPayed()){
						//退款申请
						if(StringUtils.isEmpty(memo)){
							memo="取消订单需要自动退款";
						}
						ordPrePayServiceAdapter.autoCreateOrderFullRefundVst(orderId, operatorId, memo);
					}*/
				}
				OrdOrder oldOrder=queryOrdorderByOrderId(orderId);
                //酒店非对接非保留房、部分非保留房，匹配部分原因并持久化到ORD_ORDER_STATUS中，分销（对接携程）异步获取订单流转不通过原因。

				if (isCategoryHotel(oldOrder) && null != oldOrder.getMainOrderItem() && !oldOrder.getMainOrderItem().hasSupplierApi()) {
                    String errorCode = getMatchedErrorCode(reason);

                    OrdOrderStatus ordOrderStatus = new OrdOrderStatus();

                    ordOrderStatus.setOrderId(orderId);
                    ordOrderStatus.setStatus(OrderStatusEnum.ORDER_PROCESS_STATUS.FAILED.getStatusCode());
                    ordOrderStatus.setErrorCode(errorCode);
                    ordOrderStatus.setErrorMsg(reason);
                    ordOrderStatus.setCreateTime(new Date());

                    //如果错误缓存表里已经存在该orderId的记录，则不插入。
                    OrdOrderStatus check = ordOrderStatusClientService.findOrdOrderStatusByOrderId(orderId);
                    if (null == check) {
                        ordOrderStatusClientService.addOrdOrderStatus(ordOrderStatus);
                    }
                }

				//订单取消成功发送预定通知
//				OrdOrder oldOrder=this.orderUpdateService.queryOrdOrderByOrderId(orderId);
//				OrdOrder oldOrder=queryOrdorderByOrderId(orderId);
				LOG.info("-------lxh---------"+orderId);
				if (resultHandle.isSuccess()) {
					if (oldOrder.getActualAmount()>0) {

						// 额外处理：107/108/110 这三个走新逻辑
						boolean ifAddition = false;
						if ( oldOrder.getDistributionChannel() != null){
							if (oldOrder.getDistributionChannel().equals("107")
									|| oldOrder.getDistributionChannel().equals("108")
									|| oldOrder.getDistributionChannel().equals("110")) {
								ifAddition = true;
							}
						}
						
						//add by mahuayang  
						boolean cancelAutoRefund = isAutoRefund(oldOrder, orderId, operatorId);

						if((ifAddition || oldOrder.getDistributorId()==Constants.DISTRIBUTOR_2.longValue()||oldOrder.getDistributorId()==Constants.DISTRIBUTOR_3.longValue())
								&&SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equals(oldOrder.getCancelStrategy())
								&&OrderEnum.ORDER_CANCEL_CODE.TIME_OUT_WAIT.name().equals(cancelCode)){
							if(!cancelAutoRefund) {
								boolean ifNew = false;
								if (oldOrder.getDistributorId() == Constants.DISTRIBUTOR_4) {
									ifNew = false;	// 分销走旧的
								} else {
									ifNew = true;	// 除分销走新的
								}
								if (ifAddition){
									ifNew = true;	// 额外处理：107/108/110 这三个走新逻辑
								}
								
								// 新逻辑：除分销  + 属于：107/108/110
								if (ifNew){
									// 自动创建全额退款的退款单并进入实际退款中，立即关闭售后
									LOG.info("autoCreateOrderFullRefundCloseSaleServiceVst start, orderId:{}", orderId);
									Long refundId = ordPrePayServiceAdapter.autoCreateOrderFullRefundCloseSaleServiceVst(orderId, operatorId);
									LOG.info("autoCreateOrderFullRefundCloseSaleServiceVst end, orderId:" + orderId + ", refundId:" + refundId!=null?String.valueOf(refundId):"null");
								} else {
									LOG.info("autoCreateRefundByCancelOrderVst start, orderId:{}", orderId);
									// 自动创建退款单
									Long refundId=ordPrePayServiceAdapter.autoCreateRefundByCancelOrderVst(orderId, operatorId);
									LOG.info("autoCreateRefundByCancelOrderVst end , orderId:" + orderId + ", refundId:" + refundId);
								}
							}

						}else{
							//门票废单无需发送预订通知
							LOG.info("saveReservationAfterCan__start__order.getOrderId"+order.getOrderId());
							if(11L != order.getCategoryId() && 12L != order.getCategoryId() && 13L != order.getCategoryId()){
								LOG.info("into__saveReservationAfterCan__start__order.getOrderId"+order.getOrderId());
							    this.comMessageService.saveReservationAfterCan(orderId, "system");
							 }

						}
					}
					if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())){
						LOG.info("-------酒店还原赠品库存------" + orderId);
						List<OrdOrderItem>orderItemList =  iOrdOrderItemService.selectByOrderId(order.getOrderId());
						ordItemFreebieService.cancelFreebie(orderItemList);
					}
					//add by zhouguoliang
					cancelOrderAndRemoveForbidBuyRecord(orderId);
					//增加订单跟踪信息到数据库，提供给驴途使用
					LOG.info("OrdOrderClientServiceImpl.cancelOrder: saveTracking"+oldOrder.getCategoryId());
					if(oldOrder!=null&&(oldOrder.getCategoryId()==11L||oldOrder.getCategoryId()==12L||oldOrder.getCategoryId()==13L)){
						OrdOrderTracking orderTracking = new OrdOrderTracking();
						orderTracking.setCategoryId(oldOrder.getCategoryId());
						orderTracking.setOrderStatus(OrderEnum.ORDER_TRACKING_STATUS.CANCEL.getCode());
						orderTracking.setChangeStatusTime(new Date());
						orderTracking.setOrderId(orderId);
						orderTracking.setCreateTime(new Date());
						LOG.info("OrdOrderClientServiceImpl.cancelOrder: saveTracking:"+orderTracking.getOrderStatus()+",orderId:"+orderTracking.getOrderId());
						ordOrderTrackingService.saveOrderTracking(orderTracking);
					}
				}
				//增加子订单订单取消预定通知
				LOG.info("-------lxh---------"+orderId+"------"+order.getBuCode());
				if(!BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(order.getCategoryId())&&!OrdOrderUtils.isLocalBuOrderNew(order)){
					LOG.info("-------lxh---------"+orderId+"------"+order.getCategoryId());
					if(Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())||
							Constant.BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())){
						if(!OrdOrderUtils.isDestBuFrontOrderNew(order)){
							List<OrdOrderItem> orderItemList = order.getOrderItemList();
							if(orderItemList!= null && orderItemList.size() > 0){
								for (OrdOrderItem orderItem : orderItemList) {
									if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())
											||BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())
											){
										boolean isSendCertificate = isSendCertificate(orderItem.getOrderItemId());
										if(isSendCertificate){
											LOG.info("-------增加取消订单预定通知---------orderId:"+orderId+",orderItemId:"+orderItem.getOrderItemId());
											Map<String, Object> param = new HashMap<String, Object>();
											param.put("objectId", orderItem.getOrderItemId());
											param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name());
											param.put("auditType", OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.name());
											String[] auditStatusArray=new String[]{OrderEnum.AUDIT_STATUS.POOL.getCode(),OrderEnum.AUDIT_STATUS.UNPROCESSED.getCode()};
											param.put("auditStatusArray",auditStatusArray );
											List<ComAudit> comAuditList=orderAuditService.queryAuditListByParam(param);
											if(null!=comAuditList&&comAuditList.size()>0){
												continue;
											}
											saveCreateOrderAudit(orderItem.getOrderItemId(),OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name(),OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.name());
										}
									}
								}
							}
						}	
					}
				}
			}
		}finally{
			SynchronizedLock.releaseMemCached(key);
		}
		}else{
			OrderCancelInfo cancelInfo = new OrderCancelInfo();
			cancelInfo.setOrderId(orderId);
			cancelInfo.setCancelCode(cancelCode);
			cancelInfo.setMemo(memo);
			cancelInfo.setOperatorId(operatorId);
			cancelInfo.setReason(reason);
			try{
		     
			orderCancelService.doOrderCancel(cancelInfo);
			resultHandle.isSuccess();
			
			}catch(Exception e){
				LOG.error(e.getMessage());
				resultHandle.isFail();
			}
		}
		return resultHandle;
	}
	
	/** 
	 * @Title: cancelOrderRouteToProcess 
	 * @Description: 路由到lvmm-order-process取消订单方法
	 * @param orderId
	 * @param cancelCode
	 * @param cancleReason
	 * @param loginUserId
	 * @param orderRemark
	 * @return
	 * @return: ResultHandle
	 */
	private ResultHandle cancelOrderRouteToProcess(Long orderId, String cancelCode, String cancleReason, String loginUserId, String orderRemark) {
		ResultHandle result = new ResultHandle();
		OrderCancelParamVo reqVo = new OrderCancelParamVo(orderId, cancelCode, cancleReason, loginUserId, orderRemark);
		com.lvmama.order.api.base.vo.RequestBody<OrderCancelParamVo> req = new com.lvmama.order.api.base.vo.RequestBody<OrderCancelParamVo>(reqVo);
		com.lvmama.order.api.base.vo.ResponseBody res = apiOrderCancelProcessService.cancelOrder(req);
		if(res != null && res.isFailure()) {
			result.setMsg(res.getMessage());
		}
		return result;
	}
	
	
	@Override
	public ResultHandle createSaleVstAndCancelOrder(OrdOrder order,String cancelCode,String reason,String operatorId, String orderMemo,
			String memo) {
		ResultHandle result=new ResultHandle();
		Long orderId = order.getOrderId();
		LOG.info("orderId="+orderId+", cancelCode"+cancelCode+", reason="+reason+", operatorId="+operatorId+", orderMemo="+orderMemo+", memo="+memo);
		Long saleId = 0L;
		try {
			String saleServiceType = "FX";
			String saleServiceSource = "FX_BACK";//FX_BACK(\"分销后台售后单\")
			saleId = orderServiceProxy.createSaleVst(orderId, saleServiceType, saleServiceSource, operatorId, memo);
			LOG.info("###orderId### "+orderId+" create saleVst successed,saleId==" + saleId);
		} catch (Exception e) {
			LOG.error("###orderId### "+orderId+" create saleVst failed", e);
			result.setMsg("创建售后单失败");
		}
		if (result.isSuccess()) {
			try {
				if("STAMP".equals(order.getOrderSubType())){
					LOG.info("券订单取消接口-------------开始");
					 LOG.info("------------------7------------------");
					String url = Constant.getInstance().getPreSaleBaseUrl()+ "/customer/stamp/order/cancel";
					Map<String, Object> map=new HashMap<String, Object>();
					map.put("orderId", orderId);
					map.put("cancelCode", cancelCode);
					map.put("memo", orderMemo);
					map.put("operatorId", operatorId);
					map.put("reason", operatorId);
					LOG.info("券订单取消接口-------------请求参数:"+map.toString()+"url:"+url);
					RestClient.getClient().put(url, map);
					LOG.info("券订单取消接口-------------结束");
				}else{
					ResultHandle resultHandle = this.cancelOrder(orderId, cancelCode, reason, operatorId, orderMemo);
				}
			} catch (Exception e) {
				LOG.error("###orderId### "+orderId+" invoking cancelOrder failed", e);
				LOG.info("###cancelOrder failed, deleting created saleVst begin###");
				ordSaleServiceService.deleteOrdSaleService(String.valueOf(saleId));
				LOG.info("###cancelOrder failed, deleting created saleVst end###");
				result.setMsg("创建售后单失败");
			}
		}
		return result;
		
	}
	//是否发送过凭证
	private boolean isSendCertificate(Long orderItemId) {
		boolean isSendCertificate = ebkCertifClientService.isSendCertificate(orderItemId);
		return isSendCertificate;
	}

	/**
	 * 驴妈妈前台，无线下单 订单取消后自动退款  部分支付  支付方式：现金 奖金 礼品卡
	 * 取消原因不做判断，可退改的订单   
	 * @author mahuayang
	 * 2017-02-10
	 */
	private boolean isAutoRefund(OrdOrder oldOrder, Long orderId, String operatorId) {
		boolean cancelAutoRefund = false;
		LOG.info("orderId:" + orderId + ",oldOrder.getDistributorId()=" + oldOrder.getDistributorId() + ",cancelStrategy=" + oldOrder.getCancelStrategy());
		LOG.info("orderId:" + oldOrder.getOrderId() + ",oldOrder.getDistributorId()=" + oldOrder.getDistributorId() 
				+ ",oldOrder.getDistributionChannel()=" + oldOrder.getDistributionChannel());
		if((oldOrder.getDistributorId()==Constants.DISTRIBUTOR_3.longValue() || 
				(oldOrder.getDistributorId()==Constants.DISTRIBUTOR_4.longValue() 
					&& (oldOrder.getDistributionChannel() == Constants.DISTRIBUTOR_LVTU.longValue() 
							|| oldOrder.getDistributionChannel() == Constants.DISTRIBUTOR_LVTUTG.longValue()
							|| oldOrder.getDistributionChannel() == Constants.DISTRIBUTOR_LVTUMS.longValue()))) 
			&& OrderEnum.PAYMENT_STATUS.PART_PAY.name().equals(oldOrder.getPaymentStatus())
			&& SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equals(oldOrder.getCancelStrategy())) {
			LOG.info("orderId:" + orderId + "符合自动退款条件");
			List<PayPayment> paymentList = payPaymentServiceAdapter.selectPayPaymentByObjectIdAndPaymentGateway(orderId, null, null);
			boolean autoRefund = true;
			if(paymentList != null && paymentList.size() > 0) {
				LOG.info("orderId:" + orderId + "paymentList.size:" + paymentList.size());
				for(PayPayment item : paymentList) {
					if(!item.getPaymentGateway().equals(com.lvmama.comm.pay.vo.Constant.PAYMENT_GATEWAY.LYTXK_STORED_CARD.name())
						&& !item.getPaymentGateway().equals(com.lvmama.comm.pay.vo.Constant.PAYMENT_GATEWAY.CASH_ACCOUNT.name())
						&& !item.getPaymentGateway().equals(com.lvmama.comm.pay.vo.Constant.PAYMENT_GATEWAY.CASH_BONUS.name())
						&& "SUCCESS".equals(item.getStatus())) {
						autoRefund = false;
						break;
					}
				}
			} else {
				LOG.info("orderId:" + orderId + "paymentList.size 0");
				autoRefund = false;
			}
			if(autoRefund) {
				LOG.info("autoCreateOrderFullRefundCloseSaleServiceVst start, orderId:{}", orderId);
				Long refundId = ordPrePayServiceAdapter.autoCreateOrderFullRefundCloseSaleServiceVst(orderId, operatorId);
				LOG.info("autoCreateOrderFullRefundCloseSaleServiceVst end, orderId:" + orderId + ", refundId:" + refundId!=null?String.valueOf(refundId):"null");
				cancelAutoRefund = true;
			}
			LOG.info("orderId:" + orderId + "cancelAutoRefund:" + cancelAutoRefund);
		}
		return cancelAutoRefund;
	}

    /**
     * 判断酒店品类
     * @param oldOrder
     * @return
     */
    private boolean isCategoryHotel(OrdOrder oldOrder) {
        return oldOrder.getCategoryId() == BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId();
    }

    private String getMatchedErrorCode(String reason) {
        if (StringUtil.isEmptyString(reason)) {
            return null;
        }
        //根据dictId和dictDefId获取需要匹配的取消原因内容
        //dictId=1000;dictDefId=200;dictName=供应商无资源取消订单, 视为库存不足。
        Map<String, Object> params1 = new HashMap<String, Object>();
        params1.put("dictId", 1000L);
        params1.put("dictDefId", 200L);
        String lowStock1 = dictClientService.findDictByCondition(params1).getReturnContent().get(0).getDictName();

        //dictId=1011;dictDefId=203;dictName=确认后满房, 视为库存不足。
        Map<String, Object> params2 = new HashMap<String, Object>();
        params2.put("dictId", 1011L);
        params2.put("dictDefId", 203L);
        String lowStock2 = dictClientService.findDictByCondition(params2).getReturnContent().get(0).getDictName();

        //dictId=1010;dictDefId=203;dictName=价格调整, 视为价格不符。
        Map<String, Object> params3 = new HashMap<String, Object>();
        params3.put("dictId", 1010L);
        params3.put("dictDefId", 203L);
        String priceError1 = dictClientService.findDictByCondition(params3).getReturnContent().get(0).getDictName();

        //dictId=1021;dictDefId=204;dictName=确认后满房, 视为库存不足。
        Map<String, Object> params4 = new HashMap<String, Object>();
        params4.put("dictId", 1021L);
        params4.put("dictDefId", 204L);
        String lowStock3 = dictClientService.findDictByCondition(params4).getReturnContent().get(0).getDictName();

        //dictId=1020;dictDefId=204;dictName=价格调整, 视为价格不符。
        Map<String, Object> params5 = new HashMap<String, Object>();
        params5.put("dictId", 1020L);
        params5.put("dictDefId", 204L);
        String priceError2 = dictClientService.findDictByCondition(params5).getReturnContent().get(0).getDictName();

        String errorCode = null;
        //使用从数据库查到的取消原因内容和入参的取消原因比较确定errorCode
        if (StringUtil.isNotEmptyString(reason)) {
            if (StringUtil.isNotEmptyString(lowStock1)) {
                if (lowStock1.equals(reason)) {
                    errorCode = OrderStatusEnum.ORDER_ERROR_CODE.LOW_STOCK.getErrorCode();
                }
            } else if (StringUtil.isNotEmptyString(lowStock2)) {
                if (lowStock2.equals(reason)) {
                    errorCode = OrderStatusEnum.ORDER_ERROR_CODE.LOW_STOCK.getErrorCode();
                }
            } else if (StringUtil.isNotEmptyString(lowStock3)) {
                if (lowStock3.equals(reason)) {
                    errorCode = OrderStatusEnum.ORDER_ERROR_CODE.LOW_STOCK.getErrorCode();
                }
            } else if (StringUtil.isNotEmptyString(priceError1)) {
                if (priceError1.equals(reason)) {
                    errorCode = OrderStatusEnum.ORDER_ERROR_CODE.PRICE_ERROR.getErrorCode();
                }
            } else if (StringUtil.isNotEmptyString(priceError2)) {
                if (priceError2.equals(reason)) {
                    errorCode = OrderStatusEnum.ORDER_ERROR_CODE.PRICE_ERROR.getErrorCode();
                }
            } else {
                errorCode = null;
            }

        }
        return errorCode;
    }

	/**获取该订单下的所有子订单商品，将满足条件的资源依次退回预控当中
	 * @param orderId
	 */
	@Override
	public boolean updateResBackToPrecontrol(Long orderId){
		boolean ret = true;
		LOG.info("开始退回买断资源"+ orderId +"start ");
		//OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
//		if(order.isCancel()){
//			LOG.info("订单已取消，不在操作子订单【资源不再返还】");
//		}
		List<OrdOrderItem> subOrders = iOrdOrderItemService.selectByOrderId(orderId);
		if(subOrders!=null && subOrders.size() >0){
			GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = null;
			for(int i=0,j=subOrders.size();i<j;i++){
				OrdOrderItem orderItem = subOrders.get(i);
				//该子订单下单时，是否是以买断价格计算的
				if(orderItem!=null && "Y".equals(orderItem.getBuyoutFlag())){
					Long suppGoodsId = orderItem.getSuppGoodsId();
					Date visitDate = orderItem.getVisitTime();

					if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == orderItem.getCategoryId()){
						LOG.info("酒店退单，那么要使用酒店的使用记录表把买断的资源退回去");
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("orderItemId", orderItem.getOrderItemId());
						List<OrdOrderHotelTimeRate> orderHotelTimeRateList = ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateList(params);
						if(orderHotelTimeRateList!=null && orderHotelTimeRateList.size()>0){
							for(int x=0,y=orderHotelTimeRateList.size();x<y;x++){
								OrdOrderHotelTimeRate rate = orderHotelTimeRateList.get(x);
								if(!"Y".equals(rate.getBuyoutFlag())){
									continue;
								}
								Date d = rate.getVisitTime();
								GoodsResPrecontrolPolicyVO tmp = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(suppGoodsId, d);
								Long num = rate.getBuyoutNum();
								num = num == null ? -1L:num;
								LOG.info("酒店退单,个数" + num);
								if(num <0){
									LOG.info("买断个数错误");
								}else{
									ret = updatePrecontrolResource(orderItem,tmp, num, rate.getSettlementPrice() * num ,d);
								}
							}
						}
					}else{
						goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(suppGoodsId, visitDate);
						ret = updatePrecontrolResource(orderItem,goodsResPrecontrolPolicyVO, orderItem.getBuyoutQuantity(), orderItem.getBuyoutTotalPrice(),visitDate);
					}
					if(ret == false){
						break;
					}
				}
			}
		}else{
			LOG.info("退买断资源没有找到子订单...." + orderId);
			ret = true;
		}
		LOG.info("结束退回买断资源"+ orderId +"end ");
		return ret ;
	}

	private boolean updatePrecontrolResource(OrdOrderItem orderItem ,GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO,Long buyoutQuantity ,Long buyoutTotalAmount,Date visitDate ){
		boolean ret =false;
		boolean reduceResult = false;
		if(goodsResPrecontrolPolicyVO!=null ){
			List<Long> goodList = null;
			List<Date> dateList = null;
			Long controlId = goodsResPrecontrolPolicyVO.getId();
			String resType = goodsResPrecontrolPolicyVO.getControlType();
			boolean isSaledOver = false;
			Long goodsId = goodsResPrecontrolPolicyVO.getSuppGoodsId();
			//购买该商品的数量
			Long reduceNum = buyoutQuantity;
			reduceNum = reduceNum==null ? 0: reduceNum;
			Long buyoutTotalPrice = buyoutTotalAmount;
			buyoutTotalPrice = buyoutTotalPrice==null ?0L:buyoutTotalPrice;
			if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equalsIgnoreCase(resType)){
				Long amountId = goodsResPrecontrolPolicyVO.getAmountId();
				//该商品在该时间内的剩余金额
				Long leftAmount = goodsResPrecontrolPolicyVO.getLeftAmount();
				isSaledOver = leftAmount==null? false:leftAmount==0;
				leftAmount = leftAmount==null? 0L:leftAmount;
				Long leftValue = leftAmount + buyoutTotalPrice;
				leftValue = leftValue< 0? 0L:leftValue;

				if(leftValue>goodsResPrecontrolPolicyVO.getAmount()){
					//退回的时候，不能超过原先设置的大小
					leftValue = goodsResPrecontrolPolicyVO.getAmount();
				}
				if(buyoutTotalPrice > 0){
					reduceResult = resControlBudgetRemote.updateAmountResPrecontrolPolicy(amountId,controlId, visitDate, leftValue);
				}else{
					LOG.info("不需要退回到预控资源中去");
				}
				if(reduceResult == false){
					LOG.error("退回买断资源失败"+ goodsId + ",按日预控，日剩余量ID:" + amountId);
				}
			}else if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.inventory.name().equalsIgnoreCase(resType)){
				Long storeId = goodsResPrecontrolPolicyVO.getStoreId();
				//该商品在该时间内的剩余库存
				Long leftQuantity = goodsResPrecontrolPolicyVO.getLeftNum();
				isSaledOver = leftQuantity==null? false:leftQuantity==0;
				leftQuantity = leftQuantity==null ? 0L:leftQuantity;


				Long leftStore = leftQuantity + reduceNum;
				leftStore = leftStore < 0? 0L:leftStore;
				if(leftStore>goodsResPrecontrolPolicyVO.getAmount()){
					//退回的时候，不能超过原先设置的大小
					leftStore = goodsResPrecontrolPolicyVO.getAmount();
				}
				if(reduceNum > 0){
					//按库存预控
					reduceResult = resControlBudgetRemote.updateStoreResPrecontrolPolicy(storeId,controlId, visitDate, leftStore);
					if(reduceResult == false){
						LOG.error("退回买断资源失败"+ goodsId + ",按日预控，日库存ID:" + storeId);
					}
				}
			}
			String  logStr = "取消订单成功，退回资源到预控中去成功，订单号："+orderItem.getOrderId()+"子订单号："+orderItem.getOrderItemId()+",商品id:"+orderItem.getSuppGoodsId()+",日期："+new SimpleDateFormat("yyyy-MM-dd").format(visitDate)+"，数量："+buyoutQuantity+",总价："+buyoutTotalAmount;
			if(reduceResult){
				LOG.info(logStr);
				//如果退回之前发现是true,也就是退回之前是0，那么退回之后要重新计算价格：因为有买断的商品存在了
				if(isSaledOver){
					goodList = new ArrayList<Long>();
					goodList.add(goodsId);
					dateList = new ArrayList<Date>();
					String dayOrCycle = goodsResPrecontrolPolicyVO.getControlClassification();
					if(ResControlEnum.CONTROL_CLASSIFICATION.Cycle.name().equals(dayOrCycle)){
						dateList.clear();
						Date  begin = goodsResPrecontrolPolicyVO.getTradeEffectDate();
						Date  end = goodsResPrecontrolPolicyVO.getTradeExpiryDate();
						while(begin.compareTo(end)<=0){
							dateList.add(begin);
							begin = DateUtil.addDays(begin, 1);
						}
					}else{
						dateList.clear();
						dateList.add(visitDate);
					}


					try{
						ResultHandleT<Integer> resultT = comPushClientService.pushTimePrice(goodList, dateList, ComIncreament.DATA_SOURCE_TYPE.CAL_BUSNINESS_DATA_JOB, true);
						Integer result = resultT.getReturnContent();
						if(result!=null && result.intValue()>0){
							LOG.info(goodsId + "买断库存卖完后，退回了，发送消息进行变价，发送消息OK");
						}
						comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM, orderItem.getOrderId(), orderItem.getOrderItemId(), "SYSTEM", logStr, ComLog.COM_LOG_LOG_TYPE.RES_PRECONTROL_POLICY_CHANGE.getCnName(), "子订单退回预控资源", "");
					}catch(Exception e){
						LOG.error(goodsId + "买断库存买完后退回了，发送消息进行变价，发送消息失败");
					}
				}
			}else{
				LOG.error("退回买断资源失败！");
			}
		}else{
			LOG.error(orderItem.getSuppGoodsId() + ","+(visitDate.getMonth()+1)+""+visitDate.getDate() +"该商品在这一天的对应的策略找不到了！！");
		}
		ret = reduceResult;
		return ret;
	}

	@Override
	public ResultHandle cancelOrderInBatchDistribution(Long orderId, String cancelCode,
			String reason, String operatorId, String memo) {
		ResultHandle resultHandle = new ResultHandle();
		LOG.info("OrdOrderClientServiceImpl.cancelOrderInBatchDistribution: orderId=" + orderId);
		final String key="VST_CANCEL_ORDER_"+orderId;
		try{
			if (SynchronizedLock.isOnDoingMemCached(key)) {
				resultHandle.setMsg("订单在重复废单操作");
				return resultHandle;
			}
			if (orderId != null) {
				OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
				if(order==null){
					resultHandle.setMsg("订单不存在");
				}else if(order.isCancel()){
					resultHandle.setMsg("订单已经被废单");
				}
				//必须是正常状态的订单才可以取消
				if (!OrderEnum.ORDER_STATUS.NORMAL.name().equals(order.getOrderStatus())
						|| !order.getCancelStrategy().equals(SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name()) ) {//可退改
					resultHandle.setMsg("订单已经不能取消。");
				}

				if(resultHandle.isFail()){
					return resultHandle;
				}
				boolean activitiNotCancel=false;
				//存在流程的数据走流程废单
				if(ActivitiUtils.hasActivitiOrder(order)){
					Map<String,Object> params = new HashMap<String, Object>();
					params.put("cancelCode", cancelCode);
					params.put("reason", reason);
					params.put("operatorId", operatorId);
					params.put("memo", memo);
					ResultHandle handle = processerClientService.cancelOrder(createKeyByOrder(order), params);
					if(handle.isFail()){
//						activitiNotCancel=true;
						LOG.info("cancel fail in  activiti,use default cancel orderId:"+orderId);
						order = queryOrdorderByOrderId(orderId);
						fillStartProcessParam(order, params);
						String processId = processerClientService.startProcesser("order_cancel_prepaid", "order_cancel_id:"+order.getOrderId(), params);
						LOG.info("CANCEL ORDER id:"+order.getOrderId()+" ProcesserId:"+processId);
					}else{
						LOG.info("order id:"+order.getOrderId()+" activiti cancel");
					}
				}

				if(ActivitiUtils.hasNotActivitiOrder(order)||activitiNotCancel){
					if(activitiNotCancel){
						orderUpdateService.updateClearOrderProcess(orderId);
					}
					resultHandle= cancelOrderLocal(orderId, cancelCode, reason, operatorId, memo);
					LOG.info("OrdOrderClientServiceImpl.cancelOrder: resultHandle.isSuccess=" + resultHandle.isSuccess() + ", resultHandle.getMsg=" + resultHandle.getMsg());
					if(resultHandle.isFail()) {
						return resultHandle;
					}
					OrdOrder canceledOrder = queryOrdorderByOrderId(orderId);
					this.insertOrderLog(canceledOrder, OrderEnum.ORDER_STATUS.CANCEL.getCode(), operatorId, memo,cancelCode,reason);

					//这个改成人工来废单
					/*if(StringUtils.equals(OrderEnum.ORDER_CANCEL_TYPE_RESOURCE_NO_CONFIM.toString(), cancelCode)&&canceledOrder.hasNeedPrepaid()&& canceledOrder.hasPayed()){
						//退款申请
						if(StringUtils.isEmpty(memo)){
							memo="取消订单需要自动退款";
						}
						ordPrePayServiceAdapter.autoCreateOrderFullRefundVst(orderId, operatorId, memo);
					}*/
				}


				//订单取消成功发送预定通知
//				OrdOrder oldOrder=this.orderUpdateService.queryOrdOrderByOrderId(orderId);
				OrdOrder oldOrder=queryOrdorderByOrderId(orderId);
				if (resultHandle.isSuccess()) {
					if (oldOrder.getActualAmount()>0) {
						if((oldOrder.getDistributorId()==Constants.DISTRIBUTOR_2.longValue()||oldOrder.getDistributorId()==Constants.DISTRIBUTOR_3.longValue())
								&&SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equals(oldOrder.getCancelStrategy())
								&&OrderEnum.ORDER_CANCEL_CODE.TIME_OUT_WAIT.name().equals(cancelCode)){
							//自动创建退款单
							Long refundId=ordPrePayServiceAdapter.autoCreateRefundByCancelOrderVst(orderId, operatorId);
							LOG.info("orderId:" + orderId + ", refundId:" + refundId);
						}else{
							this.comMessageService.saveReservationAfterCan(orderId, "system");
						}
					}
				}

			}
		}finally{
			SynchronizedLock.releaseMemCached(key);
		}
		return resultHandle;
	}

	public void fillStartProcessParam(OrdOrder order, Map<String, Object> params) {
		params.put("orderId", order.getOrderId());
		params.put("mainOrderItem", order.getMainOrderItem());
		params.put("order", order);
	}
	/**
	 * 流程正常后不再使用该方法
	 */
	@Deprecated
	public ResultHandle cancelOrderLocal(Long orderId, String cancelCode,
			String reason, String operatorId, String memo) {
		ResultHandle resultHandle = new ResultHandle();
		try {
			OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
			if(order==null || order.isCancel()){
				resultHandle.setMsg("订单不存在/订单已经被废单");
				return resultHandle;
			}
			LOG.info("OrdOrderClientServiceImpl.cancelOrderLocal: orderId=" + orderId);
			resultHandle = orderUpdateService.updateCancelOrder(orderId, cancelCode, reason, operatorId,memo);
			LOG.info("OrdOrderClientServiceImpl.cancelOrderLocal: resultHandle.isSuccess=" + resultHandle.isSuccess() + ", resultHandle.getMsg=" + resultHandle.getMsg());
			String addition = cancelCode + "_=_" + reason + "_=_" + operatorId;

			if (resultHandle.isSuccess()) {
				LOG.info("OrdOrderClientServiceImpl.cancelOrderLocal: send OrderCancelMessage");
				orderMessageProducer.sendMsg(MessageFactory.newOrderCancelMessage(orderId, addition));
			}
		} catch (Exception ex) {
			LOG.error(ExceptionFormatUtil.getTrace(ex));
			resultHandle.setMsg(ex.getMessage());
			throw new RuntimeException(ex);
		}
		return resultHandle;
	}

	@Override
	public ResultHandle executeUpdateInfoStatus(OrdOrder order, String newStatus,String assignor,String memo) {
		ResultHandleT<ComAudit> resultHandle = new ResultHandleT<ComAudit>();
		try {
			resultHandle = orderStatusManageService.updateInfoStatus(order, newStatus, assignor, memo);
			LOG.info("OrdOrderClientServiceImpl.executeUpdateInfoStatus,OrderID=" + order.getOrderId() + ",resultHandle.isSuccess=" + resultHandle.isSuccess()
					+ ", newInfoStatus=" + newStatus);
			//如果是信息资源审核通过
			if (resultHandle.isSuccess() && INFO_STATUS.INFOPASS.name().equals(newStatus)) {
				if(OrdOrderUtils.isDestBuFrontOrderNew(order)){
					LOG.info("isDestBuFrontOrderNew not do completeTaskByAudit,orderId=" +order.getOrderId());
				}else{
					if(ActivitiUtils.hasActivitiOrder(order) && !resultHandle.hasNull()){
						processerClientService.completeTaskByAudit(createKeyByOrder(order), resultHandle.getReturnContent());
					}
				}
				orderMessageProducer.sendMsg(MessageFactory.newOrderInformationStatusMessage(order.getOrderId(), newStatus));
				LOG.info("OrdOrderClientServiceImpl.executeUpdateInfoStatus,OrderID=" + order.getOrderId() + ",INFOPASS, send jms message.");
			}
		} catch (Exception ex) {
			LOG.error(ExceptionFormatUtil.getTrace(ex));
			LOG.info("OrdOrderClientServiceImpl.executeUpdateInfoStatus,OrderID=" + order.getOrderId() + ",Exception:" + ex.getMessage());
			resultHandle.setMsg(ex.getMessage());
		}

		return resultHandle;
	}


	/**
	 * 更新订单信息审核状态
	 * @return
	 */
	public ResultHandle executeUpdateChildInfoStatus(OrdOrderItem orderItem,String newStatus,String assignor,String memo){


		ResultHandleT<ComAudit> resultHandle = new ResultHandleT<ComAudit>();
		try {
			resultHandle = orderStatusManageService.updateChildInfoStatus(orderItem, newStatus, assignor, memo);
			LOG.info("OrdOrderClientServiceImpl.executeUpdateChildInfoStatus,OrderItemId=" + orderItem.getOrderItemId() + ",resultHandle.isSuccess=" + resultHandle.isSuccess()
					+ ", newInfoStatus=" + newStatus);

			//如果是信息资源审核通过
			if (resultHandle.isSuccess() && INFO_STATUS.INFOPASS.name().equals(newStatus)) {

//				ComAudit audit =resultHandle.getReturnContent();
				OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderItem.getOrderId());
				if(OrdOrderUtils.isDestBuFrontOrderNew(order)&&!OrdOrderUtils.isBusHotelOrder(order)){
					LOG.info("isDestBuFrontOrderNew not do completeTaskByAudit,orderId=" +order.getOrderId() 
							+",OrderItemId=" + orderItem.getOrderItemId());
				}else{
					if(ActivitiUtils.hasActivitiOrder(order) && !resultHandle.hasNull()){
						processerClientService.completeTaskByAudit(createKeyByOrderItem(orderItem,ActivitiUtils.ITEM_TYPE.approve.name()),resultHandle.getReturnContent());
					}
				}
				

				if (order.hasInfoPass()) {
					orderMessageProducer.sendMsg(MessageFactory.newOrderInformationStatusMessage(orderItem.getOrderId(), newStatus));
				}

				LOG.info("OrdOrderClientServiceImpl.executeUpdateInfoStatus,OrderItemId=" + orderItem.getOrderItemId() + ",INFOPASS, send jms message.");
			}
		} catch (Exception ex) {
			LOG.error(ExceptionFormatUtil.getTrace(ex));
			LOG.info("OrdOrderClientServiceImpl.executeUpdateInfoStatus,OrderItemId=" +  orderItem.getOrderItemId() + ",Exception:" + ex.getMessage());
			resultHandle.setMsg(ex.getMessage());
		}

		return resultHandle;

	}
	/**
	 * 更新订单资源审核状态
	 * @param orderId
	 * @return
	 */
	public ResultHandle executeUpdateResourceStatus(Long orderId, String newStatus,String resourceRetentionTime,String assignor,String memo){
		LOG.info("OrdOrderClientServiceImpl.executeUpdateResourceStatus: orderId=" + orderId);

		ResultHandleT<ComAudit> result=orderStatusManageService.updateResourceStatus(orderId, newStatus,resourceRetentionTime, assignor, memo);

		LOG.info("OrdOrderClientServiceImpl.executeUpdateResourceStatus: result.isSuccess=" + result.isSuccess() + ", result.getMsg=" + result.getMsg());
		if (result.isSuccess()) {
			OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
			order.setOrderItemList(iOrdOrderItemService.selectByOrderId(orderId));
			try{
				if(ActivitiUtils.hasActivitiOrder(order)&& !result.hasNull()){
					LOG.info("processerClientService.completeTaskByAudit::::::::::::::::::"+result.getReturnContent().getAuditId());
					ActivitiKey activitiKey = null;
					OrdOrderItem mainOrderItem = order.getMainOrderItem();
					if(OrdOrderUtils.isDestBuFrontOrder(order)
							&& BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == order.getCategoryId()
							&& mainOrderItem != null && mainOrderItem.hasSupplierApi()){// 对接单酒店对接审核流程
						activitiKey = createKeyOfHotelDockByOrder(order, ActivitiUtils.getOrderType(result.getReturnContent()));
					}else{
						activitiKey = createKeyByOrder(order);
					}
					LOG.info("processerClientService.completeTaskByAudit,activitiKey:"+activitiKey);
					processerClientService.completeTaskByAudit(activitiKey, result.getReturnContent());
					LOG.info("processerClientService.completeTaskByAudit success.");
					
				}
			}catch(Exception e){
				LOG.info("processerClientService.completeTaskByAudit have exception and orderId is::::::::::::::::::"+orderId);
				LOG.error("{}", e);
			}

			this.sendResourceStatusAmpleMsg(orderId);

			//扣款申请
			prepayAmpleVst(orderId, order);
		}
		return result;

	}
	@Override
	public ResultHandleT<ComAudit> executeCompensateUpdateOrderResourceStatusAmple(Long orderId,String resourceRetentionTime,String assignor,String memo){
		ResultHandleT<ComAudit> resultHandleT = orderStatusManageService.compensateUpdateChildResourceStatus(orderId, OrderEnum.RESOURCE_STATUS.AMPLE.name(), resourceRetentionTime, assignor, memo, false);
		if (resultHandleT.isSuccess()) {
			OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
			LOG.info("resource ample, order id:" + order.getOrderId()+",hasResourceAmple:"+order.hasResourceAmple());
			if (order.hasResourceAmple()) {
				//发送资源审核消息
				this.sendResourceStatusAmpleMsg(order.getOrderId());
				//扣款申请
				prepayAmpleVst(order.getOrderId(), order);
			}
		}
		return resultHandleT;
	}
	
	/**
	 * 单酒店对接流程key
	 * @param order
	 * @param type
	 * @return
	 */
	private ActivitiKey createKeyOfHotelDockByOrder(OrdOrder order, String type){
		LOG.info("createKeyOfHotelDockByOrder order.orderid="+order.getOrderId()+",type="+type);
		return new ActivitiKey((String)null, ActivitiUtils.createOrderHotelDockBussinessKey(order, type));
	}

	public void prepayAmpleVst(Long orderId, OrdOrder order) {
		if(order.hasNeedPrepaid()){
			if(order.getOughtAmount()==0){
				ordPrePayServiceAdapter.vstOrder0YuanPayMsg(orderId);
			}else if(order.hasPayed()){
				ordPrePayServiceAdapter.resourceAmpleVst(orderId);
			}
		}
	}

	public ResultHandle executeUpdateChildResourceStatus(OrdOrderItem ordOrderItem, String newStatus,String resourceRetentionTime,String assignor, String memo, boolean ifEBK){
		return executeUpdateChildResourceStatus(ordOrderItem, newStatus, resourceRetentionTime, assignor, memo, null, ifEBK);
	}

	/**
	 * 更新子订单资源审核状态
	 * @param ordOrderItem
	 * @return
	 */
	public ResultHandle executeUpdateChildResourceStatus(OrdOrderItem ordOrderItem, String newStatus,String resourceRetentionTime,String assignor,String memo, String supplierName, boolean ifEBK){
		LOG.info("OrdOrderClientServiceImpl.executeUpdateChildResourceStatus: orderItemId=" + ordOrderItem.getOrderItemId()+",hasSupplierApi:"+ordOrderItem.hasSupplierApi());

		Long orderId=ordOrderItem.getOrderId();
		OrdOrder order = this.queryOrdorderByOrderId(orderId);
		//单酒店直接更新主订单资源状态
		if(order.getCategoryId() == 1) {
			return this.executeUpdateResourceStatus(orderId, newStatus, resourceRetentionTime, assignor, memo);
		}
		ResultHandleT<ComAudit> result=orderStatusManageService.updateChildResourceStatus(ordOrderItem.getOrderItemId(), newStatus, resourceRetentionTime, assignor, memo, supplierName, ifEBK);

		LOG.info("OrdOrderClientServiceImpl.executeUpdateChildResourceStatus: result.isSuccess=" + result.isSuccess() + ", result.getMsg=" + result.getMsg());
		if (result.isSuccess()) {

//			ComAudit audit =result.getReturnContent();
			if(ActivitiUtils.hasActivitiOrder(order)&& !result.hasNull()){
				LOG.info("processerClientService.completeTaskByAudit::::::::::::::::::"+result.getReturnContent().getAuditId());
				ActivitiKey activitiKey = null;
				if(OrdOrderUtils.isDestBuFrontOrder(order)
						&& BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == ordOrderItem.getCategoryId()
						&& ordOrderItem.hasSupplierApi()&&!OrdOrderUtils.isBusHotelOrder(order)){// 对接打包酒店对接审核流程
					activitiKey = createKeyOfHotelDockByOrderItem(ordOrderItem, ActivitiUtils.getOrderType(result.getReturnContent()));
				}else if (CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equalsIgnoreCase(order.getBuCode()) 
						&& BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == ordOrderItem.getCategoryId()
						&& ordOrderItem.hasSupplierApi()
						){					
					activitiKey = createKeyOfHotelDockByOrderItem(ordOrderItem, ActivitiUtils.getOrderType(result.getReturnContent()));
					LOG.info("[OutboundBuActivitiKey]"+activitiKey);
				} else {
					activitiKey = createKeyByOrderItem(ordOrderItem, ActivitiUtils.getOrderType(result.getReturnContent()));
				}
				
				try{
					OrderVo vo = new OrderVo();
					EnhanceBeanUtils.copyProperties(order, vo);
					
					if(orderRouteService.isOrderRouteToNewWorkflow(vo)) {
						AuditActiviTask auditActiviTask = new AuditActiviTask();
						EnhanceBeanUtils.copyProperties(result.getReturnContent(), auditActiviTask);
						auditActiviTask.setOrderId(order.getOrderId());
						LOG.info("OrdOrderClientServiceImpl#executeUpdateChildResourceStatus#request#orderId="+orderId+",objectId="+auditActiviTask.getObjectId()+",objectType="+auditActiviTask.getObjectType()+",auditType="+auditActiviTask.getAuditType());
						ResponseBody<String> responseAudit = apiOrderWorkflowService.completeTaskByAudit(new RequestBody<AuditActiviTask>().setTFlowStyle(auditActiviTask));
						
						if(responseAudit!=null && responseAudit.isSuccess() &&responseAudit.getT()!=null){
							LOG.info("OrdOrderClientServiceImpl#executeUpdateChildResourceStatus#response#value="+responseAudit.getT());
						}
					}else{
						LOG.info("processerClientService.completeTaskByAudit,activitiKey:"+activitiKey+"=dateTime:"+System.currentTimeMillis()+"orderitemId:"+ordOrderItem.getOrderItemId());
						processerClientService.completeTaskByAudit(activitiKey, result.getReturnContent());
						LOG.info("processerClientService.completeTaskByAudit success.enddateTime:"+System.currentTimeMillis()+"orderitemId:"+ordOrderItem.getOrderItemId());
					}
				}catch(Exception e){
					LOG.info("==error==orderId:"+order.getOrderId()+"=e:",e);
				}
			}
			order = this.queryOrdorderByOrderId(orderId);
			//重新加载
			LOG.info("==orderId:"+order.getOrderId()+"==order.isPayMentType():"+order.isPayMentType()
			+",order.isIncludeFlightHotel:"+order.isIncludeFlightHotel()+",order.BuCode:"+order.getBuCode());
			if(OrdOrderUtils.isLocalBuOrderNew(order)&&order.isPayMentType()&&Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())){
				try{
					OrdOrderItem orderItem=order.getOrderItemByOrderItemId(ordOrderItem.getOrderItemId());
					orderItem.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
					LOG.info("==workflow==orderitemId:"+ordOrderItem.getOrderItemId()+"=dateTime:"+System.currentTimeMillis());
					processerHotelflowNew(order);
					LOG.info("==workflow==orderitemId:"+ordOrderItem.getOrderItemId()+"=enddateTime:"+System.currentTimeMillis());
				}catch(Exception e){
					LOG.info("==error==orderId:"+order.getOrderId()+"=e:",e);
				}
			}else if(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId() != ordOrderItem.getCategoryId()
					&&Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())&&order.isIncludeFlightHotel()&&order.isPayMentType()){
				try{
					OrdOrderItem orderItem=order.getOrderItemByOrderItemId(ordOrderItem.getOrderItemId());
					orderItem.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
					LOG.info("==workflow==orderitemId:"+ordOrderItem.getOrderItemId()+"=dateTime:"+System.currentTimeMillis());
					processerHotelflow(order);
					LOG.info("==workflow==orderitemId:"+ordOrderItem.getOrderItemId()+"=enddateTime:"+System.currentTimeMillis());
				}catch(Exception e){
					LOG.info("==error==orderId:"+order.getOrderId()+"=e:",e);
				}
			}
			
			LOG.info("resource ample, order id:" + order.getOrderId()+",hasResourceAmple:"+order.hasResourceAmple()+",objectId"+ordOrderItem.getOrderItemId());
			if (order.hasResourceAmple()) {
				this.sendResourceStatusAmpleMsg(ordOrderItem.getOrderId());
				prepayAmpleVst(orderId, order);
			}
			
		}
		return result;

	}
	
	/**
	 * 更新对接机票子订单资源审核状态
	 * @param ordOrderItem
	 * @return
	 */
	public ResultHandle executeFlightOrderResourcePass(OrdOrderItem ordOrderItem, String newStatus,String resourceRetentionTime,String assignor,String memo, String supplierName, boolean ifEBK){
		LOG.info("OrdOrderClientServiceImpl.executeFlightOrderResourcePass: orderItemId=" + ordOrderItem.getOrderItemId()+",hasSupplierApi:"+ordOrderItem.hasSupplierApi());

		Long orderId=ordOrderItem.getOrderId();
		OrdOrder order = this.queryOrdorderByOrderId(orderId);
		//单酒店直接更新主订单资源状态
		if(order.getCategoryId() == 1) {
			return this.executeUpdateResourceStatus(orderId, newStatus, resourceRetentionTime, assignor, memo);
		}
		ResultHandleT<ComAudit> result=orderStatusManageService.updateFlightOrderResourcePass(ordOrderItem.getOrderItemId(), newStatus, resourceRetentionTime, assignor, memo, supplierName, ifEBK);

		LOG.info("OrdOrderClientServiceImpl.executeFlightOrderResourcePass: result.isSuccess=" + result.isSuccess() + ", result.getMsg=" + result.getMsg());
		if (result.isSuccess()) {

			order = this.queryOrdorderByOrderId(orderId);
			//重新加载
			LOG.info("==orderId:"+order.getOrderId()+"==order.isPayMentType():"+order.isPayMentType()
			+",order.isIncludeFlightHotel:"+order.isIncludeFlightHotel()+",order.BuCode:"+order.getBuCode());
			if(OrdOrderUtils.isLocalBuOrderNew(order)&&order.isPayMentType()&&Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())){
				try{
					OrdOrderItem orderItem=order.getOrderItemByOrderItemId(ordOrderItem.getOrderItemId());
					orderItem.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
					LOG.info("==workflow==orderitemId:"+ordOrderItem.getOrderItemId()+"=dateTime:"+System.currentTimeMillis());
					processerHotelflowNew(order);
					LOG.info("==workflow==orderitemId:"+ordOrderItem.getOrderItemId()+"=enddateTime:"+System.currentTimeMillis());
				}catch(Exception e){
					LOG.info("==error==orderId:"+order.getOrderId()+"=e:",e);
				}
			}else if(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId() != ordOrderItem.getCategoryId()
					&&Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())&&order.isIncludeFlightHotel()&&order.isPayMentType()){
				try{
					OrdOrderItem orderItem=order.getOrderItemByOrderItemId(ordOrderItem.getOrderItemId());
					orderItem.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
					LOG.info("==workflow==orderitemId:"+ordOrderItem.getOrderItemId()+"=dateTime:"+System.currentTimeMillis());
					processerHotelflow(order);
					LOG.info("==workflow==orderitemId:"+ordOrderItem.getOrderItemId()+"=enddateTime:"+System.currentTimeMillis());
				}catch(Exception e){
					LOG.info("==error==orderId:"+order.getOrderId()+"=e:",e);
				}
			}
			
			LOG.info("resource ample, order id:" + order.getOrderId()+",hasResourceAmple:"+order.hasResourceAmple()+",objectId"+ordOrderItem.getOrderItemId());
			if (order.hasResourceAmple()) {
				this.sendResourceStatusAmpleMsg(ordOrderItem.getOrderId());
				prepayAmpleVst(orderId, order);
			}
			
		}
		return result;

	}
	
	
	public void sendReourceStatusAmplAndPrepayAmpleVstMsg(OrdOrder order){
		LOG.info("==workflow enter==orderId:"+order.getOrderId()+"====resouce Ample");
		if (order.hasResourceAmple()) {
			this.sendResourceStatusAmpleMsg(order.getOrderId());
			prepayAmpleVst(order.getOrderId(), order);
		}
	}

	/**
	 * 打包酒店对接流程key
	 * @param item
	 * @param type
	 * @return
	 */
	private ActivitiKey createKeyOfHotelDockByOrderItem(OrdOrderItem item, String type){
		LOG.info("createKeyOfHotelDockByOrderItem item.orderid="+item.getOrderId()+",type="+type);
		return new ActivitiKey((String)null, ActivitiUtils.createItemHotelDockBussinessKey(item, type));
	}

	@Override
	public ResultHandleT<ComAudit> executeUpdateOrderResourceStatusAmple(ComAudit audit,String resourceRetentionTime,String assignor,String memo){
		ResultHandleT<ComAudit> resultHandleT = null;
		if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name().equals(audit.getObjectType())){//目前只有单酒店
			resultHandleT = orderStatusManageService.updateResourceStatus(audit.getObjectId(), OrderEnum.RESOURCE_STATUS.AMPLE.name(), resourceRetentionTime, assignor, memo);
			if (resultHandleT.isSuccess()) {
				LOG.info("resource ample, order id:" + audit.getObjectId());
				OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(audit.getObjectId());
				//发送资源审核消息
				this.sendResourceStatusAmpleMsg(audit.getObjectId());
				//扣款申请
				prepayAmpleVst(audit.getObjectId(), order);
			}
		}else{
			resultHandleT = orderStatusManageService.updateChildResourceStatus(audit.getObjectId(), OrderEnum.RESOURCE_STATUS.AMPLE.name(), resourceRetentionTime, assignor, memo, false);
			if (resultHandleT.isSuccess()) {
				OrdOrderItem orderItem = this.orderUpdateService.getOrderItem(audit.getObjectId());
				OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderItem.getOrderId());
				LOG.info("resource ample, order id:" + order.getOrderId()+",hasResourceAmple:"+order.hasResourceAmple()+",objectId"+audit.getObjectId());
				if (order.hasResourceAmple()) {
					//发送资源审核消息
					this.sendResourceStatusAmpleMsg(order.getOrderId());
					//扣款申请
					prepayAmpleVst(order.getOrderId(), order);
				}
			}
		}

		return resultHandleT;
	}
	@Override
	public ResultHandle executeUpdateOrderResourceStatusAmple(Long objectId, OrderEnum.AUDIT_OBJECT_TYPE objectType, String resourceRetentionTime, String assignor, String memo){
		ResultHandle resultHandle = null;
		if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.equals(objectType)){
			resultHandle = orderStatusManageService.updateResourceStatus(objectId, OrderEnum.RESOURCE_STATUS.AMPLE.name(), "", "system", memo);
			if (resultHandle.isSuccess()) {
				LOG.info("resource ample, order id:" + objectId);
				OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(objectId);
				//发送资源审核消息
				this.sendResourceStatusAmpleMsg(objectId);
				//扣款申请
				prepayAmpleVst(objectId, order);
			}
		}else{
			resultHandle = orderStatusManageService.updateChildResourceStatus(objectId, OrderEnum.RESOURCE_STATUS.AMPLE.name(), "", "system", memo, false);
			if (resultHandle.isSuccess()) {
				OrdOrderItem orderItem = this.orderUpdateService.getOrderItem(objectId);
				OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderItem.getOrderId());
				LOG.info("resource ample, order id:" + order.getOrderId()+",hasResourceAmple:"+order.hasResourceAmple()+",objectId"+objectId);
				if (order.hasResourceAmple()) {
					//发送资源审核消息
					this.sendResourceStatusAmpleMsg(order.getOrderId());
					//扣款申请
					prepayAmpleVst(order.getOrderId(), order);
				}
			}
		}
		return resultHandle;
	}

	@Override
	public List<Person> loadUserReceiversByUserId(String userId) {
		List<Person> personList = null;
		List<PetUsrReceivers> petUsrReceiversList = receiverUserServiceAdapter.loadUserReceiversByUserId(userId);
		if (petUsrReceiversList != null && petUsrReceiversList.size() > 0) {
			personList = new ArrayList<Person>();
			Person person = null;
			for (PetUsrReceivers receivers : petUsrReceiversList) {
				if (receivers != null) {
					person = PetUsrReceiversExchanger.changePetUsrReceivers2Person(receivers);
					if (person != null) {
						personList.add(person);
					}
				}
			}
		}

		return personList;
	}

	@Override
	public void createContact(List<Person> list, String userId) {
		if (list != null && list.size() > 0) {
			List<PetUsrReceivers> petUsrReceiversList = new ArrayList<PetUsrReceivers>();
			PetUsrReceivers receivers = null;
			for (Person person : list) {
				if (person != null) {
					receivers = PetUsrReceiversExchanger.changePerson2PetUsrReceivers(person, OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
					if (receivers != null) {
						petUsrReceiversList.add(receivers);
					}
				}
			}

			if (petUsrReceiversList.size() > 0) {
				receiverUserServiceAdapter.createContact(petUsrReceiversList, userId);//ReceiverUserServiceAdapterImpl
			}
		}

	}

	@Override
	public List<Person> loadUserReceiversByUserId(String userId, int count) {
		List<Person> personList = null;
		List<PetUsrReceivers> petUsrReceiversList = receiverUserServiceAdapter.loadUserReceiversByUserId(userId, count);
		if (petUsrReceiversList != null && petUsrReceiversList.size() > 0) {
			personList = new ArrayList<Person>();
			Person person = null;
			for (PetUsrReceivers receivers : petUsrReceiversList) {
				if (receivers != null) {
					person = PetUsrReceiversExchanger.changePetUsrReceivers2Person(receivers);
					if (person != null) {
						personList.add(person);
					}
				}
			}
		}

		return personList;
	}

	/**
	 * 资源审核成功后发送消息
	 * @param orderId
	 * @return
	 */
	public void sendResourceStatusAmpleMsg(Long orderId){

		//ResultHandle resultHandle=new ResultHandle();

		//发送jms消息通知资源审核通过
		orderMessageProducer.sendMsg(MessageFactory.newOrderResourceStatusMessage(orderId, OrderEnum.RESOURCE_STATUS.AMPLE.getCode()));

		//发送jms资源审核通过消息给驴途
		OrdOrder order=queryOrdorderByOrderId(orderId);
		String addition=order.getProductId()+","+order.getUserNo();
		petOrderMessageService.sendOrderResourcePassMessage(orderId, addition);
	}

	public void sendPayedMsg(OrdOrder order){

		orderMessageProducer.sendMsg(MessageFactory.newOrderPaymentMessage(order.getOrderId(), OrderEnum.PAYMENT_STATUS.PAYED.getCode()));
	}

	/**
	 * 订单应付金额修改成功后发送消息
	 * @param orderItemId
     * @param addition
	 * @return
	 */
	public void sendOrdSettlementPriceChangeMsg(Long orderItemId,String addition){

		orderMessageProducer.sendMsg(MessageFactory.newOrdSettlementPriceChangeMessage(orderItemId, addition));
	}


	public void sendOrdItemPriceConfirmChangeMsg(Long ordItemId,String addition){
		orderMessageProducer.sendMsg(MessageFactory.newOrdItemPriceConfirmChangeMessage(ordItemId, addition));

	}


	public void sendOrderSendFaxMsg(OrdOrder order,String addition){

		//String addition=OrderEnum.ORDER_COMMON_TYPE.fax_flag.name()+"_"+OrderEnum.ORDER_COMMON_TYPE.fax_rule.name();
		orderMessageProducer.sendMsg(MessageFactory.newOrderModifyMessage(order.getOrderId(), addition));
	}


	/**
	 * 二次取消
	 * @param orderItemId
	 * @param addition
	 */
	public void sendOrderSendTwiceFaxMsg(Long orderItemId,String addition){

		//String addition=OrderEnum.ORDER_COMMON_TYPE.fax_flag.name()+"_"+OrderEnum.ORDER_COMMON_TYPE.fax_rule.name();
		orderMessageProducer.sendMsg(MessageFactory.newOrderTwiceCancelMessage(orderItemId, addition));
	}

	/**
	 * 子订单详情发送传真
	 * @param orderItem
     * @param addition
	 * @return
	 */
	public void sendOrderItemSendFaxMsg(OrdOrderItem orderItem,String addition)
	{
		LOG.info("===orderEcontractGeneratorService.sendOrderItemSendFaxMsg.orderItem:"+orderItem.getOrderItemId()+"addition:"+addition);
		orderMessageProducer.sendMsg(MessageFactory.newOrderMemoMessage(orderItem.getOrderItemId(), addition));
		LOG.info("===orderEcontractGeneratorService.sendOrderItemSendFaxMsg.orderItem:"+orderItem.getOrderItemId()+"addition:"+addition);
	}


	@Override
	public List<OrdOrder> queryOrdorderByOrderIdList(List<Long> orderIdList) {
		List<OrdOrder> orderList = null;
		if (orderIdList != null && orderIdList.size() > 0) {
			orderList = orderUpdateService.queryOrdorderByOrderIdList(orderIdList);
		}

		return orderList;
	}

	/**
	 * 根据订单id查询订单(包含子订单)
	 *
	 * @param orderId
	 */
	@Override
	public OrdOrder queryOrderWithItems(Long orderId) {
		if (orderId == null || orderId < 0) {
			return null;
		}

		OrdOrder order = ordOrderDao.selectByPrimaryKey(orderId);
		if(order == null || order.getOrderId() == null || order.getOrderId() < 0) {
			return null;
		}

		List<OrdOrderItem> ordOrderItems = iOrdOrderItemService.selectByOrderId(orderId);
		order.setOrderItemList(ordOrderItems);

		return order;
	}

	/**
	 * 根据订单Id列表查询订单,数量暂限制为50(订单包含子项)
	 *
	 * @param orderIdList
	 * @return
	 */
	@Override
	public List<OrdOrder> queryLimitOrderListByOrderIdList(List<Long> orderIdList) {
		List<Long> limitOrderIdList = orderIdList;
		int limitNum = 50;
		if(CollectionUtils.isNotEmpty(orderIdList) &&  orderIdList.size()>limitNum){
			limitOrderIdList = new ArrayList<Long>();
			limitOrderIdList.addAll(orderIdList.subList(0, (limitNum-1)));
		}

		List<OrdOrder> orderList = queryOrdorderByOrderIdList(limitOrderIdList);
		return orderList;
	}

	/**
	 * 子订单手动生成结算单消息
	 * @param orderItemId
     * @param addition
	 * @return
	 */
	public void sendManualSettlmenteMsg(Long  orderItemId,String addition)
	{
		orderMessageProducer.sendMsg(MessageFactory.newOrderItemSettleMessage(orderItemId, addition));
	}



	@Override
	public ResultHandle updateOrderItem(OrdOrderItem ordOrderItem) {
		ResultHandle resultHandle = new ResultHandle();
		try {
			if (ordOrderItem != null) {
				int i = orderUpdateService.updateOrderItemByIdSelective(ordOrderItem);
				if (i != 1) {
					resultHandle.setMsg("订单子项(ID=" + ordOrderItem.getOrderItemId() + ")更新失败。");
				}
				if(i>0 && StringUtils.isNotBlank(ordOrderItem.getPriceConfirmStatus())){
					String addition=new StringBuffer(ordOrderItem.getOrderItemId()+"").append("|").toString();
					//加入外币记录表修改
					if (ordOrderItem.getRefundCurrencySettPrice() != null) {
						changeOrderItemExtend(ordOrderItem);
					}
					sendOrdItemPriceConfirmChangeMsg(ordOrderItem.getOrderItemId(),addition);
				}

			}
		} catch(Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			resultHandle.setMsg(e);
		}

		return resultHandle;
	}

	@Override
	public OrdOrderItem getOrderItem(Long orderItemId) {
		OrdOrderItem orderItem = null;
		if (orderItemId != null) {
			orderItem = orderUpdateService.getOrderItem(orderItemId);
		}
		return orderItem;
	}

	@Override
	public ResultHandle updateRefundedAmount(Long orderId, Long refundmentId,
			long amount) {
		ResultHandle resultHandle = new ResultHandle();
		try {
			if (!orderUpdateService.updateRefundedAmount(orderId, refundmentId, amount)) {
				resultHandle.setMsg("订单(ID=" + orderId + ",refundmentId" + refundmentId + ",amount=" + amount + ")更新失败。");
			}
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			resultHandle.setMsg(e);
		}

		return resultHandle;
	}

	@Override
	public ResultHandle paymentSuccess(PayPayment payment) {
		ResultHandle resultHandle = new ResultHandle();
		try {
//			LOG.info("order_payment_orderId:"+payment.getObjectId()+" serialNo:"+payment.geneSerialNo()+"   amount:"+payment.getAmount());
			if (payment != null) {
				LOG.info("order_payment_orderId:" + payment.getObjectId()
						+ ", paymentId:" + payment.getPaymentId() + ", serialNo:"
						+ payment.geneSerialNo() + ", amount:"
						+ payment.getAmount());
				OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(payment.getObjectId());
				if(order!=null) {
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("orderId", payment.getObjectId());
					params.put("paymentId", payment.getPaymentId());
					//防止重复累加已付金额
					if(CollectionUtils.isEmpty(ordPaymentInfoService.findOrdPaymentInfoList(params))) {
						// 记录支付信息 ——— 一个事务
						order = orderUpdateService.updatePaymentSuccessInfo(payment);
						if (order != null) {
							// 预售券订单，定金支付成功，记录定金支付时间
							if(StringUtils.equals(order.getOrderSubType(), "STAMP")) {
							    LOG.info("------------------1------------------");
								String url = Constant.getInstance().getPreSaleBaseUrl()
										+ "/customer/stamp/order/payment/deposit?orderId=" + order.getOrderId()
										+ "&actualAmount=" + order.getActualAmount();
								try {
									LOG.info("notify stamp deposit pay, orderId=" + order.getOrderId() + ",actualAmount="+order.getActualAmount());
									Boolean ret = RestClient.getClient().postForObject(url, null, Boolean.class);
									LOG.info("notify stamp deposit pay success, orderId=" + order.getOrderId() + ",ret="+ret);
									if(ret) {
										orderMessageProducer.sendMsg(MessageFactory.newOrderDepositPaymentMessage(order.getOrderId(), OrderEnum.PAYMENT_STATUS.PART_PAY.getCode()));
									}
								} catch (Throwable e) {
									LOG.error("notify stamp deposit pay failed, orderId:" + order.getOrderId(), e);
								}
							}

							// 通知预售券，券状态改为未使用
							if (StringUtils.equals(order.getOrderSubType(), "STAMP") && order.hasFullPayment()) {
								try {
									LOG.info("notify stamp complete pay, orderId=" + order.getOrderId());
									   LOG.info("------------------2------------------");
									RestClient.getClient().postForObject(Constant.getInstance().getPreSaleBaseUrl()
											+ "/customer/stamp/order/payment/complete?orderId=" + order.getOrderId(),
											null, Void.class);
									LOG.info("notify stamp complete pay success, orderId=" + order.getOrderId());
								} catch (Throwable e) {
									LOG.error("notify stamp complete pay failed, orderId:" + order.getOrderId(), e);
								}
							}
							//如果是支付立减的订单
							if (payment.getPromotionAmount()!=null) {
								try {
									LOG.info("paymentPromotion order orderId=" + order.getOrderId()+"amount"+payment.getPromotionAmount());
									OrdPayPromotion ordPayPromotion=new OrdPayPromotion();
									ordPayPromotion.setPayPromotionId(payment.getPromotionId());
									ordPayPromotion.setOrderId(order.getOrderId());
									ordPayPromotion.setFavorableAmount(payment.getPromotionAmount());
									PaymentChannelProfileVo vo=RestClient.getClient().getForObject(Constant.getInstance().getPayPromotionBaseUrl() + "/payment/{id}", PaymentChannelProfileVo.class, payment.getPromotionId());
									ordPayPromotion.setPromTitle(vo.getName());
									if(null==ordPayPromotionService.queryOrdPayPromotionByOrderId(order.getOrderId())){
										ordPayPromotionService.savePayPromotion(ordPayPromotion);
										//扣减优惠金额
										try {
											RestClient.getClient().getForObject(Constant.getInstance().getPayPromotionBaseUrl()+"/customer/payment/order/deductionAmount?promotionId="+payment.getPromotionId()+"&promotionAmount="+payment.getPromotionAmount(),Boolean.class);
										} catch (Exception e) {
											LOG.error("payment koujian shibai !!!!, orderId:" + order.getOrderId(), e);
										}
									}
									
									LOG.info("save paymentprotion orderID "+order.getOrderId()+"amount"+payment.getPromotionAmount());
									OrdOrder orderPay = new OrdOrder();
									orderPay.setOrderId(order.getOrderId());
									orderPay.setOrderUpdateTime(new Date());
									orderPay.setOrderSubType("PAY_PROMOTION");
									orderUpdateService.updateByPrimaryKeySelective(orderPay);
									//订单支付立减
									OrdOrderAmountItem orderAmountItem=getOrdOrderAmountItem(payment);
									ordOrderAmountItemService.insertOrderAmountItem(orderAmountItem);
									
								} catch (Throwable e) {
									LOG.error("payment order save failed, orderId:" + order.getOrderId(), e);
								}
							}
						}
						
						//如果全额支付而且使用订单分摊子系统（lvmm-order-apportion），发送订单分摊消息
						sendOrderApportionMsg(order);

						//支付线上问题紧急处理
						//TODO 目前看已经通过订单对象的hasFullPayment()方法修复了。这段代码应该可以优化掉
						if(order.getPayPromotionAmount()!=null){
							if(order.hasPayPromtionFullPayment()&&order.hasPayed()){
								// 消息与启动工作流解耦
								sendPaymentMsg(order);
								doPaymentSuccessMsg(order);
								//新零售要求订单支付后通知CRM系统 add by wuxz 2017-11-22
								sendNotifyCrmMsg(order);
								/*目的地游玩人后置订单，设置补充游玩人最晚时间*/
                                initDestBuTravDelayWaitTime(order);
							}else{
								petOrderPayMessageService.sendOrderPartPayMessage(order.getOrderId(), null);
								LOG.info("Part payment message send success, orderId=" + order.getOrderId());
								sendPartPaymentMsg(order); //o2o订单， 部分支付发送消息
							}
						}else{
							if(order.hasFullPayment()&&order.hasPayed()){
								// 消息与启动工作流解耦
								sendPaymentMsg(order);
								doPaymentSuccessMsg(order);
								//新零售要求订单支付后通知CRM系统 add by wuxz 2017-11-22
								sendNotifyCrmMsg(order);
								/*目的地游玩人后置订单，设置补充游玩人最晚时间*/
	                            initDestBuTravDelayWaitTime(order);
							}else{ // 部分支付消息，分销用
								petOrderPayMessageService.sendOrderPartPayMessage(order.getOrderId(), null);
								LOG.info("Part payment message send success, orderId=" + order.getOrderId());
								sendPartPaymentMsg(order); //o2o订单， 部分支付发送消息
							}

						}

					}
					/*增加对订单跟踪的操作 start add by yanghaifeng */
					if(order.getCategoryId()==11L||order.getCategoryId()==12L||order.getCategoryId()==13L){
						OrdOrderTracking ordOrderTracking = new OrdOrderTracking();
						ordOrderTracking.setOrderStatus(order.getPaymentStatus());
						ordOrderTracking.setCategoryId(order.getCategoryId());
						ordOrderTracking.setChangeStatusTime(new Date());
						ordOrderTracking.setOrderId(order.getOrderId());
						ordOrderTracking.setCreateTime(new Date());
						LOG.info("payMentSuccess start method saveOrderTracking:"+ordOrderTracking.getOrderId()+","+ordOrderTracking.getOrderStatus());
						ordOrderTrackingService.saveOrderTracking(ordOrderTracking);
						boolean isSupplierOrder = false;
						Map<String,Object> paramMap = new HashMap<String, Object>();
						paramMap.put("orderId", order.getOrderId());
						List<OrdOrderItem> list  = iOrdOrderItemService.selectByParams(paramMap);
						if(CollectionUtils.isNotEmpty(list)){
							for (OrdOrderItem ordOrderItem : list) { //验证是否全部为非对接子订单
								String supplierApiFlag =  (String)ordOrderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.supplierApiFlag.name());
								if ("Y".equalsIgnoreCase(supplierApiFlag)) {
									isSupplierOrder = true;
								}
								if(!isSupplierOrder && StringUtils.equals(ordOrderItem.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name()), SuppGoods.NOTICETYPE.QRCODE.name())){
									isSupplierOrder = true;
								}
							}
							if(order.getPaymentTarget().equals(SuppGoods.PAYTARGET.PREPAID.name())&&!isSupplierOrder&&order.hasFullPayment()&&order.hasPayed()){ //如果为预付、全部子订单为非对接，且全部付款并且已经支付
								ordOrderTracking.setOrderStatus(OrderEnum.ORDER_TRACKING_STATUS.CREDITED.getCode());
								ordOrderTracking.setChangeStatusTime(order.getVisitTime());
								ordOrderTracking.setCreateTime(new Date());
								ordOrderTrackingService.saveOrderTracking(ordOrderTracking); //保存凭证已经生成
								LOG.info("payMentSuccess start method saveOrderTracking:"+ordOrderTracking.getOrderId()+","+ordOrderTracking.getOrderStatus());
							}
						}
					}
					
					//增加付款操作的日志，方便对付款行为进行跟踪
//					if(n == 1) {
					lvmmLogClientService.sendLog(
							ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
							order.getOrderId(),
							order.getOrderId(),
							payment.getOperator(),
							"对编号为["
									+ order.getOrderId()
									+ "]的订单进行付款，操作人："
									+ payment.getOperator()
									+ "，付款金额："
									+ payment.getAmountYuan()
									+ "元，付款渠道："
									+ Constant.PAYMENT_GATEWAY
									.getCnName(payment
											.getPaymentGateway()),
							ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_PAYMENT
									.name(),
							ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_PAYMENT
									.getCnName()
									+ "[" + OrderEnum.PAYMENT_STATUS
									.getCnName(order
											.getPaymentStatus()) + "]",
							"");
//					}
//					if(n==1 && order.hasFullPayment()&&order.hasPayed()){
					if(order.getPayPromotionAmount()!=null){
						if(order.hasPayPromtionFullPayment()&&order.hasPayed()){
							//出境BU发送信息安全卡邮件
							if (order.getBuCode() != null && CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(order.getBuCode())) {
								LOG.info("hasPayPromtionFullPayment sendSafetyInfoEmail, orderId=" + order.getOrderId());
								sendSafetyInfoEmail(order);
							}
							if(order.hasResourceAmple()&&order.getOughtAmount()>0){
								try {
									ordPrePayServiceAdapter.resourceAmpleVst(order.getOrderId());
									LOG.info("ordPrePayServiceAdapter.resourceAmpleVst, orderId=" + order.getOrderId());
								} catch (RuntimeException e) {
									LOG.error("ordPrePayServiceAdapter error order id "+order.getOrderId(),e);
								}
								
							}
						
						}
					}else{
						if(order.hasFullPayment()&&order.hasPayed()){
							//出境BU发送信息安全卡邮件
							if (order.getBuCode() != null && CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(order.getBuCode())) {
								LOG.info("hasFullPayment sendSafetyInfoEmail, orderId=" + order.getOrderId());
								sendSafetyInfoEmail(order);
							}
							//doPaymentSuccessMsg(order);处理短信不停的发先移到前面
							//扣款申请，支付已经完成的订单才发起操作
							if(order.hasResourceAmple()&&order.getOughtAmount()>0){
								try {
									ordPrePayServiceAdapter.resourceAmpleVst(order.getOrderId());
									LOG.info("ordPrePayServiceAdapter.resourceAmpleVst, orderId=" + order.getOrderId());
								} catch (RuntimeException e) {
									LOG.error("ordPrePayServiceAdapter error order id "+order.getOrderId(),e);
								}
	
							}
	
						}
					}


					/*//出境小驴白条游玩人后置支付新增逻辑start
					//小驴白条支付
					boolean isBaiTiao = payment.getPaymentGateway().equals("SAN_BAI_TIAO") || payment.getPaymentGateway().equals("BAI_TIAO_APP");
					//出境订单
					boolean isOutBoundBu = Constant.BU_NAME.OUTBOUND_BU.getCode().equals(order.getBuCode());
					//游玩人后置并且未锁定
					boolean isTravelDelyAndNotLocked = order.getTravellerDelayFlag().equals("Y") && (!order.getTravellerLockFlag().equals("Y"));
					LOG.info("order:"+order.getOrderId()+"isBaiTiao"+isBaiTiao+"isOutBoundBu"+isOutBoundBu+"isTravelDelyAndNotLocked"+isTravelDelyAndNotLocked);
					//同时满足以上三个条件
					LOG.info("orderid:"+order.getOrderId()+" isBaiTiao:"+isBaiTiao+" isOutBoundBu:"+isOutBoundBu+" isTravelDelyAndNotLocked:"+isTravelDelyAndNotLocked);
					if(isBaiTiao && isOutBoundBu && isTravelDelyAndNotLocked){
						LOG.info("OrderPaymentSms:orderId:"+order.getOrderId()+"===order.getCategoryId():"+order.getCategoryId()+"==order.getMainOrderItemProductType():"+order.getMainOrderItemProductType());
						// 改变订单游玩人锁定状态
						int re=ordOrderTravellerService.updateOrderLockTraveller(order.getOrderId());
						if(re>0){
							order.setTravellerLockFlag("Y");
						}
					}
					//小驴白条游玩人后置支付新增逻辑end*/



				} else {
					resultHandle.setMsg("订单(ID=" + payment.getObjectId() + ")不存在。");
				}
			} else {
				resultHandle.setMsg("PayPayment is null.");
			}
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			resultHandle.setMsg(e);
		}

		return resultHandle;
	}
	
	/**
	 * 根据payment对象获得订单金额变换对象
	 */
	private OrdOrderAmountItem getOrdOrderAmountItem(PayPayment payment){
		OrdOrderAmountItem item = new OrdOrderAmountItem();
	 	item.setOrderId(payment.getObjectId());
        item.setItemAmount(-payment.getPromotionAmount());
        item.setOrderAmountType(OrderEnum.ORDER_AMOUNT_TYPE.PAY_PROMOTION_AMOUNT.name());
        item.setItemName(OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_PROMOTION.getCode());
        return item;
	}

	/**
	 *出境BU下发游客安全信息卡至游客邮箱
	 */
	@Override
	public void sendSafetyInfoEmail(OrdOrder order){
		LOG.info("sendSafetyInfoEmail start, orderId=" + order.getOrderId());
		try {
			List<OrdOrderItem> orderItems = iOrdOrderItemService.selectByOrderId(order.getOrderId());
			order.setOrderItemList(orderItems);
            if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(order.getCategoryId())
                    || BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(order.getCategoryId())
                    || BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(order.getCategoryId())
                    || BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCategoryId().equals(order.getCategoryId())
                    || BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(order.getCategoryId())) {
                sendEmail(order);
            } else if (BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(order.getCategoryId())) {
				ResultHandleT<ProdProduct> resultProduct=prodProductClientService.findRealProdProductsByOrder(order);
				if(resultProduct==null || resultProduct.isFail() || resultProduct.hasNull()){
					LOG.info("resultProduct is null or fail, orderId=" + order.getOrderId());
					return;
				}
				ProdProduct prodProduct=resultProduct.getReturnContent();
                if (prodProduct != null && "MULTIDAYTOUR".equals(prodProduct.getProducTourtType())) {
                    sendEmail(order);
                }
            }
			LOG.info("sendSafetyInfoEmail send success, orderId=" + order.getOrderId());
		} catch (Exception e) {
			LOG.info("sendSafetyInfoEmail method error, orderId=" + order.getOrderId());
		}
        LOG.info("sendSafetyInfoEmail end, orderId=" + order.getOrderId());
	}

	/**
	 *出境BU下发游客安全信息卡至游客邮箱
	 */
	private void sendEmail(OrdOrder order){
		LOG.info("sendEmail method start, orderId=" + order.getOrderId());
        OrdPerson contactPerson = null;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("objectId", order.getOrderId());
		params.put("objectType", OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
		params.put("personType", OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
		List<OrdPerson> ordPersonList = iOrdPersonService.findOrdPersonList(params);
        if (order != null && CollectionUtils.isNotEmpty(ordPersonList)) {
            for (OrdPerson person : ordPersonList) {
                String personType = person.getPersonType();
                if (OrderEnum.ORDER_PERSON_TYPE.CONTACT.name().equals(personType)) {
                    contactPerson = person;
                    break;
                }
            }
        }
        if (contactPerson != null && contactPerson.getEmail() != null) {
			LOG.info("contactPerson.getEmail()="+contactPerson.getEmail());
            File directioryFile = ResourceUtil.getResourceFile("/WEB-INF/resources/econtractTemplate");
            String fileName = "safetyInfo.docx";
            Long fileId = null;
            Object cached = null;
            try{
                cached =  MemcachedUtil.getInstance().get("memcached_default_safetyInfoEmail_key");
                if(cached != null) {
                    fileId = (Long)cached;
                    LOG.info("memcached fileId success, orderId=" + order.getOrderId() + "fileId=" + fileId);
                }
            }catch(Exception e){
                LOG.info("memcached safetyInfoEmail_key error, orderId=" + order.getOrderId());
            }
            if(cached == null) {
                try{
                    File file = new File(directioryFile, fileName);
//					FileInputStream fis = new FileInputStream(new File(directioryFile, fileName));
//					fileId = vstFSClient.uploadFile(fileName, fis, "COM_AFFIX");
//					fis.close();
                    fileId = vstFSClient.uploadFile(file, "COM_AFFIX");
                    LOG.info("upload file, orderId=" + order.getOrderId() + "fileId=" + fileId);
                    if (fileId != null && fileId != 0) {
                        MemcachedUtil.getInstance().set("memcached_default_safetyInfoEmail_key", fileId);
                    } else {
						LOG.info("fileId is null");
                        return;
                    }
                } catch (Exception e) {
                    LOG.info("upload file fail," + order.getOrderId());
                }
            }
            try {
                EmailContent emailContent = new EmailContent();
                emailContent.setContentText(getHtmlMailContent());
                emailContent.setFromAddress("service@cs.lvmama.com");
                emailContent.setFromName("驴妈妈旅游网");
                emailContent.setSubject("游客安全信息卡通知");
                emailContent.setToAddress(contactPerson.getEmail());

                List<EmailAttachment> emailAttachments = new ArrayList<EmailAttachment>();
                EmailAttachment emailAttachment = new EmailAttachment();
                emailAttachment.setFileId(fileId);
                emailAttachment.setFileName("游客安全信息卡.docx");
                emailAttachments.add(emailAttachment);
                LOG.info("开始发送游客安全信息卡");
                vstEmailServiceAdapter.sendEmailDirectFillAttachment(emailContent, emailAttachments);
            } catch (Exception e) {
                LOG.info("sendEmail error," + order.getOrderId());
            }
        }
        LOG.info("sendEmail method end, orderId=" + order.getOrderId());
	}

	private String getHtmlMailContent(){
		StringBuilder content = new StringBuilder("亲爱的驴友您好！<br/>");
		content.append("&nbsp;&nbsp;&nbsp;&nbsp;非常感谢您通过驴妈妈旅游预定了本次出行。为了您能拥有一次平安愉悦的旅行，请您将国家旅游局制定的《游客安全信息卡》（此邮件附件）打印并填写完整，并在旅行过程中随身携带。<br/>");
		content.append("&nbsp;&nbsp;&nbsp;&nbsp;《游客安全信息卡》中，包含：姓名、性别、身份证号码、血型、电话号码、过往病史、过敏史等信息。为了您的健康平安和顺利出行，请如实填写。在您的旅行过程中，有可能会被抽检是否携带此卡，为了您的顺利出行，请务必将此卡打印，并在旅游过程中随身携带。预祝您旅游愉快，谢谢！<br/>");
		content.append("<span style='float:right;'>驴妈妈旅行网</span><br/>");
		return content.toString();
	}

	/**
	 * 发送支付成功消息
	 * @param order
	 */
	private void sendPaymentMsg(OrdOrder order){
		// 消息与启动工作流解耦
		try {
			orderMessageProducer.sendMsg(MessageFactory.newOrderPaymentMessage(
					order.getOrderId(), OrderEnum.PAYMENT_STATUS.PAYED.getCode()));
			LOG.info("message send success, orderId=" + order.getOrderId());
			
			// 门票过期退意向消息推送
			sendExpiredRefundMsg(order);
		} catch (Exception e) {
			LOG.error("message send error, orderId=" + order.getOrderId());
		}
	}
	
	private void sendPartPaymentMsg(OrdOrder order){
		try {
			if(order != null && (order.getDistributorId() == com.lvmama.vst.comm.vo.Constant.DIST_O2O_SELL 
					|| order.getDistributorId() == com.lvmama.vst.comm.vo.Constant.DIST_O2O_APP_SELL)){
				LOG.info("o2o order send part pay msg orderId:"+order.getOrderId());
				orderMessageProducer.sendMsg(MessageFactory.newOrderPartPaymentMessage(
					order.getOrderId(), ""));
			}
		} catch (Exception e) {
			LOG.error("message send part pay error, orderId=" + order.getOrderId());
		}
	}
	
	/**
	 * 门票过期退意向消息推送
	 * <p> "11/12/13" + "TICKET_BU" + 非扫码购(DISTRIBUTOR_ID!=6)</p>
	 * 
	 * @param order OrdOrder
	 */
	private void sendExpiredRefundMsg(OrdOrder order) {
		boolean erBol = Constant.TICKET_CATEGORY_IDS.contains(order.getCategoryId()) 
				&& CommEnumSet.BU_NAME.TICKET_BU.getCode().equalsIgnoreCase(order.getBuCode())
				&& order.getDistributorId() != Constant.DIST_OFFLINE_EXTENSION;
		
		Long orderId = order.getOrderId();
		if (erBol) {
			try {
				LOG.info("SendExpiredRefundMsg: orderId=" + orderId);
				
				orderMessageProducer.sendMsg(MessageFactory.newExpiredRefundMessage(orderId, ""));
			} catch (Exception e) {
				LOG.error("SendExpiredRefundMsg is error!, orderId=" + orderId, e);
			}
		} else {
			LOG.info("Don't sendExpiredRefundMsg: orderId=" + orderId 
					+ String.format(",categoryId=%s,buCode=%s,distributorId=%s", order.getCategoryId(), order.getBuCode(), order.getDistributorId()));
		}
	}

	@Override
	public ResultHandle transferPaymentSuccess(List<PayPayment> paymentList) {
		ResultHandle handle = new ResultHandle();
		if(CollectionUtils.isEmpty(paymentList)){
			handle.setMsg("支付信息列表为空");
			LOG.info("paymentList is empty");
			return handle;
		}

		LOG.info("transferPaymentSuccess, orderId:" + paymentList.get(0).getObjectId());

		OrdOrder order = orderUpdateService.updateTransferOrder(paymentList);
		if(order==null){
			handle.setMsg("订单不存在");
			return handle;
		}

		if(order.hasPayed()){
			sendPaymentMsg(order);
			doPaymentSuccessMsg(order);
			//发送创建分摊消息
			sendOrderApportionMsg(order);
			/*目的地游玩人后置订单，设置补充游玩人最晚时间*/
            initDestBuTravDelayWaitTime(order);
		}

		if(order.getActualAmount()>order.getOughtAmount()){
			//自动退款操作
			ComJobConfig config = new ComJobConfig();
			config.setCreateTime(new Date());
			config.setObjectId(order.getOrderId());
			config.setObjectType("ORD_ORDER");
			config.setPlanTime(new Date());
			config.setRetryCount(1L);
			config.setJobType(ComJobConfig.JOB_TYPE.ORDER_TRANSFER_REFUNDMENT.name());
			comJobConfigService.saveComJobConfig(config);
		}
		return handle;
	}

	/**
	 * 发送创建分摊消息
	 * @param order
	 */
	private void sendOrderApportionMsg(OrdOrder order){
		//如果全额支付而且使用订单分摊子系统（lvmm-order-apportion），发送订单分摊消息
		try {
			if(order.hasFullPayment()&&order.hasPayed()&&ApportionUtil.isLvmmOrderApportion()){
				RequestBody<Long> request=new RequestBody<Long>();
				request.setT(order.getOrderId());
				LOG.info("sendOrderApportionMsg, orderId:" + order.getOrderId());
				apiOrderApportionService.sendOrderApportionMsg(request);
			}
		} catch (Exception e) {
			LOG.error("sendOrderApportionMsg failed, orderId:" + order.getOrderId(),e);
		}
	}


	@Override
	public ResultHandle doPaymentSuccessMsg(OrdOrder order) {
		LOG.info("doPaymentSuccessMsg, orderId:" + order.getOrderId());
		ResultHandle resultHandle = new ResultHandle();
		
		if(!order.hasNeedPrepaid()){
			LOG.info("现付订单不启动支付流程, orderId:" + order.getOrderId());
			return resultHandle;
		}
		
		ResultHandle handle = new ResultHandle();
		if (ActivitiUtils.hasActivitiOrder(order)) {
			try {
				OrderVo vo = ordOrderClientExtendService.initOrderVo(order, "payment");
				if (order2RouteService.isOrderRouteToNewWorkflow(vo)) {
					ordOrderClientExtendService.paymentOrderFromWorkflowNewPlus(vo);
					// 不调用setMsg,如此订单二期失败的不会走vst_order的job补偿
				} else if(orderRouteService.isOrderRouteToNewWorkflow(vo)) {
					if(!ordOrderClientExtendService.paymentOrderFromWorkflowNew(vo)) {
						handle.setMsg("doPaymentSuccessMsg error!");
					}
				} else {
					handle = processerClientService.paymentSuccess(createKeyByOrder(order));
				}
			} catch (Exception e) {
				// 启动异常处理
				handle.setMsg("doPaymentSuccessMsg error!");
			}
		}
		if(handle !=null && handle.isSuccess()){
			LOG.info("doPaymentSuccessMsg, send payment message, orderId:"
					+ order.getOrderId());
		}else{
			LOG.info("doPaymentSuccessMsg, error:"+ order.getOrderId());
			resultHandle.setMsg("doPaymentSuccessMsg, error");
			//记录需要补偿的单子
			OrdPayProcessJob record = ordPayProcessJobService.selectByPrimaryKey(order.getOrderId());
			if(record == null){
				record = new OrdPayProcessJob();
				record.setOrderId(order.getOrderId());
				record.setPayTime(order.getPaymentTime());
				record.setTimes(0L);
				ordPayProcessJobService.insert(record );
			}
		}
		return resultHandle;
	}

	@Override
	public void ordRefundment2UpdateSettlement(Long refundmentId) {
		orderMessageProducer.sendMsg(MessageFactory.newOrderRefundedSuccessMessage(refundmentId));
	}

	/**
	 * 批量人工分单
	 *
	 * isForce为true时为强制人工分单，不考虑人员的在线状态,也可以扩充其他条件
	 * isForce为false时为普通人工分单，考虑人员的在线状态
	 *
	 * @param auditIdStatusList 要符合"ID_STATUS"格式
	 * @param assignor 指派人
	 * @param orgIds  处理人组织
	 * @param isForce 是否强制分单
	 * @return
	 */
	public Map<String, Object> makeOrderAuditForManualAudit(final List<String> auditIdStatusList, final String assignor, final List<Long> orgIds,final String targetOperatorUser,final boolean isForce) {
		//组装页面提示信息
		Map<String, Object> message = new HashMap<String, Object>();
		List<ComAudit> successList = new ArrayList<ComAudit>();

		//初始化提示信息
		message.put("totalCount", auditIdStatusList.size());//页面选中的总数
		message.put("successCount", successList.size());//成功分单数
		message.put("failureCount", (auditIdStatusList.size()-successList.size()));//失败分单数

		//校验输入参数
		if(null == auditIdStatusList || auditIdStatusList.isEmpty()){
			if(LOG.isDebugEnabled()){//此判断要加,提高效率
				LOG.error("参数有错误，请好好检查!");
			}
			return message;
		}

		if(!UtilityTool.isValid(assignor)){
			if(LOG.isDebugEnabled()){
				LOG.error("参数有错误，请好好检查!");
			}
			return message;
		}

		if(null == orgIds || orgIds.isEmpty()){
			if(LOG.isDebugEnabled()){
				LOG.error("参数有错误，请好好检查!");
			}
			return message;
		}

		for(String auditIdStatus:auditIdStatusList){
			String[] auditIdStatusArr = auditIdStatus.split("_");
			Long auditId = Long.valueOf(auditIdStatusArr[0]);
			String auditStatus = auditIdStatusArr[1];

			//根据auditId查找订单对象
			ComAudit audit = orderDistributionBusiness.findOrderFromOrderPoolAndUnprocess(auditId);
			if(null == audit){
				if(LOG.isDebugEnabled()){
					LOG.debug("没有找到订单的活动对象!");
				}
				continue;
			}
			OrdOrder order = orderDistributionBusiness.findOrderFromOrder(audit);
			if(order==null){
				if(LOG.isDebugEnabled()){
					LOG.debug("没有找到订单的活动对象!");
				}
				continue;
			}
			audit.setAuditStatus(auditStatus);
			audit = orderDistributionBusiness.makeOrderAuditForManualAudit(order,audit,auditStatus,orgIds,targetOperatorUser,assignor,isForce);
			if(null != audit){
				signalProcess(order, audit,assignor);
				successList.add(audit);
			}else{
				if(LOG.isDebugEnabled()){
					LOG.debug("编号为["+order.getOrderId()+"]的订单人工分单失败");
				}
			}
		}

		//页面选中的总数
		message.put("totalCount", auditIdStatusList.size());
		//成功分单数
		message.put("successCount", successList.size());
		//失败分单数，有可能是被系统自动分单了，也有可能系统发生未知的运行时异常导致失败
		message.put("failureCount", (auditIdStatusList.size()-successList.size()));

		return message;
	}

	@Override
	public ComActivitiRelation getRelation(OrdOrder order){
		try{
			ComActivitiRelation comActiveRelation = comActivitiRelationService.queryRelation(order.getProcessKey(), order.getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
			if(comActiveRelation == null){//补偿机制,通过工作流再次去触发查询
				String processId = processerClientService.queryProcessIdByBusinessKey(ActivitiUtils.createOrderBussinessKey(order));
				LOG.info("===processerClientService.queryProcessIdByBusinessKey==orderId:"+order.getOrderId()+"====processId:"+processId);
				if(processId != null){
					comActivitiRelationService.saveRelation(order.getProcessKey(), processId, order.getOrderId(), ComActivitiRelation.OBJECT_TYPE.ORD_ORDER);
					comActiveRelation = new ComActivitiRelation();
					comActiveRelation.setObjectId(order.getOrderId());
					comActiveRelation.setObjectType(ComActivitiRelation.OBJECT_TYPE.ORD_ORDER.name());
					comActiveRelation.setProcessId(processId);
					comActiveRelation.setProcessKey(order.getProcessKey());
				}
			}
			return comActiveRelation;
		}catch(Exception e){
			LOG.error("ComActivitiRelation getRelation error:"+e);
		}
		return null;
	}

	@Override
	public void makeOrderAuditForSystem(ComAudit audit) {
		ComAudit result = orderDistributionBusiness.makeOrderAudit(audit);
		if(result != null){
			OrdOrder order = orderDistributionBusiness.findOrderFromOrder(audit);
			signalProcess(order, result, result.getOperatorName());
		}
	}



	private void signalProcess(final OrdOrder order,ComAudit audit,String assignor){
		if(ActivitiUtils.hasActivitiOrder(order)){
			ActivitiKey key = null;
			if(ActivitiUtils.hasChildTask(audit)){
				OrdOrderItem item = orderUpdateService.getOrderItem(audit.getObjectId());
				key = createKeyByOrderItem(item, ActivitiUtils.getOrderType(audit));
			}else{
				key=createKeyByOrder(order);
			}
			processerClientService.manualAssign(key, audit, audit.getOperatorName());
			LOG.info("object id:"+order.getOrderId()+",object_type:"+audit.getObjectType()+" activiti relation execution");
		}
		StringBuffer sb =new StringBuffer();
		sb.append("活动编号:");
		sb.append(audit.getAuditId());
		sb.append("分单");
		ComLog.COM_LOG_OBJECT_TYPE t=null;
		if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name().equals(audit.getObjectType())){
			t=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM;
		}else{
			t=ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER;
		}
		comLogClientService.insert(t,
				audit.getObjectId(),
				audit.getObjectId(),
				assignor,
				sb.toString(),
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CREATE_AUDIT.name(),
				OrderEnum.AUDIT_TYPE.getCnName(audit.getAuditType())+"任务分单",
				"");
	}

	@Override
	public ResultHandle updateCertificateStatus(OrdOrder order,
			OrderAttachment orderAttachment, String assignor, String memo) {
		LOG.info("updateCertificateStatus,order:"+order.getOrderId());
		ResultHandleT<ComAudit> handle = orderStatusManageService.updateCertificateStatus(order, orderAttachment, assignor, memo);
		if(handle.isSuccess()){
			if(ActivitiUtils.hasActivitiOrder(order) && !handle.hasNull()){
				ActivitiKey activitiKey = null;
				OrdOrderItem mainOrderItem = order.getMainOrderItem();
				if(OrdOrderUtils.isDestBuFrontOrderNew(order)){
					LOG.info("isDestBuFrontOrderNew not do completeTaskByAudit,orderId=" +order.getOrderId());
				}else{
					if(OrdOrderUtils.isDestBuFrontOrder(order)
							&& BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == order.getCategoryId()
							&& mainOrderItem != null && mainOrderItem.hasSupplierApi()){// 对接单酒店对接审核流程
						activitiKey = createKeyOfHotelDockByOrder(order, ActivitiUtils.getOrderType(handle.getReturnContent()));
					}else{
						activitiKey = createKeyByOrder(order);
					}
					LOG.info("processerClientService.completeTaskByAudit,activitiKey:"+activitiKey);
					processerClientService.completeTaskByAudit(activitiKey, handle.getReturnContent());
					LOG.info("processerClientService.completeTaskByAudit success.");
				}
			}
		}
		return handle;
	}

	@Override
	public ResultHandle updatePretrialAudit(OrdOrder order, String operator,
			String remark) {
		ResultHandleT<ComAudit> handle = orderStatusManageService.updatePretrialAudit(order, operator, remark);
		if(handle.isSuccess()){
			if(ActivitiUtils.hasActivitiOrder(order) && !handle.hasNull()) {
				OrderVo orderVo = new OrderVo();
				LOG.info("source order: " + JSON.toJSONString(order));
				EnhanceBeanUtils.copyProperties(order, orderVo);
				if (orderRouteService.isOrderRouteToNewWorkflow(orderVo)) {
					LOG.info("OrderRouteToNewWorkflow for orderId:" + order.getOrderId());
					AuditActiviTask auditActiviTask = new AuditActiviTask();
					EnhanceBeanUtils.copyProperties(handle.getReturnContent(), auditActiviTask);
					auditActiviTask.setOrderId(orderVo.getOrderId());
					apiOrderWorkflowService.completeTaskByAudit(new RequestBody<AuditActiviTask>().setTFlowStyle(auditActiviTask));

				} else {
					processerClientService.completeTaskByAudit(createKeyByOrder(order), handle.getReturnContent());

				}
			}
		}
		return handle;
	}

	/**
	 * 子订单凭证确认操作
	 * @param orderItem
	 * @param orderAttachment
	 * @param assignor
	 * @param memo
	 * @return
	 */
	public ResultHandle updateChildCertificateStatus(OrdOrderItem orderItem,OrderAttachment orderAttachment,String assignor,String memo)
	{

		ResultHandleT<ComAudit> handle = orderStatusManageService.updateChildCertificateStatus(orderItem, orderAttachment, assignor, memo);
		LOG.info("updateChildCertificateStatusOrderId:"+orderItem.getOrderItemId()+",handle:"+handle.isSuccess());
		if(handle.isSuccess()){

//			ComAudit audit = handle.getReturnContent();
			/*
			if(ActivitiUtils.hasActivitiOrder(order) && !handle.hasNull()){
				processerClientService.completeTaskByAudit(createKeyByOrder(order),handle.getReturnContent());
			}*/
			Long orderId=orderItem.getOrderId();
			OrdOrder order =this.queryOrdorderByOrderId(orderId);
			//解决巴士+酒目的地bu走国内流程问题
			if(Constant.BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())&&BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId() ==order.getCategoryId().longValue()&&BizEnum.BIZ_CATEGORY_TYPE.category_route_bus_hotel.getCategoryId() ==order.getSubCategoryId().longValue()){
				order.setBuCode(Constant.BU_NAME.LOCAL_BU.getCode());
			}
			LOG.info("updateChildCertificateStatusOrderId:"+orderItem.getOrderItemId()+",isdestbu:"+OrdOrderUtils.isDestBuFrontOrder(order)+",orderBuCode:"+order.getBuCode());
			if(OrdOrderUtils.isDestBuFrontOrder(order)){
				if(ActivitiUtils.hasActivitiOrder(order) && !handle.hasNull()){
					LOG.info("updateChildCertificateStatus-object id:"+order.getOrderId()+",object_type:"+handle.getReturnContent().getObjectType()+" activiti relation execution");
					if(OrdOrderUtils.isDestBuFrontOrderNew(order)){
						LOG.info("isDestBuFrontOrderNew not do completeTaskByAudit,orderId=" +order.getOrderId()
								+",orderItemId=" +orderItem.getOrderItemId());
					}else{
						ActivitiKey activitiKey = null;
						if(OrdOrderUtils.isDestBuFrontOrder(order)
								&& BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == orderItem.getCategoryId()
								&& orderItem.hasSupplierApi()){// 对接单酒店对接审核流程
							activitiKey = createKeyOfHotelDockByOrderItem(orderItem, ActivitiUtils.getOrderType(handle.getReturnContent()));
						}else{
							activitiKey = createKeyByOrderItem(orderItem, ActivitiUtils.getOrderType(handle.getReturnContent()));
						}
						LOG.info("processerClientService.completeTaskByAudit,activitiKey:"+activitiKey);
						processerClientService.completeTaskByAudit(activitiKey, handle.getReturnContent());
						LOG.info("processerClientService.completeTaskByAudit success.");
					}
					
				}

			}else if(Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())){
				if(ActivitiUtils.hasActivitiOrder(order) ){
					//酒店或者跟团游对接或者巴士+酒
					if((!handle.hasNull()&&order.isIncludeFlightHotel() &&BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() ==orderItem.getCategoryId().longValue())
							||(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId() ==orderItem.getCategoryId().longValue() && "Y".equals(orderItem.getContentMap().get("supplierApiFlag")))
							||(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId() ==order.getCategoryId().longValue()&&BizEnum.BIZ_CATEGORY_TYPE.category_route_bus_hotel.getCategoryId() ==order.getSubCategoryId().longValue()&&BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() ==orderItem.getCategoryId().longValue())
							||(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId() ==order.getCategoryId().longValue()&&BizEnum.BIZ_CATEGORY_TYPE.category_route_bus_hotel.getCategoryId() ==order.getSubCategoryId().longValue()&&BizEnum.BIZ_CATEGORY_TYPE.category_traffic_bus_other.getCategoryId() ==orderItem.getCategoryId().longValue())){
						try{
							LOG.info("updateChildCertificateStatus-object id:"+order.getOrderId()+",object_type:"+handle.getReturnContent().getObjectType()+" activiti relation execution");
							ActivitiKey activitiKey=createKeyByOrderItem(orderItem, ActivitiUtils.getOrderType(handle.getReturnContent()));
							LOG.info("processerClientService.completeTaskByAudit,activitiKey:"+activitiKey);
							processerClientService.completeTaskByAudit(activitiKey,handle.getReturnContent());
							LOG.info("processerClientService.completeTaskByAudit success.");
						}catch(Exception e){
							LOG.error("processerClientService.completeTaskByAudit fail.e:",e);
						}
					}
				}
			}
		}
		return handle;

	}
	
	@Override
	public ResultHandle updateChildCertificateStatus(final Long orderItemId,String assignor,String memo)
	{
		ResultHandle rh = new ResultHandle();
		OrdOrderItem orderItem = orderUpdateService.getOrderItem(orderItemId);
		if(orderItem==null){
			rh.setMsg("订单子项不存在");
			return rh;
		}
		if(!OrderEnum.CERT_CONFIRM_STATUS.UNCONFIRMED.name().equalsIgnoreCase(orderItem.getCertConfirmStatus())){
			rh.setMsg("订单子项凭证状态不能修改");
			return rh;
		}
		OrderAttachment oa = new OrderAttachment();
		return updateChildCertificateStatus(orderItem,oa,assignor,memo);
	}
	@Override
	public ResultHandle updateCancelConfim(OrdOrder order, String assignor,
			String memo) {
		ResultHandleT<ComAudit> handle = orderStatusManageService.updateCancelConfim(order, assignor, memo);
		if(handle.isSuccess()){
			if(ActivitiUtils.hasActivitiOrder(order) && !handle.hasNull()){
				processerClientService.completeTaskByAudit(createKeyByOrder(order),handle.getReturnContent());
			}
		}
		return handle;
	}

	@Override
	public ResultHandleT<ComAudit> updateChildCancelConfim(OrdOrderItem orderItem,String assignor,String memo){
		ResultHandleT<ComAudit> handle = orderStatusManageService.updateChildCancelConfim(orderItem, assignor, memo);
		/*if(handle.isSuccess()){
			if(ActivitiUtils.hasActivitiOrder(order) && !handle.hasNull()){
				processerClientService.completeTaskByAudit(createKeyByOrder(order),handle.getReturnContent());
			}
		}*/
		return handle;
	}

	@Override
	public ResultHandle updateOnlineRefundConfim(OrdOrder order,String assignor,String memo){
		ResultHandleT<ComAudit> handle = orderStatusManageService.updateOnlineRefundConfim(order, assignor, memo);
		if(handle.isSuccess()){
			orderRefundProcesserAdapter.completeTaskByOnlineRefundAudit(order, handle.getReturnContent());
		}
		return handle;
	}

	@Override
	public String getActivitiProcessId(Long orderId) {
		OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
		if(order!=null){
			ComActivitiRelation r=getRelation(order);
			if(r!=null){
				return r.getProcessId();
			}
		}
		return null;
	}


	private ActivitiKey createKeyByOrder(OrdOrder order){
		ComActivitiRelation relation = getRelation(order);
		LOG.info("createKeyByOrder order.orderid="+order.getOrderId());
		return new ActivitiKey(relation, ActivitiUtils.createOrderBussinessKey(order));
	}

	private ActivitiKey createKeyByOrderItem(OrdOrderItem item,String type){
		LOG.info("createKeyByOrderItem item.orderid="+item.getOrderId()+",type="+type);
		return new ActivitiKey((String)null, ActivitiUtils.createOrderBussinessKey(item, type));
	}

	@Override
	public ResultHandle validateOrderCombPack(BuyInfo buyInfo) {
		return orderCombPackService.validateOrderCombPack(buyInfo);
	}

	/**
	 *
	 * 保存日志
	 *
	 */
	private void insertOrderLog(final Long orderId,String assignor,String memo,String appendMessage){
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
				orderId,
				orderId,
				assignor,
				"编号为["+orderId+"]的订单，人工审核确认",
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(),
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName(),
				memo);
	}

	@Override
	public ResultHandle startBackOrder(Long orderId, String operatorId) {
		ResultHandle handle = new ResultHandle();
		final String startKey="VST_CANCEL_ORDER_"+orderId;
		try{
			if (SynchronizedLock.isOnDoingMemCached(startKey)) {
				handle.setMsg("并发发起操作");
				return handle;
			}
			OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
			ActivitiKey key = createKeyByOrder(order);
            /**
             *  2018-11-07 houjian 【WIFI电话卡后台下单 人工审核确认后 支付成功 系统仍然自动取消】问题修复
             */
//			if(order.isLocalBuFrontOrder()){
//				LOG.info("orderId:"+orderId+",is local bu delete Job");
			try {
				LOG.info("orderId:" + orderId + ",delete Job");
				processerClientService.deleteJobTask(key, BOUNDARY_TIMER, operatorId);
//			}
				processerClientService.completeTask(key, TASK_KEY.backBookManualTask.name(), operatorId);
				insertOrderLog(orderId, operatorId + "-人工审核确认", "", "");
			}catch(Exception e){
				LOG.info(String.format("ProcesserClientService invoke failed:%s",e.getMessage()));
			}
		}catch(Exception ex){
			handle.setMsg(ex);
		}finally{
			SynchronizedLock.release(startKey);
		}
		return handle;
	}

	@Override
	public ResultHandle generateEcontract(Long orderId, String operatorName) {
		return orderEcontractGeneratorService.generateEcontract(orderId, operatorName);
	}

	@Override
	public List<OrdMulPriceRate> queryOrdMulPriceRateByOrderItemId(Long orderItemId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", orderItemId);
		return ordMulPriceRateService.findOrdMulPriceRateList(params);
	}

	@Override
	public ResultHandle saveOrdOrderLosc(List<OrdOrderLosc> list) {
		ResultHandle handle = new ResultHandle();
		try{
			if(!CollectionUtils.isEmpty(list)){
				for (OrdOrderLosc ordOrderLosc : list) {
					ordOrderLoscService.addOrderLosc(ordOrderLosc);
				}
			}
		}catch(Exception ex){
			handle.setMsg(ex);
			LOG.info("{}", ex);
		}
		return handle;
	}

	@Override
	public Long cancelOrderDeductAmount(BuyInfo buyInfo) {
		return orderPriceService.cancelOrderDeductAmount(buyInfo);
	}

	@Override
	public ResultHandle validateCoupon(BuyInfo buyInfo) {
		return couponService.validateCoupon(buyInfo);
	}

	@Override
	public Pair<FavorStrategyInfo,Object> calCoupon(BuyInfo buyInfo) {
		return couponService.calCoupon(buyInfo);
	}

	/**
	 * 产生订单活动并且保存 待分配 状态
	 */
	public ComAudit saveCreateOrderAudit(Long objectId,String objectType,String auditType){
		//allocationMessageProducer
		ComAudit comAudit = orderAuditService.saveCreateOrderAudit(objectId, objectType, auditType);
		if(comAudit != null){
			allocationMessageProducer.sendMsg(MessageFactory.newOrderAllocationMessage(comAudit.getAuditId(), "TRUE"));
		}
		return comAudit;
	}
	@Override
	public ComAudit createItemTask(Long objectId,OrderEnum.AUDIT_TYPE type){
		LOG.info("orderService.createItemTask:objectId=" + objectId
				+ " OrderEnum.AUDIT_TYPE=" + type);
		Assert.notNull(type);
		boolean checkType=type.equals(OrderEnum.AUDIT_TYPE.CERTIFICATE_AUDIT)||type.equals(OrderEnum.AUDIT_TYPE.CANCEL_AUDIT);
		Assert.isTrue(checkType);
		boolean isOrder=false;
		OrdOrderItem  orderItem=iOrdOrderItemService.selectOrderItemByOrderItemId(objectId);
		if(orderItem!=null){
			OrdOrder oldOrder=this.orderUpdateService.queryOrdOrderByOrderId(orderItem.getOrderId());
			if(oldOrder.getCategoryId()==1){
				if(type.equals(OrderEnum.AUDIT_TYPE.CANCEL_AUDIT)
						&&CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equalsIgnoreCase(oldOrder.getBuCode())){
					return null;
				}
				isOrder=true;
			}
		}

		// 凭证确认或者取消确认活动只创建一次，alter by xiaoyulin
		String objectType = null;
		if(isOrder){
			objectType = OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name();
		}else{
			objectType = OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name();
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("auditType", type.name());
		params.put("objectType", objectType);
		params.put("objectId", objectId);
		List<ComAudit> auditList = orderAuditService.queryAuditListByCondition(params);
		if(auditList != null && auditList.size() > 0){
			return auditList.get(0);
		}

		if(isOrder){
			return saveCreateOrderAudit(orderItem.getOrderId(),OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name(),type.name());
		}else{
			return saveCreateOrderAudit(objectId,OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.name(),type.name());
		}
	}

	/**
	 * 是否存在某活动
	 * @param objectId
	 * @param objectType
	 * @param auditType
	 * @return
	 */
	public boolean isExistAudit(Long objectId,String objectType,String auditType){

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("objectId", objectId);
		param.put("objectType", objectType);
		param.put("auditType", auditType);
		int n=orderAuditService.getTotalCount(param);
		if (n>0) {
			return true;
		}
		return false;
	}

    /**
     * 根据订单ID，子订单ID，audit sub type查询 audit
     */
    public List<ComAudit> selectAuditBySubType(Long objectId,String objectType,String auditSubtype) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("objectId", objectId);
        param.put("objectType", objectType);
        param.put("auditSubtype", auditSubtype);
        List<ComAudit> list = orderAuditService.queryAuditListByParam(param);
        return list;
    }

	@Override
	public List<OrdOrder> getOrdListByCondition(
			ComplexQuerySQLCondition condition) {
		return complexQueryService.queryOrderListByCondition(condition);
	}


	@Override
	public ResultHandleT<List<OrdPassCode>> getOrdPassCodeByCheckInAndCode(Long checkInId,String addCode) {
		ResultHandleT<List<OrdPassCode>> handle = new ResultHandleT<List<OrdPassCode>>();
		try{
			handle.setReturnContent(supplierOrderHandleService.getOrdPassCodeByCheckInAndCode(checkInId, addCode));
		}catch(Exception e){
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}

		return handle;
	}

	@Override
	public ResultHandle saveReservation(ComMessage comMessage,OrderEnum.AUDIT_SUB_TYPE subType,Long objectId,OrderEnum.AUDIT_OBJECT_TYPE objectType,OrderEnum.AUDIT_TYPE type){
		ResultHandle handle = new ResultHandle();
		OrdOrderItem orderItem=null;
		try{
			Assert.hasText(comMessage.getMessageContent());
			if(StringUtils.isNotEmpty(comMessage.getSender())){
				comMessage.setSender("SYSTEM");
			}
			if(type==null){
				type=OrderEnum.AUDIT_TYPE.RESOURCE_AUDIT;
			}

			if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.equals(objectType)){
				comMessageService.saveReservation(comMessage, type.name(), subType.name(), objectId, "system", null);
			}else{
				orderItem = orderUpdateService.getOrderItem(objectId);
				if(orderItem!=null){
					comMessageService.saveReservationChildOrder(comMessage,type.name(),subType.name(),orderItem.getOrderId(),objectId,"system",null);
				}
			}
		} catch (Exception e) {
			if (Constants.NO_PERSON.equals(e.getMessage())) {

				String content="找不到分单人，创建预定通知失败";
				if(OrderEnum.AUDIT_OBJECT_TYPE.ORDER.equals(objectType)){
					this.comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
							objectId, objectId, comMessage.getSender(), content, ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_CHANGE.name(),
							ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_CHANGE.getCnName() + "[" + content + "]", content);
				}else{
					this.comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ITEM,
							orderItem.getOrderId(), objectId, comMessage.getSender(), content, ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_CHANGE.name(),
							ComLog.COM_LOG_LOG_TYPE.ORD_COM_MESSAGE_CHANGE.getCnName() + "[" + content + "]", content);

				}

				handle.setMsg(content);
			} else {
				LOG.error(ExceptionFormatUtil.getTrace(e));
				handle.setMsg(e);
			}

		}

		return handle;
	}
	public OrdOrder findExpressAddress(Long orderId) {


		OrderActivityParam orderActivityParam = new OrderActivityParam();//订单活动

		OrderExcludedParam orderExcludedParam = new OrderExcludedParam();//订单排除
		OrderIndentityParam orderIndentityParam = new OrderIndentityParam();//订单主键
		OrderPageIndexParam orderPageIndexParam = new OrderPageIndexParam();//订单分页
		OrderStatusParam orderStatusParam = new OrderStatusParam();//订单状态
		OrderTimeRangeParam orderTimeRangeParam = new OrderTimeRangeParam();//订单时间

		OrderContentParam orderContentParam = new OrderContentParam();//订单内容
		OrderFlagParam orderFlagParam = new OrderFlagParam();//订单表标志
		orderFlagParam.setOrderPersonTableFlag(true);
		orderFlagParam.setOrderAddressTableFlag(true);

		List<OrderSortParam> orderSortParamList = new ArrayList<OrderSortParam>();//订单排序
		orderSortParamList.add(OrderSortParam.CREATE_TIME_DESC);

		HashSet<Long> set=new HashSet<Long>();
		set.add(orderId);
		orderContentParam.setOrderIds(set);
		orderContentParam.setPersoType(OrderEnum.ORDER_PERSON_TYPE.ADDRESS.name());
		//设置到接口参数
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		condition.setOrderContentParam(orderContentParam);
		condition.setOrderSortParams(orderSortParamList);

		condition.setOrderActivityParam(orderActivityParam);

		condition.setOrderExcludedParam(orderExcludedParam);
		condition.setOrderFlagParam(orderFlagParam);
		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderIndentityParam(orderIndentityParam);
		condition.setOrderPageIndexParam(orderPageIndexParam);

		condition.setOrderStatusParam(orderStatusParam);
		condition.setOrderTimeRangeParam(orderTimeRangeParam);

		//调用接口
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);
		/*
		OrderContentParam orderContentParam = new OrderContentParam();//订单内容
		OrderFlagParam orderFlagParam = new OrderFlagParam();//订单表标志
		orderFlagParam.setOrderPersonTableFlag(true);
		orderFlagParam.setOrderAddressTableFlag(true);

		List<OrderSortParam> orderSortParamList = new ArrayList<OrderSortParam>();//订单排序
		orderSortParamList.add(OrderSortParam.CREATE_TIME_DESC);

		HashSet<Long> set=new HashSet<Long>();
		set.add(orderId);
		orderContentParam.setOrderIds(set);
		orderContentParam.setPersoType(OrderEnum.ORDER_PERSON_TYPE.ADDRESS.name());
		//设置到接口参数
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
		condition.setOrderContentParam(orderContentParam);
		condition.setOrderSortParams(orderSortParamList);

		//调用接口
		List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);

		*/
		if (CollectionUtils.isNotEmpty(orderList)) {
			return orderList.get(0);
		}else{
			return null;
		}

	}

	@Override
	public ResultHandle saveOrderPerson(Long orderId, BuyInfo buyInfo, String operatorId) {
		//身份证输入规范中的字母为大写，所以在此统一转为大写
		if(buyInfo != null){
			List<Person> travellers = buyInfo.getTravellers();
			if(travellers != null && travellers.size() > 0){
				for(Person person : travellers){
					if(person != null && !StringUtil.isEmptyString(person.getIdNo())){
						person.setIdNo(person.getIdNo().toUpperCase());
					}
				}
			}
		}
		ResultHandle handle = new ResultHandle();
		// 判断购买人和紧急联系人包含游玩人名称中是否包含“测试下单”关键字，包含测试关键字时，将该订单标识为测试订单，标识设置成Y。默认是N
		//获取测试订单
	    //得到定义的订单测试的常量
		char isTest='N';
		try {
			isTest = buyInfo.getIsTestOrder();
			} catch (NullPointerException e) {

			}
		if(isTest!='Y'){

			String orderValue=Constant.getInstance().getOrderValue();
			String  contactName="";
		    String  emergencyPersonName="";
		    try {
					contactName = buyInfo.getContact().getFullName();
			    } catch (NullPointerException e) {
					}
			try {
					emergencyPersonName = buyInfo.getEmergencyPerson().getFullName();
			} catch (NullPointerException e) {

			}
		     // 如果购买人和联系人都不包含测试下单，那么就在游玩人中查找是否包含。直接包含直接设置成测试订单的标记。
			if ((StringUtil.isNotEmptyString(contactName)&&contactName.contains(orderValue)) || (StringUtil.isNotEmptyString(emergencyPersonName)&&emergencyPersonName.contains(orderValue))) {
				buyInfo.setIsTestOrder('Y');
				} else
					try {
							{
								List<Person> travellers = buyInfo.getTravellers();
							    for (Person person : travellers) {
							    if (person.getFullName().contains(orderValue)) {
								buyInfo.setIsTestOrder('Y');
								break;
								}
							    }
							}
						} catch (NullPointerException e) {
						}
				    }
		    try{
			if('Y' == buyInfo.getIsTestOrder()) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("isTestOrder", buyInfo.getIsTestOrder());
				parameters.put("orderId", orderId);
				int c = orderUpdateService.updateIsTestOrder(parameters);
				if(c != 1) {
					String msg = "update order:" + orderId + "  to test order failed, updated rows " + c;
					LOG.error(msg);
					handle.setMsg(msg);
					return handle;
				}
			}

			handle = bookService.saveOrderPerson(orderId,buyInfo);
			OrdOrder order = queryOrdorderByOrderId(orderId);
//			boolean isLockSeatSuccess = lockSeatForFlightOrderMult(order);

			String specialTicket = getSpecialTicket(order);
			if(order.getDistributorId() == Constant.DIST_BACK_END && StringUtils.isNotEmpty(specialTicket)) {
                if(!checkLimit(buyInfo)){
                    //				Log.info(ComLogUtil.printTraceInfo(methodName,"上海迪斯尼锁库存失败", "this.reservationOrder", System.currentTimeMillis() - startTime));
                    cancelAfterLockStockFail(order);
                    handle = new ResultHandleT<OrdOrder>();
                    handle.setMsg("由于当前预订人数较多，请您稍后再试！！");
                    return handle;
                }
                Object[] array = reservationSupplierOrder(order);
                boolean isSuccess = (Boolean) array[0];
                if(!isSuccess){
    //				Log.info(ComLogUtil.printTraceInfo(methodName,"上海迪斯尼锁库存失败", "this.reservationOrder", System.currentTimeMillis() - startTime));
                    cancelAfterLockStockFail(order);
                    handle = new ResultHandleT<OrdOrder>();
                    //handle.setMsg("抱歉，上海迪士尼占用库存失败！");
                    handle.setMsg("抱歉，"+specialTicket+"占用库存失败！");
                    return handle;
                }
			}
			
			//门票邮寄订单       将需要的信息存到表中    以便ebk查询邮寄订单
			if(order.getDistributorId() == Constant.DIST_BACK_END && order.getAddressPerson() != null){
				for (OrdOrderItem ordOrderItem : order.getOrderItemList()){
					//后台下单  实体票     门票/WIFI      寄件方是供应商
					if(ordOrderItem.hasExpresstypeDisplay()
							&&(OrderUtils.isTicketByCategoryId(ordOrderItem.getCategoryId()) || ordOrderItem.getCategoryId() == 28L)
							&&SuppGoods.EXPRESSTYPE.SUPPLIER.name().equals(ordOrderItem.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.express_type.name()))){
						OrdTicketPost post = new OrdTicketPost();
						BeanUtils.copyProperties(ordOrderItem, post);

						//取票人   先取第一游玩人  第一游玩人为空  取联系人
						OrdPerson traveller  = order.getFirstTravellerPerson();
						if(traveller != null){
							post.setFullName(traveller.getFullName());
							post.setMobie(traveller.getMobile());
						}else{
							OrdPerson contact = order.getOnlyContactPerson();
							post.setFullName(contact.getFullName());
							post.setMobie(contact.getMobile());
						}

						OrdPerson addresser = order.getAddressPerson();
						post.setAddressName(addresser.getFullName());
						post.setAddressMobile(addresser.getMobile());

						OrdAddress  address = order.getOrdAddress();
						post.setAddress(StringUtil.coverNullStrValue(address.getProvince())
								+StringUtil.coverNullStrValue(address.getCity())
								+StringUtil.coverNullStrValue(address.getDistrict())
								+StringUtil.coverNullStrValue(address.getStreet()));


						try {
							ordTicketPostService.insertOrdTicketPost(post);
						} catch (Exception e) {
							LOG.info("ordTicketPostService.insertOrdTicketPost error:"+e.getMessage());
						}
					}
				}
			}

			if(order.hasNeedPrepaid() && order.getDistributorId() == Constant.DIST_BACK_END){
				if(order.getOughtAmount()==0&&!order.isNeedResourceConfirm()){//操作0元支付
					LOG.info("ordPrePayServiceAdapter.vstOrder0YuanPayMsg()"+order.getOrderId());
					ordPrePayServiceAdapter.vstOrder0YuanPayMsg(order.getOrderId());
				}

				if(order.hasPayed()){
					ActivitiKey key = createKeyByOrder(order);
					processerClientService.completeTask(key, TASK_KEY.backSaveTraveller.name(), operatorId);
				}
			}

//			if(!isLockSeatSuccess) {
//				handle.setMsg("机票锁舱失败，请重新下单或联系客服下单");
//				cancelAfterLockSeatFail(order);
//			}
		}catch(Exception ex){
			handle.setMsg(ex);
		}
		return handle;
	}

	@Override
	public OrdOrder queryUserFirstOrder(Long userId){
		Long orderId = orderUpdateService.queryUserFirstOrder(userId);
		if(orderId==null){
			return null;
		}
		return queryOrdorderByOrderId(orderId);
	}



	@Override
	public List<OrdTravelContractVo> queryTravelContractList(Long orderId){
		List<OrdTravelContract> ordTravelContractList=findOrdTravelContract(orderId);
		List<OrdTravelContractVo> travelContractVoList= new ArrayList<OrdTravelContractVo>();
		List<OrdOrderItem> orderItems = iOrdOrderItemService.selectByOrderId(orderId);
		Map<Long,OrdOrderItem> orderItemMap= new HashMap<Long,OrdOrderItem>();

		if(orderItems!=null){
			for(OrdOrderItem orderItem:orderItems){
				orderItemMap.put(orderItem.getOrderItemId(), orderItem);
			}
		}

		if(ordTravelContractList!=null){
			for(OrdTravelContract contract:ordTravelContractList){
				OrdTravelContractVo contractVo = new OrdTravelContractVo();
				BeanUtils.copyProperties(contract, contractVo);
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("ordContractId", contractVo.getOrdContractId());
				List<OrdItemContractRelation> conRelList = iOrdItemContractRelationService.findOrdItemContractRelationList(params);
				//合同类型
				contractVo.setContractTemplateName(ELECTRONIC_CONTRACT_TEMPLATE.getCnName(contractVo.getContractTemplate()));
				//合同状态
				contractVo.setStatusName(OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.getCnName(contractVo.getStatus()));
				//签约方式
				contractVo.setSigningTypeName(OrderEnum.ORDER_CONTRACT_SIGNING_TYPE.getCnName(contractVo.getSigningType()));

				String attUrl = contractVo.getAttachementUrl();
				if(StringUtils.isNotEmpty(attUrl)){
					ComFileMap comFile= ordTravelContractService.getComFileMapByFileName(attUrl);
					if(comFile!=null&&comFile.getComFileId()!=null){
						contractVo.setAttachementFileId(comFile.getFileId().toString());
					}else{
						contractVo.setAttachementFileId(null);
					}
				}
				contractVo.setOrderItemList(getOrderItemByContract(orderItemMap, conRelList));
				travelContractVoList.add(contractVo);
			}
		}
		return travelContractVoList;
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

	@Override
	public List<OrdTravelContract> findOrdTravelContract(Long orderId) {
		Map<String, Object> parametersTravelContract = new HashMap<String, Object>();
		parametersTravelContract.put("orderId",orderId);
		List<OrdTravelContract> ordTravelContractList=ordTravelContractService.findOrdTravelContractList(parametersTravelContract);
		return ordTravelContractList;
	}

	@Override
	public List<OrdTravelContract> findOrdTravelContractListByParams(Map<String, Object> params){
		return ordTravelContractService.findOrdTravelContractList(params);
	}

	@Override
	public String payFromBonusForVstOrder(String userNo,Long orderId,Long amount){
		return bonusPayService.payFromBonusForVstOrder(userNo, orderId, amount);
	}


	@Override
	public ResultHandleT<List<OrdPassCode>> selectOrdPassCodeByParams(
			Map<String, Object> params) {
		ResultHandleT<List<OrdPassCode>> handle = new ResultHandleT<List<OrdPassCode>>();
		try{
			handle.setReturnContent(supplierOrderHandleService.selectOrdPassCodeByParams(params));
		}catch(Exception e){
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		return handle;
	}

	@Override
	public ResultHandleT<List<OrdTicketPerform>> selectTicketPerform(Map<String, Object> params) {
		ResultHandleT<List<OrdTicketPerform>> handle = new ResultHandleT<List<OrdTicketPerform>>();
		try{
			handle.setReturnContent(supplierOrderHandleService.selectOrdTicketPerforms(params));
		}catch(Exception e){
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		return handle;
	}

	@Override
	public ResultHandleT<Integer> setOrderRebateFlagToSucc(Long orderId){
		ResultHandleT<Integer> resultHandle = new ResultHandleT<Integer>();
		if(orderId!=null){
			OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
			if(order==null){
				resultHandle.setMsg("订单不存在");
				return resultHandle;
			}
			OrdOrder updateOrder = new OrdOrder();
			updateOrder.setOrderId(order.getOrderId());
			updateOrder.setRebateFlag("Y");
			int result = orderUpdateService.updateByPrimaryKeySelective(updateOrder);
			resultHandle.setReturnContent(result);

		}else{
			resultHandle.setMsg("orderId为空");
		}
		return resultHandle;
	}


	@Override
	public ResultHandle updateOrderperon(Long orderId, OrdOrderPersonVO vo) {
		ResultHandle handle = new ResultHandle();
		OrdOrder order = queryOrdorderByOrderId(orderId);
		if(order==null){
			handle.setMsg("订单不存在");
			return handle;
		}
		ResultHandleT<String> result = orderUpdateService.updateOrderPerson(order,vo);
		if(result.isSuccess()&&!result.hasNull()){
			orderMessageProducer.sendMsg(MessageFactory.newOrderModifyPersonMessage(orderId, result.getReturnContent()));

			//这一步放到ebk去做
			/*if(CollectionUtils.isNotEmpty(order.getOrderItemList())){
				for (OrdOrderItem orderItem : order.getOrderItemList()) {
					if((BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(orderItem.getCategoryId())
							||BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(orderItem.getCategoryId()))
							&&orderItem.hasContentValue("fax_flag","Y")){
						ComMessage comMessage = new ComMessage();
						comMessage.setMessageContent("子订单号"+orderItem.getOrderItemId()+"，游玩人修改，请进行跟踪确认.");
						comMessage.setMessageStatus(OrderEnum.MESSAGE_STATUS.UNPROCESSED.name());
						saveReservation(comMessage, OrderEnum.AUDIT_SUB_TYPE.CHANG_ORDER, orderItem.getOrderItemId(), OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM, OrderEnum.AUDIT_TYPE.BOOKING_AUDIT);
					}
				}

			}*/
		}
		return handle;
	}

	/**
	 * 下单页面需要展示的合同模板
	 * @param buyInfo
	 * @return
	 */
	@Override
	public Set<String> getOrderPageNeedContractTemp(BuyInfo buyInfo){
		Set<String> contractTemplates = new HashSet<String>();
		OrdOrder tempOrder = bookService.initOrderAndCalc(buyInfo);
		LOG.info("getOrderPageNeedContractTemp合同模板BUCODE=="+tempOrder.getBuCode());
		boolean hasPreauthBook=false;//强制预授权是否
		if (TimePriceUtils.hasPreauthBook(tempOrder.getLastCancelTime(),tempOrder.getCreateTime())) {
			hasPreauthBook=true;
		}
		List<ProdProduct> prodProducts = new ArrayList<ProdProduct>();
		boolean  preSalesContarctFlag=false;
		//判断是否是台湾旅游须知标记
		boolean taiwanTravelContarctFlag = false;
		boolean zheJiangDongGangContarctFlag = false;
		List<Product> products = buyInfo.getProductList();
		ProdProductParam param = new ProdProductParam();
		 param.setDest(true);
		 param.setLineRoute(true);
		 param.setBizDistrict(true);
		 param.setProdEcontract(true);
		 if(products!=null){
			 for(Product pro:products){
				 ProdProduct Product = productClientService.findLineProductByProductId(pro.getProductId(), param).getReturnContent();
				 if(Product!=null){
					 if(Product.getProdEcontract()!=null &&
							 ELECTRONIC_CONTRACT_TEMPLATE.PRESALE_AGREEMENT.name().equals(Product.getProdEcontract().getEcontractTemplate())){
						 preSalesContarctFlag=true;
						 break;
					 }
					 if(Product.getProdEcontract()!=null &&
							 ELECTRONIC_CONTRACT_TEMPLATE.TAIWAN_AGREEMENT.name().equals(Product.getProdEcontract().getEcontractTemplate())){
						 taiwanTravelContarctFlag= true;
						 break;
					 }
					 if(Product.getProdEcontract()!=null &&
							 ELECTRONIC_CONTRACT_TEMPLATE.DONGGANG_ZHEJIANG_CONTRACT.name().equals(Product.getProdEcontract().getEcontractTemplate())){
						 zheJiangDongGangContarctFlag= true;
						 break;
					 }
					 prodProducts.add(Product);
				 }
			 }
		 }

		 if(!preSalesContarctFlag || !taiwanTravelContarctFlag){
			 List<Item> items = buyInfo.getItemList();
			 if(null!=items && !items.isEmpty()){
					for (Item item : items){
							Long goodsId = item.getGoodsId();
							SuppGoods goods = suppGoodsService.findSuppGoodsById(goodsId).getReturnContent();
							if(goods!=null){
								ProdProduct Product = productClientService.findLineProductByProductId(goods.getProductId(), param).getReturnContent();
								if(Product!=null){
									if(Product.getProdEcontract()!=null &&
											ELECTRONIC_CONTRACT_TEMPLATE.PRESALE_AGREEMENT.name().equals(Product.getProdEcontract().getEcontractTemplate())){
										preSalesContarctFlag=true;
										 break;
									 }
									if(null !=Product.getProdEcontract() && ELECTRONIC_CONTRACT_TEMPLATE.TAIWAN_AGREEMENT.name().equals(Product.getProdEcontract().getEcontractTemplate())){
										taiwanTravelContarctFlag = true;
										break;
									}
									if(null !=Product.getProdEcontract() && ELECTRONIC_CONTRACT_TEMPLATE.DONGGANG_ZHEJIANG_CONTRACT.name().equals(Product.getProdEcontract().getEcontractTemplate())){
										zheJiangDongGangContarctFlag = true;
										break;
									}
									prodProducts.add(Product);
								}
							}
						}
					}
		 }
		 if(preSalesContarctFlag){
			 contractTemplates.add(com.lvmama.vst.back.prod.po.ProdEcontract.ELECTRONIC_CONTRACT_TEMPLATE.PRESALE_AGREEMENT.name());
		 }else if (taiwanTravelContarctFlag){
			 contractTemplates.add(com.lvmama.vst.back.prod.po.ProdEcontract.ELECTRONIC_CONTRACT_TEMPLATE.TAIWAN_AGREEMENT.name());
		 }else if(zheJiangDongGangContarctFlag){
			 contractTemplates.add(com.lvmama.vst.back.prod.po.ProdEcontract.ELECTRONIC_CONTRACT_TEMPLATE.DONGGANG_ZHEJIANG_CONTRACT.name());
		 }else{
			if(prodProducts!=null){
				for(ProdProduct prod:prodProducts){

					String categoryCode = prod.getBizCategory().getCategoryCode();
					String productType = prod.getProductType();
					ProdEcontract prodEcontract = prod.getProdEcontract();
					//跟团游
					 if("category_route_group".equals(categoryCode)){
						 //国内短线
						 if(PRODUCTTYPE.INNERSHORTLINE.getCode().equals(productType)){
							//国内团队旅游合同
							contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.getCode());
						}
						 //国内长线
						 if(PRODUCTTYPE.INNERLONGLINE.getCode().equals(productType)){
							 //国内团队旅游合同
							 contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.getCode());
						 }

						//出境/港澳台,国内边境游和出境使用一样的合同
						 if(PRODUCTTYPE.FOREIGNLINE.getCode().equals(productType) || PRODUCTTYPE.INNER_BORDER_LINE.getCode().equals(productType)){
							if(PRODUCTTYPE.FOREIGNLINE.getCode().equals(productType) && prodEcontract != null && ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())) {
								LOG.info("getOrderPageNeedContractTemp group_foreign and productId is "+prodEcontract.getProductId());
								//出境旅游委托服务协议
								contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode());
							} else {
								//出境旅游合同
								contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_OUTBOUND_TOURISM.getCode());
							}
							// 预付款协议
							if(hasPreauthBook || ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())) {
								contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode());
							}
						 }
					}
					 
					boolean relatedMarketingFlag = isExsitLocalRouteItemInOrder(tempOrder);//判断订单是否有"关联销售当地游"订单
					boolean orderItemLocalOrGroupRouteFlag = isLocalOrGroupRouteOrderItem(tempOrder.getOrderItemList());//该子订单是否包含当地游，跟团游产品
							
					 //自由行
					 if(BIZ_CATEGORY_TYPE.category_route_freedom.getCode().equals(categoryCode)){
						 if(ProductPreorderUtil.isDestinationBUDetail(prod)){
							//目的地委托服务协议
							 contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.getCode());
						 }else{
							    //下单是否包含（关联销售（当地游），选择线路（当地游，跟团游））
							    if(relatedMarketingFlag || orderItemLocalOrGroupRouteFlag){
									//国内团队旅游合同
									contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.getCode());
									
								}else{
									//委托服务协议
									contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode());
								}
						 }
						 //国外
						if(PRODUCTTYPE.FOREIGNLINE.getCode().equals(productType)){
							//预付款协议
							if(hasPreauthBook || ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())) {
								contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode());
							}
						}
					}
					
					//酒店套餐
					 if(BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCode().equals(categoryCode)){
						 if(ProductPreorderUtil.isDestinationBUDetail(prod)){
							//目的地委托服务协议
							 contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.getCode());
						 }else{
							 contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode());
						 }
						 //国外
						if(PRODUCTTYPE.FOREIGNLINE.getCode().equals(productType)){
							//预付款协议
							if(hasPreauthBook || ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())) {
								contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode());
							}
						}
					}
					 
					 

					//当地游
					 if(BIZ_CATEGORY_TYPE.category_route_local.getCode().equals(categoryCode)){
						//国内
						 if(PRODUCTTYPE.INNERLINE.getCode().equals(productType) || PRODUCTTYPE.INNERSHORTLINE.getCode().equals(productType) || PRODUCTTYPE.INNERLONGLINE.getCode().equals(productType)){
							//国内团队旅游合同
							contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.getCode());

						 }
						//国外
						 if(PRODUCTTYPE.FOREIGNLINE.getCode().equals(productType)){
							 //委托服务协议
							 contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode());
							if(hasPreauthBook || ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())) {
								contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode());
							}
						 }
					}

					 //签证
					 if(BIZ_CATEGORY_TYPE.category_visa.getCode().equals(categoryCode)){
						 //委托服务协议
						 contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode());
					 }

					//定制游
					 if(BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.name().equals(categoryCode)){
						 //国内短线
						 if(PRODUCTTYPE.INNERSHORTLINE.getCode().equals(productType)){

							if(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())){
								//委托服务协议
								contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode());
							}else{
								//国内团队旅游合同
								contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.getCode());
							}
						 }
						 //国内长线
						 if(PRODUCTTYPE.INNERLONGLINE.getCode().equals(productType)){

							 if(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())){
								 //委托服务协议
								 contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode());
							 }else{
								 //国内团队旅游合同
								 contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_WITHIN_TERRITORY.getCode());
							 }
						 }

						 //出境/港澳台,国内边境游和出境使用一样的合同
						 if(PRODUCTTYPE.FOREIGNLINE.getCode().equals(productType)){
							if(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())){
								//委托服务协议
								contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode());
							}else{
								//出境旅游合同
								contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.TEAM_OUTBOUND_TOURISM.getCode());
							}
							// 预付款协议
							if(hasPreauthBook || ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.name().equalsIgnoreCase(prodEcontract.getEcontractTemplate())) {
								contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode());
							}
						 }
					}

				}
			}
			if(contractTemplates.size()>1){
				for(String temp:contractTemplates){
					if(temp.equals(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode())){
						contractTemplates.clear();
						contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.PREPAYMENTS.getCode());
					}
				}
				if(null !=tempOrder.getBuCode() && CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equalsIgnoreCase(tempOrder.getBuCode())){
					LOG.info("-----目的地委托服务协议模板-----");
					boolean isDestAgement = false;
					for(String temp:contractTemplates)
					{
						if(temp.equals(ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.getCode())||temp.equals(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode())){
							isDestAgement = true;
							LOG.info("-----目的地委托服务协议模板isDestAgement==true-----");
						}
					}
					if(isDestAgement)
					{
						contractTemplates.clear();
						LOG.info("-----目的地委托服务协议模板-----");
						contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.getCode());
					}
				}else
				{
					for(String temp:contractTemplates){
						if(temp.equals(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode())){
							contractTemplates.clear();
							contractTemplates.add(ELECTRONIC_CONTRACT_TEMPLATE.COMMISSIONED_SERVICE_AGREEMENT.getCode());
						}
					}
				}

			}
		 }
		return contractTemplates;
	}
	
	//判断订单是否有"关联销售当地游"订单
		protected boolean isExsitLocalRouteItemInOrder(OrdOrder order) {
			boolean relatedMarketingFlag = false;
			List<OrdOrderItem> OrdOrderItemList = order.getOrderItemList();
			
			for(OrdOrderItem ordOrderItem : OrdOrderItemList){
				if(ordOrderItem.getContent() != null  && ordOrderItem.getContent().length()>0){
					String relatedMarketingFlagStr = (String)ordOrderItem.getContentValueByKey("relatedMarketingFlag");
					if(relatedMarketingFlagStr != null && "localRoute".equals(relatedMarketingFlagStr)){
						relatedMarketingFlag = true;
					}
				}
			}
			return relatedMarketingFlag;
		}
		//该子订单是否包含当地游，跟团游产品
		protected boolean isLocalOrGroupRouteOrderItem(List<OrdOrderItem> ordOrderItem) {
			boolean orderItemLocalRouteFlag = false;
			if (ordOrderItem != null && ordOrderItem.size() > 0) {
				for (OrdOrderItem item : ordOrderItem) {
					if (item != null && (item.getCategoryId().longValue() == 16 || item.getCategoryId().longValue() == 15)) {
						orderItemLocalRouteFlag = true;
					}
				}
			}
			return orderItemLocalRouteFlag;
		}

	/**
	 * 查询生成预付款协议的目的地列表
	 * @return
	 */
	public List<String> getCreatePrePayEcontractDest(String code){
		String prePayDestStr = Constant.getInstance().getProperty(code);
		List<String> prePayDestIds = new ArrayList<String>();
		if(StringUtils.isNotEmpty(prePayDestStr)){
			JSONObject json = JSONObject.fromObject(prePayDestStr);
			Set<String> keys = json.keySet();
			for(String key:keys){
				prePayDestIds.add(json.getString(key));
			}
		}
		LOG.info("prePayDestIds========================"+prePayDestIds);
		return 	prePayDestIds;
	}

	/**
	 * 判断行程中是否包含北京一日游
	 * @param product
	 * @return
	 */
	public boolean isBeijingOneDayTravel(ProdProduct product,BuyInfo buyInfo){
		 //行程
		 ProdLineRoute reute =new ProdLineRoute();
		 if(buyInfo!=null && buyInfo.getProdLineRoute()!=null){
			 reute=buyInfo.getProdLineRoute();
		 }
		//始发地
		 if(product.getBizDistrict()==null){
			 return false;
		 }
		 long begin = product.getBizDistrict().getDistrictId();

		 String beijinDistrictIdStr = Constant.getInstance().getProperty("create_beijin_econtract_district");
		 if(StringUtils.isEmpty(beijinDistrictIdStr)){
				LOG.error("始发地区域北京Id未配置");
		 }
		 long beijinDistrictId = Integer.parseInt(beijinDistrictIdStr);

		String beijinDestId = Constant.getInstance().getProperty("create_beijin_econtract_dest");
		if(StringUtils.isEmpty(beijinDestId)){
			LOG.error("目的地北京Id未配置");
		}
		List<String> dest = new ArrayList<String>();
		dest.add(beijinDestId);

		//目的地是否包含北京
		boolean destBeijing = isExistAssignDestination(product, dest);
		//始发地北京 目的地北京  行程1天，入住0
		if(destBeijing&&begin==beijinDistrictId&&reute.getRouteNum()==1&&reute.getStayNum()==0){
			return true;
		}
		return false;
	}

	/**
	 * 判断目的地是否包含指定地区中
	 * @param
	 * @param
	 * @return
	 */
	public boolean isExistAssignDestination(ProdProduct product,List<String> destIdList){
		List<ProdDestRe> relist = product.getProdDestReList();
		if(relist!=null&&relist.size()>0){
			for(ProdDestRe re:relist){
				if(re.getDestId()==null){
					return false;
				}
				Map<String, Object> foreignParams = new HashMap<String, Object>();
				foreignParams.put("destId", re.getDestId());
				foreignParams.put("destIdList", destIdList);
				ResultHandleT<Integer> resultT = destContentClientService.hasExistAssignDest(foreignParams);
				if(resultT!=null && resultT.isSuccess() && resultT.getReturnContent()!=null){
					if(resultT.getReturnContent().intValue()!=0){
						return true;
					}
				}
//				int result =destContentClientService.hasExistAssignDest(foreignParams).getReturnContent();
//				if(result!=0)
//					return true;
			}
		}
		return false;
	}


	/**
	 * 取合同模板
	 * @return
	 */
	public ResultHandleT<String> getContractTemplateHtml(String templateCode){
		ResultHandleT<String> result = null;
		result = MemcachedUtil.getInstance().get(templateCode);
		if(null == result){
			result = orderEcontractGeneratorService.getContractTemplateHtml(templateCode);
			MemcachedUtil.getInstance().set(templateCode, 3600, result);
			LOG.info("组装合同内容，并加入缓存中"+templateCode);
		}else{
			LOG.info("缓存中获取合同模板:"+templateCode);
		}
		if(result.getMsg()!=null)
			LOG.info("getContractTemplateHtml:::"+result.getMsg());
		return result;
	}

	public ResultHandleT<Long> getDepartureNoticeCount(Map<String, Object> params){
		ResultHandleT<Long> resultHandleT = new ResultHandleT<Long>();
		resultHandleT.setReturnContent(iOrdOrderService.getDepartureNoticeCount(params));
		return resultHandleT;
	}

	public ResultHandleT<List<DepartureNoticeVo>> selectDepartureNoticeList(Map<String, Object> params){
		ResultHandleT<List<DepartureNoticeVo>> resultHandleT = new ResultHandleT<List<DepartureNoticeVo>>();
		resultHandleT.setReturnContent(iOrdOrderService.selectDepartureNoticeList(params));
		return resultHandleT;
	}

	@Override
	public ResultHandleT<List<OrderAttachment>> findOrderAttachmentByCondition(
			Map<String, Object> params) {
		ResultHandleT<List<OrderAttachment>> orderAttachmentHandlet = new ResultHandleT<List<OrderAttachment>>();
		List<OrderAttachment>  orderAttachmentList = orderAttachmentService.findOrderAttachmentByCondition(params);
		orderAttachmentHandlet.setReturnContent(orderAttachmentList);
		return orderAttachmentHandlet;
	}

	@Override
	public ResultHandleT<List<ExpressSuppGoodsVO>> findOrderExpressGoods(
			BuyInfo buyInfo) {
		return orderExpressService.findOrderExpressGoods(buyInfo);
	}

	@Override
	public List<OrdPromotion> queryOrdPromotionListByOrdItemList(Map<String, Object> params){
		return orderPromotionService.selectOrdPromotionsByOrderItemId(params);
	}

	@Override
	public boolean checkRecentlyExistNewOrderByProductId(Long productId){
		Map<String, Object> params = new HashMap<String, Object>();
		Date endTime = new Date();
		Calendar beginCalen = Calendar.getInstance();
		beginCalen.setTime(endTime);
		beginCalen.add(Calendar.MINUTE, -5);
		Date beginTime = beginCalen.getTime();
		params.put("beginTime", beginTime);
		params.put("endTime", endTime);
		params.put("productId", productId);

		int count = iOrdOrderItemService.countOrderItemByCreateTimeAndProductId(params);
		if(count>0)
			return true;
		else
			return false;
	}

	@Override
	public ResultHandle saveNoticeRegiment(TravelContractVO travelContractVo, String operatorName) {
		return noticeRegimentService.saveNoticeRegiment(travelContractVo, operatorName);
	}

	@Override
	public ResultHandle updateCertificateStatus(OrdOrder order,
			String assignor, String memo) {
		ResultHandleT<ComAudit> handle = orderStatusManageService.updateCertificateStatus(order, new OrderAttachment(), assignor, memo);
		return handle;
	}

	@Override
	public ComAudit createOrderTask(Long objectId, AUDIT_TYPE type) {
		LOG.info("objectId:"+objectId+"auditType:"+type);
		Assert.notNull(objectId);
		Assert.notNull(type);
		Assert.isTrue(type.equals(OrderEnum.AUDIT_TYPE.CANCEL_AUDIT));
		return saveCreateOrderAudit(objectId,OrderEnum.AUDIT_OBJECT_TYPE.ORDER.name(),type.name());
	}

	/**
	 * 获取门票子订单的二维码信息、取票时间、取票地址和入园方式
	 * @param item
	 */
	private List<TicketOrderItemVo> getTicketOrderItemVoList(OrdOrderItem item){
		List<TicketOrderItemVo> ticketOrderItemVoList = new ArrayList<TicketOrderItemVo>();
		//取到取票地址和入园方式
		SuppGoodsTicketDetailVO sgtdv = suppGoodsClientService.findSuppGoodsTicketDetailById(item.getSuppGoodsId()).getReturnContent();
//		PassProvider provider = supplierOrderOperator.getProductServiceInfo(item.getSuppGoodsId());
		List<OrdPassCode> ordPassCodeList = item.getOrdPassCodeList();
		if(ordPassCodeList != null && !ordPassCodeList.isEmpty()){ // 有二维码
			//查询履行记录
			List<OrdTicketPerform> ordTicketPerformList = this.complexQueryService.selectByOrderItem(item.getOrderItemId());
			for(OrdPassCode ordPassCode : ordPassCodeList){
				TicketOrderItemVo ticketOrderItemVo = new  TicketOrderItemVo();
				if(sgtdv != null && sgtdv.getSuppGoodsDesc() != null){
					ticketOrderItemVo.setEnterStyle((sgtdv.getSuppGoodsDesc().getEnterStyle() != null) ? sgtdv.getSuppGoodsDesc().getEnterStyle() : "");
					ticketOrderItemVo.setChangeAddress((sgtdv.getSuppGoodsDesc().getChangeAddress() != null) ? sgtdv.getSuppGoodsDesc().getChangeAddress() : "");
					ticketOrderItemVo.setChangeTime((sgtdv.getSuppGoodsDesc().getChangeTime() != null) ? sgtdv.getSuppGoodsDesc().getChangeTime() : "");
				}else{
					ticketOrderItemVo.setEnterStyle("");
					ticketOrderItemVo.setChangeAddress("");
					ticketOrderItemVo.setChangeTime("");
				}
				ticketOrderItemVo.setVisitTime(item.getVisitTime());
				ticketOrderItemVo.setServiceId(ordPassCode.getServiceId());
				ticketOrderItemVo.setOrderItemId(item.getOrderItemId());
				ticketOrderItemVo.setOrderId(item.getOrderId());
				ticketOrderItemVo.setProductName(item.getProductName());
                ticketOrderItemVo.setGoodsId(item.getSuppGoodsId());
                //取子订单中的商品名称
                ticketOrderItemVo.setGoodsName(item.getSuppGoodsName());
				//添加是否使用字段
			    if(CollectionUtils.isNotEmpty(ordTicketPerformList)){
			    	OrdTicketPerform ordTicketPerform = ordTicketPerformList.get(0);
			    	Date performTime = ordTicketPerform.getPerformTime();
			    	if(null != performTime){
			    		//"Y"表示已使用
			    		ticketOrderItemVo.setIsUsed("Y");
			    	}else{
			    		//"N"表示未使用
						ticketOrderItemVo.setIsUsed("N");
			    	}
			    }
			    //品类
			    ticketOrderItemVo.setCategoryCode(item.getContentStringByKey("categoryCode"));
			    LOG.info("=============品类=========="+item.getContentStringByKey("categoryCode"));
			    //演出票区域
			    ticketOrderItemVo.setShowTicketRegion(item.getShowTicketRegion());
			    LOG.info("=============区域=========="+item.getShowTicketRegion());
			    if(null == item.getShowTicketEventStartTime() || "".equals(item.getShowTicketEventStartTime())){
			    	ticketOrderItemVo.setShowTicketEvent("通场");
			    }else{
			    	String ShowTicketEvent = item.getShowTicketEventStartTime() + "--" + item.getShowTicketEventEndTime();
			    	//演出票场次
			    	ticketOrderItemVo.setShowTicketEvent(ShowTicketEvent);
			    	LOG.info("=============场次=========="+ShowTicketEvent);
			    }
			    String idFlag = "";
				if(null != item.getSuppGoodsId()){
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("objectId", item.getSuppGoodsId());
					params.put("objectType", ComOrderRequired.OBJECTTYPE.SUPP_GOODS.name());
					List<ComOrderRequired> findComOrderRequiredList = this.comOrderRequiredClientRemote.findComOrderRequiredList(params);
					if(null != findComOrderRequiredList && findComOrderRequiredList.size()>0){
						ComOrderRequired comOrderRequired = findComOrderRequiredList.get(0);
						idFlag = comOrderRequired.getIdFlag();
					}
				}
			    //是否需要身份证
			    ticketOrderItemVo.setIsNeedIdFlag(idFlag);
			    LOG.info("=============身份证=========="+idFlag);

			     //期票信息
		        if(StringUtil.isNotEmptyString(item.getContent())){
		            JSONObject  jsonObject = JSONObject.fromObject(item.getContent());
		            LOG.info("ordOrderClient.getTicketOrderItemVo:"+ticketOrderItemVo.getOrderId()+','+jsonObject.toString());
                    ticketOrderItemVo.setAperiodicFlag(null!=jsonObject.get(OrderEnum.ORDER_TICKET_TYPE.aperiodic_flag.name())?jsonObject.get(OrderEnum.ORDER_TICKET_TYPE.aperiodic_flag.name()).toString():"N");
		            if(null!=jsonObject.get("aperiodic_flag")&&jsonObject.get("aperiodic_flag").equals("Y")){
                        ticketOrderItemVo.setGoodsExpInfo(jsonObject.get(OrderEnum.ORDER_TICKET_TYPE.goodsExpInfo.name())==null?"":jsonObject.get(OrderEnum.ORDER_TICKET_TYPE.goodsExpInfo.name()).toString());
		                ticketOrderItemVo.setStartDate(jsonObject.get("aperiodic_start")==null?"":jsonObject.get("aperiodic_start").toString());
		                ticketOrderItemVo.setEndDate(jsonObject.get("aperiodic_end")==null?"":jsonObject.get("aperiodic_end").toString());
		                ticketOrderItemVo.setUnvalidDesc(jsonObject.get("aperiodic_unvalid_desc")==null?"":(jsonObject.get("aperiodic_unvalid_desc").toString()+"不可使用"));
		            }
			    }

				// 二维码信息
				ticketOrderItemVo.setAddCode(ordPassCode.getAddCode());
				ticketOrderItemVo.setCodeImage(ordPassCode.getCodeImage());
				if(ordPassCode.getCodeImage()!=null&&("".equals(ordPassCode.getPicFilePath())||ordPassCode.getPicFilePath()==null)){
					PassCodeImageVo passCodeImageVo = passCodeService.getPassCodeImageBySerialNo(ordPassCode.getPassSerialno());
					if(passCodeImageVo!=null){
						ordPassCode.setPicFilePath(passCodeImageVo.getPicFilePath());
					}
				}
				ticketOrderItemVo.setUrl(ordPassCode.getUrl());
				ticketOrderItemVo.setCodeImageFlag(ordPassCode.getCodeImageFlag());
				ticketOrderItemVo.setPicFilePath(ordPassCode.getPicFilePath());
				ticketOrderItemVo.setPassCodeId(ordPassCode.getPassCodeId());
				ticketOrderItemVo.setCode(ordPassCode.getCode());
				ticketOrderItemVoList.add(ticketOrderItemVo);

				ticketOrderItemVo.setCodeImagePdfFlag(this.getCodeImagePdfFlag(item.getSuppGoodsId()));
				ticketOrderItemVo.setFileId(ordPassCode.getFileId());

			}
		}
		return ticketOrderItemVoList;
	}



	/**
	 * 获取电子票详情信息
	 * @param item
	 * @param passCodeId
	 * @return
	 */
	private TicketOrderItemVo getTicketOrderItemVo(OrdOrderItem item, Long passCodeId) {
		// 取到取票地址和入园方式
		SuppGoodsTicketDetailVO sgtdv = suppGoodsClientService.findSuppGoodsTicketDetailById(item.getSuppGoodsId()).getReturnContent();
		TicketOrderItemVo ticketOrderItemVo = new TicketOrderItemVo();
		if (sgtdv != null && sgtdv.getSuppGoodsDesc() != null) {
            SuppGoodsDesc desc = sgtdv.getSuppGoodsDesc();
			ticketOrderItemVo.setEnterStyle((desc != null) ? desc.getEnterStyle() : "");
			ticketOrderItemVo.setChangeAddress((desc.getChangeAddress() != null) ? desc.getChangeAddress() : "");
			ticketOrderItemVo.setChangeTime((desc.getChangeTime() != null) ? desc.getChangeTime() : "");
			ticketOrderItemVo.setMapImgUrl(desc.getMapImgUrl());
            if (StringUtils.isNotEmpty(desc.getVisitAddress())) {
                ticketOrderItemVo.setVisitAddress(desc.getVisitAddress());
                ticketOrderItemVo.setVisitLimit(desc.getLimitTime());
                ticketOrderItemVo.setPriceIncludes(desc.getPriceIncludes());
            }
		} else {
			ticketOrderItemVo.setEnterStyle("");
			ticketOrderItemVo.setChangeAddress("");
			ticketOrderItemVo.setChangeTime("");
		}
		//取票地点取【产品基本维护信息】-【入场须知】-【取票地点】后台数据 
		//取票时间取【产品基本维护信息】-【入场须知】-【取票时间】后台数据
		if(item.getCategoryId() == 31L){
			try {
				ticketOrderItemVo.setChangeAddress("");
				ticketOrderItemVo.setChangeTime("");
				Map<String, Object> params = new HashMap<>();
				Map<String, Object> bizParams = new HashMap<>();
				bizParams.put("categoryId", 31);
				bizParams.put("PROP_NAME", "取票地点");
				com.lvmama.vst.comm.vo.ResultHandleT<List<BizCategoryProp>> bizCategoryProps = categoryPropClientRemote.findCategoryPropList(bizParams);
				if(null != bizCategoryProps && null != bizCategoryProps.getReturnContent() && bizCategoryProps.getReturnContent().size()>0) {
					params.put("productId", item.getProductId());
					params.put("propId", bizCategoryProps.getReturnContent().get(0).getPropId()); 
					com.lvmama.vst.comm.vo.ResultHandleT<List<ProdProductProp>> resultHandleT = prodProductPropClientRemote.findProdProductPropList(params);
					if(null != resultHandleT && null != resultHandleT.getReturnContent() && resultHandleT.getReturnContent().size() > 0){
						ticketOrderItemVo.setChangeAddress((resultHandleT.getReturnContent().get(0).getPropValue() != null) ? resultHandleT.getReturnContent().get(0).getPropValue() : "");
					}
				}
				bizParams.put("PROP_NAME", "取票时间");
				bizCategoryProps = categoryPropClientRemote.findCategoryPropList(bizParams);
				if(null != bizCategoryProps && null != bizCategoryProps.getReturnContent() && bizCategoryProps.getReturnContent().size()>0) {
					params.put("propId", "");
					params.put("productId", item.getProductId());
					params.put("propId", bizCategoryProps.getReturnContent().get(0).getPropId()); 
					com.lvmama.vst.comm.vo.ResultHandleT<List<ProdProductProp>> resultHandleT = prodProductPropClientRemote.findProdProductPropList(params);
					if(null != resultHandleT && null != resultHandleT.getReturnContent() && resultHandleT.getReturnContent().size() > 0){
						ticketOrderItemVo.setChangeTime((resultHandleT.getReturnContent().get(0).getPropValue() != null) ? resultHandleT.getReturnContent().get(0).getPropValue() : "");
					}
				}
			} catch (Exception e) {
				LOG.error("###取票地点取【产品基本维护信息】-【入场须知】-【取票地点】后台数据 失败"+e.getMessage());
			}
			
		  }
	      Integer quantity = Integer.valueOf(item.getQuantity().toString());
	        String smsContent = "";
	        //电子期票取得开始时间及结束时间
	        if(StringUtil.isNotEmptyString(item.getContent())){
	            JSONObject  jsonObject = JSONObject.fromObject(item.getContent());
	            LOG.info("ordOrderClient.getTicketOrderItemVo:"+ticketOrderItemVo.getOrderId()+','+jsonObject.toString());
                ticketOrderItemVo.setAperiodicFlag(jsonObject.get("aperiodic_flag")==null?"N":jsonObject.get("aperiodic_flag").toString());
                if(null!=jsonObject.get("aperiodic_flag")&&jsonObject.get("aperiodic_flag").equals("Y")){
	                ticketOrderItemVo.setStartDate(jsonObject.get("aperiodic_start")==null?"":jsonObject.get("aperiodic_start").toString());
	                ticketOrderItemVo.setEndDate(jsonObject.get("aperiodic_end")==null?"":jsonObject.get("aperiodic_end").toString());
	                ticketOrderItemVo.setUnvalidDesc(jsonObject.get("aperiodic_unvalid_desc")==null?"":(jsonObject.get("aperiodic_unvalid_desc").toString()+"不可使用"));
	            }
	            ticketOrderItemVo.setAdultQuantity(jsonObject.get("adult_quantity")==null?0:Integer.valueOf(jsonObject.get("adult_quantity").toString())*quantity);
	            ticketOrderItemVo.setChildQuantity((jsonObject.get("child_quantity")==null?0:Integer.valueOf(jsonObject.get("child_quantity").toString())*quantity));
//	            Map<String,Object> params = new HashMap<String, Object>();
//	            List<String> smsContentList = new ArrayList<String>();
//	            params.put("orderId", item.getOrderId());
//	            List<OrdSmsSend> smsList = new ArrayList<OrdSmsSend>();
//	            smsList = orderSendSmsService.findOrdSmsSendList(params);
//	            if(CollectionUtils.isNotEmpty(smsList)){
//	                for (OrdSmsSend ordSmsSend : smsList) {
//	                    smsContentList.add(ordSmsSend.getContent());
//	                }
//	            }
                Object goodsExp = item.getContentValueByKey(OrderEnum.ORDER_TICKET_TYPE.goodsExpInfo.name());
                if(goodsExp != null){
                    ticketOrderItemVo.setGoodsExpInfo(goodsExp.toString());
                }

	            smsContent = orderSendSmsService.getContent(item.getOrderId(),SEND_NODE.PAY_ROUTE_AERO_HOTEL);
	            smsContent = (StringUtil.isEmptyString(smsContent)==true)?"":smsContent.substring(smsContent.indexOf("您的订单号"), smsContent.length());
	            ticketOrderItemVo.setSmsContent(smsContent);
		}
		ticketOrderItemVo.setVisitTime(item.getVisitTime());
		ticketOrderItemVo.setOrderItemId(item.getOrderItemId());
		ticketOrderItemVo.setOrderId(item.getOrderId());
		ticketOrderItemVo.setProductName(item.getProductName());
		//取子订单里的商品名称
		ticketOrderItemVo.setGoodsName(item.getSuppGoodsName());
//		if (item.getSuppGoods() != null) {
//			ticketOrderItemVo.setGoodsName(item.getSuppGoods().getGoodsName());
//		}
		String idFlag = "";
		if(null != item.getSuppGoodsId()){
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("objectId", item.getSuppGoodsId());
				params.put("objectType", ComOrderRequired.OBJECTTYPE.SUPP_GOODS.name());
				List<ComOrderRequired> findComOrderRequiredList = this.comOrderRequiredClientRemote.findComOrderRequiredList(params);
				if(null != findComOrderRequiredList && findComOrderRequiredList.size()>0){
					ComOrderRequired comOrderRequired = findComOrderRequiredList.get(0);
					idFlag = comOrderRequired.getIdFlag();
				}
		}
		//是否需要身份证
		ticketOrderItemVo.setIsNeedIdFlag(idFlag);
		Log.info("==============身份证==========="+ idFlag);
		//品类
		ticketOrderItemVo.setCategoryCode(item.getContentStringByKey("categoryCode"));
		Log.info("==============品类==========="+ item.getContentStringByKey("categoryCode"));
		//演出票区域
		ticketOrderItemVo.setShowTicketRegion(item.getShowTicketRegion());
		Log.info("==============区域==========="+ item.getShowTicketRegion());
		if(null == item.getShowTicketEventStartTime() || "".equals(item.getShowTicketEventStartTime())){
		    	ticketOrderItemVo.setShowTicketEvent("通场");
		}else{
		        String ShowTicketEvent = item.getShowTicketEventStartTime() + "--" + item.getShowTicketEventEndTime();
		    	//演出票场次
		    	ticketOrderItemVo.setShowTicketEvent(ShowTicketEvent);
		    	LOG.info("=============场次=========="+ShowTicketEvent);
		}
		// 二维码信息
		List<OrdPassCode> ordPassCodeList = item.getOrdPassCodeList();
		if (ordPassCodeList != null && !ordPassCodeList.isEmpty() && passCodeId != null) { // 有二维码
			for (OrdPassCode ordPassCode : ordPassCodeList) {
				if(ordPassCode.getPassCodeId().longValue() == passCodeId.longValue()){
					ticketOrderItemVo.setAddCode(ordPassCode.getAddCode());
					ticketOrderItemVo.setCodeImage(ordPassCode.getCodeImage());
					if(ordPassCode.getCodeImage()!=null&&("".equals(ordPassCode.getPicFilePath())||ordPassCode.getPicFilePath()==null)){
						PassCodeImageVo passCodeImageVo = passCodeService.getPassCodeImageBySerialNo(ordPassCode.getPassSerialno());
						if(passCodeImageVo!=null){
							ordPassCode.setPicFilePath(passCodeImageVo.getPicFilePath());
						}
					}
					ticketOrderItemVo.setPicFilePath(ordPassCode.getPicFilePath());
					ticketOrderItemVo.setPassCodeId(ordPassCode.getPassCodeId());
					ticketOrderItemVo.setCode(ordPassCode.getCode());

					ticketOrderItemVo.setServiceId(ordPassCode.getServiceId());
					ticketOrderItemVo.setCodeImagePdfFlag(this.getCodeImagePdfFlag(item.getSuppGoodsId()));
					ticketOrderItemVo.setFileId(ordPassCode.getFileId());
					break;
				}
			}
		}
		return ticketOrderItemVo;
	}

	/**
	 * 设置订单项其他详情信息
	 * @param item
	 */
	private void setOrderItemOtherDetails(OrdOrderItem item){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", item.getOrderItemId());
		List<OrdPassCode> ordPassCodeList = supplierOrderHandleService.selectOrdPassCodeByParams(params);
		item.setOrdPassCodeList(ordPassCodeList);
		// if(StringUtils.isEmpty(item.getProductName())){// 产品名没查商品
			SuppGoodsParam param = new SuppGoodsParam();
			param.setProduct(false);
			ProdProductParam ppp = new ProdProductParam();
			ppp.setBizCategory(false);
			param.setProductBranch(false);
			param.setSupplier(false);
			param.setProductParam(ppp);
			SuppGoods suppGoods = suppGoodsClientService.findSuppGoodsById(item.getSuppGoodsId(), param).getReturnContent();
			item.setSuppGoods(suppGoods);
		// }
	}

	@Override
	public ResultHandleT<Long> getTicketItemCountByUserIdAndCreateTime(String userId, Date createTime){
		ResultHandleT<Long> resultHandleT = new ResultHandleT<Long>();
		Long count = 0L;
		if(StringUtils.isNotEmpty(userId) && createTime != null){
			count = iOrdOrderItemService.getTicketItemCountByUserIdAndCreateTime(userId, createTime);
		}
		resultHandleT.setReturnContent(count);
		return resultHandleT;
	}

	@Override
	public ResultHandleT<List<TicketOrderItemVo>> getTicketItemListByUserId(String userId){
		ResultHandleT<List<TicketOrderItemVo>> resultHandleT = new ResultHandleT<List<TicketOrderItemVo>>();
		if(StringUtils.isNotEmpty(userId)){
			List<OrdOrderItem> itemList = iOrdOrderItemService.getTicketItemListByUserId(userId);
			List<TicketOrderItemVo> ticketOrderItemVoList = new ArrayList<TicketOrderItemVo>();
			if(itemList != null && itemList.size() > 0){
				for(OrdOrderItem item : itemList){
					if(item != null && (item.getCategoryId() == 11L || item.getCategoryId() == 12L
							|| item.getCategoryId() == 13L)){
						setOrderItemOtherDetails(item);
						List<TicketOrderItemVo> ticketOrderItemVoTempList = this.getTicketOrderItemVoList(item);
						if(!ticketOrderItemVoTempList.isEmpty())
							ticketOrderItemVoList.addAll(ticketOrderItemVoTempList);
					}
				}
			}
			if(!ticketOrderItemVoList.isEmpty()){
				resultHandleT.setReturnContent(ticketOrderItemVoList);
			}
			else{
				resultHandleT.setMsg("ticketOrderItemVoList is empty. ");
			}
		}else{
			resultHandleT.setMsg("User id can not be null. ");
		}

		return resultHandleT;
	}

	@Override
	public ResultHandleT<List<TicketOrderItemVo>> getTicketItemListByOrderId(Long orderId){
		ResultHandleT<List<TicketOrderItemVo>> resultHandleT = new ResultHandleT<List<TicketOrderItemVo>>();
		if(orderId!=null){
			List<OrdOrderItem> itemList = iOrdOrderItemService.selectByOrderId(orderId);
			List<TicketOrderItemVo> ticketOrderItemVoList = new ArrayList<TicketOrderItemVo>();
			if(itemList != null && itemList.size() > 0){
				for(OrdOrderItem item : itemList){
					if(item != null){
						setOrderItemOtherDetails(item);
						List<TicketOrderItemVo> ticketOrderItemVoTempList = getTicketOrderItemVoList(item);
						if(!ticketOrderItemVoTempList.isEmpty())
							ticketOrderItemVoList.addAll(ticketOrderItemVoTempList);
					}
				}
			}
			if(!ticketOrderItemVoList.isEmpty()){
				resultHandleT.setReturnContent(ticketOrderItemVoList);
			}
			else{
				resultHandleT.setMsg("ticketOrderItemVoList is empty. ");
			}
		}else{
			resultHandleT.setMsg("orderId can not be null. ");
		}

		return resultHandleT;
	}

	@Override
	public ResultHandleT<TicketOrderItemVo> getTicketItemByItemId(Long ordOrderItemId, Long passCodeId) {
		ResultHandleT<TicketOrderItemVo> resultHandleT = new ResultHandleT<TicketOrderItemVo>();
		OrdOrderItem item = iOrdOrderItemService.selectOrderItemByOrderItemId(ordOrderItemId);
		if(item != null && (item.getCategoryId() == 11L || item.getCategoryId() == 12L
				|| item.getCategoryId() == 13L || item.getCategoryId() == 31L)){
			setOrderItemOtherDetails(item);
			TicketOrderItemVo ticketOrderItemVo = getTicketOrderItemVo(item, passCodeId);
			ticketOrderItemVo.setMainProductId(prodProductClientService.getMainProductId(item.getProductId()));
			resultHandleT.setReturnContent(ticketOrderItemVo);
		}else{
			resultHandleT.setMsg("This item is not a ticket item. ");
		}
		return resultHandleT;
	}

	@Override
	public String calPerformStatus(List<OrdTicketPerform> resultList,
			OrdOrder order, OrdOrderItem ordOrderItem) {
		return OrderUtils.calPerformStatus(resultList, order, ordOrderItem);
	}

	@Override
	public String calculatePerformStatus(List<OrdTicketPerform> resultList,
			OrdOrderItem orderItem) {
		return OrderUtils.calulatePerformStatus(resultList, orderItem);
	}


	@Override
	public ResultHandleT<List<OrdTicketPerform>> selectByOrderItem(Long orderItemId) {
		ResultHandleT<List<OrdTicketPerform>> handle = new ResultHandleT<List<OrdTicketPerform>>();
		try{
			handle.setReturnContent(this.complexQueryService.selectByOrderItem(orderItemId));
		}catch(Exception e){
			LOG.error("{}", e);
		}
		return handle;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int updateOrdOrder(OrdOrder ordOrder) {
		return orderUpdateService.updateOrdOrder(ordOrder);
	}

	@Override
	public int updateIsTestOrder(Map<String, Object> map) {
		return this.orderUpdateService.updateIsTestOrder(map);
	}

	@Override
	public OrdOrder querySimpleOrder(Long orderId) {
		return orderUpdateService.queryOrdOrderByOrderId(orderId);
	}

	@Override
	public ResultHandleT<List<OrdTicketPerform>> selectByOrderItems(
			List<Long> orderItemIds) {
		ResultHandleT<List<OrdTicketPerform>> handle = new ResultHandleT<List<OrdTicketPerform>>();
		try{
			handle.setReturnContent(this.complexQueryService.selectByOrderItems(orderItemIds));
		}catch(Exception e){
			LOG.error("{}", e);
		}
		return handle;
	}

	@Override
	public ResultHandleT<List<OrdOrderItem>> selectOrderItemsByIds(
			List<Long> ids) {
		ResultHandleT<List<OrdOrderItem>> handle = new ResultHandleT<List<OrdOrderItem>>();
		try{
			handle.setReturnContent(this.iOrdOrderItemService.selectOrderItemsByIds(ids));
		}catch(Exception e){
			LOG.error("{}", e);
		}
		return handle;
	}

	@Override
	public ResultHandleT<List<OrdOrderItem>> selectSubOrderItemsByIds(
			List<Long> ids) {
		ResultHandleT<List<OrdOrderItem>> handle = new ResultHandleT<List<OrdOrderItem>>();
		try{
			handle.setReturnContent(this.iOrdOrderItemService.selectSubOrderItemsByIds(ids));
		}catch(Exception e){
			LOG.error("{}",e);
		}
		return handle;
	}

	@Override
	public ResultHandleT<PermUser> queryOrderPrincipal(String objectType,
			Long objectId) {
		ResultHandleT<PermUser> handle = new ResultHandleT<PermUser>();
		try{
			handle.setReturnContent(orderResponsibleService.getOrderPrincipal(objectType, objectId));
		}catch(Exception e){
			LOG.error(e.getMessage());
			handle.setMsg(e.getMessage());
		}

		return handle;
	}

	@Override
	public Page<OrdOrderItem> listOrderItemByConditions(Page<OrdOrderItem> page, Map<String, Object> paramMap) {
		Assert.notNull(page) ;
		long totalCount = iOrdOrderService.listOrderItemByConditionsCount(paramMap);
		if (totalCount > 0L) {
			page = Page.page(totalCount, page.getPageSize(), page.getCurrentPage());
			paramMap.put("_start", page.getStartRows());
			paramMap.put("_end", page.getEndRows());
			//如果_orderby没有值，默认订单创建时间降序
			Object orderby= paramMap.get("_orderby");
			if (orderby == null || "".equals(orderby)) {
				paramMap.put("_orderby", "create_Time");
				paramMap.put("_order", "desc");
			}
			page.setTotalResultSize(totalCount);
			page.setItems(iOrdOrderService.listOrderItemByConditions(page, paramMap));
		}
		return page;
	}

	@Override
	public ResultHandleT<Long> listOrderItemByConditionsCount(Map<String, Object> paramMap) {
		ResultHandleT<Long> handle = new ResultHandleT<Long>();

		try {
			long totalCount = iOrdOrderService.listOrderItemByConditionsCount(paramMap);
			handle.setReturnContent(totalCount);
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		return handle;
	}

    @Override
    public List<ResPrecontrolOrderVo> findPercontrolGoodsOrderList(Page<ResPrecontrolOrderVo> page) {

        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("_start",page.getStartRows());
        paramsMap.put("_end",page.getEndRows());
        paramsMap.put("suppGoodsId",page.getParam().getSuppGoodsId());
        paramsMap.put("tradeEffectDate",page.getParam().getTradeEffectDate());
        paramsMap.put("tradeExpiryDate",page.getParam().getTradeExpiryDate());
        paramsMap.put("qrySource", page.getParam().getQrySource());
        String goodsName = null;
        Long category = null;
		try {
			ResultHandleT<SuppGoods> goodsT = suppGoodsHotelAdapterClientService.findSuppGoodsById(page.getParam().getSuppGoodsId(), new SuppGoodsParam());
			goodsName = goodsT.getReturnContent().getGoodsName();
			Log.info("findPercontrolGoodsOrderList return goodsName: " + goodsName); 
			SimpleDateFormat sdf =new SimpleDateFormat(DateUtil.PATTERN_yyyy_MM_dd_HH_mm_ss);
			Log.info("===========findPercontrolGoodsOrderList1===category== "+page.getParam().getSuppGoodsId()+"==="+sdf.format(page.getParam().getTradeEffectDate())+"==="+sdf.format(page.getParam().getTradeExpiryDate())+"==="+ goodsT.isSuccess()+"goodsName is ="+goodsName);
			if (goodsT.isSuccess()) {
				ResultHandleT<ProdProduct> product = productHotelAdapterClientService.findProductById(goodsT.getReturnContent().getProductId());
				Log.info("===========findPercontrolGoodsOrderList2===category==========="+ product.isSuccess());				
				if (product.isSuccess()) {
					category = product.getReturnContent().getBizCategoryId();					
					Log.info("===========countPercontrolGoodsOrderList===category==========="+ category);
				}
			}
		} catch (Exception e) {
			LOG.warn(ExceptionFormatUtil.getTrace(e));
			List<ResPrecontrolOrderVo> resPreOrderList = iOrdOrderService.findPercontrolGoodsOrderList(paramsMap);
			for (ResPrecontrolOrderVo resPrecontrolOrderVo : resPreOrderList) {
				resPrecontrolOrderVo.setSuppGoodsName(goodsName);
			}
			Log.warn("===========findPercontrolGoodsOrderList===roor===suppgoodsId==========="+ page.getParam().getSuppGoodsId()+"===message==="+e.getMessage());
			return resPreOrderList;
		}
		
		if (category != null && category == 1L) {
			List<ResPrecontrolOrderVo> result = iOrdOrderService.findPercontrolHotelGoodsOrderList(paramsMap);
			Log.info("===========findPercontrolGoodsOrderList===result==========="+ result.size());
			for (ResPrecontrolOrderVo resPrecontrolOrderVo : result) {
				resPrecontrolOrderVo.setSuppGoodsName(goodsName);
			}
			return result;
		}else {
			Log.info("===========findPercontrolGoodsOrderList===category==========="+ category);
			List<ResPrecontrolOrderVo> result = iOrdOrderService.findPercontrolGoodsOrderList(paramsMap);
			for (ResPrecontrolOrderVo resPrecontrolOrderVo : result) {
				resPrecontrolOrderVo.setSuppGoodsName(goodsName);
			}
			return result;
		}

    }

    @Override
	public Long countPercontrolGoodsOrderList(Long suppGoodsId,
			Date tradeEffectDate, Date tradeExpiryDate, Integer qrySource) {

		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("suppGoodsId", suppGoodsId);
		paramsMap.put("tradeEffectDate", tradeEffectDate);
		paramsMap.put("tradeExpiryDate", tradeExpiryDate);
		paramsMap.put("qrySource", qrySource);
		Long category = null;
		try {
			SuppGoodsParam params = new SuppGoodsParam();
			ResultHandleT<SuppGoods> goodsT = suppGoodsHotelAdapterClientService
					.findSuppGoodsById(suppGoodsId ,params);
			SimpleDateFormat sdf =new SimpleDateFormat(DateUtil.PATTERN_yyyy_MM_dd_HH_mm_ss);
			Log.info("===========countPercontrolGoodsOrderList===category== "+suppGoodsId+"==="+sdf.format(tradeEffectDate)+"==="+sdf.format(tradeExpiryDate)+"==="+ goodsT.isSuccess());
			Log.info("===========countPercontrolGoodsOrderList1===category==========="+ goodsT.isSuccess());
			if (goodsT.isSuccess()) {
				ResultHandleT<ProdProduct> product = productHotelAdapterClientService.findProductById(goodsT.getReturnContent()
								.getProductId());
				Log.info("===========countPercontrolGoodsOrderList2===category==========="+ product.isSuccess());
				if (product.isSuccess()) {
					category = product.getReturnContent().getBizCategoryId();
					
					Log.info("===========countPercontrolGoodsOrderList===category==========="+ category);
				}
			}
		} catch (Exception e) {
			LOG.info(ExceptionFormatUtil.getTrace(e));
			return iOrdOrderService.countPercontrolGoodsOrderList(paramsMap);
		}
		if (category != null && category == 1L) {		
			long result =iOrdOrderService.countPercontrolHotelGoodsOrderList(paramsMap);
			Log.info("===========countPercontrolGoodsOrderList===result==========="+ result);
			return result;
		} else {
			long result = iOrdOrderService.countPercontrolGoodsOrderList(paramsMap);
			Log.info("===========countPercontrolGoodsOrderList===is not hotel==========="+result);
			return result;
		}
	}


    @Override
	public List<OrdInvoice> getOrdInvoiceListByOrderIdList(List<Long> orderIds) {
		return ordInvoiceService.getOrdInvoiceListByOrderIdList(orderIds);
	}
    
	@Override
	public OrdInvoice makeInvoiceOrdPerson(OrdInvoice ordInvoice) {
		if (!ordInvoice.getDeliveryType().equals("SELF")) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("objectId", ordInvoice.getOrdInvoiceId());
			params.put("objectType", "ORD_INVOICE");
			List<OrdPerson> ordPersonList = iOrdPersonService.findOrdPersonList(params);
			if (ordPersonList != null && ordPersonList.size() > 0) {
				ordInvoice.setDeliveryAddress(ordPersonList.get(0));
				params.put("ordPersonId", ordPersonList.get(0).getOrdPersonId());
				List<OrdAddress> list = ordAdressService.findOrdAddressList(params);
				if (list != null && list.size() > 0) {
					ordInvoice.getDeliveryAddress().setAddressList(list);
				}
			}
		}
		return ordInvoice;
	}

	@Override
	public ResultHandleT<String> getContractTemplateHtml(String templateCode,
			String productId) {
		ResultHandleT<String> result = null;
		String key = null;
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("objectId", productId);
		param.put("parentType", "PROD_PRODUCT");
		param.put("_orderby", "CREATE_TIME");
		param.put("_order", "DESC");
		key = productId + "_CONTRACT_TEMPLATE_HTML";
//		if(CollectionUtils.isNotEmpty(logList)){
//			 key = productId+"_"+DateUtil.formatDate(logList.get(0).getCreateTime(), DateUtil.HHMMSS_DATE_FORMAT);
//		}
		LOG.info("合同组装缓存key is "+key);
		result = MemcachedUtil.getInstance().get(key);
		if(null == result){
			 result = orderEcontractGeneratorService.getContractTemplateHtml(templateCode, Long.valueOf(productId));
			 MemcachedUtil.getInstance().set(key, 300, result);
			 LOG.info("组装合同模板，产品ID："+productId);
		}else{
			 LOG.info("缓存中获取合同模板，产品ID："+productId);
		}

		if(result.getMsg()!=null)
			LOG.info("getContractTemplateHtml:::"+result.getMsg());
		return result;
	}


	@Override
	public ResultHandle saveOrdPremUserRel(OrdPremUserRel ordPremUserRel) {
		return iOrdPremUserRelService.saveOrdPremUserRel(ordPremUserRel);
	}

	public String saveCompositeInvoice(String orderIds, Map<String, String> map) {

		// JSONResult result = new JSONResult();
		String errMsg = "";
		String title = map.get("title");
		String content = map.get("content");
		Long amount = PriceUtil.moneyConvertLongPrice(map.get("amount"));
		String receiverName = map.get("receiverName");
		String mobileNumber = map.get("mobileNumber");
		String deliveryType = map.get("deliveryType");

		// 新增购买方地址、税号、购买方电话、开户银行、开户银行账号  added by zhaofei
		// 购买方地址
		String purchaseWay = map.get("purchaseWay");
		// 纳税人识别号
		String taxNumber = map.get("taxNumber");
		// 购买方地址
		String buyerAddress = map.get("buyerAddress");
		// 购买方电话
		String buyerTelephone = map.get("buyerTelephone");
		// 开户银行
		String bankAccount = map.get("bankAccount");
		// 开户银行账号
		String accountBankAccount = map.get("accountBankAccount");
		

		List<Pair<OrdInvoice, OrdPerson>> invoices = new ArrayList<Pair<OrdInvoice, OrdPerson>>();
		try {
			List<Long> orderIdList = new ArrayList<Long>();
			String[] orders = orderIds.split(",");
			for (String orderId : orders) {
				orderIdList.add(Long.parseLong(orderId));
			}
			List<OrdOrder> orderList = queryOrdorderByOrderIdList(orderIdList);

			// 发票信息
			OrdInvoice or = new OrdInvoice();
			or.setTitle(title);
			or.setContent(content);
			or.setDeliveryType(deliveryType);
			or.setAmount(amount);
			or.setCompany(Constant.INVOICE_COMPANY.COMPANY_3.name());// 公司

			// 新增购买方地址、税号、购买方电话、开户银行、开户银行账号  added by zhaofei
			or.setPurchaseWay(purchaseWay);					// 购买方式（公司、个人）
			or.setTaxNumber(taxNumber);						//纳税人识别号
			or.setBuyerAddress(buyerAddress);				//购买方地址
			or.setBuyerTelephone(buyerTelephone);			//购买方电话
			or.setBankAccount(bankAccount);					//开户银行
			or.setAccountBankAccount(accountBankAccount);	//开户银行账号

			OrdPerson ordPerson = null;

			if (!or.getDeliveryType().equals("SELF") && deliveryType != null) {
				String provinceName = map.get("provinceName");
				String cityName = map.get("cityName");
				String address = map.get("address");

				// 地址
				List<OrdAddress> addressList = new ArrayList<OrdAddress>();
				OrdAddress ordAddress = new OrdAddress();
				ordAddress.setProvince(provinceName);
				ordAddress.setCity(cityName);
				ordAddress.setStreet(address);
				addressList.add(ordAddress);

				// 游玩人
				ordPerson = new OrdPerson();
				ordPerson.setFullName(receiverName);
				ordPerson.setMobile(mobileNumber);
				ordPerson.setObjectType("ORD_INVOICE");
				ordPerson
						.setPersonType(IReceiverUserServiceAdapter.RECEIVERS_TYPE.ADDRESS
								.name());
				ordPerson.setAddressList(addressList);
			}

			Pair<OrdInvoice, OrdPerson> kv = Pair.make_pair(or, ordPerson);
			invoices.add(kv);

			ResultHandle handle = insertInvoiceByOrders(invoices, orderList,"登陆人");

			if (handle.isFail()) {
				errMsg = handle.getMsg();
			}
		} catch (Exception e) {
			// result.raise(e);
			errMsg = e.getMessage();
		}
		// HttpServletResponse response = null;
		// result.output(response );
		return errMsg;
	}

	private ResultHandle insertInvoiceByOrders(
			final List<Pair<OrdInvoice, OrdPerson>> invoices,
			final List<OrdOrder> orderIds, final String operatorId) {
		ResultHandle handle = new ResultHandle();
		if (CollectionUtils.isEmpty(orderIds)) {
			handle.setMsg("订单号为空");
		} else {
			try {
				if (orderIds.size() == 1) {
					ordInvoiceService.insert(invoices, orderIds.get(0)
							.getOrderId(), operatorId);
				} else if (invoices.size() > 1) {
					throw new Exception("多个订单号不可开多张发票");
				} else {
					ordInvoiceService.insert(invoices.get(0), orderIds,
							operatorId);
				}
			} catch (Exception ex) {
				handle.setMsg(ex.getMessage());
			}
		}
		return handle;
	}

	public List<OrdInvoice> insertInvoiceOrders(final List<Pair<OrdInvoice, OrdPerson>> invoices,final List<OrdOrder> orderList, final String operatorId) {
		
			ResultHandle handle = new ResultHandle();
			List<OrdInvoice> ordInvoiceList = new ArrayList<OrdInvoice>();
			if (CollectionUtils.isEmpty(orderList)) {
				handle.setMsg("订单为空");
			} else {
				try {
					for(Pair<OrdInvoice, OrdPerson> invoice : invoices){
						OrdInvoice ordInvoice = ordInvoiceService.insert(invoice, orderList,operatorId);
						ordInvoiceList.add(ordInvoice);
					}
					if(CollectionUtils.isEmpty(ordInvoiceList)){
						throw new Exception("发票添加失败");
					}
				} catch (Exception ex) {
					handle.setMsg(ex.getMessage());
				}
			}
		
		return ordInvoiceList;
	}
	public boolean supportDestroy(OrdOrderItem ordOrderItem){
		Long goodsId = ordOrderItem.getSuppGoodsId();
		Long isSupportDestroyCode = 0L;// 默认不支持,1:支持;0:不支持
		PassProvider provider = null;
		Map<String,Object> contentMap = ordOrderItem.getContentMap();
		String categoryType =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
		if (ProductCategoryUtil.isTicket(categoryType)) {
            provider=supplierOrderOperator.getProductServiceInfo(goodsId);
		}
		if (provider != null) {
			isSupportDestroyCode = provider.getIsSupportDestroyCode();
		}
		return isSupportDestroyCode.equals(1L);
	}

	public Boolean isDistribution(OrdOrder order) {
		boolean isDistribution = false;
		try{
			Map<String, Long> map = new HashMap<String, Long>(){{
				put("DISTRIBUTOR_LVTU", 10000L);
				put("DISTRIBUTOR_LVTUTG", 10001L);
				put("DISTRIBUTOR_LVTUMS", 10002L);
				put("DISTRIBUTOR_TEMAI", 107L);
				put("DISTRIBUTOR_TUANGOU", 108L);
				put("DISTRIBUTOR_MIAOSHA", 110L);

			}};
			if(order.getDistributorId() == 4L){
				isDistribution = true;
				Iterator<Long> i = map.values().iterator();
				if(order.getDistributionChannel()!=null){
					Long channel = order.getDistributionChannel();
					while(i.hasNext() && isDistribution){
						Long next = i.next();
						isDistribution = ( next.longValue() == channel.longValue() ) ? false : true;
					}
				}
			}
		}catch(Exception e){
			LOG.error("isDistribution orderId="+order.getOrderId()+",error", e);
		}
		return Boolean.valueOf(isDistribution);
	}
	@Override
	public Map<String, Boolean> querySupplierNoticeStatus(Long orderId){
		LOG.info("============querySupplierNoticeStatus orderId={} start",orderId);
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		Boolean IS_EBK_NOTICE = Boolean.FALSE;       //供应商通知方式是否为EBK
		Boolean IS_FAX_NOTICE = Boolean.FALSE;       //供应商通知方式是否为传真
		Boolean IS_SUPPLIER_NOTICE = Boolean.FALSE;  //通知方式是否为对接
		Boolean IS_SUPPORT_DESTROY_CODE = Boolean.TRUE;  //是否支持废码
		Boolean IS_ENTER_NOT_IN_TIME = Boolean.FALSE;    //不可及时通关
		Boolean IS_ERROR = Boolean.FALSE;
		try{
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			List<OrdOrderItem> ordItemsList =order.getOrderItemList();

			String fax = OrderEnum.ORDER_COMMON_TYPE.fax_flag.name();
			String ebk = OrderEnum.ORDER_COMMON_TYPE.ebk_flag.name();
			String supplier = OrderEnum.ORDER_COMMON_TYPE.supplierApiFlag.name();
			for(OrdOrderItem ordOrderItem : ordItemsList){
				//门票业务类订单使用状态
				Map<String,Object> performMap = ordOrderItem.getContentMap();
				String categoryCode =  (String) performMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
				if (ProductCategoryUtil.isTicket(categoryCode)) {
					if(!IS_FAX_NOTICE){
						IS_FAX_NOTICE = ordOrderItem.hasContentValue(fax, "Y");
					}
//						if(!IS_EBK_NOTICE){
//							IS_EBK_NOTICE = ordOrderItem.hasContentValue(ebk, "Y");
//						}
					if(!IS_EBK_NOTICE){
//							Map<String,Object> paramsMap=new HashMap<String,Object>();
//							paramsMap.put("objectId", ordOrderItem.getSuppGoodsId());
//							paramsMap.put("objectType", EbkUserPrdGoodsRe.OBJECT_TYPE.SUPP_GOODS);
//							ResultHandleT<List<Long>> result = ebkUserClientService.getEbkUserPrdGoodsReIds(paramsMap);
//							List<Long> contentList = result.getReturnContent();
//							if(contentList!=null&&contentList.size()>0){
//								IS_EBK_NOTICE= true;
//							}
						Map<String,Object> paramUser=new HashMap<String,Object>();
						paramUser.put("cancelFlag", "Y");
						paramUser.put("supplierId", ordOrderItem.getSupplierId());
						List<EbkUser> ebkUserList = ebkUserClientService.getEbkUserList(paramUser).getReturnContent();
						if(ebkUserList!=null&& !ebkUserList.isEmpty()){
							IS_EBK_NOTICE= true;
						}
					}
					if(IS_EBK_NOTICE && !IS_ENTER_NOT_IN_TIME){
						String notInTimeFlag = ordOrderItem
								.getNotInTimeFlag();
						if (notInTimeFlag == null
								|| "".equals(notInTimeFlag)) {
							Long supplierId = ordOrderItem.getSupplierId();
							SuppSupplier suppSupplier = suppSupplierClientService.findSuppSupplierById(supplierId).getReturnContent();
							String notInTimeFlag_supplier = suppSupplier.getNotInTimeFlag();
							if("Y".equals(notInTimeFlag_supplier)){
								IS_ENTER_NOT_IN_TIME = Boolean.TRUE;
							}else{
								SuppGoods suppgoods = suppGoodsHotelAdapterClientService.findSuppGoodsById(ordOrderItem.getSuppGoodsId(), new SuppGoodsParam()).getReturnContent();
								String notInTimeFlag_suppgoods =  suppgoods.getNotInTimeFlag();
								if("Y".equals(notInTimeFlag_suppgoods)){
									IS_ENTER_NOT_IN_TIME = Boolean.TRUE;
								}
							}
						}else{
							if("Y".equals(notInTimeFlag)){
								IS_ENTER_NOT_IN_TIME = Boolean.TRUE;
							}
						}

					}
					if(!IS_SUPPLIER_NOTICE){
						IS_SUPPLIER_NOTICE = ordOrderItem.hasContentValue(supplier, "Y");
					}
					if(!IS_SUPPLIER_NOTICE){
						IS_SUPPLIER_NOTICE = ordOrderItem.hasContentValue(OrderEnum.ORDER_TICKET_TYPE.notify_type.name(), SuppGoods.NOTICETYPE.QRCODE.name());
					}
					if(IS_SUPPORT_DESTROY_CODE)
						IS_SUPPORT_DESTROY_CODE = supportDestroy(ordOrderItem);
				}
			}
			//子订单非FAX,非EBK，非对接，不能及时入园
			for(OrdOrderItem item : ordItemsList){
				Map<String,Object> performMap = item.getContentMap();
				String categoryCode =  (String) performMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
				if (ProductCategoryUtil.isTicket(categoryCode)) {
					boolean isFaxNotice = item.hasContentValue(fax, "Y");
					boolean isEbkNotice = false;
//						Map<String,Object> paramsMap=new HashMap<String,Object>();
//						paramsMap.put("objectId", item.getSuppGoodsId());
//						paramsMap.put("objectType", EbkUserPrdGoodsRe.OBJECT_TYPE.SUPP_GOODS);
//						ResultHandleT<List<Long>> result = ebkUserClientService.getEbkUserPrdGoodsReIds(paramsMap);
//						List<Long> contentList = result.getReturnContent();
//						if(contentList!=null&&contentList.size()>0){
//							isEbkNotice= true;
//						}
					Map<String,Object> paramUser=new HashMap<String,Object>();
					paramUser.put("cancelFlag", "Y");
					paramUser.put("supplierId", item.getSupplierId());
					List<EbkUser> ebkUserList = ebkUserClientService.getEbkUserList(paramUser).getReturnContent();
					if(ebkUserList!=null&& !ebkUserList.isEmpty()){
						isEbkNotice= true;
					}
					boolean isSupplierNotice = false;
					isSupplierNotice = item.hasContentValue(supplier, "Y");
					if(!isSupplierNotice){
						isSupplierNotice = item.hasContentValue(OrderEnum.ORDER_TICKET_TYPE.notify_type.name(), SuppGoods.NOTICETYPE.QRCODE.name());
					}
					if (!isFaxNotice && !isEbkNotice
							&& !isSupplierNotice) {
						IS_ENTER_NOT_IN_TIME = Boolean.TRUE;
						break;
					}
				}
			}
		}catch(Exception e){
			LOG.error("querySupplierNoticeStatus orderId="+orderId+",error", e);
			IS_ERROR = Boolean.TRUE;
		}
		map.put("IS_ERROR", IS_ERROR);
		map.put("IS_FAX_NOTICE", IS_FAX_NOTICE);
		map.put("IS_EBK_NOTICE", IS_EBK_NOTICE);
		map.put("IS_ENTER_NOT_IN_TIME",IS_ENTER_NOT_IN_TIME);
		map.put("IS_SUPPLIER_NOTICE", IS_SUPPLIER_NOTICE);
		map.put("IS_SUPPORT_DESTROY_CODE", IS_SUPPORT_DESTROY_CODE);
		LOG.info("============querySupplierNoticeStatus orderId={} end, map={}",orderId, map.toString());
		return map;
	}
	@Override
	public Map<String, Boolean> queryOrderInfoStatus(Long orderId) {
		LOG.info("============queryOrderInfoStatus orderId={} start=======",orderId);
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		Boolean IS_IN_USE = Boolean.FALSE;//订单是否已经使用(包括全部使用和部分使用)
		Boolean IS_IN_PART_USE = Boolean.FALSE;//订单是否部分使用
		Boolean IS_NEED_CONFIRM = Boolean.FALSE;//是否待人工确认
		Boolean IS_DISTRIBUTOR_SUPPORT = Boolean.FALSE;		//是否分销
		Boolean IS_ERROR = Boolean.FALSE;   //是否异常
		Boolean IS_ENTITY_TICKET = Boolean.FALSE;   // 是否是实体票
		Boolean IS_OVERDUE=Boolean.FALSE;//期票超过有效期30天
		Boolean IS_LOSS_AMOUNT_ZERO = Boolean.FALSE;//退款金额为0
        Boolean IS_UNRETREATAND_CHANGE = Boolean.FALSE;//不退不改
		String performStatus = "";
		try{
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			IS_DISTRIBUTOR_SUPPORT = isDistribution(order);
			List<OrdOrderItem> ordItemsList =order.getOrderItemList();

			//订单使用状态
			List<String> perFormStatusList = new ArrayList<String>();
			for (OrdOrderItem ordOrderItem : ordItemsList) {
				//门票业务类订单使用状态
				Map<String,Object> performMap = ordOrderItem.getContentMap();
				String categoryCode =  (String) performMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
				if (ProductCategoryUtil.isTicket(categoryCode)) {
					if(!IS_ENTITY_TICKET){//是否是实体票
						IS_ENTITY_TICKET = ordOrderItem.hasExpresstypeDisplay();
					}
					//门票业务类订单使用状态
					List<OrdTicketPerform> resultList = new ArrayList<OrdTicketPerform>();
					resultList = complexQueryService.selectByOrderItem(ordOrderItem.getOrderItemId());
					//门票状态
					String performStatusName=OrderEnum.PERFORM_STATUS_TYPE.getCnName(showPerformStatus(resultList,order,ordOrderItem)) ;
					perFormStatusList.add(performStatusName);
				}
                if(SuppGoodsBaseTimePriceVo.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equalsIgnoreCase(ordOrderItem.getCancelStrategy())){
                    IS_UNRETREATAND_CHANGE = Boolean.TRUE;
                }
			}
			//门票业务类订单使用状态
			if (OrderUtils.isTicketByCategoryId(order.getCategoryId())) {
				//门票业务类订单使用状态
				performStatus = OrderUtils.getMainOrderPerformStatus(perFormStatusList);
				if(performStatus.contains("待人工确认")){
					IS_NEED_CONFIRM = Boolean.TRUE;
				}
				else if(perFormStatusList.contains("部分使用") || perFormStatusList.contains("已使用")){
					IS_IN_USE = Boolean.TRUE;
					if("部分使用".equals(performStatus)){
						IS_IN_PART_USE = Boolean.TRUE;
					}
				}
				IS_OVERDUE=isOverdue(ordItemsList);
				IS_LOSS_AMOUNT_ZERO=refundAmountIsZero(ordItemsList);
			}else{//非门票
				IS_ERROR = Boolean.TRUE;
			}
		}catch(Exception e){
			LOG.error("performOrderStatus orderId="+orderId+",error", e);
			IS_ERROR = Boolean.TRUE;
		}
		map.put("IS_ERROR", IS_ERROR);
		map.put("IS_NEED_CONFIRM", IS_NEED_CONFIRM);
		map.put("IS_IN_USE", IS_IN_USE);
		map.put("IS_IN_PART_USE", IS_IN_PART_USE);
		map.put("IS_DISTRIBUTOR_SUPPORT", IS_DISTRIBUTOR_SUPPORT);
		map.put("IS_ENTITY_TICKET", IS_ENTITY_TICKET);
		map.put("IS_OVERDUE", IS_OVERDUE);
		map.put("IS_LOSS_AMOUNT_ZERO", IS_LOSS_AMOUNT_ZERO);
        map.put("IS_UNRETREATAND_CHANGE",IS_UNRETREATAND_CHANGE);
		LOG.info("============queryOrderInfoStatus orderId={} end, map={}",orderId, map.toString());
		return map;
	}

	/**
	 * 康旅卡
	 * @param orderId
	 * @return
	 */
	@Override
	public Map<String, Boolean> queryOrderInfoStatusKanglv(Long orderId) {
		LOG.info("============queryOrderInfoStatus orderId={} start=======",orderId);
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		Boolean IS_IN_USE = Boolean.FALSE;//订单是否已经使用(包括全部使用和部分使用)
		Boolean IS_ERROR = Boolean.FALSE;   //是否异常
		try{
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			List<OrdOrderItem> ordItemsList =order.getOrderItemList();
			//订单使用状态
			List<String> perFormStatusList = new ArrayList<String>();
			for (OrdOrderItem ordOrderItem : ordItemsList) {
				//门票业务类订单使用状态
				Map<String,Object> performMap = ordOrderItem.getContentMap();
				String categoryCode =  (String) performMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
				if (ProductCategoryUtil.isTicket(categoryCode)) {
					//门票业务类订单使用状态
					List<OrdTicketPerform> resultList = new ArrayList<OrdTicketPerform>();
					resultList = complexQueryService.selectByOrderItem(ordOrderItem.getOrderItemId());
					//门票状态
					String performStatusName=OrderEnum.PERFORM_STATUS_TYPE.getCnName(showPerformStatus(resultList,order,ordOrderItem)) ;
					perFormStatusList.add(performStatusName);
				}
			}
			//门票业务类订单使用状态
			if (OrderUtils.isTicketByCategoryId(order.getCategoryId())) {
				//门票业务类订单使用状态
				if(perFormStatusList.contains("部分使用") || perFormStatusList.contains("已使用")){
					IS_IN_USE = Boolean.TRUE;
				}
			}else{//非门票
				IS_ERROR = Boolean.TRUE;
			}
		}catch(Exception e){
			LOG.error("performOrderStatus orderId="+orderId+",error", e);
			IS_ERROR = Boolean.TRUE;
		}
		map.put("IS_ERROR", IS_ERROR);
		map.put("IS_IN_USE", IS_IN_USE);
		LOG.info("============queryOrderInfoStatus orderId={} end, map={}",orderId, map.toString());
		return map;
	}

	//是否期票且逾期超过30天
	private boolean isOverdue(List<OrdOrderItem> orderItemList) throws Exception{
		Boolean isOverdue = null;
		Date lastTime =null;
		for (OrdOrderItem orderItem : orderItemList) {
			Boolean isAperiodic = orderItem.hasTicketAperiodic();
			if (isAperiodic) {
				// 取出期票的有效时间
				Date suppGoodExpEndTime = null;
				// 首选取快照中的最晚有效期
				Object expEndTime = orderItem
							.getContentValueByKey("goodsEndTime");
				if (null != expEndTime) {
					String expEndTimeStr = expEndTime.toString();
					SimpleDateFormat sdf = new SimpleDateFormat(
								DateUtil.HHMM_DATE_FORMAT);
					suppGoodExpEndTime = sdf.parse(expEndTimeStr);
				}
				// 历史数据取子订单最晚游玩日有效期
				if (null == suppGoodExpEndTime) {
					suppGoodExpEndTime = orderItem.getValidEndTime();
				}
				// 最后取商品有效期
				if (null == suppGoodExpEndTime) {
					SuppGoodsExp suppGoodExp = suppGoodsClientService
								.findTicketSuppGoodsExp(orderItem.getSuppGoodsId());
					suppGoodExpEndTime = suppGoodExp.getEndTime();
				}
				if (null != suppGoodExpEndTime) {
					if(lastTime==null||suppGoodExpEndTime.before(lastTime)){
							lastTime = (Date) suppGoodExpEndTime;
					}
				}else{
					isOverdue = false;
					return false;
				}
			} else {
					isOverdue = false;
					return false;
			}
		}
		Date nowTime = new Date();
		Date endTime = DateUtils.addDays(lastTime, 30);
		if (nowTime.after(endTime)) {
			isOverdue = true;
		} else {
			isOverdue = false;
		}
		return isOverdue;
	}
	//能否算出扣款金额
	@Override
	public Map<String, Object> queryOrderLossAmount(Long orderId){
		LOG.info("============queryOrderLossAmount orderId={} start=======",orderId);
		Map<String, Object> map = new HashMap<String, Object>();
		Boolean IS_ERROR = Boolean.FALSE;
		Long LOSS_AMOUNT = 0L;//定义主订单扣款金额
		Long REFUND_AMOUNT = null; //退款金额
		List<Long> itemRuleList = new ArrayList<Long>();
		try
		{
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			Long amountChange = orderDetailAction.getTotalAmountChange(order.getOrderId(),"ORDER",null);
			List<OrdOrderItem> orderItemList = null;
			//如果订单结果修改
			if(amountChange!=0)
			{
				orderItemList = orderDetailAction.calculateOrderPrice(order,amountChange);
			}else{
				orderItemList = order.getOrderItemList();
			}
			List<SuppGoodsRefundVO> goodsRefundList = null;
			Date cancleDate = new Date();
			int i = 0;
			for (OrdOrderItem orderItem : orderItemList) {
				Date lastTime = orderRefundRulesService.getLastTime(orderItem);
				goodsRefundList = SuppGoodsRefundTools.calcDeductAmt(orderItem, lastTime, cancleDate);

				if(null !=goodsRefundList && goodsRefundList.size()>0){
					i+=1;
					//退改信息
					for (SuppGoodsRefundVO suppGoodsRefundVO : goodsRefundList) {
						//根据当前时间匹配的规则
						if(null != suppGoodsRefundVO.getIsCurrent()&&suppGoodsRefundVO.getIsCurrent()){
							LOSS_AMOUNT +=suppGoodsRefundVO.getDeductAmt();
							itemRuleList.add(suppGoodsRefundVO.getRefundId());
						}

					}
				}else if(orderItem.hasCategory(BIZ_CATEGORY_TYPE.category_insurance)){
					Date nowDate=new Date();//当前日期
					Date visitDate=orderItem.getVisitTime();//游玩日期
					if(DateUtils.addMinutes(visitDate, 1439).before(nowDate)){//如果过了游玩日期，损失金额=原来损失金额+保险
						LOSS_AMOUNT+=orderItem.getTotalAmount();
					}
				}
			}
			if(itemRuleList.size() !=i || i==0){//本次订单取消不满足退改规则，请手动计算扣款金额
				REFUND_AMOUNT = null;
			}else if(LOSS_AMOUNT>order.getActualAmount()){//扣款金额大于付款金额，请人工计算
				REFUND_AMOUNT = null;
			}else{//计算退款金额
				REFUND_AMOUNT = order.getActualAmount()-LOSS_AMOUNT;
			}
		}catch(Exception e){
			LOG.error("lossAmount orderId="+orderId+",error", e);
			IS_ERROR = Boolean.TRUE;
		}
		map.put("IS_ERROR", IS_ERROR);
		map.put("LOSS_AMOUNT", REFUND_AMOUNT);
		LOG.info("============queryOrderLossAmount orderId={} end, map={}",orderId, map.toString());
		return map;
	}
	
	@Override
	public OrdRefundmentItemSplit queryOrdItemRefundAmountForTicket(Long orderItemId,Long refundQuantity){
		LOG.info("============queryTiketOrdItemRefundAmount orderItemId={} start=======",orderItemId);
		OrdRefundmentItemSplit ordRefundmentItemSplit = new OrdRefundmentItemSplit();
		Map<String,Object> map =  queryOrdItemRefundInfoForTicket(orderItemId,refundQuantity);
		ordRefundmentItemSplit.setActualAmout((Long) map.get("actualAmout"));
		ordRefundmentItemSplit.setInitActualLoss((Long) map.get("lossAmount"));
		ordRefundmentItemSplit.setInitRefundedPrice((Long) map.get("refundAmount"));
		ordRefundmentItemSplit.setRefundedAmount((Long) map.get("refundedAmont"));
		ordRefundmentItemSplit.setRefundOughtPrice((Long) map.get("refundOughtPrice"));
		ordRefundmentItemSplit.setActualRefundRule(String.valueOf(map.get("calculateRule")));
		ordRefundmentItemSplit.setRefundRuleSnapshot(String.valueOf(map.get("refundRule")));
		return ordRefundmentItemSplit;
	}
	
	@Override
	public Map<String,Object> queryOrdItemRefundInfoForTicket(Long orderItemId,Long refundQuantity){
		LOG.info("============queryTiketOrdItemRefundAmount orderItemId={} refundQuantity={} start=======",orderItemId,refundQuantity);
		Map<String,Object> map = new HashMap<String,Object>();
		Long refundedQuantity =0L;//子订单已退款份数
		Long refundedAmont = 0L; //子订单已退款金额
		Long lossAmount = null;//扣款金额
		Long refundAmount = null; //退款金额
		Long refundOughtPrice = null;//退款份数对应的实付金额
		Long actualAmout = 0L;//子订单原实付金额
		String refundRule ="";//匹配的退改规则  
		String calculateRule ="";//计算规则

		try
		{
			OrdOrderItem orderItem = orderUpdateService.getOrderItem(orderItemId);
			
			Long manualChangeAmount = 0L; //订单金额减少分摊
			if(!orderItem.hasCategory(BIZ_CATEGORY_TYPE.category_insurance)){
				OrderApportionInfoQueryVO orderApportionInfoQueryVO = new OrderApportionInfoQueryVO();
				orderApportionInfoQueryVO.setOrderId(orderItem.getOrderId());
				orderApportionInfoQueryVO.setOrderItemId(orderItem.getOrderItemId());
				OrderApportionInfoPO orderApportionInfo =apportionInfoQueryService.calculateOrderApportionInfo(orderApportionInfoQueryVO);
				LOG.info("子订单：" + orderItemId + "分摊, orderApportionInfo = " + JSON.toJSONString(orderApportionInfo));
				List<OrderItemApportionInfoPO> orderItemApportionInfos = orderApportionInfo.getOrderItemApportionInfoPOList();
				if(orderItemApportionInfos !=null && !orderItemApportionInfos.isEmpty()){
					manualChangeAmount = orderItemApportionInfos.get(0).getItemTotalManualChangeAmount();
					actualAmout = orderItemApportionInfos.get(0).getItemTotalActualPaidAmount();
				}
			}else{//保险
				actualAmout = orderItem.getPrice()*orderItem.getQuantity();
			}
			
			List<RefundOrderItemSplit> ordRefundMentItems = orderRefundSplitServiceAdapter.queryOrdRefundmentItemSplitAllByOrderItemId(orderItemId).getReturnContent();
			LOG.info("子订单：" + orderItemId + "已退, ordRefundMentItems = " + JSON.toJSONString(ordRefundMentItems));
			for(RefundOrderItemSplit item:ordRefundMentItems){
				if(orderItemId.equals(item.getOrderItemId())){
					refundedAmont += item.getRefundPrice();
					refundedQuantity += item.getRefundQuantity();
				}
			}
			
			// To fix bug 90191, comment out this paragraph, and get refundOughtPrice from [orderRefundSplitServiceAdapter.getMaxActualPayByRefund]
			/*
			// 修复bug 89842, 假设份数为1份，退一份之后，第二次欲退0份，【refundedQuantity < orderItem.getQuantity()】则无法成立，子订单退款份数对应实付金额就无法计算，故改为【<=】
			if(refundedQuantity <= orderItem.getQuantity()){
				if (refundQuantity == 0) {
					// 如退款份数为0，子订单退款份数所对应实付金额=子订单原总实付金额-子订单已退款金额
					refundOughtPrice =  actualAmout - refundedAmont;
				} else {
					//子订单退款份数所对应实付金额=（子订单原总实付金额-子订单已退款金额）/（预订份数-已退款份数）*退款份数
					refundOughtPrice =  (actualAmout - refundedAmont)*refundQuantity/(orderItem.getQuantity()-refundedQuantity);
				}
			}
			*/
			ResultHandleT<Map<String, Long>> actuallyPaidDetailReturned = orderRefundSplitServiceAdapter.getMaxActualPayByRefund(orderItemId, refundQuantity);
			if(actuallyPaidDetailReturned != null) {
				Map<String, Long> actuallyPaidDetail = actuallyPaidDetailReturned.getReturnContent();
				LOG.info("actuallyPaidDetail -> " + GsonUtils.toJson(actuallyPaidDetail));
				if (refundQuantity > 0) {
					if (actuallyPaidDetail.get("lastValid") == -1l) {
						refundOughtPrice = actuallyPaidDetail.get("averageActuanAmount") * refundQuantity;
						LOG.info("actuallyPaid -> " + refundOughtPrice);
					} else if (actuallyPaidDetail.get("lastValid") == 1l) {
						refundOughtPrice = actuallyPaidDetail.get("averageActuanAmount") * (refundQuantity - 1l) + actuallyPaidDetail.get("lastActuanAmount");
						LOG.info("allRemainingActuallyPaid -> " + refundOughtPrice);
					}
				} else {
					if (actuallyPaidDetail.get("lastValid") == -1l) {
//						refundOughtPrice = actuallyPaidDetail.get("averageActuanAmount") * actuallyPaidDetail.get("bookQuantity");
						refundOughtPrice = actuallyPaidDetail.get("initActualAmount") - refundedAmont;
						LOG.info("allActuallyPaid -> " + refundOughtPrice);
					} else if (actuallyPaidDetail.get("lastValid") == 1l) {
						refundOughtPrice = actuallyPaidDetail.get("lastActuanAmount");
						LOG.info("allRemainingActuallyPaid -> " + refundOughtPrice);
					}
				}
			}
			
			Date lastTime = orderRefundRulesService.getLastTime(orderItem);
			List<SuppGoodsRefundVO> goodsRefundList = SuppGoodsRefundTools.calcDeductAmtByRefundQualityNew(orderItem, lastTime, new Date(),refundQuantity,manualChangeAmount);
	
			if(null !=goodsRefundList && goodsRefundList.size()>0){
				//退改信息
				for (SuppGoodsRefundVO suppGoodsRefundVO : goodsRefundList) {
					//根据当前时间匹配的规则
					if(null != suppGoodsRefundVO.getIsCurrent()&&suppGoodsRefundVO.getIsCurrent()){
						lossAmount =suppGoodsRefundVO.getDeductAmt();
						refundRule = GsonUtils.toJson(suppGoodsRefundVO);
					}	
				}
				if(refundOughtPrice == null ||lossAmount==null || refundOughtPrice < lossAmount){
					refundAmount = null;
				}else{
					//子订单退款金额=子订单退款份数所对应实付金额-子订单扣款金额（手续费）
					refundAmount =  refundOughtPrice - lossAmount;
				}
			}else if(orderItem.hasCategory(BIZ_CATEGORY_TYPE.category_insurance)){
				Date nowDate=new Date();//当前日期
				Date visitDate=orderItem.getVisitTime();//游玩日期
				if(DateUtils.addMinutes(visitDate, 1439).before(nowDate)){//过了游玩日期
					refundAmount = 0L;
					lossAmount =orderItem.getPrice()*refundQuantity;
				}else{
					refundAmount =orderItem.getPrice()*refundQuantity;
					lossAmount = 0L;
				}
			}else if(isInvoiceExpress(orderItem)){
				//发票快递子单（不看退改，根据审核情况）
				refundAmount =orderItem.getPrice()*refundQuantity;
				lossAmount = 0L;
			}
			
		}catch(Exception e){
			LOG.error("lossAmount orderItemId="+orderItemId+",error", e);
		}
		calculateRule =  "子订单原实付金额="+PriceUtil.trans2YuanStr(actualAmout)+"；子订单退款份数所对应实付金额="+PriceUtil.trans2YuanStr(refundOughtPrice)+
				"；子订单扣款金额（手续费）="+PriceUtil.trans2YuanStr(lossAmount)+"；子订单退款金额="+PriceUtil.trans2YuanStr(refundAmount);

		map.put("actualAmout", actualAmout);
		map.put("lossAmount", lossAmount);
		map.put("refundAmount", refundAmount);
		map.put("refundedAmont", refundedAmont);
		map.put("refundOughtPrice", refundOughtPrice);
		map.put("calculateRule", calculateRule);
		map.put("refundRule", refundRule);
		LOG.info("queryOrdItemRefundInfoForTicket orderItemId = " + orderItemId + " result = " + JSON.toJSONString(map));
		return map;
	}

	/**
	 * 在线退款业务，计算门票的使用情况 包含期票和非期票
	 * @param resultList
	 * @param order
	 * @param ordOrderItem
	 * @return
	 */
	private String showPerformStatus(List<OrdTicketPerform> resultList,
			OrdOrder order, OrdOrderItem ordOrderItem) {
		if (ordOrderItem == null && order != null) {
			ordOrderItem = order.getMainOrderItem();
		}
		String performStatus = OrderUtils.calPerformStatus(resultList, ordOrderItem);
		if ("UNPERFORM".equals(performStatus)) {
			Boolean isAperiodic = ordOrderItem.hasTicketAperiodic();
			if (isAperiodic) {// 期票
				Date suppGoodExpEndTime = null;
				Object expEndTime = ordOrderItem
						.getContentValueByKey("goodsEndTime");
				if (null != expEndTime) {
					String expEndTimeStr = expEndTime.toString();
					suppGoodExpEndTime = DateUtil.toDate(expEndTimeStr,
							DateUtil.HHMM_DATE_FORMAT);
					// 历史数据取子订单最晚游玩日有效期
					if (null == suppGoodExpEndTime) {
						suppGoodExpEndTime = ordOrderItem.getValidEndTime();
					}
					if (null == suppGoodExpEndTime) {
						SuppGoodsExp suppGoodExp = suppGoodsService
								.findTicketSuppGoodsExp(ordOrderItem
										.getSuppGoodsId());
						suppGoodExpEndTime = suppGoodExp.getEndTime();
					}
				}
				if (suppGoodExpEndTime == null || (DateUtils.addMinutes(suppGoodExpEndTime, 1439).after(
						new Date()))) {
					performStatus = "UNPERFORM";
				} else {
					performStatus = "NEED_CONFIRM";
				}
			}
		}
		return performStatus;
	}

	private boolean refundAmountIsZero(List<OrdOrderItem> ordOrderItemList) throws Exception{
		for(OrdOrderItem ordOrderItem : ordOrderItemList){
			LOG.info("ordOrderItem refundRules = "+ordOrderItem.getRefundRules());
			if(ordOrderItem.getRefundRules() != null){

				//List<SuppGoodsRefund> refundList = JSONUtil.jsonArray2Bean(ordOrderItem.getRefundRules(), SuppGoodsRefund.class);
				List<SuppGoodsRefund> refundList =  new ArrayList<SuppGoodsRefund>();
				if(StringUtil.isNotEmptyString(ordOrderItem.getRefundRules())){
					List<SuppGoodsRefund> refundList_ = com.alibaba.fastjson.JSONArray.parseArray(ordOrderItem.getRefundRules(), SuppGoodsRefund.class);
					if(refundList_ != null){
						refundList = refundList_;
					}
				}
                Date visitTime = ordOrderItem.getVisitTime();
				if(CollectionUtils.isNotEmpty(refundList)){
					SuppGoodsRefund nearRefund = null;

                    SuppGoodsRefund otherRefund = null;
                    for(SuppGoodsRefund refund : refundList){
                        if(SuppGoodsRefund.CANCEL_TIME_TYPE.OTHER.name().equals(refund.getCancelTimeType())){
                            otherRefund = refund;
                            break;
                        }
                    }

                    if(otherRefund != null)
                        refundList.remove(otherRefund);

					Collections.sort(refundList, new Comparator<SuppGoodsRefund>(){
						@Override
						public int compare(SuppGoodsRefund o1, SuppGoodsRefund o2) {
							if(o1.getLatestCancelTime()>o2.getLatestCancelTime()){
								return -1;
							}else{
								return 1;
							}
						}
					});
					
					Date lastTime = orderRefundRulesService.getLastTime(ordOrderItem);
					for(SuppGoodsRefund refund : refundList){
						Date refundTime = DateUtils.addMinutes(lastTime,-refund.getLatestCancelTime().intValue());
						if(new Date().before(refundTime)){
							nearRefund = refund;
							break;
						}
					}
                    if(nearRefund == null)
                        nearRefund = otherRefund;

					if(nearRefund != null &&
                            (((SuppGoodsRefund.DEDUCTTYPE.PERCENT.name().equals(nearRefund.getDeductType())) && nearRefund.getDeductValue() == 10000)
							|| (SuppGoodsRefund.DEDUCTTYPE.AMOUNT.name().equals(nearRefund.getDeductType()) && nearRefund.getDeductValue().compareTo(ordOrderItem.getPrice()) >= 0))){
						return true;
					}
				}
			}
		}
		return false;
	}


	@Override
	public VstCashAccountVO queryMoneyAccountByUserId(Long userId) {
		return orderPriceService.queryMoneyAccountByUserId(userId);
	}


	@Override
	public boolean vstPayFromMoneyAccount(String bizType, Long userId,
			Long orderId, Long payAmount) {
		return orderPriceService.vstPayFromMoneyAccount(bizType, userId, orderId, payAmount);
	}


	@Override
	public boolean vstPayFromBonusAccount(String bizType, Long orderId,
			Long userId, Long payAmount) {
		return orderPriceService.vstPayFromBonusAccount(bizType, orderId, userId, payAmount);
	}


	@Override
	public List<UserCouponVO> getUserCouponList(BuyInfo buyInfo) {
		List<UserCouponVO> userCouplist =new ArrayList<UserCouponVO>();
		List<CouponCheckParam> couponCheckParams = null;
		Long userId = buyInfo.getUserNo();
		Long quantity = Long.valueOf(buyInfo.getQuantity());//初始化购买份数
		

		LOG.info("getLoginUserAccountInformation buyinfo:"+buyInfo.toJsonStr());

		//OrdOrderDTO order = bookService.initOrderAndCalc(buyInfo);//初始化订单
		OrdOrderDTO order = bookService.initOrderBasic(buyInfo);//初始化订单优化
		Long categoryId = order.getCategoryId();//获取产品的品类Id
		Long orderAmount = order.getValidPromtionAmount();//去除保险快递附加
		LOG.info("categoryId:"+categoryId+",validorderAmount:"+orderAmount);
		OrdOrderItem orderMainItem = null;

		for (OrdOrderItem orderItem : order.getOrderItemList()){
			if(quantity==0L && "true".equals(orderItem.getMainItem())){
				if(null != orderItem.getQuantity() && 0L!=orderItem.getQuantity()){
					quantity = orderItem.getQuantity();
					orderMainItem = orderItem;

				}else{
					Long adultQuantity = orderItem.getAdultQuantity();
					Long childQuantity = orderItem.getChildQuantity();
					quantity = adultQuantity.longValue()+childQuantity.longValue();
				}
			}
//			//去除保险快递的费用
//			if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){
//			    Long insuranceQuantity = 1L;
//			    if(null != orderItem.getQuantity()){
//			    	insuranceQuantity = orderItem.getQuantity();
//			    }
//				LOG.info("扣除保险费 :"+orderItem.getPrice()*insuranceQuantity);
//				orderAmount = orderAmount-orderItem.getPrice()*insuranceQuantity;
//			}
//			//去除快递押金的费用
//			if(BizEnum.BIZ_CATEGORY_TYPE.category_other.getCategoryId().equals(orderItem.getCategoryId())){
//				if(ProdProduct.PRODUCTTYPE.DEPOSIT.name().equals(OrderUtil.getProductType(orderItem))){
//					Long depositQuantity = 1L;
//					if(null != orderItem.getQuantity()){
//						depositQuantity = orderItem.getQuantity();
//					}
//					LOG.info("扣除押金费 :"+orderItem.getPrice());
//					orderAmount = orderAmount  - orderItem.getPrice()*depositQuantity;
//				}else{
//					LOG.info("扣除快递费 :"+orderItem.getPrice());
//					orderAmount = orderAmount - orderItem.getPrice();
//				}
//
//			}
			

		}
		
		//减去促销
		PriceInfo priceInfo=new PriceInfo();//couponExclusion
		List<PromPromotion> promotionList= orderPriceService.getPromotions(order,priceInfo); 
	    if(!Boolean.TRUE.equals(priceInfo.getCouponExclusion())){
	    	Long promotionAmount=0l;
	    	for(PromPromotion prom :promotionList){
				promotionAmount+=prom.getDiscountAmount();
			}
	        orderAmount=orderAmount-promotionAmount;
	        LOG.info("减去促销promtionAmount:"+promotionAmount+",orderAmount:"+orderAmount);
	    }
	    if(orderAmount<=0){
	      return userCouplist;
	    }
		if(quantity==0L){
			quantity=1l;
		}

		LOG.info("获取份数是 :"+quantity);
		//判断门票是否可用优惠券
		if(innerValidateGoodsCategory(categoryId) || innerValidateProductCategory(categoryId))
		{
			LOG.info("属于门票的产品，进入验证是否支持优惠券");
			ProdProduct product = null;
			if(null != buyInfo.getProductId()){
				ResultHandleT<ProdProduct> restult = productHotelAdapterClientService.findProdProductByIdFromCache(buyInfo.getProductId());
				if(null != restult && null != restult.getReturnContent()){
					product = restult.getReturnContent();
				}
			}
			//判断是否可使用优惠券
			String icCoupon = isUseCoupon(buyInfo, orderMainItem, product);
			if("N".equalsIgnoreCase(icCoupon)){
				UserCouponVO userCoupon = new UserCouponVO();
				userCoupon.setValidInfo("本产品不支持使用优惠券!");
				List<UserCouponVO> userCouponList = new ArrayList<UserCouponVO>();
				userCouponList.add(userCoupon);
				return userCouponList;
			}
		}

		CouponCheckParam couponCheckParam = null;
		if(CollectionUtils.isNotEmpty(order.getOrderPackList())){
			couponCheckParams = new ArrayList<CouponCheckParam>();
			//机+酒 去除大交通优惠券
			//boolean isCategoryRouteFlightHotel = false;
			for (OrdOrderPack p:order.getOrderPackList()) {
				LOG.info("OrdOrderPackSubCategoryId:"+order.getSubCategoryId());
				couponCheckParam = new CouponCheckParam();
				couponCheckParam.setUserId(userId);
				couponCheckParam.setProductType(p.getProduct().getProductType());
//				if(null ==p.getCategoryId() || p.getCategoryId()==0L){
//				 couponCheckParam.setCategoryId(getCategoryIdByBuyInfo(buyInfo));
//				}else if(p.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId())){//20161013 CHENHAO自由行产品
//					if(order.getSubCategoryId()!=null && (order.getBuCode().equalsIgnoreCase(CommEnumSet.BU_NAME.LOCAL_BU.getCode())
//							|| order.getBuCode().equalsIgnoreCase(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode())
//							|| order.getBuCode().equalsIgnoreCase(CommEnumSet.BU_NAME.DESTINATION_BU.getCode()))){
//						//couponCheckParam.setProductType(null);
//						couponCheckParam.setCategoryId(order.getSubCategoryId());
//					}else{
//						couponCheckParam.setCategoryId(p.getCategoryId());
//					}
//				}else{
//					couponCheckParam.setCategoryId(p.getCategoryId());
//				}
				if(order.getSubCategoryId()!=null){
					couponCheckParam.setCategoryId(order.getSubCategoryId());
				}else{
					couponCheckParam.setCategoryId(order.getCategoryId());
				}
				//if(order.getSubCategoryId() != null && !isCategoryRouteFlightHotel){
					//isCategoryRouteFlightHotel = order.getSubCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId());
				//}
				//LOG.info("=================II2========================="+isCategoryRouteFlightHotel);
				couponCheckParam.setPlatform(generatePlatformStr(buyInfo));
				couponCheckParam.setSaleUnitType("PROD");//BRANCH or PROD
				couponCheckParam.setSaleUnitId(p.getProductId());
				couponCheckParam.setIsMainProduct("true");
				//changeCouponCheckParamAddBu(couponCheckParam,order);
				couponCheckParams.add(couponCheckParam);
				
			}
//			for (OrdOrderItem it:order.getOrderItemList()) {
//				//20170905 机+酒  去除大交通优惠券
//				LOG.info("=================IT11========================="+it.getCategoryId());
//				if(isCategoryRouteFlightHotel && innerIsCategoryTraffic(it.getCategoryId())){
//					LOG.info("=================IT11========================="+it.getCategoryId());
//					continue ;
//				}
//				couponCheckParam = new CouponCheckParam();
//				couponCheckParam.setUserId(userId);
//				couponCheckParam.setCategoryId(it.getSuppGoods().getCategoryId());
//				couponCheckParam.setPlatform(generatePlatformStr(buyInfo));
//				couponCheckParam.setSaleUnitType("BRANCH");//BRANCH or PROD
//				couponCheckParam.setSaleUnitId(it.getSuppGoodsId());
//				couponCheckParam.setProductType(it.getSuppGoods().getProdProduct().getProductType());
//				couponCheckParam.setPrice(it.getPrice());
//				couponCheckParam.setIsMainProduct(it.getMainItem());
//				if(it.getQuantity()!=null){
//					couponCheckParam.setQuantity(it.getQuantity().intValue());
//				}
//				changeCouponCheckParamAddBu(couponCheckParam,order);
//				couponCheckParams.add(couponCheckParam);
//				String json = JSONUtil.bean2Json(couponCheckParam);
//				LOG.info("优惠券接口参数2 couponCheckParam json:"+json+"#######");
//			}
		}else{
			couponCheckParams = couponCheckParams(order, buyInfo);
		}
		CouponOrderDTO dto= new CouponOrderDTO();
		dto.setUserId(userId);
		dto.setDistributorId(buyInfo.getDistributionId());
		dto.setOrderSource("master");//vst order 都是这个
		dto.setOughtAmount(orderAmount);
		dto.setQuantity(quantity);
		LOG.info("getUserCouponList request dto:"+JSONUtil.bean2Json(dto));
		List<com.lvmama.coupon.mark.favor.bo.CouponCheckParam> list2 =new ArrayList<com.lvmama.coupon.mark.favor.bo.CouponCheckParam>();
		for(CouponCheckParam p:couponCheckParams){
			com.lvmama.coupon.mark.favor.bo.CouponCheckParam p2=new com.lvmama.coupon.mark.favor.bo.CouponCheckParam();
			BeanUtils.copyProperties(p, p2);
			list2.add(p2);
		}
		LOG.info("request list2 :"+JSONUtil.bean2Json(list2));
		List<com.lvmama.coupon.mark.dto.UserCouponVO> userCouplist2 = couponOrderService.getUserCouponList(list2, dto);
		LOG.info("return  json:"+JSONUtil.bean2Json(userCouplist2));
		if(userCouplist2!=null){
			for(com.lvmama.coupon.mark.dto.UserCouponVO vo:userCouplist2){
				UserCouponVO vo2 =new UserCouponVO();
				BeanUtils.copyProperties(vo, vo2);
				userCouplist.add(vo2);
			}
		}
		return userCouplist;
	}

	/**
	 * 生成平台字符串，
	 * "VST", "MOBILE","LINEPROM"
	 * */
	private String generatePlatformStr(BuyInfo buyInfo) {
		Long distributionId = buyInfo.getDistributionId();
		if(distributionId==6l){
			return "LINEPROM";
		}
		Long distributionChannel = buyInfo.getDistributionChannel();
		String distributorCode = buyInfo.getDistributorCode();
		OrderChannelJudgeVO orderChannelJudgeVO = new OrderChannelJudgeVO(distributionId, distributionChannel, distributorCode);
		OrderEnum.OrderChannel orderChannelEnum = orderChannelService.judgeOrderChannel(orderChannelJudgeVO);

		if(orderChannelEnum.getOrderChannelNo() == OrderEnum.OrderChannel.WIRELESS.getOrderChannelNo()){
			return "MOBILE";
		}

		return "VST";
	}


	@Override
	public List<UserCouponVO> getUserCouponVOList(BuyInfo buyInfo) {
		return getUserCouponVOList(buyInfo,false);
	}

	@Override
	public List<UserCouponVO> getUserCouponVOList(BuyInfo buyInfo,boolean isCreateOrder) {

		LOG.info("getUserCouponVOList下单方法中接收到的JSON"+buyInfo.toJsonStr());
		List<UserCouponVO> couplist = new ArrayList<UserCouponVO>();
		OrdOrderDTO orderDto=null;
		if(isCreateOrder){
			orderDto = bookService.initOrderAndCalc(buyInfo);//减掉促销金额
		}else{
			orderDto = bookService.initOrderBasic(buyInfo);//初始化订单优化
		}
		Long categoryId = getCategoryIdByBuyInfo(buyInfo);//获取产品的品类Id
		
		OrdOrderItem orderMainItem = null;
		Long orderAmount = orderDto.getOughtAmount();
		Long quantity = Long.valueOf(buyInfo.getQuantity());
		for (OrdOrderItem orderItem : orderDto.getOrderItemList()){
			if(quantity==0L && "true".equals(orderItem.getMainItem())){
				if(null != orderItem.getQuantity() && 0L!=orderItem.getQuantity()){
					quantity = orderItem.getQuantity();
					orderMainItem = orderItem;
				}else{
					Long adultQuantity = orderItem.getAdultQuantity();
					Long childQuantity = orderItem.getChildQuantity();
					quantity = adultQuantity.longValue()+childQuantity.longValue();
				}
			}
			//去除保险快递的费用
			if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){
			    Long insuranceQuantity = 1L;
			    if(null != orderItem.getQuantity()){
			    	insuranceQuantity = orderItem.getQuantity();
			    }
				LOG.info("扣除保险费 :"+orderItem.getPrice()*insuranceQuantity);
				orderAmount = orderAmount-orderItem.getPrice()*insuranceQuantity;
			}
			//去除快递押金的费用
			if(BizEnum.BIZ_CATEGORY_TYPE.category_other.getCategoryId().equals(orderItem.getCategoryId())){
				if(ProdProduct.PRODUCTTYPE.DEPOSIT.name().equals(OrderUtil.getProductType(orderItem))){
					Long depositQuantity = 1L;
					if(null != orderItem.getQuantity()){
						depositQuantity = orderItem.getQuantity();
					}
					LOG.info("扣除押金费 :"+orderItem.getPrice());
					orderAmount = orderAmount  - orderItem.getPrice()*depositQuantity;
				}else{
					LOG.info("扣除快递费 :"+orderItem.getPrice());
					orderAmount = orderAmount - orderItem.getPrice();
				}

			}
			if(orderItem.getItem().getRouteRelation()!=null&&"ADDITION".equals(orderItem.getItem().getRouteRelation().toString())){
				if(buyInfo.getDistributionId()==2l||buyInfo.getDistributionId()==3l||Boolean.TRUE.equals(buyInfo.getIsNewCoupon())){
					LOG.info("扣除附加:"+orderItem.getTotalAmount());  
					orderAmount=orderAmount - orderItem.getTotalAmount();
				}
			}

		}
		
		//判断商品品类 和产品品类，wifi是否可用优惠券
		if( innerValidateGoodsCategory(categoryId) || innerValidateProductCategory(categoryId))	{

			ProdProduct product = null;
			if(null != buyInfo.getProductId()){
//						ResultHandleT<ProdProduct> result = prodProductClientService.findProdProductById(buyInfo.getProductId());
				ResultHandleT<ProdProduct> result = productHotelAdapterClientService.findProdProductByIdFromCache(buyInfo.getProductId());
				if(null != result && null != result.getReturnContent()){
					product = result.getReturnContent();
				}
			}
			//判断是否可使用优惠券
			String icCoupon = isUseCoupon(buyInfo, orderMainItem, product);
			if("N".equalsIgnoreCase(icCoupon)){
				UserCouponVO userCoupon = new UserCouponVO();
				userCoupon.setValidInfo("本产品不支持使用优惠券!");
				List<UserCouponVO> userCouponList = new ArrayList<UserCouponVO>();
				userCouponList.add(userCoupon);
				return userCouponList;
			}
		}
		if(CollectionUtils.isEmpty(buyInfo.getUserCouponVoList())||orderAmount<=0){
			return couplist;
		}
		
		if(isCreateOrder==false){//填单页输入优惠劵 减去促销
			PriceInfo priceInfo=new PriceInfo();
			List<PromPromotion> promotionList= orderPriceService.getPromotions(orderDto,priceInfo); 
			LOG.info("CouponExclusion:"+priceInfo.getCouponExclusion());
		    if(!Boolean.TRUE.equals(priceInfo.getCouponExclusion())){
		    	Long promotionAmount=0l;
		    	for(PromPromotion prom :promotionList){
					promotionAmount+=prom.getDiscountAmount();
				}
		        orderAmount=orderAmount-promotionAmount;
		        LOG.info("减去促销promtionAmount:"+promotionAmount+",orderAmount:"+orderAmount);
		    }
		}
		if(quantity==0L){
			quantity=1l;
		}
		
		//优惠券券号
		List<UserCouponVO> couponCodeVOList = buyInfo.getUserCouponVoList();
		Long userId = buyInfo.getUserNo();
		//优惠券验证参数
		List<CouponCheckParam> couponCheckParams = null;
		List<OrdOrderPack> orderPackList = orderDto.getOrderPackList();
		CouponCheckParam couponCheckParam = null;
		if(CollectionUtils.isNotEmpty(orderPackList)){
			
			//机+酒 去除大交通优惠券
			//boolean isCategoryRouteFlightHotel = false;
			couponCheckParams = new ArrayList<CouponCheckParam>();
			for (OrdOrderPack pack : orderPackList) {
				LOG.info("packcategoryId:"+pack.getCategoryId()+",ordersubCategoryid:"+orderDto.getSubCategoryId());
				couponCheckParam = new CouponCheckParam();
				couponCheckParam.setUserId(userId);
				couponCheckParam.setProductType(pack.getProduct().getProductType());
				if(null ==pack.getCategoryId() ||pack.getCategoryId()==0L){
					couponCheckParam.setCategoryId(getCategoryIdByBuyInfo(buyInfo));
				}else if(pack.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId())){//20161010 CHENHAO自由行产品
					if(orderDto.getSubCategoryId()!=null && (orderDto.getBuCode().equalsIgnoreCase(CommEnumSet.BU_NAME.LOCAL_BU.getCode())
							|| orderDto.getBuCode().equalsIgnoreCase(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode())
							|| orderDto.getBuCode().equalsIgnoreCase(CommEnumSet.BU_NAME.DESTINATION_BU.getCode()))){
						//couponCheckParam.setProductType(null);
						couponCheckParam.setCategoryId(orderDto.getSubCategoryId());
					}else{
						couponCheckParam.setCategoryId(pack.getCategoryId());
					}
				}else{
				    couponCheckParam.setCategoryId(pack.getCategoryId());
				}
			
//				if(orderDto.getSubCategoryId() != null && !isCategoryRouteFlightHotel){
//					isCategoryRouteFlightHotel = orderDto.getSubCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId());
//				}
//				LOG.info("=================II21========================="+isCategoryRouteFlightHotel);
				couponCheckParam.setPlatform(generatePlatformStr(buyInfo));
				couponCheckParam.setSaleUnitType("PROD");
				couponCheckParam.setSaleUnitId(pack.getProductId());
				couponCheckParam.setIsMainProduct("true");
				//changeCouponCheckParamAddBu(couponCheckParam,orderDto);
				couponCheckParams.add(couponCheckParam);
			}
//			for (OrdOrderItem it:orderDto.getOrderItemList()) {
//				//20170905 机+酒  去除大交通优惠券
//				LOG.info("=================IT112========================="+it.getCategoryId());
//				if(isCategoryRouteFlightHotel && innerIsCategoryTraffic(it.getCategoryId())){
//					LOG.info("=================IT112========================="+it.getCategoryId());
//					continue ;
//				}
//				couponCheckParam = new CouponCheckParam();
//				couponCheckParam.setUserId(userId);
//				couponCheckParam.setCategoryId(it.getCategoryId());
//				couponCheckParam.setPlatform(generatePlatformStr(buyInfo));
//				couponCheckParam.setSaleUnitType("BRANCH");//BRANCH or PROD
//				couponCheckParam.setSaleUnitId(it.getSuppGoods().getSuppGoodsId());
//				couponCheckParam.setProductType(it.getSuppGoods().getProdProduct().getProductType());
//				couponCheckParam.setPrice(it.getPrice());
//		        couponCheckParam.setIsMainProduct(it.getMainItem());
//		        if(it.getQuantity()!=null){
//		          couponCheckParam.setQuantity(it.getQuantity().intValue());
//		        }
//				changeCouponCheckParamAddBu(couponCheckParam,orderDto);
//				couponCheckParams.add(couponCheckParam);
//				String json = JSONUtil.bean2Json(couponCheckParam);
//				LOG.info("优惠券接口参数5 couponCheckParam json:"+json+"#######");
//			}
		}else{
			couponCheckParams = couponCheckParams(orderDto, buyInfo);
		}
		LOG.info("ELSE优惠券接口参数3 couponCheckParam json:"+JSONUtil.bean2Json(couponCheckParams)+"#######");
		List<String> couponCodeList = new ArrayList<String>();
		for(UserCouponVO vo:couponCodeVOList){
			couponCodeList.add(vo.getCouponCode());
			LOG.info("==========cm============couponCodeVOList.coupon.code:"+vo);

		}
        CouponOrderDTO dto= new CouponOrderDTO();
        dto.setUserId(userId);
        dto.setDistributorId(buyInfo.getDistributionId());
        dto.setOrderSource("master");//vst order 都是这个
        dto.setOughtAmount(orderAmount);
        dto.setQuantity(quantity);
        List<com.lvmama.coupon.mark.favor.bo.CouponCheckParam> list2 =new ArrayList<com.lvmama.coupon.mark.favor.bo.CouponCheckParam>();
        for(CouponCheckParam p:couponCheckParams){
          com.lvmama.coupon.mark.favor.bo.CouponCheckParam p2=new com.lvmama.coupon.mark.favor.bo.CouponCheckParam();
          BeanUtils.copyProperties(p, p2);
          list2.add(p2);
        }
        LOG.info("insert request dto:"+JSONUtil.bean2Json(dto));
        LOG.info("list2:"+JSONUtil.bean2Json(list2));
        List<com.lvmama.coupon.mark.dto.UserCouponVO> userCouplist2 = null;
        if(isCreateOrder){
        	userCouplist2=couponOrderService.validateOrderCouponCode(list2,couponCodeList, dto);
        }else{
        	userCouplist2=couponOrderService.validateAddCouponCode(list2,couponCodeList, dto);
        }
        LOG.info("return userCouplist2:"+JSONUtil.bean2Json(userCouplist2));
        if(userCouplist2!=null){
	        for(com.lvmama.coupon.mark.dto.UserCouponVO vo:userCouplist2){
	            UserCouponVO vo2 =new UserCouponVO();
	            BeanUtils.copyProperties(vo, vo2);
	            couplist.add(vo2);
	          }
        }
		return couplist;
	}

	private void changeCouponCheckParamAddBu(CouponCheckParam  couponCheckParam,OrdOrderDTO orderDto){
		String bu = orderDto.getBuCode();
		if(StringUtils.isNotBlank(bu)){
			couponCheckParam.setBu(bu);
		}
	}

	@Override
	public int updateMarkCouponCodeUsed(List<String> couponCodes, boolean used) {
//		 int a = favorServiceAdapter.updateMarkCouponCodeUsed(couponCodes, used);
		return favorServiceAdapter.updateMarkCouponCodeUsed(couponCodes, used);
	}

	@Override
	public Map<String,Object> calcDeductAmtForDistribution(
			Long orderId) {
		LOG.info("============ queryOrderLossAmount orderId={} =======", orderId);
		try {
			List<OrderItemRefundVo> refundList = new ArrayList<>();
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			List<OrdOrderItem> orderItemList = order.getOrderItemList();
			for (OrdOrderItem orderItem : orderItemList) {
				//获取子订单退款信息
				Map<String, Object> ordItemRefundInfo = queryOrdItemRefundInfoForTicket(orderItem.getOrderItemId(), orderItem.getQuantity());
				if (isRefundRuleAvailable(ordItemRefundInfo)) {//处理退款规则可用子订单
					setOrderItemRefundInfo(orderItem, ordItemRefundInfo, refundList);
				} else {//处理无法计算退款的子订单
					return getEmptyOrderItemAmountInfo(false, orderId, "包含无法计算退款的子订单");
				}
			}
			return validateAndGetOrderItemAmountInfo(refundList, orderId, order.getActualAmount());
		} catch (Exception e) {
			LOG.error("============ queryOrderLossAmount orderId = " + orderId + ", error :", e);
			return getEmptyOrderItemAmountInfo(true, orderId, "系统错误," + e.getMessage());
		}
	}


	private List<CouponCheckParam> couponCheckParams(OrdOrderDTO order,BuyInfo buyinfo){
		List<CouponCheckParam> couponCheckParams = new ArrayList<CouponCheckParam>();
		ProdProductParam param = new ProdProductParam();
		param.setProductProp(true);
		param.setProductBranchValue(true);
		param.setProdEcontract(true);
		CouponCheckParam couponCheckParam = null;
		List<OrdOrderItem> ordItemList = order.getOrderItemList();
//		for (OrdOrderItem ordOrderItem : ordItemList) {
//			couponCheckParam = new CouponCheckParam();
//			couponCheckParam.setUserId(buyinfo.getUserNo());
//			couponCheckParam.setCategoryId(getCategoryIdByBuyInfo(buyinfo));
//			couponCheckParam.setPlatform(generatePlatformStr(buyinfo));
//			couponCheckParam.setSaleUnitType("PROD");
//			couponCheckParam.setSaleUnitId(ordOrderItem.getProductId());
//			ResultHandleT<ProdProduct> resultHandle = productHotelAdapterClientService.findProdProductByIdFromCache(buyinfo.getProductId());
//			ProdProduct product = resultHandle.getReturnContent();
//			couponCheckParam.setProductType(product.getProductType());
//			//wifi/当地玩乐设置产品类型为空，暂时解决wifi品类绑定问题
//			if(BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(product.getBizCategoryId())||
//					BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(product.getBizCategoryId())||
//					BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(product.getBizCategoryId())||
//					OrderUtil.isWifiCategory(ordOrderItem)){
//				couponCheckParam.setProductType(null);
//			}
//			changeCouponCheckParamAddBu(couponCheckParam,order);
//			couponCheckParams.add(couponCheckParam);
//			String json = JSONUtil.bean2Json(couponCheckParam);
//			LOG.info("优惠券接口参数6######******PROD couponCheckParam json:"+json+"#######");
//		}
		for (OrdOrderItem ordOrderItem : ordItemList) {
			couponCheckParam = new CouponCheckParam();
			couponCheckParam.setUserId(buyinfo.getUserNo());
			couponCheckParam.setCategoryId(ordOrderItem.getSuppGoods().getCategoryId());
			couponCheckParam.setPlatform(generatePlatformStr(buyinfo));
			if(order.getCategoryId()!=null&&order.getCategoryId()==17l){
				couponCheckParam.setSaleUnitType("PROD");
				couponCheckParam.setSaleUnitId(ordOrderItem.getProductId());
			}else{
				couponCheckParam.setSaleUnitType("BRANCH");
				couponCheckParam.setSaleUnitId(ordOrderItem.getSuppGoodsId());
			}
			couponCheckParam.setProductType(ordOrderItem.getSuppGoods().getProdProduct().getProductType());
			//wifi/当地玩乐设置产品类型为空，暂时解决wifi品类绑定问题
			if(BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(ordOrderItem.getSuppGoods().getProdProduct().getBizCategoryId())||
					BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(ordOrderItem.getSuppGoods().getProdProduct().getBizCategoryId())||
					BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(ordOrderItem.getSuppGoods().getProdProduct().getBizCategoryId())
					||OrderUtil.isWifiCategory(ordOrderItem)){
				couponCheckParam.setProductType(null);
			}
			couponCheckParam.setPrice(ordOrderItem.getPrice());
		    couponCheckParam.setIsMainProduct(ordOrderItem.getMainItem());
		    if(ordOrderItem.getQuantity()!=null){
		        couponCheckParam.setQuantity(ordOrderItem.getQuantity().intValue());
		    }
			//changeCouponCheckParamAddBu(couponCheckParam,order);
			couponCheckParams.add(couponCheckParam);
		}
		return couponCheckParams;

	}

	/**
	 * 获取产品品类ID
	 * @param buyInfo
	 * @return
	 */
	private Long getCategoryIdByBuyInfo(BuyInfo buyInfo){
		Log.info("-------------start getCategoryIdByBuyInfo-----------");
		Long categoryId = buyInfo.getCategoryId();
		ProdProduct product = null;
		if(null ==categoryId || categoryId ==0L){
			Log.info("-------------categoryId is null-----------");
			 product = buyInfo.getProdProduct();
			if(null!=product){
				categoryId = product.getBizCategoryId();
			}else
			{
				Log.info("-------------product is null-----------");
				if(null !=buyInfo.getProductId()){
//					ResultHandleT<ProdProduct> restult = prodProductClientService.findProdProductById(buyInfo.getProductId());
					ResultHandleT<ProdProduct> restult = productHotelAdapterClientService.findProdProductByIdFromCache(buyInfo.getProductId());
					if(null != restult && null != restult.getReturnContent()){
						product = restult.getReturnContent();
						categoryId = product.getBizCategoryId();
					}
				}else{
					Log.info("-------------buyInfo.getProductId() is null-----------");
					List<Product> productList = buyInfo.getProductList();
					Long productId = 0L;
					if(CollectionUtils.isNotEmpty(productList)){
						productId = productList.get(0).getProductId();
//						ResultHandleT<ProdProduct> restult = prodProductClientService.findProdProductById(productId);
						ResultHandleT<ProdProduct> restult = productHotelAdapterClientService.findProdProductByIdFromCache(productId);
						if(null != restult){
							ProdProduct productBiz = restult.getReturnContent();
							categoryId = productBiz.getBizCategoryId();
						}
					}
				}
			}
		}
		return categoryId;
	}

	/**
	 * 是否支持使用优惠券
	 * @return
	 */
	private String isUseCoupon(BuyInfo buyinfo,OrdOrderItem ordMainItem,ProdProduct product){
		Log.info("开始检查门票是否可使用优惠券");
		Long catageryId = getCategoryIdByBuyInfo(buyinfo);
		Long goodsId = null;
		Long productId = null;
		if(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(catageryId)
		       || BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(catageryId)
		       || BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.getCategoryId().equals(catageryId)){
			if(null == product){
				List<Product> productList = buyinfo.getProductList();
				if(CollectionUtils.isNotEmpty(productList)){
					productId = productList.get(0).getProductId();
//					ResultHandleT<ProdProduct> restult = prodProductClientService.findProdProductByIdFromCache(productId);
					ResultHandleT<ProdProduct> restult = productHotelAdapterClientService.findProdProductByIdFromCache(productId);					
					if(null != restult){
						product = restult.getReturnContent();
					}
				}
			}
			//自主打包或者供应商打包
			if(null != product.getPackageType() && product.getPackageType().equals(PACKAGETYPE.LVMAMA.getCode())){
				productId = product.getProductId();
				Log.info("1111111------------------productID:"+productId);
			}else if(null != product.getPackageType() && BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.getCategoryId().equals(catageryId)
					&&product.getPackageType().equals(PACKAGETYPE.SUPPLIER.getCode())){
				productId = product.getProductId();
				Log.info("33333------------------productID:"+productId);
			}else
			{
				if(null != ordMainItem){
					goodsId=ordMainItem.getSuppGoodsId();
					if(null == goodsId){
						productId = ordMainItem.getProductId();
					}
				}
				
				if(null == goodsId && null ==productId){
					if(null != product){
						productId = product.getProductId();
					}
				}
				Log.info("2222222------------------productID:"+goodsId);
			}
		}else if(innerValidateProductCategory(catageryId)){
			//产品的处理
			if(null == product){
				List<Product> productList = buyinfo.getProductList();
				if(CollectionUtils.isNotEmpty(productList)){
					productId = productList.get(0).getProductId();
//					ResultHandleT<ProdProduct> restult = prodProductClientService.findProdProductByIdFromCache(productId);
					ResultHandleT<ProdProduct> restult = productHotelAdapterClientService.findProdProductByIdFromCache(productId);
					if(null != restult){
						product = restult.getReturnContent();
						productId = product.getProductId();
					}else{
						if(null != ordMainItem){
							goodsId=ordMainItem.getSuppGoodsId();
						}
					}
				}
			}
			//自由行特殊处理
			if(null != product){
				if(product.getBizCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId())){
					if(null != product.getSubCategoryId()){
						if(product.getSubCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId())||
								product.getSubCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId())){
							catageryId = product.getSubCategoryId();
						productId = product.getProductId();
						}
					}
				}else{
					if(null != ordMainItem){
						goodsId=ordMainItem.getSuppGoodsId();
					}
				}
			}

		}else
		{
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(catageryId)){
			    if(null == product){
	                List<Product> productList = buyinfo.getProductList();
	                if(CollectionUtils.isNotEmpty(productList)){
	                    productId = productList.get(0).getProductId();
//	                    ResultHandleT<ProdProduct> restult = prodProductClientService.findProdProductByIdFromCache(productId);
	                    ResultHandleT<ProdProduct> restult = productHotelAdapterClientService.findProdProductByIdFromCache(productId);
	                    if(null != restult){
	                        product = restult.getReturnContent();
	                        productId = product.getProductId();
	                    }else{
	                        if(null != ordMainItem){
	                            goodsId=ordMainItem.getSuppGoodsId();
	                        }
	                    }
	                }
	            }else{
	                productId = product.getProductId();
	            }
			}else if(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(catageryId)){
				// 酒套餐
				if(product != null){
					productId = product.getProductId();
				}else if(ordMainItem.getSuppGoodsId() != null){
					// 根据产品Id查询商品信息
					ResultHandleT<SuppGoods> resultHandleT = suppGoodsHotelAdapterClientService.findSuppGoodsById(ordMainItem.getSuppGoodsId());
					if(resultHandleT != null && resultHandleT.getReturnContent() != null){
						SuppGoods hotelCombSuppGoods = resultHandleT.getReturnContent();
						productId = hotelCombSuppGoods.getProductId();// 酒套餐产品Id
					}
				}
			}

			if(null != ordMainItem){
				goodsId=ordMainItem.getSuppGoodsId();
			}
			Log.info("========================GOODSID:"+goodsId);
		}

		int checkLimit = 0;
		String  islimit ="Y";
		Long checkCouponLimitId = null ;
		if(null !=goodsId){
			checkCouponLimitId = goodsId;
		}
		if(null !=productId){
			checkLimit = 1;
			checkCouponLimitId = productId;
		}
		Log.info("不支持优惠券检查参数=======================goodsId"+goodsId+"====productId+"+productId);
		Log.info("不支持优惠券检查参数============catageryId"+catageryId+"===========checkCouponLimitId"+checkCouponLimitId+"====checkLimit+"+checkLimit);
		//是否支持优惠券检查
		try {
			if(null != checkCouponLimitId){
				ResultHandleT<MarkcouponLimitInfo> resultCouponInfo =markCouponLimitClientService.couponIslimitInfo(checkCouponLimitId,checkLimit);
				if(resultCouponInfo.isSuccess() && resultCouponInfo.getReturnContent()!=null){
					MarkcouponLimitInfo markInfo = resultCouponInfo.getReturnContent();
					islimit = markInfo.getIslimit();
					Log.info("开始检查产品是否可使用优惠券---返回:"+markInfo.getIslimit());
				}
			}

		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}

		Log.info("不支持优惠券返回结果======================="+islimit);
		return islimit;
	}
	
	@Override
	public Map<String, Object> queryOrderItemAmountInfo(Long orderId) {
		LOG.info("============ queryOrderItemAmountInfo orderId = {}", orderId);
		try {
			List<OrderItemRefundVo> refundList = new ArrayList<>();
			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			List<OrdOrderItem> orderItemList = order.getOrderItemList();
			for (OrdOrderItem orderItem : orderItemList) {
				//获取子订单退款信息
				Map<String, Object> ordItemRefundInfo = queryOrdItemRefundInfoForTicket(orderItem.getOrderItemId(), orderItem.getQuantity());
				if (isRefundRuleAvailable(ordItemRefundInfo)) {//处理退款规则可用子订单
					setOrderItemRefundInfo(orderItem, ordItemRefundInfo, refundList);
				} else if (isInsurance(orderItem)) {//处理保险子订单
					setInsuranceRefundInfo(orderItem, ordItemRefundInfo, refundList);
				} else if (isInvoiceExpress(orderItem)) {
					//处理发票快递子订单
					setInvoiceRefundInfo(orderItem, ordItemRefundInfo, refundList);
				}else {//处理无法计算退款的子订单
					return getEmptyOrderItemAmountInfo(false, orderId, "包含无法计算退款的子订单");
				}
			}
			return validateAndGetOrderItemAmountInfo(refundList, orderId, order.getActualAmount());
		} catch (Exception e) {
			LOG.error("============ queryOrderItemAmountInfo orderId = " + orderId + ", error :", e);
			return getEmptyOrderItemAmountInfo(true, orderId, "系统错误," + e.getMessage());
		}
	}
		
	/**
	 * 是否为保险
	 *
	 * @param orderItem
	 * @return
	 */
	private boolean isInsurance(OrdOrderItem orderItem) {
		return orderItem.hasCategory(BIZ_CATEGORY_TYPE.category_insurance);
	}

	/**
	 * 是否为发票快递
	 *
	 * @param orderItem
	 * @return
	 */
	private boolean isInvoiceExpress(OrdOrderItem orderItem) {
		return orderItem.hasCategory(BIZ_CATEGORY_TYPE.category_other) && SuppSettlementEntities.CONTRACT_SETTLE_BU.CUSTOMER_SERVICE_CENTER_BU.getCode().equals(orderItem.getBuCode());
	}

	/**
	 * 是否扣款保险
	 *
	 * @param orderItem
	 * @return
	 */
//	private boolean isDeductInsurance(OrdOrderItem orderItem) {
//		Date nowDate = new Date();
//		//游玩日期
//		Date visitDate = orderItem.getVisitTime();
//		//如果过了游玩日期,保险也要扣款
//		return DateUtils.addMinutes(visitDate, 1439).before(nowDate);
//	}

	/**
	 * 是否退款规则可用
	 */
	private boolean isRefundRuleAvailable(Map<String, Object> ordItemRefundInfo) {
		if (ordItemRefundInfo != null && !ordItemRefundInfo.isEmpty()) {
			String refundRule = (String) ordItemRefundInfo.get("refundRule");
			if (StringUtils.isNotBlank(refundRule)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 设置保险退款信息
	 */
	private void setInsuranceRefundInfo(OrdOrderItem orderItem, Map<String, Object> ordItemRefundInfo, List<OrderItemRefundVo> refundList) {
		if (ordItemRefundInfo != null && !ordItemRefundInfo.isEmpty()) {
			Long refundAmount = (Long) ordItemRefundInfo.get("refundAmount");
			Long lossAmount = (Long) ordItemRefundInfo.get("lossAmount");
			Long refundOughtPrice = (Long) ordItemRefundInfo.get("refundOughtPrice");
			String calculateRule = (String) ordItemRefundInfo.get("calculateRule");
			if (lossAmount != null && lossAmount >= 0) {
				OrderItemRefundVo refundItem = new OrderItemRefundVo();
				BeanUtils.copyProperties(orderItem, refundItem);
				Date visitTime = orderItem.getVisitTime();
				refundItem.setDeductDate(visitTime);
				Long quantity = orderItem.getQuantity();
				refundItem.setDeductQuantity(quantity);
				refundItem.setDeductAmount(lossAmount);
				String yuanStr = PriceUtil.trans2YuanStr(lossAmount);
				refundItem.setDeductDetailInfo(DateUtil.formatDate(visitTime, "MM月dd日 HH:mm") + "后退款，共扣除" + yuanStr + "元");
				refundItem.setRefundAmount(refundAmount);
				refundItem.setRefundQuantity(quantity);
				refundItem.setRefundActualAmount(refundOughtPrice);
				refundItem.setRefundRuleDetail(calculateRule);
				refundList.add(refundItem);
			}
		}
	}

	/**
	 * 设置发票快递退款信息
	 */
	private void setInvoiceRefundInfo(OrdOrderItem orderItem, Map<String, Object> ordItemRefundInfo, List<OrderItemRefundVo> refundList) {
		if (ordItemRefundInfo != null && !ordItemRefundInfo.isEmpty()) {
			Long refundAmount = (Long) ordItemRefundInfo.get("refundAmount");
			Long lossAmount = (Long) ordItemRefundInfo.get("lossAmount");
			Long refundOughtPrice = (Long) ordItemRefundInfo.get("refundOughtPrice");
			String calculateRule = (String) ordItemRefundInfo.get("calculateRule");
			if (lossAmount != null && lossAmount >= 0) {
				OrderItemRefundVo refundItem = new OrderItemRefundVo();
				BeanUtils.copyProperties(orderItem, refundItem);
				refundItem.setDeductDate(null);
				refundItem.setDeductQuantity(orderItem.getQuantity());
				refundItem.setDeductAmount(lossAmount);
				String yuanStr = PriceUtil.trans2YuanStr(lossAmount);
				refundItem.setDeductDetailInfo("订单未寄出退款,共扣除" + yuanStr + "元");
				Long quantity = orderItem.getQuantity();
				refundItem.setDeductQuantity(quantity);
				refundItem.setDeductAmount(lossAmount);
				refundItem.setRefundAmount(refundAmount);
				refundItem.setRefundQuantity(quantity);
				refundItem.setRefundActualAmount(refundOughtPrice);
				refundItem.setRefundRuleDetail(calculateRule);
				refundList.add(refundItem);
			}
		}
	}

	/**
	 * 设置子订单退款信息
	 */
	private void setOrderItemRefundInfo(OrdOrderItem orderItem, Map<String, Object> ordItemRefundInfo, List<OrderItemRefundVo> refundList) {
		if (ordItemRefundInfo != null && !ordItemRefundInfo.isEmpty()) {
			String refundRule = (String) ordItemRefundInfo.get("refundRule");
			SuppGoodsRefundVO suppGoodsRefundVO = JSON.parseObject(refundRule, SuppGoodsRefundVO.class);
			Long refundAmount = (Long) ordItemRefundInfo.get("refundAmount");
			Long lossAmount = (Long) ordItemRefundInfo.get("lossAmount");
			Long refundOughtPrice = (Long) ordItemRefundInfo.get("refundOughtPrice");
			String calculateRule = (String) ordItemRefundInfo.get("calculateRule");
			//处理有退款规则的子订单
			if (suppGoodsRefundVO != null && lossAmount != null && lossAmount >= 0) {
				OrderItemRefundVo refundItem = new OrderItemRefundVo();
				BeanUtils.copyProperties(orderItem, refundItem);
				String yuanStr = PriceUtil.trans2YuanStr(lossAmount);
				if (SuppGoodsRefundVO.CANCEL_TIME_TYPE.OTHER.getCode().equals(suppGoodsRefundVO.getCancelTimeType())) {
					refundItem.setDeductDate(null);
					refundItem.setDeductDetailInfo("订单未使用退款,共扣除" + yuanStr + "元");
				} else {//按照时间区间阶梯退
					Date refundDate = suppGoodsRefundVO.getRefundDate();
					refundItem.setDeductDate(refundDate);
					refundItem.setDeductDetailInfo(DateUtil.formatDate(refundDate, "MM月dd日 HH:mm") + "前退款,共扣除" + yuanStr + "元");
				}
				Long quantity = orderItem.getQuantity();
				refundItem.setDeductQuantity(quantity);
				refundItem.setDeductAmount(lossAmount);
				refundItem.setRefundAmount(refundAmount);
				refundItem.setRefundQuantity(quantity);
				refundItem.setRefundActualAmount(refundOughtPrice);
				refundItem.setRefundRuleDetail(calculateRule);
				refundItem.setRefundRule(refundRule);
				refundList.add(refundItem);
			}
		}
	}

	/**
	 * 返回空的子订单金额信息
	 *
	 * @return
	 */
	private Map<String, Object> getEmptyOrderItemAmountInfo(Boolean isError, Long orderId, String message) {
		Map<String, Object> map = new HashMap<>();
		map.put("IS_ERROR", isError);
		map.put("TOTAL_DEDUCT_AMOUNT", null);
		map.put("TOTAL_REFUND_AMOUNT", null);
		map.put("ITEM_DEDUCT_INFO", new ArrayList<OrderItemRefundVo>());
		LOG.info("============ getEmptyOrderItemAmountInfo orderId = {} , map = {}, msg = {}", new Object[]{orderId, map.toString(), message});
		return map;
	}

	/**
	 * 返回完整的子订单金额信息
	 *
	 * @return
	 */
	private Map<String, Object> validateAndGetOrderItemAmountInfo(List<OrderItemRefundVo> refundList, Long orderId, Long totalActualAmount) {
		//子订单退款信息为空
		if (refundList.isEmpty()) {
			return getEmptyOrderItemAmountInfo(false, orderId, "无子订单退款信息");
		}
		//子订单总扣款金额
		Long totalDeductAmount = 0L;
		//子订单总退款金额
		Long totalRefundAmount = 0L;
		for (OrderItemRefundVo itemRefundVo : refundList) {
			Long deductAmount = itemRefundVo.getDeductAmount();
			Long refundAmount = itemRefundVo.getRefundAmount();
			if (deductAmount == null || refundAmount == null) {
				return getEmptyOrderItemAmountInfo(false, orderId, "子订单 " + itemRefundVo.getOrderItemId() + " 退款或扣款金额为空");
			}
			totalDeductAmount += deductAmount;
			totalRefundAmount += refundAmount;
		}
		//子订单总扣款金额大于订单实付金额
		if (totalDeductAmount > totalActualAmount) {
			return getEmptyOrderItemAmountInfo(false, orderId, "子订单总扣款金额大于订单实付金额");
		}
		Map<String, Object> map = new HashMap<>();
		map.put("IS_ERROR", false);
		map.put("TOTAL_DEDUCT_AMOUNT", totalDeductAmount);
		map.put("TOTAL_REFUND_AMOUNT", totalRefundAmount);
		map.put("ITEM_DEDUCT_INFO", refundList);
		LOG.info("============ validateAndGetOrderItemAmountInfo orderId = {} , map = {}", orderId, map.toString());
		return map;
	}




	@Override
	public OrdFlightTicketStatus queryFlightTicketStatus(Long orderItemId) {
		if(orderItemId == null) {
			return null;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", orderItemId);
		List<OrdFlightTicketStatus> ordFlightTicketStatusList = ordFlightTicketStatusService.findByCondition(params);
		if(ordFlightTicketStatusList != null && ordFlightTicketStatusList.size() > 0) {
			return ordFlightTicketStatusList.get(0);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public PermUser getResourceApprover(Long objectId) {
		//资源审核人
		return orderResponsibleService.getResourceApprover(objectId, "ORDER_ITEM");
	}

	/**
	 * 根据商品ID和游玩日期获取马戏票场次信息
	 * @param suppGoodsId
	 * @param visitTime
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResultHandleT<List<CircusActInfo>> queryCircusActTimes(Long suppGoodsId, Date visitTime) throws Exception {
		LOG.info("queryCircusActTimes--suppGoodsId:" + suppGoodsId + ",visitTime:" + visitTime);
		ResultHandleT<List<CircusActInfo>> handle = new ResultHandleT<List<CircusActInfo>>();
		if(suppGoodsId == null || visitTime == null) {
			handle.setMsg("参数无效， suppGoodsId:" + suppGoodsId + ", visitTime:" + visitTime);
			return handle;
		}
		String key = SuppGoodsCircusUtils.getCircusTimesCacheKey(suppGoodsId, visitTime);
		//先从缓存获取
		if(MemcachedUtil.getInstance().keyExists(key)) {
			LOG.info("key exist");
			handle.setReturnContent((List<CircusActInfo>)MemcachedUtil.getInstance().get(key));
			return handle;
		}

		List<String> actTimes = supplierStockCheckService.getActTimes(suppGoodsId, visitTime);
		List<CircusActInfo> actInfoList = new ArrayList<CircusActInfo>();
		StringBuffer actSB = new StringBuffer();
		if(CollectionUtils.isNotEmpty(actTimes)) {
			for(String act : actTimes) {
				String[] actArray = act.split(",");
				if(actArray == null || actArray.length < 2) {
					LOG.error("数据异常, act:" + act);
					continue;
				}
				if(actSB.length() > 0) {
					actSB.append(";");
				}
				CircusActInfo actInfo = new CircusActInfo();
				actInfoList.add(actInfo);
				actInfo.setCircusActId(actArray[0]);
				actInfo.setCircusActStartTime(actArray[1]);
                actSB.append(actArray[0] + "," + actArray[1]);
                if(actArray.length > 2){
                    actInfo.setCircusActEndTime(actArray[2]);
                    actSB.append("," + actArray[2]);
                }
			}
		}

		//将场次信息推送给分销
		if(false) {
			petMessageService.sendPerformanceTicketMessage(suppGoodsId,
					ComPush.OBJECT_TYPE.PERFORMANCE_TICKET.name(),
					ComPush.PUSH_CONTENT.SUPP_GOODS_PERFORMANCE_TIMES.name(),
					DateUtil.formatSimpleDate(visitTime) + "|" + actSB.toString());
		}

		//放入缓存
		MemcachedUtil.getInstance().set(key, 180, actInfoList);

		handle.setReturnContent(actInfoList);

		return handle;
	}


	/**
	 * 根据商品ID和游玩日期&场次信息，获取该场次的库存
	 * @param suppGoodsId
	 * @param visitTime
	 * @param actId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResultHandleT<Long> queryCircusActStock(Long suppGoodsId, Date visitTime,
			String actId) throws Exception {
		ResultHandleT<Long> handle = new ResultHandleT<Long>();
		if(suppGoodsId == null || visitTime == null || StringUtils.isBlank(actId)) {
			handle.setMsg("参数无效， suppGoodsId:" + suppGoodsId + ", visitTime:" + visitTime + ", actId:" + actId);
			return handle;
		}
		String key = SuppGoodsCircusUtils.getCircusStockCacheKey(suppGoodsId, visitTime, actId);
		//先从缓存获取
		if(MemcachedUtil.getInstance().keyExists(key)) {
			handle.setReturnContent((Long) MemcachedUtil.getInstance().get(key));
			return handle;
		}

		Long actStock = supplierStockCheckService.getActCount(suppGoodsId, visitTime, actId);
		//放入缓存
		MemcachedUtil.getInstance().set(key, 300, actStock);

		handle.setReturnContent(actStock);

		//刷新分销缓存
		/*
		String detailKey = SuppGoodsCircusUtils.getCircusTimeDetailCacheKey(suppGoodsId, visitTime);
		if(MemcachedUtil.getInstance().keyExists(detailKey)) {
			List<SuppGoodsCircusDetail> suppGoodsCircusDetails = (List<SuppGoodsCircusDetail>)MemcachedUtil.getInstance().get(detailKey);
			if(CollectionUtils.isNotEmpty(suppGoodsCircusDetails)) {
				for(SuppGoodsCircusDetail suppGoodsCircusDetail : suppGoodsCircusDetails) {
					if(String.valueOf(suppGoodsCircusDetail.getActId()).equals(actId)) {
						suppGoodsCircusDetail.setStock(actStock);
					}
				}
			}

			MemcachedUtil.getInstance().replace(detailKey, 24 * 60 * 60, suppGoodsCircusDetails);
		}

		//更新数据库
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("stock", actStock);
		params.put("suppGoodsId", suppGoodsId);
		params.put("visitTime", visitTime);
		params.put("actId", Long.valueOf(actId));

		suppGoodsCircusDetailClientService.updateStockByCondition(params);
		*/

		return handle;
	}


	@Override
	public ResultHandleT<List<SuppGoodsCircusDetail>> queryPeriodCircusInfo(
			Long suppGoodsId) {
		return queryPeriodCircusInfo(suppGoodsId, new Date(), DateUtil.getDateAfterMonths(new Date(), 1));
	}


	@Override
	public ResultHandleT<List<SuppGoodsCircusDetail>> queryPeriodCircusInfo(
			Long suppGoodsId, Date fromDate, Date toDate) {
		ResultHandleT<List<SuppGoodsCircusDetail>> handle = new ResultHandleT<List<SuppGoodsCircusDetail>>();
		if(suppGoodsId == null || fromDate == null || fromDate == null) {
			handle.setMsg("参数无效， suppGoodsId:" + suppGoodsId + ", fromDate:" + fromDate + ", fromDate:" + fromDate);
			return handle;
		}
		List<SuppGoodsCircusDetail> suppGoodsCircusDetails = new ArrayList<SuppGoodsCircusDetail>();
		Date date = fromDate;
		for (int day = 0; !date.after(toDate); day++) {
			date = DateUtil.toYMDDate(DateUtil
					.getDateAfterDays(fromDate, day));
			String key = SuppGoodsCircusUtils.getCircusTimeDetailCacheKey(suppGoodsId, date);
			if(MemcachedUtil.getInstance().keyExists(key)) {
				suppGoodsCircusDetails.addAll((List<SuppGoodsCircusDetail>) MemcachedUtil.getInstance().get(key));
			} else {
				Date tDate = fromDate;
				for (int d = 0; !tDate.after(toDate); d++) {
					tDate = DateUtil.toYMDDate(DateUtil
							.getDateAfterDays(fromDate, d));
					String tKey = SuppGoodsCircusUtils.getCircusTimeDetailCacheKey(suppGoodsId, tDate);
					if(MemcachedUtil.getInstance().keyExists(tKey)) {
						MemcachedUtil.getInstance().remove(tKey);
					}
				}

				Map<String, Object> params = new HashMap<String, Object>();
				params.put("suppGoodsId", suppGoodsId);
				params.put("startVisitTime", fromDate);
				params.put("endVisitTime", toDate);

				List<SuppGoodsCircusDetail> suppGoodsCircusDetailList = suppGoodsCircusDetailClientService.queryCircusByCondition(params);
				if(CollectionUtils.isNotEmpty(suppGoodsCircusDetailList)) {
					for(SuppGoodsCircusDetail suppGoodsCircusDetail : suppGoodsCircusDetailList) {
						String circusDetailKey = SuppGoodsCircusUtils.getCircusTimeDetailCacheKey(suppGoodsId, suppGoodsCircusDetail.getVisitTime());
						List<SuppGoodsCircusDetail> cachedDetailList = (List<SuppGoodsCircusDetail>)MemcachedUtil.getInstance().get(circusDetailKey);
						if(cachedDetailList == null) {
							cachedDetailList = new ArrayList<SuppGoodsCircusDetail>();
						}
						cachedDetailList.add(suppGoodsCircusDetail);
						MemcachedUtil.getInstance().set(circusDetailKey, cachedDetailList);
					}
				}

				handle.setReturnContent(suppGoodsCircusDetailList);
				return handle;
			}
		}

		handle.setReturnContent(suppGoodsCircusDetails);
		return handle;
	}


	/**
	 * 同步订单权限
	 * @param order
	 */
	@Async
	private void synManagerIdPerm(OrdOrder order) {
		if (LOG.isDebugEnabled())
			LOG.debug("start method<synManagerIdPerm>");
		if(null ==order || null ==order.getOrderId()) return;
		try{
			StringBuilder sBuilder =new StringBuilder(80);
			List<Long> keyList = new ArrayList<Long>();
			//查询订单打包(自主打包)
			if(null != order.getOrdOrderPack() && ProdProduct.PACKAGETYPE.LVMAMA.getCode().equals(order.getOrdOrderPack().getOwnPack())){
				//主订单(打包产品经理)
				appendManagerId(sBuilder, keyList, order.getManagerId());
			}
			//子订单相关经理
			if(CollectionUtils.isNotEmpty(order.getOrderItemList())){
				List<Long> productIdList =new ArrayList<Long>();
				for(OrdOrderItem item :order.getOrderItemList()){
					if(null == item) continue;
					//子订单(供应商商品经理)
					appendManagerId(sBuilder, keyList, item.getManagerId());
					if(null != item.getProductId()) productIdList.add(item.getProductId());
				}
				Map<String, Object> params =new HashMap<String, Object>();
				//子订单所属产品经理ID
				params.put("productIds", productIdList.toArray(new Long[productIdList.size()]));
				ResultHandleT<List<Long>> result =productClientService.findProdProducManagerIdList(params);
				if(result.isSuccess()){
					List<Long> managerIdList =result.getReturnContent();
					if(CollectionUtils.isNotEmpty(managerIdList)){
						for(Long managerId :managerIdList){
							appendManagerId(sBuilder, keyList, managerId);
						}
					}
				}

			}
			if(sBuilder.length() > 0) sBuilder.insert(0, Constant.COMMA);
			order.setManagerIdPerm(sBuilder.toString());

			iOrdOrderService.updateManagerIdPerm(order);
		}catch(Exception e){
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
	}

	/**
	 * 追加产品经理
	 * @param sBuilder
	 * @param keyList
	 * @param managerId
	 */
	private void appendManagerId(StringBuilder sBuilder, List<Long> keyList, Long managerId){
		if(null ==managerId) return;
		if(null == keyList || keyList.contains(managerId)) return;
		sBuilder.append(managerId).append(Constant.COMMA);
		keyList.add(managerId);
	}

	@Override
	public List<OrdItemFreebiesRelation> queryFreebieRelByItemId(
			Long orderId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", orderId);
		return ordItemFreebieService.queryFreebieListByItem(params);
	}


	@Override
	public ResultHandleT<Long> queryDistCircusActStock(Long suppGoodsId,
			Date visitTime, String actId, boolean byCache) throws Exception {
		ResultHandleT<Long> handle = new ResultHandleT<Long>();
		if(suppGoodsId == null || visitTime == null || StringUtils.isBlank(actId)) {
			handle.setMsg("参数无效， suppGoodsId:" + suppGoodsId + ", visitTime:" + visitTime + ", actId:" + actId);
			return handle;
		}
		String key = SuppGoodsCircusUtils.getCircusStockCacheKey(suppGoodsId, visitTime, actId);
		if(byCache) {
			//先从缓存获取
			if(MemcachedUtil.getInstance().keyExists(key)) {
				handle.setReturnContent((Long) MemcachedUtil.getInstance().get(key));
				return handle;
			}
		}

		Long actStock = supplierStockCheckService.getActCount(suppGoodsId, visitTime, actId);
		if(byCache) {
			//放入缓存
			MemcachedUtil.getInstance().set(key, 300, actStock);
		}

		handle.setReturnContent(actStock);

		return handle;
	}


	@SuppressWarnings("unchecked")
	@Override
	public ResultHandleT<List<CircusActInfo>> queryDistCircusActTimes(
			Long suppGoodsId, Date visitTime, boolean byCache) throws Exception {
		LOG.info("queryDistCircusActTimes--suppGoodsId:" + suppGoodsId + ",visitTime:" + visitTime + ",byCache:" + byCache);
		ResultHandleT<List<CircusActInfo>> handle = new ResultHandleT<List<CircusActInfo>>();
		if(suppGoodsId == null || visitTime == null) {
			handle.setMsg("参数无效， suppGoodsId:" + suppGoodsId + ", visitTime:" + visitTime);
			return handle;
		}
		String key = SuppGoodsCircusUtils.getCircusTimesCacheKey(suppGoodsId, visitTime);
		if(byCache) {
			//先从缓存获取
			if(MemcachedUtil.getInstance().keyExists(key)) {
				List<CircusActInfo> infoList = (List<CircusActInfo>)MemcachedUtil.getInstance().get(key);
				LOG.info("key:" + key + ",value:" + infoList);
				handle.setReturnContent(infoList);
				return handle;
			}
		}

		List<String> actTimes = supplierStockCheckService.getActTimes(suppGoodsId, visitTime);
		List<CircusActInfo> actInfoList = new ArrayList<CircusActInfo>();
		if(CollectionUtils.isNotEmpty(actTimes)) {
			LOG.info("actTimes is not empty");
			for(String act : actTimes) {
				String[] actArray = act.split(",");
				if(actArray == null || actArray.length < 2) {
					LOG.error("数据异常, act:" + act);
					continue;
				}
				CircusActInfo actInfo = new CircusActInfo();
				actInfoList.add(actInfo);
				actInfo.setCircusActId(actArray[0]);
				actInfo.setCircusActStartTime(actArray[1]);
				if(actArray.length > 2) {
					actInfo.setCircusActEndTime(actArray[2]);
				}
			}
		}

		if(byCache) {
			//放入缓存
			MemcachedUtil.getInstance().set(key, 180, actInfoList);
		}

		handle.setReturnContent(actInfoList);

		return handle;
	}

	@Override
	public List<OrdOrderPack> findOrdOrderPackList(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return iOrdOrderPackService.findOrdOrderPackList(params);
	}

	@Override
	public int isCancelOrderWithHotelSupp(Long orderId) {
		OrdOrder ordOrder = this.queryOrdorderByOrderId(orderId);
		if (ordOrder == null) {
			LOG.info("get order by orderId is null,please check data,orderId:" + orderId);
			return -1;
		}
		List<OrdOrderItem> ordOrderItemList = ordOrder.getOrderItemList();
		for (OrdOrderItem ordOrderItem : ordOrderItemList) {
			/**
			 * 判断是否为开放平台供应商，此处handle为supplier_id，supplier_id=5400为港捷旅，supplier_id=1为艺龙
			 * 其他情况则为开放供应商
			 */
			boolean isSupplierApi = ordOrderItem.hasSupplierApi();//判断是否对接
			try {
				if (isSupplierApi) {//供应商对接
					return vstOrderRefundServiceVstSupp.isCancelOrderWithSupp(ordOrder);

				} else {//vst_back ebooking 供应商系统，非对接供应商
					return vstOrderRefundServiceVstBack.isCancelOrderWithSupp(ordOrder);
				}
			} catch (Exception e) {
				LOG.error(""+e.getMessage());
			}
		}
		return -1;
	}


	@Override
	public List<OrdMulPriceRate> findOrdMulPriceRateList(
			Map<String, Object> params) {
		return ordMulPriceRateService.findOrdMulPriceRateList(params);

	}
	
	public List<OrdOrderAmountItem> findOrderAmountItemList(Map<String, Object> paramsMap) {
        return ordOrderAmountItemService.findOrderAmountItemList(paramsMap);
    }

    /**
	 * 设置订单产品公告快照
	 * @param order
	 */
	public void setOrderNotice(OrdOrder order)
	{
		LOG.info("---start set notice in createOrder----");
		try{
			LOG.info("The orderId is:" + order.getOrderId());
			if(order.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId())
					||order.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId())
					||order.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId())
					||order.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId())
					||order.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route.getCategoryId())

					||order.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId())
					||order.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId())
					||order.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId())
					||order.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId())
					||order.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.getCategoryId()))

			{
				OrdOrderNotice ordNotice = new OrdOrderNotice();
				String startTimeStr = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("startTimeStr", startTimeStr);
				params.put("endTimeStr", startTimeStr);
				params.put("cancelFlag", "Y");
				LOG.info("---start set orderNotice startTimeStr and endTimeStr is----"+startTimeStr);
				Long productId = order.getProductId();
				LOG.info("---start set orderNotice productId is----"+productId);
				if(null != productId)
				{
					params.put("productId", productId);
					List<ProdProductNotice> noticeList= prodProductNoticeClientService.findProductNoticeList_desc(params);
					if(CollectionUtils.isNotEmpty(noticeList)){
						LOG.info("setOrderNotice noticeList size" + noticeList.size());

						for(ProdProductNotice prodNotice : noticeList){
						ordNotice.setContent(prodNotice.getContent());
						ordNotice.setNoticeId(prodNotice.getNoticeId());
						ordNotice.setOrdOrderId(order.getOrderId());
						ordNotice.setProductId(prodNotice.getProductId());
						ordNotice.setStartTime(prodNotice.getStartTime());
						ordNotice.setEndTime(prodNotice.getEndTime());
						ordNotice.setNoticeType(prodNotice.getNoticeType());
						ordProductNotice.insert(ordNotice);
					  }
					}else{
						LOG.info("---setOrderNotice noticeList size is 0");
					}
				}

			}
		}catch(Exception e){
			LOG.error("create Order  at notice exception and orderId is "+order.getOrderId(), e);
		}

	}

    public  boolean isShanghaiDisneyOrder(OrdOrder order){
    	LOG.info("check ShanghaiDisneyOrder-==============" + order.getOrderId());
		boolean flag=false;
		 List<OrdOrderItem> itemList=order.getOrderItemList();
		 for(OrdOrderItem item:itemList){
			 if(DisneyUtils.isDisney(item)){
				 flag=true;
				 break;
			 }
		 }
		 LOG.info("check ShanghaiDisneyOrder-==============" + order.getOrderId()+"success+=========="+flag);
		return flag;

	}

	@Override
	public String getCodeImagePdfFlag(Long suppGoodsId) {
//		String pdfFlagKey = "KEY_CODEIMAGE_PDF_FLAG_" + suppGoodsId;
//		String pdfFlag = MemcachedUtil.getInstance().get(pdfFlagKey);
//        if(pdfFlag != null){
//        	return pdfFlag;
//        }
//
//        pdfFlag = "N";
//		PassProvider provider = supplierOrderOperator.getProductServiceInfo(suppGoodsId);
//		LOG.info("getCodeImagePdfFlag, suppGoodsId:" + suppGoodsId + ", provider:" + provider);
//		if(provider != null) {
//			LOG.info("providerName:" + provider.getProviderName() + ".");
//		}
//		if (provider != null
//				&& ("上海迪士尼".equals(provider.getProviderName()))) {
//			pdfFlag = "Y";
//		}
//
//		MemcachedUtil.getInstance().set(pdfFlagKey, 300, pdfFlag);
//
//		return pdfFlag;
		// 上海迪士尼去掉PDF，注释原代码，统一返回N
		return "N";
	}


	@Override
	public List<OrdOrderItem> queryOrderItemByOrderId(Long orderId) {
		// TODO Auto-generated method stub
		return this.orderUpdateService.queryOrderItemByOrderId(orderId);
	}

	@Override
	public List<O2oOrder> queryO2oOrder(Map<String, Object> paramsMap) {
		return o2oOrderService.queryO2oOrder(paramsMap);
	}
	
	@Override
	public List<O2oOrder> findO2oOrderList(Map paramsMap) {
		return o2oOrderService.findO2oOrderList(paramsMap);
	}
	
	
	@Override
	public List<O2oOrder> queryForListForReport(Map<String, Object> paramsMap) {
		return o2oOrderService.queryForListForReport(paramsMap);
	}
	
	@Override
	public Long getCountByProperty(Map<String, Object> paramsMap) {
		return o2oOrderService.getCountByProperty(paramsMap);
	}

	@Override
	public boolean updateSendNoticeRegiment(Long orderId, String email,
			String loginUserId) {
		return ordAdditionStatusService.updateSendNoticeRegiment(orderId, email, loginUserId);
	}

	public List<OrdSmsSend> findOrdSmsSendList(Map<String, Object> params){
		return orderSendSmsService.findOrdSmsSendList(params);
	}

	public int findOrdSmsSendCount(Map<String, Object> params){
		return orderSendSmsService.findOrdSmsSendCount(params);
	}

	@Override
	public Map<String, Object> queryOrderStatus(Long orderId) {
		OrdOrder order = queryOrdorderByOrderId(orderId);
		//判断订单状态已支付或取消
		String viewOrderStatus = order.getViewOrderStatus();
		//判断凭证确认状态
		String certConfirmStatus = order.getCertConfirmStatus();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("viewOrderStatus", viewOrderStatus);
		map.put("certConfirmStatus", certConfirmStatus);
		//如果订单状态为取消
		if(viewOrderStatus.equalsIgnoreCase(OrderEnum.ORDER_VIEW_STATUS.CANCEL.name())){
			Long refunds = 0L;
			//退款
			List<OrdRefundment> ordRefundments = orderRefundmentService.findOrderRefundmentByOrderIdStatus(orderId, Constant.REFUNDMENT_STATUS.REFUNDED.name());
			LOG.info("订单OrderId:===="+orderId+"退款单记录===="+ordRefundments.size());
			for (OrdRefundment ordRefundment : ordRefundments) {
				//退款总金额
				if(Constant.REFUND_TYPE.ORDER_REFUNDED.name().equals(ordRefundment.getRefundType())){
					refunds += ordRefundment.getAmount();
				}
			}
			LOG.info("订单OrderId:===="+orderId+"退款单总金额refunds：===="+refunds);
			if(refunds>0){
				map.put("refundmentStatus", Constant.REFUNDMENT_STATUS.REFUNDED.name());
			}
			if(ordRefundments==null || ordRefundments.size()<=0){
				map.put("refundmentStatus", "NOORDEREFUNDMENT");
			}

		}

		return map;
	}

	public void saveOrderAttachment(OrderAttachment orderAttachment,String operatorName,String memo){
		orderAttachmentService.saveOrderAttachment(orderAttachment, operatorName, memo);
	}

	public void saveOrderAttachment(OrderAttachment orderAttachment,ComLog comLog){
		orderAttachmentService.saveOrderAttachment(orderAttachment, comLog);
	}

	/**
	 * 设置资源保留时长
	 * @param ordOrder
	 * @param ordOrderItem
	 */
	public void setResouceKeepTime(OrdOrder ordOrder,OrdOrderItem ordOrderItem){
		if(ordOrder == null || ordOrderItem == null){
			LOG.error("ordOrder == null or ordOrderItem == null");
			return;
		}
		if(ordOrderItem.isApiFlightTicket()&&BU_NAME.LOCAL_BU.getCode().equals(ordOrder.getBuCode())){
			Map<String,Object> paraMap1 = new HashMap<String, Object>();
			paraMap1.put("orderItemId", ordOrderItem.getOrderItemId());
			paraMap1.put("statusCode", OrderEnum.ORD_FLIGHT_TICKET_STATUS.LOCK_SUCCESS.name());
			List<OrdFlightTicketStatus> OrdFlightTicketStatus = ordFlightTicketStatusService.findByCondition(paraMap1);
			if(null!=OrdFlightTicketStatus&&OrdFlightTicketStatus.size()>0){
				return;
			}
		}
		LOG.info("setResouceKeepTime ordOrderId="+ordOrder.getOrderId()+" ordOrderItemId="+ordOrderItem.getOrderItemId());
		BizSystemConfigure bizSystemConfigure = bizSystemConfigureClientRemote.getCurrentSysConfigureByKey("VST_ORDER-AIRTICKET_ORDERITEM_KEEPTIME");
		//设置资源保留时长
		Date retentionTime = DateUtil.getDateAfterMinutes(60);
		if(bizSystemConfigure != null && StringUtil.isNotEmptyString(bizSystemConfigure.getConfigureValue()) && StringUtil.isNumber(bizSystemConfigure.getConfigureValue())){
			String dbTime = bizSystemConfigure.getConfigureValue();
			LOG.info("setResouceKeepTime num defalut=60 dbTime="+dbTime);
			retentionTime = DateUtil.getDateAfterMinutes(Long.parseLong(dbTime));
		}
		if(judgmentCondition(ordOrder)){
			Date maxLastCancelTime = null;
			for (OrdOrderItem orderItem : ordOrder.getOrderItemList()) {
				Date lastCancelTime = orderItem.getLastCancelTime();
				if (lastCancelTime != null) {
					if (maxLastCancelTime == null) {
						maxLastCancelTime = lastCancelTime;
					} else {
						if (lastCancelTime.before(maxLastCancelTime)) {
							maxLastCancelTime = lastCancelTime;
						}
					}
				}
			}
			if(maxLastCancelTime != null && maxLastCancelTime.before(retentionTime)){
				retentionTime = maxLastCancelTime;
			}
		}
		String currRetentionTimeStr = ordOrderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name());
		Date currRetentionTime = null;
		if(StringUtils.isNotBlank(currRetentionTimeStr)){
			currRetentionTime = DateUtil.toDate(currRetentionTimeStr, DateUtil.HHMMSS_DATE_FORMAT);
		}
		String resourceRetentionTime = DateUtil.formatDate(retentionTime, DateUtil.HHMMSS_DATE_FORMAT);
		LOG.info("setResouceKeepTime compare currRetentionTime="+currRetentionTime+" retentionTime="+retentionTime);
		if(currRetentionTime == null || retentionTime.before(currRetentionTime)) {
			LOG.info("setResouceKeepTime retentionTime="+retentionTime);
			ordOrderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name(), resourceRetentionTime);
			if(ordOrderItem.isApiFlightTicket()){
				Date visitdate=ordOrder.getVisitTime();
				Date dates=new Date("2016/09/29");
				Date dates1=new Date("2016/10/8");
				if(dates.before(visitdate)&&dates1.after(visitdate)){
					Date nowdate=new Date();
					Date tempDate=DateUtil.DsDay_Minute(nowdate, 30);
					ordOrderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name(), DateUtil.formatDate(tempDate, DateUtil.HHMMSS_DATE_FORMAT));
					ordOrder.setWaitPaymentTime(tempDate);
					if(retentionTime != null && retentionTime.before(tempDate)){						
						ordOrderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name(), DateUtil.formatDate(retentionTime, DateUtil.HHMMSS_DATE_FORMAT));
						ordOrder.setWaitPaymentTime(retentionTime);
					}
				}
			}
			this.updateOrderItem(ordOrderItem);
			//设置支付等待时间
			if(ordOrder.getWaitPaymentTime() == null || retentionTime.before(ordOrder.getWaitPaymentTime())) {
				ordOrder.setWaitPaymentTime(retentionTime);
			}
			this.updateOrdOrder(ordOrder);
		}
	}
	
	/**
	 * 判断前提条件
	 * 1、国内bu
	 * 2、包含机票和酒店
	 * 
	 * @param order
	 * @return
	 */
	private boolean judgmentCondition(OrdOrder order){
		boolean flag = false;
		if(order != null && CommEnumSet.BU_NAME.LOCAL_BU.getCode().equalsIgnoreCase(order.getBuCode())){
			List<Long> categoryIds = Lists.newArrayList();
			if(CollectionUtils.isNotEmpty(order.getOrderItemList())){
				for(OrdOrderItem orderItem : order.getOrderItemList()){
					if(orderItem != null){
						categoryIds.add(orderItem.getCategoryId());
					}
				}
			}
			if(CollectionUtils.isNotEmpty(categoryIds)){
				if(categoryIds.contains(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId())
						&& categoryIds.contains(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId())){
					flag = true;
				}
			}
		}
		return flag;
	}
	
	@Override
	public void updateOrderPerson(OrdPerson ordPerson) {
		orderUpdateService.updateOrderPerson(ordPerson);
	}

	public Object[] reservationSupplierOrder(OrdOrder ordOrder){

		Object[] array = new Object[2];
		List<SuppOrderResult> results = null;
	        Boolean result= Boolean.TRUE;
	        String errMsg = "";
	        array[0] = result;
	        array[1] = errMsg;
	        try{
	            results = supplierOrderService.reservationSupplierOrder(ordOrder);
	            for (SuppOrderResult suppOrderResult : results) {
					if(suppOrderResult == null) {
						LOG.info("远程调用返回结果存在空记录");
						result= Boolean.FALSE;
						array[0] = result;
						break;
					}

					LOG.info("返回结果，是否成功：" + suppOrderResult.isSuccess()
							+ "[orderId=" + suppOrderResult.getOrdOrderId()
							+ ",orderItemId=" + suppOrderResult.getOrderItemId()
							+ "],errMsg:" + suppOrderResult.getErrMsg());

					if (!suppOrderResult.isSuccess()) {
						errMsg = suppOrderResult.getErrMsg();
						errMsg = errMsg.substring(errMsg.lastIndexOf(":")+1, errMsg.length());
						if(errMsg.getBytes().length == errMsg.length()){
							errMsg = "锁库存失败，请稍后再试。";
						}
						array[1] = errMsg;
						result=Boolean.FALSE;
						array[0] = result;
						break;
					}
				}
	        }catch(Exception e){
	        	LOG.error("锁库存出现系统异常",e);
	            throw new RemoteAccessException("锁库存出现系统异常",e);// 抛出这样的异常，调用方会重试的
	        }
	        LOG.info("vst_passport" + getSpecialTicket(ordOrder) + "锁库存是否成功"+result);
	        return array;
	}

	@Override
	public ResultHandleT<Integer> insertSelectiveO2oOrder(O2oOrder oOrder) {
		ResultHandleT<Integer> result =new ResultHandleT<Integer>();

		if(oOrder==null){
			return result;
		}
		 result.setReturnContent(o2oOrderService.insertSelective(oOrder));
		 return result;
	}

	@Override
	public ResultHandleT<Integer> deleteO2oOrderByOrderId(Long orderId) {
		ResultHandleT<Integer> result =new ResultHandleT<Integer>();

		if(orderId==null){
			return result;
		}
		 result.setReturnContent(o2oOrderService.deleteByOrderId(orderId));
		 return result;
	}


	/**
	 * 商品品类校验
	 * @param categoryId
	 * @return
	 */

	public boolean innerValidateGoodsCategory(Long categoryId ){
		boolean result =
				(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId().equals(categoryId)
		|| BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId().equals(categoryId)
		|| BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId().equals(categoryId)
		|| BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(categoryId)
		|| BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(categoryId)
		|| BizEnum.BIZ_CATEGORY_TYPE.category_wifi.getCategoryId().equals(categoryId)
		|| BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(categoryId)
		|| BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().equals(categoryId)
		|| BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(categoryId)
		|| BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(categoryId)
		|| BizEnum.BIZ_CATEGORY_TYPE.category_connects.getCategoryId().equals(categoryId)
		|| BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId().equals(categoryId)
		|| BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.getCategoryId().equals(categoryId)//增加定制游
		|| BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().equals(categoryId)  //增加大交通+x
		|| BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(categoryId)  //增加当地玩乐 美食
		|| BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(categoryId) //增加当地玩乐 娱乐
		|| BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(categoryId)  //增加当地玩乐 购物
		);
		return result ;

	}




    /**
	 * 产品品类校验
	 * @param categoryId
	 * @return
	 */
	public boolean innerValidateProductCategory(Long categoryId ){
		boolean result =
				( BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId().equals(categoryId)
		|| BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(categoryId)
		|| BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().equals(categoryId)
		|| BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().equals(categoryId)
		|| BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().equals(categoryId)
		|| BizEnum.BIZ_CATEGORY_TYPE.category_route_customized.getCategoryId().equals(categoryId)//增加定制游
		|| BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().equals(categoryId)  //增加大交通+x
		);
		return result ;

	}

	public String  getIdNo(OrdOrder order) {
		String idNo = "";

        OrdPerson ordPerson = null;
        OrdPerson firstAdultOrdPerson = order.getFirstAdultTravellerPerson();
        OrdPerson firstOrdPerson = order.getFirstTravellerPerson();
        OrdPerson contactPerson = order.getContactPerson();
        if (firstAdultOrdPerson != null) {
            ordPerson = firstAdultOrdPerson;
        } else if (firstOrdPerson != null) {
            ordPerson = firstOrdPerson;
        } else {
            ordPerson = contactPerson;
        }

        idNo = ordPerson.getIdNo();
        return idNo;
    }

	public String failReservationMsg(String specialTicket,Map<String,String> prmMap){
		String msg = "";
		if(SuppGoods.SPECIAL_TICKET_TYPE.DISNEY_TICKET.getCode().equals(specialTicket)||SuppGoods.SPECIAL_TICKET_TYPE.DISNEY_SHOW.getCode().equals(specialTicket)){
			msg = "该日期上海迪士尼商品余量不足，建议您尝试修改数量或选择其他日期。";
		}
		if(SuppGoods.SPECIAL_TICKET_TYPE.DALI_TICKET.getCode().equals(specialTicket)){
			String idNo = prmMap.get("idNo");
			if(StringUtils.isEmpty(idNo)){
				msg = "请提供正确的身份证号码。";
			}else{
				if(idNo.length()>10){
					idNo = idNo.substring(0,4)+"***"+idNo.substring(idNo.length()-6,idNo.length());
					msg = "该身份证："+idNo+"已购买过该商品，请使用其他身份证购买。";
				}
			}
		}
		if(SuppGoods.SPECIAL_TICKET_TYPE.AI_PIAO_TICKET.getCode().equals(specialTicket)){
			msg = prmMap.get("errMsg");
		}
		if(SuppGoods.SPECIAL_TICKET_TYPE.XIAO_JING_TICKET.getCode().equals(specialTicket)){
			msg = "该日期商品余量不足，建议您尝试修改数量或选择其他日期。";
		}
		
		if(SuppGoods.SPECIAL_TICKET_TYPE.YONGLE_SHOW_TICKET.getCode().equals(specialTicket)){
			msg = prmMap.get("errMsg");
		}
		
		if(SuppGoods.SPECIAL_TICKET_TYPE.ZHI_YOU_BAO_CHECK_TICKET.getCode().equals(specialTicket)){
			msg = prmMap.get("errMsg");
		}

		return msg;
	}


	@Override
	public List<OrdPerson> findOrdPerson(Long orderId, List<Long> personIds) {
		// TODO Auto-generated method stub
		return iOrdPersonService.findOrdPerson(orderId, personIds);
	}


	@Override
	public VstTravellerCallBackResponseDto updateTravellerPersonInfo(
			VstTravellerCallBackRequest travellerRequest) {

		return iOrdPersonService.updateTravellerPersonInfo(travellerRequest);
	}

    @Override
    public VstTravellerCallBackResponseDto checkDestBuTravDelayPersonInfo(
            VstTravellerCallBackRequest travellerRequest) {

        return iOrdPersonService.checkDestBuTravDelayPersonInfo(travellerRequest);
    }
	
	@Override
	public List<OrdPerson> selectLatestContactPerson(Map<String, Object> params) {
		return iOrdPersonService.selectLatestContactPerson(params);
	}

	public OrdOrderTravellerConfirm selectSingleByOrderId(Long orderId) {
		// TODO Auto-generated method stub
		return ordOrderTravellerConfirmService.selectSingleByOrderId(orderId);
	}
	@Override
	public ResultHandle travellerLockAudit(Long orderId, String operatorId) {
		
		ResultHandle handle = new ResultHandle();
		try{
			OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
			ActivitiKey key = createKeyByOrder(order);
			LOG.info("==travellerLockAudit==orderId:"+orderId+",ActivitiKey:"+key);
			processerClientService.completeTask(key, TASK_KEY.travellerLock.name(), operatorId);
		}catch(Exception ex){
			LOG.info("==travellerLockAudit==orderId:"+orderId,ex.getMessage());
			handle.setMsg(ex);
		}
		return handle;
	}
	
	private void saveLimitationsRedis(PromForbidKeyPo promForbidBuyOrder) {
		try {
			if (promForbidBuyOrder != null) {
				LOG.info("promForbidBuyOrder.isIsneedSave()"+promForbidBuyOrder.isIsneedSave());
				if (promForbidBuyOrder.isIsneedSave() == true) {
					JedisTemplate2 writerInstance = JedisTemplate2
							.getReaderInstance();
					Map<String, String> saveredis = promForbidBuyOrder
							.getRedisSaveContent();

					for (Map.Entry<String, String> entry : saveredis.entrySet()) {
						// 存30天
						// 判断是否存在key 如果存在key就累加完了再存。不存在就新增
						boolean exists = writerInstance.exists(entry.getKey());
						if (!exists) {
							writerInstance.set(entry.getKey(),
									entry.getValue(), 2592000 * 6);
							LOG.info("saveRedisLimit" + entry.getKey()+ ",value" + entry.getValue());
						} else {
							String value = writerInstance.get(entry.getKey());
							if (value != null && ("").equals(value) == false) {
								Pattern pattern = Pattern.compile("^\".*\"$");
								Matcher matcher = pattern.matcher(value);
								if (matcher.matches()) {
									value = value.substring(1,
											value.length() - 1);
								}
								Integer cunzaide = Integer.parseInt(value);
								Integer bucunzai = Integer.parseInt(entry
										.getValue());
								String quantiy = String.valueOf(cunzaide
										+ bucunzai);
								writerInstance.set(entry.getKey(), quantiy);
								LOG.info("updateRedisLimit" + entry.getKey()+",value:"+quantiy);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.error("FORBIDBUY REDIS EXCEPTION ");
		}
	}
	public  void cancelOrderAndRemoveForbidBuyRecord(Long orderId) {
		try {
			LOG.info("START FORBIDBUY CANCEL ORDER ID==" + orderId);


			OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
			if (order == null) {
				return;
			}
			String mobileEquipmentNo = complexQueryService.findMobileId(orderId);
			if (StringUtils.isNotBlank(mobileEquipmentNo)) {
				LOG.info("service select mobileEquipmentNo by orderid" +orderId+"mobileEquipmentNo" +mobileEquipmentNo);
				order.setMobileEquipmentNo(mobileEquipmentNo);
			}
			List<OrdOrderItem> orderItemList = order.getOrderItemList();
			Long productid = 0L;
			if (order.getOrdOrderPack() != null) {
				productid = order.getOrdOrderPack().getProductId();
			} else {
				productid = order.getMainOrderItem().getProductId();
			}

			Map<String, Integer> serchekeyMap14 = new HashMap<String, Integer>();

			Map<String, Object> paramTraveller = new HashMap<String, Object>();
			paramTraveller.put("object",
					OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER.name());
			paramTraveller.put("objectId", orderId);
			paramTraveller.put("personType",
					OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
			List<OrdPerson> personTraveller = iOrdPersonService
					.findOrdPersonList(paramTraveller);

			Date visdate = order.getVisitTime();
			Date createDate = order.getCreateTime();
			Map<String, Object> params = new HashMap<String, Object>();

			params.put("objectType", "PRODUCT");
			params.put("objectId", productid);
			LOG.info(" PRODUCT objectId" + productid);

			List<PromForbidBuy> promForbidBuy = promForbidBuyClientService
					.getPromForbidBuyByParams(params);
			if (promForbidBuy == null || promForbidBuy.isEmpty()) {
				LOG.info("HAS DATE PROMFORBIDBUY " + orderId);
				// 商品的情况
				for (OrdOrderItem ordOrderItem : orderItemList) {
					Long quantity14 = ordOrderItem.getQuantity();
					params.put("objectType", "GOODS");
					Long suppGoodsId = ordOrderItem.getSuppGoodsId();
					params.put("objectId", suppGoodsId);
					LOG.info(" GOODS objectId" + suppGoodsId);

					List<PromForbidBuy> promForbidBuy2 = promForbidBuyClientService
							.getPromForbidBuyByParams(params);
					if (promForbidBuy2 == null || promForbidBuy2.isEmpty()) {
						LOG.info("has been continue" + orderId);
						continue;
					}
					PromForbidBuy pb = promForbidBuy2.get(0);
					//add by zhouguoliang for group forbid
					if(("GROUPGOODS").equals(pb.getObjectType())){
						suppGoodsId=pb.getGroupId();
					}

					String time = formartdate(getDate(pb, visdate, createDate));
					String periodtype = getPeriodType(pb);
					String objecttype = getObjectTypeKey(pb);
					String quantitytype = getQuantityType(pb);
					List<String> ids14 = allids14(pb, order);
					// List<String> ids23 = allids23(pb, personTraveller,
					// order);
					List<String> ids2 = allids2(pb, personTraveller, order);
					List<String> ids3 = allids3(pb, personTraveller, order);
					// 用户id和 设备号。买了多少分。serchekeylist14这个map里面
					for (String id : ids14) {
						String seacheKey1 = periodtype + COMMA + time + COMMA
								+ id + COMMA + objecttype + COMMA
								+ suppGoodsId + COMMA
								+ quantitytype;
						if (quantitytype.equals("O")) {
							quantity14 = 1L;
						} else {
							quantity14 = ordOrderItem.getQuantity();
						}
						serchekeyMap14.put(seacheKey1, quantity14.intValue());
					}
					LOG.info(" serchekeyilist14 is " + serchekeyMap14);
					// 如果 手机号不为空 那么判断手机的list里面的长度。如果长度大于1则数量设置成1
					// 如果长度等于1则放入item里面的数量
					if (ids2 != null && ids2.isEmpty() == false) {
						// 判断是否是根据商品数量限购的。如果是根据产品限购的，那么需要判断数量是怎么算
						if (quantitytype.equals("P")) {
							if (ids2.size() == 1) {
								String seacheKey2 = periodtype + COMMA + time
										+ COMMA + ids2.get(0) + COMMA
										+ objecttype + COMMA
										+ suppGoodsId + COMMA
										+ quantitytype;
								serchekeyMap14.put(seacheKey2, ordOrderItem
										.getQuantity().intValue());
							}
							if (ids2.size() > 1) {
								for (String phoneNo : ids2) {
									String seacheKey2 = periodtype + COMMA
											+ time + COMMA + phoneNo + COMMA
											+ objecttype + COMMA
											+ suppGoodsId
											+ COMMA + quantitytype;
									serchekeyMap14.put(seacheKey2, 1);
								}
							}
						}// 判断如果是订单数量限购，如果是根据订单数量限购的，那么直接将数量设置成1 订单只可能去掉一笔
						else {
							for (String phoneNo : ids2) {
								String seacheKey2 = periodtype + COMMA + time
										+ COMMA + phoneNo + COMMA + objecttype
										+ COMMA + suppGoodsId
										+ COMMA + quantitytype;
								serchekeyMap14.put(seacheKey2, 1);
							}
							LOG.info("IDS 2 serchekeyMap14 is "
									+ serchekeyMap14);
						}
					}
					// 手机号逻辑到此结束 serchekylist23

					// 身份证号逻辑开始
					// 如果 身份证号码不为空，那么判断身份证list 长度。如果长度大于1
					// 则数量直接设置成1，如果长度等于1则放入item里面的数量
					if (ids3 != null && ids3.isEmpty() == false) {
						if (quantitytype.equals("P")) {
							if (ids3.size() == 1) {
								String seacheKey2 = periodtype + COMMA + time
										+ COMMA + ids3.get(0) + COMMA
										+ objecttype + COMMA
										+ suppGoodsId + COMMA
										+ quantitytype;
								serchekeyMap14.put(seacheKey2, ordOrderItem
										.getQuantity().intValue());
							} else if (ids3.size() > 1) {
								for (String idtypeAndIds : ids3) {
									String seacheKey2 = periodtype + COMMA
											+ time + COMMA + idtypeAndIds
											+ COMMA + objecttype + COMMA
											+ suppGoodsId
											+ COMMA + quantitytype;
									serchekeyMap14.put(seacheKey2, 1);
								}
							}
						} else {
							for (String idtypeAndIds : ids3) {
								String seacheKey2 = periodtype + COMMA + time
										+ COMMA + idtypeAndIds + COMMA
										+ objecttype + COMMA
										+ suppGoodsId + COMMA
										+ quantitytype;
								serchekeyMap14.put(seacheKey2, 1);
							}
						}
					}
					// 身份证逻辑到此结束
					LOG.info(" serchekeyilist23 is " + serchekeyMap14);
				}

			}
			// 非商品的情况 看产品
			if (promForbidBuy != null && promForbidBuy.isEmpty() == false) {
				LOG.info("into product forbid buy " + orderId);

				PromForbidBuy pb = promForbidBuy.get(0);
				String time = formartdate(getDate(pb, visdate, createDate));
				String periodtype = getPeriodType(pb);
				String objecttype = getObjectTypeKey(pb);
				String quantitytype = getQuantityType(pb);
				List<String> ids14 = allids14(pb, order);
				List<String> ids23 = allids23(pb, personTraveller, order);
				LOG.info("pb .to string is " + pb.toString());
				for (String id : ids14) {
					String seacheKey1 = periodtype + COMMA + time + COMMA + id
							+ COMMA + objecttype + COMMA + productid + COMMA
							+ quantitytype;
					serchekeyMap14.put(seacheKey1, 1);
				}
				LOG.info(" product serchekeyilist14 is " + serchekeyMap14);
				for (String id : ids23) {
					String seacheKey2 = periodtype + COMMA + time + COMMA + id
							+ COMMA + objecttype + COMMA + productid + COMMA
							+ quantitytype;
					serchekeyMap14.put(seacheKey2, 1);
				}
				LOG.info(" product serchekeyilist23 is " + serchekeyMap14);
			}

			// 搜索redis里面的东西。看存不存在。如果存在。就把他减去他购买的份数。或者订单笔数。
			findRedisAndMinusQuantity(serchekeyMap14,
					JedisTemplate2.getReaderInstance());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("FORBIDBUY CANCELORDER FAILD");
		}

	}


	@Override
	public String getRouteOrderRefundRules(Long orderId) {
		LOG.info("getRouteOrderRefundRules,orderId="+orderId);

		StringBuffer rules = new StringBuffer();
		OrdOrder ordOrder = complexQueryService.queryOrderByOrderId(orderId);
		if(ordOrder == null){
			LOG.info("订单不存在，orderId = " + orderId);
			return rules.toString();
		}
		LOG.info("订单品类Id，categoryId = " + ordOrder.getCategoryId());
		//判断是否是自由行或者酒店套餐
		if(!BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(ordOrder.getCategoryId()) &&
				!BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().equals(ordOrder.getCategoryId()) &&
				(!BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(ordOrder.getCategoryId()))){
			LOG.info("订单orderId = " + orderId + "，不属于酒店套餐和自由行，无法获取退改规则。");
			return rules.toString();
		}
		LOG.info("订单orderId = " + orderId + "，退改策略cancelStrategy = " + ordOrder.getRealCancelStrategy());
		if(ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy()) ||
				ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy()) ||
				ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){//不退不改或者可退该或者人工退改

			List<OrdOrderItem> ordOrderItems = ordOrder.getOrderItemList();
			for(OrdOrderItem orderItem : ordOrderItems) {

				//排除保险
				if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){
					continue;
				}

				LOG.info("订单orderId = " + orderId + "，子订单orderItemId = " + orderItem.getOrderItemId() + "，categoryId = " + orderItem.getCategoryId());
				Map<String,Object> contentMap = orderItem.getContentMap();
				LOG.info("productName = " + orderItem.getProductName() + "，branchName = " + contentMap.get("branchName"));


				if((BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(ordOrder.getCategoryId()) &&
						BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())) ||(
								BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(ordOrder.getCategoryId()) 
								&& BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(orderItem.getCategoryId()))||
						orderItem.getOrderPackId() != null){//主订单是酒店套餐或者自由行中被打包的商品
					LOG.info("订单orderId = " + orderId + "，子订单orderItemId = " + orderItem.getOrderItemId() + "，categoryId = " + orderItem.getCategoryId());

					//不退不改
					if(ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){
						rules.append(orderItem.getProductName() + "-" + contentMap.get("branchName") + "\n");
						rules.append("该产品不可退改，入住如需修改或取消，一律收取订单的全部费用的100%作为损失费，敬请谅解！\n");
					}else if(ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){//人工退改
						rules.append(orderItem.getProductName() + "-" + contentMap.get("branchName") + "\n");
						rules.append("该产品支持人工退改，可致电24小时服务热线1010-6060。\n");
					}else if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){//可退该
						rules.append(orderItem.getProductName() + "-" + contentMap.get("branchName") + "\n");
						LOG.info("orderItemId = " + orderItem.getOrderItemId() + "，refundRules = " + orderItem.getRefundRules());
						//List<ProdRefundRule> rulesList = com.alibaba.fastjson.JSONArray.parseArray(orderItem.getRefundRules(), ProdRefundRule.class);
						List<ProdRefundRule> rulesList =  new ArrayList<ProdRefundRule>();
						if(StringUtil.isNotEmptyString(orderItem.getRefundRules())){
							List<ProdRefundRule> refundList_ = com.alibaba.fastjson.JSONArray.parseArray(orderItem.getRefundRules(), ProdRefundRule.class);
							if(refundList_ != null){
								rulesList = refundList_;
							}
						}
						
						StringBuffer buffer = new StringBuffer();
						
						for(ProdRefundRule rule: rulesList){
							String ruleDesc = rule.getRuleDescNew(ordOrder.getVisitTime(),ordOrder.getCreateTime());
							if(!"".equals(ruleDesc)) {
								buffer.append(ruleDesc+"\n");
							}
						}
						
						if("过期".equals(buffer.substring(0, 2))) {
							buffer.replace(0, 2, "申请退款");
						}
						
						rules.append(buffer);
					}
				}else{//关联销售（只显示门票）
					String categoryCode =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
					ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(categoryCode);
					BizCategory bizCategory=result.getReturnContent();
					if((bizCategory.getParentId()!=null && bizCategory.getParentId().equals(5L))) {//门票
						//产品名+规格名
						rules.append(orderItem.getProductName() + "-" + contentMap.get("branchName") + "\n");
						//将门票的的退改规则快照转化为商品退改规则list
						//List<SuppGoodsRefund> refundList = com.alibaba.fastjson.JSONArray.parseArray(orderItem.getRefundRules(), SuppGoodsRefund.class);
						List<SuppGoodsRefund> refundList =  new ArrayList<SuppGoodsRefund>();
						if(StringUtil.isNotEmptyString(orderItem.getRefundRules())){
							List<SuppGoodsRefund> refundList_ = com.alibaba.fastjson.JSONArray.parseArray(orderItem.getRefundRules(), SuppGoodsRefund.class);
							if(refundList_ != null){
								refundList = refundList_;
							}
						}
						rules.append(SuppGoodsRefundTools.SuppGoodsRefundVOToStr(refundList,contentMap.get("aperiodic_flag").toString())+"\n");
					}
				}
			}
		}else if(ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE.getCode().equals(ordOrder.getRealCancelStrategy())){//同步商品退改
			List<OrdOrderItem> ordOrderItems = ordOrder.getOrderItemList();
			for(OrdOrderItem orderItem : ordOrderItems){
				LOG.info("订单orderId = " + orderId + "，子订单orderItemId = " + orderItem.getOrderItemId() + "，categoryId = " + orderItem.getCategoryId());
				Map<String,Object> contentMap = orderItem.getContentMap();
				String categoryCode =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
				ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(categoryCode);
				BizCategory bizCategory=result.getReturnContent();

				LOG.info("productName = " + orderItem.getProductName() + "，branchName = " + contentMap.get("branchName"));
				if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId()) ){//酒店
					SuppGoodsTimePrice timePrice = new SuppGoodsTimePrice();
					timePrice.setDeductValue(orderItem.getDeductAmount());
					timePrice.setDeductType(orderItem.getDeductType());
					//产品名+规格名
					rules.append(orderItem.getProductName() + "-" + contentMap.get("branchName") + "\n");
					if (SuppGoods.PAYTARGET.PREPAID.name().equalsIgnoreCase(ordOrder.getPaymentTarget())) {
						if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name().equalsIgnoreCase(orderItem.getCancelStrategy())) { // 预付可退改
							LOG.info("----------查查看子订单所属BU----------------"+orderItem.getBuCode());
							if(CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equalsIgnoreCase(orderItem.getBuCode())){
								Date lastCancelTime = orderItem.getLastCancelTime();
								if(!lastCancelTime.after(ordOrder.getCreateTime())) {
									rules.append("订单一经预订成功，不可变更/取消，如未入住将扣除全额房费"+"\n");
								}else {									
									rules.append("在"+DateUtil.getFormatDate(lastCancelTime,DateUtil.HHMM_DATE_FORMAT)+
											"前您可免费变更/取消订单，超时变更/取消订单，酒店将扣除房费"+"\n");
								}								
							}else{
								rules.append("在"+DateUtil.getFormatDate(orderItem.getLastCancelTime(),DateUtil.HHMM_DATE_FORMAT)+
										"前您可免费变更/取消订单，超时变更/取消订单，酒店将扣除房费￥"+orderItem.getDeductAmountToYuan()+"\n");
							}							
						}else{
							rules.append("订单一经预订成功，不可变更/取消，如未入住将扣除" + SuppGoodsTimePrice.getReturnMessage(timePrice)+"\n");
						}
					}
				}else if((bizCategory.getParentId()!=null && bizCategory.getParentId().equals(5L))){//门票
					//产品名+规格名
					rules.append(orderItem.getProductName() + "-" + contentMap.get("branchName") + "\n");
					//将门票的的退改规则快照转化为商品退改规则list
					//List<SuppGoodsRefund> refundList = com.alibaba.fastjson.JSONArray.parseArray(orderItem.getRefundRules(), SuppGoodsRefund.class);
					List<SuppGoodsRefund> refundList =  new ArrayList<SuppGoodsRefund>();
					if(StringUtil.isNotEmptyString(orderItem.getRefundRules())){
						List<SuppGoodsRefund> refundList_ = com.alibaba.fastjson.JSONArray.parseArray(orderItem.getRefundRules(), SuppGoodsRefund.class);
						if(refundList_ != null){
							refundList = refundList_;
						}
					}
					rules.append(SuppGoodsRefundTools.SuppGoodsRefundVOToStr(refundList,contentMap.get("aperiodic_flag").toString())+"\n");
				}else if(BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().equals(orderItem.getCategoryId())){
					//产品名+规格名
					rules.append(orderItem.getProductName() + "-" + contentMap.get("branchName") + "\n");					
					if(ProdRefund.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){//不退不改						
						rules.append("该产品不可退改，入住如需修改或取消，一律收取订单的全部费用的100%作为损失费，敬请谅解！\n");
					}else if(ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(orderItem.getCancelStrategy())){//人工退改
						rules.append("该产品支持人工退改，可致电24小时服务热线1010-6060。\n");
					}else if(ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(orderItem.getCancelStrategy())){//可退改
						//将快照中的退改规则转化为产品阶梯退改规则list
						///List<ProdRefundRule> rulesList = com.alibaba.fastjson.JSONArray.parseArray(orderItem.getRefundRules(), ProdRefundRule.class);
						List<ProdRefundRule> rulesList =  new ArrayList<ProdRefundRule>();
						if(StringUtil.isNotEmptyString(orderItem.getRefundRules())){
							List<ProdRefundRule> refundList_ = com.alibaba.fastjson.JSONArray.parseArray(orderItem.getRefundRules(), ProdRefundRule.class);
							if(refundList_ != null){
								rulesList = refundList_;
							}
						}
						
						StringBuffer buffer = new StringBuffer();
						
						for(ProdRefundRule rule: rulesList){
							String ruleDesc = rule.getRuleDescNew(ordOrder.getVisitTime(),ordOrder.getCreateTime());
							if(!"".equals(ruleDesc)) {
								buffer.append(ruleDesc+"\n");
							}
						}
						
						if("过期".equals(buffer.substring(0, 2))) {
							buffer.replace(0, 2, "申请退款");
						}
												
						rules.append(buffer);
					}

				}
			}
		}
		LOG.info("orderId = " + orderId + "，rules = " + rules.toString());
		return rules.toString();
	}

	@Override
	public VstTravellerCallBackResponseDto updateAndSaveTravellerPersonInfo(
			VstTravellerCallBackRequest travellerRequest) {
		return iOrdPersonService.updateAndSaveTravellerPersonInfo(travellerRequest);
	}

	@Override
	public int queryListTraverllerCountByOrderId(Long orderId) {
		
		return iOrdPersonService.queryListTraverllerCountByOrderId(orderId);
	}

	@Override
	public ResultHandle updateOrderperonTravellerDelayFlag(Long orderId, OrdOrderPersonVO vo, String flag) {
		ResultHandle handle = new ResultHandle();
		OrdOrder order = queryOrdorderByOrderId(orderId);
		if(order==null){
			handle.setMsg("订单不存在");
			return handle;
		}
		ResultHandleT<String> result = orderUpdateService.updateOrderPerson(order,vo);
		if(result.isSuccess()&&!result.hasNull()){
			
			if("Y".equals(order.getTravellerDelayFlag())){
				if("Y".equals(flag)||"Y".equals(order.getTravellerLockFlag())){
					orderMessageProducer.sendMsg(MessageFactory.newOrderModifyPersonMessage(orderId, result.getReturnContent()));
				}
			}else{
				orderMessageProducer.sendMsg(MessageFactory.newOrderModifyPersonMessage(orderId, result.getReturnContent()));
			}
		}
		return handle;
	}


    @Override
    public Object getOrderCount(String createTimeBegin, String createTimeEnd,
                                Long categoryId, String superChannelIdsStr) {
        if(StringUtils.isEmpty(createTimeBegin)||StringUtils.isEmpty(createTimeEnd)||null==categoryId||StringUtils.isEmpty(superChannelIdsStr)){
            LOG.info("属性为空");
            return null;
        }
        Map<String, Object> objectMap = new HashMap<String, Object>();
        OrderMonitorCnd orderMonitorCnd = new OrderMonitorCnd();
        orderMonitorCnd.setSuperChannelIdsStr(superChannelIdsStr);
        orderMonitorCnd.setCreateTimeBegin(createTimeBegin);
        orderMonitorCnd.setCreateTimeEnd(createTimeEnd);
        orderMonitorCnd.setCategoryId(categoryId);
        ParameterConverter<OrderMonitorCnd> pc = new BeanESParameterConverter();
        ESParams eSParams = pc.convert(orderMonitorCnd);
        SearchResponse esResponse = search(null, null, eSParams);
        SearchHits hits = esResponse.getHits();
        if (hits != null) {
            objectMap.put("OrdOrderCount", hits.getTotalHits());
            LOG.info("获取订单数量成功");
        } else {
            LOG.info("获取订单数量失败");
        }
        return objectMap;
    }
    
    /*
	 * 提供给无线的接口 获取不同状态的订单数量
	 */
	public Object getOrderCount(String paymentTimeBegin, String paymentTimeEnd,
			Long categoryId, String buCode, String distributionChannel,
			String distributionCode) {
		if (StringUtils.isEmpty(paymentTimeBegin)
				|| StringUtils.isEmpty(paymentTimeEnd) || null == categoryId
				|| StringUtils.isEmpty(distributionCode)) {
			LOG.info("传入参数为空！");
			return null;
		}
		if (!(distributionCode.contains("ANDROID")
				|| distributionCode.contains("IPHONE") || distributionCode.contains("TOUCH"))) {
			LOG.info("传入参数distributionCode前缀应是(ANDROID,IPHONE,TOUCH)");
			return null;
		}
		Map<String, Object> objectMap = new HashMap<String, Object>();
		try {
			SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			OrderMonitorCnd orderMonitorCnd = new OrderMonitorCnd();
 			orderMonitorCnd.setDistributionChannel(distributionChannel);
			orderMonitorCnd.setPaymentTimeBegin(sdf.parse(paymentTimeBegin));
			orderMonitorCnd.setPaymentTimeEnd(sdf.parse(paymentTimeEnd));
 			orderMonitorCnd.setCategoryId(categoryId);
			orderMonitorCnd.setBelongBU(buCode);
			//模糊查询
			orderMonitorCnd.setDistributionCode(distributionCode);
			//查询订单支付成功的数据(PAYED)
			orderMonitorCnd.setPaymentStatus("PAYED");
			ParameterConverter<OrderMonitorCnd> pc = new BeanESParameterConverter();
			ESParams eSParams = pc.convert(orderMonitorCnd);
			SearchResponse esResponse = search(null, null, eSParams);
			SearchHits hits = esResponse.getHits();
			if (hits != null) {
				objectMap.put("OrdOrderCount", hits.getTotalHits());
				LOG.info("获取订单数量成功");
			} else {
				LOG.info("获取订单数量失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("操作失败!");
		}
		return objectMap;
	}

    public SearchResponse search(Integer page, Integer pageSize, ESParams params) {
        Integer  currentPageSize = pageSize == null ? 10 : pageSize;
        // 组装订单分页类条件
        Integer currentPage = page == null ? 1 : page;

        Client client = ESWrapper.getInstance();
        String[] indices = ESWrapper.getIndices();
        String[] types = ESWrapper.getDocumentType();
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indices).setTypes(types)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH);

        searchRequestBuilder = new ESQueryBuilder().request_parameters(searchRequestBuilder, params);
        SearchResponse response = searchRequestBuilder.setFrom((currentPage - 1) * currentPageSize)
                .addSort(SortBuilders.fieldSort("ORDER_ID").order(SortOrder.DESC))
                .setSize(currentPage * currentPageSize).setExplain(true).execute().actionGet();
        return response;
    }
	
    @Async
    private void recordOrdOrderStatus(ResultHandleT<OrdOrder> handle) {
        ordOrderStatusClientService.addOrdOrderStatus(handle);
    }
	
	@Override
	public ResultHandleT<List<TicketOrderItemVo>> getPageTicketItemListByUserId(String userId,Integer pageSize,Integer pageNo){
		ResultHandleT<List<TicketOrderItemVo>> resultHandleT = new ResultHandleT<List<TicketOrderItemVo>>();
		if(StringUtils.isNotEmpty(userId)){
			Integer  currentPageSize = pageSize == null ? 10 : pageSize;
	        Integer currentPageNo = pageNo == null ? 1 : pageNo;
	        Integer totalCount = iOrdOrderItemService.getTicketItemCountByUserId(userId);
	        Page<TicketOrderItemVo> pageParam = Page.page(totalCount, currentPageSize, currentPageNo);
	        Map<String,Object> paramsMap = new HashMap<String, Object>();
	        paramsMap.put("userId", userId);
			paramsMap.put("_start", pageParam.getStartRows());
			paramsMap.put("_end", pageParam.getEndRows());
			paramsMap.put("_orderby", "ord.CREATE_TIME");
			paramsMap.put("_order", "DESC");
			List<OrdOrderItem> itemList = iOrdOrderItemService.getPageTicketItemListByUserId(paramsMap);
			List<TicketOrderItemVo> ticketOrderItemVoList = new ArrayList<TicketOrderItemVo>();
			if(itemList != null && itemList.size() > 0){
				for(OrdOrderItem item : itemList){
					if(item != null && (item.getCategoryId() == 11L || item.getCategoryId() == 12L
							|| item.getCategoryId() == 13L || item.getCategoryId() == 31L)){
						setOrderItemOtherDetails(item);
						List<TicketOrderItemVo> ticketOrderItemVoTempList = getTicketOrderItemVoList(item);
						if(!ticketOrderItemVoTempList.isEmpty())
							ticketOrderItemVoList.addAll(ticketOrderItemVoTempList);
					}
				}
			}
			if(!ticketOrderItemVoList.isEmpty()){
				resultHandleT.setReturnContent(ticketOrderItemVoList);
				resultHandleT.setMsg(totalCount.toString());
			}
			else{
				resultHandleT.setMsg("ticketOrderItemVoList is empty. ");
			}
		}else{
			resultHandleT.setMsg("User id can not be null. ");
		}
			
		return resultHandleT;
	}
	@Override
    public Page<VstOrdOrderItemDateVo> getAllOrdOrderItemDate(String visitTime,Long pageSize,Long pageNo ) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("visitTime", visitTime);
        Long currentPageNo = pageNo == null ? 1 : pageNo;
        Integer totalCount = iOrdOrderItemService.queryOrderItemCountByVisitDate(params);
        Page<TicketOrderItemVo> pageParam = Page.page(totalCount, pageSize, currentPageNo);
        Map<String,Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("visitTime", visitTime);
        paramsMap.put("_start", pageParam.getStartRows());
        paramsMap.put("_end", pageParam.getEndRows());
        paramsMap.put("_orderby", "ORD.ORDER_ID");
        List<VstOrdOrderItemDateVo> ordOrderItemList =  iOrdOrderItemService.queryTicketOrderItemByVisitDate(paramsMap);
        Page<VstOrdOrderItemDateVo> page = new Page<VstOrdOrderItemDateVo>();
        page.setItems(ordOrderItemList);
        page.setPageSize(pageSize);
        page.setTotalResultSize(totalCount);
        return page;
    }
	@Override
	public PassProvider getProductServiceInfo(Long goodsId){
		return supplierOrderOperator.getProductServiceInfo(goodsId);
	}

	@Override
	public List<OrdInvoiceRelation> getOrdInvoiceRelation(Map<String, Object> param) {
		return orderInvoiceRelationService.getListByParam(param);
	}

	@Override
	public List<OrdInvoice> getOrdInvoiceListByOrderId(Map<String, Object> param) {
		return ordInvoiceService.getOrdInvoiceListByOrderId(param);
	}
	
	@Override
	public Long getInvoiceCount(Map<String, Object> param) {
		return ordInvoiceService.getInvoiceCount(param);
	}
	
	@Override
	public OrdInvoice getInvoiceById(Long id) {
		return ordInvoiceService.selectByPrimaryKey(id);
	}

	@Override
	public List<OrdAddress> findOrdAddressList(Map<String, Object> param) {
		return ordAdressService.findOrdAddressList(param);
	}

	@Override
	public void updateOrdAddressAndPerson(OrdPerson ordPerson,
			OrdAddress ordAddress, Long orderId, String loginUserId) {
		ordAdressService.updateOrdAddressAndPerson(ordPerson, ordAddress, orderId, loginUserId);
	}

	@Override
	public void sendSmsByCustom(Long orderId, String content, String operate,
			String mobile) {
		orderSmsSendService.sendSmsByCustom(orderId, content, operate, mobile);
		
	}

	@Override
	public List<SuppOrderResult> resendSms(Long ordOrderId) {
		return passportSendSmsService.resendSms(ordOrderId);
	}
	
	@Override
	public List<SuppOrderResult> getTicketExchangeStatus(Long orderId,
			Long orderItemId) {
		return supplierOrderOtherService.getTicketExchangeStatus(orderId, orderItemId);
	}

	@Override
	public List<OrdItemAdditionStatus> findOrdItemAdditionStatusList(
			Map<String, Object> params) {
		return ordItemAdditionStatusService.findOrdItemAdditionStatusList(params);
	}

	@Override
	public Pair<Date, List<Long>> updateOrderItemVisitTime(Long orderId,
			Long orderItemId, Date changeVisitDate) {
		Pair<Date, List<Long>> resultHandle = orderUpdateService.updateOrderItemVisitTime(orderId, orderItemId, changeVisitDate);
		//o2o渠道订单发送更新消息
		try{
			OrdOrder order = iOrdOrderService.findByOrderId(orderId);
			if((order != null && order.getOrderId() != null) 
					&& (order.getDistributorId() == Constant.DIST_O2O_SELL || order.getDistributorId() == Constant.DIST_O2O_APP_SELL)){
				orderMessageProducer.sendMsg(MessageFactory.newVistTimeChangeMessage(orderId, ""));
			}
		}catch(Exception e){
			LOG.error("o2o order change vistTime, sendMsg Error", e);
		}
		return resultHandle;
	}

	@Override
	public ResultHandleT<VstOrderSupApiFlightStatus> querySupApiFlightOrderItemStatusByOrderId(Long orderId) {
		ResultHandleT<VstOrderSupApiFlightStatus> result=new ResultHandleT<VstOrderSupApiFlightStatus>();
		
		//查询orderInfo by orderId
		VstOrderSupApiFlightStatus returnContent=new VstOrderSupApiFlightStatus();
		returnContent.setOrderId(orderId);
		result.setReturnContent(returnContent);
		try{
			//非空检验
			if(orderId==null || orderId==0){
				returnContent.setMsgCode(VstOrderSupApiFlightStatus.SUPAPIFLIGHTSTATUS_CODE.CODE_501.getCode());
				returnContent.setMessage(VstOrderSupApiFlightStatus.SUPAPIFLIGHTSTATUS_CODE.CODE_501.getMsg());
				return result;
			}
			//查询
			OrdOrder order=complexQueryService.queryOrderByOrderId(orderId);
			if(order!=null){
				returnContent.setInfoStatus(order.hasInfoAndResourcePass());
				if(order.getOrderItemList()!=null&&order.getOrderItemList().size()>0){
					List<VstOrderItemSupApiFlightStatus> listItem=new ArrayList<VstOrderItemSupApiFlightStatus>();
					VstOrderItemSupApiFlightStatus itemStatus=null;
					for (OrdOrderItem item : order.getOrderItemList()) {
						itemStatus=new VstOrderItemSupApiFlightStatus();
				    	if(item.isApiFlightTicket()){
				    		itemStatus.setApiFlightTicket(true);
						}else{
							itemStatus.setApiFlightTicket(false);
						}
						itemStatus.setCategoryId(item.getCategoryId());
						itemStatus.setOrderItemId(item.getOrderItemId());
						itemStatus.setResourceStatus(item.getResourceStatus());
						itemStatus.setResourceConfirm(item.getNeedResourceConfirm());
						listItem.add(itemStatus);
					}
					returnContent.setOrderItemSupApiFlight(listItem);
				}
				returnContent.setMsgCode(VstOrderSupApiFlightStatus.SUPAPIFLIGHTSTATUS_CODE.CODE_200.getCode());
			}
		}catch(Exception e){
			LOG.error("orderId:"+orderId+",exception:"+e);
			returnContent.setMsgCode(VstOrderSupApiFlightStatus.SUPAPIFLIGHTSTATUS_CODE.CODE_500.getCode());
			returnContent.setMessage(VstOrderSupApiFlightStatus.SUPAPIFLIGHTSTATUS_CODE.CODE_500.getMsg());
		}
		return result;
	}

    @Override
	public Pair<Boolean,String> getPromForbidBuyOrder(final BuyInfo buyInfo, String operatorId){
    	Pair<Boolean,String> pair = new Pair<Boolean, String>();
    	//身份证输入规范中的字母为大写，所以在此统一转为大写
		if(buyInfo != null){
			List<Person> travellers = buyInfo.getTravellers();
			if(travellers != null && travellers.size() > 0){
				for(Person person : travellers){
					if(person != null && !StringUtil.isEmptyString(person.getIdNo())){
						person.setIdNo(person.getIdNo().toUpperCase());
					}
				}
			}
		}
		Long startTime = System.currentTimeMillis();
		LOG.info("getPromForbidBuyOrder下单方法中接收到的JSON"+buyInfo.toJsonStr());
		Map<String,Object> map = new HashMap<String, Object>();
		PromForbidKeyPo promForbidBuyOrder=null;
		if (orderPromServiceImpl != null) {
			Long startOrderPromTime = System.currentTimeMillis();
			 promForbidBuyOrder = orderPromServiceImpl.isPromForbidBuyOrder(buyInfo);
			 LOG.info(ComLogUtil.printTraceInfo("isPromForbidBuyOrder","查询限购信息", "orderPromServiceImpl.isPromForbidBuyOrder", System.currentTimeMillis() - startOrderPromTime));
			if (promForbidBuyOrder != null) {
				pair.setFirst(promForbidBuyOrder.isIsforbid());
				if (promForbidBuyOrder.isIsforbid()) {
					if(promForbidBuyOrder.getMsg().length()>200){
						LOG.info("promForbidBuyOrder.getMsg(): "+promForbidBuyOrder.getMsg());
					}
					pair.setSecond(promForbidBuyOrder.getMsg());
				}
			}
		}else{
			LOG.info("is null forbid buy");
		}
		LOG.info("forbidbuyMap:"+map);
		LOG.info(ComLogUtil.printTraceInfo("getPromForbidBuyOrder","查询限购信息", "VstOrderManageServiceImpl.getPromForbidBuyOrder", System.currentTimeMillis() - startTime));
		return pair;
	}

    /**
     * 单酒店，根据商品ID，入住日期，离店日期，分销商ID查询库存
     * @param suppGoodsId 商品ID
     * @param visitTimeDate 入住日期
     * @param leaveTimeDate 离店日期
     * @param distributionId 分销商ID
     * @return
     */
    @Override
    public ResultHandleT<SuppGoodsStock> getHotelDatesSuppGoodsStock(Long suppGoodsId, Date visitTimeDate, Date leaveTimeDate, Long distributionId) {
        return orderStockService.getHotelSuppGoodsStock(suppGoodsId, visitTimeDate, leaveTimeDate, distributionId);
    }
	
	/**
	 * 根据产品ID和门店ID查询指定时间内的有效订单的数量 
	 * @param params
	 * @return
	 */
	@Override
	public ResultHandleT<Integer> countOrderMainItemForO2oTicketByProductId(Map<String, Object> params) {
		ResultHandleT<Integer> resultHandleT=new ResultHandleT<Integer>();
		if (params.get("productId")==null
			||params.get("distributorId")==null
			||params.get("distributorCode")==null
			||params.get("beginTime")==null
			||params.get("endTime")==null){
			resultHandleT.setReturnContent(0);
			resultHandleT.setInfoMsg("查询参数不完整");
			return resultHandleT;
		}
		Integer count=iOrdOrderItemService.countOrderMainItemForO2oTicketByProductId(params);
		 resultHandleT.setReturnContent(count);
		 return resultHandleT;
	}
	
	/**
	 * 根据orderId获取orderItem list
	 * @param orderId
	 */
	@Override
	public List<Long> getOrderItemIdListByOrderId(Long orderId){
		return iOrdOrderItemService.getOrderItemIdListByOrderId(orderId);
	}
	
	/**
	 * 根据orderItemId获取orderId
	 */
	@Override
	public Long getOrderIdByOrderItemId(Long orderItemId){
		return iOrdOrderItemService.getOrderIdByOrderItemId(orderItemId);
	}

	
	/**
	 * 根据OrderItemid获得通关码
	 */
	@Override
	public ResultHandleT<OrdPassCode> getOrdPassCodeByOrderItemId(
			Long orderItemInId) {
		ResultHandleT<OrdPassCode> handle = new ResultHandleT<OrdPassCode>();
		try{
			handle.setReturnContent(supplierOrderHandleService.getOrdPassCodeByOrderItemId(orderItemInId));
		}catch(Exception e){
			handle.setMsg(e);
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		return handle;
	}
	

	
	public Long getOrderIdByPassCodeId(Long passCodeId){
		return intfPassCodeService.getOrderIdByPassCodeId(passCodeId);
	}


    @Override
    public List<OrdOrderInsurance> getInsuranceOrderByOrderIdList(List<Long> orderIdList) {
        List<OrdOrderInsurance> orderList = null;
        if (orderIdList != null && orderIdList.size() > 0) {
            orderList = orderUpdateService.getInsuranceOrderByOrderIdList(orderIdList);
        }

        return orderList;
    }

	@Override
	public ResultHandleT<OrdOrder> loadOrderWithItemByOrderId(Long orderId) {
		ResultHandleT<OrdOrder> handle=new ResultHandleT<OrdOrder>();
		if(orderId==null){
			LOG.warn("[loadOrderWithItemByOrderId] orderId is null");
			return handle;
		}
		String key=MemcachedEnum.OrderWithItem.getKey()+orderId;
		OrdOrder ordOrder=MemcachedUtil.getInstance().get(key);
		if(ordOrder==null){
			ordOrder=iOrdOrderService.loadOrderWithItemByOrderId(orderId);
			if(ordOrder==null){
				return handle;
			}
			MemcachedUtil.getInstance().set(key, MemcachedEnum.OrderWithItem.getSec(), ordOrder);
			handle.setReturnContent(ordOrder);
		}else{
			OrdOrder newordOrder=iOrdOrderService.findByOrderId(orderId);
			if(newordOrder!=null){
				newordOrder.setOrderItemList(ordOrder.getOrderItemList());
				handle.setReturnContent(newordOrder);
			}else{
				handle.setReturnContent(ordOrder);
			}
		}
		return handle;
	}
    /**
	 * 更新子订单退款信息
	 * @param orderItemList
	 */
	@Override
	public ResultHandle updateOrderItemRefundQutityAndPrice(List<OrdOrderItem> orderItemList) {
		LOG.info("start updateOrderItemRefundQutityAndPrice");
		ResultHandle handle = new ResultHandle();
		List<Long> orderItemIdList  = new ArrayList<Long>();
		for(OrdOrderItem orderItem : orderItemList){
			orderItemIdList.add(orderItem.getOrderItemId());
		}
		List<OrdOrderItem> dbOrderItemList = iOrdOrderItemService.selectOrderItemsByIds(orderItemIdList);
		for(OrdOrderItem paramOrderItem : orderItemList){
			if(paramOrderItem.getRefundQuantity()==null){
				handle.setMsg("子订单的退款数量不能为空");
				return handle;
			}
			for(OrdOrderItem dbOrderItem : dbOrderItemList){
				LOG.info("dbOrderItemId="+dbOrderItem.getOrderItemId()+",dbOrderRefundQuantity="+dbOrderItem.getRefundQuantity()+",dbOrderItemQuantity="+dbOrderItem.getQuantity()+","
						+ "dbOrderItemPrice="+dbOrderItem.getPrice()+",paramOrderItemId="+paramOrderItem.getOrderItemId()+",paramOrderItemRefundQuantity="+paramOrderItem.getRefundQuantity());
				if(dbOrderItem.getOrderItemId().longValue() == paramOrderItem.getOrderItemId().longValue()){
					//总共退款份数= 参数传入份数+之前退款份数
					Long dbrefundQuantity = 0l;
					if (dbOrderItem.getRefundQuantity() != null) {
						dbrefundQuantity = dbOrderItem.getRefundQuantity();
					} 
					Long totalRefundQuantity = paramOrderItem.getRefundQuantity() + dbrefundQuantity;
					LOG.info("totalRefundQuantity="+totalRefundQuantity);
					if(totalRefundQuantity > dbOrderItem.getQuantity()){
						String msg = "子订单(" +paramOrderItem.getOrderItemId() + ")的退款退款数量("+totalRefundQuantity+")大于当前子订单数量("+dbOrderItem.getRefundQuantity() + ")";
						LOG.info(msg);
						handle.setMsg(msg);
						return handle;
					}else{
						paramOrderItem.setRefundQuantity(totalRefundQuantity);
					}
					//LOG.info("dbOrderItemRefundPrice="+dbOrderItem.getRefundPrice()+",paramOrderItemRefundPrice="+paramOrderItem.getRefundPrice()+",dbOrderItemPrice="+dbOrderItem.getPrice());
					
					
					Long totalRefundPrice_ = totalRefundQuantity * dbOrderItem.getPrice();
					/*Long totalRefundPrice = paramOrderItem.getRefundPrice() + dbOrderItem.getRefundPrice();
					if(totalRefundPrice_ > dbOrderItem.getPrice()){
						String msg = "子订单(" +paramOrderItem.getOrderItemId() + ")的退款退款金额("+totalRefundPrice_+")大于当前子订单金额("+dbOrderItem.getPrice() + ")";
						LOG.info(msg);
						handle.setMsg(msg);
						return handle;
					}else{
						
					}*/
					paramOrderItem.setRefundPrice(totalRefundPrice_);
					
					LOG.info("paramRefundPrice="+totalRefundPrice_+",paramRefundQuantity="+totalRefundQuantity);
				}
			}
		}
		orderUpdateService.updateOrderItemRefundQutityAndPrice(orderItemList);
		return handle;
	}
	/**
 	 * 根据OrderId获取对应的子订单列表
 	 * @param orderId
 	 * @return
 	 */
	@Override
	public ResultHandleT<List<OrdOrderItem>> getOrderItemListByOrderId(Long orderId) {
		ResultHandleT<List<OrdOrderItem>> result=new ResultHandleT<List<OrdOrderItem>>(); 
		List<OrdOrderItem> orderItemList = iOrdOrderItemService.selectByOrderId(orderId);
		if(CollectionUtils.isEmpty(orderItemList)){
			result.setMsg("此OrderId("+orderId+")查询不到对应的OrderItemList");
		}
		
		result.setReturnContent(orderItemList);
		return result;
		
	}
    
    /**
 	 * 提供给目的地：返回所有门票子订单对接申码未成功、向供应商下单未成功的订单
 	 * （只要有一个名票子订单下单成功,则该订单状态为成功）
 	 * @param orderIds
 	 * @return
 	 */
	@Override
	public ResultHandleT<List<Long>> getFailTicketOrderIds(
			List<Long> orderIds) {
		LOG.info("getFailTicketOrderIds start:"+orderIds);
		
		ResultHandleT<List<Long>> handle = new ResultHandleT<List<Long>>();
		if(orderIds == null || orderIds.isEmpty()){
			handle.setMsg("orderIds不能为空");
			return handle;
		}
		if(orderIds.size() >50){
			handle.setMsg("orderIds个数不能超过50");
			return handle;
		}
		List<Long> failOrderIds =  new ArrayList<Long>();
        try {
			for (Long orderId : orderIds) {
				List<OrdOrderItem> orderItems = iOrdOrderItemService.selectByOrderId(orderId);
				Boolean hasQrcode = false;
				Boolean hasEbkOrFax = false;
				Boolean hasNoCheck =  false;//是否包含通知方式全不勾选
				for (OrdOrderItem orderItem : orderItems) {
					String ebkFlag = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.ebk_flag.name());
					String faxFlag = orderItem.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.fax_flag.name());					
					if (OrderUtils.isTicketByCategoryId(orderItem.getCategoryId())) {
						String noticeType = orderItem.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.notify_type.name());
						if (!"QRCODE".equals(noticeType) && !"Y".equals(ebkFlag) && !"Y".equals(faxFlag)) {
							hasNoCheck =  true;
							break; 
						}
						if ("QRCODE".equals(noticeType)) {
							hasQrcode = true; //包含对接申码
						}
						//子订单凭证判断是否生成
						if(StringUtils.isNotEmpty(faxFlag)||StringUtils.isNotEmpty(ebkFlag)){
							if ("Y".equals(ebkFlag) || "Y".equals(faxFlag)) {
								hasEbkOrFax = true; //包含ebk或fax
							}
						}
					}else{
						hasEbkOrFax = true;						
					}

										
				}
				if(hasNoCheck){
					continue;//子订单中包含通知方式全不勾选，则订单状态为成功
				}
				if (hasQrcode) {
					String qrcodeStr = passportServiceRemote.queryCodeStatusByOrderId(orderId);
					if (qrcodeStr.contains("APPLIED_SUCCESS")) {
						continue; //子订单中包含申码成功，则订单状态为成功
					}
				}
				if (hasEbkOrFax) {
					Boolean certifFlag = ebkCertifClientService.isExistCertifByOrder(orderId);
					if (certifFlag) {
						continue; //向供应商下单成功，则订单状态为成功
					}
				}
				failOrderIds.add(orderId);
			}
			LOG.info("getFailTicketOrderIds return:"+failOrderIds);
			handle.setReturnContent(failOrderIds);
			
		} catch (Exception e) {
			LOG.error("getFailTicketOrderIds error:"+e.getMessage());
			handle.setMsg(e.getMessage());
		}
		return handle;
	}
    
    
    @Override
	public OrdPayPromotion queryOrdPayPromotionByOrderId(Long orderId) {
		return ordPayPromotionService.queryOrdPayPromotionByOrderId(orderId);
	}
    
    /**
	 * 判断订单是否支持一键重下
	 * 目前根据品类判断
	 *
	 * @param order
	 */
	@Override
	public Boolean isOrderSupportOneKeyRecreate(OrdOrder order,ProdProduct product) {
		if(order == null){
			return Boolean.FALSE;
		}
		Long categoryId = product.getBizCategory().getCategoryId();
		Long subCategoryId = product.getSubCategoryId();

		LOG.info("Now judge whether order " + order.getOrderId() + " supports recreate, category id is " + categoryId + ", subCategoryId is " + subCategoryId);
		if(categoryId == null || categoryId < 0){
			LOG.info("Order " + order.getOrderId() + " category id is invalidate, does not support one key recreate");
			return Boolean.FALSE;
		}

		//跟团游和当地游支持
		if(BizEnum.BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().longValue()==categoryId.longValue()
				|| BizEnum.BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().longValue()==categoryId.longValue()){
			return Boolean.TRUE;
		}
		//自由行，需要判断子类型，交通+服务、机+酒，两种子品类支持，其它不支持
		if(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId().longValue()==categoryId.longValue()) {
			if(subCategoryId != null && subCategoryId >= 0){
				if(BizEnum.BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().longValue() == subCategoryId.longValue()
						|| BizEnum.BIZ_CATEGORY_TYPE.category_route_traffic_service.getCategoryId().longValue() == subCategoryId.longValue()
						|| BizEnum.BIZ_CATEGORY_TYPE.category_route_scene_hotel.getCategoryId().longValue() == subCategoryId.longValue()){
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}
	
	@Override
	public Map<String,Object> queryTicketOrderItemByOrderId(Map<String, Object> params) {
		List<OrdOrderItem> ordOrderItemList = null;
		OrdOrderItem ordOrderItem = null;
		Map<String, Object> map = new HashMap<String, Object>();
		List<OrderPayLousVo> orderPayLousVoList = (List<OrderPayLousVo>) params.get("orderPayLousVoList");
		Float total = 0f; // 总占比金额
		List<Long> orderIds = new ArrayList<Long>();
		Float totalBaiTiaoAmount = 0.0f;
		if (CollectionUtils.isNotEmpty(orderPayLousVoList)) {
			for (OrderPayLousVo orderPayLousVo : orderPayLousVoList) {
				total = 0f; 
				map.put("userNo", params.get("userNo"));
				map.put("orderId", orderPayLousVo.getOrderId());
				if(map.get("userNo")==null||map.get("userNo")==null){
					LOG.info("orderPayLousVo userNo:"+map.get("userNo")+",orderId"+map.get("orderId"));
					map.put("msg", "传参有误！");
					break;
				}
				orderIds.add(orderPayLousVo.getOrderId());
				ordOrderItemList = iOrdOrderItemService.queryTicketOrderItemByOrderId(map);
				Long totalAmount = 0L; // 子订单的总和，求子订单占比使用
				Long totalTicketAmount = 0L; // 子订单中总和
				if(CollectionUtils.isNotEmpty(ordOrderItemList)){
					Iterator<OrdOrderItem> it = ordOrderItemList.iterator();
					while (it.hasNext()) {
						ordOrderItem = it.next();
						totalAmount += ordOrderItem.getPrice() * ordOrderItem.getQuantity();
						if (ordOrderItem.getCategoryId() == 11L
								|| ordOrderItem.getCategoryId() == 12L
								|| ordOrderItem.getCategoryId() == 13L
								|| ordOrderItem.getCategoryId() == 31L) { // 如果属于门票,则累加出金额
							totalTicketAmount += ordOrderItem.getPrice() * ordOrderItem.getQuantity();
						} else {
							it.remove();
						}
					}
				}
				if (totalAmount != 0) {
					total = Float.valueOf(totalTicketAmount) /Float.valueOf(totalAmount); // 计算门票订单所占比例
					if(ordOrderItem.getActualAmount()!=null){
						totalBaiTiaoAmount =totalBaiTiaoAmount+total*Float.valueOf(ordOrderItem.getActualAmount().toString())*orderPayLousVo.getBaitiaoPayProp(); //计算白条占比总金额
					}
				}
			}
		}
		map.clear();
		map.put("totalBaiTiaoAmount", totalBaiTiaoAmount);
		map.put("orderIds", orderIds);
		return map;
	}

	@Override
	public List<OrdOrder> getOrderInfoByVisitTimeAndCat(Map<String, Object> paramMap) {
		return ordOrderDao.getOrderInfoByVisitTimeAndCat(paramMap);
	}
	
	@Override
	public ResultHandleT<BuyPresentActivityInfo> findPromBuyPresent(
			com.lvmama.vst.comm.vo.order.OrdOrderDTO order) {
		ResultHandleT<BuyPresentActivityInfo>  handle = null;
		try {
			promBuyPresentforClient.findPromBuyPresent(order);
		} catch (Exception e) {
			handle.setMsg("促销价格计算查询失败");
		}
		return handle;
	}


	@Override
	public ResultHandleT<List<PromPromotion>> vstFindPromPromotionClient(OrdOrderPrice ordOrderPrice) {
		ResultHandleT<List<PromPromotion>>  handle = new ResultHandleT<List<PromPromotion>>();
		try {
			List<PromPromotion> promList = orderPriceService.vstFindPromPromotion(ordOrderPrice);
			handle.setReturnContent(promList);
		} catch (Exception e) {
			handle.setMsg("促销信息查询失败");
		}
		return handle;
	}
    
	@Override
	public Boolean isIdNoRepeat(BuyInfo buyInfo) {
        OrdOrder temOrder = this.bookService.initOrderAndCalc(buyInfo);
        int productOrderItemCount=0;
        LOG.info("isIdNoRepeat orderItemSize:" + temOrder.getOrderItemList().size());
        if (temOrder.getOrderItemList() != null) {
    	    for(OrdOrderItem ordOrderItem: temOrder.getOrderItemList()){
    	    	if(ordOrderItem.getCategoryId()==BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId()||ordOrderItem.getCategoryId()==BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId()||ordOrderItem.getCategoryId()==BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId()){
    	    			productOrderItemCount++;
    	    	}
    	    }
    	    LOG.info("isIdNoRepeat 商品子订单个数:"+productOrderItemCount);
            List<OrdPerson> ordTravellerList = temOrder.getOrdTravellerList();
            if (CollectionUtils.isEmpty(ordTravellerList)) {
                return false;
            }
            if(productOrderItemCount!=1){
            	return false;
            }
            LOG.info("isIdNoRepeat 游玩人信息:" + ordTravellerList.toString());
            Map<String, Set<String>> idMap = new HashMap<String, Set<String>>();
            for (OrdPerson ordPerson : ordTravellerList) {
                String idType = ordPerson.getIdType();
                String idNo = ordPerson.getIdNo();
                if (StringUtils.isEmpty(idNo) || StringUtils.isEmpty(idType)) {
                    continue;
                }
                Set<String> idNoSet = idMap.get(idType);
                if (idNoSet == null) {
                    idNoSet = new HashSet<String>();
                    idMap.put(idType, idNoSet);
                }
                if (!idNoSet.add(idNo)) {
                    return true;
                }
            }
        }
        return false;
    }


	/**
     * @Description: 单酒店 前台下单 保存发票申请信息， 用于入住后24小数 自动申请发票 
     * @author Wangsizhi
     * @date 2016-10-24 下午5:19:43
     */
    @Override
    public ResultHandle saveComplexApplyInvoiceInfo(Map<String, String> paramMap) { 
        
        ResultHandle resultHandle = new ResultHandle();
        String orderId = paramMap.get("orderId");
        try {
        	
            LOG.info("------单酒店 前台下单  保存发票申请信息----开始,订单ID-----------saveOrdApplyInvoiceInfo----:" + orderId);
        	ordApplyInvoiceInfoService.saveOrdApplyInvoiceInfo(orderId,paramMap);
        	LOG.info("------单酒店 前台下单  保存发票申请信息----成功,订单ID-----------saveOrdApplyInvoiceInfo---:" + orderId);
        } catch (Exception e) {
            resultHandle.setMsg("保存发票信息失败");
            LOG.info("------单酒店 前台下单  保存发票申请信息----异常,订单ID-----------saveOrdApplyInvoiceInfo--:" + orderId);
            LOG.error("{}", e);
        }
        return resultHandle;
    }
    
    
    /**
     * @Description:根据用户id获取发票填充信息
     * @author wangyongfang
     * @date 2016-10-24 下午5:19:43
     */
    @Override
    public List<OrderpersonInvoiceInfoAddress> findInvoicePersonInfo(Map<String, Object> paramMap) { 
    	
    	List<OrderpersonInvoiceInfoAddress>  ordPersonList = null;
    	try {
    	      if(paramMap.isEmpty()) {
    	          LOG.info("OrdOrderClientServiceImpl.findInvoicePersonInfo map is null paramMap="+ paramMap);
    	          return null;
              }
    	    
    	      JSONObject mapStr = JSONObject.fromObject(paramMap);
    	      LOG.info("OrdOrderClientServiceImpl.findInvoicePersonInfo paramMap="+ mapStr.toString());
    	      
    	      String userId = (String) paramMap.get("userId");
    	      if (StringUtils.isBlank(userId)) {
    	          LOG.info("OrdOrderClientServiceImpl.findInvoicePersonInfo userId is null userId="+ userId);
                  return null;
    	      }
    	      
    	      LOG.info("OrdOrderClientServiceImpl.findInvoicePersonInfo userId="+ userId + "---paramMap" + mapStr.toString());
    	      
    	      ordPersonList = ordApplyInvoiceInfoService.findInvoicePersonInfo(paramMap);
    	      LOG.info("------select----OrderpersonInvoiceInfoAddress------:"+ordPersonList.size());
    	      LOG.info("OrdOrderClientServiceImpl.findInvoicePersonInfo userId="+ userId + "---ordPersonList.get(0)="+ordPersonList.get(0).toString());
    	 } catch (Exception e) {
    		 LOG.info(e.getMessage());
    		 LOG.info("------select----OrderpersonInvoiceInfoAddress------:");
    	 } 
    	return ordPersonList;
    	
    } 	

	@Override
	public void sendMessage(Message message) {
		orderMessageProducer.sendMsg(message);
	}
	
	@Override
	public ResultHandleT<Integer> getOrderItemCountByConditionsInSingleTable(Map<String, Object> params) {
		ResultHandleT<Integer> handle = new ResultHandleT<Integer>();
		try {
			handle.setReturnContent(iOrdOrderItemService.getTotalCount(params));
		} catch(Exception e) {
			LOG.error("Error occurred while getting the order item count by condition in a single table", e);
			handle.setMsg("单表获取子订单数出错");
		}
		return handle;
	}

	@Override
    public List<Long> queryStampOrderIds(int startBeforeDays) {
	    // select * from ord_order where order_subtype is not null;
		// STAMP:券订单，STAMP_PROD:券兑换的商品订单
		return ordOrderDao.queryStampOrderIds(startBeforeDays);
    }
    
    @Override
    public ResultHandleT<List<OrdTravAdditionConf>> queryOrdTravAdditionConfByParam(
            Map<String, Object> param) {
        ResultHandleT<List<OrdTravAdditionConf>> handle = new ResultHandleT<List<OrdTravAdditionConf>>();
        try {
            handle.setReturnContent(ordTravAdditionConfService.queryOrdTravAdditionConfByParam(param));
        } catch(Exception e) {
            LOG.error("Error occurred while getting the travAddtionConf info by condition in a single table", e);
            handle.setMsg("根据条件查询出游人补全表单配置记录出错");
        }
        return handle;
        
        
    }
    
    @Override
    public ResultHandleT<Integer> saveTravAdditionConf(OrdTravAdditionConf ordTravAdditionConf) {
        ResultHandleT<Integer> handle = new ResultHandleT<Integer>();
        try {
            handle.setReturnContent(ordTravAdditionConfService.saveTravAdditionConf(ordTravAdditionConf));
        } catch(Exception e) {
            LOG.error("Error occurred while save the travAddtionConf orderId:"+ordTravAdditionConf.getOrderId(), e);
            handle.setMsg("保存出游人补全表单配置出错");
        }
        return handle;
        
    }
    
    @Override
    public ResultHandleT<Integer> updateTravAdditionConf(OrdTravAdditionConf ordTravAdditionConf) {
        ResultHandleT<Integer> handle = new ResultHandleT<Integer>();
        try {
            handle.setReturnContent(ordTravAdditionConfService.updateTravAdditionConf(ordTravAdditionConf));
        } catch(Exception e) {
            LOG.error("Error occurred while update the travAddtionConf orderId:"+ordTravAdditionConf.getOrderId(), e);
            handle.setMsg("更新出游人补全表单配置出错");
        }
        return handle;
        
    }
    
    
    
	/**
	 * @desc 订单附件有效状态更改
	 * @param param
	 * @return
	 */
	@Override
	public int updateOrderAttachmentFlag(Map<String, Object> param) {
		return orderAttachmentService.updateOrderAttachmentFlag(param);
	}
	@Override
	public void newOrdOrderDownpayMessage(Long orderId, String addition) {
			LOG.info("----------newOrdOrderDownpayMessage:"+orderId+"addition:"+addition);
			orderMessageProducer.sendMsg(MessageFactory.newOrdOrderDownpayMessage(orderId, addition));
	}


	@Override
	public boolean doProcessFlightWorkFlow(OrdOrder order) {
		LOG.info("==doProcessFlightWorkFlow===enter==");
		if(BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())){
			//判断是否含酒店机票的订单，发起流程
			List<OrdOrderItem> hotelOrderItem=new ArrayList<OrdOrderItem>();
			List<OrdOrderItem> flightItem=new ArrayList<OrdOrderItem>();
			//判断订单是否包含机票
			for (OrdOrderItem item : order.getOrderItemList()) {
				if(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()==item.getCategoryId().longValue()){
					flightItem.add(item);
				}else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==item.getCategoryId().longValue()){
					hotelOrderItem.add(item);
				}
			}
			LOG.info("==doProcessFlightWorkFlow=orderId:"+order.getOrderId()+"==order.isPayMentType():"+order.isPayMentType()
			+",order.isIncludeFlightHotel:"+order.isIncludeFlightHotel()+",order.isHotelCerConfirmStatus():"+order.isHotelCerConfirmStatus());
			//订单包含对接机票，非预授权,酒店凭证通过
			if(order.isHotelCerConfirmStatus()&&!order.isPayMentType()&&order.isIncludeFlightHotel()){
				ActivitiKey activitiKey = null;
				if(ActivitiUtils.hasActivitiOrder(order)){
					if(!order.hasCanceled()){
						for (OrdOrderItem ordOrderItem : flightItem) {
							activitiKey = createKeyByOrderItem(ordOrderItem, "APPROVE");
							LOG.info("processerClientService.completeTask,activitiKey:"+activitiKey);
							processerClientService.completeTask(activitiKey, "usertask4", "USER");
							LOG.info("processerClientService.completeTask success.");
						}
						return true;
					}
				}
			}
			
		}
		return false;
	}
	@Override
	public void updateOrderResourcePassAmple(Long orderId) {
		OrdOrder order=this.queryOrdorderByOrderId(orderId);
		if(!order.hasResourceAmple()&&order.isHotelCerConfirmStatus()){
			boolean isResourceAmple=true;
			for (OrdOrderItem item : order.getOrderItemList()) {
				if(!item.hasResourceAmple()){
					isResourceAmple=false;
				}
			}
			if(isResourceAmple){
				order.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.name());
				order.setResourceAmpleTime(new Date());
				this.updateOrdOrder(order);
				//发送消息给驴途
				sendResourceStatusAmpleMsg(order.getOrderId());
				prepayAmpleVst(order.getOrderId(),order);
			}
		}
		
	}

	
	
	@Override
	public ResultHandleT<HotelOrderRebate> calcHotelRebate(HotelOrderRebate hotelRebate) {
		ResultHandleT<HotelOrderRebate> handle = new ResultHandleT<HotelOrderRebate>();
		HotelOrderRebate vstHotelRebate = null;
		try
		{
			vstHotelRebate = hotelRebateBussiness.calcHotelRebate(hotelRebate);
		}catch(Exception e)
		{
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		
		if(null == vstHotelRebate){
			handle.setMsg("返现计算异常!");		
		}else{
			handle.setReturnContent(vstHotelRebate);
		}
		
		return handle;
	}

	
	@Override
	public ResultHandle invokeInterfacePlatform() {
		LOG.info("start create supplier order:");
		List<Long> orderIds = this.complexQueryService.findNeedCreateSupplierOrders();
		
		if(!CollectionUtils.isEmpty(orderIds)) {
			for(Long orderId : orderIds) {
				LOG.info("start process order:" + orderId);
				try {
					OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
					if(order == null) {
						LOG.error("can not find the order:" + orderId);
						continue;
					}
					
					if(order.hasCanceled()) {
						LOG.error("order has been canceled:" + orderId);
						continue;
					}
					
					if(OrderEnum.INVOKE_INTERFACE_PF_STATUS.CREATED.name().equals(order.getInvokeInterfacePfStatus())) {
						LOG.error("order has been created supplier order:" + orderId);
						continue;
					}
					
					List<OrdOrderItem> list = orderUpdateService.queryOrderItemByOrderId(orderId);
					Set<Long> set = new HashSet<Long>();
					for(Iterator<OrdOrderItem> it = list.iterator(); it.hasNext(); ){
						OrdOrderItem orderItem = it.next();
						if(orderItem.isSupplierOrderItem() && !OrderEnum.INVOKE_INTERFACE_PF_STATUS.CREATED.name().equals(orderItem.getInvokeInterfacePfStatus())) {
							set.add(orderItem.getOrderItemId());
						}
					}
					OrderSupplierOperateResult result = supplierOrderOperator.createSupplierOrder(orderId,set);
				} catch (Exception e) {
					LOG.error("create supplier order failed, orderId:" + orderId);
					LOG.error("{}", e);
				}
				
				LOG.info("end process order:" + orderId);
			}
		}
		return null;
	}
	
	private void processerHotelflow(OrdOrder order){
		List<OrdOrderItem> hotelOrderItem=new ArrayList<OrdOrderItem>();
		List<OrdOrderItem> flightItem=new ArrayList<OrdOrderItem>();
		boolean isOtherResourceProcesser=true;
		boolean isFlightResourcePass=true;
		//判断订单是否包含机票
		for (OrdOrderItem item : order.getOrderItemList()) {
			if(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()==item.getCategoryId().longValue()){
				flightItem.add(item);
				if(!OrderEnum.RESOURCE_STATUS.AMPLE.getCode().equals(item.getResourceStatus())){
					isFlightResourcePass=false;
				}
			}else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==item.getCategoryId().longValue()){
				hotelOrderItem.add(item);
			}else if(!item.hasResourceAmple()){
				isOtherResourceProcesser=false;
				break;
			}
		}
		LOG.info("==orderId:"+order.getOrderId()+",order.getBuCode():"+order.getBuCode()+
				",isFlightResourcePass:"+isFlightResourcePass+",isOtherResourceProcesser:"+isOtherResourceProcesser);
		if(Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())&&isFlightResourcePass&&isOtherResourceProcesser&&order.isIncludeFlightHotel()){
			ActivitiKey activitiKey = null;
			if(ActivitiUtils.hasActivitiOrder(order)){
				LOG.info("==orderId:"+order.getOrderId()+",hasActivitiOrder:true");
				if(!order.hasCanceled()){
					for (OrdOrderItem ordOrderItem : hotelOrderItem) {
						activitiKey = createKeyByOrderItem(ordOrderItem, "APPROVE");
						LOG.info("processerClientService.completeTaskByAudit,activitiKey:"+activitiKey);
						processerClientService.processerTask(activitiKey, "receivetask1", "USER");
						LOG.info("processerClientService.completeTaskByAudit success.");
					}
					
				}
			}
		}
	}
	
	private void processerHotelflowNew(OrdOrder order){
		List<OrdOrderItem> hotelOrderItem=new ArrayList<OrdOrderItem>();
		List<OrdOrderItem> flightItem=new ArrayList<OrdOrderItem>();
		boolean isOtherResourceProcesser=true;
		boolean isFlightResourcePass=true;
		//判断订单是否包含机票
		for (OrdOrderItem item : order.getOrderItemList()) {
			if(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId().longValue()==item.getCategoryId().longValue()&&!item.isApiFlightTicket()){
				flightItem.add(item);
				if(!OrderEnum.RESOURCE_STATUS.AMPLE.getCode().equals(item.getResourceStatus())){
					isFlightResourcePass=false;
				}
			}else if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().longValue()==item.getCategoryId().longValue()
					||BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId().longValue()==item.getCategoryId().longValue()
					||BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().longValue()==item.getCategoryId().longValue()){
				hotelOrderItem.add(item);
			}else if(!item.hasResourceAmple()){
				isOtherResourceProcesser=false;
				break;
			}
		}
		LOG.info("==orderId:"+order.getOrderId()+",order.getBuCode():"+order.getBuCode()+
				",isFlightResourcePass:"+isFlightResourcePass+",isOtherResourceProcesser:"+isOtherResourceProcesser);
		if(Constant.BU_NAME.LOCAL_BU.getCode().equals(order.getBuCode())&&isFlightResourcePass&&isOtherResourceProcesser&&order.isIncludeFlightHotel()){
			ActivitiKey activitiKey = null;
			if(ActivitiUtils.hasActivitiOrder(order)){
				LOG.info("==orderId:"+order.getOrderId()+",hasActivitiOrder:true");
				if(!order.hasCanceled()){
					for (OrdOrderItem ordOrderItem : hotelOrderItem) {
						activitiKey = createKeyByOrderItem(ordOrderItem, "APPROVE");
						LOG.info("processerClientService.completeTaskByAudit,activitiKey:"+activitiKey);
						processerClientService.processerTask(activitiKey, "receivetask1", "USER");
						LOG.info("processerClientService.completeTaskByAudit success.");
					}
					
				}
			}
		}
	}
	
	/*
	 * @Description: 目的地 酒店套餐 酒+景 自由行 意外险游玩人后置，游玩人补充 
     * orderPerson有ordPersonId为补充已有游玩人信息，无ordPersonId为新加游玩人.
     * 补充游玩人信息，同时更新后置信息表（OrdTravAdditionConf）
     * @author Wangsizhi
     * @date 2016-11-23 上午11:02:29
     */
    @Override
    public ResultHandleT<Boolean> supplyDestBuAccTrav(OrdPerson ordPerson) {
        ResultHandleT<Boolean> result = new ResultHandleT<Boolean>();
        if (null == ordPerson) {
            result.setMsg("无游玩人可以补充");
            return result;
        }
        
        LOG.info("supplyDestBuAccTrav ordPerson " + ordPerson.toString());
        
        String objectType = ordPerson.getObjectType();
        Long objectId = ordPerson.getObjectId();
        Long orderId = null;
        String personType = ordPerson.getPersonType();
        if (StringUtils.isNotBlank(objectType) && OrderEnum.SETTLEMENT_TYPE.ORDER.name().equals(objectType)
                && StringUtils.isNotBlank(personType) && OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equals(personType)
                ) {
            orderId = objectId;
        }else {
            result.setMsg("非订单联系人");
            return result;
        }
        
        if (null != orderId) {
            OrdAccInsDelayInfo accInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(orderId);
            if (null != accInsDelayInfo) {
                String travDelayFlag = accInsDelayInfo.getTravDelayFlag();
                String travDelayStatus = accInsDelayInfo.getTravDelayStatus();
                if (StringUtils.isNotBlank(travDelayFlag) && "Y".equalsIgnoreCase(travDelayFlag)) {
                    if (StringUtils.isNotBlank(travDelayStatus) && 
                            (travDelayStatus.equalsIgnoreCase(OrderEnum.ORDER_TRAV_DELAY_STATUS.COMPLETED.name()) 
                                || travDelayStatus.equalsIgnoreCase(OrderEnum.ORDER_TRAV_DELAY_STATUS.ABANDON.name()))
                            ) {
                        result.setMsg("已弃保或者以完成补全");
                        return result;
                    }
                }
            }
        }
        
        Long ordPersonId = ordPerson.getOrdPersonId();
        
        LOG.info("ordPersonId = " + ordPersonId);
        
        if (null != ordPersonId) {
            /*补充已有游玩人*/
            
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("orderId", orderId);
            param.put("orderPersonId", ordPersonId);
            param.put("valid", "Y");
            
            LOG.info("orderId = " + orderId);
            LOG.info("ordPersonId = " + ordPersonId);
            
            
            List<OrdTravAdditionConf> ordTravAdditionConfList = ordTravAdditionConfService.queryOrdTravAdditionConfByParam(param);
            
            if (null == ordTravAdditionConfList || ordTravAdditionConfList.size() == 0) {
                LOG.info("ordTravAdditionConfList is null ");
            }
            
            for (OrdTravAdditionConf ordTravAdditionConf : ordTravAdditionConfList) {
                LOG.info(ordTravAdditionConf.toString());
            }
            
            
            if (null != ordTravAdditionConfList && ordTravAdditionConfList.size() > 0) {
                OrdTravAdditionConf ordTravAdditionConf = ordTravAdditionConfList.get(0);
                /*电话号码*/
                String yes = "Y";
                String phoneNum = ordTravAdditionConf.getPhoneNum();
                if (StringUtils.isNotBlank(phoneNum) && yes.equals(phoneNum)) {
                    String phone = ordPerson.getMobile();
                    if (StringUtils.isBlank(phone)) {
                        LOG.info("游玩人手机号不能为空, orderId = " + orderId + "  ordPersonId = " + ordPersonId);
                        result.setMsg("游玩人手机号不能为空");
                        return result;
                    }
                    LOG.info("phone = " + phone);
                    
                }
                /*英文名*/
                String enName = ordTravAdditionConf.getEnName();
                if (StringUtils.isNotBlank(enName) && yes.equals(enName)) {
                    String firstName = ordPerson.getFirstName();
                    String lastName = ordPerson.getLastName();
                    if (StringUtils.isBlank(firstName) || StringUtils.isBlank(lastName))  {
                        if (StringUtils.isBlank(firstName)) {
                            LOG.info("游玩人英文名不能为空, orderId = " + orderId + "  ordPersonId = " + ordPersonId);
                            result.setMsg("游玩人英文名不能为空");
                            return result;
                        }
                        if (StringUtils.isBlank(lastName)) {
                            LOG.info("游玩人英文姓不能为空, orderId = " + orderId + "  ordPersonId = " + ordPersonId);
                            result.setMsg("游玩人英文姓不能为空");
                            return result;
                        }
                    }
                    LOG.info("firstName = " + firstName);
                    LOG.info("lastName = " + lastName);
                }
                /*邮箱*/
                String emailNum = ordTravAdditionConf.getEmail();
                if (StringUtils.isNotBlank(emailNum) && yes.equals(emailNum)) {
                    String email = ordPerson.getEmail();
                    if (StringUtils.isBlank(email)) {
                        LOG.info("游玩人邮箱不能为空, orderId = " + orderId + "  ordPersonId = " + ordPersonId);
                        result.setMsg("游玩人邮箱不能为空");
                        return result;
                    }
                    LOG.info("email = " + email);
                }
                /*人群*/
                String occupNum = ordTravAdditionConf.getOccup();
                if (StringUtils.isNotBlank(occupNum) && yes.equals(occupNum)) {
                    String occup = ordPerson.getPeopleType();
                    if (StringUtils.isBlank(occup)) {
                        LOG.info("游玩人人群类型(成人|儿童|老年人)不能为空, orderId = " + orderId + "  ordPersonId = " + ordPersonId);
                        result.setMsg("游玩人人群类型(成人|儿童|老年人)不能为空");
                        return result;
                    }
                    LOG.info("occup = " + occup);
                }
                /*证件*/
                String idTypeNum = ordTravAdditionConf.getIdType();
                if (StringUtils.isNotBlank(idTypeNum) && yes.equals(idTypeNum)) {
                    String idType = ordPerson.getIdType();
                    String idNo = ordPerson.getIdNo();
                    if (StringUtils.isBlank(idType) || StringUtils.isBlank(idNo)) {
                        LOG.info("游玩人证件不能为空, orderId = " + orderId + "  ordPersonId = " + ordPersonId);
                        result.setMsg("游玩人证件不能为空");
                        return result;
                        }
                    LOG.info("idType = " + idType);
                    }
                /*更新ordPerson表，补全信息*/
                iOrdPersonService.updateByPrimaryKeySelective(ordPerson);
                /*更新ordTravAdditionConf表，置词条补全记录为无效*/
                ordTravAdditionConf.setValid("N");
                LOG.info("ordTravAdditionConf " + ordTravAdditionConf.toString());
                ordTravAdditionConfService.updateTravAdditionConf(ordTravAdditionConf);
                result.setReturnContent(true);
            }else {
                LOG.info("游玩人不需要补充, orderId = " + orderId + "  ordPersonId = " + ordPersonId);
                result.setMsg("游玩人不需要补充");
                return result;
            }
        }
        
        /*检测是否补全该订单所需所有后置的游玩人  开始*/
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("orderId", ordPerson.getObjectId());
        param.put("valid", "Y");
        List<OrdTravAdditionConf> ordTravAdditionConfs = ordTravAdditionConfService.queryOrdTravAdditionConfByParam(param);
        
        if (null == ordTravAdditionConfs || ordTravAdditionConfs.size() == 0) {
            LOG.info("ordTravAdditionConfs is null");
            OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(ordPerson.getObjectId());
            ordAccInsDelayInfo.setTravDelayStatus(OrderEnum.ORDER_TRAV_DELAY_STATUS.COMPLETED.name());
            //更新订单后置状态为已完成
            ordAccInsDelayInfoService.updateOrdAccInsDelayInfo(ordAccInsDelayInfo);
            
            OrdOrder order = queryOrdorderByOrderId(orderId);
            /*启动工作流，推送意外险子单*/
            this.handleTravDelayOrder(order);
        } else {
            for (OrdTravAdditionConf ordTravAdditionConf2 : ordTravAdditionConfs) {
                LOG.info("ordTravAdditionConf2 " + ordTravAdditionConf2.toString());
            }
        }
        /*检测是否补全该订单所需所有后置的游玩人  结束*/
        
        return result;
        
    }

    /**
     * @Description: 目的地游玩人后置，游玩人补充等待时间设置 
     * @author Wangsizhi
     * @date 2016-12-1 下午7:47:05
     */
    private void initDestBuTravDelayWaitTime(OrdOrder order) {
    	LOG.info("initDestBuTravDelayWaitTimeOrderId" + order.getOrderId());
    	
        OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(order.getOrderId());
        
        if (null != ordAccInsDelayInfo) {
           // String tag0 = "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%";
            String tag = "";//tag0 + "\n" + tag0 + "\n" + tag0 + "\n" + tag0 + "\n" + tag0 + "\n" + tag0 + "\n";
            
            LOG.info(tag + "initDestBuTravDelayWaitTime");
            LOG.info("travDelayFlag = " + ordAccInsDelayInfo.getTravDelayFlag());
            LOG.info("travDelayStatus = " + ordAccInsDelayInfo.getTravDelayStatus());
            
            if (OrdOrderUtils.isDestBuTravDelayedOrder(ordAccInsDelayInfo)) {
                /*设置补充游玩人等待时间*/
                Long orderId = order.getOrderId();
                Date travDelayWaitTime = null;
                Date lastAheadTime = calcOrderAheadTime(orderId);
                LOG.info("OrderId =" + order.getOrderId() + " LastAheadTime =" + lastAheadTime  + " order.getPaymentTime =" + order.getPaymentTime());
                if (null != lastAheadTime) {
                	
                    int hours = DateUtil.getHour(order.getPaymentTime(), lastAheadTime);
                    LOG.info("hours = " + hours);
                    if (hours >= 24) {
                    	//设置最晚预订时间前一个小时
                    	travDelayWaitTime=DateUtil.DsDay_HourOfDay(lastAheadTime, -1);
                    }else if (0 <= hours && hours < 24) {
                        travDelayWaitTime = DateUtil.dateAddMinutes(lastAheadTime, -3);
                    }
                }
                
                ordAccInsDelayInfo.setTravDelayWaitTime(travDelayWaitTime);
                LOG.info("OrderId" + order.getOrderId() + "\nTravDelayWaitTime = " + ordAccInsDelayInfo.getTravDelayWaitTime());
                
                String addTravRemindStatus = "NO_SEND";
                ordAccInsDelayInfo.setAdTravRemindStatus(addTravRemindStatus);

                ordAccInsDelayInfoService.updateOrdAccInsDelayInfo(ordAccInsDelayInfo);
            }
        }
    }
    @Override
    public void updateOrderTravStatus(Map<String, Object> param) {
        try {
            
            /**
             * Map<String, Object> map = new HashMap<String, Object>();
            map.put("orderId", orderId);
            map.put("travDelayStatus", OrderEnum.ORDER_TRAV_DELAY_STATUS.COMPLETED.name());
             */
            String orderId = (String) param.get("orderId");
            String travDelayStatus = (String) param.get("travDelayStatus");
            
            OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(Long.parseLong(orderId));
            ordAccInsDelayInfo.setTravDelayStatus(travDelayStatus);
            
            ordAccInsDelayInfoService.updateOrdAccInsDelayInfo(ordAccInsDelayInfo);
            LOG.info("update order travStatus orderId:"+param.get("orderId")+"==status:"+param.get("travDelayStatus"));
        } catch(Exception e) {
            LOG.error("Error occurred while update order travStatus orderId:"+param.get("orderId")+"==status:"+param.get("travDelayStatus"), e);
        }
    }
    
    private Date calcOrderAheadTime(final long orderId) {
    	LOG.info("calcOrderAheadTime orderId =" + orderId + " begin");
    	Date lastAheadTime = null;
        OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
        List<OrdOrderItem> orderItems = orderUpdateService.queryOrderItemByOrderId(orderId);
        order.setOrderItemList(orderItems);
        
        List<OrdOrderItem> accInsOrderItemList = OrdOrderUtils.getAccInsOrderItem(order);
        
        List<OrdOrderItem> orderItemList = new ArrayList<OrdOrderItem>();
        //排除子单list中LastAheadTime为null的子单
        for (OrdOrderItem ordOrderItem : accInsOrderItemList) {
            Date itemLastAheadTime = ordOrderItem.getLastAheadTime();
            if (null != itemLastAheadTime) {
                orderItemList.add(ordOrderItem);
            }
        }
        OrdOrderItem orderItem = null;
        for(int i = 0;i<orderItemList.size()-1;i++){
            for(int j=0;j<orderItemList.size()-1-i;j++){
                if(orderItemList.get(j).getLastAheadTime().after(orderItemList.get(j+1).getLastAheadTime())){
                    orderItem = orderItemList.get(j);
                    orderItemList.set(j, orderItemList.get(j+1));
                    orderItemList.set(j+1, orderItem);
                }
            }
        }
        lastAheadTime = orderItemList.get(0).getLastAheadTime();
        String lastConfirmTimetr = DateUtil.formatDate(lastAheadTime, DateUtil.PATTERN_yyyy_MM_dd_HH_mm);
        lastAheadTime = DateUtil.stringToDate(lastConfirmTimetr, DateUtil.PATTERN_yyyy_MM_dd_HH_mm);
        LOG.info("calcOrderAheadTime orderId =" + orderId + " lastAheadTime ="+ lastConfirmTimetr +" end");
        return lastAheadTime;
    }
    
	
	@Override
	public ResultHandleT<List<UserETicketOrderItemVo>> getUserTicketItemList(
			Map<String, Object> param) {
		ResultHandleT<List<UserETicketOrderItemVo>> resultHandleT = new ResultHandleT<List<UserETicketOrderItemVo>>();
		try {
			Long destId=Long.parseLong(String.valueOf(param.get("destId")));
			List<OrdETicketOrderItem> itemList = iOrdOrderItemService.selectUserTicketByParams(param);
			List<UserETicketOrderItemVo> ticketOrderItemVoList = new ArrayList<UserETicketOrderItemVo>();
			if(itemList != null && itemList.size() > 0){
				for(OrdETicketOrderItem item : itemList){
					if(item != null){
						 //期票信息
				        if(StringUtil.isNotEmptyString(item.getContent())){
				            JSONObject  jsonObject = JSONObject.fromObject(item.getContent());
				            if(null!=jsonObject.get("aperiodic_flag") && jsonObject.get("aperiodic_flag").equals("Y")){
				            	 LOG.info("getUserTicketItemList check aperiodic "+item.getOrderItemId()+" is aperiodic");
				            	//本次不返回期票
				            	continue;
				            }
				            LOG.info("getUserTicketItemList check aperiodic "+item.getOrderItemId()+" not aperiodic");
					    }
				        setOrderItemOtherDetails(item);
				        // 关联目的地
				        Map<String, Object> prodDestReParams = new HashMap<String, Object>();
				        prodDestReParams.put("productId", item.getSuppGoods().getProductId());
				        List<ProdDestRe> listRes = prodDestReClientRemote.selectWithDestListByParams(prodDestReParams)
				                .getReturnContent();
				        if (CollectionUtils.isEmpty(listRes)) {
				        	LOG.info("getUserTicketItemList selectWithDestListByParams Empty itemId=="+item.getOrderItemId());
				            continue;
				        }else{
				        	boolean containflag=false;
				        	for (ProdDestRe prodDestRe:listRes) {
				        		if(destId.equals(prodDestRe.getDestId())){
				        			containflag=true;
				        			break;
				        		}
							}
				        	if(!containflag){
				        		LOG.info("getUserTicketItemList nocaontain destId itemId=="+item.getOrderItemId()+GsonUtils.toJson(listRes));
				        		continue;
				        	}
				        }
						List<UserETicketOrderItemVo> ticketOrderItemVoTempList = this.getUserTicketOrderItemVoList(item);
						if(!ticketOrderItemVoTempList.isEmpty()){
							ticketOrderItemVoList.addAll(ticketOrderItemVoTempList);
						}
					}
				}
			}
			if(!ticketOrderItemVoList.isEmpty()){
				resultHandleT.setReturnContent(ticketOrderItemVoList);
			}
			else{
				resultHandleT.setMsg("getUserTicketItemList ticketOrderItemVoList is empty");
			}
		} catch (Exception e) {
			LOG.info("getUserTicketItemList wap error "+e.toString());
			resultHandleT.setMsg(e.getMessage());
		}
		return resultHandleT;
	}
	
	/**
	 * 获取门票子订单的二维码信息、取票时间、取票地址和入园方式
	 * @param item
	 */
	private List<UserETicketOrderItemVo> getUserTicketOrderItemVoList(OrdETicketOrderItem item){
		List<UserETicketOrderItemVo> ticketOrderItemVoList = new ArrayList<UserETicketOrderItemVo>();
		//取到取票地址和入园方式
		SuppGoodsTicketDetailVO sgtdv = suppGoodsClientService.findSuppGoodsTicketDetailById(item.getSuppGoodsId()).getReturnContent();
		List<OrdPassCode> ordPassCodeList = item.getOrdPassCodeList();
		if(ordPassCodeList != null && !ordPassCodeList.isEmpty()){ // 有二维码
			//查询履行记录
			for(OrdPassCode ordPassCode : ordPassCodeList){
				UserETicketOrderItemVo ticketOrderItemVo = new  UserETicketOrderItemVo();
				if(sgtdv != null && sgtdv.getSuppGoodsDesc() != null){
					ticketOrderItemVo.setEnterStyle((sgtdv.getSuppGoodsDesc().getEnterStyle() != null) ? sgtdv.getSuppGoodsDesc().getEnterStyle() : "");
					ticketOrderItemVo.setChangeAddress((sgtdv.getSuppGoodsDesc().getChangeAddress() != null) ? sgtdv.getSuppGoodsDesc().getChangeAddress() : "");
					ticketOrderItemVo.setChangeTime((sgtdv.getSuppGoodsDesc().getChangeTime() != null) ? sgtdv.getSuppGoodsDesc().getChangeTime() : "");
				}else{
					ticketOrderItemVo.setEnterStyle("");
					ticketOrderItemVo.setChangeAddress("");
					ticketOrderItemVo.setChangeTime("");
				}
				ticketOrderItemVo.setVisitTime(item.getVisitTime());
				ticketOrderItemVo.setServiceId(ordPassCode.getServiceId());
				ticketOrderItemVo.setOrderItemId(item.getOrderItemId());
				ticketOrderItemVo.setOrderId(item.getOrderId());
				ticketOrderItemVo.setProductName(item.getProductName());
                ticketOrderItemVo.setGoodsId(item.getSuppGoodsId());
                ticketOrderItemVo.setPaymentTarget(item.getPaymentTarget());
				if(item.getSuppGoods()!=null){
					ticketOrderItemVo.setGoodsName(item.getSuppGoods().getGoodsName());
				}
				//子订单使用状态
			    ticketOrderItemVo.setIsUsed(item.getPerformStatus());
			    //品类
			    ticketOrderItemVo.setCategoryCode(item.getContentStringByKey("categoryCode"));
			    LOG.info("=============品类=========="+item.getContentStringByKey("categoryCode"));
			    //演出票区域
			    ticketOrderItemVo.setShowTicketRegion(item.getShowTicketRegion());
			    LOG.info("=============区域=========="+item.getShowTicketRegion());
			    if(null == item.getShowTicketEventStartTime() || "".equals(item.getShowTicketEventStartTime())){
			    	ticketOrderItemVo.setShowTicketEvent("通场");
			    }else{
			    	String ShowTicketEvent = item.getShowTicketEventStartTime() + "--" + item.getShowTicketEventEndTime();
			    	//演出票场次
			    	ticketOrderItemVo.setShowTicketEvent(ShowTicketEvent);
			    	LOG.info("=============场次=========="+ShowTicketEvent);
			    }
			    String idFlag = "";
				if(null != item.getSuppGoodsId()){
					Map<String, Object> params = new HashMap<String, Object>();
					params.put("objectId", item.getSuppGoodsId());
					params.put("objectType", ComOrderRequired.OBJECTTYPE.SUPP_GOODS.name());
					List<ComOrderRequired> findComOrderRequiredList = this.comOrderRequiredClientRemote.findComOrderRequiredList(params);
					if(null != findComOrderRequiredList && findComOrderRequiredList.size()>0){
						ComOrderRequired comOrderRequired = findComOrderRequiredList.get(0);
						idFlag = comOrderRequired.getIdFlag();
					}
				}
			    //是否需要身份证
			    ticketOrderItemVo.setIsNeedIdFlag(idFlag);
			    LOG.info("=============身份证=========="+idFlag);

				// 二维码信息
				ticketOrderItemVo.setAddCode(ordPassCode.getAddCode());
				ticketOrderItemVo.setCodeImage(ordPassCode.getCodeImage());
				if(ordPassCode.getCodeImage()!=null&&("".equals(ordPassCode.getPicFilePath())||ordPassCode.getPicFilePath()==null)){
					PassCodeImageVo passCodeImageVo = passCodeService.getPassCodeImageBySerialNo(ordPassCode.getPassSerialno());
					if(passCodeImageVo!=null){
						ordPassCode.setPicFilePath(passCodeImageVo.getPicFilePath());
					}
				}
				ticketOrderItemVo.setPicFilePath(ordPassCode.getPicFilePath());
				ticketOrderItemVo.setPassCodeId(ordPassCode.getPassCodeId());
				ticketOrderItemVo.setCode(ordPassCode.getCode());
				ticketOrderItemVoList.add(ticketOrderItemVo);

				ticketOrderItemVo.setCodeImagePdfFlag(this.getCodeImagePdfFlag(item.getSuppGoodsId()));
				ticketOrderItemVo.setFileId(ordPassCode.getFileId());
                ticketOrderItemVo.setUrl(ordPassCode.getUrl());
			}
		}
		return ticketOrderItemVoList;
	}
	@Override
	public List<ResPrecontrolOrderVo> findPercontrolGoodsHisOrderList(Page<HisResPrecontrolOrderVo> page) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("suppGoodsId", page.getParam().getSuppGoodsId());
		paramsMap.put("preControlPolicyId", page.getParam().getPreControlPolicyId());
		paramsMap.put("startIndex", (page.getPage()-1)*page.getPageSize());
		paramsMap.put("endIndex", page.getPage()*page.getPageSize());
		
		return iOrdOrderService.findPercontrolGoodsHisOrderList(paramsMap);
	}
	
	public long countPercontrolGoodsHisOrder(long suppGoodsId, long preControlPolicyId){
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("suppGoodsId",suppGoodsId);
		paramsMap.put("preControlPolicyId",preControlPolicyId);
		return iOrdOrderService.countPercontrolGoodsHisOrder(paramsMap);
	}
	
	@Override
	public VstInvoiceAmountVo getInvoiceAmount(Long orderId){
		return ordInvoiceService.getInvoiceAmount(orderId);
	}

    @Override
    public ResultHandle handleTravDelayOrder(OrdOrder order) {
        LOG.info("handleTravDelayOrder, orderId:" + order.getOrderId());
        ResultHandle handle = new ResultHandle();
        if (ActivitiUtils.hasActivitiOrder(order)) {
            try {
                handle = processerClientService.handleTravDelayOrder(createKeyByOrder(order));
            } catch (Exception e) {
                // 启动异常处理
                handle.setMsg("handleTravDelayOrder error! Start workflow to send accInsOrderItem e = " + e.getMessage());
            }
        }
        if(handle !=null && handle.isSuccess()){
            LOG.info("handleTravDelayOrder success, orderId:" + order.getOrderId());
        }else{
            LOG.info("handleTravDelayOrder failed, orderId:"+ order.getOrderId());
        }
        return handle;
    }
    
    /**
     * 
     * @Description: 弃保意外险子单：退款，更改主单游玩人后置状态，发送短信 
     * @author Wangsizhi
     * @date 2016-12-27 下午5:14:58
     */
    @Override
    public ResultHandle cancelTravDelayOrderItems(final Long orderId, String operatorName, String memo) {
        LOG.info("cancelTravDelayOrderItemsOrderId" + orderId);
        ResultHandle resultHandle = new ResultHandle();
        if (null == orderId) {
            resultHandle.setMsg("cancelTravDelayOrderItems, orderId is null");
            return resultHandle;
        }
        if (StringUtils.isBlank(operatorName)) {
            operatorName = "SYSTEM";
        }
        if (StringUtils.isBlank(memo)) {
            memo = "SYSTEM";
        }
        
        OrdAccInsDelayInfo accInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(orderId);
        String delayStatus = accInsDelayInfo.getTravDelayStatus();
        if (StringUtils.isNotBlank(delayStatus) 
                && delayStatus.equalsIgnoreCase(OrderEnum.ORDER_TRAV_DELAY_STATUS.ABANDON.name())) {
            String msg = "AccInsOrderItem has abandon. orderId:" + orderId;
            LOG.info(msg);
            resultHandle.setMsg(msg);
            return resultHandle;
        }
        
        List<OrdOrderItem> insAccOrderItems = this.getInsAccOrderItems(orderId);
        Long refundAmount = 0L;
        String orderItemIds = "";
        LOG.info("ordorderitemList");
        for (OrdOrderItem ordOrderItem : insAccOrderItems) {
            refundAmount += ordOrderItem.getTotalAmount();
            orderItemIds += ordOrderItem.getOrderItemId();
            LOG.info(ordOrderItem.toString());
        }
        LOG.info("cancelTravDelayOrderItems, orderItemIds  = " + orderItemIds);
        LOG.info("refundAmount = " + refundAmount);
        LOG.info("operatorName = " + operatorName);
        LOG.info("memo = " + memo);
        
        LOG.info("before call zhifu tuikuan refundAmount = " + orderUpdateService.queryOrdOrderByOrderId(orderId).getRefundedAmount());
        
        //更改主单游玩人后置状态, 减掉相应退款金额
        /*弃保意外险*/
        OrdAccInsDelayInfo ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(orderId);
        ordAccInsDelayInfo.setTravDelayStatus(OrderEnum.ORDER_TRAV_DELAY_STATUS.ABANDON.name());
        ordAccInsDelayInfoService.updateOrdAccInsDelayInfo(ordAccInsDelayInfo);
        /*设置退款金额*/
        //ordOrderDao.updateRefundedAmount(orderId, refundAmount);
        
        /*触发工作流，若流程已经走到“待游玩人后置补全或弃保”节点，则正常流转；若未走到，则会出现异常，异常可以接受，不影响流程运转*/
        OrdOrder order = orderUpdateService.queryOrdOrderByOrderId(orderId);
        ResultHandle workflowResult = this.handleTravDelayOrder(order);
        
        LOG.info("after set order refundAmount  refundAmount = " + orderUpdateService.queryOrdOrderByOrderId(orderId).getRefundedAmount());
        
        /*设置订单子项*/
        List<OrdOrderItem> orderItems = orderUpdateService.queryOrderItemByOrderId(orderId);
        order.setOrderItemList(orderItems);

        List<OrdOrderItem> noAccInsOrderItems = OrdOrderUtils.getNoAccInsOrderItem(order);
        
        //判断非意外险子单是否推送完毕
        boolean isNoAccInsCreated = true;
        for (OrdOrderItem ordOrderItem : noAccInsOrderItems) {
            String invokeInterfacePfStatus = ordOrderItem.getInvokeInterfacePfStatus();
            if (ordOrderItem.isSupplierOrderItem()) {
                if (StringUtils.isNotBlank(invokeInterfacePfStatus) && !invokeInterfacePfStatus.equalsIgnoreCase(OrderEnum.INVOKE_INTERFACE_PF_STATUS.CREATED.name())) {
                    isNoAccInsCreated = false;
                    break;
                }    
            }
        }
        
        //如果取消意外险子单时，非意外险供应商子单都完成供应商订单创建，则将意外险子单供应商推送状态置为cancel， 将主单供应商推送状态置为created
        if (isNoAccInsCreated) {
            orderUpdateService.updateInvokeInterfacePfStatus(orderId, OrderEnum.INVOKE_INTERFACE_PF_STATUS.CREATED.name());
        }
        
        //将意外险子单供应商推送状态置为cancel
        StringBuffer sb = new StringBuffer();
        for (Iterator<OrdOrderItem> it = insAccOrderItems
                .iterator(); it.hasNext();) {
            OrdOrderItem item = it.next();
            item.setInvokeInterfacePfStatus(OrderEnum.INVOKE_INTERFACE_PF_STATUS.CANCELED.name());
            if(sb.length() > 0) {
                sb.append(",");
            }
            
            sb.append(item.getOrderItemId());
        }
        //更新调用状态
        if(sb.length() > 0) {
            orderUpdateService.updateItemInvokeInterfacePfStatus(sb.toString(), OrderEnum.INVOKE_INTERFACE_PF_STATUS.CANCELED.name());
        }
        
        Map<String, Object> params=new HashMap<String, Object>();
        params.put("priceTypeArray", new Object[]{OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCode(),OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD.getCode(),OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.getCode()});
        params.put("orderItemIdArray", sb.toString().split(","));
        List<OrdMulPriceRate> list=ordMulPriceRateClientService.findOrdMulPriceRateList(params);
        PriceTypeVO altogether=new PriceTypeVO();
        if(list!=null&&list.size()>0){
    		for (OrdMulPriceRate ordMulPriceRate : list) {
    			PriceTypeVO pricevo=OrdRefundBussinessServiceRemote.getMuliTypeRefundedInfo(orderId, ordMulPriceRate.getOrderItemId(), ordMulPriceRate.getPriceType());
    			if(pricevo!=null){
    				if(pricevo.getPrice()>=0){
    					altogether.setPrice(((altogether.getPrice()==null?0:altogether.getPrice())+pricevo.getPrice()));
    				}
    			}
			}
        	
        }else{
        	for (OrdOrderItem item : insAccOrderItems) {
        		PriceTypeVO pricevo=OrdRefundBussinessServiceRemote.getRefundedInfo(orderId, item.getOrderItemId());
        		if(pricevo!=null){
	        		if(pricevo.getPrice()>=0){
	        			altogether.setPrice(((altogether.getPrice()==null?0:altogether.getPrice())+pricevo.getPrice()));
	    			}
        		}
        	}
        }
        
        //退款
        ordAccInsDelayInfo = ordAccInsDelayInfoService.selectByOrderId(orderId);
        if (StringUtils.isNotBlank(ordAccInsDelayInfo.getTravDelayStatus()) 
                && ordAccInsDelayInfo.getTravDelayStatus().equalsIgnoreCase(OrderEnum.ORDER_TRAV_DELAY_STATUS.ABANDON.name())) {
            //Long refundId = ordPrePayServiceAdapter.createSaleAndRefundmentVst(orderId, insAccOrderItems, refundAmount, operatorName, memo);
        	 LOG.info("cancelTravDelayOrderItems, orderId:" + orderId + ", amount:" + (refundAmount-(altogether.getPrice()==null?0:altogether.getPrice())));
            Long refundId = ordPrePayServiceAdapter.autoCreateSaleAndRefundmentVst(orderId, insAccOrderItems, (refundAmount-(altogether.getPrice()==null?0:altogether.getPrice())), operatorName, memo, "FRONT");
            LOG.info("cancelTravDelayOrderItems, orderId:" + orderId + ", refundId:" + refundId);
            
            String content = "订单 " + orderId + "， 意外险子单 " + orderItemIds + ", 取消意外险，退款成功，退款单 " + refundId + ", 退款金额 " + refundAmount;
            
            ComLog log = new ComLog();
            log.setObjectId(orderId);
            log.setObjectType(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER.getParentType().name());
            log.setParentId(orderId);
            log.setParentType(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER.getParentType().name());
            log.setOperatorName(operatorName);
            log.setLogName("取消意外险");
            log.setLogType(Constant.COM_LOG_ORDER_EVENT.updateOrder.name());
            log.setContent(content);
            
            lvmmLogClientService.sendLog(log);
            
        }
        
        LOG.info("after call zhifu tuikuan  refundAmount = " + orderUpdateService.queryOrdOrderByOrderId(orderId).getRefundedAmount());
        
        /*发送弃保短信*/
        orderSmsSendProcesser.sendSms(MessageFactory.cancelInsAccDelayOrderItemMessage(orderId, ""), order);
        
        return resultHandle;
    }
    
    /**
     * 
     * @Description: 根据orderId获取意外险子单列表 
     * @author Wangsizhi
     * @date 2016-12-21 下午8:03:54
     */
    private List<OrdOrderItem> getInsAccOrderItems(Long orderId) {
        List<OrdOrderItem> insAccOrderItemList = new ArrayList<OrdOrderItem>();
        List<OrdOrderItem> orderItemList = orderUpdateService.queryOrderItemByOrderId(orderId);
        /*过滤出目的地就套餐、酒+景、自由行主单中意外险子单，此部分子单待游玩人补充完整后再推送供应商*/
        for (OrdOrderItem ordOrderItem : orderItemList) {
            String destBuAccFlag = ordOrderItem.getContentStringByKey("destBuAccFlag");
            if (StringUtils.isNotBlank(destBuAccFlag) && StringUtils.equalsIgnoreCase(destBuAccFlag, "Y")) {
            	ordOrderItem.setRefundQuantity(ordOrderItem.getQuantity());
            	ordOrderItem.setRefundPrice(ordOrderItem.getTotalAmount());
                insAccOrderItemList.add(ordOrderItem);
            }
        }
        return insAccOrderItemList;
    }

	@Override
	public ResultHandleT<Page<OrdOrderVo>> newCompositeQuery(Page<OrdOrderVo> page,
			ComplexQuerySQLCondition condition) {
		ResultHandleT<Page<OrdOrderVo>> resultHandleT = new ResultHandleT<Page<OrdOrderVo>>();
		try {
			//检查输入参数
			if(condition.getOrderIndentityParam() == null || CollectionUtils.isEmpty(condition.getOrderIndentityParam().getOrderIds())) {
				throw new Exception("订单Id集合不能为空");
			}
			long totalCount=complexQueryService.queryOrderCountByCondition(condition);
			if(totalCount>0L){
				page.setTotalResultSize(totalCount);
				OrderPageIndexParam param = new OrderPageIndexParam();
				param.setBeginIndex((int)page.getStartRows());
				param.setEndIndex((int)page.getEndRows());
				condition.setOrderPageIndexParam(param);
				condition.getOrderFlagParam().setOrderPageFlag(true);
				condition.getOrderFlagParam().setOrderItemTableFlag(true);
				
				//查询订单详情列表
				List<OrdOrder> orderList = complexQueryService.queryOrderListByCondition(condition);						
				initOrdOrderShowOneKeyOrderFlag(orderList);												
				List<OrdOrderVo> orderVoList = new ArrayList<OrdOrderVo>(orderList.size());
				if(CollectionUtils.isNotEmpty(orderList)) {
					//获取演出票基础信息
					List<Long> productIdList = new ArrayList<Long>();
					for(OrdOrder order : orderList) {
						productIdList.add(order.getProductId());
					}
					ResultHandleT<List<ShowTicketBaseInfo>> resultHandle = showTicketBaseInfoClientService.findTicketShowBaseInfoListByProductIdList(productIdList);
					if(resultHandle.getMsg() != null) {
						LOG.warn("findTicketShowBaseInfoListByProductIdList error：" + resultHandle.getMsg());
					}
					List<ShowTicketBaseInfo> showTicketBaseInfoList = resultHandle.getReturnContent();
					Map<Long, ShowTicketBaseInfo> showTicketBaseInfoMap = new HashMap<Long, ShowTicketBaseInfo>();
					if(CollectionUtils.isNotEmpty(resultHandle.getReturnContent())) {
						for(ShowTicketBaseInfo showTicketBaseInfo : showTicketBaseInfoList) {
							showTicketBaseInfoMap.put(showTicketBaseInfo.getProductId(), showTicketBaseInfo);
						}
					}
					
					for(OrdOrder order : orderList) {
						OrdOrderVo orderVo = new OrdOrderVo();
						LOG.info("source order: " + JSON.toJSONString(order));
						EnhanceBeanUtils.copyProperties(order, orderVo);
						//设置演出票基础信息
						orderVo.setShowTicketBaseInfo(showTicketBaseInfoMap.get(orderVo.getProductId()));
						//设置二维码信息
						OrdOrderItem mainOrderItem = orderVo.getMainOrderItem();
						ResultHandleT<OrdPassCode> result = getOrdPassCodeByOrderItemId(mainOrderItem.getOrderItemId());
						if(result.getMsg() != null) {
							LOG.warn("getOrdPassCodeByOrderItemId error：" + result.getMsg());
						}						
						List<OrdPassCode> passCodeList = new ArrayList<OrdPassCode>();
						passCodeList.add(result.getReturnContent());
						mainOrderItem.setOrdPassCodeList(passCodeList);
						for(OrdOrderItem orderItem : orderVo.getOrderItemList()) {
							result = getOrdPassCodeByOrderItemId(orderItem.getOrderItemId());
							if(result.getMsg() != null) {
								LOG.warn("getOrdPassCodeByOrderItemId error：" + result.getMsg());
							}
							passCodeList = new ArrayList<OrdPassCode>();
							passCodeList.add(result.getReturnContent());
							orderItem.setOrdPassCodeList(passCodeList);
						}
						orderVoList.add(orderVo);
					}
				}
				page.setItems(orderVoList);
				resultHandleT.setReturnContent(page);
			}
		} catch (Exception e) {
			resultHandleT.setMsg(e);
			LOG.error("newCompositeQuery is error :"+ExceptionFormatUtil.getTrace(e));
		}
		return resultHandleT;
	}
	
	
	@Override
	public ResultHandleT<List<OrdOrderSharedStock>> getOrderShareStockByParams(Map<String, Object> param) {
		ResultHandleT<List<OrdOrderSharedStock>> resultHandle = new ResultHandleT<List<OrdOrderSharedStock>>();
		try
		{
			List<OrdOrderSharedStock> ordShareList = orderUpdateService.getOrderShareStockByParams(param);
			resultHandle.setReturnContent(ordShareList);
		}catch(Exception e){
			LOG.error("getOrderShareStockByParams is error :"+ExceptionFormatUtil.getTrace(e));
		}
		
		return resultHandle;
	}
	
	
	
	
	public ResultHandleT<Integer> updateOrdOrderStock(OrdOrderStock ordOrderStock){
		ResultHandleT<Integer> result = new ResultHandleT<Integer>();		
		try
		{
			int resultUpdate = 0;
			resultUpdate = orderUpdateService.updateOrdOrderStock(ordOrderStock);
			result.setReturnContent(Integer.valueOf(resultUpdate));
		}catch(Exception e){
			LOG.error("updateOrdOrderStock is error :"+ExceptionFormatUtil.getTrace(e));
		}
		return result;
	}
	


	@Override
	public List<OrdOrderItem> initOrderWithBuyInfo(final BuyInfo buyInfo) {
		List<OrdOrderItem> orderItemList =new ArrayList<OrdOrderItem>();
		try {
			LOG.info("com.lvmama.vst.order.service.IBookService.initOrderWithBuyInfo  start");

			OrdOrderDTO orderDto=bookService.initOrderWithBuyInfo(buyInfo);
			if (CollectionUtils.isNotEmpty(orderDto.getOrderItemList())) {
				for (OrdOrderItem ooi : orderDto.getOrderItemList()) {
					OrdOrderItem oo=new OrdOrderItem();
					EnhanceBeanUtils.copyProperties(ooi, oo);
					oo.setOrderPack(null);
					orderItemList.add(oo);
				}
			}
		} catch (Exception e) {
			LOG.error("initOrderWithBuyInfo is error :"+ExceptionFormatUtil.getTrace(e));
		}
		return orderItemList;
	}


	@Override
    public OrdAccInsDelayInfo getAccInsDelayInfoByOrderId(final Long orderId){
	    return ordAccInsDelayInfoService.selectByOrderId(orderId);
	}

    @Override
    public ResultHandleT<List<OrdOrderItem>> getUserOrderItemList(Map<String, Object> param) {
        ResultHandleT<List<OrdOrderItem>> result = new ResultHandleT<List<OrdOrderItem>>();
        try {
            Long destId = Long.parseLong(String.valueOf(param.get("destId")));
            //根据目的地获取产品ID
            Map<String, Object> prodDestReParams = new HashMap<String, Object>();
            prodDestReParams.put("destId", destId);
            ResultHandleT<List<ProdDestRe>> resultHandleT = prodDestReClientRemote.selectWithDestListByParams(prodDestReParams);
            if (null != resultHandleT) {
                List<ProdDestRe> listRes = prodDestReClientRemote.selectWithDestListByParams(prodDestReParams).getReturnContent();
                if (null != listRes && listRes.size() > 0) {
                    List<Long> productIds = new ArrayList<Long>();
                    for (ProdDestRe prodDestRe : listRes) {
                        productIds.add(prodDestRe.getProductId());
                    }
                    param.put("productIds", productIds);
                    //按订单ID倒序
                    param.put("_orderby", "item.order_id");
                    param.put("_order", "DESC");
                    List<OrdOrderItem> itemList = iOrdOrderItemService.queryUserOrderItemList(param);
                    if (null != itemList && itemList.size() > 0) {
                        List<OrdOrderItem> ordOrderItemList = new ArrayList<OrdOrderItem>();
                        for (OrdOrderItem orderItem : itemList) {
                            //期票信息
                            if (StringUtil.isNotEmptyString(orderItem.getContent())) {
                                JSONObject jsonObject = JSONObject.fromObject(orderItem.getContent());
                                if (null != jsonObject.get("aperiodic_flag") && jsonObject.get("aperiodic_flag").equals("Y")) {
                                    LOG.info("getUserOrderItemList destId=" + destId + " check aperiodic " + orderItem.getOrderItemId() + " is aperiodic");
                                    //不返回期票
                                    continue;
                                }
                            }
                            ordOrderItemList.add(orderItem);
                        }
                        if (ordOrderItemList.size() > 0) {
                            result.setReturnContent(ordOrderItemList);
                        } else {
                            LOG.error("getUserOrderItemList destId=" + destId + " no valid OrdOrderItem");
                            result.setMsg("无订单");
                        }

                    } else {
                        LOG.error("getUserOrderItemList destId=" + destId + " no OrdOrderItem");
                        result.setMsg("无数据");
                    }
                } else {
                    LOG.error("getUserOrderItemList destId=" + destId + " no product");
                    result.setMsg("该目的地下无产品");
                }
            } else {
                LOG.error("getUserOrderItemList destId=" + destId + " no product");
                result.setMsg("该目的地下无产品");
            }
        } catch (Exception e) {
            result.setMsg(e);
            LOG.error("getUserOrderItemList  destId=" + String.valueOf(param.get("destId"))+" error :" + e.toString());
        }
        return result;
    }

	@Override
	/**
	 * 新收客表统计查询
	 * @param paramProdProduct
	 * @return
	 */
	public ResultHandleT<Long> countRouteProductList(Map<String, Object> paramProdProduct) {
		Long count = ordOrderDao.countRouteProductList(paramProdProduct);
		if(count==null){
			count=0L;
		}
		ResultHandleT<Long> result = new ResultHandleT<>();
		result.setReturnContent(count);
		return result;
	}

	@Override
	/**
	 * 新收客表统计查询
	 * @param paramProdProduct
	 * @return
	 */
	public ResultHandleT<List<ProdOrdRoute>> findRouteProductList(Map<String, Object> paramProdProduct) {
		List<ProdOrdRoute> list = ordOrderDao.findRouteProductList(paramProdProduct);
		if(list==null){
			list=Collections.EMPTY_LIST;
		}
		ResultHandleT<List<ProdOrdRoute>> result = new ResultHandleT<List<ProdOrdRoute>>();
		result.setReturnContent(list);
		return result;
	}


	@Override
	/**
	 * 新收客表统计查询导出
	 * @param paramProdProduct
	 * @return
	 */
	public ResultHandleT<List<ProdOrdRoute>> findRouteProductListForReport(Map<String, Object> paramProdProduct) {
		List<ProdOrdRoute> list = ordOrderDao.findRouteProductListForReport(paramProdProduct);
		if(list==null){
			list=Collections.EMPTY_LIST;
		}
		ResultHandleT<List<ProdOrdRoute>> result = new ResultHandleT<List<ProdOrdRoute>>();
		result.setReturnContent(list);
		return result;
	}

	/**
   	 * 判断分销单酒店资源审核状态
   	 * 手工渠道订单资审自动通过
   	 * @param order
   	 */
   	private void calDistributorOrderResourceStatus(OrdOrder order) {
   		if (order!=null&&CollectionUtils.isNotEmpty(order.getOrderItemList())) {
   			for (OrdOrderItem orderItem : order.getOrderItemList()) {
   				if(order.getDistributorId() != null
   						&& Constant.DIST_BRANCH_SELL == order.getDistributorId()
   						&& CommEnumSet.BU_NAME.DESTINATION_BU.getCode().equals(order.getBuCode())
   						&& BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == order.getCategoryId()
   						&& "DISTRIBUTOR_SG".equals(order.getDistributorCode())){
   					orderItem.setNeedResourceConfirm("false");
   					orderItem.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.getCode());

   					Log.info("OrdOrderClientServiceImpl_DISTRIBUTOR_SG resource auto pass productId:"+orderItem.getProductId() +
   							" goodsId:" + orderItem.getSuppGoodsId());
   					// 设置主订单的资源状态
   					order.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
   					// 保存订单子表资审状态
   					OrdOrderItem newOrderItem = new OrdOrderItem();
   					newOrderItem.setOrderItemId(orderItem.getOrderItemId());
   					newOrderItem.setNeedResourceConfirm("false");
   					newOrderItem.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
   					updateOrderItem(newOrderItem);
   					// 保存订单表资审状态
   					OrdOrder newOrder = new OrdOrder();
   					newOrder.setOrderId(order.getOrderId());
   					newOrder.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.getCode());
   					updateOrdOrder(newOrder);
   					// 发送资源审核通过消息
   					sendResourceStatusAmpleMsg(order.getOrderId());
   				}
   			}
   		}
   	}



	/**
	 * 获取门票子订单结算价
	 * @param req
	 * @return
	 */
	public TicketSettlementDetail getTicketSettlementDetail(SettlementDetailAcquisitionReq req) {
		/*
			1，子订单品类是景点门票，其它票，组合套餐票，演出票，美食，娱乐，购物，WiFi电话卡，交通接驳，保险；不考虑BU。
			判断以下规则是否可自动修改子订单结算价。
	
			1)判断子订单库存是否纯买断，或非买断；
			若否，子订单退款金额<实付金额（部分退），，则发送邮件和VST右下角弹框提醒给子订单产品经理去修改结算价，同时将子订单上价格确认状态字段标记为“价格待确认”
			子订单退款金额=实付金额（全额退），不推送消息。将该子订单实际结算价修改为0，价格待确认状态为已确认
	
			2)若是，再判断子订单目前实际结算总价是否等于原实际结算总价，
			若不等于，子订单退款金额<实付金额（部分退），则发送邮件和VST右下角弹框提醒给子订单产品经理去修改结算价，同时将子订单上价格确认状态字段标记为“价格待确认”
			子订单退款金额=实付金额（全额退），不推送消息。将该子订单实际结算价修改为0，价格待确认状态为已确认
	
			3)若等于，再判断退款单中子订单退款金额50 是否等于BU接口返回的退款金额40，
			若不等于，子订单退款金额<实付金额（部分退），部分退则发送邮件和VST右下角弹框提醒给子订单业务产品经理去修改结算价，同时将子订单上价格确认状态字段标记为“价格待确认”
			子订单退款金额=实付金额（全额退）不推送消息。将该子订单实际结算价修改为0，价格待确认状态为已确认。
			
			3)若等于，再判断退款单中子订单退款金额50 是否等于BU接口返回的退款金额40，
			若不等于，子订单退款金额<实付金额（部分退），部分退则发送邮件和VST右下角弹框提醒给子订单业务产品经理去修改结算价，同时将子订单上价格确认状态字段标记为“价格待确认”
			子订单退款金额=实付金额（全额退）不推送消息。将该子订单实际结算价修改为0，价格待确认状态为已确认。
			
			4)若等于，在判断该子订单价格确认状态是否等于已确认
			若不是已确认，子订单退款金额<实付金额（部分退），部分退则发送邮件和VST右下角弹框提醒给子订单业务产品经理去修改结算价，同时将子订单上价格确认状态字段标记为“价格待确认”
			子订单退款金额=实付金额（全额退）不推送消息。将该子订单实际结算价修改为0，价格待确认状态为已确认。
	
			5)若等于，则将BU接口返回的子订单结算总价值，修改成子订单实际结算总价。
		 */
		
		LOG.info("SettlementDetailAcquisitionReq -> " + GsonUtils.toJson(req));

		TicketSettlementDetail detail = null;

		if (req != null) {
			OrdOrderItem subOrder = getSubOrder(req.getSubOrderId());
			LOG.info("sub-order -> " + GsonUtils.toJson(subOrder));

			if (subOrder != null) {
				if (isCountableCategory(subOrder)) {
					boolean isHybridBuyOut = isHybridBuyOut(subOrder);
					LOG.info((isHybridBuyOut ? "" : "not ") + "hybrid buyout order");

					boolean totalSettlementPriceChanged = isTotalSettlementPriceChanged(subOrder);
					LOG.info("total settlement price" + (totalSettlementPriceChanged ? "" : " not") + " changed");
					
					boolean refundAmountChanged = isRefundAmountChanged(req);
					LOG.info("refund amount" + (refundAmountChanged ? "" : " not") + " changed");
					
					boolean priceNotConfirmed = isPriceNotConfirmed(subOrder);
					LOG.info("price" + (priceNotConfirmed ? " not" : "") + " confirmed");
					
					// 混合买断、结算价格有改变的、退款金额有改变的、价格状态未确认的订单； 如果是部分退触发人工流程， 如果是整单退则结算价格置为零，价格确认状态为已确认
					if (isHybridBuyOut || totalSettlementPriceChanged || refundAmountChanged || priceNotConfirmed) {
						boolean isPartRefuned = isPartRefunded(subOrder, req);
						LOG.info((isPartRefuned ? "" : "not ") + "part refunded");
						if (isPartRefuned) {
							detail = genSettlementDetail4OrderPartRefunded();
							triggerManualProcess(subOrder);
						} else {
							detail = genSettlementDetail4OrderFullRefunded();
						}
					}
					
					// 纯买断或非买断、结算价格没有改变的、退款金额没有改变、价格状态已确认的订单可以走结算价格自动计算流程
					if (!isHybridBuyOut && !totalSettlementPriceChanged && !refundAmountChanged && !priceNotConfirmed) {
						detail = genSettlementDetail(subOrder, req);						
						// 计算失败，触发人工流程
						if (isSettlementDetailNotValid(detail))
							triggerManualProcess(subOrder);
					}
				} else {
					LOG.warn("uncountable sub-order[" + subOrder.getOrderItemId() + "]");
				}
			}
		} else {
			LOG.warn("null SettlementDetailAcquisitionReq");
		}

		LOG.info("TicketSettlementDetail -> " + GsonUtils.toJson(detail));

		return detail;
	}
	
	OrdOrderItem getSubOrder(long subOrderId) {
		OrdOrderItem subOrder = null;
		try {
			subOrder = this.iOrdOrderItemService.selectOrderItemByOrderItemId(subOrderId);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return subOrder;
	}
	
	boolean isCountableCategory(OrdOrderItem subOrder) {
		if (subOrder != null && subOrder.getCategoryId() != null) {
			Long categoryId = subOrder.getCategoryId();
			if (categoryId.equals(BizEnum.BIZ_CATEGORY_TYPE.category_ticket.getCategoryId())) {
				return true;
			}
			if (categoryId.equals(BizEnum.BIZ_CATEGORY_TYPE.category_single_ticket.getCategoryId())) {
				return true;
			}
			if (categoryId.equals(BizEnum.BIZ_CATEGORY_TYPE.category_other_ticket.getCategoryId())) {
				return true;
			}
			if (categoryId.equals(BizEnum.BIZ_CATEGORY_TYPE.category_comb_ticket.getCategoryId())) {
				return true;
			}
			if (categoryId.equals(BizEnum.BIZ_CATEGORY_TYPE.category_show_ticket.getCategoryId())) {
				return true;
			}
			if (categoryId.equals(BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId())) {
				return true;
			}
			if (categoryId.equals(BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId())) {
				return true;
			}
			if (categoryId.equals(BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId())) {
				return true;
			}
			if (categoryId.equals(BizEnum.BIZ_CATEGORY_TYPE.category_wifi.getCategoryId())) {
				return true;
			}
			if (categoryId.equals(BizEnum.BIZ_CATEGORY_TYPE.category_connects.getCategoryId())) {
				return true;
			}
		}
		return false;
	}
	
	boolean isHybridBuyOut(OrdOrderItem subOrder) {
		if (subOrder != null) {
			if (subOrder.getBuyoutFlag() == null) { 
				return false;
			} else {
				if (subOrder.getBuyoutFlag().equalsIgnoreCase(BUYOUT_FLAG_YES)) {
					// 纯买断
					if (subOrder.getBuyoutQuantity() != null && subOrder.getQuantity() != null
							&& subOrder.getBuyoutQuantity().longValue() > 0l && subOrder.getQuantity().longValue() > 0l
							&& subOrder.getBuyoutQuantity() == subOrder.getQuantity())
						return false;
				} else if (subOrder.getBuyoutFlag().equalsIgnoreCase(BUYOUT_FLAG_NO)) {
					// 非买断
					return false;
				}
			}
		}
		return true;
	}
	
//	Long getDeductAmountOrPercentage(OrdOrderItem subOrder, SettlementDetailAcquisitionReq req) {
//		Long deductAmountOrPercentage = null;
//		if (subOrder == null) return deductAmountOrPercentage;
//		if (subOrder.getRefundRules() == null) return deductAmountOrPercentage;
//		if (subOrder.getRefundRules().equals("")) return deductAmountOrPercentage;
//		if (req == null) return deductAmountOrPercentage;
//		if (req.getRefundTime() == null) return deductAmountOrPercentage;
//		List<SuppGoodsRefundVO> rules = null;
//		try {
//			rules = JSONUtil.jsonArray2Bean(subOrder.getRefundRules(), SuppGoodsRefundVO.class);
//		} catch (Exception e) {
//			LOG.error(e.getMessage());
//		}
//		if (rules == null) return deductAmountOrPercentage;
//		// parse rules
//		int exceptionRuleIdx = -1;
//		int ruleIdx = 0;
//		Map<Integer, Date> criticalPoint = new HashMap<Integer, Date>();
//		for (SuppGoodsRefundVO rule : rules) {
//			if (rule.getCancelTimeType().equals(SuppGoodsRefundVO.CANCEL_TIME_TYPE.OTHER.toString())) {
//				exceptionRuleIdx = ruleIdx;
//			} else {
//				if (rule.getLatestCancelTime() != null) {
//					if (subOrder.getVisitTime() != null) {
//						criticalPoint.put(ruleIdx, DateUtils.addMinutes(subOrder.getVisitTime(),
//								(int) Math.negateExact((rule.getLatestCancelTime()))));
//					} else {
//						LOG.warn("[visit time] of {orderItemId:" + subOrder.getOrderItemId()
//								+ "} is empty, so time-affected deduction amount may not be calculated out.");
//					}
//				}
//			}
//			ruleIdx++;
//		}
//		// figure it out in which interval date of application for drawback falls 
//		Date lastMaxDate = null;
//		Date lastMinDate = null;
//		for (Integer idx : criticalPoint.keySet()) {
//			if (lastMaxDate == null || criticalPoint.get(idx).after(lastMaxDate)) {
//				lastMaxDate = criticalPoint.get(idx);
//			} 
//			if (lastMinDate == null || criticalPoint.get(idx).before(lastMinDate)) {
//				lastMinDate = criticalPoint.get(idx);
//			}
//			if(req.getRefundTime().before(lastMaxDate)) {
//			}
//			if (req.getRefundTime().after(lastMinDate)) {
//			}
//		}
//		return deductAmountOrPercentage;
//	}
	
	TicketSettlementDetail genSettlementDetail(OrdOrderItem subOrder, SettlementDetailAcquisitionReq req) {
		TicketSettlementDetail detail = new TicketSettlementDetail(
				OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.UN_CONFIRMED.toString());
		if (subOrder == null) return detail;
		if (subOrder.getSettlementPrice() == null) return detail;
		if (subOrder.getQuantity() == null) return detail;
		if (req == null) return detail;
		if (req.getRefundedUnits() < 0) return detail;
		if (req.getMatchedRefundRule() == null) return detail;
		if (req.getMatchedRefundRule().trim().equals("")) return detail;
		
		// parse matched rule
		SuppGoodsRefundVO matchedRule = null;
		boolean parsedWell = true;
		try {
			matchedRule = com.alibaba.fastjson.JSONObject.parseObject(req.getMatchedRefundRule(), SuppGoodsRefundVO.class);
		} catch (Exception e) {
			parsedWell = false;
			LOG.error(e.getMessage());
		}
		
		// 如果解析异常只能滚粗去走人工计算流程
		if (!parsedWell) return detail;
		
		// 如果没有退款规则走人工流程
		if (matchedRule == null) return detail;
		
		// 如果退款类型不为【可退改】或【部分可退改】的话，返回默认detail，走人工流程
		if (!matchedRule.getCancelStrategy().equals("RETREATANDCHANGE") && !matchedRule.getCancelStrategy().equals("PARTRETREATANDCHANGE")) return detail;
		
		// 如果扣掉类型为空的话则无法据此判断使用何种计算公式
		if (matchedRule.getDeductType() == null) return detail;
		
		if (matchedRule.getDeductValue() == null) return detail;
		
		if (matchedRule.getDeductType().equals("PERCENT")) {
			long settlementPrice = (long) ((float) ((subOrder.getQuantity() - req.getRefundedUnits())
					* subOrder.getSettlementPrice())
					+ (float) matchedRule.getDeductValue() / 10000f
							* (float) (req.getRefundedUnits() * subOrder.getSettlementPrice()));
			detail = new TicketSettlementDetail(settlementPrice,
					OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.toString());
		} else if (matchedRule.getDeductType().equals("AMOUNT")) {
			// 如果【单份扣款金额】大于【原结算价格】，走人工流程 			
			if (matchedRule.getDeductValue() <= subOrder.getSettlementPrice()) {
				long settlementPrice = (subOrder.getQuantity() - req.getRefundedUnits()) * subOrder.getSettlementPrice()
						+ matchedRule.getDeductValue() * req.getRefundedUnits();
				detail = new TicketSettlementDetail(settlementPrice,
						OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.toString());
			}
		}

		//如果有外币退改记录则走外币
		if (matchedRule.getCurrencyDeductType() != null) {

			OrdOrderItemExtend extend = ordOrderItemExtendService.selectByPrimaryKey(subOrder.getOrderItemId());
			//如果外币不存在，或者外币结算单价或者结算汇率为null走人工流程
			if (extend == null || extend.getSettlementPriceRate() == null || extend.getForeignSettlementPrice() == null || matchedRule.getCurrencyDeductValue() == null) {
				lvmamaLog.info("修改外币结算价数据null,orderItemId:" + subOrder.getOrderItemId() + ",extend:" + JSONUtil.bean2Json(extend) + ",matchedRule:" + JSONUtil.bean2Json(matchedRule));
				return new TicketSettlementDetail(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.UN_CONFIRMED.toString());
			}

			//结算价修改为退款份数*外币指定结算价
			if ("PERCENT".equals(matchedRule.getCurrencyDeductType())) {
				long settlementPrice = (long) ((float) ((subOrder.getQuantity() - req.getRefundedUnits()) * subOrder.getSettlementPrice())
						+ (float) matchedRule.getCurrencyDeductValue() / 10000f * (float) (req.getRefundedUnits() * subOrder.getSettlementPrice()));
				//计算外币结算价，从extend表外币结算单价计算
				long currencySettlementPrice = (long) ((float) ((subOrder.getQuantity() - req.getRefundedUnits()) * extend.getForeignSettlementPrice())
						+ (float) matchedRule.getCurrencyDeductValue() / 10000f * (float) (req.getRefundedUnits() * extend.getForeignSettlementPrice()));

				detail = new TicketSettlementDetail(settlementPrice, OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.toString());
				detail.setCurrencySettlementPrice(currencySettlementPrice);
				lvmamaLog.info("genSettlementDetail currency percent orderItemId:" + subOrder.getOrderItemId() + "detail:" + JSONUtil.bean2Json(detail));
			} else if ("AMOUNT".equals(matchedRule.getDeductType())) {
				// 如果【单份扣款金额】大于【原结算价格】，走人工流程
				if (matchedRule.getDeductValue() <= subOrder.getSettlementPrice()) {
					//金额由退改设置的每张与供应商结算外币价格*结算汇率*退款份数
					Long currencyDeductPrice = new BigDecimal(matchedRule.getCurrencyDeductValue() * req.getRefundedUnits()).longValue();
					//计算外币结算价，从extend表外币结算单价计算
					long currencySettlementPrice = (subOrder.getQuantity() - req.getRefundedUnits()) * extend.getForeignSettlementPrice()
							+  currencyDeductPrice;

					long settlementPrice = new BigDecimal(currencySettlementPrice).multiply(extend.getSettlementPriceRate()).setScale(0,BigDecimal.ROUND_UP).longValue();

					detail = new TicketSettlementDetail(settlementPrice, OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.toString());
					detail.setCurrencySettlementPrice(currencySettlementPrice);
					lvmamaLog.info("genSettlementDetail currency amount orderItemId:" + subOrder.getOrderItemId() + "detail:" + JSONUtil.bean2Json(detail));
				}
			}

		}

		return detail;
	}
	
	boolean isSettlementDetailNotValid(TicketSettlementDetail detail) {
		if (detail == null || detail.getSettlementPrice() == null || detail.getSettlementPrice() < 0l) {
			return true;
		}
		return false;
	}
	
	// 比较子订单的【原结算总价】和【结算总价】，此处的【结算总价】非本次计算所得的结算总价
	boolean isTotalSettlementPriceChanged(OrdOrderItem subOrder) {
		if (subOrder != null && subOrder.getActualSettlementPrice() != null && subOrder.getQuantity() != null
				&& subOrder.getSettlementPrice() != null) {
			Long originalTotalSettlementPrice = subOrder.getSettlementPrice() * subOrder.getQuantity();
			LOG.info("originalTotalSettlementPrice -> " + originalTotalSettlementPrice);
			Long actualTotalSettlementPrice = subOrder.getActualSettlementPrice() * subOrder.getQuantity();
			LOG.info("actualTotalSettlementPrice -> " + actualTotalSettlementPrice);
			if (actualTotalSettlementPrice.equals(originalTotalSettlementPrice)) {
				return false;
			}
		}
		return true;
	}
	
	boolean isRefundAmountChanged(SettlementDetailAcquisitionReq req) {
		if (req != null && req.getRefundAmount() != null && req.getModifiedRefundAmount() != null) {
			if (req.getRefundAmount().equals(req.getModifiedRefundAmount())) {
				return false;
			}
		}
		return true;
	}
	
	boolean isPriceNotConfirmed(OrdOrderItem subOrder) {
		// 下单之后空值取代了子订单在数据库里的默认价格确认状态——已确认，所以除非该字段是UN_CONFIRMED，我们认为其他值都是已确认
//		if (subOrder != null && subOrder.getPriceConfirmStatus() != null
//				&& subOrder.getPriceConfirmStatus().equals(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED)) {
//			return false;
//		}
		if (subOrder != null) {
			if(subOrder.getPriceConfirmStatus() != null && subOrder.getPriceConfirmStatus().equals(OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.UN_CONFIRMED.toString()))
				return true;
		}
		return false;
	}
	
	boolean isPartRefunded(OrdOrderItem subOrder, SettlementDetailAcquisitionReq req) {
		if (subOrder != null && subOrder.getOrderItemId() != null && req != null
				&& req.getTotalRefundAmount() != null) {
			try {
				OrderItemApportionInfoQueryVO queryParam = new OrderItemApportionInfoQueryVO();
				queryParam.setOrderItemId(subOrder.getOrderItemId());
				OrderItemApportionInfoPO appInfo = apportionInfoQueryService.calcOrderItemApportionInfo(queryParam);
				LOG.info("appInfo -> " + GsonUtils.toJson(appInfo));
				if (req.getTotalRefundAmount().equals(appInfo.getItemTotalActualPaidAmount())) {
					return false;
				}
			} catch (Exception e) {
				LOG.warn("fail 2 get actually paid amount of sub-order due 2 apportion service exception");
				LOG.error(e.getMessage(), e);
			}
		}
		return true;
	}

	TicketSettlementDetail genSettlementDetail4OrderPartRefunded() {
		return new TicketSettlementDetail(
				OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.UN_CONFIRMED.toString());
	}
	
	TicketSettlementDetail genSettlementDetail4OrderFullRefunded() {
		return new TicketSettlementDetail(0l,
				OrderEnum.ORDITEM_PRICE_CONFIRM_STATUS.PRICE_CONFIRMED.toString());
	}
	
	PermUser getPermUser(OrdOrderItem subOrder) {
		PermUser pm = null;
		if (subOrder != null && subOrder.getManagerId() != null) {
			try {
				pm = permUserService.getPermUserByUserId(subOrder.getManagerId());
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
		}
		return pm;
	}
	
	void sendEmail2Pm(PermUser pm, OrdOrderItem subOrder) {
		if (pm != null && pm.getEmail() != null && !pm.getEmail().trim().equals("") && subOrder != null
				&& subOrder.getOrderItemId() != null) {
			EmailContent emailContent = new EmailContent();
			emailContent.setFromAddress("service@cs.lvmama.com");
			emailContent.setContentText("<b>修改退款订单的结算价，子订单号:</b>" + subOrder.getOrderItemId());
			emailContent.setSubject("<b>修改退款订单的结算价，子订单号:</b>" + subOrder.getOrderItemId());
			emailContent.setFromName("驴妈妈旅游网");
			emailContent.setCreateTime(new Date());
			emailContent.setToAddress(pm.getEmail());
			LOG.info("send sub-order[" + subOrder.getOrderId() + ", " + subOrder.getOrderItemId() + "] info 2 "
					+ pm.getEmail());
			try {
				long resp = vstEmailServiceAdapter.sendEmailDirect(emailContent);
				if (resp == 0) {
					LOG.warn("Send email 2 [" + pm.getEmail() + "] failed.");
				}
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
		} else {
			LOG.warn("Can't not send a email to nobody or the email is empty.");
		}
	}
	
	void popUpHint(PermUser pm, OrdOrderItem subOrder) {
		if (pm != null && pm.getUserName() != null && !pm.getUserName().equals("") && subOrder != null
				&& subOrder.getOrderItemId() != null) {
			com.lvmama.comm.pet.po.pub.ComMessage hint = new com.lvmama.comm.pet.po.pub.ComMessage();
			hint.setSender(Constant.SYSTEM_USER);
			hint.setContent("请修改退款订单的结算价(子订单号:" + subOrder.getOrderItemId() + ")");
			LOG.info("popup hint 2 " + pm.getUserName());
			try {
				popupHintService.insertComMessage(hint, Arrays.asList(pm.getUserName()));
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
		} else {
			LOG.warn("Can't not send a popup to nobody or the popup is empty.");
		}
	}
	
	void triggerManualProcess(OrdOrderItem subOrder) {
		PermUser pm = getPermUser(subOrder);
		LOG.info("pm -> " + GsonUtils.toJson(subOrder));
		sendEmail2Pm(pm, subOrder);
		popUpHint(pm, subOrder);
	}
	
	final static public String BUYOUT_FLAG_YES = "Y";
	final static public String BUYOUT_FLAG_NO = "N";



   	@Override
	public void newOrderAuditReceiveTaskMessage(Long orderId){
		// 订单审核消息任务守护Job表，记录
		OrdAuditProcessTask record = ordAuditProcessTaskService.selectByPrimaryKey(orderId);
		if(record == null){
			record = new OrdAuditProcessTask();
			record.setOrderId(orderId);
			record.setStatus("N");
			record.setTimes(0L);
			ordAuditProcessTaskService.insert(record);

			// 发送消息
			orderWorkflowMessageProducer.sendMsg(MessageFactory.newOrderAuditReceiveTaskMessage(orderId));
			LOG.info("newOrderAuditReceiveTaskMessage success, orderId:" + orderId);
		}
	}

   	@Override
   	public boolean handelAuditReceiveTask(OrdOrder order){
   		if(order==null||order.getOrderId() ==null){
   			LOG.info("handelAuditReceiveTask, order is null");
   			return false;
   		}
   		LOG.info("handelAuditReceiveTask, orderId:" + order.getOrderId());
   		if(order.hasCanceled()) {
   			LOG.error("order has been canceled:" + order.getOrderId());
			ordAuditProcessTaskService.makeValid(order.getOrderId());
			return false;
		}
   		
   		ResultHandle handle = new ResultHandle();
   		try {
			ActivitiKey key = createKeyByOrder(order);
			handle = processerClientService.completeTaskWithStatus(key, "orderAuditReceiveTask", "SYSTEM");
		} catch (Exception e) {
			// 启动异常处理
			handle.setMsg("handelAuditReceiveTask error!");
		}

   		if(handle != null && handle.isSuccess()){
			ordAuditProcessTaskService.makeSuccess(order.getOrderId());
			return true;
		}

   		LOG.error("handelAuditReceiveTask error!");
   		ordAuditProcessTaskService.addTimes(order.getOrderId());
   		return false;
   	}
   	
   	
	public ResultHandleT<List<OrdOrderHotelTimeRate>> findOrdOrderHotelTimeRateList(Long orderItemId){
		ResultHandleT<List<OrdOrderHotelTimeRate>> resultHandle = new ResultHandleT<List<OrdOrderHotelTimeRate>>();
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", orderItemId);
		List<OrdOrderHotelTimeRate> ordOrderHotelTimeList = ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateList(params);
		resultHandle.setReturnContent(ordOrderHotelTimeList);
		return resultHandle;
	}

	
	@Override
	public ResultHandleT<OrderApportionInfoPO> calculateOrderApportionInfo(OrderApportionInfoQueryVO orderApportionInfoQueryVO) {
		ResultHandleT<OrderApportionInfoPO> resultHandle = new ResultHandleT<OrderApportionInfoPO>();
		try
		{
			OrderApportionInfoPO orderApportionInfo =apportionInfoQueryService.calculateOrderApportionInfo(orderApportionInfoQueryVO);		
			resultHandle.setReturnContent(orderApportionInfo);
		}catch(Exception e){
			resultHandle.setMsg(e);
		}		
		return resultHandle;
	}

	
	public ResultHandle updateItemActualSettlement(Long actualSettlementPrice,String priceConfirmStatus,Long orderItemId){
		ResultHandle resultHandle = new ResultHandle();
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", orderItemId);
		if(null != actualSettlementPrice){
			params.put("actualSettlementPrice", actualSettlementPrice);
		}
		if(null != priceConfirmStatus){
			params.put("priceConfirmStatus", priceConfirmStatus);
		}
		try
		{
			int res = orderUpdateService.updateOrderItemActSettlement(params);
			
			if(res>0 && null != params.get("priceConfirmStatus")){
				String addition=new StringBuffer(orderItemId+"").append("|").toString();
				sendOrdItemPriceConfirmChangeMsg(orderItemId,addition);
			}
			if(res<=0){
				resultHandle.setMsg("子订单(ID=" + orderItemId + ",priceConfirmStatus" + priceConfirmStatus + ",actualSettlementPrice=" + actualSettlementPrice + ")更新失败。");
			}
		}catch(Exception e)
		{
			LOG.error(ExceptionFormatUtil.getTrace(e));
			resultHandle.setMsg(e);
		}
		return resultHandle;		
	}

	@Override
	public ResultHandleT<String> getDifferentialChildRule(Long orderItemId) {
		ResultHandleT<String> result = new ResultHandleT<String>();
		try
		{
			OrdOrderItem orderItem = getOrderItem(orderItemId);
			Map<String, Object> map = orderDetailAction.differentialChildRule(orderItem);
			String refoundStr = "";
			if(null != map.get("refoundStr")){
				refoundStr = map.get("refoundStr").toString();
			}
			result.setReturnContent(refoundStr);
		}catch(Exception e){
			LOG.error(ExceptionFormatUtil.getTrace(e));
			result.setMsg(e);
		}		
		return result;
	}
	
	/**
	 * 修改主订单和子订单的退款金额和退款份数
	 * @return
	 */
	public ResultHandleT<Object> updateOrdRefundAmountAndQuantity(OrdRefundUpdateVO ordRefundUpdate){
		LOG.info("OrdOrderClientServiceImpl.updateOrdRefundAmountAndQuantity--> start" +GsonUtils.toJson(ordRefundUpdate));
		ResultHandleT<Object> result = new ResultHandleT<Object>();
		try
		{
			LOG.info("OrdOrderClientServiceImpl》》updateOrdRefundAmountAndQuantity》》updateRefundedAmount--> start" );
			//修改主订单退改金额
			updateRefundedAmount(ordRefundUpdate.getOrderId(), ordRefundUpdate.getRefundmentId(), ordRefundUpdate.getAmount());
			LOG.info("OrdOrderClientServiceImpl》》updateOrdRefundAmountAndQuantity》》updateRefundedAmount--> end" );
			LOG.info("OrdOrderClientServiceImpl》》updateOrdRefundAmountAndQuantity》》saveOrdRefundItem--> start" );
			//子单退款记录
			for (OrdRefundItem ordRefundItem : ordRefundUpdate.getOrdRefundItemList()) {
				ordRefundItemService.saveOrdRefundItemSelective(ordRefundItem);
			}						
			LOG.info("OrdOrderClientServiceImpl》》updateOrdRefundAmountAndQuantity》》saveOrdRefundItem--> end" );
		}catch(Exception e)
		{
			LOG.error(ExceptionFormatUtil.getTrace(e));
			result.setMsg(e);
		}	
		LOG.info("OrdOrderClientServiceImpl.updateOrdRefundAmountAndQuantity--> end" );
		return result;
	}
	

	@Override
	public ResultHandleT<OrderCheckAheadTimeVo> checkAheadTime(BuyInfo buyInfo){
        ResultHandleT handle = new ResultHandleT();
        try {
            handle = orderCheckService.checkAheadTime(buyInfo);
        } catch (Exception e) {
            handle.setMsg("checkAheadTime error!");
        }

		return handle;
	}


	@Override
	public String selectByOrderIdKey(Long orderId) {
		try {
			if(orderId == null){
				LOG.info("OrdOrderClientServiceImpl.selectByOrderIdKey-->orderId is null" + orderId);
				return null;
			}
			
			if(orderId != null){
				OrdAdditionStatus status = ordAdditionStatusService.selectByOrderIdKey(orderId);
				if(StringUtil.isNotEmptyString(status.getStatus())){
					return status.getStatus();
				} 
			}
		} catch (Exception e) {
			LOG.info("selectByOrderIdKey:" + orderId,e);
		}
		return null;
	}	

	
	@Override
	public ResultHandleT<Integer> addOrdRefundmentLog(OrdRefundmentLog ordRefundmentLog){
		ResultHandleT<Integer> resultHandle = new ResultHandleT<Integer>();
		try
		{
			LOG.info("addOrdRefundmentLog orderId start:"+ordRefundmentLog.getOrderId());
			int result = ordRefundmentLogService.addOrdRefundmentLog(ordRefundmentLog);			
			resultHandle.setReturnContent(Integer.valueOf(result));
		}catch(Exception e)
	  {
		LOG.error("addOrdRefundmentLog error"+e);
		resultHandle.setMsg(e);
	  }
		return resultHandle;
	}



	@Override
	public List<OrdOrderHotelTimeRate> queryOrderHotelTimeRate(Long orderItemId)
	{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", orderItemId);
		List<OrdOrderHotelTimeRate> orderHotelTimeRateList = ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateList(params);

		return orderHotelTimeRateList;
	}

	@Override
	public List<MarkCouponUsage> queryMarkCouponUsageByOrderId(Long orderId)
	{
		List<MarkCouponUsage> couponList = favorServiceAdapter.getMarkCouponUsageByOrderId(orderId);

		return couponList;
	}

	@Override
	public List<UserCouponVO> getHotelcombUserCouponVOList(com.lvmama.vst.comm.vo.order.OrdOrderDTO orderDto,
			BuyInfo buyInfo) {
		LOG.info("插入...getLoginUserAccountInformation下单方法中接收到的JSON"+buyInfo.toJsonStr());
		Long categoryId = getCategoryIdByBuyInfo(buyInfo);//获取产品的品类Id
		OrdOrderItem orderMainItem = null;
		Long orderAmount = orderDto.getOughtAmount();//存储订单的总金额（除去保险和快递的金额）
		Long quantity = Long.valueOf(buyInfo.getQuantity());
		for (OrdOrderItem orderItem : orderDto.getOrderItemList()){
			if(quantity==0L && "true".equals(orderItem.getMainItem())){
				if(null != orderItem.getQuantity() && 0L!=orderItem.getQuantity()){
					quantity = orderItem.getQuantity();
					orderMainItem = orderItem;
				}else{
					Long adultQuantity = orderItem.getAdultQuantity();
					Long childQuantity = orderItem.getChildQuantity();
					quantity = adultQuantity.longValue()+childQuantity.longValue();
				}
			}
			//去除保险快递的费用
			if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){
			    Long insuranceQuantity = 1L;
			    if(null != orderItem.getQuantity()){
			    	insuranceQuantity = orderItem.getQuantity();
			    }
				LOG.info("扣除保险费 :"+orderItem.getPrice()*insuranceQuantity);
				orderAmount = orderAmount-orderItem.getPrice()*insuranceQuantity;
			}
			//去除快递押金的费用
			if(BizEnum.BIZ_CATEGORY_TYPE.category_other.getCategoryId().equals(orderItem.getCategoryId())){
				if(ProdProduct.PRODUCTTYPE.DEPOSIT.name().equals(OrderUtil.getProductType(orderItem))){
					Long depositQuantity = 1L;
					if(null != orderItem.getQuantity()){
						depositQuantity = orderItem.getQuantity();
					}
					LOG.info("扣除押金费 :"+orderItem.getPrice());
					orderAmount = orderAmount  - orderItem.getPrice()*depositQuantity;
				}else{
					LOG.info("扣除快递费 :"+orderItem.getPrice());
					orderAmount = orderAmount - orderItem.getPrice();
				}

			}

		}
		if(quantity==0L){
			quantity=1l;
		}
		//判断商品品类 和产品品类，wifi是否可用优惠券
		if( innerValidateGoodsCategory(categoryId) || innerValidateProductCategory(categoryId))	{

			ProdProduct product = null;
			if(null != buyInfo.getProductId()){
//				ResultHandleT<ProdProduct> result = prodProductClientService.findProdProductById(buyInfo.getProductId());
				ResultHandleT<ProdProduct> result = productHotelAdapterClientService.findProdProductByIdFromCache(buyInfo.getProductId());
				if(null != result && null != result.getReturnContent()){
					product = result.getReturnContent();
				}
			}
			//判断是否可使用优惠券
			String icCoupon = isUseCoupon(buyInfo, orderMainItem, product);
			if("N".equalsIgnoreCase(icCoupon)){
				UserCouponVO userCoupon = new UserCouponVO();
				userCoupon.setValidInfo("本产品不支持使用优惠券!");
				List<UserCouponVO> userCouponList = new ArrayList<UserCouponVO>();
				userCouponList.add(userCoupon);
				return userCouponList;
			}
		}
		//优惠券券号
		List<UserCouponVO> couponCodeVOList = buyInfo.getUserCouponVoList();
		Long userId = buyInfo.getUserNo();
		//优惠券验证参数
		List<CouponCheckParam> couponCheckParams = null;
		List<OrdOrderPack> orderPackList = orderDto.getOrderPackList();
		CouponCheckParam couponCheckParam = null;
		if(CollectionUtils.isNotEmpty(orderPackList)){
			couponCheckParams = new ArrayList<CouponCheckParam>();
			for (OrdOrderPack pack : orderPackList) {
				LOG.info("优惠券接口参数4######******SubCategoryId :"+orderDto.getSubCategoryId()+"#######");
				couponCheckParam = new CouponCheckParam();
				couponCheckParam.setUserId(userId);
				couponCheckParam.setProductType(pack.getProduct().getProductType());
				if(null ==pack.getCategoryId() ||pack.getCategoryId()==0L){
					couponCheckParam.setCategoryId(getCategoryIdByBuyInfo(buyInfo));
				}else if(pack.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId())){//20161010 CHENHAO自由行产品
					if(orderDto.getSubCategoryId()!=null && (orderDto.getBuCode().equalsIgnoreCase(CommEnumSet.BU_NAME.LOCAL_BU.getCode())
							|| orderDto.getBuCode().equalsIgnoreCase(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode())
							|| orderDto.getBuCode().equalsIgnoreCase(CommEnumSet.BU_NAME.DESTINATION_BU.getCode()))){
						//couponCheckParam.setProductType(null);
						couponCheckParam.setCategoryId(orderDto.getSubCategoryId());
					}else{
						couponCheckParam.setCategoryId(pack.getCategoryId());
					}
				}else{
					//TODO 测试petpublic补丁是否有效
//					if (BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().equals(pack.getCategoryId())){
//						couponCheckParam.setProductType(null);
//					}
				    couponCheckParam.setCategoryId(pack.getCategoryId());
				}
				couponCheckParam.setPlatform(generatePlatformStr(buyInfo));
				couponCheckParam.setSaleUnitType("PROD");
				couponCheckParam.setSaleUnitId(pack.getProductId());
				changeCouponCheckParamAddBuForHotelcomb(couponCheckParam,orderDto);
				couponCheckParams.add(couponCheckParam);
				String json = JSONUtil.bean2Json(couponCheckParam);
				LOG.info("优惠券接口参数4######******couponCheckParam json:"+json+"#######");
			}
			for (OrdOrderItem it:orderDto.getOrderItemList()) {
				couponCheckParam = new CouponCheckParam();
				couponCheckParam.setUserId(userId);
				couponCheckParam.setCategoryId(it.getCategoryId());
				couponCheckParam.setPlatform(generatePlatformStr(buyInfo));
				couponCheckParam.setSaleUnitType("BRANCH");//BRANCH or PROD
				couponCheckParam.setSaleUnitId(it.getSuppGoods().getSuppGoodsId());
				couponCheckParam.setProductType(it.getSuppGoods().getProdProduct().getProductType());
//    			if (BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().equals(it.getSuppGoods().getProdProduct().getCategoryId())){
//    				couponCheckParam.setProductType(null);
//    			}
				changeCouponCheckParamAddBuForHotelcomb(couponCheckParam,orderDto);
				couponCheckParams.add(couponCheckParam);
				String json = JSONUtil.bean2Json(couponCheckParam);
				LOG.info("优惠券接口参数5######******couponCheckParam json:"+json+"#######");
			}
		}else{
			couponCheckParams = couponCheckParamsForHotelcomb(orderDto, buyInfo);
		}
		LOG.info("ELSE优惠券接口参数3######******couponCheckParam json:"+JSONUtil.bean2Json(couponCheckParams)+"#######");
		List<String> couponCodeList = new ArrayList<String>();
		for(UserCouponVO vo:couponCodeVOList){
			couponCodeList.add(vo.getCouponCode());
			LOG.info("==========cm============couponCodeVOList.coupon.code:"+vo);

		}
		LOG.info("####*****method getUserCouponVOList userid is "+userId+"and oughtAmount is "+orderAmount+"*****#####"+"quantity="+quantity);
	    List<UserCouponVO> couplist = favorServiceAdapter.getUserCouponVOList(couponCheckParams, couponCodeList, orderAmount,quantity);
	    if( null != couplist ){
	    	for(UserCouponVO vo:couplist){
				LOG.info("==========cm============favorServiceAdapter.getUserCouponVOList:"+vo);
			}
	    }
	    
		return couplist;
	}
	private void changeCouponCheckParamAddBuForHotelcomb(CouponCheckParam couponCheckParam, com.lvmama.vst.comm.vo.order.OrdOrderDTO ordOrderDTO){
		String bu = ordOrderDTO.getBuCode();
		if(StringUtils.isNotBlank(bu)){
			couponCheckParam.setBu(bu);
		}
	}
	
	private List<CouponCheckParam> couponCheckParamsForHotelcomb(com.lvmama.vst.comm.vo.order.OrdOrderDTO order,BuyInfo buyinfo){
		List<CouponCheckParam> couponCheckParams = new ArrayList<CouponCheckParam>();
		ProdProductParam param = new ProdProductParam();
		param.setProductProp(true);
		param.setProductBranchValue(true);
		param.setProdEcontract(true);
		CouponCheckParam couponCheckParam = null;
		List<OrdOrderItem> ordItemList = order.getOrderItemList();
		for (OrdOrderItem ordOrderItem : ordItemList) {
			couponCheckParam = new CouponCheckParam();
			couponCheckParam.setUserId(buyinfo.getUserNo());
			couponCheckParam.setCategoryId(getCategoryIdByBuyInfo(buyinfo));
			couponCheckParam.setPlatform(generatePlatformStr(buyinfo));
			couponCheckParam.setSaleUnitType("PROD");
			couponCheckParam.setSaleUnitId(ordOrderItem.getProductId());
//			ResultHandleT<ProdProduct> resultHandle = prodProductClientService.findLineProductByProductId(buyinfo.getProductId(), param);
			ResultHandleT<ProdProduct> resultHandle = productHotelAdapterClientService.findProdProductByIdFromCache(buyinfo.getProductId());
			ProdProduct product = resultHandle.getReturnContent();
			couponCheckParam.setProductType(product.getProductType());
			//wifi/当地玩乐设置产品类型为空，暂时解决wifi品类绑定问题
			if(BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(product.getBizCategoryId())||
					BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(product.getBizCategoryId())||
					BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(product.getBizCategoryId())||
					OrderUtil.isWifiCategory(ordOrderItem)){
				couponCheckParam.setProductType(null);
			}
			changeCouponCheckParamAddBuForHotelcomb(couponCheckParam,order);
			couponCheckParams.add(couponCheckParam);
			String json = JSONUtil.bean2Json(couponCheckParam);
			LOG.info("优惠券接口参数6######******PROD couponCheckParam json:"+json+"#######");
		}
		for (OrdOrderItem ordOrderItem : ordItemList) {
			couponCheckParam = new CouponCheckParam();
			couponCheckParam.setUserId(buyinfo.getUserNo());
			couponCheckParam.setCategoryId(ordOrderItem.getSuppGoods().getCategoryId());
			couponCheckParam.setPlatform(generatePlatformStr(buyinfo));
			couponCheckParam.setSaleUnitType("BRANCH");
			couponCheckParam.setSaleUnitId(ordOrderItem.getSuppGoodsId());
			couponCheckParam.setProductType(ordOrderItem.getSuppGoods().getProdProduct().getProductType());
			//wifi/当地玩乐设置产品类型为空，暂时解决wifi品类绑定问题
			if(BizEnum.BIZ_CATEGORY_TYPE.category_food.getCategoryId().equals(ordOrderItem.getSuppGoods().getProdProduct().getBizCategoryId())||
					BizEnum.BIZ_CATEGORY_TYPE.category_sport.getCategoryId().equals(ordOrderItem.getSuppGoods().getProdProduct().getBizCategoryId())||
					BizEnum.BIZ_CATEGORY_TYPE.category_shop.getCategoryId().equals(ordOrderItem.getSuppGoods().getProdProduct().getBizCategoryId())
					||OrderUtil.isWifiCategory(ordOrderItem)){
				couponCheckParam.setProductType(null);
			}
			changeCouponCheckParamAddBuForHotelcomb(couponCheckParam,order);
			couponCheckParams.add(couponCheckParam);
			String json = JSONUtil.bean2Json(couponCheckParam);
			LOG.info("优惠券接口参数7######******BRANCH couponCheckParam json:"+json+"#######");
		}
		return couponCheckParams;
	}
	
	@Override
	public void updateOrdTravelContract(Map<String, Object> params) {
		ordTravelContractService.updateContractStatusByOrderId(params);
	}
	
	
	@Override
	public OrdOrder queryHotelOrderByOrderId(Long orderId) {
		OrdOrder ordOrder = ordOrderDao.selectByPrimaryKey(orderId);
		if (ordOrder != null) {
			List<OrdOrderItem> ordOrderItemList = iOrdOrderItemService.selectByOrderId(ordOrder.getOrderId());
			if (ordOrderItemList != null && ordOrderItemList.size() > 0) {
				for (OrdOrderItem orderItem : ordOrderItemList) {
					if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())) {
						Map<String, Object> itemParams = new HashMap<String, Object>();
						itemParams.put("orderItemId", orderItem.getOrderItemId());
						itemParams.put("_orderby", "VISIT_TIME");
						itemParams.put("_order", "desc");
						orderItem.setOrderHotelTimeRateList(ordOrderHotelTimeRateService.findOrdOrderHotelTimeRateList(itemParams));
					}
				}
			}
			ordOrder.setOrderItemList(ordOrderItemList);
		}
		return ordOrder;

	}


	@Override
	public int updateO2oOrder(O2oOrder o2oOrder) {
		return o2oOrderService.updateO2oOrder(o2oOrder);
	}


	@Override
	public void splitOrdRefundmentUpdateSettlement(Long refundmentId) {
		orderMessageProducer.sendMsg(MessageFactory.newOrderRefumentOkMessage(refundmentId));
	}
	
	@Override
	public List<UserCouponVO> getHotelcombUserCouponList(com.lvmama.vst.comm.vo.order.OrdOrderDTO order,
			BuyInfo buyInfo) {
		List<CouponCheckParam> couponCheckParams = null;
		Long userId = buyInfo.getUserNo();
		Long quantity = Long.valueOf(buyInfo.getQuantity());//初始化购买份数
		Long categoryId = getCategoryIdByBuyInfo(buyInfo);//获取产品的品类Id

		LOG.info("登录...getLoginUserAccountInformation下单方法中接收到的JSON"+buyInfo.toJsonStr());

		Long orderAmount = order.getOughtAmount();//存储订单的总金额（除去保险和快递的金额）
		LOG.info("获取品类ID是 :"+categoryId+"订单总价："+orderAmount);
		OrdOrderItem orderMainItem = null;

		for (OrdOrderItem orderItem : order.getOrderItemList()){
			if(quantity==0L && "true".equals(orderItem.getMainItem())){
				if(null != orderItem.getQuantity() && 0L!=orderItem.getQuantity()){
					quantity = orderItem.getQuantity();
					orderMainItem = orderItem;

				}else{
					Long adultQuantity = orderItem.getAdultQuantity();
					Long childQuantity = orderItem.getChildQuantity();
					quantity = adultQuantity.longValue()+childQuantity.longValue();
				}
			}
			//去除保险快递的费用
			if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCategoryId().equals(orderItem.getCategoryId())){
			    Long insuranceQuantity = 1L;
			    if(null != orderItem.getQuantity()){
			    	insuranceQuantity = orderItem.getQuantity();
			    }
				LOG.info("扣除保险费 :"+orderItem.getPrice()*insuranceQuantity);
				orderAmount = orderAmount-orderItem.getPrice()*insuranceQuantity;
			}
			//去除快递押金的费用
			if(BizEnum.BIZ_CATEGORY_TYPE.category_other.getCategoryId().equals(orderItem.getCategoryId())){
				if(ProdProduct.PRODUCTTYPE.DEPOSIT.name().equals(OrderUtil.getProductType(orderItem))){
					Long depositQuantity = 1L;
					if(null != orderItem.getQuantity()){
						depositQuantity = orderItem.getQuantity();
					}
					LOG.info("扣除押金费 :"+orderItem.getPrice());
					orderAmount = orderAmount  - orderItem.getPrice()*depositQuantity;
				}else{
					LOG.info("扣除快递费 :"+orderItem.getPrice());
					orderAmount = orderAmount - orderItem.getPrice();
				}

			}

		}
		if(quantity==0L){
			quantity=1l;
		}

		LOG.info("获取份数是 :"+quantity);
		//判断门票是否可用优惠券
		if(innerValidateGoodsCategory(categoryId) || innerValidateProductCategory(categoryId))
		{
			LOG.info("属于门票的产品，进入验证是否支持优惠券");
			ProdProduct product = null;
			if(null != buyInfo.getProductId()){
				ResultHandleT<ProdProduct> restult = productHotelAdapterClientService.findProdProductByIdFromCache(buyInfo.getProductId());
				if(null != restult && null != restult.getReturnContent()){
					product = restult.getReturnContent();
				}
			}
			//判断是否可使用优惠券
			String icCoupon = isUseCoupon(buyInfo, orderMainItem, product);
			if("N".equalsIgnoreCase(icCoupon)){
				UserCouponVO userCoupon = new UserCouponVO();
				userCoupon.setValidInfo("本产品不支持使用优惠券!");
				List<UserCouponVO> userCouponList = new ArrayList<UserCouponVO>();
				userCouponList.add(userCoupon);
				return userCouponList;
			}
		}

		CouponCheckParam couponCheckParam = null;
		if(CollectionUtils.isNotEmpty(order.getOrderPackList())){
			couponCheckParams = new ArrayList<CouponCheckParam>();
			for (OrdOrderPack p:order.getOrderPackList()) {
				LOG.info("优惠券接口参数1######******SubCategoryId :"+order.getSubCategoryId()+"#######");
				couponCheckParam = new CouponCheckParam();
				couponCheckParam.setUserId(userId);
				couponCheckParam.setProductType(p.getProduct().getProductType());
				if(null ==p.getCategoryId() || p.getCategoryId()==0L){
				 couponCheckParam.setCategoryId(getCategoryIdByBuyInfo(buyInfo));
				}else if(p.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom.getCategoryId())){//20161013 CHENHAO自由行产品
					if(order.getSubCategoryId()!=null && (order.getBuCode().equalsIgnoreCase(CommEnumSet.BU_NAME.LOCAL_BU.getCode())
							|| order.getBuCode().equalsIgnoreCase(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode())
							|| order.getBuCode().equalsIgnoreCase(CommEnumSet.BU_NAME.DESTINATION_BU.getCode()))){
						//couponCheckParam.setProductType(null);
						couponCheckParam.setCategoryId(order.getSubCategoryId());
					}else{
						couponCheckParam.setCategoryId(p.getCategoryId());
					}
				}else{
//					if (BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().equals(p.getCategoryId())){
//						couponCheckParam.setProductType(null);
//					}
					couponCheckParam.setCategoryId(p.getCategoryId());
				}
				couponCheckParam.setPlatform(generatePlatformStr(buyInfo));
				couponCheckParam.setSaleUnitType("PROD");//BRANCH or PROD
				couponCheckParam.setSaleUnitId(p.getProductId());
				changeCouponCheckParamAddBuForHotelcomb(couponCheckParam,order);
				couponCheckParams.add(couponCheckParam);
				String json = JSONUtil.bean2Json(couponCheckParam);
				LOG.info("优惠券接口参数1######******couponCheckParam json:"+json+"#######");
			}
			for (OrdOrderItem it:order.getOrderItemList()) {
				 couponCheckParam = new CouponCheckParam();
				couponCheckParam.setUserId(userId);
				couponCheckParam.setCategoryId(it.getSuppGoods().getCategoryId());
				couponCheckParam.setPlatform(generatePlatformStr(buyInfo));
				couponCheckParam.setSaleUnitType("BRANCH");//BRANCH or PROD
				couponCheckParam.setSaleUnitId(it.getSuppGoodsId());
				couponCheckParam.setProductType(it.getSuppGoods().getProdProduct().getProductType());
//    			if (BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId().equals(it.getSuppGoods().getProdProduct().getCategoryId())){
//    				couponCheckParam.setProductType(null);
//    			}
				changeCouponCheckParamAddBuForHotelcomb(couponCheckParam,order);
				couponCheckParams.add(couponCheckParam);
				String json = JSONUtil.bean2Json(couponCheckParam);
				LOG.info("优惠券接口参数2######******couponCheckParam json:"+json+"#######");
			}
		}else{
			couponCheckParams = couponCheckParamsForHotelcomb(order, buyInfo);
		}
		LOG.info("ELSE优惠券接口参数3######******couponCheckParam json:"+JSONUtil.bean2Json(couponCheckParams)+"#######");
		LOG.info("####*****method getUserCouponList userid is "+userId+"and oughtAmount is "+orderAmount+"*****#####"+"quantity="+quantity);
		List<UserCouponVO> userCouplist = favorServiceAdapter.getUserCouponList(couponCheckParams, userId, orderAmount,quantity);
		LOG.info("优惠券返回结果userCouplist："+userCouplist.size());
		return userCouplist;
	}


	@Override
	public Map<String, Object> updatePaymentApportionAmount(Long orderId, Long orderItemId, Long preferentialAmount) {
		Map<String, Object> result = new HashMap<>();
		try {
			LOG.info("begin******updatePaymentApportionAmount******* orderId:" + orderId + " orderItemId:" + orderItemId + " preferentialAmount:" + preferentialAmount);
			if (orderId == null || preferentialAmount == null || orderItemId == null){
				result.put("code", 100);
				result.put("msg", "参数错误！");
				return result;
			}
			//更新分摊价
			OrderItemApportionState orderItemApportionState = new OrderItemApportionState();
			orderItemApportionState.setOrderId(orderId);
			orderItemApportionState.setOrderItemId(orderItemId);
			orderItemApportionState.setApportionAmount(preferentialAmount);
			orderItemApportionState.setCostCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_distributor.name());
			int upStateAmount= orderItemApportionStateService.updateAmountByParam(orderItemApportionState);

			OrdOrderCostSharingItem orderCostSharingItem = new OrdOrderCostSharingItem();
			orderCostSharingItem.setOrderId(orderId);
			orderCostSharingItem.setOrderItemId(orderItemId);
			orderCostSharingItem.setAmount(preferentialAmount);
			orderCostSharingItem.setCostCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_distributor.name());
			int upSharingAmount = orderCostSharingItemService.updateAmountByParam(orderCostSharingItem);


			//拉取订单售价 用于计算应退金额
			OrdOrderItem orderItem = ordOrderItemService.selectOrderItemByOrderItemId(orderItemId);
			if (orderItem == null || orderItem.getTotalAmount() == null){
				result.put("code", 101);
				result.put("msg", "orderId:" + orderId + "orderItemId:" + orderItemId + " 还未完成支付分摊！");
				return result;
			}
			//更新支付价
			Long payApportionAmount = orderItem.getTotalAmount() - preferentialAmount;

			orderItemApportionState.setApportionAmount(payApportionAmount);
			orderItemApportionState.setCostCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment.name());
			int upStatePayAmount= orderItemApportionStateService.updateAmountByParam(orderItemApportionState);

			orderCostSharingItem.setAmount(payApportionAmount);
			orderCostSharingItem.setCostCategory(OrderEnum.ORDER_APPORTION_TYPE.apportion_type_payment.name());
			int upSharingPayAmount = orderCostSharingItemService.updateAmountByParam(orderCostSharingItem);

			if (upStateAmount <= 0 || upSharingAmount <=0 || upStatePayAmount <=0 || upSharingPayAmount <=0){
				result.put("code", 101);
				result.put("msg", "orderId:" + orderId + "orderItemId:" + orderItemId + " 还未完成支付分摊！");
				return result;
			}

		}catch (Exception e){
			result.put("code", 500);
			result.put("msg", e);
			return result;
		}
		result.put("code", 200);
		result.put("msg", "SUCCESS");
		return result;
	}

	/**
	 * 判断主站支付分摊是否完成
	 * @param orderId
	 * @param orderItemIds
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean isPaymentApportionSuccess(Long orderId, List<Long> orderItemIds, List<String> costCategoryList) {
		int size = 2*orderItemIds.size();
		OrderCostSharingItemQueryVO orderCostSharingItemQueryVO = new OrderCostSharingItemQueryVO();
		orderCostSharingItemQueryVO.setOrderId(orderId);
		orderCostSharingItemQueryVO.setOrderItemIdList(orderItemIds);
		orderCostSharingItemQueryVO.setCostCategoryList(costCategoryList);
		List<OrdOrderCostSharingItem> ordOrderCostSharingItems = orderCostSharingItemService.queryOrdOrderCostSharingItemList(orderCostSharingItemQueryVO);
		if (CollectionUtils.isNotEmpty(ordOrderCostSharingItems) && ordOrderCostSharingItems.size() >= size) {
			OrderItemApportionStateQueryVO orderItemApportionStateQueryVO = new OrderItemApportionStateQueryVO();
			orderItemApportionStateQueryVO.setOrderId(orderId);
			orderItemApportionStateQueryVO.setOrderItemIdList(orderItemIds);
			orderItemApportionStateQueryVO.setCostCategoryList(costCategoryList);
			List<OrderItemApportionState> orderItemApportionStates = orderItemApportionStateService.queryOrderItemApportionStateList(orderItemApportionStateQueryVO);
			if (CollectionUtils.isNotEmpty(orderItemApportionStates) && orderItemApportionStates.size() >= size)
				return true;
		}
		return false;
	}

    @Override
    public void createApportionSuccessMsg(Long orderId) {
        LOG.info("createApportionSuccessMsg createMsg  ORDER_APPORTION_SUCCESS_MSG"+orderId  + " by tnt ");
        orderMessageProducer.sendMsg(MessageFactory.newOrderApportionSuccessMessage(orderId));
    }

	@Override
	public List<Long> getStampByUserId(String userId, int start, int end) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userId", userId);
		paramMap.put("_start", start);
		paramMap.put("_end", end);
		LOG.info("getStampByUserId with " + JSON.toJSONString(paramMap));
		return ordOrderDao.getStampByUserId(paramMap);
	}

	@Override
	public int updateSubTypeByOrderId(String subType, long orderId) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("subType", subType);
		paramMap.put("orderId", orderId);
		LOG.info("updateSubTypeByOrderId with " + JSON.toJSONString(paramMap));
		return ordOrderDao.updateSubTypeByOrderId(paramMap);
	}

	@Override
	public Long countStampByUserId(String userId) {
		LOG.info("countStampByUserId with " + userId);
		return ordOrderDao.countStampByUserId(userId);
	}

	@Override
	public int updateWaitPaymentTimeByOrderId(Long orderId) {
		LOG.info("updateWaitPaymentTimeByOrderId with " + orderId);
		return ordOrderDao.updateWaitPaymentTimeByOrderId(orderId);
	}

	@Override
	public OrdOrder findByOrderId(Long orderId) {
		LOG.info("findByOrderId with " + orderId);
		return iOrdOrderService.findByOrderId(orderId);
	}

	@Override
	public int updateItemSubTypeByOrderId(String subType, Long orderId) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("subType", subType);
		paramMap.put("orderId", orderId);
		LOG.info("updateItemSubTypeByOrderId with " + JSON.toJSONString(paramMap));
		return ordOrderItemDao.updateSubTypeByOrderId(paramMap);
	}

	@Override
	public ResultHandle saveStampOrder(OrdStampOrder ord) {
		Assert.notNull(ord);
		String json = JSON.toJSONString(ord);
		LOG.info("saveStampOrder with " + json);
        ResultHandle resultHandle=new ResultHandle();
        try {
            int i = ordStampOrderDao.insertSelective(ord);
            if(i <= 0) {
            	LOG.error("saveStampOrder fail with " + json);
            	resultHandle.setMsg("saveStampOrder fail");
            }
        } catch (Exception e) {
        	LOG.error(ExceptionFormatUtil.getTrace(e));
            resultHandle.setMsg(e);
        }
        return resultHandle;
	}

	@Override
	public ResultHandle saveStampOrderItem(OrdStampOrderItem item) {
		Assert.notNull(item);
		String json = JSON.toJSONString(item);
		LOG.info("saveStampOrderItem with " + json);
        ResultHandle resultHandle=new ResultHandle();
        try {
            int i = ordStampOrderItemDao.insertSelective(item);
            if(i <= 0) {
            	LOG.error("saveStampOrderItem fail with " + json);
            	resultHandle.setMsg("saveStampOrderItem fail");
            }
        } catch (Exception e) {
        	LOG.error(ExceptionFormatUtil.getTrace(e));
            resultHandle.setMsg(e);
        }
        return resultHandle;
	}

	@Override
	public OrdStampOrder findStampByOrderId(Long orderId) {
		LOG.info("findStampByOrderId with " + orderId);
		return ordStampOrderDao.selectByPrimaryKey(orderId);
	}

	@Override
	public int updateStampOrder(OrdStampOrder ord) {
		LOG.info("updateStampOrder with " + JSON.toJSONString(ord));
		return ordStampOrderDao.updateByPrimaryKeySelective(ord);
	}

	@Override
	public OrdStampOrderItem findStampItemByOrderItemId(Long orderItemId) {
		LOG.info("findStampItemByOrderItemId with " + orderItemId);
		return ordStampOrderItemDao.selectByPrimaryKey(orderItemId);
	}

	@Override
	public OrdStampOrderItem findStampItemByOrderId(Long orderId) {
		LOG.info("findStampItemByOrderId with " + orderId);
		return ordStampOrderItemDao.selectByOrderId(orderId);
	}

	@Override
	public Long countByStampDefinitionId(String stampDefinitionId) {
		LOG.info("countByStampDefinitionId with " + stampDefinitionId);
		return ordStampOrderItemDao.countByStampDefinitionId(stampDefinitionId);
	}

	@Override
	public List<StampOrderVo> queryStampOrder(String stampId, String orderId, String contactName, String contactMobile,
			int startRow, int pageSize) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("_start", startRow);
		paramMap.put("_end", startRow+pageSize);
		paramMap.put("stampId", stampId);
		paramMap.put("orderId", orderId);
		paramMap.put("contactName", contactName);
		paramMap.put("contactMobile", contactMobile);
		LOG.info("queryStampOrder with " + JSON.toJSONString(paramMap));
		return ordStampOrderDao.queryStampOrder(paramMap);
	}

	@Override
	public Long countStampOrder(String stampId, String orderId, String contactName, String contactMobile) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("stampId", stampId);
		paramMap.put("orderId", orderId);
		paramMap.put("contactName", contactName);
		paramMap.put("contactMobile", contactMobile);
		LOG.info("countStampOrder with " + JSON.toJSONString(paramMap));
		return this.ordStampOrderDao.countStampOrder(paramMap);
	}

	@Override
	public ResultHandleT<List<ResPrecontrolOrderVo>> selectByVisitTimeAndGoodsId(Date startDate, Date endDate, List<Long> goodIds) {
		ResultHandleT<List<ResPrecontrolOrderVo>> resultHandleT = new ResultHandleT<>();
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("startDate", startDate);
			paramMap.put("endDate", endDate);
			paramMap.put("goodIds", goodIds);
			LOG.info("selectByVisitTimeAndGoodsId with " + JSON.toJSONString(paramMap));
			List<ResPrecontrolOrderVo> resPrecontrolOrderVos =  this.ordOrderDao.selectByVisitTimeAndGoodsId(paramMap);
			resultHandleT.setReturnContent(resPrecontrolOrderVos);
		} catch (Exception e) {
			LOG.error("===>OrdOrderClientServiceImplselectByVisitTimeAndGoodsId.exception",e);
			resultHandleT.setMsg("===>OrdOrderClientServiceImplselectByVisitTimeAndGoodsId.exception"+e.getMessage());
		}
		return resultHandleT;
	}

	@Override
	public ResultHandle insertPushOrderBatch(Map<String, Object> params) {
		ResultHandle resultHandle = new ResultHandle();
		try {
			this.ordOrderDao.insertPushOrderBatch(params);
		} catch (Exception e) {
			LOG.error("===>OrdOrderClientServiceImpl.insertPushOrderBatch.exception", e);
			resultHandle.setMsg("===>OrdOrderClientServiceImpl.insertPushOrderBatch.exception" + e.getMessage());
		}
		return resultHandle;
	}

	@Override
	public ResultHandle insertPushOrderBatchHotel(Map<String, Object> params) {
		ResultHandle resultHandle = new ResultHandle();
		try {
			this.ordOrderDao.insertPushOrderBatchHotel(params);
		} catch (Exception e) {
			LOG.error("===>OrdOrderClientServiceImpl.insertPushOrderBatchHotel.exception", e);
			resultHandle.setMsg("===>OrdOrderClientServiceImpl.insertPushOrderBatchHotel.exception" + e.getMessage());
		}
		return resultHandle;
	
	}
	
	/**
	 * 是否是大交通品类
	 * @param categorId
	 * @return
	 */
	private boolean  innerIsCategoryTraffic(Long categorId ){
		if(categorId == null){
			return false ;
		}
		
		return categorId.equals(BizEnum.BIZ_CATEGORY_TYPE.category_traffic.getCategoryId()) 
				|| categorId.equals(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aeroplane.getCategoryId()) 
				|| categorId.equals(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_aero_other.getCategoryId())
				|| categorId.equals(BizEnum.BIZ_CATEGORY_TYPE.category_traffic_train_other.getCategoryId())
				|| categorId.equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_aero_hotel.getCategoryId())
				;
				
	}
	
	  /**
     * 根据子订单ID，audit type查询 audit
     */
    public List<ComAudit> selectAuditByAuditType(Long objectId,String auditType) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("objectId", objectId);
        param.put("auditType", auditType);
        List<ComAudit> list = orderAuditService.queryAuditListByParam(param);
        return list;
    }
    
    @Override
	public void updateByPrimaryKeyNew(ComAudit audit) {
		orderAuditService.updateByPrimaryKeyNew(audit);
		
	}



	@Override
	public ResultHandleT preLockSeat(BuyInfo form, String loginUserId) {
		ResultHandle resultHandle=new ResultHandle();
		ResultHandleT<Object> resultHandleT =new ResultHandleT<>();
		long start = System.currentTimeMillis();
		OrdOrder temOrder = this.bookService.initOrderItems(form);
		long end = System.currentTimeMillis();
		LOG.info("前置锁仓发起锁仓initOrderAndCalc  cost:"+(end-start) +"毫秒");
		Map<String, String> map=new HashMap<String, String>();
		List<OrdOrderItem> apiFlightList = new ArrayList<OrdOrderItem>();
		long start1 = System.currentTimeMillis();
		for(OrdOrderItem orderItem : temOrder.getOrderItemList()) {
			if(orderItem.isApiFlightTicket()) {
				Long orderItemId=ordOrderItemDao.getOrderItemIdForPreLockSeat();
				orderItem.setOrderItemId(orderItemId);
				map.put(orderItem.getSuppGoodsId().toString(), orderItemId.toString());
				apiFlightList.add(orderItem);
			}
		}
		if(apiFlightList.size()==0){
			return resultHandleT;
		}
		Long orderId=ordOrderDao.getOrderIdForPreLockSeat();
		temOrder.setOrderId(orderId);
		map.put("0", orderId.toString());
		long end1 = System.currentTimeMillis();
		LOG.info("前置锁仓发起锁仓生成orderId和orderItemId  cost:"+(end1-start1) +"毫秒");
		long start2 = System.currentTimeMillis();
		try {
			resultHandle = flightOrderProcessService.preLockSeat(temOrder, apiFlightList);
		} catch (Exception e) {
			LOG.info("{前置锁仓发起锁仓失败!}", e);
			resultHandle.setMsg("前置锁仓发起锁仓失败!");
		}
		long end2 = System.currentTimeMillis();
		LOG.info("前置锁仓发起锁仓调用flightOrderProcessService.preLockSeat  cost:"+(end2-start2) +"毫秒");
		if(resultHandle!=null&&resultHandle.isSuccess()){
			LOG.info("preLockSeat:"+JSONObject.fromObject(map).toString());
			resultHandleT.setReturnContent(JSONObject.fromObject(map).toString());
		}
		return resultHandleT;
	}

	@Override
	public ResultHandleT<PagedTicketOrderInfo> getPagedTicketOrderInfoByMobile(String mobile, Integer pageSize,
			Integer start) {
		LOG.info("mobile[" + mobile + "], pageSize[" + pageSize + "], start[" + start + "]");
		ResultHandleT<PagedTicketOrderInfo> result = new ResultHandleT<PagedTicketOrderInfo>();
		if (mobile == null || mobile.trim().equals("")) {
			String msg = "missing mobile";
			LOG.warn(msg);
			result.setMsg(msg);
			return result;
		}
		
		try {
        	String msg = "No order is related to mobile[" + mobile + "]!";
			
	        Integer totalQuantity = getTicketOrderTotalQuantityByMobile(mobile);
	        if (totalQuantity == null || totalQuantity <= 0) {
	        	LOG.debug(msg);
	        	result.setMsg(msg);
	        	return result;
	        }
	        
			List<Long> orderIdL =  getPagedTicketOrderIdByMobile(mobile, pageSize, start, totalQuantity);
			if (orderIdL == null || orderIdL.size() <= 0) {
	        	LOG.debug(msg);
	        	result.setMsg(msg);
	        	return result;
			}
			
			List<TicketOrderInfo> ticketOrderInfoL = getTicketOrderInfoById(orderIdL);
			if (ticketOrderInfoL == null || ticketOrderInfoL.size() <= 0) {
	        	LOG.debug(msg);
				result.setMsg(msg);
				return result;
			}
			
			PagedTicketOrderInfo content = new PagedTicketOrderInfo(totalQuantity, ticketOrderInfoL);
			result.setReturnContent(content);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			result.setMsg(e);
		}
		return result;
	}
	
	private Integer getTicketOrderTotalQuantityByMobile(String mobile) {
		Integer totalQuantity2Return = new Integer(0);
		if (ordOrderDao != null) {
			Integer returnedTotalQuantity = ordOrderDao.getTicketOrderTotalQuantityByMobile(mobile);
			LOG.info("ticketOrderTotalQuantity -> " + returnedTotalQuantity);
			if (returnedTotalQuantity != null && returnedTotalQuantity > 0)
				totalQuantity2Return = returnedTotalQuantity;
		} else {
			LOG.warn("null order DAO");
		}
		return totalQuantity2Return;
	}
	
	private List<Long> getPagedTicketOrderIdByMobile(String mobile, Integer pageSize, Integer start,
			Integer totalQuantity) {
		List<Long> orderIdL = new ArrayList<Long>();
		if (ordOrderDao != null) {
			Integer currentPageSize = pageSize == null ? 10 : pageSize;
			Integer currentStartPageNo = start == null ? 1 : start;
			Page page = Page.page(totalQuantity, currentPageSize, currentStartPageNo);
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			paramsMap.put("mobile", mobile);
			paramsMap.put("_start", page.getStartRows());
			paramsMap.put("_end", page.getEndRows());
			paramsMap.put("_orderby", "oi.visit_time");
			paramsMap.put("_order", "DESC");
			List<RawTicketOrderInfo> rawTicketOrderInfoL = ordOrderDao.getPagedRawTicketOrderInfoByMobile(paramsMap);
			LOG.debug("rawTicketOrderInfoL -> " + JSON.toJSONString(rawTicketOrderInfoL));
			if (rawTicketOrderInfoL != null) {
				for (RawTicketOrderInfo raw : rawTicketOrderInfoL) {
					if (!isRefundApplied(raw.getOrderId()))
						orderIdL.add(raw.getOrderId());
					else 
						LOG.info("order[" + raw.getOrderId() + "] refund applied, remove");
				}
				LOG.info("order(s) before filtering by visit date -> " + orderIdL);
				
				for (RawTicketOrderInfo raw : rawTicketOrderInfoL) {
					if (raw.getVisitTime() != null && raw.getContent() != null) {
						try {
							@SuppressWarnings("unchecked")
							Map<String, Object> contentMap = (Map<String, Object>) JSONObject
									.toBean(JSONObject.fromObject(raw.getContent().trim()), jsonConfig);
							if (contentMap != null) {
								String aperiodicFlag = (String) contentMap.get("aperiodic_flag");
								if (aperiodicFlag.equalsIgnoreCase("Y")) {
									String aperiodicEndDateStr = (String) contentMap.get("aperiodic_end");
									String aperiodicEndDateTimeStr = aperiodicEndDateStr + " 23:59:59";
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									Date aperiodicEndDate = sdf.parse(aperiodicEndDateTimeStr);
									if (aperiodicEndDate.before(new Date())) {
										orderIdL.remove(raw.getOrderId());
										LOG.info("aperiodic, expired, remove orderId[" + raw.getOrderId() + "]");
									}
								} else if (aperiodicFlag.equalsIgnoreCase("N")) {
									Long certValidDay = Long.valueOf(contentMap.get("cert_valid_day").toString());
									Long expireDateInMillis = raw.getVisitTime().getTime() + certValidDay * 24 * 3600 * 1000;
									Long now = System.currentTimeMillis();
									LOG.info("expire time: " + expireDateInMillis + ", now: " + now);
									if (expireDateInMillis <= now) {
										orderIdL.remove(raw.getOrderId());
										LOG.info("not aperiodic, expired, remove orderId[" + raw.getOrderId() + "]");
									}
								} else {
									LOG.info("aperiodic_flag missing, remove orderId[" + raw.getOrderId() + "]");
									orderIdL.remove(raw.getOrderId());
								}
							} 
						} catch (Exception e) {
							LOG.error(e.getMessage(), e);
							LOG.error("error occurred, remove orderId[" + raw.getOrderId() + "]");
							orderIdL.remove(raw.getOrderId());
						}
					} else {
						LOG.info("visit time or valid period is null, remove orderId[" + raw.getOrderId() + "]");
						orderIdL.remove(raw.getOrderId());
					}
				}
				LOG.info("order(s) after filtering by visit date -> " + orderIdL);
			}
		} else {
			LOG.warn("null order DAO");
		}
		return orderIdL;
	}
	
	private static JsonConfig jsonConfig = new JsonConfig(); 
	static {
		PropertyFilter filter = new PropertyFilter() {
			public boolean apply(Object source, String name, Object value) {
				if (value instanceof JSONNull || value == null || "null".equals(value)) {
					return true;
				}
				return false;
			}
		};
		jsonConfig.setRootClass( HashMap.class );  
		jsonConfig.setJavaPropertyFilter(filter);
		jsonConfig.setJsonPropertyFilter(filter);
	}
	
	private List<TicketOrderInfo> getTicketOrderInfoById(List<Long> orderIdL) {
		List<TicketOrderInfo> ticketOrderInfoL = new ArrayList<TicketOrderInfo>();
		if (ordOrderDao != null) {
			ticketOrderInfoL = ordOrderDao.getTicketOrderInfoById(orderIdL);
		} else {
			LOG.warn("null order DAO");
		}
		return ticketOrderInfoL;
	}
	
	@Override
	public String resendPassport(String orderId) {
		LOG.info("resendPassport, orderId: " + orderId);
		int flag = -1;
		String result = "failed 其他错误 无法发送凭证";
		if (passportSendSmsService != null) {
			LOG.info("resendPassport, try to send passport sms");
			try {
				flag = passportSendSmsService.resendSmsForComingCall(Long.valueOf(orderId));
				if (flag == 0)
					result = "succeeded";
				if (flag == 1)
					result = "failed 订单号为空";
				if (flag == 2)
					result = "failed 该无成功申码记录，无法发送凭证";
				// flag = 3, 其他错误
			} catch (Exception e) {
				LOG.error("resendPassport Exception", e);
			}
		} else {
			LOG.info("resendPassport null passport sms service");
		}
		
		// 无申码记录的订单对接方式可能为纯EBK或传真，发送支付成功短信
		if (flag == 2) {
			LOG.info("resendPassport, try to send payed sms");
			result = "failed 其他错误 无法发送支付成功短信";
			if (iOrdOrderService != null && orderSmsSendProcesser != null) {
				try {
					OrdOrder order = iOrdOrderService.loadOrderWithItemByOrderId(Long.valueOf(orderId));
					orderSmsSendProcesser.handle(MessageFactory.newOrderPaymentMessage(Long.valueOf(orderId), ""),
							order);
					result = "succeeded";
				} catch (Exception e) {
					LOG.error("resendPassport Exception", e);
				}
			} else {
				LOG.info("resendPassport null order-service or order-sms-service");
			}
		}
		
		LOG.info("resendPassport result:" + result);
		
		return result;
	}	


	/* 
	 * 默认输入的订单号都属于门票BU的订单，故方法内部不做品类校验
	 */
	@Override
	public String applyForRefund(String orderId) {
		LOG.info("phone call cancel, orderId: " + orderId);
		
		String result = "failed";
		
		if (orderId == null || orderId.trim().equals("")) 
			return result + " 订单号为空";
		
		Long orderIdInNumberFormat = null;
		try {
			orderIdInNumberFormat = Long.valueOf(orderId);
		} catch (NumberFormatException nfe) {
			LOG.error(nfe.getMessage(), nfe);
			return result + " 订单号不是一个数字";
		}
		if (orderIdInNumberFormat == null)
			return result + " 订单号为空";
		
		if (isRefundApplied(orderIdInNumberFormat))
			return result + " 订单已有退款申请";
		
		ResultHandleT<List<OrdOrderItem>> wrappedOrderItemList = getOrderItemListByOrderId(orderIdInNumberFormat);
		if (wrappedOrderItemList == null || wrappedOrderItemList.getReturnContent() == null
				|| wrappedOrderItemList.getReturnContent().size() < 1)
			return result + " 没有子订单";
		
		// 排除仅含有退改类型为【可退该】的子订单之外的订单
		LOG.info("applyForRefund subOrderList -> " + JSON.toJSONString(wrappedOrderItemList.getReturnContent()));
		for (OrdOrderItem subOrder : wrappedOrderItemList.getReturnContent()) {
			if (subOrder == null || subOrder.getCancelStrategy() == null)
				return result + " 子订单或退改类型为空";
			if (subOrder.getCancelStrategy().equals(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.MANUALCHANGE.toString()))
				return result + " 退改类型为人工退改，语音退不予处理";
			if (subOrder.getCancelStrategy()
					.equals(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.PARTRETREATANDCHANGE.toString()))
				return result + " 退改类型为部分退，语音退不予处理";
			if (subOrder.getCancelStrategy()
					.equals(SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.toString()))
				return result + " 退改类型为不可退";
		}
		
		// 退款金额>=实付金额，为全损，等同于不可退，不予退款
		Map<String, Long> amountMap = queryOrdItemRefundAmountForTicket(wrappedOrderItemList.getReturnContent());
		if (amountMap != null && amountMap.get("actualPayAmount") != null && amountMap.get("deductAmount") != null) {
			if (amountMap.get("deductAmount").longValue()>=amountMap.get("actualPayAmount").longValue()) {
				LOG.info("applyForRefund order[" + orderId + "] deductAmount[" + amountMap.get("deductAmount") + "]");
				LOG.info("applyForRefund order[" + orderId + "] actualPayAmount[" + amountMap.get("actualPayAmount") + "]");
				LOG.info("applyForRefund order[" + orderId + "]全损, 作不可退处理");
				return result + " 全损, 作不可退处理";
			}
		} else {
			return result + " 未知实付金额或扣款金额";
		}
		
		Map<String, Boolean> orderInfoStatus = queryOrderInfoStatus(orderIdInNumberFormat);
		if (orderInfoStatus == null || orderInfoStatus.get(VstOrdOrderEnum.IS_ERROR.name()) == null
				|| orderInfoStatus.get(VstOrdOrderEnum.IS_ERROR.name())
				|| orderInfoStatus.get(VstOrdOrderEnum.IS_IN_USE.name()) == null)
			return result + " 订单使用状态不明确";
		
		// 【已使用】或【部分使用】，inUse为true
		Boolean inUse = orderInfoStatus.get(VstOrdOrderEnum.IS_IN_USE.name());
		if (inUse == null || inUse.equals(true)) 
			return result + " 订单已使用，不予处理";
		
		Map<String, Boolean> communicationPattern = querySupplierNoticeStatus(orderIdInNumberFormat);
		if (communicationPattern == null)
			return result + " 对接方式不明确，无法生成售后单";
		
		UserRefundApplyVO template = createRefundApplyTemplate(orderIdInNumberFormat, wrappedOrderItemList.getReturnContent(), communicationPattern);
		if (template == null)
			return result + " 无法生成退款申请";
		
		if (createVoiceRefund(orderIdInNumberFormat, template))
			result = "succeeded";
		else 
			result += " 取消订单或者创建退款申请失败"; 
			
		return result;
	}
	
	private boolean isRefundApplied(Long orderId) {
		boolean isRefundApplied = false;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("orderId", orderId);
		List<OrdRefundApply> ordRefundApplies = null;
		try {
			ordRefundApplies = ordRefundApplyService.queryRefundApplyByParam(paramMap);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		if (ordRefundApplies != null && ordRefundApplies.size() > 0) {
			isRefundApplied = true;
		}
		LOG.info("order[" + orderId + "] refund" + (isRefundApplied ? "" : " not") + " applied");
		return isRefundApplied;
	}	
	
	private UserRefundApplyVO createRefundApplyTemplate(Long orderId, List<OrdOrderItem> subOrderList,
			Map<String, Boolean> communicationPattern) {
		// 如遇订单为期票清单、没有最后取消时间或最后取消时间已过等情况的话，创建售后单模版
		if (isAperiodicOrder(subOrderList) || noLastCancelTimeOrLaterThanTheTime(subOrderList))
			return createAfterSaleBillTypeRefundApplyTemplate(orderId, subOrderList);

		// 对接方式为传真的话，生成售后单模版
		Boolean isInformedByFax = communicationPattern.get(VstOrdOrderEnum.IS_FAX_NOTICE.toString());
		if ((isInformedByFax != null && isInformedByFax))
			return createAfterSaleBillTypeRefundApplyTemplate(orderId, subOrderList);

		// 对接方式为EBK的话，不能及时通关的生成售后单模版，否则生成退款申请模版
		Boolean isInformedByEbk = communicationPattern.get(VstOrdOrderEnum.IS_EBK_NOTICE.toString());
		Boolean notInTime = communicationPattern.get(VstOrdOrderEnum.IS_ENTER_NOT_IN_TIME.toString());
		if (isInformedByEbk != null && isInformedByEbk) {
			if (notInTime != null && notInTime)
				return createAfterSaleBillTypeRefundApplyTemplate(orderId, subOrderList);
			else
				return createCommonTypeRefundApplyTemplate(orderId, subOrderList, UserRefundApplyEnum.EBK.toString());
		}

		// 如果对接方式不为传真、EBK的时候仍然不是API（对接服务商或供应商系统）的话，创建售后单模版
		Boolean isInformedByApi = communicationPattern.get(VstOrdOrderEnum.IS_SUPPLIER_NOTICE.toString());
		if (isInformedByApi != null && !isInformedByApi)
			return createAfterSaleBillTypeRefundApplyTemplate(orderId, subOrderList);

		// 如果对接方式是API且支持废码的话，生成退款申请模版，否则生成售后单模版
		Boolean isCodeDestroyingSupported = communicationPattern
				.get(VstOrdOrderEnum.IS_SUPPORT_DESTROY_CODE.toString());
		if (isCodeDestroyingSupported != null && isCodeDestroyingSupported) {
			return createCommonTypeRefundApplyTemplate(orderId, subOrderList, UserRefundApplyEnum.DOCKING.toString());
		} else {
			return createAfterSaleBillTypeRefundApplyTemplate(orderId, subOrderList);
		}
	}
	
	private boolean isAperiodicOrder(List<OrdOrderItem> subOrderList) {
		boolean result = false;
		for (OrdOrderItem subOrder : subOrderList) {
			if (subOrder.hasTicketAperiodic()) {
				result = true;
				break;
			}
		}
		LOG.info("isAperiodicOrder -> " + result);
		return result;
	}
	
	private boolean noLastCancelTimeOrLaterThanTheTime(List<OrdOrderItem> subOrderList) {
		boolean result = false;
		for (OrdOrderItem subOrder : subOrderList) {
			if (subOrder.getLastCancelTime() == null) {
				result = true;
				break;
			}
			if (subOrder.getLastCancelTime().before(new Date())) {
				result = true;
				break;
			}
		}
		LOG.info("noLastCancelTimeOrLaterThanTheTime -> " + result);
		return result;
	}
	
	private UserRefundApplyVO createAfterSaleBillTypeRefundApplyTemplate(Long orderId,
			List<OrdOrderItem> subOrderList) {
		LOG.info("售后单模版created");
		UserRefundApplyVO template = new UserRefundApplyVO();
		template.setOrderId(orderId);
		template.setRefundType(com.lvmama.comm.vo.Constant.REFUND_APPLY_TYPE.SALE.getCode());
		template.setOrderName(getProductName(subOrderList));
		Map<String, Long> amountMap = queryOrdItemRefundAmountForTicket(subOrderList);
		template.setActualPayAmount(amountMap.get("actualPayAmount"));
		template.setRefundAmount(amountMap.get("refundAmount"));
		return template;
	}
	
	private String getProductName(List<OrdOrderItem> subOrderList) {
		String productName = "unknown";
		for (OrdOrderItem subOrder : subOrderList) {
			if (subOrder.getMainItem() != null && subOrder.getMainItem().equalsIgnoreCase("true")) {
				return subOrder.getProductName();
			}
		}
		return productName;
	}
	
	private Map<String, Long> queryOrdItemRefundAmountForTicket(List<OrdOrderItem> subOrderList) {
		Map<String, Long> amountMap = new HashMap<String, Long>();
		for (OrdOrderItem subOrder : subOrderList) {
			if (subOrder != null && subOrder.getOrderItemId() != null && subOrder.getQuantity() != null) {
				// 退款份数=子订单所有份数
				OrdRefundmentItemSplit amountItems = queryOrdItemRefundAmountForTicket(subOrder.getOrderItemId(),
						subOrder.getQuantity());
				if (amountItems != null) {
					Long actualPayAmount = amountItems.getActualAmout();
					if (actualPayAmount == null)
						actualPayAmount = 0l;
					if (amountMap.get("actualPayAmount") != null)
						actualPayAmount += amountMap.get("actualPayAmount");
					amountMap.put("actualPayAmount", actualPayAmount);
					
					Long refundAmount = amountItems.getInitRefundedPrice();
					if (refundAmount == null)
						refundAmount = 0l;
					if (amountMap.get("refundAmount") != null)
						refundAmount += amountMap.get("refundAmount");
					amountMap.put("refundAmount", refundAmount);
					
					Long deductAmount = amountItems.getInitActualLoss();
					if (deductAmount == null)
						deductAmount = 0l;
					if (amountMap.get("deductAmount") != null)
						deductAmount += amountMap.get("deductAmount");
					amountMap.put("deductAmount", deductAmount);
				}					
			}
		}
		return amountMap;
	}
	
	private UserRefundApplyVO createCommonTypeRefundApplyTemplate(Long orderId, List<OrdOrderItem> subOrderList,
			String refundIdentity) {
		LOG.info("退款申请模版created");
		UserRefundApplyVO template = new UserRefundApplyVO();
		template.setOrderId(orderId);
		template.setRefundType(com.lvmama.comm.vo.Constant.REFUND_APPLY_TYPE.REFUND.getCode());
		// UserRefundApplyVO与UserRefundApply结果不一致，接refundExplain字段暂存一下refundIdentity
		template.setRefundExplain(refundIdentity);
		template.setOrderName(getProductName(subOrderList));
		List<UserDeductDetailVO> userDeductDetailList = new ArrayList<UserDeductDetailVO>();
		for (OrdOrderItem subOrder : subOrderList) {
			// 退款份数=子订单所有份数
			OrdRefundmentItemSplit amountItems = queryOrdItemRefundAmountForTicket(subOrder.getOrderItemId(),
					subOrder.getQuantity());
			if (amountItems != null) {
				UserDeductDetailVO detail = new UserDeductDetailVO();
				detail.setOrderId(orderId);
				detail.setItemId(subOrder.getOrderItemId());

				Long actualPayAmount = amountItems.getActualAmout();
				if (actualPayAmount == null)
					actualPayAmount = 0l;
				detail.setItemRefundActualAmount(actualPayAmount);
				Long totalActualPayAmount = template.getActualPayAmount();
				if (totalActualPayAmount == null)
					totalActualPayAmount = 0l;
				totalActualPayAmount += actualPayAmount;
				template.setActualPayAmount(totalActualPayAmount);

				Long refundAmount = amountItems.getInitRefundedPrice();
				if (refundAmount == null)
					refundAmount = 0l;
				detail.setItemRefundAmount(refundAmount);
				Long totalRefundAmount = template.getRefundAmount();
				if (totalRefundAmount == null)
					totalRefundAmount = 0l;
				totalRefundAmount += refundAmount;
				template.setRefundAmount(totalRefundAmount);

				Long toDeductAmount = amountItems.getInitActualLoss();
				if (toDeductAmount == null)
					toDeductAmount = 0l;
				detail.setItemDeductAmount(toDeductAmount);
				Long toDeductAmountInTotal = template.getDeductAmount();
				if (toDeductAmountInTotal == null)
					toDeductAmountInTotal = 0l;
				toDeductAmountInTotal += toDeductAmount;
				template.setDeductAmount(toDeductAmountInTotal);

				detail.setItemNum(subOrder.getQuantity());
				detail.setItemProductName(subOrder.getProductName());
				detail.setItemSuppGoodsName(subOrder.getSuppGoodsName());
				detail.setItemRefundQuantity(subOrder.getQuantity());
				detail.setItemRefundRuleDetail(amountItems.getActualRefundRule());
				detail.setItemDeductRuleSnapshot(amountItems.getRefundRuleSnapshot());
				detail.setItemSingleDeductAmount(
						toDeductAmount / (subOrder.getQuantity() != null ? subOrder.getQuantity() : 1l));

				userDeductDetailList.add(detail);
			}
		}
		template.setUserDeductDetailVOs(userDeductDetailList);
		return template;
	}
	
	private boolean cancelOrderSendMsgDoCreateRefundApplyThreeInOne(Long orderId, UserRefundApplyVO template) {
		// 当退款申请类型直接为“退款”的时候才取消订单，如果类型为“售后”的话不取消，仅生成售后单
		LOG.info("订单[" + orderId + "]的退款申请类型为" + template.getRefundType());
		boolean doCreateRefundApply = true;
		if (com.lvmama.comm.vo.Constant.REFUND_APPLY_TYPE.REFUND.getCode().equals(template.getRefundType())) {
			LOG.info("订单[" + orderId + "]的退款申请类型为【退款】，取消订单");
			if (doCreateRefundApply = cancelOrder(orderId)) {
				LOG.info("订单[" + orderId + "]取消" + (doCreateRefundApply ? "成功" : "失败"));
				sendMsg(orderId);
			}
		}
		if (doCreateRefundApply) {
			LOG.info("为订单[" + orderId + "]创建售后/退款单");
			if (doCreateRefundApply(orderId, template))
				return true;
		}
		return false;
	}

    /**
     * 跟在线退保持一致，先走创建申请，走子单取消流程
     * @param orderId
     * @param template
     * @return
     */
    private boolean createVoiceRefund(Long orderId, UserRefundApplyVO template) {
        // 直接生成退款申请然后走废码取消订单接口
        if (doCreateRefundApply(orderId, template)) {
            return true;
        }
        return false;
    }
	
	private boolean cancelOrder(Long orderId) {
		try {
			ResultHandle cancelResult = cancelOrder(orderId, OrderEnum.ORDER_CANCEL_CODE.PHONE_CALL_CANCEL.toString(),
					"code=" + OrderEnum.ORDER_CANCEL_CODE.PHONE_CALL_CANCEL.name() + "||reason=语音退", null, "语音退");
			if (cancelResult != null) {
				if (cancelResult.isSuccess()) {
					return true;
				} else {
					LOG.warn("订单取消失败:" + cancelResult.getMsg());
					return false;
				}
			} else {
				LOG.warn("result of cancelling order[" + orderId + "] is unknown");
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return false;
	}
	
	private void sendMsg(Long orderId) {
		try {
			if (orderMessageProducer != null)
				orderMessageProducer.sendMsg(MessageFactory.newOrderCancelMessage(orderId,
						"很遗憾您取消了订单，但是小驴仍然会为您做驴做马，任劳任怨的， sEE yOU nEXT tIME~"));
			else
				LOG.warn("message producer not ready, msg of order[" + orderId + "] can't be sent");
		} catch (Exception e) {
			LOG.warn("msg of order[" + orderId + "] can't be sent due to error below");
			LOG.error(e.getMessage(), e);
		}
	}
	
	private boolean doCreateRefundApply(Long orderId, UserRefundApplyVO template) {
		try {
			if (ordRefundApplyService != null) {
				OrdRefundApply refundApply = new OrdRefundApply();
				refundApply.setOrderId(template.getOrderId());
				refundApply.setRefundMoney(template.getRefundAmount());
				refundApply.setSystemType("VST");
				refundApply.setCreateTime(new Date());
				refundApply.setDisposeStatus("N");
				refundApply.setOperatorName("用户");
				refundApply.setRefundReason("语音退");
				refundApply.setRefundFrom("语音菜单");
				refundApply.setRefundType(template.getRefundType());
				refundApply.setActualPay(template.getActualPayAmount());
				refundApply.setRefundIdentify(template.getRefundExplain());
				if (com.lvmama.comm.vo.Constant.REFUND_APPLY_TYPE.SALE.getCode().equals(template.getRefundType())) {
					refundApply.setRefundStatus(com.lvmama.comm.vo.Constant.REFUNDMENT_STATUS.UNVERIFIED.getCode());
				} else if (com.lvmama.comm.vo.Constant.REFUND_APPLY_TYPE.REFUND.getCode()
						.equals(template.getRefundType())) {
					refundApply.setRefundStatus(com.lvmama.comm.vo.Constant.REFUNDMENT_STATUS.REFUND_APPLY.getCode());
				}
				Long refundId = ordRefundApplyService.insertRefundApply(refundApply);
				createOrUpdateOrdOrderStatus(orderId, template);
				if (refundId != null) {
                    LOG.info("语音退取消订单,orderId:" + orderId);
                    cancelOrder(refundApply,refundId);
                }
				return true;
			} else {
				LOG.warn("refund apply service not ready, refund apply of order[" + template.getOrderId()
						+ "] can't be created");
			}
		} catch (Exception e) {
			LOG.warn("refund apply of order[" + orderId + "] can't be created due to error below");
			LOG.error(e.getMessage(), e);
		}
		return false;
	}

    /**
     * 语音退走子单取消方法（先废码在取消订单）
     * @param refundApply
     * @param refundId
     */
	private void cancelOrder(OrdRefundApply refundApply,Long refundId) {

        if(refundApply.getRefundType().equals(com.lvmama.comm.vo.Constant.REFUND_APPLY_TYPE.REFUND.getCode())){
            //退款单需取消订单
            OrdOrderCancelInfoVo cancelInfoVo = new OrdOrderCancelInfoVo();
            cancelInfoVo.setOrderId(refundApply.getOrderId());
            cancelInfoVo.setCancelSerialNo(refundId.toString());
            cancelInfoVo.setCancelType(ItemCancelEnum.ORDER_CANCEL_CODE.API.getCode());
            cancelInfoVo.setIsOrderCancel(ItemCancelEnum.IS_ORDER_CANCEL.Y.getCode());
            cancelInfoVo.setReason(refundApply.getRefundReason());
            //57=其他，根据用户页面申请退款枚举类
            cancelInfoVo.setCancelCode("57");
            cancelInfoVo.setOrderMemo("用户在线语音退");
            cancelInfoVo.setOperatorName(refundApply.getOperatorName());
            ResponseBody<Integer> responseBody = apiOrderItemCancelProcessService.cancelOrderItem(new RequestBody<OrdOrderCancelInfoVo>().setTFlowStyle(cancelInfoVo));
            LOG.info("语音退取消订单返回,responseBody:"+ JSONUtil.bean2Json(responseBody) +",orderId:" + refundApply.getOrderId());
            LOG.info("语音退取消订单结束,orderId:" + refundApply.getOrderId());
        }
    }
	
	private void createOrUpdateOrdOrderStatus(Long orderId, UserRefundApplyVO template) {
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("orderId", orderId);
			paramMap.put("systemType", "VST");
			VstOrdOrderStatus status = vstOrdOrderStatusService.queryOrderStatusByParam(paramMap);

			if (status == null) {
				LOG.info("create status of order[" + orderId + "]");
				status = new VstOrdOrderStatus();
				status.setOrderId(template.getOrderId());
				status.setSystemType("VST");
				status.setPlaceName(template.getOrderName());
				status.setProductType("TICKET");
				status.setActualPay(template.getActualPayAmount());
				status.setRefundAmount(template.getRefundAmount());
				status.setRefundType(template.getRefundType());
				status.setPaymentStatus("PAYED");
				if (com.lvmama.comm.vo.Constant.REFUND_APPLY_TYPE.SALE.getCode().equals(template.getRefundType())) {
					status.setOrderStatus("CANCEL");
				} else if (com.lvmama.comm.vo.Constant.REFUND_APPLY_TYPE.REFUND.getCode()
						.equals(template.getRefundType())) {
					status.setOrderStatus("NORMAL");
				}				
				vstOrdOrderStatusService.insertOrdOrderStatus(status);
			} else {
				LOG.info("update status of order[" + orderId + "]");
				status.setRefundType(template.getRefundType());
				if (com.lvmama.comm.vo.Constant.REFUND_APPLY_TYPE.SALE.getCode().equals(template.getRefundType())) {
					status.setOrderStatus("CANCEL");
				} else if (com.lvmama.comm.vo.Constant.REFUND_APPLY_TYPE.REFUND.getCode()
						.equals(template.getRefundType())) {
					status.setOrderStatus("NORMAL");
				}				
				vstOrdOrderStatusService.updateOrdOrderStatus(status);
			}
		} catch (Exception e) {
			LOG.error("fail to create or update status of order[" + orderId + "]");
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public TicketOrderInfo getSingleTicketOrder(String orderId) {
		LOG.info("get info, orderId: " + orderId);
		TicketOrderInfo toi = null;
		if (orderId == null || orderId.trim().equals(""))
			return toi;
		
		Long orderIdL = new Long(-1);
		try {
			orderIdL = Long.valueOf(orderId);
		} catch (NumberFormatException nfe) {
			LOG.error("fail to turn orderId into LONG format");
		}
		if (orderIdL == null || orderIdL <= 0)
			return toi;
		
		if (ordOrderDao == null)
			return toi;
		
		try {
			List<TicketOrderInfo> ticketOrderInfoL = ordOrderDao.getSingleTicketOrder(orderId);
			if (ticketOrderInfoL != null && ticketOrderInfoL.size() > 0) {
				// 取第一游玩人
				toi = ticketOrderInfoL.get(0);
				LOG.info("get toi -> " + JSON.toJSONString(toi));

			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
		return toi;
	}


	@Override
	@ReadOnlyDataSource
	public List<Long> queryOrderIdsByParams(Map<String, Object> params) {
		return ordOrderDao.queryOrderIdsByParams(params);
	}

	@Override
	public ResultHandleT<Object> saveOrderPerson(BuyInfo buyInfo,
			String loginUserId) {
		ResultHandleT<Object> resultHandleT=new ResultHandleT<>();
		//保存游玩人信息
		List<Person> allPerson = buyInfo.getTravellers();
		//如果姓名是'待填写'，就不传递了
		List<Person> validPersonList = new ArrayList<Person>();
		if (allPerson != null) {
			for (Person p : allPerson) {
				if (StringUtils.isNotEmpty(p.getFullName()) && !"待填写".equals(p.getFullName())) {
					validPersonList.add(p);
				}
			}
		}
		if (buyInfo.getContact() != null) {
			validPersonList.add(buyInfo.getContact());
		}
		if (buyInfo.getEmergencyPerson() != null) {
			validPersonList.add(buyInfo.getEmergencyPerson());
		}
		//savePerson(allPerson, orderHandle.getReturnContent().getUserId());
		this.createContact(validPersonList, loginUserId);
		return resultHandleT;
	}

	@Override
	public ResultHandleT<Object> queryLockSeatResult(String lockSetOrderId) {
		Map<String, String> map=new HashMap<String, String>();
		JSONObject json = JSONObject.fromObject(lockSetOrderId);
    	map=(Map<String, String>) JSONObject.toBean(json,Map.class);
    	Long orderId = Long.valueOf(map.get("0"));
		LOG.info("queryLockSeatResult begin ：主订单ID " + orderId );
		boolean lockSeatResult = true;
		BaseResponseVSTDto<FlightOrderForm> flightOrders = null;
		long start = System.currentTimeMillis();
		try {
			flightOrders = pcOrderService.getFlightOrders(orderId, null);
			LOG.info("前置锁舱查询锁仓参数：----" + JSONArray.fromObject(flightOrders).toString());
		} catch (Exception e) {
			LOG.info("{查询锁仓结果失败}", e);
			return null;
		}
		long end = System.currentTimeMillis();
		int apiFlightItem=0;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if(!"0".equals(entry.getKey())){
				apiFlightItem+=1;
			}
		}
		int count=0;
		if(flightOrders!=null&&flightOrders.getResults()!=null&&flightOrders.getResults().size()>0){
			for (FlightOrderForm flightOrderForm : flightOrders.getResults()) {
				if(!flightOrderForm.getCanPay()){
					lockSeatResult=false;
				}else{
					count+=1;
					//锁仓成功后需要保存OrdFlightTicketStatus 锁仓成功状态
					OrdFlightTicketStatus ordFlightTicketStatus = new OrdFlightTicketStatus();
					ordFlightTicketStatus.setOrderItemId(flightOrderForm.getVstOrderId());
					ordFlightTicketStatus.setStatusCode(OrderEnum.ORD_FLIGHT_TICKET_STATUS.LOCK_SUCCESS.name());
					//保存机票状态
					ordFlightTicketStatusService.saveFlightTicketStatus(ordFlightTicketStatus);
				}
			}
		}else{
			lockSeatResult=false;
		}
		String logContent = "查询锁仓结果：主订单ID[" + orderId + "]，, cost:" + (end-start) +"毫秒"+",lockSeatResult:"+lockSeatResult;
		LOG.info(logContent);
		if(lockSeatResult&&count==apiFlightItem){
			LOG.info("锁仓成功：主订单ID[" + orderId + "]");
			return new ResultHandleT<>();
		}
		return null;
	}

	/**
	 * 仅用于对来自O2O门店后台的订单支付成功后，发消息通知CRM系统。不需要保证消息的稳定性
	 * 
	 * @param order
	 */
	private void sendNotifyCrmMsg(OrdOrder order) {
		try {
			if(order.getDistributorId() == 2L && StringUtils.isNotEmpty(order.getDistributorCode())) {
				String addition = new StringBuilder().append(String.valueOf(order.getUserNo())).append("|").append(order.getDistributorCode()).toString();
				notifyCrmForO2OrderMessageProducer.sendMsg(MessageFactory.newOrderPaymentMessage(order.getOrderId(), addition));
				LOG.debug("send message of o2o order to crm success. orderId:" + order.getOrderId());
			}
		} catch (Exception e) {
			LOG.error("send message of o2o order to crm failure", e);
		}
	}

    /**
     * @Description 用户取消发票申请 
     * @param DoUserCancelApplyInvoiceRequestVo
     * @return
     */
    public ResultHandle doUserCancelApplyInvoice(DoUserCancelApplyInvoiceRequest doUserCancelApplyInvoiceRequest){
        ResultHandle resultHandle = new ResultHandle();
        if (null == doUserCancelApplyInvoiceRequest || null == doUserCancelApplyInvoiceRequest.getId()) {
            LOG.info("OrdOrderClientServiceImpl doUserCancelApplyInvoice input request is null");
            resultHandle.setMsg("入参为空");
            return resultHandle;
        }
        //发票申请Id
        Long id = doUserCancelApplyInvoiceRequest.getId();
        
        try {
            ordApplyInvoiceInfoService.revokeApplyInvoice(id);
            LOG.info("OrdOrderClientServiceImpl doUserCancelApplyInvoice success id=" + id);
        } catch (Exception e) {
            LOG.info(e.getMessage());
            resultHandle.setMsg(e.getMessage());
        }
        return resultHandle;
    }

    /**
     * @Description 用户更新发票申请全部信息（发票、地址，关联订单），更新发票申请信息(状态除外) 
     * @author Wangsizhi
     * @date 2017-12-4 下午7:39:27
     */
    public ResultHandle doUserUpdateApplyInvoice(OrdApplyInvoicePersonAddress ordApplyInvoicePersonAddress){
        ResultHandle resultHandle = new ResultHandle();
        if (null == ordApplyInvoicePersonAddress) {
            LOG.info("OrdOrderClientServiceImpl doUserUpdateApplyInvoice input request is null");
            resultHandle.setMsg("入参为空");
            return resultHandle;
        }
        try {
            ordApplyInvoiceInfoService.updateApplyInvoiceInfo(ordApplyInvoicePersonAddress);
            LOG.info("OrdOrderClientServiceImpl doUserUpdateApplyInvoice success orderId=" + ordApplyInvoicePersonAddress.getOrderId());
        } catch (Exception e) {
            LOG.info(e.getMessage());
            resultHandle.setMsg(e.getMessage());
        }
        return resultHandle;
    }

    /**
     * @Description: 根据订单Id查询  发票申请全部信息（发票、地址，关联订单）
     */
    public ResultHandleT<ArrayList<OrdApplyInvoicePersonAddress>> findAppInvFullInfoByOrderId(Long orderId){
        ResultHandleT<ArrayList<OrdApplyInvoicePersonAddress>> resultHandleT = new ResultHandleT<ArrayList<OrdApplyInvoicePersonAddress>>();
        if (null == orderId) {
            LOG.info("OrdOrderClientServiceImpl findAppInvFullInfoByOrderId input request is null");
            resultHandleT.setMsg("入参为空");
            return resultHandleT;
        }
        try {
            List<OrdApplyInvoicePersonAddress> oapaList = ordApplyInvoiceInfoService.findAppInvFullInfoByOrderId(orderId);
            resultHandleT.setReturnContent((ArrayList<OrdApplyInvoicePersonAddress>) oapaList);
        } catch (Exception e) {
            LOG.info(e.getMessage());
            resultHandleT.setMsg(e.getMessage());
        }
        return resultHandleT;
    }

    /**
     * 根据用户Id查询  发票申请全部信息（发票、地址，关联订单） 列表
     * 只返回状态为： PENDING("待申请"), CANCEL("订单取消"),(随订单取消发票申请), REVOKE("发票取消"),(用户取消发票申请)的发票申请记录
     * @param 用户Id
     * @return
     */
    @Override
    public Page<OrdApplyInvoicePersonAddress> findSpecAppInvFullInfoListByUserId(
            Page<OrdApplyInvoicePersonAddress> page, String userId){
        long totalCount = ordApplyInvoiceInfoService.findSpecAppInvFullInfoListByUserIdCount(userId);
        if (totalCount > 0l) {
            page.setTotalResultSize(totalCount);
            long startRows = page.getStartRows();
            long endRows = page.getEndRows();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("userId", userId);
            map.put("_start", startRows);
            map.put("_end", endRows);
         
            LOG.info("OrdOrderClientServiceImpl findSpecAppInvFullInfoListByUserId input userId=" + userId + "---map=" + JSONObject.fromObject(map).toString());
            
            List<OrdApplyInvoicePersonAddress> oapaList = ordApplyInvoiceInfoService.findSpecAppInvFullInfoListByUserId(map);
            if (null != oapaList && oapaList.size() >0) {
                page.setItems(oapaList);
            }else {
                LOG.info("OrdOrderClientServiceImpl findSpecAppInvFullInfoListByUserId result is null userId=" + userId);
            }
        }else {
            LOG.info("OrdOrderClientServiceImpl findSpecAppInvFullInfoListByUserId totalCount is null userId=" + userId);
        }
        return page;
    }
    
    /**
     * 根据用户Id、订单Id查询  发票申请全部信息（发票、地址，关联订单） 列表
     * 只返回状态为： PENDING("待申请"), CANCEL("订单取消"),(随订单取消发票申请), REVOKE("发票取消"),(用户取消发票申请)的发票申请记录
     * @param 用户Id 或  订单Id
     * @return
     */
    @Override
    public Page<OrdApplyInvoicePersonAddress> listAppInvFullInfoByCondition(Page<OrdApplyInvoicePersonAddress> page, OrdAppInvInfoQueryVo oaiiqVo){
        Long orderId = oaiiqVo.getOrderId();
        String userId = oaiiqVo.getUserId();
        
        LOG.info("OrdOrderClientServiceImpl listAppInvFullInfoByCondition orderId=" + orderId + "---userId=" + userId);
        
        long totalCount = ordApplyInvoiceInfoService.listAppInvFullInfoByConditionCount(oaiiqVo);
        if (totalCount > 0l) {
            page.setTotalResultSize(totalCount);
            long startRows = page.getStartRows();
            long endRows = page.getEndRows();
            Map<String, Object> map = new HashMap<String, Object>();
            if (null != orderId) {
                map.put("orderId", orderId);
            }
            if (StringUtils.isNotEmpty(userId)) {
                map.put("userId", userId);
            }
            
            map.put("_start", startRows);
            map.put("_end", endRows);
         
            List<OrdApplyInvoicePersonAddress> oapaList = ordApplyInvoiceInfoService.listAppInvFullInfoByCondition(map);
            if (null != oapaList && oapaList.size() >0) {
                page.setItems(oapaList);
            }else {
                LOG.info("OrdOrderClientServiceImpl listAppInvFullInfoByCondition result is null userId=" + userId);
            }
        }else {
            LOG.info("OrdOrderClientServiceImpl listAppInvFullInfoByCondition totalCount is null userId=" + userId);
        }
        return page;
    }
    
    /**
     * @Description: 根据发票Id查询  发票申请全部信息（发票、地址，关联订单）
     */
    @Override
    public ResultHandleT<OrdApplyInvoicePersonAddress> findAppInvFullInfoById(Long id){
        ResultHandleT<OrdApplyInvoicePersonAddress> resultHandleT = new ResultHandleT<OrdApplyInvoicePersonAddress>();
        if (null == id) {
            LOG.info("OrdOrderClientServiceImpl findAppInvFullInfoById input request is null");
            resultHandleT.setMsg("入参为空");
            return resultHandleT;
        }
        try {
            OrdApplyInvoicePersonAddress oapa = ordApplyInvoiceInfoService.findAppInvFullInfoById(id);
            resultHandleT.setReturnContent(oapa);
        } catch (Exception e) {
            LOG.info(e.getMessage());
            resultHandleT.setMsg(e.getMessage());
        }
        return resultHandleT;
    }

    /**
     * 根据 订单id查询 是否可申请发票 可以申请发票，则返回：true; 不可以申请发票, 则返回：false
     * 如果订单存在一条 状态为【PENDING("待申请")、* APPLIED("已申请")、 MANUAL("人工申请"),】,则不可以申请发票, 其余为可以申请发票。
     * @param orderId 订单orderId
     * @return
     */
    @Override
    public ResultHandleT<Boolean> checkIfApplyInvoiceByOrderId(Long orderId){
        ResultHandleT<Boolean> resultHandleT = new ResultHandleT<Boolean>();
        if (null == orderId) {
            LOG.info("OrdOrderClientServiceImpl checkIfApplyInvoiceByOrderId input request is null");
            resultHandleT.setMsg("入参为空");
            return resultHandleT;
        }
        try {
            Boolean result = ordApplyInvoiceInfoService.checkIfApplyInvoiceByOrderId(orderId);
            resultHandleT.setReturnContent(result);
        } catch (Exception e) {
            LOG.info(e.getMessage());
            resultHandleT.setMsg(e.getMessage());
        }
        return resultHandleT;
    }
    
    /**
     * 返回 门票部分品类是否可以申请发票信息： 景点门票（品类ID:11），组合套餐票（自主打包,品类ID:13，package_type:LVMAMA）
     * 老数据订单ord_order表字段need_invoice值之前业务含义为是否可以开票不针对商品或者产品，而是针对是否已经开过发票，
     * 历史订单默认可开票,历史订单的值包含true,false,part,null, 需要转换为true代表可开票
     * 新订单值应该是Y或N，Y需要转换为true,N需要转换为false
     * @param orderId 订单orderId
     * @return
     */
    @Override
    public ResultHandleT<OrderNeedInvoiceInfo> findOrderNeedInvoiceInfoByOrderId(Long orderId){
        ResultHandleT<OrderNeedInvoiceInfo> resultHandleT = new ResultHandleT<OrderNeedInvoiceInfo>();
        if (null == orderId) {
            LOG.info("OrdOrderClientServiceImpl findOrderNeedInvoiceInfoByOrderId input request is null");
            resultHandleT.setMsg("入参为空");
            return resultHandleT;
        }
        try {
            OrdOrder ordOrder = complexQueryService.queryOrderByOrderId(orderId);
            if (null == ordOrder) {
                String msg = "查询订单为空orderId=" + orderId;
                LOG.info(msg);
                resultHandleT.setMsg(msg);
                return resultHandleT;
            }
            
            OrderNeedInvoiceInfo oni = new OrderNeedInvoiceInfo();
            oni.setCreateTime(ordOrder.getCreateTime());
            oni.setOrderId(orderId);
            oni.setVisitTime(ordOrder.getVisitTime());
            String ordNeedInvoice = ordOrder.getNeedInvoice();
            
            boolean needInvoice = true;//默认
            if (StringUtils.isNotBlank(ordNeedInvoice) && "N".equalsIgnoreCase(ordNeedInvoice)) {//N需要转换为false
                needInvoice = false;
            }
            oni.setNeedInvoice(needInvoice);
            
            HashMap<Long, Boolean> orderItemNeedInvoices = new HashMap<Long, Boolean>();
            
            List<OrdOrderItem> orderItemList = ordOrder.getOrderItemList();
            for (OrdOrderItem ordOrderItem : orderItemList) {
                String needInvoiceValue = ordOrderItem.getContentStringByKey("needInvoice");
                Long orderItemId = ordOrderItem.getOrderItemId();
                if (StringUtils.isBlank(needInvoiceValue)) {
                    LOG.info("OrdOrderClientServiceImpl findOrderNeedInvoiceInfoByOrderId 子单快照中无needInvoice---OrderItemId=" + orderItemId);
                    orderItemNeedInvoices.put(orderItemId, new Boolean(true));
                }else {
                    if ("N".equalsIgnoreCase(needInvoiceValue)) {
                            orderItemNeedInvoices.put(orderItemId, new Boolean(false));
                        }else if ("Y".equalsIgnoreCase(needInvoiceValue)) {
                            orderItemNeedInvoices.put(orderItemId, new Boolean(true));
                    }
                }
            }
            oni.setOrderItemNeedInvoices(orderItemNeedInvoices);
            
            resultHandleT.setReturnContent(oni);
        } catch (Exception e) {
            LOG.info(e.getMessage());
            resultHandleT.setMsg(e.getMessage());
        }
        return resultHandleT;
    }
	
	@Override
	public void sendExpiredOrderItemRefundedMsgForEbk(Long orderItemId) {
		orderMessageProducer.sendMsg(MessageFactory.newExpiredOrderItemRefundedMsgForEbk(orderItemId));
	}

	@Override
	public ResultMessage checkFlightTicket(BuyInfo buyInfo, String flightTicketPrice) {
		return bookService.checkFlightTicket(buyInfo,flightTicketPrice);
	}

	/**
	 * 根据订单号查询定金可退金额
	 *
	 * @param orderId
	 * @return 可退金额，单位：分
	 */
	@Override
	public Long findRetreatAmountByOrderId(Long orderId) {
		try {
			return ordDepositRefundAuditService.findRetreatAmountByOrderId(orderId);
		} catch (Exception e) {
			LOG.error("findRetreatAmountByOrderId  " + orderId , e);
		}
		return null;
	}

	/**
	 * 更新定金退款标识(已审核通过的记录)
	 *
	 * @param orderId
	 */
	@Override
	public void updateRefundFlag(Long orderId) {
		try {
			ordDepositRefundAuditService.updateRefundFlag(orderId);
		}catch (Exception e){
			LOG.error("update refund flag failed , orderId :[%s]" , orderId);
		}
	}


	/**
	 * 该方法在分销每天导出淘宝退款订单列表，循环调用该方法。
	 * 全额退：1、修改子订单结算价为0，2、修改主订单退款金额
	 * 部分退：1、修改主订单退款金额，当天重复提交以第一笔为准。已退款金额=原已退款金额+退款金额；当天如果多笔，第二笔几以后不做处理。
	 * @param orderId 订单id
	 * @param refundAmount 退款金额，以分为单位，比喻退款1.11元，refundAmount=111
	 * @param refundType 退款类型：full:全额退，part:部分退
	 * @return
	 */
	@Override
	public boolean refundOrderByTaobao(String orderIds, Long refundAmount,
			String refundType) {
		//检查请求参数
		if(StringUtils.isEmpty(orderIds)||StringUtils.isEmpty(refundType)||refundAmount==null||refundAmount<=0){
			LOG.error("请求参数错误，orderIds："+orderIds +" refundAmount:"+refundAmount+"  refundType:"+refundType);
			return false;
		}
		refundType=refundType.toLowerCase();
		if((!"full".equals(refundType))&&(!"part".equals(refundType))){
			LOG.error("请求参数错误，refundType只允许[full,part],当前参数是："+refundType);
			return false;
		}
		
		 Pattern pattern = Pattern.compile("[\\d,]+");
		 Matcher matcher = pattern.matcher(orderIds);
		 if(!matcher.matches()){
			 LOG.error("请求参数错误，orderIds参数错误,当前orderIds是："+orderIds);
			return false;
		 }
	LOG.info("淘宝请求退款，refundOrderByTaobao，orderIds："+orderIds+"refundAmount"+refundAmount+"分，refundType"+refundType);
	boolean result=true;
	try{
	
		//修改退款金额和结算价
		if("full".equals(refundType)){
			updateTotalSettlementByTaobaoRefund(orderIds, refundAmount);
		}else if("part".equals(refundType)){
			//部分退修改主订单退款金额(多个订单的情况极少，所以放for循环里面)
			for(String strOrderId:orderIds.split(",")){
				if(StringUtils.isEmpty(strOrderId)){
					continue;
				}
				//订单上次退款时间
				//（配置把reader节点与writer配置反了,而且别的地方已经反着用了，已请示反着用算了。）
				//String  str_activeRefundedTime= JedisTemplate2.getReaderInstance().get(RedisEnum.KEY.ORD_ORDER_REFUNDED_DATE.name()+"_"+strOrderId);
				JedisTemplate2 redisReader=JedisTemplate2.getWriterInstance();
				if(redisReader==null)
					LOG.error("redis的redisReader异常，请与运维联系，redisReader===null");
				String redisKey=RedisEnum.KEY.ORD_ORDER_REFUNDED_DATE.name()+"_"+strOrderId;
				String  str_activeRefundedTime= redisReader.get(redisKey);
				if(StringUtils.isNotBlank(str_activeRefundedTime)){
					LOG.info("taobao部分退款， redis ORD_ORDER_REFUNDED_DATE value is not null,key:"+redisKey+" value:"+str_activeRefundedTime);
				}
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String str_now=dateFormat.format(new Date());
				//部分退一天只允许退款一次，多次退款，第二次及以后不做处理
				if(StringUtils.isNotBlank(str_activeRefundedTime)&&str_now.equals(str_activeRefundedTime)){
					LOG.info("taobao部分退款，strOrderId:"+strOrderId+" 当天已发生过退款，str_activeRefundedTime： "+str_activeRefundedTime);
					//result=false;
					continue;
				}
				
				try{
					//先写redis尽量防止某个orderId并发问题
					if(!saveOrderId2Redis(strOrderId, str_now)){
						continue;
					}
					Long orderId=Long.parseLong(strOrderId);
					OrdOrder order=new OrdOrder();
					order.setOrderId(orderId);
					//部分退退款金额是以前退款金额+本次退款金额
					OrdOrder activeOrder=iOrdOrderService.findByOrderId(orderId);
					if(activeOrder==null){
						LOG.error("taobao部分退款,订单ID错误，系统没有相关的订单信息，orderId:"+orderId);
						result=false;
						continue ;
					}
					updatePriceConfirmStatusAndRefundedAmountForTaobaoRefund(refundAmount, strOrderId, str_now, orderId,activeOrder,order);
				}catch(Exception e){
					JedisTemplate2 redisWriter=JedisTemplate2.getReaderInstance();
					if(redisWriter!=null&&redisWriter.exists(redisKey)){
						redisWriter.del(redisKey);	
					}
					result=false;
					continue ;
				}
				
			}
		}
		
		}catch(Exception e){
			LOG.error("淘宝退款发生未知异常 com.lvmama.vst.order.client.ord.service.impl.OrdOrderClientServiceImpl.refundOrderByTaobao"+e.getMessage(),e);
			return false;
		}
		return result;
	}

    @Override
    public ResultHandle updateOrdTravellers(Long orderId, OrdOrderPersonVO ordOrderPersonVO) {
        ResultHandle handle = new ResultHandle();
        OrdOrder order = queryOrdorderByOrderId(orderId);
        if(order==null){
            handle.setMsg("订单不存在");
            return handle;
        }
        
        ResultHandleT<String> result = orderUpdateService.updateOrdTravellers(order,ordOrderPersonVO);
        if(result.isSuccess()&&!result.hasNull()){
            orderMessageProducer.sendMsg(MessageFactory.newOrderModifyPersonMessage(orderId, result.getReturnContent()));
        }
        return handle;
    }

    /**
	 * 修改订单退款金额并修改子订单价格确认状态
	 * @param refundAmount
	 * @param strOrderId
	 * @param str_now
	 * @param orderId
	 * @param activeOrder
	 * @param order
	 */
	private void updatePriceConfirmStatusAndRefundedAmountForTaobaoRefund(Long refundAmount,
			String strOrderId, String str_now, Long orderId,OrdOrder activeOrder,OrdOrder order) {
		Long _refundAmount=0L;
		if(activeOrder.getRefundedAmount() ==null){
			_refundAmount=refundAmount;
		}else{
			 _refundAmount=refundAmount+activeOrder.getRefundedAmount();
		}
		order.setRefundedAmount(_refundAmount);
		ordOrderDao.updateOrder(order);
		LOG.info("taobao部分退款，strOrderId:"+strOrderId+" 修改退款金额为(refundAmount)： "+_refundAmount/100+"元");
		//将将所有子订单状态改为未确认，并发送消息
		ordOrderItemService.updatePriceConfirmStatusByOrderId(orderId,ORDITEM_PRICE_CONFIRM_STATUS.UN_CONFIRMED.getCode());
		
		//发送日志消息
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,orderId,orderId,"SYSTEM",
				"淘宝发生部分退款，退款金额为："+(double)refundAmount/100+"元",
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_PART_CANCEL.name(),
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_PART_CANCEL.getCnName(),"淘宝发生部分退款");
		LOG.info("taobao部分退款，orderId:"+orderId+"已发送订单退款日志，ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_PART_CANCEL");
	}

	private boolean saveOrderId2Redis(String strOrderId, String str_now) {
		String redisKey=RedisEnum.KEY.ORD_ORDER_REFUNDED_DATE.name()+"_"+strOrderId;
		//（配置把reader节点与writer配置反了,而且别的地方已经反着用了，已请示反着用算了。）
		//JedisTemplate2.getWriterInstance().set(redisKey,str_now, RedisEnum.KEY.ORD_ORDER_REFUNDED_DATE.getSeconds());
		JedisTemplate2 redisWriter=JedisTemplate2.getReaderInstance();
		JedisTemplate2 redisReader=JedisTemplate2.getWriterInstance();

		if(redisReader.exists(redisKey)){
			String redis_exTime=redisReader.get(redisKey);//上次执行日期
			//如果存储的时间不是当天时间
			if(StringUtils.isNotBlank(redis_exTime)&&(!StringUtils.equals(redis_exTime,str_now))){
				redisWriter.del(redisKey);
			}
		}
		
		Boolean  result=redisWriter.setnx(redisKey, str_now);
		//成功则设置有效期
		if(result){
			Boolean setExpirResult=redisWriter.expire(redisKey, RedisEnum.KEY.ORD_ORDER_REFUNDED_DATE.getSeconds());
			LOG.info("taobao部分退款，strOrderId:"+strOrderId+"redis存储订单退款时间,key: "+redisKey+" value:"+redisReader.get(redisKey)+" setExpirResult:"
			+setExpirResult+" Seconds:"+RedisEnum.KEY.ORD_ORDER_REFUNDED_DATE.getSeconds());
		}else{
			LOG.info("redis存储失败，请与运维联系，strOrderId:"+strOrderId+"redis存储订单退款时间,key: "+redisKey+" value:"+str_now);
		}
		return result;
	}
	//淘宝全额退款，1、修改订单退款金额 ；2、修改子订单结算价格为0
	private void updateTotalSettlementByTaobaoRefund(String orderIds,
			Long refundAmount) {
		//1、修改退款金额(根据业务逻辑：orderIds多数情况只有一个orderId,很少情况会有多个，所有放for循环里面)
		for(String strOrderId:orderIds.split(",")){
			if(StringUtils.isBlank(strOrderId)){
				continue;
			}
			Long orderId=Long.parseLong(strOrderId);
			OrdOrder order=new OrdOrder();
			order.setOrderId(orderId);
			order.setRefundedAmount(refundAmount);
			ordOrderDao.updateOrder(order);
			LOG.info("taobao全额退款，orderId:"+orderId+"退款金额："+(double)refundAmount/100+"元,ord_order表RefundedAmount字段已更新");
			//发送订单结算价变动消息
			sendOrdSettlementPriceChangeMsgByOrderId(orderId);
			LOG.info("taobao全额退款，orderId:"+orderId+"已发送订单结算价变更消息  sendOrdSettlementPriceChangeMsg");
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,orderId,orderId,"SYSTEM",
					"淘宝全额退款，修改结算价为：0，退款金额为："+(double)refundAmount/100+"元",
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.name(),
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.getCnName(),"淘宝全额退款");
			LOG.info("taobao全额退款，orderId:"+orderId+"已发送订单退款日志，ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL");
		}
		//2、修改子订单的结算价为0
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("orderIds", orderIds);
		ordOrderItemDao.updateTotalSettlementPriceByOrderId(map);
		LOG.info("taobao全额退款，orderIds:"+orderIds+"更改所以订单的子订单的计算价为 ：0 ");
	}
	
	/**
	 * 根据订单ID,
	 * @param orderId
	 */
	private void sendOrdSettlementPriceChangeMsgByOrderId(Long orderId){
		List<OrdOrderItem> list=ordOrderItemDao.selectByOrderId(orderId);
		for(OrdOrderItem item:list){
			Long orderItemId=item.getOrderItemId();
			this.sendOrdSettlementPriceChangeMsg(orderItemId,orderItemId+"|");
		}
	}
	
	@Override
	public List<OrdOrder> selectHotelOrderList() {
		LOG.info("OrdOrderClientServiceImpl selectHotelOrderList start");
		long startTime = System.currentTimeMillis();
		List<OrdOrder> ordOrderList = ordOrderDao.selectHotelOrderList();
		long endTime = System.currentTimeMillis(); 
		LOG.info("OrdOrderClientServiceImpl selectHotelOrderList running times = " + (endTime - startTime)/1000 + "s" );
		return ordOrderList;
	}

	@Override
	public ResultHandle updateOrdInsurers(Long orderId, OrdOrderPersonVO ordOrderPersonVO) {
		ResultHandle handle = new ResultHandle();
		OrdOrder order = queryOrdorderByOrderId(orderId);
		if (order == null) {
			handle.setMsg("订单不存在");
			return handle;
		}

		ResultHandleT<String> result = orderUpdateService.updateOrdInsurers(order,ordOrderPersonVO);
		LOG.info("updateOrdInsurers，orderId:" + orderId + "更改投保人后返回的信息 ：result= " + GsonUtils.toJson(result.getReturnContent()));
		if (result.isSuccess() && !result.hasNull()) {
			order.setOrdInsurerList(ordOrderPersonVO.getInsurers());
			order.setAddition(result.getReturnContent());
			// 判断该订单是否生成保单
			Map<String, Object> query = new HashMap<String, Object>();
			query.put("orderId", orderId);
			List<InsPolicy> insPolicyList = insPolicyClientService.queryInsPolicy(query);
			LOG.info("insPolicyList，orderId:" + orderId + "insPolicyList= " + GsonUtils.toJson(insPolicyList));
			if (null != insPolicyList && insPolicyList.size() > 0) {
				// 说明有保单，调用修改保单的接口
				//走新订单系统，调用询单
				if(order2RouteService.isOrderRouteToNewSys(order.getOrderId())){
					RequestBody<PersonUpdateVo> requestBody=new RequestBody<PersonUpdateVo>();
					PersonUpdateVo param=new PersonUpdateVo();
					param.setOrderId(order.getOrderId());
					param.setAddition(order.getAddition());
					List<OrdPersonVo> ordInsurerList=new ArrayList<OrdPersonVo>();
					for(OrdPerson person:ordOrderPersonVO.getInsurers()){
						OrdPersonVo personVo=new OrdPersonVo();
						EnhanceBeanUtils.copyProperties(person,personVo);
						ordInsurerList.add(personVo);
					}
					param.setOrdInsurerList(ordInsurerList);
					requestBody.setT(param);
					apiSupplierInquiryService.updatePerson(requestBody);
				}else{
					supplierOrderService.updateOrder(order);
				}

			}
		}
		return handle;
	}

	@Override
	public ResultHandle saveNewOrderPerson(Long orderId, BuyInfo buyInfo, String operatorId) {
		//身份证输入规范中的字母为大写，所以在此统一转为大写
		if(buyInfo != null){
			List<Person> travellers = buyInfo.getTravellers();
			if(travellers != null && travellers.size() > 0){
				for(Person person : travellers){
					if(person != null && !StringUtil.isEmptyString(person.getIdNo())){
						person.setIdNo(person.getIdNo().toUpperCase());
					}
				}
			}
		}
		ResultHandle handle = new ResultHandle();
		// 判断购买人和紧急联系人包含游玩人名称中是否包含“测试下单”关键字，包含测试关键字时，将该订单标识为测试订单，标识设置成Y。默认是N
		//获取测试订单
	    //得到定义的订单测试的常量
		char isTest='N';
		try {
			isTest = buyInfo.getIsTestOrder();
			} catch (NullPointerException e) {

			}
		if(isTest!='Y'){

			String orderValue=Constant.getInstance().getOrderValue();
			String  contactName="";
		    String  emergencyPersonName="";
		    try {
					contactName = buyInfo.getContact().getFullName();
			    } catch (NullPointerException e) {
					}
			try {
					emergencyPersonName = buyInfo.getEmergencyPerson().getFullName();
			} catch (NullPointerException e) {

			}
		     // 如果购买人和联系人都不包含测试下单，那么就在游玩人中查找是否包含。直接包含直接设置成测试订单的标记。
			if ((StringUtil.isNotEmptyString(contactName)&&contactName.contains(orderValue)) || (StringUtil.isNotEmptyString(emergencyPersonName)&&emergencyPersonName.contains(orderValue))) {
				buyInfo.setIsTestOrder('Y');
				} else
					try {
							{
								List<Person> travellers = buyInfo.getTravellers();
							    for (Person person : travellers) {
							    if (person.getFullName().contains(orderValue)) {
								buyInfo.setIsTestOrder('Y');
								break;
								}
							    }
							}
						} catch (NullPointerException e) {
						}
				    }
		    try{
			if('Y' == buyInfo.getIsTestOrder()) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("isTestOrder", buyInfo.getIsTestOrder());
				parameters.put("orderId", orderId);
				int c = orderUpdateService.updateIsTestOrder(parameters);
				if(c != 1) {
					String msg = "saveNewOrderPerson.update order:" + orderId + "  to test order failed, updated rows " + c;
					LOG.error(msg);
					handle.setMsg(msg);
					return handle;
				}
			}

			handle = bookService.saveNewOrderPerson(orderId,buyInfo);
			OrdOrder order = queryOrdorderByOrderId(orderId);
//			boolean isLockSeatSuccess = lockSeatForFlightOrderMult(order);

			String specialTicket = getSpecialTicket(order);
			if(order.getDistributorId() == Constant.DIST_BACK_END && StringUtils.isNotEmpty(specialTicket)) {
                if(!checkLimit(buyInfo)){
                    //				Log.info(ComLogUtil.printTraceInfo(methodName,"上海迪斯尼锁库存失败", "this.reservationOrder", System.currentTimeMillis() - startTime));
                    cancelAfterLockStockFail(order);
                    handle = new ResultHandleT<OrdOrder>();
                    handle.setMsg("由于当前预订人数较多，请您稍后再试！！");
                    return handle;
                }
                Object[] array = reservationSupplierOrder(order);
                boolean isSuccess = (Boolean) array[0];
                if(!isSuccess){
    //				Log.info(ComLogUtil.printTraceInfo(methodName,"上海迪斯尼锁库存失败", "this.reservationOrder", System.currentTimeMillis() - startTime));
                    cancelAfterLockStockFail(order);
                    handle = new ResultHandleT<OrdOrder>();
                    //handle.setMsg("抱歉，上海迪士尼占用库存失败！");
                    handle.setMsg("抱歉，"+specialTicket+"占用库存失败！");
                    return handle;
                }
			}
			
			//门票邮寄订单       将需要的信息存到表中    以便ebk查询邮寄订单
			if(order.getDistributorId() == Constant.DIST_BACK_END && order.getAddressPerson() != null){
				for (OrdOrderItem ordOrderItem : order.getOrderItemList()){
					//后台下单  实体票     门票/WIFI      寄件方是供应商
					if(ordOrderItem.hasExpresstypeDisplay()
							&&(OrderUtils.isTicketByCategoryId(ordOrderItem.getCategoryId()) || ordOrderItem.getCategoryId() == 28L)
							&&SuppGoods.EXPRESSTYPE.SUPPLIER.name().equals(ordOrderItem.getContentStringByKey(OrderEnum.ORDER_TICKET_TYPE.express_type.name()))){
						OrdTicketPost post = new OrdTicketPost();
						BeanUtils.copyProperties(ordOrderItem, post);

						//取票人   先取第一游玩人  第一游玩人为空  取联系人
						OrdPerson traveller  = order.getFirstTravellerPerson();
						if(traveller != null){
							post.setFullName(traveller.getFullName());
							post.setMobie(traveller.getMobile());
						}else{
							OrdPerson contact = order.getOnlyContactPerson();
							post.setFullName(contact.getFullName());
							post.setMobie(contact.getMobile());
						}

						OrdPerson addresser = order.getAddressPerson();
						post.setAddressName(addresser.getFullName());
						post.setAddressMobile(addresser.getMobile());

						OrdAddress  address = order.getOrdAddress();
						post.setAddress(StringUtil.coverNullStrValue(address.getProvince())
								+StringUtil.coverNullStrValue(address.getCity())
								+StringUtil.coverNullStrValue(address.getDistrict())
								+StringUtil.coverNullStrValue(address.getStreet()));


						try {
							ordTicketPostService.insertOrdTicketPost(post);
						} catch (Exception e) {
							LOG.info("ordTicketPostService.insertOrdTicketPost error:"+e.getMessage());
						}
					}
				}
			}

			if(order.hasNeedPrepaid() && order.getDistributorId() == Constant.DIST_BACK_END){
				if(order.getOughtAmount()==0&&!order.isNeedResourceConfirm()){//操作0元支付
					LOG.info("ordPrePayServiceAdapter.vstOrder0YuanPayMsg()"+order.getOrderId());
					ordPrePayServiceAdapter.vstOrder0YuanPayMsg(order.getOrderId());
				}

				if(order.hasPayed()){
					ActivitiKey key = createKeyByOrder(order);
					processerClientService.completeTask(key, TASK_KEY.backSaveTraveller.name(), operatorId);
				}
			}

//			if(!isLockSeatSuccess) {
//				handle.setMsg("机票锁舱失败，请重新下单或联系客服下单");
//				cancelAfterLockSeatFail(order);
//			}
		}catch(Exception ex){
			handle.setMsg(ex);
		}
		return handle;
	}
	
	private void logTraceNumber(OrdOrder order) {
		try{
			String content = OrderUtils.getServerIp(false) + ", traceNumber:" + com.lvmama.log.util.LogTrackContext.getTrackNumber();
			lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
					order.getOrderId(),
					order.getOrderId(),
					"SYSTEM",
					content,
					"ORD_ORDER_CREATE",
					"调试信息",
					"");
		} catch(Exception e) {}
	}

	/**
	 * 修改子单总结算价如有extend记录一并修改
	 * @param orderItem
	 */
	private void changeOrderItemExtend(OrdOrderItem orderItem) {
		lvmamaLog.info("updateOrderItem updateExtend start");
		OrdOrderItemExtend extend = ordOrderItemExtendService.selectByPrimaryKey(orderItem.getOrderItemId());
		if (extend != null && extend.getSettlementPriceRate() != null) {
			//外币结算总价
			Long foreignActTotalPrice = orderItem.getRefundCurrencySettPrice();
			Long foreignActPrice = new BigDecimal(foreignActTotalPrice).divide(new BigDecimal(orderItem.getQuantity()),0,BigDecimal.ROUND_UP).longValue();
			OrdOrderItemExtend update = new OrdOrderItemExtend();
			update.setOrderItemId(orderItem.getOrderItemId());
			update.setForeignActTotalSettlePrice(foreignActTotalPrice);
			update.setForeignActualSettlementPrice(foreignActPrice);
			lvmamaLog.info("updateOrderItem updateExtend update:" + JSONUtil.bean2Json(update));
			ordOrderItemExtendService.updateByPrimaryKeySelective(update);
		}
		lvmamaLog.info("updateOrderItem updateExtend end");
	}

}
