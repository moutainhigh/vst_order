package com.lvmama.vst.order.confirm.service.status.impl;

import com.lvmama.order.enums.BizEnum;
import com.lvmama.vst.back.order.po.Confirm_Enum;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.pub.po.ComAudit;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.vo.ResultHandleT;
import com.lvmama.vst.order.confirm.ord.IOrdStatusManageConfirmService;
import com.lvmama.vst.order.confirm.service.status.IConfirmStatusService;
import com.lvmama.vst.order.confirm.vo.ConfirmStatusParamVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * 确认状态服务(默认实现)
 */
@Service("defaultConfirmStatusService")
public class DefaultConfirmStatusService implements IConfirmStatusService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultConfirmStatusService.class);
    @Autowired
    private IOrdStatusManageConfirmService ordStatusManageConfirmService;
    @Resource(name="myJmsTemplate")
    private JmsTemplate myJmsTemplate;
    /**
     * 更新子订单确认状态
     * @param confirmStatusParamVo
     * @return
     * @throws Exception
     */
    @Override
    public ResultHandleT<ComAudit> updateChildConfirmStatus(ConfirmStatusParamVo confirmStatusParamVo)
            throws Exception{
        //加载参数
        OrdOrderItem orderItem = confirmStatusParamVo.getOrderItem();
        Confirm_Enum.CONFIRM_STATUS newStatus =confirmStatusParamVo.getNewStatus();
        String operator =confirmStatusParamVo.getOperator();
        String memo =confirmStatusParamVo.getMemo();

        ResultHandleT<ComAudit> result =new ResultHandleT<ComAudit>();
        if(orderItem ==null ||newStatus ==null){
            result.setMsg("orderItem or newStatus is null");
            return result;
        }
        result =ordStatusManageConfirmService
                .updateChildConfirmStatusByAudit(orderItem,newStatus,operator,memo);
        if(BizEnum.BIZ_CATEGORY_TYPE.category_hotel.getCategoryId().equals(orderItem.getCategoryId())) {
            Message message = new Message();
            message.setEventType("ORDER_CERT_CONFIRM_MSG");
            message.setObjectId(orderItem.getOrderItemId());
            LOG.info("DefaultConfirmStatusService updateChildConfirmStatus send FINANCE msg start OrderId=" + orderItem.getOrderId());
            tpByfish(message);
            LOG.info("DefaultConfirmStatusService updateChildConfirmStatus send FINANCE msg success OrderId=" + orderItem.getOrderId());
        }
        LOG.info("OrderItemId=" + orderItem.getOrderItemId()
                +",newStatus=" +newStatus.name()
                +",isSuccess=" + result.isSuccess());
		/*if(result.isSuccess()
				&& result.getReturnContent() !=null){
			ordItemConfirmProcessService.completeTaskByAuditHasCompensated(orderItem, result.getReturnContent());
		}*/
        return result;
    }
    /**
     * 员工库处理
     * @param confirmStatusParamVo 接口参数
     * @return
     * @throws Exception
     */
    @Override
    public <T> T handle(ConfirmStatusParamVo confirmStatusParamVo)
            throws Exception{
        return null;
    }
    private boolean tpByfish(final Message message ) {
        try {
            myJmsTemplate.send("ActiveMQ.FINANCE.FISH", new MessageCreator() {
                @Override
                public javax.jms.Message createMessage(Session session) throws JMSException {
                    TextMessage textMessage = session.createTextMessage(String.valueOf(message.getObjectId()));
                    textMessage.setStringProperty("eventType", message.getEventType());
                    return textMessage;
                }
            });
            return true;
        }catch (Exception e) {
            LOG.error("DefaultConfirmStatusService tpByfish is error orderItemId:"+message.getObjectId() , e);
            return false;
        }
    }

}
