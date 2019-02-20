package com.lvmama.vst.order.service.impl;

import com.lvmama.order.vst.api.common.service.IApiOrdItemAdditionService;
import com.lvmama.order.vst.api.common.vo.request.OrdItemAdditionVo;
import com.lvmama.vst.back.order.po.OrdItemAddition;
import com.lvmama.vst.comm.utils.bean.EnhanceBeanUtils;
import com.lvmama.vst.order.service.IOrdItemAdditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 子订单附加信息桥接服务
 *
 * @author houjian
 * @date 2018/5/12.
 */
@Service("apiOrdItemAdditionService")
public class ApiOrdItemAdditionServiceImpl implements IApiOrdItemAdditionService {
    @Autowired
    private IOrdItemAdditionService ordItemAdditionService;

    @Override
    public OrdItemAdditionVo findOrdItemAdditionById(Long orderItemId) {
        OrdItemAddition source = this.ordItemAdditionService.findOrdItemAdditionById(orderItemId);
        if (source == null)
            return null;
        OrdItemAdditionVo target = new OrdItemAdditionVo();
        EnhanceBeanUtils.copyProperties(source, target);
        return target;
    }
}
