package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.OrdRefundItem;

/**
 * Created by zhouyanqun on 2017/4/11.
 */
public interface OrdRefundItemService {
    /**
     * 保存新记录
     * */
    int saveOrdRefundItem(OrdRefundItem ordRefundItem);
    
    /**
     * 保存记录
     * @param ordRefundItem
     * @return
     */
    public int saveOrdRefundItemSelective(OrdRefundItem ordRefundItem);
    
    
}
