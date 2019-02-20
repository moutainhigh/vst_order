package com.lvmama.vst.order.web;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
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

import com.lvmama.comm.bee.po.ord.OrdRefundment;
import com.lvmama.crm.outer.user.api.CrmUserLabelService;
import com.lvmama.vst.back.order.po.OrdAmountChange;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.order.po.OrderItemApportionInfoPO;
import com.lvmama.vst.order.service.IOrderAmountChangeService;
import com.lvmama.vst.order.service.OrderPromotionService;
import com.lvmama.vst.order.utils.ApportionUtil;
import com.lvmama.vst.order.utils.RestClient;
import com.lvmama.vst.order.web.service.OrderDetailApportionService;
import com.lvmama.vst.order.web.service.OrderShipDetailApportionService;
import com.lvmama.vst.pet.adapter.FavorServiceAdapter;
import com.lvmama.vst.pet.adapter.OrderRefundmentServiceAdapter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvmama.comm.bee.po.distribution.DistributorInfo;
import com.lvmama.comm.pet.fs.client.FSClient;
import com.lvmama.comm.pet.fs.vo.ComFile;
import com.lvmama.comm.pet.po.perm.PermUser;
import com.lvmama.comm.vst.VSTEnum;
import com.lvmama.coupon.api.order.service.CouponOrderService;
import com.lvmama.crm.enumerate.CsVipUserIdentityTypeEnum;
import com.lvmama.crm.service.CsVipDubboService;
import com.lvmama.finance.comm.finance.po.SetSettlementItem;
import com.lvmama.finance.comm.vst.service.SettlementService;
import com.lvmama.ship.api.common.vo.ShipResultHandleT;
import com.lvmama.ship.api.common.vo.prod.goods.ShipGoodsBaseVo;
import com.lvmama.ship.api.ship.prod.service.ShipGoodsService;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizDict;
import com.lvmama.vst.back.biz.po.BizDictDef;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.biz.service.DictClientService;
import com.lvmama.vst.back.client.biz.service.DictDefClientService;
import com.lvmama.vst.back.client.biz.service.DistrictClientService;
import com.lvmama.vst.back.client.dist.service.DistributorClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.ord.service.OrderSendSMSService;
import com.lvmama.vst.back.client.ord.service.OrderTravellerConfirmClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdTrafficClientService;
import com.lvmama.vst.back.client.pub.service.ComLogClientService;
import com.lvmama.vst.back.client.supp.service.SuppFaxClientService;
import com.lvmama.vst.back.client.supp.service.SuppSupplierClientService;
import com.lvmama.vst.back.dist.po.Distributor;
import com.lvmama.vst.back.dujia.comm.route.detail.utils.RouteDetailFormat;
import com.lvmama.vst.back.ebooking.vo.fax.EbkFaxVO;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.OrdAuditConfig;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderDownpay;
import com.lvmama.vst.back.order.po.OrdOrderDownpay.PAY_STATUS;
import com.lvmama.vst.back.order.po.OrdOrderDownpay.PAY_TYPE;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdOrderTravellerConfirm;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderAttachment;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.INFO_STATUS;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_AMOUNT_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_COMMON_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PERSON_ID_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PERSON_PEOPLE_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_PRICE_RATE_TYPE;
import com.lvmama.vst.back.order.po.OrderEnum.RESOURCE_STATUS;
import com.lvmama.vst.back.order.po.OrderTravellerOperateDO;
import com.lvmama.vst.back.prod.curise.vo.CuriseProductVO;
import com.lvmama.vst.back.prod.po.ProdEcontract;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prom.po.OrdPayPromotion;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.pub.po.ComMessage;
import com.lvmama.vst.back.supp.po.SuppFaxRule;
import com.lvmama.vst.back.supp.po.SuppSupplier;
import com.lvmama.vst.comlog.LvmmLogClientService;
import com.lvmama.vst.comm.enumeration.CommEnumSet;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.CalendarUtils;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.DESCoder;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.ResourceUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.TimePriceUtils;
import com.lvmama.vst.comm.utils.json.JSONOutput;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.web.HttpServletLocalThread;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Page;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.ResultMessage;
import com.lvmama.vst.comm.vo.econtract.TravelContractVO;
import com.lvmama.vst.comm.vo.order.GoodsPersonVO;
import com.lvmama.vst.comm.vo.order.HotelTimeRateInfo;
import com.lvmama.vst.comm.vo.order.OrderAttachmentVO;
import com.lvmama.vst.comm.vo.order.OrderMonitorRst;
import com.lvmama.vst.comm.web.BaseActionSupport;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.ebooking.client.ebk.serivce.EbkFaxTaskClientService;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif;
import com.lvmama.vst.ebooking.ebk.po.EbkCertif.EBK_CERTIFICATE_CONFIRM_CHANNEL;
import com.lvmama.vst.order.contract.service.IOrderElectricContactService;
import com.lvmama.vst.order.contract.service.IOrderNoticeRegimentService;
import com.lvmama.vst.order.contract.service.IOrderTravelContractDataService;
import com.lvmama.vst.order.contract.service.impl.OrderTravelContractDataServiceFactory;
import com.lvmama.vst.order.contract.vo.OutboundTourContractVO;
import com.lvmama.vst.order.po.OrderCallId;
import com.lvmama.vst.order.service.IComMessageService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdAdditionStatusService;
import com.lvmama.vst.order.service.IOrdAuditConfigService;
import com.lvmama.vst.order.service.IOrdItemPersonRelationService;
import com.lvmama.vst.order.service.IOrdMulPriceRateService;
import com.lvmama.vst.order.service.IOrdOrderPackService;
import com.lvmama.vst.order.service.IOrdPersonService;
import com.lvmama.vst.order.service.IOrdTravelContractService;
import com.lvmama.vst.order.service.IOrderAttachmentService;
import com.lvmama.vst.order.service.IOrderAuditService;
import com.lvmama.vst.order.service.IOrderCallIdService;
import com.lvmama.vst.order.service.IOrderLocalService;
import com.lvmama.vst.order.service.IOrderResponsibleService;
import com.lvmama.vst.order.service.IOrderSendSmsService;
import com.lvmama.vst.order.service.IOrderStatusManageService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.OrdOrderTravellerService;
import com.lvmama.vst.order.service.OrdPayPromotionService;
import com.lvmama.vst.order.service.impl.OrderEcontractGeneratorService;
import com.lvmama.vst.order.utils.CallCenterUtils;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;
import com.lvmama.vst.pet.adapter.TntDistributorServiceAdapter;

import net.sf.json.JSONObject;

/**
 * 油轮订单详情页面action
 * 
 * @author jszhangwei
 * @param <>
 * 
 */
@Controller
@RequestMapping("/order/orderShipManage")
public class OrderShipDetailAction extends BaseActionSupport {

	private static final Logger LOG = LoggerFactory.getLogger(OrderShipDetailAction.class);	
	
	@Autowired
	private IOrderStatusManageService orderStatusManageService;

	@Autowired
	private IComplexQueryService complexQueryService;
	
	@Autowired
	private SuppSupplierClientService suppSupplierClientService;
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
	private PermUserServiceAdapter permUserServiceAdapter;
	
	@Autowired
	private SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;

	@Autowired
	private DistrictClientService districtClientService;
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	@Autowired
	private OrdPayPromotionService ordPayPromotionService;
	
	@Autowired
	private IOrderLocalService orderLocalService;
	
	// 注入分销商业务接口(订单来源、下单渠道)
	@Autowired
	private DistributorClientService distributorClientService;

	@Autowired
	private DictDefClientService dictDefClientService;

	@Autowired
	private CrmUserLabelService crmUserLabelService;
	
	@Autowired
	private DictClientService dictClientService;

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
	
	@Resource(name="noticeRegimentService")
	private IOrderNoticeRegimentService noticeRegimentService;
	@Autowired
	private IOrderSendSmsService iOrderSendSmsService;

	@Autowired
	private OrderTravelContractDataServiceFactory orderTravelContractDataServiceFactory;


	@Autowired
	private OrderRefundmentServiceAdapter orderRefundmentService;
	
	private static final String TRAVEL_ECONTRACT_DIRECTORY = "/WEB-INF/resources/econtractTemplate";
	
	/**
	 * 大交通组详细信息查询服务接口
	 */
	@Autowired
	private ProdTrafficClientService prodTrafficClientServiceRemote;
	
	@Autowired
	private TntDistributorServiceAdapter tntDistributorServiceRemote;

	@Resource(name="orderMessageProducer")
	private TopicMessageProducer orderMessageProducer;
	
	@Autowired
	private OrdOrderTravellerService ordOrderTravellerService;
	
	@Autowired
    private OrderTravellerConfirmClientService orderTravellerConfirmClientService;
	
	@Autowired		
	private OrderEcontractGeneratorService orderEcontractGeneratorService;
	
	//结算状态改造 从支付获取
	@Autowired
	private SettlementService settlementService;
	
	@Autowired
	protected ShipGoodsService shipGoodsService;

	@Autowired
	private OrderShipDetailApportionService orderShipDetailApportionService;

	@Autowired
	private OrderDetailApportionService orderDetailApportionService;

	@Autowired
	private IOrderAmountChangeService orderAmountChangeService;

	@Autowired
	private OrderPromotionService promotionService;

	@Autowired
	private FavorServiceAdapter favorService;
	
	@Resource(name="orderCallIdService")
    private IOrderCallIdService orderCallIdService;
	
	@Autowired(required=false)
	private CsVipDubboService csVipDubboService;
	
