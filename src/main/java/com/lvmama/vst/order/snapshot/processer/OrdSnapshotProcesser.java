package com.lvmama.vst.order.snapshot.processer;

import com.lvmama.vst.back.client.ord.service.OrderService;
import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.comm.jms.Message;
import com.lvmama.vst.comm.jms.MessageProcesser;
import com.lvmama.vst.order.snapshot.async.IVstMsgRecoupSnapshotService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Resource;

/**
 * 异步订单快照
 */
public class OrdSnapshotProcesser implements MessageProcesser {
    private static final Log LOG = LogFactory.getLog(OrdSnapshotProcesser.class);
    @Resource
    private OrderService orderService;
    @Resource
    private IVstMsgRecoupSnapshotService vstMsgRecoupSnapshotService;

    /**
     * 异步消息快照
     * @param message
     */
    @Override
    public void process(Message message) {
        Long objectId = message.getObjectId();
        if (LOG.isInfoEnabled()) {
            LOG.info("Start OrdSnapshotProcesser type:" + message.getEventType()
                    + ",objectType: " + message.getObjectType()
                    + ",objectId:" + objectId);
        }
        /*if(!MessageUtils.hasOrderModifyMessage(message)
                || StringUtils.isEmpty(addition)) return;*/

        OrdOrder ordOrder = orderService.queryOrdorderByOrderId(objectId);
        if(CollectionUtils.isEmpty(ordOrder.getOrderItemList())) return;
        try {
            //recoup
            vstMsgRecoupSnapshotService.recoupProdProduct(ordOrder);
            vstMsgRecoupSnapshotService.recoupSuppGoods(ordOrder);

        }catch (Exception e) {
            LOG.error("OrdSnapshotProcesser exception :" +e);
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("End OrdSnapshotProcesser objectId:" + objectId);
        }
    }

}
