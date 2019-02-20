package com.lvmama.vst.order.service.book.destbu;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import com.lvmama.comm.vst.vo.CardInfo;
import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.biz.po.BizDictDef;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.biz.service.DictDefClientService;
import com.lvmama.vst.back.client.goods.service.SuppGoodsClientService;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.client.precontrol.service.ResWarmRuleClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductClientService;
import com.lvmama.vst.back.client.prod.service.ProdProductNoticeClientService;
import com.lvmama.vst.back.client.prom.service.PromForbidBuyClientService;
import com.lvmama.vst.back.client.pub.service.ComLogClientService;
import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.po.ResWarmRule;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.newHotelcomb.service.INewHotelCombTimePriceService;
import com.lvmama.vst.back.order.exception.OrderException;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderNotice;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.prod.po.ProdProductNotice;
import com.lvmama.vst.back.prom.po.PromForbidBuy;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.back.prom.po.PromotionEnum;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.back.service.VstPromotionOrderService;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.DESCoder;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.Pair;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.Constant.ACTIVITY_TYPE;
import com.lvmama.vst.comm.vo.Constant.ORDER_FAVORABLE_TYPE;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.comm.vo.order.BuyPresentActivityInfo;
import com.lvmama.vst.comm.vo.order.FavorStrategyInfo;
import com.lvmama.vst.comm.vo.order.PriceInfo;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo;
import com.lvmama.vst.comm.vo.order.destbu.DestBuBuyInfo.Coupon;
import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.redis.JedisTemplate2;
import com.lvmama.vst.order.service.IBookService;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdOrderService;
import com.lvmama.vst.order.service.IOrdPersonService;
import com.lvmama.vst.order.service.IOrdProductNotice;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.service.book.NewHotelComOrderBussiness;
import com.lvmama.vst.order.service.book.NewHotelComOrderInitService;
import com.lvmama.vst.order.service.book.OrderSaveService;
import com.lvmama.vst.order.service.impl.PromBuyPresentBussiness;
import com.lvmama.vst.order.service.impl.PromotionBussiness;
import com.lvmama.vst.order.timeprice.service.impl.NewOrderHotelCompTimePriceServiceImpl;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.pet.adapter.FavorServiceAdapter;
import com.lvmama.vst.pet.adapter.IOrdUserOrderServiceAdapter;
import com.lvmama.vst.pet.adapter.IPayPaymentServiceAdapter;
import com.lvmama.vst.pet.adapter.QueryPaymentGatewayServiceAdapter;
import com.lvmama.vst.pet.vo.UserCouponVO;
import com.lvmama.vst.pet.vo.VstCashAccountVO;
/**
 * @author dengcheng
 * @version 2016年10月25日 下午6:05:51
 * @email dengcheng@lvmama.com
 */

public abstract class DestBuHotelBookServiceImpl {


	@Autowired
	private OrdOrderStockDao orderStockDao;
	@Autowired
	private NewHotelComOrderBussiness  newHotelComOrderBussiness;
	@Autowired
	private IBookService bookService;

	@Autowired
	protected OrderService orderService;

	@Autowired
	private IPayPaymentServiceAdapter payPaymentServiceAdapter;

	@Autowired
	private IOrdUserOrderServiceAdapter ordUserOrderServiceAdapter;// 调用vstpet获取现金和奖金余额接口

	@Autowired
	private IOrdUserOrderServiceAdapter ordUserOrderService;

	@Autowired
	private ProdProductNoticeClientService prodProductNoticeClientService;

	@Autowired
	private QueryPaymentGatewayServiceAdapter queryPaymentGatewayServiceAdapter;
	@Autowired
	private IOrdProductNotice ordProductNotice;

	@Autowired
	private PromotionBussiness promotionBussiness;

	@Autowired
	private VstPromotionOrderService vstPromotionOrderServiceRemote;

	@Autowired
	private PromBuyPresentBussiness promBuyPresentBussiness;
	@Autowired
	private NewHotelComOrderInitService newHotelComOrderInitService;

	@Autowired
	protected FavorServiceAdapter favorService;
	@Autowired
	private ResPreControlService resControlBudgetRemote;
    @Autowired
    private NewOrderHotelCompTimePriceServiceImpl  orderHotelComp2HotelTimePriceService;
    @Autowired
    private OrderSaveService orderSaveService;
    @Autowired
	protected SuppGoodsClientService suppGoodsClientService;
    @Autowired
    private ResWarmRuleClientService resWarmRuleClientService;
    @Resource
    private TopicMessageProducer resPreControlEmailMessageProducer;
    
	@Autowired
	private ComLogClientService comLogClientService;
	
	@Autowired
	private DictDefClientService dictDefClientService;
	

	@Resource(name="orderMessageProducer")
	private TopicMessageProducer orderMessageProducer;
	
	@Autowired
	private IOrderUpdateService orderUpdateService;
	
	@Autowired
    private IOrdPersonService iOrdPersonService;
	@Autowired
	private IOrdOrderService iOrdOrderService;
	@Autowired
	private ProdProductClientService productClientService;
	
	@Autowired
	protected IComplexQueryService complexQueryService;

	@Autowired
	private PromForbidBuyClientService  promForbidBuyClientService;
	private static final Logger LOG = LoggerFactory
			.getLogger(DestBuHotelBookServiceImpl.class);

