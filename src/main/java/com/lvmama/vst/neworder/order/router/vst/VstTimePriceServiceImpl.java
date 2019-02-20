package com.lvmama.vst.neworder.order.router.vst;

import com.google.common.base.Preconditions;
import com.lvmama.dest.api.common.RequestBody;
import com.lvmama.dest.api.hotelcomb.vo.HotelCombBuyInfoVo;
import com.lvmama.dest.api.order.enums.OrderEnum;
import com.lvmama.vst.back.goods.po.SuppGoods;
import com.lvmama.vst.back.goods.po.SuppGoodsAddTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsBaseTimePrice;
import com.lvmama.vst.back.goods.po.SuppGoodsNotimeTimePrice;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.neworder.order.router.ILookUpService;
import com.lvmama.vst.neworder.order.router.ITimePriceRouterService;
import com.lvmama.vst.neworder.order.vo.BaseTimePrice;
import com.lvmama.vst.neworder.order.vo.OrderHotelCombBuyInfo;
import com.lvmama.vst.order.timeprice.service.OrderTimePriceService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dengcheng on 17/4/26.
 */
@Component("vstSystemTimePriceService")
public class VstTimePriceServiceImpl implements ITimePriceRouterService {

//    @Resource(name="orderTicketAddTimePriceService")
 //   OrderTimePriceService orderTimePriceService;
	@Resource
	ILookUpService lookUpService;
    @Override
    public BaseTimePrice findTimePrice(SuppGoods goods,OrderHotelCombBuyInfo.GoodsItem goodItem,OrderHotelCombBuyInfo.Item item) {
        Preconditions.checkNotNull(item.getGoodsId());
        Preconditions.checkNotNull(item.getVisitTime());

        BaseTimePrice btp = new BaseTimePrice();
        OrderTimePriceService  orderTimePriceService = lookUpService.lookupTicketTimePrice(goods.getCategoryId());
        ResultHandleT<SuppGoodsBaseTimePrice> timePriceResultHandleT =  orderTimePriceService.getTimePrice(item.getGoodsId(),DateUtil.toDate(item.getVisitTime(),DateUtil.SIMPLE_DATE_FORMAT),true);
        SuppGoodsBaseTimePrice timePrice = timePriceResultHandleT.getReturnContent();

        Preconditions.checkArgument(timePriceResultHandleT!=null,"商品%s无可售信息",item.getGoodsId()+"");
        Preconditions.checkArgument(timePriceResultHandleT.getReturnContent()!=null,"商品%s无可售信息",item.getGoodsId()+"");
       if(timePrice instanceof SuppGoodsNotimeTimePrice){
        SuppGoodsNotimeTimePrice notimeTimePrice=(SuppGoodsNotimeTimePrice)timePrice;

        btp.setGoodsId(notimeTimePrice.getSuppGoodsId());
        btp.setSettmentPrice(notimeTimePrice.getSettlementPrice());
        btp.setSalePrice(notimeTimePrice.getPrice());
        btp.setAheadBookTime(notimeTimePrice.getAheadBookTime());
        btp.setSpecDate(notimeTimePrice.getSpecDate());
        btp.setStockFlag(notimeTimePrice.getStockFlag());//是否需要扣减库存的标记，为"Y"时需要扣减
        btp.setStock(notimeTimePrice.getStock());
       }else if(timePrice instanceof SuppGoodsAddTimePrice){
    	   SuppGoodsAddTimePrice notimeTimePrice=(SuppGoodsAddTimePrice)timePrice; 
    	   btp.setGoodsId(notimeTimePrice.getSuppGoodsId());
           btp.setSettmentPrice(notimeTimePrice.getSettlementPrice());
           btp.setSalePrice(notimeTimePrice.getPrice());
           btp.setAheadBookTime(notimeTimePrice.getAheadBookTime());
           btp.setSpecDate(notimeTimePrice.getSpecDate());
           btp.setStockFlag(notimeTimePrice.getStockFlag());//是否需要扣减库存的标记，为"Y"时需要扣减
           btp.setStock(notimeTimePrice.getStock());
           btp.setShareDayLimitId(notimeTimePrice.getShareDayLimitId());
           btp.setShareTotalStockId(notimeTimePrice.getShareTotalStockId());
          
       }
	  return btp;
    }
}
