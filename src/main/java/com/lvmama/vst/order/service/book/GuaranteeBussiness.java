/**
 * 
 */
package com.lvmama.vst.order.service.book;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrdGuaranteeCreditCard;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.vo.order.GuaranteeCreditCard;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * @author lancey
 *
 */
@Component
public class GuaranteeBussiness {
	
	public void calcOrderGuaranteeType(OrdOrderDTO order){
		if(order.hasNeedPrepaid()){
			return;
		}
		if (order.getMainOrderItem() != null) {
			order.setBookLimitType(order.getMainOrderItem().getBookLimitType());
		}
		
		if (order.getBookLimitType() == null
				|| OrderEnum.GUARANTEE_TYPE.NONE.name().equalsIgnoreCase(order.getBookLimitType())) {
			if (order.getOrderItemList() != null) {
				for (OrdOrderItem orderItem : order.getOrderItemList()) {
					if (orderItem != null) {
						if (orderItem.getBookLimitType() == null
								&& !OrderEnum.GUARANTEE_TYPE.NONE.name().equalsIgnoreCase(orderItem.getBookLimitType())) {
							order.setBookLimitType(orderItem.getBookLimitType());
							break;
						}
					}
				}
			}
		}
	}

	public String initGuaranteeCreditCard(OrdOrderDTO order) {
		String errorMsg = null;
		if ((order != null) && (order.getBuyInfo() != null)) {
			if (errorMsg == null) {
				// 构建信用卡信息
				if (OrderEnum.CREDIT_CARDER_GUARANTEE.GUARANTEE.name().equals(order.getBuyInfo().getNeedGuarantee())) {
					GuaranteeCreditCard guaranteeCreditCard = order.getBuyInfo().getGuarantee();
					if (guaranteeCreditCard != null && guaranteeCreditCard.getCardNo() != null) {
						OrdGuaranteeCreditCard ordCard = makeGuaranteeCreditCard(guaranteeCreditCard, order.getActualAmount());
						if (ordCard != null) {
							//计算担保金额
							ordCard.setGuaranteeAmount(computeOrderGuaranteeTotalAmount(order));
							List<OrdGuaranteeCreditCard> cardList = new ArrayList<OrdGuaranteeCreditCard>();
							ordCard.setGuaranteeAmount(order.getOughtAmount());
							cardList.add(ordCard);
							order.setOrdGuaranteeCreditCardList(cardList);
						}
					} else {
						errorMsg = "请填写信用卡信息。";
					}
				}
			}
		} else {
			errorMsg = "您还没有下单。";
		}

		return errorMsg;
	}
	
	/**
	 * 构造担保信用卡信息
	 * 
	 * @param vCard
	 * @param guaranteeAmount
	 * @return
	 */
	private OrdGuaranteeCreditCard makeGuaranteeCreditCard(GuaranteeCreditCard vCard, Long guaranteeAmount) {
		OrdGuaranteeCreditCard ordCard = null;

		if (vCard != null) {
			ordCard = new OrdGuaranteeCreditCard();

			ordCard.setCvv(vCard.getCvv());
			ordCard.setExpirationMonth(vCard.getExpirationMonth());
			ordCard.setExpirationYear(vCard.getExpirationYear());
			ordCard.setGuaranteeAmount(guaranteeAmount);
			ordCard.setHolderName(vCard.getHolderName());
			ordCard.setIdNo(vCard.getIdNo());
			ordCard.setIdType(vCard.getIdType());
			ordCard.setCardNo(vCard.getCardNo());
		}

		return ordCard;
	}
	
	/**
	 * 计算订单担保金额
	 * 
	 * @param order
	 * @return
	 */
	private long computeOrderGuaranteeTotalAmount(OrdOrderDTO order) {
		long totalAmount = 0;
		if (order.getOrderItemList() != null) {
			for (OrdOrderItem orderItem : order.getOrderItemList()) {
				if (orderItem != null) {
					if (orderItem.getBookLimitType() != null && !orderItem.getBookLimitType().equalsIgnoreCase(OrderEnum.GUARANTEE_TYPE.NONE.name())) {
						totalAmount = totalAmount + orderItem.getDeductAmount();
					}
				}
			}
		}
		
		return totalAmount;
	}
}
