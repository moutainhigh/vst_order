package com.lvmama.vst.order.dao;

import com.lvmama.vst.back.order.po.OrdRefundItem;

/**
 * Created by zhouyanqun on 2017/4/11.
 */
public interface OrdRefundItemDao {
    /**
     * 插入新记录
     * */
    int insert(OrdRefundItem ordRefundItem);
    
    
    
    public int insertSelective(OrdRefundItem ordRefundItem);
}
