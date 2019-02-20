/**
 * 订单创建服务实现类。
 */
package com.lvmama.vst.order.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import com.lvmama.vst.back.client.supp.service.SuppSettlementEntityClientService;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.vo.SuppGoodsParam;
import com.lvmama.vst.back.supp.po.SuppSettlementEntities;
import com.lvmama.vst.comm.web.BusinessException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizDistrict;
import com.lvmama.vst.back.client.biz.service.CategoryClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsTimePriceClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prom.service.PromotionService;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.goods.vo.ProdProductParam;
import com.lvmama.vst.back.order.po.OrdAdditionStatus;
import com.lvmama.vst.back.order.po.OrdAddress;
import com.lvmama.vst.back.order.po.OrdGuaranteeCreditCard;
import com.lvmama.vst.back.order.po.OrdItemContractRelation;
import com.lvmama.vst.back.order.po.OrdItemPersonRelation;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderAmountItem;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrdPromotion;
import com.lvmama.vst.back.order.po.OrdTravelContract;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdDestRe;
import com.lvmama.vst.back.prod.po.ProdLineRoute;
import com.lvmama.vst.back.prod.po.ProdProduct;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.back.prom.po.PromotionEnum;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.TimePriceUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyInfo;
import com.lvmama.vst.comm.vo.order.BuyInfoPromotion;
import com.lvmama.vst.comm.vo.order.BuyInfoPromotion.ItemPrice;
import com.lvmama.vst.order.contract.service.IOrderElectricContactService;
import com.lvmama.vst.order.dao.OrdAdditionStatusDAO;
import com.lvmama.vst.order.dao.OrdAddressDao;
import com.lvmama.vst.order.dao.OrdGuaranteeCreditCardDao;
import com.lvmama.vst.order.dao.OrdItemPersonRelationDao;
import com.lvmama.vst.order.dao.OrdMulPriceRateDAO;
import com.lvmama.vst.order.dao.OrdOrderAmountItemDao;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdOrderHotelTimeRateDao;
import com.lvmama.vst.order.dao.OrdOrderItemDao;
import com.lvmama.vst.order.dao.OrdOrderPackDao;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.dao.OrdPersonDao;
import com.lvmama.vst.order.dao.OrdPromotionDao;
import com.lvmama.vst.order.dao.OrdTravelContractDAO;
import com.lvmama.vst.order.service.IOrdItemContractRelationService;
import com.lvmama.vst.order.service.IOrdTravelContractService;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.order.vo.OrdOrderUpdateStockDTO;
import com.lvmama.vst.pet.adapter.IOrdUserOrderServiceAdapter;
import com.lvmama.vst.pet.goods.PetProdGoodsAdapter;

/**
 * @author lancey
 * 
 */
@Service("orderCreateService")
public class OrderCreateServiceImpl extends AbstractOrderService {

	private static final Log LOG = LogFactory.getLog(OrderCreateServiceImpl.class);

	@Autowired
	private OrderValidCheckBussiness orderValidCheckBussiness;

	@Autowired
	private OrdOrderDao orderDao;

	@Autowired
	private OrdOrderItemDao orderItemDao;

	@Autowired
	private OrdOrderHotelTimeRateDao orderHotelTimeRateDao;

	@Autowired
	private OrdGuaranteeCreditCardDao guaranteeCreditCardDao;

	@Autowired
	private OrdOrderAmountItemDao ordOrderAmountItemDao;

	@Autowired
	private OrdPersonDao ordPersonDao;

	@Autowired
	private OrdAddressDao ordAddressDao;

	@Autowired
	private OrdOrderPackDao ordOrderPackDao;

	@Autowired
	private OrdOrderStockDao ordOrderStockDao;
	
	@Autowired
	private OrdPromotionDao ordPromotionDao;
	
	@Autowired
	private OrdMulPriceRateDAO ordMulPriceRateDAO;
	
	@Autowired
	private OrdItemPersonRelationDao ordItemPersonRelationDao;
	
	@Autowired
	protected SuppGoodsTimePriceClientService suppGoodsTimePriceClientService;
	
	@Autowired
	private PromotionService promotionService;
	
	@Autowired
	private IOrdUserOrderServiceAdapter ordUserOrderService;
	
	@Autowired
	private PetProdGoodsAdapter petProdGoodsAdapter;
	