	@RequestMapping(value = "/showOrderStatusManage")
	public String showOrderStatusManage(Model model, HttpServletRequest request) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showOrderStatusManage>");
		}
		String loginUserId=this.getLoginUserId();
		model.addAttribute("loginUserId", loginUserId);
		String orderIdStr = getRequestParameter("orderId", request);
		Long orderId=NumberUtils.toLong(orderIdStr);
		String isPaymentFlag="N";
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		if (order.getDistributorId()!=null && (order.getDistributorId().equals(Constant.DIST_O2O_SELL) || order.getDistributorId().equals(Constant.DIST_O2O_APP_SELL))) {
			try {
				String sign=DESCoder.encrypt(order.getBackUserId());
				model.addAttribute("o2oUserNameSign",sign);	
				model.addAttribute("o2oUserName",order.getBackUserId());	
			} catch (Exception e) {
				log.error("orderId"+orderIdStr+e);
			}
		}
		
		//设置团结算标识
		if(null!=order&&StringUtil.isNotEmptyString(order.getGroupSettleFlag())){
			if("Y".equals(order.getGroupSettleFlag())){
				model.addAttribute("groupSettleFlag", "是");
			}else{
				model.addAttribute("groupSettleFlag", "否");
			}
		}

		OrdOrderDownpay ord=null;
		String isDownpayFlag="N";
		//是否已支付
		if(!OrderEnum.PAYMENT_STATUS.PAYED.name().equals(order.getPaymentStatus())){
	        //定金支付设置
			if(CommEnumSet.BU_NAME.OUTBOUND_BU.getCode().equals(order.getBuCode()) &&
					com.lvmama.comm.pay.vo.Constant.PAYMENT_GATEWAY_DIST_MANUAL.DISTRIBUTOR_B2B.getCode().equals(order.getDistributorCode()) &&
					BizEnum.BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().equals(order.getCategoryId())){
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
		
		//vst组织鉴权
		super.vstOrgAuthentication(OrderDetailAction.class, order.getManagerIdPerm());
		
		String distributionPrice = order.getOrderAmountItemByType(ORDER_AMOUNT_TYPE.DISTRIBUTION_PRICE.name());
		model.addAttribute("distributionPrice",distributionPrice);
		OrdOrderPack ordOrderPack=new OrdOrderPack();
		Map<String, Object> paramPack = new HashMap<String, Object>();
		paramPack.put("orderId", orderId);//订单号
		
		List<OrdOrderPack> orderPackList=ordOrderPackService.findOrdOrderPackList(paramPack);
		if (!orderPackList.isEmpty()) {
			ordOrderPack=orderPackList.get(0);
		}
		model.addAttribute("ordOrderPack",ordOrderPack);
		
		
		
		//主订单负责人
		String objectType="ORDER";
		Long objectId=orderId;
		PermUser permUserPrincipal= orderResponsibleService.getOrderPrincipal(objectType, objectId);
		if(permUserPrincipal!=null){
			model.addAttribute("orderPrincipal",permUserPrincipal.getRealName());
		}
		List<OrdPerson> personList = order.getOrdPersonList();
		List<OrdOrderItem> items = order.getOrderItemList();
		OrdPerson ordPersonContact = new OrdPerson();
		OrdPerson ordPersonBooker = new OrdPerson();
		List<OrdPerson> ordPersonEmergencyList = new ArrayList<OrdPerson>(); // 紧急联系人 
		String travellerName = "";
		int travellerNum=0;
		
		List<OrdPerson> travellerList = new ArrayList<OrdPerson>();
		Map<String, Map<String, List<OrdPerson>>> travellerMap = new HashMap<String, Map<String,List<OrdPerson>>>();
		
		//游客列表展示
		for (OrdPerson ordPerson : personList) {
			ordPerson.setIdTypeName(OrderEnum.ORDER_PERSON_ID_TYPE.getCnName(ordPerson.getIdType()));
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("ordPersonId", ordPerson.getOrdPersonId()); 
			List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(params);
			if (!ordItemPersonRelationList.isEmpty()) {
				OrdOrderItem orderItemShip=new OrdOrderItem();
				ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCode());
				BizCategory bizCategoryShip=result.getReturnContent();
				Long categoryId=bizCategoryShip.getCategoryId();
				for (OrdItemPersonRelation ordItemPersonRelation : ordItemPersonRelationList) {
					OrdOrderItem orderItemObj=ordOrderUpdateService.getOrderItem(ordItemPersonRelation.getOrderItemId());
					if (categoryId.equals(orderItemObj.getCategoryId())) {
						orderItemShip=orderItemObj;
						Long suppGoodsId = orderItemShip.getSuppGoodsId();
						if(suppGoodsId == null){
							suppGoodsId = Long.valueOf(0);
						}
						Long roomNo= ordItemPersonRelation.getRoomNo();
						if(roomNo == null){
							roomNo = Long.valueOf(0);
						}
						Map<String, List<OrdPerson>> map1 = travellerMap.get(suppGoodsId.toString());
						if(map1 == null){
							map1 = new HashMap<String, List<OrdPerson>>();
							travellerMap.put(suppGoodsId.toString(), map1);
							List<OrdPerson> persons = new ArrayList<OrdPerson>();
							persons.add(ordPerson);
							map1.put(roomNo.toString(), persons);
						}else{
							List<OrdPerson> persons = map1.get(roomNo.toString());
							if(persons == null){
								persons = new ArrayList<OrdPerson>();
								persons.add(ordPerson);
								map1.put(roomNo.toString(), persons);
							}else{
								persons.add(ordPerson);
							}
						}
						break;
					}
					
				}
				Map<String,Object> contentMap = orderItemShip.getContentMap();
				String branchName =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());
				ordPerson.setCheckInRoomName(branchName);//入住房间
			}
			
			String personType = ordPerson.getPersonType();
			if (OrderEnum.ORDER_PERSON_TYPE.CONTACT.name().equals(personType)) {
				ordPersonContact = ordPerson;
				continue;
			}else if (OrderEnum.ORDER_PERSON_TYPE.BOOKER.name().equals(personType)) {
				ordPersonBooker = ordPerson;
				continue;
			}else if (OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name().equals(personType)) {
				ordPersonEmergencyList.add(ordPerson);
				continue;
			}else if (OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equals(personType)) {
				if (travellerName.length() > 0) {
					travellerName += ",";
				}
				travellerName += ordPerson.getFullName();
				travellerNum+=1;
				travellerList.add(ordPerson);
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
		//数据修复
		if(items != null && (travellerMap == null || (travellerMap != null && travellerMap.size() <= 0))){
			int num = 0;
			int sum = 0;
			int total = 0;
			List<OrdPerson> ordTravellers = order.getOrdTravellerList();
			for(OrdOrderItem item : items){
				Long categoryId = item.getCategoryId();
				if(categoryId == null){
					categoryId = Long.valueOf(0);
				}	
				if(categoryId != 2L ){
					continue;
				}else{
					total ++;
				}
			}
			for(OrdOrderItem item : items){
				Long suppGoodsId = item.getSuppGoodsId();
				Long categoryId = item.getCategoryId();
				Long quantity = item.getQuantity();
				Map<String,Object> contentMapShip = item.getContentMap();
				if(categoryId == null){
					categoryId = Long.valueOf(0);
				}
				if(contentMapShip == null){
					contentMapShip = new HashMap<String, Object>();
				}
				String branchName =  (String) contentMapShip.get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());
				if(categoryId != 2L ){
					continue;
				}
				if(suppGoodsId == null){
					suppGoodsId = Long.valueOf(0);
				}
				if(quantity == null){
					quantity = Long.valueOf(0);
				}				
				for(int i=0; i < quantity.longValue(); i++){
					//最后一个item最后一间房
					if(num == (total - 1) && (i == (quantity.longValue() - 1))){
						for(int t=sum; t < (ordTravellers.size()); t++){
							Map<String, List<OrdPerson>> map1 = travellerMap.get(suppGoodsId.toString());
							OrdPerson ordPerson2 = ordTravellers.get(t);
							if(ordPerson2 == null){
								continue;
							}
							ordPerson2.setCheckInRoomName(branchName + "");
							if(map1 == null){
								map1 = new HashMap<String, List<OrdPerson>>();
								travellerMap.put(suppGoodsId.toString(), map1);
								List<OrdPerson> persons = new ArrayList<OrdPerson>();
								if(t >= ordTravellers.size()){
									break;
								}
								persons.add(ordPerson2);
								map1.put((i+1)+"", persons);
							}else{
								List<OrdPerson> persons = map1.get((i+1)+"");
								if(persons == null){
									persons = new ArrayList<OrdPerson>();
									if(i >= ordTravellers.size()){
										break;
									}							
									persons.add(ordPerson2);
									map1.put((i+1)+"", persons);
								}else{
									if(i >= ordTravellers.size()){
										break;
									}
									persons.add(ordPerson2);
								}
							}
						}
						break;
					}						
					
					Map<String, List<OrdPerson>> map1 = travellerMap.get(suppGoodsId.toString());
					if(sum >= ordTravellers.size()){
						break;
					}
					OrdPerson ordPerson2 = ordTravellers.get(sum);
					if(ordPerson2 == null){
						continue;
					}
					ordPerson2.setCheckInRoomName(branchName + "");
					if(map1 == null){
						map1 = new HashMap<String, List<OrdPerson>>();
						travellerMap.put(suppGoodsId.toString(), map1);
						List<OrdPerson> persons = new ArrayList<OrdPerson>();
						persons.add(ordPerson2);
						map1.put((i+1)+"", persons);
					}else{
						List<OrdPerson> persons = map1.get((i+1)+"");
						if(persons == null){
							persons = new ArrayList<OrdPerson>();
							persons.add(ordPerson2);
							map1.put((i+1)+"", persons);
						}else{
							persons.add(ordPerson2);
						}
					}	
					sum++;
				}
				num++; 
			}
		}

		//退款
		Long refunds = 0L;
		Long compensations = 0L;
		List<OrdRefundment> ordRefundments = orderRefundmentService.findOrderRefundmentByOrderIdStatus(order.getOrderId(), Constant.REFUNDMENT_STATUS.REFUNDED.name());
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
		model.addAttribute("refunds",PriceUtil.trans2YuanStr(refunds));
		model.addAttribute("compensations",PriceUtil.trans2YuanStr(compensations));


		//此订单优惠总金额
		Long favorUsageAmount = favorService.getSumUsageAmount(order.getOrderId());
		model.addAttribute("favorUsageAmount",favorUsageAmount==null?0:new BigDecimal(favorUsageAmount.floatValue()/100).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue());

		//促销减少总金额
		String totalOrderAmount = order.getOrderAmountItemByType(OrderEnum.ORDER_AMOUNT_TYPE.PROMOTION_PRICE.name());
		model.addAttribute("totalOrderAmount",totalOrderAmount);

		//订单金额-价格修改
		Long totalAmountChange=getTotalAmountChange(orderId,null,null);
		model.addAttribute("totalAmountChange", PriceUtil.trans2YuanStr(totalAmountChange));

		model.addAttribute("personMap", travellerMap);
		model.addAttribute("ordPersonContact", ordPersonContact);
		model.addAttribute("ordPersonBooker", ordPersonBooker);
		model.addAttribute("ordPersonEmergencyList", ordPersonEmergencyList);
		model.addAttribute("travellerName", travellerName);
		model.addAttribute("travellerNum", travellerNum);
		//游玩人是否后置
		model.addAttribute("travellerDelayFlag", order.getTravellerDelayFlag());
		//model.addAttribute("travellerDelayFlag", "Y");
		//游玩人是否锁定
		model.addAttribute("travellerLockFlag", order.getTravellerLockFlag());
		//model.addAttribute("travellerLockFlag", "Y");
		
		if(orderId!=null){
			OrdOrderTravellerConfirm 
						ordOrderTravellerConfirm =
									orderTravellerConfirmClientService.
										findOrderTravellerConfirmInfoByOrderId(orderId);
			if(ordOrderTravellerConfirm!=null){
				model.addAttribute("ordOrderTravellerConfirm", ordOrderTravellerConfirm);
			}
		}
		
		//ProdProduct prodProduct=prodProductClientService.findProdProductById(ordOrderPack.getProductId(), Boolean.TRUE, Boolean.TRUE);
		if(order.getDistributionChannel() != null) {
			ResultHandleT<DistributorInfo> distributorInfoResult = tntDistributorServiceRemote.getDistributorById(order.getDistributionChannel());
			if (distributorInfoResult.isFail() || distributorInfoResult.getReturnContent() == null) {
				LOG.error("获取渠道代码" + order.getDistributionChannel() + "对应的渠道信息失败");
			}
			if(distributorInfoResult.getReturnContent() != null) {
				model.addAttribute("distributionChannelName", distributorInfoResult.getReturnContent().getDistributorName());
			}
		}

		//下单渠道
		Distributor distributor = distributorClientService.findDistributorById(order.getDistributorId()).getReturnContent();
		
		//产品经理
		if(order.getManagerId() != null){
			PermUser permUser=permUserServiceAdapter.getPermUserByUserId(order.getManagerId());
			if (permUser!=null) {
				//String productManager=permUser.getUserName();
				model.addAttribute("productManager",permUser );
			}
		}
				
		model.addAttribute("distributorName", distributor.getDistributorName());
		model.addAttribute("orderStatusStr", OrderEnum.ORDER_STATUS.getCnName(order.getOrderStatus()));
		model.addAttribute("paymentStatusStr", OrderEnum.PAYMENT_STATUS.getCnName(order.getPaymentStatus()));
		
		model.addAttribute("order", order);
		
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
		
		if(order.getLastCancelTime()!=null){
			Date now=new Date();
			Date lastCancelTime=order.getLastCancelTime();
			model.addAttribute("isGreaterNow", now.compareTo(lastCancelTime));
		}
		
		
		//产品名称  ORD_ORDER_PACK
		//上船地点     下船地点 	 所属航线
		Map<String,Object> orderPackContentMap = ordOrderPack.getContentMap();
		model.addAttribute("orderPackContentMap",orderPackContentMap);

		
		String endSailingDate =  (String) orderPackContentMap.get(OrderEnum.ORDER_PACK_TYPE.end_sailing_date.name());
		if (!StringUtils.isEmpty(endSailingDate)) {
			model.addAttribute("arrivalDays", 1+CalendarUtils.getDayCounts(order.getVisitTime(), DateUtil.toDate(endSailingDate, "yyyy-MM-dd")));
		}
		
		//相关订单
		/*
		ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();
				//condition.getOrderContentParam().setUserId(order.getUserId());
		condition.getOrderContentParam().setUserNo(order.getUserNo());
		condition.getOrderIndentityParam().setProductId(ordOrderPack.getProductId());
		condition.getOrderStatusParam().setOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.getCode());
		//组装订单标志类条件
		condition.getOrderFlagParam().setOrderTableFlag(true);//获得订单号
		condition.getOrderFlagParam().setOrderItemTableFlag(false);//
		condition.getOrderFlagParam().setOrderPersonTableFlag(true);//获得联系人
		condition.getOrderFlagParam().setOrderHotelTimeRateTableFlag(false);//获得离店时间
		condition.getOrderFlagParam().setOrderPageFlag(true);//需要分页
		//组装订单排序类条件
		condition.getOrderSortParams().add(OrderSortParam.CREATE_TIME_DESC);

		// 根据条件获取订单总记录数
		Long totalCount = complexQueryService.queryOrderCountByCondition(condition);
		boolean otherOrder=false;		
		if (totalCount>=2) {
			otherOrder=true;
		}
		model.addAttribute("otherOrder",otherOrder);*/
		
		//节省系统开销，不进行查询，统一设置为true，前台显示“另有订单”
		model.addAttribute("otherOrder",true);
		
		//定金是否存在
		boolean hasDepositsAmount=false;
		if (order.getDepositsAmount()!=null && order.getDepositsAmount()>0 ) {
			hasDepositsAmount=true;
		}
		model.addAttribute("hasDepositsAmount",hasDepositsAmount);
		
		//定金应收款
		double depositsOughtAmount=0.0;
		//订单已收款大于定金应收款的时候
		if ( (order.getActualAmount()-order.getDepositsAmount() ) >0) {
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
		if (TimePriceUtils.hasPreauthBook(order.getLastCancelTime(),
					order.getCreateTime())) {
			hasPreauthBook=true;
		}
		model.addAttribute("hasPreauthBook",hasPreauthBook);
		
		//订单取消类型
		Map<String, Object> dictDefPara = new HashMap<String, Object>();
		dictDefPara.put("dictCode", Constants.ORDER_CANCEL_TYPE);
		dictDefPara.put("cancelFlag", "Y");
		List<BizDictDef> dictDefList = dictDefClientService.findDictDefList(dictDefPara).getReturnContent();
		model.addAttribute("orderCancelTypeList", dictDefList);
		
		
		//预定通知
		int messageCount;
		Long[] auditIdArray = findBookingAuditIds("ORDER",orderId);
		if (auditIdArray.length==0) {
			messageCount=0;
		}else{
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("messageStatus",OrderEnum.MESSAGE_STATUS.UNPROCESSED.getCode());
			parameters.put("auditIdArray",auditIdArray);
			//parameters.put("receiver",loginUserId);
			messageCount=comMessageService.findComMessageCount(parameters);
		}
		model.addAttribute("messageCount", messageCount);
		
		
		//活动判断逻辑
		
		
		boolean isDonePretrialAudit = this.isDonePretrialAudit(order);
		model.addAttribute("isDonePretrialAudit", isDonePretrialAudit);
		
		
		boolean isDoneNoticeRegimentAudit = this.isDoneNoticeRegimentAudit(order);
		model.addAttribute("isDoneNoticeRegimentAudit", isDoneNoticeRegimentAudit);
		
		
		boolean isDonePaymentAudit = this.isDonePaymentAudit(order);
		model.addAttribute("isDonePaymentAudit", isDonePaymentAudit);
		
		boolean isDoneCancleConfirmedtAudit =this.isDoneCancleConfirmedAudit(order);
		model.addAttribute("isDoneCancleConfirmedtAudit", isDoneCancleConfirmedtAudit);
		
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
		model.addAttribute("hasInfoAndResourcePass", order.hasInfoAndResourcePass());
		
		//合同状态
		OrdTravelContract ordTravelContract = findOrdTravelContract(orderId);
		model.addAttribute("ordTravelContract",ordTravelContract);
		model.addAttribute("contractStatusName",OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.getCnName(ordTravelContract.getStatus()));
		
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
				
		//舱房子订单
		OrdOrderItem ordItemShip=new OrdOrderItem();
		
		//子订单列表展示
		List<OrderMonitorRst> childOrderResultList = new ArrayList<OrderMonitorRst>();
		List<OrdOrderItem> orderItemsList = ordOrderUpdateService.queryOrderItemByOrderId(orderId);
		Map<String, Object> attachmentParam = new HashMap<String, Object>();
		for (OrdOrderItem ordOrderItem : orderItemsList) {
			Map<String,Object> contentMap = ordOrderItem.getContentMap();
			String childOrderTypeCode =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
			
			if (BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCode().equals(childOrderTypeCode)) {
				ordItemShip=ordOrderItem;
			}

			objectType="ORDER_ITEM";
			objectId=ordOrderItem.getOrderItemId();
			
			OrderMonitorRst orderMonitorRst = new OrderMonitorRst();
			PermUser orderPrincipal=orderResponsibleService.getOrderPrincipal(objectType, objectId);
			if(orderPrincipal!=null){
				String principal = orderPrincipal.getRealName();
				orderMonitorRst.setPrincipal(principal);//负责人
			}
			//资源审核人
			String resourceApprover = orderResponsibleService
					.getResourceApprover(objectId, objectType).getRealName();
			orderMonitorRst.setResourceApprover(resourceApprover);//资源审核人
			orderMonitorRst.setOrderItemMemo(ordOrderItem.getOrderMemo());//子订单备注
			orderMonitorRst.setOrderId(ordOrderItem.getOrderId());
			orderMonitorRst.setOrderItemId(ordOrderItem.getOrderItemId());
			orderMonitorRst.setCurrentStatus(this.buildCurrentStatus(order,ordOrderItem));
			orderMonitorRst.setChildOrderType(childOrderTypeCode);
			orderMonitorRst.setChildOrderTypeName(BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCnName(childOrderTypeCode));
			orderMonitorRst.setProductName(this.buildProductName(ordOrderItem));
			
			if(contentMap.containsKey("child_quantity") || contentMap.containsKey("adult_quantity")) {
				if(contentMap.containsKey("child_quantity")) {
					orderMonitorRst.setChildBuyCount(contentMap.get("child_quantity")==null? 0 
							: (Integer)contentMap.get("child_quantity"));
				}
				
				if(contentMap.containsKey("adult_quantity")) {
					orderMonitorRst.setAdultBuyCount(contentMap.get("adult_quantity")==null? 0 
							: (Integer)contentMap.get("adult_quantity"));
				}
			}
			orderMonitorRst.setBuyCount(ordOrderItem.getQuantity().intValue());
			
			String categoryCode =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
			buildBuyPrice(ordOrderItem, orderMonitorRst, categoryCode);// 构建子订单销售价
			
			orderMonitorRst.setVisitTime(DateUtil.formatDate(ordOrderItem.getVisitTime(), "yyyy-MM-dd"));
			Map<String, Object> paramOrdItemPersonRelation = new HashMap<String, Object>();
			paramOrdItemPersonRelation.put("orderItemId", ordOrderItem.getOrderItemId()); 
			List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(paramOrdItemPersonRelation);
			
			orderMonitorRst.setPersonCount(ordItemPersonRelationList.size());
			
			//附件数量
			attachmentParam.put("orderId", orderId);
			attachmentParam.put("orderItemId", ordOrderItem.getOrderItemId());
			orderMonitorRst.setOrderAttachmentNumber(orderAttachmentService.countOrderAttachmentByCondition(attachmentParam));
			
			childOrderResultList.add(orderMonitorRst);
		}
		//假如开关打开的话，加入分摊信息
		if (ApportionUtil.isApportionEnabled()) {
			orderShipDetailApportionService.calcOrderDetailItemApportion(orderId, childOrderResultList);
		}

		model.addAttribute("childOrderMonitorRstList",childOrderResultList);
		model.addAttribute("ordItemShip", ordItemShip);
		
		
		//退改政策
		String cancelStrategyType=order.getCancelStrategy();
		if (!StringUtils.isEmpty(cancelStrategyType)) {
			model.addAttribute("cancelStrategyTypeStr", SuppGoodsTimePrice.CANCELSTRATEGYTYPE.getCnName(cancelStrategyType));
		}
		Long deductAmount=order.getAllDeductAmount();
		
		model.addAttribute("deductAmountStr", deductAmount/100.0+"");
		/*
		//签约状态			签证状态			出团通知书  状态
		Map<String, Object> paramsOrdAdditionStatus = new HashMap<String, Object>();
		paramsOrdAdditionStatus.put("orderId", orderId);//订单号
		List<OrdAdditionStatus> ordAdditionStatusList=ordAdditionStatusService.findOrdAdditionStatusList(paramsOrdAdditionStatus);
		//Map additionStatusMap=new HashMap<String ,>();
		for (OrdAdditionStatus ordAdditionStatus : ordAdditionStatusList) {
			String status="";
			if (OrderEnum.ORD_ADDITION_STATUS_TYPE.CONTRACT_STATUS.getCode().equals(ordAdditionStatus.getStatusType())) {
				status=OrderEnum.CONTRACT_STATUS_TYPE.getCnName(ordAdditionStatus.getStatus());
			}else if (OrderEnum.ORD_ADDITION_STATUS_TYPE.VISA_STATUS.getCode().equals(ordAdditionStatus.getStatusType())) {
				//status=OrderEnum..getCnName(ordAdditionStatus.getStatus());
			}else if (OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.getCode().equals(ordAdditionStatus.getStatusType())) {
				status=OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.getCnName(ordAdditionStatus.getStatus());
			}
			model.addAttribute(ordAdditionStatus.getStatusType(),status);
			
		}*/
		
		
		//支付等待时间
		String waitPaymentTime=DateUtil.formatDate(order.getWaitPaymentTime(), "yyyy-MM-dd HH:mm");
		if (waitPaymentTime==null) {
			waitPaymentTime="";
		}
		model.addAttribute("waitPaymentTime", waitPaymentTime);
		
        //add by zjt
        String callId = StringUtils.isEmpty(request.getParameter("callid"))?"":request.getParameter("callid");
        model.addAttribute("callid", callId);		
		
		return "/order/orderStatusManage/ship/orderShipDetails";
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
				model.addAttribute("markCouponUsageList", favorService.getMarkCouponUsageByOrderId(orderId));
			} catch (Exception e) {
				model.addAttribute("msg", "调用接口异常!");
				LOG.error(ExceptionFormatUtil.getTrace(e));
			}
		}
		return "/order/orderStatusManage/allCategory/orderFavorUsageDetail";
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
						amountChangeDesc.append(OrderEnum.ORDER_PRICE_RATE_TYPE.getCnName(m.getKey()));
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


	@RequestMapping(value = "/findGoodsPersonList")
	public String findGoodsPersonList(Model model, HttpServletRequest request,Long orderId, Long orderItemId, String isChild){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<findGoodsPersonList>");
		}

		List<GoodsPersonVO> goodsPersonList=new ArrayList<GoodsPersonVO>();
		List<OrdOrderItem> ordItemList=ordOrderUpdateService.queryOrderItemByOrderId(orderId);
		for (OrdOrderItem ordOrderItem : ordItemList) {
			if(ordOrderItem.getCategoryId().longValue() == 3L){
				if(StringUtil.isNotEmptyString(isChild)){
					if("Y".equals(isChild)){
						if((orderItemId.longValue() != ordOrderItem.getOrderItemId().longValue())){
							continue;
						}
					}else{
						;
					}
				}else {
					break;
				}
			}else{
				continue;
			}
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("orderItemId", ordOrderItem.getOrderItemId()); 
			List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(params);
			if (CollectionUtils.isEmpty(ordItemPersonRelationList)) {
				continue;
			}
			
			GoodsPersonVO goodsPersonVO=new GoodsPersonVO();
			goodsPersonVO.setGoodsName(ordOrderItem.getSuppGoodsName());
			goodsPersonVO.setAssociationCount(ordItemPersonRelationList.size()); 
			goodsPersonVO.setNoAssociationCount(goodsPersonVO.getAssociationCount()-ordItemPersonRelationList.size());
			
			StringBuffer associationPerson=new StringBuffer();
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
		return "/order/orderStatusManage/ship/findGoodsPersonList";
	}		
	
	/**
	 * 构建子订单销售单价与总价
	 * 
	 * @param orderItem
	 * @return
	 */
	private void buildBuyPrice(OrdOrderItem orderItem, OrderMonitorRst orderMonitorRst, String categoryCode) {
		
		StringBuffer buyItemPrice = new StringBuffer(); // 销售价
		Long buyItemTotalPrice = 0L; // 总价
		
		if (BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCode().equals(categoryCode)) {
			buyItemPrice.append(PriceUtil.trans2YuanStr(orderItem.getPrice())).append("元");
			buyItemTotalPrice += orderItem.getPrice()*orderItem.getQuantity();
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCode().equals(categoryCode)){
			buyItemPrice.append(PriceUtil.trans2YuanStr(orderItem.getPrice())).append("元");
			buyItemTotalPrice += orderItem.getPrice()*orderItem.getQuantity();			
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCode().equals(categoryCode)){
			String[] priceTypeArray = new String[] {
					ORDER_PRICE_RATE_TYPE.PRICE_ADULT_12.getCode(),
					ORDER_PRICE_RATE_TYPE.PRICE_ADULT_34.getCode(),
					ORDER_PRICE_RATE_TYPE.PRICE_CHILD_34.getCode(),
					ORDER_PRICE_RATE_TYPE.PRICE_GAP.getCode()};

			Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
			paramsMulPriceRate.put("orderItemId", orderItem.getOrderItemId()); 
			paramsMulPriceRate.put("priceTypeArray",priceTypeArray ); 
			
			List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
			if (CollectionUtils.isNotEmpty(ordMulPriceRateList)) {
				for (int i = 0; i < ordMulPriceRateList.size(); i++) {
					OrdMulPriceRate ordMulPriceRate = ordMulPriceRateList.get(i);
					if (i>0) {
						buyItemPrice.append("</br>");
					}
					buyItemPrice.append(ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCnName(ordMulPriceRate.getPriceType())).append(":").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");
				}
			}else{
				buyItemPrice.append(PriceUtil.trans2YuanStr(orderItem.getPrice())).append("元");
			}
			buyItemTotalPrice += orderItem.getPrice()*orderItem.getQuantity();
		}else if(BizEnum.BIZ_CATEGORY_TYPE.category_sightseeing.getCode().equals(categoryCode) 
				|| BizEnum.BIZ_CATEGORY_TYPE.category_cruise_addition.getCode().equals(categoryCode)){
			String[] priceTypeArray = new String[] {
					ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCode(),
					ORDER_PRICE_RATE_TYPE.PRICE_CHILD.getCode()};

			Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
			paramsMulPriceRate.put("orderItemId", orderItem.getOrderItemId()); 
			paramsMulPriceRate.put("priceTypeArray",priceTypeArray ); 
			
			List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
			if (CollectionUtils.isNotEmpty(ordMulPriceRateList)) {
				for (int i = 0; i < ordMulPriceRateList.size(); i++) {
					OrdMulPriceRate ordMulPriceRate = ordMulPriceRateList.get(i);
					if (i>0) {
						buyItemPrice.append("</br>");
					}
					buyItemPrice.append(ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCnName(ordMulPriceRate.getPriceType())).append(":").append(PriceUtil.trans2YuanStr(ordMulPriceRate.getPrice())).append("元");
					buyItemTotalPrice += ordMulPriceRate.getPrice()*ordMulPriceRate.getQuantity();
				}
			}else{
				buyItemPrice.append(PriceUtil.trans2YuanStr(orderItem.getPrice())).append("元");
				buyItemTotalPrice += orderItem.getPrice()*orderItem.getQuantity();
			}
		}
				
		orderMonitorRst.setBuyItemPrice(buyItemPrice.toString());
		orderMonitorRst.setBuyItemTotalPrice(PriceUtil.trans2YuanStr(buyItemTotalPrice)+"元");
				
	}


	public OrdTravelContract findOrdTravelContract(Long orderId) {
		//合同状态
		OrdTravelContract ordTravelContract=new OrdTravelContract();
		Map<String, Object> parametersTravelContract = new HashMap<String, Object>();
		parametersTravelContract.put("orderId",orderId);
		List<OrdTravelContract> ordTravelContractList=ordTravelContractService.findOrdTravelContractList(parametersTravelContract);
		if (!ordTravelContractList.isEmpty()) {
			ordTravelContract=ordTravelContractList.get(0);
		}
		return ordTravelContract;
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
		
	@RequestMapping(value = "/showChildOrderStatusManage")
	public String showChildOrderStatusManage(Model model, HttpServletRequest request,Long orderItemId)
			 {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showChildOrderStatusManage>");
		}
//		String loginUserId=this.getLoginUserId();

		
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
		Long[] auditIdArray = findBookingAuditIds("ORDER_ITEM",orderItemId);
		if (auditIdArray.length==0) {
			messageCount=0;
		}else{
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("messageStatus",OrderEnum.MESSAGE_STATUS.UNPROCESSED.getCode());
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
		model.addAttribute("paymentStatusStr", OrderEnum.PAYMENT_STATUS.getCnName(order.getPaymentStatus()));
		
		//合同状态
		OrdTravelContract ordTravelContract = findOrdTravelContract(orderId);
		model.addAttribute("ordTravelContract",ordTravelContract);
		model.addAttribute("contractStatusName",OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS.getCnName(ordTravelContract.getStatus()));
				
		

		/*
		//签约状态			签证状态			出团通知书  状态
		Map<String, Object> paramsOrdAdditionStatus = new HashMap<String, Object>();
		paramsOrdAdditionStatus.put("orderId", orderId);//订单号
		List<OrdAdditionStatus> ordAdditionStatusList=ordAdditionStatusService.findOrdAdditionStatusList(paramsOrdAdditionStatus);
		//Map additionStatusMap=new HashMap<String ,>();
		for (OrdAdditionStatus ordAdditionStatus : ordAdditionStatusList) {
			String status="";
			if (OrderEnum.ORD_ADDITION_STATUS_TYPE.CONTRACT_STATUS.getCode().equals(ordAdditionStatus.getStatusType())) {
				status=OrderEnum.CONTRACT_STATUS_TYPE.getCnName(ordAdditionStatus.getStatus());
			}else if (OrderEnum.ORD_ADDITION_STATUS_TYPE.VISA_STATUS.getCode().equals(ordAdditionStatus.getStatusType())) {
				//status=OrderEnum..getCnName(ordAdditionStatus.getStatus());
			}else if (OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS.getCode().equals(ordAdditionStatus.getStatusType())) {
				status=OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.getCnName(ordAdditionStatus.getStatus());
			}
			model.addAttribute(ordAdditionStatus.getStatusType(),status);
			
		}*/
		
		
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
		model.addAttribute("orderStatusStr", OrderEnum.ORDER_STATUS.getCnName(order.getOrderStatus()));
		//结算状态
		//model.addAttribute("settlementStatusStr", OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.getCnName(orderItem.getSettlementStatus()));

		/**
		* 2017/03/06 结算状态改造
		*/

		model.addAttribute("settlementStatusStr", OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED.getCnName(getSetSettlementItemStatus(orderItem.getOrderItemId())));

		
		

		//订单取消类型
		Map<String, Object> dictDefPara = new HashMap<String, Object>();
		dictDefPara.put("dictCode", Constants.ORDER_CANCEL_TYPE);
		dictDefPara.put("cancelFlag", "Y");
		List<BizDictDef> dictDefList = dictDefClientService.findDictDefList(dictDefPara).getReturnContent();
		model.addAttribute("orderCancelTypeList", dictDefList);
		return "/order/orderStatusManage/ship/orderChildShipDetails";
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
		Page pageParam = Page.page(count, 10, pagenum);
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
	
	
	@RequestMapping(value = "/showSoundRecList")
	public String showSoundRecList(Model model,Long objectId,String objectType,Integer page,HttpServletRequest req){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<OrderShipDetailAction.showlogAndSoundRecList>");
		}
/*		Map<String, String> urlParamMap = new HashMap<String, String>();
		urlParamMap.put("objectType", objectType);		
		urlParamMap.put("objectId", objectId.toString());
		urlParamMap.put("sysName", "VST");
		if (page == null || page <= 0){
			urlParamMap.put("curPage", "1");
		}
		else{
			urlParamMap.put("curPage", page.toString());
		}
		Page<ComLog> comLogPage = CallCenterUtils.getRealComLogPageInfo(objectId, page, urlParamMap);*/
		
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
	public void saveCallIdAndOrderId(Model model,Long orderId, String callId){
		JSONObject jsonResult=new JSONObject();
		Long objectId = orderId;
		if (StringUtils.isNotBlank(callId) && objectId != null){
			try{
	    		DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    		String currDateTime = f.format(new Date());
				String createDateOfCallid = getRequest().getParameter("createdateofcallid")==null?currDateTime:this.getRequest().getParameter("createdateofcallid");				
				String content = CallCenterUtils.getContent(objectId, callId, createDateOfCallid);
			    String operatorName = getLoginUserId();
/*				//保存到日志表(先判断是否已经关联过了)
				Map<String, String> urlParamMap = new HashMap<String, String>();
				urlParamMap.put("objectType", ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER.getCode());		
				urlParamMap.put("objectId", objectId.toString());
				urlParamMap.put("sysName", "VST");
				urlParamMap.put("curPage", "1");
				//urlParamMap.put("content", content);
				urlParamMap.put("parentId", String.valueOf(objectId + CallCenterUtils.PARENT_DELTA));
				
				Page<ComLog> comLogPage = CallCenterUtils.getOrdLogInfo(objectId, 1L, urlParamMap);
				if (comLogPage == null || comLogPage.getItems() == null || comLogPage.getItems().size() <= 0 ||  
						comLogPage.getTotalResultSize() <= 0 || !CallCenterUtils.isExistsCallIdJoin(comLogPage, orderId, callId, createDateOfCallid)){
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
				jsonResult.put("msg", e.getMessage().substring(0,100));				
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
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<updateOrderStatus>");
		}
		String operation=request.getParameter("operation");
		String cancelCode=request.getParameter("cancelCode");
		String cancleReasonText=request.getParameter("cancleReasonText");
		//String orderRemark=request.getParameter("orderRemark");
		String orderRemark=request.getParameter("orderRemark");
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
				result = orderStatusManageService.updatePaymentAudit(oldOrder, loginUserId, orderRemark,OrderEnum.AUDIT_TYPE.PAYMENT_AUDIT.getCode());
			}
		}else if ("timePaymentAudit".equals(operation)) {//小驴分期催支付
			
			if (isDoneTimePaymentAudit(oldOrder)) {
				result.setMsg(msg+" 小驴分期催支付活动状态处于已处理，不可再次催支付  ");
			}else{
				result = orderStatusManageService.updatePaymentAudit(oldOrder, loginUserId, orderRemark,OrderEnum.AUDIT_TYPE.TIME_PAYMENT_AUDIT.getCode());
			}
		}else if ("noticeRegimentAudit".equals(operation)) {//通知出团
			
			if (isDoneNoticeRegimentAudit(oldOrder)) {
				result.setMsg(msg+" 通知出团活动状态处于已处理，不可再次通知出团 ");
			}else{
				result = orderStatusManageService.updateNoticeRegimentAudit(oldOrder, loginUserId, orderRemark);
			}
		}else if ("cancelStatusConfim".equals(operation)) {//取消订单已确认
			
			result =orderLocalService.updateCancelConfim(oldOrder, loginUserId, orderRemark);
			
		}else if ("pretrialAudit".equalsIgnoreCase(operation)){
			result = orderLocalService.updatePretrialAudit(oldOrder, loginUserId, orderRemark);
		}else if ("cancelStatus".equals(operation)) {
			
			if (oldOrder.getOrderStatus().equals(OrderEnum.ORDER_STATUS.CANCEL.name())) {
				name=OrderEnum.ORDER_STATUS.CANCEL.getCnName(oldOrder.getOrderStatus());
				result.setMsg(msg+"订单取消状态  "+name);
			}else{
				
				result = orderLocalService.cancelOrder(oldOrder.getOrderId(), cancelCode, cancleReasonText, loginUserId, orderRemark);
				
			}
		}
		if (result.isFail()) {
			return new ResultMessage(ResultMessage.ERROR, result.getMsg()+" 请勿再次操作，页面即将刷新");
			
		}

		return ResultMessage.UPDATE_SUCCESS_RESULT;
	}


	
	
	

	/**
	 * @param orderItem
	 * @param orderAttachment
	 * @return
	 */
	@RequestMapping(value = "/updateChildOrderStatus")
	@ResponseBody
	public Object updateChildOrderStatus( HttpServletRequest request,OrdOrderItem orderItem,OrderAttachment orderAttachment) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<updateChildOrderStatus>");
		}
		String operation=request.getParameter("operation");
		String cancelCode=request.getParameter("cancelCode");
		String cancleReasonText=request.getParameter("cancleReasonText");
		//String orderRemark=request.getParameter("orderRemark");
		String orderRemark=request.getParameter("orderRemark");
		
		
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
				name=OrderEnum.INFO_STATUS.INFOPASS.getCnName(oldOrderItem.getInfoStatus());
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
				name=OrderEnum.RESOURCE_STATUS.AMPLE.getCnName(oldOrderItem.getInfoStatus());
				result.setMsg(msg+"资源审核状态 "+name);
			}else{
				newStatus=RESOURCE_STATUS.AMPLE.name();
				String resourceRetentionTime=request.getParameter("resourceRetentionTime");
				
				result =orderLocalService.executeUpdateChildResourceStatus(oldOrderItem, newStatus, resourceRetentionTime,loginUserId, orderRemark, false);
				
			}	
 		}else if ("certificateStatus".equals(operation)) {//凭证确认
 			//根据该orderId和凭证确认类型查询附件,判读是否已经凭证确认活动过
			if (isDoneCertificate(oldOrderItem.getCertConfirmStatus())) {
				result.setMsg(msg+" 凭证确认已经完成,不可再次凭证确认  ");
			}else{

	 			orderAttachment.setOrderId(orderId);
	 			orderAttachment.setAttachmentType(OrderEnum.ATTACHMENT_TYPE.CERTIFICATE.name());
	 			orderAttachment.setMemo("凭证确认附件");
	 			orderAttachment.setCreateTime(Calendar.getInstance().getTime());
	 			orderAttachment.setFileId(orderAttachment.getFileId());
	 			
	 			result = orderLocalService.updateChildCertificateStatus(oldOrderItem, orderAttachment, loginUserId, orderRemark);
			}
		}else if ("cancelStatusConfim".equals(operation)) {//取消订单已确认
			result =orderLocalService.updateCancelConfim(oldOrder, loginUserId, orderRemark);
		}else if ("cancelStatus".equals(operation)) {
			if (oldOrder.getOrderStatus().equals(OrderEnum.ORDER_STATUS.CANCEL.name())) {
				name=OrderEnum.ORDER_STATUS.CANCEL.getCnName(oldOrder.getOrderStatus());
				result.setMsg(msg+"订单取消状态  "+name);
			}else{
				result = orderLocalService.cancelOrder(oldOrder.getOrderId(), cancelCode, cancleReasonText, loginUserId, orderRemark);
			}
		}
		if (result.isFail()) {
			return new ResultMessage(ResultMessage.ERROR, result.getMsg()+" 请勿再次操作，页面即将刷新");
		}

		return ResultMessage.UPDATE_SUCCESS_RESULT;
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
		String resourceRetentionTime =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.res_retention_time.name());
		
		model.addAttribute("resourceRetentionTime", resourceRetentionTime);
		model.addAttribute("kssj",DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm")+":00");
		model.addAttribute("jssj",DateUtil.formatDate(orderItem.getVisitTime(), "yyyy-MM-dd")+" 23:59:00");
		
		return "/order/orderStatusManage/ship/showUpdateRetentionTime";
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
	public String showManualSendOrderFax(Model model, HttpServletRequest request){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showManualSendOrderFax>");
		}
		
		String orderIdStr=request.getParameter("orderId");
		Long orderId=NumberUtils.toLong(orderIdStr);
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		OrdOrderItem orderItem=order.getMainOrderItem();
		
		model.addAttribute("order", order);
		model.addAttribute("orderItem", orderItem);
		
		String faxFlag=(String)orderItem.getContentValueByKey(ORDER_COMMON_TYPE.fax_flag.name());
		
		if ("Y".equals(faxFlag)) {
			ShipResultHandleT<ShipGoodsBaseVo> resultHandleSuppGoods = shipGoodsService.findGoodsDetail(orderItem.getSuppGoodsId());
			if (resultHandleSuppGoods.isSuccess() && resultHandleSuppGoods.getReturnContent() != null) {
				ShipGoodsBaseVo suppGoods = resultHandleSuppGoods.getReturnContent();
				Long faxRuleId= suppGoods.getFaxRuleId();
				if (faxRuleId!=null) {
					ResultHandleT<SuppFaxRule> resultHandleSuppFaxRule = suppFaxClientService.findSuppFaxRuleById(faxRuleId);
					if (resultHandleSuppFaxRule.isSuccess()) {
						model.addAttribute("suppFaxRule", resultHandleSuppFaxRule.getReturnContent());
					} else {
						LOG.info("method showManualSendOrderFax:findSuppFaxRuleById(ID=" + faxRuleId + ") is fail,msg=" + resultHandleSuppFaxRule.getMsg());
					}
				}
			}
			//model.addAttribute("orderAffirmTypeList", OrderEnum.ORDER_AFFIRM_TYPE.values());
		}
		model.addAttribute("faxFlag", faxFlag);
		
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
		String toFax=request.getParameter("toFax");
		String certifType=request.getParameter("certifType");
		String faxRemark=request.getParameter("messageContent");
		
		
		//OrdOrderItem orderItem=order.getMainOrderItem();
		
		String addition="";
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		OrdOrderItem orderItem=order.getMainOrderItem();
		String faxFlag=(String)orderItem.getContentMap().get(OrderEnum.ORDER_COMMON_TYPE.fax_flag.name());
		
		ResultHandle result=this.orderStatusManageService.manualSendOrderFax(order, toFax,faxRemark, assignor, EbkCertif.EBK_CERTIFICATE_TYPE.getCnName(certifType));
		if (result.isSuccess()) {
			if ("Y".equals(faxFlag)) {
				addition=certifType+"_"+toFax;
			}else if("N".equals(faxFlag)){
				addition=certifType;
				
			}
			orderLocalService.sendOrderSendFaxMsg(order,addition);
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
		
		ShipResultHandleT<ShipGoodsBaseVo> resultHandleSuppGoods = shipGoodsService.findGoodsDetail(orderItem.getSuppGoodsId());
		if (resultHandleSuppGoods.isSuccess() && resultHandleSuppGoods.getReturnContent() != null) {
			ShipGoodsBaseVo suppGoods = resultHandleSuppGoods.getReturnContent();
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
		
		OrdPerson ordPerson=orderUpdateService.findOrderPersonById(Long.valueOf(ordPersonId));
		
		model.addAttribute("ordPerson", ordPerson);
		
		return "/order/orderStatusManage/ship/updatePerson";
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

	
	@RequestMapping(value = "/showAddMessage")
	public String showAddMessage(Model model, HttpServletRequest request){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showAddMessage>");
		}
		
		model.addAttribute("auditTypeList", OrderEnum.AUDIT_TYPE.values());
		
		String messageObjectValue="";
		String orderId=request.getParameter("orderId");
		String orderItemId="";
		String orderType=request.getParameter("orderType");
		if ("parent".equals(orderType)) {
			
			messageObjectValue=orderId;
		}else{
			orderItemId=request.getParameter("orderItemId");
			messageObjectValue=orderItemId;
		}
		
		
		Map<String, String> messageObjectMap = new LinkedHashMap<String, String>();
		messageObjectMap.put(orderId+"-ORDER", "主订单-"+orderId);
		
		List<OrdOrderItem> orderItemsList = ordOrderUpdateService.queryOrderItemByOrderId(NumberUtils.toLong(orderId));
		for (OrdOrderItem ordOrderItem : orderItemsList) {
			
			messageObjectMap.put(ordOrderItem.getOrderItemId()+"-ORDER_ITEM", ordOrderItem.getProductName().trim()+"-"+ordOrderItem.getSuppGoodsName()+"-"+ordOrderItem.getOrderItemId());
			
		}
		
		model.addAttribute("messageObjectMap", messageObjectMap);
		
		model.addAttribute("messageObjectValue", messageObjectValue);
		
		return "/order/orderStatusManage/ship/addMessage";
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
			
			if ("ORDER".equals(orderType)) {//发送给主订单
				comMessageService.saveReservation(comMessage, auditType,
						orderId, assignor,orderRemark);
			}else{//发送给子订单
				comMessageService.saveReservationChildOrder(comMessage, auditType,
						orderId,objectId, assignor,orderRemark);
			}
			 
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
	public String findComMessageList(Model model,Long orderId,Integer page,HttpServletRequest req){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<findComMessageList>");
		}
	
		String orderType= HttpServletLocalThread.getRequest().getParameter("orderType");
		//String loginUserId=this.getLoginUserId();
		String objectType="";
		Long objectId=null;
		if ("parent".equals(orderType)) {
			objectId=orderId;
			objectType="ORDER";
		}else{

			String orderItemId=HttpServletLocalThread.getRequest().getParameter("orderItemId");
			objectId=NumberUtils.toLong(orderItemId);
			
			objectType="ORDER_ITEM";
		}
		Long[] auditIdArray = findBookingAuditIds(objectType,objectId);
		List<ComMessage> messageList=new ArrayList<ComMessage>();
		if (auditIdArray.length>0) {
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("messageStatus",OrderEnum.MESSAGE_STATUS.UNPROCESSED.getCode());
			parameters.put("auditIdArray",auditIdArray);
			
			//parameters.put("receiver",loginUserId);
			
			int count=comMessageService.findComMessageCount(parameters);
			int pagenum = page == null ? 1 : page;
			Page pageParam = Page.page(count, 10, pagenum);
			//pageParam.buildUrl(req);
			pageParam.buildJSONUrl(req);
			parameters.put("_start", pageParam.getStartRows());
			parameters.put("_end", pageParam.getEndRows());
			parameters.put("_orderby", "COM_MESSAGE.CREATE_TIME");
			parameters.put("_order", "DESC");
			
			
			messageList=comMessageService.findComMessageList(parameters);
			
		}
		
		model.addAttribute("messageList", messageList);
		
		/*pageParam.setItems(messageList);
		model.addAttribute("pageParam", pageParam);
		*/
		return "/order/orderStatusManage/findComMessageList";
		
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
		
		
		
		return "/order/orderStatusManage/ship/addCertificate";
	}	
	
	

	@RequestMapping(value = "/findTouristList")
	public String findTouristList(Model model, HttpServletRequest request,Long orderItemId){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<findTouristList>");
		}
		String branchName  = "";
		
		OrdOrderItem orderItemObj=ordOrderUpdateService.getOrderItem(orderItemId);
		Map<String,Object> contentMap = orderItemObj.getContentMap();
		
		ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCode());
		BizCategory bizCategoryShip=result.getReturnContent();
		
		ResultHandleT<BizCategory> resultVisa=categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCode());
		BizCategory bizCategoryVisa=resultVisa.getReturnContent();
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", orderItemId); 
		List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(params);
		
		List<OrdPerson> personList =new ArrayList<OrdPerson>();
		Map<String, Map<String, List<OrdPerson>>> travellerMap = new HashMap<String, Map<String,List<OrdPerson>>>();
		
		if (orderItemObj.getCategoryId().equals(bizCategoryShip.getCategoryId())||orderItemObj.getCategoryId().equals(bizCategoryVisa.getCategoryId())) {//油轮 签证 取规格
			for (OrdItemPersonRelation ordItemPersonRelation : ordItemPersonRelationList) {
				OrdPerson ordPerson=ordPersonService.findOrdPersonById(ordItemPersonRelation.getOrdPersonId());
				ordPerson.setIdTypeName(OrderEnum.ORDER_PERSON_ID_TYPE.getCnName(ordPerson.getIdType()));
				branchName  =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());
				ordPerson.setCheckInRoomName(branchName);//入住房间
				personList.add(ordPerson);
				
				Long suppGoodsId = orderItemObj.getSuppGoodsId();
				if(suppGoodsId == null){
					suppGoodsId = Long.valueOf(0);
				}
				Long roomNo= ordItemPersonRelation.getRoomNo();
				if(roomNo == null){
					roomNo = Long.valueOf(0);
				}				
				Map<String, List<OrdPerson>> map1 = travellerMap.get(suppGoodsId.toString());
				if(map1 == null){
					map1 = new HashMap<String, List<OrdPerson>>();
					travellerMap.put(suppGoodsId.toString(), map1);
					List<OrdPerson> persons = new ArrayList<OrdPerson>();
					persons.add(ordPerson);
					map1.put(roomNo.toString(), persons);
				}else{
					List<OrdPerson> persons = map1.get(roomNo.toString());
					if(persons == null){
						persons = new ArrayList<OrdPerson>();
						persons.add(ordPerson);
						map1.put(roomNo.toString(), persons);
					}else{
						persons.add(ordPerson);
					}
				}				
			}
		}else if (orderItemObj.getCategoryId().longValue() != bizCategoryShip.getCategoryId().longValue()) {//非邮轮
			
			List<OrdOrderItem> ordItemList=ordOrderUpdateService.queryOrderItemByOrderId(orderItemObj.getOrderId());
			
			//取到所有邮轮子项
			List<Long> itemIdList=new ArrayList<Long>();
			for (int i = 0; i < ordItemList.size(); i++) {
				OrdOrderItem orderItem=ordItemList.get(i);
				if (orderItem.getCategoryId()==bizCategoryShip.getCategoryId()){
					itemIdList.add(orderItem.getOrderItemId());
				}
			}
			
			//取到当前非邮轮子项的游玩人
			for (OrdItemPersonRelation ordItemPersonRelation : ordItemPersonRelationList) {
				OrdPerson ordPerson=ordPersonService.findOrdPersonById(ordItemPersonRelation.getOrdPersonId());
				personList.add(ordPerson);
			
			}
			
			//游客列表展示
			for (OrdPerson ordPerson : personList) {
				ordPerson.setIdTypeName(OrderEnum.ORDER_PERSON_ID_TYPE.getCnName(ordPerson.getIdType()));
				
				//取到当前非邮轮子项游玩人对应的邮轮子项关系
				Map<String, Object> paramsOrdPerson = new HashMap<String, Object>();
				paramsOrdPerson.put("ordPersonId", ordPerson.getOrdPersonId());
				paramsOrdPerson.put("orderItemIdArray",itemIdList.toArray() ); 
				List<OrdItemPersonRelation> ordItemShipPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(paramsOrdPerson);
				
				if (!ordItemShipPersonRelationList.isEmpty()) {
					OrdItemPersonRelation ordItemPersonRelation=ordItemShipPersonRelationList.get(0);
					
					OrdOrderItem orderItemShip=ordOrderUpdateService.getOrderItem(ordItemPersonRelation.getOrderItemId());
					Map<String,Object> contentMapShip = orderItemShip.getContentMap();
					
					branchName =  (String) contentMapShip.get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());
					ordPerson.setCheckInRoomName(branchName);//入住房间
					
					Long suppGoodsId = orderItemShip.getSuppGoodsId();
					if(suppGoodsId == null){
						suppGoodsId = Long.valueOf(0);
					}
					Long roomNo= ordItemPersonRelation.getRoomNo();
					if(roomNo == null){
						roomNo = Long.valueOf(0);
					}				
					Map<String, List<OrdPerson>> map1 = travellerMap.get(suppGoodsId.toString());
					if(map1 == null){
						map1 = new HashMap<String, List<OrdPerson>>();
						travellerMap.put(suppGoodsId.toString(), map1);
						List<OrdPerson> persons = new ArrayList<OrdPerson>();
						persons.add(ordPerson);
						map1.put(roomNo.toString(), persons);
					}else{
						List<OrdPerson> persons = map1.get(roomNo.toString());
						if(persons == null){
							persons = new ArrayList<OrdPerson>();
							persons.add(ordPerson);
							map1.put(roomNo.toString(), persons);
						}else{
							persons.add(ordPerson);
						}
					}						
				}

			}
		}
		model.addAttribute("personMap", travellerMap);
		String categoryCode =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
		model.addAttribute("categoryCode",categoryCode);
		return "/order/orderStatusManage/ship/findTouristList";
	}	
	
	
	

	@RequestMapping(value = "/findOrderSuppGoodsInfo")
	public String findOrderSuppGoodsInfo(Model model, HttpServletRequest request,Long orderItemId){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<findOrderSuppGoodsInfo>");
		}
		
		OrdOrderItem orderItem=ordOrderUpdateService.getOrderItem(orderItemId);
		
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderItem.getOrderId());
		
		model.addAttribute("order",order );
		model.addAttribute("orderItem",orderItem );
		
		
		
		ResultHandleT<SuppSupplier> resultHandleSuppSupplier = suppSupplierClientService.findSuppSupplierById(orderItem.getSupplierId());
		if (resultHandleSuppSupplier.isSuccess()) {
			SuppSupplier  suppSupplier= resultHandleSuppSupplier.getReturnContent();
			model.addAttribute("suppSupplier", suppSupplier);
		} else {
			LOG.info("method:showOrderStatusManage,resultHandleSuppSupplier.isFial,msg=" + resultHandleSuppSupplier.getMsg());
		}
		

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("orderItemId", orderItemId); 
		List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(params);
		
		
		StringBuffer tnSB = new StringBuffer();
		List<OrdPerson> personList =new ArrayList<OrdPerson>();
		for (OrdItemPersonRelation ordItemPersonRelation : ordItemPersonRelationList) {
			
			
			OrdPerson ordPerson=ordPersonService.findOrdPersonById(ordItemPersonRelation.getOrdPersonId());
			
			personList.add(ordPerson);
			
//			if (travellerName.length() > 0) {
//				travellerName += ",";
//			}
			if(tnSB.length() > 0) {
				tnSB.append(",");
			}
			tnSB.append(ordPerson.getFullName());
//			travellerName += ordPerson.getFullName();
		}
		String travellerName= tnSB.toString();
		model.addAttribute("travellerName", travellerName);
		model.addAttribute("travellerNum", personList.size());

		OrdOrderPack ordOrderPack=new OrdOrderPack();
		Map<String, Object> paramPack = new HashMap<String, Object>();
		paramPack.put("orderId", order.getOrderId());//订单号
		List<OrdOrderPack> orderPackList=ordOrderPackService.findOrdOrderPackList(paramPack);
		if (!orderPackList.isEmpty()) {
			ordOrderPack=orderPackList.get(0);
		}
		model.addAttribute("ordOrderPack",ordOrderPack);
		
		Map<String,Object> orderPackContentMap = ordOrderPack.getContentMap();
		String endSailingDate =  (String) orderPackContentMap.get(OrderEnum.ORDER_PACK_TYPE.end_sailing_date.name());
		
		model.addAttribute("endSailingDate",endSailingDate);
		if(!StringUtils.isEmpty(endSailingDate)){
			model.addAttribute("arrivalDays", 1+CalendarUtils.getDayCounts(orderItem.getVisitTime(), DateUtil.toDate(endSailingDate, "yyyy-MM-dd")));
		}
		
		
		Map<String,Object> contentMap = orderItem.getContentMap();
		String categoryCode =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
		model.addAttribute("categoryCode",categoryCode);
		
		//驴妈妈价
		StringBuffer lvmamaPrice=new StringBuffer();
		if (BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCode().equals(categoryCode)) {
			
			
		/*	String[] priceTypeArray = new String[] {
					,
					ORDER_PRICE_RATE_TYPE.PRICE_ADULT_34.getCode(),
					ORDER_PRICE_RATE_TYPE.PRICE_CHILD_34.getCode() };*/

			Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
			paramsMulPriceRate.put("orderItemId", orderItemId); 
			paramsMulPriceRate.put("priceType",ORDER_PRICE_RATE_TYPE.PRICE_ADULT_12.getCode() ); 
			
			List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
			if (!ordMulPriceRateList.isEmpty()) {
				
				OrdMulPriceRate ordMulPriceRate =ordMulPriceRateList.get(0);
				lvmamaPrice.append("第1、2人价格：").append(ordMulPriceRate.getPrice()/100.0).append("元");
				
			}
			lvmamaPrice.append("</br>");
			
			paramsMulPriceRate.put("priceType",ORDER_PRICE_RATE_TYPE.PRICE_ADULT_34.getCode() ); 
			ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
			if (!ordMulPriceRateList.isEmpty()) {
				
				OrdMulPriceRate ordMulPriceRate =ordMulPriceRateList.get(0);
				lvmamaPrice.append("第3、4人价格：").append(ordMulPriceRate.getPrice()/100.0).append("元");
				
			}else{
				lvmamaPrice.append("第3、4人价格：").append("——");
			}

			lvmamaPrice.append("</br>");
			
			paramsMulPriceRate.put("priceType",ORDER_PRICE_RATE_TYPE.PRICE_CHILD_34.getCode() ); 
			ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
			if (!ordMulPriceRateList.isEmpty()) {
				
				OrdMulPriceRate ordMulPriceRate =ordMulPriceRateList.get(0);
				lvmamaPrice.append("第3、4人儿童价格：").append(ordMulPriceRate.getPrice()/100.0).append("元");
				
			}else{
				lvmamaPrice.append("第3、4人儿童价格：").append("——");
			}
			
			lvmamaPrice.append("</br>");
			
			paramsMulPriceRate.put("priceType",ORDER_PRICE_RATE_TYPE.PRICE_GAP.getCode()); 
			ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
			if (!ordMulPriceRateList.isEmpty()) {
				
				OrdMulPriceRate ordMulPriceRate =ordMulPriceRateList.get(0);
				lvmamaPrice.append("床位费价格：").append(ordMulPriceRate.getPrice()/100.0).append("元");
				
			}else{
				lvmamaPrice.append("床位费价格：").append("——");
			}
					
			
		}else if (BizEnum.BIZ_CATEGORY_TYPE.category_sightseeing.getCode().equals(categoryCode)
				|| BizEnum.BIZ_CATEGORY_TYPE.category_cruise_addition.getCode().equals(categoryCode)) {
			
			Map<String, Object> paramsMulPriceRate = new HashMap<String, Object>();
			paramsMulPriceRate.put("orderItemId", orderItemId); 
			paramsMulPriceRate.put("priceType",ORDER_PRICE_RATE_TYPE.PRICE_ADULT.getCode() ); 
			
			List<OrdMulPriceRate> ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
			if (!ordMulPriceRateList.isEmpty()) {
				
				OrdMulPriceRate ordMulPriceRate =ordMulPriceRateList.get(0);
				lvmamaPrice.append("成人价：").append(ordMulPriceRate.getPrice()/100.0).append("元");
				
			}
			lvmamaPrice.append("</br>");
			
			paramsMulPriceRate.put("priceType",ORDER_PRICE_RATE_TYPE.PRICE_CHILD.getCode() ); 
			ordMulPriceRateList=ordMulPriceRateService.findOrdMulPriceRateList(paramsMulPriceRate);
			if (!ordMulPriceRateList.isEmpty()) {
				
				OrdMulPriceRate ordMulPriceRate =ordMulPriceRateList.get(0);
				lvmamaPrice.append("儿童价：").append(ordMulPriceRate.getPrice()/100.0).append("元");
				
			}else{
				lvmamaPrice.append("儿童价：").append("——");
			}
			
			
		}else if (BizEnum.BIZ_CATEGORY_TYPE.category_visa.getCode().equals(categoryCode)) {
			
			lvmamaPrice.append("销售价：").append(orderItem.getPrice()/100.0).append("元");
		}else if (BizEnum.BIZ_CATEGORY_TYPE.category_insurance.getCode().equals(categoryCode)) {
			
			lvmamaPrice.append("销售价：").append(orderItem.getPrice()/100.0).append("元");
		}

		//非酒店子单计算分摊信息
		OrderItemApportionInfoPO orderItemApportionInfoPO = null;
		if (ApportionUtil.isApportionEnabled()) {
			orderItemApportionInfoPO = orderDetailApportionService.generateItemApportionInfoPO4Detail(orderItem);
		}
		model.addAttribute("orderItemApportionInfo", orderItemApportionInfoPO);
		model.addAttribute("lvmamaPrice",lvmamaPrice.toString());
		
		
		return "/order/orderStatusManage/ship/findOrderSuppGoodsInfo";
	}	


	@RequestMapping(value = "/findOrderBaseInfo")
	public String findOrderBaseInfo(Model model, HttpServletRequest request,Long orderItemId){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<findOrderBaseInfo>");
		}
		
		OrdOrderItem orderItem=ordOrderUpdateService.getOrderItem(orderItemId);
		
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderItem.getOrderId());
		
		model.addAttribute("order",order );
		model.addAttribute("orderItem",orderItem );
		
		
		if(order.getLastCancelTime()!=null){
			Date now=new Date();
			Date lastCancelTime=order.getLastCancelTime();
			model.addAttribute("isGreaterNow", now.compareTo(lastCancelTime));
		}
		

		//下单渠道
		Distributor distributor = distributorClientService.findDistributorById(order.getDistributorId()).getReturnContent();
				
		model.addAttribute("distributorName", distributor.getDistributorName());
		
		if(order.getDistributionChannel() != null) {
			ResultHandleT<DistributorInfo> distributorInfoResult = tntDistributorServiceRemote.getDistributorById(order.getDistributionChannel());
			if (distributorInfoResult.isFail() || distributorInfoResult.getReturnContent() == null) {
				LOG.error("获取渠道代码" + order.getDistributionChannel() + "对应的渠道信息失败");
			}
			if(distributorInfoResult.getReturnContent() != null) {
				model.addAttribute("distributionChannelName", distributorInfoResult.getReturnContent().getDistributorName());
			}
		}		
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
		if (!StringUtils.isEmpty(cancelStrategyType)) {
			model.addAttribute("cancelStrategyTypeStr", SuppGoodsTimePrice.CANCELSTRATEGYTYPE.getCnName(cancelStrategyType));
		}
		/*
		//扣款类型
		String deductAmountStr="";
		Long deductAmount=orderItem.getDeductAmount();
		if (deductAmount!=null) {
			deductAmountStr=deductAmount/100.0+"";
		}else{
			deductAmountStr="0";
		}
		model.addAttribute("deductAmountStr", deductAmountStr);
		
		
		String deductTypeStr="";
		String deductType=orderItem.getDeductType();
		if (!StringUtils.isEmpty(deductType)) {
			deductTypeStr=SuppGoodsTimePrice.DEDUCTTYPE.getCnName(orderItem.getDeductType());
		}
		model.addAttribute("deductTypeStr", deductTypeStr);
		
		*/
		
		
		return "/order/orderStatusManage/ship/findOrderBaseInfo";
	}	
	


	@RequestMapping(value = "/findOrdPersonBooker")
	public String findOrdPersonBooker(Model model, HttpServletRequest request,Long orderItemId){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<findOrdPersonBooker>");
		}
		
		OrdOrderItem orderItem=ordOrderUpdateService.getOrderItem(orderItemId);
		
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderItem.getOrderId());

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
		try {
			String id = String.valueOf(order.getUserNo());
			Map<String, Object> map = csVipDubboService.getCsVipByCondition(id, 
					CsVipUserIdentityTypeEnum.USER_ID);
			isCsVip = (Boolean) map.get("isCsVip");
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		
		OrdPerson ordPersonContact = new OrdPerson();
		OrdPerson ordPersonBooker = new OrdPerson();
		
		//游客列表展示
		for (OrdPerson ordPerson : order.getOrdPersonList()) {

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
				/*if (travellerName.length() > 0) {
					travellerName += ",";
				}
				travellerName += ordPerson.getFullName();
				travellerNum+=1;*/
			}

		}

		model.addAttribute("userSuperVip",userSuperVip);

		model.addAttribute("isCsVip", isCsVip);
		model.addAttribute("ordPersonContact", ordPersonContact);
		model.addAttribute("ordPersonBooker", ordPersonBooker);
		
		
		model.addAttribute("order",order );
		model.addAttribute("orderItem",orderItem );
		
		

		
		
		
		return "/order/orderStatusManage/ship/findOrdPersonBooker";
	}	
	
	
	
	@RequestMapping(value = "/showUpdateTourist")
	public String showUpdateTourist(Model model, HttpServletRequest request,Long orderId){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showUpdateTourist>");
		}
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		//游玩人是否后置
		model.addAttribute("travellerDelayFlag", order.getTravellerDelayFlag());
		//model.addAttribute("travellerDelayFlag", "Y");
		//游玩人是否锁定
		model.addAttribute("travellerLockFlag", order.getTravellerLockFlag());
		//model.addAttribute("travellerLockFlag", "Y");
		
		List<OrdPerson> personList = order.getOrdPersonList();
		List<OrdPerson> travellerList = new ArrayList<OrdPerson>();
		Map<String, Map<String, List<OrdPerson>>> travellerMap = new HashMap<String, Map<String,List<OrdPerson>>>();
		//游客列表展示
		for (OrdPerson ordPerson : personList) {
			ordPerson.setIdTypeName(OrderEnum.ORDER_PERSON_ID_TYPE.getCnName(ordPerson.getIdType()));
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("ordPersonId", ordPerson.getOrdPersonId()); 
			List<OrdItemPersonRelation> ordItemPersonRelationList=ordItemPersonRelationService.findOrdItemPersonRelationList(params);
			
			if (!ordItemPersonRelationList.isEmpty()) {
				OrdOrderItem orderShipItem=null;
				for (OrdItemPersonRelation ordItemPersonRelation : ordItemPersonRelationList) {
					OrdOrderItem orderItemObj=ordOrderUpdateService.getOrderItem(ordItemPersonRelation.getOrderItemId());
					String  categoryCode=orderItemObj.getContentStringByKey(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name());
					if (BIZ_CATEGORY_TYPE.category_cruise.getCode().equals(categoryCode)) {
						orderShipItem=orderItemObj;
						Long suppGoodsId = orderShipItem.getSuppGoodsId();
						if(suppGoodsId == null){
							suppGoodsId = Long.valueOf(0);
						}
						Long roomNo= ordItemPersonRelation.getRoomNo();
						if(roomNo == null){
							roomNo = Long.valueOf(0);
						}
						Map<String, List<OrdPerson>> map1 = travellerMap.get(suppGoodsId.toString());
						if(map1 == null){
							map1 = new HashMap<String, List<OrdPerson>>();
							travellerMap.put(suppGoodsId.toString(), map1);
							List<OrdPerson> persons = new ArrayList<OrdPerson>();
							persons.add(ordPerson);
							map1.put(roomNo.toString(), persons);
						}else{
							List<OrdPerson> persons = map1.get(roomNo.toString());
							if(persons == null){
								persons = new ArrayList<OrdPerson>();
								persons.add(ordPerson);
								map1.put(roomNo.toString(), persons);
							}else{
								persons.add(ordPerson);
							}
						}						
						break;
					}
				}
				Map<String,Object> contentMap = orderShipItem == null ? new HashMap<String, Object>() : orderShipItem.getContentMap();
				String branchName =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());
				ordPerson.setCheckInRoomName(branchName);//入住房间
				ordPerson.setOrderItemId(orderShipItem.getOrderItemId());
			}
			if (OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name().equals(ordPerson.getPersonType())) {
				travellerList.add(ordPerson);
			}
		}
		Map<String, String> roomNameMap = new HashMap<String, String>();
		List<OrdOrderItem> orderItemList=ordOrderUpdateService.queryOrderItemByOrderId(orderId);
		for (OrdOrderItem ordOrderItem : orderItemList) {
			ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCode());
			BizCategory bizCategory=result.getReturnContent();
			if (ordOrderItem.getCategoryId().longValue() ==bizCategory.getCategoryId().longValue()) {//邮轮
				Map<String,Object> contentMap = ordOrderItem.getContentMap();
				String branchName =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());
				roomNameMap.put(ordOrderItem.getOrderItemId()+"", branchName);
			}
		}
		model.addAttribute("personMap", travellerMap);
		model.addAttribute("roomNameMap", roomNameMap);
		model.addAttribute("idTypeList", OrderEnum.ORDER_PERSON_ID_TYPE.values());
		model.addAttribute("peopleTypeList", OrderEnum.ORDER_PERSON_PEOPLE_TYPE.values());
		model.addAttribute("genderTypeList", OrderEnum.ORDER_PERSON_GENDER_TYPE.values());
		model.addAttribute("ordItemPersonRelation", new OrderShipDetailAction());
		return "/order/orderStatusManage/ship/showUpdateTourist";
	}	
	
	/**
	 * 编辑紧急联系人
	 * @param model
	 * @param request
	 * @param orderId
	 * @return
	 */
	@RequestMapping(value = "/showUpdatePersonEmergency")
	public String showUpdatePersonEmergency(Model model, HttpServletRequest request,Long orderId){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showUpdatePersonEmergency>");
		}
		List<OrdPerson> ordPersonEmergencyList = new ArrayList<OrdPerson>(); // 紧急联系人列表
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		List<OrdPerson> personList = order.getOrdPersonList();
		for (OrdPerson ordPerson : personList) {
			String personType = ordPerson.getPersonType();
			if (OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name().equals(personType)) {
				ordPersonEmergencyList.add(ordPerson);
			}
		}
		model.addAttribute("ordPersonEmergencyList", ordPersonEmergencyList);
		
		return "/order/orderStatusManage/ship/showUpdatePersonEmergency";
	}
	
	/**
	 * 修改紧急联系人
	 * @param request
	 * @param ordItemPersonRelation
	 * @return
	 */
	@RequestMapping(value = "/updateOrdPersonEmergency")
	@ResponseBody
	public Object updateOrdPersonEmergency( HttpServletRequest request,OrdItemPersonRelation ordItemPersonRelation,Long orderId){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<updateOrdPersonEmergency>");
		}
		OrdOrder ordOrder = complexQueryService.queryOrderByOrderId(orderId);
		List<OrdPerson> ordPersonList = ordItemPersonRelation.getOrdPersonList();
		for (OrdPerson ordPerson : ordPersonList) {
			this.ordPersonService.updateByPrimaryKeySelective(ordPerson);
		}
		Map<String, String> logMap = getChangeLogMap(ordOrder, ordPersonList, true);
		
		if(logMap.get("remark") != null && logMap.get("remark").length() > 0) {
		//添加日志
		lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
				orderId, 
				orderId, 
				this.getLoginUserId(), 
				"将编号为["+orderId+"]的订单，修改游客信息", 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
				ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"[修改游客信息-"+logMap.get("infoType")+"]",
				logMap.get("remark"));
		}
		return ResultMessage.UPDATE_SUCCESS_RESULT;
	}
	
	@RequestMapping(value = "/updateTourist")
	@ResponseBody
	public Object updateTourist( HttpServletRequest request,OrdOrder order,OrdItemPersonRelation ordItemPersonRelation){
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<updateTourist>");
		}
		Long orderId=order.getOrderId();
		OrdOrder ordOrder = complexQueryService.queryOrderByOrderId(orderId);
		Map<Long, Integer> oldMap = new HashMap<Long, Integer>();
		List<OrdOrderItem> orderItemList=ordOrderUpdateService.queryOrderItemByOrderId(orderId);
		for (OrdOrderItem ordOrderItem : orderItemList) {
			ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCode());
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
				String branchName =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());
				result.setMsg(branchName+" 新的房间数量:"+newNum+"和原有房间数量"+oldNum+",不一致 ");
				break;
			}
        } 
		if (result.isFail()) {
			return new ResultMessage(ResultMessage.ERROR, result.getMsg());
		}
		
		int num = 0;
		String trallverLog ="";
		StringBuffer travellerPerson = new StringBuffer(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
		for (OrdPerson ordPerson : ordPersonList) {
			OrdPerson oldOrdPerson =ordPersonService.findOrdPersonById(ordPerson.getOrdPersonId());
			num = this.ordPersonService.updateByPrimaryKeySelective(ordPerson);
			if(num>0){
				trallverLog=getTrallverLog(trallverLog,ordPerson, oldOrdPerson);
			}
			travellerPerson.append("_");
			travellerPerson.append(ordPerson.getOrdPersonId());
		}
		
		if(num > 0){
			
			travellerPerson.append(",");
			travellerPerson.setLength(travellerPerson.length()-1);
			String travellerLockFlag =request.getParameter("travellerLockFlag");
			if(null!=travellerLockFlag&&travellerLockFlag.equals("true")){
				if(order.getTravellerLockFlag().equals("Y")){
					orderMessageProducer.sendMsg(MessageFactory.newOrderModifyPersonMessage(orderId, travellerPerson.toString()));
				}else{
					//改变订单游玩人锁定状态
					ordOrderTravellerService.updateOrderLockTraveller(orderId);
					//推动工作流
					orderLocalService.travellerLockAudit(orderId, getLoginUserId());
				}
//				发送邮轮电子合同
				orderEcontractGeneratorService.generateEcontract(orderId, this.getLoginUserId());
				//发送短信，需要有合同编号，先生成合同再发送
				if("Y".equals(ordOrder.getTravellerDelayFlag())){
					LOG.info("OrderPaymentSms:orderId:"+ordOrder.getOrderId()+"===>enter=message=flag=order.getCategoryId()");
					if(BIZ_CATEGORY_TYPE.category_route_group.getCategoryId().longValue()==ordOrder.getCategoryId().longValue()
							||BIZ_CATEGORY_TYPE.category_comb_cruise.getCategoryId().longValue()==ordOrder.getCategoryId().longValue()
							||BIZ_CATEGORY_TYPE.category_route_local.getCategoryId().longValue()==ordOrder.getCategoryId().longValue()
							||(order.getSubCategoryId() != null && BIZ_CATEGORY_TYPE.category_route_flight_hotel.getCategoryId().longValue()==ordOrder.getSubCategoryId().longValue()&&!ordOrder.isContainApiFlightTicket())
							||(order.getSubCategoryId() != null && BIZ_CATEGORY_TYPE.category_route_traffic_service.getCategoryId().longValue()==ordOrder.getSubCategoryId().longValue())){
							//确定游玩人后置订单锁定模板
							LOG.info("OrderPaymentSms:orderId:"+ordOrder.getOrderId()+"===>>PAY_PAYED_DELAY_TRAVELLER_CONFIRM==游玩人后置订未锁定模板===");
							iOrderSendSmsService.sendSms(ordOrder.getOrderId(), OrdSmsTemplate.SEND_NODE.PAY_PAYED_DELAY_TRAVELLER_CONFIRM);
							LOG.info("OrderPaymentSms:orderId:"+ordOrder.getOrderId()+"===>>PAY_PAYED_DELAY_TRAVELLER_CONFIRM==游玩人后置订单锁定模板=end==");
					}
				}
				
			}else{
				orderMessageProducer.sendMsg(MessageFactory.newOrderModifyPersonMessage(orderId, travellerPerson.toString()));
			}
		}
		
		Map<String, String> logMap = getChangeLogMap(ordOrder, ordPersonList, false);
		if(logMap.get("remark") != null) {
			//添加日志
			if(!trallverLog.equals("")){
				lvmmLogClientService.sendLog(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
						orderId, 
						orderId, 
						this.getLoginUserId(), 
						trallverLog, 
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.name(), 
						ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_STATUS_CHANGE.getCnName()+"[修改游客信息-"+logMap.get("infoType")+"]",
						"");
			}
			
		}
		return ResultMessage.UPDATE_SUCCESS_RESULT;
		
	}
	
	/**
	 * 获取游客日志
	 * @param ordOrder
	 * @param ordPersonList
	 * @return
	 */
	private Map<String, String> getChangeLogMap(OrdOrder ordOrder, List<OrdPerson> ordPersonList, boolean isEmergency) {
		Map<String, String> logMap = new HashMap<String, String>();
		Map<Long, OrdPerson> ordPersonMap = new HashMap<Long, OrdPerson>();
		for(OrdPerson ordPerson : ordOrder.getOrdPersonList()) {
			ordPersonMap.put(ordPerson.getOrdPersonId(), ordPerson);
		}
		String infoType = "";
		String travellerRemark = "";
		if(CollectionUtils.isNotEmpty(ordPersonList)) {
			for(int i = 0; i < ordPersonList.size(); i++) {
				StringBuffer travellerStr = new StringBuffer();
				OrdPerson newTraveller = ordPersonList.get(i);
				OrdPerson oldTraveller = ordPersonMap.get(newTraveller.getOrdPersonId());
				travellerStr.append(ComLogUtil.getChangeLog("中文姓名", oldTraveller.getFullName(), newTraveller.getFullName()));
				travellerStr.append(ComLogUtil.getChangeLog("手机号", oldTraveller.getMobile(), newTraveller.getMobile()));
				
				if(!isEmergency) {
					travellerStr.append(ComLogUtil.getChangeLog("英文姓", oldTraveller.getLastName(), newTraveller.getLastName()));
					travellerStr.append(ComLogUtil.getChangeLog("英文名", oldTraveller.getFirstName(), newTraveller.getFirstName()));
					travellerStr.append(ComLogUtil.getChangeLog("性别", oldTraveller.getGenderName(), newTraveller.getGenderName()));
					travellerStr.append(ComLogUtil.getChangeLog("出生地", oldTraveller.getBirthPlace(), newTraveller.getBirthPlace()));
					travellerStr.append(ComLogUtil.getChangeLog("出生日期", DateUtil.formatDate(oldTraveller.getBirthday(), "yyyy-MM-dd"), DateUtil.formatDate(newTraveller.getBirthday(), "yyyy-MM-dd")));
					travellerStr.append(ComLogUtil.getChangeLog("人群", ORDER_PERSON_PEOPLE_TYPE.getCnName(oldTraveller.getPeopleType()), ORDER_PERSON_PEOPLE_TYPE.getCnName(newTraveller.getPeopleType())));
					travellerStr.append(ComLogUtil.getChangeLog("证件类型", ORDER_PERSON_ID_TYPE.getCnName(oldTraveller.getIdType()), ORDER_PERSON_ID_TYPE.getCnName(newTraveller.getIdType())));
					travellerStr.append(ComLogUtil.getChangeLog("证件号码", oldTraveller.getIdNo(), newTraveller.getIdNo()));
					travellerStr.append(ComLogUtil.getChangeLog("签发地", oldTraveller.getIssued(), newTraveller.getIssued()));
					travellerStr.append(ComLogUtil.getChangeLog("签发日期", DateUtil.formatDate(oldTraveller.getIssueDate(), "yyyy-MM-dd"), DateUtil.formatDate(newTraveller.getIssueDate(), "yyyy-MM-dd")));
					travellerStr.append(ComLogUtil.getChangeLog("有效日期", DateUtil.formatDate(oldTraveller.getExpDate(), "yyyy-MM-dd"), DateUtil.formatDate(newTraveller.getExpDate(), "yyyy-MM-dd")));
					if(travellerStr.length() > 0) {
						if(i == 0) {
							if(!infoType.contains(newTraveller.getCheckInRoomName())) {
								infoType += newTraveller.getCheckInRoomName();
							}
						} else {
							if(!infoType.contains(newTraveller.getCheckInRoomName())) {
								infoType += "&" + newTraveller.getCheckInRoomName();
							}
						}
						travellerRemark += newTraveller.getCheckInRoomName() + "-入住人" + newTraveller.getOrderRemark() + " : " + travellerStr.substring(1) + "<br/>";
					}
				} else {
					infoType = "紧急联系人";
					if(travellerStr.length() > 0) {
						travellerRemark += travellerStr.substring(1) + "<br/>";
					}
				}
			}
		}
		logMap.put("infoType", infoType);
		logMap.put("remark", travellerRemark);
		return logMap;
	}

	/**
	 * @author chenguangyao
	 * 拼装游玩人修改信息 新旧值对比
	 * @param 
	 * newOrdPerson 修改后游玩人
	 * oldOrdPerson 修改前游玩人
	 * */
	private String getTrallverLog(String oldLog,OrdPerson newOrdPerson,OrdPerson oldOrdPerson){
		
		StringBuffer logContent= new StringBuffer("");
		logContent.append("修改了游玩人信息(旧：中文名/"+(oldOrdPerson.getFullName()==null?"无":oldOrdPerson.getFullName()));
		logContent.append(",英文名/"+(oldOrdPerson.getLastName()==null?"无":oldOrdPerson.getLastName())+"."+(oldOrdPerson.getFirstName()==null?"无":oldOrdPerson.getFirstName()));
		logContent.append(",手机号码/"+(oldOrdPerson.getMobile()==null?"无":oldOrdPerson.getMobile()));
		logContent.append(",人群/"+(oldOrdPerson.getPeopleType()==null?"无":oldOrdPerson.getPeopleTypeName()));
		logContent.append(",证件类型/"+(oldOrdPerson.getIdType()==null?"无":oldOrdPerson.getIdTypeName()));
		logContent.append(",证件号码/"+(oldOrdPerson.getIdNo()==null?"无":oldOrdPerson.getIdNo()));
		if(oldOrdPerson.getBirthday()!=null){
			logContent.append(",出生日期/"+DateUtil.SimpleFormatDateToString(oldOrdPerson.getBirthday()));
		}else{
			logContent.append(",出生日期/无");
		}
		logContent.append(",性别/"+(oldOrdPerson.getGenderName()==null?"无":oldOrdPerson.getGenderName())+";");
		
		
		logContent.append("新：中文名/"+(newOrdPerson.getFullName()==null?"无":newOrdPerson.getFullName()));
		logContent.append(",英文名/"+(newOrdPerson.getLastName()==null?"无":newOrdPerson.getLastName())+"."+(newOrdPerson.getFirstName()==null?"无":newOrdPerson.getFirstName()));
		logContent.append(",手机号码/"+(newOrdPerson.getMobile()==null?"无":newOrdPerson.getMobile()));
		logContent.append(",人群/"+(newOrdPerson.getPeopleType()==null?"无":newOrdPerson.getPeopleTypeName()));
		logContent.append(",证件类型/"+(newOrdPerson.getIdType()==null?"无":newOrdPerson.getIdTypeName()));
		logContent.append(",证件号码/"+(newOrdPerson.getIdNo()==null?"无":newOrdPerson.getIdNo()));
		if(newOrdPerson.getBirthday()!=null){
			logContent.append(",出生日期/"+DateUtil.SimpleFormatDateToString(newOrdPerson.getBirthday()));
		}else{
			logContent.append(",出生日期/无");
		}
		logContent.append(",性别/"+(newOrdPerson.getGenderName()==null?"无":newOrdPerson.getGenderName()));
		logContent.append(")");
		
		return oldLog+logContent.toString();
	}
	
	
	@RequestMapping(value = "/showUpdateWaitPaymentTime")
	public String showUpdateWaitPaymentTime(Model model, HttpServletRequest request){
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<showUpdateWaitPaymentTime>");
		}
		return "/order/orderStatusManage/ship/showUpdateWaitPaymentTime";
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
		
		Date minDate=this.orderStatusManageService.getMinDate(orderId, lastCancelTime);
		
		if (minDate!=null && newWaitPaymentTime!=null && newWaitPaymentTime.after(minDate)) {
			String message=" 最晚支付等待时间最晚为（资源保留时间、最晚无损取消时间中最小的）："+DateUtil.formatDate(minDate, "yyyy-MM-dd HH:mm");
			return new ResultMessage(ResultMessage.ERROR,message);
		}
		/**
		 * 新增判断，如果修改过后的支付等待时间不能小于下单时间
		 * */
		if(newWaitPaymentTime != null && newWaitPaymentTime.before(oldOrder.getCreateTime())){
			String message=" 支付等待时间不得早于下单时间!";
			return new ResultMessage(ResultMessage.ERROR,message);
		}
		/**
		 * 支付等待时间不得晚于出发日期之后7天，包含第7天
		 * */
		if(newWaitPaymentTime != null && newWaitPaymentTime.after(DateUtil.dsDay_Date(oldOrder.getVisitTime(), 8))){
			String message=" 支付等待时间不得晚于出发日期之后7天!";
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
			
			
			return ResultMessage.UPDATE_SUCCESS_RESULT;
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
		
		
		String[] orderIds = request.getParameter("orderIds").split(",");
		
		if (orderIds.length>1) {
			model.addAttribute("SingleSelection", false);
		}else{
			model.addAttribute("SingleSelection", true);
			
			boolean categoryCruise=false;
			
			Long orderId= Long.valueOf(orderIds[0].split("_")[0]);
			
			OrdOrder order=this.complexQueryService.queryOrderByOrderId(orderId);
			
			OrdOrderItem orderItem=order.getMainOrderItem();
			
			ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_cruise.getCode());
			BizCategory bizCategoryShip=result.getReturnContent();
			Long categoryId=bizCategoryShip.getCategoryId();
			if (categoryId.equals(orderItem.getCategoryId())) {
				categoryCruise=true;
			}
			
			model.addAttribute("categoryCruise", categoryCruise);
		}
		
		
		
		
		
		return "/order/orderStatusManage/ship/uploadNoticeRegiment";
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
	 * 上传并且立即发送出团通知书
	 * @param model
	 * @throws BusinessException
	 */
	@RequestMapping("/uploadAndSendNoticeRegiment")
	public void uploadAndSendNoticeRegiment(Model model,OrderAttachmentVO orderAttachmentVO,HttpServletResponse response,HttpServletRequest req) throws BusinessException{
		//构造返回的json数据
		JSONObject jsonObject = new JSONObject();
		try {
			String[] orderIds = req.getParameter("orderIds").split(",");
			
			Long fileId = orderAttachmentVO.getFileId();
			String fileName = orderAttachmentVO.getFileName();
			String memo = orderAttachmentVO.getMemo();
			
			Long orderId= Long.valueOf(orderIds[0].split("_")[0]);
			String email=orderIds[0].split("_")[1];
			
			ordAdditionStatusService.addUploadAndSendNoticeRegiment(fileId, fileName, memo, orderId, email, this.getLoginUserId());
			
			
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
	 * 短信发送出团通知书
	 * @param model
	 * @param orderId
	 * @throws BusinessException
	 */
	@RequestMapping("/smsSendNoticeRegiment")
	public void smsSendNoticeRegiment(Model model,Long orderId,String smsContent,String mobile,HttpServletResponse response,HttpServletRequest req) throws BusinessException{
		//构造返回的json数据
		JSONObject jsonObject = new JSONObject();
		try {
			
			ordAdditionStatusService.addSMSNoticeRegiment(orderId, smsContent, mobile, this.getLoginUserId());
			
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


	
	/**
	 * 进入上传附件页面
	 * 
	 * @param model
	 * @param orderId
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/showSendSMSNoticeRegiment")
	public String showSendSMSNoticeRegiment(Model model, HttpServletRequest request,Long orderId) throws BusinessException{
		
		
		
		
		OrdOrder order=this.complexQueryService.queryOrderByOrderId(orderId);
		
		model.addAttribute("mobile", order.getContactPerson().getMobile());
		
		return "/order/orderStatusManage/ship/sendSMSNoticeRegiment";
	}
	
	@RequestMapping(value = "/fileDownLoad")
	public void fileDownLoad(Long orderId, HttpServletRequest request, HttpServletResponse response) {
			OutputStream os = null;
			try {
				
				os = response.getOutputStream();
				
				Map<String,Object> param = new HashMap<String,Object>();
				param.put("orderId",orderId);
				param.put("attachmentType",OrderEnum.ATTACHMENT_TYPE.NOTICE_REGIMENT.name());
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
	 * 进入有邮件模板页面
	 * 
	 * @param model
	 * @param orderId
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/showNoticeRegimentEmailTemplate")
	public String showNoticeRegimentEmailTemplate(Model model, HttpServletRequest request,Long orderId) throws BusinessException{
		
		
		OrdOrder order=this.complexQueryService.queryOrderByOrderId(orderId);
		
		File directioryFile = ResourceUtil.getResourceFile(TRAVEL_ECONTRACT_DIRECTORY);
		
		Map<String,Object> rootMap=noticeRegimentService.captureContract(null, order, directioryFile);
		
		
		
		model.addAttribute("travelContractVO", rootMap.get("travelContractVO"));
		model.addAttribute("order", rootMap.get("order"));
		model.addAttribute("routeDetailFormat", new RouteDetailFormat());

		model.addAttribute("IDTypeList", OrderEnum.ORDER_PERSON_ID_TYPE.values());
		
		List<OrdOrderItem>  orderItemList=order.getOrderItemList();
		
//		BizDict bizDict = dictClientService.findDictById(Long.valueOf(ordOrderGoods.getInternet())).getReturnContent();
		List<BizDict> hotelStarList=dictClientService.findDictListByDefId(515L).getReturnContent();;
		model.addAttribute("hotelStarList", hotelStarList);
		
		ResultHandleT<BizCategory> result=categoryClientService.findCategoryByCode(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCode());
		BizCategory bizCategory=result.getReturnContent();
		
		//入住酒店:自由行自主打包有酒店子订单抓取，其他不展示
		List<OrdOrderItem>  hotelOrderItemList=new ArrayList<OrdOrderItem>();
		if (order.getOrdOrderPack()!=null&&order.getOrdOrderPack().hasOwn()) {
			
			for (OrdOrderItem ordOrderItem : orderItemList) {
				
				if (bizCategory.getCategoryId().equals(ordOrderItem.getCategoryId())) {
					
					OrdOrderItem hoterlOrderItem=new OrdOrderItem();
					ProdProduct prodProduct=prodProductClientService.findProdProductById(ordOrderItem.getProductId(), Boolean.TRUE, Boolean.TRUE);
					Map<String, Object> propValue=prodProduct.getPropValue();
					
					BeanUtils.copyProperties(ordOrderItem, hoterlOrderItem);
					hoterlOrderItem.setDeductType((String)propValue.get("address"));
					hoterlOrderItem.setProductName(StringUtil.filterOutHTMLTags(hoterlOrderItem.getProductName()));
					hoterlOrderItem.setDeductType(StringUtil.filterOutHTMLTags(hoterlOrderItem.getDeductType()));
					hoterlOrderItem.setOrderMemo(StringUtil.filterOutHTMLTags(hoterlOrderItem.getOrderMemo()));
					hotelOrderItemList.add(hoterlOrderItem);
				}
				
			}
			
		}
//		model.addAttribute("hasHotel", hasHotel);
		model.addAttribute("hotelOrderItemList", hotelOrderItemList);
	
		
		return "/order/econtractTemplate/noticeRegimentTemplate";
	}
	
	@RequestMapping("/viewNoticeRegimentTemplate")
	public String viewNoticeRegimentTemplate(TravelContractVO travelContractVo, HttpServletRequest request) throws BusinessException{


		HttpServletLocalThread.getModel().addAttribute("travelContractVo", travelContractVo);
		
		return "/order/econtractTemplate/noticeRegimentTemplate";
	}	
	
	
	/**
	 * 生成pdf且发送出团通知书
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/saveNoticeRegiment")
	@ResponseBody
	public Object saveNoticeRegiment(TravelContractVO travelContractVo, HttpServletRequest request){
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<saveNoticeRegiment>");
		}
		
		LOG.info("saveNoticeRegiment agent is"+travelContractVo.getAgent());
		ResultHandle resultHandle= noticeRegimentService.saveNoticeRegiment(travelContractVo, this.getLoginUserId());
		if (resultHandle.isFail()) {
			return new ResultMessage("ERROR", resultHandle.getMsg());
		}
		
		return new ResultMessage("success", "生成且发送成功");
		
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
	public String showUpdateTravelContract(Model model, HttpServletRequest request,Long orderId) throws BusinessException{
		
		ResultHandle resultHandle = new ResultHandle();
		
		OrdTravelContract ordTravelContract = findOrdTravelContract(orderId);
		
		OrdOrder order = complexQueryService.queryOrderByOrderId(orderId);
		
		IOrderTravelContractDataService orderTravelContractDataService = orderTravelContractDataServiceFactory.createTravelContractDataService(order);
		if (orderTravelContractDataService == null) {
			resultHandle.setMsg("无法抓取合同所需的数据。");
			return "/order/orderStatusManage/ship/lvmama_travelContractTemplate";
		}
		List<OrdTravelContract> list = new ArrayList<OrdTravelContract>();
		list.add(ordTravelContract);
		order.setOrdTravelContractList(list);
		
		
		File directioryFile = ResourceUtil.getResourceFile(TRAVEL_ECONTRACT_DIRECTORY);
		
		/*
		
		
		if (directioryFile == null || !directioryFile.exists()) {
			resultHandle.setMsg("合同模板目录不存在。");
		}
		Configuration configuration = initConfiguration(directioryFile);
		if (configuration == null) {
			resultHandle.setMsg("初始化freemarker失败。");
		}
		*/
		
		ResultHandleT<OutboundTourContractVO> resultHandleT = orderTravelContractDataService.captureOutboundTourContract(order);
		if (resultHandleT.isFail()) {
			resultHandle.setMsg("抓取邮轮数据失败。");
		}
		

		if (resultHandle.isSuccess()) {
			
			
			OutboundTourContractVO contractVO = resultHandleT.getReturnContent();
			contractVO.setTemplateDirectory(directioryFile.getAbsolutePath());
			
			
			OrdOrderPack ordOrderPack = order.getOrderPackList().get(0);
			ResultHandleT<CuriseProductVO> resultHandleCuriseProductVO = orderTravelContractDataService.getCombCuriseProducatData(ordOrderPack.getCategoryId(), ordOrderPack.getProductId());
			CuriseProductVO curiseProductVO = resultHandleCuriseProductVO.getReturnContent();
			ProdEcontract prodEcontract = curiseProductVO.getEcontract();
			if (prodEcontract.getMinPerson() != null) {
				contractVO.setHasMinPersonCount(true);
			} else {
				contractVO.setHasMinPersonCount(false);
			}
			
			
			contractVO.setContractVersion(ordTravelContract.getVersion());
			contractVO.setPayWay("在线支付给驴妈妈");
			
			model.addAttribute("contractVO", contractVO);
			
			
			model.addAttribute("hasMinPersonCount", contractVO.getHasMinPersonCount()+"");
			model.addAttribute("delegateGroup", contractVO.getDelegateGroup()+"");
			model.addAttribute("hasInsurance", contractVO.isHasInsurance()+"");
			
			
			
			
		}
		
		
		
		
		return "/order/orderStatusManage/ship/lvmama_travelContractTemplate";
	}
	
	/**
	 * 查询订单附件
	 * 
	 * @param model
	 * @param orderId
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/viewSendNoticeList")
	public String queryOrderAttachment(Model model,Long orderId,Long orderItemId,String contactEmail,HttpServletRequest req) throws BusinessException{
		
		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("orderId", orderId);
		param.put("attachmentType", OrderEnum.ATTACHMENT_TYPE.NOTICE_REGIMENT.name());
		param.put("_orderby","ORD_ATTACHMENT.create_time desc");
		List<OrderAttachment>  orderAttachmentList = orderAttachmentService.findOrderAttachmentByCondition(param);
		
		List<OrderAttachmentVO> resultList=new ArrayList<OrderAttachmentVO>();
		
		for (OrderAttachment orderAttachment : orderAttachmentList) {
			
			OrderAttachmentVO orderAttachmentVO=new OrderAttachmentVO();
			BeanUtils.copyProperties(orderAttachment, orderAttachmentVO);
			
			String comtent="";
			if (OrderEnum.FILE_TYPE.SMS.name().equals(orderAttachment.getFileType())) {
				comtent=(String)orderAttachment.getContentValueByKey(OrderEnum.ORDER_ATTACHMENT_CONTENT.sms.name());
			}else{
				comtent=orderAttachment.getMemo();
			}
			
			orderAttachmentVO.setMemo(comtent);
			
			resultList.add(orderAttachmentVO);
			
		}
//		model.addAttribute("orderAttachmentList", orderAttachmentList);
		model.addAttribute("orderAttachmentList", resultList);
		OrdOrder order = new OrdOrder();
		order.setOrderId(orderId);
		if(isDoneNoticeRegimentAudit(order)){
			model.addAttribute("contactEmail", contactEmail);
		}
		
		return "/order/orderAttachment/viewOrderAttachment";
	}
	

	@RequestMapping(value = "/updateTravelContract")
	@ResponseBody
	public Object updateTravelContract( HttpServletRequest request,OutboundTourContractVO contractVO ){
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("start method<updateTravelContract>");
		}
		
		
		OrdOrder oldOrder=ordOrderUpdateService.queryOrdOrderByOrderId(Long.valueOf(contractVO.getOrderId()));
		
		if (oldOrder.getOrderStatus().equals(OrderEnum.ORDER_STATUS.CANCEL.name())) {
			//name=OrderEnum.ORDER_STATUS.CANCEL.getCnName(oldOrder.getOrderStatus());
			return new ResultMessage(ResultMessage.ERROR,"订单已经取消不可修改合同");
		}
		
		
		String operatorName=this.getLoginUserId();
		
		ResultHandle resultHandle = orderTravelElectricContactService.updateTravelContact(contractVO, operatorName);
		
		
		if (resultHandle.isFail()) {
			String message="更新失败";
			return new ResultMessage(ResultMessage.ERROR,message);
		}
		
		return ResultMessage.UPDATE_SUCCESS_RESULT;
		
	}

	private String buildProductName(OrdOrderItem ordOrderItem) {
		String productName = "未知产品名称";
		if (null != ordOrderItem) {

			Map<String,Object> contentMap = ordOrderItem.getContentMap();
			
			String branchName =  (String) contentMap.get(OrderEnum.ORDER_COMMON_TYPE.branchName.name());
			
			productName = ordOrderItem.getProductName()+"-"+branchName+"("+ordOrderItem.getSuppGoodsName()+")";
		}
		return productName;
	}
	/**
	 * 处理订单的当前状态
	 * 
	 * @param ordOrderItem
	 * @return
	 */
	private String buildCurrentStatus(OrdOrder order,OrdOrderItem ordOrderItem) {
		StringBuilder builder = new StringBuilder();
		
		if (order.hasCanceled()) {
			return "废单";
		}
		
		//组装审核状态
		if(OrderEnum.INFO_STATUS.UNVERIFIED.name().equals(ordOrderItem.getInfoStatus())
				&& OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(ordOrderItem.getResourceStatus())){
			builder.append("未审核");
		}else if(OrderEnum.INFO_STATUS.INFOFAIL.name().equals(ordOrderItem.getInfoStatus())
				||OrderEnum.RESOURCE_STATUS.LOCK.name().equals(ordOrderItem.getResourceStatus())){
			builder.append("审核不通过");
		}else if(OrderEnum.INFO_STATUS.INFOPASS.name().equals(ordOrderItem.getInfoStatus())
				&&OrderEnum.RESOURCE_STATUS.AMPLE.name().equals(ordOrderItem.getResourceStatus())){
			builder.append("审核通过");
		}else{
			builder.append("审核中");
		}
		
		builder.append("<br>");
		
		

		builder.append("资源审核（");
		//组装资源确认状态
		if(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(ordOrderItem.getResourceStatus())){
			builder.append("未审核");
		}else if(OrderEnum.RESOURCE_STATUS.AMPLE.name().equals(ordOrderItem.getResourceStatus())){
			builder.append("确认通过");
		}else{
			builder.append("确认不通过");
		}
		
		
		
		builder.append("）<br>");
		

		builder.append("信息审核（");
		
		//组装信息确认状态
		if(OrderEnum.INFO_STATUS.UNVERIFIED.name().equals(ordOrderItem.getInfoStatus())){
			builder.append("未审核");
		}else if(OrderEnum.INFO_STATUS.INFOPASS.name().equals(ordOrderItem.getInfoStatus())){
			builder.append("确认通过");
		}else{
			builder.append("确认不通过");
		}
		
		builder.append("）");
		
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
		param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString());
		param.put("auditType", OrderEnum.AUDIT_TYPE.TIME_PAYMENT_AUDIT.getCode());
		param.put("auditStatus",OrderEnum.AUDIT_STATUS.PROCESSED.getCode() );
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
		param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString());
		param.put("auditType", OrderEnum.AUDIT_TYPE.NOTICE_AUDIT.getCode());
		param.put("auditStatus",OrderEnum.AUDIT_STATUS.PROCESSED.getCode() );
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
		param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER.toString());
		param.put("auditType", OrderEnum.AUDIT_TYPE.PRETRIAL_AUDIT.getCode());
		param.put("auditStatus",OrderEnum.AUDIT_STATUS.PROCESSED.getCode() );
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
			
			if (OrderEnum.AUDIT_TYPE.PAYMENT_AUDIT.getCode().equals(comAudit.getAuditType())) {
				map.put("PAYMENT_AUDIT", true);
			}else if (OrderEnum.AUDIT_TYPE.TIME_PAYMENT_AUDIT.getCode().equals(comAudit.getAuditType())) {
				map.put("TIME_PAYMENT_AUDIT", true);
			}else if (OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.getCode().equals(comAudit.getAuditType())) {
				map.put("CANCEL_AUDIT", true);
			}else if (OrderEnum.AUDIT_TYPE.NOTICE_AUDIT.getCode().equals(comAudit.getAuditType())) {
				map.put("NOTICE_REGIMENT_AUDIT", true);
			}else if (OrderEnum.AUDIT_TYPE.PRETRIAL_AUDIT.getCode().equals(comAudit.getAuditType())) {
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
	 * @param orderItem
	 * @return 
	 */
	private Map getOrderItemUnprocessedAudit(OrdOrderItem orderItem) {
//		boolean orderStatusNormal=OrderEnum.ORDER_STATUS.NORMAL.getCode().equals(order.getOrderStatus());
//		boolean paymentStatusPayed=OrderEnum.PAYMENT_STATUS.PAYED.equals(order.getPaymentStatus());
//		OrderEnum.AUDIT_STATUS.POOL.getCode() ,
		String[]  auditStatusArray=new String[]{OrderEnum.AUDIT_STATUS.UNPROCESSED.getCode() };
		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("objectId", orderItem.getOrderItemId());
		param.put("objectType", OrderEnum.AUDIT_OBJECT_TYPE.ORDER_ITEM.toString());
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
			}else if (OrderEnum.AUDIT_TYPE.CANCEL_AUDIT.getCode().equals(comAudit.getAuditType())) {
				map.put("CANCEL_AUDIT", true);
			}
		}
		
		
		//boolean isHaveUnprocessedAudit=comAuditList.size()>0?true:false;
		/*if ( orderStatusNormal && !paymentStatusPayed && !paymentAuditProcessed) {
			isDonePaymentAudit=true;
		}*/
		
		return map;
	}


	/**
	 * 是否显示定金核损
	 * @param order
	 * @return
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
		//定金支付
		if(PAY_TYPE.FULL.toString().equals(orderDownpay.getPayType())){
			return false;
		}

		//订单部分支付
		if (!OrderEnum.PAYMENT_STATUS.PART_PAY.toString().equals(order.getPaymentStatus())) {
			return false;
		}

		//订单已取消
		if (!OrderEnum.ORDER_STATUS.CANCEL.toString().equals(order.getOrderStatus())) {
			return false;
		}

		//定金已支付
		if(!OrderEnum.PAYMENT_STATUS.PAYED.toString().equals(orderDownpay.getPayStatus())){
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
	

	private List<HotelTimeRateInfo>  handleHouseTimeRateInfo(
			List<OrdOrderHotelTimeRate> orderHotelTimeRateList,OrdOrderItem orderItem) {
		List<ArrayList> settleList=new ArrayList<ArrayList>();
		ArrayList<OrdOrderHotelTimeRate> hotelTimeTateList=null;
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
		
		ResultHandleT<SuppGoodsTimePrice> resultHandleSuppGoodsTimePrice = suppGoodsTimePriceClientService.getTimePrice(orderItem.getSuppGoodsId(), orderItem.getVisitTime(), false);
		String lastTime="";
		String guaranteeTime="";
		if (resultHandleSuppGoodsTimePrice.isSuccess()) {
			SuppGoodsTimePrice suppGoodsTimePrice = resultHandleSuppGoodsTimePrice.getReturnContent();
			if (suppGoodsTimePrice!=null) {
				if (suppGoodsTimePrice.getAheadBookTime()!=null) {
					//最晚预定时间
					Date time=CalendarUtils.getEndDateByMinute(suppGoodsTimePrice.getSpecDate(), -suppGoodsTimePrice.getAheadBookTime());
					lastTime=CalendarUtils.getDateFormatString(time, CalendarUtils.YYYY_MM_DD_HH_MM_PATTERN);
				}
				if (suppGoodsTimePrice.getLatestUnguarTime()!=null) {
					//担保时间
					Date time=CalendarUtils.getEndDateByMinute(suppGoodsTimePrice.getSpecDate(), -suppGoodsTimePrice.getLatestUnguarTime());
					guaranteeTime=CalendarUtils.getDateFormatString(time, CalendarUtils.HH_MM_PATTERN);
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
	 * 根据orderId,传凭证确认类型附件,判读是否已经做过凭证确认活动
	 * @param certConfirmStatus
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
		
		
		if (OrderEnum.CERT_CONFIRM_STATUS.CONFIRMED.name().equals(certConfirmStatus)) {
			result=true;
		}
		
		
		
		return result;
	}
	
	/**
	 * 
	 * 查询出未分配或者待处理的 预订通知 auditIdArray
	 * @param objectId
	 * @return
	 */
	private Long[] findBookingAuditIds(String objectType,Long objectId) {
		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("objectId", objectId);
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
	
	
	@RequestMapping("/editPersonCountiune")
	public String editPersonCountiune(Model model,HttpServletRequest req) throws BusinessException{

		return "/order/orderStatusManage/ship/editPersonCountiune";
	}
	
	
	
	@RequestMapping("/updateTravellerConfirm")
	@ResponseBody
	public Object updateTravellerConfirm(HttpServletRequest request,OrdOrderTravellerConfirm ordOrderTravellerConfirm){
	
		ResultMessage msg = ResultMessage.createResultMessage();
		try {
			
			if(ordOrderTravellerConfirm!=null){
			
				ordOrderTravellerConfirm.setUpdateTime(new Date());
				int result= 0;
				OrderTravellerOperateDO orderTravellerOperateDO = new  OrderTravellerOperateDO();
				orderTravellerOperateDO.setOrderTravellerConfirm(ordOrderTravellerConfirm);
				orderTravellerOperateDO.setUserCode(getLoginUserId());
				orderTravellerOperateDO.setChannelType(VSTEnum.DISTRIBUTION.LVMAMABACK.getNum()+"");
				result=orderTravellerConfirmClientService.updateOrderTravellerConfirmInfo(orderTravellerOperateDO);
				if(result>0){
					msg.setMessage("保存成功");
				}else{
					msg.setMessage("保存失败");
				}
			}		
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
			msg.raise(e.getMessage());
		}
		return msg;
	}
		
}
