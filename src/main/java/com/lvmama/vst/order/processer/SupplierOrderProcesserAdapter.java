package com.lvmama.vst.order.processer;

import com.lvmama.order.api.base.vo.RequestBody;
import com.lvmama.order.api.base.vo.ResponseBody;
import com.lvmama.order.inquiry.api.service.IApiSupplierInquiryService;
import com.lvmama.order.inquiry.vo.comm.api.OrderTwiceCancelVo;
import com.lvmama.order.inquiry.vo.comm.api.OrderUpdateVo;
import com.lvmama.order.inquiry.vo.comm.api.PersonUpdateVo;
import com.lvmama.order.inquiry.vo.comm.api.SupplierParamVo;
import com.lvmama.order.inquiry.vo.comm.response.InquirySuppOrderVo;
import com.lvmama.order.route.service.ICertifRouteService;
import com.lvmama.order.route.service.IOrder2RouteService;
import com.lvmama.vst.back.biz.po.BizEnum;
import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.goods.po.SuppGoodsTimePrice;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.back.pub.po.ComJobConfig;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.comm.jms.TopicMessageProducer;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.utils.MessageUtils;
import com.lvmama.vst.comm.utils.StringUtil;
import com.lvmama.vst.comm.vo.order.ComplexQuerySQLCondition;
import com.lvmama.vst.comm.vo.order.OrderFlagParam;
import com.lvmama.vst.comm.vo.order.OrderIndentityParam;
import com.lvmama.vst.order.service.IComplexQueryService;
import com.lvmama.vst.order.service.IOrdOrderItemPassCodeSMSService;
import com.lvmama.vst.order.service.IOrderUpdateService;
import com.lvmama.vst.order.vo.OrderSupplierOperateResult;
import com.lvmama.vst.supp.client.service.SupplierOrderOtherService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author kemeisong
 * @Title: SupplierOrderProcesserAdapter
 * @Description: TODO
 * @date 2018/10/25 19:29
 */
public class SupplierOrderProcesserAdapter implements MessageProcesser{
    private static final Log LOG = LogFactory.getLog(SupplierOrderProcesserAdapter.class);
    @Autowired
    private OrderService orderService;
    @Autowired
    private SupplierOrderProcesser supplierOrderProcesser;
    @Resource
    private IOrder2RouteService order2RouteService;
    @Resource(name="orderCertifMessageProducer")
    private TopicMessageProducer orderCertifMessageProducer;
    @Autowired
    private IApiSupplierInquiryService apiSupplierInquiryService;
    @Autowired
    private IOrderUpdateService orderUpdateService;
    @Autowired
    private IOrdOrderItemPassCodeSMSService ordOrderItemPassCodeSMSLocalService;
    @Autowired
    private SupplierOrderOtherService supplierOrderOtherService;
    @Resource
    private ICertifRouteService certifRouteService;
    @Autowired
    private IComplexQueryService complexQueryService;

    @Override
    public void process(Message message) {
        LOG.info("SupplierOrderProcesserAdapter.process: objectId="+message.getObjectId()+",jms eventType=" + message.getEventType()+",systemType:"+message.getSystemType());

        Long  objectId= message.getObjectId();
        //是否走询单
        boolean isNewSys =false;
        OrdOrder order=null;
        if(isOrderMessage(message)){
            isNewSys=order2RouteService.isOrderRouteToNewSys(objectId);

            if(MessageUtils.isOrderCancelMsg(message)){
                 order=orderService.queryOrdorderByOrderId(objectId);
                //如果是酒店套餐取消走消息
                if(order.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_route_hotelcomb.getCategoryId())){
                    if(order.getNewWorkflowFlag() != null && order.getNewWorkflowFlag().equals("N")) {
                        isNewSys = false;
                    }else if(order.hasInfoAndResourcePass()&& order.hasPayed()){
                        isNewSys = true;
                    }
                }
                //如果是老工作流酒店取消走消息
                if(order.getCategoryId().equals(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId())&&order.getNewWorkflowFlag()!=null&&order.getNewWorkflowFlag().equals("N")){
                    isNewSys =false;
                }
            }
        }
        if(isOrderItemMessage(message)){
            isNewSys=order2RouteService.isOrderItemRouteToNewSys(objectId);
        }