	@Autowired
	private OrdAdditionStatusDAO ordAdditionStatusDAO;
	
	@Autowired
	private OrdTravelContractDAO ordTravelContractDAO;
	
	@Autowired
	private IOrdTravelContractService ordTravelContractService;
	
	@Autowired
	private ProdProductClientService productClientService;
	
	@Resource(name="advanceProductAgreementContractService")
	private IOrderElectricContactService orderElectricContactService;

	@Autowired
	private SuppSettlementEntityClientService suppSettlementEntityClientService;
	
	public ResultHandleT<OrdOrder> createOrder(BuyInfo buyInfo, String operatorId) {
		ResultHandleT<OrdOrderDTO> handle = orderValidCheckBussiness.initOrderDTO(buyInfo, operatorId);
		ResultHandleT<OrdOrder> handleOrder = new ResultHandleT<OrdOrder>();

		if (handle.isFail()) {
			handleOrder.setMsg(handle.getMsg());
		} else {
			OrdOrderDTO order = handle.getReturnContent();

			beginRunRules(order);
			
			calcPaymentType(order);

			// 计算优惠策略
			calcCoupon(order);
			
			calcWorkflow(order);

			// 保存订单对象图
			saveOrder(order);

			endRunRules(order);
			OrdOrder result = new OrdOrder();
			BeanUtils.copyProperties(order, result);
			handleOrder.setReturnContent(result);
		}

		return handleOrder;
	}
	
	/**
	 * 计算是否需要改为强制预授权处理
	 * @param order
	 */
	private void calcPaymentType(OrdOrderDTO order){
		//预付订单并且默认不是强制预授权才需要计算
		if(order.hasNeedPrepaid()&&!order.isPayMentType()){
			String strategy=SuppGoodsTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE.name();
			for(OrdOrderItem orderItem:order.getOrderItemList()){
				if(SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name().equalsIgnoreCase(orderItem.getCancelStrategy())){
					strategy = orderItem.getCancelStrategy();
					break;
				}
			}
			//订单属于不退不改的情况下直接改成订单为强制预授权退出
			if(SuppGoodsTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE.name().equalsIgnoreCase(strategy)){
				order.setPaymentType(SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name());
				orderUpdateService.setOrderWatiPaymentTime(order, order.getCreateTime(), false);
			} else if (TimePriceUtils.hasPreauthBook(order.getLastCancelTime(),
					order.getCreateTime())) {// 需要按时间来更改强制预授权的订单
				//更改强制预授权
				order.setPaymentType(SuppGoodsTimePrice.BOOKLIMITTYPE.PREAUTH.name());
				Date aheadTime = null;
				for(OrdOrderItem orderItem:order.getOrderItemList()){
					if (orderItem.getAheadTime() != null
							&& (aheadTime == null || aheadTime.after(orderItem
									.getAheadTime()))) {
						aheadTime = orderItem.getAheadTime();
					}
				}
				//等待时间
				order.setWaitPaymentTime(aheadTime);
			}
		}
	}
	
	private void calcWorkflow(OrdOrderDTO order){
		if(Constant.getInstance().isActivitiAble()){
//			if(order.hasNeedPrepaid()){
//				order.setProcessKey("single_hotel_prepaid_order");
//			}else{
//				order.setProcessKey("single_hotel_pay_order");
//			}
			if(CollectionUtils.isNotEmpty(order.getOrderPackList())){
				order.setProcessKey("order_prepaid_main");
			}else{
				if(order.hasNeedPrepaid()){
					order.setProcessKey("single_hotel_pre_order");
				}else{
					order.setProcessKey("single_hotel_pay_order");
				}
			}
		}
	}
	
	/**
	 * 初始化数据后使用规则引擎来处理业务
	 * 
	 * @param order
	 */
	private void beginRunRules(OrdOrderDTO order) {
		
	}

	private void endRunRules(OrdOrderDTO order) {

	}

