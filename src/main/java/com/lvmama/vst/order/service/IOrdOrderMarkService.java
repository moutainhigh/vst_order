package com.lvmama.vst.order.service;

import com.lvmama.vst.back.order.po.OrdOrderMark;
import com.lvmama.vst.comm.web.BusinessException;

public interface IOrdOrderMarkService {
    /**
     * 保存搬单
     * 
     * @param ordOrderMark
     * @return
     * @throws BusinessException
     */
    Long saveOrdOrderMark(OrdOrderMark ordOrderMark) throws BusinessException;

    /**
     * 更新搬单
     * 
     * @param ordOrderMark
     * @return
     * @throws BusinessException
     */
    int updateOrdOrderMark(OrdOrderMark ordOrderMark) throws BusinessException;

    /**
     * 根据订单ID查询搬单
     * 
     * @param orderId
     *            订单ID
     * @return
     * @throws BusinessException
     */
    OrdOrderMark findOrdOrderMarkByOrderId(Long orderId) throws BusinessException;

}
