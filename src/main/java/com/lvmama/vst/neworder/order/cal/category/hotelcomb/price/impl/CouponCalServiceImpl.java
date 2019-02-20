package com.lvmama.vst.neworder.order.cal.category.hotelcomb.price.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import com.lvmama.dest.api.order.vo.buy.BaseBuyInfo.Coupon;
import com.lvmama.dest.hotel.trade.common.RequestBody;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.CardInfo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombTradeBuyInfoVo;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.UserCouponVO;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdOrderPack;
import com.lvmama.vst.comm.utils.DESCoder;
import com.lvmama.vst.comm.utils.ExceptionFormatUtil;
import com.lvmama.vst.comm.utils.Pair;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.ResultHandle;
import com.lvmama.vst.comm.vo.Constant.ORDER_FAVORABLE_TYPE;

import com.lvmama.vst.comm.vo.order.FavorStrategyInfo;

import com.lvmama.vst.neworder.order.cal.category.hotelcomb.price.CouponCalService;
import com.lvmama.vst.neworder.order.create.builder.category.hotel.factory.vo.CouponAmount;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.order.vo.OrdOrderDTO;
import com.lvmama.vst.pet.adapter.FavorServiceAdapter;
import com.lvmama.vst.pet.adapter.IOrdUserOrderServiceAdapter;
import com.lvmama.vst.pet.adapter.IPayPaymentServiceAdapter;
import com.lvmama.vst.pet.vo.VstCashAccountVO;

@Component("couponCalService")
public class CouponCalServiceImpl implements CouponCalService {

	private static final Logger logger = LoggerFactory.getLogger(CouponCalServiceImpl.class);

	@Autowired
	protected FavorServiceAdapter favorService;
	
	

	@Override
	public CouponAmount getOrderCoupoAmount(OrdOrderDTO order, OrderHotelCombBuyInfo buyInfo) {
		logger.info("CouponCalServiceImpl---getOrderCoupoAmount--started");
		CouponAmount coupons = new CouponAmount();
		/* 优惠券验证计算 start */
		Long orderPrice = order.getOughtAmount();
		Long rebateAmount = order.getRebateAmount();
		Long couponAmount = 0L;
		String youhuiType = buyInfo.getYouhui();
		if (StringUtils.isNotEmpty(youhuiType) && ORDER_FAVORABLE_TYPE.coupon.getCode().equals(youhuiType)) {
			List<Coupon> couponList = buyInfo.getCouponList();
			if (null != couponList && couponList.size() > 0) {
				//buyInfo.setOrderTotalPrice(orderPrice);// 设置订单总价
				List<ResultHandle> couponResultHandles = new ArrayList<ResultHandle>(2);

				// for (Coupon coupon : couponList) {
				Coupon coupon = couponList.get(0);
				if (StringUtil.isNotEmptyString(coupon.getCode())) {
					Pair<FavorStrategyInfo, Object> resultPair = this.calCoupon(order, buyInfo);
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
				coupons.setCouponResultHandles(couponResultHandles);
			}
		}
		coupons.setAmount(couponAmount);	
		logger.info("CouponCalServiceImpl---getOrderCoupoAmount--end--coupons:"+JSONObject.toJSONString(coupons));
		
		return coupons;
	}

	public Pair<FavorStrategyInfo, Object> calCoupon(OrdOrderDTO orderDto, OrderHotelCombBuyInfo buyInfo) {
		// TODO Auto-generated method stub
		logger.info("CouponCalServiceImpl---calCoupon--started");
		Pair<FavorStrategyInfo, Object> result = new Pair<FavorStrategyInfo, Object>();
		List<Coupon> list = buyInfo.getCouponList();
		if (null != list && list.size() > 0) {

			OrdOrder order = new OrdOrder();
			order.setDistributionChannel(buyInfo.getDistributionChannel());
			order.setDistributorId(buyInfo.getDistributionId());
			order.setOughtAmount(orderDto.getOughtAmount());

			try {
				result = favorService.calculateFavor(order, list.get(0).getCode(), buyInfo.getUserNo());
			} catch (Exception e) {
				logger.error(ExceptionFormatUtil.getTrace(e));
			}
		} else {
			   result.setMsg("优惠券代码为空.");
		}
		logger.info("CouponCalServiceImpl---calCoupon--end---result:"+JSONObject.toJSONString(result));
		return result;
	}

	
}
