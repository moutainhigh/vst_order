package com.lvmama.vst.neworder.order.router.hotel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombOrderService;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombProductService;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombBuyInfoVo;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombBuyOutTimePrice;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombSuppGoodsTimePriceVo;
import com.lvmama.dest.api.hotelcomb.vo.TimePriceQueryVo;
import com.lvmama.dest.api.order.enums.OrderEnum;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.neworder.order.EnhanceBeanUtils;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.neworder.order.router.ITimePriceRouterService;
import com.lvmama.vst.neworder.order.router.vst.VstBuyOutTimePriceImpl;
import com.lvmama.vst.neworder.order.vo.BaseTimePrice;
import com.lvmama.vst.neworder.order.vo.BuyOutTimePrice;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo.GoodsItem;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo.Item;

@Component("hotelCombBuyOutTimePrice")
public class HotelCombTimePrice implements ITimePriceRouterService {

	private static final Logger LOG = LoggerFactory.getLogger(HotelCombTimePrice.class);

	@Resource
	IHotelCombOrderService hotelCombOrderService;

	@Resource
	IHotelCombProductService hotelCombProductService;

	@Override
	public BaseTimePrice findTimePrice(SuppGoods goods, GoodsItem goodItem, Item item) {
		Preconditions.checkNotNull(goodItem.getGoodsId());
		Preconditions.checkNotNull(goodItem.getCheckInDate());
		BuyOutTimePrice buyOutTimePrice = new BuyOutTimePrice();

		RequestBody request = new RequestBody();
		HotelCombBuyInfoVo hotelCombBuyInfoVo = new HotelCombBuyInfoVo();
		List<HotelCombBuyInfoVo.GoodsItem> goodsList = new ArrayList<HotelCombBuyInfoVo.GoodsItem>();
		HotelCombBuyInfoVo.GoodsItem goodsItem = new HotelCombBuyInfoVo().new GoodsItem();
		goodsItem.setCheckInDate(goodItem.getCheckInDate());
		goodsItem.setGoodsId(goodItem.getGoodsId());
		goodsItem.setPricePlanId(goodItem.getPricePlanId());
		goodsItem.setQuantity(goodItem.getQuantity());
		goodsList.add(goodsItem);

		hotelCombBuyInfoVo.setGoodsList(goodsList);
		request.setT(hotelCombBuyInfoVo);
		// 资源初始化
		String statusResource = OrderEnum.RESOURCE_STATUS.AMPLE.name();
		RequestBody<List<String>> response = hotelCombOrderService.calOrderResource(request);
		LOG.info("hotelCombOrderService.calOrderResource" + response.getT());
		if (response.getT() != null) {
			LOG.info("hotelCombOrderService.calOrderResource" + response.getT().get(0) + "size:"
					+ response.getT().size());
		}
		if (response.getT() != null && response.getT().size() == 0) {

			statusResource = OrderEnum.RESOURCE_STATUS.UNVERIFIED.name();

		}
		if (response.getT() != null && response.getT().size() > 0) {
			for (String str : response.getT()) {
				if (OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(str)) {
					statusResource = OrderEnum.RESOURCE_STATUS.UNVERIFIED.name();
					LOG.info("calOrderResource--for--statusResource:" + statusResource);
					break;
				}
			}
		}
		LOG.info("calOrderResource--end--statusResource:" + statusResource);
		buyOutTimePrice.setResrouseStatus(statusResource);

		// 买断价格、买断资源初始化
		request.setT(goodsItem);
		ResponseBody<Boolean> hasPreControllerResponse = hotelCombOrderService.hasControled(request);
		if (hasPreControllerResponse.getT()) {
			buyOutTimePrice.setBuyoutFlag("Y");
			ResponseBody<HotelCombBuyOutTimePrice> hotelCombSuppGoodTimePrice = hotelCombOrderService
					.calHotelCombTimePrice(request);
			HotelCombBuyOutTimePrice hotelCombSuppGoodsTimePriceVo = hotelCombSuppGoodTimePrice.getT();
			/**
			 * 写成List便于以后扩展其他库存场景的买断
			 */
			List<Map<Long, BuyOutTimePrice>> buyOutTimePriceList = new ArrayList<Map<Long, BuyOutTimePrice>>();
			
			Long pricePlanId = null;
			if (hotelCombSuppGoodsTimePriceVo.getBuyOutTimePrice() != null) {
				for (Map<Long, com.lvmama.dest.api.hotelcomb.vo.BuyOutTimePrice> buyOutTimePriceTemp : hotelCombSuppGoodsTimePriceVo
						.getBuyOutTimePrice()) {
					BuyOutTimePrice temp = new BuyOutTimePrice();
					if(null != buyOutTimePriceTemp.get(goodItem.getGoodsId())){
						EnhanceBeanUtils.copyProperties(buyOutTimePriceTemp.get(goodItem.getGoodsId()), temp);
						pricePlanId = temp.getPricePlanId();
						Map<Long, BuyOutTimePrice> buyOutMap = new HashMap<Long, BuyOutTimePrice>();
						buyOutMap.put(goodItem.getGoodsId(), temp);
						buyOutTimePriceList.add(buyOutMap);
					}
				}
			}
			buyOutTimePrice.setPricePlanId(pricePlanId);
			buyOutTimePrice.setBuyOutTimePriceList(buyOutTimePriceList);
			// EnhanceBeanUtils.copyProperties(hotelCombSuppGoodsTimePriceVo.getBuyOutTimePrice())
			buyOutTimePrice.setSalePrice(hotelCombSuppGoodsTimePriceVo.getPrice());
			buyOutTimePrice.setSettmentPrice(hotelCombSuppGoodsTimePriceVo.getSettlementPrice());
			return buyOutTimePrice;
		}

		return buyOutTimePrice;
	}

}
