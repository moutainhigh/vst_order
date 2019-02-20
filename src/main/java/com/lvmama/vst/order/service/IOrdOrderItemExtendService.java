package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.OrdOrderItemExtend;


/**
 * @author limingkai
 * @Description
 * @date 2018/11/2
 */


public interface IOrdOrderItemExtendService {

    /**
     * 删除子单外币记录
     * @param orderItemId
     * @return
     */
    int deleteByPrimaryKey(Long orderItemId);

    /**
     * 新增子单外币记录
     * @param ordOrderItemExtend
     * @return
     */
    int insert(OrdOrderItemExtend ordOrderItemExtend);

    /**
     * 根据子单号获取外币记录
     * @param orderItemId
     * @return
     */
    OrdOrderItemExtend selectByPrimaryKey(Long orderItemId);

    /**
     * 更新子单外币记录
     * @param ordOrderItemExtend
     * @return
     */
    int updateByPrimaryKeySelective(OrdOrderItemExtend ordOrderItemExtend);
}
