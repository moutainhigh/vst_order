package com.lvmama.vst.neworder.order.create.persistance.category;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.order.vo.OrdOrderDTO;

/**
 * Created by dengcheng on 17/2/22.
 */
public interface IOrderDbStoreFactory {
    /**
     * 订单项目持久化
     * @param order
     * @return
     */
    OrdOrder persistanceOrder(OrdOrderDTO order);

    /**
     * 设置订单为正常
     * @param order
     */
    void setOrderNormal(OrdOrderDTO order);

}
