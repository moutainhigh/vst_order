package com.lvmama.vst.neworder.order.create.hook.hotelcomb.aop;


import com.alibaba.fastjson.JSON;
import com.google.common.cache.CacheLoader;
import com.lvmama.dest.dock.response.ResponseBody;
import com.lvmama.dest.hotel.trade.common.RequestBody;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombTradeBuyInfoVo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.neworder.order.cancel.IOrderCancelService;
import com.lvmama.vst.neworder.order.cancel.vo.OrderCancelInfo;
import com.lvmama.vst.neworder.order.create.hook.hotelcomb.chain.IOrderProcessChain;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.elasticsearch.common.base.Throwables;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import scala.reflect.New;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by dengcheng on 17/3/15.
 * 酒店子系统相关订单API切面
 */
@Aspect
@Order(0)
@Component
public class HotelCombOrderApiAspect {

    private static final Log LOG = LogFactory.getLog(HotelCombOrderApiAspect.class);

    @Resource
    List<IOrderProcessChain> hotelOrderOtherInterfaceChainList;


    @Resource
    IOrderCancelService orderCancelService;


    @Around("execution(* com.lvmama.vst.neworder.order.service.api.hotelcomb.proxy.IHotelCombOrderServiceProxy.checkOrder(..)) or execution(* com.lvmama.vst.neworder.order.service.api.hotelcomb.proxy.IHotelCombOrderServiceProxy.calOrderPriceList(..))")
    public Object  arround(ProceedingJoinPoint pjp) throws Throwable{

        Object[] objects = pjp.getArgs();
        LOG.info("in ... arround");
        String method = pjp.getSignature().getName();

        if (objects != null && objects.length > 0) {
            Iterator<IOrderProcessChain> iterList= hotelOrderOtherInterfaceChainList.iterator();
            while (iterList.hasNext()) {
                IOrderProcessChain chain = iterList.next();
                chain.beforDoFilter((RequestBody<HotelCombTradeBuyInfoVo>)objects[0],method);
            }
        }

        Object o = null;
        o = pjp.proceed();
        return o;
    }

}
