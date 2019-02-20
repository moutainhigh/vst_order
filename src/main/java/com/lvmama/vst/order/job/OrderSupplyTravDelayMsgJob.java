package com.lvmama.vst.order.job;

import java.util.*;

import com.lvmama.vst.comm.utils.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.order.po.OrdAccInsDelayInfo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.dao.OrdAccInsDelayInfoDao;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.processer.OrderSmsSendProcesser;
import com.lvmama.vst.order.service.IOrderUpdateService;

/** 
 * @Title: OrderSupplyTravDelayMsgJob.java 
 * @Package com.lvmama.vst.order.job 
 * @Description: Job 用于从支付完成到过游玩人补充等待时间一半，发送短信提醒补充游玩人
 * @author Wangsizhi
 * @date 2016-12-24 下午4:47:36 
 * @version V1.0.0 
 */
public class OrderSupplyTravDelayMsgJob implements Runnable {
    
    @Autowired
    private IOrderUpdateService orderUpdateService;
    @Autowired
    private OrderSmsSendProcesser orderSmsSendProcesser;
    @Autowired
    private OrdOrderDao ordOrderDao;
    @Autowired
    private OrdAccInsDelayInfoDao ordAccInsDelayInfoDao;
    
    private static final Log LOG = LogFactory.getLog(OrderQuitInsAccJob.class);
    @Override
    public void run() {
        if(Constant.getInstance().isJobRunnable()){
            LOG.info("OrderSupplyTravDelayMsgJob start");
            
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("travDelayFlag", "Y");
            param.put("travDelayStatus", "UNCOMPLETED");
            param.put("adTravRemindStatus", "NO_SEND");
            
            List<OrdAccInsDelayInfo> accInsDelayHalfWaitTimeRemindOrderList = ordAccInsDelayInfoDao.getAccInsDelayHalfWaitTimeRemindOrderList(param);
            
            if (null != accInsDelayHalfWaitTimeRemindOrderList && accInsDelayHalfWaitTimeRemindOrderList.size() > 0) {
                for (OrdAccInsDelayInfo ordAccInsDelayInfo : accInsDelayHalfWaitTimeRemindOrderList) {
                    Long orderId = ordAccInsDelayInfo.getOrderId();
                    LOG.info("OrderSupplyTravDelayMsgJob orderId = " + orderId);
                    
                    String orderStatus = null;
                    String paymentStatus = null;

                    OrdOrder ordOrder = orderUpdateService.queryOrdOrderByOrderId(orderId);
                    if (null != ordOrder) {
                        orderStatus = ordOrder.getOrderStatus();
                        paymentStatus = ordOrder.getPaymentStatus();
                    }
                    
                    if (StringUtils.isNotBlank(orderStatus)
                            && Constant.ORD_ORDER_STATUS.NORMAL.name().equalsIgnoreCase(orderStatus)
                            &&StringUtils.isNotBlank(paymentStatus)
                            && OrderEnum.PAYMENT_STATUS.PAYED.name().equalsIgnoreCase(paymentStatus)) {
                        Date delayWaitTime = ordAccInsDelayInfo.getTravDelayWaitTime();
                        Date paymentTime = ordOrder.getPaymentTime();
                        int hour = DateUtil.getHour(paymentTime, delayWaitTime);
                        Calendar calendar = Calendar.getInstance();
                        int currHour = calendar.get(Calendar.HOUR_OF_DAY);
                        LOG.info("OrderId" + orderId + "sent conditions."+hour+","+currHour);
                        /**
                         * 1、支付时间和过期时间差小于24小时，直接发
                         * 2、支付时间和过期时间差大于24小时，并且当前小时大于7点，直接发
                         * 3、其它，顺延
                         */
                        if(hour <= 24 || (hour > 24 && currHour >= 7)){
                            orderSmsSendProcesser.sendSms(MessageFactory.sendInsAccOrderDelayRemindMessage(orderId, ""), ordOrder);
                            LOG.info("OrderId" + orderId + "sent supply accInsDelayTrav remind success.");
                            //更新订单后置信息表提醒短信发送状态为已提醒
                            ordAccInsDelayInfo.setAdTravRemindStatus("SEND");
                            ordAccInsDelayInfoDao.updateByPrimaryKey(ordAccInsDelayInfo);
                        }else{
                            return;
                        }
                    }else {
                        LOG.info("sendInsAccOrderDelayRemindMessage fail order = " + orderId 
                                + "   orderStatus = " + orderStatus + "   paymentStatus = " + paymentStatus);
                    }
                    

                }
            }
        }
    }

}
