package com.lvmama.vst.order.service.book.destbu;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.comm.vst.VstOrderEnum.ORDER_CONTRACT_SIGNING_TYPE;

import com.lvmama.crm.model.OrdOrder;

import com.lvmama.comm.vst.vo.CardInfo;

import com.lvmama.vst.back.biz.po.BizBranch;

import com.lvmama.vst.back.biz.po.BizEnum;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.biz.po.BizEnum.BIZ_CATEGORY_TYPE;

import com.lvmama.vst.back.client.biz.service.BranchClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.client.prod.service.ProdLineRouteClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;

import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.control.vo.ResPreControlTimePriceVO;
import com.lvmama.vst.back.goods.po.PresaleStampTimePrice;

import com.lvmama.vst.back.client.prom.service.MarkCouponLimitClientService;

import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsLineTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsRefund;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.newHotelcomb.po.NewHotelCombTimePrice;
import com.lvmama.vst.back.newHotelcomb.po.SuppGoodsTimeStock;
import com.lvmama.vst.back.newHotelcomb.service.INewHotelCombTimePriceService;
import com.lvmama.vst.back.newHotelcomb.vo.AdditSuppGoodsGroupVO;
import com.lvmama.vst.back.newHotelcomb.vo.AdditSuppGoodsVo;
import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrdAddress;
import com.lvmama.vst.back.order.po.OrdOrderAmountItem;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderEnum.ORDER_TRAVEL_CONTRACT_STATUS;
import com.lvmama.vst.back.order.po.OrderItemAdditSuppGoods;
import com.lvmama.vst.back.order.po.OrderStatusEnum;
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prod.po.ProdRefund;
import com.lvmama.vst.back.prod.po.ProdRefundRule;
import com.lvmama.vst.back.prod.po.ProdProduct.PACKAGETYPE;
import com.lvmama.vst.back.prod.vo.ProdLineRouteVO;
import com.lvmama.vst.comm.enumeration.CommEnumSet.ELECTRONIC_CONTRACT_TEMPLATE;
import com.lvmama.vst.comm.utils.DESCoder;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ErrorCodeMsg;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.utils.json.JSONUtil;
import com.lvmama.vst.comm.utils.order.DestBuOrderPropUtil;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.OrderUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Constant.ORDER_FAVORABLE_TYPE;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.MarkcouponLimitInfo;

import com.lvmama.vst.comm.vo.order.Person;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo.Item;

import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.service.book.AbstractBookService;
import com.lvmama.vst.order.service.book.CreateOrdTravelContractData;
import com.lvmama.vst.order.service.book.NewHotelComOrderBussiness;
import com.lvmama.vst.order.service.book.NewHotelComOrderInitService;
import com.lvmama.vst.order.service.book.OrderPromotionBussiness;
import com.lvmama.vst.order.service.book.OrderRebateBussiness;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.utils.BlackListBussiness;
import com.lvmama.vst.order.utils.FieldValidUtils;
import com.lvmama.vst.order.utils.OrderUtils;

import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderItemDTO;

import com.lvmama.vst.pet.adapter.FavorServiceAdapter;

import com.lvmama.vst.pet.vo.CouponCheckParam;


import com.lvmama.vst.pet.vo.CouponCheckParam;
import com.lvmama.vst.pet.vo.UserCouponVO;
import com.lvmama.vst.pet.vo.VstCashAccountVO;

