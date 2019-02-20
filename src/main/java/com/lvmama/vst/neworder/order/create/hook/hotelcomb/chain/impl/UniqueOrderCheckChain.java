package com.lvmama.vst.neworder.order.create.hook.hotelcomb.chain.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.CacheLoader;
import com.google.common.eventbus.Subscribe;
import com.lvmama.comm.utils.DateUtil;
import com.lvmama.dest.hotel.trade.common.RequestBody;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombTradeBuyInfoVo;
import com.lvmama.dest.hotel.trade.vo.base.Person;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.order.po.OrderStatusEnum;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.neworder.order.create.hook.hotelcomb.aop.HotelCombOrderPersistanceAspect;
import com.lvmama.vst.neworder.order.create.hook.hotelcomb.chain.IOrderProcessChain;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by dengcheng on 17/3/29.
 * 采用堆外缓存来实现 之前是支持并发的Map
 * 重单校验/分布式环境下不严谨 copy过来 小部分修改
 */
@Component("uniqueOrderCheckChain")
public class UniqueOrderCheckChain implements IOrderProcessChain{
    private static final Logger LOG = LoggerFactory
            .getLogger(UniqueOrderCheckChain.class);


    @Override
    public void beforDoFilter(RequestBody<HotelCombTradeBuyInfoVo> requestBody,String method) {
        if(method.indexOf("submitOrder") != -1){
            String  key = getBuyInfoHashCode(requestBody);
          //  NewOrderConstant.orderThreadLocalCache.get().put(NewOrderConstant.UNIQUE_ORDER_KEY,key);
            boolean  flag = MemcachedUtil.getInstance().set(key, 2, JSON.toJSONString(requestBody));
            if(!flag){
            	 LOG.info("订单重复");
            	 throw new com.lvmama.dest.hotel.trade.utils.BusinessException(String.format("%s,%s",OrderStatusEnum.ORDER_ERROR_CODE.REPEAT_CREATE_ORDER.getErrorCode(),"重复创建订单"));
            }
//            Object obj = null;
//            try {
//                obj = NewOrderConstant.orderCache.get(key);
//            } catch (CacheLoader.InvalidCacheLoadException ex) {
//                LOG.error("Not hit in localCache");
//            } catch (Throwable t) {
//                t.printStackTrace();;
//            }
//
//            if(obj!=null){
//                LOG.info("订单重复");
//                throw new com.lvmama.dest.hotel.trade.utils.BusinessException(String.format("%s,%s",OrderStatusEnum.ORDER_ERROR_CODE.REPEAT_CREATE_ORDER.getErrorCode(),"重复创建订单"));
//            } else {
//                NewOrderConstant.orderCache.put(key,1);
//            }
        }

    }




    public void AfterDoFilter(RequestBody<HotelCombTradeBuyInfoVo> requestBody,OrdOrder order,String method) {
            //do nothing
    }

    private synchronized String getBuyInfoHashCode(RequestBody<HotelCombTradeBuyInfoVo> requestBody) {
        Long time = System.currentTimeMillis();
        String signStr = this.getUniqueOrderSignStr(requestBody);
        LOG.info(signStr);
        Long endTime = System.currentTimeMillis();
        LOG.info("excute time:"+(endTime-time));
        String key = DigestUtils.shaHex(signStr);
        endTime = System.currentTimeMillis();
        LOG.info("excute time2:"+(endTime-time));
        return key;
    }

    private String getUniqueOrderSignStr(RequestBody<HotelCombTradeBuyInfoVo> requestBody){
        HotelCombTradeBuyInfoVo buyInfoVo = requestBody.getT();
        String sign ="" ;

        for (HotelCombTradeBuyInfoVo.ProductItem productItem: buyInfoVo.getProductList()) {
            sign += productItem.getProductId()+"&";

        }

         sign += requestBody.getUserId()+"&";

        for (HotelCombTradeBuyInfoVo.GoodsItem goodsItem:
                buyInfoVo.getGoodsList()) {
            sign+=goodsItem.getGoodsId()+"&";
            sign+= DateUtil.formatDate(goodsItem.getCheckInDate(),"yyyy-MM-dd")+"&";
//            sign+= DateUtil.formatDate(goodsItem.get,"yyyy-MM-dd")+"&";
        }

        for (Person person:
                buyInfoVo.getTravellers()) {
            sign+=person.getFullName()+"&";
            sign+=person.getGender()+"&";
            sign+=person.getMobile()+"&";
            sign+=person.getPersonType()+"&";

        }
        LOG.info("sign:"+sign);
        return  sign;

    }


}
