package com.lvmama.vst.order.confirm.factory;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.pub.po.ComActivitiRelation;
import com.lvmama.vst.comm.utils.ActivitiUtils;
import com.lvmama.vst.comm.vo.ActivitiKey;
import com.lvmama.vst.order.confirm.service.impl.OrdItemConfirmProcessServiceImpl;
import com.lvmama.vst.order.service.IOrderLocalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ActivitiKey
 */
public class ActivitiKeyFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ActivitiKeyFactory.class);
    /**
     * 确认子流程key
     * @param item
     * @param type
     * @return
     */
    public static ActivitiKey createKeyByOrderItem_confirm(OrdOrderItem item,String type){
        LOG.info("item.orderid="+item.getOrderId()+",type="+type);
        return new ActivitiKey((String) null,ActivitiUtils.createItemConfirmBussinessKey(item,type));
    }
    /**
     * o2o流程key
     * @param relation
     * @param order
     * @return
     */
    public static ActivitiKey createKeyByOrder_O2O(ComActivitiRelation relation,OrdOrder order){
        LOG.info("order.orderid="+order.getOrderId());
        return new ActivitiKey(relation, ActivitiUtils.createO2OBussinessKey(order));
    }
    public static ActivitiKey createKeyByOrderItem_O2O(OrdOrderItem item,String type){
        LOG.info("item.orderid="+item.getOrderId());
        return new ActivitiKey((String)null, ActivitiUtils.createO2OItemBussinessKey(item, type));
    }
}