	/**
	 * 计算优惠活动
	 * 
	 * @param order
	 * @return
	 */
	private OrdOrderDTO calcCoupon(OrdOrderDTO order) {
		if (order != null && order.getBuyInfo() != null) {
			BuyInfo buyInfo = order.getBuyInfo();
			List<Long> promotionIdList = buyInfo.getPromotionIdList();
			List<OrdOrderItem> orderItemList = order.getOrderItemList();
			if (orderItemList != null && orderItemList.size() > 0 && order.getItemPriceTableMap() != null) {
				List<BuyInfoPromotion.Item> promotionItemList = new ArrayList<BuyInfoPromotion.Item>();
				String payTarget = null;
				Map<Date, ItemPrice> itemPriceMap = null;
				BuyInfoPromotion.Item promotionItem = null;
				ResultHandleT<BuyInfoPromotion.Item> resultHandle = null;
				Long totalPrice = 0L;
				Long totalSettlementPrice = 0L;
				Long orderTotalPrice = 0L;
				Long orderTotalSettlementPrice = 0L;
				//所有满足的促销Map
				Map<Long, PromPromotion> promotionMap = new HashMap<Long, PromPromotion>();
				for (OrdOrderItem orderItem : orderItemList) {
					if (orderItem != null) {
						payTarget = order.getPaymentTarget();
						//获取时间价格表
						itemPriceMap = order.getItemPriceTableMap().get(orderItem);
						promotionItem = makeBuyInfoPromotionItemFromOrderItem(orderItem, payTarget, itemPriceMap);
						promotionItemList.add(promotionItem);
						//计算优惠价格
						resultHandle = promotionService.calcPromotion(promotionItem, order.getDistributorId(), promotionIdList, true);
						if (resultHandle.isFail()) {
							throw new IllegalArgumentException(resultHandle.getMsg());
						}
						promotionItem = resultHandle.getReturnContent();
						//订单子项价格更新
						totalPrice = computeBuyInfoPromotionItemReducePrice(promotionItem, PromotionEnum.PRICE_TYPE.DISTRIBUTOR_TYPE.name());
						totalSettlementPrice = computeBuyInfoPromotionItemReducePrice(promotionItem, PromotionEnum.PRICE_TYPE.SUPPLIER_TYPE.name());
						orderItem.setActualSettlementPrice(orderItem.getActualSettlementPrice() - totalSettlementPrice);
						orderItem.setTotalSettlementPrice(orderItem.getTotalSettlementPrice() - totalSettlementPrice * orderItem.getQuantity());
						//为订单统计优惠总价
						orderTotalPrice = orderTotalPrice + totalPrice * orderItem.getQuantity();
						orderTotalSettlementPrice = orderTotalSettlementPrice + totalSettlementPrice * orderItem.getQuantity();
						//创建订单子项优惠列表
						List<OrdPromotion> ordPromotionList = makeOrdPromotionList(promotionItem);
						if (ordPromotionList != null && ordPromotionList.size() > 0) {
							orderItem.setOrdPromotionList(ordPromotionList);
						}
						//促销添加到总列表中
						if (promotionItem != null && promotionItem.getGoodsPromotionList() != null
								&& promotionItem.getGoodsPromotionList().size() > 0) {
							addUpPromotionByPromotionId(promotionMap, promotionItem.getGoodsPromotionList());
						}
					}
				}
				//验证是否有没有符合条件的促销
				ResultHandle handle = checkAllPromotionIdsInOrder(promotionMap, promotionIdList);
				if (handle.isFail()) {
					throw new IllegalArgumentException(handle.getMsg());
				}
				//更新订单应付金额
				order.setOughtAmount(order.getOughtAmount() - orderTotalPrice);
				//更新订单金额表
				List<OrdOrderAmountItem> amountItemList = order.getOrderAmountItemList();
				if (amountItemList == null) {
					amountItemList = new ArrayList<OrdOrderAmountItem>();
					order.setOrderAmountItemList(amountItemList);
				}
				//售价优惠金额子项
				if (orderTotalPrice != 0) {
					OrdOrderAmountItem amountItem = makeOrdOrderAmountItem(-orderTotalPrice, OrderEnum.ORDER_AMOUNT_TYPE.PROMOTION_PRICE.name(), OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_PROMOTION.name());
					amountItemList.add(amountItem);
				}
				//结算价优惠金额子项
				if (orderTotalSettlementPrice != 0) {
					OrdOrderAmountItem amountItem = makeOrdOrderAmountItem(-orderTotalSettlementPrice, OrderEnum.ORDER_AMOUNT_TYPE.PROMOTION_SETTLEPRICE.name(), OrderEnum.ORDER_AMOUNT_NAME.AMOUNT_NAME_PROMOTION.name());
					amountItemList.add(amountItem);
				}
			}
		}
		
		return order;
	}
	
