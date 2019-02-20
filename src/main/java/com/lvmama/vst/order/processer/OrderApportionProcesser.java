package com.lvmama.vst.order.processer;

import com.lvmama.vst.back.order.po.OrderApportionDepot;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.*;
import com.lvmama.vst.comm.utils.gson.GsonUtils;
import com.lvmama.vst.comm.vo.MemcachedEnum;
import com.lvmama.vst.order.service.OrderApportionDepotService;
import com.lvmama.vst.order.service.apportion.OrderAmountApportionService;
import com.lvmama.vst.order.service.apportion.assist.ApportionDataAssist;
import com.lvmama.vst.order.utils.ApportionUtil;
import com.lvmama.vst.order.vo.ApportionQueryVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;


/**
 * 异步分摊
 *
 * @author chenpingfan
 */
public class OrderApportionProcesser implements MessageProcesser {

    protected transient final Log logger = LogFactory.getLog(getClass());

    @Resource
    private OrderApportionDepotService orderApportionDepotService;
    @Resource
    private OrderAmountApportionService orderAmountApportionService;
    @Resource(name="orderMessageProducer")
    private TopicMessageProducer orderMessageProducer;
    @Resource
    private ApportionDataAssist apportionDataAssist;

    @Override
    public void process(Message message) {

        if (!ApportionUtil.isApportionEnabled()) {
            logger.info("orderApportionProcesser Apportion is not enabled, please check, orderId is " + message.getObjectId());
            return;
        }
        //订单分摊走新系统(lvmm_order_apportion),老系统（vst_order）直接返回
        if (ApportionUtil.isLvmmOrderApportion()) {
            logger.info("orderApportionProcesser LvmmOrderApportion is  enabled, please check, orderId is " + message.getObjectId());
            return;
        }
        
        if (MessageUtils.isOrderCreateMsg(message)) {
            executeCreateMsg(message);
        }

        if (MessageUtils.isOrderPaymentMsg(message)) {
            executePayedMsg(message);
        }

    }


    /**
     * 订单创建消息
     *
     * @param message
     */
    private void executeCreateMsg(Message message) {
        Long orderId = message.getObjectId();
        logger.info("executeCreateMsg 接受消息 orderId=" + orderId);
        String memoryCacheKey = MemcachedEnum.OrderApportionExecution.getKey() + orderId;
        if (MemcachedUtil.getInstance().keyExists(memoryCacheKey)) {
            logger.info("executeCreateMsg has memCachedValue and orderId = " + orderId);
            return;
        }
        /**缓存设置锁**/
        MemcachedUtil.getInstance().set(memoryCacheKey, MemcachedEnum.OrderApportionExecution.getSec(), Constants.Y_FLAG);

        OrderApportionDepot orderApportionDepot = orderApportionDepotService.queryApportionByOrderId(orderId);
        logger.info("executeCreateMsg orderApportionDepotList" + GsonUtils.toJson(orderApportionDepot));

        //如果订单正在分摊，或者已经分摊过，则把缓存锁去掉，返回
        if (orderApportionDepot == null
                || StringUtils.equals(orderApportionDepot.getApportionMessage(), OrderEnum.APPORTION_EXECUTE_STATUS.apportion_execute_status_doing.name())
                || StringUtils.equals(orderApportionDepot.getApportionStatus(), OrderEnum.APPORTION_STATUS.apportion_status_booking_completed.name())) {
            MemcachedUtil.getInstance().remove(memoryCacheKey);
            logger.info("Order " + orderId + " do not exists in table depot or is now apportion or already completed booking apportion");
            return;
        }
        orderApportionDepot.setApportionMessage(OrderEnum.APPORTION_EXECUTE_STATUS.apportion_execute_status_doing.name());
        orderApportionDepot.setUpdateTime(new Date());
        //保存分摊标识到数据库
        orderApportionDepotService.updateOrderApportionDepot(orderApportionDepot);
        try {
            //如果分摊来源是价格修改，要先作废之前的分摊记录，然后重新分摊下单项和价格修改项
            this.checkManualChangePriceRedo(orderApportionDepot);
            //执行保存订单分摊计算
            logger.info("executeCreateMsg calcAndSaveBookingApportionAmount start & orderId=" + orderId);
            orderAmountApportionService.calcAndSaveBookingApportionAmount(orderId);
            logger.info("executeCreateMsg calcAndSaveBookingApportionAmount end & orderId=" + orderId);
            //回写DB,删除分摊（将msg设置为空）
            orderApportionDepot.setApportionStatus(OrderEnum.APPORTION_STATUS.apportion_status_booking_completed.name());
            orderApportionDepot.setApportionMessage(null);
            orderApportionDepot.setUpdateTime(Calendar.getInstance().getTime());
            orderApportionDepotService.updateOrderApportionDepot(orderApportionDepot);
            logger.info("executeCreateMsg updateOrderApportionDepot success! orderId=" + orderId);
        } catch (Exception e) {
            logger.error("executeCreateMsg error:orderId=" + orderId + " and error info :" + ExceptionFormatUtil.getTrace(e));
            //回写DB
            orderApportionDepot.setApportionStatus(OrderEnum.APPORTION_STATUS.apportion_status_booking_failed.name());
            orderApportionDepot.setApportionMessage(e.getMessage());
            orderApportionDepot.setValidFlag(Constants.N_FLAG);
            orderApportionDepot.setUpdateTime(Calendar.getInstance().getTime());
            orderApportionDepotService.updateOrderApportionDepot(orderApportionDepot);
            logger.error("executeCreateMsg orderApportionDepot save ValidFlag=N to db");
            throw new RuntimeException("executeCreateMsg 执行分摊计算异常---" + e.getMessage());
        }
        MemcachedUtil.getInstance().remove(memoryCacheKey);
        logger.info("executeCreateMsg memoryCacheKey has remove and orderId=" + orderId);
    }

