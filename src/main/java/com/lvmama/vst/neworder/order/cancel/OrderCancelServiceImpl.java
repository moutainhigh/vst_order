package com.lvmama.vst.neworder.order.cancel;

import com.google.common.collect.Maps;
import com.lvmama.dest.hotel.trade.hotelcomb.vo.OrderCancelVo;
import com.lvmama.vst.back.biz.po.BizDictDef;
import com.lvmama.vst.back.client.biz.service.DictDefClientService;
import com.lvmama.vst.back.client.pub.service.ComLogClientService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.order.po.OrderStatusEnum;
import com.lvmama.vst.back.pub.po.ComLog;
import com.lvmama.vst.comm.jms.MessageFactory;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.SynchronizedLock;
import com.lvmama.vst.neworder.order.EnhanceBeanUtils;
import com.lvmama.vst.neworder.order.cancel.category.hotelcomb.chain.ICancelChain;
import com.lvmama.vst.neworder.order.cancel.category.hotelcomb.eventbus.OrderCancelEventBus;
import com.lvmama.vst.neworder.order.cancel.vo.OrderCancelInfo;
import com.lvmama.vst.order.dao.OrdOrderDao;
import com.lvmama.vst.order.dao.OrdOrderStockDao;
import com.lvmama.vst.order.service.*;
import com.lvmama.vst.order.service.book.NewHotelComOrderBussiness;
import com.lvmama.vst.order.service.book.NewHotelComOrderInitService;
import com.lvmama.vst.order.service.book.OrderSaveService;
import com.lvmama.vst.order.service.impl.OrdOrderUpdateServiceImpl;
import com.lvmama.vst.order.timeprice.service.impl.NewOrderHotelCompTimePriceServiceImpl;
import com.lvmama.vst.order.timeprice.service.impl.OrderTicketNoTimePriceServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by dengcheng on 17/2/21.
 */
@Component("orderCancelServiceRemote")
public class OrderCancelServiceImpl implements IOrderCancelService {

    private static final Logger LOG = LoggerFactory
            .getLogger(OrderCancelServiceImpl.class);


    @Autowired
    private NewHotelComOrderBussiness newHotelComOrderBussiness;
    @Autowired
    private OrdOrderStockDao orderStockDao;
    @Autowired
    private NewHotelComOrderInitService newHotelComOrderInitService;
    @Autowired
    private NewOrderHotelCompTimePriceServiceImpl orderHotelComp2HotelTimePriceService;

    @Autowired
    protected IComplexQueryService complexQueryService;

    @Autowired
    private ComActivitiRelationService comActivitiRelationService;

    private static ConcurrentMap<String, Long> bookUniqueMap = new ConcurrentHashMap<String, Long>();

    @Autowired
    private OrderSaveService orderSaveService;

    @Autowired
    private IOrdOrderTrackingService ordOrderTrackingService;




    @Resource(name = "orderMessageProducer")
    private TopicMessageProducer orderMessageProducer;

    @Autowired
    private IOrderUpdateService orderUpdateService;


    @Autowired
    private OrderTicketNoTimePriceServiceImpl orderTicketNoTimePriceServiceImpl;


    @Resource
    private ComLogClientService comLogClientService;

    @Resource
    private DictDefClientService dictDefClientService;

    @Resource
    List<ICancelChain> hotelCombCancelProcessorChain;

    @Resource
    IOrdOrderService orderService;

    @Resource
    private OrdOrderDao orderDao;

    @Resource
    OrderCancelEventBus newOrderCancelEventBus;


    private boolean isBackUser(String operatorId) {
        boolean isBackUser = false;
        if (operatorId != null) {
            if ("system".equalsIgnoreCase(operatorId)
                    || "admin".equalsIgnoreCase(operatorId)
                    || operatorId.startsWith("cs")
                    || operatorId.startsWith("lv")) {
                isBackUser = true;
            }
        }

        return isBackUser;
    }


    public void cancelForWorkFlow(OrderCancelInfo info){
        Long orderId = info.getOrderId();
//        String cancelCode = info.getCancelCode();
//        String reason = info.getReason();
//        String memo = info.getMemo();
//        String operatorId = info.getOperatorId();
        OrdOrder order =  complexQueryService.queryOrderByOrderId(orderId);
        LOG.info("cancelForWorkFlow==== newOrderCancelEventBus.post==orderId"+orderId);
        newOrderCancelEventBus.post(order);

    }

