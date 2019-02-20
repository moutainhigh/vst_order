/**
 * 
 */
package com.lvmama.vst.order.service.book.impl.hotel;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.lvmama.vst.back.client.precontrol.service.ResWarmRuleClientService;
import com.lvmama.vst.back.control.po.ResWarmRule;
import com.lvmama.vst.back.order.po.OrdMulPriceRate;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.DateUtil;

import com.lvmama.vst.order.dao.OrdMulPriceRateDAO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.client.precontrol.service.ResPreControlService;
import com.lvmama.vst.back.client.pub.service.ComPushClientService;
import com.lvmama.vst.back.control.po.ResControlEnum;
import com.lvmama.vst.back.control.vo.GoodsResPrecontrolPolicyVO;
import com.lvmama.vst.back.order.po.OrdOrderHotelTimeRate;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderStock;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComIncreament;
import com.lvmama.vst.order.dao.OrdOrderHotelTimeRateDao;
import com.lvmama.vst.order.service.book.AbstractOrderItemSaveBussiness;
import com.lvmama.vst.order.service.book.OrderItemSaveBussiness;
import com.lvmama.vst.order.service.book.OrderSaveService;
import com.lvmama.vst.order.vo.OrdOrderDTO;

import javax.annotation.Resource;

/**
 * @author lancey
 *
 */
@Component("hotelOrderItemSaveBussiness")
public class HotelOrderItemSaveBussiness extends AbstractOrderItemSaveBussiness implements OrderItemSaveBussiness{
	
	private static final Logger logger = LoggerFactory.getLogger(HotelOrderItemSaveBussiness.class);
	@Autowired
	private OrdOrderHotelTimeRateDao orderHotelTimeRateDao;
	@Autowired
	private ResPreControlService resControlBudgetRemote;

    @Resource
    private TopicMessageProducer resPreControlEmailMessageProducer;

    @Autowired
    private ResWarmRuleClientService resWarmRuleClientService;
    @Autowired
    private ComPushClientService comPushClientService;

	@Autowired
	private OrdMulPriceRateDAO ordMulPriceRateDAO;

