package com.lvmama.vst.neworder.order.cal.category.hotelcomb.price.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lvmama.vst.back.biz.po.BizCategory;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.utils.order.ProductCategoryUtil;
import com.lvmama.vst.comm.vo.Constant.ORDER_FAVORABLE_TYPE;
import com.lvmama.vst.neworder.order.cal.category.hotelcomb.price.BonusCalService;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.BonusAmount;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.pet.adapter.IOrdUserOrderServiceAdapter;

@Component("bonusAmount")
public class BonusCalServiceImpl implements BonusCalService {

	private static final Logger logger = LoggerFactory.getLogger(BonusCalServiceImpl.class);

	@Autowired
	private IOrdUserOrderServiceAdapter ordUserOrderServiceAdapter;

	@Override
	public BonusAmount getBonusAmountOfBuyUserNo(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		BonusAmount bonusAmount = new BonusAmount();
		Long maxBonus = 0L;
		Long bonus = 0L;
		String youhuiType = buyInfo.getYouhui();
		Long orderPrice = order.getOughtAmount();
		List<String> goodsCategorys = new ArrayList<String>();
		try {
			for (OrdOrderItem orderItem : order.getOrderItemList()) {
				SuppGoods suppGoods = orderItem.getSuppGoods();
				BizCategory category = suppGoods.getProdProduct().getBizCategory();
				String categoryCode = category.getCategoryCode();
				// 记录商品品类以及距离类型
				if (ProductCategoryUtil.isRoute(categoryCode)) {
					String type = category.getCategoryCode() + "_" + suppGoods.getProdProduct().getProductType();
					goodsCategorys.add(type);
				} else {
					goodsCategorys.add(categoryCode);
				}
			}
			logger.info("-----------------------------------buyInfo.getUserNo():" + buyInfo.getUserNo());
			logger.info("-----------------------------------orderPrice" + buyInfo.getUserNo());
			logger.info("-----------------------------------goodsCategorys:" + goodsCategorys);
			maxBonus = ordUserOrderServiceAdapter.getOrderBonusCanPayAmount(buyInfo.getUserNo(), orderPrice,
					goodsCategorys);
			logger.info("-----------------------------------maxBonus:" + maxBonus);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		if (StringUtils.isNotEmpty(youhuiType) && ORDER_FAVORABLE_TYPE.bonus.getCode().equals(youhuiType)) {
			logger.info("ordUserOrderServiceAdapter.getOrderBonusCanPayAmount userNo:" + buyInfo.getUserNo()
					+ ",orderPrice:" + orderPrice + ",goodsCategorys:" + goodsCategorys);
			logger.info("maxBonus ======" + maxBonus);
			bonus = maxBonus;
			String target = buyInfo.getTarget();
			// 如果是抵扣现金框触发
			if (StringUtils.isNotEmpty(target) && target.equals(ORDER_FAVORABLE_TYPE.bonus.getCode())) {
				bonus = PriceUtil.convertToFen(buyInfo.getBonusYuan());
				if (bonus > maxBonus) {
					bonus = maxBonus;
				}
			}
		}
		logger.info("可用奖金计算完成---------------------------------");
		bonusAmount.setBonusAmount(bonus);

		bonusAmount.setBonusMax(maxBonus);

		return bonusAmount;
	}

}
