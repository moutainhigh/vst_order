package com.lvmama.vst.neworder.order.create.deduct.product.category.vst;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.lvmama.cmt.comm.vo.BusinessException;
import com.lvmama.dest.api.order.vo.HotelOrderUpdateStockDTO;
import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.client.precontrol.service.ResWarmRuleClientService;
import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.po.ResWarmRule;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrderStatusEnum;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.service.book.IOrderSaveService;
import com.lvmama.vst.order.service.book.OrderSaveService;
import com.lvmama.vst.order.vo.OrdOrderDTO;

@Component("vstDeductStockService")
public class VstDeductStockServiceImpl implements VstDeductStockService {

	private static final Logger logger = LoggerFactory.getLogger(VstDeductStockServiceImpl.class);

	@Autowired
	private OrderSaveService orderSaveService;

	@Autowired
	OrdOrderStockDao ordOrderStockDao;

	@Autowired
	private ResPreControlService resControlBudgetRemote;

	@Resource
	private TopicMessageProducer resPreControlEmailMessageProducer;

	@Autowired
	private ResWarmRuleClientService resWarmRuleClientService;

	@Override
	public void deductStock(OrdOrderDTO order) {
		List<OrdOrderItem> orderItemList = order.getOrderItemList();
		// 扣减基本库存
		try {
			logger.info("deductStock-----orderSaveService.deductStock---start");
			if (order.getUpdateStockMap() != null) {
				List<HotelOrderUpdateStockDTO> asynchronousOrdUpdateStockList = new ArrayList<>();
				orderSaveService.deductStock(order,asynchronousOrdUpdateStockList);
			}
			logger.info("deductStock-----扣减预控库---start");
	
			for (OrdOrderItem orderItem : orderItemList) {

				// 扣减预控库存
				// 如果是预控资源那么进行扣减
				if ("Y".equals(orderItem.getBuyoutFlag())) {
					SuppGoods goods = orderItem.getSuppGoods();
					Long goodsId = goods.getSuppGoodsId();
					Date visitDate = orderItem.getVisitTime();

					int thisOrderItemCategoryId = orderItem.getCategoryId().intValue();
					switch (thisOrderItemCategoryId) {
					case 1:
						logger.info("酒店更新预控资源请查看saveBussiness.saveAddition(order, orderItem)方法");
						break;

					default:
						// 通过商品Id和游玩时间获取，该商品在该时间，所在的预控策略对象
						GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = resControlBudgetRemote
								.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
						// 如果能找到该有效预控的资源
						// --不在检验是否还有金额或者库存的剩余
						// (goodsResPrecontrolPolicyVO.getLeftNum() >0 ||
						// goodsResPrecontrolPolicyVO.getLeftAmount()>0)
						if (goodsResPrecontrolPolicyVO != null) {
							Long controlId = goodsResPrecontrolPolicyVO.getId();
							String resType = goodsResPrecontrolPolicyVO.getControlType();
							// 购买该商品的数量
							Long reduceNum = orderItem.getBuyoutQuantity();
							Long leftQuantity = goodsResPrecontrolPolicyVO.getLeftNum();
							Long leftAmount = goodsResPrecontrolPolicyVO.getLeftAmount();
							boolean cancelFlag = "Y".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay()) ? true
									: false;
							boolean reduceResult = false;

							if (ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equalsIgnoreCase(resType)
									&& leftAmount != null && (leftAmount > 0 || cancelFlag)) {
								// 该商品在该时间内的剩余库存
								Long amountId = goodsResPrecontrolPolicyVO.getAmountId();
								// 按金额预控
								Long value = orderItem.getBuyoutTotalPrice();
								Long leftValue = leftAmount - value;
								// 金额预控最小只能是0
								leftValue = leftValue < 0 ? 0L : leftValue;
								reduceResult = resControlBudgetRemote.updateAmountResPrecontrolPolicy(amountId,
										controlId, visitDate, leftValue);
								if (reduceResult) {
									logger.info("按金额预控-更新成功");
									sendBudgetMsgToSendEmail(goodsResPrecontrolPolicyVO, leftAmount, leftValue);
								}
								// 如果预控金额已经没了，清空该商品在这一天的预控缓存
								if (leftValue == 0 && reduceResult && !cancelFlag) {
									resControlBudgetRemote.handleResPrecontrolSaledOut(goodsResPrecontrolPolicyVO,
											visitDate, goodsId);

								}
							} else if (ResControlEnum.RES_PRECONTROL_POLICY_TYPE.inventory.name()
									.equalsIgnoreCase(resType) && leftQuantity != null
									&& (leftQuantity > 0 || cancelFlag)) {
								// 该商品在该时间内的剩余库存
								Long leftStore = leftQuantity - reduceNum;
								// 库存最小只能是0
								leftStore = leftStore < 0 ? 0L : leftStore;
								Long storeId = goodsResPrecontrolPolicyVO.getStoreId();
								// 按库存预控
								reduceResult = resControlBudgetRemote.updateStoreResPrecontrolPolicy(storeId, controlId,
										visitDate, leftStore);
								if (reduceResult) {
									logger.info("按库存预控-更新成功");
									sendBudgetMsgToSendEmail(goodsResPrecontrolPolicyVO, leftQuantity, leftStore);
								}
								// 如果预控库存已经没了，清空该商品在这一天的预控缓存
								if (leftStore == 0 && reduceResult && !cancelFlag) {
									resControlBudgetRemote.handleResPrecontrolSaledOut(goodsResPrecontrolPolicyVO,
											visitDate, goodsId);
								}
							}
							if (reduceResult) {
								logger.info("扣减预控资源成功，订单号：" + orderItem.getOrderId() + "子订单号："
										+ orderItem.getOrderItemId() + ",商品id:" + orderItem.getSuppGoodsId() + "，数量："
										+ orderItem.getBuyoutQuantity() + ",总价：" + orderItem.getBuyoutTotalPrice());
							}
						}
						break;
					}

				}
			}
		} catch (BusinessException e) {
			throw new BusinessException(
					String.format("%s,%s", OrderStatusEnum.ORDER_ERROR_CODE.HOTEL_COMB_DEDUCT_FAILURE.getErrorCode(),
							OrderStatusEnum.ORDER_ERROR_CODE.HOTEL_COMB_DEDUCT_FAILURE.getContent()));
		}
		//
	}

	/**
	 * 发送预控消息
	 * 
	 * @param goodsResPrecontrolPolicyVO
	 * @param currentAmount
	 *            当前剩余金额/库存
	 * @param leftAmount
	 *            剩余金额/库存
	 */
	@Async
	private void sendBudgetMsgToSendEmail(GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO, Long currentAmount,
			Long leftAmount) {
		try {
			List<ResWarmRule> resWarmRules = resWarmRuleClientService
					.findAllRulesById(goodsResPrecontrolPolicyVO.getId());
			List<String> rules = new ArrayList<String>();
			for (ResWarmRule rule : resWarmRules) {
				rules.add(rule.getName());
			}
			if (!DateUtil.accurateToDay(new Date()).after(goodsResPrecontrolPolicyVO.getTradeExpiryDate())) {
				// 按日预控
				if (ResControlEnum.CONTROL_CLASSIFICATION.Daily.name()
						.equalsIgnoreCase(goodsResPrecontrolPolicyVO.getControlClassification())) {
					// 买断“金额/库存”全部消耗完时，发邮件提醒
					if (rules.contains("lossAll") && leftAmount.longValue() == 0) {
						logger.info("按日-消耗完毕-发邮件");
						resPreControlEmailMessageProducer.sendMsg(
								MessageFactory.newSendResPreControlEmailMessage(goodsResPrecontrolPolicyVO.getId(),
										"SEND_DAILY_EMAIL", DateUtil.formatSimpleDate((new Date()))));
					}
				}
				// 按周期
				if (ResControlEnum.CONTROL_CLASSIFICATION.Cycle.name()
						.equalsIgnoreCase(goodsResPrecontrolPolicyVO.getControlClassification())) {
					// 买断“金额/库存”全部消耗完时，发邮件提醒
					if (rules.contains("lossAll") && leftAmount.longValue() == 0) {
						logger.info("按周期-消耗完毕-发邮件");
						resPreControlEmailMessageProducer.sendMsg(MessageFactory.newSendResPreControlEmailMessage(
								goodsResPrecontrolPolicyVO.getId(), "SEND_CYCLE_EMAIL", "Normal"));
					}
					// 每当“金额/库存”减少${10%}，发邮件提醒销量。${10%}为变量，根据用户实际选择为准。
					if (rules.contains("loss")) {
						String valueStr = null;
						for (ResWarmRule rule : resWarmRules) {
							if ("loss".equals(rule.getName())) {
								valueStr = rule.getValue();
							}
						}
						if (null == valueStr) {
							return;
						}
						Long totalAmount = goodsResPrecontrolPolicyVO.getAmount();
						Integer value = Integer.valueOf(valueStr);
						double reduce = totalAmount * (value) / 100;
						// 本次使用数量
						Long usedNum = currentAmount - leftAmount;
						// 本次使用占比
						double percent = usedNum / totalAmount.doubleValue();

						// 使用占比 大于等于 设置的比例 就应该发送邮件
						if (percent * 100 >= value.doubleValue()) {
							logger.info("按周期-消耗完百分比-发邮件");
							resPreControlEmailMessageProducer.sendMsg(MessageFactory.newSendResPreControlEmailMessage(
									goodsResPrecontrolPolicyVO.getId(), "SEND_CYCLE_EMAIL", "Normal"));
						} else {
							double ceil = currentAmount / totalAmount.doubleValue();
							BigDecimal b = new BigDecimal(ceil);
							b = b.setScale(1, BigDecimal.ROUND_FLOOR);
							double floor = leftAmount / totalAmount.doubleValue();
							BigDecimal d = new BigDecimal(floor);
							d = d.setScale(1, BigDecimal.ROUND_DOWN);
							double split = totalAmount * (d.doubleValue() + (b.doubleValue() - d.doubleValue()));
							if (currentAmount >= split && split > leftAmount) {
								logger.info("按周期-消耗完百分比-发邮件");
								resPreControlEmailMessageProducer
										.sendMsg(MessageFactory.newSendResPreControlEmailMessage(
												goodsResPrecontrolPolicyVO.getId(), "SEND_CYCLE_EMAIL", "Normal"));
							}
						}

						/*
						 * for(int i = 1;totalAmount-reduce*i>=0;i++){
						 * if(currentAmount >= totalAmount-reduce*i &&
						 * leftAmount < totalAmount-reduce*i){
						 * resPreControlEmailMessageProducer.sendMsg(
						 * MessageFactory.newSendResPreControlEmailMessage(
						 * goodsResPrecontrolPolicyVO.getId(),
						 * "SEND_CYCLE_EMAIL", "Normal")); break; } }
						 */
					}
				}
			}
		} catch (Exception e) {
			logger.error("买断预控，发送邮件出错：" + e.getMessage());
		}
	}

}