	@Override
	public void saveAddition(OrdOrderDTO order, OrdOrderItem orderItem) {
		Long goodsId = orderItem.getSuppGoodsId();
		if ((orderItem != null) && (orderItem.getOrderHotelTimeRateList() != null)) {
			List<Long> goodList = null;
			List<Date> dateList = null;
			for (OrdOrderHotelTimeRate orderHotelTimeRate : orderItem.getOrderHotelTimeRateList()) {
				orderHotelTimeRate.setOrderItemId(orderItem.getOrderItemId());
				orderHotelTimeRateDao.insertSelective(orderHotelTimeRate);
				saveOrderStockInHotelTimeRate(orderHotelTimeRate);
				if("Y".equals(orderHotelTimeRate.getBuyoutFlag())){
					Date visitDate = orderHotelTimeRate.getVisitTime();
					//通过商品Id和游玩时间获取，该商品在该时间，所在的预控策略对象
					GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO = resControlBudgetRemote.getResPrecontrolPolicyByGoodsIdVisitdate(goodsId, visitDate);
					//判断是否是超卖
					boolean cancelFlag = "Y".equalsIgnoreCase(goodsResPrecontrolPolicyVO.getIsCanDelay())?true:false;
					//如果能找到该有效预控的资源  
					//   --不在检验是否还有金额或者库存的剩余  (goodsResPrecontrolPolicyVO.getLeftNum() >0 || goodsResPrecontrolPolicyVO.getLeftAmount()>0)
					if(goodsResPrecontrolPolicyVO != null  ){
						Long controlId = goodsResPrecontrolPolicyVO.getId();
						String resType = goodsResPrecontrolPolicyVO.getControlType();
						//购买该商品的数量
						Long reduceNum = orderHotelTimeRate.getQuantity();
						Long leftQuantity = goodsResPrecontrolPolicyVO.getLeftNum();
						Long leftAmount = goodsResPrecontrolPolicyVO.getLeftAmount();
						
						boolean reduceResult = false;
						
						if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.amount.name().equalsIgnoreCase(resType) && leftAmount!=null &&(leftAmount>0||cancelFlag)){
							//该商品在该时间内的剩余库存
							Long amountId = goodsResPrecontrolPolicyVO.getAmountId();
							//按金额预控
							Long value = orderHotelTimeRate.getSettlementPrice() * orderHotelTimeRate.getBuyoutNum();
							Long leftValue = leftAmount - value;
							//金额预控最小只能是0
							leftValue = leftValue< 0? 0L:leftValue;
							reduceResult = resControlBudgetRemote.updateAmountResPrecontrolPolicy(amountId,controlId, visitDate, leftValue);
							if(reduceResult){
								logger.info("按金额预控-更新成功");
								sendBudgetMsgToSendEmail(goodsResPrecontrolPolicyVO,leftAmount,leftValue);
							}
							//如果预控库存已经没了，清空该商品在这一天的预控缓存
							if(leftValue == 0 && reduceResult){
								if(!cancelFlag)
								resControlBudgetRemote.handleResPrecontrolSaledOut(goodsResPrecontrolPolicyVO,visitDate,goodsId);
							}
						}else if(ResControlEnum.RES_PRECONTROL_POLICY_TYPE.inventory.name().equalsIgnoreCase(resType) && leftQuantity!=null &&(leftQuantity>0||cancelFlag)){
							//该商品在该时间内的剩余库存
							Long leftStore = leftQuantity - reduceNum;
							//库存最小只能是0
							leftStore = leftStore < 0? 0L:leftStore;
							Long storeId = goodsResPrecontrolPolicyVO.getStoreId();
							//按库存预控
							reduceResult = resControlBudgetRemote.updateStoreResPrecontrolPolicy(storeId,controlId, visitDate, leftStore);
							if(reduceResult){
								logger.info("按库存预控-更新成功");
								sendBudgetMsgToSendEmail(goodsResPrecontrolPolicyVO,leftQuantity,leftStore);
							}
							//如果预控库存已经没了，清空该商品在这一天的预控缓存
							if(leftStore == 0 && reduceResult){
								if(!cancelFlag)
								resControlBudgetRemote.handleResPrecontrolSaledOut(goodsResPrecontrolPolicyVO,visitDate,goodsId);
							}
						}
						if(reduceResult){
							logger.info("酒店扣减预控资源成功，订单号："+orderItem.getOrderId()+"子订单号："+orderItem.getOrderItemId()+",商品id:"+orderItem.getSuppGoodsId()+"，日期："+new SimpleDateFormat("yyyy-MM-dd").format(visitDate)+"，数量："+orderHotelTimeRate.getQuantity()+",单价："+orderHotelTimeRate.getSettlementPrice());
						}
					}
				}
			}
		}
		if(orderItem!=null && orderItem.getOrdMulPriceRateList()!=null){
			for (OrdMulPriceRate rate:orderItem.getOrdMulPriceRateList()){
				rate.setOrderItemId(orderItem.getOrderItemId());
				ordMulPriceRateService.addOrdMulPriceRate(rate);
			}
		}
	}
    /**
     * 发送预控消息
     * @param goodsResPrecontrolPolicyVO
     * @param currentAmount 当前剩余金额/库存
     * @param leftAmount  剩余金额/库存
     */
    private void sendBudgetMsgToSendEmail(GoodsResPrecontrolPolicyVO goodsResPrecontrolPolicyVO,Long currentAmount,Long leftAmount){
        List<ResWarmRule> resWarmRules = resWarmRuleClientService.findAllRulesById(goodsResPrecontrolPolicyVO.getId());
        List<String> rules = new ArrayList<String>();
        for(ResWarmRule rule : resWarmRules)
            rules.add(rule.getName());

        if(!DateUtil.accurateToDay(new Date()).after(goodsResPrecontrolPolicyVO.getTradeExpiryDate())){
            //按日预控
            if(ResControlEnum.CONTROL_CLASSIFICATION.Daily.name().equalsIgnoreCase(goodsResPrecontrolPolicyVO.getControlClassification())){
                //买断“金额/库存”全部消耗完时，发邮件提醒
                if(rules.contains("lossAll") && leftAmount.longValue() == 0) {
                	logger.info("按日-消耗完毕-发邮件");
                    resPreControlEmailMessageProducer.sendMsg(MessageFactory.newSendResPreControlEmailMessage(goodsResPrecontrolPolicyVO.getId(), "SEND_DAILY_EMAIL", DateUtil.formatSimpleDate((new Date()))));
                }
            }

            //按周期
            if(ResControlEnum.CONTROL_CLASSIFICATION.Cycle.name().equalsIgnoreCase(goodsResPrecontrolPolicyVO.getControlClassification())){
                //买断“金额/库存”全部消耗完时，发邮件提醒
                if(rules.contains("lossAll") && leftAmount.longValue() == 0){
                	logger.info("按周期-消耗完毕-发邮件");
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
                    Long reduce = totalAmount*(value)/100;
                    reduce = (reduce != 0?reduce:1L);

                    //本次使用数量
                    Long usedNum = currentAmount - leftAmount;
                    //本次使用占比
                    double percent = usedNum/totalAmount.doubleValue();
                    
                    //使用占比 大于等于 设置的比例 就应该发送邮件
                    if(percent * 100 >= value.doubleValue()){
                    	logger.info("按周期-消耗完百分比-发邮件");
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
                    		logger.info("按周期-消耗完百分比-发邮件");
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
				orderStock.setObjectType(OrderEnum.ORDER_STOCK_OBJECT_TYPE.HOTEL_TIME_RATE.name());
				orderStock.setOrderItemId(hotelTimeRate.getOrderItemId());
				
				orderStockDao.insertSelective(orderStock);
			}
		}
	}

	@Override
	public void saveOrderItemPersonRelation(OrdOrderItem orderItem) {
		// TODO Auto-generated method stub
		
	}
}
