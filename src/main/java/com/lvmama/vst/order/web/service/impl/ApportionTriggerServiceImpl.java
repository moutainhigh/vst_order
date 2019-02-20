package com.lvmama.vst.order.web.service.impl;

import com.lvmama.comm.utils.NumberUtils;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderApportionDepot;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.Constants;
import com.lvmama.vst.comm.utils.MemcachedUtil;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.comm.vo.MemcachedEnum;
import com.lvmama.vst.order.processer.OrderApportionProcesser;
import com.lvmama.vst.order.service.IOrdOrderService;
import com.lvmama.vst.order.service.OrderApportionDepotService;
import com.lvmama.vst.order.service.apportion.OrderAmountApportionService;
import com.lvmama.vst.order.service.apportion.assist.ApportionDataAssist;
import com.lvmama.vst.order.utils.ApportionUtil;
import com.lvmama.vst.order.vo.ApportionQueryVO;
import com.lvmama.vst.order.web.service.ApportionTriggerService;
import com.lvmama.vst.order.web.vo.OrderApportionMsgVO;
import com.lvmama.vst.order.web.vo.OrderApportionVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by zhouyanqun on 2017/6/1.
 */
@Component
public class ApportionTriggerServiceImpl implements ApportionTriggerService {
    private static final Log log = LogFactory.getLog(ApportionTriggerServiceImpl.class);

    @Resource
    private ApportionDataAssist apportionDataAssist;
    @Resource
    private OrderAmountApportionService orderAmountApportionService;
    @Resource
    private OrderApportionDepotService orderApportionDepotService;
    @Resource
    private IOrdOrderService ordOrderService;
    @Resource(name="orderMessageProducer")
    private TopicMessageProducer orderMessageProducer;
    @Resource(name = "orderApportionProcesser")
    private OrderApportionProcesser orderApportionProcesser;

    /**
     * 检查订单id是否合法，如果不合法，抛出异常
     * */
    private void checkOrderId(Long orderId) {
        if (NumberUtils.isNotAboveZero(orderId)) {
            throw new RuntimeException("订单号" + orderId + "非法");
        }
    }

    /**
     * 分摊并且保存订单上所有的金额，包括优惠、促销、渠道优惠、手工改价、实付，一共5种类型的金额
     * 1. 作废以前的所有分摊项
     * 2. 执行下分摊
     * 3. 修改表order_apportion_depot表的分摊状态
     *
     * @param orderId
     */
    @Override
    public void apportionAndSaveFullAmount(OrderApportionVO orderApportionVO) {
        //判断分摊开关是否开启
        if(!ApportionUtil.isApportionEnabled()) {
            log.error("Apportion is not enabled, please check");
            return;
        }
        Long orderId = orderApportionVO.getOrderId();
        //是否强制
        boolean force = StringUtils.equals(orderApportionVO.getForce(), Constants.Y_FLAG);
        checkOrderId(orderId);
        String memoryCacheLockKey = MemcachedEnum.OrderApportionExecution.getKey() + orderId;
        String memoryCacheLockValue = MemcachedUtil.getInstance().get(memoryCacheLockKey);
        if (StringUtils.equals(memoryCacheLockValue, Constants.Y_FLAG) && !force) {
            throw new RuntimeException("订单 " + orderId + ", 正在被另一个任务分摊，缓存锁未释放");
        }
        //添加缓存锁
        MemcachedUtil.getInstance().set(memoryCacheLockKey, MemcachedEnum.OrderApportionExecution.getSec(), Constants.Y_FLAG);
        log.info("We are trying to apportion all amount for order " + orderId + ", it's not easy ,we are so tired, but we will never give up");
        //当前时间
        Date currentTime = Calendar.getInstance().getTime();
        //锁定分摊仓库表，不让其它功能执行分摊
        OrderApportionDepot orderApportionDepot = orderApportionDepotService.queryApportionByOrderId(orderId);
        //分摊仓库表记录是否存在
        boolean depotRecordExists = orderApportionDepot != null;
        if (!force && depotRecordExists && StringUtils.equals(OrderEnum.APPORTION_EXECUTE_STATUS.apportion_execute_status_doing.name(), orderApportionDepot.getApportionMessage())) {
            throw new RuntimeException("订单 " + orderId + ", 正在被另一个任务分摊，数据库标识锁未释放");
        }
        if (depotRecordExists) {
            orderApportionDepot.setApportionMessage(OrderEnum.APPORTION_EXECUTE_STATUS.apportion_execute_status_doing.name());
            orderApportionDepot.setUpdateTime(currentTime);
            //更新数据库标识，辅助避免多个任务分摊同一个订单
            orderApportionDepotService.updateOrderApportionDepot(orderApportionDepot);
            log.info("Order " + orderId + " database record is locked");
        } else {
            log.info("Order " + orderId + " do not have record in depot table");
        }
        try {
            //作废之前的分摊项
            ApportionQueryVO apportionQueryVO = new ApportionQueryVO();
            apportionQueryVO.setOrderId(orderId);
            apportionDataAssist.invalidOrderApportionData(apportionQueryVO);
            log.info("Previous all apportion of order " + orderId + " amount is invalided");
            //执行下单分摊
            orderAmountApportionService.calcAndSaveBookingApportionAmount(orderId);
            log.info("Order " + orderId + " booking amount apportion completed, now apportion manual change amount");
            //执行手工改价分摊
            orderAmountApportionService.apportionAndSaveFullManualChangeAmount(orderId);
            if (depotRecordExists) {
                orderApportionDepot.setApportionOrigin(null);
                orderApportionDepot.setApportionStatus(OrderEnum.APPORTION_STATUS.apportion_status_booking_completed.name());
                orderApportionDepot.setUpdateTime(currentTime);
            }
            log.info("Booking and manual change amount apportion finished, now judge whether order " + orderId + " needs to apportion payment amount");
            //判断是否需要实付分摊
            OrdOrder order = ordOrderService.findByOrderId(orderId);
            if (StringUtils.equals(order.getPaymentStatus(), OrderEnum.PAYMENT_STATUS.PAYED.name())) {
                orderAmountApportionService.apportionAndSaveActualPaidAmount(orderId);
                if (depotRecordExists) {
                    orderApportionDepot.setApportionStatus(OrderEnum.APPORTION_STATUS.apportion_status_payment_completed.name());
                }
                log.info("Order " + orderId + ", payment amount apportion completed");
                this.createApportionSuccessMsg(orderId);
            }
        } catch (Exception e) {
            log.info("Error apportion full amount for order " + orderId, e);
            //修改分摊状态为下单项分摊失败
            if (depotRecordExists) {
                orderApportionDepot.setApportionStatus(OrderEnum.APPORTION_STATUS.apportion_status_failed.name());
                orderApportionDepot.setUpdateTime(currentTime);
                orderApportionDepot.setValidFlag(Constants.N_FLAG);
                orderApportionDepotService.updateOrderApportionDepot(orderApportionDepot);
            }
            //删除缓存锁
            MemcachedUtil.getInstance().remove(memoryCacheLockKey);
            throw new RuntimeException(e);
        }
        this.unlockRecord(orderApportionDepot, depotRecordExists);
        //删除缓存锁
        MemcachedUtil.getInstance().remove(memoryCacheLockKey);
        log.info("We completed apportion amount for order " + orderId);
    }

