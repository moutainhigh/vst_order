package com.lvmama.vst.neworder.order.create.hook.hotelcomb.aop;
import com.alibaba.fastjson.JSON;
import com.lvmama.comm.utils.DateUtil;
import com.lvmama.dest.hotel.trade.common.RequestBody;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombTradeBuyInfoVo;
import com.lvmama.dest.hotel.trade.vo.base.Person;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.neworder.order.NewOrderConstant;
import com.lvmama.vst.neworder.order.cancel.IOrderCancelService;
import com.lvmama.vst.neworder.order.cancel.vo.OrderCancelInfo;
import com.lvmama.vst.neworder.order.create.hook.hotelcomb.chain.IOrderProcessChain;

import org.apache.commons.codec.digest.DigestUtils;
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

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by dengcheng on 17/5/4.
 */
@Aspect
@Order(0)
@Component
public class HotelCombSubmitOrderAspect {
    private static final Log LOG = LogFactory.getLog(HotelCombSubmitOrderAspect.class);
    @Resource
    List<IOrderProcessChain> hotelCommitOrderChainList;


    @Resource
    IOrderCancelService orderCancelService;

    @AfterThrowing(value = "execution(* com.lvmama.vst.neworder.order.service.api.hotelcomb.proxy.IHotelCombOrderServiceProxy.submitOrder(..))", throwing = "e")
    public void afterThrowingAdvice(JoinPoint pjp, Exception e) throws Throwable {
    	  Object[] objects = pjp.getArgs();
    	try {
            String method = pjp.getSignature().getName();
            Map<String, Object> threadMap = NewOrderConstant.orderThreadLocalCache.get();
            OrdOrder order = (OrdOrder) threadMap.get("currentThreadOrder");
          
            Object obj = null;
            if (order != null) {
                LOG.info("aspect order:" + order + "orderId:" + order.getOrderId());

                LOG.info("aspect orderjson:" + JSON.toJSONString(order));
                OrderCancelInfo info = new OrderCancelInfo();
                info.setOrderId(order.getOrderId());
                info.setCancelCode(OrderEnum.CANCEL_CODE_TYPE.OTHER_REASON.name());
                info.setReason("订单报错异常取消");
                info.setOperatorId("System");
              //  orderCancelService.doOrderCancel(info);
            }
        } catch (Throwable t) {
            Throwables.propagate(t);
        } finally {
            LOG.info("afterThrowingAdvice remove orderThreadLocalCache");
            NewOrderConstant.orderThreadLocalCache.remove();
      
			RequestBody<HotelCombTradeBuyInfoVo> obj =(RequestBody<HotelCombTradeBuyInfoVo>) objects[0] ;
        	if(obj!=null){
        		String  key = this.getBuyInfoHashCode(obj);
         	    MemcachedUtil.getInstance().remove(key);
         }
        }  	
        throw  e;
        
    }

    @AfterReturning(returning="rvt",value = "execution(* com.lvmama.vst.neworder.order.service.api.hotelcomb.proxy.IHotelCombOrderServiceProxy.submitOrder(..))")
    public void afterReturning(Object rvt)
    {
        LOG.info("return :" + rvt);
        LOG.info("afterReturning remove orderThreadLocalCache");
        NewOrderConstant.orderThreadLocalCache.remove();
    }

    @Around("execution(* com.lvmama.vst.neworder.order.service.api.hotelcomb.proxy.IHotelCombOrderServiceProxy.submitOrder(..))")
    public Object  arround(ProceedingJoinPoint pjp) throws Throwable{
        Object[] objects = pjp.getArgs();
        LOG.info("in ... arround");
        String method = pjp.getSignature().getName();

        if (objects != null && objects.length > 0) {
            Iterator<IOrderProcessChain> iterList= hotelCommitOrderChainList.iterator();
            while (iterList.hasNext()) {
                IOrderProcessChain chain = iterList.next();
                chain.beforDoFilter((RequestBody<HotelCombTradeBuyInfoVo>)objects[0],method);
            }
        }

        Object o = null;
        o = pjp.proceed();

        if (objects != null && objects.length > 0) {
            Iterator<IOrderProcessChain> iterList= hotelCommitOrderChainList.iterator();
            while (iterList.hasNext()) {
                IOrderProcessChain chain = iterList.next();
                // NewOrderConstant.orderThreadLocalCache.get().put("currentThreadOrder",order);
                OrdOrder order =  (OrdOrder) NewOrderConstant.orderThreadLocalCache.get().get("currentThreadOrder");
                chain.AfterDoFilter((RequestBody<HotelCombTradeBuyInfoVo>)objects[0],order,method);
            }
        }

        return o;
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
