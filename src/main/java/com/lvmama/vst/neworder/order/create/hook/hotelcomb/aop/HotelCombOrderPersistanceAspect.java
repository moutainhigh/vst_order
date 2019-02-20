package com.lvmama.vst.neworder.order.create.hook.hotelcomb.aop;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.neworder.order.cancel.IOrderCancelService;
import com.lvmama.vst.neworder.order.cancel.vo.OrderCancelInfo;
import com.lvmama.vst.neworder.order.create.builder.category.newhotelcomb.factory.product.DTOProductServiceImpl;
import com.lvmama.vst.order.service.IOrdOrderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.elasticsearch.common.base.Throwables;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.lvmama.vst.back.order.po.OrdOrder;
import scala.reflect.New;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by dengcheng on 17/3/15.
 * 酒店子系统相关订单持久化方法切面
 */
//@Aspect
//@Order(0)
//@Component
public class HotelCombOrderPersistanceAspect {

//    @Resource
//    BeforeOrderCreatedEventBus orderCreatedEventBus;
//
//
//
//
//    private static final Log LOG = LogFactory.getLog(HotelCombOrderPersistanceAspect.class);
//
//
//
//    @Resource
//    IOrdOrderService orderService;






//    @Around("execution(* com.lvmama.vst.neworder.order.create.persistance.category.newhotel.factory.HotelCombDbStroeFactory.persistanceOrder(..))")
//    public Object  arround(ProceedingJoinPoint pjp) throws Throwable{
//        Object[] objects = pjp.getArgs();
//        Object o = null;
//        try {
//            beforeOrderCreatedEventBus.post(objects[0]);
//
//            o = pjp.proceed();
//            if(o!=null){
//                afterOrderCreatedEventBus.post(o);
//            }
//        } catch (Throwable t) {
////            NewOrderConstant.orderThreadLocalCache.get().put(NewOrderConstant.PERSISTANCE_FLAG,"false");
//            Throwables.propagate(t);
//        } finally {
//
//            OrdOrder order= (OrdOrder)o;
//            NewOrderConstant.orderThreadLocalCache.get().put("currentThreadOrder",order);
//
//           String key = (String) NewOrderConstant.orderThreadLocalCache.get().get(NewOrderConstant.UNIQUE_ORDER_KEY);
//            /**
//             * 清理key
//             */
//           orderCache.invalidate(key);
//
//
//        }
//        return o;
//    }

}
