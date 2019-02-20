package com.lvmama.vst.order.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdAccInsDelayInfo;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.comm.vo.Constant;
import com.lvmama.vst.order.dao.OrdAccInsDelayInfoDao;
import com.lvmama.vst.order.service.IOrderUpdateService;

/** 
 * @Title: OrderQuitInsAccJob.java 
 * @Package com.lvmama.vst.order.job 
 * @Description: Job 用于过游玩人补充等待时间自动弃保意外险
 * @author Wangsizhi
 * @date 2016-12-24 下午4:46:12 
 * @version V1.0.0 
 */
public class OrderQuitInsAccJob implements Runnable {

    private static final Log LOG = LogFactory.getLog(OrderQuitInsAccJob.class);
    
    @Autowired
    private IOrderUpdateService orderUpdateService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrdAccInsDelayInfoDao ordAccInsDelayInfoDao;
    
    @Override
    public void run() {
        if(Constant.getInstance().isJobRunnable()){
            LOG.info("OrderQuitInsAccJob start");
            
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("travDelayFlag", "Y");
            param.put("travDelayStatus", "UNCOMPLETED");
            List<OrdAccInsDelayInfo> accInsDelayTimeoutOrderIdList = ordAccInsDelayInfoDao.getAccInsDelayTimeoutOrderList(param);
            
            if (null != accInsDelayTimeoutOrderIdList && accInsDelayTimeoutOrderIdList.size() > 0 ) {
                for (OrdAccInsDelayInfo ordAccInsDelayInfo : accInsDelayTimeoutOrderIdList) {
                    Long orderId = ordAccInsDelayInfo.getOrderId();
                    LOG.info("OrderQuitInsAccJob orderId = " + orderId);
                    
                    String orderStatus = null;
                    String paymentStatus = null;

                    OrdOrder ordOrder = orderUpdateService.queryOrdOrderByOrderId(orderId);
                    if (null != ordOrder) {
                        orderStatus = ordOrder.getOrderStatus();
                        paymentStatus = ordOrder.getPaymentStatus();
                    }
                    
                    if (StringUtils.isNotBlank(orderStatus)
                            && Constant.ORD_ORDER_STATUS.CANCEL.name().equalsIgnoreCase(orderStatus) ) {
                        ordAccInsDelayInfo.setTravDelayStatus(OrderEnum.ORDER_TRAV_DELAY_STATUS.CANCEL.name());
                        ordAccInsDelayInfoDao.updateByPrimaryKey(ordAccInsDelayInfo);
                        LOG.info("OrderQuitInsAccJob order=" + orderId + "---orderStatus=" + orderStatus + "update TravDelayStatus to CANCEL");
                        return;
                    }
                    
                    if (StringUtils.isNotBlank(orderStatus)
                            && Constant.ORD_ORDER_STATUS.NORMAL.name().equalsIgnoreCase(orderStatus)
                            &&StringUtils.isNotBlank(paymentStatus)
                            && OrderEnum.PAYMENT_STATUS.PAYED.name().equalsIgnoreCase(paymentStatus)) {
                        orderService.cancelTravDelayOrderItems(ordAccInsDelayInfo.getOrderId(), null, null);
                    }else {
                        LOG.info("OrderQuitInsAccJob fail order = " + orderId
                                + "   orderStatus = " + orderStatus + "   paymentStatus = " + paymentStatus);
                    }
                    
                }
            }
        }
    }

}