	/**
	 * 构造OrdOrderAmountItem对象
	 * 
	 * @param amount
	 * @param amountType
	 * @param name
	 * @return
	 */
	private OrdOrderAmountItem makeOrdOrderAmountItem(Long amount, String amountType, String name) {
		OrdOrderAmountItem orderAmountItem = new OrdOrderAmountItem();
		orderAmountItem.setItemAmount(amount);
		orderAmountItem.setOrderAmountType(amountType);
		orderAmountItem.setItemName(name);
		return orderAmountItem;
	}
	
	/**
	 * 构造OrdPromotion列表
	 * 
	 * @param promotionItem
	 * @return
	 */
	private List<OrdPromotion> makeOrdPromotionList(BuyInfoPromotion.Item promotionItem) {
		List<OrdPromotion> ordPromotionList = null;
		if (promotionItem != null) {
			List<PromPromotion> promotionList = promotionItem.getGoodsPromotionList();
			if (promotionList != null && promotionList.size() > 0) {
				ordPromotionList = new ArrayList<OrdPromotion>();
				OrdPromotion ordPromotion = null;
				for (PromPromotion p : promotionList) {
					if (p != null) {
						ordPromotion = new OrdPromotion();
						ordPromotion.setCode(p.getCode());
						ordPromotion.setPriceType(p.getPriceType());
						ordPromotion.setPromPromotionId(p.getPromPromotionId());
						ordPromotion.setPromTitle(p.getTitle());
						
						ordPromotionList.add(ordPromotion);
					}
				}
			}
		}
		
		return ordPromotionList;
	}
	
	/**
	 * 根据降价方案，计算订单子项优惠总额
	 * 
	 * @param promotionItem
	 * @param priceType
	 * @return
	 */
	private Long computeBuyInfoPromotionItemReducePrice(BuyInfoPromotion.Item promotionItem, String priceType) {
		Long totalPrice = 0L;
		if (promotionItem != null && priceType != null) {
			Map<Long, Long> priceMap = promotionItem.getPromPriceMap();
			if (priceMap != null && priceMap.size() > 0) {
				PromPromotion promotion = null;
				Long promotionId = null;
				Set<Entry<Long, Long>> entrySet = priceMap.entrySet();
				for (Entry<Long, Long> entry : entrySet) {
					if (entry != null) {
						promotionId = entry.getKey();
						promotion = getPromotionFromBuyInfoPromotionItem(promotionItem, promotionId);
						if (promotion != null && priceType.equals(promotion.getPriceType())
								&& PromotionEnum.RESULT_TYPE.REDUCE_PRICE.name().equals(promotion.getPromResult().getResultType())) {
							totalPrice = totalPrice + entry.getValue();
						}
					}
				}
			}
		}
		
		return totalPrice;
	}
	
	/**
	 * 按照PromotionId统计Promotion对象
	 * 
	 * @param promotionMap
	 * @param promotionList
	 */
	private void addUpPromotionByPromotionId(Map<Long, PromPromotion> promotionMap, List<PromPromotion> promotionList) {
		if (promotionMap != null && promotionList != null && promotionList.size() > 0) {
			for (PromPromotion p : promotionList) {
				if (p != null) {
					if (promotionMap.get(p.getPromPromotionId()) == null) {
						promotionMap.put(p.getPromPromotionId(), p);
					}
				}
			}
		}
	}
	
	/**
	 * 验证所选的促销ID是否都已经被应用
	 * 
	 * @param promotionMap
	 * @param promotionIdList
	 * @return
	 */
	private ResultHandle checkAllPromotionIdsInOrder(Map<Long, PromPromotion> promotionMap, List<Long> promotionIdList) {
		ResultHandle handle = new ResultHandle();
		String errMsg = null;
		PromPromotion promotion = null;
		if (promotionIdList != null && promotionIdList.size() > 0) {
			for (Long id : promotionIdList) {
				promotion = null;
				if (id != null) {
					if (promotionMap != null) {
						promotion = promotionMap.get(id);
					}
					if (promotion == null) {
						if (errMsg == null) {
							errMsg = "促销（ID=" + id + "）不满足促销条件。";
						} else {
							errMsg = errMsg + "\n促销（ID=" + id + "）不满足促销条件。";
						}
					}
				}
			}
		}
		
		if (errMsg != null) {
			handle.setMsg(errMsg);
		}
		return handle;
	}
	
