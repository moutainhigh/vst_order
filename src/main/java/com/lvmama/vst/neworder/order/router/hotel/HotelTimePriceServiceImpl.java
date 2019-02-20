package com.lvmama.vst.neworder.order.router.hotel;

import com.google.common.base.Preconditions;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.common.ResponseBody;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombOrderService;
import com.lvmama.dest.api.hotelcomb.interfaces.IHotelCombProductService;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombBuyInfoVo;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombSuppGoodsTimePriceVo;
import com.lvmama.dest.api.hotelcomb.vo.TimePriceQueryVo;
import com.lvmama.dest.api.order.enums.OrderEnum;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.neworder.order.EnhanceBeanUtils;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.adapter.HotelSysProductAdpaterServiceImpl;
import com.lvmama.vst.neworder.order.router.ITimePriceRouterService;
import com.lvmama.vst.neworder.order.vo.BaseTimePrice;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;

import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dengcheng on 17/4/26.
 */
@Component("HotelSystemTimePriceService")
public class HotelTimePriceServiceImpl implements ITimePriceRouterService {

	private static final Logger LOG = LoggerFactory
            .getLogger(HotelTimePriceServiceImpl.class);
    @Resource
    IHotelCombOrderService hotelCombOrderService;

    @Resource
    IHotelCombProductService hotelCombProductService;

    @Override
    public BaseTimePrice findTimePrice(SuppGoods goods,OrderHotelCombBuyInfo.GoodsItem goodItem,OrderHotelCombBuyInfo.Item item) {
        Preconditions.checkNotNull(goodItem.getGoodsId());
        Preconditions.checkNotNull(goodItem.getCheckInDate());
        BaseTimePrice btp = new BaseTimePrice();

        TimePriceQueryVo tpQ = new TimePriceQueryVo();
        tpQ.setGoodsId(goodItem.getGoodsId());

        tpQ.setPricePlanId(goodItem.getPricePlanId());
        tpQ.setCheckInDate(goodItem.getCheckInDate());

        ResponseBody<List<HotelCombSuppGoodsTimePriceVo>> hotelCombSuppGoodsTimePriceVoResponseBody = hotelCombProductService.getHotelCombTimePrice(new RequestBody<TimePriceQueryVo>().setTFlowStyle(tpQ,NewOrderConstant.VST_ORDER_TOKEN));

        List<HotelCombSuppGoodsTimePriceVo> hotelCombSuppGoodsTimePriceVoList= hotelCombSuppGoodsTimePriceVoResponseBody.getT();

        Preconditions.checkArgument(!hotelCombSuppGoodsTimePriceVoList.isEmpty(),"商品%s无可售信息",goodItem.getGoodsId()+"");
        HotelCombSuppGoodsTimePriceVo hotelCombPrice = hotelCombSuppGoodsTimePriceVoList.get(0);
        RequestBody  request  = new RequestBody();
        HotelCombBuyInfoVo hotelCombBuyInfoVo  = new HotelCombBuyInfoVo();
        List<HotelCombBuyInfoVo.GoodsItem> goodsList  = new ArrayList<HotelCombBuyInfoVo.GoodsItem>();
        HotelCombBuyInfoVo.GoodsItem hotelCombGoods = new HotelCombBuyInfoVo().new GoodsItem();
        EnhanceBeanUtils.copyProperties(goodItem,hotelCombGoods);
        goodsList.add(hotelCombGoods);

        hotelCombBuyInfoVo.setGoodsList(goodsList);
        request.setT(hotelCombBuyInfoVo);
        String statusResource = OrderEnum.RESOURCE_STATUS.AMPLE.name();
        RequestBody<List<String>> response =hotelCombOrderService.calOrderResource(request) ;
        LOG.info("hotelCombOrderService.calOrderResource"+response.getT());
        if(response.getT()!=null){
        LOG.info("hotelCombOrderService.calOrderResource"+response.getT().get(0)+"size:"+response.getT().size());
        }
        if(response.getT()!=null && response.getT().size()==0){
        	
        	 statusResource =OrderEnum.RESOURCE_STATUS.UNVERIFIED.name();
        	 
        } 
        if(response.getT()!=null && response.getT().size()>0){
        for(String str:response.getT()){
            if(OrderEnum.RESOURCE_STATUS.UNVERIFIED.name().equals(str)){
                statusResource =OrderEnum.RESOURCE_STATUS.UNVERIFIED.name();
                LOG.info("calOrderResource--for--statusResource:"+statusResource);
                break ;
            }
        }
        }
        LOG.info("calOrderResource--end--statusResource:"+statusResource);
        btp.setResrouseStatus(statusResource);
        btp.setGoodsId(hotelCombPrice.getSuppGoodsId());
        btp.setSpecDate(hotelCombPrice.getSpecDate());
        btp.setPricePlanId(hotelCombPrice.getPricePlanId());
        btp.setSalePrice(hotelCombPrice.getSalePrice());
        btp.setAheadBookTime(hotelCombPrice.getAheadBookTime());
        btp.setSettmentPrice(hotelCombPrice.getSettlementPrice());
        return btp;
    }
}