    //检查订单是否有待重做的分摊，如果有，执行手工分摊的重做
    private void checkManualChangePriceRedo(OrderApportionDepot orderApportionDepot) {
        if (!StringUtils.equals(orderApportionDepot.getApportionOrigin(), OrderEnum.APPORTION_ORIGIN.apportion_origin_price_change.getApportionOriginName())) {
            return;
        }
        Long orderId = orderApportionDepot.getOrderId();
        logger.info("Invalid previous apportion record, order is " + orderId);
        ApportionQueryVO apportionQueryVO = new ApportionQueryVO();
        apportionQueryVO.setOrderId(orderId);
        apportionDataAssist.invalidOrderApportionData(apportionQueryVO);
        logger.info("Invalid previous apportion record completed, order is " + orderId);
        //重新分摊所有的手工改价
        orderAmountApportionService.apportionAndSaveFullManualChangeAmount(orderId);
        orderApportionDepot.setApportionOrigin(null);
        logger.info("Manual change amount redo done, order is " + orderId);
    }


    /**
     * 处理支付成功消息
     *
     * @param message
     */
    private void executePayedMsg(Message message) {
        Long orderId = message.getObjectId();
        logger.info("executePayedMsg 接受消息 orderId=" + orderId);
        String memoryCacheKey = MemcachedEnum.OrderApportionExecution.getKey() + orderId;
        if (MemcachedUtil.getInstance().keyExists(memoryCacheKey)) {
            logger.info("executePayedMsg has memCachedValue and orderId = " + orderId);
            return;
        }
        /**缓存设置锁**/
        MemcachedUtil.getInstance().set(memoryCacheKey, MemcachedEnum.OrderApportionExecution.getSec(), Constants.Y_FLAG);

        OrderApportionDepot orderApportionDepot = orderApportionDepotService.queryApportionByOrderId(orderId);
        if (orderApportionDepot == null) {
            MemcachedUtil.getInstance().remove(memoryCacheKey);
            logger.info("executePayedMsg apportionDepotList is null and  orderId=" + orderId);
            return;
        }

        logger.info("executePayedMsg orderApportionDepotList" + GsonUtils.toJson(orderApportionDepot));
        //不为空则说明批次正在分摊
        if (StringUtils.equals(orderApportionDepot.getApportionMessage(), OrderEnum.APPORTION_EXECUTE_STATUS.apportion_execute_status_doing.name())) {
            MemcachedUtil.getInstance().remove(memoryCacheKey);
            logger.info("executePayedMsg has calcApportionAmount and orderId = " + orderId);
            return;
        }

        orderApportionDepot.setApportionMessage(OrderEnum.APPORTION_EXECUTE_STATUS.apportion_execute_status_doing.name());
        orderApportionDepot.setUpdateTime(Calendar.getInstance().getTime());
        //保存进DB
        orderApportionDepotService.updateOrderApportionDepot(orderApportionDepot);
        try {
            //如果分摊来源是价格修改，要先作废之前的分摊记录，然后重新分摊下单项和价格修改项
            this.checkManualChangePriceRedo(orderApportionDepot);
            //如果订单尚未进行下单项分摊，先做下单项分摊
            if (!StringUtils.equals(orderApportionDepot.getApportionStatus(), OrderEnum.APPORTION_STATUS.apportion_status_booking_completed.name())) {
                orderAmountApportionService.calcAndSaveBookingApportionAmount(orderId);
            }
            logger.info("executePayedMsg booking subjects apportion completed, order is " + orderId);
            //进行实付分摊计算
            orderAmountApportionService.apportionAndSaveActualPaidAmount(orderId);
            int result = orderApportionDepotService.deleteByPrimaryKey(orderApportionDepot.getOrderApportionId());
            logger.info("executePayedMsg success! orderId=" + orderId + " and result count =" + result);
            this.createApportionSuccessMsg(orderId);
        } catch (Exception e) {
            logger.error("executePayedMsg error:orderId=" + orderId + " and error info :" + ExceptionFormatUtil.getTrace(e));
            orderApportionDepot.setApportionStatus(OrderEnum.APPORTION_STATUS.apportion_status_payment_failed.name());
            orderApportionDepot.setApportionMessage(e.getMessage());
            orderApportionDepot.setValidFlag(Constants.N_FLAG);
            orderApportionDepot.setUpdateTime(Calendar.getInstance().getTime());
            orderApportionDepotService.updateOrderApportionDepot(orderApportionDepot);
            logger.error("executePayedMsg orderApportionDepot save ValidFlag=N to db");
            throw new RuntimeException("orderId=" + orderId + " executePayedMsg 执行分摊计算异常---" + e.getMessage());
        }
        MemcachedUtil.getInstance().remove(memoryCacheKey);
        logger.info("executePayedMsg memoryCacheKey has remove and orderId=" + orderId);
    }

    /**
     * 创建分摊完成消息
     * @param orderId
     */
    public void createApportionSuccessMsg(Long orderId){
        logger.info("createApportionSuccessMsg createMsg  ORDER_APPORTION_SUCCESS_MSG"+orderId);
        orderMessageProducer.sendMsg(MessageFactory.newOrderApportionSuccessMessage(orderId));
    }

}