	/**
	 * 根据PromotionID获取订单中优惠
	 * 
	 * @param promotionItem
	 * @param promotionId
	 * @return
	 */
	private PromPromotion getPromotionFromBuyInfoPromotionItem(BuyInfoPromotion.Item promotionItem, Long promotionId) {
		PromPromotion promotion = null;
		if (promotionItem != null && promotionItem.getGoodsPromotionList() != null && promotionId != null) {
			List<PromPromotion> promotionList = promotionItem.getGoodsPromotionList();
			if (promotionList.size() > 0) {
				for (PromPromotion p : promotionList) {
					if (p != null) {
						if (promotionId.equals(p.getPromPromotionId())) {
							promotion = p;
							break;
						}
					}
				}
			}
		}
		
		return promotion;
	}
	
	/**
	 * 构造优惠信息中的优惠子项
	 * 
	 * @param orderItem
	 * @param payTarget
	 * @param itemPriceMap
	 * @return
	 */
	private BuyInfoPromotion.Item makeBuyInfoPromotionItemFromOrderItem(OrdOrderItem orderItem, String payTarget, Map<Date, ItemPrice> itemPriceMap) {
		BuyInfoPromotion.Item promotionItem = null;
		if (orderItem != null) {
			promotionItem = new BuyInfoPromotion.Item();
			promotionItem.setCategoryId(orderItem.getCategoryId());
			promotionItem.setGoodsId(orderItem.getSuppGoodsId());
			promotionItem.setItemPriceMap(itemPriceMap);
			promotionItem.setPayTarget(payTarget);
			promotionItem.setQuantity(orderItem.getQuantity());
			promotionItem.setSettlementAmount(orderItem.getTotalSettlementPrice());
			promotionItem.setTotalAmount(orderItem.getPrice() * orderItem.getQuantity());
			promotionItem.setVisitTime(orderItem.getVisitTime());
			Date visitDate = orderItem.getVisitTime();
			if (itemPriceMap != null) {
				Date leaveDate = DateUtil.dsDay_Date(visitDate, itemPriceMap.size());
				promotionItem.setLeaveTime(leaveDate);
			}
		}
		
		return promotionItem;
	}
	
	/**
	 * 重新检查并重设定金
	 * @param order
	 */
	void calcDepositsAmount(OrdOrderDTO order){
		if(order.getDepositsAmount()!=null&&order.getDepositsAmount()>0L){
			if(order.getDepositsAmount()>order.getOughtAmount()){
				order.setDepositsAmount(order.getOughtAmount());
			}
		}
	}

	/**
	 * 保存订单（整个对象图）
	 * 
	 * @param order
	 */
	public void saveOrder(OrdOrderDTO order) {
		if (order != null) {
			updateGoodsStockInOrder(order);
			
			calcWaitPayment(order);
			
			calcDepositsAmount(order);
			
			orderDao.saveOrder(order);

			saveOrderPackInOrder(order);
			savePersonInOrder(order);
			saveOrderItemInOrder(order);
			saveGuaranteeCreditCardInOrder(order);
			saveOrderAmountItemInOrder(order);
			saveOrdAdditionStatusInOrder(order);
			saveOrdTravelContractInOrder(order);
			
			saveOrdUserOrder(order);
			
			orderDao.updateByPrimaryKey(order);
		}
	}
	
	private void calcWaitPayment(OrdOrderDTO order){
		if(order.isPayMentType()){
			orderUpdateService.setOrderWatiPaymentTime(order, order.getCreateTime(), true);
		}
		if(order.hasInfoAndResourcePass()){
			order.setApproveTime(order.getCreateTime());
			orderUpdateService.setOrderWatiPaymentTime(order, order.getCreateTime(), true);
		}
	}