        LOG.info("SupplierOrderProcesserAdapter.process: objectId="+message.getObjectId()+",isNewSys:"+isNewSys);
        if (!isNewSys) {
            //对接-模拟消息走原逻辑
            supplierOrderProcesser.process(message);
            //非对接-发送消息（转发之前所有发给ActiveMQ.VST_ORDER的消息）
            orderCertifMessageProducer.sendMsg(message);
        }else{
              if(MessageUtils.isOrderInfoPassMsg(message) ||MessageUtils.isOrderResourcePassMsg(message)
                      ||MessageUtils.isOrderPaymentMsg(message)){
                  //工作流切询单,消息不处理

              }else if(MessageUtils.isOrderCancelMsg(message)){
                  //取消单释放库存，临时解决线上问题，后期需迁入询单系统
                  try{
                      if(order==null){
                          order=orderService.queryOrdorderByOrderId(objectId);
                      }
                      String getSpecialTicket = getSpecialTicket(order);
                      //if(DisneyUtils.isDisney(order)){
                      if(StringUtil.isNotEmptyString(getSpecialTicket)) {
                          LOG.info("SupplierOrderProcesserAdapter.process: OrderCancelMsg,OrderID=" + message.getObjectId());
                          supplierOrderOtherService.freeSuppOderStock(objectId);
                          LOG.info("SupplierOrderProcesserAdapter.process: OrderCancelMsg,OrderID=" + message.getObjectId() + "end....");
                      }
                  }catch(Exception e){
                      LOG.info("SupplierOrderProcesserAdapter.freeSuppOderStock: [orderId="
                              + objectId + "] get failed." + e.getMessage());
                  }
              }else if(MessageUtils.hasOrderModifyMessage(message)){
                  modifyOrderProcess(message);
              }else if(MessageUtils.hasOrderModifyPersonMessage(message)){
                  modifyPersonProcess(message);
              }else if(MessageUtils.isOrderModifySettlementPriceMsg(message)){
                  modifySettlementProcess(message);
              }else if(MessageUtils.hasOrderMemoMessage(message)){
                 modifyMemoProcess(message);
              }else if(MessageUtils.isOrderTwiceCancelMsg(message)){
                  orderTwiceCancel(message);
              }else{
                supplierOrderProcesser.process(message);
                //除以上消息类型全部转发到vst_certif
                orderCertifMessageProducer.sendMsg(message);
            }
        }
    }

    //修改游玩人--调询单
    private void modifyPersonProcess(Message message){
        Long orderId = message.getObjectId();
        String str=OrderEnum.OrderModifyType.ModifyPerson.name()+":"+message.getAddition();
        RequestBody<PersonUpdateVo> requestBody=new RequestBody<PersonUpdateVo>();
        PersonUpdateVo param=new PersonUpdateVo();
        param.setOrderId(orderId);
        param.setAddition(str);
        requestBody.setT(param);
        ResponseBody<List<InquirySuppOrderVo>> result= apiSupplierInquiryService.updatePerson(requestBody);
    }


    //修改结算价--调询单
    private void modifySettlementProcess(Message message) {
        com.lvmama.order.api.base.vo.RequestBody<OrderUpdateVo> requestBody = new com.lvmama.order.api.base.vo.RequestBody<OrderUpdateVo>();
        OrderUpdateVo updateVo = new OrderUpdateVo();
        updateVo.setOrderItemId(message.getObjectId());
        updateVo.setAddition(message.getAddition());
        requestBody.setT(updateVo);
        ResponseBody<Void> result = apiSupplierInquiryService.updateSettlementPrice(requestBody);
    }
    //修改订单--调询单(目前仅支持非对接，对接仅支持修改游玩人)
    private void modifyOrderProcess(Message message) {
        Long orderId = message.getObjectId();
        OrderUpdateVo paramVo=new OrderUpdateVo();
        paramVo.setOrderId(message.getObjectId());
        paramVo.setAddition(message.getAddition());
        ResponseBody<Void> resultBd=apiSupplierInquiryService.updateCertif(new RequestBody<OrderUpdateVo>(paramVo));

    }

    //修改订单备注--调询单
    private void modifyMemoProcess(Message message) {
        com.lvmama.order.api.base.vo.RequestBody<OrderUpdateVo> requestBody=new com.lvmama.order.api.base.vo.RequestBody<OrderUpdateVo>();
        OrderUpdateVo updateVo=new OrderUpdateVo();
        updateVo.setOrderItemId(message.getObjectId());
        updateVo.setAddition(message.getAddition());
        requestBody.setT(updateVo);
        com.lvmama.order.api.base.vo.ResponseBody<Void> result= apiSupplierInquiryService.updateOrderMemo(requestBody);
    }

    //订单二次取消--调询单
    private void orderTwiceCancel(Message message) {
        com.lvmama.order.api.base.vo.RequestBody<OrderTwiceCancelVo> requestBody=new com.lvmama.order.api.base.vo.RequestBody<OrderTwiceCancelVo>();
        OrderTwiceCancelVo cancelVo=new OrderTwiceCancelVo();
        cancelVo.setOrderItemId(message.getObjectId());
        cancelVo.setAddition(message.getAddition());
        requestBody.setT(cancelVo);
        com.lvmama.order.api.base.vo.ResponseBody<Void> result= apiSupplierInquiryService.orderTwiceCancel(requestBody);
    }


    private boolean isOrderMessage(Message message){
        if(MessageUtils.isOrderCreateMsg(message)){
            return true;
        }else if(MessageUtils.isOrderInfoPassMsg(message)){
            return true;
        }else if(MessageUtils.isOrderResourcePassMsg(message)){
            return true;
        }else if(MessageUtils.isOrderPaymentMsg(message)){
            return true;
        }else if(MessageUtils.isOrderCancelMsg(message)) {
            return true;
        }else if(MessageUtils.hasOrderModifyMessage(message)){
            return true;
        }else if(MessageUtils.hasOrderModifyPersonMessage(message)){
            return true;
        }
        return false;
    }

    private boolean isOrderItemMessage(Message message){
        if(MessageUtils.isOrderModifySettlementPriceMsg(message)){
            return true;
        }else if(MessageUtils.hasOrderMemoMessage(message)){
            return true;
        }else if(MessageUtils.isOrderTwiceCancelMsg(message)){
            return true;
        }
        return false;
    }


    /**
     * 根据OrderId返回单个带有订单子项Order对象
     *
     * @param orderId
     * @return
     */
    private OrdOrder getOrderWithOrderItemByOrderId(Long orderId) {
        OrdOrder order = null;
        ComplexQuerySQLCondition condition = new ComplexQuerySQLCondition();

        OrderIndentityParam orderIndentityParam = new OrderIndentityParam();
        orderIndentityParam.setOrderId(orderId);

        OrderFlagParam orderFlagParam = new OrderFlagParam();
        orderFlagParam.setOrderItemTableFlag(true);

        condition.setOrderIndentityParam(orderIndentityParam);
        condition.setOrderFlagParam(orderFlagParam);

        List<OrdOrder> orderList = complexQueryService
                .queryOrderListByCondition(condition);
        if (orderList != null && orderList.size() == 1) {
            order = orderList.get(0);
        }
        return order;
    }
    public String getSpecialTicket(OrdOrder order) {
        String specialTicketType = "";
        if (CollectionUtils.isEmpty(order.getOrderItemList())) {
            return specialTicketType;
        }
        for (OrdOrderItem item : order.getOrderItemList()) {
            specialTicketType = item.getContentStringByKey("specialTicketType");
            if(StringUtil.isNotEmptyString(specialTicketType)){
                break;
            }
        }
        LOG.info("orderId==="+order.getOrderId()+"specialTicketType==="+specialTicketType);
        return specialTicketType;
    }

}
