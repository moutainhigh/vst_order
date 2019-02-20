package com.lvmama.vst.neworder.order.create.hook.hotelcomb.chain.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.lvmama.dest.hotel.trade.common.RequestBody;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombTradeBuyInfoVo;
import com.lvmama.dest.hotel.trade.vo.base.Person;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.neworder.order.create.hook.hotelcomb.chain.IOrderProcessChain;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by dengcheng on 17/3/31.
 * 基本参数校验处理器
 */
@Component("paramterProcessChain")
public class ParamterProcessChain implements IOrderProcessChain{

    @Subscribe
    public void beforDoFilter(RequestBody<HotelCombTradeBuyInfoVo> requestBody,String method) {
        Preconditions.checkNotNull(requestBody);
        Preconditions.checkNotNull(requestBody.getT());
//        Preconditions.checkArgument(requestBody.getUserId()!=null,"userId not bean null");
        Preconditions.checkArgument(requestBody.getDistributionId()!=null,"distributionId not bean null");
//        Preconditions.checkArgument(requestBody.getDistributionChannel()!=null,"distributionChannel not bean null");
//        Preconditions.checkArgument(requestBody.getDistributorCode()!=null,"distributorCode not bean null");
        Preconditions.checkArgument(requestBody.getToken()!=null,"token not bean null");

        Preconditions.checkNotNull(requestBody.getT().getGoodsList(),"goodsList is null");
        Preconditions.checkNotNull(requestBody.getT().getProductList(),"product List is null");
//        Preconditions.checkNotNull(requestBody.getT().getTravellers(),"traveller List is null");


        Preconditions.checkArgument(!requestBody.getT().getGoodsList().isEmpty(),"goodsList not bean empty");
        Preconditions.checkArgument(!requestBody.getT().getProductList().isEmpty(),"productList not bean empty");
//        Preconditions.checkArgument(!requestBody.getT().getTravellers().isEmpty(),"travellers not bean empty");

        for (HotelCombTradeBuyInfoVo.GoodsItem goodsItem: requestBody.getT().getGoodsList()) {
                Preconditions.checkNotNull(goodsItem.getCheckInDate(),"checkInDate not bean null");
                Preconditions.checkNotNull(goodsItem.getQuantity(),"quantity not bean null");
                Preconditions.checkNotNull(goodsItem.getCategoryId(),"categoryId not bean null");
                Preconditions.checkNotNull(goodsItem.getSubCategoryId(),"subCategoryId not bean null");

                if(goodsItem.getSubCategoryId().equals(32L)){
                    Preconditions.checkNotNull(goodsItem.getPricePlanId(),"pricePlanId not bean null");
                }

        }

        for (HotelCombTradeBuyInfoVo.ProductItem productItem: requestBody.getT().getProductList()) {
            Preconditions.checkNotNull(productItem.getProductId());
        }


    }

    @Override
    public void AfterDoFilter(RequestBody<HotelCombTradeBuyInfoVo> requestBody, OrdOrder order,String method) {
        //do nothing
    }

}