	/**
	 * 更新商品库存
	 * 
	 * @param order
	 */
	private void updateGoodsStockInOrder(OrdOrderDTO order) {
		if (order != null && order.getUpdateStockDTOMap() != null && !order.getUpdateStockDTOMap().isEmpty()) {
			Long timePriceId = null;
			OrdOrderUpdateStockDTO ordOrderUpdateStockDTO = null;
			OrderTimePriceService orderTimePriceService = null;
			Map<String, Object> dataMap =  null;
			Boolean apiFlag = null;
			for (Entry<Long, OrdOrderUpdateStockDTO> entry : order.getUpdateStockDTOMap().entrySet()) {
				if (entry != null) {
					timePriceId = entry.getKey();
					ordOrderUpdateStockDTO = entry.getValue();
					orderTimePriceService = ordOrderUpdateStockDTO.getOrderTimePriceService();
					SuppGoodsBaseTimePrice suppGoodsBaseTimePrice = ordOrderUpdateStockDTO.getTimePrice();
					apiFlag = order.getApiFlagMap().get(suppGoodsBaseTimePrice.getSuppGoodsId());
					if (apiFlag != null && apiFlag) {
						dataMap = null;
					} else {
						dataMap = new HashMap<String, Object>();
						dataMap.put("isUpdateSuperStock", new Boolean(true));
						dataMap.put("suppGoodsId", suppGoodsBaseTimePrice.getSuppGoodsId());
						dataMap.put("beginDate", suppGoodsBaseTimePrice.getSpecDate());
						dataMap.put("endDate", suppGoodsBaseTimePrice.getSpecDate());
					}
					
					orderTimePriceService.updateStock(timePriceId, -ordOrderUpdateStockDTO.getUpdateStock(), dataMap);
				}
			}
		}
	}

	/**
	 * 保存打包商品
	 * 
	 * @param order
	 */
	private void saveOrderPackInOrder(OrdOrderDTO order) {
		if ((order != null) && (order.getOrderPackList() != null)) {
			for (OrdOrderPack pack : order.getOrderPackList()) {
				pack.setOrderId(order.getOrderId());
				ordOrderPackDao.insertSelective(pack);
			}
		}
	}

	/**
	 * 保存订单子项
	 * 
	 * @param order
	 */
	private void saveOrderItemInOrder(OrdOrderDTO order) {
		if ((order != null) && (order.getOrderItemList() != null)) {
			for (OrdOrderItem orderItem : order.getOrderItemList()) {
				orderItem.setOrderId(order.getOrderId());

                //设置该订单所属的用户组
                SuppGoods suppGoods = orderItem.getSuppGoods();
                if(null != suppGoods){
                    orderItem.setEbkSupplierGroupId(suppGoods.getEbkSupplierGroupId());
                }else{
                    orderItem.setEbkSupplierGroupId(0L);
                }

				List<OrdOrderPack> ordOrderPackList = order.getOrderPackList();
				if (ordOrderPackList != null && !ordOrderPackList.isEmpty()) {
					orderItem.setOrderPackId(ordOrderPackList.get(0).getOrderPackId());
				}

				// 保存子订单中 结算对象ID及CODE
				try{
					Long suppGoodsId = orderItem.getSuppGoodsId();
					if(null==suppGoodsId){
						throw new BusinessException(" [ OrderCreateServiceImpl ] , ERROR : suppgoods is is null in orderItem ");
					}

					if(suppGoods==null){
						ResultHandleT<SuppGoods> suppGoodsResultHandleT = suppGoodsClientService.findSuppGoodsById(suppGoodsId,new SuppGoodsParam());
						if(!suppGoodsResultHandleT.isSuccess()){
							throw new BusinessException(" [ OrderCreateServiceImpl ] , ERROR : can not find suppGoods by id ("+suppGoodsId+")");
						}else{
							suppGoods = suppGoodsResultHandleT.getReturnContent();
						}
					}

					String settleEntityCode = suppGoods.getSettlementEntityCode();
					if(StringUtils.isEmpty(settleEntityCode)){
						throw new BusinessException(" [ OrderCreateServiceImpl ] , ERROR : suppGoods has no settlement entity code!!! suppGoods id is "+suppGoods.getSuppGoodsId());
					}

					ResultHandleT<SuppSettlementEntities> resultHandleT = suppSettlementEntityClientService.findSuppSettlementEntityByCode(settleEntityCode);
					if(!resultHandleT.isSuccess()){
						throw new BusinessException(" [ OrderCreateServiceImpl ] , ERROR : find SuppSettlementEntities by code ("+settleEntityCode+" ) failed");
					}

					SuppSettlementEntities settlementEntities = resultHandleT.getReturnContent();
					if(null==settlementEntities){
						throw new BusinessException(" [ OrderCreateServiceImpl ] , ERROR : can not find SuppSettlementEntities by code ("+settleEntityCode+" )");
					}

					// 存入订单表中的值为 ID_CODE形式
					String codeValue = settlementEntities.getId()+"_"+settlementEntities.getCode();
					orderItem.setSettlementEntityCode(codeValue);

				}catch (Exception e){
					LOG.error(" [ OrderCreateServiceImpl ] set orderItem settleEntityCode has exception, error msg : "+e.getMessage());
				}

				orderItemDao.insertSelective(orderItem);

				savePersonRelationInOrderItem(orderItem);
				saveOrderStockInOrderItem(orderItem);
				saveOrdMulPriceRateInOrderItem(orderItem);
				saveOrderHotelTimeRateInOrderItem(orderItem);
				saveOrdPromotionInOrderItem(orderItem);
			}
		}
	}