@Component("newHotelComOrderInitService")
public class NewHotelComOrderInitServiceImpl extends AbstractBookService
		implements NewHotelComOrderInitService {

	private static final Logger LOG = LoggerFactory
			.getLogger(DestBuHotelBookServiceImpl.class);

	@Autowired
	private SuppGoodsClientService suppGoodsClientService;

	@Autowired
	private ProdProductClientService productClientService;

	@Autowired
	private BranchClientService branchClientService;

	@Autowired
	private INewHotelCombTimePriceService newHotelCombTimePriceClientRemote;

	@Autowired
	private ProdLineRouteClientService prodLineRouteClientRemote;

	
	@Autowired
	private BlackListBussiness blackListBussiness;
	@Autowired
	private NewHotelComOrderBussiness  newHotelComOrderBussiness;
	
	@Autowired
	private SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;
	
	@Autowired
	protected ResPreControlService resControlBudgetRemote;

	
	@Autowired
	private MarkCouponLimitClientService markCouponLimitClientService;
	
	@Autowired
	private ProdProductClientService prodProductClientService;
	
	@Autowired
	private FavorServiceAdapter favorServiceAdapter;


	@Override 
	public OrdOrderDTO initOrderAndCalc(DestBuBuyInfo destbuyInfo,OrdOrderDTO order ) {

        SuppGoods  suppGoods  = new  SuppGoods()  ;
	      LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------initOrder---start");
			initOrder(destbuyInfo, order,suppGoods);
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------initOrder---end");
			// 游玩时间计算
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcOrderVisitTime---start");
		     calcOrderVisitTime(order);
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcOrderVisitTime---end");

			//限购计算
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcBlackList---start");

			calcBlackList(order);
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcBlackList---end");

			// 计算订单的品类
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcOrderCategroy---start");

			calcOrderCategroy(order);
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcOrderCategroy---end");

			// 添加bu计算，
		//     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcBuCode---end");

		//	calcBuCode(order,suppGoods);
		  //   LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcBuCode---end");

			 //工作流设置
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcWorkflow---start");
			calcWorkflow(order);
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcWorkflow---end");

			// 设置退改
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------setCancelStrategyToOrder---start");

			setCancelStrategyToOrder(order);
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------setCancelStrategyToOrder---end");

			// 依赖资源审核的状态做支付时间
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcPaymentType---start");
      
			calcPaymentType(order);
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcPaymentType---end");

			// 资源(主订单)计算
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcResourceConfirm---start");
			calcResourceConfirm(order);
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcResourceConfirm---end");
			// 订单金额计算
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcOrderAmount---start");
			calcOrderAmount(order);
		    LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcOrderAmount---end");
			//计算订单是否需要填充紧急联系人
			calOrderPesornList(order);
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcRebate---start");
			calcRebate(order);// 订单返现
			   LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcRebate---end");
			   LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcBonus---start");
			calcBonus(order,destbuyInfo);// 抵扣计算
			  LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcBonus---end");
			  calcPromition(order); //促销计算
			  LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcManagerId---start");
			calcManagerId(order);// 产品经理计算
			// 初始化电子合同
			  LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcManagerId---end");
			  LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------initOrdTravelContract---start");
			initOrdTravelContract(order);
			  LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------initOrdTravelContract---end");
		return order;
	}


	private void calOrderPesornList(OrdOrderDTO order) {
		List <OrdPerson> listOrdPerson=order.getOrdPersonList();
		if (CollectionUtils.isNotEmpty(listOrdPerson)) {
			boolean havaEmergencyContact=false;
			for (OrdPerson person : order.getOrdPersonList()) {
				 if(OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name().equalsIgnoreCase(person.getPersonType())){
					 havaEmergencyContact=true;
				}
			}
			if (order.getDistributorId()!=null && order.getDistributorId() !=2L) {
				if (order.getCategoryId()!=null&&(15L==order.getCategoryId() || 16L==order.getCategoryId() || 17L==order.getCategoryId() || 18L==order.getCategoryId() || 8L == order.getCategoryId())) {
					if(!havaEmergencyContact){
						OrdPerson emergencyContact =new OrdPerson();
						emergencyContact.setPersonType(OrderEnum.ORDER_PERSON_TYPE.EMERGENCY.name());
						emergencyContact.setObjectType(OrderEnum.SETTLEMENT_TYPE.ORDER.name());
						listOrdPerson.add(emergencyContact);
					}
				}
			}
			order.setOrdPersonList(listOrdPerson);
		}else{
			new BusinessException(ErrorCodeMsg.ERR_EMERCONTACT);
		}
	}

	public void initOrder(DestBuBuyInfo destbuyInfo, OrdOrderDTO order,SuppGoods suppGoodsMain ) {

		initBuyInfo(destbuyInfo);
		initOrderBase(destbuyInfo, order);
		// 游玩人初始化
		initPerson(order,destbuyInfo);
		// 子订单初始化
		List<OrdOrderItem> ordOrderItemList = new ArrayList<OrdOrderItem>();
		SuppGoodsParam suppGoodsParam = new SuppGoodsParam();
		suppGoodsParam.setProduct(true);
		suppGoodsParam.setProductBranch(true);
		suppGoodsParam.getProductParam().setBizCategory(true);
		suppGoodsParam.setSupplier(true);
		List<OrderItemAdditSuppGoods> OrderItemAdditSuppGoodsList = new ArrayList<OrderItemAdditSuppGoods>();
		for (DestBuBuyInfo.Item item : destbuyInfo.getItemList()) {
			
			ResultHandleT<SuppGoods> suppGoods = suppGoodsClientService.findSuppGoodsById(item.getGoodsId(), suppGoodsParam);
			if(suppGoods==null||suppGoods.getReturnContent()==null){
				throw new BusinessException(ErrorCodeMsg.ERR_SUPPGOOD_001);
			}else{
			
			OrdOrderItem orderItem = initItem(order, item,destbuyInfo);
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId()==(suppGoods.getReturnContent().getCategoryId())){
				// 主订单设置和计算
				order.setPaymentTarget(suppGoods.getReturnContent().getPayTarget());
				suppGoodsMain = suppGoods.getReturnContent();	
				orderItem.setMainItem("true");
				order.setFilterMainOrderItem(orderItem);
				
				// 添加bu计算，
			     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcBuCode---end");

				 calcBuCode(order,suppGoodsMain);
			     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcBuCode---end");
				// 初始化商品附加商品信息
				List<AdditSuppGoodsGroupVO>  additSuppGoodsGroupList = item.getAdditSuppGoodsGroupVO();  
				if(additSuppGoodsGroupList!=null&&additSuppGoodsGroupList.size()>0){
				for(AdditSuppGoodsGroupVO additSuppGoodsGroup:additSuppGoodsGroupList){
					List<AdditSuppGoodsVo>   additSuppGoodsList=additSuppGoodsGroup.getAdditSuppGoodsVo();
					   for(AdditSuppGoodsVo additSuppGoods:additSuppGoodsList){
							OrderItemAdditSuppGoods orderItemAdditSuppGoods = new OrderItemAdditSuppGoods();
							orderItemAdditSuppGoods.setAddItSuppGoodsId(additSuppGoods.getAdditSuppGoodsId());
							orderItemAdditSuppGoods.setQuantity(additSuppGoods.getQuantity());
							orderItemAdditSuppGoods.setCreateDay(new Date());
							OrderItemAdditSuppGoodsList.add(orderItemAdditSuppGoods);
							//orderItemId 要等插入后进行设置
					//		orderItemAdditSuppGoods.setOrderItemId(orderItemId);
							
					   }
				
				  
				}
					
				}
			}
          orderItem.setOrderItemAdditSuppGoods(OrderItemAdditSuppGoodsList);
			ordOrderItemList.add(orderItem);
			}
		}
		order.setOrderItemList(ordOrderItemList);
		if (order.getNopackOrderItemList() == null) {
			order.setNopackOrderItemList(order.getOrderItemList());
		}
		
		//  促销初始化
		if(!destbuyInfo.getPromotionMap().isEmpty()){
			boolean haveExcludeProm=false;
			boolean haveChannelProm=false;
			int promCount=0;
			for(String key:destbuyInfo.getPromotionMap().keySet()){
				List<Long> promotionIds = destbuyInfo.getPromotionMap().get(key);
				if(CollectionUtils.isNotEmpty(promotionIds)){
					//对促销的list进行去重
					List<Long> prodIdsNew = new ArrayList<Long>();
					for(Long id:promotionIds){
						if(!prodIdsNew.contains(id)){
							prodIdsNew.add(id);
						}
					}
			//	OrderPromotionBussiness bussiness = orderOrderFactory.createInitPromition(key);
			//		LOG.info("bussiness.initPromotion params:key="+key+",promotionIds="+prodIdsNew);
			//	key="orderProm_"+key ;
					List<OrdPromotion> list = newHotelComOrderBussiness.initPromotion(order,key,prodIdsNew);

					order.addOrdPromotions(key,list);
				}
			}
		}
	}

	private void initBuyInfo(DestBuBuyInfo buyInfo) {

		if (StringUtils.isEmpty(buyInfo.getVisitTime())) {
			throwNullException("游玩日期不存在");
		}
		if (CollectionUtils.isNotEmpty(buyInfo.getItemList())) {
			for (Item item : buyInfo.getItemList()) {
				item.setVisitTime(buyInfo.getVisitTime());
			}
		}

	}

	public void initOrderBase(DestBuBuyInfo buyInfo, OrdOrderDTO order) {
		order.setCreateTime(new Date());
		order.setOrderUpdateTime(new Date());
		order.setBonusAmount(0L);

		order.setRebateAmount(0L);
		order.setRebateFlag("N");
		order.setOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.name());
		order.setPaymentStatus(OrderEnum.PAYMENT_STATUS.UNPAY.name());
		order.setPaymentTime(new Date());
		order.setInfoStatus(OrderEnum.INFO_STATUS.UNVERIFIED.name());
		order.setViewOrderStatus(OrderEnum.ORDER_VIEW_STATUS.UNVERIFIED.name());
		order.setActualAmount(0L);
		order.setDepositsAmount(0L);
		order.setNeedInvoice("false");
		order.setCurrencyCode(OrderEnum.ORDER_CURRENCY_CODE.RMB.name());

		// DestBuBuyInfo buyInfo = order.getDestBuBuyInfo();

		// 手机设备号
		order.setMobileEquipmentNo(buyInfo.getMobileEquipmentNo());
		// order.setAnonymityBookFlag(buyInfo.getAnonymityBookFlag());
		order.setDistributorId(buyInfo.getDistributionId());
		order.setDistributorCode(buyInfo.getDistributorCode());
		order.setDistributorName(buyInfo.getDistributorName());
		order.setLineRouteId(buyInfo.getLineRouteId());
		if (LOG.isInfoEnabled()) {
			LOG.info("distributorCode==========" + buyInfo.getDistributorCode());
		}
		order.setRemark(buyInfo.getRemark());
		// 设置分销商ID
		order.setDistributionChannel(buyInfo.getDistributionChannel());
		if (LOG.isInfoEnabled()) {
			LOG.info("distributionChannel=========="
					+ buyInfo.getDistributionChannel());
		}
		order.setUserId(buyInfo.getUserId());
		order.setUserNo(buyInfo.getUserNo());
		
		if (StringUtils.isNotEmpty(buyInfo.getNeedInvoice())) {
			order.setInvoiceStatus(buyInfo.getNeedInvoice());
		} else {
			order.setInvoiceStatus(OrderEnum.NEED_INVOICE_STATUS.UNBILL.name());
		}
		order.setRemark(buyInfo.getRemark());
		order.setClientIpAddress(buyInfo.getIp());

		order.setCertConfirmStatus(OrderEnum.CERT_CONFIRM_STATUS.UNCONFIRMED
				.name());
		order.setCancelCertConfirmStatus(OrderEnum.CANCEL_CERTCONFIRM_STATUS.UNCONFIRMED
				.name());

		order.setBookLimitType(OrderEnum.GUARANTEE_TYPE.NONE.name());
		order.setPaymentType(SuppGoodsTimePrice.BOOKLIMITTYPE.NONE.name());
		// order.setStartDistrictId(buyInfo.getStartDistrictId());
		order.setIsTestOrder(buyInfo.getIsTestOrder());
		// order.setSmsLvmamaFlag(buyInfo.getSmsLvmamaFlag());
		order.setSupplierApiFlag(OrderEnum.SUPPLIER_API_FLAG.N.name());
		// 初始化促销
		


	}

	private void calcOrderVisitTime(OrdOrderDTO order) {
		int size = order.getOrderItemList().size();
		Date visitTime = order.getOrderItemList().get(0).getVisitTime();
		Date lastCancelTime = order.getOrderItemList().get(0)
				.getLastCancelTime();

		for (int i = 1; i < size; i++) {
			OrdOrderItem orderItem = order.getOrderItemList().get(i);
			LOG.info("Now check visit time for item, item size is "
					+ order.getOrderItemList().size() + ", visitTime is "
					+ visitTime);

			if (orderItem.getVisitTime().before(visitTime)) {
				visitTime = orderItem.getVisitTime();
			}
			if (orderItem.getLastCancelTime() != null) {
				if (lastCancelTime == null) {
					lastCancelTime = orderItem.getLastCancelTime();
				} else if (lastCancelTime.after(orderItem.getLastCancelTime())) {
					lastCancelTime = orderItem.getLastCancelTime();
				}
			}
		}
		order.setLastCancelTime(lastCancelTime);
		order.setVisitTime(visitTime);
	}

	private OrdOrderItem initItem(final OrdOrderDTO order,
			DestBuBuyInfo.Item item,DestBuBuyInfo buyInfo) {
		LOG.info("----start initItem-----");
		if (item.getQuantity() > 0
				|| (item.getAdultQuantity() + item.getChildQuantity()) > 0) {
			OrdOrderItemDTO orderItem = new OrdOrderItemDTO();
	

			orderItem.setVisitTime(item.getVisitTimeDate());
			orderItem.setSuppGoodsId(item.getGoodsId());
			orderItem.setQuantity((long) item.getQuantity());
			// orderItem.setItem(item);
			orderItem.setTotalAmount(item.getTotalAmount());
			// orderItem.setTotalSettlementPrice(item.getTotalSettlementPrice());
			orderItem.setSharedStockList(item.getSharedStockList());
			// 结构化酒店套餐,具体选择规格
			// orderItem.setHotelcombOptions(item.getHotelcombOptions());
			SuppGoodsParam param = new SuppGoodsParam();
			param.setProduct(true);
			ProdProductParam ppp = new ProdProductParam();
			ppp.setBizCategory(true);
			param.setProductBranch(true);
			param.setSupplier(true);
			param.setProductParam(ppp);
			param.setSuppGoodsExp(true);
			param.setSuppGoodsEventAndRegion(true);
			// LOG.info("suppGoodsClientService.findSuppGoodsById开始---goodsId="+item.getGoodsId()+item.getOrderSubType());

			SuppGoods suppGoods = suppGoodsClientService.findSuppGoodsById(
					item.getGoodsId(), param).getReturnContent();
        
			// 设置子单“公司主体”--SuppGoodsId--
			if (StringUtils.isNotBlank(suppGoods.getCompanyType())) {
				orderItem.setCompanyType(suppGoods.getCompanyType());
			}
			orderItem.setProductId(suppGoods.getProductId());

			orderItem.setCategoryId(suppGoods.getCategoryId());
            item.setProductCategoryId(suppGoods.getCategoryId());
            orderItem.setSuppGoods(suppGoods);
			// 子订单商品校验
			checkSaleAble(suppGoods);

			// orderItem.setItem(item);
			initItemDetail(order, orderItem, suppGoods,buyInfo);

			if (item.getGoodType() != null
					&& "localRoute".equals(item.getGoodType())) {// orderitem标识是否为关联销售商品
				orderItem.putContent("relatedMarketingFlag", "localRoute");
			}

			// 时间价格表处理
//			OrderTimePriceService orderTimePriceService = orderOrderFactory
//					.createTimePrice(orderItem);
			// 时间价格当中处理门票的特殊性的时间价格问题
			LOG.info("orderTimePriceService.validate开始");
			ResultHandle handle  =null;
			if(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId()==(suppGoods.getCategoryId())){
			 handle = this.validate(suppGoods, item, orderItem,
					order);
			}else{
				orderItem.setMainItem("false");
				OrderTimePriceService orderTimePriceService = orderOrderFactory.createTimePrice(orderItem); // fangxiang  
				handle =orderTimePriceService.validate(suppGoods, item, orderItem, order);
			}
			LOG.info("orderTimePriceService.validate返回");
			if (handle.isFail()) {
				throwIllegalException(handle.getMsg());
			}

			LOG.info("---initItem return orderItem----");
			return orderItem;

		} else {
			return null;
		}
	}

	private void calcMainItem(OrdOrderDTO order, SuppGoods supp) {

		order.getOrderItemList().get(0).setMainItem("true");

		order.setFilialeName(supp.getFiliale());// 分公司名称
	}

	private void calcOrderCategroy(OrdOrderDTO order) {
		if(order.getCategoryId()==null&&order.getFilterMainOrderItem()!=null){
			order.setCategoryId(order.getFilterMainOrderItem().getCategoryId());
		}
	}

	private void calcBuCode(OrdOrderDTO order, SuppGoods supp) {
		//主商品bu
		LOG.info("=主商品id==id:"+supp.getSuppGoodsId()+"buCode:"+supp.getBu());
		order.setBuCode(supp.getBu());// bucode
		order.setCompanyType(supp.getCompanyType());// 公司主体
		order.setAttributionId(supp.getAttributionId());// 公司归属地
	}

	// 退改
	private void setCancelStrategyToOrder(OrdOrderDTO order)
			throws BusinessException {
		ResultHandleT<ProdProduct> resultProduct = productClientService
				.findProdProductByIdFromCache(order.getProductId());
		if (resultProduct == null || resultProduct.getReturnContent() == null) {
			throw new BusinessException(ErrorCodeMsg.ERR_PRODUCT_00002);
		} else {
			// 获取退改策略

			ProdRefund refund = productClientService.findProductReFundByProdId(
					order.getProductId(), order.getVisitTime());
			if (refund == null) {
				LOG.error("prodProduct product id:{} 无退改策略"
						+ order.getProductId());
				throw new BusinessException(ErrorCodeMsg.ERR_PROREFUND_001);
			}
			order.setRealCancelStrategy(refund.getCancelStrategy());
			this.setCancelStrategyToOrderItem(order, refund);
		}

	}

	// 设置子项的退改
	private void setCancelStrategyToOrderItem(OrdOrderDTO order,
			ProdRefund refund) throws BusinessException {
		if (order.getRealCancelStrategy() == null
				|| ProdRefund.CANCELSTRATEGYTYPE.GOODSRETREATANDCHANGE
						.getCode().equals(order.getRealCancelStrategy())
				|| ProdRefund.CANCELSTRATEGYTYPE.MANUALCHANGE.getCode().equals(
						order.getRealCancelStrategy()))
			return;

		if (ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode().equals(
				order.getRealCancelStrategy())) {
			if (CollectionUtils.isEmpty(refund.getProdRefundRules())) {// 退改规则不能为空
				LOG.error("prodProduct product id:{} 无退改规则"
						+ refund.getProductId());
				throw new BusinessException(ErrorCodeMsg.ERR_PROREFUND_002);
			}

			String refundRules = getRefundRulesByOrderItem(order, refund);
			for (OrdOrderItem orderItem : order.getOrderItemList()) {
				orderItem.setCancelStrategy(order.getRealCancelStrategy());
				orderItem.setRefundRules(refundRules);
			}
		}

	}

	/**
	 * 获取退改规则JSON
	 * 
	 * @param order
	 * @param refund
	 *            主产品退改策略
	 * @return
	 */
	private String getRefundRulesByOrderItem(OrdOrderDTO order,
			ProdRefund refund) {

		List<SuppGoodsRefund> refunds = new ArrayList<SuppGoodsRefund>();
		for (ProdRefundRule rule : refund.getProdRefundRules()) {
			SuppGoodsRefund goodsrefund = new SuppGoodsRefund();
			goodsrefund.setCancelStrategy(refund.getCancelStrategy());
			goodsrefund.setLatestCancelTime(rule.getLongLastTime());
			goodsrefund.setCancelTimeType(rule.getLastTime());
			BeanUtils.copyProperties(rule, goodsrefund);
			refunds.add(goodsrefund);
		}
		String jsonSt = JSONArray.fromObject(refunds).toString();
		if (jsonSt.length() >= 4000) {
			LOG.warn("Order [productId=" + refund.getProductId()
					+ "]'s refundRules_json is out of size 4000/"
					+ jsonSt.length() + " .Has ["
					+ refund.getProdRefundRules().size() + "] refund rules.");
			jsonSt = jsonSt.substring(0, 3999);
		}
		return jsonSt;
	}

	private void calcPaymentType(OrdOrderDTO order) {
		if (order.hasNeedPrepaid()) {
			OrdOrderItem orderItem = order.getOrderItemList().get(0);
			Date aheadTime = orderItem.getAheadTime();
			order.setApproveTime(order.getCreateTime());

			order.setWaitPaymentTime(OrdOrderUtils
					.calcWaitPaymentTimeForDestBu(aheadTime,
							order.getCreateTime()));// 等待时间

		}
	}

	public void calcManagerId(OrdOrderDTO order) {
		Long managerId = null;
		if (managerId == null) {
			OrdOrderItem mainOrderItem = null;
			List<OrdOrderItem> ordOrderItemList = order.getOrderItemList();
			if (CollectionUtils.isNotEmpty(ordOrderItemList)) {
				for (OrdOrderItem item : ordOrderItemList) {
					if ("true".equalsIgnoreCase(item.getMainItem())) {
						mainOrderItem = item;
						break;
					}
				}
			}
			if (mainOrderItem != null) {
				managerId = mainOrderItem.getManagerId();
			}
		}

		// 3.设置值.
		if (managerId != null) {
			order.setManagerId(managerId);
		} else {
			LOG.error("no manager_id");
		}

	}

	/**
	 * 计算资源相关
	 * 
	 * @param order
	 */
	private void calcResourceConfirm(OrdOrderDTO order) {
		LOG.info("计算资源相关 itemlist的大小++++++++++++++++++++++++++++" + order);
		if (order != null
				&& CollectionUtils.isNotEmpty(order.getOrderItemList())) {
			LOG.info("计算资源相关 itemlist的大小++++++++++++++++++++++++++++"
					+ order.getOrderItemList().size());
			for (OrdOrderItem orderItem : order.getOrderItemList()) {
				if (CollectionUtils.isNotEmpty(orderItem.getOrderStockList())) {

					for (OrdOrderStock stock : orderItem.getOrderStockList()) {
						if ("true".equalsIgnoreCase(stock
								.getNeedResourceConfirm())) {
							orderItem.setNeedResourceConfirm("true");
							break;
						}
					}
					String status = orderItem.getOrderStockList().get(0)
							.getResourceStatus();
					int size = orderItem.getOrderStockList().size();
					if (size > 1) {
						for (int i = 1; i < orderItem.getOrderStockList()
								.size(); i++) {
							status = getOrderResourceStatus(status, orderItem
									.getOrderStockList().get(i)
									.getResourceStatus());
						}
					}

					orderItem.setResourceStatus(status);

					// 计算主订单的资源状态
					setOrderResourceStatus(orderItem, order);

				}

			}

		}
	}

	private static OrderEnum.RESOURCE_STATUS[] RESOURCE_STATUS_ARRAY = {
			OrderEnum.RESOURCE_STATUS.LOCK,
			OrderEnum.RESOURCE_STATUS.UNVERIFIED,
			OrderEnum.RESOURCE_STATUS.AMPLE };

	private String getOrderResourceStatus(final String resourceStatus,
			final String newResourceStatus) {
		if (StringUtils.isEmpty(resourceStatus)) {
			return newResourceStatus;
		}
		OrderEnum.RESOURCE_STATUS newStatus = OrderEnum.RESOURCE_STATUS
				.valueOf(newResourceStatus);
		OrderEnum.RESOURCE_STATUS orderResourceStatus = OrderEnum.RESOURCE_STATUS
				.valueOf(resourceStatus);
		int newPos = ArrayUtils.indexOf(RESOURCE_STATUS_ARRAY, newStatus);
		int oldPos = ArrayUtils.indexOf(RESOURCE_STATUS_ARRAY,
				orderResourceStatus);
		if (newPos < oldPos) {
			return newStatus.name();
		} else {
			return orderResourceStatus.name();
		}
	}

	private void setOrderResourceStatus(final OrdOrderItem orderItem,
			final OrdOrderDTO order) {
		String status = getOrderResourceStatus(order.getResourceStatus(),
				orderItem.getResourceStatus());
		if (StringUtils.isNotEmpty(status)) {
			order.setResourceStatus(status);
		}
	}

	/**
	 * 计算订单金额
	 * 
	 * @param order
	 */

	private void calcOrderAmount(OrdOrderDTO order) {
		long totalAmount = 0L;
		long totalSettlement = 0L;
		for (OrdOrderItem orderItem : order.getOrderItemList()) {
			orderItem.setActualSettlementPrice(orderItem.getSettlementPrice());
			if (orderItem.getTotalSettlementPrice() == null) {
				orderItem.setTotalSettlementPrice(orderItem
						.getActualSettlementPrice() * orderItem.getQuantity());
			}
			if (orderItem.getTotalAmount() == null) {
				orderItem.setTotalAmount(orderItem.getPrice()
						* orderItem.getQuantity());

				LOG.info("-------------------------------------------------------"
						+ orderItem.getTotalAmount()
						+ "orderItem.getPrice()"
						+ orderItem.getPrice() + "orderItem.getQuantity()");
			}

			totalAmount += orderItem.getTotalAmount();
			totalSettlement += orderItem.getTotalSettlementPrice();
		}
		OrdOrderAmountItem item = makeOrderAmountItem(
				OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_PRICE,
				OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_ORDER, totalAmount);
		order.addOrderAmountItem(item);
		item = makeOrderAmountItem(
				OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_SETTLEPRICE,
				OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_ORDER, totalSettlement);
		order.addOrderAmountItem(item);
     LOG.info("totalAmount======="+totalAmount);
		order.setOughtAmount(totalAmount);
	}

	private OrdOrderAmountItem makeOrderAmountItem(
			OrderEnum.ORDER_AMOUNT_TYPE type, OrderEnum.ORDER_AMOUNT_NAME name,
			long totalAmount) {
		OrdOrderAmountItem item = new OrdOrderAmountItem();
		item.setItemAmount(totalAmount);
		item.setOrderAmountType(type.name());
		item.setItemName(name.getCode());
		return item;
	}

	// 返现计算
	@Override
	public void calcRebate(OrdOrderDTO order) {
		newHotelComOrderBussiness.DestBucalcRebate(order);
	}

	public void initPerson(OrdOrderDTO order,DestBuBuyInfo destbuyInfo) {
		List<OrdPerson> ordPersonList = order.getOrdPersonList();
		if (ordPersonList == null) {
			ordPersonList = new ArrayList<OrdPerson>();
		}
		// 下单人
		Person booker = destbuyInfo.getBooker();
		// 从vo的Person对象转换成po的OrdPerson对象。
		OrdPerson ordPerson = getOrdPersonFromPerson(booker);

		if (ordPerson != null) {
			// 设置下单人与订单关联
			ordPerson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER
					.name());
			// 设置下单人类型。
			ordPerson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.BOOKER.name());
			ordPersonList.add(ordPerson);
		}
		Person contact =destbuyInfo.getContact();
		OrdPerson contactPerson = getOrdPersonFromPerson(contact);
		if(contactPerson!=null){
			contactPerson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER
					.name());
			// 设置下单人类型。
			contactPerson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.CONTACT.name());
			ordPersonList.add(contactPerson);
		}
		List<Person> travellers = destbuyInfo.getTravellers();
		if(travellers!=null&&travellers.size()>0){
			LOG.info("travellers的size"+travellers.size());
			
			for(Person  traveller:travellers){
				if(traveller.getFullName()!=null){
				OrdPerson travellerPerson = getOrdPersonFromPerson(traveller);
				if(travellerPerson!=null){
					LOG.info("travellerPerson======"+travellerPerson.getFirstName());
					travellerPerson.setObjectType(OrderEnum.ORDER_PERSON_OBJECT_TYPE.ORDER
							.name());
					// 设置下单人类型。
					travellerPerson.setPersonType(OrderEnum.ORDER_PERSON_TYPE.TRAVELLER.name());
					ordPersonList.add(travellerPerson);
				}
			 }
			}
		}
		order.setOrdPersonList(ordPersonList);

	}

	/**
	 * vo中的Person对象转换成po中的OrdPerson对象。
	 * 
	 * @param person
	 * @return
	 */
	private OrdPerson getOrdPersonFromPerson(Person person) {
		OrdPerson ordPerson = null;

		if (person != null) {
			ordPerson = new OrdPerson();
			ordPerson.setEmail(person.getEmail());
			ordPerson.setFax(person.getFax());
			ordPerson.setFirstName(person.getFirstName());

			ordPerson.setGender(person.getGender());

			ordPerson.setLastName(person.getLastName());
			ordPerson.setMobile(person.getMobile());
			ordPerson.setNationality(person.getNationality());
			ordPerson.setPhone(person.getPhone());
			ordPerson.setIdNo(person.getIdNo());
			ordPerson.setIdType(person.getIdType());

			ordPerson.setPeopleType(person.getPeopleType());
			if (person.getBirthday() != null
					&& !"".equals(person.getBirthday().trim())) {
				ordPerson.setBirthday(DateUtil.toDate(person.getBirthday()
						.trim(), "yyyy-MM-dd"));
			}

			// 台胞证和回乡证设置签发地和有效期
			if (!StringUtil.isEmptyString(person.getIdType())) {
				//
				// if(person.getIdType().equals(OrderEnum.ORDER_PERSON_ID_TYPE.TAIBAOZHENG.name())||
				// person.getIdType().equals(OrderEnum.ORDER_PERSON_ID_TYPE.HUIXIANG.name())){
				//
				// 有效期
				if (person.getExpDate() != null
						&& !"".equals(person.getExpDate())) {
					ordPerson.setExpDate(person.getExpDate());
				}
				// 签发地
				ordPerson.setIssued(person.getIssued());
				// }
				// 出生地
				ordPerson.setBirthPlace(person.getBirthPlace());
				// 签发日期
				if (person.getIssueDate() != null
						&& !"".equals(person.getIssueDate())) {
					ordPerson.setIssueDate(person.getIssueDate());
				}
			}
			// 设置全名
			boolean isChineseName = false;
			String lastName = ordPerson.getLastName();
			String firstName = ordPerson.getFirstName();
			if (lastName != null) {
				if (StringUtil.hasChinese(lastName)) {
					isChineseName = true;
				}
			}

			if (!isChineseName && (firstName != null)) {
				if (StringUtil.hasChinese(firstName)) {
					isChineseName = true;
				}
			}
			ordPerson.setFullName(person.getFullName());
			if (StringUtils.isEmpty(person.getFullName())) {
				if (isChineseName) {
					StringBuffer sb = new StringBuffer();
					if (StringUtils.isNotEmpty(lastName)) {
						sb.append(lastName);
					}
					if (StringUtils.isNotEmpty(firstName)) {
						sb.append(firstName);
					}
					ordPerson.setFullName(sb.toString());
				} else {
					StringBuffer sb = new StringBuffer();

					if (StringUtils.isNotEmpty(firstName)) {
						sb.append(firstName);
					}
					if (StringUtils.isNotEmpty(lastName)) {
						if (sb.length() > 1) {
							sb.append("/");
						}
						sb.append(lastName);
					}
					ordPerson.setFullName(sb.toString());
				}
			}

			if (!FieldValidUtils.checkFieldLength(ordPerson.getFullName(), 150)) {
				throwIllegalException("游玩人中文姓名长度过长。");
			}
		}

		return ordPerson;
	}

	private void checkSaleAble(SuppGoods suppGoods) {
		LOG.info("scheckSaleAble开始");
		ResultHandleT<BizBranch> branch = branchClientService
				.findBranchById(suppGoods.getProdProductBranch().getBranchId());
		if (branch == null || branch.hasNull()) {
			throwIllegalException("商品不可售");
		}
		suppGoods.getProdProductBranch()
				.setBizBranch(branch.getReturnContent());
		if (!suppGoods.isValid()) {
			throwIllegalException("商品不可售");
		}
		LOG.info("scheckSaleAble结束");
	}

	public void initItemDetail(OrdOrderDTO order, OrdOrderItem orderItem,
			SuppGoods suppGoods,DestBuBuyInfo buyInfo) {
		initOrderItemBase(order, orderItem, suppGoods, buyInfo);
		// 出团公告
		if (orderItem.getOrderPack() == null) {
			OrdAdditionStatus ordAdditionStatus = OrderUtils
					.makeOrdAdditionStatus(
							OrderEnum.ORD_ADDITION_STATUS_TYPE.NOTICE_REGIMENT_STATUS
									.name(),
							OrderEnum.NOTICE_REGIMENT_STATUS_TYPE.NO_UPLOAD
									.name());
			order.addOrdAdditionStatus(ordAdditionStatus);
		}
		ProdProductParam param = new ProdProductParam();
		param.setLineRoute(true);
		ResultHandleT<ProdProduct> product=prodProductClientService.findLineProductByProductId(orderItem.getProductId(), param);
			if (product!=null && product.getReturnContent()!=null && CollectionUtils.isNotEmpty(product.getReturnContent().getProdLineRouteList())) {//根据Item的产品ID获取其产品信息
				ProdLineRouteVO route = product.getReturnContent().getProdLineRouteList().get(0);
				if(route!=null){//获取产品上的线路信息
					orderItem.putContent(OrderEnum.ORDER_PACK_TYPE.route_days.name(), route.getRouteNum());
					orderItem.putContent(OrderEnum.ORDER_PACK_TYPE.route_nights.name(), route.getStayNum());
				}
			}
		

		
		orderItem.putContent(OrderEnum.ORDER_ROUTE_TYPE.route_product_type.name(), orderItem.getSuppGoods().getProdProduct().getProductType());
	}

	private void initOrderItemBase(OrdOrderDTO order, OrdOrderItem orderItem,
			SuppGoods suppGoods,DestBuBuyInfo buyInfo) {
		orderItem.setSuppGoods(suppGoods);
		orderItem.setCategoryId(suppGoods.getProdProduct().getBizCategoryId());
		orderItem.setBranchId(suppGoods.getProdProductBranch()
				.getProductBranchId());
		// 产品经理ID
		if (suppGoods.getManagerId() == null) {
			LOG.error("supp_goods_id:" + suppGoods.getSuppGoodsId()
					+ " have no manager_id");
		} else {
			orderItem.setManagerId(suppGoods.getManagerId());
		}
		orderItem.setBuCode(suppGoods.getBu());// 赋予商品真实BU，改字段值根据业务逻辑判断是否改变
		orderItem.setRealBuType(suppGoods.getBu());// 赋予商品真实BU
		orderItem.setAttributionId(suppGoods.getAttributionId());// 赋予商品归属地
		// 凭证确认状态
		orderItem
				.setCertConfirmStatus(OrderEnum.ITEM_CERT_CONFIRM_STATUS.UNCONFIRMED
						.name());

		// 取消凭证确认
		orderItem
				.setCancelCertConfirmStatus(OrderEnum.ITEM_CANCEL_CERTCONFIRM_STATUS.UNCONFIRMED
						.name());

		// orderItem.setBookLimitType(OrderEnum.GUARANTEE_TYPE.NONE.name());

		// 扣款类型
		orderItem.setDeductType(SuppGoodsTimePrice.DEDUCTTYPE.NONE.name());

		orderItem.setOrderStatus(OrderEnum.ORDER_STATUS.NORMAL.name());

		orderItem.setPaymentStatus(OrderEnum.PAYMENT_STATUS.UNPAY.name());
      
		// 传真备注，设置在订单子项中
		String faxMemo = suppGoods.getFaxRemark();
		if (faxMemo != null && !"".equals(faxMemo)) {
			orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fax_remark.name(),
					faxMemo);
		}
		// 合同ID
		orderItem.setContractId(suppGoods.getContractId());

		// 产品ID
		orderItem.setProductId(suppGoods.getProductId());

		// 供应商ID
		orderItem.setSupplierId(suppGoods.getSupplierId());

		// 产品名称
		orderItem.setProductName(suppGoods.getProdProduct().getProductName());

		// 商品名称
		orderItem.setSuppGoodsName(suppGoods.getGoodsName());

		// 供应商产品名称
		orderItem.setSuppProductName(suppGoods.getProdProduct()
				.getSuppProductName());

		// 履行状态
		orderItem.setPerformStatus(OrderEnum.ORDER_PERFORM_STATUS.UNPERFORM
				.name());

		// 结算状态
		orderItem
				.setSettlementStatus(OrderEnum.ORDER_SETTLEMENT_STATUS.UNSETTLEMENTED
						.name());

		// 信息状态-未确认
		orderItem.setInfoStatus(OrderEnum.INFO_STATUS.UNVERIFIED.name());

		// 品类code
		orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.categoryCode.name(),
				suppGoods.getProdProduct().getBizCategory().getCategoryCode());

		// 添加子订单流程key
		orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(),
				suppGoods.getProdProduct().getBizCategory().getProcessKey());

		// 供应商标识
		orderItem.putContent(
				OrderEnum.ORDER_COMMON_TYPE.supplierApiFlag.name(),
				suppGoods.getApiFlag());

		// order.putApiFlag(suppGoods.getSuppGoodsId(),"Y".equals(suppGoods.getApiFlag()));

		String branchName = suppGoods.getProdProductBranch().getBranchName();

		ResultHandleT<BizBranch> branch = branchClientService
				.findBranchById(orderItem.getSuppGoods().getProdProductBranch()
						.getBranchId());
		if (branch == null || branch.hasNull()) {
			throwNullException("规格不存在");
		}
		orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.branchAttachFlag
				.name(), branch.getReturnContent().getAttachFlag());
		orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.branchCode.name(),
				branch.getReturnContent().getBranchCode());

		// 传真规则
		if (suppGoods.getFaxRuleId() != null) {
			orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fax_rule.name(),
					suppGoods.getFaxRuleId());
		}

		// 是否使用传真
		if (suppGoods.getFaxFlag() != null) {
			orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.fax_flag.name(),
					suppGoods.getFaxFlag());
		}

		orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.branchName.name(),
				branchName.trim());

		orderItem.setNeedResourceConfirm("false");

		// 冗余下单时间
		orderItem.setCreateTime(order.getCreateTime());
		orderItem.setOrderUpdateTime(order.getOrderUpdateTime());
		//冗余产品打包方式
		ResultHandleT<ProdProduct> resultHandle = prodProductClientService.findProdProductById(orderItem.getProductId());
		if(resultHandle.isSuccess()){
			ProdProduct product = resultHandle.getReturnContent();
			if(StringUtil.isNotEmptyString(product.getPackageType())){
				orderItem.setPackageType(product.getPackageType());
			}
		}
	}

	@Override
	public ResultHandle validate(SuppGoods suppGoods, Item item,
			OrdOrderItemDTO orderItem, OrdOrderDTO order)
			throws BusinessException {
		ResultHandle handle = new ResultHandle();
		// 查询时间价格表
	ResultHandleT<NewHotelCombTimePrice> timePriceHandle = newHotelCombTimePriceClientRemote
			.findSuppGoodsTimePriceBySuppGoodsIdAndSpecDate(
					suppGoods.getSuppGoodsId(), orderItem.getVisitTime());
	//	NewHotelCombTimePrice   newHotelCombTimePrice1 =new NewHotelCombTimePrice();
	//	newHotelCombTimePrice1.setBookLimitType("");
	//	newHotelCombTimePrice1.setPrice(100L);
	//	newHotelCombTimePrice1.setSalePrice(120L);
	//	newHotelCombTimePrice1.setPricePlanId(11L);
	//	newHotelCombTimePrice1.setAheadBookTime(1000L);
	//	newHotelCombTimePrice1.setSalePrice(100L);
	//	SuppGoodsTimeStock suppGoodsTimeStock  = new SuppGoodsTimeStock();
	//	suppGoodsTimeStock.setOnsaleFlag(1);
	//	suppGoodsTimeStock.setStock(20L);
	//	suppGoodsTimeStock.setOversellFlag(1);
	//	suppGoodsTimeStock.setStockType("CONTROL");
	//	newHotelCombTimePrice1.setRouteId(10422L);
	//	newHotelCombTimePrice1.setSuppGoodsTimeStock(suppGoodsTimeStock);
	//	ResultHandleT<NewHotelCombTimePrice>   timePriceHandle = new ResultHandleT<NewHotelCombTimePrice>();
		//timePriceHandle.setReturnContent(newHotelCombTimePrice1);
		if (timePriceHandle == null || timePriceHandle.hasNull()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(
						"getTimePriceAndCheck(SuppGoods, BuyInfo.Item, Date) - timePriceHandle == null||timePriceHandle.hasNull(),suppGoodsId={},visitTime={}", new Object[] { suppGoods.getSuppGoodsId(), orderItem.getVisitTime() }); //$NON-NLS-1$
			}

			throw new BusinessException(ErrorCodeMsg.ERR_TIMEPRICE_006);
		}
		NewHotelCombTimePrice newHotelCombTimePrice = timePriceHandle
				.getReturnContent();
	
		
		
		// 判断商品是否可售
		checkOnsaleFlag(newHotelCombTimePrice);
		// 结算价格
		orderItem.setSettlementPrice(newHotelCombTimePrice.getPrice());
		// 实际结算价格
		orderItem.setActualSettlementPrice(newHotelCombTimePrice.getPrice());
		orderItem.setPrice(newHotelCombTimePrice.getSalePrice());		//判断是否有买断价格
		//商品的（编辑时设置的）库存类型
       String  stockTypeBig = suppGoods.getStockType();
      // stockTypeBig="ALONE_STOCK";
		//
		// 订购数量 校验
		checkParam(suppGoods, item);
		// 初始化子订单商品扣存记录表
		List<OrdOrderStock> orderStockList = new ArrayList<OrdOrderStock>();
		Long routeId = newHotelCombTimePrice.getRouteId();
		if (routeId == null || routeId == 0) {
			throw new BusinessException("该商品没有设置行程");
		}

		ResultHandleT<ProdLineRoute> result = prodLineRouteClientRemote
				.findByProdLineRouteId(routeId);
		if (result == null && result.getReturnContent() == null) {
			throw new BusinessException("行程不存在或者无效");
		}
		// 设置行程
		order.setLineRouteId(routeId);

		// 酒店时间价格表
		List<SuppGoodsTimePrice> suppGoodsTimePriceList = newHotelCombTimePrice
				.getSuppGoodsTimePrice();
		Long quantities = orderItem.getQuantity();
		if(suppGoodsTimePriceList!=null && suppGoodsTimePriceList.size()>0){
		for (SuppGoodsTimePrice suppGoodTimePrice : suppGoodsTimePriceList) {
			String saleOnFlag = suppGoodTimePrice.getOnsaleFlag();// 是否可售
			String overFlag = suppGoodTimePrice.getOversellFlag();// 是否超卖
			String stockFlag = suppGoodTimePrice.getStockFlag();// 是否是保留房和非保留房
			// 共享库存
			if (SuppGoods.BIZ_STOCK_TYPE.SHARE_STOCK.name().equals(stockTypeBig)) {
				// 酒店商品是否可售

				if ("1".equals(saleOnFlag)) {
					// 是保留房并且不可超卖
					if (("1".equals(stockFlag)) && "0".equals(overFlag)) {
						
						if (quantities < suppGoodTimePrice.getStock()) {
							
							OrdOrderStock stock = createStock(
									suppGoodTimePrice.getSpecDate(),
									orderItem.getQuantity());
							// 资源不需要确认
							makeNotNeedResourceConfirm(stock);
						 
							orderStockList.add(stock);
						} else {
							throw new BusinessException("库存不足");
						}
						// 保留房并且可超卖
					} else if (("1".equals(stockFlag)) && "1".equals(overFlag)) {
						if (quantities < suppGoodTimePrice.getStock()) {
							OrdOrderStock stock = createStock(
									suppGoodTimePrice.getSpecDate(),
									orderItem.getQuantity());
							// 资源不需要确认
							makeNotNeedResourceConfirm(stock);
							orderStockList.add(stock);

						} else {
							// 保留房可超卖 无库存下单
							OrdOrderStock stock = createStock(
									suppGoodTimePrice.getSpecDate(),
									suppGoodTimePrice.getStock());
							makeNotNeedResourceConfirm(stock);
							orderStockList.add(stock);
						}
						// 非保留房
					} else if ("0".equals(stockFlag)) {
						OrdOrderStock stock = createStock(
								suppGoodTimePrice.getSpecDate(),
								orderItem.getQuantity());
						makeNeedResourceConfirm(stock);
						orderStockList.add(stock);
					}
				} else {
					throw new BusinessException("商品不可售");

				}
				// 非共享库存
			} 
		 }
		}
		
		if(SuppGoods.BIZ_STOCK_TYPE.ALONE_STOCK.name().equals(stockTypeBig)) {
			String stockType = newHotelCombTimePrice
					.getSuppGoodsTimeStock().getStockType();
			Integer saleOnFlag = newHotelCombTimePrice.getSuppGoodsTimeStock().getOnsaleFlag();// 是否可售
			Integer overFlag = newHotelCombTimePrice.getSuppGoodsTimeStock().getOversellFlag();
		//	String stockType = newHotelCombTimePrice.getSuppGoodsTimeStock().getStockType();
			// 切位库存
			if (SuppGoodsLineTimePrice.STOCKTYPE.CONTROL.name()
					.equalsIgnoreCase(stockType)) {
				// 可超卖
				if (1==overFlag) {
					if ( newHotelCombTimePrice.getSuppGoodsTimeStock().getStock()>=quantities ) {
						OrdOrderStock stock = createStock(
								newHotelCombTimePrice.getSuppGoodsTimeStock().getSpecDate(),
								orderItem.getQuantity());
						// 资源不需要确认
						makeNotNeedResourceConfirm(stock);
						orderStockList.add(stock);

					} else if (quantities > newHotelCombTimePrice.getSuppGoodsTimeStock().getStock()) {
						OrdOrderStock stock = createStock(
								newHotelCombTimePrice.getSuppGoodsTimeStock().getSpecDate(),
								newHotelCombTimePrice.getSuppGoodsTimeStock().getStock());
						// 资源不需要确认
						makeNotNeedResourceConfirm(stock);
						orderStockList.add(stock);
					}
				}
				// 不可超卖
				else {
					if (newHotelCombTimePrice.getSuppGoodsTimeStock().getStock()>=quantities ) {
						OrdOrderStock stock = createStock(
								newHotelCombTimePrice.getSuppGoodsTimeStock().getSpecDate(),
								orderItem.getQuantity());
						// 资源不需要确认
						makeNotNeedResourceConfirm(stock);
						orderStockList.add(stock);
					} else {
						throw new BusinessException("库存不足");
					}
				}
				// 现询
			} else if (SuppGoodsLineTimePrice.STOCKTYPE.INQUIRE_NO_STOCK
					.name().equalsIgnoreCase(stockType)) {
				OrdOrderStock stock = createStock(
						newHotelCombTimePrice.getSuppGoodsTimeStock().getSpecDate(),
						orderItem.getQuantity());
				makeNeedResourceConfirm(stock);
				orderStockList.add(stock);
			}
		}
		makeNeedResourceConfirm(orderItem, orderStockList);
		makeOrderItemTime(orderItem,newHotelCombTimePrice);
		orderItem.setOrderStockList(orderStockList);
		

		return handle;
	}
	
	private void makeOrderItemTime(OrdOrderItem item,
			NewHotelCombTimePrice newHotelCombTimePrice ) {
		/*if(item.getVisitTime()==null||newHotelCombTimePrice.getSpecDate().before(item.getVisitTime())){
			item.setVisitTime(newHotelCombTimePrice.getSpecDate());
		}*/
		if(newHotelCombTimePrice.getAheadBookTime()!=null){
			Date aheadTime = DateUtils.addMinutes(item.getVisitTime(),-newHotelCombTimePrice.getAheadBookTime().intValue());
			if(item.getAheadTime() == null||aheadTime.before(item.getAheadTime())){
				item.setAheadTime(aheadTime);
			}
		}
//		if(timePrice.getLatestCancelTime()!=null){
//			Date lastCancelTime = DateUtils.addMinutes(timePrice.getSpecDate(), -timePrice.getLatestCancelTime().intValue());
//			if(item.getLastCancelTime()==null||lastCancelTime.before(item.getLastCancelTime())){
//				item.setLastCancelTime(lastCancelTime);
//			}
//		}
	}
	
	
	private long doCalcPriceInfo(SuppGoods suppGoods, Item item,
			OrdOrderItem orderItem, SuppGoodsLineTimePrice lineTimePrice) {
		long settlementPrice = getSettlementPriceTypeValue(suppGoods, lineTimePrice, OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name());
		long price = getPriceTypeValue(suppGoods, lineTimePrice, OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT.name());
		lineTimePrice.setBakPrice(settlementPrice);
		/** 开始资源预控买断价格  **/
		Long precontrolSettlePrice = null;
		Long precontrolSalePrice = null;
		List<ResPreControlTimePriceVO> resPriceList = null;
		long buyoutTotalPrice = 0;
		long notBuyoutTotalPrice = 0;
		Long leftMoney = null;
		long buyoutNum = 0;
		SuppGoods goods = orderItem.getSuppGoods();
		Long goodsId = goods.getSuppGoodsId();
		Date visitDate = orderItem.getVisitTime();
		GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO=new GoodsResPrecontrolPolicyVO();
		boolean hasControled=false;
		List<PresaleStampTimePrice> presales=new ArrayList<PresaleStampTimePrice>();

		
		
		if(!OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(item.getOrderSubType())){
			//通过商品Id和游玩时间获取，该商品在该时间，所在的预控策略对象
			 goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
			//如果能找到该有效预控的资源
			 hasControled = goodsResPrecontrolPolicyVO != null && goodsResPrecontrolPolicyVO.isControl();
			 LOG.info("vst_order===goodsResPrecontrolPolicyVO==="+ GsonUtils.toJson(goodsResPrecontrolPolicyVO));
		}
		
		if(hasControled ){
			resPriceList = resControlBudgetRemote.queryPreControlTimePriceByParam(orderItem.getVisitTime(),orderItem.getCategoryId(), orderItem.getSuppGoodsId());
			if(resPriceList==null || (resPriceList!=null && resPriceList.size()<=0)){
				hasControled = false;
			}else{
				LOG.info("***资源预控***");
				LOG.info("酒店套餐：" + orderItem.getSuppGoodsId() + "存在预控资源");
			}
			if(resPriceList!=null && resPriceList.size()>0){
				
				
				precontrolSettlePrice = getPrecontrolSettlementPriceTypeValue(suppGoods,resPriceList,OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENTPRICE_PRE.name());
				precontrolSalePrice = getPrecontrolPriceTypeValue(suppGoods,resPriceList,OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_PRE.name());
				if(precontrolSettlePrice!=null ){
					settlementPrice = precontrolSettlePrice.longValue();
				}
				if(precontrolSalePrice!=null ){
					price = precontrolSalePrice.longValue();
				}
			}
		}
		/** end **/
		orderItem.setSettlementPrice(settlementPrice);
		orderItem.setPrice(price);
//		if(detailAddPrice != null) {
//			fillPackageOrderItemPrice(orderItem, detailAddPrice);
//		} else if(detail != null){//针对打包重新计算单价
//			fillPackageOrderItemPrice(orderItem, detail);
//		}
		orderItem.setQuantity((long)item.getQuantity());
		//如果是券
		if(OrderEnum.ORDER_STAMP.STAMP_PROD.name().equalsIgnoreCase(item.getOrderSubType())){
			Map<String,Object> map =new HashMap<String, Object>();
			map.put("goodsId", goodsId);
			map.put("applyDate", visitDate);
			presales=suppGoodsTimePriceClientService.selectPresaleStampTimePrices(map);
		LOG.info("酒店套餐的预售结算价是-----------------------------------------------------------------"+presales.get(0).getValue());
			//只修改结算价，不修改销售价格
			settlementPrice=presales.get(0).getValue();
			orderItem.setSettlementPrice(settlementPrice);
		}
		//设置买断的数量和总价
		if(hasControled){
			String preControlType = goodsResPrecontrolPolicyVO.getControlType();
			if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equals(preControlType)){
				//记录买断和非买断的结算总额
				if(leftMoney==null ){
					leftMoney = goodsResPrecontrolPolicyVO.getLeftAmount().longValue() ;
				}
				long shouldSettleTotalPrice = orderItem.getQuantity()*settlementPrice;
				if(shouldSettleTotalPrice>leftMoney&& leftMoney>0){
					buyoutNum = (long) Math.ceil(leftMoney/orderItem.getSettlementPrice().doubleValue());
					//买断+非买断
					buyoutTotalPrice = buyoutTotalPrice + buyoutNum *settlementPrice;
					long notBuyNum = (orderItem.getQuantity() - buyoutNum);
					if(notBuyNum>0){
						notBuyoutTotalPrice = notBuyoutTotalPrice + notBuyNum* lineTimePrice.getBakPrice();
					}
				}else if(shouldSettleTotalPrice<=leftMoney){
					buyoutNum = orderItem.getQuantity();
					//买断
					buyoutTotalPrice = buyoutTotalPrice + shouldSettleTotalPrice;
				}
				orderItem.setBuyoutQuantity(buyoutNum);
				orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
				orderItem.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
				leftMoney = leftMoney - shouldSettleTotalPrice;
				orderItem.setBuyoutPrice((long)orderItem.getBuyoutTotalPrice()/orderItem.getBuyoutQuantity());
				
				
			}else if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.inventory.name().equals(preControlType)){
				//记录买断的库存，以及各自的结算总额
				long roomNum = 0;
				if(orderItem.getQuantity()!=null ){
					roomNum = orderItem.getQuantity().longValue();
				}
				long leftQuantity = 0;
				if(goodsResPrecontrolPolicyVO.getLeftNum()!=null){
					leftQuantity = goodsResPrecontrolPolicyVO.getLeftNum().longValue();
				}
				long buyoutsaledNum = 0;
				if(orderItem.getBuyoutQuantity()!=null ){
					buyoutsaledNum = orderItem.getBuyoutQuantity().longValue();
				}
				if(roomNum>leftQuantity){
					orderItem.setBuyoutQuantity(buyoutsaledNum + leftQuantity);
					buyoutTotalPrice = buyoutTotalPrice + leftQuantity*precontrolSettlePrice;
					notBuyoutTotalPrice = notBuyoutTotalPrice + (lineTimePrice.getBakPrice() * (roomNum-leftQuantity));
					//酒店设置非买断的总价
					orderItem.setNotBuyoutSettleAmout(notBuyoutTotalPrice);
					//设置买断的总价
					orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
				}else{
					orderItem.setBuyoutQuantity(buyoutsaledNum + roomNum);
					buyoutTotalPrice = buyoutTotalPrice + roomNum*precontrolSettlePrice;
					orderItem.setBuyoutTotalPrice(buyoutTotalPrice);
				}
				
				orderItem.setBuyoutPrice((long)orderItem.getBuyoutTotalPrice()/orderItem.getBuyoutQuantity());
			}
			orderItem.setBuyoutFlag("Y");
			orderItem.setNebulaProjectId(goodsResPrecontrolPolicyVO.getNebulaProjectId());
		}
		
		
		
		orderItem.setActualSettlementPrice(orderItem.getSettlementPrice());
		orderItem.setTotalSettlementPrice(orderItem.getSettlementPrice()*orderItem.getQuantity());
		
		return orderItem.getQuantity();
	}
	protected Long getSettlementPriceTypeValue(SuppGoods suppGoods,SuppGoodsLineTimePrice timePrice, String priceKey){
		OrderEnum.ORDER_PRICE_RATE_TYPE linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.valueOf(priceKey);
		if(linePriceType==null){
			throwIllegalException("价格类型错误");
		}
		if(SuppGoods.PRICETYPE.SINGLE_PRICE.name().equalsIgnoreCase(suppGoods.getPriceType())){
			linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT;
		}

		Long value=0L;
		switch (linePriceType) {
		case PRICE_ADULT:
			value = timePrice.getAuditSettlementPrice();
			break;
		case PRICE_CHILD:
			value = timePrice.getChildSettlementPrice();
			break;
		case PRICE_SPREAD:
			value = timePrice.getGrapSettlementPrice();
			break;
		default:
			break;
		}
		if(value==null){
			throwIllegalException("时间价格表禁售");
		}
		return value;
	}
	protected Long getPriceTypeValue(SuppGoods suppGoods,SuppGoodsLineTimePrice timePrice, String priceKey){
		OrderEnum.ORDER_PRICE_RATE_TYPE linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.valueOf(priceKey);
		if(linePriceType==null){
			throwIllegalException("价格类型错误");
		}
		if(SuppGoods.PRICETYPE.SINGLE_PRICE.name().equalsIgnoreCase(suppGoods.getPriceType())){
			linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT;
		}
		Long value=0L;
		switch (linePriceType) {
		case PRICE_ADULT:
			value = timePrice.getAuditPrice();
			break;
		case PRICE_CHILD:
			value = timePrice.getChildPrice();
			break;
		case PRICE_SPREAD:
			value = timePrice.getGapPrice();
			break;
		default:
			break;
		}
		if(value==null){
			throwIllegalException("时间价格表禁售");
		}
		return value;
	}
	protected Long getPrecontrolSettlementPriceTypeValue(SuppGoods suppGoods,List<ResPreControlTimePriceVO> resPriceList, String priceKey){
		OrderEnum.ORDER_PRICE_RATE_TYPE linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.valueOf(priceKey);
		if(linePriceType==null){
			throwIllegalException("价格类型错误");
		}
		if(suppGoods.getCategoryId().intValue()!=17 &&SuppGoods.PRICETYPE.SINGLE_PRICE.name().equalsIgnoreCase(suppGoods.getPriceType())){
			linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE;
		}
		if(suppGoods.getCategoryId()!=null && suppGoods.getCategoryId().equals(17)){
			linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENTPRICE_PRE;
		}
		Long value=null;
		switch (linePriceType) {
		case PRICE_ADULT:
			for(int i=0,j=resPriceList.size();i<j;i++){
				ResPreControlTimePriceVO timePrice = resPriceList.get(i);
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_ADULT_PRE.name().equals(timePrice.getPreControlTimePriceAttrCode())){
					value = timePrice.getValue();
				}
			}
			
			break;
		case PRICE_CHILD:
			for(int i=0,j=resPriceList.size();i<j;i++){
				ResPreControlTimePriceVO timePrice = resPriceList.get(i);
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_CHILD_PRE.name().equals(timePrice.getPreControlTimePriceAttrCode())){
					value = timePrice.getValue();
				}
			}
			break;
		case PRICE_SPREAD:
			for(int i=0,j=resPriceList.size();i<j;i++){
				ResPreControlTimePriceVO timePrice = resPriceList.get(i);
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENT_SPREAD_PRE.name().equals(timePrice.getPreControlTimePriceAttrCode())){
					value = timePrice.getValue();
				}
			}
			break;
		//酒店套餐
		case SETTLEMENTPRICE_PRE:
			for(int i=0,j=resPriceList.size();i<j;i++){
				ResPreControlTimePriceVO timePrice = resPriceList.get(i);
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.SETTLEMENTPRICE_PRE.name().equals(timePrice.getPriceClassificationCode())){
					value = timePrice.getValue();
				}
			}
			
			break;
		default:
			break;
		}
		if(value==null){
			throwIllegalException("时间价格表禁售");
		}
		return value;
	}
	protected Long getPrecontrolPriceTypeValue(SuppGoods suppGoods,List<ResPreControlTimePriceVO> resPriceList, String priceKey){
		OrderEnum.ORDER_PRICE_RATE_TYPE linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.valueOf(priceKey);
		if(linePriceType==null){
			throwIllegalException("价格类型错误");
		}
		if(suppGoods.getCategoryId().intValue()!=17 && SuppGoods.PRICETYPE.SINGLE_PRICE.name().equalsIgnoreCase(suppGoods.getPriceType())){
			linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT_PRE;
		}
		if(suppGoods.getCategoryId()!=null && suppGoods.getCategoryId().equals(17)){
			linePriceType = OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_PRE;
		}
		Long value=null;
		switch (linePriceType) {
		case PRICE_ADULT:
			for(int i=0,j=resPriceList.size();i<j;i++){
				ResPreControlTimePriceVO timePrice = resPriceList.get(i);
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_ADULT_PRE.name().equals(timePrice.getPreControlTimePriceAttrCode())){
					value = timePrice.getValue();
				}
			}
			break;
		case PRICE_CHILD:
			for(int i=0,j=resPriceList.size();i<j;i++){
				ResPreControlTimePriceVO timePrice = resPriceList.get(i);
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_CHILD_PRE.name().equals(timePrice.getPreControlTimePriceAttrCode())){
					value = timePrice.getValue();
				}
			}
			break;
		case PRICE_SPREAD:
			for(int i=0,j=resPriceList.size();i<j;i++){
				ResPreControlTimePriceVO timePrice = resPriceList.get(i);
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_SPREAD_PRE.name().equals(timePrice.getPreControlTimePriceAttrCode())){
					value = timePrice.getValue();
				}
			}
			break;
		//酒店套餐
		case PRICE_PRE:
			for(int i=0,j=resPriceList.size();i<j;i++){
				ResPreControlTimePriceVO timePrice = resPriceList.get(i);
				if(OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_PRE.name().equals(timePrice.getPriceClassificationCode())){
					value = timePrice.getValue();
				}
			}
			break;
		default:
			break;
		}
		if(value==null){
			throwIllegalException("时间价格表禁售");
		}
		return value;
	}/**
	*校验参数
	*/
	private void checkOnsaleFlag(NewHotelCombTimePrice timePrice)
			throws BusinessException {
		if (0 == (timePrice.getOnsaleFlag())) {
			throw new BusinessException("商品游玩日期不可售");
		}
	}

	private void checkParam(SuppGoods suppGoods, Item item)
			throws BusinessException {

		if (suppGoods == null) {
			throw new BusinessException("商品ID=" + item.getGoodsId() + "不存在");
		}

		if (item.getQuantity() <= 0) {
			throw new BusinessException("商品 " + suppGoods.getGoodsName()
					+ " 订购数量小于等于零");
		}

		if ((null != suppGoods.getMaxQuantity())
				&& (item.getQuantity() > suppGoods.getMaxQuantity())) {
			// throw new IllegalArgumentException("商品 " +
			// suppGoods.getGoodsName() + " 订购数量超出最大值");
			throw new BusinessException(
					OrderStatusEnum.ORDER_ERROR_CODE.OUT_MAXIMUM_DAY
							.getErrorCode(),
					"商品 " + suppGoods.getGoodsName() + " 订购数量超出最大值");
		}

		if ((null != suppGoods.getMinQuantity())
				&& (item.getQuantity() < suppGoods.getMinQuantity())) {
			throw new BusinessException("商品 " + suppGoods.getGoodsName()
					+ " 订购数量小于最小值");
		}

		if (item.getOwnerQuantity() > item.getQuantity()) {
			throw new BusinessException("商品" + suppGoods.getGoodsName()
					+ "  实际订购数量小于零");

		}
	}

	private OrdOrderStock createStock(Date visitTime, long quantity) {
		OrdOrderStock stock = new OrdOrderStock();
		stock.setQuantity(quantity);
		// stock.setInventory(OrderEnum.INVENTORY_STATUS.UNINVENTORY.name());
		stock.setVisitTime(visitTime);
		// stock.setNeedResourceConfirm("true");
		// stock.setResourceStatus(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name());
		return stock;
	}

	/**
	 * 不需要资源确认
	 * 
	 * @param stock
	 */
	private void makeNotNeedResourceConfirm(final OrdOrderStock stock) {
		stock.setNeedResourceConfirm("false");
		stock.setInventory(OrderEnum.INVENTORY_STATUS.INVENTORY.name());
		stock.setResourceStatus(OrderEnum.RESOURCE_STATUS.AMPLE.name());
	}

	/**
	 * 需要资源审核的库存项
	 * 
	 * @param stock
	 */
	private void makeNeedResourceConfirm(final OrdOrderStock stock) {
		stock.setNeedResourceConfirm("true");
		stock.setInventory(OrderEnum.INVENTORY_STATUS.UNINVENTORY.name());
		stock.setResourceStatus(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name());
	}

	private void makeNeedResourceConfirm(OrdOrderItem orderItem,
			List<OrdOrderStock> stockList) {
		for (OrdOrderStock stock : stockList) {
			setOrderItemsNeedResourceConfirm(stock.getNeedResourceConfirm(),
					orderItem);
		}
	}

	/**
	 * 设置订单那子项是否需要资源确认
	 * 
	 * @param needResourceConfirm
	 * @param orderItem
	 */
	private void setOrderItemsNeedResourceConfirm(String needResourceConfirm,
			OrdOrderItem orderItem) {
		if ("true".equals(orderItem.getNeedResourceConfirm())) {
			orderItem.setNeedResourceConfirm(needResourceConfirm);
		}
	}

	/**
	 * 初始化订单电子合同
	 */
	public void initOrdTravelContract(OrdOrderDTO order) throws BusinessException  {

	
					// 订单所有合同集合
			List<OrdTravelContract> contracts = new ArrayList<OrdTravelContract>();
			Long distributorId = order.getDistributorId();// 销售渠道ID

			 createOrderTravelContract(ELECTRONIC_CONTRACT_TEMPLATE.DEST_COMMISSIONED_SERVICE_AGREEMENT.getCode(), contracts, order.getOrderItemList(),distributorId); 
         	if (CollectionUtils.isNotEmpty(contracts)) {
				LOG.info("contracts size = " + contracts.size());
			} else {
			    throw new BusinessException("初始化电子合同出现异常");
			}
			// 订单合同
			order.setOrdTravelContractList(contracts);
		
	}
	
	/**
	 *初始化订单合同信息
	 * @param distributorId 
	 */
	public void createOrderTravelContract(String template,List<OrdTravelContract> contracts,List<OrdOrderItem> orderItemList, Long distributorId){
		OrdTravelContract travel = new OrdTravelContract();
	 	travel.setContractTemplate(template);
	 	travel.setCreateTime(new Date());
	 	if (distributorId!=null && distributorId==10) {
		 	travel.setSigningType(ORDER_CONTRACT_SIGNING_TYPE.BRANCHES.getCode());
		}else {
		 	travel.setSigningType(ORDER_CONTRACT_SIGNING_TYPE.ONLINE.getCode());
		}
	 	travel.setStatus(ORDER_TRAVEL_CONTRACT_STATUS.UNSIGNED.getCode());
	 	travel.setOrderItems(orderItemList);
	 	contracts.add(travel);
	}
	
	/**
	 * 计算订单奖金抵扣相关
	 */
	private void calcBonus(OrdOrderDTO order,DestBuBuyInfo destbuyInfo){

		if(destbuyInfo!=null){
			String youhuiType = destbuyInfo.getYouhui();
			if(StringUtils.isNotEmpty(youhuiType)&&ORDER_FAVORABLE_TYPE.bonus.getCode().equals(youhuiType)){
				Float bonusYuan = destbuyInfo.getBonusYuan();
				if(bonusYuan!=null){
					order.setBonusAmount(PriceUtil.convertToFen(bonusYuan));
				}
			}
		}
		
	}

	private  void calcBlackList(OrdOrderDTO order) {
		String err = blackListBussiness.isBlackList(order);
		if (StringUtils.isNotEmpty(err)) {
			throwIllegalException(err);
		}
	}
	
	private void calcWorkflow(OrdOrderDTO order){
		LOG.info("ActivitiAble="+Constant.getInstance().isActivitiAble());
		if (Constant.getInstance().isActivitiAble()) {

		
				if (order.hasNeedPrepaid()) {
					   if(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId()==order.getCategoryId()){
						order.setProcessKey("destbu_order_prepaid_main");
						calcDestBuSubWorkflow(order);
					   }
				} else {
					order.setProcessKey("order_pay_main_process");
				}
			
		}
		
	}
	/**
	 * 计算目的地BU子订单流程 (酒店或酒店套餐)
	 * <br/>PS:
	 * @param order
	 */
	private void calcDestBuSubWorkflow(OrdOrderDTO order) {

			for (OrdOrderItem orderItem : order.getOrderItemList()) {
				 if (BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId()==orderItem.getCategoryId()) {// 酒店套餐子单
					orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "destbu_hotelcomb");
				}else if (BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId() == orderItem.getCategoryId()) {// 酒店子单
					orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "destbu_hotel");
				}else if (BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId() == orderItem.getCategoryId()) {// 酒店套餐子单
					orderItem.putContent(OrderEnum.ORDER_COMMON_TYPE.processKey.name(), "destbu_hotelcomb");
				}
			}
		
	}

	

	


	public String checkNewHotelComIsUseCoupon(OrdOrderDTO ordOrderDTO,DestBuBuyInfo destBuBuyInfo, OrdOrderItem ordMainItem,ProdProduct product) {
		Log.info("开始检查酒套餐是否可使用优惠券");
		Long catageryId = ordOrderDTO.getCategoryId();
		Long goodsId = null;
		Long productId = null;
		
		if(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(catageryId)){
		    if(null == product){
                //List<Product> productList = buyinfo.getProductList();destBuBuyInfo.getProductId();
                //if(CollectionUtils.isNotEmpty(productList)){
                    productId = destBuBuyInfo.getProductId();
                    ResultHandleT<ProdProduct> restult = prodProductClientService.findProdProductByIdFromCache(productId);
                    if(null != restult){
                        product = restult.getReturnContent();
                        productId = product.getProductId();
                    }else{
                        if(null != ordMainItem){
                            goodsId=ordMainItem.getSuppGoodsId();
                        }
                    }
                //}
            }else{
                productId = product.getProductId();
            }
		}

		if(null != ordMainItem){
			goodsId=ordMainItem.getSuppGoodsId();
		}
		Log.info("========================GOODSID:"+goodsId);

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

	public List<CouponCheckParam> couponCheckParams(OrdOrderDTO order,DestBuBuyInfo destBuBuyInfo) {
		List<CouponCheckParam> couponCheckParams = new ArrayList<CouponCheckParam>();
		ProdProductParam param = new ProdProductParam();
		param.setProductProp(true);
		param.setProductBranchValue(true);
		param.setProdEcontract(true);
		CouponCheckParam couponCheckParam = null;
		List<OrdOrderItem> ordItemList = order.getOrderItemList();
		for (OrdOrderItem ordOrderItem : ordItemList) {
			couponCheckParam = new CouponCheckParam();
			couponCheckParam.setUserId(destBuBuyInfo.getUserNo());
			couponCheckParam.setCategoryId(order.getCategoryId());
			couponCheckParam.setPlatform("VST");
			couponCheckParam.setSaleUnitType("PROD");
			couponCheckParam.setSaleUnitId(destBuBuyInfo.getProductId());
			ResultHandleT<ProdProduct> resultHandle = prodProductClientService.findLineProductByProductId(destBuBuyInfo.getProductId(), param);
			ProdProduct product = resultHandle.getReturnContent();
			couponCheckParam.setProductType(product.getProductType());
			//wifi设置产品类型为空，暂时解决wifi品类绑定问题
			if(OrderUtil.isWifiCategory(ordOrderItem)){
				couponCheckParam.setProductType(null);
			}
			changeCouponCheckParamAddBu(couponCheckParam,order);
			couponCheckParams.add(couponCheckParam);
			String json = JSONUtil.bean2Json(couponCheckParam);
			LOG.info("优惠券接口参数6######******PROD couponCheckParam json:"+json+"#######");
		}
		for (OrdOrderItem ordOrderItem : ordItemList) {
			couponCheckParam = new CouponCheckParam();
			couponCheckParam.setUserId(destBuBuyInfo.getUserNo());
			couponCheckParam.setCategoryId(ordOrderItem.getSuppGoods().getCategoryId());
			couponCheckParam.setPlatform("VST");
			couponCheckParam.setSaleUnitType("BRANCH");
			couponCheckParam.setSaleUnitId(ordOrderItem.getSuppGoodsId());
			couponCheckParam.setProductType(ordOrderItem.getSuppGoods().getProdProduct().getProductType());
			//wifi设置产品类型为空，暂时解决wifi品类绑定问题
			if(OrderUtil.isWifiCategory(ordOrderItem)){
				couponCheckParam.setProductType(null);
			}
			changeCouponCheckParamAddBu(couponCheckParam,order);
			couponCheckParams.add(couponCheckParam);
			String json = JSONUtil.bean2Json(couponCheckParam);
			LOG.info("优惠券接口参数7######******BRANCH couponCheckParam json:"+json+"#######");
		}
		return couponCheckParams;

	}
	
	private void changeCouponCheckParamAddBu(CouponCheckParam  couponCheckParam,OrdOrderDTO orderDto){
		String bu = orderDto.getBuCode();
		if(StringUtils.isNotBlank(bu)){
			couponCheckParam.setBu(bu);
		}
	}

	@Override
	public List<UserCouponVO> getUserCouponVOList(DestBuBuyInfo destBuBuyInfo,OrdOrderDTO ordOrderDTO) {
		//将buyInfo转化为order对象

		LOG.info("插入...getLoginUserAccountInformation下单方法中接收到的JSON"+destBuBuyInfo.toJsonStr());

		//OrdOrderDTO ordOrderDTO = this.initOrderAndCalc(destBuBuyInfo, order);
		//Long categoryId = getCategoryIdByBuyInfo(destBuBuyInfo,ordOrderDTO);//获取产品的品类Id
		
		Long categoryId = ordOrderDTO.getCategoryId();
		OrdOrderItem orderMainItem = null;
		Long orderAmount = ordOrderDTO.getOughtAmount();//存储订单的总金额（除去保险和快递的金额）
		Long quantity = Long.valueOf(destBuBuyInfo.getQuantity());
		for (OrdOrderItem orderItem : ordOrderDTO.getOrderItemList()){
			if(quantity==0L && "true".equals(orderItem.getMainItem())){
				if(null != orderItem.getQuantity() && 0L!=orderItem.getQuantity()){
					quantity = orderItem.getQuantity();
					orderMainItem = orderItem;
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
		if( BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb.getCategoryId().equals(categoryId))	{

			ProdProduct product = null;
			if(null != destBuBuyInfo.getProductId()){
//						ResultHandleT<ProdProduct> restult = prodProductClientService.findProdProductById(buyInfo.getProductId());
				ResultHandleT<ProdProduct> restult = prodProductClientService.findProdProductByIdFromCache(destBuBuyInfo.getProductId());
				if(null != restult && null != restult.getReturnContent()){
					product = restult.getReturnContent();
				}
			}
			//判断是否可使用优惠券
			String icCoupon = this.checkNewHotelComIsUseCoupon(ordOrderDTO,destBuBuyInfo, orderMainItem, product);
			if("N".equalsIgnoreCase(icCoupon)){
				UserCouponVO userCoupon = new UserCouponVO();
				userCoupon.setValidInfo("本产品不支持使用优惠券!");
				List<UserCouponVO> userCouponList = new ArrayList<UserCouponVO>();
				userCouponList.add(userCoupon);
				return userCouponList;
			}
		}
		//优惠券券号
		List<UserCouponVO> couponCodeVOList = destBuBuyInfo.getUserCouponVoList();
		Long userId = destBuBuyInfo.getUserNo();
		//优惠券验证参数		
		List<CouponCheckParam> couponCheckParams = this.couponCheckParams(ordOrderDTO, destBuBuyInfo);
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
	
	//促销计算
	
	public void 	calcPromition(OrdOrderDTO order){
		if(MapUtils.isNotEmpty(order.getPromotionMap())){
			long discountAmount=0;
			//支付渠道
			String paymentChannel=null;
			for(String key:order.getPromotionMap().keySet()){
				List<OrdPromotion> list = order.getPromotionMap().get(key);
				for(OrdPromotion op:list){
					if(op.getPromFavorable().hasApplyAble()){
						long amount = op.getPromFavorable().getDiscountAmount();
						discountAmount+=amount;
						op.setFavorableAmount(amount);
						//orderSaveService.setOrdPromotionFavorableAmount(op);
						if(Constant.ACTIVITY_TYPE.ORDERCHANNELFAVORABLE.name().equalsIgnoreCase(op.getPromotion().getPromitionType())){
							if(StringUtils.isNotEmpty(paymentChannel)){
								throwIllegalException("渠道促销一订单只允许使用一次");
							}
							paymentChannel = op.getPromotion().getChannelOrder();
						}
					}
				}
			}
			if(paymentChannel!=null){
				order.setPromPaymentChannel(paymentChannel);
			}
			if(discountAmount>0){
				if(discountAmount>order.getOughtAmount()){
					discountAmount = order.getOughtAmount();
				}
				order.setOughtAmount(order.getOughtAmount()-discountAmount);
				OrdOrderAmountItem item = makeOrderAmountItem(OrderEnum.ORDER_AMOUNT_TYPE.PROMOTION_PRICE,OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_PROMOTION,-discountAmount);
				order.addOrderAmountItem(item);
			}
		}else{
			LOG.info("order.getPromotionMap() is null");
		}	
	}
	
	@Override 
	public OrdOrderDTO initOrderAndCalcForFront(DestBuBuyInfo destbuyInfo,OrdOrderDTO order ) {
		SuppGoods  suppGoods  = new  SuppGoods()  ;
	      LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------initOrder---start");
			initOrder(destbuyInfo, order,suppGoods);
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------initOrder---end");
			// 游玩时间计算
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcOrderVisitTime---start");
		     calcOrderVisitTime(order);
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcOrderVisitTime---end");
		     
		  // 计算订单的品类
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcOrderCategroy---start");

			calcOrderCategroy(order);
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcOrderCategroy---end");

			// 添加bu计算，
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcBuCode---end");

			calcBuCode(order,suppGoods);
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcBuCode---end");
		  // 依赖资源审核的状态做支付时间
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcPaymentType---start");
      
			calcPaymentType(order);
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcPaymentType---end");

			// 资源(主订单)计算
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcResourceConfirm---start");
			calcResourceConfirm(order);
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcResourceConfirm---end");
			// 订单金额计算
		     LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcOrderAmount---start");
			calcOrderAmount(order);
		    LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcOrderAmount---end");
		    
		    LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcRebate---start");			
		    calcRebate(order);// 订单返现
			LOG.info("NewHotelComOrderInitServiceImpl---------initOrderAndCalc---------calcRebate---end");
			
			return order;
	}
}