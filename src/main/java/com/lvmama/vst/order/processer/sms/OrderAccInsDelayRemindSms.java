package com.lvmama.vst.order.processer.sms;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.order.po.OrdAccInsDelayInfo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdSmsTemplate;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.utils.order.OrdOrderUtils;
import com.lvmama.vst.comm.utils.order.PriceUtil;
import com.lvmama.vst.comm.vo.Constant;

/** 
 * @Title: OrderAccInsDelayRemindSms.java 
 * @Package com.lvmama.vst.order.processer.sms 
 * @Description: TODO 
 * @author wangsizhi
 * @date 2017-1-23 下午4:20:06 
 * @version V1.0.0 
 */
public class OrderAccInsDelayRemindSms implements AbstractSms {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(OrderAccInsDelayRemindSms.class);
    
    private OrdAccInsDelayInfo ordAccInsDelayInfo;
    
    public OrderAccInsDelayRemindSms(OrdAccInsDelayInfo ordAccInsDelayInfo) {
        this.ordAccInsDelayInfo = ordAccInsDelayInfo;
    }
    
    public OrdAccInsDelayInfo getOrdAccInsDelayInfo() {
        return ordAccInsDelayInfo;
    }

    public void setOrdAccInsDelayInfo(OrdAccInsDelayInfo ordAccInsDelayInfo) {
        this.ordAccInsDelayInfo = ordAccInsDelayInfo;
    }

    @Override
    public List<String> exeSmsRule(OrdOrder order) {
     Long orderId = order.getOrderId();
        
        logger.info("OrderCancelSms ===>>> AccInsDelayRemind(order)=" + order.getOrderId()); 
        //发送规则列表
        List<String> sendList = new ArrayList<String>();
        
        //不发送规则列表
        List<String> noneSendList = new ArrayList<String>();
        
        String tag1 = "+++++++++++++++++++++++++++++++++++";
        String tag = tag1 + "\n" + tag1 + "\n" + tag1 + "\n" +
                tag1 + "\n" +tag1 + "\n" + tag1 + "\n";
        
        logger.info(tag);
        logger.info("OrderId = " + order.getOrderId());
        logger.info("TravDelayFlag = " + ordAccInsDelayInfo.getTravDelayFlag());
        logger.info("TravDelayStatus = " + ordAccInsDelayInfo.getTravDelayStatus());
        logger.info("PaymentStatus = " + order.getPaymentStatus());
        logger.info("OrderStatus = " + order.getOrderStatus());
        
        if(isAccInsDelay(order) && isPayed(order) && orderStatus(order)){
            sendList.add(OrdSmsTemplate.SEND_NODE.ACCINS_DELAY_REMIND.name());
        }
       else{
            if (logger.isWarnEnabled()) {
                logger.warn("exeSmsRule(OrdOrder) - don't found OrderAccInsDelayRemind template"); //$NON-NLS-1$
            }               
        }
        
        if(noneSendList.size() >0){
            for(String noneSend : noneSendList){
                if(sendList.contains(noneSend)){
                    sendList.remove(noneSend);
                }
            }
        }
        return sendList;
    }

    @Override
    public String fillSms(String content, OrdOrder order) {
        // TODO Auto-generated method stub
        return null;
    }
    //意外险后置订单
    public boolean isAccInsDelay(OrdOrder order){
        String travDelayFlag = ordAccInsDelayInfo.getTravDelayFlag();
        String travDelayStatus = ordAccInsDelayInfo.getTravDelayStatus();
        
        if (StringUtils.isNotBlank(travDelayFlag) && StringUtils.equalsIgnoreCase(travDelayFlag, "Y")
                && StringUtils.isNotBlank(travDelayStatus) 
                && StringUtils.equalsIgnoreCase(travDelayStatus,  OrderEnum.ORDER_TRAV_DELAY_STATUS.UNCOMPLETED.name())) {
            return true;
        }
        
        return false;
    }
    public boolean isPayed(OrdOrder order) {
            if (OrderEnum.PAYMENT_STATUS.PAYED.name().equalsIgnoreCase(
                    order.getPaymentStatus())) {
                return true;
            }
            return false;
    }
    public boolean  orderStatus(OrdOrder order){
        if(Constant.ORDER_STATUS_ENUM.NORMAL.name().equalsIgnoreCase(order.getOrderStatus())){
            return true;
        }
        return false;
    }

}
