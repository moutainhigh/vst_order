package com.lvmama.vst.neworder.order.cal.category.hotelcomb.price.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.prom.po.PromPromotion;
import com.lvmama.vst.back.prom.po.PromotionEnum;
import com.lvmama.vst.back.service.VstPromotionOrderService;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.vo.Constant.ACTIVITY_TYPE;
import com.lvmama.vst.neworder.order.cal.category.hotelcomb.price.PromotionForHotelcomService;
import com.lvmama.vst.neworder.order.cal.category.hotelcomb.product.CalProductServiceImpl;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.order.service.impl.PromotionBussiness;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.pet.adapter.QueryPaymentGatewayServiceAdapter;

@Component("hotelCombPromotion")
public class PromotionServiceImpl implements PromotionForHotelcomService {

	private static final Logger logger = LoggerFactory.getLogger(PromotionServiceImpl.class);
	@Autowired
	private PromotionBussiness promotionBussiness;

	@Autowired
	private VstPromotionOrderService vstPromotionOrderServiceRemote;
	
	@Autowired
	private QueryPaymentGatewayServiceAdapter queryPaymentGatewayServiceAdapter;	

	@Override
	// 分销促销筛选   如果分销平台有促销,就取代vst_prom的促销
	public void checkPromotion(List<PromPromotion> promotionList, OrderHotelCombBuyInfo buyInfo) {
		Long subDistributionId = buyInfo.getSubDistributorId();
		//如果是渠道促销，返回渠道名称
		buildPayChannelCnName(promotionList);
		//没有字渠道
		if(subDistributionId==null){
			return;
		}
		if (!CollectionUtils.isEmpty(promotionList)) {
			for (PromPromotion promPromotion : promotionList) {
				logger.info("平台促销log" + promPromotion.getPromPromotionId());
			}
		} else {
			logger.info("平台促销log信息为空");
		}

		if (subDistributionId != null) {
			promotionList = vstPromotionOrderServiceRemote.getOrderPromotionList(promotionList, subDistributionId);
			if (!CollectionUtils.isEmpty(promotionList)) {
				for (PromPromotion promPromotion : promotionList) {
					logger.info("分销筛选平台促销log" + promPromotion.getPromPromotionId());
				}
			} else {
				logger.info("分销筛选平台促销log信息为空");
			}
		}
		if (promotionList != null) {
			 Long subDistributorId = buyInfo.getSubDistributorId();
			if (subDistributionId == 107 || subDistributionId == 108 || subDistributionId == 110) {
				// 检查促销可用余额是否满足
				Iterator<PromPromotion> it = promotionList.iterator();
				while (it.hasNext()) {
					PromPromotion promPromotion = it.next();
					if (promPromotion.getPromAmount() != null) {
						long usedAmount = promPromotion.getUsedAmount() == null ? 0L : promPromotion.getUsedAmount();
						long balance = promPromotion.getPromAmount() - usedAmount;
						// 活动可用余额大于等于促销金额才存
						logger.info("优惠条件剩余可用金额" + promPromotion.getPromAmount() + usedAmount + "+++++++++++++当前优惠金额"
								+ promPromotion.getDiscountAmount());
						if (balance < promPromotion.getDiscountAmount()) {
							it.remove();
							logger.info("去除优惠条件剩余可用金额" + balance + "+++++++++++++当前优惠金额"
									+ promPromotion.getDiscountAmount());

						}
					}
				}

			}
		}
	
	}

	@Override
	public List<PromPromotion> findPromPromotion(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		List<PromPromotion> result = new ArrayList<PromPromotion>();
		try {

			if (org.apache.commons.collections.CollectionUtils.isNotEmpty(order.getOrderItemList())) {
			
				for (OrdOrderItem orderItem : order.getOrderItemList()) {
					// 次规格产品不参与促销
					if ("Y".equals(orderItem.getSuppGoods().getProdProductBranch().getBizBranch().getAttachFlag())) {
						if (StringUtils.isNotEmpty(
								(orderItem.getSuppGoods().getProdProduct().getBizCategory().getPromTarget()))) {
							List<PromPromotion> list = promotionBussiness.makeSuppGoodsPromotion(order, orderItem,
									PromotionEnum.PRICE_TYPE.DISTRIBUTOR_TYPE.name(), buyInfo.getUserNo());
							if (!list.isEmpty()) {
								result.addAll(list);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(ExceptionFormatUtil.getTrace(e));
		}
		return result;
	}

	/**
	 * 如果是渠道优惠，绑定支付渠道中文名
	 * @param promotionList
	 */
	public void buildPayChannelCnName(List<PromPromotion> promotionList){
		try {
			if(promotionList!=null){
				Map<String, String> paymentGate = queryPaymentGatewayServiceAdapter.getPaymentGateway();
				for(PromPromotion prom:promotionList){
					if(ACTIVITY_TYPE.ORDERCHANNELFAVORABLE.getCode().equals(prom.getPromitionType())){
						prom.setChannelOrder(paymentGate.get(prom.getChannelOrder()));
					}
				}
			}
		} catch (Exception e) {
			logger.error(ExceptionFormatUtil.getTrace(e));
		}
	}
}