	/**
	 * 更新非酒店类订单本地库存量
	 * 
	 * @param orderItem
	 */
	private void saveOrderStockInOrderItem(OrdOrderItem orderItem) {
		if ((orderItem != null) && (orderItem.getOrderStockList() != null)) {
			for (OrdOrderStock orderStock : orderItem.getOrderStockList()) {
				orderStock.setObjectId(orderItem.getOrderItemId());
				orderStock.setOrderItemId(orderItem.getOrderItemId());
				ordOrderStockDao.insertSelective(orderStock);
			}
		}
	}

	/**
	 * 更新酒店类订单本地库存量
	 * 
	 * @param hotelTimeRate
	 */
	private void saveOrderStockInHotelTimeRate(OrdOrderHotelTimeRate hotelTimeRate) {
		if ((hotelTimeRate != null) && (hotelTimeRate.getOrderStockList() != null)) {
			for (OrdOrderStock orderStock : hotelTimeRate.getOrderStockList()) {
				orderStock.setObjectId(hotelTimeRate.getHotelTimeRateId());
				orderStock.setOrderItemId(hotelTimeRate.getOrderItemId());
				
				ordOrderStockDao.insertSelective(orderStock);
			}
		}
	}

	/**
	 * 保存酒店相关的订单子项每天使用的时间记录
	 * 
	 * @param orderItem
	 */
	private void saveOrderHotelTimeRateInOrderItem(OrdOrderItem orderItem) {
		if ((orderItem != null) && (orderItem.getOrderHotelTimeRateList() != null)) {
			for (OrdOrderHotelTimeRate orderHotelTimeRate : orderItem.getOrderHotelTimeRateList()) {
				orderHotelTimeRate.setOrderItemId(orderItem.getOrderItemId());
				orderHotelTimeRateDao.insertSelective(orderHotelTimeRate);

				saveOrderStockInHotelTimeRate(orderHotelTimeRate);
			}
		}
	}
	
	/**
	 * 保存订单子项中的订单-促销关系
	 * 
	 * @param orderItem
	 */
	private void saveOrdPromotionInOrderItem(OrdOrderItem orderItem) {
		if ((orderItem != null) && (orderItem.getOrdPromotionList() != null)) {
			for (OrdPromotion ordPromotion : orderItem.getOrdPromotionList()) {
				ordPromotion.setOrderItemId(orderItem.getOrderItemId());

				ordPromotionDao.insertSelective(ordPromotion);
			}
		}
	}
	
	/**
	 * 保存子订单多价格表
	 * 
	 * @param orderItem
	 */
	private void saveOrdMulPriceRateInOrderItem(OrdOrderItem orderItem) {
		if ((orderItem != null) && (orderItem.getOrdMulPriceRateList() != null)) {
			for (OrdMulPriceRate ordMulPriceRate : orderItem.getOrdMulPriceRateList()) {
				ordMulPriceRate.setOrderItemId(orderItem.getOrderItemId());

				ordMulPriceRateDAO.insertSelective(ordMulPriceRate);
			}
		}
	}

