package com.lvmama.vst.order.service;

import java.util.List;
import java.util.Map;

import com.lvmama.vst.comm.web.BusinessException;
import com.lvmama.vst.order.vo.OrdOrderMarkVo;

public interface IOrdOrderMarkReService {
    /**
     * 根据参数查询订单列表信息
     * 
     * @param params
     *            参数
     * @return 订单列表信息
     */
    List<OrdOrderMarkVo> findOrdOrderMarkResByParams(Map<String, Object> params) throws BusinessException;

    /**
     * 获取订单条数
     * 
     * @param params
     *            参数
     * @return
     * @throws BusinessException
     */
    int getTotalCount(Map<String, Object> params) throws BusinessException;
}