	/**
	 * 目的地bu 酒店套餐订单保存
	 */
	public abstract ResultHandleT<OrdOrder> createOrder(final DestBuBuyInfo buyInfo,final String operatorId);

	
	/**
	 * 实现 价格计算
	 */
	public PriceInfo calOrderPrice(OrdOrderDTO order, DestBuBuyInfo buyInfo) {
		ResultHandleT<PriceInfo> priceInfoHandle = new ResultHandleT<PriceInfo>();
		PriceInfo priceInfo = new PriceInfo();

		long orderMarketPrice = 0L;// 订单市场价金额
		long orderPrice = 0L;// 订单销售价金额
		long orderOughtPay = 0L;// 订单应付金额
		long promotionAmount = 0L; // 订单促销商品优惠总金额
		long couponAmount = 0L; // 订单优惠券抵扣总金额
		long quantitySum = 0L;
		long ticketAmount = 0L;
		long insurancePrice = 0L;
		long expressPrice = 0L;
		long bonus = 0L;
		long maxBonus = 0L;
		long gapPrice = 0L; // 房差价格
		long rebateAmount = 0L;// 点评返现金额
		long depositPrice = 0L;

		// 记录商品品类
		List<String> goodsCategorys = new ArrayList<String>();

		try {

			orderPrice = order.getOughtAmount();
			rebateAmount = order.getRebateAmount();
			// 修改毛利率是否大于0.00
			// 计算毛利是否大于0.03
			// boolean isCanBoundLipinkaPay = false;
			// Long originalPrice =
			// order.getOrderAmountItemValue(OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_PRICE.name());
			// Long originalSettlePrice =
			// order.getOrderAmountItemValue(OrderEnum.ORDER_AMOUNT_TYPE.ORIGINAL_SETTLEPRICE.name());
			// if(originalPrice != null && originalSettlePrice != null) {
			// isCanBoundLipinkaPay = (originalPrice > (originalSettlePrice * (1
			// + 0.00)));
			// }
			// priceInfo.setCanBoundLipinkaPay(isCanBoundLipinkaPay);
			priceInfo.setRebateAmount(rebateAmount);
			// end

			priceInfo.setPaymentTarget(order.getPaymentTarget());
			priceInfo.setPaymentType(order.getPaymentType());
			priceInfo.setResourceStatus(order.getResourceStatus());
			priceInfo.setDoBookPolicyStr(getCancelStrategy(order));
			// 修改分销子渠道的促销
			// 促销
			List<PromPromotion> promotionList = findPromPromotion(order);
			if (!CollectionUtils.isEmpty(promotionList))
				for (PromPromotion promPromotion : promotionList) {
					LOG.info("平台促销log" + promPromotion.getPromPromotionId());
				}
			else {
				LOG.info("平台促销log信息为空");
			}
			if (buyInfo.getSubDistributorId() != null) {
				promotionList = vstPromotionOrderServiceRemote
						.getOrderPromotionList(promotionList,
								buyInfo.getSubDistributorId());
				if (!CollectionUtils.isEmpty(promotionList))
					for (PromPromotion promPromotion : promotionList) {
						LOG.info("分销筛选平台促销log"
								+ promPromotion.getPromPromotionId());
					}
				else {
					LOG.info("分销筛选平台促销log信息为空");
				}
				if (promotionList != null) {
					Long subDistributorId = buyInfo.getSubDistributorId();
					if (subDistributorId == 107 || subDistributorId == 108
							|| subDistributorId == 110) {
						// 检查促销可用余额是否满足
						Iterator<PromPromotion> it = promotionList.iterator();
						while (it.hasNext()) {
							PromPromotion promPromotion = it.next();
							if (promPromotion.getPromAmount() != null) {
								long usedAmount = promPromotion.getUsedAmount() == null ? 0L
										: promPromotion.getUsedAmount();
								long balance = promPromotion.getPromAmount()
										- usedAmount;
								// 活动可用余额大于等于促销金额才存
								LOG.info("优惠条件剩余可用金额"
										+ promPromotion.getPromAmount()
										+ usedAmount + "+++++++++++++当前优惠金额"
										+ promPromotion.getDiscountAmount());
								if (balance < promPromotion.getDiscountAmount()) {
									it.remove();
									LOG.info("去除优惠条件剩余可用金额" + balance
											+ "+++++++++++++当前优惠金额"
											+ promPromotion.getDiscountAmount());

								}
							}
						}

					}
				}
			}
			// 如果是渠道优惠，绑定支付渠道中文名
			buildPayChannelCnName(promotionList);
			priceInfo.setPromotionList(promotionList);
			// 满赠
			BuyPresentActivityInfo buyPresentInfo = promBuyPresentBussiness
					.findPromBuyPresentForOrder(order);

			priceInfo.setBuyPresentActivityInfo(buyPresentInfo);
			List<OrdOrderItem> orderItemList = order.getOrderItemList();

			for (OrdOrderItem ordOrderItem : orderItemList) {

				long itemsPrice = 0; // 一条商品的价格

				List<OrdMulPriceRate> mulPriceRateList = ordOrderItem
						.getOrdMulPriceRateList();
				if (!CollectionUtils.isEmpty(mulPriceRateList)) {
					for (OrdMulPriceRate mul : mulPriceRateList) {
						if (OrderEnum.ORDER_PRICE_RATE_TYPE.PRICE_SPREAD.name()
								.equals(mul.getPriceType())) {
							gapPrice += mul.getPrice(); // 计算房差价格
						}
					}
				}
				SuppGoods suppGoods = ordOrderItem.getSuppGoods();

				BizCategory category = suppGoods.getProdProduct()
						.getBizCategory();
				String categoryCode = category.getCategoryCode();

				// 记录商品品类以及距离类型
				if (ProductCategoryUtil.isRoute(categoryCode)) {
					String type = category.getCategoryCode() + "_"
							+ suppGoods.getProdProduct().getProductType();
					goodsCategorys.add(type);
				} else {
					goodsCategorys.add(categoryCode);
				}

				if (ProductCategoryUtil.isInsurance(categoryCode)) {
					itemsPrice += getTotalAmount(ordOrderItem);
					insurancePrice += itemsPrice;
				} else if (ProductCategoryUtil.isRoute(categoryCode)) {
					itemsPrice += getTotalAmount(ordOrderItem);
				}
				priceInfo.getItemPriceMap().put(ordOrderItem.getSuppGoodsId(),
						PriceUtil.trans2YuanStr(itemsPrice));
				priceInfo.getItemMulPriceMap().put(
						ordOrderItem.getSuppGoodsId(),
						ordOrderItem.getOrdMulPriceRateList());
				quantitySum += ordOrderItem.getQuantity();// 购买商品数量总和
			}
			/* 优惠券验证计算 start */
			String youhuiType = buyInfo.getYouhui();
			if (StringUtils.isNotEmpty(youhuiType)
					&& ORDER_FAVORABLE_TYPE.coupon.getCode().equals(youhuiType)) {
				List<Coupon> couponList = buyInfo.getCouponList();
				if (null != couponList && couponList.size() > 0) {
					// buyInfo.setOrderTotalPrice(orderPrice);//设置订单总价
					List<ResultHandle> couponResultHandles = new ArrayList<ResultHandle>(
							2);

					// for (Coupon coupon : couponList) {
					Coupon coupon = couponList.get(0);
					if (StringUtil.isNotEmptyString(coupon.getCode())) {
						Pair<FavorStrategyInfo, Object> resultPair = this
								.calCoupon(order, buyInfo);
						if (resultPair.isSuccess()) {
							FavorStrategyInfo fsi = resultPair.getFirst();
							couponAmount += fsi.getDiscountAmount();
							if (couponAmount == 0) {
								Pair<FavorStrategyInfo, Long> resultPairNotUse = new Pair<FavorStrategyInfo, Long>();
								resultPairNotUse.setMsg(fsi.getDisplayInfo());
								couponResultHandles.add(resultPairNotUse);
							}
						} else {
							couponResultHandles.add(resultPair);
						}
					}
					// }
					priceInfo.setCouponResutHandles(couponResultHandles);
				}
			}

			// 获取订单可使用奖金金额
			try {
				LOG.info("-----------------------------------buyInfo.getUserNo():"
						+ buyInfo.getUserNo());
				LOG.info("-----------------------------------orderPrice"
						+ buyInfo.getUserNo());
				LOG.info("-----------------------------------goodsCategorys:"
						+ goodsCategorys);
				maxBonus = ordUserOrderServiceAdapter
						.getOrderBonusCanPayAmount(buyInfo.getUserNo(),
								orderPrice, goodsCategorys);
				LOG.info("-----------------------------------maxBonus:"
						+ maxBonus);

			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
			if (StringUtils.isNotEmpty(youhuiType)
					&& ORDER_FAVORABLE_TYPE.bonus.getCode().equals(youhuiType)) {
				LOG.info("ordUserOrderServiceAdapter.getOrderBonusCanPayAmount userNo:"
						+ buyInfo.getUserNo()
						+ ",orderPrice:"
						+ orderPrice
						+ ",goodsCategorys:" + goodsCategorys);
				LOG.info("maxBonus ======" + maxBonus);
				bonus = maxBonus;
				String target = buyInfo.getTarget();
				// 如果是抵扣现金框触发
				if (StringUtils.isNotEmpty(target)
						&& target.equals(ORDER_FAVORABLE_TYPE.bonus.getCode())) {
					bonus = PriceUtil.convertToFen(buyInfo.getBonusYuan());
					if (bonus > maxBonus) {
						bonus = maxBonus;
					}
				}
			}
			LOG.info("可用奖金计算完成---------------------------------");

			if (promotionList != null) {
				for (PromPromotion prom : promotionList) {

					if (!ACTIVITY_TYPE.ORDERCHANNELFAVORABLE.getCode().equals(
							prom.getPromitionType())) {
						promotionAmount += prom.getDiscountAmount();
					}
				}
			}

			/* 订单促销金额end */
			LOG.info("应付金额计算" + "orderPrice:" + orderPrice + "couponAmount:"
					+ couponAmount + "promotionAmount" + promotionAmount
					+ "bonus" + bonus);
			orderOughtPay = orderPrice - couponAmount - promotionAmount - bonus;// 应付金额
			if (orderOughtPay < 1) {
				orderOughtPay = 0;
			}
			priceInfo.setGoodsTotalPrice(orderPrice - expressPrice
					- insurancePrice - depositPrice);
			priceInfo.setBonusYuan(PriceUtil.convertToYuan(bonus));
			priceInfo.setBonus(bonus);
			priceInfo.setMaxBonus(maxBonus);
			priceInfo.setCoupon(couponAmount);
			priceInfo.setMarketPrice(orderMarketPrice);
			priceInfo.setPrice(orderPrice);
			priceInfo.setOughtPay(orderOughtPay);
			priceInfo.setOrderQuantity(quantitySum);
			priceInfo.setPromotionAmount(promotionAmount);
			priceInfo.setTicketGoodsPrice(ticketAmount);
			priceInfo.setInsurancePrice(insurancePrice);
			priceInfo.setExpressPrice(expressPrice);
			priceInfo.setDepositPrice(depositPrice);

			LOG.info("价格计算完成————————————————————————————————————————-");

		} catch (OrderException ex) {
			LOG.error(
					"=com.lvmama.vst.order.service.impl.OrderPriceServiceImpl.countPrice error:",
					ex);
			LOG.info(ex.getMessage());
			priceInfo.sendError(ex.getMessage());
		}

		return priceInfo;

	}

	public Pair<FavorStrategyInfo, Object> calCoupon(OrdOrderDTO order,
			DestBuBuyInfo buyInfo) throws BusinessException {
		Pair<FavorStrategyInfo, Object> result = new Pair<FavorStrategyInfo, Object>();
		List<Coupon> list = buyInfo.getCouponList();
		try {
			result = favorService.calculateFavor(order, list.get(0).getCode(),
					buyInfo.getUserNo());
		} catch (BusinessException e) {
			throw new BusinessException("");
		}
		return result;

	}

	/**
	 * 如果是渠道优惠，绑定支付渠道中文名
	 * 
	 * @param promotionList
	 */
	public void buildPayChannelCnName(List<PromPromotion> promotionList) {
		try {
			if (promotionList != null) {
				Map<String, String> paymentGate = queryPaymentGatewayServiceAdapter
						.getPaymentGateway();
				for (PromPromotion prom : promotionList) {
					if (ACTIVITY_TYPE.ORDERCHANNELFAVORABLE.getCode().equals(
							prom.getPromitionType())) {
						prom.setChannelOrder(paymentGate.get(prom
								.getChannelOrder()));
					}
				}
			}
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
	}

	// 获取促销列表
	public List<PromPromotion> findPromPromotion(OrdOrderDTO order) {
		List<PromPromotion> result = new ArrayList<PromPromotion>();
		try {

			if (org.apache.commons.collections.CollectionUtils.isNotEmpty(order
					.getNopackOrderItemList())) {
				for (OrdOrderItem orderItem : order.getNopackOrderItemList()) {
					// 次规格产品不参与促销
					if ("Y".equals(orderItem.getSuppGoods()
							.getProdProductBranch().getBizBranch()
							.getAttachFlag())) {
						if (StringUtils.isNotEmpty((orderItem.getSuppGoods()
								.getProdProduct().getBizCategory()
								.getPromTarget()))) {
							List<PromPromotion> list = promotionBussiness
									.makeSuppGoodsPromotion(
											order,
											orderItem,
											PromotionEnum.PRICE_TYPE.DISTRIBUTOR_TYPE
													.name());
							if (!list.isEmpty()) {
								result.addAll(list);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.error(ExceptionFormatUtil.getTrace(e));
		}
		return result;
	}

	public long getTotalAmount(OrdOrderItem ordOrderItem) {
		if (ordOrderItem.getTotalAmount() != null) {
			return ordOrderItem.getTotalAmount();
		}
		return ordOrderItem.getPrice() * ordOrderItem.getQuantity();
	}

	private String getCancelStrategy(OrdOrderDTO order) {
		StringBuilder str = new StringBuilder(25);

		String cancelStrategy = "";
		int refunCount = 0;

		List<OrdOrderItem> orderItemList = order.getOrderItemList();
		long amount = 0;
		for (OrdOrderItem ordOrderItem : orderItemList) {
			if (StringUtil.isEmptyString(cancelStrategy)
					&& !StringUtil.isEmptyString(ordOrderItem
							.getCancelStrategy())) {
				cancelStrategy = ordOrderItem.getCancelStrategy();
				refunCount += 1;
			} else if (!cancelStrategy.equals(ordOrderItem.getCancelStrategy())) {
				refunCount += 1;
			}
			if (ordOrderItem.getDeductAmount() != null) {
				amount += ordOrderItem.getDeductAmount();
			}
		}

		if (refunCount > 1) {
			str.append("人工退改. ");
		} else if (refunCount == 1) {
			if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.RETREATANDCHANGE
					.getCode().equals(cancelStrategy)) {
				str.append("请在"
						+ DateUtil.formatDate(order.getLastCancelTime(),
								"yyyy-MM-dd HH:mm:ss") + "之前取消订单 逾期将收取金额"
						+ PriceUtil.convertToYuan(amount) + "作为违约金。");
			} else if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.UNRETREATANDCHANGE
					.getCode().equals(cancelStrategy)) {
				str.append("不退不改 . ");
			} else if (SuppGoodsBaseTimePrice.CANCELSTRATEGYTYPE.MANUALCHANGE
					.getCode().equals(cancelStrategy)) {
				str.append("人工退改. ");
			} else {
				str.append("无");
			}
		} else {
			str.append("无");
		}
		return str.toString();
	}

	/***
	 * 验证优惠券、奖金、现金、礼品卡、储值卡使用情况
	 * 
	 * @param buyInfo
	 * @return
	 */

	public String chechOrderPayForOther(DestBuBuyInfo buyInfo,OrdOrderDTO order) {

		String erroFlag = "";
		// 根据用户NO获取奖金账户和现金账户信息

		if ((buyInfo.getBonusAmountHidden() != null && buyInfo
				.getBonusAmountHidden().intValue() > 0)
				|| (buyInfo.getCashAmountHidden() != null && buyInfo
						.getCashAmountHidden().intValue() > 0)) {
			VstCashAccountVO vstCashAccountVO = ordUserOrderServiceAdapter
					.queryMoneyAccountByUserId(buyInfo.getUserNo());
			Long bonusBalance = vstCashAccountVO.getNewBonusBalance();// 获取奖金余额
			Long MaxPayMoney = vstCashAccountVO.getMaxPayMoney();// 获取可用于支付的现金余额
			if (buyInfo.getBonusAmountHidden() != null
					&& bonusBalance.intValue() < buyInfo.getBonusAmountHidden()
							.intValue()) {
				LOG.error(buyInfo.getUserNo() + "您的账户奖金金额发生变化,请重新输入" + "账户奖金余额"
						+ bonusBalance.intValue() + "该笔订单需要支付的金额"
						+ buyInfo.getBonusAmountHidden().intValue());

				erroFlag = "您的账户奖金金额发生变化,请重新输入";
				return erroFlag;
			}
			if (buyInfo.getCashAmountHidden() != null
					&& MaxPayMoney.intValue() < buyInfo.getCashAmountHidden()
							.intValue()) {
				LOG.error(buyInfo.getUserNo() + "您的账户奖金金额发生变化,请重新输入" + "账户存款余额"
						+ MaxPayMoney.intValue() + "该笔订单需要存款的金额"
						+ buyInfo.getCashAmountHidden().intValue());

				erroFlag = "您的账户存款金额发生变化,请重新输入";
				return erroFlag;
			}

		}

		if (CollectionUtils.isNotEmpty(buyInfo.getUserCouponVoList())) {
			// 查询用户的优惠券
			List<UserCouponVO> userCouponVOList = newHotelComOrderInitService
					.getUserCouponVOList(buyInfo,order);
			List<UserCouponVO> listTmp = new ArrayList<UserCouponVO>();
			for (UserCouponVO c : userCouponVOList) {
				if (StringUtil.isNotEmptyString(c.getValidInfo())) {
					erroFlag = "优惠券(" + c.getCouponCode() + "),"
							+ c.getValidInfo();
					return erroFlag;
				}

				for (UserCouponVO cc : buyInfo.getUserCouponVoList()) {
					if (cc.getCouponCode().equals(c.getCouponCode())) {
						listTmp.add(c);
					}
				}
			}
			buyInfo.setUserCouponVoList(listTmp);
		}

		if (CollectionUtils.isNotEmpty(buyInfo.getGiftCardList())) {
			Map<String, String> map = new HashMap<String, String>();
			for (CardInfo c : buyInfo.getGiftCardList()) {
				String keystr = c.getCardNo() + "_lvmama";
				try {
					map.put(c.getCardNo(),
							DESCoder.decrypt(c.getPassWd(), keystr));
				} catch (Exception e) {

					LOG.error("DES 解密失败");
					erroFlag = "礼品卡密码解密失败";
					return erroFlag;
				}
			}
			List<CardInfo> listGifCardInfo = null;
			try {
				listGifCardInfo = payPaymentServiceAdapter
						.getLvmamaStoredCardListByCardNo(map);
			} catch (Exception e) {
				erroFlag = "获取礼品卡信息失败";
				return erroFlag;

			}
			if (CollectionUtils.isEmpty(listGifCardInfo)
					|| (listGifCardInfo.size() != buyInfo.getGiftCardList()
							.size())) {
				erroFlag = "礼品卡验证结果与选择礼品卡不匹配";
				return erroFlag;
			}
			for (CardInfo c : listGifCardInfo) {
				if ("0".equals(c.getStatus())) {
					erroFlag = c.getBakWord();
					return erroFlag;
				}
			}

		}

		if (CollectionUtils.isNotEmpty(buyInfo.getStoreCardList())) {
			List<String> listCardNo = new ArrayList<String>();
			for (CardInfo c : buyInfo.getStoreCardList()) {
				listCardNo.add(c.getCardNo());
			}
			List<CardInfo> listtStoreCardInfo = null;
			try {
				listtStoreCardInfo = payPaymentServiceAdapter
						.getStoredCardListByCardNo(listCardNo);
			} catch (Exception e) {
				erroFlag = "获取储值卡信息失败";
				return erroFlag;

			}
			if (CollectionUtils.isEmpty(listtStoreCardInfo)
					|| (listtStoreCardInfo.size() != buyInfo.getStoreCardList()
							.size())) {
				erroFlag = "储值卡验证结果与选择礼品卡不匹配";
				return erroFlag;
			}
			for (CardInfo c : listtStoreCardInfo) {
				if ("0".equals(c.getStatus())) {
					erroFlag = c.getBakWord();
					return erroFlag;
				}
			}

		}

		return erroFlag;
	}

	/**
	 * 设置订单产品公告快照
	 * 
	 * @param order
	 */
	public void setOrderNotice(OrdOrder order) throws BusinessException {
		LOG.info("---start set notice in createOrder----");
		try {
			LOG.info("The orderId is:" + order.getOrderId());
			if (order.getCategoryId().equals(
					BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb
							.getCategoryId())
					|| order.getCategoryId().equals(
							BizEnum.BIZ_CATEGORY_TYPE.category_route_local
									.getCategoryId())
					|| order.getCategoryId().equals(
							BizEnum.BIZ_CATEGORY_TYPE.category_route_freedom
									.getCategoryId())
					|| order.getCategoryId().equals(
							BizEnum.BIZ_CATEGORY_TYPE.category_route_group
									.getCategoryId())
					|| order.getCategoryId().equals(
							BizEnum.BIZ_CATEGORY_TYPE.category_route
									.getCategoryId())
					|| order.getCategoryId()
							.equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_new_hotelcomb
									.getCategoryId()))

			{
				OrdOrderNotice ordNotice = new OrdOrderNotice();
				String startTimeStr = (new SimpleDateFormat("yyyy-MM-dd"))
						.format(new Date());
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("startTimeStr", startTimeStr);
				params.put("endTimeStr", startTimeStr);
				params.put("cancelFlag", "Y");
				LOG.info("---start set orderNotice startTimeStr and endTimeStr is----"
						+ startTimeStr);
				Long productId = order.getProductId();
				LOG.info("---start set orderNotice productId is----"
						+ productId);
				if (null != productId) {
					params.put("productId", productId);
					List<ProdProductNotice> noticeList = prodProductNoticeClientService
							.findProductNoticeList_desc(params);
					if (CollectionUtils.isNotEmpty(noticeList)) {
						LOG.info("setOrderNotice noticeList size"
								+ noticeList.size());

						for (ProdProductNotice prodNotice : noticeList) {
							ordNotice.setContent(prodNotice.getContent());
							ordNotice.setNoticeId(prodNotice.getNoticeId());
							ordNotice.setOrdOrderId(order.getOrderId());
							ordNotice.setProductId(prodNotice.getProductId());
							ordNotice.setStartTime(prodNotice.getStartTime());
							ordNotice.setEndTime(prodNotice.getEndTime());
							ordNotice.setNoticeType(prodNotice.getNoticeType());
							ordProductNotice.insert(ordNotice);
						}
					} else {
						LOG.info("---setOrderNotice noticeList size is 0");
					}
				}

			}
		} catch (Exception e) {
			LOG.error("create Order  at notice exception and orderId is "
					+ order.getOrderId(), e);
		}

	}
	
	
	/**
	 * 
	 * 目的地BU扣减库存
	 * @param order
	 */
	public abstract void deductStock(OrdOrder order);
	/**
	 * 目的地bu预控资源扣减接口
	 * 迁移by caiyingshi from  ----->com.lvmama.vst.order.service.book.OrderSaveService
	 */
	public abstract void decutResBackToPrecontrol(OrdOrder order);
	/**
	 * 目的地BU库存校验
	 * 实现 库存校验
	 */
	public abstract ResultHandleT<Object> calOrderStock(final DestBuBuyInfo buyInfo, final String operatorId);

	
	 /**
     * 发送预控消息
     * @param goodsResPrecontrolPolicyVO
     * @param currentAmount 当前剩余金额/库存
     * @param leftAmount  剩余金额/库存
     */
    public void sendBudgetMsgToSendEmail(GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO,Long currentAmount,Long leftAmount){
    	try{
	        List<ResWarmRule> resWarmRules = resWarmRuleClientService.findAllRulesById(goodsResPrecontrolPolicyVO.getId());
	        List<String> rules = new ArrayList<String>();
	        for(ResWarmRule rule : resWarmRules){
	            rules.add(rule.getName());
	        }
	        if(!DateUtil.accurateToDay(new Date()).after(goodsResPrecontrolPolicyVO.getTradeExpiryDate())){
	            //按日预控
	            if(ResControlEnum.CONTROL_CLASSIFICATION.Daily.name().equalsIgnoreCase(goodsResPrecontrolPolicyVO.getControlClassification())){
	                //买断“金额/库存”全部消耗完时，发邮件提醒
	                if(rules.contains("lossAll") && leftAmount.longValue() == 0) {
	                	LOG.info("按日-消耗完毕-发邮件");
	                    resPreControlEmailMessageProducer.sendMsg(MessageFactory.newSendResPreControlEmailMessage(goodsResPrecontrolPolicyVO.getId(), "SEND_DAILY_EMAIL", DateUtil.formatSimpleDate((new Date()))));
	                }
	            }
	            //按周期
	            if(ResControlEnum.CONTROL_CLASSIFICATION.Cycle.name().equalsIgnoreCase(goodsResPrecontrolPolicyVO.getControlClassification())){
	                //买断“金额/库存”全部消耗完时，发邮件提醒
	                if(rules.contains("lossAll") && leftAmount.longValue() == 0){
	                	LOG.info("按周期-消耗完毕-发邮件");
	                    resPreControlEmailMessageProducer.sendMsg(MessageFactory.newSendResPreControlEmailMessage(goodsResPrecontrolPolicyVO.getId(), "SEND_CYCLE_EMAIL", "Normal"));
	                }
	                //每当“金额/库存”减少${10%}，发邮件提醒销量。${10%}为变量，根据用户实际选择为准。
	                if(rules.contains("loss")){
	                    String valueStr = null;
	                    for(ResWarmRule rule : resWarmRules){
	                        if("loss".equals(rule.getName())){
	                            valueStr = rule.getValue();
	                        }
	                    }
	                    if(null == valueStr){
	                        return;
	                    }
	                    Long totalAmount = goodsResPrecontrolPolicyVO.getAmount();
	                    Integer value = Integer.valueOf(valueStr);
	                    double reduce = totalAmount*(value)/100;
	                    //本次使用数量
	                    Long usedNum = currentAmount - leftAmount;
	                    //本次使用占比
	                    double percent = usedNum/totalAmount.doubleValue();
	                    
	                    //使用占比 大于等于 设置的比例 就应该发送邮件
	                    if(percent * 100 >= value.doubleValue()){
	                    	LOG.info("按周期-消耗完百分比-发邮件");
	                    	resPreControlEmailMessageProducer.sendMsg(MessageFactory.newSendResPreControlEmailMessage(goodsResPrecontrolPolicyVO.getId(), "SEND_CYCLE_EMAIL", "Normal"));
	                    }else{
	                    	double ceil =  currentAmount/totalAmount.doubleValue();
	                    	BigDecimal b = new BigDecimal(ceil);
	                    	b = b.setScale(1, BigDecimal.ROUND_FLOOR);
	                    	double floor =  leftAmount/totalAmount.doubleValue();
	                    	BigDecimal d = new BigDecimal(floor);
	                    	d = d.setScale(1, BigDecimal.ROUND_DOWN);
	                    	double split = totalAmount * (d.doubleValue() +(b.doubleValue()-d.doubleValue()));
	                    	if(currentAmount>=split && split>leftAmount){
	                    		LOG.info("按周期-消耗完百分比-发邮件");
	                    		resPreControlEmailMessageProducer.sendMsg(MessageFactory.newSendResPreControlEmailMessage(goodsResPrecontrolPolicyVO.getId(), "SEND_CYCLE_EMAIL", "Normal"));
	                    	}
	                    }
	                    
	                    /*for(int i = 1;totalAmount-reduce*i>=0;i++){
	                        if(currentAmount >= totalAmount-reduce*i && leftAmount < totalAmount-reduce*i){
	                        	resPreControlEmailMessageProducer.sendMsg(MessageFactory.newSendResPreControlEmailMessage(goodsResPrecontrolPolicyVO.getId(), "SEND_CYCLE_EMAIL", "Normal"));
	                            break;
	                        }
	                    }*/
	                }
	            }
	        }
    	}catch(Exception e){
    		LOG.error("新酒套餐====》买断预控，发送邮件出错："+e.getMessage());
    	}
    }
    /**
	 * 
	 * 保存日志
	 * 
	 */
	public void insertOrderLog( OrdOrder order ,String type,String assignor,String memo,String cancelCode,String reason){
		if (order != null) {
			String zhOrderStatus = OrderEnum.ORDER_STATUS.getCnName(type);
			Long orderId=order.getOrderId();
			
		    if (cancelCode != null && StringUtils.isNumeric(cancelCode)) {
		    	BizDictDef bizDictDef=dictDefClientService.findDictDefById(new Long(cancelCode)).getReturnContent();
		    	if (bizDictDef != null) {
		    		cancelCode = bizDictDef.getDictDefName();
		    	}
		    }
		    
		    if (cancelCode == null) {
		    	cancelCode = "默认";
		    }
		    
		    //拼接日志内容
			String cancelStr="   取消类型："+ OrderEnum.ORDER_CANCEL_CODE.getCnName(cancelCode) +",取消原因："+reason;
			String content="将编号为["+orderId+"]的订单活动变更为["+ zhOrderStatus +"]"+cancelStr;
			if (order.isSupplierOrder()) {
				content+="。此订单为供应商订单，自动发送消息给供应商，等待供应商确认后才可会真正取消订单";
			}
			
			comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER, 
					orderId, 
					orderId, 
					assignor, 
					content, 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.name(), 
					ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.getCnName()+"["+ zhOrderStatus +"]",
					memo);
		}
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
	public void fillStartProcessParam(OrdOrder order, Map<String, Object> params) {
		params.put("orderId", order.getOrderId());
		params.put("mainOrderItem", order.getMainOrderItem());
		params.put("order", order);
	}
	
	/**
	 * 同步订单权限
	 * @param order
	 */
	@Async
	public void synManagerIdPerm(OrdOrder order) {
		if (LOG.isDebugEnabled())
			LOG.debug("start method<synManagerIdPerm>");
		if(null ==order || null ==order.getOrderId()) return;
		try{
			StringBuilder sBuilder =new StringBuilder(80);
			List<Long> keyList = new ArrayList<Long>();
		
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
	public static final String COMMA = ",";
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
					params.put("objectId", ordOrderItem.getSuppGoodsId());
					LOG.info(" GOODS objectId" + ordOrderItem.getSuppGoodsId());

					List<PromForbidBuy> promForbidBuy2 = promForbidBuyClientService
							.getPromForbidBuyByParams(params);
					if (promForbidBuy2 == null || promForbidBuy2.isEmpty()) {
						LOG.info("has been return" + orderId);
						return;
					}
					PromForbidBuy pb = promForbidBuy2.get(0);
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
								+ ordOrderItem.getSuppGoodsId() + COMMA
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
										+ ordOrderItem.getSuppGoodsId() + COMMA
										+ quantitytype;
								serchekeyMap14.put(seacheKey2, ordOrderItem
										.getQuantity().intValue());
							}
							if (ids2.size() > 1) {
								for (String phoneNo : ids2) {
									String seacheKey2 = periodtype + COMMA
											+ time + COMMA + phoneNo + COMMA
											+ objecttype + COMMA
											+ ordOrderItem.getSuppGoodsId()
											+ COMMA + quantitytype;
									serchekeyMap14.put(seacheKey2, 1);
								}
							}
						}// 判断如果是订单数量限购，如果是根据订单数量限购的，那么直接将数量设置成1 订单只可能去掉一笔
						else {
							for (String phoneNo : ids2) {
								String seacheKey2 = periodtype + COMMA + time
										+ COMMA + phoneNo + COMMA + objecttype
										+ COMMA + ordOrderItem.getSuppGoodsId()
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
										+ ordOrderItem.getSuppGoodsId() + COMMA
										+ quantitytype;
								serchekeyMap14.put(seacheKey2, ordOrderItem
										.getQuantity().intValue());
							} else if (ids3.size() > 1) {
								for (String idtypeAndIds : ids3) {
									String seacheKey2 = periodtype + COMMA
											+ time + COMMA + idtypeAndIds
											+ COMMA + objecttype + COMMA
											+ ordOrderItem.getSuppGoodsId()
											+ COMMA + quantitytype;
									serchekeyMap14.put(seacheKey2, 1);
								}
							}
						} else {
							for (String idtypeAndIds : ids3) {
								String seacheKey2 = periodtype + COMMA + time
										+ COMMA + idtypeAndIds + COMMA
										+ objecttype + COMMA
										+ ordOrderItem.getSuppGoodsId() + COMMA
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

	public static String formartdate(Date date) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return simpleDateFormat.format(date);
	}
	public static String getPeriodType(PromForbidBuy pb) {

		if ("CREATEDATE".equals(pb.getPeriodType())) {
			return "C";
		}
		if ("VISITDATE".equals(pb.getPeriodType())) {
			return "V";
		}
		return "N";
	}
	public static String getObjectTypeKey(PromForbidBuy pb) {
		String objectTypekey = "";
		if ("GOODS".equals(pb.getObjectType())) {
			objectTypekey = "G";
		}
		if ("PRODUCT".equals(pb.getObjectType())) {
			objectTypekey = "P";
		}
		if ("CATEGORY".equals(pb.getObjectType())) {
			objectTypekey = "C";
		}
		return objectTypekey;
	}
	
	public static Date getDate(PromForbidBuy pb, Date visdate, Date createDate) {

		if ("CREATEDATE".equals(pb.getPeriodType())) {

			return createDate;
		}
		if ("VISITDATE".equals(pb.getPeriodType())) {
			return visdate;
		}
		if ("N".equals(pb.getPeriodType())) {

			if (pb.getVisitBeginDate() != null && pb.getVisitEndDate() != null) {
				return visdate;
			} else {
				return createDate;
			}

		}
		return null;
	}

	
	public static String getQuantityType(PromForbidBuy pb) {
		String op = pb.getQuantityType();
		if ("ORDER".equals(op)) {
			return "O";
		}

		else if ("PRODUCT_GOODS".equals(op)) {
			return "P";
		}
		return "";
	}


	public static List<String> allids14(PromForbidBuy pb, OrdOrder order) {
		ArrayList<String> ids = new ArrayList<String>();
		String ptype = pb.getPtype();
		if (ptype.contains("1")) {
			String userId = getUserId(order);
			ids.add(userId);
		}
		if (ptype.contains("4")) {
			ids.add(getMobileEquipmentNo(order));
		}
		return ids;
	}
	public static String getUserId(OrdOrder order) {

		return String.valueOf(order.getUserNo());
	}
	public static String getMobileEquipmentNo(OrdOrder order) {
		LOG.info("mobileequipmentno is" + order.getMobileEquipmentNo());
		return order.getMobileEquipmentNo();
	}
	public static List<String> allids2(PromForbidBuy pb,
			List<OrdPerson> personTraller, OrdOrder order) {
		ArrayList<String> ids = new ArrayList<String>();
		String ptype = pb.getPtype();
		if (ptype.contains("2")) {
			List<String> phoneslist = getphonesIds(order, personTraller);
			ids.addAll(phoneslist);
		}
		return ids;
	}
	public static List<String> getphonesIds(OrdOrder order,
			List<OrdPerson> personlist) {
		ArrayList<String> ids = new ArrayList<String>();
		for (OrdPerson ordPerson : personlist) {
			String mobile = ordPerson.getMobile();
			ids.add(mobile);
		}
		LOG.info("THE PHONE LIST ORDER IS " + personlist);
		return ids;
	}

	public static List<String> allids3(PromForbidBuy pb,
			List<OrdPerson> personTraller, OrdOrder order) {
		ArrayList<String> ids = new ArrayList<String>();
		String ptype = pb.getPtype();
		if (ptype.contains("3")) {
			List<String> idtypeAndidlist = getIdtypeAndIds(order, personTraller);
			ids.addAll(idtypeAndidlist);
		}
		return ids;
	}
	public static List<String> getIdtypeAndIds(OrdOrder order,
			List<OrdPerson> personlist) {
		ArrayList<String> ids = new ArrayList<String>();
		for (OrdPerson ordPerson : personlist) {
			String idtype = ordPerson.getIdType();
			String idNo = ordPerson.getIdNo();
			ids.add(idtype + idNo);
		}
		LOG.info("THE ID AND TYPE IS " + ids);
		return ids;
	}
	
	public static List<String> allids23(PromForbidBuy pb,
			List<OrdPerson> personTraller, OrdOrder order) {
		ArrayList<String> ids = new ArrayList<String>();
		String ptype = pb.getPtype();
		if (ptype.contains("2")) {
			List<String> phoneslist = getphonesIds(order, personTraller);
			ids.addAll(phoneslist);
		}
		if (ptype.contains("3")) {
			List<String> idtypeAndidlist = getIdtypeAndIds(order, personTraller);
			ids.addAll(idtypeAndidlist);
		}
		return ids;
	}
	public static void findRedisAndMinusQuantity(
			Map<String, Integer> serchekeyMap, JedisTemplate2 jedisTemplate2) {
		LOG.info("INTO findRedisAndMinusQuantity ");
		try {
			if (serchekeyMap != null && serchekeyMap.isEmpty()==false) {
				for (Map.Entry<String, Integer> serchekey : serchekeyMap.entrySet()) {
					// 判断存在与否。
					String key = serchekey.getKey();
					boolean exists = jedisTemplate2.exists(key);
					// 如果存在则做减法。 先拿到redis里面的value 在拿到map里面的value
					// 然后把redis里面的value
					// 减去map里面的value
					LOG.info("exitsts status is" +exists);
					if (exists) {
						Integer needTomin = serchekey.getValue();
						
						Integer quantity= Integer.valueOf(exceptPercent(jedisTemplate2.get(key)));
						
						Integer nowquantity = quantity - needTomin;
						LOG.info("REDIS CANLE KEY AND QUANTITY IS"
								+ serchekey.getKey() + "--" + nowquantity);
						if (nowquantity <= 0) {
							nowquantity = 0;
						}
						jedisTemplate2.set(serchekey.getKey(),
								String.valueOf(nowquantity));
					}
					// 不存在则啥也不干
				}
			}
		} catch (Exception e) {
			LOG.info("REDIS EXCEPTION");
		}
	}
	public static String exceptPercent(String input) {
		// 专门针对限购redis里面的数量 所以直接设置成0
		if (input == null || ("\"\"").equals(input)) {
			return "0";
		}
		Pattern pattern = Pattern.compile("^\".*\"$");
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			input = input.substring(1, input.length() - 1);
		}
		return input;
	}
}
