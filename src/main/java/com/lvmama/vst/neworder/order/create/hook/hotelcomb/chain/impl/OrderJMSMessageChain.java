package com.lvmama.vst.neworder.order.create.hook.hotelcomb.chain.impl;

import com.google.common.eventbus.Subscribe;
import com.lvmama.dest.hotel.trade.common.RequestBody;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.HotelCombTradeBuyInfoVo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.ComLogUtil;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.neworder.order.cancel.IOrderCancelService;
import com.lvmama.vst.neworder.order.cancel.vo.OrderCancelInfo;
import com.lvmama.vst.neworder.order.create.hook.hotelcomb.chain.IOrderProcessChain;
import com.lvmama.vst.pet.adapter.IOrdPrePayServiceAdapter;
import org.elasticsearch.common.base.Throwables;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by dengcheng on 17/2/18.
 */
@Component("orderJMSMessageChain")
public class OrderJMSMessageChain implements IOrderProcessChain{

    @Resource(name = "orderMessageProducer")
    private TopicMessageProducer orderMessageProducer;

    private static final Logger LOG = LoggerFactory
            .getLogger(OrderJMSMessageChain.class);

    @Resource
    private IOrdPrePayServiceAdapter ordPrePayServiceAdapter;


    @Resource
    IOrderCancelService orderCancelService;

    @Override
    public void beforDoFilter(RequestBody<HotelCombTradeBuyInfoVo> requestBody,String method) {

    }

    @Override
    public void AfterDoFilter(RequestBody<HotelCombTradeBuyInfoVo> requestBody, OrdOrder order,String method) {
    	LOG.info("OrderJMSMessageChain----AfterDoFilter--start");
        if(method.indexOf("submitOrder") != -1) {
            try {
                long startTime = System.currentTimeMillis();
                orderMessageProducer.sendMsg(MessageFactory
                        .newOrderCreateMessage(order.getOrderId()));

                Log.info(ComLogUtil.printTraceInfo("OrderJMSMessageNotifier#process" + order.getOrderId(), "推送消息",
                        "orderMessageProducer.sendMsg",
                        System.currentTimeMillis() - startTime));

                //如果订单是0元订单发消息通知
                if (order.hasNeedPrepaid()
                        && order.getDistributorId() != Constant.DIST_BACK_END) {
                    if (order.getOughtAmount() == 0
                            && !order.isNeedResourceConfirm()) {// 操作0元支付
                        LOG.info("ordPrePayServiceAdapter.vstOrder0YuanPayMsg()"
                                + order.getOrderId());
                        ordPrePayServiceAdapter
                                .vstOrder0YuanPayMsg(order.getOrderId());
                    }
                }
//        HotelCombOrderPersistanceAspect.orderThreadLocalCache.get().put(NewOrderConstant.ORDER_MSG_FLAG,"true");
            } catch (Throwable t) {
            	t.printStackTrace();
            	
            	LOG.error("OrderJMSMessageChain---AfterDoFilter-- end---消息发送异常");
            	//调用取消订单
            	  OrderCancelInfo info = new OrderCancelInfo();
                  info.setOrderId(order.getOrderId());
                  info.setCancelCode(OrderEnum.CANCEL_CODE_TYPE.OTHER_REASON.name());
                  info.setReason("订单创建消息发送异常");
                  info.setOperatorId("System");
                  orderCancelService.doOrderCancel(info);
//        HotelCombOrderPersistanceAspect.orderThreadLocalCache.get().put(NewOrderConstant.ORDER_MSG_FLAG,"false");
                Throwables.propagate(t);

            }
        	LOG.info("OrderJMSMessageChain----AfterDoFilter--end");
        }
    }
}