	/**
	 * 保存担保信用卡信息
	 * 
	 * @param order
	 */
	private void saveGuaranteeCreditCardInOrder(OrdOrderDTO order) {
		if ((order != null) && (order.getOrdGuaranteeCreditCardList() != null)) {
			for (OrdGuaranteeCreditCard card : order.getOrdGuaranteeCreditCardList()) {
				card.setOrderId(order.getOrderId());
				guaranteeCreditCardDao.insertSelective(card);
			}
		}
	}

	/**
	 * 保存订单金额变换信息
	 * 
	 * @param order
	 */
	private void saveOrderAmountItemInOrder(OrdOrderDTO order) {
		if ((order != null) && (order.getOrderAmountItemList() != null)) {
			for (OrdOrderAmountItem orderAmountItem : order.getOrderAmountItemList()) {
				orderAmountItem.setOrderId(order.getOrderId());
				ordOrderAmountItemDao.insertSelective(orderAmountItem);
			}
		}
	}
	
	/**
	 * 保存订单附件状态
	 * 
	 * @param order
	 */
	private void saveOrdAdditionStatusInOrder(OrdOrderDTO order) {
		if ((order != null) && (order.getOrdAdditionStatusList() != null)) {
			for (OrdAdditionStatus ordAdditionStatus : order.getOrdAdditionStatusList()) {
				ordAdditionStatus.setOrderId(order.getOrderId());
				ordAdditionStatusDAO.insertSelective(ordAdditionStatus);
			}
		}
	}
	
	/**
	 * 保存合同表
	 * 
	 * @param order
	 */
	private void saveOrdTravelContractInOrder(OrdOrderDTO order) {
		if ((order != null) && (order.getOrdTravelContractList() != null)) {
			for (OrdTravelContract ordTravelContract : order.getOrdTravelContractList()) {
				ordTravelContract.setOrderId(order.getOrderId());
				
//				ordTravelContractDAO.insertSelective(ordTravelContract);
				ordTravelContractService.saveOrdTravelContract(ordTravelContract, "SYSTEM");
			}
		}
	}
	
	/**
	 * 保存订单当中的联系人、游玩人
	 * 
	 * @param order
	 * @throws
	 */
	private void savePersonInOrder(OrdOrderDTO order) {
		if ((order != null) && (order.getOrdPersonList() != null)) {
			for (OrdPerson person : order.getOrdPersonList()) {
				person.setObjectId(order.getOrderId());
				ordPersonDao.insertSelective(person);

				saveAddressInPerson(person);
			}
		}
	}
	
	private void savePersonRelationInOrderItem(OrdOrderItem orderItem) {
		if ((orderItem != null) && (orderItem.getOrdItemPersonRelationList() != null)) {
			for (OrdItemPersonRelation ordItemPersonRelation : orderItem.getOrdItemPersonRelationList()) {
				if (ordItemPersonRelation != null) {
					ordItemPersonRelation.initOrdPersonId();
					ordItemPersonRelation.setOrderItemId(orderItem.getOrderItemId());
					
					ordItemPersonRelationDao.insertSelective(ordItemPersonRelation);
				}
			}
		}
	}

	/**
	 * 保存联系人的地址
	 * 
	 * @param person
	 */
	private void saveAddressInPerson(OrdPerson person) {
		if ((person != null) && (person.getAddressList() != null)) {
			for (OrdAddress address : person.getAddressList()) {
				address.setOrdPersonId(person.getOrdPersonId());
				ordAddressDao.insertSelective(address);
			}
		}
	}
	
	/**
	 * 订单整合（BEE和VST）
	 * 
	 * @param orderInfo
	 */
	private void saveOrdUserOrder(OrdOrderDTO order) {
		if (order != null) {
			String categoryIdStr = null;
			if (order.getOrderPackList() != null
					&& !order.getOrderPackList().isEmpty()
					&& order.getOrderPackList().get(0).getCategoryId() != null) {
				categoryIdStr = order.getOrderPackList().get(0).getCategoryId().toString();
			} else if (order.getMainOrderItem() != null && order.getMainOrderItem().getCategoryId() != null) {
				categoryIdStr = order.getMainOrderItem().getCategoryId().toString();
			}
			ordUserOrderService.insertOrdUserOrder(order.getCreateTime(), order.getOrderId(), categoryIdStr, order.getUserNo());
		}
	}
}