    /**
     * 处理消息，用于模拟消息
     *
     * @param orderApportionMsgVO
     */
    @Override
    public void sendOrderMsg(OrderApportionMsgVO orderApportionMsgVO) {
        if (orderApportionMsgVO == null || NumberUtils.isNotAboveZero(orderApportionMsgVO.getOrderId()) || StringUtils.isBlank(orderApportionMsgVO.getEventType())) {
            throw new RuntimeException("非法的消息类型:" + GsonUtils.toJson(orderApportionMsgVO));
        }
        Long orderId = orderApportionMsgVO.getOrderId();
        String eventType = orderApportionMsgVO.getEventType();
        Message message = null;
        if (StringUtils.equals(Constant.EVENT_TYPE.ORDER_CREATE_MSG.name(), eventType)) {
            message = MessageFactory.newOrderCreateMessage(orderId);
        } else if (StringUtils.equals(Constant.EVENT_TYPE.ORDER_PAYMENT_MSG.name(), eventType)) {
            message = MessageFactory.newOrderPaymentMessage(orderId, "");
        } else {
            throw new RuntimeException("非法的消息类型, 订单id" + orderId);
        }
        orderApportionProcesser.process(message);
    }

    /**
     * 解锁分摊状态，同时更新或者删除记录
     * */
    private void unlockRecord(OrderApportionDepot orderApportionDepot, boolean depotRecordExists) {
        if (orderApportionDepot == null || NumberUtils.isNotAboveZero(orderApportionDepot.getOrderId())) {
            return;
        }
        if (StringUtils.equals(orderApportionDepot.getApportionStatus(), OrderEnum.APPORTION_STATUS.apportion_status_payment_completed.name())){
            //已支付订单，删除记录
            if (depotRecordExists) {
                orderApportionDepotService.deleteByPrimaryKey(orderApportionDepot.getOrderApportionId());
            }
        } else {
            //未支付的订单，更新状态
            orderApportionDepot.setApportionMessage(null);
            if (depotRecordExists) {
                orderApportionDepotService.updateOrderApportionDepot(orderApportionDepot);
            }
        }
    }

    /**
     * 创建分摊完成消息
     * @param orderId
     */
    public void createApportionSuccessMsg(Long orderId){
        log.info("createApportionSuccessMsg createMsg  ORDER_APPORTION_SUCCESS_MSG"+orderId);
        orderMessageProducer.sendMsg(MessageFactory.newOrderApportionSuccessMessage(orderId));
    }
}