    public void doOrderCancel(OrderCancelInfo info){

        Long orderId = info.getOrderId();
        String cancelCode = info.getCancelCode();
        String reason = info.getReason();
        String memo = info.getMemo();
        String operatorId = info.getOperatorId();
        
        LOG.info("OrderCancelServiceImpl.doOrderCancel start,order id:" + orderId);

        final String key = "VST_CANCEL_ORDER_" + orderId;
//        Map<String,Object> additionMap = Maps.newHashMap();
//        additionMap.put("orderId",orderId);
//        additionMap.put("cancelCode",cancelCode);
//        additionMap.put("reason",reason);
//        additionMap.put("memo",memo);

        try {
            if (SynchronizedLock.isOnDoingMemCached(key)) {
                throw new com.lvmama.dest.hotel.trade.utils.BusinessException(String.format("%s,%s",OrderStatusEnum.ORDER_ERROR_CODE.REPEAT_CANCEL_ORDER
                        .getErrorCode(),"不能重复操作"));
            }

            OrdOrder order =  complexQueryService.queryOrderByOrderId(orderId);

            if(order.isCancel()){
                throw new com.lvmama.dest.hotel.trade.utils.BusinessException(String.format("%s,%s",OrderStatusEnum.ORDER_ERROR_CODE.CANCELED_ORDER
                        .getErrorCode(),"订单已取消"));
            }

            if (!isBackUser("admin") && !order.isCanCancel()) {
                throw new com.lvmama.dest.hotel.trade.utils.BusinessException(String.format("%s,%s",OrderStatusEnum.ORDER_ERROR_CODE.CANCEL_ORDER_ERROR
                        .getErrorCode(),"订单不能取消，请联系客服"));
            }



            
            // 更新订单状态为取消
//            int effects = updateOrderToCancel(orderId, cancelCode, reason, memo, operatorId);
//            if(effects == 0){
//            	 LOG.error("order cancel error");
//                 throw new com.lvmama.dest.hotel.trade.utils.BusinessException(String.format("%s,%s",OrderStatusEnum.ORDER_ERROR_CODE.CANCEL_ORDER_ERROR
//                         .getErrorCode(),"订单取消失败，请联系客服"));
 //           }else{
//	            LOG.info("db update success");
	            insertOrderLog(order,
	                    OrderEnum.ORDER_STATUS.CANCEL.getCode(),
	                    operatorId, memo, cancelCode, reason);
	            
//	            LOG.info("OrderCancelServiceImpl send OrderCancelMessage");
//	            String addition = cancelCode + "_=_" + reason + "_=_" + operatorId;
//	            orderMessageProducer.sendMsg(MessageFactory.newOrderCancelMessage(orderId, addition));
//            }

            /**
             * chain 模式处理需要同步提交的逻辑
             */
            for (Iterator<ICancelChain> iter = hotelCombCancelProcessorChain.iterator(); iter.hasNext(); ) {
                ICancelChain chain = iter.next();
                chain.chain(order);
            }
            
        } catch (com.lvmama.dest.hotel.trade.utils.BusinessException t) {
            throw  new RuntimeException(t.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
            throw new com.lvmama.dest.hotel.trade.utils.BusinessException(String.format("%s,%s",OrderStatusEnum.ORDER_ERROR_CODE.CANCEL_ORDER_ERROR
                    .getErrorCode(),"订单取消失败，请联系客服"));
        } finally {
            SynchronizedLock.releaseMemCached(key);
        }
    }
    
    /**
     * 更新订单状态为取消
     * @param orderId
     * @param cancelCode
     * @param reason
     * @param memo
     * @return
     */
    private int updateOrderToCancel(Long orderId, String cancelCode, String reason, String memo, String operatorId){
    	Date cancleTime = new Date();
        OrdOrder updateOrder = new OrdOrder();
        updateOrder.setOrderId(orderId);
        updateOrder.setOrderStatus(OrderEnum.ORDER_STATUS.CANCEL.name());
        updateOrder.setCancelCode(cancelCode);
        if (reason != null && reason.length() > OrdOrderUpdateServiceImpl.CANCEL_REASON_MAX_LEN) {
            LOG.info("OrdOrderUpdateServiceImpl.updateOrderForCancel:reason.length > " + OrdOrderUpdateServiceImpl.CANCEL_REASON_MAX_LEN + ",reason=" + reason);
            reason = reason.substring(0, OrdOrderUpdateServiceImpl.CANCEL_REASON_MAX_LEN);
        }

        updateOrder.setReason(reason);
        updateOrder.setCancelTime(cancleTime);
        updateOrder.setOrderMemo(memo);
        int effects = orderDao.updateByPrimaryKeySelective(updateOrder);
        if(effects>0){
        	   LOG.info("OrdOrderUpdateServiceImpl.updateByPrimaryKeySelective success"+effects);
        }else{
        	return  effects ;
        }
        // 还酒店套餐库存
        OrderCancelInfo orderCancelInfo = new OrderCancelInfo();
        orderCancelInfo.setOrderId(orderId);
        orderCancelInfo.setCancelCode(cancelCode);
        orderCancelInfo.setReason(reason);
        orderCancelInfo.setMemo(memo);
        orderCancelInfo.setOperatorId(operatorId);
        this.cancelForWorkFlow(orderCancelInfo);
        return effects;
    }


    /**
     *
     * 保存日志
     *
     */
    public void insertOrderLog( OrdOrder order ,String type,String assignor,String memo,String cancelCode,String reason){
        if (order != null) {
            String zhOrderStatus = OrderEnum.ORDER_STATUS.getCnName(type);
            Long orderId=order.getOrderId();

            if (cancelCode != null && StringUtils.isNumeric(cancelCode)) {
                BizDictDef bizDictDef=dictDefClientService.findDictDefById(new Long(cancelCode)).getReturnContent();
                if (bizDictDef != null) {
                    cancelCode = bizDictDef.getDictDefName();
                }
            }

            if (cancelCode == null) {
                cancelCode = "默认";
            }

            //拼接日志内容
            String cancelStr="   取消类型："+ OrderEnum.ORDER_CANCEL_CODE.getCnName(cancelCode) +",取消原因："+reason;
            String content="将编号为["+orderId+"]的订单活动变更为["+ zhOrderStatus +"]"+cancelStr;

            if (order.isSupplierOrder()) {
                content+="。此订单为供应商订单，自动发送消息给供应商，等待供应商确认后才可会真正取消订单";
            }
            order.setCancelCode(cancelCode);
            order.setReason(reason);
            order.setBackUserId(assignor);
            order.setOrderMemo(memo);

            comLogClientService.insert(ComLog.COM_LOG_OBJECT_TYPE.ORD_ORDER_ORDER,
                    orderId,
                    orderId,
                    assignor,
                    content,
                    ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.name(),
                    ComLog.COM_LOG_LOG_TYPE.ORD_ORDER_CANCEL.getCnName()+"["+ zhOrderStatus +"]",
                    memo);
        }
    }


}
