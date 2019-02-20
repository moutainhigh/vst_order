package com.lvmama.vst.order.processer;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.lvmama.comm.jms.Message;
import com.lvmama.comm.jms.MessageProcesser;
import com.lvmama.comm.search.vst.vo.SuppGoodsRefund;
import com.lvmama.dest.interfaces.vst.comm.model.ProdRefund;
import com.lvmama.vst.back.order.po.OrdOrderItem;

import kafka.utils.Json;

/*
 * 符合过期退条件的支付完成订单的筛选器
 * 
 * 功能：通过接收订单支付完成的消息，获取相关订单的详细信息，并加以筛选，将符合过期退条件的订单持久化到【过期退意向订单表】
 * 
 * 作为消息消费者MessageConsumer的消息处理类通过spring完成实例化
 * 
 */
public class PayedTicketOverdueQualifyingFilter implements MessageProcesser {
	private final static Log log = LogFactory.getLog(PayedTicketOverdueQualifyingFilter.class);

	@Override
	public void process(Message msg) {

	}

	/**
	 * 检查子订单的退款策略是否符合过期退条件
	 * 
	 * @param subOrder
	 *            子订单
	 * @return 订单可退改且是整单退、无损退，结果为true
	 */
	boolean checkEligibilityAccording2RefundPolicy(OrdOrderItem subOrder) {
		log.debug("checkEligibilityAccording2RefundPolicy subOrder -> " + JSON.toJSONString(subOrder));

		if (subOrder == null)
			return false;

		try {
			// 判断是否可退该、是否是整单退
			if (subOrder.getCancelStrategy() == null || subOrder.getCancelStrategy().trim().equals("")
					|| !subOrder.getCancelStrategy().equals(ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode()))
				return false;

			if (subOrder.getRefundRules() == null || subOrder.getRefundRules().trim().equals(""))
				return false;

			// 判断是否是无损退
			List<SuppGoodsRefund> refundRuleL = JSON.parseArray(subOrder.getRefundRules(), SuppGoodsRefund.class);
			if (refundRuleL == null || refundRuleL.size() < 1)
				return false;
			for (SuppGoodsRefund refundRule : refundRuleL) {
				if (!refundRule.getCancelStrategy().equals(ProdRefund.CANCELSTRATEGYTYPE.RETREATANDCHANGE.getCode()))
					return false;

				if (refundRule.getDeductValue() == null || !refundRule.getDeductValue().equals(new Long(0)))
					return false;
			}

		} catch (Exception e) {
			log.error("fail 2 check refund policy");
			log.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	/**
	 * 检查子订单商品快照中的【是否支持过期退】字段
	 * 
	 * @param subOrder
	 *            子订单
	 * @return 支持过期退结果为true
	 */
	boolean checkOverdueTicketCancellationSupportedFlag(OrdOrderItem subOrder) {
		log.debug("checkOverdueTicketCancellationSupportedFlag subOrder -> " + JSON.toJSONString(subOrder));

		if (subOrder == null)
			return false;
		
		if (subOrder.getContent() == null || subOrder.getContent().equals(""))
			return false;
		
		if (!subOrder.isExpiredRefund())
			return false;

		return true;
	}
}
